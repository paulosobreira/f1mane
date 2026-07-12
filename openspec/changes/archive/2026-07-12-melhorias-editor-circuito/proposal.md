## Why

O editor de circuitos hoje carrega automaticamente o primeiro circuito da lista ao iniciar (risco de edição acidental do circuito errado), permite salvar um circuito com uma `ObjetoEscapada` incompleta (sem ponto de saída ancorado) ou sem `distanciaKm` informada (degradando silenciosamente o cálculo de velocidade real em corrida), e desenha o traçado do box sem nenhum tratamento de borda — diferente do traçado da pista, que já tem borda branca (`desenhaTintaPistaZebra`). Essas quatro lacunas produzem circuitos salvos em estado inconsistente ou visualmente incompleto sem nenhum aviso ao usuário.

## What Changes

- Editor de circuitos SHALL iniciar sem nenhum circuito carregado; o combo box de seleção SHALL refletir esse estado "nenhum circuito carregado" em vez de pré-selecionar o primeiro item da lista.
- Salvar um circuito SHALL ser bloqueado (com alerta, seguindo o padrão `alertaPontoEscapadaInvalido()`/`MainPanelEditorTestDouble`) quando existir qualquer `ObjetoEscapada` sem ponto de saída ancorado (`indiceSaida == -1`).
- Salvar um circuito SHALL ser bloqueado (com alerta) quando `distanciaKm` não tiver sido informada (campo vazio ou zero).
- Traçado do box (`desenhaPistaBox`) SHALL ganhar uma borda branca ao longo do seu desenho, exceto nos trechos onde esse traçado intersecciona geometricamente com o traçado da pista — nesses trechos a linha branca SHALL NOT ser desenhada.
- Listas de nós da pista e de nós do box (`gerarListsNosPistaBox`) SHALL passar a ficar dentro de um `JSplitPane` vertical redimensionável pelo usuário, substituindo o `GridLayout(2, 1)` de altura fixa atual — mesmo padrão já usado entre as listas de objetos de desenho e de função (`gerarSecaoObjetos`).

## Capabilities

### New Capabilities
- `editor-inicializacao-sem-circuito`: comportamento de inicialização do editor sem circuito pré-carregado e estado correspondente do combo box de seleção.
- `box-borda-branca-intersecao`: desenho de borda branca ao longo do traçado do box, suprimida nos trechos que interseccionam o traçado da pista.
- `nos-pista-box-splitpane`: listas de nós da pista e do box dentro de um `JSplitPane` vertical redimensionável, em vez de altura fixa.

### Modified Capabilities
- `objeto-escapada-tracado`: adiciona um requisito de bloqueio de salvamento do circuito quando existir `ObjetoEscapada` incompleta (sem `indiceSaida` ancorado).
- `circuito-info-editor`: adiciona um requisito de bloqueio de salvamento do circuito quando `distanciaKm` não tiver sido informada.

## Impact

- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: `iniciarComNavegacao()` (remove auto-load do índice 0), `repopularComboCircuitos()`/`atualizarBotoesNavegacao()` (estado "nenhum circuito" do combo), `salvarPista()` (novas validações de escapada incompleta e `distanciaKm` antes de `vetorizarCircuito()`/gravação em arquivo), novo(s) método(s) `protected` de alerta seguindo o padrão de `alertaPontoEscapadaInvalido()`.
- `src/test/java/br/f1mane/editor/MainPanelEditorTestDouble.java`: overrides para os novos alertas de validação de salvamento.
- `src/main/java/br/f1mane/visao/PainelCircuito.java`: `desenhaPistaBox()` (ou novo método de "tinta" do box análogo a `desenhaTintaPistaZebra()`), com lógica de detecção de interseção geométrica entre o traçado do box e o traçado da pista para suprimir a borda branca nos trechos sobrepostos.
- `src/main/java/br/f1mane/editor/MainPanelEditor.java`: `gerarListsNosPistaBox()` (troca `GridLayout(2, 1)` + `JScrollPane`s de altura fixa por um `JSplitPane` vertical, reaproveitando o padrão de `gerarSecaoObjetos()`/`splitListas`).
- Nenhum impacto em persistência de arquivo (XML) ou em `LetsRace`/multiplayer — mudanças restritas ao editor local e à sua camada de renderização.
