package br.nnpe;

public final class Global {

    public static boolean DEBUG = false;
    public static final String DATA_FORMATO = "dd/MM/yyyy";
    public static final int MAX_VOLTAS = 72;
    public static final int MIN_VOLTAS = Global.DEBUG ? 1 : 12;
    public static final int LATENCIA_MAX = 500;
    public static final int LATENCIA_MIN = 100;
    public static final int LIMITE_DRS = 300;
    public static final Integer SEGUNDOS_PARA_INICIAR_CORRRIDA = Integer.valueOf(Global.DEBUG
            ? 30
            : 60);
    public static final double VELOCIDADE_PISTA = 1;
    public static final int TAMANHO_RETA_DRS = 1500;
    public static final double LIMITE_ESTRESSE_PARA_RERRAR_CURVA = 90;
    public static double MOD_GANHO_SUAVE = 4;
    public static double MOD_GANHO_SUAVE_MULTIPLAYER = 2.0;
    public static boolean DESENHA_DIFF_REAL_SUAVE = false;

    public static boolean DEBUG_SEM_CHUVA = false;
    public static boolean FORCE_MODELO_V2 = true;
    public static final int CARGA_ERS = 100;
    public static final int DURABILIDADE_AREOFOLIO = 5;

    public static final String CONTROLE_AUTOMATICO = "CONTROLE_AUTOMATICO";

    public static final String CONTROLE_MANUAL = "CONTROLE_MANUAL";
    public static boolean setarHints = true;

    public static boolean LOG_COLISAO = false;

    public static boolean GERAR_IMAGEM_CIRCUITO_EM_MEMORIA = true;

    /** Tamanho (em nós de reta) da zona de frenagem antes de um cluster de curva baixa qualificado. */
    public static final int TAMANHO_ZONA_FRENAGEM = 300;
    /**
     * Máximo de trechos (não nós individuais — uma única curva alta longa
     * conta como um trecho só, por mais nós que tenha) de curva alta
     * tolerados entre a reta e o cluster de curva baixa pra ainda contar
     * como zona de frenagem (mais que isso indica uma sequência de
     * esses/chicane, não uma curva fechada de verdade).
     */
    public static final int MAX_CLUSTERS_CURVA_ALTA_ZONA_FRENAGEM = 1;
    /** Mínimo de nós de curva baixa num cluster pra ele contar como curva fechada de verdade (não um kink isolado). */
    public static final int MIN_NOS_CURVA_BAIXA_ZONA_FRENAGEM = 8;
    /**
     * Multiplicador da chance-base de marca de pneu (travada de roda) no
     * início da zona de frenagem (posição relativa 0.0, o nó mais distante
     * da curva) — o freio forte acontece mais ao entrar na zona.
     */
    public static final double INTENSIDADE_MARCA_INICIO_ZONA_FRENAGEM = 1.0;
    /**
     * Multiplicador da chance-base de marca de pneu no final da zona de
     * frenagem (posição relativa 1.0, o último nó do cluster de curva
     * baixa) — o carro já reduziu a velocidade, travadas ficam mais raras.
     */
    public static final double INTENSIDADE_MARCA_FIM_ZONA_FRENAGEM = 0.3;

    private Global() {
    }

}
