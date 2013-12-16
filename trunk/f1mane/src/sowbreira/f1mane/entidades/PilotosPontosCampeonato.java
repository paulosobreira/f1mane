package sowbreira.f1mane.entidades;

public class PilotosPontosCampeonato {

	private String nome;
	private int pontos;
	private int vitorias;

	public PilotosPontosCampeonato() {

	}

	public PilotosPontosCampeonato(String nome, int pontos, int vitorias) {
		super();
		this.nome = nome;
		this.pontos = pontos;
		this.vitorias = vitorias;
	}

	public int getVitorias() {
		return vitorias;
	}

	public void setVitorias(int vitorias) {
		this.vitorias = vitorias;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PilotosPontosCampeonato) {
			PilotosPontosCampeonato pilotosPontosCampeonato = (PilotosPontosCampeonato) obj;
			if (nome != null)
				return nome.equals(pilotosPontosCampeonato.getNome());
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		if (nome != null) {
			return nome.hashCode();
		}
		return super.hashCode();
	}

}
