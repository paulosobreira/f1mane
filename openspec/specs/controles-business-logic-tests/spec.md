# controles-business-logic-tests

## Purpose

Cobertura de teste unitário da lógica de negócio real nas 5 classes de `br.f1mane.servidor.controles` (`ControlePaddockServidor`, `ControleJogosServer`, `ControleClassificacao`, `ControleCampeonatoServidor`, `ControlePersistencia`), mockando `ControlePersistencia`/`Session` do Hibernate através dos construtores já existentes, sem exigir refactor de produção.

## Requirements

### Requirement: As classes de br.f1mane.servidor.controles SHALL ser testáveis sem mudança de produção
Testes unitários das classes `ControlePaddockServidor`, `ControleJogosServer`, `ControleClassificacao`, `ControleCampeonatoServidor` e `ControlePersistencia` SHALL usar os construtores já existentes dessas classes, mockando `ControlePersistencia` (e `org.hibernate.Session`/`Transaction` quando necessário) via Mockito. Nenhum construtor novo ou mudança de visibilidade SHALL ser necessária nessas 5 classes.

#### Scenario: ControleClassificacao é testável mockando apenas ControlePersistencia
- **WHEN** um teste cria `new ControleClassificacao(controlePersistenciaMock, controleCampeonatoServidorMock)`
- **THEN** nenhuma conexão de banco real é acionada

### Requirement: A tabela de pontos por posição SHALL ter cobertura de teste
`ControleClassificacao.gerarPontos(Piloto)` SHALL retornar os pontos corretos conforme a tabela de pontuação por posição de chegada (1º:25, 2º:18, 3º:15, 4º:12, 5º:10, 6º:8, 7º:6, 8º:4, 9º:2, 10º:1, 11º em diante:0).

#### Scenario: Posições de pontuação retornam o valor correto
- **WHEN** `gerarPontos(piloto)` é chamado com `piloto.getPosicao()` igual a cada uma das posições 1 a 10
- **THEN** o retorno corresponde exatamente ao valor da tabela de pontos por posição

#### Scenario: Posição fora do pódio de pontos retorna zero
- **WHEN** `gerarPontos(piloto)` é chamado com `piloto.getPosicao()` igual a 11 ou maior
- **THEN** o retorno é 0

### Requirement: A validação de redistribuição de pontos SHALL ter cobertura de teste
`ControleClassificacao.validadeDistribuicaoPontos` (que orquestra `Util.processaValorPontosCarreira` por atributo) SHALL calcular corretamente o saldo de pontos de construtores ao mudar múltiplos atributos (aerodinâmica, carro, freio, piloto) simultaneamente, incluindo o caso de regressão do bug de pontos infinitos no nível 999.

#### Scenario: Upgrade de um único atributo debita o custo correto
- **WHEN** `validadeDistribuicaoPontos` é chamado com um único atributo subindo de nível dentro da mesma faixa de custo
- **THEN** o saldo retornado é igual ao saldo base menos o custo daquele atributo

#### Scenario: Mudança simultânea em múltiplos atributos soma os custos/refunds corretamente
- **WHEN** `validadeDistribuicaoPontos` é chamado com dois atributos subindo e um descendo ao mesmo tempo
- **THEN** o saldo retornado reflete a soma de todos os débitos e créditos individuais, na ordem aerodinâmica → carro → freio → piloto

### Requirement: A atualização de carreira SHALL validar nome e duplicidade antes de gravar
`ControleClassificacao.atualizaCarreira(String, CarreiraDadosSrv)` SHALL retornar `MsgSrv` (sem gravar) quando o nome do carro/piloto está vazio, excede 20 caracteres, ou já existe para outro jogador; SHALL gravar e retornar mensagem de sucesso quando a validação passa.

#### Scenario: Nome de carro vazio é rejeitado
- **WHEN** `atualizaCarreira(idUsuario, carreiraDados)` é chamado com `nomeCarro` vazio
- **THEN** o retorno é um `MsgSrv` e `controlePersistencia.gravarDados` nunca é chamado

#### Scenario: Nome de carro maior que 20 caracteres é rejeitado
- **WHEN** `atualizaCarreira(idUsuario, carreiraDados)` é chamado com `nomeCarro` de mais de 20 caracteres
- **THEN** o retorno é um `MsgSrv` e `controlePersistencia.gravarDados` nunca é chamado

#### Scenario: Nome de carro duplicado é rejeitado
- **WHEN** `controlePersistencia.existeNomeCarro(...)` retorna `true` para o nome informado
- **THEN** `atualizaCarreira` retorna um `MsgSrv` e `controlePersistencia.gravarDados` nunca é chamado

#### Scenario: Atualização válida grava e retorna sucesso
- **WHEN** todos os dados são válidos e não há duplicidade de nome
- **THEN** `controlePersistencia.gravarDados` é chamado uma vez e o retorno não é `MsgSrv` de erro nem `ErroServ`

### Requirement: A criação de campeonato SHALL exigir sessão de cliente válida
`ControleCampeonatoServidor.criarCampeonato` SHALL retornar `MsgSrv` de erro quando a sessão do cliente é nula, sem persistir nenhum dado.

#### Scenario: Sessão nula é rejeitada
- **WHEN** `criarCampeonato(clientPaddockPack)` é chamado com `clientPaddockPack.getSessaoCliente() == null`
- **THEN** o retorno é um `MsgSrv` e nenhuma chamada de persistência ocorre

### Requirement: A criação de sessão por nome SHALL reutilizar sessão existente para o mesmo usuário
`ControlePaddockServidor.criarSessaoNome(String)` SHALL retornar uma sessão existente (atualizando `ultimaAtividade`) quando já existe um cliente com o mesmo `idUsuario` na lista de clientes ativos, e SHALL criar uma nova sessão com token gerado quando não existe.

#### Scenario: Nome já existente reaproveita a sessão
- **WHEN** já existe um `SessaoCliente` com `idUsuario` igual ao nome informado em `dadosPaddock.getClientes()`
- **THEN** `criarSessaoNome(nome)` retorna um objeto referenciando essa mesma sessão, sem gerar um novo token

#### Scenario: Nome novo cria sessão com token
- **WHEN** não existe nenhum `SessaoCliente` com aquele `idUsuario`
- **THEN** `criarSessaoNome(nome)` retorna uma nova sessão com `token` não nulo e `idUsuario` igual ao nome informado

### Requirement: Os controles de pilotagem SHALL validar a existência do piloto antes de aplicar a mudança
`ControleJogosServer.mudarGiroMotor`, `mudarAgressividadePiloto`, `mudarTracadoPiloto`, `mudarDrs`, `mudarErs` e `boxPiloto` SHALL retornar um resultado de falha (sem lançar exceção) quando o piloto identificado por `idPiloto` não é encontrado na sessão/jogo do cliente.

#### Scenario: boxPiloto com piloto inexistente retorna falha sem exceção
- **WHEN** `boxPiloto(sessaoCliente, idPiloto, ...)` é chamado e o piloto não é encontrado para essa sessão
- **THEN** o método retorna `Boolean.FALSE` (ou equivalente de falha) em vez de lançar exceção
