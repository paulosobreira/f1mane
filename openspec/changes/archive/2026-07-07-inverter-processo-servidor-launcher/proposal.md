## Why

Hoje o `MainLauncher` sobe o Tomcat (backend inteiro) **na própria JVM** e usa `ProcessBuilder` para abrir as aplicações Swing (`MainFrame`, `AppletPaddock`) em JVMs separadas. Como o sistema de i18n (`Lang`) é totalmente estático por JVM (`bundle`, `sufix`, flag `srvgame`), o servidor e a UI Swing dividindo a mesma JVM corrompem um ao outro: as mensagens do launcher/Swing não aparecem no idioma correto. Invertendo a separação — Swing tudo junto numa JVM, servidor sozinho em outra — cada lado passa a ter seu próprio `Lang` isolado.

## What Changes

- **Inverte o modelo de processos do `MainLauncher`**: sem argumentos, o backend (Tomcat + `PaddockServer`) passa a ser iniciado como **processo separado** via `ProcessBuilder`, e as opções Swing do launcher ("Java solo game" → `MainFrame`, "Java multiplayer game" → `AppletPaddock`) passam a rodar **na mesma JVM** do launcher, sem `ProcessBuilder`.
- O modo headless passa a ser acionado pelo parâmetro fixo **`--headless`** (como no Docker/servidor): Tomcat na própria JVM, sem GUI/QR Code e sem spawn de processo filho.
- O launcher passa a gerenciar o ciclo de vida do processo do servidor: encerrar o launcher encerra o processo filho (sem JVMs órfãs).
- **Internacionaliza o launcher**: os rótulos hardcoded em inglês ("Copy link", "Open in browser", "Java solo game", "Java multiplayer game") passam por `Lang.msg(...)`, com chaves novas nos bundles `idiomas/mensagens_XX.properties`.
- **Remove os sprites do fat jar**: adiciona `sprites/*` ao filtro de excludes do `maven-shade-plugin` no `pom.xml`, no mesmo padrão do exclude existente de `circuitos/*_mro.jpg` (imagens de referência dos circuitos). O runtime já tem fallback (`SpriteSheet.isDisponivel` retorna `false` e `CarregadorRecursos` cai no modelo universal v2 via `pintarModeloV2`).

## Capabilities

### New Capabilities
- `launcher-servidor-processo-separado`: `MainLauncher` (GUI) inicia o backend numa JVM filha via `ProcessBuilder` e roda as aplicações Swing na própria JVM, gerenciando o ciclo de vida do processo filho.
- `launcher-i18n`: rótulos do launcher Swing traduzidos via `Lang`, isolados da i18n do servidor (que vive em outra JVM).
- `fatjar-sem-sprites`: o fat jar final não empacota `sprites/*.png`; a renderização usa o fallback do modelo universal v2.

### Modified Capabilities
- `sdd-execution-modes`: o requisito "MainLauncher descrito" muda — o launcher GUI não hospeda mais o Tomcat na própria JVM; ele orquestra o processo servidor separado, e `--headless` permanece como o modo que sobe o Tomcat in-process.

## Impact

- `src/main/java/br/f1mane/MainLauncher.java` — inversão dos processos, ciclo de vida do filho, i18n dos rótulos.
- `src/main/resources/idiomas/mensagens_XX.properties` — novas chaves de tradução do launcher.
- `pom.xml` — exclude `sprites/*` no shade plugin.
- Docker/`build.sh` — sem mudança de contrato (`--headless` preserva o comportamento atual).
- Testes: cobertura unitária onde couber (montagem de comando do processo, chaves de i18n presentes nos bundles).
