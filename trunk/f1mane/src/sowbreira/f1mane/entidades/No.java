package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.io.Serializable;

import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado Em 11:04:20
 */
public class No implements Serializable {
	public static Color LARGADA = Color.BLUE;
	public static Color RETA = Color.GREEN;
	public static Color CURVA_ALTA = Color.YELLOW;
	public static Color CURVA_BAIXA = Color.RED;
	public static Color BOX = Color.CYAN;
	public static Color PARADA_BOX = Color.ORANGE;
	private Point point = new Point(1000, 1000);
	private int index;
	private Color tipo;
	private boolean noEntradaBox;
	private boolean noSaidaBox;

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public boolean isNoEntradaBox() {
		return noEntradaBox;
	}

	public void setNoEntradaBox(boolean noEntradaBox) {
		this.noEntradaBox = noEntradaBox;
	}

	public boolean isNoSaidaBox() {
		return noSaidaBox;
	}

	public void setNoSaidaBox(boolean noSaidaBox) {
		this.noSaidaBox = noSaidaBox;
	}

	public String toString() {
		if (LARGADA.equals(tipo)) {
			return "LARGADA";
		} else if (RETA.equals(tipo)) {
			return "RETA";
		} else if (CURVA_ALTA.equals(tipo)) {
			return "CURVA_ALTA";
		} else if (CURVA_BAIXA.equals(tipo)) {
			return "CURVA_BAIXA";
		} else if (BOX.equals(tipo)) {
			return "BOX";
		} else if (PARADA_BOX.equals(tipo)) {
			return "PARADA_BOX";
		}

		return super.toString();
	}

	public BufferedImage getBufferedImage() {
		BufferedImage srcBufferedImage = new BufferedImage(14, 14,
				BufferedImage.TYPE_INT_ARGB);
		Graphics g = srcBufferedImage.getGraphics();
		Color c = new Color(0.0f, 0.0f, 0.0f, 0.0f);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setColor(c);
		g2.fillRect(0, 0, 14, 14);
		g2.setColor(tipo);
		g2.fillRoundRect(0, 0, 10, 10, 15, 15);
		g2.dispose();

		return srcBufferedImage;
	}

	public int getDrawX() {
		return point.x - 5;
	}

	public int getDrawY() {
		return point.y - 5;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public Color getTipo() {
		return tipo;
	}

	public void setTipo(Color tipo) {
		this.tipo = tipo;
	}

	public int getX() {
		return point.x;
	}

	public int getY() {
		return point.y;
	}

	public boolean isBox() {
		return ((BOX.equals(tipo)) || (PARADA_BOX.equals(tipo)));
	}

	public boolean verificaRetaOuLargada() {
		return (LARGADA.equals(tipo)) || (RETA.equals(tipo));
	}

	public boolean verificaCruvaAlta() {
		return (CURVA_ALTA.equals(tipo));
	}

	public boolean verificaCruvaBaixa() {
		return (CURVA_BAIXA.equals(tipo));
	}
}
