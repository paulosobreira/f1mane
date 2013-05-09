package sowbreira.f1mane.entidades;

import java.io.Serializable;

import sowbreira.f1mane.controles.ControleEstatisticas;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 16:03:49
 */
public class Volta implements Serializable {
	private long ciclosInicio;
	private long ciclosFim;
	private long tempoPausado;
	private int pilotoId;
	private boolean voltaBox;
	private boolean voltaSafetyCar;

	public boolean isVoltaBox() {
		return voltaBox;
	}

	public void setVoltaBox(boolean voltaBox) {
		this.voltaBox = voltaBox;
	}

	public String encode() {
		return ciclosInicio + "§" + ciclosFim + "§" + pilotoId + "§"
				+ tempoPausado;
	}

	public void decode(String val) {
		if (val == null || "".equals(val)) {
			return;
		}
		String[] sp = val.split("§");
		ciclosInicio = parseLong(sp[0]);
		ciclosFim = parseLong(sp[1]);
		pilotoId = parseInt(sp[2]);
		tempoPausado = parseInt(sp[3]);
	}

	private long parseLong(String string) {
		try {
			return Long.parseLong(string);
		} catch (Exception e) {
		}
		return 0;
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	public int getPiloto() {
		return pilotoId;
	}

	public void setPiloto(int piloto) {
		this.pilotoId = piloto;
	}

	public long getCiclosFim() {
		return ciclosFim;
	}

	public void setCiclosFim(long ciclosFim) {
		this.ciclosFim = ciclosFim;
	}

	public long getCiclosInicio() {
		return ciclosInicio;
	}

	public void setCiclosInicio(long ciclos) {
		this.ciclosInicio = ciclos;
	}

	public double obterTempoVolta() {
		return ((ciclosFim - ciclosInicio) - tempoPausado);
	}

	public String obterTempoVoltaFormatado() {
		long fullnum = (long) obterTempoVolta();

		return ControleEstatisticas.formatarTempo(fullnum);
	}

	public static void main(String[] args) {
		long fullnum = 85700;
		long minu = (fullnum / 60000);
		long seg = ((fullnum - (minu * 60000)) / 1000);
		long mili = fullnum - ((minu * 60000) + (seg * 1000));

		Volta volta = new Volta();
		volta.decode("10000_20000_23");
		Logger.logar(volta.obterTempoVoltaFormatado());

		// System.out.prlongln("Min " + minu);
		// System.out.prlongln("Segs " + seg);
		// System.out.prlongln("mili " + mili);
	}

	public boolean isVoltaSafetyCar() {
		return voltaSafetyCar;
	}

	public void setVoltaSafetyCar(boolean voltaSAfetyCar) {
		this.voltaSafetyCar = voltaSAfetyCar;
	}

	public long getTempoPausado() {
		return tempoPausado;
	}

	public void setTempoPausado(long tempoPausado) {
		this.tempoPausado = tempoPausado;
	}

}
