## Context

`pom.xml` hoje não tem nenhuma dependência de teste e não existe `src/test/java`. Os comandos de build documentados em `CLAUDE.md` (`mvn clean package -Ph2 -DskipTests`) sempre pulam testes — não por terem sido desabilitados de propósito, mas porque nunca existiram. A verificação de regressão é 100% manual (rodar o jogo, ou `./simulacao.sh` para corridas headless).

`LetsRace` (`br.f1mane.servidor.rest.LetsRace`) é o único recurso JAX-RS do projeto (`@Path("/letsRace")`, registrado via scan de pacote em `web.xml` → `jersey.config.server.provider.packages=br.f1mane.servidor.rest`), com 38 métodos de endpoint. Hoje ela não tem construtor explícito: os campos `controlePaddock` e `carregadorRecursos` são inicializados inline a partir de singletons estáticos (`PaddockServer.getControlePaddock()`, `CarregadorRecursos.getCarregadorRecursos(false)`). `PaddockServer.getControlePaddock()` por sua vez chama `PaddockServer.init(null)`, que constrói um `ControlePaddockServidor` real (com `ControlePersistencia`/Hibernate por trás) e **sobe uma thread** (`MonitorAtividade`). Isso significa que, do jeito que está, não dá pra instanciar `LetsRace` em teste sem acionar esse grafo inteiro — exatamente o mesmo problema de acoplamento já mapeado para `ControleSafetyCar`/`ControleCorrida`, só que aqui ele bloqueia 100% da camada REST, não só uma classe.

A maior parte da lógica de `LetsRace` é fina (extrai header/path param → chama um método do `ControlePaddockServidor` ou `ControleJogosServer` → embrulha o retorno num `Response`), mas o mapeamento desse retorno para status HTTP tem ramificação real e foi a causa direta de bug nesta sessão: `processsaMensagem(Object, String)` decide 400 (`MsgSrv`) vs 500 (`ErroServ`) vs `null` (sucesso, segue o fluxo normal), e cada endpoint individualmente decide 401 (sessão inválida via `obterSessaoPorToken`), 403 (`sessaoCliente.isGuest()`), 204 (resultado vazio) ou 200. Essa é a lógica que mais vale a pena travar com teste — é onde já vazou bug pra produção, e é pequena/determinística o suficiente para testar sem mocks pesados, desde que `ControlePaddockServidor`/`ControleJogosServer` em si sejam mockáveis (ambos são classes concretas, não-`final`, com métodos não-`final` — `Mockito.mock()` padrão funciona sem precisar de `mockito-inline`).

Já `br.nnpe.Util` (utilitários) é uma classe de funções estáticas sem dependência de UI, thread ou I/O — candidata de baixo custo, mantida como segunda prioridade desta mudança.

## Goals / Non-Goals

**Goals:**
- Ter `mvn test` funcionando e rodando testes reais no CI/dev (mesmo que o build de produção continue usando `-DskipTests`).
- Tornar `LetsRace` instanciável em teste sem subir o grafo real do servidor (Hibernate, threads), via injeção de dependência mínima.
- Cobrir com teste unitário todo endpoint de `LetsRace`: delegação correta de parâmetros para `ControlePaddockServidor`/`ControleJogosServer` (mockados) e o mapeamento de retorno para status HTTP (200/204/400/401/403/500), incluindo o caminho `processsaMensagem` que causou o bug de `criarSessaoNome` corrigido nesta sessão.
- Cobrir com teste unitário a lógica de pontos de carreira (`Util.processaValorPontosCarreira`) — a fonte do bug de pontos infinitos já corrigido, hoje sem nenhuma rede de proteção contra regressão.
- Estabelecer o padrão de onde/como escrever testes futuros (estrutura de pastas, framework, convenção de nomes) para que o restante do motor de jogo possa ganhar cobertura incrementalmente.

**Non-Goals:**
- Testar o conteúdo de negócio dentro de `ControlePaddockServidor`/`ControleJogosServer` (ex.: regra exata de criação de sessão, cálculo de classificação) — esses são mockados nos testes de `LetsRace`; testá-los de verdade é trabalho futuro, com o mesmo padrão de injeção de dependência se necessário.
- Testes de integração ponta-a-ponta (subir Tomcat embutido, requisição HTTP real) — fora de escopo; o foco é teste unitário chamando os métodos Java de `LetsRace` diretamente.
- Cobertura do motor de jogo local (`ControleCorrida`, `ControleSafetyCar`, rendering) nesta mudança — fica como follow-up, sem compromisso de spec aqui.
- Refatoração ampla de `LetsRace` ou dos controllers — a única mudança de produção é a adição de um construtor secundário em `LetsRace` para injeção; nenhum endpoint muda de comportamento.

## Decisions

- **JUnit 5 + Mockito**, escopados como `<scope>test</scope>` no `pom.xml`, ativos apenas no profile `test` já existente (que hoje só inclui `logback-test.xml`). Alternativa considerada: TestNG — descartada por não trazer vantagem aqui e por JUnit 5 ser o padrão de fato no ecossistema Java/Maven atual.
- **`maven-surefire-plugin`** explicitado no `pom.xml` (hoje ausente) fixando uma versão compatível com Java 21 e JUnit 5 (`junit-platform`), para que `mvn test` funcione de forma determinística.
- **Construtor pacote-privado em `LetsRace`** para injeção de teste:
  ```java
  public LetsRace() {
      this(CarregadorRecursos.getCarregadorRecursos(false), PaddockServer.getControlePaddock());
  }
  LetsRace(CarregadorRecursos carregadorRecursos, ControlePaddockServidor controlePaddock) {
      this.carregadorRecursos = carregadorRecursos;
      this.controlePaddock = controlePaddock;
  }
  ```
  O construtor público sem argumentos é o que o Jersey usa via scan de pacote (precisa continuar existindo e se comportando igual); o construtor pacote-privado é usado só pelos testes (mesmo pacote `br.f1mane.servidor.rest`), injetando `ControlePaddockServidor` mockado. Alternativa considerada: reflection para sobrescrever o campo `controlePaddock` em teste — descartada por ser mais frágil e menos legível que um construtor explícito; um segundo construtor é o padrão usual para recursos JAX-RS testáveis.
- **`ControleJogosServer` também mockado** (não instanciado de verdade): como ele é obtido via `controlePaddock.getControleJogosServer()`, basta o mock de `ControlePaddockServidor` retornar um mock de `ControleJogosServer` em `when(...)`.
- **Estrutura espelhada**: `src/test/java/br/f1mane/servidor/rest/LetsRaceTest.java`, `src/test/java/br/nnpe/UtilTest.java` — convenção Maven padrão, zero configuração extra de `testSourceDirectory`.
- **Build de produção inalterado**: `-DskipTests` continua nos comandos documentados de `mvn package`. `mvn test` passa a ser o comando para rodar a suíte explicitamente.

## Risks / Trade-offs

- [O construtor pacote-privado é uma mudança em código de produção, mesmo que pequena] → Mitigação: o construtor público (usado pelo Jersey em runtime) delega para o novo construtor com exatamente os mesmos valores que os field initializers atuais produziam — comportamento de produção idêntico, validado rodando o jar empacotado depois da mudança.
- [38 endpoints é uma superfície grande; cobrir todos com o mesmo nível de detalhe pode ser desproporcional] → Mitigação: priorizar os endpoints que (a) têm ramificação de status HTTP (401/403/400/500/204) e (b) já causaram bug real (`criarSessaoNome`, `processsaMensagem`); endpoints que só repassam um valor sem decisão (ex.: `verificaServico`) recebem um teste simples de delegação, não uma suíte extensa.
- [Versão do `maven-surefire-plugin` incompatível com o `maven.compiler.release=21` já usado] → Mitigação: usar a mesma versão major já validada para Java 21 (Surefire 3.x), testada localmente antes de finalizar a tarefa.

## Migration Plan

1. Adicionar dependências de teste e plugin do Surefire ao `pom.xml`.
2. Adicionar o construtor pacote-privado em `LetsRace` (sem alterar o construtor público).
3. Criar `src/test/java/br/f1mane/servidor/rest/LetsRaceTest.java` cobrindo autenticação/autorização, `processsaMensagem` e os endpoints de maior risco.
4. Criar `src/test/java/br/nnpe/UtilTest.java` cobrindo a tabela de custo/refund de pontos.
5. Rodar `mvn test` e confirmar que passa; rodar `mvn clean package -Ph2 -DskipTests` e confirmar que o jar gerado continua idêntico em comportamento.
6. Atualizar `CLAUDE.md` com o comando `mvn test`.

Não há rollback de dados ou produção envolvido — é só adição de testes, dependências de escopo `test`, e um construtor adicional não-destrutivo.

## Open Questions

- Vale a pena, numa próxima mudança, aplicar o mesmo padrão de injeção de dependência a `ControlePaddockServidor`/`ControleJogosServer` para testar a lógica de negócio real (não só a camada REST mockada)? **Resolvido**: não é necessário — ver decisão abaixo, essas classes já são construtor-injetadas.

## Decisão adicional: cobertura de `br.f1mane.servidor.controles`

Diferente de `LetsRace`, as 5 classes do pacote já recebem suas dependências via construtor: `ControleCampeonatoServidor(ControlePersistencia, ControlePaddockServidor)`, `ControleClassificacao(ControlePersistencia, ControleCampeonatoServidor)`, `ControleJogosServer(DadosPaddock, ControleClassificacao, ControleCampeonatoServidor, ControlePersistencia, ControlePaddockServidor)`, `ControlePaddockServidor(ControlePersistencia)`. `ControlePersistencia` em si não é `final`, seus métodos (`getSession()`, `carregaCarreiraJogador`, `existeNomeCarro`, `gravarDados`, etc.) também não são `final` — mockável direto com `Mockito.mock(ControlePersistencia.class)`. **Nenhuma mudança de produção é necessária** para tornar essas 5 classes testáveis.

**Atualização**: `ControleClassificacao`, `ControleCampeonatoServidor` e `ControlePaddockServidor` tinham um campo `carregadorRecursos` inicializado inline via `CarregadorRecursos.getCarregadorRecursos(false)` (não injetado), igual ao problema original de `LetsRace` antes do refactor. Isso foi corrigido com o mesmo padrão de `LetsRace`: cada uma das 3 classes ganhou um construtor pacote-privado extra recebendo `CarregadorRecursos`, com o construtor público delegando para ele com o valor real (`CarregadorRecursos.getCarregadorRecursos(false)`) — comportamento de produção idêntico. `ControlePaddockServidor` repassa o `CarregadorRecursos` injetado para as instâncias internas de `ControleCampeonatoServidor`/`ControleClassificacao` que ele mesmo constrói, então o grafo inteiro compartilha a mesma instância (real ou mock). Isso desbloqueou teste das validações de livery/capacete em `atualizaCarreira` e do caminho de sucesso completo de `criarCampeonato` (que dependia de `carregarTemporadasPilotosDefauts()`).

Critério de priorização por classe (do mesmo jeito que em `LetsRace`): cobrir métodos com ramificação de regra de negócio real; pular getters/setters e delegações de uma linha sem decisão.

- `ControleClassificacao`: `gerarPontos` (tabela de pontos por posição — pura, zero mock), `validadeDistribuicaoPontos`/`redistribuiPontos` (static, orquestra `Util.processaValorPontosCarreira` — sequência direta do bug de `f47e7a74`), `atualizaCarreira` (validação de nome/tamanho/duplicidade, mock de `ControlePersistencia`).
- `ControleCampeonatoServidor`: `criarCampeonato`/`finalizaCampeonato` (validação de sessão nula, regras de ciclo de vida).
- `ControlePaddockServidor`: `criarSessaoNome`/`criarSessaoGoogle`/`criarSessaoVisitante`/`obterSessaoPorToken` (a mesma lógica que `LetsRaceTest` hoje só vê mockada).
- `ControleJogosServer`: `mudarGiroMotor`, `mudarAgressividadePiloto`, `mudarTracadoPiloto`, `mudarDrs`, `mudarErs`, `boxPiloto` — validação de piloto/sessão antes de aplicar a mudança.
- `ControlePersistencia`: `getSession()` (branch `Global.DATABASE`) — cobertura mais rasa, é majoritariamente wrapper de CRUD do Hibernate.
