package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;
import br.nnpe.Util;

public class FormEntrada extends JPanel {
	private JComboBox comboIdiomas = new JComboBox(new String[] {
			Lang.msg("pt"), Lang.msg("en") });
	private JTextField nomeLogar = new JTextField(20);
	private JTextField nomeVisitante = new JTextField(20);
	private JTextField nomeRegistrar = new JTextField(20);

	private JLabel senhaLabel = new JLabel("Senha") {
		public String getText() {
			return Lang.msg("234");
		}
	};
	private JPasswordField senha = new JPasswordField(20);

	private JCheckBox recuperar = new JCheckBox();
	private JLabel recuperarLabel = new JLabel("Recuperar Senha") {
		public String getText() {
			return Lang.msg("235");
		}
	};

	private JLabel emailLabel = new JLabel("Entre com seu e-mail") {
		public String getText() {
			return Lang.msg("168");
		}
	};
	private JTextField email = new JTextField(20);

	public FormEntrada() {
		setLayout(new BorderLayout());
		add(gerarLoginVisitante(), BorderLayout.NORTH);
		add(gerarRegistrar(), BorderLayout.CENTER);
		add(gerarLogin(), BorderLayout.SOUTH);
		setSize(300, 300);
		setVisible(true);
	}

	public JCheckBox getRecuperar() {
		return recuperar;
	}

	private JPanel gerarRegistrar() {
		JPanel registrarPanel = new JPanel(new GridLayout(5, 2));
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
		registrarPanel.add(emailLabel);
		registrarPanel.add(email);
		JPanel recupearPanel = new JPanel();
		recupearPanel.add(recuperarLabel);
		recupearPanel.add(recuperar);
		registrarPanel.add(recupearPanel);
		comboIdiomas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				Logger.logar(Lang
						.key(comboIdiomas.getSelectedItem().toString()));
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
		JPanel newPanel = new JPanel(new BorderLayout());
		newPanel.add(registrarPanel, BorderLayout.CENTER);
		JPanel langPanel = new JPanel(new BorderLayout());
		langPanel.setBorder(new TitledBorder("Idiomas") {
			public String getTitle() {
				return Lang.msg("219");
			}
		});
		langPanel.add(comboIdiomas, BorderLayout.CENTER);
		newPanel.add(langPanel, BorderLayout.SOUTH);
		return newPanel;
	}

	private JPanel gerarLoginVisitante() {
		JPanel panel = new JPanel(new GridLayout(2, 2));
		panel.setBorder(new TitledBorder("Visitante") {
			@Override
			public String getTitle() {
				return Lang.msg("307");
			}
		});
		panel.add(new JLabel("Entre com seu Nome") {
			public String getText() {
				return Lang.msg("167");
			}
		});
		panel.add(nomeVisitante);
		return panel;
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
				return Lang.msg("167");
			}
		});
		panel.add(nomeLogar);
		panel.add(senhaLabel);
		panel.add(senha);
		return panel;
	}

	public JTextField getNome() {
		if (!Util.isNullOrEmpty(nomeVisitante.getText()))
			return nomeVisitante;
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
		FormEntrada formEntrada = new FormEntrada();
		formEntrada.setToolTipText(Lang.msg("066"));
		int result = JOptionPane.showConfirmDialog(null, formEntrada, Lang
				.msg("066"), JOptionPane.OK_CANCEL_OPTION);

		if (JOptionPane.OK_OPTION == result) {
			System.out.println("ok");
		}
	}

	public JTextField getEmail() {
		return email;
	}
}
