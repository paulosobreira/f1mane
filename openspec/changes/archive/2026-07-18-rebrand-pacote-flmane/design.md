## Context

O jogo já se apresenta publicamente como "Fl-Mane" (`pom.xml` `artifactId=flmane`, título do webapp, `flmane.css`, `flmane.jar`), mas o pacote Java raiz continua `br.f1mane.*` (240 arquivos em `src/main/java`/`src/test/java`) e a string `f1mane` ainda aparece em vários artefatos de código/build que citam esse pacote por nome (config de servlet, JPA, relançamento de processo do launcher).

A investigação que precedeu este change (feita em modo de exploração, `/opsx:explore`) levantou um ponto que muda o formato do trabalho: a persistência de circuitos (`Circuito`, `ObjetoTransparencia`, `ObjetoEscapada`, `ObjetoLivre`, `TipoObjetoLivre` etc., usados por `CarregadorRecursos`, `MainPanelEditor` e `ControleCampeonato`) é feita com `java.beans.XMLEncoder`/`XMLDecoder`, **não** XStream. Cada objeto serializado grava seu nome de classe totalmente qualificado no atributo `class="..."` do XML (ex.: `<object class="br.f1mane.entidades.Circuito">`), e `XMLDecoder` resolve esse atributo via `Class.forName()` puro no carregamento — sem nenhum hook de alias, remapeamento ou compatibilidade com nomes antigos. Isso significa que renomear o pacote Java sem também reescrever esse atributo em todo arquivo de circuito existente quebra o carregamento de **todo** circuito do jogo com `ClassNotFoundException`.

Confirmado por grep: os ~80 arquivos em `src/main/resources/circuitos/*_mro.xml`/`*_mro_meta.xml` (incluindo `montreal_mro.xml`/`montreal_mro_meta.xml`, que tinham edição em andamento no editor de circuitos no momento desta investigação) carregam esse atributo. Não há uso de `XStream`, `DiscriminatorValue` ou qualquer outro mecanismo de nome-de-classe-como-string na camada de persistência JPA (`br.f1mane.servidor.entidades.persistencia`) — só `persistence.xml` lista as classes por FQCN, o que é config de boot do Hibernate, não dado persistido em linha de banco.

## Goals / Non-Goals

**Goals:**
- Renomear `br.f1mane.*` → `br.flmane.*` em todo o código-fonte (main + test), preservando 100% do comportamento observável do jogo.
- Atualizar toda referência à string `f1mane` em artefatos de build/config que apontam para esse pacote por nome (`pom.xml`, `web.xml`, `persistence.xml`, `MainLauncher`, 2 testes).
- Reescrever o atributo `class="br.f1mane...."` em todos os arquivos de circuito do repositório (incluindo o fixture de teste de formato antigo) na mesma mudança, para que o rename do pacote não quebre o carregamento de nenhum circuito existente no repositório.

**Non-Goals:**
- Não introduz nenhum shim de compatibilidade retroativa para `class="br.f1mane...."` em arquivos fora do repositório (saves de campeonato/box de um jogador real, tipicamente em `~/flmane-data`). Ver Risks/Trade-offs.
- Não altera `properties/*/carros.properties`/`pilotos.properties`, nomes de circuito com referência a F1, `openspec/specs/`, `openspec/changes/archive/` nem `CLAUDE.md` — decisão explícita do usuário, tratada como escopo de iniciativa(s) separada(s), mesmo sabendo que isso deixa 5 specs vivas (`dev-editor-tools`, `sdd-execution-modes`, `launcher-servidor-processo-separado`, `letsrace-endpoint-tests`, `controles-business-logic-tests`) com texto de requisito citando `br.f1mane` desatualizado após este change.
- Não redesenha o mecanismo de persistência de circuitos (continua `XMLEncoder`/`XMLDecoder` com FQCN embutido) — só atualiza o valor da string.

## Decisions

### D1 — Estratégia de dados persistidos: reescrever em vez de shim de compatibilidade

Duas estratégias foram consideradas para os `class="br.f1mane...."` embutidos nos XMLs de circuito:

- **A (escolhida)**: reescrever o atributo `class` em todo arquivo de circuito do repositório como parte deste mesmo change, num passo mecânico de find-and-replace sobre o valor exato do atributo (`class="br.f1mane.` → `class="br.flmane.`), preservando todo o resto do XML byte a byte.
- **B (rejeitada por ora)**: manter os XMLs do repositório como estão e adicionar um shim de carregamento — por exemplo, pré-processar o stream antes de entregá-lo a `XMLDecoder` (regex no texto) ou interceptar a resolução de classe — remapeando `br.f1mane.` → `br.flmane.` de forma transparente, tanto para os arquivos do repositório quanto para qualquer save externo de jogador.

**Por que A**: o usuário confirmou explicitamente essa escolha durante a exploração. A reescrita direta é mais simples (sem código de compatibilidade permanente para manter) e mantém os arquivos do repositório como fonte única de verdade sobre o nome de classe atual. O trade-off aceito conscientemente: arquivos fora do repositório (saves de campeonato/box de jogadores reais) não são cobertos por este change e deixam de carregar após o upgrade — ver Risks/Trade-offs.

### D2 — Escopo dos arquivos de circuito: todos, incluindo os com edição em andamento e o fixture de formato antigo

`montreal_mro.xml`/`montreal_mro_meta.xml` estavam com alterações não commitadas (edição ativa no editor de circuitos) no momento da investigação. O usuário confirmou que esses dois arquivos entram no escopo da reescrita mesmo assim — a reescrita do atributo `class` é um find-and-replace textual restrito ao valor do atributo, não uma reformatação do arquivo, então não há conflito com o conteúdo editado pelo usuário (posições de nós, objetos, metadados) nesses arquivos.

`src/test/resources/circuitos/fixture_formato_antigo_mro.xml` também entra, pelo mesmo motivo do D1: se ele embute `class="br.f1mane...."`, um teste que o carrega via `XMLDecoder` quebra após o rename do pacote se o fixture não for atualizado junto.

**Alternativa considerada**: excluir os `.bak` de circuito (`montecarlo_mro.xml.bak`, `montreal_mro.xml.bak`, `monza_mro.xml.bak`, `nuburgring_mro.xml.bak`) da reescrita, por serem artefatos de backup gerados pelo editor, não carregados por nenhum caminho de código do jogo. **Decisão**: excluir — não são lidos por `CarregadorRecursos` nem por nenhum fluxo de jogo (só existem como histórico local do editor), então reescrevê-los não tem efeito funcional; ficam como estão, desatualizados, sem risco.

### D3 — `readme.md`, `readme-pt.md`, `docs/sdd.md`: incluídos na reescrita mecânica

Esses três arquivos citam `f1mane`/`br.f1mane` em texto solto (não são `openspec/specs/` nem `CLAUDE.md`, que foram os dois únicos excluídos explicitamente pelo usuário). Por consistência com o objetivo do change (nenhuma referência de código/build ao nome antigo sobrevive) e por não se enquadrarem em nenhuma das exclusões enumeradas, entram no escopo da reescrita textual simples (`f1mane` → `flmane`, `br.f1mane` → `br.flmane`), sem necessidade de revisão de conteúdo além da troca de string.

### D4 — Ordem de execução

1. Rename do pacote Java (`src/main/java/br/f1mane` → `br/flmane`, `src/test/java/br/f1mane` → `br/flmane`), com atualização de todo `package`/`import`/referência textual em javadoc.
2. Atualização dos 2 pontos de string literal que o rename de pacote não cobre sozinho: `MainLauncher` (`ProcessBuilder`) e o teste com `Class.forName`.
3. Atualização de `pom.xml`, `web.xml`, `persistence.xml`.
4. Reescrita do atributo `class="br.f1mane...."` em todos os XMLs de circuito (produção + fixture de teste).
5. Atualização de `readme.md`, `readme-pt.md`, `docs/sdd.md`.
6. `mvn test` verde antes de considerar o change concluído — nenhuma mudança de comportamento esperada, então a suíte inteira deve passar sem alteração de asserts (exceto os 2 testes que citam `br.f1mane` como string, que precisam da string atualizada para continuar passando).

Cada passo é independentemente verificável (compila / carrega circuito / suíte passa) antes de avançar ao próximo, mas não há necessidade de PRs separados — é um rename mecânico de baixo risco por natureza, ao contrário de lote1 (que movia lógica de negócio).

## Risks / Trade-offs

- **[Save de campeonato/box de jogador real fora do repositório para de carregar após o upgrade]** → Não mitigado por este change (D1, estratégia A aceita conscientemente pelo usuário). Registrado aqui para que a decisão fique rastreável; se isso se provar um problema real em produção, a estratégia B (shim de compatibilidade) fica disponível como follow-up, seguindo o mesmo padrão já usado por `MigracaoCicloCircuito`/`MigracaoCircuitoMetadados` para outras mudanças de formato.
- **[Rename de pacote perder alguma referência textual não coberta por import/package, ex. javadoc `@see`, comentário, ou string de log]** → Mitigação: `mvn test` verde não garante isso sozinho (comentários não afetam compilação); complementar com uma busca final por `br\.f1mane` e `f1mane` (case-sensitive) em todo `src/` após o rename, restrita às áreas em escopo, para confirmar zero ocorrência residual fora das exclusões conhecidas.
- **[Reescrita do atributo `class` em XML de circuito corromper algum arquivo por engano, ex. um circuito que já tenha migrado parcialmente ou tenha formato inesperado]** → Mitigação: a reescrita é uma substituição textual exata do valor `br.f1mane.` por `br.flmane.` dentro de atributos `class="..."`, sem tocar em mais nada do arquivo; validar com `git diff` que cada arquivo alterado só tem esse tipo de mudança, e carregar pelo menos um circuito de cada família (`_mro.xml` normal, `_mro_meta.xml`, o fixture de formato antigo) após a reescrita para confirmar que ainda desserializa.
- **[5 specs vivas em `openspec/specs/` ficam com texto de requisito desatualizado citando `br.f1mane`]** → Aceito conscientemente (Non-Goals) por decisão explícita do usuário; não é um risco deste change em si, mas fica registrado para que uma iniciativa futura corrija o texto dessas specs.

## Open Questions

(nenhuma pendente — os pontos levantados durante a exploração foram todos resolvidos nas Decisions acima: estratégia de dados persistidos (D1), escopo dos `.bak` (D2) e de `readme`/`docs` (D3) já decididos com o usuário)
