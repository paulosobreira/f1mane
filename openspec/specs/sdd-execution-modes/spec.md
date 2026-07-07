# sdd-execution-modes

## Purpose
Documents the three execution modes of the `flmane.jar` — their entry points, what each initialises, and how the headless simulation mode works.

## Requirements

### Requirement: Modos de execução documentados
O SDD SHALL descrever os três modos de execução do JAR `flmane.jar`, seus entry points e o que cada um inicializa.

#### Scenario: Entry points descritos
- **WHEN** o leitor consulta a seção de modos de execução
- **THEN** encontra os três entry points: `MainLauncher`, `MainFrame` e `AppletPaddock`, com a classe Java correspondente a cada um

### Requirement: MainLauncher descrito
O SDD SHALL descrever que `MainLauncher` tem dois modos: **GUI** (sem args) — exibe o launcher Swing com QR Code e inicia o backend numa **JVM filha** via `ProcessBuilder` (`java -cp <jar> br.f1mane.MainLauncher --headless`), com as aplicações Swing (`MainFrame`, `AppletPaddock`) abrindo na própria JVM do launcher; e **headless** (parâmetro fixo `--headless`, usado no Docker/servidor) — sobe **Tomcat 11** na porta 8080 na própria JVM, extrai `webapp/` do JAR para um diretório temporário `flmane-webapp` e mapeia contexto em `/flmane`, sem GUI e sem processo filho. O Tomcat 11 usa **Jakarta EE 11** (`jakarta.*` namespace) — a dependência `javax.servlet-api` foi substituída por `jakarta.servlet-api 6.1`.

#### Scenario: MainLauncher descrito com Tomcat 11
- **WHEN** o leitor consulta o modo web
- **THEN** o SDD descreve que o backend sobe **Tomcat 11** na porta 8080 usando Jakarta Servlet 6.1 — na JVM filha (modo GUI) ou na própria JVM (modo `--headless`)

#### Scenario: Separação de JVMs do modo GUI documentada
- **WHEN** o leitor consulta o modo launcher (GUI)
- **THEN** o SDD descreve que o launcher e as aplicações Swing compartilham uma JVM (com um único estado de i18n `Lang`) e o servidor roda em JVM separada, encerrada junto com o launcher

#### Scenario: Namespace jakarta documentado no modo servidor
- **WHEN** o leitor consulta detalhes de dependências do modo servidor
- **THEN** o SDD indica que o projeto usa `jakarta.servlet.*`, `jakarta.ws.rs.*` (Jersey 4.x) e `jakarta.persistence.*` (Hibernate 7.4 / Jakarta Persistence 3.2), sem nenhuma referência ao namespace legado `javax.*`

#### Scenario: MainFrame descrito
- **WHEN** o leitor consulta o modo solo
- **THEN** o SDD descreve que `MainFrame` instancia `ControleJogoLocal` e `PainelMenuLocal`, abre janela 1280×720 e gerencia o jogo solo em Swing

#### Scenario: AppletPaddock descrito
- **WHEN** o leitor consulta o modo multiplayer cliente
- **THEN** o SDD descreve que `AppletPaddock` instancia `ControlePaddockCliente`, conecta ao servidor em `http://localhost:8080` e inicia a sessão de jogo multiplayer

#### Scenario: MainFrameSimulacao descrito
- **WHEN** o leitor consulta simulação headless
- **THEN** o SDD descreve que `MainFrameSimulacao` aceita args `(temporada, circuito, voltas)`, desativa flags de rendering (`desenhaBkg`, `desenhaPista`, `desenhaImagens`) e é equivalente ao comando `./simulacao.sh`
