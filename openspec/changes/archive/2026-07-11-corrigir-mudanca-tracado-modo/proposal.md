## Why

`Piloto.processaMudarTracado()` usa um único gate (`isJogadorHumano() && MANUAL.equals(getAutomaticoManual())`) para decidir se todo o método roda ou não. Isso mistura, sob a mesma chave, decisões de "piloto automático" (a IA jogando pelo jogador humano) com mecânicas físicas que deveriam valer sempre (snap de traçado no box, desvio de carro batido sob safety car) e com uma mecânica exclusiva de bots (escape de fila indiana). Como resultado: no jogo online o gate nunca dispara (o `getAutomaticoManual()` do servidor/cliente está fixo em `AUTOMATICO`), então a IA assume decisões de traçado que deveriam ser sempre manuais para o humano online; e no solo em modo manual, o gate bloqueia até o snap de box e o desvio de safety car, que deveriam continuar valendo. Depois da reescrita de `ObjetoEscapada` ancorada ao traçado, essa mistura está produzindo jogabilidade ruim perceptível.

## What Changes

- `processaMudarTracado()` deixa de ter um gate único no topo. Os branches internos passam a ter regras próprias:
  - Snap de traçado ao entrar/sair do box e desvio automático de carro batido sob safety car passam a rodar sempre, para qualquer piloto, em qualquer modo (automático, manual ou online) — nunca dependem de `automaticoManual`.
  - `tentarEscaparFilaIndiana()` ganha um guard explícito `!isJogadorHumano()`: só bots de IA tentam escapar da fila indiana; o jogador humano nunca é afetado por essa mecânica, em nenhum modo.
  - O branch de desviar de retardatário à frente passa a seguir a mesma regra de 3 estados já usada em `processaIAnovoIndex()`: desligado para o humano em modo manual local, sempre desligado para o humano online (`controleJogo instanceof JogoServidor`), e ligado no automático local exceto durante a janela de `isManualTemporario()`.
- Nenhuma mudança em `processaIAnovoIndex()` (giro do motor, ERS, DRS, ultrapassagem/defesa) — já implementa corretamente a regra dos 3 estados; serve de referência.
- Nenhuma mudança em `processaEscapadaDaPista()`/`escapaTracado()` (escapada por stress, derrapagem por pneu gasto em curva baixa) — já rodam incondicionalmente e continuam assim.

## Capabilities

### New Capabilities
- `piloto-controle-automatico-manual`: define quando o "piloto automático" (IA decidindo pelo jogador humano) está ativo — online sempre desligado, solo manual sempre desligado, solo automático ligado exceto durante a janela pós-input (`manualTemporario`) — e quais subsistemas essa chave governa (giro/ERS/DRS/ultrapassagem-defesa e a escolha proativa de traçado por retardatário), deixando explícito que mecânicas físicas (box, desvio de safety car, escapada, derrapagem) e a mecânica exclusiva de bots (fila indiana) ficam fora dessa chave.

### Modified Capabilities
- `tracado-safe-lane-change`: o requisito "Escape de fila indiana" passa a exigir explicitamente que o piloto não seja o jogador humano (hoje não há essa restrição, e o jogador humano em modo automático é afetado incorretamente); novos requisitos documentam que o snap de traçado no box e o desvio de carro batido sob safety car não dependem do modo automático/manual do piloto.

## Impact

- Código: `src/main/java/br/f1mane/entidades/Piloto.java` (`processaMudarTracado()`, `tentarEscaparFilaIndiana()`).
- Sem mudança de API pública, sem mudança de schema/persistência.
- Afeta diretamente a jogabilidade em três contextos: jogo solo Swing (ambos os modos), multiplayer online (servidor `JogoServidor` e cliente `JogoCliente`/`AppletPaddock`).
- Fora de escopo (não alterado por esta mudança): o consumo de escapada via `circuito.getEscapeMap()` em corrida, que já é documentado como não implementado (`circuito.getEscapeMap()` retorna sempre mapa vazio, ver spec `objeto-escapada-tracado`) — a "escapada" que já roda incondicionalmente hoje é só a de stress em curva alta (traçado 4/5, quando o mapa tiver dados) e a de pneu gasto em curva baixa (traçado 1/2).
