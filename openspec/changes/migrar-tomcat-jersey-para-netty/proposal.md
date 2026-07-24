## Why

O servidor web embutido hoje depende de uma pilha Jakarta EE completa (Tomcat 11 + Jasper + Jersey/JAX-RS + HK2) só para expor ~40 endpoints REST, um servlet cru com protocolo de serialização Java próprio (`ServletPaddock`) e alguns arquivos estáticos. Essa pilha obriga upgrades em lockstep sempre que uma peça avança de versão (como já aconteceu na migração recente para Jakarta EE 11 documentada em `java-tomcat-upgrade`) e traz complexidade (web.xml, Jasper/JSP, injeção HK2) desproporcional ao que a aplicação realmente usa. Migrar para um servidor Netty embutido, sob controle direto do projeto, remove essa cadeia de dependências e simplifica o modo servidor a um único componente assíncrono.

## What Changes

- Remover as dependências `tomcat-embed-jasper`, `jersey-server`, `jersey-container-servlet`, `jersey-hk2`, `jersey-media-json-jackson` e `jakarta.servlet-api` do `pom.xml`; adicionar Netty (`io.netty:netty-all` ou os módulos equivalentes) e `jackson-databind` como dependência direta (antes transitiva via Jersey).
- **BREAKING**: `MainLauncher.iniciarServidorHeadless()` deixa de subir Tomcat (`org.apache.catalina.startup.Tomcat`) e passa a inicializar um servidor HTTP Netty (`ServerBootstrap`) na porta 8080, sem `Context`/`web.xml`.
- **BREAKING**: `LetsRace` deixa de usar anotações JAX-RS (`@Path`, `@GET`, `@POST`, `@HeaderParam`, `@PathParam`, `@QueryParam`, `jakarta.ws.rs.core.Response`). Os 38 endpoints passam a ser métodos comuns despachados por um roteador Netty escrito à mão, que casa método HTTP + path e extrai parâmetros de header/path/query manualmente.
- **BREAKING**: `ServletPaddock` deixa de estender `HttpServlet`; sua lógica (protocolo de serialização Java em `/ServletPaddock` e as páginas HTML de admin — exceptions/sessões) passa a ser um handler Netty equivalente.
- `conf.jsp` (que depende do motor JSP/Jasper do Tomcat, indisponível sem servlet container) é reescrito como página gerada em Java, preservando o mesmo conteúdo e os mesmos links (`ServletPaddock?tipo=X`, `?tipo=S`).
- Arquivos estáticos hoje servidos pelo Tomcat a partir de `src/main/webapp/` (html5/, bootstrap/, images/, jquery/, jwtdecode/, index.html, favicon, páginas de política de privacidade) passam a ser servidos por um handler de arquivos estático do próprio Netty, preservando as mesmas rotas (contexto `/flmane`, welcome file `index.html`).
- `src/main/webapp/WEB-INF/web.xml` é removido (não há mais servlet container); o mapeamento de rotas passa a viver no código do router Netty.
- `README.md` e `README.pt-BR.md` atualizados para descrever a nova stack do servidor web (Netty no lugar de Tomcat/Jersey).

## Capabilities

### New Capabilities
- `netty-http-server`: servidor HTTP embutido em Netty que substitui o Tomcat — bootstrap do servidor, roteamento para os endpoints equivalentes a `LetsRace`, handler do protocolo de serialização de `ServletPaddock`, página de admin (`conf.jsp` reescrita) e serviço de arquivos estáticos de `webapp/`.

### Modified Capabilities
- `java-tomcat-upgrade`: os requisitos que descrevem Tomcat 11 e Jersey 4.x como servidor/JAX-RS embutidos são substituídos por requisitos equivalentes usando Netty; requisitos de Hibernate/Jakarta Persistence (não relacionados a Tomcat/Jersey) permanecem inalterados.
- `sdd-execution-modes`: a descrição do modo `--headless` de `MainLauncher` passa a citar o servidor Netty embutido em vez de "Tomcat 11" e Jersey; a menção ao namespace `jakarta.servlet.*`/`jakarta.ws.rs.*` é removida.
- `launcher-servidor-processo-separado`: as referências a "Tomcat + PaddockServer" no processo filho passam a "Netty + PaddockServer".
- `letsrace-endpoint-tests`: o requisito de instanciação em teste (injeção de `ControlePaddockServidor`/`CarregadorRecursos` sem acionar `PaddockServer.init`) é preservado, mas deixa de mencionar o construtor "usado pelo Jersey em produção" — passa a ser o construtor usado pelo router Netty. Os contratos de status HTTP (401/403/400/500/200) e de delegação para os controllers permanecem os mesmos.

## Impact

- **Dependências** (`pom.xml`): remove `tomcat-embed-jasper`, `jersey-server`, `jersey-container-servlet`, `jersey-hk2`, `jersey-media-json-jackson`, `jakarta.servlet-api`; adiciona Netty e `jackson-databind` direto.
- **Código**: `MainLauncher.java` (bootstrap do servidor), `LetsRace.java` (1054 linhas, 38 endpoints — remoção das anotações JAX-RS), `ServletPaddock.java` (deixa de ser `HttpServlet`), novo roteador Netty, nova classe para a página de admin (substituindo `conf.jsp`), novo handler de arquivos estáticos.
- **Recursos removidos**: `src/main/webapp/WEB-INF/web.xml`, `src/main/webapp/conf.jsp` (substituído por código Java).
- **Specs existentes**: `java-tomcat-upgrade`, `sdd-execution-modes`, `launcher-servidor-processo-separado`, `letsrace-endpoint-tests` recebem spec deltas.
- **Testes**: testes de `LetsRace` (`letsrace-endpoint-tests`) precisam ser adaptados à nova forma de construção/roteamento, mantendo os mesmos cenários de comportamento.
- **Documentação**: `README.md`, `README.pt-BR.md`.
- **Build/Docker**: `flmane.dockerfile` e `docker-compose` não mudam em termos de JVM/porta (continua expondo 8080), mas o comportamento interno do processo `--headless` muda de Tomcat para Netty.
