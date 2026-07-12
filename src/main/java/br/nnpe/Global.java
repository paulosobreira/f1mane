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
    /**
     * Limite de estresse (separado de {@link #LIMITE_ESTRESSE_PARA_RERRAR_CURVA},
     * que continua exclusivo do gatilho cego antigo em
     * Piloto.processaEscapadaDaPista) usado só pelo comprometimento da
     * escapada ancorada ao traçado (Piloto.processaEscapadaAncoradaAoTracado)
     * — pode ser calibrado independentemente, sem afetar o gatilho cego.
     */
    public static final double LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA = 90;
    /**
     * Segundo limite de estresse da escapada ancorada, mais baixo que
     * {@link #LIMITE_ESTRESSE_PARA_ESCAPADA_ANCORADA} — usado como
     * pré-condição alternativa do teste de pneus
     * (Piloto.testeEscapadaPneus()), junto com pneus abaixo de 30%.
     */
    public static final double LIMITE_ESTRESSE_PARA_ESCAPADA_PNEUS = 70;
    /**
     * Fila indiana sem colisão física literal (Piloto.processaPenalidadeColisao/
     * tentarEscaparFilaIndiana): janela de índices à frente, na mesma linha,
     * pra contar como "preso" mesmo sem sobreposição de caixa de colisão.
     * Valor inicial deliberadamente agressivo (mais fácil de disparar que o
     * caminho de colisão física) — a pedido do usuário, pra recalibrar pra
     * baixo depois de observar em corrida real, não pra cima.
     */
    public static final int JANELA_FILA_SEM_COLISAO = 70;
    /** Limite de {@code ganho} pra fila indiana sem colisão física — mais permissivo que o `<= 10` da colisão física. */
    public static final double GANHO_LIMITE_FILA_SEM_COLISAO = 15;
    /** Limiar de ciclos consecutivos pra fila indiana sem colisão física — metade do limiar de 8 da colisão física. */
    public static final int LIMIAR_CICLOS_FILA_SEM_COLISAO = 4;
    public static double MOD_GANHO_SUAVE = 4;
    public static double MOD_GANHO_SUAVE_MULTIPLAYER = 2.0;
    public static boolean DESENHA_DIFF_REAL_SUAVE = false;

    public static boolean DEBUG_SEM_CHUVA = false;
    public static final int CARGA_ERS = 100;
    public static final int DURABILIDADE_AREOFOLIO = 5;

    public static final String CONTROLE_AUTOMATICO = "CONTROLE_AUTOMATICO";

    public static final String CONTROLE_MANUAL = "CONTROLE_MANUAL";
    public static boolean setarHints = true;

    public static boolean LOG_COLISAO = false;

    /**
     * Substitui a antiga GERAR_IMAGEM_CIRCUITO_EM_MEMORIA: ativo gera a
     * imagem do circuito proceduralmente em memória (em vez de carregar o
     * arquivo de fundo real), usa os nomes-homenagem de carro/piloto (em vez
     * dos nomes reais), e prioriza o modelo colorido genérico sobre sprite
     * sheet/arquivo individual pra carro/capacete.
     */
    public static boolean MODO_HOMENAGEM = true;

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

    /**
     * Chance de uma travada de roda também gerar fumaça, além da marca de
     * pneu (que é sempre gerada). Nem toda travada de roda deve produzir
     * fumaça, senão a marca e a fumaça deixam de ser eventos distintos para
     * os renderers (Swing e web).
     */
    public static final double CHANCE_FUMACA_TRAVADA_RODA = 0.6;

    private Global() {
    }

}
