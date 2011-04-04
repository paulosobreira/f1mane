package sowbreira.f1mane.controles;

import java.util.Iterator;
import java.util.List;

import br.nnpe.Html;
import br.nnpe.Util;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class ControleSafetyCar {
	private ControleCorrida controleCorrida;
	private ControleJogoLocal controleJogo;
	private SafetyCar safetyCar;
	private ThreadRecolihimentoCarro recolihimentoCarro;

	/**
	 * @param controleCorrida
	 * @param controleJogo
	 */
	public ControleSafetyCar(ControleJogoLocal controleJogo,
			ControleCorrida controleCorrida) {
		super();
		this.controleCorrida = controleCorrida;
		this.controleJogo = controleJogo;
		safetyCar = new SafetyCar();
	}

	public boolean isSaftyCarNaPista() {
		return (safetyCar.isNaPista());
	}

	public void safetyCarNaPista(Piloto piloto) {
		if (safetyCar.isNaPista()) {
			return;
		}
//		System.out.println("======SC PISTA======"
//				+ controleJogo.getNumVoltaAtual());
		int pts = controleCorrida.calculaQtdePtsPistaPoleParaSaidaBox();
		safetyCar.setNoAtual(controleCorrida.getNoSaidaBox());
		safetyCar.setPtosPista(pts);
		safetyCar.setNaPista(true);
		safetyCar.setVaiProBox(false);
		controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("029")));
		recolihimentoCarro = new ThreadRecolihimentoCarro(controleJogo, piloto,
				safetyCar);
		recolihimentoCarro.start();

	}

	public void processarCiclo() {
		if (!safetyCar.isNaPista()) {
			return;
		}
		int cont = safetyCar.getNoAtual().getIndex();
		Circuito circuito = controleJogo.getCircuito();
		if ((cont > (circuito.getEntradaBoxIndex() - 50) && cont < (circuito
				.getEntradaBoxIndex() + 50)) && safetyCar.isVaiProBox()) {
			controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("030")));
			safetyCar.setNaPista(false);
			safetyCar.setSaiuVolta(controleJogo.getNumVoltaAtual());
		}

		List pista = controleJogo.getNosDaPista();
		int index = safetyCar.getNoAtual().getIndex();
		No noAtual = safetyCar.getNoAtual();
		int bonus = noAtual.verificaCruvaBaixa() || noAtual.verificaCruvaAlta() ? ((Math
				.random() > .5) ? 2 : 1) : (Math.random() > .3) ? 2 : 1;
		Piloto pole = (Piloto) controleJogo.getPilotos().get(0);
		if (safetyCar.getPtosPista() > (pole.getPtosPista() + 200)) {
			bonus = (Math.random() > .5) ? 1 : 0;
			safetyCar.setTracado(0);
		}
		bonus *= (controleJogo.getCircuito().getMultiplciador() * controleJogo
				.getIndexVelcidadeDaPista()) * 0.7;
		bonus = calculaMediaSC(bonus);
		index += bonus;
		int diff = index - pista.size();
		/**
		 * Completou Volta
		 */
		if (diff >= 0) {
			index = diff;
		}
		safetyCar.setPtosPista(safetyCar.getPtosPista() + bonus);
		if (verificaTracadoPilto(index)) {
			safetyCar.setTracado(1);
		}
		safetyCar.setNoAtual((No) pista.get(index));
	}

	private boolean verificaTracadoPilto(int indice) {
		List pilotos = controleJogo.getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (this.equals(piloto)
					|| piloto.getTracado() != safetyCar.getTracado()) {
				continue;
			}

			int indiceCarro = piloto.getNoAtual().getIndex();

			int traz = indiceCarro - Carro.LARGURA;
			int frente = indiceCarro + Carro.LARGURA;

			List lista = piloto.obterPista(controleJogo);

			if (traz < 0) {
				traz = (lista.size() - 1) + traz;
			}
			if (frente > (lista.size() - 1)) {
				frente = (frente - (lista.size() - 1)) - 1;
			}

			if (indice >= traz && indice <= frente) {
				return true;
			}
		}
		return false;

	}

	private int calculaMediaSC(int bonus) {
		List listGanho = safetyCar.getMediaSc();
		if (listGanho.size() > 10) {
			listGanho.remove(0);
		}
		listGanho.add(new Double(bonus));
		double soma = 0;
		for (Iterator iterator = listGanho.iterator(); iterator.hasNext();) {
			Double val = (Double) iterator.next();
			soma += val.doubleValue();
		}
		return (int) (soma / listGanho.size());
	}

	public SafetyCar getSafetyCar() {
		return safetyCar;
	}

	public boolean verificaPoleFrenteSafety(Piloto piloto) {
		return piloto.getPtosPista() >= safetyCar.getPtosPista();
	}

	public boolean isSafetyCarVaiBox() {
		return safetyCar.isVaiProBox();
	}

	public boolean safetyCarUltimas3voltas() {
		if (safetyCar.getSaiuVolta() == 0) {
			return false;
		}
		return (controleJogo.getNumVoltaAtual() - safetyCar.getSaiuVolta()) < 3;
	}

	public void matarThreads() {
		if (recolihimentoCarro != null) {
			recolihimentoCarro.interrupt();
		}

	}
}
