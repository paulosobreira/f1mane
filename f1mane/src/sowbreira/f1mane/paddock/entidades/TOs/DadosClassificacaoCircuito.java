package sowbreira.f1mane.paddock.entidades.TOs;

public class DadosClassificacaoCircuito {
	private String nome;
	private String imagemJogador;
	private Long pontos = new Long(0);
	private Long corridas = new Long(0);
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
	public Long getPontos() {
		return pontos;
	}
	public void setPontos(Long pontos) {
		this.pontos = pontos;
	}
	public Long getCorridas() {
		return corridas;
	}
	public void setCorridas(Long corridas) {
		this.corridas = corridas;
	}

}
