package sowbreira.f1mane.paddock.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author paulo.sobreira
 * 
 */
public class ServletPaddock extends HttpServlet {

	private static ControlePaddockServidor controlePaddock;
	private static ControlePersistencia controlePersistencia;
	private static boolean debug = false;
	private static MonitorAtividade monitorAtividade;

	public void init() throws ServletException {
		super.init();
		try {
			controlePersistencia = new ControlePersistencia(getServletContext()
					.getRealPath("")
					+ File.separator + "WEB-INF" + File.separator);
		} catch (Exception e) {
			e.printStackTrace();
		}
		controlePaddock = new ControlePaddockServidor(controlePersistencia);
		monitorAtividade = new MonitorAtividade(controlePaddock);
		Thread monitor = new Thread(monitorAtividade);

		monitor.start();
	}

	public void destroy() {
		monitorAtividade.setAlive(false);
		try {
			controlePersistencia.gravarDados();
		} catch (IOException e) {
			e.printStackTrace();
		}
		super.destroy();
	}

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doGet(arg0, arg1);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		ObjectInputStream inputStream = new ObjectInputStream(req
				.getInputStream());

		if (inputStream != null) {
			Object object = null;
			try {
				object = inputStream.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			Object escrever = controlePaddock.processarObjetoRecebido(object);

			dumaparDados(escrever);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(escrever);
			oos.flush();
			res.getOutputStream().write(bos.toByteArray());
			// ByteArrayOutputStream ziped = ZipUtil.compactarObjeto(escrever,
			// res
			// .getOutputStream());

			// dumaparDados(bos);

			return;
		} else {
			System.out.println("Input null");
		}

		PrintWriter printWriter = res.getWriter();
		printWriter.write("ServletPaddock Ok");
		res.flushBuffer();
	}

	private void dumaparDados(Object escrever) throws IOException {
		if (debug && (escrever != null)) {
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
		// System.out.println(System.getProperties().getProperty(element));
		//
		// }

	}
}
