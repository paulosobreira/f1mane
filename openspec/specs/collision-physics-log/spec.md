# Spec: collision-physics-log

## Purpose

Logging detalhado do sistema de detecção de colisão entre carros durante a simulação, controlado por flag estática para uso em debug e análise de comportamento.

## Requirements

### Requirement: Flag de log de colisão
O sistema SHALL disponibilizar a flag estática `Global.LOG_COLISAO` (boolean) que controla a geração de logs detalhados de colisão. A flag SHALL ser `false` por padrão e `true` quando `MainFrameSimulacao` inicializa a corrida.

#### Scenario: Flag desativada não gera log
- **WHEN** `Global.LOG_COLISAO` é `false`
- **THEN** nenhuma linha de log prefixada com `[COLISAO]` é gerada durante o ciclo

#### Scenario: Flag ativada gera log por piloto por ciclo
- **WHEN** `Global.LOG_COLISAO` é `true`
- **THEN** ao final de cada chamada a `processaColisao()` para cada piloto, o sistema SHALL emitir via `Logger.logar` uma linha contendo: prefixo `[COLISAO]`, nome do piloto, `noIndex`, `tracado`, bounds das três hitboxes (dianteira/centro/traseira como `x,y,w,h`), valores booleanos de `colisaoDiantera` e `colisaoCentro`, e valor atual de `ganho`

### Requirement: Log de evento de colisão detectada
Quando uma colisão é detectada (piloto colisão != null), o sistema SHALL emitir um log adicional com prefixo `[COLISAO_EVENTO]` identificando os dois pilotos envolvidos, seus índices na pista e o tipo de colisão (DIANTEIRA_TRAZEIRA, DIANTEIRA_CENTRO).

#### Scenario: Colisão dianteira com traseira registrada
- **WHEN** `colisaoDiantera` é true e a interseção é com `trazeiraColisao` do piloto à frente
- **THEN** log `[COLISAO_EVENTO]` contém tipo=`DIANTEIRA_TRAZEIRA`

#### Scenario: Colisão dianteira com centro registrada
- **WHEN** `colisaoDiantera` é true e a interseção é com `centroColisao` do piloto à frente
- **THEN** log `[COLISAO_EVENTO]` contém tipo=`DIANTEIRA_CENTRO`

#### Scenario: Colisão centro com traseira registrada
- **WHEN** `colisaoCentro` é true
- **THEN** log `[COLISAO_EVENTO]` contém tipo=`CENTRO_TRAZEIRA`
