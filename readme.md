# F1Mane
 
F1-MANE (MANager & Engineer) é um jogo de gerenciamento de corrida. O jogo consiste em comandar o piloto e fazer ajustes telemétricos do carro. O jogador poderá configurar sua corrida escolhendo número de voltas, tipo de clima e configuração do carro. Em um campeonato o jogador poderá escolher um piloto/equipe iniciante e desafiar rivais mais promissores  e caso vença poderá assumir o controle da novo piloto/equipe.

## F1-MANE por Paulo Sobreira
- Fl-MANE (MANager & Engineer) É um jogo de gerenciamento e engenharia de corrida.
- O jogador envia comandos sobre como guiar e faz ajustes telemétricos do carro.
- É possível escolher estratégia de box e muda-lá a qualquer momento na corrida.
- Configuração da corrida com varias opções de como numero de voltas e tipo de clima e etc.
- O jogo gerencia eventos dinamicos como mudan�a de clima e acidentes entre os corredores.
- Várias temporadas e pistas reais disponíveis

-Contato Paulo Sobreira : sowbreira@gmail.com.

## Controles:

F1 ou A - Giro Baixo Modo Economia de Motor e Combustível
F2 ou S - Giro Normal Modo Normal de Motor e Combustível
F3 ou D - Giro Alto Modo Extremo de Motor e Combustível
F4 - Alterna rapidamente Modo de Pilotagem (Normal/Agressivo)
F5 ou Z - Piloto em Modo Cauteloso.
F6 ou X - Piloto Normal.
F7 ou C - Piloto em Modo Agressivo.
F8 ou G - Força o piloto a seguir um traçado.
F12 ou B - Alterna Modo Box.
Esc - Remove Informações extras da tela.
W - Ativa o Ers.
E - Ativa o DRS (Usado somente em retas ou pertes do ciruito consideradas retas) .
Setas e mouse - Escolhe umm traçado para o piloto seguir.

# Informação Adicional

## Editor de objetos de Pista:

Pra criar uma pista precisa-se ter uma imagem(jpg) de um circuito na
pasta bin\sowbreira\f1mane\recursos então deve-se ir no menu "Criar Arquivo
Circuito" e escolher esta imagem.
-Adicionar os nós de largada, reta , curva alta ,box , objetos de sobreposição
e depois salvar a pista (arquivo tipo .f1mane) na pasta bin\sowbreira\f1mane\recursos
Controle sobre os objetos ao se clicar no botão "Mover/Parar".
Setas : mover pela tela.
Ctrl +Setas :  move objeto selecionado pela tela.
Shift + Page up e down : altera a  largura do objeto .
Ctrl + Page up e down : altera a  altura do objeto.
Page up e down : Gira objeto.
Ctrl + C :  cria uma copia do objeto.

-Atualizar pistas.properties

-Iniciar o jogo a pista nova deve esta em circuitos.


## Construção

- mvn clean package
- mvn war:war
- docker build -f f1mane.dockerfile . -t sowbreira/f1mane
- docker push sowbreira/f1mane
