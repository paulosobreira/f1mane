package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;

/**
 * @author Paulo Sobreira Criado em 21/10/2007 as 18:07:48
 */
public class DadosJogador implements Serializable {
	private String nome;
	private long ultimoAceso;
	private long pontos;
	private long corridas;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public long getPontos() {
		return pontos;
	}

	public void setPontos(long pontos) {
		this.pontos = pontos;
	}

	public long getUltimoAceso() {
		return ultimoAceso;
	}

	public void setUltimoAceso(long ultimoAceso) {
		this.ultimoAceso = ultimoAceso;
	}

	public long getCorridas() {
		return corridas;
	}

	public void setCorridas(long corridas) {
		this.corridas = corridas;
	}

}
