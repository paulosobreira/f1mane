package br.f1mane.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjetoPistaJSon {

	private Integer indexInicio;

	private Integer indexFim;

	private Integer x;

	private Integer y;

	boolean transparenciaBox;

	public Integer getIndexInicio() {
		return indexInicio;
	}

	public void setIndexInicio(Integer indexInicio) {
		this.indexInicio = indexInicio;
	}

	public Integer getIndexFim() {
		return indexFim;
	}

	public void setIndexFim(Integer indexFim) {
		this.indexFim = indexFim;
	}

	public Integer getX() {
		return x;
	}

	public void setX(Integer x) {
		this.x = x;
	}

	public Integer getY() {
		return y;
	}

	public void setY(Integer y) {
		this.y = y;
	}

	public boolean isTransparenciaBox() {
		return transparenciaBox;
	}

	public void setTransparenciaBox(boolean transparenciaBox) {
		this.transparenciaBox = transparenciaBox;
	}

}
