## 1. Enums

- [x] 1.1 Criar `br.f1mane.entidades.TipoObjetoConstrucao` (enum: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`)
- [x] 1.2 Criar `br.f1mane.entidades.DirecaoEmpilhamento` (enum com 8 valores: cima, baixo, esquerda, direita, cima-esquerda, cima-direita, baixo-esquerda, baixo-direita), com javadoc explicando o vetor unitário `(dx, dy)` de cada valor

## 2. ObjetoConstrucao — propriedades, forma por tipo e empilhamento genérico

- [x] 2.1 Adicionar campos `tipo` (default `QUADRADO`), `afunilamento` (int, default > 0), `quantidadeEmpilhamento` (int, default 1, mínimo 1), `direcaoEmpilhamento` (default não-nulo) e `grauEmpilhamento` (int, pixels, default > 0) a `ObjetoConstrucao`, com getters/setters (setters com validação de mínimo, mesmo padrão de `ObjetoLivre.setTipo`/`ObjetoGuardRails`)
- [x] 2.2 Extrair o desenho de cada forma (QUADRADO, REDONDO, CAMINHAO, BARCO) para um método privado `desenhaFormaUnica(Graphics2D, double zoom, int deslocamentoX, int deslocamentoY)` que despacha por `tipo` e desenha uma única instância da forma, deslocada pela posição base recebida
- [x] 2.3 Implementar a forma `QUADRADO` dentro de `desenhaFormaUnica` (mesma lógica hoje existente: dois retângulos arredondados aninhados)
- [x] 2.4 Implementar a forma `REDONDO` (elipses aninhadas, mesma composição externa/interna do `QUADRADO`)
- [x] 2.5 Implementar a forma `CAMINHAO` (duas formas lado a lado, não aninhadas, usando `corPimaria`/`corSecundaria`) — reforçado com um pivô de rotação compartilhado (centro do objeto, não de cada peça) para as duas peças girarem juntas como corpo rígido
- [x] 2.6 Implementar a forma `BARCO` (retângulo com extremidade afunilada via `GeneralPath`, comprimento da seção afunilada proporcional a `afunilamento`)
- [x] 2.7 Atualizar `desenha()` para chamar `desenhaFormaUnica` em loop `quantidadeEmpilhamento` vezes, acumulando a cada iteração um deslocamento de `grauEmpilhamento` pixels na direção de `direcaoEmpilhamento` (vetor unitário × pixels) — funciona para qualquer `tipo`, sem nenhum tipo dedicado a empilhamento
- [x] 2.8 Atualizar `obterArea()` para retornar a união das áreas de todas as repetições desenhadas (1 se `quantidadeEmpilhamento=1`), seguro antes do primeiro `desenha()`

## 3. Editor — formulário completo

- [x] 3.1 Adicionar `JComboBox<TipoObjetoConstrucao>` a `FormularioObjetos`, seguindo o padrão de `tipoObjetoLivreCombo`
- [x] 3.2 Adicionar spinner de `afunilamento`, exibido apenas quando `tipo=BARCO`
- [x] 3.3 Adicionar spinners de `quantidadeEmpilhamento`/`grauEmpilhamento` e `JComboBox<DirecaoEmpilhamento>`, exibidos sempre (para qualquer `tipo`, não condicionais)
- [x] 3.4 Atualizar `montarPainelParaTipo`, `carregarCampos` e `formularioObjetoPista` para `ObjetoConstrucao`, incluindo os novos campos (condicionais e sempre-visíveis)

## 4. Editor — menu de contexto de ajuste rápido

- [x] 4.1 Atualizar `MainPanelEditor.criaPainelAjusteRapido` para exibir os mesmos campos (tipo, afunilamento condicional a BARCO, empilhamento sempre visível) para `ObjetoConstrucao` — nota: o combo de `tipo` em si fica só no formulário completo (`FormularioObjetos`), igual ao padrão já usado para `ObjetoLivre`/`ObjetoGuardRails`, cujo tipo/orientação também não aparecem no menu rápido; o menu rápido cobre afunilamento (condicional) e empilhamento (sempre), como pede o cenário de spec

## 5. Compatibilidade e testes

- [x] 5.1 Confirmar que `ObjetoConstrucao` sem `tipo`/`afunilamento`/`quantidadeEmpilhamento`/`direcaoEmpilhamento`/`grauEmpilhamento` gravados no XML carrega com os valores padrão (`QUADRADO`, `quantidadeEmpilhamento=1`, sem alterar aparência) — `ObjetoConstrucaoTest.construtor_padraoEhQuadradoComQuantidadeEmpilhamentoUm`; como o XMLDecoder só chama setters para propriedades presentes no XML, um `ObjetoConstrucao` decodificado de um circuito antigo (sem essas propriedades) preserva os defaults do construtor, mesmo mecanismo já coberto para `ObjetoGuardRails`/`larguraLinha` em `CircuitoObjetosCenarioIntegrationTest`
- [x] 5.2 Testes unitários para `desenha()`/`obterArea()` de cada um dos 4 tipos, com `quantidadeEmpilhamento=1` e com `quantidadeEmpilhamento>1` (sem lançar exceção, área cobrindo todas as repetições) — cobrir especificamente que empilhar um tipo diferente de `QUADRADO` (ex.: `REDONDO`) funciona igual — `ObjetoConstrucaoTest` (13 testes, todos passando)
- [x] 5.3 Teste unitário: variar `grauEmpilhamento` mantendo `largura`/`altura` fixos e confirmar que o deslocamento entre repetições muda proporcionalmente ao valor em pixels configurado, não à área da forma — `ObjetoConstrucaoTest.grauEmpilhamento_deslocamentoEmPixels_naoEmFracaoDeLarguraAltura`
- [x] 5.4 Testar manualmente no editor de circuitos: criar um `ObjetoConstrucao`, alternar entre os 4 tipos, ajustar largura/altura/afunilamento/quantidadeEmpilhamento/direcaoEmpilhamento/grauEmpilhamento pelo formulário e pelo menu de contexto, e confirmar que o desenho reflete cada mudança (inclusive empilhar tipos diferentes de QUADRADO) — sem display interativo neste ambiente para testar o editor Swing ao vivo; verificado via renderização direta (`ObjetoConstrucao.desenha()`) para uma amostra visual dos 4 tipos e de empilhamento com QUADRADO (torre) e REDONDO (cascata diagonal de círculos), confirmando visualmente cada forma e que o empilhamento funciona igual para um tipo diferente de QUADRADO
