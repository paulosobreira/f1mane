## Why

`EditorCoresCarros` (cores/atributos de carros) e o editor de circuito acessado via `MainFrameEditor`/`MainPanelEditor` são ferramentas de uso exclusivo de desenvolvimento para alterar os recursos-fonte do jogo (`src/main/resources/properties/**/carros.properties` e `src/main/resources/circuitos/*.xml`). Hoje, ambas resolvem o caminho dos arquivos via `Class.getResource(...).toURI()`, o que aponta para o classpath de execução (`target/classes` quando rodado via IDE, ou um `jar:` URI inválido quando rodado via `java -cp target/flmane.jar`, lançando `IllegalArgumentException`). Na prática, alterações feitas nessas ferramentas nunca chegam a `src/main/resources`: ou falham com exceção, ou são gravadas em `target/classes`, que é descartável e apagado em `mvn clean`, nunca aparecendo em `git diff`. Além disso, essas classes utilitárias de desenvolvimento estão hoje fora do pacote `br.f1mane.editor`, que é o pacote já usado para as demais ferramentas de edição de recursos do jogo (`MainPanelEditor`, `FormularioObjetos`, etc.), e a seleção de temporada do `EditorCoresCarros` usa um combobox que não corresponde ao padrão de navegação prev/next pedido para esse fluxo.

## What Changes

- Corrigir `EditorCoresCarros` para gravar sempre em `src/main/resources/properties/<temporada>/carros.properties` (caminho relativo à raiz do projeto), em vez de resolver o caminho via classpath/`target`.
- Mover `EditorCoresCarros` de `br.f1mane.util` para o pacote `br.f1mane.editor`.
- Substituir o `JComboBox` de seleção de temporada do `EditorCoresCarros` por botões "Temporada anterior" / "Próxima temporada".
- Corrigir o editor de circuito (`MainPanelEditor`, usado a partir de `MainFrameEditor`) para que abrir (`editar`/`novo`) e salvar (`salvarPista`) circuitos tenham como diretório padrão `src/main/resources/circuitos` (caminho relativo à raiz do projeto), em vez do diretório do `.class` resolvido via classpath (que aponta para `target/classes` ou falha dentro do jar).
- Mover `MainFrameEditor` de `br.f1mane` para o pacote `br.f1mane.editor`, ajustando o import correspondente em `MainPanelEditor`.
- **BREAKING**: o nome totalmente qualificado de `MainFrameEditor` muda de `br.f1mane.MainFrameEditor` para `br.f1mane.editor.MainFrameEditor` — qualquer script/comando externo que invoque essa classe diretamente (`java -cp ... br.f1mane.MainFrameEditor`) precisa ser atualizado.

## Capabilities

### New Capabilities
- `dev-editor-tools`: ferramentas Swing de desenvolvimento (`EditorCoresCarros`, editor de circuito via `MainFrameEditor`/`MainPanelEditor`) que leem e gravam diretamente os recursos-fonte do jogo em `src/main/resources`, com navegação de temporada e organização de pacote consistentes.

### Modified Capabilities
- (nenhuma — não há spec existente cobrindo essas ferramentas de edição de recursos)

## Impact

- `src/main/java/br/f1mane/util/EditorCoresCarros.java` → movido para `src/main/java/br/f1mane/editor/EditorCoresCarros.java`, com correção de caminho de gravação e troca do combobox por navegação anterior/próxima.
- `src/main/java/br/f1mane/MainFrameEditor.java` → movido para `src/main/java/br/f1mane/editor/MainFrameEditor.java`.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java` → atualizar import de `MainFrameEditor` e corrigir diretório padrão usado em `novo()`, `editar()` e `salvarPista()`.
- Nenhum impacto em runtime do jogo (modos `MainLauncher`, `MainFrame`, `AppletPaddock`) — essas classes são utilitários de desenvolvimento, sem uso fora de si mesmas e do menu do editor.
- Javadoc de uso (`Uso: java -cp target/flmane.jar ...`) em `EditorCoresCarros` deve ser atualizado para refletir que essas ferramentas precisam ser executadas com a raiz do projeto como diretório de trabalho (mesmo padrão já usado por `SpriteSheet.main`, que grava em `src/main/resources/sprites`).
