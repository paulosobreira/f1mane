## 1. Estrutura e cabeçalho do SDD

- [x] 1.1 Criar `docs/sdd.md` com cabeçalho, índice de seções e introdução de 2-3 parágrafos sobre o projeto F1 Mane

## 2. Modos de execução

- [x] 2.1 Documentar seção **Modos de Execução** com tabela dos 3+1 entry points (`MainLauncher`, `MainFrame`, `AppletPaddock`, `MainFrameSimulacao`) e o que cada um inicializa
- [x] 2.2 Documentar o que `MainLauncher` faz internamente: extração de `webapp/`, Tomcat na porta 8080, launcher Swing com modos web/solo/multiplayer, flag `--headless`

## 3. Engine de jogo (local)

- [x] 3.1 Documentar seção **Engine de Jogo** com diagrama de composição de `ControleJogoLocal` e seus subsistemas (`controleCorrida`, `gerenciadorVisual`, `controleEstatisticas`, `controleCampeonato`)
- [x] 3.2 Documentar o tick loop de `ControleCiclo`: sequência de operações, temporização via `tempoCicloCircuito()` (~50ms), o delay inicial de largada com 5 piscadas
- [x] 3.3 Documentar `ControleCorrida` (física de corrida), `ControleBox` (pit stops, `boxEquipes`, `boxRapido`), `ControleSafetyCar` (`ThreadRecolhimentoCarro`), `ControleClima` (`ThreadMudancaClima`)

## 4. Camada servidor / multiplayer

- [x] 4.1 Documentar seção **Multiplayer** com `PaddockServer` como singleton e sua sequência de inicialização
- [x] 4.2 Documentar a REST API de `LetsRace`: endpoints, autenticação por token no header, formato de resposta JSON
- [x] 4.3 Documentar `SessaoCliente` (campos de estado do jogador), `ControleJogosServer` (`mapaJogosCriados`, limite `MaxJogo=5`), e o papel de `JogoServidor` (estende `ControleJogoLocal`)
- [x] 4.4 Documentar `JogoCliente` com nota explícita de implementação parcial de `InterfaceJogo`

## 5. Modelo de dados

- [x] 5.1 Documentar seção **Modelo de Dados** com os campos relevantes de `Piloto`: identidade, posição na corrida, estado físico, flags de feature (ERS, DRS, box)
- [x] 5.2 Documentar `Carro`: constantes de tipo de pneu, estados de dano, posições de asa, campos de desgaste/temperatura/combustível
- [x] 5.3 Documentar `No`: constantes de tipo por cor, campos `point`, `index`, `tracado`, marcadores de entrada/saída do box
- [x] 5.4 Documentar `Circuito`: listas `pista`/`pistaFull`/`box`/`boxFull`, campos de configuração, método `vetorizarPista()`

## 6. Persistência

- [x] 6.1 Documentar seção **Persistência** com os perfis Maven H2/MySQL e como o filtro de build injeta os valores JDBC em `META-INF/persistence.xml`
- [x] 6.2 Documentar `F1ManeDados` (`@MappedSuperclass`) e as 6 entidades concretas com seus nomes de tabela e relacionamentos `@OneToMany`

## 7. Rendering

- [x] 7.1 Documentar seção **Rendering** com o modelo Swing de `PainelCircuito` (`paintComponent`), as 3 flags estáticas de rendering e os overlays de debug
- [x] 7.2 Documentar `SpriteSheet`: layout de pixels por temporada, constantes (`LADO_W=180`, `CIMA_W=90`, `CAP_PER_ROW=12`), métodos de extração e fórmula de índice de capacete

## 8. Recursos e i18n

- [x] 8.1 Documentar seção **Recursos** com `CarregadorRecursos` (singleton, caches estáticos, `XMLDecoder` para circuitos, 37 circuitos em `resources/circuitos/`)
- [x] 8.2 Documentar o sistema de i18n: `Lang.msg()`, bundles `mensagens_XX.properties`, modo servidor com encoding `¢key¢`, header `idioma` por request

## 9. Revisão e consistência

- [x] 9.1 Revisar o documento completo: verificar que todos os nomes de classe, constantes e caminhos de arquivo correspondem ao código real
- [x] 9.2 Adicionar links de âncora entre seções no índice
