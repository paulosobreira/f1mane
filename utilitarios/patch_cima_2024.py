#!/usr/bin/env python3
"""
Substitui apenas a linha de carros vistos de cima (Y=40, altura=90px)
no spritesheet t2024.png, usando os _cima.png gerados em carros/t2024/.
Preserva intactas as linhas de visao lateral e capacetes.
"""

import os
from PIL import Image

BASE_DIR  = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
SPRITES   = os.path.join(RESOURCES, "sprites")
CARROS    = os.path.join(RESOURCES, "carros", "t2024")
PROPS     = os.path.join(RESOURCES, "properties", "t2024", "carros.properties")
SHEET     = os.path.join(SPRITES, "t2024.png")

CIMA_W = 90
CIMA_H = 90
ROW_Y  = 40   # posicao Y da linha "cima" no spritesheet


def read_car_order(props_path):
    """Le carros.properties e retorna (chave, img_field) na ordem do arquivo."""
    cars = []
    with open(props_path, encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, _, val = line.partition("=")
            parts = val.split(",")
            img_field = parts[4].split(";")[0]          # ex: mclaren.png
            base_name = img_field.rsplit(".", 1)[0]     # ex: mclaren
            cars.append((key.strip(), base_name))
    return cars


def main():
    cars = read_car_order(PROPS)
    sheet = Image.open(SHEET).convert("RGBA")

    print(f"Spritesheet: {SHEET}  ({sheet.width}x{sheet.height})")
    print(f"Substituindo linha cima (Y={ROW_Y}) para {len(cars)} equipes:\n")

    replaced = 0
    for idx, (car_key, base_name) in enumerate(cars):
        cima_path = os.path.join(CARROS, f"{base_name}_cima.png")
        if not os.path.exists(cima_path):
            print(f"  [{idx}] {car_key}: sem _cima.png em t2024, pulando")
            continue

        cima = Image.open(cima_path).convert("RGBA")
        if cima.size != (CIMA_W, CIMA_H):
            cima = cima.resize((CIMA_W, CIMA_H), Image.NEAREST)

        x = idx * CIMA_W
        y = ROW_Y

        # Apaga a area antiga
        blank = Image.new("RGBA", (CIMA_W, CIMA_H), (0, 0, 0, 0))
        sheet.paste(blank, (x, y))

        # Cola o novo sprite
        sheet.paste(cima, (x, y), cima)
        print(f"  [{idx}] {car_key:30s} -> colado em ({x}, {y})")
        replaced += 1

    # Colar tambem o nowing overlay na posicao reservada (indice 10)
    nowing_path = os.path.join(CARROS, "nowing_cima.png")
    if os.path.exists(nowing_path):
        nowing = Image.open(nowing_path).convert("RGBA")
        if nowing.size != (CIMA_W, CIMA_H):
            nowing = nowing.resize((CIMA_W, CIMA_H), Image.NEAREST)
        no_wing_x = max(len(cars), 10) * CIMA_W
        sheet.paste(nowing, (no_wing_x, ROW_Y), nowing)
        print(f"\n  nowing_cima colado em ({no_wing_x}, {ROW_Y})")

    sheet.save(SHEET, "PNG")
    print(f"\n{replaced}/{len(cars)} sprites substituidos. Salvo: {SHEET}")


if __name__ == "__main__":
    main()
