# Design — Ícones genéricos em alta resolução

## Context

Os ícones de HUD vivem em `src/main/resources/png/` e são consumidos por três caminhos:

1. **Swing** (`PainelCircuito`, `PainelMenuLocal`): a maioria é carregada e desenhada em **tamanho natural** (`g2d.drawImage(img, x, y, null)`) — `sol/lua/nublado/chuva` (35×35), `fuel` (25×25), `sfcima` (90×90), `farois*` (150×89). Os pneus grandes (142×142) são redimensionados **em runtime** por fator relativo: `ImageUtil.geraResize(img, 0.3)` (~43 px) e `geraResize(img, 0.15)` (~21 px, minis do mapa).
2. **Cliente web** via REST `LetsRace` `GET /letsRace/png/{recurso}` ([LetsRace.java:624](src/main/java/br/f1mane/servidor/rest/LetsRace.java:624)), que carrega `png/{recurso}.png` do classpath e devolve o bitmap. O canvas web desenha em tamanho natural (ex.: `vdp.js:1388` usa `imgFarois.width` para centralizar; `sfcima` é usado como sprite de carro em `vdp.js:783`). `controles.html`/`jogar.html`/`resultado.js` usam `pneu*Menor` (44×44) em `<img>`.
3. **Carga com transparência**: `carregaBufferedImageTransparecia(file, null)` (CarregadorRecursos.java:924) com `cor = null` apenas converte para `TYPE_INT_ARGB` copiando as 4 bandas do raster — se o PNG fonte não tiver canal alfa, a banda 3 fica 0 e a imagem sai invisível. **Todos os PNGs novos devem ser exportados como RGBA.**

Problemas atuais: pneus com marcas registradas (Pirelli/P Zero/Cinturato); ícones de clima/fuel/safety car/faróis com baixa resolução, recorte com margens desperdiçadas e artefatos; `pneuMole.png` com dimensão inconsistente (144×142 vs. 142×142 dos irmãos; `pneuMoleMenor` 44×43); `tyre.png` órfão (zero referências em Java, JS e HTML).

Precedente no repo: arte gerada por script Python com Pillow em `utilitarios/` (`gerar_cima_*.py`, `gerar_spritesheets.py`).

## Goals / Non-Goals

**Goals:**
- Eliminar marcas registradas dos pneus.
- Elevar a qualidade visual (estilo flat, recorte pleno, sem artefatos) mantendo **exatamente** as dimensões embarcadas que o rendering espera.
- Pipeline reproduzível: um script gera masters em alta resolução e os PNGs do jogo por downscale de alta qualidade.
- Remover o asset morto `tyre.png`.
- Proteger o contrato de dimensões com teste unitário.

**Non-Goals:**
- Nenhuma mudança de comportamento em `PainelCircuito`, `PainelMenuLocal`, `LetsRace`, `mid.js`, `vdp.js` — nem de layout do HUD.
- Não redesenhar outros assets (`flags.png`, `maisAsa/menosAsa`, `GridCarro.png`, sprites de carros, `travadaRoda*`, etc.).
- Não converter o rendering para consumir imagens em alta resolução com scaling dinâmico (ex.: HiDPI) — fica para o futuro.

## Decisions

### D1 — Manter as dimensões embarcadas; alta resolução vive nos masters
Os PNGs em `src/main/resources/png/` continuam nos tamanhos atuais (com normalização: `pneuMole.png` 144×142 → 142×142, `pneuMoleMenor.png` 44×43 → 44×44). A alta resolução entra como **masters** (≥512 px no menor lado) que o script reduz com Lanczos para o tamanho do jogo.

*Alternativa rejeitada*: embarcar hi-res e redimensionar no load. Quebraria o cliente web (canvas desenha em tamanho natural — os faróis apareceriam 8× maiores) e o Swing (idem), exigindo mudanças coordenadas em Java + JS sem ganho visual real, já que o downscale offline com Lanczos é superior ao resize bilinear em runtime.

O código Java segue intocado: `geraResize(0.3)`/`geraResize(0.15)` continuam corretos porque a base 142×142 não muda.

### D2 — Geração programática com Pillow (supersampling), não arte manual/externa
Novo `utilitarios/gerar_icones.py` desenha cada ícone vetorialmente com Pillow em canvas supersampled (8× o tamanho do master) e reduz com `Image.LANCZOS` duas vezes: supersample → master (gravado em `utilitarios/icones_hires/`) e master → tamanho do jogo (gravado em `src/main/resources/png/`). Determinístico, versionável, re-executável — mesmo padrão dos `gerar_cima_*.py`.

*Alternativa rejeitada*: arte desenhada à mão ou gerada por IA como binário sem fonte — não reproduzível e sem trilha de como o asset surgiu (relevante justamente por causa do histórico de marca registrada).

### D3 — Masters fora de `src/main/resources`
Masters em `utilitarios/icones_hires/` para não inflar o fat jar (o Maven empacota tudo de `resources`). O jar embarca só os tamanhos de jogo.

### D4 — Linguagem visual dos novos ícones
- **Pneus** (142×142 + Menor 44×44, RGBA): pneu visto de frente — anel externo preto (borracha) com **faixa lateral colorida** (amarela = mole, branca = duro, azul = chuva, mantendo o código de cores já usado no jogo), aro cinza com raios genéricos. **Nenhum texto** na lateral — marcações neutras (traços/segmentos) no lugar de logotipos. Fundo transparente.
- **Clima** (35×35, RGBA): flat, conteúdo ocupando ~90% do quadro. `sol` = disco amarelo com raios; `lua` = crescente claro (legível sobre céu escuro); `nublado` = nuvem cinza-clara sobreposta a sol parcial; `chuva` = nuvem cinza com gotas azuis.
- **Fuel** (25×25, RGBA): bomba de combustível flat em **tons de cinza** exclusivamente.
- **Safety car** (`sfcima`, 90×90, RGBA): carro de rua visto de cima, estilo flat compatível com os sprites `gerar_cima_*.py`, corpo prata/branco com detalhes escuros, **mesma orientação (heading) do sprite atual** — validar por comparação lado a lado antes de substituir, pois `vdp.js` e o Swing o tratam como sprite de carro e o rotacionam.
- **Faróis de largada** (150×89, RGBA): painel flat — 5 hastes/colunas escuras com luzes circulares vermelhas. Estados: `farois-apagados` (todas apagadas — vermelho escuro dessaturado), `farois1`…`farois4` (1 a 4 colunas acesas — vermelho vivo), `farois` (5 acesas). Mesma geometria em todos os 6 arquivos para o cross-fade da largada não "pular".

### D5 — Remoção de `tyre.png` sem substituto
Zero referências no código (a busca por `tyre` só encontra strings de i18n e o readme). O endpoint genérico `/png/{recurso}` poderia teoricamente servi-lo, mas nenhum cliente o requisita. Remover o arquivo; nenhuma mudança de código necessária.

### D6 — Teste de contrato de dimensões
Novo teste JUnit (ex.: `IconesPngDimensoesTest` em `src/test/java/br/f1mane/recursos/`) que carrega cada PNG do conjunto via `CarregadorRecursos`/`ImageIO` e afirma as dimensões esperadas (142×142, 44×44, 35×35, 25×25, 90×90, 150×89) e a presença de canal alfa nos que passam por `carregaBufferedImageTransparecia`. Protege regenerações futuras do script contra quebra silenciosa do HUD. Sem Swing — só decodificação de imagem (regra do projeto sobre testes).

## Risks / Trade-offs

- [Orientação errada do `sfcima` quebra o safety car girando "de lado" na pista] → comparar heading com o sprite atual antes de substituir; conferir em `simulacao.sh` (headless) e/ou partida local com safety car forçado.
- [PNG exportado sem canal alfa some no Swing (banda 3 = 0 em `carregaBufferedImageTransparecia`)] → script sempre exporta RGBA; teste de contrato afirma `image.getColorModel().hasAlpha()`.
- [Julgamento estético é subjetivo — flat pode destoar do restante do HUD] → gerar primeiro, revisar screenshots (menu local + corrida + web `controles.html`/`teste-png.html`) antes de considerar concluído; iterar no script é barato.
- [Cache de imagens de `CarregadorRecursos` e cache do navegador podem mostrar assets antigos durante validação] → reiniciar o jogo/servidor após regenerar e usar hard-refresh no browser.
- [Cross-fade dos faróis na largada (`vdp.js:1379-1404`) alterna os 6 PNGs assumindo mesmo enquadramento] → o script desenha os 6 estados a partir da mesma geometria base, mudando só o estado das luzes.

## Migration Plan

1. Adicionar script + masters + novos PNGs num único commit (assets são substituídos in-place; nomes e caminhos idênticos).
2. Remover `tyre.png` no mesmo commit.
3. Rollback = reverter o commit (assets antigos voltam; nenhum schema/API envolvido).

## Open Questions

- Marcações neutras na lateral do pneu: só traços, ou texto genérico tipo "SOFT/HARD/WET"? Default: **sem texto** (traços), por ser mais limpo em 43 px e evitar qualquer discussão de trade dress.
