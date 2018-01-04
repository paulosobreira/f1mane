package br.nnpe;

import java.awt.Color;

import javax.swing.JFrame;

public class Constantes {

	public static final String DATA_FORMATO = "dd/MM/yyyy";
	public static final String DATA_FORMATO_YYYYMMDD = "yyyy-MM-dd";
	public static final String DATA_FORMATO_COMPLETO = "yyyy-MM-dd HH:mm:ss";
	public static final String DATA_FORMATO_DDMMYYYY = "ddMMyyyy";

	public static final int MAX_VOLTAS = 72;
	public static final int MIN_VOLTAS = 14;
	public static final int CICLO = 180;
	public static final int CICLO_SOM = 100;
	public static final int ACIMA_MEDIA_NORMAL = 5;
	public static final int ACIMA_MEDIA_FACIL = 10;
	public static final int LATENCIA_MAX = 250;
	public static final int LATENCIA_MIN = 50;
	public static final int LIMITE_DRS = 300;
	public static final Integer SEGUNDOS_PARA_INICIAR_CORRRIDA = Logger.ativo
			? 10
			: 90;
	public static int TAMANHO_RETA_DRS = 1500;

	private Constantes() {
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(200, 200);
		frame.getContentPane()
				.setBackground(new Color(200, 200, 200, 500 / 100));
		frame.setVisible(true);
	}
}
