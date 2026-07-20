## Why

Colocar vários `ObjetoPneus` ao longo de uma barreira (ex.: contornando uma curva) hoje exige repetir "Criar Objeto" > "Pneus" > clicar, um por um, sempre nascendo no ângulo 0 (sem seguir a curva) e no tamanho do último objeto editado — sem nenhuma forma rápida de alinhar cada bloco com o traçado. Além disso, o ângulo da linha de largada (quadriculado preto/branco) é sempre calculado automaticamente a partir da direção local da pista no nó de Largada, sem nenhuma forma de ajustá-lo manualmente quando o cálculo automático não corresponde ao que o autor do circuito quer.

## What Changes

- "Criar Objeto" > "Pneus" SHALL passar a usar um facilitador de desenho por uma corrente contínua de cliques, no mesmo estilo visual (marcador magenta do ponto pendente) de Guard Rails/Arquibancada — mas sem acumular pontos num único objeto: cada clique novo (a partir do segundo) fecha um segmento com o clique imediatamente anterior, gerando um `ObjetoPneus` **independente** por segmento (clique 1+2 → objeto A, clique 2+3 → objeto B, clique 3+4 → objeto C, ...). Em cada segmento, o ponto pendente (clique anterior) e o clique atual viram a linha de centro do objeto: a distância entre eles vira a largura (em unidades de grade, 10px cada — mesma unidade que `ObjetoPneus` já usa) e o ângulo entre eles vira a rotação. Total compatibilidade com o objeto de sempre (mesmos campos, mesmo desenho) — não um novo tipo de objeto nem um objeto que acumula pontos.
- **Correção de bug**: a posição do objeto criado SHALL ser calculada de forma que o segmento clicado corresponda exatamente à linha de centro do objeto (o cálculo original ancorava a quina diretamente no primeiro clique, o que só ficava correto pra ângulo 0 — `ObjetoPneus.desenha()` rotaciona em torno do próprio centro, não da quina, então em qualquer outro ângulo o objeto criado não seguia o ângulo dos cliques).
- Enquanto a corrente está ativa, o editor SHALL exibir um marcador magenta no ponto pendente (o clique aguardando o próximo, pra fechar o próximo segmento) — mesmo estilo de preview já usado por GuardRails/Arquibancada/ObjetoLivre/Escapada durante a criação.
- A corrente SHALL continuar ativa após cada segmento fechado (pronta pro próximo clique, sem precisar reabrir "Criar Objeto"), até o usuário clicar com o botão direito, que encerra a corrente sem criar mais nenhum objeto — os objetos já criados pelos segmentos anteriores permanecem no circuito.
- A altura e as cores de cada `ObjetoPneus` criado pela corrente SHALL vir da memória de "último objeto Pneus editado/criado" (mecanismo `MemoriaPropriedadesObjeto` já existente no editor, reaproveitado — não um novo mecanismo), enquanto a largura e o ângulo SHALL vir de cada segmento, conforme acima.
- `Circuito` SHALL expor uma nova propriedade `anguloLargada` (graus, opcional): quando definida, sobrepõe o ângulo calculado automaticamente a partir da direção local da pista pra desenhar a linha de largada (quadriculado); quando não definida (circuitos existentes, ou nunca alterada), o comportamento é idêntico ao de hoje (cálculo automático).
- O editor de circuitos SHALL exibir um spinner de ângulo (0 a 360°) ao lado do campo de largura da pista, pré-preenchido com `anguloLargada` já salvo ou, se ainda não houver override, com o ângulo calculado automaticamente — editável, gravando um override em `circuito.anguloLargada` assim que o usuário alterar o valor.

## Capabilities

### New Capabilities
- `editor-pneus-multiplos-cliques`: facilitador de desenho de `ObjetoPneus` por uma corrente contínua de cliques no editor de circuitos (marcador magenta do ponto pendente, mesmo estilo dos demais objetos ponto a ponto), gerando um objeto independente por segmento consecutivo até o clique direito, com largura/ângulo vindos de cada segmento clicado (correspondendo exatamente à linha de centro do objeto, pra qualquer ângulo) e altura/cores vindas do último template lembrado.

### Modified Capabilities
- `objetos-cenario-circuito`: a criação de `ObjetoPneus` deixa de seguir o mesmo fluxo de clique único usado por Construção — passa a usar o novo facilitador de pares de cliques descrito acima.
- `circuito-info-editor`: novo campo `anguloLargada`, editável no editor de circuitos (spinner 0-360° ao lado do campo de largura da pista), sobrepondo o cálculo automático do ângulo da linha de largada quando definido.

## Impact

- `src/main/java/br/flmane/editor/MainPanelEditor.java`: `iniciarCriacaoObjeto()` (novo ramo pra `ObjetoPneus`), novos campos `desenhandoPneusEmSequencia`/`primeiroCliquePneus`, novo ramo em `clickEditarObjetos` (dentro de `adicionaEventosMouse`), novo `desenhaPreObjetoPneus(Graphics2D)` (preview do ponto pendente em magenta, chamado junto dos demais `desenhaPreObjeto*`), novo spinner `anguloLargadaSpinner` em `gerarTopoNavegacaoEAcoes()`, novo helper `normalizaAngulo(double)`.
- `src/main/java/br/flmane/entidades/Circuito.java`: novo campo `anguloLargada` (`Double`, `@JsonIgnore`), getter/setter, incluído em `copiaParaArquivoMetadados()`.
- `src/main/java/br/flmane/entidades/DesenhoProceduralCircuito.java`: `desenhaLinhaDeLargada(...)` passa a considerar `circuito.getAnguloLargada()` antes de calcular o ângulo automaticamente; novo helper público `calculaAnguloNaturalLargada(Circuito)` (usado pelo editor pra pré-preencher o spinner).
- `src/main/resources/idiomas/mensagens_{pt,en,es,it}.properties`: nova chave `anguloLargada`.
- `src/test/java/br/flmane/editor/MainPanelEditorCriarPneusSequenciaTest.java` (novo), `src/test/java/br/flmane/editor/MainPanelEditorTopoNavegacaoTest.java` (ajustado pra 2 spinners na linha 1), `src/test/java/br/flmane/entidades/DesenhoProceduralCircuitoTest.java` (novos testes de `calculaAnguloNaturalLargada`/override), `src/test/java/br/flmane/recursos/CircuitoMetadadosArquivoTest.java` (novos testes de persistência de `anguloLargada`).
- Nenhum impacto em `ControleCorrida`/motor de jogo além do já existente uso de `desenhaLinhaDeLargada` (que passa a respeitar o override, se algum circuito tiver um); nenhuma mudança em multiplayer/JSON (`anguloLargada` é `@JsonIgnore`, mesmo padrão de `multiplicadorLarguraPista`).
