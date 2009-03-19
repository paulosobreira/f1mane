package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;

/**
 * @author Paulo Sobreira Criado em 21/08/2007 as 21:08:26
 */
public class DadosParciais implements Serializable {
	public int voltaAtual;
	public int pselCombust;
	public String pselTpPneus;
	public int pselCombustBox;
	public String pselTpPneusBox;
	public int pselVelocidade;
	public int pselPneus;
	public int pselMotor;
	public int pselParadas;
	public int pselGiro;
	public boolean pselBox;
	public int pselMaxPneus;
	public String clima;
	public String estado;
	public String dano;
	public String pselAsa;
	public String pselAsaBox;
	public String nomeJogador;
	public String texto;
	public Volta melhorVolta;
	public Volta peselMelhorVolta;
	public Volta peselUltima;
	public int[] pilotsPonts = new int[22];

	public String encode() {
		String codPneu = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(pselTpPneus)) {
			codPneu = "C";
		}
		if (Carro.TIPO_PNEU_DURO.equals(pselTpPneus)) {
			codPneu = "D";
		}
		if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneus)) {
			codPneu = "M";
		}
		String codPneuBox = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(pselTpPneusBox)) {
			codPneuBox = "C";
		}
		if (Carro.TIPO_PNEU_DURO.equals(pselTpPneusBox)) {
			codPneuBox = "D";
		}
		if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneusBox)) {
			codPneuBox = "M";
		}
		String codClima = "";
		if (Clima.CHUVA.equals(clima)) {
			codClima = "C";
		}
		if (Clima.NUBLADO.equals(clima)) {
			codClima = "N";
		}
		if (Clima.SOL.equals(clima)) {
			codClima = "S";
		}
		if (Clima.ALEATORIO.equals(clima)) {
			codClima = "A";
		}
		String codDano = "";
		if (Carro.ABANDONOU.equals(dano)) {
			codDano = "A";
		}
		if (Carro.BATEU_FORTE.equals(dano)) {
			codDano = "B";
		}
		if (Carro.EXPLODIU_MOTOR.equals(dano)) {
			codDano = "E";
		}
		if (Carro.PERDEU_AEREOFOLIO.equals(dano)) {
			codDano = "P";
		}
		if (Carro.PNEU_FURADO.equals(dano)) {
			codDano = "F";
		}
		if (Carro.PANE_SECA.equals(dano)) {
			codDano = "S";
		}
		String codpselAsa = "";
		if (Carro.ASA_NORMAL.equals(pselAsa)) {
			codpselAsa = "N";
		}
		if (Carro.MAIS_ASA.equals(pselAsa)) {
			codpselAsa = "A";
		}
		if (Carro.MENOS_ASA.equals(pselAsa)) {
			codpselAsa = "M";
		}
		String codpselAsaBox = "";
		if (Carro.ASA_NORMAL.equals(pselAsaBox)) {
			codpselAsaBox = "N";
		}
		if (Carro.MAIS_ASA.equals(pselAsaBox)) {
			codpselAsaBox = "A";
		}
		if (Carro.MENOS_ASA.equals(pselAsaBox)) {
			codpselAsaBox = "M";
		}
		return voltaAtual + "," + pselCombust + "," + codPneu + ","
				+ pselCombustBox + "," + codPneuBox + "," + pselVelocidade
				+ "," + pselPneus + "," + pselMotor + "," + pselParadas + ","
				+ pselGiro + "," + (pselBox ? "S" : "N") + "," + pselMaxPneus
				+ "," + codClima + "," + estado + "," + codDano + ","
				+ codpselAsa + "," + codpselAsaBox;

	};

	public void decode(String val) {

	}

	public static void main(String[] args) {
		System.out.println("asd&qwe&zxc&tyu".split("&")[2]);
	}

}
