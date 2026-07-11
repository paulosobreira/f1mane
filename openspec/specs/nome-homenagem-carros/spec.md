# nome-homenagem-carros Specification

## Purpose
TBD - created by archiving change modo-homenagem. Update Purpose after archive.
## Requirements
### Requirement: `carros.properties` tem uma coluna `nomeHomenagem`

Cada linha de `carros.properties` (formato `Chave=potencia,cor1-r,cor1-g,cor1-b,imagem[;imagem2;...],cor2-r,cor2-g,cor2-b,aero,freios`) SHALL ganhar um 11º campo posicional opcional, `nomeHomenagem`, lido apenas quando presente (`values.length > 10`, seguindo o mesmo padrão já usado pra `aero`/`freios`). A ausência desse campo SHALL NOT quebrar o carregamento do carro.

#### Scenario: Carro com nomeHomenagem presente
- **WHEN** uma linha de `carros.properties` tem 11 campos, com o 11º sendo o nome-homenagem
- **THEN** o carro carregado expõe esse valor via um getter dedicado (ex.: `getNomeHomenagem()`), sem afetar a leitura dos 10 campos existentes

#### Scenario: Carro sem nomeHomenagem (retrocompatibilidade)
- **WHEN** uma linha de `carros.properties` tem só os 10 campos originais
- **THEN** o carro é carregado normalmente, com o nome-homenagem tratado como ausente/vazio, sem lançar exceção

### Requirement: Todas as temporadas existentes têm `nomeHomenagem` preenchido com o nome canônico derivado

Para cada `carros.properties` de cada temporada em `src/main/resources/properties/t*/`, o campo `nomeHomenagem` SHALL ser preenchido com o nome canônico da equipe, derivado do primeiro nome de arquivo da coluna `imagem` (antes do primeiro `;`), removendo a extensão, removendo prefixos de convenção de nomenclatura (ex.: `tn_`, ano de 4 dígitos, `voi-`) e removendo sufixos de modelo/versão do carro, normalizado em minúsculas.

#### Scenario: Nome de imagem com prefixo de convenção antiga
- **WHEN** a coluna `imagem` de um carro é `tn_1972voi-lotus72d.png`
- **THEN** `nomeHomenagem` desse carro é `lotus`

#### Scenario: Nome de imagem no formato Equipe-Modelo
- **WHEN** a coluna `imagem` de um carro é `Renault-R27.png`
- **THEN** `nomeHomenagem` desse carro é `renault`

#### Scenario: Todas as temporadas cobertas
- **WHEN** qualquer `carros.properties` sob `src/main/resources/properties/t*/` é inspecionado após esta mudança
- **THEN** toda linha de carro tem um `nomeHomenagem` não vazio preenchido

### Requirement: Nome canônico da imagem é a chave de identidade do carro entre temporadas

O nome canônico derivado do nome de arquivo da coluna `imagem` (ver requisito anterior) SHALL servir como identificador estável de uma mesma equipe/carro através de várias temporadas, independente da versão/modelo específico daquela temporada (ex.: `McLaren-MP4/1` numa temporada e `McLaren-MP4/4` noutra têm nomes de imagem diferentes por versão, mas ambos canonicalizam pra `mclaren`). Esse identificador SHALL ser o que a edição de `nomeHomenagem` usa pra decidir quais outras linhas, em quais outras temporadas, recebem a mesma alteração quando o usuário optar por propagar a mudança (ver `dev-editor-tools`).

#### Scenario: Mesma equipe em versões diferentes ao longo das temporadas
- **WHEN** dois carros em temporadas diferentes têm nomes de imagem que canonicalizam pro mesmo nome (ex.: `mclaren`), mesmo com sufixos de versão/modelo diferentes
- **THEN** os dois são considerados a mesma identidade canônica pra fins de propagação de `nomeHomenagem`

