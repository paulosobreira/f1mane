## Context

O modo headless já não retém `BufferedImage` em caches estáticos (`headless-imagens-disco`): `CarregadorRecursos.modoHeadlessDisco` desliga `bufferImages`, `bufferCarros*`, `bufferCapacete`, `bufferCircuitos` e o cache de `SpriteSheet`, e `ImagensHeadlessDisco` serve bytes de disco. O que sobrou é custo de **runtime da JVM** e de **acoplamento a Swing/Java2D no caminho de execução**:

- `flmane.dockerfile` usa `-Djava.awt.headless=true` apenas no `RUN` de pré-geração; o `ENTRYPOINT` (`java -jar app.jar --headless`) sobe sem a flag e sem nenhum ajuste de heap consciente de cgroup.
- `MainFrameSimulacao` (`./simulacao.sh`, simulação headless) chama `new ControleJogoLocal(3)`, construtor que **sempre** instancia `GerenciadorVisual` — a thread de rendering roda numa simulação que nunca desenha. O caminho multiplayer escapa disso só por acidente de herança: `JogoServidor extends ControleJogoLocal` e o construtor por temporada tem um `if (!(this instanceof JogoServidor))`.
- `InterfaceJogo` importa `javax.swing.*` e `br.flmane.visao.PainelTabelaResultadoFinal`; `ControleEstatisticas` mantém campos `JPanel`/`JEditorPane` e `atualizaInfoDebug()` chama `SwingUtilities.invokeLater` sem verificar se o componente existe — invocá-lo num processo servidor acorda a Event Dispatch Thread e estoura em `infoTextual` nulo.
- `LetsRace` instancia `PainelCircuito` (um `JPanel`) por requisição de fundo de circuito não pré-gerado.
- `Logger.topExceptions` é um `LinkedHashMap` estático que cresce até 10.000 assinaturas por processo, sem descarte.

Não há linha de base medida: hoje qualquer afirmação sobre "gastar menos memória" é impressão.

## Goals / Non-Goals

**Goals:**
- Reduzir heap e metaspace do processo `--headless` em regime e por corrida ativa.
- Garantir que o processo servidor nunca inicialize toolkit gráfico nativo nem a Event Dispatch Thread.
- Tornar explícito (por construção e por teste) o que é do modo GUI e o que é do modo servidor, em vez de depender de `instanceof` acidental.
- Medir antes e depois com o mesmo procedimento.

**Non-Goals:**
- Reescrever o rendering ou trocar Java2D por outra tecnologia de desenho — a geração de imagens headless continua usando Java2D (`BufferedImage` + `Graphics2D`), que funciona sem display.
- Remover as classes Swing do fat jar (`br.flmane.visao`, `br.flmane.editor`, `br.flmane.servidor.applet`): o mesmo jar serve os três modos por design; classe não carregada não custa heap.
- Mexer no protocolo REST, no formato das imagens ou no modelo de dados.
- Otimizar o modo GUI/solo/editor.

## Decisions

### 1. Flags de JVM concentradas no `ENTRYPOINT`, não em script wrapper
`ENTRYPOINT ["java","-Djava.awt.headless=true","-XX:MaxRAMPercentage=75","-jar","app.jar","--headless"]`.

*Por quê:* mantém a configuração de runtime num único lugar auditável e preserva `exec`-form (PID 1 recebe sinais, shutdown limpo). Alternativa considerada: script `entrypoint.sh` lendo variáveis de ambiente — mais flexível, mas adiciona um shell entre o PID 1 e a JVM e espalha a configuração; rejeitada por ora, já que não há hoje demanda de tunar flags por deploy.

*Coletor:* manter o default do Temurin 25 (G1) em vez de forçar SerialGC. G1 já se ajusta a container pequeno; forçar coletor sem medição seria palpite. Reavaliar com os números da medição.

### 2. Rendering opt-in por construtor, não `instanceof`
Substituir `if (!(this instanceof JogoServidor))` por um sinal explícito: os construtores de `ControleJogoLocal` usados pelos modos gráficos criam `GerenciadorVisual`; um construtor/flag "sem rendering" é usado por `JogoServidor` e por `MainFrameSimulacao`.

*Por quê:* o `instanceof` esconde a regra dentro do construtor da superclasse e não cobre o caminho de simulação; qualquer subclasse nova reintroduz o problema em silêncio. Alternativa considerada: consultar `GraphicsEnvironment.isHeadless()` dentro do construtor — rejeitada porque acopla decisão de arquitetura a estado global de JVM e quebraria o processo filho do launcher, que roda `--headless` mas serve o teste local.

### 3. Componentes Swing sempre lazy e com guarda de ausência
`ControleEstatisticas.atualizaInfoDebug()` retorna cedo se `infoTextual == null` (nenhum componente foi solicitado); `painelNarracao()`/`painelDebug()`/`obterResultadoFinal()` seguem criando sob demanda.

*Por quê:* é a mudança de menor risco que já elimina a EDT do servidor. O cálculo do texto de debug também deixa de ser feito quando ninguém o exibe, poupando `StringBuilder`s por tick.

### 4. Isolamento do contrato: `InterfaceJogo` deixa de arrastar `br.flmane.visao`
Extrair os pontos que devolvem componentes gráficos (`painelNarracao`, `painelDebug`, `obterResultadoFinal`) para uma interface separada (ex.: `InterfaceJogoVisual`), implementada por `ControleJogoLocal` e consumida só por `MainFrame`/applet. `InterfaceJogo` fica sem `import javax.swing` e sem tipos de `br.flmane.visao`.

*Por quê:* enquanto a assinatura de retorno for `PainelTabelaResultadoFinal`, o verificador de bytecode carrega a classe ao resolver o método no caminho servidor. Alternativa considerada: manter os métodos e devolver `Object` — evita o carregamento, mas troca tipagem por cast em todo chamador; rejeitada.

*Escopo:* `LetsRace` continua usando `PainelCircuito` para o fallback preguiçoso de fundo de circuito. Isso é geração de imagem Java2D, não UI, e só ocorre quando o asset não foi pré-gerado — fora do escopo desta mudança, mas anotado como candidato a extração futura para `DesenhoProceduralCircuito`.

### 5. `Logger.topExceptions` com descarte LRU
Manter `LinkedHashMap` em modo access-order com `removeEldestEntry` no limite atual (10.000), em vez do comportamento atual de parar de registrar ao encher.

*Por quê:* preserva o diagnóstico das exceções recentes num container de uptime longo e limita o crescimento por construção, sem estrutura nova.

### 6. Medição por RSS do cgroup + Flight Recorder (não `jcmd`)
Script `utilitarios/medir_memoria_headless.sh` com cenário de carga fixo (boot limpo com imagens assadas; depois N corridas criadas via REST), medindo `VmRSS` de `/proc/1/status` dentro do container em cada ponto e lendo `jdk.GCHeapSummary`/`jdk.MetaspaceSummary`/`jdk.ClassLoadingStatistics` de uma gravação JFR.

*Por quê:* a imagem de produção é `eclipse-temurin:25-jre-alpine` — uma **JRE, que não traz `jcmd`** (`ls /opt/java/openjdk/bin` → `java jfr jrunscript jwebserver keytool rmiregistry`). Sem `jcmd` não há como pedir heap-após-GC de fora do processo, então o JFR passa de alternativa a escolha principal: é injetado no boot via `JAVA_TOOL_OPTIONS` (`FLMANE_JAVA_TOOL_OPTIONS` no compose, vazio no uso normal) e gravado por `dumponexit`, sem tocar no `ENTRYPOINT` de produção. O RSS complementa porque é o número que o cgroup enxerga e o que motiva OOM-kill.

*Consequência no compose:* `docker-compose.yaml` ganha duas interpolações com default — `JAVA_TOOL_OPTIONS: ${FLMANE_JAVA_TOOL_OPTIONS:-}` e `"${FLMANE_PORTA_HOST:-80}:8080"` — ambas no-op no deploy real. A segunda existe porque o ambiente de desenvolvimento é Podman rootless, onde publicar na porta 80 falha.

## Risks / Trade-offs

- **Extrair `InterfaceJogoVisual` toca muitos arquivos e pode quebrar o modo GUI** → fazer a extração como passo isolado, sem mudança de comportamento, com a suíte de testes rodando antes e depois e verificação manual do modo solo (`MainFrame`) e do applet.
- **`MaxRAMPercentage=75` pode ser agressivo em container pequeno** (heap grande + metaspace + buffers nativos estourando o limite do cgroup e levando a OOM-kill) → validar com o procedimento de medição num container com limite explícito antes de fixar o valor.
- **Remover `GerenciadorVisual` da simulação pode alterar o resultado da corrida** se algum estado for atualizado pela thread de rendering → confirmar por simulação determinística (mesma seed) que o resultado final é idêntico antes e depois.
- **Guarda em `atualizaInfoDebug()` pode esconder debug no modo GUI** se o painel for solicitado depois do primeiro tick → a guarda testa o componente, não um modo; assim que o painel é criado, as atualizações voltam a fluir.
- **`java.awt.headless=true` no `ENTRYPOINT` quebraria o container se algum caminho servidor dependesse de display** → a etapa de pré-geração já roda com a flag hoje e gera todas as imagens, o que é a evidência de que o caminho Java2D funciona headless.
- **Ganho pode ser pequeno** — a maior parte do desperdício de imagens já foi eliminada. Por isso a medição é requisito, e não ornamento: se o ganho não aparecer, isso é registrado em vez de mascarado.

## Migration Plan

1. Medir a linha de base no container atual (pós-boot e com N corridas) e registrar os números.
2. Aplicar as mudanças de baixo risco: flags do `ENTRYPOINT`, guarda em `atualizaInfoDebug`, LRU em `Logger`, liberação das estruturas de pré-geração.
3. Aplicar a mudança de rendering opt-in (`JogoServidor` + `MainFrameSimulacao`), validando resultado idêntico por seed fixa.
4. Extrair `InterfaceJogoVisual` e conferir que nenhuma classe de `br.flmane.visao` é carregada pelo caminho de corrida headless.
5. Medir de novo, comparar e registrar.
6. Rebuild da imagem (`utilitarios/build_container.sh`) e `mvn clean package -Ph2 -DskipTests` para manter o jar do usuário atualizado.

*Rollback:* cada passo é um commit independente e reversível; nenhum dado, volume ou contrato externo é alterado, então reverter é só voltar o commit e reconstruir a imagem.

## O que a implementação corrigiu do plano

- **`MainFrameSimulacao` não é headless.** A premissa de que `./simulacao.sh` rodava sem UI estava errada: `MainFrameSimulacao extends MainFrame extends JFrame` e desenha de verdade (só desliga camadas via `PainelCircuito.desenhaBkg/desenhaImagens/desenhaPista`). O `GerenciadorVisual` ali é necessário — tarefa descartada.
- **O ganho do rendering opt-in é menor do que parecia.** O construtor de `GerenciadorVisual` é barato (guarda a referência e cria um `VisualRandom`); a thread de rendering só sobe em `iniciarInterfaceGraficaJogo()`. E o caminho servidor já escapava pelo `instanceof JogoServidor`. O valor da mudança é robustez (decisão explícita, imune a subclasse nova), não bytes.
- **`InterfaceJogoVisual` precisou estender `InterfaceJogo`.** As classes de desenho consomem os dois contratos; sem a herança, `GerenciadorVisual` e `MainFrame` precisariam de dois campos. `PainelCircuito` ficou com `InterfaceJogo` + verificação de tipo, porque o servidor o instancia no fallback de fundo de circuito.
- **Medição exigiu duas interpolações no compose** (`JAVA_TOOL_OPTIONS` e porta), além de `mem_limit` — sem limite de memória no container, `MaxRAMPercentage` só pioraria o dimensionamento.

## Open Questions

- Valor final de `MaxRAMPercentage` — depende da medição do passo 1 e do limite de memória real do host de produção (hoje o compose não impõe limite).
- Vale mover a geração de fundo de circuito de `PainelCircuito` para `DesenhoProceduralCircuito` em `LetsRace`, eliminando o último `JPanel` do caminho servidor, ou deixar para uma mudança própria?
- O procedimento de medição deve virar parte do CI, ou basta ser um utilitário manual em `utilitarios/`?
