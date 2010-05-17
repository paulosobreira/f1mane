package sowbreira.f1mane.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado Em 10:51:26
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
	private BufferedImage backGround;
	int ultimoItemBoxSelecionado = -1;
	int ultimoItemPistaSelecionado = -1;
	private JRadioButton pistasButton = new JRadioButton();
	private JRadioButton boxButton = new JRadioButton();

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
		desenhaPainelClassico(g2d);
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

	public Dimension getPreferredSize() {
		return new Dimension(backGround.getWidth(), backGround.getHeight());
	}

	public Dimension getMinimumSize() {
		return super.getPreferredSize();
	}

	public Dimension getMaximumSize() {
		return super.getPreferredSize();
	}

}
