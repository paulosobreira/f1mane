package sowbreira.f1mane.controles;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import br.nnpe.GeoUtil;
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
		posicionarCarrosLargada();
	}

	private void gerarVoltaQualificacaoAleatoria() {
		modoQualify = true;
		int position = controleJogo.getNosDaPista().size() - 1;
		No noLargada = (No) controleJogo.getNosDaPista().get(position);
		List pilotos = controleJogo.getPilotos();
		nivelaPontecia(pilotos);
		nivelaHabilidade(pilotos);
		double incCurva = 0.5;
		double increta = 0.7;
		if (controleJogo.isSemReabastacimento()) {
			incCurva = 0.7;
			increta = 0.9;
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			piloto.setNoAtual(noLargada);
			int contCiclosQualificacao = 0;
			while ((Double.valueOf(piloto.getPtosPista()) / Double
					.valueOf(controleJogo.getNosDaPista().size())) <= 1) {
				piloto.processarCiclo(controleJogo);
				contCiclosQualificacao++;
				if (Math.random() > (piloto.getCarro().porcentagemCombustivel() / 100.0)
						&& !piloto.getNoAtual().verificaRetaOuLargada()
						&& piloto.getCarro().testeAerodinamica()
						&& piloto.getCarro().testeFreios()) {
					contCiclosQualificacao -= Math.random() > incCurva ? 1 : 0;
				}
				if (Math.random() > (piloto.getCarro().porcentagemCombustivel() / 100.0)
						&& piloto.getNoAtual().verificaRetaOuLargada()
						&& piloto.testeHabilidadePilotoCarro(controleJogo)) {
					contCiclosQualificacao -= Math.random() > increta ? 1 : 0;
				}
				if (piloto.getCarro().verificaPneusIncompativeisClima(
						controleJogo)) {
					contCiclosQualificacao++;
				}
			}
			int modMili = 9;
			for (int j = 0; j < 9; j++) {
				if (piloto.testeHabilidadePiloto(controleJogo)) {
					modMili--;
				}
			}
			piloto.setCiclosVoltaQualificacao(Util
					.inte(((contCiclosQualificacao * controleJogo
							.getTempoCiclo()) + modMili)));
			piloto.setNumeroVolta(0);
			piloto.setUltimaVolta(null);
			piloto.setVoltaAtual(null);
			piloto.setContTravouRodas(0);
			piloto.setCiclosDesconcentrado(0);
			piloto.setVoltas(new ArrayList());
			if (controleJogo.asfaltoAbrasivo()
					&& Carro.TIPO_PNEU_MOLE.equals(piloto.getCarro()
							.getTipoPneu())
					&& piloto.getCarro().porcentagemDesgastePeneus() < 80) {
				piloto.getCarro().setPorcentPneus(80);
			}
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

	private void evitaMesmoCiclo(Piloto p) {
		List pilotos = controleJogo.getPilotos();
		boolean diffTodos = false;
		while (!diffTodos) {
			boolean diffTodosIn = true;
			for (int i = 0; i < pilotos.size(); i++) {
				Piloto piloto = (Piloto) pilotos.get(i);
				if (p.getCiclosVoltaQualificacao() != piloto
						.getCiclosVoltaQualificacao() || p.equals(piloto)) {
					continue;
				} else {
					diffTodosIn = false;
					p.setCiclosVoltaQualificacao(p.getCiclosVoltaQualificacao() - 1);
				}
			}
			if (diffTodosIn) {
				diffTodos = true;
			}
		}

	}

	private void nivelaHabilidade(List pilotos) {
		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;
				return new Integer(piloto0.getHabilidade())
						.compareTo(new Integer(piloto1.getHabilidade()));
			}
		});
		int limite = -1;
		Piloto ant = null;
		int maiorDiff = 0;
		while (maiorDiff > limite) {
			limite = Util.intervalo(5, 10);
			maiorDiff = 0;
			ant = null;
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (ant != null) {
					int diff = piloto.getHabilidade() - ant.getHabilidade();
					if (diff > limite) {
						ant.setHabilidadeAntesQualify(ant.getHabilidade());
						ant.setHabilidade(piloto.getHabilidade() - limite);
					}
					if (diff > maiorDiff) {
						maiorDiff = diff;
					}
				}
				ant = piloto;
			}
		}
	}

	private void nivelaPontecia(List pilotos) {
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
			limite = Util.intervalo(5, 10);
			maiorDiff = 0;
			ant = null;
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (ant != null) {
					int diff = piloto.getCarro().getPotencia()
							- ant.getCarro().getPotencia();
					if (diff > limite) {
						ant.getCarro().setPotenciaAntesQualify(
								ant.getCarro().getPotencia());
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
	}

	public boolean isModoQualify() {
		return modoQualify;
	}

	public void posicionarCarrosLargada() {
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

			Point cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA * 1.2),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA * 1.2),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
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
			piloto.setIndiceTracado(0);
			piloto.setNoAtual(nM);
			piloto.setPosicao(i + 1);
			piloto.setPosicaoInicial(piloto.getPosicao());
			piloto.zerarGanhoEVariaveisUlt();
			piloto.setPtosPista(nM.getIndex());
			piloto.setPtosPistaIncial(nM.getIndex());
			Carro carro = piloto.getCarro();
			carro.setTempMax(carro.getPotencia() / 4);
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				carro.setTempMax(carro.getPotencia() / 2);
			}
			if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				carro.setTempMax(carro.getPotencia() / 6);
			}
			carro.setDurabilidadeAereofolio(controleJogo
					.getDurabilidadeAreofolio());
			Logger.logar(" Posição Largada :"
					+ piloto.getPosicao()
					+ " Nome : "
					+ piloto.getNome()
					+ " Pneu : "
					+ piloto.getCarro().getTipoPneu()
					+ " Combustivel : "
					+ piloto.getCarro().porcentagemCombustivel()
					+ " Asa : "
					+ piloto.getCarro().getAsa()
					+ " Tempo Qualificação : "
					+ ControleEstatisticas.formatarTempo(piloto
							.getCiclosVoltaQualificacao()));
		}

	}
}
