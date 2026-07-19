## 1. Cópia profunda de `ObjetoPista`

- [x] 1.1 Adicionar helper `private static ObjetoPista copiaProfunda(ObjetoPista origem)` em `MainPanelEditor.java`, usando round-trip de serialização Java (`ObjectOutputStream`/`ObjectInputStream` sobre `ByteArrayOutputStream`/`ByteArrayInputStream`), envolto em try/catch apropriado (`IOException`/`ClassNotFoundException`) já que `ObjetoPista implements Serializable`.
- [x] 1.2 Teste unitário do helper cobrindo pelo menos um tipo com `List<Point>` mutável (ex. `ObjetoGuardRails` ou `ObjetoLivre`): confirmar que a cópia é uma instância diferente, com listas de pontos diferentes (não a mesma referência), e que mover a cópia não altera o original.

## 2. Botões e memória de "Copiar Objetos"

- [x] 2.1 Ajustar `gerarBotaoCriarObjeto()` para `GridLayout(5, 1)` e adicionar os botões "Copiar Objetos" e "Colar Objetos" logo abaixo de "Colar Cor".
- [x] 2.2 Novo campo `private List<ObjetoPista> objetosCopiados = new ArrayList<>();` em `MainPanelEditor`.
- [x] 2.3 Implementar `void copiarObjetosSelecionados()`: lê `todosSelecionados(formularioListaObjetos)`; se vazio, retorna sem efeito; caso contrário, substitui `objetosCopiados` por uma cópia profunda (via `copiaProfunda()`) de cada selecionado.
- [x] 2.4 Ligar o botão "Copiar Objetos" a `copiarObjetosSelecionados()` (mesmo padrão do `ActionListener` de "Copiar Cor").

## 3. Diálogo de aviso e modo de posicionamento por clique

- [x] 3.1 Novo método `protected void avisarClicarParaColarObjetos()` em `MainPanelEditor`, mostrando `JOptionPane.showMessageDialog(srcFrame, Lang.msg("clicarParaColarObjetos"))` — seguindo o padrão de `alertaPontoEscapadaInvalido()` já documentado no `CLAUDE.md` do projeto.
- [x] 3.2 Override de `avisarClicarParaColarObjetos()` em `src/test/java/br/flmane/editor/MainPanelEditorTestDouble.java`, incrementando um contador (mesmo padrão dos overrides existentes nesse double), com getter correspondente.
- [x] 3.3 Novo campo `private List<ObjetoPista> objetosParaColar;` em `MainPanelEditor` (lote pendente de posicionamento, `null` fora do modo de colagem).
- [x] 3.4 Implementar `void colarObjetosSelecionados()`: se `objetosCopiados` estiver vazio, retorna sem efeito (sem diálogo); caso contrário, chama `avisarClicarParaColarObjetos()`, monta `objetosParaColar` com uma cópia profunda de cada item de `objetosCopiados`, e liga `posicionaObjetoPista = true`.
- [x] 3.5 Ligar o botão "Colar Objetos" a `colarObjetosSelecionados()`.
- [x] 3.6 Em `clickEditarObjetos` (dentro de `adicionaEventosMouse`), adicionar um novo ramo `else if (posicionaObjetoPista && objetosParaColar != null && !objetosParaColar.isEmpty())`, posicionado ao lado do ramo existente `posicionaObjetoPista && objetoPista != null` (linhas ~2054-2079): calcula o delta entre `ultimoClicado` e o centro (`obterArea()`) do primeiro objeto de `objetosParaColar`; para cada objeto do lote, aplica esse mesmo delta à sua `quina` atual via `setPosicaoQuina(...)` e adiciona à coleção do `Circuito` correspondente ao seu tipo (`TipoObjetoPista.de(objeto).isCenario()`); ao final, `formularioListaObjetos.listarObjetos()`, `atualizarPainelFiltroTipos()`, `repaint()`, `objetosParaColar = null`, `posicionaObjetoPista = false`.

## 4. i18n

- [x] 4.1 Adicionar chaves `copiarObjetos`/`colarObjetos`/`clicarParaColarObjetos` em `mensagens_pt.properties`, `mensagens_en.properties`, `mensagens_es.properties` e `mensagens_it.properties`, logo após `copiarCor`/`colarCor` em cada arquivo.

## 5. Testes

- [x] 5.1 Criar `src/test/java/br/flmane/editor/MainPanelEditorCopiarColarObjetosTest.java` (extends `MainPanelEditorTestDouble`, mesmo padrão de `MainPanelEditorCopiarColarCorTest.java`), cobrindo: copiar sem seleção (sem efeito); copiar substitui conteúdo anterior da memória; colar sem nada copiado (nenhum diálogo, sem efeito); colar com objeto(s) copiado(s) mostra o diálogo (via contador do double) e entra em modo de posicionamento; um clique simulado no canvas após colar insere o objeto centrado no ponto clicado; colar múltiplos objetos preserva a distância/direção relativa entre eles após o clique; mover o objeto colado não afeta o original; colar duas vezes seguidas (dois cliques em pontos diferentes) gera dois objetos independentes.
- [x] 5.2 Teste cobrindo troca de circuito entre copiar e colar (`carregarCircuitoExistente()` com um circuito diferente) confirmando que colar ainda funciona com a memória da cópia anterior.
- [x] 5.3 Rodar `mvn test` e confirmar que a suíte completa (incluindo os testes existentes de `FormularioListaObjetos`/lista única/filtro/criação de objeto via `posicionaObjetoPista`) continua passando.

## 6. Correções pós-teste manual (feedback do usuário)

- [x] 6.1 Corrigir objeto colado não aparecer até recarregar o circuito: novo helper `forcarAtualizacaoArea(ObjetoPista)` em `MainPanelEditor` (desenha num `BufferedImage` 1x1 descartável logo após `setPosicaoQuina(...)`, antes de adicionar à coleção do `Circuito`), fechando o impasse entre `estaVisivelNoViewport()` (usa `obterArea()`) e `desenha()` (único lugar que atualiza essa área) — ver design.md decisão 6.
- [x] 6.2 Teste `cliqueAposColar_areaJaReflexteAPosicaoFinalSemPrecisarDeRepaint()` confirmando que `obterArea()` do objeto colado já reflete a posição final imediatamente após o clique, sem depender de repaint.
- [x] 6.3 Corrigir `FormularioListaObjetos.removerObjetoSelecionado()` para remover todos os itens selecionados (`getSelectedIndices()`, do maior índice pro menor) em vez de só um (`getSelectedIndex()`) — afeta tanto o botão "Remover" quanto a tecla Delete, que chamam o mesmo método.
- [x] 6.4 Teste `removerVariosObjetosSelecionados_removeTodosDaColecaoCorreta()` em `FormularioListaObjetosUnificadaTest.java` cobrindo remoção de seleção múltipla misturando objetos de cenário e de função.
- [x] 6.5 Delta spec `MODIFIED Requirements` para a capability `editor-lista-objetos-filtro-tipo` (requirements "Lista única de objetos no editor" e "Tecla Delete remove o objeto selecionado na lista única") documentando o suporte a seleção múltipla no Remover/Delete.
- [x] 6.6 Rodar `mvn test` novamente e confirmar que a suíte completa continua passando (780 testes).

## 7. Contorno de seleção: rotação + cor distinta pra seleção múltipla (feedback do usuário)

- [x] 7.1 Extrair helper `private void desenhaContornoSelecao(Graphics2D g2d, ObjetoPista objeto, Color cor)` em `MainPanelEditor.java` a partir da lógica de rotação já existente em `desenhaObjetoSelecionadoNoCanvas()`, parametrizada por cor; `desenhaObjetoSelecionadoNoCanvas()` passa a delegar pra ele com `Color.ORANGE`.
- [x] 7.2 Nova constante `private static final Color COR_SELECAO_MULTIPLA = Color.CYAN;`.
- [x] 7.3 Reescrever `desenhaListaObjetos(Graphics2D)` pra usar `todosSelecionados(formularioListaObjetos)` em vez de `primeiroSelecionado(...)`: com 1 selecionado, mantém a etiqueta de número + contorno laranja (via `desenhaContornoSelecao`); com mais de 1, desenha só o contorno em `COR_SELECAO_MULTIPLA` pra cada um, sem etiqueta.
- [x] 7.4 Criar `src/test/java/br/flmane/editor/MainPanelEditorIndicadorSelecaoTest.java` cobrindo: contorno sem rotação continua alinhado aos eixos; contorno de um objeto rotacionado (90°) acompanha a rotação (verificado por amostragem de pixels via `BufferedImage`, mesmo padrão de `MainPanelEditorViewportCullingTest`); seleção múltipla desenha contorno ciano em cada objeto selecionado sem nenhum laranja; sem seleção não desenha nada.
- [x] 7.5 Rodar `mvn test` e confirmar que a suíte completa continua passando.

## 8. Unificar seleção do canvas e da lista, etiqueta em todo objeto selecionado (feedback do usuário)

- [x] 8.1 Em `FormularioListaObjetos.java`, no `ListSelectionListener.valueChanged()` (não suprimido por `selecaoProgramatica`), sincronizar `editor.objetoPista` com a seleção atual da lista: exatamente 1 selecionado → esse objeto; 0 ou 2+ selecionados → `null`.
- [x] 8.2 Remover `desenhaObjetoSelecionadoNoCanvas()` de `MainPanelEditor.java` (e sua chamada em `paintComponent`) — dead code depois da sincronização, já que `desenhaListaObjetos()` (lendo a seleção da lista, agora sempre sincronizada com `objetoPista`) cobre o mesmo caso.
- [x] 8.3 Reescrever `desenhaListaObjetos(Graphics2D)` pra desenhar a etiqueta de número (extraída em `desenhaEtiquetaObjeto(Graphics2D, ObjetoPista)`) e o contorno pra **cada** objeto selecionado, seleção única ou múltipla — reverte a task 7.3 (que só mostrava etiqueta na seleção única).
- [x] 8.4 Testes em `FormularioListaObjetosUnificadaTest.java`: selecionar um objeto diferente na lista atualiza `editor.getObjetoPista()` (sem fantasma do antigo); seleção múltipla zera `objetoPista`; limpar a seleção zera `objetoPista`.
- [x] 8.5 Teste em `MainPanelEditorIndicadorSelecaoTest.java`: seleção múltipla desenha a etiqueta de número em cada objeto selecionado (não só o contorno).
- [x] 8.6 Rodar `mvn test` e confirmar que a suíte completa continua passando (788 testes).

## 9. Canvas nunca cria/mantém seleção múltipla (feedback do usuário, revisão)

- [x] 9.1 Corrigir `FormularioListaObjetos.selecionarSemCentralizar(ObjetoPista)`: condição passa a ser `list.getSelectedIndex() != indice || list.getSelectedIndices().length != 1`, colapsando qualquer seleção múltipla pré-existente pra seleção única mesmo quando o objeto clicado no canvas já é o de menor índice na seleção múltipla (caso em que `getSelectedIndex() != indice` sozinho não pega).
- [x] 9.2 Teste em `FormularioListaObjetosUnificadaTest.java` cobrindo esse caso específico (seleção múltipla {0,2}, clique no canvas no objeto do índice 0 — o menor — deveria colapsar pra seleção única).
- [x] 9.3 Rodar `mvn test` e confirmar que a suíte completa continua passando (789 testes).

## 10. Documentação da mudança

- [ ] 10.1 Rodar `openspec archive editor-copiar-colar-objetos` (ou `/opsx:archive`) depois que os testes passarem, para mover os deltas de spec para `openspec/specs/editor-copiar-colar-objetos/spec.md` e `openspec/specs/editor-lista-objetos-filtro-tipo/spec.md`.
