# editor-lista-objetos-filtro-tipo

## Purpose

Unificar, no editor de circuitos, a exibição dos objetos de cenário (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`, `ObjetoLivre`) e dos objetos de função (`ObjetoEscapada`, `ObjetoTransparencia`) em uma única lista visual, com um painel de filtro por tipo (checkboxes dinâmicos conforme os tipos presentes no circuito, mais um modo "Somente Selecionado"), sem alterar onde cada objeto é persistido no `Circuito` (`objetos`/`objetosCenario`).

## Requirements

### Requirement: Lista única de objetos no editor
O editor de circuitos SHALL exibir os objetos de cenário (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`, `ObjetoLivre`) e os objetos de função (`ObjetoEscapada`, `ObjetoTransparencia`) em uma única lista, em vez de duas listas separadas por um `JSplitPane`. Ao reordenar (Cima/Baixo/Primeiro/Ultimo) ou remover um ou mais itens dessa lista única, o editor SHALL continuar gravando cada objeto na coleção original do `Circuito` correspondente ao seu tipo (`circuito.getObjetos()` para objetos de função, `circuito.getObjetosCenario()` para objetos de cenário), preservando a ordem relativa dentro de cada coleção conforme a ordem em que os itens daquele tipo aparecem na lista única. O botão "Remover" SHALL remover **todos** os objetos atualmente selecionados de uma vez quando há seleção múltipla, não apenas um.

#### Scenario: Objetos de cenário e de função aparecem juntos
- **WHEN** o circuito carregado no editor tem pelo menos um objeto de cenário e um objeto de função
- **THEN** ambos aparecem na mesma lista, sem divisão visual em duas listas separadas

#### Scenario: Reordenar preserva a coleção de origem de cada objeto
- **WHEN** o usuário usa Cima/Baixo/Primeiro/Ultimo para mover um objeto de função para uma posição entre objetos de cenário na lista única
- **THEN** o objeto de função continua em `circuito.getObjetos()` (não migra para `circuito.getObjetosCenario()`), e sua posição relativa dentro de `circuito.getObjetos()` reflete a nova posição relativa entre os demais objetos de função na lista única

#### Scenario: Remover um objeto da lista única remove da coleção correta
- **WHEN** o usuário seleciona um objeto de cenário na lista única e clica em "Remover"
- **THEN** o objeto é removido de `circuito.getObjetosCenario()`, e a lista única deixa de exibi-lo

#### Scenario: Remover vários objetos selecionados de uma vez remove todos eles
- **WHEN** o usuário seleciona vários objetos na lista única (seleção múltipla, misturando objetos de cenário e de função) e clica em "Remover"
- **THEN** todos os objetos selecionados são removidos das coleções do `Circuito` correspondentes ao seu tipo, e a lista única deixa de exibir qualquer um deles, mantendo os objetos não selecionados

### Requirement: Painel de filtro por tipo de objeto
Abaixo da lista única de objetos, o editor SHALL exibir um painel de filtro dividido em dois grupos separados visualmente por uma linha divisória: um grupo fixo com três checkboxes mutuamente exclusivos ("Mostrar Todos", "Mostrar Nenhum" e "Somente Selecionado"), e um grupo com um checkbox por tipo de objeto atualmente presente no circuito.

#### Scenario: Painel de filtro aparece abaixo da lista
- **WHEN** o editor de circuitos exibe a seção de objetos
- **THEN** o painel de filtro aparece abaixo da lista única de objetos, com o grupo "Mostrar Todos"/"Mostrar Nenhum"/"Somente Selecionado" seguido de uma linha divisória e do grupo de checkboxes de tipo

#### Scenario: "Mostrar Todos" marca todos os checkboxes de tipo
- **WHEN** o usuário marca o checkbox "Mostrar Todos"
- **THEN** todos os checkboxes de tipo do segundo grupo ficam marcados, todos os objetos do circuito voltam a aparecer na lista e no desenho do circuito, e "Mostrar Nenhum"/"Somente Selecionado" ficam desmarcados

#### Scenario: "Mostrar Nenhum" desmarca todos os checkboxes de tipo
- **WHEN** o usuário marca o checkbox "Mostrar Nenhum"
- **THEN** todos os checkboxes de tipo do segundo grupo ficam desmarcados, nenhum objeto do circuito aparece na lista ou no desenho do circuito, e "Mostrar Todos"/"Somente Selecionado" ficam desmarcados

### Requirement: Checkboxes de tipo dinâmicos conforme os tipos presentes no circuito
O segundo grupo do painel de filtro SHALL exibir exatamente um checkbox para cada tipo de objeto (`TipoObjetoPista`) que tenha pelo menos um exemplar presente no circuito carregado, e nenhum checkbox para tipos sem nenhum exemplar presente. Esse conjunto de checkboxes SHALL ser recalculado sempre que um objeto é criado ou removido no editor.

#### Scenario: Criar o primeiro objeto de um tipo adiciona seu checkbox
- **WHEN** o usuário cria o primeiro `ObjetoPneus` de um circuito que ainda não tinha nenhum
- **THEN** um checkbox para o tipo "Pneus" passa a aparecer no segundo grupo do painel de filtro, já marcado (visível)

#### Scenario: Remover o último objeto de um tipo remove seu checkbox
- **WHEN** o usuário remove o único `ObjetoTransparencia` presente no circuito
- **THEN** o checkbox para o tipo "Transparência" deixa de aparecer no segundo grupo do painel de filtro

#### Scenario: Circuito sem nenhum objeto não exibe nenhum checkbox de tipo
- **WHEN** o circuito carregado no editor não tem nenhum objeto
- **THEN** o segundo grupo do painel de filtro não exibe nenhum checkbox de tipo, mantendo apenas "Mostrar Todos"/"Mostrar Nenhum"

### Requirement: Filtro por tipo afeta a lista e o desenho do circuito
Desmarcar o checkbox de um tipo SHALL esconder todos os objetos daquele tipo tanto na lista única do editor quanto no desenho do circuito (canvas), incluindo torná-los não selecionáveis por clique direto no canvas enquanto escondidos. Esse filtro por tipo SHALL ser independente e cumulativo com o checkbox global "Objetos" já existente (`desenhaObjetosDesenho`), que continua controlando o desenho de todos os objetos de cenário de uma vez, sem alteração de comportamento.

#### Scenario: Desmarcar um tipo esconde da lista
- **WHEN** o usuário desmarca o checkbox do tipo "Guard Rails" e o circuito tem objetos desse tipo
- **THEN** nenhum `ObjetoGuardRails` aparece na lista única enquanto o checkbox estiver desmarcado

#### Scenario: Desmarcar um tipo esconde do desenho do circuito
- **WHEN** o usuário desmarca o checkbox do tipo "Guard Rails" e o circuito tem objetos desse tipo
- **THEN** nenhum `ObjetoGuardRails` é desenhado no canvas do editor enquanto o checkbox estiver desmarcado

#### Scenario: Objeto escondido pelo filtro não é selecionável por clique no canvas
- **WHEN** o usuário clica no canvas exatamente sobre a posição de um objeto cujo tipo está desmarcado no filtro
- **THEN** nenhum objeto é selecionado por esse clique

#### Scenario: Filtro por tipo e checkbox global "Objetos" são cumulativos
- **WHEN** o checkbox global "Objetos" está desmarcado (nenhum objeto de cenário é desenhado) e o usuário também desmarca o checkbox de um tipo de objeto de função
- **THEN** os objetos de cenário continuam escondidos pelo checkbox global, os objetos de função do tipo desmarcado também ficam escondidos pelo filtro por tipo, e os dois controles continuam funcionando de forma independente quando reativados separadamente

#### Scenario: Objeto escondido pelo filtro continua existindo no circuito
- **WHEN** o usuário desmarca o checkbox de um tipo, escondendo objetos desse tipo da lista e do canvas
- **THEN** esses objetos continuam presentes em `circuito.getObjetos()`/`circuito.getObjetosCenario()`, não são removidos do circuito, e voltam a aparecer na lista e no canvas assim que o checkbox do tipo é marcado novamente

### Requirement: Modo "Somente Selecionado" foca em um único objeto
O checkbox "Somente Selecionado" do grupo 1 SHALL ficar desabilitado quando não houver nenhum objeto selecionado na lista única. Quando marcado (com uma seleção ativa), a lista única e o desenho do circuito SHALL exibir exclusivamente o objeto que estava selecionado no momento em que o checkbox foi marcado, independente do estado dos checkboxes de tipo do segundo grupo.

#### Scenario: Checkbox desabilitado sem seleção
- **WHEN** nenhum objeto está selecionado na lista única
- **THEN** o checkbox "Somente Selecionado" aparece desabilitado, impedindo que seja marcado

#### Scenario: Marcar com um objeto selecionado mostra só ele
- **WHEN** o usuário seleciona um objeto na lista única e marca o checkbox "Somente Selecionado"
- **THEN** a lista única passa a exibir apenas esse objeto, e o desenho do circuito passa a mostrar apenas esse objeto, mesmo que outros tipos estejam marcados como visíveis no segundo grupo

#### Scenario: Desmarcar restaura o filtro por tipo anterior
- **WHEN** o usuário desmarca manualmente o checkbox "Somente Selecionado"
- **THEN** a lista única e o desenho do circuito voltam a refletir exatamente o estado dos checkboxes de tipo do segundo grupo, como estava antes de "Somente Selecionado" ser marcado

#### Scenario: Remover o objeto em foco encerra o modo automaticamente
- **WHEN** o objeto atualmente em foco pelo modo "Somente Selecionado" é removido do circuito
- **THEN** o modo "Somente Selecionado" é desmarcado automaticamente, e a lista única volta a exibir os objetos conforme o filtro por tipo do segundo grupo

#### Scenario: Checkboxes de tipo ficam desabilitados durante o modo
- **WHEN** o modo "Somente Selecionado" está ativo
- **THEN** os checkboxes de tipo do segundo grupo (e "Mostrar Todos"/"Mostrar Nenhum") ficam desabilitados, sem perder seus estados marcados/desmarcados anteriores

### Requirement: Tecla Delete remove o objeto selecionado na lista única
Com o foco na lista única de objetos (`FormularioListaObjetos`) e um ou mais itens selecionados, pressionar a tecla Delete SHALL remover todos os itens selecionados, com o mesmo efeito do botão "Remover" já existente: cada objeto é removido da coleção do `Circuito` correspondente ao seu tipo (`circuito.getObjetos()` ou `circuito.getObjetosCenario()`) e deixa de aparecer na lista e no desenho do circuito.

#### Scenario: Delete com item selecionado remove o objeto
- **WHEN** o usuário seleciona um objeto na lista única de objetos e pressiona a tecla Delete com o foco na lista
- **THEN** o objeto selecionado é removido da coleção correspondente do `Circuito`, desaparece da lista única e deixa de ser desenhado no canvas

#### Scenario: Delete com múltiplos itens selecionados remove todos eles
- **WHEN** o usuário seleciona vários objetos na lista única (seleção múltipla) e pressiona a tecla Delete com o foco na lista
- **THEN** todos os objetos selecionados são removidos das coleções do `Circuito` correspondentes ao seu tipo, e a lista única deixa de exibir qualquer um deles

#### Scenario: Delete sem seleção não tem efeito
- **WHEN** o usuário pressiona a tecla Delete com o foco na lista única de objetos e nenhum item selecionado
- **THEN** nenhum objeto é removido e a lista permanece inalterada

#### Scenario: Delete na lista funciona mesmo sem seleção prévia no canvas
- **WHEN** o usuário seleciona um objeto exclusivamente através da lista única (sem clicar nesse objeto no canvas) e pressiona Delete com o foco na lista
- **THEN** o objeto é removido normalmente, independente do valor atual do campo de seleção usado pelo clique no canvas

### Requirement: Contorno de seleção no canvas acompanha o ângulo do objeto
Qualquer contorno de seleção desenhado no canvas ao redor de um objeto (seleção via lista única ou seleção do objeto "ativo" que responde aos atalhos de teclado) SHALL acompanhar o ângulo (`getAngulo()`) desse objeto, rotacionando em torno do centro da sua área visual, em vez de permanecer sempre alinhado aos eixos.

#### Scenario: Objeto sem rotação tem contorno alinhado aos eixos
- **WHEN** o usuário seleciona na lista única um objeto com ângulo 0
- **THEN** o contorno desenhado ao redor do objeto é um retângulo alinhado aos eixos, coincidindo com a área visual do objeto

#### Scenario: Objeto rotacionado tem contorno rotacionado junto
- **WHEN** o usuário seleciona na lista única um objeto com um ângulo diferente de 0
- **THEN** o contorno desenhado gira junto com o objeto, em vez de permanecer alinhado aos eixos originais da área bruta

### Requirement: Seleção múltipla marca cada objeto selecionado com uma cor distinta da seleção única
Quando mais de um objeto está selecionado de uma vez na lista única (seleção múltipla), o editor SHALL desenhar um contorno de seleção ao redor de cada objeto selecionado, numa cor diferente da usada para seleção única — a cor de seleção única SHALL ficar reservada exclusivamente para quando há exatamente um objeto selecionado.

#### Scenario: Seleção múltipla marca todos os objetos selecionados
- **WHEN** o usuário seleciona vários objetos de uma vez na lista única (seleção múltipla)
- **THEN** cada um dos objetos selecionados aparece com um contorno de seleção no canvas

#### Scenario: Seleção múltipla não usa a cor de seleção única
- **WHEN** há mais de um objeto selecionado de uma vez na lista única
- **THEN** nenhum dos contornos desenhados usa a cor reservada para seleção única

#### Scenario: Seleção única continua usando sua cor própria
- **WHEN** exatamente um objeto está selecionado na lista única
- **THEN** o contorno desenhado ao redor desse objeto usa a cor de seleção única, não a cor de seleção múltipla

### Requirement: Etiqueta com o número do objeto aparece em qualquer objeto selecionado
Toda vez que um objeto está marcado como selecionado no canvas (seleção única ou múltipla), o editor SHALL exibir, ao lado dele, a etiqueta com seu número/posição na lista — a etiqueta SHALL NOT ficar restrita à seleção única.

#### Scenario: Seleção única mostra a etiqueta
- **WHEN** exatamente um objeto está selecionado na lista única
- **THEN** a etiqueta com o número desse objeto aparece ao lado dele no canvas

#### Scenario: Seleção múltipla mostra a etiqueta em cada objeto selecionado
- **WHEN** vários objetos estão selecionados de uma vez na lista única
- **THEN** a etiqueta com o número de cada um deles aparece ao lado do respectivo objeto no canvas

### Requirement: Seleção do canvas e da lista única são sempre a mesma seleção
O editor SHALL manter uma única seleção corrente, refletida tanto no destaque visual do canvas quanto na seleção da lista única — não SHALL existir um objeto "ativo" do canvas que continue marcado depois que uma seleção diferente for feita pela lista (ou vice-versa).

#### Scenario: Selecionar um objeto diferente na lista atualiza o objeto ativo do canvas
- **WHEN** o usuário seleciona um objeto diretamente no canvas, e em seguida seleciona um objeto diferente pela lista única
- **THEN** o objeto selecionado anteriormente no canvas deixa de responder aos atalhos de teclado de edição, e deixa de aparecer marcado — apenas o objeto selecionado pela lista fica marcado

#### Scenario: Selecionar múltiplos objetos na lista desativa a edição por atalho de teclado
- **WHEN** o usuário seleciona vários objetos de uma vez na lista única
- **THEN** nenhum objeto único responde aos atalhos de teclado de edição de objeto (ex.: Z/X, setas, PageUp/PageDown, Delete do canvas), até que a seleção volte a ser de um único objeto

#### Scenario: Limpar a seleção da lista limpa o objeto ativo do canvas
- **WHEN** o usuário limpa a seleção da lista única (nenhum item selecionado)
- **THEN** nenhum objeto responde aos atalhos de teclado de edição de objeto

### Requirement: Clique no canvas nunca cria nem mantém seleção múltipla
Um clique num objeto do canvas SHALL sempre resultar em seleção única (apenas esse objeto), mesmo que uma seleção múltipla anterior (feita pela lista única) já incluísse esse mesmo objeto — seleção múltipla só pode ser criada ou mantida pela lista única, nunca pelo canvas.

#### Scenario: Clicar no canvas colapsa uma seleção múltipla existente
- **WHEN** o usuário tem vários objetos selecionados de uma vez pela lista única, e clica no canvas em um dos objetos já selecionados
- **THEN** a seleção passa a conter apenas o objeto clicado, e os demais deixam de estar selecionados

#### Scenario: Clicar no canvas em outro objeto também colapsa a seleção múltipla
- **WHEN** o usuário tem vários objetos selecionados de uma vez pela lista única, e clica no canvas em um objeto que não fazia parte dessa seleção
- **THEN** a seleção passa a conter apenas o objeto clicado
