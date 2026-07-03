## MODIFIED Requirements

### Requirement: Bloqueio de avanço por colisão dianteira-centro
Quando `colisaoDiantera` do carro de trás intersecta `centroColisao` do carro à frente na mesma linha (mesmo `tracado`, ou o carro à frente ainda cruzando essa linha a partir de outro `tracado`), o sistema SHALL limitar o `ganho` do carro de trás ao valor `ganho` do carro à frente, e SHALL adicionalmente limitar o avanço posicional (`noIndex`) do carro de trás no ciclo para nunca entrar ou atravessar a área ocupada pelo carro à frente, independentemente do quão maior seja a aproximação em um único ciclo.

#### Scenario: Carro de trás não ultrapassa carro da frente por colisão de centro
- **WHEN** `processaPenalidadeColisao` é chamado e `colisaoDiantera` está ativa com interseção no `centroColisao` do piloto à frente
- **THEN** `ganho` do piloto de trás é definido como `Math.min(ganho, pilotoFrente.getGanho())`
- **THEN** o `noIndex` resultante do piloto de trás no final do ciclo SHALL ser menor ou igual ao `noIndex` do piloto à frente

#### Scenario: Colisão em traçados diferentes não aplica bloqueio
- **WHEN** piloto de trás e piloto à frente estão em traçados diferentes e nenhum dos dois está cruzando a linha do outro (`indiceTracado == 0` para ambos ou `tracadoAntigo` não corresponde)
- **THEN** `processaPenalidadeColisao` não aplica cap de ganho mesmo que hitboxes se intersectem

#### Scenario: Carro da frente ainda cruzando a linha atual também bloqueia
- **WHEN** o piloto à frente mudou de traçado (`indiceTracado > 0`) e seu `tracadoAntigo` é igual ao `tracado` do piloto de trás
- **THEN** `processaPenalidadeColisao` trata como mesma linha e aplica o cap de `ganho` normalmente

#### Scenario: Avanço de um ciclo não atravessa carro parado à frente
- **WHEN** o avanço calculado no ciclo (`roundGanho`) é maior que a distância real até o carro à frente na mesma linha, incluindo quando o carro à frente está imóvel (acidente, largada, bandeirada)
- **THEN** o avanço aplicado ao `noIndex` do carro de trás SHALL ser reduzido para parar no limite de um comprimento de carro antes do carro à frente, mesmo que o `ganho` bruto calculado fosse suficiente para ultrapassá-lo

### Requirement: Desaceleração por colisão dianteira-traseira
Quando `colisaoDiantera` do carro de trás intersecta apenas `trazeiraColisao` do carro à frente (sem interseção com centro) na mesma linha, o sistema SHALL aplicar redução de `ganho` em 30% além de limitar ao ganho do carro da frente.

#### Scenario: Toque traseiro desacelera carro de trás consideravelmente
- **WHEN** `processaPenalidadeColisao` detecta colisão apenas com `trazeiraColisao` (não com `centroColisao`) na mesma linha
- **THEN** `ganho` do piloto de trás é `Math.min(ganho * 0.7, pilotoFrente.getGanho())`

### Requirement: Colisão centro-traseira impede avanço
Quando `colisaoCentro` do carro de trás intersecta `trazeiraColisao` do carro à frente na mesma linha, o carro de trás SHALL manter seu `ganho` limitado ao `ganho` do carro à frente (não ultrapassa) e o avanço posicional SHALL respeitar o mesmo limite de não atravessamento.

#### Scenario: Interseção centro com traseira bloqueia avanço
- **WHEN** `colisaoCentro` é true e `colisaoDiantera` é false, na mesma linha
- **THEN** `ganho` do piloto de trás é `Math.min(ganho, pilotoFrente.getGanho())`
