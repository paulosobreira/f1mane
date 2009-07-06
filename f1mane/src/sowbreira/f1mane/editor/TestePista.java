package sowbreira.f1mane.editor;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import sowbreira.f1mane.controles.ControleBox;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

public class TestePista {
	private Circuito circuito;
	private Point testCar;
	private boolean alive;
	private boolean irProBox;
	private boolean maxHP;
	private List pontosPista;
	private List pontosBox;
	private MainPanelEditor editor;
	private Thread testTh;

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

	public void iniciarTeste() throws Exception {
		pontosPista = circuito.geraPontosPista();
		pontosBox = circuito.geraPontosBox();

		ControleBox controleBox = new ControleBox();
		try {
			controleBox.calculaNosBox(pontosPista, pontosBox);
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(editor, Lang.msg("040"), Lang
					.msg("041"), JOptionPane.ERROR_MESSAGE);
		}

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
						while (cont < pontosPista.size()) {
							no = (No) pontosPista.get(cont);

							if (irProBox && no.isNoEntradaBox()) {
								for (Iterator iter = pontosBox.iterator(); iter
										.hasNext();) {
									No noBox = (No) iter.next();
									testCar = noBox.getPoint();
									Thread.sleep(70);
									editor.repaint();
								}

								for (int i = 0; i < pontosPista.size(); i++) {
									No noSaidaBox = (No) pontosPista.get(i);

									if (noSaidaBox.isNoSaidaBox()) {
										no = noSaidaBox;
										irProBox = false;
										cont = i;

										break;
									}
								}
							}

							testCar = no.getPoint();
							editor.repaint();

							if (No.RETA.equals(no.getTipo())
									|| No.LARGADA.equals(no.getTipo())) {
								if (maxHP) {
									cont += 3;
								} else {
									if ((cont % 2) == 0) {
										cont += 3;
									} else {
										cont += 2;
									}
								}
							} else if (No.CURVA_ALTA.equals(no.getTipo())) {
								if (maxHP) {
									cont += 2;
								} else {
									if ((cont % 2) == 0) {
										cont += 2;
									} else {
										cont += 1;
									}
								}
							} else if (No.CURVA_BAIXA.equals(no.getTipo())) {
								cont += 1;
							}

							Thread.sleep(70);
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

	protected void finalize() throws Throwable {
		alive = false;
		super.finalize();
	}

	public void testarBox() {
		irProBox = !irProBox;
	}

	public void regMax() {
		maxHP = !maxHP;
	}
}
