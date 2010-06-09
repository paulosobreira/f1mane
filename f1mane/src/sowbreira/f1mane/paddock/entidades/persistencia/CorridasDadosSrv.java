package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import sowbreira.f1mane.controles.InterfaceJogo;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:47:15
 */
@Entity
public class CorridasDadosSrv extends F1ManeDados implements Serializable {

	@ManyToOne
	@JoinColumn(nullable = false)
	private JogadorDadosSrv jogadorDadosSrv;
	private long tempoInicio, tempoFim;
	private String piloto;
	private String carro;
	private String circuito;
	private String nivel;
	private String temporada;
	private boolean mudouCarro;
	private int numVoltas;
	private int porcentConcluida;
	private int posicao;
	private int pontos;

	public JogadorDadosSrv getJogadorDadosSrv() {
		return jogadorDadosSrv;
	}

	public void setJogadorDadosSrv(JogadorDadosSrv jogadorDadosSrv) {
		this.jogadorDadosSrv = jogadorDadosSrv;
	}

	public int getNumVoltas() {
		return numVoltas;
	}

	public void setNumVoltas(int numVoltas) {
		this.numVoltas = numVoltas;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

	public String getCarro() {
		return carro;
	}

	public void setCarro(String carro) {
		this.carro = carro;
	}

	public String getPiloto() {
		return piloto;
	}

	public void setPiloto(String piloto) {
		this.piloto = piloto;
	}

	public long getTempoFim() {
		return tempoFim;
	}

	public void setTempoFim(long tempoFim) {
		this.tempoFim = tempoFim;
	}

	public long getTempoInicio() {
		return tempoInicio;
	}

	public void setTempoInicio(long tempoInicio) {
		this.tempoInicio = tempoInicio;
	}

	public String getCircuito() {
		return circuito;
	}

	public void setCircuito(String circuito) {
		this.circuito = circuito;
	}

	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	public boolean isMudouCarro() {
		return mudouCarro;
	}

	public void setMudouCarro(boolean mudouCarro) {
		this.mudouCarro = mudouCarro;
	}

	public int getPorcentConcluida() {
		return porcentConcluida;
	}

	public void setPorcentConcluida(int porcentCocluida) {
		this.porcentConcluida = porcentCocluida;
	}

	public String getNivel() {
		if (Util.isNullOrEmpty(nivel)) {
			return InterfaceJogo.NORMAL;
		}
		return nivel;
	}

	public void setNivel(String nivel) {
		this.nivel = nivel;
	}

	public String getTemporada() {
		return temporada;
	}

	public void setTemporada(String temporada) {
		this.temporada = temporada;
	}

}
