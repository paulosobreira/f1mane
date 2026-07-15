## Why

Hoje, configurar um `ObjetoTransparencia` no editor é feito às cegas: (1) o objeto entra com nível de desenho 0, podendo ficar escondido atrás de outros objetos de cenário; (2) o traço usado enquanto o usuário desenha o polígono é uma linha preta fina, sem destaque (diferente de `ObjetoLivre`, que já usa traço grosso magenta); (3) os campos `inicioTransparencia`/`fimTransparencia` (índice de nó da pista que limita onde o recorte de transparência se aplica — ver mudança `transparencia-intervalo-safetycar-preview`) precisam ser digitados manualmente no formulário, sem nenhuma pista visual de qual trecho da pista esses números realmente representam, e sem nenhum cálculo automático a partir de onde o objeto foi desenhado. Isso torna fácil configurar um intervalo errado (ou esquecer de configurar) sem perceber até testar a corrida.

## What Changes

- Novo `ObjetoTransparencia` criado no editor passa a ter nível de desenho padrão 100 (bem acima de qualquer objeto de cenário comum), garantindo que ele sempre desenhe por cima de tudo na tela do editor.
- O traço usado ao desenhar o polígono de um `ObjetoTransparencia` (preview enquanto o usuário clica os pontos) passa a usar cor destacada e traço mais grosso, no mesmo padrão já usado por `ObjetoLivre` (hoje é uma linha preta fina, sem marcador de vértice).
- `inicioTransparencia` e `fimTransparencia` passam a ser calculados automaticamente a partir dos nós da pista cobertos pela área do objeto, tanto ao terminar de desenhá-lo quanto sempre que ele for reposicionado (arrastar, setas, ou alterar largura/altura/ângulo pelo menu de contexto) — mesmo padrão de recálculo automático já usado para `ObjetoEscapada` (`reprocessaEscapadaSeNecessario`).
- Esses índices continuam editáveis manualmente: o painel de ajuste rápido (menu de contexto) já tem campos para eles; o valor calculado automaticamente é só o ponto de partida, não trava edição manual.
- Quando um `ObjetoTransparencia` está selecionado (e só nesse caso), o editor desenha dois marcadores tracejados perpendiculares à pista, no mesmo estilo/posicionamento da linha de largada: um em magenta no nó de `inicioTransparencia`, outro em verde no nó de `fimTransparencia`. Nenhum marcador é desenhado quando o objeto não está selecionado, nem quando `inicioTransparencia == 0 && fimTransparencia == 0` (sem intervalo configurado).

## Capabilities

### New Capabilities
- `editor-marcadores-transparencia`: comportamento do editor de circuitos ao autorar um `ObjetoTransparencia` — nível de desenho padrão, traço de desenho destacado, cálculo automático do intervalo de nó, e marcadores visuais tracejados de início/fim quando selecionado.

### Modified Capabilities
(nenhuma — não há spec existente descrevendo esse fluxo de autoria)

## Impact

- `src/main/java/br/f1mane/editor/MainPanelEditor.java` — finalização do desenho do polígono (nível padrão, cálculo automático de intervalo), `desenhaPreObjetoTransparencia` (traço destacado), pontos de reposicionamento (`mouseDragged`, `esquerdaObj`/`direitaObj`/`cimaObj`/`baixoObj`, spinners de largura/altura/ângulo em `criaPainelAjusteRapido`), e um novo método de desenho dos marcadores de início/fim.
- Sem impacto em `PainelCircuito.java` (renderização de jogo), `vdp.js`, `Circuito.java`/serialização JSON, ou no formato de XML dos circuitos — os campos `inicioTransparencia`/`fimTransparencia`/`nivelDesenho` já existem e continuam com o mesmo significado; só passam a ganhar um valor inicial calculado em vez de ficarem zerados por padrão.
