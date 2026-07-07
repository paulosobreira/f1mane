## Why

No cliente web (HTML5), o carregamento da imagem de fundo do circuito só é disparado quando a página de corrida já está de pé e o polling atinge o estado de qualify/corrida (`cpu.js` → `mid_carregaBackGroundCorrida()`), várias trocas de página e round-trips depois do jogador confirmar "Jogar" — e o JPG é gerado sob demanda no servidor (`LetsRace.circuitoJpg`), não é um arquivo estático. Esse atraso dá a impressão de que o jogo travou logo ao iniciar. Ao mesmo tempo, a consolidação de `geraTravadaRoda` (`ControleJogoLocal.java`) passou a setar `marcaPneu=true` e o contador de fumaça (`travouRodas`) sempre juntos, tornando impossível produzir um evento "só marca" — o status `"M"` enviado ao cliente web nunca mais ocorre (só `"T"`), e o guard de fumaça do renderer Swing (`isMarcaPneu()`) fica sempre satisfeito, quebrando a independência entre marca e fumaça nos dois clientes. Além disso, o cliente web nunca teve acesso ao modelo de marcas por nó (`TravadaRoda`, com `idNo`/`tracado`) que já existe no servidor para o caminho do applet — ele infere a posição das marcas a cada tick a partir de um único caractere de status por piloto, sem produção/consumo real de marcas por trecho da pista.

## What Changes

- Antecipar o disparo do carregamento do JPG de fundo do circuito no cliente web para o momento em que o circuito é conhecido (seleção/clique em "Jogar"), em vez de esperar o polling da tela de corrida atingir qualify/corrida — eliminando a sensação de travamento sem voltar a gerar a imagem enquanto o jogador ainda nem confirmou a corrida.
- Restaurar, em `ControleJogoLocal.geraTravadaRoda`, a capacidade de gerar eventos de travada de roda **só com marca** (sem fumaça) de forma independente de eventos **marca + fumaça**, em vez de sempre setar os dois juntos.
- **BREAKING (protocolo)**: estender o payload de posição de pilotos (`ControleJogosServer`/`LetsRace`) para expor as marcas de pneu produzidas no backend por nó de pista (reaproveitando o modelo `TravadaRoda`/`idNo`/`tracado` já usado no caminho do applet), e migrar o cliente web para consumir/acumular essa lista autoritativa por nó em vez de inferir posição de marca a partir do único caractere de status (`"T"`/`"M"`) por piloto a cada tick.

## Capabilities

### New Capabilities
- `web-preload-background-circuito`: define quando o cliente HTML5 deve disparar o carregamento do JPG de fundo do circuito (gerado sob demanda no servidor) em relação à navegação/seleção de circuito, para que a imagem já esteja carregando (ou pronta) quando a tela de corrida abrir.
- `web-marcas-pneu-consumo-backend`: define como o cliente HTML5 obtém e consome as marcas de pneu produzidas no backend por nó de pista (produção autoritativa no servidor, consumo/acumulação no cliente), substituindo a inferência local baseada no status efêmero por piloto.

### Modified Capabilities
- `zona-frenagem-marcas-intensidade`: adiciona o requisito de que `geraTravadaRoda` possa gerar eventos de marca-só e de marca+fumaça de forma independente (não mais sempre acoplados), restaurando a distinção que os renderers (Swing e web) esperam.

## Impact

- **Código afetado (web/JS)**: `src/main/webapp/html5/js/jogar.js` (dispara preload ao selecionar/confirmar circuito), `mid.js` (`mid_carregaBackGroundCorrida`), `cpu.js` (`cpu_dadosParciais`, mapas `pilotosTravadaMap`/`pilotosTravadaFumacaMap`), `vdp.js` (`vdp_desenhaTravadaRoda`, `vdp_desenhaTravadaRodaFumaca`, `borrachaNaPista`).
- **Código afetado (servidor/Java)**: `ControleJogoLocal.java` (`geraTravadaRoda`), `ControleJogosServer.java` (`gerarPosicaoPilotos`, payload de posição/`Posis`/`PosisPack`), `LetsRace.java` (endpoint REST de posição), `br.f1mane.servidor.entidades.TOs.TravadaRoda` (reaproveitado para o caminho web).
- **Sem alteração no caminho Swing solo** além de, indiretamente, voltar a permitir que `desenhaFumacaTravaRodaCarroCima` desenhe fumaça em eventos que não sejam marca-só (o guard existente já espera essa distinção).
- **Testes**: novos testes cobrindo geração independente de marca-só vs. marca+fumaça em `ControleJogoLocal`, e cobertura (manual/QA, já que é client-side JS) do preload de background e do consumo de marcas por nó no cliente web.
