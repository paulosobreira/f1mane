## Why

A mecânica de "piloto desconcentrado" (`ciclosDesconcentrado`/`verificaDesconcentrado()`) é um estado puramente interno — nunca exposto ao jogador em nenhuma tela, JSON ou painel de debug (`@JsonIgnore`, zero chamadores externos) — que bloqueia por completo, durante 5 a 700 ciclos dependendo do gatilho, uma faixa larga de comportamentos do piloto: todo o teste de habilidade (`testeHabilidadePiloto()`, usado em quase todo cálculo de ganho, ultrapassagem, troca de traçado, uso de DRS/ERS), toda a tomada de decisão de IA por tick (`processaIAnovoIndex()`), tentativas de DRS/ERS, escape de fila indiana, mudança de traçado pra fora das faixas de escape, e força o piloto a sair do modo agressivo/giro máximo. Como o jogador não tem nenhuma forma de perceber esse estado, o comportamento resultante (um carro que simplesmente para de reagir/ultrapassar/testar por um tempo) parece um bug em vez de uma característica do piloto.

## What Changes

- **BREAKING (comportamento de simulação)**: remover por completo `ciclosDesconcentrado`, `verificaDesconcentrado()`, `decrementaPilotoDesconcentrado()`, `desconcentradoAgressivoNesteTick` e `processaStressDesconcentradoAgressivo()` de `Piloto.java`.
- Remover o gate `verificaDesconcentrado()` de `tentarEscaparFilaIndiana()`, `processaIAnovoIndex()`, `iaTentaUsarDRS()`, `iaTentaUsarErs()`, `testeHabilidadePiloto()` e `mudarTracado()` — esses métodos passam a rodar sua lógica normal incondicionalmente (nenhum bloqueio silencioso).
- Em `processaMudancaRegime()`, trocar a condição de downgrade forçado (AGRESSIVO→NORMAL, giro máximo→normal) de `verificaDesconcentrado()` para um limiar de estresse — preserva o efeito de "piloto se acalma sob pressão" de forma visível (correlacionada com estresse) em vez de um estado invisível.
- Converter os 4 pontos que hoje setam `ciclosDesconcentrado` (escapar da pista sob estresse alto em modo agressivo; ceder passagem a carro mais rápido atrás; ser ultrapassado/mensagem de retardatário; largada ruim em `ControleQualificacao`) para, em vez de iniciar um bloqueio temporizado, gerar um incremento de estresse proporcional ao evento — usando o mecanismo já centralizado em `Piloto.incStress()`.
- Manter a mensagem informativa de "problema na largada" (`isProblemaLargada()`), disparando-a no momento do evento em vez de esperar o fim de uma contagem regressiva que deixa de existir.
- Remover os testes que cobrem o mecanismo de bloqueio/flag descontinuado (`decrementaPilotoDesconcentrado_*`, `processaStressDesconcentradoAgressivo_*` em `PilotoProcessaStressConsolidadoTest.java`) e adicionar testes cobrindo que as ações antes bloqueadas agora procedem normalmente, e que os 4 gatilhos geram estresse.

## Capabilities

### New Capabilities
- `piloto-sem-bloqueio-por-desconcentracao`: documenta que nenhuma ação do piloto (teste de habilidade, decisão de IA por tick, DRS/ERS, escape de fila indiana, troca de traçado) é mais suprimida por um estado interno invisível; e que o downgrade de modo de pilotagem em `processaMudancaRegime()` passa a ser condicionado a um limiar de estresse observável em vez do estado removido.

### Modified Capabilities
- `piloto-gestao-estresse`: adiciona 4 novos gatilhos de incremento de estresse (escapada de pista sob pressão, ceder passagem, ser ultrapassado, largada ruim) substituindo o antigo bloqueio por `ciclosDesconcentrado`; remove o gatilho `processaStressDesconcentradoAgressivo` (obsoleto, dependia do mecanismo removido).

## Impact

- **Código afetado**: `Piloto.java` (remoção de campos/métodos de desconcentração; alteração de 6 métodos que tinham o gate; conversão dos 4 pontos de gatilho; `processaMudancaRegime()`), `ControleQualificacao.java` (gatilho de largada ruim).
- **Testes**: remoção dos testes do mecanismo descontinuado; novos testes confirmando que as ações antes bloqueadas (teste de habilidade, decisão de IA, DRS/ERS, escape de fila, mudança de traçado) não são mais suprimidas, e que os 4 gatilhos agora geram estresse com a magnitude definida em design.md.
- **Sem mudança de API pública** além da remoção dos métodos/campos citados (todos já eram de uso interno, sem chamadores externos ao `Piloto`/`ControleQualificacao`).
- **Efeito de jogo esperado**: pilotos deixam de "congelar" silenciosamente após incidentes; o efeito de "abalo" desses eventos passa a se manifestar via acúmulo de estresse (que já tem consequências visíveis no sistema existente), sujeito a observação em simulação conforme pedido.
