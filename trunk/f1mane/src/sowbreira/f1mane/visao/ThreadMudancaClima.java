package sowbreira.f1mane.visao;

import java.awt.Point;
import java.awt.Toolkit;

import sowbreira.f1mane.controles.InterfaceJogo;
import br.nnpe.Logger;

public class ThreadMudancaClima extends Thread {
	private InterfaceJogo controleJogo;

	/**
	 * @param controleJogo
	 */
	public ThreadMudancaClima(InterfaceJogo controleJogo) {
		super();
		this.controleJogo = controleJogo;
	}

	public void run() {
		Toolkit toolkit = controleJogo.getMainFrame().getToolkit();
		// Point point = controleJogo.getPointDesenhaClima();
		for (int i = 0; i < 200; i++) {
			try {
				Point p;
				if (i % 2 == 0) {
					// p = new Point(point.x - 1, point.y - 1);
				} else {
					// p = new Point(point.x + 1, point.y + 1);
				}
				// controleJogo.setPointDesenhaClima(p);
				if (i % 20 == 0 && InterfaceJogo.VALENDO) {
					toolkit.beep();
				}
				sleep(100);
			} catch (InterruptedException e) {
				Logger.logarExept(e);
				return;
			}
		}
		// controleJogo.setPointDesenhaClima(point);
	}
}
