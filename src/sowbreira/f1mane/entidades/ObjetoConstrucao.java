package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class ObjetoConstrucao extends ObjetoPista {

	RoundRectangle2D externo;
	RoundRectangle2D interno;

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		externo = new RoundRectangle2D.Double(getPosicaoQuina().x,
				getPosicaoQuina().y, largura, altura, 15, 15);
		interno = new RoundRectangle2D.Double((getPosicaoQuina().x + 10),
				(getPosicaoQuina().y + 10), (largura - 20), (altura - 20), 15,
				15);
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
		//affineTransform = AffineTransform.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, interno.getBounds().getCenterX(),
				interno.getBounds().getCenterY());
		generalPath = new GeneralPath(interno);
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

}
