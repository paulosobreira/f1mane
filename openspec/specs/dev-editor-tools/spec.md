# Spec: dev-editor-tools

## Purpose

Defines the behavior of developer/editor tooling for source-resource editing: the car color editor (`EditorCoresCarros`) and the circuit editor (`MainFrameEditor`/`MainPanelEditor`). These tools must read and write source files directly under `src/main/resources` (not runtime classpath locations), and must be organized under the `br.f1mane.editor` package.
## Requirements
### Requirement: Car color editor persists to source resources
`EditorCarrosPilotos` (renomeado de `EditorCoresCarros`) SHALL read and write `carros.properties` directly under `src/main/resources/properties/<temporada>/carros.properties`, resolved relative to the project root working directory, and SHALL NOT resolve the file to save via the runtime classpath (`Class.getResource(...)`/`target/classes`/jar entries). When editing a car, the editor SHALL also read and write the `nomeHomenagem` field (11th positional column).

#### Scenario: Saving a single car's colors
- **WHEN** the user edits a car's colors in `EditorCarrosPilotos` and clicks the save button (💾 icon)
- **THEN** the corresponding line in `src/main/resources/properties/<temporada>/carros.properties` is rewritten with the new values, and the change is visible via `git diff` in that file

#### Scenario: Saving all cars in a season
- **WHEN** the user clicks "Salvar Todos" for the currently loaded season
- **THEN** `src/main/resources/properties/<temporada>/carros.properties` is rewritten in place with every modified car's updated values, preserving comments and line order

#### Scenario: Target file missing at expected source path
- **WHEN** `src/main/resources/properties/<temporada>/carros.properties` does not exist relative to the working directory (e.g., tool launched from outside the project root)
- **THEN** the save operation fails with a clear error message shown to the user instead of writing to an unrelated location such as `target/classes`

#### Scenario: Editing a car's tribute name
- **WHEN** the user edits the `nomeHomenagem` field for a car in `EditorCarrosPilotos` and saves
- **THEN** the corresponding line in `carros.properties` has its 11th field updated to the new value, without altering the other 10 fields

### Requirement: Car color editor uses previous/next season navigation
`EditorCarrosPilotos` SHALL let the user switch between seasons using "previous" and "next" navigation controls instead of a dropdown combo box, operating over the same ordered list of available seasons, shared between the "carros" and "pilotos" editing modes.

#### Scenario: Navigating to the next season
- **WHEN** the user clicks the "next season" control while a season other than the last is selected
- **THEN** the editor loads and displays the next season in the ordered list, and the "next season" control is disabled when the last season is reached

#### Scenario: Navigating to the previous season
- **WHEN** the user clicks the "previous season" control while a season other than the first is selected
- **THEN** the editor loads and displays the previous season in the ordered list, and the "previous season" control is disabled when the first season is reached

### Requirement: Circuit editor persists to source resources
The circuit editor (`EditorCircuitos` opening `MainPanelEditor`) SHALL default file-open and file-save dialogs for circuit images and circuit XML files to `src/main/resources/circuitos`, resolved relative to the project root working directory, and SHALL NOT default to a directory derived from the runtime classpath location of a compiled class.

#### Scenario: Opening an existing circuit for editing
- **WHEN** the user navigates to an existing circuit using the "Anterior"/"Próximo" buttons in the circuit editor
- **THEN** the editor loads the corresponding XML from `src/main/resources/circuitos` (resolved via `circuitos.properties`), without opening a `JFileChooser`

#### Scenario: Creating a new circuit still starts from a background image
- **WHEN** the user clicks "Criar Nova" and picks a background reference image
- **THEN** the file chooser opens with `src/main/resources/circuitos` as its starting directory, filtered to image files, exactly as before this change

#### Scenario: Saving a circuit for the first time
- **WHEN** the user creates or edits a circuit and clicks "Salvar Pista Atual" (or presses F8) without a file already associated
- **THEN** the save file chooser opens with `src/main/resources/circuitos` as its starting directory, and confirming the dialog writes the circuit XML to the selected path under that directory by default

### Requirement: Editor de circuitos não usa menu, apenas botões de topo
O editor de circuitos (`EditorCircuitos`) SHALL NOT exibir um `JMenuBar`; as únicas ações de topo disponíveis SHALL ser os botões "Salvar Pista Atual" e "Criar Nova".

#### Scenario: Menu bar removido
- **WHEN** a janela do editor de circuitos é aberta
- **THEN** nenhum `JMenuBar` está presente, e o frame expõe exatamente dois controles de ação de topo: "Salvar Pista Atual" e "Criar Nova"

#### Scenario: Salvar Pista Atual substitui o antigo item de menu
- **WHEN** o usuário clica em "Salvar Pista Atual"
- **THEN** o mesmo comportamento de salvamento antes disparado pelo item de menu "Salvar Pista F8" é executado (o atalho de teclado F8 continua disparando a mesma ação)

#### Scenario: Criar Nova substitui o antigo item de menu
- **WHEN** o usuário clica em "Criar Nova"
- **THEN** o mesmo fluxo de criação de circuito antes disparado pelo item de menu "Criar Circuito" é executado (escolha de imagem de fundo via file chooser)

### Requirement: Navegação Anterior/Próximo e combobox para selecionar circuito existente
O editor de circuitos SHALL permitir navegar pelos circuitos existentes com controles "Anterior"/"Próximo" alimentados pela lista de `circuitos.properties` (com fallback de varredura de `src/main/resources/circuitos/*.xml` caso a properties não possa ser lida), replicando o padrão de navegação por temporada já usado em `EditorCoresCarros`, em vez de um `JFileChooser` bruto. Adicionalmente, o editor SHALL exibir essa mesma lista em um combobox posicionado onde hoje aparece o rótulo do circuito atual, mostrando o nome amigável de cada circuito (parseado de `circuitos.properties`) como texto visível, com o nome do arquivo XML como valor interno de seleção; selecionar um item no combobox SHALL carregar esse circuito exatamente como Anterior/Próximo carregam o circuito adjacente, e Anterior/Próximo SHALL manter o combobox sincronizado com o índice atual sem disparar recarregamento duplicado.

#### Scenario: Navegar para o próximo circuito
- **WHEN** o usuário clica em "Próximo" estando em um circuito que não é o último da lista
- **THEN** o editor carrega e exibe o próximo circuito da lista ordenada, o controle "Próximo" fica desabilitado ao alcançar o último circuito, e o combobox passa a mostrar o nome amigável do novo circuito selecionado

#### Scenario: Navegar para o circuito anterior
- **WHEN** o usuário clica em "Anterior" estando em um circuito que não é o primeiro da lista
- **THEN** o editor carrega e exibe o circuito anterior da lista ordenada, o controle "Anterior" fica desabilitado ao alcançar o primeiro circuito, e o combobox passa a mostrar o nome amigável do novo circuito selecionado

#### Scenario: Fallback quando circuitos.properties não pode ser lido
- **WHEN** `properties/circuitos.properties` não pode ser lido
- **THEN** o editor monta a lista navegável (e as opções do combobox) varrendo diretamente os arquivos `src/main/resources/circuitos/*.xml`, usando o nome do arquivo como nome amigável nesse caso

#### Scenario: Combobox mostra nome amigável, não o arquivo XML
- **WHEN** o editor de circuitos exibe a lista de circuitos disponíveis no combobox
- **THEN** cada item mostra o nome amigável do circuito (ex.: "Albert Park"), não o nome do arquivo XML (ex.: `albert_park_mro.xml`)

#### Scenario: Selecionar um circuito no combobox carrega esse circuito
- **WHEN** o usuário seleciona um item diferente do atual no combobox de circuitos
- **THEN** o editor carrega o circuito correspondente ao arquivo XML associado a esse item, do mesmo jeito que Anterior/Próximo carregam um circuito adjacente

### Requirement: Editor de circuitos não exibe botão de créditos
O editor de circuitos (`EditorCircuitos`/`MainPanelEditor`) SHALL NOT exibir um botão "Créditos" nem oferecer um fluxo de edição de posição de créditos na imagem do circuito.

#### Scenario: Botão de créditos removido
- **WHEN** a janela do editor de circuitos é aberta após esta mudança
- **THEN** nenhum botão "Créditos" está presente entre os controles de topo, e clicar em qualquer ponto do canvas não aciona mais um fluxo de definição de posição de créditos

### Requirement: Editor tools are grouped under the editor package
`EditorCoresCarros` and `MainFrameEditor` SHALL live in the `br.f1mane.editor` package, alongside the other source-resource editing tools (`MainPanelEditor`, `FormularioObjetos`, `FormularioListaObjetos`).

#### Scenario: Package location after the change
- **WHEN** inspecting the compiled classes of the project after this change
- **THEN** `EditorCoresCarros` resolves to `br.f1mane.editor.EditorCoresCarros` and `MainFrameEditor` resolves to `br.f1mane.editor.MainFrameEditor`, with no remaining references to the old `br.f1mane.util.EditorCoresCarros` or `br.f1mane.MainFrameEditor` fully-qualified names anywhere in `src/main/java`

### Requirement: Controles de nós de pista ficam num único grid de 1 coluna
Os controles da seção de nós de pista do editor de circuitos (tipo de nó, lado do box, lado de saída do box, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada") SHALL ficar organizados num único painel de grid de 1 coluna (uma linha por controle), em vez de sub-painéis separados que hoje misturam arranjo empilhado com arranjo lado a lado.

#### Scenario: Controles de nós aparecem um por linha
- **WHEN** o editor de circuitos é aberto
- **THEN** tipo de nó, lado do box, lado de saída do box, e os botões de apagar nó aparecem como linhas distintas de um único grid de 1 coluna, na seção de nós de pista

### Requirement: Linhas da barra superior do editor têm altura visual consistente
As linhas visuais da barra superior do editor de circuitos (`gerarTopoNavegacaoEAcoes`) que hoje ficam com alturas diferentes — a linha com nome do circuito/ativo/% chuva e a linha seguinte (largura da pista/noite/cores) — SHALL ter a mesma altura.

#### Scenario: Duas linhas de campos têm a mesma altura
- **WHEN** a barra superior do editor de circuitos é renderizada com largura suficiente para quebrar em pelo menos duas linhas visuais
- **THEN** a linha com nome do circuito/ativo/% chuva e a linha com largura da pista/noite/cores têm a mesma altura

### Requirement: Listas de objetos têm rótulos identificando desenho e função
A seção de objetos do editor de circuitos SHALL exibir um rótulo "Objetos Desenho" acima da lista de objetos de cenário (`objetosCenario`) e um rótulo "Objetos Função" acima da lista de objetos de função (`objetos`).

#### Scenario: Rótulos aparecem acima de cada lista
- **WHEN** a seção de objetos do editor de circuitos é aberta
- **THEN** o rótulo "Objetos Desenho" aparece acima da lista de cima (objetos de cenário) e o rótulo "Objetos Função" aparece acima da lista de baixo (objetos de função)

### Requirement: Editor de carros/pilotos tem um seletor de modo de edição

`EditorCarrosPilotos` SHALL expor um seletor (ex.: toggle ou combobox) no topo da janela pra alternar entre editar "Carros" (comportamento existente, com o campo `nomeHomenagem` novo) e editar "Pilotos" (nova visão). A troca de modo SHALL preservar a temporada atualmente selecionada.

#### Scenario: Alternar pro modo de edição de pilotos
- **WHEN** o usuário seleciona "Pilotos" no seletor de modo, estando numa temporada X
- **THEN** o editor exibe um card por piloto daquela mesma temporada X, sem precisar navegar de novo pra ela

#### Scenario: Alternar de volta pro modo de edição de carros
- **WHEN** o usuário seleciona "Carros" no seletor de modo, estando numa temporada X
- **THEN** o editor volta a exibir um card por carro daquela mesma temporada X, com os campos de cor e `nomeHomenagem` como antes

### Requirement: Editor de pilotos edita apenas `nomeHomenagem` e `habilidade`, com imagem somente leitura

No modo de edição de pilotos, `EditorCarrosPilotos` SHALL exibir, por piloto da temporada selecionada, a imagem do carro e do capacete daquele piloto (com as cores reais do carro associado) como somente leitura, e SHALL permitir editar apenas `nomeHomenagem` e `habilidade` (a habilidade daquele piloto naquela temporada).

#### Scenario: Editar o nome-homenagem de um piloto
- **WHEN** o usuário edita o campo `nomeHomenagem` de um piloto no modo de edição de pilotos e salva
- **THEN** a linha correspondente em `pilotos.properties` daquela temporada tem seu 3º campo atualizado, sem alterar `Carro`/`Habilidade`

#### Scenario: Editar a habilidade de um piloto
- **WHEN** o usuário edita o campo `habilidade` de um piloto no modo de edição de pilotos e salva
- **THEN** a linha correspondente em `pilotos.properties` daquela temporada tem seu 2º campo (`Habilidade`) atualizado

#### Scenario: Imagem do piloto não é editável
- **WHEN** o usuário está no modo de edição de pilotos
- **THEN** não há nenhum controle pra alterar a imagem/cor do carro ou capacete exibidos pro piloto — só campos de texto pra `nomeHomenagem` e `habilidade`

### Requirement: Alteração de `nomeHomenagem` pode ser propagada pra todas as temporadas da mesma identidade

Sempre que o usuário alterar o campo `nomeHomenagem` de um carro ou de um piloto, `EditorCarrosPilotos` SHALL exibir um checkbox (ex.: "Aplicar a todas as temporadas") próximo ao campo. Quando o usuário salvar com o checkbox marcado, a mudança SHALL ser aplicada a `nomeHomenagem` de toda linha, em qualquer `carros.properties`/`pilotos.properties` de qualquer temporada, que compartilhe a mesma identidade do item editado (nome canônico da imagem, pra carro; chave real, pra piloto — ver `nome-homenagem-carros`/`nome-homenagem-pilotos`). Quando o checkbox estiver desmarcado (padrão), a mudança SHALL afetar apenas a linha da temporada atualmente selecionada, como hoje.

#### Scenario: Salvar só pra temporada atual (checkbox desmarcado)
- **WHEN** o usuário edita `nomeHomenagem` de um carro/piloto, deixa o checkbox desmarcado, e salva
- **THEN** só a linha correspondente na temporada atualmente selecionada é atualizada; outras temporadas com a mesma identidade permanecem com o valor anterior

#### Scenario: Propagar pra todas as temporadas (checkbox marcado) — carro
- **WHEN** o usuário edita `nomeHomenagem` de um carro cujo nome de imagem canonicaliza pra `mclaren` (ex.: presente como `McLaren-MP4/1` numa temporada e `McLaren-MP4/4` noutra), marca o checkbox, e salva
- **THEN** toda linha de carro, em toda temporada, cujo nome de imagem também canonicalize pra `mclaren` tem seu `nomeHomenagem` atualizado pro mesmo novo valor

#### Scenario: Propagar pra todas as temporadas (checkbox marcado) — piloto
- **WHEN** o usuário edita `nomeHomenagem` de um piloto com uma chave real que aparece em `pilotos.properties` de múltiplas temporadas, marca o checkbox, e salva
- **THEN** toda linha de piloto, em toda temporada, com essa mesma chave real tem seu `nomeHomenagem` atualizado pro mesmo novo valor

#### Scenario: Propagação não afeta outros campos
- **WHEN** a propagação pra todas as temporadas é aplicada
- **THEN** apenas o campo `nomeHomenagem` das linhas correspondentes é alterado — cores, potência, imagem, carro associado, habilidade, e demais campos de cada linha permanecem intocados

