## MODIFIED Requirements

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

## ADDED Requirements

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

### Requirement: Navegação Anterior/Próximo para selecionar circuito existente
O editor de circuitos SHALL permitir navegar pelos circuitos existentes com controles "Anterior"/"Próximo" alimentados pela lista de `circuitos.properties` (com fallback de varredura de `src/main/resources/circuitos/*.xml` caso a properties não possa ser lida), replicando o padrão de navegação por temporada já usado em `EditorCoresCarros`, em vez de um `JFileChooser` bruto.

#### Scenario: Navegar para o próximo circuito
- **WHEN** o usuário clica em "Próximo" estando em um circuito que não é o último da lista
- **THEN** o editor carrega e exibe o próximo circuito da lista ordenada, e o controle "Próximo" fica desabilitado ao alcançar o último circuito

#### Scenario: Navegar para o circuito anterior
- **WHEN** o usuário clica em "Anterior" estando em um circuito que não é o primeiro da lista
- **THEN** o editor carrega e exibe o circuito anterior da lista ordenada, e o controle "Anterior" fica desabilitado ao alcançar o primeiro circuito

#### Scenario: Fallback quando circuitos.properties não pode ser lido
- **WHEN** `properties/circuitos.properties` não pode ser lido
- **THEN** o editor monta a lista navegável varrendo diretamente os arquivos `src/main/resources/circuitos/*.xml`
