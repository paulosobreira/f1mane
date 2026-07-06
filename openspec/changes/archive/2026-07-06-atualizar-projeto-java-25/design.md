## Context

O projeto foi migrado para Java 21 + Jakarta EE 11 (Tomcat 11, Hibernate 7.4, Jersey 4.x) no change arquivado `java-tomcat-upgrade`. Java 25 é a LTS seguinte e já é a versão recomendada para novos deploys. O ambiente de desenvolvimento atual (verificado nesta máquina) tem JDK 21.0.2 via sdkman e não tem acesso à internet para instalar candidatos novos a partir daqui — a instalação do JDK 25 é um passo manual do desenvolvedor/CI, fora do alcance deste change no repositório.

Diferente do change anterior, este é um bump de versão de plataforma, não uma migração de API (`javax.*` → `jakarta.*`). As dependências principais (Hibernate 7.4, Jersey 4.x, Tomcat 11, Jakarta Servlet 6.1) já são recentes o suficiente para não exigirem mudança de versão só por causa do JDK. O ponto de atenção real é a cadeia de teste: `mockito-core` usa `byte-buddy` (inline mock maker) para instrumentar classes em tempo de execução, e essa combinação historicamente precisa de uma versão nova o bastante para reconhecer o bytecode/major version de JDKs recém-lançados — os logs de teste atuais já mostram avisos de "Java agent loaded dynamically" e auto-attach do byte-buddy, que tende a ficar mais restrito em JDKs futuros.

## Goals / Non-Goals

**Goals:**
- Compilar e rodar o projeto (build, testes, os três modos de execução) sob JDK 25.
- Manter os três perfis Maven (`h2`, `mysql`, `test`) e os dois Dockerfiles/compose funcionando sem mudança de comportamento de jogo.
- Deixar o `pom.xml` e o `flmane.dockerfile` como única fonte de verdade da versão de Java do projeto (sem duplicar o número em outros lugares).

**Non-Goals:**
- Adotar sintaxe/APIs novas do Java 22–25 (ex.: `Scoped Values`, mudanças em `switch` pattern matching, etc.) — puramente bump de versão.
- Atualizar Hibernate/Jersey/Tomcat para versões mais novas além do que for estritamente necessário para compatibilidade com JDK 25.
- Mudar a estratégia de containerização (múltiplos estágios, distroless, etc.) além de trocar a tag da imagem base.

## Decisions

- **Trocar só `maven.compiler.release` e a tag da imagem Docker, sem tocar em dependências, a menos que o build/testes quebrem.** Alternativa considerada: fazer bump preventivo de todas as libs relacionadas a JVM (mockito, byte-buddy, surefire, shade). Rejeitada como passo automático porque adicionaria mudanças não testadas sem necessidade comprovada — a abordagem é rodar `mvn test`/`mvn clean package` sob JDK 25 primeiro e só then decidir quais dependências realmente precisam de bump, guiado pelo erro real (fail-fast, não bump especulativo).
- **`mockito-core` é tratado como a dependência de maior risco e verificado explicitamente**, dado que o warning de auto-attach do byte-buddy já aparece nos testes em JDK 21 (`Mockito is currently self-attaching to enable the inline-mock-maker`). Se os testes falharem sob JDK 25 por causa disso, o plano é atualizar `mockito-core` para a versão estável mais recente compatível (ex.: linha 5.18.x ou superior, a confirmar no momento da implementação, já que não há acesso à internet neste ambiente para checar o changelog agora).
- **Spec modificada é a mesma `java-tomcat-upgrade`** (não uma nova capability), porque os requisitos que estão mudando ("Projeto compilado com Java 21", "Imagem Docker base atualizada para JRE 21") já existem e este change apenas atualiza o número de versão neles — não é um novo comportamento de sistema.

## Risks / Trade-offs

- [Risco] `mockito-core`/`byte-buddy` não reconhece o bytecode do JDK 25 e os testes com mock passam a falhar ou emitir erro em vez de warning → Mitigação: rodar a suíte de testes sob JDK 25 antes de considerar o change concluído; se necessário, atualizar `mockito-core` para uma versão com suporte confirmado.
- [Risco] Imagem `eclipse-temurin:25-jre-alpine` pode não existir ainda no Docker Hub no momento da implementação (cadência de publicação de imagens Alpine costuma atrasar em relação ao release da JVM) → Mitigação: se a tag Alpine não existir, usar `eclipse-temurin:25-jre` (Debian-based) como fallback temporário, documentando a diferença de tamanho de imagem.
- [Risco] Ambiente local/CI sem JDK 25 instalado impede validação imediata → Mitigação: instalar via sdkman (`sdk install java 25-tem` ou equivalente) como primeiro passo manual antes de rodar os comandos de build; este passo não é automatizável a partir deste sandbox (sem acesso à internet confirmado).
- [Trade-off] Não atualizar Hibernate/Jersey/Tomcat junto pode deixar uma janela onde o projeto está em JDK 25 mas em versões de biblioteca "antigas" para o ecossistema Jakarta EE 11 — aceitável porque essas libs já são compatíveis e atualizar todas de uma vez aumentaria o raio de impacto sem necessidade.

## Migration Plan

1. Instalar/disponibilizar JDK 25 no ambiente de desenvolvimento e CI (fora do repositório).
2. Atualizar `pom.xml` (`maven.compiler.release` e `maven-compiler-plugin`) para `25`.
3. Rodar `mvn test` sob JDK 25; corrigir/atualizar dependências apenas se algo quebrar.
4. Rodar `mvn clean package -Ph2 -DskipTests` e `-Pmysql -DskipTests`; validar os três entry points (`MainLauncher`, `MainFrame`, `AppletPaddock`).
5. Atualizar `flmane.dockerfile` para a tag `25-jre-alpine` (ou fallback), rebuildar a imagem e validar `docker compose up` local.
6. Atualizar `CLAUDE.md` se houver menção explícita à versão de Java.

Rollback: reverter o commit único que troca `pom.xml`/`flmane.dockerfile` — não há migração de dados ou schema envolvida, então o rollback é reverter os arquivos de configuração.

## Open Questions

- Qual versão mínima de `mockito-core` de fato suporta JDK 25 sem warnings/erros? Só será possível confirmar rodando os testes no momento da implementação (ambiente com internet/JDK 25 disponível).
- A imagem `eclipse-temurin:25-jre-alpine` já existe publicamente na data da implementação, ou é necessário usar a variante Debian como fallback?
