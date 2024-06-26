package br.f1mane.controles;

import br.nnpe.Html;
import br.nnpe.Logger;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.SafetyCar;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira
 */
public class ThreadRecolihimentoCarro extends Thread {
	private final ControleJogoLocal controleJogo;
	private final Piloto piloto;
	private final SafetyCar safetyCar;
	private final int delayRecolhimento;

	/**
	 * @param controleJogo
	 * @param piloto
	 * @param safetyCar
	 */
	public ThreadRecolihimentoCarro(ControleJogoLocal controleJogo,
			Piloto piloto, SafetyCar safetyCar) {
		super();
		this.controleJogo = controleJogo;
		this.piloto = piloto;
		this.safetyCar = safetyCar;
		delayRecolhimento = controleJogo.totalVoltasCorrida() * 7000
				+ ((int) (Math.random() * 50000));
	}

	public void run() {
		try {
			sleep(delayRecolhimento);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		int index = safetyCar.getNoAtual().getIndex();
		int size = controleJogo.getNosDaPista().size();
		double div = ((double) index) / ((double) size);
		while (div < 0.3 || div > 0.7) {
			try {
				sleep(500);
			} catch (InterruptedException e) {
			}
			index = safetyCar.getNoAtual().getIndex();
			size = controleJogo.getNosDaPista().size();
			div = ((double) index) / ((double) size);
		}

		piloto.getCarro().setRecolhido(true);
		safetyCar.setVaiProBox(true);
		controleJogo.infoPrioritaria(Html.saftyCar(Lang.msg("031",
				new String[] { piloto.getNome() })));

	}
}
