# Spec: circuito-tracado-suavizado

## Purpose

Defines how the track/curb outline is drawn from the circuit's key nodes (`pistaKey`/`boxKey`): a smoothed curve (no visible sharp corners at direction changes) instead of a straight polyline, with the curve zebra rendered as a continuous, homogeneous dashed pattern per curve stretch, shared identically between the circuit editor preview (`PainelCircuito`) and the in-memory race background image generation (`DesenhoProceduralCircuito`).

## Requirements

### Requirement: Traçado da pista é suavizado entre os nós-chave
O contorno da pista (e, por seguir o mesmo caminho, da zebra) SHALL ser desenhado como uma curva suave interpolada pelos nós de `pistaKey` — passando exatamente por cada nó-chave existente, sem alterar suas posições — em vez de segmentos de reta ligando nó a nó. Mudanças de direção entre nós-chave consecutivos, incluindo as mais acentuadas, NÃO SHALL produzir pontas/quinas visíveis no contorno desenhado.

#### Scenario: Mudança de direção acentuada entre nós-chave não forma ponta
- **WHEN** o circuito tem dois segmentos consecutivos de `pistaKey` com uma mudança de direção acentuada entre eles (ex.: sequência de nós de curva com ângulo aberto)
- **THEN** o traçado desenhado forma uma curva suave passando pelos nós envolvidos, sem ponta/quina visível no contorno da pista

#### Scenario: Traçado fechado da pista não tem descontinuidade no fechamento do laço
- **WHEN** o circuito é desenhado (o traçado da pista é um laço fechado, do último nó de volta ao primeiro)
- **THEN** a curva se fecha suavemente entre o último e o primeiro nó, sem quebra de continuidade visível nesse ponto

### Requirement: Zebra das curvas mantém padrão de listras contínuo ao longo de todo o trecho
Nos trechos de curva (nós consecutivos do tipo `CURVA_ALTA`/`CURVA_BAIXA`), a faixa de zebra SHALL ser desenhada como um único traçado contínuo por trecho, de modo que o padrão de listras (tracejado) não desalinhe nem reinicie nas emendas entre nós-chave internos ao trecho.

#### Scenario: Trecho de curva com múltiplos nós-chave tem zebra homogênea
- **WHEN** um trecho de curva do circuito é composto por vários nós-chave `CURVA_ALTA`/`CURVA_BAIXA` consecutivos
- **THEN** a faixa de zebra desse trecho é desenhada com o padrão de listras contínuo e alinhado do início ao fim, sem desalinhamento visível nas emendas entre os nós internos

#### Scenario: Zebra continua restrita aos trechos de curva
- **WHEN** a pista alterna entre trechos de reta e de curva
- **THEN** a faixa de zebra continua aparecendo somente nos trechos classificados como curva, exatamente como antes desta mudança

### Requirement: Editor e geração de imagem de corrida produzem o mesmo desenho de pista/zebra
`PainelCircuito` (preview do editor) e `DesenhoProceduralCircuito` (geração da imagem de fundo da corrida) SHALL usar a mesma lógica de suavização de traçado e de continuidade de zebra, de forma que as duas superfícies produzam um desenho visualmente idêntico para o mesmo circuito.

#### Scenario: Mesmo circuito desenhado nas duas superfícies é visualmente idêntico
- **WHEN** o mesmo circuito é desenhado pelo preview do editor e pela geração da imagem de fundo usada na corrida
- **THEN** o traçado suavizado da pista e o padrão de zebra são visualmente idênticos nas duas superfícies

### Requirement: Cores customizáveis de zebra/box continuam válidas sobre o traçado suavizado
As cores customizáveis de zebra (`corZebra1`/`corZebra2`) e de box (`corBox1`/`corBox2`) definidas em `Circuito` SHALL continuar sendo aplicadas exatamente como antes desta mudança, agora sobre o caminho suavizado — inclusive o fallback independente branco/vermelho quando não definidas.

#### Scenario: Cores de zebra customizadas continuam aplicadas sobre a curva suavizada
- **WHEN** um circuito tem `corZebra1` e `corZebra2` definidas e o traçado suavizado é desenhado
- **THEN** a faixa de zebra contínua dos trechos de curva alterna `corZebra1` e `corZebra2`, do mesmo jeito que alternava antes da suavização
