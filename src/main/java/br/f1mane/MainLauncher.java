package br.f1mane;

import br.f1mane.recursos.CarregadorRecursos;
import br.nnpe.ImageUtil;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.util.Enumeration;

public class MainLauncher {


    public static void main(String[] args) {

        try {

            int port = 8080;

            String webappDir = "src/main/webapp";

            File base = new File(webappDir);

            System.out.println("WEBAPP: " + base.getAbsolutePath());

            if (!base.exists()) {
                throw new RuntimeException(
                        "Diretorio webapp nao encontrado: "
                                + base.getAbsolutePath());
            }

            Tomcat tomcat = new Tomcat();

            tomcat.setPort(port);

            tomcat.getConnector();

            Context context = tomcat.addWebapp(
                    "/flmane",
                    base.getAbsolutePath());

            File webXml = new File(base, "WEB-INF/web.xml");

            if (webXml.exists()) {
                context.setConfigFile(webXml.toURI().toURL());
                System.out.println("WEB.XML: " + webXml.getAbsolutePath());
            }

            tomcat.start();

            String ip = descobrirIP();

            String url = "http://" + ip + ":" + port
                    + "/flmane/html5/index.html";

            System.out.println("=================================");
            System.out.println("SERVER STARTED");
            System.out.println(url);
            System.out.println("=================================");

            boolean abrirJanela = false;

            if (args != null) {
                for (String arg : args) {
                    if ("qr".equalsIgnoreCase(arg)) {
                        abrirJanela = true;
                    }
                }
            }

            if (abrirJanela) {
                mostrarLauncher(url);
            }

            tomcat.getServer().await();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void mostrarLauncher(String url) throws Exception {
        JFrame frame = new JFrame();
        frame.setSize(1280, 720);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setTitle(
                "Fl-MANE " +
                        CarregadorRecursos.getVersaoFormatado());
        BufferedImage bg1 = ImageUtil.gerarFade(
                CarregadorRecursos.carregaBufferedImage("bg/bg1.jpg"),
                40);
        JPanel backgroundPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(
                        bg1,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        null);
            }
        };
        backgroundPanel.setLayout(new GridBagLayout());
        frame.setContentPane(backgroundPanel);
        JPanel painel = new JPanel();
        painel.setOpaque(false);
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBorder(
                BorderFactory.createEmptyBorder(
                        20,
                        20,
                        20,
                        20));
        JLabel titulo = new JLabel("Fl-MANE");
        titulo.setFont(new Font("Arial", Font.BOLD, 72));
        titulo.setForeground(Color.BLACK);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(titulo);
        painel.add(Box.createVerticalStrut(18));
        BufferedImage qrImage = gerarQRCode(url, 300, 300);
        JLabel qrLabel = new JLabel(new ImageIcon(qrImage));
        qrLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        painel.add(qrLabel);
        painel.add(Box.createVerticalStrut(18));
        JTextField campo = new JTextField(url);
        campo.setEditable(false);
        campo.setMaximumSize(new Dimension(700, 35));
        campo.setHorizontalAlignment(JTextField.CENTER);
        painel.add(campo);
        painel.add(Box.createVerticalStrut(18));
        JLabel copiar = criarMenuLabel(
                "Copy link",
                () -> {
                    Toolkit.getDefaultToolkit()
                            .getSystemClipboard()
                            .setContents(
                                    new StringSelection(url),
                                    null);
                });
        painel.add(copiar);
        painel.add(Box.createVerticalStrut(8));
        JLabel abrirWeb = criarMenuLabel(
                "Open in browser",
                () -> {
                    try {
                        Desktop.getDesktop()
                                .browse(new URI(url));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        painel.add(abrirWeb);
        painel.add(Box.createVerticalStrut(8));
        JLabel abrirDesktop = criarMenuLabel(
                "Java solo game",
                () -> {
                    try {
                        ProcessBuilder pb =
                                new ProcessBuilder(
                                        "java",
                                        "-Xms64m",
                                        "-Xmx512m",
                                        "-cp",
                                        "target/flmane.jar",
                                        "br.f1mane.MainFrame"
                                );
                        pb.inheritIO();
                        pb.start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        painel.add(abrirDesktop);

        painel.add(Box.createVerticalStrut(8));
        JLabel abrirDesktopMulti = criarMenuLabel(
                "Java multiplayer game",
                () -> {
                    try {
                        ProcessBuilder pb =
                                new ProcessBuilder(
                                        "java",
                                        "-Xms64m",
                                        "-Xmx512m",
                                        "-cp",
                                        "target/flmane.jar",
                                        "br.f1mane.servidor.applet.AppletPaddock"
                                );
                        pb.start();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        painel.add(abrirDesktopMulti);

        backgroundPanel.add(painel);

        frame.setVisible(true);
    }

    private static JLabel criarMenuLabel(
            String texto,
            Runnable acao) {

        JLabel label = new JLabel(texto);

        label.setFont(new Font("Arial", Font.BOLD, 32));

        label.setForeground(Color.BLACK);

        label.setAlignmentX(Component.CENTER_ALIGNMENT);

        label.setCursor(
                Cursor.getPredefinedCursor(
                        Cursor.HAND_CURSOR));

        label.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {

                label.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {

                label.setForeground(Color.BLACK);
            }

            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {

                acao.run();
            }
        });

        return label;
    }

    private static BufferedImage gerarQRCode(
            String texto,
            int largura,
            int altura) throws Exception {

        BitMatrix matrix =
                new MultiFormatWriter().encode(
                        texto,
                        BarcodeFormat.QR_CODE,
                        largura,
                        altura);

        return MatrixToImageWriter.toBufferedImage(matrix);
    }

    private static String descobrirIP() {

        try {

            Enumeration<NetworkInterface> interfaces =
                    NetworkInterface.getNetworkInterfaces();

            while (interfaces.hasMoreElements()) {

                NetworkInterface ni = interfaces.nextElement();

                if (!ni.isUp() || ni.isLoopback()) {
                    continue;
                }

                Enumeration<InetAddress> addresses =
                        ni.getInetAddresses();

                while (addresses.hasMoreElements()) {

                    InetAddress addr = addresses.nextElement();

                    if (addr instanceof Inet4Address
                            && !addr.isLoopbackAddress()) {

                        return addr.getHostAddress();
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "localhost";
    }
}