package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.List;

import sowbreira.f1mane.entidades.Volta;

/**
 * @author Paulo Sobreira Criado em 15/08/2007 as 16:33:52
 */
public class DadosJogo implements Serializable {
	private List pilotosList = null;
	private Volta melhoVolta;
	private int voltaAtual;
	private boolean corridaTerminada;
	private String texto;
	private String clima;

	public String getClima() {
		return clima;
	}

	public void setClima(String clima) {
		this.clima = clima;
	}

	public boolean isCorridaTerminada() {
		return corridaTerminada;
	}

	public void setCorridaTerminada(boolean corridaTerminada) {
		this.corridaTerminada = corridaTerminada;
	}

	public int getVoltaAtual() {
		return voltaAtual;
	}

	public void setVoltaAtual(int voltaAtual) {
		this.voltaAtual = voltaAtual;
	}

	public String getTexto() {
		return texto;
	}

	public void setTexto(String texto) {
		this.texto = texto;
	}

	public Volta getMelhoVolta() {
		return melhoVolta;
	}

	public void setMelhoVolta(Volta melhoVolta) {
		this.melhoVolta = melhoVolta;
	}

	public List getPilotosList() {
		return pilotosList;
	}

	public void setPilotosList(List objects) {
		this.pilotosList = objects;
	}

}
