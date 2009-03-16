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

	public long getUlimaAtividade() {
		return ulimaAtividade;
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
