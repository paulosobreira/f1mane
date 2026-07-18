## MODIFIED Requirements

### Requirement: Derrapagem do traçado 0 para o 1/2 é independente de stress e modo de pilotagem
Enquanto o piloto estiver no traçado 0, em um nó de curva baixa ou curva alta, com pneus abaixo de 30% de vida útil, e falhar em `testeHabilidadePilotoFreios()`, `ControleEscapada` SHALL mudar para o traçado 1 ou 2 (derrapagem) — sem depender de `getStress()` nem de `modoPilotagem`. A escolha de lado SHALL seguir a mesma regra já usada hoje: se `getTracadoAntigo()` for 1, muda para 2; se for 2, muda para 1; se não houver traçado anterior (0), sorteia entre 1 e 2. `ControleEscapada` SHALL chamar `controleJogo.travouRodas(piloto)` ao disparar a derrapagem, sem incrementar `stress` nem emitir mensagem de log.

#### Scenario: Pneus gastos e falha no teste de freios em curva baixa derrapam para o traçado 1 ou 2
- **WHEN** um piloto está no traçado 0, no nó atual do tipo curva baixa, com pneus abaixo de 30%, e `testeHabilidadePilotoFreios()` falha
- **THEN** o piloto muda para o traçado 1 ou 2 (conforme `getTracadoAntigo()`) e `controleJogo.travouRodas(this)` é chamado

#### Scenario: Pneus gastos e falha no teste de freios em curva alta também derrapam
- **WHEN** um piloto está no traçado 0, no nó atual do tipo curva alta, com pneus abaixo de 30%, e `testeHabilidadePilotoFreios()` falha
- **THEN** o piloto muda para o traçado 1 ou 2, do mesmo jeito que em curva baixa

#### Scenario: Pneus acima de 30% não derrapam, mesmo falhando no teste de freios
- **WHEN** um piloto está no traçado 0 em curva baixa ou alta, com pneus em 30% ou mais, e `testeHabilidadePilotoFreios()` falharia se chamado
- **THEN** o piloto permanece no traçado 0

#### Scenario: Sucesso no teste de freios evita a derrapagem mesmo com pneus gastos
- **WHEN** um piloto está no traçado 0 em curva baixa ou alta, com pneus abaixo de 30%, e `testeHabilidadePilotoFreios()` é bem-sucedido
- **THEN** o piloto permanece no traçado 0

#### Scenario: Fora de curva (reta, largada, box) não derrapa
- **WHEN** um piloto está no traçado 0, com pneus abaixo de 30%, mas o nó atual não é curva baixa nem curva alta
- **THEN** o piloto permanece no traçado 0, independentemente do resultado de `testeHabilidadePilotoFreios()`

#### Scenario: Stress e modo de pilotagem não influenciam a derrapagem
- **WHEN** um piloto satisfaz as condições de derrapagem (traçado 0, curva baixa/alta, pneus < 30%, falha no teste de freios) com `modoPilotagem == NORMAL` e `stress == 0`
- **THEN** o piloto derrapa normalmente para o traçado 1 ou 2, exatamente como derraparia em `AGRESSIVO` com stress alto

#### Scenario: Alternância de lado usa o traçado anterior quando disponível
- **WHEN** um piloto que estava no traçado 1 antes de ir para o traçado 0 (`getTracadoAntigo() == 1`) satisfaz as condições de derrapagem
- **THEN** o piloto deriva para o traçado 2 (lado oposto ao anterior)

#### Scenario: Sem traçado anterior, o lado é sorteado
- **WHEN** um piloto sem traçado anterior definido (`getTracadoAntigo() == 0`) satisfaz as condições de derrapagem
- **THEN** o piloto deriva para o traçado 1 ou 2, sorteado por `controleJogo.getRandom().intervalo(1, 2)`

### Requirement: Guardas de segurança já existentes continuam bloqueando a derrapagem
`ControleEscapada.processaDerrapagem()` SHALL NOT disparar a derrapagem quando o safety car está na pista, quando o modo é qualify, ou quando o piloto está com `getPtosBox() != 0` — as mesmas guardas já aplicadas hoje no topo do método.

#### Scenario: Safety car na pista impede a derrapagem
- **WHEN** o safety car está na pista e um piloto satisfaz as demais condições de derrapagem
- **THEN** o piloto não derrapa nesse ciclo

#### Scenario: Modo qualify impede a derrapagem
- **WHEN** a corrida está em modo qualify e um piloto satisfaz as demais condições de derrapagem
- **THEN** o piloto não derrapa nesse ciclo

#### Scenario: Piloto em rota de box não derrapa
- **WHEN** `getPtosBox() != 0` e um piloto satisfaz as demais condições de derrapagem
- **THEN** o piloto não derrapa nesse ciclo
