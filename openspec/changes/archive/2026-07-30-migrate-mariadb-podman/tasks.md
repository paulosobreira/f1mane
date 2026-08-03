## 1. Maven — perfil e dependência

- [x] 1.1 `pom.xml`: substituir dependência `com.mysql:mysql-connector-j` por `org.mariadb.jdbc:mariadb-java-client`
- [x] 1.2 `pom.xml`: renomear perfil `mysql` para `mariadb`, com `jdbc.driver=org.mariadb.jdbc.Driver`, `jdbc.url=jdbc:mariadb://db:3306/flmane...`, `hibernate.dialect=org.hibernate.dialect.MariaDBDialect`
- [x] 1.3 Rodar `mvn clean package -Pmariadb -DskipTests` e confirmar que `target/flmane.jar` é gerado sem erro

## 2. Docker Compose

- [x] 2.1 `docker-compose.yaml`: trocar imagem do serviço `db` de `mysql:8.4` para `mariadb:11`
- [x] 2.2 `docker-compose.yaml`: trocar healthcheck de `mysqladmin ping` para `healthcheck.sh --connect --innodb_initialized`
- [x] 2.3 `docker-compose.yaml`: renomear volume `mysql_data` para `mariadb_data`
- [x] 2.4 `docker-compose.yaml`: atualizar `JDBC_URL` do serviço `flmane` para `jdbc:mariadb://db:3306/flmane?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC`
- [x] 2.5 `docker-compose.yaml`: prefixar imagem do serviço `flmane` com `docker.io/` (evita prompt de registry no Podman)
- [x] 2.6 Confirmar que `phpmyadmin` (`PMA_HOST`/`PMA_PORT`) continua funcional apontando pro serviço `db` MariaDB, sem trocar de imagem

## 3. Build script e Dockerfile

- [x] 3.1 `utilitarios/build.sh`: trocar `-Pmysql` por `-Pmariadb`
- [x] 3.2 `utilitarios/build.sh`: detectar runtime disponível (`docker` preferencial, `podman` como alternativa) e usar o comando compose correspondente (`docker compose` vs `podman compose`)
- [x] 3.3 Validar `flmane.dockerfile` sem alterações necessárias (base `eclipse-temurin` é neutra); só confirmar build funciona via `podman build`

## 4. Validação end-to-end

- [x] 4.1 Subir `docker compose up -d` local, confirmar app conecta no MariaDB e persiste dados (criar corrida/campeonato de teste)
- [x] 4.2 Subir `podman compose up -d` (ou `podman-compose up -d`) local, repetir validação; anotar qualquer ajuste de porta necessário em ambiente rootless
- [x] 4.3 Confirmar healthcheck do serviço `db` fica `healthy` em ambos os runtimes antes do `flmane` subir

## 5. Documentação

- [x] 5.1 `CLAUDE.md`: atualizar tabela de "Perfis Maven" (`mysql` → `mariadb`, descrição do banco)
- [x] 5.2 `CLAUDE.md`: atualizar comandos de build em "Build & Run" (`mvn clean package -Pmysql` → `-Pmariadb`)
- [x] 5.3 Documentar no `CLAUDE.md` (ou README, se existir) a ressalva de porta privilegiada em Podman rootless e o passo manual de dump/restore para quem migra dados de uma instalação MySQL existente
