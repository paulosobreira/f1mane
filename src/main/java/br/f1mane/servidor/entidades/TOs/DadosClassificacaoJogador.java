package br.f1mane.servidor.entidades.TOs;

public class DadosClassificacaoJogador {
	private String nome;
	private String imagemJogador;
	private Integer pontos = new Integer(0);
	private Integer corridas = new Integer(0);
	public String getNome() {
		return nome;
	}
	public void setNome(String nome) {
		this.nome = nome;
	}
	public String getImagemJogador() {
		return imagemJogador;
	}
	public void setImagemJogador(String imagemJogador) {
		this.imagemJogador = imagemJogador;
	}
	public Integer getPontos() {
		return pontos;
	}
	public void setPontos(Integer pontos) {
		this.pontos = pontos;
	}
	public Integer getCorridas() {
		return corridas;
	}
	public void setCorridas(Integer corridas) {
		this.corridas = corridas;
	}

}
