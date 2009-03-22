package sowbreira.f1mane.paddock.applet;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.XMLEncoder;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class FormEntradaSimples extends JPanel {

	private JLabel nomeLabel = new JLabel("Entre com seu Nome");
	private JTextField nome = new JTextField(20);
	private JLabel emailLabel = new JLabel("Entre com seu e-mail (Opcional)");
	private JTextField email = new JTextField(20);
	private JLabel infoLabel = new JLabel("Formulário de login");

	public FormEntradaSimples() {
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
		panel.setBorder(new TitledBorder("Registrar"));
		panel.setLayout(gridLayout);
		panel.add(emailLabel);
		panel.add(email);
		add(panel, BorderLayout.CENTER);
	}

	private void gerarLogin() {
		JPanel panel = new JPanel();
		GridLayout gridLayout = new GridLayout(2, 2);
		panel.setBorder(new TitledBorder("Entrar"));
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
