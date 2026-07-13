## Context

`MainPanelEditor` (canvas do editor de circuitos, `src/main/java/br/f1mane/editor/MainPanelEditor.java`) é um `JPanel` sem thread de render próprio: tudo é desenhado em `paintComponent()`, disparado sincronamente na EDT sempre que `repaint()` é chamado. O pan por seta (`esquerda()/direita()/cima()/baixo()`, linhas ~1590-1653) move a `viewPosition` do `JScrollPane` em passos de 40px e chama `repaint()` a cada passo — segurar uma seta gera dezenas de repaints completos por segundo, cada um percorrendo `desenhaObjetosNivel` para todos os objetos visíveis (já com o corte de viewport de `editor-viewport-culling`, mas ainda assim custoso em circuitos densos).

Na lista única de objetos (`FormularioListaObjetos`, feature de `editor-lista-objetos-filtro-tipo`), o `JList list` não tem `KeyListener` próprio. A tecla Delete só é tratada pelo `KeyAdapter` global de `EditorCircuitos`, que chama `MainPanelEditor.apagarObjetoSelecionado()` — método que opera sobre o campo `objetoPista` (setado por clique no canvas), não sobre a seleção da `JList`. Precedente já existe: `pistaJList`/`boxJList` (listas de nós) têm cada uma seu próprio `KeyAdapter` com tratamento de `VK_DELETE`.

## Goals / Non-Goals

**Goals:**
- Reduzir a quantidade de repaints completos da camada de objetos durante o pan por seta, sem quebrar o movimento visual da viewport em si.
- Redesenhar os objetos uma única vez, 0,5 segundos após o usuário parar de pressionar/segurar as setas.
- Permitir remover o objeto selecionado na `JList` de `FormularioListaObjetos` pressionando Delete com foco nela, reaproveitando a lógica já existente do botão "Remover".

**Non-Goals:**
- Não altera o comportamento de pan em si (distância por passo, teclas Alt/Shift para mover/redimensionar objeto selecionado).
- Não altera `apagarObjetoSelecionado()` nem o fluxo de Delete via clique no canvas (`objetoPista`) — o novo `KeyListener` da lista é um caminho adicional, não uma substituição.
- Não introduz threads de render em background; o debounce usa `javax.swing.Timer`, que já dispara na EDT.

## Decisions

**O debounce protege só a iteração sobre os objetos, dentro de `paintComponent` — não o `repaint()` em si.**
Uma primeira versão desta feature pulava a própria chamada de `repaint()` durante o pan (só chamando `timer.restart()`), e isso quebrou `desenhaInfo()`/`desenhaControles()`: esses dois métodos desenham overlays (painéis de HUD com texto/atalhos) fixos ao canto da viewport, recalculando sua posição a cada chamada a partir de `limitesViewPort()` — ou seja, ao contrário do resto do canvas (fundo, traçado, objetos), eles NÃO são conteúdo estático em coordenadas de circuito. O scroll da `JViewport` usa blit (`copyArea`) para deslocar os pixels já pintados e só pede repaint automático da faixa recém-exposta; para conteúdo estático isso é correto (a mesma pista, no mesmo lugar, só a janela visível mudou). Mas para os overlays pinados à viewport, o blit arrasta a posição "congelada" do frame anterior junto com o resto do canvas, em vez de recalculá-la — um artefato visual visível sobre o fundo/pista enquanto o usuário segura a seta.
A correção: `repaint()` volta a ser chamado a cada passo de pan, normalmente (via `iniciarOuEstenderDebounceRedesenhoPosScroll()`), então `paintComponent` roda a cada passo e os overlays fixos à viewport são sempre recalculados corretamente. O que fica condicionado ao debounce é só a iteração sobre `todosObjetos()`/`niveisDesenhoOrdenados()`/`desenhaObjetosNivel()` dentro de `paintComponent` — isolada em `deveDesenharObjetos()` (`!timerRedesenhoObjetosPosScroll.isRunning()`) — que é o custo que efetivamente motivou o pedido original ("percorrer todos os objetos visíveis" em circuitos densos). Enquanto um gesto de pan está em andamento (timer rodando), essa camada simplesmente não é desenhada nesse frame (não fica "parada" da posição anterior — como todo o resto do frame é redesenhado do zero a cada `paintComponent`, os objetos ficam ausentes até o debounce disparar); ao soltar a seta, 0,5s depois o timer dispara, chama `repaint()`, e como não há mais gesto em andamento `deveDesenharObjetos()` volta a `true`.
- Alternativa considerada (versão anterior deste design): pular o próprio `repaint()` durante o pan, com um repaint imediato só no primeiro passo do gesto — rejeitada porque não resolve o artefato do HUD pinado à viewport nos passos seguintes do mesmo gesto (só no primeiro).
- Alternativa considerada: mover `desenhaInfo`/`desenhaControles` pra fora do `paintComponent` do canvas, como componentes Swing reais posicionados sobre o `JViewport` (ex.: `scrollPane.setCorner(...)` ou um glass pane) — resolveria o artefato "pela raiz" (deixariam de fazer parte do raster afetado pelo blit-scroll), mas é um refactor bem maior; não adotado nesta mudança porque manter `repaint()` a cada passo (barato, já que a parte cara — os objetos — está protegida) já resolve o artefato relatado sem mexer na estrutura de desenho existente.
- Alternativa considerada: usar `Thread.sleep`/`ScheduledExecutorService` para o debounce — rejeitada porque exigiria sincronizar de volta com a EDT para chamar `repaint()`, sem ganho sobre `javax.swing.Timer`, que já roda na EDT e é o padrão Swing para isso.

**Escopo do debounce: apenas a iteração sobre objetos originada do pan por seta (`esquerda/direita/cima/baixo`), não outras causas de redesenho.**
Outras chamadas de `repaint()` no arquivo (drag de mouse, seleção na lista, criação/edição de objeto) continuam fora do controle de `deveDesenharObjetos()` no sentido de que não reiniciam o timer — mas, como a checagem do timer está dentro de `paintComponent`, se uma dessas ações disparar um repaint enquanto o timer de um pan recente ainda está rodando, os objetos também ficariam temporariamente ausentes nesse frame. Na prática isso é um caso raro (editar/arrastar um objeto enquanto ainda se está no meio de um gesto de pan) e não afeta a resposta ao clique/drag em si, só adia por até 0,5s o redesenho visual dos demais objetos nesse cenário combinado — aceito como trade-off menor, não coberto por teste dedicado.

**Delete na `JList` reaproveita a lógica do botão "Remover", não `apagarObjetoSelecionado()`.**
O novo `KeyAdapter` em `FormularioListaObjetos.list` (em `iniciarComponentes()`) trata `VK_DELETE` chamando a mesma lógica do `ActionListener` do botão "Remover" (`defaultListModelOP.remove(sel); atualizarCircuito();`), extraída para um método privado reutilizável. Isso mantém consistência com o padrão já usado em `pistaJList`/`boxJList` e evita duplicar/alterar `MainPanelEditor.apagarObjetoSelecionado()`, que continua servindo exclusivamente o fluxo de seleção por clique no canvas.

## Risks / Trade-offs

- [Objetos ficam completamente ausentes do canvas durante um gesto de pan (em vez de mostrar sua posição anterior), reaparecendo só 0,5s depois de parar] → Mitigação: é o comportamento aceito para esta mudança — o objetivo original era reduzir custo durante o scroll, e ausência total (com o resto do frame — fundo, traçado, HUD — sempre correto) é mais barata e visualmente mais limpa do que manter objetos desenhados na posição errada.
- [`repaint()` a cada passo de pan reintroduz o custo do resto do `paintComponent` — fundo, traçado, HUD — que a versão original desta mudança tentava eliminar por completo] → Mitigação: esse custo é o que já existia antes desta feature inteira (pré-existente, não é o alvo do pedido original); o pedido era especificamente sobre o custo de "percorrer todos os objetos visíveis", que é o que de fato fica protegido pelo debounce.
- [Editar/arrastar um objeto com o mouse enquanto o timer de um pan recente ainda está rodando (gesto combinado raro) deixa a camada de objetos ausente durante esse intervalo, mesmo fora do pan em si] → Mitigação: trade-off aceito, não coberto por teste dedicado — cenário raro (usuário solta a seta e imediatamente clica um objeto antes dos 0,5s), sem perda de dados, só um atraso visual adicional.
- [Timer de 0,5s compartilhado entre os quatro métodos de pan pode não disparar se o usuário alternar rapidamente entre setas diferentes sem pausa] → Mitigação: é o comportamento desejado — qualquer novo passo de pan (em qualquer direção) reinicia o timer, então o redesenho dos objetos só ocorre 0,5s após o *último* movimento, independente da direção.
- [Testes existentes de `MainPanelEditor` que chamam `esquerda()/direita()/cima()/baixo()` e verificam `repaint()` imediato podem quebrar] → Mitigação: ajustados para verificar `repaint()` a cada passo e o estado de `deveDesenharObjetos()`/timer, em vez de assumir que o redesenho da camada de objetos é imediato (ver `MainPanelEditorScrollDebounceTest`).

## Migration Plan

Mudança aditiva e local ao editor (ferramenta de desenvolvimento, não afeta runtime do jogo/servidor). Sem dado persistido, sem rollback especial: reverter o commit é suficiente caso necessário.

## Open Questions

Nenhuma em aberto — o requisito de negócio (0,5 segundos, gatilho por parada do scroll) foi confirmado diretamente pelo usuário.
