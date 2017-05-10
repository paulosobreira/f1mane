package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @author Paulo Sobreira Criado em 17/08/2007 as 20:25:54
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Posis implements Serializable {

	public int idPiloto, idNo, tracado;
	public String status;
	public boolean humano;
	public String encode() {
		return idPiloto + "!" + idNo + "!" + status + "!" + (humano ? "S" : "N")
				+ "!" + tracado;
	}

	public void decode(String val) {
		String[] sp = val.split("!");
		idPiloto = parseInt(sp[0]);
		idNo = parseInt(sp[1]);
		status = sp[2];
		humano = "S".equals(sp[3]);
		tracado = parseInt(sp[4]);
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	public int getIdPiloto() {
		return idPiloto;
	}

	public void setIdPiloto(int idPiloto) {
		this.idPiloto = idPiloto;
	}

	public int getIdNo() {
		return idNo;
	}

	public void setIdNo(int idNo) {
		this.idNo = idNo;
	}

	public int getTracado() {
		return tracado;
	}

	public void setTracado(int tracado) {
		this.tracado = tracado;
	}

	public boolean isHumano() {
		return humano;
	}

	public void setHumano(boolean humano) {
		this.humano = humano;
	}

}
