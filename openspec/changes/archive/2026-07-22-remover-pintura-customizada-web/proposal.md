## Why

O modo carreira web (`equipe.html`) permite ao jogador escolher, para o capacete e para o carro, a pintura de um piloto/carro real de uma temporada específica (por ex. "usar a pintura do carro do piloto X de 2019"), com validação de requisitos (`pinturaCarro`/`pinturaCapacete`). Essa mecânica depende das imagens reais de sprites por temporada (`sprites/tANO.png`), que — conforme já documentado em `fatjar-sem-sprites` — não são mais empacotadas no fat jar; a renderização de carros/capacetes hoje sempre cai no modelo procedural v2 (`pintarModeloV2`), gerado a partir das cores da equipe. A mecânica de escolha de pintura customizada ficou órfã: sua tela, validação e persistência não têm mais efeito visual real no jogo.

## What Changes

- **BREAKING**: Remove da tela de equipe web (`equipe.html`/`equipe.js`) a seção "escolha" de pintura — carrossel de temporadas, tabelas de capacetes/carros por piloto/temporada, e os cliques nas imagens de capacete/carro que abriam essa seleção.
- **BREAKING**: Remove a validação server-side de pintura em `ControleClassificacao.atualizaCarreira` (blocos que usam `pinturaCapacete`/`pinturaCarro` para bloquear a gravação quando o piloto/carro real escolhido exige mais pontos do que o jogador tem).
- Remove os campos `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery`, `idCarroLivery` — o **input do jogador** — de `CarreiraDadosSrv` e do trecho de `ControleClassificacao.atualizaCarreira` que os copiava para lá a partir do request.
- Simplifica (sem remover) o código que mapeia `CarreiraDadosSrv` → `Piloto`/`CampeonatoTO` em `ControlePersistencia.carreiraDadosParaPiloto`, `ControleClassificacao.atualizarJogadoresOnlineCarreira` e `ControleCampeonatoServidor.processaCampeonatoTOCarreira`: cada um tinha um `if (livery customizada != null) { usa a livery } else { usa Util.rgb2hex(cor) }`; como o input deixa de existir, cada um passa a sempre usar o fallback de cor (o branch `if` morre, o `else` vira o único caminho).
- Remove as chaves de idioma `pinturaCarro`, `pinturaCapacete`, `pinturaCarroEscolha`, `pinturaCapaceteEscolha` dos 4 bundles (`mensagens_pt/en/es/it.properties`).
- Atualiza/remove os testes que hoje cobrem esse comportamento (`ControleClassificacaoTest`, `ControlePersistenciaTest`) para refletir a remoção.

### Fora de escopo (investigado e descartado)

Uma investigação inicial cogitou remover também os 4 campos de `Piloto` (`temporadaCapaceteLivery`/`temporadaCarroLivery`/`idCapaceteLivery`/`idCarroLivery`), os endpoints REST `/temporadas`, `/temporadas/{temporada}`, `/capacete/{temporada}/{piloto}`, `/carroLado/{temporada}/{carro}`, `/carroCima/{temporada}/{carro}`, `/carroCimaSemAreofolio/{temporada}/{carro}`, e o código correspondente em `jogar.js`/`resultado.js`/`campeonato.js`/`mid.js`/`classificacao_equipes.js`. Isso foi descartado ao implementar: esses campos e endpoints **não são exclusivos** da mecânica de escolha — são o mecanismo geral de renderização de qualquer piloto (NPC ou jogador), onde o mesmo par `temporada/id` ora contém a referência real (para pilotos de IA daquela temporada, calculado no cliente como `temporadaSelecionada`/`piloto.id`) ora contém um par de cores em hex de 6 caracteres (fallback do próprio jogador, decidido no backend por `length()==6` em `ControlePaddockServidor`). `/temporadas` e `/temporadas/{temporada}` também alimentam seletores de temporada em `jogar.js`, `campeonato.js` e `classificacao_temporada.js`, sem relação com a escolha de pintura. Remover qualquer um desses quebraria a renderização de carros/capacetes em telas de corrida, resultado e campeonato para todo mundo, não só para quem usava a customização.

## Capabilities

### New Capabilities
_Nenhuma._

### Modified Capabilities
- `dead-code-removal`: adiciona requisito/cenários cobrindo a remoção da mecânica de escolha de pintura de capacete/carro customizada no modo web (UI, validação server-side, campos de persistência e endpoints exclusivos), seguindo o mesmo padrão já usado para outras mecânicas descontinuadas (ex.: `escapaTracado`).

## Impact

- **Frontend web**: `src/main/webapp/html5/equipe.html`, `src/main/webapp/htm5lsrc/equipe.html`, `src/main/webapp/html5/js/equipe.js`. (`jogar.js`, `resultado.js`, `campeonato.js`, `mid.js`, `classificacao_equipes.js` e o REST `LetsRace`/`ControlePaddockServidor` **não são afetados** — ver "Fora de escopo" acima.)
- **Backend**: `ControleClassificacao` (validação + `atualizarJogadoresOnlineCarreira`), `ControlePersistencia` (`carreiraDadosParaPiloto`), `ControleCampeonatoServidor` (`processaCampeonatoTOCarreira`), `CarreiraDadosSrv`. `Piloto` não muda.
- **i18n**: `mensagens_pt/en/es/it.properties`.
- **Persistência**: a coluna correspondente aos 4 atributos de livery em `CarreiraDadosSrv` deixa de ser gravada; jogadores com valores antigos gravados no banco simplesmente deixam de ser lidos (o mapeamento para `Piloto` já sempre calcula o fallback de cor).
- **Testes**: `ControleClassificacaoTest`, `ControlePersistenciaTest` (ambos já têm cenários específicos de livery a remover/ajustar).
