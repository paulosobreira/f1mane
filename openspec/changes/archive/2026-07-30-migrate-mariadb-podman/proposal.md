## Why

O perfil `mysql` do Maven e o `docker-compose.yaml` usam MySQL 8.4 e o driver `com.mysql.cj.jdbc.Driver`. MySQL Community tem licenciamento mais restritivo e o build/`docker-compose.yaml` atual assume o Docker Engine (`docker compose`, `docker build`); rodando via Podman surgem atritos: bind de porta privilegiada (`80:8080`) em modo rootless, nome de imagem sem registry totalmente qualificado, healthcheck com binário `mysqladmin` que não existe na imagem MariaDB, e o script `utilitarios/build.sh` chamando `docker` diretamente. Este change migra o banco de produção/Docker de MySQL para MariaDB (drop-in compatível, licença mais permissiva) e ajusta compose/build/Dockerfile para funcionar tanto em Docker quanto em Podman.

## What Changes

- **BREAKING**: perfil Maven `mysql` renomeado/substituído por perfil `mariadb`, trocando driver JDBC (`com.mysql.cj.jdbc.Driver` → `org.mariadb.jdbc.Driver`), dependência (`mysql-connector-j` → `mariadb-java-client`) e dialect Hibernate (`MySQLDialect` → `MariaDBDialect`)
- `docker-compose.yaml`: serviço `db` migrado de `mysql:8.4` para `mariadb:11` (ou LTS mais recente), healthcheck trocado de `mysqladmin ping` para `healthcheck.sh --connect` (ferramenta nativa MariaDB), volume renomeado `mysql_data` → `mariadb_data`
- `docker-compose.yaml` e `flmane.dockerfile` revisados para portabilidade Podman: variáveis de ambiente sem depender de recursos exclusivos do Docker Engine, porta de publicação documentada para uso com `podman-compose`/`podman compose` rootless
- `utilitarios/build.sh` (ou script equivalente) ajustado para detectar/aceitar `podman`/`podman compose` como alternativa a `docker`/`docker compose`
- `JDBC_URL` de exemplo em `docker-compose.yaml` migrado de `jdbc:mysql://...` para `jdbc:mariadb://...`
- Documentação em `CLAUDE.md` (seção Build & Run, tabela de perfis Maven) atualizada para refletir `mariadb` no lugar de `mysql`

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `sdd-persistence`: o perfil Maven documentado para Docker/produção passa de `mysql` para `mariadb`, com novo driver, URL JDBC e dialect Hibernate

## Impact

- `pom.xml`: dependência `mysql-connector-j` substituída por `mariadb-java-client`; perfil `mysql` substituído por `mariadb`
- `docker-compose.yaml`: serviço `db` (imagem, healthcheck, volume) e variáveis `JDBC_URL`/`JDBC_USER`/`JDBC_PASSWORD` do serviço `flmane`; serviço `phpmyadmin` avaliado quanto à compatibilidade com MariaDB (mantém-se, é compatível com o protocolo MySQL)
- `flmane.dockerfile`: revisão para compatibilidade Podman (sem mudanças estruturais esperadas, apenas validação)
- `utilitarios/build.sh`: troca de comandos `docker`/`docker compose` por detecção de runtime (docker ou podman)
- `CLAUDE.md`: seção "Perfis Maven" e "Build & Run" atualizadas
- Dados existentes em volumes MySQL de instalações já em produção precisam de migração/dump-restore manual (fora do escopo deste change de código; mencionar no design.md)
