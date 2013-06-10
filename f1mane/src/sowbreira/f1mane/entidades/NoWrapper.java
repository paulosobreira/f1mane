package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;

public class NoWrapper {

	private No no;

	public NoWrapper(No no) {
		super();
		this.no = no;
	}

	public BufferedImage getBufferedImage() {
		return no.getBufferedImage();
	}

	public int getDrawX() {
		return no.getDrawX();
	}

	public int getDrawY() {
		return no.getDrawY();
	}

	public int getIndex() {
		return no.getIndex();
	}

	public Point getPoint() {
		return no.getPoint();
	}

	public Color getTipo() {
		return no.getTipo();
	}

	public int getX() {
		return no.getX();
	}

	public int getY() {
		return no.getY();
	}

	public boolean isBox() {
		return no.isBox();
	}

	public boolean isNoEntradaBox() {
		return no.isNoEntradaBox();
	}

	public boolean isNoSaidaBox() {
		return no.isNoSaidaBox();
	}

	public void setIndex(int index) {
		no.setIndex(index);
	}

	public void setNoEntradaBox(boolean noEntradaBox) {
		no.setNoEntradaBox(noEntradaBox);
	}

	public void setNoSaidaBox(boolean noSaidaBox) {
		no.setNoSaidaBox(noSaidaBox);
	}

	public void setPoint(Point point) {
		no.setPoint(point);
	}

	public void setTipo(Color tipo) {
		no.setTipo(tipo);
	}

	public String toString() {
		return no.toString();
	}

	public boolean verificaCruvaAlta() {
		return no.verificaCruvaAlta();
	}

	public boolean verificaCruvaBaixa() {
		return no.verificaCruvaBaixa();
	}

	public boolean verificaRetaOuLargada() {
		return no.verificaRetaOuLargada();
	}

	@Override
	public int hashCode() {
		int index = no.getIndex();
		Color tipo = no.getTipo();
		Point point = no.getPoint();
		final int prime = 31;
		int result = 1;
		result = prime * result + index;
		result = prime * result + ((point == null) ? 0 : point.hashCode());
		result = prime * result + ((tipo == null) ? 0 : tipo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		int index = no.getIndex();
		Color tipo = no.getTipo();
		Point point = no.getPoint();

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NoWrapper other = (NoWrapper) obj;
		if (index != other.getIndex())
			return false;
		if (point == null) {
			if (other.getPoint() != null)
				return false;
		} else if (!point.equals(other.getPoint()))
			return false;
		if (tipo == null) {
			if (other.getTipo() != null)
				return false;
		} else if (!tipo.equals(other.getTipo()))
			return false;
		return true;
	}
}
