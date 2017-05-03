package sowbreira.f1mane.entidades;

import java.awt.Point;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Ponto {
	@JsonIgnore
	private Point point;

	public double getX() {
		return point.getX();
	}

	public double getY() {
		return point.getY();
	}

	public void setPoint(Point point) {
		this.point = point;
	}

}
