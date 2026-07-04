## Context

`ObjetoGuardRails` (`src/main/java/br/f1mane/entidades/ObjetoGuardRails.java`) é criado ponto a ponto no editor (clique esquerdo adiciona ponto, clique direito finaliza), e a lista `pontos` (`List<Point>`) vira `caminho` (`GeneralPath`) só quando `gerar()` é chamado explicitamente ao finalizar. `ObjetoLivre` já resolve um problema parecido de edição pós-criação com um modo dedicado no editor: o campo `MainPanelEditor.editandoPontosDe` (tipado `ObjetoLivre`) guarda qual objeto está em edição, e `tentarIniciarArrasteVerticeOuHaste`/os handlers de mouse em `MainPanelEditor` arrastam vértices (`PontoCurva`) e suas hastes. Guard rails não tem hastes — os segmentos são sempre retos — então a edição de pontos aqui é mais simples que a de `ObjetoLivre`.

A espessura da linha fina (`LARGURA_LINHA`) e o vão (`VAO_ENTRE_LINHAS`) são hoje `static final int` fixos em 1, usados por `construirLinhas()` para calcular quantas linhas cabem no comprimento total do encadeamento.

O circuito expõe nós de pista via `Circuito.getNos()`/`getNosBox()` (lista de `No`, cada um com `Point` de posição) e objetos de cenário via `Circuito.getObjetosCenario()`.

## Goals / Non-Goals

**Goals:**
- Permitir mover, inserir e remover pontos de um `ObjetoGuardRails` já existente, no editor.
- Tornar `larguraLinha`/`vaoEntreLinhas` propriedades editáveis por objeto, com o valor 1 preservado como default de compatibilidade.
- Adicionar snap de pontos (criação e arraste) a nós de pista e a pontos de outros objetos de cenário, dentro de uma tolerância em pixels de tela.

**Non-Goals:**
- Não adiciona curvatura (hastes) a `ObjetoGuardRails` — segmentos continuam sempre retos.
- Não altera o fluxo de criação inicial ponto a ponto (clique esquerdo adiciona, direito finaliza) — a edição pós-criação é um modo separado, ativado explicitamente.
- Não adiciona snap para outros tipos de objeto (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoPneus`, `ObjetoLivre`) — escopo restrito a `ObjetoGuardRails`.
- Não muda a forma como `ObjetoLivre` edita seus próprios pontos/hastes.

## Decisions

**Estado de edição de pontos como campo separado, não reaproveitando `editandoPontosDe`.** `editandoPontosDe` é tipado `ObjetoLivre` e o código de arraste manipula `PontoCurva`/hastes diretamente. Em vez de generalizar esse campo para um tipo comum (que exigiria uma interface nova compartilhada entre `ObjetoLivre` e `ObjetoGuardRails` só para o editor), adiciona-se um campo irmão `editandoPontosGuardRailsDe` (tipo `ObjetoGuardRails`) em `MainPanelEditor`, com sua própria lógica de arraste/inserção/remoção sobre `Point` puro. Alternativa considerada: introduzir uma interface `ObjetoComPontosEditaveis` — descartada por ora porque só há dois tipos e a lógica de haste de um não se aplica ao outro; reavaliar se um terceiro tipo precisar do mesmo modo.

**Inserção de ponto via clique sobre o segmento, não um botão separado.** Ao estar em modo de edição de pontos de um `ObjetoGuardRails`, um clique esquerdo suficientemente próximo de um segmento (mas não de um ponto existente) insere um novo ponto ali, mesma UX de "clicar para adicionar" já familiar do fluxo de criação. Remoção usa o botão direito sobre um ponto existente (paralelo ao "clique direito finaliza" do fluxo de criação, mas em modo de edição o direito remove em vez de finalizar).

**`larguraLinha`/`vaoEntreLinhas` viram campos de instância com default 1 no construtor.** Isso preserva a aparência de circuitos XML antigos (que não têm esses valores gravados) sem precisar de lógica de migração: `XMLDecoder` simplesmente não chama o setter e o campo mantém o valor do construtor.

**Snap consulta nós e objetos a cada movimento do mouse, sem estrutura de indexação espacial.** O número de nós por circuito e de objetos de cenário é pequeno (dezenas a poucas centenas), então uma varredura linear a cada evento de mouse é suficiente; não se justifica uma grade espacial ou k-d tree para este volume de dados.

## Risks / Trade-offs

- [Campo `editandoPontosGuardRailsDe` duplica parte da estrutura de `editandoPontosDe`] → Aceitável dado o non-goal de curvatura; se um terceiro objeto precisar de edição de pontos, extrair a interface comum nesse momento.
- [Snap por varredura linear em circuitos muito grandes pode custar por frame de arraste] → Mitigado pelo volume tipicamente pequeno de nós/objetos por circuito; se necessário no futuro, cachear a lista de candidatos ao entrar em modo de edição em vez de por evento de mouse.
- [Remover um ponto de um guard rails com exatamente 2 pontos deixaria o objeto sem segmento] → Mitigado pelo requisito explícito de bloquear essa remoção (ver spec `guard-rails-point-editing`).
