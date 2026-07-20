## Context

Hoje `pistaKey` é uma lista pequena de nós-chave (curva alta/baixa, reta, box, largada), e tanto `PainelCircuito` (preview do editor) quanto `DesenhoProceduralCircuito` (geração da imagem de fundo da corrida) desenham a pista/zebra ligando esses nós com `g2d.drawLine` reto, nó a nó, com a `BasicStroke` (`JOIN_ROUND`) só arredondando a emenda entre dois segmentos retos — não a trajetória em si. A zebra usa um `BasicStroke` tracejado (`CAP_BUTT`, `JOIN_MITER`, dash fixo) desenhado com um `drawLine` por segmento, cada um resetando a fase do tracejado pra 0.

Essa mesma lógica está duplicada nas duas classes (a classe `DesenhoProceduralCircuito` foi extraída de `PainelCircuito` e seu próprio javadoc já registra isso). `pistaFull` (traçado vetorizado por Bresenham, usado por física/IA/colisão em `ControleCorrida` e afins) não é tocado por essa mudança — só o desenho a partir de `pistaKey`.

## Goals / Non-Goals

**Goals:**
- Traçado da pista (e zebra, que segue o mesmo caminho) sem pontas visíveis nas mudanças de direção entre nós-chave — curva suave passando exatamente pelos nós-chave existentes.
- Zebrado das curvas com padrão de listras contínuo/homogêneo ao longo de todo o trecho de curva, sem desalinhar nas emendas entre nós-chave.
- Editor (`PainelCircuito`) e geração de imagem em memória (`DesenhoProceduralCircuito`) continuam produzindo exatamente o mesmo desenho, agora a partir de uma única implementação do algoritmo de suavização (elimina a duplicação existente).
- Sem regressão em box, linha de largada, vagas de box, objetos de cenário e cores customizáveis de zebra/box (`corZebra1`/`corZebra2`/`corBox1`/`corBox2`).

**Non-Goals:**
- Não altera `pistaFull`, física, IA ou qualquer lógica de corrida — é puramente desenho a partir de `pistaKey`.
- Não altera o modelo `Circuito`/`No` nem o formato XML persistido — nenhum campo novo, nenhuma migração de dados.
- Não introduz dependência externa — usa só `java.awt.geom` (`GeneralPath.curveTo`, já disponível).
- Não redesenha vagas de box, linha de largada quadriculada ou objetos de cenário — esses já são satisfatórios e ficam fora de escopo.

## Decisions

### 1. Suavização via spline Catmull-Rom convertida em curvas de Bézier cúbicas
O caminho da pista passa a ser construído com `GeneralPath.moveTo`/`curveTo` (Bézier cúbica) entre cada par de nós-chave consecutivos, com os pontos de controle derivados dos nós vizinhos (conversão padrão Catmull-Rom → Bézier). Isso garante que a curva passe exatamente pelos nós-chave existentes (preserva a semântica de cada nó — tipo, posição do box/largada) e tenha derivada contínua nas emendas, eliminando as pontas em qualquer mudança de direção, não só nas mais acentuadas.

**Alternativa considerada e rejeitada:** arredondar só o vértice de cada nó (inserir um arco curto substituindo a quina, como em `RoundRectangle2D`), mantendo o resto do segmento reto. Rejeitada porque continuaria reto entre os arredondamentos — não resolve o caso de vários nós-chave próximos com ângulos abertos formando uma sequência de pequenas quinas, que é justamente o padrão mais comum em curvas do circuito.

Para reduzir *overshoot* (a spline "estourar" pra fora do traçado pretendido quando os nós-chave têm espaçamento muito desigual, comum entre trechos retos longos e curvas com nós próximos), usar Catmull-Rom **centrípeta** (parametrização por distância, não uniforme) em vez da variante uniforme clássica — a variante centrípeta é a escolha padrão pra evitar laços/overshoot em pontos com espaçamento irregular.

O traçado da pista é um laço fechado (`fechado = true` em `construirCaminho`) — os vizinhos "antes do primeiro" e "depois do último" nó são os nós do fim/início da própria lista (wrap circular). O traçado do box é aberto (`fechado = false`) — nas pontas, usa a condição de extremidade clampeada (o próprio nó extremo faz o papel do vizinho ausente), evitando extrapolar tangente pra fora da lista.

### 2. Zebra desenhada como caminho único e contínuo por trecho de curva, não segmento a segmento
Em vez de um `drawLine` por par de nós com a `BasicStroke` tracejada resetando fase a cada chamada, os nós consecutivos classificados como curva (`CURVA_ALTA`/`CURVA_BAIXA`) são agrupados num único `GeneralPath` contínuo (mesma suavização da decisão 1) e desenhados com **uma única chamada** de `g2d.draw`/`g2d.setStroke(zebra)`. Como a fase do tracejado do `BasicStroke` é calculada pelo Java2D ao longo do `Shape` inteiro, o padrão de listras fica automaticamente contínuo e alinhado do início ao fim do trecho de curva, sem precisar rastrear comprimento acumulado manualmente.

Os pontos de fronteira curva/reta continuam exatamente onde estão hoje: cada segmento da spline corresponde 1:1 a um par de nós-chave original, então a classificação "esse trecho é curva" não muda — só passa a ser agrupada em um `Shape` por sequência contígua de trechos-curva, em vez de redesenhada isoladamente por par.

**Alternativa considerada e rejeitada:** manter o desenho segmento a segmento e calcular manualmente a fase do dash acumulada (offset em pixels percorridos) para passar a cada `BasicStroke` sucessivo. Rejeitada por ser mais código, mais sujeita a erro de arredondamento/deriva de ponto flutuante, e por resolver com mais complexidade exatamente o que `BasicStroke`/`Graphics2D` já fazem sozinhos quando o `Shape` é contínuo.

### 3. Unificar a lógica em `DesenhoProceduralCircuito`, `PainelCircuito` delega
A construção do caminho suavizado (decisões 1 e 2) fica só em `DesenhoProceduralCircuito` (já é o local documentado como "reutilizável fora do editor"), como métodos estáticos que recebem `zoom` e um deslocamento de centralização (`Point`) — o mesmo parâmetro que `desenhaLinhaDeLargada` já aceita opcionalmente pra suportar viewport. `PainelCircuito.desenhaPista`/`desenhaTintaPistaZebra`/`desenhaPistaBox`/`desenhaTintaPistaBox` passam a delegar para essas versões compartilhadas em vez de manter uma segunda implementação do algoritmo, no mesmo espírito de como `desenhaVagasBox` já foi unificado com a flag `modoEditor`.

**Alternativa considerada e rejeitada:** implementar a suavização duas vezes (uma em cada classe) mantendo a estrutura atual. Rejeitada porque a duplicação já é um problema documentado (risco de as duas telas divergirem visualmente) e o objetivo explícito desta mudança inclui não piorar essa divergência.

## Risks / Trade-offs

- **[Risco] Overshoot da spline em nós muito próximos/espaçamento irregular** → Mitigação: usar Catmull-Rom centrípeta (não uniforme); validar visualmente contra os circuitos XML existentes (`src/main/resources/circuitos/*.xml`) durante a implementação, com atenção a chicanes (nós próximos em sequência) e a transições retas-longas → curva.
- **[Risco] Custo de CPU maior no preview interativo do editor** (a suavização roda a cada repaint de `PainelCircuito`, potencialmente a cada frame do `GerenciadorVisual`) → Mitigação: `pistaKey` tem poucas dezenas de nós por circuito, então o custo é O(n) sobre uma lista pequena — mesma ordem de grandeza do laço de `drawLine` atual. Se o profiling mostrar impacto perceptível, cachear o `GeneralPath` construído e invalidar só quando `pistaKey`/`zoom`/deslocamento de centralização mudarem, em vez de reconstruir a cada `paint`.
- **[Risco] Regressão em testes que hoje fixam premissas do desenho atual (segmento reto, dash por par de nós)** → Mitigação: revisar `CircuitoCoresBoxZebraTest` e `DesenhoProceduralCircuitoTest` antes de implementar; ajustar/estender esses testes para validar o novo caminho (ex.: shape contínuo por trecho de curva) em vez de presumir `drawLine` por par de nós.
- **[Trade-off] Zebra/pista deixam de coincidir pixel a pixel com o traçado reto anterior** — aceitável e é o objetivo da mudança (o traçado reto é exatamente o defeito relatado), mas vale confirmar visualmente que a largura efetiva da pista/zebra não muda perceptivelmente perto dos nós-chave, já que a curva pode se afastar levemente da polilinha original entre dois nós.

## Migration Plan

Mudança puramente de desenho, sem dado persistido novo ou alterado — não há passo de migração. Deploy é a troca normal do jar (`mvn clean package -Ph2 -DskipTests`, conforme `CLAUDE.md`). Rollback, se necessário, é reverter o commit — nenhum estado em disco (XMLs de circuito, banco) precisa ser desfeito.

## Open Questions

- Qual tensão/parametrização exata da Catmull-Rom centrípeta (α) minimiza overshoot sem "cortar caminho" perto de nós-chave muito espaçados — decidir empiricamente durante a implementação, testando contra os circuitos existentes.
- O traçado do box (`desenhaPistaBox`/`desenhaTintaPistaBox`) deve receber a mesma suavização por consistência visual, mesmo o pedido original tendo focado no traçado principal da pista — recomendação é aplicar a mesma técnica ao box também (mesmo defeito de pontas existe lá), mas fica como decisão a confirmar durante a implementação caso o efeito visual no box seja indesejado (ex.: por ser um trajeto mais "geométrico"/reto por natureza).
