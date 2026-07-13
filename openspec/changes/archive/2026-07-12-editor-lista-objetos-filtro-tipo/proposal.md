## Why

O editor de circuitos hoje mostra os objetos do circuito em duas listas separadas (`formularioListaObjetosDesenho`/`objetosCenario` em cima, `formularioListaObjetosFuncao`/`objetos` embaixo, divididas por um `JSplitPane` 70/30 em `gerarSecaoObjetos()`), sem nenhuma forma de filtrar por tipo — em circuitos com muitos objetos (arquibancadas, construções, guard rails, pneus, escapadas, transparências), encontrar um objeto específico exige rolar duas listas distintas. Unificar numa lista só e adicionar um filtro por tipo torna a navegação mais direta, sem mudar onde cada objeto é persistido no `Circuito`.

## What Changes

- As duas listas de objetos do editor (`formularioListaObjetosDesenho` e `formularioListaObjetosFuncao`) SHALL ser substituídas por uma única lista unificada, exibindo objetos de cenário/desenho e objetos de função juntos, na mesma área antes ocupada pelo `JSplitPane`.
- A lista unificada SHALL continuar escrevendo cada objeto na coleção original do `Circuito` (`objetos` para função, `objetosCenario` para cenário/desenho, conforme `TipoObjetoPista.isCenario()`) — **BREAKING** apenas para o código interno do editor que hoje referencia os dois campos `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` separadamente; nenhuma mudança em persistência XML, `ControleCorrida` ou qualquer consumidor fora do editor.
- Abaixo da lista unificada, um novo painel de filtro por tipo SHALL ser adicionado, com dois grupos separados visualmente por uma linha divisória (`JSeparator`):
  - Grupo 1: três checkboxes mutuamente exclusivos — "Mostrar Todos" e "Mostrar Nenhum" (marcam/desmarcam de uma vez todos os checkboxes de tipo do grupo 2), e "Somente Selecionado" (exibe, na lista e no canvas, só o objeto atualmente selecionado na lista; SHALL ficar desabilitado quando não há objeto selecionado).
  - Grupo 2: um checkbox dinâmico por tipo de objeto (`TipoObjetoPista`) atualmente presente no circuito — aparece quando o primeiro objeto daquele tipo é criado, desaparece quando o último objeto daquele tipo é removido.
- Desmarcar o checkbox de um tipo (ou ativar "Somente Selecionado") SHALL esconder os objetos correspondentes tanto na lista quanto no desenho do circuito (canvas do editor), incluindo torná-los não selecionáveis por clique no canvas enquanto escondidos. Esconder um objeto pelo filtro SHALL NOT removê-lo do circuito — ele continua normalmente em `circuito.getObjetos()`/`circuito.getObjetosCenario()`, só fica oculto na UI do editor enquanto o filtro estiver ativo.
- O checkbox global "Objetos" já existente (`desenhaObjetosDesenho`, liga/desliga desenho de todos os objetos de cenário de uma vez) SHALL continuar funcionando sem alteração, de forma independente e cumulativa com o novo filtro por tipo.
- Aproveitando que `desenhaObjetosNivel()`/`niveisDesenhoOrdenados()` já serão tocados para aplicar o novo filtro, o laço de desenho por nível SHALL parar de recalcular `todosObjetos()` (concatenação de `circuito.getObjetos()` + `circuito.getObjetosCenario()`) uma vez por nível a cada frame — hoje isso aloca e percorre a lista completa de objetos repetidamente (uma vez em `niveisDesenhoOrdenados()`, mais uma vez por nível distinto em `desenhaObjetosNivel()`) a cada `paintComponent()`, redundante com o corte por viewport já existente (`editor-viewport-culling`), que evita o `desenha()` mas não essa realocação. Não foi identificada necessidade de indexação espacial (quadtree/grid) além disso — o corte por viewport já existente é suficiente para a escala atual de objetos por circuito.

## Capabilities

### New Capabilities
- `editor-lista-objetos-filtro-tipo`: lista única de objetos do editor (substituindo as duas listas separadas) com painel de filtro por tipo baseado em checkbox, dinâmico conforme os tipos presentes no circuito, mais o modo "Somente Selecionado", afetando lista e canvas sem remover objetos do circuito.

### Modified Capabilities
- `objetos-cenario-circuito`: a UI do editor para listar/selecionar objetos de cenário deixa de ter uma lista dedicada separada da de objetos de função — passa a compartilhar a lista unificada descrita acima.

## Impact

- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: `gerarSecaoObjetos()` (remove o `JSplitPane` de duas listas, monta lista única + painel de filtro com o novo checkbox "Somente Selecionado"), os ~7 pontos que hoje escolhem entre `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` (criação, remoção, cima/baixo/primeiro/último, copiar/colar cor, seleção via canvas), `desenhaObjetosNivel()`/`niveisDesenhoOrdenados()`/`desenhaListaObjetos()`/`encontraObjetoPistaNaLista()`/`paintComponent()` (novo filtro por tipo e "Somente Selecionado" aplicados antes do desenho/seleção via canvas, mais o ajuste de performance para computar `todosObjetos()` uma única vez por frame).
- `src/main/java/br/f1mane/editor/FormularioListaObjetos.java`: passa a suportar (ou é complementado por) uma fonte de dados unificada que lê de duas coleções do `Circuito` e devolve cada objeto reordenado para a coleção de origem correta ao salvar a ordem.
- `src/main/java/br/f1mane/editor/TipoObjetoPista.java`: novo helper para determinar o `TipoObjetoPista` de uma instância `ObjetoPista` existente (hoje só cria a partir do tipo, não identifica o tipo de um objeto já existente).
- `src/test/java/br/f1mane/editor/`: testes que hoje populam `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` separadamente (`MainPanelEditorCopiarColarCorTest.java` e outros) precisam ser adaptados para a lista única; novos testes cobrindo o painel de filtro (aparição/desaparição dinâmica de checkboxes, efeito na lista e no desenho).
- Nenhum impacto em `Circuito` (modelo de dados), persistência XML, `ControleCorrida`/motor de jogo, ou multiplayer — mudança restrita à camada de UI do editor local.
