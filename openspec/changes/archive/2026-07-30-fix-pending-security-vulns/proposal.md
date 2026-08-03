## Why

Auditoria de dependências (GitHub security alerts) mostrou duas vulnerabilidades ainda pendentes no working tree: o bundle jQuery 3.2.1 servido em produção (XSS, alert #5) nunca foi atualizado apesar do alert estar marcado "closed as fixed", e o pom.xml ainda declara `commons-lang:commons-lang:2.6` (grupo legado, distinto de `commons-lang3`), vulnerável a Uncontrolled Recursion (alert #9, status "fix already started", sem correção disponível nessa linha de versão). Ambos representam risco real hoje, não histórico.

## What Changes

- Atualizar `src/main/webapp/jquery/jquery-3.2.1.min.js` para jQuery `>=3.5.0` (última 3.x estável), eliminando o XSS herdado do parser HTML do jQuery.
- Auditar uso de `commons-lang:commons-lang:2.6` em `src/main/java` e `src/main/webapp` (JSPs/scripts server-side, se houver):
  - Se não houver uso direto: remover a dependência do `pom.xml`.
  - Se houver uso residual: migrar as chamadas para `org.apache.commons:commons-lang3:3.18.0` (já presente no pom) e então remover a dependência legada.
- Rebuild do jar (`mvn clean package -Ph2 -DskipTests`) após qualquer alteração em `src/main/java`, conforme diretriz do projeto.
- Fora de escopo: commons-beanutils, hibernate-core, jackson-databind, commons-lang3, netty-codec-http — já corrigidos nas versões atuais do pom.xml, não requerem ação.

## Capabilities

### New Capabilities
- `dependency-vulnerability-remediation`: garante que dependências third-party (frontend vendored e Maven) usadas pelo projeto não contenham vulnerabilidades conhecidas sem correção disponível, e que dependências sem uso não fiquem declaradas no build.

### Modified Capabilities
(nenhuma — não há spec existente cobrindo versionamento de dependências)

## Impact

- Código afetado: `src/main/webapp/jquery/jquery-3.2.1.min.js` (substituição de arquivo), `pom.xml` (remoção/ajuste de dependência).
- Possível impacto em `src/main/java` se houver uso de `org.apache.commons.lang.*` (grupo antigo) a migrar para `org.apache.commons.lang3.*`.
- Build: exige `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar` antes de qualquer teste manual.
- Sem mudança de comportamento funcional esperada — mudança é puramente de manutenção de segurança.
