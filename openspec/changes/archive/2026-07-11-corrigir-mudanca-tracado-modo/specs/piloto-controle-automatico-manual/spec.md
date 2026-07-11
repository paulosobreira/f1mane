## ADDED Requirements

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
No jogo solo (Swing), quando a partida foi iniciada com `automaticoManual` igual a `CONTROLE_AUTOMATICO`, o sistema SHALL permitir que decisões de piloto automático assumam o carro do jogador humano por padrão. Qualquer entrada do jogador que dispare `setManualTemporario()` SHALL suspender essas decisões pelo contador `manualTemporario` (renovado a cada nova entrada), voltando ao piloto automático quando o contador chegar a zero sem nova entrada.

#### Scenario: Piloto automático assume o carro sem entrada recente do jogador
- **WHEN** a partida solo está em modo automático e o jogador não deu nenhuma entrada que dispare `setManualTemporario()` nos últimos ciclos (contador `manualTemporario` em zero)
- **THEN** decisões de piloto automático (giro, ERS, DRS, ataque-defesa, escolha proativa de traçado) são tomadas normalmente pela IA em nome do carro do jogador

#### Scenario: Entrada do jogador suspende o piloto automático temporariamente
- **WHEN** o jogador dá uma entrada que dispara `setManualTemporario()` durante uma partida solo em modo automático
- **THEN** nenhuma decisão de piloto automático é tomada pela IA em nome do carro do jogador enquanto o contador `manualTemporario` for maior que zero, decrementando a cada ciclo

#### Scenario: Nova entrada do jogador renova a janela de suspensão
- **WHEN** o jogador dá uma nova entrada que dispara `setManualTemporario()` enquanto o contador `manualTemporario` de uma suspensão anterior ainda não chegou a zero
- **THEN** o contador é reiniciado para o valor cheio, estendendo a suspensão do piloto automático a partir desse ciclo

### Requirement: Escopo do piloto automático
A chave de piloto automático/manual (regida pelos três requisitos acima) SHALL governar exclusivamente: a seleção de giro do motor, o acionamento de ERS e DRS, as tentativas de ultrapassagem/defesa (`processaIAnovoIndex()`), e a escolha proativa de traçado para desviar de um piloto retardatário à frente (`processaMudarTracado()`). Essa chave SHALL NOT afetar mecânicas físicas sempre-ativas (snap de traçado no box, desvio de carro batido sob safety car, escapada por stress, derrapagem por pneu gasto em curva baixa) nem a mecânica de escape de fila indiana, que é exclusiva de bots — o comportamento dessas mecânicas está definido na spec `tracado-safe-lane-change`.

#### Scenario: Modo manual não impede snap de traçado no box
- **WHEN** o jogador humano está em modo manual (local ou online) e seu carro entra ou sai da faixa de boxes
- **THEN** o snap de traçado correspondente ao box acontece normalmente, sem depender da chave de piloto automático/manual

#### Scenario: Modo manual não impede desvio de carro batido sob safety car
- **WHEN** o jogador humano está em modo manual (local ou online), o safety car está na pista, e há um piloto batido no raio de desvio à frente na mesma faixa
- **THEN** o desvio automático de traçado acontece normalmente, sem depender da chave de piloto automático/manual
