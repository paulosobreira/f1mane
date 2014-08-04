package sowbreira.f1mane.editor;

import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import sowbreira.f1mane.controles.ControleBox;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

public class TestePista {
	protected static final long SEEP_TIME = 100;
	private Circuito circuito;
	private Point testCar;
	public Point trazCar;
	public Point frenteCar;
	private boolean alive;
	private boolean irProBox;
	private List pontosPista;
	private List pontosBox;
	private MainPanelEditor editor;
	private Thread testTh;

	public boolean isAlive() {
		return alive;
	}

	public TestePista(MainPanelEditor editor, Circuito circuito) {
		this.circuito = circuito;
		this.editor = editor;
	}

	public Point getTestCar() {
		return testCar;
	}

	public void setTestCar(Point testCar) {
		this.testCar = testCar;
	}

	public void iniciarTeste(final double multi) throws Exception {
		pontosPista = circuito.getPistaFull();
		pontosBox = circuito.getBoxFull();

		if (testTh != null) {
			alive = false;
			testTh.interrupt();
			testTh = null;
			return;
		}

		testTh = new Thread(new Runnable() {
			public void run() {
				while (alive) {
					int cont = 0;
					No no = null;
					try {
						while (cont < pontosPista.size() && alive) {
							editor.setUltimoClicado(null);
							no = (No) pontosPista.get(cont);
							int entradaBox = circuito.getEntradaBoxIndex();
							if (irProBox
									&& (cont > (entradaBox - 100) && cont < (entradaBox + 100))) {
								int contBox = 0;
								while (contBox < pontosBox.size() && alive) {
									No noBox = (No) pontosBox.get(contBox);
									posicionaCarroBox(contBox, noBox, pontosBox);
									centralizaTestCar();
									if (No.RETA.equals(noBox.getTipo())
											|| No.LARGADA.equals(noBox
													.getTipo())) {
										contBox += ((Math.random() > .7 ? 3 : 4) * multi);

									} else if (No.CURVA_ALTA.equals(noBox
											.getTipo())) {
										contBox += ((Math.random() > .7 ? 2 : 3) * multi);

									} else {
										contBox += ((Math.random() > .7 ? 1 : 2) * multi);
									}

									Thread.sleep(SEEP_TIME);
								}

								cont = circuito.getSaidaBoxIndex();
								no = (No) pontosPista.get(cont);
								irProBox = false;

							}

							posicionaCarro(cont, no, pontosPista);
							centralizaTestCar();
							if (No.RETA.equals(no.getTipo())
									|| No.LARGADA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 3 : 4) * multi);

							} else if (No.CURVA_ALTA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 2 : 3) * multi);
							} else if (No.CURVA_BAIXA.equals(no.getTipo())) {
								cont += ((Math.random() > .7 ? 1 : 2) * multi);
							}

							Thread.sleep(SEEP_TIME);
						}
					} catch (Exception e) {
						Logger.logarExept(e);
					}
				}
			}
		});
		alive = true;
		testTh.start();
	}

	protected void posicionaCarro(int cont, No no, List lista) {
		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = (lista.size() - 1) + traz;
		}
		if (frente > lista.size()) {
			frente = frente - (lista.size() - 1);
		}

		trazCar = ((No) lista.get(traz)).getPoint();
		frenteCar = ((No) lista.get(frente)).getPoint();

		testCar = no.getPoint();

	}

	protected void posicionaCarroBox(int cont, No no, List lista) {
		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = 0;
		}
		if (frente > lista.size()) {
			frente = (lista.size() - 1);
		}

		trazCar = ((No) lista.get(traz)).getPoint();
		frenteCar = ((No) lista.get(frente)).getPoint();

		testCar = no.getPoint();

	}

	protected void finalize() throws Throwable {
		alive = false;
		super.finalize();
	}

	public void testarBox() {
		irProBox = !irProBox;
	}

	public boolean isIrProBox() {
		return irProBox;
	}

	protected void centralizaTestCar() {
		editor.centralizarPonto(testCar);
	}

	public void pararTeste() {
		if (testTh != null) {
			alive = false;
			testTh.interrupt();
			testTh = null;
		}
	}
}
