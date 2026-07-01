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
The circuit editor (`MainFrameEditor` opening `MainPanelEditor`) SHALL default file-open and file-save dialogs for circuit images and circuit XML files to `src/main/resources/circuitos`, resolved relative to the project root working directory, and SHALL NOT default to a directory derived from the runtime classpath location of a compiled class.

#### Scenario: Opening an existing circuit for editing
- **WHEN** the user selects "Editar Circuito" from the editor menu
- **THEN** the file chooser opens with `src/main/resources/circuitos` as its starting directory

#### Scenario: Saving a circuit for the first time
- **WHEN** the user creates or edits a circuit and triggers "Salvar Pista" (F8) without a file already associated
- **THEN** the save file chooser opens with `src/main/resources/circuitos` as its starting directory, and confirming the dialog writes the circuit XML to the selected path under that directory by default

### Requirement: Editor tools are grouped under the editor package
`EditorCoresCarros` and `MainFrameEditor` SHALL live in the `br.f1mane.editor` package, alongside the other source-resource editing tools (`MainPanelEditor`, `FormularioObjetos`, `FormularioListaObjetos`).

#### Scenario: Package location after the change
- **WHEN** inspecting the compiled classes of the project after this change
- **THEN** `EditorCoresCarros` resolves to `br.f1mane.editor.EditorCoresCarros` and `MainFrameEditor` resolves to `br.f1mane.editor.MainFrameEditor`, with no remaining references to the old `br.f1mane.util.EditorCoresCarros` or `br.f1mane.MainFrameEditor` fully-qualified names anywhere in `src/main/java`
