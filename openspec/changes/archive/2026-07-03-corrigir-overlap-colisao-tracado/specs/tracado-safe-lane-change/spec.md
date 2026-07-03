## ADDED Requirements

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
Um piloto que permanece em colisão com o carro à frente na mesma linha por múltiplos ciclos consecutivos com `ganho` em nível de rastejo (≤ 10) SHALL, após um limiar de ciclos, tentar mudar para um traçado lateral verificado como livre, ignorando apenas a checagem de vizinhança genérica que normalmente rejeitaria a mudança por causa dos próprios carros da fila.

#### Scenario: Carro preso rastejando escapa para traçado lateral livre
- **WHEN** um piloto acumula pelo menos 8 ciclos consecutivos de colisão na mesma linha com `ganho` ≤ 10, e existe um traçado lateral sem nenhum outro carro ocupando ou cruzando uma janela de 100 nós atrás e 60 à frente da posição do piloto
- **THEN** o piloto muda para esse traçado lateral e o contador de ciclos preso é zerado

#### Scenario: Progresso normal não aciona o escape
- **WHEN** um piloto está em colisão com o carro à frente mas o `ganho` está acima do nível de rastejo (> 10) em qualquer ciclo
- **THEN** o contador de ciclos preso é zerado e o escape de fila não é acionado

#### Scenario: Traçado lateral ocupado não é usado
- **WHEN** o piloto está preso e ambos os traçados laterais possíveis têm algum carro ocupando ou cruzando a janela de verificação
- **THEN** o escape não muda o traçado do piloto neste ciclo

#### Scenario: Escape desabilitado sob safety car
- **WHEN** o safety car está na pista
- **THEN** o escape de fila indiana não é acionado, mesmo com o contador de ciclos preso acima do limiar
