## ADDED Requirements

### Requirement: Editor não desenha objetos fora do viewport visível
No laço principal de desenho por nível (`desenhaObjetosNivel`), o editor de circuitos SHALL pular a chamada de `desenha()` de um `ObjetoPista` cujo retângulo de teste não intersecte o retângulo retornado por `limitesViewPort()`. O retângulo de teste SHALL ser `obterArea()` quando `angulo == 0`, ou um retângulo centrado no centro de `obterArea()` com metade do lado igual a `Math.hypot(largura, altura) / 2` quando `angulo != 0`.

#### Scenario: Objeto totalmente fora da área visível não é desenhado
- **WHEN** o retângulo de teste de um `ObjetoPista` está totalmente fora do retângulo de `limitesViewPort()`
- **THEN** `desenha()` não é chamado para esse objeto nesse frame

#### Scenario: Objeto que intersecta a borda do viewport continua sendo desenhado
- **WHEN** o retângulo de teste de um `ObjetoPista` intersecta parcialmente o retângulo de `limitesViewPort()` (por exemplo, metade do objeto visível e metade fora)
- **THEN** `desenha()` é chamado normalmente para esse objeto

#### Scenario: Objeto rotacionado usa o raio circunscrito para decidir visibilidade
- **WHEN** um `ObjetoPista` tem `angulo` diferente de `0`
- **THEN** o teste de visibilidade usa o retângulo expandido ao raio circunscrito de `largura`/`altura` em vez de `obterArea()` bruta, evitando descartar erroneamente um objeto cuja forma rotacionada ainda entra na tela

### Requirement: Corte por viewport não altera o resultado visual de objetos desenhados
O corte por viewport SHALL preservar a ordem de níveis de desenho (`niveisDesenhoOrdenados`) e a aparência de qualquer objeto efetivamente desenhado — a otimização decide apenas SE `desenha()` é chamado, nunca COMO o objeto é desenhado. Overlays de criação/edição em andamento (objeto sendo posicionado, marcadores de edição de pontos, contorno de seleção) SHALL continuar sendo desenhados fora desse corte, como hoje.

#### Scenario: Circuito totalmente visível desenha exatamente os mesmos objetos que antes da otimização
- **WHEN** todos os objetos de um circuito estão dentro do viewport atual
- **THEN** o conjunto e a ordem de objetos desenhados são idênticos ao comportamento anterior à introdução do corte por viewport

#### Scenario: Objeto em criação continua visível independente do corte
- **WHEN** o usuário está no meio da criação de um objeto multi-ponto (por exemplo `ObjetoGuardRails` ou `ObjetoEscapada`) cujo preview ainda não foi adicionado às listas de objetos do circuito
- **THEN** o preview em andamento continua sendo desenhado normalmente, independentemente de estar dentro ou fora do retângulo de `limitesViewPort()`
