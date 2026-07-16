## MODIFIED Requirements

### Requirement: Tipos de vegetação, água, brita, listrado e xadrez desenham um padrão procedural
Quando `tipo` é `AGUA`, `BRITA`, `LISTRADO` ou `XADREZ`, `ObjetoLivre.desenha()` SHALL preencher a forma com `corPimaria` como cor de fundo e, restringindo o desenho seguinte à área da forma (clip), sobrepor um padrão procedural simples e determinístico usando `corSecundaria`: pequenos arcos ondulados horizontais para água; pequenos círculos deslocados para brita; listras retas paralelas para listrado; e uma grade quadriculada alternada para xadrez.

Quando `tipo` é `VEGETACAO_DENSA` ou `VEGETACAO_SIMPLES`, `ObjetoLivre.desenha()` SHALL NÃO preencher a forma com nenhuma cor de fundo — a área do objeto livre permanece transparente onde não há marca — e, em vez disso, SHALL sobrepor marcas individuais baseadas em sprites recoloridos por substituição de matiz/saturação preservando o brilho original de cada pixel (ver `CarregadorRecursos.pintarMonocromatico`), originados de arquivos PNG em `src/main/resources/png/` (nunca gerados/editados em tempo de execução a partir de outro formato):

- **`VEGETACAO_DENSA`**: cada árvore vista de lado é composta por um tronco (sprite `vegetacaoCauleN.png`, N=1..5, pintado com `corPimaria`) e uma copa (sprite `vegetacaoCopaN.png`, N=1..6, pintada com `corSecundaria`), sorteados independentemente (30 combinações possíveis). A ALTURA da árvore é sempre a mesma entre árvores (sem variação de escala). A LARGURA da copa sorteia, por árvore, um fator de esticamento entre a proporção nativa do sprite (mais estreita) e o esticamento total até preencher um envelope quadrado (mais larga) — variação deliberada de combinação visual, não ruído. Árvores candidatas que se sobreporiam a uma já aceita (círculo de exclusão baseado no raio da copa) OU cuja silhueta completa (tronco e copa juntos) não coubesse inteiramente dentro da área da forma são descartadas (a posição fica sem árvore, nunca desenhada parcialmente).
- **`VEGETACAO_SIMPLES`**: cada marca sorteia relva (sprite `vegetacaoRelvaN.png`, N=1..4, pintada com `corPimaria`) OU arbusto (sprite `vegetacaoArbustoN.png`, N=1..4, pintado com `corSecundaria`) — nunca os dois juntos — e uma variante entre as 4 de cada. Os sprites SHALL ser desenhados SEM esticamento: a proporção largura/altura nativa é sempre preservada (a maior dimensão nativa escalada para o tamanho-alvo, a outra proporcionalmente), com uma leve variação de escala uniforme entre marcas. O tamanho de cada marca é 1/3 da largura de referência da copa de `VEGETACAO_DENSA` (calculada com os fatores de densidade da densa, independente da densidade da própria simples). A densidade da grade de `VEGETACAO_SIMPLES` é o dobro da densidade original desse tipo. Marcas de `VEGETACAO_SIMPLES` NÃO têm checagem de sobreposição nem de corte na borda — podem se intersectar livremente entre si e/ou aparecer cortadas pela borda da forma.

O padrão SHALL ser determinístico (sem aleatoriedade não seedada), produzindo o mesmo resultado visual em desenhos sucessivos do mesmo objeto sem "piscar" ou mudar de frame para frame.

#### Scenario: Vegetação densa desenha árvores vistas de lado com tronco e copa a partir de sprites
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_DENSA` é desenhado
- **THEN** a área da forma fica transparente exceto onde há árvore, e cada árvore desenhada tem o tronco (sprite de caule recolorido) preenchido com `corPimaria` e a copa (sprite de copa recolorido) preenchida com `corSecundaria`, com a mesma altura entre árvores mas largura de copa variando por um fator de esticamento sorteado

#### Scenario: Árvore cortada pela borda não é desenhada
- **WHEN** uma árvore candidata, posicionada pela dispersão da grade de `VEGETACAO_DENSA`, teria parte do tronco ou da copa fora da área da forma do objeto livre
- **THEN** essa árvore inteira não é desenhada (nem tronco nem copa aparecem), e a posição correspondente fica sem marca

#### Scenario: Vegetação simples desenha relva e arbusto a partir de sprites, sem esticamento
- **WHEN** um `ObjetoLivre` com `tipo=VEGETACAO_SIMPLES` é desenhado
- **THEN** a área da forma fica transparente exceto onde há marca, e cada marca é uma relva (sprite recolorido com `corPimaria`) OU um arbusto (sprite recolorido com `corSecundaria`), desenhada preservando a proporção largura/altura nativa do sprite (sem esticar), num tamanho igual a 1/3 da largura de referência da árvore de `VEGETACAO_DENSA`

#### Scenario: Marcas de vegetação simples podem se sobrepor e ficar cortadas na borda
- **WHEN** duas marcas de `VEGETACAO_SIMPLES` são posicionadas próximas o bastante para se tocar, ou uma marca cai perto da borda da forma
- **THEN** as marcas são desenhadas normalmente, mesmo se sobrepostas entre si ou parcialmente fora da área visível da forma (sem a checagem de contenção usada por `VEGETACAO_DENSA`)

#### Scenario: Água desenha padrão de ondas
- **WHEN** um `ObjetoLivre` com `tipo=AGUA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos arcos/ondas horizontais usando `corSecundaria`, restrito à área da forma

#### Scenario: Brita desenha padrão de pontos
- **WHEN** um `ObjetoLivre` com `tipo=BRITA` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por um padrão de pequenos círculos deslocados usando `corSecundaria`, restrito à área da forma

#### Scenario: Listrado desenha listras retas paralelas
- **WHEN** um `ObjetoLivre` com `tipo=LISTRADO` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por listras retas paralelas alternando `corSecundaria`, restritas à área da forma

#### Scenario: Xadrez desenha grade quadriculada alternada
- **WHEN** um `ObjetoLivre` com `tipo=XADREZ` é desenhado
- **THEN** a área da forma é preenchida com `corPimaria` e sobreposta por uma grade de células quadradas, alternando `corSecundaria` e a cor de fundo em padrão quadriculado, restrita à área da forma

#### Scenario: Padrão não muda entre desenhos sucessivos
- **WHEN** o mesmo `ObjetoLivre` com `tipo` diferente de `POLIGONO_SIMPLES` é desenhado duas vezes seguidas sem alteração de posição, tamanho, ângulo ou tipo
- **THEN** o padrão procedural resultante é idêntico nas duas renderizações, incluindo qual combinação de sprites e qual fator de esticamento foi sorteado para cada árvore/marca no caso de `VEGETACAO_DENSA`/`VEGETACAO_SIMPLES`
