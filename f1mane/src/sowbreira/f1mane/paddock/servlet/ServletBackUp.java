package sowbreira.f1mane.paddock.servlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author paulo.sobreira
 * 
 */
public class ServletBackUp extends HttpServlet {

	private ControlePersistencia controlePersistencia;
	public static String webInfDir;

	public static String webDir;
	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"dd/MM/yyyy");

	public void init() throws ServletException {
		super.init();
		webDir = getServletContext().getRealPath("") + File.separator;
		webInfDir = webDir + "WEB-INF" + File.separator;
		Lang.setSrvgame(true);
		controlePersistencia = new ControlePersistencia(webDir, webInfDir);
	}

	public void destroy() {
		super.destroy();
	}

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doGet(arg0, arg1);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			res.setHeader("Content-Disposition", "attachment;filename=\""
					+ "algol_data" + "_" + dateFormat.format(new Date())
					+ ".zip" + "\"");

			byte[] ret = controlePersistencia.obterBytesBase();
			if (ret == null) {
				return;
			}
			res.getOutputStream().write(ret);
			res.flushBuffer();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
	}

	public static void main(String[] args) {

	}
}
