## ADDED Requirements

### Requirement: Box tem sempre a mesma velocidade, sem sorteio rápido/lento
`ControleBox` SHALL deixar de sortear um box "rápido" ou "lento" por corrida (campo `boxRapido` e o sorteio no construtor removidos). Em `processarPilotoBox`, cada um dos quatro incrementos de progresso/velocidade de box (nó de box fora da janela de parada, reta/largada, curva alta, demais casos) SHALL usar um único valor fixo, igual à média entre os valores "rápido" e "lento" de hoje (20/15 → 17 ou 18 conforme arredondamento; 25/20 → 22 ou 23; 20/15 → 17 ou 18; 15/10 → 12 ou 13), em todos os cenários de corrida, sem depender de sorteio.

#### Scenario: Incremento de box é o mesmo em toda corrida
- **WHEN** duas corridas diferentes passam pelo mesmo tipo de nó de box (por exemplo, reta/largada)
- **THEN** o incremento de progresso/velocidade aplicado é idêntico nas duas corridas, independentemente de qualquer sorteio

#### Scenario: isBoxRapido deixa de existir
- **WHEN** o código é compilado após esta mudança
- **THEN** não há mais nenhuma referência a `boxRapido`/`isBoxRapido()` em `ControleBox`, `InterfaceJogo`, `ControleJogoLocal` ou `JogoCliente`

### Requirement: Limite de últimas voltas para decidir parada é único
`Piloto.java` SHALL usar um único valor fixo de `limiteUltimasVoltas` (a média entre os valores 80 e 85 de hoje, arredondada) para decidir se ainda vale a pena entrar no box perto do fim da corrida, em vez de escolher entre 80 e 85 com base em `isBoxRapido()`.

#### Scenario: Decisão de parada tardia usa o mesmo limite sempre
- **WHEN** o piloto avalia se ainda deve entrar no box com a corrida em porcentagem avançada
- **THEN** o limite usado é o mesmo valor fixo em qualquer corrida, independentemente de qualquer característica de box sorteada
