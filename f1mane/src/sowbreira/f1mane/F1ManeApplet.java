package sowbreira.f1mane;

import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

import br.nnpe.Util;

import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado Em 12:05:02
 */
public class F1ManeApplet extends JApplet {

	public void init() {
		MainFrame frame;
		try {
			String lang = getParameter("lang");
			if (!Util.isNullOrEmpty(lang)) {
				Lang.mudarIdioma(lang);
			}
			frame = new MainFrame(this);
			frame.setSize(810, 650);
			frame.setAlwaysOnTop(true);
			frame.setVisible(true);
		} catch (IOException e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(this, retorno.toString(), Lang
					.msg("059"), JOptionPane.ERROR_MESSAGE);
		}

	}

}
