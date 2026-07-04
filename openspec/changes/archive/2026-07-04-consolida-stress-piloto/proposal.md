## Why

O incremento/decremento de estresse do piloto está espalhado em 8 pontos de chamada, em 4 classes diferentes (`Piloto`, `Carro`, `ControleCorrida`, `ControleBox`), cada um decidindo independentemente quando e quanto o estresse muda. Isso torna difícil enxergar, auditar ou ajustar a regra completa de estresse do piloto num único lugar — qualquer mudança de comportamento exige caçar todos os pontos espalhados. `Piloto.processaStress()` já existe e roda a cada tick, mas hoje só cobre o decaimento passivo por modo de pilotagem (NORMAL/LENTO); os demais gatilhos (colisão, pneu incompatível, freada mal-sucedida, desconcentração, desgaste de pneu, acidente com perda de aerofólio, avanço na fila do box) ficam fora dele.

## What Changes

- Mover para dentro de `Piloto.processaStress()` a decisão completa (condição de disparo + valor) de todos os incrementos/decrementos de estresse hoje espalhados em:
  - `Piloto.processaPneusIncomaptiveis()` (pneu incompatível com o clima, curva baixa/alta)
  - `Piloto.processaPenalidadeColisao()` (colisão em andamento, com bônus se `evitaBaterCarroFrente`)
  - `Piloto.processaFreioNaReta()` (freada mal-sucedida sob pressão em curva baixa)
  - `Piloto.decrementaPilotoDesconcentrado()` (modo agressivo desconcentrado)
  - `Carro.calculaDesgastePneus()` (4 chamadas: desgaste de pneu por tipo de nó)
  - `ControleCorrida.danificaAreofolio()` (acidente com perda de aerofólio)
  - `ControleBox` (avanço na fila do box)
- Todos os locais originais passam a apenas **ler** `getStress()`/estado relevante quando precisarem (ex.: `Carro.calculaDesgastePneus` ainda usa `stress > 80` pra decidir o desgaste do pneu no mesmo tick), mas nunca mais chamam `incStress`/`decStress` diretamente.
- `incStress`/`decStress` continuam existindo como os únicos métodos que de fato escrevem no campo `stress` (mecanismo de cap/RNG/posição já centralizado ali) — a mudança é sobre **quem decide chamá-los**, não sobre o mecanismo em si.
- **BREAKING (comportamento de simulação, não de API)**: a ordem dos sorteios de RNG no tick muda, já que os gatilhos hoje disparados de dentro de `Carro.calculaDesgastePneus` (que roda antes de `processaStress()` no tick) passam a ser avaliados dentro de `processaStress()`. Resultados de uma simulação com seed fixa deixam de ser idênticos aos de antes da mudança; o comportamento agregado (frequência/intensidade das mudanças de estresse) permanece o mesmo.

## Capabilities

### New Capabilities
- `piloto-gestao-estresse`: documenta a regra completa e centralizada de quando e quanto o estresse do piloto aumenta ou diminui, hoje implementada inteiramente dentro de `Piloto.processaStress()`.

### Modified Capabilities
(nenhuma — não há spec de comportamento de jogo existente sendo alterada; este é um refactor de organização de código, sem mudança de regra de negócio observável pelo jogador)

## Impact

- **Código afetado**: `Piloto.java` (`processaStress`, `processaPneusIncomaptiveis`, `processaPenalidadeColisao`, `processaFreioNaReta`, `decrementaPilotoDesconcentrado`), `Carro.java` (`calculaDesgastePneus`), `ControleCorrida.java` (`danificaAreofolio`), `ControleBox.java` (processamento de fila do box).
- **Testes**: os testes que hoje verificam incremento/decremento de estresse a partir desses métodos (via mocks de `InterfaceJogo`/`GameRandom`) precisam ser reescritos para chamar `processaStress()` e verificar o `getStress()` resultante, em vez de verificar chamadas diretas a `incStress`/`decStress` nos métodos originais.
- **Determinismo**: qualquer teste ou replay que dependa da sequência exata de chamadas de `GameRandom` no tick será afetado pela mudança de ordem descrita acima.
- **Sem mudança de API pública**: `incStress`/`decStress`/`getStress`/`setStress` continuam com a mesma assinatura.
