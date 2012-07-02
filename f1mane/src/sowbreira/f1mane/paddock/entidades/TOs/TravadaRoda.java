package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import br.nnpe.Util;

public class TravadaRoda implements Serializable {
	private int idNo;
	private int tracado;
	private int tipo;

	public TravadaRoda() {
		if (Math.random() > .9) {
			setTipo(0);
		} else {
			setTipo(Util.intervalo(1, 2));
		}
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}

	public int getTracado() {
		return tracado;
	}

	public void setTracado(int tracado) {
		this.tracado = tracado;
	}

	public int getIdNo() {
		return idNo;
	}

	public void setIdNo(int idNo) {
		this.idNo = idNo;
	}

	@Override
	public String toString() {
		return (idNo + " " + tracado + " " + tipo);
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return toString().equals(obj.toString());
	}

	public String encode() {
		return idNo + "§" + tracado;
	}

	public void decode(String val) {
		if (val == null || "".equals(val)) {
			return;
		}
		String[] sp = val.split("§");
		idNo = parseInt(sp[0]);
		tracado = parseInt(sp[1]);
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}
}
