## MODIFIED Requirements

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
