## Context

- O editor de circuitos (`MainPanelEditor`, um único `JPanel` de até 10000×10000 dentro de um `JScrollPane` sem scrollbars nativas, panning feito por `esquerda()/direita()/cima()/baixo()`) percorre `todosObjetos()` (concatenação de `circuito.getObjetos()` + `getObjetosCenario()`) uma vez por nível de desenho em `desenhaObjetosNivel`, chamando `desenha()` incondicionalmente em cada objeto, mesmo fora da área visível. `limitesViewPort()` já existe e retorna o retângulo visível (usado hoje só para posicionar HUD), mas não é usado para decidir o que desenhar.
- `ObjetoLivre` já tem 4 tipos de padrão (`POLIGONO_SIMPLES`, `VEGETACAO_DENSA`, `VEGETACAO_SIMPLES`, `AGUA`, `BRITA`), todos roteados por `desenhaComClipSemAntialiasing`, que recebe a forma **já rotacionada e escalada** (`formaFinal`) como base do clip, e desenha a grade/dispersão do padrão em coordenadas de tela eixo-alinhadas — por isso a rotação do objeto (`angulo`) gira a silhueta mas não o conteúdo do padrão.
- `ObjetoGuardRails` já implementa o paradigma "objeto de caminho definido por cliques no canvas": clique esquerdo adiciona ponto, clique direito finaliza, com snap a nós de pista (`aplicaSnap`, tolerância `RAIO_SNAP_GUARD_RAILS_PX=10`) e um modo de edição de pontos pós-criação (mover/inserir/remover). É a referência direta para o novo `ObjetoEscapada`.
- `No.tracado` é um índice inteiro de linha: `0` implícito (pista central, `circuito.getPista()`), `1`/`2` são as bordas offset geradas por `gerarTracado1e2Pista()`/`gerarTracado1e2Box()` (`circuito.getPista1Full()`/`getPista2Full()`), e `4`/`5` são as trilhas de escapada do modelo antigo (`pista4Full`/`pista5Full`, geradas por `preencheTracadoEscapeSuave`) — que serão removidas.
- `Circuito.getEscapeMap()` (`Map<PontoEscape, List<No>>`) é consumido só por `Piloto.processaEscapadaDaPista()`/o lookahead de escapada em `Piloto.java`, que é explicitamente fora de escopo desta mudança.
- `ObjetoConstrucao` usa uma única constante `MARGEM_INTERNA=10` tanto para a margem entre forma externa/interna (todos os tipos) quanto para o vão entre os dois módulos do `CAMINHAO`.

## Goals / Non-Goals

**Goals:**
- Pular a chamada de `desenha()` para objetos fora do viewport no editor, sem mudar ordem/níveis de desenho nem a aparência de objetos visíveis.
- Fazer o `angulo` de `ObjetoLivre` rotacionar só o conteúdo do padrão (para todos os tipos não sólidos, existentes e novos), sem nunca rotacionar a silhueta/contorno do objeto livre em si.
- Adicionar os tipos `LISTRADO` e `XADREZ` a `TipoObjetoLivre`, reaproveitando o mesmo mecanismo de clip/desenho dos padrões existentes.
- Redesenhar `ObjetoEscapada` como objeto de caminho de dois pontos (saída e retorno) ancorado a nós de traçado 1/2, com validação de proximidade e alerta, no mesmo espírito de UX do `ObjetoGuardRails`.
- Reduzir à metade o vão entre os módulos do `CAMINHAO`, sem afetar a margem de borda usada pelos demais tipos.

**Non-Goals:**
- Não altera `Piloto.java` nem o comportamento de corrida ao escapar da pista — fica para uma mudança futura que reconecte o novo `ObjetoEscapada` ao runtime.
- Não introduz índice espacial (quadtree/grid) para o culling — interseção linear de retângulos é suficiente para a quantidade de objetos por circuito.
- Não muda o mecanismo de linhas finas do `ObjetoGuardRails` nem o desenho aninhado dos demais tipos de `ObjetoConstrucao`.
- Não persiste referência a `No.index` no novo `ObjetoEscapada` — como o `ObjetoGuardRails`, guarda apenas as posições (`Point`) resultantes do snap.

## Decisions

### D1 — Culling por retângulo circunscrito, sem índice espacial
`desenhaObjetosNivel` passa a consultar `limitesViewPort()` uma vez por chamada e, para cada `ObjetoPista`, calcular um retângulo de teste antes de invocar `desenha()`:
- `angulo == 0`: usa `obterArea()` diretamente.
- `angulo != 0`: usa um retângulo expandido centrado no centro de `obterArea()`, com metade do lado igual ao raio do círculo circunscrito (`Math.hypot(largura, altura)/2`) — cobre qualquer orientação sem recalcular `AffineTransform` a cada frame.
Um objeto só é desenhado se esse retângulo intersecta `limitesViewPort()`.

Alternativa considerada: reaproveitar `obterAreaClique()` (já é rotação-aware). Descartada porque ela existe para tolerância de clique (infla a área por uma margem de interação), não para bounds geométricos — o raio circunscrito é mais barato (sem `AffineTransform`) e nunca sub-estima a área ocupada.

O culling se aplica apenas ao laço principal de `desenhaObjetosNivel`; overlays de preview/edição (objeto em criação, marcadores de ponto, seleção) continuam sempre desenhados, como hoje.

### D2 — Padrão rotaciona via transform do `Graphics2D`; a silhueta NUNCA rotaciona
**Revisado após feedback do usuário**: o pedido original ("fazer o ângulo afetar o padrão") foi inicialmente implementado fazendo `angulo` rotacionar a silhueta E o padrão juntos (mesmo comportamento de rotação de sempre, só que agora também aplicado ao padrão). O usuário corrigiu: `angulo` deve afetar **só a angulação do padrão**, nunca o contorno do objeto livre em si — o objeto livre passa a ser desenhado sempre na mesma orientação, e é só o conteúdo do padrão que gira dentro dele.

Implementação final em `desenha()`/`desenhaComClipSemAntialiasing`:
1. O preenchimento sólido de fundo usa `formaZoomLocal` (path traduzido por `posicaoQuina` e escalado por `zoom`, **sem nenhuma rotação**) — a silhueta desenhada nunca depende de `angulo`.
2. Para o padrão: `g2d.clip(formaZoomLocal)` é aplicado **antes** de rotacionar o transform do `Graphics2D` — como o clip fica fixo em espaço de dispositivo assim que definido, ele trava o padrão dentro do contorno real e fixo do objeto. Só depois disso o `Graphics2D` é rotacionado (`g2d.rotate(rad, pivoX, pivoY)`, pivô = centro da forma em espaço de tela) e a lambda de desenho do padrão roda usando `formaZoomLocal.getBounds()` como base da grade, em coordenadas locais.
3. Como o sistema de coordenadas do `Graphics2D` já está rotacionado quando a lambda desenha (mas o clip não), toda primitiva do padrão (linhas, arcos, óvalos) sai rotacionada na tela, sempre recortada pela silhueta fixa — sem que `desenhaBrita`/`desenhaPadraoVegetacao`/`desenhaPadraoEmGrade`/os dois novos métodos precisem saber sobre rotação.

A ordem clip-antes-de-rotacionar é o ponto chave: clipar DEPOIS de rotacionar (a primeira versão) faz o próprio clip girar junto com o padrão, arrastando a silhueta junto — inverter a ordem desacopla os dois.

`ObjetoPista.obterAreaClique()` (base) rotaciona a área de clique pelo `angulo` para coincidir com a forma desenhada — como a silhueta de `ObjetoLivre` não rotaciona mais, essa premissa deixa de valer só para esse subtipo; `ObjetoLivre` passa a sobrescrever `obterAreaClique()` para nunca rotacionar (só expandir pela margem de tolerância), reaproveitando a constante `TOLERANCIA_CLIQUE_PX` (promovida de `private` para `protected` em `ObjetoPista`).

Alternativa considerada: pré-rotacionar cada primitiva individualmente. Descartada por exigir reescrever todos os métodos de padrão; a abordagem por transform isola a mudança em um único ponto (`desenhaComClipSemAntialiasing` + a chamada em `desenha()`).

Efeito aceito como **BREAKING** (já sinalizado no proposal): circuitos existentes com `ObjetoLivre` rotacionado (`angulo != 0`) mudam de aparência — a silhueta volta a ficar sempre na orientação original, e o padrão (quando não sólido) passa a girar dentro dela.

### D3 — LISTRADO e XADREZ seguem o mesmo template dos padrões existentes
Novos métodos `desenhaPadraoListrado`/`desenhaPadraoXadrez`, chamados a partir do mesmo dispatch de `tipo` em `desenha()`, ambos roteados por `desenhaComClipSemAntialiasing`:
- `LISTRADO`: listras retas paralelas ao eixo X local, largura de listra baseada em `PASSO_PADRAO_LOCAL * zoom`, alternando `corSecundaria`/vazio.
- `XADREZ`: grade de células quadradas do mesmo passo, preenchendo com `corSecundaria` quando `(coluna + linha) % 2 == 0`.
Por serem desenhados em espaço local (pós-D2), a rotação já os afeta automaticamente, sem código extra específico de rotação.

### D3.1 — Área de geração do padrão expandida ao raio circunscrito (cobertura total após rotação)
**Revisado após feedback do usuário**: os cinco métodos de padrão (`desenhaPadraoEmGrade`, `desenhaPadraoVegetacao`, `desenhaBrita`, `desenhaPadraoListrado`, `desenhaPadraoXadrez`) geravam a grade/dispersão só sobre `formaLocal.getBounds()` (o bounding box exato da forma, com uma margem de ~1 passo). Para uma forma não quadrada, rotacionar essa grade (D2) faz sua cobertura "encolher" em alguma direção — os cantos da silhueta fixa (mais distantes do centro que os lados) ficam fora do alcance da grade rotacionada e aparecem sem padrão (só `corPimaria`, nunca tocados por `corSecundaria`).

Correção: novo helper `areaCoberturaPadrao(Shape formaLocal)` retorna um quadrado concêntrico com o bounding box original, com metade do lado igual ao raio do círculo circunscrito desse bounding box (`Math.hypot(largura, altura) / 2`, o mesmo cálculo do raio circunscrito usado no culling de viewport, D1). Qualquer ponto do bounding box original está a, no máximo, esse raio de distância do centro — logo, mesmo depois de rotacionado por qualquer ângulo em torno do mesmo centro, esse quadrado expandido continua cobrindo 100% da área original. Os cinco métodos passam a usar `areaCoberturaPadrao(formaLocal)` em vez de `formaLocal.getBounds()` como base dos laços de geração; o clip (que continua sendo a forma real, não o quadrado expandido) recorta o excesso gerado fora da silhueta.

Trade-off aceito: para formas muito alongadas (proporção larga:estreita grande), a área de geração cresce bem mais que a área realmente visível (ex.: um retângulo 200×50 gera sobre um quadrado ~206×206 em vez de 200×50), custando mais iterações de laço. Aceitável dado que objetos no editor não costumam ter proporções extremas, e a alternativa (uma região de cobertura ajustada ao ângulo exato de rotação, mais apertada) exigiria recalcular a área a cada frame com base em `angulo`, adicionando complexidade sem necessidade clara.

### D4 — `ObjetoEscapada` como encadeamento de pontos (como GuardRails), com validação só nas duas pontas
**Revisado após feedback do usuário**: a primeira versão restringia o objeto a exatamente 2 pontos (saída fixa + retorno fixo). O usuário corrigiu: o objeto deve ser um encadeamento de pontos como `ObjetoGuardRails` (clique esquerdo adiciona ponto, clique direito finaliza), onde só o PRIMEIRO ponto (entrada) e o ÚLTIMO ponto (saída, definido pelo clique direito que finaliza) são validados contra o traçado — os pontos do MEIO (o trajeto da zona de escapada em si) são livres, sem validação nenhuma, podendo ficar em qualquer posição. O bug relatado ("valida no segundo nó, não no nó de entrada") vinha exatamente de o modelo anterior só suportar 2 pontos: um segundo clique esquerdo (que deveria ser um ponto livre do trajeto) era tratado como "redefinir a entrada" e re-validado incorretamente.

Modelo final (`ObjetoEscapada.pontos: List<Point>`, idêntico em espírito a `ObjetoGuardRails.pontos`):
- **Primeiro clique esquerdo** (nó de entrada): candidato é o `No` mais próximo do clique dentre `circuito.getPista1Full()` e `circuito.getPista2Full()` — nunca `circuito.getPista()` (traçado 0). Se a menor distância exceder a tolerância (`TOLERANCIA_ESCAPADA_PX`, 2px de tela), a criação é cancelada nesse clique, um `JOptionPane` (`Lang.msg("pontoEscapadaInvalido")`, `ERROR_MESSAGE`) avisa, e nenhum ponto é adicionado. O traçado aceito (`1` ou `2`) é guardado em `tracadoOrigem`; o índice do nó (`No.getIndex()`) é guardado em `indiceEntrada` (ver D7).
- **Cliques esquerdos seguintes** (trajeto livre): `escapada.getPontos().add(ultimoClicado)` direto, sem nenhuma validação.
- **Clique direito** (nó de saída, finaliza): mesma busca de nó mais próximo, restrita ao traçado igual a `tracadoOrigem`. Se inválido, mostra o mesmo alerta e não finaliza — objeto continua em modo de criação. Se válido, acrescenta o ponto, guarda o índice em `indiceSaida`, e finaliza (adiciona a `circuito.getObjetos()`).
- `gerar()` constrói o `GeneralPath` como uma polilinha reta (como GuardRails) por todos os pontos.
- Edição pós-criação (arrastar qualquer ponto) reaproveita a mesma regra: primeiro/último ponto validados ao soltar (revertendo com alerta se inválido), pontos do meio sempre aceitos como estão (sem validação) — ver `finalizarArrastePontoEscapada`.

Alternativa descartada (versão anterior desta mesma decisão): 2 pontos fixos sem trajeto livre. Rejeitada pelo usuário porque o trajeto de uma zona de escapada real raramente é uma linha reta simples entre entrada e saída.

### D7 — Índices de traçado (`indiceEntrada`/`indiceSaida`) para reconectar o "Teste Pista" do editor
**Adicionado após feedback do usuário**: apesar de `Piloto.java` (gameplay) continuar fora de escopo (D5), o usuário pediu explicitamente que o recurso "Teste Pista" do PRÓPRIO EDITOR (`TestePista.java`, carro de teste que percorre a pista dentro do `MainPanelEditor`, distinto de `Piloto.java`) reconhecesse e seguisse a nova escapada — isso não existia mais depois de D5 zerar `Circuito.getEscapeMap()`.

- `ObjetoEscapada` ganha `indiceEntrada`/`indiceSaida` (`int`, default `-1`): o índice (`No.getIndex()`, compartilhado entre `pistaFull`/`pista1Full`/`pista2Full`) do nó em que a entrada/saída foram validadas — gravado pelo editor no momento da validação (criação e edição), não recalculado depois.
- `TestePista.obterTracadoEscapadaAtivo(int index)` foi reescrito para iterar `circuito.getObjetos()` (em vez do extinto `circuito.getEscapeMap()`) procurando um `ObjetoEscapada` cujo intervalo `[indiceEntrada, indiceSaida]` cubra o índice atual do carro de teste — isso é justamente "o carro estar no traçado em que a escapada foi definida": o intervalo só existe porque a entrada foi ancorada a um nó real desse traçado.
- Dentro do intervalo, `TestePista.construirTracadoEscapada(...)` interpola por comprimento de arco ao longo de `escapada.getPontos()` (via `escapada.obterPontosAbsolutos()`), produzindo uma lista de nós do tamanho da pista (nulls fora da zona) — mesmo formato que `posicionaCarroConsiderandoEscapada`/`noNaListaOuFallback` já esperavam do antigo `escapeMap`, então o resto do mecanismo (incluindo o fallback pra pista normal fora da zona) não precisou mudar.
- Explicitamente fora de escopo: `Piloto.java` continua sem nenhuma ligação a `ObjetoEscapada`/`indiceEntrada`/`indiceSaida` — a reconexão é só para o carro de teste do editor.

### D8 — Bug corrigido: `posicaoQuina` precisa resincronizar após cada edição de ponto
**Encontrado e corrigido durante a validação do feedback do usuário** ("funciona só depois de recarregar a pista"): `calculaOffsetTelaEscapada` (e o equivalente de GuardRails, `calculaOffsetTelaGuardRails`) calculam o deslocamento local↔tela como `posicaoQuina - bounds(pontos).location`. Antes desta correção, depois de mover um ponto (`pontos.set(...)` + `gerar()`), `posicaoQuina` NUNCA era resincronizado com os novos bounds — então a PRÓXIMA vez que o deslocamento fosse recalculado (no mesmo arrasto ou num arrasto seguinte), ele vinha errado, e a posição gravada do ponto saía deslocada da posição realmente validada/clicada — mesmo com o ÍNDICE de nó (`indiceEntrada`/`indiceSaida`) gravado corretamente (esses vêm direto da busca por nó mais próximo, não passam pelo deslocamento). Isso explica o sintoma relatado: só recarregar o circuito (que sempre revetoriza e reconstrói os objetos do zero, sem nenhum deslocamento acumulado) "resolvia".

Correção: depois de todo `gerar()` disparado por uma edição de ponto (em `mouseDragged` e nos dois ramos de `finalizarArrastePontoEscapada`), chamar `setPosicaoQuina(obterArea().getLocation())` para restabelecer a invariante `posicaoQuina == bounds(pontos).location` (deslocamento sempre `0,0`) antes da próxima leitura do deslocamento.

`ObjetoGuardRails` tem exatamente o mesmo padrão de código (mesma falta de resincronização) e provavelmente sofre do mesmo bug — sinalizado como tarefa separada (fora do escopo desta mudança, que é sobre `ObjetoEscapada`), não corrigido aqui.

### D5 — Remoção do modelo antigo de `ObjetoEscapada` e dos dados derivados
- `ObjetoEscapada` perde os campos reinterpretados como comprimento/amplitude e o desenho elíptico; passa a ser um encadeamento de pontos com validação nas duas pontas (D4), continuando como "objeto de função" (lista `objetos`, fora do sistema de níveis) como hoje.
- **Revisado durante a implementação:** `Circuito.java` mantém os campos `pista4Full`/`pista5Full`/`escapeMap` e seus getters (`getPista4Full()`, `getPista5Full()`, `getEscapeMap()`) — a leitura do código mostrou que `Piloto.java` e `PainelCircuito.java` acessam `getPista4Full()`/`getPista5Full()` diretamente por índice (`.get(noAtual.getIndex())`), então removê-los quebraria essa leitura fora da classe Escapada, violando a restrição de não alterar a implementação de escapada em `Piloto.java`. Em vez de remover, `gerarEscapeMap()` teve o corpo que lia a API antiga de `ObjetoEscapada` removido (a criação de zonas de escapada some, mas as listas continuam do tamanho certo, populadas só com `null`, exatamente como já acontecia para qualquer circuito sem `ObjetoEscapada`). `preencheTracadoEscapeSuave()`, sem mais chamador, foi removida por estar morta.
- **Revisado:** `PontoEscape.java` é mantido (não removido) — é o tipo de chave de `Map<PontoEscape, List<No>>` retornado por `getEscapeMap()`, lido diretamente por `Piloto.java`, `PainelCircuito.java`, `ControleRecursos.java`, `MainPanelEditor.java` e `TestePista.java`; removê-lo quebraria a compilação desses 5 arquivos, todos fora do escopo desta mudança.
- Os 33 arquivos `src/main/resources/circuitos/*.xml` com blocos `<object class="br.f1mane.entidades.ObjetoEscapada">` têm esses blocos removidos numa passagem única (script Python, não em runtime), preservando o restante do XML — validado removendo cada bloco `<void method="add">...</void>` completo (não só o `<object>` interno) e checando que nenhum `id` XMLEncoder definido dentro de um bloco removido é referenciado (`idref`) fora dele.
- O overlay de debug de `PainelCircuito.desenhaDebugIinfo` (gated por `Global.DEBUG`, fora de `Piloto.java`) foi adaptado para usar `ObjetoEscapada.obterPontosAbsolutos()` em vez da API antiga (`getLargura()`/`getAltura()` como gatilho) — sem essa adaptação o bloco continuaria compilando mas nunca desenharia nada, já que um `ObjetoEscapada` construído só com `posicaoQuina` (sem `pontoSaida`/`pontoRetorno`) não desenha mais nada no novo modelo.
- Testes que cobrem hoje o modelo antigo (`CircuitoEscapeMapTest`, `TestePistaEscapadaTest`, e menções em `DesenhoProceduralCircuitoTest`, `ObjetoPistaNivelDesenhoTest`, `ObjetoDesenhoLimitesTest`, `MainPanelEditorNivelObjetoTest`, `MainPanelEditorDesenhaObjetosCheckboxTest`, `FormularioObjetosObjetoLivreTest`, `CircuitoMetadadosArquivoTest`) são atualizados para o novo modelo ou removidos quando testavam exclusivamente o comportamento eliminado.

### D6 — Vão do CAMINHAO como constante separada da margem de borda
Introduz `VAO_MODULOS_CAMINHAO = MARGEM_INTERNA / 2` (5px) em `ObjetoConstrucao`, usada apenas em `larguraEfetiva()` e `desenhaCaminhao()` no lugar de `MARGEM_INTERNA`; `MARGEM_INTERNA` continua em 10px para a margem entre forma externa/interna de todos os tipos (incluindo os dois módulos do próprio CAMINHAO).

Alternativa considerada: tornar o vão um campo editável no formulário, como `larguraLinhaGuardRails`. Descartada por não ter sido pedida — o pedido foi reduzir o valor fixo pela metade.

## Risks / Trade-offs

- [Remover `ObjetoEscapada` dos 33 circuitos apaga posicionamento manual já feito] → Comunicado como **BREAKING** no proposal; o modelo antigo (elipse solta) é geometricamente incompatível com o novo (par de pontos no traçado), então não há conversão automática razoável — reposicionamento é manual, no novo editor, após a mudança.
- [Tolerância de "2 pixels" pode ser frustrante em zoom baixo, se interpretada como pixels de mundo] → Assumida como pixels de **tela** (pós-zoom), mesma escala do `RAIO_SNAP_GUARD_RAILS_PX` do GuardRails; ver Open Questions.
- [`getEscapeMap()` sempre vazio muda o comportamento de corrida silenciosamente] → Aceito como comportamento correto até a reconexão futura; documentado aqui e no proposal para não ser lido como regressão não intencional.
- [Retângulo circunscrito do culling é conservador para objetos rotacionados, desenhando um pouco além do estritamente visível] → Aceitável: o objetivo é pular objetos claramente fora de tela; over-inclusão nunca causa corte visual incorreto.
- [Transform do `Graphics2D` em D2 precisa sempre ser restaurado, mesmo em exceção] → Reaproveita o padrão `try/finally` já existente em `desenhaComClipSemAntialiasing`.

## Migration Plan

1. D1 (culling) — isolado, sem dependência dos demais.
2. D2 + D3 (rotação de padrão + novos tipos) em `ObjetoLivre`/`TipoObjetoLivre`/`FormularioObjetos`.
3. D6 (vão do CAMINHAO) — mudança pequena e isolada.
4. D4 + D5 (novo `ObjetoEscapada`): primeiro o novo modelo e fluxo de clique, depois a remoção do código/dados antigos e a limpeza dos 33 XMLs, por último o ajuste dos testes afetados.

Sem necessidade de rollback em produção (mudança de editor/dados de circuito, não de runtime de servidor); reverter é possível via `git revert` já que os XMLs alterados ficam sob controle de versão.

## Open Questions

- A tolerância "no máximo dois pixels" citada pelo usuário: confirmar se são pixels de tela (pós-zoom) ou de mundo. Assumida aqui como pixels de tela, mesma escala do snap do GuardRails.
- A validação do ponto de retorno foi descrita como "ainda maior" que a da saída: confirmar se isso significa apenas a exigência adicional de mesmo traçado (assumido aqui), ou também uma tolerância de distância mais apertada que os 2px da saída.
- Se o novo `ObjetoEscapada` deve suportar edição de pontos pós-criação (mover saída/retorno) reaproveitando a UI de GuardRails, ou se por ora só criação nova é suportada (apagar e recriar). Assumido aqui que a edição é suportada, por consistência com GuardRails.
