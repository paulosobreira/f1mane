## MODIFIED Requirements

### Requirement: Nome de piloto e carro é resolvido uma vez no carregamento, conforme `MODO_HOMENAGEM`

Ao carregar `pilotos.properties`/`carros.properties` de uma temporada, `Piloto.nome` e `Carro.nome` SHALL ser definidos como `nomeHomenagem` quando `Global.MODO_HOMENAGEM` for `true`, ou como o nome real (a chave original da properties) quando for `false`. `Carro` SHALL ganhar um campo `nomeOriginal` (análogo ao já existente em `Piloto`) pra guardar o nome real independente do modo. Quando `nomeHomenagem` estiver ausente/vazio pra um carro ou piloto (temporada não migrada, ou removido manualmente), o nome real SHALL ser usado como fallback, mesmo com `MODO_HOMENAGEM` ativo.

#### Scenario: Modo homenagem ativo usa o nome-homenagem
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e um piloto/carro com `nomeHomenagem` preenchido é carregado
- **THEN** `getNome()` desse piloto/carro retorna o valor de `nomeHomenagem`

#### Scenario: Modo homenagem ativo sem nomeHomenagem cai no nome real
- **WHEN** `Global.MODO_HOMENAGEM` é `true` e um piloto/carro sem `nomeHomenagem` preenchido é carregado
- **THEN** `getNome()` desse piloto/carro retorna o nome real, sem lançar exceção

#### Scenario: Nomes de circuito não têm mais nenhuma distorção de vogais
- **WHEN** um nome de circuito é exibido (em qualquer estado de `MODO_HOMENAGEM`)
- **THEN** o nome exibido é o valor puro resolvido de `circuitos.properties`, sem passar por `Util.substVogais` — esse método não existe mais no código-fonte (ver capability `circuito-nome-exibicao`)
