## 1. Suavização do traçado (lógica compartilhada)

- [x] 1.1 Em `DesenhoProceduralCircuito`, implementar a conversão de uma lista de `No` (com wrap circular para caminho fechado, ou condição de extremidade clampeada para caminho aberto) em pontos de controle de Bézier via Catmull-Rom centrípeta.
- [x] 1.2 Construir o `GeneralPath` suavizado da pista (`pistaKey`, fechado) usando `moveTo`/`curveTo` a partir dos pontos de controle da tarefa 1.1, substituindo os `drawLine` retos de `desenhaPista`.
- [x] 1.3 Construir o `GeneralPath` suavizado do box (`boxKey`, aberto) da mesma forma, substituindo os `drawLine` retos de `desenhaPistaBox`/`desenhaTintaPistaBox` (ver decisão de design sobre aplicar a mesma suavização ao box).
- [x] 1.4 Validar visualmente (ou com teste) que os pontos suavizados passam exatamente pelos nós-chave originais e que não há overshoot perceptível em pelo menos 2-3 circuitos existentes com curvas/chicanes (`src/main/resources/circuitos/*.xml`).

## 2. Zebra contínua por trecho de curva

- [x] 2.1 Agrupar nós-chave consecutivos classificados como `CURVA_ALTA`/`CURVA_BAIXA` em trechos contíguos, preservando a fronteira exata curva/reta que existe hoje.
- [x] 2.2 Para cada trecho contíguo de curva, construir um único `GeneralPath` suavizado (reaproveitando a lógica da tarefa 1.1/1.2) e desenhar a faixa de zebra tracejada com uma única chamada `g2d.draw`, em vez de um `drawLine` por par de nós.
- [x] 2.3 Confirmar que a tinta de borda branca fora das curvas (`corZebra` não aplicada) continua desenhada como antes, só que sobre o caminho suavizado.
- [x] 2.4 Validar visualmente que o padrão de listras fica alinhado/contínuo do início ao fim de cada trecho de curva, sem reinício nas emendas entre nós-chave internos.

## 3. Unificação `PainelCircuito` / `DesenhoProceduralCircuito`

- [x] 3.1 Expor os métodos de construção de caminho suavizado (tarefas 1 e 2) como estáticos em `DesenhoProceduralCircuito`, recebendo `zoom` e um deslocamento de centralização (`Point`, default `(0,0)`) — mesmo padrão já usado por `desenhaLinhaDeLargada`.
- [x] 3.2 Atualizar `PainelCircuito.desenhaPista`, `desenhaTintaPistaZebra`, `desenhaPistaBox`, `desenhaTintaPistaBox` para delegar aos métodos compartilhados de `DesenhoProceduralCircuito`, em vez de manter a implementação duplicada.
- [x] 3.3 Remover o código de construção de caminho reto agora morto em `PainelCircuito` e `DesenhoProceduralCircuito` (a versão antiga segmento a segmento).
- [x] 3.4 Conferir visualmente que o preview do editor e a imagem gerada em memória (`DesenhoProceduralCircuito.geraImagem`) ficam idênticos para o mesmo circuito.

## 4. Testes e regressão

- [x] 4.1 Revisar `CircuitoCoresBoxZebraTest` e `DesenhoProceduralCircuitoTest` (e outros testes que dependem do desenho de pista/zebra/box) e ajustar as asserções para o novo caminho suavizado, sem perder a cobertura das cores customizáveis (`corZebra1`/`corZebra2`/`corBox1`/`corBox2`) e do fallback branco/vermelho.
- [x] 4.2 Rodar `mvn test` e confirmar que a suíte completa passa.
- [x] 4.3 Rodar `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar` e testar manualmente no editor de circuitos (`java -cp target/flmane.jar br.flmane.editor.EditorCircuitos`), abrindo pelo menos um circuito com curvas/chicanes conhecidas por mostrar o problema relatado.
