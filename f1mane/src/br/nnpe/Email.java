package br.nnpe;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class Email {
	private static String MAILSERVER = "";

	private static String USERNAME = "";

	private static String PASSWORD = "";

	public Email() {
	}

	public Email(String path) throws FileNotFoundException, IOException {
		ResourceBundle bundle;
		bundle = new PropertyResourceBundle(new FileInputStream(path
				+ "email.properties"));
		USERNAME = bundle.getString("USERNAME");
		PASSWORD = bundle.getString("PASSWORD");
		MAILSERVER = bundle.getString("MAILSERVER");
//		Logger.logarExept(new Exception("Email Carregado USERNAME :" + USERNAME
//				+ " PASSWORD: " + PASSWORD + " MAILSERVER: " + MAILSERVER));
	}

	public static void main(String[] args) throws AddressException,
			MessagingException {
		Email email = new Email();
		email.sendSimpleMail("Teste", new String[] { "sowbreira@gmail.com" },
				" f1mane@sowbra.com.br", "Teste Corpo", false);
	}

	public void sendSimpleMail(String subject, String[] to, String mensagem,
			boolean html) throws AddressException, MessagingException {
		sendSimpleMail(subject, to, USERNAME, mensagem, html);
	}

	public void sendSimpleMail(String subject, String[] to, String from,
			String mensagem, boolean html) throws AddressException,
			MessagingException {

		Properties mailProps = new Properties();

		mailProps.put("mail.smtp.host", MAILSERVER);
		mailProps.put("mail.smtp.port", 25);

		Authenticator auth = new SMTPAuthenticator();
		Session mailSession = Session.getInstance(mailProps, auth);

		mailProps.put("mail.smtp.auth", "true");

		InternetAddress remetente = new InternetAddress(from);

		Address[] addresses = new Address[to.length];
		for (int i = 0; i < to.length; i++) {
			InternetAddress destinatario = new InternetAddress(to[i]);
			addresses[i] = destinatario;
		}

		Message message = new MimeMessage(mailSession);

		message.setSentDate(new Date());

		message.setFrom(remetente);

		message.setRecipients(Message.RecipientType.TO, addresses);

		message.setSubject(subject);

		if (html) {
			message.setContent(mensagem.toString(), "text/html");
		} else {
			message.setContent(mensagem.toString(), "text/plain");
		}

		Transport.send(message);

	}

	public void sendAttachMail(String subject, String[] to, String from,
			String mensagem, File file, String fileName)
			throws AddressException, MessagingException {
		Properties mailProps = new Properties();

		mailProps.put("mail.smtp.host", MAILSERVER);
		// mailProps.put("mail.smtp.port", 125);
		Authenticator auth = new SMTPAuthenticator();
		Session mailSession = Session.getInstance(mailProps, auth);

		mailProps.put("mail.smtp.auth", "true");

		InternetAddress remetente = new InternetAddress(from);

		Address[] addresses = new Address[to.length];
		for (int i = 0; i < to.length; i++) {
			InternetAddress destinatario = new InternetAddress(to[i]);
			addresses[i] = destinatario;
		}

		Message message = new MimeMessage(mailSession);

		message.setSentDate(new Date());

		message.setFrom(remetente);

		message.setRecipients(Message.RecipientType.TO, addresses);

		message.setSubject(subject);

		Multipart mp = new MimeMultipart();
		MimeBodyPart mbp1 = new MimeBodyPart();
		mbp1.setContent(mensagem.toString(), "text/html");
		mp.addBodyPart(mbp1);

		// anexando arquivos.
		// File f = new File("C:\\Temp\\OrdemCompra-42653.pdf");
		MimeBodyPart mbp = new MimeBodyPart();
		mbp.setFileName(fileName);
		mbp.setDataHandler(new DataHandler(new FileDataSource(file)));
		mp.addBodyPart(mbp);

		// message.setContent(mensagem.toString(), "text/html");
		message.setContent(mp);

		Transport.send(message);
	}

	public class SMTPAuthenticator extends Authenticator {
		public PasswordAuthentication getPasswordAuthentication() {

			return new PasswordAuthentication(USERNAME, PASSWORD);
		}
	}

	/*
	 * public static void main(String[] args) { Email mail = new Email(); try {
	 * String emails = "bruno.miranda@tbmtextil.com.br"; //Transforma num array
	 * os emails separados por virgula. String[] to = emails.split(",");
	 * //mail.sendAttachMail
	 * ("Assunto",to,"bruno.miranda@tbmtextil.com.br","Corpo Msg"); } catch
	 * (Exception e) { // TODO: handle exception } }
	 */
}
