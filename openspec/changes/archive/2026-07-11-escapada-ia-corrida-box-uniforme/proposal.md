## Why

A mudança anterior (`objeto-escapada-tracado`, arquivada) reconectou o EDITOR ao novo modelo de `ObjetoEscapada` (encadeamento de pontos ancorado ao traçado 1/2 via `indiceEntrada`/`indiceSaida`), mas deixou a CORRIDA de fora de propósito: `Piloto.processaEscapadaDaPista()` continua rodando sobre a API antiga (`circuito.getEscapeMap()`), que hoje sempre retorna vazia, então nenhum piloto realmente escapa em corrida. `Circuito.gerarEscapeMap()` já populava `pista4Full`/`pista5Full` do tamanho certo especificamente para permitir essa reconexão futura (ver javadoc do método). Esta mudança é essa reconexão: pilotos agressivos e estressados passam a escapar de verdade pelas zonas desenhadas no editor, os demais tentam evitar essas zonas, e a velocidade/comportamento durante a escapada fica consistente. De quebra, corrige uma inconsistência não relacionada mas do mesmo bloco de configuração de corrida: hoje o box sorteia "rápido" ou "lento" uma vez por corrida (efeito de loteria em vez de característica do circuito), e o pedido é unificar num valor único intermediário.

## What Changes

- Reconecta `Circuito.gerarEscapeMap()` para popular `pista4Full`/`pista5Full` com o trajeto real interpolado de cada `ObjetoEscapada` (mesma técnica de interpolação por comprimento de arco já usada em `TestePista.construirTracadoEscapada`), em vez das listas sempre nulas de hoje — `pista4Full` a partir das escapadas com `tracadoOrigem==2`, `pista5Full` a partir de `tracadoOrigem==1` (mapeamento que já é exigido pelas regras existentes de retorno de traçado em `Piloto.mudarTracado`: traçado 4 só volta para {0,1}, traçado 5 só volta para {0,2}).
- Novo comportamento em `Piloto.processaEscapadaDaPista()`: piloto em modo `AGRESSIVO` com stress acima do limite (`Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA`, hoje 90), a 50 índices de nó ou menos da entrada de um `ObjetoEscapada` cujo `tracadoOrigem` é o traçado atual do piloto (1 ou 2), fica comprometido a escapar nessa zona.
- Novo comportamento de desvio para todo piloto controlado por IA: monitora a próxima zona de escapada no seu traçado atual (1 ou 2) e, a 100 índices de nó da entrada, tenta mudar para o traçado 0; se não conseguir até chegar à entrada, também escapa.
- Ao alcançar o índice de entrada estando comprometido (por agressividade+stress ou por falha no desvio), o piloto muda para o traçado de escapada correspondente (4 ou 5, força a mudança) e segue o trajeto de `ObjetoEscapada.getPontos()` (via `pista4Full`/`pista5Full`) até o índice de saída.
- Durante a escapada (traçado 4/5 e o retorno subsequente, mesma janela já coberta por `verificaForaPista`), o ganho de velocidade passa a ser reduzido pela metade **e** o piloto é travado em modo `LENTO` com giro mínimo (`Carro.GIRO_MIN_VAL`) — a redução por ganho já existia; a novidade é também forçar modo/giro.
- Mantém, sem alterações, o gatilho cego já existente (stress+agressivo+curva baixa no traçado 0 empurrando para 1/2 aleatoriamente) — roda em paralelo à nova lógica baseada em zonas reais, por decisão explícita para este ciclo.
- Nova variável global de teste (`Global`, seguindo o padrão de flags booleanas existente): quando ativa, ignora a checagem de agressividade+stress (mas mantém a exigência de posição/traçado), forçando qualquer piloto a se comprometer com a escapada assim que estiver a 50 índices ou menos da entrada — para validação em corridas de teste.
- **BREAKING** (comportamento de corrida, não de API pública): remove a distinção `boxRapido`/`boxLento` de `ControleBox` — o box passa a ter sempre a mesma velocidade/incremento de progresso, usando o valor intermediário entre os dois conjuntos de constantes de hoje, em todos os pontos onde `isBoxRapido()` é lido hoje (incrementos de `ptosBox` em `ControleBox.processarPilotoBox`, e o limite de últimas voltas para decidir parada em `Piloto.java`).

## Capabilities

### New Capabilities
- `escapada-ia-corrida`: comportamento de IA em corrida para comprometimento, desvio e execução de zonas de `ObjetoEscapada` (traçados 1/2 → 4/5), incluindo a redução de velocidade/modo durante a escapada e a variável global de teste.
- `box-velocidade-uniforme`: remoção do sorteio boxRapido/boxLento em `ControleBox`, substituído por um único valor intermediário de velocidade/progresso de box, usado de forma consistente em todos os cenários da corrida.

### Modified Capabilities
- (nenhuma — `piloto-gestao-estresse` e `tracado-safe-lane-change` são consumidos como estão, sem mudança de requisito; o gatilho cego existente em `processaEscapadaDaPista` permanece inalterado por decisão explícita)

## Impact

- `src/main/java/br/f1mane/entidades/Circuito.java` (`gerarEscapeMap()`, `pista4Full`/`pista5Full`)
- `src/main/java/br/f1mane/entidades/Piloto.java` (`processaEscapadaDaPista()` e vizinhança: `mudarTracado`, `getTracado`, `modoPilotagem`, `getStress`, `getCarro().setGiro`)
- `src/main/java/br/nnpe/Global.java` (nova flag de teste)
- `src/main/java/br/f1mane/controles/ControleBox.java` (`boxRapido`, `processarPilotoBox`)
- `src/main/java/br/f1mane/entidades/Piloto.java` (`limiteUltimasVoltas`, leitura de `isBoxRapido()`)
- Testes headless de simulação (`MainFrameSimulacao`, testes JUnit existentes de `ControleBox`/`Piloto` se houver) e circuitos com `ObjetoEscapada` real (poucos hoje, já que a remoção do modelo antigo limpou os XMLs existentes — este comportamento só se manifesta em circuitos onde zonas forem desenhadas novamente no editor).
