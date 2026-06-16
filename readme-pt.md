# Fl-MANE

**Fl-MANE (Formula livre Manager & Engineer)** é um simulador de gerenciamento e estratégia de Fórmula 1 desenvolvido por **Paulo Sobreira**.

No jogo, você assume os papéis de **Chefe de Equipe** e **Engenheiro de Corrida**, tomando decisões estratégicas e técnicas para maximizar o desempenho do carro e do piloto durante as corridas.

## Principais Funcionalidades

### 🏎️ Configuração de Corridas

Personalize diversos aspectos da corrida:

* Escolha do circuito
* Número de voltas
* Condições climáticas
* Acertos do carro

### 🎮 Comandos para o Piloto

Durante a corrida, envie instruções em tempo real:

* Ajuste do regime do motor
* Nível de agressividade do piloto
* Gerenciamento de combustível
* Conservação do equipamento

### 🔧 Estratégia de Pit Stop

Defina e altere estratégias a qualquer momento:

* Escolha dos pneus
* Momento ideal para parada
* Adaptação às condições da pista
* Reação a eventos inesperados

### 🌦️ Eventos Dinâmicos

As corridas são influenciadas por diversos fatores:

* Mudanças climáticas
* Acidentes
* Quebras mecânicas
* Situações de corrida imprevisíveis

### 🏆 Modo Campeonato

Construa sua carreira ao longo das temporadas:

* Comece em equipes menores
* Conquiste melhores contratos
* Enfrente adversários cada vez mais competitivos
* Dispute títulos mundiais

### 🌍 Diversas Temporadas e Circuitos

Corra em diferentes épocas da Fórmula 1, com carros, equipes e desafios variados.

---

# Controles

| Tecla         | Função                                                 |
| ------------- | ------------------------------------------------------ |
| A             | Giro baixo do motor (menor desempenho e menor consumo) |
| S             | Giro normal do motor                                   |
| D             | Giro alto do motor (maior desempenho e maior consumo)  |
| Z             | Piloto em modo cauteloso                               |
| X             | Piloto em modo normal                                  |
| C             | Piloto em modo agressivo                               |
| B             | Ativar/desativar modo Pit Stop                         |
| ↓             | Ativar ERS                                             |
| ↑             | Ativar DRS (apenas em retas)                           |
| Setas / Mouse | Escolher trajetória do piloto                          |
| ESC           | Pausar o jogo                                          |

---

# Requisitos

## Versão Desktop

* Java 21 ou superior
* Windows, Linux ou macOS

### Windows

```bash
Fl-Mane.bat
```

### Linux

```bash
./Fl-Mane.sh
```

---

# Compilação

## Maven

Gerar o fatjar do projeto:

```bash
mvn clean package -Pmysql ou mvn clean package -Ph2 
```

---

# Docker

Construir a imagem:

```bash
docker build -f flmane.dockerfile -t sowbreira/flmane .
```

Enviar para o Docker Hub:

```bash
docker push sowbreira/flmane
```

Executar localmente:

```bash
docker run -p 8080:8080 sowbreira/flmane
```

---

# Docker Compose

Baixe o arquivo de configuração:

```bash
curl -LfO https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml
```

Inicie os serviços:

```bash
docker compose up -d
```

Serviços incluídos:

* Fl-MANE
* MySQL 8.4
* phpMyAdmin

---

# Acessando a Aplicação

### Jogo

```text
http://localhost/flmane/html5/index.html
```

### phpMyAdmin

```text
http://localhost:8080
```

---

# Executando no Play With Docker

O Fl-MANE pode ser executado diretamente no ambiente Play With Docker.

1. Crie uma sessão no Play With Docker.
2. Baixe o arquivo `docker-compose.yaml`.
3. Execute:

```bash
docker compose up -d
```

4. Acesse:

```text
http://<url-gerada>/flmane/html5/index.html
```

---

# Tecnologias Utilizadas

* Java 21
* Tomcat Embedded
* Maven
* MySQL
* Docker
* HTML5
* JavaScript

---

# Autor

**Paulo Sobreira**

Projeto independente de simulação e gerenciamento de Fórmula 1 desenvolvido para fins educacionais e de entretenimento.
