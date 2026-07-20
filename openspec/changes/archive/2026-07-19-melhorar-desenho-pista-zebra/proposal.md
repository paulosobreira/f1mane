## Why

O desenho procedural do circuito (pista + zebra), usado tanto no preview do editor (`PainelCircuito`) quanto na geração da imagem de fundo da corrida (`DesenhoProceduralCircuito`), tem dois defeitos visuais recorrentes hoje: (1) o zebrado (curb) das curvas fica visivelmente irregular/quebrado em alguns trechos, e (2) o traçado da pista forma pontas/quinas em vez de curvas suaves nas mudanças de direção. Ambos vêm da mesma causa raiz — o caminho é uma polilinha reta entre nós-chave (`pistaKey`), desenhada segmento a segmento — e valem a pena corrigir juntos.

## What Changes

- Suavizar o traçado da pista (e da zebra, que segue o mesmo caminho) interpolando uma curva entre os nós-chave em vez de usar segmentos retos ponta a ponta, eliminando as quinas visíveis no contorno da pista/zebra em mudanças de direção acentuadas.
- Tornar o zebrado das curvas contínuo/homogêneo: hoje cada segmento entre nós-chave é desenhado com `g2d.drawLine` isolado e um `BasicStroke` tracejado com fase sempre 0, então o padrão de listras desalinha a cada nó-chave (segmentos de comprimento diferente). Passa a desenhar a zebra de cada trecho de curva como um único `Shape`/caminho contínuo, com a fase do tracejado acompanhando a distância percorrida, mantendo o padrão alinhado ao longo de toda a curva.
- Unificar a lógica de construção do caminho da pista/zebra entre `PainelCircuito` (editor) e `DesenhoProceduralCircuito` (geração de imagem em memória), hoje duplicada, para que as duas superfícies produzam exatamente o mesmo desenho sem manter duas implementações do mesmo algoritmo.
- Sem mudança de dados: `Circuito`/`No`/XML de circuitos permanecem iguais — a suavização e a continuidade da zebra são só de desenho, calculadas em tempo de render a partir dos nós já existentes.

## Capabilities

### New Capabilities
- `circuito-tracado-suavizado`: como o traçado da pista e a faixa de zebra das curvas são desenhados a partir dos nós-chave — caminho suavizado (sem pontas) e zebrado com padrão de listras contínuo/homogêneo ao longo de cada trecho de curva.

### Modified Capabilities
(nenhuma — `circuito-cores-box-zebra` cobre só as cores aplicadas à zebra/box, não a geometria do traçado ou a continuidade do tracejado; essa mudança não altera nenhum requisito daquela spec, apenas passa a valer sobre um caminho geometricamente diferente.)

## Impact

- `src/main/java/br/flmane/visao/PainelCircuito.java` — `desenhaPista`, `desenhaTintaPistaZebra`, `desenhaPistaBox`, `desenhaTintaPistaBox`, `construirCaminho` (preview do editor, com viewport/zoom/`descontoCentraliza`).
- `src/main/java/br/flmane/entidades/DesenhoProceduralCircuito.java` — mesmos métodos equivalentes, usados na geração da imagem de fundo da corrida (zoom fixo, sem viewport).
- Sem mudança em `Circuito`, `No`, XMLs de circuito ou nas cores customizáveis de zebra/box (`corZebra1`/`corZebra2`/`corBox1`/`corBox2`, spec `circuito-cores-box-zebra`) — continuam aplicadas exatamente como hoje, só que sobre o novo caminho suavizado.
- Sem mudança na linha de largada, vagas de box, objetos de cenário ou qualquer lógica de jogo/física — é puramente desenho.
- Testes existentes que dependem do desenho atual da pista/zebra (ex.: `CircuitoCoresBoxZebraTest`, `DesenhoProceduralCircuitoTest`) precisam continuar passando ou ser atualizados para o novo caminho.
