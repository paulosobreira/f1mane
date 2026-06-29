## ADDED Requirements

### Requirement: Projeto compilado com Java 21
O build do projeto SHALL usar `maven.compiler.release=21` no `pom.xml`.

#### Scenario: Build bem-sucedido com Java 21
- **WHEN** o desenvolvedor executa `mvn clean package -Ph2 -DskipTests` em ambiente com JDK 21+
- **THEN** o build conclui sem erros de compilação e o JAR `flmane.jar` é gerado

### Requirement: Tomcat embutido atualizado para 11.x
O projeto SHALL usar `tomcat-embed-jasper` versão 11.x (Jakarta EE 11 / Servlet 6.1). Esta versão é necessária para compatibilidade com Hibernate 7.4 (Jakarta Persistence 3.2).

#### Scenario: Servidor web sobe com Tomcat 11
- **WHEN** o JAR é iniciado via `MainLauncher`
- **THEN** o Tomcat 11 sobe na porta 8080, serve o contexto `/flmane` e registra a versão 11 nos logs de inicialização

#### Scenario: Import javax.servlet no código causa falha de compilação
- **WHEN** qualquer import `javax.servlet.*` existe no código-fonte
- **THEN** o build falha em tempo de compilação, não em runtime

### Requirement: Servlet API e JAX-RS migrados para Jakarta EE 11
O projeto SHALL depender de `jakarta.servlet-api 6.1` e Jersey 4.x. Jersey 4.x é necessário para compatibilidade com Servlet 6.1 (Tomcat 11). Todos os imports `javax.servlet.*`, `javax.ws.rs.*` e `javax.inject.*` SHALL ser substituídos pelos equivalentes `jakarta.*`.

#### Scenario: Nenhum import javax.servlet ou javax.ws.rs no código
- **WHEN** é executado `grep -r "javax\.servlet\|javax\.ws\.rs" src/main/java`
- **THEN** nenhum resultado é retornado

#### Scenario: Endpoint REST responde após atualização
- **WHEN** o servidor está rodando e é feita uma requisição ao endpoint `/letsRace/*`
- **THEN** a resposta HTTP é recebida com o status esperado pela lógica de negócio

### Requirement: Hibernate atualizado para 7.4
O projeto SHALL usar `hibernate-core` versão 7.4, que implementa Jakarta Persistence 3.2 (Jakarta EE 11), compatível com Tomcat 11. O artefato `hibernate-entitymanager` SHALL ser removido do `pom.xml`.

#### Scenario: hibernate-entitymanager ausente no pom.xml
- **WHEN** é verificado o `pom.xml`
- **THEN** não existe nenhuma dependência com `artifactId` `hibernate-entitymanager`

#### Scenario: Hibernate inicializa com Jakarta Persistence
- **WHEN** o servidor sobe e a `EntityManagerFactory` é criada por `HibernateUtil`
- **THEN** a factory é criada sem exceção e as entidades são mapeadas corretamente

### Requirement: Imports javax.persistence migrados para jakarta.persistence
Todos os imports `javax.persistence.*` SHALL ser substituídos por `jakarta.persistence.*` nos seguintes arquivos: `F1ManeDados.java`, `CampeonatoSrv.java`, `CarreiraDadosSrv.java`, `CorridaCampeonatoSrv.java`, `CorridasDadosSrv.java`, `DadosCorridaCampeonatoSrv.java`, `JogadorDadosSrv.java` e `HibernateUtil.java`.

#### Scenario: Nenhum import javax.persistence no código
- **WHEN** é executado `grep -r "javax\.persistence" src/main/java`
- **THEN** nenhum resultado é retornado

### Requirement: API Criteria legada substituída por HQL tipado em ControlePersistencia
Todos os métodos de `ControlePersistencia.java` que usam `session.createCriteria()`, `org.hibernate.Criteria`, `org.hibernate.criterion.Restrictions`, `org.hibernate.criterion.Order` e `org.hibernate.Query` (legado) SHALL ser reescritos usando `session.createQuery(hql, Tipo.class)` com parâmetros nomeados. Esses tipos foram removidos no Hibernate 6 e não existem mais.

#### Scenario: Nenhum uso de Criteria API legada
- **WHEN** é executado `grep -r "createCriteria\|org\.hibernate\.Criteria\|criterion\.Restrictions\|criterion\.Order\|import org\.hibernate\.Query" src/main/java`
- **THEN** nenhum resultado é retornado

#### Scenario: Query por campo único retorna resultado correto
- **WHEN** é chamado `controlePersistencia.carregaDadosJogador(idUsuario, session)` com um ID existente
- **THEN** o `JogadorDadosSrv` correspondente é retornado sem exceção

#### Scenario: Query com join retorna resultado correto
- **WHEN** é chamado `controlePersistencia.carregaCarreiraJogador(idUsuario, false, session)` com ID existente
- **THEN** o `CarreiraDadosSrv` correspondente é retornado (inclui join com `jogadorDadosSrv`)

#### Scenario: Query com ordenação retorna resultado correto
- **WHEN** é chamado `controlePersistencia.obterListaCampeonatos(session)`
- **THEN** a lista é retornada ordenada por `dataCriacao` descendente

### Requirement: saveOrUpdate substituído por merge
Todas as chamadas `session.saveOrUpdate()` em `ControlePersistencia.java` e `ControleClassificacao.java` SHALL ser substituídas por `session.merge()`. O método `saveOrUpdate` foi removido no Hibernate 6.

#### Scenario: Nenhum uso de saveOrUpdate
- **WHEN** é executado `grep -r "saveOrUpdate" src/main/java`
- **THEN** nenhum resultado é retornado

#### Scenario: Novo jogador é persistido via merge
- **WHEN** é chamado `controlePersistencia.adicionarJogador()` com um novo jogador
- **THEN** o jogador é salvo no banco e pode ser recuperado na mesma sessão

#### Scenario: Dados existentes são atualizados via merge
- **WHEN** é chamado `controlePersistencia.gravarDados()` com uma entidade de ID existente
- **THEN** a entidade é atualizada no banco sem duplicação

### Requirement: persistence.xml migrado para Jakarta Persistence 3.2
O arquivo `persistence.xml` SHALL usar namespace XML Jakarta (`https://jakarta.ee/xml/ns/persistence`), versão `3.2` e property keys `jakarta.persistence.jdbc.*`.

#### Scenario: persistence.xml com namespace e versão Jakarta Persistence 3.2
- **WHEN** o arquivo `persistence.xml` é inspecionado
- **THEN** `xmlns` aponta para `https://jakarta.ee/xml/ns/persistence`, `version` é `3.2` e nenhuma propriedade usa o prefixo `javax.`

### Requirement: Strings javax.persistence em ServletPaddock atualizadas
As comparações de string hardcoded com `"javax.persistence.jdbc.*"` em `ServletPaddock.java` (linhas ~219–225 no método `getMetaData()`) SHALL ser atualizadas para `"jakarta.persistence.jdbc.*"`.

#### Scenario: Strings de property key atualizadas em ServletPaddock
- **WHEN** é verificado `ServletPaddock.java`
- **THEN** não existe nenhuma string literal `"javax.persistence.jdbc"` no arquivo

### Requirement: Dialeto MySQL e hard-codes atualizados para Hibernate 6
A propriedade `hibernate.dialect` SHALL usar `org.hibernate.dialect.MySQLDialect` em vez de `org.hibernate.dialect.MySQL8Dialect` tanto no `pom.xml` (profile mysql) quanto na string hard-coded em `ServletPaddock.getMetaData()`.

#### Scenario: MySQL8Dialect não referenciado em nenhum lugar
- **WHEN** é executado `grep -r "MySQL8Dialect" .`
- **THEN** nenhum resultado é retornado

#### Scenario: Servidor sobe com perfil MySQL sem warning de dialeto
- **WHEN** o servidor sobe com perfil MySQL
- **THEN** o Hibernate não loga warning de dialeto depreciado

### Requirement: GenerationType.AUTO substituído por IDENTITY
O `GenerationType.AUTO` em `F1ManeDados.java` SHALL ser substituído por `GenerationType.IDENTITY`, mapeando explicitamente para `AUTO_INCREMENT` no MySQL e `IDENTITY` no H2, eliminando a ambiguidade de comportamento entre Hibernate 5 e 6.

#### Scenario: Geração de ID funciona em H2
- **WHEN** uma nova entidade é persistida no banco H2 de desenvolvimento
- **THEN** o ID é gerado automaticamente sem exceção de sequence ou constraint

#### Scenario: Geração de ID funciona em MySQL
- **WHEN** uma nova entidade é persistida no banco MySQL
- **THEN** o ID é gerado via AUTO_INCREMENT sem criação de sequence desnecessária

### Requirement: Imagem Docker base atualizada para JRE 21
O `Dockerfile` SHALL usar uma imagem JRE/JDK 21.

#### Scenario: Container sobe com JRE 21
- **WHEN** o Docker Compose é iniciado
- **THEN** o container `flmane` sobe sem erro de versão de JVM e serve a aplicação normalmente
