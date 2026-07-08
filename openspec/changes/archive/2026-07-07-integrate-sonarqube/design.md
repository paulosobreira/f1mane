## Context

Projeto FlMane é um jogo Java 25 com Maven, Hibernate, Jersey, Tomcat embutido e frontend HTML5. Possui testes com JUnit 5 + Mockito e cobertura JaCoCo. Atualmente não há análise estática automatizada — bugs e code smells são detectados apenas em execução ou revisão manual.

A equipe deseja integrar SonarQube para análise contínua de qualidade, com execução local via Docker Compose e análise sob demanda via Maven.

## Goals / Non-Goals

**Goals:**
- Adicionar `sonar-maven-plugin` ao `pom.xml` com configuração padrão
- Criar `sonar-project.properties` com paths de código-fonte, exclusões e encoding
- Adicionar service `sonarqube` (community edition) ao `docker-compose.yaml`
- Criar profile Maven `sonar` para ativar análise sem interferir no build padrão
- Criar script `analise-sonar.sh` para iniciar SonarQube + rodar análise em um comando
- Configurar exclusões adequadas (sprites, webapp, testes, classes geradas)

**Non-Goals:**
- Configuração de Quality Gates personalizados (usar o default da comunidade)
- Integração com GitHub Actions / CI (será feita em change separada)
- Análise de branches ou PR decoration
- Plugin SonarQube para IDEs
- Migração de regras ou perfis de qualidade existentes

## Decisions

| Decisão | Opção escolhida | Alternativas | Razão |
|---|---|---|---|
| Versão SonarQube | Community (LTS) | Developer, Enterprise | Gratuita, cobre análise estática e cobertura. Sem custo de licença. |
| Porta do service | 9000 (host) → 9000 (container) | 80, 8080 | 8080 é usada pelo Tomcat do jogo; 9000 é a porta default do SonarQube |
| Banco do SonarQube | H2 embutido (default da imagem) | PostgreSQL, MySQL | Para uso local, H2 é suficiente. Se necessidade de persistência entre restarts, migrar depois |
| Ativação do plugin | Profile Maven `sonar` ativado explicitamente (`-Psonar`) | Sempre ativo, goal separado | Não impacta build padrão; análise sob demanda |
| Properties | `sonar-project.properties` na raiz | Embed no pom.xml | Separação clara entre build e config; mais fácil de manter |
| Exclusão de cobertura | `**/webapp/**`, `**/sprites/**`, `**/test/**`, `**/META-INF/**` | — | Evita poluir relatório com código não-java ou boilerplate |

## Risks / Trade-offs

| Risco | Mitigação |
|---|---|
| SonarQube Community não analisa C/C++ ou outras linguagens além das suportadas | Projeto é puro Java + HTML5/JS, coberto pelo Community |
| Análise consome ~2-4min adicionais no build | Ativada apenas sob demanda via profile `sonar`, não no `package` padrão |
| Container SonarQube consome ~1GB RAM e ~600MB de imagem | Documentado como requisito opcional; análise pode rodar em SonarCloud no futuro |
| `sonar-maven-plugin` pode conflitar com `maven-shade-plugin` no goal `verify` | Configurado para rodar apenas no goal `sonar:sonar`, sem amarrar em fase de build |
