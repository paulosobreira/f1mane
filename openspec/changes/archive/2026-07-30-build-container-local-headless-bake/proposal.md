## Why

Hoje `utilitarios/build.sh` builda a imagem `flmane` com tag `docker.io/sowbreira/flmane:latest`, sugerindo um fluxo de registry (push/pull) que na prática não é necessário para deploy local via Docker/Podman Compose — o `docker-compose.yaml` só precisa da imagem já construída localmente. Além disso, o modo `--headless` do jogo pré-gera em disco (num diretório temporário, recriado do zero) as imagens de circuito/carro/capacete toda vez que o container sobe — isso já foi endereçado para não reter `BufferedImage` em memória (capability `headless-imagens-disco`), mas o custo de tempo de boot (minutos, dependendo do número de temporadas/circuitos) se repete a cada `restart`/redeploy do container, quando na verdade os assets gerados não mudam entre um restart e outro da mesma imagem.

## What Changes

- `utilitarios/build.sh` renomeado para `utilitarios/build_container.sh`
- Imagem do serviço `flmane` passa a ser sempre construída e usada localmente: tag sem domínio de registry (`sowbreira/flmane:latest`, sem prefixo `docker.io/`), `docker-compose.yaml` ganha `build:` apontando pro `flmane.dockerfile` e `pull_policy: never`, garantindo que nunca tenta puxar do Docker Hub
- README.md/README.pt-BR.md: remover instrução de `docker push sowbreira/flmane` (fluxo local não publica em registry)
- **BREAKING**: `--headless` deixa de sempre pré-gerar as imagens em disco num diretório temporário efêmero; passa a existir um modo de pré-geração "assar" (`--pre-gerar-imagens`) que roda uma vez, grava num diretório fixo e sai; o `flmane.dockerfile` roda esse modo durante o `build` da imagem (`RUN`), então as imagens já vêm prontas na imagem final
- Na subida normal (`--headless`), se o diretório de imagens já tiver uma pré-geração completa (marcador de conclusão), a pré-geração é pulada — restart de container não paga o custo de boot de novo
- Fallback preservado: se a variável de ambiente do diretório fixo não estiver setada (ex.: rodando o jar direto fora do Dockerfile), o comportamento atual (diretório temporário, pré-gera sempre) continua valendo

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `headless-imagens-disco`: pré-geração de imagens passa a poder ocorrer no `docker build` (modo "assar", diretório fixo) em vez de sempre no boot do container; boot normal pula a pré-geração quando já encontra um diretório pré-gerado completo

## Impact

- `utilitarios/build.sh` → `utilitarios/build_container.sh` (rename + ajuste de tag de imagem)
- `docker-compose.yaml`: serviço `flmane` (`image`, `build`, `pull_policy`)
- `flmane.dockerfile`: novo `RUN` de pré-geração + `ENV` do diretório fixo de imagens
- `src/main/java/br/flmane/MainLauncher.java`: novo modo de linha de comando `--pre-gerar-imagens` (gera e sai), lógica de skip na subida normal via marcador de conclusão
- `src/main/java/br/flmane/recursos/ImagensHeadlessDisco.java`: diretório fixo via variável de ambiente (em vez de sempre temporário), suporte a marcador de conclusão
- `README.md`, `README.pt-BR.md`, `CLAUDE.md`: comandos/caminhos atualizados
- `openspec/specs/headless-imagens-disco/spec.md`: requisito de pré-geração modificado
