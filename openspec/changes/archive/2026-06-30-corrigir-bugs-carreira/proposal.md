## Why

O sistema de distribuição de pontos de carreira possui um bug na fronteira do nível 999: o upgrade do nível 998 para 999 tem custo zero, enquanto o downgrade de 999 para 998 devolve 50 pontos — permitindo geração infinita de pontos. A interação com carreira é exclusivamente web via serviços REST (`LetsRace`).

## What Changes

- Corrigir condição `< 999` → `<= 999` em `Util.processaValorPontosCarreira` para que o upgrade 998→999 tenha o custo correto de 50 pontos
- Remover instrução `setIdCarroLivery` duplicada em `ControleClassificacao.atualizaCarreira` (linha 417)

## Capabilities

### New Capabilities

### Modified Capabilities
- `distribuicao-pontos-carreira`: O custo de upgrade deve ser simétrico ao refund de downgrade em todos os níveis, incluindo 999

## Impact

- `src/main/java/br/nnpe/Util.java` — corrigir condição em `processaValorPontosCarreira`
- `src/main/java/br/f1mane/servidor/controles/ControleClassificacao.java` — remover setter duplicado
