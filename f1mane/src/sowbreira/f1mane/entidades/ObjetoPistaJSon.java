package sowbreira.f1mane.entidades;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjetoPistaJSon {

	private List<Ponto> pontos = new ArrayList<Ponto>();

	private Integer indexInicio;

	private Integer indexFim;

	public List<Ponto> getPontos() {
		return pontos;
	}

	public void setPontos(List<Ponto> pontos) {
		this.pontos = pontos;
	}

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

}
