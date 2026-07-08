## ADDED Requirements

### Requirement: Service SonarQube no Docker Compose
O `docker-compose.yaml` DEVE conter um service `sonarqube` usando a imagem `sonarqube:community` (LTS).

#### Scenario: Container sobe sem erro
- **WHEN** executado `docker compose up -d sonarqube`
- **THEN** o container inicia e a API responde em `http://localhost:9000/api/system/health`

### Requirement: Porta mapeada
O service DEVE mapear a porta `9000` do container para `9000` no host.

#### Scenario: Acesso via navegador
- **WHEN** o container está rodando
- **THEN** `http://localhost:9000` exibe a tela de login do SonarQube

### Requirement: Healthcheck configurado
O service DEVE ter `healthcheck` para aguardar a inicialização completa do SonarQube.

#### Scenario: Docker aguarda health
- **WHEN** executado `docker compose up -d sonarqube`
- **THEN** o status do container só fica `healthy` após o SonarQube estar pronto

### Requirement: Volume para dados
O service DEVE usar um volume nomeado `sonarqube_data` para persistir configurações e resultados de análise entre restart do container.

#### Scenario: Dados persistem após restart
- **WHEN** o container é restartado (`docker compose restart sonarqube`)
- **THEN** as análises anteriores e configurações permanecem disponíveis

### Requirement: Script analise-sonar.sh
Um script `analise-sonar.sh` DEVE ser criado na raiz do projeto para iniciar o SonarQube, aguardar health, executar `mvn test` e depois `mvn sonar:sonar`.

#### Scenario: Análise completa em um comando
- **WHEN** executado `./analise-sonar.sh`
- **THEN** o SonarQube inicia, a análise Maven executa e o relatório fica disponível em `http://localhost:9000`
