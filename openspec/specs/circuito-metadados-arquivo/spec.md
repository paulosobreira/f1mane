# circuito-metadados-arquivo

## Purpose

Separar a persistência de um circuito em dois arquivos — metadados+traçado (`<nome>_mro_meta.xml`) e objetos de cenário (`<nome>_mro.xml`) — para permitir leitura leve de metadados/traçado sem desserializar objetos de cenário, mantendo `carregarCircuito()` com o mesmo contrato de retorno de hoje (um `Circuito` totalmente populado e vetorizado).

## Requirements

### Requirement: Salvar circuito grava metadados+traçado num arquivo e objetos de cenário em outro
Ao salvar um circuito pelo editor (`MainPanelEditor.salvarPista()`), o sistema SHALL gravar dois arquivos: `<nome>_mro_meta.xml`, contendo `nome`, `noite`, `usaBkg`, `probalidadeChuva`, `velocidadePista`, `ladoBox`, `ladoBoxSaidaBox`, `corFundo`, `corAsfalto`, `corBox1`, `corBox2`, `corZebra1`, `corZebra2`, `pista` e `box`; e `<nome>_mro.xml`, contendo apenas `objetos` e `objetosCenario`. `ativo` SHALL NOT ser gravado em nenhum dos dois (ver requisito de `ativo` em `circuitos.properties`). Nenhum dos dois arquivos SHALL conter os campos que pertencem ao outro.

#### Scenario: Salvar grava os dois arquivos
- **WHEN** o usuário salva um circuito no editor
- **THEN** o sistema grava `<nome>_mro_meta.xml` (nome/noite/usaBkg/probalidadeChuva/velocidadePista/ladoBox/ladoBoxSaidaBox/cores/pista/box) e `<nome>_mro.xml` (objetos/objetosCenario), sem `ativo` em nenhum dos dois, e sem duplicar os campos do outro

#### Scenario: Nome do arquivo de metadados segue convenção de sufixo
- **WHEN** o circuito é salvo com o arquivo de objetos nomeado `<nome>_mro.xml`
- **THEN** o arquivo de metadados correspondente é gravado como `<nome>_mro_meta.xml`, no mesmo diretório

### Requirement: Campos derivados da pista não são persistidos em nenhum dos dois arquivos
`pistaFull`, `pista1Full`, `pista2Full`, `pista4Full`, `pista5Full`, `boxFull`, `box1Full`, `box2Full`, `pistaKey`, `boxKey`, `escapeMap`, `escapeList`, `entradaBoxIndex`, `saidaBoxIndex`, `paradaBoxIndex` e `fimParadaBoxIndex` SHALL NOT ser gravados em `<nome>_mro_meta.xml` nem em `<nome>_mro.xml` — esses campos SHALL continuar sendo recalculados em memória por `Circuito.vetorizarPista()` a partir de `pista`, `box`, `objetos` e dos multiplicadores, exatamente como já acontece hoje.

#### Scenario: Arquivos salvos não contêm campos derivados
- **WHEN** um circuito é salvo pelo editor
- **THEN** nem `<nome>_mro_meta.xml` nem `<nome>_mro.xml` contêm `pistaFull`, `pista1Full`, `pista2Full`, `pista4Full`, `pista5Full`, `boxFull`, `box1Full`, `box2Full`, `pistaKey`, `boxKey`, `escapeMap`, `escapeList`, `entradaBoxIndex`, `saidaBoxIndex`, `paradaBoxIndex` ou `fimParadaBoxIndex`

#### Scenario: Campos derivados continuam corretos após carregar
- **WHEN** um circuito é carregado após ser salvo no novo formato
- **THEN** `circuito.getPistaFull()`, `circuito.getBoxFull()` e os demais campos derivados retornam os mesmos valores que retornariam se calculados diretamente de `pista`/`box`/`objetos` por `vetorizarPista()`, sem depender de terem sido lidos de um arquivo

### Requirement: Carregar circuito mescla os dois arquivos, popula ativo e já entrega o circuito vetorizado
`CarregadorRecursos.carregarCircuito(String nmCircuito)` SHALL continuar retornando um único `Circuito` totalmente populado (mesma assinatura e contrato de hoje), lendo tanto `<nome>_mro_meta.xml` (quando existir) quanto `<nome>_mro.xml`, mesclando `objetos`/`objetosCenario` no `Circuito` que carrega os metadados/`pista`/`box`, populando `ativo` a partir de `circuitos.properties` (nunca do XML), e chamando `vetorizarPista()` sobre o resultado antes de retornar ou colocar em cache.

#### Scenario: Carregamento completo combina os dois arquivos, popula ativo e já sai vetorizado
- **WHEN** `carregarCircuito()` é chamado para um circuito que tem os dois arquivos (`_mro_meta.xml` e `_mro.xml`)
- **THEN** o `Circuito` retornado tem metadados, `pista`/`box`, `objetos`/`objetosCenario`, `ativo` (vindo de `circuitos.properties`) e todos os campos derivados (`pistaFull`, `boxFull`, etc.) corretamente populados, sem que o chamador precise invocar `vetorizarPista()` para obter esses valores

#### Scenario: Circuito no formato antigo (arquivo único) ainda carrega corretamente
- **WHEN** `carregarCircuito()` é chamado para um circuito cujo `_mro_meta.xml` não existe (ainda no formato de arquivo único anterior a esta mudança)
- **THEN** o `Circuito` retornado é populado inteiramente a partir de `_mro.xml`, tem `ativo` sobrescrito a partir de `circuitos.properties` e é vetorizado antes de retornar, com o mesmo resultado visível que o carregamento produzia antes desta mudança

### Requirement: Leitura rápida de traçado e metadados sem desserializar objetos de cenário
O sistema SHALL oferecer um caminho de leitura que obtém metadados e o traçado básico de um circuito (nome, cores, `pista`, `box`) lendo apenas `<nome>_mro_meta.xml`, sem decodificar `<nome>_mro.xml` nem chamar `vetorizarPista()`. `GerenciadorVisual.desenhaMiniCircuito()` SHALL usar esse caminho para obter `pista`/`box` ao desenhar a miniatura do circuito selecionado, em vez de `carregarCircuito()` completo.

#### Scenario: Leitura rápida expõe pista/box para a miniatura
- **WHEN** o arquivo `<nome>_mro_meta.xml` de um circuito é decodificado isoladamente
- **THEN** o objeto resultante tem `pista` e `box` populados com os mesmos nós autorados gravados pelo editor, suficientes para traçar o desenho simplificado que `desenhaMiniCircuito()` já produz hoje

#### Scenario: Miniatura não desserializa objetos de cenário
- **WHEN** o usuário seleciona um circuito no combo do menu solo e `desenhaMiniCircuito()` desenha a miniatura
- **THEN** o sistema não decodifica `<nome>_mro.xml` (objetos/objetosCenario) nem chama `vetorizarPista()` só para esse desenho

### Requirement: Circuitos existentes são migrados para os dois novos arquivos
Todo circuito XML já existente em `src/main/resources/circuitos/` no formato de arquivo único, no momento desta mudança, SHALL ser migrado para o novo par `<nome>_mro_meta.xml`/`<nome>_mro.xml`, preservando nome, cores, `pista`, `box`, `objetos` e `objetosCenario`, e descartando os campos derivados (que deixam de ter equivalente persistido). `ativo` SHALL ser migrado separadamente para `circuitos.properties` (ver capability `circuito-ativo`), não para nenhum dos dois arquivos.

#### Scenario: Circuito existente migrado carrega com os mesmos dados visíveis
- **WHEN** um circuito que já existia antes desta mudança é carregado após a migração completa (arquivos de circuito e `circuitos.properties`)
- **THEN** `carregarCircuito()` retorna os mesmos valores de metadados, `pista`, `box`, `objetos`, `objetosCenario` e `ativo` que retornava antes da migração, agora lidos a partir dos dois novos arquivos e de `circuitos.properties`, com os campos derivados recalculados em memória
