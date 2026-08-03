## Why

`docker-compose.yaml` hoje sobe sempre os quatro serviços juntos — `flmane`, `db`, `phpmyadmin` e `sonarqube` — sem distinção entre ambiente de desenvolvimento e produção. `phpmyadmin` (acesso direto ao banco) e `sonarqube` (análise estática) são ferramentas de apoio ao desenvolvimento; não fazem sentido — e representam superfície de ataque/consumo de recursos desnecessário — num deploy de produção. Hoje, quem quiser subir só produção precisa editar o compose manualmente ou comentar serviços, o que é frágil e fácil de esquecer.

## What Changes

- `docker-compose.yaml` passa a conter só o essencial pra rodar o jogo: `flmane` e `db`
- Novo `docker-compose.override.yaml`: acrescenta `phpmyadmin` e `sonarqube`, mesclado automaticamente por `docker compose up`/`podman compose up` sem flag nenhuma (comportamento padrão do Compose Spec) — fluxo de dev não muda nada no dia a dia
- Produção roda com `docker compose -f docker-compose.yaml up -d` (ou equivalente Podman), explicitamente ignorando o override
- `CLAUDE.md`, `README.md`, `README.pt-BR.md` documentam os dois modos de subida (dev = comando padrão, prod = com `-f` explícito)

## Capabilities

### New Capabilities
- `compose-dev-prod`: descreve a separação entre `docker-compose.yaml` (base, produção) e `docker-compose.override.yaml` (acréscimos de dev), e como cada ambiente sobe

### Modified Capabilities
(nenhuma — não há spec existente cobrindo o compose)

## Impact

- `docker-compose.yaml`: remove `phpmyadmin` e `sonarqube`
- Novo arquivo `docker-compose.override.yaml`: contém `phpmyadmin` e `sonarqube`, incluindo a network/volumes que eles usam (`sonarqube_data`)
- `CLAUDE.md`/`README.md`/`README.pt-BR.md`: comandos de subida para dev vs. produção
- `utilitarios/build_container.sh`: continua usando o comportamento padrão do compose (dev, com override) — sem mudança de comportamento pro fluxo local existente
