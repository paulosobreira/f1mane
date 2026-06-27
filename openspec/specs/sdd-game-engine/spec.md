# sdd-game-engine

## Purpose
Documents `ControleJogoLocal` as the central hub of a solo match, the tick-loop mechanics of `ControleCiclo`, and all subsystems delegated by the game engine.

## Requirements

### Requirement: Hub central da engine documentado
O SDD SHALL descrever `ControleJogoLocal` como hub central da partida solo, com seus subsistemas agregados.

#### Scenario: Composição de ControleJogoLocal descrita
- **WHEN** o leitor consulta a seção da engine
- **THEN** o SDD lista os campos de subsistema: `controleCorrida`, `controleEstatisticas`, `controleCampeonato`, `gerenciadorVisual`, `pilotoSelecionado`, `pilotoJogador` e flags de feature (`trocaPneu`, `reabastecimento`, `ers`, `drs`, `safetyCar`)

### Requirement: Tick loop documentado
O SDD SHALL descrever o loop de simulação de `ControleCiclo` com sua sequência de operações e temporização.

#### Scenario: Sequência do tick loop descrita
- **WHEN** o leitor consulta o tick loop
- **THEN** o SDD descreve a sequência: (1) sleep 2000ms + 5 piscadas de 1000ms antes da largada; (2) loop principal: `processaVoltaSafetyCar`, `decrementaParadoBox`, `processarCiclo`, `calculaVelocidadeExibir`, `processarPilotoBox`, `atualizaClassificacao`, `verificaFinalCorrida`; (3) `Thread.sleep(tempoCicloCircuito())` — taxa variável por circuito (~50ms)

### Requirement: Subsistemas de corrida documentados
O SDD SHALL descrever os subsistemas delegados por `ControleJogoLocal`.

#### Scenario: ControleCorrida descrito
- **WHEN** o leitor consulta a física da corrida
- **THEN** o SDD descreve que `ControleCorrida` gerencia voltas, consumo de combustível, acidentes, ultrapassagens e atualiza a classificação

#### Scenario: ControleBox descrito
- **WHEN** o leitor consulta pit stops
- **THEN** o SDD descreve que `ControleBox` gerencia os nós de entrada/saída/parada do box, a ocupação de vagas por equipe (`boxEquipes`, `boxEquipesOcupado`), e o flag `boxRapido` (50% chance)

#### Scenario: ControleSafetyCar descrito
- **WHEN** o leitor consulta o safety car
- **THEN** o SDD descreve que `ControleSafetyCar` ativa/desativa o `SafetyCar` via `safetyCarNaPista(Piloto)` e usa `ThreadRecolhimentoCarro` para recolher carros acidentados

#### Scenario: ControleClima descrito
- **WHEN** o leitor consulta mudanças climáticas
- **THEN** o SDD descreve que `ControleClima` gera o clima inicial e é executado em `ThreadMudancaClima`; os tipos de clima incluem `SOL`, `CHUVA` e variantes

#### Scenario: GerenciadorVisual descrito
- **WHEN** o leitor consulta o gerenciamento visual
- **THEN** o SDD descreve que `GerenciadorVisual` agrega `PainelCircuito`, combos de seleção de circuito/temporada/piloto, sliders de configuração e checkboxes de feature, e inicializa a interface gráfica via `iniciarInterfaceGraficaJogo()`
