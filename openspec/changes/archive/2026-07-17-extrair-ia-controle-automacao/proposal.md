## Why

Toda a lógica de piloto automático (decisão de giro/ERS/DRS, ataque/defesa, ida ao box, traçado proativo) hoje vive misturada com o resto do estado físico/gameplay dentro de `Piloto.java` (>4600 linhas), tornando a classe difícil de navegar e acoplando decisão de IA a implementação de física. Além disso, em partidas online, um piloto de IA pode hoje trocar de modo de pilotagem (ou qualquer outra ação) em todo ciclo sem nenhum limite de frequência, diferente de um jogador humano que é naturalmente limitado pela velocidade de clique/toque — isso pode gerar comportamento de bot artificialmente "nervoso" comparado a um humano.

## What Changes

- Nova classe `br.f1mane.controles.ControleAutomacao`, instanciada em `ControleCorrida` (mesmo padrão de `ControleBox`/`ControleSafetyCar`/`ControleClima`/`ControleQualificacao`), que passa a concentrar toda a lógica de decisão de piloto automático hoje espalhada em `Piloto.java`: `processaIAnovoIndex()`, `processaIaIrBox()`, `iaTentaUsarErs()`, `iaTentaUsarDRS()`, `tentarPassaPilotoDaFrente()`, `tentarEscaparPilotoAtras()`, `modoIADefesaAtaque()`, `tentarEscaparFilaIndiana()`, `autopilotAtivo()`/`autopilotDesligado()`, e o bookkeeping de `manualTemporario` (suspensão temporária por entrada do jogador).
- `ControleAutomacao` decide, mas não executa a ação diretamente por campo — ela chama os mesmos métodos públicos de comando que um jogador humano aciona (`setModoPilotagem`, `setAtivarDRS`, `setAtivarErs`, `setBox`, `mudarTracado`), sem criar uma nova via de comando paralela.
- `Piloto` passa a acionar o tick de automação através da interface de jogo (`InterfaceJogo.processarAutomacao(Piloto)`), com `ControleJogoLocal` delegando para o `ControleAutomacao` da corrida — substitui as chamadas diretas a `processaIAnovoIndex()`/`processaIaIrBox()` dentro do pipeline de `processaNovoIndex()`.
- `ControleAutomacao` continua tendo acesso de leitura a `InterfaceJogo` (circuito, clima, safety car, outros pilotos etc.) — a mudança não isola a IA dos dados de jogo, só move onde a decisão é tomada.
- **BREAKING (comportamento novo)**: em partidas online (`controleJogo instanceof JogoServidor`), cada tipo de ação decidida pela automação (modo de pilotagem, giro, ERS, DRS, box, traçado) passa a ter um cooldown independente de meio segundo (500ms) por piloto — uma tentativa de repetir o mesmo tipo de ação antes de 500ms desde a última execução bem-sucedida é ignorada silenciosamente. Esse cooldown vale só para pilotos controlados por IA (bots), não para jogadores humanos. (Valor original de 1s ajustado pra 500ms a pedido do usuário após a implementação.)
- Consequência de limpeza: 8 campos de `Piloto` que hoje só existem como variáveis de trabalho de `processaIAnovoIndex()`/`modoIADefesaAtaque()` (`temMotor`, `temCombustivel`, `temPneu`, `porcentagemDesgastePneus`, `porcentagemMotor`, `porcentagemCombustivel`, `superAquecido`, `porcentagemCorridaRestante`) deixam de existir em `Piloto` — viram estado local do tick de `ControleAutomacao`.
- Novos getters públicos em `Piloto` para os poucos campos hoje privados que a decisão de IA lê e que não tinham acesso público (`calculaDiferencaParaProximo`, `calculaDiferencaParaAnterior`, `calculaDiffParaProximoRetardatario`, `calculaDiffParaProximoRetardatarioMesmoTracado`, `carroPilotoDaFrenteRetardatario`, `maxGanhoBaixa`, `maxGanhoAlta`) — necessário porque `ControleAutomacao` vive em outro pacote (`br.f1mane.controles`).

## Capabilities

### New Capabilities

(nenhuma — é relocação de comportamento existente mais uma regra nova de limite de frequência, que cabe na capability já existente abaixo)

### Modified Capabilities

- `piloto-controle-automatico-manual`: a lógica de decisão passa a viver em `ControleAutomacao` em vez de métodos de `Piloto` (referências de método atualizadas nos requisitos existentes, sem mudança de comportamento); adiciona um requisito novo — cooldown de meio segundo (500ms) por tipo de ação para pilotos de IA em partidas online.
- `piloto-modo-pilotagem-agressividade`: o requisito "Decisão automática da IA recua de AGRESSIVO acima de 95% de stress" passa a referenciar `ControleAutomacao` em vez de `Piloto.modoIADefesaAtaque()` — relocação de método, comportamento inalterado.

## Impact

- **Código afetado**: `src/main/java/br/f1mane/entidades/Piloto.java` (remoção de ~10 métodos e 8 campos, adição de ~7 getters públicos, troca das chamadas diretas de IA por uma chamada a `controleJogo.processarAutomacao(this)`), novo arquivo `src/main/java/br/f1mane/controles/ControleAutomacao.java`, `src/main/java/br/f1mane/controles/ControleCorrida.java` (instancia e expõe `ControleAutomacao`), `src/main/java/br/f1mane/controles/InterfaceJogo.java` e `src/main/java/br/f1mane/controles/ControleJogoLocal.java` (novo método `processarAutomacao`), `src/main/java/br/f1mane/servidor/JogoServidor.java` (herda `ControleJogoLocal`, sem mudança adicional esperada), pontos de entrada humana em `ControleJogoLocal` que hoje chamam `piloto.setManualTemporario()` diretamente.
- **Fora de escopo**: mecânicas sempre-ativas que a spec `piloto-controle-automatico-manual` já explicita como fora da chave automático/manual (snap de traçado no box, desvio de carro batido sob safety car, escapada por stress/pneu, bandeirada, colisão contra carro da frente, `desviaPilotoNaFrente`) — continuam em `Piloto`, sem mudança. A unificação dos dois caminhos de comando duplicados (Swing local vs REST online, que hoje implementam os mesmos "verbos" com código divergente) também fica fora de escopo — este change só garante que `ControleAutomacao` chama os métodos públicos já existentes, sem tentar unificar as duas pilhas de entrada humana.
- **Testes**: `PilotoAutopilotModoTest`, `PilotoModoIADefesaAtaqueRecuoTest`, `PilotoAgressividadeEfetivaTest` (parte online/manual) precisam ser adaptados ou movidos para testar `ControleAutomacao` em vez de métodos privados de `Piloto` via reflexão.
