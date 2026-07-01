## 1. Renomear ObjetoGuadRails

- [x] 1.1 Buscar `ObjetoGuadRails` em todo o repositório (`src/main/java`, `src/test`, `src/main/resources`, incluindo XMLs de circuito) para confirmar que não há referências além da própria classe
- [x] 1.2 Renomear o arquivo/classe `ObjetoGuadRails` para `ObjetoGuardRails` e atualizar todas as referências no código
- [x] 1.3 Compilar (`mvn test-compile`) para garantir que nenhuma referência ao nome antigo restou

## 2. Defaults seguros e inicialização defensiva nos quatro tipos de objeto

- [x] 2.1 Adicionar construtor sem argumentos em `ObjetoArquibancada` definindo `largura`, `altura`, `corPimaria`, `corSecundaria` e `transparencia` com valores visíveis por padrão
- [x] 2.2 Repetir para `ObjetoConstrucao`
- [x] 2.3 Repetir para `ObjetoGuardRails` (defaults compatíveis com sua geometria de linha fina/alta)
- [x] 2.4 Repetir para `ObjetoPneus`, usando defaults pequenos para `largura`/`altura` (contagem de grade), evitando desenhar uma quantidade excessiva de pneus por padrão
- [x] 2.5 Inicializar os campos `externo`/`interno` (shapes `RoundRectangle2D`) desses quatro tipos no ponto de declaração, para que `obterArea()` nunca retorne NPE se chamado antes do primeiro `desenha()`
- [x] 2.6 Escrever teste unitário cobrindo: instanciar cada um dos quatro tipos, chamar `obterArea()` antes de `desenha()` (sem exceção), e chamar `desenha()` num `Graphics2D` de um `BufferedImage` de teste (sem exceção)

## 3. Registro extensível de tipos de objeto no editor

- [x] 3.1 Criar enum `TipoObjetoPista` em `br.f1mane.editor` com uma entrada por tipo criável (rótulo de exibição + fábrica `Supplier<ObjetoPista>`), incluindo os tipos já existentes (`ObjetoTransparencia`, `ObjetoEscapada`) e os quatro novos (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`)
- [x] 3.2 Atualizar `FormularioObjetos` para popular `tipoComboBox` a partir de `TipoObjetoPista.values()`, removendo as constantes `OBJETO_TRANSPARENCIA`/`OBJETO_ESCAPADA` hardcoded
- [x] 3.3 Atualizar o listener do botão "Criar Objeto" em `MainPanelEditor` para instanciar o objeto via `TipoObjetoPista` selecionado, removendo o `if/else` por string
- [x] 3.4 Confirmar que o fluxo de posicionamento por clique (`clickEditarObjetos`, ramo `posicionaObjetoPista`) e a listagem em `FormularioListaObjetos` continuam funcionando sem alteração para os quatro novos tipos (eles já são genéricos sobre `ObjetoPista`)

## 4. Edição de cor e tamanho em `FormularioObjetos`

- [x] 4.1 Adicionar `MouseListener` em `labelCor1`/`labelCor2` abrindo `JColorChooser` (mesmo padrão de `EditorCoresCarros`), chamando `setCor(...)` já existente
- [x] 4.2 Descomentar/corrigir a gravação de `corPimaria`/`corSecundaria` no objeto em `formularioObjetoPista()`, lendo o background dos labels de cor
- [x] 4.3 Adicionar spinner de `altura` ao painel do formulário (hoje inexistente) e efetivamente exibir o spinner de `largura` (hoje criado mas nunca adicionado ao painel)
- [x] 4.4 Ler os valores de `largura`/`altura` de volta em `formularioObjetoPista()`, gravando no objeto em edição
- [ ] 4.5 Testar manualmente no editor: criar um objeto de cada um dos quatro tipos, alterar cor e tamanho pelo formulário, confirmar atualização visual imediata (`repaint()`)

## 5. Geração em memória da imagem de fundo do circuito

- [x] 5.1 Extrair `desenhaTintaPistaEZebra`, `desenhaPista`, `desenhaPistaBox` e o loop de `circuito.getObjetos()` (hoje `desenhaObjetosBaixo`/`desenhaObjetosCima`) de `MainPanelEditor` para uma classe compartilhada `DesenhoProceduralCircuito` (ex.: em `br.f1mane.entidades`), recebendo `Circuito` e `Graphics2D`/`zoom`; atualizar `MainPanelEditor` para chamar essa classe em vez da lógica local
- [x] 5.2 No loop de objetos de `DesenhoProceduralCircuito`, excluir explicitamente `ObjetoEscapada` e `ObjetoTransparencia` do desenho (esses dois continuam com tratamento especial próprio em `PainelCircuito`, fora desta classe)
- [x] 5.3 Adicionar em `DesenhoProceduralCircuito` um método que recebe um `Circuito` e retorna um `BufferedImage` pronto (dimensões calculadas a partir dos limites dos nós da pista, em escala real), desenhando pista + zebra + box + objetos (já filtrados)
- [x] 5.4 Adicionar `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` (`boolean`, default `false`)
- [x] 5.5 Adicionar `CarregadorRecursos.carregaBackGroundJogo(...)` (novo método, não altera `carregaBackGround` original) que, quando a flag estiver ativa, gera a imagem via `DesenhoProceduralCircuito` em vez de `ImageIO.read(circuitos/<jpg>)`; `PainelCircuito.carregaBackGround()` passa a chamar esse novo método. `carregaBackGround` original fica intacto e continua sendo usado pelo editor (`MainPanelEditor`) para carregar a imagem real de referência, sem ser afetado pela flag
- [x] 5.6 Atualizar `LetsRace./circuitoJpg/{nmCircuito}` para, quando a flag estiver ativa, gerar a imagem via `DesenhoProceduralCircuito` e serializá-la como jpg (`ImageIO.write`) na resposta, em vez de ler `circuitos/<nmCircuito>` do disco
- [x] 5.7 Confirmar que os fallbacks de rede (`ControleJogoLocal.carregaBackGround`, `JogoCliente.carregaBackGround`) não precisam de alteração, já que consomem o resultado (estático ou gerado) através do endpoint REST acima
- [x] 5.8 Escrever teste unitário para `DesenhoProceduralCircuito`: gerar a imagem de um circuito de teste com um objeto de cada um dos quatro tipos-alvo mais um `ObjetoEscapada` e um `ObjetoTransparencia`, e verificar que a imagem resultante tem dimensão maior que zero e que a geração não lança exceção

## 6. Validação de ponta a ponta

- [ ] 6.1 No editor, criar um circuito de teste com um exemplar de cada um dos quatro tipos, salvar (F8) em `src/main/resources/circuitos` — **pendente: requer sessão interativa no editor Swing, não disponível neste ambiente de execução do agente**
- [x] 6.2 Confirmar round-trip via `XMLEncoder`/`XMLDecoder` (posição, tamanho, cor) — coberto automaticamente por `CircuitoObjetosCenarioIntegrationTest.objetoDeCenario_sobrevivePersistenciaReflexiva`, usando um circuito real (`albert_park_mro.xml`) em vez de passar pela UI do editor
- [x] 6.3 Com `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA = false` (default), confirmar que o comportamento é idêntico ao anterior a esta mudança (imagem de fundo lida do jpg em disco) — coberto por `CircuitoObjetosCenarioIntegrationTest.flagDesativada_carregaBackGroundJogo_leJpgEstatico`
- [x] 6.4 Com a flag ativada, confirmar que a geração em memória funciona sobre um circuito real com um objeto de cenário — coberto por `CircuitoObjetosCenarioIntegrationTest.flagAtivada_carregaBackGroundJogo_geraImagemEmMemoria`. A confirmação visual (rodar uma corrida de verdade e observar a tela) continua **pendente: requer sessão gráfica interativa**
- [ ] 6.5 Com a flag ativada, chamar `/circuitoJpg/{nmCircuito}` do servidor rodando de verdade e confirmar a imagem retornada — **pendente: requer subir o servidor Tomcat embutido e fazer uma chamada HTTP real**
- [x] 6.6 Rodar `mvn test` e confirmar que a suíte existente continua passando — 131/131 testes passando (126 pré-existentes + 5 novos)

## 7. Lista separada de objetos de cenário e interações avançadas no editor

- [x] 7.1 Adicionar `objetosCenario` (`List<ObjetoPista>`) em `Circuito`, com getter/setter bean padrão, persistido automaticamente via `XMLEncoder`/`XMLDecoder` como `objetos` já é
- [x] 7.2 Adicionar flag `cenario` em `TipoObjetoPista` (true para Arquibancada/Construcao/GuardRails/Pneus, false para Transparencia/Escapada) e um método `isCenario()`
- [x] 7.3 Em `DesenhoProceduralCircuito.desenhaObjetos()`, trocar o loop filtrado por `instanceof` para iterar diretamente `circuito.getObjetosCenario()` (fica automaticamente restrito aos quatro tipos, sem exclusão manual)
- [x] 7.4 Parametrizar `FormularioListaObjetos` para operar sobre qualquer uma das duas listas do circuito (em vez de sempre `circuito.getObjetos()`), preservando o comportamento atual para quem continuar usando a lista `objetos`
- [x] 7.5 Em `MainPanelEditor`, instanciar um segundo `FormularioListaObjetos` para `objetosCenario` e exibi-lo junto do existente (empilhados na borda leste da janela via `GridLayout(2,1)`)
- [x] 7.6 Atualizar o fluxo de criação ("Criar Objeto") para adicionar o objeto recém-criado a `objetosCenario` ou `objetos`, conforme `TipoObjetoPista.isCenario()`. Corrigido também `copiarObjeto()` (Alt+C), que sempre adicionava a `objetos`, para copiar para a mesma lista do objeto original
- [x] 7.7 Atualizar os loops de desenho do editor (`desenhaObjetosBaixo`/`desenhaObjetosCima`) e o hit-test de duplo-clique (`editaObjetoPista`) para considerar as duas listas (`objetos` + `objetosCenario`), extraindo `encontraObjetoPista`/`todosObjetos` como helpers comuns
- [x] 7.8 Adicionar menu de contexto (clique direito, `JPopupMenu`) sobre um objeto encontrado pelo helper de busca, com spinners de largura/altura/ângulo que escrevem direto no objeto e chamam `repaint()`; ao abrir o menu, atualizar o campo de seleção (`objetoPista`) usado pelos atalhos de teclado existentes
- [x] 7.9 Adicionar arrastar-e-soltar: no `mousePressed` sobre a área de um objeto (modo "sem seleção"/edição, botão esquerdo), guardar o objeto e o deslocamento relativo ao ponto clicado; no `mouseDragged`, atualizar `posicaoQuina` e repintar; usar uma flag (`arrastouObjeto`) para o `mouseClicked` subsequente não disparar criação de objeto nem inserção de nó
- [x] 7.10 Compilar e rodar `mvn test`, confirmando que a suíte (incluindo os testes de `DesenhoProceduralCircuito` e o round-trip) continua passando — 131/131 passando
- [ ] 7.11 Teste manual no editor: criar um objeto de cenário e confirmar que aparece na lista nova (não na antiga); clicar com o botão direito e ajustar largura/altura/ângulo pelo menu; arrastar o objeto com o mouse; confirmar que Alt+setas ainda funciona — **requer sessão interativa, não disponível neste ambiente de execução do agente**

## 8. Cor de fundo e cor do asfalto configuráveis no editor

- [x] 8.1 Adicionar `corAsfalto` (`Color`) em `Circuito`, com getter/setter bean padrão (mesmo tratamento de `corFundo`, já existente)
- [x] 8.2 Em `DesenhoProceduralCircuito.desenhaPista`, usar `circuito.getCorAsfalto()` quando definida, com fallback para a constante `COR_PISTA` (agora pública) quando `null`
- [x] 8.3 No painel de controles do editor (`MainPanelEditor`), ao lado do checkbox "Noite", adicionar duas caixas de cor clicáveis (`JLabel` + `JColorChooser`, mesmo padrão de `FormularioObjetos`): uma para `circuito.setCorFundo(...)`, outra para `circuito.setCorAsfalto(...)`, cada uma chamando `repaint()` ao mudar
- [x] 8.4 Sincronizar as duas caixas de cor com os valores do circuito ao abrir um circuito existente (mesmo ponto onde `noite.setSelected(circuito.isNoite())` já acontece)
- [x] 8.5 Compilar e rodar `mvn test`, confirmando que a suíte continua passando
- [x] 8.6 Correção: o rótulo da caixa de cor de fundo usava a chave `Lang.msg("corFundoPista")`, sem tradução cadastrada em nenhum bundle (mesmo padrão de outras chaves não traduzidas nesse formulário, ex. `angulorRotacao`), então exibia o texto cru da chave — mencionando "Pista" em vez de "Cenário". Trocado por texto literal fixo ("Cor de Fundo do Cenário"/"Cor do Asfalto"), sem depender de `Lang.msg`, eliminando a dependência de uma tradução que nunca existiu
- [x] 8.7 Correção: `paintComponent` (modo `!mostraBG`) nunca preenchia o fundo com `circuito.getCorFundo()` antes de desenhar pista/zebra/box — só `DesenhoProceduralCircuito.geraImagem()` (usado na geração em memória para corrida) fazia esse preenchimento. Alterar `corFundo` não tinha efeito visual no editor. Corrigido adicionando `g2d.fillRect(...)` com `corFundo` (fallback para a cor padrão do painel) logo antes da chamada a `desenhaPistaZebraEBox`
- [x] 8.8 Ajuste: as caixas de cor mostravam o texto "Clique" antes de qualquer cor ser escolhida. Trocado para um indicador de cor puro (sem texto, tamanho fixo 30x20, borda preta, cursor de mão), preenchido diretamente com a cor atual — o próprio retângulo colorido é o elemento clicável
- [ ] 8.9 Teste manual no editor: abrir um circuito, clicar nas duas caixas de cor, escolher cores, confirmar atualização visual imediata na pré-visualização (fundo inteiro para "Cor de Fundo do Cenário", só a pista para "Cor do Asfalto") — **requer sessão interativa, não disponível neste ambiente de execução do agente**
