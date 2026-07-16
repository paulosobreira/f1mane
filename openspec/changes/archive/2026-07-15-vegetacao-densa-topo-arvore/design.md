## Context

`ObjetoLivre.desenha()` já usa um mecanismo procedural genérico (`desenhaComClipSemAntialiasing`) que aplica clip pela silhueta do objeto, desliga antialiasing e roda o desenho do padrão dentro de um sistema de coordenadas rotacionado por `angulo`. `desenhaPadraoVegetacao` percorre uma grade de células (`passo`) com jitter (`Random` de semente fixa `SEMENTE_VEGETACAO`) e, pra cada célula, desenhava a mesma primitiva (`desenhaPrimitivaPadrao`, um switch por `tipo`). Para `VEGETACAO_DENSA` essa primitiva era uma cruz de 3 `drawLine`.

Durante a exploração desta mudança, o usuário apontou o SVG `vecteezy_plant-top-view-flat-design_99391.svg` (ícones de plantas vistas de cima) como referência de estilo, mas deixou explícito que não queria embutir o arquivo/imagem no jogo — queria "ler" o padrão visual (silhuetas de copa: pontas radiais, arredondada, espinhos, lóbulos) e reimplementá-lo como código procedural, no mesmo espírito dos outros padrões de `ObjetoLivre` (nenhum deles usa assets externos).

## Goals / Non-Goals

**Goals:**
- Substituir a marca única da vegetação densa por várias silhuetas de "topo de árvore" geradas em código (sem nenhuma dependência de asset externo ou biblioteca de SVG).
- Preservar todos os invariantes já testados: cor exata (`corSecundaria` + `transparencia`), determinismo entre renderizações, ausência de exceção, silhueta sempre um polígono simples (fill = 1 componente conexo por marca).
- Permitir ajuste fino de tamanho/densidade por constantes isoladas, já que o resultado visual foi calibrado por iteração com o usuário (tamanho e espaçamento mudaram várias vezes até o resultado atual).

**Non-Goals:**
- Não carregar nem rasterizar o SVG de referência — ele não é usado em tempo de execução, só serviu de inspiração visual durante o desenvolvimento.
- Não alterar `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`, `LISTRADO`, `XADREZ` nem o mecanismo de clip/rotação/antialiasing compartilhado.
- Não introduzir nova biblioteca gráfica (Batik ou similar).

## Decisions

- **Silhueta via polígono em estrela (raio externo/interno alternado por vértice)** em vez de path bézier complexo: `formaTopoArvore(cx, cy, raio, variante, anguloBase)` gera um `GeneralPath` com `pontas*2` vértices, alternando `raio` e `raio*fatorEntalhe`. É barato de computar, sempre produz um polígono simples (não autointerceptante) — importante porque o preenchimento (`g2d.fill`) deve resultar em exatamente 1 componente conexo por touceira, requisito implícito dos testes de contagem de blobs.
  - Alternativa descartada: parsear os paths do SVG de referência (via Batik ou parser manual) e usar as formas originais. Rejeitada porque o usuário pediu explicitamente para não usar o arquivo no jogo, e adicionaria uma dependência pesada (Batik) só para um padrão de preenchimento.
  - Alternativa descartada: pré-renderizar sprites PNG a partir do SVG (padrão já usado pelo projeto para carros/capacetes via `SpriteSheet`). Rejeitada pelo mesmo motivo — o usuário não queria usar o arquivo, só o estilo.
- **4 variantes fixas** (`pontas`/`fatorEntalhe` codificados num `switch`) em vez de parâmetros totalmente aleatórios: dá variedade visual suficiente (estrela pontiaguda, copa arredondada, espinhos finos, copa lobulada) mantendo previsibilidade — cada variante tem uma silhueta reconhecível, evitando formas degeneradas que apareceriam com parâmetros aleatórios sem curadoria.
- **Ampliação do raio (2x–3x) aplicada por cima do `fatorTamanho` existente**, não substituindo-o: preserva a variação de tamanho já testada (`temTamanhoBemVariadoEntreAsMarcas`) e some-se a ela — resultado final tem tanto a variação original quanto o aumento pedido.
- **Densidade controlada só por `FATOR_PASSO_VEGETACAO_DENSA`** (não por um novo parâmetro): como densidade (marcas/área) ∝ `1/fatorPasso²`, cada ajuste pedido pelo usuário ("triplique", "diminua pela metade", "diminua pela metade de novo", "volte pra densidade anterior") foi implementado multiplicando/dividindo esse único fator por `sqrt(n)`, mantendo a razão touceira/célula (raio ainda deriva de `passo`) constante — sem precisar de uma segunda constante de densidade.
- **Sorteio de variante/ângulo/ampliação consome o mesmo `Random` da posição/tamanho**, na mesma ordem a cada execução: é o que já garante determinismo nos outros padrões (`SEMENTE_VEGETACAO` fixa); não foi criado um `Random` separado.

## Risks / Trade-offs

- [Copas grandes (raio ampliado) podem se sobrepor ou até se fundir visualmente com vizinhas quando a densidade é alta] → Mitigado por calibração iterativa direta com o usuário (densidade e ampliação foram ajustadas várias vezes até o equilíbrio atual: 1.5x densidade original, ampliação 2x–3x); testes de "several separate blobs" (`>= 3`) continuam passando porque a área de teste (triângulo 300x300) comporta dezenas de células mesmo com a densidade aumentada.
- [Constantes de estilo (pontas/entalhe por variante, faixa de ampliação, fator de densidade) ficam "mágicas" no código, sem uma regra de negócio que as derive] → Aceito: é um padrão puramente visual/decorativo: comentários no código documentam a proporção pretendida e o histórico dos ajustes.

## Migration Plan

Não há migração de dados: o padrão é gerado em tempo de desenho, não persistido. Circuitos XML existentes com `ObjetoLivre` do tipo `VEGETACAO_DENSA` passam a exibir a nova silhueta automaticamente na próxima renderização, sem qualquer alteração no arquivo do circuito. Rollback é reverter o commit — não há estado externo a desfazer.

## Open Questions

Nenhuma pendente — mudança já implementada e validada (40/40 testes) antes deste documento.
