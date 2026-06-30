## ADDED Requirements

### Requirement: O projeto SHALL ter infraestrutura de testes unitários executável via Maven
O `pom.xml` SHALL declarar JUnit 5 e Mockito como dependências de escopo `test`, e o `maven-surefire-plugin` SHALL estar configurado para descobrir e executar testes JUnit 5 ao rodar `mvn test`. A adição dessas dependências NÃO SHALL alterar o conteúdo do jar de produção gerado por `mvn clean package -Ph2 -DskipTests` ou `-Pmysql -DskipTests`.

#### Scenario: mvn test executa a suíte
- **WHEN** `mvn test` é executado na raiz do projeto
- **THEN** todas as classes em `src/test/java` cujo nome termina em `Test` são executadas e o build reporta sucesso se todas passarem

#### Scenario: Build de produção não é afetado
- **WHEN** `mvn clean package -Ph2 -DskipTests` é executado
- **THEN** o `target/flmane.jar` gerado não contém nenhuma classe de `src/test/java` nem as dependências de teste (JUnit, Mockito) no classpath empacotado

### Requirement: A estrutura de testes SHALL espelhar os pacotes de produção
Cada classe de teste SHALL viver em `src/test/java/<mesmo-pacote-da-classe-testada>`, seguindo a convenção padrão do Maven (`testSourceDirectory` default), sem necessidade de configuração adicional no `pom.xml` para localizar os testes.

#### Scenario: Teste de Util fica no pacote br.nnpe
- **WHEN** o arquivo `src/test/java/br/nnpe/UtilTest.java` é lido
- **THEN** ele declara `package br.nnpe;`, o mesmo pacote de `src/main/java/br/nnpe/Util.java`

### Requirement: O cálculo de pontos de carreira SHALL ter cobertura de teste unitário
`Util.processaValorPontosCarreira` SHALL ter testes unitários cobrindo a tabela de custo por faixa de nível (documentada em `openspec/specs/distribuicao-pontos-carreira/spec.md`), incluindo o caso de regressão do bug de pontos infinitos no nível 999 (commit `f47e7a74`).

#### Scenario: Upgrade 998→999 custa 50 pontos
- **WHEN** `processaValorPontosCarreira(998, 999, numero)` é chamado com um `Numero` cujo valor inicial é conhecido
- **THEN** o valor do `Numero` após a chamada é igual ao valor inicial menos 50

#### Scenario: Downgrade 999→998 devolve 50 pontos
- **WHEN** `processaValorPontosCarreira(999, 998, numero)` é chamado com um `Numero` cujo valor inicial é conhecido
- **THEN** o valor do `Numero` após a chamada é igual ao valor inicial mais 50

#### Scenario: Ciclo downgrade seguido de upgrade no nível 999 não altera o saldo
- **WHEN** `processaValorPontosCarreira(999, 998, numero)` é chamado e, em seguida, `processaValorPontosCarreira(998, 999, numero)` é chamado sobre o mesmo `Numero`
- **THEN** o valor final do `Numero` é igual ao valor antes do primeiro chamado

#### Scenario: Custo segue a tabela de faixas abaixo de 999
- **WHEN** `processaValorPontosCarreira` é chamado com pares de nível-atual/nível-alvo cobrindo cada faixa (≤599, 600-699, 700-799, 800-899, 900-999) tanto em upgrade quanto em downgrade
- **THEN** o débito ou crédito aplicado ao `Numero` corresponde exatamente ao custo por ponto definido na tabela da faixa do nível-alvo

### Requirement: O comando de teste SHALL estar documentado no CLAUDE.md
A seção "Build & Run" do `CLAUDE.md` SHALL incluir o comando `mvn test` como parte do fluxo de verificação local.

#### Scenario: CLAUDE.md menciona mvn test
- **WHEN** `CLAUDE.md` é lido
- **THEN** a seção "Build & Run" contém o comando `mvn test`
