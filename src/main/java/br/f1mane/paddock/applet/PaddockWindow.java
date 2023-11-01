package br.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.entidades.Clima;
import br.f1mane.paddock.entidades.Comandos;
import br.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import br.f1mane.paddock.entidades.TOs.DadosPaddock;
import br.f1mane.paddock.entidades.TOs.DetalhesJogo;
import br.f1mane.paddock.entidades.TOs.SessaoCliente;
import br.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import br.f1mane.paddock.servlet.ControleJogosServer;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author paulo.sobreira
 * 
 */
public class PaddockWindow {

	private final JPanel mainPanel;
	private ControlePaddockCliente controlePaddockCliente;
	private final JList listaClientes = new JList();
	private final JList listaJogosCriados = new JList();
	private final JTextArea textAreaChat = new JTextArea();
	private final JTextField textoEnviar = new JTextField();
	private final HashMap mapaJogosCriados = new HashMap();
	private final HashMap mapaJogosVoltas = new HashMap();
	protected BufferedImage img;

	private final JButton sairJogo = new JButton("Enviar Texto") {

		public String getText() {

			return Lang.msg("sairJogo");
		}
	};
	private final JButton entrarJogo = new JButton("Entrar Jogo") {

		public String getText() {

			return Lang.msg("175");
		}
	};
	private final JButton criarJogo = new JButton("Criar Jogo") {

		public String getText() {

			return Lang.msg("176");
		}
	};
	private final JButton iniciarJogo = new JButton("Iniciar Jogo") {

		public String getText() {

			return Lang.msg("094");
		}
	};
	private final JButton verDetalhes = new JButton("Ver Detalhes") {

		public String getText() {

			return Lang.msg("178");
		}
	};
	private final JButton classificacao = new JButton("Classificação") {

		public String getText() {

			return Lang.msg("ranking");
		}
	};

	private final JButton carreira = new JButton("Modo Carreira") {

		public String getText() {

			return Lang.msg("221");
		}
	};
	private final JButton construtores = new JButton("Construtores") {

		public String getText() {

			return Lang.msg("222");
		}
	};
	private final JButton conta = new JButton("Conta") {

		public String getText() {

			return Lang.msg("223");
		}
	};

	private final JButton campeonato = new JButton("campeonato") {

		public String getText() {

			return Lang.msg("268");
		}
	};

	private final JButton verCampeonato = new JButton("verCampeonato") {

		public String getText() {

			return Lang.msg("verCampeonato");
		}
	};

	private final JComboBox comboIdiomas = new JComboBox(
			new String[]{Lang.msg("pt"), Lang.msg("en")});
	private final JButton sobre = new JButton("Sobre") {
		public String getText() {
			return Lang.msg("sobre");
		}
	};
	private final JButton logs = new JButton("logs") {
		public String getText() {
			return Lang.msg("267");
		}
	};
	private final JLabel infoLabel1 = new JLabel("  ");
	private final Set chatTimes = new HashSet();
	private final SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public static void main(String[] args) {
		PaddockWindow paddockWindow = new PaddockWindow(null);
		DefaultListModel clientesModel = new DefaultListModel();
		for (int i = 0; i < 10; i++) {
			clientesModel.addElement("Teste" + i);
		}
		paddockWindow.listaClientes.setModel(clientesModel);
		JFrame frame = new JFrame();
		frame.getContentPane().add(paddockWindow.getMainPanel());
		frame.setSize(800, 400);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	public PaddockWindow(ControlePaddockCliente controlePaddockApplet) {
		CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
		carregadorRecursos.carregarTemporadasPilotos();
		mainPanel = new JPanel(new BorderLayout());
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
		campeonato.setEnabled(false);
		carreira.setEnabled(false);

		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Thread enviarTexto = new Thread(new Runnable() {

					public void run() {
						try {
							controlePaddockCliente
									.enviarTexto(textoEnviar.getText());
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
				controlePaddockCliente.criarJogo();

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
						controlePaddockCliente
								.entarJogo(mapaJogosCriados.get(object));
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
                object = listaClientes.getSelectedValue();
                if (object == null) {
                    object = listaJogosCriados.getSelectedValue();
                    if (object == null
							&& listaJogosCriados.getModel().getSize() == 1) {
						object = listaJogosCriados.getModel().getElementAt(0);
					}
					try {
						controlePaddockCliente
								.verDetalhesJogo(mapaJogosCriados.get(object));
					} catch (Exception ex) {
						Logger.logarExept(ex);
					}
				} else {
                    controlePaddockCliente.verDetalhesJogador(object);
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
				List<String> carregarCreditosJogo = CarregadorRecursos
						.carregarCreditosJogo();

				StringBuilder msg = new StringBuilder();

				for (Iterator iterator = carregarCreditosJogo
						.iterator(); iterator.hasNext();) {
					String string = (String) iterator.next();
					msg.append(string);
				}

				JOptionPane.showMessageDialog(getMainPanel(), msg.toString(),
						Lang.msg("sobre"), JOptionPane.INFORMATION_MESSAGE);
			}
		});
		logs.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
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
			area.append("Qtde : " + Logger.topExceptions.get(exept));
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
		JPanel sPanel = new JPanel(new BorderLayout());
		mainPanel.add(cPanel, BorderLayout.CENTER);
		mainPanel.add(sPanel, BorderLayout.SOUTH);
		JPanel chatPanel = new JPanel();
		if (controlePaddockCliente != null) {
			chatPanel.setBorder(
					new TitledBorder("Fl-MANager Engineer Chat Room Ver "
							+ controlePaddockCliente.getVersaoFormatado()));
		}
		JPanel usersPanel = new JPanel();
		usersPanel.setBorder(new TitledBorder("Jogadores Online") {
			public String getTitle() {
				return Lang.msg("186");
			}
		});
		cPanel.add(chatPanel, BorderLayout.CENTER);
		cPanel.add(usersPanel, BorderLayout.EAST);
		JPanel jogsPanel = new JPanel();
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
		jogsPane.setPreferredSize(new Dimension(150, 235));
		usersPanel.add(jogsPane);
		JScrollPane jogsCriados = new JScrollPane(listaJogosCriados);
		jogsCriados.setPreferredSize(new Dimension(150, 100));
		jogsPanel.add(jogsCriados);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(3, 4));
		buttonsPanel.add(entrarJogo);
		buttonsPanel.add(sairJogo);
		buttonsPanel.add(criarJogo);
		buttonsPanel.add(iniciarJogo);
		buttonsPanel.add(verDetalhes);
		buttonsPanel.add(classificacao);
		buttonsPanel.add(verCampeonato);
		buttonsPanel.add(comboIdiomas);
		comboIdiomas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.logar(
						Lang.key(comboIdiomas.getSelectedItem().toString()));
				String i = Lang.key(comboIdiomas.getSelectedItem().toString());
				int selectedIndex = comboIdiomas.getSelectedIndex();
				if (i != null && !i.isEmpty()) {
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
		buttonsPanel.add(logs);
		buttonsPanel.add(sobre);
		JPanel panelTextoEnviar = new JPanel();
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
					jPanel.add(
							new JLabel(element.getNomeJogador()));
				} else {
					jPanel.setLayout(new GridLayout(2, 1));
					jPanel.add(
							new JLabel(element.getNomeJogador()));
					jPanel.add(new JLabel(" "
							+ element.getPilotoAtual() + " "
							+ Lang.decodeTexto(element.getJogoAtual())));
				}
				if (isSelected) {
					jPanel.setBorder(new LineBorder(new Color(184, 207, 229)));
				}
				for (int i = 0; i < jPanel.getComponentCount(); i++) {
					Component component = jPanel.getComponent(i);
				}
				mainPanel.repaint();
				return jPanel;
			}
		});
		DefaultListModel listaJogosCriadosModel = new DefaultListModel<>();
		listaJogosCriados.setModel(listaJogosCriadosModel);
		if (listaJogosCriadosModel.size() != dadosPaddock.getJogosCriados()
				.size()) {
			atualizaListaJogos(dadosPaddock, listaJogosCriadosModel);
		} else if (listaJogosCriadosModel.size() == dadosPaddock
				.getJogosCriados().size()) {
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
				atualizaListaJogos(dadosPaddock, listaJogosCriadosModel);
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
				Logger.logar("VER_INFO_VOLTAS_JOGO ret == null");
				continue;
			}
			SrvPaddockPack srvPaddockPack = (SrvPaddockPack) ret;
			mapaJogosVoltas.put(key,
					srvPaddockPack.getDetalhesJogo().getVoltaAtual() + "/"
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
					jPanel.add(new JLabel(
							value.toString() + " " + object.toString()));
				}

				if (isSelected) {
					jPanel.setBorder(new LineBorder(new Color(184, 207, 229)));
				}
				for (int i = 0; i < jPanel.getComponentCount(); i++) {
					Component component = jPanel.getComponent(i);
				}
				mainPanel.repaint();
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
			controlePaddockCliente
					.adicionaTextoJogo(dadosPaddock.getLinhaChat());
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

		panel.add(painelEntradaCliente.gerarSeletorTemporadaCircuito(),
				BorderLayout.NORTH);
		painelEntradaCliente.getComboBoxCircuito().setSelectedItem(
				detalhesJogo.getDadosCriarJogo().getCircuitoSelecionado());
		painelEntradaCliente.getComboBoxCircuito().setEnabled(false);
		String temporada = detalhesJogo.getDadosCriarJogo().getTemporada();
		if (temporada != null) {
			painelEntradaCliente.getComboTemporada().setSelectedItem(
					temporada.substring(1, temporada.length()));
			painelEntradaCliente.getComboTemporada().setEnabled(false);
		}
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
		panelJogo.add(
				new JLabel(detalhesJogo.getDadosCriarJogo().getNivelCorrida()));
		panelJogo.add(new JLabel("Hora Criação : ") {
			public String getText() {
				return Lang.msg("192");
			}
		});
		panelJogo.add(new JLabel(
				df.format(new Timestamp(detalhesJogo.getTempoCriacao()))));
		panelJogo.add(new JLabel("Inicio Automatico : ") {
			public String getText() {
				return Lang.msg("193");
			}
		});
		panelJogo.add(new JLabel(df.format(
				new Timestamp(detalhesJogo.getTempoCriacao() + 300000))));
		panelJogo.add(new JLabel("Pista : ") {
			public String getText() {
				return Lang.msg("194");
			}
		});
		panelJogo.add(new JLabel(
				detalhesJogo.getDadosCriarJogo().getCircuitoSelecionado()));
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
		panelJogo.add(
				new JLabel(Lang.msg("197")));
		panelJogo.add(new JLabel("Potencia Todos: ") {
			public String getText() {
				return Lang.msg("198");
			}
		});
		panelJogo.add(
				new JLabel(Lang.msg("197")));
		panelJogo.add(new JLabel("Clima: ") {
			public String getText() {
				return Lang.msg("199");
			}
		});
		panelJogo.add(new JLabel(
				new Clima(detalhesJogo.getDadosCriarJogo().getClima())
						.toString()));
		panelJogo.add(new JLabel("") {

			public String getText() {

				return Lang.msg("302");
			}
		});
		panelJogo.add(
				new JLabel(detalhesJogo.getDadosCriarJogo().isReabastecimento()
						? Lang.msg("SIM")
						: Lang.msg("NAO")));
		panelJogo.add(new JLabel("Sem troca pneu: ") {

			public String getText() {

				return Lang.msg("303");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo().isTrocaPneu()
				? Lang.msg("SIM")
				: Lang.msg("NAO")));

		panelJogo.add(new JLabel("Kers: ") {

			public String getText() {

				return Lang.msg("kers");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo().isErs()
				? Lang.msg("SIM")
				: Lang.msg("NAO")));

		panelJogo.add(new JLabel("DRS: ") {

			public String getText() {

				return Lang.msg("drs");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo().isDrs()
				? Lang.msg("SIM")
				: Lang.msg("NAO")));

		panelJogo.add(new JLabel("nomeCampeonato") {

			public String getText() {

				return Lang.msg("nomeCampeonato");
			}
		});
		panelJogo.add(new JLabel(
				detalhesJogo.getDadosCriarJogo().getNomeCampeonato()));

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
		panel.add(new JLabel(
				df.format(new Timestamp(cliente.getUlimaAtividade()))));
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

}
