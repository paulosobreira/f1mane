#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJECT_ROOT"

echo "=== Resetando SonarQube (do zero) ==="
docker compose rm -sf sonarqube 2>/dev/null || true
docker compose down 2>/dev/null || true
docker volume rm -f flmane_sonarqube_data 2>/dev/null || true

echo "=== Iniciando SonarQube ==="
docker compose up -d sonarqube

echo "=== Aguardando SonarQube ficar pronto ==="
until curl -s -u admin:admin "http://localhost:9000/api/system/status" 2>/dev/null | grep -q '"status":"UP"'; do
  echo "Aguardando SonarQube..."
  sleep 5
done
echo "SonarQube pronto!"

echo "=== Gerando token de acesso ==="
SONAR_TOKEN=$(curl -s -u admin:admin -X POST \
  "http://localhost:9000/api/user_tokens/generate" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "name=flmane-local-$(date +%s)" | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")
echo "Token gerado: $SONAR_TOKEN"

echo "=== Executando análise com JaCoCo ==="
mvn -Psonar clean test sonar:sonar -Dsonar.token="$SONAR_TOKEN"

echo ""
echo "=== Análise concluída! ==="
echo "Relatório disponível em: http://localhost:9000"
echo "Projeto: FlMane"
