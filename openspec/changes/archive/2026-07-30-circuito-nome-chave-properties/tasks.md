## 1. Circuito: nome deixa de ser bean persistido

- [x] 1.1 Em `Circuito.java`, renomear o setter `setNome(String)` para `definirNomePorConvencao(String)` (fora do padrão JavaBean, igual `definirBackGroundPorConvencao`), mantendo `getNome()` como está
- [x] 1.2 Remover a linha `copia.setNome(nome)` de `Circuito.copiaParaArquivoMetadados()` (`Circuito.java:539`)
- [x] 1.3 Atualizado `MainPanelEditor.java:4316` e `ControleRecursos.java:166` (bloco redundante removido — `carregarCircuito()` já popula `nome` sozinho) e `CircuitoMetadadosArquivoTest.java:38` (pendente de ajuste na seção 5)

## 2. CarregadorRecursos: resolver e escrever nome via circuitos.properties

- [x] 2.1 Adicionado `CarregadorRecursos.nomeExibicaoCircuito(String nmCircuito)` (lê via classpath, mesmo padrão de `circuitoAtivo`) com fallback `nomeExibicaoPorConvencao` quando não há linha em `circuitos.properties` (decisão da Open Question do design.md)
- [x] 2.2 `carregarCircuito()` e `carregarMetadadosCircuito()` populam `circuito.definirNomePorConvencao(nomeExibicaoCircuito(nmCircuito))` logo após popular `ativo`
- [x] 2.3 Generalizada a escrita linha-a-linha: `atualizarCircuitosProperties(File, String nmCircuitoXml, String nomeExibicao, boolean ativo)` reescreve a linha inteira numa única passada; `atualizarAtivoEmCircuitosProperties(File,...)` agora preserva o nome atual da linha via `lerNomeDaFonte`
- [x] 2.4 Adicionado `atualizarNomeEmCircuitosProperties(String nmCircuitoXml, String nomeExibicao)` (wrapper público, preserva `ativo` da linha via `lerAtivoDaFonte`)
- [x] 2.5 Adicionado `lerNomeDaFonte(String nmCircuitoXml)` (lê direto do arquivo-fonte, mesmo padrão de `lerAtivoDaFonte`, com fallback por convenção)

## 3. Editor: salvar nome grava e recarrega circuitos.properties

- [x] 3.1 Em `gravarCircuitoEmDisco()`, após `atualizarAtivoEmCircuitosProperties(...)`, chamado `atualizarNomeEmCircuitosProperties(file.getName(), circuito.getNome())`
- [x] 3.2 Em seguida, `lerNomeDaFonte(file.getName())` atualiza `nomePistaText.setText(...)` e `circuito.definirNomePorConvencao(...)` com o valor relido
- [x] 3.3 `gravarCircuitoEmDisco()` é compartilhado por `salvarPista()` e `autoSalvarComBackup()` — mesmo caminho cobre os dois, sem duplicação

## 4. Remover Util.substVogais por completo

- [x] 4.1 Removido campo `Util.substVogais` e método `Util.substVogais(String)`
- [x] 4.2 Call sites atualizados para nome puro: `CarregadorRecursos.java`, `ControleRecursos.java` (parâmetro `substVogais` removido de `nomeCircuitoParaArquivoCircuito`, único caller ajustado em `ControleCampeonatoServidor.java`), `PainelMenuLocal.java` (4 pontos), `ControleCampeonatoServidor.java:213-214`, `ControleJogosServer.java:329-330`, `PainelCircuito.java:4278`; imports `Util` não usados removidos de `ControleRecursos.java` e `MainFrame.java`
- [x] 4.3 Removido o bloco `if ("real".equals(args[i]))` em `MainFrame.java`

## 5. Specs e validação

- [x] 5.1 Ajustados `CircuitoMetadadosArquivoTest.java` (setter renomeado, assertion de `nome` no XML invertida para `assertFalse`) e `CircuitoCoresBoxZebraTest.java` (sanity-check trocado de `nome` — não mais round-tripado — para `corFundo`, que é o campo realmente sob teste)
- [x] 5.2 `mvn test` — 818 testes, 0 falhas
- [x] 5.3 `mvn clean package -Ph2 -DskipTests` — jar atualizado
- [x] 5.4 Substituído por testes automatizados diretos das funções que o editor chama (`atualizarNomeEmCircuitosProperties`, `lerNomeDaFonte`, preservando `ativo`) em `CircuitoMetadadosArquivoTest` — 4 casos novos, todos verdes. Clique real no editor Swing (`MainPanelEditor`) NÃO foi feito nesta sessão: sem ferramenta de automação de GUI disponível (sem xdotool/equivalente para Swing). Recomenda-se validação manual pelo usuário: abrir um circuito, alterar `nomePistaText`, salvar, e conferir `circuitos.properties`.
