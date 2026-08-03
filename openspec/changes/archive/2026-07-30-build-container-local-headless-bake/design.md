## Context

`utilitarios/build_container.sh` (renomeado de `build.sh`) builda `flmane.dockerfile` e sobe o `docker-compose.yaml` local. A imagem hoje é tagueada `docker.io/sowbreira/flmane:latest` — prefixo adicionado no change `migrate-mariadb-podman` só para evitar o prompt interativo de registry do Podman ao fazer **pull** de imagens de terceiros (mariadb, phpmyadmin); para a imagem `flmane`, que é sempre construída localmente e nunca puxada de um registry no fluxo atual, esse prefixo é ruído — pior, sugere (incorretamente) que o compose pode tentar puxá-la do Docker Hub se a build falhar silenciosamente.

Separadamente, `MainLauncher.iniciarServidorHeadless` roda `preGerarImagensHeadless()` toda vez que o processo `--headless` sobe, escrevendo num diretório temporário criado do zero (`ImagensHeadlessDisco.iniciar()` → `criarDiretorioTemporarioSeguro`, sob `~/.flmane/tmp`). Em container, cada restart usa um filesystem de container novo (ou pelo menos um processo novo), então a pré-geração roda de novo — minutos de boot repetidos sem necessidade, já que os assets gerados (imagens de circuito/carro/capacete) só mudam se os XMLs/properties de origem mudarem, o que só acontece com uma nova imagem (novo build).

## Goals / Non-Goals

**Goals:**
- `build_container.sh` builda a imagem `flmane` e o compose a usa sempre local, sem qualquer interação com `docker.io` para essa imagem específica
- Pré-geração das imagens headless roda uma vez, no `docker build`, e fica embutida na imagem final
- Restart do container `flmane` (mesma imagem) não paga o custo de pré-geração de novo
- Comportamento de dev/local (`mvn`/jar direto, fora do Dockerfile) continua funcionando sem exigir a variável de ambiente nova

**Non-Goals:**
- Não mexer nas imagens `mariadb`/`phpmyadmin` (continuam pull de registry, prefixo `docker.io/` mantido nelas — são imagens de terceiros, não construídas localmente)
- Não introduzir cache/invalidação inteligente entre versões de dados (ex.: detectar que um circuito mudou e regenerar só aquele asset) — pré-geração continua sendo tudo-ou-nada, só o *quando* ela roda muda
- Não alterar a geração sob demanda de assets ausentes (`headless-imagens-disco` já cobre isso e continua igual)

## Decisions

### Tag da imagem `flmane` sem domínio de registry + `pull_policy: never`
`docker-compose.yaml` ganha:
```yaml
flmane:
  image: sowbreira/flmane:latest
  build:
    context: .
    dockerfile: flmane.dockerfile
  pull_policy: never
```
`pull_policy: never` (suportado pela Compose Spec, tanto `docker compose` quanto `podman compose`/`podman-compose` recentes) garante que o compose nunca tenta um `pull` — se a imagem não existir localmente, falha explicitamente em vez de tentar buscar no Docker Hub. `build:` deixa explícito que a imagem é sempre construída a partir do Dockerfile local; `docker compose up`/`podman compose up` sem imagem local ainda buildam automaticamente se necessário, mas o fluxo recomendado continua sendo `build_container.sh` (build explícito + compose up).

Alternativa considerada: manter o prefixo `docker.io/` só que sem publicar — rejeitada porque não resolve o problema real (o nome sugere um registry path que nunca é usado) e não impede tentativa de pull automática do compose se a imagem local sumir.

### Modo de pré-geração "assar" via novo argumento `--pre-gerar-imagens`
`MainLauncher.main` ganha um novo branch de argumento, paralelo ao `--headless` existente:
```java
if (contemArg(args, "--pre-gerar-imagens")) {
    assarImagensHeadless();
    return;
}
```
`assarImagensHeadless()` reaproveita a mesma sequência de `iniciarServidorHeadless(true)` até o fim de `preGerarImagensHeadless()` (ativa modo disco, chama `ImagensHeadlessDisco.iniciar()`, gera tudo), grava um arquivo marcador (`.pronto`) no diretório base ao final, e sai (`System.exit(0)`) sem jamais fazer bind de porta — puramente um passo de build, roda dentro de um `RUN` do Dockerfile e termina.

Alternativa considerada: gerar as imagens fora da JVM (script separado) — rejeitada porque duplicaria a lógica de desenho já em Java (`DesenhoProceduralCircuito`, `CarregadorRecursos`) sem ganho.

### Diretório de imagens fixo via variável de ambiente, com fallback pro comportamento atual
`ImagensHeadlessDisco.iniciar()` passa a checar `System.getenv("FLMANE_IMAGENS_HEADLESS_DIR")`:
- **Setada** (caso do Dockerfile): usa esse caminho fixo diretamente (cria se não existir), sem os subdiretórios ficarem sob um temp dir aleatório — é esse mesmo caminho que persiste na camada da imagem entre o `RUN` de build e o `ENTRYPOINT` de runtime, e entre restarts do mesmo container/imagem
- **Não setada** (dev local, jar rodado direto, GUI): mantém o comportamento atual — `criarDiretorioTemporarioSeguro`, diretório novo a cada boot

`MainLauncher.iniciarServidorHeadless(true)` passa a checar, antes de chamar `preGerarImagensHeadless()`, se o diretório base já contém o marcador `.pronto`; se sim, pula a pré-geração inteira (loga que está reaproveitando imagens já assadas) e segue direto pro bind da porta. Isso cobre tanto o caso "imagem já assada no build" (marcador presente, pula sempre) quanto o caso "diretório fixo setado mas vazio" (primeira execução sem bake prévio — ainda gera, e grava o marcador pra próxima vez).

Alternativa considerada: verificar timestamp/hash dos XMLs de origem em vez de um marcador simples — rejeitada como over-engineering; o marcador binário (assado ou não) é suficiente porque o único jeito de os dados de origem mudarem é uma nova imagem, que nunca reaproveita a camada com o marcador antigo (novo `COPY target/flmane.jar` invalida o cache de camada e força novo `RUN --pre-gerar-imagens`).

### `flmane.dockerfile`: `RUN` de bake logo após copiar o jar
```dockerfile
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY target/flmane.jar app.jar
ENV FLMANE_IMAGENS_HEADLESS_DIR=/app/imagens-headless
RUN java -Djava.awt.headless=true -jar app.jar --pre-gerar-imagens
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar","--headless"]
```
A pré-geração não depende de banco de dados (`ControleRecursos.carregarCircuitos()`/`CarregadorRecursos` só leem XML/properties do classpath), então roda dentro do `RUN` sem precisar de serviços externos disponíveis durante o `docker build`. `ENV` antes do `RUN` garante que tanto o processo de bake quanto o `ENTRYPOINT` final enxergam o mesmo caminho.

## Risks / Trade-offs

- [Risco] Falha de um asset específico durante o bake (já tratada, logada e pulada) ainda assim grava o marcador `.pronto` ao final → Mitigação: comportamento consistente com o requisito existente "geração sob demanda" — o asset que faltou é gerado na primeira requisição em runtime e persistido no mesmo diretório fixo, então o gap se resolve sozinho na primeira visita, sem exigir novo build
- [Risco] Build da imagem fica mais lento (a pré-geração roda uma vez a mais, durante `docker build`) → Aceito: é exatamente o trade-off pedido — mover o custo de "toda subida de container" para "toda vez que a imagem é reconstruída", que é bem mais raro
- [Risco] `pull_policy: never` quebra quem espera `docker compose up` sozinho (sem build local prévio) puxar a imagem de algum lugar → Mitigação: já não fazia sentido nesse fluxo (não há push pra registry); `build_container.sh` sempre builda antes de subir, e o `build:` no compose também cobre `docker/podman compose up --build`
- [Trade-off] Diretório fixo `/app/imagens-headless` dentro da imagem aumenta o tamanho final da imagem Docker (imagens de todos os circuitos/temporadas ativas ficam embutidas) → Aceito, é o ponto central do change: trocar tempo de boot repetido por tamanho de imagem construída uma vez

## Migration Plan

1. `src/main/java/br/flmane/recursos/ImagensHeadlessDisco.java`: `iniciar()` passa a checar a env var; adicionar helper de marcador `.pronto` (escrever/checar)
2. `src/main/java/br/flmane/MainLauncher.java`: novo branch `--pre-gerar-imagens` (assa e sai); `iniciarServidorHeadless(true)` pula `preGerarImagensHeadless()` quando o marcador já existe
3. `flmane.dockerfile`: `ENV FLMANE_IMAGENS_HEADLESS_DIR` + `RUN java ... --pre-gerar-imagens`
4. `utilitarios/build.sh` → `utilitarios/build_container.sh`, ajustando a tag da imagem para `sowbreira/flmane:latest` (sem `docker.io/`)
5. `docker-compose.yaml`: serviço `flmane` ganha `build:`/`pull_policy: never`, tag sem `docker.io/`
6. `README.md`/`README.pt-BR.md`: remover `docker push sowbreira/flmane`, atualizar caminho do script
7. `CLAUDE.md`: atualizar referência ao script de build (nome/caminho)
8. Rebuildar a imagem (`utilitarios/build_container.sh`) e validar: primeiro boot do container loga a pré-geração (durante o build, não no start do container); `podman logs`/`docker logs` do container em runtime não deve mostrar "PRE-GERANDO IMAGENS HEADLESS"; restart do mesmo container (`compose restart flmane`) sobe rápido e serve imagens normalmente

Rollback: reverter os commits; imagem antiga com o comportamento anterior continua funcional (pré-geração sempre no boot).

## Open Questions

(nenhuma)
