/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.swing.JApplet;
import javax.swing.JOptionPane;

import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author paulo.sobreira
 * 
 */
public class AppletPaddock extends JApplet {

	private static final long serialVersionUID = -2007934906883016154L;
	private ControlePaddockCliente controlePaddockApplet;
	private String versao;
	DecimalFormat decimalFormat = new DecimalFormat("#,##");

	public String getVersao() {
		return " " + decimalFormat.format(new Integer(versao));
	}

	public void init() {
		super.init();

		try {
			String lang = getParameter("lang");
			if (!Util.isNullOrEmpty(lang)) {
				Lang.mudarIdioma(lang);
			}
			initProperties();
			controlePaddockApplet = new ControlePaddockCliente(
					this.getCodeBase(), this);
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					controlePaddockApplet.logar();
				}
			};
			Thread thread = new Thread(runnable);
			thread.start();
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(this, retorno.toString(),
					Lang.msg("059"), JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}

	}

	public void initProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass()
				.getResourceAsStream("client.properties"));
		this.versao = properties.getProperty("versao");

	}

	public void destroy() {
		controlePaddockApplet.sairPaddock();
		super.destroy();
	}

	public static void main(String[] args) {
		// Logger.logar(JOptionPane.showInputDialog("teste"));
	}

}