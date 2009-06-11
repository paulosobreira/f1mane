package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import sowbreira.f1mane.recursos.idiomas.Lang;

public class FormEntrada extends JPanel {
	private JComboBox comboIdiomas = new JComboBox(new String[] {
			Lang.msg("pt"), Lang.msg("en") });
	private JLabel nomeLabel = new JLabel("Entre com seu Nome") {
		public String getText() {
			return Lang.msg("167");
		}
	};
	private JTextField nome = new JTextField(20);
	private JLabel emailLabel = new JLabel("Entre com seu e-mail (Opcional)") {
		public String getText() {
			return Lang.msg("168");
		}
	};
	private JTextField email = new JTextField(20);
	private JLabel infoLabel = new JLabel("Formulário de login") {
		public String getText() {
			return Lang.msg("066");
		}
	};

	public FormEntrada() {
		setLayout(new BorderLayout());
		gerarLogin();
		gerarRegistrar();
		add(infoLabel, BorderLayout.SOUTH);
		setSize(300, 300);
		setVisible(true);
		nomeLabel.requestFocus();
	}

	private void gerarRegistrar() {
		JPanel panel = new JPanel();
		GridLayout gridLayout = new GridLayout(2, 2);
		panel.setBorder(new TitledBorder("Registrar") {
			public String getTitle() {
				return Lang.msg("218");
			}
		});
		panel.setLayout(gridLayout);
		panel.add(emailLabel);
		panel.add(email);
		comboIdiomas.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				System.out.println(Lang.key(comboIdiomas.getSelectedItem()
						.toString()));
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
		newPanel.add(panel, BorderLayout.CENTER);
		newPanel.add(comboIdiomas, BorderLayout.SOUTH);
		add(newPanel, BorderLayout.CENTER);
	}

	private void gerarLogin() {
		JPanel panel = new JPanel();
		GridLayout gridLayout = new GridLayout(2, 2);
		panel.setBorder(new TitledBorder("Entrar") {
			@Override
			public String getTitle() {
				// TODO Auto-generated method stub
				return Lang.msg("171");
			}
		});
		panel.setLayout(gridLayout);
		panel.add(nomeLabel);
		panel.add(nome);
		add(panel, BorderLayout.NORTH);
	}

	public void setTextInfo1(String text) {
		nomeLabel.setText(text);
	}

	public JTextField getNome() {
		return nome;
	}

	public void setNome(JTextField nome) {
		this.nome = nome;
	}

	public static void main(String[] args) throws FileNotFoundException {
		FileOutputStream fileOutputStream = new FileOutputStream("teste.xml");
		XMLEncoder encoder = new XMLEncoder(fileOutputStream);
		String teste = "HandlerFactory";
		encoder.writeObject(teste);
		encoder.flush();
		encoder.close();
	}

	public JTextField getEmail() {
		return email;
	}
}
