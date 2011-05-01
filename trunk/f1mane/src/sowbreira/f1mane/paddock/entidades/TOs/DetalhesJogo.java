package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Paulo Sobreira Criado em 04/08/2007 as 20:02:13
 */
public class DetalhesJogo implements Serializable {

	private static final long serialVersionUID = -3091559660158990260L;
	private Map jogadoresPilotos = new HashMap();
	private DadosCriarJogo dadosCriarJogo;
	private long tempoCriacao;
	private long voltaAtual;
	private long numVoltas;
	private String nomeCriador;

	public String getNomeCriador() {
		return nomeCriador;
	}

	public long getNumVoltas() {
		return numVoltas;
	}

	public void setNumVoltas(long numVoltas) {
		this.numVoltas = numVoltas;
	}

	public void setNomeCriador(String nomeCriador) {
		this.nomeCriador = nomeCriador;
	}

	public long getTempoCriacao() {
		return tempoCriacao;
	}

	public void setTempoCriacao(long criacao) {
		this.tempoCriacao = criacao;
	}

	public Map getJogadoresPilotos() {
		return jogadoresPilotos;
	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosCriarJogo;
	}

	public void setDadosCriarJogo(DadosCriarJogo dadosCriarJogo) {
		this.dadosCriarJogo = dadosCriarJogo;
	}

	public void setJogadoresPilotos(Map jogadoresPilotos) {
		this.jogadoresPilotos = jogadoresPilotos;
	}

	public long getVoltaAtual() {
		return voltaAtual;
	}

	public void setVoltaAtual(long voltaAtual) {
		this.voltaAtual = voltaAtual;
	}

}
