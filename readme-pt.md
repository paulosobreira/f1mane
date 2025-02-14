# Fl-MANE
Fl-MANE (Formula Legends Manager & Engineer) é um jogo de gerenciamento de corridas de Fórmula 1 desenvolvido por Paulo Sobreira. No jogo, os jogadores assumem o papel de gerente e engenheiro de uma equipe de corrida, tomando decisões estratégicas e técnicas para otimizar o desempenho do carro e do piloto durante as corridas.
## Principais características:
- Configuração da Corrida: Os jogadores podem personalizar diversos aspectos da corrida, como o número de voltas, condições climáticas e configurações do carro.
- Comandos de Pilotagem: Durante a corrida, é possível enviar comandos ao piloto, ajustando o estilo de condução e a potência do motor para equilibrar desempenho e conservação de recursos.
- Estratégia de Pit Stop: O jogo permite que os jogadores definam e modifiquem a estratégia de pit stop a qualquer momento, considerando fatores como desgaste de pneus, consumo de combustível e condições da pista.
- Eventos Dinâmicos: Fl-MANE simula eventos dinâmicos, incluindo mudanças climáticas e acidentes, adicionando um elemento de imprevisibilidade às corridas.
- Múltiplas Temporadas e Pistas: O jogo oferece várias temporadas e pistas, proporcionando uma experiência diversificada e desafiadora.
- Modo Campeonato: No modo campeonato, os jogadores podem começar com uma equipe iniciante e, ao vencer corridas, progredir para equipes mais promissoras, enfrentando rivais cada vez mais desafiadores.


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
