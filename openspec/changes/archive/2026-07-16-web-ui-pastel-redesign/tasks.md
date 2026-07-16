## 1. Tema pastel — variáveis CSS

- [x] 1.1 Definir a paleta pastel (fundo, fundo alternativo, texto, borda, destaque, perigo/erro) como variáveis CSS em `:root` no topo de `src/main/webapp/html5/css/flmane.css`, escolhendo valores com contraste mínimo 4.5:1 texto/fundo e 3:1 para bordas de componentes
- [x] 1.2 Substituir, uma a uma, as ~9 cores hexadecimais fixas hoje existentes em `flmane.css` pelas variáveis correspondentes
- [x] 1.3 Revisar `css/mdb-btns.css` e ajustar cores de botão hardcoded (se houver) para usar as mesmas variáveis, mantendo o estilo Material dos botões
- [x] 1.4 Validar contraste texto/fundo e bordas em cada tela após a troca de paleta — validado via `getComputedStyle`/valores de variáveis CSS no navegador (captura de screenshot indisponível nesta sessão, ver observação em 5.1)

## 2. Uniformizar folhas de estilo entre páginas

- [x] 2.1 Adicionar `<link>` para `css/mdb-btns.css` em `jogar.html`, `equipe.html`, `controles.html` e `resultado.html` (gap adicional encontrado durante a implementação). `corrida.html` deliberadamente excluída — ver 2.2. IMPORTANTE: descoberto durante a implementação que `src/main/webapp/html5/*.html` é sobrescrito a cada `mvn package` a partir de `src/main/webapp/htm5lsrc/*.html` (execução `src_html` do maven-antrun-plugin no `pom.xml`) — todas as edições de HTML foram feitas em `htm5lsrc/`, não diretamente em `html5/`
- [x] 2.2 RESOLVIDA (não fazer): usuário confirmou não adicionar `bootstrap.min.css` nem `mdb-btns.css` a `corrida.html` — é a página de renderização da corrida em tempo real (canvas puro), não usa nenhum componente Bootstrap hoje, e não deve ser alterada (ver Non-Goals em design.md)
- [x] 2.3 Verificar cada página afetada após a inclusão das folhas de estilo — confirmado via fetch/DOM que `mdb-btns.css` carrega corretamente em `jogar.html`, `equipe.html`, `controles.html` e `resultado.html`, que `corrida.html` permanece intocada, e que a paleta pastel (`--cor-fundo`, etc.) resolve corretamente em runtime; `resultado.html` também precisou de `bootstrap.min.js` (faltava por completo, seria quebrado por 4.3 sem esse ajuste — ver nota em 4.3)

## 3. Animação de aparecimento de botão

- [x] 3.1 Criar classe CSS (`@keyframes` + classe, ex. `.destaque-surgimento`) em `flmane.css` com animação curta (salto ou piscada), removida automaticamente no fim (`animationend`) para permitir reaplicação futura — implementada via helper `destacarAoAparecer($el)` em `js/util.js`
- [x] 3.2 Aplicar a classe em `js/jogar.js` no ponto onde `#criaJogo` é exibido (`removeClass('hide')`, linha ~207-208)
- [x] 3.3 Aplicar a classe em `mostrarEntrarJogo()` (`js/jogar.js:93-111`) quando `#btnJogar` passa a ser exibido
- [x] 3.4 RESOLVIDA (não fazer): usuário confirmou não animar nada dentro do canvas de corrida — `#info`/`#imgJog1`/`#imgJog2` em `js/ctl.js` são atualizados a cada tick do loop de renderização (`ctl_desenhaInfoBaixo`/`ctl_desenhaInfoCarros`), não em resposta a uma seleção pontual; `js/ctl.js` e `corrida.html` permanecem intocados. Animação de destaque fica restrita às telas de menu
- [x] 3.5 Testado no navegador (build real, `jogar.html`): `#criaJogo` inicia oculto, `destacarAoAparecer()` aplica a classe `destaque-surgimento` e `animation-name` computado confirma a keyframe correta

## 4. Corrigir auto-avanço dos carousels de temporada/circuito

- [x] 4.1 Aplicar `$('.carousel').carousel({pause:true, interval:false})` em `js/classificacao_temporada.js`, seguindo o mesmo padrão de `js/jogar.js:88-91`
- [x] 4.2 Adicionar chamadas defensivas `.carousel('pause')` após cada população via AJAX em `js/classificacao_temporada.js` (equivalente a `jogar.js:588-589,660,663,694-695`) — já existiam nesse arquivo, confirmado sem alteração necessária
- [x] 4.3 Aplicar a mesma guarda (`interval:false` + `pause` defensivo) em `resultado.html`/`js/resultado.js`, mesmo que hoje só exista um slide — descoberto que `resultado.html` nunca carregou `bootstrap.min.js` (o carousel era inerte); adicionado o script junto da guarda para a proteção ter efeito real
- [x] 4.4 Revisar `classificacao_circuito.html`/`js/classificacao_circuito.js` para confirmar que o carousel de circuito também está protegido, aplicando a mesma guarda se estiver faltando — `data-ride="carousel"` já não estava presente nesse carousel (seguro), guarda `interval:false` adicionada mesmo assim por padronização
- [x] 4.5 Testado no navegador (build real): `classificacao_temporada.html` e `jogar.html` — aguardado 7s+ sem interação, temporada/label selecionados permaneceram estáveis, `paused:true` e nenhum interval ativo. `resultado.html` verificado estaticamente (ordem correta de scripts, `bootstrap.min.js` 200 OK, plugin `$.fn.carousel` confirmado funcional neste ambiente) — não foi possível manter a página carregada tempo suficiente para o teste ao vivo dos 7s sem uma sessão de jogo válida (a página redireciona rapidamente sem `token`/`nomeJogo` reais)

## 5. Validação final

- [x] 5.1 Percorrido no navegador (build real, servidor local): tema pastel, contraste (variáveis CSS resolvidas), animação de botão e ausência de auto-avanço de carousel confirmados via DOM/computed styles e network requests. OBSERVAÇÃO: a ferramenta de screenshot do navegador não respondeu (timeout) durante toda a sessão de verificação, mesmo em páginas simples — não foi possível anexar capturas visuais; a validação foi feita por inspeção programática (getComputedStyle, classes, instâncias do plugin carousel)
- [x] 5.2 `mvn test` executado com sucesso (exit code 0) após as alterações
