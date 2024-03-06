package br.f1mane.servidor.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.f1mane.controles.ControleCorrida;
import br.f1mane.entidades.Carro;
import br.f1mane.entidades.No;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.Volta;
import br.f1mane.servidor.entidades.Comandos;
import br.f1mane.servidor.entidades.TOs.ClientPaddockPack;
import br.f1mane.servidor.entidades.TOs.DadosJogo;
import br.f1mane.servidor.entidades.TOs.DadosParciais;
import br.f1mane.servidor.entidades.TOs.ErroServ;
import br.f1mane.servidor.entidades.TOs.MsgSrv;
import br.f1mane.servidor.entidades.TOs.Posis;
import br.f1mane.servidor.entidades.TOs.PosisPack;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;
import br.f1mane.servidor.entidades.TOs.SrvJogoPack;
import br.f1mane.servidor.entidades.TOs.TravadaRoda;
import br.f1mane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 05/08/2007 as 11:43:33
 */
public class MonitorJogo implements Runnable {
    private final JogoCliente jogoCliente;
    private final ControlePaddockCliente controlePaddockCliente;
    private String estado = null;
    private final SessaoCliente sessaoCliente;
    private Thread atualizadorPainel;
    private Thread threadCmd;
    private boolean jogoAtivo = true;
    public long lastPosis = 0;
    public boolean procPosis = false;
    private boolean atualizouDados;
    private boolean setZoom;
    private boolean apagouLuz;
    protected boolean modoBox;

    public boolean isJogoAtivo() {
        return jogoAtivo;
    }

    public void setJogoAtivo(boolean jogoAtivo) {
        this.jogoAtivo = jogoAtivo;
    }

    public MonitorJogo(JogoCliente local,
                       ControlePaddockCliente controlePaddockCliente,
                       SessaoCliente sessaoCliente) {
        this.jogoCliente = local;
        this.controlePaddockCliente = controlePaddockCliente;
        this.sessaoCliente = sessaoCliente;
    }

    public void run() {
        boolean interrupt = false;
        while (!interrupt && controlePaddockCliente.isComunicacaoServer()
                && jogoAtivo) {
            try {
                long tempoCiclo = 500;
                Logger.logar("MonitorJogo");
                Logger.logar("MonitorJogo verificaEstadoJogo()");
                verificaEstadoJogo();
                Logger.logar(
                        "MonitorJogo jogoCliente.preparaGerenciadorVisual(true)");
                jogoCliente.preparaGerenciadorVisual(true);
                Logger.logar("MonitorJogo esperaJogoComecar()");
                esperaJogoComecar();
                Logger.logar("MonitorJogo mostraQualify()");
                mostraQualify();
                Logger.logar("MonitorJogo apagaLuzesLargada()");
                apagaLuzesLargada();
                Logger.logar("MonitorJogo processaCiclosCorrida(tempoCiclo)");
                processaCiclosCorrida(tempoCiclo);
                Logger.logar("MonitorJogo mostraResultadoFinal(tempoCiclo)");
                mostraResultadoFinal(tempoCiclo);
                Thread.sleep(controlePaddockCliente.getLatenciaMinima());
            } catch (Exception e) {
                interrupt = true;
                matarCmdThread();
                Logger.logarExept(e);
                if (!(e instanceof InterruptedException)) {
                    JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
                            e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        if (jogoCliente != null) {
            jogoCliente.matarThreadsResultadoFnal();
        }

    }

    private void apagaLuzesLargada() {
        boolean interupt = false;
        while (!interupt && Comandos.LUZES.equals(estado)
                && controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
            try {
                iniciaJalena();
                atualizarDados();
                jogoCliente.desenhouQualificacao();
                atualizaZoom();
                if (!apagouLuz) {
                    Logger.logar(
                            "apagaLuzesLargada atualizarDadosParciais(jogoCliente.getDadosJogo(), 5000");
                    atualizarDadosParciais(jogoCliente.getDadosJogo(),
                            jogoCliente.getPilotoSelecionado());
                    Thread.sleep(6500);
                } else {
                    Logger.logar("apagaLuzesLargada 500");
                    Thread.sleep(600);
                }
                verificaEstadoJogo();
                Logger.logar("apagaLuzesLargada verificaEstadoJogo");
                apagarLuz();
            } catch (InterruptedException e) {
                interupt = true;
                Logger.logarExept(e);
            }
        }
    }

    private void mostraResultadoFinal(long tempoCiclo)
            throws InterruptedException {
        boolean interrupt = false;
        while (!interrupt && Comandos.MOSTRA_RESULTADO_FINAL.equals(estado)
                && controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
            try {
                List pilotos = jogoCliente.getPilotosCopia();
                for (Iterator iterator = pilotos.iterator(); iterator
                        .hasNext(); ) {
                    Piloto piloto = (Piloto) iterator.next();
                    atualizarDadosParciais(jogoCliente.getDadosJogo(), piloto);
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        interrupt = true;
                        Logger.logarExept(e);
                    }
                }
                atualizarDados();
                jogoCliente.exibirResultadoFinal();
                jogoAtivo = false;
                Thread.sleep(tempoCiclo);
            } catch (InterruptedException e) {
                Logger.logarExept(e);
                throw e;
            }
        }
    }

    private void processaCiclosCorrida(long tempoCiclo)
            throws InterruptedException {
        while (Comandos.CORRIDA_INICIADA.equals(estado) && controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
            try {
                jogoCliente.setAtualizacaoSuave(getLatenciaReal() <= 1000);
                if (controlePaddockCliente
                        .getLatenciaReal() > Constantes.LATENCIA_MAX) {
                    jogoCliente.autoDrs();
                }
                atualizarDados();
                iniciaJalena();
                atualizaZoom();
                apagarLuz();
                jogoCliente.desenhaQualificacao();
                jogoCliente.desenhouQualificacao();
                jogoCliente.selecionaPilotoJogador();
                disparaAtualizadorPainel(tempoCiclo);
                atualizarDadosParciais(jogoCliente.getDadosJogo(),
                        jogoCliente.getPilotoSelecionado(), true);
                Thread.sleep(tempoCiclo);
            } catch (InterruptedException e) {
                Logger.logarExept(e);
                throw e;
            }
        }
    }

    private void atualizaZoom() {
        if (!setZoom && jogoCliente.getFPS() >= 30) {
            // jogoCliente.setMouseZoom(0.7);
            setZoom = true;
        }
    }

    private void disparaAtualizadorPainel(final long tempoCiclo) {
        if (atualizadorPainel != null) {
            return;
        }
        atualizadorPainel = new Thread(new Runnable() {
            public void run() {
                Logger.logar("MonitorJogo disparaAtualizadorPainel");
                boolean interrupt = false;
                while (!interrupt && jogoAtivo) {
                    try {
                        if (jogoCliente.getPilotoSelecionado() == null) {
                            jogoCliente.selecionaPilotoJogador();
                        }
                        jogoCliente.atualizaIndexTracadoPilotos();
                        Thread.sleep(tempoCiclo);
                    } catch (Exception e) {
                        interrupt = true;
                        Logger.logarExept(e);
                    }
                }

            }

        });
        atualizadorPainel.start();
    }

    private void mostraQualify() throws InterruptedException {
        boolean interrupt = false;
        boolean creditos = false;
        boolean atualizouDadosQualify = false;
        while (!interrupt && Comandos.MOSTRANDO_QUALIFY.equals(estado)
                && controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
            int cont = 0;
            while (!atualizouDadosQualify && !atualizouDados && cont < 15) {
                atualizarDados();
                if (atualizouDados) {
                    atualizouDadosQualify = true;
                } else {
                    cont++;
                    Thread.sleep(100);
                }
            }
            iniciaJalena();
            if (!creditos) {
                Thread.sleep(1500);
                creditos = true;
            }
            jogoCliente.desenhaQualificacao();
            verificaEstadoJogo();
        }
    }

    private void esperaJogoComecar() throws InterruptedException {
        while (Comandos.ESPERANDO_JOGO_COMECAR.equals(estado) && controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
            jogoCliente.carregaBackGroundCliente();
            verificaEstadoJogo();
            try {
                atualizouDados = false;
                Thread.sleep(600);
            } catch (InterruptedException e) {
                Logger.logarExept(e);
                throw e;
            }
        }
    }

    private void atualizaModoCarreira() {
        try {
            ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                    Comandos.VER_CARREIRA, sessaoCliente);

            clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
            Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
            if (retornoNaoValido(ret)) {
                return;
            }
            if (ret != null) {
                CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) ret;
                if (carreiraDadosSrv.isModoCarreira()) {
                    jogoCliente.setNomePilotoJogador(
                            carreiraDadosSrv.getNomePiloto());
                }
            }
            clientPaddockPack = new ClientPaddockPack(
                    Comandos.DADOS_PILOTOS_JOGO, sessaoCliente);
            clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
            ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
            if (retornoNaoValido(ret)) {
                return;
            }
            if (ret != null) {
                clientPaddockPack = (ClientPaddockPack) ret;
                if (clientPaddockPack.getDadosJogoCriado()
                        .getPilotosCarreira() != null) {
                    Logger.logar(
                            " Dentro dadosParticiparJogo.getPilotosCarreira()");
                    List pilots = clientPaddockPack.getDadosJogoCriado()
                            .getPilotosCarreira();
                    List carros = new ArrayList();
                    for (Iterator iterator = pilots.iterator(); iterator
                            .hasNext(); ) {
                        Piloto piloto = (Piloto) iterator.next();
                        if (!carros.contains(piloto.getCarro())) {
                            carros.add(piloto.getCarro());
                        }
                    }
                    Logger.logar("Tamanho da lista Cliente " + carros.size());
                    jogoCliente.geraBoxesEquipes(carros);
                }
            }
        } catch (Exception e) {
            Logger.logarExept(e);
            JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
                    e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void atualizaPosisPack(PosisPack posisPack) {
        if (posisPack == null) {
            return;
        }
        if (posisPack.safetyNoId != 0) {
            jogoCliente.setSafetyCarNaPista(true);
            jogoCliente.atualizaPosSafetyCar(posisPack.safetyNoId,
                    posisPack.safetySair);
        } else {
            jogoCliente.setSafetyCarNaPista(false);
        }
        if (posisPack.posis == null) {
            return;
        }
        atualizarListaPilotos(posisPack.posis);
    }

    private boolean retornoNaoValido(Object ret) {
        return ret instanceof ErroServ || ret instanceof MsgSrv;
    }

    public void atualizarListaPilotos(Object[] posisArray) {
        List<Piloto> pilotos = jogoCliente.getPilotos();
        if (pilotos == null) {
            return;
        }
        for (int i = 0; i < posisArray.length; i++) {
            Posis posis = (Posis) posisArray[i];
            for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext(); ) {
                Piloto piloto = iter.next();
                piloto.setFaiscas(false);
                if (piloto.getId() != posis.getIdPiloto()) {
                    continue;
                }
                piloto.setPtosPista(posisArray.length - i);
                String statusPilotos = posis.getStatus();
                if (statusPilotos != null) {
                    if ("F".equals(statusPilotos)) {
                        piloto.setFaiscas(true);
                    } else if ("T".equals(statusPilotos)
                            || "M".equals(statusPilotos)) {
                        jogoCliente.travouRodas(piloto);
                        if (piloto.getNoAtual() != null && jogoCliente.obterIdPorNo(piloto.getNoAtual()) != null) {
                            TravadaRoda travadaRoda = new TravadaRoda();
                            travadaRoda.setIdNo(
                                    jogoCliente.obterIdPorNo(piloto.getNoAtual()).intValue());
                            jogoCliente.travouRodas(travadaRoda);
                        }
                    } else if ("A".equals(statusPilotos)) {
                        piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO,
                                null);
                        piloto.getCarro().setDurabilidadeAereofolio(0);
                    } else if ("R".equals(statusPilotos)) {
                        piloto.getCarro().setRecolhido(true);
                    } else if ("B".equals(statusPilotos)) {
                        piloto.setRecebeuBanderada(true);
                    } else if ("BA".equals(statusPilotos)) {
                        piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO,
                                null);
                        piloto.getCarro().setDurabilidadeAereofolio(0);
                        piloto.setRecebeuBanderada(true);
                    }
                }
                piloto.setJogadorHumano(posis.isHumano());
                int pos = posis.getTracado();

                if (piloto.getIndiceTracado() > 0
                        && pos != piloto.getTracado()) {
                    piloto.decIndiceTracado(jogoCliente);
                } else {
                    if (piloto.getIndiceTracado() <= 0) {
                        piloto.setTracadoAntigo(piloto.getTracado());
                    }
                    piloto.setTracado(pos);
                    if (piloto.getIndiceTracado() <= 0 && piloto
                            .getTracado() != piloto.getTracadoAntigo()) {
                        piloto.calculaIndiceTracado(jogoCliente);
                    }
                }
                jogoCliente.calculaSegundosParaLider(piloto);
                piloto.calculaCarrosAdjacentes(jogoCliente);
                Map<Integer, No> mapaIdsNos = jogoCliente.getMapaIdsNos();
                List nosDoBox = jogoCliente.getNosDoBox();
                if (posis.getIdNo() >= -1) {
                    No no = (No) mapaIdsNos.get(new Integer(posis.getIdNo()));
                    piloto.setNoAtual(no);
                    if (nosDoBox.contains(no)) {
                        piloto.setPtosBox(1);
                    } else {
                        piloto.setPtosBox(0);
                    }
                }
            }
        }
    }

    private void calculaNumeroVoltaPorPontosPontosPista(Piloto piloto) {
        int volta = ((int) Math.floor(
                piloto.getPtosPista() / jogoCliente.getNosDaPista().size()))
                - 1;
        if (volta < 0) {
            volta = 0;
        }
        piloto.setNumeroVolta(volta);
    }

    public static void main(String[] args) {
        // int valor = 2000;
        // System.out.println(valor > 1500 && valor <= 2000);

        for (int i = 0; i < 200; i += 5) {
            System.out.println(
                    "if (diffINdex >=" + i + "&& diffINdex <" + (i + 5));
        }
        // int cont = 0;
        // for (int i = 0; i < 2000; i += 20) {
        // cont++;
        // }
        // System.out.println(cont);
    }

    private void apagarLuz() {
        jogoCliente.apagarLuz();
        apagouLuz = true;
    }

    public void atualizarDados() {
        if (atualizouDados) {
            return;
        }
        try {
            ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                    Comandos.OBTER_DADOS_JOGO, sessaoCliente);
            clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
            Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
            if (retornoNaoValido(ret)) {
                return;
            }
            if (ret != null) {
                DadosJogo dadosJogo = (DadosJogo) ret;
                jogoCliente.setDadosJogo(dadosJogo);
                if ((jogoCliente.getPilotos() == null
                        || jogoCliente.getPilotos().isEmpty())
                        || (jogoCliente.getPilotos() != null
                        && dadosJogo.getPilotos() != null
                        && jogoCliente.getPilotos().size() != dadosJogo
                        .getPilotos().size())) {
                    atualizouDados = false;
                } else {
                    atualizouDados = true;
                    atualizaModoCarreira();
                    atualizarDadosParciais(dadosJogo, null);
                    Logger.logar("atualizouDados = true");
                }
            } else {
                atualizouDados = false;
            }
        } catch (Exception e) {
            Logger.logarExept(e);
            atualizouDados = false;
        }
    }

    private void iniciaJalena() {
        if (jogoAtivo) {
            jogoCliente.iniciaJanela();
        }
    }

    private void verificaEstadoJogo() {
        ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                Comandos.VERIFICA_ESTADO_JOGO, sessaoCliente);
        clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
        Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
        if (retornoNaoValido(ret)) {
            return;
        }
        if (ret != null) {
            SrvJogoPack jogoPack = (SrvJogoPack) ret;
            estado = jogoPack.getEstadoJogo();
        }
    }

    public String getEstado() {
        return estado;
    }

    public void abandonar() {
        try {
            ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                    Comandos.SAIR_JOGO, sessaoCliente);
            clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
            controlePaddockCliente.enviarObjeto(clientPaddockPack);
            jogoCliente.matarTodasThreads();
        } catch (Exception e) {
            Logger.logarExept(e);
            jogoAtivo = false;
        }

    }

    public void atualizarDadosParciais(DadosJogo dadosJogo,
                                       Piloto pilotoSelecionado) {
        atualizarDadosParciais(dadosJogo, pilotoSelecionado, false);
    }

    public void atualizarDadosParciais(DadosJogo dadosJogo,
                                       Piloto pilotoSelecionado, boolean atualizaPosicoes) {
        try {
            String dataSend = jogoCliente.getNomeJogoCriado() + "#"
                    + sessaoCliente.getToken();
            if (pilotoSelecionado != null) {
                dataSend += "#" + pilotoSelecionado.getId();
            }
            Object ret = controlePaddockCliente.enviarObjeto(dataSend, true);
            if (retornoNaoValido(ret)) {
                return;
            }
            if (ret == null) {
                Logger.logar("atualizarDadosParciais null");
                return;
            }
            // dec dadosParciais
            String enc = (String) ret;
            DadosParciais dadosParciais = new DadosParciais();
            dadosParciais.decode(enc);
            estado = dadosParciais.estado;
            jogoCliente.verificaMudancaClima(dadosParciais.clima);
            jogoCliente.info(dadosParciais.texto);
            jogoCliente.setVantagem(dadosParciais.vantagem);
            dadosJogo.setClima(dadosParciais.clima);
            dadosJogo
                    .setMelhoVolta(new Volta(dadosParciais.melhorVoltaCorrida));
            dadosJogo.setVoltaAtual(Integer.valueOf(dadosParciais.voltaAtual));
            List<Piloto> pilotos = jogoCliente.getPilotos();
            if (pilotoSelecionado != null) {
                Piloto piloto = pilotoSelecionado;
                piloto.setMelhorVolta(new Volta(dadosParciais.melhorVolta));
                piloto.getVoltas().clear();
                piloto.getVoltas().add(new Volta(dadosParciais.ultima5));
                piloto.getVoltas().add(new Volta(dadosParciais.ultima4));
                piloto.getVoltas().add(new Volta(dadosParciais.ultima3));
                piloto.getVoltas().add(new Volta(dadosParciais.ultima2));
                piloto.getVoltas().add(new Volta(dadosParciais.ultima1));
                piloto.processaUltimas5Voltas();
                piloto.setNumeroVolta(dadosParciais.voltaAtual);
                piloto.setNomeJogador(dadosParciais.nomeJogador);
                piloto.setQtdeParadasBox(dadosParciais.paradas);
                piloto.setJogadorHumano(piloto.getTokenJogador() != null);
                piloto.getCarro().setDanificado(dadosParciais.dano, null);
                if (!jogoCliente.isSafetyCarNaPista()
                        && piloto.isDesqualificado()) {
                    piloto.getCarro().setRecolhido(true);
                }
                piloto.setBox(dadosParciais.box);
                piloto.setStress(dadosParciais.stress);
                piloto.setPodeUsarDRS(dadosParciais.podeUsarDRS);
                piloto.setRecebeuBanderada(dadosParciais.recebeuBanderada);
                piloto.getCarro().setCargaErs(dadosParciais.cargaErs);
                piloto.setAlertaMotor(dadosParciais.alertaMotor);
                piloto.setAlertaAerefolio(dadosParciais.alertaAerefolio);
                if (piloto.getCargaKersOnline() != dadosParciais.cargaErs) {
                    piloto.setAtivarErs(true);
                    piloto.setCargaKersOnline(dadosParciais.cargaErs);
                } else {
                    piloto.setAtivarErs(false);
                }
                piloto.getCarro()
                        .setPorcentagemDesgasteMotor(dadosParciais.pMotor);
                piloto.getCarro()
                        .setPorcentagemDesgastePneus(dadosParciais.pPneus);
                piloto.getCarro()
                        .setPorcentagemCombustivel(dadosParciais.pCombust);
                piloto.setNumeroVolta(dadosParciais.pVolta);
                piloto.getCarro().setAsa(dadosParciais.asaBox);
                piloto.getCarro().setTipoPneu(dadosParciais.tpPneus);
                if (piloto.getCarroPilotoDaFrente() != null) {
                    piloto.getCarroPilotoDaFrente()
                            .setTipoPneu(dadosParciais.tpPneusFrente);
                }
                if (piloto.getCarroPilotoAtras() != null) {
                    piloto.getCarroPilotoAtras()
                            .setTipoPneu(dadosParciais.tpPneusAtras);
                }
                piloto.setVelocidade(dadosParciais.velocidade);
                piloto.setVelocidadeExibir(dadosParciais.velocidade);
                piloto.setVelocidade(dadosParciais.velocidade);
                piloto.setQtdeCombustBox(dadosParciais.combustBox);
                piloto.setTipoPneuBox(dadosParciais.tpPneusBox);
                piloto.setModoPilotagem(dadosParciais.modoPilotar);
                piloto.setAsaBox(dadosParciais.asaBox);
                piloto.getCarro().setAsa(dadosParciais.asa);
                piloto.getCarro().setGiro(dadosParciais.giro);
                piloto.setVantagem(dadosParciais.vantagem);
            }
            if (atualizaPosicoes) {
                atualizaPosisPack(dadosParciais.posisPack);
            }
            Collections.sort(pilotos, new Comparator<Piloto>() {
                @Override
                public int compare(Piloto piloto0, Piloto piloto1) {
                    return ControleCorrida.compare(piloto0, piloto1);
                }
            });
            for (int i = 0; i < pilotos.size(); i++) {
                Piloto piloto = (Piloto) pilotos.get(i);
                piloto.setPosicao(i + 1);
            }
        } catch (Exception e) {
            Logger.logarExept(e);
        }

    }

    public void mudarGiroMotor(final Object selectedItem) {
        Logger.logar("mudarGiroMotor " + selectedItem);
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    String giro = (String) selectedItem;
                    if (!Carro.GIRO_MAX.equals(giro)
                            && !Carro.GIRO_MIN.equals(giro)
                            && !Carro.GIRO_NOR.equals(giro)) {
                        return;
                    }
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_GIRO_MOTOR, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    clientPaddockPack.setGiroMotor(giro);
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void mudarModoBox() {
        Logger.logar("alterarOpcoesBox ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_MODO_BOX, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    int porcentCombust = 50;
                    String tpPneu = Carro.TIPO_PNEU_DURO;
                    String tpAsa = Carro.ASA_NORMAL;
                    clientPaddockPack.setTpPneuBox(tpPneu);
                    clientPaddockPack.setCombustBox(porcentCombust);
                    clientPaddockPack.setAsaBox(tpAsa);
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                    modoBox = ret != null;
                } catch (Exception e) {
                    Logger.logarExept(e);
                }

            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void alterarOpcoesBox(Object tpPneu, Object combust, Object asa) {
        Logger.logar("alterarOpcoesBox ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        final ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                Comandos.ALTERAR_OPCOES_BOX, sessaoCliente);
        clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
        clientPaddockPack.setTpPneuBox((String) tpPneu);
        clientPaddockPack.setCombustBox(((Integer) combust).intValue());
        clientPaddockPack.setAsaBox((String) asa);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }

            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();
    }

    public void mudarModoPilotagem(final String modo) {
        Logger.logar("mudarModoPilotagem " + modo);
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_MODO_PILOTAGEM, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    clientPaddockPack.setModoPilotagem(modo);
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }

            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void mudarAutoPos(final boolean autoPos) {
        Logger.logar("mudarAutoPos ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            autoPos
                                    ? Comandos.MUDAR_MODO_AUTOPOS_S
                                    : Comandos.MUDAR_MODO_AUTOPOS_N,
                            sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }

            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void mudarTracado(final int tracado) {
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_TRACADO, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());

                    clientPaddockPack.setTracado(tracado);
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }

        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void mudarModoDRS(final boolean modo) {
        Logger.logar("mudarModoDRS " + modo);
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    if (jogoCliente.isChovendo()) {
                        jogoCliente.info(Lang.msg("drsDesabilitado"));
                    }
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_DRS, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    clientPaddockPack.setDataObject(Boolean.valueOf(modo));
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void mudarModoKers(final boolean modo) {
        Logger.logar("mudarModoDRS " + modo);
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_KERS, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    clientPaddockPack.setDataObject(Boolean.valueOf(modo));
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void driveThru(final Piloto pilotoSelecionado) {
        if (pilotoSelecionado == null || !pilotoSelecionado.isJogadorHumano()
                || sessaoCliente.getToken()
                .equals(pilotoSelecionado.getTokenJogador())) {
            jogoCliente
                    .adicionarInfoDireto(Lang.msg("selecionePilotoDriveThru"));
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.DRIVE_THRU, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    clientPaddockPack
                            .setDataObject(pilotoSelecionado.getTokenJogador());
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public void fechaJanela() {
        if (jogoCliente != null && jogoCliente.getMainFrame() != null)
            jogoCliente.getMainFrame().setVisible(false);

    }

    public void matarCmdThread() {
        if (threadCmd != null) {
            threadCmd.interrupt();
        }
    }

    public void matarTodasThreads() {
        if (atualizadorPainel != null) {
            atualizadorPainel.interrupt();
        }
        if (threadCmd != null) {
            threadCmd.interrupt();
        }
    }

    public int getLatenciaReal() {
        if (controlePaddockCliente == null) {
            return 0;
        }
        return controlePaddockCliente.getLatenciaReal();
    }

    public void pilotoSelecionadoMinimo() {
        Logger.logar("pilotoSelecionadoMinimo ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_PILOTO_MINIMO, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();

    }

    public void pilotoSelecionadoNormal() {
        Logger.logar("pilotoSelecionadoNormal ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_PILOTO_NORMAL, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();
    }

    public void pilotoSelecionadoMaximo() {
        Logger.logar("pilotoSelecionadoMaximo ");
        if (threadCmd != null && threadCmd.isAlive()) {
            return;
        }
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
                            Comandos.MUDAR_PILOTO_MAXIMO, sessaoCliente);
                    clientPaddockPack
                            .setNomeJogo(jogoCliente.getNomeJogoCriado());
                    Object ret = controlePaddockCliente
                            .enviarObjeto(clientPaddockPack, true);
                } catch (Exception e) {
                    Logger.logarExept(e);
                }
            }
        };
        threadCmd = new Thread(runnable);
        threadCmd.start();
    }

    public boolean getModoBox() {
        return modoBox;
    }
}
