## ADDED Requirements

### Requirement: Área visual de um ObjetoEscapada cobre a espessura do traçado
`ObjetoEscapada#obterAreaVisual()` SHALL retornar o retângulo bruto dos vértices (`obterArea()`) expandido pela espessura (`largura`) com que o traçado é desenhado, e não apenas esse retângulo bruto. Essa área é usada para desenhar o contorno de destaque quando a escapada está selecionada na lista de objetos do circuito, garantindo que o destaque cubra o traçado realmente visível — não é mais usada para hit-testing de clique no canvas (ver requisito abaixo).

#### Scenario: Destaque de seleção pela lista cobre a espessura visível do traçado
- **WHEN** uma `ObjetoEscapada` está selecionada na lista de objetos do circuito
- **THEN** o retângulo de destaque desenhado no canvas cobre o traçado desenhado (incluindo a espessura de `largura`), não só um retângulo fino em cima dos vértices originais

### Requirement: ObjetoEscapada, uma vez criada, não tem nenhuma interação por clique no canvas
Uma `ObjetoEscapada` já finalizada SHALL NOT responder a nenhuma interação por clique no canvas do editor: não pode ser selecionada, arrastada, nem abrir menu de contexto (clique direito) nem diálogo de propriedades (duplo clique). O ponto único de hit-testing por clique no canvas (`encontraObjetoPista`/`encontraObjetoPistaNaLista`) SHALL excluir toda `ObjetoEscapada` da busca. A única interação suportada depois da criação é a remoção pela lista de objetos do circuito (botão "Remover" ou tecla Delete), que opera sobre a seleção da própria lista, não sobre hit-testing de clique no canvas.

#### Scenario: Clicar em qualquer parte do traçado (vértice ou dentro da espessura) não encontra a escapada
- **WHEN** o usuário clica em qualquer ponto do traçado desenhado de uma `ObjetoEscapada` já finalizada — em cima de um vértice ou dentro da espessura visível
- **THEN** `encontraObjetoPista` não retorna essa escapada

#### Scenario: Clique e arraste não move a escapada
- **WHEN** o usuário pressiona e arrasta o mouse sobre uma `ObjetoEscapada` já finalizada
- **THEN** a escapada não é selecionada (`objetoPista`/`objetoArrastando` continuam `null`) e `posicaoQuina` não muda

#### Scenario: Clique direito não abre menu de contexto
- **WHEN** o usuário clica com o botão direito sobre uma `ObjetoEscapada` já finalizada
- **THEN** nenhum menu de contexto é aberto, e a escapada não é selecionada

#### Scenario: Duplo clique (canvas ou lista) não abre diálogo de propriedades
- **WHEN** o usuário dá duplo clique em uma `ObjetoEscapada` já finalizada, no canvas ou na lista de objetos
- **THEN** nenhum diálogo de propriedades (`FormularioObjetos`) é aberto

#### Scenario: Duplo clique em outros tipos de objeto continua funcionando normalmente
- **WHEN** o usuário dá duplo clique em um objeto de qualquer outro tipo (ex.: `ObjetoLivre`), no canvas ou na lista
- **THEN** o diálogo de propriedades (`FormularioObjetos`) é aberto normalmente, como antes desta mudança

#### Scenario: Remoção pela lista de objetos continua funcionando
- **WHEN** o usuário seleciona uma `ObjetoEscapada` na lista de objetos e clica em "Remover" ou pressiona a tecla Delete
- **THEN** a escapada é removida do circuito normalmente

## MODIFIED Requirements

### Requirement: Entrada e saída gravam o índice do nó de traçado ancorado
Ao validar a entrada ou a saída de um `ObjetoEscapada` na criação, o editor SHALL gravar o índice do nó de traçado (`No.getIndex()`, compartilhado entre `pistaFull`, `pista1Full` e `pista2Full`) em que esse ponto foi ancorado — `indiceEntrada` para o primeiro ponto, `indiceSaida` para o último. Esses índices SHALL ser usados para localizar a zona de escapada ao longo da volta, sem precisar recalcular a posição do nó mais próximo depois.

#### Scenario: Criar a escapada grava os índices de entrada e saída
- **WHEN** um `ObjetoEscapada` é criado com sucesso (entrada e saída validadas)
- **THEN** `indiceEntrada` e `indiceSaida` refletem os índices dos nós de traçado usados para ancorar cada ponta

### Requirement: O carro de teste do editor (Teste Pista) segue o trajeto da escapada dentro do intervalo ancorado
Quando o modo de escapada do carro de teste do editor (`TestePista`, ligado pelo checkbox "Testar Escapada") está ativo, e o índice atual do carro ao longo da pista cai dentro do intervalo `[indiceEntrada, indiceSaida]` de algum `ObjetoEscapada` do circuito, o carro de teste SHALL seguir o trajeto de `pontos` dessa escapada (interpolado ao longo do caminho) em vez da pista normal — precisamente porque esse intervalo é ancorado a nós reais do traçado em que a escapada foi definida. Fora desse intervalo, ou com o modo de escapada desligado, o carro de teste SHALL continuar na pista normal, como antes desta mudança. Esse comportamento SHALL refletir criações feitas na mesma sessão do editor, sem exigir salvar nem recarregar o circuito.

#### Scenario: Índice de entrada ativa o trajeto da escapada
- **WHEN** o modo de escapada do carro de teste está ligado e o índice atual do carro é igual a `indiceEntrada` de um `ObjetoEscapada`
- **THEN** a posição do carro de teste passa a ser o primeiro ponto do trajeto dessa escapada

#### Scenario: Índice de saída corresponde ao fim do trajeto da escapada
- **WHEN** o modo de escapada do carro de teste está ligado e o índice atual do carro é igual a `indiceSaida` de um `ObjetoEscapada`
- **THEN** a posição do carro de teste é o último ponto do trajeto dessa escapada

#### Scenario: Fora do intervalo, o carro de teste usa a pista normal
- **WHEN** o índice atual do carro de teste está fora de `[indiceEntrada, indiceSaida]` de qualquer `ObjetoEscapada` do circuito
- **THEN** a posição do carro de teste vem da pista normal, independente do modo de escapada estar ligado ou desligado

#### Scenario: Criar a escapada na mesma sessão já é reconhecido pelo Teste Pista
- **WHEN** o usuário termina de criar um `ObjetoEscapada` (saída validada), e em seguida ativa o modo de escapada do carro de teste, sem salvar nem recarregar o circuito
- **THEN** o carro de teste já segue o trajeto dessa escapada quando seu índice cai no intervalo `[indiceEntrada, indiceSaida]`

## REMOVED Requirements

### Requirement: Edição de pontos de um ObjetoEscapada existente reaplica a validação só na entrada e na saída
**Reason**: Decisão de produto — uma `ObjetoEscapada` já criada não pode mais ser editada de forma alguma pelo canvas (nem pontos, nem propriedades, nem reposicionada); a única operação suportada depois da criação é a remoção pela lista de objetos. O único ponto de entrada da UI para esse modo de edição (botão "Editar Pontos" do menu de contexto) deixou de existir, então o mecanismo interno (`editandoPontosEscapadaDe` e os métodos associados) foi removido junto.
**Migration**: Para mudar a geometria de uma zona de escapada já criada, remova o objeto pela lista de objetos (botão "Remover" ou tecla Delete) e crie um novo objeto no lugar certo.
