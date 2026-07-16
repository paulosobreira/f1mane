## ADDED Requirements

### Requirement: Nenhum avanço automático dos carousels de seleção
Nenhum carousel usado para seleção de temporada ou circuito (`#temporadaCarousel`, `#circuitoCarousel`) SHALL avançar automaticamente por temporização, independentemente da página em que aparece ou de quantos itens ele contém.

#### Scenario: classificacao_temporada.html com múltiplas temporadas
- **WHEN** `classificacao_temporada.html` carrega e `listaTemporadas()` popula duas ou mais temporadas em `#temporadaCarousel`
- **THEN** a temporada selecionada permanece a mesma indefinidamente, sem trocar sozinha após ~5 segundos

#### Scenario: resultado.html
- **WHEN** `resultado.html` é carregado com o carousel `#temporadaCarousel` (`data-ride="carousel"`) presente
- **THEN** o carousel não inicia auto-rotação, mesmo que futuramente seja populado com múltiplos itens

#### Scenario: Páginas já protegidas continuam protegidas
- **WHEN** `jogar.html`, `campeonato.html` ou `equipe.html` são carregadas
- **THEN** os carousels de temporada e circuito continuam sem auto-rotação, como já ocorre hoje via `interval:false`

### Requirement: Seleção estável até ação explícita do usuário
A temporada e o circuito exibidos/selecionados SHALL mudar somente em resposta a uma ação explícita do usuário (clique em controle, indicador ou item do carousel), nunca por temporizador.

#### Scenario: Usuário não interage com o seletor
- **WHEN** o usuário carrega `jogar.html`, `campeonato.html`, `equipe.html`, `resultado.html` ou `classificacao_temporada.html` e não clica em nenhum controle do carousel de temporada/circuito
- **THEN** a temporada e o circuito exibidos permanecem os mesmos desde o carregamento inicial, por tempo indefinido
