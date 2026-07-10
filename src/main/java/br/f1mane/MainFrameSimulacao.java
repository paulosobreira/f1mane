package br.f1mane;

import br.f1mane.controles.ControleCiclo;
import br.f1mane.controles.ControleJogoLocal;
import br.f1mane.entidades.Clima;
import br.f1mane.entidades.TemporadasDefault;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.visao.PainelCircuito;
import br.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Global;
import br.nnpe.Logger;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Paulo Sobreira Created on 28/08/2014
 */
public class MainFrameSimulacao extends MainFrame {

    private static final long serialVersionUID = -284357233387917389L;
    private String circuito;
    private int voltas;
    private boolean kers;
    private boolean drs;
    private boolean trocaPneus;
    private boolean reabastecimento;
    private int turbulencia;
    private String clima;
    private String temporada;
    private boolean abrasivo;
    private double fatorAcidente;
    private long seed;


    public MainFrameSimulacao(String[] args) {
        String temporadaParam = null;
        if (args.length > 0) {
            temporadaParam = args[0];
        }
        String circuitoParam = null;
        if (args.length > 1) {
            circuitoParam = args[1];
        }
        if (args.length > 2) {
            voltas = Integer.parseInt(args[2]);
        }
        setSize(1030, 720);
        String title = "Fl-Mane " + getVersao();
        setTitle(title);
        try {
            Global.DEBUG = true;
            Global.LOG_COLISAO = true;
            controleJogo = new ControleJogoLocal(3);
            controleJogo.setMainFrame(this);
            PainelCircuito.desenhaBkg = false;
            PainelCircuito.desenhaImagens = false;
            PainelCircuito.desenhaPista = false;
            ControleCiclo.VALENDO = false;
            clima = Clima.SOL;
            List<String> listCircuitos = new ArrayList<String>(controleJogo.getCircuitos().keySet());

            CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
            CarregadorRecursos.carregarTemporadas();
            carregadorRecursos.carregarTemporadasPilotos();
            List<String> listTemporadas = new ArrayList<String>(carregadorRecursos.getVectorTemps());

            if (circuitoParam != null) {
                circuito = circuitoParam;
            } else {
                Collections.shuffle(listCircuitos);
                circuito = listCircuitos.get(0);
            }

            if (temporadaParam != null) {
                temporada = temporadaParam;
            } else {
                Collections.shuffle(listTemporadas);
                temporada = listTemporadas.get(0);
            }
            Map<String, TemporadasDefault> temporadasDefaultMap =
                    carregadorRecursos.carregarTemporadasPilotosDefauts();
            TemporadasDefault temporadaDefault = temporadasDefaultMap.get("t" + temporada);
            if (voltas == 0) {
                voltas = 12;
            }
            kers = temporadaDefault.getErs().booleanValue();
            drs = temporadaDefault.getDrs().booleanValue();
            trocaPneus = temporadaDefault.getTrocaPneu().booleanValue();
            reabastecimento = temporadaDefault.getReabastecimento().booleanValue();
            turbulencia = controleJogo.getRandom().intervalo(0, 500);
            fatorAcidente = 100 - (controleJogo.getFatorAcidente() * 100);
            controleJogo.iniciarJogoMenuLocal(circuito, temporada, voltas,
                    turbulencia, clima, Global.CONTROLE_AUTOMATICO, null, kers, drs, trocaPneus,
                    reabastecimento, 0, null, null, false, true);
            Thread.sleep(5000);
            abrasivo = controleJogo.asfaltoAbrasivoReal();
            seed = controleJogo.getRandom().getSeed();
            mostraDadosSimulacao();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
    }

    public static void main(String[] args) {
        MainFrameSimulacao frame = new MainFrameSimulacao(args);
    }

    public void mostrarGraficos() {
    }

    public Graphics2D obterGraficos() {
        return null;
    }

    @Override
    public void exibirResultadoFinal(PainelTabelaResultadoFinal resultadoFinal) {
        try {
            mostraDadosSimulacao();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        super.exibirResultadoFinal(resultadoFinal);
    }

    private void mostraDadosSimulacao() throws IOException, ClassNotFoundException {
        Logger.logar("############################ Dados Simulação ############################");
        Logger.logar("Circuito : " + circuito);
        Logger.logar("Temporada : " + temporada);
        Logger.logar("Clima : " + clima);
        Logger.logar("Voltas : " + voltas);
        Logger.logar("Drs : " + drs);
        Logger.logar("Kers : " + kers);
        Logger.logar("Reabastecimento : " + reabastecimento);
        Logger.logar("Turbulencia : " + turbulencia);
        Logger.logar("TrocaPneus : " + trocaPneus);
        Logger.logar("Abrasivo : " + abrasivo);
        Logger.logar("Fator Acidente : " + fatorAcidente);
        Logger.logar("Seed usada " + seed);
        Logger.logar("#########################################################################");
    }

}
