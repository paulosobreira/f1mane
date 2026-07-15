## 1. Nível de desenho padrão

- [x] 1.1 Em `MainPanelEditor`, no ponto de finalização do desenho de `ObjetoTransparencia` (junto de `objetoTransparencia.setTransparencia(125)`, em torno da linha 1907), chamar `objetoTransparencia.setNivelDesenho(100)`.

## 2. Traço de preview destacado

- [x] 2.1 Atualizar `desenhaPreObjetoTransparencia` para salvar `Stroke`/`Color` anteriores, usar `Color.CYAN` + `BasicStroke(3f)` (ou traço equivalente, distinto do magenta/verde dos marcadores de intervalo), desenhar um marcador (círculo) em cada vértice já clicado, e restaurar `Stroke`/`Color` no fim — mesmo padrão de `desenhaPreObjetoLivre`.

## 3. Cálculo automático de início/fim

- [x] 3.1 Adicionar `recalculaIntervaloTransparencia(ObjetoTransparencia objeto)` em `MainPanelEditor`: itera `circuito.getPistaFull()`, filtra nós cujo `getPoint()` cai dentro de `objeto.obterArea()`, define `inicioTransparencia`/`fimTransparencia` como o menor/maior `getIndex()` encontrado (ou `0`/`0` se nenhum nó cair na área).
- [x] 3.2 Adicionar `recalculaTransparenciaSeNecessario(ObjetoPista alvo)` que chama `recalculaIntervaloTransparencia` quando `alvo instanceof ObjetoTransparencia`, seguindo o padrão de `reprocessaEscapadaSeNecessario`.
- [x] 3.3 Chamar `recalculaTransparenciaSeNecessario` no ponto de finalização do desenho do polígono (mesmo ponto do item 1.1).
- [x] 3.4 Chamar `recalculaTransparenciaSeNecessario` em `mouseDragged` (junto de `reprocessaEscapadaSeNecessario(objetoArrastando)`), em `esquerdaObj`/`direitaObj`/`cimaObj`/`baixoObj`, e nos spinners de largura/altura/ângulo em `criaPainelAjusteRapido`.

## 4. Marcadores tracejados de início/fim

- [x] 4.1 Adicionar `desenhaMarcadoresIntervaloTransparencia(Graphics2D g2d)` em `MainPanelEditor`: obtém o objeto selecionado via `primeiroSelecionado(formularioListaObjetos)`; retorna sem desenhar nada se não for `ObjetoTransparencia`, ou se `inicioTransparencia == 0 && fimTransparencia == 0`.
- [x] 4.2 Para cada índice (início/fim) com bounds-check contra `circuito.getPistaFull()`, calcular o ângulo local (nó antes/depois) e o traço perpendicular cobrindo a largura da pista, seguindo o mesmo cálculo geométrico já usado em `desenhaCarroTeste` (`GeoUtil.calculaAngulo`/`GeoUtil.calculaPonto`).
- [x] 4.3 Desenhar o traço do início com `BasicStroke` tracejado (dash array) em `Color.MAGENTA`, e o do fim na mesma espessura/estilo em verde (ex.: `new Color(0, 170, 0)`).
- [x] 4.4 Chamar `desenhaMarcadoresIntervaloTransparencia` a partir de `paintComponent`, junto dos outros métodos `desenhaPreObjeto*`.

## 5. Testes

- [x] 5.1 Teste cobrindo que finalizar o desenho de um `ObjetoTransparencia` define `nivelDesenho == 100`.
- [x] 5.2 Teste cobrindo `recalculaIntervaloTransparencia`: objeto cobrindo um trecho conhecido da pista principal resulta no menor/maior índice esperado; objeto fora da pista resulta em `0`/`0`.
- [x] 5.3 Teste cobrindo que mover (`mouseDragged`/setas) ou redimensionar um `ObjetoTransparencia` já existente recalcula `inicioTransparencia`/`fimTransparencia`, inclusive sobrescrevendo um valor manual anterior.
- [x] 5.4 Teste cobrindo que `desenhaMarcadoresIntervaloTransparencia` não desenha nada quando o objeto não está selecionado, nem quando o intervalo é `0`/`0`.

## 6. Validação manual

- [x] 6.1 Rodar `mvn test` e garantir que a suíte passa.
- [x] 6.2 No editor, desenhar um novo objeto de transparência sobre um trecho da pista e conferir visualmente: nível por cima de outros objetos, traço destacado durante o desenho, intervalo calculado automaticamente, e marcadores magenta/verde aparecendo só quando o objeto está selecionado. (Conferido manualmente pelo usuário no editor real.)
