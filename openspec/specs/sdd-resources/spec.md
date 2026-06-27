# sdd-resources

## Purpose
Documents how game resources are loaded and cached by `CarregadorRecursos`, and how the i18n system resolves messages both in solo and multiplayer server mode.

## Requirements

### Requirement: CarregadorRecursos documentado
O SDD SHALL descrever `CarregadorRecursos` como singleton com cache de recursos.

#### Scenario: Singleton e cache descritos
- **WHEN** o leitor consulta o carregamento de recursos
- **THEN** o SDD descreve que `CarregadorRecursos` é singleton obtido via `getCarregadorRecursos(cache: boolean)`; mantém caches estáticos: `bufferImages`, `bufferCarros`, `bufferCircuitos: Map<String,Circuito>`, `bufferCapacete: Map<String,BufferedImage>`

#### Scenario: Carregamento de circuitos descrito
- **WHEN** o leitor consulta como circuitos são carregados
- **THEN** o SDD descreve que circuitos são definidos em XML (37 arquivos em `src/main/resources/circuitos/`, formato `{nome}_mro.xml`) e desserializados com `XMLDecoder`; `carregarCircuito(nome)` retorna um `Circuito` cacheado

#### Scenario: Carregamento de pilotos e carros descrito
- **WHEN** o leitor consulta como dados de temporada são carregados
- **THEN** o SDD descreve que pilotos e carros vivem em `src/main/resources/properties/tANO/pilotos.properties` e `carros.properties`; a lista de temporadas é carregada de `properties/temporadas.properties` via `carregarTemporadas()`

### Requirement: Sistema de i18n documentado
O SDD SHALL descrever como a internacionalização funciona em modo local e servidor.

#### Scenario: Resolução de mensagens descrita
- **WHEN** o leitor consulta o sistema de i18n
- **THEN** o SDD descreve que `Lang.msg(key)` resolve em `PropertyResourceBundle` do bundle do idioma atual (`mensagens_XX.properties` em `src/main/resources/idiomas/`); em modo servidor (`srvgame=true`) retorna `"¢key¢"` para tradução lazy no cliente; `Lang.mudarIdioma(sufix)` troca o bundle em runtime; o header HTTP `idioma` define o idioma por request no servidor

#### Scenario: Idiomas disponíveis documentados
- **WHEN** o leitor consulta os idiomas suportados
- **THEN** o SDD lista os bundles disponíveis em `src/main/resources/idiomas/` e descreve que o cliente web envia o idioma no header `idioma` e a tradução ocorre no servidor antes de retornar a resposta
