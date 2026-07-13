## Why

Movimentar a tela do editor de circuitos pelas setas do teclado redesenha todos os objetos do circuito a cada passo, o que degrada a performance em circuitos com muitos objetos. Além disso, ao selecionar um objeto na lista única do editor (`FormularioListaObjetos`), a tecla Delete não faz nada — a remoção via teclado só funciona quando o objeto foi clicado no canvas, obrigando o usuário a usar o botão "Remover" na lista.

## What Changes

- Ao mover a tela pelas setas do teclado (pan via `MainPanelEditor.esquerda()/direita()/cima()/baixo()`), o redesenho completo dos objetos do circuito passa a ser adiado (debounce) por 0,5 segundos após o último movimento, em vez de redesenhar a cada passo de scroll.
- Durante o período de scroll ativo (antes do debounce disparar), o canvas continua respondendo ao movimento da viewport, mas sem repintar a camada de objetos a cada passo.
- Adiciona um `KeyListener` na `JList` de `FormularioListaObjetos` para a tecla Delete: com um objeto selecionado na lista, Delete remove esse objeto (mesma lógica do botão "Remover" já existente), sem exigir que o objeto também esteja selecionado via clique no canvas (`objetoPista`).

## Capabilities

### New Capabilities
- `editor-scroll-atraso-desenho`: adia o redesenho dos objetos do circuito por 0,5 segundos após o usuário parar de mover a tela pelas setas do teclado, reduzindo repaints durante o scroll.

### Modified Capabilities
- `editor-lista-objetos-filtro-tipo`: a tecla Delete, com foco na lista única de objetos e um item selecionado, passa a remover o objeto selecionado (mesmo comportamento do botão "Remover").

## Impact

- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: métodos `esquerda()`, `direita()`, `cima()`, `baixo()` (pan por seta) e o ponto de repaint da camada de objetos.
- `src/main/java/br/f1mane/editor/FormularioListaObjetos.java`: `iniciarComponentes()` (onde o `JList list` é configurado) e reuso da lógica de remoção já usada pelo botão "Remover" (`atualizarCircuito()`).
- Testes existentes que exercitam pan por seta e o filtro/lista de objetos (`MainPanelEditor*Test`, `FormularioListaObjetos*Test` se existirem) podem precisar de ajuste para o novo debounce.
