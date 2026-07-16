## Why

O cliente web (`src/main/webapp/html5/*.html`) usa uma paleta de cores sem sistema de tema (poucas cores fixas em `flmane.css`, sem variáveis CSS), o que deixa componentes com baixo contraste e pouca distinção visual entre estados. Botões de ação que só aparecem em resposta a uma seleção do jogador (ex.: botão de iniciar jogo após escolher piloto) surgem sem nenhuma pista visual, deixando fácil perder a mudança de estado. Além disso, os seletores de temporada e circuito em `jogar.html`, `campeonato.html`, `equipe.html`, `resultado.html` e `classificacao_temporada.html` são implementados como carousels Bootstrap 3 (`data-ride="carousel"`); em `classificacao_temporada.html` falta a mesma trava `interval:false` presente nas demais páginas, então o carousel de temporada avança sozinho a cada ~5s e troca a temporada selecionada sem ação do usuário — um comportamento não intencional que precisa ser eliminado em todas as páginas, de forma preventiva, não só corrigido onde já foi observado.

## What Changes

- Introduzir uma paleta de tons pastéis com variáveis CSS (`:root`) em `flmane.css`, substituindo as ~9 cores fixas atuais, com contraste suficiente para texto e bordas de componentes (WCAG AA como referência).
- Padronizar a inclusão de `flmane.css`/`mdb-btns.css` em todas as páginas html5 (hoje `jogar.html`, `equipe.html`, `corrida.html` e `controles.html` não carregam `mdb-btns.css`, e `corrida.html` nem carrega `bootstrap.min.css`), para que o novo tema se aplique de forma consistente.
- Aumentar o destaque/contraste visual de componentes interativos (botões, painéis, itens selecionáveis) em relação ao fundo.
- Adicionar uma animação de "surgimento" (ex.: leve salto ou piscada, via CSS `@keyframes` + classe aplicada no momento da transição) em qualquer botão de ação que passe a ficar visível/ativo em resposta a uma mudança de estado (ex.: `#btnJogar` em `jogar.js`, `#criaJogo` ao selecionar piloto, toggles de `#info`/`#imgJog1`/`#imgJog2` em `ctl.js`), reaproveitando os pontos já existentes de `.show()`/`.removeClass('hide')` em vez de introduzir um novo mecanismo de state.
- Corrigir o auto-avanço do carousel de temporada em `classificacao_temporada.html`/`classificacao_temporada.js`, aplicando o mesmo padrão de guarda (`pause:true, interval:false`) já usado em `jogar.js`/`campeonato.js`/`equipe.js`, e auditar `resultado.html` (que tem `data-ride="carousel"` sem nenhuma guarda em JS) para aplicar a mesma trava preventivamente, mesmo hoje não sendo populado com múltiplos slides.
- Garantir, como regra geral e não caso a caso, que nenhum carousel de seleção de temporada/circuito no cliente web possa iniciar auto-rotação: a seleção do usuário (ou o valor default carregado) deve permanecer estável até uma ação explícita do usuário.

## Capabilities

### New Capabilities
- `web-ui-tema-pastel`: paleta de cores pastel com variáveis CSS, aplicada de forma consistente em todas as páginas html5, com regras de contraste mínimo para texto e componentes interativos.
- `web-ui-animacao-aparecimento-botao`: animação de destaque (salto/piscada) aplicada a qualquer botão de ação que transicione de oculto/inativo para visível/ativo.
- `web-selecao-preemptiva-temporada-circuito`: seletores de temporada e circuito (carousels) nunca avançam sozinhos — a seleção permanece exatamente a que o usuário fez (ou o default carregado) até uma ação explícita.

### Modified Capabilities
(nenhuma — esta mudança não altera requisitos de capacidades existentes, apenas introduz as duas novas capacidades acima)

## Impact

- CSS: `src/main/webapp/html5/css/flmane.css` (novo sistema de variáveis de tema), `css/mdb-btns.css`, inclusão desses arquivos nas páginas que hoje não os carregam.
- HTML: `jogar.html`, `campeonato.html`, `equipe.html`, `resultado.html`, `classificacao_temporada.html`, `classificacao_circuito.html`, `corrida.html`, `controles.html`, `configuracao.html`, `index.html`, `classificacao*.html` (aplicação do tema e, onde aplicável, revisão da marcação dos carousels).
- JS: `js/jogar.js`, `js/campeonato.js`, `js/equipe.js`, `js/classificacao_temporada.js`, `js/classificacao_circuito.js`, `js/resultado.js`, `js/ctl.js` (guarda de carousel + classes de animação nos pontos de show/hide de botão).
- Nenhum impacto em backend, API REST (`LetsRace`) ou modelo de dados — mudança restrita ao cliente web estático.
