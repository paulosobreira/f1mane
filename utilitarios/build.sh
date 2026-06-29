#!/bin/bash
set -e
docker compose down
mvn clean package -Pmysql -DskipTests
docker build \
  -f flmane.dockerfile \
  -t sowbreira/flmane:latest .
docker compose up -d