#!/usr/bin/env python3
"""
Remove white background from t2026 side car and helmet images.

Uses per-row/per-column car boundaries to determine which white pixels
are background vs part of the car/helmet. This approach guarantees
that white elements inside the car/helmet shape are never removed.
"""
import os
from PIL import Image

BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), "src", "main", "resources")
CARRO_DIR = os.path.join(BASE, "carros", "t2026")
CAPACETE_DIR = os.path.join(BASE, "capacetes", "t2026")


def remove_bg_by_bounds(img, threshold=250, expand=2):
    """Remove white background using per-row and per-column boundaries.

    For each row, find leftmost and rightmost 'colored' pixel.
    For each column, find topmost and bottommost 'colored' pixel.
    Only remove white pixels outside these combined bounds.
    """
    pixels = img.load()
    w, h = img.size

    left_bound = [w] * h
    right_bound = [-1] * h
    top_bound = [h] * w
    bottom_bound = [-1] * w

    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a > 0 and (r < threshold or g < threshold or b < threshold):
                if x < left_bound[y]:
                    left_bound[y] = x
                if x > right_bound[y]:
                    right_bound[y] = x
                if y < top_bound[x]:
                    top_bound[x] = y
                if y > bottom_bound[x]:
                    bottom_bound[x] = y

    for y in range(h):
        if left_bound[y] < w:
            left_bound[y] = max(0, left_bound[y] - expand)
        if right_bound[y] >= 0:
            right_bound[y] = min(w - 1, right_bound[y] + expand)
    for x in range(w):
        if top_bound[x] < h:
            top_bound[x] = max(0, top_bound[x] - expand)
        if bottom_bound[x] >= 0:
            bottom_bound[x] = min(h - 1, bottom_bound[x] + expand)

    removed = 0
    for y in range(h):
        for x in range(w):
            in_row = left_bound[y] <= x <= right_bound[y]
            in_col = top_bound[x] <= y <= bottom_bound[x]
            if not (in_row and in_col):
                r, g, b, a = pixels[x, y]
                if a > 0 and r >= threshold and g >= threshold and b >= threshold:
                    pixels[x, y] = (255, 255, 255, 0)
                    removed += 1

    return img


def process_car_side():
    files = sorted(os.listdir(CARRO_DIR))
    for fname in files:
        if fname.endswith("_cima.png"):
            continue
        if not fname.endswith(".png"):
            continue
        path = os.path.join(CARRO_DIR, fname)
        img = Image.open(path).convert("RGBA")
        before = img.getpixel((0, 0))
        img = remove_bg_by_bounds(img)
        after = img.getpixel((0, 0))
        print(f"  {fname}: corner ({before}) -> ({after})")
        img.save(path)


def process_helmets():
    files = sorted(os.listdir(CAPACETE_DIR))
    for fname in files:
        if not fname.endswith(".png"):
            continue
        path = os.path.join(CAPACETE_DIR, fname)
        img = Image.open(path).convert("RGBA")
        before = img.getpixel((0, 0))
        img = remove_bg_by_bounds(img)
        after = img.getpixel((0, 0))
        print(f"  {fname}: corner ({before}) -> ({after})")
        img.save(path)


def main():
    print("Processing car side images (carros/t2026)...")
    process_car_side()
    print("\nProcessing helmet images (capacetes/t2026)...")
    process_helmets()
    print("\nDone!")


if __name__ == "__main__":
    main()
