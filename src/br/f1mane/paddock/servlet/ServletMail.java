package br.f1mane.paddock.servlet;

import java.io.File;
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
import br.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;

public class ServletMail extends HttpServlet {

	private ControlePersistencia controlePersistencia;

	public void init() throws ServletException {
		super.init();
		try {
			controlePersistencia = new ControlePersistencia(
					getServletContext().getRealPath("") + File.separator
							+ "WEB-INF" + File.separator);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}
	public void doPost(HttpServletRequest request,
			HttpServletResponse responose)
					throws ServletException, IOException {
		String tipo = request.getParameter("tipo");
		String passe = request.getParameter("passe");
		try {
			if (Util.isNullOrEmpty(passe) || !Util.md5(passe)
					.equals("c846d80d826291f2a6a0d7a57e540307")) {
				return;
			}
			if ("admail".equals(tipo)) {
				adMail(request.getParameter("assunto"),
						request.getParameter("texto"), responose);
			} else if ("recuperar".equals(tipo)) {
				recuperar(request.getParameter("email"), responose);
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
			printWriter.write("<h2>Fl-Mane Recuperar</h2><br><hr>");
			Session session = controlePersistencia.getSession();
			String senha, nome;
			try {
				List jogador = session.createCriteria(JogadorDadosSrv.class)
						.add(Restrictions.eq("email", emailJogador)).list();
				JogadorDadosSrv jogadorDadosSrv = (JogadorDadosSrv) (jogador
						.isEmpty() ? null : jogador.get(0));
				if (jogadorDadosSrv != null) {
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
					printWriter.write("<br>Gerado Nova Senha<br><hr>");
					printWriter.write("<br>E-Mail : " + emailJogador);
					printWriter.write("<br>Nome : " + nome);
					printWriter.write("<br>Senha : " + senha);
				}else{
					printWriter.write("<br>Email n&atilde;o encontrado<br>");
				}
			} finally {
				if (session.isOpen()) {
					session.close();
				}
			}
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
			Session session = controlePersistencia.getSession();
			int cont = 0;
			try {
				Set top = controlePersistencia.obterListaJogadores(session);
				for (Iterator iterator = top.iterator(); iterator.hasNext();) {
					String nomeJogador = (String) iterator.next();
					JogadorDadosSrv carregaDadosJogador = controlePersistencia
							.carregaDadosJogador(nomeJogador, session);
					try {
						// email.sendSimpleMail(assunto,
						// new String[]{carregaDadosJogador.getEmail()},
						// texto, false);
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

	private void adMail(String assunto, String texto, String passe,
			HttpServletResponse res) {
		try {
			// email.sendSimpleMail(assunto,
			// new String[] { "sowbreira@gmail.com" }, texto, false);
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
