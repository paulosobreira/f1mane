## MODIFIED Requirements

### Requirement: Métodos sem chamadores são removidos
Métodos Java (private, public ou protected) que não possuem nenhum chamador em todo o codebase NÃO SHALL ser mantidos no código de produção.

#### Scenario: isClimaAleatorio removido de ControleClima
- **WHEN** `ControleClima.java` é lido
- **THEN** o método `isClimaAleatorio()` não existe

#### Scenario: getTipoJson removido de No
- **WHEN** `No.java` é lido
- **THEN** o método `getTipoJson()` não existe

#### Scenario: getCor2Hex removido de Carro
- **WHEN** `Carro.java` é lido
- **THEN** o método `getCor2Hex()` não existe

#### Scenario: exibirResiltadoFinal removido de MainFrameEditor
- **WHEN** `MainFrameEditor.java` é lido
- **THEN** o método `exibirResiltadoFinal(PainelTabelaResultadoFinal)` não existe

#### Scenario: verificaCarroLentoOuDanificado e acharPilotoDaFrente removidos de ControleCorrida
- **WHEN** `ControleCorrida.java` é lido
- **THEN** os métodos `verificaCarroLentoOuDanificado(Piloto)` e `acharPilotoDaFrente(Piloto)` não existem

#### Scenario: Setters órfãos removidos de ControleRecursos
- **WHEN** `ControleRecursos.java` é lido
- **THEN** os métodos `setMapaIdsNos(Map)` e `setMapaNosIds(Map)` não existem

#### Scenario: escapaTracado e processaPontoEscape removidos de Piloto
- **WHEN** `Piloto.java` é lido
- **THEN** os métodos `escapaTracado()` e `processaPontoEscape()` não existem (o gatilho geométrico antigo, permanentemente inerte desde que `Circuito.gerarEscapeMap()` passou a manter `escapeMap` sempre vazio, foi substituído pela mecânica de derrapagem — ver spec `derrapagem-piloto`)

#### Scenario: obterLadoEscape removido de ControleRecursos e InterfaceJogo
- **WHEN** `ControleRecursos.java` e `InterfaceJogo.java` são lidos
- **THEN** o método `obterLadoEscape(Point)` não existe em nenhum dos dois (seu único consumidor, `Piloto.escapaTracado()`, foi removido)

## ADDED Requirements

### Requirement: Modelos e overlays de mecânicas descontinuadas são removidos
Quando uma mecânica de jogo é removida ou substituída, o modelo de dados exclusivo dela, os campos que só existiam para alimentá-lo, e qualquer overlay de renderização/debug que o consome NÃO SHALL permanecer no código de produção, mesmo que ainda compilem sem erro (ex.: iterando um mapa que passou a estar sempre vazio).

#### Scenario: Classe PontoEscape removida
- **WHEN** o build é executado
- **THEN** o arquivo `PontoEscape.java` não existe na árvore de fontes

#### Scenario: Campo escapeMap e seus acessores removidos de Circuito
- **WHEN** `Circuito.java` é lido
- **THEN** o campo `escapeMap` e os métodos `getEscapeMap()`/`setEscapeMap()` não existem; `gerarEscapeMap()` (ou o método que o sucede) só popula `pista4Full`/`pista5Full`

#### Scenario: Campos pontoEscape, distanciaEscape e indexRefEscape removidos de Piloto
- **WHEN** `Piloto.java` é lido
- **THEN** os campos `pontoEscape`, `distanciaEscape`, `indexRefEscape` e os getters `getPontoDerrapada()`, `getDistanciaDerrapada()`, `getIndexRefEscape()` não existem

#### Scenario: RAIO_DERRAPAGEM removido de Carro
- **WHEN** `Carro.java` é lido
- **THEN** a constante `RAIO_DERRAPAGEM` não existe

#### Scenario: Overlay de debug do escapeMap removido do editor e da renderização de corrida
- **WHEN** `MainPanelEditor.java` e `PainelCircuito.java` são lidos
- **THEN** nenhum dos dois contém código que itera `circuito.getEscapeMap()` ou chama `piloto.getPontoDerrapada()`

#### Scenario: Botão de debug escapaTracado removido de MainFrame
- **WHEN** `MainFrame.java` é lido
- **THEN** não existe mais um `ActionListener` que chama `Piloto.escapaTracado()`

#### Scenario: Ramo morto de desvio proativo removido de processaMudarTracado
- **WHEN** `Piloto.java` é lido
- **THEN** o ramo `else if` de `processaMudarTracado()` que testava `pontoEscape != null && ... && distanciaEscape < Carro.RAIO_DERRAPAGEM` não existe
