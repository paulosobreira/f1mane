package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class ObjetoLivre extends ObjetoPista {
	private List<Point> pontos = new ArrayList<Point>();
	Polygon polygon = new Polygon();

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public void gerar() {
		polygon = new Polygon();
		for (Point ponto : pontos) {
			polygon.addPoint((int) (ponto.x), (int) (ponto.y));
		}
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		if (posicaoQuina != null) {
			polygon.translate(posicaoQuina.x - polygon.getBounds().x,
					posicaoQuina.y - polygon.getBounds().y);
		}

		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, polygon.getBounds().getCenterX(),
				polygon.getBounds().getCenterY());
		GeneralPath generalPath = new GeneralPath(polygon);
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.fill(generalPath.createTransformedShape(affineTransform));
	}

	@Override
	public Rectangle obterArea() {
		return polygon.getBounds();
	}
}
