package br.f1mane;

import br.f1mane.controles.ControleCiclo;
import br.f1mane.controles.ControleJogoLocal;
import br.f1mane.controles.ControleRecursos;
import br.f1mane.controles.InterfaceJogo;
import br.f1mane.entidades.Clima;
import br.f1mane.entidades.TemporadasDefault;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.visao.PainelCircuito;
import br.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Logger;
import br.nnpe.Util;

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
    private String nivel;
    private String temporada;
    private boolean abrasivo;
    private boolean boxRapido;
    private double fatorAcidente;

    public MainFrameSimulacao() {
        setSize(1030, 720);
        String title = "Fl-Mane " + getVersao() + " MANager & Engineer";
        setTitle(title);
        try {
            controleJogo = new ControleJogoLocal();
            controleJogo.setMainFrame(this);
            Logger.ativo = true;
            PainelCircuito.desenhaBkg = false;
            PainelCircuito.desenhaImagens = false;
            PainelCircuito.desenhaPista = false;
            ControleCiclo.VALENDO = false;
            clima = Clima.SOL;
            nivel = InterfaceJogo.NORMAL;
            List<String> listCircuitos = new ArrayList<String>(controleJogo.getCircuitos().keySet());
            Collections.shuffle(listCircuitos);

            CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
            carregadorRecursos.carregarTemporadas();
            carregadorRecursos.carregarTemporadasPilotos();
            List<String> listTemporadas = new ArrayList<String>(carregadorRecursos.getVectorTemps());
            Collections.shuffle(listTemporadas);

            circuito = listCircuitos.get(0);
            temporada = listTemporadas.get(0);
            Map<String, TemporadasDefault> temporadasDefaultMap =
                    carregadorRecursos.carregarTemporadasPilotosDefauts();
            TemporadasDefault temporadaDefault = temporadasDefaultMap.get("t" + temporada);
            voltas = Util.intervalo(12, 72);
            kers = temporadaDefault.getErs();
            drs = temporadaDefault.getDrs();
            trocaPneus = temporadaDefault.getTrocaPneu();
            reabastecimento = temporadaDefault.getReabastecimento();
            turbulencia = Util.intervalo(0, 500);
            controleJogo.iniciarJogoMenuLocal(circuito, temporada, voltas,
                    turbulencia, clima, nivel, null, kers, drs, trocaPneus,
                    reabastecimento, 0, null, null, false);
            abrasivo = controleJogo.asfaltoAbrasivo();
            boxRapido = controleJogo.isBoxRapido();
            fatorAcidente = 100 - (controleJogo.getFatorAcidente() * 100);
            mostraDadosSimulacao();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
    }

    public static void main(String[] args) {
        MainFrameSimulacao frame = new MainFrameSimulacao();
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
            e.printStackTrace();
        }
        super.exibirResultadoFinal(resultadoFinal);
    }

    private void mostraDadosSimulacao() throws IOException, ClassNotFoundException {
        System.out
                .println("############################ Dados Simulação ############################");
        System.out.println("Circuito : " + circuito);
        System.out.println("VelocidadePista : " + CarregadorRecursos.carregarCircuito((String) controleJogo.getCircuitos().get(circuito)).getVelocidadePista());
        System.out.println("Temporada : " + temporada);
        System.out.println("Clima : " + clima);
        System.out.println("Nivel : " + nivel);
        System.out.println("Voltas : " + voltas);
        System.out.println("Drs : " + drs);
        System.out.println("Kers : " + kers);
        System.out.println("Reabastecimento : " + reabastecimento);
        System.out.println("Turbulencia : " + turbulencia);
        System.out.println("TrocaPneus : " + trocaPneus);
        System.out.println("Abrasivo : " + abrasivo);
        System.out.println("Box Rapido : " + boxRapido);
        System.out.println("Fator Acidente : " + fatorAcidente);
        System.out
                .println("#########################################################################");
    }

}
