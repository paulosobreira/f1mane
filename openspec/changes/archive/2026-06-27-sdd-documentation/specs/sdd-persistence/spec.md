## ADDED Requirements

### Requirement: Perfis Maven documentados
O SDD SHALL descrever os dois perfis de banco de dados e quando cada um é usado.

#### Scenario: Perfis H2 e MySQL descritos
- **WHEN** o leitor consulta a camada de persistência
- **THEN** o SDD descreve: perfil `h2` (default) usa H2 em `~/flmane-data/flmane` para desenvolvimento local; perfil `mysql` usa MySQL em `db:3306/flmane` para Docker/produção; o filtro de build injeta os valores JDBC em `META-INF/persistence.xml` no momento do `mvn package`

### Requirement: Hierarquia de entidades JPA documentada
O SDD SHALL descrever `F1ManeDados` como base e as entidades que a estendem.

#### Scenario: F1ManeDados descrita
- **WHEN** o leitor consulta as entidades de persistência
- **THEN** o SDD descreve que `F1ManeDados` é `@MappedSuperclass` com `@Id @GeneratedValue id: Long`, `dataCriacao: Date` e `loginCriador: String`; e lista as 6 entidades concretas: `JogadorDadosSrv (@Table("usuario"))`, `CampeonatoSrv (@Table("f1_campeonatosrv"))`, `CorridaCampeonatoSrv`, `CorridasDadosSrv`, `DadosCorridaCampeonatoSrv`, `CarreiraDadosSrv`

#### Scenario: Relacionamentos JPA descritos
- **WHEN** o leitor consulta as relações entre entidades
- **THEN** o SDD descreve que `JogadorDadosSrv` tem `@OneToMany(cascade=ALL)` para `CorridasDadosSrv`, e que `CampeonatoSrv` tem `@OneToMany(cascade=ALL)` para `CorridaCampeonatoSrv`
