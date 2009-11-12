package sowbreira.f1mane.paddock.servlet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sowbreira.f1mane.paddock.PaddockConstants;
import sowbreira.f1mane.paddock.ZipUtil;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Email;
import br.nnpe.Logger;

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
		email = new Email(getServletContext().getRealPath("") + File.separator
				+ "WEB-INF" + File.separator);
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
		try {
			controlePersistencia.gravarDados();
		} catch (IOException e) {
			Logger.topExecpts(e);
		}
		super.destroy();
	}

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		doGet(arg0, arg1);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		try {
			ObjectInputStream inputStream = new ObjectInputStream(req
					.getInputStream());

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
				Logger.logar("Input null");
			}

			PrintWriter printWriter = res.getWriter();
			printWriter.write("ServletPaddock Ok");
			res.flushBuffer();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
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
