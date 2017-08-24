package sowbreira.f1mane.entidades;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

import sowbreira.f1mane.controles.ControleEstatisticas;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 16:03:49
 */
public class Volta implements Serializable {
	private int pilotoId;
	@JsonIgnore
	private Long ciclosInicio = new Long(0);
	@JsonIgnore
	private Long ciclosFim = new Long(0);
	@JsonIgnore
	private Long tempoNumero;
	@JsonIgnore
	private Long tempoPausado = new Long(0);
	@JsonIgnore
	private boolean voltaBox;
	@JsonIgnore
	private boolean voltaSafetyCar;

	public Volta(Long fullnum) {
		super();
		this.tempoNumero = fullnum;
	}

	public Volta() {
	}

	public boolean isVoltaBox() {
		return voltaBox;
	}

	public void setVoltaBox(boolean voltaBox) {
		this.voltaBox = voltaBox;
	}

	private long parseLong(String string) {
		try {
			return Long.parseLong(string);
		} catch (Exception e) {
		}
		return 0;
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	public int getPiloto() {
		return pilotoId;
	}

	public void setPiloto(int piloto) {
		this.pilotoId = piloto;
	}

	public long getCiclosFim() {
		return ciclosFim;
	}

	public void setCiclosFim(long ciclosFim) {
		this.ciclosFim = ciclosFim;
	}

	public long getCiclosInicio() {
		return ciclosInicio;
	}

	public void setCiclosInicio(long ciclos) {
		this.ciclosInicio = ciclos;
	}

	public Long obterTempoVolta() {
		if (tempoNumero != null) {
			return tempoNumero;
		}
		return ((ciclosFim - ciclosInicio) - tempoPausado);
	}

	public String getTempoVoltaFormatado() {
		Long fullnum = obterTempoVolta();
		return ControleEstatisticas.formatarTempo(fullnum);
	}

	public boolean isVoltaSafetyCar() {
		return voltaSafetyCar;
	}

	public void setVoltaSafetyCar(boolean voltaSAfetyCar) {
		this.voltaSafetyCar = voltaSAfetyCar;
	}

	public long getTempoPausado() {
		return tempoPausado;
	}

	public void setTempoPausado(long tempoPausado) {
		this.tempoPausado = tempoPausado;
	}
	
	public Long getTempoNumero() {
		tempoNumero = obterTempoVolta();
		if (tempoNumero == null) {
			tempoNumero = new Long(0);
		}
		return tempoNumero;
	}

	public void setTempoNumero(Long tempoNumero) {
		this.tempoNumero = tempoNumero;
	}
}
