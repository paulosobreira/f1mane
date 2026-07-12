## 1. Fixtures de teste de Piloto (base pros próximos itens)

- [x] 1.1 Em `PilotoEscapadaAncoradaTracadoTest.criarPiloto()`, adicionar `carro.setPotencia(1000)` e `carro.setFreios(1000)` por padrão, ajustando o comentário existente sobre pneus "novos" pra cobrir também esses dois campos.
- [x] 1.2 Revisar cada teste do arquivo que dependia do default zerado — na prática, o efeito foi um segundo `nextDouble()` (testePotencia() agora sucede e cai pra testeHabilidadePiloto()) em 4 testes com verificação de contagem de RNG (`marcado_naoETestadoDeNovo_aoAlcancarAEntrada`, `teste2_naoAvaliado_quandoTeste1JaMarcou`, `testePreventivo_resetaAoMudarDeVolta_permitindoNovoTeste`, `janela_exatosCentoECinquentaIndices_jaRecebeOTeste`), corrigidos de `times(1)`/`times(3)` pra `times(2)`/`times(4)`.
- [x] 1.3 Repetido em `PilotoDerrapagemTest.criarPiloto()` (campo `freios`) — nenhum teste desse arquivo verifica contagem de RNG, todos os 14 continuaram verdes.
- [x] 1.4 `mvn test`: suíte completa (593 testes) verde.

## 2. Entrada no box em três camadas (suave → forçada → garantida)

- [x] 2.1 Verificado nos dados reais (37 circuitos `*_meta.xml` com o campo preenchido): `ladoBox` e `ladoBoxSaidaBox` divergem em ~73% deles (ex.: Interlagos 1/2) — geometria intencional (editor expõe os dois como combos independentes), não inconsistência. Mantidos os dois campos separados: camadas 1/2 continuam usando `getLadoBoxSaidaBox()`, camada 3 continua usando `getLadoBox()`, ambos como já eram usados antes desta mudança.
- [x] 2.2 `Piloto.efetivaMudancaTracado(int alvo, boolean forcaMudar)` extraído do bloco de bookkeeping de `mudarTracado`, reaproveitado pelo fluxo normal.
- [x] 2.3 `Piloto.posicionarNoBox(int alvo)` adicionado: no-op se já no traçado alvo, retorna `false` se `impedidoDeMudarTracadoPorEscapada`, senão chama `efetivaMudancaTracado(alvo, true)` — camada 3.
- [x] 2.4 `ControleBox.processarPilotoPararBox()` trocado pra `piloto.posicionarNoBox(...)`.
- [x] 2.5 Janela `JANELA_ENTRADA_BOX_FORCADA = 100` adicionada; branch de entrada no box em `processaMudarTracado()` calcula `forcarEntradaBox` e passa pra `mudarTracado(alvo/0, forcarEntradaBox)` — camada 2.
- [x] 2.6 Confirmado por leitura: branch sem guarda de `autopilotAtivo()`, camada 2 já age pro humano manual.
- [x] 2.7 Teste `camada3_posicionaNoLadoDoBoxQuandoAindaNaoEstaLa`/`camada3_ladoOpostoTambemFunciona` (`PilotoEntradaBoxCamadasTest`).
- [x] 2.8 Teste `camada3_eNoOpQuandoJaEstaNoTracadoCerto`.
- [x] 2.9 Teste `camada3_naoReposicionaPilotoMarcadoPelaEscapada`.
- [x] 2.10 Testes `camada2_dentroDaJanelaForcada_forcaAMudancaIgnorandoAnimacaoEmAndamento`, `camada2_ageTambemProJogadorHumanoEmModoManual`, e `camada1_forA_daJanelaForcada_naoForcaAMudanca` como controle.

## 2b. Escapada exclui piloto indo pro box desde a decisão (não só fisicamente na pit lane)

- [x] 2b.1 Guarda de `processaEscapadaDaPista()` trocada pra `if (isBox() || getPtosBox() != 0) { impedidoDeMudarTracadoPorEscapada = false; return; }`.
- [x] 2b.2 Teste `pilotoDecidiuIrProBox_naoETestadoNemMarcadoPelaEscapada` (`PilotoEscapadaAncoradaTracadoTest`).
- [x] 2b.3 Teste `pilotoJaMarcadoPelaEscapada_decideIrProBox_travaELiberada`.
- [x] 2b.4 Delta spec `escapada-ia-corrida` desta mudança já escrita com o conteúdo corrigido (fase de proposta) — confere com o código atual de `Piloto.java` (sem AGRESSIVO/LENTO/recompensa/exceção humano manual); mescla definitiva acontece no `/opsx:archive`.

## 2c. Bug encontrado testando esta mudança: DRS não desligava a caminho do box (sem relação com o escopo original)

- [x] 2c.1 `Piloto.verificaPodeUsarDRS()`: guarda trocada de `if (getPtosBox() != 0)` pra `if (isBox() || getPtosBox() != 0)`.
- [x] 2c.2 `Piloto.processaUsoDRS()`: novo guard no topo — `if (isBox() || getPtosBox() != 0) { asa=MAIS_ASA; ativarDRS=false; podeUsarDRS=false; return; }`, antes da lógica normal de DRS.
- [x] 2c.3 Visibilidade de `processaUsoDRS()` alterada de `private` pra package-private, pro teste chamar direto (mesmo padrão já usado em `processaMudarTracado()`/`modoIADefesaAtaque()`/`tentarEscaparFilaIndiana()`).
- [x] 2c.4 3 testes novos em `PilotoDrsDesligadoNoBoxTest`: desliga desde a decisão (`isBox()`, ainda fora da pit lane), continua desligado fisicamente na pit lane, e controle (fora do box não desliga à toa).

## 3. Refatora `processaMudarTracado()` em causas nomeadas com prioridade explícita

- [x] 3.1 Extraídos `desviaCarroBatidoSobSafetyCar()`, `processaEntradaSaidaBox()`, `evitaColidirComRetardatario()`, `desviaRetardatarioMesmoTracado()`, `espelhaTracadoCarroAtras()`, `recentralizaSemTrafego()` (`tentarEscaparFilaIndiana()` já existia). Preservada a nuance de que `desviaCarroBatidoSobSafetyCar()` NÃO é mutuamente exclusivo com o resto (só o sub-caso "desvio forçado com animação concluída" retorna `true`, sinalizando parar; os sub-casos de tracado 4/5 agem mas retornam `false`, deixando a lista de causas rodar no mesmo ciclo — igual ao `if` original, que não tinha `return` nesses sub-casos).
- [x] 3.2 Cadeia `else if` substituída por `List<BooleanSupplier>` (`Arrays.asList(this::processaEntradaSaidaBox, this::tentarEscaparFilaIndiana, this::evitaColidirComRetardatario, this::desviaRetardatarioMesmoTracado, this::espelhaTracadoCarroAtras, this::recentralizaSemTrafego)`) com loop parando no primeiro `true`.
- [x] 3.3 `mvn test`: 602 testes, 0 falhas — sem mudança de comportamento observável.
- [x] 3.4 Teste novo `duasCausasSatisfeitas_soAPrioridadeMaisAltaAge` (`PilotoEntradaBoxCamadasTest`): entrada no box (prioridade 1) e recentralização sem tráfego (prioridade 6) satisfeitas juntas, alvos diferentes (2 vs 0) confirmam que só a de maior prioridade age.

## 4. Fila indiana: detecção sem colisão física literal (valores iniciais agressivos)

- [x] 4.1 Constantes adicionadas em `Global`.
- [x] 4.2 `Piloto.atualizaCiclosPresoFilaProximidade()` adicionado (campo `ciclosPresoFilaProximidade`), chamado no topo de `processaPenalidadeColisao()`, independente de `getColisao()`.
- [x] 4.3 `tentarEscaparFilaIndiana()` dispara com `ciclosPresoFila >= 8 OU ciclosPresoFilaProximidade >= LIMIAR_CICLOS_FILA_SEM_COLISAO`; ambos zerados ao escapar com sucesso.
- [x] 4.4 Teste `tentarEscaparFilaIndiana_semColisaoFisica_masPertoNaMesmaLinha_escapaComLimiarMenor` (`PilotoPenalidadeEscapeFilaTest`).
- [x] 4.5 Testes `tentarEscaparFilaIndiana_foraDaJanelaDeProximidade_naoAcionaOCaminhoNovo` e `..._dentroDaJanelaMasGanhoAcimaDoLimite_naoAcionaOCaminhoNovo`. Ajuste de isolamento necessário: 2 testes pré-existentes (`poucosCiclosPreso_naoEscapa`, `semColisaoContadorZera`) tinham "Frente" a 40 índices — dentro da nova janela de 50 — e passaram a disparar via o caminho novo; movidos pra 400 índices pra isolar só o contador original.

## 5. Agressividade efetiva sob stress alto (stress > 95)

- [x] 5.1 `Piloto.getModoPilotagemEfetivo()` adicionado.
- [x] 5.2 Call sites migrados: `Piloto.java` (`retardaFreiandoReta` x2, `processaStressDesgastePneus` x4, `processaMudancaRegime`→`mensangesModoAgressivo` x2, `calculaModificadorPrincipal` x3, `incStress`/`decStress`), `Carro.java` (desgaste de pneu por modo), `ControleCorrida.java` (chance de acidente, `verificaAcidenteJogadorHumano` x2). `PainelCircuito.java` intocado.
- [x] 5.3 Auto-downgrade de `incStress()` removido.
- [x] 5.4 Testes em `PilotoAgressividadeEfetivaTest` (10 testes): leitura pura de `getModoPilotagemEfetivo()`, `decStress`/`incStress` escalando pelo modo efetivo, stress≥99 não muta mais o campo armazenado.
- [x] 5.5 Recuo adicionado em `modoIADefesaAtaque()`.
- [x] 5.6 Teste `stressAcimaDe95_testeDeHabilidadeBemSucedido_recuaParaNormal` (`PilotoModoIADefesaAtaqueRecuoTest`).
- [x] 5.7 Teste `stressAcimaDe95_testeDeHabilidadeMalsucedido_aindaEscolheAgressivo`.
- [x] 5.8 Teste `stressDentroDoLimite_naoConsomeRNGExtraDoRecuo` (verifica `times(1)` de `nextDouble()`).
- [x] 5.9 Teste `jogadorHumanoSelecionaAgressivoManualmente_comStressAlto_funcionaImediatamenteSemTeste` (`PilotoAgressividadeEfetivaTest`).

**Achado durante a implementação, corrigido na spec**: `ControleCorrida.verificaAcidenteJogadorHumano` (a segunda exceção de proteção do modo manual) também foi migrado pra `getModoPilotagemEfetivo()`, seguindo a mesma regra geral do item 5.2 — o que significa que essa exceção deixa de disparar quando o stress já neutralizou o efeito de AGRESSIVO (não por causa da chave automático/manual, mas porque a pré-condição de risco em si não se aplica mais). Spec `piloto-controle-automatico-manual` corrigida pra refletir isso com precisão (cenário existente ajustado + cenário novo).

## 6. Confirmação do modo de pilotagem do jogador humano manual (documentação + regressão, sem mudança de código)

Decidido com o usuário: bandeirada e desvio de retardatário (`desviaPilotoNaFrente`) continuam forçando `modoPilotagem`/`giro` do jogador humano manual, junto de escapada e colisão contra o carro da frente — ou seja, **não há gap a fechar** além da remoção do item 5.3. Este grupo é só verificação.

- [x] 6.1 Confirmado por leitura: `processaIAnovoIndex()` retorna cedo com `if (autopilotDesligado()) return;`; `modoIADefesaAtaque()` só é alcançado através dele (via `tentarPassaPilotoDaFrente()`).
- [x] 6.2 Teste `humanoManual_eForcadoParaLentoAoSerUltrapassadoComoRetardatario` (`PilotoAutopilotModoTest`).
- [x] 6.3 Teste `humanoManual_eForcadoParaLentoPelaBandeirada`.
- [x] 6.4 Escapada já coberta por `PilotoEscapadaAncoradaTracadoTest` (`jogadorHumanoEmModoManual_semExcecao_*`, sessão anterior). Colisão contra carro da frente (`ControleCorrida.verificaAcidenteJogadorHumano`) confirmada por leitura — mesmo caminho de código de antes, só a condição de entrada migrou pra `getModoPilotagemEfetivo()` (item 5.2); sem infraestrutura de teste prévia pra `ControleCorrida` nesse repo, e construir uma do zero pra confirmar comportamento inalterado não se paga — suíte completa (622 testes) verde é a confirmação prática.
- [x] 6.5 Teste `humanoManual_processaIAnovoIndex_naoAlteraModoPilotagemNemGiro`.

## 7. Calibração e verificação final

- [ ] 7.1 **Pendente do usuário** — precisa de corrida real jogada, não dá pra fazer de forma autônoma: rodar `./simulacao.sh` (headless) e, principalmente, jogar de verdade, observando se o novo caminho de fila indiana (item 4) dispara cedo/agressivo demais (bots saindo de fila que se resolveria sozinha); ajustar `JANELA_FILA_SEM_COLISAO`/`GANHO_LIMITE_FILA_SEM_COLISAO`/`LIMIAR_CICLOS_FILA_SEM_COLISAO` pra baixo (nunca pra cima) conforme observado. Vale a mesma observação pra "quase agressivo acima de 95%" (item 5.5) e pras três camadas de entrada no box (item 2).
- [x] 7.2 `mvn test`: 622 testes, 0 falhas.
- [x] 7.3 `mvn clean package -Ph2 -DskipTests`: build empacota sem erros.
- [ ] 7.4 Revisar `openspec/specs/tracado-safe-lane-change/spec.md`, `openspec/specs/piloto-controle-automatico-manual/spec.md`, `openspec/specs/piloto-modo-pilotagem-agressividade/spec.md` (nova) e `openspec/specs/tracado-decisao-prioridade-ia/spec.md` (nova) após o merge das specs desta mudança, confirmando que refletem o comportamento final implementado (acontece no `/opsx:archive`).
