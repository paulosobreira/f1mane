#!/usr/bin/env python3
"""
Melhora os sprites top-view das temporadas classicas (t1974-t1993)
aplicando cores historicamente precisas de cada livery real.

As cores C1/C2 de destino sao baseadas nas liveries documentadas:
Marlboro, JPS, Camel, Canon, Gitanes, Parmalat, Martini, etc.

Uso: python melhorar_cima_classicos.py [t1974 t1983 ...]
"""

import os, sys, math
from PIL import Image

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SPRITES   = os.path.join(RESOURCES, "sprites")

LADO_W, LADO_H = 180, 40
CIMA_W, CIMA_H =  90, 90
ROW_CIMA = 40

# ─── paleta de cores historicas ───────────────────────────────────────────────
# Cores referenciais de liveries classicas da F1

FERRARI_RED    = (200,  30,   0)
FERRARI_RED2   = (160,  20,   0)
MARLBORO_RED   = (200,   0,   0)
MARLBORO_WHITE = (240, 240, 240)
JPS_BLACK      = ( 25,  20,  20)
JPS_GOLD       = (190, 155,  35)
CAMEL_YELLOW   = (220, 185,   0)
CAMEL_BLACK    = ( 30,  30,  30)
CANON_YELLOW   = (230, 200,   0)
WILLIAMS_BLUE  = ( 25,  45, 110)
ELF_BLUE       = ( 40,  75, 165)
ELF_WHITE      = (240, 240, 240)
GITANES_BLUE   = ( 20,  50, 135)
GITANES_DARK   = ( 15,  35,  90)
PARMALAT_WHITE = (240, 240, 240)
PARMALAT_BLUE  = (  0,  60, 175)
MARTINI_WHITE  = (240, 240, 240)
MARTINI_RED    = (180,  30,  50)
LEYTON_TEAL    = (  0, 125, 155)
LEYTON_DARK    = (  0,  60,  80)
BENETTON_GREEN = ( 40, 160,  70)
BENETTON_BLUE  = (  0,  40, 200)
ARROWS_GOLD    = (200, 165,  25)
ARROWS_DARK    = ( 20,  20,  20)
BARCLAY_YELLOW = (215, 190,   0)
OLIVETTI_BLUE  = ( 20,  35,  90)
OLIVETTI_WHITE = (220, 220, 220)
FOOTWORK_YEL   = (215, 190,   0)
FOOTWORK_WHITE = (240, 240, 240)
BRAUN_DARK     = ( 35,  30,  75)
BRAUN_TEAL     = ( 40, 150, 165)
TYRRELL_BLUE   = ( 45,  75, 165)
TYRRELL_WHITE  = (240, 240, 240)
SHADOW_BLACK   = ( 25,  25,  25)
SHADOW_DARK    = ( 45,  40,  50)
TOLEMAN_CREAM  = (200, 175,  60)
TOLEMAN_DARK   = ( 50,  45,  80)
DALLARA_BLUE   = ( 20,  30, 120)
DALLARA_WHITE  = (220, 220, 220)
LARROUSSE_YEL  = (210, 180,  10)
LARROUSSE_DARK = ( 40,  35,  85)
JORDAN_BLUE    = ( 15,  50, 155)
JORDAN_RED     = (200,  20,  30)
SAUBER_DARK    = ( 45,  40,  40)
SAUBER_LIGHT   = (180, 175, 170)
MINARDI_DARK   = ( 45,  35,  40)
MINARDI_YEL    = (220, 185,   0)
LOLA_RED       = (190,  35,  30)
LOLA_WHITE     = (235, 235, 235)
AGS_DARK       = ( 55,  45,  55)
AGS_RED        = (180,  60,  65)
BRM_GREEN      = ( 35,  95,  45)
BRM_WHITE      = (220, 220, 220)
ALFA_RED       = (200,  25,  35)
ALFA_WHITE     = (235, 235, 235)
RENAULT_YEL    = (240, 205,   5)
RENAULT_DARK   = (  0,  35, 120)
MARCH_RED      = (200,  45,   0)
MARCH_ORANGE   = (210,  80,  10)
ISO_RED        = (185,  40,  40)
ISO_WHITE      = (230, 230, 230)

# ─── mapeamento por temporada ─────────────────────────────────────────────────
# img_field_sem_ext → (C1_destino, C2_destino)
# C1 = cor principal do corpo; C2 = cor secundaria/detalhe
# As propriedades .properties tem C1/C2 originais (usados como SRC no remap)

LIVERIES = {

    "t1974": {
        # McLaren M23: Marlboro vermelho/branco
        "Mclaren-M23":       (MARLBORO_RED,   MARLBORO_WHITE),
        # Ferrari 312B3: vermelho italiano
        "Ferrari-312B3":     (FERRARI_RED,    FERRARI_RED2),
        # Tyrrell 007: azul Elf
        "Tyrrel-007":        (ELF_BLUE,       ELF_WHITE),
        # Lotus 76: JPS preto/dourado
        "Lotus-76":          (JPS_BLACK,      JPS_GOLD),
        # Brabham BT44: Martini branco
        "Brabham-BT44":      (MARTINI_WHITE,  MARTINI_RED),
        # BRM P201: verde BRM
        "BRM-P201":          (BRM_GREEN,      BRM_WHITE),
        # Shadow DN3: preto
        "Shadow-DN3":        (SHADOW_BLACK,   SHADOW_DARK),
        # March 741: vermelho STP
        "March-741":         (MARCH_RED,      MARCH_ORANGE),
        # ISO Williams: vermelho/branco
        "Iso_Williams-FW":   (ISO_RED,        ISO_WHITE),
        # Surtees TS16: vermelho/branco
        "Surtess-TS16":      ((200, 50, 70),  (220, 220, 220)),
        # Ensign N174: laranja/escuro
        "Ensign-N174":       ((200, 90, 30),  ( 40,  35,  40)),
    },

    "t1983": {
        # Ferrari 126C3: vermelho
        "Ferrari-126C3":     (FERRARI_RED,    FERRARI_RED2),
        # Renault RE40: amarelo Gitanes/azul
        "Renault-RE40":      (RENAULT_YEL,    RENAULT_DARK),
        # Brabham BT52: Parmalat branco/azul
        "Brabham-BT52":      (PARMALAT_BLUE,  PARMALAT_WHITE),
        # Williams FW08C: Saudia/TAG branco
        "Williams-FW08C":    (MARLBORO_WHITE, (  0, 130,  60)),
        # McLaren MP4/1C: Marlboro vermelho/branco
        "Mclaren-MP4-1C":    (MARLBORO_RED,   MARLBORO_WHITE),
        # Alfa Romeo 183T: vermelho/branco
        "AlfaRomeo-183T":    (ALFA_RED,       ALFA_WHITE),
        # Tyrrell 012: verde Benetton
        "Tyrrel-012":        (BENETTON_GREEN, (220, 220, 220)),
        # Toleman TG183: creme/escuro
        "Toleman-TG183":     (TOLEMAN_CREAM,  TOLEMAN_DARK),
        # Lotus 92: JPS preto/dourado
        "Lotus-92":          (JPS_BLACK,      JPS_GOLD),
        # Arrows A6: Warsteiner dourado/escuro
        "Arrows-A6":         (ARROWS_GOLD,    ARROWS_DARK),
        # Ligier JS21: Gitanes azul
        "Ligier-JS21":       (GITANES_BLUE,   GITANES_DARK),
    },

    "t1986": {
        # Williams FW11: Canon amarelo/azul
        "Williams-FW11":     (CANON_YELLOW,   WILLIAMS_BLUE),
        # McLaren MP4/2C: Marlboro vermelho/branco
        "Mclaren-MP4-2C":    (MARLBORO_RED,   MARLBORO_WHITE),
        # Ferrari F186: vermelho
        "Ferrari-F186":      (FERRARI_RED,    FERRARI_RED2),
        # Lotus 98T: JPS preto/dourado
        "Lotus-98T":         (JPS_BLACK,      JPS_GOLD),
        # Benetton B186: verde/azul
        "Benetton-B186":     (BENETTON_GREEN, BENETTON_BLUE),
        # Arrows A9: Barclay amarelo/escuro
        "Arrows-A9":         (BARCLAY_YELLOW, ARROWS_DARK),
        # Ligier JS27: Gitanes azul
        "Ligier-JS27":       (GITANES_BLUE,   GITANES_DARK),
        # Tyrrell 015: azul/branco
        "Tyrrel-015":        (TYRRELL_BLUE,   TYRRELL_WHITE),
        # Minardi M186: escuro/amarelo Simod
        "Minardi-M186":      (MINARDI_DARK,   MINARDI_YEL),
        # Lola THL2: Ford vermelho/escuro
        "Lola-THL2":         (LOLA_RED,       ( 30,  30,  30)),
        # Brabham BT55: Olivetti azul escuro/branco
        "Brabham-BT55":      (OLIVETTI_BLUE,  OLIVETTI_WHITE),
    },

    "t1987": {
        # Williams FW11B: Canon amarelo/azul
        "Williams-FW11B":    (CANON_YELLOW,   WILLIAMS_BLUE),
        # McLaren MP4/3: Marlboro vermelho/branco
        "McLaren-MP4-3":     (MARLBORO_RED,   MARLBORO_WHITE),
        # Lotus 99T: Camel amarelo/preto
        "Lotus-99T":         (CAMEL_YELLOW,   CAMEL_BLACK),
        # Ferrari F187: vermelho
        "Ferrari-F187":      (FERRARI_RED,    FERRARI_RED2),
        # Benetton B187: verde/vermelho (USMotorsport)
        "Benetton-B187":     (BENETTON_GREEN, (160,  25,  25)),
        # Tyrrell DG016: Data General azul escuro
        "Tyrrell-DG016":     (( 35,  40, 100), (170, 170, 185)),
        # Arrows A10: USF&G amarelo/vermelho
        "Arrows-A10":        ((215,  45,   0), (230, 200,   0)),
        # Brabham BT56: Olivetti azul/branco
        "Brabham-BT56":      (OLIVETTI_BLUE,  OLIVETTI_WHITE),
        # Lola LC87: Beatrice azul/vermelho
        "Lola-LC87":         (( 30,  35, 120), (190,  25,  35)),
        # Zakspeed 861: West branco/preto
        "Zakspeed-861":      ((230, 230, 230), ( 20,  20,  20)),
        # Ligier JS29: Gitanes azul
        "Ligier-JS29":       (GITANES_BLUE,   GITANES_DARK),
        # Minardi M187: escuro/amarelo
        "Minardi-M187":      (MINARDI_DARK,   MINARDI_YEL),
    },

    "t1988": {
        # McLaren MP4/4: Marlboro vermelho/branco (carro dominante 88)
        "Mclaren-MP4-4":     (MARLBORO_RED,   MARLBORO_WHITE),
        # Ferrari F187/88C: vermelho
        "Ferrari-F187-88C":  (FERRARI_RED,    FERRARI_RED2),
        # Williams FW12: Canon amarelo
        "Williams-FW12":     (CANON_YELLOW,   WILLIAMS_BLUE),
        # Benetton B188: verde/azul Benetton
        "Benetton-B188":     (BENETTON_GREEN, BENETTON_BLUE),
        # Lotus 100T: Camel amarelo
        "Lotus-100T":        (CAMEL_YELLOW,   CAMEL_BLACK),
        # Tyrrell 017: Braun escuro
        "Tyrrel-017":        (BRAUN_DARK,     BRAUN_TEAL),
        # Arrows A10B: USF&G amarelo
        "Arrows-A10B":       ((215,  45,   0), (225, 195,   0)),
        # Minardi M188: escuro/amarelo Simod
        "Minardi-M188":      (MINARDI_DARK,   MINARDI_YEL),
        # Dallara F188: Scuderia Italia azul escuro/branco
        "Dallara-F188":      (DALLARA_BLUE,   DALLARA_WHITE),
        # Ligier JS31: Gitanes azul
        "Ligier-JS31":       (GITANES_BLUE,   GITANES_DARK),
        # AGS JH22: escuro/vermelho
        "AGS-JH22":          (AGS_DARK,       AGS_RED),
    },

    "t1990": {
        # McLaren MP4/5B: Marlboro vermelho/branco
        "Mclaren-MP4_5B":    (MARLBORO_RED,   MARLBORO_WHITE),
        # Ferrari F1/641: vermelho
        "Ferrari-F1641":     (FERRARI_RED,    FERRARI_RED2),
        # Benetton B190: Camel amarelo/verde
        "Benetton-B190":     (CAMEL_YELLOW,   BENETTON_GREEN),
        # Williams FW13B: Canon amarelo/azul
        "Williams-FW13B":    (CANON_YELLOW,   WILLIAMS_BLUE),
        # Tyrrell 019: Braun escuro/branco
        "Tyrrel-019":        (BRAUN_DARK,     BRAUN_TEAL),
        # Lola LC90: Larrousse amarelo escuro
        "Lola-LC90":         (LARROUSSE_YEL,  LARROUSSE_DARK),
        # Leyton House CG901: TEAL caracteristico
        "LeytonHouse-CG901": (LEYTON_TEAL,    LEYTON_DARK),
        # Lotus 102: Camel amarelo/preto
        "Lotus-102":         (CAMEL_YELLOW,   CAMEL_BLACK),
        # Arrows A11: Footwork amarelo/branco
        "Arrows-A11":        (FOOTWORK_YEL,   FOOTWORK_WHITE),
        # Brabham BT58: Olivetti azul/branco
        "Brabham-BT58":      (OLIVETTI_BLUE,  OLIVETTI_WHITE),
        # Minardi M190: escuro/amarelo
        "Minardi-M190":      (MINARDI_DARK,   MINARDI_YEL),
    },

    "t1993": {
        # Williams FW15C: Canon amarelo/azul
        "Williams-FW15C":    (CANON_YELLOW,   WILLIAMS_BLUE),
        # Benetton B193: Camel amarelo/verde
        "Benetton-B193":     (CAMEL_YELLOW,   BENETTON_GREEN),
        # McLaren MP4/8: Marlboro vermelho/branco
        "Mclaren-MP4_8":     (MARLBORO_RED,   MARLBORO_WHITE),
        # Ferrari F93A: vermelho
        "Ferrari-F93A":      (FERRARI_RED,    FERRARI_RED2),
        # Ligier JS39: Gitanes azul
        "Ligier-JS39":       (GITANES_BLUE,   GITANES_DARK),
        # Lotus 107B: Castrol/Hitachi verde/branco
        "Lotus-107B":        ((  0, 130,  60), (230, 230, 230)),
        # Sauber C12: escuro metalico
        "Sauber-C12":        (SAUBER_DARK,    SAUBER_LIGHT),
        # Minardi M193: escuro/branco
        "Minardi-M193":      (MINARDI_DARK,   (195, 195, 195)),
        # Larrousse LH93: amarelo/escuro
        "Larrousse-LH93":    (LARROUSSE_YEL,  LARROUSSE_DARK),
        # Jordan 193: Sasol azul/vermelho
        "Jordan-193":        (JORDAN_BLUE,    JORDAN_RED),
        # Tyrrell YM21 (021): Braun roxo/teal
        "Tyrrel-YM21":       (BRAUN_DARK,     BRAUN_TEAL),
        # Lola T93/30: vermelho Ferrari
        "Lola-T93_30":       (LOLA_RED,       LOLA_WHITE),
    },
}


# ─── algoritmo de remapeamento ────────────────────────────────────────────────

def dist(a, b):
    return math.sqrt(sum((x-y)**2 for x,y in zip(a,b)))

def remap_image(cima, src_c1, src_c2, dst_c1, dst_c2):
    out = cima.copy(); pi = cima.load(); po = out.load()
    for y in range(cima.height):
        for x in range(cima.width):
            r, g, b, a = pi[x, y]
            if a == 0: continue
            br  = (r + g + b) / 3
            sat = max(r, g, b) - min(r, g, b)
            # Preservar pneus/chassis: baixa saturacao E brilho medio-escuro
            if sat < 25 and br < 130:
                continue
            d1 = dist((r,g,b), src_c1); d2 = dist((r,g,b), src_c2)
            t  = max(d1+d2, 1); w1, w2 = d2/t, d1/t
            tr = dst_c1[0]*w1 + dst_c2[0]*w2
            tg = dst_c1[1]*w1 + dst_c2[1]*w2
            tb = dst_c1[2]*w1 + dst_c2[2]*w2
            tbr = (tr+tg+tb)/3
            if tbr > 1:
                s = max(0.35, min(2.2, br/tbr))
                tr,tg,tb = min(255,tr*s),min(255,tg*s),min(255,tb*s)
            po[x,y] = (int(tr),int(tg),int(tb),a)
    return out


def read_car_props(path):
    cars = []
    with open(path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line: continue
            key, _, val = line.partition("=")
            p = val.split(",")
            c1 = (int(p[1]),int(p[2]),int(p[3]))
            c2 = (int(p[5]),int(p[6]),int(p[7]))
            cars.append((key.strip(), c1, c2))
    return cars


def process_season(season):
    liveries = LIVERIES.get(season)
    if not liveries:
        print(f"  {season}: sem liveries definidas"); return

    sheet_path = os.path.join(SPRITES, f"{season}.png")
    props_path = os.path.join(RESOURCES,"properties",season,"carros.properties")
    if not os.path.exists(sheet_path): print(f"  {season}: sheet nao encontrada"); return

    sheet = Image.open(sheet_path).convert("RGBA")
    cars  = read_car_props(props_path)
    print(f"\n[{season}] {len(cars)} carros")

    for idx, (key, prop_c1, prop_c2) in enumerate(cars):
        if key not in liveries:
            print(f"  [{idx:2d}] {key}: sem livery, pulando"); continue
        dst_c1, dst_c2 = liveries[key]

        cx   = idx * CIMA_W
        cima = sheet.crop((cx, ROW_CIMA, cx + CIMA_W, ROW_CIMA + CIMA_H))
        improved = remap_image(cima, prop_c1, prop_c2, dst_c1, dst_c2)
        sheet.paste(Image.new("RGBA",(CIMA_W,CIMA_H),(0,0,0,0)),(cx,ROW_CIMA))
        sheet.paste(improved,(cx,ROW_CIMA),improved)
        print(f"  [{idx:2d}] {key:28s}  C1:{dst_c1}  C2:{dst_c2}")

    sheet.save(sheet_path,"PNG")
    print(f"  -> salvo: {sheet_path}")


def main():
    targets = [s for s in sys.argv[1:] if s.startswith("t")] \
              if len(sys.argv)>1 else list(LIVERIES.keys())
    print(f"Aplicando liveries historicas: {targets}")
    for s in targets:
        process_season(s)
    print("\nConcluido!")

if __name__ == "__main__":
    main()
