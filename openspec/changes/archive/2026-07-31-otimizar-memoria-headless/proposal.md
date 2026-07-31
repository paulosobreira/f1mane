## Why

O modo headless (`java -jar app.jar --headless`, o deploy real em container) já não retém `BufferedImage` em cache estático — `headless-imagens-disco` resolveu essa parte. Mas o processo servidor continua pagando memória por coisas que só existem para o modo Swing/Java2D: a JVM do container sobe **sem** `-Djava.awt.headless=true` (só o `RUN` de pré-geração do `flmane.dockerfile` usa a flag) e sem nenhum ajuste de heap consciente de container, e o caminho de corrida do servidor (`ControleJogoLocal`/`InterfaceJogo`/`ControleEstatisticas`) instancia e carrega classes Swing (`GerenciadorVisual`, `JPanel`, `JEditorPane`, `JScrollPane`) que nenhum cliente web consome — cada corrida ativa no servidor carrega e aloca componentes de UI que ninguém desenha.

O objetivo é reduzir o footprint de heap/metaspace por processo e por corrida no modo headless, sem alterar o comportamento do modo GUI (`MainFrame`, editor, applet), que continua precisando de todo o Swing.

## What Changes

- **Runtime da JVM em container**: o `ENTRYPOINT` do `flmane.dockerfile` passa a rodar com `-Djava.awt.headless=true` e com dimensionamento de heap consciente de container (`-XX:MaxRAMPercentage`), além da escolha explícita de coletor adequada ao perfil do serviço. Hoje a flag headless só existe no `RUN` de pré-geração — no runtime a JVM ainda pode inicializar o toolkit gráfico nativo (fontconfig/X11) e reter buffers nativos que nunca serão usados.
- **Corrida de servidor sem componentes Swing**: `ControleJogoLocal` deixa de instanciar `GerenciadorVisual` quando roda sob o servidor headless, e os pontos de `InterfaceJogo`/`ControleEstatisticas` que hoje devolvem `JPanel`/`JEditorPane`/`PainelTabelaResultadoFinal` passam a ser criados sob demanda (lazy) — nunca no caminho servidor. O contrato de `InterfaceJogo` que hoje força o carregamento de `br.flmane.visao` é isolado, para que a árvore de classes Swing não seja sequer carregada num processo `--headless`.
- **Medição antes e depois**: instrumentação reprodutível (heap após boot, heap por corrida ativa, classes carregadas) para que o ganho seja um número, não uma impressão — hoje não há linha de base.
- **Higiene de estruturas estáticas de vida longa**: `Logger.topExceptions` (mapa estático que cresce até 10.000 entradas por processo) e os caches estáticos residuais de `CarregadorRecursos` passam a ter comportamento definido e limitado no modo headless.
- Nenhuma mudança de comportamento observável para o cliente web: mesmos endpoints, mesmas imagens, mesma corrida. **Sem BREAKING** para o modo GUI/solo/editor.

## Capabilities

### New Capabilities
- `headless-jvm-runtime`: flags de JVM do processo headless em container (headless real, heap consciente de cgroup, coletor), incluindo o que deve valer no `RUN` de pré-geração vs. no `ENTRYPOINT`.
- `headless-sem-swing`: o caminho de corrida do servidor headless não instancia nem carrega classes Swing/`br.flmane.visao`; componentes de UI passam a ser criados sob demanda apenas nos modos GUI.
- `memoria-headless-medicao`: procedimento e limiares de medição de memória do processo headless (linha de base pós-boot, custo por corrida ativa), usados como critério de aceite desta e de futuras mudanças.

### Modified Capabilities
- `headless-imagens-disco`: acrescenta requisito de liberação das estruturas usadas durante a pré-geração (listas de pilotos/temporadas e caches auxiliares) antes de o servidor começar a aceitar requisições, hoje não especificado.

## Impact

- Código: `flmane.dockerfile`, `MainLauncher`, `ControleJogoLocal`, `InterfaceJogo`, `ControleEstatisticas`, `br.nnpe.Logger`, `CarregadorRecursos`.
- Modos afetados: apenas o processo `--headless` (deploy container e processo filho do launcher). `MainFrame` (solo Swing), `EditorCircuitos` e `AppletPaddock` mantêm o comportamento atual.
- Testes: novos testes de que o caminho servidor não instancia `GerenciadorVisual` nem componentes Swing; testes existentes de `MainLauncher`/`ImagensHeadlessDisco` continuam válidos.
- Deploy: mudança de `ENTRYPOINT` exige rebuild da imagem (`utilitarios/build_container.sh`); nenhum dado ou volume é afetado.
- Não altera o contrato REST de `LetsRace` nem o formato das imagens servidas.
