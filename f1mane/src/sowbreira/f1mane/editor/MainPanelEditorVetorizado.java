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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.ObjetoArquibancada;
import sowbreira.f1mane.entidades.ObjetoCirculo;
import sowbreira.f1mane.entidades.ObjetoConstrucao;
import sowbreira.f1mane.entidades.ObjetoGuadRails;
import sowbreira.f1mane.entidades.ObjetoLivre;
import sowbreira.f1mane.entidades.ObjetoPista;
import sowbreira.f1mane.entidades.ObjetoPneus;
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
public class MainPanelEditorVetorizado extends JPanel {
	private static final long serialVersionUID = -7001602531075714400L;
	private static final String LADO_COMBO_1 = "BOX LADO 1";
	private static final String LADO_COMBO_2 = "BOX LADO 2";
	private static final Color COR_PISTA = new Color(192, 192, 192);
	private Circuito circuito = new Circuito();
	private TestePistaVetorizado testePistaVetorizado;
	private JFrame srcFrame;
	private JRadioButton pistasButton = new JRadioButton();
	private JRadioButton boxButton = new JRadioButton();
	private JScrollPane scrollPane;
	public double zoom = 1;
	private BufferedImage carroCima;
	private int mx;
	private int my;
	private int pos = 0;
	protected double multiplicadorPista = 20;
	private double multiplicadorLarguraPista = 1.1;
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
	private BufferedImage backGround;
	private double currentZoom;
	private BufferedImage drawBuffer;
	private Thread threadBkgGen;
	private JCheckBox nosChave;

	public boolean isMoverObjetoPista() {
		return moverObjetoPista;
	}

	public Point getUltimoClicado() {
		return ultimoClicado;
	}

	public void setUltimoClicado(Point ultimoClicado) {
		this.ultimoClicado = ultimoClicado;
	}

	public ObjetoPista getObjetoPista() {
		return objetoPista;
	}

	public void setObjetoPista(ObjetoPista objetoPista) {
		this.objetoPista = objetoPista;
	}

	public void setMoverObjetoPista(boolean moverObjetoPista) {
		this.moverObjetoPista = moverObjetoPista;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public JFrame getSrcFrame() {
		return srcFrame;
	}

	public MainPanelEditorVetorizado(JFrame frame) throws IOException,
			ClassNotFoundException {
		this.srcFrame = frame;

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
		if (circuito.isUsaBkg()) {

		}
		backGround = CarregadorRecursos.carregaBackGround(circuito
				.getBackGround(), this, circuito);
		carroCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("CarroCima.png");
		formularioListaObjetos = new FormularioListaObjetos(this);
		MainPanelEditorVetorizado.this
				.addMouseWheelListener(new MouseWheelListener() {

					@Override
					public void mouseWheelMoved(MouseWheelEvent e) {
						if (circuito.isUsaBkg()) {
							zoom += e.getWheelRotation() / 90.0;
						} else {
							zoom += e.getWheelRotation() / 50.0;
						}
						if (zoom > 1) {
							zoom = 1;
						}
						if (zoom < 0.1) {
							zoom = 0.1;
						}
						atualizaVarZoom();
						if (ultimoClicado != null) {
							centralizarPonto(ultimoClicado);
							MainPanelEditorVetorizado.this.repaint();
						}
					}
				});
		MainPanelEditorVetorizado.this.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				srcFrame.requestFocus();
				ultimoClicado = new Point(Util.inte(e.getPoint().x / zoom),
						Util.inte(e.getPoint().y / zoom));
				if (!moverObjetoPista && e.getClickCount() > 1) {
					editaObjetoPista(ultimoClicado);
					return;
				}
				if (objetoPista == null) {
					return;
				}
				if (e.getButton() != MouseEvent.BUTTON1) {
					moverObjetoPista = false;
				}
				if (desenhandoObjetoLivre
						&& (objetoPista instanceof ObjetoLivre)) {
					ObjetoLivre objetoLivre = (ObjetoLivre) objetoPista;
					if (e.getButton() == MouseEvent.BUTTON1) {
						objetoLivre.getPontos().add(ultimoClicado);
					} else {
						desenhandoObjetoLivre = false;
						if (circuito.getObjetos() == null)
							circuito.setObjetos(new ArrayList<ObjetoPista>());
						circuito.getObjetos().add(objetoLivre);
						objetoLivre.setNome("Objeto "
								+ circuito.getObjetos().size());
						objetoLivre.gerar();
					}
					repaint();
				} else if (desenhandoObjetoLivre
						&& (objetoPista instanceof ObjetoTransparencia)) {
					ObjetoTransparencia objetoTransparencia = (ObjetoTransparencia) objetoPista;
					if (e.getButton() == MouseEvent.BUTTON1) {
						objetoTransparencia.getPontos().add(ultimoClicado);
					} else {
						desenhandoObjetoLivre = false;
						if (circuito.getObjetos() == null)
							circuito.setObjetos(new ArrayList<ObjetoPista>());
						circuito.getObjetos().add(objetoTransparencia);
						objetoTransparencia.setNome("Objeto "
								+ circuito.getObjetos().size());
						objetoTransparencia.gerar();
					}
					repaint();
				} else if (posicionaObjetoPista) {
					if (circuito.getObjetos() == null)
						circuito.setObjetos(new ArrayList<ObjetoPista>());
					objetoPista.setPosicaoQuina(ultimoClicado);
					circuito.getObjetos().add(objetoPista);
					objetoPista.setNome("Objeto "
							+ circuito.getObjetos().size());
					repaint();
					posicionaObjetoPista = false;
				} else if (moverObjetoPista) {
					if (objetoPista != null)
						objetoPista.setPosicaoQuina(new Point(Util.inte(e
								.getPoint().x
								/ zoom), Util.inte(e.getPoint().y / zoom)));
					repaint();
				}
				super.mouseClicked(e);
			}
		});
		srcFrame.setPreferredSize(new Dimension(800, 600));
		srcFrame.getContentPane().setLayout(new BorderLayout());
		srcFrame.getContentPane().add(scrollPane, BorderLayout.CENTER);
		testePistaVetorizado = new TestePistaVetorizado(this, circuito);
		iniciaEditor(srcFrame);
		vetorizarPistaCarregado();
		repaint();
		srcFrame.pack();
	}

	protected void editaObjetoPista(Point point) {
		if (circuito.getObjetos() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetos()) {
			if (objetoPista.obterArea().contains(point)) {
				FormularioObjetos formularioObjetos = new FormularioObjetos(
						MainPanelEditorVetorizado.this);
				formularioObjetos.objetoLivreFormulario(objetoPista);
				break;
			}
		}
	}

	protected void atualizaVarZoom() {
		larguraPistaPixeis = Util.inte(carroCima.getWidth()
				* multiplicadorLarguraPista * zoom);
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
					testePistaVetorizado.iniciarTeste(multiplicadorPista);
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
				testePistaVetorizado.testarBox();
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
				testePistaVetorizado.regMax();
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
					vetorizarPista();
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
							MainPanelEditorVetorizado.this.srcFrame, Lang
									.msg("escolhaCor"), Color.WHITE);
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
				circuito.setNoite(noite.isSelected());
				repaint();
			}
		});

		JPanel panelUsaBkg = new JPanel(new GridLayout(1, 2));
		panelUsaBkg.add(new JLabel("UsaBkg"));
		final JCheckBox usaBkg = new JCheckBox();
		if (circuito != null && circuito.isUsaBkg()) {
			usaBkg.setSelected(true);
		}
		panelUsaBkg.add(usaBkg);
		buttonsPanel.add(panelUsaBkg);
		usaBkg.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				circuito.setUsaBkg(usaBkg.isSelected());
				repaint();
			}
		});

		JButton criarObjeto = new JButton("Criar Objeto");
		criarObjeto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					FormularioObjetos formularioObjetos = new FormularioObjetos(
							MainPanelEditorVetorizado.this);
					formularioObjetos.mostrarPainelModal();
					if (FormularioObjetos.OBJETO_LIVRE.equals(formularioObjetos
							.getTipoComboBox().getSelectedItem())) {
						objetoPista = new ObjetoLivre();
						desenhandoObjetoLivre = true;
					} else if (FormularioObjetos.OBJETO_CONSTRUCAO
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoConstrucao();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_ARQUIBANCADA
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoArquibancada();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_PNEUS
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoPneus();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_CIRCULO
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoCirculo();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_GUAD_RAILS
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoGuadRails();
						posicionaObjetoPista = true;
					} else if (FormularioObjetos.OBJETO_TRANSPARENCIA
							.equals(formularioObjetos.getTipoComboBox()
									.getSelectedItem())) {
						objetoPista = new ObjetoTransparencia();
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
		JButton listaObjetos = new JButton("Listar Objetos");
		listaObjetos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					formularioListaObjetos.mostrarPainel();
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
					srcFrame.requestFocus();

					// List remover = new ArrayList();
					// List add = new ArrayList();
					// List<ObjetoPista> objetos = circuito.getObjetos();
					// int size = objetos.size();
					// for (Iterator iterator = objetos.iterator(); iterator
					// .hasNext();) {
					// ObjetoPista objetoPista = (ObjetoPista) iterator.next();
					// if (objetoPista instanceof ObjetoGuadRails) {
					// // ObjetoGuadRails objetoGuadRails = new
					// // ObjetoGuadRails();
					// // objetoGuadRails
					// // .setAltura(objetoPista.getAltura() * 10);
					// // objetoGuadRails.setAngulo(objetoPista.getAngulo());
					// // objetoGuadRails.setCorPimaria(objetoPista
					// // .getCorSecundaria());
					// // objetoGuadRails.setCorSecundaria(objetoPista
					// // .getCorSecundaria());
					// // objetoGuadRails.setPosicaoQuina(objetoPista
					// // .getPosicaoQuina());
					// // objetoGuadRails.setPintaEmcima(objetoPista
					// // .isPintaEmcima());
					// // objetoGuadRails.setTransparencia(255);
					// // objetoGuadRails.setNome("Objeto " + (size++));
					// // add.add(objetoGuadRails);
					// remover.add(objetoPista);
					// }
					// }
					// objetos.removeAll(remover);
					// objetos.addAll(add);
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
		nosChavePanel.add(larguraPistaText);
		nosChave = new JCheckBox();
		nosChavePanel.add(nosChave);
		buttonsPanel.add(nosChavePanel);
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
		if (circuito.isUsaBkg()) {
			if (currentZoom != zoom) {
				Runnable runnable = new Runnable() {
					@Override
					public void run() {
						AffineTransform affineTransform = AffineTransform
								.getScaleInstance(zoom, zoom);
						AffineTransformOp affineTransformOp = new AffineTransformOp(
								affineTransform,
								AffineTransformOp.TYPE_BILINEAR);
						drawBuffer = new BufferedImage((int) (backGround
								.getWidth() * zoom), (int) (backGround
								.getHeight() * zoom),
								BufferedImage.TYPE_INT_ARGB);
						affineTransformOp.filter(backGround, drawBuffer);
					}
				};
				if (threadBkgGen != null) {
					threadBkgGen.interrupt();
				}
				threadBkgGen = new Thread(runnable);
				threadBkgGen.start();
			}
			if (drawBuffer == null) {
				drawBuffer = backGround;
			}

			g2d.drawImage(drawBuffer, 0, 0, null);

			currentZoom = zoom;
		}

		if (carroCima == null)
			return;
		if (larguraPistaPixeis == 0)
			larguraPistaPixeis = Util.inte(carroCima.getWidth()
					* multiplicadorLarguraPista * zoom);

		if (!circuito.isUsaBkg()) {
			if (pista == null)
				pista = new BasicStroke(larguraPistaPixeis,
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (pistaTinta == null)
				pistaTinta = new BasicStroke(Util
						.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (box == null)
				box = new BasicStroke(Util.inte(larguraPistaPixeis * .4),
						BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			if (zebra == null)
				zebra = new BasicStroke(Util.inte(larguraPistaPixeis * 1.05),
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
						new float[] { 10, 10 }, 0);
			Rectangle limitesViewPort = (Rectangle) limitesViewPort();
			g2d.setColor(circuito.getCorFundo());
			g2d.fill(limitesViewPort);
			desenhaTintaPistaEZebra(g2d);
			desenhaPista(g2d);
			desenhaPistaBox(g2d);
		}
		desenhaObjetosBaixo(g2d);
		desenhaCarroTeste(g2d);
		desenhaEntradaParadaSaidaBox(g2d);
		desenhaLargada(g2d);
		desenhaGrid(g2d);
		desenhaBoxes(g2d);
		desenhaPreObjetoLivre(g2d);
		desenhaPreObjetoTransparencia(g2d);
		desenhaObjetosCima(g2d);

		desenhaInfo(g2d);

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

		if (nosChave != null && nosChave.isSelected()) {
			No oldNo = null;
			int count = 0;
			for (Iterator iter = circuito.getPista().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				count++;
				g2d.drawImage(no.getBufferedImage(), no.getDrawX(), no
						.getDrawY(), null);

				if (oldNo == null) {
					oldNo = no;
				} else {
					g2d.drawLine(oldNo.getX(), oldNo.getY(), no.getX(), no
							.getY());
					oldNo = no;
				}

				if (count % 3 == 0) {
					g2d.setColor(Color.WHITE);
					g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6,
							6, 2, 2);
					g2d.setColor(Color.black);
					g2d.drawString("Index " + no.getIndex(), no.getDrawX(), no.getDrawY());
				}
			}

			oldNo = null;
			count = 0;
			for (Iterator iter = circuito.getBox().iterator(); iter.hasNext();) {
				No no = (No) iter.next();
				count++;
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

				if (count % 3 == 0) {
					g2d.setColor(Color.WHITE);
					g2d.fillRoundRect(no.getDrawX() + 2, no.getDrawY() + 2, 6,
							6, 2, 2);
					g2d.setColor(Color.black);
					g2d.drawString("Index " + no.getIndex(), no.getDrawX(), no.getDrawY());
				}
			}

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
			if (objetoPista.isPintaEmcima())
				continue;
			objetoPista.desenha(g2d, zoom);
			AffineTransform affineTransform = AffineTransform.getScaleInstance(
					1, 1);
			double rad = Math.toRadians((double) objetoPista.getAngulo());

			GeneralPath generalPath = new GeneralPath(objetoPista.obterArea());
			affineTransform.setToRotation(rad, generalPath.getBounds()
					.getCenterX(), generalPath.getBounds().getCenterY());
			generalPath.transform(affineTransform);
			affineTransform.setToScale(zoom, zoom);
			g2d.draw(generalPath.createTransformedShape(affineTransform));
		}

	}

	public Circuito getCircuito() {
		return circuito;
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
			AffineTransform affineTransform = AffineTransform.getScaleInstance(
					1, 1);
			double rad = Math.toRadians((double) objetoPista.getAngulo());

			GeneralPath generalPath = new GeneralPath(objetoPista.obterArea());
			affineTransform.setToRotation(rad, generalPath.getBounds()
					.getCenterX(), generalPath.getBounds().getCenterY());
			generalPath.transform(affineTransform);
			affineTransform.setToScale(zoom, zoom);
			g2d.draw(generalPath.createTransformedShape(affineTransform));

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

	private void desenhaBoxes(Graphics2D g2d) {
		int paradas = circuito.getParadaBoxIndex();
		for (int i = 0; i < 12; i++) {
			int iP = paradas + Util.inte(Carro.LARGURA * 1.5 * i)
					+ Carro.LARGURA;
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
					.inte(Carro.ALTURA
							* getCircuito().getMultiplicadorLarguraPista()
							* zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixo = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA
							* getCircuito().getMultiplicadorLarguraPista()
							* zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point cimaBoxC1 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte((Carro.ALTURA) * 3.5 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixoBoxC1 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte((Carro.ALTURA) * 3.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point cimaBoxC2 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte((Carro.ALTURA) * 3.5 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));
			Point baixoBoxC2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte((Carro.ALTURA) * 3.2 * zoom), new Point(Util
					.inte(rectangle.getCenterX()), Util.inte(rectangle
					.getCenterY())));

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
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));
			generalPath = new GeneralPath(retC1);
			affineTransformRect.setToRotation(rad, retC1.getCenterX(), retC1
					.getCenterY());
			g2d.setColor(Color.CYAN);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));

			generalPath = new GeneralPath(retC2);
			affineTransformRect.setToRotation(rad, retC2.getCenterX(), retC2
					.getCenterY());
			g2d.setColor(Color.MAGENTA);
			g2d.fill(generalPath.createTransformedShape(affineTransformRect));
			// g2d.fillOval((int) rectangle.getCenterX(), (int) rectangle
			// .getCenterY(), 10, 10);
			g2d.setColor(Color.ORANGE);

			g2d.fillOval((int) cimaBoxC1.x, (int) cimaBoxC1.y, 10, 10);
			g2d.setColor(Color.RED);

			g2d.fillOval((int) baixoBoxC1.x, (int) baixoBoxC1.y, 10, 10);

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
			int iP = 50 + Util.inte(((Carro.LARGURA) * 0.8) * i);
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

		Rectangle limitesViewPort = (Rectangle) limitesViewPort();
		int x = limitesViewPort.getBounds().x + 30;
		int y = limitesViewPort.getBounds().y + 20;
		g2d.setColor(PainelCircuito.lightWhiteRain);
		g2d.fillRoundRect(x - 15, y - 15, 200, 130, 15, 15);
		g2d.setColor(Color.black);
		g2d.drawString("Zoom : " + zoom, x, y);
		y += 20;
		g2d.drawString("Multiplicador Pista : " + multiplicadorPista, x, y);
		y += 20;
		g2d.drawString("Multiplicador Largura Pista : "
				+ multiplicadorLarguraPista, x, y);
		y += 20;
		g2d.drawString("Box : " + testePistaVetorizado.isIrProBox(), x, y);
		y += 20;
		g2d.drawString("Simula Max : " + testePistaVetorizado.isMaxHP(), x, y);
		if (circuito.getObjetos() != null) {
			y += 20;
			g2d.drawString("Num Objetos : " + circuito.getObjetos().size(), x,
					y);
		}
		limitesViewPort.width -= 100;
		limitesViewPort.height -= 100;

		// g2d.draw(limitesViewPort);
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
		if (testePistaVetorizado != null
				&& testePistaVetorizado.getTestCar() != null) {

			int width = (int) (carroCima.getWidth());
			int height = (int) (carroCima.getHeight());
			int w2 = width / 2;
			int h2 = height / 2;
			int carx = testePistaVetorizado.getTestCar().x - w2;
			int cary = testePistaVetorizado.getTestCar().y - h2;

			AffineTransform afZoom = new AffineTransform();
			AffineTransform afRotate = new AffineTransform();
			double calculaAngulo = GeoUtil.calculaAngulo(
					testePistaVetorizado.frenteCar,
					testePistaVetorizado.trazCar, 0);
			Rectangle2D rectangle = new Rectangle2D.Double(
					(testePistaVetorizado.getTestCar().x - Carro.MEIA_LARGURA),
					(testePistaVetorizado.getTestCar().y - Carro.MEIA_ALTURA),
					Carro.LARGURA, Carro.ALTURA);
			Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));
			g2d.setColor(Color.black);
			Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
					.inte(Carro.ALTURA * 1.2), new Point(Util.inte(rectangle
					.getCenterX()), Util.inte(rectangle.getCenterY())));

			if (pos == 0) {
				carx = testePistaVetorizado.getTestCar().x - w2;
				cary = testePistaVetorizado.getTestCar().y - h2;
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

			g2d.fillOval(Util.inte(testePistaVetorizado.frenteCar.x * zoom),
					Util.inte(testePistaVetorizado.frenteCar.y * zoom), Util
							.inte(5 * zoom), Util.inte(5 * zoom));
			g2d.fillOval(Util.inte(testePistaVetorizado.trazCar.x * zoom), Util
					.inte(testePistaVetorizado.trazCar.y * zoom), Util
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
		g2d.setColor(COR_PISTA);
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

	public void vetorizarPista() {
		testePistaVetorizado.pararTeste();
		if (ladoBoxCombo.getSelectedItem().equals(LADO_COMBO_1)) {
			circuito.setLadoBox(1);
		} else {
			circuito.setLadoBox(2);
		}
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		srcFrame.setCursor(cursor);
		circuito.vetorizarPista(this.multiplicadorPista,
				this.multiplicadorLarguraPista);

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
				repaint();
				scrollPane.getViewport().setViewPosition(p);
			}
		});

	}

	private void vetorizarPistaCarregado() {
		testePistaVetorizado.pararTeste();
		if (circuito.getLadoBox() == 1) {
			ladoBoxCombo.setSelectedItem(LADO_COMBO_1);
		} else {
			ladoBoxCombo.setSelectedItem(LADO_COMBO_2);
		}
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
		multiplicadorPista = circuito.getMultiplciador();
		multiplicadorLarguraPista = circuito.getMultiplicadorLarguraPista();
		if (multiplicadorPista == 0) {
			multiplicadorPista = 1;
		}
		if (multiplicadorLarguraPista == 0) {
			multiplicadorLarguraPista = 1.1;
		}
		srcFrame.setCursor(cursor);
		circuito.vetorizarPista(multiplicadorPista, multiplicadorLarguraPista);

		tamanhoPistaText.setText(String.valueOf(multiplicadorPista));
		larguraPistaText.setText(String.valueOf(multiplicadorLarguraPista));
		if (circuito.getCorFundo() != null)
			transparencia.setValue(circuito.getCorFundo().getAlpha());
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

	public void esquerdaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.x -= 10;
			repaint();
			return;
		}
	}

	public void esquerda() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				p.x -= 20;
				repaint();
				scrollPane.getViewport().setViewPosition(p);
			}
		});
	}

	public void direitaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.x += 10;
			repaint();
			return;
		}
	}

	public void direita() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				p.x += 20;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
	}

	public void cimaObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.y -= 10;
			repaint();
			return;
		}
	}

	public void cima() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				p.y -= 20;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
	}

	public void baixoObj() {
		if (moverObjetoPista && objetoPista.getPosicaoQuina() != null) {
			Point p = objetoPista.getPosicaoQuina();
			p.y += 10;
			repaint();
			return;
		}
	}

	public void baixo() {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				Point p = scrollPane.getViewport().getViewPosition();
				p.y += 20;
				repaint();
				scrollPane.getViewport().setViewPosition(p);

			}
		});
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

			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			repaint();
			return;
		}
	}
}
