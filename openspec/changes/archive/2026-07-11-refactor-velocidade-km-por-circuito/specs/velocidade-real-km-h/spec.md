## ADDED Requirements

### Requirement: Velocidade instantânea é calculada a partir do avanço real na pista e da extensão do circuito

Quando `circuito.getDistanciaKm()` for diferente de zero, `Piloto.calculoVelocidade(double ganho)` SHALL calcular a velocidade em km/h a partir do avanço de índice por ciclo (`ganho`), do total de nós de uma volta (`controleJogo.getNosDaPista().size()`), da extensão real do circuito em metros (`circuito.getDistanciaKm()`) e da duração do ciclo de simulação (`controleJogo.tempoCicloCircuito()`), segundo a fórmula `velocidadeKmh = (ganho * distanciaKm * 3600.0) / (nosDaPista.size() * tempoCicloCircuito())`, antes de aplicar o teto descrito no requisito de teto com oscilação.

#### Scenario: Circuito com distanciaKm informado usa a fórmula real
- **WHEN** o piloto avança `ganho` unidades de índice num ciclo, num circuito com `distanciaKm` diferente de zero
- **THEN** a velocidade calculada é `(ganho * distanciaKm * 3600.0) / (nosDaPista.size() * tempoCicloCircuito())`, arredondada para inteiro, antes do teto

#### Scenario: Circuitos com extensões diferentes produzem velocidades diferentes para o mesmo avanço
- **WHEN** dois circuitos com `distanciaKm` diferentes têm o mesmo número de nós por volta, o mesmo `ciclo` e o piloto avança o mesmo `ganho` em cada um
- **THEN** a velocidade calculada é proporcional a `distanciaKm` de cada circuito, ou seja, os dois valores são diferentes entre si

### Requirement: Circuitos sem distanciaKm informado mantêm a fórmula anterior

Quando `circuito.getDistanciaKm()` for igual a zero, `Piloto.calculoVelocidade(double ganho)` SHALL continuar usando a fórmula anterior baseada em `ganho`/`ganhoMax` e no percentual de combustível, sem tentar dividir por `distanciaKm` nem por `nosDaPista.size()` pra esse propósito. O teto com oscilação SHALL valer igualmente para esse fallback.

#### Scenario: Circuito sem distanciaKm não muda de comportamento de cálculo
- **WHEN** um circuito tem `distanciaKm == 0` e o piloto avança `ganho` num ciclo
- **THEN** a velocidade calculada (antes do teto) é igual à que a fórmula existente antes desta mudança produziria para o mesmo `ganho`, percentual de combustível e situação de reta/curva

### Requirement: Velocidade nunca ultrapassa o teto e oscila ao atingi-lo

A velocidade final de um piloto (após a fórmula real ou o fallback) SHALL nunca ultrapassar `TETO_VELOCIDADE_MAX` (375 km/h). Sempre que o valor calculado alcançar ou ultrapassar `TETO_VELOCIDADE_MIN` (370 km/h), a velocidade exibida para aquele piloto naquele ciclo SHALL vir de uma oscilação que alterna gradualmente entre `TETO_VELOCIDADE_MIN` e `TETO_VELOCIDADE_MAX` (subindo até o topo, depois descendo até o piso, repetindo), em vez de permanecer fixa num único valor.

#### Scenario: Velocidade calculada abaixo do teto não é alterada
- **WHEN** a velocidade calculada (real ou fallback) é menor que `TETO_VELOCIDADE_MIN`
- **THEN** a velocidade final do piloto é exatamente o valor calculado, sem oscilação

#### Scenario: Velocidade calculada no teto ou acima passa a oscilar
- **WHEN** a velocidade calculada é maior ou igual a `TETO_VELOCIDADE_MIN` em ciclos consecutivos
- **THEN** a velocidade final do piloto varia entre `TETO_VELOCIDADE_MIN` e `TETO_VELOCIDADE_MAX` ao longo desses ciclos, subindo até `TETO_VELOCIDADE_MAX` e depois descendo até `TETO_VELOCIDADE_MIN`, sem nunca ultrapassar `TETO_VELOCIDADE_MAX` nem ficar abaixo de `TETO_VELOCIDADE_MIN`

#### Scenario: Oscilação do teto é independente por piloto
- **WHEN** dois pilotos diferentes atingem o teto de velocidade em ciclos diferentes
- **THEN** cada piloto oscila entre `TETO_VELOCIDADE_MIN` e `TETO_VELOCIDADE_MAX` de forma independente, sem compartilhar fase de oscilação com o outro piloto

### Requirement: Reta sustentada aciona uma subida artificial gradual até o teto, só no valor exibido

Quando o piloto permanecer num nó de reta/largada contínuo (`No.verificaRetaOuLargada()`) por `LIMIAR_RETA_SUSTENTADA_MS` (3000ms) ou mais, sem estar no traçado de fuga (`getTracado() == 4 || getTracado() == 5`) e sem estar na zona de frenagem (`controleJogo.isNoZonaFrenagem(noAtual)`), `Piloto.calculaVelocidadeExibir()` SHALL ignorar a velocidade real (`Piloto.velocidade`, resultado de `calculoVelocidade`) e, em vez disso, incrementar `velocidadeExibir` do ciclo anterior a cada ciclo, sujeito a um teto com oscilação (370–375) equivalente ao já descrito pra velocidade real, mas com estado de oscilação próprio. `Piloto.velocidade` (a velocidade real, consumida por `ControleJogosServer` no multiplayer e pelos efeitos visuais de chuva em `PainelCircuito`) SHALL NOT ser alterada por esse efeito — ele existe exclusivamente no valor exibido no velocímetro. O tamanho do incremento SHALL ficar mais tênue conforme `velocidadeExibir` sobe: `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2 km/h) por ciclo abaixo de `LIMIAR_VELOCIDADE_INCREMENTO_TENUE` (300 km/h); metade disso (1 km/h) por ciclo entre 300 e `LIMIAR_VELOCIDADE_INCREMENTO_MUITO_TENUE` (340 km/h); e, a partir de 340 km/h, apenas 1 km/h a cada `CICLOS_POR_INCREMENTO_MUITO_TENUE` (3) ciclos — de forma que o teto (370–375) só seja efetivamente alcançado em retas contínuas bem longas. A contagem de tempo contínuo em reta SHALL zerar assim que o nó atual deixar de ser reta/largada, ou o piloto entrar no traçado de fuga, mesmo que o nó da pista principal naquele índice continue marcado como reta — nesses casos, `calculaVelocidadeExibir()` volta a usar a suavização normal (em direção a `velocidade`) imediatamente, sem o incremento artificial. Estar na zona de frenagem SHALL apenas suspender a aplicação do incremento (voltando à suavização normal enquanto durar), sem zerar a contagem de tempo contínuo em reta — ao sair da zona de frenagem ainda na mesma reta, com a contagem já acima do limiar, o incremento artificial SHALL retomar no mesmo ciclo, sem esperar novos 3000ms.

#### Scenario: Menos de 3 segundos contínuos em reta usa a suavização normal
- **WHEN** o piloto está num nó de reta/largada há menos de 3000ms contínuos
- **THEN** `velocidadeExibir` continua sendo suavizada em direção a `velocidade` pela lógica existente, sem o incremento artificial

#### Scenario: 3 segundos contínuos em reta, fora da zona de frenagem, ativam a subida artificial no valor exibido
- **WHEN** o piloto completa 3000ms contínuos num nó de reta/largada (fora do traçado de fuga e fora da zona de frenagem), com `velocidadeExibir` abaixo de 300 km/h
- **THEN** a partir desse ciclo, `velocidadeExibir` de cada ciclo seguinte (enquanto continuar na mesma condição e abaixo de 300 km/h) é o `velocidadeExibir` do ciclo anterior mais `INCREMENTO_VELOCIDADE_RETA_SUSTENTADA` (2 km/h), ignorando `velocidade` daquele ciclo

#### Scenario: Entre 300 e 340 km/h, o incremento cai pela metade
- **WHEN** a reta sustentada está ativa e `velocidadeExibir` está entre `LIMIAR_VELOCIDADE_INCREMENTO_TENUE` (300) e `LIMIAR_VELOCIDADE_INCREMENTO_MUITO_TENUE` (340) km/h
- **THEN** cada ciclo incrementa `velocidadeExibir` em apenas 1 km/h, em vez dos 2 km/h usados abaixo de 300

#### Scenario: Acima de 340 km/h, o incremento fica muito tênue
- **WHEN** a reta sustentada está ativa e `velocidadeExibir` está em ou acima de `LIMIAR_VELOCIDADE_INCREMENTO_MUITO_TENUE` (340) km/h
- **THEN** `velocidadeExibir` só sobe 1 km/h a cada `CICLOS_POR_INCREMENTO_MUITO_TENUE` (3) ciclos consecutivos nessa condição, permanecendo igual nos demais ciclos, de forma que alcançar o teto (370–375) a partir daí exige uma reta contínua consideravelmente mais longa

#### Scenario: Zona de frenagem desativa o incremento artificial, mesmo com a contagem acima do limiar
- **WHEN** o piloto está num nó marcado como zona de frenagem (`controleJogo.isNoZonaFrenagem`), mesmo que a contagem de tempo contínuo em reta já esteja acima de 3000ms
- **THEN** `velocidadeExibir` continua sendo suavizada em direção a `velocidade` pela lógica existente naquele ciclo, sem o incremento artificial

#### Scenario: Sair da zona de frenagem reativa o incremento artificial sem reiniciar a contagem
- **WHEN** o piloto sai da zona de frenagem ainda num nó de reta/largada contínuo, com a contagem de tempo contínuo em reta já acima de 3000ms
- **THEN** o incremento artificial em `velocidadeExibir` retoma no mesmo ciclo, sem precisar acumular novos 3000ms

#### Scenario: Velocidade real nunca é alterada pelo efeito de reta sustentada
- **WHEN** o piloto está em reta sustentada havendo o incremento artificial ativo em `velocidadeExibir`
- **THEN** `Piloto.velocidade` (e, portanto, `ControleJogosServer.dadosParciais.velocidade` e a intensidade dos efeitos de chuva em `PainelCircuito`) continua sendo só o resultado de `calculoVelocidade`, sem nenhuma influência do incremento artificial

#### Scenario: Sair da reta interrompe a subida artificial imediatamente
- **WHEN** o piloto estava em reta sustentada (subida artificial ativa em `velocidadeExibir`) e o nó atual muda para curva alta ou curva baixa
- **THEN** no ciclo seguinte `velocidadeExibir` volta a ser suavizado em direção a `velocidade` pela lógica existente, sem continuar o incremento artificial, e a contagem de tempo contínuo em reta reinicia do zero

#### Scenario: Traçado de fuga nunca ativa a reta sustentada
- **WHEN** o piloto está no traçado de fuga (`getTracado()` 4 ou 5), mesmo que o nó da pista principal naquele índice seja marcado como reta
- **THEN** a contagem de tempo contínuo em reta nunca avança, e a subida artificial nunca é ativada em `velocidadeExibir` enquanto o piloto permanecer no traçado de fuga

### Requirement: Escala de cor do velocímetro usa a mesma referência de teto

`PainelCircuito.desenhaVelocidade()` SHALL usar `TETO_VELOCIDADE_MAX` (em vez do valor fixo `330` hoje usado) como referência de 100% na escala de cor do velocímetro (`OcilaCor.porcentVermelho100Verde0`).

#### Scenario: Velocímetro fica na cor mais "quente" exatamente no teto
- **WHEN** a velocidade exibida de um piloto é igual a `TETO_VELOCIDADE_MAX`
- **THEN** a escala de cor do velocímetro corresponde a 100%, igual ao que ocorria antes com o valor fixo `330` quando a velocidade chegava a 330
