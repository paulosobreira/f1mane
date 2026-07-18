## Context

`Piloto.java` (4356 linhas) roda seu pipeline de tick em `processaNovoIndex()`, uma sequência fixa de ~25 chamadas onde a ordem importa (há uma dependência documentada: derrapagem/escapada precisam rodar depois de estresse, porque leem o valor fresco daquele tick). Este lote extrai 3 dessas chamadas — derrapagem, escapada da pista e freio na reta — mais DRS (chamado fora dessa sequência principal, em `processaUsoDRS`/`processaUsoERS`), para classes `Controle*` dedicadas em `br.f1mane.controles`.

Já existe um precedente direto: `ControleAutomacao` (commit `cec67cda`) extraiu a decisão de piloto automático inteira. A extração seguiu um padrão consistente:
- Classe nova recebe `ControleJogoLocal`/`ControleCorrida` no construtor, é instanciada e exposta por `ControleCorrida`.
- `Piloto` continua acionando a lógica via `InterfaceJogo` (não referência direta à classe concreta), preservando a posição da chamada dentro de `processaNovoIndex()` como um índice legível do pipeline.
- As decisões executam através dos mesmos métodos públicos de comando que o jogador humano usa (`setModoPilotagem`, `setAtivarDRS`, `mudarTracado` etc.) — sem via de escrita paralela.
- `InterfaceJogo` tem 2 implementações (`ControleJogoLocal`, `JogoCliente`); `JogoCliente` (multiplayer, nunca roda simulação local) precisa de stubs no-op para qualquer método novo.
- Estado que era recalculado a cada tick (não persistente) foi apagado de `Piloto` e virou variável local/record na classe nova. Estado que persiste entre ticks e é exclusivo da mecânica extraída virou `Map<Piloto, X>` dentro da classe nova (`manualTemporarioPorPiloto`, `ultimaExecucaoPorPiloto`).

Este design examina, campo a campo, onde cada mecânica deste lote se encaixa nesse padrão — porque nem todo campo tem o mesmo perfil de acoplamento.

## Goals / Non-Goals

**Goals:**
- Extrair `processaDerrapagem()` + `processaEscapadaDaPista()` (e todo o cluster que ela orquestra) para `ControleEscapada`.
- Extrair `processaFreioNaReta()` para `ControleFreio`.
- Extrair `processaUsoDRS()` + `verificaPodeUsarDRS()` para `ControleDrs`.
- Preservar comportamento observável exatamente como está hoje — nenhuma mudança de requisito, só de classe/método hospedeiro.
- Minimizar o crescimento da superfície pública de `Piloto`: só ganham getter/setter novo os campos que uma mecânica **fora deste lote** (estresse, `mudarTracado`) também precisa ler ou escrever. Estado que é uso exclusivo da mecânica extraída migra para dentro da classe nova (padrão `Map<Piloto, X>`, como em `ControleAutomacao`), não para um getter/setter em `Piloto`.

**Non-Goals:**
- Redesenhar o fluxo de `ganho` como pipeline de modificadores explícito — `ganho` continua sendo lido/escrito via `getGanho()`/`setGanho()` (já públicos), exatamente como as outras mecânicas já fazem hoje.
- Tocar em `ganho`, `estresse`, `colisão` ou no mutator `mudarTracado()` — permanecem em `Piloto`/`ControleCorrida` sem alteração.
- Adicionar cobertura de teste nova além da necessária para religar os mocks existentes (não é objetivo deste lote melhorar cobertura, só preservá-la).

## Decisions

### D1 — Agrupamento em 3 classes, não 4 nem 1

`ControleEscapada` cobre **derrapagem + escapada da pista** juntas: o código já as documenta como mecânicas "irmãs" (mesmo javadoc cruzado) e elas compartilham o helper `proximaEscapadaNoCircuito()`. Separá-las forçaria esse helper a virar público entre duas classes, ou a ser duplicado — pior em ambos os casos do que mantê-las juntas.

`ControleFreio` e `ControleDrs` ficam separadas entre si: não compartilham estado nem helper, e são conceitualmente independentes (frenagem vs. aerodinâmica).

**Alternativa considerada**: uma única classe `ControleMecanicasPista` para as 4 mecânicas. Rejeitada — perderia a granularidade que faz a extração valer a pena para navegabilidade (o objetivo é arquivos menores e nomeados por responsabilidade, não só sair de `Piloto.java`).

### D2 — Onde cada campo mora depois da extração

Regra aplicada: um campo só ganha getter/setter público novo em `Piloto` se alguma mecânica que **permanece** em `Piloto` (estresse, `mudarTracado()`) também precisa lê-lo ou escrevê-lo. Campos de uso exclusivo da mecânica extraída migram para dentro da classe nova, como `Map<Piloto, EstadoX>` privado — mesmo padrão de `ControleAutomacao.manualTemporarioPorPiloto`.

Levantamento concreto (verificado no código, não presumido):

| Campo | Hoje | Também usado por (fora do escopo) | Decisão |
|---|---|---|---|
| `impedidoDeMudarTracadoPorEscapada` | privado, sem acessor | `Piloto.mudarTracado()` (linhas 3514, 3670) | **Fica em Piloto** — novo `isImpedidoDeMudarTracadoPorEscapada()`/`setImpedidoDeMudarTracadoPorEscapada()` |
| `modoPilotagemAntesDaFuga`, `giroAntesDaFuga`, `estavaNoTracadoDeFuga` | privados, sem acessor | ninguém além de `processaEscapadaDaPista()` | **Migra para `ControleEscapada`** (`Map<Piloto, EstadoEscapada>`) |
| `resultadoTesteEscapadaPorZonaNestaVolta`, `numeroVoltaDoCacheDeTesteEscapada` | privados, sem acessor | ninguém além do cluster de escapada | **Migra para `ControleEscapada`** |
| `freiandoReta` | já público (`isFreiandoReta()`/`setFreiandoReta()`) | `processaGanhoDanificado()` (linha 1227) e outro ponto (linha 3745), ambos fora de escopo | **Fica em Piloto** — acessores já existem, nenhuma mudança |
| `freioNaRetaMalSucedidoNesteTick` | privado, sem acessor público | `processaStressFreioNaRetaMalSucedido()` (linha 2488-2492), cluster de estresse, fora de escopo | **Fica em Piloto** — novo `getFreioNaRetaMalSucedidoNesteTick()`/`setFreioNaRetaMalSucedidoNesteTick()` |
| `retardaFreiandoReta`, `freioNaRetaAvaliadoNesteEvento` | privados, sem acessor | ninguém além de `processaFreioNaReta()` | **Migra para `ControleFreio`** |
| `ganho` | já público (`getGanho()`/`setGanho()`) | várias mecânicas fora de escopo | **Fica em Piloto** — acessores já existem |
| `ativarDRS`, `podeUsarDRS` | já públicos (`isAtivarDRS()`/`setAtivarDRS()`, `isPodeUsarDRS()`/`setPodeUsarDRS()`) | usados amplamente (turbulência, HUD) | **Ficam em Piloto** — acessores já existem, `ControleDrs` não precisa de nenhum acessor novo |
| `carroPilotoDaFrenteRetardatario`, `calculaDiffParaProximoRetardatario`, `calculaDiffParaProximoRetardatarioMesmoTracado` | já públicos (getters existentes, linhas 4066/4076/3999) | usados por turbulência e outras mecânicas fora de escopo | **Ficam em Piloto** — leitura apenas, acessores já existem |

Resultado líquido: **apenas 2 pares de getter/setter novos em `Piloto`** (`impedidoDeMudarTracadoPorEscapada`, `freioNaRetaMalSucedidoNesteTick`) — bem abaixo dos 7 membros novos que a extração de `ControleAutomacao` precisou, porque a maior parte do estado que as 4 mecânicas deste lote leem já tinha acessor público (usado por outras integrações) antes mesmo deste lote existir.

**Alternativa considerada**: mover `impedidoDeMudarTracadoPorEscapada` também para `ControleEscapada` e fazer `Piloto.mudarTracado()` perguntar a `ControleEscapada` via `InterfaceJogo` (ex.: `controleJogo.isImpedidoDeMudarTracadoPorEscapada(this)`). Rejeitada por ora — `mudarTracado()` é chamado com alta frequência (é o primitivo mais compartilhado do jogo) e está fora de escopo deste lote; introduzir uma chamada indireta ali é uma mudança de acoplamento maior do que vale a pena para este lote. Fica registrada como candidata para quando/se `mudarTracado()` for revisitado.

### D3 — Wiring: replica `ControleAutomacao`, com dois ajustes encontrados na implementação

`ControleEscapada`, `ControleFreio`, `ControleDrs`: construtor **`(InterfaceJogo)`**, não `(ControleJogoLocal, ControleCorrida)` como inicialmente planejado. Dois ajustes descobertos ao implementar `ControleDrs` (primeira das três, ver tasks.md grupo 1):

1. **`ControleCorrida` não é necessário.** Nenhum método de DRS/freio/escapada/derrapagem chama algo em `ControleCorrida` que não esteja também em `InterfaceJogo` — diferente de `ControleAutomacao`, essas mecânicas não têm nenhum branch `instanceof JogoServidor`. Guardar uma referência não utilizada violaria a diretriz do projeto de não introduzir campos além do que a tarefa exige.
2. **`InterfaceJogo` (a interface) basta, não precisa ser `ControleJogoLocal` (a classe concreta).** Todos os métodos usados (`isDrs`, `isSafetyCarNaPista`, `travouRodas`, `getRandom`, `getCircuito`, etc.) já são da interface. Isso também é o que faz os testes existentes continuarem funcionando sem mudar o tipo do mock (ver D4).

`ControleCorrida` continua instanciando e expondo cada classe nova (`controleDrs`, `controleFreio`, `controleEscapada`) do mesmo jeito que `controleAutomacao`, passando `controleJogo` (que satisfaz `InterfaceJogo`). `InterfaceJogo` ganha os métodos de delegação correspondentes (`processarUsoDRS(Piloto)`, `processarFreioNaReta(Piloto)`, `processarDerrapagem(Piloto)`, `processarEscapadaDaPista(Piloto)`). `ControleJogoLocal` implementa cada um delegando para a classe nova; `JogoCliente` ganha stubs no-op.

`Piloto.processaNovoIndex()` troca `processaDerrapagem()`/`processaEscapadaDaPista()`/`processaFreioNaReta()`/`processaUsoDRS()` pelas chamadas equivalentes via `controleJogo`, **nas mesmas posições exatas** da sequência atual — isso preserva a dependência de ordem com estresse (que fica em `Piloto`, roda antes).

### D4 — Testes: troca direta de chamada, não helper de rewiring

Achado na implementação de `ControleDrs`: os testes existentes (`PilotoDerrapagemTest`, `PilotoEscapadaAncoradaTracadoTest`, `PilotoFreioNaRetaZonaFrenagemTest`, `PilotoProcessaFreioNaRetaMolhadoTest`, `PilotoDrsDesligadoNoBoxTest`) já chamavam o método extraído **diretamente no `Piloto`** (ex.: `piloto.processaUsoDRS()`), não através de `controleJogo`. Isso é diferente da situação de `ControleAutomacao`, onde `PilotoAutopilotModoTest` exercita `piloto.processaMudarTracado()`, que **internamente** chama `controleJogo.decideXxx(this)` — só nesse caso o mock precisa ser religado a uma instância real, porque o próprio `Piloto` é quem invoca o delegate.

Para os 4 métodos deste lote, como o teste é quem chama o método diretamente (não `Piloto`), a atualização é mais simples: trocar `piloto.processaUsoDRS()` por `new ControleDrs(controleJogo).processaUsoDRS(piloto)` (instanciando a classe nova no teste, com o mesmo mock de `InterfaceJogo` já usado). Nenhum helper de rewiring do tipo `ligarControleAutomacao()` é necessário para `ControleDrs`/`ControleFreio`/`ControleEscapada`. `PilotoGanhoTracadoDeFugaTest` e `CircuitoEscapadaTracadoTest`, verificados durante a implementação, não chamam nenhum método movido neste lote (tocam `calculaModificadorPrincipal()` e `Circuito.reprocessarEscapadas()`, ambos fora de escopo) — não precisam de nenhuma alteração.

## Risks / Trade-offs

- **[Ordem do pipeline quebrar silenciosamente]** → Mitigação: as chamadas extraídas mantêm exatamente a mesma posição em `processaNovoIndex()`; nenhuma mecânica reordena.
- **[Mock de `InterfaceJogo` mascarar teste sem exercitar lógica real]** → Mitigação: aplicar o helper de rewiring (D4) em cada teste afetado, como parte das tasks de cada extração — não depois, como um "acerto" separado.
- **[Pilotos "modelo"/template sem `controleJogo` (ex. `TemporadasDefault.pilotos`)]** → Mitigação: qualquer novo método de delegação em `Piloto` que passe por `controleJogo` deve ser null-safe, replicando o fix aplicado a `isManualTemporario()`/`setManualTemporario()` na extração anterior.
- **[`Map<Piloto, X>` nas classes novas sem limpeza ao fim de partida]** → Mesmo padrão de `ControleAutomacao`, que já tem esse comportamento hoje; não é um risco introduzido por este lote, mas fica registrado como Open Question abaixo, já que este lote adiciona mais 2 mapas desse tipo (em `ControleEscapada` e `ControleFreio`).
- **[Campo com nome igual em duas classes causar confusão de leitura]** → `retardaFreiandoReta`/`freioNaRetaAvaliadoNesteEvento` deixam de ser campo de `Piloto` e viram estado de `ControleFreio` — mitigado só por revisão de código atenta durante o PR, sem mecanismo automático.

## Migration Plan

Três extrações independentes entre si (nenhuma depende de outra), recomendada esta ordem por fricção crescente:

1. **`ControleDrs`** primeiro — todos os campos que toca já são públicos (D2), nenhum getter/setter novo necessário. Valida o pipeline de wiring (D3) e o helper de rewiring de teste (D4) com o menor risco possível antes de aplicá-lo às extrações maiores.
2. **`ControleFreio`** — precisa de 1 getter/setter novo (`freioNaRetaMalSucedidoNesteTick`) e migração de 2 campos para dentro da classe nova.
3. **`ControleEscapada`** por último — a maior (~450 linhas), precisa de 1 getter/setter novo (`impedidoDeMudarTracadoPorEscapada`) e migração de 5 campos para dentro da classe nova.

Cada etapa é um PR próprio, com a suíte de testes completa (`mvn test`) verde antes de avançar para a próxima. Sem plano de rollback especial — é reposicionamento de código versionado normalmente; reverter um commit é suficiente se necessário.

## Open Questions

- Vale a pena, ao extrair `ControleFreio`, simplificar `retardaFreiandoReta`/`freioNaRetaAvaliadoNesteEvento` para variáveis locais dentro de um único método (já que seu ciclo de vida real é inteiramente dentro de uma chamada de `processaFreioNaReta()`) em vez de `Map<Piloto, X>`? Mais simples, mas foge do padrão uniforme das outras extrações. A decidir durante a implementação de `ControleFreio`.
- Os `Map<Piloto, X>` acumulados em `ControleAutomacao` (e, após este lote, também em `ControleEscapada`/`ControleFreio`) nunca são limpos ao fim de uma partida — isso é um vazamento real de memória em partidas de campeonato de longa duração com essas instâncias reutilizadas, ou é inofensivo porque a instância de `ControleCorrida`/`ControleJogoLocal` inteira é descartada ao fim da partida? Vale investigar como um follow-up separado, fora deste lote.
- Nomes definitivos dos métodos de delegação em `InterfaceJogo` (`processarDerrapagem` vs. `decideDerrapagem` vs. outro) — a decidir na implementação, seguindo a convenção de nomenclatura já usada por `ControleAutomacao` (verbo `processar`/`decide` conforme o método retorna `void` ou `boolean`).
