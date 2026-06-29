## Context

O projeto usa Java 11, Tomcat Embed 9.0.89 (Servlet 4.0 / `javax.*`), Jersey 2.41 e Hibernate 5.6.15 (JPA 2.2 / `javax.persistence.*`). A análise completa do código revelou que a migração para Hibernate 7.4 é o trabalho mais substancial, pois o Hibernate 6+ removeu integralmente a API `Criteria` legada que é usada extensivamente no projeto.

**Inventário completo de arquivos afetados:**

| Arquivo | Motivo | Esforço |
|---|---|---|
| `pom.xml` | Tomcat 11, Jersey 4.x, Hibernate 7.4, remoção de hibernate-entitymanager | Baixo |
| `F1ManeDados.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `CampeonatoSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `CarreiraDadosSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `CorridaCampeonatoSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `CorridasDadosSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `DadosCorridaCampeonatoSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `JogadorDadosSrv.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `HibernateUtil.java` | `javax.persistence.*` → `jakarta.*` | Baixo |
| `LetsRace.java` | `javax.ws.rs.*` → `jakarta.ws.rs.*` | Baixo |
| `Compress.java` | `javax.servlet.*` → `jakarta.servlet.*` | Baixo |
| `GZIPWriterInterceptor.java` | `javax.ws.rs.*` → `jakarta.ws.rs.*` | Baixo |
| `ServletPaddock.java` | `javax.servlet.*` + strings `javax.persistence.jdbc.*` + `MySQL8Dialect` hard-coded | Médio |
| `ControlePersistencia.java` | **Reescrita de 24+ métodos**: Criteria API, `saveOrUpdate()`, `org.hibernate.Query` | **Alto** |
| `ControleClassificacao.java` | 1 `createCriteria()` + 3 `saveOrUpdate()` → `merge()` | Médio |
| `persistence.xml` | Namespace Jakarta, versão 3.0, property keys | Baixo |
| `Dockerfile` | Imagem base JRE 11 → 21 | Baixo |

## Goals / Non-Goals

**Goals:**
- Java 21 LTS como versão do compilador e runtime
- Tomcat 11.x (Jakarta Servlet 6.1)
- Jersey 4.x (`jakarta.ws.rs`) — necessário para Servlet 6.1
- Hibernate 7.4 (`jakarta.persistence` 3.2)
- Todos os imports `javax.*` migrados para `jakarta.*`
- `ControlePersistencia.java` com queries em HQL tipado (sem Criteria legada)
- `session.saveOrUpdate()` substituído por `session.merge()` em todo o código
- `persistence.xml` com namespace Jakarta e property keys atualizadas
- Imagem Docker base em JRE 21

**Non-Goals:**
- Adotar recursos de linguagem Java 21 no código existente (records, virtual threads, etc.)
- Migrar para Jakarta CDI ou outro container de injeção
- Migrar as queries de HQL para JPA Criteria API moderna (opcional, pós-migração)
- Alterar lógica de negócio do jogo

## Decisions

### D1: Java 21 LTS (não Java 17 ou 24)
Java 21 é o LTS mais recente com suporte garantido até 2031. Java 17 já é LTS anterior; Java 24 não é LTS.

### D2: Tomcat 11.x (Jakarta EE 11 / Servlet 6.1)
Tomcat 11 implementa Jakarta EE 11 (Servlet 6.1) e é necessário para rodar Hibernate 7.4, que requer Jakarta Persistence 3.2. Tomcat 10.1 (Jakarta EE 10) não suporta Jakarta Persistence 3.2 e não é compatível com Hibernate 7.x.

### D3: Hibernate 7.4 (não 6.x)
Hibernate 7.x requer Jakarta Persistence 3.2 (Jakarta EE 11), alinhado com Tomcat 11. Hibernate 6.x usa Jakarta Persistence 3.1 (Jakarta EE 10) e não é compatível com o stack completo Jakarta EE 11. Indo direto para 7.4 evita um upgrade intermediário desnecessário. O impacto no código é o mesmo — a API Criteria legada já foi removida no Hibernate 6 e `saveOrUpdate` também, portanto a reescrita de `ControlePersistencia.java` é idêntica independentemente de ir para 6.x ou 7.4.

### D4: Reescrever Criteria API legada para HQL (não para JPA Criteria API moderna)
O Hibernate 6 removeu completamente `session.createCriteria()`, `Restrictions`, `Order` e `org.hibernate.Criteria`. A substituição mais direta e de menor risco é HQL (`session.createQuery("from Entidade where campo = :param", Entidade.class)`), que:
- Preserva a semântica das queries originais
- É familiar para quem já lê o código atual
- Requer menos refatoração estrutural do que migrar para JPA Criteria Builder

Alternativa considerada: JPA Criteria API moderna — descartada para este upgrade por aumentar significativamente o esforço sem benefício funcional imediato.

### D5: `saveOrUpdate()` → `merge()` (não `persist()`)
`session.merge()` tem semântica equivalente ao `saveOrUpdate()` do Hibernate 5 (insere se novo, atualiza se existente). `session.persist()` lança exceção se o objeto já tem ID.

### D6: Trocar `GenerationType.AUTO` por `IDENTITY` nas entidades
O `GenerationType.AUTO` no Hibernate 6 usa SEQUENCE por padrão, comportamento diferente do Hibernate 5 com MySQL (que usava `AUTO_INCREMENT` / IDENTITY). Como não há banco legado a preservar, a estratégia mais simples e explícita é trocar `AUTO` por `IDENTITY` em `F1ManeDados.java`, alinhando o comportamento com o `AUTO_INCREMENT` do MySQL e eliminando qualquer ambiguidade entre ambientes.

### D7: Jersey 4.x (não 3.x)
Jersey 3.x foi construído para Servlet 6.0 (Jakarta EE 10). Tomcat 11 usa Servlet 6.1 (Jakarta EE 11) — Jersey 4.x é a versão correta para este stack. As anotações `jakarta.ws.rs.*` usadas no código (`@GET`, `@POST`, `@Path`, `@Produces`, etc.) são as mesmas entre Jersey 3 e 4: não há mudança de código, apenas de versão dos artefatos no `pom.xml`.

## Risks / Trade-offs

- **[Risco baixo] `GenerationType.AUTO` → trocado por `IDENTITY`** → Sem banco legado, a troca é direta. `IDENTITY` mapeia explicitamente para `AUTO_INCREMENT` no MySQL e para `IDENTITY` no H2 — comportamento previsível em ambos os ambientes.
- **[RISCO ALTO] `ControlePersistencia.java` é a classe central da persistência** — Tem 24+ métodos que precisam ser reescritos. Qualquer erro de migration pode corromper dados ou causar falhas silenciosas. Cada método deve ser testado individualmente após migração.
- **[Risco médio] `SchemaExport` em `ServletPaddock.java`** → A API de bootstrapping do Hibernate 6 manteve `MetadataSources` e `StandardServiceRegistryBuilder`, mas `SchemaExport` pode ter sofrido mudanças menores. A funcionalidade de `createSchema` é administrativa (acesso por senha) — testar separadamente.
- **[Risco médio] Dependências transitivas com `javax.*`** → Verificar `mvn dependency:tree` para garantir que nenhuma lib ainda puxa `javax.servlet-api`, `javax.ws.rs-api` ou `javax.persistence-api`.
- **[Risco médio] `hibernate-entitymanager` como transitiva** → Mesmo removendo do `pom.xml` direto, outra lib pode puxá-lo. Verificar com `mvn dependency:tree | grep hibernate-entitymanager` após upgrade.
- **[Trade-off] JDK 21 obrigatório em todos os ambientes de build** → Desenvolvedores e CI precisam de JDK 21 instalado.
- **[Risco baixo] `MySQL8Dialect` em dois lugares** → Está no `pom.xml` (via profile) e hard-coded em `ServletPaddock.getMetaData()` como string literal. Atualizar os dois.

## Migration Plan

### Fase 1 — pom.xml e namespace (baixo risco, não muda comportamento)
1. Atualizar `maven.compiler.release` para `21`
2. Atualizar Tomcat para 11.x, Jersey para 4.x, Hibernate para 7.4; remover `hibernate-entitymanager`; trocar `jakarta.servlet-api` para 6.1
3. Migrar imports `javax.*` → `jakarta.*` nos 12 arquivos de namespace
4. Atualizar `persistence.xml` (namespace Jakarta, versão `3.2`, property keys, dialeto)
5. Atualizar strings em `ServletPaddock.java`
6. Atualizar Dockerfile
7. Verificar `mvn dependency:tree` para transitivas com `javax.*`
8. Compilar: `mvn clean package -Ph2 -DskipTests` — deve compilar sem erros

### Fase 2 — Reescrita de queries (alto risco, testar com cuidado)
9. Reescrever todos os 24+ métodos de `ControlePersistencia.java`:
   - `session.createCriteria(X).add(Restrictions.eq("campo", val)).list()` → `session.createQuery("from X where campo = :p", X.class).setParameter("p", val).list()`
   - `session.saveOrUpdate(obj)` → `session.merge(obj)`
   - `org.hibernate.Query` → `session.createQuery(hql, Type.class)`
   - `createAlias()` → JPA JOIN em HQL
   - `criteria.uniqueResult()` → `.uniqueResultOptional().orElse(null)` ou `.getSingleResultOrNull()`
10. Reescrever método `obterListaClassificacao()` em `ControleClassificacao.java`
11. Substituir 3 `saveOrUpdate()` em `ControleClassificacao.java` por `merge()`
12. Verificar API de `SchemaExport` em `ServletPaddock.createSchema()`

### Fase 3 — Validação
13. Compilar com testes: `mvn clean package -Ph2`
14. Subir servidor, testar endpoint REST e persistência de dados

**Rollback**: revert no `pom.xml`, nos arquivos Java e no `persistence.xml`.

## Open Questions

- O CI usa JDK 11 explicitamente? Se sim, precisa ser atualizado para JDK 21.
- O `Dockerfile` usa qual imagem base exatamente (`eclipse-temurin`, `openjdk`, outra)?
- A funcionalidade de `createSchema` em `ServletPaddock` é usada ativamente ou é apenas utilitário administrativo?
