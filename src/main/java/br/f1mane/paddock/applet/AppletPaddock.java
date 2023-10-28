
/**
 * 
 */
package br.f1mane.paddock.applet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import br.f1mane.recursos.CarregadorRecursos;
import br.nnpe.Logger;
import br.f1mane.paddock.PaddockConstants;
import br.f1mane.recursos.idiomas.Lang;

/**
 * @author paulo.sobreira
 * 
 */
public class AppletPaddock {

	private static final long serialVersionUID = -2007934906883016154L;
	private ControlePaddockCliente controlePaddockCliente;
	private String versao;
	private JFrame frame;
	private URL codeBase;
	DecimalFormat decimalFormat = new DecimalFormat("#,###");

	public String getVersaoFormatado() {
		if (versao == null) {
			try {
				initProperties();
			} catch (IOException e) {
				Logger.logarExept(e);
			}
		}
		return decimalFormat.format(new Integer(versao));
	}

	public String getVersao() {
		if (versao == null) {
			try {
				initProperties();
			} catch (IOException e) {
				Logger.logarExept(e);
			}
		}
		return versao;
	}

	public static void main(String[] args) throws IOException {
		AppletPaddock appletPaddock = new AppletPaddock();
		if (args != null && args.length > 0) {
			Logger.logar("codeBase "+args[0]);
			appletPaddock.codeBase = new URL(args[0]);
		}
		if (args != null && args.length > 1) {
			Logger.logar("lang "+args[1]);
			Lang.mudarIdioma(args[1]);
		}
		if(appletPaddock.getCodeBase()==null){
			URL url = new URL("https://sowbreira-26fe1.firebaseapp.com/f1mane/host");
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String urlDeploy = reader.readLine();
			appletPaddock.codeBase = new URL(urlDeploy.trim());
			
		}
		appletPaddock.init();
	}

	public void init() {
		try {
			frame = new JFrame();
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			initProperties();
			controlePaddockCliente = new ControlePaddockCliente(
					this.getCodeBase(), this);
			controlePaddockCliente.verificaVersao();
			controlePaddockCliente.init();
			Runnable runnable = new Runnable() {
				@Override
				public void run() {
					controlePaddockCliente.logarGuest();
				}
			};

			Thread thread = new Thread(runnable);
			thread.start();
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuilder retorno = new StringBuilder();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(null, retorno.toString(),
					Lang.msg("059"), JOptionPane.ERROR_MESSAGE);
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
		properties.load(CarregadorRecursos.recursoComoStream("client.properties"));
		this.versao = properties.getProperty("versao");
		if (versao.contains(".")) {
			this.versao = versao.replaceAll("\\.", "");
		}

	}

}
