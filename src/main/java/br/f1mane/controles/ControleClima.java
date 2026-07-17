package br.f1mane.controles;

import br.nnpe.Global;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.f1mane.entidades.Clima;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 20:14:25
 */
public class ControleClima {
    private final InterfaceJogo controleJogo;
    private String clima;
    private int voltaMudancaClima;
    private int intervaloMudancaClima;
    private final int numVoltas;
    private final int metadeVoltas;
    private final int quartoVoltas;
    private ThreadMudancaClima threadMudancaClima;
    private boolean climaAleatorio;

    /**
     * Estado da rampa de "molhado%" (0.0 seco, 1.0 chuva plena), independente do
     * clima categórico exibido. {@code molhadoCicloInicio}/{@code molhadoValorInicio}
     * marcam o início do trecho de rampa em curso; {@code molhadoAlvo} é 0.0 ou 1.0.
     * A progressão é medida em ciclos de simulação (não em relógio de parede), pra
     * ficar consistente independente de {@code ControleCiclo.VALENDO}.
     */
    private double molhadoValorInicio;
    private double molhadoAlvo;
    private int molhadoCicloInicio;
    private int ultimoPercentualMolhadoLogado = -1;

    public ControleClima(InterfaceJogo controleJogo, int totalVoltas) {
        super();
        this.controleJogo = controleJogo;
        numVoltas = totalVoltas;
        metadeVoltas = totalVoltas / 2;
        quartoVoltas = totalVoltas / 4;
    }

    public String getClima() {
        return clima;
    }

    public InterfaceJogo getControleJogo() {
        return controleJogo;
    }

    public int getIntervaloMudancaClima() {
        return intervaloMudancaClima;
    }

    public void setIntervaloMudancaClima(int intervaloMudancaClima) {
        this.intervaloMudancaClima = intervaloMudancaClima;
    }

    public void setClima(String clima) {
        String climaAnterior = this.clima;
        this.clima = clima;
        if (Clima.CHUVA.equals(clima) && !Clima.CHUVA.equals(climaAnterior)) {
            iniciarRampaMolhado(1.0);
        } else if (!Clima.CHUVA.equals(clima) && Clima.CHUVA.equals(climaAnterior)) {
            iniciarRampaMolhado(0.0);
        }
    }

    public void gerarClimaInicial(Clima climaSel) {
        if (Global.DEBUG_SEM_CHUVA) {
            clima = Clima.SOL;
            inicializarMolhado();
            return;
        }

        if (!Clima.ALEATORIO.equals(climaSel.getClima())) {
            clima = climaSel.getClima();
            inicializarMolhado();
            return;
        }
        climaAleatorio = true;
        int val = 1 + (int) (controleJogo.getRandom().nextDouble() * 3);

        switch (val) {
            case 1:
                clima = Clima.SOL;

                break;

            case 2:
                clima = Clima.NUBLADO;

                break;

            case 3:
                clima = Clima.CHUVA;

                break;

            default:
                break;
        }
        inicializarMolhado();
    }

    /**
     * Estabelece o estado inicial de "molhado%" a partir do clima com que a
     * corrida começa, sem rampa — se a corrida já começa chovendo, o efeito é
     * pleno desde a largada, não há "antes" pra ramp-up.
     */
    private void inicializarMolhado() {
        double valorInicial = Clima.CHUVA.equals(clima) ? 1.0 : 0.0;
        molhadoValorInicio = valorInicial;
        molhadoAlvo = valorInicial;
        molhadoCicloInicio = controleJogo.getCicloAtual();
        ultimoPercentualMolhadoLogado = percentual(valorInicial);
    }

    private void iniciarRampaMolhado(double alvo) {
        double valorAtual = getMolhado();
        molhadoValorInicio = valorAtual;
        molhadoCicloInicio = controleJogo.getCicloAtual();
        molhadoAlvo = alvo;
        ultimoPercentualMolhadoLogado = percentual(valorAtual);
        Logger.logar("[ControleClima] Rampa de molhado " + (alvo > valorAtual ? "subindo" : "descendo")
                + ": partindo de " + formatarPercentual(valorAtual) + " rumo a " + formatarPercentual(alvo));
    }

    private static int percentual(double valor) {
        return (int) Math.round(valor * 100);
    }

    private static String formatarPercentual(double valor) {
        return percentual(valor) + "%";
    }

    /**
     * Valor contínuo (0.0 a 1.0) de "quão molhada" a pista está para efeito dos
     * bônus/penalidades de {@code ganho} interpolados por clima — independente do
     * clima categórico exibido (`SOL`/`NUBLADO`/`CHUVA`). Sobe/desce linearmente ao
     * longo de aproximadamente um `tempoMedioVoltaMs()` sempre que o clima categórico
     * entra ou sai de `CHUVA`; reversível a partir do valor atual (ver
     * {@link #iniciarRampaMolhado(double)}).
     */
    public double getMolhado() {
        long duracaoCiclos = duracaoRampaCiclos();
        if (duracaoCiclos <= 0) {
            registraProgressoSeNecessario(molhadoAlvo);
            return molhadoAlvo;
        }
        int ciclosDecorridos = controleJogo.getCicloAtual() - molhadoCicloInicio;
        if (ciclosDecorridos <= 0) {
            return molhadoValorInicio;
        }
        // Taxa constante (1.0 unidade a cada duracaoCiclos), não uma interpolação
        // proporcional à distância total do trecho — assim uma reversão no meio do
        // caminho anda na mesma velocidade que uma rampa cheia, só que por menos tempo.
        double avanco = ((double) ciclosDecorridos) / duracaoCiclos;
        double valor;
        if (molhadoAlvo >= molhadoValorInicio) {
            valor = Math.min(molhadoValorInicio + avanco, molhadoAlvo);
        } else {
            valor = Math.max(molhadoValorInicio - avanco, molhadoAlvo);
        }
        registraProgressoSeNecessario(valor);
        return valor;
    }

    /**
     * Loga cada ponto percentual novo que a rampa atravessa (não cada consulta a
     * {@code getMolhado()}, que é chamado a cada ciclo por carro) — dá o rastro
     * "porcentagem a porcentagem" da transição, do início até o alvo.
     */
    private void registraProgressoSeNecessario(double valorAtual) {
        int percentualAtual = percentual(valorAtual);
        if (percentualAtual == ultimoPercentualMolhadoLogado) {
            return;
        }
        ultimoPercentualMolhadoLogado = percentualAtual;
        boolean concluida = valorAtual == molhadoAlvo;
        Logger.logar("[ControleClima] molhado=" + percentualAtual + "%" + (concluida ? " (rampa concluida)" : ""));
    }

    private long duracaoRampaCiclos() {
        long tempoCiclo = controleJogo.tempoCicloCircuito();
        if (tempoCiclo <= 0) {
            return 0;
        }
        return controleJogo.tempoMedioVoltaMs() / tempoCiclo;
    }

    /**
     * Avaliada a cada volta nova (chamada de {@code ControleJogoLocal.processaNovaVolta()}),
     * mas só tenta de fato disparar uma {@link ThreadMudancaClima} depois que
     * {@code intervaloMudancaClima} voltas se passarem desde a última avaliação
     * (sorteado em {@code quartoVoltas + rnd()*metadeVoltas} na primeira vez, e depois
     * por {@link #intervaloNublado()}/{@link #intervaloSol()}/{@link #intervaloChuva()})
     * — mecanismo da proposta inicial, restaurado a pedido do usuário.
     */
    public void processaPossivelMudancaClima() {
        Logger.logar("[ControleClima] Volta " + controleJogo.getNumVoltaAtual() + ": clima=" + clima
                + " molhado=" + formatarPercentual(getMolhado())
                + " (proxima avaliacao na volta " + (voltaMudancaClima + intervaloMudancaClima) + ")");
        if (Global.DEBUG_SEM_CHUVA) {
            clima = Clima.SOL;
            return;
        }
        if ((voltaMudancaClima + intervaloMudancaClima) > controleJogo.getNumVoltaAtual()) {
            return;
        }
        voltaMudancaClima = controleJogo.getNumVoltaAtual();
        if (intervaloMudancaClima == 0) {
            intervaloMudancaClima = quartoVoltas
                    + ((int) (controleJogo.getRandom().nextDouble() * metadeVoltas));
            Logger.logar("[ControleClima] Intervalo inicial sorteado: " + intervaloMudancaClima
                    + " voltas (primeira avaliacao na volta " + (voltaMudancaClima + intervaloMudancaClima) + ")");
            return;
        }
        if (threadMudancaClima != null && !threadMudancaClima.isProcessada()) {
            Logger.logar("[ControleClima] Tentativa de mudanca de clima na volta " + voltaMudancaClima
                    + " ADIADA: a thread anterior ainda nao terminou de processar");
            return;
        }
        Logger.logar("[ControleClima] Tentativa de mudanca de clima DISPARADA na volta " + voltaMudancaClima
                + " (clima atual=" + clima + ")");
        threadMudancaClima = new ThreadMudancaClima(this);
        threadMudancaClima.start();
    }

    public void informaMudancaClima() {
        if (Clima.SOL.equals(clima)) {
            controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("004")));
        } else if (Clima.NUBLADO.equals(clima)) {
            controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("005")));
        } else if (Clima.CHUVA.equals(clima)) {
            controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("006")));
        }
    }

    public void intervaloNublado() {
        setClima(Clima.NUBLADO);
        intervaloMudancaClima = (quartoVoltas / 2)
                + ((int) (controleJogo.getRandom().nextDouble() * quartoVoltas));
        if (intervaloMudancaClima > 0 && (controleJogo
                .totalVoltasCorrida() > (controleJogo.getNumVoltaAtual()
                + intervaloMudancaClima))) {
            controleJogo.infoPrioritaria(Html.msgClima(Html.msgClima(
                    Lang.msg("007", new Object[]{Integer.valueOf(intervaloMudancaClima)}))));
        }
    }

    public void intervaloSol() {
        setClima(Clima.SOL);
        intervaloMudancaClima = quartoVoltas
                + ((int) (controleJogo.getRandom().nextDouble() * numVoltas));
    }

    public void intervaloChuva() {
        setClima(Clima.CHUVA);
        intervaloMudancaClima = quartoVoltas
                + ((int) (controleJogo.getRandom().nextDouble() * metadeVoltas));
        if (controleJogo.getRandom().nextDouble() > 0.5) {
            intervaloMudancaClima = intervaloMudancaClima / 2;
        }
    }

    public void matarThreads() {
        if (threadMudancaClima != null) {
            threadMudancaClima.interrupt();
        }

    }

    public boolean verificaPossibilidadeChoverNaPista() {
        int porc = controleJogo.porcentagemChuvaCircuito();
        double val = (porc / 100.0);
        return controleJogo.getRandom().nextDouble() < (val);
    }

    public void climaLimpo() {
        if (Clima.NUBLADO.equals(getClima())) {
            setClima(Clima.SOL);
        }
        if (Clima.CHUVA.equals(getClima())) {
            setClima(Clima.NUBLADO);
        }

    }

    public void climaChuvoso() {
        if (Clima.NUBLADO.equals(getClima())) {
            setClima(Clima.CHUVA);
        }
        if (Clima.SOL.equals(getClima())) {
            setClima(Clima.NUBLADO);
        }
    }

}
