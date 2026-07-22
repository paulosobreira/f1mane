## Context

A mecânica de "pintura customizada" deixa o jogador escolher, na tela web de equipe (`equipe.html`), a imagem de capacete/carro de um piloto/carro real de uma temporada específica, em vez da imagem gerada a partir das cores da própria equipe. Investigação de código (feita antes de tocar em qualquer arquivo de backend) mostrou que ela **não é uma feature isolada** — parte da infraestrutura que ela usa é compartilhada com o mecanismo geral de renderização de qualquer piloto (NPC ou jogador):

1. **UI** (`equipe.html` + `equipe.js`): seção `#escolha` (carrossel de temporadas + tabelas de capacetes/carros), populada via `GET /rest/letsRace/temporadas` e `GET /rest/letsRace/temporadas/{temporada}`, com preview via `GET /rest/letsRace/{capacete,carroLado,carroCima}/{temporada}/{id}`. **Exclusiva da mecânica** — já removida.
2. **Validação de negócio** (`ControleClassificacao.atualizaCarreira`): ao salvar, se `idCapaceteLivery`/`idCarroLivery` do request apontam para um piloto/carro cujos requisitos excedem os pontos do jogador, a gravação é rejeitada com `Lang.msg("pinturaCapacete"/"pinturaCarro", ...)`. **Exclusiva da mecânica.**
3. **Input do jogador** (`CarreiraDadosSrv.temporadaCapaceteLivery/temporadaCarroLivery/idCapaceteLivery/idCarroLivery`, `Integer`, nullable): só é preenchido quando o jogador escolhe uma livery na UI. **Exclusivo da mecânica.**
4. **Renderização geral do piloto** (`Piloto.temporadaCapaceteLivery/temporadaCarroLivery/idCapaceteLivery/idCarroLivery`, `String`): campo de saída, sempre preenchido para qualquer piloto que passe por `ControlePersistencia.carreiraDadosParaPiloto`, `ControleClassificacao.atualizarJogadoresOnlineCarreira` ou `ControleCampeonatoServidor.processaCampeonatoTOCarreira` — com a livery customizada (se o jogador escolheu) OU, no `else`, com `Util.rgb2hex(cor)` (fallback). **NÃO é exclusivo da mecânica**: é o valor que os scripts web (`jogar.js`, `resultado.js`, `campeonato.js`, `mid.js`, `classificacao_equipes.js`) leem para montar a URL de imagem de qualquer jogador, customizado ou não.
5. **Endpoints REST por temporada+id** (`LetsRace`/`ControlePaddockServidor`: `/capacete/{temporada}/{piloto}`, `/carroLado|carroCima|carroCimaSemAreofolio/{temporada}/{carro}`): recebem os dois parâmetros acima e, em `ControlePaddockServidor`, desambiguam por tamanho de string — `temporada.length()==6 && id.length()==6` → é um par de cores hex, renderiza por cor; caso contrário → é temporada real + id real, busca o piloto/carro daquela temporada em `carregarTemporadasPilotos()` e renderiza o sprite dele. **NÃO é exclusivo da mecânica**: para pilotos de IA em uma corrida, o cliente (`jogar.js`) já monta esse mesmo par diretamente como `temporadaSelecionada` (a temporada sendo jogada) + `piloto.id`, sem nenhuma customização envolvida — é assim que o jogo mostra o capacete/carro correto de cada piloto de IA daquela temporada. `/temporadas` e `/temporadas/{temporada}` também alimentam seletores de temporada em `jogar.js`, `campeonato.js` e `classificacao_temporada.js`.

Como documentado em `openspec/specs/fatjar-sem-sprites/spec.md`, o fat jar final não empacota mais `sprites/*.png`; `CarregadorRecursos`/`SpriteSheet` sempre degradam para o modelo procedural v2 nesse caso. Isso não muda a conclusão acima: o item 5 continua sendo o único mecanismo de renderização de piloto por temporada+id que existe, com ou sem sprites reais disponíveis — só muda a imagem final gerada, não o fato de que o endpoint é compartilhado.

**Correção de rota durante a implementação**: a primeira versão deste design (e da proposta) assumia que os itens 4 e 5 eram exclusivos da mecânica de escolha e propunha removê-los junto com os itens 1–3. Isso foi identificado como incorreto ao começar a editar `jogar.js` (tarefa 2.1): o código ali usa `piloto.idCapaceteLivery` para decidir entre a livery customizada e o default `temporadaSelecionada`/`piloto.id` — ou seja, o campo em si é usado por um fluxo (renderização geral de qualquer piloto) mais amplo que a mecânica sendo removida. Os itens 4 e 5 foram retirados do escopo antes de qualquer edição de backend ou dos 5 scripts JS ser feita.

## Goals / Non-Goals

**Goals:**
- Remover a mecânica de escolha de pintura customizada do modo web: a UI (`equipe.html`/`equipe.js`), a validação server-side, o input do jogador (`CarreiraDadosSrv`) e as chaves de i18n exclusivas.
- Simplificar o código de mapeamento que hoje escolhe entre "livery customizada" e "fallback de cor" (`ControlePersistencia`, `ControleClassificacao.atualizarJogadoresOnlineCarreira`, `ControleCampeonatoServidor`), já que só resta um caminho possível.
- Manter 100% intacto o comportamento de renderização observável por qualquer usuário: pilotos de IA continuam mostrando seu capacete/carro real da temporada; jogadores continuam mostrando o capacete/carro nas cores da própria equipe (que já era o único resultado visual possível, dado que sprites reais não são mais empacotadas — `fatjar-sem-sprites`).

**Non-Goals:**
- Não remove `Piloto.temporadaCapaceteLivery/temporadaCarroLivery/idCapaceteLivery/idCarroLivery` nem os endpoints REST por temporada+id — são infraestrutura de renderização geral, não exclusivos da mecânica (ver Context).
- Não mexe em `jogar.js`, `resultado.js`, `campeonato.js`, `mid.js`, `classificacao_equipes.js` — nenhum deles precisa de alteração; o valor que eles leem de `piloto.idCapaceteLivery` etc. continua sendo produzido corretamente pelo backend simplificado (sempre o fallback de cor).
- Não mexe no sistema de sprites/modelo v2 em si (`SpriteSheet`, `CarregadorRecursos`, `fatjar-sem-sprites`).
- Não mexe na tela/mecânica do modo solo (Swing, `MainFrame`) — os campos e código exclusivos da mecânica (`CarreiraDadosSrv`, a validação em `ControleClassificacao`) só existem no fluxo web; nenhuma classe Swing os referencia.
- Não adiciona uma migração de schema explícita (ver Risks).

## Decisions

- **Remover só o input do jogador (`CarreiraDadosSrv`), não o campo de saída (`Piloto`).** `CarreiraDadosSrv.temporadaCapaceteLivery` etc. representa exclusivamente a escolha feita na UI removida — sem chamador depois da remoção da UI, é código morto de verdade. Já `Piloto.temporadaCapaceteLivery` etc. é consumido por 5 scripts JS independentes da mecânica de customização; removê-lo quebraria a renderização de qualquer piloto (NPC ou jogador) nessas telas.
- **Simplificar os métodos de mapeamento em vez de removê-los.** `ControlePersistencia.carreiraDadosParaPiloto`, `ControleClassificacao.atualizarJogadoresOnlineCarreira` e `ControleCampeonatoServidor.processaCampeonatoTOCarreira` hoje têm a forma `if (livery customizada != null) { usa a livery } else { usa Util.rgb2hex(cor) }`. Como o input desaparece, cada `if` fica com uma condição que nunca é verdadeira — removê-lo e manter só o `else` preserva o comportamento observável atual (que já era sempre o fallback de cor, pois a mecânica de customização já não produzia efeito visual real) sem apagar o método inteiro.
- **Não remover os endpoints REST nem os 5 scripts JS que os consomem** (correção de rota — ver Context). Eles continuam sendo o único mecanismo de renderização de piloto por temporada+id, usado tanto para pilotos de IA (via `temporadaSelecionada`/`piloto.id` calculado no cliente) quanto para jogadores (via o campo `Piloto` sempre preenchido pelo backend).
- **Tratar como Modified Capability de `dead-code-removal`**, não como uma nova capability própria, seguindo o precedente já estabelecido no repo para remoção de mecânicas descontinuadas (ex.: `escapaTracado`/`PontoEscape`) — mas com escopo restrito ao que é de fato exclusivo da mecânica.
- **Não migrar dados históricos.** Com `hibernate.hbm2ddl.auto=update`, remover um campo de uma entidade não derruba a coluna correspondente no banco (Hibernate `update` só adiciona, nunca remove) — a coluna antiga em `CarreiraDadosSrv` fica órfã e inofensiva.

## Risks / Trade-offs

- **[Risco] Coluna órfã no banco** (a coluna de `CarreiraDadosSrv` correspondente aos 4 atributos de livery) permanece em bases MySQL/H2 já existentes → Mitigação: aceitável, são apenas bytes ociosos.
- **[Risco] Quebra de contrato JSON para clientes desatualizados** — o payload de `/rest/letsRace/equipe` deixa de conter os 4 campos de livery de `CarreiraDadosSrv`. Um client web em cache antigo (`equipe.js` de uma versão anterior) simplesmente para de mostrar a opção de escolha, sem erro. Mitigação: nenhuma ação necessária, é uma degradação graciosa.
- **[Risco evitado] Quebra de renderização em corrida/resultado/campeonato** — a hipótese inicial de remover `Piloto`/os endpoints REST/os 5 scripts JS teria quebrado a exibição de capacete/carro para todo mundo. Mitigado ao restringir o escopo antes de editar qualquer um desses arquivos (ver Context).

## Migration Plan

1. Remover UI (`equipe.html`/`equipe.js` em `html5/` e `htm5lsrc/`) — feito, sem impacto nos demais itens.
2. Remover validação em `ControleClassificacao.atualizaCarreira` e o trecho que copia a livery do request para `CarreiraDadosSrv`.
3. Remover os 4 campos de `CarreiraDadosSrv`; simplificar (não remover) o mapeamento correspondente em `ControlePersistencia.carreiraDadosParaPiloto`, `ControleClassificacao.atualizarJogadoresOnlineCarreira` e `ControleCampeonatoServidor.processaCampeonatoTOCarreira`.
4. Remover as chaves de idioma órfãs (`pinturaCarro`, `pinturaCapacete`, `pinturaCarroEscolha`, `pinturaCapaceteEscolha`) dos 4 bundles.
5. Atualizar `ControleClassificacaoTest`/`ControlePersistenciaTest` removendo/ajustando os cenários de livery que não compilam mais.
6. `mvn clean package -Ph2 -DskipTests` para atualizar o jar local antes de qualquer teste manual, por diretriz do `CLAUDE.md`.

Rollback: reverter o commit da mudança (sem migração de dados envolvida, é seguro reverter a qualquer momento).

## Open Questions

- Nenhuma pendente — escopo corrigido e confirmado por leitura direta do código consumidor (`jogar.js`, `ControlePaddockServidor`) antes de qualquer edição de backend.
