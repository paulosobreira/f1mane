# netty-http-server

## Purpose

Documents the embedded Netty HTTP server that replaced Tomcat/Jasper/Jakarta Servlet/JAX-RS as the web backend for `flmane.jar` — covering server startup, REST routing equivalent to the former `LetsRace` JAX-RS resource, the `ServletPaddock` serialization protocol, the JSP-free configuration page, and static file serving.

## Requirements

### Requirement: Servidor HTTP embutido em Netty
O modo `--headless` de `MainLauncher` SHALL inicializar um servidor HTTP Netty (`ServerBootstrap` com `NioEventLoopGroup`) na porta 8080, sem depender de Tomcat, Jersey ou da Servlet API.

#### Scenario: Servidor sobe com Netty
- **WHEN** `MainLauncher --headless` é executado
- **THEN** um canal Netty é vinculado à porta 8080 e o log de inicialização exibe o banner "SERVER STARTED" com a URL do servidor, sem nenhuma classe `org.apache.catalina.*` sendo instanciada

### Requirement: Roteamento para os endpoints equivalentes a LetsRace
O servidor SHALL expor, sob `/flmane/rest/letsRace/*`, os mesmos endpoints (path, método HTTP, parâmetros de header/path/query e content-type de resposta) hoje anotados com JAX-RS em `LetsRace`, despachados por um roteador Netty que não depende de anotações `jakarta.ws.rs.*`.

#### Scenario: Endpoint JSON responde igual
- **WHEN** uma requisição GET é feita para `/flmane/rest/letsRace/circuitos`
- **THEN** a resposta tem content-type `application/json` e o mesmo corpo que o endpoint retorna hoje

#### Scenario: Endpoint de imagem responde igual
- **WHEN** uma requisição GET é feita para `/flmane/rest/letsRace/carroLado/{temporada}/{carro}`
- **THEN** a resposta tem content-type `image/png` e o corpo são os bytes da imagem, sem serialização JSON

#### Scenario: Mesmo path aceita GET e POST distintos
- **WHEN** requisições GET e POST são feitas para `/flmane/rest/letsRace/equipe` (ou `/flmane/rest/letsRace/campeonato`)
- **THEN** cada uma é despachada para o handler correspondente ao método, preservando o comportamento atual de cada verbo

### Requirement: Protocolo de serialização de ServletPaddock preservado
O servidor SHALL expor em `/flmane/ServletPaddock` um handler que lê o corpo da requisição como um `ObjectInputStream` Java quando presente, delega para `ControlePaddockServidor.processarObjetoRecebido`, e escreve a resposta via `ObjectOutputStream` (ou `ZipUtil` quando `PaddockConstants.modoZip`), com o mesmo comportamento de hoje independentemente de GET ou POST.

#### Scenario: Cliente multiplayer troca objetos serializados
- **WHEN** `AppletPaddock` envia um objeto serializado para `/flmane/ServletPaddock`
- **THEN** o servidor desserializa o objeto, delega para `processarObjetoRecebido` e devolve a resposta serializada no mesmo formato de hoje

#### Scenario: Requisição sem stream de objeto cai nas páginas de admin
- **WHEN** uma requisição sem `ObjectInputStream` válido é feita para `/flmane/ServletPaddock?tipo=X` ou `?tipo=S`
- **THEN** a resposta é a página HTML de exceptions ou de sessões ativas, respectivamente, igual ao comportamento atual

### Requirement: Página de configuração sem motor JSP
O servidor SHALL servir em `GET /flmane/conf.jsp` uma página HTML gerada em Java (sem depender de Jasper/JSP) com o mesmo conteúdo do `conf.jsp` atual (versão Java, appserver, host, OS, locale/timezone, uso de memória) e os mesmos links para `ServletPaddock?tipo=X` e `?tipo=S`.

#### Scenario: conf.jsp responde sem servlet container
- **WHEN** uma requisição GET é feita para `/flmane/conf.jsp`
- **THEN** a resposta é HTML 200 com as mesmas seções de informação do ambiente que a JSP atual exibe

### Requirement: Arquivos estáticos servidos pelo Netty
O servidor SHALL servir os arquivos estáticos extraídos de `webapp/` (`html5/`, `bootstrap/`, `images/`, `jquery/`, `jwtdecode/`, `index.html`, favicon, páginas de política de privacidade) sob o prefixo `/flmane/`, com `index.html` como documento padrão em `/flmane/` e `/flmane/html5/`.

#### Scenario: Cliente HTML5 carrega normalmente
- **WHEN** o navegador acessa `/flmane/html5/index.html`
- **THEN** a página e todos os seus assets (css/js/img) referenciados por caminho relativo são servidos com o content-type correto

#### Scenario: Welcome file funciona no contexto raiz
- **WHEN** o navegador acessa `/flmane/` sem especificar um arquivo
- **THEN** o servidor responde com o conteúdo de `index.html`
