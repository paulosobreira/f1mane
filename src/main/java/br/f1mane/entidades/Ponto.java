package br.f1mane.entidades;

import java.awt.Point;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Ponto {
	@JsonIgnore
	private Point point;

	public Ponto(Point point) {
		super();
		this.point = point;
	}

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
