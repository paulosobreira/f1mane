package br.f1mane.recursos;

import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

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

    public static BufferedImage gerarTemplate(int numEquipes) {
        int wingIdx = Math.max(numEquipes, 10);
        int sheetWidth = wingIdx * LADO_W;
        int sheetHeight = 240;

        BufferedImage img = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(1));
        g.setFont(new Font("SansSerif", Font.PLAIN, 10));

        int numHelmets = numEquipes * 2;

        // Helper: draw cell border + center cross for a region
        class Helper {
            void drawCell(int x, int y, int w, int h, String label) {
                g.setColor(Color.DARK_GRAY);
                g.drawRect(x, y, w, h);
                int cx = x + w / 2;
                int cy = y + h / 2;
                g.drawLine(cx - 8, cy, cx + 8, cy);
                g.drawLine(cx, cy - 8, cx, cy + 8);
                if (label != null) {
                    g.setColor(Color.GRAY);
                    g.drawString(label, x + 2, y + h - 4);
                }
            }
        }
        Helper hlp = new Helper();

        // Row 0: Carro LADO (180x40 each)
        for (int i = 0; i < numEquipes; i++) {
            hlp.drawCell(i * LADO_W, Y_LADO, LADO_W, LADO_H, "L" + (i + 1));
        }

        // Row 1: Carro CIMA (90x90 each)
        for (int i = 0; i < numEquipes; i++) {
            hlp.drawCell(i * CIMA_W, Y_CIMA, CIMA_W, CIMA_H, "C" + (i + 1));
        }
        // Wing overlay slot at index wingIdx
        g.setColor(Color.LIGHT_GRAY);
        g.drawRoundRect(wingIdx * CIMA_W + 4, Y_CIMA + 4, CIMA_W - 8, CIMA_H - 8, 8, 8);
        g.setColor(Color.GRAY);
        g.drawString("WING", wingIdx * CIMA_W + 20, Y_CIMA + CIMA_H / 2 + 4);

        // Row 2+3: Capacetes (55x55 each)
        for (int i = 0; i < numHelmets; i++) {
            int row = i / CAP_PER_ROW;
            int col = i % CAP_PER_ROW;
            int y = (row == 0) ? Y_CAP1 : Y_CAP2;
            hlp.drawCell(col * CAP_W, y, CAP_W, CAP_H, "H" + (i + 1));
        }

        g.dispose();
        return img;
    }

    public static void main(String[] args) throws IOException {
        int numEquipes = args.length > 0 ? Integer.parseInt(args[0]) : 11;
        BufferedImage template = gerarTemplate(numEquipes);
        String outDir = "src/main/resources/sprites";
        new File(outDir).mkdirs();
        File outFile = new File(outDir, "template_" + numEquipes + ".png");
        ImageIO.write(template, "png", outFile);
        System.out.println("Template gerado: " + outFile.getAbsolutePath() + " (" + template.getWidth() + "x" + template.getHeight() + ")");
    }
}
