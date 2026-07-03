## 1. Limite posicional de colisão (collision-physics-block)

- [x] 1.1 Implementar `Piloto.limitaAvancoCarroFrente(long avanco)`: percorre pilotos na mesma linha (mesmo `tracado`, ou cruzando a partir de `tracadoAntigo`), calcula distância real (`noIndex`, com wrap de volta) e reduz o avanço para nunca entrar na área do carro à frente.
- [x] 1.2 Aplicar `limitaAvancoCarroFrente` sobre `roundGanho` em `processaNovoIndex` (ciclo normal) e sobre o avanço de "devagarinho pós-bandeirada`.
- [x] 1.3 Estender `processaPenalidadeColisao` para tratar como mesma linha também o carro à frente com `indiceTracado > 0` e `tracadoAntigo` igual ao traçado do carro de trás.
- [x] 1.4 Atualizar `openspec/specs/collision-physics-block/spec.md` (delta MODIFIED) com os requirements de avanço posicional e de reconhecimento de carro em transição de linha.

## 2. Escape de fila indiana (tracado-safe-lane-change)

- [x] 2.1 Adicionar contador `ciclosPresoFila`: incrementa em `processaPenalidadeColisao` quando há colisão na mesma linha com `ganho` ≤ 10; zera quando a colisão cessa.
- [x] 2.2 Implementar `Piloto.verificaTracadoLivreParaEscapar(int alvo)`: verifica ausência de carros ocupando ou cruzando o traçado-alvo numa janela de -100/+60 nós.
- [x] 2.3 Implementar `Piloto.tentarEscaparFilaIndiana()`: dispara após 8 ciclos presos, tenta os traçados laterais possíveis (0→1/2, 1/2→0), desabilitado sob safety car, bandeirada, box e traçados de escapada (4/5).
- [x] 2.4 Adicionar sobrecarga `mudarTracado(int, boolean forcaMudar, boolean escapandoFila)` que pula apenas `verificaColisaoAoMudarDeTracado` quando `escapandoFila=true`, preservando os demais guards.
- [x] 2.5 Acionar `tentarEscaparFilaIndiana` em `processaMudarTracado`, antes dos desvios normais de retardatário.

## 3. Cooldown e proteção da animação de troca (tracado-safe-lane-change)

- [x] 3.1 Calcular o piso do cooldown de `mudarTracado` como `(Circuito.getIndiceTracado() / 2) + 4` ciclos quando a atualização suave está ativa, em vez de um valor fixo.
- [x] 3.2 Garantir que `ultimaMudancaPos` só é atualizado quando a mudança é efetivamente aplicada, não em tentativas bloqueadas.
- [x] 3.3 Adicionar guard `indiceTracado == 0` em `escapaTracado` (escapada por stress espera a animação atual terminar).
- [x] 3.4 Adicionar guard `indiceTracado == 0` no desvio forçado de safety car em `processaMudarTracado`.
- [x] 3.5 Em `mudarTracado`, quando uma mudança forçada ocorre com `indiceTracado > 0` e reverte para `tracadoAntigo`, recalcular `indiceTracado` espelhando o progresso já percorrido em vez de reiniciar a animação.
- [x] 3.6 Atualizar `openspec/specs/tracado-safe-lane-change/spec.md` (novo, ADDED) com os requirements de cooldown, proteção de animação e escape de fila.

## 4. Diagnóstico e validação

- [x] 4.1 Adicionar logs `[OVERLAP_REAL]` (em `ControleJogoLocal.atualizaIndexTracadoPilotos`), `[ESCAPE_FILA]` e `[TRACADO_RESET]` atrás de `Global.LOG_COLISAO`.
- [x] 4.2 Rodar `MainFrameSimulacao` headless em múltiplos circuitos/temporadas e confirmar ausência de `[OVERLAP_REAL]` nos logs.
- [x] 4.3 Escrever `PilotoColisaoFilaTest` cobrindo `limitaAvancoCarroFrente`, `verificaTracadoLivreParaEscapar` e o comportamento de `mudarTracado` durante animação em andamento.
- [x] 4.4 Escrever `PilotoPenalidadeEscapeFilaTest` cobrindo a cadeia completa com geometria real: detecção de colisão, penalidade (incluindo carro em transição), contador de fila e cooldown.
- [x] 4.5 Rodar a suíte completa (`mvn test`) e confirmar 0 falhas antes do commit.

## 5. Entrega

- [x] 5.1 Commit da correção e dos testes em branch dedicada (`feature/correcao-overlap-colisao-tracado`).
- [x] 5.2 Push da branch para o remoto.
- [ ] 5.3 Abrir PR e mesclar em `master`.
