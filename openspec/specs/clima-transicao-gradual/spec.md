# Spec: clima-transicao-gradual

## Purpose

Regras que governam a transição gradual de clima entre condição seca e chuvosa: o timing de disparo das tentativas de mudança de clima (baseado no tempo médio de volta do líder, em vez de um delay fixo de narrativa), a rampa reversível de "molhado%" que substitui o salto abrupto de 0% a 100% de efeito de chuva, a interpolação das fórmulas de `ganho` afetadas por essa rampa, e a obrigatoriedade de pneu de chuva no pit stop — para qualquer piloto, humano incluso — enquanto o clima vigente for chuva.

## Requirements

### Requirement: Tempo médio de volta acessível ao controle de clima
`ControleClima` SHALL ter acesso a um tempo médio de volta em milissegundos reais (`tempoMedioVoltaMs`), calculado como a média de `Volta.obterTempoVolta()` (convertido para ms via `tempoCicloCircuito()`) das voltas já registradas do piloto líder (primeira posição). Antes da primeira volta do líder ser registrada, o sistema SHALL usar como fallback um valor fixo de 1 minuto (`Global.TEMPO_MEDIO_VOLTA_CLIMA_MINIMO_MS`), garantindo que a primeira tentativa de mudança de clima da corrida dispare em até 1 minuto real — SHALL NOT derivar esse fallback da geometria ou da estimativa de tempo de volta do circuito, que pode ultrapassar 1 minuto.

#### Scenario: Tempo médio de volta calculado a partir das voltas do líder
- **WHEN** o piloto líder já completou uma ou mais voltas na corrida
- **THEN** `tempoMedioVoltaMs` é a média das durações reais dessas voltas, em milissegundos

#### Scenario: Fallback antes da primeira volta do líder
- **WHEN** nenhuma volta do piloto líder foi registrada ainda (início da corrida)
- **THEN** `tempoMedioVoltaMs` retorna o valor fixo de 1 minuto (60000ms), independente do circuito

### Requirement: Avaliação de mudança de clima gatilhada por intervalo de voltas sorteado
`ControleClima.processaPossivelMudancaClima()` é chamada a cada volta nova do líder, mas SHALL NOT tentar disparar uma nova `ThreadMudancaClima` em toda chamada — SHALL só disparar quando o número de voltas sorteado (`intervaloMudancaClima`, sorteado em `quartoVoltas + rnd()·metadeVoltas` na primeira avaliação da corrida, e depois por `intervaloNublado()`/`intervaloSol()`/`intervaloChuva()` a cada transição efetivada) tiver se passado desde a última avaliação. Enquanto esse intervalo não se cumpre, a chamada SHALL retornar sem nenhum efeito. Uma tentativa anterior ainda em andamento (thread ainda dormindo, sem ter concluído seu processamento) SHALL continuar adiando a próxima tentativa, mesmo que o intervalo de voltas já tenha se cumprido.

#### Scenario: Sorteio do intervalo inicial na primeira avaliação da corrida
- **WHEN** a corrida começa e `processaPossivelMudancaClima()` é chamada pela primeira vez
- **THEN** um intervalo em voltas é sorteado (`quartoVoltas + rnd()·metadeVoltas`) e nenhuma `ThreadMudancaClima` é disparada nessa chamada

#### Scenario: Tentativa adiada enquanto o intervalo sorteado não se cumpre
- **WHEN** o líder completa uma volta e o número de voltas desde a última avaliação ainda é menor que `intervaloMudancaClima`
- **THEN** `processaPossivelMudancaClima()` retorna sem disparar nenhuma thread nem sortear nada novo

#### Scenario: Disparo quando o intervalo sorteado se cumpre e não há thread pendente
- **WHEN** o líder completa uma volta, o intervalo sorteado já se cumpriu, e não há `ThreadMudancaClima` anterior pendente
- **THEN** uma nova `ThreadMudancaClima` é criada e iniciada nessa volta

#### Scenario: Tentativa adiada por thread anterior pendente, mesmo com intervalo cumprido
- **WHEN** o intervalo sorteado já se cumpriu, mas a `ThreadMudancaClima` da tentativa anterior ainda não terminou de processar
- **THEN** nenhuma thread nova é criada nessa volta — a próxima tentativa só acontece quando a thread anterior concluir

### Requirement: Disparo da mudança de clima em intervalo aleatório dentro do tempo médio de volta
Uma vez disparada, a transição (`ThreadMudancaClima`) SHALL dormir um atraso aleatório uniformemente distribuído entre 0 e `tempoMedioVoltaMs` antes de efetivar a mudança de clima, substituindo o intervalo fixo de narrativa (3-15 segundos reais) usado anteriormente.

#### Scenario: Atraso de disparo dentro da janela do tempo médio de volta
- **WHEN** `ControleClima` dispara uma nova tentativa de mudança de clima
- **THEN** a thread responsável pela transição dorme um valor aleatório entre 0 e `tempoMedioVoltaMs` antes de efetivar a mudança e notificar os jogadores

### Requirement: Falha ao processar uma tentativa não trava avaliações futuras
Se `ThreadMudancaClima` lançar qualquer exceção durante seu processamento (incluindo interrupção do sleep), o sistema SHALL ainda assim marcar essa tentativa como concluída, permitindo que uma nova tentativa seja disparada na próxima volta — SHALL NOT deixar o sistema de clima permanentemente travado em "aguardando" pelo resto da corrida por causa de uma única falha.

#### Scenario: Exceção durante o processamento libera tentativas futuras
- **WHEN** `ThreadMudancaClima.run()` lança uma exceção (ex.: `InterruptedException`) antes de concluir a decisão de clima
- **THEN** a thread ainda assim é marcada como processada, e a próxima volta consegue disparar uma nova tentativa normalmente

### Requirement: Rampa reversível de "molhado%" entre condição seca e chuvosa
`ControleClima` SHALL manter um valor contínuo "molhado%" (0.0 a 1.0), independente do clima categórico exibido (`SOL`/`NUBLADO`/`CHUVA`). Quando o clima categórico transiciona de `NUBLADO` para `CHUVA`, "molhado%" SHALL subir linearmente de seu valor atual até 1.0 ao longo de aproximadamente um `tempoMedioVoltaMs`. Quando o clima categórico transiciona de `CHUVA` para `NUBLADO`, "molhado%" SHALL descer linearmente de seu valor atual até 0.0 ao longo de aproximadamente um `tempoMedioVoltaMs`. Transições entre `SOL` e `NUBLADO` sem passar por `CHUVA` SHALL NOT alterar "molhado%". Se o clima categórico mudar de direção (seco↔chuva) enquanto uma rampa está em andamento, "molhado%" SHALL inverter sua direção-alvo a partir do valor atual, sem saltar para 0.0 ou 1.0 antes de inverter.

#### Scenario: Chuva começando sobe "molhado%" gradualmente
- **WHEN** o clima categórico muda de `NUBLADO` para `CHUVA`
- **THEN** "molhado%" começa a subir linearmente do valor atual até 1.0, levando aproximadamente `tempoMedioVoltaMs` para atingir 1.0 caso não haja nova mudança de clima nesse intervalo

#### Scenario: Chuva parando desce "molhado%" gradualmente
- **WHEN** o clima categórico muda de `CHUVA` para `NUBLADO`
- **THEN** "molhado%" começa a descer linearmente do valor atual até 0.0, levando aproximadamente `tempoMedioVoltaMs` para atingir 0.0 caso não haja nova mudança de clima nesse intervalo

#### Scenario: Transição sol/nublado isolada não afeta "molhado%"
- **WHEN** o clima categórico muda entre `SOL` e `NUBLADO` sem que `CHUVA` esteja envolvido na transição
- **THEN** "molhado%" permanece inalterado

#### Scenario: Reversão de rampa em andamento inverte a partir do valor atual
- **WHEN** "molhado%" está subindo em direção a 1.0 e ainda não chegou lá, e o clima categórico muda de volta de `CHUVA` para `NUBLADO` antes da rampa completar
- **THEN** "molhado%" passa a descer em direção a 0.0 a partir do valor que tinha no momento da reversão, sem saltar para 1.0 primeiro

### Requirement: Fórmulas de ganho interpoladas por "molhado%"
Os três pontos de cálculo de `ganho` que hoje leem o clima como binário (`Carro.calculaModificadorPneu`, `Piloto.calculaModificadorPrincipal`, `Piloto.processaFreioNaReta`) SHALL usar "molhado%" para interpolar linearmente entre o resultado correspondente à condição seca (equivalente a "molhado%" = 0.0, comportamento hoje aplicado quando `isChovendo()` é falso) e o resultado correspondente à condição de chuva plena (equivalente a "molhado%" = 1.0, comportamento hoje aplicado quando `isChovendo()` é verdadeiro), em vez de escolher um dos dois extremos com base num valor booleano.

#### Scenario: Molhado% em 0% reproduz o comportamento seco de hoje
- **WHEN** "molhado%" é 0.0
- **THEN** os três cálculos de `ganho` produzem exatamente o mesmo resultado que a fórmula de condição seca de hoje

#### Scenario: Molhado% em 100% reproduz o comportamento de chuva de hoje
- **WHEN** "molhado%" é 1.0
- **THEN** os três cálculos de `ganho` produzem exatamente o mesmo resultado que a fórmula de condição de chuva de hoje

#### Scenario: Molhado% intermediário produz penalidade proporcional
- **WHEN** "molhado%" está em um valor entre 0.0 e 1.0 (ex.: 0.4)
- **THEN** cada um dos três cálculos de `ganho` produz um resultado entre o valor seco e o valor de chuva plena, proporcional a "molhado%"

### Requirement: Ajustes de asa permanecem binários, fora da rampa de molhado%
`Carro.calculaModificadorAsa` SHALL continuar aplicando seus bônus/penalidades com base exclusivamente no clima categórico atual (`isChovendo()`/estado do teste de freios), sem interpolação por "molhado%". Esse comportamento SHALL permanecer idêntico ao existente antes desta change.

#### Scenario: Bônus de asa não muda gradualmente com molhado%
- **WHEN** "molhado%" está em qualquer valor intermediário entre 0.0 e 1.0
- **THEN** `Carro.calculaModificadorAsa` aplica o mesmo resultado que aplicaria hoje, baseado apenas no clima categórico atual, sem levar "molhado%" em consideração

### Requirement: Pneu de chuva obrigatório no pit stop durante chuva, para qualquer piloto
Quando um piloto — humano ou controlado por IA — entra no box enquanto o clima categórico vigente é `CHUVA` (em qualquer valor de "molhado%", incluindo valores próximos de 0.0 logo após o início da rampa), o sistema SHALL definir o pneu do carro como pneu de chuva, ignorando qualquer escolha de pneu diferente feita ou configurada para esse pit stop.

#### Scenario: Jogador humano tenta escolher pneu seco durante chuva
- **WHEN** o jogador humano entra no box com o clima categórico vigente `CHUVA` (mesmo que "molhado%" ainda esteja subindo) e seleciona um pneu que não é de chuva
- **THEN** o carro sai do box com pneu de chuva, independentemente da seleção feita

#### Scenario: Pit stop fora de chuva mantém escolha livre
- **WHEN** um piloto entra no box e o clima categórico vigente não é `CHUVA`
- **THEN** a escolha de pneu do pit stop não é sobrescrita por essa regra

#### Scenario: Escolha volta a ser livre assim que a chuva para
- **WHEN** o clima categórico deixa de ser `CHUVA` (transição para `NUBLADO`), independentemente do valor de "molhado%" em queda
- **THEN** pit stops seguintes não forçam mais pneu de chuva por causa desta regra — a escolha de pneu volta a ser livre

### Requirement: Chuva não pode, em expectativa, produzir ganho maior que condição seca em curvas
Para qualquer nó classificado como curva alta ou curva baixa, com pneu, asa, potência e habilidade do piloto equivalentes, o valor esperado (média sobre muitas amostras) de `ganho` calculado com "molhado%" = 1.0 SHALL ser menor que o valor esperado de `ganho` calculado com "molhado%" = 0.0. Essa é uma garantia estatística/de expectativa, não uma garantia por tick individual — sorteios probabilísticos isolados (`testeHabilidadePiloto()` e testes correlatos) PODEM, em casos pontuais, produzir um `ganho` de chuva numericamente maior que uma amostra específica de `ganho` seco no mesmo nó, sem que isso viole este requisito. Nós classificados como reta fora de zona de frenagem, e zonas de frenagem cuja próxima curva é curva alta, SHALL continuar sem qualquer diferença de `ganho` entre seco e chuva, como já ocorre hoje — este requisito não exige fechar essas duas lacunas.

#### Scenario: Média de ganho em curva cai com molhado% crescente
- **WHEN** muitas amostras de `ganho` são coletadas no mesmo nó de curva (alta ou baixa), mantendo pneu, asa, potência e habilidade constantes, variando apenas "molhado%" de 0.0 para 1.0
- **THEN** a média das amostras em "molhado%" = 1.0 é menor que a média das amostras em "molhado%" = 0.0

#### Scenario: Reta fora de zona de frenagem permanece sem diferença entre seco e chuva
- **WHEN** o piloto está em um nó de reta fora de qualquer zona de frenagem, com qualquer valor de "molhado%"
- **THEN** o cálculo de `ganho` não depende de "molhado%" nem do clima categórico, igual ao comportamento anterior a esta change

#### Scenario: Frenagem antes de curva alta permanece sem diferença entre seco e chuva
- **WHEN** o piloto está numa zona de frenagem cuja próxima curva é curva alta, com qualquer valor de "molhado%"
- **THEN** `processaFreioNaReta` não aplica nenhuma redução baseada em "molhado%", igual ao comportamento anterior a esta change

#### Scenario: Amostra isolada mais rápida na chuva não viola a garantia
- **WHEN**, num tick isolado de curva, o `ganho` sorteado com "molhado%" = 1.0 é numericamente maior que o `ganho` sorteado com "molhado%" = 0.0 no mesmo nó, por efeito de sorteios probabilísticos independentes
- **THEN** essa amostra isolada não constitui violação deste requisito — a garantia é definida sobre o valor esperado, não sobre toda amostra individual
