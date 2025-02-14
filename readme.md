# Fl-MANE
Fl-MANE (Formula Legends Manager & Engineer) is a Formula 1 racing management game developed by Paulo Sobreira. In the game, players take on the role of a racing team manager and engineer, making strategic and technical decisions to improve the performance of the car and driver during races.
## Main features:
- Race Setup: Players can customize various aspects of the race, such as the number of laps, weather conditions and car settings.
- Driving Commands: During the race, it is possible to send commands to the driver, adjusting the driving style and engine power to balance performance and resource management.
- Pit Stop Strategy: The game allows players to define and modify a pit stop strategy at any time, considering factors such as tire wear, fuel consumption and track conditions.
- Dynamic Events: Fl-MANE simulates dynamic events, including weather changes and accidents, adding an element of unpredictability to racing.
- Multiple Seasons and Tracks: The game offers multiple seasons and tracks, providing a diverse and challenging experience.
- Championship Mode: In championship mode, players can start with a beginner team and, by winning races, progress to more promising teams, facing increasingly challenging rivals.

## Controls:

- A - Low RPM (Less overall performance as well as less Engine and Fuel consumption)
- S - Normal RPM
- D - High RPM (More overall performance as well as more Engine and Fuel consumption)
- Z - Driver in Cautious Mode (Less performance in corners and reduces driver stress).
- X - Normal Driver.
- C - Driver in Aggressive Mode (More performance in corners and increases driver stress).
- B - Toggles Pit Mode on and off.
- Esc - Pauses the game.
- Down Arrow - Activates Ers.
- Up Arrow - Activates DRS (Used only on straights or parts of the circuit marked as straights).
- Arrows and mouse - Chooses a path for the driver to follow.

## Requirements

- Java 11 is required to run the game
- No Windows uses Fl-Mane.bat, no Linux uses Fl-Mane.sh

# Technical information

## Maven and Docker construction

- mvn clean package
- mvn war: war
- docker build -f flmane.dockerfile. -t sowbreira/flmane
- docker push sowbreira/flmane

## How to test on Play with Docker

Can be run on [Play with Docker](https://labs.play-with-docker.com/)

>Download the docker compose file
```
curl -LfO 'https://raw.githubusercontent.com/paulosobreira/f1mane/master/docker-compose.yaml'
```

>Start Mysql, PhpMyAdmin and FlMane containers
```
docker compose
```

>Access URL:
```
link_generated_by_playwithdocker/**flmane/html5/index.html**
```