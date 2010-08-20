/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.awt.Color;
import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author paulo.sobreira
 * 
 */
public class JFramePaddock extends JFrame {

	private static final long serialVersionUID = -2007934906883016154L;
	private ControlePaddockCliente controlePaddockApplet;
	private URL url;

	public void init() {

		try {
			url = new URL(JOptionPane.showInputDialog(Lang.msg("172")));
			Panel panel = new Panel();
			panel.setBackground(Color.WHITE);
			getContentPane().add(panel);
			controlePaddockApplet = new ControlePaddockCliente(url, panel);
			controlePaddockApplet.logar();
			setTitle("F1-Mane Webpaddock Cliente 1.0");
			setVisible(true);
			addWindowListener(new WindowAdapter() {

				public void windowClosing(WindowEvent e) {
					controlePaddockApplet.sairPaddock();
					super.windowClosing(e);
					System.exit(0);
				}

			});
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(this, retorno.toString(), "Erro",
					JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
			System.exit(0);
		}

	}

	public static void main(String[] args) {
		JFramePaddock framePaddock = new JFramePaddock();
		framePaddock.setSize(900, 600);
		framePaddock.init();
	}

}
