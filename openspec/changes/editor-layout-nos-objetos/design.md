## Context

`MainPanelEditor.gerarSecaoNos()` monta a seção de nós de pista empilhando três sub-painéis (`GridLayout(3, 1)`): `gerarComboTipoNo()` (label + combo, `JPanel` com `FlowLayout` padrão), `gerarCombosLadoBox()` (dois combos, também `FlowLayout` padrão) e `gerarBotoesAcoesNo()` (dois botões, `GridLayout(2, 1)`). O resultado visual é inconsistente: alguns controles ficam lado a lado dentro do próprio sub-painel, outros empilhados. `gerarTopoNavegacaoEAcoes()` é um único `JPanel` com `FlowLayout`, contendo (nessa ordem) botões de navegação, combo de circuito, botão salvar, nome do circuito (`JTextField` com altura fixada em 40px via `getPreferredSize()` sobrescrito), ativo, % chuva, largura da pista, noite, e os indicadores de cor — como `FlowLayout` quebra em múltiplas linhas visuais conforme a largura da janela, e os componentes têm alturas preferidas diferentes (40px para o campo de nome, altura padrão do Swing para os demais), as linhas resultantes ficam com alturas desiguais. `gerarSecaoObjetos()` já separa duas listas (`formularioListaObjetosDesenho`/`formularioListaObjetosFuncao`, um `JSplitPane` 70/30) mas sem nenhum rótulo indicando qual é a de objetos de desenho e qual é a de função.

## Goals / Non-Goals

**Goals:**
- Consolidar os controles de nós de pista (tipo de nó, lado do box, lado de saída, apagar último nó, apagar nó selecionado) num único grid de 1 coluna — uma linha por controle (ou por par label+controle), substituindo os sub-painéis com `FlowLayout` interno.
- Igualar a altura das linhas visuais da barra superior do editor que hoje ficam desiguais (a linha com nome do circuito/ativo/% chuva e a linha seguinte, com largura da pista/noite/cores).
- Adicionar rótulos "Objetos Desenho" e "Objetos Função" acima das respectivas listas.

**Non-Goals:**
- Não mudar nenhum comportamento funcional (ações dos botões, dados exibidos) — só reorganização visual.
- Não redesenhar a barra superior inteira nem trocar `FlowLayout` por outro gerenciador de layout ali — só normalizar a altura dos componentes que já existem, para que as linhas resultantes do wrap fiquem visualmente parelhas.

## Decisions

### Grid de 1 coluna único para os controles de nós de pista
Substituir os três sub-painéis independentes (`gerarComboTipoNo()`, `gerarCombosLadoBox()`, `gerarBotoesAcoesNo()`) por um único `JPanel` com `GridLayout(N, 1)`, onde cada linha é um componente (ou um par label+controle agrupado num painel interno simples de uma linha) — tipo de nó, lado do box, lado de saída do box, apagar último nó, apagar nó selecionado. Mantém os mesmos componentes/listeners já existentes, só reorganiza o container.

### Igualar altura das linhas via altura de componente comum
Em vez de calcular dinamicamente onde o `FlowLayout` vai quebrar linha (frágil, depende da largura da janela), a correção fixa uma altura de componente comum (baseada na altura hoje usada por `nomePistaText`, 40px, ou um valor um pouco menor se 40px ficar largo demais) e aplica essa altura preferida aos demais componentes da barra superior (labels, checkboxes, spinners, indicadores de cor) que hoje usam o tamanho padrão do Swing — assim, qualquer linha em que o `FlowLayout` quebrar tende a ficar com altura visualmente consistente, já que todo componente individual tem a mesma altura preferida.
- Alternativa descartada — trocar `FlowLayout` por `GridBagLayout`/linhas explícitas: resolveria de forma mais robusta, mas é uma reescrita bem maior da barra superior, fora do escopo de um ajuste de altura de linha.

### Rótulos das listas de objetos via `JLabel` acima de cada uma, dentro do `JSplitPane`
`gerarSecaoObjetos()` embrulha cada lista (`formularioListaObjetosDesenho.getObjetos()`/`formularioListaObjetosFuncao.getObjetos()`) num painel `BorderLayout` com um `JLabel` ("Objetos Desenho"/"Objetos Função") em `NORTH` e o componente de lista original em `CENTER`, antes de passar esses painéis pro `JSplitPane` — preserva o split 70/30 e todos os botões/listeners já existentes dentro de cada `FormularioListaObjetos`.

## Risks / Trade-offs

- [Sem display interativo neste ambiente de desenvolvimento para conferir visualmente o resultado do grid/altura de linha] → Validação via inspeção de código e, se possível, execução do editor localmente pelo usuário após a implementação; documentado como limitação de verificação, não como requisito não cumprido.
- [Altura de componente fixa (ex. 40px) ficar grande demais em telas menores] → Escolher um valor moderado (ex. 28-32px) que ainda comporte o texto/spinners, em vez de manter os 40px atuais do campo de nome, se necessário ajustar durante a implementação.

## Open Questions

- Altura exata de linha a adotar na barra superior fica a critério de quem implementar/ajustar visualmente — não afeta nenhum comportamento funcional nem cenário de spec.
