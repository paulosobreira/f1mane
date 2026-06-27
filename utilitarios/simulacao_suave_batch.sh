#!/bin/bash
# Executa 50 simulações cobrindo diversas pistas/temporadas (12–16 voltas)
# para coletar dados [SUAVE_GANHO] e [SUAVE_GAP] e alimentar a análise.
# Uso: ./simulacao_suave_batch.sh [jar_path]

JAR="${1:-target/flmane.jar}"
LOG_DIR="logs"
ANALISE_SCRIPT="analise_suave.py"

# 50 combinações separadas por | para suportar nomes de circuito com espaço
# Formato: temporada|circuito|voltas
SIMULACOES=(
    "2024|Catalunya|14"
    "2024|Monza|12"
    "2023|Spa-Francorchamps|13"
    "2019|Silverstone|14"
    "2024|Interlagos|15"
    "2023|Suzuka|13"
    "2024|Bahrain|16"
    "2023|Singapura|14"
    "2019|Austin|13"
    "2018|Albert Park|12"
    "2018|Imola|15"
    "2017|Nuburgring|14"
    "2016|Montreal|16"
    "2015|Hungaro Ring|13"
    "2014|Baku|12"
    "2013|Jeddah|15"
    "2012|Hermanos Rodriguez|14"
    "2011|Valencia|13"
    "2010|Bahrain|16"
    "2024|Suzuka|14"
    "2023|Interlagos|15"
    "2019|Monza|12"
    "2018|Catalunya|13"
    "2017|Spa-Francorchamps|15"
    "2016|Silverstone|14"
    "2015|Singapura|16"
    "2014|Austin|13"
    "2013|Albert Park|12"
    "2012|Imola|14"
    "2011|Nuburgring|15"
    "2010|Sepang|13"
    "2024|Jeddah|16"
    "2023|Bahrain|12"
    "2019|Interlagos|14"
    "2018|Montreal|15"
    "2017|Baku|13"
    "2016|Suzuka|16"
    "2015|Catalunya|14"
    "2014|Monza|12"
    "2013|Silverstone|15"
    "2012|Albert Park|16"
    "2011|Spa-Francorchamps|13"
    "2010|Hungaro Ring|14"
    "2024|Monte Carlo|12"
    "2023|Austin|15"
    "2019|Bahrain|13"
    "2018|Singapura|16"
    "2017|Interlagos|14"
    "2016|Imola|12"
    "2015|Hermanos Rodriguez|14"
    "2026|Catalunya|13"
    "2026|Monza|12"
    "2024|Hungaro Ring|15"
    "2023|Nuburgring|14"
    "2019|Spa-Francorchamps|16"
    "2018|Austin|13"
    "2017|Bahrain|12"
    "2016|Interlagos|15"
    "2015|Silverstone|14"
    "2014|Suzuka|16"
    "2013|Montreal|13"
    "2012|Singapura|15"
    "2011|Albert Park|12"
    "2010|Imola|14"
    "2019|Monte Carlo|15"
    "2018|Baku|13"
    "2024|Sepang|16"
    "2023|Jeddah|12"
    "2017|Valencia|14"
    "2016|Hermanos Rodriguez|13"
)

TOTAL=${#SIMULACOES[@]}
OK=0
FALHA=0

echo "=========================================="
echo "  F1-Mane Simulacao Suave em Lote"
echo "  Total: $TOTAL simulacoes | JAR: $JAR"
echo "=========================================="

for i in "${!SIMULACOES[@]}"; do
    sim="${SIMULACOES[$i]}"
    IFS='|' read -r temporada circuito voltas <<< "$sim"
    NUM=$((i + 1))

    echo -n "[$NUM/$TOTAL] $temporada / $circuito / ${voltas}v ... "

    java -cp "$JAR" br.f1mane.MainFrameSimulacao "$temporada" "$circuito" "$voltas" 2>/dev/null
    EXIT=$?

    if [ $EXIT -eq 0 ]; then
        echo "OK"
        ((OK++))
    else
        echo "FALHA (exit=$EXIT)"
        ((FALHA++))
    fi
done

echo ""
echo "=========================================="
echo "  Resultado: $OK OK | $FALHA FALHAS"
echo "=========================================="

echo ""
echo "--- Rodando analise_suave.py ---"
if command -v python3 &>/dev/null; then
    python3 "$ANALISE_SCRIPT"
elif command -v python &>/dev/null; then
    python "$ANALISE_SCRIPT"
else
    echo "Python nao encontrado. Execute manualmente: python analise_suave.py"
fi
