#!/usr/bin/env python3
"""
Gera sprites top-view para temporadas historicas usando t2018 como base.
Extrai cada carro de cima do spritesheet t2018 e remapeia as cores para
as liveries da temporada alvo.
Uso: python gerar_cima_historico.py [t2017 t2016 t2015 t2014 ...]
"""

import os
import sys
import math
from PIL import Image

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SPRITES   = os.path.join(RESOURCES, "sprites")

CIMA_W, CIMA_H = 90, 90
ROW_Y = 40

# ─── equipes do t2018 (fonte), na ordem do properties ────────────────────────
# indice → (nome_base, C1, C2) conforme carros.properties t2018
T2018_TEAMS = [
    ("mercedes",   (220, 230, 230), (100, 140, 140)),  # 0
    ("ferrari",    (200,  21,  23), (255, 255, 255)),  # 1
    ("rbr",        ( 30,  50,  80), (200,  30,  30)),  # 2
    ("haas",       (255, 255, 255), (200,  21,  23)),  # 3
    ("renault",    (250, 170,  50), ( 33,  43,  45)),  # 4
    ("forceindia", (250, 150, 180), (250, 150, 180)),  # 5
    ("mclaren",    (255, 115,  10), (  5,  40, 150)),  # 6
    ("str",        ( 10,  65, 205), (255,   0,   0)),  # 7
    ("sauber",     (140,   0,  20), (255, 255, 255)),  # 8
    ("williams",   (255, 255, 255), ( 40,  47,  82)),  # 9
]

# Indice no spritesheet t2018 para cada base
T2018_INDEX = {name: i for i, (name, _, _) in enumerate(T2018_TEAMS)}
T2018_COLORS = {name: (c1, c2) for name, c1, c2 in T2018_TEAMS}


# ─── mapeamento por temporada ─────────────────────────────────────────────────
# img_field_sem_ext → (base_t2018, C1_destino, C2_destino)

SEASONS = {

    "t2017": {
        # ordem: mercedes ferrari rbr forceindia williams mclaren str renault haas sauber
        "mercedes":   ("mercedes",   (220, 230, 230), (100, 140, 140)),
        "ferrari":    ("ferrari",    (200,  21,  23), (255, 255, 255)),
        "rbr":        ("rbr",        ( 30,  50,  80), (200,  30,  30)),
        "forceindia": ("forceindia", (250, 150, 180), (180,  80, 140)),
        "williams":   ("williams",   (255, 255, 255), ( 40,  47,  82)),
        "mclaren":    ("mclaren",    (255, 115,  10), ( 40,  40,  40)),
        "str":        ("str",        ( 10,  65, 205), (255,   0,   0)),
        "renault":    ("renault",    (250, 170,  50), ( 33,  43,  45)),
        "haas":       ("haas",       (255, 255, 255), (200,  21,  23)),
        "sauber":     ("sauber",     (  0, 100, 205), (255, 255, 255)),
    },

    "t2016": {
        "mercedes":   ("mercedes",   (220, 230, 230), (100, 140, 140)),
        "ferrari":    ("ferrari",    (200,  21,  23), (255, 255, 255)),
        "rbr":        ("rbr",        ( 30,  50,  80), (200,  30,  30)),
        "forceindia": ("forceindia", (184, 184, 184), ( 35,  39,  47)),  # 2016 FI era cinza
        "williams":   ("williams",   (255, 255, 255), ( 40,  47,  82)),
        "mclaren":    ("mclaren",    ( 40,  40,  40), (234,  72,  71)),  # preto+vermelho
        "str":        ("str",        ( 30,  55,  96), (183,  20,  21)),
        "haas":       ("haas",       (255, 255, 255), (200,  21,  23)),
        "renault":    ("renault",    (250, 170,  50), ( 33,  43,  45)),
        "sauber":     ("str",        (  0,  38, 138), (248, 184,  54)),  # azul+amarelo
        "manor":      ("ferrari",    (255,  41,  21), (  9,   8,  84)),  # vermelho+azul escuro
    },

    "t2015": {
        "mercedes":   ("mercedes",   (229, 234, 237), (144, 183, 188)),
        "ferrari":    ("ferrari",    (200,  21,  23), (255, 255, 255)),
        "williams":   ("williams",   (255, 255, 255), ( 40,  47,  82)),
        "rbr":        ("rbr",        ( 73,  26, 107), (193,  30,  31)),  # roxo+vermelho
        "str":        ("str",        ( 30,  55,  96), (183,  20,  21)),
        "forceindia": ("forceindia", (184, 184, 184), ( 35,  39,  47)),
        "lotus":      ("sauber",     ( 33,  43,  45), (252, 216, 159)),  # preto+dourado
        "sauber":     ("str",        (  0,  38, 138), (248, 184,  54)),
        "mclaren":    ("mclaren",    ( 45,  49,  57), (234,  72,  71)),  # cinza+vermelho
        "manor":      ("ferrari",    (255,  41,  21), (  9,   8,  84)),
    },

    "t2014": {
        "mercedes":   ("mercedes",   (229, 234, 237), (144, 183, 188)),
        "williams":   ("williams",   (255, 255, 255), ( 40,  47,  82)),
        "rbr":        ("rbr",        ( 73,  26, 107), (193,  30,  31)),
        "mclaren":    ("mclaren",    (150, 150, 150), (200, 200, 200)),  # prata
        "ferrari":    ("ferrari",    (200,  21,  23), (255, 255, 255)),
        "forceindia": ("forceindia", (251,  90,   2), (140, 201, 137)),  # laranja+verde
        "sauber":     ("sauber",     (105, 112, 120), (105, 112, 120)),  # cinza
        "lotus":      ("sauber",     ( 33,  43,  45), (252, 216, 159)),  # preto+dourado
        "str":        ("str",        ( 30,  55,  96), (183,  20,  21)),
        "caterham":   ("ferrari",    ( 59, 152, 105), (  0,  93,  46)),  # verde
        "marussia":   ("haas",       ( 70,  70,  74), (228,  22,  39)),  # cinza+vermelho
    },
}


# ─── utilitarios ─────────────────────────────────────────────────────────────

def dist(a, b):
    return math.sqrt(sum((x - y) ** 2 for x, y in zip(a, b)))


def remap_image(src_img, src_c1, src_c2, dst_c1, dst_c2):
    out = src_img.copy()
    pix_in  = src_img.load()
    pix_out = out.load()
    DARK = 55
    for y in range(src_img.height):
        for x in range(src_img.width):
            r, g, b, a = pix_in[x, y]
            if a == 0:
                continue
            br  = (r + g + b) / 3
            sat = max(r, g, b) - min(r, g, b)
            if br < DARK and sat < 30:
                continue
            d1 = dist((r, g, b), src_c1)
            d2 = dist((r, g, b), src_c2)
            total = max(d1 + d2, 1)
            w1, w2 = d2 / total, d1 / total
            tr = dst_c1[0]*w1 + dst_c2[0]*w2
            tg = dst_c1[1]*w1 + dst_c2[1]*w2
            tb = dst_c1[2]*w1 + dst_c2[2]*w2
            tbr = (tr + tg + tb) / 3
            if tbr > 1:
                s = max(0.4, min(2.0, br / tbr))
                tr, tg, tb = min(255, tr*s), min(255, tg*s), min(255, tb*s)
            pix_out[x, y] = (int(tr), int(tg), int(tb), a)
    return out


def read_car_order(props_path):
    cars = []
    with open(props_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, val = line.partition("=")
            parts = val.split(",")
            base = parts[4].split(";")[0].rsplit(".", 1)[0]
            cars.append((key.strip(), base))
    return cars


# ─── main ─────────────────────────────────────────────────────────────────────

def process_season(season, t2018_sheet):
    team_map = SEASONS.get(season)
    if not team_map:
        print(f"  Sem mapeamento para {season}, pulando")
        return

    props   = os.path.join(RESOURCES, "properties", season, "carros.properties")
    cars    = read_car_order(props)
    sheet   = Image.open(os.path.join(SPRITES, f"{season}.png")).convert("RGBA")

    print(f"\n[{season}] {len(cars)} equipes")

    for idx, (car_key, base_name) in enumerate(cars):
        if base_name not in team_map:
            print(f"  [{idx}] {base_name}: sem mapeamento, pulando")
            continue

        src_name, dst_c1, dst_c2 = team_map[base_name]
        src_idx = T2018_INDEX.get(src_name)
        if src_idx is None:
            print(f"  [{idx}] {base_name}: base '{src_name}' nao encontrada em t2018")
            continue

        # Extrair sprite de cima do t2018
        x18 = src_idx * CIMA_W
        src_sprite = t2018_sheet.crop((x18, ROW_Y, x18 + CIMA_W, ROW_Y + CIMA_H))

        # Remap de cores
        src_c1, src_c2 = T2018_COLORS[src_name]
        result = remap_image(src_sprite, src_c1, src_c2, dst_c1, dst_c2)

        # Colar no spritesheet alvo
        x = idx * CIMA_W
        sheet.paste(Image.new("RGBA", (CIMA_W, CIMA_H), (0, 0, 0, 0)), (x, ROW_Y))
        sheet.paste(result, (x, ROW_Y), result)
        print(f"  [{idx}] {car_key:30s} <- t2018[{src_name}] C1:{dst_c1} C2:{dst_c2}")

    # nowing overlay (posicao reservada)
    no_x = max(len(cars), 10) * CIMA_W
    nowing_src = t2018_sheet.crop((10 * CIMA_W, ROW_Y, 11 * CIMA_W, ROW_Y + CIMA_H))
    sheet.paste(nowing_src, (no_x, ROW_Y), nowing_src)

    out = os.path.join(SPRITES, f"{season}.png")
    sheet.save(out, "PNG")
    print(f"  -> salvo: {out}")


def main():
    targets = [s for s in sys.argv[1:] if s.startswith("t")] if len(sys.argv) > 1 else list(SEASONS.keys())
    t2018_sheet = Image.open(os.path.join(SPRITES, "t2018.png")).convert("RGBA")
    print(f"Base: t2018 ({t2018_sheet.size})")
    print(f"Processando: {targets}")
    for s in targets:
        process_season(s, t2018_sheet)
    print("\nConcluido!")


if __name__ == "__main__":
    main()
