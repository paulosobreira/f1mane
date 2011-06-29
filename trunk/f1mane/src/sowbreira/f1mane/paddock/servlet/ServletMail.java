package sowbreira.f1mane.paddock.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;

import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;

import br.nnpe.Logger;
import br.nnpe.Util;

public class ServletMail extends HttpServlet {

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		String tipo = arg0.getParameter("tipo");
		if ("admail".equals(tipo)) {
			try {
				adMail(arg0.getParameter("assunto"),
						arg0.getParameter("texto"), arg0.getParameter("passe"),
						arg1);
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}
	}

	private void adMail(String assunto, String texto, String passe,
			HttpServletResponse res) throws NoSuchAlgorithmException,
			UnsupportedEncodingException {
		if (Util.isNullOrEmpty(passe)
				|| !Util.md5(passe).equals("f02c7b3c384412e5755883da397a28fd")) {
			return;
		}
		try {
			res.setContentType("text/html");
			PrintWriter printWriter = res.getWriter();
			printWriter.write("<html><body>");
			printWriter.write("<h2>F1-Mane Admin - Mail</h2><br><hr>");
			Session session = ServletPaddock.controlePersistencia.getSession();
			int cont = 0;
			try {
				Set top = ServletPaddock.controlePersistencia
						.obterListaJogadores(session);
				for (Iterator iterator = top.iterator(); iterator.hasNext();) {
					String nomeJogador = (String) iterator.next();
					JogadorDadosSrv carregaDadosJogador = ServletPaddock.controlePersistencia
							.carregaDadosJogador(nomeJogador, session);
					try {
						ServletPaddock.email
								.sendSimpleMail(assunto,
										new String[] { carregaDadosJogador
												.getEmail() },
										"admin@f1mane.com", texto, false);
						printWriter.write("Jogador : " + nomeJogador
								+ " E-mail " + carregaDadosJogador.getEmail());
						printWriter.write("<br>");
						cont++;
					} catch (Exception e) {
						printWriter.write("ERRO ENVIANDO MAIL : Jogador : "
								+ nomeJogador + " E-mail "
								+ carregaDadosJogador.getEmail());
						printWriter.write("<br>");
						cont++;
						continue;
					}
				}
			} finally {
				if (session.isOpen()) {
					session.close();
				}
			}
			printWriter.write("<br><hr><br>Emails Enviados " + cont
					+ "<br><hr>");
			printWriter.write("</body></html>");
			res.flushBuffer();
//			ServletPaddock.email.sendSimpleMail(assunto, new String[] {
//					"sowbreira@gmail.com"},
//					"admin@f1mane.com", texto, false);
		} catch (Exception e) {
			PrintWriter printWriter = null;
			try {
				printWriter = res.getWriter();
			} catch (IOException e1) {
				Logger.logarExept(e1);
			}
			e.printStackTrace(printWriter);
		}

	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
	}


}
