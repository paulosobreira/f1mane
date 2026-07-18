## 1. Rename do pacote Java

- [x] 1.1 Mover todo `src/main/java/br/f1mane/**` para `src/main/java/br/flmane/**`, atualizando a declaração `package` de cada arquivo
- [x] 1.2 Mover todo `src/test/java/br/f1mane/**` para `src/test/java/br/flmane/**`, atualizando a declaração `package` de cada arquivo
- [x] 1.3 Atualizar todo `import br.f1mane....` (main e test) para `import br.flmane....` — incluindo `br/nnpe/ImageUtil.java`, o único arquivo fora da árvore `br.f1mane` que importava dela
- [x] 1.4 Atualizar referências textuais a `br.f1mane`/`f1mane` em javadoc e comentários (ex.: `@see`, comentários explicativos que citam o FQCN)
- [x] 1.5 Confirmar que `mvn -q compile test-compile` passa sem erro após o rename

## 2. Strings literais não cobertas pelo rename de import

- [x] 2.1 Atualizar `MainLauncher.java` — string literal em `ProcessBuilder` de `"br.f1mane.MainLauncher"` para `"br.flmane.MainLauncher"`
- [x] 2.2 Atualizar `PilotoModoIADefesaAtaqueRecuoTest.java` — `Class.forName("br.f1mane.controles.ControleAutomacao$EstadoTecnico")` para `Class.forName("br.flmane.controles.ControleAutomacao$EstadoTecnico")`
- [x] 2.3 Atualizar `ObjetoPistaNivelDesenhoTest.java` — string XML embutida no teste, de `class="br.f1mane.entidades.ObjetoArquibancada"` para `class="br.flmane.entidades.ObjetoArquibancada"`

## 3. Artefatos de build e config

- [x] 3.1 Atualizar `pom.xml` — `mainClass` do plugin de shade/assembly, de `br.f1mane.MainLauncher` para `br.flmane.MainLauncher`
- [x] 3.2 Atualizar `src/main/webapp/WEB-INF/web.xml` — `servlet-class` (`br.f1mane.servidor.servlet.ServletPaddock` → `br.flmane.servidor.servlet.ServletPaddock`) e o `param-value` de scan de pacote do Jersey (`br.f1mane.servidor.rest` → `br.flmane.servidor.rest`)
- [x] 3.3 Atualizar `src/main/resources/META-INF/persistence.xml` — as 6 entradas `<class>br.f1mane.servidor.entidades.persistencia....</class>` para `br.flmane.servidor.entidades.persistencia....`

## 4. Dados de circuito (atributo `class` embutido)

- [x] 4.1 Reescrever `class="br.f1mane...."` → `class="br.flmane...."` em todos os `src/main/resources/circuitos/*_mro.xml`
- [x] 4.2 Reescrever `class="br.f1mane...."` → `class="br.flmane...."` em todos os `src/main/resources/circuitos/*_mro_meta.xml`, incluindo `montreal_mro_meta.xml` — inclui `montecarlo_transp_mro_meta .xml` (nome de arquivo com espaço antes de `.xml`, único meta existente para esse circuito; escapou do glob `*_mro_meta.xml` mas foi pego pela varredura case-insensitive final)
- [x] 4.3 Reescrever `class="br.f1mane...."` → `class="br.flmane...."` em `src/test/resources/circuitos/fixture_formato_antigo_mro.xml`
- [x] 4.4 Confirmar via `git diff` que cada arquivo tocado nesta seção só teve o valor do atributo `class` alterado (nenhuma outra mudança de conteúdo/formatação) — confirmado para todos; `montreal_mro.xml`/`montreal_mro_meta.xml` mostram diff adicional pré-existente (edição do usuário no editor de circuitos, já presente antes deste change)
- [x] 4.5 Não alterar os arquivos `.bak` de circuito (`montecarlo_mro.xml.bak`, `montreal_mro.xml.bak`, `monza_mro.xml.bak`, `nuburgring_mro.xml.bak`) — fora de escopo (D2 do design.md)

## 5. Documentação solta (fora de specs/CLAUDE.md)

- [x] 5.1 Atualizar `readme.md` — trocar `f1mane`/`br.f1mane` por `flmane`/`br.flmane` (preservada a URL do GitHub `raw.githubusercontent.com/paulosobreira/f1mane/...`, já que o repositório remoto continua se chamando `f1mane`)
- [x] 5.2 Atualizar `readme-pt.md` — mesma lógica de 5.1
- [x] 5.3 Atualizar `docs/sdd.md` — trocar `br.f1mane`/`F1ManeDados` por `br.flmane`/`FlManeDados`; reescrita a nota sobre o nome (linha ~49), que afirmava que esses artefatos "não são renomeados" — texto agora desatualizado pelo próprio propósito deste change
- [x] 5.4 Não alterar nenhum arquivo em `openspec/specs/` nem `openspec/changes/archive/` (registro histórico, fora de escopo)
- [x] 5.5 Não alterar `CLAUDE.md`

## 6. Achados adicionais durante a verificação (fora da lista original, mesmo escopo)

- [x] 6.1 Renomear a classe `F1ManeDados` (base abstrata de todas as entidades de persistência) para `FlManeDados`, incluindo a variável `f1ManeDados` — não foi pego pelo sed original por usar capitalização mista (`F1ManeDados`), fora do padrão `br.f1mane` em minúsculas
- [x] 6.2 Atualizar `<title>F1Mane Conf</title>` e `<h3>F1Mane` em `src/main/webapp/conf.jsp` — mesmo motivo (capitalização mista)
- [x] 6.3 Atualizar scripts em `utilitarios/` (`analise-sonar.sh`, `gerar-instalador.bat`, `simulacao.sh`, `simulacao_batch.sh`, `simulacao_suave_batch.sh`) — referências a `br.f1mane.MainLauncher`/`MainFrameSimulacao`, nome do volume Docker `f1mane_sonarqube_data`, e comentário `F1Mane` em `gerar-instalador.bat`; não estavam na lista original de artefatos de build
- [x] 6.4 Atualizar `.gitignore` (`f1mane_save.xml` → `flmane_save.xml`) — entrada não referenciada em nenhum código encontrado, mas mantida consistente
- [x] 6.5 Atualizar `src/main/resources/idiomas/mensagens_{en,es,it,pt}.properties` — chave `f1maneSwing` → `flmaneSwing` (não referenciada em nenhum código Java encontrado) e o texto de `backGroundNull` em es/pt que citava um caminho `/f1mane/...`

## 7. Verificação final

- [x] 7.1 Buscar por `f1mane` case-insensitive em todo o repositório, excluindo `target/`, `.git/`, `openspec/`, `.claude/`, `tomcat.8080/` (work dir gerado pelo Tomcat, não versionado), `CLAUDE.md`, `properties/*/carros.properties`, `properties/*/pilotos.properties`, nomes de circuito e os `.bak` da seção 4 — zero ocorrência residual fora das exclusões conhecidas (a busca inicial era case-sensitive só para `f1mane` minúsculo; ampliada para case-insensitive depois do achado da seção 6, que só apareceu em capitalização mista)
- [x] 7.2 Rodar `mvn test` completo e confirmar suíte verde — 767 testes, 0 falhas, 0 erros, 2 skipped (pré-existentes)
- [x] 7.3 Carregar manualmente (via `MainFrameSimulacao`) um circuito comum (Catalunya, 2024, 3 voltas) e confirmar que desserializa sem `ClassNotFoundException` — corrida completou normalmente, resultados finais gerados
- [x] 7.4 Build do fat jar (`mvn clean package -Ph2 -DskipTests`) e confirmar que `java -jar target/flmane.jar --headless` sobe normalmente — manifest `Main-Class: br.flmane.MainLauncher` confirmado; Tomcat sobe (`SERVER STARTED`); `br.flmane.servidor.servlet.ServletPaddock` responde HTTP 200 em `/flmane/ServletPaddock`; `index.html` responde 200; nenhuma exceção ou `ClassNotFoundException` no log de boot
