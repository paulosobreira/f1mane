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
	private String rival;
	private int vitorias;
	private int derrotas;
	private int ptsPiloto;
	private Map<String, String> pilotosEquipesCampeonato = new HashMap<String, String>();
	private Map<String, Integer> pilotosHabilidadeCampeonato = new HashMap<String, Integer>();
	private Map<String, Integer> equipesPotenciaCampeonato = new HashMap<String, Integer>();
	private Integer qtdeVoltas;
	private boolean semReabasteciemnto;
	private boolean semTrocaPneus;
	private boolean kers;
	private boolean drs;
	private boolean ultimaCorridaSemDesafiar;
	private List pilotos = new LinkedList();
	private List corridas = new LinkedList();
	private Map<String, List> dadosCorridas = new HashMap<String, List>();

	public String getNomePiloto() {
		return nomePiloto;
	}

	public String getRival() {
		return rival;
	}

	public void setRival(String rival) {
		this.rival = rival;
	}

	public int getVitorias() {
		return vitorias;
	}

	public void setVitorias(int vitorias) {
		this.vitorias = vitorias;
	}

	public int getDerrotas() {
		return derrotas;
	}

	public void setDerrotas(int derrotas) {
		this.derrotas = derrotas;
	}

	public int getPtsPiloto() {
		return ptsPiloto;
	}

	public boolean isUltimaCorridaSemDesafiar() {
		return ultimaCorridaSemDesafiar;
	}

	public void setUltimaCorridaSemDesafiar(boolean ultimaCorridaSemDesafiar) {
		this.ultimaCorridaSemDesafiar = ultimaCorridaSemDesafiar;
	}

	public void setPtsPiloto(int ptsPiloto) {
		this.ptsPiloto = ptsPiloto;
	}

	public Map<String, Integer> getPilotosHabilidadeCampeonato() {
		return pilotosHabilidadeCampeonato;
	}

	public void setPilotosHabilidadeCampeonato(
			Map<String, Integer> pilotosHabilidadeCampeonato) {
		this.pilotosHabilidadeCampeonato = pilotosHabilidadeCampeonato;
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

	public Map<String, Integer> getEquipesPotenciaCampeonato() {
		return equipesPotenciaCampeonato;
	}

	public void setEquipesPotenciaCampeonato(
			Map<String, Integer> equipesPotenciaCampeonato) {
		this.equipesPotenciaCampeonato = equipesPotenciaCampeonato;
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

	public void setDadosCorridas(Map<String, List> dadosCorridas) {
		this.dadosCorridas = dadosCorridas;
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
