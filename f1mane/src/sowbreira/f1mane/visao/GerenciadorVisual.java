package sowbreira.f1mane.visao;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.applet.JogoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.recursos.idiomas.LangVO;
import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

public class GerenciadorVisual {
	public static final int VDP1 = 1;
	public static final int VDP2 = 2;
	private JPanel panelControleBox;
	private JPanel painelInfText;
	private JPanel painelInfGraf;
	private JEditorPane infoTextual;
	private ArrayList bufferTextual;
	private JScrollPane scrollPaneTextual;
	private JLabel imgClima;
	private JLabel infoAdicionaLinha1;
	private JProgressBar combustivelBar;
	private JProgressBar pneuBar;
	private JProgressBar motorBar;
	private JProgressBar pilotoBar;
	private JComboBox comboBoxClimaInicial;
	private JComboBox comboBoxNivelCorrida;
	private JComboBox comboBoxCircuito;
	private JComboBox boxPilotoSelecionado;
	private JComboBox comboBoxTemporadas;
	private JList listPilotosSelecionados;
	private JComboBox boxPneuInicial;
	private JComboBox comboBoxAsaInicial;
	private JSlider spinnerCombustivel;
	private JSpinner spinnerQtdeVoltas;
	private JSlider sliderTempoCiclo;
	private JSpinner spinnerSkillPadraoPilotos;
	private JSpinner spinnerPotenciaPadraoCarros;
	private JSpinner spinnerQtdeMinutosQualificacao;
	private JSlider spinnerDificuldadeUltrapassagem;
	private PainelCircuito painelCircuito;
	private JScrollPane scrollPane;
	private PainelTabelaPosicoes painelPosicoes;
	private InterfaceJogo controleJogo;
	private JPanel centerPanel = new JPanel();
	private JPanel eastPanel = new JPanel();
	private JLabel infoCorrida;
	private JLabel infoPiloto;
	private JPanel infoText = new JPanel();
	private JTextField nomeJogador;
	private JButton agressivo;
	private JButton alternaPiloto;
	private JButton box;
	private JButton driverThru;
	private JButton progBox;
	private PainelTabelaResultadoFinal resultadoFinal;
	private ThreadMudancaClima clima;
	private int tempoSleep = 30;
	private JComboBox giro;
	private JComboBox modoPiloto;
	private JComboBox comboBoxTipoPneu;
	private JComboBox comboBoxAsa;
	private JSlider sliderPercentCombust;
	private JButton f1;
	private JButton f2;
	private JButton f3;
	private JButton f5;
	private JButton f6;
	private JButton f7;
	private JButton drsBtn;
	private JButton kersBtn;
	private JButton autoPos;
	private Color corPadraoBarra;
	private int larguraFrame = 0;
	private int alturaFrame = 0;
	private long lastPress;
	private ProgamacaoBox progamacaoBox;
	private long ultimaChamadaBox;
	private List listaPilotosCombo;
	private List listaCarrosCombo;
	protected JCheckBox semTrocaPneu;
	protected JCheckBox semReabastacimento;
	protected JCheckBox kers;
	protected JCheckBox drs;
	private JPanel panelControlePos;
	private ImageIcon iconLua = new ImageIcon(
			CarregadorRecursos.carregarImagem("clima/lua.gif"));
	private ImageIcon iconSol = new ImageIcon(
			CarregadorRecursos.carregarImagem("clima/sol.gif"));
	private ImageIcon iconNublado = new ImageIcon(
			CarregadorRecursos.carregarImagem("clima/nublado.gif"));
	private ImageIcon iconChuva = new ImageIcon(
			CarregadorRecursos.carregarImagem("clima/chuva.gif"));
	private long ultimaTravavadaRodas;
	private Thread thDesenhaQualificacao;
	private int vdp = VDP1;

	public int getVdp() {
		return vdp;
	}

	public void setVdp(int vdp) {
		this.vdp = vdp;
	}

	public JComboBox getComboBoxTemporadas() {
		return comboBoxTemporadas;
	}

	public JScrollPane getScrollPane() {
		return scrollPane;
	}

	public GerenciadorVisual(InterfaceJogo controleJogo) throws IOException {
		this.controleJogo = controleJogo;
		progamacaoBox = new ProgamacaoBox();
	}

	public void iniciarInterfaceGraficaJogo() throws IOException {
		Logger.logar("iniciarInterfaceGraficaJogo()");
		painelCircuito = new PainelCircuito(controleJogo, this);
		scrollPane = new JScrollPane(painelCircuito,
				JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		larguraFrame = 1024;
		alturaFrame = 768;
		carregarInfoClima();
		gerarPainelPosicoes();
		gerarPainelComandos();
		gerarPainelInfoText();
		gerarPainetInfoGraf();
		gerarLayout();

		JFrame frame = controleJogo.getMainFrame();

		MouseWheelListener mw = new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				synchronized (PainelCircuito.zoomMutex) {
					double val = painelCircuito.mouseZoom;
					val += e.getWheelRotation() / 60.0;
					if (controleJogo != null
							&& painelCircuito.getBackGround() != null
							&& controleJogo.getCircuito() != null
							&& controleJogo.getCircuito().isUsaBkg()) {
						Rectangle limitesViewPort = (Rectangle) painelCircuito
								.limitesViewPort();
						if ((painelCircuito.getBackGround().getWidth() * val) < limitesViewPort
								.getWidth()) {
							return;
						}
						if ((painelCircuito.getBackGround().getHeight() * val) < limitesViewPort
								.getHeight()) {
							return;
						}

					}
					if (val > 1.0) {
						painelCircuito.mouseZoom = 1.0;
					} else if (val < 0.2) {
						painelCircuito.mouseZoom = 0.2;
					} else {
						painelCircuito.mouseZoom = Util.double2Decimal(val);
					}
				}
			}
		};
		KeyListener keyListener = geraKeyListener();
		frame.addKeyListener(keyListener);
		painelCircuito.addKeyListener(keyListener);
		painelInfText.addKeyListener(keyListener);
		painelPosicoes.addKeyListener(keyListener);
		painelPosicoes.getPosicoesTable().addKeyListener(keyListener);
		pneuBar.addKeyListener(keyListener);
		agressivo.addKeyListener(keyListener);
		modoPiloto.addKeyListener(keyListener);
		panelControleBox.addKeyListener(keyListener);
		box.addKeyListener(keyListener);
		progBox.addKeyListener(keyListener);
		comboBoxTipoPneu.addKeyListener(keyListener);
		combustivelBar.addKeyListener(keyListener);
		giro.addKeyListener(keyListener);
		infoText.addKeyListener(keyListener);
		sliderPercentCombust.addKeyListener(keyListener);
		scrollPaneTextual.addKeyListener(keyListener);
		infoTextual.addKeyListener(keyListener);
		comboBoxAsa.addKeyListener(keyListener);
		driverThru.addKeyListener(keyListener);
		f1.addKeyListener(keyListener);
		f2.addKeyListener(keyListener);
		f3.addKeyListener(keyListener);
		driverThru.addMouseWheelListener(mw);
		f1.addMouseWheelListener(mw);
		f2.addMouseWheelListener(mw);
		f3.addMouseWheelListener(mw);
		f5.addKeyListener(keyListener);
		f6.addKeyListener(keyListener);
		f7.addKeyListener(keyListener);
		drsBtn.addKeyListener(keyListener);
		kersBtn.addKeyListener(keyListener);
		f5.addMouseWheelListener(mw);
		f6.addMouseWheelListener(mw);
		f7.addMouseWheelListener(mw);
		drsBtn.addMouseWheelListener(mw);
		kersBtn.addMouseWheelListener(mw);

		autoPos.addKeyListener(keyListener);
		alternaPiloto.addKeyListener(keyListener);
		alternaPiloto.addMouseWheelListener(mw);
		autoPos.addMouseWheelListener(mw);
		frame.addMouseWheelListener(mw);
		painelInfText.addMouseWheelListener(mw);
		painelPosicoes.addMouseWheelListener(mw);
		painelPosicoes.getPosicoesTable().addMouseWheelListener(mw);
		pneuBar.addMouseWheelListener(mw);
		agressivo.addMouseWheelListener(mw);
		modoPiloto.addMouseWheelListener(mw);
		panelControleBox.addMouseWheelListener(mw);
		box.addMouseWheelListener(mw);
		progBox.addMouseWheelListener(mw);
		comboBoxTipoPneu.addMouseWheelListener(mw);
		combustivelBar.addMouseWheelListener(mw);
		giro.addMouseWheelListener(mw);
		infoText.addMouseWheelListener(mw);
		sliderPercentCombust.addMouseWheelListener(mw);
		scrollPaneTextual.addMouseWheelListener(mw);
		infoTextual.addMouseWheelListener(mw);
		comboBoxAsa.addMouseWheelListener(mw);
	}

	public void finalize() throws Throwable {
		tempoSleep = 0;
		if (clima != null) {
			clima.interrupt();
		}
		if (thDesenhaQualificacao != null) {
			thDesenhaQualificacao.interrupt();
		}
		if (painelCircuito != null) {
			painelCircuito.setBackGround(null);
			painelCircuito.setTileMap(null);
		}
		ControleSom.paraTudo();
		super.finalize();
	}

	public void carregarInfoClima() {
		imgClima = new JLabel();
	}

	private void addiconarListenerComandos(final JComboBox comboBoxTipoPneu,
			final JSlider sliderPercentCombust) {
		agressivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarModoAgressivo();
			}
		});

		modoPiloto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String modo = modoPiloto.getSelectedItem().toString();
				mudarModoPilotagem(Lang.key(modo));
			}
		});

		giro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarGiro();
			}
		});
		box.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarModoBox();
			}

		});
		driverThru.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controleJogo.driveThru();
			}

		});
		f1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				giro.setSelectedItem(new LangVO(Carro.GIRO_MIN));
			}

		});
		f2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				giro.setSelectedItem(new LangVO(Carro.GIRO_NOR));
			}

		});
		f3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				giro.setSelectedItem(new LangVO(Carro.GIRO_MAX));
			}

		});

		f5.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoPiloto.setSelectedItem(new LangVO(Piloto.LENTO));
				mudarModoPilotagem(Piloto.LENTO);
			}

		});
		f6.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoPiloto.setSelectedItem(new LangVO(Piloto.NORMAL));
				mudarModoPilotagem(Piloto.NORMAL);
			}

		});
		f7.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modoPiloto.setSelectedItem(new LangVO(Piloto.AGRESSIVO));
				mudarModoPilotagem(Piloto.AGRESSIVO);
			}

		});
		drsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				drs();
			}

		});

		kersBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				kers();
			}

		});

		autoPos.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarAutoPos();
			}

		});

		alternaPiloto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudaPilotoSelecionado();
			}

		});

		progBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				progamacaoBox();
			}

		});
		JFrame frame = controleJogo.getMainFrame();
		WindowListener[] listeners = frame.getWindowListeners();
		for (int i = 0; i < listeners.length; i++) {
			frame.removeWindowListener(listeners[i]);
		}
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			public void windowClosing(WindowEvent e) {
				if (controleJogo == null) {
					return;
				}
				int ret = JOptionPane.showConfirmDialog(
						controleJogo.getMainFrame(), Lang.msg("095"),
						Lang.msg("094"), JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.NO_OPTION) {
					return;
				}
				controleJogo.abandonar();
				super.windowClosing(e);
				if (controleJogo.getMainFrame().isModoApplet()) {
					controleJogo.getMainFrame().setVisible(false);
					controleJogo.matarTodasThreads();
				} else {
					System.exit(0);
				}
			}
		});
	}

	protected void mudarAutoPos() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarAutoPos();

	}

	protected void mudarPos2() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarPos(2);
	}

	protected void mudarPos0() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarPos(0);
	}

	protected void mudarPos1() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarPos(1);
	}

	protected void progamacaoBox() {
		JOptionPane.showMessageDialog(null, progamacaoBox.getPainel());
	}

	protected void mudarModoPilotagem(String modo) {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarModoPilotagem(modo);

	}

	private KeyListener geraKeyListener() {
		return new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				long now = System.currentTimeMillis();
				if ((now - lastPress) < 250) {
					return;
				}
				lastPress = now;
				int keyCoode = e.getKeyCode();

				if (keyCoode == KeyEvent.VK_F1) {
					giro.setSelectedItem(new LangVO(Carro.GIRO_MIN));
				}
				if (keyCoode == KeyEvent.VK_F2) {
					giro.setSelectedItem(new LangVO(Carro.GIRO_NOR));
				}
				if (keyCoode == KeyEvent.VK_F3) {
					giro.setSelectedItem(new LangVO(Carro.GIRO_MAX));
				}
				if (keyCoode == KeyEvent.VK_F4) {
					mudarModoAgressivo();
				}
				if (keyCoode == KeyEvent.VK_F11) {
					progamacaoBox();
				}
				if (keyCoode == KeyEvent.VK_F12) {
					mudarModoBox();
				}
				if (keyCoode == KeyEvent.VK_F5) {
					modoPiloto.setSelectedItem(new LangVO(Piloto.LENTO));
					mudarModoPilotagem(Piloto.LENTO);
				}
				if (keyCoode == KeyEvent.VK_F6) {
					modoPiloto.setSelectedItem(new LangVO(Piloto.NORMAL));
					mudarModoPilotagem(Piloto.NORMAL);
				}
				if (keyCoode == KeyEvent.VK_F7) {
					modoPiloto.setSelectedItem(new LangVO(Piloto.AGRESSIVO));
					mudarModoPilotagem(Piloto.AGRESSIVO);
				}
				if (keyCoode == KeyEvent.VK_ESCAPE) {
					painelCircuito.setDesenhaInfo(!painelCircuito
							.isDesenhaInfo());
				}
				if (keyCoode == KeyEvent.VK_F8) {
					mudarAutoPos();
					controleJogo.selecionaPilotoJogador();
				}
				if (keyCoode == KeyEvent.VK_LEFT) {
					if (controleJogo.getPilotoJogador() == null) {
						return;
					}
					if (controleJogo.getPilotoJogador().getTracado() == 2) {
						mudarPos0();
					} else {
						mudarPos1();
					}
				}
				if (keyCoode == KeyEvent.VK_RIGHT) {
					if (controleJogo.getPilotoJogador() == null) {
						return;
					}
					if (controleJogo.getPilotoJogador().getTracado() == 1) {
						mudarPos0();
					} else {
						mudarPos2();
					}
				}
				if (keyCoode == KeyEvent.VK_F9) {
					mudaPilotoSelecionado();
				}
				if (keyCoode == KeyEvent.VK_F10) {
					ligaDesligaSom();
				}
				if (keyCoode == KeyEvent.VK_K) {
					kers();
				}
				if (keyCoode == KeyEvent.VK_D) {
					drs();
				}
				if (Logger.ativo) {
					if (keyCoode == KeyEvent.VK_EQUALS) {
						if (controleJogo.getPilotoJogador() != null
								&& controleJogo.getPilotoJogador()
										.isJogadorHumano())
							controleJogo.aumentaFatorAcidade();
					}
					if (keyCoode == KeyEvent.VK_MINUS) {
						if (controleJogo.getPilotoJogador() != null
								&& controleJogo.getPilotoJogador()
										.isJogadorHumano())
							controleJogo.diminueFatorAcidade();
					}
				}
			}
		};
	}

	protected void drs() {
		if (controleJogo == null) {
			return;
		}
		boolean drs = controleJogo.mudarModoDRS();
	}

	protected void kers() {
		if (controleJogo == null) {
			return;
		}
		boolean kers = controleJogo.mudarModoKers();
	}

	protected void ligaDesligaSom() {
		ControleSom.ligaDesligaSom();
	}

	protected void mudaPilotoSelecionado() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudaPilotoSelecionado();

	}

	protected void mudarModoBox() {
		if (controleJogo == null) {
			return;
		}
		Integer value = (Integer) sliderPercentCombust.getValue();
		if (value.intValue() < 0) {
			value = new Integer(0);
		}
		if (value.intValue() > 100) {
			value = new Integer(100);
		}
		controleJogo.setBoxJogadorHumano(
				Lang.key(comboBoxTipoPneu.getSelectedItem().toString()), value,
				Lang.key(comboBoxAsa.getSelectedItem().toString()));
		modoBox();

	}

	private void modoBox() {
		if ((System.currentTimeMillis() - ultimaChamadaBox) < 1000) {
			return;
		}
		boolean modo = controleJogo.mudarModoBox();
		if (!(controleJogo instanceof JogoCliente)) {
			if (modo && !(controleJogo instanceof JogoCliente)) {
				box.setText(Lang.msg("137"));
			} else {
				box.setText(Lang.msg("138"));
			}

		}
		ultimaChamadaBox = System.currentTimeMillis();
	}

	protected void mudarGiro() {
		if (controleJogo == null) {
			return;
		}
		controleJogo
				.mudarGiroMotor(Lang.key(giro.getSelectedItem().toString()));
	}

	protected void mudarModoAgressivo() {
		if (controleJogo == null) {
			return;
		}
		boolean modo = controleJogo.mudarModoAgressivo();
		if (!(controleJogo instanceof JogoCliente)) {
			if (modo) {
				agressivo.setText(Lang.msg("134"));
			} else {
				agressivo.setText(Lang.msg("141"));
			}
		}

	}

	public Piloto obterPilotoSecionadoTabela(Piloto pilotoSelecionado) {
		if (painelPosicoes == null) {
			return null;
		}
		Piloto novoSel = painelPosicoes
				.obterPilotoSecionadoTabela(pilotoSelecionado);
		controleJogo.selecionouPiloto(novoSel);
		return novoSel;
	}

	private void atualizarImgClima(Clima clima) {
		if (Clima.SOL.equals(clima.getClima())) {
			imgClima.setIcon(iconSol);
			if (controleJogo.getCircuito() != null
					&& controleJogo.getCircuito().isNoite()) {
				imgClima.setIcon(iconLua);
			}
		}
		if (Clima.CHUVA.equals(clima.getClima()))
			imgClima.setIcon(iconChuva);
		if (Clima.NUBLADO.equals(clima.getClima()))
			imgClima.setIcon(iconNublado);

	}

	public void atualizaPainel() {
		if (controleJogo == null) {
			return;
		}

		Piloto pilotoSelecionado = controleJogo.getPilotoSelecionado();
		atualizaPainelGraficoPilotoSelecionado(pilotoSelecionado);
		atualizaInfoAdicional(pilotoSelecionado);
		atualizarImgClima(new Clima(controleJogo.getClima()));
		if (pilotoSelecionado == null) {
			List l = controleJogo.getCircuito().getPistaFull();
			No n = (No) l.get(0);
			painelCircuito.centralizarPonto(n.getPoint());
		} else {
			painelCircuito.centralizarPonto(pilotoSelecionado.getNoAtual()
					.getPoint());
		}
	}

	private void atualizaInfoAdicional(Piloto pilotoSelecionado) {
		Volta voltaCorrida = controleJogo.obterMelhorVolta();
		if (voltaCorrida != null) {
			List piltos = controleJogo.getPilotos();
			Piloto piloto = null;
			for (Iterator iter = piltos.iterator(); iter.hasNext();) {
				Piloto element = (Piloto) iter.next();
				if (element.getId() == voltaCorrida.getPiloto()) {
					piloto = element;
					break;
				}

			}
			String text = Lang.msg("142",
					new Object[] { voltaCorrida.obterTempoVoltaFormatado() })
					+ (piloto != null ? piloto.getNome() + " - "
							+ piloto.getCarro().getNome() : "")
					+ " "
					+ controleJogo.getNumVoltaAtual()
					+ "/"
					+ controleJogo.totalVoltasCorrida() + " ";

			text += Lang.msg("245") + " : " + controleJogo.getNivelCorrida();
			if (controleJogo.isSemReabastacimento()) {
				text += Lang.msg("304");
			}
			if (controleJogo.isSemTrocaPneu()) {
				text += Lang.msg("305");
			}
			if (controleJogo.isSafetyCarNaPista()) {
				text += Lang.msg("145");
			}
			if (controleJogo.isKers()) {
				text += " Kers ";
			}
			if (controleJogo.isDrs()) {
				text += " DRS ";
			}
			infoCorrida.setText(text);
		}

		if (pilotoSelecionado != null) {

			String plider = "";

			String infoBox = pilotoSelecionado.getCarro().getTipoPneu() + " "
					+ pilotoSelecionado.getCarro().getAsa() + Lang.msg("146")
					+ pilotoSelecionado.getQtdeParadasBox() + " "
					+ (pilotoSelecionado.isBox() ? Lang.msg("147") : "");

			if (pilotoSelecionado.getPosicao() == 1) {
				plider = Lang.msg("148");
			} else {
				controleJogo.calculaSegundosParaLider(pilotoSelecionado);
				plider = pilotoSelecionado.getSegundosParaLider();
			}

			String text = Lang.msg("149") + plider;

			if ((pilotoSelecionado.getNumeroVolta() > 0)) {
				if (pilotoSelecionado.getUltimaVolta() != null) {
					text += (Lang.msg("150") + pilotoSelecionado
							.getUltimaVolta().obterTempoVoltaFormatado());
				}

				Volta voltaPiloto = controleJogo
						.obterMelhorVolta(pilotoSelecionado);

				if (voltaPiloto != null) {
					text += (Lang.msg("151") + voltaPiloto
							.obterTempoVoltaFormatado());
				}
			}
			infoPiloto.setText(text + " " + infoBox);
		}
	}

	private void atualizaPainelGraficoPilotoSelecionado(Piloto pilotoSelecionado) {
		if (pilotoSelecionado == null) {
			return;
		}
		int pneus = pilotoSelecionado.getCarro().porcentagemDesgastePeneus();
		pneuBar.setValue(pneus);
		mudaCorBarra(pneus, pneuBar);
		int porcentComb = pilotoSelecionado.getCarro().porcentagemCombustivel();
		combustivelBar.setValue(porcentComb);
		mudaCorBarra(porcentComb, combustivelBar);
		int motor = pilotoSelecionado.getCarro().porcentagemDesgasteMotor();
		motorBar.setValue(motor);
		mudaCorBarra(motor, motorBar);
		pilotoBar.setValue(pilotoSelecionado.getStress());
	}

	private void mudaCorBarra(int porcent, JProgressBar bar) {
		if (corPadraoBarra == null) {
			corPadraoBarra = bar.getForeground();
		}
		if (porcent < 40 && porcent > 25) {
			bar.setForeground(PainelCircuito.yel);
		} else if (porcent <= 25) {
			bar.setForeground(PainelCircuito.red);
		} else {
			bar.setForeground(corPadraoBarra);
		}
	}

	public JSlider getSpinnerDificuldadeUltrapassagem() {
		return spinnerDificuldadeUltrapassagem;
	}

	public JComboBox getComboBoxCircuito() {
		return comboBoxCircuito;
	}

	public JSlider getSpinnerTempoCiclo() {
		return sliderTempoCiclo;
	}

	public JComboBox getBoxPilotoSelecionado() {
		return boxPilotoSelecionado;
	}

	public JComboBox getBoxPneuInicial() {
		return boxPneuInicial;
	}

	public JComboBox getComboBoxClimaInicial() {
		return comboBoxClimaInicial;
	}

	public JComboBox getComboBoxNivelCorrida() {
		return comboBoxNivelCorrida;
	}

	public JProgressBar getCombustivelBar() {
		return combustivelBar;
	}

	public InterfaceJogo getControleJogo() {
		return controleJogo;
	}

	public JLabel getImgClima() {
		return imgClima;
	}

	public JLabel getInfoAdicionaLinha1() {
		return infoAdicionaLinha1;
	}

	public JEditorPane getInfoTextual() {
		return infoTextual;
	}

	public JPanel getPainelInfGraf() {
		return painelInfGraf;
	}

	public JPanel getPainelInfText() {
		return painelInfText;
	}

	public PainelTabelaPosicoes getPainelPosicoes() {
		return painelPosicoes;
	}

	public JPanel getPanelControle() {
		return panelControleBox;
	}

	public JSpinner getSpinnerSkillPadraoPilotos() {
		return spinnerSkillPadraoPilotos;
	}

	public JSpinner getSpinnerPotenciaPadraoCarros() {
		return spinnerPotenciaPadraoCarros;
	}

	public JSpinner getSpinnerQtdeMinutosQualificacao() {
		return spinnerQtdeMinutosQualificacao;
	}

	public JProgressBar getPneuBar() {
		return pneuBar;
	}

	public JSlider getSpinnerCombustivelInicial() {
		return spinnerCombustivel;
	}

	public JSpinner getSpinnerQtdeVoltas() {
		return spinnerQtdeVoltas;
	}

	public PainelTabelaResultadoFinal exibirResultadoFinal() {
		PainelTabelaResultadoFinal resultadoFinal = new PainelTabelaResultadoFinal(
				controleJogo.getPilotos(), controleJogo.getMainFrame()
						.isModoApplet());
		this.resultadoFinal = resultadoFinal;
		return resultadoFinal;

	}

	public PainelTabelaResultadoFinal getResultadoFinal() {
		return resultadoFinal;
	}

	public void apagarLuz() {
		painelCircuito.apagarLuz();
		painelCircuito.mouseZoom = 0.5;
		atualizaPainel();
	}

	public JTextField getNomeJogador() {
		return nomeJogador;
	}

	public void informaMudancaClima() {
		clima = new ThreadMudancaClima(painelCircuito);
		clima.start();

	}

	private void gerarPainelInfoText() {
		painelInfText = new JPanel(new BorderLayout());
		infoTextual = new JEditorPane("text/html", "");
		infoTextual.setEditable(false);
		bufferTextual = new ArrayList();
		scrollPaneTextual = new JScrollPane(infoTextual) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(scrollPaneTextual.getWidth(), 110);
			}
		};
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		panel.add(scrollPaneTextual, BorderLayout.CENTER);
		// scrollPaneTextual.setBounds(0, 0, larguraFrame
		// - painelPosicoes.getLarguraPainel() - 30, 200);
		painelInfText.add(panel, BorderLayout.CENTER);
		infoText.setLayout(new GridLayout(1, 1));
		infoCorrida = new JLabel(Lang.msg("213"));
		infoPiloto = new JLabel(Lang.msg("214"));
		infoText.add(infoCorrida);
		painelInfText.add(infoText, BorderLayout.NORTH);
	}

	private void gerarPainetInfoGraf() {
		JPanel panelCol1 = new JPanel();
		panelCol1.setLayout(new BorderLayout());

		JPanel panelCol2 = new JPanel();
		panelCol2.setLayout(new BorderLayout());

		JPanel panelCol3 = new JPanel();
		panelCol3.setLayout(new BorderLayout());

		JPanel panelCol4 = new JPanel();
		panelCol4.setLayout(new BorderLayout());

		JLabel combustivel = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("215");
			}
		};
		panelCol1.add(combustivel, BorderLayout.NORTH);
		combustivelBar = new JProgressBar(JProgressBar.VERTICAL);
		combustivelBar.setStringPainted(true);
		panelCol1.add(combustivelBar, BorderLayout.CENTER);

		JLabel pneu = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("216");
			}
		};
		panelCol2.add(pneu, BorderLayout.NORTH);
		pneuBar = new JProgressBar(JProgressBar.VERTICAL);
		pneuBar.setStringPainted(true);
		panelCol2.add(pneuBar, BorderLayout.CENTER);
		motorBar = new JProgressBar(JProgressBar.VERTICAL);
		motorBar.setStringPainted(true);
		JLabel motoLabel = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("217");
			}
		};
		panelCol3.add(motoLabel, BorderLayout.NORTH);
		panelCol3.add(motorBar, BorderLayout.CENTER);

		pilotoBar = new JProgressBar(JProgressBar.VERTICAL);
		pilotoBar.setStringPainted(true);
		JLabel pilotoLabel = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("153");
			}
		};

		panelCol4.add(pilotoLabel, BorderLayout.NORTH);
		panelCol4.add(pilotoBar, BorderLayout.CENTER);

		painelInfGraf = new JPanel(new GridLayout(1, 4));
		painelInfGraf.add(panelCol1);
		painelInfGraf.add(panelCol2);
		painelInfGraf.add(panelCol3);
		painelInfGraf.add(panelCol4);
	}

	private void gerarLayout() {
		controleJogo.getMainFrame().getContentPane().removeAll();
		controleJogo.getMainFrame().getContentPane()
				.setLayout(new BorderLayout());

		JPanel southPanel = new JPanel(new BorderLayout());
		JPanel controles = new JPanel(new GridLayout(1, 2));
		controles.add(panelControleBox);
		controles.add(panelControlePos);

		southPanel.add(controles, BorderLayout.EAST);
		southPanel.add(painelInfText, BorderLayout.CENTER);
		centerPanel.setLayout(new BorderLayout());
		centerPanel.add(southPanel, BorderLayout.SOUTH);
		centerPanel.add(scrollPane, BorderLayout.CENTER);

		eastPanel.setLayout(new BorderLayout());

		if (controleJogo instanceof JogoCliente) {
			JPanel jPanel = new JPanel(new BorderLayout());
			jPanel.add(driverThru, BorderLayout.NORTH);
			jPanel.add(painelInfGraf, BorderLayout.CENTER);
			eastPanel.add(jPanel, BorderLayout.CENTER);
		} else {
			eastPanel.add(painelInfGraf, BorderLayout.CENTER);
		}
		eastPanel.add(painelPosicoes, BorderLayout.NORTH);

		controleJogo.getMainFrame().getContentPane()
				.add(centerPanel, BorderLayout.CENTER);
		controleJogo.getMainFrame().getContentPane()
				.add(eastPanel, BorderLayout.EAST);
		centerPanel.revalidate();
		if (controleJogo.getMainFrame().isModoApplet()) {
			controleJogo.getMainFrame().setSize(larguraFrame, alturaFrame);
		} else {
			controleJogo.getMainFrame().setSize(larguraFrame, alturaFrame);
		}

	}

	private void gerarPainelComandos() {

		box = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("135");
			}
		};
		progBox = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("262");
			}
		};
		modoPiloto = new JComboBox();
		modoPiloto.addItem(new LangVO("008"));
		modoPiloto.addItem(new LangVO(Piloto.AGRESSIVO));
		modoPiloto.addItem(new LangVO(Piloto.NORMAL));
		modoPiloto.addItem(new LangVO(Piloto.LENTO));
		giro = new JComboBox();
		giro.addItem(new LangVO("012"));
		giro.addItem(new LangVO(Carro.GIRO_NOR));
		giro.addItem(new LangVO(Carro.GIRO_MAX));
		giro.addItem(new LangVO(Carro.GIRO_MIN));

		comboBoxTipoPneu = new JComboBox();
		comboBoxTipoPneu.addItem(new LangVO(Carro.TIPO_PNEU_MOLE));
		comboBoxTipoPneu.addItem(new LangVO(Carro.TIPO_PNEU_DURO));
		comboBoxTipoPneu.addItem(new LangVO(Carro.TIPO_PNEU_CHUVA));

		sliderPercentCombust = new JSlider(0, 100);
		sliderPercentCombust.setPaintTicks(true);
		sliderPercentCombust.setMajorTickSpacing(10);
		sliderPercentCombust.setValue(new Integer(50));

		comboBoxAsa = new JComboBox();
		comboBoxAsa.addItem(new LangVO(Carro.ASA_NORMAL));
		comboBoxAsa.addItem(new LangVO(Carro.MAIS_ASA));
		comboBoxAsa.addItem(new LangVO(Carro.MENOS_ASA));

		panelControleBox = new JPanel();
		panelControleBox.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("136");
			}
		});
		GridLayout gridLayout = new GridLayout(5, 1) {
			public Dimension preferredLayoutSize(Container parent) {
				return new Dimension(150, 140);
			}
		};

		panelControleBox.setLayout(gridLayout);
		panelControleBox.add(box);
		panelControleBox.add(comboBoxTipoPneu);
		panelControleBox.add(comboBoxAsa);
		panelControleBox.add(new JLabel() {
			public String getText() {
				return Lang.msg("083");
			};
		});
		panelControleBox.add(sliderPercentCombust);

		panelControlePos = new JPanel(new GridLayout(4, 1));
		panelControlePos.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("menuCorrida");
			}
		});
		autoPos = new JButton("") {
			@Override
			public String getText() {
				return Lang.msg("autoPos");
			}
		};
		agressivo = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("pilotagem");
			}
		};
		alternaPiloto = new JButton("") {
			@Override
			public String getText() {
				return Lang.msg("alternaPiloto");
			}
		};
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		driverThru = new JButton("driverThru") {
			public String getText() {
				return Lang.msg("driverThru");
			};
		};

		f1 = new JButton("F1") {
			public String getToolTipText() {
				return Lang.msg("F1hlp");
			};

		};
		toolTipManager.registerComponent(f1);
		f2 = new JButton("F2") {
			public String getToolTipText() {
				return Lang.msg("F2hlp");
			};
		};
		toolTipManager.registerComponent(f2);
		f3 = new JButton("F3") {
			public String getToolTipText() {
				return Lang.msg("F3hlp");
			};
		};
		toolTipManager.registerComponent(f3);
		f5 = new JButton("F5") {
			public String getToolTipText() {
				return Lang.msg("F5hlp");
			};
		};
		toolTipManager.registerComponent(f5);
		f6 = new JButton("F6") {
			public String getToolTipText() {
				return Lang.msg("F6hlp");
			};
		};
		toolTipManager.registerComponent(f6);
		f7 = new JButton("F7") {
			public String getToolTipText() {
				return Lang.msg("F7hlp");
			};
		};
		toolTipManager.registerComponent(f7);
		drsBtn = new JButton("DRS (D)") {
			public String getToolTipText() {
				return Lang.msg("F1");
			};
		};
		kersBtn = new JButton("KERS (K)") {
			public String getToolTipText() {
				return Lang.msg("F1");
			};
		};

		JPanel motorPanel = new JPanel(new GridLayout(1, 3));
		motorPanel.add(f1);
		motorPanel.add(f2);
		motorPanel.add(f3);
		JPanel pilotoPanel = new JPanel(new GridLayout(1, 3));
		pilotoPanel.add(f5);
		pilotoPanel.add(f6);
		pilotoPanel.add(f7);

		JPanel optsPanel = new JPanel(new GridLayout(1, 2));
		optsPanel.add(drsBtn);
		optsPanel.add(kersBtn);

		panelControlePos.add(motorPanel);
		panelControlePos.add(pilotoPanel);
		panelControlePos.add(optsPanel);
		panelControlePos.add(autoPos);

		addiconarListenerComandos(comboBoxTipoPneu, sliderPercentCombust);
	}

	private void gerarPainelPosicoes() throws IOException {
		painelPosicoes = new PainelTabelaPosicoes(controleJogo);
	}

	private void gerarPainelJogoSingle(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(10, 2, 5, 5));
		JLabel label = new JLabel() {

			public String getText() {
				return Lang.msg("110",
						new String[] { String.valueOf(Constantes.MIN_VOLTAS),
								String.valueOf(Constantes.MAX_VOLTAS) });
			}
		};
		painelInicio.add(label);
		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
		if (!ControleJogoLocal.VALENDO) {
			spinnerQtdeVoltas.setValue(new Integer(30));
		}

		painelInicio.add(spinnerQtdeVoltas);

		painelInicio.add(new JLabel() {

			public String getText() {
				return Lang.msg("111");
			}
		});
		nomeJogador = new JTextField();
		painelInicio.add(nomeJogador);

		boxPilotoSelecionado = new JComboBox();
		boxPilotoSelecionado.addItem(Lang.msg("119"));

		for (Iterator iter = controleJogo.getPilotos().iterator(); iter
				.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			boxPilotoSelecionado.addItem(piloto);
		}

		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("120");
			}
		});
		painelInicio.add(boxPilotoSelecionado);
		comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("212");
			}
		});
		painelInicio.add(comboBoxNivelCorrida);

		comboBoxClimaInicial = new JComboBox();
		comboBoxClimaInicial.addItem(new Clima(Clima.SOL));
		comboBoxClimaInicial.addItem(new Clima(Clima.NUBLADO));
		comboBoxClimaInicial.addItem(new Clima(Clima.CHUVA));
		comboBoxClimaInicial.addItem(new Clima(Clima.ALEATORIO));

		for (int i = 0; i < comboBoxClimaInicial.getItemCount(); i++) {
			Clima clima = (Clima) comboBoxClimaInicial.getItemAt(i);

			if (clima.getClima().equals(controleJogo.getClima())) {
				comboBoxClimaInicial.setSelectedIndex(i);
			}
		}

		painelInicio.add(new JLabel() {
			public String getText() {

				return Lang.msg("123");
			}
		});
		painelInicio.add(comboBoxClimaInicial);

		JLabel tipoPneu = new JLabel(Lang.msg("009"));
		boxPneuInicial = new JComboBox();
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));

		JLabel tipoAsa = new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("010");
			}
		};
		comboBoxAsaInicial = new JComboBox();
		comboBoxAsaInicial.addItem(Lang.msg(Carro.ASA_NORMAL));
		comboBoxAsaInicial.addItem(Lang.msg(Carro.MAIS_ASA));
		comboBoxAsaInicial.addItem(Lang.msg(Carro.MENOS_ASA));

		JLabel qtdeComustivel = new JLabel() {
			public String getText() {
				return Lang.msg("011");
			}
		};
		spinnerCombustivel = new JSlider(0, 100);
		spinnerCombustivel.setValue(new Integer(50));
		spinnerCombustivel.setPaintLabels(true);
		painelInicio.add(tipoPneu);
		painelInicio.add(boxPneuInicial);
		painelInicio.add(tipoAsa);
		painelInicio.add(comboBoxAsaInicial);
		painelInicio.add(qtdeComustivel);
		painelInicio.add(spinnerCombustivel);
		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("124");
			}
		});
		spinnerDificuldadeUltrapassagem = new JSlider(000, 500);
		spinnerDificuldadeUltrapassagem.setValue(new Integer(Util.intervalo(
				000, 500)));
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(000), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("FACIL");
			}
		});
		labelTable.put(new Integer(500), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("DIFICIL");
			}
		});
		spinnerDificuldadeUltrapassagem.setLabelTable(labelTable);
		spinnerDificuldadeUltrapassagem.setPaintLabels(true);
		painelInicio.add(spinnerDificuldadeUltrapassagem);
		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		sliderTempoCiclo = new JSlider(Constantes.MIN_CICLO,
				Constantes.MAX_CICLO);
		sliderTempoCiclo.setValue(new Integer(Util.intervalo(
				Constantes.MIN_CICLO, Constantes.MAX_CICLO)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(Constantes.MIN_CICLO), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("RAPIDOS");
			}
		});
		labelTable.put(new Integer(Constantes.MAX_CICLO), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("LENTOS");
			}
		});
		sliderTempoCiclo.setLabelTable(labelTable);
		sliderTempoCiclo.setPaintLabels(true);
		painelInicio.add(sliderTempoCiclo);

		// painelInicio.add(new JLabel() {
		// public String getText() {
		// return Lang.msg("112");
		// }
		// });
		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		// painelInicio.add(spinnerSkillPadraoPilotos);

		// painelInicio.add(new JLabel() {
		// public String getText() {
		// return Lang.msg("113");
		// }
		// });
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));
		// painelInicio.add(spinnerPotenciaPadraoCarros);

	}

	public List getListaPilotosCombo() {
		return listaPilotosCombo;
	}

	public List getListaCarrosCombo() {
		return listaCarrosCombo;
	}

	private void gerarPainelJogoMulti(JPanel incialPanel) {
		final CarregadorRecursos carregadorRecursos = new CarregadorRecursos(
				true);
		final Map circuitosPilotos = carregadorRecursos
				.carregarTemporadasPilotos();
		comboBoxTemporadas = new JComboBox(carregadorRecursos.getVectorTemps());

		final DefaultListModel defaultListModelPilotosSelecionados = new DefaultListModel();
		listPilotosSelecionados = new JList(defaultListModelPilotosSelecionados);
		listaPilotosCombo = new ArrayList();
		comboBoxTemporadas.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				listaPilotosCombo.clear();
				String temporarada = (String) carregadorRecursos
						.getTemporadas().get(arg0.getItem());
				listaPilotosCombo.addAll((Collection) circuitosPilotos
						.get(temporarada));
				Collections.sort(listaPilotosCombo, new Comparator() {

					@Override
					public int compare(Object o1, Object o2) {
						Piloto p1 = (Piloto) o1;
						Piloto p2 = (Piloto) o2;
						return p1.getCarro().getNome()
								.compareTo(p2.getCarro().getNome());
					}

				});
				defaultListModelPilotosSelecionados.clear();
				for (Iterator iterator = listaPilotosCombo.iterator(); iterator
						.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					defaultListModelPilotosSelecionados.addElement(piloto);
				}
				try {
					listaCarrosCombo = carregadorRecursos
							.carregarListaCarros(temporarada);
				} catch (IOException e) {
					Logger.logarExept(e);
				}

			}
		});
		comboBoxTemporadas.setSelectedIndex(1);
		comboBoxTemporadas.setSelectedIndex(0);

		final JPanel pilotoPanel = new JPanel(new BorderLayout());

		pilotoPanel.setBorder(new TitledBorder(Lang.msg("274")));

		pilotoPanel.add(comboBoxTemporadas, BorderLayout.NORTH);
		pilotoPanel.add(new JScrollPane(listPilotosSelecionados) {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(210, 270);
			}
		}, BorderLayout.CENTER);

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(7, 2, 5, 5));
		JLabel label = new JLabel() {

			public String getText() {
				return Lang.msg("110",
						new String[] { String.valueOf(Constantes.MIN_VOLTAS),
								String.valueOf(Constantes.MAX_VOLTAS) });
			}
		};
		grid.add(label);
		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(12));
		if (!ControleJogoLocal.VALENDO) {
			spinnerQtdeVoltas.setValue(new Integer(30));
		}

		grid.add(spinnerQtdeVoltas);

		boxPilotoSelecionado = new JComboBox();
		boxPilotoSelecionado.addItem(Lang.msg("119"));

		comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("212");
			}
		});
		grid.add(comboBoxNivelCorrida);

		comboBoxClimaInicial = new JComboBox();
		comboBoxClimaInicial.addItem(new Clima(Clima.SOL));
		comboBoxClimaInicial.addItem(new Clima(Clima.NUBLADO));
		comboBoxClimaInicial.addItem(new Clima(Clima.CHUVA));
		comboBoxClimaInicial.addItem(new Clima(Clima.ALEATORIO));

		for (int i = 0; i < comboBoxClimaInicial.getItemCount(); i++) {
			Clima clima = (Clima) comboBoxClimaInicial.getItemAt(i);

			if (clima.getClima().equals(controleJogo.getClima())) {
				comboBoxClimaInicial.setSelectedIndex(i);
			}
		}

		grid.add(new JLabel() {
			public String getText() {

				return Lang.msg("123");
			}
		});
		grid.add(comboBoxClimaInicial);

		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("124");
			}
		});
		spinnerDificuldadeUltrapassagem = new JSlider(000, 500);
		spinnerDificuldadeUltrapassagem.setValue(new Integer(Util.intervalo(
				000, 500)));
		Hashtable labelTable = new Hashtable();
		labelTable.put(new Integer(000), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("FACIL");
			}
		});
		labelTable.put(new Integer(500), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("DIFICIL");
			}
		});
		spinnerDificuldadeUltrapassagem.setLabelTable(labelTable);
		spinnerDificuldadeUltrapassagem.setPaintLabels(true);
		grid.add(spinnerDificuldadeUltrapassagem);
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		sliderTempoCiclo = new JSlider(Constantes.MIN_CICLO,
				Constantes.MAX_CICLO);
		sliderTempoCiclo.setValue(new Integer(Util.intervalo(
				Constantes.MIN_CICLO, Constantes.MAX_CICLO)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(Constantes.MIN_CICLO), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("RAPIDOS");
			}
		});
		labelTable.put(new Integer(Constantes.MAX_CICLO), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("LENTOS");
			}
		});
		sliderTempoCiclo.setLabelTable(labelTable);
		sliderTempoCiclo.setPaintLabels(true);
		grid.add(sliderTempoCiclo);

		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));

		JPanel p1 = new JPanel(new GridLayout(1, 2));

		p1.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		semReabastacimento = new JCheckBox();
		p1.add(semReabastacimento);
		grid.add(p1);

		JPanel p2 = new JPanel(new GridLayout(1, 2));

		p2.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		semTrocaPneu = new JCheckBox();
		p2.add(semTrocaPneu);
		grid.add(p2);

		JPanel p3 = new JPanel(new GridLayout(1, 2));
		p3.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("kers");
			}
		});
		kers = new JCheckBox();
		p3.add(kers);
		grid.add(p3);

		JPanel p4 = new JPanel(new GridLayout(1, 2));
		p4.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("drs");
			}
		});
		drs = new JCheckBox();
		p4.add(drs);
		grid.add(p4);

		grid.setBorder(new TitledBorder(Lang.msg("273")));

		incialPanel.add(grid, BorderLayout.CENTER);
		incialPanel.add(gerarSeletorCircuito(), BorderLayout.NORTH);
		incialPanel.add(pilotoPanel, BorderLayout.EAST);

	}

	private Component gerarSeletorCircuito() {
		JPanel grid = new JPanel();
		comboBoxCircuito = new JComboBox();
		List circuitosList = new ArrayList();
		for (Iterator iter = controleJogo.getCircuitos().keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			circuitosList.add(key);
		}
		Collections.shuffle(circuitosList);
		for (Iterator iterator = circuitosList.iterator(); iterator.hasNext();) {
			String object = (String) iterator.next();
			comboBoxCircuito.addItem(object);
		}
		grid.add(new JLabel() {
			public String getText() {
				return Lang.msg("121");
			}
		});
		grid.add(comboBoxCircuito);
		final JLabel circuitosLabel = new JLabel() {
			public Dimension getPreferredSize() {
				return new Dimension(400, 200);
			}
		};
		comboBoxCircuito.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (arg0.getStateChange() == ItemEvent.SELECTED) {
					desenhaMiniCircuito(circuitosLabel);
				}
			}
		});

		JPanel retPanel = new JPanel(new BorderLayout());
		retPanel.add(grid, BorderLayout.WEST);
		retPanel.add(circuitosLabel, BorderLayout.CENTER);
		desenhaMiniCircuito(circuitosLabel);
		return retPanel;
	}

	protected void desenhaMiniCircuito(JLabel circuitosLabel) {
		BufferedImage bufferedImage = new BufferedImage(400, 200,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) bufferedImage.getGraphics();

		setarHints(g2d);
		g2d.setStroke(new BasicStroke(3.0f));
		g2d.setColor(Color.BLACK);
		String circuitoStr = (String) controleJogo.getCircuitos().get(
				comboBoxCircuito.getSelectedItem());
		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(false);
		ObjectInputStream ois;
		Circuito circuito = null;
		try {
			ois = new ObjectInputStream(carregadorRecursos.getClass()
					.getResourceAsStream(circuitoStr));
			circuito = (Circuito) ois.readObject();
			circuito.vetorizarPista();
		} catch (Exception e) {
			e.printStackTrace();
		}

		List pista = circuito.getPista();
		ArrayList pistaMinimizada = new ArrayList();
		double doubleMulti = 25;
		Map map = new HashMap();
		for (Iterator iterator = pista.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (!pistaMinimizada.contains(p)) {
				map.put(p, no);
				pistaMinimizada.add(p);
			}

		}
		Point o = new Point(10, 10);
		Point oldP = null;
		for (Iterator iterator = pistaMinimizada.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				No no = (No) map.get(oldP);
				if (no.verificaCruvaBaixa()) {
					g2d.setColor(Color.red);
				} else if (no.verificaCruvaAlta()) {
					g2d.setColor(Color.orange);
				} else if (no.verificaRetaOuLargada()) {
					g2d.setColor(new Color(0, 200, 0));
				}
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}
		Point p0 = (Point) pistaMinimizada.get(0);
		g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p0.x, o.y + p0.y);

		ArrayList boxMinimizado = new ArrayList();
		List box = circuito.getBox();
		for (Iterator iterator = box.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			Point p = new Point(no.getX(), no.getY());
			p.x /= doubleMulti;
			p.y /= doubleMulti;
			if (!boxMinimizado.contains(p))
				boxMinimizado.add(p);
		}
		g2d.setStroke(new BasicStroke(2.0f));
		oldP = null;
		g2d.setColor(Color.lightGray);
		for (Iterator iterator = boxMinimizado.iterator(); iterator.hasNext();) {
			Point p = (Point) iterator.next();
			if (oldP != null) {
				g2d.drawLine(o.x + oldP.x, o.y + oldP.y, o.x + p.x, o.y + p.y);
			}
			oldP = p;
		}

		circuitosLabel.setIcon(new ImageIcon(bufferedImage));

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

	public JCheckBox getSemTrocaPneu() {
		return semTrocaPneu;
	}

	public JCheckBox getSemReabastacimento() {
		return semReabastacimento;
	}

	public JCheckBox getKers() {
		return kers;
	}

	public JCheckBox getDrs() {
		return drs;
	}

	public boolean iniciarJogoSingle() {
		JPanel painelInicio = new JPanel();
		gerarPainelJogoSingle(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(12));
		int ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
				painelInicio, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}

		Integer qtdeVoltas = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVoltas < 12) {
			spinnerQtdeVoltas.setValue(new Integer(12));
		}
		Integer combustivelInicial = (Integer) spinnerCombustivel.getValue();
		if (combustivelInicial <= 0) {
			if (semReabastacimento.isSelected()) {
				spinnerCombustivel.setValue(new Integer(1));
			} else {
				spinnerCombustivel.setValue(new Integer(20));
			}

		}

		Object selec = listPilotosSelecionados.getSelectedValue();
		controleJogo.setTemporada("t" + comboBoxTemporadas.getSelectedItem());
		if (!ControleJogoLocal.VALENDO) {
			selec = "";
		}

		if (selec instanceof Piloto) {
			controleJogo.efetuarSelecaoPilotoJogador(selec,
					Lang.key(boxPneuInicial.getSelectedItem().toString()),
					spinnerCombustivel.getValue(), nomeJogador.getText(),
					Lang.key((String) comboBoxAsaInicial.getSelectedItem()));
		}
		return true;
	}

	public boolean iniciarJogoMulti(Campeonato campeonato) {
		JPanel painelInicio = new JPanel(new BorderLayout());
		gerarPainelJogoMulti(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(12));
		if (campeonato != null) {

			semReabastacimento.setSelected(campeonato.isSemReabasteciemnto());
			semReabastacimento.setEnabled(false);

			semTrocaPneu.setSelected(campeonato.isSemTrocaPneus());
			semTrocaPneu.setEnabled(false);

			kers.setSelected(campeonato.isKers());
			kers.setEnabled(false);

			drs.setSelected(campeonato.isDrs());
			drs.setEnabled(false);

			comboBoxTemporadas.setSelectedItem(campeonato.getTemporada());
			comboBoxTemporadas.setEnabled(false);

			spinnerQtdeVoltas.setValue(campeonato.getQtdeVoltas());
			spinnerQtdeVoltas.setEnabled(false);
			comboBoxNivelCorrida
					.setSelectedItem(Lang.msg(campeonato.getNivel()));
			comboBoxNivelCorrida.setEnabled(false);
			List indices = new ArrayList();
			DefaultListModel defaultListModel = (DefaultListModel) listPilotosSelecionados
					.getModel();

			ArrayList mudouCarro = new ArrayList();
			for (int i = 0; i < defaultListModel.getSize(); i++) {
				Piloto piloto = (Piloto) defaultListModel.get(i);
				if (Util.isNullOrEmpty(campeonato.getNomePiloto())) {
					if (campeonato.getPilotos().contains(piloto.toString())) {
						indices.add(new Integer(i));
					}
				} else {
					String carro = campeonato.getPilotosEquipesCampeonato()
							.get(piloto.getNome());
					if (Util.isNullOrEmpty(carro)) {
						piloto.setNome(campeonato.getNomePiloto());
						piloto.setHabilidade(campeonato.getPtsPiloto());
						piloto.setJogadorHumano(true);
						mudouCarro.add(piloto);
					} else if (!carro.equals(piloto.getCarro().getNome())) {
						mudouCarro.add(piloto);
					}
				}
			}
			if (!Util.isNullOrEmpty(campeonato.getNomePiloto())) {
				for (Iterator iterator = mudouCarro.iterator(); iterator
						.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					String carro = campeonato.getPilotosEquipesCampeonato()
							.get(piloto.getNome());
					for (int i = 0; i < defaultListModel.getSize(); i++) {
						Piloto p = (Piloto) defaultListModel.get(i);
						if (p.getCarro().getNome().equals(carro)) {
							piloto.setCarro(CarregadorRecursos.criarCopiaCarro(
									p.getCarro(), piloto));
						}
					}
				}
				for (int i = 0; i < defaultListModel.getSize(); i++) {
					Piloto piloto = (Piloto) defaultListModel.get(i);
					if (piloto.isJogadorHumano()) {
						indices.add(new Integer(i));
					}
				}
				controleJogo.verificaDesafioCampeonatoPiloto();
			}
			int[] inds = new int[indices.size()];
			for (int i = 0; i < inds.length; i++) {
				inds[i] = ((Integer) indices.get(i)).intValue();
			}
			listPilotosSelecionados.setSelectedIndices(inds);
			listPilotosSelecionados.setEnabled(false);

			comboBoxCircuito.setSelectedItem(campeonato.getCircuitoVez());
			comboBoxCircuito.setEnabled(false);
			spinnerSkillPadraoPilotos.setEnabled(false);
			spinnerPotenciaPadraoCarros.setEnabled(false);
			spinnerDificuldadeUltrapassagem.setEnabled(false);
			int val = 1 + (int) (Math.random() * 3);

			Clima climaTmp = null;
			switch (val) {
			case 1:
				climaTmp = new Clima(Clima.SOL);

				break;

			case 2:
				climaTmp = new Clima(Clima.NUBLADO);

				break;

			case 3:
				climaTmp = new Clima(Clima.CHUVA);

				break;

			default:
				break;
			}
			comboBoxClimaInicial.setSelectedItem(climaTmp);
			comboBoxClimaInicial.setEnabled(false);

		}
		int ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
				painelInicio, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}
		Object[] selec = listPilotosSelecionados.getSelectedValues();

		for (int i = 0; i < selec.length; i++) {
			JPanel painelJogSel = new JPanel(new GridLayout(5, 2));
			painelJogSel.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("123");
				}
			});
			Clima clima = (Clima) comboBoxClimaInicial.getSelectedItem();
			ImageIcon icon = new ImageIcon(
					CarregadorRecursos.carregarImagem("clima/"
							+ clima.getClima()));
			painelJogSel.add(new JLabel(icon));

			JTextField nomeJogador = new JTextField();
			painelJogSel.add(new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("111");
				}
			});
			painelJogSel.add(nomeJogador);
			JLabel tipoPneu = new JLabel(Lang.msg("009"));
			boxPneuInicial = new JComboBox();
			boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
			boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
			boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));

			JLabel tipoAsa = new JLabel() {
				@Override
				public String getText() {
					return Lang.msg("010");
				}
			};
			comboBoxAsaInicial = new JComboBox();
			comboBoxAsaInicial.addItem(Lang.msg(Carro.ASA_NORMAL));
			comboBoxAsaInicial.addItem(Lang.msg(Carro.MAIS_ASA));
			comboBoxAsaInicial.addItem(Lang.msg(Carro.MENOS_ASA));

			JLabel qtdeComustivel = new JLabel() {
				public String getText() {
					return Lang.msg("011");
				}
			};
			spinnerCombustivel = new JSlider(0, 100);
			spinnerCombustivel.setPaintLabels(true);
			Hashtable labelTable = new Hashtable();
			labelTable.put(new Integer(000), new JLabel("") {
				@Override
				public String getText() {
					return Lang.msg("MENOS");
				}
			});
			labelTable.put(new Integer(100), new JLabel("") {
				@Override
				public String getText() {
					return Lang.msg("MAIS");
				}
			});
			spinnerCombustivel.setLabelTable(labelTable);
			spinnerCombustivel.setPaintLabels(true);

			spinnerCombustivel.setValue(new Integer(50));
			painelJogSel.add(tipoPneu);
			painelJogSel.add(boxPneuInicial);
			painelJogSel.add(tipoAsa);
			painelJogSel.add(comboBoxAsaInicial);
			painelJogSel.add(qtdeComustivel);
			painelJogSel.add(spinnerCombustivel);

			JOptionPane.showMessageDialog(controleJogo.getMainFrame(),
					painelJogSel,
					Lang.msg("275", new String[] { selec[i].toString() }),
					JOptionPane.QUESTION_MESSAGE);
			controleJogo.efetuarSelecaoPilotoJogador(selec[i],
					Lang.key(boxPneuInicial.getSelectedItem().toString()),
					spinnerCombustivel.getValue(), nomeJogador.getText(),
					Lang.key((String) comboBoxAsaInicial.getSelectedItem()));

		}
		return true;
	}

	private void gerarPainelJogo(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(13, 2));
		painelInicio.add(new JLabel("Quantidade de Minutos Qualificação :"));
		spinnerQtdeMinutosQualificacao = new JSpinner();
		spinnerQtdeMinutosQualificacao.setValue(new Integer(5));
		painelInicio.add(spinnerQtdeMinutosQualificacao);
	}

	public boolean iniciarJogo() {
		JPanel painelInicio = new JPanel();
		painelInicio.setBorder(new TitledBorder(
				"Modo Completo com Qualificação:"));
		gerarPainelJogoSingle(painelInicio);
		gerarPainelJogo(painelInicio);
		int ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
				painelInicio, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret != JOptionPane.YES_OPTION) {
			return false;
		}

		Integer qtdeVoltas = (Integer) spinnerQtdeVoltas.getValue();
		if (qtdeVoltas < 12) {
			spinnerQtdeVoltas.setValue(new Integer(12));
		}
		Integer combustivelInicial = (Integer) spinnerCombustivel.getValue();
		if (combustivelInicial <= 0) {
			if (semReabastacimento.isSelected()) {
				spinnerCombustivel.setValue(new Integer(1));
			} else {
				spinnerCombustivel.setValue(new Integer(20));
			}

		}

		Object selec = boxPilotoSelecionado.getSelectedItem();

		if (!ControleJogoLocal.VALENDO) {
			selec = "";
		}

		if (selec instanceof Piloto) {
			controleJogo.efetuarSelecaoPilotoJogador(selec,
					Lang.key(boxPneuInicial.getSelectedItem().toString()),
					spinnerCombustivel.getValue(), nomeJogador.getText(),
					Lang.key(comboBoxAsaInicial.getSelectedItem().toString()));
		}

		return true;
	}

	public void desenhaQualificacao() {
		No n = (No) controleJogo.getCircuito().getPistaFull().get(0);
		try {
			painelCircuito.centralizarPontoDireto(n.getPoint());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				No n = (No) controleJogo.getCircuito().getPistaFull().get(0);
				try {
					painelCircuito.centralizarPontoDireto(n.getPoint());
				} catch (Exception e) {
					Logger.logarExept(e);
				}
				infoCorrida.setText(Lang.msg("213"));
				infoPiloto.setText(Lang.msg("214"));
				List pilotos = controleJogo.getPilotos();
				List ptosPilotos = new ArrayList();
				painelCircuito.addMouseListener(new MouseAdapter() {
					public void mouseClicked(MouseEvent e) {
						tempoSleep = 0;
					}

				});
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					Logger.logarExept(e);
				}
				Rectangle limitesViewPort = null;

				limitesViewPort = (Rectangle) painelCircuito.limitesViewPort();

				int iniY1 = 30;
				int iniY2 = 40;
				int midPainel = 0;
				if (limitesViewPort != null)
					midPainel = (limitesViewPort.width / 2);
				else {
					midPainel = 500;
					Logger.logarExept(new Exception("limitesViewPort == null "));
				}
				for (int i = 0; i < pilotos.size(); i++) {
					Piloto piloto = (Piloto) pilotos.get(i);
					if (piloto.getPosicao() % 2 == 0) {
						ptosPilotos.add(new Point(midPainel + 30, iniY2));
						iniY2 += 40;
					} else {
						ptosPilotos.add(new Point(midPainel - 120, iniY1));
						iniY1 += 40;
					}
				}
				Logger.logar("Iniciar Loop desenha Qualy");
				for (int i = 0; i < pilotos.size(); i++) {
					Piloto piloto = (Piloto) pilotos.get(i);
					Point point = (Point) ptosPilotos.get(i);
					int x = limitesViewPort.x + limitesViewPort.width;
					while (x > (point.x + limitesViewPort.x)) {
						Point pd = new Point(x, point.y + limitesViewPort.y);
						painelCircuito.definirDesenhoQualificacao(piloto, pd);
						if (tempoSleep != 0) {
							try {
								painelCircuito.repaint();
							} catch (Exception e) {
								Logger.logarExept(e);
							}
						} else {
							break;
						}
						x -= 3;
						try {
							Thread.sleep(tempoSleep);
						} catch (InterruptedException e) {
							Logger.logarExept(e);
						}
					}
					painelCircuito.getMapDesenharQualificacao().put(piloto,
							point);
				}
				try {
					painelCircuito.repaint();
				} catch (Exception e) {
					Logger.logarExept(e);
				}

				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					Logger.logarExept(e);
				}

				painelCircuito.setDesenhouQualificacao(true);
				Logger.logar("DesenhouQualificacao");
			}
		};
		if (thDesenhaQualificacao != null) {
			thDesenhaQualificacao.interrupt();
		}
		thDesenhaQualificacao = new Thread(runnable);
		thDesenhaQualificacao.run();
	}

	public void atulizaTabelaPosicoes() {
		painelPosicoes.atulizaTabelaPosicoes(controleJogo.getPilotos(),
				controleJogo.getPilotoSelecionado());

	}

	public void adicionarInfoDireto(String string) {
		if (string == null) {
			return;
		}
		try {
			synchronized (bufferTextual) {
				if (string != null && !string.startsWith("<table>"))
					string = Html.cinza(Lang.msg("082")
							+ controleJogo.getNumVoltaAtual() + " ")
							+ string + "<br>";
				if (bufferTextual.size() > 6) {
					boolean contains = false;
					for (int i = bufferTextual.size() - 1; i < bufferTextual
							.size() - 5; i--) {
						if (string.equals(bufferTextual.get(i))) {
							contains = true;
						}
					}
					if (contains) {
						return;
					}
				}

				bufferTextual.add(string);

				StringBuffer buffer = new StringBuffer();
				for (int i = bufferTextual.size() - 1; i >= 0; i--) {
					String texto = Html.sansSerif(bufferTextual.get(i)
							.toString());
					buffer.append(texto);
				}
				final StringReader reader = new StringReader(buffer.toString());

				Runnable doInfo = new Runnable() {
					public void run() {
						try {
							infoTextual.read(reader, "");
						} catch (IOException e) {
							Logger.logarExept(e);
						}
					}
				};
				SwingUtilities.invokeLater(doInfo);

			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public int getTempoSleep() {
		return tempoSleep;
	}

	public void setTempoSleep(int tempoSleep) {
		this.tempoSleep = tempoSleep;
	}

	public JComboBox getComboBoxAsa() {
		return comboBoxAsa;
	}

	public void setComboBoxAsa(JComboBox comboBoxAsa) {
		this.comboBoxAsa = comboBoxAsa;
	}

	public JLabel getInfoCorrida() {
		return infoCorrida;
	}

	public JLabel getInfoPiloto() {
		return infoPiloto;
	}

	public JComboBox getModoPiloto() {
		return modoPiloto;
	}

	public void sincronizarMenuInicioMenuBox(Object tipoPeneuJogador,
			Object combustJogador, Object asaJogador) {
		if (comboBoxTipoPneu != null) {
			LangVO langVO = null;
			if (tipoPeneuJogador instanceof LangVO) {
				langVO = (LangVO) tipoPeneuJogador;
			} else {
				langVO = new LangVO(tipoPeneuJogador.toString());
			}
			comboBoxTipoPneu.setSelectedItem(langVO);
		}
		if (asaJogador != null) {
			LangVO langVO = null;
			if (asaJogador instanceof LangVO) {
				langVO = (LangVO) asaJogador;
			} else {
				langVO = new LangVO(asaJogador.toString());
			}
			comboBoxAsa.setSelectedItem(langVO);
		}
		if (sliderPercentCombust != null && (combustJogador instanceof Integer)) {
			sliderPercentCombust.setValue((Integer) combustJogador);
		}
	}

	public JSlider getSpinnerCombustivel() {
		return spinnerCombustivel;
	}

	public JComboBox getComboBoxAsaInicial() {
		return comboBoxAsaInicial;
	}

	public void setComboBoxAsaInicial(JComboBox comboBoxAsaInicial) {
		this.comboBoxAsaInicial = comboBoxAsaInicial;
	}

	public void verificaProgramacaoBox() {
		if (controleJogo.getPilotoJogador() == null
				|| controleJogo.getPilotoJogador().isBox()) {
			return;
		}
		long volta = controleJogo.getPilotoJogador().getNumeroVolta();
		boolean ativo1 = progamacaoBox.getAtive1().isSelected();
		long voltaParada1 = ((Integer) (progamacaoBox
				.getSpinnerNumVoltaParada1().getValue())).intValue();
		if (ativo1 && volta == voltaParada1) {
			controleJogo.setBoxJogadorHumano(Lang.key(progamacaoBox
					.getBoxPneuParada1().getSelectedItem().toString()),
					progamacaoBox.getSliderPercentCombustParada1().getValue(),
					Lang.key(progamacaoBox.getComboBoxAsaParada1()
							.getSelectedItem().toString()));
			modoBox();
			if (controleJogo.getPilotoJogador().getPtosBox() != 0) {
				progamacaoBox.getAtive1().setSelected(false);
			}
		}
		boolean ativo2 = progamacaoBox.getAtive2().isSelected();
		long voltaParada2 = ((Integer) (progamacaoBox
				.getSpinnerNumVoltaParada2().getValue())).intValue();
		if (ativo2 && volta == voltaParada2
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			controleJogo.setBoxJogadorHumano(Lang.key(progamacaoBox
					.getBoxPneuParada2().getSelectedItem().toString()),
					progamacaoBox.getSliderPercentCombustParada2().getValue(),
					Lang.key(progamacaoBox.getComboBoxAsaParada2()
							.getSelectedItem().toString()));
			modoBox();
			if (controleJogo.getPilotoJogador().getPtosBox() != 0) {
				progamacaoBox.getAtive2().setSelected(false);
			}
		}
		boolean ativo3 = progamacaoBox.getAtive3().isSelected();
		long voltaParada3 = ((Integer) (progamacaoBox
				.getSpinnerNumVoltaParada3().getValue())).intValue();
		if (ativo3 && volta == voltaParada3
				&& controleJogo.getPilotoJogador().getPtosBox() == 0) {
			controleJogo.setBoxJogadorHumano(Lang.key(progamacaoBox
					.getBoxPneuParada3().getSelectedItem().toString()),
					progamacaoBox.getSliderPercentCombustParada3().getValue(),
					Lang.key(progamacaoBox.getComboBoxAsaParada3()
							.getSelectedItem().toString()));
			modoBox();
			if (controleJogo.getPilotoJogador().getPtosBox() != 0) {
				progamacaoBox.getAtive3().setSelected(false);
			}
		}
	}

	public boolean isProgamaBox() {
		return progamacaoBox.getAtive1().isSelected()
				|| progamacaoBox.getAtive2().isSelected()
				|| progamacaoBox.getAtive3().isSelected();
	}

	public void setZoom(double d) {
		if (painelCircuito == null) {
			return;
		}
		painelCircuito.zoom = d;
		painelCircuito.atualizaVarZoom();
	}

	public void adicinaTravadaRoda(TravadaRoda travadaRoda) {
		if (ultimaTravavadaRodas == 0) {
			ultimaTravavadaRodas = System.currentTimeMillis();
		}
		if (System.currentTimeMillis() - ultimaTravavadaRodas < 4000) {
			return;
		}
		if (painelCircuito != null) {
			ultimaTravavadaRodas = System.currentTimeMillis();
			painelCircuito.adicionatrvadaRoda(travadaRoda);
		}
	}

	public void setPosisRec(Point point) {
		if (painelCircuito == null)
			return;
		painelCircuito.setPosisRec(point);

	}

	public void setPosisAtual(Point point) {
		if (painelCircuito == null)
			return;
		painelCircuito.setPosisAtual(point);

	}

	public void carregaBackGroundCliente() {
		if (painelCircuito != null && painelCircuito.getBackGround() == null) {
			painelCircuito.carregaBackGround();
		}

	}

	public static void main(String[] args) {
		int parseInt = Integer.parseInt("123");
		System.out.println(parseInt);
		JFrame jFrame = new JFrame();
		JSlider spinner = new JSlider(0, 100);
		spinner.setPaintTrack(true);
		spinner.setPaintLabels(true);
		spinner.setPaintTicks(true);
		jFrame.getContentPane().add(spinner);
		jFrame.pack();
		jFrame.setVisible(true);
	}
}
