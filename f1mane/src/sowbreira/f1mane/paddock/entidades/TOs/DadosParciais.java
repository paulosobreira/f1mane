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
	public String tpPneus;
	public int combustBox;
	public String tpPneusBox;
	public String modoPilotar;
	public int velocidade;
	public int pCombust;
	public int pPneus;
	public int pMotor;
	public int paradas;
	public int giro;
	public int stress;
	public int cargaKers;
	public boolean alertaMotor;
	public boolean alertaAerefolio;
	public boolean box;
	public boolean podeUsarDRS;
	public String tpPneusFrente;
	public String tpPneusAtras;
	public String clima;
	public String estado;
	public String dano;
	public String asa;
	public String asaBox;
	public Long melhorVoltaCorrida;
	public Long melhorVolta;
	public Long ultima1;
	public Long ultima2;
	public Long ultima3;
	public Long ultima4;
	public Long ultima5;
	public String nomeJogador;
	public String texto;
	public String vantagem;
	public String[] statusPilotos = new String[24];

	public void decode(String val) {
		String[] sp = val.split("@");
		int spcont = 0;
		voltaAtual = parseInt(sp[spcont++]);
		pCombust = parseInt(sp[spcont++]);
		tpPneus = decodeTpPneu(sp[spcont++]);
		combustBox = parseInt(sp[spcont++]);
		tpPneusBox = decodeTpPneu(sp[spcont++]);
		velocidade = parseInt(sp[spcont++]);
		pPneus = parseInt(sp[spcont++]);
		pMotor = parseInt(sp[spcont++]);
		paradas = parseInt(sp[spcont++]);
		giro = parseInt(sp[spcont++]);
		stress = parseInt(sp[spcont++]);
		cargaKers = parseInt(sp[spcont++]);
		alertaMotor = "S".equals(sp[spcont++]);
		alertaAerefolio = "S".equals(sp[spcont++]);
		modoPilotar = decodeModoPilotar(sp[spcont++]);
		box = "S".equals(sp[spcont++]);
		podeUsarDRS = "S".equals(sp[spcont++]);
		tpPneusFrente = decodeTpPneu(sp[spcont++]);
		tpPneusAtras = decodeTpPneu(sp[spcont++]);
		clima = decodeClima(sp[spcont++]);
		estado = sp[spcont++];
		dano = decodeDano(sp[spcont++]);
		asa = decodeAsa(sp[spcont++]);
		asaBox = decodeAsa(sp[spcont++]);
		melhorVoltaCorrida = parseLong(sp[spcont++]);
		melhorVolta = parseLong(sp[spcont++]);
		int contJogador = spcont++;
		nomeJogador = (sp[contJogador] == null || "".equals(sp[contJogador])
				? null
				: sp[contJogador]);
		int contTexto = spcont++;
		if (sp[contTexto] != null && !"".equals(sp[contTexto])) {
			texto = Lang.decodeTexto(sp[contTexto]);
		}
		vantagem = sp[spcont++];
		ultima1 = parseLong(sp[spcont++]);
		ultima2 = parseLong(sp[spcont++]);
		ultima3 = parseLong(sp[spcont++]);
		ultima4 = parseLong(sp[spcont++]);
		ultima5 = parseLong(sp[spcont++]);
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
		if (Carro.TIPO_PNEU_CHUVA.equals(tpPneus)) {
			codPneu = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(tpPneus)) {
			codPneu = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(tpPneus)) {
			codPneu = "M";
		}

		String codPneuFrente = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(tpPneusFrente)) {
			codPneuFrente = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(tpPneusFrente)) {
			codPneuFrente = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(tpPneusFrente)) {
			codPneuFrente = "M";
		}

		String codPneuAtras = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(tpPneusAtras)) {
			codPneuAtras = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(tpPneusAtras)) {
			codPneuAtras = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(tpPneusAtras)) {
			codPneuAtras = "M";
		}

		String codPneuBox = "";
		if (Carro.TIPO_PNEU_CHUVA.equals(tpPneusBox)) {
			codPneuBox = "C";
		} else if (Carro.TIPO_PNEU_DURO.equals(tpPneusBox)) {
			codPneuBox = "D";
		} else if (Carro.TIPO_PNEU_MOLE.equals(tpPneusBox)) {
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
		if (Carro.ASA_NORMAL.equals(asa)) {
			codpselAsa = "N";
		} else if (Carro.MAIS_ASA.equals(asa)) {
			codpselAsa = "A";
		} else if (Carro.MENOS_ASA.equals(asa)) {
			codpselAsa = "M";
		}
		String codpselAsaBox = "";
		if (Carro.ASA_NORMAL.equals(asaBox)) {
			codpselAsaBox = "N";
		} else if (Carro.MAIS_ASA.equals(asaBox)) {
			codpselAsaBox = "A";
		} else if (Carro.MENOS_ASA.equals(asaBox)) {
			codpselAsaBox = "M";
		}
		String codpselModoPilotar = "";
		if (Piloto.LENTO.equals(modoPilotar)) {
			codpselModoPilotar = "L";
		} else if (Piloto.NORMAL.equals(modoPilotar)) {
			codpselModoPilotar = "N";
		} else if (Piloto.AGRESSIVO.equals(modoPilotar)) {
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

		String enc = voltaAtual + "@" + pCombust + "@" + codPneu + "@"
				+ combustBox + "@" + codPneuBox + "@" + velocidade + "@"
				+ pPneus + "@" + pMotor + "@" + paradas + "@"
				+ giro + "@" + stress + "@" + cargaKers + "@"
				+ (alertaMotor?"S":"N") + "@" + (alertaAerefolio?"S":"N")+ "@"
				+ codpselModoPilotar + "@" + (box ? "S" : "N") + "@"
				+ (podeUsarDRS ? "S" : "N") + "@" + codPneuFrente + "@"
				+ codPneuAtras + "@" + codClima + "@" + estado + "@" + codDano
				+ "@" + codpselAsa + "@" + codpselAsaBox + "@" + melhorVoltaCorrida
				+ "@" + melhorVolta + "@"
				+ (nomeJogador == null ? "" : nomeJogador) + "@"
				+ (texto == null ? "" : texto) + "@"
				+ (vantagem == null ? "" : vantagem) + "@" + ultima1 + "@"
				+ ultima2 + "@" + ultima3 + "@" + ultima4 + "@"
				+ ultima5 + "@" + lessLastPipe + "@" + lessLastPipe2;
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
