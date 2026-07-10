## MODIFIED Requirements

### Requirement: ObjetoLivre tem um tipo que define um padrão de preenchimento
`ObjetoLivre` SHALL expor uma propriedade `tipo` do enum `TipoObjetoLivre` (`POLIGONO_SIMPLES`, `VEGETACAO_DENSA`, `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`, `LISTRADO`, `XADREZ`), com `POLIGONO_SIMPLES` como valor padrão quando não definida. O formulário de edição de objetos (`FormularioObjetos`) SHALL permitir escolher o `tipo` quando o objeto em edição é um `ObjetoLivre`.

#### Scenario: ObjetoLivre recém-criado tem tipo padrão
- **WHEN** um novo `ObjetoLivre` é instanciado sem que o usuário defina um tipo
- **THEN** `getTipo()` retorna `POLIGONO_SIMPLES`

#### Scenario: Usuário escolhe um tipo no formulário
- **WHEN** o usuário abre o formulário de edição de um `ObjetoLivre` e seleciona "Água" no campo de tipo
- **THEN** `objetoLivre.getTipo()` passa a retornar `AGUA` após salvar o formulário

#### Scenario: Usuário escolhe listrado ou xadrez no formulário
- **WHEN** o usuário abre o formulário de edição de um `ObjetoLivre` e seleciona "Listrado" ou "Xadrez" no campo de tipo
- **THEN** `objetoLivre.getTipo()` passa a retornar `LISTRADO` ou `XADREZ`, respectivamente, após salvar o formulário

### Requirement: Tipos de vegetação, água, brita, listrado e xadrez desenham um padrão procedural
Quando `tipo` é `VEGETACAO_DENSA`, `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`, `LISTRADO` ou `XADREZ`, `ObjetoLivre.desenha()` SHALL preencher a forma com `corPimaria` como cor de fundo e, restringindo o desenho seguinte à área da forma (clip), sobrepor um padrão procedural simples e determinístico usando `corSecundaria`: traços curtos representando touceiras para vegetação (densa ou simples), pequenos arcos ondulados horizontais para água, pequenos círculos deslocados para brita, listras retas paralelas para listrado, e uma grade quadriculada alternada para xadrez. O padrão SHALL ser determinístico (sem aleatoriedade não seedada, exceto onde já documentado para vegetação/brita), produzindo o mesmo resultado visual em desenhos sucessivos do mesmo objeto sem "piscar" ou mudar de frame para frame.

#### Scenario: Vegetação densa desenha padrão de touceiras
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_DENSA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de traços curtos usando `corSecundaria`, restrito à área da forma

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
- **THEN** o padrão procedural resultante é idêntico nas duas renderizações

## ADDED Requirements

### Requirement: Ângulo do objeto rotaciona o padrão, não a silhueta do objeto livre
Para todo `ObjetoLivre` cujo `tipo` seja diferente de `POLIGONO_SIMPLES`, `ObjetoLivre.desenha()` SHALL rotacionar o conteúdo do padrão procedural (grade, dispersão, listras ou xadrez) pelo `angulo` do objeto, em torno do centro da forma — sem rotacionar a silhueta/contorno do objeto livre em si, que permanece sempre desenhada na mesma orientação, qualquer que seja o valor de `angulo`. A área de clique (`obterAreaClique()`) SHALL acompanhar esse comportamento, permanecendo não rotacionada, para continuar coincidindo com a forma realmente desenhada.

#### Scenario: Padrão rotaciona sem rotacionar a silhueta
- **WHEN** um `ObjetoLivre` com `tipo=XADREZ` (ou qualquer outro tipo não sólido) tem `angulo` diferente de `0`
- **THEN** as linhas do padrão desenhado aparecem rotacionadas na tela pelo `angulo` do objeto, mas o contorno externo da forma preenchida com `corPimaria` permanece na mesma posição/orientação de quando `angulo` é `0`

#### Scenario: Ângulo não altera a silhueta de um objeto de qualquer tipo
- **WHEN** um `ObjetoLivre` (de qualquer `tipo`, incluindo `POLIGONO_SIMPLES`) é desenhado duas vezes com `angulo` diferente entre as duas renderizações, mantendo posição e tamanho
- **THEN** os limites da área pintada com `corPimaria` (a silhueta) são idênticos nas duas renderizações

#### Scenario: Ângulo zero preserva o padrão alinhado aos eixos
- **WHEN** um `ObjetoLivre` com `tipo` não sólido tem `angulo` igual a `0`
- **THEN** o padrão é desenhado alinhado aos eixos horizontal/vertical, como no comportamento anterior a esta mudança

#### Scenario: Rotação do padrão não altera o clip nem extrapola a área da forma
- **WHEN** um `ObjetoLivre` com `tipo` não sólido e `angulo` diferente de `0` é desenhado
- **THEN** o padrão continua restrito à área (fixa, não rotacionada) da forma, sem desenhar fora dos limites da silhueta

### Requirement: Padrão rotacionado cobre toda a área do objeto livre, incluindo os cantos
Para toda forma de `ObjetoLivre` cujo bounding box não seja quadrado, `ObjetoLivre.desenha()` SHALL gerar o padrão procedural sobre uma área maior que o bounding box exato da forma — grande o suficiente para que, depois de rotacionada por qualquer `angulo`, a cobertura resultante continue alcançando toda a silhueta original, sem deixar cantos ou regiões sem padrão (limitados apenas à cor primária).

#### Scenario: Canto de um retângulo alongado continua coberto após rotação
- **WHEN** um `ObjetoLivre` não quadrado (ex.: um retângulo bem mais largo que alto) com `tipo` não sólido tem `angulo` diferente de `0`
- **THEN** cada um dos quatro cantos da silhueta continua tendo `corSecundaria` desenhada em sua vizinhança, não ficando limitado apenas à `corPimaria` de fundo

#### Scenario: Cobertura extra é recortada pelo clip da silhueta real
- **WHEN** o padrão é gerado sobre uma área maior que o bounding box da forma para garantir a cobertura após rotação
- **THEN** nenhum pixel do padrão aparece fora do contorno real da forma — o excesso de área de geração é recortado pelo clip, que continua sendo a silhueta exata (não expandida)

#### Scenario: Área de clique não rotaciona junto com o ângulo
- **WHEN** um `ObjetoLivre` tem `angulo` diferente de `0`
- **THEN** `obterAreaClique()` retorna a área não rotacionada (apenas expandida pela margem de tolerância), coincidindo com o contorno realmente desenhado
