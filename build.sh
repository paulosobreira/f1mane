#!/bin/bash
docker compose down
mvn clean package
mvn war:war
docker build -f f1mane.dockerfile . -t sowbreira/f1mane
docker compose up
