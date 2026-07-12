## ADDED Requirements

### Requirement: Listas de nós da pista e do box ficam dentro de um JSplitPane redimensionável
O editor de circuitos (`gerarListsNosPistaBox()`) SHALL exibir a lista de nós da pista e a lista de nós do box dentro de um `JSplitPane` vertical (`JSplitPane.VERTICAL_SPLIT`), em vez do `GridLayout(2, 1)` de altura fixa usado hoje, no mesmo padrão já aplicado entre a lista de objetos de desenho e a lista de objetos de função (`gerarSecaoObjetos()`). O `JSplitPane` SHALL ter um divisor arrastável (`setOneTouchExpandable(true)`) e SHALL NOT impor altura fixa a nenhuma das duas listas.

#### Scenario: Usuário redimensiona a lista de nós da pista arrastando o divisor
- **WHEN** o usuário arrasta o divisor entre a lista de nós da pista e a lista de nós do box para baixo ou para cima
- **THEN** a altura de cada lista muda proporcionalmente ao arrasto, e nenhuma das duas volta a um tamanho fixo depois do ajuste

#### Scenario: Layout inicial mantém proporção equivalente ao comportamento atual
- **WHEN** o editor é aberto com um circuito carregado, antes de qualquer ajuste manual do divisor
- **THEN** a lista de nós da pista e a lista de nós do box aparecem com alturas comparáveis à divisão 50/50 atual, mas dentro de um componente redimensionável

#### Scenario: Redimensionar não afeta o conteúdo ou seleção das listas
- **WHEN** o usuário redimensiona as listas de nós arrastando o divisor
- **THEN** os itens listados, a seleção atual e o comportamento de clique/seleção de cada `JList` (`pistaJList`/`boxJList`) permanecem inalterados
