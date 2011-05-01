package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.paddock.servlet.JogoServidor;

/**
 * @author paulo.sobreira
 * 
 */
public class SessaoCliente implements Serializable {

	private static final long serialVersionUID = -1814045404166555104L;

	private long ulimaAtividade;

	private String nomeJogador;

	private String jogoAtual;

	private String pilotoAtual;

	public long getUlimaAtividade() {
		return ulimaAtividade;
	}

	public String getJogoAtual() {
		return jogoAtual;
	}

	public void setJogoAtual(String jogoAtual) {
		this.jogoAtual = jogoAtual;
	}

	public String getPilotoAtual() {
		return pilotoAtual;
	}

	public void setPilotoAtual(String pilotoAtual) {
		this.pilotoAtual = pilotoAtual;
	}

	public void setUlimaAtividade(long ulimaAtividade) {
		this.ulimaAtividade = ulimaAtividade;
	}

	public String getNomeJogador() {
		return nomeJogador;
	}

	public void setNomeJogador(String apelido) {
		this.nomeJogador = apelido;
	}

	public boolean equals(Object obj) {
		SessaoCliente sessaoCliente = (SessaoCliente) obj;
		return nomeJogador.equals(sessaoCliente.getNomeJogador());
	}

	public String toString() {
		return nomeJogador;
	}
}
