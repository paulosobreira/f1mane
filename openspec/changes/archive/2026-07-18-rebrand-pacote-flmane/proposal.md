## Why

O rebrand público do projeto para "Fl-Mane" (Formula Livre) já está em vigor em várias camadas — `pom.xml` (`artifactId=flmane`), título e CSS do cliente web (`Fl-Mane`, `flmane.css`), nome do jar (`flmane.jar`) — mas o pacote Java principal continua `br.f1mane.*` (240 arquivos) e a string `f1mane` ainda aparece em artefatos internos de código e build (config de servlet/JPA, script de relançamento do launcher, atributos de classe embutidos nos arquivos de circuito). Essa divergência entre o nome público já adotado e o nome interno ainda em uso é o que este change fecha.

## What Changes

- Renomeia o pacote Java `br.f1mane.*` para `br.flmane.*` em todo `src/main/java` e `src/test/java` (~240 arquivos: declarações de pacote, imports, javadoc `@see`/referências textuais).
- Atualiza toda referência literal à string `f1mane` em artefatos de código/build fora do pacote Java:
  - `pom.xml` — `mainClass` do plugin de shade/assembly (`br.f1mane.MainLauncher` → `br.flmane.MainLauncher`).
  - `src/main/webapp/WEB-INF/web.xml` — `servlet-class` e o `param-value` de scan de pacote do Jersey (`br.f1mane.servidor.rest` → `br.flmane.servidor.rest`).
  - `src/main/resources/META-INF/persistence.xml` — as 6 entradas `<class>br.f1mane.servidor.entidades.persistencia....</class>`.
  - `MainLauncher.java` — a string literal `"br.f1mane.MainLauncher"` usada em `ProcessBuilder` para relançar o processo headless.
  - `PilotoModoIADefesaAtaqueRecuoTest.java` — `Class.forName("br.f1mane.controles.ControleAutomacao$EstadoTecnico")`.
  - `ObjetoPistaNivelDesenhoTest.java` — string XML embutida no teste (`class="br.f1mane.entidades.ObjetoArquibancada"`).
- **BREAKING** (dados persistidos): reescreve o atributo `class="br.f1mane...."` em todos os arquivos de circuito do repositório — `src/main/resources/circuitos/*_mro.xml` e `*_mro_meta.xml` (~80 arquivos, incluindo `montreal_mro.xml`/`montreal_mro_meta.xml`) — para `class="br.flmane...."`, e no fixture de teste `src/test/resources/circuitos/fixture_formato_antigo_mro.xml`. A persistência desses arquivos usa `java.beans.XMLEncoder`/`XMLDecoder`, que resolve o atributo `class` via `Class.forName()` sem nenhum mecanismo de alias — por isso o rename do pacote Java só é seguro se todo `class="br.f1mane...."` existente for reescrito junto, na mesma mudança. Ver design.md para a decisão completa e o risco não mitigado sobre saves externos ao repositório.
- Nenhuma mudança de comportamento de jogo é pretendida — apenas o nome do pacote/classe que hospeda cada mecânica muda, e o atributo `class` nos XMLs de circuito passa a apontar para o novo nome.

## Capabilities

### New Capabilities
- `pacote-flmane`: garante que todo artefato de código (pacote Java, config de build/servlet/JPA, atributo `class` embutido nos XMLs de circuito) referencia exclusivamente `br.flmane`/`flmane`, sem nenhuma ocorrência residual de `br.f1mane`/`f1mane` fora das exclusões de escopo já registradas (properties de carro/piloto, nomes de circuito, `openspec/specs/`, `openspec/changes/archive/`, `CLAUDE.md`).

### Modified Capabilities
(nenhuma — por decisão explícita do usuário, os arquivos em `openspec/specs/` não são alterados por este change, mesmo os que hoje citam `br.f1mane`/`f1mane` por nome em texto de requisito — `dev-editor-tools`, `sdd-execution-modes`, `launcher-servidor-processo-separado`, `letsrace-endpoint-tests`, `controles-business-logic-tests`. Essas referências ficam desatualizadas após este change e devem ser corrigidas por uma iniciativa separada.)

## Impact

- **Código afetado**: todo `src/main/java/br/f1mane/**` e `src/test/java/br/f1mane/**` (movidos para `br/flmane/**`), `pom.xml`, `src/main/webapp/WEB-INF/web.xml`, `src/main/resources/META-INF/persistence.xml`.
- **Dados afetados**: `src/main/resources/circuitos/*_mro.xml`, `*_mro_meta.xml` (~80 arquivos) e `src/test/resources/circuitos/fixture_formato_antigo_mro.xml` — atributos `class="br.f1mane...."` reescritos para `class="br.flmane...."`.
- **Fora de escopo, explicitamente**: `src/main/resources/properties/*/carros.properties` e `pilotos.properties` (nomes de carro/piloto com referências a F1, ex. `BMW-Sauber-F1-07`); nomes de circuito com referência a F1; `openspec/specs/` e `openspec/changes/archive/` (registro histórico); `CLAUDE.md`. Tratados por iniciativa(s) separada(s).
- **Risco conhecido, não mitigado por este change**: campeonatos/saves de box gravados por um jogador fora do repositório (ex. `~/flmane-data`) via `ControleCampeonato` (`XMLEncoder`/`XMLDecoder`) podem conter `class="br.f1mane...."` e deixarão de carregar após o rename — não há shim de compatibilidade retroativa neste change (estratégia A, confirmada com o usuário). Ver design.md.
- **Sem impacto em**: `properties/*` de carros/pilotos, nomes de circuito, banco de dados (H2/MySQL — sem uso de FQCN como discriminador confirmado em `persistencia/`), i18n (o único hit de "F1" em `mensagens_en.properties`/`mensagens_it.properties` é a tecla de função F1, não a marca, e não é tocado).
