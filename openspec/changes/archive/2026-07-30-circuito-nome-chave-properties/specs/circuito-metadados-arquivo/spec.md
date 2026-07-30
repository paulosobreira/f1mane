## MODIFIED Requirements

### Requirement: Salvar circuito grava metadados+traçado num arquivo e objetos de cenário em outro
Ao salvar um circuito pelo editor (`MainPanelEditor.salvarPista()`), o sistema SHALL gravar dois arquivos: `<nome>_mro_meta.xml`, contendo `noite`, `usaBkg`, `probalidadeChuva`, `velocidadePista`, `ladoBox`, `ladoBoxSaidaBox`, `corFundo`, `corAsfalto`, `corBox1`, `corBox2`, `corZebra1`, `corZebra2`, `pista` e `box`; e `<nome>_mro.xml`, contendo apenas `objetos` e `objetosCenario`. `ativo` SHALL NOT ser gravado em nenhum dos dois (ver requisito de `ativo` em `circuitos.properties`). `nome` SHALL NOT ser gravado em nenhum dos dois (ver capability `circuito-nome-exibicao` — o nome de exibição vive em `circuitos.properties`). Nenhum dos dois arquivos SHALL conter os campos que pertencem ao outro.

#### Scenario: Salvar grava os dois arquivos
- **WHEN** o usuário salva um circuito no editor
- **THEN** o sistema grava `<nome>_mro_meta.xml` (noite/usaBkg/probalidadeChuva/velocidadePista/ladoBox/ladoBoxSaidaBox/cores/pista/box) e `<nome>_mro.xml` (objetos/objetosCenario), sem `ativo` nem `nome` em nenhum dos dois, e sem duplicar os campos do outro

#### Scenario: Nome do arquivo de metadados segue convenção de sufixo
- **WHEN** o circuito é salvo com o arquivo de objetos nomeado `<nome>_mro.xml`
- **THEN** o arquivo de metadados correspondente é gravado como `<nome>_mro_meta.xml`, no mesmo diretório
