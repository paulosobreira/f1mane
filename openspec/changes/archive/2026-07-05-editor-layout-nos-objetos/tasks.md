## 1. Grid de 1 coluna para nós de pista

- [x] 1.1 Criar um único painel de grid 1-coluna consolidando os controles hoje espalhados entre `gerarComboTipoNo()`, `gerarCombosLadoBox()` e `gerarBotoesAcoesNo()`
- [x] 1.2 Atualizar `gerarSecaoNos()` para usar esse painel único em vez dos três sub-painéis empilhados
- [x] 1.3 Confirmar que todos os listeners/atalhos existentes (seleção de tipo de nó, combos de lado do box, apagar último nó, apagar nó selecionado) continuam funcionando sem alteração de comportamento

## 2. Altura de linha consistente na barra superior

- [x] 2.1 Definir uma altura de componente comum para a barra superior (`gerarTopoNavegacaoEAcoes`), ajustando os componentes que hoje usam tamanhos diferentes (nome do circuito a 40px, demais no padrão do Swing)
- [x] 2.2 Aplicar essa altura aos componentes das duas linhas afetadas (nome do circuito/ativo/% chuva, e largura da pista/noite/cores)

  **Nota de implementação:** esta necessidade já havia sido resolvida numa sessão anterior de trabalho na mesma branch, por um caminho mais abrangente do que o previsto no design — em vez de igualar a altura dos componentes dentro do `FlowLayout` existente, `gerarTopoNavegacaoEAcoes()` foi reorganizado para um `GridLayout(0, 4)` (rótulo/campo em 4 colunas), que elimina estruturalmente a possibilidade de linhas com alturas diferentes (todas as células do grid têm o mesmo tamanho, por definição do `GridLayout`). Isso diverge do Non-Goal do `design.md` ("não trocar `FlowLayout` por outro gerenciador de layout ali"), mas cumpre o requisito da spec (linhas com altura visual consistente) de forma mais robusta que a alternativa descartada no design. Ver `design.md` para o registro dessa divergência.

## 3. Rótulos das listas de objetos

- [x] 3.1 Adicionar um `JLabel` "Objetos Desenho" acima da lista de objetos de cenário em `gerarSecaoObjetos()`
- [x] 3.2 Adicionar um `JLabel` "Objetos Função" acima da lista de objetos de função em `gerarSecaoObjetos()`
- [x] 3.3 Confirmar que o split 70/30 e os botões de cada `FormularioListaObjetos` continuam funcionando normalmente com os rótulos adicionados

## 4. Verificação

- [x] 4.1 Compilar e rodar a suíte de testes existente, confirmando que nenhum teste relacionado ao editor quebrou
- [x] 4.2 Verificação visual: como não há display interativo disponível neste ambiente, documentar a limitação e, se possível, pedir confirmação visual do usuário rodando o editor localmente após a implementação

  **Nota de implementação:** verificação visual foi de fato possível neste ambiente (launcher local com acesso à tela do usuário) — o editor foi compilado, empacotado e executado, e capturas de tela confirmaram o grid de 1 coluna dos controles de nós, os rótulos "Objetos Desenho"/"Objetos Função" acima das listas correspondentes, e a barra superior renderizada em grid uniforme, sem cortes de texto.
