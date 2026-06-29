## Why

O projeto usa Java 11, Tomcat 9 (Servlet 4.0 / `javax.*`) e Hibernate 5.6 (JPA 2.2 / `javax.persistence.*`), todos fora do ciclo de suporte ativo. A migração para **Java 21 LTS + Tomcat 11 (Jakarta EE 11) + Hibernate 7.4** atualiza o projeto para o stack mais recente e suportado, trazendo ganhos de performance, segurança e acesso a APIs modernas da linguagem.

A análise do código revelou que a migração do Hibernate é o trabalho mais substancial: a partir do Hibernate 6, a API `Criteria` legada foi completamente removida — ela é usada em 24+ métodos de `ControlePersistencia.java` — e `saveOrUpdate()` também foi removido. Toda a camada de queries precisa ser reescrita.

## What Changes

- Compilador: Java 11 → **Java 21 LTS**
- `tomcat-embed-jasper`: 9.0.x → **11.x** (Jakarta EE 11 / Servlet 6.1)
- `javax.servlet-api 4.0.1` → **`jakarta.servlet-api 6.1`**
- Jersey: 2.41 (`javax.ws.rs`) → **4.x** (`jakarta.ws.rs`) — necessário para Servlet 6.1
- `hibernate-core`: 5.6.15 → **7.4** (Jakarta Persistence 3.2)
- `hibernate-entitymanager` **removido** — fundido em `hibernate-core` a partir do Hibernate 6
- Todos os imports `javax.servlet.*`, `javax.ws.rs.*`, `javax.inject.*` e `javax.persistence.*` migrados para `jakarta.*`
- `persistence.xml`: namespace, versão (`3.2`) e property keys atualizados para Jakarta Persistence 3.2
- `ControlePersistencia.java`: 24+ métodos reescritos de Criteria API legada para HQL tipado
- `session.saveOrUpdate()` → `session.merge()` em todas as ocorrências
- `org.hibernate.Query` (legado) → `session.createQuery(hql, Tipo.class)` tipado
- `MySQL8Dialect` → `MySQLDialect` (renomeado no Hibernate 6+)
- `GenerationType.AUTO` → `GenerationType.IDENTITY` em `F1ManeDados.java`
- Imagem Docker base: JRE 11 → **JRE 21**

## Capabilities

### New Capabilities

- `java-tomcat-upgrade`: Roteiro de migração completo para Java 21 + Tomcat 11 + Hibernate 7.4

### Modified Capabilities

- `sdd-execution-modes`: Modo servidor web migra de Tomcat 9 (`javax.*`) para Tomcat 11 (`jakarta.*` / Servlet 6.1); comportamento externo não muda
- `sdd-persistence`: Hibernate 5 → 7.4 com migração de namespace JPA, remoção da API Criteria legada e reescrita da camada de queries

## Impact

**`pom.xml`** (1 arquivo):
- Tomcat 11.x, Jersey 4.x, `jakarta.servlet-api 6.1`, `hibernate-core 7.4`; remoção de `hibernate-entitymanager`; `maven.compiler.release=21`

**Código Java — troca de namespace `javax.*` → `jakarta.*`** (12 arquivos, baixo esforço):
- 7 entidades JPA: `F1ManeDados`, `CampeonatoSrv`, `CarreiraDadosSrv`, `CorridaCampeonatoSrv`, `CorridasDadosSrv`, `DadosCorridaCampeonatoSrv`, `JogadorDadosSrv`
- `HibernateUtil.java`
- `LetsRace.java`, `Compress.java`, `GZIPWriterInterceptor.java`
- `ServletPaddock.java` (imports + strings hardcoded `"javax.persistence.jdbc.*"` + dialeto MySQL)

**Código Java — reescrita de queries Hibernate** (2 arquivos, alto esforço):
- `ControlePersistencia.java`: 24+ métodos com `createCriteria()`, `Restrictions`, `Order`, `saveOrUpdate()`, `org.hibernate.Query` — reescritos para HQL tipado e `merge()`
- `ControleClassificacao.java`: 1 `createCriteria()` e 3 `saveOrUpdate()` → `merge()`

**Config** (1 arquivo):
- `persistence.xml`: namespace Jakarta, versão `3.2`, property keys `jakarta.persistence.jdbc.*`, dialeto `MySQLDialect`

**`F1ManeDados.java`**: `GenerationType.AUTO` → `IDENTITY`

**Docker**: imagem base JRE 11 → JRE 21
