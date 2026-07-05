## ADDED Requirements

### Requirement: Controles de nós de pista ficam num único grid de 1 coluna
Os controles da seção de nós de pista do editor de circuitos (tipo de nó, lado do box, lado de saída do box, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada") SHALL ficar organizados num único painel de grid de 1 coluna (uma linha por controle), em vez de sub-painéis separados que hoje misturam arranjo empilhado com arranjo lado a lado.

#### Scenario: Controles de nós aparecem um por linha
- **WHEN** o editor de circuitos é aberto
- **THEN** tipo de nó, lado do box, lado de saída do box, e os botões de apagar nó aparecem como linhas distintas de um único grid de 1 coluna, na seção de nós de pista

### Requirement: Linhas da barra superior do editor têm altura visual consistente
As linhas visuais da barra superior do editor de circuitos (`gerarTopoNavegacaoEAcoes`) que hoje ficam com alturas diferentes — a linha com nome do circuito/ativo/% chuva e a linha seguinte (largura da pista/noite/cores) — SHALL ter a mesma altura.

#### Scenario: Duas linhas de campos têm a mesma altura
- **WHEN** a barra superior do editor de circuitos é renderizada com largura suficiente para quebrar em pelo menos duas linhas visuais
- **THEN** a linha com nome do circuito/ativo/% chuva e a linha com largura da pista/noite/cores têm a mesma altura

### Requirement: Listas de objetos têm rótulos identificando desenho e função
A seção de objetos do editor de circuitos SHALL exibir um rótulo "Objetos Desenho" acima da lista de objetos de cenário (`objetosCenario`) e um rótulo "Objetos Função" acima da lista de objetos de função (`objetos`).

#### Scenario: Rótulos aparecem acima de cada lista
- **WHEN** a seção de objetos do editor de circuitos é aberta
- **THEN** o rótulo "Objetos Desenho" aparece acima da lista de cima (objetos de cenário) e o rótulo "Objetos Função" aparece acima da lista de baixo (objetos de função)
