# compose-dev-prod

## Purpose
Descreve a separação entre `docker-compose.yaml` (base, runtime de produção) e `docker-compose.override.yaml` (extras de desenvolvimento), carregado automaticamente pelo Compose.


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
- **THEN** o serviço `db` aparece definido uma única vez, só no arquivo base, e o serviço `flmane` aparece no override exclusivamente com a chave `ports` (republicação da porta de dev) — imagem, build, `mem_limit`, `environment` e `depends_on` ficam só no arquivo base

### Requirement: A porta publicada do jogo difere entre dev e produção
O serviço `flmane` SHALL ser publicado na porta `80` do host na subida de produção e na porta `8000` na subida de dev, ambas apontando pra `8080` do container. A diferença SHALL vir do `docker-compose.override.yaml`, sem exigir edição do arquivo base nem variável de ambiente no uso normal. Ambas as portas SHALL continuar sobrescrevíveis por `FLMANE_PORTA_HOST`.

#### Scenario: Dev publica em 8000
- **WHEN** o compose é executado sem `-f` explícito
- **THEN** o serviço `flmane` publica **somente** `8000:8080` — a porta `80` não é publicada

#### Scenario: Produção publica em 80
- **WHEN** o compose é executado com `-f docker-compose.yaml` explícito
- **THEN** o serviço `flmane` publica `80:8080`

#### Scenario: Lista de portas é substituída, não concatenada
- **WHEN** o override redefine `ports` do serviço `flmane`
- **THEN** a redefinição usa a tag `!override` do Compose Spec, porque o merge padrão do Compose concatena listas de portas — sem a tag, a subida de dev publicaria `80` e `8000` simultaneamente
