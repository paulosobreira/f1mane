# sdd-execution-modes

## Purpose
Documents the three execution modes of the `flmane.jar` — their entry points, what each initialises, and how the headless simulation mode works.

## Requirements

### Requirement: Modos de execução documentados
O SDD SHALL descrever os três modos de execução do JAR `flmane.jar`, seus entry points e o que cada um inicializa.

#### Scenario: Entry points descritos
- **WHEN** o leitor consulta a seção de modos de execução
- **THEN** encontra os três entry points: `MainLauncher`, `MainFrame` e `AppletPaddock`, com a classe Java correspondente a cada um

#### Scenario: MainLauncher descrito
- **WHEN** o leitor consulta o modo web
- **THEN** o SDD descreve que `MainLauncher` sobe Tomcat na porta 8080, extrai `webapp/` do JAR para um diretório temporário `flmane-webapp`, mapeia contexto em `/flmane`, e exibe launcher Swing com QR Code (exceto quando `--headless`)

#### Scenario: MainFrame descrito
- **WHEN** o leitor consulta o modo solo
- **THEN** o SDD descreve que `MainFrame` instancia `ControleJogoLocal` e `PainelMenuLocal`, abre janela 1280×720 e gerencia o jogo solo em Swing

#### Scenario: AppletPaddock descrito
- **WHEN** o leitor consulta o modo multiplayer cliente
- **THEN** o SDD descreve que `AppletPaddock` instancia `ControlePaddockCliente`, conecta ao servidor em `http://localhost:8080` e inicia a sessão de jogo multiplayer

#### Scenario: MainFrameSimulacao descrito
- **WHEN** o leitor consulta simulação headless
- **THEN** o SDD descreve que `MainFrameSimulacao` aceita args `(temporada, circuito, voltas)`, desativa flags de rendering (`desenhaBkg`, `desenhaPista`, `desenhaImagens`) e é equivalente ao comando `./simulacao.sh`
