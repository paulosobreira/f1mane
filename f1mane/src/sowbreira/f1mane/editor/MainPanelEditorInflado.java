package sowbreira.f1mane.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class MainPanelEditorInflado extends JPanel {
	private static final long serialVersionUID = -7001602531075714400L;
	private Circuito circuito = new Circuito();
	private TestePistaInflado testePistaInflado;
	private JFrame srcFrame;
	private JRadioButton pistasButton = new JRadioButton();
	private JRadioButton boxButton = new JRadioButton();
	private JScrollPane scrollPane;
	public double zoom = 1;
	private BufferedImage carroCima;
	private int mx;
	private int my;
	private int pos = 0;
	protected double tamanhoPista = 20;
	private double larguraPista = 1.1;
	private JTextField larguraPistaText;
	private JTextField tamanhoPistaText;
	private BasicStroke trilho = new BasicStroke(1);
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private int larguraPistaPixeis;
	private BasicStroke zebra;

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public MainPanelEditorInflado(JFrame frame) throws IOException,
			ClassNotFoundException {
		this.srcFrame = frame;
		setBackground(new Color(255, 250, 240));
		srcFrame.getContentPane().removeAll();
		setSize(10000, 10000);
		scrollPane = new JScrollPane(this,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		JFileChooser fileChooser = new JFileChooser(CarregadorRecursos.class
				.getResource("CarregadorRecursos.class").getFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		ExampleFileFilter exampleFileFilter = new ExampleFileFilter("f1mane");
		fileChooser.setFileFilter(exampleFileFilter);

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.CANCEL_OPTION) {
			return;
		}

		FileInputStream inputStream = new FileInputStream(fileChooser
				.getSelectedFile());
		ObjectInputStream ois = new ObjectInputStream(inputStream);

		circuito = (Circuito) ois.readObject();

		carroCima = CarregadorRecursos.carregaImg("carrocimatrans.png");
		carroCima = ImageUtil.geraTransparencia(carroCima, Color.BLACK);
		MainPanelEditorInflado.this
				.addMouseWheelListener(new MouseWheelListener() {

					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						zoom += e.getWheelRotation() / 100.0;
						if (zoom > 1) {
							zoom = 1;
						}
						if (zoom < 0.1) {
							zoom = 0.1;
						}
						atualizaVarZoom();
						// MainPanelEditorInflado.this.repaint();
					}
				});

		srcFrame.setPreferredSize(new Dimension(800, 600));
		srcFrame.getContentPane().setLayout(new BorderLayout());
		srcFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		testePistaInflado = new TestePistaInflado(this, circuito);
		iniciaEditor(srcFrame);
		inflarPista();
		srcFrame.pack();
	}

	protected void atualizaVarZoom() {
		larguraPistaPixeis = Util.inte(carroCima.getWidth() * larguraPista
				* zoom);
		pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[] {
						10, 10 }, 0);
	}

	private void iniciaEditor(JFrame frame) {
		ButtonGroup buttonGroup = new ButtonGroup();

		buttonGroup.add(boxButton);
		buttonGroup.add(pistasButton);
		pistasButton.setSelected(true);
		JPanel radioPistaPanel = new JPanel();
		radioPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("032");
			}
		});
		radioPistaPanel.add(pistasButton);
		JPanel radioBoxPanel = new JPanel();
		radioBoxPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("033");
			}
		});
		radioBoxPanel.add(boxButton);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(2, 6));

		JButton testaPistaButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("034");
			}
		};
		testaPistaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					testePistaInflado.iniciarTeste(tamanhoPista);
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});
		buttonsPanel.add(testaPistaButton);

		JButton testaBoxButton = new JButton("Ligar/Desligar Box") {
			@Override
			public String getText() {
				return Lang.msg("035");
			}
		};
		testaBoxButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testePistaInflado.testarBox();
			}
		});
		buttonsPanel.add(testaBoxButton);

		JButton regMax = new JButton("Ligar/Desligar Agressivo") {
			@Override
			public String getText() {
				return Lang.msg("036");
			}
		};
		regMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testePistaInflado.regMax();
			}
		});
		buttonsPanel.add(regMax);
		JButton left = new JButton() {
			@Override
			public String getText() {
				return "<";
			}
		};
		left.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pos = 1;
				// Point p = new Point(
				// scrollPane.getViewport().getViewPosition().x,
				// scrollPane.getViewport().getViewPosition().y);
				// p.x -= 10;
				// scrollPane.getViewport().setViewPosition(p);
				// MainPanelEditorInflado.this.repaint();

			}
		});
		buttonsPanel.add(left);

		JButton center = new JButton() {
			@Override
			public String getText() {
				return "|";

			}
		};
		center.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pos = 0;
				// Point p = new Point(
				// scrollPane.getViewport().getViewPosition().x,
				// scrollPane.getViewport().getViewPosition().y);
				// p.y += 10;
				// scrollPane.getViewport().setViewPosition(p);
				// MainPanelEditorInflado.this.repaint();

			}
		});
		buttonsPanel.add(center);

		JButton right = new JButton() {
			@Override
			public String getText() {
				return ">";
			}
		};
		right.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pos = 2;
				// Point p = new Point(
				// scrollPane.getViewport().getViewPosition().x,
				// scrollPane.getViewport().getViewPosition().y);
				// p.x += 10;
				// scrollPane.getViewport().setViewPosition(p);
				// MainPanelEditorInflado.this.repaint();

			}
		});
		buttonsPanel.add(right);

		JButton inflarPistaBot = new JButton("inflarPista") {
			@Override
			public String getText() {
				return Lang.msg("inflarPista");
			}
		};
		inflarPistaBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					tamanhoPista = Double.parseDouble(tamanhoPistaText
							.getText());
					larguraPista = Double.parseDouble(larguraPistaText
							.getText());
					inflarPista();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(inflarPistaBot);

		buttonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("tamanhoPista");
			}
		});

		tamanhoPistaText = new JTextField();
		tamanhoPistaText.setText("" + tamanhoPista);
		buttonsPanel.add(tamanhoPistaText);

		buttonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("larguraPista");
			}
		});

		larguraPistaText = new JTextField();
		larguraPistaText.setText("" + larguraPista);
		buttonsPanel.add(larguraPistaText);

		frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

	}

	private void setarHints(Graphics2D g2d) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
				RenderingHints.VALUE_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_DITHERING,
				RenderingHints.VALUE_DITHER_ENABLE);
		g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BILINEAR);

	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		setarHints(g2d);

		if (carroCima == null)
			return;
		if (larguraPistaPixeis == 0)
			larguraPistaPixeis = Util.inte(carroCima.getWidth() * larguraPista
					* zoom);
		if (pista == null)
			pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
					BasicStroke.JOIN_ROUND);
		if (pistaTinta == null)
			pistaTinta = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		if (box == null)
			box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
					BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		if (zebra == null)
			zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
					BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
					new float[] { 10, 10 }, 0);
		desenhaTintaPistaEZebra(g2d);
		desenhaPista(g2d);
		desenhaPistaBox(g2d);
		desenhaCarroTeste(g2d);
		desenhaInfo(g2d);
		desenhaEntradaParadaSaidaBox(g2d);
		desenhaLargada(g2d);
		desenhaGrid(g2d);
		desenhaBoxes(g2d);

	}

	private void desenhaBoxes(Graphics2D g2d) {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 2 * i);
			No n1 = (No) circuito.getBoxFull().get(iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getBoxFull().get(iP);
			No n2 = (No) circuito.getBoxFull().get(iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util
					.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom), Util
					.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util
					.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Rectangle2D rectangleTraco = null;
			if (circuito.getOrientacaoBox() == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				rectangleTraco = new Rectangle2D.Double((cima.x),
						(cima.y - (Carro.MEIA_ALTURA * zoom)), (1 * zoom),
						(Carro.ALTURA * zoom));
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				rectangleTraco = new Rectangle2D.Double((baixo.x),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)), (1 * zoom),
						(Carro.ALTURA * zoom));
			}

			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(Color.BLACK);
			g2d.draw(generalPath.createTransformedShape(affineTransformRect));
			generalPath = new GeneralPath(rectangleTraco);
			affineTransformRect.setToRotation(rad, rectangleTraco.getCenterX(),
					rectangleTraco.getCenterY());
			g2d.setColor(Color.white);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

		}

	}

	private void desenhaLargada(Graphics2D g2d) {
		No n1 = (No) circuito.getPistaFull().get(0);
		No n2 = (No) circuito.getPistaFull().get(20);
		Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
				.getPoint().y
				* zoom));
		Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
				.getPoint().y
				* zoom));
		double larguraZebra = (larguraPistaPixeis * 0.01);
		RoundRectangle2D rectangle = new RoundRectangle2D.Double(
				(p1.x - (larguraZebra / 2)), (p1.y - (larguraPistaPixeis / 2)),
				larguraZebra, larguraPistaPixeis, 5 * zoom, 5 * zoom);
		double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
		double rad = Math.toRadians((double) calculaAngulo);
		GeneralPath generalPath = new GeneralPath(rectangle);
		AffineTransform affineTransformRect = AffineTransform.getScaleInstance(
				zoom, zoom);
		affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
				rectangle.getCenterY());
		g2d.setColor(Color.white);
		g2d.fill(generalPath.createTransformedShape(affineTransformRect));

	}

	private void desenhaGrid(Graphics2D g2d) {

		for (int i = 0; i < 24; i++) {
			int iP = 50 + Util.inte(Carro.LARGURA * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util
					.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom), Util
					.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util
					.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			if (i % 2 == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			}

			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(Color.white);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			iP += 5;
			n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
					.getPoint().y
					* zoom));
			pm = new Point(Util.inte(nM.getPoint().x * zoom), Util.inte(nM
					.getPoint().y
					* zoom));
			p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
					.getPoint().y
					* zoom));
			calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			cima = GeoUtil.calculaPonto(calculaAngulo, Util.inte(Carro.ALTURA
					* 1.2 * zoom), new Point(Util.inte(rectangle.getCenterX()),
					Util.inte(rectangle.getCenterY())));
			baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			if (i % 2 == 0) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
			}

			generalPath = new GeneralPath(rectangle);

			affineTransformRect = AffineTransform.getScaleInstance(zoom, zoom);
			rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(Color.lightGray);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

		}

	}

	private void desenhaEntradaParadaSaidaBox(Graphics2D g2d) {
		Point e = ((No) circuito.getPistaFull().get(
				circuito.getEntradaBoxIndex())).getPoint();
		Point p = ((No) circuito.getBoxFull().get(circuito.getParadaBoxIndex()))
				.getPoint();
		Point s = ((No) circuito.getPistaFull()
				.get(circuito.getSaidaBoxIndex())).getPoint();
		g2d.setColor(Color.BLACK);
		g2d.fillOval(Util.inte(e.x * zoom), Util.inte(e.y * zoom), Util
				.inte(5 * zoom), Util.inte(5 * zoom));
		g2d.fillOval(Util.inte(p.x * zoom), Util.inte(p.y * zoom), Util
				.inte(5 * zoom), Util.inte(5 * zoom));

		g2d.fillOval(Util.inte(s.x * zoom), Util.inte(s.y * zoom), Util
				.inte(5 * zoom), Util.inte(5 * zoom));

	}

	private void desenhaInfo(Graphics2D g2d) {
		g2d.setColor(Color.black);
		Rectangle limitesViewPort = (Rectangle) limitesViewPort();
		int x = limitesViewPort.getBounds().x + 30;
		int y = limitesViewPort.getBounds().y + 20;
		g2d.drawString("Zoom : " + zoom, x, y);
		y += 20;
		g2d.drawString("Tamanho : " + tamanhoPista, x, y);
		y += 20;
		g2d.drawString("ir box : " + testePistaInflado.isIrProBox(), x, y);
		y += 20;
		g2d.drawString("Pista : " + larguraPista, x, y);
		limitesViewPort.width -= 100;
		limitesViewPort.height -= 100;

		g2d.draw(limitesViewPort);
	}

	private void desenhaTintaPistaEZebra(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setStroke(pistaTinta);

		No oldNo = null;
		int cont = 0;
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.setColor(Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo())
						|| No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util
							.inte(oldNo.getY() * zoom), Util.inte(no.getX()
							* zoom), Util.inte(no.getY() * zoom));

				}
				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	private void desenhaCarroTeste(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(trilho);
		if (testePistaInflado != null && testePistaInflado.getTestCar() != null) {

			int width = (int) (carroCima.getWidth());
			int height = (int) (carroCima.getHeight());
			int w2 = width / 2;
			int h2 = height / 2;
			int carx = testePistaInflado.getTestCar().x - w2;
			int cary = testePistaInflado.getTestCar().y - h2;

			AffineTransform afZoom = new AffineTransform();
			AffineTransform afRotate = new AffineTransform();
			double calculaAngulo = GeoUtil.calculaAngulo(
					testePistaInflado.frenteCar, testePistaInflado.trazCar, 0);
			Rectangle2D rectangle = new Rectangle2D.Double((testePistaInflado
					.getTestCar().x - Carro.MEIA_LARGURA), (testePistaInflado
					.getTestCar().y - Carro.MEIA_ALTURA), Carro.LARGURA,
					Carro.ALTURA);
			Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));
			g2d.setColor(Color.black);
			Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));

			if (pos == 0) {
				carx = testePistaInflado.getTestCar().x - w2;
				cary = testePistaInflado.getTestCar().y - h2;
			}
			if (pos == 1) {
				carx = Util.inte((p1.x - w2));
				cary = Util.inte((p1.y - h2));
			}
			if (pos == 2) {
				carx = Util.inte((p2.x - w2));
				cary = Util.inte((p2.y - h2));
			}

			// g2d.drawString("" + (calculaAngulo + 180), Util.inte(p2.x), Util
			// .inte(p2.y));
			// g2d.drawLine(Util.inte(rectangle.getCenterX()),
			// Util.inte(rectangle
			// .getCenterY()), Util.inte(p1.x), Util.inte(p1.y));
			// g2d.drawLine(Util.inte(rectangle.getCenterX()),
			// Util.inte(rectangle
			// .getCenterY()), Util.inte(p2.x), Util.inte(p2.y));
			double rad = Math.toRadians((double) calculaAngulo);
			afZoom.setToScale(zoom, zoom);
			afRotate.setToRotation(rad, carroCima.getWidth() / 2, carroCima
					.getHeight() / 2);

			BufferedImage rotateBuffer = new BufferedImage(width, width,
					BufferedImage.TYPE_INT_ARGB);
			BufferedImage zoomBuffer = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			AffineTransformOp op = new AffineTransformOp(afRotate,
					AffineTransformOp.TYPE_BILINEAR);
			op.filter(carroCima, zoomBuffer);
			AffineTransformOp op2 = new AffineTransformOp(afZoom,
					AffineTransformOp.TYPE_BILINEAR);
			op2.filter(zoomBuffer, rotateBuffer);
			g2d.drawImage(rotateBuffer, Util.inte(carx * zoom), Util.inte(cary
					* zoom), null);

			GeneralPath generalPath = new GeneralPath(rectangle);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(new Color(255, 0, 0, 140));
			// g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			g2d.fillOval(Util.inte(testePistaInflado.frenteCar.x * zoom), Util
					.inte(testePistaInflado.frenteCar.y * zoom), Util
					.inte(5 * zoom), Util.inte(5 * zoom));
			g2d.fillOval(Util.inte(testePistaInflado.trazCar.x * zoom), Util
					.inte(testePistaInflado.trazCar.y * zoom), Util
					.inte(5 * zoom), Util.inte(5 * zoom));

		}

	}

	private void desenhaPistaBox(Graphics2D g2d) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(box);
		No oldNo = null;
		for (Iterator iter = circuito.getBoxKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));

				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getBoxKey().get(
				circuito.getBoxKey().size() - 1);

		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	private void desenhaPista(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(pista);
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));

				oldNo = no;
			}
		}

		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));
	}

	public Shape limitesViewPort() {
		Rectangle rectangle = scrollPane.getViewport().getBounds();
		// rectangle.width += 50;
		// rectangle.height += 50;
		rectangle.x = scrollPane.getViewport().getViewPosition().x;
		rectangle.y = scrollPane.getViewport().getViewPosition().y;
		return rectangle;
	}

	public void salvarPista() throws IOException {
		JFileChooser fileChooser = new JFileChooser(CarregadorRecursos.class
				.getResource("CarregadorRecursos.class").getFile());
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		ExampleFileFilter exampleFileFilter = new ExampleFileFilter("f1mane");
		fileChooser.setFileFilter(exampleFileFilter);

		int result = fileChooser.showOpenDialog(null);

		if (result == JFileChooser.CANCEL_OPTION) {
			return;
		}

		File file = fileChooser.getSelectedFile();
		file = new File(file.getCanonicalFile() + ".f1mane");

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		oos.writeObject(circuito);
		oos.flush();
		fileOutputStream.close();
	}

	public Dimension getPreferredSize() {
		return new Dimension(Util.inte((mx + 1000)), Util.inte((my + 1000)));
	}

	public void inflarPista() {
		testePistaInflado.pararTeste();
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		srcFrame.setCursor(cursor);
		circuito.geraPontosPistaInflada(this.tamanhoPista);
		cursor = Cursor.getDefaultCursor();
		srcFrame.setCursor(cursor);

		List l = circuito.getPistaFull();

		for (Iterator iterator = l.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point point = no.getPoint();
			if (point.x > mx) {
				mx = point.x;
			}
			if (point.y > my) {
				my = point.y;
			}

		}

		mx += 300;
		my += 300;
		srcFrame.pack();
		No n1 = (No) l.get(0);
		centralizarPonto(n1.getPoint());

	}

	public void centralizarPonto(Point pin) {
		final Point p = new Point((int) (pin.x * zoom)
				- (scrollPane.getViewport().getWidth() / 2),
				(int) (pin.y * zoom)
						- (scrollPane.getViewport().getHeight() / 2));
		if (p.x < 0) {
			p.x = 1;
		}
		double maxX = ((getWidth() * zoom) - scrollPane.getViewport()
				.getWidth());
		if (p.x > maxX) {
			p.x = Util.inte(maxX) - 1;
		}
		if (p.y < 0) {
			p.y = 1;
		}
		double maxY = ((getHeight() * zoom) - (scrollPane.getViewport()
				.getHeight()));
		if (p.y > maxY) {
			p.y = Util.inte(maxY) - 1;
		}

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				scrollPane.getViewport().setViewPosition(p);
				repaint();
			}
		});

	}

}
