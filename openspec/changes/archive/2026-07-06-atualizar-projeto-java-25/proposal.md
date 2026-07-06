## Why

O projeto está fixado em Java 21 (LTS de 2023) desde a migração documentada em `java-tomcat-upgrade`. Java 25 é a LTS mais recente (setembro/2025) e já está disponível nos runtimes usados em produção; atualizar reduz o gap de patches de segurança e desempenho da JVM e evita que o projeto fique preso numa LTS cada vez mais distante da atual.

## What Changes

- Atualizar `maven.compiler.release` (propriedade e `maven-compiler-plugin`) de `21` para `25` no `pom.xml`.
- Atualizar a imagem base do `flmane.dockerfile` de `eclipse-temurin:21-jre-alpine` para `eclipse-temurin:25-jre-alpine`.
- Revisar e, se necessário, atualizar versões de dependências/plugins sensíveis à versão da JVM usada em build/test (`mockito-core`/`byte-buddy`, `maven-compiler-plugin`, `maven-shade-plugin`) para versões com suporte confirmado a Java 25 — **BREAKING** apenas se alguma dessas libs exigir bump de versão que altere comportamento de mocks/build.
- Validar que `mvn test`, `mvn clean package -Ph2 -DskipTests` e `mvn clean package -Pmysql -DskipTests` continuam funcionando sob JDK 25, e que os três modos de execução (`MainLauncher`, `MainFrame`, `AppletPaddock`) sobem normalmente.
- Atualizar a documentação de setup (`CLAUDE.md`, se aplicável) para referenciar Java 25 como versão alvo.

Fora de escopo: qualquer adoção de novas features de linguagem do Java 22–25 (ex.: padrões de `switch` mais recentes, `Scoped Values`, etc.) — este change é só o bump de versão da plataforma, não uma modernização de sintaxe.

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `java-tomcat-upgrade`: os requisitos "Projeto compilado com Java 21" e "Imagem Docker base atualizada para JRE 21" mudam para exigir Java 25 em vez de Java 21.

## Impact

- `pom.xml` (`maven.compiler.release`, `maven-compiler-plugin`, possíveis bumps de `mockito-core`)
- `flmane.dockerfile` (imagem base)
- Ambiente de desenvolvimento local e CI: exige JDK 25 instalado (hoje o ambiente tem JDK 21 via sdkman)
- Nenhuma mudança de comportamento de jogo esperada; risco concentrado em compatibilidade de build/test (Mockito inline-mock-maker com byte-buddy, warnings de agent dinâmico já observados nos testes atuais em JDK 21) e no `docker-compose.yaml`/deploy (imagem Docker maior/menor conforme disponibilidade de Alpine para Java 25).
