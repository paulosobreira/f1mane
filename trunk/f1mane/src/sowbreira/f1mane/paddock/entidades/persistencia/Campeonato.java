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

@Entity
public class Campeonato extends F1ManeDados {
	private transient String circuitoAtual;
	private String temporada;
	@Column(unique = true, nullable = false)
	private String nome;
	private String nivel;
	private Integer qtdeVoltas;
	private boolean semReabasteciemnto;
	private boolean semTrocaPneus;
	private boolean kers;
	private boolean drs;

	@OneToOne
	@JoinColumn(nullable = false)
	private JogadorDadosSrv jogadorDadosSrv;
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "campeonato")
	private List<CorridaCampeonato> corridaCampeonatos = new LinkedList<CorridaCampeonato>();

	public String getTemporada() {
		return temporada;
	}

	public String getCircuitoAtual() {
		return circuitoAtual;
	}

	public void setCircuitoAtual(String circuitoAtual) {
		this.circuitoAtual = circuitoAtual;
	}

	public String getNome() {
		return nome;
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

	public void setNome(String nome) {
		this.nome = nome;
	}

	public JogadorDadosSrv getJogadorDadosSrv() {
		return jogadorDadosSrv;
	}

	public void setJogadorDadosSrv(JogadorDadosSrv jogadorDadosSrv) {
		this.jogadorDadosSrv = jogadorDadosSrv;
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

	public List<CorridaCampeonato> getCorridaCampeonatos() {
		return corridaCampeonatos;
	}

	public void setCorridaCampeonatos(List<CorridaCampeonato> corridaCampeonatos) {
		this.corridaCampeonatos = corridaCampeonatos;
	}

}
