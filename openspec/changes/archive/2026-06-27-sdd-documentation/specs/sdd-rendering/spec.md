## ADDED Requirements

### Requirement: PainelCircuito documentado
O SDD SHALL descrever como `PainelCircuito` renderiza a corrida.

#### Scenario: Modelo de rendering descrito
- **WHEN** o leitor consulta o pipeline de rendering
- **THEN** o SDD descreve que `PainelCircuito` usa o modelo Swing (`paintComponent(Graphics)`) sem método `render()` explícito; as flags estáticas `desenhaBkg`, `desenhaPista` e `desenhaImagens` controlam o que é desenhado e são desativadas no modo headless (`MainFrameSimulacao`); `Global.DEBUG = true` ativa overlays visuais

#### Scenario: Sprites carregados descritos
- **WHEN** o leitor consulta os recursos visuais do painel
- **THEN** o SDD lista os principais buffers de imagem: carros danificados, pneus por tipo (`pneuMoleImg`, `pneuDuroImg`, `pneuChuvaImg`), setas direcionais, grade de largada, efeitos de roda travada e faróis

### Requirement: SpriteSheet documentado
O SDD SHALL descrever o layout do SpriteSheet e como extrair sprites por índice de piloto.

#### Scenario: Layout de pixels do SpriteSheet descrito
- **WHEN** o leitor consulta o sistema de sprites
- **THEN** o SDD descreve que `SpriteSheet` carrega uma imagem por temporada (`sprites/tANO.png`) com layout fixo: Y=0 carros de lado (`LADO_W=180 × LADO_H=40`), Y=40 carros de cima (`CIMA_W=90 × CIMA_H=90`), Y=130 capacetes linha 1, Y=185 capacetes linha 2; `CAP_PER_ROW=12` capacetes por linha; extração via `subimage(idx*W, Y, W, H)`

#### Scenario: Métodos de extração de sprite descritos
- **WHEN** o leitor consulta como obter o sprite de um piloto
- **THEN** o SDD descreve: `SpriteSheet.getCarroLado(temporada, idx)`, `getCarroCima(temporada, idx)`, `getCapacete(temporada, idx)` — onde `idx` é a posição do piloto na lista da temporada; para capacetes: `row = idx / 12`, `col = idx % 12`
