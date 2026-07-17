## 1. Tempo médio de volta

- [x] 1.1 Adicionar `tempoMedioVoltaMs()` a `InterfaceJogo`
- [x] 1.2 Implementar em `ControleJogoLocal`: média de `Volta.obterTempoVolta()` (convertida via `tempoCicloCircuito()`) das voltas já registradas do líder (`getPilotos().get(0)`), com fallback quando o líder ainda não completou nenhuma volta
- [x] 1.3 Cachear o valor calculado, recalculando apenas quando o líder fecha uma nova volta (mesmo gatilho de `processaNovaVolta()`), não a cada tick
- [x] 1.4 Implementar o equivalente em `JogoCliente` (fluxo multiplayer) — descoberta na implementação: `JogoCliente` não roda `ControleClima`/`ThreadMudancaClima` (a simulação de clima é autoritativa no servidor, via `JogoServidor extends ControleJogoLocal`); implementado como retorno direto do mesmo fallback
- [x] 1.5 Teste unitário: `tempoMedioVoltaMs()` retorna a média correta com 1, 2 e N voltas registradas do líder
- [x] 1.6 Teste unitário: `tempoMedioVoltaMs()` usa o fallback quando o líder não tem nenhuma volta registrada
- [x] 1.7 **Bug pós-implementação, relatado pelo usuário**: clima nunca mudava numa corrida iniciada em NUBLADO mesmo com `probalidadeChuva=99`. Causa raiz: o fallback original (`nosDaPista.size() * tempoCicloCircuito()`) tratava os 16.829 nós densos/interpolados em pixels de Interlagos como 16.829 ciclos de avanço (1 nó = 1 ciclo), dando ~44,9 minutos de "tempo médio de volta" em vez de ~90s — travando `ThreadMudancaClima` num sleep real de quase 45 minutos sempre que o fallback fosse usado. Corrigido reaproveitando `Circuito.estimarTempoVoltaMs()` (Circuito.java:740-752, já existente, usado pelo editor pra calibrar `ciclo`), que soma `1/ganhoMédio(tipoDoNó)` por nó em vez de 1 ciclo fixo — dá 87.675ms para Interlagos. Aplicado em `ControleJogoLocal.calculaTempoMedioVoltaMs()` e `JogoCliente.tempoMedioVoltaMs()`; teste 1.6 atualizado para refletir a fórmula corrigida

## 2. Timing do disparo da mudança de clima

- [x] 2.1 Alterar `ThreadMudancaClima.run()` para dormir `intervalo(0, tempoMedioVoltaMs)` em vez do intervalo fixo `intervalo(3000, 15000)`
- [x] 2.2 Teste unitário: o atraso de disparo respeita o limite superior de `tempoMedioVoltaMs` fornecido pelo mock de `ControleClima`/`InterfaceJogo`

## 3. Estado "molhado%" em ControleClima

- [x] 3.1 Adicionar a `ControleClima` os campos de estado da rampa: valor atual (0.0–1.0), direção-alvo, e timestamp/referência de início do trecho de rampa em curso
- [x] 3.2 Implementar `getMolhado()`: calcula o valor interpolado no momento da consulta a partir do ciclo de início, direção-alvo e `tempoMedioVoltaMs`, sem exceder os limites 0.0/1.0 — usa `getCicloAtual()` (ciclos de simulação) como relógio, não tempo de parede, pra ficar consistente independente de `ControleCiclo.VALENDO`
- [x] 3.3 Disparar início/redirecionamento de rampa num único ponto: dentro de `setClima(String)`, comparando clima anterior e novo — cobre `intervaloChuva()`/`intervaloNublado()`/`intervaloSol()` (via `ThreadMudancaClima`) E `climaLimpo()`/`climaChuvoso()` (debug manual) automaticamente, sem precisar instrumentar cada um; sem alterar `molhado%` em transições `SOL`↔`NUBLADO` que não envolvam `CHUVA`
- [x] 3.4 Implementar a reversão a partir do valor atual, à taxa constante (1.0 unidade por `tempoMedioVoltaMs`, não uma interpolação proporcional à distância do trecho) — corrigido durante os testes: a primeira versão interpolava sobre a duração total do trecho, fazendo uma reversão parcial "andar mais devagar" que uma rampa cheia, o que contradizia a taxa constante do design
- [x] 3.5 Teste unitário: rampa completa de 0.0 a 1.0 ao longo de `tempoMedioVoltaMs` ao entrar em `CHUVA`
- [x] 3.6 Teste unitário: rampa completa de 1.0 a 0.0 ao longo de `tempoMedioVoltaMs` ao sair de `CHUVA`
- [x] 3.7 Teste unitário: transição `SOL`↔`NUBLADO` isolada não altera `molhado%`
- [x] 3.8 Teste unitário: reversão no meio da rampa (em 0.4 subindo) inverte a partir do valor atual, sem saltar para os extremos, na mesma taxa constante

## 4. Interpolação das fórmulas de ganho

- [x] 4.1 Refatorar `Carro.calculaModificadorPneu` para computar o resultado "seco" (fórmula hoje aplicada quando não chove) e o resultado "molhado" (fórmula hoje aplicada quando chove) e misturar por `molhado%`, preservando ambas as fórmulas como os extremos 0.0/1.0
- [x] 4.2 Refatorar `Piloto.calculaModificadorPrincipal`: `comparador -= molhado% * (testeHabilidadePilotoAerodinamica() ? 0.2 : 0.3)` em vez da subtração condicionada a `isChovendo()`
- [x] 4.3 Refatorar `Piloto.processaFreioNaReta`: `minMulti -= molhado% * 0.3`, mantendo `retardaFreiandoReta = false` atrelado a `isChovendo()` puro (não a `molhado%`)
- [x] 4.4 Confirmar que `Carro.calculaModificadorAsa` permanece inalterado (sem leitura de `molhado%`)
- [x] 4.5 Teste unitário: `molhado% = 0.0` reproduz bit-a-bit o resultado da fórmula seca atual nos três pontos
- [x] 4.6 Teste unitário: `molhado% = 1.0` reproduz bit-a-bit o resultado da fórmula de chuva atual nos três pontos
- [x] 4.7 Teste unitário: `molhado%` intermediário produz resultado entre os dois extremos nos três pontos — para `calculaModificadorPrincipal` (saída discreta: banda alta/baixa), "entre os extremos" foi demonstrado pelo ponto de corte do sorteio se deslocando monotonicamente com `molhado%`, não por um terceiro valor numérico
- [x] 4.8 Teste unitário: `Carro.calculaModificadorAsa` não muda de resultado para diferentes valores de `molhado%` com o mesmo clima categórico

## 5. Pneu de chuva obrigatório no pit stop

- [x] 5.1 Em `ControleJogoLocal.setUpJogadorHumano`, antes de `Carro.trocarPneus`, sobrescrever `tipoPneu` para `Carro.TIPO_PNEU_CHUVA` quando `Clima.CHUVA.equals(clima)` no momento do pit stop
- [x] 5.2 Investigar o fluxo de pit stop do jogador humano no multiplayer/servidor — resolvido: `JogoServidor.setUpJogadorHumano` é só `return super.setUpJogadorHumano(...)` (herda de `ControleJogoLocal`, já corrigido em 5.1); `JogoCliente.setUpJogadorHumano` só encaminha a seleção pro servidor via `monitorJogo.alterarOpcoesBox(...)`, não decide o pneu. Não existe fluxo separado a corrigir.
- [x] 5.3 Confirmar que a lógica de box da IA (`ControleBox.java:444-457`/`538-550`) já cobre o caso e não precisa de alteração — confirmado, usa `isChovendo()` categórico diretamente
- [x] 5.4 Teste unitário: jogador humano que seleciona pneu seco durante `Clima.CHUVA` sai do box com `TIPO_PNEU_CHUVA`
- [x] 5.5 Teste unitário: jogador humano que seleciona pneu seco fora de `CHUVA` sai do box com o pneu escolhido, sem sobrescrita
- [x] 5.6 Teste unitário: após a chuva parar (`clima` não é mais `CHUVA`), a escolha de pneu no pit stop volta a ser livre

## 6. Validação estatística: chuva não supera seco em curvas, em expectativa

- [x] 6.1 Escrever harness de teste (Monte Carlo, JUnit) que amostra `ganho` (3000 amostras, `GameRandom` real com seed fixa 12345 — reprodutível, não a seed=1 especial) no mesmo nó de curva, mesmo pneu/asa/potência/habilidade, comparando `molhado% = 0.0` vs `molhado% = 1.0`, para os três pontos (`Carro.calculaModificadorPneu`, `Piloto.calculaModificadorPrincipal`, `Piloto.processaFreioNaReta`)
- [x] 6.2 Teste: média de `ganho` amostrado em `molhado% = 1.0` é estatisticamente menor que a média em `molhado% = 0.0`, em curva baixa (os três pontos) — curva alta já é coberta indiretamente pelos mesmos pontos de código (mesmo `!no.verificaRetaOuLargada()`/`!reta` gate de curva baixa e alta)
- [x] 6.3 Teste: em nó de reta fora de zona de frenagem, amostras de `ganho` com `molhado% = 0.0` e `molhado% = 1.0` têm média idêntica (bit-a-bit, não só estatisticamente — o código nem lê `molhado%` nesse caso)
- [x] 6.4 Teste: em zona de frenagem cuja próxima curva é curva alta, amostras de `ganho` com `molhado% = 0.0` e `molhado% = 1.0` têm média idêntica (bit-a-bit — bloco inteiro é pulado, `molhado%` nunca é lido)
- [x] 6.5 Documentado no Javadoc da classe de teste (`GanhoMolhadoEstatisticoTest`) que a garantia é estatística — amostras isoladas discordantes são esperadas e não indicam falha

## 7. Regressão e verificação

- [x] 7.1 Rodar a suíte completa (`mvn test`) — 765 testes, 0 falhas, 0 erros, 2 skips pré-existentes (não relacionados a esta change)
- [x] 7.2 Simular headless (`java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Interlagos 40`, Interlagos = 70% de chance de chuva) e observar nos logs — corrida completou as 40 voltas sem exceção, com transição real SOL→NUBLADO→CHUVA ("Mudança de Clima. O ceu esta nublado." seguido de "Mudança de Clima. Começa a chover."). A verificação numérica da rampa gradual de `ganho` (em vez do salto abrupto) já está coberta pelos testes Monte Carlo do grupo 6; esta simulação confirma a integração ponta a ponta numa corrida real, sem crashes

## 8. Ajustes pós-implementação (feedback do usuário testando em jogo real)

- [x] 8.1 **Logs de debug**: adicionados em `ControleClima`/`ThreadMudancaClima` — tentativa disparada/adiada, clima mudou/permaneceu, e o percentual de `molhado%` a cada mudança detectável (não a cada consulta, que é por ciclo por carro)
- [x] 8.2 **Bug — clima nunca mudava com `probalidadeChuva=99`**: fallback `nosDaPista.size() * tempoCicloCircuito()` tratava nós densos em pixels (16.829 no Interlagos) como ciclos de avanço, dando ~45min em vez de ~90s. Corrigido para `Circuito.estimarTempoVoltaMs()` (registrado em 1.7 acima)
- [x] 8.3 **Bug — `NullPointerException` ao entrar no box**: `PainelCircuito.centralizaCarroDesenhar` não calculava `p4`/`p5` pra nós do box, quebrando quando um piloto chegava ao box vindo da faixa de fuga (traçado 5). Sem relação com esta change, mas bloqueava a verificação (jogo travava/degradava antes de conseguir observar mudanças de clima). Corrigido com o mesmo fallback já usado em outro ramo do método (`p4=p2`, `p5=p1`). Coberto por `PainelCircuitoCentralizaCarroBoxTest`
- [x] 8.4 **Pedido — avaliação a cada volta, sem espera de intervalo sorteado**: removido o gate pré-existente de `voltaMudancaClima`/intervalo inicial sorteado em `processaPossivelMudancaClima()`. Agora tenta disparar toda volta, só adiando se a tentativa anterior ainda estiver em andamento (ver D6 em design.md) — **revertido em 8.9**, ver abaixo
- [x] 8.5 **Bug — falha em `ThreadMudancaClima` travava o sistema pra sempre** ("adiando toda volta"): `processada` só era marcado no caminho de sucesso; qualquer exceção (ex.: `InterruptedException`) deixava a thread presa em `isProcessada()==false` pra sempre, bloqueando novas tentativas pelo resto da corrida. Corrigido movendo `processada = true` pro bloco `finally`. Coberto por `ThreadMudancaClimaDisparoTest.run_falhaComExcecao_aindaAssimMarcaProcessada` (verificado que falha sem a correção, passa com ela)
- [x] 8.6 **Pedido — tempo mínimo de volta de 1 minuto pro clima**: fallback de `tempoMedioVoltaMs()` trocado de `circuito.estimarTempoVoltaMs()` (podia passar de 1min, ex. 87,7s no Interlagos) pra um valor fixo `Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS = 60_000L`, garantindo que a primeira tentativa de mudança de clima da corrida dispare em até 1 minuto real. Teste `ControleJogoLocalTempoMedioVoltaMsTest` atualizado
- [x] 8.7 **Pedido — log porcentagem a porcentagem da rampa de `molhado%`**: `ControleClima` agora loga cada ponto percentual novo que a rampa atravessa (`ultimoPercentualMolhadoLogado`), não só início/fim — verificado via `ControleClimaMolhadoTest` mostrando o rastro completo (ex.: 0%→50%→100%)
- [x] 8.8 Suíte completa re-executada após cada ajuste — 767 testes, 0 falhas, 2 skips pré-existentes
- [x] 8.9 **Pedido — reverter 8.4, voltar ao mecanismo da proposta inicial**: restaurado o campo `voltaMudancaClima` e o gate de voltas em `processaPossivelMudancaClima()` (ver D9 em design.md). Mantidos, por serem correções/pedidos independentes do gate em si: piso de 1min (8.6), fix do `finally` (8.5), logs de debug (8.1/8.7, ajustados pra logar de novo o sorteio do intervalo inicial e a volta da próxima avaliação). Suíte completa re-executada — 767 testes, 0 falhas. Confirmado em corrida real: "Intervalo inicial sorteado: 24 voltas" voltou a aparecer, voltas 1-23 só mostram "proxima avaliacao na volta 24" sem tentativa
