## 1. Gatilhos já internos a Piloto.java

- [x] 1.0 Mover a chamada de `processaStress()` no tick principal (`moveCarro`) para depois de `processaColisao`, `processaEvitaBaterCarroFrente`, `processaFreioNaReta` e `decrementaPilotoDesconcentrado` (perto de onde `controleJogo.verificaAcidente(this)` já roda) — ver Decisão 5 revisada em design.md
- [x] 1.1 Migrar `processaPneusIncomaptiveis()`: mover a condição (pneu incompatível + curva baixa/alta) e os valores (4/2, reduzidos a 0 com `testeHabilidadePiloto()`) para dentro de `processaStress()`; remover as duas chamadas `incStress` originais
- [x] 1.2 Migrar `processaPenalidadeColisao()`: mover a condição (`getColisao() != null`, bônus se `evitaBaterCarroFrente`) e o valor (`incStress(1)` x2) para dentro de `processaStress()`; remover as chamadas originais
- [x] 1.3 Migrar `processaFreioNaReta()`: manter a condição e o reset de `retardaFreiandoReta` exatamente onde estão, mas substituir a chamada `incStress` por um flag consumível (`freioNaRetaMalSucedidoNesteTick`, guardando a magnitude `10 - desgastePneus/100`); `processaStress()` consome e limpa o flag
- [x] 1.4 Migrar `decrementaPilotoDesconcentrado()`: usar flag consumível (`desconcentradoAgressivoNesteTick`) ao invés de duplicar a condição, já que o próprio método decrementa `ciclosDesconcentrado` antes que `processaStress()` pudesse rederivá-la
- [x] 1.5 Criar `PilotoProcessaStressConsolidadoTest.java` (11 testes) cobrindo os 4 gatilhos, verificando o resultado via os novos métodos privados `processaStress*()` + `getStress()`, e confirmando que os métodos originais não alteram mais o estresse diretamente
- [x] 1.6 `mvn -o clean test`: 355 testes, 0 falhas

## 2. Desgaste de pneu (Carro.calculaDesgastePneus)

- [x] 2.1 Mover para `processaStress()` (novo helper `processaStressDesgastePneus()`, chamado primeiro, espelhando a posição original de `calculaDesgastePneus` no tick) os 4 gatilhos: incremento em curva baixa, decremento em curva baixa (stress>80), decremento em curva alta (stress>70), incremento em reta/largada (stress>60) — preservando as fórmulas baseadas em `getPorcentagemDesgastePneus()` e os testes de habilidade
- [x] 2.2 Confirmado: `Carro.calculaDesgastePneus()` continua lendo `getStress()` normalmente onde precisa decidir `teste`/`desgPneus`, apenas sem mais chamar `incStress`/`decStress`
- [x] 2.3 Criado `CarroCalculaDesgastePneusStressTest.java` (9 testes) cobrindo os 4 gatilhos migrados e confirmando que `calculaDesgastePneus()` não altera mais o estresse
- [x] 2.4 `mvn -o clean test`: 364 testes, 0 falhas

## 3. Fila do box (ControleBox)

- [x] 3.1 Descoberto: `processaStress()` nunca roda enquanto `getPtosBox() != 0` (gate em `ControleCiclo`); em vez de expor estado novo, criar `Piloto.processaStressFilaBox()` como método dedicado (fora de `processaStress()`) — ver Decisão 4 revisada em design.md
- [x] 3.2 Chamar `piloto.processaStressFilaBox()` a partir de `ControleBox.processarPilotoBox()` no lugar do `decStress(2)` direto
- [x] 3.3 Criar teste cobrindo esse gatilho e confirmando que `ControleBox` não chama mais `decStress` diretamente
- [x] 3.4 Rodar `mvn -o clean test`

## 4. Acidente com perda de aerofólio (ControleCorrida.danificaAreofolio)

- [x] 4.1 Adicionado `Piloto.sofreuDanoAereofolioNesteTick` + `sinalizaDanoAereofolio()`, chamado por `ControleCorrida.danificaAreofolio()` ao invés de `incStress(15)` diretamente
- [x] 4.2 `processaStress()` chama `processaStressDanoAereofolio()`, que consome e limpa o flag, chamando `incStress(15)`
- [x] 4.3 Testes cobrindo flag setado/não setado em `PilotoProcessaStressConsolidadoTest.java` (3 novos testes); `ControleCorrida.danificaAreofolio()` não chama mais `incStress` (confirmado por leitura direta — instanciar `ControleCorrida` isoladamente exige setup pesado, não vale o custo pra uma mudança de 1 linha)
- [x] 4.4 `mvn -o clean test`: 368 testes, 0 falhas

## 5. Verificação final

- [x] 5.1 Confirmado via busca (`grep -rn "\.incStress(\|\.decStress("`): nenhuma chamada fora de `Piloto.java`; dentro de `Piloto.java`, todas as chamadas estão em `processaStress()` e seus helpers `processaStress*()` (mais `processaStressFilaBox()`, exceção documentada na Decisão 4)
- [x] 5.2 `mvn -o clean test`: 368 testes, 0 falhas
- [x] 5.3 Nenhum teste do projeto usa `GameRandom` com seed real ou depende de trajetória exata de simulação (todos usam Mockito com stubs por chamada) — nenhum ajuste necessário
