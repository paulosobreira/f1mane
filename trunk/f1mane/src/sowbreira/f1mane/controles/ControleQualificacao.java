package sowbreira.f1mane.controles;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
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
		Circuito circuito = controleJogo.getCircuito();
		for (int i = 0; i < controleJogo.getPilotos().size(); i++) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			int iP = 50 + Util.inte(Carro.LARGURA * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x), Util.inte(n1
					.getPoint().y));
			Point pm = new Point(Util.inte(nM.getPoint().x), Util.inte(nM
					.getPoint().y));
			Point p2 = new Point(Util.inte(n2.getPoint().x), Util.inte(n2
					.getPoint().y));

			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));
			if (i % 2 == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA)),
						(cima.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
				piloto.setTracado(2);
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA)),
						(baixo.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
						(Carro.ALTURA));
				piloto.setTracado(1);
			}
			piloto.setNoAtual(nM);
			piloto.setPosicao(i + 1);
			piloto.zerarGanho();
			piloto.setPtosPista(nM.getIndex());
			piloto.setPtosPistaIncial(nM.getIndex());
			piloto.setVelocidade(0);
			Carro carro = piloto.getCarro();
			carro.setDurabilidadeAereofolio(Util.inte(10 - (controleJogo
					.getNiveljogo() * 10)));

			Logger.logar(" PosLarg " + piloto.getPosicao() + " Nome "
					+ piloto.getNome() + " pts " + piloto.getPtosPista());
		}

	}

	public void iniciarQualificacao(int tempoQualificacao) {
		// TODO Auto-generated method stub

	}
}
