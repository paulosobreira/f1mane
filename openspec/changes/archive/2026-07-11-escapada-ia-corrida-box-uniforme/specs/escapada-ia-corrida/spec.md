## ADDED Requirements

### Requirement: Traçados de escapada da corrida são derivados dos ObjetoEscapada do circuito
`Circuito.gerarEscapeMap()` SHALL popular `pista4Full` e `pista5Full` a partir dos `ObjetoEscapada` presentes em `circuito.getObjetos()`, em vez de listas sempre nulas. Para cada `ObjetoEscapada` com `indiceEntrada >= 0` e `indiceSaida > indiceEntrada`: quando `tracadoOrigem == 1`, os índices `[indiceEntrada, indiceSaida]` de `pista5Full` SHALL ser preenchidos com nós interpolados por comprimento de arco ao longo de `escapada.getPontos()`; quando `tracadoOrigem == 2`, o mesmo SHALL ocorrer em `pista4Full`. Esse mapeamento (1→5, 2→4, não 1→4/2→5) é exigido por `Piloto.mudarTracado`, que só permite retornar de 4 para 2 e de 5 para 1. Índices fora de qualquer zona SHALL permanecer `null`, e `escapeMap` SHALL continuar sempre vazio.

#### Scenario: ObjetoEscapada no traçado 1 popula pista5Full
- **WHEN** o circuito é (re)vetorizado e existe um `ObjetoEscapada` com `tracadoOrigem == 1`, `indiceEntrada == 200` e `indiceSaida == 260`
- **THEN** `circuito.getPista5Full()` tem nós não nulos exatamente nos índices 200 a 260, interpolados ao longo do trajeto de pontos dessa escapada, e nulos em todos os outros índices

#### Scenario: ObjetoEscapada no traçado 2 popula pista4Full
- **WHEN** o circuito é (re)vetorizado e existe um `ObjetoEscapada` com `tracadoOrigem == 2`, `indiceEntrada == 500` e `indiceSaida == 550`
- **THEN** `circuito.getPista4Full()` tem nós não nulos exatamente nos índices 500 a 550, interpolados ao longo do trajeto de pontos dessa escapada, e nulos em todos os outros índices

#### Scenario: Circuito sem ObjetoEscapada mantém as listas totalmente nulas
- **WHEN** o circuito é (re)vetorizado e `circuito.getObjetos()` não contém nenhum `ObjetoEscapada` (ou é vazio/nulo)
- **THEN** `pista4Full` e `pista5Full` continuam do tamanho de `pistaFull`, com todas as posições `null`, como já ocorre hoje

#### Scenario: ObjetoEscapada com trajeto degenerado não gera exceção
- **WHEN** um `ObjetoEscapada` tem `getPontos()` nulo ou com menos de 2 pontos
- **THEN** nenhum nó é gerado para esse objeto (a lista de destino permanece nula nos índices dele), sem lançar exceção durante a vetorização

### Requirement: Só piloto "em risco" recebe o teste de habilidade da escapada; os demais nunca são testados nem escapam
Em `Piloto.processaEscapadaAncoradaAoTracado()`, um único teste de habilidade por `ObjetoEscapada` por volta (ver requisito de cache por volta) SHALL ser realizado, e apenas quando o piloto estiver "em risco" (`modoPilotagem == AGRESSIVO` e `getStress()` acima do limite, ou pneus abaixo de 20% e não `LENTO`) — sem nenhuma tentativa de mudar de traçado antes disso (ver requisito "a lógica de escapada nunca dirige o carro pra longe de uma zona"). O teste ocorre assim que o piloto fica em risco em qualquer ponto dentro da janela de detecção da zona (a mesma janela usada para localizar a `ObjetoEscapada` mais próxima no traçado atual). Um piloto que nunca estiver "em risco" enquanto a zona ainda está ao alcance SHALL NOT ser testado e SHALL NOT escapar por essa zona nesta volta.

#### Scenario: Piloto agressivo e estressado fica em risco dentro de 100 índices
- **WHEN** um piloto tem `modoPilotagem == AGRESSIVO`, `stress` acima do limite, está no traçado 1, e a entrada de um `ObjetoEscapada` com `tracadoOrigem == 1` está 80 índices à frente
- **THEN** o piloto recebe o teste de habilidade nesse ciclo (ou já recebeu, se testado antes)

#### Scenario: Fora da janela de detecção, ainda não recebe o teste
- **WHEN** um piloto agressivo e estressado está no traçado 1 e a entrada do `ObjetoEscapada` mais próximo com `tracadoOrigem == 1` está 150 índices à frente
- **THEN** o piloto ainda não recebe o teste

#### Scenario: Piloto que nunca fica em risco não é testado e não escapa
- **WHEN** um piloto com `modoPilotagem == NORMAL`, `stress` dentro do limite e pneus acima de 20% alcança `indiceEntrada` de um `ObjetoEscapada` no seu traçado atual, sem em nenhum momento anterior ter ficado "em risco"
- **THEN** nenhum teste de habilidade é realizado, e o piloto NÃO escapa por essa zona, permanecendo no traçado 1 ou 2 em que estava

### Requirement: Entrada já passada além de uma pequena tolerância torna a zona inelegível
Se o índice atual do piloto já ultrapassou `indiceEntrada` de uma `ObjetoEscapada` em mais de uma pequena tolerância de nós (cobrindo apenas o avanço de um único ciclo, já que o índice pode avançar mais de 1 por ciclo), essa zona SHALL deixar de ser elegível para forçar a escapada nesse traçado — mesmo que o piloto esteja em algum ponto dentro de `[indiceEntrada, indiceSaida]` (por exemplo por ter mudado para esse traçado no meio da zona, sem relação alguma com a entrada). O piloto só poderá escapar pela PRÓXIMA `ObjetoEscapada` cuja entrada ainda esteja à frente.

#### Scenario: Piloto materializa no meio de uma zona sem ter passado pela entrada não é forçado a escapar
- **WHEN** um piloto muda para o traçado 1 num índice bem depois de `indiceEntrada` (bem além da tolerância de um ciclo) de uma `ObjetoEscapada` com `tracadoOrigem == 1` cujo `indiceSaida` ainda não foi alcançado
- **THEN** o piloto NÃO é forçado a escapar por essa zona, mesmo estando dentro do intervalo `[indiceEntrada, indiceSaida]`

#### Scenario: Salto de um único ciclo sobre a entrada ainda conta
- **WHEN** o índice do piloto avança, num único ciclo, de antes para ligeiramente depois de `indiceEntrada` (dentro da tolerância de um ciclo), ainda no traçado 1 ou 2 correspondente
- **THEN** o piloto ainda é considerado como tendo alcançado a entrada, podendo escapar normalmente

### Requirement: A lógica de escapada nunca dirige o carro pra longe de uma zona
`Piloto.processaEscapadaAncoradaAoTracado()` SHALL NOT tentar `mudarTracado(0)` (ou qualquer outra mudança de traçado) só porque existe uma `ObjetoEscapada` à frente no traçado atual do piloto — a decisão de sair do traçado 1 ou 2 de volta para o 0 é SHALL ser feita exclusivamente pela lógica geral de condução (`processaMudarTracado()`/`processaIAnovoIndex()` para pilotos de IA) ou pelo próprio jogador, em modo manual. A lógica de escapada é puramente reativa: a partir de onde o piloto já está no momento em que alcança `indiceEntrada`, decide apenas se ele escapa ou não (ver requisitos seguintes) — nunca movimenta o carro lateralmente por iniciativa própria antes disso.

#### Scenario: Piloto derrapou (gatilho cego) para um traçado com escapada à frente não é trazido de volta automaticamente
- **WHEN** um piloto muda de traçado 0 para 1 ou 2 pelo gatilho cego (perda de controle) e, num ciclo seguinte, existe uma `ObjetoEscapada` a mais de 40 índices de distância no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` relacionada à escapada é feita — o piloto permanece no traçado em que a derrapagem o colocou até que a lógica geral de condução (ou o jogador) decida mudar

#### Scenario: Piloto normal cruzando um traçado com escapada à frente não é desviado
- **WHEN** um piloto não comprometido está dentro da janela de 100 índices da entrada de uma `ObjetoEscapada` no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` é feita nesse ciclo; se o piloto ainda estiver no mesmo traçado ao alcançar `indiceEntrada`, o desfecho é decidido pelos requisitos de comprometimento/gatilho desta spec

### Requirement: Escapada é executada ao alcançar a entrada da zona, só para quem está marcado pra escapar
Quando um piloto "em risco" que falhou no teste de habilidade desta zona (ou está sujeito à variável global de teste) alcança `indiceEntrada` da zona-alvo ainda no traçado 1 ou 2 correspondente, `Piloto` SHALL forçar a mudança para o traçado de escapada correspondente (`mudarTracado(5, true)` quando `tracadoOrigem == 1`; `mudarTracado(4, true)` quando `tracadoOrigem == 2`) — exceto nos casos cobertos pelo requisito seguinte (piloto em modo LENTO, ou teste de habilidade bem-sucedido). Um piloto que nunca ficou "em risco" NÃO é forçado a escapar (ver requisito anterior).

#### Scenario: Comprometido por agressividade executa a escapada na entrada
- **WHEN** um piloto "em risco" (agressividade+stress, ou pneus baixos) que falhou no teste de habilidade desta zona alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 2`, ainda no traçado 2
- **THEN** o piloto muda (forçado) para o traçado 4

### Requirement: Piloto em modo LENTO nunca escapa; um piloto "em risco" (exceto jogador humano em modo manual) tem uma última chance via teste de habilidade
No gatilho da escapada (o momento em que `Piloto` forçaria a mudança para o traçado de fuga), se `modoPilotagem == LENTO`, o piloto SHALL NOT escapar por essa zona — independentemente de estresse, agressividade ou da variável global de teste (LENTO também impede o piloto de sequer ser testado). Para um piloto "em risco" que NÃO seja um jogador humano em modo de controle manual (`isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())`), `Piloto` SHALL realizar um teste de habilidade (`testeHabilidadePiloto()`); em caso de sucesso, o piloto SHALL mudar para `modoPilotagem == LENTO` e NÃO escapar por essa zona (permanece no traçado 1 ou 2 correspondente). Só em caso de falha no teste (ou por ser jogador humano em modo manual "em risco", que não recebe esse teste automático) é que a escapada é de fato forçada.

#### Scenario: Piloto em modo LENTO nunca escapa, mesmo com stress acima do limite
- **WHEN** um piloto com `modoPilotagem == LENTO` e stress acima do limite alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual
- **THEN** o piloto não escapa e continua no traçado 1 ou 2 em que estava

#### Scenario: Teste de habilidade bem-sucedido evita a escapada
- **WHEN** um piloto "em risco" de IA (ou jogador humano em modo automático), não em modo LENTO, e o teste de habilidade desta zona é bem-sucedido
- **THEN** o piloto muda para `modoPilotagem == LENTO`, não escapa por essa zona, e continua no traçado 1 ou 2 em que estava

#### Scenario: Teste de habilidade sem sucesso força a escapada normalmente
- **WHEN** um piloto "em risco" de IA (ou jogador humano em modo automático), não em modo LENTO, e o teste de habilidade desta zona falha
- **THEN** o piloto é forçado a escapar normalmente ao alcançar `indiceEntrada`, como já descrito no requisito anterior

#### Scenario: Jogador humano em modo manual "em risco" não recebe o teste de habilidade automático
- **WHEN** um jogador humano "em risco" com `Global.CONTROLE_MANUAL` ativo, não em modo LENTO, alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual
- **THEN** nenhum teste de habilidade é realizado, e a escapada é forçada diretamente — a responsabilidade de mudar para LENTO pra evitar a escapada é do próprio jogador

#### Scenario: Jogador humano em modo manual que já está em LENTO continua não escapando
- **WHEN** um jogador humano em modo manual, já com `modoPilotagem == LENTO` (definido pelo próprio jogador), alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual
- **THEN** o piloto não escapa, pela mesma regra de LENTO que vale pra qualquer piloto

### Requirement: Retorno da escapada usa um teste de habilidade e volta sempre ao traçado de origem
Enquanto o piloto estiver no traçado de fuga (4 ou 5), `Piloto` SHALL monitorar a `ObjetoEscapada` ativa nesse traçado (aquela cujo intervalo `[indiceEntrada, indiceSaida]` cobre o índice atual do piloto) e, quando a distância até `indiceSaida` dessa zona for 100 índices de nó ou menos, SHALL realizar a cada ciclo um teste de habilidade do piloto (`testeHabilidadePiloto()`) para decidir se ele já consegue voltar ao traçado normal de pilotagem. Em caso de sucesso, `Piloto` SHALL forçar a mudança de volta para o MESMO traçado de origem (1 ou 2) de onde a escapada começou — nunca para o outro traçado lateral nem diretamente para o traçado 0 (`mudarTracado(2, true)` quando o traçado de fuga é 4; `mudarTracado(1, true)` quando o traçado de fuga é 5). Em caso de falha, o piloto SHALL continuar no traçado de fuga e repetir o teste no próximo ciclo.

#### Scenario: Retorno bem-sucedido do traçado de fuga 5 volta para o traçado de origem 1
- **WHEN** um piloto está no traçado 5, a 100 índices de nó ou menos do `indiceSaida` da `ObjetoEscapada` ativa (`tracadoOrigem == 1`), e o teste de habilidade é bem-sucedido nesse ciclo
- **THEN** o piloto muda (forçado) para o traçado 1 — nunca para o traçado 2

#### Scenario: Retorno bem-sucedido do traçado de fuga 4 volta para o traçado de origem 2
- **WHEN** um piloto está no traçado 4, a 100 índices de nó ou menos do `indiceSaida` da `ObjetoEscapada` ativa (`tracadoOrigem == 2`), e o teste de habilidade é bem-sucedido nesse ciclo
- **THEN** o piloto muda (forçado) para o traçado 2 — nunca para o traçado 1

#### Scenario: Teste de habilidade sem sucesso mantém o piloto no traçado de fuga
- **WHEN** um piloto está no traçado de fuga dentro da janela de 100 índices do fim da zona, mas o teste de habilidade falha nesse ciclo
- **THEN** o piloto continua no traçado de fuga, e o teste é repetido no próximo ciclo

#### Scenario: Fora da janela de 100 índices do fim, não tenta voltar
- **WHEN** um piloto está no traçado de fuga, mas a mais de 100 índices de nó do `indiceSaida` da zona ativa
- **THEN** nenhum teste de habilidade nem tentativa de `mudarTracado` relacionados ao retorno são feitos nesse ciclo

### Requirement: Ultrapassar levemente o fim da zona não abandona a tentativa de retorno
Se o índice atual do piloto já ultrapassou `indiceSaida` de uma `ObjetoEscapada` ativa no seu traçado de fuga em até uma tolerância de nós (cobrindo o avanço de um único ciclo, reduzido pelo multiplicador de ganho de 0.4 do traçado de fuga, mas ainda podendo ser maior que 1 por ciclo), o piloto SHALL continuar recebendo o teste de habilidade de retorno normalmente, em vez de a zona deixar de ser encontrada. Sem essa tolerância, um piloto cujo teste de habilidade falhasse repetidamente durante toda a janela de retorno poderia ter o índice ultrapassar `indiceSaida` antes de um sucesso, ficando preso no traçado de fuga indefinidamente (nada mais no jogo transiciona um piloto de volta do traçado 4/5 fora dessa lógica).

#### Scenario: Salto de ciclo além do fim ainda tenta voltar
- **WHEN** o índice do piloto avança, num único ciclo, de dentro da janela de retorno para até uma pequena tolerância além de `indiceSaida`, ainda no traçado de fuga correspondente
- **THEN** o piloto ainda recebe o teste de habilidade de retorno nesse ciclo, podendo voltar ao traçado de origem normalmente

#### Scenario: Muito além do fim (fora da tolerância), a zona deixa de ser encontrada
- **WHEN** o índice do piloto está bem além de `indiceSaida` de uma zona (fora da tolerância de salto de ciclo)
- **THEN** essa zona não é mais considerada ativa nesse traçado de fuga, e nenhum teste de habilidade de retorno relacionado a ela é feito

### Requirement: Velocidade e modo reduzidos só enquanto literalmente no traçado de fuga, restaurados ao normal ao voltar
Enquanto (e só enquanto) `getTracado()` for 4 ou 5, `Piloto.processaEscapadaDaPista()` SHALL multiplicar o ganho por 0.8, forçar `carro.setGiro(Carro.GIRO_MIN_VAL)` e `setModoPilotagem(LENTO)`. A redução SHALL NOT se estender à janela de retorno (animação de troca de traçado de volta ao traçado de origem) — assim que o traçado deixa de ser 4 ou 5, `modoPilotagem` e o giro SHALL ser restaurados aos valores de imediatamente antes de entrar no traçado de fuga (não simplesmente resetados para um valor fixo, e não deixados travados em `LENTO`/`GIRO_MIN_VAL` indefinidamente).

#### Scenario: Giro e modo são travados durante a escapada
- **WHEN** um piloto está no traçado 4 ou 5
- **THEN** `carro.getGiro()` é `Carro.GIRO_MIN_VAL` e `getModoPilotagem()` é `LENTO` nesse ciclo, além do ganho vir multiplicado por 0.8

#### Scenario: Giro e modo NÃO são reduzidos durante a janela de retorno (traçado já normal)
- **WHEN** um piloto tem `tracadoAntigo` 4 ou 5 e ainda está animando a troca de volta (`getIndiceTracado() > 0`), mas o traçado atual já é 0, 1 ou 2
- **THEN** o ganho não é multiplicado, e `carro.getGiro()`/`getModoPilotagem()` não são alterados por essa lógica

#### Scenario: Modo e giro são restaurados ao normal assim que o piloto sai do traçado de fuga
- **WHEN** um piloto estava no traçado 4 ou 5 (modo e giro reduzidos) e, num ciclo seguinte, o traçado deixa de ser 4 ou 5
- **THEN** `modoPilotagem` e o giro voltam exatamente aos valores que tinham no ciclo imediatamente anterior a entrar no traçado de fuga — nunca ficam travados em `LENTO`/`GIRO_MIN_VAL`, mesmo que outra lógica do jogo (ex.: com `colisao != null`) não rode seu próprio reset nesse ciclo

### Requirement: Ganho base usa sempre a escada de curva baixa no traçado de fuga, independente do tipo do nó da pista principal
`Piloto.calculaModificadorPrincipal()` SHALL tratar o piloto como estando em curva baixa (a escada de ganho mais lenta do jogo, 10-20) sempre que `getTracado()` for 4 ou 5 — mesmo que o nó da pista PRINCIPAL no mesmo índice (usado por `getNoAtual()` independente do traçado lateral) seja do tipo reta ou curva alta. O multiplicador de 0.8 (requisito anterior) SHALL continuar sendo aplicado em cima desse ganho já reduzido.

#### Scenario: Traçado de fuga sobre um trecho reto da pista principal ainda usa a escada de curva baixa
- **WHEN** um piloto está no traçado 4 ou 5, e o nó da pista principal no mesmo índice é do tipo `No.RETA`
- **THEN** `calculaModificadorPrincipal()` retorna um valor da escada de curva baixa (10-20), não da escada de reta (30-50)

#### Scenario: Fora do traçado de fuga, o tipo real do nó continua valendo
- **WHEN** um piloto está no traçado 0, 1 ou 2, e o nó atual é do tipo `No.RETA`
- **THEN** `calculaModificadorPrincipal()` usa a escada de reta normalmente, sem alteração

### Requirement: Variável global força a escapada de forma determinística, sem RNG nem exceções
`Global` SHALL expor uma flag booleana (`FORCAR_ESCAPADA_TESTE`, default `false`) que, quando `true`, força qualquer piloto no traçado 1 ou 2 a escapar ao alcançar `indiceEntrada` de um `ObjetoEscapada` no seu traçado atual, dentro da janela de 100 índices — sem depender de `modoPilotagem`/`getStress()`/pneus, sem o teste de habilidade preventivo nem o do gatilho, e sobrepondo até a exceção de `modoPilotagem == LENTO`. A exigência de traçado/posição (estar no traçado 1 ou 2, dentro da janela de 100 índices da entrada) SHALL continuar valendo — a flag não teleporta nem ignora geometria.

#### Scenario: Flag ativa compromete piloto normal e não estressado
- **WHEN** `Global.FORCAR_ESCAPADA_TESTE` é `true` e um piloto com `modoPilotagem == NORMAL` e `stress` baixo alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 1`, ainda no traçado 1
- **THEN** o piloto escapa, sem nenhum teste de habilidade

#### Scenario: Flag ativa escapa mesmo com o piloto em modo LENTO
- **WHEN** `Global.FORCAR_ESCAPADA_TESTE` é `true` e um piloto com `modoPilotagem == LENTO` alcança `indiceEntrada` de um `ObjetoEscapada` no seu traçado atual
- **THEN** o piloto escapa mesmo assim — a flag sobrepõe até a exceção de LENTO

#### Scenario: Flag ativa não dispensa a exigência de posição
- **WHEN** `Global.FORCAR_ESCAPADA_TESTE` é `true` e um piloto está no traçado 0, ou a mais de 100 índices de qualquer entrada de escapada no seu traçado
- **THEN** o piloto não escapa nesse ciclo

### Requirement: A mecânica usa a mesma regra em qualquer volta, incluindo a volta 1
`Piloto.processaEscapadaAncoradaAoTracado()` e `Piloto.processaSaidaDaEscapada()` SHALL NOT ter nenhum tratamento diferente por número de volta — o teste de habilidade preventivo e o gatilho de escapada/retorno se comportam de forma idêntica na volta 1 e em qualquer outra volta (decisão explícita do usuário — ver D13 do design.md, que documenta uma tentativa revertida de desligar a mecânica na volta 1).

#### Scenario: Volta 1 escapa normalmente, sem tratamento especial
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto agressivo e estressado alcança a entrada de uma `ObjetoEscapada`
- **THEN** o piloto escapa, exatamente como aconteceria em qualquer outra volta

#### Scenario: Volta 1 processa o retorno do traçado de fuga normalmente
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto está no traçado 4 ou 5 dentro da janela de retorno, com teste de habilidade bem-sucedido
- **THEN** o piloto volta ao traçado de origem, exatamente como aconteceria em qualquer outra volta
