# editor-marcadores-transparencia

## Purpose

Comportamento do editor de circuitos ao autorar um `ObjetoTransparencia`: nível de desenho padrão sempre por cima, traço de desenho destacado enquanto o objeto está sendo desenhado, cálculo automático do intervalo de nó (`inicioTransparencia`/`fimTransparencia`) a partir da área do objeto, e marcadores visuais tracejados de início/fim quando o objeto está selecionado — tornando visível, no próprio editor, em que trecho da pista o recorte de transparência definido em `transparencia-intervalo-no` vai realmente se aplicar.

## Requirements

### Requirement: Novo objeto de transparência nasce com nível de desenho no topo
Quando o usuário finaliza o desenho de um novo `ObjetoTransparencia` no editor, o sistema SHALL definir seu `nivelDesenho` como `100` por padrão, garantindo que ele desenhe por cima de qualquer objeto de cenário comum na tela do editor, sem exigir ajuste manual.

#### Scenario: Objeto de transparência recém-criado desenha por cima de outros objetos
- **WHEN** o usuário termina de desenhar um `ObjetoTransparencia` (clique direito finalizando o polígono) sobre uma área onde já existe outro objeto de cenário com nível de desenho menor
- **THEN** o novo `ObjetoTransparencia` é desenhado por cima desse objeto de cenário, e seu `nivelDesenho` fica em `100`

#### Scenario: Nível padrão não afeta objetos de transparência já existentes
- **WHEN** um circuito já salvo, com um `ObjetoTransparencia` cujo `nivelDesenho` é diferente de `100`, é carregado no editor
- **THEN** o `nivelDesenho` desse objeto permanece o valor salvo, sem ser alterado automaticamente para `100`

### Requirement: Traço de desenho do objeto de transparência é destacado
Enquanto o usuário está desenhando o polígono de um `ObjetoTransparencia` (clicando pontos, antes de finalizar), o sistema SHALL desenhar as linhas entre os pontos já clicados com cor destacada e traço mais grosso que o traço padrão de 1px, com um marcador em cada vértice já clicado — no mesmo padrão visual já usado para `ObjetoLivre`. Esse traço em desenho, junto dos marcadores de intervalo do requirement seguinte, SHALL ser desenhado por cima de qualquer objeto do circuito (inclusive outros objetos de nível de desenho positivo), para que o usuário sempre consiga ver por onde está desenhando/editando a transparência.

#### Scenario: Preview do polígono em desenho usa traço destacado
- **WHEN** o usuário está desenhando um `ObjetoTransparencia` e já clicou dois ou mais pontos
- **THEN** as linhas conectando esses pontos são desenhadas com cor destacada e espessura maior que 1px, com um marcador visível em cada vértice

#### Scenario: Traço em desenho aparece por cima de outros objetos
- **WHEN** o usuário está desenhando um `ObjetoTransparencia` sobre uma área onde há outro objeto com nível de desenho maior que zero (por exemplo, outro `ObjetoTransparencia` já no nível padrão 100)
- **THEN** o traço em desenho continua visível por cima desse outro objeto, em vez de ficar escondido atrás dele

### Requirement: Início e fim de transparência são calculados automaticamente a partir da área do objeto
Ao finalizar o desenho de um `ObjetoTransparencia`, ou sempre que ele for reposicionado ou redimensionado (arrastar, setas de movimento, ou alterar largura/altura/ângulo pelo menu de contexto), o sistema SHALL recalcular `inicioTransparencia` e `fimTransparencia` como, respectivamente, o menor e o maior índice de nó dentre os nós da pista principal (`circuito.getPistaFull()`) cujo ponto cai dentro da área do objeto. Se nenhum nó da pista cair dentro da área do objeto, ambos os campos SHALL ficar em `0` (equivalente a "sem intervalo configurado", aplicando-se ao circuito inteiro). Esses campos continuam editáveis manualmente pelo usuário a qualquer momento depois do cálculo automático.

#### Scenario: Finalizar o desenho calcula o intervalo automaticamente
- **WHEN** o usuário termina de desenhar um `ObjetoTransparencia` cuja área cobre um trecho da pista principal
- **THEN** `inicioTransparencia` e `fimTransparencia` são definidos, respectivamente, como o menor e o maior índice de nó da pista principal dentro da área do objeto, sem exigir digitação manual

#### Scenario: Reposicionar o objeto recalcula o intervalo
- **WHEN** o usuário arrasta, usa as setas de movimento, ou altera largura/altura/ângulo de um `ObjetoTransparencia` já existente, movendo sua área para cobrir um trecho diferente da pista
- **THEN** `inicioTransparencia` e `fimTransparencia` são recalculados para refletir os nós da pista cobertos pela nova área, substituindo o valor anterior (inclusive um valor editado manualmente antes do reposicionamento)

#### Scenario: Objeto fora da faixa da pista principal não recebe intervalo
- **WHEN** o usuário finaliza ou reposiciona um `ObjetoTransparencia` cuja área não cobre nenhum nó de `circuito.getPistaFull()`
- **THEN** `inicioTransparencia` e `fimTransparencia` ficam ambos em `0`

#### Scenario: Valor calculado automaticamente continua editável manualmente
- **WHEN** o usuário abre o menu de contexto (painel de ajuste rápido) de um `ObjetoTransparencia` cujo intervalo foi calculado automaticamente
- **THEN** o usuário consegue alterar `inicioTransparencia` e `fimTransparencia` manualmente pelos campos do menu de contexto, e esse valor manual persiste até o próximo reposicionamento do objeto

### Requirement: Marcadores tracejados de início e fim aparecem só para o objeto de transparência selecionado
Quando um `ObjetoTransparencia` está selecionado no editor e tem `inicioTransparencia` ou `fimTransparencia` diferente de `0`, o sistema SHALL desenhar, sobre a pista, um traço tracejado perpendicular à direção local da pista em cada um dos dois nós (início e fim), cobrindo a largura da pista naquele ponto — em magenta para o nó de início e em verde para o nó de fim. Nenhum marcador SHALL ser desenhado quando o objeto não estiver selecionado, ou quando `inicioTransparencia == 0 && fimTransparencia == 0`.

#### Scenario: Selecionar um objeto de transparência com intervalo mostra os marcadores
- **WHEN** o usuário seleciona, na lista de objetos do editor, um `ObjetoTransparencia` com `inicioTransparencia` e `fimTransparencia` diferentes de zero
- **THEN** o editor desenha um traço tracejado magenta perpendicular à pista no nó de `inicioTransparencia`, e um traço tracejado verde perpendicular à pista no nó de `fimTransparencia`

#### Scenario: Desselecionar o objeto remove os marcadores
- **WHEN** o usuário seleciona um objeto diferente (ou nenhum objeto), deixando de ter o `ObjetoTransparencia` como selecionado
- **THEN** os traços tracejados de início/fim desse objeto deixam de ser desenhados

#### Scenario: Objeto sem intervalo configurado não mostra marcadores mesmo selecionado
- **WHEN** o usuário seleciona um `ObjetoTransparencia` cujos `inicioTransparencia` e `fimTransparencia` são ambos `0`
- **THEN** nenhum traço tracejado de início/fim é desenhado para esse objeto
