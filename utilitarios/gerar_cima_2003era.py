#!/usr/bin/env python3
"""
Gera sprites top-view para t2007 e t2008 usando t2003 como base.
Uso: python gerar_cima_2003era.py [t2007 t2008 ...]
"""

import os, sys, math
from PIL import Image

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SPRITES   = os.path.join(RESOURCES, "sprites")
CIMA_W, CIMA_H, ROW_Y = 90, 90, 40

# ─── t2003: equipes na ordem do properties ───────────────────────────────────
T2003_TEAMS = [
    ("ferrari",  (218,  48,  33), (218,  48,  33)),  # 0 – vermelho
    ("williams", ( 34,  35, 117), (252, 250, 251)),  # 1 – azul escuro+branco
    ("mclaren",  (180, 183, 188), ( 17,  12,  19)),  # 2 – prata+escuro
    ("renault",  ( 54, 124, 219), (247, 223,  73)),  # 3 – azul+amarelo
    ("sauber",   ( 29,  48, 166), ( 87, 161, 198)),  # 4 – azul+azul claro
    ("jordan",   (234, 172,  31), ( 35,  27,  17)),  # 5 – amarelo+escuro
    ("jaguar",   ( 29,  82,  66), (246, 247, 249)),  # 6 – verde escuro+branco
    ("bar",      (240, 239, 237), (159,  47,  33)),  # 7 – branco+vermelho
    ("minardi",  ( 10,  19,  26), ( 10,  19,  26)),  # 8 – preto
    ("toyota",   (141,  53,  52), (253, 253, 253)),  # 9 – vermelho escuro+branco
]
T2003_IDX    = {n: i for i, (n, _, _) in enumerate(T2003_TEAMS)}
T2003_COLORS = {n: (c1, c2) for n, c1, c2 in T2003_TEAMS}

# ─── mapeamentos ─────────────────────────────────────────────────────────────
# img_field_sem_ext (exato do properties) → (base_t2003, C1_destino, C2_destino)
SEASONS = {

    "t2007": {
        # img sem extensao          base t2003  C1 destino           C2 destino
        "McLaren-MP4-22":    ("mclaren",  (224, 224, 224), (230,  80,  27)),
        "Ferrari-F2007":     ("ferrari",  (190,  31,   0), (190,  31,   0)),
        "BMW-Sauber-F1-07":  ("bar",      (248, 248, 248), ( 40,  65, 157)),
        "Renault-R27":       ("jordan",   (250, 170,  50), (247, 247, 247)),
        "Williams-FW29":     ("williams", ( 45,  47, 106), (247, 247, 247)),
        "Toyota-TF107":      ("toyota",   (247, 247, 247), (180,   4,  24)),
        "Redbull-RB3":       ("sauber",   ( 30,  55,  96), (193,  30,  31)),
        "Honda-RA107":       ("sauber",   ( 73,  93, 166), ( 77, 187, 158)),
        "Toro-Rosso-STR02":  ("sauber",   ( 30,  55,  96), (183,  20,  21)),
        "Spyker-F8-VII":     ("jordan",   (251,  90,   2), (251,  90,   2)),
        "Super-Aguri-SA06":  ("bar",      (255, 255, 255), (224,   6,  31)),
    },

    "t2008": {
        "McLarenMP4-23":     ("mclaren",  (224, 224, 224), (230,  80,  27)),
        "Ferrari-F2008":     ("ferrari",  (190,  31,   0), (190,  31,   0)),
        "BMW-F108":          ("bar",      (248, 248, 248), ( 40,  65, 157)),
        "Renault-R28":       ("jordan",   (250, 170,  50), (247, 247, 247)),
        "Willians-FW30":     ("williams", ( 45,  47, 106), (247, 247, 247)),
        "Toyota-TF108":      ("toyota",   (247, 247, 247), (180,   4,  24)),
        "Redbull-RB4":       ("sauber",   ( 30,  55,  96), (193,  30,  31)),
        "Honda-RA108":       ("sauber",   ( 73,  93, 166), ( 77, 187, 158)),
        "STR3":              ("sauber",   ( 30,  55,  96), (183,  20,  21)),
        "ForceIndia-VMJ01":  ("jaguar",   (251,  90,   2), ( 44, 144, 107)),
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
            print(f"  [{idx}] '{base}': sem mapeamento, pulando"); continue
        src_name,dc1,dc2 = team_map[base]
        si = T2003_IDX[src_name]
        sc1,sc2 = T2003_COLORS[src_name]
        sprite = src_sheet.crop((si*CIMA_W, ROW_Y, (si+1)*CIMA_W, ROW_Y+CIMA_H))
        result = remap_image(sprite, sc1, sc2, dc1, dc2)
        x = idx*CIMA_W
        sheet.paste(Image.new("RGBA",(CIMA_W,CIMA_H),(0,0,0,0)),(x,ROW_Y))
        sheet.paste(result,(x,ROW_Y),result)
        print(f"  [{idx}] {car_key:35s} <- t2003[{src_name}]")

    # nowing overlay
    no_x = max(len(cars),10)*CIMA_W
    nowing = src_sheet.crop((10*CIMA_W,ROW_Y,11*CIMA_W,ROW_Y+CIMA_H))
    sheet.paste(nowing,(no_x,ROW_Y),nowing)

    out = os.path.join(SPRITES,f"{season}.png")
    sheet.save(out,"PNG")
    print(f"  -> {out}")

def main():
    targets = [s for s in sys.argv[1:] if s.startswith("t")] if len(sys.argv)>1 else list(SEASONS.keys())
    src = Image.open(os.path.join(SPRITES,"t2003.png")).convert("RGBA")
    print(f"Base: t2003 {src.size}  |  Processando: {targets}")
    for s in targets:
        process_season(s, src)
    print("\nConcluido!")

if __name__ == "__main__":
    main()
