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
O SDD SHALL descrever que `MainLauncher` sobe **Tomcat 11** na porta 8080, extrai `webapp/` do JAR para um diretório temporário `flmane-webapp`, mapeia contexto em `/flmane`, e exibe launcher Swing com QR Code (exceto quando `--headless`). O Tomcat 11 usa **Jakarta EE 11** (`jakarta.*` namespace) — a dependência `javax.servlet-api` foi substituída por `jakarta.servlet-api 6.1`.

#### Scenario: MainLauncher descrito com Tomcat 11
- **WHEN** o leitor consulta o modo web
- **THEN** o SDD descreve que `MainLauncher` sobe **Tomcat 11** na porta 8080, usando Jakarta Servlet 6.1

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
