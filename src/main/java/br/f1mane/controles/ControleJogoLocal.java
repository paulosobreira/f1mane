package br.f1mane.controles;

import br.f1mane.MainFrame;
import br.f1mane.entidades.*;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.JogoServidor;
import br.f1mane.servidor.entidades.TOs.TravadaRoda;
import br.f1mane.visao.GerenciadorVisual;
import br.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author Paulo Sobreira
 */
public class ControleJogoLocal extends ControleRecursos
        implements
        InterfaceJogo {
    protected Piloto pilotoSelecionado;
    protected Piloto pilotoJogador;

    protected GerenciadorVisual gerenciadorVisual;
    protected ControleCorrida controleCorrida;
    protected ControleEstatisticas controleEstatisticas;
    protected ControleCampeonato controleCampeonato;

    protected final List pilotosJogadores = new ArrayList();
    protected boolean corridaTerminada;
    protected boolean trocaPneu;
    protected boolean reabastecimento;
    protected boolean ers;
    protected boolean drs;
    protected boolean safetyCar = true;

    protected boolean continuaCampeonato;

    protected Integer qtdeVoltas = null;
    protected Integer diffultrapassagem = null;
    protected String circuitoSelecionado = null;
    protected boolean atualizacaoSuave = true;
    private String automaticoManual;


    private MainFrame mainFrame;

    public ControleJogoLocal(String temporada) throws Exception {
        super(temporada);
        if (!(this instanceof JogoServidor)) {
            gerenciadorVisual = new GerenciadorVisual(this);
        }
        controleEstatisticas = new ControleEstatisticas(this);
    }

    public ControleJogoLocal() throws Exception {
        super();
        gerenciadorVisual = new GerenciadorVisual(this);
        controleEstatisticas = new ControleEstatisticas(this);
    }

    public ControleJogoLocal(MainFrame mainFrame) throws Exception {
        super();
        gerenciadorVisual = new GerenciadorVisual(this);
        controleEstatisticas = new ControleEstatisticas(this);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getCombustBox(Piloto)
     */
    public Integer getCombustBox(Piloto piloto) {
        return piloto.getCombustJogador();
    }

    public boolean isErs() {
        return ers;
    }

    public void setErs(boolean ers) {
        this.ers = ers;
    }

    public boolean isDrs() {
        return drs;
    }

    public void setDrs(boolean drs) {
        this.drs = drs;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getTipoPneuBox(Piloto)
     */
    public String getTipoPneuBox(Piloto piloto) {
        return piloto.getTipoPneuJogador();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#isCorridaTerminada()
     */
    public boolean isCorridaTerminada() {
        return corridaTerminada;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#setCorridaTerminada(boolean)
     */
    public void setCorridaTerminada(boolean corridaTerminada) {
        this.corridaTerminada = corridaTerminada;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getNosDoBox()
     */
    public List<No> getNosDoBox() {
        return nosDoBox;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getMainFrame()
     */
    public MainFrame getMainFrame() {
        return mainFrame;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getCircuito()
     */
    public Circuito getCircuito() {
        return circuito;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getNosDaPista()
     */
    public List getNosDaPista() {
        return nosDaPista;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getCarros()
     */
    public List getCarros() {
        return carros;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getPilotos()
     */
    public List<Piloto> getPilotos() {
        return pilotos;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#matarTodasThreads()
     */
    public void matarTodasThreads() {
        try {
            if (controleCorrida != null) {
                controleCorrida.finalize();
            }
            if (gerenciadorVisual != null) {
                gerenciadorVisual.finalize();
            }
            if (controleEstatisticas != null) {
                controleEstatisticas.setConsumidorAtivo(false);
                controleEstatisticas.finalize();
            }
        } catch (Throwable e) {
            Logger.logarExept(e);
        }
    }

    public String getClima() {
        if (controleCorrida != null)
            return controleCorrida.getControleClima().getClima();
        return null;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#atualizaIndexTracadoPilotos()
     */
    public void atualizaIndexTracadoPilotos() {
        decrementaTracado();
    }

    public void decrementaTracado() {
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            piloto.decIndiceTracado(this);
        }
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#info(java.lang.String)
     */
    public void info(String info) {
        if (isModoQualify()) {
            return;
        }
        controleEstatisticas.info(info);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#infoPrioritaria(java.lang.String)
     */
    public void infoPrioritaria(String info) {
        if (isModoQualify()) {
            return;
        }
        controleEstatisticas.info(info, true);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#porcentagemCorridaConcluida()
     */
    public int porcentagemCorridaConcluida() {
        if (controleCorrida == null) {
            return 0;
        }
        return controleCorrida.porcentagemCorridaConcluida();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getNumVoltaAtual()
     */
    public int getNumVoltaAtual() {
        if (controleCorrida == null) {
            return 0;
        }
        return controleCorrida.voltaAtual();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#totalVoltasCorrida()
     */
    public int totalVoltasCorrida() {
        if (controleCorrida == null) {
            return 0;
        }
        return controleCorrida.getQtdeTotalVoltas();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#verificaBoxOcupado(Carro)
     */
    public boolean verificaBoxOcupado(Carro carro) {
        if (isModoQualify()) {
            return false;
        }
        return controleCorrida.verificaBoxOcupado(carro);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#calculaSegundosParaLider(Piloto)
     */
    public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
        return controleEstatisticas.calculaSegundosParaLider(pilotoSelecionado);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#verificaUltimaVolta()
     */
    public boolean verificaUltimaVolta() {
        if (isModoQualify() || controleCorrida == null) {
            return false;
        }
        return ((controleCorrida.getQtdeTotalVoltas()
                - 1) == getNumVoltaAtual());
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#processaVoltaRapida(Piloto)
     */
    public void processaVoltaRapida(Piloto piloto) {
        controleEstatisticas.processaVoltaRapida(piloto);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getCicloAtual()
     */
    public int getCicloAtual() {
        return controleCorrida.getCicloAtual();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#verificaVoltaMaisRapidaCorrida(Piloto)
     */
    public void verificaVoltaMaisRapidaCorrida(Piloto piloto) {
        controleEstatisticas.verificaVoltaMaisRapida(piloto);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#obterIndicativoCorridaCompleta()
     */
    public double obterIndicativoCorridaCompleta() {
        return (porcentagemCorridaConcluida() / 100.0) + 1;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#obterMelhorVolta()
     */
    public Volta obterMelhorVolta() {
        return controleEstatisticas.getVoltaMaisRapida();
    }

    public void verificaAcidente(Piloto piloto) {
        controleCorrida.verificaAcidente(piloto);
    }

    public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
                                            Object combust, String nomeJogador, Object asa) {
        pilotoJogador = (Piloto) selec;
        pilotoJogador.setJogadorHumano(true);
        pilotoJogador.setNomeJogador(nomeJogador);
        pilotoJogador.setTipoPneuJogador((String) tpneu);
        pilotoJogador.setCombustJogador((Integer) combust);
        pilotoJogador.setAsaJogador((String) asa);
        pilotosJogadores.add(pilotoJogador);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#mudarModoBox()
     */
    public boolean mudarModoBox() {
        if (pilotoJogador.getPtosBox() != 0) {
            return false;
        }
        if (pilotoJogador != null) {
            int porcentCombust = 50;
            String tpPneu = Carro.TIPO_PNEU_DURO;
            String tpAsa = Carro.ASA_NORMAL;
            setBoxJogadorHumano(tpPneu, Integer.valueOf(porcentCombust), tpAsa);
            pilotoJogador.setBox(!pilotoJogador.isBox());
            return pilotoJogador.isBox();
        }
        return false;
    }

    /**
     *
     */
    public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa) {
        String tipoPneuJogador = (String) tpneu;
        Integer combustJogador = (Integer) combust;
        String asaJogador = (String) asa;
        if (pilotoJogador != null) {
            pilotoJogador.setTipoPneuJogador(tipoPneuJogador);
            pilotoJogador.setCombustJogador(combustJogador);
            pilotoJogador.setAsaJogador(asaJogador);
            pilotoJogador.setTipoPneuBox(tipoPneuJogador);
            pilotoJogador.setQtdeCombustBox(combustJogador.intValue());
            pilotoJogador.setAsaBox(asaJogador);
        }

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#selecionaPilotoJogador()
     */
    public void selecionaPilotoJogador() {
        pilotoSelecionado = pilotoJogador;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#apagarLuz()
     */
    public void apagarLuz() {
        gerenciadorVisual.apagarLuz();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#processaNovaVolta()
     */
    public void processaNovaVolta() {
        int qtdeDesqualificados = 0;
        Piloto piloto = pilotos.get(0);

        if (piloto.getNumeroVolta() == (totalVoltasCorrida() - 1)
                && (piloto.getPosicao() == 1) && !isCorridaTerminada()) {
            String nomeJogadorFormatado = piloto.nomeJogadorFormatado();
            if (Util.isNullOrEmpty(nomeJogadorFormatado)) {
                nomeJogadorFormatado = " ";
            }
            infoPrioritaria(Html.preto(piloto.getNome()) + Html.verde(
                    Lang.msg("045", new String[]{nomeJogadorFormatado})));
        }

        for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext(); ) {
            piloto = iter.next();
            if (piloto.isDesqualificado()) {
                qtdeDesqualificados++;
            }
        }
        if (qtdeDesqualificados >= 10) {
            setCorridaTerminada(true);
            controleCorrida.terminarCorrida();
            infoPrioritaria(Html
                    .vinho(Lang.msg("024", new Object[]{Integer.valueOf(getNumVoltaAtual())})));
        }
        if (getNumVoltaAtual() == 2 && isDrs() && !isChovendo()
                && !isSafetyCarNaPista()) {
            infoPrioritaria(Html.azul(Lang.msg("drsHabilitado")));
        }
        controleCorrida.getControleClima().processaPossivelMudancaClima();
        if (!isSafetyCarNaPista()) {
            Thread nvolta = new Thread(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(5000);
                        controleEstatisticas.tabelaComparativa();
                    } catch (Exception e) {
                        Logger.logarExept(e);
                    }
                }
            });
            nvolta.start();
        }
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#isChovendo()
     */
    public boolean isChovendo() {
        return Clima.CHUVA.equals(getClima());
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#informaMudancaClima()
     */
    public void informaMudancaClima() {
        gerenciadorVisual.informaMudancaClima();

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#pausarJogo()
     */
    public void pausarJogo() {
        info(Html.preto(controleCorrida.isCorridaPausada()
                ? Lang.msg("025")
                : Lang.msg("026")));
        controleCorrida.setCorridaPausada(!controleCorrida.isCorridaPausada());

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#obterResultadoFinal()
     */
    public PainelTabelaResultadoFinal obterResultadoFinal() {

        return gerenciadorVisual.getResultadoFinal();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#isSafetyCarNaPista()
     */
    public boolean isSafetyCarNaPista() {
        if (controleCorrida == null)
            return false;
        return controleCorrida.isSafetyCarNaPista();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getSafetyCar()
     */
    public SafetyCar getSafetyCar() {
        return controleCorrida.getSafetyCar();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#isSafetyCarVaiBox()
     */
    public boolean isSafetyCarVaiBox() {
        if (controleCorrida != null) {
            return !controleCorrida.isSafetyCarVaiBox();
        }
        return true;
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#obterCarroNaFrente(Piloto)
     */
    public Carro obterCarroNaFrente(Piloto piloto) {
        return controleCorrida.obterCarroNaFrente(piloto);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#obterCarroAtras(Piloto)
     */
    public Carro obterCarroAtras(Piloto piloto) {
        return controleCorrida.obterCarroAtras(piloto);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#calculaSegundosParaProximo(Piloto)
     */
    public String calculaSegundosParaProximo(Piloto psel) {
        return controleEstatisticas.calculaSegundosParaProximo(psel);
    }

    public double calculaDiferencaParaProximoDouble(Piloto psel) {
        return controleEstatisticas.calculaDiferencaParaProximoDouble(psel);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getIndexVelcidadeDaPista()
     */
    public double getIndexVelcidadeDaPista() {
        if (controleCorrida != null) {
            return controleCorrida.getIndexVelcidadeDaPista();
        }
        return 0;
    }

    @Override
    public void iniciarJogo() throws Exception {
        iniciarJogo(null);
    }

    /**
     *
     */
    public void iniciarJogo(ControleCampeonato controleCampeonato)
            throws Exception {
        this.controleCampeonato = controleCampeonato;
        Campeonato campeonato = null;
        if (controleCampeonato != null)
            campeonato = controleCampeonato.getCampeonato();
        if (gerenciadorVisual.iniciarJogoMulti(campeonato)) {
            processarEntradaDados();
            carregaRecursos((String) getCircuitos().get(circuitoSelecionado),
                    gerenciadorVisual.getListaPilotosCombo(),
                    gerenciadorVisual.getListaCarrosCombo());
            this.automaticoManual = Lang.key(gerenciadorVisual
                    .getComboBoxNivelCorrida().getSelectedItem().toString());
            controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
                    diffultrapassagem.intValue());
            controleCorrida.getControleClima()
                    .gerarClimaInicial((Clima) gerenciadorVisual
                            .getComboBoxClimaInicial().getSelectedItem());
            controleCorrida.gerarGridLargada();
            gerenciadorVisual.iniciarInterfaceGraficaJogo();
            controleCorrida.iniciarCorrida();
            if (controleCampeonato != null) {
                controleCampeonato.iniciaCorrida(circuitoSelecionado);
            }
            controleEstatisticas.inicializarThreadConsumidoraInfo();
        }
        Logger.logar("Circuito Selecionado " + circuitoSelecionado);
        Logger.logar("porcentagemChuvaCircuito(circuitoSelecionado) "
                + porcentagemChuvaCircuito(circuitoSelecionado));
        Logger.logar(
                "porcentagemChuvaCircuito() " + porcentagemChuvaCircuito());
    }

    @Override
    public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato,
                                              int combustivelSelecionado, String asaSelecionado,
                                              String pneuSelecionado, String clima) throws Exception {
        controleCampeonato = new ControleCampeonato(mainFrame);
        Map circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
        List pilotos = new ArrayList((Collection) circuitosPilotos
                .get("t" + campeonato.getTemporada()));
        Piloto pilotoSel = null;
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            if (campeonato.getNomePiloto().equals(piloto.getNome())) {
                pilotoSel = piloto;
                break;
            }
        }
        iniciarJogoMenuLocal(campeonato.getCircuitoVez(),
                campeonato.getTemporada(), campeonato.getQtdeVoltas().intValue(),
                Util.intervalo(50, 500), clima, campeonato.getNivel(),
                pilotoSel, campeonato.isKers(), campeonato.isDrs(),
                campeonato.isTrocaPneus(), campeonato.isReabastecimento(),
                combustivelSelecionado, asaSelecionado, pneuSelecionado, campeonato.isSafetycar());
        this.controleCampeonato = new ControleCampeonato(campeonato, mainFrame);
        controleCampeonato.iniciaCorrida(campeonato.getCircuitoVez());
    }

    @Override
    public void iniciarJogoMenuLocal(String circuitoSelecionado,
                                     String temporadaSelecionada, int numVoltasSelecionado,
                                     int turbulenciaSelecionado, String climaSelecionado,
                                     String automaticoManual, Piloto pilotoSelecionado, boolean ers,
                                     boolean drs, boolean trocaPneus, boolean reabastecimento,
                                     int combustivelSelecionado, String asaSelecionado,
                                     String pneuSelecionado, boolean safetycar) throws Exception {
        this.qtdeVoltas = new Integer(numVoltasSelecionado);
        this.diffultrapassagem = new Integer(turbulenciaSelecionado);
        this.reabastecimento = reabastecimento;
        this.trocaPneu = trocaPneus;
        this.circuitoSelecionado = circuitoSelecionado;
        this.ers = ers;
        this.drs = drs;
        this.safetyCar = safetycar;
        this.automaticoManual = automaticoManual;
        setTemporada("t" + temporadaSelecionada);
        carregarPilotosCarros();
        carregaRecursos((String) getCircuitos().get(circuitoSelecionado));
        List<Piloto> pilotosList = getPilotos();
        for (Iterator iterator = pilotosList.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            if (piloto.equals(pilotoSelecionado)) {
                efetuarSelecaoPilotoJogador(piloto, pneuSelecionado,
                        new Integer(combustivelSelecionado), "Fl-Mane",
                        asaSelecionado);
                break;
            }
        }
        controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
                diffultrapassagem.intValue());
        controleCorrida.getControleClima()
                .gerarClimaInicial(new Clima(climaSelecionado));
        controleCorrida.gerarGridLargada();
        gerenciadorVisual.iniciarInterfaceGraficaJogo();
        controleCorrida.iniciarCorrida();
        controleEstatisticas.inicializarThreadConsumidoraInfo();
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#getCircuitos()
     */
    public Map<String, String> getCircuitos() {
        return circuitos;
    }

    protected void processarEntradaDados() throws Exception {
        try {
            qtdeVoltas = (Integer) gerenciadorVisual.getSpinnerQtdeVoltas()
                    .getValue();
            if (qtdeVoltas.intValue() != 0) {
                if (qtdeVoltas.intValue() >= 72) {
                    qtdeVoltas = new Integer(72);
                }
            }
            diffultrapassagem = Integer.valueOf(gerenciadorVisual
                    .getSpinnerDificuldadeUltrapassagem().getValue());
            circuitoSelecionado = (String) gerenciadorVisual
                    .getComboBoxCircuito().getSelectedItem();

            if (gerenciadorVisual.getReabastecimento().isSelected()) {
                reabastecimento = true;
            }
            if (gerenciadorVisual.getTrocaPneu().isSelected()) {
                trocaPneu = true;
            }
            if (gerenciadorVisual.getErs().isSelected()) {
                ers = true;
            }
            if (gerenciadorVisual.getDrs().isSelected()) {
                drs = true;
            }
            setTemporada("t" + gerenciadorVisual.getComboBoxTemporadas()
                    .getSelectedItem());
        } catch (Exception e) {
            throw new Exception(Lang.msg("027"));
        }

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#exibirResultadoFinal()
     */
    public void exibirResultadoFinal() {
        gerenciadorVisual.exibirResultadoFinal();
        if (Logger.ativo)
            mainFrame.exibirResultadoFinal(
                    gerenciadorVisual.exibirResultadoFinal());
        controleCorrida.pararThreads();
        controleEstatisticas.setConsumidorAtivo(false);
        if (controleCampeonato != null) {
            Logger.logar(
                    "controleCampeonato.processaFimCorrida(getPilotos());");
            controleCampeonato.processaFimCorrida(getPilotos());
        }
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto piloto = (Piloto) pilotos.get(i);
            Logger.logar((i + 1) + " Posicao " + piloto.getPosicao() + " - "
                    + piloto.getNome() + "-" + piloto.getCarro().getNome() + " Volta :" + piloto.getNumeroVolta()
                    + " Paradas Box :" + piloto.getQtdeParadasBox()
                    + " Vantagem :" + piloto.getVantagem());

        }
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#abandonar()
     */
    public void abandonar() {
        if (pilotoJogador != null) {
            pilotoJogador.getCarro().setDanificado(Carro.ABANDONOU, this);
        }
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#zerarMelhorVolta()
     */
    public void zerarMelhorVolta() {
        controleEstatisticas.zerarMelhorVolta();

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#adicionarInfoDireto(java.lang.String)
     */
    public void adicionarInfoDireto(String string) {
        if (gerenciadorVisual == null) {
            return;
        }
        gerenciadorVisual.adicionarInfoDireto(string);

    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#atulizaTabelaPosicoes()
     */
    public void atulizaTabelaPosicoes() {

    }

    public void selecionouPiloto(Piloto pilotoSelecionado) {
        this.pilotoSelecionado = pilotoSelecionado;
        if (pilotosJogadores.contains(pilotoSelecionado)) {
            pilotoJogador = pilotoSelecionado;
        }
        gerenciadorVisual.atualizaPilotoSelecionado();
    }

    public Piloto getPilotoSelecionado() {
        return pilotoSelecionado;
    }

    public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
                                  Object combust, Object asa) {
        if (asa == null) {
            controleCorrida.processarTipoAsaAutomatico(pilotoJogador);
            asa = pilotoJogador.getCarro().getAsa();
        }

        String tipoPneu = (String) tpPneu;
        Integer qtdeCombustPorcent = (Integer) combust;
        if (isSemReabastecimento()) {
            qtdeCombustPorcent = 100;
        }
        pilotoJogador.getCarro().trocarPneus(this, tipoPneu,
                controleCorrida.getDistaciaCorrida());
        int undsComnustAbastecer = (controleCorrida.getTanqueCheio()
                * qtdeCombustPorcent.intValue()) / 100;
        if (isSemReabastecimento() && isCorridaIniciada() && !isModoQualify()
                && pilotoJogador.getNumeroVolta() >= 0) {
            undsComnustAbastecer = 0;
        }
        pilotoJogador.getCarro().setCombustivel(undsComnustAbastecer
                + pilotoJogador.getCarro().getCombustivel());
        if (isDrs()) {
            pilotoJogador.getCarro().setAsa(Carro.MAIS_ASA);
        } else {
            String strAsa = (String) asa;
            if (!strAsa.equals(pilotoJogador.getCarro().getAsa())) {
                infoPrioritaria(
                        Html.laranja(
                                Lang.msg("028",
                                        new String[]{
                                                pilotoJogador
                                                        .nomeJogadorFormatado(),
                                                pilotoJogador.getNome()})));
            }
            pilotoJogador.getCarro().setAsa(strAsa);
        }
        if (undsComnustAbastecer < 0) {
            undsComnustAbastecer = 0;
        }
        return undsComnustAbastecer;
    }

    public Volta obterMelhorVolta(Piloto pilotoSelecionado) {
        return pilotoSelecionado.obterVoltaMaisRapida();
    }

    public Piloto getPilotoJogador() {
        return pilotoJogador;
    }

    public void mudarGiroMotor(Object selectedItem) {
        String giroMotor = (String) selectedItem;
        if (pilotoJogador != null) {
            pilotoJogador.getCarro().mudarGiroMotor(giroMotor);
            pilotoJogador.setManualTemporario();
        }

    }

    public int calculaDiferencaParaProximo(Piloto piloto) {
        if (controleEstatisticas == null) {
            return 0;
        }
        return controleEstatisticas.calculaDiferencaParaProximo(piloto);
    }

    public void mudarModoPilotagem(String modo) {
        if (pilotoJogador != null){
            pilotoJogador.setModoPilotagem(modo);
            pilotoJogador.setManualTemporario();
        }
    }

    public String getAsaBox(Piloto piloto) {
        return piloto.getAsaJogador();
    }

    public void setMainFrame(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public boolean isModoQualify() {
        if (controleCorrida != null) {
            return controleCorrida.isModoQualify();
        }
        return false;
    }

    public void tabelaComparativa() {
        controleEstatisticas.tabelaComparativa();

    }

    public void iniciaJanela() {
        // TODO Auto-generated method stub

    }

    public boolean isCorridaIniciada() {
        if (controleCorrida == null) {
            return false;
        }
        return controleCorrida.isCorridaIniciada();
    }

    public int getMediaPontecia() {
        if (getCarros() == null) {
            return 0;
        }
        int somaPontecias = 0;
        for (int i = 0; i < getCarros().size(); i++) {
            Carro carro = (Carro) getCarros().get(i);
            somaPontecias += (carro.getPotencia() + carro.getFreios()
                    + carro.getAerodinamica());
        }
        int mediaPontecia = somaPontecias / (getCarros().size());
        return mediaPontecia;
    }

    @Override
    public void mudaPilotoSelecionado() {
    }

    public boolean isSemTrocaPneu() {
        return !trocaPneu;
    }

    public boolean isSemReabastecimento() {
        return !reabastecimento;
    }

    @Override
    public List getCarrosBox() {
        return controleCorrida.getControleBox().getCarrosBox();
    }

    @Override
    public void mudarTracado(int pos) {
        if (pilotoJogador == null)
            return;
        pilotoJogador.mudarTracado(pos, this);
    }

    /**
     * Minimo 0.5 = Mais dificil de passar Maximo 1.0 = Mais facil de passar
     */
    @Override
    public double getFatorUtrapassagem() {
        if (controleCorrida != null) {
            return controleCorrida.getFatorUtrapassagem();
        }
        return 1;
    }

    @Override
    public void setManualTemporario() {
        if(getPilotoSelecionado()!=null){
            pilotoSelecionado.setManualTemporario();
        }
    }

    @Override
    public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente) {
    }

    @Override
    public No getNoEntradaBox() {
        return controleCorrida.getControleBox().getEntradaBox();

    }

    @Override
    public void travouRodas(Piloto piloto) {
        travouRodas(piloto, false);
    }

    @Override
    public void travouRodas(Piloto piloto, boolean semFumaca) {
        if (piloto.isRecebeuBanderada()) {
            return;
        }
        if (isChovendo()) {
            return;
        }
        if (piloto.getPtosBox() != 0) {
            return;
        }
        double lim = 0.3;
        if (asfaltoAbrasivo()) {
            lim = 0.5;
        }
        if (Math.random() > lim) {
            return;
        }
        TravadaRoda travadaRoda = new TravadaRoda();
        travadaRoda.setIdNo(mapaNosIds.get(piloto.getNoAtual()).intValue());
        travadaRoda.setTracado(piloto.getTracado());
        int qtdeFumaca = 0;
        if (piloto.getNoAtual().verificaRetaOuLargada()) {
            qtdeFumaca = Util.intervalo(10, 25);
        } else if (piloto.getNoAtual().verificaCurvaAlta()) {
            qtdeFumaca = Util.intervalo(10, 30);
        } else if (piloto.getNoAtual().verificaCurvaBaixa()) {
            qtdeFumaca = Util.intervalo(10, 20);
        }
        if (semFumaca) {
            piloto.setMarcaPneu(true);
            piloto.setTravouRodas(0);
        } else {
            piloto.setTravouRodas(qtdeFumaca);
        }
        if (gerenciadorVisual != null && ControleCiclo.VALENDO)
            gerenciadorVisual.adicinaTravadaRoda(travadaRoda);

    }

    @Override
    public boolean verificaNoPitLane(Piloto piloto) {
        if (piloto == null) {
            return false;
        }
        return piloto.getPtosBox() > 0;
    }

    public BufferedImage carregaBackGround(String backGround) {
        URL url;
        try {
            String caminho = mainFrame.getCodeBase()
                    + "sowbreira/f1mane/recursos/" + backGround;
            Logger.logar("Caminho Carregar Bkg " + caminho);
            url = new URL(caminho);
            BufferedImage buff = ImageIO.read(url.openStream());
            return buff;
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return null;
    }

    @Override
    public boolean mudarModoDRS() {
        if (pilotoJogador == null)
            return false;
        if (isChovendo()) {
            controleEstatisticas.info(Lang.msg("drsDesabilitado"));
            pilotoJogador.setAtivarDRS(false);
            return false;
        }
        if (getNumVoltaAtual() <= 1) {
            pilotoJogador.setAtivarDRS(false);
            return false;
        }
        pilotoJogador.setAtivarDRS(!pilotoJogador.isAtivarDRS());
        return pilotoJogador.isAtivarDRS();
    }

    @Override
    public boolean mudarModoKers() {
        if (pilotoJogador == null)
            return false;
        pilotoJogador.setAtivarErs(!pilotoJogador.isAtivarErs());
        pilotoJogador.setManualTemporario();
        return pilotoJogador.isAtivarErs();
    }

    @Override
    public int calculaDiferencaParaAnterior(Piloto piloto) {
        return controleEstatisticas.calculaDiferencaParaAnterior(piloto);
    }

    @Override
    public int percetagemDeVoltaConcluida(Piloto pilotoSelecionado) {
        if (circuito == null) {
            return 0;
        }
        if (pilotoSelecionado.getPtosBox() != 0) {
            return 0;
        }
        double pista = circuito.getPistaFull().size();
        double indexPiloto = pilotoSelecionado.getNoAtual().getIndex();
        return (int) ((indexPiloto / pista) * 100.0);
    }

    @Override
    public boolean verirficaDesafiandoCampeonato(Piloto piloto) {
        if (controleCampeonato != null) {
            Campeonato campeonato = controleCampeonato.getCampeonato();
            if (campeonato != null) {
                return piloto.getNome().equals(campeonato.getRival());
            }
        }
        return false;
    }

    @Override
    public boolean verificaCampeonatoComRival() {
        if (controleCampeonato != null) {
            Campeonato campeonato = controleCampeonato.getCampeonato();
            if (campeonato != null) {
                return !Util.isNullOrEmpty(campeonato.getRival());
            }
        }
        return false;
    }

    @Override
    public String calculaSegundosParaRival(Piloto pilotoSelecionado) {
        Piloto piloto = obterRivalCampeonato();
        if (piloto == null) {
            return "";
        }
        String rival = piloto.getNome();
        Piloto pRival = null;
        for (Iterator iterator = getPilotos().iterator(); iterator.hasNext(); ) {
            Piloto p = (Piloto) iterator.next();
            if (p.getNome().equals(rival)) {
                pRival = piloto;
            }
        }
        return controleEstatisticas.calculaSegundosParaRival(pilotoSelecionado, pRival, tempoCicloCircuito());
    }

    @Override
    public String obterSegundosParaRival(Piloto pilotoSelecionado) {
        return pilotoSelecionado.getSegundosParaRival();
    }

    @Override
    public void verificaDesafioCampeonatoPiloto() {
        if (controleCampeonato == null) {
            return;
        }
        controleCampeonato.verificaDesafioCampeonatoPiloto();

    }

    @Override
    public void aumentaFatorAcidade() {
        if (controleCorrida != null) {
            controleCorrida.aumentaFatorAcidade();
        }

    }

    @Override
    public void diminueFatorAcidade() {
        if (controleCorrida != null) {
            controleCorrida.diminueFatorAcidade();
        }
    }

    @Override
    public void setPontosPilotoLargada(long ptosPista) {
        controleCorrida.setPontosPilotoLargada(ptosPista);
    }

    @Override
    public boolean asfaltoAbrasivo() {
        if (controleCorrida != null) {
            return controleCorrida.asfaltoAbrasivo();
        }
        return false;
    }

    @Override
    public boolean asfaltoAbrasivoReal() {
        if (controleCorrida != null) {
            return controleCorrida.asfaltoAbrasivoReal();
        }
        return false;
    }

    @Override
    public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
                                    Piloto p) {
        return controleCorrida.ganhoComSafetyCar(ganho, controleJogo, p);
    }

    @Override
    public void driveThru() {
        // TODO Auto-generated method stub

    }

    @Override
    public int porcentagemChuvaCircuito() {
        return porcentagemChuvaCircuito(circuitoSelecionado);
    }

    @Override
    public boolean isBoxRapido() {
        if (controleCorrida == null) {
            return false;
        }
        return controleCorrida.getControleBox().isBoxRapido();
    }

    @Override
    public List<String> listaInfo() {
        if (gerenciadorVisual != null) {
            return gerenciadorVisual.getBufferTextual();
        }
        return new ArrayList<String>();
    }

    @Override
    public void forcaSafatyCar() {
        if (controleCorrida.isSafetyCarNaPista()) {
            return;
        }
        int i = 1;
        Piloto piloto = pilotos.get(pilotos.size() - i);
        while (Carro.BATEU_FORTE.equals(piloto.getCarro().getDanificado())) {
            piloto = pilotos.get(pilotos.size() - (++i));
        }
        piloto.getCarro().setDanificado(Carro.BATEU_FORTE, this);
        controleCorrida.safetyCarNaPista(piloto);
    }

    @Override
    public No obterProxCurva(No noAtual) {
        return mapaNoProxCurva.get(noAtual);
    }

    @Override
    public boolean verificaLag() {
        return false;
    }

    @Override
    public int getLag() {
        return 0;
    }

    @Override
    public int calculaDiffParaProximoRetardatario(Piloto piloto,
                                                  boolean analisaTracado) {
        return controleEstatisticas.calculaDiffParaProximoRetardatario(piloto,
                analisaTracado);
    }

    @Override
    public No getNoSaidaBox() {
        return controleCorrida.getNoSaidaBox();
    }

    @Override
    public void selecionaPilotoCima() {
        gerenciadorVisual.selecionaPilotoCima();

    }

    @Override
    public void selecionaPilotoBaixo() {
        gerenciadorVisual.selecionaPilotoBaixo();

    }

    @Override
    public boolean isJogoPausado() {
        return controleCorrida.isCorridaPausada();
    }

    @Override
    public void descontaTempoPausado(Volta volta) {
        controleCorrida.descontaTempoPausado(volta);

    }

    @Override
    public void criarCampeonato() throws Exception {
        controleCampeonato.criarCampeonato();
    }

    @Override
    public void criarCampeonatoPiloto() throws Exception {
        controleCampeonato.criarCampeonatoPiloto();
    }

    @Override
    public Campeonato continuarCampeonato() {
        if (controleCampeonato == null) {
            controleCampeonato = new ControleCampeonato(mainFrame);
        }
        return controleCampeonato.continuarCampeonato();
    }

    @Override
    public Campeonato continuarCampeonatoXml() {
        return controleCampeonato.continuarCampeonatoXml();
    }

    @Override
    public void dadosPersistenciaCampeonato(Campeonato campeonato) {
        if (controleCampeonato != null && campeonato == null) {
            campeonato = controleCampeonato.getCampeonato();
        }
        ControleCampeonato.dadosPersistencia(campeonato, mainFrame);
    }

    @Override
    public void proximaCorridaCampeonato() {
        controleCampeonato.detalhesCorrida();
    }

    @Override
    public void climaChuvoso() {
        controleCorrida.climaChuvoso();

    }

    @Override
    public void climaLimpo() {
        controleCorrida.climaLimpo();

    }

    @Override
    public void ativaVerControles() {
        if (gerenciadorVisual != null)
            gerenciadorVisual.ativaVerControles();
    }

    public boolean verificaPistaEmborrachada() {
        double indicativoEmborrachamentoPista = .85;
        if (!isChovendo()) {
            double emborrachamento = porcentagemCorridaConcluida() / 200.0;
            if (emborrachamento > .4) {
                emborrachamento = .4;
            }
            indicativoEmborrachamentoPista -= emborrachamento;
        }
        return Math.random() > 0.5
                || Math.random() > indicativoEmborrachamentoPista;
    }

    @Override
    public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato,
                                            String temporadaSelecionada, int numVoltasSelecionado,
                                            int turbulenciaSelecionado, String climaSelecionado,
                                            String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
                                            boolean drs, boolean trocaPneus, boolean reabastecimento) {
        controleCampeonato = new ControleCampeonato(mainFrame);
        return controleCampeonato.criarCampeonatoPiloto(cirucitosCampeonato,
                temporadaSelecionada, numVoltasSelecionado,
                turbulenciaSelecionado, climaSelecionado, nivelSelecionado,
                pilotoSelecionado, kers, drs, trocaPneus, reabastecimento);
    }

    @Override
    public void voltaMenuPrincipal() {
        if (controleCampeonato != null) {
            controleCampeonato.continuarCampeonatoCache();
            matarTodasThreads();
            mainFrame.setCampeonato(controleCampeonato.getCampeonato());
            mainFrame.iniciar();
        } else {
            matarTodasThreads();
            mainFrame.iniciar();
        }
    }

    @Override
    public List<PilotosPontosCampeonato> geraListaPilotosPontos() {
        if (controleCampeonato == null) {
            return new ArrayList<PilotosPontosCampeonato>();
        }
        controleCampeonato.geraListaPilotosPontos();
        return controleCampeonato.getPilotosPontos();
    }

    @Override
    public List<ConstrutoresPontosCampeonato> geraListaContrutoresPontos() {
        if (controleCampeonato == null) {
            return new ArrayList<ConstrutoresPontosCampeonato>();
        }
        controleCampeonato.geraListaContrutoresPontos();
        return controleCampeonato.getContrutoresPontos();
    }

    @Override
    public void continuarCampeonato(Campeonato campeonato) {
        if (controleCampeonato == null) {
            controleCampeonato = new ControleCampeonato(mainFrame);
        }
        controleCampeonato.setCampeonato(campeonato);

    }

    @Override
    public Piloto obterRivalCampeonato() {
        if (controleCampeonato == null) {
            return null;
        }
        if (controleCampeonato.getCampeonato() == null) {
            return null;
        }
        if (controleCampeonato.getCampeonato().getRival() == null) {
            return null;
        }
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto p = (Piloto) iterator.next();
            if (p.getNome()
                    .equals(controleCampeonato.getCampeonato().getRival())) {
                return p;
            }

        }
        return null;
    }

    @Override
    public Carro obterCarroNaFrenteRetardatario(Piloto piloto,
                                                boolean analisaTracado) {
        return controleCorrida.obterCarroNaFrenteRetardatario(piloto,
                analisaTracado);
    }

    /**
     * @see br.f1mane.controles.InterfaceJogo#desenhaQualificacao()
     */
    public void desenhaQualificacao() {
        try {
            if (ControleCiclo.VALENDO) {
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Logger.logarExept(e);
        }
        if (gerenciadorVisual != null) {
            gerenciadorVisual.setDesenhouCreditos(true);
        }
        desenhouQualificacao();
    }

    @Override
    public void desenhouQualificacao() {
        if (gerenciadorVisual != null) {
            try {
                while (ControleCiclo.VALENDO && gerenciadorVisual.naoDesenhouPilotosQualificacao()) {
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Logger.logarExept(e);
            }
            try {
                if (ControleCiclo.VALENDO) {
                    Thread.sleep(4000);
                }
            } catch (InterruptedException e) {
                Logger.logarExept(e);
            }
            gerenciadorVisual.setDesenhouQualificacao(true);
        } else {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                Logger.logarExept(e);
            }
        }
        selecionaPilotoJogador();
    }

    @Override
    public void detalhesCorridaCampeonato() {
        controleCampeonato.detalhesCorrida();
    }

    @Override
    public boolean safetyCarUltimas3voltas() {
        if (controleCorrida != null) {
            return controleCorrida.safetyCarUltimas3voltas();
        }
        return false;
    }

    @Override
    public double getFatorAcidente() {
        if (controleCorrida != null) {
            return controleCorrida.getFatorAcidente();
        }
        return 0;
    }

    @Override
    public boolean verificaInfoRelevante(Piloto piloto) {
        return controleEstatisticas.verificaInfoRelevante(piloto);
    }

    @Override
    public void processaMudancaEquipeCampeontato() throws Exception {
        if (controleCampeonato != null) {
            controleCampeonato.processaMudancaEquipe();
        }

    }

    @Override
    public Campeonato continuarCampeonatoXmlDisco() {
        if (controleCampeonato != null) {
            return controleCampeonato.continuarCampeonatoXmlDisco();
        }
        return null;
    }

    @Override
    public No obterCurvaAnterior(No noAtual) {
        return mapaNoCurvaAnterior.get(noAtual);
    }

    @Override
    public int getFPS() {
        if (gerenciadorVisual != null) {
            return gerenciadorVisual.getFps();
        }
        return 0;
    }

    @Override
    public String calculaSegundosParaProximo(Piloto psel, int diferenca) {
        return controleEstatisticas.calculaSegundosParaProximo(psel, diferenca);
    }

    @Override
    public void pilotoSelecionadoMinimo() {
        if (pilotoJogador != null) {
            pilotoJogador.setModoPilotagem(Piloto.LENTO);
            pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_MIN);
            pilotoJogador.setAtivarErs(false);
            pilotoJogador.setManualTemporario();
        }

    }

    @Override
    public void pilotoSelecionadoNormal() {
        if (pilotoJogador != null) {
            pilotoJogador.setModoPilotagem(Piloto.NORMAL);
            pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_NOR);
            pilotoJogador.setAtivarErs(false);
            pilotoJogador.setManualTemporario();
        }

    }

    @Override
    public void pilotoSelecionadoMaximo() {
        if (pilotoJogador != null) {
            pilotoJogador.setModoPilotagem(Piloto.AGRESSIVO);
            pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_MAX);
            pilotoJogador.setAtivarErs(true);
            pilotoJogador.setAtivarDRS(true);
            pilotoJogador.setManualTemporario();
        }

    }

    @Override
    public boolean mostraTipoPneuAdversario() {
        return true;
    }

    @Override
    public JPanel painelNarracao() {
        if (gerenciadorVisual != null) {
            return gerenciadorVisual.getPainelNarracaoText();
        }
        return null;
    }

    @Override
    public JPanel painelDebug() {
        if (controleEstatisticas != null) {
            return controleEstatisticas.getPainelDebug();
        }
        return null;
    }

    @Override
    public void atualizaInfoDebug() {
        if (controleEstatisticas != null) {
            controleEstatisticas.atualizaInfoDebug();
        }
    }

    @Override
    public void atualizaInfoDebug(StringBuilder buffer) {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory() / 1048576;
        long allocatedMemory = runtime.totalMemory() / 1048576;
        long freeMemory = runtime.freeMemory() / 1048576;

        buffer.append("MAXMEMORY :").append(maxMemory).append("<br>");
        buffer.append("ALLOCATEDMEMORY :").append(allocatedMemory).append("<br>");
        buffer.append("FREEMEMORY :").append(freeMemory).append("<br>");

        Field[] declaredFields = this.getClass().getDeclaredFields();
        List<String> campos = new ArrayList<String>();
        buffer.append("-=ControleJogo=- <br>");
        campos.add("asfaltoAbrasivo = " + this.asfaltoAbrasivo() + "<br>");
        campos.add("porcentagemChuvaCircuito = "
                + this.porcentagemChuvaCircuito() + "<br>");
        campos.add("isBoxRapido = " + this.isBoxRapido() + "<br>");
        campos.add("verificaPistaEmborrachada = "
                + this.verificaPistaEmborrachada() + "<br>");
        campos.add("porcentagemCorridaConcluida = "
                + this.porcentagemCorridaConcluida() + "<br>");
        campos.add(
                "FatorUtrapassagem = " + this.getFatorUtrapassagem() + "<br>");
        campos.add("FatorAcidente = " + this.getFatorAcidente() + "<br>");
        campos.add("NumVoltaAtual = " + this.getNumVoltaAtual() + "<br>");
        campos.add(
                "totalVoltasCorrida = " + this.totalVoltasCorrida() + "<br>");
        campos.add(
                "verificaUltimaVolta = " + this.verificaUltimaVolta() + "<br>");
        for (Field field : declaredFields) {
            try {
                Object object = field.get(this);
                String valor = "null";
                if (object != null) {
                    if (Util.isWrapperType(object.getClass())) {
                        continue;
                    }
                    valor = object.toString();
                }
                campos.add(field.getName() + " = " + valor + "<br>");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        campos.add("Clima = " + this.getClima() + "<br>");
        campos.add("IndicativoCorridaCompleta = "
                + this.obterIndicativoCorridaCompleta() + "<br>");
        campos.add("isChovendo = " + this.isChovendo() + "<br>");
        campos.add(
                "isSafetyCarNaPista = " + this.isSafetyCarNaPista() + "<br>");
        campos.add("isSafetyCarVaiBox = " + !this.isSafetyCarVaiBox() + "<br>");
        campos.add("IndexVelcidadeDaPista = " + this.getIndexVelcidadeDaPista()
                + "<br>");
        campos.add("isModoQualify = " + this.isModoQualify() + "<br>");
        campos.add("Temporada = " + this.getTemporada() + "<br>");
        campos.add("verificaCampeonatoComRival = "
                + this.verificaCampeonatoComRival() + "<br>");
        campos.add("safetyCarUltimas3voltas = " + this.safetyCarUltimas3voltas()
                + "<br>");
        campos.add("mostraTipoPneuAdversario = "
                + this.mostraTipoPneuAdversario() + "<br>");
        campos.add(
                "isCorridaTerminada = " + this.isCorridaTerminada() + "<br>");
        campos.add("isCorridaIniciada = " + this.isCorridaIniciada() + "<br>");
        campos.add("MediaPontecia = " + this.getMediaPontecia() + "<br>");
        Collections.sort(campos, new StringComparator());
        for (Iterator<String> iterator = campos.iterator(); iterator
                .hasNext(); ) {
            buffer.append(iterator.next());
        }
    }

    @Override
    public void forcaQuerbraAereofolio(Piloto piloto) {
        if (piloto == null) {
            return;
        }
        piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO,
                this);
        piloto.getCarro().setDurabilidadeAereofolio(0);
    }

    public boolean isAtualizacaoSuave() {
        return atualizacaoSuave;
    }

    public void setAtualizacaoSuave(boolean atualizacaoSuave) {
        this.atualizacaoSuave = atualizacaoSuave;
    }

    @Override
    public void setRecebeuBanderada(Piloto piloto) {
        if (!piloto.isRecebeuBanderada()) {
            piloto.setRecebeuBanderada(true);
            if (!piloto.isDesqualificado()) {
                piloto.setPosicaoBandeirada(piloto.getPosicao());
            }
            if (piloto.getCarroPilotoAtras() != null) {
                piloto.setVantagem(piloto.getCalculaSegundosParaAnterior());
            }
            Logger.logar(piloto.toString() + " Pts " + piloto.getPtosPista());
            Logger.logar(
                    piloto.toString() + " Pts Depois " + piloto.getPtosPista());

            String nomeJogadorFormatado = piloto.nomeJogadorFormatado();
            if (Util.isNullOrEmpty(nomeJogadorFormatado)) {
                nomeJogadorFormatado = " ";
            }
            if (piloto.getPosicao() == 1) {
                infoPrioritaria(
                        Html.preto(piloto.getNome())
                                + Html.verde(Lang.msg("044",
                                new String[]{
                                        String.valueOf(
                                                piloto.getPosicao()),
                                        nomeJogadorFormatado})));
            } else {
                info(Html.preto(piloto.getNome())
                        + Html.verde(
                        Lang.msg("044",
                                new String[]{
                                        String.valueOf(
                                                piloto.getPosicao()),
                                        nomeJogadorFormatado})));
            }
            double somaBaixa = 0;
            for (Iterator iterator = piloto.getGanhosBaixa()
                    .iterator(); iterator.hasNext(); ) {
                Double d = (Double) iterator.next();
                somaBaixa += d.doubleValue();
            }
            double somaAlta = 0;
            for (Iterator iterator = piloto.getGanhosAlta().iterator(); iterator
                    .hasNext(); ) {
                Double d = (Double) iterator.next();
                somaAlta += d.doubleValue();
            }
            double somaReta = 0;
            for (Iterator iterator = piloto.getGanhosReta().iterator(); iterator
                    .hasNext(); ) {
                Double d = (Double) iterator.next();
                somaReta += d.doubleValue();
            }
            somaBaixa /= piloto.getGanhosBaixa().size();
            somaAlta /= piloto.getGanhosAlta().size();
            somaReta /= piloto.getGanhosReta().size();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss .S");
            Logger.logar("Bandeirada " + piloto.getNome() + " Pts pista "
                    + piloto.getPtosPista() + " Pos " + piloto.getPosicao()
                    + " T " + df.format(new Date()));
            Logger.logar(" SomaBaixa " + somaBaixa + " SomaAlta " + somaAlta
                    + " SomaReta " + somaReta);

        }

    }

    public boolean isTrocaPneu() {
        return trocaPneu;
    }

    public boolean isReabastecimento() {
        return reabastecimento;
    }

    public boolean isSafetyCar() {
        return safetyCar;
    }

    @Override
    public Piloto getPilotoBateu() {
        return controleCorrida.getPilotoBateu();
    }

    @Override
    public boolean verificaSaidaBox(Piloto piloto) {
        return controleCorrida.verificaSaidaBox(piloto);
    }

    @Override
    public boolean verificaEntradaBox(Piloto piloto) {
        return controleCorrida.verificaEntradaBox(piloto);
    }

    @Override
    public Double getFatorBoxTemporada() {
        TemporadasDefault temporadasDefault = carregadorRecursos
                .carregarTemporadasPilotosDefauts().get(getTemporada());
        return temporadasDefault.getFatorBox();
    }

    @Override
    public void desqualificaPiloto(Piloto piloto) {
        int desqualificados = 0;
        List<Piloto> pilotos = getPilotos();
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto pilotoLista = (Piloto) iterator.next();
            if (pilotoLista.isDesqualificado()) {
                desqualificados++;
            }

        }
        piloto.setDesqualificado(true);
        piloto.setPtosPista(desqualificados);
        piloto.setPosicaoBandeirada(pilotos.size() + desqualificados);
    }

    @Override
    public String getVantagem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setVantagem(String vantagem) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getFatorConsumoPneuSemTroca() {
        return controleCorrida.getFatorConsumoPneuSemTroca();
    }

    @Override
    public double getFatorConsumoCombustivelSemReabastecimento() {
        return controleCorrida.getFatorConsumoCombustivelSemReabastecimento();
    }

    @Override
    public long tempoCicloCircuito() {
        return circuitosCiclo.get(circuito.getNome());
    }

    @Override
    public String getAutomaticoManual() {
        return automaticoManual;
    }

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }
}