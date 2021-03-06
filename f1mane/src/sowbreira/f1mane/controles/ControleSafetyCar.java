package sowbreira.f1mane.controles;

import java.util.Iterator;
import java.util.List;

import br.nnpe.Html;
import br.nnpe.Logger;
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
	private Piloto pilotoBateu;

	/**
	 * @param controleCorrida
	 * @param controleJogo
	 */
	public ControleSafetyCar(ControleJogoLocal controleJogo, ControleCorrida controleCorrida) {
		super();
		this.controleCorrida = controleCorrida;
		this.controleJogo = controleJogo;
		safetyCar = new SafetyCar();
	}

	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo, Piloto piloto) {
		if (piloto.getPosicao() != 1 && piloto.getCarroPilotoDaFrente() != null) {
			Piloto pilotoFrente = piloto.getCarroPilotoDaFrente().getPiloto();
			if (pilotoFrente.getPtosBox() != 0 || pilotoFrente.getCarro().verificaParado()
					|| Carro.PNEU_FURADO.equals(pilotoFrente.getCarro().getDanificado())
					|| Carro.PERDEU_AEREOFOLIO.equals(pilotoFrente.getCarro().getDanificado())
					|| piloto.verificaNaoPrecisaDesviar(pilotoFrente)
					|| piloto.getNumeroVolta() != pilotoFrente.getNumeroVolta()) {
				return ganho;
			}
			long diffIndex = piloto.getDiferencaParaProximo();
			if (diffIndex < 100) {
				ganho = 1;
			} else {
				ganho = limitaGanho(ganho, diffIndex, 150);
			}
		} else {
			long indexNafrente = safetyCar.getPtosPista();
			long index = piloto.getPtosPista();
			if (indexNafrente < index) {
				indexNafrente += controleJogo.getNosDaPista().size();
			}
			long diffIndex = (indexNafrente - index);
			if (diffIndex < 100) {
				ganho = 1;
			} else {
				long max = 150;
				if (safetyCar.isVaiProBox()) {
					max = 300;
				}
				ganho = limitaGanho(ganho, diffIndex, max);
			}

		}
		if (ganho > 30) {
			ganho = 30;
		}
		if (ganho < 15 && piloto.getDiferencaParaProximo() > 200) {
			ganho = 15;
		}
		if (piloto.getTracado() == 4) {
			piloto.mudarTracado(2, controleJogo);
		}
		if (piloto.getTracado() == 5) {
			piloto.mudarTracado(1, controleJogo);
		}

		return ganho;
	}

	private double limitaGanho(double ganho, long diffIndex, long max) {
		if (diffIndex < max) {
			for (double i = 1; i < max; i += 5) {
				if (diffIndex < i) {
					ganho *= i / max;
					break;
				}
			}
		}
		return ganho;
	}

	public boolean isSaftyCarNaPista() {
		return (safetyCar.isNaPista());
	}

	public void safetyCarNaPista(Piloto piloto) {
		if (safetyCar.isNaPista()) {
			return;
		}
		pilotoBateu = piloto;
		long pts = controleCorrida.calculaQtdePtsPistaPoleParaSaidaBox();
		safetyCar.setNoAtual(controleCorrida.getNoSaidaBox());
		safetyCar.setPtosPista(pts);
		safetyCar.setNaPista(true);
		Logger.logar("SAFETY CAR");
		safetyCar.setVaiProBox(false);
		controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("029")));
		recolihimentoCarro = new ThreadRecolihimentoCarro(controleJogo, piloto, safetyCar);
		recolihimentoCarro.start();

	}

	public void processarCiclo() {
		if (!safetyCar.isNaPista()) {
			return;
		}
		int cont = safetyCar.getNoAtual().getIndex();
		Circuito circuito = controleJogo.getCircuito();
		if ((cont > (circuito.getEntradaBoxIndex() - 50) && cont < (circuito.getEntradaBoxIndex() + 50))
				&& safetyCar.isVaiProBox()) {
			controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("030")));
			safetyCar.setNaPista(false);
			safetyCar.setSaiuVolta(controleJogo.getNumVoltaAtual());
		}

		List pista = controleJogo.getNosDaPista();
		int index = safetyCar.getNoAtual().getIndex();
		No noAtual = safetyCar.getNoAtual();
		int bonus = 0;
		Piloto pole = (Piloto) controleJogo.getPilotosCopia().get(0);
		long ptsSc = safetyCar.getPtosPista();
		long polePts = pole.getPtosPista();
		if (ptsSc < (polePts+500)) {
			bonus = Math.random() < 0.3 ? 3 : 2;
			if (noAtual.verificaCurvaAlta()) {
				bonus = Math.random() < 0.9 ? 2 : 1;
			}
			if (noAtual.verificaCurvaBaixa()) {
				bonus = Math.random() < 0.7 ? 2 : 1;
			}
			bonus *= (controleJogo.getCircuito().getMultiplciador() * controleJogo.getIndexVelcidadeDaPista()) * .8;
			bonus = calculaMediaSC(bonus);
		}

		index += bonus;
		int diff = index - pista.size();
		/**
		 * Completou Volta
		 */
		if (diff >= 0) {
			index = diff;
		}
		safetyCar.setPtosPista(safetyCar.getPtosPista() + bonus);
		int desviar = desviarTracado(index);
		if (desviar == -1) {
			safetyCar.setTracado(0);
		} else if (desviar == 0) {
			safetyCar.setTracado(Util.intervalo(1, 2));
		} else {
			safetyCar.setTracado(0);
		}

		safetyCar.setNoAtual((No) pista.get(index));
	}
	
	public boolean verificaPoleFrenteSafety(Piloto piloto) {
		return piloto.getPtosPista() >= safetyCar.getPtosPista();
	}	

	public static void main(String[] args) {
		double multi = 0.1;
		int bonus = 1;
		bonus *= multi;
	}

	private int desviarTracado(int indice) {
		List pilotos = controleJogo.getPilotosCopia();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (safetyCar.equals(piloto) || piloto.getPosicao() == 1 || piloto.getTracado() != safetyCar.getTracado()
					|| piloto.getCarro().isRecolhido()) {
				continue;
			}

			int indiceCarro = piloto.getNoAtual().getIndex();

			int traz = indiceCarro - 300;
			int frente = indiceCarro + 100;

			List lista = piloto.obterPista(controleJogo);

			if (traz < 0) {
				traz = (lista.size() - 1) + traz;
			}
			if (frente > (lista.size() - 1)) {
				frente = (frente - (lista.size() - 1)) - 1;
			}

			if (indice >= traz && indice <= frente) {
				return piloto.getTracado();
			}
		}
		return -1;
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

	public Piloto getPilotoBateu() {
		return pilotoBateu;
	}
}
