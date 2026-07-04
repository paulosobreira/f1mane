## Why

Hoje as marcas de pneu ("travada de roda", `travouRodas`/`TravadaRoda`, pintadas permanentemente no `backGround` do circuito por `PainelCircuito.adicionatrvadaRoda`) e as faíscas concentram-se quase todas em cima dos próprios nós de curva — sobretudo `CURVA_BAIXA` (`Piloto.processaEscapadaDaPista`, linha ~1592) e, secundariamente, num acionamento condicional dentro de `Piloto.processaFreioNaReta` (estresse alto + modo agressivo). Isso não reflete como frenagens reais acontecem: o piloto trava as rodas e deixa marca principalmente na reta de frenagem que antecede uma curva fechada, não dentro da própria curva. `processaFreioNaReta` já calcula uma janela fixa de 300 nós até a próxima curva (via `mapaNoProxCurva`, que aponta pra curva mais próxima de qualquer tipo), mas essa janela não distingue uma reta de frenagem de verdade (muita reta, poucas/nenhuma curva alta, e uma quantidade relevante de nós de curva baixa formando a curva fechada) de uma aproximação a uma chicane ou esse rápido.

## What Changes

- Introduz o conceito de **zona de frenagem**: um trecho da pista detectado automaticamente a partir do padrão dos nós — uma quantidade considerável de nós de reta, poucos ou nenhum nó de curva alta, e uma quantidade relativa relevante de nós de curva baixa mais à frente (não apenas um ou dois nós isolados) — indicando uma curva fechada se aproximando sem curvas rápidas intermediárias.
- Refatora `Piloto.processaFreioNaReta` (e os campos `freiandoReta`/`retardaFreiandoReta`) para usar a zona de frenagem detectada em vez da janela fixa de 300 nós baseada só na distância até a próxima curva de qualquer tipo.
- Redistribui a intensidade de marcas de pneu e faíscas: reduz para ~20-30% do que é hoje a chance de gerar marca/faísca em cima dos próprios nós de curva (`CURVA_BAIXA`/`CURVA_ALTA`), e concentra a maior parte das travadas de roda, marcas de pneu e faíscas dentro da zona de frenagem.
- Adiciona, no editor de circuitos, uma marcação visual (cor diferenciada, por exemplo um tom mais acinzentado) sobre o trecho da pista identificado como zona de frenagem, pra o projetista do circuito conseguir ver onde o algoritmo detectou a zona — sem afetar a geração da imagem usada em corrida real (nem o modo de geração em memória).

**Removido durante a implementação**: a proposta original incluía um flag de sessão do editor ("Faísca e Marcas de Pneu") que fazia o teste de pista (`TestePista`) simular marcas de pneu/faíscas ao passar pela zona de frenagem. Essa simulação foi implementada, mas depois retirada a pedido: desenhar faísca/marca de pneu não é relevante para o editor de circuitos, que serve pra desenhar o traçado e posicionar objetos, não pra prever o comportamento visual de uma corrida. A marcação visual da zona de frenagem (item acima) permanece — ela é sobre onde a zona está, não sobre simular o efeito de uma corrida.

## Capabilities

### New Capabilities
- `zona-frenagem-deteccao`: detecção automática de zonas de frenagem a partir do padrão de nós da pista (reta → poucas/nenhuma curva alta → quantidade relevante de curva baixa), e uso dessa zona por `Piloto.processaFreioNaReta` para substituir a janela fixa de 300 nós.
- `zona-frenagem-marcas-intensidade`: redistribuição da intensidade de marcas de pneu/faíscas entre nós de curva (reduzidos) e zona de frenagem (aumentada), preservando o restante do comportamento de `travouRodas`/`processaFaiscas`.
- `zona-frenagem-visualizacao-editor`: marcação visual (cor diferenciada) do trecho de pista identificado como zona de frenagem no editor de circuitos, sempre visível ao editar, sem alterar a geração da imagem usada em corrida real.

### Modified Capabilities
(nenhuma — não há spec existente cobrindo o comportamento atual de frenagem/marcas de pneu; `sdd-game-engine`/`sdd-rendering` são specs de documentação estrutural, não de regras de jogo)

## Impact

- `src/main/java/br/f1mane/entidades/Piloto.java`: `processaFreioNaReta()` passa a consultar a zona de frenagem em vez de só `controleJogo.obterProxCurva`/janela fixa de 300; `processaFaiscas()` e o gatilho de `travouRodas(this)` dentro de `processaEscapadaDaPista()`/`processaFreioNaReta()` passam a considerar se o nó atual está dentro da zona de frenagem antes de aplicar a chance de marca/faísca.
- `src/main/java/br/f1mane/controles/ControleRecursos.java`: ao lado de `mapaNoProxCurva`/`mapaNoCurvaAnterior` (calculados em `vetorizarPista`/setup do circuito), adiciona o cálculo da zona de frenagem por nó (`Set<No>` calculado por `calculaZonaFrenagem`, também exposto como método estático reutilizável pelo editor).
- `src/main/java/br/f1mane/controles/ControleJogoLocal.java`: `travouRodas(Piloto, boolean)` passa a considerar a zona de frenagem ao decidir intensidade/quantidade de fumaça, não só o tipo do nó atual.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: nova rotina de desenho (`desenhaZonaFrenagemOverlay`) que tinge/marca visualmente os nós de pista pertencentes à zona de frenagem, sempre visível quando o traçado está sendo mostrado. (Não há mais nenhum código de simulação de faísca/marca de pneu no editor — removido.)
