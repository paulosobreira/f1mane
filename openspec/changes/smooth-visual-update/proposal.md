## Why

O sistema atual de atualização suave (`atualizacaoSuave` em `PainelCircuito`) usa uma interpolação por percentual fixo (`MOD_GANHO_SUAVE = 4%` da diferença restante por frame), o que causa problemas: quando a diferença entre `noAtualSuave` e `noAtual` é pequena o carro avança menos de 1 nó por frame e fica estático visualmente; quando é grande o carro acelera visivelmente para "pular" ao nó real. Além disso, a mudança de traçado no visual pode causar colisão visual entre carros próximos pois o deslocamento lateral não considera a posição suave dos outros carros. O snap imediato acontece apenas a partir de 1000 nós de diferença — muito tarde. A nova solução deve garantir fluidez constante, manter o carro dentro de 100 nós do nó real e sincronizar imediatamente acima de 200 nós, priorizando evitar sobreposição visual durante transições de traçado.

## What Changes

- Substituir o `ganhoSuave` por percentual por um avanço por **velocidade visual constante baseada na diferença**: avanço mínimo garantido de 1 nó/frame, avanço proporcional à diferença quando maior que o limiar, com teto de ganho para não ultrapassar o nó real
- Reduzir o limiar de snap de 1000 para **200 nós** (configurável em `Global`)
- Adicionar limiar de **catch-up acelerado** quando diferença > 100 nós: ganho suave dobrado para fechar a diferença antes de atingir 200
- Durante transição de traçado (`indiceTracado > 0`): **priorizar movimento lateral** sobre avanço frontal — reduzir `ganhoSuave` frontal em 50% enquanto a transição estiver ativa, para sincronizar o traçado visual rapidamente e evitar colisão visual com carros no traçado destino
- Manter compatibilidade total com os traçados 0–5, box e escapada de pista
- Flag `Global.LOG_COLISAO` já existente pode ser reaproveitada para logar diferenças suave→real quando acima dos limiares

## Capabilities

### New Capabilities

- `smooth-visual-advance`: Lógica de avanço suave por velocidade constante com limiares de 100 (catch-up) e 200 (snap) nós, substituindo o avanço por percentual fixo
- `smooth-tracado-priority`: Durante transição de traçado, reduz o avanço frontal suave para priorizar a conclusão do movimento lateral e evitar colisão visual

### Modified Capabilities

- (nenhuma mudança de requisitos em specs existentes)

## Impact

- `PainelCircuito.java` — método `atualizacaoSuave()` (linhas 660–751): lógica de cálculo de `ganhoSuave` e limiar de snap
- `br.nnpe.Global` — novas constantes `SMOOTH_MAX_DIFF` (200), `SMOOTH_CATCHUP_DIFF` (100), `SMOOTH_LATERAL_REDUCAO` (0.5)
- Sem impacto em `Piloto.java`, `ControleCiclo`, nem na camada de jogo/física
- Sem impacto no modo multiplayer (mantém `MOD_GANHO_SUAVE_MULTIPLAYER` para `JogoCliente`)
