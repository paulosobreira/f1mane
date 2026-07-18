## ADDED Requirements

### Requirement: Pacote Java raiz é `br.flmane`
Todo código-fonte Java do projeto (`src/main/java` e `src/test/java`) SHALL usar `br.flmane` como pacote raiz. Nenhuma classe, import, javadoc ou string literal em `src/main/java`/`src/test/java` SHALL referenciar o pacote antigo `br.f1mane`, exceto onde o próprio código precisa citar o nome antigo por motivo de teste de migração de dado legado (não aplicável neste change, já que a estratégia adotada é reescrever os dados existentes em vez de manter compatibilidade retroativa).

#### Scenario: Nenhuma referência residual a br.f1mane no código-fonte
- **WHEN** se busca por `br.f1mane` (case-sensitive) em todo `src/main/java` e `src/test/java`
- **THEN** nenhuma ocorrência é encontrada

#### Scenario: Subprocesso headless do launcher usa o novo FQCN
- **WHEN** `MainLauncher` é executado em modo GUI (sem argumentos) e relança o backend numa JVM filha
- **THEN** o comando montado pelo `ProcessBuilder` referencia `br.flmane.MainLauncher`, não `br.f1mane.MainLauncher`

### Requirement: Artefatos de build/config referenciam `br.flmane`/`flmane`
Os artefatos de build e configuração que citam o pacote ou o nome do projeto por string literal SHALL usar `br.flmane`/`flmane`, não `br.f1mane`/`f1mane`: `pom.xml` (classe principal do jar), `web.xml` (classe de servlet e pacote de scan do Jersey) e `persistence.xml` (lista de entidades JPA).

#### Scenario: Jar executável aponta para a classe principal renomeada
- **WHEN** o fat jar é construído via `mvn clean package`
- **THEN** o manifest do jar declara `br.flmane.MainLauncher` como classe principal

#### Scenario: Servidor web resolve servlet e pacote REST renomeados
- **WHEN** o Tomcat embutido sobe a aplicação a partir de `web.xml`
- **THEN** a `servlet-class` resolve para `br.flmane.servidor.servlet.ServletPaddock` e o scan de pacote do Jersey aponta para `br.flmane.servidor.rest`

#### Scenario: JPA registra as entidades do pacote renomeado
- **WHEN** o Hibernate inicializa a partir de `persistence.xml`
- **THEN** todas as 6 classes de entidade listadas resolvem sob `br.flmane.servidor.entidades.persistencia`

### Requirement: Arquivos de circuito carregam com o pacote renomeado
Todo arquivo de circuito do repositório (`src/main/resources/circuitos/*_mro.xml`, `*_mro_meta.xml`) e o fixture de teste de formato antigo (`src/test/resources/circuitos/fixture_formato_antigo_mro.xml`) SHALL ter seus atributos `class="..."` apontando para `br.flmane`, de forma que `XMLDecoder` resolva as classes corretamente após o rename do pacote Java. Arquivos de backup do editor (`.bak`) ficam fora desta garantia, por não serem carregados por nenhum caminho de código do jogo.

#### Scenario: Circuito de produção carrega após o rename
- **WHEN** `CarregadorRecursos` carrega qualquer circuito de `src/main/resources/circuitos/` via `XMLDecoder`
- **THEN** o objeto `Circuito` e seus objetos de cenário (`ObjetoTransparencia`, `ObjetoEscapada`, `ObjetoLivre`, `TipoObjetoLivre`, etc.) são reconstruídos sem `ClassNotFoundException`

#### Scenario: Fixture de teste de formato antigo continua carregando
- **WHEN** o teste que exercita `fixture_formato_antigo_mro.xml` roda
- **THEN** o arquivo é desserializado com sucesso usando as classes do pacote `br.flmane`
