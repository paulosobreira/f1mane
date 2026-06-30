## 1. Preparar assets e flag Global

- [x] 1.1 Em `src/main/java/br/nnpe/Global.java`, adicionar `public static boolean FORCE_MODELO_V2 = false;` após `DEBUG_SEM_CHUVA`
- [x] 1.2 Fazer `git add src/main/resources/png/carro-cima-v2.png src/main/resources/png/carro-lado-v2.png src/main/resources/png/capacete-v2.png` para rastrear os assets v2

## 2. Remover assets antigos

- [x] 2.1 Executar `git rm -r src/main/resources/png/cima19701979 src/main/resources/png/cima19801997 src/main/resources/png/cima19982008 src/main/resources/png/cima20092016 src/main/resources/png/cima2017`
- [x] 2.2 Executar `git rm src/main/resources/png/Capacete.png src/main/resources/png/CapaceteC1.png src/main/resources/png/CapaceteC2.png src/main/resources/png/CarroLado.png src/main/resources/png/CarroLadoC1.png src/main/resources/png/CarroLadoC2.png src/main/resources/png/CarroLadoC3.png src/main/resources/png/CarroLadoDef.png`

## 3. Adicionar pintarModeloV2 em CarregadorRecursos

- [x] 3.1 Adicionar método estático `public static BufferedImage pintarModeloV2(String assetPath, Color cor1, Color cor2, int targetW, int targetH)` em `CarregadorRecursos.java`:
  - Carregar asset via `carregaBufferedImageTransparecia(assetPath)` → `srcImg`
  - Criar `destImg` com mesma dimensão e `TYPE_INT_ARGB`
  - Percorrer pixels: verde (R<100 && G>150 && B<100 && A>0) → RGB de cor1; branco (R>200 && G>200 && B>200 && A>0) → RGB de cor2; demais → copiar original
  - Calcular `escala = Math.min((double)targetW/srcW, (double)targetH/srcH)`
  - Se dimensões já iguais ao target: retornar `destImg`
  - Caso contrário: escalar com `SCALE_SMOOTH`, centralizar em canvas `targetW × targetH` transparente e retornar

## 4. Refatorar métodos desenha* para usar v2

- [x] 4.1 Em `desenhaCarroLado(Carro carro)`: substituir toda a lógica de `CarroLado.png` + `gerarCoresCarros` por `return pintarModeloV2("png/carro-lado-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.LADO_W, SpriteSheet.LADO_H)`
- [x] 4.2 Em `desenhaCArroladoSemAereofolio(Carro carro)`: mesma substituição (usar `carro-lado-v2.png`)
- [x] 4.3 Em `desenhaCarroCima(Carro carro)` — remover parâmetro `String modelo` e substituir lógica por `return pintarModeloV2("png/carro-cima-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.CIMA_W, SpriteSheet.CIMA_H)`
- [x] 4.4 Em `desenhaCapacete(Piloto piloto)`: substituir lógica de `Capacete.png` + `gerarCoresCarros` por `return pintarModeloV2("png/capacete-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.CAP_W, SpriteSheet.CAP_H)`

## 5. Remover obterModeloCarroCima e atualizar call sites

- [x] 5.1 Remover o método `obterModeloCarroCima(String temporada)` de `CarregadorRecursos.java`
- [x] 5.2 Em `obterCarroCima()`: remover a linha `String modelo = obterModeloCarroCima(temporada)` e ajustar a chamada do fallback final para `desenhaCarroCima(carro)` (sem parâmetro modelo)
- [x] 5.3 Em `obterCarroCimaSemAreofolio()`: remover a linha `String modelo = obterModeloCarroCima(temporada)` e ajustar o fallback final analogamente

## 6. Adicionar bloco FORCE_MODELO_V2 nos métodos obter*

- [x] 6.1 No início do corpo de `obterCarroLado()` (após resolver `temporada` e obter `carro`), adicionar bloco FORCE_MODELO_V2
- [x] 6.2 Repetir para `obterCarroCima()` com `carro-cima-v2.png` e `CIMA_W/CIMA_H`
- [x] 6.3 Repetir para `obterCarroCimaSemAreofolio()` com `carro-cima-v2.png`
- [x] 6.4 Repetir para `obterCapacete()` com `capacete-v2.png` e `CAP_W/CAP_H`

## 7. Atualizar FormCarreira

- [x] 7.1 Em `FormCarreira.gerarCarroCima()`: substituir toda a lógica por `CarregadorRecursos.pintarModeloV2("png/carro-cima-v2.png", ...)`
- [x] 7.2 Em `FormCarreira.gerarCarroLado()`: substituir toda a lógica por `CarregadorRecursos.pintarModeloV2("png/carro-lado-v2.png", ...)`

## 8. Verificação

- [x] 8.1 Compilar com `mvn clean package -Ph2 -DskipTests` e confirmar build verde
- [x] 8.2 Verificar que nenhuma referência a `png/cima`, `CarroLado.png`, `CarroLadoC`, `Capacete.png`, `CapaceteC` permanece no código Java (buscar com grep)
- [ ] 8.3 Executar o jogo com temporada sem spritesheet e verificar visualmente que carros e capacetes são pintados corretamente com cor1/cor2
- [ ] 8.4 Ativar `FORCE_MODELO_V2 = true` temporariamente e verificar temporada com spritesheet (ex: t2024) usando o modelo v2
- [ ] 8.5 Reverter `FORCE_MODELO_V2 = false` e confirmar que t2024 volta a usar spritesheet normalmente
