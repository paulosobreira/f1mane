## Context

Hoje, em modo headless (`MainLauncher --headless`, backend Netty + `LetsRace`), toda imagem (fundo/miniatura de circuito, carro-lado, carro-cima, capacete) é gerada em memória a cada requisição HTTP e, dependendo do caminho de código, fica presa para sempre em um `Map` estático de `CarregadorRecursos` (`bufferImages`, `bufferCarros`, `cacheModeloV2`, `cacheMonocromatico`) ou de `SpriteSheet` (`cache`). `CarregadorRecursos.bufferCircuitos` também retém, para sempre, o grafo completo de `No`/objetos de cada `Circuito` já acessado — incluindo objetos que só servem para desenhar o traçado, não para a física/mecânica da corrida. Nenhum desses caches tem política de expiração; o processo servidor não recebe `-Djava.awt.headless=true`, então `ImageUtil.toCompatibleImage` duplica cada imagem carregada (ver `ImageUtil.java:294-320`).

O modo local/solo (`MainFrame`, `GerenciadorVisual`) já libera a imagem de fundo ao encerrar uma corrida (`GerenciadorVisual.dispose()`) e não é afetado por esta mudança — o pipeline novo só entra em vigor quando o processo é iniciado com `--headless`.

## Goals / Non-Goals

**Goals:**
- Eliminar o crescimento ilimitado de memória em modo headless causado por caches estáticos de imagens (`BufferedImage`) e por grafos de `Circuito`/`No` retidos apenas para desenho.
- Gerar, uma única vez na subida do servidor headless, os arquivos de imagem (circuitos ativos, carros/capacetes de todas as temporadas configuradas) em um diretório temporário em disco, reaproveitando a lógica de geração já existente.
- Fazer os endpoints de imagem de `LetsRace` servirem bytes diretamente do arquivo em disco em modo headless, sem manter o `BufferedImage` resultante em um cache estático.
- Liberar da memória, após a geração do arquivo em disco, os objetos de um `Circuito` usados exclusivamente para desenho, mantendo em memória apenas o que a mecânica de jogo (`ControleCorrida` e afins) efetivamente consulta durante uma corrida ativa.
- Habilitar o atalho já existente de `ImageUtil` para JVMs headless (`-Djava.awt.headless=true` no processo filho).

**Non-Goals:**
- Não alterar o comportamento do modo GUI/solo (`MainFrame`, `AppletPaddock`) nem o pipeline de desenho usado por eles (`PainelCircuito`/`GerenciadorVisual` continuam gerando em memória, como hoje).
- Não migrar o transporte HTTP nem o formato de resposta dos endpoints existentes — a URL e o contrato REST de cada endpoint de imagem permanecem os mesmos; muda apenas a origem dos bytes (disco em vez de memória) e o descarte pós-uso.
- Não implementar um CDN/servidor de arquivos estáticos genérico — o mecanismo é específico para as imagens já servidas por `LetsRace`.
- Não resolver, nesta mudança, a ausência de `Cache-Control` em alguns endpoints (`circuitoBg`, `circuitoMini`, endpoints de carro/capacete) — fica registrado como melhoria futura, não bloqueia o objetivo de memória.

## Decisions

### 1. Diretório de imagens pré-geradas, análogo ao `flmane-webapp`
Criar um diretório temporário próprio (ex.: `<tmp>/flmane-imagens-headless/{circuitos,carros,capacetes}/`), no mesmo padrão de `extrairWebapp()` (`MainLauncher.java:152-191`): recriado do zero a cada boot do processo `--headless` (sem tentar reaproveitar de um boot anterior), para nunca servir um arquivo desatualizado em relação a XML/`properties` que possam ter mudado entre deploys. Alternativa considerada — cachear entre reinicializações comparando timestamps — descartada por complexidade desnecessária: a geração acontece uma vez por boot e é rápida o bastante (poucas dezenas de circuitos/carros/pilotos).

### 2. Pré-geração sequencial na subida do servidor, não paralela nem sob demanda pura
Em `MainLauncher.iniciarServidorHeadless()`, após subir os componentes essenciais, percorrer sequencialmente: (a) todos os circuitos com `ativo=true` (via o mesmo mecanismo de `ControleRecursos.carregarCircuitos()`/`circuitoAtivo`), gerando fundo + miniatura; (b) todas as temporadas configuradas (`carregarTemporadas()`), para cada carro/piloto gerando carro-lado, carro-cima (com/sem aerofólio) e capacete. Cada imagem é gerada, escrita em disco e imediatamente descartada (referência local, sem inserir em nenhum `Map` estático) antes de passar para a próxima — o pico de memória do próprio processo de pré-geração fica limitado a poucas imagens por vez, não ao total. Alternativa considerada — gerar tudo sob demanda no primeiro request de cada asset — rejeitada porque não cumpre o pedido de ter tudo pronto em disco já na subida e reintroduziria um cache em memória "primeira vez" se não for cuidadosamente descartado.

### 3. Flag de modo headless-disco em `CarregadorRecursos`/`SpriteSheet`
Introduzir uma flag estática equivalente à já existente `cache` (ex.: `modoHeadlessDisco`), setada por `MainLauncher.iniciarServidorHeadless()` antes da pré-geração. Quando ativa: `obterCarroCima`/`obterCarroLado`/`obterCapacete`/`pintarModeloV2`/`pintarMonocromatico`/`desenhaCapacete` e `SpriteSheet.carregar` não inserem o resultado nos `Map`s estáticos existentes; em vez disso, os métodos correspondentes em `LetsRace` resolvem o caminho do arquivo pré-gerado e leem os bytes diretamente do disco. Reaproveita-se a lógica de geração atual (mesmos métodos), mudando apenas o destino do resultado (arquivo vs. mapa em memória).

### 4. Fallback preguiçoso para assets não pré-gerados
Circuito ativado ou temporada/carro adicionado após o boot (sem reiniciar o processo) não estará no diretório pré-gerado. Nesse caso, o endpoint correspondente gera a imagem sob demanda (mesmo código da pré-geração), grava no mesmo diretório e serve o arquivo — sem inserir em cache de memória. Concorrência de múltiplas requisições simultâneas para o mesmo asset ausente é resolvida com um lock por chave (caminho do arquivo), não com um cache de `BufferedImage`: o lock guarda apenas a chave/caminho, nunca os bytes da imagem.

### 5. Separação de objetos de desenho vs. mecânica em `Circuito`
Adicionar um método `Circuito.liberarObjetosDesenho()` que zera as referências aos objetos usados exclusivamente por `DesenhoProceduralCircuito`/`PainelCircuito` (elementos decorativos de cenário, `ObjetoLivre` não consultados pela física), preservando os campos lidos por `ControleCorrida`/`ControleSafetyCar`/`ControleBox`/`ControleAutomacao` durante uma corrida (ex.: `pistaFull`, `box1Full`, os nós de posição/traçado). Esse método é chamado logo após a imagem do circuito ser gravada em disco, tanto no passo de pré-geração quanto no fallback preguiçoso. A lista exata de campos "só desenho" depende de um levantamento por grep/leitura cruzada com os componentes de mecânica de jogo — tratado como tarefa de implementação dedicada (auditoria antes de remover qualquer campo), não decidido a priori neste design.

### 6. `-Djava.awt.headless=true` no processo filho
Adicionar a flag na construção do comando em `MainLauncher.iniciarProcessoServidor` (`java -Xms64m -Xmx512m -Djava.awt.headless=true -cp <jar> br.flmane.MainLauncher --headless`). Isso ativa o atalho já existente em `ImageUtil.toCompatibleImage` (`GraphicsEnvironment.isHeadless()`) sem exigir nenhuma mudança nesse método.

## Risks / Trade-offs

- [Remover campo de `Circuito` que na verdade é lido pela mecânica de jogo, quebrando física/colisão de forma sutil] → Mitigação: auditoria por grep de cada campo candidato em `ControleCorrida`/`ControleSafetyCar`/`ControleBox`/`ControleAutomacao` antes de zerá-lo; cobrir com teste de simulação headless (`./simulacao.sh`) comparando resultado de corrida antes/depois da liberação.
- [Pré-geração no boot aumenta o tempo de subida do servidor headless] → Mitigação: geração sequencial e descartável é rápida (mesma lógica hoje executada por requisição); medir tempo de boot antes/depois e, se necessário, logar progresso.
- [Diretório temporário cresce sem limite em ambientes Docker com disco limitado] → Mitigação: diretório é recriado (não acumulado) a cada boot; número de arquivos é limitado (circuitos ativos × 2 + temporadas × carros/pilotos × 3), tamanho estimável e documentável.
- [Fallback preguiçoso reintroduz um mapa em memória (locks por chave)] → Mitigação: o mapa guarda apenas chaves/caminhos (strings), nunca bytes de imagem — footprint desprezível comparado ao `BufferedImage` que ele evita cachear.
- [`-Djava.awt.headless=true` quebra algo que dependia implicitamente de um `GraphicsEnvironment` com display] → Mitigação: geração de imagem já usa `BufferedImage`/`Graphics2D` off-screen, que funciona normalmente em JVM headless; o próprio `ImageUtil` já antecipa esse modo.
- [Modo GUI/solo ser afetado por engano] → Mitigação: toda a mudança fica atrás do branch `--headless` existente em `MainLauncher`; `MainFrame`/`GerenciadorVisual` não chamam nenhum código novo.

## Migration Plan

Mudança é puramente de comportamento interno em runtime, sem alteração de schema/dados persistidos. Após implementar:
1. `mvn clean package -Ph2 -DskipTests` para gerar o jar atualizado (obrigatório após qualquer alteração em `src/main/java` — ver `CLAUDE.md`).
2. Rodar `java -jar target/flmane.jar --headless` localmente (ou via `docker compose`) e verificar no client web (`http://localhost:8080/flmane/html5/index.html`) que circuitos, carros e capacetes carregam normalmente.
3. Rodar `./simulacao.sh` para confirmar que a mecânica de corrida headless não regride após a liberação de objetos de desenho.
4. Rollback: reverter os commits da mudança; não há estado persistido a desfazer.

## Open Questions

- O diretório de imagens pré-geradas deve ser limpo ao encerrar o processo, ou deixado para inspeção manual em caso de troubleshooting? Proposta: deixar (é um diretório temporário do SO, já sujeito a limpeza externa), sem lógica extra de shutdown.
- Falha ao pré-gerar um asset específico (ex.: XML de circuito malformado) deve abortar o boot inteiro ou só logar e pular aquele asset? Proposta: logar e pular — mantém o servidor no ar para os demais circuitos/temporadas.
- Lista definitiva de campos "só desenho" em `Circuito`/`No` — depende de auditoria a ser feita na implementação (tarefa dedicada em `tasks.md`), não de uma decisão de design fechada aqui.
