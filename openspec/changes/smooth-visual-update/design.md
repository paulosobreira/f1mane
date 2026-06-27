## Context

O `atualizacaoSuave()` em `PainelCircuito.java` (linhas 660–751) roda a cada frame do render thread (30–60 Hz via `GerenciadorVisual`). Ele calcula `diferencaSuavelReal` (diferença de índice entre `noAtualSuave` e `noAtual`) e avança `noAtualSuave` em `ganhoSuave` nós por frame. O cálculo atual:

```java
int ganhoSuave = (int) Math.round(modGanho * diferencaSuavelReal / 100.0);
ganhoSuave = Math.min(ganhoSuave, diferencaSuavelReal);
```

Com `modGanho = 4`, para diferença de 10 nós: `ganhoSuave = 0` (arredonda para 0). O carro fica parado. Para diferença de 500: `ganhoSuave = 20`, o carro salta visivelmente. O snap imediato só ocorre acima de 1000 nós — tarde demais dado o requisito de 200.

O `indiceTracado` controla a transição lateral: decrementado por `decIndiceTracado()` a cada frame, vai de ~200–1000 até 0. O rendering interpola a posição lateral com base nesse índice. O problema: enquanto `indiceTracado > 0`, o carro ainda avança frontalmente ao ritmo normal, chegando ao nó real visual antes de concluir a transição lateral, causando sobreposição com carros no traçado destino.

## Goals / Non-Goals

**Goals:**
- `ganhoSuave` mínimo de 1 nó/frame quando há qualquer diferença (carro sempre em movimento)
- Avanço proporcional à diferença com teto para não ultrapassar `noAtual`
- Catch-up acelerado (ganho dobrado) quando diferença > `SMOOTH_CATCHUP_DIFF` (100 nós)
- Snap imediato ao `noAtual` quando diferença > `SMOOTH_MAX_DIFF` (200 nós)
- Durante `indiceTracado > 0` (transição lateral ativa): `ganhoSuave` frontal reduzido por `SMOOTH_LATERAL_REDUCAO` (50%) para priorizar conclusão da transição
- Constantes configuráveis em `Global` para ajuste pós-validação

**Non-Goals:**
- Não alterar física do jogo (`noAtual`, `processarCiclo`, `processaColisao`)
- Não alterar multiplayer (`JogoCliente` mantém lógica atual com `MOD_GANHO_SUAVE_MULTIPLAYER`)
- Não alterar `decIndiceTracado()` — apenas controlar o avanço frontal durante a transição

## Decisions

**1. Avanço por velocidade constante base + proporcional**

Nova fórmula para `ganhoSuave`:
```java
int base = Math.max(1, diferencaSuavelReal / 10);  // 10% da diferença, mínimo 1
if (diferencaSuavelReal > SMOOTH_CATCHUP_DIFF) base *= 2;  // catch-up
ganhoSuave = Math.min(base, diferencaSuavelReal);
```

Para diferença de 5: `base = max(1, 0) = 1` → carro sempre move.
Para diferença de 50: `base = 5` → suave.
Para diferença de 120: `base = 12 * 2 = 24` → catch-up.
Acima de 200: snap imediato.

A divisão por 10 (10% da diferença) é equivalente ao `MOD_GANHO_SUAVE=10` mas com mínimo garantido de 1. Esse divisor pode ser ajustado pela constante `SMOOTH_GANHO_DIVISOR` em `Global`.

**2. Prioridade lateral durante transição de traçado**

Quando `piloto.getIndiceTracado() > 0`, aplicar redução:
```java
if (piloto.getIndiceTracado() > 0) {
    ganhoSuave = (int)(ganhoSuave * Global.SMOOTH_LATERAL_REDUCAO);
    ganhoSuave = Math.max(1, ganhoSuave);  // mínimo 1
}
```

Isso desacelera o avanço frontal suave, dando tempo para `decIndiceTracado()` completar a transição lateral antes do carro visual alcançar o nó real. Evita colisão visual com carros já no traçado destino.

**3. Limiar de snap reduzido para 200**

```java
if (diferencaSuavelReal > Global.SMOOTH_MAX_DIFF) {
    piloto.setNoAtualSuave(piloto.getNoAtual());
    Logger.logar("smooth snap diff=" + diferencaSuavelReal);
}
```

Substitui o limite atual de 1000 (linha ~744 em `PainelCircuito`). O limiar de 200 é configurável pós-validação.

**4. Constantes em `Global` (não em `PainelCircuito`)**

Centralizar em `Global` permite ajuste sem recompilar lógica de rendering:
- `SMOOTH_MAX_DIFF = 200` — snap threshold
- `SMOOTH_CATCHUP_DIFF = 100` — catch-up trigger
- `SMOOTH_GANHO_DIVISOR = 10` — divisor base (10% da diferença)
- `SMOOTH_LATERAL_REDUCAO = 0.5` — fator de redução durante transição lateral

## Risks / Trade-offs

- [Risco] Carro suave avança menos durante transição lateral → pode parecer "travado" momentaneamente → Mitigação: mínimo garantido de 1 nó/frame mesmo com redução; transições laterais duram ~100 frames no máximo.
- [Risco] Snap a 200 nós pode ser visualmente abrupto em corridas com muitos carros próximos → Mitigação: catch-up a 100 nós fecha a diferença antes de chegar a 200 na maioria dos casos; limiar é ajustável.
- [Risco] O divisor de 10 pode ser agressivo demais em circuitos lentos → Mitigação: `SMOOTH_GANHO_DIVISOR` configurável; validação com `simulacao_batch.sh` revela circuitos problemáticos.
- [Trade-off] `JogoCliente` (multiplayer) não usa essa nova lógica — mantém comportamento atual para não quebrar latência de rede.

## Migration Plan

1. Adicionar constantes em `Global.java`
2. Refatorar bloco de cálculo de `ganhoSuave` em `atualizacaoSuave()` (~linhas 700–715)
3. Substituir limiar de snap de 1000 para `Global.SMOOTH_MAX_DIFF`
4. Adicionar bloco de redução lateral após o cálculo de `ganhoSuave`
5. Validar com `MainFrame` (visual com `VALENDO=true`)
6. Rodar `simulacao_batch.sh` para verificar ausência de travamentos ou snaps excessivos
7. Ajustar constantes se necessário, repetir validação

Rollback: reverter o bloco em `atualizacaoSuave()` e remover constantes de `Global` — zero impacto na física.
