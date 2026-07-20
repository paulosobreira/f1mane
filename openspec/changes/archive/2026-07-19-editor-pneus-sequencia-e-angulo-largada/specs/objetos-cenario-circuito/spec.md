## MODIFIED Requirements

### Requirement: Tipos de cenário de pista disponíveis no editor
O registro de tipos de `ObjetoPista` SHALL incluir `ObjetoArquibancada`, `ObjetoConstrucao`, `ObjetoGuardRails` e `ObjetoPneus`, tornando-os criáveis e posicionáveis no editor de circuitos. `ObjetoArquibancada`, `ObjetoConstrucao` e `ObjetoGuardRails` SHALL usar o mesmo fluxo de clique-para-posicionar já usado por `ObjetoEscapada`. `ObjetoPneus` SHALL usar, em vez disso, o facilitador de corrente contínua de cliques descrito na capability `editor-pneus-multiplos-cliques` (cada segmento entre cliques consecutivos gera um `ObjetoPneus` independente, até o clique direito).

#### Scenario: Criar uma arquibancada
- **WHEN** o usuário seleciona o tipo "Arquibancada" e clica em um ponto do circuito no editor
- **THEN** um `ObjetoArquibancada` é adicionado a `circuito.getObjetos()` na posição clicada e aparece na lista de objetos do editor

#### Scenario: Criar construção e guard rails
- **WHEN** o usuário repete o fluxo de criação para os tipos "Construção" e "Guard Rails"
- **THEN** os respectivos `ObjetoConstrucao` e `ObjetoGuardRails` são adicionados a `circuito.getObjetos()` e aparecem na lista de objetos do editor

#### Scenario: Criar pneus usa a corrente contínua de cliques, não um clique único
- **WHEN** o usuário seleciona o tipo "Pneus" em "Criar Objeto"
- **THEN** o editor entra no modo de posicionamento por corrente contínua de cliques (ver `editor-pneus-multiplos-cliques`), em vez de posicionar um `ObjetoPneus` com um único clique
