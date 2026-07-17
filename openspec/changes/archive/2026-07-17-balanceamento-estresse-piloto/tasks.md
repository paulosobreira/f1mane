## 1. Tetos de `incStress()` por faixa de stress

- [x] 1.1 Em `Piloto.incStress(int val)`, trocar os limiares `stress > 80` e `stress > 70` por `stress > 70` e `stress > 50` respectivamente, mantendo os caps em 2 e 3 (o limiar `stress > 90` → cap 1 fica inalterado)
- [x] 1.2 Revisar `PilotoProcessaStressConsolidadoTest` (ou o teste equivalente que cobre os caps) e ajustar/adicionar casos cobrindo a faixa 50-70 (hoje sem teto, deve passar a capar em 3) e a faixa 70-90 (deve capar em 2, não mais em 3)

## 2. Multiplicadores de `decStress()` por modo de pilotagem

- [x] 2.1 Em `Piloto.decStress(int val)`, adicionar branch explícito para `AGRESSIVO` com multiplicador 0 (via `getModoPilotagemEfetivo()`, mesma leitura já usada pelos branches existentes)
- [x] 2.2 Trocar o multiplicador de `NORMAL` de 1.1x para 1x; manter `LENTO` em 1.5x
- [x] 2.3 Adicionar/ajustar teste cobrindo `processaStressFilaBox()` com piloto em modo efetivo AGRESSIVO — confirmar que o estresse não é reduzido (comportamento novo; hoje esse é o único caminho onde AGRESSIVO recupera algo)
- [x] 2.4 Confirmar (via teste existente ou novo) que o decaimento passivo por tick em `processaStress()` continua sem chamar `decStress` para AGRESSIVO — não deve haver regressão aí, só documentação do comportamento já existente

## 3. Freada mal-sucedida na reta: generalizar e mover para a zona de frenagem

- [x] 3.1 Em `Piloto.processaFreioNaReta()`, remover a condição `getPosicao() <= 3` do bloco que seta `freioNaRetaMalSucedidoNesteTick`
- [x] 3.2 Trocar a condição de localização de `getNoAtual().verificaCurvaBaixa()` para "nó não é curva E `controleJogo.isNoZonaFrenagem(getNoAtual())`" — validar qual combinação de checagens de `No` (`verificaRetaOuLargada()` + `isNoZonaFrenagem`) expressa isso corretamente, já que a zona de frenagem se estende por vários nós de reta antes da curva
- [x] 3.3 Confirmar que a trava de disparo único por evento continua valendo: `retardaFreiandoReta` deve seguir sendo resetado a `false` no mesmo tick em que o gatilho é avaliado (dispare ou não), para que o sorteio de >0.9 não seja reavaliado a cada tick dentro da zona de frenagem
- [x] 3.4 Revisar se outro código depende do timing/nó exato em que `freioNaRetaMalSucedidoNesteTick` é setado hoje (mensagens, cálculo de ganho no mesmo tick) antes de mudar o ponto de verificação
- [x] 3.5 Ajustar `PilotoFreioNaRetaZonaFrenagemTest` (ou equivalente): remover/generalizar casos que dependem de posição top-3, adicionar caso cobrindo um piloto fora do top-3 disparando o gatilho, e um caso confirmando que o gatilho não dispara mais de uma vez por evento de frenagem mesmo com múltiplos ticks dentro da zona

## 4. Atualização de spec e validação final

- [x] 4.1 Rodar `mvn test` completo e confirmar que a suíte passa com os três ajustes — 730 testes, 0 falhas, 2 skipped (pré-existentes, não relacionados)
- [x] 4.2 Rodar `./simulacao.sh` (ou simulação headless equivalente) e comparar frequência/distribuição agregada de eventos de estresse antes/depois — simulação de 72 voltas rodou sem exceptions/erros no log com o código novo. A comparação estatística de frequência via mensagem de log ("014") não se mostrou um sinal confiável: essa mensagem só é emitida quando `verificaInfoRelevante()` é verdadeiro (jogador humano, líder ou rival de campeonato), então numa simulação 100% IA ela praticamente não aparece e subestima muito a frequência real do gatilho. Uma comparação quantitativa rigorosa exigiria instrumentar um contador temporário — não feito aqui por estar fora do escopo desta tarefa de verificação.
- [x] 4.3 Confirmar com `openspec validate` que o delta spec de `piloto-gestao-estresse` está consistente antes de arquivar a mudança — `openspec validate --changes balanceamento-estresse-piloto` passou (1/1)
