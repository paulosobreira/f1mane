# launcher-servidor-processo-separado

## Purpose
Documents that the GUI launcher spawns the backend (Tomcat + PaddockServer) in a separate child JVM via `ProcessBuilder`, manages that child process's lifecycle, and hosts the Swing game applications (`MainFrame`, `AppletPaddock`) in-process instead of spawning them.

## Requirements

### Requirement: Launcher GUI inicia o backend em processo separado
Quando executado sem argumentos (modo GUI), `MainLauncher` SHALL iniciar o backend (Tomcat + PaddockServer) numa JVM filha via `ProcessBuilder` (`java -cp <jar> br.f1mane.MainLauncher --headless`), e NÃO SHALL subir o Tomcat na própria JVM. A URL exibida (QR Code e campo de texto) SHALL continuar sendo calculada localmente via descoberta de IP + porta 8080.

#### Scenario: Execução GUI spawna o servidor como filho
- **WHEN** `MainLauncher` é executado sem argumentos
- **THEN** um processo filho `java ... br.f1mane.MainLauncher --headless` é iniciado, o Tomcat não é instanciado na JVM do launcher, e a janela do launcher mostra o QR Code com a URL do servidor

#### Scenario: Logs do servidor visíveis no console do launcher
- **WHEN** o processo filho do servidor escreve em stdout/stderr
- **THEN** a saída aparece no console do launcher (`inheritIO()`), incluindo o banner "SERVER STARTED"

### Requirement: Parâmetro fixo --headless aciona o modo servidor in-process
Quando executado com o parâmetro **`--headless`** (casos Docker e servidor), `MainLauncher` SHALL extrair `webapp/` do JAR, subir o Tomcat na própria JVM na porta 8080, não exibir nenhuma GUI/QR Code e não iniciar nenhum processo filho.

#### Scenario: Headless sobe Tomcat in-process
- **WHEN** `MainLauncher --headless` é executado
- **THEN** o Tomcat roda na mesma JVM, nenhuma janela Swing é criada e nenhum `ProcessBuilder` é acionado

### Requirement: Encerrar o launcher encerra o processo do servidor
O launcher SHALL gerenciar o ciclo de vida do processo filho: ao encerrar a JVM do launcher (fechamento da janela com `EXIT_ON_CLOSE` ou término normal), o processo filho do servidor SHALL ser destruído via shutdown hook, sem deixar JVM órfã.

#### Scenario: Fechar a janela do launcher mata o servidor
- **WHEN** o usuário fecha a janela do launcher
- **THEN** o shutdown hook chama `destroy()` no processo filho e o servidor é encerrado

### Requirement: Aplicações Swing rodam na JVM do launcher
As opções "jogo solo" (`MainFrame`) e "jogo multiplayer" (`AppletPaddock`) do launcher SHALL executar na mesma JVM do launcher (sem `ProcessBuilder`), em modo hospedado com `DISPOSE_ON_CLOSE`, de forma que fechar a janela do jogo não encerre a JVM do launcher.

#### Scenario: Jogo solo abre in-JVM
- **WHEN** o usuário clica na opção de jogo solo do launcher
- **THEN** o `MainFrame` abre na mesma JVM, sem novo processo `java`

#### Scenario: Fechar o jogo solo não fecha o launcher
- **WHEN** o usuário fecha a janela do `MainFrame` aberto pelo launcher
- **THEN** a janela do jogo é descartada (`DISPOSE_ON_CLOSE`) e o launcher e o processo do servidor continuam rodando

#### Scenario: Multiplayer abre in-JVM
- **WHEN** o usuário clica na opção de jogo multiplayer do launcher
- **THEN** o `AppletPaddock` abre na mesma JVM, sem novo processo `java`, e fechá-lo não encerra o launcher
