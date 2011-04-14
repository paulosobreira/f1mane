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
	private String nomePiloto;
	private int ptsPiloto;
	private Map<String, String> pilotosEquipesCampeonato = new HashMap<String, String>();
	private Integer qtdeVoltas;
	private boolean semReabasteciemnto;
	private boolean semTrocaPneus;
	private boolean kers;
	private boolean drs;
	private List pilotos = new LinkedList();
	private List corridas = new LinkedList();
	private Map<String, List> dadosCorridas = new HashMap<String, List>();

	public String getNomePiloto() {
		return nomePiloto;
	}

	public int getPtsPiloto() {
		return ptsPiloto;
	}

	public void setPtsPiloto(int ptsPiloto) {
		this.ptsPiloto = ptsPiloto;
	}

	public Map<String, String> getPilotosEquipesCampeonato() {
		return pilotosEquipesCampeonato;
	}

	public void setPilotosEquipesCampeonato(
			Map<String, String> pilotosEquipesCampeonato) {
		this.pilotosEquipesCampeonato = pilotosEquipesCampeonato;
	}

	public void setNomePiloto(String nomePiloto) {
		this.nomePiloto = nomePiloto;
	}

	public String getTemporada() {
		return temporada;
	}

	public boolean isSemReabasteciemnto() {
		return semReabasteciemnto;
	}

	public void setSemReabasteciemnto(boolean semReabasteciemnto) {
		this.semReabasteciemnto = semReabasteciemnto;
	}

	public boolean isSemTrocaPneus() {
		return semTrocaPneus;
	}

	public void setSemTrocaPneus(boolean semTrocaPneus) {
		this.semTrocaPneus = semTrocaPneus;
	}

	public boolean isKers() {
		return kers;
	}

	public void setKers(boolean kers) {
		this.kers = kers;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
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
