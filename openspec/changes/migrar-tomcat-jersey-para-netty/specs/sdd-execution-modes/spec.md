## MODIFIED Requirements

### Requirement: MainLauncher descrito
O SDD SHALL descrever que `MainLauncher` tem dois modos: **GUI** (sem args) — exibe o launcher Swing com QR Code e inicia o backend numa **JVM filha** via `ProcessBuilder` (`java -cp <jar> br.f1mane.MainLauncher --headless`), com as aplicações Swing (`MainFrame`, `AppletPaddock`) abrindo na própria JVM do launcher; e **headless** (parâmetro fixo `--headless`, usado no Docker/servidor) — sobe um **servidor HTTP Netty embutido** na porta 8080 na própria JVM, extrai `webapp/` do JAR para um diretório temporário `flmane-webapp` e serve o contexto `/flmane`, sem GUI e sem processo filho. O servidor não depende de Tomcat, Jasper/JSP, Jakarta Servlet API nem JAX-RS/Jersey — o roteamento HTTP e a serialização de resposta são implementados diretamente sobre Netty.

#### Scenario: MainLauncher descrito com Netty
- **WHEN** o leitor consulta o modo web
- **THEN** o SDD descreve que o backend sobe um **servidor HTTP Netty** na porta 8080 — na JVM filha (modo GUI) ou na própria JVM (modo `--headless`) — sem Tomcat, Jasper, Jakarta Servlet API ou Jersey/JAX-RS

#### Scenario: Separação de JVMs do modo GUI documentada
- **WHEN** o leitor consulta o modo launcher (GUI)
- **THEN** o SDD descreve que o launcher e as aplicações Swing compartilham uma JVM (com um único estado de i18n `Lang`) e o servidor roda em JVM separada, encerrada junto com o launcher

#### Scenario: Ausência de servlet/JAX-RS documentada no modo servidor
- **WHEN** o leitor consulta detalhes de dependências do modo servidor
- **THEN** o SDD indica que o projeto não depende de `jakarta.servlet.*` nem `jakarta.ws.rs.*` — o roteamento HTTP (`/flmane/rest/letsRace/*`, `/flmane/ServletPaddock`, arquivos estáticos) é implementado diretamente sobre Netty

#### Scenario: MainFrame descrito
- **WHEN** o leitor consulta o modo solo
- **THEN** o SDD descreve que `MainFrame` instancia `ControleJogoLocal` e `PainelMenuLocal`, abre janela 1280×720 e gerencia o jogo solo em Swing

#### Scenario: AppletPaddock descrito
- **WHEN** o leitor consulta o modo multiplayer cliente
- **THEN** o SDD descreve que `AppletPaddock` instancia `ControlePaddockCliente`, conecta ao servidor em `http://localhost:8080` e inicia a sessão de jogo multiplayer

#### Scenario: MainFrameSimulacao descrito
- **WHEN** o leitor consulta simulação headless
- **THEN** o SDD descreve que `MainFrameSimulacao` aceita args `(temporada, circuito, voltas)`, desativa flags de rendering (`desenhaBkg`, `desenhaPista`, `desenhaImagens`) e é equivalente ao comando `./simulacao.sh`
