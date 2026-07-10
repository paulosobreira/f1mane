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

### Requirement: Piloto agressivo e muito estressado se compromete a escapar perto da entrada
Em `Piloto.processaEscapadaDaPista()`, quando `modoPilotagem == AGRESSIVO` e `getStress() > getValorLimiteStressePararErrarCurva()` (ou a variável global de teste estiver ativa — ver requisito de variável global), e o piloto está no traçado 1 ou 2, e existe um `ObjetoEscapada` com `tracadoOrigem` igual ao traçado atual do piloto cuja `indiceEntrada` está a 40 índices de nó ou menos à frente do índice atual do piloto (`indiceEntrada - noAtual.getIndex()` entre 0 e 40, inclusive), o piloto SHALL se comprometer a escapar nessa zona — sem tentar mudar para o traçado 0 nesse meio tempo.

#### Scenario: Piloto agressivo e estressado se compromete dentro de 40 índices
- **WHEN** um piloto tem `modoPilotagem == AGRESSIVO`, `stress` acima do limite, está no traçado 1, e a entrada de um `ObjetoEscapada` com `tracadoOrigem == 1` está 30 índices à frente
- **THEN** o piloto fica comprometido a escapar nessa zona e não tenta mudar para o traçado 0 antes de alcançar a entrada

#### Scenario: Fora da janela de 40 índices, ainda não se compromete
- **WHEN** um piloto agressivo e estressado está no traçado 1 e a entrada do `ObjetoEscapada` mais próximo com `tracadoOrigem == 1` está 80 índices à frente
- **THEN** o piloto ainda não se compromete a escapar (cai no comportamento de monitoramento/desvio do requisito seguinte)

### Requirement: Entrada já passada além de uma pequena tolerância torna a zona inelegível
Se o índice atual do piloto já ultrapassou `indiceEntrada` de uma `ObjetoEscapada` em mais de uma pequena tolerância de nós (cobrindo apenas o avanço de um único ciclo, já que o índice pode avançar mais de 1 por ciclo), essa zona SHALL deixar de ser elegível para forçar a escapada nesse traçado — mesmo que o piloto esteja em algum ponto dentro de `[indiceEntrada, indiceSaida]` (por exemplo por ter mudado para esse traçado no meio da zona, sem relação alguma com a entrada). O piloto só poderá escapar pela PRÓXIMA `ObjetoEscapada` cuja entrada ainda esteja à frente.

#### Scenario: Piloto materializa no meio de uma zona sem ter passado pela entrada não é forçado a escapar
- **WHEN** um piloto muda para o traçado 1 num índice bem depois de `indiceEntrada` (bem além da tolerância de um ciclo) de uma `ObjetoEscapada` com `tracadoOrigem == 1` cujo `indiceSaida` ainda não foi alcançado
- **THEN** o piloto NÃO é forçado a escapar por essa zona, mesmo estando dentro do intervalo `[indiceEntrada, indiceSaida]`

#### Scenario: Salto de um único ciclo sobre a entrada ainda conta
- **WHEN** o índice do piloto avança, num único ciclo, de antes para ligeiramente depois de `indiceEntrada` (dentro da tolerância de um ciclo), ainda no traçado 1 ou 2 correspondente
- **THEN** o piloto ainda é considerado como tendo alcançado a entrada, podendo escapar normalmente

### Requirement: Pilotos monitoram e tentam evitar zonas de escapada no seu traçado
Todo piloto controlado por IA que esteja no traçado 1 ou 2, e não esteja comprometido a escapar (ver requisito anterior), SHALL monitorar a próxima `ObjetoEscapada` cujo `tracadoOrigem` seja o traçado atual do piloto. Quando a distância até `indiceEntrada` dessa escapada for 100 índices de nó ou menos, o piloto SHALL tentar, a cada ciclo, mudar para o traçado 0 (`mudarTracado(0)`, sem forçar, respeitando as regras normais de cooldown/colisão). Caso consiga mudar para o traçado 0 antes de alcançar `indiceEntrada`, deixa de monitorar essa zona. Caso alcance `indiceEntrada` ainda no traçado 1 ou 2 original, o piloto também se compromete a escapar nessa zona.

#### Scenario: Piloto consegue desviar a tempo
- **WHEN** um piloto não comprometido está a 100 índices da entrada de um `ObjetoEscapada` no seu traçado atual, e `mudarTracado(0)` é bem-sucedido antes do piloto alcançar a entrada
- **THEN** o piloto segue no traçado 0 e não escapa por essa zona

#### Scenario: Piloto não consegue desviar e acaba escapando
- **WHEN** um piloto não comprometido está dentro da janela de 100 índices de uma escapada no seu traçado atual, tenta mudar para o traçado 0 repetidamente, mas todas as tentativas falham (cooldown ou colisão) até o índice atual alcançar `indiceEntrada`
- **THEN** o piloto se compromete a escapar nessa zona no exato momento em que alcança `indiceEntrada`

#### Scenario: Fora de qualquer janela de escapada, nenhuma tentativa é feita
- **WHEN** um piloto está no traçado 1 ou 2 e não há nenhum `ObjetoEscapada` com `tracadoOrigem` igual ao seu traçado dentro de 100 índices à frente
- **THEN** nenhuma tentativa de `mudarTracado(0)` relacionada a escapada é feita nesse ciclo

### Requirement: Escapada é executada ao alcançar a entrada da zona
Quando um piloto comprometido a escapar (por agressividade+stress, pela variável global de teste, ou por ter falhado em desviar) alcança `indiceEntrada` da zona-alvo ainda no traçado 1 ou 2 correspondente, `Piloto` SHALL forçar a mudança para o traçado de escapada correspondente (`mudarTracado(5, true)` quando `tracadoOrigem == 1`; `mudarTracado(4, true)` quando `tracadoOrigem == 2`) — exceto nos casos cobertos pelo requisito seguinte (piloto em modo LENTO, ou teste de habilidade bem-sucedido no gatilho).

#### Scenario: Comprometido por agressividade executa a escapada na entrada
- **WHEN** um piloto comprometido por agressividade+stress alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 2`, ainda no traçado 2
- **THEN** o piloto muda (forçado) para o traçado 4

#### Scenario: Comprometido por falha de desvio executa a escapada na entrada
- **WHEN** um piloto que tentou e falhou em desviar alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 1`, ainda no traçado 1
- **THEN** o piloto muda (forçado) para o traçado 5

### Requirement: Piloto em modo LENTO nunca escapa; qualquer outro (exceto jogador humano em modo manual) tem uma última chance via teste de habilidade
No gatilho da escapada (o momento em que `Piloto` forçaria a mudança para o traçado de fuga), se `modoPilotagem == LENTO`, o piloto SHALL NOT escapar por essa zona — independentemente de estresse, agressividade ou da variável global de teste. Caso contrário, e caso o piloto NÃO seja um jogador humano em modo de controle manual (`isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())`), `Piloto` SHALL realizar um teste de habilidade (`testeHabilidadePiloto()`); em caso de sucesso, o piloto SHALL mudar para `modoPilotagem == LENTO` e NÃO escapar por essa zona (permanece no traçado 1 ou 2 correspondente). Só em caso de falha no teste (ou por ser jogador humano em modo manual, que não recebe esse teste automático) é que a escapada é de fato forçada.

#### Scenario: Piloto em modo LENTO nunca escapa, mesmo com stress acima do limite
- **WHEN** um piloto com `modoPilotagem == LENTO` e stress acima do limite alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual
- **THEN** o piloto não escapa e continua no traçado 1 ou 2 em que estava

#### Scenario: Teste de habilidade bem-sucedido no gatilho evita a escapada
- **WHEN** um piloto de IA (ou jogador humano em modo automático), não em modo LENTO, alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual e o teste de habilidade é bem-sucedido nesse ciclo
- **THEN** o piloto muda para `modoPilotagem == LENTO`, não escapa por essa zona, e continua no traçado 1 ou 2 em que estava

#### Scenario: Teste de habilidade sem sucesso força a escapada normalmente
- **WHEN** um piloto de IA (ou jogador humano em modo automático), não em modo LENTO, alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual e o teste de habilidade falha nesse ciclo
- **THEN** o piloto é forçado a escapar normalmente, como já descrito no requisito anterior

#### Scenario: Jogador humano em modo manual não recebe o teste de habilidade automático
- **WHEN** um jogador humano com `Global.CONTROLE_MANUAL` ativo, não em modo LENTO, alcança `indiceEntrada` de uma `ObjetoEscapada` no seu traçado atual
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

### Requirement: Velocidade e modo reduzidos só enquanto literalmente no traçado de fuga, restaurados ao normal ao voltar
Enquanto (e só enquanto) `getTracado()` for 4 ou 5, `Piloto.processaEscapadaDaPista()` SHALL multiplicar o ganho por 0.4, forçar `carro.setGiro(Carro.GIRO_MIN_VAL)` e `setModoPilotagem(LENTO)`. A redução SHALL NOT se estender à janela de retorno (animação de troca de traçado de volta ao traçado de origem) — assim que o traçado deixa de ser 4 ou 5, `modoPilotagem` e o giro SHALL ser restaurados aos valores de imediatamente antes de entrar no traçado de fuga (não simplesmente resetados para um valor fixo, e não deixados travados em `LENTO`/`GIRO_MIN_VAL` indefinidamente).

#### Scenario: Giro e modo são travados durante a escapada
- **WHEN** um piloto está no traçado 4 ou 5
- **THEN** `carro.getGiro()` é `Carro.GIRO_MIN_VAL` e `getModoPilotagem()` é `LENTO` nesse ciclo, além do ganho vir multiplicado por 0.4

#### Scenario: Giro e modo NÃO são reduzidos durante a janela de retorno (traçado já normal)
- **WHEN** um piloto tem `tracadoAntigo` 4 ou 5 e ainda está animando a troca de volta (`getIndiceTracado() > 0`), mas o traçado atual já é 0, 1 ou 2
- **THEN** o ganho não é multiplicado, e `carro.getGiro()`/`getModoPilotagem()` não são alterados por essa lógica

#### Scenario: Modo e giro são restaurados ao normal assim que o piloto sai do traçado de fuga
- **WHEN** um piloto estava no traçado 4 ou 5 (modo e giro reduzidos) e, num ciclo seguinte, o traçado deixa de ser 4 ou 5
- **THEN** `modoPilotagem` e o giro voltam exatamente aos valores que tinham no ciclo imediatamente anterior a entrar no traçado de fuga — nunca ficam travados em `LENTO`/`GIRO_MIN_VAL`, mesmo que outra lógica do jogo (ex.: com `colisao != null`) não rode seu próprio reset nesse ciclo

### Requirement: Variável global força o comprometimento independente de agressividade/estresse
`Global` SHALL expor uma flag booleana (`FORCAR_ESCAPADA_TESTE`, default `false`) que, quando `true`, faz qualquer piloto se comprometer a escapar assim que estiver a 40 índices ou menos da entrada de um `ObjetoEscapada` no seu traçado atual, independentemente de `modoPilotagem` ou `getStress()`. A exigência de traçado/posição (estar no traçado 1 ou 2, dentro da janela de 40 índices) SHALL continuar valendo.

#### Scenario: Flag ativa compromete piloto normal e não estressado
- **WHEN** `Global.FORCAR_ESCAPADA_TESTE` é `true` e um piloto com `modoPilotagem == NORMAL` e `stress` baixo está no traçado 1, a 20 índices da entrada de um `ObjetoEscapada` com `tracadoOrigem == 1`
- **THEN** o piloto se compromete a escapar nessa zona, exatamente como um piloto agressivo e estressado se comprometeria

#### Scenario: Flag ativa não dispensa a exigência de posição
- **WHEN** `Global.FORCAR_ESCAPADA_TESTE` é `true` e um piloto está no traçado 0, ou a mais de 40 índices de qualquer entrada de escapada no seu traçado
- **THEN** o piloto não se compromete a escapar nesse ciclo
