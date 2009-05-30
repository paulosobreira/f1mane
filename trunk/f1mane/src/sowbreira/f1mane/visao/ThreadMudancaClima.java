package sowbreira.f1mane.visao;

import java.awt.Point;
import java.awt.Toolkit;

public class ThreadMudancaClima extends Thread {
	private PainelCircuito painelCircuito;

	/**
	 * @param painelCircuito
	 */
	public ThreadMudancaClima(PainelCircuito painelCircuito) {
		super();
		this.painelCircuito = painelCircuito;
	}

	public void run() {
		Toolkit toolkit = painelCircuito.getToolkit();
		Point point = painelCircuito.getPointDesenhaClima();
		for (int i = 0; i < 200; i++) {
			try {
				Point p;
				if (i % 2 == 0) {
					p = new Point(point.x - 1, point.y - 1);
				} else {
					p = new Point(point.x + 1, point.y + 1);
				}
				painelCircuito.setPointDesenhaClima(p);
				if (i % 20 == 0) {
					toolkit.beep();
				}
				sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		painelCircuito.setPointDesenhaClima(point);
	}
}
