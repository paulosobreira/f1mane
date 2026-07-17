# Ícones genéricos em alta resolução (pneus, clima, faróis, safety car, fuel)

## Why

Os ícones de HUD do jogo (`src/main/resources/png/`) têm dois problemas: os pneus (`pneuMole.png`, `pneuDuro.png`, `pneuChuva.png`) exibem marcas registradas ("Pirelli", "P Zero", "Cinturato"), o que é um risco legal para distribuição; e vários ícones (`sol.png`, `lua.png`, `nublado.png`, `chuva.png`, `fuel.png`, `sfcima.png`, `farois*.png`) são bitmaps de baixa resolução com recorte ruim e artefatos de compressão, destoando do redesign visual recente do cliente web.

## What Changes

- **Pneus sem marca**: novas artes genéricas (sem qualquer marca registrada) para `pneuMole.png` (faixa amarela), `pneuDuro.png` (faixa branca) e `pneuChuva.png` (faixa azul), geradas a partir de masters em alta resolução e entregues nos tamanhos atuais (142×142, normalizando o `pneuMole.png` que hoje é 144×142).
- **Versões menores dos pneus**: `pneuMoleMenor.png`, `pneuDuroMenor.png`, `pneuChuvaMenor.png` (44×44, normalizando o `pneuMoleMenor.png` que hoje é 44×43) passam a ser geradas por downscale de alta qualidade (Lanczos) dos mesmos masters — hoje são cópias reduzidas independentes. Os resizes em runtime do Swing (`geraResize(0.3)` e `geraResize(0.15)` em `PainelCircuito`/`PainelMenuLocal`) continuam funcionando pois os tamanhos embarcados não mudam.
- **Ícones de clima**: `sol.png`, `lua.png`, `nublado.png`, `chuva.png` redesenhados em estilo flat com recorte correto (conteúdo ocupando o quadro), gerados de masters em alta resolução, mantendo 35×35 embarcado.
- **Fuel em tons de cinza**: `fuel.png` substituído por versão flat em tons de cinza, gerada de master em alta resolução, mantendo 25×25.
- **Remoção de `tyre.png`**: nenhuma referência no código Java, web ou HTML — arquivo removido.
- **Safety car de cima**: `sfcima.png` regenerado a partir de master em alta resolução, mantendo 90×90 e a orientação atual (é desenhado como sprite de carro tanto no Swing quanto no canvas web via `/letsRace/png/sfcima`).
- **Faróis de largada flat**: `farois-apagados.png`, `farois1.png` … `farois4.png` e `farois.png` (5 acesos) redesenhados em estilo flat condizente com os gráficos do jogo, gerados de masters em alta resolução, mantendo 150×89.
- **Pipeline de geração**: novo script `utilitarios/gerar_icones.py` (mesmo padrão dos `gerar_cima_*.py`/`gerar_spritesheets.py` existentes) desenha os masters com supersampling e produz os PNGs embarcados nos tamanhos exatos esperados pelo jogo. Masters ficam fora de `src/main/resources` para não inflar o fat jar.
- **Contrato de dimensões**: teste unitário garante que cada PNG embarcado mantém as dimensões esperadas pelo rendering (Swing desenha a maioria em tamanho natural e o cliente web também).

## Capabilities

### New Capabilities

- `icones-hud-genericos`: ícones de HUD (pneus, clima, combustível, faróis de largada, safety car de cima) livres de marca registrada, em estilo flat, gerados a partir de masters de alta resolução por script reproduzível, embarcados nas dimensões exatas que o rendering Swing e o cliente web esperam.

### Modified Capabilities

_Nenhuma — `sdd-rendering` descreve os buffers de imagem em nível de arquitetura e não muda; a troca é de assets e pipeline, sem alteração de requisitos existentes._

## Impact

- **Assets**: todos em `src/main/resources/png/` — `pneuMole.png`, `pneuDuro.png`, `pneuChuva.png`, `pneuMoleMenor.png`, `pneuDuroMenor.png`, `pneuChuvaMenor.png`, `sol.png`, `lua.png`, `nublado.png`, `chuva.png`, `fuel.png`, `sfcima.png`, `farois.png`, `farois1.png`–`farois4.png`, `farois-apagados.png`; remoção de `tyre.png`.
- **Código**: nenhuma mudança funcional esperada em `PainelCircuito`, `PainelMenuLocal` ou `LetsRace` — os consumidores carregam por nome e desenham em tamanho natural ou com resize relativo; as dimensões embarcadas são preservadas.
- **Web**: `mid.js`/`vdp.js` consomem os mesmos endpoints `/letsRace/png/{recurso}` sem alteração; `controles.html`, `jogar.html` e `resultado.js` continuam usando `pneu*Menor`.
- **Novo**: `utilitarios/gerar_icones.py` + diretório de masters em alta resolução (`utilitarios/icones_hires/`); teste de dimensões dos PNGs.
