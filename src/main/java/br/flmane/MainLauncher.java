package br.flmane;

import br.flmane.controles.ControleRecursos;
import br.flmane.entidades.Circuito;
import br.flmane.entidades.DesenhoProceduralCircuito;
import br.flmane.entidades.Piloto;
import br.flmane.recursos.CarregadorRecursos;
import br.flmane.recursos.ImagensHeadlessDisco;
import br.flmane.recursos.SpriteSheet;
import br.flmane.recursos.idiomas.Lang;
import br.flmane.servidor.applet.AppletPaddock;
import br.flmane.servidor.netty.FlmaneHttpDispatcher;
import br.flmane.visao.PainelCircuito;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class MainLauncher {


    private static final int PORT = 8080;

    /**
     * Setada só pelo processo filho que {@link #iniciarProcessoServidor}
     * sobe pro modo GUI — nunca pelo usuário/deploy — pra manter o cache de
     * imagens em memória (comportamento normal) naquele filho, em vez do
     * modo disco que {@code --headless} usa por padrão.
     */
    private static final String ENV_CACHE_MEMORIA_LOCAL = "FLMANE_CACHE_MEMORIA_LOCAL";

    public static void main(String[] args) {

        try {
            if (contemArg(args, "--pre-gerar-imagens")) {
                // Modo "assar": roda só a pré-geração de imagens headless e
                // sai, sem bindar porta — usado pelo flmane.dockerfile num
                // RUN durante o docker build, pra embutir as imagens já
                // prontas na imagem final e restarts do container não
                // pagarem esse custo de novo.
                assarImagensHeadless();
                return;
            }
            if (contemArg(args, "--headless")) {
                // --headless sozinho já significa modo disco: pré-gera as
                // imagens em disco e não retém BufferedImage nenhum em
                // memória. Único caso em que isso não vale é o processo
                // filho que o próprio modo GUI sobe logo abaixo — sinalizado
                // pra si mesmo via variável de ambiente (não por outra flag
                // de linha de comando, que o usuário/deploy nunca precisa
                // conhecer) porque aquele filho só serve o teste local via
                // navegador e não deve pagar os minutos de pré-geração.
                boolean modoImagensDisco =
                        !"true".equals(System.getenv(ENV_CACHE_MEMORIA_LOCAL));
                iniciarServidorHeadless(modoImagensDisco);
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

    private static boolean contemArg(String[] args, String procurado) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (procurado.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Folga generosa para o body agregado de uma requisição: cobre tanto o
     * protocolo de serialização Java de ServletPaddock (estado de jogo
     * completo, potencialmente grande) quanto os corpos JSON de LetsRace.
     */
    private static final int TAMANHO_MAXIMO_REQUISICAO = 100 * 1024 * 1024;

    /**
     * @param modoImagensDisco quando {@code true} (padrão de {@code --headless}),
     *                         ativa a pré-geração de imagens em disco e desliga
     *                         os caches estáticos de {@code BufferedImage} em
     *                         memória. Quando {@code false} (só o processo
     *                         filho que o modo GUI sobe pra teste local via
     *                         navegador, ver {@link #ENV_CACHE_MEMORIA_LOCAL}),
     *                         mantém o cache em memória normal e não paga o
     *                         custo de boot da pré-geração.
     */
    private static void iniciarServidorHeadless(boolean modoImagensDisco) throws Exception {
        File base = extrairWebapp();
        System.out.println(
                "WEBAPP: " +
                        base.getAbsolutePath());
        if (!base.exists()) {
            throw new RuntimeException(
                    "Diretorio webapp nao encontrado: "
                            + base.getAbsolutePath());
        }
        if (modoImagensDisco) {
            CarregadorRecursos.ativarModoHeadlessDisco();
            SpriteSheet.ativarModoHeadlessDisco();
            garantirImagensHeadless();
        }

        FlmaneHttpDispatcher dispatcher = new FlmaneHttpDispatcher(base.toPath());
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new HttpServerCodec());
                            ch.pipeline().addLast(
                                    new HttpObjectAggregator(TAMANHO_MAXIMO_REQUISICAO));
                            ch.pipeline().addLast(dispatcher);
                        }
                    });
            Channel channel = bootstrap.bind(PORT).sync().channel();
            String ip = descobrirIP();
            String url = "http://" + ip + ":" + PORT
                    + "/flmane/html5/index.html";
            System.out.println("=================================");
            System.out.println("SERVER STARTED");
            System.out.println(url);
            System.out.println("=================================");
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    /**
     * Roda a pré-geração de imagens headless e sai, sem bindar porta —
     * modo "assar" invocado via {@code --pre-gerar-imagens}, pensado pra
     * rodar dentro de um {@code RUN} do {@code flmane.dockerfile}.
     */
    private static void assarImagensHeadless() throws Exception {
        CarregadorRecursos.ativarModoHeadlessDisco();
        SpriteSheet.ativarModoHeadlessDisco();
        garantirImagensHeadless();
    }

    /**
     * Inicia {@link ImagensHeadlessDisco} e, se o diretório resultante já
     * tiver uma pré-geração completa de uma execução anterior (imagens
     * assadas no {@code docker build} ou de um boot headless anterior sobre
     * o mesmo diretório fixo), pula a pré-geração inteira; caso contrário,
     * gera tudo e grava o marcador de conclusão.
     */
    private static void garantirImagensHeadless() throws Exception {
        garantirImagensHeadless(System.getenv("FLMANE_IMAGENS_HEADLESS_DIR"));
    }

    /**
     * Pacote-visível pra teste: mesma lógica de {@link #garantirImagensHeadless()},
     * recebendo o diretório fixo diretamente em vez de lê-lo de
     * {@code System.getenv} (não mutável a partir do teste).
     */
    static void garantirImagensHeadless(String diretorioFixo) throws Exception {
        ImagensHeadlessDisco.iniciar(diretorioFixo);
        if (ImagensHeadlessDisco.preGeracaoConcluida()) {
            System.out.println(
                    "IMAGENS HEADLESS JA PRE-GERADAS, REAPROVEITANDO: "
                            + ImagensHeadlessDisco.diretorioBase());
            CarregadorRecursos.liberarCachesPreGeracao();
            return;
        }
        preGerarImagensHeadless();
        ImagensHeadlessDisco.marcarPreGeracaoConcluida();
        // O grid completo de todas as temporadas e os circuitos desserializados
        // só serviram pra gerar as imagens: soltar antes do bind da porta pra
        // que o regime do servidor não carregue o pico da pré-geração.
        CarregadorRecursos.liberarCachesPreGeracao();
    }

    /**
     * Gera em disco, sequencialmente, a imagem de fundo/miniatura de cada
     * circuito ativo e as imagens de carro-lado/carro-cima (com e sem
     * aerofólio)/capacete de cada carro/piloto de cada temporada
     * configurada — cada imagem é escrita em
     * {@link ImagensHeadlessDisco} e a referência em memória é descartada
     * antes de passar para a próxima, para que o pico de memória fique
     * limitado a poucas imagens por vez, nunca ao total. Falha ao gerar um
     * asset específico é logada e pulada, sem abortar a subida do servidor.
     */
    private static void preGerarImagensHeadless() {
        long inicio = System.currentTimeMillis();
        System.out.println(
                "PRE-GERANDO IMAGENS HEADLESS: " + ImagensHeadlessDisco.diretorioBase());
        ProgressoTerminal progresso = new ProgressoTerminal(contarAssetsHeadless());
        preGerarCircuitos(progresso);
        preGerarCarrosPilotos(progresso);
        progresso.concluir();
        System.out.println("PRE-GERACAO DE IMAGENS CONCLUIDA EM "
                + (System.currentTimeMillis() - inicio) + "ms");
    }

    /**
     * Quantidade de itens que {@link #preGerarCircuitos}/
     * {@link #preGerarCarrosPilotos} vão percorrer — usada só para
     * dimensionar a barra de progresso do console (não precisa bater com o
     * número exato de arquivos gerados, já que cada circuito/piloto gera
     * mais de um arquivo).
     */
    private static int contarAssetsHeadless() {
        int total = ControleRecursos.carregarCircuitos().size();
        CarregadorRecursos carregadorRecursos = CarregadorRecursos.getCarregadorRecursos(false);
        for (String temporadaBare : carregadorRecursos.getVectorTemps()) {
            List<Piloto> pilotos = carregadorRecursos
                    .carregarTemporadasPilotos().get("t" + temporadaBare);
            if (pilotos != null) {
                total += pilotos.size();
            }
        }
        return total;
    }

    private static void preGerarCircuitos(ProgressoTerminal progresso) {
        Map<String, String> circuitos = ControleRecursos.carregarCircuitos();
        for (String arquivoXml : circuitos.values()) {
            try {
                Circuito circuito = CarregadorRecursos.carregarCircuito(arquivoXml);
                BufferedImage fundo = DesenhoProceduralCircuito.geraImagem(circuito);
                ImagensHeadlessDisco.gravar(
                        ImagensHeadlessDisco.arquivoCircuitoFundo(circuito.getBackGround()),
                        fundo, "jpg");
                BufferedImage mini = circuito.desenhaMiniCircuito();
                ImagensHeadlessDisco.gravar(
                        ImagensHeadlessDisco.arquivoCircuitoMini(arquivoXml), mini, "png");
                circuito.liberarObjetosDesenho();
            } catch (Exception e) {
                Logger.logar("Falha ao pre-gerar imagem do circuito "
                        + arquivoXml + ": " + e.getMessage());
                Logger.logarExept(e);
            } finally {
                progresso.avancar("circuito " + arquivoXml);
            }
        }
    }

    private static void preGerarCarrosPilotos(ProgressoTerminal progresso) {
        CarregadorRecursos carregadorRecursos =
                CarregadorRecursos.getCarregadorRecursos(false);
        for (String temporadaBare : carregadorRecursos.getVectorTemps()) {
            String temporadaKey = "t" + temporadaBare;
            try {
                List<Piloto> pilotos = carregadorRecursos
                        .carregarTemporadasPilotos().get(temporadaKey);
                if (pilotos == null) {
                    continue;
                }
                Set<Integer> carrosGerados = new HashSet<>();
                for (Piloto piloto : pilotos) {
                    preGerarCarroEPiloto(
                            carregadorRecursos, piloto, temporadaBare, temporadaKey,
                            carrosGerados, progresso);
                }
            } catch (Exception e) {
                Logger.logar("Falha ao pre-gerar temporada "
                        + temporadaBare + ": " + e.getMessage());
                Logger.logarExept(e);
            }
        }
    }

    private static void preGerarCarroEPiloto(
            CarregadorRecursos carregadorRecursos,
            Piloto piloto,
            String temporadaBare,
            String temporadaKey,
            Set<Integer> carrosGerados,
            ProgressoTerminal progresso) {
        try {
            if (piloto.getCarro() != null
                    && carrosGerados.add(piloto.getCarro().getId())) {
                int idCarro = piloto.getCarro().getId();
                ImagensHeadlessDisco.gravar(
                        ImagensHeadlessDisco.arquivoCarroLado(temporadaBare, idCarro),
                        carregadorRecursos.obterCarroLado(piloto, temporadaKey), "png");
                ImagensHeadlessDisco.gravar(
                        ImagensHeadlessDisco.arquivoCarroCima(temporadaBare, idCarro, false),
                        carregadorRecursos.obterCarroCima(piloto, temporadaKey), "png");
                ImagensHeadlessDisco.gravar(
                        ImagensHeadlessDisco.arquivoCarroCima(temporadaBare, idCarro, true),
                        carregadorRecursos.obterCarroCimaSemAreofolio(piloto, temporadaKey), "png");
            }
            ImagensHeadlessDisco.gravar(
                    ImagensHeadlessDisco.arquivoCapacete(temporadaBare, piloto.getId()),
                    carregadorRecursos.obterCapacete(piloto, temporadaKey), "png");
        } catch (Exception e) {
            Logger.logar("Falha ao pre-gerar imagem de carro/piloto da temporada "
                    + temporadaBare + ": " + e.getMessage());
            Logger.logarExept(e);
        } finally {
            progresso.avancar(temporadaKey + " " + piloto.getNome());
        }
    }

    /**
     * Barra de progresso estilo terminal (sobrescrita na mesma linha via
     * {@code \r}) para a pré-geração de imagens headless — só serve pra dar
     * visibilidade do que está acontecendo durante o boot (que pode levar
     * minutos com muitas temporadas/circuitos configurados), sem gerar uma
     * linha de log por asset.
     */
    private static final class ProgressoTerminal {
        private static final int LARGURA_BARRA = 30;

        private final int total;
        private int atual;

        private ProgressoTerminal(int total) {
            this.total = total;
        }

        private void avancar(String rotulo) {
            atual++;
            int preenchido = total <= 0 ? LARGURA_BARRA
                    : (int) Math.min(LARGURA_BARRA, (atual * (long) LARGURA_BARRA) / total);
            int percentual = total <= 0 ? 100 : Math.min(100, (atual * 100) / total);
            StringBuilder linha = new StringBuilder();
            linha.append('\r').append('[');
            for (int i = 0; i < LARGURA_BARRA; i++) {
                linha.append(i < preenchido ? '=' : ' ');
            }
            linha.append("] ").append(percentual).append("% (")
                    .append(atual).append('/').append(total).append(") ")
                    .append(rotulo);
            // Preenche com espaços até uma largura fixa pra apagar o resto
            // de uma linha anterior mais comprida (rótulo variável).
            while (linha.length() < 110) {
                linha.append(' ');
            }
            System.out.print(linha);
            System.out.flush();
        }

        private void concluir() {
            System.out.println();
        }
    }

    private static Process iniciarProcessoServidor(String jar)
            throws Exception {
        ProcessBuilder pb =
                new ProcessBuilder(
                        "java",
                        "-Xms64m",
                        "-Xmx512m",
                        "-Djava.awt.headless=true",
                        "-cp",
                        jar,
                        "br.flmane.MainLauncher",
                        "--headless"
                );
        pb.environment().put(ENV_CACHE_MEMORIA_LOCAL, "true");
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