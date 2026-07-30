## MODIFIED Requirements

### Requirement: Pré-geração de imagens em disco na subida do servidor headless
Ao iniciar em modo `--headless`, `MainLauncher` SHALL gerar em disco, antes de aceitar requisições de imagem, a imagem de fundo e a miniatura de todo circuito com `ativo=true`, e a imagem de carro-lado, carro-cima (com e sem aerofólio) e capacete de cada carro/piloto de cada temporada configurada, usando a lógica de geração já existente (`DesenhoProceduralCircuito.geraImagem`/`PainelCircuito.desenhaCircuito`, `CarregadorRecursos.pintarModeloV2`/`pintarMonocromatico`/`desenhaCapacete`, ou `SpriteSheet` quando aplicável) — **exceto** quando o diretório de imagens já contém um marcador de pré-geração concluída (ver Requisito "Diretório de imagens fixo e reaproveitável entre restarts"), caso em que a pré-geração é pulada.

#### Scenario: Circuitos ativos pré-gerados no boot
- **WHEN** o servidor é iniciado com `--headless` (sem marcador de pré-geração concluída) e existem N circuitos com `ativo=true` em `circuitos.properties`
- **THEN** ao final da subida, o diretório de imagens contém o arquivo de fundo e de miniatura de cada um dos N circuitos

#### Scenario: Circuito inativo não é pré-gerado
- **WHEN** o servidor é iniciado com `--headless` e um circuito tem `ativo=false`
- **THEN** nenhum arquivo de imagem é gerado para esse circuito durante a subida

#### Scenario: Carros e capacetes de todas as temporadas pré-gerados no boot
- **WHEN** o servidor é iniciado com `--headless` (sem marcador de pré-geração concluída) e existem temporadas configuradas em `properties/temporadas.properties`, cada uma com seus carros e pilotos
- **THEN** ao final da subida, o diretório de imagens contém a imagem de carro-lado, carro-cima (com e sem aerofólio) de cada carro e a imagem de capacete de cada piloto, para cada temporada

#### Scenario: Falha ao gerar um asset não aborta a subida do servidor
- **WHEN** a geração de imagem de um circuito ou carro/piloto específico falha durante a pré-geração (ex.: XML malformado)
- **THEN** o servidor registra a falha em log, pula esse asset e continua a subida normalmente, servindo os demais assets pré-gerados

#### Scenario: Processo filho do modo GUI não paga o custo de pré-geração
- **WHEN** `MainLauncher` sobe em modo GUI (sem `--headless`) e inicia seu processo filho interno (que recebe `--headless` para isolar o estado estático de `Lang`)
- **THEN** esse processo filho é sinalizado internamente (variável de ambiente própria, nunca exposta como flag de linha de comando) para não pré-gerar imagens em disco nem desligar os caches estáticos de `BufferedImage` em memória — o comportamento de pré-geração em disco é exclusivo do deploy real (`flmane.dockerfile`, `java -jar app.jar --headless` direto)

### Requirement: Diretório de imagens fixo e reaproveitável entre restarts
`ImagensHeadlessDisco.iniciar()` SHALL usar o diretório apontado pela variável de ambiente `FLMANE_IMAGENS_HEADLESS_DIR`, quando definida, em vez de criar um diretório temporário novo a cada boot; quando não definida, o comportamento SHALL continuar sendo a criação de um diretório temporário novo por processo (`~/.flmane/tmp`), preservando o fluxo de desenvolvimento local/GUI. Ao final de uma pré-geração completa (bake ou boot normal), o sistema SHALL gravar um arquivo marcador de conclusão nesse diretório; na subida seguinte, se o marcador já existir, a pré-geração SHALL ser pulada.

#### Scenario: Variável de ambiente setada aponta pro diretório fixo
- **WHEN** `FLMANE_IMAGENS_HEADLESS_DIR` está definida ao chamar `ImagensHeadlessDisco.iniciar()`
- **THEN** o diretório base usado é exatamente o caminho apontado pela variável, criado se ainda não existir, sem gerar um caminho temporário aleatório

#### Scenario: Variável de ambiente ausente preserva o comportamento anterior
- **WHEN** `FLMANE_IMAGENS_HEADLESS_DIR` não está definida ao chamar `ImagensHeadlessDisco.iniciar()`
- **THEN** um novo diretório temporário seguro é criado sob `~/.flmane/tmp`, como antes desta mudança

#### Scenario: Restart do container reaproveita imagens já pré-geradas
- **WHEN** o servidor `--headless` sobe e o diretório de imagens (apontado pela variável de ambiente) já contém o marcador de pré-geração concluída de uma execução anterior
- **THEN** a pré-geração inteira é pulada, o boot segue direto para o bind da porta, e os endpoints de imagem servem os arquivos já existentes normalmente

### Requirement: Modo de pré-geração isolado para bake em tempo de build
`MainLauncher` SHALL aceitar o argumento de linha de comando `--pre-gerar-imagens`, que executa a mesma pré-geração de imagens do modo `--headless`, grava o marcador de conclusão ao final, e encerra o processo sem jamais abrir a porta HTTP — permitindo que o `flmane.dockerfile` rode esse modo dentro de um `RUN` durante o `docker build`, embutindo as imagens já geradas na imagem final.

#### Scenario: `--pre-gerar-imagens` gera e sai sem bindar porta
- **WHEN** `MainLauncher` é executado com `--pre-gerar-imagens` e `FLMANE_IMAGENS_HEADLESS_DIR` apontando pro diretório de destino
- **THEN** o processo gera todas as imagens de circuitos/carros/capacetes ativos, grava o marcador de conclusão nesse diretório e termina (`exit 0`) sem iniciar o servidor Netty

#### Scenario: Imagem Docker final já nasce com as imagens pré-geradas
- **WHEN** `flmane.dockerfile` builda a imagem, executando `--pre-gerar-imagens` num `RUN` antes do `ENTRYPOINT`
- **THEN** um container iniciado a partir dessa imagem com `--headless` encontra o marcador de conclusão já presente e não executa a pré-geração no boot
