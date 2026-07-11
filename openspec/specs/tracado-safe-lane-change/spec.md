# Spec: tracado-safe-lane-change

## Purpose

Regras que governam mudanças de traçado (troca de linha/lane) dos carros durante a simulação de corrida, garantindo que a animação de troca seja respeitada por cooldowns, mudanças forçadas, reversões, e que carros presos em fila na mesma linha consigam escapar.

## Requirements

### Requirement: Cooldown de mudança de traçado cobre a animação completa
Com atualização suave ativa, o sistema SHALL impor um intervalo mínimo entre duas mudanças de traçado não forçadas do mesmo piloto que seja pelo menos igual à duração da animação de troca da pista atual (`indiceTracado` inicial dividido por 2, o decremento por ciclo) mais uma folga fixa de 4 ciclos.

#### Scenario: Nova mudança logo após a anterior é bloqueada mesmo com animação visualmente concluída
- **WHEN** um piloto muda de traçado e, antes de decorrido o intervalo mínimo calculado, tenta uma nova mudança não forçada
- **THEN** `mudarTracado` retorna `false` e o `tracado` do piloto permanece o mesmo

#### Scenario: Cooldown só conta mudanças efetivadas
- **WHEN** uma tentativa de mudança de traçado é bloqueada por qualquer guard (cooldown, colisão ao mudar, bandeirada)
- **THEN** o instante da última mudança (`ultimaMudancaPos`) NÃO é atualizado por essa tentativa bloqueada

### Requirement: Mudança forçada não reinicia animação em andamento sem necessidade
Os pontos de mudança forçada de traçado (escapada por stress, desvio de safety car) SHALL aguardar a animação de troca em andamento (`indiceTracado == 0`) antes de agir, adiando a ação por ciclos subsequentes em vez de interrompê-la.

#### Scenario: Escapada por stress espera a animação atual terminar
- **WHEN** o piloto está com `indiceTracado > 0` (animação de troca em andamento) e as condições de escapada por stress são atendidas
- **THEN** `escapaTracado` retorna `false` neste ciclo e a escapada é reavaliada nos ciclos seguintes até `indiceTracado` chegar a 0

#### Scenario: Desvio forçado por safety car espera a animação atual terminar
- **WHEN** o piloto está no raio de desvio de um carro batido sob safety car e `indiceTracado > 0`
- **THEN** o desvio forçado não é aplicado neste ciclo e é reavaliado no ciclo seguinte

### Requirement: Reversão forçada no meio da animação continua da posição atual
Quando uma mudança de traçado forçada ocorre com uma animação ainda em andamento (`indiceTracado > 0`) e o traçado de destino é igual ao `tracadoAntigo` do piloto (reversão para a linha de origem), o sistema SHALL recalcular `indiceTracado` para refletir o progresso lateral já percorrido, em vez de reiniciar a interpolação do valor cheio.

#### Scenario: Reversão espelha o progresso da animação
- **WHEN** uma mudança forçada reverte para o `tracadoAntigo` do piloto com `indiceTracado` parcialmente decrementado
- **THEN** o novo `indiceTracado` é definido como o valor cheio da animação menos o `indiceTracado` restante antes da reversão (com piso de 1)

#### Scenario: Mudança forçada para uma terceira linha reinicia a animação
- **WHEN** uma mudança forçada ocorre no meio de uma animação e o traçado de destino NÃO é o `tracadoAntigo` do piloto
- **THEN** `indiceTracado` é definido para o valor cheio da nova animação, sem espelhamento

### Requirement: Escape de fila indiana
Um piloto que NÃO seja o jogador humano (bot de IA) e que permaneça em colisão com o carro à frente na mesma linha por múltiplos ciclos consecutivos com `ganho` em nível de rastejo (≤ 10) SHALL, após um limiar de ciclos, tentar mudar para um traçado lateral verificado como livre, ignorando apenas a checagem de vizinhança genérica que normalmente rejeitaria a mudança por causa dos próprios carros da fila. O jogador humano SHALL NEVER ter seu traçado mudado por essa mecânica, em nenhum modo (automático, manual, local ou online).

#### Scenario: Bot preso rastejando escapa para traçado lateral livre
- **WHEN** um piloto que não é o jogador humano acumula pelo menos 8 ciclos consecutivos de colisão na mesma linha com `ganho` ≤ 10, e existe um traçado lateral sem nenhum outro carro ocupando ou cruzando uma janela de 100 nós atrás e 60 à frente da posição do piloto
- **THEN** o piloto muda para esse traçado lateral e o contador de ciclos preso é zerado

#### Scenario: Progresso normal não aciona o escape
- **WHEN** um piloto que não é o jogador humano está em colisão com o carro à frente mas o `ganho` está acima do nível de rastejo (> 10) em qualquer ciclo
- **THEN** o contador de ciclos preso é zerado e o escape de fila não é acionado

#### Scenario: Traçado lateral ocupado não é usado
- **WHEN** um piloto que não é o jogador humano está preso e ambos os traçados laterais possíveis têm algum carro ocupando ou cruzando a janela de verificação
- **THEN** o escape não muda o traçado do piloto neste ciclo

#### Scenario: Escape desabilitado sob safety car
- **WHEN** o safety car está na pista
- **THEN** o escape de fila indiana não é acionado, mesmo com o contador de ciclos preso acima do limiar e o piloto não sendo o jogador humano

#### Scenario: Jogador humano nunca aciona o escape de fila indiana
- **WHEN** o jogador humano acumula 8 ou mais ciclos consecutivos de colisão na mesma linha com `ganho` ≤ 10, em qualquer modo (automático, manual, local ou online)
- **THEN** o escape de fila indiana não é acionado para o carro do jogador; o traçado dele só muda por outra mecânica (entrada manual do jogador, box, safety car, escapada, derrapagem)

### Requirement: Snap de traçado no box independe do modo automático/manual do piloto
Os pontos de entrada e saída de box (`isBoxSaiuNestaVolta()`/`verificaSaidaBox()`, `isBox()`/`verificaEntradaBox()`) SHALL disparar a mudança de traçado correspondente para qualquer piloto — jogador humano ou bot — em qualquer modo de piloto automático/manual (automático, manual, local ou online), sem serem bloqueados pela chave descrita na spec `piloto-controle-automatico-manual`.

#### Scenario: Jogador humano em modo manual local tem o traçado ajustado ao sair do box
- **WHEN** o jogador humano, em uma partida solo com `automaticoManual` igual a `CONTROLE_MANUAL`, sai do box na volta atual
- **THEN** seu traçado é ajustado para o lado de saída do box do circuito, do mesmo jeito que aconteceria em modo automático

#### Scenario: Jogador humano online tem o traçado ajustado ao entrar no box
- **WHEN** o jogador humano, em uma partida online, se aproxima da entrada do box
- **THEN** seu traçado é ajustado para o lado de entrada do box do circuito, sem depender de nenhuma configuração de modo automático/manual

### Requirement: Desvio de carro batido sob safety car independe do modo automático/manual do piloto
Quando o safety car está na pista e há um piloto batido dentro do raio de desvio à frente na mesma faixa, o sistema SHALL desviar o traçado de qualquer piloto — jogador humano ou bot — automaticamente, em qualquer modo de piloto automático/manual, sem ser bloqueado pela chave descrita na spec `piloto-controle-automatico-manual`.

#### Scenario: Jogador humano em modo manual local desvia do carro batido sob safety car
- **WHEN** o jogador humano, em uma partida solo com `automaticoManual` igual a `CONTROLE_MANUAL`, está na mesma faixa e dentro do raio de desvio de um piloto batido, com o safety car na pista
- **THEN** o traçado do carro do jogador é mudado automaticamente para desviar, do mesmo jeito que aconteceria em modo automático

#### Scenario: Jogador humano online desvia do carro batido sob safety car
- **WHEN** o jogador humano, em uma partida online, está na mesma faixa e dentro do raio de desvio de um piloto batido, com o safety car na pista
- **THEN** o traçado do carro do jogador é mudado automaticamente para desviar, sem depender de nenhuma configuração de modo automático/manual
