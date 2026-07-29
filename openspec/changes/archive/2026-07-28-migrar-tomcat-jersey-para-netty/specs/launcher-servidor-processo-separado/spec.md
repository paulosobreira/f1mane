## MODIFIED Requirements

### Requirement: Launcher GUI inicia o backend em processo separado
Quando executado sem argumentos (modo GUI), `MainLauncher` SHALL iniciar o backend (Netty + `PaddockServer`) numa JVM filha via `ProcessBuilder` (`java -cp <jar> br.f1mane.MainLauncher --headless`), e NÃO SHALL subir o servidor Netty na própria JVM. A URL exibida (QR Code e campo de texto) SHALL continuar sendo calculada localmente via descoberta de IP + porta 8080.

#### Scenario: Execução GUI spawna o servidor como filho
- **WHEN** `MainLauncher` é executado sem argumentos
- **THEN** um processo filho `java ... br.f1mane.MainLauncher --headless` é iniciado, o servidor Netty não é instanciado na JVM do launcher, e a janela do launcher mostra o QR Code com a URL do servidor

#### Scenario: Logs do servidor visíveis no console do launcher
- **WHEN** o processo filho do servidor escreve em stdout/stderr
- **THEN** a saída aparece no console do launcher (`inheritIO()`), incluindo o banner "SERVER STARTED"

### Requirement: Parâmetro fixo --headless aciona o modo servidor in-process
Quando executado com o parâmetro **`--headless`** (casos Docker e servidor), `MainLauncher` SHALL extrair `webapp/` do JAR, subir o servidor Netty na própria JVM na porta 8080, não exibir nenhuma GUI/QR Code e não iniciar nenhum processo filho.

#### Scenario: Headless sobe Netty in-process
- **WHEN** `MainLauncher --headless` é executado
- **THEN** o servidor Netty roda na mesma JVM, nenhuma janela Swing é criada e nenhum `ProcessBuilder` é acionado
