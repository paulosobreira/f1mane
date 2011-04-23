package sowbreira.f1mane.paddock.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import br.nnpe.Logger;

public class ServletMail extends HttpServlet {

	public void doPost(HttpServletRequest arg0, HttpServletResponse arg1)
			throws ServletException, IOException {
		String tipo = arg0.getParameter("tipo");
		if ("admail".equals(tipo)) {
			adMail(arg0.getParameter("assunto"), arg0.getParameter("texto"),
					arg0.getParameter("passe"), arg1);
		}
	}

	private void adMail(String assunto, String texto, String passe,
			HttpServletResponse res) {
		try {
			ServletPaddock.email.sendSimpleMail(assunto, new String[] {
					"sowbreira@gmail.com", "gizelehidaka@gmail.com" },
					"admin@f1mane.com", texto, false);
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
