## 1. ObjetoGuardRails: propriedades e manipulação de pontos

- [x] 1.1 Transformar `LARGURA_LINHA`/`VAO_ENTRE_LINHAS` em campos de instância `larguraLinha`/`vaoEntreLinhas` (bean property, default 1), atualizando `construirLinhas()` para usá-los em vez das constantes
- [x] 1.2 Adicionar métodos em `ObjetoGuardRails` para mover um ponto por índice, inserir um ponto em uma posição de índice específica, e remover um ponto por índice (bloqueando remoção quando `pontos.size() <= 2`), todos chamando `gerar()` ao final
- [x] 1.3 Adicionar teste unitário cobrindo: default de `larguraLinha`/`vaoEntreLinhas` = 1, mover/inserir/remover ponto, e bloqueio de remoção com apenas 2 pontos

## 2. FormularioObjetos: campos de espessura e vão

- [x] 2.1 Adicionar campos numéricos "Espessura da linha" e "Vão entre linhas" no painel específico de `ObjetoGuardRails` em `FormularioObjetos`, ao lado do combo de orientação existente
- [x] 2.2 Ler `larguraLinha`/`vaoEntreLinhas` do objeto ao abrir o formulário, e gravar os valores editados ao salvar
- [x] 2.3 Adicionar/ajustar teste de `FormularioObjetos` cobrindo leitura e gravação desses dois campos

## 3. MainPanelEditor: modo de edição de pontos de guard rails

- [x] 3.1 Adicionar campo `editandoPontosGuardRailsDe` (tipo `ObjetoGuardRails`) e a ação (menu de contexto/edição) que entra e sai desse modo, análoga à existente para `ObjetoLivre`
- [x] 3.2 Implementar arraste de um ponto existente: iniciar arraste ao clicar próximo de um ponto, atualizar a posição durante o movimento, e gravar/`gerar()` ao soltar o botão
- [x] 3.3 Implementar inserção de ponto: clique esquerdo suficientemente próximo de um segmento (mas não de um ponto existente) insere um novo ponto naquela posição do encadeamento
- [x] 3.4 Implementar remoção de ponto: clique direito sobre um ponto existente remove esse ponto (respeitando o bloqueio de mínimo de 2 pontos)
- [x] 3.5 Garantir que o modo de edição de pontos de guard rails não interfere com o fluxo de criação ponto a ponto nem com o arraste de objeto inteiro já existente

## 4. Snap a nós de pista e a pontos de outros objetos

- [x] 4.1 Implementar função utilitária de snap que, dado um ponto candidato e uma tolerância em pixels de tela, retorna o candidato mais próximo entre `circuito.getPista()`, `circuito.getBox()` e os pontos/vértices dos objetos em `circuito.getObjetosCenario()`, ou o próprio ponto se nada estiver dentro da tolerância
- [x] 4.2 Aplicar essa função ao clique que adiciona um novo ponto durante a criação de um `ObjetoGuardRails`
- [x] 4.3 Aplicar essa função ao soltar o botão do mouse ao arrastar um ponto existente em modo de edição de pontos (tarefa 3.2)
- [x] 4.4 Adicionar teste cobrindo: snap a nó dentro da tolerância, ausência de snap fora da tolerância, e escolha do candidato mais próximo quando nó e ponto de outro objeto competem

## 5. Compatibilidade e verificação manual

- [x] 5.1 Confirmar que um circuito XML existente com `ObjetoGuardRails` (sem `larguraLinha`/`vaoEntreLinhas` gravados) carrega e desenha com a mesma aparência de antes desta mudança
- [x] 5.2 Rodar `mvn test` e garantir que os testes existentes de `ObjetoGuardRails`/editor continuam passando
- [ ] 5.3 Testar manualmente no editor (`EditorCircuitos`): criar um guard rails com snap ativo, editar seus pontos (mover/inserir/remover), e ajustar espessura/vão pelo formulário
