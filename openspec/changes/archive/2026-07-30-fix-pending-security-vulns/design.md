## Context

Auditoria de segurança (GitHub Dependabot/CodeQL alerts) confirmou que a maioria dos alertas listados já está corrigida nas versões atuais do `pom.xml` (hibernate-core 7.4.0.Final, jackson-databind 2.18.9, commons-beanutils 1.11.0, commons-lang3 3.18.0, netty-codec-http 4.1.136.Final). Dois itens permanecem pendentes de fato no working tree:

1. `src/main/webapp/jquery/jquery-3.2.1.min.js` — vendored, servido diretamente ao cliente web. Vulnerável a XSS via `jQuery.htmlPrefilter`/parsing de `<option>` malformado.
2. `commons-lang:commons-lang:2.6` no `pom.xml` — grupo Maven legado (pré-lang3), sem uso confirmado por grep em `src/main/java`. Alert #9 está marcado "fix already started", não "fixed", e essa linha antiga não recebe patch.

## Goals / Non-Goals

**Goals:**
- Eliminar as duas vulnerabilidades pendentes sem alterar comportamento funcional do jogo/editor/servidor.
- Confirmar (não presumir) que `commons-lang` 2.6 é dependência morta antes de remover, evitando quebra silenciosa.
- Deixar rastro reproduzível (jar rebuildado) para teste manual, conforme CLAUDE.md.

**Non-Goals:**
- Não mexer em dependências já corrigidas (hibernate, jackson, beanutils, commons-lang3, netty) — fora de escopo.
- Não migrar o projeto para gerenciamento de assets via npm/CDN — troca do jquery é local, mesmo diretório vendored.
- Não adicionar scanner automatizado de dependências (CI) nesta mudança.

## Decisions

- **jQuery: substituição direta do arquivo minificado, mesma major (3.x).**
  Alternativa considerada: pular pra jQuery 4.x. Rejeitada — projeto usa jQuery legado (plugins/seletores antigos possivelmente incompatíveis com breaking changes do 4.x) e o fix do XSS já está disponível dentro da linha 3.x (>=3.5.0). Menor risco de regressão em `src/main/webapp/*.js`/JSPs que dependem de comportamento jQuery 3.x.

- **commons-lang legado: grep antes de remover, não remoção cega.**
  Alternativa considerada: remover direto do pom.xml sem checar. Rejeitada — CLAUDE.md exige investigar causa/uso antes de descartar algo; dependência pode ser transitiva de outro artefato ou ter uso indireto (reflection, import estático) que grep simples não pega em uma primeira passada. Passo explícito de verificação (`grep -r "org.apache.commons.lang\."` excluindo `lang3`, mais `mvn dependency:tree` para confirmar se é declarada como transitiva por outra lib) entra como tarefa própria antes da remoção.

- **Se houver uso real de `org.apache.commons.lang.*`: migrar para `commons-lang3` equivalente, não manter as duas.**
  Manter as duas dependências (`commons-lang` 2.6 e `commons-lang3` 3.18.0) simultaneamente deixaria a vulnerabilidade presente mesmo com lang3 disponível. Migração de import é mecânica (`org.apache.commons.lang.StringUtils` → `org.apache.commons.lang3.StringUtils`, etc.) mas precisa revisão método a método pois nem toda API é 1:1.

## Risks / Trade-offs

- [jQuery 3.5+ pode ter mudado comportamento de seletores/eventos usados em código legado do editor/HUD web] → Testar manualmente as páginas web (`index.html`, editor) após troca, conforme regra do projeto de validar UI no browser antes de reportar concluído.
- [commons-lang pode ser dependência transitiva de outra lib no classpath, não direta] → Rodar `mvn dependency:tree -Dincludes=commons-lang:commons-lang` antes de remover; se transitiva, documentar e decidir se exclusão via `<exclusions>` é aplicável ou se o risco é aceitável (dependência de terceiro fora do controle direto).
- [Migração de lang→lang3 pode ter diferença sutil de comportamento em algum método] → Rodar suíte de testes (`mvn test`) após qualquer migração de import, mais rebuild do jar.

## Migration Plan

1. Baixar/gerar jQuery 3.5.0+ minificado, substituir `src/main/webapp/jquery/jquery-3.2.1.min.js` (manter nome de arquivo ou atualizar referências, o que for menos invasivo — checar HTMLs/JSPs que referenciam o caminho).
2. Rodar `mvn dependency:tree -Dincludes=commons-lang:commons-lang` e `grep -rn "org.apache.commons.lang\." src/main/java | grep -v lang3`.
3. Baseado no resultado: remover dependência do pom.xml OU migrar imports para lang3 e então remover.
4. `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`.
5. Teste manual: abrir launcher web, navegar páginas que usam jquery; rodar `mvn test` para cobrir uso de commons-lang3.
6. Rollback: reverter commit único da mudança (arquivo jquery + pom.xml são independentes, revert é trivial via git).

## Open Questions

- Nenhuma pendência bloqueante — verificação de uso de `commons-lang` será feita como primeira tarefa de implementação e pode alterar se o passo é "remover" ou "migrar".
