package br.nnpe;

public class Numero {
	private Double numero;

	public Numero(Integer ptsCarreira) {
		this.numero = new Double(ptsCarreira.doubleValue());
	}

	public Double getNumero() {
		return numero;
	}

	public void setNumero(Double numero) {
		this.numero = numero;
	}

}
