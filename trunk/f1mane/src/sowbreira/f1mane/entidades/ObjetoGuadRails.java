package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

public class ObjetoGuadRails extends ObjetoPista {

	RoundRectangle2D externo;

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		externo = new RoundRectangle2D.Double(getPosicaoQuina().x,
				getPosicaoQuina().y, 2, altura, 5, 5);
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
		g2d.draw(generalPath.createTransformedShape(affineTransform));
	}

	@Override
	public Rectangle obterArea() {
		return externo.getBounds();
	}

	public static void main(String[] args) {
		System.out.println(220 / 10);
	}
}
