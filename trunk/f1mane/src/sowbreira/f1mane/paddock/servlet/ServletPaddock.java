package sowbreira.f1mane.paddock.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.dialect.Dialect;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;

import sowbreira.f1mane.paddock.PaddockConstants;
import sowbreira.f1mane.paddock.ZipUtil;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Email;
import br.nnpe.HibernateUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class ServletPaddock extends HttpServlet {

	private static ControlePaddockServidor controlePaddock;
	private static ControlePersistencia controlePersistencia;
	private static MonitorAtividade monitorAtividade;

	public static Email email;

	public void init() throws ServletException {
		super.init();
		try {
			email = new Email(getServletContext().getRealPath("")
					+ File.separator + "WEB-INF" + File.separator);
		} catch (Exception e) {
			Logger.logarExept(e);
			email = null;
		}
		Lang.setSrvgame(true);
		try {
			controlePersistencia = new ControlePersistencia(getServletContext()
					.getRealPath("")
					+ File.separator + "WEB-INF" + File.separator);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		controlePaddock = new ControlePaddockServidor(controlePersistencia);
		monitorAtividade = new MonitorAtividade(controlePaddock);
		Thread monitor = new Thread(monitorAtividade);
		monitor.start();
	}

	public void destroy() {
		monitorAtividade.setAlive(false);
//		try {
//			controlePersistencia.gravarDados();
//		} catch (IOException e) {
//			Logger.topExecpts(e);
//		}
		super.destroy();
	}

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doGet(arg0, arg1);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {

			ObjectInputStream inputStream = null;
			try {
				inputStream = new ObjectInputStream(req.getInputStream());
			} catch (Exception e) {
				Logger.logar("inputStream null - > doGetHtml");
			}

			if (inputStream != null) {
				Object object = null;

				object = inputStream.readObject();

				Object escrever = controlePaddock
						.processarObjetoRecebido(object);

				if (PaddockConstants.modoZip) {
					dumaparDadosZip(ZipUtil.compactarObjeto(
							PaddockConstants.debug, escrever, res
									.getOutputStream()));
				} else {
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					dumaparDados(escrever);
					ObjectOutputStream oos = new ObjectOutputStream(bos);
					oos.writeObject(escrever);
					oos.flush();
					res.getOutputStream().write(bos.toByteArray());
				}

				return;
			} else {
				doGetHtml(req, res);
				return;
			}

		} catch (Exception e) {
			Logger.topExecpts(e);
		}
	}

	public void doGetHtml(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		PrintWriter printWriter = res.getWriter();
		res.setContentType("text/html");
		try {
			printWriter.println("<html><body>");
			String tipo = req.getParameter("tipo");
			AnnotationConfiguration cfg = new AnnotationConfiguration();
			cfg.configure("hibernate.cfg.xml");

			SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
			if (tipo == null) {
				return;
			} else if ("x".equals(tipo)) {
				topExceptions(res);
			} else if ("C".equals(tipo)) {
				topConstrutors(res);
			} else if ("create_schema".equals(tipo)) {
				createSchema(cfg, sessionFactory, printWriter);
			} else if ("update_schema".equals(tipo)) {
				updateSchema(cfg, sessionFactory, printWriter);
			}
			printWriter.println("<br/> " + tipo + " done");
		} catch (Exception e) {
			printWriter.println(e.getMessage());
		}
		printWriter.println("<br/><a href='conf.jsp'>back</a>");
		printWriter.println("</body></html>");
		res.flushBuffer();
	}

	private void updateSchema(AnnotationConfiguration cfg,
			SessionFactory sessionFactory, PrintWriter printWriter)
			throws SQLException {
		Dialect dialect = Dialect.getDialect(cfg.getProperties());
		Session session = sessionFactory.openSession();
		DatabaseMetadata meta = new DatabaseMetadata(session.connection(),
				dialect);
		String[] strings = cfg.generateSchemaUpdateScript(dialect, meta);
		executeStatement(sessionFactory, strings, printWriter);

	}

	private void executeStatement(SessionFactory sessionFactory,
			String[] strings, PrintWriter printWriter) throws SQLException {

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		for (int i = 0; i < strings.length; i++) {
			String string = strings[i];
			java.sql.Statement statement = session.connection()
					.createStatement();
			statement.execute(string);
			printWriter.println("<br/> " + string);
		}

		session.flush();

	}

	private void createSchema(AnnotationConfiguration cfg,
			SessionFactory sessionFactory, PrintWriter printWriter)
			throws HibernateException, SQLException {
		Dialect dialect = Dialect.getDialect(cfg.getProperties());
		String[] strings = cfg.generateSchemaCreationScript(dialect);
		executeStatement(sessionFactory, strings, printWriter);
	}

	private void topExceptions(HttpServletResponse res) throws IOException {
		res.setContentType("text/html");
		PrintWriter printWriter = res.getWriter();
		printWriter.write("<html><body>");
		printWriter.write("<h2>F1-Mane Exceções</h2><br><hr>");
		synchronized (Logger.topExceptions) {
			Set top = Logger.topExceptions.keySet();
			for (Iterator iterator = top.iterator(); iterator.hasNext();) {
				String exept = (String) iterator.next();
				printWriter.write("Quantidade : "
						+ Logger.topExceptions.get(exept));
				printWriter.write("<br>");
				printWriter.write(exept);
				printWriter.write("<br><hr>");
			}
		}
		printWriter.write("</body></html>");
		res.flushBuffer();
	}

	private void topConstrutors(HttpServletResponse res) throws IOException {
		res.setContentType("text/html");
		PrintWriter printWriter = res.getWriter();
		printWriter.write("<html><body>");
		printWriter.write("<h2>F1-Mane Construtores</h2><br><hr>");
		synchronized ("") {
			Set top = controlePersistencia.obterListaJogadores();
			for (Iterator iterator = top.iterator(); iterator.hasNext();) {
				String nomeJogador = (String) iterator.next();
				CarreiraDadosSrv carreiraDadosSrv = controlePersistencia
						.carregaCarreiraJogador(nomeJogador, false,
								controlePersistencia.getSession());
				if (carreiraDadosSrv == null) {
					continue;
				}
				if (Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
						|| Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())) {
					continue;
				}
				printWriter.write("Jogador : " + nomeJogador);
				printWriter.write("<br> Pts Piloto: "
						+ carreiraDadosSrv.getPtsPiloto());
				printWriter.write("<br> Pts Carro: "
						+ carreiraDadosSrv.getPtsCarro());
				printWriter.write("<br> Pts Const: "
						+ carreiraDadosSrv.getPtsConstrutores());
				printWriter.write("<br><hr>");
			}
		}
		printWriter.write("</body></html>");
		res.flushBuffer();

	}

	private void dumaparDadosZip(ByteArrayOutputStream byteArrayOutputStream)
			throws IOException {
		if (PaddockConstants.debug) {
			String basePath = getServletContext().getRealPath("")
					+ File.separator + "WEB-INF" + File.separator;
			FileOutputStream fileOutputStream = new FileOutputStream(basePath
					+ "Pack-" + System.currentTimeMillis() + ".zip");
			fileOutputStream.write(byteArrayOutputStream.toByteArray());
			fileOutputStream.close();

		}

	}

	private void dumaparDados(Object escrever) throws IOException {
		if (PaddockConstants.debug && (escrever != null)) {
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(
					arrayOutputStream);
			objectOutputStream.writeObject(escrever);
			String basePath = getServletContext().getRealPath("")
					+ File.separator + "WEB-INF" + File.separator;
			FileOutputStream fileOutputStream = new FileOutputStream(basePath
					+ escrever.getClass().getSimpleName() + "-"
					+ System.currentTimeMillis() + ".txt");
			fileOutputStream.write(arrayOutputStream.toByteArray());
			fileOutputStream.close();

		}

	}

	public static void main(String[] args) {
		// Enumeration e = System.getProperties().propertyNames();
		// while (e.hasMoreElements()) {
		// String element = (String) e.nextElement();
		// System.out.print(element + " - ");
		// Logger.logar(System.getProperties().getProperty(element));
		//
		// }

	}
}
