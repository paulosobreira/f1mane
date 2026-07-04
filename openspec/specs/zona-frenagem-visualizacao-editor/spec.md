# zona-frenagem-visualizacao-editor

## Purpose

Permitir ao projetista de circuitos visualizar, no editor, onde as zonas de frenagem foram detectadas automaticamente ao longo do traçado, sem que essa marcação vaze para a imagem de fundo usada em corrida real.

## Requirements

### Requirement: Editor de circuitos marca visualmente o trecho de pista identificado como zona de frenagem
O editor de circuitos SHALL desenhar uma marcação visual com cor diferenciada (por exemplo, um tom acinzentado) sobre o trecho da pista correspondente à zona de frenagem detectada automaticamente, permitindo ao projetista do circuito identificar visualmente onde essa zona começa e termina ao longo do traçado.

#### Scenario: Zona de frenagem aparece destacada ao abrir o circuito no editor
- **WHEN** um circuito com uma zona de frenagem detectada é aberto no editor de circuitos
- **THEN** o trecho de pista correspondente à zona de frenagem é desenhado com a cor/marcação diferenciada, distinguível do restante do traçado

#### Scenario: Circuito sem zona de frenagem detectada não mostra marcação
- **WHEN** um circuito não tem nenhum trecho que satisfaça os critérios de zona de frenagem
- **THEN** nenhuma marcação de zona de frenagem é desenhada no traçado

### Requirement: Marcação visual da zona de frenagem não afeta a imagem usada em corrida real
A marcação visual da zona de frenagem SHALL ser desenhada por uma rotina exclusiva do editor de circuitos, separada da rotina de desenho de pista/zebra/box compartilhada com a geração de imagem de fundo usada em corrida real (`DesenhoProceduralCircuito.desenhaPistaZebraEBox`), de forma que essa marcação nunca apareça na imagem de fundo usada durante uma corrida (solo, multiplayer, ou gerada em memória).

#### Scenario: Imagem gerada em memória para corrida não contém a marcação da zona de frenagem
- **WHEN** `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA` está ativo e a imagem de fundo é gerada para uma corrida real a partir de um circuito com zona de frenagem detectada
- **THEN** a imagem gerada não contém a marcação visual diferenciada da zona de frenagem, apenas o traçado normal da pista

#### Scenario: Editor ainda mostra a marcação ao lado da mesma rotina de desenho compartilhada
- **WHEN** o editor desenha o traçado da pista usando `DesenhoProceduralCircuito.desenhaPistaZebraEBox`
- **THEN** a marcação da zona de frenagem aparece desenhada por cima, numa chamada separada, exclusiva do editor
