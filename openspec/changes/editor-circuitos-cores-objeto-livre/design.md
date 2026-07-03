## Context

`Circuito` já persiste `corFundo`/`corAsfalto` como campos `Color` transientes (`@JsonIgnore`), gravados via `XMLEncoder`/`XMLDecoder` como blocos `<object class="java.awt.Color">` de 4 inteiros RGBA, com getters/setters simples e indicadores de cor clicáveis no editor (`criaIndicadorDeCor()` em `MainPanelEditor`). Zebra e box hoje não seguem esse padrão: a zebra é pintada com `Color.WHITE`/`Color.RED` fixos em `PainelCircuito.desenhaTintaPistaZebra()`, e o box é pintado com as cores do carro (`carro.getCor1()`/`getCor2()`) em `desenhaBoxes()` — não há cor de circuito para a área de box.

A seleção de circuito no editor hoje é um `JLabel` (`lblCircuitoAtual`) que mostra o nome do arquivo XML, alimentado por uma lista `circuitosXml` carregada de `properties/circuitos.properties` (formato `arquivo.xml=Nome Amigável,cicloMs`) — o nome amigável já é parseado mas descartado, só o nome do arquivo é exibido, junto de botões Anterior/Próximo.

`ObjetoLivre` estende `ObjetoPista` e hoje é apenas uma lista de `Point` (`pontos`) convertida em um `java.awt.Polygon` (linhas retas) e preenchida com `corPimaria` sólida. Os pontos são adicionados por clique durante a criação (`desenhandoObjetoLivre` em `MainPanelEditor`), sem nenhuma forma de arrastar um ponto já colocado, e sem noção de curvatura — cada segmento é sempre uma reta.

## Goals / Non-Goals

**Goals:**
- Tornar zebra e box customizáveis por circuito, com fallback visualmente idêntico ao comportamento atual quando as novas cores não estão definidas.
- Trocar a exibição do circuito atual no editor por um combobox de nomes amigáveis, sem quebrar a navegação Anterior/Próximo existente.
- Remover o botão e o fluxo de créditos do editor de circuitos.
- Permitir que `ObjetoLivre` desenhe curvas (arcos) por segmento, editáveis por arraste de ponto e de haste no editor, ao estilo da ferramenta de caminhos do GIMP.
- Permitir que `ObjetoLivre` seja classificado por `tipo` (polígono simples, vegetação densa, água, brita) e desenhe um padrão procedural simples característico do tipo dentro da área fechada pela curva.
- Preservar a compatibilidade com todos os circuitos XML existentes sem exigir migração de dados.

**Non-Goals:**
- Não introduz handles assimétricos (entrada/saída independentes) como no GIMP avançado — a haste de cada ponto é simétrica (um segmento de reta passando pelo ponto, com as duas pontas controlando os dois segmentos adjacentes em espelho), suficiente para desenhar arcos suaves sem duplicar a complexidade de interação.
- Não gera texturas realistas (imagens bitmap) para vegetação/água/brita — os padrões são desenhos vetoriais simples (Java2D primitivo: pequenos traços, círculos, linhas onduladas), conforme pedido explicitamente ("padrão bem simples").
- Não altera a pintura do box durante uma corrida real quando um carro está associado ao slot (cores do carro continuam tendo prioridade); as novas cores de circuito só preenchem o caso hoje sem cor definida (preview no editor, geração de imagem em memória sem contexto de carro).
- Não adiciona um novo sistema de undo/redo dedicado à edição de pontos/hastes — usa o mesmo fluxo de salvar/recarregar já existente no editor.

## Decisions

**Cores de box/zebra seguem exatamente o padrão de `corFundo`/`corAsfalto`**
Novos campos `corBox1`, `corBox2`, `corZebra1`, `corZebra2` em `Circuito`, mesmo tipo (`Color`, transiente, `@JsonIgnore`), mesmos getters/setters simples, mesmos indicadores de cor no editor (reaproveitando `criaIndicadorDeCor()`/`atualizaCorLabel()`). Quando `null` (circuito antigo, XML sem esses campos), `desenhaTintaPistaZebra()` usa `Color.WHITE`/`Color.RED` como hoje, e o box mantém o comportamento atual baseado em cor do carro — nenhuma migração de XML é necessária. Alternativa considerada: gravar branco/vermelho explicitamente em todo XML existente (como foi feito para `ativo` em outra mudança) — rejeitada aqui porque o fallback em código já resolve o caso sem tocar em nenhum arquivo de circuito.

**Cor de box do circuito só se aplica quando não há cor de carro/time no contexto**
`corBox1`/`corBox2` alimentam o desenho do box no editor (que não tem contexto de corrida/carro) e a geração de imagem em memória (`objetos-cenario-circuito`, que também não tem carros). Em corrida real, `desenhaBoxes()` continua priorizando `carro.getCor1()`/`getCor2()` do time ocupando o slot — comportamento de jogo inalterado. Alternativa considerada: sempre sobrepor a cor do circuito à cor do carro — rejeitada por remover a identificação visual de equipe, que é o propósito atual do desenho do box em corrida.

**Combobox substitui o rótulo, Anterior/Próximo continuam funcionando em sincronia**
`lblCircuitoAtual` é substituído por um `JComboBox<CircuitoComboItem>` (um pequeno record/classe com `nomeAmigavel` e `arquivoXml`, `toString()` retornando o nome amigável) populado a partir da mesma lista já usada por `circuitosXml`/`popularCircuitos()`. Selecionar um item dispara o mesmo carregamento hoje disparado por Anterior/Próximo; Anterior/Próximo, por sua vez, atualizam `setSelectedIndex` do combobox sem dispará-lo em loop (guard contra reentrância no listener). Alternativa considerada: remover Anterior/Próximo e deixar só o combobox — rejeitada porque a spec existente (`dev-editor-tools`) já garante essa navegação e o usuário não pediu para removê-la, só para trocar a exibição do nome.

**Modelo de pontos com haste simétrica, compatível com `pontos` legado**
Nova classe bean `PontoCurva` (construtor sem args, getters/setters — compatível com `XMLEncoder`) com `Point posicao` e `Point hasteFim` (uma ponta da haste; a ponta oposta é espelhada matematicamente como `2*posicao - hasteFim`, nunca persistida). Quando `hasteFim` é `null` ou igual a `posicao`, o ponto se comporta como hoje (segmento reto). `ObjetoLivre` ganha um novo campo `List<PontoCurva> vertices`; o campo antigo `pontos` (`List<Point>`) é mantido só para leitura de XMLs legados. Na primeira vez que `gerar()`/`desenha()` roda e `vertices` está vazio mas `pontos` não, os vértices são sintetizados a partir de `pontos` com haste nula (retas), preservando o desenho idêntico ao atual sem exigir edição do XML. Uma vez que o objeto é editado no novo editor, `vertices` passa a ser a fonte de verdade e `pontos` para de ser usado (mas permanece no arquivo por compatibilidade com ferramentas externas, se houver). Alternativa considerada: reaproveitar `pontos` mudando seu tipo genérico para `List<PontoCurva>` — rejeitada porque `XMLDecoder` chamaria `pontos.add(Point)` em XMLs antigos (erasure de generics permite a chamada em runtime) e um `ClassCastException` ocorreria na primeira leitura de `PontoCurva` esperada.

**Curva por segmento via `CubicCurve2D`, forma final em `GeneralPath`**
Para dois vértices consecutivos A e B, o segmento é uma cúbica de Bézier usando a ponta de saída da haste de A (a ponta voltada para B) e a ponta de entrada da haste de B (a ponta voltada para A) como pontos de controle. Se ambas as hastes coincidem com seus vértices, a curva degenera para uma reta (comportamento idêntico ao polígono atual). O último segmento fecha a forma voltando ao primeiro vértice, do mesmo jeito. `ObjetoLivre.gerar()` passa a montar um `java.awt.geom.GeneralPath` fechado em vez de um `Polygon`.

**Padrões de preenchimento reaproveitam `corPimaria`/`corSecundaria` como "cor de fundo" e "cor de padrão"**
Em vez de criar dois novos campos de cor específicos de `ObjetoLivre`, o `tipo` novo (enum `TipoObjetoLivre`: `POLIGONO_SIMPLES` default, `VEGETACAO_DENSA`, `AGUA`, `BRITA`) reutiliza os campos já herdados de `ObjetoPista` — `corPimaria` vira semanticamente "cor de fundo" e `corSecundaria` vira "cor de padrão" quando o objeto é um `ObjetoLivre`. O formulário (`FormularioObjetos`) apenas rotula os dois seletores de cor como "Cor de Fundo"/"Cor de Padrão" quando o objeto editado é `ObjetoLivre` (em vez de "Cor Primária"/"Cor Secundária"). Isso preserva 100% de compatibilidade com XMLs existentes (nenhum campo novo de cor a migrar) e reaproveita a UI de cor já existente no formulário. Alternativa considerada: criar `corFundo`/`corPadrao` dedicados em `ObjetoLivre` — rejeitada por duplicar semântica sem necessidade e por exigir fallback explícito para XMLs antigos que só têm `corPimaria`/`corSecundaria`.

**Desenho do padrão: clip na forma + primitiva simples repetida em grade**
`ObjetoLivre.desenha()`, para `tipo != POLIGONO_SIMPLES`, preenche o `GeneralPath` com `corPimaria` (fundo) e então aplica `g2d.clip(path)` para restringir o desenho seguinte à área da forma, percorrendo uma grade de passo fixo (proporcional a `Carro.ALTURA`/zoom, não a pixels de tela) dentro do retângulo delimitador e desenhando, em cada célula, uma primitiva simples com `corSecundaria`: traços curtos em cruz para vegetação densa, pequenos arcos/ondas horizontais para água, pequenos círculos deslocados para brita. O passo e o deslocamento de cada célula são determinísticos (derivados da posição arredondada da célula, sem `Math.random()`), garantindo que o padrão não pisque nem mude entre frames. `POLIGONO_SIMPLES` mantém o preenchimento sólido de `corPimaria` de hoje, sem uso de `corSecundaria`.

## Risks / Trade-offs

- [Objetos livres muito grandes com padrão em grade podem custar mais tempo de desenho por frame que o preenchimento sólido atual] → Mitigação: passo de grade fixo e não muito pequeno (ordem de grandeza de um carro), e o clip restringe o trabalho de fato pintado à área da forma, não ao retângulo delimitador inteiro.
- [Curvas muito fechadas com hastes longas podem gerar self-intersection na forma preenchida] → Mitigação: comportamento aceito como responsabilidade do usuário ao editar (mesma tolerância que ferramentas de path do GIMP têm para curvas mal-formadas); não é um requisito bloquear geometrias inválidas nesta mudança.
- [Arrastar pontos/hastes no editor pode conflitar com os mouse listeners existentes de pan/zoom/seleção de outros objetos] → Mitigação: o modo de edição de vértices só fica ativo quando o `ObjetoLivre` selecionado está com seu modo de edição de pontos explicitamente ligado (ex.: um botão "Editar Pontos" no formulário ou lista de objetos), análogo ao `desenhandoObjetoLivre` já usado durante a criação.
- [Circuitos antigos com `ObjetoLivre` que dependiam do preenchimento sólido de `corPimaria` continuam corretos, mas se o autor do circuito nunca configurou `corSecundaria`, um novo `tipo` de padrão pode desenhar com uma cor secundária "acidental" herdada de um valor padrão da classe] → Mitigação: `ObjetoPista` já define um valor padrão não nulo para `corSecundaria` (ver `objetos-cenario-circuito`); ao trocar de `POLIGONO_SIMPLES` para outro tipo no editor, o formulário exibe a cor de padrão atual para revisão antes de salvar.

## Migration Plan

Aditivo, sem necessidade de reescrever nenhum XML de circuito existente: os quatro novos campos de cor em `Circuito` e o novo campo `vertices`/`tipo` em `ObjetoLivre` têm fallback em código para o comportamento atual quando ausentes. Deploy via build normal (`mvn clean package`); sem flag de rollout. Rollback: reverter o commit; nenhuma migração de dados a desfazer, já que nenhum arquivo de circuito precisa ser reescrito para esta mudança funcionar. Validação: abrir circuitos existentes no editor após a mudança e confirmar que zebra, box e objetos livres existentes são desenhados de forma idêntica a antes.

## Open Questions

- O passo exato da grade de padrão (vegetação/água/brita) e o tamanho das primitivas serão ajustados visualmente durante a implementação — o design fixa a abordagem (grade determinística + clip), não os valores numéricos finais.
- Se o editor deve expor um botão dedicado "Editar Pontos" ou se o modo de arraste de vértices/hastes fica sempre ativo quando um `ObjetoLivre` está selecionado na lista de objetos — decisão de UX a validar durante a implementação, sem impacto no modelo de dados.
