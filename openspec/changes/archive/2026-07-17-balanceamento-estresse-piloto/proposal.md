## Why

O sistema de estresse do piloto (`piloto-gestao-estresse`) tem dois pontos de ajuste fino desalinhados com o comportamento desejado: os tetos de `incStress()` só começam a amortecer picos de estresse a partir de stress>70, deixando a faixa 50-70 sem nenhum freio para eventos grandes (colisão, dano de aerofólio, freada mal-sucedida); e o modo AGRESSIVO, apesar de já não se recuperar passivamente por tick, ainda recupera estresse cheio ao entrar na fila do box — a única via de alívio que sobra pra esse modo. Além disso, o gatilho de "freada mal-sucedida na reta" está restrito artificialmente ao top-3 e só é avaliado no instante de entrada na curva, não durante a própria frenagem.

## What Changes

- **Caps de `incStress()` por faixa de stress**: os limiares mudam de `stress>70→3 / stress>80→2 / stress>90→1` para `stress>50→3 / stress>70→2 / stress>90→1`. A faixa 50-70, hoje sem nenhum amortecimento, passa a ter teto de 3. Valores continuam inteiros — sem mudança de tipo.
- **BREAKING (balanceamento)**: multiplicadores de `decStress()` por modo de pilotagem passam de `NORMAL 1.1x / LENTO 1.5x / AGRESSIVO sem branch (1x implícito)` para `AGRESSIVO 0x (explícito) / NORMAL 1x / LENTO 1.5x`. Pilotos em modo efetivo AGRESSIVO deixam de recuperar qualquer estresse — inclusive na fila do box, que hoje é a única via de recuperação que sobra pra esse modo (o decaimento passivo por tick já excluía AGRESSIVO).
- Gatilho de "freada mal-sucedida na reta" (`processaFreioNaReta()`): remove a restrição `getPosicao() <= 3` — passa a valer para qualquer piloto, não só o top-3. O ponto de verificação muda de "chegando no nó de curva baixa" para "na reta, dentro da zona de frenagem" (nó que não é curva E dentro de `isNoZonaFrenagem`), preservando o disparo único por evento de frenagem (mesma trava de consumo de `retardaFreiandoReta` já usada hoje) para não inflar a chance efetiva do sorteio de ~10% por evento.

## Capabilities

### New Capabilities

(nenhuma)

### Modified Capabilities

- `piloto-gestao-estresse`: os requisitos de "Regras de disparo preservadas" precisam refletir os novos limiares de `incStress()`, os novos multiplicadores de `decStress()` por modo, e a nova condição de elegibilidade/localização do gatilho de freada mal-sucedida na reta (todos os pilotos, avaliado na zona de frenagem em vez de na entrada da curva).

## Impact

- **Código afetado**: `src/main/java/br/f1mane/entidades/Piloto.java` — `incStress()`, `decStress()`, `processaFreioNaReta()`.
- **Sem impacto de schema/persistência/API** — mudança é só de constantes e condições dentro da lógica de stress já existente.
- **Fora de escopo**: a divergência já identificada entre a spec `piloto-gestao-estresse` (que documenta `processaStress()` como ponto único de escrita de estresse) e o código atual (que tem 3 chamadas diretas de `incStress` fora de `processaStress()`, em `ControleQualificacao` e em `desviaPilotoNaFrente()`/`mensagemRetardatario()` de `Piloto.java`) não é tratada nesta mudança — só é mencionada no design como contexto relevante.
