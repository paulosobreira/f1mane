# Fl-Mane

## Fl-MANE por Paulo Sobreira
- Fl-MANE (Formula legends MANager & Engineer) é um jogo de gerenciamento de corrida.
- Configure sua corrida escolhendo número de voltas, tipo de clima e configuração do carro.
- Envie comandos sobre como guiar e faz ajustes telemétricos do carro.
- É possível escolher estratégia de box e muda-lá a qualquer momento na corrida.
- Configuração da corrida com varias opções de como numero de voltas e tipo de clima e etc.
- O jogo gerencia eventos dinamicos como mudança de clima e acidentes entre os corredores.
- Várias temporadas e pistas disponíveis
- No modo campeonato, poderá escolher um piloto/equipe iniciante e desafiar rivais mais promissores e caso vença poderá assumir o controle da novo piloto/equipe.

## Controles:

- A - Giro Baixo Modo Economia de Motor e Combustível
- S - Giro Normal Modo Normal de Motor e Combustível
- D - Giro Alto Modo Extremo de Motor e Combustível
- Z - Piloto em Modo Cauteloso.
- X - Piloto Normal.
- C - Piloto em Modo Agressivo.
- B - Alterna Modo Box.
- Esc - Remove Informações extras da tela.
- Seta Baixo - Ativa o Ers.
- Seta Cima - Ativa o DRS (Usado somente em retas ou pertes do ciruito consideradas retas) .
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
- docker build -f flmane.dockerfile . -t sowbreira/flmane
- docker push sowbreira/flmane

## Como testar no Play with Docker

Pode ser executado no [Play with Docker](https://labs.play-with-docker.com/)

>Baixar o aqruivo do docker compose
```
curl -LfO 'https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml'
```

>Iniciar containers do Mysql,PhpMyAdmin e FlMane
```
docker compose up
```

>Url de acesso:

link_gerado_playwithdocker/**flmane/html5/index.html**