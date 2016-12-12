/**
 * 
 */
package sowbreira.f1mane.paddock.applet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author paulo.sobreira
 * 
 */
public class AppletPaddock {

	private static final long serialVersionUID = -2007934906883016154L;
	private ControlePaddockCliente controlePaddockApplet;
	private String versao;
	private JFrame frame;
	private URL codeBase;
	DecimalFormat decimalFormat = new DecimalFormat("#,###");

	public String getVersao() {
		if (versao == null) {
			try {
				initProperties();
			} catch (IOException e) {
				Logger.logarExept(e);
			}
		}
		return " " + decimalFormat.format(new Integer(versao));
	}
	
	public static void main(String[] args) throws MalformedURLException {
		AppletPaddock appletPaddock = new AppletPaddock();
		if (args != null && args.length > 0) {
			Logger.logar("codeBase "+args[0]);
			appletPaddock.codeBase = new URL(args[0]);
		}
		if (args != null && args.length > 1) {
			Logger.logar("lang "+args[1]);
			Lang.mudarIdioma(args[1]);
		}
		appletPaddock.init();
	}
	
	public void init() {
		try {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			initProperties();
			controlePaddockApplet = new ControlePaddockCliente(this
					.getCodeBase(), this);
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
			JOptionPane.showMessageDialog(null, retorno.toString(), Lang
					.msg("059"), JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}

	}


	public URL getCodeBase() {
		return codeBase;
	}
	
	public JFrame getFrame() {
		return frame;
	}

	public void initProperties() throws IOException {
		Properties properties = new Properties();
		properties.load(this.getClass()
				.getResourceAsStream("client.properties"));
		this.versao = properties.getProperty("versao");
		if (versao.contains(".")) {
			this.versao = versao.replaceAll("\\.", "");
		}

	}

}
