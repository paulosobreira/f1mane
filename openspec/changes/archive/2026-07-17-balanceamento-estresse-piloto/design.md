## Context

`piloto-gestao-estresse` centraliza as regras de ganho/perda de estresse do piloto em `Piloto.processaStress()`, com os valores brutos de cada gatilho amortecidos por dois mecanismos genéricos em `incStress()`/`decStress()` (Piloto.java): tetos por faixa de stress (`incStress`) e multiplicadores por modo de pilotagem (`decStress`). Esta mudança ajusta esses dois mecanismos genéricos e um gatilho específico (freada mal-sucedida na reta), com base em observações levantadas em sessão de exploração sobre onde o amortecimento atual deixa buracos (faixa 50-70 sem teto) e onde um modo de pilotagem fica sem nenhuma via de recuperação de fato.

## Goals / Non-Goals

**Goals:**
- Estender o amortecimento de `incStress()` pra cobrir a faixa de stress 50-70, hoje sem nenhum teto.
- Tornar explícito e uniforme o multiplicador de `decStress()` por modo de pilotagem, incluindo um valor 0x pra AGRESSIVO.
- Generalizar o gatilho de freada mal-sucedida na reta pra todos os pilotos, e avaliá-lo no trecho de frenagem em si (não na entrada da curva), preservando a probabilidade efetiva atual (~10% por evento).

**Non-Goals:**
- Não resolve a divergência já identificada entre a spec (`processaStress()` como ponto único de escrita de estresse) e o código atual, que tem chamadas diretas de `incStress`/`decStress` fora desse método (`ControleQualificacao.java:272`, `Piloto.desviaPilotoNaFrente()`, `Piloto.mensagemRetardatario()`). Fica registrado como débito técnico pré-existente, fora de escopo aqui.
- Não altera a magnitude bruta de nenhum gatilho de `incStress` (12/8 de colisão, 30 de dano de aerofólio, `30 - desgaste/100` de freada) — só os tetos que os amortecem em stress alto.
- Não introduz o gatilho de "escapada de pista sob pressão" que a spec descreve mas que não existe no código atual (`testeEscapadaStress()` hoje só lê stress, não escreve) — fica como está, também fora de escopo.

## Decisions

### 1. Tetos de `incStress()`: mover limiares (50/70/90) em vez de reduzir os valores dos caps

Trocar `stress>70→3 / stress>80→2 / stress>90→1` por `stress>50→3 / stress>70→2 / stress>90→1`, mantendo os mesmos três valores de teto (3/2/1), só descendo os limiares em que cada um passa a valer.

**Alternativa considerada e descartada**: manter os limiares atuais (70/80/90) e cortar os valores dos caps pela metade (1.5/1/0.5). Foi descartada porque `stress` e `val` em `incStress(int val)` são `int` — caps fracionários exigiriam decidir entre arredondar ou truncar, e as duas escolhas mudam o comportamento de forma não óbvia (truncar criaria um teto suave perto de stress~90, arredondar tornaria a mudança de nível 90 um no-op). Mover os limiares evita esse problema por construção — todos os valores continuam inteiros.

### 2. `decStress()`: multiplicador explícito por modo, incluindo AGRESSIVO 0x

Adicionar um branch explícito pra AGRESSIVO com multiplicador 0, ao lado de NORMAL 1x (era 1.1x) e LENTO 1.5x (inalterado).

**Efeito colateral aceito, não uma alternativa**: o decaimento passivo por tick em `processaStress()` já não chama `decStress` pra AGRESSIVO (o `if/else if` só cobre NORMAL/LENTO) — então o multiplicador 0x não muda nada nesse caminho. O único caminho onde ele muda algo de fato é `processaStressFilaBox()` (chamado por `ControleBox` sem checar o modo do piloto), que hoje aplica os 2 pontos de recuperação cheios pra qualquer piloto na fila, AGRESSIVO incluso. Com essa mudança, um piloto em modo efetivo AGRESSIVO passa a não ter nenhuma via de recuperação de estresse — nem rodando, nem na fila do box — até o modo efetivo mudar (`getModoPilotagemEfetivo()` rebaixa AGRESSIVO pra NORMAL quando `stress>95`, mas só pra decisão automática de IA, não pro jogador humano em modo manual). Essa é uma consequência intencional da mudança, não um bug a evitar — registrada aqui pra quem revisar o código entender o motivo.

### 3. Freada mal-sucedida na reta: generalizar pra todos + mover verificação pra zona de frenagem, preservando disparo único

Duas mudanças na mesma condição, em `processaFreioNaReta()`:
- Remove `getPosicao() <= 3` — o gatilho deixa de ser exclusivo do top-3.
- A condição de localização muda de "chegando no nó de curva baixa" (`getNoAtual().verificaCurvaBaixa()`) pra "na reta, dentro da zona de frenagem" (nó que não é curva E `controleJogo.isNoZonaFrenagem(getNoAtual())`).

**Decisão explícita, confirmada com o usuário**: como uma zona de frenagem cobre vários nós/ticks (diferente do nó único de entrada de curva usado hoje), mover a verificação sem mais nada faria o sorteio de >0.9 ser avaliado em cada tick dentro da zona, inflando a chance efetiva de disparo bem acima dos ~10% pretendidos por evento. A implementação deve preservar o padrão de disparo único já existente via `retardaFreiandoReta` (que é consumido e resetado no mesmo instante em que o gatilho é avaliado) — a mudança é só sobre ONDE e PRA QUEM a avaliação acontece, não sobre QUANTAS VEZES por evento de frenagem.

## Risks / Trade-offs

- **[Risco] AGRESSIVO sem nenhuma recuperação de estresse pode se tornar uma escolha estritamente pior em corridas longas, mesmo pra jogadores humanos que sabem o que estão fazendo, já que não há mecanismo de auto-rebaixamento de modo pro jogador manual.** → Mitigação: nenhuma nesta mudança (é o comportamento pretendido); vale observar em playtesting se isso torna AGRESSIVO inviável na prática ao longo de uma corrida completa, e não só em estilos curtos/qualify.
- **[Risco] Generalizar a freada mal-sucedida pra todos os pilotos (não só top-3) aumenta a frequência agregada desse gatilho de estresse na simulação como um todo**, já que hoje só ~3 de N pilotos são elegíveis. → Mitigação: o teto de `incStress` na faixa 50-70 (decisão 1) amortece parte desse aumento agregado para pilotos que já estão estressados; nenhuma mitigação adicional é necessária pra pilotos com stress baixo, onde o aumento de frequência é o efeito pretendido.
- **[Risco] Mover a verificação de "entrada da curva" pra "zona de frenagem" muda o node exato em que `freioNaRetaMalSucedidoNesteTick` é setado**, o que pode interagir com timing de outros efeitos por tick que dependem do nó atual do piloto (ex.: mensagens, cálculo de ganho). → Mitigação: revisar se algum outro código lê `freioNaRetaMalSucedidoNesteTick` ou o estado do piloto assumindo que isso só acontece no nó de curva baixa, antes de implementar.

## Open Questions

Nenhuma pendente — as três decisões acima foram discutidas e confirmadas em sessão de exploração antes desta proposta, incluindo a pergunta explícita sobre frequência do sorteio na decisão 3.
