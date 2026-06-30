## ADDED Requirements

### Requirement: Assets v2 são os únicos modelos de fallback
O repositório não deve conter os diretórios de modelo por era nem os arquivos de máscara antigos. Os assets v2 são os únicos modelos genéricos de carro e capacete.

#### Scenario: Ausência dos modelos por era
- **WHEN** o build é executado
- **THEN** os diretórios `png/cima19701979/`, `png/cima19801997/`, `png/cima19982008/`, `png/cima20092016/`, `png/cima2017/` não existem no classpath

#### Scenario: Ausência dos arquivos de máscara antigos
- **WHEN** o build é executado
- **THEN** os arquivos `png/Capacete.png`, `png/CapaceteC1.png`, `png/CapaceteC2.png`, `png/CarroLado.png`, `png/CarroLadoC1.png`, `png/CarroLadoC2.png`, `png/CarroLadoC3.png`, `png/CarroLadoDef.png` não existem no classpath

### Requirement: Pipeline de pintura por substituição de cor
O sistema deve pintar o modelo v2 substituindo pixels verdes por cor1 e pixels brancos por cor2 do carro, na resolução nativa do asset.

#### Scenario: Classificação de pixel verde
- **WHEN** o algoritmo analisa um pixel com R < 100 && G > 150 && B < 100 && alpha > 0
- **THEN** o pixel de saída recebe os canais RGB de cor1, mantendo o alpha original

#### Scenario: Classificação de pixel branco
- **WHEN** o algoritmo analisa um pixel com R > 200 && G > 200 && B > 200 && alpha > 0
- **THEN** o pixel de saída recebe os canais RGB de cor2, mantendo o alpha original

#### Scenario: Pixel neutro ou transparente
- **WHEN** o pixel não se enquadra em verde nem branco
- **THEN** o pixel é copiado sem alteração para a imagem de saída

### Requirement: Redimensionamento proporcional após pintura
A imagem pintada é redimensionada para o tamanho do slot mantendo proporção, com centralização em canvas transparente.

#### Scenario: Redimensionamento com proporção diferente
- **WHEN** o asset v2 não tem as dimensões exatas do slot alvo
- **THEN** escala = `min(targetW/srcW, targetH/srcH)`; imagem escalada centralizada em canvas `targetW × targetH` transparente

#### Scenario: Asset já no tamanho exato
- **WHEN** o asset v2 já tem exatamente as dimensões do slot
- **THEN** a imagem pintada é retornada diretamente sem novo canvas

### Requirement: Fallback v2 substitui todos os métodos desenha* antigos
Os métodos que usavam compositing por máscara agora usam o pipeline v2.

#### Scenario: Fallback de carro lateral
- **WHEN** não há spritesheet nem PNG individual para a equipe e o modelo v2 não é forçado
- **THEN** `obterCarroLado()` retorna `pintarModeloV2("png/carro-lado-v2.png", cor1, cor2, LADO_W, LADO_H)` — tamanho alvo 180×40

#### Scenario: Fallback de carro top-down
- **WHEN** não há spritesheet nem PNG individual para a equipe
- **THEN** `obterCarroCima()` retorna `pintarModeloV2("png/carro-cima-v2.png", cor1, cor2, CIMA_W, CIMA_H)` — tamanho alvo 90×90

#### Scenario: Fallback de capacete
- **WHEN** não há PNG individual para o piloto
- **THEN** `obterCapacete()` retorna `pintarModeloV2("png/capacete-v2.png", cor1Carro, cor2Carro, CAP_W, CAP_H)` — tamanho alvo 55×55

### Requirement: Flag para forçar modelo v2 sobre sprites
`Global.FORCE_MODELO_V2` permite ativar o pipeline v2 mesmo quando há spritesheet ou PNGs individuais disponíveis.

#### Scenario: Flag ativada com spritesheet disponível
- **WHEN** `Global.FORCE_MODELO_V2 == true` e há spritesheet para a temporada
- **THEN** os métodos `obterCarroLado()`, `obterCarroCima()` e `obterCapacete()` ignoram o spritesheet e retornam resultado do pipeline v2

#### Scenario: Flag desativada (padrão)
- **WHEN** `Global.FORCE_MODELO_V2 == false`
- **THEN** spritesheets e PNGs individuais continuam sendo usados normalmente; v2 atua apenas como fallback

### Requirement: Cache das imagens pintadas
Imagens geradas pelo pipeline v2 são armazenadas nos buffers existentes da mesma forma que sprites.

#### Scenario: Segunda chamada para o mesmo carro
- **WHEN** `obterCarroLado()` é chamado duas vezes para o mesmo `carro.getNome()`
- **THEN** `pintarModeloV2` é executado apenas na primeira chamada; a segunda retorna do buffer

### Requirement: FormCarreira usa modelo v2 para preview de carro
Os métodos `gerarCarroCima()` e `gerarCarroLado()` de `FormCarreira` usam o pipeline v2 em vez do compositing antigo.

#### Scenario: Preview de carro na tela de carreira
- **WHEN** o usuário altera cor1 ou cor2 na tela de carreira
- **THEN** `gerarCarroCima()` e `gerarCarroLado()` chamam `pintarModeloV2` com as cores selecionadas e exibem o resultado
