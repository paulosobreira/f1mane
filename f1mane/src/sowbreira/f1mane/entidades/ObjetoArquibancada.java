package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class ObjetoArquibancada extends ObjetoPista {

	RoundRectangle2D externo;

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		externo = new RoundRectangle2D.Double(getPosicaoQuina().x,
				getPosicaoQuina().y, largura, altura, 15, 15);
		int linhas = (int) (externo.getHeight() / 10);

		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, externo.getBounds().getCenterX(),
				externo.getBounds().getCenterY());
		GeneralPath generalPath = new GeneralPath(externo);
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(generalPath.createTransformedShape(affineTransform));
		generalPath = new GeneralPath();

		for (int i = 0; i < linhas; i++) {
			if (i % 2 == 0)
				continue;
			RoundRectangle2D interno = new RoundRectangle2D.Double(
					(getPosicaoQuina().x + 10),
					(getPosicaoQuina().y + (10 * i)), largura - 20, 10, 15, 15);
			generalPath.append(interno, true);

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
