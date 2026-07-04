# guard-rails-point-editing

## Purpose

Permitir que o editor de circuitos edite os pontos de um `ObjetoGuardRails` já criado — movendo, inserindo e removendo pontos do encadeamento diretamente no canvas — sem exigir apagar o objeto e recriá-lo do zero, e sem expor controles de curvatura (haste), já que os segmentos de guard rails são sempre retos.

## Requirements

### Requirement: Editor permite editar pontos de um ObjetoGuardRails já criado
O editor de circuitos SHALL permitir, para um `ObjetoGuardRails` selecionado em modo de edição de pontos (acionado a partir do menu de contexto/edição, como já ocorre para `ObjetoLivre`), mover um ponto existente arrastando-o, inserir um novo ponto no meio de um segmento, e remover um ponto existente — sem exigir apagar o objeto e desenhá-lo novamente do zero.

#### Scenario: Entrar em modo de edição de pontos de um guard rails
- **WHEN** o usuário aciona a edição de pontos para um `ObjetoGuardRails` já existente no circuito
- **THEN** o editor exibe cada ponto do encadeamento como alvo arrastável sobre o canvas, e nenhum novo ponto é adicionado ao final do encadeamento por esse acionamento

#### Scenario: Mover um ponto existente
- **WHEN** o usuário, em modo de edição de pontos de um `ObjetoGuardRails`, arrasta um ponto existente para uma nova posição no canvas
- **THEN** a posição desse ponto em `pontos` é atualizada, o caminho (`caminho`) é reconstruído e o objeto é redesenhado refletindo a nova posição, sem alterar a ordem dos demais pontos

#### Scenario: Inserir um ponto no meio de um segmento
- **WHEN** o usuário aciona a inserção de ponto sobre um segmento entre dois pontos consecutivos de um `ObjetoGuardRails` em modo de edição
- **THEN** um novo ponto é adicionado a `pontos` na posição correspondente entre os dois pontos do segmento, e o encadeamento passa a ter um segmento a mais sem alterar a posição dos pontos existentes

#### Scenario: Remover um ponto existente
- **WHEN** o usuário aciona a remoção de um ponto específico de um `ObjetoGuardRails` em modo de edição, e o encadeamento tem mais de dois pontos
- **THEN** esse ponto é removido de `pontos`, o caminho é reconstruído ligando diretamente os pontos vizinhos, e o objeto continua desenhável

#### Scenario: Remoção é bloqueada quando restariam menos de dois pontos
- **WHEN** o usuário tenta remover um ponto de um `ObjetoGuardRails` que possui exatamente dois pontos
- **THEN** a remoção não é efetuada e o encadeamento continua com seus dois pontos originais

### Requirement: Edição de pontos de ObjetoGuardRails não usa hastes de curvatura
Diferente da edição de pontos de `ObjetoLivre` (que usa `PontoCurva` com haste de curvatura), a edição de pontos de `ObjetoGuardRails` SHALL operar apenas sobre posições (`Point`), sem expor nenhum controle de curvatura, mantendo os segmentos sempre retos entre pontos consecutivos.

#### Scenario: Nenhum controle de haste aparece para guard rails
- **WHEN** o usuário está em modo de edição de pontos de um `ObjetoGuardRails`
- **THEN** o editor não exibe nem permite arrastar nenhuma ponta de haste de curvatura para esse objeto
