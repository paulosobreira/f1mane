package sowbreira.f1mane.paddock.servlet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sowbreira.f1mane.paddock.ZipUtil;

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

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		String tipo = req.getParameter("tipo");
		if (tipo == null) {
			return;
		}
		res.setHeader("Content-Disposition", "attachment;filename=\""
				+ "paddockDadosSrv" + tipo + "_"
				+ dateFormat.format(new Date()) + ".zip" + "\"");
		byte[] ret = controlePersistencia.obterBytesBase(tipo);
		if (ret == null) {
			return;
		}
		res.getOutputStream().write(ret);
	}

	public static void main(String[] args) {
		Enumeration e = System.getProperties().propertyNames();
		while (e.hasMoreElements()) {
			String element = (String) e.nextElement();
			System.out.print(element + " - ");
			System.out.println(System.getProperties().getProperty(element));

		}

	}
}
