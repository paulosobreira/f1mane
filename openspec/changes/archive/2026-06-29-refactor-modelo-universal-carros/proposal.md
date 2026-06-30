## Why

O sistema atual mantém múltiplos diretórios de modelo por era (`png/cima2017/`, `png/cima20092016/`, etc.) e arquivos de máscara individuais (`CarroLado.png`, `CarroLadoC1/C2/C3.png`, `Capacete.png`, `CapaceteC1/C2.png`) como fallback para quando não há spritesheet disponível. Esses assets são substituídos por três novos assets universais (`carro-cima-v2.png`, `carro-lado-v2.png`, `capacete-v2.png`) que codificam as regiões de cor diretamente na imagem (verde = cor1, branco = cor2), eliminando a necessidade de múltiplos arquivos de máscara e de lógica de seleção por era.

## What Changes

- **Remoção** de todos os diretórios de modelo por era: `png/cima19701979/`, `png/cima19801997/`, `png/cima19982008/`, `png/cima20092016/`, `png/cima2017/`
- **Remoção** dos assets de máscara antigos: `png/Capacete.png`, `png/CapaceteC1.png`, `png/CapaceteC2.png`, `png/CarroLado.png`, `png/CarroLadoC1.png`, `png/CarroLadoC2.png`, `png/CarroLadoC3.png`, `png/CarroLadoDef.png`
- Novo método `pintarModeloV2(assetPath, cor1, cor2, targetW, targetH)` em `CarregadorRecursos` — pinta o asset v2 substituindo verde→cor1 e branco→cor2 na resolução nativa, depois redimensiona com proporção preservada
- `desenhaCarroLado()`, `desenhaCArroladoSemAereofolio()`, `desenhaCapacete()` e `desenhaCarroCima()` passam a chamar `pintarModeloV2` com os respectivos assets v2
- `obterModeloCarroCima()` e toda a lógica de seleção de modelo por era são removidos
- `FormCarreira.gerarCarroCima()` e `gerarCarroLado()` atualizados para usar v2
- Adição de `Global.FORCE_MODELO_V2 = false` — quando `true`, ignora spritesheets e PNGs individuais por equipe e usa sempre o pipeline v2

## Capabilities

### New Capabilities
- `modelo-universal-v2`: Pintura de carro e capacete via substituição de cor (verde→cor1, branco→cor2) sobre assets v2, com resize proporcional para o tamanho alvo; substitui todo o sistema de modelos por era

### Modified Capabilities
- (nenhuma — a remoção dos modelos por era não tem spec existente a ser modificado)

## Impact

- `src/main/java/br/nnpe/Global.java` — nova constante `FORCE_MODELO_V2`
- `src/main/java/br/f1mane/recursos/CarregadorRecursos.java` — novo `pintarModeloV2`; remoção de `obterModeloCarroCima()`; refatoração de `desenhaCarroLado`, `desenhaCArroladoSemAereofolio`, `desenhaCapacete`, `desenhaCarroCima`
- `src/main/java/br/f1mane/servidor/applet/FormCarreira.java` — `gerarCarroCima()` e `gerarCarroLado()` atualizados
- **Removidos**: `src/main/resources/png/cima19701979/`, `cima19801997/`, `cima19982008/`, `cima20092016/`, `cima2017/` (diretórios completos)
- **Removidos**: `src/main/resources/png/Capacete.png`, `CapaceteC1.png`, `CapaceteC2.png`, `CarroLado.png`, `CarroLadoC1.png`, `CarroLadoC2.png`, `CarroLadoC3.png`, `CarroLadoDef.png`
- `src/main/resources/png/carro-cima-v2.png`, `carro-lado-v2.png`, `capacete-v2.png` — assets já criados (untracked), passam a ser rastreados
- Sem impacto em `SpriteSheet.java` — spritesheets por temporada continuam sendo a opção preferida
