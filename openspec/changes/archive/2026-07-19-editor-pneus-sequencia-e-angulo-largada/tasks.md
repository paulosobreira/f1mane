## 1. Facilitador de pares de cliques para Pneus

- [x] 1.1 Novos campos `private boolean desenhandoPneusEmSequencia;` e `private Point primeiroCliquePneus;` em `MainPanelEditor`.
- [x] 1.2 `iniciarCriacaoObjeto()`: novo `else if (objetoPista instanceof ObjetoPneus)` zerando o rascunho (`objetoPista = null`) e ligando `desenhandoPneusEmSequencia=true`/`primeiroCliquePneus=null` (mantendo `posicionaObjetoPista=true`, já ligado antes do if/else para todos os tipos).
- [x] 1.3 Novo ramo em `clickEditarObjetos` (`else if (posicionaObjetoPista && desenhandoPneusEmSequencia)`): botão direito encerra a corrente sem criar objeto; primeiro clique da corrente só guarda `primeiroCliquePneus`; qualquer clique seguinte fecha um segmento (cria o `ObjetoPneus`, decisão 2 do design) e o próprio clique vira o novo ponto pendente (`primeiroCliquePneus = ultimoClicado`, corrente contínua, ver task 6.2 pela revisão desse comportamento).
- [x] 1.4 Criação do objeto ao fechar um segmento: `MemoriaPropriedadesObjeto.aplicar(novo)` primeiro (altura/cores do template), depois `setLargura`/`setAngulo` derivados do segmento (`distancia(...)`/`GeoUtil.calculaAngulo(...)`, já existentes), `setPosicaoQuina(...)` calculada pra o segmento virar a linha de centro do objeto (ver task 6.1 pela correção desse cálculo), adiciona a `circuito.getObjetosCenario()`, nome/lista/filtro/auto-save.
- [x] 1.5 Import `br.flmane.entidades.ObjetoPneus` em `MainPanelEditor.java`.

## 2. Testes do facilitador de Pneus

- [x] 2.1 Criar `src/test/java/br/flmane/editor/MainPanelEditorCriarPneusSequenciaTest.java` cobrindo: dois cliques criam um objeto com largura/ângulo corretos; clique direito encerra sem criar; altura/cores vêm do template lembrado enquanto largura/ângulo vêm dos cliques; o objeto criado é um `ObjetoPneus` comum (mesma classe, mesmos campos) — ver seção 6 pela reescrita completa cobrindo a corrente contínua e a correção de ângulo.
- [x] 2.2 Rodar `mvn test` e confirmar que a suíte completa continua passando.

## 3. Ângulo da linha de largada em `Circuito`

- [x] 3.1 Novo campo `@JsonIgnore private Double anguloLargada;` em `Circuito.java`, com getter/setter.
- [x] 3.2 Incluir `anguloLargada` em `copiaParaArquivoMetadados()`.
- [x] 3.3 `DesenhoProceduralCircuito.desenhaLinhaDeLargada(...)`: usar `circuito.getAnguloLargada()` quando não nulo, senão o cálculo automático existente.
- [x] 3.4 Novo helper público `DesenhoProceduralCircuito.calculaAnguloNaturalLargada(Circuito)` (mesma busca de nó de largada/vizinho + cálculo de ângulo, sem considerar o override), usado pelo editor pra pré-preencher o spinner.

## 4. Spinner de ângulo da largada no editor

- [x] 4.1 Novo campo `private JSpinner anguloLargadaSpinner;` em `MainPanelEditor`.
- [x] 4.2 Em `gerarTopoNavegacaoEAcoes()`, construir o spinner (`SpinnerNumberModel` 0-360, passo 1) com valor inicial resolvido via `circuito.getAnguloLargada()` ou `DesenhoProceduralCircuito.calculaAnguloNaturalLargada(circuito)` (guardando `circuito == null`), normalizado pra 0-360 (`normalizaAngulo`, novo helper) antes de anexar o `ChangeListener` — sem sincronização adicional em `refletirCircuitoNosCampos()` (ver design.md decisão 4).
- [x] 4.3 `ChangeListener` do spinner grava `circuito.setAnguloLargada(...)` e chama `repaint()`.
- [x] 4.4 Posicionar o spinner na linha 1 do topo, logo após o campo "largura" (mesmo `adicionaParTopo(...)`), com o `JLabel` da nova chave `anguloLargada`.
- [x] 4.5 Nova chave `anguloLargada` em `mensagens_pt.properties`, `mensagens_en.properties`, `mensagens_es.properties` e `mensagens_it.properties`.

## 5. Testes do ângulo da largada

- [x] 5.1 `DesenhoProceduralCircuitoTest.java`: `calculaAnguloNaturalLargada` retorna `null` sem nó de largada e calcula a direção local corretamente com nó de largada; `desenhaLinhaDeLargada` com override muda a orientação do desenho (comparação de imagens), e sem override reproduz exatamente o mesmo desenho que um override igual ao valor calculado.
- [x] 5.2 `CircuitoMetadadosArquivoTest.java`: sem `anguloLargada` definida, a propriedade não aparece no XML gerado; com `anguloLargada` definida, aparece com o valor correto.
- [x] 5.3 `MainPanelEditorTopoNavegacaoTest.java`: ajustar o teste existente que esperava 1 spinner na linha 1 pra 2 (largura + ângulo da largada); novo teste confirmando a faixa 0-360 do spinner de ângulo.
- [x] 5.4 Rodar `mvn test` e confirmar que a suíte completa continua passando (801 testes).

## 6. Correções pós-teste manual (feedback do usuário)

- [x] 6.1 Corrigir o bug "não segue o ângulo dos cliques": `posicaoQuina` agora é calculada de trás pra frente a partir do centro de rotação, de forma que o ponto pendente e o clique que fecha o segmento coincidam com o centro das bordas esquerda/direita do objeto rotacionado (linha de centro), em vez de ancorar a quina diretamente no primeiro clique (só correto pra ângulo 0) — ver design.md decisão 2.
- [x] 6.2 Trocar o modelo de pares independentes por uma corrente contínua: o clique que fecha um segmento vira o novo ponto pendente (`primeiroCliquePneus = ultimoClicado`, não `null`), então cliques consecutivos (1,2), (2,3), (3,4)... cada um fecha seu próprio segmento/objeto, até o clique direito.
- [x] 6.3 Novo método `desenhaPreObjetoPneus(Graphics2D)` (chamado junto de `desenhaPreObjetoLivre`/`GuardRails`/`Arquibancada`/`Escapada` em `paintComponent`): marca em `Color.MAGENTA` o ponto pendente da corrente, mesmo estilo (cor, raio) dos demais previews ponto a ponto.
- [x] 6.4 Reescrever `MainPanelEditorCriarPneusSequenciaTest.java` pra cobrir: primeiro clique só marca o ponto pendente; segmento em ângulo != 0 segue corretamente o ângulo dos cliques (quina não fica mais exatamente no primeiro clique); terceiro clique fecha um segundo segmento com o segundo clique (não com o primeiro); clique direito encerra sem desfazer os objetos já criados; marcador magenta aparece no ponto pendente.
- [x] 6.5 Rodar `mvn test` e confirmar que a suíte completa continua passando (807 testes).

## 7. Documentação da mudança

- [ ] 7.1 Rodar `openspec archive editor-pneus-sequencia-e-angulo-largada` (ou `/opsx:archive`) depois que os testes passarem, para mover os deltas de spec para `openspec/specs/editor-pneus-multiplos-cliques/spec.md`, `openspec/specs/objetos-cenario-circuito/spec.md` e `openspec/specs/circuito-info-editor/spec.md`.

## 8. Correção do ângulo negativo (2ª rodada de feedback, via análise de log)

- [x] 8.1 Logs temporários `[PNEUS-DEBUG]` adicionados em `MainPanelEditor` (clique recebido, ângulo calculado, ângulo lido após `setAngulo`) para diagnosticar relato do usuário de ângulo zerado numa sessão real no circuito SPA.
- [x] 8.2 Sessão de teste real do usuário no SPA + análise de `logs/flmane.log`: isolado que `GeoUtil.calculaAngulo` sempre calcula certo, mas `novoPneus.getAngulo()` volta `0.0` sempre que o valor calculado é negativo — bug no setter, não no facilitador de cliques.
- [x] 8.3 Causa raiz: `ObjetoDesenho.setAngulo` (`src/main/java/br/flmane/entidades/ObjetoDesenho.java`) fazia `Math.max(0, angulo)` (clamp) em vez de normalizar módulo 360, contradizendo o próprio javadoc da classe. Afeta os cinco tipos de `ObjetoDesenho`, não só Pneus — bug pré-existente, fora do diff original deste change.
- [x] 8.4 Corrigido para `angulo % 360`, somando 360 se negativo.
- [x] 8.5 `ObjetoDesenhoLimitesTest` (teste pré-existente): dois casos que fixavam o clamp antigo (`setAngulo(-1)` → `0`) atualizados pra esperar o módulo 360 (`-1` → `359`, `-5` → `355`).
- [x] 8.6 Novo teste de regressão `MainPanelEditorCriarPneusSequenciaTest.doisCliques_comAnguloNegativoDoAtan2_naoZeraOAngulo` cobrindo um segmento cujo `atan2` dá ângulo negativo.
- [x] 8.7 Logs `[PNEUS-DEBUG]` removidos de `MainPanelEditor` depois de confirmada a causa raiz.
- [x] 8.8 Rodar `mvn test` (810 testes) e `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`.

## 9. Causa raiz do "Objeto 70: null" + centering de largada (3ª rodada de feedback)

- [x] 9.1 Diagnóstico do "Objeto 70: null" no circuito SPA: comparado campo a campo com "Objeto 69" (vizinho na lista), confirmando que só `angulo`/`largura`/`posicaoQuina` foram persistidos — `nome`, `altura`, `nivelDesenho` ficaram no default do construtor (omitidos pelo XMLEncoder por igualarem o default).
- [x] 9.2 Corrigida a race de centralização: `centralizarPonto()` (`MainPanelEditor.java`) lia o tamanho do viewport sincronamente, antes do `setExtendedState(MAXIMIZED_BOTH)` (assíncrono) ter efeito — todo o cálculo movido pra dentro do `invokeLater` já existente.
- [x] 9.3 Usuário capturou o `NullPointerException` real ao vivo, confirmando a causa exata do "Objeto 70: null": `clickEditarObjetos` (ramo genérico de posicionamento por clique único, usado por `ObjetoConstrucao`) chamava `objetoPista.setNome(...)` (campo) depois de `formularioListaObjetos.listarObjetos()`, que dispara um instante de seleção vazia que o `ListSelectionListener` usa pra zerar esse mesmo campo — mesma race que outros ramos de criação já evitavam com uma variável local.
- [x] 9.4 Corrigido: variável local `novoObjeto` capturada antes de `listarObjetos()`, usada para `setPosicaoQuina`/`add`/`setNome`/`reprocessaEscapadaSeNecessario`.
- [x] 9.5 Novo teste de regressão `MainPanelEditorCriarObjetoConstrucaoCliqueTest` reproduzindo a pré-condição exata (objeto já selecionado na lista antes do clique de posicionamento).
- [x] 9.6 Rodar `mvn test` (811 testes) e `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`.
- [x] 9.7 Resolvido: `spa_mro.xml` não tem mais nenhum objeto sem nome (usuário corrigiu/removeu manualmente antes da resposta) — nada a corrigir no arquivo.

## 10. NullPointerException ao selecionar objeto sem nome (4ª rodada de feedback)

- [x] 10.1 Usuário capturou outro `NullPointerException` ao vivo: `"Cannot invoke \"String.split(String)\" because the return value of \"ObjetoPista.getNome()\" is null"`.
- [x] 10.2 Causa: `desenhaEtiquetaObjeto` (`MainPanelEditor.java`, chamado a cada repaint por `desenhaListaObjetos` pra todo objeto selecionado) fazia `objeto.getNome().split(" ")[1]` sem checar nulo — qualquer objeto sem nome (legado, corrompido, ou uma race futura equivalente à da seção 8/9) travava o desenho da seleção inteiro a cada frame, só de estar selecionado.
- [x] 10.3 Corrigido com um helper `numeroDaEtiqueta(String nome)`: nome nulo ou sem espaço (nome customizado sem seguir o padrão "Objeto N") retorna `"?"` em vez de lançar exceção — mesmo raciocínio de defesa em profundidade que motivou a correção anterior na origem (seção 8/9), mas aqui no consumidor, pra qualquer objeto malformado (passado, presente ou futuro) não derrubar o editor inteiro só por estar selecionado.
- [x] 10.4 Novo teste de regressão em `MainPanelEditorIndicadorSelecaoTest` (`objetoSemNome_naoTravaODesenhoDaEtiqueta`).
- [x] 10.5 Verificado `spa_mro.xml`: não há mais nenhum objeto sem nome no arquivo atual — o objeto que causou esse crash específico já não existe mais (removido/corrigido pelo usuário antes desta investigação).
- [x] 10.6 Rodar `mvn test` (812 testes) e `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`.

## 11. Duplicar Objeto (Alt+C) seleciona a cópia (5ª rodada de feedback)

- [x] 11.1 Pedido do usuário: depois de Alt+C, a cópia deveria ficar selecionada no canvas e com foco no canvas, em vez de exigir um clique extra na cópia antes de poder movê-la/editá-la com os atalhos de teclado.
- [x] 11.2 `copiarObjeto()` (`MainPanelEditor.java`) agora, depois de adicionar a cópia ao circuito: torna `objetoPista` a própria cópia (não o original), sincroniza a seleção da lista via `selecionarNasListas(...)` (fonte de verdade do contorno desenhado no canvas, sem centralizar — a cópia nasce em cima do original, já visível) e chama `srcFrame.requestFocus()` (guardado com `if (srcFrame != null)`, já que `MainPanelEditorTestDouble` em testes existentes não tem frame) pra garantir que o `KeyListener` de atalhos (ligado ao `JFrame`, ver `EditorCircuitos.ativarKeysEditor()`) continue recebendo teclas mesmo se o mouse não estiver mais sobre o canvas.
- [x] 11.3 Novo teste `MainPanelEditorAutoSaveUndoTest.copiarObjeto_altC_copiaViraObjetoAtivoESelecionadoNaLista` confirmando que a cópia (não o original) vira `objetoPista` e a seleção da lista.
- [x] 11.4 Rodar `mvn test` (813 testes) e `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar`.
