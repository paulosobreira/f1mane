package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

public class FormEntrada extends JPanel {
	private JComboBox comboIdiomas = new JComboBox(new String[] { Lang.msg("pt"), Lang.msg("en") });
	private JTextField nomeLogar = new JTextField(20);
	private JTextField nomeRegistrar = new JTextField(20);
	private JTextField nomeRecuperar = new JTextField(20);
	private JTextField emailRegistrar = new JTextField(20);
	private JTextField emailRecuperar = new JTextField(20);
	private JPasswordField senha = new JPasswordField(20);
	private int conta1;
	private int conta2;
	private JTextField resultadorConta = new JTextField(20);

	private ControlePaddockCliente controlePaddockCliente;

	private JLabel senhaLabel = new JLabel("Senha") {
		public String getText() {
			return Lang.msg("senha");
		}
	};

	private JCheckBox lembrar = new JCheckBox();
	private JLabel recuperarLabel = new JLabel("Recuperar Senha") {
		public String getText() {
			return Lang.msg("235");
		}
	};

	public FormEntrada(ControlePaddockCliente controlePaddockCliente) {
		this.controlePaddockCliente = controlePaddockCliente;
		setLayout(new BorderLayout());
		JTabbedPane jTabbedPane = new JTabbedPane();
		JPanel panelAbaEntrar = new JPanel(new BorderLayout(15, 15));
		JPanel panelabaEntrarCenter = new JPanel(new BorderLayout());
		panelabaEntrarCenter.add(gerarLogin(), BorderLayout.NORTH);
		panelabaEntrarCenter.add(gerarLembrar(), BorderLayout.CENTER);
		panelabaEntrarCenter.add(gerarIdiomas(), BorderLayout.SOUTH);
		panelAbaEntrar.add(panelabaEntrarCenter, BorderLayout.CENTER);
		jTabbedPane.addTab(Lang.msg("171"), panelAbaEntrar);
		JPanel panelAbaRegistrar = new JPanel(new BorderLayout());
		panelAbaRegistrar.add(gerarRegistrar(), BorderLayout.CENTER);
		jTabbedPane.addTab(Lang.msg("registrar"), panelAbaRegistrar);
		JPanel panelAbaRecuperar = new JPanel(new BorderLayout());
		panelAbaRecuperar.add(gerarRecuperar(), BorderLayout.CENTER);
		jTabbedPane.addTab(Lang.msg("recuperar"), panelAbaRecuperar);
		add(jTabbedPane, BorderLayout.CENTER);
		jTabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				nomeRegistrar.setText("");
				nomeRecuperar.setText("");
				emailRegistrar.setText("");
				emailRecuperar.setText("");
			}
		});
		setSize(300, 300);
		setVisible(true);

	}

	public JCheckBox getLembrar() {
		return lembrar;
	}

	public void setLembrar(JCheckBox lembrar) {
		this.lembrar = lembrar;
	}

	private Component gerarLembrar() {
		lembrar = new JCheckBox();
		JPanel langPanel = new JPanel();
		langPanel.add(lembrar);
		langPanel.add(new JLabel() {
			@Override
			public String getText() {
				return Lang.msg("lembrar");
			}
		});
		return langPanel;
	}

	private JPanel gerarIdiomas() {
		comboIdiomas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.logar(Lang.key(comboIdiomas.getSelectedItem().toString()));
				String i = Lang.key(comboIdiomas.getSelectedItem().toString());
				if (i != null && !"".equals(i)) {
					Lang.mudarIdioma(i);
					comboIdiomas.removeAllItems();
					comboIdiomas.addItem(Lang.msg("pt"));
					comboIdiomas.addItem(Lang.msg("en"));
				}
				FormEntrada.this.repaint();
			}
		});
		JPanel langPanel = new JPanel(new BorderLayout());
		langPanel.setBorder(new TitledBorder("Idiomas") {
			public String getTitle() {
				return Lang.msg("219");
			}
		});
		langPanel.add(comboIdiomas, BorderLayout.CENTER);

		return langPanel;
	}

	private JPanel gerarRegistrar() {
		JPanel registrarPanel = new JPanel(new GridLayout(6, 2));
		registrarPanel.setBorder(new TitledBorder("Registrar") {
			public String getTitle() {
				return Lang.msg("218");
			}
		});
		registrarPanel.add(new JLabel("Entre com seu Nome") {
			public String getText() {
				return Lang.msg("167");
			}
		});
		registrarPanel.add(nomeRegistrar);
		registrarPanel.add(new JLabel("Entre com seu e-mail") {
			public String getText() {
				return Lang.msg("168");
			}
		});
		registrarPanel.add(emailRegistrar);
		conta1 = Util.intervalo(0, 10);
		conta2 = Util.intervalo(0, 10);
		registrarPanel.add(new JLabel("Conta Facil") {
			public String getText() {
				return Lang.msg("contaFacil", new String[] { "" + conta1, "" + conta2 });
			}
		});
		registrarPanel.add(resultadorConta);
		JPanel newPanel = new JPanel(new BorderLayout());
		newPanel.add(registrarPanel, BorderLayout.NORTH);
		return newPanel;
	}

	private JPanel gerarRecuperar() {
		JPanel newPanel = new JPanel(new GridLayout(3, 1));
		newPanel.add(new JLabel("mensagemRecuperarSenha1") {
			public String getText() {
				return Lang.msg("mensagemRecuperarSenha1");
			}
		});
		newPanel.add(new JLabel("mensagemRecuperarSenha2") {
			public String getText() {
				return Lang.msg("mensagemRecuperarSenha2");
			}
		});
		newPanel.add(new JLabel("mensagemRecuperarSenha3") {
			public String getText() {
				return Lang.msg("mensagemRecuperarSenha3");
			}
		});
		return newPanel;
	}

	private JPanel gerarLogin() {
		JPanel panel = new JPanel();
		GridLayout gridLayout = new GridLayout(4, 2);
		panel.setBorder(new TitledBorder("Entrar") {
			@Override
			public String getTitle() {
				return Lang.msg("171");
			}
		});
		panel.setLayout(gridLayout);
		panel.add(new JLabel("Entre com seu Nome") {
			public String getText() {
				return Lang.msg("entreNomeOuEmail");
			}
		});
		panel.add(nomeLogar);
		panel.add(senhaLabel);
		panel.add(senha);
		return panel;
	}

	public JTextField getNome() {
		if (!Util.isNullOrEmpty(nomeRegistrar.getText()))
			return nomeRegistrar;
		return nomeLogar;
	}

	public void setNome(JTextField nome) {
		this.nomeLogar = nome;
	}

	public JPasswordField getSenha() {
		return senha;
	}

	public static void main(String[] args) throws FileNotFoundException {
		// FileOutputStream fileOutputStream = new
		// FileOutputStream("teste.xml");
		// XMLEncoder encoder = new XMLEncoder(fileOutputStream);
		// String teste = "HandlerFactory";
		// encoder.writeObject(teste);
		// encoder.flush();
		// encoder.close();
		FormEntrada formEntrada = new FormEntrada(null);
		formEntrada.setToolTipText(Lang.msg("066"));
		int result = JOptionPane.showConfirmDialog(null, formEntrada, Lang.msg("066"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			Logger.logar("ok");
		}
	}

	public JTextField getNomeRegistrar() {
		return nomeRegistrar;
	}

	public JTextField getNomeRecuperar() {
		return nomeRecuperar;
	}

	public JTextField getEmailRegistrar() {
		return emailRegistrar;
	}

	public JTextField getEmailRecuperar() {
		return emailRecuperar;
	}

	public int getConta1() {
		return conta1;
	}

	public int getConta2() {
		return conta2;
	}

	public JTextField getResultadorConta() {
		return resultadorConta;
	}

}
