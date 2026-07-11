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

- [ ] 8a. **Implementado e depois revertido.** Tentativa: ignorar a lógica nova inteira (`processaEscapadaAncoradaAoTracado`/`processaSaidaDaEscapada`) durante a volta 1 (`controleJogo.getNumVoltaAtual() <= 1`), no mesmo espírito de `iaTentaUsarDRS()`. **Decisão final do usuário (revertida): não deve haver diferença nenhuma de regra entre a volta 1 e as demais — a mecânica de escapada usa sempre a mesma regra, em qualquer volta.** Guard removido dos dois métodos; testes trocados por `volta1_mesmaRegraDeQualquerOutraVolta_escapaNormalmente`, `volta1_tentaDesviarNormalmenteComoQualquerOutraVolta`, `volta1_retornoDoTracadoDeFugaFuncionaComoQualquerOutraVolta` em `PilotoEscapadaAncoradaTracadoTest`, confirmando ausência de tratamento especial pra volta 1.
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

## 13. Tolerância análoga no lado da saída (piloto podia ficar preso no traçado de fuga pra sempre)

**(achado ao investigar a causa raiz do bug relatado em Interlagos que motivou o aumento da tolerância de entrada — item 9 — mesmo mecanismo, dessa vez no retorno)**

- [x] 13.1 Causa raiz confirmada: `processaSaidaDaEscapada()` exigia `distanciaSaida >= 0` exato, e `escapadaAtivaNoTracadoDeFuga()` só considerava a zona ativa enquanto `indiceAtual <= indiceSaida`. Um piloto cujo `testeHabilidadePiloto()` falhasse durante toda a janela de retorno podia ter o índice ultrapassar `indiceSaida` num salto só (mesmo mecanismo do item 9, com avanço reduzido pelo multiplicador de ganho 0.4 do traçado de fuga — ~20-30 por ciclo em vez de ~50-55), ficando preso no traçado de fuga pra sempre (nada mais no jogo o traz de volta fora dessa lógica).
- [x] 13.2 Nova constante `Piloto.TOLERANCIA_INDICES_SAIDA_JA_PASSADA = 100`, usada em `escapadaAtivaNoTracadoDeFuga()` (zona ativa até `indiceSaida + 100`) e `processaSaidaDaEscapada()` (`distanciaSaida` aceito até `-100`).
- [x] 13.3 Testes adicionados em `PilotoEscapadaAncoradaTracadoTest`: `saidaDaEscapada_saltoGrandeAlemDoFim_dentroDaTolerancia_aindaTentaVoltar`, `saidaDaEscapada_muitoAlemDoFim_foraDaTolerancia_naoTentaVoltar`.
- [x] 13.4 Spec (`escapada-ia-corrida/spec.md`, novo requisito) e design (`design.md`, D14) atualizados.
- [x] 13.5 `mvn test` completo rodado novamente — 0 falhas.

## 14. Remove a tentativa de desvio (mudarTracado(0)) — lógica de escapada nunca mais dirige o carro

**(bug relatado pelo usuário: "derrapagem" observada do traçado 1/2 pro 0, que na visão dele não deveria acontecer)**

- [x] 14.1 Causa raiz confirmada: o gatilho cego manda o piloto de 0 pra 1/2 (derrapagem) e retorna no mesmo ciclo, sem rodar a lógica de escapada nova. No ciclo seguinte, com o piloto ainda agressivo+estressado mas a escapada a mais de 40 índices (comum, já que a janela de detecção vai até 100), a tentativa de desvio (D3) mandava ele de volta pro traçado 0 — parecendo uma "derrapagem" na direção errada.
- [x] 14.2 Decisão do usuário: a lógica de escapada não deve dirigir o carro pra longe da zona por conta própria — quem decide isso é a condução geral (`processaMudarTracado`/`processaIAnovoIndex` pra IA, ou o jogador em modo manual). Bloco `mudarTracado(0)` (e a variável `comprometido`, que só existia pra essa decisão) removidos inteiramente de `processaEscapadaAncoradaAoTracado()`.
- [x] 14.3 Testes ajustados em `PilotoEscapadaAncoradaTracadoTest` (6 testes que dependiam do desvio reescritos pra confirmar ausência de movimento antes da entrada) e novo teste `aposDerrapagemDoGatilhoCego_naoVoltaAutomaticamenteProTracadoZero` cobrindo o cenário exato do bug relatado.
- [x] 14.4 Spec (`escapada-ia-corrida/spec.md`, Goals e requisitos atualizados) e design (`design.md`, D15, supersede D3) atualizados.
- [x] 14.5 `mvn test` completo rodado novamente — 0 falhas.

## 15. Ganho na escapada força escada de curva baixa (independente do trecho real da pista principal)

**(bug relatado pelo usuário: mesmo com o multiplicador de 0.4, o traçado de fuga ainda parecia rápido demais)**

- [x] 15.1 Hipótese inicial (`no.setTipo(No.RETA)` em `Circuito.preencherTracadoEscapada()`) verificada e descartada antes de implementar — esse campo nunca é lido pra física, já que `Piloto.getNoAtual()` sempre vem da pista principal (mesmo índice), não de `pista4Full`/`pista5Full`.
- [x] 15.2 Causa raiz confirmada empiricamente (circuito real carregado, tipos de nó contados nas 3 zonas de Interlagos): a escada de ganho usada na escapada é a do trecho da pista PRINCIPAL por baixo, que varia por zona — uma das zonas de Interlagos é 72% reta, herdando a escada mais rápida do jogo (30-50) mesmo dentro da escapada.
- [x] 15.3 `Piloto.calculaModificadorPrincipal()` alterado: variáveis locais `reta`/`curvaAlta`/`curvaBaixa` substituem as chamadas diretas a `noAtual.verificaXxx()`, forçando `curvaBaixa=true` (e `reta`/`curvaAlta=false`) sempre que `getTracado() == 4 || 5`, independente do tipo real do nó.
- [x] 15.4 Novo teste `PilotoGanhoTracadoDeFugaTest` (3 testes, usando `GameRandom(1)` pra determinismo): confirma ganho base 10 (curva baixa forçada) no traçado de fuga mesmo com nó reta por baixo, vs. 30 (reta) fora do traçado de fuga com o mesmo nó — regressão confirmada.
- [x] 15.5 Spec (`escapada-ia-corrida/spec.md`, novo requisito) e design (`design.md`, D16) atualizados, incluindo a limitação aceita (modificadores de combustível/pneu em `Carro.calcularModificadorCarro()` ainda leem o tipo real do nó, fora do escopo desta correção).
- [x] 15.6 `mvn test` completo rodado novamente — 0 falhas.

## 16. Só piloto "em risco" pode ser testado/forçado a escapar (corrige efeito colateral do item 14)

**(bug relatado pelo usuário, playtest em Interlagos: todo piloto estava escapando independente das regras, "como se `FORCAR_ESCAPADA_TESTE` estivesse sempre ligada")**

- [x] 16.1 Causa raiz confirmada: o item 14 removeu a tentativa de desvio (`mudarTracado(0)`), que numa corrida real fazia a maioria dos pilotos NÃO comprometidos sair do traçado 1/2 antes de alcançar a entrada. A "última chance clássica" no gatilho, porém, sempre se aplicava a QUALQUER piloto (comprometido ou não) que chegasse lá — um teste de habilidade com no máximo ~10% de chance de sucesso (`habilidade/1000`). Sem a tentativa de desvio, praticamente todo piloto que cruzasse o traçado 1/2 numa zona passou a cair nesse teste de baixíssima chance e a escapar.
- [x] 16.2 Decisão do usuário: só escapa quem está de fato "marcado pra escapar" — `(AGRESSIVO && stress > limite && !testeHabilidadePiloto())` OU `(pneu < 20% && !LENTO && !testeHabilidadePiloto())`. Um piloto normal que nunca ficar em risco não é testado e não escapa.
- [x] 16.3 `processaEscapadaAncoradaAoTracado()` reescrito: teste preventivo (100 índices antes da entrada) e "última chance clássica" no gatilho unificados num único teste por zona por volta, gated por `emRiscoDeEscapada()`. Testa assim que o piloto fica em risco em qualquer ponto da janela de detecção da zona (-150 a 100, a mesma usada pra achar a zona); se falhar, fica marcado no cache e a escapada é forçada assim que `distancia <= 0` (preservando a tolerância de salto de ciclo do item 9).
- [x] 16.4 Testes em `PilotoEscapadaAncoradaTracadoTest` ajustados: 2 testes que assumiam escapada de piloto NORMAL sem risco reescritos pra confirmar ausência de escapada (`pilotoNormal_naoEmRisco_...`), 2 novos testes cobrindo a escapada real de piloto em risco por pneus baixos (`pneusBaixos_...`), e 4 testes de teste-de-habilidade/jogador-humano ajustados pra usar pneus baixos como condição de risco. `mvn test` completo rodado novamente — 545 testes, 0 falhas.
- [x] 16.5 Spec (`escapada-ia-corrida/spec.md`, requisitos unificados) e design (`design.md`, D17) atualizados.

## 17. `mudarTracado()` só permite entrar em 4/5 via mudança forçada

**(investigação pedida pelo usuário: suspeita de que um piloto da frente pudesse ser arrastado pro traçado de fuga por um piloto de trás que estava escapando)**

- [x] 17.1 `desviaPilotoNaFrente()` (lógica de retardatário) confirmada segura — só atribui 0/1/2, nunca copia o traçado de outro carro. Vetor real encontrado em outro lugar: o ramo de defesa de posição `mudouTracadoReta` em `processaMudarTracado()` chama `mudarTracado(carroPilotoAtras.getPiloto().getTracado())` sem validar o valor copiado.
- [x] 17.2 Causa raiz: `mudarTracado()` só bloqueava entrada em 4/5 partindo da origem 0; nada impedia origem 1 ou 2. Segundo vetor do mesmo problema encontrado na API do jogador (`ControleJogosServer.mudarTracado()`), que repassa o traçado vindo do cliente sem validação.
- [x] 17.3 Guard geral adicionado em `mudarTracado(int, boolean, boolean)`: `!forcaMudar && (mudarTracado == 4 || 5)` bloqueia. Toda mudança legítima pra 4/5 já usa `forcaMudar=true`, então nenhum chamador legítimo foi afetado.
- [x] 17.4 Novo `PilotoMudarTracadoNaoEntraEmFugaTest` (5 testes): guard direto em `mudarTracado()`, mudança forçada continua funcionando, e o cenário de integração exato relatado (piloto da frente não é arrastado pro traçado de fuga por um piloto de trás escapando; controle confirmando que o espelhamento de traçado normal 0/1/2 continua funcionando). `mvn test` completo — 550 testes, 0 falhas.
- [x] 17.5 Design (`design.md`, D18) atualizado.

## 18. Multiplicador de ganho no traçado de fuga sobe de 0.4 para 0.8

**(pedido do usuário: já que o D16 força a escada de ganho mais lenta do jogo (curva baixa) no traçado de fuga, a redução extra de 0.4x ficava excessiva — dupla penalização)**

- [x] 18.1 `Piloto.processaEscapadaDaPista()`: `ganho *= 0.40` trocado por `ganho *= 0.80`.
- [x] 18.2 Comentários que citavam o valor antigo (javadoc de `TOLERANCIA_INDICES_SAIDA_JA_PASSADA`, comentário do teste de tolerância de salto no retorno) atualizados para 0.8/~40-60 (o comentário histórico do D16, que descreve o bug encontrado quando o multiplicador AINDA era 0.4, foi mantido como está — é uma nota histórica, não uma descrição do comportamento atual).
- [x] 18.3 Teste `velocidadeEModoReduzidosDuranteATracado4` atualizado (espera 80 em vez de 40, ganho inicial 100).
- [x] 18.4 Spec (`escapada-ia-corrida/spec.md`) e design (`design.md`, D19) atualizados.
- [x] 18.5 `mvn test` completo rodado novamente — sem falhas.

## 7. Validação manual

- [ ] 7.1 Desenhar pelo menos uma `ObjetoEscapada` num circuito de teste (editor), rodar uma simulação headless (`MainFrameSimulacao`) com `Global.FORCAR_ESCAPADA_TESTE = true` e confirmar nos logs/telemetria que pilotos realmente passam pelo traçado 4/5 na zona desenhada, sem exceção.
- [ ] 7.2 Rodar a mesma simulação com a flag desativada e confirmar que só pilotos "em risco" (agressivos+estressados, ou pneus baixos) que falharem no teste de habilidade escapam, e que o restante circula normalmente sem ser desviado pela lógica de escapada.
- [ ] 7.3 Confirmar visualmente (ou por log) que a velocidade cai perceptivelmente durante a janela de escapada e volta ao normal depois do retorno.
