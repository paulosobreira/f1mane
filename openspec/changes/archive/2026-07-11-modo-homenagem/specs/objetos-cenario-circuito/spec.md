## MODIFIED Requirements

### Requirement: Imagem de fundo do circuito pode ser gerada em memória em vez de lida de arquivo
O sistema SHALL oferecer uma flag booleana `Global.MODO_HOMENAGEM` (default `true`) que, quando ativa, faz todo carregamento da imagem de fundo do circuito (`circuitos/*_mro.jpg`) ser substituído pela geração em memória dessa imagem, reproduzindo o mesmo desenho procedural de pista, zebra, box e objetos de cenário já usado pelo editor de circuitos quando este está em modo sem imagem de fundo. Com a flag desativada, o comportamento SHALL permanecer idêntico ao anterior a esta mudança (leitura do arquivo em disco). Esta flag substitui a antiga `Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA`, removida.

#### Scenario: Flag desativada preserva o comportamento atual
- **WHEN** `Global.MODO_HOMENAGEM` é `false` e uma corrida solo carrega um circuito
- **THEN** a imagem de fundo é lida do arquivo `circuitos/<nome>_mro.jpg`, exatamente como antes desta mudança

#### Scenario: Flag ativada gera a imagem em memória no modo solo
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e uma corrida solo carrega um circuito
- **THEN** `PainelCircuito` usa como imagem de fundo uma imagem gerada em memória a partir da geometria da pista e dos objetos do circuito, sem ler `circuitos/<nome>_mro.jpg` do disco

#### Scenario: Flag ativada gera a imagem em memória para o endpoint do multiplayer
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e o endpoint REST `/circuitoJpg/{nmCircuito}` é chamado pelo cliente Java do multiplayer
- **THEN** o servidor responde com uma imagem gerada em memória (codificada como jpg), em vez do arquivo estático em disco
