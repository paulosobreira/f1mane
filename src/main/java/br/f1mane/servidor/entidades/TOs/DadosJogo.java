package br.f1mane.servidor.entidades.TOs;

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
	private List<Piloto> pilotos = null;
	private Volta melhoVolta;
	private Integer voltaAtual;
	private Integer numeroVotas;
	private Boolean corridaTerminada;
	private Boolean corridaIniciada;
	private Boolean drs;
	private Boolean ers;
	private Boolean trocaPneu;
	private Boolean reabastecimento;
	private String clima;
	private String nomeCircuito;
	private String arquivoCircuito;
	private String temporada;
	private Integer idPilotoSelecionado;
	private String nomeJogo;
	private String estado;
	private String segundosParaIniciar;
	private String campeonato;
	private String rodadaCampeonato;

	public List<Piloto> getPilotos() {
		return pilotos;
	}
	public void setPilotos(List<Piloto> pilotosList) {
		this.pilotos = pilotosList;
	}
	public Volta getMelhoVolta() {
		return melhoVolta;
	}
	public void setMelhoVolta(Volta melhoVolta) {
		this.melhoVolta = melhoVolta;
	}
	public Integer getVoltaAtual() {
		return voltaAtual;
	}
	public void setVoltaAtual(Integer voltaAtual) {
		this.voltaAtual = voltaAtual;
	}
	public Integer getNumeroVotas() {
		return numeroVotas;
	}
	public void setNumeroVotas(Integer numeroVotas) {
		this.numeroVotas = numeroVotas;
	}
	public Boolean getCorridaTerminada() {
		return corridaTerminada;
	}
	public void setCorridaTerminada(Boolean corridaTerminada) {
		this.corridaTerminada = corridaTerminada;
	}
	public Boolean getCorridaIniciada() {
		return corridaIniciada;
	}
	public void setCorridaIniciada(Boolean corridaIniciada) {
		this.corridaIniciada = corridaIniciada;
	}
	public Boolean getDrs() {
		return drs;
	}
	public void setDrs(Boolean drs) {
		this.drs = drs;
	}
	public Boolean getErs() {
		return ers;
	}
	public void setErs(Boolean ers) {
		this.ers = ers;
	}
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
	public String getClima() {
		return clima;
	}
	public void setClima(String clima) {
		this.clima = clima;
	}
	public String getNomeCircuito() {
		return nomeCircuito;
	}
	public void setNomeCircuito(String nomeCircuito) {
		this.nomeCircuito = nomeCircuito;
	}
	public String getArquivoCircuito() {
		return arquivoCircuito;
	}
	public void setArquivoCircuito(String arquivoCircuito) {
		this.arquivoCircuito = arquivoCircuito;
	}
	public String getTemporada() {
		return temporada;
	}
	public void setTemporada(String temporada) {
		this.temporada = temporada;
	}
	public Integer getIdPilotoSelecionado() {
		return idPilotoSelecionado;
	}
	public void setIdPilotoSelecionado(Integer idPilotoSelecionado) {
		this.idPilotoSelecionado = idPilotoSelecionado;
	}
	public String getNomeJogo() {
		return nomeJogo;
	}
	public void setNomeJogo(String nomeJogo) {
		this.nomeJogo = nomeJogo;
	}
	public String getEstado() {
		return estado;
	}
	public void setEstado(String estado) {
		this.estado = estado;
	}
	public String getSegundosParaIniciar() {
		return segundosParaIniciar;
	}
	public void setSegundosParaIniciar(String segundosParaIniciar) {
		this.segundosParaIniciar = segundosParaIniciar;
	}
	public String getCampeonato() {
		return campeonato;
	}
	public void setCampeonato(String campeonato) {
		this.campeonato = campeonato;
	}
	public String getRodadaCampeonato() {
		return rodadaCampeonato;
	}
	public void setRodadaCampeonato(String rodadaCampeonato) {
		this.rodadaCampeonato = rodadaCampeonato;
	}

}
