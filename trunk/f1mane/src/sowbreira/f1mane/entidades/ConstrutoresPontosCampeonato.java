package sowbreira.f1mane.entidades;

import java.io.Serializable;

public class ConstrutoresPontosCampeonato implements Serializable {

	private String nomeEquipe;
	private int pontos;

	
	
	public ConstrutoresPontosCampeonato(String nomeEquipe, int pontos) {
		super();
		this.nomeEquipe = nomeEquipe;
		this.pontos = pontos;
	}

	public ConstrutoresPontosCampeonato() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean equals(Object obj) {
		ConstrutoresPontosCampeonato construtoresPontosCampeonato = (ConstrutoresPontosCampeonato) obj;
		return nomeEquipe.equals(construtoresPontosCampeonato.getNomeEquipe());
	}

	@Override
	public int hashCode() {
		return nomeEquipe.hashCode();
	}

	public String getNomeEquipe() {
		return nomeEquipe;
	}

	public void setNomeEquipe(String nomeEquipe) {
		this.nomeEquipe = nomeEquipe;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

}
