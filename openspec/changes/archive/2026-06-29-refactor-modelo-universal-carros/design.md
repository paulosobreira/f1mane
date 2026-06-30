## Context

`CarregadorRecursos` usa três camadas para obter o visual de um carro:
1. **Spritesheet da temporada** (`SpriteSheet`) — preferido, pré-gerado por `gerar_spritesheets.py`
2. **PNG individual por equipe** (`carros/<season>/<team>.png`) — quando não há sprite
3. **Compositing por máscara** — fallback final usando `CarroLado.png` + C1/C2/C3 ou `png/cimaNNNN/CarroCima.png` + C1/C2/C3

O terceiro nível é completamente substituído pelo pipeline v2. Os assets antigos do nível 3 são removidos do repositório.

**Assets antigos a remover:**
- Diretórios: `png/cima19701979/`, `png/cima19801997/`, `png/cima19982008/`, `png/cima20092016/`, `png/cima2017/` (cada um com `CarroCima.png`, `CarroCimaC1.png`, `CarroCimaC2.png`, `CarroCimaC3.png`)
- Arquivos: `png/Capacete.png`, `png/CapaceteC1.png`, `png/CapaceteC2.png`, `png/CarroLado.png`, `png/CarroLadoC1.png`, `png/CarroLadoC2.png`, `png/CarroLadoC3.png`, `png/CarroLadoDef.png`

**Assets novos (já existem como untracked):**
- `png/carro-lado-v2.png` — silhueta lateral; verde = cor1, branco = cor2
- `png/carro-cima-v2.png` — silhueta top-down; verde = cor1, branco = cor2
- `png/capacete-v2.png` — capacete genérico; verde = cor1, branco = cor2

## Goals / Non-Goals

**Goals:**
- Novo método estático `pintarModeloV2(String assetPath, Color cor1, Color cor2, int targetW, int targetH): BufferedImage`
- Refatorar todos os métodos `desenha*` para usar `pintarModeloV2`
- Remover `obterModeloCarroCima()` e toda a lógica de seleção de modelo por era
- Remover os arquivos físicos de modelo antigo via `git rm`
- Atualizar `FormCarreira.gerarCarroCima()` e `gerarCarroLado()` para v2
- `Global.FORCE_MODELO_V2`: quando `true`, o pipeline v2 é ativado no topo de `obterCarroLado/Cima/Capacete`, ignorando spritesheets e PNGs individuais

**Non-Goals:**
- Alterar `SpriteSheet.java` — spritesheets continuam sendo a opção preferida
- Alterar `gerar_spritesheets.py` — o gerador Python não é impactado
- Criar variantes do modelo v2 para diferentes eras

## Decisions

**Algoritmo `pintarModeloV2`:**
```
1. Carregar asset v2 via carregaBufferedImageTransparecia(assetPath) → srcImg (resolução nativa)
2. Criar destImg = new BufferedImage(srcW, srcH, TYPE_INT_ARGB)
3. Para cada pixel (x, y):
   - Ler ARGB do srcImg
   - Se alpha == 0: copiar pixel transparente
   - Se verde (R<100 && G>150 && B<100): substituir RGB por cor1, manter alpha
   - Se branco (R>200 && G>200 && B>200): substituir RGB por cor2, manter alpha
   - Caso contrário: copiar pixel original
4. Calcular escala = min(targetW / srcW, targetH / srcH)
5. Se srcW == targetW && srcH == targetH: retornar destImg diretamente
6. Caso contrário:
   - scaledW = (int)(srcW * escala), scaledH = (int)(srcH * escala)
   - scaledImg = destImg.getScaledInstance(scaledW, scaledH, SCALE_SMOOTH)
   - result = new BufferedImage(targetW, targetH, TYPE_INT_ARGB) — transparente
   - Desenhar scaledImg em result centrado: offsetX=(targetW-scaledW)/2, offsetY=(targetH-scaledH)/2
   - Retornar result
```

**Cadeia de fallback após refatoração:**
```
obterCarroLado(piloto, temporada):
  if FORCE_MODELO_V2 → pintarModeloV2("png/carro-lado-v2.png", cor1, cor2, LADO_W, LADO_H)
  else if SpriteSheet disponível → SpriteSheet.getCarroLado(temporada, idx)
  else if PNG individual existe (carros/<season>/<img>) → carrega PNG
  else → pintarModeloV2(...)  // substitui desenhaCarroLado() e desenhaCArroladoSemAereofolio()
```
Mesmo padrão para `obterCarroCima`, `obterCarroCimaSemAreofolio` e `obterCapacete`.

**Remoção de `obterModeloCarroCima()`:** O método e todos os seus call sites são removidos. `desenhaCarroCima(String modelo, Carro carro)` muda assinatura para `desenhaCarroCima(Carro carro)` usando v2 diretamente.

**`FormCarreira`:** `gerarCarroCima()` e `gerarCarroLado()` substituem toda a lógica de compositing por uma chamada a `CarregadorRecursos.pintarModeloV2(...)`.

## Risks / Trade-offs

- `CarroLadoDef.png` (carro sem aerofólio) é removido; o fallback sem aerofólio passa a usar `carro-lado-v2.png` da mesma forma que o normal. Se o asset v2 não tiver uma variante sem asas, o visual ficará igual ao normal — aceitável enquanto não houver asset dedicado.
- A tolerância de cor verde/branco pode capturar pixels de antialiasing. O artist deve evitar antialiasing nas bordas de cor1 e cor2 no asset v2.
