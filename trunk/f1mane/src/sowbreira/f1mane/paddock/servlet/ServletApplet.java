package sowbreira.f1mane.paddock.servlet;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
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
public class ServletApplet extends HttpServlet {

	private String basePath;

	public void init() throws ServletException {
		super.init();
		try {
			basePath = getServletContext().getRealPath("") + File.separator
					+ "WEB-INF" + File.separator;
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(
					new FileInputStream(basePath + File.separator + "lib"
							+ File.separator + "f1mane.jar"));
			int byt = bufferedInputStream.read();

			while (-1 != byt) {
				arrayOutputStream.write(byt);
				byt = bufferedInputStream.read();
			}
			FileOutputStream fileOutputStream = new FileOutputStream(
					getServletContext().getRealPath("") + File.separator
							+ "f1mane.jar");
			fileOutputStream.write(arrayOutputStream.toByteArray());
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
		ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				new FileInputStream(basePath + File.separator + "lib"
						+ File.separator + "f1mane.jar"));
		int byt = bufferedInputStream.read();

		while (-1 != byt) {
			arrayOutputStream.write(byt);
			byt = bufferedInputStream.read();
		}
		res.setContentType("application/octet-stream");
		res.setHeader("Content-Disposition", "attachment; filename=\""
				+ "f1mane.jar" + "\"");
		res.getOutputStream().write(arrayOutputStream.toByteArray());
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
