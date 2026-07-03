## Why

O editor de circuitos já permite customizar cor de fundo e cor de asfalto por circuito, mas a zebra (branco/vermelho) e a área de box (cores dos carros) ainda são hardcoded ou dependentes de dados que não existem em modo edição. A seleção do circuito atual no editor mostra o nome bruto do arquivo XML em vez do nome amigável já disponível em `circuitos.properties`. Há também um botão "Créditos" que não faz mais sentido no fluxo atual do editor. Por fim, o objeto livre (`ObjetoLivre`) só desenha um polígono de linhas retas preenchido com uma cor sólida — não há como desenhar curvas/arcos ajustando a curvatura de cada segmento (como a ferramenta de caminhos do GIMP), nem como classificar a área por tipo (vegetação, água, brita) com um padrão de preenchimento correspondente, o que hoje força o uso de imagens de fundo pré-renderizadas para representar essas áreas.

## What Changes

- Adicionar campos de cor `corBox1`/`corBox2` e `corZebra1`/`corZebra2` em `Circuito`, editáveis no editor junto aos indicadores de cor de fundo e cor de asfalto já existentes, com branco/vermelho como padrão de `corZebra1`/`corZebra2` quando não definidos.
- Usar `corZebra1`/`corZebra2` no desenho da zebra (`PainelCircuito.desenhaTintaPistaZebra`) no lugar de `Color.WHITE`/`Color.RED` hardcoded.
- Usar `corBox1`/`corBox2` como cor padrão de desenho da área de box no editor e em cenários sem carro ocupando o box (a lógica de pintar com a cor do carro em corrida permanece inalterada).
- Substituir o rótulo de circuito atual (`lblCircuitoAtual`, hoje mostrando o nome do arquivo XML) por um combobox que lista o nome amigável de cada circuito (de `circuitos.properties`), mantendo o arquivo XML como valor interno de seleção; selecionar um item no combobox carrega esse circuito, e a navegação Anterior/Próximo existente continua funcionando e mantém o combobox sincronizado.
- Remover o botão "Créditos" e o fluxo de edição de posição de créditos do editor de circuitos.
- Reformular `ObjetoLivre` para edição vetorial ao estilo da ferramenta de caminhos do GIMP: cada ponto pode ter hastes (handles) que controlam a curvatura do segmento adjacente, permitindo desenhar arcos/curvas em vez de apenas linhas retas entre pontos; os pontos existentes tornam-se arrastáveis diretamente no editor (não só clicáveis na criação).
- Adicionar a `ObjetoLivre` uma propriedade `tipo` (enum: poligono simples, vegetação densa, água, brita) e campos de cor de fundo e cor de padrão; cada tipo (exceto o padrão "polígono simples") desenha, dentro da área fechada pela curva, um padrão procedural simples característico (vegetação, água ou brita) usando as duas cores, em vez do preenchimento sólido atual.

## Capabilities

### New Capabilities

- `circuito-cores-box-zebra`: Campos de cor customizáveis para box (`corBox1`/`corBox2`) e zebra (`corZebra1`/`corZebra2`) por circuito, editáveis no editor e usados no desenho em jogo/editor no lugar dos valores hardcoded.
- `objeto-livre-vetorial`: Edição vetorial de `ObjetoLivre` com pontos arrastáveis e hastes de curvatura por segmento (estilo ferramenta de caminhos do GIMP), mais uma propriedade `tipo` que seleciona um padrão de preenchimento procedural (vegetação, água, brita) desenhado com cor de fundo e cor de padrão configuráveis.

### Modified Capabilities

- `dev-editor-tools`: A seleção do circuito atual no editor passa de rótulo de texto (nome do arquivo XML) para um combobox com o nome amigável do circuito, e o botão "Créditos" (e o fluxo de edição de posição de créditos) é removido do editor de circuitos.

## Impact

- `br.f1mane.entidades.Circuito` — novos campos `corBox1`, `corBox2`, `corZebra1`, `corZebra2` (padrão `Circuito.java:77-79`, junto a `corFundo`/`corAsfalto`).
- `br.f1mane.visao.PainelCircuito` — `desenhaTintaPistaZebra()` (zebra) e `desenhaBoxes()`/`gerarBoxes()` (box) passam a usar as cores do circuito quando aplicável.
- `br.f1mane.editor.MainPanelEditor` — novos indicadores de cor (padrão de `criaIndicadorDeCor()`), combobox de seleção de circuito substituindo `lblCircuitoAtual`, remoção do botão/fluxo de créditos, e a nova interação de arraste de pontos/hastes do objeto livre.
- `br.f1mane.entidades.ObjetoLivre` — novo modelo de pontos com hastes de curvatura (curvas de Bézier por segmento), propriedade `tipo`, `corFundo`/`corPadrao`, e novo método de desenho com padrão procedural por tipo.
- `br.f1mane.editor.FormularioObjetos` — novo campo de seleção de `tipo` e cores de fundo/padrão para `ObjetoLivre`.
- Circuitos existentes em `src/main/resources/circuitos/*.xml` continuam carregando normalmente; os novos campos de cor e o novo modelo de `ObjetoLivre` têm valores padrão seguros quando ausentes do XML.
