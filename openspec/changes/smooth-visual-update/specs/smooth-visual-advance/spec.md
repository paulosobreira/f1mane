## ADDED Requirements

### Requirement: Constantes de limiar de atualização suave
O sistema SHALL expor em `br.nnpe.Global` as constantes: `SMOOTH_MAX_DIFF` (int, padrão 200), `SMOOTH_CATCHUP_DIFF` (int, padrão 100), `SMOOTH_GANHO_DIVISOR` (int, padrão 10) e `SMOOTH_LATERAL_REDUCAO` (double, padrão 0.5). Essas constantes controlam o comportamento do avanço suave e SHALL ser mutáveis em tempo de execução para facilitar ajustes pós-validação.

#### Scenario: Constantes acessíveis globalmente
- **WHEN** qualquer classe acessa `Global.SMOOTH_MAX_DIFF`
- **THEN** o valor retornado é 200 por padrão

### Requirement: Avanço suave com mínimo garantido de 1 nó por frame
O método `atualizacaoSuave()` em `PainelCircuito` SHALL calcular `ganhoSuave` como `Math.max(1, diferencaSuavelReal / Global.SMOOTH_GANHO_DIVISOR)`, garantindo que quando há qualquer diferença entre `noAtualSuave` e `noAtual` o carro visual sempre avança pelo menos 1 nó por frame. O `ganhoSuave` SHALL ser limitado a `diferencaSuavelReal` (nunca ultrapassa o nó real).

#### Scenario: Diferença pequena não trava o carro
- **WHEN** `diferencaSuavelReal` é 5 nós
- **THEN** `ganhoSuave` é 1 (mínimo garantido, pois 5/10=0 arredondado)
- **THEN** `noAtualSuave` avança 1 nó naquele frame

#### Scenario: Diferença moderada avança proporcionalmente
- **WHEN** `diferencaSuavelReal` é 50 nós
- **THEN** `ganhoSuave` é 5 (50/10)
- **THEN** `noAtualSuave` avança 5 nós naquele frame

### Requirement: Catch-up acelerado acima de 100 nós de diferença
Quando `diferencaSuavelReal` exceder `Global.SMOOTH_CATCHUP_DIFF` (100), o `ganhoSuave` calculado SHALL ser multiplicado por 2, permitindo que a posição suave feche a diferença antes de atingir o limiar de snap.

#### Scenario: Diferença acima de 100 ativa catch-up
- **WHEN** `diferencaSuavelReal` é 120 nós
- **THEN** `ganhoSuave` base é `max(1, 120/10) = 12`, multiplicado por 2 = 24
- **THEN** `noAtualSuave` avança 24 nós naquele frame

### Requirement: Sincronização imediata acima de 200 nós de diferença
Quando `diferencaSuavelReal` exceder `Global.SMOOTH_MAX_DIFF` (200), `noAtualSuave` SHALL ser definido imediatamente igual a `noAtual` (snap), e o evento SHALL ser registrado em log. O limiar de 1000 nós existente SHALL ser substituído por `Global.SMOOTH_MAX_DIFF`.

#### Scenario: Snap imediato ao ultrapassar 200 nós
- **WHEN** `diferencaSuavelReal` é 201 nós
- **THEN** `noAtualSuave` é definido igual a `noAtual` no mesmo frame
- **THEN** um log é emitido com o valor da diferença

#### Scenario: Diferença de 199 nós não causa snap
- **WHEN** `diferencaSuavelReal` é 199 nós
- **THEN** nenhum snap ocorre; `ganhoSuave` é calculado normalmente com catch-up ativo
