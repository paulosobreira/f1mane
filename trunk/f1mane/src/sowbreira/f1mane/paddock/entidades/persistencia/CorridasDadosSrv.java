package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;

import br.nnpe.Util;

import sowbreira.f1mane.controles.InterfaceJogo;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:47:15
 */
public class CorridasDadosSrv implements Serializable {

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
