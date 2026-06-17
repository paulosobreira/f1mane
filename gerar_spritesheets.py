#!/usr/bin/env python3
"""
Regenerates all spritesheets following FILE ORDER from carros.properties and pilotos.properties.
Uses the individual PNG images from carros/{season}/ and capacetes/{season}/ when available.
Falls back to template compositing (CarroLado.png + tints) only when no individual PNG exists.
"""

import os
import sys
from PIL import Image

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RESOURCES = os.path.join(BASE_DIR, "src", "main", "resources")
PROPERTIES = os.path.join(RESOURCES, "properties")
SPRITES = os.path.join(RESOURCES, "sprites")
PNG = os.path.join(RESOURCES, "png")


def read_properties_in_order(filepath):
    """Read .properties file preserving line order."""
    entries = []
    with open(filepath, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#"):
                continue
            if "=" not in line:
                continue
            key, _, value = line.partition("=")
            entries.append((key.strip(), value.strip()))
    return entries


def tint_image(mask_path, target_color, output_size=None):
    """Replicate gerarCoresCarros: average mask pixel with target color."""
    mask = Image.open(mask_path).convert("RGBA")
    if output_size and mask.size != output_size:
        mask = mask.resize(output_size, Image.NEAREST)
    result = Image.new("RGBA", mask.size, (0, 0, 0, 0))
    for x in range(mask.width):
        for y in range(mask.height):
            r, g, b, a = mask.getpixel((x, y))
            if a > 0:
                nr = (r + target_color[0]) // 2
                ng = (g + target_color[1]) // 2
                nb = (b + target_color[2]) // 2
                result.putpixel((x, y), (nr, ng, nb, a))
    return result


def composite_layer(base, overlay):
    """SRC_OVER alpha compositing: overlay drawn on top of base."""
    result = base.copy()
    for x in range(min(base.width, overlay.width)):
        for y in range(min(base.height, overlay.height)):
            sr, sg, sb, sa = overlay.getpixel((x, y))
            if sa == 0:
                continue
            dr, dg, db, da = result.getpixel((x, y))
            if sa == 255:
                result.putpixel((x, y), (sr, sg, sb, sa))
            else:
                alpha = sa / 255.0
                nr = int(sr * alpha + dr * (1 - alpha))
                ng = int(sg * alpha + dg * (1 - alpha))
                nb = int(sb * alpha + db * (1 - alpha))
                na = int(sa + da * (1 - alpha))
                if na > 255:
                    na = 255
                result.putpixel((x, y), (nr, ng, nb, na))
    return result


def generate_carro_lado_fallback(cor1, cor2, output_size=(180, 40)):
    """Fallback: composite base + C1(cor1) + C2(cor2)."""
    base = Image.open(os.path.join(PNG, "CarroLado.png")).convert("RGBA")
    base = base.resize(output_size, Image.NEAREST)
    c1 = tint_image(os.path.join(PNG, "CarroLadoC1.png"), cor1, output_size)
    c2 = tint_image(os.path.join(PNG, "CarroLadoC2.png"), cor2, output_size)
    result = composite_layer(base, c1)
    result = composite_layer(result, c2)
    return result


def generate_carro_cima_fallback(cor1, cor2, modelo, output_size=(90, 90)):
    """Fallback: composite model_base + C1(cor1) + C2(cor2)."""
    model_dir = os.path.join(PNG, modelo)
    base = Image.open(os.path.join(model_dir, "CarroCima.png")).convert("RGBA")
    if base.size != output_size:
        base = base.resize(output_size, Image.NEAREST)
    c1 = tint_image(os.path.join(model_dir, "CarroCimaC1.png"), cor1, output_size)
    c2 = tint_image(os.path.join(model_dir, "CarroCimaC2.png"), cor2, output_size)
    result = composite_layer(base, c1)
    result = composite_layer(result, c2)
    return result


def generate_wing_overlay(modelo, output_size=(90, 90)):
    """Generate wing-only overlay: white pixels only where the aero foil is.
    The wing is the difference between C2 (includes wing) and C3 (excludes wing).
    """
    model_dir = os.path.join(PNG, modelo)
    c2 = Image.open(os.path.join(model_dir, "CarroCimaC2.png")).convert("RGBA")
    c3 = Image.open(os.path.join(model_dir, "CarroCimaC3.png")).convert("RGBA")
    if c2.size != output_size:
        c2 = c2.resize(output_size, Image.NEAREST)
    if c3.size != output_size:
        c3 = c3.resize(output_size, Image.NEAREST)
    result = Image.new("RGBA", output_size, (0, 0, 0, 0))
    for x in range(output_size[0]):
        for y in range(output_size[1]):
            _, _, _, a2 = c2.getpixel((x, y))
            _, _, _, a3 = c3.getpixel((x, y))
            if a2 > 0 and a3 == 0:
                result.putpixel((x, y), (255, 255, 255, 255))
    return result


def generate_capacete_fallback(cor1, cor2, output_size=(55, 55)):
    """Fallback: composite Capacete.png + C1(cor1) + C2(cor2)."""
    base = Image.open(os.path.join(PNG, "Capacete.png")).convert("RGBA")
    base = base.resize(output_size, Image.NEAREST)
    c1 = tint_image(os.path.join(PNG, "CapaceteC1.png"), cor1, output_size)
    c2 = tint_image(os.path.join(PNG, "CapaceteC2.png"), cor2, output_size)
    result = composite_layer(base, c1)
    result = composite_layer(result, c2)
    return result


def load_carro_lado(season, img_field, cor1, cor2, output_size=(180, 40)):
    """
    Load side car image from individual PNG in carros/{season}/{img_field}.
    Falls back to template compositing if PNG not found.
    """
    img_path = os.path.join(RESOURCES, "carros", season, img_field)
    if os.path.exists(img_path):
        img = Image.open(img_path).convert("RGBA")
        if img.size != output_size:
            img = img.resize(output_size, Image.NEAREST)
        return img
    return generate_carro_lado_fallback(cor1, cor2, output_size)


def load_carro_cima(season, img_field, cor1, cor2, modelo, output_size=(90, 90)):
    """
    Load top car image from individual PNG in carros/{season}/{name}_cima.png.
    Falls back to template compositing if PNG not found.
    """
    base_name = img_field.rsplit(".", 1)[0]
    cima_name = base_name + "_cima.png"
    cima_path = os.path.join(RESOURCES, "carros", season, cima_name)
    if os.path.exists(cima_path):
        img = Image.open(cima_path).convert("RGBA")
        if img.size != output_size:
            img = img.resize(output_size, Image.NEAREST)
        return img
    return generate_carro_cima_fallback(cor1, cor2, modelo, output_size)


def load_capacete(season, pilot_key, cor1, cor2, output_size=(55, 55)):
    """
    Load helmet image from individual PNG in capacetes/{season}/{pilot_key}.png.
    Falls back to template compositing if PNG not found.
    """
    helmet_path = os.path.join(RESOURCES, "capacetes", season, pilot_key + ".png")
    if os.path.exists(helmet_path):
        img = Image.open(helmet_path).convert("RGBA")
        if img.size != output_size:
            # Create output-sized canvas and paste centered at top
            canvas = Image.new("RGBA", output_size, (0, 0, 0, 0))
            canvas.paste(img, (0, 0), img)
            return canvas
        return img
    return generate_capacete_fallback(cor1, cor2, output_size)


def get_modelo(temporada):
    """Replicate obterModeloCarroCima."""
    ano = int(temporada.replace("t", ""))
    if ano <= 1980:
        return "cima19701979"
    if ano < 1997:
        return "cima19801997"
    if ano < 2009:
        return "cima19982008"
    if ano < 2017:
        return "cima20092016"
    return "cima2017"


def generate_spritesheet(temporada):
    """Generate a complete spritesheet for a given season."""
    props_dir = os.path.join(PROPERTIES, temporada)
    carros_file = os.path.join(props_dir, "carros.properties")
    pilotos_file = os.path.join(props_dir, "pilotos.properties")

    if not os.path.exists(carros_file) or not os.path.exists(pilotos_file):
        print(f"  SKIP: missing properties for {temporada}")
        return

    # Read in FILE ORDER
    carros = read_properties_in_order(carros_file)
    pilotos = read_properties_in_order(pilotos_file)

    num_cars = len(carros)
    num_pilots = len(pilotos)

    # Parse car data
    parsed_cars = []
    for key, value in carros:
        parts = value.split(",")
        cor1 = (int(parts[1]), int(parts[2]), int(parts[3]))
        cor2 = (int(parts[5]), int(parts[6]), int(parts[7]))
        img_field = parts[4].split(";")[0]
        parsed_cars.append((key, cor1, cor2, img_field))

    # Parse pilot data
    parsed_pilots = []
    for key, value in pilotos:
        parts = value.split(",")
        car_name = parts[0]
        parsed_pilots.append((key, car_name))

    # Match pilots to cars (by name)
    pilot_car_colors = []
    for pkey, pcar_name in parsed_pilots:
        found = False
        for ckey, ccor1, ccor2, cimg in parsed_cars:
            if pcar_name == ckey:
                pilot_car_colors.append((pkey, ccor1, ccor2))
                found = True
                break
        if not found:
            print(f"  WARN: pilot {pkey} car '{pcar_name}' not found in carros")
            pilot_car_colors.append((pkey, (128, 128, 128), (64, 64, 64)))

    modelo = get_modelo(temporada)

    # Sprite sheet dimensions
    lado_w, lado_h = 180, 40
    cima_w, cima_h = 90, 90
    cap_w, cap_h = 55, 55
    cap_per_row = 12

    no_wing_idx = max(num_cars, 10) if num_cars > 10 else 10
    sheet_width = max(num_cars, 10) * 180
    sheet_height = 240

    print(f"  {temporada}: {num_cars} cars, {num_pilots} pilots, {sheet_width}x{sheet_height}")

    sheet = Image.new("RGBA", (sheet_width, sheet_height), (0, 0, 0, 0))

    # Row 0: Carros LADO
    for idx, (_, cor1, cor2, img_field) in enumerate(parsed_cars):
        img = load_carro_lado(temporada, img_field, cor1, cor2, (lado_w, lado_h))
        sheet.paste(img, (idx * lado_w, 0), img)

    # Row 1: Carros CIMA + WING OVERLAY
    for idx, (_, cor1, cor2, img_field) in enumerate(parsed_cars):
        img = load_carro_cima(temporada, img_field, cor1, cor2, modelo, (cima_w, cima_h))
        sheet.paste(img, (idx * cima_w, 40), img)

    wing_overlay = generate_wing_overlay(modelo, (cima_w, cima_h))
    sheet.paste(wing_overlay, (no_wing_idx * cima_w, 40), wing_overlay)

    # Row 2-3: Capacetes
    for idx, (pkey, cor1, cor2) in enumerate(pilot_car_colors):
        pilot_key = pkey.replace(".", "")
        img = load_capacete(temporada, pilot_key, cor1, cor2, (cap_w, cap_h))
        row = idx // cap_per_row
        col = idx % cap_per_row
        y = 130 if row == 0 else 185
        sheet.paste(img, (col * cap_w, y), img)

    out_dir = SPRITES
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, f"{temporada}.png")
    sheet.save(out_path, "PNG")


def main():
    seasons = sorted([d for d in os.listdir(PROPERTIES) if d.startswith("t") and os.path.isdir(os.path.join(PROPERTIES, d))])
    print(f"Regenerating {len(seasons)} spritesheets using individual PNG images...\n")
    for s in seasons:
        generate_spritesheet(s)
    print("\nDone!")


if __name__ == "__main__":
    main()
