package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

public class CorridaCampeonatoTO implements Serializable {

	private int rodada;

	private String nomeCircuito;

	private String arquivoCircuito;

	private String data;

	private String vencedor;

	public String getNomeCircuito() {
		return nomeCircuito;
	}

	public void setNomeCircuito(String nomeCircuito) {
		this.nomeCircuito = nomeCircuito;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getVencedor() {
		return vencedor;
	}

	public void setVencedor(String vencedor) {
		this.vencedor = vencedor;
	}

	public int getRodada() {
		return rodada;
	}

	public void setRodada(int rodada) {
		this.rodada = rodada;
	}

	public String getArquivoCircuito() {
		return arquivoCircuito;
	}

	public void setArquivoCircuito(String arquivoCircuito) {
		this.arquivoCircuito = arquivoCircuito;
	}

}
