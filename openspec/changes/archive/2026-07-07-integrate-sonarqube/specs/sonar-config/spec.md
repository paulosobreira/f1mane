## ADDED Requirements

### Requirement: Maven plugin configurado
O `pom.xml` DEVE conter o `sonar-maven-plugin` na seção `<build><plugins>`.

#### Scenario: Plugin presente no build
- **WHEN** executado `mvn -Psonar sonar:sonar`
- **THEN** o mojo `sonar:sonar` é resolvido sem erro de plugin desconhecido

### Requirement: Profile Maven sonar
O `pom.xml` DEVE conter um profile `sonar` que ative a análise estática sem interferir nos profiles existentes (`h2`, `mysql`, `test`, `prod`).

#### Scenario: Profile ativo não quebra build padrão
- **WHEN** executado `mvn clean package -Ph2` (profile default)
- **THEN** o build completa sem executar goal `sonar:sonar`

#### Scenario: Profile sonar ativo
- **WHEN** executado `mvn -Psonar sonar:sonar -Dsonar.host.url=http://localhost:9000`
- **THEN** a análise é enviada ao SonarQube

### Requirement: sonar-project.properties na raiz
O arquivo `sonar-project.properties` DEVE existir na raiz do projeto com as seguintes chaves:
- `sonar.projectKey`
- `sonar.projectName`
- `sonar.sources`
- `sonar.tests`
- `sonar.java.binaries`
- `sonar.java.test.binaries`
- `sonar.junit.reportPaths`
- `sonar.coverage.jacoco.xmlReportPaths`
- `sonar.exclusions`
- `sonar.coverage.exclusions`

#### Scenario: Propriedades carregadas pelo plugin
- **WHEN** executado `mvn -Psonar sonar:sonar`
- **THEN** o plugin lê `sonar-project.properties` e aplica as configurações

### Requirement: Exclusões configuradas
Os patterns de exclusão DEVEM ignorar:
- `**/webapp/**` — assets frontend
- `**/sprites/**` — imagens de sprite
- `**/META-INF/**` — metadados de build
- `**/module-info.class` — classes geradas

#### Scenario: Webapp excluído da análise
- **WHEN** executada análise SonarQube
- **THEN** arquivos em `src/main/webapp/` NÃO aparecem no relatório de código

### Requirement: Relatório JaCoCo integrado
O SonarQube DEVE consumir o relatório XML do JaCoCo gerado na fase `test`.

#### Scenario: Cobertura aparece no SonarQube
- **WHEN** executado `mvn -Psonar test sonar:sonar`
- **THEN** o SonarQube exibe percentual de cobertura compatível com o relatório JaCoCo

### Requirement: Encoding UTF-8
O `sonar-project.properties` DEVE definir `sonar.sourceEncoding=UTF-8`.

#### Scenario: Encoding correto
- **WHEN** executada análise
- **THEN** o SonarQube reporta `sourceEncoding` como `UTF-8`
