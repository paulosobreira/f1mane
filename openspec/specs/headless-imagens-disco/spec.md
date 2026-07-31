# headless-imagens-disco

## Purpose
Pipeline de geração e disponibilização de imagens (circuitos, carros, capacetes) em modo headless — pré-geração para disco na subida do servidor, leitura direta de disco pelos endpoints REST, sem retenção dos `BufferedImage`s em caches estáticos em memória.

## Requirements

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

### Requirement: Endpoints de imagem servem bytes diretamente do disco em modo headless
Em modo headless, os endpoints de imagem de `LetsRace` (`circuitoJpg`, `circuitoBg`, `circuitoMini`, `carroCimaTemporadaCarro`, `carroCimaSemAreofolioTemporadaCarro`, `capaceteTemporadaPiloto`, `carroLadoTemporadaCarro`) SHALL ler os bytes da resposta diretamente do arquivo pré-gerado em disco correspondente, sem gerar ou consultar um `BufferedImage` em cache estático de memória.

#### Scenario: Requisição de imagem de circuito pré-gerado
- **WHEN** o cliente HTML5 requisita a imagem de fundo de um circuito que foi pré-gerado no boot
- **THEN** o servidor responde com os bytes lidos do arquivo em disco, sem executar novamente a lógica de desenho do circuito

#### Scenario: Requisições repetidas não regeneram a imagem
- **WHEN** o mesmo endpoint de imagem de circuito/carro/capacete é requisitado múltiplas vezes em modo headless
- **THEN** cada requisição lê o mesmo arquivo em disco já existente, sem gerar um novo `BufferedImage` em memória a cada chamada

### Requirement: Assets não pré-gerados são gerados sob demanda e persistidos em disco
Quando um circuito, temporada ou carro/piloto passa a existir (é ativado ou configurado) após a subida do servidor headless, sem haver reinício do processo, o endpoint de imagem correspondente SHALL gerar a imagem sob demanda na primeira requisição, gravá-la no mesmo diretório de imagens pré-geradas, e servi-la a partir do arquivo — sem reter o `BufferedImage` resultante em um cache estático de memória.

#### Scenario: Circuito ativado após o boot é servido sob demanda
- **WHEN** um circuito é marcado `ativo=true` depois que o servidor headless já subiu, e sua imagem é requisitada pela primeira vez
- **THEN** o servidor gera a imagem, grava o arquivo no diretório de imagens em disco e responde com esses bytes; requisições seguintes leem o arquivo já gravado

#### Scenario: Requisições concorrentes ao mesmo asset ausente não duplicam geração nem cacheiam a imagem em memória
- **WHEN** duas requisições simultâneas pedem a imagem de um mesmo asset ainda não pré-gerado
- **THEN** a imagem é gerada uma única vez e gravada em disco; nenhuma das requisições mantém o `BufferedImage` resultante em um mapa estático após responder

### Requirement: Caches estáticos de imagem em memória ficam inativos em modo headless
Em modo headless, os caches estáticos `bufferImages`, `bufferCarros`, `cacheModeloV2` e `cacheMonocromatico` de `CarregadorRecursos`, e o cache estático de `SpriteSheet`, SHALL permanecer sem os `BufferedImage`s gerados para os endpoints de imagem — a fonte de verdade servida ao cliente é sempre o arquivo em disco.

#### Scenario: Cache estático não cresce ao servir imagens em modo headless
- **WHEN** o servidor headless serve repetidamente imagens de circuitos, carros e capacetes ao longo do tempo
- **THEN** os caches estáticos de `CarregadorRecursos` e `SpriteSheet` não acumulam os `BufferedImage`s desses assets

### Requirement: Objetos de desenho de um circuito são liberados da memória após a geração do arquivo em disco
Após a imagem de um circuito ser gravada em disco (na pré-geração do boot ou na geração sob demanda), o sistema SHALL liberar da memória os objetos de `Circuito`/`No` usados exclusivamente para desenho (elementos decorativos de cenário/`ObjetoLivre` não consultados pela mecânica de jogo), preservando os campos efetivamente lidos pela mecânica de corrida (`ControleCorrida`, `ControleSafetyCar`, `ControleBox`, `ControleAutomacao`) enquanto o circuito estiver em uso por uma corrida ativa.

#### Scenario: Objetos só de desenho são liberados após gerar a imagem
- **WHEN** a imagem de fundo de um circuito termina de ser gravada em disco
- **THEN** as referências aos objetos de `Circuito`/`No` usados apenas para desenho desse circuito deixam de ser mantidas em memória

#### Scenario: Mecânica de corrida não é afetada pela liberação
- **WHEN** uma corrida está em andamento num circuito cuja imagem já foi gerada e cujos objetos de desenho já foram liberados
- **THEN** `ControleCorrida` e os demais componentes de mecânica de jogo continuam funcionando normalmente, sem `NullPointerException` nem alteração de comportamento de física/colisão/ultrapassagem

### Requirement: JVM do servidor headless roda com `-Djava.awt.headless=true`
O comando usado por `MainLauncher.iniciarProcessoServidor` para iniciar o processo filho `--headless` SHALL incluir a flag de JVM `-Djava.awt.headless=true`.

#### Scenario: Processo filho headless recebe a flag
- **WHEN** o launcher em modo GUI inicia o processo filho do servidor
- **THEN** o comando montado pelo `ProcessBuilder` inclui `-Djava.awt.headless=true` entre os argumentos da JVM

#### Scenario: Duplicação de imagem compatível com tela é evitada
- **WHEN** o servidor headless carrega uma imagem estática via `CarregadorRecursos`/`ImageUtil`
- **THEN** `GraphicsEnvironment.isHeadless()` retorna `true` e `ImageUtil.toCompatibleImage` não cria uma cópia adicional da imagem

### Requirement: Estruturas auxiliares da pré-geração liberadas antes de servir requisições
Ao concluir a pré-geração de imagens em modo headless (ou ao pular a pré-geração por marcador já presente), `MainLauncher` SHALL liberar as estruturas carregadas apenas para essa etapa — listas de pilotos e carros por temporada, circuitos desserializados e quaisquer caches auxiliares populados durante a geração — antes de bindar a porta, de modo que o heap em regime não carregue o pico da pré-geração.

#### Scenario: Heap em regime após pré-geração completa
- **WHEN** o servidor headless conclui a pré-geração de todas as imagens e passa a aceitar requisições
- **THEN** as estruturas usadas só pela pré-geração já não são alcançáveis, e o heap usado após coleta é comparável ao de um boot que pulou a pré-geração por marcador

#### Scenario: Boot com imagens já assadas
- **WHEN** o servidor sobe sobre um diretório com marcador de pré-geração concluída
- **THEN** nenhuma lista de pilotos/carros nem circuito é desserializado por conta da pré-geração, e a subida vai direto ao bind da porta

#### Scenario: Assets continuam disponíveis após a liberação
- **WHEN** um cliente requisita, após a subida, a imagem de fundo de um circuito, um carro ou um capacete
- **THEN** os bytes são servidos a partir do arquivo em disco, sem depender de nenhuma estrutura liberada
