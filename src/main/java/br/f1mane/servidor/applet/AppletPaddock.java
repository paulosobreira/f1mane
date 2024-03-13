
/**
 *
 */
package br.f1mane.servidor.applet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
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
 */
public class AppletPaddock {

    private static final long serialVersionUID = -2007934906883016154L;
    private ControlePaddockCliente controlePaddockCliente;

    private JFrame frame;
    private URL codeBase;


    public static void main(String[] args) throws IOException {
        AppletPaddock appletPaddock = new AppletPaddock();
        appletPaddock.init();
    }

    public void init() {
        try {
            frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            CarregadorRecursos.initProperties();
            controlePaddockCliente = new ControlePaddockCliente(this);
            controlePaddockCliente.verificaVersao();
            controlePaddockCliente.init();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    if (getCodeBase() == null) {
                        String host = JOptionPane.showInputDialog(AppletPaddock.this.getFrame(), "Host. Esc to localhost.");
                        if (Util.isNullOrEmpty(host)) {
                            host = "http://localhost";
                        }
                        try {
                            setCodeBase(new URL(host));
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e);
                        }
                    }
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


    public void setCodeBase(URL codeBase) {
        this.codeBase = codeBase;
    }
}
