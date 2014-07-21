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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.beans.Transient;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.ObjetoEscapada;
import sowbreira.f1mane.entidades.ObjetoLivre;
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.ObjetoTransparencia;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelCircuito;
import br.nnpe.GeoUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class MainPanelEditor extends JPanel {
	private static final long serialVersionUID = -7001602531075714400L;
	private Circuito circuito = new Circuito();
	private TestePista testePista;
	private Color tipoNo = null;
	private No ultimoNo = null;
	private JList pistaJList;
	private JList boxJList;
	private JFrame srcFrame;
	private boolean desenhaTracado = true;
	private boolean creditos = false;
	private boolean pontosEscape = false;
	public final static Color oran = new Color(255, 188, 40, 180);
	public final static Color ver = new Color(255, 10, 10, 150);

	private BufferedImage backGround;
	int ultimoItemBoxSelecionado = -1;
	int ultimoItemPistaSelecionado = -1;

	private JRadioButton largadaButton = new JRadioButton();
	private JRadioButton retaButton = new JRadioButton();
	private JRadioButton curvaAltaButton = new JRadioButton();
	private JRadioButton curvaBaixaButton = new JRadioButton();
	private JRadioButton boxButton = new JRadioButton();
	private JRadioButton boxRetaButton = new JRadioButton();
	private JRadioButton boxCurvaAltaButton = new JRadioButton();
	private JRadioButton paraBoxButton = new JRadioButton();

	private JScrollPane scrollPane;

	private static final String LADO_COMBO_1 = "BOX LADO 1";
	private static final String LADO_COMBO_2 = "BOX LADO 2";
	private static final Color COR_PISTA = new Color(192, 192, 192);
	public double zoom = 1;
	private BufferedImage carroCima;
	private int mx;
	private int my;
	private int pos = 0;
	protected double multiplicadorPista = 0;
	private double multiplicadorLarguraPista = 0;
	private JTextField larguraPistaText;
	private JTextField tamanhoPistaText;
	private BasicStroke trilho = new BasicStroke(1);
	private BasicStroke pista;
	private BasicStroke pistaTinta;
	private BasicStroke box;
	private int larguraPistaPixeis;
	private BasicStroke zebra;
	private JComboBox ladoBoxCombo;
	private ObjetoPista objetoPista;
	private boolean desenhandoObjetoLivre;
	private boolean posicionaObjetoPista;
	private boolean moverObjetoPista;
	private Point ultimoClicado;
	private JSpinner transparencia = new JSpinner();
	private FormularioListaObjetos formularioListaObjetos;
	private BufferedImage drawBuffer;
	private Thread threadBkgGen;
	private JCheckBox nosChave;
	private boolean mostraBG = false;
	protected boolean editarObjetos;

	public MainPanelEditor() {
	}

	public MainPanelEditor(String backGroundStr, JFrame frame) {
		backGround = CarregadorRecursos.carregaBackGround(backGroundStr, this,
				circuito);
		this.srcFrame = frame;
		iniciaEditor(frame);
		atualizaListas();
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
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

		FileInputStream inputStream = new FileInputStream(
				fileChooser.getSelectedFile());
		ObjectInputStream ois = new ObjectInputStream(inputStream);
		circuito = (Circuito) ois.readObject();
		testePista = new TestePista(this, circuito);
		backGround = CarregadorRecursos.carregaBackGround(
				circuito.getBackGround(), this, circuito);
		this.srcFrame = frame;
		iniciaEditor(frame);
		atualizaListas();
		vetorizarCircuito();
		frame.pack();
		frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		centralizarPonto(((No) circuito.getPistaFull().get(0)).getPoint());
	}

	private void vetorizarCircuito() {
		mx = 0;
		my = 0;
		pista = null;
		pistaTinta = null;
		box = null;
		zebra = null;
		larguraPistaPixeis = 0;
		testePista.pararTeste();
		if (ladoBoxCombo.getSelectedItem().equals(LADO_COMBO_1)) {
			circuito.setLadoBox(1);
		} else {
			circuito.setLadoBox(2);
		}
		if (multiplicadorPista == 0 && circuito != null) {
			multiplicadorPista = circuito.getMultiplciador();
		}
		if (multiplicadorPista == 0) {
			multiplicadorPista = 11;
		}
		if (multiplicadorLarguraPista == 0 && circuito != null) {
			multiplicadorLarguraPista = circuito.getMultiplicadorLarguraPista();
		}
		if (multiplicadorLarguraPista == 0) {
			multiplicadorLarguraPista = 1.1;
		}
		circuito.setUsaBkg(true);
		circuito.vetorizarPista(this.multiplicadorPista,
				this.multiplicadorLarguraPista);
		tamanhoPistaText.setText(String.valueOf(multiplicadorPista));
		larguraPistaText.setText(String.valueOf(multiplicadorLarguraPista));
		if (circuito.getCorFundo() != null)
			transparencia.setValue(circuito.getCorFundo().getAlpha());

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
		No n1 = (No) l.get(0);
		centralizarPonto(n1.getPoint());
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

		carroCima = CarregadorRecursos.carregaBufferedImage("CarroCima.png");

		carroCima.getGraphics().drawImage(
				CarregadorRecursos.carregaBufferedImage("CarroCimaC1.png"), 0,
				0, null);
		carroCima.getGraphics().drawImage(
				CarregadorRecursos.carregaBufferedImage("CarroCimaC2.png"), 0,
				0, null);
		carroCima.getGraphics().drawImage(
				CarregadorRecursos.carregaBufferedImage("CarroCimaC3.png"), 0,
				0, null);

		JPanel controlPanel = gerarListsNosPistaBox();

		JPanel buttonsPanel = gerarBotoesTracado();

		JPanel radiosPanel = new JPanel();
		radiosPanel.setLayout(new GridLayout(1, 9));

		ButtonGroup buttonGroup = new ButtonGroup();

		buttonGroup.add(largadaButton);
		buttonGroup.add(retaButton);
		buttonGroup.add(curvaAltaButton);
		buttonGroup.add(curvaBaixaButton);
		buttonGroup.add(boxButton);
		buttonGroup.add(boxRetaButton);
		buttonGroup.add(boxCurvaAltaButton);
		buttonGroup.add(paraBoxButton);

		JPanel bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("099");
			}
		});
		bottonsPanel.add(largadaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("100");
			}
		});
		bottonsPanel.add(retaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("101");
			}
		});
		bottonsPanel.add(curvaAltaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("102");
			}
		});
		bottonsPanel.add(curvaBaixaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("103");
			}
		});
		bottonsPanel.add(boxButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("boxReta");
			}
		});
		bottonsPanel.add(boxRetaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("boxCurvaAlta");
			}
		});
		bottonsPanel.add(boxCurvaAltaButton);
		radiosPanel.add(bottonsPanel);

		bottonsPanel = new JPanel();
		bottonsPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("104");
			}
		});
		bottonsPanel.add(paraBoxButton);
		radiosPanel.add(bottonsPanel);

		gerarLayout(frame, controlPanel, buttonsPanel, radiosPanel);
		testePista = new TestePista(this, circuito);
		adicionaEventosMouse(frame);
	}

	private JPanel gerarBotoesTracado() {
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(1, 4));

		JButton creditosButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("creditos");
			}
		};
		creditosButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					creditos();
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});
		buttonsPanel.add(creditosButton);

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

		JButton apagarUltimoNoButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("105");
			}
		};
		apagarUltimoNoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					apagarUltimoNo();
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});

		buttonsPanel.add(apagarUltimoNoButton);

		JButton apagaNoListaButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("apagaNoListaButton");
			}
		};
		apagaNoListaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					apagarUltimoNoBox();
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});

		buttonsPanel.add(apagaNoListaButton);

		buttonsPanel.add(desenhaTracadoBot);
		return buttonsPanel;
	}

	private JPanel gerarListsNosPistaBox() {
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
			}
		});
		JPanel radioPistaPanel = new JPanel();
		radioPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("032");
			}
		});
		JPanel pistas = new JPanel();
		pistas.setLayout(new BorderLayout());
		pistas.add(radioPistaPanel, BorderLayout.NORTH);
		JScrollPane pistaJListJScrollPane = new JScrollPane(pistaJList) {
			@Override
			@Transient
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().height, 160);
			}
		};
		pistas.add(pistaJListJScrollPane, BorderLayout.CENTER);
		controlPanel.add(pistas);
		JPanel boxes = new JPanel();
		boxes.setLayout(new BorderLayout());
		radioPistaPanel = new JPanel();
		radioPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("033");
			}
		});
		boxes.add(radioPistaPanel, BorderLayout.NORTH);
		JScrollPane boxJListJScrollPane = new JScrollPane(boxJList) {
			@Override
			@Transient
			public Dimension getPreferredSize() {
				return new Dimension(super.getPreferredSize().height, 160);
			}
		};
		boxes.add(boxJListJScrollPane, BorderLayout.CENTER);
		controlPanel.add(boxes);
		return controlPanel;
	}

	private void gerarLayout(JFrame frame, JPanel controlPanel,
			JPanel buttonsPanel, JPanel radiosPanel) {
		frame.getContentPane().removeAll();
		frame.setLayout(new BorderLayout());
		if (backGround != null)
			this.setPreferredSize(new Dimension(backGround.getWidth(),
					backGround.getHeight()));
		else {
			this.setPreferredSize(new Dimension(10000, 10000));
		}
		frame.getContentPane().add(controlPanel, BorderLayout.WEST);
		scrollPane = new JScrollPane(this,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		JPanel nothPanel = new JPanel(new GridLayout(2, 1));
		nothPanel.add(radiosPanel);
		nothPanel.add(buttonsPanel);
		frame.getContentPane().add(nothPanel, BorderLayout.NORTH);
		frame.getContentPane()
				.add(iniciaEditorVetorizado(), BorderLayout.SOUTH);
		formularioListaObjetos = new FormularioListaObjetos(this);
		formularioListaObjetos.listarObjetos();
		frame.getContentPane().add(formularioListaObjetos.getObjetos(),
				BorderLayout.EAST);

	}

	public void esquerda() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				if (p == null) {
					return;
				}
				p.x -= 40;
				repaint();
				scrollPane.getViewport().setViewPosition(p);
			}
		});
	}

	public void direita() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				if (p == null) {
					return;
				}
				p.x += 40;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
	}

	public void cima() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				if (p == null) {
					return;
				}
				p.y -= 40;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
	}

	public void baixo() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				if (p == null) {
					return;
				}
				p.y += 40;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
	}

	private void adicionaEventosMouse(final JFrame frame) {
		this.addMouseListener(new MouseAdapter() {

			public void mouseEntered(MouseEvent e) {
				super.mouseEntered(e);
				frame.requestFocus();
			}

			public void mouseClicked(MouseEvent e) {
				if (editarObjetos) {
					clickEditarObjetos(e);
				} else {
					No no = new No();
					no.setTipo(getTipoNo());
					no.setPoint(e.getPoint());
					inserirNoNasJList(no);
					ultimoNo = no;
				}
				repaint();
			}

			private void clickEditarObjetos(MouseEvent e) {
				ultimoClicado = e.getPoint();
				if (!moverObjetoPista && e.getClickCount() > 1) {
					editaObjetoPista(ultimoClicado);
					return;
				}
				if (e.getButton() != MouseEvent.BUTTON1) {
					moverObjetoPista = false;
				}
				if (desenhandoObjetoLivre
						&& (objetoPista instanceof ObjetoTransparencia)) {
					ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
					if (e.getButton() == MouseEvent.BUTTON1) {
						objetoTransparencia.getPontos().add(ultimoClicado);
					} else {
						desenhandoObjetoLivre = false;
						objetoTransparencia.setTransparencia(125);
						if (circuito.getObjetos() == null)
							circuito.setObjetos(new ArrayList<ObjetoPista>());
						circuito.getObjetos().add(objetoTransparencia);
						formularioListaObjetos.listarObjetos();
						objetoTransparencia.setNome("Objeto "
								+ circuito.getObjetos().size());
						objetoTransparencia.gerar();
						objetoPista = null;
					}
					repaint();
					return;
				} else if (posicionaObjetoPista) {
					if (circuito.getObjetos() == null)
						circuito.setObjetos(new ArrayList<ObjetoPista>());
					objetoPista.setPosicaoQuina(ultimoClicado);
					circuito.getObjetos().add(objetoPista);
					formularioListaObjetos.listarObjetos();
					objetoPista.setNome("Objeto "
							+ circuito.getObjetos().size());
					repaint();
					posicionaObjetoPista = false;
					return;
				} else if (moverObjetoPista) {
					if (objetoPista != null)
						objetoPista.setPosicaoQuina(new Point(Util.inte(e
								.getPoint().x / zoom), Util.inte(e.getPoint().y
								/ zoom)));
					repaint();
					return;
				} else if (creditos) {
					circuito.setCreditos(e.getPoint());
					repaint();
					creditos = false;
					return;
				} else if (pontosEscape) {
					if (circuito.getEscapeList() == null) {
						circuito.setEscapeList(new ArrayList<Point>());
					}
					circuito.getEscapeList().add(e.getPoint());
					repaint();
					pontosEscape = false;
					return;
				}

				Logger.logar("Pontos Editor :" + e.getX() + " - " + e.getY());
				if ((getTipoNo() == null) || (e.getButton() == 3)) {
					srcFrame.requestFocus();

					return;
				}
			}
		});
	}

	protected void editaObjetoPista(Point point) {
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (objetoPista.obterArea().contains(point)) {
				FormularioObjetos formularioObjetos = new FormularioObjetos(
						MainPanelEditor.this);
				formularioObjetos.objetoLivreFormulario(objetoPista);
				break;
			}
		}
	}

	private void inserirNoNasJList(No no) {
		if (boxButton.isSelected() || paraBoxButton.isSelected()
				|| boxRetaButton.isSelected()
				|| boxCurvaAltaButton.isSelected()) {
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
				JOptionPane.showMessageDialog(this, Lang.msg("038"),
						Lang.msg("039"), JOptionPane.INFORMATION_MESSAGE);
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
		if (mostraBG && backGround != null) {
			g2d.drawImage(backGround, 0, 0, null);
		}

		if (larguraPistaPixeis == 0)
			larguraPistaPixeis = Util.inte(Carro.LARGURA * 1.5
					* multiplicadorLarguraPista * zoom);

		if (!mostraBG) {
			if (pista == null)
				pista = new BasicStroke(larguraPistaPixeis,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (pistaTinta == null)
				pistaTinta = new BasicStroke(
						Util.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (box == null)
				box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (zebra == null)
				zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
						new float[] { 10, 10 }, 0);
			g2d.setColor(circuito.getCorFundo());
			desenhaTintaPistaEZebra(g2d);
			desenhaPista(g2d);
			desenhaPistaBox(g2d);
		}
		desenhaCarroTeste(g2d);
		desenhaEntradaParadaSaidaBox(g2d);
		desenhaLargada(g2d);
		desenhaGrid(g2d);
		desenhaBoxes(g2d);
		desenhaObjetosBaixo(g2d);
		desenhaPreObjetoLivre(g2d);
		desenhaPreObjetoTransparencia(g2d);
		desenhaObjetosCima(g2d);
		desenhaInfo(g2d);
		desenhaControles(g2d);
		desenhaListaObjetos(g2d);
		desenhaNosChave(g2d);
		desenhaPainelClassico(g2d);
	}

	private void desenhaControles(Graphics2D g2d) {
		Rectangle limitesViewPort = (Rectangle) limitesViewPort();
		int x = limitesViewPort.getBounds().x + limitesViewPort.width - 300;
		int y = limitesViewPort.getBounds().y + 20;
		g2d.setColor(PainelCircuito.lightWhiteRain);
		g2d.fillRoundRect(x - 15, y - 15, 200, 180, 15, 15);
		g2d.setColor(Color.black);
		// Esquerda
		g2d.drawString("\u2190", x, y);
		y += 20;
		// Baixo
		g2d.drawString("\u2193", x, y);
		y += 20;
		//Direira
		g2d.drawString("\u2192", x, y);
		y += 20;
		//Cima
		g2d.drawString("\u2191", x + 5, y + 25);
		// TODO Auto-generated method stub
	}

	private void desenhaListaObjetos(Graphics2D g2d) {
		if (formularioListaObjetos != null) {
			if (formularioListaObjetos.getList().getSelectedIndex() != -1) {
				ObjetoPista objetoPista = (ObjetoPista) formularioListaObjetos
						.getDefaultListModelOP().get(
								formularioListaObjetos.getList()
										.getSelectedIndex());
				g2d.setColor(PainelCircuito.lightWhiteRain);
				Point loc = objetoPista.obterArea().getLocation();
				loc = new Point((int) (loc.x * zoom), (int) (loc.y * zoom));
				g2d.fillRect(loc.x, loc.y, 22, 12);
				g2d.setColor(Color.BLACK);
				g2d.drawString(objetoPista.getNome().split(" ")[1], loc.x,
						loc.y + 10);
			}
		}
	}

	private void desenhaInfo(Graphics2D g2d) {

		Rectangle limitesViewPort = (Rectangle) limitesViewPort();
		int x = limitesViewPort.getBounds().x + 30;
		int y = limitesViewPort.getBounds().y + 20;
		g2d.setColor(PainelCircuito.lightWhiteRain);
		g2d.fillRoundRect(x - 15, y - 15, 200, 180, 15, 15);
		g2d.setColor(Color.black);
		g2d.drawString("Zoom : " + zoom, x, y);
		y += 20;
		g2d.drawString("Multiplicador Pista : " + multiplicadorPista, x, y);
		y += 20;
		g2d.drawString("Multiplicador Largura Pista : "
				+ multiplicadorLarguraPista, x, y);
		y += 20;
		g2d.drawString("Box : " + testePista.isIrProBox(), x, y);
		y += 20;
		g2d.drawString("Simula Max : " + testePista.isMaxHP(), x, y);
		if (circuito.getObjetos() != null) {
			y += 20;
			g2d.drawString("Num Objetos : " + circuito.getObjetos().size(), x,
					y);
		}
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
		y += 20;
		g2d.drawString(Lang.msg("ALTA") + ":" + noAlta + " "
				+ (int) (100 * noAlta / total) + "%", x, y);
		y += 20;
		g2d.drawString(Lang.msg("MEDIA") + ":" + noMedia + " "
				+ (int) (100 * noMedia / total) + "%", x, y);
		y += 20;
		g2d.drawString(Lang.msg("BAIXA") + ":" + noBaixa + " "
				+ (int) (100 * noBaixa / total) + "%", x, y);

		// limitesViewPort.width -= 100;
		// limitesViewPort.height -= 100;

		// g2d.draw(limitesViewPort);
	}

	private void desenhaObjetosCima(Graphics2D g2d) {
		if (circuito == null) {
			return;
		}
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (!objetoPista.isPintaEmcima())
				continue;
			objetoPista.desenha(g2d, zoom);
		}
	}

	private void desenhaPreObjetoTransparencia(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		if (objetoPista == null || !desenhandoObjetoLivre
				|| !(objetoPista instanceof ObjetoTransparencia))
			return;
		ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
		if (objetoTransparencia.getPontos().size() == 1) {
			return;
		}
		Point ant = null;
		for (Point p : objetoTransparencia.getPontos()) {
			if (ant != null) {
				g2d.drawLine(Util.inte(ant.x * zoom), Util.inte(ant.y * zoom),
						Util.inte(p.x * zoom), Util.inte(p.y * zoom));
			}
			ant = p;
		}

	}

	private void desenhaPreObjetoLivre(Graphics2D g2d) {
		g2d.setColor(Color.BLACK);
		if (objetoPista == null || !desenhandoObjetoLivre
				|| !(objetoPista instanceof ObjetoLivre))
			return;
		ObjetoLivre objetoLivre = (ObjetoLivre) objetoPista;
		if (objetoLivre.getPontos().size() == 1) {
			return;
		}
		Point ant = null;
		for (Point p : objetoLivre.getPontos()) {
			if (ant != null) {
				g2d.drawLine(Util.inte(ant.x * zoom), Util.inte(ant.y * zoom),
						Util.inte(p.x * zoom), Util.inte(p.y * zoom));
			}
			ant = p;
		}

	}

	private void desenhaBoxes(Graphics2D g2d) {
		if (circuito.getBoxFull().isEmpty()) {
			return;
		}
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 1.5 * i)
					+ Carro.LARGURA;
			No n1 = (No) circuito.getBoxFull().get(iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getBoxFull().get(iP);
			No n2 = (No) circuito.getBoxFull().get(iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom),
					Util.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom),
					Util.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom),
					Util.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA
							* getCircuito().getMultiplicadorLarguraPista()
							* zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA
							* getCircuito().getMultiplicadorLarguraPista()
							* zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte((Carro.ALTURA) * 3.2 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte((Carro.ALTURA) * 3.5 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte((Carro.ALTURA) * 3.2 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));

			RoundRectangle2D retC1 = null;
			RoundRectangle2D retC2 = null;
			if (circuito.getLadoBox() == 1) {
				rectangle = new Rectangle2D.Double(
						(cima.x - (Carro.MEIA_LARGURA * zoom)),
						(cima.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double(
						(cimaBoxC1.x - (Carro.LARGURA * zoom)),
						(cimaBoxC1.y - (Carro.ALTURA * zoom)),
						(Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom),
						5, 5);
				retC2 = new RoundRectangle2D.Double(
						(cimaBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(cimaBoxC2.y + (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5, 5);
			} else {
				rectangle = new Rectangle2D.Double(
						(baixo.x - (Carro.MEIA_LARGURA * zoom)),
						(baixo.y - (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom));
				retC1 = new RoundRectangle2D.Double(
						(baixoBoxC1.x - (Carro.LARGURA * zoom)),
						(baixoBoxC1.y - (Carro.ALTURA * zoom)),
						(Carro.LARGURA * 2 * zoom), (Carro.ALTURA * 3 * zoom),
						5, 5);
				retC2 = new RoundRectangle2D.Double(
						(baixoBoxC2.x - (Carro.MEIA_LARGURA * zoom)),
						(baixoBoxC2.y + (Carro.MEIA_ALTURA * zoom)),
						(Carro.LARGURA * zoom), (Carro.ALTURA * zoom), 5, 5);
			}

			GeneralPath generalPath = new GeneralPath(rectangle);
			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			double rad = Math.toRadians((double) calculaAngulo);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(new Color(255, 0, 255, 150));
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));
			generalPath = new GeneralPath(retC1);
			affineTransformRect.setToRotation(rad, retC1.getCenterX(),
					retC1.getCenterY());
			g2d.setColor(new Color(0, 255, 255, 150));
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			generalPath = new GeneralPath(retC2);
			affineTransformRect.setToRotation(rad, retC2.getCenterX(),
					retC2.getCenterY());
			g2d.setColor(new Color(255, 0, 255, 150));
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));
			// g2d.fillOval((int) rectangle.getCenterX(), (int) rectangle
			// .getCenterY(), 10, 10);
			g2d.setColor(Color.ORANGE);

			g2d.fillOval((int) cimaBoxC1.x, (int) cimaBoxC1.y, 10, 10);
			g2d.setColor(Color.RED);

			g2d.fillOval((int) baixoBoxC1.x, (int) baixoBoxC1.y, 10, 10);

		}

	}

	private void desenhaGrid(Graphics2D g2d) {
		if (circuito.getPistaFull().isEmpty()) {
			return;
		}
		for (int i = 0; i < 24; i++) {
			int iP = 50 + Util.inte(((Carro.LARGURA) * 0.8) * i);
			No n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			No nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			No n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			Point p1 = new Point(Util.inte(n1.getPoint().x * zoom),
					Util.inte(n1.getPoint().y * zoom));
			Point pm = new Point(Util.inte(nM.getPoint().x * zoom),
					Util.inte(nM.getPoint().y * zoom));
			Point p2 = new Point(Util.inte(n2.getPoint().x * zoom),
					Util.inte(n2.getPoint().y * zoom));
			double calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			Point cima = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA * 1.2 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA * 1.2 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
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
			g2d.setColor(new Color(255, 255, 255, 150));
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			iP += 5;
			n1 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP - Carro.MEIA_LARGURA);
			nM = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP);
			n2 = (No) circuito.getPistaFull().get(
					circuito.getPistaFull().size() - iP + Carro.MEIA_LARGURA);
			p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
					.getPoint().y * zoom));
			pm = new Point(Util.inte(nM.getPoint().x * zoom), Util.inte(nM
					.getPoint().y * zoom));
			p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
					.getPoint().y * zoom));
			calculaAngulo = GeoUtil.calculaAngulo(p1, p2, 0);
			rectangle = new Rectangle2D.Double((pm.x - (Carro.MEIA_LARGURA)),
					(pm.y - (Carro.MEIA_ALTURA)), (Carro.LARGURA),
					(Carro.ALTURA));

			cima = GeoUtil.calculaPonto(calculaAngulo, Util.inte(Carro.ALTURA
					* 1.2 * zoom), new Point(Util.inte(rectangle.getCenterX()),
					Util.inte(rectangle.getCenterY())));
			baixo = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA * 1.2 * zoom),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
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
			g2d.setColor(new Color(192, 192, 192, 150));
		}

	}

	private void desenhaLargada(Graphics2D g2d) {
		if (circuito.getPistaFull().isEmpty()) {
			return;
		}
		No n1 = (No) circuito.getPistaFull().get(0);
		No n2 = (No) circuito.getPistaFull().get(20);
		Point p1 = new Point(Util.inte(n1.getPoint().x * zoom), Util.inte(n1
				.getPoint().y * zoom));
		Point p2 = new Point(Util.inte(n2.getPoint().x * zoom), Util.inte(n2
				.getPoint().y * zoom));
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

	private void desenhaEntradaParadaSaidaBox(Graphics2D g2d) {
		if (circuito.getPistaFull().isEmpty()) {
			return;
		}
		if (circuito.getBoxFull().isEmpty()) {
			return;
		}
		Point e = ((No) circuito.getPistaFull().get(
				circuito.getEntradaBoxIndex())).getPoint();
		Point p = ((No) circuito.getBoxFull().get(circuito.getParadaBoxIndex()))
				.getPoint();
		Point s = ((No) circuito.getPistaFull()
				.get(circuito.getSaidaBoxIndex())).getPoint();
		g2d.setColor(Color.BLACK);
		g2d.fillOval(Util.inte(e.x * zoom), Util.inte(e.y * zoom),
				Util.inte(5 * zoom), Util.inte(5 * zoom));
		g2d.fillOval(Util.inte(p.x * zoom), Util.inte(p.y * zoom),
				Util.inte(5 * zoom), Util.inte(5 * zoom));

		g2d.fillOval(Util.inte(s.x * zoom), Util.inte(s.y * zoom),
				Util.inte(5 * zoom), Util.inte(5 * zoom));

	}

	private void desenhaCarroTeste(Graphics2D g2d) {
		g2d.setColor(Color.black);
		g2d.setStroke(trilho);
		if (testePista != null && testePista.getTestCar() != null) {

			int width = (int) (carroCima.getWidth());
			int height = (int) (carroCima.getHeight());
			int w2 = width / 2;
			int h2 = height / 2;
			int carx = testePista.getTestCar().x - w2;
			int cary = testePista.getTestCar().y - h2;

			AffineTransform afZoom = new AffineTransform();
			AffineTransform afRotate = new AffineTransform();
			double calculaAngulo = GeoUtil.calculaAngulo(testePista.frenteCar,
					testePista.trazCar, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(testePista.getTestCar().x - Carro.MEIA_LARGURA),
					(testePista.getTestCar().y - Carro.MEIA_ALTURA),
					Carro.LARGURA, Carro.ALTURA);
			Point p1 = GeoUtil.calculaPonto(
					calculaAngulo,
					Util.inte(Carro.ALTURA * multiplicadorLarguraPista),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));
			g2d.setColor(Color.black);
			Point p2 = GeoUtil.calculaPonto(
					calculaAngulo + 180,
					Util.inte(Carro.ALTURA * multiplicadorLarguraPista),
					new Point(Util.inte(rectangle.getCenterX()), Util
							.inte(rectangle.getCenterY())));

			if (pos == 0) {
				carx = testePista.getTestCar().x - w2;
				cary = testePista.getTestCar().y - h2;
			}
			if (pos == 1) {
				carx = Util.inte((p1.x - w2));
				cary = Util.inte((p1.y - h2));
			}
			if (pos == 2) {
				carx = Util.inte((p2.x - w2));
				cary = Util.inte((p2.y - h2));
			}

			double rad = Math.toRadians((double) calculaAngulo);
			afZoom.setToScale(zoom, zoom);
			afRotate.setToRotation(rad, carroCima.getWidth() / 2,
					carroCima.getHeight() / 2);

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
			g2d.drawImage(rotateBuffer, Util.inte(carx * zoom),
					Util.inte(cary * zoom), null);

			AffineTransform affineTransformRect = AffineTransform
					.getScaleInstance(zoom, zoom);
			affineTransformRect.setToRotation(rad, rectangle.getCenterX(),
					rectangle.getCenterY());
			g2d.setColor(new Color(255, 0, 0, 140));

			g2d.fillOval(Util.inte(testePista.frenteCar.x * zoom),
					Util.inte(testePista.frenteCar.y * zoom),
					Util.inte(5 * zoom), Util.inte(5 * zoom));
			g2d.fillOval(Util.inte(testePista.trazCar.x * zoom),
					Util.inte(testePista.trazCar.y * zoom),
					Util.inte(5 * zoom), Util.inte(5 * zoom));
		}
	}

	private void desenhaObjetosBaixo(Graphics2D g2d) {
		if (circuito == null) {
			return;
		}
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (objetoPista.isPintaEmcima()
					|| objetoPista.getPosicaoQuina() == null)
				continue;
			objetoPista.desenha(g2d, zoom);
		}

	}

	private void desenhaPista(Graphics2D g2d) {
		No oldNo = null;
		g2d.setColor(COR_PISTA);
		g2d.setStroke(pista);
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));

				oldNo = no;
			}
		}

		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom),
				Util.inte(oldNo.getY() * zoom),
				Util.inte(noFinal.getX() * zoom),
				Util.inte(noFinal.getY() * zoom));
	}

	public Shape limitesViewPort() {
		Rectangle rectangle = scrollPane.getViewport().getBounds();
		// rectangle.width += 50;
		// rectangle.height += 50;
		rectangle.x = scrollPane.getViewport().getViewPosition().x;
		rectangle.y = scrollPane.getViewport().getViewPosition().y;
		return rectangle;
	}

	private void desenhaNosChave(Graphics2D g2d) {
		if (nosChave != null && nosChave.isSelected()) {
			No oldNo = null;
			int count = 0;
			for (Iterator iter = circuito.getPista().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				g2d.drawImage(no.getBufferedImage(), no.getDrawX(),
						no.getDrawY(), null);
				g2d.setColor(Color.BLACK);
				if (oldNo == null) {
					oldNo = no;
					g2d.setColor(Color.WHITE);
					g2d.drawString("Index " + count, no.getDrawX(),
							no.getDrawY());
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(),
							no.getY());
					count += GeoUtil.drawBresenhamLine(oldNo.getX(),
							oldNo.getY(), no.getX(), no.getY()).size();
					g2d.setColor(Color.WHITE);
					g2d.drawString("Index " + count, no.getDrawX(),
							no.getDrawY());
					oldNo = no;
				}
			}
			oldNo = null;
			count = 0;
			for (Iterator iter = circuito.getBox().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				g2d.setColor(no.getTipo());
				g2d.fillRoundRect(no.getDrawX(), no.getDrawY(), 10, 10, 15, 15);
				g2d.setColor(Color.BLACK);
				if (oldNo == null) {
					oldNo = no;
					g2d.setColor(Color.WHITE);
					g2d.drawString("Index " + count, no.getDrawX(),
							no.getDrawY());
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(),
							no.getY());
					count += GeoUtil.drawBresenhamLine(oldNo.getX(),
							oldNo.getY(), no.getX(), no.getY()).size();
					g2d.setColor(Color.WHITE);
					g2d.drawString("Index " + count, no.getDrawX(),
							no.getDrawY());
					oldNo = no;
				}
			}
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
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));

				oldNo = no;
			}
		}
		if (circuito.getBoxKey() != null && !circuito.getBoxKey().isEmpty()) {
			No noFinal = (No) circuito.getBoxKey().get(
					circuito.getBoxKey().size() - 1);

			g2d.drawLine(Util.inte(oldNo.getX() * zoom),
					Util.inte(oldNo.getY() * zoom),
					Util.inte(noFinal.getX() * zoom),
					Util.inte(noFinal.getY() * zoom));
		}
	}

	private void desenhaTintaPistaEZebra(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setStroke(pistaTinta);

		No oldNo = null;
		for (Iterator iter = circuito.getPistaKey().iterator(); iter.hasNext();) {
			No no = (No) iter.next();
			if (oldNo == null) {
				oldNo = no;
			} else {
				g2d.setColor(Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(Util.inte(oldNo.getX() * zoom),
						Util.inte(oldNo.getY() * zoom),
						Util.inte(no.getX() * zoom),
						Util.inte(no.getY() * zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo())
						|| No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inte(oldNo.getX() * zoom),
							Util.inte(oldNo.getY() * zoom),
							Util.inte(no.getX() * zoom),
							Util.inte(no.getY() * zoom));

				}
				oldNo = no;
			}
		}
		No noFinal = (No) circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inte(oldNo.getX() * zoom),
				Util.inte(oldNo.getY() * zoom),
				Util.inte(noFinal.getX() * zoom),
				Util.inte(noFinal.getY() * zoom));
	}

	private void desenhaPainelClassico(Graphics g2d) {
		if (circuito != null && circuito.getCreditos() != null) {
			g2d.setColor(oran);
			g2d.fillOval(circuito.getCreditos().x - 2,
					circuito.getCreditos().y - 2, 8, 8);
		}

		if (circuito != null) {
			int altura = Carro.LARGURA * 5;
			int mAltura = altura / 2;
			List<Point> escapeList = circuito.getEscapeList();

			if (escapeList != null) {
				for (Iterator iterator = escapeList.iterator(); iterator
						.hasNext();) {
					Point point = (Point) iterator.next();
					g2d.setColor(ver);
					g2d.fillOval(point.x - mAltura, point.y - mAltura, altura,
							altura);
				}
			}
		}

		if (desenhaTracado) {
			No oldNo = null;

			for (Iterator iter = circuito.getPista().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				g2d.drawImage(no.getBufferedImage(), no.getDrawX(),
						no.getDrawY(), null);

				if (oldNo == null) {
					oldNo = no;
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(),
							no.getY());
					oldNo = no;
				}

				if (pistaJList != null && pistaJList.getSelectedValue() == no) {
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
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(),
							no.getY());
					oldNo = no;
				}

				if (boxJList != null && boxJList.getSelectedValue() == no) {
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

	public void apagarUltimoNoPista() {
		if (circuito.getPista().size() == 0) {
			return;
		}

		((DefaultListModel) pistaJList.getModel()).removeElement(circuito
				.getPista().remove(circuito.getPista().size() - 1));
		repaint();
	}

	public void apagarUltimoNoBox() {
		if (circuito.getBox().size() == 0) {
			return;
		}

		((DefaultListModel) boxJList.getModel()).removeElement(circuito
				.getBox().remove(circuito.getBox().size() - 1));
		repaint();
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
		String fileName = file.getCanonicalFile().toString();
		if (!fileName.endsWith(".f1mane")) {
			fileName += ".f1mane";
		}
		file = new File(fileName);

		FileOutputStream fileOutputStream = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream);
		oos.writeObject(circuito);
		oos.flush();
		fileOutputStream.close();
	}

	public Dimension getPreferredSize() {
		if (backGround != null) {
			return new Dimension(backGround.getWidth(), backGround.getHeight());
		} else {
			return new Dimension(10000, 10000);
		}
	}

	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return super.getPreferredSize();
	}

	public void creditos() {
		creditos = true;
	}

	public Color getTipoNo() {
		if (largadaButton.isSelected()) {
			tipoNo = No.LARGADA;
		}
		if (retaButton.isSelected()) {
			tipoNo = No.RETA;
		}
		if (curvaAltaButton.isSelected()) {
			tipoNo = No.CURVA_ALTA;
		}
		if (curvaBaixaButton.isSelected()) {
			tipoNo = No.CURVA_BAIXA;
		}
		if (boxButton.isSelected()) {
			tipoNo = No.BOX;
		}
		if (boxRetaButton.isSelected()) {
			tipoNo = No.RETA;
		}
		if (boxCurvaAltaButton.isSelected()) {
			tipoNo = No.CURVA_ALTA;
		}
		if (paraBoxButton.isSelected()) {
			tipoNo = No.PARADA_BOX;
		}
		return tipoNo;
	}

	private JPanel iniciaEditorVetorizado() {
		JPanel radioPistaPanel = new JPanel();
		radioPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("032");
			}
		});
		JPanel radioBoxPanel = new JPanel();
		radioBoxPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("033");
			}
		});
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(3, 6));

		JButton testaPistaButton = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("034");
			}
		};
		testaPistaButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					vetorizarCircuito();
					testePista.iniciarTeste(multiplicadorPista);
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
		JButton left = new JButton() {
			@Override
			public String getText() {
				return "<";
			}
		};
		left.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pos = 1;
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
			}
		});
		buttonsPanel.add(right);

		JButton inflarPistaBot = new JButton("") {
			@Override
			public String getText() {
				return Lang.msg("vetorizarPista");
			}
		};
		ladoBoxCombo = new JComboBox();
		ladoBoxCombo.addItem(LADO_COMBO_1);
		ladoBoxCombo.addItem(LADO_COMBO_2);
		buttonsPanel.add(ladoBoxCombo);

		inflarPistaBot.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					multiplicadorPista = Double.parseDouble(tamanhoPistaText
							.getText());
					multiplicadorLarguraPista = Double
							.parseDouble(larguraPistaText.getText());
					vetorizarCircuito();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(inflarPistaBot);

		tamanhoPistaText = new JTextField();
		tamanhoPistaText.setText("" + multiplicadorPista);
		JPanel tamanhoPistaPanel = new JPanel(new GridLayout(1, 2));
		tamanhoPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("tamanhoPista");
			}
		});
		tamanhoPistaPanel.add(tamanhoPistaText);
		buttonsPanel.add(tamanhoPistaPanel);

		larguraPistaText = new JTextField();
		larguraPistaText.setText("" + multiplicadorLarguraPista);

		JPanel larguraPistaPanel = new JPanel(new GridLayout(1, 2));
		larguraPistaPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("larguraPista");
			}
		});
		larguraPistaPanel.add(larguraPistaText);
		buttonsPanel.add(larguraPistaPanel);

		JButton corFundo = new JButton("Cor de Fundo");
		corFundo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Color color = JColorChooser.showDialog(
							MainPanelEditor.this.srcFrame,
							Lang.msg("escolhaCor"), Color.WHITE);
					circuito.setCorFundo(color);
					repaint();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(corFundo);
		transparencia.setValue(255);
		JPanel panelTransparencia = new JPanel(new GridLayout(1, 2));
		panelTransparencia.add(new JLabel("Transparencia Fundo"));
		panelTransparencia.add(transparencia);
		buttonsPanel.add(panelTransparencia);
		transparencia.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				int transp = (Integer) transparencia.getValue();
				if (transp > 255) {
					transp = 255;
				}
				if (transp < 0) {
					transp = 0;
				}
				if (circuito.getCorFundo() == null) {
					return;
				}
				Color color = new Color(circuito.getCorFundo().getRed(),
						circuito.getCorFundo().getGreen(), circuito
								.getCorFundo().getBlue(), transp);
				circuito.setCorFundo(color);
				repaint();
			}
		});

		JPanel panelNoite = new JPanel(new GridLayout(1, 2));
		panelNoite.add(new JLabel("Noite"));
		final JCheckBox noite = new JCheckBox();
		panelNoite.add(noite);
		buttonsPanel.add(panelNoite);
		noite.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				editarObjetos = true;
				circuito.setNoite(noite.isSelected());
				repaint();
			}
		});

		JPanel panelUsaBkg = new JPanel(new GridLayout(1, 2));
		panelUsaBkg.add(new JLabel("UsaBkg"));
		final JCheckBox usaBkg = new JCheckBox();
		if (circuito != null && mostraBG) {
			usaBkg.setSelected(true);
		}
		panelUsaBkg.add(usaBkg);
		buttonsPanel.add(panelUsaBkg);
		usaBkg.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				mostraBG = usaBkg.isSelected();
				repaint();
			}
		});

		JButton criarObjeto = new JButton("Criar Objeto");
		criarObjeto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editarObjetos = true;
					FormularioObjetos formularioObjetos = new FormularioObjetos(
							MainPanelEditor.this);
					formularioObjetos.mostrarPainelModal();
					if (FormularioObjetos.OBJETO_ESCAPADA
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoEscapada();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_TRANSPARENCIA
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoTransparencia();
						objetoPista.setTransparencia(125);
						posicionaObjetoPista = true;
						desenhandoObjetoLivre = true;
					}
					formularioObjetos.formularioObjetoPista(objetoPista);
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(criarObjeto);
		JButton listaObjetos = new JButton("Editar Objetos") {
			@Override
			public String getText() {
				return "Editar Objetos " + editarObjetos;
			}
		};
		listaObjetos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editarObjetos = !editarObjetos;
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(listaObjetos);
		JButton moverPelaTela = new JButton("Mover Pela Tela");
		moverPelaTela.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editarObjetos = true;
					srcFrame.requestFocus();
				} catch (Exception e2) {
					e2.printStackTrace();
				}

			}
		});
		buttonsPanel.add(moverPelaTela);

		JPanel nosChavePanel = new JPanel(new GridLayout(1, 2));
		nosChavePanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("desnhaNosChave");
			}
		});
		nosChave = new JCheckBox();
		nosChavePanel.add(nosChave);
		buttonsPanel.add(nosChavePanel);
		return buttonsPanel;
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
				repaint();
				scrollPane.getViewport().setViewPosition(p);
			}
		});

	}

	public void esquerdaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.x -= 5;
			repaint();
			return;
		}
	}

	public void direitaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.x += 5;
			repaint();
			return;
		}
	}

	public void cimaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.y -= 5;
			repaint();
			return;
		}
	}

	public void baixoObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.y += 5;
			repaint();
			return;
		}
	}

	public void menosAngulo() {
		if (moverObjetoPista) {
			objetoPista.setAngulo(objetoPista.getAngulo() - 1);
			repaint();
			return;
		}
	}

	public void maisAngulo() {
		if (moverObjetoPista) {
			objetoPista.setAngulo(objetoPista.getAngulo() + 1);
			repaint();
			return;
		}
	}

	public void maisLargura() {
		if (moverObjetoPista) {
			objetoPista.setLargura(objetoPista.getLargura() + 1);
			repaint();
			return;
		}
	}

	public void menosLargura() {
		if (moverObjetoPista) {
			objetoPista.setLargura(objetoPista.getLargura() - 1);
			repaint();
			return;
		}
	}

	public void maisAltura() {
		if (moverObjetoPista) {
			objetoPista.setAltura(objetoPista.getAltura() + 1);
			repaint();
			return;
		}

	}

	public void menosAltura() {
		if (moverObjetoPista) {
			objetoPista.setAltura(objetoPista.getAltura() - 1);
			repaint();
			return;
		}
	}

	public void copiarObjeto() {
		if (moverObjetoPista) {
			try {
				ObjetoPista objetoPistaNovo = objetoPista.getClass()
						.newInstance();
				objetoPistaNovo.setAltura(objetoPista.getAltura());
				objetoPistaNovo.setAngulo(objetoPista.getAngulo());
				objetoPistaNovo.setCorPimaria(objetoPista.getCorPimaria());
				objetoPistaNovo
						.setCorSecundaria(objetoPista.getCorSecundaria());
				objetoPistaNovo.setLargura(objetoPista.getLargura());
				objetoPistaNovo.setPintaEmcima(objetoPista.isPintaEmcima());
				objetoPistaNovo
						.setTransparencia(objetoPista.getTransparencia());
				objetoPistaNovo.setPosicaoQuina(new Point(objetoPista
						.getPosicaoQuina().x, objetoPista.getPosicaoQuina().y));

				if (objetoPista instanceof ObjetoLivre) {
					ObjetoLivre src = (ObjetoLivre) objetoPista;
					ObjetoLivre dst = (ObjetoLivre) objetoPistaNovo;
					dst.setPontos(new ArrayList<Point>());
					List<Point> pontos = src.getPontos();
					for (Point point : pontos) {
						dst.getPontos().add(new Point(point.x, point.y));
					}
					dst.gerar();
				}
				circuito.getObjetos().add(objetoPistaNovo);
				objetoPistaNovo.setNome("Objeto "
						+ circuito.getObjetos().size());

			} catch (Exception e) {
				Logger.logarExept(e);
			}
			repaint();
			return;
		}
	}

	public boolean isMoverObjetoPista() {
		return moverObjetoPista;
	}

	public void setMoverObjetoPista(boolean moverObjetoPista) {
		this.moverObjetoPista = moverObjetoPista;
	}

	public ObjetoPista getObjetoPista() {
		return objetoPista;
	}

	public void setObjetoPista(ObjetoPista objetoPista) {
		this.objetoPista = objetoPista;
	}

	public JFrame getSrcFrame() {
		return srcFrame;
	}

	public Point getUltimoClicado() {
		return ultimoClicado;
	}

	public void setUltimoClicado(Point ultimoClicado) {
		this.ultimoClicado = ultimoClicado;
	}

	public Circuito getCircuito() {
		return circuito;
	}

	public void setCircuito(Circuito circuito) {
		this.circuito = circuito;
	}

}
