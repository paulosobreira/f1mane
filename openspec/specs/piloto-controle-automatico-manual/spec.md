# Spec: piloto-controle-automatico-manual

## Purpose

Regras que governam quando o piloto automático (IA) pode assumir decisões pelo carro do jogador humano, distinguindo os modos automático e manual em partidas solo e online, e delimitando quais mecânicas de traçado ficam sujeitas a essa chave.

## Requirements

### Requirement: Piloto automático nunca assume o carro do jogador humano em jogo online
Em qualquer partida online (`controleJogo instanceof JogoServidor` no servidor, ou o equivalente cliente `JogoCliente`), o sistema SHALL tratar o jogador humano como sempre manual para efeito de piloto automático, independentemente do valor de `automaticoManual` guardado em `DadosCriarJogo`/`Campeonato.nivel` para aquela partida. O valor escolhido na criação do jogo continua sendo apenas exibido nas telas do jogo (rótulo), sem afetar esse comportamento.

#### Scenario: Jogador humano online com "Automático" selecionado na criação continua manual
- **WHEN** um jogador cria ou entra em uma partida online com `automaticoManual` igual a `CONTROLE_AUTOMATICO`
- **THEN** nenhuma decisão de piloto automático (giro, ERS, DRS, ataque-defesa, escolha proativa de traçado) é tomada pela IA em nome do carro desse jogador

#### Scenario: Jogador humano online com "Manual" selecionado também continua manual
- **WHEN** um jogador cria ou entra em uma partida online com `automaticoManual` igual a `CONTROLE_MANUAL`
- **THEN** o comportamento é idêntico ao cenário anterior — nenhuma decisão de piloto automático é tomada pela IA em nome do carro desse jogador

### Requirement: Piloto automático desligado no jogo solo em modo manual
No jogo solo (Swing), quando a partida foi iniciada com `automaticoManual` igual a `CONTROLE_MANUAL`, o sistema SHALL tratar o carro do jogador humano exatamente como no jogo online: nenhuma decisão de piloto automático assume o carro.

#### Scenario: Solo em modo manual nunca aciona piloto automático
- **WHEN** uma partida solo é iniciada com `automaticoManual` igual a `CONTROLE_MANUAL` e o jogador está pilotando
- **THEN** nenhuma decisão de piloto automático (giro, ERS, DRS, ataque-defesa, escolha proativa de traçado) é tomada pela IA em nome do carro do jogador, em nenhum ciclo da corrida

### Requirement: Piloto automático ativo por padrão no jogo solo em modo automático, suspenso temporariamente por entrada do jogador
No jogo solo (Swing), quando a partida foi iniciada com `automaticoManual` igual a `CONTROLE_AUTOMATICO`, o sistema SHALL permitir que decisões de piloto automático (tomadas por `ControleAutomacao`) assumam o carro do jogador humano por padrão. Qualquer entrada do jogador que dispare `ControleAutomacao.suspenderTemporariamente(Piloto)` SHALL suspender essas decisões pelo contador `manualTemporario` (renovado a cada nova entrada, guardado por `ControleAutomacao`), voltando ao piloto automático quando o contador chegar a zero sem nova entrada.

#### Scenario: Piloto automático assume o carro sem entrada recente do jogador
- **WHEN** a partida solo está em modo automático e o jogador não deu nenhuma entrada que dispare `ControleAutomacao.suspenderTemporariamente(Piloto)` nos últimos ciclos (contador `manualTemporario` em zero)
- **THEN** decisões de piloto automático (giro, ERS, DRS, ataque-defesa, escolha proativa de traçado) são tomadas normalmente pela IA em nome do carro do jogador

#### Scenario: Entrada do jogador suspende o piloto automático temporariamente
- **WHEN** o jogador dá uma entrada que dispara `ControleAutomacao.suspenderTemporariamente(Piloto)` durante uma partida solo em modo automático
- **THEN** nenhuma decisão de piloto automático é tomada pela IA em nome do carro do jogador enquanto o contador `manualTemporario` for maior que zero, decrementando a cada ciclo

#### Scenario: Nova entrada do jogador renova a janela de suspensão
- **WHEN** o jogador dá uma nova entrada que dispara `ControleAutomacao.suspenderTemporariamente(Piloto)` enquanto o contador `manualTemporario` de uma suspensão anterior ainda não chegou a zero
- **THEN** o contador é reiniciado para o valor cheio, estendendo a suspensão do piloto automático a partir desse ciclo

### Requirement: Escopo do piloto automático
A chave de piloto automático/manual (regida pelos requisitos desta spec) SHALL governar exclusivamente as decisões tomadas por `ControleAutomacao`: a seleção de giro do motor, o acionamento de ERS e DRS, as tentativas de ultrapassagem/defesa, e a escolha proativa de traçado para desviar de um piloto retardatário à frente. `ControleAutomacao` SHALL executar essas decisões chamando os mesmos métodos públicos de comando de `Piloto` que o jogador humano aciona (`setModoPilotagem`, `setAtivarDRS`, `setAtivarErs`, `setBox`, `mudarTracado`), sem via de comando paralela. `Piloto` SHALL acionar o tick de `ControleAutomacao` através de `InterfaceJogo.processarAutomacao(Piloto)`, nunca instanciando ou chamando `ControleAutomacao` diretamente. Essa chave SHALL NOT afetar mecânicas físicas sempre-ativas (snap de traçado no box, desvio de carro batido sob safety car, escapada por stress, derrapagem por pneu gasto em curva baixa), a mecânica de escape de fila indiana (exclusiva de bots, decidida por `ControleAutomacao`, mas sem chave automático/manual — bot nunca tem "manual"), nem as mudanças de `modoPilotagem`/`giro` acionadas por bandeirada, colisão contra o carro da frente, ou por servir de retardatário sendo ultrapassado (`desviaPilotoNaFrente`) — essas quatro últimas SHALL continuar em `Piloto`, forçando `modoPilotagem`/`giro` de qualquer piloto, humano manual incluso, exatamente como fariam para um piloto de IA. O comportamento das mecânicas de traçado está definido na spec `tracado-safe-lane-change`.

#### Scenario: Modo manual não impede snap de traçado no box
- **WHEN** o jogador humano está em modo manual (local ou online) e seu carro entra ou sai da faixa de boxes
- **THEN** o snap de traçado correspondente ao box acontece normalmente, sem depender da chave de piloto automático/manual

#### Scenario: Modo manual não impede desvio de carro batido sob safety car
- **WHEN** o jogador humano está em modo manual (local ou online), o safety car está na pista, e há um piloto batido no raio de desvio à frente na mesma faixa
- **THEN** o desvio automático de traçado acontece normalmente, sem depender da chave de piloto automático/manual

#### Scenario: Modo manual não impede a escapada de pista forçar LENTO
- **WHEN** o jogador humano está em modo manual e entra no traçado de fuga (4/5) por ter sido marcado pela escapada ancorada
- **THEN** `modoPilotagem` é forçado para `LENTO` e `giro` para o mínimo, sem depender da chave de piloto automático/manual, restaurando o modo/giro anteriores ao sair da escapada

#### Scenario: Modo manual não impede dano por colisão contra o carro da frente forçar LENTO
- **WHEN** o jogador humano está em modo manual, com `modoPilotagem` efetivamente `AGRESSIVO` (armazenado `AGRESSIVO` e `stress <= 95`, ver spec `piloto-modo-pilotagem-agressividade`), e sofre dano de aerofólio por `ControleCorrida.verificaAcidenteJogadorHumano`
- **THEN** `modoPilotagem` é forçado para `LENTO`, sem depender da chave de piloto automático/manual

#### Scenario: Colisão contra o carro da frente não se aplica quando AGRESSIVO já não é efetivo
- **WHEN** o jogador humano está em modo manual, com `modoPilotagem` armazenado `AGRESSIVO` mas `stress > 95` (efetivamente `NORMAL`)
- **THEN** `ControleCorrida.verificaAcidenteJogadorHumano` não aplica essa penalidade — não por causa da chave de piloto automático/manual, mas porque a pré-condição de risco (agressividade efetiva) já não se aplica, igual valeria pra um piloto de IA no mesmo estado

#### Scenario: Modo manual não impede ser forçado pra LENTO ao ser ultrapassado como retardatário
- **WHEN** o jogador humano está em modo manual e é ultrapassado por outro piloto que aciona a mecânica de desvio de retardatário (`desviaPilotoNaFrente`)
- **THEN** `modoPilotagem`/`giro` do jogador humano são forçados para `LENTO`/mínimo, sem depender da chave de piloto automático/manual — igual a qualquer piloto de IA nessa situação

#### Scenario: Modo manual não impede ser forçado pra LENTO pela bandeirada
- **WHEN** o jogador humano está em modo manual e recebe a bandeirada quadriculada
- **THEN** `modoPilotagem`/`giro` do jogador humano são forçados para `LENTO`/mínimo, sem depender da chave de piloto automático/manual

### Requirement: Cooldown de meio segundo por tipo de ação da automação em jogo online
Em partidas online (`controleJogo instanceof JogoServidor`), `ControleAutomacao` SHALL limitar cada tipo de ação que decide executar em nome de um piloto de IA (modo de pilotagem, giro do motor, ERS, DRS, box, traçado) a no máximo uma execução a cada 500 milissegundos, por piloto, por tipo de ação, independentemente dos outros tipos. Uma tentativa de executar o mesmo tipo de ação antes de decorridos 500ms desde a última execução bem-sucedida daquele tipo para aquele piloto SHALL ser descartada silenciosamente nesse ciclo — sem erro, sem log de aviso, sem consumir a decisão de outro tipo de ação no mesmo ciclo. O timestamp de referência SHALL ser atualizado somente quando a ação é de fato executada, nunca quando é descartada pelo cooldown. Esse limite SHALL NOT se aplicar a partidas solo, nem a nenhuma ação disparada por um jogador humano (online ou local).

#### Scenario: Segunda mudança de modo de pilotagem antes de 500ms é ignorada
- **WHEN** `ControleAutomacao` executa uma mudança de modo de pilotagem para um piloto de IA numa partida online, e decide executar outra mudança de modo de pilotagem para o mesmo piloto menos de 500ms depois
- **THEN** a segunda mudança é descartada — `modoPilotagem` permanece no valor definido pela primeira execução

#### Scenario: Mudança de modo após 500ms é aplicada normalmente
- **WHEN** `ControleAutomacao` executa uma mudança de modo de pilotagem para um piloto de IA numa partida online, e decide executar outra mudança de modo de pilotagem para o mesmo piloto 500ms ou mais depois
- **THEN** a segunda mudança é aplicada normalmente, e o timestamp de referência é atualizado para o momento dessa execução

#### Scenario: Cooldowns de tipos de ação diferentes são independentes
- **WHEN** `ControleAutomacao` executa uma mudança de modo de pilotagem para um piloto de IA numa partida online, e no mesmo ciclo (ou logo em seguida) decide também ativar DRS para o mesmo piloto
- **THEN** a ativação de DRS não é bloqueada pelo cooldown da mudança de modo — cada tipo de ação tem seu próprio cronômetro

#### Scenario: Tentativa descartada pelo cooldown não renova o cronômetro
- **WHEN** uma tentativa de ação de um tipo é descartada por estar dentro do cooldown de 500ms
- **THEN** o timestamp de referência daquele tipo de ação não muda — o cooldown continua contando a partir da última execução bem-sucedida, não da tentativa descartada

#### Scenario: Cooldown não se aplica em partida solo
- **WHEN** `ControleAutomacao` decide e executa ações repetidas do mesmo tipo para um piloto de IA numa partida solo, em ciclos consecutivos
- **THEN** nenhuma delas é descartada por cooldown — o limite de 500ms por tipo de ação é exclusivo de partidas online

#### Scenario: Cooldown não se aplica a ações de jogador humano
- **WHEN** um jogador humano (local ou online) dispara múltiplas ações do mesmo tipo em menos de 500ms através da UI/API
- **THEN** todas são processadas normalmente pelos caminhos de comando existentes — o cooldown desta spec rege exclusivamente ações decididas por `ControleAutomacao` em nome de um piloto de IA
