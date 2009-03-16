package sowbreira.f1mane.controles;

import sowbreira.f1mane.entidades.Clima;
/**
 * @author Paulo Sobreira
 */
public class ThreadMudancaClima extends Thread {
	private ControleClima controleClima;

	/**
	 * @param controleClima
	 */
	public ThreadMudancaClima(ControleClima controleClima) {
		super();
		this.controleClima = controleClima;
	}

	public void run() {
		try {
			if (ControleJogoLocal.VALENDO) {
				sleep(5000 + ((int) (Math.random() * 25000)));
			}

			if (Clima.SOL.equals(controleClima.getClima())
					|| Clima.CHUVA.equals(controleClima.getClima())) {

				controleClima.intervaloNublado();

			} else if (Clima.NUBLADO.equals(controleClima.getClima())) {
				if (Math.random() > controleClima.getControleJogo()
						.getNiveljogo()) {
					controleClima.intervaloSol();
				} else {
					controleClima.intervaloChuva();
				}

			}
			controleClima.informaMudancaClima();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
