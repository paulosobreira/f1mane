
/**
 *
 */
package br.f1mane.servidor.applet;

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
import br.f1mane.recursos.idiomas.Lang;
import br.nnpe.Util;

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
    final DecimalFormat decimalFormat = new DecimalFormat("#,###");

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
        String host = JOptionPane.showInputDialog("Host. Esc to localhost.");
        if(Util.isNullOrEmpty(host)){
            host = "http://localhost";
        }
        appletPaddock.setCodeBase(new URL(host));
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
                    controlePaddockCliente.logar();
                }
            };

            Thread thread = new Thread(runnable);
            thread.start();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            StringBuilder retorno = new StringBuilder();
            int size = ((trace.length > 10) ? 10 : trace.length);

            for (int i = 0; i < size; i++)
                retorno.append(trace[i]).append("\n");
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
        properties.load(CarregadorRecursos.recursoComoStream("application.properties"));
        this.versao = properties.getProperty("versao");
        if (versao.contains(".")) {
            this.versao = versao.replaceAll("\\.", "");
        }

    }

    public void setCodeBase(URL codeBase) {
        this.codeBase = codeBase;
    }
}
