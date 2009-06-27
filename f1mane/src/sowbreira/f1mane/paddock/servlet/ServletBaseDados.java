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

import br.nnpe.Dia;

/**
 * @author paulo.sobreira
 * 
 */
public class ServletBaseDados extends HttpServlet {

	private String basePath;
	private ControlePersistencia controlePersistencia;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy");
	public static Map topExceptions = new HashMap();

	public void init() throws ServletException {
		super.init();
		try {
			basePath = getServletContext().getRealPath("") + File.separator
					+ "WEB-INF" + File.separator;
			controlePersistencia = new ControlePersistencia(basePath);
		} catch (Exception e) {
			e.printStackTrace();
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
		} else {
			res.setHeader("Content-Disposition", "attachment;filename=\""
					+ "paddockDadosSrv" + tipo + "_"
					+ dateFormat.format(new Date()) + ".zip" + "\"");
			byte[] ret = controlePersistencia.obterBytesBase(tipo);
			if (ret == null) {
				return;
			}
			res.getOutputStream().write(ret);
		}
	}

	private void topExceptions(HttpServletResponse res) throws IOException {
		PrintWriter printWriter = res.getWriter();
		printWriter.write("<h2>F1-Mane Paddock Exceções</h2><br><hr>");
		synchronized (topExceptions) {
			Set top = topExceptions.keySet();
			for (Iterator iterator = top.iterator(); iterator.hasNext();) {
				String exept = (String) iterator.next();
				printWriter.write("Quantidade : " + topExceptions.get(exept));
				printWriter.write("<br>");
				printWriter.write(exept);
				printWriter.write("<br><hr>");
			}

			res.flushBuffer();
		}

	}

	public static void main(String[] args) {
		Enumeration e = System.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			String element = (String) e.nextElement();
			System.out.print(element + " - ");
			System.out.println(System.getProperties().getProperty(element));

		}

	}

	public static void topExecpts(Exception e) {
		if (topExceptions.size() < 100) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 5) ? 5 : trace.length);
			retorno.append(e.getClass() + " - " + e.getLocalizedMessage()
					+ "<br>");
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "<br>");
			String val = retorno.toString();
			Integer numExceps = (Integer) topExceptions.get(val);
			if (numExceps == null) {
				topExceptions.put(val, new Integer(1));
			} else {
				topExceptions.put(val, new Integer(numExceps.intValue() + 1));
			}
		}

	}
}
