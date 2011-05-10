package sowbreira.f1mane;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import sowbreira.f1mane.controles.ControleCampeonato;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.editor.MainPanelEditor;
import sowbreira.f1mane.editor.MainPanelEditorVetorizado;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.ControleSom;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Logger;

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
	private ControleCampeonato controleCampeonato;
	private JApplet applet;
	private JMenuBar bar;
	private JMenu menuJogo;
	private JMenu menuCampeonato;
	private JMenu menuEditor;
	private JMenu menuIdiomas;
	private JMenu menuInfo;
	private JCheckBoxMenuItem atualizacaoSuave;
	private JCheckBoxMenuItem som;
	protected MainPanelEditorVetorizado editorInflado;
	private JMenuItem iniciar;
	private JMenuItem pausa;
	private boolean appletStand;
	public static BufferedImage bg;

	public InterfaceJogo getControleJogo() {
		return controleJogo;
	}

	public JApplet getApplet() {
		return applet;
	}

	public boolean isAtualizacaoSuave() {
		if (atualizacaoSuave == null) {
			return false;
		}
		return atualizacaoSuave.isSelected();
	}

	public MainFrame(JApplet modoApplet, boolean appletStand)
			throws IOException {
		this.applet = modoApplet;
		this.appletStand = appletStand;
		controleCampeonato = new ControleCampeonato(this);
		bar = new JMenuBar();
		if (appletStand) {
			applet.getRootPane().setMenuBar(bar);
		} else {
			setJMenuBar(bar);
		}

		menuJogo = new JMenu() {
			public String getText() {
				return Lang.msg("088");
			}

		};

		bar.add(menuJogo);

		menuCampeonato = new JMenu() {
			public String getText() {
				return Lang.msg("268");
			}

		};

		bar.add(menuCampeonato);

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
		menuIdiomas = new JMenu() {
			public String getText() {
				return Lang.msg("219");
			}

		};
		bar.add(menuIdiomas);

		if (modoApplet != null) {
			menuEditor.setEnabled(false);

		}

		gerarMenusSingle(menuJogo);
		gerarMenusCampeonato(menuCampeonato);
		gerarMenusEditor(menuEditor);
		gerarMenusInfo(menuInfo);
		gerarMenusSobre(menuInfo);
		gerarMenusidiomas(menuIdiomas);
		setSize(1030, 720);
		String title = "F1-MANE " + getVersao() + " MANager & Engineer";
		if (this.appletStand) {
			Component parent = applet;
			while (parent.getParent() != null)
				parent = parent.getParent();
			if (parent instanceof Frame) {
				((Frame) parent).setTitle(title);
			}
		} else {
			setTitle(title);
		}

	}

	private String getVersao() {
		if (this.appletStand) {
			if (applet instanceof F1ManeApplet) {
				F1ManeApplet f1ManeApplet = (F1ManeApplet) applet;
				return f1ManeApplet.getVersao();
			}

			Component parent = applet;

			return "";
		} else {
			return " Debug ";
		}

	}

	private void gerarMenusCampeonato(JMenu menu) {
		JMenuItem criarCampeonato = new JMenuItem("Criar Campeonato") {
			public String getText() {
				return Lang.msg("campeonatoEquipes");
			}

		};
		criarCampeonato.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					controleCampeonato.criarCampeonato();
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});

		JMenuItem criarCampeonatoPiloto = new JMenuItem("campeonatoPiloto") {
			public String getText() {
				return Lang.msg("campeonatoPiloto");
			}

		};
		criarCampeonatoPiloto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					controleCampeonato.criarCampeonatoPiloto();
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});

		JMenuItem continuarCampeonato = new JMenuItem("Continuar Campeonato") {
			public String getText() {
				return Lang.msg("270");
			}

		};
		continuarCampeonato.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controleCampeonato.continuarCampeonato();
			}
		});
		JMenuItem dadosPersistencia = new JMenuItem("Criar Campeonato") {
			public String getText() {
				return Lang.msg("271");
			}

		};
		dadosPersistencia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controleCampeonato.dadosPersistencia();
			}
		});
		JMenuItem proxCorrida = new JMenuItem("Proxima Corrida") {
			public String getText() {
				return Lang.msg("286");
			}

		};
		proxCorrida.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controleCampeonato.proximaCorrida();
			}
		});

		menu.add(criarCampeonato);
		menu.add(criarCampeonatoPiloto);
		menu.add(continuarCampeonato);
		menu.add(dadosPersistencia);
		menu.add(proxCorrida);

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
					removerKeyListeners();
				} catch (Exception ex) {
					Logger.logarExept(ex);
				}
			}
		});
		menuInfo2.add(logs);

	}

	private void gerarMenusSobre(JMenu menu2) {
		JMenuItem sobre = new JMenuItem("Sobre o autor do jogo") {
			public String getText() {
				return Lang.msg("093");
			}

		};
		sobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = Lang.msg("184")
						+ " Paulo Sobreira \n sowbreira@gmail.com \n"
						+ "http://sowbreira.appspot.com \n" + "2007-2011";
				JOptionPane.showMessageDialog(MainFrame.this, msg, Lang
						.msg("093"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		menu2.add(sobre);
	}

	@Override
	public Container getContentPane() {
		if (appletStand) {
			return applet.getContentPane();
		}
		return super.getContentPane();
	}

	@Override
	public JRootPane getRootPane() {
		if (appletStand) {
			return applet.getRootPane();
		}
		return super.getRootPane();
	}

	@Override
	public synchronized void addWindowListener(WindowListener l) {
		if (appletStand) {
			Component parent = applet;
			while (parent.getParent() != null)
				parent = parent.getParent();
			if (parent instanceof Frame) {
				((Frame) parent).addWindowListener(l);
			}
		}
		super.addWindowListener(l);
	}

	@Override
	public synchronized void addKeyListener(KeyListener l) {
		if (appletStand) {
			applet.addKeyListener(l);
		} else {
			super.addKeyListener(l);
		}
	}

	@Override
	public Container getParent() {
		if (appletStand) {
			return applet.getParent();
		}
		return super.getParent();
	}

	@Override
	public synchronized void addMouseWheelListener(MouseWheelListener l) {
		if (appletStand) {
			applet.addMouseWheelListener(l);
		} else {
			super.addMouseWheelListener(l);
		}
	}

	public boolean isAppletStand() {
		return appletStand;
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
					if (controleJogo != null) {
						if (controleJogo.isCorridaIniciada()) {
							int ret = JOptionPane.showConfirmDialog(
									MainFrame.this, Lang.msg("095"), Lang
											.msg("094"),
									JOptionPane.YES_NO_OPTION);
							if (ret == JOptionPane.NO_OPTION) {
								return;
							}
						}
						controleJogo.matarTodasThreads();
					}
					controleJogo = new ControleJogoLocal();
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

		atualizacaoSuave = new JCheckBoxMenuItem("Atualizacao Suave") {
			public String getText() {
				return Lang.msg("atualizacaoSuave");
			}

		};
		atualizacaoSuave.setSelected(true);
		atualizacaoSuave.setEnabled(false);
		menu1.add(atualizacaoSuave);
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
	}

	protected void selecionarTemporada() {
		// carregarTemporadas();
		// JComboBox temporadasCombo = new JComboBox(vectorTemps);
		// JOptionPane.showMessageDialog(this, temporadasCombo, Lang.msg("272"),
		// JOptionPane.QUESTION_MESSAGE);
		// temporarada = (String) temporadas
		// .get(temporadasCombo.getSelectedItem());
	}

	private void ativarKeysEditor() {
		removerKeyListeners();
		addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				int keyCoode = e.getKeyCode();
				if (editor != null) {
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
					if (keyCoode == KeyEvent.VK_LEFT) {
						editor.esquerda();
					} else if (keyCoode == KeyEvent.VK_RIGHT) {
						editor.direita();
					} else if (keyCoode == KeyEvent.VK_UP) {
						editor.cima();
					} else if (keyCoode == KeyEvent.VK_DOWN) {
						editor.baixo();
					}
				}
				if (editorInflado != null) {
					if (e.isControlDown() && keyCoode == KeyEvent.VK_LEFT) {
						editorInflado.esquerdaObj();
					} else if (e.isControlDown()
							&& keyCoode == KeyEvent.VK_RIGHT) {
						editorInflado.direitaObj();
					} else if (e.isControlDown() && keyCoode == KeyEvent.VK_UP) {
						editorInflado.cimaObj();
					} else if (e.isControlDown()
							&& keyCoode == KeyEvent.VK_DOWN) {
						editorInflado.baixoObj();
					} else if (keyCoode == KeyEvent.VK_LEFT) {
						editorInflado.esquerda();
					} else if (keyCoode == KeyEvent.VK_RIGHT) {
						editorInflado.direita();
					} else if (keyCoode == KeyEvent.VK_UP) {
						editorInflado.cima();
					} else if (keyCoode == KeyEvent.VK_DOWN) {
						editorInflado.baixo();
					} else if (e.isShiftDown()
							&& keyCoode == KeyEvent.VK_PAGE_UP) {
						editorInflado.maisLargura();
					} else if (e.isShiftDown()
							&& keyCoode == KeyEvent.VK_PAGE_DOWN) {
						editorInflado.menosLargura();
					} else if (e.isControlDown()
							&& keyCoode == KeyEvent.VK_PAGE_UP) {
						editorInflado.maisAltura();
					} else if (e.isControlDown()
							&& keyCoode == KeyEvent.VK_PAGE_DOWN) {
						editorInflado.menosAltura();
					} else if (e.isControlDown() && keyCoode == KeyEvent.VK_C) {
						editorInflado.copiarObjeto();
					} else if (keyCoode == KeyEvent.VK_PAGE_UP) {
						editorInflado.menosAngulo();
					} else if (keyCoode == KeyEvent.VK_PAGE_DOWN) {
						editorInflado.maisAngulo();
					}

				}
			}
		});
	}

	@Override
	public synchronized KeyListener[] getKeyListeners() {
		if (appletStand)
			return applet.getKeyListeners();
		return super.getKeyListeners();
	}

	@Override
	public synchronized void removeKeyListener(KeyListener l) {
		if (appletStand)
			applet.removeKeyListener(l);
		else
			super.removeKeyListener(l);
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
					Logger.logarExept(e1);
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
					Logger.logarExept(e1);
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
					Logger.logarExept(e1);
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
					Logger.logarExept(e1);
				}
			}
		});
		menu4.add(apagarUltimoNoPista);

		JMenuItem apagarUltimoNoBox = new JMenuItem(
				"Apagar ultimo NO (SHIFT+DEL)") {
			public String getText() {
				return Lang.msg("107");
			}

		};
		apagarUltimoNoBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					editor.apagarUltimoNoBox();
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});
		menu4.add(apagarUltimoNoBox);

		JMenuItem salvarPista = new JMenuItem("Salvar Pista F8") {
			public String getText() {
				return Lang.msg("108");
			}

		};
		salvarPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					if (editor == null) {
						editorInflado.salvarPista();
					} else {
						editor.salvarPista();
					}

				} catch (Exception e1) {
					Logger.logarExept(e1);
				}
			}
		});
		menu4.add(salvarPista);

		JMenuItem vetorizarPista = new JMenuItem("vetorizarPista") {
			public String getText() {
				return Lang.msg("vetorizarPista");
			}

		};
		vetorizarPista.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				try {
					if (controleJogo != null) {
						controleJogo.matarTodasThreads();
					}
					editorInflado = new MainPanelEditorVetorizado(
							MainFrame.this);
					ativarKeysEditor();
				} catch (Exception e1) {
					Logger.logarExept(e1);
				}

			}
		});
		menu4.add(vetorizarPista);
	}

	public static void main(String[] args) throws IOException {
		MainFrame frame = new MainFrame(null, false);
		if (args != null && args.length > 0) {
			Lang.mudarIdioma(args[0]);
		}
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		frame.iniciar();
	}

	public void iniciar() {
		if (ControleJogoLocal.VALENDO) {
			if (!appletStand)
				setVisible(true);
			bg = CarregadorRecursos.carregaBufferedImage("f1bg.png");
			JPanel bgPanel = new JPanel() {
				protected void paintComponent(Graphics g) {
					g.drawImage(bg, 0, 0, null);
				};

				@Override
				public Dimension getPreferredSize() {
					return new Dimension(1020, 699);
				}
			};
			getContentPane().add(bgPanel, BorderLayout.CENTER);
			bgPanel.updateUI();

		} else {
			try {
				Logger.ativo = true;
				if (controleJogo != null) {
					controleJogo.matarTodasThreads();
				}
				controleJogo = new ControleJogoLocal();
				controleJogo.setMainFrame(this);
				controleJogo.iniciarJogo();
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	public void exibirResiltadoFinal(PainelTabelaResultadoFinal resultadoFinal) {

		JOptionPane.showMessageDialog(this, new JScrollPane(resultadoFinal),
				"Resultado Final. ", JOptionPane.INFORMATION_MESSAGE);

	}

	public boolean isModoApplet() {
		return applet != null;
	}

	public void desbilitarMenusModoOnline() {
		if (iniciar != null) {
			iniciar.setEnabled(false);
		}
		if (pausa != null) {
			pausa.setEnabled(false);
		}
		if (atualizacaoSuave != null) {
			atualizacaoSuave.setEnabled(true);
		}
		menuEditor.setEnabled(false);

	}

	public void setControleJogo(InterfaceJogo controleJogo) {
		this.controleJogo = controleJogo;
	}

}
