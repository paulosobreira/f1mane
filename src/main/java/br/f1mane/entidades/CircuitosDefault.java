package br.f1mane.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CircuitosDefault {

	private String nome;
	private String arquivo;
	private Integer probalidadeChuva;
	@JsonIgnore
	private Integer voltas = Integer.valueOf(16);
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getArquivo() {
		return arquivo;
	}
	public void setArquivo(String arquivo) {
		this.arquivo = arquivo;
	}

	public int getVoltas() {
		return voltas.intValue();
	}
	public void setVoltas(int voltas) {
		this.voltas = Integer.valueOf(voltas);
	}
	public int getProbalidadeChuva() {
		return probalidadeChuva.intValue();
	}
	public void setProbalidadeChuva(int probalidadeChuva) {
		this.probalidadeChuva = Integer.valueOf(probalidadeChuva);
	}

}
