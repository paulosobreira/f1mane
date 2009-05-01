package sowbreira.f1mane;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.editor.MainPanelEditor;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;

/**
 * @author Paulo Sobreira Created on 31/12/2004
 */
public class MainFrame extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -284357233387917389L;
	private MainPanelEditor editor;
	private InterfaceJogo controleJogo;
	private boolean modoApplet;
	private JRadioButtonMenuItem t2007 = new JRadioButtonMenuItem() {
		public String getText() {
			return Lang.msg("085");
		}

	};
	private JRadioButtonMenuItem t2008 = new JRadioButtonMenuItem() {
		public String getText() {
			return Lang.msg("086");
		}

	};

	private JRadioButtonMenuItem t2009 = new JRadioButtonMenuItem() {
		public String getText() {
			return Lang.msg("087");
		}

	};
	private JMenuBar bar;
	private JMenu menuJogo;
	private JMenu menuEditor;
	private JMenu menuInfo;

	public MainFrame(boolean modoApplet) throws IOException {
		this.modoApplet = modoApplet;
		bar = new JMenuBar();
		setJMenuBar(bar);

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

		menuEditor = new JMenu() {
			public String getText() {
				return Lang.msg("090");
			}

		};
		bar.add(menuEditor);
		if (modoApplet) {
			menuEditor.setEnabled(false);
		}

		gerarMenusSingle(menuJogo);
		gerarMenusEditor(menuEditor);
		gerarMenusInfo(menuInfo);
		gerarMenusSobre(menuInfo);
		getContentPane().setLayout(null);
		setSize(800, 630);
		setTitle("F1-Mane 1.8");
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
					e1.printStackTrace();
				}
				area.setCaretPosition(0);
				JOptionPane
						.showMessageDialog(MainFrame.this,
								new JScrollPane(area), Lang.msg("091"),
								JOptionPane.INFORMATION_MESSAGE);
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
						exibirResiltadoFinal(controleJogo.obterResultadoFinal());
					}

					removerKeyListeners();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		menuInfo2.add(resFinal);

	}

	private void gerarMenusSobre(JMenu menu2) {
		JMenuItem sobre = new JMenuItem("Sobre o autor do jogo") {
			public String getText() {
				return Lang.msg("093");
			}

		};
		sobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = "Feito por Paulo Sobreira \n sowbreira@yahoo.com.br \n"
						+ "http://br.geocities.com/sowbreira/ \n"
						+ "Iniciado em Maio de 2007";
				JOptionPane.showMessageDialog(MainFrame.this, msg, Lang
						.msg("093"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu2.add(sobre);
	}

	private void gerarMenusSingle(JMenu menu1) {
		JMenuItem iniciar = new JMenuItem("Iniciar Jogo") {
			public String getText() {
				return Lang.msg("094");
			}

		};
		iniciar.setEnabled(false);
		iniciar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removerKeyListeners();
					if (controleJogo != null) {
						int ret = JOptionPane.showConfirmDialog(MainFrame.this,
								Lang.msg("095"), Lang.msg("094"),
								JOptionPane.YES_NO_OPTION);
						if (ret == JOptionPane.NO_OPTION) {
							return;
						}
						controleJogo.matarTodasThreads();
					}
					String temporarada = null;
					if (getT2007().isSelected()) {
						temporarada = "t2007";

					} else if (getT2008().isSelected()) {
						temporarada = "t2008";
					} else if (getT2009().isSelected()) {
						temporarada = "t2009";
					}

					controleJogo = new ControleJogoLocal(temporarada);
					controleJogo.setMainFrame(MainFrame.this);
					controleJogo.iniciarJogo();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		// menu1.add(iniciar);
		JMenuItem iniciarSimples = new JMenuItem("Iniciar Jogo") {
			public String getText() {
				return Lang.msg("094");
			}

		};

		iniciarSimples.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					removerKeyListeners();
					if (controleJogo != null) {
						int ret = JOptionPane.showConfirmDialog(MainFrame.this,
								Lang.msg("095"), Lang.msg("094"),
								JOptionPane.YES_NO_OPTION);
						if (ret == JOptionPane.NO_OPTION) {
							return;
						}
						controleJogo.matarTodasThreads();
					}
					String temporarada = null;
					if (getT2007().isSelected()) {
						temporarada = "t2007";

					} else if (getT2008().isSelected()) {
						temporarada = "t2008";
					}

					controleJogo = new ControleJogoLocal(temporarada);
					controleJogo.setMainFrame(MainFrame.this);
					controleJogo.iniciarJogo();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		menu1.add(iniciarSimples);
		JMenuItem pausa = new JMenuItem("Pausa Jogo") {
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
					ex.printStackTrace();
				}
			}
		});
		menu1.add(pausa);
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(t2007);
		buttonGroup.add(t2008);
		buttonGroup.add(t2009);
		menu1.add(t2007);
		menu1.add(t2008);
		menu1.add(t2009);
		t2009.setSelected(true);

	}

	private void ativarKeysEditor() {
		removerKeyListeners();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();

				if (keyCoode == KeyEvent.VK_DELETE) {
					editor.apagarUltimoNo();
				}

				if ((keyCoode == KeyEvent.VK_DELETE)
						&& (e.getModifiers() == KeyEvent.SHIFT_MASK)) {
					editor.apagarUltimoNoPista();
				}

				if ((keyCoode == KeyEvent.VK_DELETE)
						&& (e.getModifiers() == KeyEvent.CTRL_MASK)) {
					editor.apagarUltimoNoBox();
				}

				if (keyCoode == KeyEvent.VK_F1) {
					editor.inserirNoLargada();
				}

				if (keyCoode == KeyEvent.VK_F2) {
					editor.inserirNoReta();
				}

				if (keyCoode == KeyEvent.VK_F3) {
					editor.inserirNoCurvaAlta();
				}

				if (keyCoode == KeyEvent.VK_F4) {
					editor.inserirNoCurvaBaixa();
				}

				if (keyCoode == KeyEvent.VK_F5) {
					editor.inserirNoBox();
				}

				if (keyCoode == KeyEvent.VK_F6) {
					editor.inserirNoParadaBox();
				}
			}
		});
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

					editor = new MainPanelEditor(MainFrame.this);
					ativarKeysEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
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
					editor = new MainPanelEditor(file.getName(), MainFrame.this);
					ativarKeysEditor();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menu4.add(abrirImg);

		JMenuItem inserirNoLargada = new JMenuItem("Inserir no Largada (F1)") {
			public String getText() {
				return Lang.msg("099");
			}

		};
		inserirNoLargada.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoLargada();
			}
		});
		menu4.add(inserirNoLargada);

		JMenuItem inserirNoReta = new JMenuItem("Inserir No Reta (F2)") {
			public String getText() {
				return Lang.msg("100");
			}

		};
		inserirNoReta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoReta();
			}
		});
		menu4.add(inserirNoReta);

		JMenuItem inserirNoCurvaAlta = new JMenuItem(
				"Inserir No Curva Alta (F3)") {
			public String getText() {
				return Lang.msg("101");
			}

		};
		inserirNoCurvaAlta.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoCurvaAlta();
			}
		});
		menu4.add(inserirNoCurvaAlta);

		JMenuItem inserirNoCurvaBaixa = new JMenuItem(
				"Inserir No Curva Baixa (F4)") {
			public String getText() {
				return Lang.msg("102");
			}

		};
		inserirNoCurvaBaixa.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoCurvaBaixa();
			}
		});
		menu4.add(inserirNoCurvaBaixa);

		JMenuItem inserirNoEntradaBox = new JMenuItem("Inserir No Box (F5)") {
			public String getText() {
				return Lang.msg("103");
			}

		};
		inserirNoEntradaBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoBox();
			}
		});
		menu4.add(inserirNoEntradaBox);

		JMenuItem inserirNoParadaBox = new JMenuItem(
				"Inserir No Parada Box (F6)") {
			public String getText() {
				return Lang.msg("104");
			}

		};
		inserirNoParadaBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				editor.inserirNoParadaBox();
			}
		});
		menu4.add(inserirNoParadaBox);

		JMenuItem apagarUltimoNo = new JMenuItem("Apagar ultimo NO (DEL)") {
			public String getText() {
				return Lang.msg("105");
			}

		};
		apagarUltimoNo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.apagarUltimoNo();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menu4.add(apagarUltimoNo);

		JMenuItem apagarUltimoNoPista = new JMenuItem(
				"Apagar ultimo NO (CTRL+DEL)") {
			public String getText() {
				return Lang.msg("106");
			}

		};
		apagarUltimoNoPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.apagarUltimoNoPista();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menu4.add(apagarUltimoNoPista);

		JMenuItem apagarUltimoNoBox = new JMenuItem(
				"Apagar ultimo NO (SHIFT+DEL)"){
			public String getText() {
				return Lang.msg("107");
			}

		};
		apagarUltimoNoBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.apagarUltimoNoBox();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		menu4.add(apagarUltimoNoBox);

		JMenuItem salvarPista = new JMenuItem("Salvar Pista F8"){
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
	}

	public static void main(String[] args) throws IOException {
		MainFrame frame = new MainFrame(false);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.iniciar();
	}

	public void iniciar() {
		if (ControleJogoLocal.VALENDO) {
			setVisible(true);
		} else {
			try {
				String temporarada = null;
				if (getT2007().isSelected()) {
					temporarada = "t2007";
				} else if (getT2008().isSelected()) {
					temporarada = "t2008";
				} else if (getT2009().isSelected()) {
					temporarada = "t2009";
				}
				controleJogo = new ControleJogoLocal(temporarada);
				controleJogo.setMainFrame(this);
				controleJogo.iniciarJogo();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public void exibirResiltadoFinal(PainelTabelaResultadoFinal resultadoFinal) {

		JOptionPane.showMessageDialog(this, new JScrollPane(resultadoFinal),
				"Resultado Final. ", JOptionPane.INFORMATION_MESSAGE);

	}

	public boolean isModoApplet() {
		return modoApplet;
	}

	public void setModoApplet(boolean modoApplet) {
		this.modoApplet = modoApplet;
	}

	public void desbilitarMenusModoOnline() {
		menuJogo.setEnabled(false);
		menuEditor.setEnabled(false);

	}

	public void setControleJogo(InterfaceJogo controleJogo) {
		this.controleJogo = controleJogo;
	}

	public JRadioButtonMenuItem getT2007() {
		return t2007;
	}

	public JRadioButtonMenuItem getT2008() {
		return t2008;
	}

	public JRadioButtonMenuItem getT2009() {
		return t2009;
	}

}
