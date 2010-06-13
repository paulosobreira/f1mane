package sowbreira.f1mane.paddock.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author Paulo Sobreira [sowbreira@gmail.com]
 * @author Rafael Carneiro [rafaelcarneirob@gmail.com]
 */
public class HypersonicServlet extends HttpServlet {

//	private Server server = new Server();

	public void init() throws ServletException {

//		String dbFile = getServletContext().getRealPath("") + File.separator
//				+ "WEB-INF" + File.separator + "hipersonic" + File.separator
//				+ "hsqldb";
//
//		server.setDatabaseName(0, "hsqldb");
//		server.setDatabasePath(0, "file:" + dbFile);
//		server.setLogWriter(new PrintWriter(System.out));
//		server.setErrWriter(new PrintWriter(System.out));
//		server.setSilent(false);
//		server.start();
//		Logger.logar("Hipersonic Started");
		super.init();
	}

	public void destroy() {
//		server.shutdown();
//		Logger.logar("Hipersonic Stoped");
		super.destroy();
	}

}
