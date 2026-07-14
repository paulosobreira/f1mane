## Why

No editor de circuitos, um `ObjetoEscapada` já criado não podia ser selecionado nem arrastado clicando sobre o traçado desenhado (`ObjetoEscapada` não sobrescrevia `obterAreaVisual()`, então a área clicável era só o retângulo bruto dos vértices originais). A investigação inicial corrigiu esse hit-testing, mas revelou, junto com o usuário, que o comportamento realmente desejado para uma `ObjetoEscapada` é o oposto do que se tentou primeiro consertar: depois de criada, ela não deveria ter NENHUMA interação por clique no canvas (nem seleção, nem arraste, nem edição de pontos, nem diálogo de propriedades) — a única operação suportada deveria ser a remoção, pela lista de objetos do circuito. Interações diretas no canvas para um objeto cuja geometria já está ancorada a nós reais do traçado (entrada/saída validadas) se mostraram propensas a erro (arrastar sem querer, reposicionar sem revalidar contra o traçado) e desnecessárias, já que reposicionar corretamente significa recriar o objeto do zero mesmo.

## What Changes

- `ObjetoEscapada` passa a sobrescrever `obterAreaVisual()`, expandindo o retângulo bruto de `obterArea()` pela espessura de `largura` (mesmo padrão de `ObjetoArquibancada#obterAreaVisual()`) — usado só para o destaque visual de seleção quando a escapada está selecionada pela lista de objetos.
- **BREAKING (comportamento do editor)**: o ponto único de hit-testing por clique no canvas (`encontraObjetoPistaNaLista`) passa a excluir toda `ObjetoEscapada` — depois de criada, ela não pode mais ser selecionada, arrastada, nem abrir menu de contexto ou diálogo de propriedades pelo canvas. A única interação suportada depois da criação é a remoção pela lista de objetos (botão "Remover" ou tecla Delete).
- O modo "Editar Pontos" de uma `ObjetoEscapada` já criada foi removido por completo (mecanismo interno + botão no menu de contexto + teste dedicado) — seu único ponto de entrada na UI (o menu de contexto) deixou de ser alcançável, então o código ficaria morto se mantido.
- Nenhuma mudança na criação do objeto (clique-a-clique com validação de entrada/saída contra o traçado) nem no consumo em corrida.

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `objeto-escapada-tracado`: adiciona o requisito de que uma `ObjetoEscapada` já criada não tem nenhuma interação por clique no canvas (só remoção pela lista de objetos), e remove o requisito de edição de pontos de uma escapada existente (capacidade descontinuada).

## Impact

- Código: `src/main/java/br/f1mane/entidades/ObjetoEscapada.java` (`obterAreaVisual()`, mantido — usado pelo destaque de seleção via lista); `src/main/java/br/f1mane/editor/MainPanelEditor.java` (`encontraObjetoPistaNaLista` exclui `ObjetoEscapada`; remoção do mecanismo de edição de pontos: campos, métodos de arraste de ponto, botão "Editar Pontos" do menu de contexto); `src/main/java/br/f1mane/editor/FormularioListaObjetos.java` (duplo clique na lista não abre diálogo de propriedades para escapada).
- Nenhuma mudança de schema/persistência (XML), API pública de criação, ou comportamento de corrida.
- Testes: `MainPanelEditorEscapadaSelecaoTest` cobre que nenhuma interação por clique no canvas encontra/seleciona/arrasta uma escapada já criada; teste dedicado de edição de pontos (`MainPanelEditorEscapadaArrastarPontoTest`) removido junto com a capacidade.
