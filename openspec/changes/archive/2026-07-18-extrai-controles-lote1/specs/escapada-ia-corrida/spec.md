## MODIFIED Requirements

### Requirement: Só piloto "em risco" recebe o teste de habilidade da escapada; os demais nunca são testados nem escapam
Em `ControleEscapada.processaEscapadaAncoradaAoTracado()`, o par de testes abaixo (teste 1, e teste 2 só se o 1 não marcar) SHALL rodar **no máximo uma vez por `ObjetoEscapada` por piloto por volta** — não uma vez por causa de risco, e sim uma vez para a zona inteira, qualquer que seja o desfecho:

1. **Teste de stress**: `getStress() > Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA` (90) E `!testeHabilidadePilotoCarro()`. Não exige `modoPilotagem == AGRESSIVO` nem exclui `LENTO` — qualquer modo de pilotagem satisfaz a pré-condição, desde que o stress esteja acima do limite. Se verdadeiro, o piloto é marcado para escapar nesta zona.
2. **Teste de pneus** (só avaliado se o teste 1 não marcou): `carro.getPorcentagemDesgastePneus() < 30` E `getStress() >= Global.LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS` (70, menor que o limite do teste de stress) E `!testeHabilidadePilotoFreios()`. Não exclui `LENTO`. Se verdadeiro, o piloto é marcado para escapar nesta zona.

O gate que garante o "no máximo uma vez" é o cache por zona por volta (`resultadoTesteEscapadaPorZonaNestaVolta`, já existente): a primeira vez que a zona entra na janela de detecção (distância até 150 índices, ver requisito de janela), se ainda não há nenhum resultado registrado para ela nesta volta, os dois testes rodam (2, condicional ao 1) e o desfecho (marcado OU não marcado) SHALL ser gravado no cache. A partir daí — em qualquer ciclo seguinte desta mesma volta, inclusive mais perto da entrada — a zona SHALL NOT ser testada de novo, **mesmo que o resultado gravado tenha sido "não marcado" (o piloto passou nos dois testes) e o piloto continue satisfazendo as mesmas pré-condições de risco**. Só a virada de volta (que limpa o cache) libera um novo par de testes para essa zona.

Cada teste SHALL NOT chamar seu teste de habilidade (`testeHabilidadePilotoCarro()`/`testeHabilidadePilotoFreios()`, ambos dependentes de RNG) quando sua pré-condição de risco (stress, ou pneus+stress) já é falsa — preservando o caráter reativo e determinístico da mecânica. Um piloto para quem nenhum dos dois testes se aplica (nenhuma pré-condição de risco satisfeita) em nenhum ponto dentro da janela de detecção SHALL NOT ser testado e SHALL NOT escapar por essa zona nesta volta.

#### Scenario: Piloto estressado é avaliado pelo teste 1 dentro de 150 índices, em qualquer modo de pilotagem
- **WHEN** um piloto tem `stress` acima de 90 (em qualquer `modoPilotagem`, incluindo `NORMAL` ou `LENTO`), está no traçado 1, e a entrada de um `ObjetoEscapada` com `tracadoOrigem == 1` está 120 índices à frente
- **THEN** o teste 1 (stress) é avaliado nesse ciclo (ou já foi avaliado antes, se a zona já estava em cache)

#### Scenario: Fora da janela de detecção, ainda não recebe nenhum teste
- **WHEN** um piloto estressado está no traçado 1 e a entrada do `ObjetoEscapada` mais próximo com `tracadoOrigem == 1` está 200 índices à frente
- **THEN** nenhum dos dois testes é avaliado ainda

#### Scenario: Piloto que nunca satisfaz nenhuma pré-condição de risco não é testado e não escapa
- **WHEN** um piloto com `stress` dentro do limite (≤ 90) e pneus acima de 30% alcança `indiceEntrada` de um `ObjetoEscapada` no seu traçado atual, sem em nenhum momento anterior ter satisfeito a pré-condição do teste 1 nem do teste 2
- **THEN** nenhum teste de habilidade é realizado, e o piloto NÃO escapa por essa zona, permanecendo no traçado 1 ou 2 em que estava

#### Scenario: Teste 1 não marca, teste 2 é avaliado em sequência
- **WHEN** um piloto tem `stress` entre 70 e 90 (satisfaz a pré-condição do teste 2, não a do teste 1, que exige `stress > 90`), com pneus abaixo de 30%
- **THEN** o teste 1 não consome RNG (pré-condição falsa), e o teste 2 é avaliado nesse mesmo ciclo

#### Scenario: Teste 1 já marca o piloto; teste 2 não é avaliado
- **WHEN** um piloto satisfaz a pré-condição do teste 1 (`stress > 90`) e `testeHabilidadePilotoCarro()` falha nesse teste
- **THEN** o piloto é marcado para escapar por causa do teste 1, e o teste 2 (pneus) NÃO é avaliado nesse ciclo, mesmo que os pneus também estejam abaixo de 30% e o stress também satisfaça o limite do teste 2

#### Scenario: Piloto que passa nos dois testes não é retestado mais perto da entrada, mesmo continuando em risco
- **WHEN** um piloto estressado (acima de 90) é avaliado pelos testes 1 e 2 ao entrar na janela de 150 índices de uma `ObjetoEscapada`, não é marcado por nenhum dos dois (passou), e continua com `stress` acima do limite nos ciclos seguintes, cada vez mais perto de `indiceEntrada` (ex.: a 100, depois a 20 índices)
- **THEN** nenhum novo teste (1 ou 2) é realizado para essa zona em nenhum desses ciclos seguintes — o resultado "não marcado" gravado no primeiro teste vale pelo resto da volta, e o piloto passa pela entrada sem escapar

#### Scenario: Cache registra o desfecho independente de qual teste (1 ou 2) decidiu, ou de nenhum ter se aplicado
- **WHEN** uma `ObjetoEscapada` é avaliada pela primeira vez nesta volta para um piloto, qualquer que seja o desfecho (marcado pelo teste 1, marcado pelo teste 2, ou não marcado por nenhum dos dois, inclusive quando nenhuma pré-condição de risco nunca se aplicou)
- **THEN** esse desfecho é gravado no cache da zona para esta volta, e nenhum dos dois testes volta a rodar para essa zona neste piloto até a volta mudar

### Requirement: A lógica de escapada nunca dirige o carro pra longe de uma zona
`ControleEscapada.processaEscapadaAncoradaAoTracado()` SHALL NOT tentar `mudarTracado(0)` (ou qualquer outra mudança de traçado) só porque existe uma `ObjetoEscapada` à frente no traçado atual do piloto — a decisão de sair do traçado 1 ou 2 de volta para o 0 é SHALL ser feita exclusivamente pela lógica geral de condução (`processaMudarTracado()`/`processaIAnovoIndex()` para pilotos de IA) ou pelo próprio jogador, em modo manual. A lógica de escapada é puramente reativa: a partir de onde o piloto já está no momento em que alcança `indiceEntrada`, decide apenas se ele escapa ou não (ver requisitos seguintes) — nunca movimenta o carro lateralmente por iniciativa própria antes disso.

#### Scenario: Piloto que derrapou para um traçado com escapada à frente não é trazido de volta automaticamente
- **WHEN** um piloto muda de traçado 0 para 1 ou 2 pela mecânica de derrapagem (ver spec `derrapagem-piloto`) e, num ciclo seguinte, existe uma `ObjetoEscapada` a mais de 150 índices de distância no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` relacionada à escapada é feita — o piloto permanece no traçado em que a derrapagem o colocou até que a lógica geral de condução (ou o jogador) decida mudar

#### Scenario: Piloto normal cruzando um traçado com escapada à frente não é desviado
- **WHEN** um piloto não comprometido está dentro da janela de 150 índices da entrada de uma `ObjetoEscapada` no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` é feita nesse ciclo; se o piloto ainda estiver no mesmo traçado ao alcançar `indiceEntrada`, o desfecho é decidido pelos requisitos de comprometimento/gatilho desta spec

### Requirement: Velocidade e modo reduzidos só enquanto literalmente no traçado de fuga, restaurados ao normal ao voltar
Enquanto (e só enquanto) `getTracado()` for 4 ou 5, `ControleEscapada.processaEscapadaDaPista()` SHALL multiplicar o ganho por 0.8, forçar `carro.setGiro(Carro.GIRO_MIN_VAL)` e `setModoPilotagem(LENTO)`. A redução SHALL NOT se estender à janela de retorno (animação de troca de traçado de volta ao traçado de origem) — assim que o traçado deixa de ser 4 ou 5, `modoPilotagem` e o giro SHALL ser restaurados aos valores de imediatamente antes de entrar no traçado de fuga (não simplesmente resetados para um valor fixo, e não deixados travados em `LENTO`/`GIRO_MIN_VAL` indefinidamente).

#### Scenario: Giro e modo são travados durante a escapada
- **WHEN** um piloto está no traçado 4 ou 5
- **THEN** `carro.getGiro()` é `Carro.GIRO_MIN_VAL` e `getModoPilotagem()` é `LENTO` nesse ciclo, além do ganho vir multiplicado por 0.8

#### Scenario: Giro e modo NÃO são reduzidos durante a janela de retorno (traçado já normal)
- **WHEN** um piloto tem `tracadoAntigo` 4 ou 5 e ainda está animando a troca de volta (`getIndiceTracado() > 0`), mas o traçado atual já é 0, 1 ou 2
- **THEN** o ganho não é multiplicado, e `carro.getGiro()`/`getModoPilotagem()` não são alterados por essa lógica

#### Scenario: Modo e giro são restaurados ao normal assim que o piloto sai do traçado de fuga
- **WHEN** um piloto estava no traçado 4 ou 5 (modo e giro reduzidos) e, num ciclo seguinte, o traçado deixa de ser 4 ou 5
- **THEN** `modoPilotagem` e o giro voltam exatamente aos valores que tinham no ciclo imediatamente anterior a entrar no traçado de fuga — nunca ficam travados em `LENTO`/`GIRO_MIN_VAL`, mesmo que outra lógica do jogo (ex.: com `colisao != null`) não rode seu próprio reset nesse ciclo

### Requirement: Piloto indo pro box é excluído da escapada desde a decisão
`ControleEscapada.processaEscapadaDaPista()` SHALL retornar sem processar nenhum teste ou execução de escapada quando o piloto tiver decidido ir pro box (`isBox() == true`), não apenas quando já estiver fisicamente dentro da pit lane (`getPtosBox() != 0`, condição já existente e mantida). Se o piloto já estava marcado pela escapada ancorada (`impedidoDeMudarTracadoPorEscapada == true`) no momento em que decide ir pro box, essa trava SHALL ser liberada nesse mesmo ponto (via `piloto.setImpedidoDeMudarTracadoPorEscapada(false)`), evitando que o piloto fique permanentemente impedido de mudar de traçado (inclusive de entrar no box) pelo resto da corrida.

#### Scenario: Piloto que decide ir pro box deixa de ser testável pela escapada, mesmo ainda na pista principal
- **WHEN** um piloto com `stress` acima de 90 decide ir pro box (`isBox()` passa a `true`) enquanto ainda está na pista principal, antes de `getPtosBox()` deixar de ser zero, e entra na janela de detecção de uma `ObjetoEscapada`
- **THEN** nenhum dos dois testes de escapada é avaliado, e o piloto não é marcado nem forçado a escapar por essa zona

#### Scenario: Decidir ir pro box depois de já marcado libera a trava de mudança de traçado
- **WHEN** um piloto já está marcado pela escapada ancorada (`impedidoDeMudarTracadoPorEscapada == true`, ainda não cumpriu a escapada) e decide ir pro box antes de alcançar a entrada da zona
- **THEN** a trava é liberada nesse mesmo ciclo, e o piloto volta a poder mudar de traçado normalmente (inclusive ser posicionado no traçado do box pelas camadas de entrada — ver spec `tracado-safe-lane-change`)

#### Scenario: Piloto já fisicamente na pit lane continua excluído, como antes
- **WHEN** um piloto tem `getPtosBox() != 0` (já dentro da pit lane)
- **THEN** `processaEscapadaDaPista()` continua retornando sem processar nada, exatamente como já acontecia antes desta mudança

### Requirement: A mecânica usa a mesma regra em qualquer volta, incluindo a volta 1
`ControleEscapada.processaEscapadaAncoradaAoTracado()` e `ControleEscapada.processaSaidaDaEscapada()` SHALL NOT ter nenhum tratamento diferente por número de volta — o teste de habilidade preventivo e o gatilho de escapada/retorno se comportam de forma idêntica na volta 1 e em qualquer outra volta (decisão explícita do usuário — ver D13 do design.md, que documenta uma tentativa revertida de desligar a mecânica na volta 1).

#### Scenario: Volta 1 escapa normalmente, sem tratamento especial
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto agressivo e estressado alcança a entrada de uma `ObjetoEscapada`
- **THEN** o piloto escapa, exatamente como aconteceria em qualquer outra volta

#### Scenario: Volta 1 processa o retorno do traçado de fuga normalmente
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto está no traçado 4 ou 5 dentro da janela de retorno, com teste de habilidade bem-sucedido
- **THEN** o piloto volta ao traçado de origem, exatamente como aconteceria em qualquer outra volta
