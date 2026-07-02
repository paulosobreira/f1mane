## Context

O editor de circuitos (`br.f1mane.editor.EditorCircuitos` + `MainPanelEditor`, ~2126 linhas) hoje:
- Seleciona circuito existente via `JFileChooser` bruto (`MainPanelEditor.editar()`), sem relação com `circuitos.properties`.
- Layout em `BorderLayout`: nós (`pistaJList`/`boxJList`) a WEST via `gerarListsNosPistaBox()`; dois `FormularioListaObjetos` empilhados a EAST (`objetos` e `objetosCenario`, adicionados na mudança recente de objetos de cenário); botões de ação de nó e de objeto soltos em `buttonsPanel`/`gerarBotoesTracado()` no NORTH.
- `JMenuBar` com menus "Editor" (Criar/Editar/Salvar) e "Informações" (logs/debug/sobre).
- `Circuito` persiste via `java.beans.XMLEncoder`/`XMLDecoder` (introspecção JavaBeans — precisa de par `isX()`/`setX()` para cada propriedade booleana; propriedades no valor padrão do construtor não são serializadas).
- A cadeia runtime de exposição de circuitos ao jogador é: `circuitos.properties` → `ControleRecursos.carregarCircuitos()` (cache estático `Map<String,String>` nome→arquivo) → consumida por `GerenciadorVisual.gerarSeletorCircuito()` (combo solo), `PainelEntradaCliente` (combo multiplayer applet) e `CarregadorRecursos.carregarCircuitosDefaults()` → `LetsRace.circuitos()` (REST, já decodifica cada `Circuito` via XML para anexar `probalidadeChuva`).

O padrão de navegação por temporada a ser replicado vive em `EditorCoresCarros`: dois `JButton` (Anterior/Próximo) + `JLabel`, populados por `popularTemporadas()` (lê `properties/temporadas.properties`, com fallback de varredura de diretório) e `navegarTemporada(delta)`/`atualizarNavegacaoTemporada()` que habilitam/desabilitam os botões nas pontas da lista e disparam o carregamento.

## Goals / Non-Goals

**Goals:**
- Substituir o `JFileChooser` de seleção de circuito existente por navegação Anterior/Próximo sobre a lista de `circuitos.properties`, replicando o padrão de `EditorCoresCarros`.
- Consolidar as listas de nós e de objetos em um único `JSplitPane` lateral, com as ações de cada domínio (nó vs. objeto) agrupadas na seção correspondente.
- Remover o `JMenuBar` do editor, substituindo por botões "Salvar Pista Atual" e "Criar Nova".
- Adicionar `ativo` (boolean, default `false`) a `Circuito`, e filtrar circuitos inativos dos seletores em jogo (solo, multiplayer applet, REST), sem impedir que o editor continue exibindo/editando-os.

**Non-Goals:**
- Não reescrever o mecanismo de persistência (continua `XMLEncoder`/`XMLDecoder`, sem migração para JSON/DB).
- Não alterar `circuitos.properties` para incluir uma terceira coluna com o status ativo — o status vive exclusivamente no XML do `Circuito` (ver Decisão 3).
- Não mexer no fluxo de teste de pista (`TestePista`), no menu de "Informações" enquanto funcionalidade de debug (essas ações somem da UI do editor, mas os atalhos/flags `Global.DEBUG` etc. continuam existindo no código, só não ficam mais acessíveis por esse menu específico).
- Não alterar o formato do `FormularioListaObjetos` em si (Cima/Baixo/Primeiro/Ultimo/Editar/Remover) — ele é reaproveitado dentro da nova seção de objetos do split pane sem mudança de contrato.

## Decisions

### 1. Navegação Anterior/Próximo lendo `circuitos.properties`, com fallback de varredura de diretório
Réplica direta do padrão já validado em `EditorCoresCarros.popularTemporadas()`: fonte primária é `properties/circuitos.properties` (já é a fonte de verdade em runtime via `ControleRecursos.carregarCircuitos()`), com fallback de varredura de `src/main/resources/circuitos/*.xml` caso a properties não possa ser lida. A troca de circuito chama `CarregadorRecursos.carregarCircuito(nomeArquivoXml)` (mesmo método já usado por `editar()` hoje) em vez de abrir um `JFileChooser`.
**Alternativa considerada**: manter `JFileChooser` e apenas adicionar botões Anterior/Próximo por cima — rejeitada porque o pedido explícito é usar exatamente o padrão do editor de carros, e manter o file chooser paralelo criaria duas formas divergentes de abrir o mesmo arquivo.

### 2. Layout: um único `JSplitPane` lateral com duas seções (nós / objetos)
Substitui o atual WEST (nós) + EAST (dois painéis de objeto empilhados) por um só `JSplitPane` (orientação vertical, `JSplitPane.VERTICAL_SPLIT`) posicionado em um único lado (EAST, mantendo o canvas maior à esquerda). A seção superior contém as duas `JList` de nós (pista/box) mais os botões de ação de nó (inserir via seleção de rádio + clique já existente, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada"). A seção inferior contém os dois `FormularioListaObjetos` (objetos/objetosCenario) mais o botão "Criar Objeto".
**Alternativa considerada**: `JSplitPane` horizontal com nós à esquerda e objetos à direita (dois lados) — rejeitada porque o pedido é consolidar em "apenas uma lateral".
**Alternativa considerada**: um único `JTabbedPane` em vez de `JSplitPane` — rejeitada porque o pedido específico menciona "split panel", e um split mantém nós e objetos visíveis simultaneamente (importante já que arrastar/clicar objetos no canvas precisa de visibilidade constante da lista).

### 3. `ativo` como propriedade do `Circuito` (XML), não coluna em `circuitos.properties`
`ativo` fica exclusivamente no XML do circuito (`Circuito.isAtivo()/setAtivo()`, JavaBeans), não em `circuitos.properties`. Motivo: `circuitos.properties` é uma lista estática mantida a mão; colocar o status lá duplicaria a fonte de verdade com o editor (que só escreve o XML). O custo é que os pontos de filtragem em runtime (`CarregadorRecursos.carregarCircuitosDefaults()`, `GerenciadorVisual.gerarSeletorCircuito()`, `PainelEntradaCliente`) precisam decodificar o XML de cada circuito para checar `isAtivo()`.
**Mitigação de custo**: `carregarCircuitosDefaults()` já decodifica cada `Circuito` (para ler `probalidadeChuva`), então o filtro ali é gratuito. Para os dois combo boxes Swing (`GerenciadorVisual`, `PainelEntradaCliente`), que hoje só usam o `Map<String,String>` de `ControleRecursos.carregarCircuitos()` sem decodificar XML, a decodificação adicional (uma vez, ao montar o combo, dezenas de arquivos) é aceitável — não é um caminho quente de execução.
**Alternativa considerada**: adicionar 3ª coluna `ativo` em `circuitos.properties` e evitar decodificar XML nos combos — rejeitada porque o pedido do usuário é uma propriedade do circuito, e manter duas fontes de verdade (properties + XML) para o mesmo dado convida a divergência (alguém edita o XML e esquece de atualizar o properties).

### 4. Default `false` exige migração explícita dos 37 circuitos existentes
Como o construtor padrão de `Circuito` passa a ter `ativo=false`, e `XMLEncoder` só grava propriedades que divergem do padrão do construtor, todo circuito já existente em `src/main/resources/circuitos/*.xml` precisa ganhar `<void property="ativo"><boolean>true</boolean></void>` explicitamente — senão, no primeiro carregamento pós-mudança, todos os circuitos hoje jogáveis desaparecem dos seletores. Isso é tratado como passo de migração dedicado em `tasks.md` (script ou edição em lote dos XMLs), executado antes/junto da mudança de filtragem entrar em vigor.

## Risks / Trade-offs

- [Risco] Migração incompleta dos 37 XMLs deixa circuitos jogáveis hoje invisíveis após a mudança → Mitigação: task dedicada de migração em lote com verificação (contar quantos `*_mro.xml` existem vs. quantos têm `ativo=true` após a edição, antes de considerar a mudança concluída).
- [Risco] Decodificar XML de todo circuito ao montar `GerenciadorVisual.gerarSeletorCircuito()`/`PainelEntradaCliente` adiciona I/O síncrono na montagem da tela → Mitigação: é uma operação de UI de configuração inicial (não por frame), sobre dezenas de arquivos pequenos; aceitável sem cache adicional nesta mudança.
- [Trade-off] Remover o menu "Informações" (logs/debug/sobre) do editor reduz descoberta dessas funções para quem não conhece atalhos/flags — aceito porque o pedido explícito do usuário é reduzir a UI a apenas salvar/criar.
- [Risco] `JSplitPane` único lateral reduz a área visível simultânea de nós+objetos comparado ao layout atual de dois lados — Mitigação: divisor arrastável (`JSplitPane` permite redimensionar em tempo de uso) e proporção inicial razoável (ex.: 50/50) definida no `tasks.md`.

## Migration Plan

1. Adicionar `ativo`/`isAtivo`/`setAtivo` a `Circuito` (default `false`) sem ainda ativar filtragem em runtime.
2. Editar em lote os 37 XMLs existentes em `src/main/resources/circuitos/` para `ativo=true` (script ou edição direta), validando que a contagem bate.
3. Ativar a filtragem por `isAtivo()` em `CarregadorRecursos.carregarCircuitosDefaults()`, `GerenciadorVisual.gerarSeletorCircuito()`, `PainelEntradaCliente` e `LetsRace.circuitos()`.
4. Reformular a UI do editor (split pane, navegação Anterior/Próximo, remoção do menu, botões Salvar/Criar) — não depende estritamente da ordem acima, mas faz sentido só entregar depois que `ativo` já existe no modelo, para o editor já poder expor um controle de "ativo" ao usuário do editor (checkbox) se necessário.

Rollback: reverter a filtragem (passos 3) é suficiente para restaurar o comportamento atual sem depender de reverter a UI do editor; os dois são independentes o bastante para reverts parciais.

## Open Questions

- O editor precisa expor um checkbox visível para alternar `ativo` diretamente na tela principal, ou isso fica apenas acessível via edição futura (formulário)? Assumido: sim, um checkbox simples perto dos botões Salvar/Criar, já que sem isso não há como o usuário do editor tornar um circuito jogável pela UI.
- Qual o nome exibido nos botões de topo — "Salvar Pista Atual"/"Criar Nova" (conforme pedido) — precisa de confirmação visual/rótulo final, mas segue o texto literal do pedido para esta proposta.
