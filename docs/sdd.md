# Software Design Document — F1 Mane

**Versão:** 1.0  
**Data:** 2026-06-27  
**Projeto:** F1 Mane — Simulador de Fórmula 1 em Java

---

## Índice

1. [Introdução](#1-introdução)
2. [Modos de Execução](#2-modos-de-execução)
   - 2.1 [Entry Points](#21-entry-points)
   - 2.2 [MainLauncher — Modo Web](#22-mainlauncher--modo-web)
   - 2.3 [MainFrame — Modo Solo](#23-mainframe--modo-solo)
   - 2.4 [AppletPaddock — Modo Multiplayer Cliente](#24-appletpaddock--modo-multiplayer-cliente)
   - 2.5 [MainFrameSimulacao — Modo Headless](#25-mainframesimulacao--modo-headless)
3. [Engine de Jogo (Local)](#3-engine-de-jogo-local)
   - 3.1 [ControleJogoLocal — Hub Central](#31-controlejogolocalHub-central)
   - 3.2 [ControleCiclo — Tick Loop](#32-controleciclo--tick-loop)
   - 3.3 [Subsistemas da Corrida](#33-subsistemas-da-corrida)
4. [Camada Servidor e Multiplayer](#4-camada-servidor-e-multiplayer)
   - 4.1 [PaddockServer — Singleton de Infraestrutura](#41-paddockserver--singleton-de-infraestrutura)
   - 4.2 [REST API — LetsRace](#42-rest-api--letsrace)
   - 4.3 [Gerenciamento de Sessão e Jogos](#43-gerenciamento-de-sessão-e-jogos)
   - 4.4 [JogoCliente — Cliente do Protocolo](#44-jogocliente--cliente-do-protocolo)
5. [Modelo de Dados](#5-modelo-de-dados)
   - 5.1 [Piloto](#51-piloto)
   - 5.2 [Carro](#52-carro)
   - 5.3 [No](#53-no)
   - 5.4 [Circuito](#54-circuito)
6. [Persistência](#6-persistência)
   - 6.1 [Perfis Maven e Configuração de Banco](#61-perfis-maven-e-configuração-de-banco)
   - 6.2 [Entidades JPA](#62-entidades-jpa)
7. [Rendering](#7-rendering)
   - 7.1 [PainelCircuito](#71-painelcircuito)
   - 7.2 [SpriteSheet](#72-spritesheet)
8. [Recursos e Internacionalização](#8-recursos-e-internacionalização)
   - 8.1 [CarregadorRecursos](#81-carregadorrecursos)
   - 8.2 [Internacionalização — Lang](#82-internacionalização--lang)

---

## 1. Introdução

O F1 Mane é um simulador de corridas de Fórmula 1 desenvolvido em Java. Ele simula temporadas completas de F1 com pit stops, safety car, mudanças climáticas, desgaste de pneus e motor, ERS/DRS e modo campeonato. O jogo suporta tanto o modo solo (um jogador contra a IA) quanto o modo multiplayer via servidor web.

A arquitetura central é deliberadamente compacta: um único JAR (`flmane.jar`) serve três modos de execução distintos — servidor web com Tomcat embutido, jogo solo em Swing e cliente multiplayer. Essa unificação foi obtida extraindo o diretório `webapp/` do próprio JAR em tempo de execução.

O mesmo engine de simulação (`ControleJogoLocal`) é reutilizado pelo modo solo e pelo servidor multiplayer. No servidor, cada instância de `JogoServidor` estende `ControleJogoLocal` e gerencia uma partida independente. No cliente web, `JogoCliente` implementa a mesma interface (`InterfaceJogo`) porém delegando o estado real para o servidor via protocolo HTTP.

---

## 2. Modos de Execução

### 2.1 Entry Points

O mesmo JAR `flmane.jar` expõe quatro entry points distintos:

| Classe | Modo | Comando típico |
|---|---|---|
| `MainLauncher` | Servidor web + launcher Swing | `java -jar target/flmane.jar` |
| `MainFrame` | Jogo solo Swing | `java -cp target/flmane.jar br.f1mane.MainFrame` |
| `AppletPaddock` | Cliente multiplayer Swing | iniciado pelo launcher |
| `MainFrameSimulacao` | Simulação headless (debug) | `./simulacao.sh` ou `java -cp ... br.f1mane.MainFrameSimulacao 2024 Catalunya 72` |

### 2.2 MainLauncher — Modo Web

`br.f1mane.MainLauncher` é o entry point padrão do JAR e realiza as seguintes operações em sequência:

1. **Extração do webapp**: chama `extrairWebapp()`, que copia o diretório `webapp/` embutido no JAR para um diretório temporário chamado `flmane-webapp` no sistema de arquivos local.
2. **Inicialização do Tomcat**: sobe Tomcat na porta 8080, mapeando o contexto `/flmane` para o diretório extraído via `addWebapp("/flmane", base.getAbsolutePath())`.
3. **Launcher Swing**: se nenhum argumento for passado (`args == null || args.length == 0`), exibe uma janela Swing com QR Code e três botões:
   - **Web (HTML5)**: abre `http://localhost:8080/flmane/html5/index.html`
   - **Solo Java**: lança `br.f1mane.MainFrame`
   - **Multiplayer Java**: lança `br.f1mane.servidor.applet.AppletPaddock`
4. **Modo headless**: se invocado com `--headless` (ex.: container Docker), o launcher omite a GUI e sobe apenas o Tomcat.

### 2.3 MainFrame — Modo Solo

`br.f1mane.MainFrame` abre a janela principal do jogo solo (1280×720 px). Em seu construtor:

- Instancia `ControleJogoLocal` como implementação de `InterfaceJogo`
- Instancia `PainelMenuLocal` como painel principal de navegação
- Exibe `JMenuBar` com menus: `menuJogo`, `menuInfo`, `menuDebug`

A partida é iniciada via menu, que chama `controleJogo.iniciarJogoMenuLocal(circuito, temporada, voltas, ...)`.

### 2.4 AppletPaddock — Modo Multiplayer Cliente

`br.f1mane.servidor.applet.AppletPaddock` é o cliente Java para jogar partidas hospedadas num servidor F1 Mane. Em `init()`:

- Instancia `ControlePaddockCliente`
- Chama `controlePaddockCliente.init()` e `controlePaddockCliente.logar()`
- Conecta ao servidor em `http://localhost:8080` (configurável)

### 2.5 MainFrameSimulacao — Modo Headless

`br.f1mane.MainFrameSimulacao` é usado para debug e testes de lógica de corrida sem interface gráfica. Aceita três argumentos posicionais: `temporada`, `circuito`, `voltas`.

Diferenças em relação ao `MainFrame`:

- Instancia `ControleJogoLocal` com seed fixo: `new ControleJogoLocal(3)`
- Desativa as três flags de rendering de `PainelCircuito`:
  - `PainelCircuito.desenhaBkg = false`
  - `PainelCircuito.desenhaPista = false`
  - `PainelCircuito.desenhaImagens = false`
- Define `Global.DEBUG = true`
- Janela menor: 1030×720 px

---

## 3. Engine de Jogo (Local)

### 3.1 ControleJogoLocal — Hub Central

`br.f1mane.controles.ControleJogoLocal` implementa `InterfaceJogo` e é o hub central de uma partida. Ele agrega os subsistemas como campos:

```
ControleJogoLocal
├── controleCorrida       : ControleCorrida       — física da corrida
├── gerenciadorVisual     : GerenciadorVisual      — rendering e UI
├── controleEstatisticas  : ControleEstatisticas   — rankings e estatísticas
├── controleCampeonato    : ControleCampeonato     — modo campeonato
├── pilotoSelecionado     : Piloto                 — piloto em foco na câmera
├── pilotoJogador         : Piloto                 — piloto humano
└── pilotosJogadores      : List<Piloto>            — todos os jogadores humanos
```

Flags de estado e feature:

| Campo | Tipo | Significado |
|---|---|---|
| `corridaTerminada` | `boolean` | corrida encerrada |
| `trocaPneu` | `boolean` | pit stop com troca de pneu habilitada |
| `reabastecimento` | `boolean` | pit stop com abastecimento habilitado |
| `ers` | `boolean` | ERS habilitado |
| `drs` | `boolean` | DRS habilitado |
| `safetyCar` | `boolean` | safety car habilitado (default: `true`) |

### 3.2 ControleCiclo — Tick Loop

`br.f1mane.controles.ControleCiclo` estende `Thread` e implementa o loop de simulação da corrida.

**Sequência de inicialização** (antes do loop principal):
1. `Thread.sleep(2000)` — pausa inicial
2. 5 iterações de `Thread.sleep(1000)` com pisca de semáforo — simula as luzes de largada

**Loop principal** (enquanto `!interrupt && processadoCilcos`):

```
para cada ciclo:
  1. controleCorrida.processaVoltaSafetyCar()
  2. para cada piloto:
       piloto.decrementaParadoBox()
       piloto.processarCiclo()
       piloto.calculaVelocidadeExibir()
       se piloto.isBox():
         controleCorrida.processarPilotoBox(piloto)
  3. controleCorrida.atualizaClassificacao()
  4. controleCorrida.verificaFinalCorrida()
  5. Thread.sleep(controleJogo.tempoCicloCircuito())
```

**Temporização**: `tempoCicloCircuito()` retorna o intervalo em ms definido em `circuitosCiclo`, um mapa estático que varia por circuito (tipicamente ~50ms). O flag estático `ControleCiclo.VALENDO` controla se o sleep é executado (desativado em testes).

### 3.3 Subsistemas da Corrida

#### ControleCorrida

`br.f1mane.controles.ControleCorrida` centraliza a física da corrida:

- Gerencia o avanço de cada piloto na pista (consumo de `ptosPista`)
- Calcula consumo de combustível e desgaste de pneus/motor por ciclo
- Detecta e processa acidentes entre pilotos adjacentes
- Gerencia ultrapassagens com base em `habilidade` e `modoPilotagem`
- Mantém e atualiza a classificação (posições)
- Detecta voltas completadas e o fim de corrida

#### ControleBox

`br.f1mane.controles.ControleBox` gerencia toda a lógica de pit stop:

| Campo | Tipo | Descrição |
|---|---|---|
| `entradaBox` | `No` | nó de entrada do pit lane |
| `saidaBox` | `No` | nó de saída do pit lane |
| `paradaBox` | `No` | nó de parada para serviço |
| `boxEquipes` | `Map<Carro, No>` | mapeamento carro → vaga da equipe |
| `boxEquipesOcupado` | `Hashtable<Carro, Carro>` | vagas ocupadas no momento |
| `boxRapido` | `boolean` | crew rápida (50% de chance, sorteado na inicialização) |

#### ControleSafetyCar

`br.f1mane.controles.ControleSafetyCar` gerencia a entrada e saída do safety car:

- Mantém referência à entidade `SafetyCar` (que implementa `PilotoSuave`)
- Ativa via `safetyCarNaPista(Piloto piloto)` — chamado quando há acidente grave
- Usa `ThreadRecolhimentoCarro` para remover carros acidentados da pista
- O método `ganhoComSafetyCar(ganho, controleJogo, piloto)` ajusta os ganhos de posição dos pilotos enquanto o SC está em pista
- Estado consultável via `isSaftyCarNaPista()`

#### ControleClima

`br.f1mane.controles.ControleClima` inicializa e gerencia mudanças climáticas:

- `gerarClimaInicial(Clima climaSel)` — define o clima inicial da corrida; respeita `Global.DEBUG_SEM_CHUVA`
- Executa em `ThreadMudancaClima` (thread separada) para simular transições climáticas durante a corrida
- Tipos de clima gerenciados: `Clima.SOL`, `Clima.CHUVA` e variantes

#### GerenciadorVisual

`br.f1mane.visao.GerenciadorVisual` é o agregador da interface gráfica do jogo:

- Cria e detém `PainelCircuito painelCircuito` (o canvas de rendering da corrida)
- Gerencia componentes de configuração pré-corrida:
  - `JComboBox` para circuito, temporada e piloto selecionado
  - `JSlider` para combustível e dificuldade de ultrapassagem
  - `JSpinner` para quantidade de voltas, skill padrão de pilotos e potência padrão de carros
  - `JCheckBox` para `trocaPneu`, `reabastecimento`, `ers`, `drs`
- Inicializa toda a interface via `iniciarInterfaceGraficaJogo() throws IOException`
- Expõe `exibirResultadoFinal()` que retorna e exibe `PainelTabelaResultadoFinal`

---

## 4. Camada Servidor e Multiplayer

### 4.1 PaddockServer — Singleton de Infraestrutura

`br.f1mane.servidor.PaddockServer` é o ponto de entrada da infraestrutura servidor. Mantém três singletons estáticos:

```java
private static ControlePaddockServidor controlePaddock
private static ControlePersistencia    controlePersistencia
private static MonitorAtividade        monitorAtividade
private static Boolean                 iniciado = Boolean.FALSE
```

O método `public static synchronized void init(String realpath)` é idempotente (guarda pelo flag `iniciado`) e executa em sequência:

1. Cria `ControlePersistencia` (acesso ao banco JPA)
2. Cria `ControlePaddockServidor` (lógica do paddock)
3. Cria e inicia `MonitorAtividade` (thread de monitoramento de sessões inativas)
4. Chama `Lang.setSrvgame(true)` — habilita o modo de encoding de i18n para servidor

`PaddockServer.init()` é chamado no startup do servlet container via listener configurado no `web.xml` do webapp.

### 4.2 REST API — LetsRace

`br.f1mane.servidor.rest.LetsRace` é o único endpoint REST do sistema, implementado com JAX-RS (Jersey). Mapeado em `/letsRace/*`.

**Autenticação**: todas as operações de jogo exigem um token de sessão passado no header HTTP `token`. Sessões são gerenciadas em `ControlePaddockServidor`.

**Endpoints principais:**

| Método | Path | Header(s) | Descrição |
|---|---|---|---|
| `GET` | `/letsRace/dadosToken` | `token` | Retorna dados da sessão do token |
| `GET` | `/letsRace/criarSessaoGoogle` | `idGoogle`, `nome`, `email`, `imagem` | Cria sessão autenticada via Google |
| `GET` | `/letsRace/criarSessaoVisitante` | — | Cria sessão de convidado |
| `GET` | `/letsRace/criarSessaoNome` | `nome` | Cria sessão por nome simples |
| `GET` | `/letsRace/circuito` | `token` | Retorna dados do circuito (`@Compress`) |

Todas as respostas são `@Produces(APPLICATION_JSON)` e retornam objetos `Response` com status HTTP e payload JSON.

### 4.3 Gerenciamento de Sessão e Jogos

#### SessaoCliente

`br.f1mane.servidor.entidades.TOs.SessaoCliente` carrega o estado de um jogador conectado:

| Campo | Tipo | Descrição |
|---|---|---|
| `token` | `String` | identificador único da sessão |
| `nomeJogador` | `String` | nome de exibição |
| `email` | `String` | e-mail do jogador |
| `idUsuario` | `String` | ID externo (Google ou gerado) |
| `imagemJogador` | `String` | URL do avatar |
| `jogoAtual` | `String` | nome do jogo em andamento |
| `pilotoAtual` | `String` | nome do piloto escolhido |
| `idPilotoAtual` | `Integer` | ID do piloto na temporada |
| `ultimaAtividade` | `long` | timestamp da última chamada (ms) |
| `guest` | `boolean` | sessão de convidado |

#### ControleJogosServer e JogoServidor

`br.f1mane.servidor.controles.ControleJogosServer` gerencia o ciclo de vida das instâncias de jogo:

```java
private Map<SessaoCliente, JogoServidor> mapaJogosCriados
public static final int MaxJogo = 5    // limite de jogos simultâneos
public static int qtdeJogos = 0        // contador atual
```

Quando um cliente solicita criar um jogo, `criarJogo(ClientPaddockPack)`:
1. Valida que o usuário não tem jogo ativo (`mapaJogosCriados`)
2. Verifica que `qtdeJogos < MaxJogo`
3. Instancia `JogoServidor` com a temporada solicitada
4. Retorna `SrvPaddockPack` com dados do jogo criado

`br.f1mane.servidor.JogoServidor` estende `ControleJogoLocal` — cada instância é um engine de corrida completo rodando no servidor para aquela partida.

`ControlePaddockServidor` coordena também `ControleClassificacao` (ranking global de jogadores) e `ControleCampeonatoServidor` (campeonatos persistidos no banco).

### 4.4 JogoCliente — Cliente do Protocolo

`br.f1mane.servidor.applet.JogoCliente` estende `ControleRecursos` e implementa `InterfaceJogo`. É o objeto que representa o estado local do jogo no cliente multiplayer.

**Nota sobre implementação parcial**: `JogoCliente` implementa os ~400 métodos de `InterfaceJogo`, mas a grande maioria retorna valores neutros (`null`, `0`, `false`) porque o estado real da corrida reside no servidor. O cliente recebe atualizações periódicas via polling REST e as aplica diretamente nos objetos `Piloto`/`Carro` locais.

Os métodos com implementação funcional no cliente incluem:
- `exibirResultadoFinal()` — delega para `gerenciadorVisual.exibirResultadoFinal()`
- `iniciaJanela()` — inicializa o painel do cliente
- Getters de estado local: `getCircuito()`, `getPilotosCopia()`, `getCarros()`, `getClima()`

---

## 5. Modelo de Dados

### 5.1 Piloto

`br.f1mane.entidades.Piloto` representa um piloto durante a corrida.

**Identidade e configuração:**

| Campo | Tipo | Descrição |
|---|---|---|
| `id` | `int` | ID único do piloto na temporada |
| `nome` | `String` | nome completo |
| `nomeAbreviado` | `String` | nome curto (ex.: "VER") |
| `habilidade` | `int` | habilidade base (1–99) |
| `habilidadeReal` | `int` | habilidade efetiva com ajustes |
| `carro` | `Carro` | carro atribuído ao piloto |
| `jogadorHumano` | `boolean` | `true` para o jogador humano |

**Estado de corrida:**

| Campo | Tipo | Descrição |
|---|---|---|
| `posicao` | `int` | posição atual na classificação |
| `numeroVolta` | `int` | volta atual |
| `ptosPista` | `long` | índice de avanço na pista (acumulado) |
| `velocidade` | `int` | velocidade de simulação |
| `velocidadeExibir` | `int` | velocidade exibida na UI (km/h) |
| `stress` | `int` | nível de stress do piloto (afeta erros) |
| `modoPilotagem` | `String` | `"AGRESSIVO"`, `"NORMAL"` ou `"LENTO"` |
| `noAtual` | `No` | nó da pista onde o piloto está |
| `voltaAtual` | `Volta` | objeto da volta em andamento |
| `melhorVolta` | `Volta` | melhor volta registrada |

**Flags de estado:**

| Campo | Tipo | Descrição |
|---|---|---|
| `box` | `boolean` | piloto está no pit lane |
| `desqualificado` | `boolean` | piloto foi desqualificado |
| `ativarErs` | `boolean` | ERS ativo neste ciclo |
| `ativarDRS` | `boolean` | DRS ativo neste ciclo |
| `cargaErsVisual` | `int` | carga ERS para exibição |
| `qtdeParadasBox` | `int` | número de pit stops realizados |

### 5.2 Carro

`br.f1mane.entidades.Carro` representa o carro de um piloto com seu estado físico e configuração.

**Tipos de pneu:**

```java
TIPO_PNEU_MOLE  = "TIPO_PNEU_MOLE"   // pneu macio (rápido, desgasta mais)
TIPO_PNEU_DURO  = "TIPO_PNEU_DURO"   // pneu duro (lento, mais durável)
TIPO_PNEU_CHUVA = "TIPO_PNEU_CHUVA"  // pneu de chuva
```

**Estados de dano** (`danificado: String`):

```java
PNEU_FURADO        = "PNEU_FURADO"         // pneu furado — reduz velocidade
PERDEU_AEREOFOLIO  = "PERDEU_AEREOFOLIO"   // asa danificada — perde downforce
BATEU_FORTE        = "BATEU_FORTE"         // acidente grave — carro lento
PANE_SECA          = "PANE_SECA"           // pane mecânica
EXPLODIU_MOTOR     = "EXPLODIU_MOTOR"      // motor fundido — DNF
```

**Configuração de asa (DRS/downforce):**

```java
MAIS_ASA   = "MAIS_ASA"    // maior downforce (curvas)
ASA_NORMAL = "ASA_NORMAL"  // configuração padrão
MENOS_ASA  = "MENOS_ASA"   // menor downforce (velocidade máxima)
```

**Resposta de direção (giro):**

```java
GIRO_MIN_VAL = 1   // subesterçante
GIRO_NOR_VAL = 5   // neutro
GIRO_MAX_VAL = 9   // sobreesterçante
```

**Dimensões para rendering:**

```java
LARGURA = 62, ALTURA = 24          // vista lateral
LARGURA_CIMA = 86, ALTURA_CIMA = 86  // vista superior
MEIA_LARGURA = 31, MEIA_ALTURA = 12
RAIO_DERRAPAGEM = 155
```

**Campos de consumo e desgaste:**

| Campo | Tipo | Descrição |
|---|---|---|
| `porcentagemCombustivel` | `int` | combustível restante (%) |
| `porcentagemDesgastePneus` | `int` | desgaste dos pneus (%) |
| `porcentagemDesgasteMotor` | `int` | desgaste do motor (%) |
| `potencia` | `int` | potência base (HP) |
| `potenciaReal` | `int` | potência efetiva com ajustes |
| `aerodinamica` | `int` | downforce efetivo |
| `freios` | `int` | eficiência de freios |
| `cargaErs` | `int` | energia armazenada no ERS |
| `combustivel` | `int` | combustível atual (litros) |
| `tanqueCheio` | `int` | capacidade máxima do tanque |
| `temperaturaMotor` | `int` | temperatura do motor |
| `temperaturaPneus` | `int` | temperatura dos pneus |
| `cor1`, `cor2` | `Color` | cores primária e secundária da equipe |

### 5.3 No

`br.f1mane.entidades.No` é a unidade atômica de um circuito — um ponto com tipo, coordenadas e metadados de pit.

**Tipos de nó** (identificados por constantes `Color`):

```java
LARGADA    = Color.BLUE    // linha de largada/chegada
RETA       = Color.GREEN   // trecho de reta
CURVA_ALTA = Color.YELLOW  // curva de alta velocidade
CURVA_BAIXA= Color.RED     // curva de baixa velocidade
BOX        = Color.CYAN    // pit lane
PARADA_BOX = Color.ORANGE  // vaga de parada no pit
FIM_BOX    = Color.PINK    // saída do pit lane
```

**Campos:**

| Campo | Tipo | Descrição |
|---|---|---|
| `point` | `Point` | coordenadas X,Y no canvas |
| `index` | `int` | índice sequencial na lista de nós |
| `tipo` | `Color` | tipo do nó (constante acima) |
| `tracado` | `int` | ID da linha de traçado (múltiplos traçados por circuito) |
| `box` | `boolean` | nó pertence ao pit lane |
| `noEntradaBox` | `boolean` | marcador de entrada do pit |
| `noSaidaBox` | `boolean` | marcador de saída do pit |

**Métodos de consulta de tipo:**

```java
boolean verificaRetaOuLargada()  // LARGADA ou RETA
boolean verificaCurvaAlta()      // CURVA_ALTA
boolean verificaCurvaBaixa()     // CURVA_BAIXA
```

### 5.4 Circuito

`br.f1mane.entidades.Circuito` agrega a pista completa como listas de `No`.

**Listas de nós:**

| Campo | Tipo | Descrição |
|---|---|---|
| `pista` | `List<No>` | nós brutos carregados do XML |
| `pistaFull` | `List<No>` | nós interpolados por `vetorizarPista()` |
| `box` | `List<No>` | nós brutos do pit lane |
| `boxFull` | `List<No>` | nós interpolados do pit lane |

`vetorizarPista(double multi, double larg)` gera `pistaFull` e `boxFull` interpolando pontos entre os nós brutos com os fatores de multiplicação e largura fornecidos.

**Configuração do circuito:**

| Campo | Tipo | Descrição |
|---|---|---|
| `nome` | `String` | nome do circuito (ex.: `"Catalunya"`) |
| `backGround` | `String` | nome do arquivo de imagem de fundo (JPG) |
| `probalidadeChuva` | `int` | probabilidade de chuva (0–100) |
| `velocidadePista` | `double` | fator de velocidade global da pista |
| `noite` | `boolean` | corrida noturna |
| `ladoBox` | `int` | lado do pit lane (`0`=esquerda, `1`=direita) |
| `usaBkg` | `boolean` | usar imagem de fundo |
| `entradaBoxIndex` | `int` | índice do nó de entrada do pit |
| `saidaBoxIndex` | `int` | índice do nó de saída do pit |
| `paradaBoxIndex` | `int` | índice do nó de parada do pit |

Objetos trackside são armazenados em `List<ObjetoPista> objetos` (arquibancadas, guard rails, escapatórias, pneus, etc.) e em `List<ObjetoPistaJSon> objetosNoTransparencia` para elementos transparentes.

---

## 6. Persistência

### 6.1 Perfis Maven e Configuração de Banco

O projeto usa dois perfis Maven para alternar entre bancos de dados:

| Perfil | Banco | URL | Uso |
|---|---|---|---|
| `h2` (default) | H2 (embedded) | `~/flmane-data/flmane` | desenvolvimento local |
| `mysql` | MySQL 8.4 | `db:3306/flmane` | Docker / produção |

O arquivo `META-INF/persistence.xml` contém placeholders `${jdbc.url}`, `${jdbc.driver}` etc. O **filtro de recursos do Maven** (`<filtering>true</filtering>` em `pom.xml`) injeta os valores do perfil ativo durante o `mvn package`, antes de empacotar o JAR. Não há seleção de perfil em runtime — o banco é definido no momento do build.

Para o build com H2:
```bash
mvn clean package -Ph2 -DskipTests
```

Para o build com MySQL (Docker):
```bash
mvn clean package -Pmysql -DskipTests
```

### 6.2 Entidades JPA

Todas as entidades de persistência estendem `F1ManeDados`:

```
F1ManeDados  (@MappedSuperclass)
├── id            : Long    @Id @GeneratedValue(AUTO)
├── dataCriacao   : Date    @Column(nullable=false)
└── loginCriador  : String  @Column(nullable=false, default="Sistema")
```

**Entidades concretas:**

| Classe | Tabela | Relacionamentos principais |
|---|---|---|
| `JogadorDadosSrv` | `usuario` | `@OneToMany → CorridasDadosSrv` (cascade=ALL, lazy) |
| `CampeonatoSrv` | `f1_campeonatosrv` | `@OneToMany → CorridaCampeonatoSrv` (cascade=ALL, lazy) |
| `CorridaCampeonatoSrv` | — | corrida individual dentro de um campeonato |
| `CorridasDadosSrv` | — | dados de corrida vinculados a `JogadorDadosSrv` |
| `DadosCorridaCampeonatoSrv` | — | dados detalhados de corrida de campeonato |
| `CarreiraDadosSrv` | — | progressão de carreira do jogador |

Todas as entidades ficam em `br.f1mane.servidor.entidades.persistencia`. O acesso ao banco é centralizado em `ControlePersistencia`, que gerencia o `EntityManagerFactory` e as operações CRUD.

---

## 7. Rendering

### 7.1 PainelCircuito

`br.f1mane.visao.PainelCircuito` é o canvas principal de rendering da corrida. Estende `JPanel` e usa o modelo padrão Swing: o rendering ocorre em `paintComponent(Graphics g)` — não há método `render()` explícito.

**Flags estáticas de rendering:**

```java
public static boolean desenhaBkg    = true  // renderiza imagem de fundo
public static boolean desenhaPista  = true  // renderiza traçado da pista
public static boolean desenhaImagens= true  // renderiza sprites de carros
```

No modo `MainFrameSimulacao`, as três flags são definidas como `false` antes de iniciar a simulação, eliminando o overhead gráfico.

**Overlays de debug**: `Global.DEBUG = true` ativa visualizações extras (nós da pista numerados, caixas de colisão, etc.). Ao ativar, o código de rendering verifica `pilotoSelecionado != null` antes de acessar propriedades de nós — o rendering roda em thread separada e o piloto pode ser `null` durante a inicialização.

**Buffers de imagem principais carregados em inicialização:**

| Campo | Descrição |
|---|---|
| `carroimgDano` | sprite de carro danificado |
| `pneuMoleImg`, `pneuDuroImg`, `pneuChuvaImg` | ícones de tipo de pneu |
| `setaCarroCima`, `setaCarroBaixo` | setas direcionais de câmera |
| `gridCarro` | overlay de grid de largada |
| `travadaRodaImg0/1/2` | efeitos de roda travada |
| `carroCimaFreios*` | efeitos de frenagem |
| `imgFarois*` | faróis (corridas noturnas) |

### 7.2 SpriteSheet

`br.f1mane.recursos.SpriteSheet` gerencia a extração de sprites de uma única imagem por temporada. A imagem fonte é carregada de `sprites/tANO.png` (onde `ANO` é o ano da temporada, ex.: `t2024.png`).

**Layout de pixels:**

```
Y=0   ┌─────────────────────────────────────────────┐
      │ Carros Vista Lateral  (LADO_W=180 × LADO_H=40)
      │ idx=0  idx=1  idx=2  ...                    │
Y=40  ├─────────────────────────────────────────────┤
      │ Carros Vista Superior (CIMA_W=90 × CIMA_H=90)
      │ idx=0  idx=1  idx=2  ...                    │
Y=130 ├─────────────────────────────────────────────┤
      │ Capacetes Linha 1  (CAP_W=55 × CAP_H=55)   │
      │ idx 0–11  (CAP_PER_ROW=12 por linha)        │
Y=185 ├─────────────────────────────────────────────┤
      │ Capacetes Linha 2                           │
      │ idx 12–23                                   │
      └─────────────────────────────────────────────┘
```

**Métodos de extração:**

```java
// Carro vista lateral: subimage(idx * 180, 0, 180, 40)
BufferedImage getCarroLado(String temporada, int idx)

// Carro vista superior: subimage(idx * 90, 40, 90, 90)
BufferedImage getCarroCima(String temporada, int idx)

// Capacete: row = idx / 12 ; col = idx % 12
// Y = (row == 0) ? Y_CAP1 : Y_CAP2
// subimage(col * 55, Y, 55, 55)
BufferedImage getCapacete(String temporada, int idx)
```

O `idx` corresponde à posição do piloto na lista de pilotos da temporada carregada por `CarregadorRecursos`.

O script `gerar_spritesheets.py` (na raiz do repositório) monta as imagens de spritesheet a partir de imagens individuais em `carros/` e `capacetes/`.

---

## 8. Recursos e Internacionalização

### 8.1 CarregadorRecursos

`br.f1mane.recursos.CarregadorRecursos` é um singleton responsável por carregar e cachear todos os recursos do jogo via classpath.

**Obtenção do singleton:**
```java
CarregadorRecursos.getCarregadorRecursos(boolean cache)
```
O parâmetro `cache` controla se os resultados são armazenados nos buffers estáticos.

**Caches estáticos:**

| Campo | Tipo | Conteúdo |
|---|---|---|
| `bufferImages` | `Map` | imagens de fundo dos circuitos |
| `bufferCarros` | `Map` | listas de carros por temporada |
| `bufferCircuitos` | `Map<String, Circuito>` | circuitos deserializados |
| `bufferCapacete` | `Map<String, BufferedImage>` | sprites de capacete |

**Circuitos**: 37 circuitos definidos em `src/main/resources/circuitos/`, no formato `{nome}_mro.xml`. São deserializados com `java.beans.XMLDecoder`:

```java
// Carrega e cacheia um circuito pelo nome
Circuito carregarCircuito(String nmCircuito)

// Lista todos os circuitos disponíveis
List<CircuitosDefault> carregarCircuitosDefaults()
```

**Pilotos e carros por temporada**: carregados de `src/main/resources/properties/tANO/`:
- `pilotos.properties` → `List<Piloto>`
- `carros.properties` → `List<Carro>`

A lista de temporadas disponíveis é carregada de `properties/temporadas.properties` via `carregarTemporadas()`, que retorna um `Vector<String>`.

### 8.2 Internacionalização — Lang

`br.f1mane.recursos.idiomas.Lang` centraliza toda a resolução de mensagens do sistema.

**Resolução em modo local (Swing):**

```java
Lang.msg("chave")
// → PropertyResourceBundle.getString("chave")
// Bundle carregado de: src/main/resources/idiomas/mensagens_XX.properties
```

Idiomas disponíveis: `pt` (Português), `en` (Inglês), `es` (Espanhol), `it` (Italiano).

Para trocar o idioma em runtime:
```java
Lang.mudarIdioma("_en")  // sufixo do arquivo properties
```

**Resolução em modo servidor:**

Quando `Lang.setSrvgame(true)` é chamado (feito por `PaddockServer.init()`), `Lang.msg(key)` retorna:
```
"¢" + key + "¢"
```

Isso adia a tradução para o cliente: o servidor envia as chaves encoded, e a tradução ocorre no cliente com base no idioma do jogador. O idioma é passado por request no header HTTP `idioma`.

**Mensagens com parâmetros:**
```java
Lang.msg("chave", new Object[]{ param1, param2 })
// → MessageFormat.format(bundle.getString("chave"), params)
```
