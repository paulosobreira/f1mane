# circuito-ativo

## Purpose

Permitir que circuitos sejam marcados como ativos ou inativos, de forma que circuitos inativos permaneçam editáveis no editor de circuitos mas fiquem ocultos das superfícies de seleção de circuito em jogo (solo, multiplayer, endpoint REST).

## Requirements

### Requirement: Circuito tem propriedade ativo com valor padrão false
`Circuito` SHALL expor uma propriedade booleana `ativo` (`isAtivo()`/`setAtivo(boolean)`), persistida via `XMLEncoder`/`XMLDecoder` do mesmo jeito que `noite`/`usaBkg`, com valor padrão `false` quando não definida explicitamente.

#### Scenario: Circuito recém-criado nasce inativo
- **WHEN** um novo `Circuito` é criado pelo editor (fluxo "Criar Nova") sem que o usuário marque explicitamente a propriedade ativo
- **THEN** `circuito.isAtivo()` retorna `false`

#### Scenario: XML sem a propriedade ativo carrega como inativo
- **WHEN** um arquivo XML de circuito existente é decodificado por `XMLDecoder` e não contém `<void property="ativo">`
- **THEN** o `Circuito` resultante tem `isAtivo()` retornando `false`, pelo comportamento padrão do construtor sem argumentos

### Requirement: Circuitos inativos não aparecem nos seletores em jogo
O sistema SHALL excluir circuitos com `isAtivo()` igual a `false` de todas as superfícies de seleção de circuito em jogo — combo solo (`GerenciadorVisual.gerarSeletorCircuito()`), combo do cliente multiplayer (`PainelEntradaCliente`) e endpoint REST `GET /circuitos` (`LetsRace.circuitos()`/`CarregadorRecursos.carregarCircuitosDefaults()`) — mantendo-os abríveis e editáveis no editor de circuitos.

#### Scenario: Seletor solo exclui circuito inativo
- **WHEN** `GerenciadorVisual.gerarSeletorCircuito()` monta o combo box de circuitos e um circuito tem `ativo=false`
- **THEN** esse circuito não aparece como opção no combo box

#### Scenario: Cliente multiplayer exclui circuito inativo
- **WHEN** `PainelEntradaCliente` monta seu combo de circuitos e um circuito tem `ativo=false`
- **THEN** esse circuito não aparece como opção no combo

#### Scenario: Endpoint REST exclui circuito inativo
- **WHEN** o endpoint `GET /circuitos` é chamado e um circuito tem `ativo=false`
- **THEN** esse circuito não está presente na lista retornada

#### Scenario: Editor continua exibindo circuito inativo
- **WHEN** o usuário navega até um circuito com `ativo=false` pelos botões Anterior/Próximo do editor de circuitos
- **THEN** o circuito é carregado e fica editável normalmente, apesar de não aparecer nos seletores em jogo

### Requirement: Migração dos circuitos existentes para ativo=true
Como o valor padrão de `ativo` passa a ser `false`, todo circuito hoje jogável em `src/main/resources/circuitos/*.xml` SHALL ter `ativo=true` definido explicitamente como parte desta mudança, para permanecer selecionável em jogo após a mudança entrar em vigor.

#### Scenario: Circuito existente permanece selecionável após a migração
- **WHEN** um circuito que já era jogável antes desta mudança é carregado após a migração (com `ativo=true` gravado em seu XML)
- **THEN** ele continua aparecendo nos seletores solo, multiplayer e no endpoint REST, exatamente como antes da mudança
