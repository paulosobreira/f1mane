# Fl-Mane

## Fl-MANE por Paulo Sobreira
- Fl-MANE (Formula legends MANager & Engineer) é um jogo de gerenciamento de corrida.
- Configure sua corrida escolhendo número de voltas, tipo de clima e configuração do carro.
- Envie comandos sobre como guiar e fazer ajustes de potência do carro.
- É possível escolher estratégia de box e mudá-la a qualquer momento na corrida.
- Configuração da corrida com várias opções de como número de voltas e tipo de clima e etc.
- O jogo gera eventos dinâmicos como mudança de clima e acidentes entre os corredores.
- Várias temporadas e pistas disponíveis
- No modo campeonato, poderá escolher um piloto/equipe iniciante e desafiar rivais mais promissores e caso vença poderá assumir o controle do novo piloto/equipe.

## Controles:

- A - Giro Baixo (Menos performance geral assim como menos consumo de Motor e Combustível)
- S - Giro Normal
- D - Giro Alto (Mais performance geral assim como mais consumo de Motor e Combustível)
- Z - Piloto em Modo Cauteloso(Menos performance nas curvas e reduz o stress do piloto).
- X - Piloto Normal.
- C - Piloto em Modo Agressivo(Mais performance nas curvas e aumenta o stress do piloto).
- B - Liga desliga Modo Box.
- Esc - Pausa o jogo.
- Seta Baixo - Ativa o Ers.
- Seta Cima - Ativa o DRS (Usado somente em retas ou pertes do ciruito consideradas retas) .
- Setas e mouse - Escolhe umm traçado para o piloto seguir.

## Requerimentos

-Para executar o jogo é necessário Java 11
-No Windows utilize Fl-Mane.bat, no Linux utilize Fl-Mane.sh

# Informação técnica

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
