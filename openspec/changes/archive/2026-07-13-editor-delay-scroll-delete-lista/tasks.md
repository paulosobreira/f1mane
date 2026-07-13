## 1. Atraso no redesenho dos objetos ao mover a tela pelas setas

- [x] 1.1 Adicionar um campo `javax.swing.Timer` em `MainPanelEditor` (delay 500ms, `setRepeats(false)`) cujo `ActionListener` chama o `repaint()` que hoje é disparado a cada passo de pan.
- [x] 1.2 Em `esquerda()`, `direita()`, `cima()` e `baixo()` (linhas ~1590-1653), manter a atualização imediata de `scrollPane.getViewport().setViewPosition(...)`, mas trocar a chamada de `repaint()` da camada de objetos por `timer.restart()`.
- [x] 1.3 Garantir que o timer é compartilhado entre as quatro direções (uma única instância), de forma que alternar entre setas diferentes reinicie a mesma contagem de 0,5s em vez de disparar múltiplos timers concorrentes.
- [x] 1.4 Conferir que outras chamadas de `repaint()` no arquivo (drag de objeto, seleção na lista, criação/edição de objeto, etc.) continuam chamando `repaint()` normalmente (não passam a reiniciar o timer de pan).
- [x] 1.6 **(Revisado — ver 1.7)** Primeira tentativa: pular `repaint()` durante o pan, exceto no 1º passo do gesto. Abandonada: `desenhaInfo()`/`desenhaControles()` são overlays fixos ao canto da viewport (posição recalculada a cada frame a partir de `limitesViewPort()`), não conteúdo estático em coordenadas de circuito — pular `repaint()` nos passos seguintes do gesto deixava sua posição "presa" ao frame anterior, arrastada pelo blit-scroll da `JViewport` como um artefato visual sobre o fundo/pista.
- [x] 1.7 Mover o adiamento pra dentro de `paintComponent`: `repaint()` volta a ser chamado a cada passo de pan (via `iniciarOuEstenderDebounceRedesenhoPosScroll()`, sempre), mantendo fundo/traçado/overlays fixos à viewport sempre corretos. Só a iteração sobre os objetos do circuito (`todosObjetos()`/`niveisDesenhoOrdenados()`/`desenhaObjetosNivel()` dentro de `paintComponent`) é pulada enquanto o timer está rodando, via novo método `deveDesenharObjetos()` (`!timerRedesenhoObjetosPosScroll.isRunning()`).
- [x] 1.5 Testar manualmente: segurar uma seta por vários segundos, soltar, e confirmar que (a) fundo/traçado/painéis de info-controles não deixam artefato/smear durante o movimento, e (b) os objetos somem durante o movimento e reaparecem corretamente ~0,5s após soltar. *(confirmado manualmente pelo usuário.)*

## 2. Tecla Delete na lista única de objetos

- [x] 2.1 Extrair a lógica atual do `ActionListener` do botão "Remover" em `FormularioListaObjetos` (`defaultListModelOP.remove(sel); atualizarCircuito();`) para um método privado reutilizável (ex.: `removerObjetoSelecionado()`).
- [x] 2.2 Em `iniciarComponentes()`, adicionar um `KeyAdapter` ao `JList list` que, ao receber `VK_DELETE` com um item selecionado, chama esse método (seguindo o padrão já usado em `pistaJList`/`boxJList`).
- [x] 2.3 Garantir que Delete sem seleção na lista não lança exceção nem altera o estado (checar índice de seleção antes de remover).
- [x] 2.4 Confirmar que esse novo caminho não interfere no `KeyAdapter` global de `EditorCircuitos`/`apagarObjetoSelecionado()` (fluxo de Delete via clique no canvas continua funcionando como antes). *(verificado por leitura: o `KeyAdapter` de `EditorCircuitos` está no `JFrame`, só recebe eventos quando o frame é o alvo do foco; o novo `KeyAdapter` está no `JList`, mesmo padrão isolado já usado por `pistaJList`/`boxJList`.)*

## 3. Testes

- [x] 3.1 Ajustar/adicionar testes de `MainPanelEditor` que cobrem `esquerda()/direita()/cima()/baixo()` para verificar o novo comportamento de debounce. *(reescrito em `MainPanelEditorScrollDebounceTest`: `repaint()` chamado em todo passo — via subclasse que conta chamadas —, timer compartilhado entre as 4 direções, e `deveDesenharObjetos()` — extraído e testado via reflection — `false` enquanto o timer está rodando.)*
- [x] 3.2 Adicionar teste cobrindo Delete na lista única com item selecionado removendo o objeto correto da coleção do `Circuito` (`getObjetos()`/`getObjetosCenario()` conforme o tipo).
- [x] 3.3 Adicionar teste cobrindo Delete na lista única sem seleção (nenhuma alteração, sem exceção).
- [x] 3.4 Rodar `mvn test` e confirmar que a suíte completa passa sem abrir diálogos Swing reais (usar `MainPanelEditorTestDouble` quando aplicável, conforme `CLAUDE.md`).
