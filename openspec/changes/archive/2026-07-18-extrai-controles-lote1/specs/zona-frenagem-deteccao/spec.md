## MODIFIED Requirements

### Requirement: Zona de frenagem substitui a janela fixa de distância em processaFreioNaReta
`ControleFreio.processaFreioNaReta()` SHALL usar a zona de frenagem detectada (em vez de apenas a distância até a curva mais próxima de qualquer tipo) para decidir quando `freiandoReta` deve ser `true` e quando o gatilho de travada de roda (`travouRodas`) pode ocorrer.

#### Scenario: Freiando fora de qualquer zona de frenagem detectada não aciona travada de roda por este caminho
- **WHEN** o piloto está numa reta que não faz parte de nenhuma zona de frenagem detectada
- **THEN** `processaFreioNaReta` não aciona `controleJogo.travouRodas(this)` por esse motivo, mesmo que a curva mais próxima de qualquer tipo esteja a menos de 300 nós

#### Scenario: Freiando dentro de uma zona de frenagem mantém o comportamento de suavização de ganho
- **WHEN** o piloto está dentro de uma zona de frenagem detectada, numa reta antes do cluster de curva baixa
- **THEN** `freiandoReta` é `true` e o multiplicador de ganho (`multi`/`minMulti`) continua sendo aplicado de forma gradual conforme a proximidade da curva, como antes desta mudança
