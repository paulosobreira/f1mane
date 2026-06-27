## ADDED Requirements

### Requirement: PaddockServer documentado
O SDD SHALL descrever `PaddockServer` como singleton que inicializa a infraestrutura servidor.

#### Scenario: Inicialização do servidor descrita
- **WHEN** o leitor consulta a camada servidor
- **THEN** o SDD descreve que `PaddockServer.init(realpath)` é `synchronized`, cria `ControlePersistencia`, `ControlePaddockServidor` e `MonitorAtividade` (thread), e seta `Lang.setSrvgame(true)`; o flag `iniciado` previne dupla inicialização

### Requirement: REST API documentada
O SDD SHALL descrever os endpoints REST de `LetsRace` com seus paths e responsabilidades.

#### Scenario: Endpoints de LetsRace descritos
- **WHEN** o leitor consulta a API REST
- **THEN** o SDD descreve que `LetsRace` é anotada com `@Path("/letsRace")`, mapeada em `/letsRace/*` via Jersey, e lista os principais endpoints: `GET /dadosToken`, `GET /criarSessaoGoogle`, `GET /criarSessaoVisitante`, `GET /criarSessaoNome`, `GET /circuito`; autenticação é por token no header HTTP `token`

### Requirement: SessaoCliente documentada
O SDD SHALL descrever os campos de `SessaoCliente` e sua função no protocolo.

#### Scenario: Campos de SessaoCliente descritos
- **WHEN** o leitor consulta o estado do jogador servidor
- **THEN** o SDD descreve os campos: `token`, `nomeJogador`, `email`, `idUsuario`, `jogoAtual`, `pilotoAtual`, `idPilotoAtual`, `ultimaAtividade`, `guest`

### Requirement: Fluxo de criação de jogo servidor documentado
O SDD SHALL descrever como `ControlePaddockServidor` delega a `ControleJogosServer` para criar e gerenciar instâncias de `JogoServidor`.

#### Scenario: Criação de jogo descrita
- **WHEN** o leitor consulta o ciclo de vida de um jogo multiplayer
- **THEN** o SDD descreve que `ControleJogosServer` mantém `mapaJogosCriados: Map<SessaoCliente, JogoServidor>`, limita a `MaxJogo = 5` jogos simultâneos, e que `JogoServidor` estende `ControleJogoLocal`

### Requirement: JogoCliente documentado com ressalva de implementação parcial
O SDD SHALL documentar `JogoCliente` descrevendo claramente que é uma implementação parcial de `InterfaceJogo`.

#### Scenario: Papel de JogoCliente descrito
- **WHEN** o leitor consulta o cliente multiplayer
- **THEN** o SDD descreve que `JogoCliente` estende `ControleRecursos` e implementa `InterfaceJogo`, com a maioria dos métodos retornando valores neutros (null, 0, false) pois o estado real vive no servidor; os métodos funcionais incluem `exibirResultadoFinal()` e `iniciaJanela()`
