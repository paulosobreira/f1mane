## ADDED Requirements

### Requirement: LetsRace SHALL ser instanciável em teste sem o grafo real do servidor
`LetsRace` SHALL expor uma forma de injetar `ControlePaddockServidor` e `CarregadorRecursos` em testes, sem acionar `PaddockServer.init` (que constrói `ControlePersistencia`/Hibernate e inicia uma thread de monitoramento). O construtor público sem argumentos usado pelo Jersey em produção NÃO SHALL mudar de comportamento.

#### Scenario: Construtor de teste injeta dependências mockadas
- **WHEN** um teste cria `new LetsRace(carregadorRecursosMock, controlePaddockMock)`
- **THEN** nenhuma conexão de banco, thread de monitoramento ou leitura de classpath é acionada

#### Scenario: Construtor público de produção preserva comportamento
- **WHEN** o Jersey instancia `LetsRace` via construtor sem argumentos
- **THEN** o `controlePaddock` resultante é o mesmo retornado por `PaddockServer.getControlePaddock()`, igual ao comportamento antes desta mudança

### Requirement: Endpoints que exigem sessão SHALL retornar 401 quando o token é inválido
Todo endpoint de `LetsRace` que chama `controlePaddock.obterSessaoPorToken(token)` SHALL retornar HTTP 401 quando esse método retorna `null`, sem chamar nenhum outro método do controller.

#### Scenario: jogar() com token inválido retorna 401
- **WHEN** `controlePaddock.obterSessaoPorToken(token)` retorna `null` e `jogar(...)` é chamado
- **THEN** a resposta tem status 401 e `controlePaddock.jogar(...)` nunca é chamado

#### Scenario: equipe() com token inválido retorna 401
- **WHEN** `controlePaddock.obterSessaoPorToken(token)` retorna `null` e `equipe(token, idioma)` é chamado
- **THEN** a resposta tem status 401

#### Scenario: dadosParciais() com token inválido retorna 401
- **WHEN** `controlePaddock.obterSessaoPorToken(token)` retorna `null` e `dadosParciais(...)` é chamado
- **THEN** a resposta tem status 401

### Requirement: campeonato() SHALL retornar 403 para sessão de visitante
`campeonato(token, idioma)` SHALL retornar HTTP 403 quando `sessaoCliente.isGuest()` é verdadeiro, sem chamar `controlePaddock.obterCampeonatoEmAberto`.

#### Scenario: Visitante tenta acessar campeonato
- **WHEN** a sessão retornada por `obterSessaoPorToken` tem `isGuest() == true`
- **THEN** a resposta de `campeonato(token, idioma)` tem status 403

### Requirement: processsaMensagem SHALL mapear MsgSrv para 400 e ErroServ para 500
Endpoints que delegam a resposta de negócio para `processsaMensagem` (`jogar`, `jogarCampeonato`, `equipe`, `equipePilotoCarro`, `gravarEquipe`, `gravarCampeonato`) SHALL retornar HTTP 400 com a mensagem traduzida quando o controller retorna `MsgSrv`, e HTTP 500 com o erro formatado quando o controller retorna `ErroServ`.

#### Scenario: jogar() com MsgSrv retorna 400
- **WHEN** `controlePaddock.jogar(...)` retorna uma instância de `MsgSrv`
- **THEN** a resposta de `jogar(...)` tem status 400 e o corpo contém a mensagem traduzida via `Lang.decodeTextoKey`

#### Scenario: jogar() com ErroServ retorna 500
- **WHEN** `controlePaddock.jogar(...)` retorna uma instância de `ErroServ`
- **THEN** a resposta de `jogar(...)` tem status 500 e o corpo contém o texto de `erroServ.obterErroFormatado()`

#### Scenario: jogar() com sucesso retorna 200 com o payload do controller
- **WHEN** `controlePaddock.jogar(...)` retorna um objeto que não é `MsgSrv` nem `ErroServ`
- **THEN** a resposta de `jogar(...)` tem status 200 e o corpo é exatamente o objeto retornado pelo controller

### Requirement: criarSessaoNome SHALL retornar 500 quando o controller falha
`criarSessaoNome(nome)` SHALL retornar HTTP 500 com o `ErroServ` serializável quando `controlePaddock.criarSessaoNome(nome)` retorna uma instância de `ErroServ`, e HTTP 200 com o payload de sessão caso contrário — cobrindo a regressão do bug corrigido nesta sessão (`ErroServ` sem `@JsonProperty` quebrava a serialização).

#### Scenario: criarSessaoNome propaga erro do controller como 500
- **WHEN** `controlePaddock.criarSessaoNome("nome")` retorna um `ErroServ`
- **THEN** `criarSessaoNome("nome")` retorna status 500 com esse `ErroServ` como entity

#### Scenario: criarSessaoNome de sucesso retorna 200
- **WHEN** `controlePaddock.criarSessaoNome("nome")` retorna um `SrvPaddockPack`
- **THEN** `criarSessaoNome("nome")` retorna status 200 com esse objeto como entity

### Requirement: Endpoints de delegação simples SHALL repassar parâmetros e resultado corretamente
Endpoints que apenas extraem parâmetros (header/path/query) e repassam para um método do controller — sem ramificação de status além de 200 (ex.: `obterJogos`, `verificaServico`, `circuitos`, `temporadas`, `potenciaMotor`, `agressividadePiloto`, `tracadoPiloto`, `drsPiloto`, `ersPiloto`, `boxPiloto`) — SHALL chamar o método correto do controller com os parâmetros corretos e devolver o resultado como entity da resposta 200.

#### Scenario: potenciaMotor delega para mudarGiroMotor com os parâmetros corretos
- **WHEN** `potenciaMotor(token, "GIRO_MAX", "piloto1")` é chamado com sessão válida
- **THEN** `controleJogosServer.mudarGiroMotor(sessaoCliente, "piloto1", "GIRO_MAX")` é chamado exatamente uma vez e seu retorno é o entity da resposta 200

#### Scenario: boxPiloto delega todos os parâmetros de pit stop
- **WHEN** `boxPiloto(token, "piloto1", true, "MACIO", 50, "BAIXA")` é chamado com sessão válida
- **THEN** `controleJogosServer.boxPiloto(sessaoCliente, "piloto1", true, "MACIO", 50, "BAIXA")` é chamado exatamente uma vez

#### Scenario: verificaServico não depende de sessão nem controller
- **WHEN** `verificaServico()` é chamado
- **THEN** a resposta tem status 200 e corpo `"ok"`, sem chamar `controlePaddock`
