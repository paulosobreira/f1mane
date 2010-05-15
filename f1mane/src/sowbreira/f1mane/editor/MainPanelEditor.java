package sowbreira.f1mane.editor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
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
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado Em 10:51:26
 */
public class MainPanelEditor extends JPanel {
	private static final long serialVersionUID = -7001602531075714400L;
	public List avatarList = new Vector();
	private Circuito circuito = new Circuito();
	private TestePista testePista;
	private TestePistaInflado testePistaInflado;
	private Color tipoNo = null;
	private No ultimoNo = null;
	private JList pistaJList;
	private JList boxJList;
	private JFrame srcFrame;
	private boolean desenhaTracado = true;
	private BufferedImage backGround;
	int ultimoItemBoxSelecionado = -1;
	int ultimoItemPistaSelecionado = -1;
	private JRadioButton pistasButton = new JRadioButton();
	private JRadioButton boxButton = new JRadioButton();
	private JScrollPane scrollPane;
	public double zoom = 1;
	private boolean inflado;
	private BufferedImage carroCima;
	private int mx;
	private int my;
	private int pos = 0;

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public MainPanelEditor(String backGroundStr, JFrame frame) {
		backGround = CarregadorRecursos.carregaBackGround(backGroundStr, this,
				circuito);
		this.srcFrame = frame;
		iniciaEditor(frame);
	}

	public MainPanelEditor(JFrame frame) throws IOException,
			ClassNotFoundException {
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

		backGround = CarregadorRecursos.carregaBackGround(circuito
				.getBackGround(), this, circuito);
		this.srcFrame = frame;
		iniciaEditor(frame);
		atualizaListas();
	}

	private void atualizaListas() {
		for (Iterator iter = circuito.getPista().iterator(); iter.hasNext();) {
			((DefaultListModel) pistaJList.getModel()).addElement(iter.next());
		}

		for (Iterator iter = circuito.getBox().iterator(); iter.hasNext();) {
			((DefaultListModel) boxJList.getModel()).addElement(iter.next());
		}
	}

	private void iniciaEditor(JFrame frame) {
		JPanel controlPanel = new JPanel();
		controlPanel.setLayout(new GridLayout(2, 1));

		pistaJList = new JList(new DefaultListModel());
		boxJList = new JList(new DefaultListModel());
		pistaJList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();

				if (keyCoode == KeyEvent.VK_DELETE) {
					if (pistaJList.getSelectedValue() == null) {
						return;
					}

					circuito.getPista().remove(pistaJList.getSelectedValue());
					((DefaultListModel) pistaJList.getModel())
							.remove(pistaJList.getSelectedIndex());
				}

				tipoNo = null;
			}
		});
		pistaJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				MainPanelEditor.this.repaint();
				if (pistaJList.getSelectedIndex() > -1)
					ultimoItemPistaSelecionado = pistaJList.getSelectedIndex();
			}
		});
		pistaJList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				ultimoItemPistaSelecionado = pistaJList.getSelectedIndex();
			}

		});
		boxJList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				MainPanelEditor.this.repaint();
				if (boxJList.getSelectedIndex() > -1)
					ultimoItemBoxSelecionado = boxJList.getSelectedIndex();
			}
		});

		boxJList.addMouseListener(new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {
				super.mouseClicked(e);
				ultimoItemBoxSelecionado = boxJList.getSelectedIndex();
			}

		});
		boxJList.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();

				if (keyCoode == KeyEvent.VK_DELETE) {
					if (boxJList.getSelectedValue() == null) {
						return;
					}

					circuito.getBox().remove(boxJList.getSelectedValue());
					((DefaultListModel) boxJList.getModel()).remove(boxJList
							.getSelectedIndex());
				}

				tipoNo = null;
			}
		});
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
		JPanel pistas = new JPanel();
		pistas.setLayout(new BorderLayout());
		pistas.add(radioPistaPanel, BorderLayout.NORTH);
		pistas.add(new JScrollPane(pistaJList), BorderLayout.CENTER);
		controlPanel.add(pistas);

		JPanel radioBoxPanel = new JPanel();
		radioBoxPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("033");
			}
		});
		radioBoxPanel.add(boxButton);
		JPanel boxes = new JPanel();
		boxes.setLayout(new BorderLayout());
		boxes.add(radioBoxPanel, BorderLayout.NORTH);
		boxes.add(new JScrollPane(boxJList), BorderLayout.CENTER);
		controlPanel.add(boxes);

		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 4));

		JButton testaPistaButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("034");
			}
		};
		testaPistaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					testePista.iniciarTeste();
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
				testePista.testarBox();
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
				testePista.regMax();
			}
		});
		buttonsPanel.add(regMax);

		JButton desenhaTracadoBot = new JButton("Desenha Tracado") {
			@Override
			public String getText() {
				return Lang.msg("037");
			}
		};
		desenhaTracadoBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				desenhaTracado = !desenhaTracado;
				MainPanelEditor.this.repaint();
			}
		});
		buttonsPanel.add(desenhaTracadoBot);

		JButton inflarPistaBot = new JButton("inflarPista") {
			@Override
			public String getText() {
				return Lang.msg("inflarPista");
			}
		};
		inflarPistaBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				inflarPista();
			}
		});
		buttonsPanel.add(inflarPistaBot);

		gerarLayout(frame, controlPanel, buttonsPanel);
		testePista = new TestePista(this, circuito);
		adicionaEventosMouse(frame);
	}

	private void gerarLayout(JFrame frame, JPanel controlPanel,
			JPanel buttonsPanel) {
		frame.getContentPane().removeAll();
		frame.setLayout(new BorderLayout());
		this.setPreferredSize(new Dimension(backGround.getWidth(), backGround
				.getHeight()));
		frame.getContentPane().add(controlPanel, BorderLayout.CENTER);
		frame.getContentPane().add(this, BorderLayout.WEST);
		frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		frame.pack();
	}

	private void adicionaEventosMouse(final JFrame frame) {
		this.addMouseListener(new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				frame.requestFocus();
			}

			public void mouseClicked(MouseEvent e) {
				Logger.logar("Pontos Editor :" + e.getX() + " - " + e.getY());
				if ((tipoNo == null) || (e.getButton() == 3)) {
					srcFrame.requestFocus();

					return;
				}

				int[] cor = new int[4];
				cor = backGround.getData().getPixel(e.getX(), e.getY(), cor);
				Logger.logar(new Color(cor[0], cor[1], cor[2], cor[3]));

				No no = new No();
				no.setTipo(tipoNo);
				no.setPoint(e.getPoint());
				inserirNoNasJList(no);

				ultimoNo = no;

				repaint();
			}
		});
	}

	private void inserirNoNasJList(No no) {
		if (boxButton.isSelected()) {

			DefaultListModel model = ((DefaultListModel) boxJList.getModel());

			if (ultimoItemBoxSelecionado > -1) {
				ultimoItemBoxSelecionado += 1;

				if (circuito.getBox().size() > ultimoItemBoxSelecionado) {
					circuito.getBox().add(ultimoItemBoxSelecionado, no);
					model.add(ultimoItemBoxSelecionado, no);
				} else {
					circuito.getBox().add(no);
					model.addElement(no);
					ultimoItemBoxSelecionado = model.size() - 1;
				}

				boxJList.setSelectedIndex(ultimoItemBoxSelecionado);
			} else {
				circuito.getBox().add(no);
				model.addElement(no);
				ultimoItemBoxSelecionado = model.size() - 1;
				boxJList.setSelectedIndex(ultimoItemBoxSelecionado);
			}
		} else {
			if (no.isBox()) {
				JOptionPane.showMessageDialog(this, Lang.msg("038"), Lang
						.msg("039"), JOptionPane.INFORMATION_MESSAGE);
				return;
			}

			DefaultListModel model = ((DefaultListModel) pistaJList.getModel());

			if (ultimoItemPistaSelecionado > -1) {
				ultimoItemPistaSelecionado += 1;

				if (circuito.getPista().size() > ultimoItemPistaSelecionado) {
					circuito.getPista().add(ultimoItemPistaSelecionado, no);
					model.add(ultimoItemPistaSelecionado, no);
				} else {
					circuito.getPista().add(no);
					model.addElement(no);
					ultimoItemPistaSelecionado = model.size() - 1;
				}
				pistaJList.setSelectedIndex(ultimoItemPistaSelecionado);
			} else {
				circuito.getPista().add(no);
				model.addElement(no);
				ultimoItemPistaSelecionado = model.size() - 1;
				pistaJList.setSelectedIndex(ultimoItemPistaSelecionado);

			}

		}
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
		if (inflado) {
			desenhaPainelInflado(g2d);
		} else {
			desenhaPainelClassico(g2d);
		}

	}

	private void desenhaPainelInflado(Graphics2D g2d) {
		BufferedImage carImg = carroCima;
		if (carImg == null)
			return;
		No oldNo = null;
		BasicStroke pista = new BasicStroke(Util.inte(carImg.getWidth() * 1.3
				* zoom), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke trilho = new BasicStroke(1);

		for (Iterator iter = circuito.getPistaInfladaKey().iterator(); iter
				.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.setColor(Color.LIGHT_GRAY);
				g2d.setStroke(pista);
				g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
						.getY()
						* zoom), Util.inte(no.getX() * zoom), Util.inte(no
						.getY()
						* zoom));

				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaInfladaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo.getY()
				* zoom), Util.inte(noFinal.getX() * zoom), Util.inte(noFinal
				.getY()
				* zoom));

		// for (Iterator iter = circuito.getPistaInfladaKey().iterator(); iter
		// .hasNext();) {
		// No no = (No) iter.next();
		//
		// // Point pin = (Point) circuito.getNosInKeys().get(no);
		// // g2d.setColor(no.getTipo());
		// // g2d.fillOval(Util.inte(pin.x * zoom), Util.inte(pin.y * zoom),
		// // Util
		// // .inte(15 * zoom), Util.inte(15 * zoom));
		// // Point pout = (Point) circuito.getNosOutKeys().get(no);
		// // g2d.fillOval(Util.inte(pout.x * zoom), Util.inte(pout.y * zoom),
		// // Util.inte(15 * zoom), Util.inte(15 * zoom));
		// // g2d.drawLine(Util.inte(pin.x * zoom), Util.inte(pin.y * zoom),
		// // Util
		// // .inte(pout.x * zoom), Util.inte(pout.y * zoom));
		//
		// if (oldNo == null) {
		// oldNo = no;
		// } else {
		// g2d.setColor(Color.black);
		// g2d.setStroke(trilho);
		// g2d.drawLine(Util.inte(oldNo.getX() * zoom), Util.inte(oldNo
		// .getY()
		// * zoom), Util.inte(no.getX() * zoom), Util.inte(no
		// .getY()
		// * zoom));
		// Point pin1 = (Point) circuito.getNosInKeys().get(oldNo);
		// Point pin2 = (Point) circuito.getNosInKeys().get(no);
		// g2d.setColor(Color.BLUE);
		// g2d.drawLine(Util.inte(pin1.x * zoom),
		// Util.inte(pin1.y * zoom), Util.inte(pin2.x * zoom),
		// Util.inte(pin2.y * zoom));
		// Point pout1 = (Point) circuito.getNosOutKeys().get(oldNo);
		// Point pout2 = (Point) circuito.getNosOutKeys().get(no);
		// g2d.setColor(Color.RED);
		// g2d.drawLine(Util.inte(pout1.x * zoom), Util.inte(pout1.y
		// * zoom), Util.inte(pout2.x * zoom), Util.inte(pout2.y
		// * zoom));
		//
		// oldNo = no;
		// }
		// }
		g2d.setColor(Color.black);
		g2d.setStroke(trilho);
		if (testePistaInflado != null && testePistaInflado.getTestCar() != null) {

			int width = (int) (carImg.getWidth());
			int height = (int) (carImg.getHeight());
			int w2 = width / 2;
			int h2 = height / 2;
			int carx = testePistaInflado.getTestCar().x - w2;
			int cary = testePistaInflado.getTestCar().y - h2;

			AffineTransform afZoom = new AffineTransform();
			AffineTransform afRotate = new AffineTransform();
			double calculaAngulo = GeoUtil.calculaAngulo(
					testePistaInflado.frenteCar, testePistaInflado.trazCar, 0);
			Rectangle2D rectangle = new Rectangle2D.Double((testePistaInflado
					.getTestCar().x - 44),
					(testePistaInflado.getTestCar().y - 17), Util.inte(88),
					Util.inte(34));
			Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inte(50),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			g2d.setColor(Color.black);
			g2d
					.drawString("" + calculaAngulo, Util.inte(p1.x), Util
							.inte(p1.y));

			Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util.inte(50),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));

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
			afRotate.setToRotation(rad, carImg.getWidth() / 2, carImg
					.getHeight() / 2);

			BufferedImage rotateBuffer = new BufferedImage(width, width,
					BufferedImage.TYPE_INT_ARGB);
			BufferedImage zoomBuffer = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_ARGB);
			AffineTransformOp op = new AffineTransformOp(afRotate,
					AffineTransformOp.TYPE_BILINEAR);
			op.filter(carImg, zoomBuffer);
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
			//g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			g2d.fillOval(Util.inte(testePistaInflado.frenteCar.x * zoom), Util
					.inte(testePistaInflado.frenteCar.y * zoom), Util
					.inte(5 * zoom), Util.inte(5 * zoom));
			g2d.fillOval(Util.inte(testePistaInflado.trazCar.x * zoom), Util
					.inte(testePistaInflado.trazCar.y * zoom), Util
					.inte(5 * zoom), Util.inte(5 * zoom));
			// calculaAngulo =
			// GeoUtil.calculaAngulo(testePistaInflado.frenteCar,
			// testePistaInflado.trazCar, 270);
			// if (calculaAngulo < 0) {
			// calculaAngulo = Math.abs(calculaAngulo);
			// }
			// calculaAngulo = 180 - calculaAngulo;

		}
	}

	private void desenhaPainelClassico(Graphics g2d) {
		g2d.drawImage(backGround, 0, 0, null);
		int noAlta = 0;
		int noMedia = 0;
		int noBaixa = 0;
		List list = circuito.geraPontosPista();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			if (no.verificaRetaOuLargada()) {
				noAlta++;
			}
			if (no.verificaCruvaAlta()) {
				noMedia++;
			}
			if (no.verificaCruvaBaixa()) {
				noBaixa++;
			}
		}
		double total = noAlta + noMedia + noBaixa;
		g2d.drawString(Lang.msg("ALTA") + ":" + noAlta + " "
				+ (int) (100 * noAlta / total) + "%", 5, 15);
		g2d.drawString(Lang.msg("MEDIA") + ":" + noMedia + " "
				+ (int) (100 * noMedia / total) + "%", 5, 35);
		g2d.drawString(Lang.msg("BAIXA") + ":" + noBaixa + " "
				+ (int) (100 * noBaixa / total) + "%", 5, 55);
		if (desenhaTracado) {
			No oldNo = null;

			for (Iterator iter = circuito.getPista().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				g2d.drawImage(no.getBufferedImage(), no.getDrawX(), no
						.getDrawY(), null);

				if (oldNo == null) {
					oldNo = no;
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(), no
							.getY());
					oldNo = no;
				}

				if (pistaJList.getSelectedValue() == no) {
					g2d.setColor(Color.WHITE);
					g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6,
							6, 2, 2);
					g2d.setColor(Color.black);
				}
			}

			oldNo = null;

			for (Iterator iter = circuito.getBox().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				g2d.setColor(no.getTipo());
				g2d.fillRoundRect(no.getDrawX(), no.getDrawY(), 10, 10, 15, 15);
				g2d.setColor(Color.BLACK);

				if (oldNo == null) {
					oldNo = no;
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(), no
							.getY());
					oldNo = no;
				}

				if (boxJList.getSelectedValue() == no) {
					g2d.setColor(Color.WHITE);
					g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6,
							6, 2, 2);
					g2d.setColor(Color.black);
				}
			}
		}

		if ((testePista != null) && (testePista.getTestCar() != null)) {
			if (Math.random() < .5)
				g2d.setColor(Color.DARK_GRAY);
			else
				g2d.setColor(Color.LIGHT_GRAY);
			g2d.fillOval(testePista.getTestCar().x - 2,
					testePista.getTestCar().y - 2, 8, 8);
		}
	}

	public void apagarUltimoNo() {
		circuito.getBox().remove(ultimoNo);
		circuito.getPista().remove(ultimoNo);
		((DefaultListModel) boxJList.getModel()).removeElement(ultimoNo);
		((DefaultListModel) pistaJList.getModel()).removeElement(ultimoNo);
		repaint();
	}

	public Shape limitesViewPort() {
		Rectangle rectangle = scrollPane.getViewport().getBounds();
		rectangle.width += 50;
		rectangle.height += 50;
		return rectangle;
	}

	public void inserirNoLargada() {
		tipoNo = No.LARGADA;
	}

	public void inserirNoReta() {
		tipoNo = No.RETA;
	}

	public void inserirNoCurvaAlta() {
		tipoNo = No.CURVA_ALTA;
	}

	public void inserirNoCurvaBaixa() {
		tipoNo = No.CURVA_BAIXA;
	}

	public void inserirNoBox() {
		tipoNo = No.BOX;
	}

	public void inserirNoParadaBox() {
		tipoNo = No.PARADA_BOX;
	}

	public void apagarUltimoNoPista() {
		if (circuito.getPista().size() == 0) {
			return;
		}

		((DefaultListModel) pistaJList.getModel()).removeElement(circuito
				.getPista().remove(circuito.getPista().size() - 1));
	}

	public void apagarUltimoNoBox() {
		if (circuito.getBox().size() == 0) {
			return;
		}

		((DefaultListModel) boxJList.getModel()).removeElement(circuito
				.getBox().remove(circuito.getBox().size() - 1));
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

	// public Dimension getPreferredSize() {
	// return new Dimension(backGround.getWidth(), backGround.getHeight());
	// }

	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return super.getPreferredSize();
	}

	public void inflarPista() {
		carroCima = CarregadorRecursos.carregaImg("carrocimatrans.png");
		carroCima = ImageUtil.geraTransparencia(carroCima, Color.BLACK);
		MainPanelEditor.this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				zoom += e.getWheelRotation() / 100.0;
				if (zoom > 1) {
					zoom = 1;
				}
				System.out.println(zoom);
				MainPanelEditor.this.repaint();
			}
		});
		srcFrame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keycode = e.getKeyCode();
				Point p = new Point((int) (scrollPane.getViewport()
						.getViewPosition().x), (int) (scrollPane.getViewport()
						.getViewPosition().y));

				if (keycode == KeyEvent.VK_LEFT) {
					pos = 2;
				} else if (keycode == KeyEvent.VK_RIGHT) {
					pos = 1;
				} else if (keycode == KeyEvent.VK_UP) {
					pos = 0;
				} else if (keycode == KeyEvent.VK_DOWN) {
					pos = 0;
				}
				if (p.x < 0 || p.y < 0) {
					return;
				}
				scrollPane.getViewport().setViewPosition(p);
				MainPanelEditor.this.repaint();
				super.keyPressed(e);
			}

		});
		double infla = 15;
		circuito.geraPontosPistaInflada(infla);
		inflado = true;
		List l = circuito.getPistaInflada();

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
		this.setPreferredSize(new Dimension(Util.inte(mx + 100 * zoom * 2),
				Util.inte(my + 100 * zoom * 2)));
		srcFrame.getContentPane().removeAll();
		srcFrame.setPreferredSize(new Dimension(1024, 768));
		scrollPane = new JScrollPane(this,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		srcFrame.getContentPane().add(scrollPane, BorderLayout.WEST);
		this.testePistaInflado = new TestePistaInflado(this, circuito);
		srcFrame.pack();
		try {
			testePistaInflado.iniciarTeste(infla);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
