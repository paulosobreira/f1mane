## 1. ControleDrs (primeiro — menor fricção, valida o padrão de wiring) ✅

- [x] 1.1 Criar `br.f1mane.controles.ControleDrs` com construtor `(InterfaceJogo)` — **desvio do design.md original**: não `(ControleJogoLocal, ControleCorrida)`. `ControleCorrida` era não utilizado (nenhum método de DRS chama nada nele); `InterfaceJogo` basta porque todos os métodos usados (`isDrs`, `isSafetyCarNaPista`, `isChovendo`, `isCorridaTerminada`, `obterCurvaAnterior`, `obterProxCurva`, `getNosDaPista`) já são da interface, sem necessidade de `instanceof JogoServidor` como em `ControleAutomacao`
- [x] 1.2 Mover `Piloto.processaUsoDRS()` (Piloto.java:2370-2395) e `Piloto.verificaPodeUsarDRS()` (2397-2423) para `ControleDrs`, trocando `this` pelo parâmetro `Piloto piloto` e usando só getters/setters já públicos (`isBox()`, `getPtosBox()`, `getNoAtual()`, `isAtivarDRS()`/`setAtivarDRS()`, `isPodeUsarDRS()`/`setPodeUsarDRS()`, `getCarro().setAsa()`, `getCarroPilotoDaFrenteRetardatario()`, `getDiffParaProximoRetardatario()`) — nenhum getter/setter novo foi necessário
- [x] 1.3 Adicionar `processarUsoDRS(Piloto)` a `InterfaceJogo`
- [x] 1.4 Implementar o método em `ControleJogoLocal` delegando para `controleCorrida.getControleDrs()`; instanciar e expor `controleDrs` em `ControleCorrida`
- [x] 1.5 Adicionar stub no-op em `JogoCliente`
- [x] 1.6 Atualizar `Piloto.processaNovoIndex()` para chamar `controleJogo.processarUsoDRS(this)` exatamente na posição onde `processaUsoDRS()` era chamado antes
- [x] 1.7 Remover `processaUsoDRS()`/`verificaPodeUsarDRS()` de `Piloto`
- [x] 1.8 Religar `PilotoDrsDesligadoNoBoxTest` — **mais simples do que previsto**: o teste já chamava `piloto.processaUsoDRS()` diretamente (não via `controleJogo`), então bastou trocar para `new ControleDrs(controleJogo).processaUsoDRS(piloto)`; não foi preciso nenhum helper de rewiring de mock (esse padrão só é necessário quando o teste exercita `Piloto` chamando o `controleJogo` internamente, o que não é o caso aqui)
- [x] 1.9 `mvn test`: 767/767 verde

## 2. ControleFreio ✅

- [x] 2.1 Criar `br.f1mane.controles.ControleFreio` com construtor `(InterfaceJogo)` (mesmo ajuste do D3 revisado — sem `ControleCorrida`)
- [x] 2.2 Adicionar `getFreioNaRetaMalSucedidoNesteTick()`/`setFreioNaRetaMalSucedidoNesteTick(Integer)` públicos em `Piloto` — o campo permanece em `Piloto` porque `processaStressFreioNaRetaMalSucedido()` (fora de escopo, cluster de estresse) o lê
- [x] 2.3 Mover `Piloto.processaFreioNaReta()` (Piloto.java:2264-2332) para `ControleFreio.processaFreioNaReta(Piloto)`; `retardaFreiandoReta`/`freioNaRetaAvaliadoNesteEvento` migraram para `Map<Piloto, Boolean>` dentro da classe nova (resolvendo a Open Question do design.md a favor do padrão `ControleAutomacao`, por consistência)
- [x] 2.4 Removidos `retardaFreiandoReta`/`freioNaRetaAvaliadoNesteEvento` de `Piloto`
- [x] 2.5 Adicionado `processarFreioNaReta(Piloto)` a `InterfaceJogo`, implementado em `ControleJogoLocal`, instanciado/exposto `controleFreio` em `ControleCorrida`, stub no-op em `JogoCliente`
- [x] 2.6 `Piloto.processaNovoIndex()` chama `controleJogo.processarFreioNaReta(this)` na mesma posição
- [x] 2.7 Religados `PilotoFreioNaRetaZonaFrenagemTest`, `PilotoProcessaFreioNaRetaMolhadoTest` **e `GanhoMolhadoEstatisticoTest`** (achado durante a implementação — não estava na lista original do design.md; também chamava `piloto.processaFreioNaReta()` diretamente) a uma instância real de `ControleFreio`, mesmo padrão de troca direta de chamada do grupo 1 (sem helper de rewiring)
- [x] 2.8 `mvn test`: 767/767 verde

## 3. ControleEscapada ✅

- [x] 3.1 Criar `br.f1mane.controles.ControleEscapada` com construtor `(InterfaceJogo)` (mesmo ajuste do D3 revisado)
- [x] 3.2 Adicionado `isImpedidoDeMudarTracadoPorEscapada()`/`setImpedidoDeMudarTracadoPorEscapada(boolean)` públicos em `Piloto` — o campo permanece em `Piloto` porque `Piloto.mudarTracado()` (fora de escopo) o lê
- [x] 3.3 Movidos `processaDerrapagem()` e o helper `proximaEscapadaNoCircuito()` para `ControleEscapada`
- [x] 3.4 Movidos `processaEscapadaDaPista()`, `processaEscapadaAncoradaAoTracado()`, `processaSaidaDaEscapada()`, `testeEscapadaStress()`/`precondicaoTesteEscapadaStress()`, `testeEscapadaPneus()`/`precondicaoTesteEscapadaPneus()`, `notificaTesteEscapada()`/`notificaEscapadaExecutada()`/`elegivelParaNotificacaoDeEscapada()`, `escapadaAtivaNoTracadoDeFuga()`, `proximaEscapadaNoTracadoAtual()`, `laneDeFugaDoTracadoOrigem()`/`tracadoOrigemDoLaneDeFuga()`, `garanteCacheDeTesteEscapadaDaVoltaAtual()` para `ControleEscapada`. `JANELA_ENTRADA_BOX_FORCADA` (interpolada no meio do bloco original) foi identificada como pertencente à lógica de entrada no box — não movida, ficou em `Piloto`
- [x] 3.5 Migrados `modoPilotagemAntesDaFuga`, `giroAntesDaFuga`, `estavaNoTracadoDeFuga`, `resultadoTesteEscapadaPorZonaNestaVolta`, `numeroVoltaDoCacheDeTesteEscapada` para dentro de `ControleEscapada` como `Map<Piloto, EstadoEscapada>` (classe interna privada), removidos de `Piloto`
- [x] 3.6 Adicionados `processarDerrapagem(Piloto)` e `processarEscapadaDaPista(Piloto)` a `InterfaceJogo`, implementados em `ControleJogoLocal`, instanciado/exposto `controleEscapada` em `ControleCorrida`, stubs no-op em `JogoCliente`
- [x] 3.7 `Piloto.processaNovoIndex()` chama as duas via `controleJogo`, mesma posição relativa
- [x] 3.8 Religados `PilotoDerrapagemTest`, `PilotoEscapadaAncoradaTracadoTest` **e `PilotoEntradaBoxCamadasTest`** (achado durante a implementação — não estava na lista original, chamava `piloto.processaEscapadaDaPista()` uma vez) a uma instância real de `ControleEscapada`, mesmo padrão de troca direta de chamada. `PilotoGanhoTracadoDeFugaTest` e `CircuitoEscapadaTracadoTest` confirmados sem necessidade de alteração (tocam `calculaModificadorPrincipal()`/`Circuito.reprocessarEscapadas()`, fora de escopo)
- [x] 3.9 `mvn test`: 767/767 verde

## 4. Verificação final ✅

- [x] 4.1 Grep confirma que `Piloto.java` só referencia os métodos/campos movidos em javadoc (atualizados para citar as classes novas), nenhuma referência de código
- [x] 4.2 `mvn test` completo com as 3 extrações juntas: 767/767 verde (mesmo total de antes — nenhum teste perdido)
- [x] 4.3 Build do jar (`mvn package -Ph2 -DskipTests`) + `java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Catalunya 5`: corrida de 5 voltas completou normalmente ("========final corrida============" no log), sem exceções nos caminhos de derrapagem/escapada/freio/DRS. (Não foi feita comparação bit-a-bit com seed fixa antes/depois — a suíte de 767 testes, muitos deles dependentes de RNG mockado determinístico, já cobre isso; a simulação serviu como smoke test end-to-end adicional)
- [x] 4.4 `git diff --stat`: `Piloto.java` caiu de 4356 para 3701 linhas (-655, ~15%) — 3 classes novas somam 716 linhas (`ControleDrs` 77, `ControleFreio` 105, `ControleEscapada` 534)
