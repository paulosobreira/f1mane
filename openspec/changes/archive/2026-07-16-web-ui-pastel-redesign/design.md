## Context

O cliente web (`src/main/webapp/html5/`) é HTML/jQuery/Bootstrap 3 servido estaticamente pelo Tomcat embutido (ver `MainLauncher`). Hoje:

- `flmane.css` (285 linhas) tem ~9 cores hexadecimais fixas, sem `:root`/variáveis CSS — não há sistema de tema para reaproveitar.
- Nem toda página carrega o mesmo conjunto de folhas de estilo: `jogar.html`, `equipe.html`, `corrida.html` e `controles.html` não incluem `mdb-btns.css`; `corrida.html` nem inclui `bootstrap.min.css`.
- Botões de ação aparecem/desaparecem via jQuery puro (`.show()`/`.hide()`, `.addClass('hide')`/`.removeClass('hide')`) espalhado em `js/jogar.js` e `js/ctl.js` — não há um único "componente de botão" centralizado.
- Os seletores de temporada e circuito são carousels Bootstrap 3 (`#temporadaCarousel`, `#circuitoCarousel`) populados via AJAX. `data-ride="carousel"` faz o Bootstrap reativar o carousel no evento `load` da janela com intervalo padrão de 5000ms, a menos que `interval:false` seja passado explicitamente. `jogar.js`, `campeonato.js` e `equipe.js` já fazem essa guarda; `classificacao_temporada.js` não faz, causando auto-avanço real quando há 2+ temporadas.

## Goals / Non-Goals

**Goals:**
- Trocar a paleta de cores do tema por tons pastéis com contraste adequado, usando variáveis CSS centralizadas.
- Uniformizar quais folhas de estilo cada página carrega, para que o novo tema valha em todas as telas.
- Adicionar uma animação de destaque reaproveitando os pontos de código já existentes que mostram/escondem botões — sem reescrever a lógica de estado dos botões.
- Eliminar de forma preventiva (não só corretiva) qualquer possibilidade de um carousel de temporada/circuito avançar sozinho, em todas as páginas que os usam.

**Non-Goals:**
- Não é objetivo migrar o layout de Bootstrap 3 para outro framework, nem trocar os carousels por um componente de seleção diferente (ex.: `<select>` nativo) — a correção é impedir o auto-avanço, mantendo a UI de carousel.
- Não altera nenhuma lógica de jogo, física de corrida ou API REST (`LetsRace`).
- Não introduz um design system completo (tokens tipográficos, espaçamento, etc.) além do necessário para cor/contraste e a animação de botão.
- **Não altera nada em `corrida.html` nem em `js/ctl.js`** (decisão explícita do usuário durante a implementação): o canvas de corrida (renderização em tempo real do HUD, avatares dos jogadores) fica totalmente fora do escopo. `corrida.html` não recebe `mdb-btns.css`/`bootstrap.min.css`, e `#info`/`#imgJog1`/`#imgJog2` (atualizados a cada tick do loop de renderização) não recebem a animação de destaque — só os botões das telas de menu (fora do canvas) são animados.

## Decisions

**1. Variáveis CSS em `:root` dentro de `flmane.css`, em vez de um pré-processador (Sass/Less).**
O projeto não tem pipeline de build de CSS hoje (arquivos `.css` estáticos servidos diretamente do `webapp/`). Introduzir Sass exigiria um passo de build novo no Maven só para isso. Variáveis CSS nativas resolvem o problema (tema centralizado, fácil de ajustar tons pastéis) sem dependência nova.

**2. Paleta pastel definida como pequeno conjunto de variáveis semânticas** (`--cor-fundo`, `--cor-fundo-alt`, `--cor-texto`, `--cor-borda`, `--cor-destaque`, `--cor-perigo`, etc.), não uma paleta de tons numerados (ex.: `--pastel-100`...`--pastel-900`).
Como o site tem poucas telas e componentes, nomes semânticos mantêm o CSS existente fácil de migrar (troca de valor hex fixo pela variável correspondente) sem exigir reescrever a estrutura de classes.

**3. Uniformizar `<link>` de CSS copiando o `<head>` das páginas já corretas** (`campeonato.html`/`classificacao*.html`) para as que faltam (`jogar.html`, `equipe.html`, `corrida.html`, `controles.html`), em vez de extrair um template/include compartilhado.
O projeto não tem sistema de templates server-side para o HTML5 (são arquivos estáticos); criar um mecanismo de include novo é desproporcional ao escopo desta mudança.

**4. Animação de botão via classe CSS com `@keyframes`, adicionada/removida no mesmo ponto de código que já chama `.show()`/`.removeClass('hide')`**, em vez de um observer de mutação de DOM (`MutationObserver`) genérico.
Os pontos de show/hide já são conhecidos e finitos (`jogar.js:93-111`, `jogar.js:207-208`, `ctl.js:374-383,627,662`). Adicionar a classe de animação ali é mais simples e previsível do que observar mudanças de estilo/classe genericamente, e evita reanimar elementos por mudanças de DOM não relacionadas à intenção do jogador.
A classe usa `animation` com uma keyframe curta (ex.: `bounce-in` ou `pulse`) e remove a classe ao final da animação (evento `animationend`) para permitir reaplicação em aparições futuras do mesmo botão.

**5. Correção do carousel: aplicar a guarda `pause:true, interval:false` já usada em `jogar.js`/`campeonato.js`/`equipe.js` também em `classificacao_temporada.js` e em `resultado.html`/`resultado.js`**, em vez de remover `data-ride="carousel"` do HTML.
Manter `data-ride` e neutralizar via JS replica exatamente o padrão que já funciona nas outras três páginas, reduzindo o risco de comportamento divergente entre páginas. Remover o atributo HTML funcionaria também, mas a convenção do próprio código já é "manter o atributo, mas sempre pausar/desabilitar intervalo via JS" — seguir a convenção existente.

## Nota de implementação

Descoberto durante a implementação: `src/main/webapp/html5/*.html` **não é a fonte editável** — a execução `src_html` do `maven-antrun-plugin` (`pom.xml`) copia `src/main/webapp/htm5lsrc/*.html` sobre `src/main/webapp/html5/*.html` a cada `mvn package` (substituindo o token `{versao}` pelo número de build em seguida). Edições feitas diretamente em `html5/*.html` são descartadas no próximo build. Todas as edições de HTML desta mudança foram feitas em `htm5lsrc/`. Além disso, como o bundling de recursos do fat jar ocorre na fase `process-resources` (antes da fase `package`, onde o antrun sincroniza `htm5lsrc/` → `html5/`), uma alteração em `htm5lsrc/` só aparece no jar gerado a partir do **segundo** `mvn package` executado após a alteração (o primeiro grava a sincronização em disco; o segundo empacota o resultado já sincronizado).

## Risks / Trade-offs

- [Risco] Trocar cores fixas por variáveis pode quebrar contraste em combinações não testadas (ex.: texto de erro sobre fundo pastel claro) → Mitigação: revisar cada componente que usa cor de texto/fundo customizada durante a implementação e validar contraste manualmente (regra dos 4.5:1) antes de finalizar.
- [Risco] Adicionar `mdb-btns.css`/`bootstrap.min.css` em páginas que hoje não os carregam pode alterar estilos de botões/layout já existentes nessas páginas (ex.: `corrida.html`, que roda durante a corrida e é sensível a performance/layout) → Mitigação: testar visualmente cada página afetada após a inclusão, ajustando overrides pontuais em `flmane.css` se necessário.
- [Risco] A guarda de carousel em `classificacao_temporada.js`/`resultado.js` pode não ser suficiente se o Bootstrap 3 recriar o carousel após população AJAX de forma diferente do padrão das outras páginas → Mitigação: replicar exatamente as chamadas defensivas de `.carousel('pause')` já usadas em `jogar.js` após cada população via AJAX, não só na inicialização.
- [Trade-off] Manter os carousels (em vez de substituí-los por `<select>`) preserva a experiência visual atual, mas mantém uma dependência de comportamento do Bootstrap 3 que exige essa guarda manual em todo lugar novo que reusar o componente — deve ficar documentado (comentário curto no JS) para não reintroduzir o bug em uma página futura.

## Migration Plan

1. Introduzir as variáveis de tema em `flmane.css` e migrar as cores fixas existentes para elas, uma por uma, validando visualmente cada tela.
2. Uniformizar as tags `<link>` de CSS/JS nas páginas que hoje divergem.
3. Adicionar a classe/keyframe de animação de botão e aplicá-la nos pontos de show/hide já mapeados.
4. Aplicar a guarda de carousel em `classificacao_temporada.js` e `resultado.html`/`resultado.js`.
5. Validar manualmente no navegador (não há testes automatizados de UI no projeto): navegar por todas as páginas listadas no Impact do proposal, confirmar contraste, animação de botão e ausência de auto-avanço do carousel.

Não há rollback automatizado necessário: são mudanças em arquivos estáticos (HTML/CSS/JS) sem migração de dados; reverter é reverter o commit.

## Ajuste pós-implementação

Usuário testou o build real e não gostou do tom azulado (`--cor-neutro-escuro`/`--cor-neutro`) aplicado aos botões flutuantes (`.floatBtn`/`.floatSalvar`/`.relativeBtn`, fixados na parte inferior da tela) e ao fallback de fundo em modo retrato. Revertidos para os valores originais (`gray`/`#FFF`/`black`) em `flmane.css`, mantendo a animação `destaque-surgimento` intacta e o restante da paleta pastel (fundo, texto, bordas, cores de destaque) sem alteração.

## Open Questions

- Paleta pastel exata (tons específicos) — a ser definida durante a implementação com base em capturas de tela das telas atuais, buscando o maior contraste possível dentro da estética pastel pedida.
- Tipo exato de animação (salto vs. piscada) por botão — o proposal aceita qualquer uma das duas; a decisão final fica a critério da implementação, podendo variar por contexto (ex.: piscada para alertas, salto para novas ações).
