# Fl-MANE

[English](README.md) | [Português (Brasil)](README.pt-BR.md)

**Fl-MANE (Formula livre Manager & Engineer)** is a Formula 1 racing management and engineering simulation game developed by **Paulo Sobreira**.

You take the roles of **Team Principal** and **Race Engineer** simultaneously — configuring the car before the race, issuing real-time commands to your driver, and reacting to dynamic race events like weather changes, accidents, and safety car periods.

---

## Execution Modes

Fl-MANE ships as a single JAR (`flmane.jar`) that supports three distinct modes:

| Mode | How to launch | Description |
|---|---|---|
| **Web (HTML5)** | `java -jar flmane.jar` | Embedded Tomcat on port 8080; play via browser |
| **Solo (Java Swing)** | `java -cp flmane.jar br.flmane.MainFrame` | Full Swing desktop game, no server required |
| **Multiplayer client** | Launched from the Swing launcher | Connects to a running Fl-MANE server |

When launched without arguments, the app shows a Swing launcher with QR Code and buttons for all three modes. Use `--headless` (or Docker) to skip the launcher GUI and run server-only.

---

## Features

### 🏎️ Race Configuration

Customize each race session before you start:

- **37 circuits** from different F1 eras, each with its own track layout, pit lane geometry, and background image
- **Multiple seasons** with era-specific cars, teams, and driver rosters
- Weather conditions: sun or rain (each circuit has a configurable rain probability)
- Number of laps, default driver skill, default car power, overtaking difficulty
- Night race support

### 🎮 Real-Time Driver Commands

Issue commands during the race without pausing:

- **Engine modes**: low RPM (A), normal (S), high RPM (D) — affects speed, fuel, and engine wear
- **Driving style**: cautious (Z), normal (X), aggressive (C) — affects overtaking and accident risk
- **ERS**: deploy stored energy for a speed boost (↓)
- **DRS**: open rear wing on straights for reduced drag (↑)
- **Racing line**: mouse or arrow keys to select the driver's trajectory

### 🔧 Pit Stop Strategy

Full pit stop control at any moment:

- Tire compound selection: **soft** (fast, high wear), **hard** (durable, slower), **rain**
- Fuel top-up and engine wear monitoring
- Configurable fast pit crew (50% chance of a faster service per race)
- React to safety car periods and weather transitions

### 🌦️ Dynamic Race Events

- **Weather changes** mid-race via a dedicated climate thread
- **Damage system**: flat tyre, broken wing, heavy crash, engine failure, blown engine — each with distinct performance consequences
- **Safety car** activates on major accidents; a recovery thread removes stranded cars
- **Accidents** and mechanical failures driven by driver stress, skill, and car condition

### 🏆 Championship and Career Mode

- Build a career across full championship seasons
- Start with a lower-tier team and earn better contracts
- Championship standings tracked in the database; persisted across sessions

### 🌐 Multiplayer (Web)

- Up to **5 simultaneous games** on a single server instance
- Browser-based client (HTML5 + JavaScript) or Java Swing client
- REST API (JAX-RS/Jersey) at `/letsRace/*`; session authentication via token in HTTP header
- Google login, guest sessions, or custom name

---

## Controls

| Key | Action |
|---|---|
| `A` | Low RPM — reduced performance, lower fuel and engine consumption |
| `S` | Normal RPM |
| `D` | High RPM — higher performance, increased fuel and engine consumption |
| `Z` | Cautious driving mode |
| `X` | Normal driving mode |
| `C` | Aggressive driving mode |
| `B` | Toggle Pit Stop mode |
| `↓` | Activate ERS |
| `↑` | Activate DRS (straights only) |
| Mouse / Arrows | Select racing line |
| `ESC` | Pause game |

---

## Requirements

- **Java 21** or newer
- Windows, Linux or macOS

### Run (pre-built JAR)

**Windows**
```bash
Fl-Mane.bat
```

**Linux / macOS**
```bash
./Fl-Mane.sh
```

---

## Building from Source

Two Maven build profiles are available. The profile determines the database at **build time** — the JDBC connection string is injected into `META-INF/persistence.xml` during packaging.

**H2 (local development — default)**
```bash
mvn clean package -Ph2 -DskipTests
```
Database stored at `~/flmane-data/flmane`. No external database needed.

**MySQL (Docker / production)**
```bash
mvn clean package -Pmysql -DskipTests
```
Expects MySQL at `db:3306/flmane`.

**Run the game after building**
```bash
java -jar target/flmane.jar
```

**Headless race simulation (debug)**
```bash
./simulacao.sh
# equivalent to:
java -cp target/flmane.jar br.flmane.MainFrameSimulacao 2024 Catalunya 72
```

---

## Docker

**Build image**
```bash
docker build -f flmane.dockerfile -t sowbreira/flmane .
```

**Push to Docker Hub**
```bash
docker push sowbreira/flmane
```

**Run locally**
```bash
docker run -p 8080:8080 sowbreira/flmane
```

---

## Docker Compose

Includes three services: Fl-MANE (port 80→8080), MySQL 8.4, and phpMyAdmin (port 8080). The app container waits for the MySQL health check before starting.

**Download compose file**
```bash
curl -LfO https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml
```

**Start all services**
```bash
docker compose up -d
```

| Service | URL |
|---|---|
| Game (HTML5) | `http://localhost/flmane/html5/index.html` |
| phpMyAdmin | `http://localhost:8080` |

---

## Play With Docker

1. Create a Play With Docker session
2. Download the compose file
3. Run `docker compose up -d`
4. Open `http://<generated-url>/flmane/html5/index.html`

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| UI (desktop) | Java Swing |
| Web server | Apache Tomcat (embedded) |
| REST API | JAX-RS / Jersey |
| Persistence | JPA (H2 or MySQL 8.4) |
| Build | Apache Maven |
| Containerization | Docker / Docker Compose |
| Web client | HTML5 + JavaScript |

---

## Architecture Overview

```
flmane.jar
├── MainLauncher       → Tomcat (port 8080) + Swing launcher
│   └── /flmane/html5/    (webapp extracted from JAR at startup)
├── MainFrame          → Standalone Swing game
│   └── ControleJogoLocal → ControleCiclo (tick loop ~50ms)
│                        ├── ControleCorrida  (race physics)
│                        ├── ControleBox      (pit stops)
│                        ├── ControleSafetyCar
│                        └── ControleClima    (weather thread)
└── AppletPaddock      → Multiplayer Java client
```

For full architectural detail, see [`docs/sdd.md`](docs/sdd.md).

---

## License

This project is distributed for educational and entertainment purposes.

Developed by **Paulo Sobreira**.
