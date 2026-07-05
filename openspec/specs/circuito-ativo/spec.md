# circuito-ativo

## Purpose

Permitir que circuitos sejam marcados como ativos ou inativos, de forma que circuitos inativos permaneçam editáveis no editor de circuitos mas fiquem ocultos das superfícies de seleção de circuito em jogo (solo, multiplayer, endpoint REST).

## Requirements

### Requirement: Circuito tem propriedade ativo com valor padrão false
`Circuito` SHALL expor uma propriedade booleana `ativo` (`isAtivo()`/`setAtivo(boolean)`), com valor padrão `false` quando não definida explicitamente. Essa propriedade SHALL ser persistida como o terceiro campo CSV da linha correspondente em `properties/circuitos.properties` (`<arquivo>_mro.xml=<NomeExibicao>,<ciclo>,<ativo>`), e NÃO SHALL ser serializada pelo `XMLEncoder`/`XMLDecoder` do circuito (nem em `<nome>_mro_meta.xml`, nem em `<nome>_mro.xml`) — diferente de `noite`/`usaBkg`, que continuam persistidos no XML do circuito.

#### Scenario: Circuito recém-criado nasce inativo
- **WHEN** um novo `Circuito` é criado pelo editor (fluxo "Criar Nova") sem que o usuário marque explicitamente a propriedade ativo
- **THEN** `circuito.isAtivo()` retorna `false`

#### Scenario: Circuito sem entrada em circuitos.properties carrega como inativo
- **WHEN** um circuito é carregado (`carregarCircuito()`) e não há linha correspondente em `properties/circuitos.properties`, ou a linha não tem terceiro campo
- **THEN** o `Circuito` resultante tem `isAtivo()` retornando `false`

#### Scenario: XML do circuito não influencia mais o valor de ativo
- **WHEN** um arquivo `<nome>_mro_meta.xml`/`<nome>_mro.xml` (ou, para circuito não migrado, um `_mro.xml` no formato antigo) contém `<void property="ativo">` com um valor qualquer
- **THEN** `carregarCircuito()` ignora esse valor e usa exclusivamente o terceiro campo da linha correspondente em `properties/circuitos.properties` para popular `isAtivo()`

### Requirement: Circuitos inativos não aparecem nos seletores em jogo
O sistema SHALL excluir circuitos com terceiro campo (`ativo`) igual a `false` (ou ausente) em `properties/circuitos.properties` de todas as superfícies de seleção de circuito em jogo — combo solo (`GerenciadorVisual.gerarSeletorCircuito()`), combo do cliente multiplayer (`PainelEntradaCliente`) e endpoint REST `GET /circuitos` (`LetsRace.circuitos()`/`CarregadorRecursos.carregarCircuitosDefaults()`) — mantendo-os abríveis e editáveis no editor de circuitos.

#### Scenario: Seletor solo exclui circuito inativo
- **WHEN** `GerenciadorVisual.gerarSeletorCircuito()` monta o combo box de circuitos e a linha de um circuito em `circuitos.properties` tem o terceiro campo `false`
- **THEN** esse circuito não aparece como opção no combo box

#### Scenario: Cliente multiplayer exclui circuito inativo
- **WHEN** `PainelEntradaCliente` monta seu combo de circuitos e a linha de um circuito em `circuitos.properties` tem o terceiro campo `false`
- **THEN** esse circuito não aparece como opção no combo

#### Scenario: Endpoint REST exclui circuito inativo
- **WHEN** o endpoint `GET /circuitos` é chamado e a linha de um circuito em `circuitos.properties` tem o terceiro campo `false`
- **THEN** esse circuito não está presente na lista retornada

#### Scenario: Editor continua exibindo circuito inativo
- **WHEN** o usuário navega até um circuito com `ativo=false` (em `circuitos.properties`) pelos botões Anterior/Próximo do editor de circuitos
- **THEN** o circuito é carregado e fica editável normalmente, apesar de não aparecer nos seletores em jogo

### Requirement: Migração dos circuitos existentes para ativo=true
Como `ativo` deixa de ser lido do XML do circuito, todo circuito hoje jogável (com `ativo=true` gravado no seu XML antes desta mudança) SHALL ter esse valor migrado para o terceiro campo da linha correspondente em `properties/circuitos.properties`, para permanecer selecionável em jogo após a mudança entrar em vigor.

#### Scenario: Circuito existente permanece selecionável após a migração
- **WHEN** um circuito que já era jogável antes desta mudança (`ativo=true` no XML) é carregado após a migração (com `ativo=true` migrado para `circuitos.properties`)
- **THEN** ele continua aparecendo nos seletores solo, multiplayer e no endpoint REST, exatamente como antes da mudança

### Requirement: Editor grava ativo em circuitos.properties ao salvar
Ao salvar um circuito (`MainPanelEditor.salvarPista()`) cujo arquivo já possui uma linha em `properties/circuitos.properties`, o sistema SHALL atualizar o terceiro campo CSV dessa linha com o valor atual do checkbox "Ativo" do editor, preservando o `NomeExibicao`/`ciclo` já existentes na linha e todas as demais linhas do arquivo (ordem e conteúdo inalterados).

#### Scenario: Salvar atualiza o terceiro campo da linha do circuito
- **WHEN** o usuário marca/desmarca o checkbox "Ativo" no editor e salva o circuito
- **THEN** a linha correspondente em `circuitos.properties` passa a ter o novo valor de `ativo` como terceiro campo, com `NomeExibicao`/`ciclo` inalterados

#### Scenario: Salvar não altera outras linhas de circuitos.properties
- **WHEN** um circuito é salvo pelo editor
- **THEN** as linhas de todos os outros circuitos em `circuitos.properties` permanecem byte-a-byte iguais às de antes do salvamento
