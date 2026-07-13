## ADDED Requirements

### Requirement: Tecla Delete remove o objeto selecionado na lista única
Com o foco na lista única de objetos (`FormularioListaObjetos`) e um item selecionado, pressionar a tecla Delete SHALL remover esse objeto, com o mesmo efeito do botão "Remover" já existente: o objeto é removido da coleção do `Circuito` correspondente ao seu tipo (`circuito.getObjetos()` ou `circuito.getObjetosCenario()`) e deixa de aparecer na lista e no desenho do circuito.

#### Scenario: Delete com item selecionado remove o objeto
- **WHEN** o usuário seleciona um objeto na lista única de objetos e pressiona a tecla Delete com o foco na lista
- **THEN** o objeto selecionado é removido da coleção correspondente do `Circuito`, desaparece da lista única e deixa de ser desenhado no canvas

#### Scenario: Delete sem seleção não tem efeito
- **WHEN** o usuário pressiona a tecla Delete com o foco na lista única de objetos e nenhum item selecionado
- **THEN** nenhum objeto é removido e a lista permanece inalterada

#### Scenario: Delete na lista funciona mesmo sem seleção prévia no canvas
- **WHEN** o usuário seleciona um objeto exclusivamente através da lista única (sem clicar nesse objeto no canvas) e pressiona Delete com o foco na lista
- **THEN** o objeto é removido normalmente, independente do valor atual do campo de seleção usado pelo clique no canvas
