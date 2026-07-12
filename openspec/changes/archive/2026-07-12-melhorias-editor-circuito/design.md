## Context

Quatro mudanças independentes no editor de circuitos (`br.f1mane.editor.MainPanelEditor`) e na renderização (`br.f1mane.visao.PainelCircuito`), levantadas pelo usuário após uso do editor:

1. `iniciarComNavegacao()` hoje seleciona e carrega o primeiro circuito de `circuitosXml` (índice 0) automaticamente ao abrir o editor.
2. `salvarPista()` não valida se `ObjetoEscapada`s no circuito têm `indiceSaida` definido (`!= -1`) antes de gravar.
3. `salvarPista()` faz `Integer.parseInt(distanciaKmText.getText().trim())` sem checar antes se o campo está vazio ou é zero — hoje isso ou lança `NumberFormatException` (capturada só pelo `dialogDeErro` genérico) ou aceita silenciosamente `0`.
4. `PainelCircuito.desenhaPistaBox()` desenha o traçado do box com um stroke cinza fino (`box`, 40% da largura da pista), sem nenhuma borda branca, direto por cima do que `desenhaPista()`/`desenhaTintaPistaZebra()` já pintaram — sem nenhuma detecção de sobreposição com o traçado da pista.

As mudanças 1–3 são bloqueios de UI/validação relativamente diretos. A mudança 4 é a única com ambiguidade técnica real (como detectar e recortar a interseção geometricamente), por isso este design foca principalmente nela.

## Goals / Non-Goals

**Goals:**
- Editor abre em estado neutro (nenhum circuito carregado), exigindo escolha explícita do usuário.
- Impedir salvar um circuito com escapada incompleta ou sem `distanciaKm` informada, com alerta claro seguindo o padrão já estabelecido (`alertaPontoEscapadaInvalido()` / `MainPanelEditorTestDouble`).
- Traçado do box ganha uma borda branca visível, mas essa borda SHALL NOT aparecer nos trechos onde o traçado do box se sobrepõe geometricamente ao traçado da pista (para não desenhar branco por cima do asfalto/zebra da pista).

**Non-Goals:**
- Não recalcula nem valida a geometria do traçado do box em si (posição dos nós de box) — só o tratamento visual da borda.
- Não adiciona validação de completude a outros tipos de `ObjetoPista` (guard rails, construções) — escopo é só `ObjetoEscapada`.
- Não migra circuitos já salvos com escapada incompleta ou `distanciaKm` zerada — a validação vale só para o próximo `salvarPista()`, não retroage.
- Não muda a largura/cor do traçado do box em si (`Color.LIGHT_GRAY`, stroke `box`), só adiciona uma camada de borda branca.

## Decisions

### 1. Combo box sem circuito pré-carregado no início
`iniciarComNavegacao()` deixa de chamar `carregarCircuitoExistente(circuitosXml.get(0))` incondicionalmente. Em vez disso, sempre entra no mesmo ramo hoje usado só para lista vazia (`indiceCircuito = -1`, sem carregar circuito), e `repopularComboCircuitos()`/`atualizarBotoesNavegacao()` passam a suportar `indiceCircuito == -1` mesmo com `circuitosXml` não vazio, exibindo um item "nenhum circuito carregado" (ou combo sem seleção) em vez de forçar `setSelectedIndex(0)`.

- **Alternativa considerada**: manter o auto-load mas exibir um diálogo de confirmação antes. Rejeitada — o usuário pediu explicitamente que nada carregue, não uma confirmação extra.

### 2. Validação de escapada incompleta bloqueia salvar
Novo método `protected boolean existeEscapadaIncompleta()` (ou verificação inline) em `MainPanelEditor` que itera `circuito.getObjetos()`, filtra `instanceof ObjetoEscapada`, e checa `((ObjetoEscapada) o).getIndiceSaida() == -1`. Chamado no início de `salvarPista()`, antes do `JFileChooser`/`vetorizarCircuito()`. Em caso positivo, chama um novo método `protected void alertaEscapadaIncompleta()` (JOptionPane via `Lang.msg(...)`, nova chave de idioma) e retorna sem salvar — mesmo padrão de `alertaPontoEscapadaInvalido()`, com override correspondente em `MainPanelEditorTestDouble`.

- **Alternativa considerada**: validar dentro de `ObjetoEscapada`/`Circuito` (ex.: `Circuito.vetorizarPista()`). Rejeitada — o javadoc de `ObjetoEscapada` (linhas 29-30) já deixa explícito que validação vive no editor, e `vetorizarCircuito()` é chamado também fora do fluxo de salvar (ex. ao carregar), onde bloquear abortaria o carregamento de um circuito existente incompleto em vez de só impedir salvá-lo.

### 3. Validação de distância/quilometragem bloqueia salvar
Antes de `Integer.parseInt(distanciaKmText.getText().trim())` em `salvarPista()`, checar se o texto está vazio (`isEmpty()`) ou se o valor parseado é `<= 0`. Se sim, chamar novo método `protected void alertaDistanciaNaoInformada()` (mesmo padrão) e abortar o salvamento antes de qualquer parse ou escrita em arquivo.

- **Alternativa considerada**: permitir `0` mas validar só campo vazio. Rejeitada — `Piloto.calculoVelocidadeReal()` já trata `distanciaKm == 0` como "não informado" (fallback), então `0` deve ser bloqueado no salvar pelo mesmo motivo, não só string vazia.

### 4. Borda branca do box com interseção subtraída via `Area`
Reaproveita o padrão já usado em `ObjetoConstrucao.java:266-269` (`new Area(forma)` + `area.subtract(new Area(outraForma))`), que é o idiom padrão do projeto para "desenhar A menos B" com Java2D.

Implementação em `PainelCircuito`:
1. Construir `Shape formaPista` = `pista.createStrokedShape(caminhoPista)` usando o mesmo `GeneralPath` e `Stroke` (`pistaTinta`/`pista`, o mais largo dos dois) já usados por `desenhaPista()`/`desenhaTintaPistaZebra()` — ou seja, a área que a pista efetivamente pinta, incluindo a borda branca dela.
2. Construir `Shape formaBordaBox` = um `Stroke` de borda (largura do `box` stroke + uma margem fixa de borda, ex. `Util.inteiro(larguraPistaPixeis * .4) + margemBorda`) aplicado ao mesmo caminho de nós usado por `desenhaPistaBox()` (`circuito.getBoxKey()`), via `strokeBorda.createStrokedShape(caminhoBox)`.
3. `Area areaBorda = new Area(formaBordaBox); areaBorda.subtract(new Area(formaPista));` — remove da borda branca do box qualquer trecho que caia dentro da área já pintada pela pista.
4. Novo método `desenhaTintaPistaBox(Graphics2D g2d)`, chamado em `desenhaBackGroundComStrokes()`/`desenhaCircuito()` **antes** de `desenhaPistaBox()` (mesma ordem relativa que `desenhaTintaPistaZebra()` → `desenhaPista()`): pinta `areaBorda` em `Color.WHITE` via `g2d.fill(areaBorda)`. Em seguida `desenhaPistaBox()` continua desenhando a linha cinza central por cima, sem alteração.

**Duplicação necessária em `DesenhoProceduralCircuito`**: descoberto durante verificação manual que `PainelCircuito` (acima) NÃO é o que renderiza a tela ao vivo do editor — `MainPanelEditor.paintComponent()` chama `DesenhoProceduralCircuito.desenhaPistaZebraEBox()`/`desenhaPistaBox()`, uma implementação estática separada e independente (mesma forma de desenho, mas sem compartilhar código com `PainelCircuito` — nem `descontoCentraliza`/viewport, que só existe no editor via scroll). A mesma técnica (`construirCaminhoFechado`/`boxBorda`/`Area.subtract`/`desenhaTintaPistaBox`) foi duplicada ali, chamada em `desenhaPistaZebraEBox()` antes de `desenhaPistaBox()`. As duas cópias divergem apenas na origem das coordenadas (`PainelCircuito` desconta `descontoCentraliza` para o viewport com scroll; `DesenhoProceduralCircuito` não, pois desenha em espaço absoluto de imagem). Não foi extraído um helper compartilhado entre as duas classes porque `PainelCircuito` já duplicava esse mesmo padrão de pista/box/zebra de `DesenhoProceduralCircuito` antes desta mudança (constatado pelas duas implementações de `desenhaPista`/`desenhaPistaBox`/`desenhaTintaPistaZebra` já existentes, uma em cada classe, sem nenhum código compartilhado) — extrair um helper comum seria uma refatoração maior, fora do escopo deste change.

**Regressão descoberta e corrigida**: `DesenhoProceduralCircuito.desenhaPista()` e `desenhaTintaPistaEZebra()` assumiam `circuito.getPistaKey()` não-vazio (`circuito.getPistaKey().get(0)` sem guarda) — nunca crashava antes porque o editor sempre carregava um circuito real ao iniciar. Com a mudança 1 (editor sem circuito pré-carregado), a primeira renderização passou a acontecer com um `Circuito` vazio e ambos lançavam `IndexOutOfBoundsException`. Corrigido com a mesma guarda que `desenhaPistaBox()` já tinha para o box (`if (getPistaKey() == null || isEmpty()) return;`).

- **Alternativa considerada (per-segmento)**: para cada segmento do traçado do box, testar `Line2D.intersectsLine`/`Rectangle.intersects` contra os segmentos da pista e pular o desenho da borda branca nesse segmento inteiro. Rejeitada — produz cortes grosseiros (um segmento inteiro sumindo ou aparecendo) em vez de um recorte geométrico preciso na interseção real, e não segue o idiom `Area` já presente no projeto.
- **Alternativa considerada (clip via `g2d.setClip`)**: usar `Area` da pista como clip invertido do `Graphics2D` ao desenhar a borda do box. Rejeitada — `Graphics2D` não tem "clip invertido" nativo (só interseção), exigiria compor a `Area` de qualquer forma; usar `Area.subtract` direto é mais simples e explícito.
- Custo de performance: `createStrokedShape`/`Area.subtract` em um traçado de circuito (dezenas a poucas centenas de nós) é desprezível comparado ao custo já existente de renderizar o circuito por frame; não introduz gargalo novo mensurável.

### 5. Listas de nós da pista/box redimensionáveis via JSplitPane
Segue **literalmente** o padrão já estabelecido (e comentado) em `gerarSecaoObjetos()` (`MainPanelEditor.java:655-664`) para o mesmo problema — altura fixa (lá `BorderLayout`, aqui `GridLayout(2,1)` + `getPreferredSize()` fixo em 160px nos `JScrollPane`) espremendo listas de conteúdo desigual:

```java
// Split vertical (não BorderLayout com altura fixa): a lista de cima
// tem lista+5 botões (Cima/Baixo/Primeiro/Ultimo/Remover) e a de
// baixo lista+3 (Cima/Baixo/Remover); uma altura em pixels fixa
// espremia as listas até sumir. 70/30 garante os 30% pedidos
// independente do conteúdo.
JSplitPane splitListas = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
        listaDesenho, listaFuncao);
splitListas.setResizeWeight(0.7);
splitListas.setOneTouchExpandable(true);
```

`gerarListsNosPistaBox()` troca o `JPanel controlPanel` com `GridLayout(2, 1)` por `JSplitPane splitNos = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pistas, boxes);`, com `setOneTouchExpandable(true)`, e um comentário explicando a proporção escolhida da mesma forma. Diferente do caso `listaDesenho`/`listaFuncao` (70/30, porque a lista de cima tem 5 botões próprios e a de baixo só 3), `pistas` e `boxes` têm exatamente o mesmo conteúdo estrutural — rótulo + `JScrollPane` da lista, sem nenhum botão embutido em nenhum dos dois lados (os botões de nó — tipo, lado do box, apagar — ficam em `gerarPainelControlesNos()`, compartilhados acima das duas listas, não dentro de `gerarListsNosPistaBox()`). Por isso `setResizeWeight(0.5)` é o valor correto aqui (proporção simétrica, igual ao `GridLayout(2,1)` atual como ponto de partida), com um comentário no mesmo espírito do original:

```java
// Split vertical (não GridLayout de altura fixa): pista e box têm o
// mesmo conteúdo (rótulo + lista, sem botões próprios — os controles
// de nó ficam em gerarPainelControlesNos(), compartilhados acima).
// 50/50 mantém a proporção atual como ponto de partida, mas agora
// redimensionável pelo usuário.
JSplitPane splitNos = new JSplitPane(JSplitPane.VERTICAL_SPLIT, pistas, boxes);
splitNos.setResizeWeight(0.5);
splitNos.setOneTouchExpandable(true);
```

Os overrides de `getPreferredSize()` que forçam altura fixa (160px) nos dois `JScrollPane`s são removidos, já que o `JSplitPane` passa a controlar a altura de cada lado.

- **Alternativa considerada**: manter `GridLayout(2,1)` e só remover o `getPreferredSize()` fixo dos `JScrollPane`. Rejeitada — `GridLayout` força os dois componentes a terem exatamente a mesma altura sempre, sem possibilidade de arrastar/redimensionar; não atende ao pedido do usuário de poder aumentar/diminuir cada lista.
- **Alternativa considerada**: copiar o `resizeWeight(0.7)` do caso `listaDesenho`/`listaFuncao` sem adaptar. Rejeitada — esse valor foi escolhido especificamente pela assimetria de botões daquele caso (5 vs 3), que não existe entre `pistas` e `boxes`; copiar cegamente ignoraria a própria lógica documentada no comentário original.

## Risks / Trade-offs

- [Risco] Recalcular `Area` da pista e da borda do box a cada frame de render pode custar mais que o desenho atual (que é só `Graphics2D.draw`/`fill` direto) → Mitigação: se necessário, cachear a `Area` resultante e invalidar só quando o circuito é revetorizado (mesmo gatilho que já invalida outros caches de desenho no editor), não a cada repaint.
- [Risco] Bloquear salvar por escapada incompleta pode surpreender quem já tinha esse hábito (deixar uma escapada "pela metade" temporariamente) → Mitigação: mensagem de alerta explícita (`Lang.msg`) apontando qual escapada está incompleta, mesmo padrão de clareza de `alertaPontoEscapadaInvalido()`.
- [Trade-off] Início sem circuito carregado exige um clique a mais do usuário em todo fluxo de edição (antes abria direto no último/primeiro circuito) → aceito, foi pedido explicitamente pelo usuário para evitar edição acidental do circuito errado.

