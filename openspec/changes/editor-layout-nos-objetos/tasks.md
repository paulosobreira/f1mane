## 1. Grid de 1 coluna para nós de pista

- [ ] 1.1 Criar um único painel de grid 1-coluna consolidando os controles hoje espalhados entre `gerarComboTipoNo()`, `gerarCombosLadoBox()` e `gerarBotoesAcoesNo()`
- [ ] 1.2 Atualizar `gerarSecaoNos()` para usar esse painel único em vez dos três sub-painéis empilhados
- [ ] 1.3 Confirmar que todos os listeners/atalhos existentes (seleção de tipo de nó, combos de lado do box, apagar último nó, apagar nó selecionado) continuam funcionando sem alteração de comportamento

## 2. Altura de linha consistente na barra superior

- [ ] 2.1 Definir uma altura de componente comum para a barra superior (`gerarTopoNavegacaoEAcoes`), ajustando os componentes que hoje usam tamanhos diferentes (nome do circuito a 40px, demais no padrão do Swing)
- [ ] 2.2 Aplicar essa altura aos componentes das duas linhas afetadas (nome do circuito/ativo/% chuva, e largura da pista/noite/cores)

## 3. Rótulos das listas de objetos

- [ ] 3.1 Adicionar um `JLabel` "Objetos Desenho" acima da lista de objetos de cenário em `gerarSecaoObjetos()`
- [ ] 3.2 Adicionar um `JLabel` "Objetos Função" acima da lista de objetos de função em `gerarSecaoObjetos()`
- [ ] 3.3 Confirmar que o split 70/30 e os botões de cada `FormularioListaObjetos` continuam funcionando normalmente com os rótulos adicionados

## 4. Verificação

- [ ] 4.1 Compilar e rodar a suíte de testes existente, confirmando que nenhum teste relacionado ao editor quebrou
- [ ] 4.2 Verificação visual: como não há display interativo disponível neste ambiente, documentar a limitação e, se possível, pedir confirmação visual do usuário rodando o editor localmente após a implementação
