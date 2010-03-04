package sowbreira.f1mane.controles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class ControleQualificacao {

	private InterfaceJogo controleJogo;
	private ControleBox controleBox;
	public static boolean modoQualify = false;

	/**
	 * @param controleJogo
	 */
	public ControleQualificacao(InterfaceJogo controleJogo,
			ControleBox controleBox) {
		super();
		this.controleJogo = controleJogo;
		this.controleBox = controleBox;
	}

	private void gerarQualificacaoAleatoria() {
		List pilotos = controleJogo.getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.setNotaQualificacaoAleatoria((piloto.getHabilidade() / 10)
					+ (5 * Math.random()) + piloto.getCarro().getPotencia()
					+ (50 * Math.random()));
		}

		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;

				return Double.compare(piloto1.getNotaQualificacaoAleatoria(),
						piloto0.getNotaQualificacaoAleatoria());
			}
		});
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			controleBox.setupCorridaQualificacaoAleatoria(piloto, i + 1);
		}
	}

	public void gerarGridLargadaSemQualificacao() {
		gerarQualificacaoAleatoria();
		gerarVoltaQualificacaoAleatoria();
		posiscionarCarrosLargada();

	}

	public int obterConsumoVolta(int distaciaCorrida) {

		modoQualify = true;
		int position = controleJogo.getNosDaPista().size() - 1;
		No noLargada = (No) controleJogo.getNosDaPista().get(position);
		List pilotos = controleJogo.getPilotos();
		Piloto piloto = (Piloto) pilotos.get(0);
		Piloto pilotoTeste = null;
		try {
			pilotoTeste = (Piloto) Util.deepCopy(piloto);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		pilotoTeste.setCombustJogador(new Integer(
				(distaciaCorrida + (distaciaCorrida / 2))));
		int consumo = pilotoTeste.getCombustJogador();
		pilotoTeste.setNoAtual(noLargada);
		pilotoTeste.getCarro().setPneuDuro(distaciaCorrida);
		pilotoTeste.getCarro().setTanqueCheio(consumo);
		pilotoTeste.getCarro().setCombustivel(consumo);
		controleBox.setupCorridaQualificacaoAleatoria(pilotoTeste, 1);
		int contCiclosQualificacao = 0;
		List listPiltos = new ArrayList();
		listPiltos.add(pilotoTeste);
		controleJogo.setPilotos(listPiltos);
		while (pilotoTeste.getNumeroVolta() < 1) {
			pilotoTeste.processarCiclo(controleJogo);
			contCiclosQualificacao++;
		}
		pilotoTeste.setNumeroVolta(0);
		pilotoTeste.setCiclosVoltaQualificacao(contCiclosQualificacao);
		controleJogo.zerarMelhorVolta();
		modoQualify = false;
		controleJogo.setPilotos(pilotos);
		return consumo - pilotoTeste.getCarro().getCombustivel();

	}

	private void gerarVoltaQualificacaoAleatoria() {
		modoQualify = true;
		int position = controleJogo.getNosDaPista().size() - 1;
		No noLargada = (No) controleJogo.getNosDaPista().get(position);
		List pilotos = controleJogo.getPilotos();
		Set jogandoresHumanos = new HashSet();
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			if (piloto.isJogadorHumano()) {
				jogandoresHumanos.add(piloto);
			}
			piloto.setJogadorHumano(false);
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			piloto.setNoAtual(noLargada);
			int contCiclosQualificacao = 0;
			while (piloto.getNumeroVolta() < 1) {
				piloto.processarCiclo(controleJogo);
				contCiclosQualificacao++;
			}
			piloto.setNumeroVolta(0);
			piloto.setCiclosVoltaQualificacao(contCiclosQualificacao);
			piloto.setUltimaVolta(null);
			piloto.setVoltaAtual(null);
			piloto.setVoltas(new ArrayList());
			controleJogo.zerarMelhorVolta();
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			if (jogandoresHumanos.contains(piloto)) {
				piloto.setJogadorHumano(true);
			}
		}
		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;

				return new Integer(piloto0.getCiclosVoltaQualificacao())
						.compareTo(new Integer(piloto1
								.getCiclosVoltaQualificacao()));
			}
		});
		modoQualify = false;
	}

	public boolean isModoQualify() {
		return modoQualify;
	}

	public void posiscionarCarrosLargada() {
		int position = controleJogo.getNosDaPista().size() - 1;

		for (int i = 0; i < controleJogo.getPilotos().size(); i++) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			No no = (No) controleJogo.getNosDaPista().get(position);
			no.setIndex(position);
			piloto.setNoAtual(no);
			piloto.setPosicao(i + 1);
			position -= 5;
			piloto.setPtosPista(i * -5);
			piloto.setVelocidade(0);
		}

	}

	public void iniciarQualificacao(int tempoQualificacao) {
		// TODO Auto-generated method stub

	}
}
