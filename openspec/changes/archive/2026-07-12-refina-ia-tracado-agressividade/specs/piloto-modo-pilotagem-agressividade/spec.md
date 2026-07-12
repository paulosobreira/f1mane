## ADDED Requirements

### Requirement: Agressividade efetiva sob stress alto
Quando o `modoPilotagem` armazenado do piloto for `AGRESSIVO` e `stress > 95`, o sistema SHALL tratar toda leitura de efeito de jogo (ganho em curva/reta, geração/escala de estresse, chance de acidente, freada mal-sucedida, e qualquer outro cálculo de gameplay que hoje compara `AGRESSIVO.equals(getModoPilotagem())`) como se o modo fosse `NORMAL`, através de um ponto único de leitura (`getModoPilotagemEfetivo()`). O valor armazenado em `modoPilotagem` SHALL NOT ser alterado por essa regra — o piloto continua exibido como `AGRESSIVO` em qualquer painel/HUD que leia `getModoPilotagem()` diretamente.

#### Scenario: Ganho em curva de piloto AGRESSIVO com stress acima de 95 usa a fórmula de NORMAL
- **WHEN** um piloto está com `modoPilotagem == AGRESSIVO`, `stress > 95`, e passa por um cálculo de ganho que hoje diferencia AGRESSIVO de NORMAL
- **THEN** o cálculo usa a fórmula/valor correspondente a NORMAL, mesmo com o campo `modoPilotagem` permanecendo `AGRESSIVO`

#### Scenario: Painel de status continua mostrando AGRESSIVO
- **WHEN** um piloto está com `modoPilotagem == AGRESSIVO` e `stress > 95`
- **THEN** qualquer leitura de `getModoPilotagem()` (painéis, HUD, mensagens) retorna `AGRESSIVO`, sem indicação visual de que os efeitos de jogo estão sendo tratados como NORMAL

#### Scenario: Stress volta a 95 ou abaixo restaura os efeitos de AGRESSIVO
- **WHEN** o `stress` de um piloto `AGRESSIVO` cai para 95 ou menos após ter estado acima de 95
- **THEN** os cálculos de gameplay voltam a usar a fórmula de AGRESSIVO no ciclo seguinte, sem necessidade de o piloto trocar de modo manualmente

#### Scenario: Regra não se aplica a NORMAL nem LENTO
- **WHEN** o `modoPilotagem` armazenado é `NORMAL` ou `LENTO`, independente do valor de `stress`
- **THEN** `getModoPilotagemEfetivo()` retorna o mesmo valor de `getModoPilotagem()`, sem nenhuma substituição

#### Scenario: Stress atingindo 99 não muta mais o modo armazenado pra NORMAL
- **WHEN** o `stress` de um piloto `AGRESSIVO` atinge 99 ou mais
- **THEN** `modoPilotagem` continua armazenado como `AGRESSIVO` (o antigo comportamento de mudar automaticamente para `NORMAL` nesse patamar foi removido, substituído pela leitura efetiva desta spec a partir de `stress > 95`)

A proteção do modo de pilotagem do jogador humano manual — incluindo a confirmação de que bandeirada, colisão contra o carro da frente, escapada e desvio de retardatário continuam forçando `modoPilotagem`/`giro` mesmo em modo manual — é regida pela spec `piloto-controle-automatico-manual`, que centraliza todo o escopo da chave automático/manual.

### Requirement: Decisão automática da IA recua de AGRESSIVO acima de 95% de stress, via teste de habilidade, sem afetar o jogador humano
Em `Piloto.modoIADefesaAtaque()` (decisão automática de agressividade, escopo da IA — bot ou piloto automático dirigindo o carro do jogador humano, nunca o jogador humano decidindo manualmente), quando as condições normais indicariam a escolha de `AGRESSIVO` E `stress > 95`, o sistema SHALL dar à decisão automática uma chance de recuar proativamente para `NORMAL` através de `testeHabilidadePilotoCarro()`: em caso de sucesso, a decisão automática escolhe `NORMAL` em vez de `AGRESSIVO` nesse ciclo; em caso de falha, a decisão automática ainda pode escolher `AGRESSIVO` normalmente, apesar do stress alto. Esse teste SHALL NOT ser avaliado (nem consumir RNG) quando `stress <= 95` ou quando as condições normais já não indicariam `AGRESSIVO`. Este requisito SHALL NOT alterar em nada a capacidade do jogador humano de selecionar e manter `AGRESSIVO` manualmente (local ou via API multiplayer) a qualquer nível de stress, sem teste e sem efeito colateral além da ausência de ganho já definida no requisito de agressividade efetiva.

#### Scenario: Decisão automática recua pra NORMAL quando o teste de habilidade é bem-sucedido
- **WHEN** a decisão automática de agressividade (`modoIADefesaAtaque()`) resultaria em `AGRESSIVO` (pneus OK, `stress < 100`), `stress > 95`, e `testeHabilidadePilotoCarro()` é bem-sucedido
- **THEN** a decisão automática escolhe `NORMAL` nesse ciclo, em vez de `AGRESSIVO`

#### Scenario: Decisão automática permanece AGRESSIVO quando o teste de habilidade falha
- **WHEN** a decisão automática de agressividade resultaria em `AGRESSIVO`, `stress > 95`, e `testeHabilidadePilotoCarro()` falha
- **THEN** a decisão automática ainda escolhe `AGRESSIVO` nesse ciclo, apesar do stress alto — a regra desta spec é uma chance de recuo, não uma garantia

#### Scenario: Teste não é avaliado com stress dentro do limite
- **WHEN** a decisão automática de agressividade resultaria em `AGRESSIVO` e `stress <= 95`
- **THEN** nenhum teste de habilidade adicional é avaliado por causa desta spec, e a decisão automática escolhe `AGRESSIVO` normalmente, sem consumir RNG extra

#### Scenario: Jogador humano continua escolhendo AGRESSIVO manualmente sem nenhum teste
- **WHEN** o jogador humano seleciona `AGRESSIVO` manualmente (local ou via API multiplayer), com `stress` a qualquer nível, incluindo acima de 95%
- **THEN** `modoPilotagem` é definido como `AGRESSIVO` imediatamente, sem nenhum teste de habilidade nem chance de recuo — esta spec rege exclusivamente a decisão automática da IA
