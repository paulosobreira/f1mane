/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author paulo.sobreira
 * 
 */
public class AppletPaddock extends JApplet {

	private static final long serialVersionUID = -2007934906883016154L;
	private ControlePaddockCliente controlePaddockApplet;

	public void init() {
		super.init();

		try {
			Properties properties = new Properties();
			properties.load(this.getClass().getResourceAsStream(
					"client.properties"));
			controlePaddockApplet = new ControlePaddockCliente(this
					.getCodeBase(), this);
			controlePaddockApplet.logar();
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(this, retorno.toString(), Lang
					.msg("059"), JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}

	}

	public void destroy() {
		controlePaddockApplet.sairPaddock();
		super.destroy();
	}

	public static void main(String[] args) {
		// Logger.logar(JOptionPane.showInputDialog("teste"));
	}

}
