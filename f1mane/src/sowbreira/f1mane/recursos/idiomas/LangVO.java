package sowbreira.f1mane.recursos.idiomas;

public class LangVO {
	private String valor;

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public LangVO(String valor) {
		super();
		this.valor = valor;
	}

	public String toString() {
		return Lang.msg(getValor());
	}

	public boolean equals(Object obj) {
		LangVO langVO = (LangVO) obj;
		return valor.equals(langVO.getValor());
	}
}
