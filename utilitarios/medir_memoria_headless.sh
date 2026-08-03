#!/bin/bash
# Mede o consumo de memoria do servidor headless (container flmane) em dois
# pontos comparaveis entre execucoes:
#
#   1. pos-boot   - servidor no ar, pre-geracao pulada pelo marcador, zero corridas
#   2. com carga  - N sessoes jogando (o servidor agrupa as sessoes na mesma
#                   corrida: ControlePaddockServidor.jogar entra no primeiro jogo
#                   existente, entao N sessoes = 1 corrida com N jogadores)
#
# A imagem de producao e uma JRE (eclipse-temurin:25-jre-alpine) e NAO tem jcmd,
# so java e jfr. Por isso a medicao usa:
#   - RSS do processo (via /proc/1/status dentro do container) em cada ponto;
#   - Flight Recorder injetado por JAVA_TOOL_OPTIONS no boot, lido com `jfr`
#     depois da parada do container, para heap apos GC, metaspace e classes
#     carregadas.
#
# Uso:
#   utilitarios/medir_memoria_headless.sh --rotulo "antes" [--corridas 3] [--porta 80]
#
# Saida legivel no terminal e um bloco pronto para colar em
# openspec/changes/otimizar-memoria-headless/medicoes.md
set -euo pipefail

ROTULO=""
CORRIDAS=3
# >=1024 por default: em Podman rootless o bind na porta 80 falha
# ("rootlessport cannot expose privileged port 80").
PORTA=8000
HOST=localhost
ESPERA_CARGA=30
MANTER=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    --rotulo)   ROTULO="$2"; shift 2 ;;
    --corridas) CORRIDAS="$2"; shift 2 ;;
    --porta)    PORTA="$2"; shift 2 ;;
    --host)     HOST="$2"; shift 2 ;;
    --espera)   ESPERA_CARGA="$2"; shift 2 ;;
    --manter)   MANTER=1; shift ;;
    -h|--help)
      sed -n '2,25p' "$0"; exit 0 ;;
    *) echo "Argumento desconhecido: $1" >&2; exit 1 ;;
  esac
done

if [[ -z "$ROTULO" ]]; then
  echo "Informe --rotulo (ex.: 'antes', 'depois') para identificar a medicao." >&2
  exit 1
fi

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

RAIZ="$(cd "$(dirname "$0")/.." && pwd)"
cd "$RAIZ"

BASE_URL="http://${HOST}:${PORTA}/flmane/rest/letsRace"
JFR_CONTAINER=/tmp/medicao.jfr
SAIDA_DIR="$RAIZ/utilitarios/logs/medicoes"
mkdir -p "$SAIDA_DIR"
JFR_HOST="$SAIDA_DIR/medicao-${ROTULO}.jfr"

log() { echo "[medicao] $*"; }

# --- 1. sobe a stack de producao com o Flight Recorder ligado -----------------

log "derrubando stack anterior"
$COMPOSE -f docker-compose.yaml down >/dev/null 2>&1 || true

log "subindo stack de producao com Flight Recorder"
export FLMANE_JAVA_TOOL_OPTIONS="-XX:StartFlightRecording=filename=${JFR_CONTAINER},dumponexit=true,settings=default"
export FLMANE_PORTA_HOST="$PORTA"
$COMPOSE -f docker-compose.yaml up -d

log "aguardando servidor responder em ${BASE_URL}/verificaServico"
for _ in $(seq 1 120); do
  if curl -fsS --max-time 3 "${BASE_URL}/verificaServico" >/dev/null 2>&1; then
    break
  fi
  sleep 5
done
if ! curl -fsS --max-time 5 "${BASE_URL}/verificaServico" >/dev/null 2>&1; then
  echo "Servidor nao respondeu a tempo em ${BASE_URL}/verificaServico" >&2
  exit 1
fi

# --- 2. RSS pos-boot ----------------------------------------------------------

rss_kb() {
  $RUNTIME exec flmane sh -c "grep VmRSS /proc/1/status | awk '{print \$2}'" 2>/dev/null | tr -d '\r'
}

sleep 5
RSS_POS_BOOT=$(rss_kb)
log "RSS pos-boot: ${RSS_POS_BOOT} kB"

# --- 3. cria N corridas via REST ---------------------------------------------

log "descobrindo temporada, circuito e piloto para o cenario de carga"
CENARIO=$(curl -fsS --compressed "${BASE_URL}/temporadas" | python3 -c '
import json,sys
d = json.load(sys.stdin)
temporadas = list(d.values())[0] if isinstance(d, dict) else list(d)
# ultima temporada da lista: a mais recente, com o maior grid carregado
print(str(temporadas[-1]).lstrip("t") if temporadas else "")
' || true)
TEMPORADA=${CENARIO:-2024}

CIRCUITO=$(curl -fsS --compressed "${BASE_URL}/circuitos" | python3 -c '
import json,sys
d = json.load(sys.stdin)
vals = list(d.values()) if isinstance(d, dict) else list(d)
primeiro = vals[0] if vals else ""
if isinstance(primeiro, dict):
    primeiro = primeiro.get("arquivo", "")
print(primeiro)
' || true)

PILOTO=$(curl -fsS --compressed "${BASE_URL}/temporadasPilotos" | python3 -c "
import json,sys
d = json.load(sys.stdin)
pilotos = d.get('t${TEMPORADA}') if isinstance(d, dict) else None
if not pilotos:
    pilotos = next((v for v in d.values() if v), []) if isinstance(d, dict) else []
print(pilotos[0].get('id', '') if pilotos and isinstance(pilotos[0], dict) else '')
" || true)

if [[ -z "$CIRCUITO" || -z "$PILOTO" ]]; then
  echo "Nao foi possivel montar o cenario de carga automaticamente" >&2
  echo "  temporada='${TEMPORADA}' circuito='${CIRCUITO}' piloto='${PILOTO}'" >&2
  echo "Informe os valores manualmente editando o script antes de medir com carga." >&2
  CORRIDAS=0
fi

CRIADAS=0
for i in $(seq 1 "$CORRIDAS"); do
  TOKEN=$(curl -fsS --compressed "${BASE_URL}/criarSessaoVisitante" | python3 -c '
import json,sys
d = json.load(sys.stdin)
sessao = d.get("sessaoCliente", d) if isinstance(d, dict) else {}
print(sessao.get("token", ""))
' || true)
  if [[ -z "$TOKEN" ]]; then
    log "falha ao criar sessao visitante ${i}"
    continue
  fi
  if curl -fsS --compressed -H "token: ${TOKEN}" -H "idioma: pt" \
      "${BASE_URL}/jogar/${TEMPORADA}/${PILOTO}/${CIRCUITO}/10/M/50/5/false" >/dev/null; then
    CRIADAS=$((CRIADAS + 1))
  else
    log "falha ao entrar na corrida (sessao ${i})"
  fi
done
log "sessoes jogando: ${CRIADAS}/${CORRIDAS}"

if [[ "$CRIADAS" -gt 0 ]]; then
  log "deixando a corrida rodar por ${ESPERA_CARGA}s"
  sleep "$ESPERA_CARGA"
fi

RSS_CARGA=$(rss_kb)
log "RSS com ${CRIADAS} sessoes jogando: ${RSS_CARGA} kB"

# --- 4. para o container e le o Flight Recorder -------------------------------

log "parando container para o dump do Flight Recorder"
$RUNTIME stop flmane >/dev/null
$RUNTIME cp "flmane:${JFR_CONTAINER}" "$JFR_HOST" 2>/dev/null || log "sem arquivo JFR (dump nao gerado)"

RESUMO_JFR=""
if [[ -f "$JFR_HOST" ]]; then
  # `jfr print` cospe um evento por segundo de gravacao: resumir_jfr.py reduz
  # isso ao ultimo valor de cada metrica em vez de milhares de linhas repetidas.
  RESUMO_JFR=$($RUNTIME run --rm \
      --entrypoint /opt/java/openjdk/bin/jfr \
      -v "$SAIDA_DIR:/medicoes:z" \
      sowbreira/flmane:latest \
      print --events jdk.GCHeapSummary,jdk.MetaspaceSummary,jdk.ClassLoadingStatistics \
      "/medicoes/$(basename "$JFR_HOST")" 2>/dev/null \
      | python3 "$RAIZ/utilitarios/resumir_jfr.py" || true)
fi

if [[ "$MANTER" -eq 0 ]]; then
  $COMPOSE -f docker-compose.yaml down >/dev/null 2>&1 || true
fi

# --- 5. relatorio -------------------------------------------------------------

mib() { awk -v kb="$1" 'BEGIN { printf "%.1f", kb/1024 }'; }

RELATORIO_ARQ="$SAIDA_DIR/medicao-${ROTULO}.txt"
cat > "$RELATORIO_ARQ" <<RELATORIO

================ MEDICAO: ${ROTULO} ================
data:                $(date -Is)
sessoes jogando:     ${CRIADAS}
cenario:             temporada=${TEMPORADA} circuito=${CIRCUITO} piloto=${PILOTO} voltas=10

RSS pos-boot:        ${RSS_POS_BOOT} kB ($(mib "${RSS_POS_BOOT:-0}") MiB)
RSS com carga:       ${RSS_CARGA} kB ($(mib "${RSS_CARGA:-0}") MiB)
custo por sessao:    $(
  if [[ "$CRIADAS" -gt 0 && -n "$RSS_POS_BOOT" && -n "$RSS_CARGA" ]]; then
    awk -v a="$RSS_POS_BOOT" -v b="$RSS_CARGA" -v n="$CRIADAS" \
      'BEGIN { printf "%.1f MiB", (b-a)/1024/n }'
  else
    echo "n/a"
  fi
)

arquivo JFR:         ${JFR_HOST}

--- eventos JFR (heap apos GC, metaspace, classes carregadas) ---
${RESUMO_JFR:-nenhum evento lido}
====================================================

RELATORIO

cat "$RELATORIO_ARQ"
echo
echo "Relatorio salvo em: $RELATORIO_ARQ"
echo "Cole o bloco acima em openspec/changes/otimizar-memoria-headless/medicoes.md"
