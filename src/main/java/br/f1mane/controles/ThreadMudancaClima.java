package br.f1mane.controles;

import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.entidades.Clima;

/**
 * @author Paulo Sobreira
 */
public class ThreadMudancaClima extends Thread {
	private final ControleClima controleClima;
	private boolean processada;

	public boolean isProcessada() {
		return processada;
	}

	public void setProcessada(boolean processada) {
		this.processada = processada;
	}

	/**
	 * @param controleClima
	 */
	public ThreadMudancaClima(ControleClima controleClima) {
		super();
		this.controleClima = controleClima;
	}

	public void run() {
		try {
			sleep(Util.intervalo(3000, 15000));
			if (Clima.SOL.equals(controleClima.getClima())
					|| Clima.CHUVA.equals(controleClima.getClima())) {
				controleClima.intervaloNublado();
			} else if (Clima.NUBLADO.equals(controleClima.getClima())) {
				if (controleClima.verificaPossibilidadeChoverNaPista()) {
					controleClima.intervaloChuva();
				} else {
					controleClima.intervaloSol();
				}
			}
			controleClima.informaMudancaClima();
			processada = true;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

}
