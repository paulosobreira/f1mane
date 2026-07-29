## Why

Em modo headless (servidor), várias estruturas estáticas de `CarregadorRecursos` (`bufferCircuitos`, `bufferImages`, `bufferCarros`, `cacheModeloV2`, `cacheMonocromatico`) e de `SpriteSheet` (`cache`) crescem durante toda a vida do processo sem nenhuma política de remoção: uma vez que um circuito, carro/cor ou piloto é acessado, seus dados (grafo de nós do circuito, `BufferedImage`s geradas) ficam presos em memória até o processo ser reiniciado. Além disso, o processo servidor filho iniciado por `MainLauncher` não recebe `-Djava.awt.headless=true`, então `ImageUtil.toCompatibleImage` não entra no seu próprio atalho para JVMs headless e duplica em memória toda imagem carregada. O resultado é uso de memória crescente e desnecessário num processo que não tem GUI e não precisa reter esses dados indefinidamente.

## What Changes

- No boot do servidor headless (`MainLauncher.iniciarServidorHeadless`), para cada circuito com `ativo=true`, gerar uma vez a imagem de fundo (e miniatura) em arquivo temporário em disco, usando a lógica de geração já existente (`DesenhoProceduralCircuito.geraImagem` / `PainelCircuito.desenhaCircuito`). O mesmo para as imagens de carro-lado, carro-cima (com e sem aerofólio) e capacete de cada piloto/carro de cada temporada configurada, usando a lógica existente (`CarregadorRecursos.pintarModeloV2`/`pintarMonocromatico`/`desenhaCapacete`, ou `SpriteSheet` quando `MODO_HOMENAGEM=false`).
- Os endpoints de imagem de `LetsRace` (`circuitoJpg`, `circuitoBg`, `circuitoMini`, `carroCimaTemporadaCarro`, `carroCimaSemAreofolioTemporadaCarro`, `capaceteTemporadaPiloto`, `carroLadoTemporadaCarro`) passam a servir os bytes diretamente do arquivo em disco pré-gerado quando rodando em modo headless, em vez de gerar/cachear um `BufferedImage` em memória a cada requisição.
- Em modo headless, os caches estáticos de imagem em memória (`bufferImages`, `bufferCarros`, `cacheModeloV2`, `cacheMonocromatico`, `SpriteSheet.cache`) deixam de reter os `BufferedImage`s já persistidos em disco — a fonte de verdade para servir passa a ser o arquivo, não o mapa estático.
- Após gerar a imagem em disco de um circuito, os objetos de `Circuito`/`No` usados **apenas** para desenho (elementos decorativos de cenário/`ObjetoLivre` não consultados pela física/lógica de corrida) são liberados da árvore de objetos mantida em `bufferCircuitos`; os nós/campos efetivamente lidos por `ControleCorrida` e demais componentes de mecânica de jogo continuam em memória enquanto o circuito estiver em uso por uma corrida ativa.
- `MainLauncher.iniciarProcessoServidor` passa a incluir `-Djava.awt.headless=true` no comando do processo filho `--headless`, habilitando o atalho já existente em `ImageUtil.toCompatibleImage` que evita duplicar imagens compatíveis com um dispositivo de tela inexistente.
- **BREAKING** (interno): o comportamento de cache de imagens em memória deixa de se aplicar em modo headless; qualquer código que dependa de `CarregadorRecursos`/`SpriteSheet` devolverem sempre um `BufferedImage` já pronto em memória (em vez de um caminho de arquivo/bytes lidos de disco) precisa ser revisado.

## Capabilities

### New Capabilities
- `headless-imagens-disco`: pipeline de geração e disponibilização de imagens (circuitos, carros, capacetes) em modo headless — pré-geração para disco na subida do servidor, leitura direta de disco pelos endpoints REST, sem retenção dos `BufferedImage`s em caches estáticos em memória.

### Modified Capabilities
- (nenhuma — as specs existentes em `sdd-resources`, `sdd-rendering` e `sdd-execution-modes` documentam o estado atual da implementação; serão atualizadas num passo de sincronização de documentação após a implementação, não fazem parte do contrato comportamental desta mudança)

## Impact

- `CarregadorRecursos` (`src/main/java/br/flmane/recursos/CarregadorRecursos.java`): caches estáticos `bufferCircuitos`, `bufferImages`, `bufferCarros`, `cacheModeloV2`, `cacheMonocromatico`; métodos `carregarCircuito`, `obterCarroCima`/`obterCarroLado`/`obterCapacete`, `pintarModeloV2`, `pintarMonocromatico`, `desenhaCapacete`.
- `SpriteSheet` (`src/main/java/br/flmane/recursos/SpriteSheet.java`): cache estático `cache`, método `carregar`.
- `DesenhoProceduralCircuito.geraImagem` (`src/main/java/br/flmane/entidades/DesenhoProceduralCircuito.java`).
- `LetsRace` (`src/main/java/br/flmane/servidor/rest/LetsRace.java`): `circuitoJpg`, `circuitoBg`, `circuitoMini`, `carroCimaTemporadaCarro`, `carroCimaSemAreofolioTemporadaCarro`, `capaceteTemporadaPiloto`, `carroLadoTemporadaCarro`.
- `MainLauncher` (`src/main/java/br/flmane/MainLauncher.java`): `iniciarServidorHeadless`, `iniciarProcessoServidor`.
- `Circuito`/`No` (`src/main/java/br/flmane/entidades/`): identificar e separar campos/objetos usados só para desenho dos usados pela mecânica de jogo (`ControleCorrida` e afins).
- Diretório temporário novo para as imagens pré-geradas em disco (análogo ao `flmane-webapp` já usado por `extrairWebapp()`).
