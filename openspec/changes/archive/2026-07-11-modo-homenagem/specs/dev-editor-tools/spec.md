## MODIFIED Requirements

### Requirement: Car color editor persists to source resources
`EditorCarrosPilotos` (renomeado de `EditorCoresCarros`) SHALL read and write `carros.properties` directly under `src/main/resources/properties/<temporada>/carros.properties`, resolved relative to the project root working directory, and SHALL NOT resolve the file to save via the runtime classpath (`Class.getResource(...)`/`target/classes`/jar entries). When editing a car, the editor SHALL also read and write the `nomeHomenagem` field (11th positional column).

#### Scenario: Saving a single car's colors
- **WHEN** the user edits a car's colors in `EditorCarrosPilotos` and clicks the save button (💾 icon)
- **THEN** the corresponding line in `src/main/resources/properties/<temporada>/carros.properties` is rewritten with the new values, and the change is visible via `git diff` in that file

#### Scenario: Saving all cars in a season
- **WHEN** the user clicks "Salvar Todos" for the currently loaded season
- **THEN** `src/main/resources/properties/<temporada>/carros.properties` is rewritten in place with every modified car's updated values, preserving comments and line order

#### Scenario: Target file missing at expected source path
- **WHEN** `src/main/resources/properties/<temporada>/carros.properties` does not exist relative to the working directory (e.g., tool launched from outside the project root)
- **THEN** the save operation fails with a clear error message shown to the user instead of writing to an unrelated location such as `target/classes`

#### Scenario: Editing a car's tribute name
- **WHEN** the user edits the `nomeHomenagem` field for a car in `EditorCarrosPilotos` and saves
- **THEN** the corresponding line in `carros.properties` has its 11th field updated to the new value, without altering the other 10 fields

### Requirement: Car color editor uses previous/next season navigation
`EditorCarrosPilotos` SHALL let the user switch between seasons using "previous" and "next" navigation controls instead of a dropdown combo box, operating over the same ordered list of available seasons, shared between the "carros" and "pilotos" editing modes.

#### Scenario: Navigating to the next season
- **WHEN** the user clicks the "next season" control while a season other than the last is selected
- **THEN** the editor loads and displays the next season in the ordered list, and the "next season" control is disabled when the last season is reached

#### Scenario: Navigating to the previous season
- **WHEN** the user clicks the "previous season" control while a season other than the first is selected
- **THEN** the editor loads and displays the previous season in the ordered list, and the "previous season" control is disabled when the first season is reached

## ADDED Requirements

### Requirement: Editor de carros/pilotos tem um seletor de modo de edição

`EditorCarrosPilotos` SHALL expor um seletor (ex.: toggle ou combobox) no topo da janela pra alternar entre editar "Carros" (comportamento existente, com o campo `nomeHomenagem` novo) e editar "Pilotos" (nova visão). A troca de modo SHALL preservar a temporada atualmente selecionada.

#### Scenario: Alternar pro modo de edição de pilotos
- **WHEN** o usuário seleciona "Pilotos" no seletor de modo, estando numa temporada X
- **THEN** o editor exibe um card por piloto daquela mesma temporada X, sem precisar navegar de novo pra ela

#### Scenario: Alternar de volta pro modo de edição de carros
- **WHEN** o usuário seleciona "Carros" no seletor de modo, estando numa temporada X
- **THEN** o editor volta a exibir um card por carro daquela mesma temporada X, com os campos de cor e `nomeHomenagem` como antes

### Requirement: Editor de pilotos edita apenas `nomeHomenagem` e `habilidade`, com imagem somente leitura

No modo de edição de pilotos, `EditorCarrosPilotos` SHALL exibir, por piloto da temporada selecionada, a imagem do carro e do capacete daquele piloto (com as cores reais do carro associado) como somente leitura, e SHALL permitir editar apenas `nomeHomenagem` e `habilidade` (a habilidade daquele piloto naquela temporada).

#### Scenario: Editar o nome-homenagem de um piloto
- **WHEN** o usuário edita o campo `nomeHomenagem` de um piloto no modo de edição de pilotos e salva
- **THEN** a linha correspondente em `pilotos.properties` daquela temporada tem seu 3º campo atualizado, sem alterar `Carro`/`Habilidade`

#### Scenario: Editar a habilidade de um piloto
- **WHEN** o usuário edita o campo `habilidade` de um piloto no modo de edição de pilotos e salva
- **THEN** a linha correspondente em `pilotos.properties` daquela temporada tem seu 2º campo (`Habilidade`) atualizado

#### Scenario: Imagem do piloto não é editável
- **WHEN** o usuário está no modo de edição de pilotos
- **THEN** não há nenhum controle pra alterar a imagem/cor do carro ou capacete exibidos pro piloto — só campos de texto pra `nomeHomenagem` e `habilidade`

### Requirement: Alteração de `nomeHomenagem` pode ser propagada pra todas as temporadas da mesma identidade

Sempre que o usuário alterar o campo `nomeHomenagem` de um carro ou de um piloto, `EditorCarrosPilotos` SHALL exibir um checkbox (ex.: "Aplicar a todas as temporadas") próximo ao campo. Quando o usuário salvar com o checkbox marcado, a mudança SHALL ser aplicada a `nomeHomenagem` de toda linha, em qualquer `carros.properties`/`pilotos.properties` de qualquer temporada, que compartilhe a mesma identidade do item editado (nome canônico da imagem, pra carro; chave real, pra piloto — ver `nome-homenagem-carros`/`nome-homenagem-pilotos`). Quando o checkbox estiver desmarcado (padrão), a mudança SHALL afetar apenas a linha da temporada atualmente selecionada, como hoje.

#### Scenario: Salvar só pra temporada atual (checkbox desmarcado)
- **WHEN** o usuário edita `nomeHomenagem` de um carro/piloto, deixa o checkbox desmarcado, e salva
- **THEN** só a linha correspondente na temporada atualmente selecionada é atualizada; outras temporadas com a mesma identidade permanecem com o valor anterior

#### Scenario: Propagar pra todas as temporadas (checkbox marcado) — carro
- **WHEN** o usuário edita `nomeHomenagem` de um carro cujo nome de imagem canonicaliza pra `mclaren` (ex.: presente como `McLaren-MP4/1` numa temporada e `McLaren-MP4/4` noutra), marca o checkbox, e salva
- **THEN** toda linha de carro, em toda temporada, cujo nome de imagem também canonicalize pra `mclaren` tem seu `nomeHomenagem` atualizado pro mesmo novo valor

#### Scenario: Propagar pra todas as temporadas (checkbox marcado) — piloto
- **WHEN** o usuário edita `nomeHomenagem` de um piloto com uma chave real que aparece em `pilotos.properties` de múltiplas temporadas, marca o checkbox, e salva
- **THEN** toda linha de piloto, em toda temporada, com essa mesma chave real tem seu `nomeHomenagem` atualizado pro mesmo novo valor

#### Scenario: Propagação não afeta outros campos
- **WHEN** a propagação pra todas as temporadas é aplicada
- **THEN** apenas o campo `nomeHomenagem` das linhas correspondentes é alterado — cores, potência, imagem, carro associado, habilidade, e demais campos de cada linha permanecem intocados
