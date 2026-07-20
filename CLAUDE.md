# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## IMPORTANTE: nunca desfazer alterações

Nunca desfaça, reverta ou descarte uma alteração no working tree — seja ela feita manualmente pelo usuário (ex.: no editor de circuitos, rodando em paralelo a uma sessão do Claude Code) ou gerada por código/ferramenta (ex.: um teste, um build, o próprio Claude). Isso vale mesmo que a alteração pareça um efeito colateral inesperado de rodar `mvn test` ou qualquer outro comando: não presuma que é algo a "limpar" — investigue a causa antes, e por padrão mantenha o arquivo como está.

Concretamente: não rode `git checkout --`, `git restore`, `git reset --hard`/`--mixed`, `git clean` ou qualquer comando destrutivo equivalente sobre arquivos modificados, a menos que o usuário peça explicitamente naquele momento. Se uma alteração já está no working tree (comitada ou não), o padrão é mantê-la e, quando fizer sentido, comitá-la — nunca descartá-la silenciosamente. Isso inclui alterações que pareçam "só código gerado": também devem ser preservadas e comitadas, não descartadas.

Esta diretiva existe porque, numa sessão anterior, `circuitos.properties` foi revertido repetidas vezes via `git checkout` por engano — presumindo (sem confirmar) que fosse efeito colateral de teste — quando na verdade era o usuário salvando de verdade o campo "Ativo" de um circuito no editor rodando em paralelo. O salvamento real do usuário foi desfeito várias vezes sem que ele soubesse.

## IMPORTANTE: manter o binário atualizado após qualquer alteração

Sempre que uma alteração em código Java (`src/main/java/...`) for feita nesta sessão, rode `mvn clean package -Ph2 -DskipTests` (gera `target/flmane.jar`) antes de considerar a tarefa concluída ou de pedir para o usuário testar manualmente. `mvn test`/`mvn compile` sozinhos recompilam as classes, mas **não** atualizam o jar que o usuário efetivamente roda (`java -cp target/flmane.jar br.flmane.editor.EditorCircuitos` ou o atalho "FlMane") — testar contra um jar desatualizado é a causa mais provável de "corrigi isso e continua acontecendo" quando o código-fonte já está correto.

Esta diretiva existe porque, numa sessão anterior, uma correção de bug foi validada só por testes automatizados (que recompilam a partir do source) e o usuário reportou que o problema "ainda" acontecia ao testar manualmente — boa parte da investigação subsequente foi gasta descartando hipóteses de bug no código antes de considerar que o jar testado podia estar desatualizado.

## Build & Run

```bash
# Rodar a suíte de testes unitários (JUnit 5 + Mockito)
mvn test

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

**IMPORTANTE: nunca mover `src/main/resources/logback-test.xml`.** Ele parece estar no lugar errado à primeira vista — só o perfil `test` o inclui no build, o que sugere que deveria morar em `src/test/resources`. Não é o caso: o `pom.xml` já tem o mecanismo certo. O build padrão exclui `logback-test.xml` explicitamente do `src/main/resources` (`<exclude>logback-test.xml</exclude>`), e só o perfil `test` o inclui de volta, de propósito, gerando um jar de debug com saída no console (nível DEBUG) em vez do `logback.xml` de produção (nível INFO, só arquivo). Mover o arquivo pra `src/test/resources` quebra esse mecanismo — `mvn package -Ptest` aponta pra `src/main/resources` e para de encontrá-lo, e o jar final perde o log de console mesmo rodando com `-Ptest`. Se precisar silenciar um logger específico em DEBUG (ex.: `org.apache.commons.beanutils`, que loga "Converting ... to type ..." em todo `describe()`/`copyProperties()`), adicione um `<logger name="..." level="WARN"/>` dentro do próprio arquivo — não mexa na localização dele.

## Docker Compose

Inclui três serviços: `flmane` (porta 80→8080), `db` (MySQL 8.4) e `phpmyadmin` (porta 8080). O container aguarda o healthcheck do MySQL antes de subir.

## Debugging

`Global.DEBUG = true` ativa overlays visuais na `PainelCircuito`. Ao ativar, verificar sempre se `pilotoSelecionado != null` antes de acessar nós — o rendering roda em thread separada e o piloto pode ser null durante inicialização.

Logs em `logs/flmane.YYYY-MM-DD.N.log` (rotação diária via Logback).

## Testes e Swing

Testes NUNCA devem disparar componentes Swing reais (`JOptionPane`, `JDialog`, `JFrame.setVisible(true)`, etc.). Vários testes de `MainPanelEditor` disparam `MouseEvent` reais para exercitar o `MouseAdapter` do editor (ex.: `MainPanelEditorEscapadaCliqueTest`), e caminhos de validação inválidos (ex.: ponto de escapada fora do traçado) chamam `alertaPontoEscapadaInvalido()`, que abre um `JOptionPane.showMessageDialog` de verdade — isso trava a suíte esperando interação do usuário.

Para cobrir esses caminhos sem abrir diálogo: use/estenda `MainPanelEditorTestDouble` (em `src/test/java/br/f1mane/editor/`), que sobrescreve `alertaPontoEscapadaInvalido()` para só registrar a chamada. Ao adicionar um novo método de alerta/diálogo em `MainPanelEditor`, torne-o `protected` (não `private`) e adicione o override correspondente no double, em vez de deixar o teste chamar o `JOptionPane` real.
