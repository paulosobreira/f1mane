#!/bin/bash
set -e

if command -v docker &>/dev/null; then
  RUNTIME=docker
elif command -v podman &>/dev/null; then
  RUNTIME=podman
else
  echo "Nenhum runtime de container encontrado (docker ou podman)." >&2
  exit 1
fi

if $RUNTIME compose version &>/dev/null; then
  COMPOSE="$RUNTIME compose"
elif command -v podman-compose &>/dev/null; then
  COMPOSE="podman-compose"
else
  echo "Nenhum plugin compose encontrado para $RUNTIME." >&2
  exit 1
fi

$COMPOSE down
mvn clean package -Pmariadb -DskipTests
$RUNTIME build \
  -f flmane.dockerfile \
  -t docker.io/sowbreira/flmane:latest .
$COMPOSE up -d
