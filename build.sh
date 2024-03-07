#!/bin/bash
docker compose down
mvn clean package
mvn war:war
docker build -f flmane.dockerfile . -t sowbreira/flmane
docker compose up
