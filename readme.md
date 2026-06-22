# Fl-MANE

**Fl-MANE (Formula livre Manager & Engineer)** is a Formula 1 racing management and engineering simulation game developed by **Paulo Sobreira**.

Take control of a Formula 1 team as both Team Principal and Race Engineer. Manage race strategy, configure your car, guide your driver, and make critical decisions during dynamic and unpredictable races.

## Features

### 🏎️ Race Configuration

Customize races with different:

* Track selections
* Weather conditions
* Number of laps
* Car setups

### 🎮 Driver Commands

Issue real-time commands during the race:

* Engine power modes
* Driving aggressiveness
* Overtaking strategies
* Resource management

### 🔧 Pit Stop Strategy

Plan and adjust pit stops at any time:

* Tire compound selection
* Fuel management
* Reaction to weather changes
* Safety car and race incidents

### 🌦️ Dynamic Race Events

Experience realistic race conditions:

* Weather changes
* Mechanical failures
* Accidents
* Strategic opportunities

### 🏆 Career and Championship Mode

Start with a lower-tier team and work your way up:

* Earn better contracts
* Compete against stronger opponents
* Fight for championship titles

### 🌍 Multiple Seasons and Circuits

Race across different Formula 1 eras and circuits with varying challenges and characteristics.

---

# Controls

| Key            | Action                                                                    |
| -------------- | ------------------------------------------------------------------------- |
| A              | Low RPM mode (reduced performance, lower fuel and engine consumption)     |
| S              | Normal RPM mode                                                           |
| D              | High RPM mode (higher performance, increased fuel and engine consumption) |
| Z              | Cautious driving mode                                                     |
| X              | Normal driving mode                                                       |
| C              | Aggressive driving mode                                                   |
| B              | Toggle Pit Mode                                                           |
| ↓ Arrow        | Activate ERS                                                              |
| ↑ Arrow        | Activate DRS (only on straights)                                          |
| Mouse / Arrows | Select racing line                                                        |
| ESC            | Pause game                                                                |

---

# Requirements

## Desktop Version

* Java 21 or newer
* Windows, Linux or macOS

Run:

### Windows

```bash
Fl-Mane.bat
```

### Linux

```bash
./Fl-Mane.sh
```

---

# Building From Source

## Maven

```bash
mvn clean package -Pmysql or mvn clean package -Ph2 
```
---

# Docker

Build image:

```bash
docker build -f flmane.dockerfile -t sowbreira/flmane .
```

Push image:

```bash
docker push sowbreira/flmane
```

Run locally:

```bash
docker run -p 8080:8080 sowbreira/flmane
```

---

# Docker Compose

Download:

```bash
curl -LfO https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml
```

Start services:

```bash
docker compose up -d
```

Services included:

* Fl-MANE
* MySQL 8.4
* phpMyAdmin

Access:

### Game

```text
http://localhost/flmane/html5/index.html
```

### phpMyAdmin

```text
http://localhost:8080
```

---

# Play With Docker

Fl-MANE can also be deployed directly on Play With Docker.

1. Create a Play With Docker session.
2. Download the compose file.
3. Run:

```bash
docker compose up -d
```

4. Open:

```text
http://<generated-url>/flmane/html5/index.html
```

---

# Technology Stack

* Java 21
* Embedded Tomcat
* Maven
* MySQL
* Docker
* HTML5
* JavaScript

---

# License

This project is distributed for educational and entertainment purposes.

Developed by **Paulo Sobreira**.
