## ADDED Requirements

### Requirement: Copiar objetos selecionados na lista única
O editor de circuitos SHALL exibir um botão "Copiar Objetos" abaixo de "Colar Cor". Ao ser clicado com um ou mais objetos selecionados na lista única, o editor SHALL guardar uma cópia profunda e independente de cada objeto selecionado numa memória interna do editor, substituindo qualquer conteúdo copiado anteriormente. Sem nenhum objeto selecionado, clicar em "Copiar Objetos" SHALL NOT ter efeito.

#### Scenario: Copiar um único objeto selecionado
- **WHEN** o usuário seleciona um objeto na lista única e clica em "Copiar Objetos"
- **THEN** uma cópia desse objeto é guardada na memória do editor

#### Scenario: Copiar múltiplos objetos selecionados
- **WHEN** o usuário seleciona vários objetos na lista única (seleção múltipla) e clica em "Copiar Objetos"
- **THEN** uma cópia de cada objeto selecionado é guardada na memória do editor

#### Scenario: Copiar sem seleção não tem efeito
- **WHEN** o usuário clica em "Copiar Objetos" sem nenhum objeto selecionado na lista única
- **THEN** a memória de objetos copiados permanece como estava antes do clique

#### Scenario: Copiar novamente substitui o conteúdo anterior
- **WHEN** o usuário copia um conjunto de objetos, depois seleciona um conjunto diferente e clica em "Copiar Objetos" novamente
- **THEN** a memória do editor passa a conter apenas cópias do segundo conjunto, descartando o primeiro

### Requirement: Colar objetos pede um clique no canvas para definir onde nascem
O editor de circuitos SHALL exibir um botão "Colar Objetos" abaixo de "Copiar Objetos". Com pelo menos um objeto na memória de objetos copiados, clicar em "Colar Objetos" SHALL exibir um diálogo informando o usuário que o próximo clique no canvas do circuito define o ponto onde o(s) objeto(s) colado(s) devem aparecer, e SHALL colocar o editor em um modo de posicionamento até esse clique acontecer. Sem nenhum objeto copiado previamente, clicar em "Colar Objetos" SHALL NOT exibir o diálogo nem entrar em modo de posicionamento.

#### Scenario: Colar exibe o diálogo de aviso
- **WHEN** o usuário tem pelo menos um objeto copiado e clica em "Colar Objetos"
- **THEN** um diálogo é exibido informando que o próximo clique no canvas define onde o(s) objeto(s) colado(s) aparecem

#### Scenario: Colar sem nada copiado não exibe diálogo nem tem efeito
- **WHEN** o usuário clica em "Colar Objetos" sem ter copiado nenhum objeto antes
- **THEN** nenhum diálogo é exibido, o editor não entra em modo de posicionamento, e a lista única permanece inalterada

### Requirement: Clique de posicionamento insere as cópias no ponto clicado
Depois do diálogo de "Colar Objetos", o próximo clique do usuário no canvas do circuito SHALL inserir, para cada objeto guardado na memória de objetos copiados, uma nova cópia profunda e independente desse objeto ao final da coleção do `Circuito` correspondente ao seu tipo (`circuito.getObjetosCenario()` para objetos de cenário, `circuito.getObjetos()` para objetos de função), preservando tamanho, ângulo, cores, pontos e demais propriedades do objeto originalmente copiado — exceto a posição, que é definida pelo clique: o primeiro objeto copiado é posicionado com o ponto clicado como seu centro, e os demais objetos da mesma cópia (quando mais de um foi copiado) SHALL manter entre si o mesmo arranjo relativo (mesma distância e direção) que tinham entre si no momento em que foram copiados.

#### Scenario: Clicar após colar um único objeto insere uma cópia centrada no clique
- **WHEN** o usuário copia um objeto, clica em "Colar Objetos", e em seguida clica em um ponto do canvas
- **THEN** um novo objeto do mesmo tipo, com as mesmas propriedades do objeto copiado, é adicionado ao final da coleção do `Circuito` correspondente ao seu tipo, centrado no ponto clicado, e passa a aparecer na lista única

#### Scenario: Clicar após colar múltiplos objetos preserva o arranjo relativo entre eles
- **WHEN** o usuário copia vários objetos selecionados, clica em "Colar Objetos", e em seguida clica em um ponto do canvas
- **THEN** um novo objeto correspondente a cada um dos copiados é adicionado ao final da coleção do `Circuito` do seu respectivo tipo, e a distância/direção entre os objetos recém-colados é igual à distância/direção entre os objetos originais no momento em que foram copiados

#### Scenario: Mover o objeto colado não afeta o objeto original
- **WHEN** o usuário copia um objeto, cola-o num ponto do canvas, e em seguida move o objeto colado (posição, ângulo ou pontos) pelo editor
- **THEN** o objeto originalmente copiado permanece com sua posição/pontos inalterados

#### Scenario: Colar duas vezes seguidas produz dois objetos independentes
- **WHEN** o usuário copia um objeto, clica em "Colar Objetos", clica num ponto do canvas, e repete "Colar Objetos" clicando em outro ponto
- **THEN** dois novos objetos são adicionados ao circuito, cada um centrado no seu respectivo ponto clicado, e mover um deles não afeta o outro

### Requirement: Objeto colado fica imediatamente visível, sem precisar recarregar o circuito
Depois do clique de posicionamento, cada objeto colado SHALL estar pronto para ser desenhado imediatamente — sem depender de nenhuma ação adicional do usuário (repaint manual, interação com outro objeto, ou recarregar o circuito) para aparecer no canvas.

#### Scenario: Objeto colado longe da posição original aparece sem precisar de nenhuma ação adicional
- **WHEN** o usuário copia um objeto, clica em "Colar Objetos", e clica num ponto do canvas distante da posição original do objeto copiado
- **THEN** o objeto colado está pronto para ser desenhado corretamente na nova posição assim que o editor pintar novamente, sem exigir que o usuário recarregue o circuito ou realize qualquer outra ação

### Requirement: Memória de objetos copiados sobrevive à troca de circuito
A memória de objetos copiados pelo botão "Copiar Objetos" SHALL permanecer disponível para "Colar Objetos" mesmo depois de o usuário trocar o circuito carregado na mesma instância do editor (abrir um circuito diferente sem fechar o editor).

#### Scenario: Colar após trocar de circuito na mesma janela do editor
- **WHEN** o usuário copia um ou mais objetos, carrega um circuito diferente na mesma instância do editor, clica em "Colar Objetos" e clica em um ponto do canvas
- **THEN** cópias dos objetos copiados anteriormente são adicionadas ao circuito recém-carregado, centradas no ponto clicado
