package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.applet.JogoCliente;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.recursos.idiomas.LangVO;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

public class GerenciadorVisual {
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
	private JSpinner spinnerCombustivel;
	private JSpinner spinnerQtdeVoltas;
	private JSlider spinnerTempoCiclo;
	private JSpinner spinnerSkillPadraoPilotos;
	private JSpinner spinnerPotenciaPadraoCarros;
	private JSpinner spinnerQtdeMinutosQualificacao;
	private JSlider spinnerDificuldadeUltrapassagem;
	private JSlider spinnerIndexVelcidadeEmReta;
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
	private JButton progBox;
	private PainelTabelaResultadoFinal resultadoFinal;
	private ThreadMudancaClima clima;
	private int tempoSleep = 30;
	private JComboBox giro;
	private JComboBox modoPiloto;
	private JComboBox comboBoxTipoPneu;
	private JComboBox comboBoxAsa;
	private JSlider sliderPercentCombust;
	private JButton pos1;
	private JButton pos0;
	private JButton pos2;
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
	private JPanel panelControlePos;

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
				painelCircuito.zoom += e.getWheelRotation() / 30.0;
				if (painelCircuito.zoom > 1) {
					painelCircuito.zoom = 1;
				}
				if (painelCircuito.zoom < 0.1) {
					painelCircuito.zoom = 0.1;
				}
				painelCircuito.atualizaVarZoom();
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
		pos0.addKeyListener(keyListener);
		pos1.addKeyListener(keyListener);
		pos2.addKeyListener(keyListener);
		autoPos.addKeyListener(keyListener);
		alternaPiloto.addKeyListener(keyListener);
		alternaPiloto.addMouseWheelListener(mw);
		pos0.addMouseWheelListener(mw);
		pos1.addMouseWheelListener(mw);
		pos2.addMouseWheelListener(mw);
		autoPos.addMouseWheelListener(mw);
		frame.addMouseWheelListener(mw);
		painelCircuito.addMouseWheelListener(mw);
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
		super.finalize();

		if (clima != null) {
			clima.interrupt();
		}
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
		pos1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarPos1();
			}

		});
		pos0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarPos0();
			}

		});
		pos2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarPos2();
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
				int ret = JOptionPane.showConfirmDialog(controleJogo
						.getMainFrame(), Lang.msg("095"), Lang.msg("094"),
						JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.NO_OPTION) {
					return;
				}
				controleJogo.abandonar();
				super.windowClosing(e);
				if (controleJogo.getMainFrame().isModoApplet()) {
					controleJogo.getMainFrame().setVisible(false);
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
				}
				if (keyCoode == KeyEvent.VK_LEFT) {
					if (controleJogo.getPilotoJogador().getTracado() == 2) {
						mudarPos0();
					} else {
						mudarPos1();
					}
				}
				if (keyCoode == KeyEvent.VK_RIGHT) {
					if (controleJogo.getPilotoJogador().getTracado() == 1) {
						mudarPos0();
					} else {
						mudarPos2();
					}
				}
				if (keyCoode == KeyEvent.VK_F9) {
					mudaPilotoSelecionado();
				}
			}
		};
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
		controleJogo.setBoxJogadorHumano(Lang.key(comboBoxTipoPneu
				.getSelectedItem().toString()), value, Lang.key(comboBoxAsa
				.getSelectedItem().toString()));
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

	ImageIcon iconSol = new ImageIcon(CarregadorRecursos
			.carregarImagem("clima/sol.gif"));
	ImageIcon iconNublado = new ImageIcon(CarregadorRecursos
			.carregarImagem("clima/nublado.gif"));
	ImageIcon iconChuva = new ImageIcon(CarregadorRecursos
			.carregarImagem("clima/chuva.gif"));

	private void atualizarImgClima(Clima clima) {
		if (Clima.SOL.equals(clima.getClima()))
			imgClima.setIcon(iconSol);
		if (Clima.CHUVA.equals(clima.getClima()))
			imgClima.setIcon(iconChuva);
		if (Clima.NUBLADO.equals(clima.getClima()))
			imgClima.setIcon(iconNublado);

	}

	public void atualizaPainel() {
		Piloto pilotoSelecionado = controleJogo.getPilotoSelecionado();
		atualizaPainelGraficoPilotoSelecionado(pilotoSelecionado);
		atualizaInfoAdicional(pilotoSelecionado);
		atualizarImgClima(new Clima(controleJogo.getClima()));
		if (ControleJogoLocal.VALENDO) {
			try {
				if (pilotoSelecionado == null) {
					List l = controleJogo.getCircuito().getPistaFull();
					No n = (No) l.get(0);
					painelCircuito.centralizarPonto(n.getPoint());
				} else {
					painelCircuito.centralizarPonto(pilotoSelecionado
							.getNoAtual().getPoint());
				}
			} catch (Exception e) {
				Logger.logar(e);
			}
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
			String text = Lang.msg("142", new Object[] { voltaCorrida
					.obterTempoVoltaFormatado() })
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

	public JSlider getSpinnerIndexVelcidadeEmReta() {
		return spinnerIndexVelcidadeEmReta;
	}

	public JSlider getSpinnerTempoCiclo() {
		return spinnerTempoCiclo;
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

	public JSpinner getSpinnerCombustivelInicial() {
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
				return new Dimension(scrollPaneTextual.getWidth(), 150);
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
		controleJogo.getMainFrame().getContentPane().setLayout(
				new BorderLayout());

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
		eastPanel.add(painelInfGraf, BorderLayout.CENTER);
		eastPanel.add(painelPosicoes, BorderLayout.NORTH);

		controleJogo.getMainFrame().getContentPane().add(centerPanel,
				BorderLayout.CENTER);
		controleJogo.getMainFrame().getContentPane().add(eastPanel,
				BorderLayout.EAST);
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
		GridLayout gridLayout = new GridLayout(6, 1) {
			public Dimension preferredLayoutSize(Container parent) {
				return new Dimension(150, 140);
			}
		};

		panelControleBox.setLayout(gridLayout);
		panelControleBox.add(progBox);
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
				return Lang.msg("TRACADO");
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
		panelControlePos.add(agressivo);
		panelControlePos.add(alternaPiloto);
		pos1 = new JButton("(|");
		pos0 = new JButton("||");
		pos2 = new JButton("|)");
		JPanel volPanel = new JPanel(new GridLayout(1, 3));
		volPanel.add(pos1);
		volPanel.add(pos0);
		volPanel.add(pos2);
		panelControlePos.add(autoPos);
		panelControlePos.add(volPanel);

		addiconarListenerComandos(comboBoxTipoPneu, sliderPercentCombust);
	}

	private void gerarPainelPosicoes() throws IOException {
		painelPosicoes = new PainelTabelaPosicoes(controleJogo);
	}

	private void gerarPainelJogoSingle(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(12, 2, 5, 5));
		JLabel label = new JLabel() {

			public String getText() {
				return Lang.msg("110");
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
		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("121");
			}
		});
		painelInicio.add(comboBoxCircuito);

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
		spinnerCombustivel = new JSpinner();
		spinnerCombustivel.setValue(new Integer(50));

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
				return Lang.msg("125");
			}
		});
		spinnerIndexVelcidadeEmReta = new JSlider(500, 1000);
		spinnerIndexVelcidadeEmReta.setValue(new Integer(Util.intervalo(500,
				1000)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(000), new JLabel("Antigos") {
			@Override
			public String getText() {
				return Lang.msg("ANTIGOS");
			}
		});
		labelTable.put(new Integer(1000), new JLabel("Novos") {
			@Override
			public String getText() {
				return Lang.msg("NOVOS");
			}
		});
		spinnerIndexVelcidadeEmReta.setLabelTable(labelTable);
		spinnerIndexVelcidadeEmReta.setPaintLabels(true);
		painelInicio.add(spinnerIndexVelcidadeEmReta);

		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		spinnerTempoCiclo = new JSlider(70, 170);
		spinnerTempoCiclo.setValue(new Integer(Util.intervalo(70, 170)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(70), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("RAPIDOS");
			}
		});
		labelTable.put(new Integer(170), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("LENTOS");
			}
		});
		spinnerTempoCiclo.setLabelTable(labelTable);
		spinnerTempoCiclo.setPaintLabels(true);
		painelInicio.add(spinnerTempoCiclo);

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
						return p1.getCarro().getNome().compareTo(
								p2.getCarro().getNome());
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
				return new Dimension(210, 300);
			}
		}, BorderLayout.CENTER);

		JPanel grid = new JPanel();
		grid.setLayout(new GridLayout(9, 2, 5, 5));
		JLabel label = new JLabel() {

			public String getText() {
				return Lang.msg("110");
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
				return Lang.msg("125");
			}
		});
		spinnerIndexVelcidadeEmReta = new JSlider(500, 1000);
		spinnerIndexVelcidadeEmReta.setValue(new Integer(Util.intervalo(500,
				1000)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(500), new JLabel("Antigos") {
			@Override
			public String getText() {
				return Lang.msg("ANTIGOS");
			}
		});
		labelTable.put(new Integer(1000), new JLabel("Novos") {
			@Override
			public String getText() {
				return Lang.msg("NOVOS");
			}
		});
		spinnerIndexVelcidadeEmReta.setLabelTable(labelTable);
		spinnerIndexVelcidadeEmReta.setPaintLabels(true);
		grid.add(spinnerIndexVelcidadeEmReta);

		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		spinnerTempoCiclo = new JSlider(70, 170);
		spinnerTempoCiclo.setValue(new Integer(Util.intervalo(70, 170)));
		labelTable = new Hashtable();
		labelTable.put(new Integer(70), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("RAPIDOS");
			}
		});
		labelTable.put(new Integer(170), new JLabel("") {
			@Override
			public String getText() {
				return Lang.msg("LENTOS");
			}
		});
		spinnerTempoCiclo.setLabelTable(labelTable);
		spinnerTempoCiclo.setPaintLabels(true);
		grid.add(spinnerTempoCiclo);

		// grid.add(new JLabel() {
		// public String getText() {
		// return Lang.msg("112");
		// }
		// });
		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		// grid.add(spinnerSkillPadraoPilotos);

		// grid.add(new JLabel() {
		// public String getText() {
		// return Lang.msg("113");
		// }
		// });
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));
		// grid.add(spinnerPotenciaPadraoCarros);

		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("302");
			}
		});
		semReabastacimento = new JCheckBox();
		grid.add(semReabastacimento);
		grid.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("303");
			}
		});
		semTrocaPneu = new JCheckBox();

		grid.add(semTrocaPneu);

		grid.setBorder(new TitledBorder(Lang.msg("273")));
		incialPanel.add(grid, BorderLayout.CENTER);
		incialPanel.add(pilotoPanel, BorderLayout.EAST);

	}

	public JCheckBox getSemTrocaPneu() {
		return semTrocaPneu;
	}

	public JCheckBox getSemReabastacimento() {
		return semReabastacimento;
	}

	public boolean iniciarJogoSingle() {
		JPanel painelInicio = new JPanel();
		gerarPainelJogoSingle(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(12));
		int ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
				painelInicio, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.NO_OPTION) {
			return false;
		}
		while ((((Integer) spinnerQtdeVoltas.getValue()).intValue() < 2)
				|| (((Integer) spinnerCombustivel.getValue()).intValue() == 0)) {
			JOptionPane.showMessageDialog(controleJogo.getMainFrame(), Lang
					.msg("128"), Lang.msg("128"),
					JOptionPane.INFORMATION_MESSAGE);
			ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
					painelInicio, Lang.msg("127"), JOptionPane.YES_NO_OPTION);
			spinnerQtdeVoltas.requestFocus();
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		Object selec = listPilotosSelecionados.getSelectedValue();
		controleJogo.setTemporada("t" + comboBoxTemporadas.getSelectedItem());
		if (!ControleJogoLocal.VALENDO) {
			selec = "";
		}

		if (selec instanceof Piloto) {
			controleJogo
					.efetuarSelecaoPilotoJogador(selec, Lang.key(boxPneuInicial
							.getSelectedItem().toString()), spinnerCombustivel
							.getValue(), nomeJogador.getText(), Lang
							.key((String) comboBoxAsaInicial.getSelectedItem()));
		}
		return true;
	}

	public boolean iniciarJogoMulti(Campeonato campeonato) {
		JPanel painelInicio = new JPanel();
		gerarPainelJogoMulti(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(12));
		if (campeonato != null) {
			comboBoxTemporadas.setSelectedItem(campeonato.getTemporada());
			comboBoxTemporadas.setEnabled(false);

			spinnerQtdeVoltas.setValue(campeonato.getQtdeVoltas());
			spinnerQtdeVoltas.setEnabled(false);
			comboBoxNivelCorrida.setSelectedItem(Lang
					.msg(campeonato.getNivel()));
			comboBoxNivelCorrida.setEnabled(false);
			List indices = new ArrayList();
			DefaultListModel defaultListModel = (DefaultListModel) listPilotosSelecionados
					.getModel();
			for (int i = 0; i < defaultListModel.getSize(); i++) {
				Piloto piloto = (Piloto) defaultListModel.get(i);
				if (campeonato.getPilotos().contains(piloto.toString())) {
					indices.add(new Integer(i));
				}
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
			spinnerIndexVelcidadeEmReta.setEnabled(false);
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
		if (ret == JOptionPane.NO_OPTION) {
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
			ImageIcon icon = new ImageIcon(CarregadorRecursos
					.carregarImagem("clima/" + clima.getClima()));
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
			spinnerCombustivel = new JSpinner();
			spinnerCombustivel.setValue(new Integer(50));
			if (semReabastacimento.isSelected())
				spinnerCombustivel.setValue(new Integer(100));
			painelJogSel.add(tipoPneu);
			painelJogSel.add(boxPneuInicial);
			painelJogSel.add(tipoAsa);
			painelJogSel.add(comboBoxAsaInicial);
			painelJogSel.add(qtdeComustivel);
			painelJogSel.add(spinnerCombustivel);

			JOptionPane.showMessageDialog(controleJogo.getMainFrame(),
					painelJogSel, Lang.msg("275", new String[] { selec[i]
							.toString() }), JOptionPane.QUESTION_MESSAGE);
			controleJogo
					.efetuarSelecaoPilotoJogador(selec[i], Lang
							.key(boxPneuInicial.getSelectedItem().toString()),
							spinnerCombustivel.getValue(), nomeJogador
									.getText(), Lang
									.key((String) comboBoxAsaInicial
											.getSelectedItem()));

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
		if (ret == JOptionPane.NO_OPTION) {
			return false;
		}
		while ((((Integer) spinnerQtdeVoltas.getValue()).intValue() < 21)
				|| (((Integer) spinnerCombustivel.getValue()).intValue() == 0)) {
			JOptionPane.showMessageDialog(controleJogo.getMainFrame(), Lang
					.msg("128"), Lang.msg("128"),
					JOptionPane.INFORMATION_MESSAGE);
			ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
					painelInicio, "Setup Inicial", JOptionPane.YES_NO_OPTION);
			spinnerQtdeVoltas.requestFocus();
			if (ret == JOptionPane.NO_OPTION) {
				return false;
			}
		}

		Object selec = boxPilotoSelecionado.getSelectedItem();

		if (!ControleJogoLocal.VALENDO) {
			selec = "";
		}

		if (selec instanceof Piloto) {
			controleJogo.efetuarSelecaoPilotoJogador(selec, Lang
					.key(boxPneuInicial.getSelectedItem().toString()),
					spinnerCombustivel.getValue(), nomeJogador.getText(), Lang
							.key(comboBoxAsaInicial.getSelectedItem()
									.toString()));
		}

		return true;
	}

	public void desenhaQualificacao() {
		infoCorrida.setText(Lang.msg("213"));
		infoPiloto.setText(Lang.msg("214"));
		List pilotos = controleJogo.getPilotos();
		List ptosPilotos = new ArrayList();

		painelCircuito.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				tempoSleep = 0;
			}

		});
		No n = (No) controleJogo.getCircuito().getPistaFull().get(0);
		painelCircuito.centralizarPontoDireto(n.getPoint());
		Rectangle limitesViewPort = null;
		limitesViewPort = (Rectangle) painelCircuito.limitesViewPort();
		int iniY1 = 30;
		int iniY2 = 40;
		int midPainel = (limitesViewPort.width / 2);
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
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Logger.logarExept(e);
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			Point point = (Point) ptosPilotos.get(i);
			limitesViewPort = (Rectangle) painelCircuito.limitesViewPort();
			int x = limitesViewPort.x + limitesViewPort.width;
			while (x > (point.x + limitesViewPort.x)) {
				limitesViewPort = (Rectangle) painelCircuito.limitesViewPort();
				Point pd = new Point(x, point.y + limitesViewPort.y);
				painelCircuito.definirDesenhoQualificacao(piloto, pd);
				if (tempoSleep != 0) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							painelCircuito.repaint();
						}
					});
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
			synchronized (painelCircuito.getMapDesenharQualificacao()) {
				painelCircuito.getMapDesenharQualificacao().put(piloto, point);
			}
		}
		try {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					painelCircuito.repaint();
				}
			});
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Logger.logarExept(e);
		}
		painelCircuito.setDesenhouQualificacao(true);

	}

	public void atulizaTabelaPosicoes() {
		painelPosicoes.atulizaTabelaPosicoes(controleJogo.getPilotos(),
				controleJogo.getPilotoSelecionado());

	}

	public void adicionarInfoDireto(String string) {
		try {
			synchronized (bufferTextual) {
				if (string != null && !string.startsWith("<table>"))
					bufferTextual.add(Html.cinza(Lang.msg("082")
							+ controleJogo.getNumVoltaAtual() + " ")
							+ string + "<br>");
				else {
					bufferTextual.add(string);

				}
				StringBuffer buffer = new StringBuffer();
				String textoAnterior = "";
				for (int i = bufferTextual.size() - 1; i >= 0; i--) {
					String texto = Html.sansSerif(bufferTextual.get(i)
							.toString());
					if (!textoAnterior.equals(texto)) {
						buffer.append(texto);
						textoAnterior = texto;
					}
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

	public JSpinner getSpinnerCombustivel() {
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
		}
		boolean ativo2 = progamacaoBox.getAtive2().isSelected();
		long voltaParada2 = ((Integer) (progamacaoBox
				.getSpinnerNumVoltaParada2().getValue())).intValue();
		if (ativo2 && volta == voltaParada2) {
			controleJogo.setBoxJogadorHumano(Lang.key(progamacaoBox
					.getBoxPneuParada2().getSelectedItem().toString()),
					progamacaoBox.getSliderPercentCombustParada2().getValue(),
					Lang.key(progamacaoBox.getComboBoxAsaParada2()
							.getSelectedItem().toString()));
			modoBox();
		}
		boolean ativo3 = progamacaoBox.getAtive3().isSelected();
		long voltaParada3 = ((Integer) (progamacaoBox
				.getSpinnerNumVoltaParada3().getValue())).intValue();
		if (ativo3 && volta == voltaParada3) {
			controleJogo.setBoxJogadorHumano(Lang.key(progamacaoBox
					.getBoxPneuParada3().getSelectedItem().toString()),
					progamacaoBox.getSliderPercentCombustParada3().getValue(),
					Lang.key(progamacaoBox.getComboBoxAsaParada3()
							.getSelectedItem().toString()));
			modoBox();
		}
	}

	public boolean isProgamaBox() {
		return progamacaoBox.getAtive1().isSelected()
				|| progamacaoBox.getAtive2().isSelected()
				|| progamacaoBox.getAtive3().isSelected();
	}
}
