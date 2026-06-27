#!/bin/bash
# Executa simulações em lote e analisa colisões detectadas
# Uso: ./simulacao_batch.sh [jar_path]

JAR="${1:-target/flmane.jar}"
LOG_DIR="logs"

SIMULACOES=(
    "2024 Catalunya 72"
    "2024 Monza 53"
    "2024 Spa 44"
    "2023 Silverstone 52"
    "2023 Interlagos 71"
    "2024 Suzuka 53"
    "2023 Bahrain 57"
    "2024 Singapura 62"
    "2023 Monza 53"
    "2024 Austin 56"
)

TOTAL=0
TOTAL_SOBREPOSICOES=0

echo "=========================================="
echo "  F1-Mane Simulacao em Lote"
echo "  JAR: $JAR"
echo "=========================================="

for sim in "${SIMULACOES[@]}"; do
    read -r temporada circuito voltas <<< "$sim"
    echo ""
    echo "------------------------------------------"
    echo "Simulando: $temporada $circuito $voltas voltas"

    java -cp "$JAR" br.f1mane.MainFrameSimulacao "$temporada" "$circuito" "$voltas" 2>/dev/null

    LOG=$(ls -t "$LOG_DIR"/*.log 2>/dev/null | head -1)
    if [ -z "$LOG" ]; then
        echo "  ERRO: log nao encontrado"
        continue
    fi

    EVENTOS=$(grep "\[COLISAO_EVENTO\]" "$LOG" | tail -500)
    TOTAL_EVENTOS=$(echo "$EVENTOS" | grep -c "COLISAO_EVENTO" || echo 0)
    DIANTEIRA_CENTRO=$(echo "$EVENTOS" | grep -c "DIANTEIRA_CENTRO" || echo 0)
    SOBREPOSICOES=$(echo "$EVENTOS" | python3 -c "
import sys, re
count = 0
for line in sys.stdin:
    m = re.search(r'atras=\S+\(idx=(\d+)\).*frente=\S+\(idx=(\d+)\)', line)
    if m:
        atras = int(m.group(1))
        frente = int(m.group(2))
        # Sobreposicao real: atras_idx > frente_idx com diferenca pequena (<=200 nos)
        # Diferenca grande indica dobradinha legitima (lapping), nao bug
        diff = atras - frente
        if 0 < diff <= 200:
            count += 1
            import sys as _s; print('  DETALHE:', line.strip(), file=_s.stderr)
print(count)
" 2>/dev/null || echo 0)

    echo "  Eventos colisao: $TOTAL_EVENTOS"
    echo "  DIANTEIRA_CENTRO: $DIANTEIRA_CENTRO"

    if [ "$SOBREPOSICOES" -gt 0 ]; then
        echo "  ATENCAO: $SOBREPOSICOES sobreposicoes detectadas em $temporada $circuito $voltas"
        TOTAL_SOBREPOSICOES=$((TOTAL_SOBREPOSICOES + SOBREPOSICOES))
    else
        echo "  OK: sem sobreposicoes detectadas"
    fi

    TOTAL=$((TOTAL + 1))
done

echo ""
echo "=========================================="
echo "  RESULTADO FINAL"
echo "  Simulacoes executadas: $TOTAL"
echo "  Total sobreposicoes: $TOTAL_SOBREPOSICOES"
if [ "$TOTAL_SOBREPOSICOES" -eq 0 ]; then
    echo "  STATUS: OK - colisao fisica funcionando corretamente"
else
    echo "  STATUS: ATENCAO - sobreposicoes encontradas, revisar logica"
fi
echo "=========================================="
