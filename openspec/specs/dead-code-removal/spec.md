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
