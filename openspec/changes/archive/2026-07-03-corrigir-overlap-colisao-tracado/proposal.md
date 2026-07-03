## Why

O bloqueio de colisão existente (`collision-physics-block`) só iguala o `ganho` do carro de trás ao do carro da frente por ciclo; isso não impede que o `noIndex` do carro de trás ultrapasse o do carro da frente quando a aproximação em um único ciclo é maior que a janela coberta pelas hitboxes (largada, carro parado por acidente, bandeirada) — o carro de trás invade ou passa por cima do carro à frente. Em filas formadas após essas invasões, a checagem de mudança de traçado (`verificaColisaoAoMudarDeTracado`) conta qualquer carro próximo, inclusive os da própria fila, e trava a saída para um traçado lateral livre. Mudanças de traçado forçadas (escapada por stress, desvio de safety car) também podiam interromper a animação de uma troca em andamento, fazendo o carro "teleportar" de linha.

## What Changes

- Adicionar um limite posicional de avanço (`limitaAvancoCarroFrente`) que nunca deixa o `noIndex` do carro de trás entrar ou atravessar a área do carro logo à frente na mesma linha, complementando o cap de `ganho` existente.
- Estender a penalidade de colisão para considerar também o carro da frente que ainda está cruzando a linha atual (mudou de traçado mas o corpo ainda não saiu dela).
- Adicionar manobra de escape de fila indiana: um carro preso rastejando atrás de outro na mesma linha por vários ciclos verifica se um traçado lateral está livre (sem contar os próprios vizinhos de fila) e muda para ele.
- Ampliar o cooldown entre mudanças de traçado para cobrir a duração completa da animação de troca mais folga, contado apenas a partir de mudanças efetivadas (tentativas bloqueadas não resetam o cooldown).
- Impedir que mudanças de traçado forçadas (escapada por stress, desvio de safety car) interrompam uma animação de troca em andamento; quando inevitável, uma reversão de linha continua a partir do progresso lateral atual em vez de reiniciar a interpolação.
- Adicionar logs de diagnóstico (`[OVERLAP_REAL]`, `[ESCAPE_FILA]`, `[TRACADO_RESET]`) atrás de `Global.LOG_COLISAO` para permitir validação via simulação headless.

## Capabilities

### New Capabilities

- `tracado-safe-lane-change`: Regras de segurança para mudança de traçado — cooldown cobrindo a animação completa, proteção contra reset de animação em mudanças forçadas, e manobra de escape de fila indiana para carros presos sem traçado lateral realmente ocupado.

### Modified Capabilities

- `collision-physics-block`: Adiciona um limite posicional (`noIndex`) de avanço que atua junto ao cap de `ganho` já existente, e estende a penalidade de colisão para reconhecer o carro da frente em transição de traçado.

## Impact

- `br.f1mane.entidades.Piloto` — `processaPenalidadeColisao`, `processaNovoIndex`, `processaMudarTracado`, `mudarTracado`, `escapaTracado`; novos métodos `limitaAvancoCarroFrente`, `tentarEscaparFilaIndiana`, `verificaTracadoLivreParaEscapar`.
- `br.f1mane.controles.ControleJogoLocal` — logging de diagnóstico `[OVERLAP_REAL]` em `atualizaIndexTracadoPilotos`.
- Novos testes: `PilotoColisaoFilaTest`, `PilotoPenalidadeEscapeFilaTest`.
