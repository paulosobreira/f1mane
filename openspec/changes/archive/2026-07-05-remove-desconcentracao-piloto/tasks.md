## 1. Remover os gates de verificaDesconcentrado() sem substituto

- [x] 1.1 Remover a checagem de `verificaDesconcentrado()` de `testeHabilidadePiloto()`
- [x] 1.2 Remover a checagem de `verificaDesconcentrado()` de `processaIAnovoIndex()`
- [x] 1.3 Remover a checagem de `verificaDesconcentrado()` de `iaTentaUsarDRS()` e `iaTentaUsarErs()`
- [x] 1.4 Remover a checagem de `verificaDesconcentrado()` de `tentarEscaparFilaIndiana()`
- [x] 1.5 Remover a checagem de `verificaDesconcentrado()` de `mudarTracado(...)`
- [x] 1.6 `mvn -o clean compile`: sem erros

## 2. processaMudancaRegime(): removido sem substituto (revisado após feedback do usuário)

- [x] 2.1 ~~Trocar `if (verificaDesconcentrado())` por `if (getStress() >= 90)`~~ — revisado: o usuário não gostou dessa "penalidade substituta" (ainda é um bloqueio de comportamento). A condição de estresse foi removida por completo, sem substituto — `processaMudancaRegime()` só trata bandeirada (LENTO/giro mínimo, inalterado) e roda as mensagens de modo incondicionalmente
- [x] 2.2 Atualizado `PilotoProcessaMudancaRegimeTest.java` (2 testes): estresse no máximo (100) não força downgrade de modo/giro; bandeirada continua forçando LENTO/giro mínimo

## 3. Converter os 4 gatilhos de ciclosDesconcentrado em incremento de estresse (reduzido pela metade, e à metade de novo se passar no teste de habilidade)

- [x] 3.1 Em `processaEscapadaDaPista()`, trocado `setCiclosDesconcentrado(testeHabilidadePiloto() ? 5 : 7)` por `incStress(testeHabilidadePiloto() ? 1 : 3)` (colapsa a distinção 5/7 num valor cheio único de 7, metade=3, testeHabilidadePiloto() reduz à metade de novo=1)
- [x] 3.2 Em `desviaPilotoNaFrente()`, trocado `pilotoNaFrente.setCiclosDesconcentrado(5)` por `pilotoNaFrente.incStress(testeHabilidadePiloto() ? 1 : 2)` (valor cheio 5, metade=2, testeHabilidadePiloto() reduz à metade de novo=1)
- [x] 3.3 Em `mensagemRetardatario()`, trocado `pilotoNaFrente.setCiclosDesconcentrado(10)` por `pilotoNaFrente.incStress(testeHabilidadePiloto() ? 2 : 5)` (valor cheio 10, metade=5, testeHabilidadePiloto() reduz à metade de novo=2)
- [x] 3.4 Em `ControleQualificacao`, trocado `piloto.setCiclosDesconcentrado(controleJogo.getRandom().intervalo(500, 700))` por um sorteio de `intervalo(15, 20)` (metade do valor cheio 30-40), reduzido à metade de novo com `piloto.incStress(testeHabilidadePiloto() ? valor / 2 : valor)`
- [x] 3.5 Mensagem/flag de "problema na largada" movida pro momento do evento em `ControleQualificacao`
- [x] 3.6 Criado `PilotoDesconcentracaoConvertidaTest.java` (4 testes) cobrindo os 3 gatilhos em `Piloto` (escapada de pista, ceder passagem, retardatário), com e sem sucesso no teste de habilidade — inclui a descoberta de que `incStress()` já escala por 0.5 em modo NORMAL/AGRESSIVO (empilhando com a redução deste gatilho, confirmado com o usuário) e que o cap `stress>90→val=1` domina no caso da escapada de pista. Largada ruim em `ControleQualificacao` verificada por inspeção de código (construir um `Circuito` real é caro demais pro valor do teste, mesmo critério usado antes pra `ControleCorrida.danificaAreofolio`)

## 4. Remover o mecanismo de desconcentração

- [x] 4.1 Removido o campo `ciclosDesconcentrado`, o método `verificaDesconcentrado()`, `decrementaPilotoDesconcentrado()`, o campo `desconcentradoAgressivoNesteTick` e o método `processaStressDesconcentradoAgressivo()` de `Piloto.java` — também removido `problemaLargada`/`isProblemaLargada()`/`setProblemaLargada()`, que ficaram sem nenhum leitor depois que a mensagem passou a disparar direto em `ControleQualificacao`
- [x] 4.2 Removida a chamada de `decrementaPilotoDesconcentrado()` do tick principal e a chamada de `processaStressDesconcentradoAgressivo()` dentro de `processaStress()`
- [x] 4.3 Removido `pilotoFrente.setCiclosDesconcentrado(0)` de `processaPenalidadeColisao()`
- [x] 4.4 Confirmado via `grep -rn "esconcentrad"` em `src/main/java`: nenhuma referência restante

## 5. Limpeza de testes e verificação final

- [x] 5.1 Removidos de `PilotoProcessaStressConsolidadoTest.java` os 4 testes do mecanismo descontinuado; atualizado o comentário de documentação da classe
- [x] 5.2 Criado `PilotoTesteHabilidadeSemBloqueioTest.java` (3 testes) confirmando que `testeHabilidadePiloto()` — o bloqueio mais abrangente — resolve só por `danificado()`/habilidade, sem condição equivalente; os outros 5 gates (processaIAnovoIndex, DRS, ERS, escape de fila, mudarTracado) verificados por remoção de código + compilação limpa + suíte completa inalterada
- [x] 5.3 `mvn -o clean test`: 383 testes, 0 falhas
- [x] 5.4 Simulação manual rodada com sucesso (`mvn package -Ph2 -DskipTests` + `MainFrameSimulacao 2024 Catalunya 72`, após o usuário ativar o circuito de Catalunya — a falha anterior era o circuito estar marcado `ativo=false`, não um problema de ambiente): corrida completa de 72 voltas, 0 erros no log, classificação final de 20 pilotos gerada normalmente, 2091 checagens de colisão e 11 bandeiradas registradas. Confirma que a remoção da mecânica de desconcentração e a conversão dos 4 gatilhos pra estresse funcionam de ponta a ponta numa corrida real, não só nos testes unitários

## 6. Levantamento e correção: desgaste de pneu gerando estresse em reta/largada (fora do escopo original, achado ao investigar o pedido do usuário)

- [x] 6.1 Instrumentado temporariamente `incStress()` (captura de origem via stack trace + mapa estático de contagem/soma, dump num shutdown hook) pra medir a contribuição real de cada gatilho de estresse numa corrida completa
- [x] 6.2 Rodada a simulação 2x: desgaste de pneu respondia por ~95-96% de todo o estresse gerado (reta/largada sozinho ~60-62%, curva baixa ~33-35%) — usuário confirmou que nunca houve intenção de gerar estresse por desgaste de pneu numa reta/largada
- [x] 6.3 Removido por completo o ramo `else if (no.verificaRetaOuLargada())` de `processaStressDesgastePneus()` (só continha a chamada de incremento, nenhum outro efeito) — reta/largada não gera mais estresse por desgaste de pneu, mantendo curva baixa (incremento) e curva alta (decremento) inalterados
- [x] 6.4 Atualizados os 2 testes que cobriam o incremento removido em `CarroCalculaDesgastePneusStressTest.java` pra confirmar ausência de alteração de estresse (NORMAL e AGRESSIVO)
- [x] 6.5 Revertida toda a instrumentação temporária (confirmado via grep, `incStress()` idêntico ao estado anterior)
- [x] 6.6 Rodada a simulação instrumentada mais 2x após a remoção pra confirmar o novo total: desgaste de pneu (só curva baixa agora) passou a ~92-93% do total — segundo maior contribuinte agora é "ceder passagem" (`desviaPilotoNaFrente`, ~6-7%)
- [x] 6.7 `mvn -o clean test`: 383 testes, 0 falhas

## 7. Redistribuição: subir colisão/aerofólio/freada mal-sucedida pro patamar de "ceder passagem" (pedido do usuário após ver o levantamento do grupo 6)

- [x] 7.1 Confirmado: curva alta ("curva de média") não gera estresse na prática — só existe um `incStress(2)` condicional em `processaStressPneusIncompativeis()` (pneu incompatível com o clima), que não ocorreu em nenhuma das simulações; desgaste de pneu em curva alta só tem `decStress` (recuperação)
- [x] 7.2 Calculado, a partir dos dados do grupo 6, o multiplicador necessário pra cada gatilho atingir a soma de "ceder passagem" (~27.823/corrida): colisão (~2.101 eventos) precisaria de ~13,2/evento; aerofólio (~52 eventos) precisaria de ~535/evento; freada mal-sucedida (~12,5 eventos) precisaria de ~2.226/evento — os dois últimos são inviáveis só por magnitude (estourariam a escala de 0-100 num único evento). Usuário confirmou: aumentar magnitude moderadamente nos três, aceitando que aerofólio/freada continuem com % baixo por serem raros
- [x] 7.3 `processaStressColisao()`: `incStress(1)` → `incStress(13)` (nas duas chamadas, incluindo o bônus de `evitaBaterCarroFrente`)
- [x] 7.4 `processaStressDanoAereofolio()`: `incStress(15)` → `incStress(30)`
- [x] 7.5 Gatilho de freada mal-sucedida (dentro de `processaFreioNaReta()`): `freioNaRetaMalSucedidoNesteTick = 10 - (desgastePneus/100)` → `30 - (desgastePneus/100)`
- [x] 7.6 Atualizados os testes afetados em `PilotoProcessaStressConsolidadoTest.java` (colisão: 1→7 escalado; dano de aerofólio: 8→15 escalado)
- [x] 7.7 Medição real (2 corridas, instrumentação temporária revertida depois): colisão subiu de ~0,5% pra ~1,8-2,3% do total — melhora real, mas não chegou no patamar de "ceder passagem" (~6,5-7%). Aerofólio e freada mal-sucedida praticamente não mudaram (~0,1% e ~0,05%) apesar do aumento de magnitude
- [x] 7.8 **Achado**: o cap pré-existente de `incStress()` (`stress>90→val=1`, `stress>80→val=2`, `stress>70→val=3`) domina justamente nesses três gatilhos, porque colisão/dano de aerofólio/freada mal-sucedida tendem a ocorrer quando o piloto já está com estresse elevado (esses eventos correlacionam com estresse alto) — o valor pedido (13 ou 30) é sistematicamente reduzido a 1-3 na maioria dos casos, então aumentar a magnitude bruta tem efeito limitado. Usuário optou por deixar como está por agora, sem enfraquecer o cap (que afetaria todos os gatilhos de estresse, não só esses três)
- [x] 7.9 `mvn -o clean test`: 383 testes, 0 falhas
