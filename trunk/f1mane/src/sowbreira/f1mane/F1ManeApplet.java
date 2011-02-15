package sowbreira.f1mane;

import java.io.IOException;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado Em 12:05:02
 */
public class F1ManeApplet extends JApplet {

	public void init() {
		MainFrame frame;
		try {
			frame = new MainFrame(this);
			frame.setVisible(true);
			frame.setSize(810, 650);
		} catch (IOException e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);
			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(this, retorno.toString(),
					Lang.msg("059"), JOptionPane.ERROR_MESSAGE);
		}

	}

}
