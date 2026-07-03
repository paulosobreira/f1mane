# Spec: objeto-livre-vetorial

## Purpose

Defines the vector-based `ObjetoLivre` model used by the circuit editor for freeform shapes: vertices with symmetric curvature handles (`PontoCurva`), backward compatibility with legacy straight-polygon objects, closed-curve shape generation via `GeneralPath`/Bézier segments, in-editor dragging of points and handles, and a `tipo` property that selects a procedural fill pattern (solid, vegetation, water, gravel).

## Requirements

### Requirement: ObjetoLivre modela vértices com haste de curvatura simétrica
`ObjetoLivre` SHALL expor uma lista de vértices (`vertices`, tipo `List<PontoCurva>`), onde cada `PontoCurva` é um bean compatível com `XMLEncoder`/`XMLDecoder` (construtor sem argumentos, getters/setters) contendo uma posição (`posicao`) e uma ponta de haste (`hasteFim`). Quando `hasteFim` é `null` ou igual a `posicao`, o vértice SHALL se comportar como um vértice reto (sem curvatura nos segmentos adjacentes).

#### Scenario: Vértice recém-adicionado é reto por padrão
- **WHEN** um novo ponto é adicionado a um `ObjetoLivre` durante a criação, sem que o usuário ajuste nenhuma haste
- **THEN** o `PontoCurva` correspondente tem `hasteFim` igual a `posicao` (ou `null`), e o segmento adjacente é desenhado como reta

#### Scenario: Haste ajustada curva os dois segmentos adjacentes
- **WHEN** o usuário arrasta a ponta da haste de um vértice para uma posição diferente de `posicao`
- **THEN** os dois segmentos que tocam esse vértice (o que chega e o que sai) passam a ser curvos, usando a ponta arrastada e seu espelho (`2*posicao - hasteFim`) como pontos de controle de cada lado

### Requirement: Compatibilidade com objetos livres legados baseados em polígono reto
`ObjetoLivre` SHALL manter o campo legado `pontos` (`List<Point>`) apenas para leitura de circuitos XML existentes. Quando `vertices` estiver vazio e `pontos` não estiver, o sistema SHALL sintetizar a lista de vértices a partir de `pontos`, cada um com haste nula (reta), produzindo um desenho idêntico ao polígono de linhas retas usado antes desta mudança.

#### Scenario: Circuito antigo com ObjetoLivre continua desenhando igual
- **WHEN** um circuito XML existente com um `ObjetoLivre` definido apenas por `pontos` (sem `vertices`) é carregado e desenhado após esta mudança
- **THEN** a forma resultante é geometricamente idêntica ao polígono de linhas retas desenhado antes desta mudança

#### Scenario: Edição no novo editor passa a usar vertices
- **WHEN** o usuário edita (arrasta um ponto ou uma haste de) um `ObjetoLivre` legado no editor após esta mudança
- **THEN** o objeto passa a ter `vertices` preenchido e usado como fonte de verdade para desenho e persistência dali em diante

### Requirement: Forma de ObjetoLivre é uma curva fechada, não um polígono de retas
`ObjetoLivre.gerar()` SHALL construir a forma como um `java.awt.geom.GeneralPath` fechado, onde cada segmento entre vértices consecutivos (incluindo o segmento de fechamento entre o último e o primeiro vértice) SHALL ser desenhado como uma curva cúbica de Bézier usando a ponta de saída da haste do vértice de origem e a ponta de entrada (espelhada) da haste do vértice de destino como pontos de controle.

#### Scenario: Todas as hastes neutras produzem um polígono reto
- **WHEN** todos os vértices de um `ObjetoLivre` têm haste nula/coincidente com a posição
- **THEN** a forma resultante é visualmente equivalente a um polígono de linhas retas entre os vértices, na mesma ordem

#### Scenario: Uma haste ajustada produz um arco visível
- **WHEN** ao menos um vértice tem `hasteFim` diferente de `posicao`
- **THEN** o(s) segmento(s) adjacente(s) a esse vértice são desenhados como arcos curvos em vez de linhas retas

### Requirement: Editor permite arrastar pontos e hastes de um ObjetoLivre existente
O editor de circuitos SHALL permitir, para um `ObjetoLivre` selecionado em modo de edição de pontos, arrastar diretamente cada vértice para reposicioná-lo e arrastar a ponta de sua haste para ajustar a curvatura dos segmentos adjacentes, sem exigir recriar o objeto do zero.

#### Scenario: Arrastar um vértice existente reposiciona o ponto
- **WHEN** o usuário, com um `ObjetoLivre` em modo de edição de pontos, arrasta um vértice existente para uma nova posição no canvas
- **THEN** a posição desse vértice é atualizada e a forma é redesenhada refletindo a nova posição

#### Scenario: Arrastar a haste de um vértice ajusta a curvatura
- **WHEN** o usuário arrasta a ponta da haste de um vértice existente
- **THEN** `hasteFim` desse vértice é atualizado e os segmentos adjacentes são redesenhados como curva

### Requirement: ObjetoLivre tem um tipo que define um padrão de preenchimento
`ObjetoLivre` SHALL expor uma propriedade `tipo` do enum `TipoObjetoLivre` (`POLIGONO_SIMPLES`, `VEGETACAO_DENSA`, `AGUA`, `BRITA`), com `POLIGONO_SIMPLES` como valor padrão quando não definida. O formulário de edição de objetos (`FormularioObjetos`) SHALL permitir escolher o `tipo` quando o objeto em edição é um `ObjetoLivre`.

#### Scenario: ObjetoLivre recém-criado tem tipo padrão
- **WHEN** um novo `ObjetoLivre` é instanciado sem que o usuário defina um tipo
- **THEN** `getTipo()` retorna `POLIGONO_SIMPLES`

#### Scenario: Usuário escolhe um tipo no formulário
- **WHEN** o usuário abre o formulário de edição de um `ObjetoLivre` e seleciona "Água" no campo de tipo
- **THEN** `objetoLivre.getTipo()` passa a retornar `AGUA` após salvar o formulário

### Requirement: Tipo POLIGONO_SIMPLES preenche com cor sólida
Quando `tipo` é `POLIGONO_SIMPLES`, `ObjetoLivre.desenha()` SHALL preencher a forma inteira com a cor primária (`corPimaria`) sólida, exatamente como o comportamento de `ObjetoLivre` antes desta mudança.

#### Scenario: Preenchimento sólido inalterado
- **WHEN** um `ObjetoLivre` com `tipo=POLIGONO_SIMPLES` é desenhado
- **THEN** a forma é preenchida inteiramente com `corPimaria`, sem nenhum padrão adicional

### Requirement: Tipos de vegetação, água e brita desenham um padrão procedural simples
Quando `tipo` é `VEGETACAO_DENSA`, `AGUA` ou `BRITA`, `ObjetoLivre.desenha()` SHALL preencher a forma com `corPimaria` como cor de fundo e, restringindo o desenho seguinte à área da forma (clip), sobrepor um padrão procedural simples e determinístico usando `corSecundaria`: traços curtos representando touceiras para vegetação densa, pequenos arcos ondulados horizontais para água, e pequenos círculos deslocados para brita. O padrão SHALL ser determinístico (sem aleatoriedade não seedada), produzindo o mesmo resultado visual em desenhos sucessivos do mesmo objeto sem que ele "pisque" ou mude de frame para frame.

#### Scenario: Vegetação densa desenha padrão de touceiras
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_DENSA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de traços curtos usando `corSecundaria`, restrito à área da forma

#### Scenario: Água desenha padrão de ondas
- **WHEN** um `ObjetoLivre` com `tipo=AGUA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos arcos/ondas horizontais usando `corSecundaria`, restrito à área da forma

#### Scenario: Brita desenha padrão de pontos
- **WHEN** um `ObjetoLivre` com `tipo=BRITA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos círculos deslocados usando `corSecundaria`, restrito à área da forma

#### Scenario: Padrão não muda entre desenhos sucessivos
- **WHEN** o mesmo `ObjetoLivre` com `tipo` diferente de `POLIGONO_SIMPLES` é desenhado duas vezes seguidas sem alteração de posição, tamanho ou tipo
- **THEN** o padrão procedural resultante é idêntico nas duas renderizações
