package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.WindowConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.DetalhesJogo;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.servlet.ControleJogosServer;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelCircuito;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class PaddockWindow {

	private JPanel mainPanel;
	private ControlePaddockCliente controlePaddockCliente;
	private JList listaClientes = new JList();
	private JList listaJogosCriados = new JList(new DefaultListModel());
	private JTextArea textAreaChat = new JTextArea();
	private JTextField textoEnviar = new JTextField();
	private HashMap mapaJogosCriados = new HashMap();
	private HashMap mapaJogosVoltas = new HashMap();
	protected BufferedImage img;

	private JButton sairJogo = new JButton("Enviar Texto") {

		public String getText() {

			return Lang.msg("sairJogo");
		}
	};
	private JButton entrarJogo = new JButton("Entrar Jogo") {

		public String getText() {

			return Lang.msg("175");
		}
	};
	private JButton criarJogo = new JButton("Criar Jogo") {

		public String getText() {

			return Lang.msg("176");
		}
	};
	private JButton iniciarJogo = new JButton("Iniciar Jogo") {

		public String getText() {

			return Lang.msg("094");
		}
	};
	private JButton verDetalhes = new JButton("Ver Detalhes") {

		public String getText() {

			return Lang.msg("178");
		}
	};
	private JButton classificacao = new JButton("Classificação") {

		public String getText() {

			return Lang.msg("179");
		}
	};

	private JButton carreira = new JButton("Modo Carreira") {

		public String getText() {

			return Lang.msg("221");
		}
	};
	private JButton construtores = new JButton("Construtores") {

		public String getText() {

			return Lang.msg("222");
		}
	};
	private JButton conta = new JButton("Conta") {

		public String getText() {

			return Lang.msg("223");
		}
	};

	private JButton campeonato = new JButton("campeonato") {

		public String getText() {

			return Lang.msg("268");
		}
	};

	private JButton verCampeonato = new JButton("verCampeonato") {

		public String getText() {

			return Lang.msg("verCampeonato");
		}
	};

	private JComboBox comboTemporada;
	private JComboBox comboIdiomas = new JComboBox(new String[] {
			Lang.msg("pt"), Lang.msg("en") });
	private JButton sobre = new JButton("Sobre") {
		public String getText() {
			return Lang.msg("sobre");
		}
	};
	private JLabel infoLabel1 = new JLabel("  ");
	private Set chatTimes = new HashSet();
	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static void main(String[] args) {
		// PaddockWindow paddockWindow = new PaddockWindow(null);
		// DefaultListModel clientesModel = new DefaultListModel();
		// for (int i = 0; i < 10; i++) {
		// clientesModel.addElement("Teste" + i);
		// }
		// paddockWindow.listaClientes.setModel(clientesModel);
		// JFrame frame = new JFrame();
		// frame.getContentPane().add(paddockWindow.getMainPanel());
		// frame.setSize(800, 400);
		// frame.setVisible(true);
		// frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(true);
		carregadorRecursos.carregarTemporadasPilotos();
		List<String> listTemporadas = new ArrayList<String>();
		Object[] array = carregadorRecursos.getVectorTemps().toArray();
		System.out.println("");

	}

	public PaddockWindow(ControlePaddockCliente controlePaddockApplet) {
		img = ImageUtil
				.geraResize(
						CarregadorRecursos.carregaBufferedImage("f1bg.png"),
						0.85, 0.66);
		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(true);
		carregadorRecursos.carregarTemporadasPilotos();
		comboTemporada = new JComboBox(carregadorRecursos.getVectorTemps()
				.toArray());
		mainPanel = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D graphics2d = (Graphics2D) g;
				if (img != null && PainelCircuito.desenhaBkg)
					graphics2d.drawImage(img, null, 0, 0);
			}
		};
		if (controlePaddockApplet != null) {
			this.controlePaddockCliente = controlePaddockApplet;
			controlePaddockApplet.setPaddockWindow(this);
		}
		gerarLayout();
		gerarAcoes();
		if (controlePaddockApplet != null) {
			atualizaInfo();
		}
	}

	private void gerarAcoes() {
		// campeonato.setEnabled(false);
		// verCampeonato.setEnabled(false);

		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Thread enviarTexto = new Thread(new Runnable() {

					public void run() {
						try {
							controlePaddockCliente.enviarTexto(textoEnviar
									.getText());
							textoEnviar.setText("");
						} catch (Exception e) {
							Logger.logarExept(e);
						}
					}
				});
				enviarTexto.start();
			}

		};
		sairJogo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.sairJogo();

			}
		});
		textoEnviar.addActionListener(actionListener);
		criarJogo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String temporada = (String) comboTemporada.getSelectedItem();
				controlePaddockCliente.criarJogo("t" + temporada);

			}

		});
		entrarJogo.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Object object = listaJogosCriados.getSelectedValue();
				if (listaJogosCriados.getModel().getSize() == 1) {
					object = listaJogosCriados.getModel().getElementAt(0);
				}
				if (object != null) {
					int result = JOptionPane.showConfirmDialog(getMainPanel(),
							Lang.msg("181") + object, Lang.msg("175"),
							JOptionPane.OK_CANCEL_OPTION);
					if (result == JOptionPane.OK_OPTION) {
						controlePaddockCliente.entarJogo(mapaJogosCriados
								.get(object));
					}
				} else {
					JOptionPane.showMessageDialog(getMainPanel(),
							Lang.msg("182"));
				}

			}

		});
		verDetalhes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object object = null;
				if (listaClientes != null) {
					object = listaClientes.getSelectedValue();
				}
				if (object == null) {
					if (listaJogosCriados != null) {
						object = listaJogosCriados.getSelectedValue();
					}
					if (object == null
							&& listaJogosCriados.getModel().getSize() == 1) {
						object = listaJogosCriados.getModel().getElementAt(0);
					}
					try {
						controlePaddockCliente.verDetalhesJogo(mapaJogosCriados
								.get(object));
					} catch (Exception ex) {
						Logger.logarExept(ex);
					}
				} else {
					if (object != null) {
						controlePaddockCliente.verDetalhesJogador(object);
					} else {
						JOptionPane.showMessageDialog(getMainPanel(),
								Lang.msg("183"));
					}
				}

			}

		});
		iniciarJogo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.iniciarJogo();

			}

		});
		classificacao.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.verClassificacao();

			}

		});

		construtores.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.verConstrutores();

			}

		});
		carreira.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.modoCarreira();

			}

		});
		sobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = Lang.msg("184") + "Paulo Sobreira \n "
						+ "sowbreira@gmail.com \n"
						+ "sowbreira.appspot.com/ \n" + "Agosto de 2007 \n ";
				msg += Lang.msg("185") + "\n" + " Gizele Hidaka \n"
						+ " Albercio Lopes \n" + " Edmar Filho \n"
						+ " Florêncio Queiroz \n" + " Jorge Botelho \n"
						+ " Danilo Pacheco \n" + " Acilon Souza \n"
						+ " Luciano Homem \n" + " Marcos Henrique";

				JOptionPane.showMessageDialog(getMainPanel(), msg,
						Lang.msg("sobre"), JOptionPane.INFORMATION_MESSAGE);
				verLogs();
			}
		});
		campeonato.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.criarCampeonato();
			}

		});
		verCampeonato.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				controlePaddockCliente.verCampeonato();
			}

		});

	}

	protected void verLogs() {
		JTextArea area = new JTextArea(20, 50);
		Set top = Logger.topExceptions.keySet();
		for (Iterator iterator = top.iterator(); iterator.hasNext();) {
			String exept = (String) iterator.next();
			area.append("Quantidade : " + Logger.topExceptions.get(exept));
			area.append("\n");
			area.append(exept.replaceAll("<br>", "\n"));
			area.append("\n");
		}
		area.setCaretPosition(0);
		JOptionPane.showMessageDialog(getMainPanel(), new JScrollPane(area),
				Lang.msg("listaDeErros"), JOptionPane.INFORMATION_MESSAGE);

	}

	private void gerarLayout() {
		JPanel cPanel = new JPanel(new BorderLayout());
		compTransp(cPanel);
		JPanel sPanel = new JPanel(new BorderLayout());
		compTransp(sPanel);
		mainPanel.add(cPanel, BorderLayout.CENTER);
		mainPanel.add(sPanel, BorderLayout.SOUTH);
		compTransp(mainPanel);
		JPanel chatPanel = new JPanel();
		compTransp(chatPanel);
		if (controlePaddockCliente != null)
			chatPanel.setBorder(new TitledBorder(
					"F1-MANager Engineer Chat Room Ver "
							+ controlePaddockCliente.getVersao()));
		JPanel usersPanel = new JPanel();
		compTransp(usersPanel);
		usersPanel.setBorder(new TitledBorder("Jogadores Online") {
			public String getTitle() {
				return Lang.msg("186");
			}
		});
		cPanel.add(chatPanel, BorderLayout.CENTER);
		cPanel.add(usersPanel, BorderLayout.EAST);
		JPanel jogsPanel = new JPanel();
		compTransp(jogsPanel);
		jogsPanel.setBorder((new TitledBorder("Lista de Jogos") {
			public String getTitle() {
				return Lang.msg("187");
			}
		}));
		sPanel.add(jogsPanel, BorderLayout.EAST);
		JPanel inputPanel = new JPanel();
		sPanel.add(inputPanel, BorderLayout.CENTER);
		/**
		 * adicionar componentes.
		 */
		JScrollPane jogsPane = new JScrollPane(listaClientes);
		compTransp(jogsPane);
		compTransp(listaClientes);
		jogsPane.setPreferredSize(new Dimension(150, 235));
		usersPanel.add(jogsPane);
		JScrollPane jogsCriados = new JScrollPane(listaJogosCriados);
		compTransp(listaJogosCriados);
		compTransp(jogsCriados);
		jogsCriados.setPreferredSize(new Dimension(150, 100));
		jogsPanel.add(jogsCriados);
		JPanel buttonsPanel = new JPanel();
		compTransp(buttonsPanel);
		buttonsPanel.setLayout(new GridLayout(3, 4));
		buttonsPanel.add(entrarJogo);
		buttonsPanel.add(sairJogo);
		buttonsPanel.add(criarJogo);
		buttonsPanel.add(iniciarJogo);
		buttonsPanel.add(verDetalhes);
		buttonsPanel.add(classificacao);
		buttonsPanel.add(comboTemporada);
		buttonsPanel.add(comboIdiomas);
		comboIdiomas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.logar(Lang
						.key(comboIdiomas.getSelectedItem().toString()));
				String i = Lang.key(comboIdiomas.getSelectedItem().toString());
				int selectedIndex = comboIdiomas.getSelectedIndex();
				if (i != null && !"".equals(i)) {
					Lang.mudarIdioma(i);
					comboIdiomas.removeAllItems();
					comboIdiomas.addItem(Lang.msg("pt"));
					comboIdiomas.addItem(Lang.msg("en"));
					comboIdiomas.setSelectedIndex(selectedIndex);
					for (int j = 0; j < mainPanel.getComponentCount(); j++) {
						mainPanel.getComponent(j).repaint();
					}
				}
			}
		});
		conta.setEnabled(false);
		buttonsPanel.add(carreira);
		buttonsPanel.add(campeonato);
		buttonsPanel.add(verCampeonato);
		buttonsPanel.add(sobre);
		JPanel panelTextoEnviar = new JPanel();
		compTransp(panelTextoEnviar);
		panelTextoEnviar.setBorder(new TitledBorder("Texto Enviar") {
			public String getTitle() {
				return Lang.msg("188");
			}
		});
		panelTextoEnviar.setLayout(new BorderLayout());
		panelTextoEnviar.add(textoEnviar, BorderLayout.CENTER);
		inputPanel.setLayout(new BorderLayout());
		inputPanel.add(panelTextoEnviar, BorderLayout.NORTH);
		inputPanel.add(buttonsPanel, BorderLayout.CENTER);
		inputPanel.add(infoLabel1, BorderLayout.SOUTH);
		chatPanel.setLayout(new BorderLayout());
		JScrollPane jScrollPane = new JScrollPane(textAreaChat);
		chatPanel.add(jScrollPane, BorderLayout.CENTER);
		compTransp(textAreaChat);
		compTransp(jScrollPane);
	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public void atualizar(DadosPaddock dadosPaddock) {
		atualizarChat(dadosPaddock);
		DefaultListModel clientesModel = new DefaultListModel();
		for (Iterator iter = dadosPaddock.getClientes().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
			clientesModel.addElement(element);
		}
		listaClientes.setModel(clientesModel);
		listaClientes.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				SessaoCliente element = (SessaoCliente) value;
				JPanel jPanel = new JPanel(new GridLayout(1, 1));
				if (Util.isNullOrEmpty(element.getPilotoAtual())) {
					jPanel.add(compTransp(new JLabel(element.getNomeJogador())));
				} else {
					jPanel.setLayout(new GridLayout(2, 1));
					jPanel.add(compTransp(new JLabel(element.getNomeJogador())));
					jPanel.add(compTransp(new JLabel(" "
							+ element.getPilotoAtual() + " "
							+ Lang.decodeTexto(element.getJogoAtual()))));
				}
				if (isSelected) {
					jPanel.setBorder(new LineBorder(new Color(184, 207, 229)));
				}
				for (int i = 0; i < jPanel.getComponentCount(); i++) {
					Component component = jPanel.getComponent(i);
					compTransp(component);
				}
				compTransp(jPanel);
				mainPanel.repaint();
				return jPanel;
			}
		});
		DefaultListModel model = ((DefaultListModel) listaJogosCriados
				.getModel());

		if (model.size() != dadosPaddock.getJogosCriados().size()) {
			atualizaListaJogos(dadosPaddock, model);
		} else if (model.size() == dadosPaddock.getJogosCriados().size()) {
			boolean diferente = false;
			for (Iterator iter = dadosPaddock.getJogosCriados().iterator(); iter
					.hasNext();) {
				String element = (String) iter.next();
				if (mapaJogosCriados.get(Lang.decodeTexto(element)) == null) {
					diferente = true;
					break;
				}
			}
			if (diferente) {
				atualizaListaJogos(dadosPaddock, model);
			}
		}

		for (Iterator iter = dadosPaddock.getJogosCriados().iterator(); iter
				.hasNext();) {
			String element = (String) iter.next();
			String key = Lang.decodeTexto(element);
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.VER_INFO_VOLTAS_JOGO,
					controlePaddockCliente.getSessaoCliente());
			clientPaddockPack.setNomeJogo(element);
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (ret == null) {
				continue;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			mapaJogosVoltas.put(key, srvPaddockPack.getDetalhesJogo()
					.getVoltaAtual()
					+ "/"
					+ srvPaddockPack.getDetalhesJogo().getNumVoltas());
		}

		listaJogosCriados.setCellRenderer(new ListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Object object = mapaJogosVoltas.get(value);
				JPanel jPanel = new JPanel(new GridLayout(1, 1));
				if (object == null) {
					jPanel.add(new JLabel(value.toString()));
				} else {
					jPanel.add(new JLabel(value.toString() + " "
							+ object.toString()));
				}

				if (isSelected) {
					jPanel.setBorder(new LineBorder(new Color(184, 207, 229)));
				} else {
					for (int i = 0; i < jPanel.getComponentCount(); i++) {
						Component component = jPanel.getComponent(i);
						component.setBackground(Color.WHITE);
					}
					jPanel.setBackground(Color.WHITE);
				}

				return jPanel;
			}
		});
		if (mainPanel != null)
			mainPanel.repaint();
	}

	private void atualizaListaJogos(DadosPaddock dadosPaddock,
			DefaultListModel model) {
		model.clear();
		mapaJogosCriados.clear();
		for (Iterator iter = dadosPaddock.getJogosCriados().iterator(); iter
				.hasNext();) {
			String element = (String) iter.next();
			String key = Lang.decodeTexto(element);
			mapaJogosCriados.put(key, element);
			model.addElement(key);
		}
	}

	private void atualizarChat(DadosPaddock dadosPaddock) {
		if (dadosPaddock == null) {
			return;
		}
		if ("".equals(dadosPaddock.getLinhaChat())
				|| dadosPaddock.getLinhaChat() == null
				|| dadosPaddock.getDataTime() == null) {
			return;
		}
		if (!chatTimes.contains(dadosPaddock.getDataTime())) {
			textAreaChat.append(dadosPaddock.getLinhaChat() + "\n");
			textAreaChat.setCaretPosition(textAreaChat.getText().length());
			chatTimes.add(dadosPaddock.getDataTime());
			controlePaddockCliente.adicionaTextoJogo(dadosPaddock
					.getLinhaChat());
		}
	}

	public void mostrarDetalhes(DetalhesJogo detalhesJogo,
			PainelEntradaCliente painelEntradaCliente) {
		JPanel panelJogadores = gerarPainelJogadores(detalhesJogo);
		JPanel panelJogo = gerarPainelJogo(detalhesJogo);
		panelJogadores.setBorder(new TitledBorder("Jogadores") {
			public String getTitle() {
				return Lang.msg("117");
			}
		});
		panelJogo.setBorder(new TitledBorder("Dados Inicio do Jogo") {
			public String getTitle() {
				return Lang.msg("122");
			}
		});
		JPanel panel = new JPanel(new BorderLayout());

		panel.add(painelEntradaCliente.gerarSeletorCircuito(),
				BorderLayout.NORTH);
		painelEntradaCliente.getComboBoxCircuito().setSelectedItem(
				detalhesJogo.getDadosCriarJogo().getCircuitoSelecionado());
		painelEntradaCliente.getComboBoxCircuito().setEnabled(false);

		JPanel p2 = new JPanel(new GridLayout(1, 2));
		p2.add(panelJogo);
		p2.add(panelJogadores);
		panel.add(p2, BorderLayout.CENTER);

		JOptionPane.showMessageDialog(mainPanel, panel);
	}

	public JPanel gerarPainelJogo(DetalhesJogo detalhesJogo) {
		JPanel panelJogo = new JPanel();
		panelJogo.setLayout(new GridLayout(14, 2));
		panelJogo.add(new JLabel("Criador : ") {
			public String getText() {
				return Lang.msg("190");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getNomeCriador()));
		panelJogo.add(new JLabel("Nivel : ") {
			public String getText() {
				return Lang.msg("191");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getNivelCorrida()));
		panelJogo.add(new JLabel("Hora Criação : ") {
			public String getText() {
				return Lang.msg("192");
			}
		});
		panelJogo.add(new JLabel(df.format(new Timestamp(detalhesJogo
				.getTempoCriacao()))));
		panelJogo.add(new JLabel("Inicio Automatico : ") {
			public String getText() {
				return Lang.msg("193");
			}
		});
		panelJogo.add(new JLabel(df.format(new Timestamp(detalhesJogo
				.getTempoCriacao() + 300000))));
		panelJogo.add(new JLabel("Pista : ") {
			public String getText() {
				return Lang.msg("194");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getCircuitoSelecionado()));
		panelJogo.add(new JLabel("Número Voltas : ") {
			public String getText() {
				return Lang.msg("195");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getVoltaAtual() + "/"
				+ detalhesJogo.getDadosCriarJogo().getQtdeVoltas().toString()));
		panelJogo.add(new JLabel("Habilidade Todos : ") {
			public String getText() {
				return Lang.msg("196");
			}
		});
		int ht = detalhesJogo.getDadosCriarJogo().getHabilidade().intValue();
		panelJogo.add(new JLabel((ht == 0 ? Lang.msg("197") : String
				.valueOf(ht))));
		panelJogo.add(new JLabel("Potencia Todos: ") {
			public String getText() {
				return Lang.msg("198");
			}
		});
		int pt = detalhesJogo.getDadosCriarJogo().getPotencia().intValue();
		panelJogo.add(new JLabel((pt == 0 ? Lang.msg("197") : String
				.valueOf(pt))));
		panelJogo.add(new JLabel("Clima: ") {
			public String getText() {
				return Lang.msg("199");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo().getClima()
				.toString()));
		panelJogo.add(new JLabel("Sem Raabaste: ") {

			public String getText() {

				return Lang.msg("302");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.isSemReabastecimento() ? Lang.msg("SIM") : Lang.msg("NAO")));
		panelJogo.add(new JLabel("Sem troca pneu: ") {

			public String getText() {

				return Lang.msg("303");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.isSemTrocaPeneu() ? Lang.msg("SIM") : Lang.msg("NAO")));

		panelJogo.add(new JLabel("Kers: ") {

			public String getText() {

				return Lang.msg("kers");
			}
		});
		panelJogo.add(new JLabel(
				detalhesJogo.getDadosCriarJogo().isKers() ? Lang.msg("SIM")
						: Lang.msg("NAO")));

		panelJogo.add(new JLabel("DRS: ") {

			public String getText() {

				return Lang.msg("drs");
			}
		});
		panelJogo.add(new JLabel(
				detalhesJogo.getDadosCriarJogo().isDrs() ? Lang.msg("SIM")
						: Lang.msg("NAO")));

		panelJogo.add(new JLabel("nomeCampeonato") {

			public String getText() {

				return Lang.msg("nomeCampeonato");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getNomeCampeonato()));

		return panelJogo;
	}

	public JPanel gerarPainelJogadores(DetalhesJogo detalhesJogo) {
		JPanel panelJogadores = new JPanel();

		Map detMap = detalhesJogo.getJogadoresPilotos();
		panelJogadores.setLayout(new GridLayout(detMap.size(), 2));
		for (Iterator iter = detMap.keySet().iterator(); iter.hasNext();) {
			String key = (String) iter.next();
			panelJogadores.add(new JLabel(key + ": "));
			panelJogadores.add(new JLabel((String) detMap.get(key)));
		}
		if (detMap.isEmpty()) {
			panelJogadores.add(new JLabel("Nenhum ") {

				public String getText() {

					return Lang.msg("202");
				}
			});
			panelJogadores.add(new JLabel("Jogador") {

				public String getText() {

					return Lang.msg("162");
				}
			});

		}
		return panelJogadores;
	}

	public void mostrarDetalhesJogador(Object object) {
		JPanel panel = new JPanel();
		SessaoCliente cliente = (SessaoCliente) object;

		panel.setLayout(new GridLayout(6, 2));

		panel.add(new JLabel("Jogador : ") {

			public String getText() {

				return Lang.msg("111");
			}
		});
		panel.add(new JLabel(cliente.getNomeJogador()));

		panel.add(new JLabel("Piloto : ") {

			public String getText() {

				return Lang.msg("253");
			}
		});

		String pilotoAtual = cliente.getPilotoAtual();
		if (Util.isNullOrEmpty(pilotoAtual)) {
			pilotoAtual = "";
		}

		panel.add(new JLabel(pilotoAtual));

		panel.add(new JLabel("Jogo : ") {

			public String getText() {

				return Lang.msg("088") + " : ";
			}
		});
		String jogoAtual = cliente.getJogoAtual();
		if (Util.isNullOrEmpty(jogoAtual)) {
			jogoAtual = "";
		} else {
			jogoAtual = Lang.decodeTexto(jogoAtual);
		}
		panel.add(new JLabel(jogoAtual));

		panel.add(new JLabel("Ultima Atividade : ") {

			public String getText() {

				return Lang.msg("170");
			}
		});
		panel.add(new JLabel(df.format(new Timestamp(cliente
				.getUlimaAtividade()))));
		JOptionPane.showMessageDialog(mainPanel, panel);
	}

	public void atualizaInfo() {
		String text = Lang.msg("114") + " "
				+ controlePaddockCliente.getLatenciaMinima();
		text += " " + Lang.msg("115") + " "
				+ controlePaddockCliente.getLatenciaReal();
		text += " " + Lang.msg("116") + " " + (ControleJogosServer.MaxJogo);

		infoLabel1.setText(text);

	}

	private Component compTransp(Component c) {
		c.setBackground(new Color(255, 255, 255, 0));
		return c;
	}
}
