package br.nnpe;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import sowbreira.f1mane.recursos.CarregadorRecursos;

/**
 * @author Paulo Sobreira Criado Em 21/08/2005
 */
public class ImageUtil {

	public static BufferedImage subImagem(BufferedImage buffer, int x, int y,
			int largura, int altura) {
		if (buffer == null) {
			return null;
		}
		BufferedImage bufferedImageRetorno = new BufferedImage(largura, altura,
				buffer.getType());
		Raster srcRaster = buffer.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];
		for (int i = x; i < (x + largura); i++) {
			for (int j = y; j < (y + altura); j++) {
				destRaster.setPixel(i - x, j - y,
						srcRaster.getPixel(i, j, argbArray));
			}
		}
		return bufferedImageRetorno;
	}

	public static BufferedImage copiaImagem(BufferedImage buffer) {
		if (buffer == null) {
			return null;
		}
		ImageIcon img = new ImageIcon(buffer);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);
				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);
				destRaster.setPixel(i, j, argbArray);
			}
		}

		return bufferedImageRetorno;
	}

	public static BufferedImage gerarFade(BufferedImage src, int translucidez) {
		ImageIcon img = new ImageIcon(src);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);
		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);
				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);
				if (argbArray[3] != 0)
					argbArray[3] = translucidez;
				destRaster.setPixel(i, j, argbArray);
			}
		}

		return bufferedImageRetorno;
	}

	public static BufferedImage geraResize(BufferedImage src, double fator) {
		return geraResize(src, fator, fator);
	}

	public static BufferedImage geraResize(BufferedImage src, double fatorx,
			double fatory) {
		if (src == null) {
			return null;
		}
		AffineTransform afZoom = new AffineTransform();
		afZoom.setToScale(fatorx, fatory);
		BufferedImage dst = new BufferedImage((int) Math.round(src.getWidth()
				* fatorx), (int) Math.round(src.getHeight() * fatory),
				BufferedImage.TYPE_INT_ARGB);
		AffineTransformOp op = new AffineTransformOp(afZoom,
				AffineTransformOp.TYPE_BILINEAR);
		op.filter(src, dst);
		return dst;
	}

	public static BufferedImage geraTransparencia(BufferedImage src, Color color) {
		ImageIcon img = new ImageIcon(src);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);

				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);

				if (color.equals(c)) {
					argbArray[3] = 0;
				}

				destRaster.setPixel(i, j, argbArray);
			}
		}

		return bufferedImageRetorno;
	}

	public static BufferedImage geraTransparencia(BufferedImage src,
			Color color, int translucidez) {
		ImageIcon img = new ImageIcon(src);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);

				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);

				if (color.equals(c)) {
					argbArray[3] = 0;
				} else {
					argbArray[3] = translucidez;
				}

				destRaster.setPixel(i, j, argbArray);
			}
		}

		return bufferedImageRetorno;
	}

	// This method returns a buffered image with the contents of an image
	public static BufferedImage toBufferedImage(String image) {

		try {
			return ImageIO.read(CarregadorRecursos.class.getResource(image));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Serve pra nada essa porra!!!
	 * 
	 * @param image
	 * @return
	 */
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;

			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);

		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();

		return cm.hasAlpha();
	}

	public static BufferedImage geraTransparencia(BufferedImage src) {
		return geraTransparencia(src, 250);
	}

	public static BufferedImage geraTransparencia(BufferedImage src, int ingVal) {
		return geraTransparencia(src, ingVal, 255);
	}

	public static BufferedImage geraTransparencia(BufferedImage src,
			int ingVal, int translucidez) {
		ImageIcon img = new ImageIcon(src);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);

				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);
				if (c.getRed() > ingVal && c.getGreen() > ingVal
						&& c.getBlue() > ingVal) {
					argbArray[3] = 0;
				} else {
					argbArray[3] = translucidez;
				}

				destRaster.setPixel(i, j, argbArray);
			}
		}

		return bufferedImageRetorno;
	}

}
