## ADDED Requirements

### Requirement: Ordem de prioridade das causas de mudança de traçado por ciclo
A cada ciclo, `Piloto.processaMudarTracado()` SHALL avaliar as causas de mudança de traçado proativa (excluindo mecânicas físicas sempre-ativas de outras specs: box, derrapagem, escapada) em ordem de prioridade fixa, agindo apenas na primeira causa cuja condição for satisfeita nesse ciclo:

1. Desvio de carro batido sob safety car na mesma linha
2. Entrada/saída de faixa de boxes (snap de traçado)
3. Escape de fila indiana (`tentarEscaparFilaIndiana`, ver spec `tracado-safe-lane-change`)
4. Evitar colidir com retardatário à frente (piloto automático ativo)
5. Desviar de retardatário no mesmo traçado mais à frente (piloto automático ativo)
6. Espelhar o traçado do carro imediatamente atrás (só piloto de IA)
7. Recentralizar no traçado 0 quando não há tráfego relevante à frente nem atrás (só piloto de IA)

#### Scenario: Duas causas satisfeitas simultaneamente, só a de maior prioridade age
- **WHEN** num mesmo ciclo, tanto a condição de "evitar colidir com retardatário à frente" quanto a de "recentralizar sem tráfego" seriam satisfeitas isoladamente
- **THEN** apenas a ação de maior prioridade na lista ocorre nesse ciclo; a de menor prioridade não é avaliada

#### Scenario: Nenhuma causa satisfeita não altera o traçado
- **WHEN** nenhuma das sete condições é satisfeita num ciclo
- **THEN** `processaMudarTracado()` não chama `mudarTracado` nesse ciclo

### Requirement: Cada causa de mudança de traçado é isolável e testável independentemente
Cada uma das sete causas listadas SHALL ser implementada como um método privado nomeado que retorna `boolean` indicando se agiu, permitindo testar sua condição de disparo isoladamente das demais.

#### Scenario: Teste unitário isola uma única causa
- **WHEN** um teste unitário monta um cenário que satisfaz apenas a condição de uma causa específica (ex.: espelhar traçado do carro atrás) e nenhuma das causas de prioridade mais alta
- **THEN** é possível verificar o resultado chamando o método privado correspondente diretamente (via visibilidade de pacote), sem depender de montar as pré-condições de todas as causas anteriores
