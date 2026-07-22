## 1. Frontend web — UI de escolha

- [x] 1.1 Remover a seção `#escolha` (carrossel de temporadas, `#tableCapacetes`, `#tableCarro`) e os inputs ocultos `#temporadaCapaceteLivery`, `#temporadaCarroLivery`, `#idCapaceteLivery`, `#idCarroLivery` de `src/main/webapp/html5/equipe.html`
- [x] 1.2 Aplicar a mesma remoção em `src/main/webapp/htm5lsrc/equipe.html`
- [x] 1.3 Remover de `src/main/webapp/html5/js/equipe.js` as funções `selecionaTemporada`, `listaTemporadas`, `gerarTrCapaceteCores`, `gerarTrCarro`, os binds de clique em `#idImgCapacete`/`#idImgCarroCima`/`#idImgCarroLado` que abrem a seleção, e as labels `pinturaCapaceteEscolha`/`pinturaCarroEscolha`
- [x] 1.4 Atualizar `carregaEquipe()` e `objetoEquipe()` em `equipe.js` para não ler/enviar mais `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery`, `idCarroLivery`, montando a URL de capacete/carro sempre a partir das cores da equipe

## 2. Frontend web — telas de corrida/resultado/campeonato (NÃO EXECUTAR)

> Escopo descartado durante a implementação: `jogar.js`, `resultado.js`, `campeonato.js`, `mid.js`, `classificacao_equipes.js` consomem `Piloto.idCapaceteLivery`/`temporadaCapaceteLivery`/etc. como o mecanismo **geral** de renderização de qualquer piloto (NPC via `temporadaSelecionada`/`piloto.id`, jogador via o fallback de cor que o backend sempre preenche) — não é código exclusivo da mecânica de escolha customizada. Ver design.md § Context ("Correção de rota"). Nenhum dos 5 arquivos precisa de alteração.

- [x] 2.1 ~~jogar.js~~ — não aplicável, ver nota acima
- [x] 2.2 ~~resultado.js~~ — não aplicável
- [x] 2.3 ~~campeonato.js~~ — não aplicável
- [x] 2.4 ~~mid.js~~ — não aplicável
- [x] 2.5 ~~classificacao_equipes.js~~ — não aplicável

## 3. Backend — validação e input do jogador

- [x] 3.1 Remover de `ControleClassificacao.atualizaCarreira` os dois blocos que validam `idCapaceteLivery`/`idCarroLivery` contra `carregarTemporadasPilotos()` e retornam `Lang.msg("pinturaCapacete"/"pinturaCarro", ...)`, e o trecho que copia esses 4 campos do `carreiraDados` (request) para o `carreiraDadosSrv` a ser persistido (também removidos, por ficarem sem uso: o campo `carregadorRecursos` e o construtor pacote-privado de 3 args de `ControleClassificacao`, com o call site em `ControlePaddockServidor` ajustado)
- [x] 3.2 Remover os campos `temporadaCapaceteLivery`, `temporadaCarroLivery`, `idCapaceteLivery`, `idCarroLivery` (e getters/setters) de `CarreiraDadosSrv.java`
- [x] 3.3 Simplificar `ControlePersistencia.carreiraDadosParaPiloto`: remover os 4 branches `if (carreiraDadosSrv.getXxxLivery() != null) {...} else { Util.rgb2hex(...) }`, mantendo só a chamada a `Util.rgb2hex(...)` (o `Piloto` de destino continua com os mesmos 4 campos, agora sempre preenchidos por cor)
- [x] 3.4 Simplificar da mesma forma `ControleClassificacao.atualizarJogadoresOnlineCarreira`
- [x] 3.5 Simplificar da mesma forma `ControleCampeonatoServidor.processaCampeonatoTOCarreira`, mantendo as atribuições de fallback já existentes

## 4. Backend — endpoints REST (NÃO EXECUTAR)

> Escopo descartado: `/temporadas`, `/temporadas/{temporada}`, `/capacete/{temporada}/{piloto}`, `/carroLado|carroCima|carroCimaSemAreofolio/{temporada}/{carro}` em `LetsRace`/`ControlePaddockServidor` são o mecanismo geral de renderização por temporada+id (usado por pilotos de IA e como destino do fallback de cor), não exclusivos da mecânica de escolha. Permanecem inalterados.

- [x] 4.1 ~~Remover endpoints de LetsRace.java~~ — não aplicável, ver nota acima
- [x] 4.2 ~~Remover métodos de ControlePaddockServidor.java~~ — não aplicável

## 5. i18n

- [x] 5.1 Remover as chaves `pinturaCarro`, `pinturaCapacete`, `pinturaCarroEscolha`, `pinturaCapaceteEscolha` de `mensagens_pt.properties`, `mensagens_en.properties`, `mensagens_es.properties` e `mensagens_it.properties`

## 6. Testes

- [x] 6.1 Remover/ajustar em `ControleClassificacaoTest.java` os cenários que exercitam a validação de livery (linhas que hoje usam `setTemporadaCapaceteLivery`/`setIdCapaceteLivery`/`setTemporadaCarroLivery`/`setIdCarroLivery`)
- [x] 6.2 Remover em `ControlePersistenciaTest.java` o teste `carreiraDadosParaPiloto_comLiveryDefinido_usaIdDaLivery` (não compila mais, pois `CarreiraDadosSrv` perde os setters); manter/ajustar `carreiraDadosParaPiloto_semLiveryDefinido_usaCorComoFallback`, que passa a documentar o único caminho existente
- [x] 6.3 Rodar `mvn test` e confirmar que a suíte passa sem os cenários removidos (808 testes, 0 falhas/erros, incluindo `ControlePaddockServidorTest`, `ControleCampeonatoServidorTest`, `LetsRaceTest` e `LangBundlesLauncherKeysTest` — todos intactos)

## 7. Build e verificação final

- [x] 7.1 Rodar `mvn clean package -Ph2 -DskipTests` para atualizar `target/flmane.jar` com as remoções (build OK; conteúdo do jar verificado sem `escolha`/`pintura`/`Livery`/`selecionaTemporada` nos arquivos web e sem as chaves de idioma removidas)
- [ ] 7.2 Testar manualmente a tela de equipe web (`equipe.html`) confirmando que a seção de escolha não aparece mais e que salvar/carregar equipe continua funcionando com a imagem por cor (pendente — requer teste em navegador, fora do alcance deste ambiente)
