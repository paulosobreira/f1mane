package sowbreira.f1mane.entidades;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CircuitosDefauts {

	private String nome;
	private String arquivo;
	private Integer probalidadeChuva;
	@JsonIgnore
	private Integer voltas = 16;
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
		return voltas;
	}
	public void setVoltas(int voltas) {
		this.voltas = voltas;
	}
	public int getProbalidadeChuva() {
		return probalidadeChuva;
	}
	public void setProbalidadeChuva(int probalidadeChuva) {
		this.probalidadeChuva = probalidadeChuva;
	}

}
