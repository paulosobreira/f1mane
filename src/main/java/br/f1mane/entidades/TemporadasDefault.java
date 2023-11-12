package br.f1mane.entidades;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TemporadasDefault implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean trocaPneu = Boolean.FALSE;
	private Boolean reabastecimento = Boolean.FALSE;
	private Boolean ers = Boolean.FALSE;
	private Boolean drs = Boolean.FALSE;
	private Boolean safetyCar = Boolean.TRUE;
	private Double fatorBox;
	private List<Piloto> pilotos;

	public Boolean getTrocaPneu() {
		return trocaPneu;
	}
	public void setTrocaPneu(Boolean trocaPneu) {
		this.trocaPneu = trocaPneu;
	}
	public Boolean getReabastecimento() {
		return reabastecimento;
	}
	public void setReabastecimento(Boolean reabastecimento) {
		this.reabastecimento = reabastecimento;
	}
	public Boolean getErs() {
		return ers;
	}
	public void setErs(Boolean ers) {
		this.ers = ers;
	}
	public Boolean getDrs() {
		return drs;
	}
	public void setDrs(Boolean drs) {
		this.drs = drs;
	}
	public List<Piloto> getPilotos() {
		return pilotos;
	}
	public void setPilotos(List<Piloto> pilotos) {
		this.pilotos = pilotos;
	}
	public Double getFatorBox() {
		return fatorBox;
	}
	public void setFatorBox(Double fatorBox) {
		this.fatorBox = fatorBox;
	}
	public Boolean getSafetyCar() {
		return safetyCar;
	}
	public void setSafetyCar(Boolean safetyCar) {
		this.safetyCar = safetyCar;
	}

}
