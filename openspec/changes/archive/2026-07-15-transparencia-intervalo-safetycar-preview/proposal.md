## Why

`ObjetoTransparencia` recorta um "buraco" transparente no sprite do carro visto de cima quando ele passa sob uma estrutura (arquibancada, ponte etc.). Os campos `inicioTransparencia`/`fimTransparencia` (índices de nó da pista) e `transparenciaBox` existem para limitar esse recorte apenas ao trecho/local onde a estrutura realmente existe. Esse filtro é aplicado corretamente no carro do piloto normal (`PainelCircuito.desenhaCarroCima` e `vdp.js` `vdp_blend`), mas dois outros pontos que desenham o mesmo recorte — o safety car e o preview de "carro de teste" do editor — ignoram esses campos e aplicam o recorte de transparência para **qualquer** `ObjetoTransparencia` do circuito, em qualquer posição. Isso faz o safety car ficar visualmente "furado" fora do trecho configurado, e faz o editor mostrar um preview que não reflete o comportamento real de corrida — enganando quem está configurando o circuito.

## What Changes

- Aplicar o mesmo filtro de `inicioTransparencia`/`fimTransparencia`/`transparenciaBox` já usado em `desenhaCarroCima` (piloto) também no desenho do safety car em `PainelCircuito.java` (em torno da linha 4733).
- Aplicar esse mesmo filtro no preview de "carro de teste" do editor (`MainPanelEditor.desenhaCarroTeste`, em torno da linha 3650), usando o nó da pista mais próximo da posição do carro de teste como referência de índice.
- Extrair a checagem de intervalo (`indexNoAtual < inicio || indexNoAtual > fim`, com a regra de "0/0 = sem filtro") para um método utilitário único reaproveitado pelos três pontos de desenho no Swing, evitando que a lógica duplicada volte a divergir no futuro.
- Sem mudanças no lado do servidor/JSON (`Circuito.java`) nem no `vdp.js` — o caminho web do piloto já está correto e não é afetado pelo safety car (que não é renderizado no cliente web) nem pelo editor (Swing-only).

## Capabilities

### New Capabilities
- `transparencia-intervalo-no`: comportamento de renderização do `ObjetoTransparencia` — o recorte de transparência no sprite do carro (visto de cima) só se aplica quando o nó atual do carro está dentro de `[inicioTransparencia, fimTransparencia]` (ou sempre, se ambos forem 0), e isso vale de forma consistente para piloto, safety car e preview de teste do editor.

### Modified Capabilities
(nenhuma — não há spec existente descrevendo esse comportamento hoje)

## Impact

- `src/main/java/br/f1mane/visao/PainelCircuito.java` — método de desenho do safety car (top-view), e possível extração de utilitário compartilhado com `desenhaCarroCima`.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java` — método `desenhaCarroTeste`.
- Sem impacto em `vdp.js`, `Circuito.java`/`ObjetoPistaJSon` ou nos XMLs de circuito existentes (nenhuma migração de dado necessária).
