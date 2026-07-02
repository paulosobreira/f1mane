## 1. Propriedade `ativo` em `Circuito`

- [x] 1.1 Adicionar campo `private boolean ativo` (default `false`) a `br.f1mane.entidades.Circuito`, com `isAtivo()`/`setAtivo(boolean)` seguindo o padrão JavaBeans já usado por `noite`/`usaBkg`
- [x] 1.2 Confirmar via teste manual (salvar/carregar um circuito no editor) que `XMLEncoder`/`XMLDecoder` grava e lê a propriedade corretamente, incluindo o caso de XML antigo sem a propriedade (deve carregar como `false`) — verificado com round-trip standalone (encode/decode com e sem a propriedade)

## 2. Migração dos circuitos existentes para `ativo=true`

- [x] 2.1 Levantar a lista completa de arquivos em `src/main/resources/circuitos/*.xml` hoje referenciados em `properties/circuitos.properties` (circuitos jogáveis) — 37 arquivos, todos referenciados
- [x] 2.2 Editar em lote (script ou edição direta) cada um desses XMLs para incluir `<void property="ativo"><boolean>true</boolean></void>`
- [x] 2.3 Validar que o número de XMLs com `ativo=true` bate com o número de entradas em `circuitos.properties`, antes de prosseguir para a filtragem em runtime — 37/37

## 3. Filtragem de circuitos inativos em runtime

- [x] 3.1 Ajustar `CarregadorRecursos.carregarCircuitosDefaults()` para pular (`continue`) circuitos com `isAtivo()` falso ao montar a lista de `CircuitosDefault` — resolvido filtrando na fonte (ver 3.2)
- [x] 3.2 Filtrar na fonte única `ControleRecursos.carregarCircuitos()` (em vez de em cada combo box separadamente): ao montar o cache estático `Map<String,String>`, decodifica cada `Circuito` e pula (`continue`) os que tiverem `isAtivo()` falso. Isso cobre automaticamente todos os consumidores desse cache — `GerenciadorVisual.gerarSeletorCircuito()`, `PainelEntradaCliente`, `PainelMenuLocal`, `MainFrameSimulacao` e `CarregadorRecursos.carregarCircuitosDefaults()` — sem precisar tocar cada um individualmente
- [x] 3.3 `PainelEntradaCliente` (combo de circuitos do cliente multiplayer) já herda a exclusão por ler `ControleRecursos.carregarCircuitos()` diretamente (`this.circuitos = ControleRecursos.carregarCircuitos()`)
- [x] 3.4 Confirmar que `LetsRace.circuitos()` (REST `GET /circuitos`) reflete a exclusão por depender de `carregarCircuitosDefaults()` → `ControleRecursos.carregarCircuitos()`
- [x] 3.5 Verificado programaticamente (sem GUI): `jerez_mro.xml` marcado temporariamente `ativo=false` desaparece de `ControleRecursos.carregarCircuitos()` (36/37 no mapa), fonte única usada por `GerenciadorVisual`, `PainelEntradaCliente` e (via `carregarCircuitosDefaults()`) `LetsRace.circuitos()`; arquivo restaurado para `ativo=true` em seguida. A verificação de que o circuito continua abrível no editor requer sessão gráfica interativa

## 4. Editor: navegação Anterior/Próximo para circuito existente

- [x] 4.1 Implementar `popularCircuitos()` em `MainPanelEditor` lendo `properties/circuitos.properties` (chaves = arquivos XML), com fallback de varredura de `src/main/resources/circuitos/*.xml`, replicando `EditorCoresCarros.popularTemporadas()`
- [x] 4.2 Adicionar botões "Anterior"/"Próximo" e um label do circuito atual (`gerarTopoNavegacaoEAcoes()`), replicando `navegarTemporada`/`atualizarNavegacaoTemporada` de `EditorCoresCarros`
- [x] 4.3 Substituir o antigo `MainPanelEditor.editar()` (baseado em `JFileChooser`) por `carregarCircuitoExistente(String)`, carregando via `CarregadorRecursos.carregarCircuito(nomeArquivoXml)` a partir da navegação Anterior/Próximo (`navegarCircuito(delta)`)
- [x] 4.4 Preservar o fluxo atual de `novo()` (escolha de imagem de fundo via `JFileChooser`) para criação de circuito novo, sem navegação Anterior/Próximo (`indiceCircuito = -1`, label mostra "(novo circuito, não salvo)")
- [ ] 4.5 Teste manual: abrir o editor, navegar entre pelo menos 3 circuitos existentes com Anterior/Próximo, e confirmar que os controles desabilitam corretamente nas pontas da lista — requer sessão gráfica interativa, não executável neste ambiente headless

## 5. Editor: split pane lateral consolidado (nós + objetos)

- [x] 5.1 Substituir `gerarListsNosPistaBox()` (WEST) e `listasObjetosPanel` (EAST) por um único `JSplitPane` (`gerarSplitPaneLateral()`, orientação vertical) posicionado em uma única lateral (EAST) da janela
- [x] 5.2 Mover as listas `pistaJList`/`boxJList`, o seletor de tipo de nó (`gerarRadiosTipoNo()`) e os botões "Apagar Ultimo NO"/"Apaga Nó na lista Selecionada" (`gerarBotoesAcoesNo()`) para a seção superior (nós) do split pane (`gerarSecaoNos()`)
- [x] 5.3 Mover os dois `FormularioListaObjetos` (`objetos`/`objetosCenario`) e o botão "Criar Objeto" (`gerarBotaoCriarObjeto()`) para a seção inferior (objetos) do split pane (`gerarSecaoObjetos()`)
- [x] 5.4 Remover o antigo `buttonsPanel`/`gerarBotoesTracado()` monolítico; os toggles de visualização (Desenha Traçado/Desenho Background/Créditos) que não são nem ação de nó nem de objeto ficaram em `gerarBotoesVisualizacao()`, no topo (NORTH), fora do split pane
- [x] 5.5 Definir proporção inicial do divisor (`setResizeWeight(0.5)`) e habilitar arraste (`JSplitPane` é arrastável por padrão; `setOneTouchExpandable(true)` adicionado)
- [ ] 5.6 Teste manual: inserir um nó pela seção de nós e criar/editar um objeto pela seção de objetos, confirmando que ambas as ações continuam funcionando dentro do novo layout — requer sessão gráfica interativa, não executável neste ambiente headless

## 6. Editor: remoção do menu e botões de topo

- [x] 6.1 Remover `gerarMenusEditor()`/`JMenuBar` de `EditorCircuitos` (também removidos os menus "Informações"/logs/debug/sobre, conforme aceito no design.md)
- [x] 6.2 Adicionar botões de topo "Salvar Pista Atual" (chama `salvarPista()`) e "Criar Nova" (chama `novo()`) em `gerarTopoNavegacaoEAcoes()`
- [x] 6.3 Confirmar que o atalho de teclado F8 continua funcionando e chama a mesma ação do botão "Salvar Pista Atual" (`EditorCircuitos.ativarKeysEditor()` inalterado, ainda chama `editor.salvarPista()`)
- [x] 6.4 Adicionar um checkbox "Ativo" junto aos botões de topo, ligado a `circuito.isAtivo()`/`setAtivo(boolean)`
- [ ] 6.5 Teste manual: abrir o editor, confirmar ausência de `JMenuBar`, e validar que os dois botões de topo e o checkbox "Ativo" disparam os fluxos corretos — requer sessão gráfica interativa, não executável neste ambiente headless; smoke test de startup sem exceção feito via `java -cp target/classes br.f1mane.editor.EditorCircuitos`

## 7. Verificação final

- [x] 7.1 Rodar `mvn test` e confirmar que a suíte existente continua passando — 138/138 testes passando (`mvn -o test -Ph2`)
- [ ] 7.2 Exercitar manualmente o fluxo completo: criar um circuito novo (nasce `ativo=false`), editá-lo com o novo split pane, marcar `ativo=true`, salvar, e confirmar que ele passa a aparecer nos seletores em jogo — requer sessão gráfica interativa, não executável neste ambiente headless
