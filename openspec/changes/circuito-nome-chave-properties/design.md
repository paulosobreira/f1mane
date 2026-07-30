## Context

O projeto já resolveu exatamente este problema para o campo `ativo` (ver `circuito-ativo` capability): `ativo` deixou de ser persistido no XML e passou a viver só em `circuitos.properties` (`<arquivoXml>=<NomeExibicao>,<ativo>`), com um par de funções `circuitoAtivo`/`atualizarAtivoEmCircuitosProperties` (via classpath, usado pelo jogo/jar) e `lerAtivoDaFonte`/`atualizarAtivoEmCircuitosProperties(File,...)` (direto no arquivo-fonte `src/main/resources/...`, usado pelo editor, que roda a partir do source, não do jar).

`nome` está no mesmo lugar que `ativo` estava antes dessa migração: campo Java Bean em `Circuito` (`Circuito.java:57`), gravado em `<nome>_mro_meta.xml` via `copiaParaArquivoMetadados()` (`Circuito.java:539`), enquanto o nome de exibição real (usado no menu, HUD, servidor) já vem de `circuitos.properties` via `CarregadorRecursos.carregarCircuitosDefaults()` (`CarregadorRecursos.java:1274-1285`). O editor (`MainPanelEditor.nomePistaText`) só grava no XML, nunca no properties — os dois valores podem divergir.

Há também um precedente direto para "não persistir no XML apesar de ter getter/setter": o campo `backGround` usa um setter fora do padrão JavaBean (`definirBackGroundPorConvencao`, não `setBackGround`) especificamente para que o `XMLEncoder` não o trate como propriedade persistente (`Circuito.java:516-521`). `XMLEncoder`/`XMLDecoder` (java.beans) só persiste uma propriedade se existir o par getter+setter no padrão `getX`/`setX` — renomear o setter é a forma estabelecida no código de "esconder" um campo do XML sem tirar o getter.

## Goals / Non-Goals

**Goals:**
- `nome` deixa de ser gravado em `_mro_meta.xml`; passa a ser resolvido a partir de `circuitos.properties` (mesmo padrão já usado para `ativo`).
- Editor: salvar o nome grava em `circuitos.properties` (não mais no XML) e recarrega o valor do arquivo-fonte imediatamente após escrever, mesmo padrão que já existe para `ativo` (`lerAtivoDaFonte`, chamado por causa do cache de classpath desatualizado).
- Remover completamente `Util.substVogais` (flag, método, todos call sites) e a flag de linha de comando `"real"` que o controlava.

**Non-Goals:**
- Não reescrever em massa os XMLs de metadados existentes para apagar o `<nome>` órfão — o decoder simplesmente vai ignorá-lo (nenhum getter/setter bean casa com essa propriedade depois da mudança, `XMLDecoder` ignora propriedades sem setter correspondente).
- Não mudar o formato/CSV de `circuitos.properties` (`<arquivo>=<NomeExibicao>,<ativo>`) — o nome de exibição já é o primeiro campo, só passa a ser gravável pelo editor.
- Não criar automaticamente uma linha nova em `circuitos.properties` para circuito ainda não cadastrado (mesma decisão já tomada para `ativo`, ver comentário em `atualizarAtivoEmCircuitosProperties`).

## Decisions

- **Renomear o setter de `nome` para fora do padrão JavaBean, igual `backGround`.**
  Alternativa considerada: usar `@JsonIgnore`/anotação. Rejeitada — o projeto persiste `Circuito` via `java.beans.XMLEncoder`/`XMLDecoder`, que não olha para anotações Jackson (são vestígio no topo da classe, não usadas aqui); a única forma de excluir uma propriedade da serialização bean é quebrar o par getter/setter convencional. Renomeio o setter para `definirNomePorConvencao(String)` (nome simétrico ao já existente `definirBackGroundPorConvencao`), mantendo `getNome()` como está. Todos os call sites de `circuito.setNome(...)` (Circuito.java, MainPanelEditor.java e quaisquer outros) precisam ser atualizados para o novo nome de método.

- **`copiaParaArquivoMetadados()` para de copiar `nome`.**
  Remove a linha `copia.setNome(nome)` (linha 539) — sem efeito depois da mudança de nome do setter (nem compilaria como propriedade bean), e não há razão para manter o valor na cópia intermediária.

- **Resolução do nome de exibição: mesmo par de funções de `ativo`, espelhado para `nome`.**
  `CarregadorRecursos` ganha `nomeExibicaoCircuito(String nmCircuito)` (equivalente a `circuitoAtivo`, lê via classpath, usado pelo jogo/jar) e `lerNomeDaFonte(String nmCircuitoXml)` (equivalente a `lerAtivoDaFonte`, lê direto do arquivo-fonte, usado pelo editor). `carregarCircuito()` passa a popular `circuito.definirNomePorConvencao(nomeExibicaoCircuito(nmCircuito))` do mesmo jeito que já faz `circuito.setAtivo(circuitoAtivo(nmCircuito))` (`CarregadorRecursos.java:1371`).

- **Escrita: estender a rotina de reescrita de `circuitos.properties` para cobrir também o nome, não duplicar a leitura/escrita linha-a-linha.**
  Alternativa considerada: função `atualizarNomeEmCircuitosProperties` totalmente separada de `atualizarAtivoEmCircuitosProperties`, cada uma reescrevendo o arquivo independentemente. Rejeitada — o editor salva os dois valores (nome e ativo) no mesmo fluxo (`salvarPista()`), então duas reescritas sequenciais do mesmo arquivo é trabalho duplicado e dobra o risco de condição de corrida/diff ruidoso. Em vez disso, generalizar a função de escrita existente para aceitar os dois campos de uma vez: `atualizarCircuitosProperties(File arquivo, String nmCircuitoXml, String nomeExibicao, boolean ativo)`, reescrevendo a linha `nmCircuitoXml=nomeExibicao,ativo` inteira numa única passada. Os métodos públicos `atualizarAtivoEmCircuitosProperties(String, boolean)` continuam existindo como wrapper de compatibilidade (lêem o nome atual da linha antes de regravar, preservando-o) para não quebrar chamadores existentes que só mexem em `ativo`; um novo `atualizarNomeEmCircuitosProperties(String nmCircuitoXml, String nomeExibicao)` faz o mesmo pelo lado do nome, preservando `ativo`.

- **Editor: após escrever, recarregar do arquivo-fonte (não só confiar no valor que acabou de escrever).**
  Mesmo raciocínio documentado em `lerAtivoDaFonte`: o editor lê recursos via caminho relativo ao projeto (`src/main/resources/...`), não via classpath (que só atualiza em rebuild) — então depois de escrever em `circuitos.properties`, o editor deve reler o arquivo-fonte (`lerNomeDaFonte`) e atualizar `nomePistaText`/`circuito` com o valor relido, garantindo que a UI reflita exatamente o que foi persistido (e não uma suposição otimista do que foi digitado).

- **Remoção de `Util.substVogais`: apagar, não só desativar por default.**
  Alternativa considerada: só trocar o default da flag `substVogais` de `true` para `false`. Rejeitada — pedido explícito do usuário é remover o recurso, não escondê-lo atrás de uma flag que continuaria existindo como código morto. Remove o campo `Util.substVogais` (`Util.java:27`), o método `Util.substVogais(String)` (`Util.java:344-368`), e troca todos os call sites para usar o nome puro diretamente (`nome` em vez de `Util.substVogais(nome)`). Remove também o parse do argumento `"real"` em `MainFrame.java:441-447`, que não tem mais função sem a feature.

## Risks / Trade-offs

- [Renomear `setNome`→`definirNomePorConvencao` é breaking para qualquer código externo ao repo que dependa do bean `Circuito` via reflection/nome de método] → Não há consumidores externos conhecidos (é uma classe de domínio interna); grep completo por `.setNome(` antes de remover garante que nenhum call site interno quebre silenciosamente.
- [XMLs de metadados existentes continuam com `<nome>` gravado (órfão) — alguém lendo o XML manualmente pode se confundir achando que aquele valor é a fonte da verdade] → Aceito como Non-Goal; documentar no changelog/commit que o campo é ignorado na leitura.
- [Circuito novo, ainda sem linha em `circuitos.properties`, fica sem nome de exibição (`nomeExibicaoCircuito` retorna null/vazio)] → Mesma limitação já aceita para `ativo` (retorna `false` por ausência de linha); tratar como Open Question abaixo, não bloqueia o core da mudança.
- [Remover `substVogais` muda texto visível em menu/HUD/servidor para todo circuito, todo save existente] → É exatamente o efeito pedido pelo usuário; não é regressão, é o objetivo.

## Migration Plan

1. Renomear setter em `Circuito.java`, remover `copia.setNome(nome)` de `copiaParaArquivoMetadados()`.
2. Adicionar `nomeExibicaoCircuito`/`lerNomeDaFonte`/`atualizarNomeEmCircuitosProperties` em `CarregadorRecursos.java`, generalizando a escrita linha-a-linha existente.
3. `CarregadorRecursos.carregarCircuito()` passa a popular `nome` a partir de `circuitos.properties`, não do XML.
4. Editor (`MainPanelEditor.salvarPista()`/`sincronizarCamposNoCircuito`): trocar `circuito.setNome(...)` por `circuito.definirNomePorConvencao(...)`; ao salvar, chamar `atualizarNomeEmCircuitosProperties` e depois `lerNomeDaFonte` para recarregar `nomePistaText`.
5. Remover `Util.substVogais` e todos os 8 call sites listados no proposal; remover parse de `"real"` em `MainFrame.java`.
6. Rodar `mvn test` (cobertura existente de `circuito-metadados-arquivo`, `circuito-info-editor`, `circuito-ativo` deve ser ajustada/estendida) e `mvn clean package -Ph2 -DskipTests` para atualizar o jar.
7. Rollback: reverter o commit único da mudança — XMLs antigos continuam funcionando (campo órfão ignorado), `circuitos.properties` não muda de formato.

## Open Questions

- Circuito novo sem linha em `circuitos.properties`: qual nome de exibição usar como fallback (nome do arquivo sem extensão? string vazia?)? Mesma lacuna que já existe para `ativo` — decidir no momento da implementação, sem bloquear o restante da mudança.
