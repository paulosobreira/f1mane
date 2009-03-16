package sowbreira.f1mane.entidades;

import java.io.Serializable;

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

	public String toString() {
		if (SOL.equals(clima)) {
			return "Ensolarado";
		} else if (NUBLADO.equals(clima)) {
			return "Nublado";
		} else if (CHUVA.equals(clima)) {
			return "Chovendo";
		} else if (ALEATORIO.equals(clima)) {
			return "Aleatorio";
		}

		return super.toString();
	}
}
