## Context

`MainPanelEditor.iniciarCriacaoObjeto()` (linha ~681) hoje trata três famílias de fluxo de criação: clique único posicionando o objeto (`posicionaObjetoPista=true`, usado por `ObjetoConstrucao`/`ObjetoPneus`, branch em `clickEditarObjetos` que faz `quina = clique - largura/2, altura/2`), encadeamento de pontos (`desenhandoObjetoLivre=true`, usado por `ObjetoLivre`/`ObjetoGuardRails`/`ObjetoArquibancada`/`ObjetoEscapada`/`ObjetoTransparencia`, finalizado por clique direito, acumulando pontos num único objeto), e agora um terceiro fluxo, introduzido por este change: pares de cliques gerando objetos independentes (só `ObjetoPneus`).

`ObjetoPneus` (`src/main/java/br/flmane/entidades/ObjetoPneus.java`) não tem `List<Point> pontos` — sua geometria é só `posicaoQuina` + `largura`/`altura` (contagem de pneus numa grade, não pixels — cada unidade renderiza como 10px, ver `desenha()`) + `angulo`. O pedido do usuário foi explícito: manter esse modelo de dados exatamente como está ("mantendo total compatibilidade"), só mudando como o editor preenche esses campos durante a criação.

`MemoriaPropriedadesObjeto` (`src/main/java/br/flmane/editor/MemoriaPropriedadesObjeto.java`) já existe e já cobre exatamente "lembrar largura/altura/ângulo/cores do último objeto editado, por classe" — `lembrar()` já é chamado a cada edição ao vivo em `FormularioObjetos.formularioObjetoPista()` (incluindo pela troca de cor, que já dispara `atualizaMain()`), e `aplicar()` já é chamado em toda criação de objeto (`iniciarCriacaoObjeto()`, linha ~696). Ou seja, a parte "memorizar o template do último objeto criado/editado na sessão, inclusive cores" já está implementada e funcionando antes deste change — o único trabalho deste change nessa frente é continuar chamando `MemoriaPropriedadesObjeto.aplicar()` para cada `ObjetoPneus` criado pelo novo facilitador de pares de cliques, e não sobrescrever a altura/cores que vêm de lá.

A linha de largada (`DesenhoProceduralCircuito.desenhaLinhaDeLargada`) já calcula seu ângulo automaticamente a partir da direção local da pista no nó de Largada (`GeoUtil.calculaAngulo(noLargada.getPoint(), noVizinho.getPoint(), 0)`), sem nenhum campo em `Circuito` pra sobrepor esse valor. Esse método já é compartilhado entre o editor e a geração de imagem em memória usada em corrida real, então qualquer override precisa valer nos dois lugares através do próprio `Circuito`, não só na UI do editor.

## Goals / Non-Goals

**Goals:**
- Colocar múltiplos `ObjetoPneus` alinhados ao longo de um traçado sem reabrir "Criar Objeto" a cada um, usando uma corrente contínua de cliques (estilo GuardRails/Arquibancada, com marcador magenta do ponto pendente) — cada segmento consecutivo (clique N, clique N+1) gera um objeto independente e imediatamente visível na lista/circuito, corretamente alinhado ao ângulo do segmento.
- Cada `ObjetoPneus` criado por esse fluxo continua sendo, campo a campo, um `ObjetoPneus` comum — nenhuma mudança na classe, no desenho, na serialização XML, ou em qualquer consumidor existente (`DesenhoProceduralCircuito`, `ControleCorrida`, etc.).
- `Circuito.anguloLargada` (novo, opcional) permite ajustar manualmente o ângulo da linha de largada quando o cálculo automático não é o desejado, editável por um spinner 0-360° no editor, ao lado do campo de largura da pista, pré-preenchido com o valor calculado quando ainda não há override.

**Non-Goals:**
- Nenhuma mudança em `ObjetoPneus` (classe/campos/desenho) — o facilitador só muda como o editor preenche `posicaoQuina`/`largura`/`angulo` na hora de criar, nada além disso.
- Nenhum novo mecanismo de "lembrar template" — reaproveita `MemoriaPropriedadesObjeto` já existente, sem alterar sua API.
- Nenhuma forma de cancelar a corrente de Pneus a meio de um segmento (ex.: depois do primeiro clique, antes do segundo) — consistente com a ausência de cancelamento em qualquer outro fluxo de criação por clique já existente no editor.
- Nenhum "rubber band" (linha de prévia acompanhando o mouse antes do próximo clique) — só o marcador do ponto pendente, mesmo estilo (magenta, sem linha até o cursor) que `desenhaPreObjetoLivre`/`GuardRails`/`Arquibancada`/`Escapada` já usam pros pontos ainda não conectados a um próximo clique.
- Nenhuma forma de "voltar pro automático" depois de definir um override de `anguloLargada` pela UI — o usuário pode ajustar manualmente pro valor calculado de volta (mesmo spinner, mesmo campo), mas não há um botão dedicado "resetar" nesta mudança.

## Decisions

### 1. Terceiro modo de criação: `desenhandoPneusEmSequencia` + `primeiroCliquePneus`, reaproveitando `posicionaObjetoPista`
Dois novos campos em `MainPanelEditor`: `boolean desenhandoPneusEmSequencia` (liga o modo) e `Point primeiroCliquePneus` (guarda o ponto pendente da corrente — o clique aguardando o próximo, pra fechar o próximo segmento; `null` só antes do primeiro clique da corrente inteira). `iniciarCriacaoObjeto()` ganha um `else if (objetoPista instanceof ObjetoPneus)` que zera `objetoPista` (o rascunho criado no início do método não é usado por este fluxo — cada segmento cria sua própria instância) e liga `desenhandoPneusEmSequencia=true`/`primeiroCliquePneus=null`, mantendo `posicionaObjetoPista=true` (já ligado antes do `if/else`, para todos os tipos).

Reaproveitar `posicionaObjetoPista=true` (em vez de introduzir um campo de supressão paralelo) foi a mesma decisão já tomada para o modo de colagem do change `editor-copiar-colar-objetos` — esse campo já é checado por ~6 guardas de clique/arraste no canvas (seleção, arraste, edição de pontos), então o novo modo herda a supressão de interações normais "de graça".

`clickEditarObjetos` ganha um novo ramo `else if (posicionaObjetoPista && desenhandoPneusEmSequencia)`:
- Botão direito: encerra a corrente (`desenhandoPneusEmSequencia=false`, `posicionaObjetoPista=false`, `primeiroCliquePneus=null`), sem criar nada a partir desse clique — mesmo padrão de finalização por clique direito já usado por Guard Rails/Arquibancada/Escapada. Os objetos já criados pelos segmentos anteriores permanecem no circuito (já são objetos reais desde o momento em que cada segmento fechou).
- Primeiro clique da corrente inteira (`primeiroCliquePneus == null`): só guarda o ponto pendente, sem criar nada ainda.
- Qualquer clique seguinte: cria o `ObjetoPneus` do segmento (ponto pendente anterior → este clique) (decisão 2), adiciona ao circuito, e o **próprio clique atual vira o novo ponto pendente** (`primeiroCliquePneus = ultimoClicado`, não `null`) — a corrente continua contínua, cada novo clique fecha um novo segmento com o clique imediatamente anterior, até o botão direito.

- **Revisão pós-feedback do usuário**: a primeira versão deste change tratava os cliques em pares independentes e não sobrepostos (clique 1+2 → objeto A, clique 3+4 → objeto B, resetando o ponto pendente pra `null` depois de cada objeto). O usuário pediu explicitamente o "mesmo estilo de desenho de reta" — uma corrente contínua onde cada clique novo fecha um segmento com o clique imediatamente anterior (clique 1+2 → objeto A, clique 2+3 → objeto B, clique 3+4 → objeto C, ...) — resetar pra `null` foi substituído por continuar com o último clique.
- **Alternativa considerada**: reaproveitar `desenhandoObjetoLivre` (o mecanismo de encadeamento de pontos já usado por GuardRails/Arquibancada) e acumular os cliques num `List<Point>` do próprio `ObjetoPneus`. Rejeitada — contradiz "mantendo total compatibilidade com o objeto pneu que tem hoje" (não tem `pontos`) e "gerando objetos independentes" (o pedido explícito do usuário é objetos separados por segmento, não um único objeto acumulando pontos).

### 2. Cálculo do novo `ObjetoPneus`: o segmento clicado vira a linha de CENTRO do objeto, não uma de suas bordas
No clique que fecha um segmento: `new ObjetoPneus()`, `MemoriaPropriedadesObjeto.aplicar(novo)` (herda altura/cores do último Pneus lembrado — largura também é setada por `aplicar()`, mas é imediatamente sobrescrita a seguir), depois `setLargura(Math.max(1, Util.inteiro(distancia(pontoPendente, ultimoClicado) / 10.0)))` e `setAngulo(GeoUtil.calculaAngulo(pontoPendente, ultimoClicado, 0))`.

**Bug corrigido nesta revisão**: a primeira versão fazia `setPosicaoQuina(pontoPendente)` diretamente — funcionava só pra ângulo 0. `ObjetoPneus.desenha()` rotaciona a grade em torno do **próprio centro** (`generalPath.getBounds().getCenterX/Y()`, calculado a partir de `posicaoQuina` + tamanho, não em torno de `posicaoQuina`), então ancorar a quina direto no primeiro clique deixava o centro de rotação deslocado do segmento clicado pra qualquer ângulo != 0 — o objeto "não seguia o ângulo dos cliques" (relatado pelo usuário). A correção calcula a quina de trás pra frente, de forma que o ponto médio da aresta esquerda (pré-rotação) caia exatamente em `pontoPendente` depois de rotacionado — o que, por construção, também faz o ponto médio da aresta direita cair exatamente em `ultimoClicado` (já que `largura` e `angulo` vieram desse mesmo segmento):

```java
double larguraPx = larguraGrade * 10.0;
double alturaPx = novo.getAltura() * 10.0;
double rad = Math.toRadians(anguloGraus);
double centroX = pontoPendente.x + (larguraPx / 2.0) * Math.cos(rad);
double centroY = pontoPendente.y + (larguraPx / 2.0) * Math.sin(rad);
novo.setPosicaoQuina(new Point(
        Util.inteiro(centroX - larguraPx / 2.0),
        Util.inteiro(centroY - alturaPx / 2.0)));
```

Efeito colateral aceito: pra ângulo 0, a quina não fica mais exatamente em `pontoPendente` (fica deslocada `altura/2` unidades pra cima) — é o preço de ter uma fórmula única, consistente pra qualquer ângulo, em vez de um caso especial só pra ângulo 0. `distancia()`/`GeoUtil.calculaAngulo()` já existem e já são usados em outros pontos do editor (nenhum cálculo geométrico novo introduzido, só a composição foi corrigida).

- **Alternativa considerada**: manter a quina no primeiro clique e, em vez disso, mudar `ObjetoPneus.desenha()` pra rotacionar em torno da quina em vez do centro. Rejeitada — violaria "mantendo total compatibilidade com o objeto pneu que tem hoje" (mudaria o comportamento de rotação de TODO `ObjetoPneus` já existente, incluindo os editados manualmente via diálogo antes deste change, não só os criados pelo facilitador).

### 3. Preview do ponto pendente: marcador magenta, mesmo estilo dos demais objetos ponto a ponto
Novo método `desenhaPreObjetoPneus(Graphics2D)`, chamado no mesmo bloco que já chama `desenhaPreObjetoLivre`/`GuardRails`/`Arquibancada`/`Escapada` (`paintComponent`). Desenha um único círculo preenchido em `Color.MAGENTA` (raio 6px) em `primeiroCliquePneus`, quando `desenhandoPneusEmSequencia` está ativo e há um ponto pendente — mesma cor/raio que os demais previews já usam pros pontos clicados, mas sem linha conectando (não há uma lista de pontos aqui, só um ponto pendente por vez).

- **Alternativa considerada**: desenhar também uma linha do ponto pendente até a posição atual do mouse (rubber band), pra mostrar o segmento sendo formado antes do próximo clique. Rejeitada por ora (ver Non-Goals) — exigiria capturar `mouseMoved` e guardar a posição atual do cursor como novo estado, algo que nenhum outro fluxo de criação por clique deste editor faz hoje; o marcador do ponto pendente já comunica visualmente onde a corrente está.

Como `ObjetoPneus` é sempre um objeto de cenário (`TipoObjetoPista.PNEUS.isCenario() == true`), o objeto vai direto para `circuito.getObjetosCenario()` — sem precisar checar `criandoObjetoCenario` (que nem chega a ser relevante pra este fluxo, já que não passa pelo branch de criação genérica).

Não é chamado `forcarAtualizacaoArea()` (o helper introduzido em `editor-copiar-colar-objetos` pra objetos colados) — esse objeto é **recém-criado**, nunca desenhado antes, então sua área começa vazia/zerada, caso em que `estaVisivelNoViewport()` já tem a salvaguarda de não cortar (mesmo comportamento que a criação de objeto único já tem hoje).

- **Alternativa considerada** (confirmada com o usuário): os dois cliques definirem cantos opostos de um retângulo (determinando altura também, não só largura). Rejeitada pelo usuário — a distância deve virar largura e o ângulo deve vir da direção entre os cliques; a altura continua vindo do template.
- **Decisão revista nesta rodada**: a primeira versão ancorava a quina diretamente no primeiro clique (pedido original do usuário, "canto no primeiro clique"). Como isso só ficava geometricamente correto pra ângulo 0 (ver bug corrigido acima), a quina agora é calculada de trás pra frente a partir do centro de rotação — o primeiro clique continua sendo o ponto de referência do segmento (a aresta esquerda do objeto, pré-rotação, cai nele), mas não é mais literalmente igual a `posicaoQuina` exceto quando o ângulo é 0.

### 4. `Circuito.anguloLargada`: `Double` opcional, não `double` primitivo
Novo campo `private Double anguloLargada;` (não persistido quando `null`, via `@JsonIgnore` — mesmo padrão de `multiplicadorLarguraPista` para exclusão do JSON de multiplayer; a persistência XML via `XMLEncoder`/bean já omite propriedades iguais ao valor padrão de uma instância nova, então um `Circuito` sem override não grava a propriedade no XML, preservando compatibilidade total com arquivos `_mro_meta.xml` já existentes). `null` = "usa o ângulo calculado automaticamente"; um valor não-nulo sobrepõe. Incluído em `copiaParaArquivoMetadados()` (a cópia que vira o arquivo `_mro_meta.xml`), já que é metadado de circuito, não campo derivado do traçado nem objeto.

`DesenhoProceduralCircuito.desenhaLinhaDeLargada(...)` passa a calcular `anguloPista` como `circuito.getAnguloLargada() != null ? circuito.getAnguloLargada() : GeoUtil.calculaAngulo(noLargada.getPoint(), noVizinho.getPoint(), 0)` — como esse método já é compartilhado entre o editor e a geração de imagem em memória usada em corrida real, o override vale nos dois automaticamente, sem duplicar lógica.

Novo helper público `DesenhoProceduralCircuito.calculaAnguloNaturalLargada(Circuito)` extrai a mesma busca de nó de largada/vizinho + cálculo de ângulo (sem considerar o override), usado só pelo editor pra pré-preencher o spinner com o valor calculado quando ainda não há override salvo.

- **Alternativa considerada**: `double anguloLargada` primitivo com um `boolean anguloLargadaDefinido` separado (ou um valor sentinela, ex. `-1`, já que ângulos válidos são 0-360). Rejeitada — `Double` nulo já é o padrão idiomático deste projeto pra "opcional, com fallback calculado" (mesmo raciocínio que outros campos opcionais do modelo), mais simples de raciocinar que um sentinela mágico ou um segundo campo booleano.

### 5. Spinner do editor: valor inicial vem do construtor do `SpinnerNumberModel`, sem re-sincronização em `refletirCircuitoNosCampos()`
`gerarTopoNavegacaoEAcoes()` (chamado do zero, recriando todos os componentes da linha 1, toda vez que o editor troca de circuito — via `aplicarCircuitoCarregadoNaUI() → iniciaEditor() → gerarLayout() → gerarTopoNavegacaoEAcoes()`) monta o `anguloLargadaSpinner` com o valor inicial já resolvido (override salvo, ou `calculaAnguloNaturalLargada(circuito)`, ou `0` se nada disso está disponível ainda) **antes** de anexar o `ChangeListener` — como a criação do `SpinnerNumberModel` não dispara esse listener, popular o valor inicial não grava, sozinho, um override em `circuito.anguloLargada`; só uma interação real do usuário no spinner (que chama `circuito.setAnguloLargada(...)`) grava.

Diferente de `larguraPistaSpinner` (que tem uma linha extra em `refletirCircuitoNosCampos()` re-sincronizando seu valor depois da troca de circuito), `anguloLargadaSpinner` **não** ganhou essa linha extra deliberadamente: como o spinner já nasce recriado do zero a cada troca de circuito com o valor certo (via construtor), uma chamada adicional de `setValue(...)` em `refletirCircuitoNosCampos()` só reintroduziria o risco de disparar o `ChangeListener` (já anexado nesse ponto) e gravar um override "acidental" igual ao valor calculado — sem nenhum ganho, já que o valor já está correto desde a construção.

- **Alternativa considerada**: um campo de supressão booleano (`ajustandoAnguloLargadaProgramaticamente`), permitindo reintroduzir uma chamada de sincronização em `refletirCircuitoNosCampos()` sem o risco de gravar um override acidental. Rejeitada por não ser necessária — como o spinner é sempre recriado do zero a cada troca de circuito (não reaproveitado), o valor inicial via construtor já é suficiente; adicionar um campo de supressão só pra uma sincronização redundante seria complexidade sem ganho.

### 6. Bug real (2ª rodada): `ObjetoDesenho.setAngulo` clampava ângulo negativo pra 0, em vez de normalizar módulo 360
Depois da correção da decisão 2 (cálculo da quina), o usuário reportou que o ângulo *ainda* nascia errado em cliques reais no circuito SPA — mas só em alguns segmentos, não em todos. Como a hipótese de estado obsoleto (jar não reconstruído) já tinha sido descartada, a investigação seguinte adicionou logs temporários (`[PNEUS-DEBUG]`) em cada ponto do fluxo de criação (clique recebido, ângulo calculado, ângulo lido de volta após `setAngulo`) e pediu uma sessão de teste real do usuário no circuito SPA para capturar `logs/flmane.log`.

O log revelou o padrão exato: `GeoUtil.calculaAngulo(...)` sempre calculava o valor matematicamente correto (incluindo negativos, ex. `-177.27°`, `-150.02°`, `-107.99°`), mas a leitura de `novoPneus.getAngulo()` **imediatamente depois** de `novoPneus.setAngulo(anguloCalculado)` — na mesma chamada de método, duas linhas de log depois — voltava `0.0` sempre que o valor calculado era negativo. Ou seja: o bug nunca esteve no cálculo do ângulo nem no fluxo de cliques do facilitador (ambos sempre corretos) — estava no próprio setter.

`ObjetoDesenho.setAngulo(double)` (`src/main/java/br/flmane/entidades/ObjetoDesenho.java`, superclasse de `ObjetoPneus`/`ObjetoLivre`/`ObjetoArquibancada`/`ObjetoConstrucao`/`ObjetoGuardRails`, entre elas e `ObjetoPista`) fazia `super.setAngulo(Math.max(0, angulo))` — um clamp que descarta qualquer parte negativa, zerando o ângulo em vez de convertê-lo pro equivalente positivo. O próprio javadoc da classe já documentava a semântica *pretendida* ("o ângulo não faz sentido negativo, equivalente ao ângulo positivo correspondente, módulo 360") — a implementação nunca bateu com esse comentário; é um bug pré-existente à parte deste change (o arquivo não fazia parte do diff desta mudança), só exposto porque este é o primeiro fluxo do editor que calcula um ângulo diretamente de `atan2` (que retorna valores em `(-180°, 180°]`) e passa o resultado bruto pra `setAngulo` — os outros quatro tipos de `ObjetoDesenho` só recebem ângulo por spinner (sempre 0-360 na UI) ou por XML legado.

Correção (afeta os cinco tipos de `ObjetoDesenho`, não só Pneus):
```java
@Override
public void setAngulo(double angulo) {
    double normalizado = angulo % 360;
    if (normalizado < 0) {
        normalizado += 360;
    }
    super.setAngulo(normalizado);
}
```

`ObjetoDesenhoLimitesTest` (teste pré-existente, fora deste change) tinha dois casos que fixavam o comportamento antigo (`setAngulo(-1)` esperando `0`) — atualizados pra esperar o equivalente positivo módulo 360 (`-1` → `359`, `-5` → `355`), consistente com o javadoc da própria classe. Novo teste de regressão em `MainPanelEditorCriarPneusSequenciaTest` (`doisCliques_comAnguloNegativoDoAtan2_naoZeraOAngulo`) cobre o caso real que disparou o bug: um segmento cujo `atan2` dá ângulo negativo.

- **Alternativa considerada**: normalizar o ângulo no próprio ramo de criação de Pneus (`clickEditarObjetos`), somando 360 se `anguloGraus < 0` antes de chamar `setAngulo`. Rejeitada — o bug é no setter, não no cálculo; normalizar só ali deixaria os outros quatro tipos de `ObjetoDesenho` (e qualquer código futuro que calcule ângulo por `atan2`/`GeoUtil.calculaAngulo` e chame `setAngulo` diretamente) vulneráveis ao mesmo clamp silencioso.
- **Licão pro processo de debugging**: raciocínio estático (revisão de código, leitura de bytecode, re-simulação manual do fluxo de cliques) não encontrou o bug porque o defeito estava numa camada mais funda (o setter da superclasse) do que qualquer hipótese cogitada (posicionamento de clique, estado da corrente, cálculo geométrico) — só logs de uma sessão real do usuário, isolando o valor exato em cada etapa, apontaram a linha exata onde o valor mudava inesperadamente.

### 7. Causa raiz do "Objeto 70: null" — `objetoPista.setNome()` depois de `listarObjetos()` no ramo genérico de posicionamento por clique único
O usuário reportou um objeto sem nome (exibindo "null" na lista) no circuito SPA, e depois capturou o `NullPointerException` real ao vivo:
```
java.lang.NullPointerException: Cannot invoke "ObjetoPista.setNome(String)" because "this.this$0.objetoPista" is null
    at MainPanelEditor$44.clickEditarObjetos(MainPanelEditor.java:2338)
```

Causa: o ramo genérico `posicionaObjetoPista && objetoPista != null` (usado por `ObjetoConstrucao`, entre outros tipos sem um ramo dedicado) adicionava o objeto à lista, chamava `formularioListaObjetos.listarObjetos()` (que limpa e repopula o `DefaultListModel`, passando por um instante de seleção vazia — já documentado no próprio `listarObjetos()`), e só then chamava `objetoPista.setNome(...)` — usando o **campo**, não uma variável local. O `ListSelectionListener` de `FormularioListaObjetos` reage a esse instante de seleção vazia chamando `editor.setObjetoPista(null)`, zerando o campo antes do `setNome()` rodar: o objeto ficava no circuito (já tinha sido adicionado à lista antes), mas sem nome — exatamente o "Objeto 70: null" relatado.

Todos os outros ramos de criação (`ObjetoLivre`/`ObjetoGuardRails`/`ObjetoArquibancada`/`ObjetoEscapada`/`ObjetoTransparencia`/paste/Pneus) já evitavam esse problema fazendo cast do objeto pra uma variável local logo no início do ramo (ex. `ObjetoGuardRails guardRails = (ObjetoGuardRails) objetoPista;`) e usando essa variável, não o campo, depois de `listarObjetos()` — só este ramo genérico usava o campo diretamente.

Correção: mesma técnica, aplicada a este ramo — `ObjetoPista novoObjeto = objetoPista;` capturado antes de `listarObjetos()`, usado para `setPosicaoQuina`/`add`/`setNome`/`reprocessaEscapadaSeNecessario`.

Teste de regressão: `MainPanelEditorCriarObjetoConstrucaoCliqueTest` reproduz a pré-condição exata (um objeto já selecionado na lista antes do clique de posicionamento, pra `listarObjetos()` realmente transicionar de "1 selecionado" pra "nada selecionado") e confirma que o clique não lança exceção e que o objeto criado recebe um nome.

## Risks / Trade-offs

- [Trade-off] Sem forma de cancelar a corrente de Pneus no meio de um segmento (depois do primeiro clique) — o usuário precisa completar o próximo clique (mesmo objeto pequeno/quase-zero é criado se os dois cliques forem muito próximos, `largura` clampada em no mínimo 1). Aceito como consistente com a ausência de cancelamento em qualquer outro fluxo de criação por clique do editor.
- [Trade-off] Cliques muito próximos (distância < 5px) geram um `ObjetoPneus` de `largura=1` (grade de 1 pneu de largura) em vez de falhar ou pedir confirmação — comportamento previsível e sem exceção, mas pode surpreender um usuário que clicou duas vezes quase no mesmo lugar por engano. Aceito — nenhuma validação adicional foi pedida, e o objeto criado por engano pode ser removido normalmente pela lista.
- [Trade-off] Pra ângulo 0, a quina do objeto criado não coincide mais exatamente com o primeiro clique (fica deslocada `altura/2` unidades de grade "acima" dele) — efeito colateral aceito da fórmula única que corrige o bug de ângulo (decisão 2); o segmento clicado continua sendo a linha de centro do objeto pra qualquer ângulo, incluindo 0.
- [Risco, mitigado] Se `gerarTopoNavegacaoEAcoes()` deixar de ser recriado do zero a cada troca de circuito no futuro (reaproveitando componentes, como uma otimização hipotética), o spinner de ângulo de largada precisaria de uma sincronização explícita com supressão de listener (decisão 5) — não é o comportamento atual, mas fica registrado como algo a revisar se esse padrão de reconstrução mudar.
