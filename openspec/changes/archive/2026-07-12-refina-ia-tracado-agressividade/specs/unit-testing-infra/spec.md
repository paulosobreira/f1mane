## ADDED Requirements

### Requirement: Fixtures de Piloto usam potência/freios realistas por padrão
Os métodos auxiliares `criarPiloto()` de `PilotoEscapadaAncoradaTracadoTest` e `PilotoDerrapagemTest` (e qualquer teste futuro de `Piloto` que monte um `Carro` do zero) SHALL configurar `potencia` e `freios` do carro com um valor alto por padrão (ex.: 1000), em vez de deixá-los no valor zero implícito de `new Carro()`. Testes que precisam deliberadamente exercitar o caminho de falha no teste de habilidade SHALL zerar `potencia`/`freios` explicitamente no próprio teste, com um comentário indicando essa intenção.

#### Scenario: Fixture padrão permite sucesso genuíno no teste de habilidade
- **WHEN** um teste usa `criarPiloto()` sem sobrescrever `potencia`/`freios`, define `habilidade` alta, e mocka `getRandom().nextDouble()` para um valor baixo
- **THEN** `Piloto.testeHabilidadePilotoCarro()`/`testeHabilidadePilotoFreios()` retornam `true` (sucesso), porque nem `potencia` nem `freios` zerados bloqueiam o teste por conta própria

#### Scenario: Teste que quer exercitar falha zera potência/freios explicitamente
- **WHEN** um teste precisa que o piloto falhe no teste de habilidade independente do sorteio, e chama `carro.setPotencia(0)` (ou `setFreios(0)`) explicitamente com um comentário indicando a intenção
- **THEN** o teste de habilidade correspondente falha sempre, de forma rastreável à configuração explícita do teste, não a um default silencioso do fixture
