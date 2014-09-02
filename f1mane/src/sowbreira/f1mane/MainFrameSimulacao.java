package sowbreira.f1mane;

import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.visao.PainelCircuito;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Logger;
import br.nnpe.Util;

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
		String title = "F1-MANE " + getVersao() + " MANager & Engineer";
		setTitle(title);
		try {
			controleJogo = new ControleJogoLocal();
			controleJogo.setMainFrame(this);
			Logger.ativo = true;
			PainelCircuito.desenhaBkg = false;
			PainelCircuito.desenhaImagens = false;
			PainelCircuito.desenhaPista = false;
			int intervaloClima = Util.intervalo(1, 3);
			int intervaloNivel = Util.intervalo(1, 3);
			switch (intervaloClima) {
			case 1:
				clima = Clima.NUBLADO;
				break;
			case 2:
				clima = Clima.SOL;
				break;
			case 3:
				clima = Clima.CHUVA;
				break;

			default:
				break;
			}
			switch (intervaloNivel) {
			case 1:
				nivel = InterfaceJogo.FACIL;
				break;
			case 2:
				nivel = InterfaceJogo.NORMAL;
				break;
			case 3:
				nivel = InterfaceJogo.DIFICIL;
				break;

			default:
				break;
			}
			List<String> listCircuitos = new ArrayList<String>();
			listCircuitos.addAll(controleJogo.getCircuitos().keySet());
			Collections.shuffle(listCircuitos);

			CarregadorRecursos carregadorRecursos = new CarregadorRecursos(true);
			carregadorRecursos.carregarTemporadasPilotos();
			List<String> listTemporadas = new ArrayList<String>();
			listTemporadas.addAll(carregadorRecursos.getVectorTemps());
			Collections.shuffle(listTemporadas);

			circuito = listCircuitos.get(0);
			temporada = listTemporadas.get(0);
			voltas = Util.intervalo(12, 72);
			kers = Math.random() > 0.5;
			drs = Math.random() > 0.5;
			trocaPneus = Math.random() > 0.5;
			reabastecimento = Math.random() > 0.5;
			turbulencia = Util.intervalo(130, 370);
			controleJogo.iniciarJogoMenuLocal(circuito, temporada, voltas,
					turbulencia, clima, nivel, null, kers, drs, trocaPneus,
					reabastecimento, 0, null, null);
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
		mostraDadosSimulacao();
		super.exibirResultadoFinal(resultadoFinal);
	}

	private void mostraDadosSimulacao() {
		System.out
				.println("############################ Dados Simulação ############################");
		System.out.println("Circuito : " + circuito);
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
