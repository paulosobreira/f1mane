# piloto-sem-bloqueio-por-desconcentracao

## Purpose

Documentar que nenhum método de `Piloto` suprime ações do jogador/IA (teste de habilidade, decisão de IA, DRS/ERS, escape de fila indiana, troca de traçado) com base num estado interno de "desconcentração" invisível ao jogador. A mecânica antiga de `ciclosDesconcentrado`/`verificaDesconcentrado()` foi removida por completo; os eventos que antes a disparavam agora geram estresse diretamente (ver capacidade `piloto-gestao-estresse`).

## Requirements

### Requirement: Nenhuma ação do piloto é suprimida por estado interno invisível
Nenhum método de `Piloto` SHALL suprimir teste de habilidade, decisão de IA, tentativa de DRS/ERS, escape de fila indiana ou troca de traçado com base num estado que não seja observável pelo jogador (nem em UI, nem em JSON, nem em painel de debug).

#### Scenario: Teste de habilidade roda normalmente após incidente
- **WHEN** `testeHabilidadePiloto()` é chamado para um piloto que sofreu um dos eventos que antes causavam desconcentração (escapada de pista, ceder passagem, ser ultrapassado, largada ruim)
- **THEN** o teste é avaliado normalmente (sorteio contra a habilidade do piloto), sem ser forçado a falhar

#### Scenario: Decisão de IA roda normalmente após incidente
- **WHEN** `processaIAnovoIndex()` é chamado para um piloto que sofreu um dos eventos que antes causavam desconcentração
- **THEN** a tomada de decisão de IA do tick (uso de ERS/DRS, ultrapassagem/defesa, mudança de modo) prossegue normalmente, sem ser pulada

#### Scenario: DRS e ERS podem ser tentados normalmente
- **WHEN** `iaTentaUsarDRS()` ou `iaTentaUsarErs()` são chamados para um piloto que sofreu um dos eventos que antes causavam desconcentração
- **THEN** a tentativa de ativação prossegue normalmente, sem ser bloqueada

#### Scenario: Escape de fila indiana pode ser tentado normalmente
- **WHEN** `tentarEscaparFilaIndiana()` é chamado para um piloto que sofreu um dos eventos que antes causavam desconcentração
- **THEN** a lógica de escape prossegue normalmente, sem ser bloqueada

#### Scenario: Troca de traçado de volta à pista não é bloqueada
- **WHEN** `mudarTracado()` é chamado para sair das faixas de escape (traçado 4/5) de volta à pista, para um piloto que sofreu um dos eventos que antes causavam desconcentração
- **THEN** a troca prossegue normalmente, sem ser bloqueada

### Requirement: Nenhum downgrade forçado de modo de pilotagem ou giro do motor por estresse
`processaMudancaRegime()` SHALL NOT forçar o piloto a sair do modo AGRESSIVO ou do giro máximo por causa de estresse alto. A única condição que continua forçando modo/giro é a bandeirada (fim de corrida), que não faz parte da mecânica de desconcentração removida.

#### Scenario: Estresse no máximo não força downgrade de modo ou giro
- **WHEN** o piloto está em modo AGRESSIVO com giro máximo e o estresse está em 100 (o máximo possível)
- **THEN** `processaMudancaRegime()` não altera o modo de pilotagem nem o giro do motor

#### Scenario: Bandeirada continua forçando modo lento e giro mínimo
- **WHEN** o piloto recebeu a bandeirada (fim de corrida)
- **THEN** `processaMudancaRegime()` continua forçando o modo de pilotagem para LENTO e o giro para o mínimo — esse comportamento é independente da mecânica de desconcentração e não muda
