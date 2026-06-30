## Requirements

### Requirement: Assets v2 são os únicos modelos de fallback
O repositório não deve conter os diretórios de modelo por era nem os arquivos de máscara antigos. Os assets v2 são os únicos modelos genéricos de carro e capacete.

#### Scenario: Ausência dos modelos por era
- **WHEN** o build é executado
- **THEN** os diretórios `png/cima19701979/`, `png/cima19801997/`, `png/cima19982008/`, `png/cima20092016/`, `png/cima2017/` não existem no classpath

#### Scenario: Ausência dos arquivos de máscara antigos
- **WHEN** o build é executado
- **THEN** os arquivos `png/Capacete.png`, `png/CapaceteC1.png`, `png/CapaceteC2.png`, `png/CarroLado.png`, `png/CarroLadoC1.png`, `png/CarroLadoC2.png`, `png/CarroLadoC3.png`, `png/CarroLadoDef.png` não existem no classpath

### Requirement: Pipeline de pintura por substituição de cor HSB
O sistema deve pintar o modelo v2 substituindo regiões verdes por cor1 e regiões brancas/cinzas por cor2, preservando o shading artístico do asset.

#### Scenario: Classificação de pixel verde (máscara cor1)
- **WHEN** o pixel tem matiz HSB em [0.22, 0.45] e saturação > 0.20 e alpha > 0
- **THEN** o pixel de saída recebe matiz e saturação de cor1, com brilho = `brilho_cor1 × brilho_pixel_original`

#### Scenario: Classificação de pixel branco/cinza (máscara cor2)
- **WHEN** o pixel tem saturação HSB < 0.20 e brilho > 0.70 e alpha > 0
- **THEN** o pixel de saída recebe matiz e saturação de cor2, com brilho = `brilho_cor2 × brilho_pixel_original`

#### Scenario: Cor escura (preto) respeitada
- **WHEN** cor1 ou cor2 tem brilho HSB = 0 (preto)
- **THEN** todos os pixels da máscara correspondente resultam em preto, independentemente do brilho original do pixel

#### Scenario: Pixel neutro ou transparente
- **WHEN** o pixel não se enquadra em verde nem branco/cinza claro, ou alpha == 0
- **THEN** o pixel é copiado sem alteração para a imagem de saída

### Requirement: Redimensionamento anti-aliased após pintura
A imagem pintada é redimensionada para o tamanho do slot com qualidade máxima e sem serrilhado.

#### Scenario: Redimensionamento progressivo
- **WHEN** o asset v2 é maior que 2× o tamanho alvo
- **THEN** o redimensionamento usa halvings BILINEAR sucessivos até ≤2× do alvo, seguido de passo final BICUBIC com ANTIALIAS_ON

#### Scenario: Centralização proporcional
- **WHEN** o asset v2 não tem as dimensões exatas do slot alvo
- **THEN** escala = `min(targetW/srcW, targetH/srcH)`; imagem escalada centralizada em canvas `targetW × targetH` transparente

### Requirement: Variante sem aerofólio (knockout por máscara alpha)
O carro visto de cima sem asa dianteira é gerado aplicando `DST_OUT` com `carro-cima-sem_asa-v2.png` antes do redimensionamento.

#### Scenario: Knockout da asa dianteira
- **WHEN** `pintarModeloV2` é chamado com `knockoutMaskPath = "png/carro-cima-sem_asa-v2.png"`
- **THEN** a máscara é aplicada em `AlphaComposite.DST_OUT` sobre a imagem pintada na resolução nativa, antes do escalonamento

### Requirement: Pré-aquecimento da variante sem aerofólio
A variante sem asa é gerada junto com a imagem normal para evitar lag na corrida quando a asa é perdida.

#### Scenario: Pré-aquecimento no primeiro render
- **WHEN** `obterCarroCima()` é chamado pela primeira vez para um carro usando o modelo v2
- **THEN** `obterCarroCimaSemAreofolio()` também é gerado e armazenado em `bufferCarrosCimaSemAreofolio` antes de retornar

### Requirement: Fallback v2 substitui todos os métodos desenha* antigos
Os métodos que usavam compositing por máscara agora usam o pipeline v2.

#### Scenario: Fallback de carro lateral
- **WHEN** não há spritesheet nem PNG individual para a equipe e o modelo v2 não é forçado
- **THEN** `obterCarroLado()` retorna `pintarModeloV2("png/carro-lado-v2.png", cor1, cor2, LADO_W, LADO_H)`

#### Scenario: Fallback de carro top-down
- **WHEN** não há spritesheet nem PNG individual para a equipe
- **THEN** `obterCarroCima()` retorna `pintarModeloV2("png/carro-cima-v2.png", cor1, cor2, CIMA_W, CIMA_H)`

#### Scenario: Fallback de capacete
- **WHEN** não há PNG individual para o piloto
- **THEN** `obterCapacete()` retorna `pintarModeloV2("png/capacete-v2.png", cor1Carro, cor2Carro, CAP_W, CAP_H)`

### Requirement: Flag para forçar modelo v2 sobre sprites
`Global.FORCE_MODELO_V2` permite ativar o pipeline v2 mesmo quando há spritesheet ou PNGs individuais disponíveis.

#### Scenario: Flag ativada com spritesheet disponível
- **WHEN** `Global.FORCE_MODELO_V2 == true` e há spritesheet para a temporada
- **THEN** os métodos `obterCarroLado()`, `obterCarroCima()` e `obterCapacete()` ignoram o spritesheet e retornam resultado do pipeline v2

#### Scenario: Flag desativada (padrão)
- **WHEN** `Global.FORCE_MODELO_V2 == false`
- **THEN** spritesheets e PNGs individuais continuam sendo usados normalmente; v2 atua apenas como fallback

### Requirement: Cache das imagens pintadas
Imagens geradas pelo pipeline v2 são armazenadas em `cacheModeloV2` (Map estático em `CarregadorRecursos`) com chave composta por `assetPath + cor1RGB + cor2RGB + targetW + targetH [+ knockoutMaskPath]`.

#### Scenario: Segunda chamada para o mesmo carro
- **WHEN** `pintarModeloV2` é chamado duas vezes com os mesmos parâmetros
- **THEN** o pipeline HSB+redimensionamento é executado apenas na primeira chamada; a segunda retorna do cache

### Requirement: FormCarreira usa modelo v2 para preview de carro
Os métodos `gerarCarroCima()` e `gerarCarroLado()` de `FormCarreira` usam o pipeline v2 em vez do compositing antigo.

#### Scenario: Preview de carro na tela de carreira
- **WHEN** o usuário altera cor1 ou cor2 na tela de carreira
- **THEN** `gerarCarroCima()` e `gerarCarroLado()` chamam `pintarModeloV2` com as cores selecionadas e exibem o resultado
