## Why

Hoje `Piloto.processaEscapadaDaPista()` mistura dois conceitos num só bloco condicional (`stress > 90 && AGRESSIVO`): uma "derrapagem" que empurra o piloto do traçado 0 para o 1/2, e a escapada de fato (traçado 1/2 → 4/5, ancorada em `ObjetoEscapada`). O ramo de derrapagem só é alcançado hoje porque `escapaTracado()` — o gatilho geométrico antigo baseado em `PontoEscape`/`escapeMap` — está permanentemente inerte (`Circuito.gerarEscapeMap()` mantém `escapeMap` sempre vazio, por decisão já documentada). Isso deixa uma cadeia grande de campos, métodos e overlays de debug (`pontoEscape`, `distanciaEscape`, `indexRefEscape`, `PontoEscape`, `obterLadoEscape`) que nunca produzem efeito em produção, além de dois bugs de comportamento na escapada ancorada: (1) um piloto marcado para escapar pode escapar da marca só mudando de modo para LENTO antes de alcançar a entrada da zona, e (2) nada impede o piloto marcado de trocar de traçado (voltar pro 0, por exemplo) antes de cumprir a escapada.

## What Changes

- **BREAKING**: A derrapagem (traçado 0 → 1/2) deixa de depender de `stress`/`AGRESSIVO`. Passa a ser sua própria mecânica, independente: piloto no traçado 0, em curva baixa ou alta, com pneus abaixo de 30% que falha em `testeHabilidadePilotoFreios()` deriva para o traçado 1 ou 2 (mesma escolha de lado de hoje: alterna com `getTracadoAntigo()`, ou sorteia se não houver traçado anterior).
- **BREAKING**: A escapada ancorada ao traçado (`processaEscapadaAncoradaAoTracado()`) passa a fazer até dois testes sequenciais, uma única vez por `ObjetoEscapada` por volta por piloto, assim que a zona entra na janela de detecção (recalibrada para 150 índices à frente, mantendo a tolerância de salto de ciclo já existente):
  1. `stress > 90 && AGRESSIVO && !testeHabilidadePiloto()` → se verdadeiro, marca o piloto para escapar nessa zona.
  2. Só se o teste 1 não marcou: `pneus < 30% && !LENTO && !testeHabilidadePiloto()` → se verdadeiro, marca o piloto para escapar nessa zona.
- **BREAKING**: Uma vez marcado para escapar, a mudança de `modoPilotagem` (para NORMAL ou LENTO) antes de alcançar a entrada da zona deixa de salvar o piloto — a escapada é executada de qualquer forma ao alcançar `indiceEntrada`.
- **BREAKING**: Um piloto marcado para escapar fica proibido de mudar de traçado (`mudarTracado`) por qualquer outra via até cumprir a escapada (entrar de fato no traçado de fuga 4/5).
- Remove o gatilho geométrico antigo (`escapaTracado()`, `processaPontoEscape()`) e todo o código morto associado (`PontoEscape`, `Circuito.escapeMap`/`getEscapeMap()`/`setEscapeMap()`, `ControleRecursos`/`InterfaceJogo#obterLadoEscape`, `Carro.RAIO_DERRAPAGEM`, os overlays de debug em `MainPanelEditor`/`PainelCircuito`, o botão de debug em `MainFrame`, e o ramo morto de desvio proativo em `processaMudarTracado()`).

## Capabilities

### New Capabilities
- `derrapagem-piloto`: mecânica de derrapagem do traçado 0 para o 1/2 por pneus gastos + falha no teste de habilidade de freios em curva, desacoplada de stress/modo de pilotagem.

### Modified Capabilities
- `escapada-ia-corrida`: substitui o modelo de "piloto em risco + teste único" por dois testes sequenciais (stress+agressividade, depois pneus), recalibra a janela de detecção para 150 índices, remove a exceção de LENTO durante a janela de comprometimento (LENTO só previne ser marcado, não salva quem já foi marcado) e adiciona o bloqueio de troca de traçado enquanto marcado.
- `tracado-safe-lane-change`: atualiza a referência ao antigo `escapaTracado()` (removido) na regra de espera de animação, e documenta o bloqueio de troca de traçado para piloto marcado para escapar.
- `dead-code-removal`: registra a remoção do gatilho geométrico antigo e de toda a plumbing associada (`PontoEscape`, `escapeMap`, `obterLadoEscape`, `RAIO_DERRAPAGEM`, overlays de debug, ramo morto de desvio em `processaMudarTracado()`).

## Impact

- `src/main/java/br/f1mane/entidades/Piloto.java`: reescreve `processaEscapadaDaPista()` (nova mecânica de derrapagem), `processaEscapadaAncoradaAoTracado()`/`emRiscoDeEscapada()` (novo modelo de dois testes + janela 150 + trava de traçado), `mudarTracado()` (nova guarda de bloqueio), remove `escapaTracado()`, `processaPontoEscape()`, campos `pontoEscape`/`distanciaEscape`/`indexRefEscape` e getters associados, e o ramo morto em `processaMudarTracado()`/`modoIADefesaAtaque()`.
- `src/main/java/br/f1mane/entidades/Circuito.java`: remove `escapeMap`/`getEscapeMap()`/`setEscapeMap()`; `gerarEscapeMap()` (ou seu sucessor) continua só populando `pista4Full`/`pista5Full`.
- `src/main/java/br/f1mane/entidades/PontoEscape.java`: removido.
- `src/main/java/br/f1mane/entidades/Carro.java`: remove `RAIO_DERRAPAGEM`.
- `src/main/java/br/f1mane/controles/ControleRecursos.java`, `src/main/java/br/f1mane/controles/InterfaceJogo.java`: remove `obterLadoEscape`.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java`, `src/main/java/br/f1mane/visao/PainelCircuito.java`: remove overlays de debug que iteram `escapeMap`/`getPontoDerrapada`.
- `src/main/java/br/f1mane/MainFrame.java`: remove (ou reaponta) o botão de debug `escapaTracado`.
- Testes afetados: `PilotoEscapadaAncoradaTracadoTest.java` (reescrita ampla), `PilotoDesconcentracaoConvertidaTest.java` (os dois testes que simulam `escapaTracado()` via reflection deixam de fazer sentido), `CircuitoEscapadaTracadoTest.java`, `CircuitoMetadadosArquivoTest.java` (referência a `getEscapeMap()`), novos testes para `derrapagem-piloto`.
