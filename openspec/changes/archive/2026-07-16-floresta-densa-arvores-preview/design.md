## Context

- `ObjetoLivre.desenha()` (`src/main/java/br/f1mane/entidades/ObjetoLivre.java`) sempre preenche a forma inteira com `corPimaria` (fundo) e, para tipos não sólidos, chama um método de padrão específico dentro de `desenhaComClipSemAntialiasing` — que aplica o clip pela silhueta fixa, desliga antialiasing, e rotaciona só o `Graphics2D` (não a silhueta) pelo `angulo` do objeto antes de rodar a lambda de desenho.
- `VEGETACAO_DENSA` hoje usa `desenhaPadraoVegetacao` com dispersão pseudo-aleatória (grade com jitter, semente fixa `SEMENTE_VEGETACAO`), sorteando por touceira: posição, um fator de tamanho (`fatorTamanho`, ±60%), uma ampliação extra (2x–3x, `AMPLIACAO_MIN/MAX_TOPO_ARVORE`), uma variante de silhueta em estrela (`formaTopoArvore`, 4 variantes) e um ângulo base. Duas salvaguardas já existem e serão reaproveitadas: `sobrepoeTouceiraAceita` (descarta touceira que invadiria o círculo de uma já aceita) e `cabeInteiraNaSilhueta` (descarta touceira cuja silhueta, já rotacionada pelo conteúdo, não caiba inteira dentro da área do objeto — ou seja, já é proibido desenhar uma touceira "cortada" pela borda; este pedido do usuário já é uma regra existente, não uma nova).
- O usuário indicou `kenney_foliage-pack/` (pasta trazida para o projeto, CC0, não referenciada em nenhum código) como referência visual: árvores vistas de LADO, cada uma com tronco fino visível e uma copa de silhueta bem distinta (triangular/conífera, arredondada, pinheiro em camadas com neve, lobulada tipo arbusto). O precedente do projeto (`openspec/changes/archive/2026-07-15-vegetacao-densa-topo-arvore`) já estabeleceu o padrão: usar a referência só para orientar as PROPORÇÕES/estilo de um `GeneralPath` gerado em código, nunca carregar o arquivo de asset em tempo de execução.
- O editor (`MainPanelEditor.java`) já separa claramente controles persistidos por circuito (linha1Painel: nome, ciclo, `ativoCheckBox`, cores, etc.) de controles de sessão/teste não persistidos (linha2Painel: `gerarBotoesVisualizacao()` = TRACADO/mostrarBackground/mostrarObjetosDesenho, e `gerarBotoesTestePista()` = Testar Pista/Testar Ir ao Box/Testar Escapada), montados num `JPanel topo` com `GridLayout(3, 1)` (`gerarLayout`, por volta da linha 1289).
- Flags estáticas globais de comportamento de renderização/gameplay já existem em `br.nnpe.Global` (`DEBUG`, `MODO_HOMENAGEM`, `DESENHA_DIFF_REAL_SUAVE`), lidas diretamente pelo código de desenho sem passar por parâmetro de método — é o mecanismo natural para uma preferência de exibição do editor que precisa ser lida de dentro de `ObjetoLivre.desenha(Graphics2D, double)`, cuja assinatura (compartilhada com o runtime de corrida via `ObjetoDesenho`/`ObjetoPista`) não deve mudar.

## Goals / Non-Goals

**Goals:**
- Substituir a silhueta de topo de árvore de `VEGETACAO_DENSA` por árvores vistas de lado (tronco em `corPimaria`, copa em `corSecundaria`), com um pequeno conjunto de modelos de MESMO tamanho entre si (sem variação de escala), inspirados no estilo do `kenney_foliage-pack` sem carregar nenhum arquivo do pacote.
- Tornar o fundo de `VEGETACAO_DENSA` transparente (sem fill sólido de `corPimaria` cobrindo a forma toda) — só as árvores individuais aparecem.
- Preservar os invariantes já testados de determinismo, anti-sobreposição e não-corte-na-borda, adaptando-os à nova silhueta (tronco+copa) em vez da estrela.
- Adicionar um checkbox "Padrão" no editor, não persistido por circuito, que alterna todo padrão não sólido de `ObjetoLivre` entre preenchimento completo e uma única marca centralizada — reduzindo custo de redesenho por frame durante a edição.
- Garantir que a marca central do modo "preview" também não apareça cortada: se não couber inteira, nada é desenhado.

**Non-Goals:**
- Não carrega, rasteriza nem referencia em tempo de execução nenhum arquivo de `kenney_foliage-pack/` (PNG/SVG/XML) — usado só como inspiração visual durante a implementação, mesmo espírito da mudança anterior de vegetação densa.
- Não altera `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`, `LISTRADO`, `XADREZ` além de ganharem o modo de marca única (preview) — sua lógica de padrão em si (grade/dispersão) não muda.
- Não adiciona campos novos ao formulário de `ObjetoLivre` nem ao XML de circuito — reaproveita `corPimaria`/`corSecundaria` já existentes, e o checkbox "Padrão" não é um campo do objeto nem do circuito.
- Não introduz índice espacial nem cache de imagem pré-renderizada para o modo de preview — a redução de custo vem só de pular a maior parte das iterações do laço de grade/dispersão.

## Decisions

### D1 — Árvore lateral como `GeneralPath` combinando tronco (retângulo/trapézio) + copa (forma por variante)
Novo método `formaArvoreLateral(cx, cyBase, variante)` retorna duas formas (tronco e copa) já posicionadas com a base do tronco em `(cx, cyBase)`, todas com as MESMAS dimensões fixas (constantes `LARGURA_TRONCO`/`ALTURA_TRONCO`/`RAIO_COPA` derivadas de `PASSO_PADRAO_LOCAL`, sem `fatorTamanho` nem ampliação aleatória) — só a variante da copa muda:
- Variante 0 (conífera): copa triangular (um `GeneralPath` de 3 pontos) centrada acima do tronco.
- Variante 1 (arredondada): copa como `Ellipse2D`/círculo.
- Variante 2 (pinheiro em camadas): 2–3 triângulos empilhados de largura decrescente, mesmo espírito do pinheiro nevado do pack (sem desenhar a neve — só a silhueta, preenchida com `corSecundaria`).
- Variante 3 (lobulada): 3 círculos sobrepostos (cluster), como um arbusto largo.

Tronco: um retângulo estreito (ou trapézio levemente mais largo na base) da mesma proporção para as 4 variantes, preenchido com `corPimaria`; copa preenchida com `corSecundaria`, desenhada por cima do tronco (a base da copa cobre o topo do tronco, escondendo a emenda).

Alternativa descartada: reaproveitar `formaTopoArvore` (estrela) ajustando proporções. Rejeitada porque uma silhueta em estrela não lê como "árvore de lado" — o pack de referência deixa claro que a legibilidade lateral vem de duas formas empilhadas (tronco fino + copa larga), não de uma única silhueta simétrica.

### D2 — Tamanho fixo entre árvores (remove `fatorTamanho`/ampliação); densidade e anti-sobreposição continuam por raio de copa constante
`VARIACAO_TAMANHO_VEGETACAO_DENSA` e `AMPLIACAO_MIN/MAX_TOPO_ARVORE` são removidas (deixam de se aplicar à densa). O sorteio por touceira passa a escolher só: posição (jitter dentro da célula, como hoje) e `variante` (0–3) — nada de escala. `sobrepoeTouceiraAceita` passa a usar `RAIO_COPA` fixo em vez de um raio por touceira. Isso é consistente com o pedido do usuário ("sem variação de tamanho, já que é uma vista lateral") e simplifica o sorteio (menos números consumidos do `Random`, mas ainda determinístico pela mesma semente).

Alternativa descartada: manter uma pequena variação de tamanho "para naturalidade". Rejeitada porque o usuário foi explícito quanto a isso, e uma vista lateral não tem a mesma justificativa de profundidade/perspectiva que motivou a variação na vista de topo.

### D3 — `corPimaria`/`corSecundaria` mudam de papel só para `VEGETACAO_DENSA`; fundo deixa de ser preenchido
Em `desenha()`, o fill de fundo (`g2d.fill(formaZoomLocal)` com `corPimaria`) passa a ser condicional: só roda quando `tipo != VEGETACAO_DENSA`. Dentro de `desenhaPadraoVegetacao` (ramo `densa`), cada árvore desenha o tronco com `corPimaria` e a copa com `corSecundaria`, diretamente (sem um fill de fundo prévio) — o resultado é transparência em toda a área do objeto livre exceto onde uma árvore é desenhada.

Alternativa descartada: adicionar um terceiro campo de cor "cor de fundo" separado, mantendo `corPimaria`/`corSecundaria` com o papel atual. Rejeitada porque o usuário pediu explicitamente 2 cores (caule/copa) e fundo transparente — um terceiro campo seria um campo a mais no formulário sem uso pedido, e mudaria o formato persistido do circuito sem necessidade.

Efeito aceito como **BREAKING** (documentado no proposal): circuitos existentes com `ObjetoLivre` do tipo `VEGETACAO_DENSA` mudam de aparência — a cor que antes era "fundo sólido" (`corPimaria`) passa a colorir só os troncos, e a área deixa de ter fundo opaco.

### D4 — `cabeInteiraNaSilhueta` estendido para a forma combinada tronco+copa
A verificação de "não desenhar se cortado pela borda" já existe (`cabeInteiraNaSilhueta`, comparando a `Area` da candidata rotacionada contra a área da silhueta do objeto). Para a árvore lateral, a candidata testada passa a ser a UNIÃO (`Area`) do tronco com a copa (não só a copa isoladamente) — se qualquer parte da árvore (tronco ou copa) ficaria fora da forma do objeto livre, a árvore inteira é descartada, mantendo a regra pedida ("o padrão só pode ser desenhado se coberto dentro da área de forma integral") sem exceção para uma das duas partes.

### D5 — Modo de preview "marca única centralizada" via flag estática em `Global`, aplicado a todos os padrões não sólidos
Nova constante mutável `Global.padraoObjetoLivreCompleto` (`boolean`, default `true`, mesmo padrão de `Global.DEBUG`/`MODO_HOMENAGEM`) — não é um `final`, é alternada em runtime pelo checkbox do editor. Cada método de padrão (`desenhaPadraoEmGrade`, `desenhaPadraoVegetacao`, `desenhaBrita`, `desenhaPadraoListrado`, `desenhaPadraoXadrez`) passa a checar essa flag no início do laço:
- Se `true` (padrão/comportamento atual): laço completo sobre `areaCoberturaPadrao(formaLocal)`, como hoje.
- Se `false`: em vez do laço, calcula o centro de `formaLocal.getBounds()` e desenha UMA única instância da primitiva do padrão ali (uma touceira/árvore, uma listra, uma célula de xadrez, etc.), sem consumir o `Random` da dispersão (mantém o padrão determinístico e barato).

Para `VEGETACAO_DENSA` especificamente, a marca única também passa por `cabeInteiraNaSilhueta` (D4): se a árvore centralizada não couber inteira, nada é desenhado nesse modo — coerente com a regra de não cortar, mesmo no modo barato.

Alternativa descartada: passar um parâmetro `boolean modoPreview` através de `ObjetoPista.desenha(Graphics2D, double)`. Rejeitada porque essa assinatura é compartilhada com todo o runtime de corrida (`PainelCircuito`, servidor) e mudar a interface de `ObjetoDesenho` para uma preferência exclusiva do editor é um acoplamento desnecessário; a flag estática (mesmo mecanismo já usado por `Global.DEBUG`) resolve sem tocar na interface.

Alternativa descartada: cache de imagem renderizada (desenhar uma vez em um `BufferedImage` e reusar). Rejeitada por ser mais complexa (invalidação ao editar posição/cor/ângulo) para um ganho que a marca única já entrega de forma mais simples.

### D6 — Checkbox "Padrão" como nova linha do painel superior do editor, não persistida
`gerarLayout()` ganha uma nova `linha3Painel`, com um `JCheckBox` único (`Lang.msg("padraoCompletoCheck")`) inicializado com `Global.padraoObjetoLivreCompleto` e cujo listener só faz `Global.padraoObjetoLivreCompleto = check.isSelected(); repaint();` — mesmo padrão dos outros checkboxes de `gerarBotoesVisualizacao()` (não lê nem grava em `circuito`). O `JPanel topo` passa de `GridLayout(3, 1)` para `GridLayout(4, 1)`, adicionando `linha3Painel` depois de `linha2Painel` (que já contém o checkbox de Testar Escapada) — satisfazendo o pedido de "terceira linha depois do checkbox de escapada" como a próxima linha do mesmo bloco de controles de sessão.

## Risks / Trade-offs

- [Circuitos existentes com `VEGETACAO_DENSA` mudam de aparência sem migração — `corPimaria` deixa de ser "fundo" e passa a ser "tronco"] → Aceito e documentado como BREAKING no proposal, no mesmo espírito da mudança anterior de vegetação densa (sem migração de dados, só de renderização); usuário pode reajustar as duas cores no editor se o resultado não agradar.
- [Modo de preview (marca única) escondendo a densidade real pode fazer o usuário esquecer de reativar antes de julgar o visual final] → Mitigado por ser sempre visível como um checkbox marcado/desmarcado no próprio editor (estado óbvio), e por ter valor padrão `true` (completo) a cada abertura do editor.
- [Flag estática em `Global` é global ao processo, não por-objeto nem por-painel] → Aceitável: é uma preferência de sessão de edição, não um dado de circuito; o mesmo padrão já é usado por `Global.DEBUG` para overlays visuais.
- [Árvore combinada tronco+copa como duas formas separadas (em vez de um único `GeneralPath`) exige unir numa `Area` só para o teste de contenção em D4] → Custo aceitável (mesma abordagem de `Area`/`subtract` já usada por `cabeInteiraNaSilhueta`), e evita complicar o desenho em si (dois `g2d.fill()` simples, cada um com sua cor, é mais direto que um path único com "furo").

## Migration Plan

1. D1 + D2 (nova silhueta de árvore lateral, tamanho fixo) isolados em `ObjetoLivre.formaArvoreLateral`/`desenhaPadraoVegetacao`, sem tocar nos demais tipos.
2. D3 (fundo transparente + remapeamento de cor) em `desenha()`, condicional só a `VEGETACAO_DENSA`.
3. D4 (contenção da forma combinada) — ajuste pontual em `cabeInteiraNaSilhueta`/chamada.
4. D5 (flag de preview + modo marca única nos 5 métodos de padrão) em `Global` + `ObjetoLivre`.
5. D6 (checkbox no editor) em `MainPanelEditor.gerarLayout`/`gerarBotoesVisualizacao` (ou novo método `gerarLinhaPadraoPreview`), por último — depende de D5 já existir.

Sem migração de dados de circuito; rollback via `git revert` (mudança de renderização/editor, não de runtime de servidor nem de formato XML).

## Open Questions

- Se a "marca única centralizada" do modo preview deve ignorar a rotação do padrão (`angulo`) ou continuar rotacionando junto — assumido aqui que continua rotacionando (mesmo pivô/transform de sempre), já que é só uma redução de QUANTIDADE de marcas, não do comportamento de rotação.
- Se o checkbox "Padrão" deve ter algum indicador visual adicional (ex.: tooltip explicando o motivo de performance) — assumido que o texto do label (`Lang.msg("padraoCompletoCheck")`) já é suficiente, sem necessidade de tooltip.
