## ADDED Requirements

### Requirement: Processo headless roda com AWT em modo headless real
Todo processo do jogo que sobe sem interface gráfica — o `RUN` de pré-geração de imagens do `flmane.dockerfile`, o `ENTRYPOINT` do container e o processo filho iniciado por `MainLauncher.iniciarProcessoServidor` — SHALL rodar com `-Djava.awt.headless=true`, de modo que nenhuma inicialização de toolkit gráfico nativo (X11/fontconfig) ocorra no processo servidor.

#### Scenario: ENTRYPOINT do container sobe headless
- **WHEN** o container `flmane` é iniciado a partir da imagem construída por `flmane.dockerfile`
- **THEN** o processo Java resultante tem a propriedade de sistema `java.awt.headless` igual a `true`

#### Scenario: Pré-geração de imagens continua headless
- **WHEN** a etapa `--pre-gerar-imagens` roda durante o `docker build`
- **THEN** o processo roda com `java.awt.headless=true` e gera as imagens normalmente, sem depender de display

#### Scenario: Modo GUI não é afetado
- **WHEN** o usuário roda `java -jar target/flmane.jar` sem `--headless` (launcher Swing) ou `java -cp target/flmane.jar br.f1mane.MainFrame`
- **THEN** o processo continua com AWT não-headless e a interface Swing abre normalmente

### Requirement: Heap do container dimensionado pelo limite de memória do cgroup
O `ENTRYPOINT` do `flmane.dockerfile` SHALL definir o heap máximo como percentual da memória disponível ao container (`-XX:MaxRAMPercentage`) em vez de depender do default da JVM, de forma que o limite de memória do container (ou a ausência dele) governe o heap sem edição de imagem.

#### Scenario: Container com limite de memória definido
- **WHEN** o container `flmane` roda com um limite de memória imposto pelo runtime (ex.: `--memory=1g`)
- **THEN** o heap máximo da JVM é derivado desse limite pelo percentual configurado, e não do default de 1/4 da memória da máquina hospedeira

#### Scenario: Servidor sobe e serve requisições com o heap configurado
- **WHEN** o container sobe com as flags de memória aplicadas
- **THEN** a pré-geração é pulada (imagens assadas no build), a porta é bindada e as requisições de imagem e de corrida são atendidas normalmente

### Requirement: Flags de runtime documentadas e verificáveis
As flags de JVM do modo headless SHALL estar concentradas no `flmane.dockerfile` (não espalhadas em scripts) e documentadas no `CLAUDE.md`, com uma verificação automatizada que falhe se o `ENTRYPOINT` perder `-Djava.awt.headless=true`.

#### Scenario: Regressão de flag detectada
- **WHEN** o `ENTRYPOINT` do `flmane.dockerfile` é alterado removendo `-Djava.awt.headless=true`
- **THEN** o teste de verificação das flags de runtime falha, apontando a flag ausente
