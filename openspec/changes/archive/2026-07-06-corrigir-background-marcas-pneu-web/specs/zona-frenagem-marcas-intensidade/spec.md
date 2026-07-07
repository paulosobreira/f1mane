## ADDED Requirements

### Requirement: Travada de roda pode gerar marca sem fumaça, independente de marca com fumaça
`ControleJogoLocal.geraTravadaRoda` SHALL decidir de forma independente se uma travada de roda também produz fumaça: toda travada de roda SHALL marcar o piloto (`piloto.setMarcaPneu(true)`), mas o contador de fumaça (`piloto.setTravouRodas(qtdeFumaca)`) SHALL só ser ativado (valor maior que zero) para uma fração dessas travadas, não em 100% delas. Isso restaura a distinção entre eventos "só marca" e "marca com fumaça" que os renderers (Swing e web) esperam.

#### Scenario: Travada de roda sem fumaça ainda marca o pneu
- **WHEN** `geraTravadaRoda` decide que esta travada de roda não deve gerar fumaça
- **THEN** `piloto.isMarcaPneu()` retorna `true` e `piloto.setTravouRodas(0)` é chamado (contador zerado, `isTravouRodas()` retorna `false`)

#### Scenario: Travada de roda com fumaça marca o pneu e ativa o contador
- **WHEN** `geraTravadaRoda` decide que esta travada de roda deve gerar fumaça
- **THEN** `piloto.isMarcaPneu()` retorna `true` e `piloto.setTravouRodas(qtdeFumaca)` é chamado com `qtdeFumaca` maior que zero (contador ativo, `isTravouRodas()` retorna `true`)

#### Scenario: Nem toda travada de roda gera fumaça
- **WHEN** múltiplas travadas de roda são geradas ao longo de uma corrida
- **THEN** existe pelo menos uma proporção configurável de eventos em que apenas a marca é produzida, sem fumaça — a fumaça não é mais um efeito garantido de 100% das travadas de roda
