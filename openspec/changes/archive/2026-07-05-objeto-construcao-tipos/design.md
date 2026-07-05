## Context

`ObjetoConstrucao` (`src/main/java/br/f1mane/entidades/ObjetoConstrucao.java`) desenha sempre a mesma forma fixa: um `RoundRectangle2D` externo preenchido com `corPimaria` e um `RoundRectangle2D` interno (10px de margem) preenchido com `corSecundaria`, ambos rotacionados por `getAngulo()` e escalados por `zoom`. `ObjetoLivre` já resolveu o mesmo tipo de problema (várias aparências para o mesmo objeto base) com um campo `tipo` (`TipoObjetoLivre`) que só muda o que é desenhado dentro de `desenha()`, sem alterar posição/ângulo/cor/transparência/nível — que continuam vindo de `ObjetoPista`/`ObjetoDesenho`. `FormularioObjetos` e `MainPanelEditor.criaPainelAjusteRapido` já sabem exibir campos condicionais por tipo concreto de objeto (`instanceof ObjetoLivre`, `instanceof ObjetoGuardRails`).

## Goals / Non-Goals

**Goals:**
- Adicionar `tipo` a `ObjetoConstrucao` com 4 variantes de desenho de forma (`QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`), reaproveitando `largura`/`altura`/`angulo`/`corPimaria`/`corSecundaria`/`transparencia` já existentes.
- Empilhamento (repetir a forma do `tipo` atual várias vezes, deslocada a cada repetição) é uma propriedade transversal (`quantidadeEmpilhamento`/`direcaoEmpilhamento`/`grauEmpilhamento`), não um `tipo` — funciona igual para qualquer um dos 4 tipos.
- `BARCO` precisa de uma propriedade extra (`afunilamento`) além do `tipo`, exposta condicionalmente no formulário e no menu de contexto do editor; as propriedades de empilhamento ficam sempre visíveis (não são condicionais a nenhum `tipo`).
- Circuitos XML existentes (sem `tipo`/empilhamento gravados) continuam carregando com a aparência atual (`QUADRADO`, `quantidadeEmpilhamento=1`).
- `obterArea()` continua correto para qualquer combinação de `tipo` + `quantidadeEmpilhamento` (a área cresce para cobrir todas as repetições, independente do `tipo` da forma repetida).

**Non-Goals:**
- Não introduzir edição vetorial (pontos/hastes) como em `ObjetoLivre` — todos os tipos de `ObjetoConstrucao` continuam parametrizados só por posição/tamanho/ângulo/tipo/empilhamento, não por pontos arrastáveis.
- Não mudar o fluxo de criação de objeto no editor (`TipoObjetoPista.CONSTRUCAO` continua criando um único `ObjetoConstrucao`; o `tipo` e o empilhamento são escolhidos depois, no formulário/menu, como já acontece com `ObjetoLivre`).
- Não adicionar novas cores: `CAMINHAO` reaproveita `corPimaria`/`corSecundaria` para as duas formas lado a lado, sem campos de cor adicionais; o empilhamento reaproveita as mesmas cores em todas as repetições (não alterna cor por repetição).

## Decisions

### `TipoObjetoConstrucao` como enum de 4 valores, dispatch em um método de forma única
Mesmo padrão de `TipoObjetoLivre`: um enum (`QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`) e um `switch`/`if` dentro de um método privado (`desenhaFormaUnica(Graphics2D, double zoom, int deslocamentoX, int deslocamentoY)`) que desenha só uma instância da forma do tipo atual, na posição base deslocada por `(deslocamentoX, deslocamentoY)`. Alternativa descartada: uma subclasse por tipo (`ObjetoConstrucaoBarco extends ObjetoConstrucao`) — rejeitada pelos mesmos motivos já registrados (quebraria o registro de tipos existente e o padrão XMLEncoder/XMLDecoder de persistência).

### Empilhamento é ortogonal ao `tipo`: `desenha()` chama a forma única em loop
`ObjetoConstrucao.desenha()` chama `desenhaFormaUnica(...)` `quantidadeEmpilhamento` vezes (mínimo 1, ou seja, sem repetição por padrão), incrementando a cada iteração o deslocamento acumulado segundo `direcaoEmpilhamento` × `grauEmpilhamento`. Isso significa que **qualquer** `tipo` (`QUADRADO`, `REDONDO`, `CAMINHAO` ou `BARCO`) pode ser empilhado — não existe um "tipo prédio" à parte; "prédio" é só o efeito visual de empilhar repetidamente uma forma `QUADRADO`, mas o mecanismo é genérico. Isso simplifica tanto `TipoObjetoConstrucao` (menos um valor) quanto o desenho (um único ponto de repetição, não uma implementação de forma dedicada a "prédio").

### Novas propriedades ficam em `ObjetoConstrucao`, não em classes novas
`afunilamento` (int, 0-90, percentual de estreitamento da proa do `BARCO`), `quantidadeEmpilhamento` (int, mínimo 1, default 1), `direcaoEmpilhamento` (enum `DirecaoEmpilhamento`) e `grauEmpilhamento` (int, pixels, default > 0) são campos simples de `ObjetoConstrucao`, sempre presentes (mesmo quando o `tipo` atual não usa `afunilamento`, ou quando `quantidadeEmpilhamento=1` não usa `direcaoEmpilhamento`/`grauEmpilhamento`), exatamente como `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas` já convivem com `orientacao`. Isso evita herança condicional e mantém `ObjetoConstrucao` uma única classe bean compatível com `XMLEncoder`/`XMLDecoder`.

### `DirecaoEmpilhamento`: 8 valores nomeados por posição, mapeados para um vetor unitário `(dx, dy)`
`CIMA`, `BAIXO`, `ESQUERDA`, `DIREITA`, `CIMA_ESQUERDA`, `CIMA_DIREITA`, `BAIXO_ESQUERDA`, `BAIXO_DIREITA` — cada valor mapeia para um vetor unitário/normalizado `(dx, dy)` (ex.: `CIMA` = `(0, -1)`, `CIMA_DIREITA` = `(0.707, -0.707)`), multiplicado por `grauEmpilhamento` (pixels) e acumulado a cada repetição extra. Alternativa descartada: guardar um ângulo livre (double) — rejeitada porque o pedido é especificamente 8 posições fixas (mesmo espírito de `OrientacaoGuardRails`), e um enum fechado é mais simples de expor num combo do formulário.

### `grauEmpilhamento` é um valor absoluto em pixels, não uma fração de `largura`/`altura`
O deslocamento entre uma repetição e a seguinte é definido diretamente em pixels pelo usuário (`grauEmpilhamento`), não derivado de um percentual do tamanho da forma — pedido explícito do usuário ("se eu coloco quatro pixels, então o próximo desenho é quatro pixels pra cima do desenho de baixo"). Isso é diferente da primeira versão deste design (que usava uma fração de `largura`/`altura`); a versão em pixels dá controle direto e prevísivel do efeito de empilhamento, independente do tamanho da forma.

### `largura`/`altura` continuam definindo o tamanho de cada forma, nunca a quantidade de repetições nem o deslocamento entre elas
`largura`/`altura` mudam o tamanho de cada repetição desenhada (todas as repetições usam o mesmo tamanho); `quantidadeEmpilhamento` é a única propriedade que controla quantas vezes a forma se repete, e `grauEmpilhamento` é a única que controla o deslocamento entre repetições. Isso é explícito no requisito para evitar a interpretação alternativa (comum em outros editores) de que aumentar a largura "ladrilha" repetições automaticamente, ou que o deslocamento escala com o tamanho da forma.

### `BARCO`: forma via `GeneralPath` com afunilamento na extremidade "direita" local (antes da rotação)
Antes de aplicar `getAngulo()`, o barco é desenhado com a proa apontando para a extremidade de maior X do retângulo local (`largura` de comprimento, `altura` de boca); a rotação existente já permite apontar a proa em qualquer direção. `afunilamento` (percentual, 0-90) define o comprimento da seção afunilada como `largura * afunilamento / 100`, com a altura reduzindo linearmente até um ponto na ponta. Isso reaproveita a mesma pipeline de rotação/zoom (`AffineTransform` + `GeneralPath.transform`) já usada por `QUADRADO`/`ObjetoLivre`, e continua valendo forma-a-forma mesmo quando empilhada (cada barco empilhado é afunilado do mesmo jeito).

### `obterArea()` cobre a união de todas as repetições desenhadas, para qualquer `tipo`
`obterArea()` retorna o bounding box da união de todas as formas desenhadas na última chamada de `desenha()` (uma só, se `quantidadeEmpilhamento=1`; várias, deslocadas cumulativamente, caso contrário), recalculado a cada `desenha()` como já acontece hoje. Antes do primeiro `desenha()`, o valor inicial usa `largura`/`altura` do objeto (sem repetição), preservando o requisito já existente em `objetos-cenario-circuito` de que `obterArea()` nunca lança `NullPointerException`.

## Risks / Trade-offs

- [Empilhamento gerar deslocamento acumulado muito grande com `quantidadeEmpilhamento` alto e/ou `grauEmpilhamento` alto, saindo da área visível do circuito] → Limitar `quantidadeEmpilhamento` no spinner do formulário/menu (ex.: máximo 20) — o usuário continua no controle de `grauEmpilhamento` em pixels, então o limite prático fica a critério de quem edita o circuito, igual a qualquer outro campo de tamanho/posição já existente.
- [Circuitos XML antigos com `ObjetoConstrucao` sem os novos campos] → Valores padrão seguros no construtor (`tipo=QUADRADO`, `quantidadeEmpilhamento=1`, `direcaoEmpilhamento` com algum valor não-nulo, `grauEmpilhamento` > 0, `afunilamento=30`), mesmo padrão já usado por `ObjetoGuardRails.larguraLinha`/`vaoEntreLinhas` (ver spec `objetos-cenario-circuito`, cenário "Guard rails de circuito antigo assume valores padrão"). Como `quantidadeEmpilhamento=1` não produz repetição, `direcaoEmpilhamento`/`grauEmpilhamento` ficam sem efeito visível até o usuário aumentar a quantidade.
- [`FormularioObjetos`/`criaPainelAjusteRapido` ficarem com lógica condicional cada vez mais ramificada à medida que mais tipos ganham campos próprios] → Aceito como trade-off consciente: o padrão condicional (`instanceof`/`tipo == X`) já existe e é o que o restante do editor usa (`ObjetoLivre`, `ObjetoGuardRails`); introduzir um mecanismo de campos dinâmicos por tipo seria um refactor maior, fora do escopo desta mudança. Os campos de empilhamento, por serem independentes do `tipo`, ficam sempre visíveis (menos ramificação que teria se fossem condicionais a um `tipo=PREDIO`).

## Open Questions

- Valor padrão exato de `afunilamento` e de `grauEmpilhamento` fica a critério de quem implementar/ajustar visualmente no editor — não afeta a forma pública das propriedades nem os cenários de spec.
