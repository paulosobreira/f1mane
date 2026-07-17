#!/usr/bin/env python3
"""
Gera os ícones de HUD do jogo (pneus, clima, combustível, faróis de largada,
safety car de cima) em estilo flat, sem marcas registradas.

Pipeline (ver openspec/changes/icones-genericos-alta-resolucao/design.md):
  1. Desenha cada ícone em canvas supersampled (master × SS_FACTOR).
  2. Reduz com Lanczos para o master em alta resolução
     (utilitarios/icones_hires/ — fora do classpath, não entra no fat jar).
  3. Reduz o master com Lanczos para o tamanho de jogo exato em
     src/main/resources/png/ (contrato de dimensões abaixo).

Todos os PNGs são exportados em RGBA: carregaBufferedImageTransparecia()
copia as 4 bandas do raster e um PNG sem canal alfa ficaria invisível.

Orientação do safety car: nariz para a ESQUERDA, como os sprites de carro
de cima (linha CIMA do spritesheet). O conteúdo ocupa ~40×20 px do canvas
90×90 para manter a escala relativa aos carros de F1 (~54×23 px).
"""

import os
from PIL import Image, ImageDraw

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
REPO_ROOT = os.path.dirname(BASE_DIR)
PNG_DIR = os.path.join(REPO_ROOT, "src", "main", "resources", "png")
HIRES_DIR = os.path.join(BASE_DIR, "icones_hires")

# Fator do master em relação ao tamanho de jogo e supersampling do desenho.
MASTER_FACTOR = 8
SS_FACTOR = 4  # canvas de desenho = master × SS_FACTOR

# Contrato de dimensões embarcadas (ver IconesPngDimensoesTest).
GAME_SIZES = {
    "pneuMole.png": (142, 142),
    "pneuDuro.png": (142, 142),
    "pneuChuva.png": (142, 142),
    "pneuMoleMenor.png": (44, 44),
    "pneuDuroMenor.png": (44, 44),
    "pneuChuvaMenor.png": (44, 44),
    "sol.png": (35, 35),
    "lua.png": (35, 35),
    "nublado.png": (35, 35),
    "chuva.png": (35, 35),
    "fuel.png": (25, 25),
    "sfcima.png": (90, 90),
    "farois-apagados.png": (150, 89),
    "farois1.png": (150, 89),
    "farois2.png": (150, 89),
    "farois3.png": (150, 89),
    "farois4.png": (150, 89),
    "farois.png": (150, 89),
}

# Cores das faixas laterais (código de cores dos compostos usado no jogo).
COR_MOLE = (255, 200, 40)
COR_DURO = (235, 235, 235)
COR_CHUVA = (45, 110, 230)


def novo_canvas(game_size):
    w, h = game_size
    ss = (w * MASTER_FACTOR * SS_FACTOR, h * MASTER_FACTOR * SS_FACTOR)
    img = Image.new("RGBA", ss, (0, 0, 0, 0))
    return img, ImageDraw.Draw(img), ss


def salvar(img, nome, game_size, master_name=None):
    """Reduz o canvas supersampled para master e tamanho de jogo, e grava."""
    w, h = game_size
    master = img.resize((w * MASTER_FACTOR, h * MASTER_FACTOR), Image.LANCZOS)
    master.save(os.path.join(HIRES_DIR, master_name or nome))
    game = master.resize((w, h), Image.LANCZOS)
    game.save(os.path.join(PNG_DIR, nome))
    print("  %-24s master %dx%d  jogo %dx%d" %
          (nome, master.width, master.height, w, h))


def ellipse_c(draw, cx, cy, r, **kw):
    draw.ellipse([cx - r, cy - r, cx + r, cy + r], **kw)


# ─── pneus ────────────────────────────────────────────────────────────────────

def desenha_pneu(cor_faixa):
    """Pneu genérico visto de frente: borracha preta, faixa lateral colorida
    com marcações neutras (segmentos, sem texto), aro cinza com raios."""
    img, draw, (S, _) = novo_canvas(GAME_SIZES["pneuMole.png"])
    c = S / 2.0

    # borracha
    ellipse_c(draw, c, c, 0.492 * S, fill=(20, 21, 23, 255))
    # banda de rodagem levemente mais escura na borda externa
    ellipse_c(draw, c, c, 0.492 * S, outline=(10, 10, 11, 255),
              width=int(0.012 * S))

    # faixa lateral colorida (anel) com 4 segmentos de intervalo neutros
    rb, hw = 0.415 * S, 0.040 * S
    ellipse_c(draw, c, c, rb + hw, fill=cor_faixa + (255,))
    ellipse_c(draw, c, c, rb - hw, fill=(20, 21, 23, 255))
    box = [c - (rb + hw), c - (rb + hw), c + (rb + hw), c + (rb + hw)]
    for ang in (45, 135, 225, 315):
        draw.arc(box, ang - 7, ang + 7, fill=(20, 21, 23, 255),
                 width=int(2.4 * hw))

    # borda do aro
    ellipse_c(draw, c, c, 0.318 * S, fill=(186, 191, 199, 255))
    ellipse_c(draw, c, c, 0.298 * S, fill=(52, 55, 61, 255))

    # raios
    import math
    for i in range(10):
        ang = math.radians(i * 36)
        x1 = c + 0.075 * S * math.cos(ang)
        y1 = c + 0.075 * S * math.sin(ang)
        x2 = c + 0.288 * S * math.cos(ang)
        y2 = c + 0.288 * S * math.sin(ang)
        draw.line([x1, y1, x2, y2], fill=(196, 201, 208, 255),
                  width=int(0.042 * S))

    # cubo central
    ellipse_c(draw, c, c, 0.085 * S, fill=(213, 217, 222, 255))
    ellipse_c(draw, c, c, 0.030 * S, fill=(42, 44, 48, 255))
    return img


def gerar_pneus():
    for nome, cor in (("pneuMole", COR_MOLE), ("pneuDuro", COR_DURO),
                      ("pneuChuva", COR_CHUVA)):
        img = desenha_pneu(cor)
        salvar(img, nome + ".png", GAME_SIZES[nome + ".png"])
        # versão menor derivada do MESMO master (Lanczos direto do canvas)
        w, h = GAME_SIZES[nome + "Menor.png"]
        master = img.resize((w * MASTER_FACTOR, h * MASTER_FACTOR),
                            Image.LANCZOS)
        master.save(os.path.join(HIRES_DIR, nome + "Menor.png"))
        master.resize((w, h), Image.LANCZOS).save(
            os.path.join(PNG_DIR, nome + "Menor.png"))
        print("  %-24s master %dx%d  jogo %dx%d" %
              (nome + "Menor.png", master.width, master.height, w, h))


# ─── clima ────────────────────────────────────────────────────────────────────

def gerar_sol():
    import math
    img, draw, (S, _) = novo_canvas(GAME_SIZES["sol.png"])
    c = S / 2.0
    # raios (8 triângulos)
    for i in range(8):
        ang = math.radians(i * 45)
        tip_x = c + 0.475 * S * math.cos(ang)
        tip_y = c + 0.475 * S * math.sin(ang)
        b1 = ang + math.radians(11)
        b2 = ang - math.radians(11)
        draw.polygon([
            (tip_x, tip_y),
            (c + 0.30 * S * math.cos(b1), c + 0.30 * S * math.sin(b1)),
            (c + 0.30 * S * math.cos(b2), c + 0.30 * S * math.sin(b2)),
        ], fill=(255, 176, 32, 255))
    # disco
    ellipse_c(draw, c, c, 0.315 * S, fill=(255, 201, 61, 255))
    ellipse_c(draw, c, c, 0.315 * S, outline=(240, 160, 24, 255),
              width=int(0.02 * S))
    return img


def gerar_lua():
    img, draw, (S, _) = novo_canvas(GAME_SIZES["lua.png"])
    # disco cheio claro
    ellipse_c(draw, 0.52 * S, 0.50 * S, 0.42 * S, fill=(232, 236, 244, 255))
    # recorte do crescente (círculo deslocado vira transparência)
    punch = Image.new("L", img.size, 0)
    pd = ImageDraw.Draw(punch)
    ellipse_c(pd, 0.30 * S, 0.40 * S, 0.38 * S, fill=255)
    img.paste((0, 0, 0, 0), (0, 0), punch)
    # crateras sutis na parte iluminada
    d = ImageDraw.Draw(img)
    ellipse_c(d, 0.64 * S, 0.36 * S, 0.045 * S, fill=(201, 207, 218, 255))
    ellipse_c(d, 0.72 * S, 0.58 * S, 0.060 * S, fill=(201, 207, 218, 255))
    ellipse_c(d, 0.56 * S, 0.74 * S, 0.040 * S, fill=(201, 207, 218, 255))
    return img


def desenha_nuvem(draw, cx, cy, escala, S, cor, cor_base):
    """Nuvem flat: três lóbulos + base achatada."""
    ellipse_c(draw, cx - 0.16 * S * escala, cy + 0.02 * S * escala,
              0.115 * S * escala, fill=cor)
    ellipse_c(draw, cx + 0.15 * S * escala, cy + 0.02 * S * escala,
              0.105 * S * escala, fill=cor)
    ellipse_c(draw, cx, cy - 0.06 * S * escala, 0.15 * S * escala, fill=cor)
    draw.rounded_rectangle(
        [cx - 0.255 * S * escala, cy - 0.01 * S * escala,
         cx + 0.245 * S * escala, cy + 0.115 * S * escala],
        radius=0.06 * S * escala, fill=cor_base)


def gerar_nublado():
    import math
    img, draw, (S, _) = novo_canvas(GAME_SIZES["nublado.png"])
    # sol parcial atrás (canto superior esquerdo)
    sc_x, sc_y = 0.34 * S, 0.32 * S
    for i in range(8):
        ang = math.radians(i * 45)
        tip_x = sc_x + 0.30 * S * math.cos(ang)
        tip_y = sc_y + 0.30 * S * math.sin(ang)
        b1, b2 = ang + math.radians(12), ang - math.radians(12)
        draw.polygon([
            (tip_x, tip_y),
            (sc_x + 0.19 * S * math.cos(b1), sc_y + 0.19 * S * math.sin(b1)),
            (sc_x + 0.19 * S * math.cos(b2), sc_y + 0.19 * S * math.sin(b2)),
        ], fill=(255, 176, 32, 255))
    ellipse_c(draw, sc_x, sc_y, 0.20 * S, fill=(255, 201, 61, 255))
    # nuvem na frente (centro-baixo-direita)
    desenha_nuvem(draw, 0.56 * S, 0.60 * S, 1.55, S,
                  (226, 230, 237, 255), (198, 204, 214, 255))
    return img


def gerar_chuva():
    img, draw, (S, _) = novo_canvas(GAME_SIZES["chuva.png"])
    desenha_nuvem(draw, 0.50 * S, 0.34 * S, 1.5, S,
                  (159, 168, 180, 255), (128, 138, 152, 255))
    # gotas
    for i, gx in enumerate((0.30, 0.52, 0.74)):
        gy = 0.70 * S + (0.05 * S if i == 1 else 0)
        r = 0.055 * S
        draw.polygon([(gx * S, gy - 2.2 * r),
                      (gx * S - 0.95 * r, gy - 0.3 * r),
                      (gx * S + 0.95 * r, gy - 0.3 * r)],
                     fill=(62, 134, 232, 255))
        ellipse_c(draw, gx * S, gy, r, fill=(62, 134, 232, 255))
    return img


# ─── combustível ──────────────────────────────────────────────────────────────

def gerar_fuel():
    """Bomba de combustível flat, exclusivamente em tons de cinza."""
    img, draw, (S, _) = novo_canvas(GAME_SIZES["fuel.png"])
    # base
    draw.rounded_rectangle([0.10 * S, 0.88 * S, 0.70 * S, 0.98 * S],
                           radius=0.03 * S, fill=(64, 64, 64, 255))
    # corpo
    draw.rounded_rectangle([0.16 * S, 0.10 * S, 0.64 * S, 0.90 * S],
                           radius=0.07 * S, fill=(106, 106, 106, 255))
    # visor
    draw.rounded_rectangle([0.23 * S, 0.18 * S, 0.57 * S, 0.42 * S],
                           radius=0.04 * S, fill=(222, 222, 222, 255))
    # mangueira: sobe pela lateral direita e termina no bocal
    draw.line([0.64 * S, 0.62 * S, 0.80 * S, 0.62 * S], fill=(58, 58, 58, 255),
              width=int(0.055 * S))
    draw.line([0.80 * S, 0.62 * S, 0.80 * S, 0.28 * S], fill=(58, 58, 58, 255),
              width=int(0.055 * S))
    # bocal
    draw.rounded_rectangle([0.73 * S, 0.14 * S, 0.87 * S, 0.30 * S],
                           radius=0.02 * S, fill=(44, 44, 44, 255))
    return img


# ─── safety car (vista de cima, nariz para a esquerda) ───────────────────────

def gerar_sfcima():
    img, draw, (S, _) = novo_canvas(GAME_SIZES["sfcima.png"])
    # conteúdo ~40×19 px no canvas de 90 px (mesma escala do sprite antigo,
    # 37×19, e proporção de carro real): x 0.28–0.72, y 0.42–0.58
    x0, x1 = 0.28 * S, 0.72 * S
    y0, y1 = 0.42 * S, 0.58 * S
    cy = (y0 + y1) / 2

    # rodas (levemente para fora do corpo)
    rw, rh = 0.062 * S, 0.020 * S
    for wx in (0.36, 0.635):
        for wy in (y0 + 0.002 * S, y1 - 0.002 * S):
            draw.rounded_rectangle(
                [wx * S - rw / 2, wy - rh, wx * S + rw / 2, wy + rh],
                radius=rh * 0.8, fill=(16, 17, 19, 255))

    # corpo com nariz afilado (frente à esquerda)
    taper = 0.030 * S
    draw.rounded_rectangle([x0, y0, x1, y1], radius=0.045 * S,
                           fill=(233, 235, 238, 255),
                           outline=(51, 54, 59, 255), width=int(0.005 * S))
    # afila o capô: recorta cantos dianteiros em diagonal
    for top in (True, False):
        ys = y0 if top else y1
        yd = y0 + taper if top else y1 - taper
        draw.polygon([(x0 - 0.004 * S, ys), (x0 + 0.085 * S, ys),
                      (x0 - 0.004 * S, yd)], fill=(0, 0, 0, 0))
    draw.line([(x0 + 0.085 * S, y0), (x0, y0 + taper)],
              fill=(51, 54, 59, 255), width=int(0.005 * S))
    draw.line([(x0 + 0.085 * S, y1), (x0, y1 - taper)],
              fill=(51, 54, 59, 255), width=int(0.005 * S))

    # retrovisores
    for my in (y0 - 0.006 * S, y1 + 0.006 * S):
        ellipse_c(draw, 0.44 * S, my, 0.010 * S, fill=(51, 54, 59, 255))

    # para-brisa (trapezio, mais largo no teto)
    draw.polygon([(0.42 * S, y0 + 0.012 * S), (0.47 * S, y0 + 0.006 * S),
                  (0.47 * S, y1 - 0.006 * S), (0.42 * S, y1 - 0.012 * S)],
                 fill=(46, 50, 55, 255))
    # teto
    draw.rectangle([0.47 * S, y0 + 0.006 * S, 0.61 * S, y1 - 0.006 * S],
                   fill=(216, 220, 226, 255))
    # barra de luz laranja no teto (atravessa a largura)
    draw.rounded_rectangle([0.488 * S, y0 - 0.003 * S,
                            0.512 * S, y1 + 0.003 * S],
                           radius=0.006 * S, fill=(255, 158, 27, 255))
    # vidro traseiro
    draw.polygon([(0.61 * S, y0 + 0.006 * S), (0.645 * S, y0 + 0.012 * S),
                  (0.645 * S, y1 - 0.012 * S), (0.61 * S, y1 - 0.006 * S)],
                 fill=(46, 50, 55, 255))
    # faróis dianteiros e lanternas traseiras
    for hy in (y0 + 0.022 * S, y1 - 0.022 * S):
        ellipse_c(draw, 0.302 * S, hy, 0.009 * S, fill=(140, 145, 152, 255))
        ellipse_c(draw, 0.706 * S, hy, 0.008 * S, fill=(176, 48, 48, 255))
    return img


# ─── faróis de largada ────────────────────────────────────────────────────────

def desenha_farois(colunas_acesas):
    """Painel flat: 5 colunas × 4 luzes; nas colunas acesas, as 2 de baixo
    ficam vermelhas. Mesma geometria em todos os estados."""
    img, draw, (W, H) = novo_canvas(GAME_SIZES["farois.png"])
    n_col, col_w, gap = 5, 0.16 * W, 0.04 * W
    total = n_col * col_w + (n_col - 1) * gap
    x = (W - total) / 2
    r_luz = 0.105 * H
    centros_y = [0.16 * H, 0.387 * H, 0.613 * H, 0.84 * H]

    for col in range(n_col):
        draw.rounded_rectangle([x, 0.01 * H, x + col_w, 0.99 * H],
                               radius=0.10 * H, fill=(32, 34, 38, 255),
                               outline=(16, 17, 19, 255), width=int(0.012 * H))
        cx = x + col_w / 2
        for i, cy in enumerate(centros_y):
            acesa = col < colunas_acesas and i >= 2
            if acesa:
                ellipse_c(draw, cx, cy, r_luz, fill=(240, 58, 40, 255))
                ellipse_c(draw, cx, cy, r_luz,
                          outline=(168, 30, 18, 255), width=int(0.018 * H))
            else:
                ellipse_c(draw, cx, cy, r_luz, fill=(49, 52, 57, 255))
                ellipse_c(draw, cx, cy, r_luz,
                          outline=(19, 20, 23, 255), width=int(0.018 * H))
        x += col_w + gap
    return img


def gerar_farois():
    estados = {"farois-apagados.png": 0, "farois1.png": 1, "farois2.png": 2,
               "farois3.png": 3, "farois4.png": 4, "farois.png": 5}
    for nome, acesas in estados.items():
        salvar(desenha_farois(acesas), nome, GAME_SIZES[nome])


# ─── main ─────────────────────────────────────────────────────────────────────

def main():
    os.makedirs(HIRES_DIR, exist_ok=True)
    print("Gerando pneus...")
    gerar_pneus()
    print("Gerando clima...")
    salvar(gerar_sol(), "sol.png", GAME_SIZES["sol.png"])
    salvar(gerar_lua(), "lua.png", GAME_SIZES["lua.png"])
    salvar(gerar_nublado(), "nublado.png", GAME_SIZES["nublado.png"])
    salvar(gerar_chuva(), "chuva.png", GAME_SIZES["chuva.png"])
    print("Gerando fuel...")
    salvar(gerar_fuel(), "fuel.png", GAME_SIZES["fuel.png"])
    print("Gerando safety car...")
    salvar(gerar_sfcima(), "sfcima.png", GAME_SIZES["sfcima.png"])
    print("Gerando farois...")
    gerar_farois()
    print("OK.")


if __name__ == "__main__":
    main()
