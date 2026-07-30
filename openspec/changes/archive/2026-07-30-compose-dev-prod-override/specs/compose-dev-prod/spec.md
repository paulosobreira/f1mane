## ADDED Requirements

### Requirement: Compose base contém só os serviços de runtime do jogo
`docker-compose.yaml` (arquivo base) SHALL conter somente os serviços necessários pra rodar o jogo em produção: `flmane` e `db`. Ferramentas de apoio ao desenvolvimento (acesso ao banco, análise estática) SHALL NOT estar no arquivo base.

#### Scenario: Subida explícita de produção não inclui ferramentas de dev
- **WHEN** o compose é executado com `-f docker-compose.yaml` explícito (sem carregar override)
- **THEN** somente os serviços `flmane` e `db` são criados

### Requirement: Extras de desenvolvimento ficam num override carregado automaticamente
`docker-compose.override.yaml` SHALL conter `phpmyadmin` e `sonarqube`, e SHALL ser carregado automaticamente pelo Compose (`docker compose`/`podman compose`/`podman-compose`) sempre que o comando for executado sem `-f` explícito, mesclando-se ao `docker-compose.yaml` base.

#### Scenario: Subida padrão (sem -f) inclui os extras de dev
- **WHEN** o compose é executado sem nenhum `-f` explícito no diretório do projeto
- **THEN** os quatro serviços (`flmane`, `db`, `phpmyadmin`, `sonarqube`) são criados, com `phpmyadmin`/`sonarqube` vindos do override

#### Scenario: Nenhuma definição de flmane/db é duplicada entre os arquivos
- **WHEN** o conteúdo de `docker-compose.yaml` e `docker-compose.override.yaml` é inspecionado
- **THEN** os serviços `flmane` e `db` aparecem definidos uma única vez, só no arquivo base
