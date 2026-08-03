## Context

`docker-compose.yaml` atual sobe 4 serviços: `flmane` (app), `db` (MariaDB), `phpmyadmin` e `sonarqube`. Os dois últimos são ferramentas de desenvolvimento/qualidade, não parte do runtime do jogo. O Compose Spec (suportado por `docker compose` e `podman compose`/`podman-compose` recentes) tem um mecanismo nativo pra isso: qualquer arquivo `docker-compose.override.yaml` no mesmo diretório do `docker-compose.yaml` é mesclado automaticamente quando nenhum `-f` explícito é passado — é assim que o Compose já resolve "extras de dev" sem precisar de flag ou variável de ambiente.

## Goals / Non-Goals

**Goals:**
- Rodar só produção (`flmane`+`db`) com um comando explícito, sem editar arquivo
- Rodar dev (com `phpmyadmin`+`sonarqube`) continua sendo o comando padrão de sempre — zero mudança de hábito pra quem já usa `docker compose up -d`/`utilitarios/build_container.sh`
- Nenhuma duplicação da definição de `flmane`/`db` entre os dois arquivos

**Non-Goals:**
- Não introduzir profiles do Compose (`--profile`) como alternativa — override é mais simples pro caso de 2 ambientes fixos (dev sempre tem os extras, prod nunca tem) e não exige lembrar de passar `--profile` em dev
- Não mudar nada do comportamento de `flmane`/`db` em si (pull_policy, build, healthcheck, portas) — só reorganizar em qual arquivo cada serviço mora
- Não adicionar segredos/config real de produção (senhas, etc.) — fora de escopo, o compose já usa valores fixos de dev/demo hoje e isso não muda aqui

## Decisions

### Override automático (`docker-compose.override.yaml`) em vez de dois arquivos completos (`docker-compose.dev.yaml`/`docker-compose.prod.yaml`)
Com dois arquivos completos, `flmane`/`db` ficariam duplicados (mesma definição em ambos), com risco real de divergirem com o tempo (alguém atualiza a imagem/healthcheck num arquivo e esquece do outro). Com override, `docker-compose.yaml` fica a única fonte de verdade pra `flmane`/`db`; `docker-compose.override.yaml` só acrescenta `phpmyadmin`/`sonarqube` por cima, sem repetir nada.

Alternativa considerada: usar `profiles:` do Compose Spec, marcando `phpmyadmin`/`sonarqube` com `profiles: ["dev"]` num único arquivo, subindo dev com `docker compose --profile dev up -d`. Rejeitada — inverte o hábito atual (hoje `docker compose up -d` já sobe tudo, incluindo as ferramentas de dev, sem flag; com profiles isso pararia de acontecer por padrão, exigindo lembrar de passar `--profile dev` toda vez em desenvolvimento, que é o caso de uso mais comum). Override deixa o caminho comum (dev) sem fricção e exige o `-f` explícito só no caminho raro (produção).

### `docker-compose.yaml` (base) contém só `flmane`+`db`; volumes/network ficam onde os serviços que os usam ficam
`sonarqube_data` (volume usado só por `sonarqube`) vai pro `docker-compose.override.yaml`; `mariadb_data` fica no base (usado por `db`). Compose mescla `volumes:`/`networks:` de múltiplos arquivos automaticamente (união das chaves), então não há conflito.

### Comando de produção: `-f docker-compose.yaml` explícito
Alternativa considerada: renomear o arquivo base pra algo como `docker-compose.prod.yaml` e o de dev pra `docker-compose.yaml` (arquivo "principal" = dev, já que é o caso comum). Rejeitada — quebra a convenção do Compose Spec (`docker-compose.override.yaml` só é auto-carregado se o arquivo base se chamar exatamente `docker-compose.yaml`/`compose.yaml`); manter `docker-compose.yaml` como o nome convencional preserva o comportamento automático em dev sem exigir `-f` nenhum.

## Risks / Trade-offs

- [Risco] Alguém roda só `docker compose -f docker-compose.yaml up -d` pensando que está em dev e não vê `phpmyadmin`/`sonarqube` subirem → Mitigação: documentar claramente em `CLAUDE.md`/READMEs que `-f` explícito = produção (sem extras), comando padrão sem `-f` = dev (com extras)
- [Risco] `podman-compose` (script Python, não o plugin `podman compose`) pode ter suporte parcial/diferente pra auto-carregar `docker-compose.override.yaml` dependendo da versão → Mitigação: mesmo padrão de risco já aceito para `pull_policy` no change anterior (`migrate-mariadb-podman`); documentar versão mínima recomendada se necessário, não é regressão nova introduzida por este change
- [Trade-off] Produção precisa lembrar do `-f docker-compose.yaml` sempre — não é o "menor comando possível" pra produção → Aceito: dev é o caminho mais frequente (múltiplas vezes ao dia durante desenvolvimento), produção é raro (deploy); otimizar o caminho comum é a troca certa

## Migration Plan

1. Criar `docker-compose.override.yaml` com `phpmyadmin`, `sonarqube` e o volume `sonarqube_data`
2. Remover `phpmyadmin`, `sonarqube` e `sonarqube_data` de `docker-compose.yaml`, mantendo `flmane`+`db`+`mariadb_data`+`networks`
3. Validar: `docker compose config`/`podman compose config` sem `-f` mostra os 4 serviços mesclados; com `-f docker-compose.yaml` mostra só `flmane`+`db`
4. Subir dev (`docker compose up -d` / `utilitarios/build_container.sh`) e confirmar `phpmyadmin`/`sonarqube` funcionando como antes
5. Subir produção (`docker compose -f docker-compose.yaml up -d`) e confirmar que só `flmane`+`db` sobem
6. Atualizar `CLAUDE.md`, `README.md`, `README.pt-BR.md` com os dois comandos

Rollback: reverter os commits; `docker-compose.yaml` volta a conter os 4 serviços num arquivo só.

## Open Questions

(nenhuma)
