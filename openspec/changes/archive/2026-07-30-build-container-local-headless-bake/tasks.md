## 1. Diretório de imagens fixo (`ImagensHeadlessDisco`)

- [x] 1.1 `ImagensHeadlessDisco.iniciar()`: checar `System.getenv("FLMANE_IMAGENS_HEADLESS_DIR")`; se definida, usar esse caminho fixo (criar diretórios `circuitos`/`carros`/`capacetes` dentro dele se ausentes); se não definida, manter o comportamento atual (`criarDiretorioTemporarioSeguro`)
- [x] 1.2 Adicionar método para gravar e checar um arquivo marcador de conclusão (ex.: `.pronto`) no diretório base
- [x] 1.3 Testes unitários: diretório fixo é reaproveitado quando a env var está setada; diretório temporário novo é criado quando não está; marcador é gravado/detectado corretamente

## 2. Modo de bake e skip no boot normal (`MainLauncher`)

- [x] 2.1 Novo branch em `main`: argumento `--pre-gerar-imagens` chama um novo método (`assarImagensHeadless`) que roda a mesma sequência de ativação de modo disco + `preGerarImagensHeadless()`, grava o marcador e sai sem bindar porta
- [x] 2.2 `iniciarServidorHeadless(true)`: antes de chamar `preGerarImagensHeadless()`, checar se o marcador de conclusão já existe no diretório base; se sim, pular a pré-geração e logar que está reaproveitando imagens já assadas
- [x] 2.3 Testes: `--pre-gerar-imagens` gera imagens e grava marcador sem levantar servidor; boot `--headless` normal com marcador presente não chama a pré-geração; boot `--headless` normal sem marcador chama a pré-geração como antes

## 3. Dockerfile

- [x] 3.1 `flmane.dockerfile`: adicionar `ENV FLMANE_IMAGENS_HEADLESS_DIR=/app/imagens-headless` após o `COPY` do jar
- [x] 3.2 `flmane.dockerfile`: adicionar `RUN java -Djava.awt.headless=true -jar app.jar --pre-gerar-imagens` antes do `ENTRYPOINT`

## 4. Build script e Compose

- [x] 4.1 Renomear `utilitarios/build.sh` para `utilitarios/build_container.sh`
- [x] 4.2 `utilitarios/build_container.sh`: trocar tag da imagem de `docker.io/sowbreira/flmane:latest` para `sowbreira/flmane:latest`
- [x] 4.3 `docker-compose.yaml`: serviço `flmane` — trocar `image: docker.io/sowbreira/flmane:latest` para `image: sowbreira/flmane:latest`, adicionar `build: {context: ., dockerfile: flmane.dockerfile}` e `pull_policy: never`

## 5. Documentação

- [x] 5.1 `README.md`/`README.pt-BR.md`: remover `docker push sowbreira/flmane`; ajustar exemplos de build/tag sem `docker.io/`
- [x] 5.2 `CLAUDE.md`: atualizar referência ao script de build (`utilitarios/build_container.sh`) e mencionar que a pré-geração de imagens headless agora ocorre no build da imagem, não no boot do container

## 6. Validação end-to-end

- [x] 6.1 Rodar `mvn clean package -Pmariadb -DskipTests` seguido de `utilitarios/build_container.sh` (ou equivalente `podman build`/`docker build`) e confirmar que o log de build mostra a pré-geração rodando durante o `RUN`, não no start do container
- [x] 6.2 Subir o container (`compose up -d flmane`) e confirmar que o log de start NÃO mostra "PRE-GERANDO IMAGENS HEADLESS" (marcador já presente)
- [x] 6.3 Reiniciar o container (`compose restart flmane`) e confirmar que sobe rápido, servindo imagens de circuito/carro/capacete corretamente via HTTP
- [x] 6.4 Confirmar que `docker-compose.yaml`/`podman compose` nunca tenta um `pull` da imagem `flmane` (só builda local)
