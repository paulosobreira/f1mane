# Spec: Dead Code Removal

## Purpose

Define the standards for keeping the codebase free of dead code: unreachable classes, unused methods, commented-out code blocks, and IDE-generated stubs that serve no production purpose.

## Requirements

### Requirement: Classes de teste desacopladas são removidas
O codebase NÃO SHALL conter classes Java cujo único propósito é teste manual de desenvolvimento e que não são referenciadas por nenhuma classe de produção.

#### Scenario: PainelTeste removida
- **WHEN** o build é executado
- **THEN** o arquivo `PainelTeste.java` não existe na árvore de fontes

### Requirement: Métodos main de debug são removidos
Classes de produção NÃO SHALL conter métodos `main` utilizados apenas para teste manual de UI.

#### Scenario: main de FormularioListaObjetos removido
- **WHEN** `FormularioListaObjetos.java` é compilado
- **THEN** a classe não possui método `main` declarado

### Requirement: Blocos de código comentado são eliminados
O codebase NÃO SHALL conter blocos de código Java comentado (blocos `/* */` ou sequências de `//`) que não sejam comentários de documentação (Javadoc) ou explicações de invariante/algoritmo.

#### Scenario: Bloco comentado em FormularioObjetos removido
- **WHEN** `FormularioObjetos.java` é lido
- **THEN** as linhas 90-109 (MouseListener comentado) não existem

#### Scenario: Bloco comentado em PainelCampeonato do servidor removido
- **WHEN** `servidor/applet/PainelCampeonato.java` é lido
- **THEN** o bloco comentado de confirmação de campeonato (linhas 80-99) não existe

#### Scenario: Linhas isoladas comentadas removidas
- **WHEN** os arquivos `PainelCampeonato.java` (visão), `PainelMenuLocal.java` e `ObjetoConstrucao.java` são lidos
- **THEN** as linhas contendo `//campeonato.getNomePiloto`, `//desenhaFPS` e `//affineTransform` não existem

### Requirement: Stubs TODO de IDE são removidos ou convertidos
Métodos gerados automaticamente por IDE com corpo contendo apenas `// TODO Auto-generated` SHALL ser removidos (se a remoção não quebra contrato de interface) ou ter o comentário TODO eliminado (se o corpo vazio é o comportamento correto por contrato).

#### Scenario: Stubs de ControleJogoLocal removidos
- **WHEN** `ControleJogoLocal.java` é lido
- **THEN** os métodos `iniciaJanela()`, `getVantagem()` e `setVantagem(String)` não contêm comentário `// TODO Auto-generated`

#### Scenario: Constructor stub de ConstrutoresPontosCampeonato removido
- **WHEN** `ConstrutoresPontosCampeonato.java` é lido
- **THEN** o construtor vazio com `// TODO Auto-generated constructor stub` não existe ou o comentário foi removido

#### Scenario: Comentários TODO auto-generated em JogoCliente removidos
- **WHEN** `JogoCliente.java` é lido
- **THEN** nenhuma linha contém o texto `// TODO Auto-generated method stub`

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
