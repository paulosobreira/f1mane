package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class ObjetoLivre extends ObjetoPista {
	private List<Point> pontos = new ArrayList<Point>();

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		g2d.setColor(new Color(corPimaria));
		Polygon polygon = new Polygon();
		for (Point ponto : pontos) {
			polygon.addPoint(ponto.x, ponto.y);
		}
		AffineTransform affineTransform = AffineTransform.getScaleInstance(
				zoom, zoom);
		double rad = Math.toRadians((double) getAngulo());
		affineTransform.setToRotation(rad, polygon.getBounds().getCenterX(),
				polygon.getBounds().getCenterY());
		GeneralPath generalPath = new GeneralPath(polygon);
		g2d.fill(generalPath.createTransformedShape(affineTransform));
	}
}
