## Context

O sistema de colisão atual em `Piloto.processaColisao()` detecta interseção entre `Rectangle` hitboxes (dianteira/centro/traseira) de cada carro mas a penalidade em `processaPenalidadeColisao()` apenas incrementa `stress` — não impede o avanço do carro. O `ganho` calculado por `processaGanho()` continua intacto, então em ciclos onde o ganho é alto o suficiente o carro de trás avança além do carro da frente trocando posições fisicamente (passa por cima).

A verificação de colisão ocorre dentro de `processaNovoIndex()` antes do `roundGanho` ser aplicado, então é o lugar certo para limitar o ganho quando houver colisão detectada. O cálculo de hitboxes usa coordenadas em pixels mapeadas nos nós (`No`) da pista e os traçados (0=centro, 1-5=laterais/escape).

O modo `ControleCiclo.VALENDO=false` já existe em `MainFrameSimulacao` e desabilita o `Thread.sleep`, tornando a simulação determinística e rápida — ideal para análise de logs.

## Goals / Non-Goals

**Goals:**
- Adicionar flag `Global.LOG_COLISAO` que ativa log estruturado por ciclo com: nome do piloto, noIndex, tracado, bounds das três hitboxes, flags colisaoDiantera/colisaoCentro, ganho atual
- Limitar `ganho` do piloto de trás quando `colisaoDiantera` intersecta `trazeiraColisao` do carro à frente (desaceleração forte, ganho limitado ao ganho do carro da frente)
- Bloquear avanço (ganho = ganho do carro da frente) quando `colisaoDiantera` intersecta `centroColisao` do carro da frente (carro de trás não pode ultrapassar)
- Script `simulacao_batch.sh` para executar N simulações aleatórias e filtrar logs de colisão

**Non-Goals:**
- Não alterar a lógica de detecção de acidente físico (`verificaAcidente`) nem o visual de colisão
- Não mudar o modo multiplayer (`PaddockServer`)
- Não reescrever o sistema de hitboxes — apenas usar os `Rectangle` já calculados

## Decisions

**1. Flag `Global.LOG_COLISAO` em vez de log sempre ativo**
O log por ciclo de todos os pilotos é verboso (~20 linhas/ciclo × 20 pilotos). Ativar apenas quando necessário evita poluir os logs de produção. A flag pode ser `true` por padrão em `MainFrameSimulacao` e `false` em `MainFrame`/`MainLauncher`.

**2. Limitar `ganho` em `processaPenalidadeColisao` (não em `processaColisao`)**
`processaColisao` é chamada antes de `processaLimitadorGanho` e antes do `roundGanho` final. Inserir o cap de ganho em `processaPenalidadeColisao` (chamado logo após `processaColisao`) mantém a ordem de chamadas e é reversível sem risco de efeito cascata em outras condições.

**3. Ganho limitado ao ganho do carro da frente**
Quando há colisão, o carro de trás não pode avançar além do carro da frente no mesmo ciclo. O valor seguro é: `ganho = Math.min(ganho, pilotoFrente.getGanho())`. Para `colisaoDiantera` com `trazeira` (toque leve) aplica-se também uma penalidade de 30% adicional (`ganho *= 0.7`) além do cap.

**4. Logging via `Logger.logar` existente**
Usa a infraestrutura de log já presente (Logback com rotação diária). Formato: `[COLISAO] ciclo=N piloto=X noIndex=Y tracado=Z diant=[x,y,w,h] centro=[...] traz=[...] colDiant=true/false colCentro=true/false ganho=G`.

## Risks / Trade-offs

- [Risco] Limitar o ganho pode afetar a lógica de ultrapassagem legítima → Mitigação: a penalidade só é aplicada quando `processaColisao` detecta interseção real; se o piloto muda de traçado a colisão deixa de ser detectada e o ganho volta ao normal no próximo ciclo.
- [Risco] Logs muito verbosos em simulações longas → Mitigação: `LOG_COLISAO` desativado por padrão; em `MainFrameSimulacao` ativado explicitamente.
- [Risco] Hitboxes calculadas com `emMovimento()` retornando cache podem estar desatualizadas → Mitigação: `processaColisao` já chama `centralizaCarroColisao()` forçando recalculo quando necessário; logging deve ocorrer após esse recalculo.

## Migration Plan

1. Adicionar `LOG_COLISAO` em `Global.java`
2. Ativar em `MainFrameSimulacao` antes do loop
3. Adicionar logging em `processaColisao` (após `centralizaCarroColisao`)
4. Modificar `processaPenalidadeColisao` para limitar ganho
5. Rodar `simulacao.sh` com Catalunya 2024 72 voltas e analisar logs
6. Se colisões de sobreposição encontradas, verificar se penalidade as elimina
7. Rodar `simulacao_batch.sh` com 10+ combinações aleatórias para regressão

Rollback: remover cap de ganho em `processaPenalidadeColisao` restaura comportamento anterior sem afetar nenhuma outra lógica.
