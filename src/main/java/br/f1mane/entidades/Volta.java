package br.f1mane.entidades;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.f1mane.controles.ControleEstatisticas;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 16:03:49
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Volta implements Serializable {
	private int pilotoId;
	@JsonIgnore
	private Long tempoNumero;
	@JsonIgnore
	private Long ciclosInicio = new Long(0);
	@JsonIgnore
	private Long ciclosFim = new Long(0);
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

	public int getPiloto() {
		return pilotoId;
	}

	public void setPiloto(int piloto) {
		this.pilotoId = piloto;
	}

	public long getCiclosFim() {
		return ciclosFim.longValue();
	}

	public void setCiclosFim(long ciclosFim) {
		this.ciclosFim = Long.valueOf(ciclosFim);
	}

	public long getCiclosInicio() {
		return ciclosInicio.longValue();
	}

	public void setCiclosInicio(long ciclos) {
		this.ciclosInicio = Long.valueOf(ciclos);
	}

	public Long obterTempoVolta() {
		if (tempoNumero != null) {
			return tempoNumero;
		}
		return Long.valueOf((ciclosFim.longValue() - ciclosInicio.longValue()) - tempoPausado.longValue());
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
		return tempoPausado.longValue();
	}

	public void setTempoPausado(long tempoPausado) {
		this.tempoPausado = Long.valueOf(tempoPausado);
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
