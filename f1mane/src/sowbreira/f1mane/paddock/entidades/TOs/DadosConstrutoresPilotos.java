package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 21/06/2009 as 19:15:34
 */
public class DadosConstrutoresPilotos implements Serializable {
	private String nome;
	private int pontos;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

}
