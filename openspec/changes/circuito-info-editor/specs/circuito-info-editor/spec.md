## ADDED Requirements

### Requirement: Circuito tem uma propriedade ciclo, não mais circuitos.properties
`Circuito` SHALL expor uma propriedade `ciclo` (inteiro, milissegundos por tick de simulação, valor padrão maior que zero), com getter/setter, persistida no arquivo de metadados do circuito. `properties/circuitos.properties` SHALL NOT conter mais esse valor — cada linha passa a ter só `<NomeExibicao>,<ativo>`.

#### Scenario: Circuito recém-criado tem um ciclo padrão
- **WHEN** um novo `Circuito` é instanciado sem que `ciclo` seja definido explicitamente
- **THEN** `circuito.getCiclo()` retorna um valor padrão maior que zero

#### Scenario: tempoCicloCircuito() lê o valor do circuito, não de circuitos.properties
- **WHEN** uma corrida (solo ou multiplayer) chama `InterfaceJogo.tempoCicloCircuito()` para o circuito carregado
- **THEN** o valor retornado é `circuito.getCiclo()`, sem consultar `circuitos.properties`

#### Scenario: circuitos.properties não tem mais o campo ciclo
- **WHEN** uma linha de `circuitos.properties` é lida após esta mudança
- **THEN** essa linha tem exatamente dois campos (`NomeExibicao`, `ativo`), sem um terceiro campo de ciclo

### Requirement: Editor calcula um tempo de volta estimado a partir do ciclo e do traçado
O editor de circuitos SHALL calcular e exibir, como informação somente-leitura, um tempo de volta estimado — a soma, para cada nó de `pistaFull`, de `1 / ganho médio do tipo daquele nó` (reta/largada, curva alta, curva baixa, usando constantes aproximadas derivadas de `Piloto.calculaModificadorPrincipal()`), multiplicada por `ciclo` (ms/tick). Esse valor SHALL ser recalculado sempre que `ciclo` for alterado ou o circuito for revetorizado.

#### Scenario: Alterar ciclo recalcula o tempo de volta estimado
- **WHEN** o usuário altera o valor de `ciclo` no editor
- **THEN** o tempo de volta estimado exibido é recalculado usando o novo valor de `ciclo`

#### Scenario: Editar o traçado recalcula o tempo de volta estimado
- **WHEN** o usuário adiciona ou remove nós de pista e o circuito é revetorizado
- **THEN** o tempo de volta estimado exibido reflete a nova contagem de nós por tipo

### Requirement: Editor mostra a distância do circuito em quilômetros
O editor de circuitos SHALL calcular e exibir, como informação somente-leitura, a distância aproximada do circuito em quilômetros, a partir do comprimento de `pistaFull` (uma amostra por pixel do traçado) e uma escala fixa de metros por pixel.

#### Scenario: Distância exibida cresce com o comprimento do traçado
- **WHEN** dois circuitos têm `pistaFull` de tamanhos diferentes (um traçado bem maior que o outro)
- **THEN** o circuito com `pistaFull` maior mostra uma distância em quilômetros maior

### Requirement: Migração dos circuitos existentes preserva o valor de ciclo
Todo circuito já listado em `circuitos.properties` no momento desta mudança SHALL ter o valor de `ciclo` hoje gravado nessa linha migrado para o campo `ciclo` do circuito correspondente, antes de esse campo ser removido de `circuitos.properties`.

#### Scenario: Circuito migrado mantém o mesmo ciclo de antes
- **WHEN** um circuito que já existia antes desta mudança é carregado após a migração
- **THEN** `circuito.getCiclo()` retorna o mesmo valor que estava gravado na linha correspondente de `circuitos.properties` antes da migração
