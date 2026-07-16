## MODIFIED Requirements

### Requirement: Tipos de vegetação, água, brita, listrado e xadrez desenham um padrão procedural
Quando `tipo` é `VEGETACAO_DENSA`, `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`, `LISTRADO` ou `XADREZ`, `ObjetoLivre.desenha()` SHALL preencher a forma com `corPimaria` como cor de fundo e, restringindo o desenho seguinte à área da forma (clip), sobrepor um padrão procedural simples e determinístico usando `corSecundaria`: para `VEGETACAO_DENSA`, silhuetas preenchidas de "topo de árvore vista de cima" (polígonos em estrela, com pontas e profundidade de entalhe variando entre 4 silhuetas distintas sorteadas por touceira), com raio ampliado por um fator aleatório entre 2x e 3x por touceira; para `VEGETACAO_SIMPLES`, traços curtos representando touceiras; pequenos arcos ondulados horizontais para água; pequenos círculos deslocados para brita; listras retas paralelas para listrado; e uma grade quadriculada alternada para xadrez. O padrão SHALL ser determinístico (sem aleatoriedade não seedada, exceto onde já documentado para vegetação/brita), produzindo o mesmo resultado visual em desenhos sucessivos do mesmo objeto sem "piscar" ou mudar de frame para frame.

#### Scenario: Vegetação densa desenha silhuetas de topo de árvore sorteadas
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_DENSA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por marcas preenchidas usando `corSecundaria`, cada uma sorteada entre 4 silhuetas de topo de árvore (polígono em estrela com pontas/entalhe variados) com raio ampliado por um fator aleatório entre 2x e 3x, restritas à área da forma

#### Scenario: Vegetação simples continua com o traço diagonal de antes
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_SIMPLES` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de traços diagonais curtos usando `corSecundaria`, restrito à área da forma, sem nenhuma das silhuetas de topo de árvore usadas por `VEGETACAO_DENSA`

#### Scenario: Água desenha padrão de ondas
- **WHEN** um `ObjetoLivre` com `tipo=AGUA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos arcos/ondas horizontais usando `corSecundaria`, restrito à área da forma

#### Scenario: Brita desenha padrão de pontos
- **WHEN** um `ObjetoLivre` com `tipo=BRITA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos círculos deslocados usando `corSecundaria`, restrito à área da forma

#### Scenario: Listrado desenha listras retas paralelas
- **WHEN** um `ObjetoLivre` com `tipo=LISTRADO` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por listras retas paralelas alternando `corSecundaria`, restritas à área da forma

#### Scenario: Xadrez desenha grade quadriculada alternada
- **WHEN** um `ObjetoLivre` com `tipo=XADREZ` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por uma grade de células quadradas, alternando `corSecundaria` e a cor de fundo em padrão quadriculado, restrita à área da forma

#### Scenario: Padrão não muda entre desenhos sucessivos
- **WHEN** o mesmo `ObjetoLivre` com `tipo` diferente de `POLIGONO_SIMPLES` é desenhado duas vezes seguidas sem alteração de posição, tamanho, ângulo ou tipo
- **THEN** o padrão procedural resultante é idêntico nas duas renderizações, incluindo qual silhueta de topo de árvore foi sorteada para cada touceira no caso de `VEGETACAO_DENSA`
