package br.f1mane.controles;

import br.nnpe.Global;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.f1mane.entidades.Clima;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 16/06/2007 as 20:14:25
 */
public class ControleClima {
	private final InterfaceJogo controleJogo;
	private String clima;
	private int voltaMudancaClima;
	private int intervaloMudancaClima;
	private final int numVoltas;
	private final int metadeVoltas;
	private final int quartoVoltas;
	private ThreadMudancaClima threadMudancaClima;
	private boolean climaAleatorio;

	public ControleClima(InterfaceJogo controleJogo, int totalVoltas) {
		super();
		this.controleJogo = controleJogo;
		numVoltas = totalVoltas;
		metadeVoltas = totalVoltas / 2;
		quartoVoltas = totalVoltas / 4;
	}

	public String getClima() {
		return clima;
	}

	public InterfaceJogo getControleJogo() {
		return controleJogo;
	}

	public int getIntervaloMudancaClima() {
		return intervaloMudancaClima;
	}

	public void setIntervaloMudancaClima(int intervaloMudancaClima) {
		this.intervaloMudancaClima = intervaloMudancaClima;
	}

	public void setClima(String clima) {
		this.clima = clima;
	}

	public boolean isClimaAleatorio() {
		return climaAleatorio;
	}

	public void gerarClimaInicial(Clima climaSel) {
		if (Global.DEBUG_SEM_CHUVA) {
			clima = Clima.SOL;
			return;
		}

		if (!Clima.ALEATORIO.equals(climaSel.getClima())) {
			clima = climaSel.getClima();
			return;
		}
		climaAleatorio = true;
		int val = 1 + (int) (Math.random() * 3);

		switch (val) {
			case 1:
				clima = Clima.SOL;

				break;

			case 2:
				clima = Clima.NUBLADO;

				break;

			case 3:
				clima = Clima.CHUVA;

				break;

			default:
				break;
		}
	}

	public void processaPossivelMudancaClima() {
		if (Global.DEBUG_SEM_CHUVA) {
			clima = Clima.SOL;
			return;
		}
		if ((voltaMudancaClima + intervaloMudancaClima) > controleJogo
				.getNumVoltaAtual()) {
			return;
		}
		voltaMudancaClima = controleJogo.getNumVoltaAtual();
		if (intervaloMudancaClima == 0) {
			intervaloMudancaClima = quartoVoltas
					+ ((int) (Math.random() * metadeVoltas));
			return;
		}
		if (threadMudancaClima != null && !threadMudancaClima.isProcessada())
			return;
		threadMudancaClima = new ThreadMudancaClima(this);
		threadMudancaClima.start();
	}

	public void informaMudancaClima() {
		if (Clima.SOL.equals(clima)) {
			controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("004")));
			Logger.logar("SOL");
		} else if (Clima.NUBLADO.equals(clima)) {
			controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("005")));
			Logger.logar("NUBLADO");
		} else if (Clima.CHUVA.equals(clima)) {
			controleJogo.infoPrioritaria(Html.msgClima(Lang.msg("006")));
			Logger.logar("CHUVA");
		}
	}

	public void intervaloNublado() {
		setClima(Clima.NUBLADO);
		intervaloMudancaClima = (quartoVoltas / 2)
				+ ((int) (Math.random() * quartoVoltas));
		if (intervaloMudancaClima > 0 && (controleJogo
				.totalVoltasCorrida() > (controleJogo.getNumVoltaAtual()
				+ intervaloMudancaClima))) {
			controleJogo.infoPrioritaria(Html.msgClima(Html.msgClima(
					Lang.msg("007", new Object[]{Integer.valueOf(intervaloMudancaClima)}))));
		}
	}

	public void intervaloSol() {
		setClima(Clima.SOL);
		intervaloMudancaClima = quartoVoltas
				+ ((int) (Math.random() * numVoltas));
	}

	public void intervaloChuva() {
		setClima(Clima.CHUVA);
		intervaloMudancaClima = quartoVoltas
				+ ((int) (Math.random() * metadeVoltas));
		if (Math.random() > 0.5) {
			intervaloMudancaClima = intervaloMudancaClima / 2;
		}
	}

	public void matarThreads() {
		if (threadMudancaClima != null) {
			threadMudancaClima.interrupt();
		}

	}

	public boolean verificaPossibilidadeChoverNaPista() {
		int porc = controleJogo.porcentagemChuvaCircuito();
		double val = (porc / 100.0);
		return Math.random() < (val);
	}

	public void climaLimpo() {
		if (Clima.NUBLADO.equals(getClima())) {
			setClima(Clima.SOL);
		}
		if (Clima.CHUVA.equals(getClima())) {
			setClima(Clima.NUBLADO);
		}

	}

	public void climaChuvoso() {
		if (Clima.NUBLADO.equals(getClima())) {
			setClima(Clima.CHUVA);
		}
		if (Clima.SOL.equals(getClima())) {
			setClima(Clima.NUBLADO);
		}
	}

}
