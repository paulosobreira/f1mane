## REMOVED Requirements

### Requirement: Tempo médio de volta acessível ao controle de clima
**Reason**: O cálculo de `tempoMedioVoltaMs` (média de `Volta.obterTempoVolta()` convertida via `tempoCicloCircuito()`) tinha um bug de unidade — `obterTempoVolta()` já retorna milissegundos reais de parede (preenchido via `System.currentTimeMillis()` em `ControleEstatisticas.processaVoltaRapida()`), então multiplicar de novo por `tempoCicloCircuito()` inflava o resultado por um fator de dezenas a centenas de vezes, fazendo o atraso real de disparo de `ThreadMudancaClima` chegar a horas em corridas reais (observado em produção: valores de 11,9 a 15,4 milhões de ms, quando o esperado era ~1 minuto). Em vez de corrigir o cálculo, ele foi substituído por valores fixos em minutos nos dois pontos que o usavam.
**Migration**: `ThreadMudancaClima` usa `Global.ATRASO_MAX_MUDANCA_CLIMA_MS` (1 minuto fixo) como teto do atraso de disparo (ver "Disparo da mudança de clima em atraso aleatório de até 1 minuto"); `ControleClima.duracaoRampaCiclos()` usa `Global.DURACAO_RAMPA_MOLHADO_MS` (1min30 fixo) como duração da rampa de "molhado%" (ver "Rampa reversível de 'molhado%'..."). `tempoMedioVoltaMs()` foi removido de `InterfaceJogo`, `ControleJogoLocal` e `JogoCliente` por ficar completamente sem uso.

## MODIFIED Requirements

### Requirement: Disparo da mudança de clima em intervalo aleatório dentro do tempo médio de volta
Uma vez disparada, a transição (`ThreadMudancaClima`) SHALL dormir um atraso aleatório uniformemente distribuído entre 0 e um teto fixo de 1 minuto (`Global.ATRASO_MAX_MUDANCA_CLIMA_MS` = 60000ms) antes de efetivar a mudança de clima, substituindo tanto o intervalo fixo de narrativa (3-15 segundos reais) quanto o tempo médio de volta calculado, usados anteriormente.

#### Scenario: Atraso de disparo dentro da janela do tempo médio de volta
- **WHEN** `ControleClima` dispara uma nova tentativa de mudança de clima
- **THEN** a thread responsável pela transição dorme um valor aleatório entre 0 e 60000ms (`Global.ATRASO_MAX_MUDANCA_CLIMA_MS`) antes de efetivar a mudança e notificar os jogadores

### Requirement: Rampa reversível de "molhado%" entre condição seca e chuvosa
`ControleClima` SHALL manter um valor contínuo "molhado%" (0.0 a 1.0), independente do clima categórico exibido (`SOL`/`NUBLADO`/`CHUVA`). Quando o clima categórico transiciona de `NUBLADO` para `CHUVA`, "molhado%" SHALL subir linearmente de seu valor atual até 1.0 ao longo de uma duração fixa de 1 minuto e meio (`Global.DURACAO_RAMPA_MOLHADO_MS` = 90000ms). Quando o clima categórico transiciona de `CHUVA` para `NUBLADO`, "molhado%" SHALL descer linearmente de seu valor atual até 0.0 ao longo dessa mesma duração fixa. Transições entre `SOL` e `NUBLADO` sem passar por `CHUVA` SHALL NOT alterar "molhado%". Se o clima categórico mudar de direção (seco↔chuva) enquanto uma rampa está em andamento, "molhado%" SHALL inverter sua direção-alvo a partir do valor atual, sem saltar para 0.0 ou 1.0 antes de inverter.

#### Scenario: Chuva começando sobe "molhado%" gradualmente
- **WHEN** o clima categórico muda de `NUBLADO` para `CHUVA`
- **THEN** "molhado%" começa a subir linearmente do valor atual até 1.0, levando 1 minuto e meio (`Global.DURACAO_RAMPA_MOLHADO_MS`) para atingir 1.0 caso não haja nova mudança de clima nesse intervalo

#### Scenario: Chuva parando desce "molhado%" gradualmente
- **WHEN** o clima categórico muda de `CHUVA` para `NUBLADO`
- **THEN** "molhado%" começa a descer linearmente do valor atual até 0.0, levando 1 minuto e meio (`Global.DURACAO_RAMPA_MOLHADO_MS`) para atingir 0.0 caso não haja nova mudança de clima nesse intervalo

#### Scenario: Transição sol/nublado isolada não afeta "molhado%"
- **WHEN** o clima categórico muda entre `SOL` e `NUBLADO` sem que `CHUVA` esteja envolvido na transição
- **THEN** "molhado%" permanece inalterado

#### Scenario: Reversão de rampa em andamento inverte a partir do valor atual
- **WHEN** "molhado%" está subindo em direção a 1.0 e ainda não chegou lá, e o clima categórico muda de volta de `CHUVA` para `NUBLADO` antes da rampa completar
- **THEN** "molhado%" passa a descer em direção a 0.0 a partir do valor que tinha no momento da reversão, sem saltar para 1.0 primeiro
