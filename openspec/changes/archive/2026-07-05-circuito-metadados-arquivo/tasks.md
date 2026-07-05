## 1. Utilitário de split (zerar e codificar)

- [x] 1.1 Criar um método utilitário (ex. em `CarregadorRecursos` ou classe nova de suporte) que, a partir de um `Circuito`, produz uma cópia rasa para o arquivo de metadados: mantém `nome`, `noite`, `usaBkg`, `probalidadeChuva`, `velocidadePista`, `ladoBox`, `ladoBoxSaidaBox`, cores, `pista` e `box`; zera (para novas listas/mapas vazios ou valores padrão do bean) `ativo`, `objetos`, `objetosCenario` e todos os campos derivados (`pistaFull`, `pista1Full`, `pista2Full`, `pista4Full`, `pista5Full`, `boxFull`, `box1Full`, `box2Full`, `pistaKey`, `boxKey`, `escapeMap`, `escapeList`, índices de box) — implementado como `Circuito.copiaParaArquivoMetadados()`; nota: `pistaFull`/`pista1..5Full`/`boxFull`/`box1/2Full`/índices de box não têm setter em `Circuito`, então nunca foram de fato persistidos pelo `XMLEncoder` — só `pistaKey`/`boxKey`/`escapeMap`/`escapeList` precisavam ser zerados ativamente
- [x] 1.2 Criar o método simétrico para o arquivo de objetos: mantém só `objetos`/`objetosCenario`; zera metadados (inclusive `ativo`), `pista`, `box` e todos os campos derivados — implementado como `Circuito.copiaParaArquivoObjetos()`
- [x] 1.3 Função de nome de arquivo de metadados a partir do nome do arquivo de objetos (`nmCircuitoXml.replaceFirst("\\.xml$", "_meta.xml")`) — `CarregadorRecursos.nomeArquivoMetadados()`

## 2. circuitos.properties ganha o campo ativo

- [x] 2.1 Definir e documentar o novo formato de linha: `<arquivo>_mro.xml=<NomeExibicao>,<ciclo>,<ativo>`
- [x] 2.2 Criar um método de leitura (ex. em `CarregadorRecursos`) que carrega `properties/circuitos.properties` e retorna o terceiro campo (`ativo`) de uma chave específica, com `false` como default se a linha ou o campo não existir — `CarregadorRecursos.lerAtivoDeCircuitosProperties()`
- [x] 2.3 Criar um método de escrita que localiza a linha de uma chave específica em `properties/circuitos.properties`, substitui (ou acrescenta) o terceiro campo, e reescreve o arquivo preservando todas as outras linhas byte-a-byte (leitura/escrita linha a linha, não via `Properties.store()`) — `CarregadorRecursos.atualizarAtivoEmCircuitosProperties()`

## 3. Salvar (MainPanelEditor.salvarPista)

- [x] 3.1 Gerar a cópia de metadados (1.1) e gravar via `XMLEncoder` em `<nome>_mro_meta.xml`
- [x] 3.2 Gerar a cópia de objetos (1.2) e gravar via `XMLEncoder` em `<nome>_mro.xml` (substitui o conteúdo atual do arquivo, que passa a não ter mais pista/box/metadados/campos derivados/ativo)
- [x] 3.3 Chamar o método de escrita (2.3) para atualizar o terceiro campo (`ativo`) da linha correspondente em `circuitos.properties`, usando o valor de `ativoCheckBox` — usa `circuito.isAtivo()` diretamente (já sincronizado com o checkbox pelo listener existente)
- [x] 3.4 Confirmar que os três artefatos (`_mro_meta.xml`, `_mro.xml`, `circuitos.properties`) são gravados de forma segura o suficiente para não deixar um deles ausente/corrompido/dessincronizado em caso de falha no meio do processo

## 4. Carregar (CarregadorRecursos.carregarCircuito)

- [x] 4.1 Decodificar `<nome>_mro.xml` (agora só `objetos`/`objetosCenario`) e, se `<nome>_mro_meta.xml` existir, decodificá-lo também (metadados + `pista` + `box`) e copiar `objetos`/`objetosCenario` para o `Circuito` decodificado do arquivo de metadados
- [x] 4.2 Se `<nome>_mro_meta.xml` não existir (circuito ainda no formato antigo de arquivo único), usar só o `Circuito` decodificado de `_mro.xml` como está hoje
- [x] 4.3 Chamar `circuito.setAtivo(...)` usando o método de leitura (2.2) sobre `circuitos.properties`, sobrescrevendo qualquer valor de `ativo` que o XML decodificado ainda tenha (circuito não migrado)
- [x] 4.4 Chamar `circuito.vetorizarPista()` sobre o `Circuito` final, antes de colocar em `bufferCircuitos` e retornar — garantindo que todo `Circuito` saído deste método já está vetorizado, independentemente de o chamador lembrar de fazê-lo
- [x] 4.5 Confirmar que o objeto retornado e o cache (`bufferCircuitos`) continuam com o mesmo comportamento de antes para todo consumidor existente (jogo solo, multiplayer, REST, editor)

## 5. Leitura rápida de ativo (CarregadorRecursos.circuitoAtivo) e listagem (ControleRecursos.carregarCircuitos)

- [x] 5.1 Reescrever `circuitoAtivo()` para usar o método de leitura (2.2) sobre `circuitos.properties`, em vez do `BufferedReader`/escaneamento de XML atual; manter o atalho de cache-hit (`bufferCircuitos.get(nmCircuito).isAtivo()`) quando aplicável
- [x] 5.2 Atualizar `ControleRecursos.carregarCircuitos()` para ler o terceiro campo (`names[2]`) diretamente no loop que já itera `circuitos.properties`, em vez de chamar `CarregadorRecursos.circuitoAtivo()` por circuito

## 6. Miniatura (GerenciadorVisual.desenhaMiniCircuito)

- [x] 6.1 Criar (ou reaproveitar de 4.1) um caminho de leitura leve que decodifica só `<nome>_mro_meta.xml` (metadados + `pista`/`box`), sem `_mro.xml` nem `vetorizarPista()` — `CarregadorRecursos.carregarMetadadosCircuito()`
- [x] 6.2 Atualizar `GerenciadorVisual.desenhaMiniCircuito()` (linha 925) para usar essa leitura leve em vez de `CarregadorRecursos.carregarCircuito()` completo

## 7. Migração dos circuitos existentes

- [x] 7.1 Escrever um utilitário (classe `main` temporária, executada uma única vez) que, para cada `*_mro.xml` em `src/main/resources/circuitos/` ainda no formato antigo: (a) decodifica o circuito, gera as duas cópias (1.1/1.2) e grava `_mro_meta.xml` e o novo `_mro.xml`; (b) lê o `ativo` atual do circuito decodificado e usa o método de escrita (2.3) para gravá-lo como terceiro campo na linha correspondente de `circuitos.properties` — `br.f1mane.recursos.MigracaoCircuitoMetadados`, idempotente (pula circuitos cujo `_mro_meta.xml` já existe)
- [x] 7.2 Rodar o utilitário e conferir que todos os arquivos `*_mro.xml` existentes foram migrados para o par `_mro_meta.xml`/`_mro.xml`, com os campos derivados descartados, e que `circuitos.properties` ganhou o terceiro campo em todas as linhas correspondentes — 37/37 circuitos migrados; `catalunya_mro.xml` caiu de ~4,8MB/216k linhas para 5,7KB (só objetos/objetosCenario); confirmado por grep que nenhum dos dois arquivos contém campos do outro
- [x] 7.3 Conferir manualmente um ou dois circuitos migrados (ex. abrir no editor de circuitos, checar nome/ativo/cores/traçado exibidos corretamente, miniatura desenhada e corrida solo carregando normalmente) — verificado via simulação headless (`java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Catalunya 5`): corrida completa em Catalunya (circuito migrado) sem exceções, com classificação final gerada normalmente; verificação visual do editor Swing/miniatura não é possível neste ambiente sem display interativo

## 8. Testes

- [x] 8.1 Teste unitário: salvar um `Circuito` de teste e verificar que `_mro_meta.xml` contém `pista`/`box`/metadados mas não `ativo`/`objetos`/`objetosCenario`/campos derivados, e que `_mro.xml` contém `objetos`/`objetosCenario` mas não metadados/`ativo`/`pista`/`box`/campos derivados — `CircuitoMetadadosArquivoTest`
- [x] 8.2 Teste unitário: carregar um circuito com os dois arquivos e verificar que o `Circuito` resultante tem metadados, `pista`, `box`, `objetos`, `objetosCenario`, `ativo` (vindo de `circuitos.properties`) e os campos derivados (via `getPistaFull()`/`getBoxFull()`/etc.) corretamente populados — `CircuitoMetadadosArquivoTest` (usa `albert_park_mro.xml` real, já migrado)
- [x] 8.3 Teste unitário: carregar um circuito no formato antigo (só `_mro.xml`, sem `_mro_meta.xml`) e verificar que o resultado é idêntico ao comportamento anterior a esta mudança, com `ativo` vindo de `circuitos.properties` — `CircuitoMetadadosArquivoTest`, fixture `src/test/resources/circuitos/fixture_formato_antigo_mro.xml`
- [x] 8.4 Atualizar `CarregadorRecursosCircuitoAtivoTest` (docstring e comentários mencionam hoje "lê do XML"; passam a refletir a leitura de `circuitos.properties`) — os asserts em si (`albert_park` ativo, `monza` inativo, arquivo inexistente retorna `false`, leve==completo) continuam válidos
- [x] 8.5 Teste unitário: salvar dois circuitos em sequência e verificar que a linha de `circuitos.properties` de um não afeta a do outro (preservação byte-a-byte das demais linhas) — `CircuitoMetadadosArquivoTest` (usa a sobrecarga `atualizarAtivoEmCircuitosProperties(File, ...)` com um arquivo temporário, não o `circuitos.properties` real)
- [x] 8.6 Teste unitário: os campos derivados calculados por `vetorizarPista()` dentro de `carregarCircuito()` batem com os mesmos campos calculados manualmente a partir de `pista`/`box`/`objetos` do circuito de teste — `CircuitoMetadadosArquivoTest`
