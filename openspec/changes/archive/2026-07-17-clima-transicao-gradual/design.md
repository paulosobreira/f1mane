## Context

O sistema de clima (`ControleClima`/`ThreadMudancaClima`) hoje só guarda um estado categórico (`clima` = `SOL`/`NUBLADO`/`CHUVA`) e transiciona esse estado uma vez por volta do líder, sempre passando por `NUBLADO` como intermediário entre `SOL` e `CHUVA` (nunca direto). O disparo real da troca acontece numa `ThreadMudancaClima` que hoje só dorme um valor fixo de narrativa (3–15s reais) sem relação com o ritmo da corrida.

Três pontos do cálculo de `ganho` leem esse estado categórico como um booleano puro via `InterfaceJogo.isChovendo()`:

- `Carro.calculaModificadorPneu` (Carro.java:585-621) — multiplicador de `ganho` em curva.
- `Piloto.calculaModificadorPrincipal` (Piloto.java:3212-3264) — redução do limiar `comparador` usado no sorteio da banda base de `ganho`.
- `Piloto.processaFreioNaReta` (Piloto.java:2268-2336) — redução do piso `minMulti` do multiplicador de frenagem na reta.

Em todos os três, a troca é instantânea: no ciclo em que `clima` muda para `CHUVA`, o carro já sofre 100% da penalidade; no ciclo em que sai de `CHUVA`, a penalidade some 100%. Não existe hoje nenhum conceito de "quão molhada a pista está" independente da flag categórica — nem no client (`JogoCliente`) nem no servidor.

Do lado do pit stop, a IA já sempre calça o pneu certo pro clima (`ControleBox.java:444-457`/`538-550`), mas o jogador humano escolhe livremente (`ControleJogoLocal.setUpJogadorHumano`, ControleJogoLocal.java:808-821), inclusive durante chuva.

## Goals / Non-Goals

**Goals:**
- Dar a `ControleClima` acesso a um tempo médio de volta em ms reais.
- Fazer `ThreadMudancaClima` disparar a mudança de clima num atraso aleatório dentro de `[0, tempoMedioVoltaMs]`, em vez do delay fixo de narrativa.
- Introduzir um estado contínuo "molhado%" (0.0–1.0) em `ControleClima`, com rampa de ~1 tempo médio de volta entre 0% e 100%, reversível a partir do valor atual se o clima virar de novo no meio da rampa.
- Interpolar por "molhado%" as três fórmulas de `ganho` listadas no Context.
- Forçar pneu de chuva no pit stop de qualquer piloto (humano incluso) enquanto o clima vigente for `CHUVA`, em qualquer ponto da rampa.

**Non-Goals:**
- Não alterar `Carro.calculaModificadorAsa` — bônus/penalidade de asa continuam binários, só refletindo o clima categórico atual (100% seco/nublado ou 100% chuva).
- Não alterar `Carro.verificaPneusIncompativeisClima()`/`Carro.testeFreios()` nem nenhum sistema que dependa deles (stress, risco de acidente do jogador humano, escapada, decisão de box automático da IA) — permanecem binários, sem rampa.
- Não forçar pneu seco ao sair da chuva — a escolha volta a ser livre assim que o clima categórico deixa de ser `CHUVA`.
- Não mudar a probabilidade de chover (`verificaPossibilidadeChoverNaPista`) nem a lógica de quando o clima categórico transiciona (`intervaloSol`/`intervaloNublado`/`intervaloChuva` continuam regendo o "quando" em voltas).
- Não introduzir um teto determinístico (`min(ganhoSeco, ganhoMolhado)`) para garantir matematicamente que nenhum tick de chuva supere um tick de seco — ver D5. Reta fora de zona de frenagem e frenagem-antes-de-curva-alta continuam sem nenhuma penalidade de chuva, como já é hoje.

## Decisions

### D1 — Fonte do tempo médio de volta: média das voltas completadas do líder, com fallback fixo de 1 minuto
`tempoMedioVoltaMs()` (novo método em `InterfaceJogo`, implementado em `ControleJogoLocal` e `JogoCliente`) calcula a média de `Volta.obterTempoVolta()` (em ciclos) das voltas já registradas do líder (`getPilotos().get(0)`, mesmo padrão de acesso ao líder já usado em `ControleJogoLocal.processaNovaVolta()` linha 442), convertida para ms reais multiplicando por `tempoCicloCircuito()`.

Antes da volta 1 do líder fechar (nenhuma `Volta` registrada), usa o fallback fixo `Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS` = 60.000ms (1 minuto) — ver "Ajuste pós-implementação: piso de 1 minuto" abaixo pra história de como chegou nesse valor (passou por duas versões anteriores).

**Alternativa considerada — estimativa fixa por circuito, sem depender de voltas reais**: mais simples e previsível, mas não reage a ritmo real de corrida (safety car mais lento, chuva deixando todo mundo mais devagar faria o "tempo médio" ficar desatualizado, encurtando/alongando artificialmente as rampas de molhado%). Rejeitada — o líder recalculando a cada volta mantém a rampa proporcional ao ritmo real da corrida.

**Descoberta na implementação**: `JogoCliente` não roda `ControleClima`/`ThreadMudancaClima` — a simulação de clima (assim como `getCicloAtual()`, que já retorna `0` fixo no client) é autoritativa no servidor, via `JogoServidor extends ControleJogoLocal`. `JogoCliente.tempoMedioVoltaMs()` foi implementado como um repasse direto do fallback do circuito, no mesmo padrão de outros métodos já stub client-side (`climaChuvoso()`/`climaLimpo()`).

**Bug encontrado e corrigido pós-implementação** (relatado pelo usuário: clima nunca mudava numa corrida iniciada em NUBLADO, mesmo com `probalidadeChuva=99`): o fallback original era `nosDaPista.size() * tempoCicloCircuito()` — a suposição de que essa era "a mesma estimativa já usada pela fórmula de velocidade real" estava **errada**. `nosDaPista` é `circuito.getPistaFull()`, a versão densa/interpolada em pixels do traçado (Bresenham entre nós, `Circuito.vetorizarPista()`) — para Interlagos, **16.829 nós**. A fórmula original tratava o carro como avançando 1 nó por ciclo, quando na real ele avança por `ganho` (tipicamente 15-40) por ciclo. Com `ciclo=160` (default de Interlagos, sem override no XML), isso dava **2.692.640ms ≈ 44,9 minutos** de "tempo médio de volta" — fazendo `ThreadMudancaClima` dormir até quase 45 minutos reais sempre que o fallback fosse usado, em vez de um valor plausível de corrida.

A correção reaproveita `Circuito.estimarTempoVoltaMs()` (Circuito.java:740-752), método que já existia no código (usado pelo editor de circuitos pra calibrar `ciclo`) e soma, por nó de `pistaFull`, `1/ganhoMédio(tipoDoNó)` ticks — usando as mesmas constantes de ganho médio documentadas ali (reta 40, curva alta 25, curva baixa 15). Pra Interlagos, isso dá **87.675ms ≈ 87,7s** — um valor real de tempo de volta. `ControleJogoLocal.calculaTempoMedioVoltaMs()` e `JogoCliente.tempoMedioVoltaMs()` foram atualizados pra chamar esse método em vez da fórmula quebrada.

**Ajuste pós-implementação: piso de 1 minuto** (pedido explícito do usuário: "o tempo mínimo de volta para o clima deve ser 1 min... a volta 1 deve ser em até 1 min"). `estimarTempoVoltaMs()` é fisicamente correto, mas ainda podia passar de 1 minuto — pra Interlagos, 87,7s. Isso significava que a primeira tentativa de mudança de clima da corrida podia demorar mais que 1 minuto real pra sequer ser avaliada. Trocado por um valor fixo, `Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS = 60_000L`, aplicado igual em `ControleJogoLocal` e `JogoCliente` — não deriva mais da geometria do circuito, garantindo `intervalo(0, tempoMedioVoltaMs) <= 60000` na primeira tentativa, sempre. O fallback já passou por três versões: `nosDaPista.size() * tempoCicloCircuito()` (quebrado, ~45min) → `circuito.estimarTempoVoltaMs()` (correto, mas podia passar de 1min) → fixo em 1min (atual).

### D2 — Rampa reversível a partir do valor atual, sem saltar para os extremos
`molhado%` guarda três campos: valor atual (0.0–1.0), direção-alvo (0.0 ou 1.0) e timestamp de início do trecho de rampa atual. A cada consulta/tick, o valor avança linearmente em direção ao alvo à taxa `1.0 / tempoMedioVoltaMs`. Se o clima categórico virar de novo (ex.: `CHUVA→NUBLADO` enquanto `molhado%` ainda está subindo), a direção-alvo inverte e a rampa passa a andar na direção contrária a partir do valor atual — sem resetar para 0 ou pular para 1 antes de inverter.

**Alternativa considerada — reiniciar do extremo oposto ao alvo**: mais simples de implementar (não precisa rastrear valor parcial + inverter direção), mas gera um salto abrupto de efeito exatamente no cenário que essa change existe para evitar. Rejeitada explicitamente pelo usuário durante a fase de exploração.

### D3 — Interpolação linear ponderada por molhado%, calculando as duas fórmulas (seca e molhada) e misturando o resultado
Cada um dos três pontos passa a calcular o valor que resultaria com `molhado% = 0` (fórmula seca de hoje) e o valor que resultaria com `molhado% = 1` (fórmula molhada de hoje), e mistura: `resultado = seco + molhado% * (molhadoTotal - seco)`. Isso preserva as fórmulas existentes como os dois extremos da interpolação, minimizando risco de regressão nos casos 0% e 100% (que devem ser bit-a-bit idênticos ao comportamento atual).

Aplicação específica por ponto:
- **`Carro.calculaModificadorPneu`**: multiplicador final de `ganho` em curva vira `ganhoSeco * (1 - molhado%) + ganhoMolhado * molhado%`, computando os dois ramos hoje mutuamente exclusivos (o bloco `if (isChovendo())` das linhas 586-588, e o bloco de bônus de pneu seco das linhas 594-618) e misturando o resultado, em vez de escolher um dos dois com base no booleano.
- **`Piloto.calculaModificadorPrincipal`**: a subtração de `comparador` (linhas 3242-3244) vira `comparador -= molhado% * (testeHabilidadePilotoAerodinamica() ? 0.2 : 0.3)` — já é uma subtração proporcional por natureza, não precisa computar dois ramos completos.
- **`Piloto.processaFreioNaReta`**: a parte numérica de `minMulti` (linha 2298) vira `minMulti -= molhado% * 0.3`. A flag `retardaFreiandoReta = false` (linha 2299) permanece atrelada a `isChovendo()` puro — é uma decisão binária de comportamento (não um valor de ganho), fora do escopo pedido de "bônus e penalidades de ganho".

**Alternativa considerada — curva não-linear (ease-in/ease-out) para a rampa**: daria uma sensação mais "natural" de pista secando/molhando, mas o usuário pediu explicitamente uma correlação direta com "% do tempo", o que aponta pra linear. Fica como possível ajuste de balanceamento futuro, não nesta change.

### D4 — Pneu de chuva forçado no pit stop de qualquer piloto quando `Clima.CHUVA` é o clima vigente, independente de molhado%
A checagem usa o clima categórico (`Clima.CHUVA.equals(clima)`), não `molhado%` — 1% de molhado já é suficiente pra forçar, porque a informação visual/categórica já avisou o jogador que está chovendo. Ponto de inserção: `ControleJogoLocal.setUpJogadorHumano` (ControleJogoLocal.java:808-821), antes da chamada a `Carro.trocarPneus`.

**Resolvido na implementação**: `JogoServidor.setUpJogadorHumano` ([JogoServidor.java:402-404](src/main/java/br/f1mane/servidor/JogoServidor.java:402)) é só `return super.setUpJogadorHumano(...)` — como `JogoServidor extends ControleJogoLocal`, o pit stop humano online passa pelo **mesmo** método que o solo, já corrigido acima. `JogoCliente.setUpJogadorHumano` ([JogoCliente.java:593-596](src/main/java/br/f1mane/servidor/applet/JogoCliente.java:593)) só encaminha a seleção do jogador pro servidor via `monitorJogo.alterarOpcoesBox(...)` — não decide o pneu, então não precisa de lógica própria. Não existe fluxo separado a corrigir.

### D5 — Garantia estatística (tendência), não teto determinístico, de que chuva nunca é mais rápida que seco
Mapeando as três fórmulas por tipo de nó, hoje **toda curva** (alta ou baixa) já é penalizada pela chuva em `calculaModificadorPneu` e `calculaModificadorPrincipal`; e frenagem-antes-de-curva-baixa é penalizada em `processaFreioNaReta`. Mas reta pura (fora de zona de frenagem) e frenagem-antes-de-curva-alta são hoje **neutras ao clima** — o cálculo de `ganho` nesses trechos independe de `isChovendo()`/`molhado%`. Como o sistema usa sorteios independentes por tick (`testeHabilidadePiloto()` e afins), mesmo nas curvas a chuva reduz a *probabilidade* da banda alta de `ganho`, não a elimina — então, por acaso, uma sequência de sorteios favoráveis na chuva pode superar uma sequência desfavorável no seco num tick ou até numa volta isolada.

Diante disso, esta change adota a garantia como **estatística/tendência**, não como invariante absoluto por tick: em expectativa (média sobre muitas amostras, mesmo pneu/asa/potência/habilidade), `ganho` em curva com `molhado% = 1.0` é menor que com `molhado% = 0.0`; reta fora de zona de frenagem e frenagem-antes-de-curva-alta permanecem sem qualquer diferença, aceitas como estão.

**Alternativa considerada — teto determinístico por tick**: calcular o `ganho` que o mesmo tick teria em seco e em molhado (usando os mesmos ou sorteios independentes) e travar o resultado final em `min(seco, molhado)` sempre que estiver chovendo. Eliminaria estruturalmente qualquer chance de a chuva vencer, inclusive nas duas zonas hoje neutras. Rejeitada nesta change — o usuário optou por manter o sistema de sorteio como está e tratar a garantia como tendência estatística, não uma trava matemática; fica registrada aqui como opção caso o comportamento observado em jogo real se mostre insatisfatório no futuro.

## Risks / Trade-offs

- **[Risco] Tempo médio de volta pode ficar artificialmente alto/baixo logo após um evento fora do padrão** (ex.: volta com passagem pelo box, volta sob safety car, primeira volta pós-largada) → **Mitigação**: nenhuma nesta change — aceito como comportamento razoável (o "tempo médio" reage ao ritmo real, incluindo essas variações; não há requisito de excluir voltas atípicas da média).
- **[Risco] Cálculo de `tempoMedioVoltaMs()` sendo chamado com frequência alta (toda leitura de `molhado%`, potencialmente a cada tick)** → **Mitigação**: cachear o valor, recalculando só quando o líder fecha uma nova volta (mesmo gatilho que já existe em `processaNovaVolta()`), não a cada tick.
- **[Risco] Duplicação de lógica entre `ControleJogoLocal` e `JogoCliente`** (mesma dor que outros métodos de `InterfaceJogo` já têm, ex. `tempoCicloCircuito()`) → **Mitigação**: nenhuma nova nesta change — segue o padrão já estabelecido no projeto de implementar cada método de `InterfaceJogo` duas vezes (local e cliente online).
- **[Trade-off] Interpolar `calculaModificadorPneu` computando as duas fórmulas completas (seca e molhada) a cada ciclo, em vez de um `if` simples** → custo de CPU adicional por ciclo por carro em curva; aceitável dado que o método já roda por ciclo por carro hoje e a fórmula em si é aritmética simples, sem I/O.

## Bug não relacionado, encontrado e corrigido durante a verificação em jogo

Ao validar a correção do fallback (abaixo) numa corrida real, o usuário reportou uma `NullPointerException` ao entrar no box, que travava/degradava a renderização em loop — o que também impedia observar mudanças de clima em corridas de poucas voltas (o jogo travava antes de chegar lá). Investigado e confirmado **sem relação com esta change**: `PainelCircuito.centralizaCarroDesenhar` ([PainelCircuito.java:2985-3007](src/main/java/br/f1mane/visao/PainelCircuito.java:2985)) só calculava `p4`/`p5` (pontos de referência das faixas de fuga 4/5) quando `!noAtual.isBox()` — um piloto sobre um nó do box com `tracadoAntigo` ainda 5 (chegou ao box vindo da faixa de fuga) disparava `p5.x` nulo no ramo `tracado==2`. Corrigido com o mesmo fallback já usado nas linhas 3002-3007 (`p4=p2`, `p5=p1`) também no ramo do box. Coberto por `PainelCircuitoCentralizaCarroBoxTest` (verificado que falha sem a correção, reproduzindo a exceção exata do log, e passa com ela).

## D6 — Reavaliação a cada volta, sem espera de N voltas sorteadas (mudança pós-implementação, pedida pelo usuário)

Descoberto durante a verificação em jogo: o gate pré-existente de `processaPossivelMudancaClima()` (`voltaMudancaClima + intervaloMudancaClima > voltaAtual`, com o intervalo inicial sorteado em `quartoVoltas + rnd()*metadeVoltas` voltas) atrasava a **primeira** tentativa de mudança de clima por, em média, metade da corrida — comportamento pré-existente, não introduzido por esta change, mas que o usuário considerou incorreto ao testar com `probalidadeChuva=99`. A pedido explícito, o gate foi removido: `processaPossivelMudancaClima()` agora tenta disparar uma nova `ThreadMudancaClima` **toda volta**, sem sorteio de intervalo — o único motivo de adiar é uma tentativa anterior ainda estar dormindo (`threadMudancaClima != null && !isProcessada()`). O campo `voltaMudancaClima` e o bloco de sorteio inicial foram removidos por ficarem mortos; `intervaloMudancaClima` continua existindo, mas só para a mensagem informativa de "quanto tempo o clima atual deve durar" em `intervaloNublado()` — não gate mais nada.

**Consequência aceita pelo usuário**: como `ThreadMudancaClima` sempre transiciona pra `NUBLADO` a partir de `SOL`/`CHUVA`, e sempre sai de `NUBLADO` (pra `CHUVA` ou `SOL`) a cada avaliação bem-sucedida, o clima agora pode variar categórico bem mais rápido que antes — potencialmente a cada volta (limitado, na prática, pelo tempo real de sleep de `ThreadMudancaClima`, que gira em torno de meia volta em média).

**REVERTIDO — ver D9.** O usuário pediu explicitamente pra voltar ao mecanismo da proposta inicial. Este D6 fica registrado como histórico da decisão (o que foi tentado, por quê, e a consequência que motivou reconsiderar), mas o código atual **não** reflete mais este D6 — reflete D9.

## D7 — Bug pós-implementação: falha em ThreadMudancaClima travava o sistema de clima pra sempre (reportado pelo usuário: "adiando toda volta")

`ThreadMudancaClima.run()` só marcava `processada = true` no fim do caminho de sucesso do `try`. Como `ControleClima.processaPossivelMudancaClima()` só cria uma thread nova quando a anterior tem `isProcessada() == true` (ver D6), qualquer exceção durante `run()` — inclusive `InterruptedException`, que já tinha aparecido num log de sessão anterior sem conexão feita na hora (`ControleClima.matarThreads()` chama `threadMudancaClima.interrupt()`) — deixava a thread presa em `isProcessada()==false` pra sempre, travando `processaPossivelMudancaClima()` em "ADIADA" pelo resto da corrida, volta após volta.

Corrigido movendo `processada = true` pro bloco `finally`, garantindo que é sempre marcado — sucesso ou falha:
```java
} finally {
    processada = true;
}
```
Coberto por `ThreadMudancaClimaDisparoTest.run_falhaComExcecao_aindaAssimMarcaProcessada` — verificado que falha sem a correção (reproduz `expected: <true> but was: <false>`) e passa com ela.

## D8 — Log porcentagem a porcentagem da rampa de molhado% (pedido do usuário)

Os logs de debug de `molhado%` (D6/D1) inicialmente só marcavam início e fim da rampa (`Rampa de molhado subindo/descendo: partindo de X rumo a Y` e uma marca de conclusão). O usuário pediu o rastro intermediário — "porcentagem a porcentagem". `ControleClima` agora guarda `ultimoPercentualMolhadoLogado` (int) e, toda vez que `getMolhado()` é consultado (uma vez por ciclo por carro em jogo real — bem mais granular que uma vez por volta) e o percentual arredondado mudou desde o último log, emite `[ControleClima] molhado=X%`, marcando `(rampa concluida)` na última linha de cada trecho. O log de início da rampa (com direção e alvo) foi mantido, já que dá contexto que o rastro percentual sozinho não dá.

## D9 — Reversão do D6: gate de voltas restaurado, mecanismo da proposta inicial

Depois de conviver com D6 (avaliação a cada volta), o usuário pediu explicitamente pra voltar ao mecanismo da proposta original — "volta a proposta inicial no código". Restaurado:

- Campo `voltaMudancaClima` (removido em D6) — volta a existir.
- `processaPossivelMudancaClima()` volta a ter o gate: `if ((voltaMudancaClima + intervaloMudancaClima) > controleJogo.getNumVoltaAtual()) { return; }`, com o bloco de sorteio do intervalo inicial (`intervaloMudancaClima == 0`) restaurado.
- A checagem de thread anterior pendente (`threadMudancaClima != null && !threadMudancaClima.isProcessada()`) continua exatamente como estava em D6 — nunca foi removida, é ortogonal ao gate de voltas.

**O que NÃO foi revertido**, porque são correções/pedidos independentes do gate de voltas em si:
- O piso de 1 minuto no fallback de `tempoMedioVoltaMs()` (D1) — continua valendo pra primeira avaliação, sempre que ela finalmente acontecer.
- O fix do `finally` em `ThreadMudancaClima` (D7) — continua evitando que uma falha isolada trave o sistema pra sempre.
- Os logs de debug (incluindo o porcentagem a porcentagem de D8) — mantidos, só ajustados pra também logar o sorteio do intervalo inicial e a volta da próxima avaliação, como estavam antes de D6.

Resultado líquido: o timing de **quando** avaliar voltou a ser exatamente o da proposta inicial (gate de voltas sorteado); o timing de **quanto dormir depois de disparado** (`[0, tempoMedioVoltaMs]`, D1/proposal original) e tudo relacionado a `molhado%`/interpolação de ganho/pneu no pit stop (D2-D5) continuam como estavam, inalterados por esta reversão.

## Migration Plan

- Sem dado persistido migrando — `molhado%` é estado em memória de `ControleClima`, recriado a cada corrida.
- Sem flag de rollout — muda o comportamento diretamente; não há branch de compatibilidade com o comportamento antigo (times de teste que dependam do salto abrupto de 0%/100% precisam ser atualizados, não mantidos em paralelo).
- Rollback é reverter o commit — não há schema ou API externa envolvida.

## Open Questions

- A média do líder deve excluir a volta em que ele passou pelo pit lane (tempo inflado pelo pit stop)? Não foi pedido explicitamente; fica como está (inclui) a menos que o usuário decida o contrário durante a implementação.
- Se a validação estatística (D5) revelar que a tendência é fraca demais em algum circuito específico (ex.: muitas retas, poucas curvas), vale revisitar o teto determinístico só pra esse caso, ou é aceitável como está? Não decidido — avaliar com dados reais durante a implementação.
