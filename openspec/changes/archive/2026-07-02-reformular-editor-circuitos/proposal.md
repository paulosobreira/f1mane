## Why

O editor de circuitos (`EditorCircuitos`/`MainPanelEditor`) hoje espalha as ações de edição de nós e de objetos de pista em painéis distintos (nós a oeste, dois painéis de objetos empilhados a leste, botões soltos ao norte), seleciona circuitos existentes por `JFileChooser` bruto sem nenhuma relação com `circuitos.properties`, e expõe todas as ações por um `JMenuBar` com itens de debug/logs misturados aos de edição. Isso torna o fluxo de criar/editar uma pista mais lento e inconsistente com o padrão de navegação por temporada já usado em `EditorCoresCarros`. Além disso, não existe hoje nenhuma forma de manter um circuito em desenvolvimento no repositório sem que ele apareça imediatamente como jogável.

## What Changes

- O editor de circuitos passa a carregar/selecionar circuitos existentes com navegação Anterior/Próximo sobre a lista vinda de `circuitos.properties` (mesmo padrão de `EditorCoresCarros.popularTemporadas()`/`navegarTemporada()`), em vez do `JFileChooser` atual usado por `MainPanelEditor.editar()`.
- As listas de nós (`pistaJList`/`boxJList`) e as listas de objetos (`objetos`/`objetosCenario`, hoje em dois `FormularioListaObjetos` separados a leste) passam a ficar em um único `JSplitPane` lateral, com uma seção para nós e outra para objetos de pista.
- As ações relativas a nós (inserir, apagar, apagar último) são agrupadas na seção de nós do split pane; as ações relativas a objetos de pista (criar, remover, reordenar, editar) são agrupadas na seção de objetos do split pane — em vez de ficarem em botões soltos no painel norte (`buttonsPanel`) e nos painéis de lista.
- `Circuito` ganha uma propriedade booleana `ativo`, com valor padrão `false`. **BREAKING**: circuitos sem essa propriedade explicitamente marcada como `true` deixam de aparecer nos seletores de circuito do jogo (solo e multiplayer), mesmo continuando editáveis no editor.
- O `JMenuBar` de `EditorCircuitos` é removido. As únicas ações de topo passam a ser botões: "Salvar Pista Atual" e "Criar Nova". Itens de debug/logs/sobre-o-autor saem do menu do editor (deixam de existir nesta tela).

## Capabilities

### New Capabilities
- `circuito-ativo`: adiciona a propriedade `ativo` (boolean, default `false`) a `Circuito`, persistida via `XMLEncoder`/`XMLDecoder` como as demais propriedades booleanas (`noite`, `usaBkg`), e filtra circuitos inativos dos seletores em jogo (solo `GerenciadorVisual`, cliente multiplayer `PainelEntradaCliente`, endpoint REST `LetsRace.circuitos()`), mantendo-os visíveis e editáveis no editor de circuitos.

### Modified Capabilities
- `dev-editor-tools`: a seleção de circuito existente para edição deixa de usar `JFileChooser` e passa a usar navegação Anterior/Próximo sobre `circuitos.properties`, mesmo padrão de `EditorCoresCarros`; o `JMenuBar` de `EditorCircuitos` é removido em favor de botões "Salvar Pista Atual"/"Criar Nova".
- `objetos-cenario-circuito`: os painéis de lista de nós e de objetos de pista (hoje em `BorderLayout` WEST/EAST separados) passam a viver em um único `JSplitPane` lateral, com as ações de nó agrupadas na seção de nós e as ações de objeto agrupadas na seção de objetos.

## Impact

- `br.f1mane.editor.EditorCircuitos`: remoção do `JMenuBar` (`gerarMenusEditor`), adição dos botões "Salvar Pista Atual"/"Criar Nova".
- `br.f1mane.editor.MainPanelEditor`: maior reformulação — substitui `gerarListsNosPistaBox()` (WEST) e `listasObjetosPanel` (EAST) por um único `JSplitPane`; reagrupa botões de `buttonsPanel`/`gerarBotoesTracado()` nas seções correspondentes; substitui o `JFileChooser` de `editar()` por navegação Anterior/Próximo lendo `circuitos.properties`; `novo()` e `salvarPista()` ajustados para o novo fluxo de botões.
- `br.f1mane.editor.FormularioListaObjetos`: reaproveitado dentro da nova seção de objetos do split pane (sem mudança de contrato).
- `br.f1mane.entidades.Circuito`: novo campo `ativo` (boolean, default `false`) com `isAtivo()`/`setAtivo(boolean)`.
- `src/main/resources/properties/circuitos.properties`: passa a ser a fonte usada pela navegação Anterior/Próximo do editor (leitura), além do uso já existente em runtime.
- `src/main/resources/circuitos/*.xml`: **migração necessária** — todos os 37 circuitos hoje jogáveis precisam ganhar `ativo=true` explicitamente, senão desaparecem do jogo com o novo default `false`.
- `br.f1mane.controles.ControleRecursos` / `br.f1mane.recursos.CarregadorRecursos` (`carregarCircuitosDefaults`): precisam decodificar `isAtivo()` de cada `Circuito` para filtrar a lista exposta ao REST e aos seletores.
- `br.f1mane.visao.GerenciadorVisual` (`gerarSeletorCircuito`) e cliente multiplayer `PainelEntradaCliente`: filtram circuitos inativos do combo box exibido ao jogador.
- `br.f1mane.servidor.rest.LetsRace` (`GET /circuitos`): passa a retornar apenas circuitos ativos.
