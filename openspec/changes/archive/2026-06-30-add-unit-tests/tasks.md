## 1. Infraestrutura de testes

- [x] 1.1 Adicionar dependências `org.junit.jupiter:junit-jupiter` e `org.mockito:mockito-core` (escopo `test`) ao `pom.xml`
- [x] 1.2 Configurar `maven-surefire-plugin` (versão compatível com Java 21 / JUnit 5) no `pom.xml`
- [x] 1.3 Criar diretório `src/test/java` e validar que `mvn test` roda (mesmo sem nenhum teste ainda) sem erro
- [x] 1.4 Confirmar que `mvn clean package -Ph2 -DskipTests` continua gerando o jar normalmente, sem classes/deps de teste empacotadas

## 2. Tornar LetsRace testável

- [x] 2.1 Adicionar construtor pacote-privado `LetsRace(CarregadorRecursos, ControlePaddockServidor)` em `LetsRace.java`
- [x] 2.2 Fazer o construtor público sem argumentos delegar para o novo construtor com os mesmos valores de hoje (`CarregadorRecursos.getCarregadorRecursos(false)`, `PaddockServer.getControlePaddock()`)
- [x] 2.3 Validar que o jar continua subindo e respondendo via Jersey normalmente após a mudança (smoke test manual: `GET /flmane/rest/letsRace/verificaServico`)

## 3. Testes de LetsRace

- [x] 3.1 Criar `src/test/java/br/f1mane/servidor/rest/LetsRaceTest.java` com mocks de `ControlePaddockServidor`/`ControleJogosServer` via Mockito
- [x] 3.2 Testes de autenticação: 401 quando `obterSessaoPorToken` retorna null (`jogar`, `equipe`, `dadosParciais`, `sairJogo`, `potenciaMotor`, etc.)
- [x] 3.3 Teste de autorização: 403 em `campeonato()` para sessão guest
- [x] 3.4 Testes de `processsaMensagem`: `MsgSrv`→400, `ErroServ`→500, sucesso→200, aplicados a `jogar`, `equipe`, `gravarEquipe`
- [x] 3.5 Testes de `criarSessaoNome`/`criarSessaoGoogle`: sucesso→200, `ErroServ`→500 (regressão do bug desta sessão)
- [x] 3.6 Testes de delegação simples para os endpoints de controle de piloto: `potenciaMotor`, `agressividadePiloto`, `tracadoPiloto`, `drsPiloto`, `ersPiloto`, `boxPiloto`
- [x] 3.7 Testes dos demais endpoints de leitura (`obterJogos`, `verificaServico`, `circuitos`, `classificacaoGeral`, `classificacaoEquipes`, `classificacaoCampeonato`, `dadosToken`, `atualizarDadosVisao`, `campeonatoPorId`, `sairJogo`)
- [x] 3.8 Rodar `mvn test` e confirmar 100% de sucesso em `LetsRaceTest` (37 testes)

## 4. Testes de Util.processaValorPontosCarreira (segunda prioridade)

- [x] 4.1 Criar `src/test/java/br/nnpe/UtilTest.java`
- [x] 4.2 Teste: upgrade 998→999 debita 50 pontos; downgrade 999→998 credita 50 pontos
- [x] 4.3 Teste: ciclo downgrade+upgrade no nível 999 não altera o saldo (regressão do bug de pontos infinitos)
- [x] 4.4 Teste parametrizado cobrindo a tabela de custo por faixa, incluindo o bump simétrico de transição de centena cheia (600/700/800/900) tanto no upgrade quanto no downgrade

## 5. Documentação

- [x] 5.1 Adicionar `mvn test` à seção "Build & Run" do `CLAUDE.md`

## 6. Validação final

- [x] 6.1 Rodar `mvn test` completo e confirmar 100% de sucesso (60/60 testes)
- [x] 6.2 Rodar `mvn clean package -Ph2 -DskipTests` e confirmar que o build de produção segue inalterado (jar sem classes/deps de teste)

## Follow-up (fora do escopo desta mudança)

- Endpoints de imagem (`circuitoJpg`, `circuitoBg`, `circuitoMini`, `objetoPista`, `carroCima*`, `capacete`, `png`) e `temporadas()`/`temporadasPilotos()`/`lang()`/`sobre()` não foram cobertos — são delegação simples sem ramificação de status relevante, ou (no caso de `temporadas()`) chamam um método **estático** de `CarregadorRecursos` via referência de instância, o que impede interceptar a chamada com um mock de instância sem refatorar a assinatura para `static`.
- Achado durante a implementação, não corrigido (fora do pedido original): `finalizaCampeonato()` em `LetsRace.java` sempre retorna HTTP 200 com o `campeonato` de entrada, mesmo quando `controlePaddock.finalizaCampeonato(...)` lança exceção — o retorno do controller (`ret`) é calculado mas nunca usado. Vale uma mudança própria se for intencional revisar.

## 7. Testes de ControleClassificacao

- [x] 7.1 Criar `src/test/java/br/f1mane/servidor/controles/ControleClassificacaoTest.java`
- [x] 7.2 Teste parametrizado de `gerarPontos` cobrindo posições 1-10 e uma posição fora do pódio de pontos
- [x] 7.3 Testes de `validadeDistribuicaoPontos`: upgrade único, downgrade único, e mudança simultânea em múltiplos atributos
- [x] 7.4 Testes de `atualizaCarreira`: nome vazio, nome >20 caracteres, nome duplicado (mock de `existeNomeCarro`/`existeNomePiloto`), e caminho de sucesso (21 testes)

## 8. Testes de ControleCampeonatoServidor

- [x] 8.1 Criar `src/test/java/br/f1mane/servidor/controles/ControleCampeonatoServidorTest.java`
- [x] 8.2 Teste: `criarCampeonato` com sessão de cliente nula retorna erro sem persistir
- [x] 8.3 Testes adicionais: validações de entrada (nome/piloto/corridas), campeonato em aberto, nome duplicado, jogador não encontrado, `verificaCampeonatoConcluido`, `finalizaCampeonato` (id correspondente/divergente/inexistente/exceção) (14 testes)

## 9. Testes de ControlePaddockServidor

- [x] 9.1 Criar `src/test/java/br/f1mane/servidor/controles/ControlePaddockServidorTest.java`
- [x] 9.2 Testes de `criarSessaoNome`: reaproveita sessão existente vs. cria nova com token
- [x] 9.3 Testes de `criarSessaoGoogle`/`criarSessaoVisitante`/`obterSessaoPorToken`/`renovarSessaoVisitante` (11 testes) — exercitam de ponta a ponta o caminho que causou o bug de `criarSessaoNome` desta sessão (não mockado, é a implementação real)

## 10. Testes de ControleJogosServer

- [x] 10.1 Criar `src/test/java/br/f1mane/servidor/controles/ControleJogosServerTest.java`
- [x] 10.2 Testes de `obterPilotoPorId`, `mudarGiroMotor`, `mudarAgressividadePiloto`, `boxPiloto` com piloto válido e piloto inexistente (10 testes)

## 11. Testes de ControlePersistencia

- [x] 11.1 Criar `src/test/java/br/f1mane/servidor/controles/ControlePersistenciaTest.java`
- [x] 11.2 `getSession()`/`Global.DATABASE=false` **não era testável**: `Global.DATABASE` era `public static final boolean = true` (constante de compilação), branch inalcançável. Substituído por: `carreiraDadosParaPiloto` (mapeamento puro carreira→piloto, com/sem livery) e `modoCarreira` (validação antes de gravar, via spy) (4 testes)

## 12. Validação final do incremento

- [x] 12.1 Rodar `mvn test` completo e confirmar 100% de sucesso (120/120 testes)
- [x] 12.2 Rodar `mvn clean package -Ph2 -DskipTests` e confirmar que o build de produção segue inalterado (jar sem classes/deps de teste)

## 13. Remoção de Global.DATABASE (código morto)

- [x] 13.1 Remover o campo `public static final boolean DATABASE = true` de `Global.java` — era código morto desde sempre que ficou `final`: constante de compilação, sem forma de alternar em runtime; histórico do git mostra que era alternado manualmente (`true`/`false`/`!Logger.ativo`) e recompilado, nunca configurável de fato
- [x] 13.2 Remover os 9 branches `if (!Global.DATABASE) { return ...; }` em `ControlePersistencia` (5), `ControleClassificacao` (3) e `ControlePaddockServidor` (1)
- [x] 13.3 Remover imports órfãos de `br.nnpe.Global` em `ControlePersistencia.java` e `ControlePaddockServidor.java`
- [x] 13.4 Rodar `mvn test` (120/120) e `mvn clean package -Ph2 -DskipTests` para confirmar que nada quebrou

## 14. Injeção de dependência de CarregadorRecursos em ControleClassificacao/ControleCampeonatoServidor/ControlePaddockServidor

- [x] 14.1 Adicionar construtor pacote-privado em `ControleClassificacao` recebendo `CarregadorRecursos`; construtor público delega com `CarregadorRecursos.getCarregadorRecursos(false)`
- [x] 14.2 Mesmo padrão em `ControleCampeonatoServidor`
- [x] 14.3 Mesmo padrão em `ControlePaddockServidor`, repassando o `CarregadorRecursos` injetado para as instâncias internas de `ControleCampeonatoServidor`/`ControleClassificacao` que ele constrói
- [x] 14.4 `ControleClassificacaoTest`: cobrir validação de capacete (`pinturaCapacete`, bloqueado e permitido) e de carro (`pinturaCarro`, bloqueado) em `atualizaCarreira`, com `CarregadorRecursos` mockado (3 testes novos)
- [x] 14.5 `ControleCampeonatoServidorTest`: cobrir o caminho de sucesso completo de `criarCampeonato` (grava e retorna `MsgSrv` de sucesso), com `TemporadasDefault` mockado via `CarregadorRecursos` (1 teste novo)
- [x] 14.6 `ControlePaddockServidorTest`: migrar para o construtor injetável por consistência (sem novos testes — os métodos que usam `carregadorRecursos` ali são majoritariamente geração de imagem, já fora de escopo)
- [x] 14.7 Rodar `mvn test` (124/124) e `mvn clean package -Ph2 -DskipTests` para confirmar que nada quebrou
