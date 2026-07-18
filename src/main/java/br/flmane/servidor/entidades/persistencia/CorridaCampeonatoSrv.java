package br.flmane.servidor.entidades.persistencia;

import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "f1_corridacampeonatosrv")
public class CorridaCampeonatoSrv extends FlManeDados {

	private String nomeCircuito;
	private String arquivoCircuito;
	private Long rodada, tempoInicio, tempoFim;
	@ManyToOne
	@JoinColumn(nullable = false)
	private CampeonatoSrv campeonato;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "corridaCampeonato")
	private List<DadosCorridaCampeonatoSrv> dadosCorridaCampeonatos = new LinkedList<DadosCorridaCampeonatoSrv>();

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

	public CampeonatoSrv getCampeonato() {
		return campeonato;
	}

	public void setCampeonato(CampeonatoSrv campeonato) {
		this.campeonato = campeonato;
	}

	public List<DadosCorridaCampeonatoSrv> getDadosCorridaCampeonatos() {
		return dadosCorridaCampeonatos;
	}

	public void setDadosCorridaCampeonatos(
			List<DadosCorridaCampeonatoSrv> dadosCorridaCampeonatos) {
		this.dadosCorridaCampeonatos = dadosCorridaCampeonatos;
	}

	public String getArquivoCircuito() {
		return arquivoCircuito;
	}

	public void setArquivoCircuito(String arquivoCircuito) {
		this.arquivoCircuito = arquivoCircuito;
	}

	public Long getRodada() {
		return rodada;
	}

	public void setRodada(Long rodada) {
		this.rodada = rodada;
	}

}
