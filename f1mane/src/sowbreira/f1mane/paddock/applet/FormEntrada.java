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

public class FormEntrada extends JPanel {

	private JLabel nomeLabel = new JLabel("Entre com seu Nome");
	private JTextField nome = new JTextField(20);
	private JLabel senhaLabel = new JLabel("Entre com a senha");
	private JPasswordField senha = new JPasswordField(20);

	private JLabel nomeLabelRegistrar = new JLabel("Digite seu Nome");
	private JTextField nomeRegistrar = new JTextField(20);
	private JLabel senhaLabelRegistrar = new JLabel("Digite a senha");
	private JPasswordField senhaRegistrar = new JPasswordField(20);
	private JLabel senhaLabelRegistrarRepetir = new JLabel("Re-Digite a senha");
	private JPasswordField senhaRegistrarRepetir = new JPasswordField(20);
	private JLabel emailLabel = new JLabel("Entre com seu e-mail");
	private JTextField email = new JTextField(20);
	private JLabel infoLabel = new JLabel("Atenção : Guarde bem a senha,"
			+ " pois ainda não é possivel aterar nem re-enviar.");

	public FormEntrada() {
		setLayout(new BorderLayout());
		gerarLogin();
		gerarRegistrar();
		add(infoLabel, BorderLayout.SOUTH);
		setSize(300, 300);
		setVisible(true);
	}

	private void gerarRegistrar() {
		JPanel panel = new JPanel();
		GridLayout gridLayout = new GridLayout(8, 2);
		panel.setBorder(new TitledBorder("Registrar"));
		panel.setLayout(gridLayout);
		panel.add(nomeLabelRegistrar);
		panel.add(nomeRegistrar);
		panel.add(senhaLabelRegistrar);
		panel.add(senhaRegistrar);
		panel.add(senhaLabelRegistrarRepetir);
		panel.add(senhaRegistrarRepetir);
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
		panel.add(senhaLabel);
		panel.add(senha);
		add(panel, BorderLayout.NORTH);
	}

	public String getSenha() {
		return md5(new String(senha.getPassword()));
	}

	public void setSenha(JPasswordField senha) {
		this.senha = senha;
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

	public JTextField getNomeRegistrar() {
		return nomeRegistrar;
	}

	public void setNomeRegistrar(JTextField nomeRegistrar) {
		this.nomeRegistrar = nomeRegistrar;
	}

	public String getSenhaRegistrar() {
		return md5(new String(senhaRegistrar.getPassword()));
	}

	public void setSenhaRegistrar(JPasswordField senhaRegistrar) {
		this.senhaRegistrar = senhaRegistrar;
	}

	public String getSenhaRegistrarRepetir() {
		return md5(new String(senhaRegistrarRepetir.getPassword()));
	}

	public void setSenhaRegistrarRepetir(JPasswordField senhaRegistrarRepetir) {
		this.senhaRegistrarRepetir = senhaRegistrarRepetir;
	}

	public static String hex(byte[] array) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < array.length; ++i) {
			sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(
					1, 3));
		}

		return sb.toString();
	}

	public static String md5(String message) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			return hex(md.digest(message.getBytes("CP1252")));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	public JTextField getEmail() {
		return email;
	}
}
