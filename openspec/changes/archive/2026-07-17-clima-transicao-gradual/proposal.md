## Why

Hoje o clima muda de forma abrupta: no exato ciclo em que `ControleClima` decide a nova condição, todos os bônus/penalidades de `ganho` ligados a chuva e a compatibilidade de pneu (em `Carro.calculaModificadorPneu`, `Piloto.calculaModificadorPrincipal` e `Piloto.processaFreioNaReta`) saltam de 0% a 100% de efeito num único tick. Isso não reflete como a pista "molha" ou "seca" de verdade e tira o valor estratégico de reagir à mudança — não há janela de reação, é ou 100% seco ou 100% chuva. Além disso, o disparo da mudança de clima usa hoje um delay fixo de narrativa (3–15s reais, `ThreadMudancaClima`) desconectado do ritmo da corrida, e o jogador humano pode entrar no box durante chuva e escolher pneu seco livremente, sem nenhuma penalização de decisão — diferente da IA, que já sempre calça o pneu certo.

## What Changes

- `ControleClima.processaPossivelMudancaClima()` continua avaliada a cada volta nova do líder, mas só dispara uma nova `ThreadMudancaClima` quando o intervalo de voltas sorteado (`quartoVoltas + rnd()·metadeVoltas` na primeira avaliação, depois recalculado a cada transição) se cumpre — mecanismo pré-existente, mantido (chegou a ser removido a pedido do usuário durante teste em jogo real, depois revertido de volta a pedido dele mesmo; ver design.md D6/D9).
- Uma vez disparada, `ThreadMudancaClima` dorme um atraso aleatório dentro de `[0, tempoMedioVoltaMs]` antes de efetivar a mudança de clima, em vez do intervalo fixo de narrativa de 3–15s de antes.
- `ThreadMudancaClima` passa a marcar sua tentativa como processada num bloco `finally`, não só no caminho de sucesso — uma falha isolada (ex.: interrupção do sleep) não trava mais o sistema de clima em "adiado" pelo resto da corrida.
- `ControleClima` ganha acesso a um tempo médio de volta (`tempoMedioVoltaMs`), calculado a partir das voltas já registradas do líder, com fallback fixo de 1 minuto antes da primeira volta do líder fechar — garante que a primeira tentativa de mudança de clima da corrida dispare em até 1 minuto real.
- `ControleClima` passa a manter um novo estado contínuo — "molhado%" (0.0 a 1.0) — que sobe de 0% a 100% ao longo de ~1 tempo médio de volta quando a chuva começa (transição NUBLADO→CHUVA), e desce de 100% a 0% quando a chuva para (transição CHUVA→NUBLADO). Transições SOL↔NUBLADO isoladas não afetam esse valor.
- Se o clima mudar de novo com uma rampa em andamento, ela inverte de direção a partir do valor atual, sem saltar para os extremos.
- As três fórmulas de `ganho` que hoje leem `isChovendo()` como binário passam a interpolar linearmente entre o resultado "100% seco" e o "100% molhado" de hoje, ponderado por "molhado%": `Carro.calculaModificadorPneu`, `Piloto.calculaModificadorPrincipal` (redução de `comparador`) e `Piloto.processaFreioNaReta` (redução numérica de `minMulti`).
- Qualquer piloto — humano incluso — que entrar no box enquanto `Clima.CHUVA` for o clima vigente (em qualquer ponto da rampa, de 1% a 100%) SHALL ser colocado em pneu de chuva, ignorando a escolha manual de pneu feita no pit stop.
- **BREAKING** (comportamento, não API): o jogador humano deixa de poder escolher pneu seco durante chuva no pit stop — hoje essa escolha é livre.

## Capabilities

### New Capabilities
- `clima-transicao-gradual`: tempo médio de volta acessível ao controle de clima, timing de disparo da mudança de clima baseado nesse tempo médio, rampa reversível de "molhado%" entre seco e chuva, interpolação das três fórmulas de ganho afetadas por essa rampa, e obrigatoriedade de pneu de chuva no pit stop (qualquer piloto) enquanto o clima vigente for chuva.

### Modified Capabilities
<!-- Nenhuma capability existente tem requisitos de spec sobre timing de clima, rampa de efeitos ou seleção de pneu no pit stop — piloto-controle-automatico-manual cobre explicitamente apenas giro/ERS/DRS/ataque-defesa/traçado, não seleção de pneu. Território novo, sem overlap de requisitos. -->

## Impact

- **Código afetado**: `ControleClima.java`, `ThreadMudancaClima.java`, `Carro.java` (`calculaModificadorPneu`), `Piloto.java` (`calculaModificadorPrincipal`, `processaFreioNaReta`), `ControleJogoLocal.java` (`setUpJogadorHumano`, novo método de tempo médio de volta), `InterfaceJogo.java` (novo método na interface), `JogoCliente.java` (implementação equivalente para multiplayer), `Global.java` (nova constante `TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS`).
- **A investigar durante a implementação**: o fluxo de pit stop do jogador humano no multiplayer/servidor (ainda não localizado — só o fluxo local solo, via `ControleJogoLocal.setUpJogadorHumano`, foi confirmado) precisa do mesmo travamento de pneu de chuva. **Resolvido**: `JogoServidor.setUpJogadorHumano` só repassa pro método já corrigido; não existe fluxo separado.
- **Fora de escopo, sem mudança**: `Carro.calculaModificadorAsa` (bônus/penalidade de asa continua binário, só 100% seco ou 100% chuva), `Carro.verificaPneusIncompativeisClima()`/`Carro.testeFreios()` (usados por stress, risco de acidente do jogador humano, escapada e box automático da IA — nenhum desses sistemas ganha rampa), e o comportamento de forçar pneu seco ao sair da chuva (não pedido — a escolha volta a ser livre assim que a chuva para).
- **Bug não relacionado, corrigido durante a verificação**: `PainelCircuito.centralizaCarroDesenhar` lançava `NullPointerException` ao renderizar um carro no box vindo da faixa de fuga (traçado 5) — pré-existente, sem relação com clima, mas bloqueava a verificação em jogo real (ver design.md).
