# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Fat jar com H2 (desenvolvimento local)
mvn clean package -Ph2 -DskipTests

# Fat jar com MySQL (produção/Docker)
mvn clean package -Pmysql -DskipTests

# Build completo + Docker (para o registry)
./build.sh

# Rodar o jogo (modo web — Tomcat embutido na porta 8080)
java -jar target/flmane.jar
# Abre launcher Swing com QR Code; web em http://localhost:8080/flmane/html5/index.html

# Rodar modo solo (Swing puro)
java -cp target/flmane.jar br.f1mane.MainFrame

# Simular uma corrida headless (útil para debug de lógica)
./simulacao.sh
# equivalente a: java -cp target/flmane.jar br.f1mane.MainFrameSimulacao 2024 Catalunya 72
```

O Maven incrementa automaticamente o campo `versao` em `application.properties` a cada `mvn package`.

## Arquitetura

### Modos de execução

O mesmo JAR serve três modos distintos:

| Entry point | Modo |
|---|---|
| `MainLauncher` | Servidor web (Tomcat embutido) + launcher Swing |
| `MainFrame` | Jogo solo em Java Swing |
| `AppletPaddock` | Cliente Java do modo multiplayer |

`MainLauncher` extrai o diretório `webapp/` do JAR para um dir temporário e sobe o Tomcat. Em Docker, é invocado com `--headless` (sem GUI).

### Camada de jogo (local)

`ControleJogoLocal` (implementa `InterfaceJogo`) é o hub central de uma partida solo. Ele agrega:
- `ControleCorrida` — física da corrida: voltas, consumo, acidentes, ultrapassagens
- `ControleCiclo` — tick loop da simulação
- `ControleBox` — lógica de pit stop
- `ControleSafetyCar` — ativação e recolhimento do safety car
- `ControleClima` — mudanças climáticas (thread separada: `ThreadMudancaClima`)
- `ControleEstatisticas` — rankings e estatísticas ao vivo
- `ControleQualificacao` — grid de largada
- `GerenciadorVisual` — thread de rendering (chama `PainelCircuito.render()` em loop)

### Camada servidor (multiplayer web)

`PaddockServer` é um singleton que inicializa `ControlePaddockServidor` e `MonitorAtividade`. O REST endpoint único é `LetsRace` (JAX-RS Jersey), mapeado em `/letsRace/*`. Toda autenticação é por token em header HTTP; `SessaoCliente` carrega o estado do jogador.

O `ControlePaddockServidor` delega a `ControleJogosServer` para operações durante a corrida (giro motor, agressividade, DRS, ERS, box, traçado).

### Circuitos e `No`

Um circuito é uma sequência de objetos `No` (nós), cada um com coordenadas (`Point`), tipo (reta, curva alta/baixa, box, largada) e índice sequencial. Os circuitos são definidos em XML (`src/main/resources/circuitos/*.xml`) e imagens JPG de fundo. `Circuito.vetorizarPista()` converte o XML em lista de nós.

### SpriteSheet

`SpriteSheet` carrega uma imagem por temporada (`sprites/tANO.png`) que contém, numa única imagem: carros de lado (linha Y=0), carros de cima (Y=40), capacetes linha 1 (Y=130) e linha 2 (Y=185). O layout de pixels é definido pelas constantes `LADO_W/H`, `CIMA_W/H`, `CAP_W/H`. `gerar_spritesheets.py` monta esses sprites a partir das imagens individuais em `carros/` e `capacetes/`.

### Temporadas e recursos

Dados de pilotos e carros vivem em `src/main/resources/properties/tANO/pilotos.properties` e `carros.properties`. `CarregadorRecursos` carrega tudo via classpath. O filtro de persistência em `pom.xml` injeta os valores de JDBC do perfil ativo em `META-INF/persistence.xml` no momento do build.

### i18n

Todas as mensagens ao usuário passam por `Lang.msg("chave")` ou `Lang.decodeTextoKey(texto, idioma)`. Os bundles ficam em `src/main/resources/idiomas/mensagens_XX.properties`. O REST envia o idioma no header `idioma` e a tradução acontece no servidor antes de retornar ao cliente.

## Perfis Maven

| Perfil | Banco | Uso |
|---|---|---|
| `h2` (default) | H2 em `~/flmane-data/flmane` | desenvolvimento local |
| `mysql` | MySQL em `db:3306/flmane` | Docker / produção |
| `test` | — | inclui `logback-test.xml` |

## Docker Compose

Inclui três serviços: `flmane` (porta 80→8080), `db` (MySQL 8.4) e `phpmyadmin` (porta 8080). O container aguarda o healthcheck do MySQL antes de subir.

## Debugging

`Global.DEBUG = true` ativa overlays visuais na `PainelCircuito`. Ao ativar, verificar sempre se `pilotoSelecionado != null` antes de acessar nós — o rendering roda em thread separada e o piloto pode ser null durante inicialização.

Logs em `logs/flmane.YYYY-MM-DD.N.log` (rotação diária via Logback).
