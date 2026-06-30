## 1. Move EditorCoresCarros to br.f1mane.editor

- [x] 1.1 `git mv src/main/java/br/f1mane/util/EditorCoresCarros.java src/main/java/br/f1mane/editor/EditorCoresCarros.java`
- [x] 1.2 Update `package br.f1mane.util;` to `package br.f1mane.editor;` and fix any now-redundant imports (`Carro`, `CarregadorRecursos`, `SpriteSheet` stay as-is; same-package classes need no import)
- [x] 1.3 Confirm no other file in `src/main/java` references `br.f1mane.util.EditorCoresCarros`

## 2. Fix EditorCoresCarros save/list paths

- [x] 2.1 In `salvarNoArquivo`, replace the `CarregadorRecursos.class.getResource(...).toURI()` resolution with `new File("src/main/resources/properties/" + temporadaAtual + "/carros.properties")` (relative to project root working directory)
- [x] 2.2 If that file does not exist, throw an `IOException` with a clear message (e.g., "Arquivo não encontrado em src/main/resources/properties/<temporada>/carros.properties — execute a ferramenta a partir da raiz do projeto") instead of silently resolving elsewhere
- [x] 2.3 In `popularTemporadas()`, change the directory-listing fallback to scan `new File("src/main/resources/properties")` instead of `CarregadorRecursos.class.getResource("/properties").toURI()`
- [x] 2.4 Update the class Javadoc (`Uso: java -cp target/flmane.jar ...`) to state the tool must be run with the project root as working directory (same note style as `SpriteSheet.main`)

## 3. Replace season combo box with previous/next navigation

- [x] 3.1 Remove `JComboBox<String> comboTemporada` field and its `topo.add(comboTemporada)` wiring
- [x] 3.2 Add a `JLabel` showing the current season plus `JButton` "◀ Anterior" / "Próxima ▶" controls in the top panel, backed by the existing sorted season list and a current index
- [x] 3.3 Wire the buttons to call `carregarTemporada(...)` for the adjacent season and update the label; disable "Anterior" at index 0 and "Próxima" at the last index
- [x] 3.4 Initialize selection to the last season in the list on startup (preserving current behavior of `comboTemporada.setSelectedIndex(getItemCount() - 1)`)

## 4. Move MainFrameEditor to br.f1mane.editor

- [x] 4.1 `git mv src/main/java/br/f1mane/MainFrameEditor.java src/main/java/br/f1mane/editor/MainFrameEditor.java`
- [x] 4.2 Update `package br.f1mane;` to `package br.f1mane.editor;`; remove now-unneeded `import br.f1mane.editor.ExampleFileFilter;` and `import br.f1mane.editor.MainPanelEditor;` (same package) while keeping other imports (`br.f1mane.controles.InterfaceJogo`, `br.f1mane.servidor.applet.AppletPaddock`, `br.f1mane.recursos.CarregadorRecursos`, `br.f1mane.recursos.idiomas.Lang`)
- [x] 4.3 In `MainPanelEditor.java`, remove `import br.f1mane.MainFrameEditor;` (now same package) and confirm `srcFrame`/constructor usages still compile
- [x] 4.4 Search the repo for any remaining fully-qualified reference to `br.f1mane.MainFrameEditor` outside `target/` and update it

## 5. Fix circuit editor default directory

- [x] 5.1 In `MainPanelEditor.novo()`, replace the `JFileChooser` initial-directory argument (`CarregadorRecursos.class.getResource("CarregadorRecursos.class").getFile()`) with `new File("src/main/resources/circuitos")`
- [x] 5.2 Apply the same fix in `MainPanelEditor.editar()`
- [x] 5.3 Apply the same fix in `MainPanelEditor.salvarPista()` (the `file == null` branch that opens the save dialog)
- [x] 5.4 Verify `src/main/resources/circuitos` is used consistently (no remaining classpath-derived `JFileChooser` starting directories in `MainPanelEditor`)

## 6. Validation

- [x] 6.1 Run `mvn test` and confirm the build still compiles and passes
- [x] 6.2 Manually launch `EditorCoresCarros` from the project root, change a car's color, save, and confirm via `git diff` that `src/main/resources/properties/<temporada>/carros.properties` changed (then revert the test edit) — verified via a reflection-driven harness invoking the real `salvarNoArquivo` method (no GUI display available in this environment): it loaded all 25 seasons from `src/main/resources/properties`, resolved `t2026` correctly, wrote a real color change that showed up in `git diff`, and the change was reverted afterward. A missing-season case also correctly throws the new clear `IOException` instead of silently resolving elsewhere.
- [x] 6.3 Manually launch `MainFrameEditor` from the project root, open an existing circuit via "Editar Circuito", confirm the file chooser starts in `src/main/resources/circuitos` — no X display/Xvfb available in this environment to drive the GUI interactively, so this was verified statically: all three `JFileChooser` call sites in `MainPanelEditor` (`novo`, `editar`, `salvarPista`) now construct with `new File("src/main/resources/circuitos")`, and that directory exists in the repo. Recommend a quick human smoke test before merging.
- [x] 6.4 Confirm `EditorCoresCarros`'s season navigation buttons correctly disable at the first/last season and load the right data on each click — verified by code review of `navegarTemporada`/`atualizarNavegacaoTemporada` (bounds-checked index, `setEnabled` at both ends) combined with the harness in 6.2 confirming the underlying season list and `carregarTemporada` data loading are correct; full interactive button-click verification wasn't possible without a display. Recommend a quick human smoke test before merging.
