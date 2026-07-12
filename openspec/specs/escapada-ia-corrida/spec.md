# Spec: escapada-ia-corrida

## Purpose

Define a mecânica de escapada (run-off) ancorada ao traçado durante a corrida: como as zonas de escapada de um circuito (`ObjetoEscapada`) são derivadas em `pista4Full`/`pista5Full`, quando um piloto é testado e forçado a escapar, como o retorno ao traçado de origem funciona, e as reduções de velocidade/modo aplicadas enquanto o piloto está no traçado de fuga — garantindo comportamento determinístico, reativo (nunca proativo) e idêntico em qualquer volta, incluindo a volta 1.

## Requirements

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
Em `Piloto.processaEscapadaAncoradaAoTracado()`, o par de testes abaixo (teste 1, e teste 2 só se o 1 não marcar) SHALL rodar **no máximo uma vez por `ObjetoEscapada` por piloto por volta** — não uma vez por causa de risco, e sim uma vez para a zona inteira, qualquer que seja o desfecho:

1. **Teste de agressividade+stress**: `getStress() > Global.LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA` E `modoPilotagem == AGRESSIVO` E `!testeHabilidadePiloto()`. Se verdadeiro, o piloto é marcado para escapar nesta zona.
2. **Teste de pneus** (só avaliado se o teste 1 não marcou): `carro.getPorcentagemDesgastePneus() < 30` E `modoPilotagem != LENTO` E `!testeHabilidadePiloto()`. Se verdadeiro, o piloto é marcado para escapar nesta zona.

O gate que garante o "no máximo uma vez" é o cache por zona por volta (`resultadoTesteEscapadaPorZonaNestaVolta`, já existente): a primeira vez que a zona entra na janela de detecção (distância até 150 índices, ver requisito de janela), se ainda não há nenhum resultado registrado para ela nesta volta, os dois testes rodam (2, condicional ao 1) e o desfecho (marcado OU não marcado) SHALL ser gravado no cache. A partir daí — em qualquer ciclo seguinte desta mesma volta, inclusive mais perto da entrada — a zona SHALL NOT ser testada de novo, **mesmo que o resultado gravado tenha sido "não marcado" (o piloto passou nos dois testes) e o piloto continue satisfazendo as mesmas pré-condições de risco**. Só a virada de volta (que limpa o cache) libera um novo par de testes para essa zona.

Cada teste SHALL NOT chamar `testeHabilidadePiloto()` (RNG) quando sua pré-condição de risco (stress+agressividade, ou pneus+não-LENTO) já é falsa — preservando o caráter reativo e determinístico da mecânica. Um piloto para quem nenhum dos dois testes se aplica (nenhuma pré-condição de risco satisfeita) em nenhum ponto dentro da janela de detecção SHALL NOT ser testado e SHALL NOT escapar por essa zona nesta volta.

#### Scenario: Piloto agressivo e estressado é avaliado pelo teste 1 dentro de 150 índices
- **WHEN** um piloto tem `modoPilotagem == AGRESSIVO`, `stress` acima do limite, está no traçado 1, e a entrada de um `ObjetoEscapada` com `tracadoOrigem == 1` está 120 índices à frente
- **THEN** o teste 1 (agressividade+stress) é avaliado nesse ciclo (ou já foi avaliado antes, se a zona já estava em cache)

#### Scenario: Fora da janela de detecção, ainda não recebe nenhum teste
- **WHEN** um piloto agressivo e estressado está no traçado 1 e a entrada do `ObjetoEscapada` mais próximo com `tracadoOrigem == 1` está 200 índices à frente
- **THEN** nenhum dos dois testes é avaliado ainda

#### Scenario: Piloto que nunca satisfaz nenhuma pré-condição de risco não é testado e não escapa
- **WHEN** um piloto com `modoPilotagem == NORMAL`, `stress` dentro do limite e pneus acima de 30% alcança `indiceEntrada` de um `ObjetoEscapada` no seu traçado atual, sem em nenhum momento anterior ter satisfeito a pré-condição do teste 1 nem do teste 2
- **THEN** nenhum teste de habilidade é realizado, e o piloto NÃO escapa por essa zona, permanecendo no traçado 1 ou 2 em que estava

#### Scenario: Teste 1 não marca, teste 2 é avaliado em sequência
- **WHEN** um piloto tem `stress` acima do limite mas `modoPilotagem != AGRESSIVO` (teste 1 não se aplica), e ao mesmo tempo pneus abaixo de 30% com `modoPilotagem != LENTO` (pré-condição do teste 2 satisfeita)
- **THEN** o teste 1 não consome `testeHabilidadePiloto()` (pré-condição falsa), e o teste 2 é avaliado nesse mesmo ciclo

#### Scenario: Teste 1 já marca o piloto; teste 2 não é avaliado
- **WHEN** um piloto satisfaz a pré-condição do teste 1 (agressivo+estressado) e `testeHabilidadePiloto()` falha nesse teste
- **THEN** o piloto é marcado para escapar por causa do teste 1, e o teste 2 (pneus) NÃO é avaliado nesse ciclo, mesmo que os pneus também estejam abaixo de 30%

#### Scenario: Piloto que passa nos dois testes não é retestado mais perto da entrada, mesmo continuando em risco
- **WHEN** um piloto agressivo e estressado é avaliado pelos testes 1 e 2 ao entrar na janela de 150 índices de uma `ObjetoEscapada`, não é marcado por nenhum dos dois (passou), e continua `AGRESSIVO` com `stress` acima do limite nos ciclos seguintes, cada vez mais perto de `indiceEntrada` (ex.: a 100, depois a 20 índices)
- **THEN** nenhum novo teste (1 ou 2) é realizado para essa zona em nenhum desses ciclos seguintes — o resultado "não marcado" gravado no primeiro teste vale pelo resto da volta, e o piloto passa pela entrada sem escapar

#### Scenario: Cache registra o desfecho independente de qual teste (1 ou 2) decidiu, ou de nenhum ter se aplicado
- **WHEN** uma `ObjetoEscapada` é avaliada pela primeira vez nesta volta para um piloto, qualquer que seja o desfecho (marcado pelo teste 1, marcado pelo teste 2, ou não marcado por nenhum dos dois, inclusive quando nenhuma pré-condição de risco nunca se aplicou)
- **THEN** esse desfecho é gravado no cache da zona para esta volta, e nenhum dos dois testes volta a rodar para essa zona neste piloto até a volta mudar

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

#### Scenario: Piloto que derrapou para um traçado com escapada à frente não é trazido de volta automaticamente
- **WHEN** um piloto muda de traçado 0 para 1 ou 2 pela mecânica de derrapagem (ver spec `derrapagem-piloto`) e, num ciclo seguinte, existe uma `ObjetoEscapada` a mais de 150 índices de distância no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` relacionada à escapada é feita — o piloto permanece no traçado em que a derrapagem o colocou até que a lógica geral de condução (ou o jogador) decida mudar

#### Scenario: Piloto normal cruzando um traçado com escapada à frente não é desviado
- **WHEN** um piloto não comprometido está dentro da janela de 150 índices da entrada de uma `ObjetoEscapada` no seu traçado atual
- **THEN** nenhuma tentativa de `mudarTracado(0)` é feita nesse ciclo; se o piloto ainda estiver no mesmo traçado ao alcançar `indiceEntrada`, o desfecho é decidido pelos requisitos de comprometimento/gatilho desta spec

### Requirement: Escapada é executada ao alcançar a entrada da zona, só para quem está marcado pra escapar
Quando um piloto marcado para escapar nesta zona (por qualquer um dos dois testes, ou pela variável global de teste) alcança `indiceEntrada` da zona-alvo ainda no traçado 1 ou 2 correspondente, `Piloto` SHALL forçar a mudança para o traçado de escapada correspondente (`mudarTracado(5, true)` quando `tracadoOrigem == 1`; `mudarTracado(4, true)` quando `tracadoOrigem == 2`), independentemente do `modoPilotagem` do piloto nesse momento (ver requisito anterior). Um piloto que nunca foi marcado NÃO é forçado a escapar.

#### Scenario: Comprometido por agressividade executa a escapada na entrada
- **WHEN** um piloto marcado para escapar (teste 1) alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 2`, ainda no traçado 2
- **THEN** o piloto muda (forçado) para o traçado 4

#### Scenario: Comprometido por pneus executa a escapada na entrada mesmo em LENTO
- **WHEN** um piloto marcado para escapar (teste 2, pneus) alcança `indiceEntrada` de um `ObjetoEscapada` com `tracadoOrigem == 1`, já em `modoPilotagem == LENTO` (mudado depois de ter sido marcado), ainda no traçado 1
- **THEN** o piloto muda (forçado) para o traçado 5

### Requirement: Piloto em modo LENTO nunca escapa; um piloto "em risco" (exceto jogador humano em modo manual) tem uma última chance via teste de habilidade
`modoPilotagem == LENTO` impede o piloto de satisfazer a pré-condição do teste de pneus (que exige `modoPilotagem != LENTO`) e, por não poder ser simultaneamente `LENTO` e `AGRESSIVO`, também nunca satisfaz a pré-condição do teste de agressividade+stress — ou seja, um piloto já em `LENTO` no momento em que uma zona entra na janela de detecção SHALL NOT ser marcado para escapar por ela. Um piloto "em risco" (pré-condição do teste 1 ou do teste 2 satisfeita) que NÃO seja um jogador humano em modo de controle manual (`isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())`) tem, em cada teste cuja pré-condição se aplica, uma chance via `testeHabilidadePiloto()`: em caso de sucesso (não marcado por aquela causa), `Piloto` SHALL mudar para `modoPilotagem == LENTO` nesse mesmo teste (recompensa por escapar da marca por pouco). Jogador humano em modo manual "em risco" NÃO recebe esse teste automático — fica marcado direto, sem chance de se salvar nem de virar LENTO automaticamente.

Diferente do modelo anterior, uma vez que o piloto foi marcado para escapar por um teste (1 ou 2), mudar `modoPilotagem` para `LENTO` (ou qualquer outro valor) DEPOIS de marcado SHALL NOT evitar a escapada — ver requisito "Escapada é executada ao alcançar a entrada da zona, só para quem está marcado pra escapar".

#### Scenario: Piloto já em LENTO nunca é marcado, mesmo com stress acima do limite
- **WHEN** um piloto com `modoPilotagem == LENTO` e stress acima do limite alcança a janela de detecção de uma `ObjetoEscapada` no seu traçado atual
- **THEN** nenhum dos dois testes marca o piloto, e ele não escapa por essa zona

#### Scenario: Sucesso no teste 1 evita a marca e vira LENTO
- **WHEN** um piloto de IA (ou jogador humano em modo automático) satisfaz a pré-condição do teste 1 (agressivo+estressado) e `testeHabilidadePiloto()` é bem-sucedido
- **THEN** o piloto não é marcado por essa causa, muda para `modoPilotagem == LENTO`, e (por já estar LENTO) também não satisfaz mais a pré-condição do teste 2 nesta volta

#### Scenario: Sucesso no teste 2 evita a marca e vira LENTO
- **WHEN** um piloto de IA (ou jogador humano em modo automático), com pneus abaixo de 30% e `modoPilotagem != LENTO`, satisfaz a pré-condição do teste 2 e `testeHabilidadePiloto()` é bem-sucedido
- **THEN** o piloto não é marcado por essa causa, e muda para `modoPilotagem == LENTO`

#### Scenario: Falha no teste aplicável marca o piloto para escapar
- **WHEN** um piloto de IA (ou jogador humano em modo automático) satisfaz a pré-condição de um dos dois testes e `testeHabilidadePiloto()` falha
- **THEN** o piloto é marcado para escapar por essa zona nesta volta

#### Scenario: Jogador humano em modo manual "em risco" é marcado direto, sem teste automático
- **WHEN** um jogador humano com `Global.CONTROLE_MANUAL` ativo satisfaz a pré-condição de um dos dois testes (não estando em LENTO)
- **THEN** nenhum teste de habilidade automático é realizado, e o piloto é marcado para escapar diretamente

#### Scenario: Mudar para LENTO depois de marcado não evita mais a escapada
- **WHEN** um piloto foi marcado para escapar por um dos dois testes e, antes de alcançar `indiceEntrada`, muda `modoPilotagem` para `LENTO`
- **THEN** a escapada é executada normalmente ao alcançar `indiceEntrada`, apesar do piloto estar em `LENTO` nesse momento

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

### Requirement: Piloto marcado para escapar fica impedido de mudar de traçado até cumprir a escapada
A partir do momento em que um piloto é marcado para escapar por uma `ObjetoEscapada` (teste 1, teste 2, ou `FORCAR_ESCAPADA_TESTE`), `Piloto.mudarTracado()` SHALL rejeitar (retornar `false` sem efeito) qualquer tentativa de mudança de traçado que não seja a própria execução forçada da escapada marcada — para IA e para jogador humano em modo manual, sem distinção de origem da chamada. A restrição SHALL ser liberada assim que a escapada marcada for cumprida (o piloto efetivamente entra no traçado de fuga 4 ou 5).

#### Scenario: Piloto marcado não consegue voltar ao traçado 0 antes da entrada
- **WHEN** um piloto foi marcado para escapar por uma `ObjetoEscapada` à frente e, antes de alcançar `indiceEntrada`, alguma outra lógica (condução geral, jogador manual) tenta `mudarTracado(0)`
- **THEN** `mudarTracado` retorna `false` e o traçado do piloto não muda

#### Scenario: Jogador humano manual marcado também fica impedido de mudar de traçado
- **WHEN** um jogador humano em modo manual foi marcado para escapar e tenta mudar de traçado manualmente antes de alcançar a entrada da zona
- **THEN** a tentativa é rejeitada, do mesmo jeito que para um piloto de IA

#### Scenario: A própria execução da escapada marcada não é bloqueada pela trava
- **WHEN** um piloto marcado para escapar alcança `indiceEntrada` da zona-alvo (`distancia <= 0`), ainda no traçado de origem
- **THEN** a mudança forçada para o traçado de fuga (4 ou 5) é aplicada normalmente, apesar da trava de mudança de traçado estar ativa até esse momento

#### Scenario: Trava é liberada assim que a escapada é cumprida
- **WHEN** um piloto marcado para escapar entra efetivamente no traçado de fuga (4 ou 5)
- **THEN** `mudarTracado` volta a aceitar mudanças de traçado normalmente para esse piloto (sujeito às demais guardas já existentes, como cooldown e colisão)

### Requirement: A mecânica usa a mesma regra em qualquer volta, incluindo a volta 1
`Piloto.processaEscapadaAncoradaAoTracado()` e `Piloto.processaSaidaDaEscapada()` SHALL NOT ter nenhum tratamento diferente por número de volta — o teste de habilidade preventivo e o gatilho de escapada/retorno se comportam de forma idêntica na volta 1 e em qualquer outra volta (decisão explícita do usuário — ver D13 do design.md, que documenta uma tentativa revertida de desligar a mecânica na volta 1).

#### Scenario: Volta 1 escapa normalmente, sem tratamento especial
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto agressivo e estressado alcança a entrada de uma `ObjetoEscapada`
- **THEN** o piloto escapa, exatamente como aconteceria em qualquer outra volta

#### Scenario: Volta 1 processa o retorno do traçado de fuga normalmente
- **WHEN** `controleJogo.getNumVoltaAtual()` é `1` e um piloto está no traçado 4 ou 5 dentro da janela de retorno, com teste de habilidade bem-sucedido
- **THEN** o piloto volta ao traçado de origem, exatamente como aconteceria em qualquer outra volta
