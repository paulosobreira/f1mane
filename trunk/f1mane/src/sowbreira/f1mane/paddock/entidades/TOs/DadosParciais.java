package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 21/08/2007 as 21:08:26
 */
public class DadosParciais implements Serializable {
	public int voltaAtual;
	public int pselCombust;
	public String pselTpPneus;
	public int pselCombustBox;
	public String pselTpPneusBox;
	public String pselModoPilotar;
	public int pselVelocidade;
	public int pselPneus;
	public int pselMotor;
	public int pselParadas;
	public int pselGiro;
	public int pselStress;
	public int cargaKers;
	public int temperaturaMotor;
	public int pselDurAereofolio;
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
	public long[] pilotsPonts = new long[24];
	public long[] pilotsTs = new long[24];

	public void decode(String val) {
		String[] sp = val.split("@");
		int spcont = 0;
		voltaAtual = parseInt(sp[spcont++]);
		pselCombust = parseInt(sp[spcont++]);
		pselTpPneus = decodeTpPneu(sp[spcont++]);
		pselCombustBox = parseInt(sp[spcont++]);
		pselTpPneusBox = decodeTpPneu(sp[spcont++]);
		pselVelocidade = parseInt(sp[spcont++]);
		pselPneus = parseInt(sp[spcont++]);
		pselMotor = parseInt(sp[spcont++]);
		pselParadas = parseInt(sp[spcont++]);
		pselGiro = parseInt(sp[spcont++]);
		pselStress = parseInt(sp[spcont++]);
		cargaKers = parseInt(sp[spcont++]);
		temperaturaMotor = parseInt(sp[spcont++]);
		pselDurAereofolio = parseInt(sp[spcont++]);
		pselModoPilotar = decodeModoPilotar(sp[spcont++]);
		pselBox = "S".equals(sp[spcont++]);
		pselMaxPneus = parseInt(sp[spcont++]);
		clima = decodeClima(sp[spcont++]);
		estado = sp[spcont++];
		dano = decodeDano(sp[spcont++]);
		pselAsa = decodeAsa(sp[spcont++]);
		pselAsaBox = decodeAsa(sp[spcont++]);
		melhorVolta = new Volta();
		melhorVolta.decode(sp[spcont++]);
		peselMelhorVolta = new Volta();
		peselMelhorVolta.decode(sp[spcont++]);
		int contJogador = spcont++;
		nomeJogador = (sp[contJogador] == null || "".equals(sp[contJogador]) ? null
				: sp[contJogador]);
		int contTexto = spcont++;
		if (sp[contTexto] != null && !"".equals(sp[contTexto])) {
			texto = Lang.decodeTexto(sp[contTexto]);
		}
		peselUltima1 = new Volta();
		peselUltima1.decode(sp[spcont++]);
		peselUltima2 = new Volta();
		peselUltima2.decode(sp[spcont++]);
		peselUltima3 = new Volta();
		peselUltima3.decode(sp[spcont++]);
		peselUltima4 = new Volta();
		peselUltima4.decode(sp[spcont++]);
		peselUltima5 = new Volta();
		peselUltima5.decode(sp[spcont++]);
		String[] pts = sp[spcont++].split("§");
		for (int i = 0; i < pts.length; i++) {
			pilotsPonts[i] = parseInt(pts[i]);
		}
		pts = sp[spcont].split("§");
		for (int i = 0; i < pts.length; i++) {
			pilotsTs[i] = parseLong(pts[i]);
		}
	}

	private String decodeModoPilotar(String string) {
		String codpselModoPilotar = "";
		if ("L".equals(string)) {
			codpselModoPilotar = Piloto.LENTO;
		} else if ("N".equals(string)) {
			codpselModoPilotar = Piloto.NORMAL;
		} else if ("A".equals(string)) {
			codpselModoPilotar = Piloto.AGRESSIVO;
		}
		return codpselModoPilotar;
	}

	private int parseInt(String string) {
		try {
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	private long parseLong(String string) {
		try {
			return Long.parseLong(string);
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
		} else if (Carro.TIPO_PNEU_DURO.equals(pselTpPneus)) {
			codPneu = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneus)) {
			codPneu = "M";
		}
		String codPneuBox = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(pselTpPneusBox)) {
			codPneuBox = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(pselTpPneusBox)) {
			codPneuBox = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneusBox)) {
			codPneuBox = "M";
		}
		String codClima = "";
		if (Clima.CHUVA.equals(clima)) {
			codClima = "C";
		} else if (Clima.NUBLADO.equals(clima)) {
			codClima = "N";
		} else if (Clima.SOL.equals(clima)) {
			codClima = "S";
		} else if (Clima.ALEATORIO.equals(clima)) {
			codClima = "A";
		}
		String codDano = "";
		if (Carro.ABANDONOU.equals(dano)) {
			codDano = "A";
		} else if (Carro.BATEU_FORTE.equals(dano)) {
			codDano = "B";
		} else if (Carro.EXPLODIU_MOTOR.equals(dano)) {
			codDano = "E";
		} else if (Carro.PERDEU_AEREOFOLIO.equals(dano)) {
			codDano = "P";
		} else if (Carro.PNEU_FURADO.equals(dano)) {
			codDano = "F";
		} else if (Carro.PANE_SECA.equals(dano)) {
			codDano = "S";
		}
		String codpselAsa = "";
		if (Carro.ASA_NORMAL.equals(pselAsa)) {
			codpselAsa = "N";
		} else if (Carro.MAIS_ASA.equals(pselAsa)) {
			codpselAsa = "A";
		} else if (Carro.MENOS_ASA.equals(pselAsa)) {
			codpselAsa = "M";
		}
		String codpselAsaBox = "";
		if (Carro.ASA_NORMAL.equals(pselAsaBox)) {
			codpselAsaBox = "N";
		} else if (Carro.MAIS_ASA.equals(pselAsaBox)) {
			codpselAsaBox = "A";
		} else if (Carro.MENOS_ASA.equals(pselAsaBox)) {
			codpselAsaBox = "M";
		}
		String codpselModoPilotar = "";
		if (Piloto.LENTO.equals(pselModoPilotar)) {
			codpselModoPilotar = "L";
		} else if (Piloto.NORMAL.equals(pselModoPilotar)) {
			codpselModoPilotar = "N";
		} else if (Piloto.AGRESSIVO.equals(pselModoPilotar)) {
			codpselModoPilotar = "A";
		}

		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < pilotsPonts.length; i++) {
			stringBuffer.append(pilotsPonts[i]);
			stringBuffer.append("§");
		}
		String lessLastPipe = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);

		stringBuffer = new StringBuffer();
		for (int i = 0; i < pilotsTs.length; i++) {
			stringBuffer.append(pilotsTs[i]);
			stringBuffer.append("§");
		}
		String lessLastPipe2 = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);

		if (texto != null && !"".equals(texto)) {
			texto = texto.replaceAll("@", "");
			texto = texto.replaceAll("§", "");
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
				+ pselGiro + "@" + pselStress + "@" + cargaKers + "@"
				+ temperaturaMotor + "@" + pselDurAereofolio + "@"
				+ codpselModoPilotar + "@" + (pselBox ? "S" : "N") + "@"
				+ pselMaxPneus + "@" + codClima + "@" + estado + "@" + codDano
				+ "@" + codpselAsa + "@" + codpselAsaBox + "@" + codMelhorVolta
				+ "@" + codpeselMelhorVolta + "@"
				+ (nomeJogador == null ? "" : nomeJogador) + "@"
				+ (texto == null ? "" : texto) + "@" + codUlt1 + "@" + codUlt2
				+ "@" + codUlt3 + "@" + codUlt4 + "@" + codUlt5 + "@"
				+ lessLastPipe + "@" + lessLastPipe2;
		// Logger.logar(enc);
		return enc;

	};

	public static void main(String[] args) {

		DadosParciais dadosParciais = new DadosParciais();
		dadosParciais.texto = "<table>    <tr>        <td>        </td>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Número 8</font>        </td >                <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Número 7</font>        </td>           <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Número 6</font>        </td>               </tr>    <tr>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >M.Webber 9</font>        </td>        <td>        <font face='sans-serif' >2:52.656</font>        </td>                <td>        <font face='sans-serif' >1:25.250</font>        </td>           <td>        <font face='sans-serif' >1:26.032</font>        </td>               </tr>    <tr>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >F.Alonso 10</font>        </td>        <td>        <font face='sans-serif' >2:18.562</font>        </td>                <td>        <font face='sans-serif' >1:44.969</font>        </td>           <td>        <font face='sans-serif' >1:40.656</font>        </td>               </tr>        <tr>        <td>        </td>        <td bgcolor='#80FF00'>        <font face='sans-serif' >-34.-094</font>        </td>                <td bgcolor='#FFFF00'>        <font face='sans-serif' >19.719</font>        </td>           <td bgcolor='#FFFF00'>        <font face='sans-serif' >14.624</font>        </td>               </tr>     </table>";
		dadosParciais.encode();
		// dadosParciais
		// .decode("4@14557@M@0@@308@11196@48328@0@5@N@23938@S@12@@N@@1240781962625§1240782037063§2@1240781962625§1240782037063§2@Sow@<table>
		// <tr> <td> </td> <td bgcolor='#E0E0E0'> <font face='sans-serif' >Volta
		// Número 8</font> </td > <td bgcolor='#E0E0E0'> <font face='sans-serif'
		// >Volta Número 7</font> </td> <td bgcolor='#E0E0E0'> <font
		// face='sans-serif' >Volta Número 6</font> </td> </tr> <tr> <td
		// bgcolor='#E0E0E0'> <font face='sans-serif' >M.Webber 9</font> </td>
		// <td> <font face='sans-serif' >2:52.656</font> </td> <td> <font
		// face='sans-serif' >1:25.250</font> </td> <td> <font face='sans-serif'
		// >1:26.032</font> </td> </tr> <tr> <td bgcolor='#E0E0E0'> <font
		// face='sans-serif' >F.Alonso 10</font> </td> <td> <font
		// face='sans-serif' >2:18.562</font> </td> <td> <font face='sans-serif'
		// >1:44.969</font> </td> <td> <font face='sans-serif' >1:40.656</font>
		// </td> </tr> <tr> <td> </td> <td bgcolor='#80FF00'> <font
		// face='sans-serif' >-34.-094</font> </td> <td bgcolor='#FFFF00'> <font
		// face='sans-serif' >19.719</font> </td> <td bgcolor='#FFFF00'> <font
		// face='sans-serif' >14.624</font> </td> </tr>
		// </table>@1240781962625§1240782037063§2@1240781886704§1240781962625§2@1240781811157§1240781886704§2@1240781735344§1240781811157§2@@6549§7279§6547§6934§7087§7070§6859§6696§6354§6846§6929§5003§6418§6398§4770§6593§6302§6965§6372§7139§0§0");
		Logger.logar("asd§qwe§zxc§tyu".split("§")[2]);
	}
}
