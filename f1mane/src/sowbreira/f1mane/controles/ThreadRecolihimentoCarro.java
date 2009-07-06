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
		delayRecolhimento = 180000 + ((int) (Math.random() * 150000));
	}

	public void run() {
		try {
			if (controleJogo.VALENDO) {
				sleep(delayRecolhimento);
			}

		} catch (Exception e) {
			Logger.logarExept(e);
		}
		piloto.getCarro().setRecolhido(true);
		safetyCar.setVaiProBox(true);
		controleJogo.infoPrioritaria(Html.orange(Lang.msg("031",
				new String[] { piloto.getNome() })));

	}
}
