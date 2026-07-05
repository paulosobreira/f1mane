## Why

`ObjetoConstrucao` hoje desenha sempre a mesma forma (dois retângulos arredondados aninhados), forçando quem monta um circuito a simular prédios, galpões, caminhões ou barcos combinando vários objetos e imagens de fundo. `ObjetoLivre` já resolveu esse problema para áreas de terreno com uma propriedade `tipo` que troca o padrão de desenho sem mexer no restante do objeto (posição, cor, ângulo); `ObjetoConstrucao` deveria seguir o mesmo caminho para formas de construção.

## What Changes

- Adicionar a `ObjetoConstrucao` uma propriedade `tipo` (enum `TipoObjetoConstrucao`), seguindo o mesmo padrão de `ObjetoLivre`/`TipoObjetoLivre`: o desenho passa a variar por tipo, mantendo posição, ângulo, cores, transparência e nível de desenho já existentes.
- `QUADRADO`: forma atual (dois retângulos arredondados aninhados), mantida como padrão para objetos já existentes e recém-criados sem tipo definido.
- `REDONDO`: mesma composição de forma externa/interna do `QUADRADO`, mas desenhada como elipses em vez de retângulos arredondados.
- `CAMINHAO`: duas formas lado a lado dentro da área do objeto (não aninhadas) — uma quadrada (cabine) e uma retangular (carroceria) — simulando um caminhão.
- `BARCO`: um retângulo com uma das extremidades afunilada em ponta na direção do comprimento, simulando a proa de um barco; o grau de afunilamento é controlável por uma nova propriedade (`afunilamento`).
- `PREDIO`: repete o desenho base (mesma forma de `QUADRADO`) empilhado quantas vezes for configurado (`quantidadeAndares`), deslocando cada repetição na direção escolhida entre 8 posições (`direcaoEmpilhamento`: acima, abaixo, esquerda, direita e as 4 diagonais).
- `largura`/`altura` continuam controlando o tamanho de cada forma desenhada (inclusive de cada andar do `PREDIO`) e não influenciam `quantidadeAndares` — repetição é controlada exclusivamente pela nova propriedade dedicada.
- Adicionar ao formulário de edição de objeto (`FormularioObjetos`) e ao menu de contexto de ajuste rápido (`MainPanelEditor.criaPainelAjusteRapido`) os novos campos condicionais: combo de `tipo`, spinner de `afunilamento` (só para `BARCO`), e spinner de `quantidadeAndares` + combo de `direcaoEmpilhamento` (só para `PREDIO`).
- `ObjetoConstrucao` sem `tipo` gravado no XML (circuitos existentes) SHALL carregar como `QUADRADO`, preservando a aparência atual.

## Capabilities

### New Capabilities
- `objeto-construcao-tipos`: Propriedade `tipo` em `ObjetoConstrucao` (enum `TipoObjetoConstrucao`: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`, `PREDIO`) com desenho procedural específico por tipo, incluindo as propriedades adicionais `afunilamento` (BARCO) e `quantidadeAndares`/`direcaoEmpilhamento` (PREDIO).

### Modified Capabilities
- `objetos-cenario-circuito`: O formulário de edição de objeto e o menu de contexto de ajuste rápido passam a exibir campos condicionais específicos de `ObjetoConstrucao` (tipo, afunilamento, quantidade/direção de empilhamento), seguindo o mesmo padrão condicional já usado para `ObjetoLivre` (combo de tipo) e `ObjetoGuardRails` (orientação/espessura/vão).

## Impact

- `br.f1mane.entidades.ObjetoConstrucao` — novo campo `tipo` e novos campos `afunilamento`, `quantidadeAndares`, `direcaoEmpilhamento`; `desenha()` passa a despachar por tipo em vez de desenhar sempre a mesma forma.
- `br.f1mane.entidades.TipoObjetoConstrucao` (novo enum) e `br.f1mane.entidades.DirecaoEmpilhamento` (novo enum, 8 direções) — seguem o padrão de `TipoObjetoLivre`/`OrientacaoGuardRails`.
- `br.f1mane.editor.FormularioObjetos` — novo combo de tipo e novos campos condicionais (afunilamento, quantidade/direção de empilhamento) para `ObjetoConstrucao`, seguindo o padrão já usado para `ObjetoLivre`/`ObjetoGuardRails` em `montarPainelParaTipo`/`carregarCampos`/`formularioObjetoPista`.
- `br.f1mane.editor.MainPanelEditor` — `criaPainelAjusteRapido` ganha os mesmos campos condicionais para ajuste rápido via menu de contexto.
- Circuitos existentes em `src/main/resources/circuitos/*.xml` continuam carregando normalmente; `ObjetoConstrucao` sem os novos campos assume `tipo=QUADRADO` e os valores padrão seguros das novas propriedades.
