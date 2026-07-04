# zona-frenagem-deteccao

## Purpose

Detectar automaticamente, a partir do padrão de nós de um circuito, os trechos de "zona de frenagem" (reta seguida de cluster de curva baixa) e expor essa informação — incluindo a posição relativa de cada nó dentro da zona — para uso pelo motor de jogo durante a simulação, substituindo a antiga lógica de janela fixa de distância até a curva mais próxima.

## Requirements

### Requirement: Zona de frenagem é detectada automaticamente a partir do padrão de nós
O sistema SHALL detectar automaticamente, ao carregar um circuito, trechos de "zona de frenagem": uma sequência de nós de reta/largada (`No.RETA`/`No.LARGADA`) imediatamente seguida por um cluster de nós de curva baixa (`No.CURVA_BAIXA`) com extensão relevante, sem uma quantidade significativa de nós de curva alta (`No.CURVA_ALTA`) entre a reta e o cluster.

#### Scenario: Reta seguida de curva baixa extensa forma uma zona de frenagem
- **WHEN** um circuito tem uma sequência de nós de reta seguida diretamente por um cluster de nós de curva baixa com pelo menos `Global.MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM` nós, sem nós de curva alta entre eles
- **THEN** os nós de reta imediatamente anteriores ao cluster (até `Global.TAMANHO_ZONA_FRENAGEM` nós) e os próprios nós do cluster de curva baixa são marcados como pertencentes a uma zona de frenagem

#### Scenario: Curva baixa isolada e curta não forma zona de frenagem
- **WHEN** um cluster de nós de curva baixa tem menos nós que `Global.MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM`
- **THEN** nenhum nó de reta anterior a esse cluster é marcado como zona de frenagem por causa dele

### Requirement: Trechos com vários trechos separados de curva alta não formam zona de frenagem
Quando, entre a reta e um cluster de curva baixa que de outra forma qualificaria como zona de frenagem, houver mais de `Global.MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM` *trechos contíguos distintos* de nós de curva alta (não a contagem total de nós — uma única curva alta longa e sinuosa, por mais nós que tenha, conta como um trecho só), o sistema SHALL NOT marcar esse trecho como zona de frenagem.

#### Scenario: Sequência de esses/chicane não é tratada como zona de frenagem
- **WHEN** entre a última reta longa e um cluster de curva baixa existem mais de `Global.MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM` trechos separados de curva alta (por exemplo, uma sequência de esses, com reta ou curva baixa intercalada entre eles)
- **THEN** nenhum nó desse trecho é marcado como zona de frenagem

#### Scenario: Um único trecho longo de curva alta ainda forma zona de frenagem
- **WHEN** entre a última reta longa e um cluster de curva baixa existe um único trecho contíguo de curva alta, mesmo que esse trecho tenha muitos nós (uma curva alta longa e sinuosa, como a primeira curva de Albert Park)
- **THEN** o trecho de reta e o cluster de curva baixa são marcados como zona de frenagem normalmente

### Requirement: Detecção considera a pista como uma volta fechada (circular)
Como a pista é uma volta fechada, quando o trecho de reta anterior a um cluster de curva baixa qualificado não tiver nós suficientes antes do índice 0, o sistema SHALL continuar a busca circularmente a partir do fim da lista de nós (ex.: a reta que antecede a linha de largada/chegada), em vez de considerar apenas os nós entre o início da lista e o cluster.

#### Scenario: Curva logo após a largada conta a reta do fim do circuito
- **WHEN** um cluster de curva baixa qualificado está a poucos nós do início da lista de nós da pista (por exemplo, logo após a largada), e o restante da reta de frenagem está nos últimos nós da lista (antes de fechar a volta no índice 0)
- **THEN** esses nós do fim da lista também são marcados como zona de frenagem, como se a lista fosse circular

### Requirement: Consulta de zona de frenagem disponível durante a simulação
O motor de jogo SHALL expor uma consulta (análoga a `obterProxCurva`) que informa se um nó específico está dentro de uma zona de frenagem, calculada uma única vez por carregamento de circuito (não recalculada a cada tick).

#### Scenario: Consultar se o nó atual do piloto está em zona de frenagem
- **WHEN** o código de simulação consulta se `piloto.getNoAtual()` está em zona de frenagem
- **THEN** a resposta reflete o cálculo feito no carregamento do circuito, sem exigir nova varredura de todos os nós da pista a cada chamada

### Requirement: Detecção calcula a posição relativa de cada nó dentro da zona
Além de marcar quais nós pertencem a uma zona de frenagem, o sistema SHALL calcular, para cada um desses nós, sua posição relativa dentro da zona: 0.0 para o nó mais distante da curva (início da zona) até 1.0 para o último nó do cluster de curva baixa (final da zona), seguindo a ordem em que o piloto realmente percorre a zona (reta → cluster).

#### Scenario: Nó no início da zona tem posição 0.0
- **WHEN** um nó é o mais distante da curva dentro de uma zona de frenagem detectada
- **THEN** sua posição relativa é 0.0

#### Scenario: Nó no final da zona tem posição 1.0
- **WHEN** um nó é o último do cluster de curva baixa de uma zona de frenagem detectada
- **THEN** sua posição relativa é 1.0

#### Scenario: Posição relativa disponível por consulta
- **WHEN** o código de simulação consulta a posição relativa de um nó que pertence a uma zona de frenagem
- **THEN** a consulta retorna um valor entre 0.0 e 1.0; para um nó fora de qualquer zona, retorna ausência de valor (não uma posição numérica)

### Requirement: Zona de frenagem substitui a janela fixa de distância em processaFreioNaReta
`Piloto.processaFreioNaReta()` SHALL usar a zona de frenagem detectada (em vez de apenas a distância até a curva mais próxima de qualquer tipo) para decidir quando `freiandoReta` deve ser `true` e quando o gatilho de travada de roda (`travouRodas`) pode ocorrer.

#### Scenario: Freiando fora de qualquer zona de frenagem detectada não aciona travada de roda por este caminho
- **WHEN** o piloto está numa reta que não faz parte de nenhuma zona de frenagem detectada
- **THEN** `processaFreioNaReta` não aciona `controleJogo.travouRodas(this)` por esse motivo, mesmo que a curva mais próxima de qualquer tipo esteja a menos de 300 nós

#### Scenario: Freiando dentro de uma zona de frenagem mantém o comportamento de suavização de ganho
- **WHEN** o piloto está dentro de uma zona de frenagem detectada, numa reta antes do cluster de curva baixa
- **THEN** `freiandoReta` é `true` e o multiplicador de ganho (`multi`/`minMulti`) continua sendo aplicado de forma gradual conforme a proximidade da curva, como antes desta mudança
