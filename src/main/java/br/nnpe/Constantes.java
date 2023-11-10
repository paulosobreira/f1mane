package br.nnpe;

public final class Constantes {

	public static final String DATA_FORMATO = "dd/MM/yyyy";
	public static final int MAX_VOLTAS = 72;
	public static final int MIN_VOLTAS = Logger.ativo ? 2 : 12;
	public static final boolean DATABASE = true;
	public static final int CICLO = 180;
	public static final int CICLO_SOM = 100;
	public static final int ACIMA_MEDIA_NORMAL = 500;
	public static final int ACIMA_MEDIA_FACIL = 1000;
	public static final int LATENCIA_MAX = 250;
	public static final int LATENCIA_MIN = 50;
	public static final int LIMITE_DRS = 300;
	public static final Integer SEGUNDOS_PARA_INICIAR_CORRRIDA = Integer.valueOf(Logger.ativo
            ? 30
            : 60);
	public static final double VELOCIDADE_PISTA = 1.5;
	public static final int TAMANHO_RETA_DRS = 1500;

	private Constantes() {
	}

}
