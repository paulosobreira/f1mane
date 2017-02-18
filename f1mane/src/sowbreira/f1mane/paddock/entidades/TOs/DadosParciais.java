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
	private static final long serialVersionUID = 4430749703769933486L;
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
	public int idTravadaRoda;
	public boolean pselBox;
	public boolean freiandoReta;
	public boolean podeUsarDRS;
	public String pselTpPneusFrente;
	public String pselTpPneusAtras;
	public String clima;
	public String estado;
	public String dano;
	public String pselAsa;
	public String pselAsaBox;
	public Long melhorVolta;
	public Long peselMelhorVolta;
	public Long peselUltima1;
	public Long peselUltima2;
	public Long peselUltima3;
	public Long peselUltima4;
	public Long peselUltima5;
	public String nomeJogador;
	public String texto;
	public String vantagem;
	public String[] statusPilotos = new String[24];

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
		idTravadaRoda = parseInt(sp[spcont++]);
		pselModoPilotar = decodeModoPilotar(sp[spcont++]);
		pselBox = "S".equals(sp[spcont++]);
		freiandoReta = "S".equals(sp[spcont++]);
		podeUsarDRS = "S".equals(sp[spcont++]);
		pselTpPneusFrente = decodeTpPneu(sp[spcont++]);
		pselTpPneusAtras = decodeTpPneu(sp[spcont++]);
		clima = decodeClima(sp[spcont++]);
		estado = sp[spcont++];
		dano = decodeDano(sp[spcont++]);
		pselAsa = decodeAsa(sp[spcont++]);
		pselAsaBox = decodeAsa(sp[spcont++]);
		melhorVolta = parseLong(sp[spcont++]);
		peselMelhorVolta = parseLong(sp[spcont++]);
		int contJogador = spcont++;
		nomeJogador = (sp[contJogador] == null || "".equals(sp[contJogador])
				? null
				: sp[contJogador]);
		int contTexto = spcont++;
		if (sp[contTexto] != null && !"".equals(sp[contTexto])) {
			texto = Lang.decodeTexto(sp[contTexto]);
		}
		vantagem = sp[spcont++];
		peselUltima1 = parseLong(sp[spcont++]);
		peselUltima2 = parseLong(sp[spcont++]);
		peselUltima3 = parseLong(sp[spcont++]);
		peselUltima4 = parseLong(sp[spcont++]);
		peselUltima5 = parseLong(sp[spcont++]);
		String[] pts = sp[spcont++].split("µ");
		for (int i = 0; i < pts.length; i++) {
			statusPilotos[i] = pts[i];
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

		String codPneuFrente = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(pselTpPneusFrente)) {
			codPneuFrente = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(pselTpPneusFrente)) {
			codPneuFrente = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneusFrente)) {
			codPneuFrente = "M";
		}

		String codPneuAtras = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(pselTpPneusAtras)) {
			codPneuAtras = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(pselTpPneusAtras)) {
			codPneuAtras = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(pselTpPneusAtras)) {
			codPneuAtras = "M";
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
		for (int i = 0; i < statusPilotos.length; i++) {
			stringBuffer.append(statusPilotos[i]);
			stringBuffer.append("µ");
		}
		String lessLastPipe = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);

		String lessLastPipe2 = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);

		if (texto != null && !"".equals(texto)) {
			texto = texto.replaceAll("@", "");
			texto = texto.replaceAll("µ", "");
		}

		String enc = voltaAtual + "@" + pselCombust + "@" + codPneu + "@"
				+ pselCombustBox + "@" + codPneuBox + "@" + pselVelocidade + "@"
				+ pselPneus + "@" + pselMotor + "@" + pselParadas + "@"
				+ pselGiro + "@" + pselStress + "@" + cargaKers + "@"
				+ temperaturaMotor + "@" + pselDurAereofolio + "@"
				+ idTravadaRoda + "@" + codpselModoPilotar + "@"
				+ (pselBox ? "S" : "N") + "@" + (freiandoReta ? "S" : "N") + "@"
				+ (podeUsarDRS ? "S" : "N") + "@" + codPneuFrente + "@"
				+ codPneuAtras + "@" + codClima + "@" + estado + "@" + codDano
				+ "@" + codpselAsa + "@" + codpselAsaBox + "@" + melhorVolta
				+ "@" + peselMelhorVolta + "@"
				+ (nomeJogador == null ? "" : nomeJogador) + "@"
				+ (texto == null ? "" : texto) + "@"
				+ (vantagem == null ? "" : vantagem) + "@" + peselUltima1 + "@"
				+ peselUltima2 + "@" + peselUltima3 + "@" + peselUltima4 + "@"
				+ peselUltima5 + "@" + lessLastPipe + "@" + lessLastPipe2;
		// Logger.logar(enc);
		return enc;

	};

	public static void main(String[] args) {

		DadosParciais dadosParciais = new DadosParciais();
		dadosParciais.texto = "<table>    <tr>        <td>        </td>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Nµmero 8</font>        </td >                <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Nµmero 7</font>        </td>           <td bgcolor='#E0E0E0'>        <font face='sans-serif' >Volta Nµmero 6</font>        </td>               </tr>    <tr>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >M.Webber 9</font>        </td>        <td>        <font face='sans-serif' >2:52.656</font>        </td>                <td>        <font face='sans-serif' >1:25.250</font>        </td>           <td>        <font face='sans-serif' >1:26.032</font>        </td>               </tr>    <tr>        <td bgcolor='#E0E0E0'>        <font face='sans-serif' >F.Alonso 10</font>        </td>        <td>        <font face='sans-serif' >2:18.562</font>        </td>                <td>        <font face='sans-serif' >1:44.969</font>        </td>           <td>        <font face='sans-serif' >1:40.656</font>        </td>               </tr>        <tr>        <td>        </td>        <td bgcolor='#80FF00'>        <font face='sans-serif' >-34.-094</font>        </td>                <td bgcolor='#FFFF00'>        <font face='sans-serif' >19.719</font>        </td>           <td bgcolor='#FFFF00'>        <font face='sans-serif' >14.624</font>        </td>               </tr>     </table>";
		dadosParciais.encode();
		// dadosParciais
		// .decode("4@14557@M@0@@308@11196@48328@0@5@N@23938@S@12@@N@@1240781962625µ1240782037063µ2@1240781962625µ1240782037063µ2@Sow@<table>
		// <tr> <td> </td> <td bgcolor='#E0E0E0'> <font face='sans-serif' >Volta
		// Nµmero 8</font> </td > <td bgcolor='#E0E0E0'> <font face='sans-serif'
		// >Volta Nµmero 7</font> </td> <td bgcolor='#E0E0E0'> <font
		// face='sans-serif' >Volta Nµmero 6</font> </td> </tr> <tr> <td
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
		// </table>@1240781962625µ1240782037063µ2@1240781886704µ1240781962625µ2@1240781811157µ1240781886704µ2@1240781735344µ1240781811157µ2@@6549µ7279µ6547µ6934µ7087µ7070µ6859µ6696µ6354µ6846µ6929µ5003µ6418µ6398µ4770µ6593µ6302µ6965µ6372µ7139µ0µ0");
		Logger.logar("asdµqweµzxcµtyu".split("µ")[2]);
	}
}
