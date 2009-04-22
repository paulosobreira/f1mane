package sowbreira.f1mane.visao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.JSpinner.DefaultEditor;
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
	private JComboBox comboBoxClimaInicial;
	private JComboBox comboBoxNivelCorrida;
	private JComboBox comboBoxCircuito;
	private JComboBox comboBoxAsa;
	private JComboBox boxPilotoSelecionado;
	private JComboBox boxPneuInicial;
	private JSpinner spinnerCombustivelInicial;
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
	private JSpinner spinnerPercentCombust;
	private Color corPadraoBarra;
	private int larguraFrame = 0;
	private int alturaFrame = 0;

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
		gerarPainelComandosNovo();
		gerarPainelInfoTextNovo();
		gerarPainetInfoGrafNovo();
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
		spinnerPercentCombust.addKeyListener(keyListener);
		DefaultEditor defaultEditor = (DefaultEditor) spinnerPercentCombust
				.getEditor();
		scrollPaneTextual.addKeyListener(keyListener);
		infoTextual.addKeyListener(keyListener);
		defaultEditor.getTextField().addKeyListener(keyListener);
		spinnerPercentCombust.getEditor().addKeyListener(keyListener);
		telemetriaPanel.addKeyListener(keyListener);

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
			final JSpinner spinnerPercentCombust) {
		agressivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mudarModoAgressivo();
			}
		});

		modoPiloto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String modo = (String) modoPiloto.getSelectedItem();
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
						.getMainFrame(),
						"Isto ocasionarar um abandono de Corrida.",
						"Abandonar Corrida", JOptionPane.YES_NO_OPTION);
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
				int keyCoode = e.getKeyCode();

				if (keyCoode == KeyEvent.VK_F1) {
					giro.setSelectedItem(Lang.msg(Carro.GIRO_MIN));
				}
				if (keyCoode == KeyEvent.VK_F2) {
					giro.setSelectedItem(Lang.msg(Carro.GIRO_NOR));
				}
				if (keyCoode == KeyEvent.VK_F3) {
					giro.setSelectedItem(Lang.msg(Carro.GIRO_MAX));
				}
				if (keyCoode == KeyEvent.VK_F4) {
					mudarModoAgressivo();
				}
				if (keyCoode == KeyEvent.VK_F12) {
					mudarModoBox();
				}
				if (keyCoode == KeyEvent.VK_F5) {
					modoPiloto.setSelectedItem(Lang.msg(Piloto.LENTO));
					mudarModoPilotagem(Piloto.LENTO);
				}
				if (keyCoode == KeyEvent.VK_F6) {
					modoPiloto.setSelectedItem(Lang.msg(Piloto.NORMAL));
					mudarModoPilotagem(Piloto.NORMAL);
				}
				if (keyCoode == KeyEvent.VK_F7) {
					modoPiloto.setSelectedItem(Lang.msg(Piloto.AGRESSIVO));
					mudarModoPilotagem(Piloto.AGRESSIVO);
				}
				if (keyCoode == KeyEvent.VK_ESCAPE) {
					painelCircuito.setDesenhaPosVelo(!painelCircuito
							.isDesenhaPosVelo());
				}
			}
		};
	}

	protected void mudarModoBox() {
		if (controleJogo == null) {
			return;
		}
		Integer value = (Integer) spinnerPercentCombust.getValue();
		if (value.intValue() < 0) {
			value = new Integer(0);
		}
		if (value.intValue() > 100) {
			value = new Integer(100);
		}
		controleJogo.setBoxJogadorHumano(Lang.key((String) comboBoxTipoPneu
				.getSelectedItem()), value, Lang.key((String) comboBoxAsa
				.getSelectedItem()));
		boolean modo = controleJogo.mudarModoBox();
		if (!(controleJogo instanceof JogoCliente)) {
			if (modo && !(controleJogo instanceof JogoCliente)) {
				box.setText("Vai p/box F12");
			} else {
				box.setText("Ir p/box F12");
			}

		}

	}

	protected void mudarGiro() {
		if (controleJogo == null) {
			return;
		}
		controleJogo.mudarGiroMotor(Lang.key((String) giro.getSelectedItem()));
	}

	protected void mudarModoAgressivo() {
		if (controleJogo == null) {
			return;
		}
		boolean modo = controleJogo.mudarModoAgressivo();
		if (!(controleJogo instanceof JogoCliente)) {
			if (modo) {
				agressivo.setText("Agressivo F4");
			} else {
				agressivo.setText("Normal F4");
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
			String text = "Melhor Volta :"
					+ voltaCorrida.obterTempoVoltaFormatado()
					+ " de "
					+ (piloto != null ? piloto.getNome() + " - "
							+ piloto.getCarro().getNome() : "") + " "
					+ controleJogo.getNumVoltaAtual() + "/"
					+ controleJogo.totalVoltasCorrida();

			text += " Nivel da corrida: " + controleJogo.getNivelCorrida();
			if (controleJogo.isSafetyCarNaPista()) {
				text += " Safety Car na pista.";
			}
			infoCorrida.setText(text);
		}

		if (pilotoSelecionado != null) {

			String plider = "";

			String infoBox = pilotoSelecionado.getCarro().getTipoPneu() + " "
					+ pilotoSelecionado.getCarro().getAsa() + " Paradas "
					+ pilotoSelecionado.getQtdeParadasBox() + " "
					+ (pilotoSelecionado.isBox() ? "Box" : "");

			if (pilotoSelecionado.getPosicao() == 1) {
				plider = "Lider ";
			} else {
				controleJogo.calculaSegundosParaLider(pilotoSelecionado);
				plider = pilotoSelecionado.getSegundosParaLider();
			}

			String text = "P/Lider: " + plider;

			if ((pilotoSelecionado.getNumeroVolta() > 0)) {
				if (pilotoSelecionado.getUltimaVolta() != null) {
					text += (" Ultima: " + pilotoSelecionado.getUltimaVolta()
							.obterTempoVoltaFormatado());
				}

				Volta voltaPiloto = controleJogo
						.obterMelhorVolta(pilotoSelecionado);

				if (voltaPiloto != null) {
					text += (" Melhor: " + voltaPiloto
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
		return spinnerCombustivelInicial;
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

	private void gerarPainelInfoTextNovo() {
		painelInfText = new JPanel(new BorderLayout());
		infoTextual = new JEditorPane("text/html", "");
		infoTextual.setEditable(false);
		bufferTextual = new ArrayList();
		scrollPaneTextual = new JScrollPane(infoTextual);
		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.add(scrollPaneTextual);
		scrollPaneTextual.setBounds(0, 0, larguraFrame
				- painelPosicoes.getLarguraPainel() - 30, 185);
		painelInfText.add(panel, BorderLayout.CENTER);
		infoText.setLayout(new GridLayout(1, 1));
		infoCorrida = new JLabel("Informações sobre a Corrida");
		infoPiloto = new JLabel("Informações sobre o Piloto");
		infoText.add(infoCorrida);
		// infoText.add(infoPiloto);
		painelInfText.add(infoText, BorderLayout.NORTH);
	}

	private void gerarPainetInfoGrafNovo() {
		JPanel panelCol1 = new JPanel();
		panelCol1.setLayout(new BorderLayout());

		JPanel panelCol2 = new JPanel();
		panelCol2.setLayout(new BorderLayout());

		JPanel panelCol3 = new JPanel();
		panelCol3.setLayout(new BorderLayout());

		JLabel combustivel = new JLabel("Comb..");
		panelCol1.add(combustivel, BorderLayout.NORTH);
		combustivelBar = new JProgressBar(JProgressBar.VERTICAL);
		combustivelBar.setStringPainted(true);
		panelCol1.add(combustivelBar, BorderLayout.CENTER);

		JLabel pneu = new JLabel("Pneus");
		panelCol2.add(pneu, BorderLayout.NORTH);
		pneuBar = new JProgressBar(JProgressBar.VERTICAL);
		pneuBar.setStringPainted(true);
		panelCol2.add(pneuBar, BorderLayout.CENTER);
		motorBar = new JProgressBar(JProgressBar.VERTICAL);
		motorBar.setStringPainted(true);
		JLabel motoLabel = new JLabel("Motor");
		panelCol3.add(motoLabel, BorderLayout.NORTH);
		panelCol3.add(motorBar, BorderLayout.CENTER);
		painelInfGraf = new JPanel(new GridLayout(1, 3));
		painelInfGraf.add(panelCol1);
		painelInfGraf.add(panelCol2);
		painelInfGraf.add(panelCol3);
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

	private void gerarPainelComandosNovo() {
		panelControle = new JPanel();

		agressivo = new JButton("Agressivo F4");
		box = new JButton("Box F12");
		modoPiloto = new JComboBox();
		modoPiloto.addItem(Lang.msg("008"));
		modoPiloto.addItem(Lang.msg(Piloto.AGRESSIVO));
		modoPiloto.addItem(Lang.msg(Piloto.NORMAL));
		modoPiloto.addItem(Lang.msg(Piloto.LENTO));
		giro = new JComboBox();
		giro.addItem(Lang.msg("012"));
		giro.addItem(Lang.msg(Carro.GIRO_NOR));
		giro.addItem(Lang.msg(Carro.GIRO_MAX));
		giro.addItem(Lang.msg(Carro.GIRO_MIN));

		comboBoxTipoPneu = new JComboBox();
		comboBoxTipoPneu.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
		comboBoxTipoPneu.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
		comboBoxTipoPneu.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));

		JLabel infoBox = new JLabel("Combust % / Asa");
		spinnerPercentCombust = new JSpinner();
		spinnerPercentCombust.setValue(new Integer(50));
		JPanel infoBoxPanel = new JPanel(new GridBagLayout());
		comboBoxAsa = new JComboBox();
		comboBoxAsa.addItem(Lang.msg(Carro.ASA_NORMAL));
		comboBoxAsa.addItem(Lang.msg(Carro.MAIS_ASA));
		comboBoxAsa.addItem(Lang.msg(Carro.MENOS_ASA));
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.gridwidth = 1;
		constraints.gridheight = 1;
		infoBoxPanel.add(spinnerPercentCombust, constraints);
		constraints = new GridBagConstraints();
		constraints.gridx = 1;
		constraints.gridy = 0;
		constraints.gridwidth = 2;
		constraints.gridheight = 1;
		infoBoxPanel.add(comboBoxAsa, constraints);
		panelControle.setLayout(new GridLayout(7, 1));
		panelControle.add(modoPiloto);
		panelControle.add(agressivo);
		panelControle.add(giro);
		panelControle.add(box);
		panelControle.add(comboBoxTipoPneu);
		panelControle.add(infoBox);
		panelControle.add(infoBoxPanel);
		addiconarListenerComandos(comboBoxTipoPneu, spinnerPercentCombust);
	}

	private void gerarPainelPosicoes() throws IOException {
		painelPosicoes = new PainelTabelaPosicoes(controleJogo);
	}

	private void gerarPainelJogoSingle(JPanel painelInicio) {
		painelInicio.setLayout(new GridLayout(14, 2));
		JLabel label = new JLabel("Número de voltas da corrida (2-72):");
		painelInicio.add(label);
		spinnerQtdeVoltas = new JSpinner();
		spinnerQtdeVoltas.setValue(new Integer(22));
		if (!ControleJogoLocal.VALENDO) {
			spinnerQtdeVoltas.setValue(new Integer(30));
		}

		painelInicio.add(spinnerQtdeVoltas);

		painelInicio.add(new JLabel("Apelido :"));
		nomeJogador = new JTextField();
		painelInicio.add(nomeJogador);

		boxPilotoSelecionado = new JComboBox();
		boxPilotoSelecionado.addItem("Ver corrida");
		for (Iterator iter = controleJogo.getPilotos().iterator(); iter
				.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			boxPilotoSelecionado.addItem(piloto);
		}

		painelInicio.add(new JLabel("Selecionar Piloto :"));
		painelInicio.add(boxPilotoSelecionado);

		comboBoxCircuito = new JComboBox();
		for (Iterator iter = controleJogo.getCircuitos().keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			comboBoxCircuito.addItem(key);
		}
		painelInicio.add(new JLabel("Selecionar Circuito :"));
		painelInicio.add(comboBoxCircuito);

		comboBoxNivelCorrida = new JComboBox();
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.NORMAL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.FACIL));
		comboBoxNivelCorrida.addItem(Lang.msg(ControleJogoLocal.DIFICIL));
		painelInicio.add(new JLabel("Nivel da corrida :"));
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

		painelInicio.add(new JLabel("Clima :"));
		painelInicio.add(comboBoxClimaInicial);

		JLabel tipoPneu = new JLabel(Lang.msg("009"));
		boxPneuInicial = new JComboBox();
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_MOLE));
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_DURO));
		boxPneuInicial.addItem(Lang.msg(Carro.TIPO_PNEU_CHUVA));

		JLabel tipoAsa = new JLabel(Lang.msg("010"));
		comboBoxAsa = new JComboBox();
		comboBoxAsa.addItem(Lang.msg(Carro.ASA_NORMAL));
		comboBoxAsa.addItem(Lang.msg(Carro.MAIS_ASA));
		comboBoxAsa.addItem(Lang.msg(Carro.MENOS_ASA));

		JLabel qtdeComustivel = new JLabel(Lang.msg("011"));
		spinnerCombustivelInicial = new JSpinner();
		spinnerCombustivelInicial.setValue(new Integer(50));

		painelInicio.add(tipoPneu);
		painelInicio.add(boxPneuInicial);
		painelInicio.add(tipoAsa);
		painelInicio.add(comboBoxAsa);
		painelInicio.add(qtdeComustivel);
		painelInicio.add(spinnerCombustivelInicial);
		painelInicio.add(new JLabel("Dificuldade Utrapassagem (0-999):"));
		spinnerDificuldadeUltrapassagem = new JSpinner();
		spinnerDificuldadeUltrapassagem.setValue(new Integer(500));
		painelInicio.add(spinnerDificuldadeUltrapassagem);
		painelInicio.add(new JLabel("Index velocidade em reta (0-999):"));
		spinnerIndexVelcidadeEmReta = new JSpinner();
		spinnerIndexVelcidadeEmReta.setValue(new Integer(500));
		painelInicio.add(spinnerIndexVelcidadeEmReta);

		painelInicio.add(new JLabel("Tempo Ciclo (50ms-120ms):"));
		spinnerTempoCiclo = new JSpinner();
		spinnerTempoCiclo.setValue(new Integer(85));
		painelInicio.add(spinnerTempoCiclo);

		painelInicio.add(new JLabel(
				"Habilidade pilotos (0 para realista (0-99)):"));
		spinnerSkillPadraoPilotos = new JSpinner();
		spinnerSkillPadraoPilotos.setValue(new Integer(0));
		painelInicio.add(spinnerSkillPadraoPilotos);

		painelInicio.add(new JLabel(
				"Potencia Carros (0 para realista (0-999)):"));
		spinnerPotenciaPadraoCarros = new JSpinner();
		spinnerPotenciaPadraoCarros.setValue(new Integer(0));
		painelInicio.add(spinnerPotenciaPadraoCarros);

	}

	public boolean iniciarJogoSingle() {
		JPanel painelInicio = new JPanel();
		gerarPainelJogoSingle(painelInicio);
		spinnerQtdeVoltas.setValue(new Integer(22));
		int ret = JOptionPane.showConfirmDialog(controleJogo.getMainFrame(),
				painelInicio, "Setup Inicial", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.NO_OPTION) {
			return false;
		}
		while ((((Integer) spinnerQtdeVoltas.getValue()).intValue() < 2)
				|| (((Integer) spinnerCombustivelInicial.getValue()).intValue() == 0)) {
			JOptionPane.showMessageDialog(controleJogo.getMainFrame(),
					"NÚMERO DE VOLTAS DEVE SER INFORMADO!!!",
					"SetUP Não preenchido corretamente.",
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
					.key((String) boxPneuInicial.getSelectedItem()),
					spinnerCombustivelInicial.getValue(),
					nomeJogador.getText(), Lang.key((String) comboBoxAsa
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
				painelInicio, "Setup Inicial", JOptionPane.YES_NO_OPTION);
		if (ret == JOptionPane.NO_OPTION) {
			return false;
		}
		while ((((Integer) spinnerQtdeVoltas.getValue()).intValue() < 21)
				|| (((Integer) spinnerCombustivelInicial.getValue()).intValue() == 0)) {
			JOptionPane.showMessageDialog(controleJogo.getMainFrame(),
					"NÚMERO DE VOLTAS DEVE SER INFORMADO!!!",
					"SetUP Não preenchido corretamente.",
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
					.key((String) boxPneuInicial.getSelectedItem()),
					spinnerCombustivelInicial.getValue(),
					nomeJogador.getText(), Lang.key((String) comboBoxAsa
							.getSelectedItem()));
		}

		return true;
	}

	public void desenhaQualificacao() {
		infoCorrida.setText("Informações sobre a Corrida");
		infoPiloto.setText("Informações sobre o Piloto");
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
		bufferTextual.add(Html.cinza("Volta " + controleJogo.getNumVoltaAtual()
				+ " ")
				+ string + "<br>");
		StringBuffer buffer = new StringBuffer();
		for (int i = bufferTextual.size() - 1; i >= 0; i--) {
			buffer.append(Html.sansSerif(bufferTextual.get(i).toString()));
		}
		StringReader reader = new StringReader(buffer.toString());
		try {
			infoTextual.read(reader, "");
		} catch (IOException e) {
			// e.printStackTrace();
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
}
