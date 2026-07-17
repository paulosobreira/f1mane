## MODIFIED Requirements

### Requirement: Regras de disparo preservadas
`Piloto.processaStress()` SHALL reavaliar, para cada gatilho migrado, exatamente a mesma condição e o mesmo valor que existiam antes da consolidação — a mudança é de organização de código, não de comportamento de jogo, exceto pelos gatilhos que substituem a mecânica de desconcentração removida (marcados abaixo), cujos valores são novos por definição, e exceto pelos gatilhos de balanceamento descritos nesta revisão (freada mal-sucedida na reta e recuperação por modo de pilotagem).

#### Scenario: Pneu incompatível com o clima
- **WHEN** o piloto está numa curva baixa ou alta com pneu incompatível com o clima atual
- **THEN** `processaStress()` incrementa o estresse em 4 (curva baixa) ou 2 (curva alta), reduzido a 0 se `testeHabilidadePiloto()` for bem-sucedido — igual ao comportamento anterior em `processaPneusIncomaptiveis()`

#### Scenario: Freada mal-sucedida na reta (generalizada para todos os pilotos, avaliada na zona de frenagem)
- **WHEN** o piloto está na reta, dentro de uma zona de frenagem detectada (não num nó de curva), com `retardaFreiandoReta` ativo, sorteio acima de 0.9 e falha no teste de habilidade de freios
- **THEN** `processaStress()` incrementa o estresse em `30 - desgastePneus/100`, reduzido na prática pelo cap interno de `incStress()` (`stress>50/70/90`) quando o piloto já está estressado, que é o caso mais comum pra esse gatilho
- **AND** a restrição anterior de elegibilidade (`getPosicao() <= 3`, top-3) é removida — o gatilho passa a valer para qualquer piloto na pista
- **AND** o gatilho continua disparando no máximo uma vez por evento de frenagem (a mesma trava de consumo de `retardaFreiandoReta`, resetada no mesmo instante em que o gatilho é avaliado, garante que o sorteio de >0.9 não seja reavaliado a cada tick dentro da zona de frenagem)

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
- **AND** esse decremento passa pelo multiplicador de recuperação por modo (ver cenário "Recuperação por modo de pilotagem" abaixo) — um piloto em modo efetivo AGRESSIVO não recupera nada aqui, diferente de antes desta revisão

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

#### Scenario: Recuperação por modo de pilotagem (AGRESSIVO deixa de recuperar)
- **WHEN** `decStress()` é chamado com o piloto em modo efetivo AGRESSIVO
- **THEN** o valor de recuperação é escalado em 0x — o piloto não recupera estresse por essa via, seja no decaimento passivo por tick (que já excluía AGRESSIVO antes desta revisão) ou no avanço na fila do box (que antes aplicava o valor cheio)
- **AND WHEN** `decStress()` é chamado com o piloto em modo NORMAL
- **THEN** o valor de recuperação é escalado em 1x (era 1.1x) — reduzido a pedido do usuário
- **AND WHEN** `decStress()` é chamado com o piloto em modo LENTO
- **THEN** o valor de recuperação é escalado em 1.5x, inalterado — a ordem relativa AGRESSIVO < NORMAL < LENTO é preservada

### Requirement: Ponto único de decisão de estresse
`Piloto.processaStress()` SHALL ser o único método que decide quando e quanto o estresse do piloto (`incStress`/`decStress`) muda a cada tick. Nenhum outro método, em nenhuma outra classe, SHALL chamar `incStress`/`decStress` diretamente.

#### Scenario: Desgaste de pneu não altera estresse diretamente
- **WHEN** `Carro.calculaDesgastePneus(No)` é executado para qualquer tipo de nó
- **THEN** o método não chama `incStress`/`decStress` do piloto; ele pode ler `getStress()` para decidir seus próprios efeitos (ex.: desgaste do pneu), mas não escreve no estresse

#### Scenario: Colisão não altera estresse diretamente
- **WHEN** `Piloto.processaPenalidadeColisao()` é executado com uma colisão em andamento
- **THEN** o método não chama `incStress` diretamente; a penalidade de estresse por colisão é aplicada por `processaStress()`

#### Scenario: Avanço na fila do box não altera estresse diretamente
- **WHEN** `ControleBox` avança um piloto na fila do box
- **THEN** `ControleBox` não chama `decStress` diretamente; a redução de estresse por estar no box é aplicada por `processaStress()`

#### Scenario: Acidente com perda de aerofólio não altera estresse diretamente
- **WHEN** `ControleCorrida.danificaAreofolio(Piloto)` decide que houve dano de aerofólio
- **THEN** o método não chama `incStress` diretamente; ele sinaliza o evento para o piloto, e `processaStress()` aplica o incremento de estresse correspondente no mesmo tick

#### Scenario: Tetos de incStress por faixa de stress (limiares revisados)
- **WHEN** `incStress(val)` é chamado com o piloto em stress acima de 90
- **THEN** o incremento é limitado a no máximo 1
- **AND WHEN** `incStress(val)` é chamado com o piloto em stress acima de 70 (e não acima de 90)
- **THEN** o incremento é limitado a no máximo 2 (era stress acima de 80)
- **AND WHEN** `incStress(val)` é chamado com o piloto em stress acima de 50 (e não acima de 70)
- **THEN** o incremento é limitado a no máximo 3 — faixa nova; antes da revisão, stress entre 50 e 70 não tinha nenhum teto e gatilhos grandes (ex.: dano de aerofólio, colisão) passavam com o valor bruto (ou reduzido pela metade em AGRESSIVO/NORMAL)
