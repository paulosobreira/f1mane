## 1. Levantamento e getters públicos (pré-requisito, sem mudança de comportamento)

- [x] 1.1 Buscar todas as referências a `manualTemporario`/`isManualTemporario()`/`setManualTemporario()` fora de `Piloto.java` (rede, serialização, UI) para confirmar que é seguro mover o campo pra `ControleAutomacao` — achado importante: `PainelCircuito.java:770` lê `isManualTemporario()` (renderização, decide suavização de posição), então `Piloto.isManualTemporario()`/`setManualTemporario()` vão continuar existindo como métodos públicos (fachada estável), só a implementação interna passa a delegar pra `ControleAutomacao` via `InterfaceJogo` — nenhum call site externo precisa mudar. As outras ocorrências (`ControleJogosServer`/`MonitorJogo`/`JogoCliente`) são um método de mesmo nome mas de outro recurso (aplet legado, "auto pos"), não tocam `Piloto.manualTemporario` — confirmado lendo o corpo de cada um
- [x] 1.2 Adicionar em `Piloto.java` os getters públicos que faltam — dois dos sete planejados já existiam sob outro nome (`getDiferencaParaProximo()` = `calculaDiferencaParaProximo`, `getDiferencaParaProximoRetardatario()` = `calculaDiffParaProximoRetardatarioMesmoTracado`); adicionados os que realmente faltavam: `getCarroPilotoDaFrenteRetardatario()`, `getDiferencaParaAnterior()`, `getDiffParaProximoRetardatario()`, `getMaxGanhoBaixa()`, `getMaxGanhoAlta()` — e mais três descobertos ao ler o corpo completo dos métodos de traçado que ainda não estavam no levantamento original: `getCiclosPresoFila()`/`getCiclosPresoFilaProximidade()`/`zerarCiclosPresoFila()` (usados por `tentarEscaparFilaIndiana`) e `getMudouTracadoReta()`/`incrementaMudouTracadoReta()` (usado por `espelhaTracadoCarroAtras`)
- [x] 1.3 Rodar `mvn test` para confirmar que os getters novos não quebram nada — 730 testes, 0 falhas, build limpo

## 2. Esqueleto de `ControleAutomacao` e fiação básica

- [x] 2.1 Criar `src/main/java/br/f1mane/controles/ControleAutomacao.java`, seguindo o padrão de construtor de `ControleBox`/`ControleSafetyCar` (recebe `ControleJogoLocal`/`InterfaceJogo` e `ControleCorrida`)
- [x] 2.2 Instanciar `ControleAutomacao` em `ControleCorrida` (mesmo bloco onde `controleBox`, `controleSafetyCar`, `controleClima`, `controleQualificacao` são criados), com getter `getControleAutomacao()`
- [x] 2.3 Adicionar `processarAutomacao(Piloto)` em `InterfaceJogo`, implementado em `ControleJogoLocal` delegando para `controleCorrida.getControleAutomacao().processarTick(piloto)` — junto com `suspenderAutomacaoTemporariamente`, `isAutomacaoSuspensaTemporariamente` e os 5 `decideXxx(Piloto)` de traçado (ver 4.2); também precisou de stubs no `JogoCliente` (cliente do applet legado, nunca roda a simulação — no-ops)
- [x] 2.4 Em `Piloto.processaNovoIndex()`, trocar as chamadas diretas `processaIAnovoIndex(); processaIaIrBox();` por `controleJogo.processarAutomacao(this);` — feito junto com 3.x/4.x num único passo em vez de com um `processarTick` vazio intermediário, já que o levantamento (grupo 1) já tinha mapeado tudo que precisava mover

## 3. Mover o núcleo de decisão (giro/ERS/DRS/ataque-defesa)

- [x] 3.1 Mover `autopilotDesligado()`/`autopilotAtivo()` de `Piloto` para `ControleAutomacao` (recebendo `Piloto` como parâmetro), preservando exatamente a mesma condição (`isJogadorHumano() && (CONTROLE_MANUAL || instanceof JogoServidor)`)
- [x] 3.2 Mover `processaIAnovoIndex()` (corpo) para `ControleAutomacao.processarTick(Piloto)`, adaptando os 8 campos de trabalho (`temMotor`, `temCombustivel`, `temPneu`, `porcentagemDesgastePneus`, `porcentagemMotor`, `porcentagemCombustivel`, `superAquecido`, `porcentagemCorridaRestante`) para variáveis locais — viraram um record privado `EstadoTecnico` passado entre os métodos de decisão, e os 8 campos foram removidos de `Piloto`
- [x] 3.3 Mover `iaTentaUsarErs()`, `iaTentaUsarDRS()`, `tentarPassaPilotoDaFrente()`, `tentarEscaparPilotoAtras()`, `modoIADefesaAtaque()` para `ControleAutomacao`
- [x] 3.4 Mover `setManualTemporario()`/`decrementaManualTemporario()`/`isManualTemporario()` para `ControleAutomacao` — **desvio do plano original**: em vez de atualizar os ~8 call sites de `ControleJogoLocal` pra chamar `controleCorrida.getControleAutomacao().suspenderTemporariamente(piloto)` diretamente, `Piloto.setManualTemporario()`/`isManualTemporario()` continuam existindo como fachada pública estável (agora delegando via `controleJogo.suspenderAutomacaoTemporariamente(this)`/`isAutomacaoSuspensaTemporariamente(this)`) — motivo: `PainelCircuito.java:770` (renderização) lê `piloto.isManualTemporario()` diretamente, então quebrar essa API pública exigiria tocar um subsistema fora do escopo deste change; manter a fachada zerou esse risco sem custo extra
- [x] 3.5 Rodar `mvn test` — quebrou `PilotoAutopilotModoTest`, `PilotoModoIADefesaAtaqueRecuoTest`, e também (não previsto no levantamento original) `ControleBoxVelocidadeUniformeTest`, `PilotoPenalidadeEscapeFilaTest` e um teste de `PilotoMudarTracadoNaoEntraEmFugaTest` — todos referenciavam `processaIaIrBox`/`tentarEscaparFilaIndiana`/causas de traçado via reflexão ou mock não coberto no catálogo inicial

## 4. Mover decisão de box e traçado proativo

- [x] 4.1 Mover `processaIaIrBox()` para `ControleAutomacao`, chamado a partir de `processarTick(Piloto)`
- [x] 4.2 Mover o corpo de decisão de `evitaColidirComRetardatario()`, `desviaRetardatarioMesmoTracado()`, `espelhaTracadoCarroAtras()`, `recentralizaSemTrafego()`, `tentarEscaparFilaIndiana()` para `ControleAutomacao.decideXxx(Piloto)` (público) — adicionados como métodos de `InterfaceJogo` (não só acessados via `getControleAutomacao()`), pra manter `Piloto` só falando com `controleJogo`, nunca instanciando/castando pra `ControleAutomacao` diretamente
- [x] 4.3 Ordem da lista de `BooleanSupplier` de `processaMudarTracado()` preservada — só os 5 itens só-IA viraram lambda de uma linha pro método correspondente em `controleJogo`
- [x] 4.4 `mvn test` — 33 falhas restantes, todas rastreadas a: reflexão em métodos que saíram de `Piloto` (esperado) + um bug de ambiguidade `obterPista(any())` ao trocar mocks de `InterfaceJogo` pra `ControleJogoLocal` (achado não previsto: `ControleJogoLocal`/`ControleRecursos` tem overload `obterPista(Piloto)` que não existe na interface) — corrigido tipando `any(No.class)` nos 3 arquivos afetados

## 5. Cooldown de meio segundo (partidas online)

- [x] 5.1 Criado `br.f1mane.controles.TipoAcaoAutomacao` (`MODO_PILOTAGEM`, `GIRO_MOTOR`, `ERS`, `DRS`, `BOX`, `TRACADO`)
- [x] 5.2 `ControleAutomacao` tem `Map<Piloto, Map<TipoAcaoAutomacao, Long>>` e dois métodos centrais: `executarSeDentroDoCooldown` (ações de efeito void) e `executarTracadoSeDentroDoCooldown` (retorna boolean, porque `mudarTracado()` tem retorno usado pelas causas de traçado)
- [x] 5.3 Todas as escritas de estado da IA passam pelos helpers `aplicarModoPilotagem`/`aplicarGiro`/`aplicarErs`/`aplicarDrs`/`aplicarBox`/`executarTracadoSeDentroDoCooldown` — nenhuma chamada direta a `piloto.setXxx()` fora desses helpers dentro de `ControleAutomacao`
- [x] 5.4 Testes do cooldown — ver `ControleAutomacaoCooldownOnlineTest` (novo arquivo), cobrindo os 6 cenários da spec

## 6. Testes e specs

- [x] 6.1 Reescrito `PilotoAutopilotModoTest` contra `ControleAutomacao` (mocks trocados de `InterfaceJogo` pra `ControleJogoLocal`, com um helper `ligarControleAutomacao()` que conecta os `decideXxx()`/`processarAutomacao`/`suspender*` do mock a uma instância real)
- [x] 6.2 Reescrito `PilotoModoIADefesaAtaqueRecuoTest` contra `ControleAutomacao.modoIADefesaAtaque(Piloto, EstadoTecnico)` — via reflexão (método e record ambos privados)
- [x] 6.3 `PilotoAgressividadeEfetivaTest` não precisou de mudança — só testa `getModoPilotagemEfetivo()`/`incStress`/`decStress`, que continuam em `Piloto`; já passava sem alteração
- [x] 6.4 `mvn test` completo — 730 testes, 0 falhas
- [x] 6.5 `openspec validate --changes extrair-ia-controle-automacao` — passou (1/1)

## 7. Sanity check funcional

- [x] 7.1 Simulação headless (`MainFrameSimulacao 2024 Catalunya 72`) rodou as 72 voltas completas sem exceptions, com tabelas de tempo até "Volta72", corrida competitiva entre pilotos (posições disputadas, gaps variando) e detecção de colisão ativa logo na largada — evidência de que a automação (giro/modo/DRS/ERS/traçado) está de fato decidindo, não apenas rodando em um estado padrão parado
- [x] 7.2 Não realizado nesta sessão: exigiria subir o servidor online, criar uma partida com bots e observar o comportamento ao vivo por >1s — fora do escopo prático de uma sessão headless. Coberto por proxy pelos 6 testes unitários de `ControleAutomacaoCooldownOnlineTest`, que exercitam exatamente os cenários da spec (segunda ação do mesmo tipo em <1s ignorada, após 1s aplicada, tipos independentes, tentativa descartada não renova o cronômetro, solo não afetado, humano nunca chega a ser afetado) sem precisar de um servidor real

## 8. Bug encontrado em teste ao vivo (pós-implementação)

- [x] 8.1 Usuário reportou `NullPointerException` (HTTP 400) em `GET /letsRace/temporadas/2026`: `Piloto.isManualTemporario()` passou a delegar pra `controleJogo.isAutomacaoSuspensaTemporariamente(this)`, mas pilotos "modelo" (`TemporadasDefault.pilotos`, sem partida associada) nunca têm `controleJogo` setado — o Jackson chama esse getter ao serializar a resposta do endpoint e explode. Antes da extração, `manualTemporario` era um `int` simples e nunca dava NPE nesse cenário
- [x] 8.2 Corrigido: `Piloto.isManualTemporario()`/`setManualTemporario()` agora fazem guard de `controleJogo == null` antes de delegar, restaurando o comportamento seguro que o campo `int` tinha antes
- [x] 8.3 Adicionado `PilotoManualTemporarioSemControleJogoTest` (2 testes) cobrindo esse cenário especificamente, pra não regredir de novo
- [x] 8.4 `mvn test` completo — 738 testes, 0 falhas

## 9. Ajuste de valor: cooldown online de 1s pra 500ms (pós-implementação)

- [x] 9.1 `ControleAutomacao.COOLDOWN_ONLINE_MILIS` alterado de `1000L` pra `500L`
- [x] 9.2 `ControleAutomacaoCooldownOnlineTest` atualizado (nomes de teste e deltas de tempo: 900/1000ms → 450/500ms)
- [x] 9.3 Spec delta (`specs/piloto-controle-automatico-manual/spec.md`), `proposal.md` e `design.md` atualizados de "1 segundo"/"1s" pra "meio segundo"/"500ms" — a citação literal do pedido original do usuário em `design.md` foi mantida como registro histórico, com nota indicando o ajuste posterior
- [x] 9.4 `mvn test` completo
