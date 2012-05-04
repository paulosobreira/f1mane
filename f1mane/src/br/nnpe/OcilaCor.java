package br.nnpe;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class OcilaCor {

	private int min;
	private int max;
	private int alpha;
	private boolean sobe;
	private int r, g, b;
	public static Map<String, OcilaCor> ocilacoes = new HashMap<String, OcilaCor>();

	public static Color geraOcila(String chave) {
		return geraOcila(chave, Color.WHITE);
	}

	public static Color geraOcila(String chave, Color cor) {
		OcilaCor ocilaCor = ocilacoes.get(chave);
		if (ocilaCor == null) {
			ocilaCor = prepara(chave, cor, 100, 200);
		}
		return ocilaCor.ocila();
	}

	public static OcilaCor prepara(String chave, Color cor, int min, int max) {
		OcilaCor ocilaCor = new OcilaCor(min, max, cor.getAlpha(),
				cor.getRed(), cor.getGreen(), cor.getBlue());
		ocilacoes.put(chave, ocilaCor);
		return ocilaCor;
	}

	private Color ocila() {
		if (sobe) {
			if (alpha < max) {
				alpha+=10;
			} else {
				sobe = false;
			}
		} else {
			if (alpha > min) {
				alpha-=10;
			} else {
				sobe = true;
			}
		}
		return new Color(r, g, b, alpha);
	}

	public OcilaCor(int min, int max, int alpha, int r, int g, int b) {
		super();
		if (alpha < min) {
			alpha = min;
		}
		if (alpha > max) {
			alpha = max;
		}
		this.min = min;
		this.max = max;
		this.alpha = alpha;
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public boolean isSobe() {
		return sobe;
	}

	public void setSobe(boolean sobe) {
		this.sobe = sobe;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	public int getAlpha() {
		return alpha;
	}

	public void setAlpha(int alpha) {
		this.alpha = alpha;
	}

}
