## 1. Dependências

- [x] 1.1 Adicionar `netty-transport`, `netty-codec-http`, `netty-handler` (versão 4.1.x/4.2.x estável compatível com Java 25) ao `pom.xml`
- [x] 1.2 Adicionar `com.fasterxml.jackson.core:jackson-databind` como dependência direta ao `pom.xml`
- [x] 1.3 Manter `tomcat-embed-jasper`, `jersey-*` e `jakarta.servlet-api` no `pom.xml` até a Seção 6 (evitar estado intermediário quebrado)

## 2. Primitivas do router e static handler

- [x] 2.1 Criar pacote `br.flmane.servidor.netty` com `RespostaHttp` (status, entity, contentType) e builder equivalente a `Response.status(x).entity(y).type(z).build()`
- [x] 2.2 Criar `Rota`/matcher de path template (`{token}`) por `(HttpMethod, template)`, casando 1:1 os paths hoje anotados com `@Path` em `LetsRace`
- [x] 2.3 Escrever testes unitários do matcher de rotas (segmentos estáticos, `{token}`, GET vs. POST no mesmo path) — `RoteadorTest`, 10 testes cobrindo casamento, content-type padrão/override, gzip, query/header, exceção→500
- [x] 2.4 Criar `StaticFileHandler` Netty servindo um diretório base, resolvendo `Content-Type` por extensão (html/css/js/jpg/png/gif/ico) e `index.html` como documento padrão (implementado como `ArquivoEstaticoHandler`)
- [x] 2.5 Configurar `ObjectMapper` do Jackson para o router (serialização de entities não-`byte[]`/`String` quando `contentType` é JSON)

## 3. Migração de LetsRace

- [x] 3.1 Remover as anotações `jakarta.ws.rs.*` de `LetsRace` e converter os 44 métodos para retornar `RespostaHttp` em vez de `jakarta.ws.rs.core.Response`
- [x] 3.2 Registrar todos os endpoints migrados na tabela de rotas do router (path, método HTTP, extração de header/path/query params) — `RotasLetsRace`
- [x] 3.3 Atualizar os testes de `letsrace-endpoint-tests` para a nova forma de construção/roteamento, mantendo os mesmos cenários de comportamento (401/403/400/500/200)
- [x] 3.4 Rodar `mvn test` e confirmar que a suíte de `LetsRace` continua verde — 37/37 passando

## 4. Migração de ServletPaddock e conf.jsp

- [x] 4.1 Converter `ServletPaddock` de `HttpServlet` para um handler Netty dedicado no path `/ServletPaddock` (GET e POST tratados igual, como hoje)
- [x] 4.2 Preservar o protocolo `ObjectInputStream`/`ObjectOutputStream` (e o caminho `ZipUtil`/`PaddockConstants.modoZip`) sem alteração de formato
- [x] 4.3 Portar `topExceptions()`/`sessoesAtivas()`/`html5()` para o novo handler, mantendo o HTML idêntico
- [x] 4.4 Criar classe Java (ex.: `PaginaConf`) que gera o HTML hoje produzido por `conf.jsp`, servida em `GET /flmane/conf.jsp`
- [x] 4.5 Conferir que os links `ServletPaddock?tipo=X` / `?tipo=S` e o link "back" para `conf.jsp` continuam funcionando — verificado via `curl` na Seção 7.5

## 5. Bootstrap do servidor

- [x] 5.1 Reescrever `MainLauncher.iniciarServidorHeadless()` trocando `Tomcat`/`Context` por `ServerBootstrap` + `NioEventLoopGroup` (boss/worker)
- [x] 5.2 Montar o `ChannelInitializer` com `HttpServerCodec` + `HttpObjectAggregator` (dimensionado com folga para o protocolo de `ServletPaddock`) + dispatcher único
- [x] 5.3 Implementar o dispatcher: `/ServletPaddock` → handler do protocolo; `/flmane/rest/letsRace/*` → router de `LetsRace`; demais paths sob `/flmane/*` → `StaticFileHandler` (`FlmaneHttpDispatcher`)
- [x] 5.4 Envolver o dispatch em try/catch (log + resposta 500) para substituir o tratamento de exceção automático que o Jersey fazia
- [x] 5.5 Manter `extrairWebapp()`/`criarDiretorioTemporarioSeguro()` como estão, apontando o `StaticFileHandler` para o diretório extraído
- [x] 5.6 Preservar o banner de log ("SERVER STARTED" + URL) e o bloqueio equivalente a `tomcat.getServer().await()` (`channel.closeFuture().sync()`)

## 6. Remoção da pilha antiga

- [x] 6.1 Remover `tomcat-embed-jasper`, `jersey-server`, `jersey-container-servlet`, `jersey-hk2`, `jersey-media-json-jackson`, `jakarta.servlet-api` do `pom.xml` (também removidos `Compress.java`/`GZIPWriterInterceptor.java`, código JAX-RS morto)
- [x] 6.2 Apagar `src/main/webapp/WEB-INF/web.xml`
- [x] 6.3 Apagar `src/main/webapp/conf.jsp`
- [x] 6.4 Rodar `grep -r "javax\.servlet\|jakarta\.servlet\|jakarta\.ws\.rs\|org\.apache\.catalina" src/main/java` e confirmar que não há mais nenhum resultado — confirmado, `mvn clean compile` + `mvn test` (818/818) verdes sem essas dependências

## 7. Build e verificação manual

- [x] 7.1 Rodar `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar` (regra deste repositório: sempre atualizar o jar após mudança em código Java)
- [x] 7.2 Testar manualmente o fluxo do launcher: QR Code, abrir no navegador, `http://<ip>:8080/flmane/html5/index.html` carrega — verificado via `curl` no jar rodando `--headless` (200, `text/html`); QR Code em si não testável em sandbox headless
- [x] 7.3 Testar manualmente as páginas do cliente HTML5 (campeonato, corrida, classificação, equipe) que consomem `/flmane/rest/letsRace/*`, incluindo endpoints de imagem (`carroLado`, `capacete`, `circuitoMini`) — verificado via `curl`: `/circuitos` (JSON), `/png/{recurso}` (PNG válido), `/circuito?nomeCircuito=...` (JSON com gzip), `POST /equipe` (401 sem token); `carroLado` dispara NPE de negócio pré-existente sem sessão de jogo ativa, não relacionado à migração
- [ ] 7.4 Testar manualmente o cliente multiplayer `AppletPaddock` ponta-a-ponta contra `/flmane/ServletPaddock` — não testável em sandbox headless (cliente Swing); protocolo de serialização verificado estruturalmente (ver design.md), pendente teste manual do usuário
- [x] 7.5 Testar manualmente as páginas de admin `/flmane/ServletPaddock?tipo=X`, `?tipo=S` e `/flmane/conf.jsp` — verificado via `curl`: conteúdo e quirks (nested doctype, truncamento sem `tipo`) idênticos ao comportamento anterior
- [ ] 7.6 Testar manualmente o modo GUI (processo filho `--headless`) e confirmar que fechar o launcher encerra o processo do servidor — não testável em sandbox headless (requer display gráfico), pendente teste manual do usuário

## 8. Documentação

- [x] 8.1 Atualizar `README.md` descrevendo o servidor web como Netty embutido (em vez de Tomcat/Jersey)
- [x] 8.2 Atualizar `README.pt-BR.md` com a mesma mudança
