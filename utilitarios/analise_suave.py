#!/usr/bin/env python3
"""
Analisa logs [SUAVE_GANHO] e [SUAVE_GAP] das simulacoes headless.
Objetivo: determinar a distancia ideal entre carro real e carro suave,
priorizando evitar colisoes visuais entre carros.

Saida: relatorio com estatisticas e recomendacoes de parametros.
"""

import os
import re
import glob
from collections import defaultdict

LOG_DIR = "logs"

# Mapa de cicloMs por circuito (de circuitos.properties)
CICLO_MS = {
    "Albert Park": 140, "Monte Carlo": 140, "Jerez": 140, "Jacarepagua": 140,
    "Spa-Francorchamps": 150, "Bahrain": 150, "Sepang": 150, "Shangai": 150,
    "Donington": 150, "Fuji": 150, "Korea": 150, "India Buddh": 150, "Baku": 150,
    "Catalunya": 160, "Interlagos": 160, "Suzuka": 160, "Hockenheim6601": 160,
    "Valencia": 160, "Austin": 160,
    "Imola": 170, "Nuburgring": 170,
    "Yas Marina": 180, "Singapura": 180, "Monza": 180, "Silverstone": 180,
    "Indianapoles": 180, "Montreal": 180, "Magny-Cours": 180, "Hockenheim": 180,
    "Hungaro Ring": 180, "Istanbul Park": 180, "Estoril": 180, "Paul Ricard": 180,
    "Phoenix": 180, "RedBull Ring": 180, "Jeddah": 180,
    "Hermanos Rodriguez": 240,
}

FPS_30_MS = 1000.0 / 30
FPS_60_MS = 1000.0 / 60


def percentile(data, p):
    if not data:
        return 0
    s = sorted(data)
    k = (len(s) - 1) * p / 100
    lo, hi = int(k), min(int(k) + 1, len(s) - 1)
    return s[lo] + (s[hi] - s[lo]) * (k - lo)


def parse_logs():
    ganho_por_ciclo_ms = defaultdict(list)  # cicloMs -> [ganho]
    gaps_tracado_0 = []   # gaps no tracado central (mais critico)
    gaps_tracado_outros = []  # gaps nos outros tracados
    intersecoes = []      # [SUAVE_INTERSECAO]: gap < 30 no mesmo tracado/volta
    snaps = []
    circuito_atual = None
    ciclo_ms_atual = 160  # default

    log_files = sorted(glob.glob(os.path.join(LOG_DIR, "*.log")))
    if not log_files:
        print(f"Nenhum arquivo .log encontrado em '{LOG_DIR}/'")
        return None

    for log_file in log_files:
        try:
            with open(log_file, encoding="utf-8", errors="replace") as f:
                for line in f:
                    # Detecta circuito atual da simulacao
                    m = re.search(r"Circuito\s*:\s*(.+)", line)
                    if m:
                        circuito_atual = m.group(1).strip()
                        ciclo_ms_atual = CICLO_MS.get(circuito_atual, 160)
                        continue

                    # [SUAVE_GANHO] ciclo=N piloto=X no=Y ganho=G tracado=T iTracado=I volta=V
                    m = re.search(r"\[SUAVE_GANHO\].*ganho=(\d+).*tracado=(\d+).*iTracado=(\d+).*volta=(\d+)", line)
                    if m:
                        ganho = int(m.group(1))
                        tracado = int(m.group(2))
                        i_tracado = int(m.group(3))
                        volta = int(m.group(4))
                        if ganho > 0 and volta > 0:  # ignora antes de largar
                            ganho_por_ciclo_ms[ciclo_ms_atual].append(ganho)
                        continue

                    # [SUAVE_GAP] ciclo=N p1=X p2=Y gap=G tracado=T volta1=V volta2=V
                    m = re.search(r"\[SUAVE_GAP\].*gap=(\d+).*tracado=(\d+).*volta1=(\d+).*volta2=(\d+)", line)
                    if m:
                        gap = int(m.group(1))
                        tracado = int(m.group(2))
                        volta1 = int(m.group(3))
                        volta2 = int(m.group(4))
                        # Ignora retardatarios (diferenca de voltas > 0)
                        if abs(volta1 - volta2) <= 1:
                            if tracado == 0:
                                gaps_tracado_0.append(gap)
                            else:
                                gaps_tracado_outros.append(gap)
                        continue

                    # [SUAVE_INTERSECAO] gap < 30 no mesmo tracado
                    m = re.search(r"\[SUAVE_INTERSECAO\].*gap=(\d+).*tracado=(\d+).*iTracado1=(\d+).*iTracado2=(\d+)", line)
                    if m:
                        intersecoes.append({
                            "gap": int(m.group(1)),
                            "tracado": int(m.group(2)),
                            "iTracado1": int(m.group(3)),
                            "iTracado2": int(m.group(4)),
                        })
                        continue

                    # Snaps do atualizacaoSuave
                    m = re.search(r"atualizacaoSuave snap diff=(\d+)", line)
                    if m:
                        snaps.append(int(m.group(1)))
        except Exception as e:
            print(f"Erro ao ler {log_file}: {e}")

    return {
        "ganho_por_ciclo_ms": ganho_por_ciclo_ms,
        "gaps_tracado_0": gaps_tracado_0,
        "gaps_tracado_outros": gaps_tracado_outros,
        "intersecoes": intersecoes,
        "snaps": snaps,
    }


def relatorio(dados):
    print("=" * 60)
    print("  ANALISE DE SUAVIDADE VISUAL - F1-Mane")
    print("=" * 60)

    ganho_por_ciclo_ms = dados["ganho_por_ciclo_ms"]
    gaps_0 = dados["gaps_tracado_0"]
    gaps_out = dados["gaps_tracado_outros"]
    intersecoes = dados["intersecoes"]
    snaps = dados["snaps"]

    # --- Ganho por cicloMs ---
    print("\n## Distribuicao de Ganho Fisico por CicloMs\n")
    print(f"  {'CicloMs':>8} | {'N':>6} | {'P50':>5} | {'P75':>5} | {'P90':>5} | {'P99':>5} | {'Max':>5}")
    print(f"  {'-'*8}-+-{'-'*6}-+-{'-'*5}-+-{'-'*5}-+-{'-'*5}-+-{'-'*5}-+-{'-'*5}")

    todos_ganhos = []
    for ciclo_ms in sorted(ganho_por_ciclo_ms):
        g = ganho_por_ciclo_ms[ciclo_ms]
        todos_ganhos.extend(g)
        frames30 = ciclo_ms / FPS_30_MS
        frames60 = ciclo_ms / FPS_60_MS
        p50 = percentile(g, 50)
        p75 = percentile(g, 75)
        p90 = percentile(g, 90)
        p99 = percentile(g, 99)
        mx = max(g)
        print(f"  {ciclo_ms:>8} | {len(g):>6} | {p50:>5.1f} | {p75:>5.1f} | {p90:>5.1f} | {p99:>5.1f} | {mx:>5}")

    print(f"\n  Total amostras: {len(todos_ganhos)}")
    if todos_ganhos:
        print(f"  Ganho global: P50={percentile(todos_ganhos,50):.1f}  P90={percentile(todos_ganhos,90):.1f}  "
              f"P99={percentile(todos_ganhos,99):.1f}  Max={max(todos_ganhos)}")

    # --- Gaps no mesmo tracado ---
    print("\n## Gaps entre Pilotos no Mesmo Tracado (mesma volta)\n")
    for label, gaps in [("Tracado 0 (centro)", gaps_0), ("Tracados 1-4 (lateral/box)", gaps_out)]:
        if not gaps:
            print(f"  {label}: sem dados")
            continue
        gaps_criticos = [g for g in gaps if g < 50]
        print(f"  {label}:")
        print(f"    Total registros: {len(gaps)}")
        print(f"    P1={percentile(gaps,1):.0f}  P5={percentile(gaps,5):.0f}  "
              f"P10={percentile(gaps,10):.0f}  P25={percentile(gaps,25):.0f}  "
              f"P50={percentile(gaps,50):.0f}  Min={min(gaps)}")
        print(f"    Gap < 50 nos: {len(gaps_criticos)} ({100*len(gaps_criticos)/len(gaps):.1f}%)")
        print(f"    Gap < 20 nos: {sum(1 for g in gaps if g<20)} ({100*sum(1 for g in gaps if g<20)/len(gaps):.1f}%)")

    # --- Intersecoes ---
    print(f"\n## Intersecoes Visuais (gap < 30 nos, mesma volta)")
    print(f"  Total eventos: {len(intersecoes)}")
    if intersecoes:
        gaps_i = [x["gap"] for x in intersecoes]
        em_transicao = [x for x in intersecoes if x["iTracado1"] > 0 or x["iTracado2"] > 0]
        no_tracado_0 = [x for x in intersecoes if x["tracado"] == 0]
        print(f"  Gap P50={percentile(gaps_i,50):.0f}  P90={percentile(gaps_i,90):.0f}  Min={min(gaps_i)}")
        print(f"  Em transicao de tracado (iTracado>0): {len(em_transicao)} ({100*len(em_transicao)/len(intersecoes):.1f}%)")
        print(f"  No tracado 0 (centro):                {len(no_tracado_0)} ({100*len(no_tracado_0)/len(intersecoes):.1f}%)")
        print(f"  Gap = 0 (sobreposicao total):         {sum(1 for g in gaps_i if g==0)}")
        print(f"  Gap < 10 nos:                         {sum(1 for g in gaps_i if g<10)}")
        print(f"  Gap 10-20 nos:                        {sum(1 for g in gaps_i if 10<=g<20)}")
        print(f"  Gap 20-30 nos:                        {sum(1 for g in gaps_i if 20<=g<30)}")

    # --- Snaps ---
    print(f"\n## Snaps (SMOOTH_MAX_DIFF={500})")
    print(f"  Total snaps: {len(snaps)}")
    if snaps:
        print(f"  P50={percentile(snaps,50):.0f}  P90={percentile(snaps,90):.0f}  Max={max(snaps)}")

    # --- Recomendacoes ---
    print("\n## Recomendacoes de Parametros\n")

    # Lag ideal: P5 do gap no tracado 0 / 2 (metade do gap minimo critico)
    # Isso garante que o carro suave nunca ultrapasse visualmente o carro a frente
    min_gap_critico = percentile(gaps_0, 5) if gaps_0 else 30
    gap_absoluto_min = min(gaps_0) if gaps_0 else 20

    # Ganho P90 global (velocidade maxima esperada por tick)
    g_p90 = percentile(todos_ganhos, 90) if todos_ganhos else 25
    g_p99 = percentile(todos_ganhos, 99) if todos_ganhos else 35

    print(f"  Gap minimo absoluto entre pilotos (tracado 0): {gap_absoluto_min:.0f} nos")
    print(f"  Gap P5 (percentil 5% mais critico):            {min_gap_critico:.0f} nos")
    print(f"  Ganho fisico P90:                              {g_p90:.1f} nos/tick")
    print(f"  Ganho fisico P99:                              {g_p99:.1f} nos/tick")

    # Lag alvo: deve ser >= 0 (carro suave pode estar na frente do real em estado estavel)
    # E deve ser << gap_min para nao causar colisao visual
    lag_alvo = min(g_p90, min_gap_critico * 0.3)  # no maximo 30% do gap critico
    lag_alvo = max(lag_alvo, g_p90 * 0.5)  # no minimo metade do ganho P90

    print(f"\n  Lag alvo recomendado:                          {lag_alvo:.1f} nos")
    print(f"  (< {min_gap_critico*0.5:.0f} para evitar colisao visual com P5 do gap)")

    # MOD_GANHO_SUAVE que resulta em lag ~lag_alvo a 30fps
    # Formula: framesPerTick_30 * MOD/100 * lag_alvo ≈ g_p90
    # Para catala (160ms, 30fps): frames=4.8, MOD ≈ 100 * g_p90 / (4.8 * lag_alvo)
    frames30_ref = 160 / FPS_30_MS  # ~4.8 para circuito referencia 160ms
    mod_recomendado = 100 * g_p90 / (frames30_ref * lag_alvo) if lag_alvo > 0 else 20

    print(f"\n  MOD_GANHO_SUAVE recomendado (base circuito 160ms/30fps): {mod_recomendado:.1f}")
    print(f"  (atual: 20.0)")

    smooth_max = int(min_gap_critico * 0.5)
    smooth_max = max(smooth_max, int(g_p99 * 2))  # nunca menor que 2x ganho P99
    print(f"\n  SMOOTH_MAX_DIFF recomendado:                   {smooth_max} nos")
    print(f"  (atual: 500)")

    print("\n## Analise de Colisao Visual por Lag\n")
    # Simula qual seria o lag com diferentes MOD_GANHO_SUAVE
    print(f"  {'MOD':>6} | {'Lag est.(160ms/30fps)':>22} | {'Lag est.(160ms/60fps)':>22} | {'Risco colisao (gap_min={:.0f})'.format(gap_absoluto_min):>28}")
    print(f"  {'-'*6}-+-{'-'*22}-+-{'-'*22}-+-{'-'*28}")
    for mod in [5, 10, 15, 20, 25, 30, 40, 50]:
        lag30 = g_p90 * 100 / (frames30_ref * mod) if mod > 0 else 999
        frames60_ref = 160 / FPS_60_MS
        lag60 = g_p90 * 100 / (frames60_ref * mod) if mod > 0 else 999
        risco = "COLISAO VISUAL" if lag30 > gap_absoluto_min else ("ok" if lag30 < gap_absoluto_min * 0.5 else "margem fina")
        print(f"  {mod:>6} | {lag30:>22.1f} | {lag60:>22.1f} | {risco:>28}")

    print("\n" + "=" * 60)
    print("Analise concluida.")


if __name__ == "__main__":
    dados = parse_logs()
    if dados:
        relatorio(dados)
