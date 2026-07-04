# zona-frenagem-marcas-intensidade

## Purpose

Restringir a geração de marcas de pneu (`TravadaRoda`) às zonas de frenagem detectadas e variar a intensidade dessa geração conforme a posição relativa do piloto dentro da zona, concentrando os efeitos visuais de frenagem (marcas de pneu e faíscas) no trecho correto da pista em vez de nos próprios nós de curva.

## Requirements

### Requirement: Marca de pneu só ocorre dentro de uma zona de frenagem
`ControleJogoLocal.travouRodas` SHALL gerar uma `TravadaRoda` (marca de pneu) **apenas** quando o nó atual do piloto pertencer a uma zona de frenagem detectada, independente do tipo do nó (reta, largada, curva alta ou curva baixa). Fora de qualquer zona de frenagem, nenhuma marca de pneu SHALL ser gerada por esse método, mesmo que o chamador (`processaEscapadaDaPista`/`processaFreioNaReta`) tenha sido acionado.

#### Scenario: Nó fora de qualquer zona de frenagem nunca gera marca
- **WHEN** `travouRodas` é chamado com o piloto num nó (de qualquer tipo) que não pertence a nenhuma zona de frenagem detectada
- **THEN** nenhuma `TravadaRoda` é gerada, independente do sorteio de probabilidade

#### Scenario: Nó dentro de uma zona de frenagem pode gerar marca
- **WHEN** `travouRodas` é chamado com o piloto num nó que pertence a uma zona de frenagem detectada
- **THEN** a geração da `TravadaRoda` segue a chance-base normal (ajustada pela posição relativa dentro da zona, ver requisito de intensidade por posição)

### Requirement: Intensidade da marca de pneu varia pela posição dentro da zona de frenagem
Dentro de uma zona de frenagem, a chance de `ControleJogoLocal.travouRodas` gerar uma `TravadaRoda` SHALL variar conforme a posição relativa do nó dentro da zona (0.0 no início — nó mais distante da curva — até 1.0 no final — último nó do cluster de curva baixa): maior no início da zona (`Global.INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM`) e menor no final (`Global.INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM`), interpolada linearmente entre os dois extremos.

#### Scenario: Início da zona tem intensidade máxima
- **WHEN** o piloto está no nó do início da zona de frenagem (posição relativa 0.0)
- **THEN** a chance-base de `travouRodas` é multiplicada por `Global.INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM`

#### Scenario: Final da zona tem intensidade reduzida
- **WHEN** o piloto está no último nó do cluster de curva baixa da zona de frenagem (posição relativa 1.0)
- **THEN** a chance-base de `travouRodas` é multiplicada por `Global.INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM`, menor que no início da zona

#### Scenario: Mesmo sorteio pode gerar marca no início e não gerar no final
- **WHEN** o mesmo valor de sorteio aleatório é usado para um nó no início da zona e para um nó no final da zona
- **THEN** é possível que a marca ocorra no início e não ocorra no final, refletindo a intensidade maior no início

### Requirement: Aumento de probabilidade de faísca por frenagem só ocorre dentro da zona de frenagem
`Piloto.processaFaiscas()` já restringe faíscas a nós de reta/largada (`verificaRetaOuLargada()`); nós de curva nunca geram faísca por esse caminho, antes ou depois desta mudança — não há redução a aplicar ali. O aumento de probabilidade condicionado a `isFreiandoReta()` (redução do limiar `mod`) SHALL, como consequência do requisito de `zona-frenagem-deteccao` que passa a exigir zona de frenagem para `freiandoReta=true`, só se aplicar quando o piloto estiver dentro de uma zona de frenagem — sem exigir nenhuma mudança adicional em `processaFaiscas()` além da já feita em `processaFreioNaReta()`.

#### Scenario: Faíscas continuam restritas a nós de reta/largada
- **WHEN** o piloto está num nó `CURVA_ALTA` ou `CURVA_BAIXA`, dentro ou fora de zona de frenagem
- **THEN** `processaFaiscas()` não seta `faiscas=true` por esse nó, exatamente como antes desta mudança

#### Scenario: Bônus de probabilidade de faísca por frenagem exige estar na zona de frenagem
- **WHEN** o piloto está numa reta fora de qualquer zona de frenagem, mesmo perto de uma curva baixa
- **THEN** `isFreiandoReta()` é `false` (ver `zona-frenagem-deteccao`), então o bônus de probabilidade de faísca (redução do limiar `mod`) não se aplica nesse ponto

### Requirement: Zona de frenagem concentra a maior parte das marcas e faíscas
Dentro de uma zona de frenagem detectada, o sistema SHALL manter (ou aumentar) a taxa de travada de roda, marca de pneu e faísca em relação ao comportamento anterior a esta mudança, de forma que a maior parte dos efeitos visuais de frenagem ocorra nesse trecho em vez de concentrada nos próprios nós de curva.

#### Scenario: Frenagem forte dentro da zona de frenagem aciona travada de roda
- **WHEN** o piloto está com stress alto e modo de pilotagem agressivo dentro de uma reta que faz parte de uma zona de frenagem detectada (`retardaFreiandoReta` verdadeiro)
- **THEN** `processaFreioNaReta` continua podendo acionar `controleJogo.travouRodas(this)`, com a mesma taxa (ou maior) de antes desta mudança
