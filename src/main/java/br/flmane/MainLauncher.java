package br.flmane;

import br.flmane.recursos.CarregadorRecursos;
import br.flmane.recursos.idiomas.Lang;
import br.flmane.servidor.applet.AppletPaddock;
import br.flmane.visao.PainelCircuito;
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
import java.io.IOException;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MainLauncher {


    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {
            if (contemHeadless(args)) {
                iniciarServidorHeadless();
                return;
            }
            // Modo GUI: o backend sobe numa JVM filha (--headless) pra que o
            // Lang estático do launcher/Swing não seja alterado pelas
            // traduções por request do servidor.
            String jar = localizarJar();
            Process servidor = iniciarProcessoServidor(jar);
            Runtime.getRuntime().addShutdownHook(
                    new Thread(servidor::destroy));
            String ip = descobrirIP();
            String url = "http://" + ip + ":" + PORT
                    + "/flmane/html5/index.html";
            mostrarLauncher(url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean contemHeadless(String[] args) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if ("--headless".equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void iniciarServidorHeadless() throws Exception {
        File base = extrairWebapp();
        System.out.println(
                "WEBAPP: " +
                        base.getAbsolutePath());
        if (!base.exists()) {
            throw new RuntimeException(
                    "Diretorio webapp nao encontrado: "
                            + base.getAbsolutePath());
        }
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(PORT);
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
        String url = "http://" + ip + ":" + PORT
                + "/flmane/html5/index.html";
        System.out.println("=================================");
        System.out.println("SERVER STARTED");
        System.out.println(url);
        System.out.println("=================================");
        tomcat.getServer().await();
    }

    private static Process iniciarProcessoServidor(String jar)
            throws Exception {
        ProcessBuilder pb =
                new ProcessBuilder(
                        "java",
                        "-Xms64m",
                        "-Xmx512m",
                        "-cp",
                        jar,
                        "br.flmane.MainLauncher",
                        "--headless"
                );
        pb.inheritIO();
        return pb.start();
    }

    private static File extrairWebapp() throws Exception {
        Path destino = criarDiretorioTemporarioSeguro("flmane-webapp");
        CodeSource src =
                MainLauncher.class
                        .getProtectionDomain()
                        .getCodeSource();
        try (JarFile jar =
                     new JarFile(
                             new File(
                                     src.getLocation().toURI()))) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().startsWith("webapp/")) {
                    continue;
                }
                String relative =
                        entry.getName()
                                .substring("webapp/".length());
                if (relative.isEmpty()) {
                    continue;
                }
                Path target =
                        destino.resolve(relative);
                if (entry.isDirectory()) {
                    Files.createDirectories(target);
                    continue;
                }
                Files.createDirectories(target.getParent());
                try (InputStream in =
                             jar.getInputStream(entry)) {
                    Files.copy(
                            in,
                            target,
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
        return destino.toFile();
    }

    /**
     * Cria o diretório temporário dentro do home do usuário (nunca no
     * diretório compartilhado java.io.tmpdir, que em Linux/macOS é
     * publicamente gravável por qualquer usuário da máquina) e, quando o
     * filesystem suporta permissões POSIX, restringe o acesso ao dono do
     * processo. O webapp extraído ali (incluindo web.xml e estáticos) não
     * deve ficar legível/gravável por outros usuários locais.
     */
    private static Path criarDiretorioTemporarioSeguro(String prefixo)
            throws IOException {
        Path base = diretorioBaseTemporario();
        if (FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> apenasDono =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            return Files.createTempDirectory(base, prefixo, apenasDono);
        }
        return Files.createTempDirectory(base, prefixo);
    }

    /**
     * Diretório privado do usuário (~/.flmane/tmp) usado como pai dos
     * diretórios temporários da aplicação, para não depender do
     * java.io.tmpdir compartilhado pela máquina.
     */
    private static Path diretorioBaseTemporario() throws IOException {
        Path base = Paths.get(
                System.getProperty("user.home"), ".flmane", "tmp");
        if (FileSystems.getDefault()
                .supportedFileAttributeViews()
                .contains("posix")) {
            FileAttribute<Set<PosixFilePermission>> apenasDono =
                    PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwx------"));
            Files.createDirectories(base, apenasDono);
            Files.setPosixFilePermissions(
                    base, PosixFilePermissions.fromString("rwx------"));
        } else {
            Files.createDirectories(base);
        }
        return base;
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
                if (PainelCircuito.desenhaBkg) {
                    g.drawImage(
                            bg1,
                            0,
                            0,
                            getWidth(),
                            getHeight(),
                            null);
                }
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
                Lang.msg("launcherCopiarLink"),
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
                Lang.msg("launcherAbrirNavegador"),
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
                Lang.msg("launcherJogoSolo"),
                () -> SwingUtilities.invokeLater(() -> {
                    try {
                        new MainFrame(null, true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }));
        painel.add(abrirDesktop);

        painel.add(Box.createVerticalStrut(8));
        JLabel abrirDesktopMulti = criarMenuLabel(
                Lang.msg("launcherJogoMulti"),
                () -> SwingUtilities.invokeLater(() -> {
                    try {
                        new AppletPaddock().init(true);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }));
        painel.add(abrirDesktopMulti);

        backgroundPanel.add(painel);

        frame.setVisible(true);
    }

    private static String localizarJar() {

        // Rodando do próprio jar (duplo-clique/java -jar em qualquer CWD), o
        // CodeSource é o caminho mais confiável; os relativos abaixo cobrem a
        // execução a partir do repositório (IDE/target-classes).
        try {
            File codeSource = new File(
                    MainLauncher.class
                            .getProtectionDomain()
                            .getCodeSource()
                            .getLocation()
                            .toURI());
            if (codeSource.isFile()) {
                return codeSource.getPath();
            }
        } catch (Exception e) {
            // sem CodeSource utilizável; tenta os caminhos relativos
        }

        File appJar = new File("app/flmane.jar");
        if (appJar.exists()) {
            return appJar.getPath();
        }

        File targetJar = new File("target/flmane.jar");
        if (targetJar.exists()) {
            return targetJar.getPath();
        }

        throw new RuntimeException(
                "Nao foi encontrado app/flmane.jar nem target/flmane.jar");
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
                if (!ni.isUp() || ni.isLoopback() || ni.isVirtual()) {
                    continue;
                }
                if (nomeVirtual(ni)) {
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

    private static boolean nomeVirtual(NetworkInterface ni) {
        String name = ni.getName().toLowerCase();
        String display = ni.getDisplayName().toLowerCase();
        return name.contains("vmware") || name.contains("virtualbox")
                || name.contains("hyper-v") || name.contains("virtual")
                || display.contains("vmware") || display.contains("virtualbox")
                || display.contains("hyper-v") || display.contains("virtual");
    }
}