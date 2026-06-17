package br.f1mane.recursos;

import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class SpriteSheet {

    public static final int LADO_W = 180;
    public static final int LADO_H = 40;
    public static final int CIMA_W = 90;
    public static final int CIMA_H = 90;
    public static final int CAP_W = 55;
    public static final int CAP_H = 55;
    public static final int CAP_PER_ROW = 12;
    public static final int Y_LADO = 0;
    public static final int Y_CIMA = 40;
    public static final int Y_CAP1 = 130;
    public static final int Y_CAP2 = 185;

    private static final Map<String, BufferedImage> cache = new HashMap<>();

    public static boolean isDisponivel(String temporada) {
        if (!cache.containsKey(temporada)) {
            cache.put(temporada, carregar(temporada));
        }
        return cache.get(temporada) != null;
    }

    private static BufferedImage carregar(String temporada) {
        try {
            BufferedImage img = ImageUtil.toBufferedImage(
                    "sprites/" + temporada + ".png");
            if (img != null) {
                return ImageUtil.toCompatibleImage(img);
            }
        } catch (Exception e) {
            Logger.logar("SpriteSheet: falha ao carregar sprites/"
                    + temporada + ".png - " + e.getMessage());
        }
        return null;
    }

    public static BufferedImage getCarroLado(String temporada, int idx) {
        BufferedImage sheet = cache.get(temporada);
        if (sheet == null) return null;
        try {
            return sheet.getSubimage(idx * LADO_W, Y_LADO, LADO_W, LADO_H);
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage getCarroCima(String temporada, int idx) {
        BufferedImage sheet = cache.get(temporada);
        if (sheet == null) return null;
        try {
            return sheet.getSubimage(idx * CIMA_W, Y_CIMA, CIMA_W, CIMA_H);
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage getWingOverlay(String temporada, int wingOverlayIdx) {
        BufferedImage sheet = cache.get(temporada);
        if (sheet == null) return null;
        try {
            return sheet.getSubimage(wingOverlayIdx * CIMA_W, Y_CIMA, CIMA_W, CIMA_H);
        } catch (Exception e) {
            return null;
        }
    }

    public static BufferedImage getWingOverlay(String temporada) {
        return getWingOverlay(temporada, 10);
    }

    public static BufferedImage getCapacete(String temporada, int idx) {
        BufferedImage sheet = cache.get(temporada);
        if (sheet == null) return null;
        try {
            int row = idx / CAP_PER_ROW;
            int col = idx % CAP_PER_ROW;
            int y = (row == 0) ? Y_CAP1 : Y_CAP2;
            return sheet.getSubimage(col * CAP_W, y, CAP_W, CAP_H);
        } catch (Exception e) {
            return null;
        }
    }
}
