package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.Iterator;

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
	public Volta melhorVolta;
	public Volta peselMelhorVolta;
	public Volta peselUltima1;
	public Volta peselUltima2;
	public Volta peselUltima3;
	public Volta peselUltima4;
	public Volta peselUltima5;
	public String nomeJogador;
	public String texto;
	public int[] pilotsPonts = new int[22];

	public void decode(String val) {
		String[] sp = val.split("@");
		voltaAtual = parseInt(sp[0]);
		pselCombust = parseInt(sp[1]);
		pselTpPneus = decodeTpPneu(sp[2]);
		pselCombustBox = parseInt(sp[3]);
		pselTpPneusBox = decodeTpPneu(sp[4]);
		pselVelocidade = parseInt(sp[5]);
		pselPneus = parseInt(sp[6]);
		pselMotor = parseInt(sp[7]);
		pselParadas = parseInt(sp[8]);
		pselGiro = parseInt(sp[9]);
		pselBox = "S".equals(sp[10]);
		pselMaxPneus = parseInt(sp[11]);
		clima = decodeClima(sp[12]);
		estado = sp[13];
		dano = decodeDano(sp[14]);
		pselAsa = decodeAsa(sp[15]);
		pselAsaBox = decodeAsa(sp[16]);
		melhorVolta = new Volta();
		melhorVolta.decode(sp[17]);
		peselMelhorVolta = new Volta();
		peselMelhorVolta.decode(sp[18]);
		nomeJogador = (sp[19] == null || "".equals(sp[19]) ? null : sp[19]);
		texto = sp[20];
		peselUltima1 = new Volta();
		peselUltima1.decode(sp[21]);
		peselUltima2 = new Volta();
		peselUltima2.decode(sp[22]);
		peselUltima3 = new Volta();
		peselUltima3.decode(sp[23]);
		peselUltima4 = new Volta();
		peselUltima4.decode(sp[24]);
		peselUltima5 = new Volta();
		peselUltima5.decode(sp[25]);
		String[] pts = sp[26].split("�");
		for (int i = 0; i < pts.length; i++) {
			pilotsPonts[i] = parseInt(pts[i]);
		}
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	private String decodeAsa(String pselAsa) {
		if ("N".equals(pselAsa)) {
			return Carro.ASA_NORMAL;
		}
		if ("A".equals(pselAsa)) {
			return Carro.MAIS_ASA;
		}
		if ("M".equals(pselAsa)) {
			return Carro.MENOS_ASA;
		}
		return null;
	}

	private String decodeDano(String dano) {
		if ("A".equals(dano)) {
			return Carro.ABANDONOU;
		}
		if ("B".equals(dano)) {
			return Carro.BATEU_FORTE;
		}
		if ("E".equals(dano)) {
			return Carro.EXPLODIU_MOTOR;
		}
		if ("P".equals(dano)) {
			return Carro.PERDEU_AEREOFOLIO;
		}
		if ("F".equals(dano)) {
			return Carro.PNEU_FURADO;
		}
		if ("S".equals(dano)) {
			return Carro.PANE_SECA;
		}
		return "";
	}

	private String decodeClima(String clima) {
		if ("C".equals(clima)) {
			return Clima.CHUVA;
		}
		if ("N".equals(clima)) {
			return Clima.NUBLADO;
		}
		if ("S".equals(clima)) {
			return Clima.SOL;
		}
		if ("A".equals(clima)) {
			return Clima.ALEATORIO;
		}
		return "";
	}

	private String decodeTpPneu(String val) {
		if ("C".equals(val)) {
			return Carro.TIPO_PNEU_CHUVA;
		}
		if ("D".equals(val)) {
			return Carro.TIPO_PNEU_DURO;
		}
		if ("M".equals(val)) {
			return Carro.TIPO_PNEU_MOLE;
		}
		return "";
	}

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
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < pilotsPonts.length; i++) {
			stringBuffer.append(pilotsPonts[i]);
			stringBuffer.append("�");
		}
		String lessLastPipe = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);

		if (texto != null && !"".equals(texto)) {
			texto = texto.replaceAll("@", "");
			texto = texto.replaceAll("�", "");
		}
		String codMelhorVolta = "";
		if (melhorVolta != null) {
			codMelhorVolta = melhorVolta.encode();
		}
		String codpeselMelhorVolta = "";
		if (peselMelhorVolta != null) {
			codpeselMelhorVolta = peselMelhorVolta.encode();
		}

		String codUlt1 = "";
		if (peselUltima1 != null) {
			codUlt1 = peselUltima1.encode();
		}
		String codUlt2 = "";
		if (peselUltima2 != null) {
			codUlt2 = peselUltima2.encode();
		}
		String codUlt3 = "";
		if (peselUltima3 != null) {
			codUlt3 = peselUltima3.encode();
		}
		String codUlt4 = "";
		if (peselUltima4 != null) {
			codUlt4 = peselUltima4.encode();
		}
		String codUlt5 = "";
		if (peselUltima5 != null) {
			codUlt5 = peselUltima5.encode();
		}
		String enc = voltaAtual + "@" + pselCombust + "@" + codPneu + "@"
				+ pselCombustBox + "@" + codPneuBox + "@" + pselVelocidade
				+ "@" + pselPneus + "@" + pselMotor + "@" + pselParadas + "@"
				+ pselGiro + "@" + (pselBox ? "S" : "N") + "@" + pselMaxPneus
				+ "@" + codClima + "@" + estado + "@" + codDano + "@"
				+ codpselAsa + "@" + codpselAsaBox + "@" + codMelhorVolta + "@"
				+ codpeselMelhorVolta + "@"
				+ (nomeJogador == null ? "" : nomeJogador) + "@"
				+ (texto == null ? "" : texto) + "@" + codUlt1 + "@" + codUlt2
				+ "@" + codUlt3 + "@" + codUlt4 + "@" + codUlt5 + "@"
				+ lessLastPipe;
		// System.out.println(enc);
		return enc;

	};

	public static void main(String[] args) {

		DadosParciais dadosParciais = new DadosParciais();
		dadosParciais
				.decode("0@0@@0@@0@0@0@0@0@N@0@S@13@@@@@@null@@-15§-5§-95§-30§-25§-20§-60§-85§-105§-40§0§-35§-45§-100§-50§-75§-10§-55§-65§-70§-80§-90@@@@@");
		System.out.println("asd�qwe�zxc�tyu".split("�")[2]);
	}
}
