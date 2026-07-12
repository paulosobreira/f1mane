## ADDED Requirements

### Requirement: Editor de circuitos inicia sem nenhum circuito carregado
`MainPanelEditor` SHALL iniciar (`iniciarComNavegacao()`) sem carregar automaticamente nenhum circuito, mesmo quando `circuitosXml` (a lista populada a partir de `properties/circuitos.properties`) não está vazia. O comportamento SHALL ser o mesmo já usado hoje para o caso de lista vazia: `indiceCircuito = -1` e nenhuma chamada a `carregarCircuitoExistente(...)`.

#### Scenario: Editor abre com circuitos disponíveis mas nenhum é carregado
- **WHEN** o editor de circuitos é iniciado e `properties/circuitos.properties` lista um ou mais circuitos
- **THEN** nenhum circuito é carregado automaticamente, e nenhuma chamada a `carregarCircuitoExistente(...)` ocorre como parte da inicialização

#### Scenario: Editor abre sem nenhum circuito disponível
- **WHEN** o editor de circuitos é iniciado e `properties/circuitos.properties` não lista nenhum circuito
- **THEN** o comportamento é o mesmo de antes desta mudança: `indiceCircuito = -1` e nenhum circuito carregado

### Requirement: Combo box reflete o estado "nenhum circuito carregado"
O combo box de seleção de circuito (`comboCircuito`) SHALL refletir visualmente o estado "nenhum circuito carregado" logo após a inicialização, em vez de aparecer com o primeiro circuito da lista pré-selecionado. `repopularComboCircuitos()`/`atualizarBotoesNavegacao()` SHALL suportar `indiceCircuito == -1` mesmo quando `circuitosXml` não está vazio, sem forçar `comboCircuito.setSelectedIndex(0)`.

#### Scenario: Combo box sem seleção após inicialização
- **WHEN** o editor termina de inicializar com um ou mais circuitos disponíveis
- **THEN** `comboCircuito` não tem nenhum circuito real selecionado (nenhum índice `>= 0` correspondente a um circuito de `circuitosXml` está marcado como selecionado)

#### Scenario: Selecionar um circuito no combo box carrega-o normalmente
- **WHEN** o usuário, com o editor no estado inicial "nenhum circuito carregado", seleciona um circuito no `comboCircuito`
- **THEN** o circuito escolhido é carregado via `carregarCircuitoExistente(...)`, exatamente como a seleção manual já funcionava antes desta mudança

### Requirement: Renderização do editor tolera um circuito vazio
Com o editor podendo iniciar sem nenhum circuito carregado (um `Circuito` recém-criado, com `pistaKey`/`boxKey` vazios), `MainPanelEditor.paintComponent()` e as rotinas de desenho que ele chama (em particular `DesenhoProceduralCircuito.desenhaPista()` e `desenhaTintaPistaEZebra()`) SHALL NOT lançar exceção nesse estado — SHALL simplesmente não desenhar o traçado, da mesma forma que já acontecia para o traçado do box.

#### Scenario: Tela do editor renderiza sem erro com circuito vazio
- **WHEN** o editor está no estado inicial sem nenhum circuito carregado e a tela é repintada
- **THEN** nenhuma exceção é lançada, e nenhum traçado de pista/zebra/box é desenhado
