## Context

Hoje o modo servidor (`MainLauncher --headless`) sobe um Tomcat 11 embutido que hospeda três coisas diferentes sob o contexto `/flmane`:

1. **Jersey/JAX-RS** (`LetsRace`, `@Path("/letsRace")`, mapeado em `/rest/*`) — 44 rotas (GET/POST) que respondem JSON (a maioria), `image/jpg`/`image/png` (sprites/circuitos, como `byte[]`) e `text/html` (`/sobre`). Duas rotas (`/equipe`, `/campeonato`) têm GET e POST no mesmo path.
2. **`ServletPaddock`** (`HttpServlet` cru, mapeado em `/ServletPaddock`) — protocolo próprio de serialização Java: lê um `Object` via `ObjectInputStream` do corpo da requisição, delega para `ControlePaddockServidor.processarObjetoRecebido`, e escreve a resposta via `ObjectOutputStream` (ou `ZipUtil` se `PaddockConstants.modoZip`). Quando o corpo não é um stream de objeto válido, cai em páginas HTML de admin (`?tipo=X` exceptions, `?tipo=S` sessões), que linkam de volta para `conf.jsp`.
3. **Arquivos estáticos** de `src/main/webapp/` (cliente HTML5 em `html5/`, bootstrap, imagens, jQuery, páginas de política de privacidade) e a JSP `conf.jsp` (que só roda porque o Tomcat traz o Jasper).

O cliente HTML5 (`src/main/webapp/html5/js/*.js`) tem as URLs `/flmane/rest/letsRace/...` **hardcoded**; `ServletPaddock` (usado pelo cliente Swing multiplayer `AppletPaddock`) é acessado em `/flmane/ServletPaddock`. `PaddockServer.init(realpath)` já ignora o parâmetro `realpath` (confirmado lendo o código) — ou seja, nada depende hoje do `ServletContext` real do Tomcat além do bootstrap em si.

O usuário já decidiu (ver proposal) que a migração remove Jersey/JAX-RS por completo, não só o Tomcat — a nova pilha é Netty "puro".

## Goals / Non-Goals

**Goals:**
- Preservar o contrato HTTP externo exatamente como está: mesmos paths, métodos, status codes e content-types em `/flmane/rest/letsRace/*`, `/flmane/ServletPaddock` e nos arquivos estáticos sob `/flmane/*` — nenhuma mudança é visível para o cliente HTML5 nem para `AppletPaddock`.
- Preservar os dois modos de `MainLauncher` (`--headless` in-process vs. GUI com processo filho) exatamente como documentado em `sdd-execution-modes`/`launcher-servidor-processo-separado`, só trocando Tomcat por Netty por baixo.
- Remover totalmente `tomcat-embed-jasper`, `jersey-*`, `jakarta.servlet-api` do `pom.xml`.
- Manter o protocolo de serialização Java de `ServletPaddock` byte-a-byte compatível.

**Non-Goals:**
- Não mudar a lógica de negócio dos endpoints (delegação para `ControlePaddockServidor`/`ControleJogosServer` continua igual).
- Não adicionar TLS/HTTPS (o Tomcat atual também não termina TLS).
- Não mudar o protocolo de comunicação do multiplayer (continua HTTP por polling, não WebSocket).
- Não adotar um framework maior sobre Netty (Vert.x, Spring WebFlux, RESTEasy Reactive) — Netty "cru", conforme decisão já tomada.

## Decisions

**1. Módulos Netty específicos, não `netty-all`.** Usar `netty-transport`, `netty-codec-http`, `netty-handler` em vez do artefato guarda-chuva `netty-all` (padrão de distribuição legado). `NioEventLoopGroup` (transporte NIO) é suficiente — não há necessidade de epoll/kqueue nativo para o volume de requisições deste jogo (poucos jogadores fazendo polling).

**2. Router próprio, enxuto.** Uma lista ordenada de rotas `(HttpMethod, template, handler)`, casando o path por segmentos (`{token}` vira parâmetro nomeado), espelhando 1:1 os templates `@Path` atuais — as URLs não mudam. Alternativa considerada: tabela de rotas baseada em regex — rejeitada, os templates atuais são simples (segmentos estáticos + `{}`), regex seria complexidade desnecessária.

**3. Tipo de resposta local `RespostaHttp`** (status, entity, contentType) substituindo `jakarta.ws.rs.core.Response`, com builder equivalente a `Response.status(x).entity(y).type(z).build()` para manter o diff nos 44 endpoints o mais mecânico possível.

**4. Jackson direto para JSON.** `ObjectMapper` (hoje trazido transitivamente por `jersey-media-json-jackson`) declarado como dependência direta e usado pelo router: quando `contentType` é JSON e a entity não é `byte[]`/`String`, serializa com Jackson — mesmo comportamento efetivo de hoje (inclusive `@JsonProperty` já presentes em TOs como `ErroServ`, cobertos por `letsrace-endpoint-tests`).

**5. Endpoints de imagem/HTML continuam devolvendo `byte[]`/`String` prontos.** O router escreve a entity direto no `ByteBuf` de resposta com o `contentType` informado — sem serialização adicional, sem mudança de comportamento.

**6. `ServletPaddock` vira um handler Netty dedicado** no path `/ServletPaddock` (aceitando o corpo tanto de GET quanto POST, já que hoje `doPost` delega para `doGet`): lê o corpo agregado num `ObjectInputStream` quando presente; sem stream de objeto válido, cai nas mesmas páginas HTML de admin (lógica de `html5()`/`topExceptions()`/`sessoesAtivas()` portada sem mudança de conteúdo).

**7. `conf.jsp` vira uma classe Java** (ex.: `PaginaConf`) que gera o HTML idêntico ao atual (versão Java, appserver, host, OS, locale/timezone, uso de memória, barra de progresso), servida em `GET /flmane/conf.jsp` — o link "back" das páginas de admin de `ServletPaddock` continua funcionando sem alteração.

**8. Estáticos servidos a partir do mesmo diretório temporário já extraído do JAR.** Mantém `extrairWebapp()`/`criarDiretorioTemporarioSeguro()` de `MainLauncher` como estão (hardening de permissões já resolvido); um `StaticFileHandler` Netty serve esse diretório sob `/flmane/*`, com `index.html` como documento padrão em `/flmane/` e `/flmane/html5/` (equivalente ao `welcome-file-list` do Tomcat). Alternativa considerada: servir direto do classpath sem extrair para temp dir — rejeitada para não mexer no fluxo de extração já endurecido, e porque nada mais depende de removê-lo (`PaddockServer.init` já ignora o `realpath`).

**9. Bootstrap do servidor.** `MainLauncher.iniciarServidorHeadless()` troca `Tomcat`/`Context` por `ServerBootstrap` + `NioEventLoopGroup` (boss/worker) + `ChannelInitializer` com `HttpServerCodec` + `HttpObjectAggregator` + um dispatcher único que roteia por prefixo: `/ServletPaddock` → handler do protocolo; `/flmane/rest/letsRace/*` → router do LetsRace; caso contrário → `StaticFileHandler`. Mantém o banner de log ("SERVER STARTED" + URL) e o bloqueio equivalente a `tomcat.getServer().await()` (`channel.closeFuture().sync()`).

## Risks / Trade-offs

- [Risco] Router escrito à mão reimplementa casamento de path template e coerção de parâmetros (int/boolean/etc.) que o JAX-RS fazia automaticamente — um parâmetro mal tratado pode gerar 500 silencioso ou parse errado em algum dos 44 endpoints → **Mitigação**: usar os cenários de `letsrace-endpoint-tests` como checklist de regressão por endpoint, e testar unitariamente o casamento de rotas isoladamente.
- [Risco] `ObjectMapper` novo pode divergir sutilmente da configuração default do `jersey-media-json-jackson` atual (formato de data, tratamento de null) → **Mitigação**: configurar o `ObjectMapper` explicitamente e comparar manualmente (curl) a resposta de alguns endpoints antes/depois da migração.
- [Risco] O protocolo `ObjectInputStream` de `ServletPaddock` exige o corpo inteiro antes de desserializar — `HttpObjectAggregator` precisa de um `maxContentLength` generoso o bastante para não truncar payloads grandes de sessão de jogo → **Mitigação**: dimensionar o aggregator com folga (comparável ao limite atual do Tomcat) e testar ponta-a-ponta com `AppletPaddock` real.
- [Risco] `StaticFileHandler` precisa resolver `Content-Type` corretamente por extensão (jpg/png/css/js/gif/ico/html) para todos os diretórios hoje servidos (`bootstrap/`, `html5/`, `images/`, `jquery/`, `jwtdecode/`) → **Mitigação**: mapa de extensão→content-type cobrindo os tipos existentes, verificado carregando `index.html` e uma amostra de cada tipo de asset no navegador.
- [Risco] Perda do tratamento de exceção automático que o Jersey fazia (exceção não tratada vira 500) → **Mitigação**: o dispatcher do router envolve a chamada do handler em try/catch, loga e responde 500, evitando derrubar a thread do event loop.
- [Trade-off] Mais código próprio para manter (router, static handler, wiring de JSON) em vez de depender de um framework maduro → aceito por decisão explícita do produto (ver proposal, "Netty puro, sem Jersey").

## Migration Plan

1. Adicionar as dependências Netty + `jackson-databind`; manter Tomcat/Jersey no `pom.xml` até o novo servidor estar funcional (evita estado intermediário quebrado).
2. Construir as primitivas do router (`RespostaHttp`, `Rota`, matcher) e o `StaticFileHandler` num pacote novo (ex.: `br.flmane.servidor.netty`), com testes unitários, sem tocar em `LetsRace`/`ServletPaddock` ainda.
3. Migrar `LetsRace` das anotações JAX-RS para métodos comuns retornando `RespostaHttp`, mantendo `letsrace-endpoint-tests` verde (ajustando a forma de construção nos testes conforme necessário).
4. Migrar `ServletPaddock` para o handler Netty; portar `conf.jsp` para a página Java.
5. Reconectar `MainLauncher.iniciarServidorHeadless()` ao bootstrap Netty; verificar manualmente os três modos de execução (`--headless`, GUI, `MainFrameSimulacao` — este último não é afetado) e o fluxo de processo filho de `launcher-servidor-processo-separado`.
6. Remover `tomcat-embed-jasper`, `jersey-*`, `jakarta.servlet-api` do `pom.xml`; apagar `WEB-INF/web.xml` e `conf.jsp`.
7. Rebuildar o fat jar (`mvn clean package -Ph2 -DskipTests` — regra deste repositório de sempre atualizar o jar após mudança em código Java) e testar manualmente: fluxo do QR Code do launcher, cliente HTML5 (campeonato/corrida/classificação), cliente multiplayer `AppletPaddock`, e as páginas de admin de `ServletPaddock` (`?tipo=X`/`?tipo=S`).
8. Atualizar `README.md`/`README.pt-BR.md`.

Sem plano de rollback além de `git revert` — é um app local de serviço único, sem deploy independente além de reconstruir o jar/imagem Docker.

## Open Questions

- Versão exata do Netty a fixar — resolver na implementação, escolhendo a última 4.1.x/4.2.x estável compatível com Java 25 no momento de executar `tasks.md`.
- Confirmar durante a implementação que nada além de `PaddockServer.init` referencia o `realpath`/`ServletContext` antes de finalizar a decisão de manter a extração para diretório temporário (Decisão 8).
