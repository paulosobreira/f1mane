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
	GeneralPath generalPath = new GeneralPath();

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public void gerar() {
		Polygon polygon = new Polygon();

		for (Point ponto : pontos) {
			polygon.addPoint((int) (ponto.x), (int) (ponto.y));
		}
		generalPath.append(polygon, true);
		posicaoQuina = generalPath.getBounds().getLocation();
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		//generalPath.getBounds().setLocation(getPosicaoQuina());
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad,
				generalPath.getBounds().getCenterX(), generalPath.getBounds()
						.getCenterY());
		GeneralPath gp = new GeneralPath(generalPath);
		gp.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.fill(gp.createTransformedShape(affineTransform));
	}

	@Override
	public Rectangle obterArea() {
		return generalPath.getBounds();
	}
}
