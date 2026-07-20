# editor-pneus-multiplos-cliques

## Purpose

Permitir que o editor de circuitos crie múltiplos objetos `ObjetoPneus` a partir de uma corrente contínua de cliques, em vez de exigir reabrir "Criar Objeto" para cada segmento — cada par de cliques consecutivos fecha um segmento e gera um `ObjetoPneus` independente, herdando altura e cores do último template lembrado, até que o usuário encerre a corrente com o clique direito.

## Requirements

### Requirement: Criar Pneus liga uma corrente contínua de cliques
Ao selecionar o tipo "Pneus" em "Criar Objeto" (ou pelo atalho Insert), o editor de circuitos SHALL entrar em um modo de posicionamento por uma corrente contínua de cliques, no mesmo estilo visual (marcador do ponto pendente) já usado por Guard Rails/Arquibancada/ObjetoLivre/Escapada, em vez de posicionar um único objeto com um clique.

#### Scenario: Selecionar Pneus não cria nenhum objeto até o segundo clique
- **WHEN** o usuário seleciona o tipo "Pneus" em "Criar Objeto" e clica uma única vez no canvas
- **THEN** nenhum `ObjetoPneus` é adicionado ao circuito até que um segundo clique seja registrado

#### Scenario: O ponto pendente fica marcado em magenta
- **WHEN** o usuário, no modo de posicionamento de Pneus, clica em um ponto do canvas e ainda não clicou no próximo
- **THEN** esse ponto aparece marcado em magenta no canvas, mesmo estilo de marcador usado por Guard Rails/Arquibancada/ObjetoLivre/Escapada durante a criação

### Requirement: Cada clique, a partir do segundo, fecha um segmento e gera um ObjetoPneus independente
A partir do segundo clique da corrente, cada clique novo SHALL fechar um segmento com o ponto pendente (o clique imediatamente anterior que ainda não fechou nenhum segmento com um clique posterior), criando um `ObjetoPneus` novo e independente (não um objeto compartilhado acumulando pontos) para esse segmento. O clique que acabou de fechar um segmento SHALL, em seguida, virar o novo ponto pendente, pronto para fechar o próximo segmento com o clique seguinte — ou seja, cliques consecutivos (1,2), (2,3), (3,4), ... cada um gera seu próprio objeto.

Para cada `ObjetoPneus` criado: a `largura` SHALL ser igual à distância entre os dois pontos do segmento, convertida para unidades de grade (10 pixels cada, mesma unidade que `ObjetoPneus.desenha()` já usa), com um mínimo de 1; o `angulo` SHALL ser igual à direção (em graus) do ponto pendente para o clique que fechou o segmento; a posição (`posicaoQuina`) SHALL ser calculada de forma que o segmento clicado corresponda exatamente à linha de centro do objeto (não a uma de suas bordas) — ou seja, o ponto pendente e o clique que fechou o segmento devem coincidir, respectivamente, com o ponto médio da borda esquerda e da borda direita do objeto já rotacionado, para qualquer ângulo, não apenas ângulo 0. O objeto criado SHALL ser adicionado a `circuito.getObjetosCenario()` e aparecer imediatamente na lista única e no canvas, com total compatibilidade com qualquer outro `ObjetoPneus` do circuito (mesmos campos, mesmo desenho, mesma edição por diálogo).

#### Scenario: Segundo clique fecha o primeiro segmento
- **WHEN** o usuário, no modo de posicionamento de Pneus, clica em um ponto e depois em um segundo ponto
- **THEN** um `ObjetoPneus` é criado para o segmento entre esses dois pontos, e o segundo ponto vira o novo ponto pendente

#### Scenario: Terceiro clique fecha um segundo segmento com o clique anterior, não com o primeiro
- **WHEN** o usuário completa o segundo clique (fechando o primeiro segmento) e em seguida clica em um terceiro ponto
- **THEN** um segundo `ObjetoPneus`, independente do primeiro, é criado para o segmento entre o segundo e o terceiro ponto — não entre o primeiro e o terceiro

#### Scenario: Distância entre os pontos do segmento vira a largura em unidades de grade
- **WHEN** um segmento é fechado entre dois pontos separados por 100 pixels
- **THEN** o `ObjetoPneus` criado para esse segmento tem `largura` igual a 10 (100 pixels / 10 pixels por unidade de grade)

#### Scenario: Direção do segmento vira o ângulo do objeto
- **WHEN** um segmento é fechado entre dois pontos alinhados na vertical, o segundo abaixo do primeiro
- **THEN** o `ObjetoPneus` criado para esse segmento tem `angulo` igual a 90 graus

#### Scenario: O segmento clicado corresponde à linha de centro do objeto, não a uma borda
- **WHEN** um segmento é fechado com um ângulo diferente de 0
- **THEN** o objeto criado está posicionado de forma que o ponto pendente e o ponto que fechou o segmento coincidam com o centro das bordas esquerda e direita do objeto já rotacionado, e não com um canto fixo independente do ângulo

#### Scenario: Cliques muito próximos ainda geram um objeto válido
- **WHEN** um segmento é fechado entre dois pontos muito próximos (distância menor que 10 pixels)
- **THEN** o `ObjetoPneus` criado tem `largura` igual a 1 (mínimo), sem lançar exceção

### Requirement: Altura e cores do Pneus criado vêm do último template lembrado
Cada `ObjetoPneus` criado pela corrente de cliques SHALL ter sua `altura` e cores (`corPimaria`/`corSecundaria`) preenchidas a partir do último `ObjetoPneus` criado ou editado na sessão do editor (mesmo mecanismo de memória por classe já usado pela criação de objeto único) — apenas `posicaoQuina`, `largura` e `angulo` vêm de cada segmento, conforme os requisitos acima.

#### Scenario: Objeto criado herda a altura do último Pneus editado
- **WHEN** o usuário edita a altura de um `ObjetoPneus` existente via diálogo de propriedades, e em seguida cria novos objetos pela corrente de cliques
- **THEN** cada novo `ObjetoPneus` criado tem a mesma altura que foi definida no objeto editado

#### Scenario: Objeto criado herda as cores do último Pneus editado
- **WHEN** o usuário altera as cores de um `ObjetoPneus` existente via diálogo de propriedades, e em seguida cria novos objetos pela corrente de cliques
- **THEN** cada novo `ObjetoPneus` criado tem as mesmas cores que foram definidas no objeto editado

### Requirement: A corrente continua ativa até o clique direito
Depois de fechar um segmento (criando um `ObjetoPneus`), o editor SHALL permanecer no modo de posicionamento, pronto para o próximo clique fechar o próximo segmento, sem exigir que o usuário reabra "Criar Objeto". Um clique com o botão direito do mouse SHALL encerrar a corrente sem criar nenhum objeto adicional a partir desse clique; os objetos já criados pelos segmentos anteriores SHALL permanecer no circuito.

#### Scenario: Vários segmentos seguidos criam vários objetos independentes
- **WHEN** o usuário clica em uma sequência de pontos, fechando vários segmentos consecutivos sem reabrir "Criar Objeto"
- **THEN** um `ObjetoPneus` independente é adicionado ao circuito para cada segmento fechado, cada um com posição/largura/ângulo derivados do seu próprio par de pontos

#### Scenario: Clique direito encerra a corrente sem desfazer os objetos já criados
- **WHEN** o usuário está no modo de posicionamento de Pneus (já tendo fechado zero ou mais segmentos) e clica com o botão direito do mouse
- **THEN** o modo de posicionamento é encerrado, nenhum `ObjetoPneus` adicional é criado a partir desse clique, e todos os objetos criados pelos segmentos já fechados continuam no circuito
