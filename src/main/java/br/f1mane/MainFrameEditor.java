package br.f1mane;

import java.awt.Container;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.f1mane.controles.InterfaceJogo;
import br.f1mane.editor.MainPanelEditor;
import br.f1mane.paddock.applet.AppletPaddock;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.PainelTabelaResultadoFinal;

/**
 * @author Paulo Sobreira Created on 14/06/2014
 */
public class MainFrameEditor extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -284357233387917389L;
	private MainPanelEditor editor;
	private InterfaceJogo controleJogo;
	private final JMenuBar bar;
	private final JMenu menuEditor;
	private final JMenu menuIdiomas;
	private final JMenu menuInfo;

	private final AppletPaddock ver = new AppletPaddock();
	protected boolean altApertado;
	protected boolean shiftApertado;

	public InterfaceJogo getControleJogo() {
		return controleJogo;
	}

	public MainFrameEditor() throws IOException {
		bar = new JMenuBar();
		this.setJMenuBar(bar);

		menuEditor = new JMenu() {
			public String getText() {
				return Lang.msg("090");
			}

		};
		bar.add(menuEditor);

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
		gerarMenusEditor(menuEditor);
		gerarMenusInfo(menuInfo);
		gerarMenusSobre(menuInfo);
		gerarMenusidiomas(menuIdiomas);
		String title = "Fl-MANE " + getVersao() + " MANager & Engineer Editor";
		setTitle(title);
		removerListeners();
		removerKeyListeners();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		final BufferedImage bg = ImageUtil.gerarFade(
				CarregadorRecursos.carregaBufferedImage("png/bg-monaco.png"), 25);
		setSize(bg.getWidth(), bg.getHeight());
		JPanel jPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.drawImage(bg, 0, 0, null);
			}
		};
		getContentPane().add(jPanel);
		this.setVisible(true);

	}

	private String getVersao() {
		return ver.getVersaoFormatado();
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
				SwingUtilities.updateComponentTreeUI(MainFrameEditor.this);
			}
		});
		en.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Lang.mudarIdioma("en");
				SwingUtilities.updateComponentTreeUI(MainFrameEditor.this);
			}
		});

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(pt);
		buttonGroup.add(en);
		menuIdiomas.add(pt);
		menuIdiomas.add(en);

	}

	private void gerarMenusInfo(JMenu menuInfo2) {
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
					JOptionPane.showMessageDialog(MainFrameEditor.this,
							new JScrollPane(area), Lang.msg("listaDeErros"),
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					ex.printStackTrace();
					dialogDeErro(ex);
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

	public void dialogDeErro(Exception e) {
		StackTraceElement[] trace = e.getStackTrace();
		StringBuilder retorno = new StringBuilder();
		int size = ((trace.length > 10) ? 10 : trace.length);

		for (int i = 0; i < size; i++)
			retorno.append(trace[i]).append("\n");
		JOptionPane.showMessageDialog(null, retorno.toString(),
				Lang.msg("059"), JOptionPane.ERROR_MESSAGE);
		
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
		JOptionPane.showMessageDialog(MainFrameEditor.this, msg,
				Lang.msg("093"), JOptionPane.INFORMATION_MESSAGE);
	}

	private void ativarKeysEditor() {
		removerKeyListeners();
		this.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();
				if (editor == null) {
					return;
				}
				if (e.isAltDown()) {
					altApertado = true;
					if (keyCoode == KeyEvent.VK_LEFT) {
						editor.esquerdaObj();
					} else if (keyCoode == KeyEvent.VK_RIGHT) {
						editor.direitaObj();
					} else if (keyCoode == KeyEvent.VK_UP) {
						editor.cimaObj();
					} else if (keyCoode == KeyEvent.VK_DOWN) {
						editor.baixoObj();
					} else if (keyCoode == KeyEvent.VK_C) {
						editor.copiarObjeto();
					}
					return;
				} else {
					altApertado = false;
				}
				if (e.isShiftDown()) {
					shiftApertado = true;
					if (keyCoode == KeyEvent.VK_RIGHT) {
						editor.maisLargura();
					} else if (keyCoode == KeyEvent.VK_LEFT) {
						editor.menosLargura();
					} else if (keyCoode == KeyEvent.VK_UP) {
						editor.maisAltura();
					} else if (keyCoode == KeyEvent.VK_DOWN) {
						editor.menosAltura();
					}
					return;
				} else {
					shiftApertado = false;
				}
				if (keyCoode == KeyEvent.VK_LEFT) {
					editor.esquerda();
				} else if (keyCoode == KeyEvent.VK_RIGHT) {
					editor.direita();
				} else if (keyCoode == KeyEvent.VK_UP) {
					editor.cima();
				} else if (keyCoode == KeyEvent.VK_DOWN) {
					editor.baixo();
				} else if (keyCoode == KeyEvent.VK_Z) {
					editor.menosAngulo();
				} else if (keyCoode == KeyEvent.VK_X) {
					editor.maisAngulo();
				}
			}
		});
	}

	public boolean isAltApertado() {
		return altApertado;
	}

	public boolean isShiftApertado() {
		return shiftApertado;
	}

	private void removerKeyListeners() {
		KeyListener[] listeners = getKeyListeners();
		for (int i = 0; i < listeners.length; i++) {
			removeKeyListener(listeners[i]);
		}
	}

	private void gerarMenusEditor(Container menu4) {
		JMenuItem abrirPista = new JMenuItem("Editar Arquivo Circuito") {
			public String getText() {
				return Lang.msg("097");
			}

		};
		abrirPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (controleJogo != null) {
						controleJogo.matarTodasThreads();
					}
					editor = new MainPanelEditor(MainFrameEditor.this);
					ativarKeysEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
					dialogDeErro(e1);
				}
			}
		});
		menu4.add(abrirPista);

		JMenuItem abrirImg = new JMenuItem("Criar Arquivo Circuito") {
			public String getText() {
				return Lang.msg("098");
			}

		};
		abrirImg.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (controleJogo != null) {
						controleJogo.matarTodasThreads();
					}

					JFileChooser fileChooser = new JFileChooser(
							CarregadorRecursos.class.getResource(
									"CarregadorRecursos.class").getFile());
					fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

					int result = fileChooser.showOpenDialog(null);

					if (result == JFileChooser.CANCEL_OPTION) {
						return;
					}

					File file = fileChooser.getSelectedFile();
					editor = new MainPanelEditor(file.getName(),
							MainFrameEditor.this);
					ativarKeysEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
					dialogDeErro(e1);
				}
			}
		});
		menu4.add(abrirImg);

		JMenuItem salvarPista = new JMenuItem("Salvar Pista F8") {
			public String getText() {
				return Lang.msg("108");
			}

		};
		salvarPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.salvarPista();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menu4.add(salvarPista);

		JMenuItem vetorizarPista = new JMenuItem("vetorizarPista") {
			public String getText() {
				return Lang.msg("reprocessarPista");
			}

		};
		vetorizarPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					if (controleJogo != null) {
						controleJogo.matarTodasThreads();
					}
					ativarKeysEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
					dialogDeErro(e1);
				}

			}
		});
		menu4.add(vetorizarPista);
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
		MainFrameEditor frame = new MainFrameEditor();
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

	public void exibirResiltadoFinal(PainelTabelaResultadoFinal resultadoFinal) {

		JOptionPane.showMessageDialog(this, new JScrollPane(resultadoFinal),
				"Resultado Final. ", JOptionPane.INFORMATION_MESSAGE);

	}

	public void setControleJogo(InterfaceJogo controleJogo) {
		this.controleJogo = controleJogo;
	}

}
