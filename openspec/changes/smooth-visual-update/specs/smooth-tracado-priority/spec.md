## ADDED Requirements

### Requirement: Redução do avanço frontal durante transição de traçado
Quando `piloto.getIndiceTracado() > 0` (transição lateral ativa), o `ganhoSuave` frontal calculado em `atualizacaoSuave()` SHALL ser multiplicado por `Global.SMOOTH_LATERAL_REDUCAO` (padrão 0.5), com mínimo garantido de 1 nó/frame. Isso prioriza a conclusão do movimento lateral (controlado por `decIndiceTracado()`) sobre o avanço frontal, reduzindo o risco de sobreposição visual com carros já presentes no traçado destino.

#### Scenario: Avanço frontal reduzido durante transição lateral
- **WHEN** `piloto.getIndiceTracado()` é 80 (transição em andamento)
- **THEN** `ganhoSuave` calculado de 10 é multiplicado por 0.5 = 5
- **THEN** `noAtualSuave` avança apenas 5 nós frontais naquele frame enquanto `indiceTracado` decrementa normalmente

#### Scenario: Mínimo garantido de 1 nó mesmo com redução lateral
- **WHEN** `piloto.getIndiceTracado()` é 30 e `ganhoSuave` calculado é 1
- **THEN** após multiplicação por 0.5 = 0.5, arredondado para 0, o mínimo de 1 é aplicado
- **THEN** `noAtualSuave` avança 1 nó frontal

#### Scenario: Avanço normal retomado após conclusão da transição
- **WHEN** `piloto.getIndiceTracado()` é 0 (transição concluída)
- **THEN** `SMOOTH_LATERAL_REDUCAO` não é aplicado e `ganhoSuave` usa valor completo calculado

### Requirement: Snap durante transição lateral sincroniza traçado visual
Quando um snap por `SMOOTH_MAX_DIFF` ocorre enquanto `indiceTracado > 0`, além de sincronizar `noAtualSuave` com `noAtual`, o traçado visual SHALL ser sincronizado: `indiceTracado` é zerado e o traçado do piloto é assumido como o traçado real atual, encerrando imediatamente a transição lateral.

#### Scenario: Snap encerra transição lateral em andamento
- **WHEN** `diferencaSuavelReal` excede 200 e `indiceTracado` é 40
- **THEN** `noAtualSuave` é sincronizado com `noAtual`
- **THEN** `indiceTracado` é definido como 0, encerrando o movimento lateral
