## Context

`ObjetoConstrucao` (`src/main/java/br/f1mane/entidades/ObjetoConstrucao.java`) desenha sempre a mesma forma fixa: um `RoundRectangle2D` externo preenchido com `corPimaria` e um `RoundRectangle2D` interno (10px de margem) preenchido com `corSecundaria`, ambos rotacionados por `getAngulo()` e escalados por `zoom`. `ObjetoLivre` já resolveu o mesmo tipo de problema (várias aparências para o mesmo objeto base) com um campo `tipo` (`TipoObjetoLivre`) que só muda o que é desenhado dentro de `desenha()`, sem alterar posição/ângulo/cor/transparência/nível — que continuam vindo de `ObjetoPista`/`ObjetoDesenho`. `FormularioObjetos` e `MainPanelEditor.criaPainelAjusteRapido` já sabem exibir campos condicionais por tipo concreto de objeto (`instanceof ObjetoLivre`, `instanceof ObjetoGuardRails`).

## Goals / Non-Goals

**Goals:**
- Adicionar `tipo` a `ObjetoConstrucao` com 5 variantes de desenho (`QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`, `PREDIO`), reaproveitando `largura`/`altura`/`angulo`/`corPimaria`/`corSecundaria`/`transparencia` já existentes.
- `PREDIO` e `BARCO` precisam de propriedades extras (`quantidadeAndares`+`direcaoEmpilhamento`, `afunilamento`) além do `tipo`, expostas no formulário e no menu de contexto do editor.
- Circuitos XML existentes (sem `tipo` gravado) continuam carregando com a aparência atual (`QUADRADO`).
- `obterArea()` continua correto para todos os tipos, incluindo `PREDIO` (cuja área desenhada é maior que `largura x altura` de um único andar).

**Non-Goals:**
- Não introduzir edição vetorial (pontos/hastes) como em `ObjetoLivre` — todos os tipos de `ObjetoConstrucao` continuam parametrizados só por posição/tamanho/ângulo/tipo, não por pontos arrastáveis.
- Não mudar o fluxo de criação de objeto no editor (`TipoObjetoPista.CONSTRUCAO` continua criando um único `ObjetoConstrucao`; o novo `tipo` é escolhido depois, no formulário/menu, como já acontece com `ObjetoLivre`).
- Não adicionar novas cores: `CAMINHAO` reaproveita `corPimaria`/`corSecundaria` para as duas formas lado a lado, sem campos de cor adicionais.

## Decisions

### `TipoObjetoConstrucao` como enum simples, dispatch em `desenha()`
Mesmo padrão de `TipoObjetoLivre`: um enum (`QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`, `PREDIO`) e um `switch`/`if` dentro de `ObjetoConstrucao.desenha()` que escolhe o método de desenho. Alternativa descartada: uma subclasse por tipo (`ObjetoConstrucaoBarco extends ObjetoConstrucao`) — rejeitada porque quebraria o registro de tipos existente (`TipoObjetoPista.CONSTRUCAO` cria um único tipo de classe) e o padrão XMLEncoder/XMLDecoder de persistência (cada subclasse precisaria de entrada própria no registro e no editor), enquanto `ObjetoLivre.tipo` já provou que um enum é suficiente para esse nível de variação visual.

### Novas propriedades ficam em `ObjetoConstrucao`, não em classes novas
`afunilamento` (int, 0-90, percentual de estreitamento da proa do `BARCO`), `quantidadeAndares` (int, mínimo 1) e `direcaoEmpilhamento` (enum `DirecaoEmpilhamento`) são campos simples de `ObjetoConstrucao`, sempre presentes (mesmo quando o `tipo` atual não os usa), exatamente como `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas` já convivem com `orientacao`. Isso evita herança condicional e mantém `ObjetoConstrucao` uma única classe bean compatível com `XMLEncoder`/`XMLDecoder`.

### `DirecaoEmpilhamento`: 8 valores nomeados por posição, não por ângulo numérico
`CIMA`, `BAIXO`, `ESQUERDA`, `DIREITA`, `CIMA_ESQUERDA`, `CIMA_DIREITA`, `BAIXO_ESQUERDA`, `BAIXO_DIREITA` — cada valor mapeia para um deslocamento fixo `(dx, dy)` em frações de `largura`/`altura` (ex.: metade do tamanho do andar), aplicado cumulativamente a cada andar extra do `PREDIO`. Alternativa descartada: guardar um ângulo livre (double) — rejeitada porque o pedido é especificamente 8 posições fixas (mesmo espírito de `OrientacaoGuardRails`, que também é um enum fechado de opções, não um ângulo livre), e um enum fechado é mais simples de expor num combo do formulário.

### `PREDIO` reaproveita o desenho de `QUADRADO` por repetição, não duplica a lógica de forma
O desenho de `PREDIO` chama o mesmo método interno que desenha um andar no estilo `QUADRADO` (retângulos arredondados aninhados), uma vez por andar, deslocando a posição a cada iteração segundo `direcaoEmpilhamento`. Isso é o que o pedido descreve ("repetição do mesmo desenho, só que empilhado") e evita duas implementações do mesmo retângulo arredondado aninhado.

### `largura`/`altura` continuam definindo o tamanho de cada forma, nunca a quantidade de repetições
Para `PREDIO`, `largura`/`altura` mudam o tamanho de cada andar desenhado (todos os andares usam o mesmo tamanho); `quantidadeAndares` é a única propriedade que controla quantas vezes o desenho se repete. Isso é explícito no requisito para evitar a interpretação alternativa (comum em outros editores) de que aumentar a largura "ladrilha" repetições automaticamente.

### `BARCO`: forma via `GeneralPath` com afunilamento na extremidade "direita" local (antes da rotação)
Antes de aplicar `getAngulo()`, o barco é desenhado com a proa apontando para a extremidade de maior X do retângulo local (`largura` de comprimento, `altura` de boca); a rotação existente já permite apontar a proa em qualquer direção. `afunilamento` (percentual, 0-90) define o comprimento da seção afunilada como `largura * afunilamento / 100`, com a altura reduzindo linearmente até um ponto na ponta. Isso reaproveita a mesma pipeline de rotação/zoom (`AffineTransform` + `GeneralPath.transform`) já usada por `QUADRADO`/`ObjetoLivre`.

### `obterArea()` cobre a união de todos os andares desenhados
Para `PREDIO`, `obterArea()` retorna o bounding box da última forma desenhada (união de todos os `Rectangle` dos andares), recalculado em `desenha()` como já acontece hoje (o campo `externo`/equivalente é atualizado a cada `desenha()` e `obterArea()` só lê esse estado). Antes do primeiro `desenha()`, o valor inicial usa `largura`/`altura` do objeto (sem repetição), preservando o requisito já existente em `objetos-cenario-circuito` de que `obterArea()` nunca lança `NullPointerException`.

## Risks / Trade-offs

- [Repetição do `PREDIO` gerar deslocamento acumulado muito grande com `quantidadeAndares` alto, saindo da área visível do circuito] → Limitar `quantidadeAndares` no spinner do formulário/menu (ex.: máximo 20) e usar um passo de deslocamento fracionário (não o `largura`/`altura` inteiro), como em qualquer editor 2D de "stacking".
- [Circuitos XML antigos com `ObjetoConstrucao` sem os novos campos] → Valores padrão seguros no construtor (`tipo=QUADRADO`, `quantidadeAndares=1`, `direcaoEmpilhamento=CIMA_DIREITA` ou similar, `afunilamento=30`), mesmo padrão já usado por `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas` (ver spec `objetos-cenario-circuito`, cenário "Guard rails de circuito antigo assume valores padrão").
- [`FormularioObjetos`/`criaPainelAjusteRapido` ficarem com lógica condicional cada vez mais ramificada à medida que mais tipos ganham campos próprios] → Aceito como trade-off consciente: o padrão condicional (`instanceof`/`tipo == X`) já existe e é o que o restante do editor usa (`ObjetoLivre`, `ObjetoGuardRails`); introduzir um mecanismo de campos dinâmicos por tipo seria um refactor maior, fora do escopo desta mudança.

## Open Questions

- Valor padrão exato de `afunilamento` e do passo de deslocamento do `PREDIO` (em % de `largura`/`altura`) fica a critério de quem implementar/ajustar visualmente no editor — não afeta a forma pública das propriedades nem os cenários de spec.
