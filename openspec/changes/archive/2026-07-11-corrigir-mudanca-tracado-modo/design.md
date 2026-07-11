## Context

`Piloto.java` roda um pipeline de métodos por ciclo (`processaNovoIndex()`, linha ~1092) que inclui, entre outros, `processaIAnovoIndex()`, `processaEscapadaDaPista()`, `processaEvitaBaterCarroFrente()` e `processaMudarTracado()` — todos chamados incondicionalmente para todo piloto, humano ou bot, a cada ciclo. Cada método decide internamente o que fazer.

`processaIAnovoIndex()` já resolve corretamente a pergunta "a IA deve pilotar giro/ERS/DRS/ataque-defesa no lugar do jogador humano agora?" com 3 estados:
1. Online (`controleJogo instanceof JogoServidor`) → nunca.
2. Solo local, `automaticoManual == MANUAL` → nunca.
3. Solo local, `automaticoManual == AUTOMATICO` → sim, exceto durante a janela `isManualTemporario()` (contador de 150 ciclos, renovado a cada input real do jogador via `setManualTemporario()`, já disparado em `GerenciadorVisual`/`ControleJogoLocal`).

`processaMudarTracado()` tenta responder uma pergunta parecida, mas com um único `if` no topo que usa só o item 2 acima (`isJogadorHumano() && MANUAL.equals(getAutomaticoManual())`), sem os itens 1 e 3. E esse `if` cerca TODOS os branches do método, incluindo dois que não são "autopilot" — são física de pista que deveria valer sempre (snap de traçado no box; desvio de carro batido sob safety car) — e um que é exclusivo de bots (`tentarEscaparFilaIndiana()`, sem guard de `isJogadorHumano()` hoje).

`JogoServidor.getAutomaticoManual()` e `JogoCliente.getAutomaticoManual()` retornam sempre `Global.CONTROLE_AUTOMATICO` (hardcoded) independente da escolha feita pelo jogador na criação do jogo — esse valor é usado apenas como rótulo de exibição em telas (`PaddockWindow`, `FormClassificacao`, `PainelCampeonato`) e não deve ser alterado por esta mudança; o item 1 (bypass online) precisa continuar sendo feito via `instanceof JogoServidor`, não via esse getter.

## Goals / Non-Goals

**Goals:**
- Separar, dentro de `processaMudarTracado()`, os branches que são "autopilot" (devem seguir a mesma regra de 3 estados de `processaIAnovoIndex()`) dos que são física sempre-ativa (box, desvio de safety car) e do que é exclusivo de bots (fila indiana).
- Fazer o comportamento do jogador humano online ser consistentemente "sempre manual" em toda a troca de traçado por autopilot, do mesmo jeito que já é para giro/ERS/DRS.
- Fazer o snap de box e o desvio de safety car funcionarem também no solo em modo manual (hoje bloqueados por engano).
- Restringir `tentarEscaparFilaIndiana()` a bots (`!isJogadorHumano()`), nunca ao jogador humano, em nenhum modo.

**Non-Goals:**
- Não mexer em `processaIAnovoIndex()` (já correto).
- Não mexer em `processaEscapadaDaPista()`/`escapaTracado()` (escapada por stress e derrapagem por pneu gasto em curva baixa) — já rodam incondicionalmente e continuam assim.
- Não wireup do `circuito.getEscapeMap()` real (fora de escopo, já documentado como pendente em `objeto-escapada-tracado`).
- Não alterar `JogoServidor.getAutomaticoManual()`/`JogoCliente.getAutomaticoManual()` nem o fluxo de `DadosCriarJogo.automaticoManual` — o valor escolhido pelo jogador ao criar um jogo online continua sendo apenas exibido, não usado para decidir comportamento de autopilot (que é sempre "manual" online, incondicionalmente).

## Decisions

**Extrair uma checagem central "autopilotAtivo()" reaproveitável, em vez de duplicar a lógica de 3 estados.**
`processaIAnovoIndex()` já contém a lógica de 3 estados inline. Para `processaMudarTracado()` seguir a mesma regra sem copiar/colar a expressão booleana (e arriscar divergência futura), extrair um método privado em `Piloto` — por exemplo `boolean autopilotDesligado()` — que encapsula `isJogadorHumano() && (MANUAL.equals(...) || instanceof JogoServidor)`, usado por ambos os métodos. `isManualTemporario()`/`decrementaManualTemporario()` continuam sendo checados no ponto de uso (cada método pode precisar decidir separadamente se decrementa o contador ali, para não decrementar duas vezes por ciclo se ambos os métodos checassem o mesmo contador no mesmo ciclo).
- Alternativa considerada: deixar a expressão duplicada nos dois métodos. Rejeitada — é exatamente o tipo de duplicação que causou a divergência atual (um método ganhou o bypass `instanceof JogoServidor`, o outro não).

**Reordenar os branches de `processaMudarTracado()` para que box e desvio de safety car fiquem antes/fora de qualquer checagem de autopilot.**
Hoje o `if` de gate vem antes de tudo. A mudança move a checagem de autopilot para dentro do método, aplicada só aos branches que decidem proativamente trocar de faixa (desviar de retardatário). Os branches de box (linhas ~2116-2128) e de desvio de safety car (linhas ~2082-2115) passam a rodar antes dessa checagem, sem condição de modo — só as condições de negócio que já têm (`isSafetyCarNaPista()`, `isBoxSaiuNestaVolta()`, etc.).
- Alternativa considerada: manter um gate único, mas com uma exceção "a menos que seja box ou safety car". Rejeitada — fica mais difícil de ler e mais fácil de esquecer de novo ao adicionar um branch futuro; a ordem física (sempre-ativo primeiro, autopilot depois) é auto-documentada.

**`tentarEscaparFilaIndiana()` ganha `!isJogadorHumano()` no próprio método, não no call site.**
Colocar o guard dentro de `tentarEscaparFilaIndiana()` (em vez de só no `else if` que a chama em `processaMudarTracado()`) garante que qualquer outro call site futuro (hoje só há um) também respeite a regra, e deixa a intenção ("isso é uma mecânica de bot") localizada onde a mecânica é definida.

## Risks / Trade-offs

- [Risco: extrair `autopilotDesligado()` mas esquecer de usá-lo nos dois lugares, perpetuando a divergência] → Mitigação: usar o mesmo método nos dois pontos de chamada (`processaIAnovoIndex()` e `processaMudarTracado()`) na mesma mudança; cobrir com teste que exercite os 3 estados para os dois métodos.
- [Risco: mover box/safety-car para fora do gate muda a ordem de avaliação e afeta efeitos colaterais de outros branches que dependiam implicitamente do early-return] → Mitigação: revisar cada branch remanescente do método após o reposicionamento para confirmar que nenhum depende de "só chega aqui se não for box/safety car"; os `if/else if` já existentes tornam isso improvável, mas exige leitura atenta na implementação.
- [Risco: jogador humano online percebendo diferença de jogabilidade em corridas já em andamento após o deploy] → Mitigação: comportamento antigo já era um bug não-intencional (autopilot de traçado rodando online); comunicar como correção de bug, não como mudança de regra de jogo.

## Migration Plan

Mudança é local a `Piloto.java`, sem alteração de schema/persistência/API. Deploy normal (build + restart do servidor). Sem passo de migração de dados. Rollback = reverter o commit.

## Open Questions

Nenhuma — pontos de ambiguidade foram resolvidos em conversa com o usuário antes desta proposta (ver proposal.md).
