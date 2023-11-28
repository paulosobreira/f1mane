# Fl-Mane

## Fl-MANE por Paulo Sobreira
- Fl-MANE (MANager & Engineer) é um jogo de gerenciamento de corrida.
- O jogador poderá configurar sua corrida escolhendo número de voltas, tipo de clima e configuração do carro.
- O jogador envia comandos sobre como guiar e faz ajustes telemétricos do carro.
- É possível escolher estratégia de box e muda-lá a qualquer momento na corrida.
- Configuração da corrida com varias opções de como numero de voltas e tipo de clima e etc.
- O jogo gerencia eventos dinamicos como mudança de clima e acidentes entre os corredores.
- Várias temporadas e pistas disponíveis
- No modo campeonato o jogador poderá escolher um piloto/equipe iniciante e desafiar rivais mais promissores  e caso vença poderá assumir o controle da novo piloto/equipe.

## Controles:

- F1 ou A - Giro Baixo Modo Economia de Motor e Combustível
- F2 ou S - Giro Normal Modo Normal de Motor e Combustível
- F3 ou D - Giro Alto Modo Extremo de Motor e Combustível
- F4 - Alterna rapidamente Modo de Pilotagem (Normal/Agressivo)
- F5 ou Z - Piloto em Modo Cauteloso.
- F6 ou X - Piloto Normal.
- F7 ou C - Piloto em Modo Agressivo.
- F8 ou G - Força o piloto a seguir um traçado.
- F12 ou B - Alterna Modo Box.
- Esc - Remove Informações extras da tela.
- W - Ativa o Ers.
- E - Ativa o DRS (Usado somente em retas ou pertes do ciruito consideradas retas) .
- Setas e mouse - Escolhe umm traçado para o piloto seguir.

# Informação Adicional

## Editor de objetos de Pista:

- Pra criar uma pista precisa-se ter uma imagem(jpg) de um circuito na pasta bin\sowbreira\f1mane\recursos 
- No menu "Criar Arquivo Circuito" e escolher esta imagem.
- Adicionar os nós de largada, reta , curva alta ,box , objetos de sobreposição
- Salvar a pista (arquivo tipo .f1mane) na pasta bin\sowbreira\f1mane\recursos

- Controle sobre os objetos ao se clicar no botão "Mover/Parar".
- - Setas : mover pela tela.
- - Ctrl +Setas :  move objeto selecionado pela tela.
- - Shift + Page up e down : altera a  largura do objeto .
- - Ctrl + Page up e down : altera a  altura do objeto.
- - Page up e down : Gira objeto.
- - Ctrl + C :  cria uma copia do objeto.

- Atualizar pistas.properties
- Iniciar o jogo a pista nova deve esta em circuitos.


## Construção Maven e Docker

- mvn clean package
- mvn war:war
- docker build -f f1mane.dockerfile . -t sowbreira/f1mane
- docker push sowbreira/f1mane

## Como testar no Play with Docker

Pode ser executado no [Play with Docker](https://labs.play-with-docker.com/)

>Baixar o aqruiovo do docker compose
```
curl -LfO 'https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml'
```

>Iniciar containers do Mysql,PhpMyAdmin e FlMane
```
docker compose up
```

>Url de acesso:

link_gerado_playwithdocker/**f1mane/html5/index.html**