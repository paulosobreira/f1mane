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
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			controleBox.setupCorridaQualificacaoAleatoria(piloto);
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
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			System.out.println(piloto + " Piloto " + piloto.getHabilidade()
					+ " Motor" + piloto.getCarro().getPotencia());

		}
		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;
				return new Integer(piloto0.getCarro().getPotencia())
						.compareTo(new Integer(piloto1.getCarro().getPotencia()));
			}
		});
		int limite = -1;
		Piloto ant = null;
		int maiorDiff = 0;
		while (maiorDiff > limite) {
			limite = 5;
			if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
				limite = 10;
			}
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				limite = 15;
			}
			maiorDiff = 0;
			ant = null;
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (ant != null) {
					int diff = piloto.getCarro().getPotencia()
							- ant.getCarro().getPotencia();
					if (diff > limite) {
						ant.getCarro().setPotencia(
								piloto.getCarro().getPotencia() - limite);
					}
					if (diff > maiorDiff) {
						maiorDiff = diff;
					}
				}
				ant = piloto;
			}
		}

		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;
				return new Integer(piloto0.getHabilidade())
						.compareTo(new Integer(piloto1.getHabilidade()));
			}
		});
		limite = -1;
		ant = null;
		maiorDiff = 0;
		while (maiorDiff > limite) {
			limite = 5;
			if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
				limite = 10;
			}
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				limite = 15;
			}
			maiorDiff = 0;
			ant = null;
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (ant != null) {
					int diff = piloto.getHabilidade() - ant.getHabilidade();
					if (diff > limite) {
						ant.setHabilidade(piloto.getHabilidade() - limite);
					}
					if (diff > maiorDiff) {
						maiorDiff = diff;
					}
				}
				ant = piloto;
			}
		}
		System.out.println("==============");
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			System.out.println(piloto + " Piloto " + piloto.getHabilidade()
					+ " Motor" + piloto.getCarro().getPotencia());

		}

		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			piloto.setNoAtual(noLargada);
			int contCiclosQualificacao = 0;
			while ((Double.valueOf(piloto.getPtosPista()) / Double
					.valueOf(controleJogo.getNosDaPista().size())) <= 1) {
				piloto.processarCiclo(controleJogo);
				contCiclosQualificacao++;
				if (Math.random() < 0.3
						&& piloto.testeHabilidadePilotoCarro(controleJogo)) {
					contCiclosQualificacao--;
					if (Carro.TIPO_PNEU_MOLE.equals(piloto.getCarro()
							.getTipoPneu())
							&& Math.random() < 0.05) {
						contCiclosQualificacao--;
					}
				}
			}
			piloto.setNumeroVolta(0);
			piloto.setCiclosVoltaQualificacao(contCiclosQualificacao);
			piloto.setUltimaVolta(null);
			piloto.setVoltaAtual(null);
			piloto.setVoltas(new ArrayList());
			piloto.setContTravouRodas(0);
			controleJogo.zerarMelhorVolta();
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
			int iP = 50 + Util.inte((Carro.LARGURA * .8) * i);
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
			int durabilidade = InterfaceJogo.DUR_AREO_NORMAL;
			carro.setTempMax(carro.getPotencia() / 4);
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				durabilidade = InterfaceJogo.DUR_AREO_FACIL;
				carro.setTempMax(carro.getPotencia() / 2);
			}
			if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				durabilidade = InterfaceJogo.DUR_AREO_DIFICIL;
				carro.setTempMax(carro.getPotencia() / 6);
			}
			carro.setDurabilidadeAereofolio(durabilidade);

			Logger.logar(" PosLarg " + piloto.getPosicao() + " Nome "
					+ piloto.getNome() + " pts " + piloto.getPtosPista());
		}

	}

	public void iniciarQualificacao(int tempoQualificacao) {
		// TODO Auto-generated method stub

	}
}
