package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 18/08/2007 as 09:24:11
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PosisPack implements Serializable {

	public Posis[] posis;
	public int safetyNoId;
	public int safetyTracado;
	public long time = System.currentTimeMillis();
	public boolean safetySair;

	public String encode() {
		if (posis.length == 0) {
			return null;
		}
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < posis.length; i++) {
			stringBuffer.append(posis[i].encode());
			stringBuffer.append("§");
		}
		String lessLastPipe = stringBuffer.toString().substring(0,
				stringBuffer.toString().length() - 1);
		return safetyNoId + "£" + safetyTracado + "£" + (safetySair ? "S" : "N")
				+ "£" + lessLastPipe;
	}

	public void decode(String val) {
		String[] sp = val.split("£");
		safetyNoId = parseInt(sp[0]);
		safetyTracado = parseInt(sp[1]);
		safetySair = "S".equals(sp[2]);
		String[] posisEnc = sp[3].split("§");
		posis = new Posis[posisEnc.length];
		for (int i = 0; i < posisEnc.length; i++) {
			posis[i] = new Posis();
			posis[i].decode(posisEnc[i]);
		}
	}

	public static void main(String[] args) {
		String val = "0&N&11-1741-S-S,5-1736-S-N,6-1731-S-N,17-1726-S-N,2-1721-S-N,1-1716-S-N,7-1711-S-N,18-1706-S-N,19-1701-S-N,15-1696-S-N,4-1691-S-N,13-1686-S-N,8-1681-S-N,12-1676-S-N,20-1671-S-N,10-1666-S-N,9-1661-S-N,16-1656-S-N,3-1651-S-N,14-1646-S-N,22-1641-S-N,21-1636-S-N";
		PosisPack posisPack = new PosisPack();
		posisPack.decode(val);
		Logger.logar(val.length());
	}

	private int parseInt(String string) {
		try {
			string = Util.extrairNumeros(string);
			return Integer.parseInt(string);
		} catch (Exception e) {
		}
		return 0;
	}

	public Posis[] getPosis() {
		return posis;
	}

	public void setPosis(Posis[] posis) {
		this.posis = posis;
	}

	public int getSafetyNoId() {
		return safetyNoId;
	}

	public void setSafetyNoId(int safetyNoId) {
		this.safetyNoId = safetyNoId;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public boolean isSafetySair() {
		return safetySair;
	}

	public void setSafetySair(boolean safetySair) {
		this.safetySair = safetySair;
	}

	public int getSafetyTracado() {
		return safetyTracado;
	}

	public void setSafetyTracado(int safetyTracado) {
		this.safetyTracado = safetyTracado;
	}

}
