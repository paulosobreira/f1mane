## Why

`ObjetoGuardRails` hoje só pode ser criado ponto a ponto uma única vez: depois de finalizar o desenho (clique direito), não há como corrigir um ponto errado sem apagar o objeto inteiro e recomeçar — diferente de `ObjetoLivre`, que já permite arrastar vértices existentes. Além disso, a aparência do padrão de linhas finas (espessura da linha e vão entre elas) é fixa em código (`LARGURA_LINHA = 1`), e desenhar o traçado ponto a ponto sobre a pista exige precisão manual do usuário, sem nenhum auxílio de alinhamento ao traçado do circuito ou a outros objetos já posicionados. Essas três lacunas tornam o fluxo de criação de guard rails mais lento e propenso a erro do que o de `ObjetoLivre`.

## What Changes

- Permitir editar um `ObjetoGuardRails` já criado no editor: mover um ponto existente, inserir um novo ponto no meio de um segmento, e remover um ponto, reaproveitando o mesmo modo de "editar pontos" (clique direito → editar) já usado por `ObjetoLivre`, adaptado para pontos retos (sem hastes de curvatura).
- Expor no `FormularioObjetos`, para `ObjetoGuardRails`, campos numéricos para a espessura da linha fina (`larguraLinha`) e o vão entre linhas (`vaoEntreLinhas`), hoje fixos em constantes (`LARGURA_LINHA`/`VAO_ENTRE_LINHAS`), passando esses dois valores a ser propriedades persistidas do objeto em vez de constantes de classe.
- Adicionar, durante o clique-a-clique de criação (e ao arrastar um ponto existente) de um `ObjetoGuardRails`, um auxílio de alinhamento (snapping) que aproxima o ponto sendo posicionado do nó de pista mais próximo (`No`) ou de um vértice/ponto de outro objeto de cenário já no circuito, quando a distância estiver dentro de uma tolerância em pixels de tela.

## Capabilities

### New Capabilities
- `guard-rails-point-editing`: edição de pontos de um `ObjetoGuardRails` já criado (mover, inserir, remover) no editor de circuitos.
- `guard-rails-snap-to-track`: alinhamento automático (snap) dos pontos de um `ObjetoGuardRails`, durante criação ou edição, a nós de pista ou pontos de outros objetos próximos.

### Modified Capabilities
- `objetos-cenario-circuito`: o requisito "Cor e tamanho de um objeto de pista são editáveis no editor" passa a incluir, especificamente para `ObjetoGuardRails`, a edição de espessura da linha fina e do vão entre linhas como propriedades do objeto (não mais constantes fixas).

## Impact

- `src/main/java/br/f1mane/entidades/ObjetoGuardRails.java`: `LARGURA_LINHA`/`VAO_ENTRE_LINHAS` deixam de ser `static final` e passam a ser campos de instância com getters/setters (bean property, persistida em XML); adiciona métodos de manipulação de pontos (inserir/mover/remover) usados pelo editor.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: novo estado de "editando pontos de guard rails" (análogo a `editandoPontosDe`/`ObjetoLivre`), lógica de arraste/inserção/remoção de ponto sem hastes, e lógica de snapping consultando os nós do circuito (`circuito.getNos()`/`getNosBox()`) e os pontos de outros objetos de cenário.
- `src/main/java/br/f1mane/editor/FormularioObjetos.java`: novos campos numéricos para espessura de linha e vão, visíveis apenas quando o objeto em edição é `ObjetoGuardRails`.
- Circuitos XML existentes com `ObjetoGuardRails`: continuam carregando normalmente; `larguraLinha`/`vaoEntreLinhas` ausentes no XML assumem os mesmos valores hoje hardcoded (1) como default, preservando a aparência atual.
