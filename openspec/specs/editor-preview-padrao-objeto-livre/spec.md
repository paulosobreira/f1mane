# Spec: editor-preview-padrao-objeto-livre

## Purpose

Defines the editor's "Padrões" checkbox — a session-only preference (never persisted to a circuit file) that lets the user trade full pattern detail for a lightweight example while editing, since redrawing every mark of a non-solid `ObjetoLivre` pattern every frame can be costly for dense/large objects. Also defines the magenta outline shown while the reduced example mode is active, so the object's real extent stays visible even when its content is reduced to a single example mark.

## Requirements

### Requirement: Editor oferece um checkbox "Padrões" que alterna entre preenchimento completo e modo de exemplo reduzido
O editor de circuitos SHALL exibir, na mesma linha/painel do checkbox de Testar Escapada (controles de teste/visualização não persistidos por circuito), um checkbox "Padrões" cujo estado NÃO é salvo no circuito (não é lido nem gravado em nenhum arquivo XML de circuito, nem persiste entre reaberturas do editor). Quando marcado (estado padrão ao abrir o editor), todo `ObjetoLivre` cujo `tipo` seja diferente de `POLIGONO_SIMPLES` SHALL ser desenhado com o preenchimento de padrão completo (comportamento existente), sem nenhuma borda extra. Quando desmarcado, o comportamento depende do tipo:

- **`VEGETACAO_DENSA`/`VEGETACAO_SIMPLES`**: desenha uma única marca de exemplo (uma árvore ou uma relva), centralizada na área da forma, ampliada para `FATOR_TAMANHO_EXEMPLO_PREVIEW` (3x) o tamanho normal de uma árvore de `VEGETACAO_DENSA` — não o tamanho normal do próprio tipo — para facilitar examinar visualmente o modelo sorteado.
- **`AGUA`, `BRITA`, `LISTRADO`, `XADREZ`**: nenhuma marca de exemplo é desenhada (o preenchimento de fundo sólido desses tipos continua normalmente); só a borda (ver requirement seguinte) marca a área.

#### Scenario: Checkbox marcado mantém o preenchimento completo do padrão, sem borda
- **WHEN** o checkbox "Padrões" está marcado (estado padrão)
- **THEN** cada `ObjetoLivre` não sólido é desenhado com o preenchimento de padrão completo, como antes desta mudança, sem nenhuma borda extra desenhada por cima

#### Scenario: Checkbox desmarcado amplia o exemplo de vegetação densa/simples
- **WHEN** o usuário desmarca o checkbox "Padrões" e o objeto é `VEGETACAO_DENSA` ou `VEGETACAO_SIMPLES`
- **THEN** só uma marca (árvore ou relva) é desenhada, centralizada na forma, num tamanho 3x maior que o tamanho normal de uma árvore de vegetação densa

#### Scenario: Checkbox desmarcado não desenha exemplo para os demais tipos
- **WHEN** o usuário desmarca o checkbox "Padrões" e o objeto é `AGUA`, `BRITA`, `LISTRADO` ou `XADREZ`
- **THEN** nenhuma marca de padrão é desenhada dentro da forma (o fundo sólido de `corPimaria` continua aparecendo normalmente)

#### Scenario: Preferência do checkbox não é persistida por circuito
- **WHEN** o usuário desmarca ou marca o checkbox "Padrões" e depois troca de circuito, salva o circuito atual, ou recarrega o editor
- **THEN** nenhum arquivo de circuito é alterado por causa desse checkbox, e seu estado é o mesmo (marcado, preenchimento completo) na próxima vez que o editor for aberto, independente do que foi selecionado antes

#### Scenario: Marca central cortada pela borda não é desenhada
- **WHEN** o checkbox "Padrões" está desmarcado e a marca única centralizada (ampliada) de um `ObjetoLivre` do tipo `VEGETACAO_DENSA` não caberia inteira dentro da área da forma
- **THEN** nenhuma árvore é desenhada nesse objeto nesse modo, em vez de desenhar uma árvore cortada

### Requirement: Borda magenta marca a área do objeto livre quando o checkbox "Padrões" está desmarcado
Para todo `ObjetoLivre` cujo `tipo` seja diferente de `POLIGONO_SIMPLES`, quando o checkbox "Padrões" estiver desmarcado, `ObjetoLivre.desenha()` SHALL desenhar um contorno na cor magenta (fixa, não relacionada a `corPimaria`/`corSecundaria`) ao longo da silhueta da forma, por cima de qualquer conteúdo desenhado dentro dela — necessário porque, no modo de exemplo reduzido, a extensão real do objeto deixa de ficar óbvia (sobretudo em `VEGETACAO_DENSA`/`VEGETACAO_SIMPLES`, que nem preenchem fundo). A borda SHALL desaparecer quando o checkbox está marcado.

#### Scenario: Borda magenta aparece só no modo de exemplo reduzido
- **WHEN** o checkbox "Padrões" está desmarcado e um `ObjetoLivre` não sólido é desenhado
- **THEN** um contorno magenta é desenhado ao longo do perímetro da forma do objeto, visível independente do tipo e de haver ou não uma marca de exemplo dentro dela

#### Scenario: Borda magenta some com o preenchimento completo
- **WHEN** o checkbox "Padrões" está marcado
- **THEN** nenhum contorno magenta é desenhado, independente do tipo do `ObjetoLivre`
