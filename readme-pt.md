# Fl-MANE

**Fl-MANE (Formula livre Manager & Engineer)** é um simulador de gerenciamento e engenharia de Fórmula 1 desenvolvido por **Paulo Sobreira**.

Você assume simultaneamente os papéis de **Chefe de Equipe** e **Engenheiro de Corrida** — configurando o carro antes da corrida, enviando comandos em tempo real ao piloto e reagindo a eventos dinâmicos como mudanças climáticas, acidentes e períodos de safety car.

---

## Modos de Execução

O Fl-MANE é distribuído como um único JAR (`flmane.jar`) que suporta três modos distintos:

| Modo | Como iniciar | Descrição |
|---|---|---|
| **Web (HTML5)** | `java -jar flmane.jar` | Tomcat embutido na porta 8080; jogue pelo navegador |
| **Solo (Java Swing)** | `java -cp flmane.jar br.flmane.MainFrame` | Jogo desktop completo em Swing, sem servidor |
| **Multiplayer cliente** | Pelo launcher Swing | Conecta a um servidor Fl-MANE em execução |

Ao iniciar sem argumentos, o app exibe um launcher Swing com QR Code e botões para os três modos. Use `--headless` (ou Docker) para omitir o launcher e rodar apenas o servidor.

---

## Funcionalidades

### 🏎️ Configuração de Corridas

Personalize cada sessão antes de largar:

- **37 circuitos** de diferentes eras da F1, cada um com layout próprio, geometria do pit lane e imagem de fundo
- **Múltiplas temporadas** com carros, equipes e pilotos da época
- Condições climáticas: sol ou chuva (cada circuito tem probabilidade de chuva configurável)
- Número de voltas, skill padrão dos pilotos, potência padrão dos carros, dificuldade de ultrapassagem
- Suporte a corridas noturnas

### 🎮 Comandos em Tempo Real

Envie instruções durante a corrida sem pausar:

- **Giro do motor**: baixo (A), normal (S), alto (D) — afeta velocidade, consumo de combustível e desgaste do motor
- **Modo de pilotagem**: cauteloso (Z), normal (X), agressivo (C) — afeta ultrapassagens e risco de acidente
- **ERS**: aciona a energia armazenada para um boost de velocidade (↓)
- **DRS**: abre a asa traseira nas retas para reduzir o arrasto (↑)
- **Trajetória**: mouse ou setas para escolher a linha do piloto

### 🔧 Estratégia de Pit Stop

Controle total sobre as paradas a qualquer momento:

- Escolha do composto de pneu: **macio** (rápido, alto desgaste), **duro** (durável, mais lento), **chuva**
- Monitoramento de combustível e desgaste do motor
- Crew rápida configurável (50% de chance de serviço mais veloz por corrida)
- Reaja ao safety car e às mudanças climáticas

### 🌦️ Eventos Dinâmicos

- **Mudanças climáticas** durante a corrida via thread dedicada
- **Sistema de danos**: pneu furado, asa quebrada, batida forte, pane seca, motor explodido — cada um com consequências distintas de desempenho
- **Safety car** ativado em acidentes graves; thread de recolhimento remove os carros da pista
- **Acidentes e quebras mecânicas** gerados por stress do piloto, habilidade e condição do carro

### 🏆 Modo Campeonato e Carreira

- Construa uma carreira ao longo de temporadas completas
- Comece em equipes menores e conquiste contratos melhores
- Classificação do campeonato persistida no banco de dados entre sessões

### 🌐 Multiplayer (Web)

- Até **5 jogos simultâneos** em uma única instância do servidor
- Cliente pelo navegador (HTML5 + JavaScript) ou pelo Java Swing
- API REST (JAX-RS/Jersey) em `/letsRace/*`; autenticação por token no header HTTP
- Login com Google, sessão de convidado ou nome personalizado

---

## Controles

| Tecla | Ação |
|---|---|
| `A` | Giro baixo do motor — menor desempenho, menor consumo |
| `S` | Giro normal do motor |
| `D` | Giro alto do motor — maior desempenho, maior consumo |
| `Z` | Piloto em modo cauteloso |
| `X` | Piloto em modo normal |
| `C` | Piloto em modo agressivo |
| `B` | Ativar/desativar modo Pit Stop |
| `↓` | Ativar ERS |
| `↑` | Ativar DRS (apenas em retas) |
| Setas / Mouse | Escolher trajetória do piloto |
| `ESC` | Pausar o jogo |

---

## Requisitos

- **Java 21** ou superior
- Windows, Linux ou macOS

### Executar (JAR pré-compilado)

**Windows**
```bash
Fl-Mane.bat
```

**Linux / macOS**
```bash
./Fl-Mane.sh
```

---

## Compilação

Dois perfis Maven estão disponíveis. O perfil determina o banco de dados em **tempo de build** — a string de conexão JDBC é injetada em `META-INF/persistence.xml` durante o empacotamento.

**H2 (desenvolvimento local — padrão)**
```bash
mvn clean package -Ph2 -DskipTests
```
Banco armazenado em `~/flmane-data/flmane`. Nenhum banco externo necessário.

**MySQL (Docker / produção)**
```bash
mvn clean package -Pmysql -DskipTests
```
Espera MySQL em `db:3306/flmane`.

**Executar após compilar**
```bash
java -jar target/flmane.jar
```

**Simulação headless (debug de lógica)**
```bash
./simulacao.sh
# equivalente a:
java -cp target/flmane.jar br.flmane.MainFrameSimulacao 2024 Catalunya 72
```

---

## Docker

**Construir imagem**
```bash
docker build -f flmane.dockerfile -t sowbreira/flmane .
```

**Enviar para o Docker Hub**
```bash
docker push sowbreira/flmane
```

**Executar localmente**
```bash
docker run -p 8080:8080 sowbreira/flmane
```

---

## Docker Compose

Inclui três serviços: Fl-MANE (porta 80→8080), MySQL 8.4 e phpMyAdmin (porta 8080). O container da aplicação aguarda o healthcheck do MySQL antes de subir.

**Baixar o arquivo de configuração**
```bash
curl -LfO https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml
```

**Iniciar todos os serviços**
```bash
docker compose up -d
```

| Serviço | URL |
|---|---|
| Jogo (HTML5) | `http://localhost/flmane/html5/index.html` |
| phpMyAdmin | `http://localhost:8080` |

---

## Play With Docker

1. Crie uma sessão no Play With Docker
2. Baixe o arquivo `docker-compose.yaml`
3. Execute `docker compose up -d`
4. Acesse `http://<url-gerada>/flmane/html5/index.html`

---

## Tecnologias

| Camada | Tecnologia |
|---|---|
| Linguagem | Java 21 |
| UI (desktop) | Java Swing |
| Servidor web | Apache Tomcat (embutido) |
| API REST | JAX-RS / Jersey |
| Persistência | JPA (H2 ou MySQL 8.4) |
| Build | Apache Maven |
| Containerização | Docker / Docker Compose |
| Cliente web | HTML5 + JavaScript |

---

## Visão Geral da Arquitetura

```
flmane.jar
├── MainLauncher       → Tomcat (porta 8080) + launcher Swing
│   └── /flmane/html5/    (webapp extraído do JAR no startup)
├── MainFrame          → Jogo solo Swing
│   └── ControleJogoLocal → ControleCiclo (tick loop ~50ms)
│                        ├── ControleCorrida  (física da corrida)
│                        ├── ControleBox      (pit stops)
│                        ├── ControleSafetyCar
│                        └── ControleClima    (thread climática)
└── AppletPaddock      → Cliente multiplayer Java
```

Para detalhes completos da arquitetura, veja [`docs/sdd.md`](docs/sdd.md).

---

## Autor

**Paulo Sobreira**

Projeto independente de simulação e gerenciamento de Fórmula 1 desenvolvido para fins educacionais e de entretenimento.
