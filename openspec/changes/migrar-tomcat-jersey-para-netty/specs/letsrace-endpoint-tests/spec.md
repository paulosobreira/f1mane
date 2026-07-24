## MODIFIED Requirements

### Requirement: LetsRace SHALL ser instanciável em teste sem o grafo real do servidor
`LetsRace` SHALL expor uma forma de injetar `ControlePaddockServidor` e `CarregadorRecursos` em testes, sem acionar `PaddockServer.init` (que constrói `ControlePersistencia`/Hibernate e inicia uma thread de monitoramento). O construtor público sem argumentos usado pelo roteador Netty em produção NÃO SHALL mudar de comportamento.

#### Scenario: Construtor de teste injeta dependências mockadas
- **WHEN** um teste cria `new LetsRace(carregadorRecursosMock, controlePaddockMock)`
- **THEN** nenhuma conexão de banco, thread de monitoramento ou leitura de classpath é acionada

#### Scenario: Construtor público de produção preserva comportamento
- **WHEN** o roteador Netty instancia `LetsRace` via construtor sem argumentos
- **THEN** o `controlePaddock` resultante é o mesmo retornado por `PaddockServer.getControlePaddock()`, igual ao comportamento antes desta mudança
