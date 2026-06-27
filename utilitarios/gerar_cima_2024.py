#!/usr/bin/env python3
"""
Gera sprites top-view (_cima.png) para a temporada 2024.
Usa as imagens t2026 como base de silhueta e remapeia as cores para
as liveries reais de 2024 de cada equipe.
"""

import os
import sys
import math
from PIL import Image

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SRC_DIR   = os.path.join(RESOURCES, "carros", "t2026")
DST_DIR   = os.path.join(RESOURCES, "carros", "t2024")


# ─── definição de cores ───────────────────────────────────────────────────────

# Cores C1/C2 reais dos sprites 2026 (vistas nas propriedades)
SRC_2026 = {
    "mclaren":     ((255, 128,   0), ( 32,  32,  32)),
    "ferrari":     ((220,   0,   0), (255, 255, 255)),
    "mercedes":    ((  0, 210, 190), (  0,   0,   0)),
    "rbr":         (( 20,  30,  90), (220,   0,   0)),
    "astonmartin": ((  0,  95,  65), (180, 255, 180)),
    "alpine":      ((  0, 120, 255), (255, 120, 180)),
    "williams":    ((  0,  90, 255), (255, 255, 255)),
    "rb":          ((255, 255, 255), (  0,  40, 120)),
    "haas":        ((255, 255, 255), (220,   0,   0)),
    "audi":        ((210, 210, 210), (200,   0,   0)),
}

# Mapeamento: equipe 2024 → (imagem_base_2026, C1_2024, C2_2024)
TEAMS_2024 = {
    # arquivo destino      base 2026       C1 (cor principal)     C2 (cor secundária)
    "mclaren":     ("mclaren",     (255, 128,  10), (  5,  40, 150)),
    "rbr":         ("rbr",         ( 30,  50,  80), (200,  30,  30)),
    "mercedes":    ("mercedes",    (  0, 207, 186), ( 30,  30,  30)),
    "ferrari":     ("ferrari",     (195,   0,   0), (210, 210, 210)),
    "astonmartin": ("astonmartin", ( 16,  52,  52), ( 80, 120,  80)),
    "rb":          ("rb",          ( 10,  65, 205), (220,  20,  20)),
    "haas":        ("haas",        ( 40,  40,  40), (190, 190, 190)),
    "williams":    ("williams",    ( 40,  47,  82), ( 55,  55,  55)),
    "alpine":      ("alpine",      ( 10,  40,  80), (100,  55,  85)),
    "stake":       ("audi",        ( 20, 100,  20), (220, 220, 220)),
}


# ─── algoritmo de remapeamento ────────────────────────────────────────────────

def dist(a, b):
    return math.sqrt(sum((x - y) ** 2 for x, y in zip(a, b)))


def remap_image(src_path, src_c1, src_c2, dst_c1, dst_c2):
    """
    Remapeia os pixels de uma imagem:
    - Pixels escuros (chassis/pneus/sombras) são preservados com pequeno ajuste.
    - Pixels próximos de src_c1 → dst_c1
    - Pixels próximos de src_c2 → dst_c2
    - Pixels intermediários recebem uma combinação proporcional das duas cores destino,
      preservando o brilho relativo do original.
    """
    img = Image.open(src_path).convert("RGBA")
    out = img.copy()
    pix_in  = img.load()
    pix_out = out.load()

    DARK_THRESHOLD = 55   # abaixo disso = chassis/pneu, preservar
    NEUTRAL_LOW    = 200  # cinza quase branco (destacar detalhes)

    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = pix_in[x, y]
            if a == 0:
                continue

            brightness = (r + g + b) / 3
            saturation = max(r, g, b) - min(r, g, b)

            # Pixels muito escuros → chassis / pneus / sombras: preservar
            if brightness < DARK_THRESHOLD and saturation < 30:
                continue

            # Calcular distância para cada "polo" de cor
            d1 = dist((r, g, b), src_c1)
            d2 = dist((r, g, b), src_c2)
            total = d1 + d2
            if total < 1:
                total = 1

            # Peso de cada polo (inversamente proporcional à distância)
            w1 = d2 / total   # quanto mais longe de C2, mais peso em C1
            w2 = d1 / total

            # Cor alvo antes do ajuste de brilho
            tr = dst_c1[0] * w1 + dst_c2[0] * w2
            tg = dst_c1[1] * w1 + dst_c2[1] * w2
            tb = dst_c1[2] * w1 + dst_c2[2] * w2

            # Preservar variação de brilho do original (destaques e sombras)
            target_br = (tr + tg + tb) / 3
            if target_br > 1:
                scale = brightness / target_br
                # Limitar escala para não distorcer demais
                scale = max(0.4, min(2.0, scale))
                tr = min(255, tr * scale)
                tg = min(255, tg * scale)
                tb = min(255, tb * scale)

            pix_out[x, y] = (int(tr), int(tg), int(tb), a)

    return out


# ─── main ────────────────────────────────────────────────────────────────────

def main():
    os.makedirs(DST_DIR, exist_ok=True)
    print(f"Gerando sprites cima 2024 em: {DST_DIR}\n")

    for team, (src_team, dst_c1, dst_c2) in TEAMS_2024.items():
        src_path = os.path.join(SRC_DIR, f"{src_team}_cima.png")
        dst_path = os.path.join(DST_DIR, f"{team}_cima.png")

        if not os.path.exists(src_path):
            print(f"  AVISO: {src_path} não encontrado, pulando {team}")
            continue

        src_c1, src_c2 = SRC_2026[src_team]
        print(f"  {team:12s} <- {src_team}_cima.png  "
              f"C1:{src_c1}->{dst_c1}  C2:{src_c2}->{dst_c2}")

        result = remap_image(src_path, src_c1, src_c2, dst_c1, dst_c2)
        result.save(dst_path, "PNG")
        print(f"             -> salvo: {dst_path}")

    # Copiar nowing_cima.png de 2026 para 2024 (overlay de asa transparente)
    nowing_src = os.path.join(SRC_DIR, "nowing_cima.png")
    nowing_dst = os.path.join(DST_DIR, "nowing_cima.png")
    if os.path.exists(nowing_src):
        img = Image.open(nowing_src)
        img.save(nowing_dst, "PNG")
        print(f"\n  nowing_cima.png copiado de t2026.")
    else:
        print(f"\n  AVISO: nowing_cima.png não encontrado em t2026.")

    print("\nConcluído! Agora execute:")
    print("  python gerar_spritesheets.py t2024")


if __name__ == "__main__":
    main()
