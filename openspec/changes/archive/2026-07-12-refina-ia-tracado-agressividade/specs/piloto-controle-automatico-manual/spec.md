## MODIFIED Requirements

### Requirement: Escopo do piloto automático
A chave de piloto automático/manual (regida pelos três requisitos acima) SHALL governar exclusivamente: a seleção de giro do motor, o acionamento de ERS e DRS, as tentativas de ultrapassagem/defesa (`processaIAnovoIndex()`), e a escolha proativa de traçado para desviar de um piloto retardatário à frente (`processaMudarTracado()`). Essa chave SHALL NOT afetar mecânicas físicas sempre-ativas (snap de traçado no box, desvio de carro batido sob safety car, escapada por stress, derrapagem por pneu gasto em curva baixa), a mecânica de escape de fila indiana (exclusiva de bots), nem as mudanças de `modoPilotagem`/`giro` acionadas por bandeirada, colisão contra o carro da frente, ou por servir de retardatário sendo ultrapassado (`desviaPilotoNaFrente`) — essas quatro últimas SHALL continuar forçando `modoPilotagem`/`giro` de qualquer piloto, humano manual incluso, exatamente como fariam para um piloto de IA. O comportamento das mecânicas de traçado está definido na spec `tracado-safe-lane-change`.

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
