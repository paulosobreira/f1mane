## ADDED Requirements

### Requirement: ObjetoConstrucao tem uma propriedade tipo com quatro variantes de desenho
`ObjetoConstrucao` SHALL expor uma propriedade `tipo` (enum `TipoObjetoConstrucao`: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`), com `QUADRADO` como valor padrão. `desenha()` SHALL desenhar uma forma diferente conforme o `tipo` atual, mantendo posição, ângulo, `corPimaria`, `corSecundaria`, transparência e nível de desenho herdados de `ObjetoPista`/`ObjetoDesenho` para todos os tipos.

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

### Requirement: Empilhamento repete a forma do tipo atual, independente de qual tipo seja
`ObjetoConstrucao` SHALL expor uma propriedade `quantidadeEmpilhamento` (inteiro, mínimo 1, valor padrão 1) que controla quantas vezes a forma do `tipo` atual (`QUADRADO`, `REDONDO`, `CAMINHAO` ou `BARCO`) é desenhada, repetida e deslocada. Empilhamento SHALL funcionar da mesma forma para qualquer valor de `tipo` — não SHALL existir um `tipo` dedicado a esse efeito.

#### Scenario: quantidadeEmpilhamento igual a 1 não gera repetição, para qualquer tipo
- **WHEN** um `ObjetoConstrucao` com `quantidadeEmpilhamento=1` é desenhado, qualquer que seja o `tipo`
- **THEN** o desenho mostra uma única forma, sem repetições

#### Scenario: Empilhamento funciona para qualquer tipo, não só QUADRADO
- **WHEN** um `ObjetoConstrucao` com `quantidadeEmpilhamento` maior que 1 e `tipo=REDONDO`, `CAMINHAO` ou `BARCO` é desenhado
- **THEN** o desenho repete a forma correspondente a esse `tipo` o número de vezes configurado, exatamente como aconteceria com `tipo=QUADRADO`

#### Scenario: Circuito antigo sem quantidadeEmpilhamento gravado assume valor padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `quantidadeEmpilhamento` gravado, é carregado
- **THEN** o objeto assume `quantidadeEmpilhamento=1` (sem repetição), preservando a aparência original

### Requirement: Direção e grau de empilhamento controlam o deslocamento entre repetições
`ObjetoConstrucao` SHALL expor uma propriedade `direcaoEmpilhamento` (enum `DirecaoEmpilhamento` com 8 valores: cima, baixo, esquerda, direita e as 4 diagonais) e uma propriedade `grauEmpilhamento` (inteiro, em pixels, valor padrão maior que zero), que juntas controlam, quando `quantidadeEmpilhamento` é maior que 1, para que direção e quantos pixels cada repetição extra é deslocada em relação à repetição anterior (deslocamento cumulativo). Essas propriedades SHALL existir independentemente do `tipo` atual do objeto.

#### Scenario: direcaoEmpilhamento controla o sentido do deslocamento
- **WHEN** um `ObjetoConstrucao` com `quantidadeEmpilhamento` maior que 1 e uma das 8 direções configurada em `direcaoEmpilhamento` é desenhado
- **THEN** cada repetição extra é deslocada na direção configurada em relação à repetição anterior, formando um empilhamento visível na direção escolhida

#### Scenario: grauEmpilhamento controla quantos pixels cada repetição se desloca
- **WHEN** o usuário configura `grauEmpilhamento` para um valor de N pixels num `ObjetoConstrucao` com `quantidadeEmpilhamento` maior que 1
- **THEN** cada repetição extra é desenhada deslocada N pixels (na direção de `direcaoEmpilhamento`) em relação à repetição imediatamente anterior — não uma fração de `largura`/`altura`, um valor absoluto em pixels

#### Scenario: Circuito antigo sem propriedades de empilhamento assume valores padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `direcaoEmpilhamento`/`grauEmpilhamento` gravados, é carregado
- **THEN** o objeto assume valores padrão não-nulos para `direcaoEmpilhamento` e `grauEmpilhamento`, permitindo que o empilhamento funcione corretamente caso `quantidadeEmpilhamento` seja alterado depois

### Requirement: Largura e altura definem o tamanho da forma, nunca a quantidade ou o deslocamento de repetições
Para todo `tipo` de `ObjetoConstrucao`, as propriedades `largura` e `altura` SHALL controlar exclusivamente o tamanho de cada forma desenhada (cada repetição, quando `quantidadeEmpilhamento` for maior que 1), nunca gerar repetições adicionais nem alterar o deslocamento entre elas. A quantidade de repetições SHALL ser controlada exclusivamente por `quantidadeEmpilhamento`, e o deslocamento entre repetições SHALL ser controlado exclusivamente por `grauEmpilhamento`.

#### Scenario: Aumentar largura não cria repetições extras
- **WHEN** o usuário aumenta `largura` ou `altura` de um `ObjetoConstrucao` com `quantidadeEmpilhamento` inalterado
- **THEN** o número de repetições desenhadas permanece o mesmo, e cada repetição é desenhada maior, refletindo o novo tamanho

#### Scenario: Aumentar quantidadeEmpilhamento não altera o tamanho de cada repetição
- **WHEN** o usuário aumenta `quantidadeEmpilhamento` de um `ObjetoConstrucao` mantendo `largura`/`altura` inalterados
- **THEN** o objeto passa a desenhar mais repetições da forma, todas com o mesmo tamanho de antes

#### Scenario: Aumentar largura não altera o deslocamento entre repetições
- **WHEN** o usuário aumenta `largura` ou `altura` de um `ObjetoConstrucao` com `grauEmpilhamento` inalterado e `quantidadeEmpilhamento` maior que 1
- **THEN** o deslocamento em pixels entre repetições consecutivas permanece o mesmo, apesar de cada repetição agora ser desenhada maior

### Requirement: BARCO tem uma propriedade de afunilamento configurável
`ObjetoConstrucao` SHALL expor uma propriedade `afunilamento` (percentual, valor padrão maior que zero) que controla o quanto a extremidade do tipo `BARCO` se estreita: o comprimento da seção afunilada é proporcional a `afunilamento` sobre `largura`. Essa propriedade SHALL existir independentemente do `tipo` atual do objeto (não afeta o desenho de tipos diferentes de `BARCO`), seguindo o mesmo padrão de propriedades sempre presentes já usado por `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas`.

#### Scenario: Alterar afunilamento muda o comprimento da proa
- **WHEN** o usuário aumenta `afunilamento` de um `ObjetoConstrucao` com `tipo=BARCO`
- **THEN** a seção afunilada da forma ocupa uma fração maior do comprimento total, e o objeto é redesenhado refletindo essa mudança

#### Scenario: Circuito antigo sem afunilamento gravado assume valor padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoConstrucao` sem `afunilamento` gravado, é carregado
- **THEN** o objeto assume o valor padrão de `afunilamento` (maior que zero), permitindo que o tipo `BARCO` seja desenhado corretamente caso o tipo seja alterado depois

### Requirement: obterArea cobre toda a forma desenhada, incluindo repetições de empilhamento
`ObjetoConstrucao.obterArea()` SHALL retornar um `Rectangle` que cobre toda a extensão desenhada pelo `tipo` e `quantidadeEmpilhamento` atuais — quando `quantidadeEmpilhamento` for maior que 1, a união das áreas de todas as repetições desenhadas — sem lançar `NullPointerException` mesmo antes do primeiro `desenha()`.

#### Scenario: Área com empilhamento cobre todas as repetições
- **WHEN** `obterArea()` é chamado após `desenha()` em um `ObjetoConstrucao` com `quantidadeEmpilhamento` maior que 1 (qualquer `tipo`)
- **THEN** o `Rectangle` retornado cobre a extensão de todas as repetições desenhadas, não apenas da primeira

#### Scenario: Área é consultável antes do primeiro desenho, para qualquer tipo
- **WHEN** `obterArea()` é chamado em um `ObjetoConstrucao` recém-criado, antes de `desenha()` ter sido chamado alguma vez, para qualquer valor de `tipo`
- **THEN** `obterArea()` retorna um `Rectangle` válido, não lança `NullPointerException`
