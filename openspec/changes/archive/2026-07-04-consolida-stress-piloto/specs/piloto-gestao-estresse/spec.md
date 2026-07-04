## ADDED Requirements

### Requirement: Ponto único de decisão de estresse
`Piloto.processaStress()` SHALL ser o único método que decide quando e quanto o estresse do piloto (`incStress`/`decStress`) muda a cada tick. Nenhum outro método, em nenhuma outra classe, SHALL chamar `incStress`/`decStress` diretamente.

#### Scenario: Desgaste de pneu não altera estresse diretamente
- **WHEN** `Carro.calculaDesgastePneus(No)` é executado para qualquer tipo de nó
- **THEN** o método não chama `incStress`/`decStress` do piloto; ele pode ler `getStress()` para decidir seus próprios efeitos (ex.: desgaste do pneu), mas não escreve no estresse

#### Scenario: Colisão não altera estresse diretamente
- **WHEN** `Piloto.processaPenalidadeColisao()` é executado com uma colisão em andamento
- **THEN** o método não chama `incStress` diretamente; a penalidade de estresse por colisão é aplicada por `processaStress()`

#### Scenario: Avanço na fila do box não altera estresse diretamente
- **WHEN** `ControleBox` avança um piloto na fila do box
- **THEN** `ControleBox` não chama `decStress` diretamente; a redução de estresse por estar no box é aplicada por `processaStress()`

#### Scenario: Acidente com perda de aerofólio não altera estresse diretamente
- **WHEN** `ControleCorrida.danificaAreofolio(Piloto)` decide que houve dano de aerofólio
- **THEN** o método não chama `incStress` diretamente; ele sinaliza o evento para o piloto, e `processaStress()` aplica o incremento de estresse correspondente no mesmo tick

### Requirement: Regras de disparo preservadas
`Piloto.processaStress()` SHALL reavaliar, para cada gatilho migrado, exatamente a mesma condição e o mesmo valor que existiam antes da consolidação — a mudança é de organização de código, não de comportamento de jogo.

#### Scenario: Pneu incompatível com o clima
- **WHEN** o piloto está numa curva baixa ou alta com pneu incompatível com o clima atual
- **THEN** `processaStress()` incrementa o estresse em 4 (curva baixa) ou 2 (curva alta), reduzido a 0 se `testeHabilidadePiloto()` for bem-sucedido — igual ao comportamento anterior em `processaPneusIncomaptiveis()`

#### Scenario: Freada mal-sucedida sob pressão
- **WHEN** o piloto está em curva baixa, freando na reta, entre os 3 primeiros colocados, com sorteio acima de 0.9 e falha no teste de habilidade de freios
- **THEN** `processaStress()` incrementa o estresse em `10 - desgastePneus/100` — igual ao comportamento anterior em `processaFreioNaReta()`

#### Scenario: Desconcentração em modo agressivo
- **WHEN** o piloto está no modo AGRESSIVO, com estresse abaixo de 70, e desconcentrado
- **THEN** `processaStress()` incrementa o estresse em 1 — igual ao comportamento anterior em `decrementaPilotoDesconcentrado()`

#### Scenario: Desgaste de pneu por tipo de nó
- **WHEN** o carro processa desgaste de pneu numa curva baixa, curva alta ou reta/largada, sob as mesmas condições de clima, box e limiares de estresse (80/70/60) de antes da consolidação
- **THEN** `processaStress()` aplica o mesmo incremento/decremento que `Carro.calculaDesgastePneus()` aplicava, usando a mesma fórmula baseada no desgaste do pneu (`10 - desgastePneus/100` para incremento, `desgastePneus/100` para decremento)

#### Scenario: Acidente com perda de aerofólio
- **WHEN** `ControleCorrida.danificaAreofolio()` sinaliza que o piloto sofreu dano de aerofólio neste tick
- **THEN** `processaStress()` incrementa o estresse em 15 nesse mesmo tick

#### Scenario: Avanço na fila do box
- **WHEN** o piloto está avançando na fila de espera do box (posição na fila menor que o tamanho total da fila)
- **THEN** `processaStress()` decrementa o estresse em 2 a cada tick — igual ao comportamento anterior em `ControleBox`

### Requirement: Ordem de RNG pode mudar sem afetar comportamento agregado
A consolidação SHALL poder alterar a ordem em que `GameRandom` é consultado dentro do tick (já que gatilhos antes avaliados em `Carro.calculaDesgastePneus`, que roda antes de `processaStress()`, passam a ser avaliados dentro de `processaStress()`), desde que a frequência e a intensidade agregada das mudanças de estresse permaneçam equivalentes às de antes da mudança.

#### Scenario: Seed fixa produz trajetória diferente, comportamento agregado equivalente
- **WHEN** uma simulação roda com uma seed de RNG fixa antes e depois da consolidação
- **THEN** a sequência exata de valores sorteados pode diferir (não há garantia de replay bit-a-bit idêntico), mas a distribuição de frequência/intensidade de mudanças de estresse ao longo de uma corrida longa é estatisticamente equivalente
