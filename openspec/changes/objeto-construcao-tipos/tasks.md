## 1. Enums

- [ ] 1.1 Criar `br.f1mane.entidades.TipoObjetoConstrucao` (enum: `QUADRADO`, `REDONDO`, `CAMINHAO`, `BARCO`, `PREDIO`)
- [ ] 1.2 Criar `br.f1mane.entidades.DirecaoEmpilhamento` (enum com 8 valores: cima, baixo, esquerda, direita, cima-esquerda, cima-direita, baixo-esquerda, baixo-direita), com javadoc explicando o deslocamento `(dx, dy)` de cada valor

## 2. ObjetoConstrucao — propriedades e desenho por tipo

- [ ] 2.1 Adicionar campos `tipo` (default `QUADRADO`), `afunilamento` (int/double, default > 0), `quantidadeAndares` (int, default 1, mínimo 1) e `direcaoEmpilhamento` (default não-nulo) a `ObjetoConstrucao`, com getters/setters (setters com validação de mínimo, mesmo padrão de `ObjetoLivre.setTipo`/`ObjetoGuardRails`)
- [ ] 2.2 Extrair o desenho atual (retângulos arredondados aninhados) para um método privado reutilizável (base do `QUADRADO` e do `PREDIO`)
- [ ] 2.3 Implementar desenho do tipo `REDONDO` (elipses aninhadas, mesma composição externa/interna do `QUADRADO`)
- [ ] 2.4 Implementar desenho do tipo `CAMINHAO` (duas formas lado a lado, não aninhadas, usando `corPimaria`/`corSecundaria`)
- [ ] 2.5 Implementar desenho do tipo `BARCO` (retângulo com extremidade afunilada via `GeneralPath`, comprimento da seção afunilada proporcional a `afunilamento`)
- [ ] 2.6 Implementar desenho do tipo `PREDIO` (repete o método do 2.2 `quantidadeAndares` vezes, deslocando cada repetição segundo `direcaoEmpilhamento`)
- [ ] 2.7 Atualizar `desenha()` para despachar por `tipo`
- [ ] 2.8 Atualizar `obterArea()` para retornar a união das áreas de todos os andares quando `tipo=PREDIO`, e o bounding box de cada forma para os demais tipos, seguro antes do primeiro `desenha()`

## 3. Editor — formulário completo

- [ ] 3.1 Adicionar `JComboBox<TipoObjetoConstrucao>` a `FormularioObjetos`, seguindo o padrão de `tipoObjetoLivreCombo`
- [ ] 3.2 Adicionar spinner de `afunilamento`, exibido apenas quando `tipo=BARCO`
- [ ] 3.3 Adicionar spinner de `quantidadeAndares` e `JComboBox<DirecaoEmpilhamento>`, exibidos apenas quando `tipo=PREDIO`
- [ ] 3.4 Atualizar `montarPainelParaTipo`, `carregarCampos` e `formularioObjetoPista` para `ObjetoConstrucao`, incluindo os novos campos condicionais

## 4. Editor — menu de contexto de ajuste rápido

- [ ] 4.1 Atualizar `MainPanelEditor.criaPainelAjusteRapido` para exibir os mesmos campos condicionais (tipo, afunilamento, quantidade/direção de empilhamento) para `ObjetoConstrucao`

## 5. Compatibilidade e testes

- [ ] 5.1 Confirmar que `ObjetoConstrucao` sem `tipo`/`afunilamento`/`quantidadeAndares`/`direcaoEmpilhamento` gravados no XML carrega com os valores padrão (`QUADRADO`, sem alterar aparência)
- [ ] 5.2 Testes unitários para `desenha()`/`obterArea()` de cada tipo (sem lançar exceção, área não vazia), incluindo `PREDIO` com `quantidadeAndares` > 1
- [ ] 5.3 Testar manualmente no editor de circuitos: criar um `ObjetoConstrucao`, alternar entre os 5 tipos, ajustar largura/altura/afunilamento/quantidadeAndares/direcaoEmpilhamento pelo formulário e pelo menu de contexto, e confirmar que o desenho reflete cada mudança
