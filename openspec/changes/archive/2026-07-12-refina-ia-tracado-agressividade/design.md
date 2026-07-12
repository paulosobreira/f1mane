## Context

Levantamento prévio (sessão de exploração) mapeou todos os chamadores de `mudarTracado()`, `setModoPilotagem()` e `setGiro()`. Achados que motivam este design:

- `Piloto.processaMudarTracado()` é uma cadeia de 9 causas em `else if`, sem nomes/documentação de prioridade — a ordem importa (só a primeira condição verdadeira age), mas está implícita na posição do código.
- `ControleBox.processarPilotoPararBox()` chama `piloto.setTracado(...)` diretamente pra posicionar o carro no lado do box, **sem passar por `mudarTracado()`** — nenhuma guarda (cooldown, animação em andamento, trava de escapada, bloqueio 1↔2 direto) se aplica a essa chamada. Já existe, porém, uma tentativa mais cedo e suave em `processaMudarTracado()` (janela de 1000 índices antes da entrada do box, `mudarTracado` não forçado, já funciona pro jogador humano em qualquer modo) — o teleporte no ponto de parada deveria ser só o último recurso, não o único mecanismo.
- `processaEscapadaDaPista()` já exclui o piloto indo pro box da escapada, mas usando `getPtosBox() != 0` (só fisicamente dentro da pit lane) em vez de `isBox()` (desde a decisão) — deixa uma janela na pista principal onde o piloto "decidido a boxar" ainda pode ser marcado. Corrigir essa checagem expõe um bug: se o piloto já estava marcado pela escapada antes de decidir boxar, a trava de mudança de traçado nunca é liberada (só é liberada dentro do código que deixa de rodar), travando-o pro resto da corrida.
- `tentarEscaparFilaIndiana()` só conta ciclos presos (`ciclosPresoFila`) quando há sobreposição física de caixa de colisão (`getColisao() != null`) com `ganho <= 10` — tráfego travado mas sem sobreposição literal nunca aciona o contador.
- `AGRESSIVO.equals(getModoPilotagem())` aparece em ~14 pontos de `Piloto.java`/`Carro.java`/`ControleCorrida.java` (ganho em curva/reta, geração de estresse, chance de acidente, freada mal-sucedida) mais vários em `PainelCircuito.java` (só renderização/HUD).
- `piloto-controle-automatico-manual` já documenta que o modo manual do jogador humano bloqueia decisões de IA em `processaIAnovoIndex()`/`processaMudarTracado()` proativo, mas NÃO menciona explicitamente `processaMudancaRegime()` (LENTO por bandeirada) nem `desviaPilotoNaFrente()` (LENTO por ser ultrapassado como retardatário) na lista de mecânicas sempre-ativas — **decidido com o usuário**: as duas devem continuar mudando `modoPilotagem` de qualquer piloto, humano manual incluso, exatamente como hoje. Não há bug aqui — só falta documentar essas duas junto das demais exceções já listadas (box, safety car, escapada, derrapagem).

## Goals / Non-Goals

**Goals:**
- Entrada no box em três camadas graduais (suave a 1000 índices, forçada a 100 índices, teleporte garantido só se as duas primeiras falharem), inclusive pro jogador humano em modo manual nas camadas 2/3.
- Escapada excluir o piloto indo pro box desde a decisão (`isBox()`), não só fisicamente dentro da pit lane — e corrigir o bug de trava presa que isso expõe.
- Tornar a prioridade de `processaMudarTracado()` explícita e testável por causa, sem mudar o comportamento observável (exceto fila indiana).
- Detectar fila indiana também sem colisão física literal.
- Introduzir "agressividade efetiva" (stress > 95 → efeitos de NORMAL, display continua AGRESSIVO) num único ponto de leitura, sem reescrever os ~14 call sites individualmente.
- Confirmar (e documentar formalmente, sem mudar comportamento) que escapada, colisão contra carro da frente, bandeirada e desvio de retardatário continuam forçando `modoPilotagem`/`giro` do jogador humano manual normalmente — são as quatro exceções sempre-ativas; remover apenas o auto-downgrade de `incStress()` em stress≥99, substituído pela regra de agressividade efetiva.
- Fixtures de teste de `Piloto` com potência/freios realistas por padrão.

**Non-Goals:**
- Não mexe na mecânica de narração/mensagens (fica pra outra melhoria, por pedido explícito).
- Não recalibra os números de `piloto-gestao-estresse` já existentes (freada, colisão, pneu) — só a leitura de "é AGRESSIVO?" que alguns deles fazem.
- Não muda o comportamento de renderização/HUD (`PainelCircuito.java`) — ele continua lendo `modoPilotagem` bruto, porque visualmente o carro deve continuar parecendo AGRESSIVO.

## Decisions

### 1. `getModoPilotagemEfetivo()` como ponto único de leitura pra ganho
Em vez de alterar cada um dos ~14 call sites de `AGRESSIVO.equals(getModoPilotagem())`, adiciona um método `Piloto.getModoPilotagemEfetivo()`: retorna `NORMAL` se `modoPilotagem == AGRESSIVO && stress > 95`, senão retorna `getModoPilotagem()` sem alteração. Os call sites de **gameplay** (`Piloto.java`, `Carro.java`, `ControleCorrida.java`) passam a comparar contra `getModoPilotagemEfetivo()`; os de **renderização** (`PainelCircuito.java`) continuam em `getModoPilotagem()` sem mudança.
- Alternativa descartada: mutar `modoPilotagem` de fato pra NORMAL quando stress>95 (como o `incStress()` já faz hoje em stress≥99) — rejeitada porque o pedido explícito foi manter o piloto **exibido** como AGRESSIVO.
- **Decidido**: o auto-downgrade existente em `incStress()` (`stress >= 99 && AGRESSIVO → setModoPilotagem(NORMAL)`) é **removido** — ficaria redundante/conflitante com a nova regra (95 é atingido antes de 99, e a nova regra não muta o campo; manter os dois faria o piloto passar de "efetivo NORMAL, exibido AGRESSIVO" pra "mutado pra NORMAL de verdade" sem necessidade).

### 1b. IA recua proativamente de AGRESSIVO acima de 95% de stress, sem punir o jogador humano
`Piloto.modoIADefesaAtaque()` (linha 3291) é o único ponto onde a IA escolhe `AGRESSIVO` (`maxPilotagem = temPneu && stress < 100`) — hoje esse limiar é 100, deixando uma faixa (95-99%) em que a IA mecanicamente escolhe AGRESSIVO mesmo sabendo (pela decisão 1) que isso já não traz ganho nenhum. Decidido com o usuário: a IA passa a ter uma chance de reconhecer isso e recuar pra NORMAL por conta própria, via teste de habilidade — não uma garantia (a IA ainda pode ficar AGRESSIVO acima de 95% se o teste falhar), e **exclusivo da decisão automática**, sem afetar o jogador humano de forma alguma (nem punição, nem bloqueio — ele continua escolhendo AGRESSIVO manualmente a qualquer stress, só sem ganho, exatamente como já vale pra todo mundo pela decisão 1).
```java
maxPilotagem = temPneu && stress < valorLimiteStressePararErrarCurva;
if (maxPilotagem && stress > 95 && testeHabilidadePilotoCarro()) {
    maxPilotagem = false; // IA reconhece que empurrar acima de 95% de stress não ajuda mais e recua
}
```
- Por que `testeHabilidadePilotoCarro()` e não um teste dedicado: é o mesmo teste já usado dentro do próprio `modoIADefesaAtaque()` (branch de reta) e em outras decisões de risco do piloto (escapada, freada) — mantém consistência de "julgamento do piloto" sem introduzir um terceiro tipo de teste.
- `modoIADefesaAtaque()` só roda dentro de `processaIAnovoIndex()` (via `tentarPassaPilotoDaFrente()`), que já retorna cedo se `autopilotDesligado()` — herda automaticamente o escopo certo (bot, ou humano em automático sem entrada manual recente) sem precisar de guarda adicional.
- Alternativa descartada: baixar o limiar `valorLimiteStressePararErrarCurva` de 100 pra 95 diretamente (determinístico, sem teste) — rejeitada porque o pedido foi especificamente via teste de habilidade, preservando a chance de a IA "errar" e continuar agressiva, em vez de um corte automático.

### 2. Prioridade de `processaMudarTracado()` como lista ordenada nomeada
Extrai cada uma das 9 causas pra um método privado com nome (`desviaCarroBatidoSobSafetyCar()`, `processaEntradaSaidaBox()`, `tentarEscaparFilaIndiana()` já existe, `evitaColidirComRetardatario()`, `espelhaTracadoCarroAtras()`, `recentralizaSemTrafego()`), cada um retornando `boolean` (agiu ou não), e substitui a cadeia `else if` por um loop sobre uma lista ordenada desses métodos, parando no primeiro que retornar `true`. Mesma ordem observável de hoje.
- Alternativa descartada: strategy pattern com classes separadas — overkill pro tamanho do método e foge do estilo do resto da classe (métodos privados simples).

### 3. Entrada no box em três camadas: suave → forçada → garantida
Decidido com o usuário: entrar no box é primordial (nunca pode simplesmente falhar), mas o caminho até lá deve ser o mais natural possível, escalando de intensidade só conforme o piloto se aproxima do ponto de parada sem ter conseguido mudar de traçado ainda.

- **Camada 1 (já existe, mantida)**: em `processaMudarTracado()`, assim que `isBox()` fica `true` e o piloto entra na janela de `verificaEntradaBox` (1000 índices antes de `entradaBoxIndex`), tenta `mudarTracado` **não forçado** pro traçado do box (via 0 como intermediário se vier do lado oposto). Já funciona pro jogador humano em qualquer modo hoje — esse trecho nunca teve guarda de `autopilotAtivo()`.
- **Camada 2 (nova)**: dentro de uma janela mais estreita — 100 índices antes de `entradaBoxIndex` — a mesma tentativa passa a usar `mudarTracado(alvo, true)` **forçada** (ignora cooldown e animação em andamento). Como a lógica continua passando pelo traçado 0 quando vem do lado oposto (comportamento já existente, preservado), essa camada nunca precisa do bypass do bloqueio 1↔2 — o pior caso é dois ciclos forçados (força pro 0, depois força pro alvo) em vez de um salto direto.
- **Camada 3 (nova, fallback)**: em `ControleBox.processarPilotoPararBox()`, se ao alcançar o ponto de parada o piloto **ainda não estiver** no traçado do box (as camadas 1/2 falharam — trânsito pesado, humano resistindo, etc.), teleporte garantido via `Piloto.posicionarNoBox(int alvo)`. Só nesse caminho o bloqueio 1↔2 é ignorado (é um reposicionamento de emergência, não uma mudança de traçado normal). Se o piloto já estiver no traçado certo, `posicionarNoBox` é um no-op — não reseta `indiceTracado`/`tracadoAntigo` à toa.
- Implementação de `posicionarNoBox`: extrai a parte de bookkeeping de `mudarTracado()` (salvar `tracadoAntigo`, `setTracado`, `calculaIndiceTracado`, log de reset) num método privado `efetivaMudancaTracado(int alvo)`, reaproveitado tanto pelo fluxo normal de `mudarTracado()` quanto por `posicionarNoBox`. Este último só respeita a trava de escapada (`impedidoDeMudarTracadoPorEscapada`) — nenhuma outra guarda.
- Alternativa descartada: adicionar mais um `boolean` de exceção em `mudarTracado(int, boolean, boolean, boolean indoParaBox)` — rejeitada por já ter 3 booleans e ficar difícil de ler nos call sites.
- **Verificado nos dados reais de circuito** (37 circuitos com o campo preenchido, arquivos `*_meta.xml`): `ladoBox` e `ladoBoxSaidaBox` são **diferentes em ~73% deles** (ex.: Interlagos `ladoBox=1`/`ladoBoxSaidaBox=2`) — não é inconsistência de dados, o editor de circuito (`MainPanelEditor`) expõe os dois como combos independentes ("Lado do Box" e "Saída do Lado do Box"). São conceitos distintos: `ladoBoxSaidaBox` é o lado por onde a pit lane se conecta à reta principal (entrada E saída — usado nos três branches de entrada/saída de `processaMudarTracado()`), `ladoBox` é o lado onde a baia do box fica fisicamente dentro da pit lane (usado só na parada). **Decisão revertida**: NÃO unificar — camadas 1/2 continuam usando `getLadoBoxSaidaBox()` (comportamento já existente, correto), camada 3 continua usando `getLadoBox()` (também já existente, correto). O "pulo" da camada 3 corrigindo pro lado da baia, depois de a aproximação levar o carro até a entrada da pit lane, é comportamento físico esperado nesses ~73% dos circuitos — não um bug a eliminar.

### 3b. Escapada exclui piloto indo pro box desde a decisão, não só fisicamente no box
`processaEscapadaDaPista()` já retorna cedo com `if (getPtosBox() != 0) return;`, mas isso só cobre o piloto já fisicamente dentro da pit lane — no trecho da pista principal entre a decisão (`isBox() == true`) e a entrada física, o piloto ainda pode ser marcado pela escapada. Muda a guarda pra `if (isBox() || getPtosBox() != 0)`.
- **Bug encontrado e corrigido junto**: se um piloto já estava marcado pela escapada (`impedidoDeMudarTracadoPorEscapada == true`) antes de decidir ir pro box, `processaEscapadaDaPista()` para de rodar por causa dessa guarda — e como só ela limpa a trava (`processaEscapadaAncoradaAoTracado()`, logo antes de forçar a mudança), a trava nunca mais é liberada, travando o piloto pra qualquer mudança de traçado (inclusive entrar no box) pelo resto da corrida. A guarda de box em `processaEscapadaDaPista()` passa a limpar `impedidoDeMudarTracadoPorEscapada = false` antes de retornar.
- Risco de exploit aceito pelo usuário: ligar/desligar a decisão de ir pro box no instante exato de uma escapada evitaria a marca — considerado de baixo risco prático porque errar esse timing custa muito mais (entrar no box sem querer) do que a própria escapada evitada.

### 4. Fila indiana: detecção sem colisão física literal, com valores iniciais agressivos de propósito
Adiciona uma segunda condição de "preso" em `processaPenalidadeColisao()`: além do caso atual (colisão física + `ganho <= 10`, limiar de 8 ciclos — **inalterado**), soma ciclos num contador **separado** quando há um piloto à frente na mesma linha dentro de uma janela de índices SEM sobreposição física, com `ganho` num nível "lento" mais folgado. Pedido explícito do usuário: começar o mais agressivo (mais fácil de disparar) que for razoável, e ajustar pra baixo depois de observar em corrida real — não o contrário.
- Valores iniciais (constantes nomeadas em `Global`, a recalibrar via `./simulacao.sh` conforme item 7 de tasks.md):
  - `JANELA_FILA_SEM_COLISAO = 50` nós à frente na mesma linha (bem mais generoso que o padrão de detecção física, que é essencialmente 0 — dois carros precisam se tocar).
  - `GANHO_LIMITE_FILA_SEM_COLISAO = 15` (mais permissivo que os `ganho <= 10` da colisão física — pega tráfego "andando bem devagar", não só travado).
  - `LIMIAR_CICLOS_FILA_SEM_COLISAO = 4` ciclos (metade do limiar de 8 usado pra colisão física — sinal mais fraco, mas queremos reagir rápido mesmo assim).
- Ambos os contadores (`ciclosPresoFila` da colisão física, e o novo da proximidade) alimentam a mesma tentativa de escape (`tentarEscaparFilaIndiana`), bastando que QUALQUER um dos dois atinja seu limiar.

### 5. Proteção do modo manual: nenhuma mudança de código necessária além da remoção do item 1
Com as decisões do usuário (bandeirada e desvio de retardatário continuam forçando `modoPilotagem`/`giro` do humano manual, junto de escapada e colisão), **todo** gatilho automático de mudança de `modoPilotagem`/`giro` já está corretamente tratado hoje: `processaIAnovoIndex()`/`modoIADefesaAtaque()` (decisão proativa da IA) já retornam cedo se `autopilotDesligado()`; escapada, colisão-contra-carro-da-frente, bandeirada e desvio de retardatário são exceções sempre-ativas por design, sem bug. O único ponto que efetivamente mudava `modoPilotagem` sem respeitar nada disso — o auto-downgrade de `incStress()` em stress≥99 — já foi removido pela decisão 1. Este item deixa de precisar de um wrapper novo (`aplicaModoPilotagemAutomatico`/`aplicaGiroAutomatico`); o trabalho remanescente é só documentar isso na spec `piloto-controle-automatico-manual` e adicionar testes de regressão que fixem o comportamento atual.
- Alternativa descartada (revisão anterior deste design): guarda central em wrappers novos — descartada porque não havia, de fato, nenhum gatilho restante que precisasse dela depois de contabilizar bandeirada e desvio de retardatário como exceções.

## Risks / Trade-offs

- [Valores iniciais agressivos da fila indiana (item 4) fazem bots escaparem "cedo demais" de situações que hoje se resolvem sozinhas] → aceito como trade-off deliberado a pedido do usuário; calibrar pra baixo rodando corridas headless (`./simulacao.sh`) depois de observar em corrida real, não pra cima.
- [`posicionarNoBox` ignorando cooldown/animação pode causar um "pulo" visual se o carro estava no meio de outra animação de troca ao entrar no box] → `efetivaMudancaTracado` já reaproveita a lógica de espelhar `indiceTracado` restante quando o alvo é o `tracadoAntigo`, então o pior caso é reiniciar a animação (comportamento igual ao de qualquer mudança forçada hoje).
- [`getModoPilotagemEfetivo()` esquecido em algum call site futuro de gameplay] → nome explícito e comentário em `getModoPilotagem()` apontando pra ele quando o motivo da leitura for efeito de jogo, não exibição.
- [`getLadoBox()` vs `getLadoBoxSaidaBox()` divergem em ~73% dos circuitos, fazendo a camada 1/2 mirar um lado e a camada 3 "corrigir" pro outro] → confirmado como geometria intencional do circuito (verificado nos dados reais), não bug — mantidos os dois campos separados, cada um no seu uso já existente.
- [Exploit: jogador liga/desliga a decisão de ir pro box perto do momento exato de uma escapada pra evitar a marca] → aceito conscientemente pelo usuário — o custo de errar esse timing (entrar no box sem querer) supera o benefício de escapar da escapada.
- **BREAKING**: pilotos AGRESSIVO com stress > 95 passam a gerar estresse/ganho como NORMAL — muda resultado de corrida em relação ao comportamento atual, especialmente perto do fim de stints longos sem pit stop.

## Open Questions

Nenhuma pendente — as 4 questões da revisão anterior foram decididas com o usuário e incorporadas nas seções acima:
1. Auto-downgrade de `incStress()` em stress≥99: **removido**.
2. Bandeirada: **continua** forçando LENTO no humano manual.
3. Desvio de retardatário: **continua** forçando LENTO no humano manual.
4. Janela/limiar da fila indiana sem colisão literal: valores iniciais **agressivos** definidos na decisão 4 (janela 50 nós, ganho≤15, limiar 4 ciclos), a recalibrar pra baixo conforme observação em corrida real.
