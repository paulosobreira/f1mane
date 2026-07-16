Proposta retroativa — implementação já concluída e validada antes deste documento. Tarefas registradas como executadas.

## 1. Silhuetas de topo de árvore

- [x] 1.1 Adicionar `VARIANTES_TOPO_ARVORE` e o método `formaTopoArvore(cx, cy, raio, variante, anguloBase)` em `ObjetoLivre`, gerando um polígono em estrela (raio externo/interno alternado) com 4 combinações de pontas/entalhe (estrela pontiaguda, copa arredondada, espinhos finos, copa lobulada)
- [x] 1.2 Em `desenhaPadraoVegetacao`, para `VEGETACAO_DENSA`, sortear a variante e o ângulo base pelo mesmo `Random` da posição/tamanho e preencher (`g2d.fill`) a silhueta em vez de desenhar a cruz de 3 traços anterior
- [x] 1.3 Remover o `case VEGETACAO_DENSA` (agora morto) de `desenhaPrimitivaPadrao`, mantendo `VEGETACAO_SIMPLES` e `AGUA` inalterados

## 2. Ampliação de tamanho

- [x] 2.1 Adicionar `AMPLIACAO_MIN_TOPO_ARVORE`/`AMPLIACAO_MAX_TOPO_ARVORE` e aplicar, só para `VEGETACAO_DENSA`, um fator aleatório sorteado nessa faixa sobre o raio já calculado por `fatorTamanho`
- [x] 2.2 Calibrar a faixa por iteração visual com o usuário: 3x–5x inicialmente, ajustada para 2x–3x no resultado final

## 3. Densidade da grade

- [x] 3.1 Recalcular `FATOR_PASSO_VEGETACAO_DENSA` para triplicar a densidade original (`1.6 / sqrt(3)`)
- [x] 3.2 Reduzir a densidade à metade a pedido do usuário (`* sqrt(2)`), chegando a 1.5x a densidade original
- [x] 3.3 Reverter uma segunda redução à metade (que teria chegado a 0.75x) de volta para 1.5x, mantida como valor final

## 4. Validação

- [x] 4.1 Rodar a suíte `ObjetoLivreTipoPadraoTest`, `ObjetoLivreTest`, `ObjetoLivreCircuitoIntegrationTest`, `ObjetoLivrePadraoRotacaoTest`, `ObjetoDesenhoLimitesTest` (40/40 passando, sem alteração de nenhum teste)
- [x] 4.2 Renderizar amostras via harness Java ad-hoc para conferência visual em cada etapa (silhuetas, densidade triplicada, ampliação, densidade final)
- [x] 4.3 Confirmar que `VEGETACAO_SIMPLES` permanece com o traço diagonal original, sem regressão

## 5. Documentação

- [x] 5.1 Criar esta proposta OpenSpec (retroativa) documentando a mudança já aplicada
- [x] 5.2 Sincronizar o delta spec de `objeto-livre-vetorial` e arquivar a mudança
