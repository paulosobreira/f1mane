## ADDED Requirements

### Requirement: Ponto de guard rails alinha (snap) a nós de pista próximos
Ao posicionar um ponto de um `ObjetoGuardRails` — seja durante a criação ponto a ponto, seja arrastando um ponto existente em modo de edição — o editor SHALL, se a posição do cursor estiver dentro de uma tolerância em pixels de tela de um nó de pista (`No`, de `circuito.getNos()` ou `circuito.getNosBox()`), usar a posição desse nó em vez da posição bruta do cursor.

#### Scenario: Ponto encosta em um nó de pista durante a criação
- **WHEN** o usuário clica para adicionar um ponto de um `ObjetoGuardRails` em criação, e a posição do clique está dentro da tolerância de snap de um nó de pista
- **THEN** o ponto adicionado usa a posição desse nó, não a posição exata do clique

#### Scenario: Clique fora da tolerância não sofre snap
- **WHEN** o usuário clica para adicionar um ponto de um `ObjetoGuardRails` em criação, e nenhum nó de pista está dentro da tolerância de snap
- **THEN** o ponto adicionado usa a posição exata do clique, sem ajuste

### Requirement: Ponto de guard rails alinha (snap) a pontos de outros objetos de cenário
O editor SHALL também considerar, para o mesmo cálculo de snap, os pontos/vértices de outros objetos de cenário já presentes em `circuito.getObjetosCenario()` (por exemplo, os pontos de outro `ObjetoGuardRails` ou os vértices de um `ObjetoLivre`), alinhando o ponto sendo posicionado ao ponto mais próximo dentre nós de pista e pontos de outros objetos, quando mais de um estiver dentro da tolerância.

#### Scenario: Snap a um ponto de outro guard rails
- **WHEN** o usuário posiciona um ponto de um `ObjetoGuardRails` dentro da tolerância de snap de um ponto pertencente a outro `ObjetoGuardRails` já no circuito
- **THEN** o ponto adicionado/movido usa a posição desse ponto vizinho

#### Scenario: Nó de pista e ponto de objeto competem pelo snap
- **WHEN** um nó de pista e um ponto de outro objeto de cenário estão ambos dentro da tolerância de snap do cursor, a distâncias diferentes
- **THEN** o snap usa o candidato mais próximo do cursor entre os dois

### Requirement: Snap também se aplica ao arrastar um ponto existente
O mesmo comportamento de snap a nós de pista e a pontos de outros objetos SHALL se aplicar ao arrastar um ponto já existente de um `ObjetoGuardRails` em modo de edição de pontos, não apenas durante a criação inicial.

#### Scenario: Arrastar um ponto existente encosta em um nó vizinho
- **WHEN** o usuário arrasta um ponto existente de um `ObjetoGuardRails` para dentro da tolerância de snap de um nó de pista
- **THEN** a posição final do ponto, ao soltar o botão do mouse, é a posição desse nó
