## MODIFIED Requirements

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

## REMOVED Requirements

### Requirement: Variável global força a escapada de forma determinística, sem RNG nem exceções
**Reason**: `Global.FORCAR_ESCAPADA_TESTE` era uma flag de validação manual usada só pra confirmar a mecânica em corrida controlada — o usuário já validou o comportamento e pediu a remoção completa (campo, uso em `processaEscapadaAncoradaAoTracado()`, e os testes que a exercitavam). A escapada ancorada não tem mais nenhum caminho que force o teste incondicionalmente.
**Migration**: Nenhuma — não havia consumidor em produção, só os testes de `PilotoEscapadaAncoradaTracadoTest`, que foram removidos junto.

## ADDED Requirements

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
