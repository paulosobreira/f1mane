## Context

`MainPanelEditor.gerarSecaoObjetos()` (`src/main/java/br/f1mane/editor/MainPanelEditor.java:637-664`) hoje monta dois `FormularioListaObjetos` (`src/main/java/br/f1mane/editor/FormularioListaObjetos.java`) independentes, cada um espelhando uma coleção diferente do `Circuito`:

```java
formularioListaObjetosDesenho = new FormularioListaObjetos(this, Circuito::getObjetosCenario, true);
formularioListaObjetosFuncao = new FormularioListaObjetos(this, Circuito::getObjetos);
...
JSplitPane splitListas = new JSplitPane(JSplitPane.VERTICAL_SPLIT, listaDesenho, listaFuncao);
splitListas.setResizeWeight(0.7);
```

Cada `FormularioListaObjetos` é autossuficiente: internamente tem um `DefaultListModel`, um `JList`, botões Cima/Baixo/(Primeiro/Ultimo)/Remover, e dois métodos-chave (`FormularioListaObjetos.java:234-253`):
- `listarObjetos()`: limpa e repopula o `DefaultListModel` a partir de `listaAccessor.apply(circuito)` (a coleção do `Circuito` que esse formulário espelha).
- `atualizarCircuito()`: limpa a coleção do `Circuito` e a repopula a partir do `DefaultListModel` atual — chamado após qualquer reordenação/remoção pelos próprios botões do formulário.

Como cada instância só conhece **uma** coleção (`objetos` OU `objetosCenario`), esse par read/write funciona porque não há ambiguidade sobre onde escrever de volta. Cerca de sete pontos em `MainPanelEditor` (criação por clique, `apagarObjetoSelecionado()`, `copiarCorObjetoSelecionado()`/`colarCorObjetosSelecionados()`, seleção via canvas) hoje escolhem entre `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` (ou entre `circuito.getObjetos()`/`circuito.getObjetosCenario()`) com base no tipo do objeto, usando `TipoObjetoPista.isCenario()` ou `instanceof`.

`TipoObjetoPista` (`src/main/java/br/f1mane/editor/TipoObjetoPista.java`) já é o registro central de tipos (label i18n via `toString()`, fábrica via `criar()`, `isCenario()`), usado hoje só para a criação de objetos — não existe hoje um caminho inverso (`ObjetoPista` instância → `TipoObjetoPista`).

Não existe no editor nenhum precedente de "grupo dinâmico de checkboxes que aparece/desaparece conforme o conteúdo de uma lista" — o precedente mais próximo é o próprio `listarObjetos()` (limpar e repopular a partir do estado atual do `Circuito`).

## Goals / Non-Goals

**Goals:**
- Uma lista única substitui as duas listas/`JSplitPane` atuais, sem mudar onde cada objeto é persistido no `Circuito` (`objetos` vs `objetosCenario` continuam existindo e sendo a fonte de verdade).
- Painel de filtro por tipo abaixo da lista única: grupo fixo "Mostrar Todos"/"Mostrar Nenhum" (checkboxes), separador visual, grupo dinâmico de checkboxes por tipo presente no circuito.
- O filtro por tipo esconde objetos tanto na lista quanto no desenho do circuito (canvas), incluindo hit-testing de clique, **sem remover nenhum objeto do `Circuito`** — esconder é puramente um estado de exibição do editor, reversível a qualquer momento reativando o checkbox correspondente.
- O checkbox global "Objetos" (`desenhaObjetosDesenho`) continua existindo e funcionando de forma independente/cumulativa com o novo filtro.
- Um terceiro modo de filtro, "Somente Selecionado", permite focar em um único objeto (lista e canvas) para trabalhar em circuitos com muitos objetos sem precisar configurar o filtro por tipo.
- Aproveitar que `desenhaObjetosNivel()`/`niveisDesenhoOrdenados()` já serão alterados para eliminar a realocação redundante de `todosObjetos()` a cada nível/frame (achado durante a análise de performance pedida pelo usuário).

**Non-Goals:**
- Não unifica `circuito.getObjetos()`/`circuito.getObjetosCenario()` num único campo do modelo de dados — permanece uma decisão de UI apenas, sem impacto em persistência XML, `ControleCorrida` ou qualquer consumidor fora do editor.
- Não adiciona um filtro por tipo ao desenho da corrida (`ControleCorrida`/`DesenhoProceduralCircuito` fora do editor) — o filtro é uma feature exclusiva do editor de circuitos, não afeta a partida em si.
- Não muda o comportamento de nenhum botão existente (Cima/Baixo/Primeiro/Ultimo/Remover/Editar) além de operarem sobre a lista unificada em vez de duas listas.

## Decisions

### 1. Uma única `FormularioListaObjetos`, com accessor de leitura/escrita compostos
Em vez de duas instâncias de `FormularioListaObjetos` com um `listaAccessor` simples cada, `gerarSecaoObjetos()` passa a criar uma única instância cujo par leitura/escrita opera sobre as duas coleções do `Circuito` ao mesmo tempo:

- **Leitura** (`listarObjetos()`): concatena `circuito.getObjetosCenario()` seguido de `circuito.getObjetos()` — mesma ordem visual que os dois painéis tinham hoje (cenário/desenho "em cima", função "embaixo"), agora como um único bloco contínuo em vez de dois `JScrollPane`s.
- **Escrita** (`atualizarCircuito()`): em vez de limpar/repopular uma única coleção, percorre o `DefaultListModel` atual e separa cada item para `circuito.getObjetosCenario()` (quando `TipoObjetoPista.de(objeto).isCenario()`) ou `circuito.getObjetos()` (caso contrário), preservando a ordem relativa de encontro de cada subgrupo. Isso mantém o efeito de Cima/Baixo/Primeiro/Ultimo dentro de cada coleção de origem, mesmo movendo o item por cima de itens do outro tipo na lista visual.

Implementação: como o construtor atual de `FormularioListaObjetos` recebe só um `Function<Circuito, List<ObjetoPista>>`, a forma mais direta é adicionar um construtor/modo alternativo (ex.: outro parâmetro `boolean unificada`, ou uma subclasse/segundo par de `Function`s — leitura via um `Function<Circuito, List<ObjetoPista>>` que já devolve a lista concatenada, e escrita via um novo `BiConsumer<Circuito, List<ObjetoPista>>` opcional que sabe fazer o split) sem quebrar o uso existente de `FormularioListaObjetos` fora deste change (não há outro uso fora de `MainPanelEditor`, então a assinatura pode mudar livremente).

- **Alternativa considerada**: manter as duas instâncias de `FormularioListaObjetos` internamente (não visíveis ao usuário) e só uni-las visualmente numa `JList` combinada por fora. Rejeitada — duplicaria seleção/scroll/eventos entre duas `JList`s tentando parecer uma só, muito mais frágil que resolver a composição na camada de leitura/escrita.
- **Alternativa considerada**: unificar de fato as duas coleções em uma só lista dentro de `Circuito`. Rejeitada nas perguntas de esclarecimento — fora de escopo, mudaria persistência XML e outros consumidores.

### 2. `TipoObjetoPista.de(ObjetoPista)`: caminho inverso tipo → instância
Novo método estático em `TipoObjetoPista`, iterando `values()` e comparando a classe concreta de cada `TipoObjetoPista.criar()` com `objeto.getClass()` (ou um `instanceof` por valor, já que são poucos tipos) para devolver o `TipoObjetoPista` correspondente a uma instância `ObjetoPista` já existente. Usado tanto pela escrita composta (decisão 1) quanto pela populção do grupo 2 de checkboxes (decisão 3) e pelo filtro de desenho (decisão 4).

- **Alternativa considerada**: adicionar um campo `TipoObjetoPista tipo` em `ObjetoPista`, setado na criação. Rejeitada — exigiria mudança em todos os construtores/desserialização XML existente (campo novo persistido ou recalculado), enquanto mapear por classe concreta é suficiente e não toca no modelo de dados persistido.

### 3. Grupo 2 (checkboxes dinâmicos): `Map<TipoObjetoPista, JCheckBox>` reconstruído a cada `listarObjetos()`
Novo campo em `MainPanelEditor`, ex. `private final Map<TipoObjetoPista, Boolean> tiposVisiveis = new EnumMap<>(TipoObjetoPista.class);` guarda o estado marcado/desmarcado por tipo, sobrevivendo a reconstruções do painel. Um novo método `atualizarPainelFiltroTipos()`:
1. Calcula o conjunto de tipos presentes em `todosObjetos()` (via `TipoObjetoPista.de(objeto)`).
2. Remove de `tiposVisiveis` entradas de tipos que não estão mais presentes (checkbox correspondente é descartado).
3. Para tipos presentes sem entrada em `tiposVisiveis`, insere `true` (visível por padrão).
4. Reconstrói o painel do grupo 2 (`JPanel` com um `JCheckBox` por tipo presente, na ordem de `TipoObjetoPista.values()`), lendo o rótulo de `tipo.toString()` (i18n já resolvido pelo enum) e o estado inicial de `tiposVisiveis.get(tipo)`.

Esse método é chamado no mesmo ponto em que hoje `listarObjetos()` já é chamado (os ~7 call sites de criação/remoção listados no proposal), então não introduz nenhum gatilho novo de atualização — reaproveita o padrão já existente de "mutar o `Circuito` e então ressincronizar a UI" (sem listener/evento, chamada direta).

- **Alternativa considerada**: registrar um listener no `Circuito`/`Circuito.objetos` (ex.: `List` observável) para disparar a atualização automaticamente. Rejeitada — o projeto não usa esse padrão em nenhum outro lugar do editor (confirmado na exploração: tudo é chamada direta e síncrona após a mutação); introduzir um observável só aqui destoaria do resto do código e adicionaria complexidade sem necessidade, já que os call sites de mutação já são conhecidos e finitos.

### 4. Filtro afeta lista, desenho e hit-testing via um único predicado
Novo método `private boolean tipoVisivel(ObjetoPista objeto)` em `MainPanelEditor`, consultando `tiposVisiveis` (decisão 3) através de `TipoObjetoPista.de(objeto)` (decisão 2; default `true` se o tipo ainda não tiver entrada, evitando esconder um objeto de um tipo que acabou de ser criado antes do painel recalcular). Esse predicado é aplicado em três pontos:
- `FormularioListaObjetos.listarObjetos()` (via um filtro adicional na composição da decisão 1): itens com `!tipoVisivel(objeto)` não entram no `DefaultListModel`.
- `desenhaObjetosNivel(Graphics2D, int)` (`MainPanelEditor.java:2804-2825`): nova condição `if (!tipoVisivel(objetoPista)) continue;`, somada (não substituindo) à condição já existente de `desenhaObjetosDesenho` — as duas continuam independentes e cumulativas.
- `encontraObjetoPistaNaLista(List<ObjetoPista>, Point)` (`MainPanelEditor.java:2490-2501`), usado pela seleção por clique no canvas: pula candidatos com `!tipoVisivel(objetoPista)`, para que um objeto escondido pelo filtro não seja selecionável.

`desenhaListaObjetos()` (indicador de seleção) não precisa de tratamento especial: como a seleção já vem da `JList` (que só contém itens visíveis pelo filtro) ou do canvas (já filtrado por `encontraObjetoPistaNaLista`), nunca haverá um objeto escondido selecionado para desenhar o indicador.

- **Alternativa considerada**: aplicar o filtro só na lista, deixando o desenho do canvas sempre completo. Rejeitada explicitamente nas perguntas de esclarecimento — o usuário optou por "lista e canvas".

### 5. Grupo 1: três checkboxes mutuamente exclusivos ("Mostrar Todos" / "Mostrar Nenhum" / "Somente Selecionado")
Três `JCheckBox` (`mostrarTodosCheck`, `mostrarNenhumCheck`, `somenteSelecionadoCheck`), mutuamente exclusivos por código (não por `ButtonGroup`, já que `ButtonGroup` normalmente exige rádio-like "sempre um marcado", e aqui os três podem ficar desmarcados quando o filtro está em um estado "parcial" — nem tudo, nem nada, nem só-o-selecionado):

- Ao marcar `mostrarTodosCheck`: todos os checkboxes do grupo 2 são marcados (e `tiposVisiveis` atualizado para `true` em todos), `mostrarNenhumCheck` e `somenteSelecionadoCheck` são desmarcados; se "Somente Selecionado" estava ativo, o modo é encerrado (ver abaixo).
- Ao marcar `mostrarNenhumCheck`: todos os checkboxes do grupo 2 são desmarcados (`tiposVisiveis` para `false` em todos), `mostrarTodosCheck` e `somenteSelecionadoCheck` são desmarcados (mesmo encerramento do modo "Somente Selecionado" se estava ativo).
- Ao marcar/desmarcar manualmente um checkbox individual do grupo 2 (só possível quando "Somente Selecionado" não está ativo — ver abaixo): `mostrarTodosCheck`/`mostrarNenhumCheck` são recalculados para refletir se o resultado bate com "tudo marcado" ou "nada marcado" (ou nenhum dos dois, no caso parcial) — sem disparar side-effect adicional além da própria correção visual dos dois checkboxes fixos.
- `somenteSelecionadoCheck` SHALL ficar desabilitado (`setEnabled(false)`) sempre que não houver nenhum objeto selecionado na lista única, reabilitado assim que uma seleção existir.

**Modo "Somente Selecionado"** (`private ObjetoPista focoSomenteSelecionado;`, além do próprio estado do checkbox):
- Ao marcar `somenteSelecionadoCheck` (só possível com uma seleção ativa): `focoSomenteSelecionado` recebe o objeto atualmente selecionado; a lista única e o desenho do circuito passam a mostrar exclusivamente esse objeto, **ignorando** o estado de `tiposVisiveis` (grupo 2) enquanto o modo estiver ativo; os checkboxes do grupo 2 (e `mostrarTodosCheck`/`mostrarNenhumCheck`) ficam desabilitados (`setEnabled(false)`) para deixar claro que estão temporariamente sem efeito, sem perder seus valores marcados/desmarcados.
- Ao desmarcar `somenteSelecionadoCheck` manualmente: `focoSomenteSelecionado` volta a `null`, os checkboxes do grupo 2 (e `mostrarTodosCheck`/`mostrarNenhumCheck`) são reabilitados, e a visibilidade volta a refletir `tiposVisiveis` exatamente como estava antes de entrar no modo (nada nesse mapa foi alterado enquanto o modo esteve ativo).
- Se o objeto referenciado por `focoSomenteSelecionado` for removido do circuito (`apagarObjetoSelecionado()`), ou se a seleção for limpa por qualquer outro caminho enquanto o modo está ativo, o modo SHALL ser encerrado automaticamente (mesmo efeito de desmarcar manualmente) — evita deixar o usuário travado numa lista vazia sem nenhum item clicável para restaurar a seleção.
- Como a lista única, nesse modo, mostra só o objeto em foco, trocar de foco exige primeiro desmarcar "Somente Selecionado" (voltando a ver a lista completa filtrada por tipo), selecionar outro objeto, e marcar de novo — comportamento aceito como o funcionamento pretendido de um modo de "foco", não um bug.

`tipoVisivel(ObjetoPista objeto)` (decisão 4) passa a checar primeiro `somenteSelecionadoCheck`/`focoSomenteSelecionado` (retorna `objeto == focoSomenteSelecionado` quando o modo está ativo) antes de consultar `tiposVisiveis`, para que os três pontos de aplicação do filtro (lista, `desenhaObjetosNivel`, `encontraObjetoPistaNaLista`) continuem usando um único predicado.

- **Alternativa considerada**: dois `JButton` ("Mostrar Todos"/"Mostrar Nenhum") executando a ação uma vez, sem manter estado próprio. Essa era a opção recomendada inicialmente, mas foi **rejeitada pelo usuário** nas perguntas de esclarecimento em favor de checkboxes com estado — mantido aqui como registro da escolha, agora estendida para o terceiro checkbox.
- **Alternativa considerada para "Somente Selecionado"**: aplicá-lo como mais um filtro cumulativo com o grupo 2 (em vez de sobrepor/ignorar `tiposVisiveis`). Rejeitada — o pedido do usuário foi "exibir só ele", não "exibir só ele entre os tipos já visíveis"; um modo de foco que ainda dependesse do estado do grupo 2 poderia mostrar zero objetos (ex.: tipo do objeto selecionado desmarcado no grupo 2), contradizendo a expectativa de sempre ver o objeto selecionado ao marcar esse checkbox.

### 6. Layout do painel: lista no CENTER, filtro no SOUTH (não NORTH/SOUTH literal)
`gerarSecaoObjetos()` usa `BorderLayout` com a lista única (`FormularioListaObjetos.getObjetos()`, que já é CENTER com scroll dentro de si) ocupando o `CENTER` do painel externo, e o novo painel de filtro ocupando o `SOUTH`. Isso preserva o comportamento de a lista esticar para ocupar o espaço vertical disponível (como acontece hoje dentro de cada `FormularioListaObjetos`), com o filtro sempre visível, altura fixa, logo abaixo.

Dentro do painel de filtro (`SOUTH`), a composição visual "grupo 1, depois linha divisória, depois grupo 2" é feita com um `JPanel` de `BoxLayout` vertical (`BoxLayout.Y_AXIS`) contendo, em ordem: o painel do grupo 1, um `JSeparator(SwingConstants.HORIZONTAL)`, e o painel do grupo 2 — não usa `BorderLayout` para essa parte interna, já que a intenção do usuário ali era só "um em cima do outro com uma linha entre", não uma relação North/South/Center com uma região central que deveria crescer.

- **Alternativa considerada (rejeitada pelo usuário)**: lista no `NORTH` do `BorderLayout` externo (como descrito literalmente no pedido original) e filtro no `SOUTH`. Nas perguntas de esclarecimento, o usuário confirmou preferir a lista no `CENTER` (esticando para ocupar o espaço disponível) — a leitura de "norte" no pedido original foi tratada como descrição informal de "lista em cima, filtro embaixo", não como exigência literal do valor `BorderLayout.NORTH`.

### 7. Performance do desenho do canvas com muitos objetos: eliminar a realocação de `todosObjetos()` por frame; sem indexação espacial por ora
O usuário pediu uma análise de performance do desenho do canvas quando há muitos objetos. Achados:

- **Já existe corte por viewport** (`editor-viewport-culling`, `estaVisivelNoViewport()`): pula a chamada de `desenha()` (a parte cara — `Graphics2D` desenhando formas) para objetos fora da tela. Isso já é a otimização de maior impacto e continua válida sem alteração.
- **Achado concreto**: `todosObjetos()` (`MainPanelEditor.java:2507-2516`) aloca um novo `ArrayList` e concatena `circuito.getObjetos()` + `circuito.getObjetosCenario()` a cada chamada. Ela é chamada uma vez em `niveisDesenhoOrdenados()` e mais uma vez **dentro de `desenhaObjetosNivel()` para cada nível distinto** — ambos chamados a cada `paintComponent()` (repintado contínuo via `GerenciadorVisual`/Swing). Com N objetos e L níveis distintos em uso, isso é `(L + 1)` alocações + varreduras completas de N objetos por frame, mesmo quando o corte por viewport já descarta a maioria deles depois — trabalho puramente redundante de realocação/concatenação que cresce com o número de objetos e de níveis.
- **Correção proposta** (bundlada nesta mudança, já que `desenhaObjetosNivel()`/`niveisDesenhoOrdenados()` já serão alterados para aplicar o novo filtro por tipo): calcular `todosObjetos()` uma única vez por `paintComponent()` e passar a lista já pronta para `niveisDesenhoOrdenados(List<ObjetoPista>)` e `desenhaObjetosNivel(Graphics2D, int, List<ObjetoPista>)`, eliminando as `L` realocações redundantes por frame. Mantém o mesmo algoritmo (filtragem por nível ainda é uma varredura linear por nível), só remove a realocação/concatenação repetida.
- **Ordem dos testes em `desenhaObjetosNivel`**: como o novo `tipoVisivel(objeto)` (decisão 5) é um `EnumMap`/lookup O(1) mais barato que `estaVisivelNoViewport()` (que calcula `obterArea()`, e em objetos rotacionados um `Math.hypot`), o teste de `tipoVisivel` SHALL vir antes do teste de viewport na condição do laço — descarta mais cedo o que já sabemos que não deve aparecer, evitando o cálculo de área para objetos escondidos pelo filtro.
- **Não recomendado por ora**: indexação espacial (quadtree/grid) para o corte por viewport em si. A varredura linear atual (`O(objetos)` por nível, por frame) é adequada para a escala observada de objetos por circuito (dezenas a poucas centenas); não há evidência de que isso seja o gargalo real. Fica registrado como possível próximo passo se algum circuito futuro crescer para milhares de objetos e um teste de performance real mostrar isso como gargalo — não implementado nesta mudança.

- **Alternativa considerada**: cachear `todosObjetos()` num campo, invalidando só quando o circuito muda (criação/remoção), em vez de recalcular por frame. Rejeitada por ora — adiciona um cache com invalidação manual (mais um lugar para esquecer de invalidar, como o campo `larguraPistaPixeis`/strokes já cacheados em `PainelCircuito` demonstram ser fonte de bugs sutis quando a invalidação é esquecida) por um ganho pequeno; calcular uma vez por frame (em vez de `L+1` vezes) já remove a redundância principal sem esse risco.

## Risks / Trade-offs

- [Risco] Os ~7 pontos em `MainPanelEditor` que hoje escolhem entre `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` (criação por clique, remoção, copiar/colar cor, seleção via canvas — ver Impact do proposal) precisam ser adaptados um a um para a única instância; esquecer algum deixaria a lista dessincronizada do `Circuito` em um fluxo específico → Mitigação: usar os testes existentes que exercitam cada um desses fluxos (`MainPanelEditorCopiarColarCorTest`, `MainPanelEditorSelecaoObjetoTest`, `MainPanelEditorEscapadaCliqueTest`, etc.) como checklist de regressão, ajustando cada um para o novo campo único e confirmando que continuam passando.
- [Risco] `TipoObjetoPista.de(ObjetoPista)` precisa cobrir exatamente os mesmos tipos que `TipoObjetoPista.values()` já cria; um novo `ObjetoPista` concreto adicionado no futuro sem entrada correspondente quebraria o filtro silenciosamente (objeto sem tipo mapeado) → Mitigação: `TipoObjetoPista.de(...)` lança exceção (em vez de retornar `null`) para uma classe não mapeada, forçando o erro a aparecer imediatamente em vez de o objeto desaparecer silenciosamente do filtro.
- [Trade-off] Objetos escondidos pelo filtro deixam de ser selecionáveis por clique no canvas (decisão 4) — um usuário que esqueça um filtro ativo pode ficar confuso por não conseguir clicar em um objeto que sabe que existe → aceito, mesmo comportamento pedido explicitamente pelo usuário para "lista e canvas"; o painel de filtro fica sempre visível (não colapsável) então o estado do filtro nunca fica escondido do usuário.
- [Trade-off] Reescrever `atualizarCircuito()` para fazer split por tipo (decisão 1) é mais complexo que o `clear()`/`addAll()` atual — pequeno aumento de complexidade ciclomática nesse método, aceito como custo necessário da unificação pedida.
- [Risco] O modo "Somente Selecionado" precisa ser encerrado automaticamente em mais de um gatilho (remoção do objeto em foco, perda de seleção por outro caminho) — esquecer um desses gatilhos deixaria o usuário com a lista mostrando um único item sem conseguir escolher outro sem recarregar o circuito → Mitigação: teste cobrindo especificamente a remoção do objeto em foco enquanto o modo está ativo (seção de testes do filtro), verificando que o modo é encerrado e a lista completa (filtrada por tipo) volta a aparecer.
- [Risco] Mover o cálculo de `todosObjetos()` para fora de `niveisDesenhoOrdenados()`/`desenhaObjetosNivel()` (decisão 7) muda a assinatura desses métodos privados — qualquer teste que os invoque via reflexão (ex.: `MainPanelEditorDesenhaObjetosCheckboxTest`, que chama `desenhaObjetosNivel` via `getDeclaredMethod`) precisa ser atualizado para a nova assinatura → Mitigação: já listado como tarefa explícita, não uma mudança silenciosa.
