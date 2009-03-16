/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.awt.Panel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
			url = new URL(JOptionPane
					.showInputDialog("Entre com a url do servidor."));
			Panel panel = new Panel();
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
			JOptionPane.showMessageDialog(this, retorno.toString(),
					"Erro enviando dados", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			System.exit(0);
		}

	}


	public static void main(String[] args) {
		JFramePaddock framePaddock = new JFramePaddock();
		framePaddock.setSize(900, 600);
		framePaddock.init();
	}

}
