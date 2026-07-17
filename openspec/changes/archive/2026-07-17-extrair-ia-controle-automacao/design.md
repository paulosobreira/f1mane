## Context

`Piloto.java` (4683 linhas) mistura três coisas hoje: física/estado de corrida (posição, desgaste, colisão), estado de rede/UI, e decisão de piloto automático (IA). A decisão de IA está espalhada em ~10 métodos privados/package-private (`processaIAnovoIndex`, `processaIaIrBox`, `iaTentaUsarErs`, `iaTentaUsarDRS`, `tentarPassaPilotoDaFrente`, `tentarEscaparPilotoAtras`, `modoIADefesaAtaque`, `tentarEscaparFilaIndiana`, `autopilotAtivo`/`autopilotDesligado`, bookkeeping de `manualTemporario`), todos operando por efeito colateral direto em campos privados de `this`.

Não existe hoje um ponto único de entrada de comando: o caminho local (Swing → `GerenciadorVisual` → `ControleJogoLocal`) e o caminho online (REST `LetsRace` → `ControleJogosServer`) implementam os mesmos "verbos" (mudar giro, modo, DRS, ERS, box, traçado) de forma duplicada e ligeiramente divergente, ambos terminando nos mesmos setters públicos de `Piloto` (`setModoPilotagem`, `setAtivarDRS`, `setAtivarErs`, `setBox`, `mudarTracado`). Esses setters são o denominador comum — é para eles que "a mesma interface do jogador humano" aponta.

O tick por ciclo é hoje todo interno a `Piloto.processarCiclo()` → `processaNovoIndex()`, que chama `processaIAnovoIndex()` e `processaIaIrBox()` inline, no meio de uma sequência maior de ~20 chamadas (física, stress, colisão etc.). Não existe hoje nenhuma classe externa (`ControleCiclo`, `ControleJogoLocal`) que orquestre a decisão de IA de fora do objeto `Piloto`.

## Goals / Non-Goals

**Goals:**
- Mover toda decisão de piloto automático para `ControleAutomacao` (`br.f1mane.controles`), deixando `Piloto` só com o estado físico/gameplay e a chamada de acionamento do tick.
- `ControleAutomacao` decide e executa através dos mesmos métodos de comando público que o jogador humano usa — sem criar uma terceira via de comando.
- `Piloto` aciona o tick de automação através de `InterfaceJogo`, não chamando `ControleAutomacao` diretamente.
- Introduzir o cooldown de 500ms por tipo de ação para bots em partidas online, sem afetar jogadores humanos nem partidas solo.
- Aproveitar a extração para eliminar os 8 campos de `Piloto` que só existem como variável de trabalho de um único método que está se mudando de lugar.

**Non-Goals:**
- Unificar os dois caminhos de comando duplicados (Swing local vs REST online). Ambos continuam existindo como estão; `ControleAutomacao` só passa a chamar os mesmos setters públicos que os dois já chamam hoje.
- Mudar qualquer regra de comportamento das mecânicas sempre-ativas listadas em `piloto-controle-automatico-manual` (snap de box, desvio sob safety car, escapada, bandeirada, colisão contra carro da frente, `desviaPilotoNaFrente`) — ficam em `Piloto`, inalteradas.
- Mudar a magnitude/condição de qualquer decisão de IA existente (giro, ERS, DRS, ataque/defesa, box, traçado) — é relocação de código, não rebalanceamento. Único comportamento novo é o cooldown online.
- Aplicar o cooldown de 500ms a jogadores humanos online — eles já são limitados fisicamente à velocidade de clique/toque, e a spec `piloto-controle-automatico-manual` já garante que IA nunca decide por humano online.

## Decisions

### 1. `ControleAutomacao` chama os setters públicos existentes de `Piloto`, não um novo barramento de comando

Alternativa considerada: criar uma interface `ComandoPiloto` nova, implementada tanto pelo caminho humano quanto por `ControleAutomacao`, unificando de vez os dois caminhos duplicados. Descartada por escopo — essa unificação é um refactor maior e ortogonal (toca `GerenciadorVisual`, `ControleJogoLocal`, `LetsRace`, `ControleJogosServer`, a família `ClientPaddockPack` legada), e não foi pedida. `ControleAutomacao` simplesmente chama `piloto.setModoPilotagem(...)`, `piloto.setAtivarDRS(...)`, `piloto.mudarTracado(...)` etc. — os mesmos métodos que os dois caminhos humanos já chamam hoje. Isso já satisfaz "os comandos da IA vão através da mesma interface que o jogador humano": o ponto de convergência é o próprio `Piloto`, não uma interface nova.

### 2. Tick acionado via `InterfaceJogo.processarAutomacao(Piloto)`, chamado uma vez em `processaNovoIndex()`

`Piloto.processaNovoIndex()` troca as duas chamadas diretas (`processaIAnovoIndex(); processaIaIrBox();`) por uma chamada única: `controleJogo.processarAutomacao(this);`. `InterfaceJogo` ganha esse método; `ControleJogoLocal` implementa delegando para `controleCorrida.getControleAutomacao().processarTick(piloto)`. Isso segue exatamente o padrão já usado por `travouRodas`, `info`, `getRandom()` etc. — `Piloto` fala com o resto do jogo através de `InterfaceJogo`, nunca instancia outros `Controle*` diretamente.

`ControleAutomacao.processarTick(Piloto)` internamente decide, na mesma ordem que `processaIAnovoIndex()` faz hoje, se o piloto está sob automação (`autopilotAtivo`), decrementa `manualTemporario` se aplicável, e só então roda a árvore de decisão (ERS, DRS, ataque/defesa, giro/modo) seguida da decisão de box.

### 3. As causas de traçado só-IA continuam expostas em `Piloto` como métodos finos, mas delegam a decisão para `ControleAutomacao`

`processaMudarTracado()` usa uma lista ordenada de `BooleanSupplier` que mistura causas sempre-ativas (snap de box, desvio sob safety car) com causas só-IA (`evitaColidirComRetardatario`, `desviaRetardatarioMesmoTracado`, `espelhaTracadoCarroAtras`, `recentralizaSemTrafego`, `tentarEscaparFilaIndiana`). Mover a lista inteira pra fora de `Piloto` exigiria reconstruir essa cadeia de responsabilidade através da fronteira de pacote, incluindo as causas sempre-ativas que não fazem parte deste refactor.

Decisão: a lista de `BooleanSupplier` continua em `Piloto`, na mesma ordem; cada supplier só-IA vira uma chamada de uma linha pra um método equivalente em `ControleAutomacao` (ex.: `controleJogo.getControleAutomacao().decideEvitaColidirComRetardatario(this)`), que faz a decisão e, se aplicável, chama `mudarTracado(...)` de volta em `this` (mesmo método público que qualquer outra causa, sempre-ativa ou não, já usa). O corpo da decisão sai de `Piloto`; a estrutura da cadeia de causas fica.

**Alternativa descartada**: mover a lista de `BooleanSupplier` inteira para `ControleAutomacao`, com `Piloto` expondo as causas sempre-ativas como métodos públicos pra `ControleAutomacao` chamar de volta. Rejeitada porque inverteria a direção de dependência das causas sempre-ativas (que nada têm a ver com IA) sem necessidade, e tornaria `processaMudarTracado()` mais difícil de auditar contra a spec `piloto-controle-automatico-manual`, que já lista essas causas junto.

### 4. Cooldown de 500ms: um mapa por piloto, dentro de `ControleAutomacao`, chaveado por tipo de ação

`ControleAutomacao` mantém `Map<Piloto, Map<TipoAcaoAutomacao, Long>>` (ou equivalente por-piloto, se `ControleAutomacao` passar a ter um objeto de estado por piloto) registrando o timestamp (`System.currentTimeMillis()`) da última execução bem-sucedida de cada tipo de ação (`MODO_PILOTAGEM`, `GIRO_MOTOR`, `ERS`, `DRS`, `BOX`, `TRACADO`). Antes de executar uma ação decidida, `ControleAutomacao` checa: se `controleJogo instanceof JogoServidor` e o piloto não é humano (`!isJogadorHumano()` — sempre o caso pra quem está sob automação online, já que humano nunca é autopilotado online) e `(agora - ultimaExecucao.get(tipo)) < 500`, a ação é descartada silenciosamente nesse ciclo; senão, executa e grava o novo timestamp.

Segue o mesmo padrão já usado por `Piloto.ultimaMudancaPos` (grava timestamp só em sucesso, nunca em tentativa bloqueada — evita que um piloto preso nunca acumule tempo suficiente pra próxima tentativa válida).

**Granularidade por tipo de ação, não um cooldown único pro piloto inteiro**: o pedido original do usuário ("se tentar mudar pra AGRESSIVO, só vai poder mudar pra NORMAL 1 segundo depois") implica que mudar de modo bloqueia a *próxima mudança de modo*, não bloqueia DRS/ERS/box no mesmo instante. Um cooldown único por piloto teria esse efeito colateral não pedido. (O valor do cooldown em si foi ajustado de 1s pra 500ms num pedido posterior — a granularidade por tipo de ação não mudou.)

### 5. Limpeza de campos: 8 variáveis de trabalho viram estado local do tick

`temMotor`, `temCombustivel`, `temPneu`, `porcentagemDesgastePneus`, `porcentagemMotor`, `porcentagemCombustivel`, `superAquecido`, `porcentagemCorridaRestante` são escritos e lidos exclusivamente dentro de `processaIAnovoIndex()`/`modoIADefesaAtaque()` (confirmado por busca no arquivo inteiro — nenhum outro método os lê). Na extração, viram variáveis locais (ou um pequeno record interno) dentro do tick de `ControleAutomacao`, e são removidos de `Piloto`.

### 6. Novos getters públicos em `Piloto`

Como `ControleAutomacao` vive em `br.f1mane.controles` e os campos abaixo são hoje `private` sem getter, a extração exige adicionar getters públicos (leitura, sem novo setter): `getCalculaDiferencaParaProximo()`, `getCalculaDiferencaParaAnterior()`, `getCalculaDiffParaProximoRetardatario()`, `getCalculaDiffParaProximoRetardatarioMesmoTracado()`, `getCarroPilotoDaFrenteRetardatario()`, `getMaxGanhoBaixa()`, `getMaxGanhoAlta()`. Os métodos de comando (`setModoPilotagem`, `setAtivarDRS`, `setAtivarErs`, `setBox`, `mudarTracado`) e os de leitura já usados hoje (`getStress`, `getPtosBox`, `getColisao`, `getCarroPilotoDaFrente`/`Atras`, `getGanho`, `getTracado`, `getVoltas`, `testeHabilidadePiloto*`, `getModoPilotagemEfetivo`) já são públicos — nenhuma mudança neles.

### 7. `manualTemporario` muda de dono, mas `Piloto` mantém a fachada pública

O contador e o decremento por ciclo migram para `ControleAutomacao` (guardados por piloto, num mapa interno). **Ajuste em relação ao plano original**: em vez de atualizar os ~8 call sites de `ControleJogoLocal` (`mudarGiroMotor`, `mudarModoPilotagem`, `pilotoSelecionadoMinimo/Normal/Maximo`, `mudarModoKers`) para chamarem `controleCorrida.getControleAutomacao().suspenderTemporariamente(piloto)` diretamente, `Piloto.setManualTemporario()`/`isManualTemporario()` continuam existindo como métodos públicos — só a implementação interna passa a delegar via `InterfaceJogo.suspenderAutomacaoTemporariamente(Piloto)`/`isAutomacaoSuspensaTemporariamente(Piloto)`. Motivo descoberto durante a implementação: `PainelCircuito.java:770` (camada de renderização) lê `pilotoSelecionado.isManualTemporario()` diretamente pra decidir suavização de posição — uma dependência externa que o levantamento inicial não tinha capturado. Manter a fachada em `Piloto` evita tocar a renderização (fora de escopo) sem nenhum custo — os call sites de `ControleJogoLocal` continuam chamando `piloto.setManualTemporario()` exatamente como antes, e o comportamento observável não muda.

## Risks / Trade-offs

- **[Risco] A cadeia de `BooleanSupplier` em `processaMudarTracado()` fica com metade dos itens delegando pra outro pacote e metade local — leitura mais indireta pra quem audita a ordem de prioridade das causas de traçado.** → Mitigação: manter a ordem e os comentários explícitos na lista em `Piloto`, e nomear os métodos de `ControleAutomacao` espelhando exatamente o nome que tinham antes (`decideEvitaColidirComRetardatario`, etc.), pra grep continuar funcionando.
- **[Risco] Esquecer de aplicar o cooldown em algum dos seis tipos de ação, deixando um buraco onde a IA online ainda age sem limite.** → Mitigação: centralizar a checagem num único método privado de `ControleAutomacao` (`executarSeDentroDoCooldown(Piloto, TipoAcaoAutomacao, Runnable)`) chamado por todo ponto de execução de ação, em vez de replicar a checagem em cada decisão.
- **[Risco] Mover `manualTemporario` pra fora de `Piloto` quebra serialização/estado esperado por algum outro código que leia esse campo diretamente (ex.: payload de rede).** → Mitigação: buscar todas as referências a `manualTemporario`/`isManualTemporario()` fora de `Piloto` antes de mover (tarefa dedicada em tasks.md), não assumir que é só uso interno.
- **[Risco] `PilotoAutopilotModoTest`/`PilotoModoIADefesaAtaqueRecuoTest` testam hoje métodos privados de `Piloto` via reflexão — vão quebrar de compilar/rodar assim que os métodos saírem de `Piloto`.** → Mitigação: não é regressão de comportamento, é atualização esperada de teste; tasks.md inclui a reescrita desses testes contra `ControleAutomacao`.

## Migration Plan

Não há dado persistido nem API externa envolvida — é refactor interno de um processo Java de vida curta (partida de corrida). Não há plano de rollback além de reverter o commit; não há migração de dados.

## Open Questions

- Nome exato do enum `TipoAcaoAutomacao` (seis valores: `MODO_PILOTAGEM`, `GIRO_MOTOR`, `ERS`, `DRS`, `BOX`, `TRACADO`) — proposto acima, mas pode ser ajustado na implementação sem afetar o resto do design.
- Se `ControleAutomacao` deve guardar o estado por piloto num `Map<Piloto, ...>` interno, ou se faz mais sentido `Piloto` guardar uma referência a um pequeno objeto `EstadoAutomacaoPiloto` (criado por `ControleAutomacao`, mas armazenado em `Piloto` como campo opaco) — ambas resolvem o problema; a escolha fica pra quem implementar, com preferência pelo `Map` interno por manter `Piloto` sem nenhum campo relacionado a automação, seguindo o Goal principal.
