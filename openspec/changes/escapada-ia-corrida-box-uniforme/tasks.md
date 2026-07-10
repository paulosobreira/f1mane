## 1. Reconexão de pista4Full/pista5Full aos ObjetoEscapada

- [x] 1.1 `GeoUtil.pontoNoTrajeto(List<Point>, double)` criado (movido de `TestePista`, que agora delega para ele) — reutilizável fora do editor, sem `br.f1mane.entidades` depender de `br.f1mane.editor`.
- [x] 1.2 `Circuito.gerarEscapeMap()` reescrito: `escapeMap` continua sempre vazio; `pista4Full`/`pista5Full` inicializadas do tamanho de `pistaFull` (todas `null`); novo helper `preencherTracadoEscapada` popula `pista5Full` (`tracadoOrigem==1`) ou `pista4Full` (`tracadoOrigem==2`) nos índices `[indiceEntrada, indiceSaida]` com nós interpolados via `GeoUtil.pontoNoTrajeto`, `setTracado(4 ou 5)` e `setIndex(index)`. **(corrigido em 8.1 — o mapeamento original desta task, `1→4`/`2→5`, estava invertido)**
- [x] 1.3 `preencherTracadoEscapada` retorna sem efeito (sem exceção) quando `indiceEntrada<0`, `indiceSaida<=indiceEntrada`, ou `obterPontosAbsolutos()` nulo/menos de 2 pontos.
- [x] 1.4 Confirmado: `gerarEscapeMap()` continua chamado só por `vetorizarPista()` e `reprocessarEscapadas()` — nenhum novo ponto de chamada adicionado.

## 2. Comprometimento e execução da escapada em Piloto.java

- [x] 2.1 `proximaEscapadaNoTracadoAtual(int)` adicionado: percorre `controleJogo.getCircuito().getObjetos()`, filtra `ObjetoEscapada` com `tracadoOrigem == tracadoAtual` e `indiceSaida >= índice atual` (não totalmente ultrapassada — mais robusto que só `indiceEntrada >= índice atual`, ver nota abaixo), retorna a de menor `indiceEntrada`.
- [x] 2.2 Novo método `processaEscapadaAncoradaAoTracado()` chamado ao final de `processaEscapadaDaPista()`: calcula `distancia = indiceEntrada - índice atual`; comprometido quando `distancia <= 50` e (`AGRESSIVO`+stress acima do limite, ou `Global.FORCAR_ESCAPADA_TESTE`).
- [x] 2.3 Quando não comprometido e `distancia > 0` (ainda não chegou), tenta `mudarTracado(0)` sem forçar.
- [x] 2.4 Quando `distancia <= 0` (chegou ou passou da entrada, ainda dentro da zona pois `indiceSaida >= índice atual`) e `getTracado()` ainda é o traçado original, força `mudarTracado(5 ou 4, true)` (traçado 5 pra origem 1, traçado 4 pra origem 2 — ver correção em 8.1). **Desvio da task original**: usei `<= 0` em vez de `== 0` porque o índice avança por `avancoLimitado` (pode ser >1 por ciclo — ver `Piloto.processaNovoIndex`), então um salto pode pular exatamente por cima do índice de entrada; `<=0` combinado com o filtro `indiceSaida >= índice atual` em 2.1 cobre esse caso sem risco de disparar numa zona já totalmente ultrapassada.
- [x] 2.5 Confirmado: `processaEscapadaAncoradaAoTracado()` é chamado só no final do método, depois de todos os `return` antecipados existentes (safety car, qualify, ptosBox, e os dois ramos do gatilho cego).

## 3. Velocidade e modo durante a escapada

- [x] 3.1 Adicionado ao `if (verificaForaPista(this))` existente: `getCarro().setGiro(Carro.GIRO_MIN_VAL);` e `setModoPilotagem(LENTO);`, junto do `ganho *= 0.50` já existente.
- [x] 3.2 Confirmado por leitura: `verificaForaPista` cobre traçado 4/5 e a janela de retorno (`voltando`) no mesmo booleano, então o mesmo `if` cobre ambas as fases.

## 4. Variável global de teste

- [x] 4.1 `Global.FORCAR_ESCAPADA_TESTE` adicionado (boolean, default false, javadoc explicando o escopo).
- [x] 4.2 Usada em `processaEscapadaAncoradaAoTracado()` como alternativa (`||`) à checagem de agressividade+estresse.

## 5. Box com velocidade única

- [x] 5.1 Campo `boxRapido` e o sorteio no construtor de `ControleBox` removidos.
- [x] 5.2 4 constantes `static final` (`VELOCIDADE_BOX_LIMITE=18`, `VELOCIDADE_BOX_RETA=23`, `VELOCIDADE_BOX_CURVA_ALTA=18`, `VELOCIDADE_BOX_DEMAIS=13`, calculadas via `Math.round((X+Y)/2.0f)` uma vez, campo estático) substituem os 4 pares condicionais em `processarPilotoBox`.
- [x] 5.3 `isBoxRapido()` removido de `ControleBox`, `InterfaceJogo`, `ControleJogoLocal` e `JogoCliente`.
- [x] 5.4 `Piloto.java`: `limiteUltimasVoltas` agora é sempre `83` (fixo), sem branch por `isBoxRapido()`.
- [x] 5.5 Removidas as referências de log/debug em `MainFrameSimulacao.java` (campo `boxRapido`, atribuição, log) e `ControleJogoLocal.java` (linha do dump de debug).

## 6. Testes automatizados

- [x] 6.1 `CircuitoEscapadaTracadoTest` (novo, `src/test/java/br/f1mane/entidades/`): escapada em `tracadoOrigem==1` popula só `pista5Full` no intervalo certo; `tracadoOrigem==2` popula só `pista4Full`; sem `ObjetoEscapada` mantém ambas nulas; trajeto degenerado não gera exceção nem preenche.
- [x] 6.2 Coberto em `PilotoEscapadaAncoradaTracadoTest` (novo, `src/test/java/br/f1mane/entidades/`): `agressivoEEstressado_dentroDe50Indices_comprometeSemTentarDesviar` e `agressivoEEstressado_foraDaJanelaDe50_aindaTentaDesviar`.
- [x] 6.3 Coberto no mesmo arquivo: `pilotoNormal_dentroDe100Indices_tentaDesviarComSucesso` (sucesso) e `pilotoNormal_alcancaAEntradaAindaNoTracadoOrigem1_forcaEscapadaNoTracado5` (falha implícita — chega na entrada sem ter conseguido sair antes — força a escapada).
- [x] 6.4 Coberto: `pilotoNormal_alcancaAEntradaAindaNoTracadoOrigem1_forcaEscapadaNoTracado5` (traçado 5, origem 1) e `..._forcaEscapadaNoTracado4` (traçado 4, origem 2).
- [x] 6.5 Coberto: `velocidadeEModoReduzidosDuranteATracado4` e `velocidadeEModoReduzidosDuranteORetornoDaTracado5`.
- [x] 6.6 Coberto: `flagGlobalDeTeste_comprometePilotoNormalNaoEstressado` e `flagGlobalDeTeste_naoDispensaExigenciaDePosicaoNoTracado0`.
- [x] 6.7 `ControleBoxVelocidadeUniformeTest` (novo, `src/test/java/br/f1mane/controles/`): incremento de reta/largada é sempre 23 em 3 execuções seguidas; `limiteUltimasVoltasParaDecidirParada_ehUnico_83` confirma o limite fixo em 83 (82% não reverte, 84% reverte).
- [x] 6.8 `gatilhoCegoExistente_continuaFuncionandoSemAlteracao` adicionado a `PilotoEscapadaAncoradaTracadoTest` (não havia cobertura antes) — confirma que o gatilho cego (curva baixa, traçado 0, agressivo+estressado) continua empurrando pro traçado 1/2 aleatório, sem relação com a nova lógica.
- [x] 6.9 `mvn test` completo rodado (ver resultado ao final desta sessão) — 0 falhas.

## 8. Correção do mapeamento traçado↔lane de fuga e retorno ao traçado de origem

**(adicionado após feedback do usuário — bug real: carros saíam pelo traçado 1 e voltavam no traçado 2)**

- [x] 8.1 Corrigido o mapeamento `tracadoOrigem`→traçado de fuga em `Circuito.preencherTracadoEscapada` e `Piloto.processaEscapadaAncoradaAoTracado`: era `1→4`/`2→5` (invertido), passou a `1→5`/`2→4` — confirmado por `Piloto.mudarTracado` (bloqueia incondicionalmente 4→{0,1} e 5→{0,2}, ou seja só permite retornar de 4 pra 2 e de 5 pra 1) e pelo antigo `escapaTracado()`/`ladoEscape` (exigia traçado 1 pro lado 5, traçado 2 pro lado 4).
- [x] 8.2 Novo método `Piloto.processaSaidaDaEscapada()`, chamado ao final de `processaEscapadaDaPista()` (depois de `processaEscapadaAncoradaAoTracado()`): enquanto `getTracado()` é 4 ou 5, busca a `ObjetoEscapada` ativa nesse traçado de fuga (`escapadaAtivaNoTracadoDeFuga`) e, a 100 índices de nó ou menos de `indiceSaida`, faz `testeHabilidadePiloto()` a cada ciclo; no sucesso, força `mudarTracado` de volta para o traçado de origem (2 vindo de 4; 1 vindo de 5) — nunca o outro lateral, porque `mudarTracado` não permite mais nenhum outro destino a partir de 4/5.
- [x] 8.3 Helpers privados `laneDeFugaDoTracadoOrigem(int)` e `tracadoOrigemDoLaneDeFuga(int)` centralizam a conversão nos dois sentidos, evitando repetir a regra 1↔5/2↔4 espalhada pelo código.
- [x] 8.4 Testes atualizados/adicionados em `PilotoEscapadaAncoradaTracadoTest`: os dois testes de execução da entrada corrigidos pro mapeamento certo (`forcaEscapadaNoTracado5` pra origem 1, `forcaEscapadaNoTracado4` pra origem 2); 4 novos testes cobrindo o retorno (`saidaDaEscapada_...`): sucesso com habilidade alta volta pro traçado de origem correto (1 e 2), falha no teste de habilidade mantém no traçado de fuga, fora da janela de 100 índices não tenta.
- [x] 8.5 Testes de `CircuitoEscapadaTracadoTest` e os docs de spec/design corrigidos pro mesmo mapeamento (`1→pista5Full`, `2→pista4Full`).
- [x] 8.6 `mvn test` completo rodado novamente após a correção — 0 falhas (ver resultado no fim da sessão).

## 9. Correção da taxa de escapada muito alta (entrada já passada + janela reduzida)

**(adicionado após feedback do usuário — bug real: qualquer carro dentro do intervalo de uma zona, mesmo tendo passado da entrada há muito tempo, era forçado a escapar)**

- [x] 9.1 Nova constante `Piloto.TOLERANCIA_INDICES_ENTRADA_JA_PASSADA = 20`: em `processaEscapadaAncoradaAoTracado`, quando `distancia < -20` a zona deixa de ser elegível (entrada considerada perdida) — só a próxima zona à frente pode disparar a escapada.
- [x] 9.2 Janela de comprometimento (agressivo+estressado, ou `Global.FORCAR_ESCAPADA_TESTE`) reduzida de `distancia <= 50` para `distancia <= 40`.
- [x] 9.3 Testes adicionados/renomeados em `PilotoEscapadaAncoradaTracadoTest`: `agressivoEEstressado_dentroDe40Indices_...`, `agressivoEEstressado_umIndiceAlemDaJanelaDe40_...` (limite exato do novo threshold), `carroDentroDaZonaMuitoAlemDaEntrada_naoForcaEscapada_...` (regressão do bug), `carroLigeiramenteAlemDaEntrada_dentroDaTolerancia_aindaForcaEscapada` (garante que o salto de 1 ciclo continua funcionando).
- [x] 9.4 Specs/design atualizados: `escapada-ia-corrida/spec.md` (janela 50→40, novo requisito de tolerância/inelegibilidade), `design.md` (D9).
- [x] 9.5 `mvn test` completo rodado novamente — 0 falhas.

## 8. Sugestões pendentes de aprovação — reduzir ainda mais a frequência de escapadas (especialmente na volta 1)

**(discutido com o usuário — 8b implementada; as demais aguardam decisão)**

- [ ] 8a. Ignorar a lógica nova inteira (`processaEscapadaAncoradaAoTracado`/`processaSaidaDaEscapada`) durante a volta 1 (`controleJogo.getNumVoltaAtual() <= 1`), no mesmo espírito de `iaTentaUsarDRS()` (`getNumVoltaAtual() > 1`) — volta 1 tem caos de largada com muita troca de traçado lateral, inflando a chance de coincidir com uma zona.
- [x] 8b. Novo `Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA` (default 90, igual ao antigo), usado só em `processaEscapadaAncoradaAoTracado` no lugar de `getValorLimiteStressePararErrarCurva()`/`Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA` — o gatilho cego antigo continua usando o threshold original, intocado; agora dá pra calibrar a mecânica nova sem afetar a antiga.
- [ ] 8c. Gate de probabilidade adicional (ex.: `controleJogo.getRandom().nextDouble() < CHANCE_ESCAPAR`) mesmo quando comprometido, pra nem todo comprometimento resultar em escapada de fato.
- [ ] 8d. Tornar a tentativa de desvio (`mudarTracado(0)` no não-comprometido) forçada (`forcaMudar=true`), pra ter mais sucesso e gerar menos "falhas de desvio" que acabam em escapada.

## 10. Piloto em modo LENTO nunca escapa + teste de habilidade como última chance no gatilho

**(adicionado após feedback do usuário)**

- [x] 10.1 No gatilho da escapada (`distancia <= 0` em `processaEscapadaAncoradaAoTracado`): se `modoPilotagem == LENTO`, retorna sem escapar (nunca escapa, mesmo comprometido por stress+agressividade ou pela flag de teste — cobre também o caso, antes não coberto, de um piloto LENTO alcançar a entrada via falha de desvio).
- [x] 10.2 Caso não esteja em LENTO, `testeHabilidadePiloto()` é chamado antes de forçar a escapada; em caso de sucesso, `setModoPilotagem(LENTO)` e retorna sem escapar; em caso de falha, força a escapada normalmente (`mudarTracado` como antes).
- [x] 10.3 Testes adicionados em `PilotoEscapadaAncoradaTracadoTest`: `pilotoEmModoLento_naoEscapaNoGatilho_mesmoComprometido`, `testeDeHabilidadeBemSucedidoNoGatilho_mudaParaLentoENaoEscapa`, `testeDeHabilidadeFalhaNoGatilho_forcaEscapadaNormalmente`.
- [x] 10.4 Spec (`escapada-ia-corrida/spec.md`, novo requisito) e design (`design.md`, D10) atualizados.
- [x] 10.5 `mvn test` completo rodado novamente — 0 falhas.

## 11. Jogador humano em modo manual não recebe o teste de habilidade automático

**(adicionado após feedback do usuário)**

- [x] 11.1 `processaEscapadaAncoradaAoTracado` passa a checar `isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())` antes de chamar `testeHabilidadePiloto()` no gatilho — se for jogador humano manual, pula direto pra forçar a escapada (mesmo padrão de checagem já usado em `processaMudarTracado`).
- [x] 11.2 Checagem de `modoPilotagem == LENTO` continua valendo igual pra jogador humano manual — se ele mesmo colocou o carro em LENTO, não escapa.
- [x] 11.3 Testes adicionados em `PilotoEscapadaAncoradaTracadoTest`: `jogadorHumanoEmModoManual_naoRecebeTesteDeHabilidade_escapaNormalmente`, `jogadorHumanoEmModoManual_seJaEstiverEmLento_naoEscapa`, `jogadorHumanoEmModoAutomatico_continuaRecebendoOTesteDeHabilidade` (regressão: humano em modo AUTOMATICO continua tratado como IA).
- [x] 11.4 Achado durante os testes (fora do escopo, não corrigido): `processaSaidaDaEscapada` (retorno da escapada) tem seu próprio teste de habilidade independente que não checa jogador humano/manual — anotado em D11 do design.md como possível revisão futura.
- [x] 11.5 Spec (`escapada-ia-corrida/spec.md`) e design (`design.md`, D11) atualizados.
- [x] 11.6 `mvn test` completo rodado novamente — 0 falhas.

## 12. Corrige piloto travado em LENTO/giro mínimo para sempre + redução só no traçado de fuga em si (0.4)

**(adicionado após feedback do usuário — bug real: piloto ficava preso em modo lento mesmo depois de voltar pro traçado 1/2)**

- [x] 12.1 Causa raiz confirmada: `processaEscapadaDaPista()` usava `verificaForaPista()` (inclui a janela de retorno) pra setar `LENTO`/`GIRO_MIN_VAL`, mas nada restaurava esses valores explicitamente — dependia de `processaIAnovoIndex()` resetar pra `NORMAL`/`GIRO_NOR_VAL`, e esse método retorna cedo sem fazer nada quando `colisao != null` (comum em corrida), deixando o piloto travado permanentemente.
- [x] 12.2 Condição de redução trocada de `verificaForaPista(this)` pra `getTracado() == 4 || getTracado() == 5` (literal, sem incluir a janela de retorno) — multiplicador de ganho trocado de 0.5 pra 0.4.
- [x] 12.3 Novos campos `modoPilotagemAntesDaFuga`, `giroAntesDaFuga`, `estavaNoTracadoDeFuga`: capturam o modo/giro no ciclo exato em que o traçado passa a ser 4/5, e restauram explicitamente esses valores no ciclo exato em que deixa de ser — sem depender de nenhum outro método externo.
- [x] 12.4 Teste antigo `velocidadeEModoReduzidosDuranteORetornoDaTracado5` removido (contradizia o novo comportamento); testes atualizados/adicionados em `PilotoEscapadaAncoradaTracadoTest`: `velocidadeEModoReduzidosDuranteATracado4` (0.4 em vez de 0.5), `velocidadeEModoNaoReduzidosDuranteAAnimacaoDeRetorno_soNoTracado4ou5Mesmo` (nada reduzido durante a janela de retorno), `aoVoltarDoTracadoDeFugaParaOTracadoDeOrigem_modoEGiroSaoRestauradosAoNormal` (regressão do bug de trava).
- [x] 12.5 Spec (`escapada-ia-corrida/spec.md`) e design (`design.md`, D12, supersede D4) atualizados.
- [x] 12.6 `mvn test` completo rodado novamente — 0 falhas.

## 7. Validação manual

- [ ] 7.1 Desenhar pelo menos uma `ObjetoEscapada` num circuito de teste (editor), rodar uma simulação headless (`MainFrameSimulacao`) com `Global.FORCAR_ESCAPADA_TESTE = true` e confirmar nos logs/telemetria que pilotos realmente passam pelo traçado 4/5 na zona desenhada, sem exceção.
- [ ] 7.2 Rodar a mesma simulação com a flag desativada e confirmar que só pilotos agressivos e muito estressados (ou que falharem em desviar) escapam, e que o restante circula normalmente.
- [ ] 7.3 Confirmar visualmente (ou por log) que a velocidade cai perceptivelmente durante a janela de escapada e volta ao normal depois do retorno.
