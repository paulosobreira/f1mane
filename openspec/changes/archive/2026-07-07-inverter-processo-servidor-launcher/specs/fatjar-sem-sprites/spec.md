## ADDED Requirements

### Requirement: Fat jar não empacota as folhas de sprites
O build do fat jar (`maven-shade-plugin`) SHALL excluir `sprites/*` do artefato final, no mesmo padrão do exclude existente de `circuitos/*_mro.jpg`, com comentário no `pom.xml` explicando que a renderização usa o fallback do modelo universal v2.

#### Scenario: Jar final sem sprites
- **WHEN** `mvn clean package` gera `target/flmane.jar`
- **THEN** o jar não contém nenhuma entrada `sprites/*.png`

### Requirement: Renderização degrada para o modelo universal v2 sem sprites
Com as folhas de sprites ausentes do classpath, `SpriteSheet.isDisponivel(temporada)` SHALL retornar `false` e `CarregadorRecursos` SHALL gerar as imagens de carro (lado/cima) e capacete pelo modelo universal v2 (`pintarModeloV2` com os templates `png/*-v2.png`), sem lançar erro nem quebrar o carregamento da temporada.

#### Scenario: Temporada carrega sem sprites no classpath
- **WHEN** uma temporada é carregada a partir do fat jar final (sem `sprites/tANO.png`)
- **THEN** as imagens de carros e capacetes são geradas via modelo v2 com as cores de `carros.properties`, sem exceção

#### Scenario: Editor rodando do repositório mantém sprites
- **WHEN** o editor/jogo roda do repositório (`target/classes`, onde `sprites/` existe)
- **THEN** `SpriteSheet.isDisponivel` continua retornando `true` e as folhas são usadas normalmente
