package br.flmane.controles;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;

import br.nnpe.Global;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.flmane.entidades.Carro;
import br.flmane.entidades.Piloto;
import br.flmane.entidades.Volta;
import br.flmane.recursos.idiomas.Lang;
import br.flmane.servidor.JogoServidor;

/**
 * Concentra toda a decisão de piloto automático (IA) — giro do motor,
 * ERS/DRS, ataque/defesa, ida ao box, e as causas de traçado exclusivas de
 * IA — que antes vivia espalhada em {@code Piloto}. As decisões são
 * executadas através dos mesmos métodos públicos de comando que o jogador
 * humano aciona ({@code setModoPilotagem}, {@code setAtivarDRS},
 * {@code setAtivarErs}, {@code setBox}, {@code mudarTracado}), sem via de
 * comando paralela. {@code Piloto} aciona o tick desta classe através de
 * {@link InterfaceJogo#processarAutomacao(Piloto)}.
 *
 * @author Paulo Sobreira
 */
public class ControleAutomacao {
    private final ControleJogoLocal controleJogo;
    private final ControleCorrida controleCorrida;

    /** Contador de suspensão temporária por entrada humana (jogo solo automático), por piloto. */
    private final Map<Piloto, Integer> manualTemporarioPorPiloto = new HashMap<>();

    /** Timestamp (epoch millis) da última execução bem-sucedida de cada tipo de ação, por piloto — só relevante em jogo online. */
    private final Map<Piloto, Map<TipoAcaoAutomacao, Long>> ultimaExecucaoPorPiloto = new HashMap<>();

    private static final long COOLDOWN_ONLINE_MILIS = 500L;

    /** Estado técnico do carro calculado uma vez por tick, usado pela decisão de ataque/defesa. */
    private record EstadoTecnico(boolean superAquecido, boolean temMotor, boolean temCombustivel, boolean temPneu) {
    }

    public ControleAutomacao(ControleJogoLocal controleJogo, ControleCorrida controleCorrida) {
        this.controleJogo = controleJogo;
        this.controleCorrida = controleCorrida;
    }

    // ---- Cooldown de 500ms por tipo de ação (online) ----

    private boolean estaDentroDoCooldown(Piloto piloto, TipoAcaoAutomacao tipo) {
        if (!(controleJogo instanceof JogoServidor)) {
            return false;
        }
        Long ultimaExecucao = ultimaExecucaoPorPiloto.getOrDefault(piloto, java.util.Collections.emptyMap()).get(tipo);
        if (ultimaExecucao == null) {
            return false;
        }
        return (System.currentTimeMillis() - ultimaExecucao) < COOLDOWN_ONLINE_MILIS;
    }

    private void registrarExecucao(Piloto piloto, TipoAcaoAutomacao tipo) {
        ultimaExecucaoPorPiloto.computeIfAbsent(piloto, p -> new EnumMap<>(TipoAcaoAutomacao.class))
                .put(tipo, System.currentTimeMillis());
    }

    private void executarSeDentroDoCooldown(Piloto piloto, TipoAcaoAutomacao tipo, Runnable acao) {
        if (estaDentroDoCooldown(piloto, tipo)) {
            return;
        }
        acao.run();
        registrarExecucao(piloto, tipo);
    }

    private boolean executarTracadoSeDentroDoCooldown(Piloto piloto, BooleanSupplier acao) {
        if (estaDentroDoCooldown(piloto, TipoAcaoAutomacao.TRACADO)) {
            return false;
        }
        boolean executou = acao.getAsBoolean();
        if (executou) {
            registrarExecucao(piloto, TipoAcaoAutomacao.TRACADO);
        }
        return executou;
    }

    private void aplicarModoPilotagem(Piloto piloto, String modo) {
        executarSeDentroDoCooldown(piloto, TipoAcaoAutomacao.MODO_PILOTAGEM, () -> piloto.setModoPilotagem(modo));
    }

    private void aplicarGiro(Piloto piloto, int giro) {
        executarSeDentroDoCooldown(piloto, TipoAcaoAutomacao.GIRO_MOTOR, () -> piloto.getCarro().setGiro(giro));
    }

    private void aplicarErs(Piloto piloto, boolean valor) {
        executarSeDentroDoCooldown(piloto, TipoAcaoAutomacao.ERS, () -> piloto.setAtivarErs(valor));
    }

    private void aplicarDrs(Piloto piloto, boolean valor) {
        executarSeDentroDoCooldown(piloto, TipoAcaoAutomacao.DRS, () -> piloto.setAtivarDRS(valor));
    }

    private void aplicarBox(Piloto piloto, boolean valor) {
        executarSeDentroDoCooldown(piloto, TipoAcaoAutomacao.BOX, () -> piloto.setBox(valor));
    }

    // ---- Suspensão temporária por entrada humana (jogo solo automático) ----

    /**
     * Suspende a automação para {@code piloto} por 150 ciclos — chamado por
     * {@code Piloto.setManualTemporario()}, que continua sendo a fachada
     * pública estável usada por qualquer entrada humana local. Não faz
     * nada se {@code piloto} não for o jogador humano.
     */
    public void suspenderTemporariamente(Piloto piloto) {
        if (!piloto.isJogadorHumano()) {
            return;
        }
        manualTemporarioPorPiloto.put(piloto, 150);
    }

    private void decrementaManualTemporario(Piloto piloto) {
        Integer valor = manualTemporarioPorPiloto.get(piloto);
        if (valor != null && valor > 0) {
            manualTemporarioPorPiloto.put(piloto, valor - 1);
        }
    }

    /** Leitura pura (sem decrementar) — chamada por {@code Piloto.isManualTemporario()}. */
    public boolean isManualTemporario(Piloto piloto) {
        Integer valor = manualTemporarioPorPiloto.get(piloto);
        return valor != null && valor > 0;
    }

    // ---- Gate automático/manual ----

    /**
     * Sempre desligado online (o jogador humano nunca é pilotado pela IA em
     * multiplayer, mesmo que o automaticoManual da partida esteja em
     * AUTOMATICO) e no solo em modo manual.
     */
    private boolean autopilotDesligado(Piloto piloto) {
        return piloto.isJogadorHumano()
                && (Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())
                        || controleJogo instanceof JogoServidor);
    }

    private boolean autopilotAtivo(Piloto piloto) {
        return !autopilotDesligado(piloto) && !isManualTemporario(piloto);
    }

    // ---- Tick principal, acionado por Piloto via InterfaceJogo.processarAutomacao(Piloto) ----

    public void processarTick(Piloto piloto) {
        processaIAnovoIndex(piloto);
        processaIaIrBox(piloto);
    }

    private void processaIAnovoIndex(Piloto piloto) {
        if (piloto.getColisao() != null || piloto.isRecebeuBanderada() || controleJogo.isModoQualify()) {
            return;
        }
        if (autopilotDesligado(piloto)) {
            return;
        }

        if (isManualTemporario(piloto)) {
            decrementaManualTemporario(piloto);
            return;
        }

        int porcentagemDesgastePneus = piloto.getCarro().getPorcentagemDesgastePneus();
        int porcentagemCombustivel = piloto.getCarro().getPorcentagemCombustivel();
        boolean superAquecido = piloto.getCarro().verificaMotorSuperAquecido();
        int porcentagemMotor = piloto.getCarro().getPorcentagemDesgasteMotor();
        int porcentagemCorridaRestante = 100 - controleJogo.porcentagemCorridaConcluida();
        boolean temMotor = porcentagemMotor > 30 || porcentagemMotor > porcentagemCorridaRestante;
        boolean temCombustivel = porcentagemCombustivel > 10;
        if (controleJogo.isSemReabastecimento()) {
            temCombustivel = porcentagemCombustivel > porcentagemCorridaRestante;
        }
        boolean temPneu = porcentagemDesgastePneus > 30;
        if (controleJogo.isSemTrocaPneu()) {
            temPneu = porcentagemDesgastePneus > 50 || porcentagemDesgastePneus > porcentagemCorridaRestante;
        }
        EstadoTecnico estadoTecnico = new EstadoTecnico(superAquecido, temMotor, temCombustivel, temPneu);

        if (controleJogo.isSafetyCarNaPista() || piloto.getPtosBox() != 0 || piloto.danificado()) {
            aplicarGiro(piloto, Carro.GIRO_MIN_VAL);
            aplicarModoPilotagem(piloto, Piloto.LENTO);
            return;
        }
        iaTentaUsarErs(piloto);
        if (controleJogo.isDrs()) {
            iaTentaUsarDRS(piloto);
        }
        boolean tentaPassarFrete = tentarPassaPilotoDaFrente(piloto, estadoTecnico);
        boolean tentarEscaparAtras = false;
        if (!tentaPassarFrete) {
            tentarEscaparAtras = tentarEscaparPilotoAtras(piloto, false, estadoTecnico);
        }
        if (piloto.getNumeroVolta() > 0) {
            if (tentaPassarFrete && piloto.getDiferencaParaProximo() < 100
                    && piloto.getCarroPilotoDaFrente().getPiloto().getPtosBox() == 0) {
                aplicarErs(piloto, true);
                if (controleJogo.verificaInfoRelevante(piloto)) {
                    String txt = Lang.msg("tentaPassarFrete", new String[] { piloto.nomeJogadorFormatado(),
                            Html.negrito(piloto.getNome()),
                            Html.negrito(piloto.getCarroPilotoDaFrente().getPiloto().getNome()) });
                    controleJogo.info(Html.preto(txt));
                }
            } else if (tentarEscaparAtras && piloto.getDiferencaParaAnterior() < 100 && piloto.getPtosBox() == 0) {
                aplicarErs(piloto, true);
                if (controleJogo.verificaInfoRelevante(piloto)) {
                    String txt = Lang.msg("tentarEscaparAtras", new String[] { piloto.nomeJogadorFormatado(),
                            Html.negrito(piloto.getNome()),
                            Html.negrito(piloto.getCarroPilotoAtras().getPiloto().getNome()) });
                    controleJogo.info(Html.preto(txt));
                }
            }
        }

        if (!tentaPassarFrete && !tentarEscaparAtras) {
            aplicarGiro(piloto, Carro.GIRO_NOR_VAL);
            aplicarModoPilotagem(piloto, Piloto.NORMAL);
        }

        if (piloto.getCarro().verificaCondicoesCautelaGiro()) {
            aplicarGiro(piloto, Carro.GIRO_MIN_VAL);
        }
        if (piloto.getCarro().verificaCondicoesCautelaPneu()) {
            aplicarModoPilotagem(piloto, Piloto.LENTO);
        }
    }

    // ---- Box ----

    private void processaIaIrBox(Piloto piloto) {
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (piloto.isJogadorHumano() || piloto.isRecebeuBanderada()) {
            return;
        }
        Carro carro = piloto.getCarro();
        int pneusPorcentagem = carro.getPorcentagemDesgastePneus();
        int combustPorcentagem = carro.getPorcentagemCombustivel();
        int corridaPorcentagem = controleJogo.porcentagemCorridaConcluida();
        if (controleJogo.isSemReabastecimento()) {
            combustPorcentagem = 100;
        }

        if (controleJogo.isSafetyCarNaPista()) {
            if (combustPorcentagem < 20 || pneusPorcentagem < 50) {
                aplicarBox(piloto, true);
            }
        }

        boolean boxPneus = false;
        if (pneusPorcentagem < 15) {
            boxPneus = true;
        }

        if (boxPneus) {
            java.util.List<Volta> voltas = piloto.getVoltas();
            if (voltas.size() > 3) {
                Volta voltaUltima = voltas.get(voltas.size() - 1);
                Volta voltaPenultima = voltas.get(voltas.size() - 2);
                Volta voltaAntiPenultima = voltas.get(voltas.size() - 3);
                if (voltaUltima.getTempoNumero().longValue() > voltaPenultima.getTempoNumero().longValue()
                        && voltaUltima.getTempoNumero().longValue() > voltaAntiPenultima.getTempoNumero().longValue()) {
                    aplicarBox(piloto, true);
                }
            }
            if (piloto.getColisao() == null && piloto.getNoAtual().verificaCurvaBaixa()
                    && (piloto.getGanho() < (.9 * piloto.getMaxGanhoBaixa().doubleValue()))) {
                aplicarBox(piloto, true);
            }
            if (piloto.getColisao() == null && piloto.getNoAtual().verificaCurvaAlta()
                    && (piloto.getGanho() < (.9 * piloto.getMaxGanhoAlta().doubleValue()))) {
                aplicarBox(piloto, true);
            }
        }

        /** Box deixou de ter velocidade "rápida"/"lenta" sorteada por corrida — limite único (média de 80/85). */
        int limiteUltimasVoltas = 83;

        if (piloto.isBox() && corridaPorcentagem > limiteUltimasVoltas && piloto.getQtdeParadasBox() > 0) {
            aplicarBox(piloto, false);
        }

        if (carro.verificaPneusIncompativeisClima()) {
            aplicarBox(piloto, true);
        }

        if (controleJogo.isSemReabastecimento() && !carro.verificaPneusIncompativeisClima()
                && controleJogo.isSemTrocaPneu() && !carro.verificaDano()) {
            aplicarBox(piloto, false);
        }

        if (carro.verificaDano()) {
            aplicarBox(piloto, true);
        }
        if ((controleJogo.isSemReabastecimento() && combustPorcentagem < 5)
                || (!controleJogo.isSemTrocaPneu() && pneusPorcentagem < 5)) {
            aplicarBox(piloto, true);
        }
        if (controleJogo.verificaUltimaVolta()) {
            aplicarBox(piloto, false);
        }

        if (controleJogo.getNumVoltaAtual() < 0) {
            aplicarBox(piloto, false);
        }

        if (controleJogo.isCorridaTerminada()) {
            aplicarBox(piloto, false);
        }
    }

    // ---- ERS / DRS ----

    private void iaTentaUsarDRS(Piloto piloto) {
        if (controleJogo.isChovendo()) {
            return;
        }
        if (controleJogo.getNumVoltaAtual() <= 1) {
            return;
        }
        if (piloto.isAtivarDRS()) {
            return;
        }
        aplicarDrs(piloto, piloto.getNoAtual().verificaRetaOuLargada() && piloto.testeHabilidadePiloto());
    }

    private void iaTentaUsarErs(Piloto piloto) {
        if (!controleJogo.isErs()) {
            return;
        }
        if (piloto.getNoAtual() == null) {
            return;
        }
        int percetagemDeVoltaConcluida = controleJogo.percetagemDeVoltaConcluida(piloto);
        if (percetagemDeVoltaConcluida > 20 && piloto.getNoAtual().verificaRetaOuLargada()) {
            aplicarErs(piloto, true);
        }
        if (percetagemDeVoltaConcluida > 60) {
            aplicarErs(piloto, true);
        }
    }

    // ---- Ataque/defesa e ultrapassagem ----

    private boolean tentarPassaPilotoDaFrente(Piloto piloto, EstadoTecnico estadoTecnico) {
        if (piloto.getCarroPilotoDaFrente() == null) {
            return false;
        }
        if (piloto.getStress() > piloto.getValorLimiteStressePararErrarCurva()) {
            return false;
        }
        if (piloto.getDiferencaParaProximo() < 500 && piloto.testeHabilidadePilotoCarro()) {
            modoIADefesaAtaque(piloto, estadoTecnico);
            return true;
        }
        return false;
    }

    private boolean tentarEscaparPilotoAtras(Piloto piloto, boolean tentaPassarFrete, EstadoTecnico estadoTecnico) {
        if (tentaPassarFrete) {
            return false;
        }
        if (piloto.getCarroPilotoAtras() == null) {
            return false;
        }
        Piloto pilotoAtras = piloto.getCarroPilotoAtras().getPiloto();
        if (pilotoAtras.getPtosBox() != 0) {
            return false;
        }
        if (piloto.getDiferencaParaAnterior() < (controleJogo.isDrs() ? 600 : 300) && piloto.testeHabilidadePilotoCarro()) {
            modoIADefesaAtaque(piloto, estadoTecnico);
            return true;
        }
        return false;
    }

    private void modoIADefesaAtaque(Piloto piloto, EstadoTecnico estadoTecnico) {
        if (!piloto.testeHabilidadePiloto()) {
            return;
        }
        double valorLimiteStressePararErrarCurva = 100;
        boolean maxPilotagem = false;
        boolean maxCarro = false;

        if (piloto.getNoAtual().verificaRetaOuLargada()) {
            maxCarro = (!estadoTecnico.superAquecido() && estadoTecnico.temMotor() && estadoTecnico.temCombustivel()
                    && piloto.testeHabilidadePilotoCarro())
                    || (!estadoTecnico.superAquecido() && piloto.isAtivarDRS() && controleJogo.isDrs()
                            && Carro.MENOS_ASA.equals(piloto.getCarro().getAsa()));
        } else {
            maxPilotagem = estadoTecnico.temPneu() && piloto.getStress() < valorLimiteStressePararErrarCurva;
            /**
             * Acima de stress 95, AGRESSIVO já não traz ganho nenhum (ver
             * Piloto.getModoPilotagemEfetivo()) — a decisão automática tem
             * uma chance de reconhecer isso e recuar pra NORMAL por conta
             * própria, via teste de habilidade. Não é garantia: em caso de
             * falha, a decisão automática ainda escolhe AGRESSIVO. Escopo
             * exclusivo desta decisão automática — não afeta o jogador
             * humano escolhendo AGRESSIVO manualmente.
             */
            if (maxPilotagem && piloto.getStress() > 95 && piloto.testeHabilidadePilotoCarro()) {
                maxPilotagem = false;
            }
        }
        if (maxPilotagem) {
            aplicarModoPilotagem(piloto, Piloto.AGRESSIVO);
        } else {
            aplicarModoPilotagem(piloto, Piloto.NORMAL);
        }
        if (maxCarro) {
            aplicarGiro(piloto, Carro.GIRO_MAX_VAL);
        } else {
            aplicarGiro(piloto, Carro.GIRO_NOR_VAL);
        }
    }

    // ---- Causas de traçado exclusivas de IA (chamadas de Piloto.processaMudarTracado()) ----

    /**
     * Escapa da fila indiana: preso ha varios ciclos atras de um carro lento
     * na mesma linha, muda para um tracado lateral comprovadamente livre.
     */
    public boolean decideTentarEscaparFilaIndiana(Piloto piloto) {
        if (piloto.isJogadorHumano()) {
            return false;
        }
        if (piloto.getCiclosPresoFila() < 8
                && piloto.getCiclosPresoFilaProximidade() < Global.LIMIAR_CICLOS_FILA_SEM_COLISAO) {
            return false;
        }
        if (controleJogo.isSafetyCarNaPista() || piloto.isRecebeuBanderada()) {
            return false;
        }
        if (piloto.getPtosBox() != 0 || piloto.isBox()) {
            return false;
        }
        int tracadoAtual = piloto.getTracado();
        if (tracadoAtual != 0 && tracadoAtual != 1 && tracadoAtual != 2) {
            return false;
        }
        int[] alvos;
        if (tracadoAtual == 0) {
            int primeiro = controleJogo.getRandom().intervalo(1, 2);
            alvos = new int[] { primeiro, primeiro == 1 ? 2 : 1 };
        } else {
            alvos = new int[] { 0 };
        }
        for (int i = 0; i < alvos.length; i++) {
            int alvo = alvos[i];
            if (piloto.verificaTracadoLivreParaEscapar(alvo)
                    && executarTracadoSeDentroDoCooldown(piloto, () -> piloto.mudarTracado(alvo, false, true))) {
                piloto.zerarCiclosPresoFila();
                if (Global.LOG_COLISAO) {
                    Logger.logar("[ESCAPE_FILA] piloto=" + piloto.getNome() + " de=" + tracadoAtual + " para=" + alvo
                            + " idx=" + (piloto.getNoAtual() != null ? piloto.getNoAtual().getIndex() : -1)
                            + " volta=" + piloto.getNumeroVolta());
                }
                return true;
            }
        }
        return false;
    }

    /** Evita colidir com um retardatário à frente (mesmo traçado, ou perto demais). Só piloto automático ativo. */
    public boolean decideEvitaColidirComRetardatario(Piloto piloto) {
        if (autopilotAtivo(piloto) && ((piloto.isProcessaEvitaBaterCarroFrente()
                && piloto.getCarroPilotoDaFrenteRetardatario() != null
                && piloto.getTracado() == piloto.getCarroPilotoDaFrenteRetardatario().getPiloto().getTracado())
                || piloto.getDiffParaProximoRetardatario() < (piloto.testeHabilidadePiloto() ? 100 : 150))) {
            piloto.desviaPilotoNaFrente(piloto, piloto.getCarroPilotoDaFrenteRetardatario().getPiloto());
            return true;
        }
        return false;
    }

    /** Desvia de um retardatário mais à frente no mesmo traçado. Só piloto automático ativo. */
    public boolean decideDesviaRetardatarioMesmoTracado(Piloto piloto) {
        if (autopilotAtivo(piloto) && (piloto.getDiferencaParaProximoRetardatario() < piloto.getDiferencaParaAnterior()
                && piloto.getDiferencaParaProximoRetardatario() < (piloto.testeHabilidadePilotoCarro() ? 150 : 200))) {
            piloto.desviaPilotoNaFrente(piloto, piloto.getCarroPilotoDaFrenteRetardatario().getPiloto());
            return true;
        }
        return false;
    }

    /** IA espelha o traçado do carro logo atrás, numa reta, pra facilitar ultrapassagem. Só piloto de IA. */
    public boolean decideEspelhaTracadoCarroAtras(Piloto piloto) {
        if (!piloto.isJogadorHumano() && piloto.getCarroPilotoAtras() != null && piloto.getMudouTracadoReta() <= 1
                && piloto.getDiferencaParaAnterior() < 150
                && piloto.getCarroPilotoAtras().getPiloto().getTracado() != piloto.getTracado()
                && piloto.getDiferencaParaAnterior() > 100 && piloto.getCarroPilotoAtras().getPiloto().getPtosBox() == 0
                && piloto.testeHabilidadePiloto() && !piloto.isFreiandoReta()) {
            int tracadoAlvo = piloto.getCarroPilotoAtras().getPiloto().getTracado();
            if (executarTracadoSeDentroDoCooldown(piloto, () -> piloto.mudarTracado(tracadoAlvo))
                    && piloto.getNoAtual().verificaRetaOuLargada()) {
                piloto.incrementaMudouTracadoReta();
            }
            return true;
        }
        return false;
    }

    /** Sem tráfego relevante à frente nem atrás, IA recentraliza no traçado 0. Só piloto de IA. */
    public boolean decideRecentralizaSemTrafego(Piloto piloto) {
        if (!piloto.isJogadorHumano() && piloto.getDiferencaParaAnterior() > 250
                && piloto.getDiffParaProximoRetardatario() > 250) {
            executarTracadoSeDentroDoCooldown(piloto, () -> piloto.mudarTracado(0));
            return true;
        }
        return false;
    }
}
