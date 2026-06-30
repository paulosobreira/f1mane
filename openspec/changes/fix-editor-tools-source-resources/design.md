## Context

`EditorCoresCarros` e o editor de circuito (`MainFrameEditor` + `MainPanelEditor`) são as duas únicas ferramentas Swing do projeto cujo propósito é editar diretamente os arquivos-fonte do jogo em `src/main/resources` (cores/atributos de carro em `carros.properties`, traçado de circuito em `circuitos/*.xml`). Ambas localizam o arquivo a gravar resolvendo `Class.getResource(...)` e convertendo a `URL` retornada em `File`/`JFileChooser`. Isso é uma confusão entre "onde o recurso está no classpath de execução" (que pode ser `target/classes` ou um jar) e "onde está o arquivo-fonte versionado" (`src/main/resources`). O projeto já tem um precedente correto para isso: `SpriteSheet.main()` (`src/main/java/br/f1mane/recursos/SpriteSheet.java:161`) grava diretamente em `"src/main/resources/sprites"`, um caminho relativo ao diretório de trabalho, partindo do princípio de que essas ferramentas são sempre executadas com a raiz do projeto como working directory (via IDE ou `java -cp target/classes ...` a partir da raiz).

## Goals / Non-Goals

**Goals:**
- Garantir que `EditorCoresCarros` sempre grave em `src/main/resources/properties/<temporada>/carros.properties`, independente de como/onde a JVM foi iniciada (classpath de jar, `target/classes`, IDE).
- Garantir que o editor de circuito sempre abra/salve por padrão em `src/main/resources/circuitos`.
- Realocar `EditorCoresCarros` e `MainFrameEditor` para o pacote `br.f1mane.editor`, consolidando todas as ferramentas de edição de recursos num único pacote (que já contém `MainPanelEditor`, `FormularioObjetos`, `FormularioListaObjetos`, `ExampleFileFilter`).
- Trocar a seleção de temporada por combobox em `EditorCoresCarros` por botões de navegação anterior/próxima, mantendo a mesma lista de temporadas já carregada.

**Non-Goals:**
- Não alterar o runtime do jogo (`MainLauncher`, `MainFrame`, `AppletPaddock`, `ControleJogoLocal`, etc.) — essas ferramentas não são usadas por nenhum modo de execução do jogo.
- Não alterar o formato de `carros.properties` nem do XML de circuito.
- Não adicionar um mecanismo genérico de "resolução de raiz de projeto" reutilizável por outras partes do sistema — o escopo é só essas duas ferramentas de edição.
- Não mudar o fluxo de uso real do `JFileChooser` no editor de circuito além do diretório inicial (o usuário continua podendo navegar e escolher outro caminho).

## Decisions

### 1. Resolver o caminho de gravação via diretório de trabalho (`user.dir`), não via classpath
`EditorCoresCarros.salvarNoArquivo` passa a construir o arquivo como `new File("src/main/resources/properties/" + temporadaAtual + "/carros.properties")` (caminho relativo, resolvido pela JVM a partir de `user.dir`), em vez de `CarregadorRecursos.class.getResource(...).toURI()`. Mesmo princípio aplicado ao fallback de listagem de temporadas em `popularTemporadas()` (que hoje também faz `new File(base.toURI())` a partir do classpath) — passa a listar diretamente `src/main/resources/properties`.

**Alternativas consideradas:**
- *Resolver a raiz do projeto subindo diretórios a partir de `CarregadorRecursos.class.getProtectionDomain().getCodeSource().getLocation()`*: mais "robusto" a diferentes working directories, mas adiciona complexidade (heurística de quantos níveis subir, falha diferente dentro de jar) para um problema que já tem solução mais simples adotada em `SpriteSheet`. Rejeitado para manter consistência com o padrão existente no código.
- *Aceitar argumento de linha de comando com o caminho da raiz do projeto*: mais explícito, mas muda a forma de invocação documentada e adiciona um parâmetro que o usuário sempre vai esquecer. Rejeitado.

Optamos pelo caminho relativo simples, documentando claramente (no Javadoc da classe) que a ferramenta deve ser executada com a raiz do projeto como working directory — igual ao padrão já estabelecido por `SpriteSheet`.

### 2. Diretório inicial do `JFileChooser` no editor de circuito
Em `MainPanelEditor.novo()`, `editar()` e `salvarPista()`, o `JFileChooser` é instanciado com `CarregadorRecursos.class.getResource("CarregadorRecursos.class").getFile()` como diretório inicial. Isso é substituído por `new File("src/main/resources/circuitos")` (caminho relativo a `user.dir`), criando o diretório com `mkdirs()` apenas se necessário para leitura (não criamos diretório novo se já existir — `src/main/resources/circuitos` já existe no repo). Continua sendo um `JFileChooser` comum: o usuário pode navegar para qualquer outro caminho: a mudança só afeta o diretório em que o diálogo abre por padrão.

### 3. Pacote `br.f1mane.editor`
`EditorCoresCarros` move de `br.f1mane.util` para `br.f1mane.editor`. `MainFrameEditor` move de `br.f1mane` para `br.f1mane.editor`. Isso agrupa todas as ferramentas de edição de recursos-fonte do jogo (circuito + cores de carro + formulários de objeto) sob o mesmo pacote, já usado por `MainPanelEditor`. Único consumidor a ajustar é o próprio `MainPanelEditor`, que importa `MainFrameEditor` (passa a ser import same-package, removendo o `import` por completo já que ambas ficam em `br.f1mane.editor`).

### 4. Navegação de temporada: botões anterior/próxima
O `JComboBox<String> comboTemporada` é substituído por: um `JLabel` mostrando a temporada atual + dois `JButton` ("◀" e "▶") que avançam/recuam um índice dentro da lista de temporadas já carregada (mesma lista hoje usada para popular o combobox). Os botões ficam desabilitados nas extremidades da lista (sem wrap-around). O método `carregarTemporada(String)` é reaproveitado sem mudanças — só muda o que dispara a troca de temporada.

## Risks / Trade-offs

- [Risco] Ferramenta passa a depender de `user.dir` ser a raiz do projeto. Se alguém rodar `java -cp target/flmane.jar br.f1mane.editor.EditorCoresCarros` de outro diretório, o salvamento falhará (arquivo não encontrado) em vez de silenciosamente gravar no lugar errado. → Mitigação: atualizar o Javadoc/`Uso:` da classe para deixar explícito que deve ser executada a partir da raiz do projeto (mesmo padrão de `SpriteSheet`), e lançar uma mensagem de erro clara (via `JOptionPane`) quando o arquivo não existir no caminho esperado, em vez de uma `NullPointerException` genérica.
- [Risco] Mover classes de pacote quebra qualquer script externo (fora do repo) que invoque `br.f1mane.MainFrameEditor` ou `br.f1mane.util.EditorCoresCarros` diretamente pelo nome totalmente qualificado. → Mitigação: nenhuma referência a essas classes foi encontrada em `build.sh`, `simulacao.sh`, `pom.xml` ou `CLAUDE.md`; o único consumidor interno (`MainPanelEditor`) é atualizado como parte desta mudança. Documentado como **BREAKING** na proposta.
- [Trade-off] Não tornamos a resolução de caminho robusta a qualquer working directory (ex.: rodando de dentro de `target/`) — aceito conscientemente para manter a implementação simples e consistente com `SpriteSheet`.

## Migration Plan

1. Mover os arquivos (`git mv`) para os novos pacotes/caminhos, atualizar `package` e imports.
2. Corrigir os caminhos de leitura/gravação em `EditorCoresCarros` e `MainPanelEditor`.
3. Substituir o combobox de temporada pelos botões anterior/próxima em `EditorCoresCarros`.
4. Rodar `mvn test` e validar manualmente: abrir `EditorCoresCarros` e `MainFrameEditor` a partir da raiz do projeto, salvar uma alteração em cada um e confirmar via `git diff` que o arquivo correto em `src/main/resources` foi alterado.
5. Não há rollback especial necessário além de reverter o commit — não há mudança de schema/dado persistido, só localização e comportamento de ferramentas de desenvolvimento.

## Open Questions

- Nenhuma em aberto — escopo e abordagem confirmados com o usuário antes da implementação.
