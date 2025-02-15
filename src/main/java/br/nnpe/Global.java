package br.nnpe;

public final class Global {

    public static final String DATA_FORMATO = "dd/MM/yyyy";
    public static final int MAX_VOLTAS = 72;
    public static final int MIN_VOLTAS = Logger.ativo ? 1 : 12;
    public static final boolean DATABASE = true;
    public static final int LATENCIA_MAX = 500;
    public static final int LATENCIA_MIN = 100;
    public static final int LIMITE_DRS = 300;
    public static final Integer SEGUNDOS_PARA_INICIAR_CORRRIDA = Integer.valueOf(Logger.ativo
            ? 30
            : 60);
    public static final double VELOCIDADE_PISTA = 1;
    public static final int TAMANHO_RETA_DRS = 1500;
    public static final double LIMITE_ESTRESSE_PARA_RERRAR_CURVA = 90;
    public static int MOD_GANHO_SUAVE = 2;
    public static boolean DESENHA_DIFF_REAL_SUAVE = false;

    public static boolean DEBUG_SEM_CHUVA = false;
    public static final int CARGA_ERS = 100;
    public static final int DURABILIDADE_AREOFOLIO = 5;

    public static final String CONTROLE_AUTOMATICO = "CONTROLE_AUTOMATICO";

    public static final String CONTROLE_MANUAL = "CONTROLE_MANUAL";
    public static boolean setarHints = true;


    private Global() {
    }

}
