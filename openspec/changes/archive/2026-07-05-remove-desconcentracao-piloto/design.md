## Context

Levantamento completo do mecanismo atual (ver proposal.md para motivação):

**Campos**: `ciclosDesconcentrado` (contador regressivo), `desconcentradoAgressivoNesteTick` (flag consumida por `processaStress()`).

**Gatilhos** (o que liga `ciclosDesconcentrado`):
| Local | Evento | Magnitude atual |
|---|---|---|
| `processaEscapadaDaPista()` | escapa da pista sob estresse alto (`> Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA`) em modo AGRESSIVO | 5 ciclos (habilidoso) / 7 (não) |
| `desviaPilotoNaFrente()` | cede passagem a carro mais rápido atrás | 5 ciclos |
| `mensagemRetardatario()` | é ultrapassado / mensagem de retardatário | 10 ciclos |
| `ControleQualificacao` | largada ruim (5% de chance, piloto não-humano sem habilidade) | 500-700 ciclos (dominante — dura praticamente a corrida toda) |

**Bloqueios** (onde `verificaDesconcentrado()` é consultado):
| Método | O que trava |
|---|---|
| `testeHabilidadePiloto()` | força TODO teste de habilidade a falhar — usado em quase todo cálculo de ganho, ultrapassagem, troca de traçado, DRS/ERS |
| `processaIAnovoIndex()` | trava toda a tomada de decisão de IA do tick |
| `iaTentaUsarDRS()` / `iaTentaUsarErs()` | trava tentativa de DRS/ERS (redundante com o item acima) |
| `tentarEscaparFilaIndiana()` | trava a lógica de escapar de fila indiana |
| `mudarTracado()` | trava saída das faixas de escape (traçado 4/5) de volta à pista |
| `processaMudancaRegime()` | força AGRESSIVO→NORMAL e giro máximo→normal, pula mensagens de modo |

**Efeito colateral já existente**: `decrementaPilotoDesconcentrado()`, além de decrementar o contador, já soma +1 de estresse por tick quando AGRESSIVO + estresse<70 + desconcentrado (`processaStressDesconcentradoAgressivo`, adicionado na consolidação anterior de `piloto-gestao-estresse`).

## Goals / Non-Goals

**Goals:**
- Nenhum comportamento do piloto (teste de habilidade, decisão de IA, DRS/ERS, escape de fila, troca de traçado, modo de pilotagem/giro do motor) fica bloqueado ou forçado por um estado invisível ao jogador nem por um limiar de estresse substituto.
- Os 4 eventos que hoje geram desconcentração continuam tendo *algum* efeito de jogo (não viram no-ops) — canalizado inteiramente pelo sistema de estresse já centralizado.

**Non-Goals:**
- Não recriar um novo estado oculto equivalente (ex.: um novo contador/flag escondido) — se a conversão para estresse não reproduzir um efeito minimamente parecido, a decisão do usuário é remover mesmo, sem substituto.
- Não mudar a mensagem de "problema na largada" em si, só o momento em que ela dispara.
- Não perseguir paridade numérica exata com o mecanismo antigo — os valores abaixo são pontos de partida para observação em simulação, não um requisito de equivalência.

## Decisions

### 1. Remover os 6 gates de `verificaDesconcentrado()` sem substituto

`testeHabilidadePiloto()`, `processaIAnovoIndex()`, `iaTentaUsarDRS()`, `iaTentaUsarErs()`, `tentarEscaparFilaIndiana()`, `mudarTracado()` — a chamada `verificaDesconcentrado()` (e o `||`/`&&` que a acopla) é removida da condição de cada um, sem qualquer verificação equivalente no lugar. Alternativa descartada: trocar por uma checagem de estresse alto (ex.: `getStress() > 90`) — rejeitada porque o pedido explícito foi "não quero que ele deixe de fazer coisas que faria", ou seja, mesmo um bloqueio condicionado a estresse alto ainda seria um bloqueio; o pedido é para *nunca* mais bloquear essas ações, e deixar o efeito por conta da penalidade natural que o próprio estresse já aplica em outros lugares (ex.: `incStress`/`decStress`, testes de habilidade que já rolam dado normalmente).

### 2. `processaMudancaRegime()`: removido sem substituto (revisado)

Decisão original (agora revertida): trocar o gate por `if (getStress() >= 90)`, mantendo o downgrade forçado de modo (AGRESSIVO→NORMAL) e giro (máximo→normal). O usuário não gostou dessa "penalidade substituta" depois de ver em uso — o downgrade forçado por estresse alto ainda é um bloqueio de comportamento (impede o piloto de permanecer agressivo/giro máximo mesmo que ele "queira"), o mesmo tipo de coisa que a Decisão 1 já havia rejeitado para os outros 6 gates. **Correção**: a condição de estresse foi removida por completo, sem nenhum substituto — `processaMudancaRegime()` agora só trata a bandeirada (que continua forçando LENTO/giro mínimo, isso nunca foi contestado) e roda as mensagens de modo incondicionalmente. Isso alinha esta decisão com a Decisão 1: nenhum dos 7 bloqueios originais tem substituto — o efeito de estresse alto fica só nas consequências que o próprio sistema de estresse já aplica em outros lugares (ex.: `incStress`'s caps).

### 3. Converter os 4 gatilhos em incremento de estresse, reduzido pela metade e ainda mais se o piloto passar no teste de habilidade

Como o pedido foi "tentar por estresse, se não fizer diferença, remove", cada `setCiclosDesconcentrado(N)` vira `incStress(M)`. Refinamento pedido pelo usuário: `M` é a metade do valor inicialmente proposto, e reduzido à metade de novo se `testeHabilidadePiloto()` for bem-sucedido — mesmo padrão de ternário já usado em outros gatilhos de estresse no código (`testeHabilidadePiloto() ? valor/2 : valor`). Fórmula única para os 4:

```java
int base = <valor cheio, sem habilidade> / 2;
incStress(testeHabilidadePiloto() ? base / 2 : base);
```

| Local | Valor cheio (antes de qualquer redução) | `base` (metade) | Final (sem habilidade / com habilidade) |
|---|---|---|---|
| `processaEscapadaDaPista()` | 7 | 3 | 3 / 1 |
| `desviaPilotoNaFrente()` | 5 | 2 | 2 / 1 |
| `mensagemRetardatario()` | 10 | 5 | 5 / 2 |
| `ControleQualificacao` (largada ruim) | intervalo(30,40) | intervalo(15,20) | intervalo(15,20) / intervalo(15,20)/2 |

**Nota sobre `processaEscapadaDaPista()`** (confirmado com o usuário): o design original diferenciava 5 (habilidoso) / 7 (não habilidoso) diretamente, sem uma "metade" definida. Pra aplicar a mesma fórmula unificada dos outros 3 gatilhos, colapsa pra um único valor cheio (7, o maior — mesmo princípio dos outros 3, que usam o valor "cheio" antes de qualquer mitigação por habilidade), com a redução por habilidade acontecendo só uma vez, pela fórmula unificada (não duas vezes — a distinção original 5/7 desaparece).

A divisão inteira trunca (`10/2/2` dá 2, não 2.5) — mesmo comportamento de todas as outras reduções por habilidade já existentes no código (`decStress`, `incStress` em `Carro.calculaDesgastePneus`), então não é uma inconsistência nova.

A magnitude da largada ruim não pode ser proporcional ao intervalo antigo (500-700 ciclos não tem equivalente direto em "quantidade de estresse instantâneo") — o valor cheio 30-40 (agora reduzido à metade, 15-20) foi escolhido como um evento de estresse acima dos outros gatilhos existentes, mas ainda dentro da faixa 0-100, sujeito ao cap interno de `incStress`. Ver Risco 1.

### 4. Remover `desconcentradoAgressivoNesteTick`/`processaStressDesconcentradoAgressivo` sem substituto

Como `ciclosDesconcentrado` deixa de existir, a condição "AGRESSIVO + estresse<70 + desconcentrado" não tem mais sentido — remove o campo, o método consumidor, e a chamada dentro de `processaStress()`. Não precisa de flag consumível (diferente do padrão usado alhures), porque não há mais nada a sinalizar.

### 5. Mensagem de "problema na largada": dispara no momento do evento

Hoje a mensagem só aparece quando o contador chega a zero (`decrementaPilotoDesconcentrado()`, ramo `ciclosDesconcentrado <= 0`). Sem o contador, `ControleQualificacao` dispara a mensagem imediatamente ao setar `setProblemaLargada(true)` (mesmo texto/estilo), já que não há mais uma contagem regressiva pra esperar terminar.

**Observação sobre `processaEscapadaDaPista()` (achada ao verificar a magnitude a pedido do usuário)**: o gatilho só dispara quando `getStress() > Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA` (90), ou seja, o estresse já está acima de 90 no momento da chamada `incStress(testeHabilidadePiloto() ? 1 : 3)`. Como `incStress()` tem o cap `if (stress > 90) val = 1;`, o valor final é **sempre 1**, independente de passar ou não no teste de habilidade — a distinção "3 sem habilidade / 1 com habilidade" nunca se manifesta na prática nesse gatilho específico (é a única situação, dos 4 convertidos, onde isso acontece, porque é o único cujo estresse mínimo pra disparar já é maior que o limiar do cap). Não é um bug introduzido pela conversão — é uma interação pré-existente entre o limiar de disparo do evento e o cap de `incStress()` — mas vale registrar caso o usuário queira revisar a magnitude sabendo disso.

### 6. Redistribuição pós-levantamento: colisão, aerofólio e freada mal-sucedida sobem de magnitude

Depois de medir empiricamente que desgaste de pneu em curva baixa respondia por ~92-93% de todo o estresse gerado (ver levantamento anexo à conversa), o usuário pediu pra aproximar colisão/aerofólio/freada mal-sucedida do patamar de "ceder passagem" (`desviaPilotoNaFrente`, ~6,5-7% do total). Cálculo a partir dos dados medidos (soma-alvo ≈ 27.823/corrida, igual à soma de "ceder passagem"):

| Gatilho | Eventos/corrida | Magnitude/evento necessária | Viável? |
|---|---|---|---|
| Colisão | ~2.101 | ~13,2 | Sim — mudança de 1→13 |
| Aerofólio | ~52 | ~535 | Não — estouraria a escala 0-100 num único evento |
| Freada mal-sucedida | ~12,5 | ~2.226 | Não — ainda mais inviável |

Aerofólio e freada mal-sucedida são raros demais (12-64 ocorrências por corrida, somando os 20 carros) pra que magnitude por evento sozinha os leve ao mesmo patamar de um gatilho que ocorre 20 mil+ vezes — precisariam de um valor de incremento maior que a própria escala de estresse. Decisão do usuário: aumentar a magnitude moderadamente mesmo assim (colisão 1→13, aerofólio 15→30, freada `10-desgaste/100`→`30-desgaste/100`), aceitando que aerofólio/freada continuem com participação percentual baixa por serem raros.

**Achado após medir o resultado real**: colisão subiu de ~0,5% pra ~1,8-2,3% (melhora real, mas não chegou nos ~6,5-7% de "ceder passagem"). Aerofólio e freada quase não mudaram (~0,1% e ~0,05%) apesar do aumento de magnitude. Causa: o cap pré-existente de `incStress()` (`stress>90→val=1`, `stress>80→val=2`, `stress>70→val=3`) — colisão, dano de aerofólio e freada mal-sucedida tendem a ocorrer justamente quando o piloto já está com estresse elevado (esses eventos correlacionam com estresse alto), então o valor pedido é sistematicamente reduzido a 1-3 na prática, limitando o efeito de qualquer aumento de magnitude bruta. Enfraquecer o cap resolveria, mas afetaria **todos** os gatilhos de estresse, não só esses três — usuário optou por deixar como está por agora.

## Risks / Trade-offs

- **[Risco] A largada ruim perde a duração de "quase a corrida inteira"** → um piloto com largada ruim hoje fica com um handicap real e prolongado (500-700 ciclos de IA travada); convertido pra um pico de estresse único, o efeito se dissipa conforme o estresse decai (mais rápido em modo NORMAL/LENTO). **Mitigação**: nenhuma automática — é exatamente o comportamento que o usuário pediu pra observar em simulação; se o efeito sumir rápido demais, os valores em `Global` podem ser ajustados numa iteração seguinte (mesmo padrão das rodadas anteriores de tuning de estresse).
- **[Risco] Remover os 6 gates pode deixar pilotos "recuperados rápido demais" de incidentes** (ex.: escapar da pista não impede mais nenhuma ação no próprio tick) → a única consequência remanescente desses eventos é o incremento de estresse (que não bloqueia nada, só acumula). **Mitigação**: nenhuma — é o comportamento pedido explicitamente; validar em simulação se o jogo continua parecendo coerente sem o bloqueio.
- **[Risco] Testes existentes quebram** (`decrementaPilotoDesconcentrado_*`, `processaStressDesconcentradoAgressivo_*` em `PilotoProcessaStressConsolidadoTest.java`) → remover esses testes faz parte das tasks; novos testes cobrem o comportamento substituto.

## Migration Plan

1. Remover os 6 gates de `verificaDesconcentrado()` (sem substituto).
2. Trocar o gate de `processaMudancaRegime()` por `getStress() >= 90`.
3. Converter os 4 gatilhos em `incStress(...)`.
4. Mover a mensagem de "problema na largada" pro momento do evento em `ControleQualificacao`.
5. Remover `ciclosDesconcentrado`, `verificaDesconcentrado()`, `decrementaPilotoDesconcentrado()`, `desconcentradoAgressivoNesteTick`, `processaStressDesconcentradoAgressivo()`, e a chamada de `decrementaPilotoDesconcentrado()` no tick principal.
6. Remover testes obsoletos, adicionar testes novos, rodar `mvn -o clean test`.

## Open Questions

- Magnitude exata do pico de estresse da largada ruim (proposto 15-20, ou 7-10 se o piloto passar no teste de habilidade) — só validável observando simulações reais, conforme o próprio usuário pediu.
