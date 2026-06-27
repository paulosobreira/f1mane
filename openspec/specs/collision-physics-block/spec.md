# Spec: collision-physics-block

## Purpose

Regras físicas de bloqueio e penalidade de avanço aplicadas quando hitboxes de carros se intersectam durante a simulação de corrida.

## Requirements

### Requirement: Bloqueio de avanço por colisão dianteira-centro
Quando `colisaoDiantera` do carro de trás intersecta `centroColisao` do carro à frente no mesmo traçado, o sistema SHALL limitar o `ganho` do carro de trás ao valor `ganho` do carro à frente (o carro de trás não pode avançar além do carro da frente no mesmo ciclo).

#### Scenario: Carro de trás não ultrapassa carro da frente por colisão de centro
- **WHEN** `processaPenalidadeColisao` é chamado e `colisaoDiantera` está ativa com interseção no `centroColisao` do piloto à frente
- **THEN** `ganho` do piloto de trás é definido como `Math.min(ganho, pilotoFrente.getGanho())`
- **THEN** o `noIndex` resultante do piloto de trás no final do ciclo SHALL ser menor ou igual ao `noIndex` do piloto à frente

#### Scenario: Colisão em traçados diferentes não aplica bloqueio
- **WHEN** piloto de trás e piloto à frente estão em traçados diferentes (`tracado != tracado`)
- **THEN** `processaPenalidadeColisao` não aplica cap de ganho mesmo que hitboxes se intersectem

### Requirement: Desaceleração por colisão dianteira-traseira
Quando `colisaoDiantera` do carro de trás intersecta apenas `trazeiraColisao` do carro à frente (sem interseção com centro), o sistema SHALL aplicar redução de `ganho` em 30% além de limitar ao ganho do carro da frente.

#### Scenario: Toque traseiro desacelera carro de trás consideravelmente
- **WHEN** `processaPenalidadeColisao` detecta colisão apenas com `trazeiraColisao` (não com `centroColisao`)
- **THEN** `ganho` do piloto de trás é `Math.min(ganho * 0.7, pilotoFrente.getGanho())`

### Requirement: Colisão centro-traseira impede avanço
Quando `colisaoCentro` do carro de trás intersecta `trazeiraColisao` do carro à frente, o carro de trás SHALL manter seu `ganho` limitado ao `ganho` do carro à frente (não ultrapassa).

#### Scenario: Interseção centro com traseira bloqueia avanço
- **WHEN** `colisaoCentro` é true e `colisaoDiantera` é false
- **THEN** `ganho` do piloto de trás é `Math.min(ganho, pilotoFrente.getGanho())`
