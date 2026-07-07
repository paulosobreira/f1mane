## 1. Configuração Maven

- [x] 1.1 Adicionar `sonar-maven-plugin` (4.0.0.4121) ao `<build><plugins>` no `pom.xml`
- [x] 1.2 Criar profile `sonar` no `pom.xml` com `<id>sonar</id>` e `<activation><activeByDefault>false</activeByDefault></activation>`
- [x] 1.3 Verificar que `mvn -Psonar sonar:sonar -Dsonar.host.url=http://localhost:9000` resolve o plugin

## 2. sonar-project.properties

- [x] 2.1 Criar `sonar-project.properties` na raiz com `sonar.projectKey=br.nnpe:flmane`
- [x] 2.2 Configurar `sonar.projectName=FlMane`, `sonar.projectVersion` dinâmico
- [x] 2.3 Mapear `sonar.sources=src/main/java`, `sonar.tests=src/test/java`
- [x] 2.4 Configurar `sonar.java.binaries=target/classes`, `sonar.java.test.binaries=target/test-classes`
- [x] 2.5 Apontar `sonar.junit.reportPaths=target/surefire-reports` e `sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml`
- [x] 2.6 Adicionar exclusões: `sonar.exclusions` e `sonar.coverage.exclusions` para `webapp/`, `sprites/`, `META-INF/`, `module-info.class`
- [x] 2.7 Definir `sonar.sourceEncoding=UTF-8`

## 3. Docker Compose

- [x] 3.1 Adicionar service `sonarqube` no `docker-compose.yaml` com imagem `sonarqube:community`
- [x] 3.2 Mapear porta `9000:9000` e adicionar variáveis de ambiente `SONAR_JDBC_*` (opcional, default H2)
- [x] 3.3 Adicionar volume nomeado `sonarqube_data` para persistência
- [x] 3.4 Configurar `healthcheck` com `test: ["CMD-SH", "curl -sf http://localhost:9000/api/system/health"]`
- [x] 3.5 Adicionar `depends_on` com condição `service_healthy` se referenciado por outros services

## 4. Script de análise

- [x] 4.1 Criar `utilitarios/analise-sonar.sh` executável
- [x] 4.2 Script deve: iniciar SonarQube (`docker compose up -d sonarqube`), aguardar healthcheck, executar `mvn -Psonar clean test sonar:sonar`, exibir URL do relatório
- [x] 4.3 Adicionar `analise-sonar.sh` ao `.gitignore` se necessário (não obrigatório)

## 5. Verificação

- [x] 5.1 Executar `mvn clean test` (profile h2) — build deve passar sem executar SonarQube
- [x] 5.2 Executar `docker compose up -d sonarqube` e confirmar healthcheck
- [x] 5.3 Executar `./analise-sonar.sh` — análise completa, relatório visível em `http://localhost:9000`
- [x] 5.4 Verificar que webapp/ e sprites/ não aparecem no relatório SonarQube
- [x] 5.5 Verificar que cobertura JaCoCo está visível no dashboard do SonarQube
