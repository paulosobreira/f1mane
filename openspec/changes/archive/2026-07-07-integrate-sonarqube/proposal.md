## Why

O projeto não possui análise estática de código automatizada. Sem SonarQube, bugs, code smells e vulnerabilidades passam despercebidos até execução ou revisão manual. Integrar SonarQube permitirá avaliação contínua da qualidade do código, detecção precoce de bugs e suporte a decisões de refatoração com base em métricas objetivas.

## What Changes

- Adicionar configuração do `sonar-maven-plugin` no `pom.xml`
- Criar arquivo `sonar-project.properties` com configurações do projeto
- Adicionar service SonarQube no `docker-compose.yaml` para execução local
- Adicionar profile Maven `sonar` para análise sob demanda
- Configurar exclusões de cobertura e paths de código válidos
- Script de análise rápida (`analise-sonar.sh`) para uso no dia a dia

## Capabilities

### New Capabilities
- `sonar-config`: Configuração base do SonarQube no Maven e propriedades do projeto
- `sonar-docker`: Ambiente SonarQube containerizado via Docker Compose para análise local

### Modified Capabilities

<!-- Nenhuma capability existente tem requisitos alterados — trata-se de nova ferramenta de qualidade, sem mudança de comportamento em specs existentes -->

## Impact

- `pom.xml` — novo plugin `sonar-maven-plugin` e profile `sonar`
- `docker-compose.yaml` — novo service `sonarqube`
- `openspec/specs/` — dois novos diretórios de spec: `sonar-config/` e `sonar-docker/`
- Build time — análise SonarQube adiciona tempo ao ciclo de build quando ativada
- Dependência externa — Docker image `sonarqube:community` (~600MB) necessária para análise local
