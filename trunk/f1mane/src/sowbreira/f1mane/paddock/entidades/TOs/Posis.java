package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 17/08/2007 as 20:25:54
 */
public class Posis implements Serializable {

	public int idPiloto, idNo, tracado, volta;
	public boolean agressivo;
	public boolean humano;
	public boolean autoPos;

	public String encode() {
		return idPiloto + "!" + idNo + "!" + (agressivo ? "S" : "N") + "!"
				+ (humano ? "S" : "N") + "!" + tracado + "!" + "!" + volta
				+ "!" + (autoPos ? "S" : "N");
	}

	public void decode(String val) {
		String[] sp = val.split("!");
		idPiloto = parseInt(sp[0]);
		idNo = parseInt(sp[1]);
		agressivo = "S".equals(sp[2]);
		humano = "S".equals(sp[3]);
		tracado = parseInt(sp[4]);
		volta = parseInt(sp[5]);
		autoPos = "S".equals(sp[6]);
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}
}
