## 1. Restaurar o gate por intervalo de voltas em ControleClima

- [x] 1.1 Restaurar o campo `numVoltas` em `ControleClima` (usado por `intervaloSol()`)
- [x] 1.2 Restaurar `intervaloNublado()`, `intervaloSol()`, `intervaloChuva()` com as fórmulas originais (`git show 6ee89de2^:src/main/java/br/f1mane/controles/ControleClima.java` como referência), incluindo a mensagem de previsão de chuva em `intervaloNublado()`
- [x] 1.3 Restaurar `processaPossivelMudancaClima()` para o gate único de intervalo de voltas aplicado a toda avaliação (remover o desvio condicionado a `primeiraMudancaAplicada`), mantendo os logs de debug adicionados nesta sessão (volta atual, clima, molhado%, próxima avaliação, DISPARADA/ADIADA)
- [x] 1.4 Remover o campo `primeiraMudancaAplicada` e os métodos `aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()`, que ficam sem uso

## 2. Restaurar o disparo em ThreadMudancaClima

- [x] 2.1 `ThreadMudancaClima.run()` volta a chamar `intervaloNublado()`/`intervaloSol()`/`intervaloChuva()` (com base no clima atual e `verificaPossibilidadeChoverNaPista()`) em vez de `aplicarPrimeiraMudanca()`/`aplicarMudancaSubsequente()`
- [x] 2.2 Manter o atraso de disparo sorteado entre 0 e `Global.ATRASO_MAX_MUDANCA_CLIMA_MS` (sem alteração — não reintroduzir `tempoMedioVoltaMs`)

## 3. Testes

- [x] 3.1 Remover `ControleClimaMudancaTest.java` (testava o mecanismo revertido: primeira troca única + repetição a cada volta)
- [x] 3.2 Confirmar que `ThreadMudancaClimaDisparoTest.java` continua passando sem alteração (só testa o atraso fixo de 1 minuto, não o gate de voltas)
- [x] 3.3 Confirmar que `ControleClimaMolhadoTest.java` e `CarroTemperaturaMotorClimaTest.java` continuam passando sem alteração (ortogonais a este change)
- [x] 3.4 Rodar a suíte completa (`mvn test`) e confirmar 0 falhas/erros — 771 testes, 0 falhas, 0 erros, 2 skips pré-existentes

## 4. Sincronizar a spec

- [x] 4.1 Rodar o sync da spec `clima-transicao-gradual` a partir da delta deste change (remove o requirement de `tempoMedioVoltaMs`, atualiza os dois requirements que mencionavam `tempoMedioVoltaMs` pros valores fixos em minutos)
- [x] 4.2 Conferir que o requirement "Avaliação de mudança de clima gatilhada por intervalo de voltas sorteado" já reflete o mecanismo restaurado sem precisar de edição (era o único requirement que já descrevia o gate aplicado a toda transição)

## 5. Arquivar o change

- [x] 5.1 Arquivar `clima-restaura-intervalo-voltas` após a suíte passar e a spec estar sincronizada
