## ADDED Requirements

### Requirement: `pilotos.properties` tem uma coluna `nomeHomenagem` após `habilidade`

Cada linha de `pilotos.properties` (formato `Chave=Carro,Habilidade`) SHALL ganhar um 3º campo posicional opcional, `nomeHomenagem`, posicionado logo após `Habilidade`, lido apenas quando presente. A ausência desse campo SHALL NOT quebrar o carregamento do piloto.

#### Scenario: Piloto com nomeHomenagem presente
- **WHEN** uma linha de `pilotos.properties` tem 3 campos, com o 3º sendo o nome-homenagem
- **THEN** o piloto carregado expõe esse valor via um getter dedicado (ex.: `getNomeHomenagem()`), sem afetar a leitura de `Carro`/`Habilidade`

#### Scenario: Piloto sem nomeHomenagem (retrocompatibilidade)
- **WHEN** uma linha de `pilotos.properties` tem só os 2 campos originais (`Carro,Habilidade`)
- **THEN** o piloto é carregado normalmente, com o nome-homenagem tratado como ausente/vazio, sem lançar exceção

### Requirement: Todos os pilotos existentes têm `nomeHomenagem` preenchido com um pseudônimo de mesma nacionalidade e inicial

Para cada piloto em cada `pilotos.properties` de cada temporada, o campo `nomeHomenagem` SHALL ser preenchido com um nome fictício que preserva a inicial do primeiro nome do piloto real (como formatada na chave da properties) e usa um sobrenome característico da mesma nacionalidade do piloto real.

#### Scenario: Piloto brasileiro
- **WHEN** um piloto real de nacionalidade brasileira tem a chave `A.Senna`
- **THEN** `nomeHomenagem` desse piloto é um nome no formato `A.<Sobrenome>`, com `<Sobrenome>` sendo um sobrenome comum brasileiro (ex.: `A.Silva`)

#### Scenario: Piloto espanhol
- **WHEN** um piloto real de nacionalidade espanhola tem a chave `F.Alonso`
- **THEN** `nomeHomenagem` desse piloto é um nome no formato `F.<Sobrenome>`, com `<Sobrenome>` sendo um sobrenome comum espanhol, diferente de `Alonso`

#### Scenario: Todos os pilotos de todas as temporadas cobertos
- **WHEN** qualquer `pilotos.properties` sob `src/main/resources/properties/t*/` é inspecionado após esta mudança
- **THEN** toda linha de piloto tem um `nomeHomenagem` não vazio preenchido, com a mesma inicial de primeiro nome do piloto real

### Requirement: Chave real do piloto é a identidade entre temporadas

A chave real do piloto (o nome usado como chave em `pilotos.properties`, ex.: `A.Senna`) SHALL servir como identificador estável de um mesmo piloto através de várias temporadas em que ele aparece. Esse identificador SHALL ser o que a edição de `nomeHomenagem` usa pra decidir quais outras linhas, em quais outras temporadas, recebem a mesma alteração quando o usuário optar por propagar a mudança (ver `dev-editor-tools`).

#### Scenario: Mesmo piloto em várias temporadas
- **WHEN** um piloto com a mesma chave real aparece em `pilotos.properties` de temporadas diferentes
- **THEN** todas essas linhas são consideradas a mesma identidade pra fins de propagação de `nomeHomenagem`
