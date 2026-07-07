## 1. Inversão de processos no MainLauncher

- [x] 1.1 Extrair do `main` o caminho de servidor (extrai webapp + Tomcat + `await()`) para um método próprio, acionado pelo parâmetro fixo `--headless` — sem GUI e sem processo filho
- [x] 1.2 No modo GUI (sem args): não subir Tomcat; chamar `localizarJar()` no boot, spawnar o filho `java -Xms64m -Xmx512m -cp <jar> br.f1mane.MainLauncher --headless` com `inheritIO()`
- [x] 1.3 Guardar o `Process` do servidor e registrar `Runtime.addShutdownHook` chamando `destroy()` (fechar o launcher encerra o servidor)
- [x] 1.4 Manter o cálculo da URL/QR local (`descobrirIP()` + porta 8080) no modo GUI

## 2. Aplicações Swing in-JVM

- [x] 2.1 Adicionar modo hospedado ao `MainFrame` (construtor/flag) usando `DISPOSE_ON_CLOSE` em vez de `EXIT_ON_CLOSE`, preservando o comportamento standalone atual
- [x] 2.2 Adicionar modo hospedado ao frame do `AppletPaddock` com `DISPOSE_ON_CLOSE`, preservando o standalone
- [x] 2.3 Substituir os dois `ProcessBuilder` dos cliques "solo"/"multiplayer" por invocação in-JVM (thread própria/`invokeLater`) no modo hospedado
- [x] 2.4 Verificar que fechar o jogo solo/multiplayer não encerra o launcher nem o processo do servidor

## 3. i18n do launcher

- [x] 3.1 Adicionar chaves `launcherCopiarLink`, `launcherAbrirNavegador`, `launcherJogoSolo`, `launcherJogoMulti` em todos os `idiomas/mensagens_XX.properties`
- [x] 3.2 Trocar os rótulos hardcoded do `MainLauncher` por `Lang.msg(chave)`
- [x] 3.3 Teste garantindo que as chaves novas existem em todos os bundles empacotados

## 4. Sprites fora do fat jar

- [x] 4.1 Adicionar `<exclude>sprites/*</exclude>` ao filtro `*:*` do `maven-shade-plugin` no `pom.xml`, com comentário no padrão do exclude de `circuitos/*_mro.jpg`
- [x] 4.2 Verificar no jar gerado a ausência de `sprites/*.png` e o carregamento de temporada via modelo v2 (`SpriteSheet.isDisponivel` false → `pintarModeloV2`) sem exceção

## 5. Validação

- [x] 5.1 `mvn test` verde
- [x] 5.2 `mvn clean package -Ph2 -DskipTests` e rodar `java -jar target/flmane.jar`: launcher abre, servidor filho sobe (banner "SERVER STARTED" no console), web responde em `/flmane/html5/index.html`
- [x] 5.3 Fechar o launcher e confirmar que o processo java do servidor morre junto
- [x] 5.4 Rodar `java -jar target/flmane.jar --headless` e confirmar o modo servidor in-process (Tomcat na própria JVM, sem GUI, sem filho)
- [ ] 5.5 Abrir jogo solo e multiplayer pelo launcher, conferir rótulos traduzidos e fechamento sem derrubar o launcher
