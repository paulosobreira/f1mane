## Why

O projeto não tem nenhum teste automatizado (`src/test` não existe, sem JUnit/Mockito no `pom.xml`). Toda verificação hoje é manual. Isso já causou regressão real: o bug de "pontos infinitos no nível 999" (commit `f47e7a74`) ficou em produção até alguém notar manualmente. Na mesma sessão de hoje, dois bugs distintos passaram batido exatamente pela porta de entrada do multiplayer — a classe REST `LetsRace`: `criarSessaoNome` retornava 400 sem nenhuma mensagem útil porque `ErroServ` não serializava em JSON, e a causa raiz (schema/JDBC mal configurado) só apareceu ao ler o log do servidor manualmente. `LetsRace` é o único ponto de entrada HTTP de todo o modo multiplayer (38 endpoints) — autenticação, criação de sessão, carregamento de circuito, jogo em corrida, campeonato, imagens. Qualquer regressão de mapeamento de erro (401/403/400/500) ou de delegação incorreta para `ControlePaddockServidor`/`ControleJogosServer` quebra o jogo pra todo cliente web, e hoje não há nenhuma rede de proteção.

## What Changes

- Adiciona JUnit 5 + Mockito como dependência de teste no `pom.xml`, escopada ao profile `test`, sem afetar o jar de produção.
- Cria a estrutura `src/test/java` espelhando os pacotes de `src/main/java`, com `maven-surefire-plugin` configurado para `mvn test` (hoje os builds documentados usam `-DskipTests`, então testes nunca rodam).
- **Foco principal**: testes unitários de `LetsRace` (`br.f1mane.servidor.rest.LetsRace`) cobrindo todo endpoint que delega para `ControlePaddockServidor`/`ControleJogosServer`, incluindo:
  - mapeamento de sessão inválida → 401, visitante em endpoint restrito a equipe → 403;
  - mapeamento de `ErroServ` → 500 e `MsgSrv` → 400 feito por `processsaMensagem` (o ponto exato que causou o bug de hoje);
  - delegação correta de parâmetros de path/header/query para o método certo do controller (giro de motor, agressividade, DRS, ERS, traçado, box, criar sessão por nome/Google/visitante, jogar, jogar campeonato, equipe, campeonato).
  - **BREAKING (interno, não de API)**: `LetsRace` ganha um construtor pacote-privado para injeção de dependências (`ControlePaddockServidor`, `CarregadorRecursos`) usado pelos testes; o construtor público sem argumentos usado pelo Jersey continua existindo e se comporta exatamente como hoje.
- Segundo plano (já coberto por infraestrutura, menor prioridade): `Util.processaValorPontosCarreira`, a função de cálculo de pontos de carreira responsável pelo bug do commit `f47e7a74`.
- Documenta no `CLAUDE.md` o comando `mvn test` como parte do fluxo de build.

- **Incremento de escopo**: cobertura de teste unitário das 5 classes de `br.f1mane.servidor.controles` (`ControlePaddockServidor`, `ControleJogosServer`, `ControleClassificacao`, `ControleCampeonatoServidor`, `ControlePersistencia`) — a lógica de negócio real que `LetsRaceTest` hoje só exercita via mock. Diferente de `LetsRace`, essas 5 classes já usam injeção de dependência via construtor (`ControlePersistencia` é passado explicitamente por todas elas) — **nenhum refactor de produção é necessário** para torná-las testáveis, só mockar `ControlePersistencia`/`Session` do Hibernate.
  - Prioridade por densidade de regra de negócio: `ControleClassificacao` (pontuação, validação de distribuição de pontos — sequência direta de `UtilTest`/`Util.processaValorPontosCarreira`) e `ControleCampeonatoServidor` (ciclo de vida de campeonato) primeiro; depois `ControlePaddockServidor` (sessão/autenticação) e `ControleJogosServer` (controles de pilotagem); `ControlePersistencia` por último, com cobertura mais rasa por ser majoritariamente wrapper de CRUD do Hibernate sem ramificação de regra.
  - Getters/setters triviais e delegações de uma linha sem decisão não SHALL receber teste dedicado — mesmo critério já usado em `LetsRaceTest`.

## Capabilities

### New Capabilities
- `unit-testing-infra`: padrão e infraestrutura de testes unitários do projeto — onde os testes vivem, qual framework, como rodam no build.
- `letsrace-endpoint-tests`: cobertura de teste unitário da camada REST `LetsRace` — autenticação/autorização, mapeamento de erro para status HTTP, e delegação correta para os controllers de jogo.
- `controles-business-logic-tests`: cobertura de teste unitário da lógica de negócio real nas 5 classes de `br.f1mane.servidor.controles`, com `ControlePersistencia`/`Session` mockados.

## Impact

- `pom.xml`: novas dependências de teste (JUnit 5, Mockito) e configuração do `maven-surefire-plugin`.
- Novo diretório `src/test/java/...` (sem impacto no jar `-Ph2`/`-Pmysql`, já que testes não entram no fat jar).
- `LetsRace.java`: adiciona um segundo construtor (pacote-privado) para permitir injetar `ControlePaddockServidor`/`CarregadorRecursos` mockados em teste, sem alterar o comportamento do construtor público usado pelo Jersey.
- `br.f1mane.servidor.controles.*`: nenhuma mudança de produção — só testes novos, usando os construtores já existentes.
- `CLAUDE.md`: adiciona `mvn test` à seção de Build & Run.
