## 1. Separar compose base e override

- [x] 1.1 Criar `docker-compose.override.yaml` com os serviços `phpmyadmin` e `sonarqube` (copiados de `docker-compose.yaml`) e o volume `sonarqube_data`
- [x] 1.2 Remover `phpmyadmin`, `sonarqube` e o volume `sonarqube_data` de `docker-compose.yaml`, mantendo `flmane`, `db`, `mariadb_data` e a seção `networks`

## 2. Validação

- [x] 2.1 `docker compose config` (ou `podman compose config`) sem `-f`: confirmar que mostra os 4 serviços mesclados (base + override)
- [x] 2.2 `docker compose -f docker-compose.yaml config`: confirmar que mostra só `flmane`+`db`
- [x] 2.3 Subir em modo dev (comando padrão / `utilitarios/build_container.sh`) e confirmar `phpmyadmin` (porta 8080) e `sonarqube` (porta 9000) funcionando como antes
- [x] 2.4 Subir em modo produção (`-f docker-compose.yaml` explícito) e confirmar que só `flmane`+`db` sobem, sem `phpmyadmin`/`sonarqube`

## 3. Documentação

- [x] 3.1 `CLAUDE.md`: documentar os dois comandos de subida (dev = padrão, produção = `-f docker-compose.yaml` explícito) e a existência do `docker-compose.override.yaml`
- [x] 3.2 `README.md`/`README.pt-BR.md`: mesma documentação, nas seções de Docker Compose
