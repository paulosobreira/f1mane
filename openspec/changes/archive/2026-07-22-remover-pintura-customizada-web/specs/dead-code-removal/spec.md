## ADDED Requirements

### Requirement: Mecânica de pintura customizada de capacete/carro é removida do modo web
O input do jogador para escolher, no modo carreira web, uma pintura de capacete/carro de um piloto/carro real de uma temporada específica (em vez da imagem gerada pelas cores da própria equipe) NÃO SHALL permanecer no código de produção — nem sua UI, nem sua validação server-side, nem o campo de input em `CarreiraDadosSrv`. O mecanismo geral de renderização de piloto por temporada+id (campos de saída em `Piloto`, endpoints REST `/capacete`, `/carroLado`, `/carroCima`, `/carroCimaSemAreofolio`, `/temporadas`) É COMPARTILHADO com a exibição de pilotos de IA e SHALL permanecer inalterado.

#### Scenario: Seção de escolha removida de equipe.html
- **WHEN** `src/main/webapp/html5/equipe.html` e `src/main/webapp/htm5lsrc/equipe.html` são lidos
- **THEN** a seção `#escolha` (carrossel de temporadas e tabelas `#tableCapacetes`/`#tableCarro`) e os inputs ocultos `#temporadaCapaceteLivery`, `#temporadaCarroLivery`, `#idCapaceteLivery`, `#idCarroLivery` não existem

#### Scenario: Funções de escolha removidas de equipe.js
- **WHEN** `src/main/webapp/html5/js/equipe.js` é lido
- **THEN** as funções `selecionaTemporada`, `listaTemporadas`, `gerarTrCapaceteCores` e `gerarTrCarro` não existem, e `objetoEquipe()` não inclui `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery` nem `idCarroLivery`

#### Scenario: Validação de pintura removida de ControleClassificacao
- **WHEN** `ControleClassificacao.atualizaCarreira` é lido
- **THEN** não existe nenhum bloco que compare `idCapaceteLivery`/`idCarroLivery` de `carreiraDados` contra atributos de um piloto/carro de `carregarTemporadasPilotos()` para rejeitar a gravação

#### Scenario: Campo de input de livery removido de CarreiraDadosSrv
- **WHEN** `CarreiraDadosSrv.java` é lido
- **THEN** os campos `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery`, `idCarroLivery` e seus getters/setters não existem

#### Scenario: Mapeamento simplificado para o único caminho restante (fallback de cor)
- **WHEN** `ControlePersistencia.carreiraDadosParaPiloto`, `ControleClassificacao.atualizarJogadoresOnlineCarreira` e `ControleCampeonatoServidor.processaCampeonatoTOCarreira` são lidos
- **THEN** nenhum dos três contém um branch condicional que leia `getTemporadaCapaceteLivery()`/`getIdCapaceteLivery()`/`getTemporadaCarroLivery()`/`getIdCarroLivery()` de `CarreiraDadosSrv`; cada um preenche os campos correspondentes de `Piloto`/`CampeonatoTO` diretamente com `Util.rgb2hex(...)`

#### Scenario: Chaves de idioma órfãs removidas
- **WHEN** `mensagens_pt.properties`, `mensagens_en.properties`, `mensagens_es.properties` e `mensagens_it.properties` são lidos
- **THEN** nenhum deles contém as chaves `pinturaCarro`, `pinturaCapacete`, `pinturaCarroEscolha` ou `pinturaCapaceteEscolha`

#### Scenario: Campos de saída em Piloto e endpoints REST por temporada+id permanecem intactos
- **WHEN** `Piloto.java`, `LetsRace.java` e `ControlePaddockServidor.java` são lidos
- **THEN** os campos `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery`, `idCarroLivery` de `Piloto`, e os endpoints `/temporadas`, `/temporadas/{temporada}`, `/capacete/{temporada}/{piloto}`, `/carroLado/{temporada}/{carro}`, `/carroCima/{temporada}/{carro}`, `/carroCimaSemAreofolio/{temporada}/{carro}` continuam existindo sem alteração, pois são usados pela renderização geral de pilotos de IA e de jogadores em `jogar.js`, `resultado.js`, `campeonato.js`, `mid.js` e `classificacao_equipes.js`
