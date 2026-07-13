## 1. Caminho inverso instância → tipo

- [x] 1.1 Adicionar `TipoObjetoPista.de(ObjetoPista objeto)` em `TipoObjetoPista.java`, mapeando cada classe concreta (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`, `ObjetoLivre`, `ObjetoEscapada`, `ObjetoTransparencia`) ao respectivo valor do enum via `instanceof`, lançando `IllegalArgumentException` para uma classe não mapeada (em vez de retornar `null`).
- [x] 1.2 Teste unitário cobrindo `TipoObjetoPista.de(...)` para cada um dos sete tipos concretos, e o caso de exceção para uma classe não mapeada (ex.: `ObjetoCirculo`, que existe no código mas não está no enum).

## 2. `FormularioListaObjetos`: leitura/escrita compostas para lista unificada

- [x] 2.1 Adicionar um modo/construtor em `FormularioListaObjetos` que lê concatenando `circuito.getObjetosCenario()` seguido de `circuito.getObjetos()` (em vez de um único `listaAccessor`), aplicando o filtro de visibilidade por tipo (ver seção 4) antes de popular o `DefaultListModel`.
- [x] 2.2 Reescrever `atualizarCircuito()` para, nesse modo unificado, percorrer o `DefaultListModel` e separar cada item de volta para `circuito.getObjetosCenario()` ou `circuito.getObjetos()` conforme `TipoObjetoPista.de(objeto).isCenario()`, preservando a ordem relativa de cada subgrupo conforme a ordem de encontro no `DefaultListModel`.
- [x] 2.3 Teste cobrindo: lista unificada com objetos de cenário e função intercalados; mover um objeto de função (Cima/Baixo/Primeiro/Ultimo) para uma posição entre objetos de cenário e confirmar que ele permanece em `circuito.getObjetos()`, não migra para `circuito.getObjetosCenario()`; remover um item da lista unificada remove da coleção correta do `Circuito`; objeto escondido pelo filtro (seção 4) continua presente na coleção do `Circuito` mesmo não aparecendo no `DefaultListModel`.

## 3. `MainPanelEditor`: unificar os dois campos e ajustar os call sites

- [x] 3.1 Substituir os campos `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` por um único campo `formularioListaObjetos` (pacote-privado, mesmo padrão de visibilidade para testes).
- [x] 3.2 Reescrever `gerarSecaoObjetos()`: remover o `JSplitPane`/`listaDesenho`/`listaFuncao`, montar a lista única no `CENTER` de um `BorderLayout`, com o botão "Criar Objeto" no `NORTH` (como hoje) e o novo painel de filtro (seção 4/5) no `SOUTH`.
- [x] 3.3 Ajustar `primeiroSelecionado`/`todosSelecionados`/`copiarCorObjetoSelecionado()`/`colarCorObjetosSelecionados()` para operar sobre a única `formularioListaObjetos` em vez de somar seleção das duas.
- [x] 3.4 Ajustar `apagarObjetoSelecionado()` para chamar `formularioListaObjetos.listarObjetos()` e `atualizarPainelFiltroTipos()` (seção 4) uma única vez após remover de `objetos` ou `objetosCenario`, em vez de escolher entre os dois formulários — e para encerrar o modo "Somente Selecionado" (seção 5) se o objeto removido for o que estava em foco.
- [x] 3.5 Ajustar os pontos de criação por clique (Transparencia, ObjetoLivre, ObjetoGuardRails, ObjetoEscapada, e o branch genérico de `criandoObjetoCenario`) para chamar `formularioListaObjetos.listarObjetos()` (e `atualizarPainelFiltroTipos()`) uma única vez, mantendo a lógica existente de adicionar a `circuito.getObjetos()` ou `circuito.getObjetosCenario()` conforme o tipo.
- [x] 3.6 Ajustar `selecionarSemCentralizar`/repaint dos formulários (linhas ~3737-3772) para referenciar só `formularioListaObjetos`.
- [x] 3.7 Atualizar `desenhaListaObjetos()` para buscar o primeiro selecionado só em `formularioListaObjetos`.

## 4. Painel de filtro por tipo (grupo 2) e "Mostrar Todos"/"Mostrar Nenhum" (grupo 1)

- [x] 4.1 Adicionar campo `private final Map<TipoObjetoPista, Boolean> tiposVisiveis = new EnumMap<>(TipoObjetoPista.class);` em `MainPanelEditor`.
- [x] 4.2 Implementar `private boolean tipoVisivel(ObjetoPista objeto)`, consultando primeiro o modo "Somente Selecionado" (seção 5) e, se não ativo, `tiposVisiveis` via `TipoObjetoPista.de(objeto)` (default `true` quando o tipo ainda não tem entrada).
- [x] 4.3 Implementar `atualizarPainelFiltroTipos()`: recalcula o conjunto de tipos presentes em `todosObjetos()`, remove de `tiposVisiveis` os tipos não mais presentes, insere `true` para tipos novos, e reconstrói o `JPanel` do grupo 2 (um `JCheckBox` por tipo presente, ordem de `TipoObjetoPista.values()`, rótulo via `tipo.toString()`), desabilitando os checkboxes reconstruídos se o modo "Somente Selecionado" estiver ativo no momento.
- [x] 4.4 Montar o painel de filtro completo: `JPanel` com `BoxLayout.Y_AXIS` contendo, em ordem, o painel do grupo 1 (checkboxes "Mostrar Todos"/"Mostrar Nenhum"/"Somente Selecionado", seção 5), um `JSeparator(SwingConstants.HORIZONTAL)`, e o painel do grupo 2 (`atualizarPainelFiltroTipos()`).
- [x] 4.5 Implementar a sincronização "Mostrar Todos"/"Mostrar Nenhum": marcar "Mostrar Todos" marca todos os checkboxes do grupo 2 (e `tiposVisiveis`), desmarca "Mostrar Nenhum" e encerra o modo "Somente Selecionado" se ativo; marcar "Mostrar Nenhum" desmarca todos (e `tiposVisiveis`), desmarca "Mostrar Todos" e igualmente encerra "Somente Selecionado" se ativo; marcar/desmarcar um checkbox individual do grupo 2 recalcula o estado de "Mostrar Todos"/"Mostrar Nenhum" (ambos desmarcados no caso parcial).
- [x] 4.6 Chamar `atualizarPainelFiltroTipos()` em todos os pontos que hoje chamam `listarObjetos()` após criar/remover um objeto (mesmos call sites da seção 3), garantindo que o painel de filtro reflita o conjunto atual de tipos.
- [x] 4.7 Adicionar chaves de idioma novas (`mostrarTodosObjetosFiltro`, `mostrarNenhumObjetoFiltro`, `somenteSelecionadoFiltro`) em `src/main/resources/idiomas/mensagens_XX.properties` para todos os idiomas suportados (`pt`, `en`, `es`, `it`).

## 5. Modo "Somente Selecionado"

- [x] 5.1 Adicionar campo `private ObjetoPista focoSomenteSelecionado;` e o `JCheckBox somenteSelecionadoCheck` em `MainPanelEditor`.
- [x] 5.2 Implementar a habilitação condicional: `somenteSelecionadoCheck` fica desabilitado sempre que não há seleção em `formularioListaObjetos.getList()`, reabilitado quando uma seleção passa a existir (listener de seleção já existente em `FormularioListaObjetos`, ou um novo hook chamado por ele).
- [x] 5.3 Implementar `marcarSomenteSelecionado()`: captura o objeto selecionado em `focoSomenteSelecionado`, desmarca "Mostrar Todos"/"Mostrar Nenhum", desabilita os checkboxes do grupo 2 (preservando seus valores), chama `formularioListaObjetos.listarObjetos()` e `repaint()`.
- [x] 5.4 Implementar `desmarcarSomenteSelecionado()`: zera `focoSomenteSelecionado`, reabilita os checkboxes do grupo 2, chama `formularioListaObjetos.listarObjetos()` e `repaint()` — chamado tanto ao desmarcar manualmente quanto pelos gatilhos automáticos abaixo.
- [x] 5.5 Chamar `desmarcarSomenteSelecionado()` automaticamente quando o objeto referenciado por `focoSomenteSelecionado` é removido (`apagarObjetoSelecionado()`, ver 3.4) ou quando a seleção é limpa por qualquer outro caminho enquanto o modo está ativo.
- [x] 5.6 Teste cobrindo: checkbox desabilitado sem seleção; marcar com uma seleção mostra só o objeto selecionado na lista e no `BufferedImage` renderizado (mesmo padrão de `MainPanelEditorDesenhaObjetosCheckboxTest`); desmarcar restaura a visibilidade conforme `tiposVisiveis`; remover o objeto em foco encerra o modo automaticamente e a lista volta a mostrar os demais objetos conforme o filtro por tipo.

## 6. Filtro aplicado ao desenho e à seleção por clique no canvas

- [x] 6.1 Em `desenhaObjetosNivel(...)`, adicionar `if (!tipoVisivel(objetoPista)) continue;` **antes** do teste de viewport (`estaVisivelNoViewport`, mais caro — ver seção 8), cumulativo com a condição existente de `desenhaObjetosDesenho`.
- [x] 6.2 Em `encontraObjetoPistaNaLista(List<ObjetoPista>, Point)`, pular candidatos com `!tipoVisivel(objetoPista)`, para que objetos escondidos pelo filtro (por tipo ou por "Somente Selecionado") não sejam selecionáveis por clique no canvas.
- [x] 6.3 Teste (seguindo o padrão de `MainPanelEditorDesenhaObjetosCheckboxTest.java`, via reflexão sobre `tiposVisiveis`/chamada direta a `desenhaObjetosNivel`) cobrindo: objeto de um tipo desmarcado não aparece no `BufferedImage` renderizado; objeto de um tipo marcado aparece normalmente; o checkbox global `desenhaObjetosDesenho` e o filtro por tipo continuam cumulativos (desmarcar os dois ao mesmo tempo não reativa nenhum objeto de cenário).
- [x] 6.4 Teste cobrindo que um clique no canvas sobre a posição de um objeto com tipo desmarcado (ou fora do foco de "Somente Selecionado") não resulta em seleção (via `encontraObjetoPistaNaLista` ou o fluxo de clique que o usa).

## 7. Atualizar testes existentes que assumem duas listas

- [x] 7.1 Ajustar `MainPanelEditorCopiarColarCorTest.java` (helper `editorComListasPopuladas`) para popular o único `formularioListaObjetos` em vez de `formularioListaObjetosFuncao`/`formularioListaObjetosDesenho` separadamente, incluindo o teste que hoje documenta "Primeiro/Ultimo só na lista de desenho" (esse comportamento deixa de fazer sentido com uma lista única — atualizar ou remover conforme o resultado real da seção 3).
- [x] 7.2 Ajustar `MainPanelEditorSelecaoObjetoTest.java`, `MainPanelEditorNivelObjetoTest.java`, `MainPanelEditorEscapadaCliqueTest.java`, `MainPanelEditorEscapadaArrastarPontoTest.java`, `MainPanelEditorEscapadaRevetorizarTest.java` para referenciar `formularioListaObjetos` em vez dos dois campos antigos, sem alterar o comportamento testado.
- [x] 7.3 Conferir que nenhum teste restante referencia `formularioListaObjetosDesenho`/`formularioListaObjetosFuncao` (busca no diretório `src/test/java/br/f1mane/editor/`).

## 8. Performance: eliminar realocação redundante de `todosObjetos()` por frame

- [x] 8.1 Alterar `niveisDesenhoOrdenados()` para receber `List<ObjetoPista>` como parâmetro em vez de chamar `todosObjetos()` internamente.
- [x] 8.2 Alterar `desenhaObjetosNivel(Graphics2D, int)` para receber também `List<ObjetoPista>` como parâmetro em vez de chamar `todosObjetos()` internamente.
- [x] 8.3 Em `paintComponent(Graphics)`, calcular `List<ObjetoPista> todos = todosObjetos();` uma única vez no início, e passar essa lista para as chamadas de `niveisDesenhoOrdenados(todos)` e `desenhaObjetosNivel(g2d, nivel, todos)` (todas as ocorrências no método).
- [x] 8.4 Atualizar `MainPanelEditorDesenhaObjetosCheckboxTest.java` (e qualquer outro teste que invoque `desenhaObjetosNivel`/`niveisDesenhoOrdenados` via reflexão) para a nova assinatura.
- [x] 8.5 Confirmar por leitura que nenhum outro chamador de `todosObjetos()` (fora de `paintComponent`) precisa da mesma otimização nesta mudança — os demais usos (ex.: `encontraObjetoPistaNaLista`, se aplicável) não são chamados em loop por frame.

## 9. Validação final

- [x] 9.1 Rodar `mvn test` e garantir que a suíte completa passa, incluindo os testes novos e ajustados das seções 1–8.
- [ ] 9.2 Rodar o editor manualmente (`java -cp target/flmane.jar br.f1mane.MainFrame`, abrir o editor de circuitos) e verificar interativamente: lista única exibe objetos de cenário e de função juntos; painel de filtro aparece abaixo da lista com "Mostrar Todos"/"Mostrar Nenhum"/"Somente Selecionado" separados por linha dos checkboxes de tipo; criar/remover objetos faz checkboxes de tipo aparecerem/desaparecerem dinamicamente; desmarcar um tipo esconde da lista e do desenho (objeto continua existindo — reaparece ao marcar de novo), e o objeto some da seleção por clique; "Mostrar Todos"/"Mostrar Nenhum" afetam todos os checkboxes de tipo de uma vez e são mutuamente exclusivos; "Somente Selecionado" fica desabilitado sem seleção, foca em um único objeto quando marcado, e volta ao filtro anterior ao desmarcar ou ao remover o objeto em foco; checkbox global "Objetos" continua funcionando de forma independente.
- [ ] 9.3 Com um circuito de teste com muitos objetos (dezenas a centenas), observar informalmente se o desenho do canvas continua fluido durante navegação/zoom após a mudança da seção 8 — não é um benchmark formal, só uma checagem de que a otimização não introduziu regressão perceptível.
