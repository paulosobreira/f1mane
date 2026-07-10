## Context

O editor já produz `ObjetoEscapada` com `pontos` (trajeto), `tracadoOrigem` (1 ou 2) e `indiceEntrada`/`indiceSaida` (índices de nó, na mesma indexação de `pistaFull`/`pista1Full`/`pista2Full`). A corrida (`Piloto.java`) ainda não lê nada disso: `processaEscapadaDaPista()` roda sobre `circuito.getEscapeMap()`, que a mudança anterior deixou sempre vazio de propósito, e sobre `pista4Full`/`pista5Full`, que `Circuito.gerarEscapeMap()` também deixou sempre nulos — mas do tamanho certo, exatamente para permitir a reconexão feita aqui (ver javadoc atual do método).

O pipeline de posição/renderização para traçado 4/5 **já existe e já funciona** (não é código morto): `Piloto.java` (por volta da linha 2440-2490) já lê `pista4Full`/`pista5Full` por índice (`noAtual.getIndex()`) para calcular a posição lateral do carro quando `getTracado()` é 4 ou 5, com fallback para `pista2Full`/`pista1Full` quando o índice não tem nó de escapada. `Piloto.mudarTracado(int, boolean, boolean)` já BLOQUEIA (mesmo com `forcaMudar=true`) as transições 4→{0,1} e 5→{0,2} — ou seja, um piloto em traçado 4 só consegue voltar para o traçado 2, e um piloto em traçado 5 só consegue voltar para o traçado 1. Isso não é uma limitação a contornar: é exatamente a regra que garante que a escapada só retorna ao traçado de origem (nunca ao outro lateral nem direto ao 0) — ver D1 (mapeamento) e D8 (uso no retorno).

Threshold de referência já existente e reaproveitado: `Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA = 90` (já é o "estresse > 90%" da regra de negócio, e já guarda o gatilho cego existente).

## Goals / Non-Goals

**Goals:**
- Pilotos agressivos e muito estressados escaparem de verdade pelas zonas desenhadas no editor (não mais um no-op).
- Pilotos em geral tentarem evitar ficar presos nas mesmas zonas, com um mecanismo de escape "de emergência" para quem não conseguir sair a tempo (evita que todo mundo pareça telepaticamente imune às zonas).
- Uma variável de teste que force o gatilho de posição sem depender de RNG de estresse/agressividade, para eu (usuário) conseguir validar a mecânica em corridas controladas.
- Unificar a velocidade de box, removendo o sorteio rápido/lento por corrida.
- Pilotos no traçado de fuga (4/5) voltarem para o MESMO traçado de origem (1 ou 2) de onde escaparam, nunca para o outro lateral — gatilho de retorno gated por um teste de habilidade do piloto perto do fim da zona.

**Non-Goals:**
- Não alterar `circuito.getEscapeMap()`/`escapaTracado()`/`processaPontoEscape()` (a via antiga baseada em `PontoEscape`) — permanece inerte, sem uso.
- Não alterar o gatilho cego existente (stress+agressivo+curva baixa no traçado 0 → traçado 1/2 aleatório) — decisão explícita de rodar em paralelo, não substituir.
- Não alterar as regras de `mudarTracado` em si (cooldown, bloqueio 1↔2, animação) — só consumir o que já existe.
- Não introduzir um valor de "velocidade alvo" novo e genérico para o modo LENTO fora do contexto de escapada — a redução de velocidade continua sendo um efeito sobre `ganho`/modo/giro, não uma velocidade fixa em km/h.
- Não mudar a UI/formulários do editor (já cobertos pela mudança anterior).

## Decisions

### D1 — `pista4Full`/`pista5Full` passam a ser derivados de `ObjetoEscapada`, recalculados junto com o resto do traçado

`Circuito.gerarEscapeMap()` (chamado por `vetorizarPista()`/`reprocessaEscapadaSeNecessario()`) passa a, para cada `ObjetoEscapada` em `circuito.getObjetos()` com `indiceEntrada >= 0` e `indiceSaida > indiceEntrada`:
- escolher a lista destino: `tracadoOrigem == 1` → `pista5Full`; `tracadoOrigem == 2` → `pista4Full`;
- preencher os índices `[indiceEntrada, indiceSaida]` dessa lista com nós interpolados por comprimento de arco ao longo de `escapada.getPontos()` (mesmo algoritmo de `TestePista.pontoNoTrajeto`/`construirTracadoEscapada`, com `no.setTracado(4 ou 5)` no nó gerado, e `no.setIndex(index)`), deixando todo o resto da lista `null` (como já é hoje).

**Nota sobre a direção do mapeamento 1→5 / 2→4 (CORRIGIDA após bug relatado em produção)**: a primeira versão desta mudança implementou `1→4`/`2→5` — o INVERSO do correto — por má leitura da própria guarda de `Piloto.mudarTracado`: `getTracado()==4 && mudarTracado in {0,1}` **BLOQUEIA** (retorna `false`) a transição de 4 para 0 ou 1, não a permite. Ou seja, um piloto em traçado 4 NÃO PODE voltar para 0 ou 1 — só para 2 (não bloqueado por essa linha). Simetricamente, traçado 5 não pode voltar para 0 ou 2 — só para 1. Isso é confirmado independentemente pelo antigo `Piloto.escapaTracado()`/`ladoEscape`: exigia `getTracado()==1` pra usar o lado 5, e `getTracado()==2` pra usar o lado 4. Logo o mapeamento correto é `tracadoOrigem==1 → traçado 5` (retorna para 1) e `tracadoOrigem==2 → traçado 4` (retorna para 2) — nunca o contrário. O bug em produção (carro saindo pelo traçado 1 e voltando no traçado 2) veio exatamente dessa inversão: com `1→4`, o piloto tentava (via o código de saída, adicionado só depois — ver D8) voltar de 4 para 1, o que `mudarTracado` sempre bloqueava; o retorno observado vinha então de um código legado não relacionado (`processaMudarTracado`, tratamento de safety car) que já usava a convenção CORRETA (4→2, 5→1) — daí a mistura visível de "saiu no 1, voltou no 2". `gerarEscapeMap()` mantém `escapeMap` sempre vazio (compatibilidade com `escapaTracado()`, que continua inerte).

Alternativa considerada: gerar `pista4Full`/`pista5Full` sob demanda (método novo, chamado só quando um piloto realmente for escapar) em vez de durante `vetorizarPista()`. Rejeitada: o pipeline de renderização/colisão já lê essas listas por índice a cada frame para QUALQUER piloto (não só os que escapam, para o fallback `p4==null?p2:p4`), então elas precisam existir e estar corretas o tempo todo, não just-in-time.

### D2 — Estado de comprometimento é recalculado a cada tick, não persistido em campo novo

Em vez de um campo `comprometidoComEscapada` em `Piloto`, cada chamada de `processaEscapadaDaPista()` recalcula do zero, olhando `circuito.getObjetos()`, qual é a próxima `ObjetoEscapada` com `tracadoOrigem == getTracado()` (só relevante quando `getTracado()` é 1 ou 2) à frente do índice atual. Isso evita um campo de estado que precisaria ser resetado em box/safety car/reinício de corrida, e mantém uma única fonte de verdade (a posição atual + os objetos do circuito).

Trade-off aceito: recalcular a cada tick é O(número de `ObjetoEscapada` do circuito) por piloto por tick — aceitável dado que zonas de escapada são poucas por circuito (tipicamente < 10).

Alternativa considerada: cachear a "próxima escapada" e invalidar só quando ela for ultrapassada. Rejeitada por complexidade desproporcional ao volume de dados.

### D3 — Uma única função de decisão unifica os dois gatilhos (comprometido vs. desvio-falhou)

Ambos os caminhos (piloto agressivo+estressado comprometido desde 40 índices, ou piloto comum que falhou em desviar a tempo) convergem exatamente na mesma ação final: ao alcançar `indiceEntrada` ainda no traçado 1/2 correspondente, forçar `mudarTracado(4 ou 5, true)`. A diferença entre os dois é só o que acontece ANTES de chegar lá:
- Comprometido (agressivo+stress>90, ou `Global.FORCAR_ESCAPADA_TESTE`): dentro de 40 índices da entrada, não tenta desviar — só espera chegar.
- Não comprometido: dentro de 100 índices da entrada, tenta `mudarTracado(0)` (sem forçar, respeitando cooldown/colisão normais) a cada tick; se conseguir, para de monitorar essa zona; se chegar em `indiceEntrada` ainda no traçado original, cai no mesmo `mudarTracado(4/5, true)` do caminho comprometido.

### D4 — Velocidade/modo durante a escapada: reforça o hook existente em vez de criar um novo

`verificaForaPista(Piloto)` já identifica exatamente a janela relevante (traçado 4/5, e o retorno via `voltando`), e `processaEscapadaDaPista()` já faz `ganho *= 0.50` nessa janela (linha ~1569-1570). A mudança só adiciona, no mesmo bloco: `carro.setGiro(Carro.GIRO_MIN_VAL)` e `setModoPilotagem(LENTO)`. Isso combina a redução de 50% do ganho com os modificadores negativos que `calculaModificadorPrincipal()` já aplica para `LENTO`+`GIRO_MIN_VAL`, resultando numa velocidade bem abaixo do normal durante toda a janela (ida + retorno), sem introduzir uma "velocidade-alvo" nova e sem duplicar lógica de cálculo de velocidade.

Trade-off aceito (explicitado pelo usuário): isso não é literalmente "metade do valor numérico do modo lento" (que não existe como valor fixo), é uma composição dos dois mecanismos de redução já existentes no motor de simulação — mais consistente com o resto do código do que inventar uma velocidade-alvo isolada.

**SUPERSEDIDA por D12** — usar `verificaForaPista` (incluindo a janela de retorno) causava o piloto ficar travado em `LENTO`/`GIRO_MIN_VAL` para sempre depois de escapar, porque nada restaurava o modo/giro explicitamente. Ver D12.

### D5 — Gatilho cego existente permanece intocado, roda em paralelo

Por decisão explícita (ver proposal), o bloco em `processaEscapadaDaPista()` que hoje empurra pilotos estressados+agressivos em curva baixa para o traçado 1/2 aleatoriamente continua exatamente como está. Ele não sabe nada sobre `ObjetoEscapada` — só troca de traçado lateral (1/2), nunca aciona 4/5 diretamente. A nova lógica desta mudança roda a partir do estado resultante (qualquer que seja o traçado 1/2 atual do piloto), então não há conflito direto — só a possibilidade (aceita) de um piloto ser empurrado para 1/2 pelo gatilho cego e, coincidentemente, já estar perto de uma zona de escapada real, entrando então na lógica nova.

### D6 — Variável global de teste

`Global.FORCAR_ESCAPADA_TESTE` (boolean, default `false`), no mesmo padrão de `Global.DEBUG`/`Global.LOG_COLISAO`. Usada como OU dentro da condição de comprometimento: `(modoPilotagem==AGRESSIVO && stress>limite) || Global.FORCAR_ESCAPADA_TESTE`. Mantém a exigência de posição (traçado 1/2 e distância ≤ 40 índices da entrada) — não força teleporte nem ignora geometria.

### D7 — Box: valor único, sem sorteio

`ControleBox.boxRapido` (campo e sorteio no construtor) é removido. Os 4 pares de incrementos em `processarPilotoBox` (20/15, 25/20, 20/15, 15/10) viram um valor único cada, a média aritmética: 17 (ou 17.5 arredondado — ver tasks para a decisão de arredondamento exata), 22 (22.5), 17 (17.5), 12 (12.5). `limiteUltimasVoltas` em `Piloto.java` (80 ou 85) vira um valor único: 82 ou 83 (média 82.5 — arredondamento a decidir em tasks, consistente com o resto). `isBoxRapido()` e `boxRapido` deixam de existir; chamadores (`InterfaceJogo`, `ControleJogoLocal`, `JogoCliente`, logs de debug em `MainFrameSimulacao`/`ControleJogoLocal`) são atualizados para não referenciar mais o conceito.

Alternativa considerada: manter `isBoxRapido()` sempre retornando `true` (ou sempre `false`) para minimizar o diff. Rejeitada: deixaria os dois conjuntos de constantes desatualizados/mortos no código e o nome do método mentiria sobre o comportamento real (não há mais "rápido" vs "lento" para se referir).

### D8 — Retorno da escapada: teste de habilidade perto do fim, sempre de volta ao traçado de origem

A primeira versão desta mudança só implementava a ENTRADA na escapada — nada fazia o piloto sair do traçado 4/5 de volta pra pista. Isso foi reportado como bug (carros ficavam presos na fuga, ou voltavam pelo traçado errado via um código legado não relacionado — ver D1). A correção adiciona `Piloto.processaSaidaDaEscapada()`, chamado ao final de `processaEscapadaDaPista()`: enquanto `getTracado()` é 4 ou 5, busca a `ObjetoEscapada` ativa nesse traçado de fuga (a que cobre o índice atual) e, a 100 índices de nó ou menos de `indiceSaida`, faz `testeHabilidadePiloto()` a cada ciclo; no sucesso, `mudarTracado(2, true)` (vindo de 4) ou `mudarTracado(1, true)` (vindo de 5) — sempre o traçado de origem, nunca o outro lateral, porque `mudarTracado` já impede qualquer outro destino a partir de 4/5 (ver D1). No fracasso, tenta de novo no próximo ciclo (mesmo padrão de retry usado no desvio de D3). Assim que a transição é efetivada, o desbloqueio de velocidade/modo (D4) acontece sozinho, via `verificaForaPista` deixando de ser verdadeiro ao fim da animação — não precisa de código extra pra "voltar ao modo normal".

Alternativa considerada: usar a mesma condição de `mudarTracado(0)` do desvio (D3) para tentar ir direto ao traçado central. Rejeitada: `mudarTracado` bloqueia 4→0 e 5→0 incondicionalmente (mesmo forçado) — só resta o traçado de origem como destino válido a partir de 4/5, então não há escolha de design aqui, é a única rota que a máquina de estados permite.

### D9 — Tolerância negativa limitada na entrada + janela de comprometimento reduzida (50→40), pra taxa de escapada real ficar mais rara

Bug real relatado: a taxa de escapadas observada em corrida estava muito mais alta que o esperado. Causa raiz: `proximaEscapadaNoTracadoAtual` só excluía zonas já totalmente ultrapassadas (`indiceSaida < índice atual`), sem nenhum limite inferior pra quão longe `indiceEntrada` podia estar atrás do índice atual — e o disparo forçado em `processaEscapadaAncoradaAoTracado` usava `distancia <= 0` sem piso. Resultado: QUALQUER piloto que aparecesse no traçado 1 ou 2 em qualquer ponto dentro de `[indiceEntrada, indiceSaida]` de uma zona — por exemplo após uma troca de traçado lateral no meio dela, sem nenhuma relação com a entrada em si (ultrapasse, defesa, saída de box) — era imediatamente forçado a escapar, mesmo sem ser agressivo/estressado e sem nunca ter se aproximado dessa zona antes.

Correção: nova constante `TOLERANCIA_INDICES_ENTRADA_JA_PASSADA = 20` — se `distancia < -20`, a entrada é considerada perdida/já passada há tempo demais, e a zona deixa de ser elegível (o piloto só poderá escapar pela PRÓXIMA zona à frente). A tolerância pequena (20, bem menor que a janela de desvio de 100) ainda cobre o caso legítimo de um salto de um único ciclo passando exatamente por cima do índice de entrada (`avancoLimitado` pode ser >1 por ciclo), sem abrir a janela inteira da zona pra qualquer chegada tardia.

Ao mesmo tempo, a janela de comprometimento (agressivo+estressado, ou flag de teste) caiu de 50 para 40 índices — reduz ainda mais a frequência total de comprometimentos, por pedido explícito do usuário.

Alternativa considerada: exigir `distancia == 0` exatamente (sem nenhuma tolerância negativa). Rejeitada: reintroduziria o problema original de saltos de ciclo pulando por cima do índice exato, fazendo a escapada nunca disparar em boa parte dos casos — pior que o comportamento atual, não só "mais raro".

### D10 — Threshold de estresse decoupled + LENTO nunca escapa + teste de habilidade como última chance no gatilho

Duas melhorias adicionais pedidas pelo usuário pra reduzir ainda mais a frequência de escapadas:

- `Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA` (novo, default 90 — mesmo valor de hoje) substitui `Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA` só dentro de `processaEscapadaAncoradaAoTracado`; o gatilho cego antigo continua usando o threshold original, intocado. Permite calibrar as duas mecânicas de forma independente no futuro.
- No gatilho da escapada (momento em que `distancia <= 0` e o piloto forçaria a mudança pro traçado de fuga): se `modoPilotagem == LENTO`, o piloto nunca escapa — nem por agressividade+stress, nem pela flag de teste, nem por ter falhado em desviar (isso valia mesmo antes de um piloto LENTO nunca se comprometer pela via agressiva, mas o caminho de "falhou em desviar" não checava o modo, então um piloto LENTO cruzando por acaso uma zona ainda era forçado — corrigido). Para qualquer outro modo, antes de forçar a escapada, `Piloto` faz um `testeHabilidadePiloto()`; se passar, muda para `LENTO` e NÃO escapa por essa zona (fica no traçado 1/2); se falhar, força a escapada como antes.

Efeito colateral desejado: isso dá aos pilotos IA uma segunda forma de evitar a escapada (além do desvio pro traçado 0), reduzindo ainda mais a taxa geral de escapadas — pilotos com `habilidade` alta escapam bem menos que os de habilidade baixa, adicionando variação por piloto que não existia antes.

Alternativa considerada: fazer o teste de habilidade repetir a cada ciclo enquanto `distancia<=0`, em vez de só uma vez no gatilho. Rejeitada: o gatilho já força a mudança de traçado (ou pra LENTO) na primeira vez que dispara, então não há um "próximo ciclo" pra tentar de novo nessa mesma zona — o piloto já saiu do estado "prestes a escapar" de um jeito ou de outro.

### D11 — Jogador humano em modo manual não recebe o teste de habilidade automático no gatilho

Pedido do usuário: o teste de habilidade que salva o piloto da escapada (D10) simula uma decisão de IA — pra um jogador humano em modo de controle manual, essa decisão é do próprio jogador, não deveria ser automática. `processaEscapadaAncoradaAoTracado` passa a checar `isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())` (mesmo padrão já usado em `processaMudarTracado`, linha ~2264) antes de chamar `testeHabilidadePiloto()`; se for jogador humano manual, pula direto pra forçar a escapada, sem o teste. A checagem de `modoPilotagem == LENTO` continua valendo igual pra todo mundo — se o PRÓPRIO jogador já colocou o carro em LENTO manualmente, ele não escapa, exatamente como o usuário descreveu ("a tarefa de deixar no modo lento... é do jogador").

Achado durante a implementação (fora do escopo desta mudança, não alterado): `Piloto.processaSaidaDaEscapada()` (o RETORNO da escapada, ver D8) tem seu próprio `testeHabilidadePiloto()` independente, que NÃO checa jogador humano/manual — então, se um jogador humano manual acabar mesmo assim entrando na escapada (por ter falhado o "teste" que nem chegou a rodar), o retorno automático pro traçado de origem ainda é decidido pela IA. Isso ficou visível ao escrever os testes: uma zona de escapada mais curta que a janela de retorno (100 índices) fazia o retorno disparar no mesmo ciclo da entrada, mascarando o comportamento sendo testado aqui. Como o usuário só pediu pra desligar o teste de ENTRADA pro jogador manual, não mexi no retorno — mas é uma inconsistência real (a IA continua "pilotando" a saída de um carro que devia ser 100% manual) que pode valer a pena revisitar depois.

### D12 — Modo/giro travados só no traçado de fuga em si, com save/restore explícito (corrige bug real: piloto travado em LENTO para sempre)

Bug relatado pelo usuário: depois de voltar do traçado de fuga pro traçado de origem, o piloto ficava permanentemente travado em modo `LENTO` e giro mínimo. Causa raiz: `verificaForaPista(Piloto)` (usada pela D4 original) inclui a janela de retorno inteira (`voltando`), e nada em `processaEscapadaDaPista()` restaurava `modoPilotagem`/giro explicitamente ao sair dessa janela — dependia de outro método (`processaIAnovoIndex`, no bloco de reset "não tentando passar/escapar de ninguém" pra `NORMAL`/`GIRO_NOR_VAL`) rodar em algum ciclo posterior. Só que `processaIAnovoIndex` retorna cedo sem fazer nada quando `colisao != null` (comum em corrida, qualquer carro seguindo outro de perto) — nesse caso o reset nunca acontece, e o piloto fica preso em `LENTO`/`GIRO_MIN_VAL` pro resto da corrida.

Correção (também pedida pelo usuário): a redução deixa de acompanhar `verificaForaPista` (que inclui a janela de retorno) e passa a valer só enquanto `getTracado()` é literalmente 4 ou 5. Três novos campos privados (`modoPilotagemAntesDaFuga`, `giroAntesDaFuga`, `estavaNoTracadoDeFuga`) capturam o estado no exato ciclo em que o traçado passa a ser 4/5, e o restauram explicitamente no exato ciclo em que deixa de ser — sem depender de nenhum outro método externo rodar seu próprio reset. O multiplicador de ganho também mudou de 0.5 para 0.4 (pedido do usuário, "um pouco mais lento").

Efeito colateral: a janela de retorno (animação de troca de traçado de volta) deixa de ter QUALQUER redução de velocidade/modo — assim que `mudarTracado` muda o traçado de volta pro de origem (ainda que a animação lateral continue suavizando visualmente por mais alguns ciclos), tudo volta ao normal imediatamente. Isso é uma mudança de comportamento deliberada pedida pelo usuário ("quando ele voltar, volta tudo ao normal"), não um efeito colateral acidental.

Alternativa considerada: manter a redução também durante a janela de retorno (como na D4 original), só corrigindo o bug de trava com save/restore. Rejeitada: o usuário pediu explicitamente que a redução valesse "somente durante esse traçado" (4/5), não durante o retorno.

## Risks / Trade-offs

- [Risco] Popular `pista4Full`/`pista5Full` a cada `vetorizarPista()`/`reprocessaEscapadaSeNecessario()` para TODOS os circuitos, mesmo sem `ObjetoEscapada`, adiciona um loop sobre `circuito.getObjetos()` a cada revetorização. → Mitigação: curto-circuito imediato quando não há nenhum `ObjetoEscapada` no circuito (a maioria dos XMLs hoje, já que a mudança anterior removeu todas as instâncias do modelo antigo); custo real só aparece em circuitos onde zonas forem redesenhadas.
- [Risco] Dois pilotos escapando na mesma zona ao mesmo tempo compartilham o mesmo trajeto (`pista4Full`/`pista5Full` é uma lista única, sem noção de "pista lateral própria por piloto"), podendo colidir/sobrepor visualmente. → Mitigação: mesmo comportamento que traçado 1/2 normais já têm hoje (múltiplos carros no mesmo traçado lateral); não é uma regressão nova, é o padrão existente do jogo.
- [Risco] `mudarTracado(4/5, true)` na entrada e `mudarTracado(1/2, true)` no retorno forçam a mudança ignorando `verificaColisaoAoMudarDeTracado` (como toda chamada com `forcaMudar=true` já faz hoje, ex.: `escapaTracado()`) — pode "teleportar" o carro para cima de outro momentaneamente. → Mitigação: comportamento idêntico ao já aceito pelo `escapaTracado()` legado; não é uma regressão introduzida por esta mudança.
- [Risco] Reduzir `limiteUltimasVoltas` de 85 para ~82-83 quando o box "seria rápido" hoje muda ligeiramente a estratégia de IA perto do fim da corrida em circuitos que sorteavam `boxRapido=true`. → Mitigação: é exatamente o comportamento pedido (unificar), e o valor médio minimiza a distância em relação a ambos os extremos anteriores.
- [Risco] `ObjetoEscapada` com `pontos` inválido/degenerado (menos de 2 pontos) não deve travar a geração de `pista4Full`/`pista5Full` nem a corrida. → Mitigação: mesma guarda defensiva que `TestePista.construirTracadoEscapada` já usa (`pontos == null || pontos.size() < 2` → não preenche nada para esse objeto, sem exceção).

## Open Questions

- Arredondamento exato das médias de box (17 vs 17.5 truncado/arredondado) — resolvido em tasks.md como `Math.round` simples, sem necessidade de decisão de design separada.
