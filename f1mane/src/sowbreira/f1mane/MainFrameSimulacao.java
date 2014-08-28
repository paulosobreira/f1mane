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
			List<String> list = new ArrayList<String>();
			list.addAll(controleJogo.getCircuitos().keySet());
			Collections.shuffle(list);
			circuito = list.get(0);
			voltas = Util.intervalo(12, 72);
			kers = Math.random() > 0.5;
			drs = Math.random() > 0.5;
			trocaPneus = Math.random() > 0.5;
			trocaPneus = true;
			reabastecimento = Math.random() > 0.5;
			turbulencia = Util.intervalo(130, 370);
			controleJogo.iniciarJogoMenuLocal(circuito, "2014", voltas,
					turbulencia, Clima.SOL, InterfaceJogo.NORMAL, null, kers,
					drs, trocaPneus, reabastecimento, 0, null, null);
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
		System.out.println("Voltas : " + voltas);
		System.out.println("Drs : " + drs);
		System.out.println("Kers : " + kers);
		System.out.println("Reabastecimento : " + reabastecimento);
		System.out.println("Turbulencia : " + turbulencia);
		System.out.println("TrocaPneus : " + trocaPneus);
		System.out
				.println("#########################################################################");
	}

}
