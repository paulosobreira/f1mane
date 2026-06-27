## Why

O jogo já possui lógica de colisão (`processaColisao` em `Piloto.java`) com hitboxes dianteira/centro/traseira, mas a penalidade atual (`processaPenalidadeColisao`) só incrementa stress — sem bloquear fisicamente o avanço do carro de trás. Isso permite que um carro "passe por cima" do carro à frente quando o `ganho` é grande o suficiente. A verificação precisa ser feita sem renderização suave (modo `VALENDO=false`) para poder inspecionar os logs e identificar os casos onde o bloqueio físico falha.

## What Changes

- Adicionar logging detalhado de colisão por ciclo: posição (noIndex), tracado, hitboxes (bounds de dianteira/centro/trazeira), flag `colisaoDiantera`, flag `colisaoCentro`, e `ganho` do piloto de trás
- Adicionar logging quando uma colisão é detectada com os dados dos dois pilotos envolvidos (piloto de trás e piloto da frente)
- Corrigir `processaPenalidadeColisao` para bloquear fisicamente o avanço do carro de trás quando `colisaoDiantera` com o centro do carro à frente (não pode ultrapassar) e desacelerar consideravelmente quando `colisaoDiantera` com a traseira do carro à frente
- Adicionar script de simulação em lote (`simulacao_batch.sh`) para testar múltiplas combinações temporada/circuito/voltas após a correção

## Capabilities

### New Capabilities

- `collision-physics-log`: Logging estruturado do estado das hitboxes e detecção de colisão a cada ciclo, ativado por flag `Global.LOG_COLISAO`, para análise via simulação headless
- `collision-physics-block`: Bloqueio físico do ganho do carro de trás quando `colisaoDiantera` intersecta `centroColisao` do carro à frente (velocidade reduzida ao nível do carro da frente), e desaceleração forte quando `colisaoDiantera` intersecta `trazeiraColisao`
- `batch-simulation`: Script `simulacao_batch.sh` que executa combinações aleatórias de temporada/circuito/voltas e agrega logs de colisão para validação em lote

### Modified Capabilities

- (nenhuma mudança de requisitos em specs existentes)

## Impact

- `br.f1mane.entidades.Piloto` — `processaColisao`, `processaPenalidadeColisao`, `centralizaCarroColisao`
- `br.f1mane.Global` — nova flag `LOG_COLISAO`
- `br.f1mane.MainFrameSimulacao` — sem mudança estrutural, apenas verifica que `ControleCiclo.VALENDO=false` está ativo
- Novo arquivo: `simulacao_batch.sh`
