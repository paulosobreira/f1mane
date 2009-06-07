/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
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

import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.DetalhesJogo;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.servlet.ControleJogosServer;
import sowbreira.f1mane.recursos.idiomas.Lang;

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
	private JTextField textoEnviar = new JTextField(
			"Digite o texto a enviar aqui.");
	private JButton enviarTexto = new JButton("Enviar Texto") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("174");
		}
	};
	private JButton entrarJogo = new JButton("Entrar Jogo") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("175");
		}
	};
	private JButton criarJogo = new JButton("Criar Jogo") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("176");
		}
	};
	private JButton iniciarJogo = new JButton("Iniciar Jogo") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("094");
		}
	};
	private JButton verDetalhes = new JButton("Ver Detalhes") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("178");
		}
	};
	private JButton classificacao = new JButton("Classificação") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("179");
		}
	};
	private JComboBox comboTemporada = new JComboBox(new String[] { "2009",
			"2008", "2007" });
	private JButton sobre = new JButton("Sobre") {
		@Override
		public String getText() {
			// TODO Auto-generated method stub
			return Lang.msg("180");
		}
	};
	private JLabel infoLabel1 = new JLabel();
	private Set chatTimes = new HashSet();
	private SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

	public PaddockWindow(ControlePaddockCliente controlePaddockApplet) {
		mainPanel = new JPanel(new BorderLayout());
		this.controlePaddockCliente = controlePaddockApplet;
		controlePaddockApplet.setPaddockWindow(this);
		gerarLayout();
		gerarAcoes();
		atualizaInfo();
	}

	private void gerarAcoes() {
		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				Thread enviarTexto = new Thread(new Runnable() {

					public void run() {
						try {
							controlePaddockCliente.enviarTexto(textoEnviar
									.getText());
							textoEnviar.setText("");
						} catch (Exception e) {
							e.printStackTrace();
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
				if (object != null) {
					int result = JOptionPane.showConfirmDialog(getMainPanel(),
							Lang.msg("181") + object);
					if (result == JOptionPane.YES_OPTION)
						controlePaddockCliente.entarJogo(object);
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
					controlePaddockCliente.verDetalhesJogo(object);
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

		sobre.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String msg = Lang.msg("184") + "Paulo Sobreira \n "
						+ "sowbreira@yahoo.com.br \n"
						+ "http://br.geocities.com/sowbreira/ \n"
						+ "Agosto de 2007 \n ";
				msg += Lang.msg("185") + "\n"
						+ " Leonardo Andrade leonardofandrade@gmail.com \n"
						+ " Daniel Souza daniels@globo.com \n"
						+ " Wendel wendel12345@yahoo.com.br \n"
						+ " Marquin marcoshenriqueca@gmail.com \n"
						+ " Luiz Carlos lcarlos.email@gmail.com \n"
						+ " Alvaru alvaru@secrel.com.br";

				JOptionPane.showMessageDialog(getMainPanel(), msg, Lang
						.msg("180"), JOptionPane.INFORMATION_MESSAGE);
			}
		});

	}

	private void gerarLayout() {
		JPanel cPanel = new JPanel(new BorderLayout());
		JPanel sPanel = new JPanel(new BorderLayout());
		// cPanel.setBorder(new TitledBorder("Paddock"));
		// sPanel.setBorder(new TitledBorder("South"));

		mainPanel.add(cPanel, BorderLayout.CENTER);
		mainPanel.add(sPanel, BorderLayout.SOUTH);
		JPanel chatPanel = new JPanel();
		chatPanel.setBorder(new TitledBorder(
				"F1-Mane Web Paddock Chat Room v 1.3"));
		JPanel usersPanel = new JPanel();
		usersPanel.setBorder(new TitledBorder("Jogadores Online") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return Lang.msg("186");
			}
		});
		cPanel.add(chatPanel, BorderLayout.CENTER);
		cPanel.add(usersPanel, BorderLayout.EAST);
		JPanel jogsPanel = new JPanel();
		jogsPanel.setBorder((new TitledBorder("Lista de Jogos") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
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
		jogsPane.setPreferredSize(new Dimension(150, 400));
		usersPanel.add(jogsPane);
		JScrollPane jogsCriados = new JScrollPane(listaJogosCriados);
		jogsCriados.setPreferredSize(new Dimension(150, 100));
		jogsPanel.add(jogsCriados);
		GridLayout gridLayout = new GridLayout(3, 1);
		inputPanel.setLayout(gridLayout);
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new GridLayout(2, 4));
		buttonsPanel.add(enviarTexto);
		buttonsPanel.add(entrarJogo);
		buttonsPanel.add(criarJogo);
		buttonsPanel.add(iniciarJogo);
		buttonsPanel.add(verDetalhes);
		buttonsPanel.add(classificacao);
		buttonsPanel.add(comboTemporada);
		buttonsPanel.add(sobre);
		inputPanel.add(buttonsPanel);
		JPanel panelTextoEnviar = new JPanel();
		panelTextoEnviar.setBorder(new TitledBorder("Texto Enviar") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return Lang.msg("188");
			}
		});
		panelTextoEnviar.setLayout(new BorderLayout());
		panelTextoEnviar.add(textoEnviar, BorderLayout.CENTER);
		inputPanel.add(panelTextoEnviar);
		inputPanel.add(infoLabel1);
		chatPanel.setLayout(new BorderLayout());
		chatPanel.add(new JScrollPane(textAreaChat), BorderLayout.CENTER);

	}

	public JPanel getMainPanel() {
		return mainPanel;
	}

	public static void main(String[] args) {
		PaddockWindow paddockWindow = new PaddockWindow(null);
		JFrame frame = new JFrame();
		frame.getContentPane().add(paddockWindow.getMainPanel());
		frame.setSize(640, 480);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
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
			for (Iterator iter = dadosPaddock.getJogosCriados().iterator(); iter
					.hasNext();) {
				String element = (String) iter.next();
				model.addElement(element);
			}
		}
	}

	private void atualizarChat(DadosPaddock dadosPaddock) {
		if ("".equals(dadosPaddock.getLinhaChat())
				|| dadosPaddock.getLinhaChat() == null
				|| dadosPaddock.getDataTime() == null) {
			return;
		}
		if (!chatTimes.contains(dadosPaddock.getDataTime())) {
			textAreaChat.append(dadosPaddock.getLinhaChat() + "\n");
			textAreaChat.setCaretPosition(textAreaChat.getText().length());
			chatTimes.add(dadosPaddock.getDataTime());
		}
	}

	public void mostrarDetalhes(DetalhesJogo detalhesJogo) {
		JPanel panelJogadores = gerarPainelJogadores(detalhesJogo);
		JPanel panelJogo = gerarPainelJogo(detalhesJogo);
		panelJogadores.setBorder(new TitledBorder("Jogadores") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return Lang.msg("117");
			}
		});
		panelJogo.setBorder(new TitledBorder("Dados Inicio do Jogo") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
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
		panelJogo.setLayout(new GridLayout(11, 2));
		panelJogo.add(new JLabel("Criador : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("190");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getNomeCriador()));
		panelJogo.add(new JLabel("Nivel : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("191");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getNivelCorrida()));
		panelJogo.add(new JLabel("Hora Criação : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("192");
			}
		});
		panelJogo.add(new JLabel(df.format(new Timestamp(detalhesJogo
				.getTempoCriacao()))));
		panelJogo.add(new JLabel("Inicio Automatico : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("193");
			}
		});
		panelJogo.add(new JLabel(df.format(new Timestamp(detalhesJogo
				.getTempoCriacao() + 300000))));
		panelJogo.add(new JLabel("Pista : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("194");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getCircuitoSelecionado()));
		panelJogo.add(new JLabel("Número Voltas : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("195");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getQtdeVoltas().toString()));
		panelJogo.add(new JLabel("Habilidade Todos : ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("196");
			}
		});
		int ht = detalhesJogo.getDadosCriarJogo().getHabilidade().intValue();
		panelJogo.add(new JLabel((ht == 0 ? Lang.msg("197") : String
				.valueOf(ht))));
		panelJogo.add(new JLabel("Potencia Todos: ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("198");
			}
		});
		int pt = detalhesJogo.getDadosCriarJogo().getPotencia().intValue();
		panelJogo.add(new JLabel((pt == 0 ? Lang.msg("197") : String
				.valueOf(pt))));
		panelJogo.add(new JLabel("Clima: ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("199");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo().getClima()
				.toString()));
		panelJogo.add(new JLabel("Fator Ultrapassagem: ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("200");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getDiffultrapassagem().toString()));
		panelJogo.add(new JLabel("Fator Velocidade: ") {
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("201");
			}
		});
		panelJogo.add(new JLabel(detalhesJogo.getDadosCriarJogo()
				.getVeloMaxReta().toString()));

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
				@Override
				public String getText() {
					// TODO Auto-generated method stub
					return Lang.msg("202");
				}
			});
			panelJogadores.add(new JLabel("Jogador") {
				@Override
				public String getText() {
					// TODO Auto-generated method stub
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
			@Override
			public String getText() {
				// TODO Auto-generated method stub
				return Lang.msg("170");
			}
		});
		panel.add(new JLabel(df.format(new Timestamp(cliente
				.getUlimaAtividade()))));
		JOptionPane.showMessageDialog(mainPanel, panel);
	}

	public void atualizaInfo() {
		String text = Lang.msg("114")
				+ controlePaddockCliente.getLatenciaMinima();
		text += Lang.msg("115") + controlePaddockCliente.getLatenciaReal();
		text += Lang.msg("116") + (ControleJogosServer.MaxJogo - 1);

		infoLabel1.setText(text);

	}
}
