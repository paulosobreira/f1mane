## 1. Checagem central de piloto automático

- [x] 1.1 Extrair um método privado em `Piloto` (ex.: `autopilotDesligado()`) com a expressão `isJogadorHumano() && (Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual()) || controleJogo instanceof JogoServidor)`, hoje inline em `processaIAnovoIndex()`.
- [x] 1.2 Atualizar `processaIAnovoIndex()` para usar o novo método no lugar da expressão inline, sem mudar comportamento.

## 2. Reestruturar `processaMudarTracado()`

- [x] 2.1 Mover os branches de snap de traçado no box (`isBoxSaiuNestaVolta()`/`verificaSaidaBox()`, `isBox()`/`verificaEntradaBox()`) para antes de qualquer checagem de piloto automático/manual, mantendo as condições de negócio já existentes.
- [x] 2.2 Mover o branch de desvio de carro batido sob safety car (`pilotoBateu`/`isSafetyCarNaPista()`) para antes de qualquer checagem de piloto automático/manual, mantendo as condições de negócio já existentes.
- [x] 2.3 Remover o gate único no topo do método (`if (isJogadorHumano() && MANUAL.equals(...)) return;`).
- [x] 2.4 Aplicar `autopilotDesligado()` (de 1.1) apenas ao branch de desviar de retardatário à frente (`evitaBaterCarroFrente`/`calculaDiffParaProximoRetardatarioMesmoTracado`), incluindo a checagem de `isManualTemporario()`/`decrementaManualTemporario()` do mesmo jeito que `processaIAnovoIndex()` já faz.
- [x] 2.5 Adicionar guard `!isJogadorHumano()` dentro de `tentarEscaparFilaIndiana()`.

## 3. Testes

- [x] 3.1 Teste: jogador humano em `JogoServidor` (ou double equivalente) nunca tem o traçado mudado pelo branch de desviar de retardatário, mesmo com as condições de distância satisfeitas.
- [x] 3.2 Teste: jogador humano solo em modo manual nunca tem o traçado mudado pelo branch de desviar de retardatário.
- [x] 3.3 Teste: jogador humano solo em modo automático tem o traçado mudado pelo branch de desviar de retardatário quando as condições são satisfeitas e `manualTemporario` está zerado; e NÃO tem quando `manualTemporario` > 0.
- [x] 3.4 Teste: jogador humano em modo manual (local e online) continua tendo o traçado ajustado corretamente ao entrar/sair do box.
- [x] 3.5 Teste: jogador humano em modo manual (local e online) continua desviando corretamente de um carro batido sob safety car.
- [x] 3.6 Teste: `tentarEscaparFilaIndiana()` nunca muda o traçado do jogador humano, mesmo com todas as condições de "preso na fila" satisfeitas; bots continuam escapando normalmente.
- [x] 3.7 Rodar `mvn test` completo e confirmar que a suíte existente (incluindo testes de `Piloto`/`ControleCorrida` relacionados a traçado) continua passando.

## 4. Verificação manual

- [ ] 4.1 Rodar uma corrida solo em modo automático, confirmar que o autopilot assume o carro e que uma entrada do jogador suspende temporariamente (contador `manualTemporario`).
- [ ] 4.2 Rodar uma corrida solo em modo manual, confirmar que o jogador controla giro/ERS/DRS/traçado por retardatário, mas o snap de box e o desvio de safety car continuam automáticos.
- [ ] 4.3 Rodar uma corrida online (`./simulacao.sh` ou sessão multiplayer local), confirmar que o jogador humano nunca tem o traçado trocado por autopilot de retardatário, mas continua tendo box e desvio de safety car automáticos.
