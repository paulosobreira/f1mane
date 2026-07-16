## ADDED Requirements

### Requirement: Animação de destaque ao surgir botão de ação
Qualquer botão de ação nas telas de menu que transicione de oculto/inativo para visível/ativo em resposta a uma mudança de estado do jogador SHALL exibir uma animação breve de destaque (ex.: salto ou piscada) no momento em que se torna visível, para sinalizar que uma nova ação ficou disponível. Esta animação é exclusiva das telas de menu (HTML fora do canvas da corrida) — nenhum componente do canvas de corrida (`corrida.html`, `js/ctl.js`) é alterado por esta mudança.

#### Scenario: Seleção de piloto exibe botão de criar jogo
- **WHEN** o jogador seleciona um piloto e o elemento `#criaJogo` passa de oculto (`.hide`) para visível
- **THEN** o botão exibe a animação de destaque no momento em que aparece

#### Scenario: Todas as condições para jogar exibem o botão de entrar no jogo
- **WHEN** temporada, piloto, circuito e token já estão selecionados e `mostrarEntrarJogo()` (`js/jogar.js`) exibe `#btnJogar`
- **THEN** o botão exibe a mesma animação de destaque no momento em que aparece

#### Scenario: HUD da corrida permanece sem animação
- **WHEN** `js/ctl.js` exibe ou oculta `#info`, `#imgJog1` ou `#imgJog2` durante o loop de renderização da corrida
- **THEN** nenhuma animação de destaque é aplicada, pois esses elementos são redesenhados a cada tick (não é uma transição pontual de estado) e fazem parte do canvas de corrida, fora do escopo desta mudança

### Requirement: Reuso dos pontos existentes de show/hide
A animação SHALL ser implementada como uma classe CSS (`@keyframes`) aplicada nos mesmos pontos de código onde hoje ocorre `.show()`, `.removeClass('hide')` ou equivalente, sem introduzir um novo mecanismo de rastreamento de estado dos botões.

#### Scenario: Botão volta a ficar oculto e reaparece
- **WHEN** um botão que recebeu a animação é ocultado novamente (`.hide()`/`.addClass('hide')`) e depois volta a ficar visível pela mesma lógica de estado
- **THEN** a animação de destaque é reaplicada a cada vez que o botão transiciona de oculto para visível
