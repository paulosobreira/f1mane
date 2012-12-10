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
				alpha += 20;
			} else {
				sobe = false;
			}
		} else {
			if (alpha > min) {
				alpha -= 20;
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

	public static Color porcentVerde100Vermelho0(int porcent) {
		int r, g;
		if (porcent < 1) {
			porcent = 1;
		}
		if (porcent > 99) {
			porcent = 99;
		}
		r = 510 * (100 - porcent) / 100;
		if (r < 255) {
			g = 255;
		} else {
			g = 255 * (porcent + 25) / 100;
			r = 255;
		}
		return new Color(r, g, 0, 150);
	}

	public static void main(String[] args) {
		for (int i = 100; i > 0; i--) {
			System.out.println(i + " " + porcentVerde100Vermelho0(i));
		}
	}

	public static Color porcentVermelho100Verde0(int porcent) {
		int r, g;
		if (porcent < 1) {
			porcent = 1;
		}
		if (porcent > 99) {
			porcent = 99;
		}
		g = 510 * (100 - porcent) / 100;
		if (g < 255) {
			r = 255;
		} else {
			r = 255 * (porcent + 25) / 100;
			g = 255;
		}
		return new Color(r, g, 0, 150);
	}
}
