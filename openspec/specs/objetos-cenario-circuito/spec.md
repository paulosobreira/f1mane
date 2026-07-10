# objetos-cenario-circuito

## Purpose

Permitir que o editor de circuitos crie, edite e gerencie objetos de cenário de pista (arquibancada, construção, guard rails, pneus) através de um registro extensível de tipos, com edição de cor/tamanho, valores padrão seguros, manipulação direta no editor (menu de contexto e arraste) e reaproveitamento do desenho procedural para gerar a imagem de fundo do circuito em memória.

## Requirements

### Requirement: Registro extensível de tipos de objeto de pista no editor
O editor de circuitos SHALL obter a lista de tipos de `ObjetoPista` criáveis (para o combo box de criação e para a instanciação do objeto) a partir de um único registro de tipos, e não de comparações de string em cadeia (`if/else`) espalhadas pelo fluxo de criação. Adicionar um novo tipo de `ObjetoPista` criável SHALL exigir apenas uma nova entrada nesse registro, sem alterar a lógica do botão "Criar Objeto".

#### Scenario: Combo de tipos reflete o registro
- **WHEN** o editor de circuitos abre o formulário de criação de objeto ("Criar Objeto")
- **THEN** o combo box de tipos lista exatamente os tipos presentes no registro de tipos, sem itens hardcoded fora dele

#### Scenario: Instanciar objeto a partir do tipo selecionado
- **WHEN** o usuário seleciona um tipo no combo e confirma a criação
- **THEN** o editor instancia o `ObjetoPista` correspondente consultando o registro pelo tipo selecionado, sem um bloco `if/else` dedicado a esse tipo

### Requirement: Tipos de cenário de pista disponíveis no editor
O registro de tipos de `ObjetoPista` SHALL incluir `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` e `ObjetoPneus`, tornando-os criáveis e posicionáveis no editor de circuitos pelo mesmo fluxo de clique-para-posicionar já usado por `ObjetoEscapada`.

#### Scenario: Criar uma arquibancada
- **WHEN** o usuário seleciona o tipo "Arquibancada" e clica em um ponto do circuito no editor
- **THEN** um `ObjetoArquibancada` é adicionado a `circuito.getObjetos()` na posição clicada e aparece na lista de objetos do editor

#### Scenario: Criar construção, guard rails e pneus
- **WHEN** o usuário repete o fluxo de criação para os tipos "Construção", "Guard Rails" e "Pneus"
- **THEN** os respectivos `ObjetoConstrucao`, `ObjetoGuardRails` e `ObjetoPneus` são adicionados a `circuito.getObjetos()` e aparecem na lista de objetos do editor

### Requirement: Cor e tamanho de um objeto de pista são editáveis no editor
O formulário de edição de `ObjetoPista` (`FormularioObjetos`) SHALL permitir escolher a cor primária e a cor secundária via seletor de cores, e definir largura e altura via campos numéricos, gravando esses valores no objeto selecionado. Para `ObjetoGuardRails` especificamente, o formulário SHALL também permitir editar, via campos numéricos, a espessura da linha fina do padrão (`larguraLinha`) e o vão entre linhas consecutivas (`vaoEntreLinhas`), que passam a ser propriedades do objeto em vez de constantes fixas de classe. Para `ObjetoConstrucao` especificamente, o formulário SHALL também permitir escolher o `tipo` (`QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`) via combo box, editar condicionalmente o afunilamento (só quando `tipo=BARCO`), e editar sempre (independente do `tipo`) a quantidade, a direção e o grau de empilhamento.

#### Scenario: Alterar a cor primária
- **WHEN** o usuário clica no indicador de cor primária no formulário e escolhe uma cor no seletor
- **THEN** `corPimaria` do objeto em edição é atualizada para a cor escolhida e o objeto é redesenhado com essa cor

#### Scenario: Alterar largura e altura
- **WHEN** o usuário altera os campos de largura e altura no formulário para um objeto em edição
- **THEN** `largura` e `altura` do objeto são atualizadas com os valores informados e o objeto é redesenhado com o novo tamanho

#### Scenario: Alterar espessura da linha fina de um guard rails
- **WHEN** o usuário altera o campo de espessura da linha fina no formulário para um `ObjetoGuardRails` em edição
- **THEN** `larguraLinha` do objeto é atualizada com o valor informado, e o padrão de linhas finas é redesenhado usando essa espessura

#### Scenario: Alterar vão entre linhas de um guard rails
- **WHEN** o usuário altera o campo de vão entre linhas no formulário para um `ObjetoGuardRails` em edição
- **THEN** `vaoEntreLinhas` do objeto é atualizado com o valor informado, e a quantidade/distribuição das linhas finas ao longo do encadeamento é recalculada usando esse vão como período-alvo

#### Scenario: Guard rails de circuito antigo assume valores padrão
- **WHEN** um circuito XML criado antes desta mudança, contendo um `ObjetoGuardRails` sem `larguraLinha`/`vaoEntreLinhas` gravados, é carregado
- **THEN** o objeto assume `larguraLinha=1` e `vaoEntreLinhas=1` (os mesmos valores antes hardcoded), preservando a aparência original

#### Scenario: Alterar o tipo de um objeto construção
- **WHEN** o usuário seleciona um tipo diferente no combo de tipo do formulário para um `ObjetoConstrucao` em edição
- **THEN** `tipo` do objeto é atualizado e o objeto é redesenhado com a forma correspondente ao novo tipo, mantendo posição, ângulo, cores, tamanho e configuração de empilhamento já definidos

#### Scenario: Campo de afunilamento só aparece para o tipo BARCO
- **WHEN** o usuário abre o formulário para um `ObjetoConstrucao` cujo tipo selecionado é `BARCO`
- **THEN** o formulário exibe o campo de afunilamento, e esse campo não aparece quando o tipo selecionado é `QUADRADO`, `REDONDO` ou `CAMINHAO`

#### Scenario: Campos de empilhamento aparecem para qualquer tipo
- **WHEN** o usuário abre o formulário para um `ObjetoConstrucao`, qualquer que seja o tipo selecionado (`QUADRADO`, `REDONDO`, `CAMINHAO` ou `BARCO`)
- **THEN** o formulário exibe os campos de quantidade, direção e grau de empilhamento, independente do tipo selecionado

#### Scenario: Menu de contexto de ajuste rápido reflete os mesmos campos
- **WHEN** o usuário abre o menu de contexto de ajuste rápido (clique direito) sobre um `ObjetoConstrucao`
- **THEN** o menu exibe os mesmos campos de empilhamento (sempre) e de afunilamento (só quando `tipo=BARCO`) que o formulário completo exibiria

### Requirement: Vão entre os módulos do CAMINHAO é menor que a margem de borda
`ObjetoConstrucao` do tipo `CAMINHAO` SHALL desenhar o vão entre os dois módulos (cabine e carroceria) usando uma constante própria (`VAO_MODULOS_CAMINHAO`) igual à metade da margem usada entre a forma externa e interna de cada módulo (`MARGEM_INTERNA`), sem alterar `MARGEM_INTERNA` em si, que continua regendo a borda aninhada de todos os tipos, incluindo os dois módulos do próprio CAMINHAO.

#### Scenario: Vão entre cabine e carroceria é a metade da margem de borda
- **WHEN** um `ObjetoConstrucao` do tipo `CAMINHAO` é desenhado
- **THEN** a distância entre a borda direita do módulo da cabine e a borda esquerda do módulo da carroceria é igual à metade do valor de `MARGEM_INTERNA`

#### Scenario: Margem de borda dos módulos permanece inalterada
- **WHEN** um `ObjetoConstrucao` do tipo `CAMINHAO` é desenhado
- **THEN** a margem entre a forma externa e a forma interna de cada módulo (cabine e carroceria) continua usando o valor de `MARGEM_INTERNA` sem redução, e o comportamento de `QUADRADO`/`REDONDO`/`BARCO` (que não têm dois módulos) não é afetado

### Requirement: Objetos de cenário têm valores padrão seguros ao serem criados
`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` e `ObjetoPneus` SHALL, ao serem instanciados sem configuração adicional, ter cor primária, cor secundária, largura e altura com valores padrão não nulos e maiores que zero, de forma que o objeto seja desenhado sem lançar exceção e seja visível antes de qualquer edição pelo usuário.

#### Scenario: Objeto recém-criado é desenhado sem erro
- **WHEN** um dos quatro tipos de objeto de cenário é instanciado e imediatamente desenhado (sem que o usuário tenha editado cor ou tamanho)
- **THEN** o método `desenha()` completa sem lançar `NullPointerException` e produz uma forma visível com dimensão maior que zero

#### Scenario: Área do objeto é consultável antes do primeiro desenho
- **WHEN** `obterArea()` é chamado em um dos quatro tipos de objeto de cenário antes de `desenha()` ter sido chamado alguma vez
- **THEN** `obterArea()` retorna um `Rectangle` válido (não lança `NullPointerException`)

### Requirement: Imagem de fundo do circuito pode ser gerada em memória em vez de lida de arquivo
O sistema SHALL oferecer uma flag booleana em `Global` (default `false`) que, quando ativa, faz todo carregamento da imagem de fundo do circuito (`circuitos/*_mro.jpg`) ser substituído pela geração em memória dessa imagem, reproduzindo o mesmo desenho procedural de pista, zebra, box e objetos de cenário já usado pelo editor de circuitos quando este está em modo sem imagem de fundo. Com a flag desativada, o comportamento SHALL permanecer idêntico ao anterior a esta mudança (leitura do arquivo em disco).

#### Scenario: Flag desativada preserva o comportamento atual
- **WHEN** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` é `false` (valor padrão) e uma corrida solo carrega um circuito
- **THEN** a imagem de fundo é lida do arquivo `circuitos/<nome>_mro.jpg`, exatamente como antes desta mudança

#### Scenario: Flag ativada gera a imagem em memória no modo solo
- **WHEN** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` é `true` e uma corrida solo carrega um circuito
- **THEN** `PainelCircuito` usa como imagem de fundo uma imagem gerada em memória a partir da geometria da pista e dos objetos do circuito, sem ler `circuitos/<nome>_mro.jpg` do disco

#### Scenario: Flag ativada gera a imagem em memória para o endpoint do multiplayer
- **WHEN** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` é `true` e o endpoint REST `/circuitoJpg/{nmCircuito}` é chamado pelo cliente Java do multiplayer
- **THEN** o servidor responde com uma imagem gerada em memória (codificada como jpg), em vez do arquivo estático em disco

### Requirement: Objetos de cenário aparecem na imagem gerada em memória; tipos com significado especial ficam de fora
Quando a imagem de fundo é gerada em memória, o desenho SHALL incluir todo `ObjetoPista` presente em `circuito.getObjetos()`, na posição, ângulo, cor e tamanho definidos no editor — incluindo `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus` e qualquer tipo futuro — com exceção de `ObjetoEscapada` e `ObjetoTransparencia`, que SHALL ser excluídos dessa geração.

#### Scenario: Cenário aparece na imagem gerada
- **WHEN** a imagem de fundo é gerada em memória para um circuito cujo `circuito.getObjetos()` contém um `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` ou `ObjetoPneus`
- **THEN** o objeto aparece desenhado na imagem gerada, na posição, ângulo e cores definidos no editor

#### Scenario: Um tipo futuro de objeto de cenário aparece sem alterar o gerador
- **WHEN** um novo subtipo de `ObjetoPista` é adicionado ao registro de tipos do editor (ver requisito de registro extensível) e um exemplar dele é colocado em um circuito
- **THEN** esse objeto aparece na imagem gerada em memória sem que a classe responsável por gerar essa imagem precise ser alterada

#### Scenario: Escapada e Transparência não ficam gravados na imagem gerada
- **WHEN** a imagem de fundo é gerada em memória para um circuito cujo `circuito.getObjetos()` contém um `ObjetoEscapada` ou um `ObjetoTransparencia`
- **THEN** nenhum dos dois aparece desenhado na imagem gerada, e ambos continuam se comportando como antes desta mudança em tempo de corrida (overlay de debug condicionado a `Global.DEBUG` para `ObjetoEscapada`; máscara/composição de boxes desenhada em tempo real para `ObjetoTransparencia`)

### Requirement: Geração em memória reaproveita a mesma rotina de desenho do editor
A lógica de desenho procedural do circuito (traçado de pista, zebra, box e objetos) SHALL existir em um único local reutilizável tanto pelo editor de circuitos (modo sem imagem de fundo) quanto pela geração em memória da imagem de fundo usada em corrida, evitando duas implementações que possam divergir.

#### Scenario: Editor e geração em memória usam a mesma rotina
- **WHEN** o editor de circuitos desenha um circuito em modo sem imagem de fundo, e a mesma versão do circuito é usada para gerar a imagem de fundo em memória
- **THEN** ambos os desenhos (pista, zebra, box e objetos não excluídos) são produzidos pelo mesmo componente de desenho, não por duas implementações separadas mantidas manualmente em sincronia

### Requirement: Objetos de cenário ficam numa lista separada, como pista e box
`Circuito` SHALL manter os objetos de cenário (`ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails`, `ObjetoPneus`) numa lista própria (`objetosCenario`), distinta da lista `objetos` que continua contendo `ObjetoEscapada` e `ObjetoTransparencia`. O editor de circuitos SHALL exibir e gerenciar as listas `objetos` e `objetosCenario` dentro da seção de objetos de um único `JSplitPane` lateral compartilhado com a seção de nós de pista/box (ver requisito "Editor consolida nós e objetos em um único split pane lateral"), em vez de painéis EAST/WEST separados.

#### Scenario: Criar um objeto de cenário adiciona à lista correta
- **WHEN** o usuário cria um `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` ou `ObjetoPneus` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetosCenario()`, e aparece na lista dedicada a objetos de cenário dentro da seção de objetos do split pane, não na lista `objetos`

#### Scenario: Criar Escapada ou Transparência continua indo para a lista objetos
- **WHEN** o usuário cria um `ObjetoEscapada` ou `ObjetoTransparencia` pelo editor
- **THEN** o objeto é adicionado a `circuito.getObjetos()`, como antes desta mudança, e aparece na lista `objetos` dentro da mesma seção de objetos do split pane

### Requirement: Editor consolida nós e objetos em um único split pane lateral
O editor de circuitos SHALL apresentar as listas de nós (`pistaJList`/`boxJList`) e as listas de objetos de pista (`objetos`/`objetosCenario`) em um único `JSplitPane` posicionado em uma única lateral da janela (não mais em painéis WEST e EAST separados), com uma seção dedicada a nós e outra dedicada a objetos.

#### Scenario: Nós e objetos compartilham a mesma lateral
- **WHEN** o editor de circuitos é aberto
- **THEN** as listas de nós e as listas de objetos aparecem dentro do mesmo `JSplitPane`, em uma única lateral da janela, com um divisor ajustável entre a seção de nós e a seção de objetos

### Requirement: Ações de nó ficam agrupadas na seção de nós do split pane
As ações relativas à edição de nós (inserir nó via clique com tipo selecionado, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada", remoção via tecla Delete na lista) SHALL ficar agrupadas dentro da seção de nós do split pane, em vez de espalhadas em um painel de botões separado (`buttonsPanel`) fora do split pane.

#### Scenario: Botões de ação de nó aparecem junto às listas de nó
- **WHEN** o usuário abre a seção de nós do split pane
- **THEN** os controles "Apagar Ultimo NO" e "Apaga Nó na lista Selecionada" estão visíveis dentro dessa seção, junto das listas `pistaJList`/`boxJList`

### Requirement: Ações de objeto ficam agrupadas na seção de objetos do split pane
As ações relativas a objetos de pista (criar via "Criar Objeto", remover, reordenar via Cima/Baixo/Primeiro/Ultimo, editar) SHALL ficar agrupadas dentro da seção de objetos do split pane, reutilizando `FormularioListaObjetos` para as duas listas (`objetos` e `objetosCenario`), em vez de o botão "Criar Objeto" ficar em um painel de botões separado fora do split pane.

#### Scenario: Botão Criar Objeto aparece junto às listas de objeto
- **WHEN** o usuário abre a seção de objetos do split pane
- **THEN** o controle "Criar Objeto" está visível dentro dessa seção, junto das listas `objetos` e `objetosCenario` e seus botões Cima/Baixo/Primeiro/Ultimo/Editar/Remover

### Requirement: Menu de contexto para ajuste rápido de objeto
Ao clicar com o botão direito sobre um objeto (de qualquer uma das duas listas) no editor de circuitos, o sistema SHALL exibir um menu de contexto com controles para largura, altura e ângulo de rotação, que gravam a alteração diretamente no objeto e atualizam o desenho, sem exigir abrir o formulário modal completo.

#### Scenario: Ajustar largura pelo menu de contexto
- **WHEN** o usuário clica com o botão direito sobre um objeto já posicionado e altera o valor de largura no menu de contexto exibido
- **THEN** a largura do objeto é atualizada imediatamente e o editor é redesenhado refletindo o novo tamanho

#### Scenario: Clique direito também seleciona o objeto para os atalhos de teclado
- **WHEN** o usuário clica com o botão direito sobre um objeto para abrir o menu de contexto
- **THEN** esse objeto passa a ser o alvo dos atalhos de teclado existentes (Alt+setas, Shift+setas, Z/X) até que outra seleção substitua

### Requirement: Arrastar objeto com o mouse
O editor de circuitos SHALL permitir mover um objeto (de qualquer uma das duas listas) clicando e segurando o botão esquerdo do mouse sobre sua área e arrastando, atualizando a posição do objeto em tempo real durante o arraste. Esse gesto SHALL coexistir com o atalho de teclado existente (Alt+setas) para mover objetos, sem removê-lo.

#### Scenario: Arrastar move o objeto sob o cursor
- **WHEN** o usuário pressiona o botão esquerdo do mouse sobre a área de um objeto e move o cursor antes de soltar
- **THEN** a posição do objeto acompanha o cursor durante o movimento, preservando o deslocamento relativo entre o ponto clicado e a quina do objeto, e a posição final é gravada quando o botão é solto

#### Scenario: Arrastar não dispara criação de objeto nem inserção de nó
- **WHEN** o usuário solta o botão do mouse após arrastar um objeto existente
- **THEN** esse clique de soltura não é interpretado como um clique para posicionar um novo objeto em criação nem como um clique para inserir um nó de pista
