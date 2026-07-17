## MODIFIED Requirements

### Requirement: Decisão automática da IA recua de AGRESSIVO acima de 95% de stress, via teste de habilidade, sem afetar o jogador humano
Em `ControleAutomacao` (decisão automática de agressividade, escopo da IA — bot ou piloto automático dirigindo o carro do jogador humano, nunca o jogador humano decidindo manualmente), quando as condições normais indicariam a escolha de `AGRESSIVO` E `stress > 95`, o sistema SHALL dar à decisão automática uma chance de recuar proativamente para `NORMAL` através de `testeHabilidadePilotoCarro()`: em caso de sucesso, a decisão automática escolhe `NORMAL` em vez de `AGRESSIVO` nesse ciclo; em caso de falha, a decisão automática ainda pode escolher `AGRESSIVO` normalmente, apesar do stress alto. Esse teste SHALL NOT ser avaliado (nem consumir RNG) quando `stress <= 95` ou quando as condições normais já não indicariam `AGRESSIVO`. Este requisito SHALL NOT alterar em nada a capacidade do jogador humano de selecionar e manter `AGRESSIVO` manualmente (local ou via API multiplayer) a qualquer nível de stress, sem teste e sem efeito colateral além da ausência de ganho já definida no requisito de agressividade efetiva.

#### Scenario: Decisão automática recua pra NORMAL quando o teste de habilidade é bem-sucedido
- **WHEN** a decisão automática de agressividade (em `ControleAutomacao`) resultaria em `AGRESSIVO` (pneus OK, `stress < 100`), `stress > 95`, e `testeHabilidadePilotoCarro()` é bem-sucedido
- **THEN** a decisão automática escolhe `NORMAL` nesse ciclo, em vez de `AGRESSIVO`

#### Scenario: Decisão automática permanece AGRESSIVO quando o teste de habilidade falha
- **WHEN** a decisão automática de agressividade resultaria em `AGRESSIVO`, `stress > 95`, e `testeHabilidadePilotoCarro()` falha
- **THEN** a decisão automática ainda escolhe `AGRESSIVO` nesse ciclo, apesar do stress alto — a regra desta spec é uma chance de recuo, não uma garantia

#### Scenario: Teste não é avaliado com stress dentro do limite
- **WHEN** a decisão automática de agressividade resultaria em `AGRESSIVO` e `stress <= 95`
- **THEN** nenhum teste de habilidade adicional é avaliado por causa desta spec, e a decisão automática escolhe `AGRESSIVO` normalmente, sem consumir RNG extra

#### Scenario: Jogador humano continua escolhendo AGRESSIVO manualmente sem nenhum teste
- **WHEN** o jogador humano seleciona `AGRESSIVO` manualmente (local ou via API multiplayer), com `stress` a qualquer nível, incluindo acima de 95%
- **THEN** `modoPilotagem` é definido como `AGRESSIVO` imediatamente, sem nenhum teste de habilidade nem chance de recuo — esta spec rege exclusivamente a decisão automática de `ControleAutomacao`, nunca o caminho de comando do jogador humano
