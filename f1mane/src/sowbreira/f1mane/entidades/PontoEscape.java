package sowbreira.f1mane.entidades;

import java.awt.Point;
import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class PontoEscape implements Serializable {
	private static final long serialVersionUID = -7551741480296259036L;

	@JsonIgnore
	private Point point;

	private int pista;

	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public int getPista() {
		return pista;
	}

	public void setPista(int pista) {
		this.pista = pista;
	}

	public Point getPoint() {
		return point;
	}

}
