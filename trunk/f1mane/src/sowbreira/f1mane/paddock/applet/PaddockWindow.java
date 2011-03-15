package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.DetalhesJogo;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.servlet.ControleJogosServer;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;

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
	private JButton enviarTexto = new JButton("Enviar Texto") {

		public String getText() {

			return Lang.msg("174");
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

	private JComboBox comboTemporada = new JComboBox(new String[] { "2010",
			"2009", "2008", "2007", "2003", "1990", "1993", "1988", "1987",
			"1986", "1974", "1972", "1968", "super" });
	private JComboBox comboIdiomas = new JComboBox(new String[] {
			Lang.msg("pt"), Lang.msg("en") });
	private JButton sobre = new JButton("Sobre") {

		public String getText() {

			return Lang.msg("180");
		}
	};
	private JLabel infoLabel1 = new JLabel("  ");
	private Set chatTimes = new HashSet();
	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public PaddockWindow(ControlePaddockCliente controlePaddockApplet) {
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
		enviarTexto.addActionListener(actionListener);
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
							Lang.msg("181") + object);
					if (result == JOptionPane.YES_OPTION) {
						controlePaddockCliente.entarJogo(mapaJogosCriados
								.get(object));
					}
				} else {
					JOptionPane.showMessageDialog(getMainPanel(), Lang
							.msg("182"));
				}

			}

		});
		verDetalhes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object object = listaJogosCriados.getSelectedValue();
				if (object != null) {
					controlePaddockCliente.verDetalhesJogo(mapaJogosCriados
							.get(object));
				} else {
					object = listaClientes.getSelectedValue();
					if (object != null) {
						controlePaddockCliente.verDetalhesJogador(object);
					} else {
						JOptionPane.showMessageDialog(getMainPanel(), Lang
								.msg("183"));
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
				msg += Lang.msg("185") + "\n" + " Florêncio Queiroz \n"
						+ " Jorge Botelho \n" + " Leonardo Andrade \n"
						+ " Daniel Souza \n" + " Wendel Silva \n"
						+ " Marcos Henrique\n" + " Alvaru";

				JOptionPane.showMessageDialog(getMainPanel(), msg, Lang
						.msg("180"), JOptionPane.INFORMATION_MESSAGE);
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

	private void gerarLayout() {
		JPanel cPanel = new JPanel(new BorderLayout());
		JPanel sPanel = new JPanel(new BorderLayout());
		mainPanel.add(cPanel, BorderLayout.CENTER);
		mainPanel.add(sPanel, BorderLayout.SOUTH);
		JPanel chatPanel = new JPanel();
		chatPanel.setBackground(Color.WHITE);
		chatPanel.setBorder(new TitledBorder(
				"F1-MANager Engineer Chat Room Ver " + InterfaceJogo.VERSAO));
		JPanel usersPanel = new JPanel();
		usersPanel.setBackground(Color.WHITE);
		usersPanel.setBorder(new TitledBorder("Jogadores Online") {
			public String getTitle() {
				return Lang.msg("186");
			}
		});
		cPanel.add(chatPanel, BorderLayout.CENTER);
		cPanel.add(usersPanel, BorderLayout.EAST);
		JPanel jogsPanel = new JPanel();
		jogsPanel.setBackground(Color.WHITE);
		jogsPanel.setBorder((new TitledBorder("Lista de Jogos") {
			public String getTitle() {
				return Lang.msg("187");
			}
		}));
		sPanel.add(jogsPanel, BorderLayout.EAST);
		JPanel inputPanel = new JPanel();
		inputPanel.setBackground(Color.WHITE);
		sPanel.add(inputPanel, BorderLayout.CENTER);
		/**
		 * adicionar componentes.
		 */
		JScrollPane jogsPane = new JScrollPane(listaClientes);
		jogsPane.setPreferredSize(new Dimension(150, 200));
		usersPanel.add(jogsPane);
		JScrollPane jogsCriados = new JScrollPane(listaJogosCriados);
		jogsCriados.setPreferredSize(new Dimension(150, 100));
		jogsPanel.add(jogsCriados);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBackground(Color.WHITE);
		buttonsPanel.setLayout(new GridLayout(3, 4));
		buttonsPanel.add(enviarTexto);
		buttonsPanel.add(entrarJogo);
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
		panelTextoEnviar.setBackground(Color.WHITE);
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
		chatPanel.add(new JScrollPane(textAreaChat), BorderLayout.CENTER);

	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

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

	public void atualizar(DadosPaddock dadosPaddock) {
		atualizarChat(dadosPaddock);

		DefaultListModel clientesModel = new DefaultListModel();
		for (Iterator iter = dadosPaddock.getClientes().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
			clientesModel.addElement(element);
		}
		listaClientes.setModel(clientesModel);

		DefaultListModel model = ((DefaultListModel) listaJogosCriados
				.getModel());
		if (model.size() != dadosPaddock.getJogosCriados().size()) {
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

	public void mostrarDetalhes(DetalhesJogo detalhesJogo) {
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
		JPanel panel = new JPanel();
		panel.add(panelJogo);
		panel.add(panelJogadores);
		JOptionPane.showMessageDialog(mainPanel, panel);
	}

	public JPanel gerarPainelJogo(DetalhesJogo detalhesJogo) {
		JPanel panelJogo = new JPanel();
		panelJogo.setLayout(new GridLayout(12, 2));
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
		panel.setLayout(new GridLayout(1, 2));
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
}
