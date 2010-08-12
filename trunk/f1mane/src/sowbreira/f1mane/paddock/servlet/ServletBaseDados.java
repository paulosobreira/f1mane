package sowbreira.f1mane.paddock.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class ServletBaseDados extends HttpServlet {

	private String basePath;
	private ControlePersistencia controlePersistencia;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy");

	public void init() throws ServletException {
		super.init();
		try {
			basePath = getServletContext().getRealPath("") + File.separator
					+ "WEB-INF" + File.separator;
			controlePersistencia = new ControlePersistencia(basePath);
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void destroy() {
		super.destroy();
	}

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doGet(arg0, arg1);
	}

	/**
	 * http://server:8080/paddock/ServletBaseDados?tipo=
	 */
	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String tipo = req.getParameter("tipo");
		if (tipo == null) {
			return;
		} else if ("x".equals(tipo)) {
			topExceptions(res);
		} else if ("C".equals(tipo)) {
			topConstrutors(res);
		} else {
			// res.setHeader("Content-Disposition", "attachment;filename=\""
			// + "paddockDadosSrv" + tipo + "_"
			// + dateFormat.format(new Date()) + ".zip" + "\"");
			// byte[] ret = controlePersistencia.obterBytesBase(tipo);
			// if (ret == null) {
			// return;
			// }
			// res.getOutputStream().write(ret);
			// try {
			// controlePersistencia.migrar();
			// } catch (Exception e) {
			// Logger.logarExept(e);
			// }
		}
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
						.carregaCarreiraJogador(nomeJogador, false);
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

	public static void main(String[] args) {
		Enumeration e = System.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			String element = (String) e.nextElement();
			System.out.print(element + " - ");
			Logger.logar(System.getProperties().getProperty(element));

		}

	}

}
