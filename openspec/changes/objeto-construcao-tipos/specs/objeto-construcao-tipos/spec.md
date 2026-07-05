## ADDED Requirements

### Requirement: ObjetoConstrucao tem uma propriedade tipo com cinco variantes de desenho
`ObjetoConstrucao` SHALL expor uma propriedade `tipo` (enum `TipoObjetoConstrucao`: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`, `PREDIO`), com `QUADRADO` como valor padrão. `desenha()` SHALL desenhar uma forma diferente conforme o `tipo` atual, mantendo posição, ângulo, `corPimaria`, `corSecundaria`, transparência e nível de desenho herdados de `ObjetoPista`/`ObjetoDesenho` para todos os tipos.

#### Scenario: Objeto recém-criado nasce QUADRADO
- **WHEN** um novo `ObjetoConstrucao` é instanciado sem que o usuário defina um tipo
- **THEN** `getTipo()` retorna `QUADRADO` e o objeto é desenhado com a forma atual (dois retângulos arredondados aninhados)

#### Scenario: Circuito antigo sem tipo gravado carrega como QUADRADO
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `tipo` gravado, é carregado
- **THEN** o objeto assume `tipo=QUADRADO`, preservando a aparência original

#### Scenario: Tipo REDONDO desenha elipses aninhadas
- **WHEN** um `ObjetoConstrucao` com `tipo=REDONDO` é desenhado
- **THEN** o desenho usa a mesma composição de forma externa (cor primária) e interna (cor secundária) do `QUADRADO`, mas como elipses em vez de retângulos arredondados

#### Scenario: Tipo CAMINHAO desenha duas formas lado a lado
- **WHEN** um `ObjetoConstrucao` com `tipo=CAMINHAO` é desenhado
- **THEN** o desenho mostra duas formas retangulares lado a lado dentro da área do objeto (não aninhadas) — uma de proporção aproximadamente quadrada e outra retangular alongada — usando `corPimaria` e `corSecundaria` para diferenciá-las

#### Scenario: Tipo BARCO desenha um retângulo com uma ponta afunilada
- **WHEN** um `ObjetoConstrucao` com `tipo=BARCO` é desenhado
- **THEN** o desenho mostra um retângulo cuja extremidade correspondente ao maior valor de X local se estreita progressivamente até um ponto, formando uma proa, antes de qualquer rotação por `angulo`

#### Scenario: Tipo PREDIO repete a forma QUADRADO empilhada
- **WHEN** um `ObjetoConstrucao` com `tipo=PREDIO` é desenhado
- **THEN** o desenho repete a mesma forma usada pelo tipo `QUADRADO` uma vez por andar configurado em `quantidadeAndares`, deslocando cada repetição na direção configurada em `direcaoEmpilhamento`

### Requirement: Largura e altura definem o tamanho da forma, nunca a quantidade de repetições
Para todo tipo de `ObjetoConstrucao`, incluindo `PREDIO`, as propriedades `largura` e `altura` SHALL controlar exclusivamente o tamanho de cada forma desenhada (cada andar, no caso do `PREDIO`), nunca gerar repetições adicionais da forma. A quantidade de repetições do `PREDIO` SHALL ser controlada exclusivamente pela propriedade `quantidadeAndares`.

#### Scenario: Aumentar largura não cria andares extras
- **WHEN** o usuário aumenta `largura` ou `altura` de um `ObjetoConstrucao` com `tipo=PREDIO` e `quantidadeAndares` inalterado
- **THEN** o número de andares desenhados permanece o mesmo, e cada andar é desenhado maior, refletindo o novo tamanho

#### Scenario: Aumentar quantidadeAndares não altera o tamanho de cada andar
- **WHEN** o usuário aumenta `quantidadeAndares` de um `ObjetoConstrucao` com `tipo=PREDIO` mantendo `largura`/`altura` inalterados
- **THEN** o objeto passa a desenhar mais repetições da forma, todas com o mesmo tamanho de antes

### Requirement: BARCO tem uma propriedade de afunilamento configurável
`ObjetoConstrucao` SHALL expor uma propriedade `afunilamento` (percentual, valor padrão maior que zero) que controla o quanto a extremidade do tipo `BARCO` se estreita: o comprimento da seção afunilada é proporcional a `afunilamento` sobre `largura`. Essa propriedade SHALL existir independentemente do `tipo` atual do objeto (não afeta o desenho de tipos diferentes de `BARCO`), seguindo o mesmo padrão de propriedades sempre presentes já usado por `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas`.

#### Scenario: Alterar afunilamento muda o comprimento da proa
- **WHEN** o usuário aumenta `afunilamento` de um `ObjetoConstrucao` com `tipo=BARCO`
- **THEN** a seção afunilada da forma ocupa uma fração maior do comprimento total, e o objeto é redesenhado refletindo essa mudança

#### Scenario: Circuito antigo sem afunilamento gravado assume valor padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `afunilamento` gravado, é carregado
- **THEN** o objeto assume o valor padrão de `afunilamento` (maior que zero), permitindo que o tipo `BARCO` seja desenhado corretamente caso o tipo seja alterado depois

### Requirement: PREDIO tem propriedades de quantidade e direção de empilhamento
`ObjetoConstrucao` SHALL expor uma propriedade `quantidadeAndares` (inteiro, mínimo 1, valor padrão 1) e uma propriedade `direcaoEmpilhamento` (enum `DirecaoEmpilhamento` com 8 valores: acima, abaixo, esquerda, direita e as 4 diagonais), que juntas controlam quantas vezes a forma é repetida e para que direção cada repetição extra é deslocada em relação à anterior, quando `tipo=PREDIO`. Essas propriedades SHALL existir independentemente do `tipo` atual do objeto.

#### Scenario: quantidadeAndares controla o número de repetições
- **WHEN** um `ObjetoConstrucao` com `tipo=PREDIO` e `quantidadeAndares=1` é desenhado
- **THEN** o desenho mostra uma única forma, sem repetições, idêntico ao tipo `QUADRADO` no mesmo tamanho/posição

#### Scenario: direcaoEmpilhamento controla o deslocamento das repetições
- **WHEN** um `ObjetoConstrucao` com `tipo=PREDIO`, `quantidadeAndares` maior que 1 e uma das 8 direções configurada em `direcaoEmpilhamento` é desenhado
- **THEN** cada repetição extra é deslocada na direção configurada em relação à repetição anterior, formando um empilhamento visível na direção escolhida

#### Scenario: Circuito antigo sem propriedades de empilhamento assume valores padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `quantidadeAndares`/`direcaoEmpilhamento` gravados, é carregado
- **THEN** o objeto assume `quantidadeAndares=1` e um valor padrão de `direcaoEmpilhamento`, permitindo que o tipo `PREDIO` seja desenhado corretamente caso o tipo seja alterado depois

### Requirement: obterArea cobre toda a forma desenhada, incluindo repetições do PREDIO
`ObjetoConstrucao.obterArea()` SHALL retornar um `Rectangle` que cobre toda a extensão desenhada pelo tipo atual — para `PREDIO`, a união das áreas de todos os andares desenhados — sem lançar `NullPointerException` mesmo antes do primeiro `desenha()`.

#### Scenario: Área do PREDIO cobre todos os andares
- **WHEN** `obterArea()` é chamado após `desenha()` em um `ObjetoConstrucao` com `tipo=PREDIO` e `quantidadeAndares` maior que 1
- **THEN** o `Rectangle` retornado cobre a extensão de todos os andares desenhados, não apenas do primeiro

#### Scenario: Área é consultável antes do primeiro desenho, para qualquer tipo
- **WHEN** `obterArea()` é chamado em um `ObjetoConstrucao` recém-criado, antes de `desenha()` ter sido chamado alguma vez, para qualquer valor de `tipo`
- **THEN** `obterArea()` retorna um `Rectangle` válido, não lança `NullPointerException`
