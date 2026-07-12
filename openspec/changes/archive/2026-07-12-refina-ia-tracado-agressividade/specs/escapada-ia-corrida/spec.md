## RENAMED Requirements

- FROM: `### Requirement: Piloto em modo LENTO nunca escapa; um piloto "em risco" (exceto jogador humano em modo manual) tem uma última chance via teste de habilidade`
- TO: `### Requirement: Piloto em modo LENTO não é excluído de nenhum teste; sucesso só evita a marca, sem recompensa, sem exceção para o jogador humano manual`

## MODIFIED Requirements

### Requirement: Só piloto "em risco" recebe o teste de habilidade da escapada; os demais nunca são testados nem escapam
Em `Piloto.processaEscapadaAncoradaAoTracado()`, o par de testes abaixo (teste 1, e teste 2 só se o 1 não marcar) SHALL rodar **no máximo uma vez por `ObjetoEscapada` por piloto por volta** — não uma vez por causa de risco, e sim uma vez para a zona inteira, qualquer que seja o desfecho:

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

### Requirement: Piloto em modo LENTO não é excluído de nenhum teste; sucesso só evita a marca, sem recompensa, sem exceção para o jogador humano manual
Diferente de um modelo anterior, `modoPilotagem == LENTO` SHALL NOT impedir o piloto de satisfazer a pré-condição de nenhum dos dois testes — um piloto já em `LENTO` no momento em que uma zona entra na janela de detecção pode ser normalmente marcado para escapar por qualquer um dos dois, se a respectiva pré-condição de stress/pneus se aplicar. Todo piloto "em risco" (pré-condição do teste 1 ou do teste 2 satisfeita), **incluindo o jogador humano em modo de controle manual**, SHALL passar pelo mesmo teste de habilidade (`testeHabilidadePilotoCarro()` ou `testeHabilidadePilotoFreios()`, conforme o teste) — não há exceção que marque o jogador humano manual direto, sem chance de se salvar. Em caso de sucesso (não marcado por aquela causa), `Piloto` SHALL NOT alterar `modoPilotagem` — o piloto simplesmente não é marcado, permanecendo no modo em que já estava.

Uma vez que o piloto foi marcado para escapar por um teste (1 ou 2), mudar `modoPilotagem` para `LENTO` (ou qualquer outro valor) DEPOIS de marcado SHALL NOT evitar a escapada — ver requisito "Escapada é executada ao alcançar a entrada da zona, só para quem está marcado pra escapar".

#### Scenario: Piloto já em LENTO pode ser marcado normalmente pelo teste 1
- **WHEN** um piloto com `modoPilotagem == LENTO` e `stress` acima de 90 alcança a janela de detecção de uma `ObjetoEscapada` no seu traçado atual, e falha no teste de habilidade correspondente
- **THEN** o piloto é marcado para escapar por essa zona, exatamente como aconteceria em qualquer outro modo de pilotagem

#### Scenario: Sucesso no teste 1 evita a marca, sem mudar o modo de pilotagem
- **WHEN** qualquer piloto (IA, ou jogador humano em qualquer modo) satisfaz a pré-condição do teste 1 (`stress > 90`) e `testeHabilidadePilotoCarro()` é bem-sucedido
- **THEN** o piloto não é marcado por essa causa, e `modoPilotagem` permanece exatamente como estava antes do teste — sem mudança para `LENTO` nem para nenhum outro valor

#### Scenario: Sucesso no teste 2 evita a marca, sem mudar o modo de pilotagem
- **WHEN** qualquer piloto (IA, ou jogador humano em qualquer modo), com pneus abaixo de 30% e `stress` acima de 70, satisfaz a pré-condição do teste 2 e `testeHabilidadePilotoFreios()` é bem-sucedido
- **THEN** o piloto não é marcado por essa causa, e `modoPilotagem` permanece inalterado

#### Scenario: Falha no teste aplicável marca o piloto para escapar, qualquer que seja a origem do piloto
- **WHEN** qualquer piloto (IA, ou jogador humano em modo manual ou automático) satisfaz a pré-condição de um dos dois testes e o teste de habilidade correspondente falha
- **THEN** o piloto é marcado para escapar por essa zona nesta volta, sem distinção de tratamento entre IA e jogador humano

#### Scenario: Mudar para LENTO depois de marcado não evita mais a escapada
- **WHEN** um piloto foi marcado para escapar por um dos dois testes e, antes de alcançar `indiceEntrada`, muda `modoPilotagem` para `LENTO`
- **THEN** a escapada é executada normalmente ao alcançar `indiceEntrada`, apesar do piloto estar em `LENTO` nesse momento

### Requirement: Piloto marcado para escapar fica impedido de mudar de traçado até cumprir a escapada
A partir do momento em que um piloto é marcado para escapar por uma `ObjetoEscapada` (teste 1 ou teste 2), `Piloto.mudarTracado()` SHALL rejeitar (retornar `false` sem efeito) qualquer tentativa de mudança de traçado que não seja a própria execução forçada da escapada marcada — para IA e para jogador humano em modo manual, sem distinção de origem da chamada. A restrição SHALL ser liberada assim que a escapada marcada for cumprida (o piloto efetivamente entra no traçado de fuga 4 ou 5), **ou assim que o piloto decidir ir pro box** (`isBox()` verdadeiro) antes de cumprir a escapada — ver requisito "Piloto indo pro box é excluído da escapada desde a decisão".

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

## ADDED Requirements

### Requirement: Piloto indo pro box é excluído da escapada desde a decisão
`Piloto.processaEscapadaDaPista()` SHALL retornar sem processar nenhum teste ou execução de escapada quando o piloto tiver decidido ir pro box (`isBox() == true`), não apenas quando já estiver fisicamente dentro da pit lane (`getPtosBox() != 0`, condição já existente e mantida). Se o piloto já estava marcado pela escapada ancorada (`impedidoDeMudarTracadoPorEscapada == true`) no momento em que decide ir pro box, essa trava SHALL ser liberada nesse mesmo ponto, evitando que o piloto fique permanentemente impedido de mudar de traçado (inclusive de entrar no box) pelo resto da corrida.

#### Scenario: Piloto que decide ir pro box deixa de ser testável pela escapada, mesmo ainda na pista principal
- **WHEN** um piloto com `stress` acima de 90 decide ir pro box (`isBox()` passa a `true`) enquanto ainda está na pista principal, antes de `getPtosBox()` deixar de ser zero, e entra na janela de detecção de uma `ObjetoEscapada`
- **THEN** nenhum dos dois testes de escapada é avaliado, e o piloto não é marcado nem forçado a escapar por essa zona

#### Scenario: Decidir ir pro box depois de já marcado libera a trava de mudança de traçado
- **WHEN** um piloto já está marcado pela escapada ancorada (`impedidoDeMudarTracadoPorEscapada == true`, ainda não cumpriu a escapada) e decide ir pro box antes de alcançar a entrada da zona
- **THEN** a trava é liberada nesse mesmo ciclo, e o piloto volta a poder mudar de traçado normalmente (inclusive ser posicionado no traçado do box pelas camadas de entrada — ver spec `tracado-safe-lane-change`)

#### Scenario: Piloto já fisicamente na pit lane continua excluído, como antes
- **WHEN** um piloto tem `getPtosBox() != 0` (já dentro da pit lane)
- **THEN** `processaEscapadaDaPista()` continua retornando sem processar nada, exatamente como já acontecia antes desta mudança
