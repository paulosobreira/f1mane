## 1. Atualizar pom.xml

- [x] 1.1 Alterar `maven.compiler.release` de `11` para `21`
- [x] 1.2 Atualizar `tomcat-embed-jasper` para a versão 11.x mais recente estável
- [x] 1.3 Remover `javax.servlet-api`; adicionar `jakarta.servlet-api:6.1`
- [x] 1.4 Atualizar `jersey-server`, `jersey-container-servlet`, `jersey-hk2` e `jersey-media-json-jackson` para versão 4.0.2 (artefato renomeado de `jersey-container-servlet-core` → `jersey-container-servlet`)
- [x] 1.5 Atualizar `hibernate-core` de `5.6.15.Final` para `7.4.0.Final` com novo groupId `org.hibernate.orm`
- [x] 1.6 Remover a dependência `hibernate-entitymanager` do `pom.xml`
- [x] 1.7 Atualizar dialeto MySQL no profile `mysql`: `MySQL8Dialect` → `MySQLDialect`
- [x] 1.8 Verificar que não restam transitivas javax legados (build passou com -U)

## 2. Migrar imports javax.servlet e javax.ws.rs (servlet / JAX-RS)

- [x] 2.1 Migrar imports em `LetsRace.java`: `javax.ws.rs.*` → `jakarta.ws.rs.*`
- [x] 2.2 Migrar imports em `ServletPaddock.java`: `javax.servlet.*` → `jakarta.servlet.*`
- [x] 2.3 Atualizar strings hardcoded em `ServletPaddock.java` (método `getMetaData()`, ~linhas 219–225): `"javax.persistence.jdbc.*"` → `"jakarta.persistence.jdbc.*"`
- [x] 2.4 Atualizar string hardcoded `"org.hibernate.dialect.MySQL8Dialect"` em `ServletPaddock.getMetaData()` para `"org.hibernate.dialect.MySQLDialect"`
- [x] 2.5 Migrar imports em `Compress.java`: `javax.servlet.*` → `jakarta.servlet.*`
- [x] 2.6 Migrar imports em `GZIPWriterInterceptor.java`: `javax.ws.rs.*` → `jakarta.ws.rs.*`

## 3. Migrar imports javax.persistence (entidades JPA e HibernateUtil)

- [x] 3.1 Migrar imports em `HibernateUtil.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.2 Migrar imports em `F1ManeDados.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.3 Migrar imports em `CampeonatoSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.4 Migrar imports em `CarreiraDadosSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.5 Migrar imports em `CorridaCampeonatoSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.6 Migrar imports em `CorridasDadosSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.7 Migrar imports em `DadosCorridaCampeonatoSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.8 Migrar imports em `JogadorDadosSrv.java`: `javax.persistence.*` → `jakarta.persistence.*`
- [x] 3.9 Verificado com `grep -r "javax\." src/main/java` — nenhum resultado (Jakarta EE)

## 4. Atualizar persistence.xml

- [x] 4.1 Trocar `xmlns="http://xmlns.jcp.org/xml/ns/persistence"` por `xmlns="https://jakarta.ee/xml/ns/persistence"`
- [x] 4.2 Atualizar `version="2.2"` para `version="3.2"`
- [x] 4.3 Renomear property keys `javax.persistence.jdbc.*` para `jakarta.persistence.jdbc.*` nos dois perfis (h2 e mysql)
- [x] 4.4 Trocar `GenerationType.AUTO` por `GenerationType.IDENTITY` em `F1ManeDados.java`

## 5. Reescrever ControlePersistencia.java — queries Criteria → HQL

- [x] 5.1 Reescrever `carregaDadosJogador()` — `createCriteria` + `Restrictions.eq("idUsuario")` → HQL
- [x] 5.2 Reescrever `carregaDadosJogadorNome()` — `createCriteria` + `Restrictions.eq("nome")` → HQL
- [x] 5.3 Reescrever `carregaDadosJogadorIdUsuario()` — `createCriteria` + `Restrictions.eq("idUsuario")` → HQL
- [x] 5.4 Reescrever `carregaDadosJogadorId()` — `createCriteria` + `Restrictions.eq("id")` → HQL
- [x] 5.5 Reescrever `carregaDadosJogadorEmail()` — `createCriteria` + `Restrictions.eq("email")` → HQL
- [x] 5.6 Reescrever `obterListaJogadores()` — `createCriteria(JogadorDadosSrv.class)` sem filtro → HQL `"from JogadorDadosSrv"`
- [x] 5.7 Reescrever `obterListaJogadoresCorridasPeriodo()` — `createCriteria` + `Restrictions.ge/le` → HQL com >= e <=
- [x] 5.8 Reescrever `obterListaCorridas()` — `createCriteria` + `createAlias("jogadorDadosSrv","j")` + `Order.asc` → HQL com JOIN e ORDER BY
- [x] 5.9 Reescrever `obterClassificacaoCircuito()` — `createCriteria` + `Restrictions.eq/gt` → HQL
- [x] 5.10 Reescrever `obterClassificacaoTemporada()` — `createCriteria` + `Restrictions.eq/gt` → HQL
- [x] 5.11 Reescrever `carregaCarreiraJogador()` — `createCriteria` + `createAlias` + `Restrictions.eq("j.idUsuario")` → HQL com JOIN
- [x] 5.12 Reescrever `obterListaCampeonatos()` — `createCriteria` + `Order.desc("dataCriacao")` → HQL com ORDER BY
- [x] 5.13 Reescrever `pesquisaCampeonato()` — `createCriteria` + `Restrictions.eq("id")` → HQL
- [x] 5.14 Reescrever `pesquisaCampeonatos(idUsuario)` — `createCriteria` + `createAlias` + eq → HQL com JOIN
- [x] 5.15 Reescrever `pesquisaCampeonatosEmAberto()` — `createCriteria` + `createAlias` + `eq(Boolean.FALSE)` → HQL com JOIN e WHERE finalizado = false
- [x] 5.16 Reescrever `pesquisaCampeonatoId()` — `createCriteria` + `uniqueResult()` → HQL com `.uniqueResultOptional().orElse(null)`
- [x] 5.17 Reescrever `pesquisaCampeonatos(jogadorDadosSrv)` — `createCriteria` + `Restrictions.eq(objeto)` → HQL
- [x] 5.18 Reescrever `existeNomeCarro()` — `createCriteria` + `createAlias` + `Restrictions.ne` → HQL com JOIN e `<>`
- [x] 5.19 Reescrever `existeNomePiloto()` — `createCriteria` + `createAlias` + `Restrictions.ne` → HQL
- [x] 5.20 Reescrever `existeNomeCampeonato()` — `createCriteria` + `Restrictions.eq("nome")` → HQL
- [x] 5.21 Reescrever `obterClassificacaoGeral()` — `createCriteria` + `Restrictions.gt("pontos")` → HQL
- [x] 5.22 Reescrever `obterClassificacaoEquipes()` — `createCriteria` + `isNotNull` + `gt` + `Order.desc` → HQL com WHERE e ORDER BY
- [x] 5.23 Reescrever `obterClassificacaoCampeonato()` — `org.hibernate.Query` legado → `session.createQuery(hql, CampeonatoSrv.class)` tipado
- [x] 5.24 Substituir `session.saveOrUpdate()` por `session.merge()` em `adicionarJogador()` e `gravarDados()`
- [x] 5.25 Remover imports removidos: `org.hibernate.Criteria`, `org.hibernate.Query`, `org.hibernate.criterion.Restrictions`, `org.hibernate.criterion.Order`

## 6. Reescrever ControleClassificacao.java — Criteria e saveOrUpdate

- [x] 6.1 Reescrever `obterListaClassificacao()`: `session.createCriteria(CorridasDadosSrv.class)` + Restrictions → HQL
- [x] 6.2 Substituir 3 chamadas `session.saveOrUpdate()` em `processaCorrida()` por `session.merge()`
- [x] 6.3 Remover imports `org.hibernate.criterion.Restrictions` do arquivo

## 7. Verificar ServletPaddock.createSchema (SchemaExport)

- [x] 7.1 `SchemaExport` removido do `hibernate-core` no Hibernate 6+. Substituído por `Persistence.generateSchema("flmane-jpa", props)` (JPA padrão). Métodos auxiliares `getMetaData()` e inner class `MyConnectionProvider` removidos.

## 8. Atualizar Dockerfile

- [x] 8.1 Dockerfile já usa `eclipse-temurin:21-jre-alpine` — nenhuma alteração necessária
- [x] 8.2 Imagem base já está em JRE 21

## 9. Validar build e execução

- [x] 9.1 `mvn clean package -Ph2 -DskipTests` — BUILD SUCCESS
- [x] 9.2 `grep -r "javax\." src/main/java` — nenhuma ocorrência Jakarta EE restante
- [x] 9.3 `grep -r "createCriteria\|saveOrUpdate\|org\.hibernate\.Query" src/main/java` — nenhum resultado
- [x] 9.4 Subir o servidor com `java -jar target/flmane.jar` e confirmar que Tomcat 11 e Hibernate 7 inicializam sem erros
- [x] 9.5 Testar um fluxo que persiste dados (ex: criação de jogador) e confirmar que `merge()` salva corretamente
- [x] 9.6 Testar uma query com join (ex: busca de carreira por idUsuario) e confirmar retorno correto
- [ ] 9.7 Executar `./simulacao.sh` e confirmar que simulação headless conclui sem exceções
- [ ] 9.8 Testar acesso à página web em `http://localhost:8080/flmane/html5/index.html`
- [ ] 9.9 Subir via Docker Compose (`docker compose up --build`) e confirmar container com JRE 21
- [ ] 9.10 Confirmar que IDs são gerados corretamente com `IDENTITY` tanto em H2 quanto em MySQL
