## MODIFIED Requirements

### Requirement: Regras de disparo preservadas
`Piloto.processaStress()` SHALL reavaliar, para cada gatilho migrado, exatamente a mesma condição e o mesmo valor que existiam antes da consolidação — a mudança é de organização de código, não de comportamento de jogo, exceto pelos gatilhos que substituem a mecânica de desconcentração removida (marcados abaixo), cujos valores são novos por definição.

#### Scenario: Pneu incompatível com o clima
- **WHEN** o piloto está numa curva baixa ou alta com pneu incompatível com o clima atual
- **THEN** `processaStress()` incrementa o estresse em 4 (curva baixa) ou 2 (curva alta), reduzido a 0 se `testeHabilidadePiloto()` for bem-sucedido — igual ao comportamento anterior em `processaPneusIncomaptiveis()`

#### Scenario: Freada mal-sucedida sob pressão (magnitude aumentada)
- **WHEN** o piloto está em curva baixa, freando na reta, entre os 3 primeiros colocados, com sorteio acima de 0.9 e falha no teste de habilidade de freios
- **THEN** `processaStress()` incrementa o estresse em `30 - desgastePneus/100` (era `10 - desgastePneus/100`) — aumentado a pedido do usuário pra dar mais peso a esse evento, embora o cap interno de `incStress()` (`stress>70/80/90`) limite o efeito na prática quando o piloto já está estressado, que é o caso mais comum pra esse gatilho

#### Scenario: Colisão em andamento (magnitude aumentada, casos agora exclusivos entre si)
- **WHEN** `evitaBaterCarroFrente` é falso e há uma colisão em andamento (`getColisao() != null`)
- **THEN** `processaStress()` incrementa o estresse em 12 (era 1, depois 13) — aumentado a pedido do usuário pra aproximar do patamar de desgaste de pneu
- **AND WHEN** `evitaBaterCarroFrente` é verdadeiro (independente de haver colisão em andamento)
- **THEN** `processaStress()` incrementa o estresse em 8 no lugar dos 12 da colisão — os dois casos passaram a ser mutuamente exclusivos (antes eram aditivos, 13+13); como `evitaBaterCarroFrente` é bem mais frequente que colisão de fato (~50.000 vs ~2.000 eventos por corrida em medição), esse gatilho passou a responder por ~31% do estresse total, um salto grande em relação ao ~3% de antes

#### Scenario: Desgaste de pneu por tipo de nó (reescrito — chance de pular escala com o desgaste, magnitude fixa por modo, curva alta agora também incrementa)
- **WHEN** o piloto processa desgaste de pneu em qualquer nó, fora de bandeirada e sem safety car na pista
- **THEN** `processaStress()` primeiro sorteia uma chance de pular o gatilho inteiro nesse tick, proporcional à porcentagem de desgaste do pneu (`getRandom().nextDouble() < porcentagemDesgastePneus/100.0`) — quanto mais desgastado o pneu, maior a chance de nenhum efeito ocorrer nesse tick
- **AND WHEN** o nó é curva baixa e o piloto está em modo AGRESSIVO
- **THEN** incrementa o estresse em 20, reduzido a 10 se `testeHabilidadePilotoAerodinamicaFreios()` for bem-sucedido
- **AND WHEN** o nó é curva baixa e o piloto está em modo NORMAL
- **THEN** incrementa o estresse em 10, reduzido a 5 se `testeHabilidadePilotoAerodinamicaFreios()` for bem-sucedido
- **AND WHEN** o nó é curva alta e o piloto está em modo AGRESSIVO
- **THEN** incrementa o estresse em 20, reduzido a 10 se `testeHabilidadePilotoCarro()` for bem-sucedido — antes curva alta nunca incrementava estresse por desgaste de pneu, só decrementava
- **AND WHEN** o nó é curva alta e o piloto está em modo NORMAL
- **THEN** incrementa o estresse em 10, reduzido a 5 se `testeHabilidadePilotoCarro()` for bem-sucedido
- **AND WHEN** o piloto está em modo LENTO, ou o nó é reta/largada
- **THEN** não há incremento algum — não há intenção, em nenhum cenário, de desgaste de pneu aumentar o estresse em reta/largada, e o modo LENTO nunca gera esse incremento
- **AND** este gatilho não decrementa mais o estresse em nenhuma condição — o decremento por limiar (`stress > 80`/`70`) que existia nas versões anteriores foi removido; a única recuperação de estresse que resta é o decaimento passivo genérico por tick em `processaStress()`

#### Scenario: Acidente com perda de aerofólio (magnitude aumentada)
- **WHEN** `ControleCorrida.danificaAreofolio()` sinaliza que o piloto sofreu dano de aerofólio neste tick
- **THEN** `processaStress()` incrementa o estresse em 30 (era 15) nesse mesmo tick — aumentado a pedido do usuário, mas o cap interno de `incStress()` limita o efeito prático quando o piloto já está estressado (medição em simulação mostrou pouca mudança na média por evento apesar do dobro de magnitude)

#### Scenario: Avanço na fila do box
- **WHEN** o piloto está avançando na fila de espera do box (posição na fila menor que o tamanho total da fila)
- **THEN** `processaStress()` decrementa o estresse em 2 a cada tick — igual ao comportamento anterior em `ControleBox`

#### Scenario: Escapada de pista sob pressão (substitui desconcentração)
- **WHEN** o piloto escapa da pista com estresse acima do limiar de re-erro de curva, em modo AGRESSIVO
- **THEN** `processaStress()` incrementa o estresse em 3, ou em 1 se `testeHabilidadePiloto()` for bem-sucedido, no lugar de iniciar o antigo bloqueio de `ciclosDesconcentrado`

#### Scenario: Ceder passagem a carro mais rápido (substitui desconcentração)
- **WHEN** um piloto retardatário cede passagem a um carro mais rápido atrás
- **THEN** `processaStress()` incrementa o estresse do retardatário em 2, ou em 1 se `testeHabilidadePiloto()` for bem-sucedido, no lugar de iniciar o antigo bloqueio de `ciclosDesconcentrado`

#### Scenario: Ser ultrapassado / mensagem de retardatário (substitui desconcentração)
- **WHEN** o piloto recebe a mensagem de retardatário por estar sendo ultrapassado
- **THEN** `processaStress()` incrementa o estresse em 5, ou em 2 se `testeHabilidadePiloto()` for bem-sucedido, no lugar de iniciar o antigo bloqueio de `ciclosDesconcentrado`

#### Scenario: Largada ruim (substitui desconcentração)
- **WHEN** `ControleQualificacao` sorteia uma largada ruim para um piloto não-humano sem habilidade (5% de chance)
- **THEN** `processaStress()` incrementa o estresse num valor sorteado entre 15 e 20, reduzido à metade se `testeHabilidadePiloto()` for bem-sucedido, no lugar de iniciar o antigo bloqueio de `ciclosDesconcentrado` de 500-700 ciclos

#### Scenario: Recuperação passiva por modo de pilotagem (multiplicador do NORMAL reduzido)
- **WHEN** `decStress()` é chamado com o piloto em modo NORMAL
- **THEN** o valor de recuperação é escalado em 1.1x (era 1.25x) — reduzido a pedido do usuário porque a recuperação do NORMAL, somada ao decaimento passivo incondicional por tick, tinha ficado agressiva demais; o multiplicador do modo LENTO permanece em 1.5x e o do AGRESSIVO permanece sem escala (1x), preservando a ordem AGRESSIVO < NORMAL < LENTO

## REMOVED Requirements

### Requirement: Desconcentração em modo agressivo
**Reason**: A mecânica de `ciclosDesconcentrado`/`verificaDesconcentrado()` foi removida por completo (ver capacidade de comportamento do piloto) — não existe mais estado de "desconcentrado" para gerar esse incremento de estresse.
**Migration**: Os eventos que antes causavam desconcentração agora geram estresse diretamente (ver os 4 novos cenários acima, marcados "substitui desconcentração").

#### Scenario: Desconcentração em modo agressivo (removido)
- **WHEN** o piloto está no modo AGRESSIVO, com estresse abaixo de 70, e desconcentrado
- **THEN** (comportamento removido) `processaStress()` incrementava o estresse em 1 — não existe mais, pois o estado de desconcentração não existe mais
