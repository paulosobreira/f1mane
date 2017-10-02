package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;

/**
 * @author Paulo Sobreira Criado em 15/08/2007 as 16:33:52
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DadosJogo implements Serializable {
	private List<Piloto> pilotosList = null;
	private Volta melhoVolta;
	private int voltaAtual;
	private int numeroVotas;
	private boolean corridaTerminada;
	private boolean corridaIniciada;
	private boolean drs;
	private boolean ers;
	private boolean trocaPneu;
	private boolean reabastacimento;
	private String texto;
	private String clima;
	private String nomeCircuito;
	private String nomeJogo;

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

	public List<Piloto> getPilotosList() {
		return pilotosList;
	}

	public void setPilotosList(List<Piloto> objects) {
		this.pilotosList = objects;
	}

	public String getNomeJogo() {
		return nomeJogo;
	}

	public void setNomeJogo(String nomeJogo) {
		this.nomeJogo = nomeJogo;
	}

	public boolean isCorridaIniciada() {
		return corridaIniciada;
	}

	public void setCorridaIniciada(boolean corridaIniciada) {
		this.corridaIniciada = corridaIniciada;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
	}

	public boolean isErs() {
		return ers;
	}

	public void setErs(boolean ers) {
		this.ers = ers;
	}

	public boolean isTrocaPneu() {
		return trocaPneu;
	}

	public void setTrocaPneu(boolean trocaPneu) {
		this.trocaPneu = trocaPneu;
	}

	public boolean isReabastacimento() {
		return reabastacimento;
	}

	public void setReabastacimento(boolean reabastacimento) {
		this.reabastacimento = reabastacimento;
	}

	public String getNomeCircuito() {
		return nomeCircuito;
	}

	public void setNomeCircuito(String nomeCircuito) {
		this.nomeCircuito = nomeCircuito;
	}

	public int getNumeroVotas() {
		return numeroVotas;
	}

	public void setNumeroVotas(int numeroVotas) {
		this.numeroVotas = numeroVotas;
	}

}
