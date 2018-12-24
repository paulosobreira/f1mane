package br.nnpe;

import java.util.Date;

public class Constantes {

	public static final String DATA_FORMATO = "dd/MM/yyyy";
	public static final String DATA_FORMATO_YYYYMMDD = "yyyy-MM-dd";
	public static final String DATA_FORMATO_COMPLETO = "yyyy-MM-dd HH:mm:ss";
	public static final String DATA_FORMATO_DDMMYYYY = "ddMMyyyy";

	public static final int MAX_VOLTAS = 72;
	public static final int MIN_VOLTAS = Logger.ativo ? 5 : 12;
	public static final boolean DATABASE = !Logger.ativo;
	public static final int CICLO = 180;
	public static final int CICLO_SOM = 100;
	public static final int ACIMA_MEDIA_NORMAL = 500;
	public static final int ACIMA_MEDIA_FACIL = 1000;
	public static final int LATENCIA_MAX = 250;
	public static final int LATENCIA_MIN = 50;
	public static final int LIMITE_DRS = 300;
	public static final Integer SEGUNDOS_PARA_INICIAR_CORRRIDA = Logger.ativo
			? 10
			: 30;
	public static final double VELOCIDADE_PISTA = 1.5;
	public static int TAMANHO_RETA_DRS = 1500;

	private Constantes() {
	}

	public static void main(String[] args) {
		System.out.println(new Date(1527814707699l));
	}
}
