## MODIFIED Requirements

### Requirement: Objetos de cenário ficam numa lista separada, como pista e box
`Circuito` SHALL manter os objetos de cenário (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`) numa lista própria (`objetosCenario`), distinta da lista `objetos` que continua contendo `ObjetoEscapada` e `ObjetoTransparencia`. O editor de circuitos SHALL exibir e gerenciar as listas `objetos` e `objetosCenario` dentro da seção de objetos de um único `JSplitPane` lateral compartilhado com a seção de nós de pista/box (ver requisito "Editor consolida nós e objetos em um único split pane lateral"), em vez de painéis EAST/WEST separados.

#### Scenario: Criar um objeto de cenário adiciona à lista correta
- **WHEN** o usuário cria um `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` ou `ObjetoPneus` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetosCenario()`, e aparece na lista dedicada a objetos de cenário dentro da seção de objetos do split pane, não na lista `objetos`

#### Scenario: Criar Escapada ou Transparência continua indo para a lista objetos
- **WHEN** o usuário cria um `ObjetoEscapada` ou `ObjetoTransparencia` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetos()`, como antes desta mudança, e aparece na lista `objetos` dentro da mesma seção de objetos do split pane

## ADDED Requirements

### Requirement: Editor consolida nós e objetos em um único split pane lateral
O editor de circuitos SHALL apresentar as listas de nós (`pistaJList`/`boxJList`) e as listas de objetos de pista (`objetos`/`objetosCenario`) em um único `JSplitPane` posicionado em uma única lateral da janela (não mais em painéis WEST e EAST separados), com uma seção dedicada a nós e outra dedicada a objetos.

#### Scenario: Nós e objetos compartilham a mesma lateral
- **WHEN** o editor de circuitos é aberto
- **THEN** as listas de nós e as listas de objetos aparecem dentro do mesmo `JSplitPane`, em uma única lateral da janela, com um divisor ajustável entre a seção de nós e a seção de objetos

### Requirement: Ações de nó ficam agrupadas na seção de nós do split pane
As ações relativas à edição de nós (inserir nó via clique com tipo selecionado, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada", remoção via tecla Delete na lista) SHALL ficar agrupadas dentro da seção de nós do split pane, em vez de espalhadas em um painel de botões separado (`buttonsPanel`) fora do split pane.

#### Scenario: Botões de ação de nó aparecem junto às listas de nó
- **WHEN** o usuário abre a seção de nós do split pane
- **THEN** os controles "Apagar Ultimo NO" e "Apaga Nó na lista Selecionada" estão visíveis dentro dessa seção, junto das listas `pistaJList`/`boxJList`

### Requirement: Ações de objeto ficam agrupadas na seção de objetos do split pane
As ações relativas a objetos de pista (criar via "Criar Objeto", remover, reordenar via Cima/Baixo/Primeiro/Ultimo, editar) SHALL ficar agrupadas dentro da seção de objetos do split pane, reutilizando `FormularioListaObjetos` para as duas listas (`objetos` e `objetosCenario`), em vez de o botão "Criar Objeto" ficar em um painel de botões separado fora do split pane.

#### Scenario: Botão Criar Objeto aparece junto às listas de objeto
- **WHEN** o usuário abre a seção de objetos do split pane
- **THEN** o controle "Criar Objeto" está visível dentro dessa seção, junto das listas `objetos` e `objetosCenario` e seus botões Cima/Baixo/Primeiro/Ultimo/Editar/Remover
