# icones-hud-genericos

## Purpose

Ícones de HUD (pneus, clima, combustível, faróis de largada, safety car de cima) livres de marca registrada, em estilo flat, gerados a partir de masters de alta resolução por script reproduzível, embarcados nas dimensões exatas que o rendering Swing e o cliente web esperam.

## Requirements

### Requirement: Ícones de pneu sem marca registrada
Os ícones de pneu (`png/pneuMole.png`, `png/pneuDuro.png`, `png/pneuChuva.png`) SHALL ser livres de qualquer marca registrada (nomes, logotipos ou trade dress de fabricantes de pneus), usando o código de cores do jogo na faixa lateral: amarelo = mole, branco = duro, azul = chuva.

#### Scenario: Pneus exibidos no HUD sem logotipos
- **WHEN** o jogador visualiza a seleção de pneus no menu local ou no HUD da corrida
- **THEN** os ícones exibem um pneu genérico com faixa colorida correspondente ao composto, sem nenhum texto ou logotipo de marca

### Requirement: Dimensões embarcadas preservadas
Cada PNG embarcado do conjunto de ícones de HUD SHALL manter as dimensões exatas esperadas pelo rendering Swing (desenho em tamanho natural ou resize relativo) e pelo cliente web (canvas em tamanho natural): pneus 142×142; pneus "Menor" 44×44; `sol`, `lua`, `nublado`, `chuva` 35×35; `fuel` 25×25; `sfcima` 90×90; `farois`, `farois1`–`farois4`, `farois-apagados` 150×89. Todos SHALL ter canal alfa (RGBA).

#### Scenario: Teste de contrato de dimensões
- **WHEN** a suíte de testes unitários roda
- **THEN** um teste carrega cada PNG do conjunto e afirma as dimensões listadas e a presença de canal alfa, falhando se qualquer regeneração alterar o contrato

#### Scenario: Resize relativo em runtime continua correto
- **WHEN** `PainelCircuito`/`PainelMenuLocal` aplicam `geraResize(0.3)` e `geraResize(0.15)` aos pneus grandes
- **THEN** os resultados mantêm os tamanhos visuais atuais (~43 px e ~21 px), pois a base 142×142 não mudou

### Requirement: Versões menores derivadas dos masters
Os arquivos `pneuMoleMenor.png`, `pneuDuroMenor.png` e `pneuChuvaMenor.png` (44×44) SHALL ser gerados por downscale de alta qualidade (Lanczos) dos mesmos masters de alta resolução dos pneus grandes, e não mantidos como artes independentes.

#### Scenario: Cliente web consome os menores
- **WHEN** `controles.html`, `jogar.html` ou `resultado.js` requisitam `/letsRace/png/pneu*Menor`
- **THEN** recebem ícones 44×44 visualmente consistentes com os pneus grandes do HUD

### Requirement: Ícones de clima e combustível em estilo flat com recorte pleno
Os ícones `sol.png`, `lua.png`, `nublado.png` e `chuva.png` SHALL ser redesenhados em estilo flat com o conteúdo ocupando o quadro (sem margens desperdiçadas nem artefatos de compressão), mantendo 35×35. O `fuel.png` SHALL ser uma bomba de combustível flat exclusivamente em tons de cinza, mantendo 25×25.

#### Scenario: Indicador de clima na corrida
- **WHEN** o painel da corrida exibe o clima atual (sol, nublado ou chuva) ou o período noturno (lua)
- **THEN** o ícone correspondente aparece nítido, com preenchimento pleno do quadro de 35×35

#### Scenario: Indicador de combustível
- **WHEN** o HUD desenha o indicador de combustível
- **THEN** o ícone de 25×25 aparece em tons de cinza, sem cores

### Requirement: Safety car de cima em alta qualidade com orientação preservada
O `sfcima.png` SHALL ser regenerado a partir de master em alta resolução, mantendo 90×90 e a mesma orientação (heading) do sprite anterior, pois o rendering Swing e o cliente web (`vdp.js`) o rotacionam como sprite de carro.

#### Scenario: Safety car em pista
- **WHEN** o safety car entra na pista (Swing ou cliente web)
- **THEN** o carro é desenhado alinhado ao traçado, sem inversão ou rotação incorreta em relação ao comportamento anterior

### Requirement: Faróis de largada flat com geometria única entre estados
Os seis arquivos de faróis (`farois-apagados.png`, `farois1.png`, `farois2.png`, `farois3.png`, `farois4.png`, `farois.png`) SHALL ser redesenhados em estilo flat condizente com os gráficos do jogo, mantendo 150×89, com a mesma geometria de painel em todos os estados — variando apenas quantas colunas de luzes vermelhas estão acesas (0, 1, 2, 3, 4 e 5 respectivamente).

#### Scenario: Sequência de largada
- **WHEN** a animação de largada alterna os seis estados (Swing ou `vdp.js`)
- **THEN** o painel permanece fixo no mesmo enquadramento, apenas com as luzes acendendo progressivamente, sem "pulos" visuais entre quadros

### Requirement: Pipeline reproduzível de geração de ícones
Um script `utilitarios/gerar_icones.py` SHALL gerar todos os ícones do conjunto de forma determinística: desenho vetorial com supersampling, gravação dos masters em alta resolução em `utilitarios/icones_hires/` (fora do classpath, para não inflar o fat jar) e downscale Lanczos para os PNGs embarcados em `src/main/resources/png/` nos tamanhos exatos do contrato.

#### Scenario: Regeneração dos assets
- **WHEN** o script é executado novamente
- **THEN** produz os mesmos masters e PNGs embarcados nos tamanhos corretos, sem intervenção manual
