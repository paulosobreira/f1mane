package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.applet.JogoCliente;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.recursos.idiomas.LangVO;
import br.nnpe.Html;

public class GerenciadorVisual {
	private JPanel panelControle;
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
	private JComboBox boxPneuInicial;
	private JComboBox comboBoxAsaInicial;
	private JSpinner spinnerCombustivel;
	private JSpinner spinnerQtdeVoltas;
	private JSpinner spinnerTempoCiclo;
	private JSpinner spinnerSkillPadraoPilotos;
	private JSpinner spinnerPotenciaPadraoCarros;
	private JSpinner spinnerQtdeMinutosQualificacao;
	private JSpinner spinnerDificuldadeUltrapassagem;
	private JSpinner spinnerIndexVelcidadeEmReta;
	private PainelCircuito painelCircuito;
	private PainelTabelaPosicoes painelPosicoes;
	private InterfaceJogo controleJogo;
	private JPanel southPanel = new JPanel();
	private JPanel telemetriaPanel = new JPanel();
	private JLabel infoCorrida;
	private JLabel infoPiloto;
	private JPanel infoText = new JPanel();
	private JTextField nomeJogador;
	private JButton agressivo;
	private JButton box;
	private PainelTabelaResultadoFinal resultadoFinal;
	private ThreadMudancaClima clima;
	private int tempoSleep = 30;
	private JComboBox giro;
	private JComboBox modoPiloto;
	private JComboBox comboBoxTipoPneu;
	private JComboBox comboBoxAsa;
	private JSlider sliderPercentCombust;
	private Color corPadraoBarra;
	private int larguraFrame = 0;
	private int alturaFrame = 0;
	private long lastPress;

	public GerenciadorVisual(InterfaceJogo controleJogo) throws IOException {
		this.controleJogo = controleJogo;
	}

	public void iniciarInterfaceGraficaJogo() throws IOException {
		painelCircuito = new PainelCircuito(controleJogo, this);
		painelCircuito.setBackGround(CarregadorRecursos.carregaBackGround(
				controleJogo.getCircuito().getBackGround(), painelCircuito,
				controleJogo.getCircuito()));
		larguraFrame = painelCircuito.getBackGround().getWidth() + 145;
		alturaFrame = painelCircuito.getBackGround().getHeight() + 280;
		carregarInfoClima();
		gerarPainelPosicoes();
		gerarPainelComandos();
		gerarPainelInfoText();
		gerarPainetInfoGraf();
		gerarLayoutNovo();

		JFrame frame = controleJogo.getMainFrame();
		KeyListener keyListener = geraKeyListener();
		frame.addKeyListener(keyListener);
		painelCircuito.addKeyListener(keyListener);
		painelInfText.addKeyListener(keyListener);
		painelPosicoes.addKeyListener(keyListener);
		painelPosicoes.getPosicoesTable().addKeyListener(keyListener);
		pneuBar.addKeyListener(keyListener);
		agressivo.addKeyListener(keyListener);
		modoPiloto.addKeyListener(keyListener);
		panelControle.addKeyListener(keyListener);
		box.addKeyListener(keyListener);
		comboBoxTipoPneu.addKeyListener(keyListener);
		combustivelBar.addKeyListener(keyListener);
		giro.addKeyListener(keyListener);
		infoText.addKeyListener(keyListener);
		sliderPercentCombust.addKeyListener(keyListener);
		scrollPaneTextual.addKeyListener(keyListener);
		infoTextual.addKeyListener(keyListener);
		telemetriaPanel.addKeyListener(keyListener);
		comboBoxAsa.addKeyListener(keyListener);
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
			}
		};
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
		boolean modo = controleJogo.mudarModoBox();
		if (!(controleJogo instanceof JogoCliente)) {
			if (modo && !(controleJogo instanceof JogoCliente)) {
				box.setText(Lang.msg("137"));
			} else {
				box.setText(Lang.msg("138"));
			}

		}

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
		Piloto novoSel = painelPosicoes
				.obterPilotoSecionadoTabela(pilotoSelecionado);
		controleJogo.selecionouPiloto(novoSel);
		return novoSel;
	}

	private void atualizarImgClima(Clima clima) {
		ImageIcon icon = new ImageIcon(CarregadorRecursos
				.carregarImagem("clima/" + clima.getClima()));
		imgClima.setIcon(icon);

	}

	public void atualizaPainel() {
		if (ControleJogoLocal.VALENDO) {
			painelCircuito.repaint();
		}
		Piloto pilotoSelecionado = controleJogo.getPilotoSelecionado();
		atualizaPainelGraficoPilotoSelecionado(pilotoSelecionado);
		atualizaInfoAdicional(pilotoSelecionado);
		atualizarImgClima(new Clima(controleJogo.getClima()));
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
					+ controleJogo.totalVoltasCorrida();

			text += Lang.msg("144") + controleJogo.getNivelCorrida();
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

	public JSpinner getSpinnerDificuldadeUltrapassagem() {
		return spinnerDificuldadeUltrapassagem;
	}

	public JComboBox getComboBoxCircuito() {
		return comboBoxCircuito;
	}

	public JSpinner getSpinnerIndexVelcidadeEmReta() {
		return spinnerIndexVelcidadeEmReta;
	}

	public JSpinner getSpinnerTempoCiclo() {
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

	public PainelCircuito getPainelCircuito() {
		return painelCircuito;
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
		return panelControle;
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
		scrollPaneTextual = new JScrollPane(infoTextual);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.add(scrollPaneTextual);
		scrollPaneTextual.setBounds(0, 0, larguraFrame
				- painelPosicoes.getLarguraPainel() - 30, 200);
		painelInfText.add(panel, BorderLayout.CENTER);
		infoText.setLayout(new GridLayout(1, 1));
		infoCorrida = new JLabel(Lang.msg("213"));
		infoPiloto = new JLabel(Lang.msg("214"));
		infoText.add(infoCorrida);
		// infoText.add(infoPiloto);
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

	private void gerarLayoutNovo() {
		controleJogo.getMainFrame().getContentPane().removeAll();
		controleJogo.getMainFrame().getContentPane().setLayout(
				new BorderLayout());
		controleJogo.getMainFrame().getContentPane().add(painelCircuito,
				BorderLayout.CENTER);
		controleJogo.getMainFrame().getContentPane().add(southPanel,
				BorderLayout.SOUTH);
		controleJogo.getMainFrame().getContentPane().add(telemetriaPanel,
				BorderLayout.EAST);
		southPanel.setLayout(new BorderLayout());
		southPanel.add(painelPosicoes, BorderLayout.WEST);
		southPanel.add(painelInfText, BorderLayout.CENTER);

		telemetriaPanel.setLayout(new BorderLayout());
		telemetriaPanel.add(panelControle, BorderLayout.SOUTH);
		telemetriaPanel.add(painelInfGraf, BorderLayout.CENTER);

		southPanel.revalidate();
		if (controleJogo.getMainFrame().isModoApplet()) {
			controleJogo.getMainFrame().setSize(larguraFrame, alturaFrame);
		} else {
			controleJogo.getMainFrame().setSize(larguraFrame, alturaFrame);
		}

	}

	private void gerarPainelComandos() {

		agressivo = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("134");
			}
		};
		box = new JButton() {
			@Override
			public String getText() {
				return Lang.msg("135");
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
		panelControle = new JPanel();
		panelControle.setBorder(new TitledBorder("") {
			public String getTitle() {
				return Lang.msg("136");
			}
		});
		GridLayout gridLayout = new GridLayout(5, 1) {
			public Dimension preferredLayoutSize(Container parent) {
				return new Dimension(150, 140);
			}
		};

		panelControle.setLayout(gridLayout);
		panelControle.add(box);
		// panelControle.add(modoPiloto);
		panelControle.add(comboBoxTipoPneu);
		panelControle.add(comboBoxAsa);
		panelControle.add(new JLabel() {
			public String getText() {
				return Lang.msg("083");
			};
		});
		panelControle.add(sliderPercentCombust);

		addiconarListenerComandos(comboBoxTipoPneu, sliderPercentCombust);
	}

	private void gerarPainelPosicoes() throws IOException {
		painelPosicoes = new PainelTabelaPosicoes(controleJogo);
	}

	private void gerarPainelJogoSingle(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(14, 2));
		JLabel label = new JLabel() {

			public String getText() {
				return Lang.msg("110");
			}
		};
		painelInicio.add(label);
		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(22));
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
		for (Iterator iter = controleJogo.getCircuitos().keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			comboBoxCircuito.addItem(key);
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
		spinnerDificuldadeUltrapassagem = new JSpinner();
		spinnerDificuldadeUltrapassagem.setValue(new Integer(500));
		painelInicio.add(spinnerDificuldadeUltrapassagem);
		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("125");
			}
		});
		spinnerIndexVelcidadeEmReta = new JSpinner();
		spinnerIndexVelcidadeEmReta.setValue(new Integer(500));
		painelInicio.add(spinnerIndexVelcidadeEmReta);

		painelInicio.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("126");
			}
		});
		spinnerTempoCiclo = new JSpinner();
		spinnerTempoCiclo.setValue(new Integer(85));
		painelInicio.add(spinnerTempoCiclo);

		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("112");
			}
		});
		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		painelInicio.add(spinnerSkillPadraoPilotos);

		painelInicio.add(new JLabel() {
			public String getText() {
				return Lang.msg("113");
			}
		});
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));
		painelInicio.add(spinnerPotenciaPadraoCarros);

	}

	public boolean iniciarJogoSingle() {
		JPanel painelInicio = new JPanel();
		gerarPainelJogoSingle(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(22));
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

		Object selec = boxPilotoSelecionado.getSelectedItem();

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
		int iniY1 = 5;
		int iniY2 = 10;
		int midPainel = painelCircuito.getWidth() / 2;
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			if (piloto.getPosicao() % 2 == 0) {
				ptosPilotos.add(new Point(midPainel + 30, iniY2));
				iniY2 += 30;
			} else {
				ptosPilotos.add(new Point(midPainel - 120, iniY1));
				iniY1 += 30;
			}
		}
		painelCircuito.setDesenhaQualificacao(true);
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			Point point = (Point) ptosPilotos.get(i);
			int x = painelCircuito.getWidth();
			while (x > point.x) {
				painelCircuito.definirDesenhoQualificacao(piloto, new Point(x,
						point.y));
				if (tempoSleep != 0) {
					painelCircuito.repaint();
				} else {
					break;
				}
				x -= 3;
				try {
					Thread.sleep(tempoSleep);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			synchronized (painelCircuito.getMapDesenharQualificacao()) {
				painelCircuito.getMapDesenharQualificacao().put(piloto, point);
			}
		}
		try {
			painelCircuito.repaint();
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		painelCircuito.setDesenhaQualificacao(false);
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
				StringReader reader = new StringReader(buffer.toString());
				infoTextual.read(reader, "");
			}
		} catch (Exception e) {
			e.printStackTrace();
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
}
