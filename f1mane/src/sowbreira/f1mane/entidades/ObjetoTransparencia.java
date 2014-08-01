package sowbreira.f1mane.entidades;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

public class ObjetoTransparencia extends ObjetoPista {

	public final static Color transp = new Color(255, 255, 255, 160);

	private static final long serialVersionUID = 2035296854458757556L;
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
		g2d.setColor(transp);
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

	public void desenhaCarro(Graphics2D g2d, double zoom, int carroX, int carroY) {
		Polygon polygonCarro = new Polygon();
		for (Point ponto : pontos) {
			polygonCarro.addPoint((int) (ponto.x - carroX),
					(int) (ponto.y - carroY));
		}

		g2d.setColor(Color.white);
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, polygonCarro.getBounds()
				.getCenterX(), polygonCarro.getBounds().getCenterY());
		GeneralPath generalPath = new GeneralPath(polygonCarro);
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		AlphaComposite composite = AlphaComposite.getInstance(
				AlphaComposite.CLEAR, 1);
		g2d.setComposite(composite);
		g2d.fill(generalPath.createTransformedShape(affineTransform));
	}

	@Override
	public Rectangle obterArea() {
		return polygon.getBounds();
	}

	@Override
	public Point getPosicaoQuina() {
		if (super.getPosicaoQuina() == null) {
			Rectangle obterArea = obterArea();
			setAltura((int) obterArea.getBounds().getHeight());
			setLargura((int) obterArea.getBounds().getWidth());
			setPosicaoQuina(new Point(obterArea.getBounds().x,
					obterArea.getBounds().y));
		}
		return super.getPosicaoQuina();
	}
}
