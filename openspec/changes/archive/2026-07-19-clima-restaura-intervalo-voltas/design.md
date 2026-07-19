## Context

Mais cedo nesta mesma sessão, a pedido do usuário, `ControleClima`/`ThreadMudancaClima` foram alterados de um mecanismo "gate por intervalo de voltas sorteado, aplicado a toda transição" (o mecanismo original, também documentado na spec `clima-transicao-gradual`) para um mecanismo "sorteio de intervalo só na primeira mudança da corrida + reavaliação a cada volta a partir daí, com a nova mudança podendo repetir a categoria atual". Na volta seguinte da conversa, o usuário decidiu reverter esse experimento e manter apenas dois valores fixos em minutos que também foram introduzidos hoje (e que resolveram um bug real, não relacionado à decisão de reavaliar a cada volta): o atraso real de disparo de `ThreadMudancaClima` (0 a 1 minuto fixo, `Global.ATRASO_MAX_MUDANCA_CLIMA_MS`) e a duração da rampa de "molhado%" (1min30 fixo, `Global.DURACAO_RAMPA_MOLHADO_MS`).

O código-alvo deste change é bit-a-bit o mesmo mecanismo de intervalo de voltas que existia no repositório antes de qualquer mudança de clima desta sessão — confirmado idêntico ao que existia antes da própria change arquivada `2026-07-17-clima-transicao-gradual` (`git show 6ee89de2^:src/main/java/br/f1mane/controles/ControleClima.java`), já que aquela change nunca alterou esse mecanismo especificamente (só adicionou "molhado%"/rampa/pneu de chuva ao redor dele).

## Goals / Non-Goals

**Goals:**
- Restaurar bit-a-bit a lógica de gate por intervalo de voltas em `processaPossivelMudancaClima()`, `intervaloNublado()`, `intervaloSol()`, `intervaloChuva()`, aplicada a toda transição de clima da corrida (não só a primeira).
- Preservar os dois valores fixos em minutos já em `Global.java` (`ATRASO_MAX_MUDANCA_CLIMA_MS`, `DURACAO_RAMPA_MOLHADO_MS`), sem alterá-los.
- Atualizar a spec `clima-transicao-gradual` pra documentar o mecanismo restaurado e os dois valores fixos, removendo menções ao `tempoMedioVoltaMs` (já removido do código) e ao mecanismo "primeira troca única" (nunca chegou a ser documentado na spec, só existiu nesta sessão).

**Non-Goals:**
- Não mexer na mudança de aquecimento de motor por clima em `Carro.java` (ortogonal, já validada com testes próprios).
- Não mexer nos dois valores fixos em minutos em si (permanecem 60_000L e 90_000L).
- Não reintroduzir `tempoMedioVoltaMs()`/`calculaTempoMedioVoltaMs()` (removidos nesta sessão por terem um bug de unidade e por ficarem completamente sem uso após os dois valores virarem fixos) — o atraso de disparo e a rampa continuam usando os valores fixos, não o tempo médio de volta calculado.

## Decisions

- **Restaurar os métodos originais em vez de generalizar o mecanismo atual**: em vez de tentar fazer `aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()` reproduzirem o comportamento de intervalo de voltas (o que exigiria reintroduzir toda a lógica de `intervaloNublado`/`intervaloSol`/`intervaloChuva` de qualquer forma), o caminho mais simples e menos arriscado é restaurar literalmente o código original (confirmado via git history), só trocando o ponto em que `ThreadMudancaClima` calcula o atraso real de disparo (que já usa o valor fixo de 1 minuto, não o mecanismo antigo de 3-15s nem o `tempoMedioVoltaMs`).
- **Remover `primeiraMudancaAplicada`/`aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()` em vez de deixá-los sem uso**: código morto não deve ficar no repositório; como o gate volta a ser uniforme para toda transição, esse campo e esses métodos perdem todo o propósito.
- **`ControleClimaMudancaTest.java` (criado nesta sessão pro mecanismo revertido) é removido**, não reescrito — ele testava exatamente o comportamento "primeira troca não repete / trocas seguintes podem repetir / avalia toda volta sem gate", que deixa de existir. Não há um teste equivalente pré-existente pro gate de intervalo de voltas (ele nunca teve um teste dedicado, nem antes desta sessão) — este change não adiciona um, por ser puramente uma reversão de código já testado manualmente/historicamente em produção antes de hoje.

## Risks / Trade-offs

- [Risco] Reversão manual de código pode divergir sutilmente do original (ex.: esquecer o `numVoltas` usado só por `intervaloSol()`) → Mitigação: comparação linha a linha com `git show 6ee89de2^:src/main/java/br/f1mane/controles/ControleClima.java` (pacote antigo, mesmo conteúdo lógico) antes de aplicar.
- [Risco] Spec desatualizada após a reversão, já que ela ainda documenta `tempoMedioVoltaMs` (removido) → Mitigação: sync da spec faz parte deste change (spec delta).
