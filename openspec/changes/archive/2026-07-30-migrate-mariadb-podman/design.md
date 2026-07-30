## Context

O jogo roda em três perfis Maven (`h2`, `mysql`, `test`). O perfil `mysql` gera o jar de produção/Docker, com valores JDBC injetados via filtro de build em `META-INF/persistence.xml`. `docker-compose.yaml` sobe três serviços dependentes de MySQL: `flmane` (app), `db` (MySQL 8.4) e `phpmyadmin`. `utilitarios/build.sh` chama `docker compose`/`docker build` diretamente. O usuário quer trocar o banco de Docker/produção para MariaDB e garantir que o mesmo compose funcione com Podman (`podman compose` ou `podman-compose`), sem quebrar o fluxo Docker existente.

## Goals / Non-Goals

**Goals:**
- Perfil Maven `mariadb` substitui `mysql`: driver `org.mariadb.jdbc.Driver`, dependência `mariadb-java-client`, dialect `MariaDBDialect`
- `docker-compose.yaml` usa imagem `mariadb` oficial, healthcheck nativo MariaDB, nomes de volume/serviço coerentes
- Compose funciona tanto com `docker compose up -d` quanto com `podman compose up -d` (ou `podman-compose`) sem edição manual
- `utilitarios/build.sh` funciona com Docker ou Podman disponível no PATH
- `CLAUDE.md` reflete a nova stack

**Non-Goals:**
- Migração de dados de instalações já em produção com MySQL (dump/restore é operação manual do usuário, documentada mas não automatizada)
- Suporte a Podman em modo rootful específico ou geração de Kubernetes YAML (`podman generate kube`)
- Mudança do perfil `h2` (desenvolvimento local não é afetado)
- CI/CD (fora do escopo; `CLAUDE.md` não descreve pipeline de CI para este repo)

## Decisions

### Driver JDBC: `mariadb-java-client` em vez de manter `mysql-connector-j`
MariaDB Server aceita o driver MySQL na maioria dos casos, mas o driver oficial MariaDB (`org.mariadb.jdbc.Driver`) é o suportado e testado contra o servidor MariaDB, evita comportamento não documentado do protocolo, e mantém o dialect Hibernate correto (`org.hibernate.dialect.MariaDBDialect`, disponível no Hibernate que o projeto já usa). Alternativa considerada: manter `mysql-connector-j` apontando pra MariaDB (funciona por compatibilidade de protocolo) — rejeitada por acoplar o projeto a um driver não oficial para o banco alvo.

### Nome do perfil Maven: `mariadb` (não reaproveitar `mysql`)
Renomear deixa explícito no `pom.xml`, no `CLAUDE.md` e nos comandos (`mvn clean package -Pmariadb`) qual banco está em uso, evitando confusão com o dialect/driver antigo. **BREAKING**: quem usava `-Pmysql` precisa trocar para `-Pmariadb`.

### Imagem `mariadb:11` (LTS) em vez de `mariadb:latest`
Fixar major version evita quebra silenciosa em rebuild. `11` é a série LTS atual do MariaDB no momento deste change; ajustar quando uma LTS mais nova for adotada deliberadamente.

### Healthcheck: `healthcheck.sh --connect --innodb_initialized` (script nativo da imagem MariaDB) em vez de `mysqladmin ping`
A imagem oficial `mariadb` inclui `healthcheck.sh` desenhado especificamente para uso em `HEALTHCHECK`/compose; `mysqladmin` também existe na imagem MariaDB (compatibilidade), mas o script nativo é o caminho documentado e recomendado pela imagem oficial.

### Compatibilidade Podman: evitar recursos exclusivos do Docker Engine no compose
- Não usar `container_name` como dependência forte de rede única (Podman rootless lida diferente com DNS entre containers em certas versões, mas `podman compose`/`podman-compose` com rede compartilhada resolve nomes de serviço normalmente — mantém-se `container_name` apenas onde já existia, sem expandir uso)
- Porta `80:8080` do serviço `flmane`: em Podman rootless, bind de porta <1024 falha sem `CAP_NET_BIND_SERVICE` ou `net.ipv4.ip_unprivileged_port_start` ajustado. Documentar no `CLAUDE.md`/README que, em Podman rootless, publicar em porta ≥1024 (ex. `8080:8080`) ou ajustar o sysctl; não forçar uma porta específica no compose já que Docker rootful não tem essa restrição
- Nome de imagem `sowbreira/flmane:latest` sem registry explícito: Podman por padrão pode pedir para escolher o registry (`docker.io`) interativamente, quebrando builds não-interativos. Adicionar prefixo explícito `docker.io/sowbreira/flmane:latest` na tag de build e no compose para evitar prompt
- `utilitarios/build.sh`: detectar `docker` ou `podman` no PATH (preferindo `docker` se ambos existirem, para não mudar comportamento de quem já usa Docker) e usar o comando compose correspondente (`docker compose` vs `podman compose`)

## Risks / Trade-offs

- [Risco] Instalações existentes com dados em volume `mysql_data`/MySQL não migram automaticamente ao trocar para `mariadb_data` → Mitigação: documentar passo manual de dump (`mysqldump`) + restore no `mariadb` novo, no README/CLAUDE.md; volume antigo não é removido automaticamente
- [Risco] `podman compose` (plugin) vs `podman-compose` (script Python) têm suporte variável a `healthcheck`/`depends_on: condition: service_healthy` dependendo da versão instalada → Mitigação: usar sintaxe compose padrão (compatível com Compose Spec), documentar versão mínima recomendada de Podman no CLAUDE.md
- [Risco] `phpmyadmin` é nomeado para MySQL mas funciona com MariaDB via protocolo compatível → Mitigação: manter como está, sem trocar de imagem (funciona igual); apenas confirmar que `PMA_HOST`/`PMA_PORT` continuam válidos
- [Trade-off] Fixar `mariadb:11` em vez de `latest` exige bump manual futuro → aceito, é o mesmo padrão já usado para `mysql:8.4`

## Migration Plan

1. `pom.xml`: adicionar dependência `mariadb-java-client`, remover `mysql-connector-j`; renomear perfil `mysql` para `mariadb` com novo driver/URL/dialect
2. `docker-compose.yaml`: trocar imagem/healthcheck/volume do serviço `db`; atualizar `JDBC_URL` do serviço `flmane` para `jdbc:mariadb://...`
3. `utilitarios/build.sh`: trocar `-Pmysql` por `-Pmariadb`; adicionar detecção docker/podman
4. `flmane.dockerfile`: validar que build/run funcionam sem alteração (imagem base `eclipse-temurin` é neutra quanto a Docker/Podman)
5. `CLAUDE.md`: atualizar tabela de perfis Maven e comandos de build
6. Rodar `mvn clean package -Pmariadb -DskipTests`, subir compose local com `podman compose up -d` e com `docker compose up -d`, validar que app conecta e persiste dados em ambos
7. Usuários com dados em MySQL: dump manual (`mysqldump`) antes de trocar de volume, restore no MariaDB novo (documentado, não automatizado)

Rollback: reverter para perfil `mysql`/imagem `mysql:8.4` via git revert do commit; volume `mysql_data` antigo, se não removido, permanece intacto.

## Open Questions

- Confirmar se há instalação de produção real do usuário rodando hoje com dados em MySQL que precise de migração assistida agora, ou se é ambiente novo/dev — impacta se o passo de dump/restore precisa ser executado nesta sessão ou só documentado
