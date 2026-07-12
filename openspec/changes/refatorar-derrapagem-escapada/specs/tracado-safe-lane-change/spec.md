## MODIFIED Requirements

### Requirement: Mudança forçada não reinicia animação em andamento sem necessidade
Os pontos de mudança forçada de traçado (desvio de safety car) SHALL aguardar a animação de troca em andamento (`indiceTracado == 0`) antes de agir, adiando a ação por ciclos subsequentes em vez de interrompê-la. A derrapagem (traçado 0 → 1/2, ver spec `derrapagem-piloto`) usa mudança NÃO forçada (`mudarTracado(destino)`, sem `forcaMudar`), então já herda esse mesmo comportamento de espera pela guarda genérica de `mudarTracado` (`if (!forcaMudar && indiceTracado != 0) return false;`), sem precisar de uma checagem própria.

#### Scenario: Desvio forçado por safety car espera a animação atual terminar
- **WHEN** o piloto está no raio de desvio de um carro batido sob safety car e `indiceTracado > 0`
- **THEN** o desvio forçado não é aplicado neste ciclo e é reavaliado no ciclo seguinte

#### Scenario: Derrapagem em andamento de animação é adiada pela guarda genérica de mudarTracado
- **WHEN** um piloto satisfaz as condições de derrapagem (ver spec `derrapagem-piloto`) mas `indiceTracado > 0` (animação de troca em andamento)
- **THEN** a chamada não forçada a `mudarTracado` retorna `false` neste ciclo, e a derrapagem é reavaliada nos ciclos seguintes até `indiceTracado` chegar a 0
