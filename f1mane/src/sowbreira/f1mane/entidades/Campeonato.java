package sowbreira.f1mane.entidades;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Campeonato implements Serializable {
	private String temporada;
	private String nivel;
	private Integer qtdeVoltas;
	private List pilotos = new LinkedList();
	private List corridas = new LinkedList();
	private Map<String, List> dadosCorridas = new HashMap<String, List>();

	public String getTemporada() {
		return temporada;
	}

	public void setTemporada(String temporada) {
		this.temporada = temporada;
	}

	public String getNivel() {
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public Integer getQtdeVoltas() {
		return qtdeVoltas;
	}

	public void setQtdeVoltas(Integer qtdeVoltas) {
		this.qtdeVoltas = qtdeVoltas;
	}

	public List getPilotos() {
		return pilotos;
	}

	public void setPilotos(List pilotos) {
		this.pilotos = pilotos;
	}

	public List getCorridas() {
		return corridas;
	}

	public void setCorridas(List corridas) {
		this.corridas = corridas;
	}

	public Map getDadosCorridas() {
		return dadosCorridas;
	}

	public String getCircuitoVez() {
		for (Iterator iterator = corridas.iterator(); iterator.hasNext();) {
			String circuito = (String) iterator.next();
			if (dadosCorridas.get(circuito) == null) {
				return circuito;
			}
		}
		return null;

	}

}
