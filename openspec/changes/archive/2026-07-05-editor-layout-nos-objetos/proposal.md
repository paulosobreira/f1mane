## Why

O editor de circuitos acumulou, ao longo de várias mudanças, painéis com layouts inconsistentes: a seção de nós de pista (`MainPanelEditor.gerarSecaoNos()`) mistura sub-painéis empilhados (`GridLayout`) com sub-painéis de fluxo horizontal (`FlowLayout` padrão do `JPanel`), a barra superior (`gerarTopoNavegacaoEAcoes()`) é um único `FlowLayout` que quebra em múltiplas linhas visuais de altura desigual (a linha com nome do circuito/ativo/chuva usa um `JTextField` com altura fixada em 40px, enquanto a linha seguinte só tem labels/spinners no tamanho padrão do Swing), e a seção de objetos (`gerarSecaoObjetos()`) mostra as duas listas (objetos de desenho, objetos de função) sem nenhum rótulo indicando qual é qual.

## What Changes

- Os controles da seção de nós de pista (tipo de nó, lado do box/lado de saída do box, "Apagar Ultimo NO", "Apaga Nó na lista Selecionada") passam a ficar num único grid de 1 coluna (uma linha por controle), em vez de sub-painéis separados que misturam `GridLayout` empilhado com `FlowLayout` lado a lado.
- As linhas da barra superior do editor (`gerarTopoNavegacaoEAcoes()`) que hoje ficam com alturas visualmente diferentes — a linha com nome do circuito/ativo/% chuva e a linha abaixo dela (largura da pista/noite/cores) — passam a ter a mesma altura.
- A seção de objetos (`gerarSecaoObjetos()`) ganha os rótulos "Objetos Desenho" e "Objetos Função" acima de cada uma das duas listas, deixando claro qual lista é qual.

## Capabilities

### Modified Capabilities
- `dev-editor-tools`: layout da seção de nós de pista consolidado num grid de 1 coluna, alturas de linha da barra superior equalizadas, e rótulos identificando as listas de objetos de desenho/função.

## Impact

- `br.f1mane.editor.MainPanelEditor` — `gerarSecaoNos()`/`gerarComboTipoNo()`/`gerarCombosLadoBox()`/`gerarBotoesAcoesNo()` reorganizados num único painel de grid 1-coluna; `gerarTopoNavegacaoEAcoes()` ganha alturas de componente normalizadas entre as linhas que hoje ficam desiguais; `gerarSecaoObjetos()` ganha dois `JLabel` de cabeçalho.
- Nenhuma mudança de comportamento/dados — puramente reorganização visual do editor Swing.
