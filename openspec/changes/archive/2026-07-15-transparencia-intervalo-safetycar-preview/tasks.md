## 1. Utilitário compartilhado em `PainelCircuito`

- [x] 1.1 Extrair de `desenhaCarroCima` a lógica de filtro (`transparenciaBox` vs. está-no-box; `inicioTransparencia`/`fimTransparencia` vs. índice atual) para um método privado reutilizável, ex.: `private boolean transparenciaAplicavel(ObjetoPista objetoPista, int indexAtual, boolean estaNoBox)`, preservando a regra "0/0 = sem filtro, sempre aplica".
- [x] 1.2 Atualizar `desenhaCarroCima` para chamar o novo método no lugar do código inline, sem mudar o comportamento observável.

## 2. Safety car respeita o intervalo e a restrição de box

- [x] 2.1 Em `desenharSafetyCarCima`, antes de chamar `objetoTransparencia.desenhaCarro(...)` no loop (em torno da linha 4733), calcular `estaNoBox` a partir de `controleJogo.obterPista(safetyCar.getNoAtual()) == controleJogo.getNosDoBox()` (mesmo padrão usado para o piloto) e usar o índice já disponível na variável local do nó atual do safety car.
- [x] 2.2 Chamar `transparenciaAplicavel(...)` (do item 1.1) e pular o objeto (`continue`) quando retornar `false`, mantendo os filtros existentes (`isPintaEmcima()`, viewport).

## 3. `TestePista` expõe índice de nó atual e estado de box

- [x] 3.1 Adicionar campo `indexAtual` (int) e `estaNoBox` (boolean) a `TestePista`, com getters.
- [x] 3.2 Atualizar `posicionaCarro` e `posicionaCarroConsiderandoEscapada` para setar `indexAtual = cont` e `estaNoBox = false`.
- [x] 3.3 Atualizar `posicionaCarroBox` (chamada durante o trecho de box em `iniciarTeste`) para setar `indexAtual = cont` (índice dentro de `pontosBox`) e `estaNoBox = true`.
- [x] 3.4 Conferir que os três pontos que hoje setam `testCar` foram todos atualizados, para `indexAtual`/`estaNoBox` nunca ficarem dessincronizados dele.

## 4. Preview de teste do editor respeita o intervalo e a restrição de box

- [x] 4.1 Em `MainPanelEditor.desenhaCarroTeste` (em torno da linha 3650), antes de chamar `objetoTransparencia.desenhaCarro(...)`, ler `testePista.getIndexAtual()` e `testePista.isEstaNoBox()`.
- [x] 4.2 Aplicar a mesma checagem de filtro do item 1.1 (reimplementada localmente em `MainPanelEditor`, já que `editor` não depende de `visao`, conforme decidido em design.md) e pular o objeto (`continue`) quando o filtro não passar.

## 5. Testes

- [x] 5.1 Teste unitário (ou de integração leve) cobrindo `transparenciaAplicavel` em `PainelCircuito`: objeto sem intervalo (0/0) sempre aplica; objeto com intervalo aplica só dentro do range; objeto com `transparenciaBox=true` só aplica quando `estaNoBox=true`.
- [x] 5.2 Teste cobrindo que `desenharSafetyCarCima` não desenha o recorte de transparência quando o safety car está fora do intervalo de nó de um `ObjetoTransparencia` configurado.
- [x] 5.3 Teste cobrindo que `TestePista` atualiza `indexAtual`/`estaNoBox` corretamente ao posicionar o carro de teste na pista normal, no box e (se aplicável) em modo escapada.
- [x] 5.4 Teste cobrindo que `MainPanelEditor.desenhaCarroTeste` não desenha o recorte de transparência quando o carro de teste está fora do intervalo de nó configurado, usando o padrão de teste double existente (sem abrir diálogo/Swing real) descrito em CLAUDE.md.

## 6. Validação manual

- [x] 6.1 Rodar `mvn test` e garantir que a suíte passa.
- [x] 6.2 Abrir o editor com um circuito que já usa `inicioTransparencia`/`fimTransparencia` (ex.: Suzuka) e confirmar visualmente que o preview do carro de teste só mostra o recorte de transparência no trecho configurado. (Conferido manualmente pelo usuário no editor real.)

## 7. Correção pós-validação: paridade quebrada pelo nível de desenho padrão

Usuário reportou que a transparência parou de aparecer no Swing depois das duas mudanças aplicadas nesta sessão. Investigação (script `CarregadorRecursos.carregarCircuito("suzuka_mro.xml")` + `transparenciaAplicavel` via reflection) confirmou `transparenciaAplicavel` correto com dados reais do Suzuka (índices 6874–7172 aplicam corretamente, fora do intervalo não aplica). Causa raiz encontrada: `desenharSafetyCarCima` tinha uma checagem pré-existente `if (objetoPista.isPintaEmcima()) continue;` que o piloto (`desenhaCarroCima`) nunca teve. Como `nivelDesenho >= 1` sempre implica `isPintaEmcima() == true` (`ObjetoPista.setNivelDesenho`), e a mudança `editor-marcadores-transparencia` passou a criar todo `ObjetoTransparencia` novo com `nivelDesenho = 100`, essa checagem antiga passou a descartar o recorte do safety car para qualquer objeto novo — quebrando a paridade com o piloto que era o próprio objetivo desta mudança.

- [x] 7.1 Remover a checagem `isPintaEmcima()` de `desenharSafetyCarCima` — `transparenciaAplicavel` já é a única autoridade sobre quando o recorte se aplica, igual ao piloto.
- [x] 7.2 Teste de regressão: objeto com `nivelDesenho=100` (padrão de objeto novo) ainda aplica o recorte no safety car.
- [x] 7.3 Confirmado: `vdp.js`/`Circuito.gerarObjetosNoTransparencia()` não têm equivalente a `isPintaEmcima()` (nível de desenho nunca é serializado em `ObjetoPistaJSon`), então não têm esse bug — mecanismo de alfa (`destination-out` em `vdp_blend`) conferido e intacto.
- [x] 7.4 `mvn test` (693 testes) continua passando.
