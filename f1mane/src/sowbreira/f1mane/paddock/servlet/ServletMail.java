package sowbreira.f1mane.paddock.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;

import br.nnpe.Logger;
import br.nnpe.PassGenerator;
import br.nnpe.Util;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;

public class ServletMail extends HttpServlet {

	public void doPost(HttpServletRequest arg0, HttpServletResponse responose)
			throws ServletException, IOException {
		String tipo = arg0.getParameter("tipo");
		String passe = arg0.getParameter("passe");
		try {
			if (Util.isNullOrEmpty(passe) || !Util.md5(passe)
					.equals("c846d80d826291f2a6a0d7a57e540307")) {
				return;
			}
			if ("admail".equals(tipo)) {
				adMail(arg0.getParameter("assunto"), arg0.getParameter("texto"),
						responose);
			} else if ("recuperar".equals(tipo)) {
				recuperar(arg0.getParameter("email"), responose);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	private void recuperar(String emailJogador, HttpServletResponse res) {
		try {
			res.setContentType("text/html");
			PrintWriter printWriter = res.getWriter();
			printWriter.write("<html><body>");
			printWriter.write("<h2>F1-Mane Recuperar</h2><br><hr>");
			Session session = ServletPaddock.controlePersistencia.getSession();
			String senha, nome;
			try {
				List jogador = session.createCriteria(JogadorDadosSrv.class)
						.add(Restrictions.eq("email", emailJogador)).list();
				JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador
						.isEmpty() ? null : jogador.get(0));
				nome = jogadorDadosSrv.getNome();
				PassGenerator generator = new PassGenerator();
				senha = generator.generateIt();
				jogadorDadosSrv.setSenha(Util.md5(senha));
				jogadorDadosSrv
						.setUltimaRecuperacao(System.currentTimeMillis());
				Transaction transaction = session.beginTransaction();
				try {
					session.saveOrUpdate(jogadorDadosSrv);
					transaction.commit();
				} catch (Exception e) {
					transaction.rollback();
					throw e;
				}
			} finally {
				if (session.isOpen()) {
					session.close();
				}
			}
			printWriter.write("<br>Gerado Nova Senha<br><hr>");
			printWriter.write("<br>E-Mail : " + emailJogador);
			printWriter.write("<br>Nome : " + nome);
			printWriter.write("<br>Senha : " + senha);
			printWriter.write("<br><hr>");
			printWriter.write("</body></html>");
			res.flushBuffer();
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

	private void adMail(String assunto, String texto, HttpServletResponse res)
			throws NoSuchAlgorithmException, UnsupportedEncodingException {
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
						ServletPaddock.email.sendSimpleMail(assunto,
								new String[]{carregaDadosJogador.getEmail()},
								texto, false);
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
			printWriter
					.write("<br><hr><br>Emails Enviados " + cont + "<br><hr>");
			printWriter.write("</body></html>");
			res.flushBuffer();
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
