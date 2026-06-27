## Why

O codebase acumulou código morto ao longo do desenvolvimento: stubs auto-gerados, blocos comentados, classes de teste soltas e métodos nunca chamados. Remover esse ruído é pré-requisito para que o SDD reflita apenas o que está realmente implementado e em uso.

## What Changes

- Remover classe de teste `PainelTeste.java` (nunca referenciada por código de produção)
- Remover método `main` de teste em `FormularioListaObjetos.java`
- Remover blocos de código comentado em `FormularioObjetos.java`, `PainelCampeonato.java` (servidor e visão), `PainelMenuLocal.java` e `ObjetoConstrucao.java`
- Remover métodos TODO stub sem implementação em `ControleJogoLocal.java` (`iniciaJanela`, `getVantagem`, `setVantagem`)
- Remover constructor stub vazio em `ConstrutoresPontosCampeonato.java`
- Limpar os ~40 métodos TODO stub em `JogoCliente.java` que implementam `InterfaceJogo` sem corpo útil (avaliar se devem ser implementados ou se a interface deve ser revisada)
- Remover linhas comentadas isoladas espalhadas pelo código
- Remover 8 métodos Java sem chamadores confirmados:
  - `ControleClima.isClimaAleatorio()`
  - `No.getTipoJson()`
  - `Carro.getCor2Hex()`
  - `MainFrameEditor.exibirResiltadoFinal()` (também tem typo no nome)
  - `ControleCorrida.verificaCarroLentoOuDanificado(Piloto)`
  - `ControleCorrida.acharPilotoDaFrente(Piloto)`
  - `ControleRecursos.setMapaIdsNos(Map)`
  - `ControleRecursos.setMapaNosIds(Map)`

## Capabilities

### New Capabilities
- `dead-code-removal`: Eliminação sistemática de código morto identificado — classes de teste, blocos comentados, stubs gerados automaticamente e métodos sem uso

### Modified Capabilities

## Impact

- `src/main/java/br/f1mane/visao/PainelTeste.java` — removida inteiramente
- `src/main/java/br/f1mane/editor/FormularioListaObjetos.java` — remoção do `main` de teste
- `src/main/java/br/f1mane/editor/FormularioObjetos.java` — remoção de bloco comentado
- `src/main/java/br/f1mane/servidor/applet/PainelCampeonato.java` — remoção de bloco comentado
- `src/main/java/br/f1mane/visao/PainelCampeonato.java` — remoção de condição comentada
- `src/main/java/br/f1mane/visao/PainelMenuLocal.java` — remoção de linha comentada
- `src/main/java/br/f1mane/entidades/ObjetoConstrucao.java` — remoção de linha comentada
- `src/main/java/br/f1mane/controles/ControleJogoLocal.java` — remoção de métodos stub
- `src/main/java/br/f1mane/entidades/ConstrutoresPontosCampeonato.java` — remoção de constructor stub
- `src/main/java/br/f1mane/servidor/applet/JogoCliente.java` — limpeza dos ~40 stubs de interface
- `src/main/java/br/f1mane/controles/ControleClima.java` — remoção de `isClimaAleatorio()`
- `src/main/java/br/f1mane/entidades/No.java` — remoção de `getTipoJson()`
- `src/main/java/br/f1mane/entidades/Carro.java` — remoção de `getCor2Hex()`
- `src/main/java/br/f1mane/MainFrameEditor.java` — remoção de `exibirResiltadoFinal()`
- `src/main/java/br/f1mane/controles/ControleCorrida.java` — remoção de `verificaCarroLentoOuDanificado()` e `acharPilotoDaFrente()`
- `src/main/java/br/f1mane/controles/ControleRecursos.java` — remoção de `setMapaIdsNos()` e `setMapaNosIds()`
- Sem impacto em APIs públicas ou comportamento em runtime
