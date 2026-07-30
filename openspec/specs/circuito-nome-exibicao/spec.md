# circuito-nome-exibicao Specification

## Purpose
TBD - created by syncing change circuito-nome-chave-properties. Update Purpose after archive.

## Requirements

### Requirement: Nome de exibição do circuito é resolvido a partir de circuitos.properties, não do XML
`Circuito.getNome()` SHALL retornar o valor do primeiro campo (nome de exibição) da linha correspondente em `circuitos.properties` (`<arquivoXml>=<NomeExibicao>,<ativo>`), populado por `CarregadorRecursos.carregarCircuito()` no mesmo momento em que `ativo` já é populado a partir desse arquivo. O setter de `nome` em `Circuito` SHALL deixar de seguir o padrão JavaBean `setNome` (renomeado para uma convenção fora do padrão, ex. `definirNomePorConvencao`), de forma que `java.beans.XMLEncoder` não persista mais essa propriedade em `<nome>_mro_meta.xml`.

#### Scenario: Nome vem de circuitos.properties, não do XML
- **WHEN** `CarregadorRecursos.carregarCircuito(nmCircuito)` é chamado para um circuito cuja linha em `circuitos.properties` tem nome de exibição "Albert Park"
- **THEN** `circuito.getNome()` retorna "Albert Park", independentemente do que estiver (ou não) gravado em `<nome>_mro_meta.xml`

#### Scenario: XML de metadados salvo não contém mais a propriedade nome
- **WHEN** um circuito é salvo pelo editor após esta mudança
- **THEN** `<nome>_mro_meta.xml` não contém a propriedade `nome`

### Requirement: Editar o nome no editor grava em circuitos.properties e recarrega o arquivo
Ao salvar um circuito no editor (`MainPanelEditor.salvarPista()`), quando o campo de nome (`nomePistaText`) tiver sido alterado, o sistema SHALL gravar o novo nome de exibição na linha correspondente de `circuitos.properties` (preservando o campo `ativo` da mesma linha), e em seguida recarregar esse valor diretamente do arquivo-fonte (não do cache/classpath), atualizando `nomePistaText` e `circuito` com o valor relido — mesmo padrão já usado para `ativo` via `lerAtivoDaFonte`, evitando que a UI mostre um valor diferente do que foi de fato persistido.

#### Scenario: Salvar novo nome atualiza circuitos.properties
- **WHEN** o usuário altera o texto de `nomePistaText` e salva o circuito
- **THEN** a linha correspondente em `circuitos.properties` passa a ter esse novo nome de exibição, mantendo o valor de `ativo` que já estava na linha

#### Scenario: UI reflete o valor relido do arquivo, não só o texto digitado
- **WHEN** o circuito é salvo e `circuitos.properties` é reescrito com o novo nome
- **THEN** o editor relê o arquivo-fonte e `nomePistaText` exibe o valor relido dessa linha, não apenas o texto que estava no campo antes de salvar

### Requirement: Nomes de circuito não passam mais por distorção de vogais
`Util.substVogais` (flag e método) SHALL ser removido do código-fonte. Nenhum ponto do sistema (menu local, HUD durante a corrida, listagem de circuitos default, servidor multiplayer/campeonato) SHALL aplicar qualquer transformação de vogais sobre o nome de um circuito — o nome exibido SHALL ser sempre o valor puro resolvido de `circuitos.properties`.

#### Scenario: Nome exibido no menu é o nome puro
- **WHEN** o menu local lista os circuitos disponíveis
- **THEN** o nome de cada circuito exibido é exatamente o valor de `circuitos.properties`, sem nenhuma vogal trocada

#### Scenario: Nome exibido no HUD da corrida é o nome puro
- **WHEN** uma corrida está em andamento e o HUD exibe o nome do circuito
- **THEN** o nome exibido é exatamente `circuito.getNome()`, sem passar por `Util.substVogais` (método que não existe mais)

#### Scenario: Argumento de linha de comando "real" deixa de ter efeito sobre nomes
- **WHEN** `MainFrame` é iniciado com o argumento `"real"`
- **THEN** esse argumento não altera mais a exibição de nomes de circuito (a distinção "modo real vs. distorcido" deixa de existir)
