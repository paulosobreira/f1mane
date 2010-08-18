package sowbreira.f1mane.paddock.entidades.persistencia;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class CorridaCampeonato extends F1ManeDados {

	private String nomeCircuito;
	private Long tempoInicio, tempoFim;
	@ManyToOne
	@JoinColumn(nullable = false)
	private Campeonato campeonato;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "corridaCampeonato")
	private List<DadosCorridaCampeonato> dadosCorridaCampeonatos = new LinkedList<DadosCorridaCampeonato>();

	public String getNomeCircuito() {
		return nomeCircuito;
	}

	public void setNomeCircuito(String nomeCircuito) {
		this.nomeCircuito = nomeCircuito;
	}

	public Long getTempoInicio() {
		return tempoInicio;
	}

	public void setTempoInicio(Long tempoInicio) {
		this.tempoInicio = tempoInicio;
	}

	public Long getTempoFim() {
		return tempoFim;
	}

	public void setTempoFim(Long tempoFim) {
		this.tempoFim = tempoFim;
	}

	public Campeonato getCampeonato() {
		return campeonato;
	}

	public void setCampeonato(Campeonato campeonato) {
		this.campeonato = campeonato;
	}

	public List<DadosCorridaCampeonato> getDadosCorridaCampeonatos() {
		return dadosCorridaCampeonatos;
	}

	public void setDadosCorridaCampeonatos(
			List<DadosCorridaCampeonato> dadosCorridaCampeonatos) {
		this.dadosCorridaCampeonatos = dadosCorridaCampeonatos;
	}

}
