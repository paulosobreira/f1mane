package sowbreira.f1mane.editor;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import sowbreira.f1mane.controles.ControleBox;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

public class TestePistaInflado {
	private Circuito circuito;
	private Point testCar;
	public Point trazCar;
	public Point frenteCar;
	private boolean alive;
	private boolean irProBox;
	private boolean maxHP;
	private List pontosPista;
	private List pontosBox;
	private MainPanelEditor editor;
	private Thread testTh;

	public TestePistaInflado(MainPanelEditor editor, Circuito circuito) {
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
		pontosPista = circuito.getPistaInflada();
		pontosBox = circuito.geraPontosBox();

		// ControleBox controleBox = new ControleBox();
		// try {
		// controleBox.calculaNosBox(pontosPista, pontosBox);
		// } catch (Exception e) {
		// Logger.logarExept(e);
		// JOptionPane.showMessageDialog(editor, Lang.msg("040"), Lang
		// .msg("041"), JOptionPane.ERROR_MESSAGE);
		// }

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

							// if (irProBox && no.isNoEntradaBox()) {
							// for (Iterator iter = pontosBox.iterator(); iter
							// .hasNext();) {
							// No noBox = (No) iter.next();
							// testCar = noBox.getPoint();
							// Thread.sleep(70);
							// editor.repaint();
							// }
							//
							// for (int i = 0; i < pontosPista.size(); i++) {
							// No noSaidaBox = (No) pontosPista.get(i);
							//
							// if (noSaidaBox.isNoSaidaBox()) {
							// no = noSaidaBox;
							// irProBox = false;
							// cont = i;
							//
							// break;
							// }
							// }
							// }

							int traz = cont - 44;
							int frente = cont + 44;

							if (traz < 0) {
								traz = (pontosPista.size() - 1) + traz;
							}
							if (frente > pontosPista.size()) {
								frente = frente - (pontosPista.size() - 1);
							}

							trazCar = ((No) pontosPista.get(traz)).getPoint();
							frenteCar = ((No) pontosPista.get(frente))
									.getPoint();

							testCar = no.getPoint();

							// editor.repaint();
							// centralizaPonto(testCar);
							centralizaTestCar();
							if (No.RETA.equals(no.getTipo())
									|| No.LARGADA.equals(no.getTipo())) {
								if (maxHP) {
									cont += (3 * multi);
								} else {
									if ((cont % 2) == 0) {
										cont += (3 * multi);
									} else {
										cont += (2 * multi);
									}
								}
							} else if (No.CURVA_ALTA.equals(no.getTipo())) {
								if (maxHP) {
									cont += 2;
								} else {
									if ((cont % 2) == 0) {
										cont += (2 * multi);
									} else {
										cont += (1 * multi);
									}
								}
							} else if (No.CURVA_BAIXA.equals(no.getTipo())) {
								cont += (1 * multi);
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

	protected void centralizaTestCar() {
		JScrollPane scrollPane = editor.getScrollPane();
		Point p = new Point((int) (testCar.x * editor.zoom) - 512,
				(int) (testCar.y * editor.zoom) - 384);
		if (p.x < 0) {
			p.x = 1;
		}
		// double maxX = ((editor.getWidth() * editor.zoom) - scrollPane
		// .getViewport().getWidth());
		// if (p.x > maxX) {
		// p.x = Util.inte(maxX) - 1;
		// }
		if (p.y < 0) {
			p.y = 1;
		}
		// double maxY = ((editor.getHeight() * editor.zoom) - (scrollPane
		// .getViewport().getHeight()));
		// if (p.y > maxY) {
		// p.y = Util.inte(maxY) - 1;
		// }
		scrollPane.getViewport().setViewPosition(p);
		editor.repaint();
	}
}
