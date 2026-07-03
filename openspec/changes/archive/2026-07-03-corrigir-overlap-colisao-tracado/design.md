## Context

`collision-physics-block` já limita o `ganho` do carro de trás ao `ganho` do carro à frente quando as hitboxes (`diateiraColisao`, `centroColisao`, `trazeiraColisao`) se intersectam. Esse cap é relativo (por ciclo) e reage à hitbox atual, não à posição (`noIndex`) resultante. Em cenários de aproximação abrupta — largada, carro imobilizado por acidente, desaceleração pós-bandeirada — o avanço de um único ciclo pode exceder a largura das hitboxes antes de a próxima leitura de colisão ocorrer, permitindo que o `noIndex` do carro de trás ultrapasse o do carro da frente mesmo com o `ganho` já igualado.

Um segundo problema, encadeado ao primeiro: quando vários carros ficam presos em fila (rastejando atrás de um acidente ou de um carro à frente também bloqueado), `verificaColisaoAoMudarDeTracado` rejeita a mudança de traçado se **qualquer** piloto — inclusive os vizinhos da própria fila — estiver a até 75 nós, mesmo que o traçado lateral escolhido esteja livre. A fila nunca se desfaz sozinha.

Terceiro: mudanças de traçado forçadas (`forcaMudar=true`, usadas por escapada de stress e desvio de safety car) ignoram o guard `indiceTracado != 0` que normalmente impede uma nova troca no meio da animação da anterior. Isso reseta `indiceTracado` para o valor cheio com uma nova linha de origem, fazendo o carro pular lateralmente de forma visível.

## Goals / Non-Goals

**Goals:**
- Garantir que, em qualquer cenário de física real (sem atualização suave), o `noIndex` de um carro nunca entre ou ultrapasse a área ocupada pelo carro logo à frente na mesma linha.
- Permitir que carros presos em fila (rastejando, sem progresso) encontrem e usem um traçado lateral genuinamente livre.
- Impedir que mudanças de traçado forçadas interrompam visualmente uma animação de troca em andamento.
- Manter observável via log headless (`Global.LOG_COLISAO`) para validação por simulação, sem depender de inspeção visual.

**Non-Goals:**
- Não mexe na física da atualização suave (`PainelCircuito.atualizacaoSuave`) — o clamp desta mudança atua na simulação real (`processaNovoIndex`), que é a fonte de verdade; a suavização apenas interpola o desenho a partir dela.
- Não introduz um novo modo de pilotagem nem altera a IA de ultrapassagem geral (`desviaPilotoNaFrente`) — o escape de fila é um mecanismo de última instância, disparado só após vários ciclos sem progresso.
- Não altera o comportamento em safety car (fila atrás do carro de segurança é intencional e o escape de fila é desabilitado nesse estado).

## Decisions

**Clamp posicional complementar ao cap de ganho (`limitaAvancoCarroFrente`)**
Em vez de substituir a lógica de `ganho` existente por algo baseado só em posição, a nova checagem roda depois do cálculo de `ganho` normal e do cap por colisão, limitando o avanço bruto (`roundGanho`) para nunca cruzar `noIndex` do carro à frente na mesma linha, com uma folga de um comprimento de carro (`METADE_CARRO * 2`). Alternativa considerada: aumentar a área das hitboxes de colisão para cobrir avanços maiores — rejeitada porque infla falsos positivos de colisão em aproximações normais de ultrapassagem.

**Contador de "preso em fila" em vez de detecção instantânea**
`ciclosPresoFila` incrementa apenas quando há colisão na mesma linha **e** o `ganho` está em nível de rastejo (≤ 10); zera assim que a colisão desaparece ou o ganho volta ao normal. Isso evita que o escape dispare para um pelotão andando compacto mas fluido (ganho normal, apenas colado). Alternativa considerada: disparar o escape na primeira colisão detectada — rejeitada por gerar zigue-zague de traçado em toda ultrapassagem comum.

**Escape ignora apenas a checagem antiga de vizinhança, não os demais guards**
`tentarEscaparFilaIndiana` chama uma nova sobrecarga `mudarTracado(pos, forcaMudar=false, escapandoFila=true)` que pula somente `verificaColisaoAoMudarDeTracado` — cooldown, bandeirada, safety car, transição em andamento e as travas de seta continuam válidos. A checagem de "livre" é refeita do zero (`verificaTracadoLivreParaEscapar`), olhando só quem ocupa ou cruza o traçado-alvo, não qualquer carro próximo.

**Cooldown dimensionado pela duração real da animação**
Com atualização suave ligada, o piso do cooldown passa a ser `(indiceTracado inicial da pista / 2) + 4` ciclos — a duração exata da interpolação lateral (`decIndiceTracado` desconta 2 por ciclo) mais folga. Isso é calculado por circuito via `Circuito.getIndiceTracado()`, então pistas com faixas mais largas (`multiplicadorLarguraPista` maior) recebem cooldown proporcionalmente maior.

**Mudança forçada no meio da animação: esperar ou continuar, nunca resetar**
Dois pontos de força (`escapaTracado`, desvio de safety car em `processaMudarTracado`) passam a aguardar `indiceTracado == 0` antes de agir — o stress ou a condição de safety car persiste entre ciclos, então adiar por alguns ciclos não perde a oportunidade. Quando uma força ainda assim ocorre a meio de animação (caminho defensivo, não esperado nos dois pontos acima) e é uma **reversão** para a linha de origem, `indiceTracado` é recalculado para espelhar o progresso already feito (`cheio - restante`) em vez de reiniciar do valor cheio — o carro continua visualmente de onde estava, só invertendo a direção da interpolação.

## Risks / Trade-offs

- [Clamp posicional pode segurar um carro levemente mais que o necessário quando dois carros mudam de traçado no mesmo ciclo em direções cruzadas] → Mitigação: a checagem usa a posição real (`noIndex`) mais recente de ambos, recalculada a cada ciclo; o pior caso é um ciclo de atraso adicional, não uma trava permanente.
- [Escape de fila pode ativar em uma ultrapassagem legítima muito lenta, ex. carro com pneus muito desgastados] → Mitigação: o limiar de 8 ciclos rastejando (ganho ≤ 10) é alto o suficiente para não confundir com desaceleração pontual; validar com simulações headless (`[ESCAPE_FILA]`) antes de reduzir o limiar.
- [Cooldown maior pode deixar a IA "grudada" em uma linha por mais tempo em pistas com faixa muito larga] → Mitigação: cooldown só se aplica com atualização suave ligada e é limitado pela duração real da animação daquela pista especificamente, não um valor global fixo.

## Migration Plan

Mudança é aditiva e não quebra estado salvo (nenhum campo persistido novo). Deploy normal via build (`mvn clean package`); não há flag de rollout — o comportamento novo é o default. Rollback: reverter o commit da correção (`Piloto.java`, `ControleJogoLocal.java`); nenhuma migração de dados envolvida. Validação pré-deploy via `MainFrameSimulacao` com `Global.LOG_COLISAO=true`, checando ausência de `[OVERLAP_REAL]` em múltiplas combinações de circuito/temporada.

## Open Questions

- O limiar de 8 ciclos e a folga de cooldown (+4 ciclos) foram calibrados por simulação exploratória; podem precisar de ajuste fino após mais rodadas de teste em pistas com curvas fechadas (não cobertas pelas simulações de reta usadas até aqui).
