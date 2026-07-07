## ADDED Requirements

### Requirement: Preload do background dispara na confirmação de "Jogar"
O cliente HTML5 SHALL disparar o carregamento do JPG de fundo do circuito (`/rest/letsRace/circuitoJpg/...`) assim que o jogador confirmar "Jogar" para um circuito, em vez de esperar a tela de corrida (`corrida.html`) fazer polling e atingir o estado de qualify/corrida.

#### Scenario: Clique em "Jogar" dispara o preload do background
- **WHEN** o jogador confirma "Jogar" para um circuito em `jogar.js`
- **THEN** o cliente inicia o carregamento do JPG de fundo daquele circuito imediatamente, em paralelo com a navegação/REST de entrada na corrida, sem esperar `corrida.html` carregar

#### Scenario: Seleção de circuito no carrossel não dispara preload
- **WHEN** o jogador apenas navega/seleciona circuitos diferentes no carrossel de `jogar.js` sem clicar em "Jogar"
- **THEN** nenhum carregamento do JPG de fundo é disparado, evitando geração desnecessária de imagem no servidor para circuitos que o jogador não confirmou

### Requirement: Tela de corrida não bloqueia à espera do background já solicitado
A tela de corrida (`corrida.html`/`cpu.js`) SHALL continuar funcionando corretamente quando o background já foi solicitado (ou já chegou) antes dela carregar, sem re-solicitar a imagem nem travar esperando uma segunda requisição.

#### Scenario: Background já carregado quando a tela de corrida abre
- **WHEN** o preload disparado no clique de "Jogar" já completou (ou está em andamento) no momento em que `corrida.html` carrega
- **THEN** `mid_carregaBackGroundCorrida`/`cpu_main` reconhecem que o carregamento já foi solicitado e não disparam uma segunda requisição para o mesmo circuito

#### Scenario: Preload antecipado falha ou não ocorre
- **WHEN** o preload disparado no clique de "Jogar" não ocorreu (ex.: navegação direta para `corrida.html` sem passar por `jogar.js`)
- **THEN** o disparo existente em `mid_carregaBackGroundCorrida`/`cpu_main` continua funcionando como fallback, carregando o background normalmente em qualquer estado — inclusive na sala de espera (07)

### Requirement: Tela de espera exibe a pista, o contador e a mensagem de carregamento
Durante a sala de espera (estado 07), a tela de corrida SHALL exibir o background da pista assim que a imagem estiver disponível, junto com o contador de segundos para o início (`iniciaEm : dadosJogo.segundosParaIniciar`, alimentado por `Global.SEGUNDOS_PARA_INICIAR_CORRRIDA`) e a mensagem `msgCarregando` ("Corrida começará em alguns instantes"), desenhados por `ctl_desenhaInfoSegundosParaIniciar`. Como `vdp_desenha` só chega em `vdp_ctl()` (que copia o overlay de controles pra tela) quando `cvBg` existe, o carregamento do background SHALL ser disparado também no estado 07 — sem isso, a tela de espera inteira fica em branco durante toda a contagem.

#### Scenario: Sala de espera mostra pista, contador e msgCarregando
- **WHEN** o jogador entra em `corrida.html` com o jogo no estado 07 e a imagem de fundo termina de carregar
- **THEN** a pista aparece ao fundo (centrada na largada), com o contador "Inicia em : N" decrementando e o texto de `msgCarregando` visível abaixo dele

#### Scenario: Contagem zera e o jogo transiciona normalmente
- **WHEN** `segundosParaIniciar` chega a zero e o servidor muda o estado do jogo
- **THEN** a tela transiciona da espera para o qualify/largada sem recarregar o background (a imagem já está em `cvBg`)
