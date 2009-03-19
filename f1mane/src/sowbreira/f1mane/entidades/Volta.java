package sowbreira.f1mane.entidades;

import java.io.Serializable;

import java.sql.Time;
import java.sql.Timestamp;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import sowbreira.f1mane.controles.ControleEstatisticas;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 16:03:49
 */
public class Volta implements Serializable {
	private long ciclosInicio;
	private long ciclosFim;
	private int pilotoId;

	public String encode() {
		return ciclosInicio + "_" + ciclosFim + "_" + pilotoId;
	}

	public void decode(String val) {
		String[] sp = val.split("_");
		ciclosInicio = Long.parseLong(sp[0]);
		ciclosFim = Long.parseLong(sp[1]);
		pilotoId = Integer.parseInt(sp[2]);
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
		return ((ciclosFim - ciclosInicio));
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
		System.out.println(volta.obterTempoVoltaFormatado());

		// System.out.prlongln("Min " + minu);
		// System.out.prlongln("Segs " + seg);
		// System.out.prlongln("mili " + mili);
	}
}
