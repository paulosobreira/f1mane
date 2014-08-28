package sowbreira.f1mane.controles;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira
 */
public class ThreadRecolihimentoCarro extends Thread {
	private ControleJogoLocal controleJogo;
	private Piloto piloto;
	private SafetyCar safetyCar;
	private int delayRecolhimento;

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
		delayRecolhimento = controleJogo.getQtdeTotalVoltas() * 7000
				+ ((int) (Math.random() * 70000));
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
