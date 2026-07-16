## Why

O padrão `VEGETACAO_DENSA` de `ObjetoLivre` desenhava sempre a mesma marca (uma cruz de 3 traços finos) repetida numa grade com jitter — visualmente pobre e pouco convincente como "floresta densa" vista de cima. Pedido do usuário, feito enquanto explorava um SVG de referência de plantas vistas de cima (`vecteezy_plant-top-view-flat-design_99391.svg`): usar esse material só como referência de estilo (não embutir o arquivo no jogo) e reimplementar o padrão proceduralmente com copas de árvore variadas, depois ajustar tamanho e densidade por feedback visual iterativo até chegar num resultado equilibrado.

## What Changes

- Cada touceira da vegetação densa passa a sortear, entre 4 silhuetas proceduais de "topo de árvore vista de cima" (polígonos em estrela com pontas/entalhe variados: estrela pontiaguda, copa arredondada, espinhos finos, copa lobulada), preenchida sólida — em vez de sempre a mesma cruz de 3 traços.
- Raio de cada touceira é ampliado por um fator aleatório entre 2x e 3x (sorteado por touceira), deixando as copas mais visíveis e com leve sobreposição entre vizinhas.
- Densidade da grade de vegetação densa passa a 1.5x a densidade original (espaçamento reduzido), mantendo a mesma proporção touceira/célula.
- Todo o sorteio (silhueta, ângulo, ampliação, posição, tamanho) continua consumindo o mesmo `Random` de semente fixa (`SEMENTE_VEGETACAO`), preservando o desenho determinístico entre renderizações sucessivas do mesmo objeto.
- `VEGETACAO_SIMPLES` não é afetado — mantém a marca diagonal única de antes.
- Proposta retroativa: a implementação já foi feita e validada (40/40 testes de `ObjetoLivre*` passando) antes deste documento ser escrito.

## Capabilities

### New Capabilities
(nenhuma)

### Modified Capabilities
- `objeto-livre-vetorial`: o requirement "Tipos de vegetação, água, brita, listrado e xadrez desenham um padrão procedural" descrevia `VEGETACAO_DENSA` com "traços curtos representando touceiras" — passa a descrever silhuetas de topo de árvore preenchidas, com raio ampliado (2x–3x) e densidade de grade 1.5x a original, mantendo o restante do requirement (determinismo, `corSecundaria`, clip pela área da forma) inalterado.

## Impact

- Código: `src/main/java/br/f1mane/entidades/ObjetoLivre.java` (`desenhaPadraoVegetacao`, novo método `formaTopoArvore`, novas constantes `VARIANTES_TOPO_ARVORE`, `AMPLIACAO_MIN_TOPO_ARVORE`, `AMPLIACAO_MAX_TOPO_ARVORE`, `FATOR_PASSO_VEGETACAO_DENSA` recalculado).
- Testes: nenhum teste precisou ser alterado — `ObjetoLivreTipoPadraoTest` e demais testes de `ObjetoLivre*` continuam cobrindo os mesmos invariantes (cor, determinismo, ausência de exceção, variação de tamanho, cobertura de área) e passam sem modificação.
- Sem mudança de API pública, de formato de persistência (XML de circuito) ou de comportamento de `VEGETACAO_SIMPLES`/demais tipos de `TipoObjetoLivre`.
