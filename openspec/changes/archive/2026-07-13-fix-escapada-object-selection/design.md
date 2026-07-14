## Context

`ObjetoEscapada` (`src/main/java/br/f1mane/entidades/ObjetoEscapada.java`) desenha um traçado poligonal (`GeneralPath caminho`) a partir de uma lista de pontos clicados no editor, com entrada e saída ancoradas a nós reais do traçado (`indiceEntrada`/`indiceSaida`).

Esta mudança passou por duas fases:

1. **Fase 1 (correção pontual)**: o relato original era que uma `ObjetoEscapada` já criada não podia ser selecionada nem arrastada clicando no traçado desenhado. A causa raiz: `ObjetoEscapada` não sobrescrevia `obterAreaVisual()` (herdava `obterArea()` de `ObjetoPista`, o retângulo bruto dos vértices, sem folga pela espessura `largura` do traçado desenhado) — só clicar bem em cima de um vértice (dentro da folga fixa de 6px) selecionava o objeto. `ObjetoArquibancada`, com a mesma estrutura de encadeamento de pontos, já tinha essa correção; `ObjetoEscapada` ficou de fora. A correção óbvia era replicar o padrão de `ObjetoArquibancada#obterAreaVisual()`.
2. **Fase 2 (decisão de produto)**: ao validar a fase 1, ficou claro que selecionar/arrastar uma escapada pelo canvas é propenso a erro (arrastar sem querer o objeto inteiro em vez de editar um ponto, por exemplo) e desnecessário — reposicionar uma zona de escapada corretamente significa recriá-la do zero (a entrada/saída precisam revalidar contra o traçado de qualquer forma). A decisão final: uma `ObjetoEscapada` já criada não tem NENHUMA interação por clique no canvas; a única operação suportada é a remoção pela lista de objetos. Isso também tornou o modo "Editar Pontos" (que dependia do menu de contexto, agora inalcançável) código morto, removido junto.

## Goals / Non-Goals

**Goals:**
- Uma `ObjetoEscapada` já criada não responde a clique/arraste/menu de contexto/duplo-clique no canvas do editor.
- A única interação pós-criação suportada é a remoção pela lista de objetos (botão "Remover" ou tecla Delete) — já funcionava, sem depender de hit-testing por clique.
- `obterAreaVisual()` continua cobrindo a espessura visível do traçado (mesmo padrão de `ObjetoArquibancada`), agora só para o destaque de seleção quando a escapada está selecionada pela lista.

**Non-Goals:**
- Não mexer em `ObjetoGuardRails`/`ObjetoArquibancada` (continuam com seleção/arraste/edição de pontos pelo canvas normalmente).
- Não mudar `obterArea()` em si, nem o fluxo de criação (clique-a-clique com validação de entrada/saída), nem o consumo em corrida.

## Decisions

- **Excluir `ObjetoEscapada` no ponto único de hit-testing por clique (`encontraObjetoPistaNaLista`)**, em vez de guardas espalhadas em cada handler (`mousePressed`, menu de contexto, duplo-clique). Como todo fluxo de clique no canvas passa por `encontraObjetoPista`, um único `continue` no laço desarma seleção, arraste do objeto inteiro, menu de contexto e diálogo de propriedades de uma vez. Alternativa considerada: guardas individuais em cada handler — rejeitada por espalhar a mesma regra em múltiplos lugares, mais fácil de esquecer ao adicionar um novo tipo de interação futuramente.
- **Manter `obterAreaVisual()` sobrescrito em `ObjetoEscapada`**, mesmo com hit-testing por clique desativado — ele ainda é usado por `desenhaListaObjetos()`/`desenhaObjetoSelecionadoNoCanvas()` para desenhar o retângulo de destaque quando a escapada está selecionada pela lista de objetos, e esse destaque deve cobrir o traçado realmente desenhado, não só os vértices brutos.
- **Remover por completo o mecanismo de "Editar Pontos" de uma escapada existente** (campos `editandoPontosEscapadaDe`/`indicePontoEscapadaArrastando`/etc., métodos `iniciarEdicaoPontosEscapada`/`tentarIniciarArrastePontoEscapada`/`finalizarArrastePontoEscapada`/`desenhaMarcadoresEdicaoPontosEscapada`, botão no menu de contexto, e o teste dedicado `MainPanelEditorEscapadaArrastarPontoTest`), em vez de deixar o código como estava (inalcançável, mas presente). Decisão confirmada explicitamente com o usuário: manter código morto por "pode ser útil depois" divergiria de manter spec e implementação honestas uma com a outra.

## Risks / Trade-offs

- [Sem forma de corrigir um pequeno erro de posicionamento numa escapada sem recriá-la do zero] → Aceito como trade-off intencional: a decisão de produto é que reposicionar significa recriar, já que entrada/saída precisam revalidar contra o traçado de qualquer forma.
- [Reverter essa decisão no futuro exigiria reimplementar o mecanismo de edição de pontos removido] → Aceitável: o padrão (campos + métodos de arraste de ponto por marcador) já existe para `ObjetoGuardRails`/`ObjetoArquibancada`, então recriar para `ObjetoEscapada` seguiria um precedente conhecido caso necessário.
