package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 17/08/2007 as 20:25:54
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Posis implements Serializable {

	private int idPiloto, idNo, tracado;
	private String status;
	private boolean humano;
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
			string = Util.extrairNumeros(string);
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

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
