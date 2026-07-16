## 1. Modelos de árvore lateral

- [x] 1.1 Adicionar constantes de tamanho fixo (`LARGURA_TRONCO`, `ALTURA_TRONCO`, `RAIO_COPA` ou equivalentes, derivadas de `PASSO_PADRAO_LOCAL`) em `ObjetoLivre`, sem variação por instância
- [x] 1.2 Implementar `formaArvoreLateral(cx, cyBase, variante)` retornando as formas de tronco (retângulo/trapézio) e copa (triangular, arredondada, pinheiro em camadas, lobulada — 4 variantes fixas), inspiradas no estilo de `kenney_foliage-pack/Preview.png` sem carregar nenhum arquivo do pacote
- [x] 1.3 Remover `formaTopoArvore`, `VARIANTES_TOPO_ARVORE`, `AMPLIACAO_MIN/MAX_TOPO_ARVORE` e `VARIACAO_TAMANHO_VEGETACAO_DENSA` (mortos após a troca de silhueta)

## 2. Cores, fundo transparente e dispersão

- [x] 2.1 Em `desenha()`, tornar o fill de fundo com `corPimaria` condicional a `tipo != VEGETACAO_DENSA`
- [x] 2.2 Em `desenhaPadraoVegetacao` (ramo `densa`), desenhar o tronco com `corPimaria` e a copa com `corSecundaria` para cada árvore aceita, sorteando só posição e `variante` (sem `fatorTamanho`/ampliação)
- [x] 2.3 Atualizar `sobrepoeTouceiraAceita` (ou equivalente) para usar `RAIO_COPA` fixo em vez do raio por touceira armazenado
- [x] 2.4 Estender `cabeInteiraNaSilhueta` (ou o ponto de chamada) para testar a `Area` combinada de tronco+copa, descartando a árvore inteira se qualquer parte ficar fora da silhueta

## 3. Modo de preview "marca única centralizada"

- [x] 3.1 Adicionar `Global.padraoObjetoLivreCompleto` (boolean, default `true`, mesmo padrão de `Global.DEBUG`) em `br/nnpe/Global.java`
- [x] 3.2 Em cada método de padrão (`desenhaPadraoEmGrade`, `desenhaPadraoVegetacao`, `desenhaBrita`, `desenhaPadraoListrado`, `desenhaPadraoXadrez`), checar a flag: se `false`, desenhar só uma instância centralizada em `formaLocal.getBounds()` em vez do laço completo
- [x] 3.3 Para `VEGETACAO_DENSA` no modo centralizado, aplicar a mesma verificação de contenção (2.4) — não desenhar se a árvore central não couber inteira

## 4. Checkbox "Padrão" no editor

- [x] 4.1 Adicionar chave de idioma `padraoCompletoCheck` em `mensagens_pt.properties`/`mensagens_en.properties`/`mensagens_es.properties`/`mensagens_it.properties`
- [x] 4.2 Criar `linha3Painel` em `MainPanelEditor.gerarLayout()` com um `JCheckBox` ligado a `Global.padraoObjetoLivreCompleto` (inicializado com o valor atual, listener grava a flag e chama `repaint()`, sem ler/gravar `circuito`)
- [x] 4.3 Ajustar o `JPanel topo` de `GridLayout(3, 1)` para `GridLayout(4, 1)`, adicionando `linha3Painel` após `linha2Painel`

## 5. Testes

- [x] 5.1 Atualizar `ObjetoLivreTipoPadraoTest` (as demais suítes de `ObjetoLivre*` não referenciavam `VEGETACAO_DENSA` e não precisaram de mudança) onde afirmava a silhueta de topo de árvore ou o fill de fundo sólido de `VEGETACAO_DENSA`, cobrindo: tronco em `corPimaria`, copa em `corSecundaria`, ausência de fundo, tamanho uniforme entre árvores, e descarte de árvore cortada pela borda. Também atualizado `MainPanelEditorTopoNavegacaoTest` (não previsto originalmente): o painel `topo` passou de 3 para 4 linhas
- [x] 5.2 Adicionado teste cobrindo `Global.padraoObjetoLivreCompleto = false` desenhando só uma marca centralizada (BRITA e `VEGETACAO_DENSA`), e o caso de descarte quando a marca central não cabe inteira
- [x] 5.3 Rodado `mvn -Ph2 test` completo — build verde, nenhum teste fora do escopo regrediu

## 6. Validação visual

- [x] 6.1 Sem display gráfico neste ambiente para abrir o Swing interativamente: validado via harness Java ad-hoc (mesmo método usado na mudança anterior de vegetação densa) renderizando um `ObjetoLivre` `VEGETACAO_DENSA` sobre um fundo quadriculado — confirma tronco (marrom) e copa (verde) em modelos variados, fundo transparente entre as árvores (quadriculado visível), e nenhuma árvore cortada na borda
- [x] 6.2 Mesmo harness com `Global.padraoObjetoLivreCompleto = false`: confirma a redução para uma única árvore centralizada, sem nenhuma alteração em arquivo de circuito (a flag é só em memória)
