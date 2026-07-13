## MODIFIED Requirements

### Requirement: Objetos de cenário e de função ficam em uma lista única no editor
`Circuito` SHALL manter os objetos de cenário (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`) numa lista própria (`objetosCenario`), distinta da lista `objetos` que continua contendo `ObjetoEscapada` e `ObjetoTransparencia` — essa separação em duas coleções no modelo de dados não muda. O editor de circuitos SHALL, no entanto, exibir e gerenciar `objetos` e `objetosCenario` como uma única lista visual dentro da seção de objetos do `JSplitPane` lateral compartilhado com a seção de nós de pista/box (ver requisito "Editor consolida nós e objetos em um único split pane lateral"), em vez de duas listas separadas.

#### Scenario: Criar um objeto de cenário adiciona à coleção correta
- **WHEN** o usuário cria um `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` ou `ObjetoPneus` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetosCenario()`, e aparece na lista única de objetos dentro da seção de objetos do split pane

#### Scenario: Criar Escapada ou Transparência continua indo para a coleção objetos
- **WHEN** o usuário cria um `ObjetoEscapada` ou `ObjetoTransparencia` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetos()`, como antes desta mudança, e aparece na mesma lista única de objetos dentro da seção de objetos do split pane, junto dos objetos de cenário

### Requirement: Ações de objeto ficam agrupadas na seção de objetos do split pane
As ações relativas a objetos de pista (criar via "Criar Objeto", remover, reordenar via Cima/Baixo/Primeiro/Ultimo, editar) SHALL ficar agrupadas dentro da seção de objetos do split pane, operando sobre a lista única de objetos (`objetos` e `objetosCenario` combinados) através de um único `FormularioListaObjetos`, em vez do botão "Criar Objeto" ficar em um painel de botões separado fora do split pane ou de dois `FormularioListaObjetos` independentes.

#### Scenario: Botão Criar Objeto aparece junto à lista de objetos
- **WHEN** o usuário abre a seção de objetos do split pane
- **THEN** o controle "Criar Objeto" está visível dentro dessa seção, junto da lista única de objetos e seus botões Cima/Baixo/Primeiro/Ultimo/Editar/Remover
