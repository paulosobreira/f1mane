## ADDED Requirements

### Requirement: Redesenho dos objetos atrasado após pan por seta
Ao mover a viewport do editor de circuitos pelas setas do teclado (sem modificadores, via `MainPanelEditor.esquerda()/direita()/cima()/baixo()`), o editor SHALL suspender o desenho da camada de objetos do circuito enquanto um passo de pan tiver ocorrido nos últimos 0,5 segundos, retomando o desenho dessa camada somente 0,5 segundos após o último passo de movimento. O restante do frame — cor de fundo, imagem de referência, traçado do circuito e os overlays fixos ao canto da viewport (painéis de informação/controles, posicionados a partir dos limites atuais da viewport) — SHALL continuar sendo redesenhado normalmente a cada passo de pan, sem esse atraso.

#### Scenario: Um único passo de seta suspende o desenho dos objetos
- **WHEN** o usuário pressiona uma seta de navegação uma única vez, movendo a viewport
- **THEN** a camada de objetos do circuito não é desenhada nesse passo, e volta a ser desenhada somente 0,5 segundos depois, caso nenhum outro passo de pan ocorra nesse intervalo

#### Scenario: Novos passos de pan reiniciam a contagem de 0,5 segundos
- **WHEN** o usuário segura ou pressiona repetidamente as setas de navegação, gerando vários passos de pan em sequência com menos de 0,5 segundos entre eles
- **THEN** a camada de objetos permanece sem ser desenhada durante essa sequência, voltando a ser desenhada uma única vez, 0,5 segundos após o último passo de pan

#### Scenario: Direções diferentes de pan compartilham a mesma contagem
- **WHEN** o usuário alterna entre setas de direções diferentes (por exemplo, direita e depois cima) sem pausa maior que 0,5 segundos entre os passos
- **THEN** a suspensão do desenho dos objetos continua como uma única contagem contínua, sendo desfeita apenas 0,5 segundos após o último passo de pan em qualquer direção

#### Scenario: Fundo, traçado e overlays fixos à viewport são redesenhados a cada passo, sem atraso
- **WHEN** o usuário move a tela pelas setas, com a camada de objetos ainda suspensa pelo atraso
- **THEN** a cor de fundo, a imagem de referência, o traçado do circuito e os overlays fixos ao canto da viewport são redesenhados corretamente a cada passo, refletindo a nova posição da viewport, sem esperar os 0,5 segundos

#### Scenario: Overlays fixos à viewport não deixam artefato visual durante o pan
- **WHEN** o usuário move a tela pelas setas em sequência (vários passos, com a camada de objetos suspensa)
- **THEN** os overlays fixos ao canto da viewport (painéis de informação/controles) permanecem visualmente presos ao canto correto da área visível a cada passo, sem deixar cópias desatualizadas ou deslocadas sobre o fundo do circuito

#### Scenario: Outras causas de redesenho não reiniciam a contagem de pan
- **WHEN** ocorre um redesenho motivado por outra ação do editor que não seja o pan por seta (por exemplo, arrastar um objeto com o mouse, selecionar um item na lista de objetos, ou criar/editar um objeto)
- **THEN** esse redesenho não reinicia nem altera a contagem de 0,5 segundos associada ao pan por seta
