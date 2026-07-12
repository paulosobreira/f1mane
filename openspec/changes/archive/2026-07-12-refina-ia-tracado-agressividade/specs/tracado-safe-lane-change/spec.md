## MODIFIED Requirements

### Requirement: Escape de fila indiana
Um piloto que NÃO seja o jogador humano (bot de IA) e que permaneça preso atrás do carro à frente na mesma linha por múltiplos ciclos consecutivos SHALL, após um limiar de ciclos, tentar mudar para um traçado lateral verificado como livre, ignorando apenas a checagem de vizinhança genérica que normalmente rejeitaria a mudança por causa dos próprios carros da fila. "Preso" SHALL ser contado por dois caminhos independentes, cada um com seu próprio contador e limiar, bastando que QUALQUER um dos dois atinja seu limiar pra acionar a tentativa de escape:
1. Colisão física literal (sobreposição de caixa de colisão) com `ganho` ≤ 10, por pelo menos 8 ciclos consecutivos (comportamento inalterado).
2. Proximidade sem sobreposição física: piloto à frente na mesma linha dentro de `JANELA_FILA_SEM_COLISAO` (50 nós) à frente, com `ganho` ≤ `GANHO_LIMITE_FILA_SEM_COLISAO` (15), por pelo menos `LIMIAR_CICLOS_FILA_SEM_COLISAO` (4) ciclos consecutivos — valores iniciais deliberadamente agressivos (mais fáceis de disparar que o caminho 1), a recalibrar pra baixo conforme observação em corrida real.

O jogador humano SHALL NEVER ter seu traçado mudado por essa mecânica, em nenhum modo (automático, manual, local ou online).

#### Scenario: Bot preso rastejando (colisão física) escapa para traçado lateral livre
- **WHEN** um piloto que não é o jogador humano acumula pelo menos 8 ciclos consecutivos de colisão física na mesma linha com `ganho` ≤ 10, e existe um traçado lateral sem nenhum outro carro ocupando ou cruzando uma janela de 100 nós atrás e 60 à frente da posição do piloto
- **THEN** o piloto muda para esse traçado lateral e ambos os contadores de ciclos preso são zerados

#### Scenario: Bot preso por proximidade, sem colisão física, também escapa com limiar menor
- **WHEN** um piloto que não é o jogador humano acumula pelo menos 4 ciclos consecutivos com um piloto à frente na mesma linha dentro de 50 nós (sem sobreposição de caixa de colisão) e `ganho` ≤ 15, e existe um traçado lateral verificado como livre
- **THEN** o piloto muda para esse traçado lateral e ambos os contadores de ciclos preso são zerados, sem precisar esperar os 8 ciclos do caminho de colisão física

#### Scenario: Progresso normal não aciona nenhum dos dois caminhos
- **WHEN** um piloto que não é o jogador humano está com o carro à frente na mesma linha (em colisão física ou dentro da janela de proximidade) mas o `ganho` está acima do limite de cada caminho (> 10 pra colisão física, > 15 pra proximidade) em qualquer ciclo
- **THEN** o contador correspondente é zerado nesse ciclo e o escape de fila não é acionado por esse caminho

#### Scenario: Traçado lateral ocupado não é usado
- **WHEN** um piloto que não é o jogador humano está preso e ambos os traçados laterais possíveis têm algum carro ocupando ou cruzando a janela de verificação
- **THEN** o escape não muda o traçado do piloto neste ciclo

#### Scenario: Escape desabilitado sob safety car
- **WHEN** o safety car está na pista
- **THEN** o escape de fila indiana não é acionado, mesmo com o contador de ciclos preso acima do limiar e o piloto não sendo o jogador humano

## ADDED Requirements

### Requirement: Entrada no box em três camadas de intensidade crescente
Assim que `isBox()` (piloto decidiu ir pro box) fica verdadeiro, o sistema SHALL tentar posicionar o piloto no traçado do box em até três camadas de intensidade crescente, cada uma só agindo se a anterior ainda não tiver colocado o piloto no traçado certo:

1. **Aproximação normal** (já existente, inalterada): dentro de 1000 índices de `entradaBoxIndex`, tenta `mudarTracado` não forçado pro traçado do box (via traçado 0 como intermediário se vier do lado oposto). Sujeita às guardas normais (cooldown, tráfego, animação em andamento).
2. **Aproximação forçada**: dentro de 100 índices de `entradaBoxIndex`, a mesma tentativa passa a usar mudança forçada (ignora cooldown e animação em andamento em curso), continuando a passar pelo traçado 0 como intermediário quando necessário — nunca precisa pular diretamente entre os traçados 1 e 2.
3. **Posicionamento garantido**: ao alcançar o ponto de parada do box, se o piloto ainda não estiver no traçado do box (as camadas 1/2 não foram suficientes), o sistema SHALL posicioná-lo nesse traçado incondicionalmente, ignorando cooldown, animação em andamento, e o bloqueio de troca direta entre os traçados 1 e 2 (que não se aplica ao posicionamento no box, porque o lado do box é fixo pela geometria do circuito, independente do traçado de origem do piloto). Se o piloto já estiver no traçado certo nesse momento, nenhuma ação ocorre.

As camadas 2 e 3 SHALL agir também para o jogador humano em modo manual, sem depender da chave de piloto automático/manual (ver spec `piloto-controle-automatico-manual`) — mesma exceção já existente pra camada 1 e pro restante do snap de box.

A trava de escapada ancorada (`impedidoDeMudarTracadoPorEscapada`) SHALL continuar tendo prioridade sobre as três camadas — nenhuma delas reposiciona um piloto ainda marcado pela escapada e não cumprida.

#### Scenario: Camada 1 resolve a tempo, sem necessidade de forçar
- **WHEN** um piloto decide ir pro box a mais de 100 índices de `entradaBoxIndex`, e o tráfego permite a mudança não forçada dentro da janela de 1000 índices
- **THEN** o piloto chega no traçado do box antes de entrar na janela de 100 índices, sem nenhuma mudança forçada ter sido necessária

#### Scenario: Camada 2 força a mudança dentro de 100 índices quando a camada 1 não resolveu
- **WHEN** um piloto decidido a ir pro box ainda não está no traçado do box ao entrar na janela de 100 índices de `entradaBoxIndex` (por cooldown, tráfego, ou qualquer outro motivo)
- **THEN** a partir desse ponto a tentativa de mudança passa a ser forçada, ignorando cooldown e animação em andamento

#### Scenario: Camada 2 age também pro jogador humano em modo manual
- **WHEN** o jogador humano em modo manual está indo pro box e entra na janela de 100 índices de `entradaBoxIndex` sem ainda estar no traçado do box
- **THEN** a mudança forçada ocorre normalmente, sem depender da chave de piloto automático/manual

#### Scenario: Camada 3 garante a entrada mesmo se as duas primeiras falharem
- **WHEN** um piloto no traçado 1 chega ao ponto de parada no box ainda nesse traçado, e o lado do box do circuito é 2 (as camadas 1/2 não conseguiram mudar a tempo)
- **THEN** o piloto é posicionado no traçado 2 nesse mesmo ciclo, sem ser rejeitado pela regra que bloqueia troca direta entre 1 e 2

#### Scenario: Camada 3 é um no-op se o piloto já está no traçado certo
- **WHEN** um piloto chega ao ponto de parada no box já no traçado do box (as camadas 1/2 resolveram a tempo)
- **THEN** nenhuma mudança de estado de traçado ocorre nesse ciclo por causa da camada 3 (sem reset de `indiceTracado`/`tracadoAntigo`)

#### Scenario: Piloto marcado pela escapada não é reposicionado no box antes de cumprir a escapada
- **WHEN** um piloto está marcado pela escapada ancorada (`impedidoDeMudarTracadoPorEscapada == true`) e alcança o ponto de parada no box no mesmo ciclo
- **THEN** o posicionamento no box não ocorre nesse ciclo; a trava de escapada continua tendo prioridade

### Requirement: DRS é desligado desde a decisão de ir pro box, não só fisicamente na pit lane
**Bug encontrado durante os testes desta mudança, sem relação com o escopo original** (achado ao validar as camadas de entrada no box acima): o sistema SHALL desligar o DRS (asa do carro de volta pra `MAIS_ASA`, `ativarDRS` e `podeUsarDRS` ambos `false`) assim que `isBox()` fica verdadeiro — não apenas quando o piloto já está fisicamente na pit lane (`getPtosBox() != 0`, condição já existente e mantida). Antes desta correção, o indicador visual de DRS continuava piscando (`Piloto.isPodeUsarDRS()` permanecia `true`) durante toda a aproximação ao box, já com o piloto comprometido a entrar.

#### Scenario: DRS desliga assim que o piloto decide ir pro box, ainda na pista principal
- **WHEN** um piloto decide ir pro box (`isBox()` passa a `true`) enquanto `getPtosBox()` ainda é `0`
- **THEN** `isPodeUsarDRS()` passa a `false`, `ativarDRS` é desligado, e a asa do carro volta pra `MAIS_ASA`

#### Scenario: DRS continua desligado fisicamente na pit lane
- **WHEN** um piloto tem `getPtosBox() != 0`
- **THEN** `isPodeUsarDRS()` é `false`, `ativarDRS` é desligado, e a asa do carro é `MAIS_ASA` — comportamento já existente, inalterado

#### Scenario: Fora do box, o uso de DRS não é afetado por esta correção
- **WHEN** um piloto não está indo pro box (`isBox() == false` e `getPtosBox() == 0`)
- **THEN** o novo guard de box não interfere — `ativarDRS`/`podeUsarDRS`/asa continuam regidos exclusivamente pelas regras de DRS já existentes (reta, zona de DRS, retardatário à frente, etc.)
