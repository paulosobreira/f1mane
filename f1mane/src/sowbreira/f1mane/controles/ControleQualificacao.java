package sowbreira.f1mane.controles;

import java.awt.Point;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import br.nnpe.Constantes;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;

/**
 * @author Paulo Sobreira
 */
public class ControleQualificacao {

	private InterfaceJogo controleJogo;
	private ControleBox controleBox;
	private boolean modoQualify = false;

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
		List<Piloto> pilotos = controleJogo.getPilotos();
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = pilotos.get(i);
			controleBox.setupCorridaQualificacao(piloto);
		}
	}

	public void gerarGridLargada() {
		modoQualify = true;
		gerarQualificacaoAleatoria();
		Logger.logar("gerarQualificacaoAleatoria();");
		gerarVoltaQualificacaoAleatoria();
		Logger.logar("gerarVoltaQualificacaoAleatoria();");
		posicionarCarrosLargada();
		Logger.logar("gerarVoltaQualificacaoAleatoria();");
		modoQualify = false;
	}

	private void gerarVoltaQualificacaoAleatoria() {
		int position = controleJogo.getNosDaPista().size() - 1;
		No noLargada = (No) controleJogo.getNosDaPista().get(position);
		List<Piloto> pilotos = controleJogo.getPilotos();
		double incCurva = 0.6;
		double increta = 0.8;
		if (controleJogo.isSemReabastacimento()) {
			incCurva = 0.8;
			increta = 0.9;
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = pilotos.get(i);
			piloto.setNoAtual(noLargada);
			int contCiclosQualificacao = 0;
			while ((Double.valueOf(piloto.getPtosPista()) / Double
					.valueOf(controleJogo.getNosDaPista().size())) <= 1) {
				piloto.processarCiclo(controleJogo);
				contCiclosQualificacao++;
				if (Math.random() > (piloto.getCarro()
						.getPorcentagemCombustivel() / 100.0)
						&& !piloto.getNoAtual().verificaRetaOuLargada()
						&& piloto.getCarro().testeAerodinamica()
						&& piloto.testeHabilidadePilotoCarro()
						&& piloto.getCarro().testeFreios()) {
					contCiclosQualificacao -= Math.random() > incCurva ? 1 : 0;
				}
				if (Math.random() > (piloto.getCarro()
						.getPorcentagemCombustivel() / 100.0)
						&& piloto.getNoAtual().verificaRetaOuLargada()
						&& piloto.getCarro().testePotencia()) {
					contCiclosQualificacao -= Math.random() > increta ? 1 : 0;
				}
				if (piloto.getCarro()
						.verificaPneusIncompativeisClima(controleJogo)) {
					contCiclosQualificacao++;
				}
			}
			int modMili = 120;
			for (int j = 0; j < 10; j++) {
				if (piloto.testeHabilidadePiloto()) {
					modMili -= 2;
				} else {
					piloto.incStress(40);
				}
				if (piloto.getCarro().testePotencia()) {
					modMili -= 2;
				} else {
					piloto.incStress(10);
				}
				if (piloto.getCarro().testeFreios()) {
					modMili -= 1;
				} else {
					piloto.incStress(10);
				}
				if (piloto.getCarro().testeAerodinamica()) {
					modMili -= 3;
				} else {
					piloto.incStress(10);
				}

			}
			piloto.setCiclosVoltaQualificacao(Util.inteiro(
					((contCiclosQualificacao * Constantes.CICLO) + modMili)));
			piloto.setNumeroVolta(-1);
			piloto.setUltimaVolta(null);
			piloto.setVoltaAtual(null);
			piloto.setTravouRodas(0);
			piloto.setVoltas(new ArrayList());
			controleJogo.zerarMelhorVolta();
		}
		nivelaPontecia(pilotos);
		nivelaHabilidade(pilotos);
		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;
				return new Integer(piloto0.getCiclosVoltaQualificacao())
						.compareTo(new Integer(
								piloto1.getCiclosVoltaQualificacao()));
			}
		});
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
					p.setCiclosVoltaQualificacao(
							p.getCiclosVoltaQualificacao() - 1);
				}
			}
			if (diffTodosIn) {
				diffTodos = true;
			}
		}

	}

	private void nivelaHabilidade(List<Piloto> pilotos) {
		int valor = 0;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.setHabilidadeAntesQualify(piloto.getHabilidade());
			valor += piloto.getHabilidade();
		}
		valor = valor / pilotos.size();

		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			int diff = 0;
			if (piloto.getHabilidade() > valor) {
				diff = piloto.getHabilidade() - valor;
				piloto.setHabilidade(
						piloto.getHabilidade() - Util.intervalo(diff/2, diff));
			} else {
				diff = valor - piloto.getHabilidade();
				piloto.setHabilidade(
						piloto.getHabilidade() + Util.intervalo(diff/2, diff));
			}

		}
		Logger.logar("-----------------=====nivelaHabilidade=====----------------");
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			Logger.logar(piloto.toString() + " HabilidadeAntesQualify : "
					+ piloto.getHabilidadeAntesQualify() + " Habilidade: "
					+ piloto.getHabilidade());
		}
	}

	private void nivelaPontecia(List<Piloto> pilotos) {
		int valor = 0;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.getCarro().setPotenciaAntesQualify(
					piloto.getCarro().getPotencia());
			valor += piloto.getCarro().getPotencia();
		}
		valor = valor / pilotos.size();

		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			int diff = 0;
			if (piloto.getCarro().getPotencia() > valor) {
				diff = piloto.getCarro().getPotencia() - valor;
				piloto.getCarro().setPotencia(
						piloto.getCarro().getPotencia() - Util.intervalo(diff/2, diff));
			} else {
				diff = valor - piloto.getCarro().getPotencia();
				piloto.getCarro().setPotencia(
						piloto.getCarro().getPotencia() + Util.intervalo(diff/2, diff));
			}

		}
		Logger.logar("-----------------=====nivelaPontecia=====----------------");
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			Logger.logar(piloto.toString() + " getPotenciaAntesQualify : "
					+ piloto.getCarro().getPotenciaAntesQualify() + " getPotencia: "
					+ piloto.getCarro().getPotencia());
		}
	}

	public boolean isModoQualify() {
		return modoQualify;
	}

	public void posicionarCarrosLargada() {
		Circuito circuito = controleJogo.getCircuito();
		for (int i = 0; i < controleJogo.getPilotos().size(); i++) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			int iP = 50 + Util.inteiro((Carro.LARGURA * .8) * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull()
					.get(circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inteiro(n1.getPoint().x),
					Util.inteiro(n1.getPoint().y));
			Point pm = new Point(Util.inteiro(nM.getPoint().x),
					Util.inteiro(nM.getPoint().y));
			Point p2 = new Point(Util.inteiro(n2.getPoint().x),
					Util.inteiro(n2.getPoint().y));

			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)), (pm.y - (Carro.MEIA_ALTURA)),
					(Carro.LARGURA), (Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo,
					Util.inteiro(Carro.ALTURA * 1.2),
					new Point(Util.inteiro(rectangle.getCenterX()),
							Util.inteiro(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180,
					Util.inteiro(Carro.ALTURA * 1.2),
					new Point(Util.inteiro(rectangle.getCenterX()),
							Util.inteiro(rectangle.getCenterY())));
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
			if (!piloto.isJogadorHumano() && !controleJogo.verificaNivelJogo()
					&& !piloto.testeHabilidadePilotoCarro()) {
				piloto.setCiclosDesconcentrado(Util.intervalo(500, 700));
				piloto.setProblemaLargada(true);
			}
			Carro carro = piloto.getCarro();
			carro.setTempMax(carro.getPotencia() / 4);
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				carro.setTempMax(carro.getPotencia() / 2);
			}
			if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				carro.setTempMax(carro.getPotencia() / 6);
			}
			carro.setDurabilidadeAereofolio(
					controleJogo.getDurabilidadeAreofolio());
			piloto.calculaCarrosAdjacentes(controleJogo);
			Logger.logar(" Posição Largada :" + piloto.getPosicao() + " Nome : "
					+ piloto.getNome() + " Pneu : "
					+ piloto.getCarro().getTipoPneu() + " Combustivel : "
					+ piloto.getCarro().getPorcentagemCombustivel() + " Asa : "
					+ piloto.getCarro().getAsa() + " Tempo Qualificação : "
					+ ControleEstatisticas.formatarTempo(
							piloto.getCiclosVoltaQualificacao()));
		}

	}
}
