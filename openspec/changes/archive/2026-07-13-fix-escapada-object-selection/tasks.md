## 1. Fase 1 — correção pontual (área clicável)

- [x] 1.1 Sobrescrever `obterAreaVisual()` em `ObjetoEscapada` (`src/main/java/br/f1mane/entidades/ObjetoEscapada.java`), expandindo o retângulo de `obterArea()` pela metade de `largura`, no mesmo padrão de `ObjetoArquibancada#obterAreaVisual()`.
- [x] 1.2 Documentar com um comentário curto por que a sobreposição é necessária.
- [x] 1.3 (Regressão encontrada ao validar 1.1) Em `MainPanelEditor#mousePressed`, impedir que um clique que erra o marcador durante o modo "Editar Pontos" caísse no fallback genérico de seleção/arraste do objeto inteiro.
- [x] 1.4 (Feedback do usuário ao validar 1.1) `ObjetoEscapada` não precisa do diálogo de propriedades por duplo-clique — extrair `MainPanelEditor#temPropriedadesEditaveisPorDialogo(ObjetoPista)` e usá-lo em `editaObjetoPista` (canvas) e no `mouseClicked` da lista de objetos em `FormularioListaObjetos`.

## 2. Fase 2 — decisão de produto: nenhuma interação por clique no canvas

- [x] 2.1 Em `encontraObjetoPistaNaLista` (`MainPanelEditor.java`), excluir toda `ObjetoEscapada` do hit-testing por clique no canvas — desarma seleção, arraste do objeto inteiro, menu de contexto e diálogo de propriedades de uma vez, já que todos passam por esse ponto único.
- [x] 2.2 Remover o guard específico de 1.3 em `mousePressed` (agora redundante/inalcançável, já coberto pela exclusão de 2.1).
- [x] 2.3 Remover por completo o mecanismo de "Editar Pontos" de uma escapada já criada: campos (`editandoPontosEscapadaDe`, `indicePontoEscapadaArrastando`, `arrastouPontoEscapada`, `pontoEscapadaAnteriorAoArraste`), métodos (`iniciarEdicaoPontosEscapada`, `encerrarEdicaoPontosEscapada`, `calculaOffsetTelaEscapada`, `tentarIniciarArrastePontoEscapada`, `finalizarArrastePontoEscapada`, `desenhaMarcadoresEdicaoPontosEscapada`, `desenhaMarcadorAmareloPreto`), o botão "Editar Pontos" no menu de contexto rápido (`criaPainelAjusteRapido`), e a chamada no loop de desenho.
- [x] 2.4 Remover o arquivo de teste dedicado `MainPanelEditorEscapadaArrastarPontoTest.java` (testava o mecanismo removido em 2.3).
- [x] 2.5 Reescrever `MainPanelEditorEscapadaSelecaoTest.java` para cobrir o comportamento final: `encontraObjetoPista` nunca encontra uma escapada (vértice ou dentro da espessura), `mousePressed` não seleciona/arma arraste, clique direito não abre menu de contexto, e o predicado `temPropriedadesEditaveisPorDialogo` continua correto.
- [x] 2.6 Rodar `mvn compile` e `mvn test` (suíte completa) para confirmar que a remoção não quebrou nada — nenhuma outra referência aos identificadores removidos deveria restar no projeto.

## 3. Sincronização de spec

- [x] 3.1 Atualizar a delta spec de `objeto-escapada-tracado` para refletir o estado final: `ADDED` (área visual para destaque de seleção pela lista; nenhuma interação por clique no canvas), `MODIFIED` (índice de traçado só gravado na criação; Teste Pista só reconhece criação, não mais edição), `REMOVED` (edição de pontos de uma escapada existente, com Reason/Migration).
- [x] 3.2 Aplicar a mesma sincronização em `openspec/specs/objeto-escapada-tracado/spec.md` (main spec), incluindo a remoção do requisito de edição de pontos e o ajuste dos requisitos que mencionavam edição de forma incidental (índices de traçado, Teste Pista).
