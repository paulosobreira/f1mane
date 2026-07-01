package br.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

public class ObjetoGuardRails extends ObjetoPista {

	RoundRectangle2D externo = new RoundRectangle2D.Double();

	public ObjetoGuardRails() {
		setLargura(2);
		setAltura(100);
		setCorPimaria(new Color(220, 220, 220));
		setCorSecundaria(new Color(220, 220, 220));
		setTransparencia(255);
	}

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
	}
}
