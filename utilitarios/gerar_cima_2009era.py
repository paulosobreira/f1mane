#!/usr/bin/env python3
"""
Gera sprites top-view para t2010-t2013 usando t2009 como base.
Uso: python gerar_cima_2009era.py [t2013 t2012 t2011 t2010 ...]
"""

import os, sys, math
from PIL import Image

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SPRITES   = os.path.join(RESOURCES, "sprites")
CIMA_W, CIMA_H, ROW_Y = 90, 90, 40

# ─── t2009: equipes na ordem do properties ───────────────────────────────────
T2009_TEAMS = [
    ("brawngp",          (247, 247, 247), (144, 255,  33)),  # 0 – branco+lima
    ("redbullracing",    ( 30,  55,  96), (193,  30,  31)),  # 1 – azul+vermelho
    ("toyota",           (247, 247, 247), (180,   4,  24)),  # 2 – branco+vermelho
    ("williams",         ( 45,  47, 106), ( 40,  65, 157)),  # 3 – azul escuro+azul
    ("mclaren",          (224, 224, 224), (230,  80,  27)),  # 4 – prata+laranja
    ("renault",          (253, 222,   7), (247, 247, 247)),  # 5 – amarelo+branco
    ("ferrari",          (255,  51,   0), (255, 255, 255)),  # 6 – vermelho+branco
    ("scuderiatororosso",( 30,  55,  96), (183,  20,  21)),  # 7 – azul+vermelho
    ("bmwsauber",        (248, 248, 248), ( 40,  65, 157)),  # 8 – branco+azul
    ("forceindia",       (251,  90,   2), ( 44, 144, 107)),  # 9 – laranja+verde
]
T2009_IDX    = {n: i for i, (n, _, _) in enumerate(T2009_TEAMS)}
T2009_COLORS = {n: (c1, c2) for n, c1, c2 in T2009_TEAMS}

# ─── mapeamentos por temporada ────────────────────────────────────────────────
# img_field_sem_ext → (base_t2009, C1_destino, C2_destino)
SEASONS = {

    "t2010": {
        "rbr":        ("redbullracing",     ( 30,  55,  96), (193,  30,  31)),
        "ferrari":    ("ferrari",            (200,  21,  23), (255, 255, 255)),
        "mclaren":    ("mclaren",            (224, 224, 224), (243,  25,  35)),
        "mercedes":   ("bmwsauber",          (229, 234, 237), (144, 183, 188)),
        "renault":    ("renault",            (253, 222,   7), ( 10,  10,  10)),
        "williams":   ("williams",           ( 45,  47, 106), ( 40,  65, 157)),
        "forceindia": ("forceindia",         (251,  90,   2), ( 44, 144, 107)),
        "str":        ("scuderiatororosso",  ( 30,  55,  96), (183,  20,  21)),
        "sauber":     ("toyota",             (238, 232, 230), (111, 109, 112)),
        "lotus":      ("forceindia",         ( 25, 124,  72), (241, 230, 155)),
        "vrt":        ("toyota",             ( 70,  70,  74), (228,  22,  39)),
        "hrt":        ("bmwsauber",          (105, 112, 120), (241, 238, 237)),
    },

    "t2011": {
        "redbull":    ("redbullracing",     ( 30,  55,  96), (193,  30,  31)),
        "mclaren":    ("mclaren",            (224, 224, 224), (243,  25,  35)),
        "ferrari":    ("ferrari",            (200,  21,  23), (255, 255, 255)),
        "mercedes":   ("bmwsauber",          (229, 234, 237), (144, 183, 188)),
        "renault":    ("ferrari",            ( 33,  43,  45), (252, 216, 159)),  # preto+dourado
        "forceindia": ("forceindia",         (251,  90,   2), (140, 201, 137)),
        "sauber":     ("toyota",             (238, 232, 230), (111, 109, 112)),
        "str":        ("scuderiatororosso",  ( 30,  55,  96), (183,  20,  21)),
        "williams":   ("brawngp",            (255, 255, 255), ( 40,  65, 157)),
        "lotus":      ("forceindia",         ( 25, 124,  72), (241, 230, 155)),
        "marussia":   ("toyota",             ( 70,  70,  74), (228,  22,  39)),
        "hrt":        ("bmwsauber",          (255, 255, 255), (128,  20,  25)),
    },

    "t2012": {
        "redbull":    ("redbullracing",     ( 30,  55,  96), (193,  30,  31)),
        "mclaren":    ("mclaren",            (224, 224, 224), (243,  25,  35)),
        "lotus":      ("ferrari",            ( 33,  43,  45), (252, 216, 159)),  # preto+dourado
        "mercedes":   ("bmwsauber",          (229, 234, 237), (144, 183, 188)),
        "ferrari":    ("ferrari",            (200,  21,  23), (255, 255, 255)),
        "forceindia": ("forceindia",         (251,  90,   2), (140, 201, 137)),
        "sauber":     ("toyota",             (238, 232, 230), (111, 109, 112)),
        "tororosso":  ("scuderiatororosso",  ( 30,  55,  96), (183,  20,  21)),
        "williams":   ("brawngp",            (255, 255, 255), ( 40,  65, 157)),
        "caterham":   ("forceindia",         ( 25, 124,  72), (241, 230, 155)),
        "marussia":   ("toyota",             ( 70,  70,  74), (228,  22,  39)),
        "hrt":        ("bmwsauber",          (255, 255, 255), (128,  20,  25)),
    },

    "t2013": {
        "rbr":        ("redbullracing",     ( 73,  26, 107), (193,  30,  31)),  # roxo+vermelho
        "ferrari":    ("ferrari",            (200,  21,  23), (255, 255, 255)),
        "lotus":      ("ferrari",            ( 33,  43,  45), (252, 216, 159)),
        "mercedes":   ("bmwsauber",          (229, 234, 237), (144, 183, 188)),
        "forceindia": ("forceindia",         (251,  90,   2), (140, 201, 137)),
        "mclaren":    ("mclaren",            (224, 224, 224), (243,  25,  35)),
        "sauber":     ("toyota",             (105, 112, 120), (105, 112, 120)),
        "str":        ("scuderiatororosso",  ( 30,  55,  96), (183,  20,  21)),
        "williams":   ("williams",           ( 40,  47,  82), (255, 255, 255)),
        "caterham":   ("forceindia",         ( 59, 152, 105), (  0,  93,  46)),
        "marussia":   ("toyota",             ( 70,  70,  74), (228,  22,  39)),
    },
}

# ─── algoritmo ───────────────────────────────────────────────────────────────

def dist(a, b):
    return math.sqrt(sum((x-y)**2 for x,y in zip(a,b)))

def remap_image(src, sc1, sc2, dc1, dc2):
    out = src.copy(); pi = src.load(); po = out.load()
    for y in range(src.height):
        for x in range(src.width):
            r,g,b,a = pi[x,y]
            if a == 0: continue
            br = (r+g+b)/3; sat = max(r,g,b)-min(r,g,b)
            if br < 55 and sat < 30: continue
            d1 = dist((r,g,b), sc1); d2 = dist((r,g,b), sc2)
            t = max(d1+d2, 1); w1,w2 = d2/t, d1/t
            tr=dc1[0]*w1+dc2[0]*w2; tg=dc1[1]*w1+dc2[1]*w2; tb=dc1[2]*w1+dc2[2]*w2
            tbr=(tr+tg+tb)/3
            if tbr > 1:
                s = max(0.4, min(2.0, br/tbr))
                tr,tg,tb = min(255,tr*s),min(255,tg*s),min(255,tb*s)
            po[x,y] = (int(tr),int(tg),int(tb),a)
    return out

def read_car_order(props):
    cars = []
    with open(props, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line: continue
            k,_,v = line.partition("=")
            base = v.split(",")[4].split(";")[0].rsplit(".",1)[0]
            cars.append((k.strip(), base))
    return cars

def process_season(season, src_sheet):
    team_map = SEASONS.get(season)
    if not team_map:
        print(f"  Sem mapeamento para {season}"); return

    props = os.path.join(RESOURCES,"properties",season,"carros.properties")
    cars  = read_car_order(props)
    sheet = Image.open(os.path.join(SPRITES,f"{season}.png")).convert("RGBA")
    print(f"\n[{season}] {len(cars)} equipes")

    for idx,(car_key,base) in enumerate(cars):
        if base not in team_map:
            print(f"  [{idx}] {base}: sem mapeamento, pulando"); continue
        src_name,dc1,dc2 = team_map[base]
        si = T2009_IDX.get(src_name)
        if si is None:
            print(f"  [{idx}] base '{src_name}' nao encontrada em t2009"); continue
        sc1,sc2 = T2009_COLORS[src_name]
        sprite = src_sheet.crop((si*CIMA_W, ROW_Y, (si+1)*CIMA_W, ROW_Y+CIMA_H))
        result = remap_image(sprite, sc1, sc2, dc1, dc2)
        x = idx*CIMA_W
        sheet.paste(Image.new("RGBA",(CIMA_W,CIMA_H),(0,0,0,0)),(x,ROW_Y))
        sheet.paste(result,(x,ROW_Y),result)
        print(f"  [{idx}] {car_key:32s} <- t2009[{src_name}]")

    # nowing overlay
    no_x = max(len(cars),10)*CIMA_W
    nowing = src_sheet.crop((10*CIMA_W,ROW_Y,11*CIMA_W,ROW_Y+CIMA_H))
    sheet.paste(nowing,(no_x,ROW_Y),nowing)

    out = os.path.join(SPRITES,f"{season}.png")
    sheet.save(out,"PNG")
    print(f"  -> {out}")

def main():
    targets = [s for s in sys.argv[1:] if s.startswith("t")] if len(sys.argv)>1 else list(SEASONS.keys())
    src = Image.open(os.path.join(SPRITES,"t2009.png")).convert("RGBA")
    print(f"Base: t2009 {src.size}  |  Processando: {targets}")
    for s in targets:
        process_season(s, src)
    print("\nConcluido!")

if __name__ == "__main__":
    main()
