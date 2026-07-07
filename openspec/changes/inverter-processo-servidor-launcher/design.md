## Context

`MainLauncher.main` hoje: sobe o Tomcat na própria JVM (`extrairWebapp()` + `tomcat.start()`), mostra o launcher Swing **somente quando não há args** (`args.length == 0`) — qualquer argumento (ex.: `--headless`, `server`) suprime a GUI —, e usa `ProcessBuilder("java", ..., "-cp", jar, "br.f1mane.MainFrame" | "...AppletPaddock")` para abrir os jogos Swing em JVMs separadas. O Docker invoca `MainLauncher --headless` (sem GUI).

O i18n (`Lang`, em `br.f1mane.recursos.idiomas`) é estático por JVM: `bundle`, `sufix`, `mapaBundle` e a flag `srvgame`. O servidor traduz por header REST (`msgKey(..., idioma)`) e mexe nesses estáticos; a UI Swing usa `Lang.msg(key)` com o `Locale.getDefault()`. Na mesma JVM um contamina o outro — por isso as mensagens do launcher saem erradas.

Os rótulos do launcher ("Copy link", "Open in browser", "Java solo game", "Java multiplayer game") são strings hardcoded em inglês.

O fat jar empacota `sprites/tANO.png` (~uma folha por temporada), mas o runtime já tem fallback completo: `SpriteSheet.isDisponivel()` retorna `false` quando o png não existe e `CarregadorRecursos` cai no modelo universal v2 (`pintarModeloV2` com `png/carro-*-v2.png`/`png/capacete-v2.png`). O precedente de exclusão é `circuitos/*_mro.jpg` no filtro do `maven-shade-plugin`.

## Goals / Non-Goals

**Goals:**
- Servidor (Tomcat + PaddockServer) numa JVM própria, iniciada e encerrada pelo launcher.
- `MainFrame` e `AppletPaddock` rodando na JVM do launcher, compartilhando o mesmo `Lang`.
- Rótulos do launcher traduzidos via `Lang.msg` (bundles `mensagens_XX.properties`).
- Modo headless acionado pelo parâmetro fixo `--headless` (contrato do Docker/servidor): Tomcat in-process, sem GUI, sem processo filho.
- Fat jar sem `sprites/*.png`.

**Non-Goals:**
- Refatorar o `Lang` para deixar de ser estático (a separação por JVM resolve o problema sem esse custo).
- Mudar o modo de descoberta de IP/porta ou o QR Code.
- Remover a classe `SpriteSheet` ou o pipeline `gerar_spritesheets.py` (continuam úteis no editor/repositório; só o empacotamento muda).
- Health-check/restart automático do processo servidor.

## Decisions

**1. Reusar `MainLauncher --headless` como entry point do processo servidor.**
O launcher GUI spawna `java -Xms... -Xmx... -cp <jar> br.f1mane.MainLauncher --headless`. O parâmetro é **fixo**: `--headless` (contrato já documentado do Docker). Alternativa considerada: criar uma classe `MainServidor` dedicada — rejeitada porque `--headless` já é exatamente "Tomcat sem GUI" e evita um segundo caminho de inicialização para manter.

**2. Fluxo do `main`:** com o argumento `--headless`: comportamento headless — extrai webapp, sobe Tomcat na própria JVM, `await()`, sem nada de Swing/QR e sem processo filho (caso Docker/servidor). Sem args (GUI): **não** sobe Tomcat; localiza o jar (`localizarJar()`), spawna o filho `--headless` com `inheritIO()` (logs do servidor no console do launcher), calcula a URL localmente (`descobrirIP()` + porta 8080, como hoje) e mostra o launcher Swing. (Hoje qualquer argumento suprime a GUI; o gatilho passa a ser o parâmetro fixo `--headless`.)

**3. Ciclo de vida do filho:** o launcher guarda o `Process` e registra `Runtime.addShutdownHook` chamando `process.destroy()` — cobre fechar a janela (`EXIT_ON_CLOSE`) e encerramento normal da JVM. Sem JVM órfã de servidor.

**4. Swing in-JVM sem matar o launcher:** `MainFrame` (standalone) e `AppletPaddock` usam `EXIT_ON_CLOSE`, o que derrubaria a JVM do launcher (e o shutdown hook mataria o servidor — nesse caso até aceitável, mas fechar o jogo solo não deve fechar o launcher). Solução: os cliques do launcher invocam as aplicações em modo "hospedado" com `DISPOSE_ON_CLOSE`. `MainFrame` já tem precedente interno (com `appletPaddock != null` usa `DO_NOTHING_ON_CLOSE`); expõe-se um caminho de construção/flag para o modo hospedado, e o mesmo para o frame do `AppletPaddock`. Alternativa considerada: system property global — rejeitada por ser implícita demais; parâmetro explícito é mais rastreável.

**5. Invocação em thread própria (fora do EDT do clique):** cada opção Swing roda `SwingUtilities.invokeLater`/thread dedicada chamando o construtor/entry hospedado, evitando bloquear o launcher.

**6. i18n do launcher:** novas chaves (`launcherCopiarLink`, `launcherAbrirNavegador`, `launcherJogoSolo`, `launcherJogoMulti`) em todos os `idiomas/mensagens_XX.properties` existentes; `MainLauncher` troca as strings hardcoded por `Lang.msg(chave)`. Como o servidor agora vive em outra JVM, o `Lang` do launcher fica estável no `Locale.getDefault()`.

**7. Sprites fora do fat jar:** adicionar `<exclude>sprites/*</exclude>` no filtro `*:*` do shade plugin, com comentário no mesmo estilo do exclude de `circuitos/*_mro.jpg` (explicando o fallback do modelo v2). Nenhuma mudança de código: o guard `SpriteSheet.isDisponivel()` já degrada para `pintarModeloV2`.

## Risks / Trade-offs

- [Porta 8080 ocupada / filho morre no start] → `inheritIO()` deixa o erro visível no console; o launcher continua mostrando a URL (QR aponta pra porta padrão). Mitigação simples e suficiente para o cenário de uso (jogo local).
- [`localizarJar()` falha rodando do IDE (sem `target/flmane.jar`)] → comportamento atual já é lançar `RuntimeException` no clique; passa a falhar no boot do launcher GUI. Documentar no erro que é preciso `mvn package` antes (mensagem já existente cobre).
- [`destroy()` no Windows pode não derrubar descendentes] → o filho é uma JVM única (sem netos); `Process.destroy()` é suficiente.
- [MainFrame/AppletPaddock com estado estático que não suporta reabertura na mesma JVM] → aceitável: mesmo comportamento de abrir duas instâncias hoje; se algo aparecer na implementação, restringir a uma instância por vez no launcher.
- [Sprites removidos mudam o visual dos carros para quem dependia da folha] → intencional: o modelo v2 é o caminho suportado no jar final; o editor (rodando do repositório) mantém acesso aos sprites.

## Migration Plan

1. Inverter processos no `MainLauncher` (mantendo `--headless` intacto) + ciclo de vida do filho.
2. Modo hospedado (DISPOSE_ON_CLOSE) em `MainFrame`/`AppletPaddock` e troca dos `ProcessBuilder` dos jogos por invocação in-JVM.
3. Chaves de i18n nos bundles + `Lang.msg` no launcher.
4. Exclude de `sprites/*` no pom.
5. Validação: `mvn package -Ph2`, rodar `java -jar target/flmane.jar` (GUI spawna servidor; web responde; fechar launcher mata servidor), rodar com `--headless` (paridade Docker), abrir jogo solo pelo launcher e fechá-lo sem derrubar o launcher.

## Open Questions

- Nenhuma bloqueante. (Se `AppletPaddock` precisar do servidor local para conectar, o launcher já o terá spawnado — ordem natural do fluxo.)
