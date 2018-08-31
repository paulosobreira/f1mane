package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;

public class CampeonatoTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private transient String circuitoAtual;

	private transient String nomePiloto;

	private transient String carroPiloto;

	private transient String temporadaCarro;

	private transient String temporadaCapacete;

	private transient String idCarro;

	private transient String idPiloto;

	private Campeonato campeonato;

	private List<CorridaCampeonatoTO> corridas = new ArrayList<CorridaCampeonatoTO>();

	public String getCircuitoAtual() {
		return circuitoAtual;
	}

	public void setCircuitoAtual(String circuitoAtual) {
		this.circuitoAtual = circuitoAtual;
	}

	public String getNomePiloto() {
		return nomePiloto;
	}

	public void setNomePiloto(String nomePiloto) {
		this.nomePiloto = nomePiloto;
	}

	public String getCarroPiloto() {
		return carroPiloto;
	}

	public void setCarroPiloto(String carroPiloto) {
		this.carroPiloto = carroPiloto;
	}

	public String getTemporadaCarro() {
		return temporadaCarro;
	}

	public void setTemporadaCarro(String temporadaCarro) {
		this.temporadaCarro = temporadaCarro;
	}

	public String getTemporadaCapacete() {
		return temporadaCapacete;
	}

	public void setTemporadaCapacete(String temporadaCapacete) {
		this.temporadaCapacete = temporadaCapacete;
	}

	public String getIdCarro() {
		return idCarro;
	}

	public void setIdCarro(String idCarro) {
		this.idCarro = idCarro;
	}

	public void setCampeonato(Campeonato campeonato) {
		this.campeonato = campeonato;
	}

	public String getIdPiloto() {
		return idPiloto;
	}

	public void setIdPiloto(String idPiloto) {
		this.idPiloto = idPiloto;
	}

	public String getTemporada() {
		return campeonato.getTemporada();
	}

	public String getNome() {
		return campeonato.getNome();
	}

	public boolean isDrs() {
		return campeonato.isDrs();
	}

	public boolean isReabastecimento() {
		return campeonato.isReabastecimento();
	}

	public boolean isTrocaPneus() {
		return campeonato.isTrocaPneus();
	}

	public Integer getQtdeVoltas() {
		return campeonato.getQtdeVoltas();
	}

	public boolean isErs() {
		return campeonato.isErs();
	}

	public List<CorridaCampeonatoTO> getCorridas() {
		return corridas;
	}

}
