## Context

`ObjetoTransparencia` recorta um "buraco" no sprite do carro (visão de cima) via `desenhaCarro(Graphics2D, double zoom, int carroX, int carroY)` ([ObjetoTransparencia.java:58](src/main/java/br/f1mane/entidades/ObjetoTransparencia.java:58)). Três lugares no Swing chamam esse método para três "carros" diferentes:

1. **Piloto** — `PainelCircuito.desenhaCarroCima` ([PainelCircuito.java:3336-3379](src/main/java/br/f1mane/visao/PainelCircuito.java:3336)). Antes de chamar `desenhaCarro`, filtra por:
   - `transparenciaBox`: se o objeto é "só de box" e o piloto não está nos nós de box (`controleJogo.obterPista(piloto.getNoAtual()) != controleJogo.getNosDoBox()`), pula.
   - intervalo de nó: se `inicioTransparencia != 0 && fimTransparencia != 0`, só aplica quando `indexNoAtual` (índice do nó atual do piloto) está dentro de `[inicio, fim]`.
2. **Safety car** — `PainelCircuito.desenharSafetyCarCima` ([PainelCircuito.java:4642](src/main/java/br/f1mane/visao/PainelCircuito.java:4642), loop em 4733-4748). Filtra só por `isPintaEmcima()` e por o objeto estar no viewport — **não** verifica `transparenciaBox` nem o intervalo de nó. O índice atual do safety car já está disponível na variável local `cont` (linha 4668, `noAtual.getIndex()`).
3. **Preview de teste do editor** — `MainPanelEditor.desenhaCarroTeste` ([MainPanelEditor.java:3603-3659](src/main/java/br/f1mane/editor/MainPanelEditor.java:3603)). Não filtra nada — aplica `desenhaCarro` para todo `ObjetoTransparencia` do circuito, sempre. `TestePista` ([TestePista.java](src/main/java/br/f1mane/editor/TestePista.java)), que controla a posição do carro de teste, hoje só expõe `getTestCar()` (um `Point`); não expõe o índice de nó atual (`cont`, interno à thread de teste em `iniciarTeste`) nem se o carro de teste está no trecho de box (`irProBox` reflete a *intenção* de ir para o box, não “está nos nós de box agora”).

Resultado: o mesmo `ObjetoTransparencia` com um intervalo de nó restrito (ex.: Suzuka, `inicioTransparencia=6874` / `fimTransparencia=7172`) corta o sprite do piloto só naquele trecho, mas corta o sprite do safety car e do carro de teste em qualquer lugar do circuito.

## Goals / Non-Goals

**Goals:**
- Safety car e preview de teste do editor respeitarem `inicioTransparencia`/`fimTransparencia`/`transparenciaBox` com a mesma semântica já usada para o piloto.
- Consolidar a checagem em um único método reutilizado pelos três lugares no Swing, para não haver uma quarta divergência futura.

**Non-Goals:**
- Mudar `vdp.js`/`Circuito.java`/`ObjetoPistaJSon` — o caminho web do piloto já está correto, e nem safety car nem o preview do editor existem no cliente web.
- Mudar o comportamento do piloto normal (já correto, serve de referência).
- Revisar a semântica de índice em si (ex.: como índices de box e de pista interagem) além do necessário para dar paridade ao safety car e ao carro de teste — isso já é o comportamento existente para o piloto e não é escopo desta mudança.
- Mudar a lógica de movimento do safety car ou do carro de teste.

## Decisions

**Extrair um método utilitário único em `PainelCircuito`** — algo como `private boolean transparenciaAplicavel(ObjetoPista objetoPista, int indexAtual, boolean estaNoBox)` — que encapsula as duas checagens (`transparenciaBox` vs. `estaNoBox`, e intervalo `[inicio, fim]` vs. `indexAtual`, com a regra "0/0 = sem filtro, aplica sempre"). `desenhaCarroCima` e `desenharSafetyCarCima` passam a chamar esse método antes de `desenhaCarro`.
- Alternativa considerada: duplicar a mesma checagem inline em `desenharSafetyCarCima`, copiando o padrão de `desenhaCarroCima`. Rejeitada porque é exatamente esse tipo de duplicação que já causou a divergência atual — outro ponto que precise do mesmo filtro no futuro (ex.: um terceiro carro/replay) corre o mesmo risco de esquecer uma das duas condições.

**Expor o índice de nó atual e o estado "está no box" em `TestePista`** — adicionar um campo (ex.: `indexAtual`) atualizado junto com `testCar` em `posicionaCarro`, `posicionaCarroConsiderandoEscapada` e `posicionaCarroBox`, mais um booleano (ex.: `estaNoBox`) que é `true` somente durante o trecho em que a thread de teste está iterando `pontosBox` (dentro do `if (irProBox && ...)` em `iniciarTeste`) e `false` no restante. `MainPanelEditor.desenhaCarroTeste` lê esses dois valores de `testePista` e usa o mesmo utilitário de `PainelCircuito` (ou uma cópia local equivalente, já que os dois arquivos não compartilham uma classe-mãe hoje) antes de chamar `desenhaCarro`.
- Alternativa considerada: recalcular o nó mais próximo a partir de `testePista.getTestCar()` (um `Point`) buscando na lista de nós do circuito. Rejeitada — mais caro (busca linear a cada frame) e menos preciso que simplesmente propagar o índice que a própria thread de teste já sabia no momento em que posicionou o carro.
- Alternativa considerada: não tratar o caso de box no preview do editor (só o intervalo de nó). Rejeitada — deixaria o preview ainda divergente do jogo real para objetos `transparenciaBox=true`, que é exatamente o tipo de objeto usado nas linhas de box.

**Reaproveitar a fórmula existente**, não redesenhar a semântica: mesma regra "0/0 = sem filtro, sempre aplica" para manter XMLs antigos sem esses campos funcionando como hoje.

## Risks / Trade-offs

- [`PainelCircuito` e `MainPanelEditor` não compartilham uma classe base para o novo utilitário] → aceitar uma pequena duplicação de ~10 linhas entre os dois arquivos (o método é puro/sem estado, baixo risco de divergir de novo) em vez de forçar uma abstração cross-módulo (editor depende de `visao`, mas o inverso não é natural) só para esta função.
- [`indexAtual` de `TestePista` pode ficar dessincronizado de `testCar` se algum caminho novo mudar `testCar` sem atualizar o índice] → todos os pontos que hoje setam `testCar` (`posicionaCarro`, `posicionaCarroBox`, `posicionaCarroConsiderandoEscapada`) são de um único arquivo pequeno (`TestePista.java`); revisão do PR deve conferir que os três foram atualizados juntos.
- [Objetos com `inicioTransparencia`/`fimTransparencia` pensados só em índices da pista principal podem se comportar de forma pouco intuitiva quando o safety car ou o carro de teste estão nos nós de box, já que a numeração de índice de box é independente da pista] → mesmo comportamento observado hoje no piloto (não é regressão introduzida por esta mudança); fora de escopo corrigir aqui.

## Open Questions

- Nenhuma pendente — implementação é direta a partir do padrão já existente em `desenhaCarroCima`.
