package br.nnpe;

import java.awt.Color;

import javax.swing.JFrame;

public class Constantes {

	public static final String DATA_FORMATO = "dd/MM/yyyy";
	public static final String DATA_FORMATO_YYYYMMDD = "yyyy-MM-dd";
	public static final String DATA_FORMATO_COMPLETO = "yyyy-MM-dd HH:mm:ss";
	public static final String DATA_FORMATO_DDMMYYYY = "ddMMyyyy";

	public static final int MAX_VOLTAS = 72;
	public static final int MIN_VOLTAS = 12;
	public static final int CICLO = 140;
	public static final int ACIMA_MEDIA_NORMAL = 5;
	public static final int ACIMA_MEDIA_FACIL = 10;
	public static final int LATENCIA_MAX = 240;
	public static final int LATENCIA_MIN = 120;
	public static final int LIMITE_DRS = 300;

	private Constantes() {
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(200, 200);
		frame.getContentPane().setBackground(
				new Color(200, 200, 200, 500 / 100));
		frame.setVisible(true);
	}
}
