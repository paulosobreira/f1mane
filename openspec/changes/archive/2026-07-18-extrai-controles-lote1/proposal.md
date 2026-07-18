## Why

`Piloto.java` concentra 4356 linhas misturando dezenas de mecânicas de jogo sem fronteira própria, o que torna o arquivo difícil de navegar mesmo quando a lógica em si é simples e autocontida. Este lote extrai as mecânicas mais isoladas (baixo acoplamento com outros sistemas, boa cobertura de teste) para classes `Controle*` dedicadas, seguindo o padrão já validado pela extração de `ControleAutomacao` (commit `cec67cda`). O objetivo é exclusivamente organizacional: nenhuma mudança de comportamento de jogo é pretendida.

## What Changes

- Extrai `Piloto.processaDerrapagem()` + o helper compartilhado `proximaEscapadaNoCircuito()` e o cluster de escapada da pista (`processaEscapadaDaPista()`, `processaEscapadaAncoradaAoTracado()`, `processaSaidaDaEscapada()`, testes de stress/pneus, notificações, cache por zona/volta) para uma nova classe `ControleEscapada`, mantida junto por compartilharem estado e por já serem documentadas no código como mecânicas "irmãs".
- Extrai `Piloto.processaFreioNaReta()` para uma nova classe `ControleFreio`.
- Extrai `Piloto.processaUsoDRS()` + `Piloto.verificaPodeUsarDRS()` para uma nova classe `ControleDrs`.
- `Piloto` passa a acionar essas mecânicas via `InterfaceJogo` (mesmo padrão de `ControleAutomacao`), preservando a ordem de chamadas hoje existente em `processaNovoIndex()`.
- Amplia a superfície pública de `Piloto` onde necessário para que as novas classes (em outro pacote) leiam/escrevam estado que hoje é privado/package-private — sem introduzir setters além do estritamente necessário.
- Adiciona stubs no-op em `JogoCliente` para os novos métodos de `InterfaceJogo` (cliente multiplayer nunca roda simulação local).
- **Fora de escopo, propositalmente**: `ganho`, `estresse`, `colisão` e o mutator `mudarTracado()` não são extraídos neste lote — são "hubs" de estado compartilhado (múltiplas mecânicas leem/escrevem o mesmo campo); mover a implementação sem redesenhar o fluxo de dados (ex.: ganho como pipeline explícito de modificadores) só deslocaria o acoplamento, sem ganho real de navegabilidade. Fica para uma iniciativa separada. `Turbulência` também fica fora por não ter nenhuma cobertura de teste hoje (`processaTurbulencia` não é referenciado em nenhum arquivo de teste), o que tornaria a extração inverificável.
- Nenhuma mudança de comportamento de jogo é pretendida: os requisitos e cenários das specs afetadas permanecem os mesmos, apenas a classe/método que hospeda a lógica muda.

## Capabilities

### New Capabilities
(nenhuma — este lote reorganiza implementação de mecânicas já especificadas, não introduz mecânica nova)

### Modified Capabilities
- `derrapagem-piloto`: os requisitos hoje descrevem a derrapagem como responsabilidade de `Piloto` (`Piloto.processaEscapadaDaPista()` como método hospedeiro, chamadas a `controleJogo.travouRodas(this)`); passam a referenciar `ControleEscapada` como classe hospedeira, mantendo cenários e comportamento idênticos.
- `escapada-ia-corrida`: requisitos referenciam `Piloto.processaEscapadaAncoradaAoTracado()` e métodos correlatos por nome; passam a referenciar os métodos equivalentes em `ControleEscapada`. O ponto de integração com `Piloto.mudarTracado()` (fora de escopo) e o campo `impedidoDeMudarTracadoPorEscapada` continuam descritos, mas com a leitura/escrita explicitada como cruzando a fronteira entre `Piloto` e `ControleEscapada`.
- `zona-frenagem-deteccao`: o requisito "Zona de frenagem substitui a janela fixa de distância em processaFreioNaReta" referencia `Piloto.processaFreioNaReta()` como método hospedeiro; passa a referenciar `ControleFreio.processaFreioNaReta()`, mantendo o comportamento (incluindo o disparo de `controleJogo.travouRodas(this)`) idêntico.

(DRS não tem spec dedicada hoje referenciando `processaUsoDRS`/`verificaPodeUsarDRS` por nome, então `ControleDrs` não exige delta de spec.)

## Impact

- **Código afetado**: `src/main/java/br/f1mane/entidades/Piloto.java` (remoção das ~520 linhas extraídas + possível ampliação de getters/setters públicos), novas classes em `src/main/java/br/f1mane/controles/` (`ControleEscapada`, `ControleFreio`, `ControleDrs`), `InterfaceJogo` (novos métodos de delegação), `ControleJogoLocal`/`ControleCorrida` (instanciação e wiring, seguindo o padrão de `ControleAutomacao`), `JogoCliente` (stubs no-op).
- **Testes afetados**: `PilotoDerrapagemTest`, `PilotoEscapadaAncoradaTracadoTest`, `PilotoGanhoTracadoDeFugaTest`, `CircuitoEscapadaTracadoTest`, `PilotoFreioNaRetaZonaFrenagemTest`, `PilotoProcessaFreioNaRetaMolhadoTest`, `PilotoDrsDesligadoNoBoxTest` — todos mockam `InterfaceJogo`/`ControleJogoLocal` e podem precisar de um helper de rewiring (nos moldes do usado para `ControleAutomacao`) para religar o mock a uma instância real da nova classe.
- **Sem impacto em**: persistência, API REST (`LetsRace`), circuitos/XML, i18n, build/Maven.
- **Sem mudança de comportamento observável** em jogo solo, servidor ou cliente multiplayer.
