# objeto-escapada-tracado

## Purpose

Defines `ObjetoEscapada` as a point-chain path object (in the same click-point-to-point spirit as `ObjetoGuardRails`) whose entry and exit points are anchored to real track nodes (`No`) on track 1 or track 2, with tolerance-based validation on entry/exit placement and editing, same-track exit enforcement, drawing of the full point path, recorded anchor node indices (`indiceEntrada`/`indiceSaida`), and consumption of that anchored range by the editor's own test car (`TestePista`). Race-time escapada consumption in `Piloto.java` is explicitly out of scope for this capability.

## Requirements

### Requirement: ObjetoEscapada é um encadeamento de pontos, com a entrada e a saída ancoradas ao traçado
`ObjetoEscapada` SHALL ser modelado como um objeto de caminho com uma lista de pontos (`pontos`), no mesmo espírito de clique-ponto-a-ponto de `ObjetoGuardRails`: clique esquerdo adiciona um ponto, clique direito finaliza. O PRIMEIRO ponto (nó de entrada) e o ÚLTIMO ponto (nó de saída, definido pelo clique direito que finaliza) SHALL cada um estar ancorado a um nó (`No`) real do traçado 1 ou 2 da pista (`circuito.getPista1Full()`/`circuito.getPista2Full()`), nunca ao traçado 0 (`circuito.getPista()`). Os pontos entre o primeiro e o último (o trajeto da zona de escapada em si) SHALL ser livres, sem nenhuma validação de proximidade a nó de traçado.

#### Scenario: Clique esquerdo perto de um nó do traçado 1 ou 2 define a entrada
- **WHEN** o usuário, criando um `ObjetoEscapada`, dá o primeiro clique esquerdo em um ponto cuja distância ao nó mais próximo de `pista1Full` ou `pista2Full` está dentro da tolerância de validação
- **THEN** esse ponto é adicionado a `pontos` como o primeiro (entrada), na posição desse nó, e o traçado usado (`1` ou `2`) é guardado como `tracadoOrigem`

#### Scenario: Clique perto do traçado 0 não é aceito como entrada
- **WHEN** o usuário dá o primeiro clique esquerdo em um ponto cujo nó mais próximo pertence apenas ao traçado central (`circuito.getPista()`, traçado 0), mesmo que essa distância seja pequena
- **THEN** esse clique não adiciona nenhum ponto, pois nós do traçado 0 nunca são candidatos válidos para a entrada

#### Scenario: Cliques esquerdos seguintes adicionam pontos livres, sem validação
- **WHEN**, com a entrada já definida, o usuário dá mais cliques esquerdos em qualquer posição (perto ou longe de qualquer traçado)
- **THEN** cada clique adiciona um ponto a `pontos` exatamente na posição clicada, sem nenhuma validação de proximidade a nó de traçado

#### Scenario: Clique direito perto de um nó do mesmo traçado da entrada define a saída e finaliza
- **WHEN**, com a entrada já definida em `tracadoOrigem` (e zero ou mais pontos livres já adicionados), o usuário clica com o botão direito em um ponto cuja distância ao nó mais próximo do MESMO traçado (`tracadoOrigem`) está dentro da tolerância de validação
- **THEN** esse ponto é adicionado a `pontos` como o último (saída), na posição desse nó, o objeto é adicionado a `circuito.getObjetos()`, e o modo de criação é encerrado

### Requirement: Clique de entrada ou de saída fora da tolerância do traçado é rejeitado com alerta
Ao posicionar o PRIMEIRO ponto (entrada, na criação) ou o ÚLTIMO ponto (saída, no clique direito que finaliza a criação, ou ao arrastar qualquer um dos dois em modo de edição), o editor SHALL validar a distância entre o ponto clicado/solto e o nó de traçado candidato mais próximo. Quando nenhum nó válido está dentro da tolerância, o editor SHALL exibir um alerta (`JOptionPane`, via `Lang.msg("pontoEscapadaInvalido")`) informando que não é um ponto de escapada válido, e SHALL NOT posicionar, mover ou finalizar com esse ponto. Essa validação SHALL NOT se aplicar aos pontos livres do meio do trajeto.

#### Scenario: Clique de entrada sem nó de traçado próximo mostra alerta
- **WHEN** o usuário dá o primeiro clique esquerdo de um `ObjetoEscapada` em um ponto sem nenhum nó de `pista1Full`/`pista2Full` dentro da tolerância
- **THEN** um alerta é exibido informando que não é um ponto de escapada válido, e nenhum ponto é adicionado

#### Scenario: Clique de saída sem nó do traçado de origem próximo mostra alerta e não finaliza
- **WHEN**, com a entrada já definida em um traçado, o usuário clica com o botão direito em um ponto sem nenhum nó do mesmo traçado dentro da tolerância
- **THEN** um alerta é exibido informando que não é um ponto de escapada válido, o objeto permanece em modo de criação (os pontos já adicionados, incluindo os livres, continuam ali), e a criação não é finalizada

### Requirement: Saída exige o mesmo traçado usado na entrada
O último ponto (saída) de um `ObjetoEscapada` SHALL ser validado contra o mesmo traçado (`1` ou `2`) identificado pelo primeiro ponto (entrada) — um nó próximo do traçado oposto ao de origem SHALL NOT ser aceito como saída, mesmo que esteja dentro da tolerância de distância.

#### Scenario: Entrada no traçado 1 exige saída também no traçado 1
- **WHEN** a entrada foi definida em um nó de `pista1Full`, e o usuário clica com o botão direito perto de um nó de `pista2Full` (mas não de `pista1Full`) dentro da tolerância de distância
- **THEN** esse clique não é aceito como saída, e o alerta de ponto inválido é exibido

#### Scenario: Entrada e saída no mesmo traçado são aceitas
- **WHEN** a entrada foi definida em um nó de `pista2Full`, e o usuário clica com o botão direito perto de outro nó de `pista2Full` dentro da tolerância de distância
- **THEN** a saída é aceita e a criação do `ObjetoEscapada` é finalizada

### Requirement: Traçado de escapada é desenhado ao longo de todos os pontos
Após a criação (ou edição) de um `ObjetoEscapada` válido (pelo menos 2 pontos), `gerar()` SHALL construir um caminho (`GeneralPath`) poligonal ligando todos os pontos em ordem, e `desenha()` SHALL desenhar esse caminho no editor (com marcadores na entrada e na saída), substituindo o desenho elíptico do modelo anterior.

#### Scenario: Objeto finalizado desenha o trajeto por todos os pontos
- **WHEN** um `ObjetoEscapada` tem 2 ou mais pontos definidos
- **THEN** o editor desenha um caminho visível ligando todos os pontos em ordem, com marcadores na entrada e na saída, e não mais uma elipse solta

### Requirement: Edição de pontos de um ObjetoEscapada existente reaplica a validação só na entrada e na saída
O editor SHALL permitir mover qualquer ponto de um `ObjetoEscapada` já criado, reaproveitando o modo de edição de pontos já usado por `ObjetoGuardRails`. Ao soltar o PRIMEIRO ou o ÚLTIMO ponto fora da tolerância do traçado correspondente (traçado 1/2 para a entrada; o mesmo traçado de origem para a saída), o editor SHALL rejeitar o movimento e reverter o ponto para sua última posição válida, exibindo o alerta de ponto inválido. Pontos do meio (trajeto livre) SHALL ser aceitos em qualquer posição solta, sem validação.

#### Scenario: Arrastar a entrada para fora do traçado é revertido
- **WHEN** o usuário arrasta o primeiro ponto (entrada) de um `ObjetoEscapada` existente e solta fora da tolerância de qualquer nó de `pista1Full`/`pista2Full`
- **THEN** o ponto reverte para sua posição anterior, e o alerta de ponto inválido é exibido

#### Scenario: Arrastar a saída para o traçado oposto ao de origem é revertido
- **WHEN** o usuário arrasta o último ponto (saída) de um `ObjetoEscapada` cuja entrada está no traçado 1, e solta perto de um nó do traçado 2
- **THEN** o ponto reverte para sua posição anterior, e o alerta de ponto inválido é exibido

#### Scenario: Arrastar um ponto livre do meio nunca é rejeitado
- **WHEN** o usuário arrasta um ponto que não é o primeiro nem o último de um `ObjetoEscapada` existente, e solta em qualquer posição
- **THEN** o ponto fica exatamente onde foi solto, sem nenhuma validação nem alerta

### Requirement: Entrada e saída gravam o índice do nó de traçado ancorado
Ao validar a entrada ou a saída de um `ObjetoEscapada` (na criação ou ao editar), o editor SHALL gravar o índice do nó de traçado (`No.getIndex()`, compartilhado entre `pistaFull`, `pista1Full` e `pista2Full`) em que esse ponto foi ancorado — `indiceEntrada` para o primeiro ponto, `indiceSaida` para o último. Esses índices SHALL ser usados para localizar a zona de escapada ao longo da volta, sem precisar recalcular a posição do nó mais próximo depois.

#### Scenario: Criar a escapada grava os índices de entrada e saída
- **WHEN** um `ObjetoEscapada` é criado com sucesso (entrada e saída validadas)
- **THEN** `indiceEntrada` e `indiceSaida` refletem os índices dos nós de traçado usados para ancorar cada ponta

#### Scenario: Editar a entrada ou a saída atualiza o índice correspondente
- **WHEN** o usuário arrasta a entrada ou a saída de um `ObjetoEscapada` existente para uma nova posição válida
- **THEN** `indiceEntrada` (se foi a entrada) ou `indiceSaida` (se foi a saída) é atualizado para o índice do novo nó ancorado

### Requirement: O carro de teste do editor (Teste Pista) segue o trajeto da escapada dentro do intervalo ancorado
Quando o modo de escapada do carro de teste do editor (`TestePista`, ligado pelo checkbox "Testar Escapada") está ativo, e o índice atual do carro ao longo da pista cai dentro do intervalo `[indiceEntrada, indiceSaida]` de algum `ObjetoEscapada` do circuito, o carro de teste SHALL seguir o trajeto de `pontos` dessa escapada (interpolado ao longo do caminho) em vez da pista normal — precisamente porque esse intervalo é ancorado a nós reais do traçado em que a escapada foi definida. Fora desse intervalo, ou com o modo de escapada desligado, o carro de teste SHALL continuar na pista normal, como antes desta mudança. Esse comportamento SHALL refletir criações e edições feitas na mesma sessão do editor, sem exigir salvar nem recarregar o circuito.

#### Scenario: Índice de entrada ativa o trajeto da escapada
- **WHEN** o modo de escapada do carro de teste está ligado e o índice atual do carro é igual a `indiceEntrada` de um `ObjetoEscapada`
- **THEN** a posição do carro de teste passa a ser o primeiro ponto do trajeto dessa escapada

#### Scenario: Índice de saída corresponde ao fim do trajeto da escapada
- **WHEN** o modo de escapada do carro de teste está ligado e o índice atual do carro é igual a `indiceSaida` de um `ObjetoEscapada`
- **THEN** a posição do carro de teste é o último ponto do trajeto dessa escapada

#### Scenario: Fora do intervalo, o carro de teste usa a pista normal
- **WHEN** o índice atual do carro de teste está fora de `[indiceEntrada, indiceSaida]` de qualquer `ObjetoEscapada` do circuito
- **THEN** a posição do carro de teste vem da pista normal, independente do modo de escapada estar ligado ou desligado

#### Scenario: Criar ou editar a escapada na mesma sessão já é reconhecido pelo Teste Pista
- **WHEN** o usuário cria ou termina de editar (soltar o mouse após arrastar) a entrada ou a saída de um `ObjetoEscapada`, e em seguida ativa o modo de escapada do carro de teste, sem salvar nem recarregar o circuito
- **THEN** o carro de teste já segue o trajeto atualizado da escapada quando seu índice cai no novo intervalo `[indiceEntrada, indiceSaida]`

### Requirement: Consumo de escapada em corrida (Piloto.java) permanece fora de escopo
A lógica de escapada em `Piloto.java` (`processaEscapadaDaPista()` e a leitura de `circuito.getEscapeMap()`) SHALL NOT ser alterada por esta mudança — só o carro de teste do próprio editor (`TestePista`, ver requisito acima) foi reconectado ao novo modelo. `Circuito.getEscapeMap()` SHALL continuar existindo como método, retornando sempre um mapa vazio, para que o código de `Piloto.java` continue compilando e executando sem exceção, mesmo sem nenhum dado de escapada disponível por essa via.

#### Scenario: Corrida com o novo modelo de escapada não usa dados de escapada
- **WHEN** uma corrida (fora do editor) é simulada em um circuito contendo `ObjetoEscapada` no novo formato
- **THEN** `Piloto.processaEscapadaDaPista()` executa normalmente, sem exceção, e `circuito.getEscapeMap()` retorna um mapa vazio, sem que nenhum piloto escape usando o novo objeto nesta mudança

### Requirement: Salvar o circuito é bloqueado se houver ObjetoEscapada incompleta
`MainPanelEditor.salvarPista()` SHALL verificar, antes de gravar o circuito em arquivo, se todo `ObjetoEscapada` presente em `circuito.getObjetos()` tem um ponto de saída ancorado (`getIndiceSaida() != -1`). Quando existir pelo menos uma `ObjetoEscapada` com `indiceSaida == -1`, o salvamento SHALL ser abortado e um alerta (`JOptionPane`, via chave `Lang.msg(...)`) SHALL ser exibido informando que há uma escapada incompleta, seguindo o mesmo padrão de método `protected` overridable de `alertaPontoEscapadaInvalido()` (com override correspondente em `MainPanelEditorTestDouble` para testes).

#### Scenario: Salvar com escapada sem saída definida é bloqueado
- **WHEN** o usuário clica em salvar um circuito que contém uma `ObjetoEscapada` cuja saída nunca foi finalizada (`indiceSaida == -1`)
- **THEN** o circuito não é gravado em arquivo, e um alerta é exibido informando que existe uma escapada incompleta

#### Scenario: Salvar com todas as escapadas completas prossegue normalmente
- **WHEN** o usuário clica em salvar um circuito em que toda `ObjetoEscapada` presente tem `indiceEntrada` e `indiceSaida` ambos definidos (`!= -1`)
- **THEN** a verificação de escapada incompleta não bloqueia o salvamento, e o fluxo de `salvarPista()` prossegue normalmente (demais validações e gravação em arquivo)

#### Scenario: Circuito sem nenhuma ObjetoEscapada não é afetado
- **WHEN** o usuário clica em salvar um circuito que não contém nenhuma `ObjetoEscapada`
- **THEN** a verificação de escapada incompleta não bloqueia o salvamento
