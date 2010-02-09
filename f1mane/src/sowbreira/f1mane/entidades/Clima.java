package sowbreira.f1mane.entidades;

import java.io.Serializable;

import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 06/05/2007 as 12:08:07
 */
public class Clima implements Serializable {

	private static final long serialVersionUID = -8243527950668384429L;
	public final static String SOL = "sol.gif";
	public final static String CHUVA = "chuva.gif";
	public final static String NUBLADO = "nublado.gif";
	public final static String ALEATORIO = "ALEATORIO";
	private String clima;

	public Clima(String clima) {
		this.clima = clima;
	}

	public String getClima() {
		return clima;
	}

	public void setClima(String clima) {
		this.clima = clima;
	}

	@Override
	public boolean equals(Object obj) {
		Clima c = (Clima) obj;
		return clima.equals(c.getClima());
	}

	@Override
	public int hashCode() {
		return clima.hashCode();
	}

	public String toString() {
		if (SOL.equals(clima)) {
			return Lang.msg("Ensolarado");
		} else if (NUBLADO.equals(clima)) {
			return Lang.msg("Nublado");
		} else if (CHUVA.equals(clima)) {
			return Lang.msg("Chovendo");
		} else if (ALEATORIO.equals(clima)) {
			return Lang.msg("Aleatorio");
		}

		return super.toString();
	}
}
