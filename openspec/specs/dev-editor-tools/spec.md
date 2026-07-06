# Spec: dev-editor-tools

## Purpose

Defines the behavior of developer/editor tooling for source-resource editing: the car color editor (`EditorCoresCarros`) and the circuit editor (`MainFrameEditor`/`MainPanelEditor`). These tools must read and write source files directly under `src/main/resources` (not runtime classpath locations), and must be organized under the `br.f1mane.editor` package.

## Requirements

### Requirement: Car color editor persists to source resources
`EditorCoresCarros` SHALL read and write `carros.properties` directly under `src/main/resources/properties/<temporada>/carros.properties`, resolved relative to the project root working directory, and SHALL NOT resolve the file to save via the runtime classpath (`Class.getResource(...)`/`target/classes`/jar entries).

#### Scenario: Saving a single car's colors
- **WHEN** the user edits a car's colors in `EditorCoresCarros` and clicks "Salvar"
- **THEN** the corresponding line in `src/main/resources/properties/<temporada>/carros.properties` is rewritten with the new values, and the change is visible via `git diff` in that file

#### Scenario: Saving all cars in a season
- **WHEN** the user clicks "Salvar Todos" for the currently loaded season
- **THEN** `src/main/resources/properties/<temporada>/carros.properties` is rewritten in place with every modified car's updated values, preserving comments and line order

#### Scenario: Target file missing at expected source path
- **WHEN** `src/main/resources/properties/<temporada>/carros.properties` does not exist relative to the working directory (e.g., tool launched from outside the project root)
- **THEN** the save operation fails with a clear error message shown to the user instead of writing to an unrelated location such as `target/classes`

### Requirement: Car color editor uses previous/next season navigation
`EditorCoresCarros` SHALL let the user switch between seasons using "previous" and "next" navigation controls instead of a dropdown combo box, operating over the same ordered list of available seasons.

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
