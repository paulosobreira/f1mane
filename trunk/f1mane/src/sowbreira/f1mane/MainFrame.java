package sowbreira.f1mane;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferStrategy;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.paddock.applet.AppletPaddock;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.ControleSom;
import sowbreira.f1mane.visao.PainelMenuLocal;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Created on 20/06/2007
 */
public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -284357233387917389L;
	protected InterfaceJogo controleJogo;
	private String codeBase;
	private JMenuBar bar;
	private JMenu menuJogo;
	private JMenu menuIdiomas;
	private JMenu menuInfo;
	private JCheckBoxMenuItem som;
	private JMenuItem iniciar;
	private JMenuItem pausa;
	private JMenuItem narracao;
	private JMenuItem verControles;
	private JFrame menuFrame;
	protected Campeonato campeonato;

	private AppletPaddock ver = new AppletPaddock();

	public InterfaceJogo getControleJogo() {
		return controleJogo;
	}

	public MainFrame(JApplet modoApplet, String codeBase) throws IOException {
		this.codeBase = codeBase;
		bar = new JMenuBar();
		menuFrame = new JFrame();
		menuFrame.setJMenuBar(bar);
		menuJogo = new JMenu() {
			public String getText() {
				return Lang.msg("088");
			}

		};
		bar.add(menuJogo);

		menuInfo = new JMenu() {
			public String getText() {
				return Lang.msg("089");
			}

		};
		bar.add(menuInfo);

		menuIdiomas = new JMenu() {
			public String getText() {
				return Lang.msg("219");
			}

		};
		bar.add(menuIdiomas);
		gerarMenusSingle(menuJogo);
		gerarMenusInfo(menuInfo);
		gerarMenusSobre(menuInfo);
		gerarMenusidiomas(menuIdiomas);
		setSize(1030, 720);
		String title = "F1-MANE " + getVersao() + " MANager & Engineer";
		setTitle(title);
		if (modoApplet == null) {
			iniciar();
			setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		} else {
			setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		}
		removerKeyListeners();
	}

	public MainFrame() {
	}

	public String getVersao() {
		return ver.getVersao();
	}

	private void gerarMenusidiomas(JMenu menuIdiomas) {
		JRadioButtonMenuItem pt = new JRadioButtonMenuItem() {
			public String getText() {
				return Lang.msg("pt");
			}

		};
		JRadioButtonMenuItem en = new JRadioButtonMenuItem() {
			public String getText() {
				return Lang.msg("en");
			}

		};
		pt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Lang.mudarIdioma("pt");
				SwingUtilities.updateComponentTreeUI(MainFrame.this);
			}
		});
		en.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Lang.mudarIdioma("en");
				SwingUtilities.updateComponentTreeUI(MainFrame.this);
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(pt);
		buttonGroup.add(en);
		menuIdiomas.add(pt);
		menuIdiomas.add(en);

	}

	private void gerarMenusInfo(JMenu menuInfo2) {
		JMenuItem leiaMe = new JMenuItem("Leia-Me") {
			public String getText() {
				return Lang.msg("091");
			}

		};
		leiaMe.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JTextArea area = new JTextArea(20, 50);
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(CarregadorRecursos
								.recursoComoStream("leiame.txt")));
				try {
					String linha = reader.readLine();
					while (linha != null) {
						area.append(linha + "\n");
						linha = reader.readLine();
					}
				} catch (IOException e1) {
					Logger.logarExept(e1);
				}
				area.setCaretPosition(0);
				JOptionPane.showMessageDialog(MainFrame.this, new JScrollPane(
						area), Lang.msg("091"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menuInfo2.add(leiaMe);
		JMenuItem resFinal = new JMenuItem("Resultado Corrida") {
			public String getText() {
				return Lang.msg("092");
			}

		};
		resFinal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (controleJogo != null) {
						exibirResultadoFinal(controleJogo.obterResultadoFinal());
					}
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});
		menuInfo2.add(resFinal);
		JMenuItem logs = new JMenuItem("Ver Logs") {
			public String getText() {
				return Lang.msg("267");
			}

		};
		logs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					JTextArea area = new JTextArea(20, 50);
					Set top = Logger.topExceptions.keySet();
					for (Iterator iterator = top.iterator(); iterator.hasNext();) {
						String exept = (String) iterator.next();
						area.append("Quantidade : "
								+ Logger.topExceptions.get(exept));
						area.append("\n");
						area.append(exept.replaceAll("<br>", "\n"));
						area.append("\n");
					}
					area.setCaretPosition(0);
					JOptionPane.showMessageDialog(MainFrame.this,
							new JScrollPane(area), Lang.msg("listaDeErros"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});
		menuInfo2.add(logs);

		JMenuItem ligarLogs = new JMenuItem("ativarLogs") {
			public String getText() {
				return Lang.msg("ativarLogs");
			}

		};
		ligarLogs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Logger.ativo = !Logger.ativo;
			}
		});
		menuInfo2.add(ligarLogs);

	}

	private void gerarMenusSobre(JMenu menu2) {
		JMenuItem sobre = new JMenuItem("Sobre o autor do jogo") {
			public String getText() {
				return Lang.msg("093");
			}

		};
		sobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mostraSobre();
			}

		});
		menu2.add(sobre);
	}

	public void mostraSobre() {
		String msg = Lang.msg("184") + " Paulo Sobreira        ".trim() + "\n"
				+ "- " + Lang.msg("pistas") + " "
				+ " www.miniracingonline.com                   ".trim() + "\n"
				+ "- " + Lang.msg("capacetesCarros") + " "
				+ " spotterguidecentral.com                    ".trim() + "\n"
				+ "- http://sowbreira.appspot.com              ".trim() + "\n"
				+ "- sowbreira@gmail.com                       ".trim() + "\n"
				+ "- 2007-2014";
		JOptionPane.showMessageDialog(MainFrame.this, msg, Lang.msg("093"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void gerarMenusSingle(JMenu menu1) {
		iniciar = new JMenuItem("Iniciar Jogo") {
			public String getText() {
				return Lang.msg("094");
			}

		};

		iniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removerKeyListeners();

					if (!verificaCriarJogo()) {
						return;
					}
					controleJogo.setMainFrame(MainFrame.this);
					controleJogo.iniciarJogo();
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}

		});
		menu1.add(iniciar);

		pausa = new JMenuItem("Pausa Jogo") {
			public String getText() {
				return Lang.msg("096");
			}

		};
		pausa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (controleJogo != null) {
						controleJogo.pausarJogo();
					}

				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});
		menu1.add(pausa);

		som = new JCheckBoxMenuItem("Som") {
			public String getText() {
				return Lang.msg("som");
			}

		};
		som.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (som.isSelected()) {
					ControleSom.somLigado = true;
				} else {
					ControleSom.somLigado = false;
				}
			}
		});
		menu1.add(som);
		narracao = new JMenuItem("compsSwing") {
			public String getText() {
				return Lang.msg("f1maneSwing");
			}

		};
		narracao.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						controleJogo.mostraCompsSwing();
					}
				}).start();

			}
		});
		menu1.add(narracao);

		verControles = new JMenuItem("verControles") {
			public String getText() {
				return Lang.msg("verControles");
			}

		};
		verControles.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				controleJogo.ativaVerControles();
			}
		});
		menu1.add(verControles);

	}

	private void removerKeyListeners() {
		KeyListener[] listeners = getKeyListeners();
		for (int i = 0; i < listeners.length; i++) {
			removeKeyListener(listeners[i]);
		}
		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();
				if (keyCoode == KeyEvent.VK_1 && e.isControlDown()) {
					mostraMenuFrame();
				}
				super.keyPressed(e);
			}
		});

	}

	public static void main(String[] args) throws IOException {
		// Logger.ativo = true;
		String codeBase = File.separator + "WebContent" + File.separator;
		if (args != null && args.length > 0) {
			codeBase = args[0];
		}
		if (args != null && args.length > 1) {
			Lang.mudarIdioma(args[1]);
		}
		MainFrame frame = new MainFrame(null, codeBase);
	}

	public void iniciar() {
		removerListeners();
		setVisible(true);
		try {
			controleJogo = new ControleJogoLocal();
			controleJogo.setMainFrame(this);
			PainelMenuLocal painelMenuSigle = new PainelMenuLocal(this);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	private void removerListeners() {
		getContentPane().removeAll();
		MouseWheelListener[] mouseWheelListeners = getMouseWheelListeners();
		for (int i = 0; i < mouseWheelListeners.length; i++) {
			removeMouseWheelListener(mouseWheelListeners[i]);
		}
		KeyListener[] keyListeners = getKeyListeners();
		for (int i = 0; i < keyListeners.length; i++) {
			removeKeyListener(keyListeners[i]);
		}
		MouseListener[] mouseListeners = getMouseListeners();
		for (int i = 0; i < mouseListeners.length; i++) {
			removeMouseListener(mouseListeners[i]);
		}
	}

	public void exibirResultadoFinal(PainelTabelaResultadoFinal resultadoFinal) {

		JOptionPane.showMessageDialog(this, new JScrollPane(resultadoFinal),
				"Resultado Final. ", JOptionPane.INFORMATION_MESSAGE);

	}

	public String getCodeBase() {
		return codeBase;
	}

	public void desbilitarMenusModoOnline() {
		if (iniciar != null) {
			iniciar.setEnabled(false);
		}
		if (pausa != null) {
			pausa.setEnabled(false);
		}
	}

	public void setControleJogo(InterfaceJogo controleJogo) {
		this.controleJogo = controleJogo;
	}

	public boolean verificaCriarJogo() throws Exception {
		if (controleJogo != null) {
			if (controleJogo.isCorridaIniciada()) {
				int ret = JOptionPane.showConfirmDialog(MainFrame.this,
						Lang.msg("095"), Lang.msg("094"),
						JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.NO_OPTION) {
					return false;
				}
			}
			controleJogo.matarTodasThreads();
		}
		controleJogo = new ControleJogoLocal(this);
		controleJogo.setMainFrame(this);
		return true;
	}

	public Campeonato getCampeonato() {
		return campeonato;
	}

	public void setCampeonato(Campeonato campeonato) {
		this.campeonato = campeonato;
	}

	public Graphics2D obterGraficos() {
		BufferStrategy strategy = getBufferStrategy();
		if (strategy == null) {
			createBufferStrategy(2);
			strategy = getBufferStrategy();
		}
		return (Graphics2D) strategy.getDrawGraphics();
	}

	public void mostrarGraficos() {
		BufferStrategy strategy = getBufferStrategy();
		strategy.getDrawGraphics().dispose();
		strategy.show();
	}

	public void mostraMenuFrame() {
		if (menuFrame != null) {
			menuFrame.setVisible(!menuFrame.isVisible());
			if (menuFrame.isVisible()) {
				menuFrame.pack();
			}
		}

	}
}
