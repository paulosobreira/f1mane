package sowbreira.f1mane.paddock.entidades.persistencia;

import java.util.LinkedList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

@Entity(name = "f1_campeonatosrv")
public class CampeonatoSrv extends F1ManeDados {

	@Column(nullable = false)
	private String idPiloto;

	@Column(nullable = false)
	private String temporada;

	@Column(nullable = false)
	private String nome;

	@Column(nullable = false)
	private String nivel;

	@Column(nullable = false)
	private Integer qtdeVoltas;

	@Column(nullable = false)
	private boolean reabastecimento;

	@Column(nullable = false)
	private boolean trocaPneus;

	@Column(nullable = false)
	private boolean ers;

	@Column(nullable = false)
	private boolean drs;

	@Column(nullable = false)
	private boolean finalizado;

	@OneToOne
	@JoinColumn(nullable = false)
	private JogadorDadosSrv jogadorDadosSrv;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "campeonato")
	private List<CorridaCampeonatoSrv> corridaCampeonatos = new LinkedList<CorridaCampeonatoSrv>();

	public String getTemporada() {
		return temporada;
	}

	public String getNome() {
		return nome;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public JogadorDadosSrv getJogadorDadosSrv() {
		return jogadorDadosSrv;
	}

	public void setJogadorDadosSrv(JogadorDadosSrv jogadorDadosSrv) {
		this.jogadorDadosSrv = jogadorDadosSrv;
	}

	public boolean isReabastecimento() {
		return reabastecimento;
	}

	public void setReabastecimento(boolean reabastecimento) {
		this.reabastecimento = reabastecimento;
	}

	public boolean isTrocaPneus() {
		return trocaPneus;
	}

	public void setTrocaPneus(boolean trocaPneus) {
		this.trocaPneus = trocaPneus;
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

	public List<CorridaCampeonatoSrv> getCorridaCampeonatos() {
		return corridaCampeonatos;
	}

	public void setCorridaCampeonatos(
			List<CorridaCampeonatoSrv> corridaCampeonatos) {
		this.corridaCampeonatos = corridaCampeonatos;
	}

	public String getIdPiloto() {
		return idPiloto;
	}

	public void setIdPiloto(String idPiloto) {
		this.idPiloto = idPiloto;
	}

	public boolean isErs() {
		return ers;
	}

	public void setErs(boolean ers) {
		this.ers = ers;
	}

	public boolean isFinalizado() {
		return finalizado;
	}

	public void setFinalizado(boolean finalizado) {
		this.finalizado = finalizado;
	}

}
