## 1. Ambiente

- [x] 1.1 Instalar/disponibilizar JDK 25 no ambiente de desenvolvimento local (ex.: `sdk install java 25-tem` via sdkman) e no CI
- [x] 1.2 Confirmar `java -version` e `mvn -version` apontando para JDK 25 antes de prosseguir

## 2. Build

- [x] 2.1 Atualizar `maven.compiler.release` de `21` para `25` em `pom.xml`
- [x] 2.2 Atualizar `<release>` do `maven-compiler-plugin` de `21` para `25` em `pom.xml`

## 3. Validação de testes e dependências

- [x] 3.1 Rodar `mvn test` sob JDK 25 e registrar quaisquer falhas/erros novos — Mockito/byte-buddy passaram sem problema; o bloqueio real foi o `jacoco-maven-plugin` 0.8.12, que não reconhece o class file major version 69 (Java 25) na fase de relatório de cobertura (`Unsupported class file major version 69`). Atualizado para `0.8.15` (mais recente publicada), que resolveu o erro.
- [x] 3.2 `mockito-core` (5.15.2) não precisou de bump — os 435 testes passaram normalmente sob JDK 25 sem erro nem warning novo da cadeia de mocks
- [x] 3.3 Warnings de agente dinâmico (auto-attach do byte-buddy) continuam iguais aos que já existiam em JDK 21; nenhum ajuste adicional necessário

## 4. Empacotamento e execução

- [x] 4.1 Rodar `mvn clean package -Ph2 -DskipTests` e validar geração do `flmane.jar`
- [x] 4.2 Rodar `mvn clean package -Pmysql -DskipTests` e validar geração do `flmane.jar`
- [x] 4.3 Subir `MainLauncher` (`--headless`, equivalente ao modo Docker) e validar web em `http://localhost:8080/flmane/html5/index.html` — Tomcat 11 embutido sobe normalmente sob JDK 25, endpoint retorna HTTP 200
- [x] 4.4 Subir modo solo (`MainFrame`) e validar que o jogo abre normalmente — processo permanece rodando (GUI Swing ativa) sem exceções em `logs/flmane.log`
- [x] 4.5 Rodar a simulação headless (`br.f1mane.MainFrameSimulacao 2024 Catalunya 72`, equivalente a `./simulacao.sh`) e validar que a simulação conclui sem erro — corrida completa as 72 voltas e termina em "========final corrida============" sem exceções no log

## 5. Docker

- [x] 5.1 Atualizar `flmane.dockerfile` para `eclipse-temurin:25-jre-alpine` — tag confirmada existente no Docker Hub, fallback Debian não foi necessário
- [x] 5.2 Rodar `docker build -f flmane.dockerfile` (equivalente à etapa de build.sh) e validar que a imagem `sowbreira/flmane:latest` é construída com sucesso sobre `eclipse-temurin:25-jre-alpine`
- [x] 5.3 Rodar `docker compose up -d` e validar `db`/`phpmyadmin`; `flmane` falhou ao subir só por limitação do ambiente local (podman rootless não pode bindar a porta privilegiada 80 do mapeamento `80:8080`, `net.ipv4.ip_unprivileged_port_start`), não por causa do Java 25. Validado à parte: rodando a mesma imagem numa porta não-privilegiada (`-p 8081:8080`) na rede do compose, o container sobe, `java -version` dentro dele reporta Temurin 25.0.3, e `http://localhost:8081/flmane/html5/index.html` responde HTTP 200. `db` (MySQL 8.4) subiu com healthcheck **healthy**; `phpmyadmin` também subiu normalmente. Stack limpa com `docker compose down` ao final, sem containers residentes.

## 6. Documentação e spec

- [x] 6.1 Verificado `CLAUDE.md` — não há menção explícita a uma versão de Java (só comandos `java -jar`/`java -cp` sem número de versão), nenhuma alteração necessária
- [x] 6.2 Delta spec de `java-tomcat-upgrade` conferida contra a implementação final — build Java 25, testes passando sob JDK 25 e imagem `eclipse-temurin:25-jre-alpine` (tag Alpine existe, fallback Debian não foi necessário) batem com o que foi implementado; nenhum ajuste no spec foi necessário
