## Why

`ObjetoConstrucao` hoje desenha sempre a mesma forma (dois retângulos arredondados aninhados), forçando quem monta um circuito a simular prédios, galpões, caminhões ou barcos combinando vários objetos e imagens de fundo. `ObjetoLivre` já resolveu esse problema para áreas de terreno com uma propriedade `tipo` que troca o padrão de desenho sem mexer no restante do objeto (posição, cor, ângulo); `ObjetoConstrucao` deveria seguir o mesmo caminho para formas de construção, e ganhar também uma forma genérica de empilhar cópias de si mesmo (efeito "prédio"/andares) que funcione com qualquer forma, não só uma específica.

## What Changes

- Adicionar a `ObjetoConstrucao` uma propriedade `tipo` (enum `TipoObjetoConstrucao`), seguindo o mesmo padrão de `ObjetoLivre`/`TipoObjetoLivre`: o desenho passa a variar por tipo, mantendo posição, ângulo, cores, transparência e nível de desenho já existentes.
- `QUADRADO`: forma atual (dois retângulos arredondados aninhados), mantida como padrão para objetos já existentes e recém-criados sem tipo definido.
- `REDONDO`: mesma composição de forma externa/interna do `QUADRADO`, mas desenhada como elipses em vez de retângulos arredondados.
- `CAMINHAO`: duas formas lado a lado dentro da área do objeto (não aninhadas) — uma quadrada (cabine) e uma retangular (carroceria) — simulando um caminhão.
- `BARCO`: um retângulo com uma das extremidades afunilada em ponta na direção do comprimento, simulando a proa de um barco; o grau de afunilamento é controlável por uma nova propriedade (`afunilamento`).
- **Empilhamento não é um tipo, é uma propriedade transversal a qualquer `tipo`**: `ObjetoConstrucao` ganha `quantidadeEmpilhamento` (quantas vezes a forma do `tipo` atual é repetida — 1 significa sem repetição, comportamento de hoje), `direcaoEmpilhamento` (enum de 8 posições: cima, baixo, esquerda, direita e as 4 diagonais) e `grauEmpilhamento` (inteiro, em pixels: o deslocamento de uma repetição para a seguinte). Com `quantidadeEmpilhamento > 1`, o objeto desenha sua forma (`QUADRADO`, `REDONDO`, `CAMINHAO` ou `BARCO`) várias vezes, cada repetição deslocada `grauEmpilhamento` pixels na direção configurada em relação à anterior (deslocamento cumulativo, "efeito prédio"/andares empilhados) — funciona igual para qualquer `tipo`, não é uma forma à parte.
- `largura`/`altura` continuam controlando o tamanho de cada forma desenhada (inclusive de cada repetição do empilhamento) e não influenciam `quantidadeEmpilhamento` — a quantidade de repetições é controlada exclusivamente por essa propriedade dedicada, e o deslocamento entre repetições é controlado exclusivamente por `grauEmpilhamento` (pixels), não por uma fração de `largura`/`altura`.
- Adicionar ao formulário de edição de objeto (`FormularioObjetos`) e ao menu de contexto de ajuste rápido (`MainPanelEditor.criaPainelAjusteRapido`) os novos campos: combo de `tipo`; spinner de `afunilamento` (exibido só quando `tipo=BARCO`); e spinners de `quantidadeEmpilhamento`/`grauEmpilhamento` + combo de `direcaoEmpilhamento`, exibidos sempre (para qualquer `tipo`), já que empilhamento não depende do tipo escolhido.
- `ObjetoConstrucao` sem `tipo`/propriedades de empilhamento gravados no XML (circuitos existentes) SHALL carregar como `QUADRADO` com `quantidadeEmpilhamento=1` (sem repetição), preservando a aparência atual.

## Capabilities

### New Capabilities
- `objeto-construcao-tipos`: Propriedade `tipo` em `ObjetoConstrucao` (enum `TipoObjetoConstrucao`: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`) com desenho procedural específico por tipo, mais as propriedades `afunilamento` (só usada por `BARCO`) e `quantidadeEmpilhamento`/`direcaoEmpilhamento`/`grauEmpilhamento` (empilhamento, aplicável a qualquer `tipo`, independente do tipo escolhido).

### Modified Capabilities
- `objetos-cenario-circuito`: O formulário de edição de objeto e o menu de contexto de ajuste rápido passam a exibir campos condicionais/sempre-visíveis específicos de `ObjetoConstrucao` (tipo, afunilamento condicional a BARCO, e empilhamento sempre visível), seguindo o mesmo padrão condicional já usado para `ObjetoLivre` (combo de tipo) e `ObjetoGuardRails` (orientação/espessura/vão).

## Impact

- `br.f1mane.entidades.ObjetoConstrucao` — novo campo `tipo` e novos campos `afunilamento`, `quantidadeEmpilhamento`, `direcaoEmpilhamento`, `grauEmpilhamento`; `desenha()` passa a desenhar a forma do `tipo` atual e repeti-la `quantidadeEmpilhamento` vezes (deslocando cada repetição por `grauEmpilhamento` na direção de `direcaoEmpilhamento`), em vez de desenhar sempre a mesma forma fixa uma única vez.
- `br.f1mane.entidades.TipoObjetoConstrucao` (novo enum: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`) e `br.f1mane.entidades.DirecaoEmpilhamento` (novo enum, 8 direções) — seguem o padrão de `TipoObjetoLivre`/`OrientacaoGuardRails`.
- `br.f1mane.editor.FormularioObjetos` — novo combo de tipo, campo condicional de afunilamento (só BARCO), e campos de empilhamento sempre visíveis (quantidade/direção/grau) para `ObjetoConstrucao`, seguindo o padrão já usado para `ObjetoLivre`/`ObjetoGuardRails` em `montarPainelParaTipo`/`carregarCampos`/`formularioObjetoPista`.
- `br.f1mane.editor.MainPanelEditor` — `criaPainelAjusteRapido` ganha os mesmos campos para ajuste rápido via menu de contexto.
- Circuitos existentes em `src/main/resources/circuitos/*.xml` continuam carregando normalmente; `ObjetoConstrucao` sem os novos campos assume `tipo=QUADRADO` e os valores padrão seguros das novas propriedades (`quantidadeEmpilhamento=1`, sem efeito de empilhamento).
