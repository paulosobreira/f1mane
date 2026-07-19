## Why

Nesta sessão, o mecanismo de avaliação de mudança de clima foi temporariamente alterado para: sortear um intervalo de voltas só para a **primeira** mudança da corrida (perto do meio), e a partir daí reavaliar **a cada volta**, permitindo repetir a categoria de clima atual (ex.: NUBLADO pode continuar NUBLADO). Essa alteração foi um experimento a pedido do usuário, mas o usuário decidiu reverter esse comportamento e voltar ao mecanismo original: **toda** transição de clima (não só a primeira) protegida por um intervalo de voltas sorteado, sempre trocando de categoria (nunca repetindo). As únicas partes do experimento de hoje que devem permanecer são dois valores fixos em minutos (atraso real de disparo e duração da rampa de "molhado%"), que resolveram um bug real de tempo de volta médio calculado incorretamente.

## What Changes

- Restaurar `ControleClima.processaPossivelMudancaClima()` para o gate de intervalo de voltas único, aplicado a **toda** avaliação (não só a primeira) — remove o campo `primeiraMudancaAplicada` e o bypass desse gate introduzido nesta sessão.
- Restaurar os métodos `intervaloNublado()`, `intervaloSol()`, `intervaloChuva()` (cada um define o novo clima e recalcula `intervaloMudancaClima` com sua própria fórmula), removendo `aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()`.
- Restaurar o campo `numVoltas` em `ControleClima` (usado pela fórmula de `intervaloSol()`).
- `ThreadMudancaClima` volta a chamar `intervaloNublado()`/`intervaloSol()`/`intervaloChuva()` em vez de `aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()`. A transição continua sempre saindo da categoria atual (nunca repete), como era antes do experimento de hoje.
- **Mantido sem alteração**: o atraso real que `ThreadMudancaClima` dorme antes de efetivar uma transição já disparada continua sorteado entre 0 e 1 minuto fixo (`Global.ATRASO_MAX_MUDANCA_CLIMA_MS`), em vez do tempo médio de volta calculado (que tinha bug de unidade).
- **Mantido sem alteração**: a duração da rampa de "molhado%" continua fixa em 1 minuto e meio (`Global.DURACAO_RAMPA_MOLHADO_MS`).
- **Mantido sem alteração**: a taxa de aquecimento do motor variando por clima (SOL mais rápido, CHUVA mais devagar, NUBLADO padrão), em `Carro.java` — ortogonal a esta change.
- Atualizar a spec `clima-transicao-gradual` para refletir o mecanismo de intervalo de voltas restaurado e os dois valores fixos em minutos mantidos.

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `clima-transicao-gradual`: os requisitos de timing de mudança de clima mudam de "tempo médio de volta calculado a partir das voltas do líder" para "atraso fixo de até 1 minuto" e "rampa fixa de 1min30"; e o requisito de gate por intervalo de voltas volta a se aplicar a toda transição (não só a primeira), com a primeira mudança deixando de ter um Requirement separado.

## Impact

- Código: `ControleClima.java`, `ThreadMudancaClima.java` (revertidos ao mecanismo original de intervalo de voltas, mantendo os dois valores fixos em minutos já em `Global.java`).
- Testes: `ControleClimaMudancaTest.java` (criado nesta sessão para o mecanismo "primeira troca única + repetição") precisa ser removido ou reescrito para o mecanismo restaurado; `ThreadMudancaClimaDisparoTest.java` deve continuar válido (não testa o gate de voltas, só o atraso fixo).
- Spec: `openspec/specs/clima-transicao-gradual/spec.md` precisa refletir o mecanismo restaurado.
- Sem mudança em `Carro.java` (aquecimento de motor por clima) nem em `Global.java` (os dois valores fixos em minutos já existem e continuam).
