package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class ObjetoPneus extends ObjetoPista {

	RoundRectangle2D externo;

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		externo = new RoundRectangle2D.Double(getPosicaoQuina().x,
				getPosicaoQuina().y, largura * 10, altura * 10, 15, 15);
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		GeneralPath generalPath = new GeneralPath();
		for (int i = 0; i < largura; i++) {
			if (i > 100) {
				continue;
			}
			for (int j = 0; j < altura; j++) {
				if (j > 50) {
					continue;
				}
				Ellipse2D ellipse2d = new Ellipse2D.Double(
						(getPosicaoQuina().x + (10 * i)),
						(getPosicaoQuina().y + (10 * j)), 10, 10);
				generalPath.append(ellipse2d, true);

			}
		}
		affineTransform.setToRotation(rad,
				generalPath.getBounds().getCenterX(), generalPath.getBounds()
						.getCenterY());
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(generalPath.createTransformedShape(affineTransform));

		generalPath = new GeneralPath();

		for (int i = 0; i < largura; i++) {
			if (i > 100) {
				continue;
			}
			for (int j = 0; j < altura; j++) {
				if (j > 50) {
					continue;
				}
				Ellipse2D ellipse2d = new Ellipse2D.Double((getPosicaoQuina().x
						+ (10 * i) + 2), (getPosicaoQuina().y + (10 * j) + 2),
						6, 6);
				generalPath.append(ellipse2d, true);

			}
		}
		affineTransform.setToRotation(rad,
				generalPath.getBounds().getCenterX(), generalPath.getBounds()
						.getCenterY());
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria()
				.getGreen(), getCorSecundaria().getBlue(), getTransparencia()));
		g2d.fill(generalPath.createTransformedShape(affineTransform));

	}

	@Override
	public Rectangle obterArea() {
		return externo.getBounds();
	}

	public static void main(String[] args) {
		System.out.println(220 / 10);
	}
}
