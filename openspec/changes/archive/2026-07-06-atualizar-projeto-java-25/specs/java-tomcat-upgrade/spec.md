## MODIFIED Requirements

### Requirement: Projeto compilado com Java 25
O build do projeto SHALL usar `maven.compiler.release=25` no `pom.xml`.

#### Scenario: Build bem-sucedido com Java 25
- **WHEN** o desenvolvedor executa `mvn clean package -Ph2 -DskipTests` em ambiente com JDK 25+
- **THEN** o build conclui sem erros de compilação e o JAR `flmane.jar` é gerado

#### Scenario: Suíte de testes passa sob JDK 25
- **WHEN** o desenvolvedor executa `mvn test` em ambiente com JDK 25
- **THEN** todos os testes concluem sem falhas ou erros causados pela versão da JVM (incluindo a cadeia Mockito/byte-buddy usada nos mocks)

### Requirement: Imagem Docker base atualizada para JRE 25
O `flmane.dockerfile` SHALL usar uma imagem JRE/JDK 25 (`eclipse-temurin:25-jre-alpine`, ou a variante Debian equivalente caso a tag Alpine ainda não esteja disponível no momento da implementação).

#### Scenario: Container sobe com JRE 25
- **WHEN** o Docker Compose é iniciado com a imagem construída a partir de `flmane.dockerfile`
- **THEN** o container `flmane` sobe sem erro de versão de JVM e serve a aplicação normalmente
