package br.f1mane.recursos;

import br.f1mane.controles.ControleRecursos;
import br.f1mane.entidades.*;
import br.nnpe.Global;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.List;
import java.util.*;

public class CarregadorRecursos {
    private static HashMap<String, String> temporadas;
    private static HashMap<String, TemporadasDefault> temporadasDefauts;
    private static Vector<String> vectorTemps;
    private Map<String, List<Piloto>> temporadasPilotos;
    private Map<String, TemporadasDefault> temporadasPilotosDefauts;
    private List<CircuitosDefault> circuitosDefauts;
    private static final Map bufferImages = new HashMap();
    private static final Map bufferCarros = new HashMap();
    private static final Map<String, Circuito> bufferCircuitos = new HashMap<String, Circuito>();
    private static final Map<String, BufferedImage> bufferCarrosCima = new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> bufferCarrosCimaSemAreofolio = new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> bufferCarrosLado = new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> bufferCarrosLadoSemAreofolio = new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> bufferCapacete = new HashMap<String, BufferedImage>();
    private static final Map<String, List<String>> cacheTimes = new HashMap<>();
    private static final Map<String, List<String>> cachePilotos = new HashMap<>();
    private static final AlphaComposite composite = AlphaComposite
            .getInstance(AlphaComposite.DST_OUT, 1);

    private static CarregadorRecursos carregadorRecursos;

    private static boolean cache;

    private static String versao;

    private static String versaoMesAno;

    private static URL codeBase;

    private static String applet;

    public static boolean carregaApenasSprites = false;

    final static DecimalFormat decimalFormat = new DecimalFormat("#,###");

    private CarregadorRecursos() {
    }

    public static void initProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(CarregadorRecursos.recursoComoStream("application.properties"));
        versao = properties.getProperty("versao");
        versaoMesAno = properties.getProperty("versaoMesAno");
        applet = properties.getProperty("applet");
        String codeBaseStr = properties.getProperty("codeBase");
        if (!Util.isNullOrEmpty(codeBaseStr)) {
            codeBase = new URL(codeBaseStr);
        }
        if (versao.contains(".")) {
            versao = versao.replaceAll("\\.", "");
        }

    }

    public static String getApplet() {
        if (applet == null) {
            try {
                initProperties();
            } catch (IOException e) {
                Logger.logarExept(e);
            }
        }
        return applet;
    }

    public static String getVersao() {
        if (versao == null) {
            try {
                initProperties();
            } catch (IOException e) {
                Logger.logarExept(e);
            }
        }
        return versao;
    }

    public static String getVersaoMesAno() {
        if (versaoMesAno == null) {
            try {
                initProperties();
            } catch (IOException e) {
                Logger.logarExept(e);
            }
        }
        return versaoMesAno;
    }

    public static String getVersaoFormatado() {
        return decimalFormat.format(Integer.parseInt(getVersao())) + " " + getVersaoMesAno();
    }

    public static synchronized CarregadorRecursos getCarregadorRecursos(
            boolean cache) {
        if (carregadorRecursos == null) {
            carregadorRecursos = new CarregadorRecursos();
            carregarTemporadas();
        }
        if (!CarregadorRecursos.cache) {
            CarregadorRecursos.cache = cache;
        }
        return carregadorRecursos;
    }

    public Vector<String> getVectorTemps() {
        if (vectorTemps == null) {
            vectorTemps = carregarTemporadas();
        }
        return vectorTemps;
    }

    public synchronized static Vector<String> carregarTemporadas() {
        if (temporadas != null) {
            return vectorTemps;
        }
        temporadas = new HashMap<String, String>();
        temporadasDefauts = new HashMap<String, TemporadasDefault>();
        vectorTemps = new Vector<String>();
        final Properties properties = new Properties();
        try {
            properties.load(
                    recursoComoStream("properties/temporadas.properties"));
            Enumeration propName = properties.propertyNames();
            while (propName.hasMoreElements()) {
                final String name = (String) propName.nextElement();
                String[] split = properties.getProperty(name).split(",");
                TemporadasDefault defauts = new TemporadasDefault();
                defauts.setTrocaPneu(Boolean.valueOf("true".equals(split[1])));
                defauts.setReabastecimento(Boolean.valueOf("true".equals(split[2])));
                defauts.setErs(Boolean.valueOf("true".equals(split[3])));
                defauts.setDrs(Boolean.valueOf("true".equals(split[4])));
                defauts.setFatorBox(Double.valueOf(Double.parseDouble(split[5])));
                defauts.setSafetyCar(Boolean.valueOf("true".equals(split[6])));
                temporadasDefauts.put(name, defauts);
                temporadas.put(split[0], name);
                vectorTemps.add(split[0]);
            }
            Collections.sort(vectorTemps, new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return o1.compareTo(o2);
                }
            });
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        return vectorTemps;
    }

    public HashMap getTemporadas() {
        return temporadas;
    }

    public static URL carregarImagemResource(String imagem) {
        return CarregadorRecursos.recursoURL(imagem);
    }

    public static BufferedImage carregaBufferedImageTranspareciaBranca(
            String file) {

        BufferedImage bufferedImage = (BufferedImage) bufferImages.get(file);

        if (bufferedImage == null) {

            BufferedImage buffer = null;
            try {
                buffer = ImageUtil.toBufferedImage(file);
                if (buffer == null) {
                    Logger.logar("carregaBufferedImageTranspareciaBranca buffer nulo");
                }

            } catch (Exception e) {
                Logger.logar(
                        " carregaBufferedImageTranspareciaBranca Erro gerando transparencia para :"
                                + file);
                Logger.logarExept(e);
            }

            bufferedImage = ImageUtil
                    .toCompatibleImage(ImageUtil.geraTransparencia(buffer));
            if (cache) {
                bufferImages.put(file, bufferedImage);
            }
        }
        return bufferedImage;
    }

    public static BufferedImage carregaBufferedImage(String file) {
        BufferedImage bufferedImage = (BufferedImage) bufferImages.get(file);

        if (bufferedImage == null) {
            try {
                bufferedImage = ImageUtil
                        .toCompatibleImage(ImageUtil.toBufferedImage(file));
            } catch (Exception e) {
                Logger.logarExept(e);
            }
            if (bufferedImage == null) {
                Logger.logar("carregaBufferedImage null : " + file);
            }
            if (cache) {
                bufferImages.put(file, bufferedImage);
            }
        }
        return bufferedImage;
    }

    public static BufferedImage carregaBackGround(String backGroundStr,
                                                  JPanel panel, Circuito circuito) {

        BufferedImage backGround = null;
        try {
            backGround = ImageIO
                    .read(CarregadorRecursos.recursoURL("circuitos/" + backGroundStr));
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        if (panel != null && backGround != null)
            panel.setSize(backGround.getWidth(), backGround.getHeight());

        if (backGround == null) {
            Logger.logar("carregaBackGround backGround nulo");
            return null;
        }
        return backGround;
    }

    /**
     * Usado pelo carregamento de imagem de fundo em corrida (não pelo
     * editor de circuitos, que precisa sempre da imagem real de arquivo
     * para servir de referência visual). Quando
     * {@link Global#GERAR_IMAGEM_CIRCUITO_EM_MEMORIA} está ativa, gera a
     * imagem em memória via {@link DesenhoProceduralCircuito} em vez de ler
     * {@code circuitos/<backGroundStr>} do disco.
     */
    public static BufferedImage carregaBackGroundJogo(String backGroundStr,
                                                       JPanel panel, Circuito circuito) {
        if (Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA && circuito != null) {
            BufferedImage backGround = DesenhoProceduralCircuito.geraImagem(circuito);
            if (panel != null) {
                panel.setSize(backGround.getWidth(), backGround.getHeight());
            }
            return backGround;
        }
        return carregaBackGround(backGroundStr, panel, circuito);
    }

    public static InputStream recursoComoStream(String recurso) {
        if (carregadorRecursos == null) {
            carregadorRecursos = new CarregadorRecursos();
        }
        return carregadorRecursos.getClass().getResourceAsStream("/" + recurso);
    }

    public static URL recursoURL(String recurso) {
        if (carregadorRecursos == null) {
            carregadorRecursos = new CarregadorRecursos();
        }
        return carregadorRecursos.getClass().getResource("/" + recurso);
    }

    private static void gerarCarrosCima() throws IOException {
        File fileT = new File("src/main/resources/properties");
        File[] dirT = fileT.listFiles();
        for (int i = 0; i < dirT.length; i++) {
            String temporarada = dirT[i].getName();
            if (!temporarada.contains(".")) {

                Properties properties = new Properties();

                properties.load(CarregadorRecursos.recursoComoStream(
                        "properties/" + temporarada + "/carros.properties"));

                Enumeration propNames = properties.propertyNames();

                while (propNames.hasMoreElements()) {
                    Carro carro = new Carro();
                    String name = (String) propNames.nextElement();
                    String prop = properties.getProperty(name);
                    carro.setNome(name);
                    String[] values = prop.split(",");
                    carro.setPotencia(Integer.parseInt(values[0]));

                    String red = values[1];
                    String green = values[2];
                    String blue = values[3];
                    carro.setImg("carros/" + temporarada + "/" + values[4]);
                    carro.setCor1(new Color(Integer.parseInt(red),
                            Integer.parseInt(green), Integer.parseInt(blue)));

                    red = values[5];
                    green = values[6];
                    blue = values[7];
                    carro.setCor2(new Color(Integer.parseInt(red),
                            Integer.parseInt(green), Integer.parseInt(blue)));
                    BufferedImage carroCima = CarregadorRecursos
                            .carregaImg("png/CarroCima.png");

                    BufferedImage cor1 = gerarCoresCarros(carro.getCor1(),
                            "CarroCimaC1.png");
                    BufferedImage cor2 = gerarCoresCarros(carro.getCor2(),
                            "CarroCimaC2.png");
                    Graphics graphics = carroCima.getGraphics();
                    Util.setarHints((Graphics2D) graphics);
                    graphics.drawImage(cor2, 0, 0, null);
                    graphics.drawImage(cor1, 0, 0, null);
                    graphics.dispose();
                    File gravar = new File("src" + File.separator + "sowbreira"
                            + File.separator + "f1mane" + File.separator
                            + "recursos" + File.separator + "carros"
                            + File.separator + temporarada + File.separator
                            + carro.getNome() + ".png");
                    ImageIO.write(carroCima, "png", gravar);
                    Logger.logar("src" + File.separator + "sowbreira"
                            + File.separator + "f1mane" + File.separator
                            + "recursos" + File.separator + "carros"
                            + File.separator + temporarada + File.separator
                            + carro.getNome() + ".png");
                }

            }
        }

    }

    public static BufferedImage gerarCoresCarros(Color corPintar,
                                                 String carro) {
        return gerarCoresCarros(corPintar, carro, BufferedImage.TYPE_INT_ARGB);
    }

    public static BufferedImage gerarCoresCarros(Color corPintar, String carro,
                                                 int argb) {
        BufferedImage srcBufferedImage = carregaBufferedImageTransparecia(
                carro);
        BufferedImage bufferedImageRetorno = new BufferedImage(
                srcBufferedImage.getWidth(), srcBufferedImage.getHeight(),
                argb);
        Raster srcRaster = srcBufferedImage.getData();
        WritableRaster destRaster = bufferedImageRetorno.getRaster();
        int[] argbArray;
        for (int i = 0; i < srcBufferedImage.getWidth(); i++) {
            for (int j = 0; j < srcBufferedImage.getHeight(); j++) {
                argbArray = new int[4];
                argbArray = srcRaster.getPixel(i, j, argbArray);
                argbArray[0] = (int) ((argbArray[0] + corPintar.getRed()) / 2);
                argbArray[1] = (int) ((argbArray[1] + corPintar.getGreen())
                        / 2);
                argbArray[2] = (int) ((argbArray[2] + corPintar.getBlue()) / 2);
                destRaster.setPixel(i, j, argbArray);
            }
        }

        return bufferedImageRetorno;
    }

    private static final Map<String, BufferedImage> cacheModeloV2 = new HashMap<>();

    public static void invalidarCacheModeloV2() {
        cacheModeloV2.clear();
    }

    public static BufferedImage pintarModeloV2(String assetPath, Color cor1, Color cor2, int targetW, int targetH) {
        return pintarModeloV2(assetPath, cor1, cor2, targetW, targetH, null);
    }

    public static BufferedImage pintarModeloV2(String assetPath, Color cor1, Color cor2, int targetW, int targetH, String knockoutMaskPath) {
        String chave = assetPath + cor1.getRGB() + "_" + cor2.getRGB() + "_" + targetW + "_" + targetH
                + (knockoutMaskPath != null ? "_" + knockoutMaskPath : "");
        BufferedImage cached = cacheModeloV2.get(chave);
        if (cached != null) return cached;

        BufferedImage src = carregaBufferedImageTransparecia(assetPath);
        int srcW = src.getWidth();
        int srcH = src.getHeight();

        // Substituição de matiz HSB: preserva o brilho (shading artístico) pixel a pixel
        // e substitui apenas matiz+saturação pela de cor1 (verde) ou cor2 (branco).
        float[] hsbCor1 = Color.RGBtoHSB(cor1.getRed(), cor1.getGreen(), cor1.getBlue(), null);
        float[] hsbCor2 = Color.RGBtoHSB(cor2.getRed(), cor2.getGreen(), cor2.getBlue(), null);
        float[] hsbPx = new float[3];
        BufferedImage painted = new BufferedImage(srcW, srcH, BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < srcW; x++) {
            for (int y = 0; y < srcH; y++) {
                int argb = src.getRGB(x, y);
                int a = (argb >> 24) & 0xFF;
                if (a == 0) { painted.setRGB(x, y, 0); continue; }
                int r = (argb >> 16) & 0xFF;
                int g = (argb >> 8) & 0xFF;
                int b = argb & 0xFF;
                Color.RGBtoHSB(r, g, b, hsbPx);
                if (hsbPx[0] >= 0.22f && hsbPx[0] <= 0.45f && hsbPx[1] > 0.20f) {
                    // brilho do alvo × brilho do pixel: preto permanece preto, cores claras mantêm shading
                    int rgb = Color.HSBtoRGB(hsbCor1[0], hsbCor1[1], hsbCor1[2] * hsbPx[2]);
                    painted.setRGB(x, y, (a << 24) | (rgb & 0x00FFFFFF));
                } else if (hsbPx[1] < 0.20f && hsbPx[2] > 0.70f) {
                    int rgb = Color.HSBtoRGB(hsbCor2[0], hsbCor2[1], hsbCor2[2] * hsbPx[2]);
                    painted.setRGB(x, y, (a << 24) | (rgb & 0x00FFFFFF));
                } else {
                    painted.setRGB(x, y, argb);
                }
            }
        }

        // Aplicar máscara de knockout (DST_OUT) na resolução nativa, antes de escalar
        if (knockoutMaskPath != null) {
            BufferedImage mask = carregaBufferedImageTransparecia(knockoutMaskPath);
            if (mask.getWidth() != srcW || mask.getHeight() != srcH) {
                BufferedImage maskScaled = new BufferedImage(srcW, srcH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gm = maskScaled.createGraphics();
                gm.drawImage(mask, 0, 0, srcW, srcH, null);
                gm.dispose();
                mask = maskScaled;
            }
            Graphics2D gk = painted.createGraphics();
            gk.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_OUT));
            gk.drawImage(mask, 0, 0, null);
            gk.dispose();
        }

        // Redimensionamento progressivo por halvings para evitar serrilhado,
        // seguido de passo final com BICUBIC centralizado no canvas alvo.
        BufferedImage result;
        if (srcW == targetW && srcH == targetH) {
            result = painted;
        } else {
            double escala = Math.min((double) targetW / srcW, (double) targetH / srcH);
            int scaledW = (int) (srcW * escala);
            int scaledH = (int) (srcH * escala);

            BufferedImage current = painted;
            int curW = srcW, curH = srcH;
            while (curW > scaledW * 2 || curH > scaledH * 2) {
                int nextW = Math.max(curW / 2, scaledW);
                int nextH = Math.max(curH / 2, scaledH);
                BufferedImage half = new BufferedImage(nextW, nextH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D gh = half.createGraphics();
                gh.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                gh.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                gh.drawImage(current, 0, 0, nextW, nextH, null);
                gh.dispose();
                current = half;
                curW = nextW;
                curH = nextH;
            }

            result = new BufferedImage(targetW, targetH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = result.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int offsetX = (targetW - scaledW) / 2;
            int offsetY = (targetH - scaledH) / 2;
            g2.drawImage(current, offsetX, offsetY, scaledW, scaledH, null);
            g2.dispose();
        }

        cacheModeloV2.put(chave, result);
        return result;
    }

    public static BufferedImage carregaImgCarro(String img) {
        BufferedImage bufferedImage = (BufferedImage) bufferCarros.get(img);
        if (bufferedImage != null) {
            return bufferedImage;
        }
        bufferedImage = ImageUtil
                .toCompatibleImage(carregaBufferedImageTranspareciaBranca(img));
        if (cache) {
            bufferCarros.put(img, bufferedImage);
        }
        return bufferedImage;
    }

    public List<Piloto> carregarListaPilotos(String temporarada)
            throws IOException {
        List<Piloto> retorno = new ArrayList<Piloto>();
        Properties properties = new Properties();

        properties.load(recursoComoStream(
                "properties/" + temporarada + "/pilotos.properties"));

        Enumeration propNames = properties.propertyNames();
        int cont = 1;
        while (propNames.hasMoreElements()) {
            Piloto piloto = new Piloto();
            piloto.setId(cont++);
            String name = (String) propNames.nextElement();
            String prop = properties.getProperty(name);
            piloto.setNomeOriginal(name);
            piloto.setNome(Util.substVogais(name));
            piloto.setNomeCarro(Util.substVogais(prop.split(",")[0]));
            int duasCasas = Integer.parseInt(prop.split(",")[1]);
            piloto.setHabilidadeReal(
                    Integer.parseInt(String.valueOf(duasCasas) + "0"));
            piloto.setHabilidade(piloto.getHabilidadeReal());
            retorno.add(piloto);
        }
        Collections.sort(retorno, new PilotoComparator());

        return retorno;
    }

    public List<Carro> carregarListaCarros(List pilotos, String temporada)
            throws IOException {
        if (temporadas != null && temporadas.get(temporada) != null) {
            Set<Carro> carros = new HashSet<Carro>();
            for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
                Piloto piloto = (Piloto) iterator.next();
                carros.add(piloto.getCarro());
            }
            List<Carro> carrosL = new ArrayList<Carro>(carros);
            Collections.sort(carrosL, new CarroComparator());
            return carrosL;
        }

        return carregarListaCarrosArquivo(temporada);
    }

    public List<Carro> carregarListaCarros(String temporada)
            throws IOException {
        return carregarListaCarrosArquivo(temporada);
    }

    public List carregarListaCarrosArquivo(String temporada)
            throws IOException {
        List retorno = new ArrayList();
        Properties properties = new Properties();
        properties.load(recursoComoStream(
                "properties/" + temporada + "/carros.properties"));
        Enumeration propNames = properties.propertyNames();
        int id = 1;
        while (propNames.hasMoreElements()) {
            Carro carro = new Carro();
            carro.setId(id++);
            String name = (String) propNames.nextElement();
            String prop = properties.getProperty(name);
            carro.setNome(Util.substVogais(name));
            String[] values = prop.split(",");
            carro.setPotencia(Integer.parseInt(values[0]));
            carro.setPotenciaReal(Integer.parseInt(values[0]));
            if (values.length > 8) {
                carro.setAerodinamica(Integer.parseInt(values[8]));
            } else {
                carro.setAerodinamica(Integer.parseInt(values[0]));
            }
            if (values.length > 9) {
                carro.setFreios(Integer.parseInt(values[9]));
            } else {
                carro.setFreios(Integer.parseInt(values[0]));
            }
            String red = values[1];
            String green = values[2];
            String blue = values[3];

            String[] tnsCarros = values[4].split(";");

            carro.setImg("carros/" + temporada + "/"
                    + tnsCarros[Util.intervalo(0, tnsCarros.length - 1)]);
            carro.setCor1(new Color(Integer.parseInt(red),
                    Integer.parseInt(green), Integer.parseInt(blue)));
            red = values[5];
            green = values[6];
            blue = values[7];
            carro.setCor2(new Color(Integer.parseInt(red),
                    Integer.parseInt(green), Integer.parseInt(blue)));
            retorno.add(carro);
        }
        return retorno;
    }

    public void ligarPilotosCarros(List pilotos, List carros) {
        for (Iterator iter = pilotos.iterator(); iter.hasNext(); ) {
            Piloto piloto = (Piloto) iter.next();
            for (Iterator iterator = carros.iterator(); iterator.hasNext(); ) {
                Carro carro = (Carro) iterator.next();
                if (piloto.getNomeCarro().equals(carro.getNome())) {
                    piloto.setCarro(criarCopiaCarro(carro, piloto));
                }
            }
        }
    }

    public static Carro criarCopiaCarro(Carro carro, Piloto piloto) {
        Carro carroNovo = new Carro();
        carroNovo.setId(carro.getId());
        carroNovo.setNome(carro.getNome());
        carroNovo.setCor1(carro.getCor1());
        carroNovo.setCor2(carro.getCor2());
        carroNovo.setImg(carro.getImg());
        carroNovo.setPiloto(piloto);
        carroNovo.setPotenciaReal(carro.getPotenciaReal());
        carroNovo.setAerodinamica(carro.getAerodinamica());
        carroNovo.setFreios(carro.getFreios());
        carroNovo.setPotencia(carro.getPotencia());
        return carroNovo;
    }

    public synchronized Map<String, List<Piloto>> carregarTemporadasPilotos() {
        if (temporadasPilotos != null) {
            return temporadasPilotos;
        }
        temporadasPilotos = new HashMap<String, List<Piloto>>();
        final Properties properties = new Properties();
        try {
            properties.load(
                    recursoComoStream("properties/temporadas.properties"));
            Enumeration propName = properties.propertyNames();
            while (propName.hasMoreElements()) {
                final String temporada = (String) propName.nextElement();
                List<Piloto> pilotos = carregarListaPilotos(temporada);
                List<Carro> carros = carregarListaCarros(pilotos, temporada);
                ligarPilotosCarros(pilotos, carros);
                temporadasPilotos.put(temporada, pilotos);
            }
        } catch (IOException e) {
            Logger.logarExept(e);
        }
        return temporadasPilotos;
    }

    public synchronized Map<String, TemporadasDefault> carregarTemporadasPilotosDefauts() {
        if (temporadasPilotosDefauts != null) {
            return temporadasPilotosDefauts;
        }
        temporadasPilotosDefauts = new HashMap<String, TemporadasDefault>();
        Map<String, List<Piloto>> carregarTemporadasPilotos = carregarTemporadasPilotos();
        for (Iterator iterator = carregarTemporadasPilotos.keySet()
                .iterator(); iterator.hasNext(); ) {
            String temporada = (String) iterator.next();
            TemporadasDefault def = temporadasDefauts.get(temporada);
            def.setPilotos(carregarTemporadasPilotos.get(temporada));
            temporadasPilotosDefauts.put(temporada, def);
        }
        return temporadasPilotosDefauts;
    }

    private static String extrairTime(String imgPath) {
        if (imgPath == null) return null;
        String nome = imgPath.substring(imgPath.lastIndexOf('/') + 1);
        if (nome.endsWith(".png")) {
            nome = nome.substring(0, nome.length() - 4);
        }
        return nome;
    }

    private static List<String> getTimesOrdenados(String temporada) {
        List<String> times = cacheTimes.get(temporada);
        if (times != null) return times;
        times = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(recursoComoStream(
                            "properties/" + temporada + "/carros.properties")));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIdx = line.indexOf('=');
                if (eqIdx < 0) continue;
                String prop = line.substring(eqIdx + 1);
                String[] values = prop.split(",");
                if (values.length < 5) continue;
                String[] tnsCarros = values[4].split(";");
                String time = tnsCarros[0].replaceAll("\\.png", "");
                times.add(time);
            }
            reader.close();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        cacheTimes.put(temporada, times);
        return times;
    }

    private static List<String> getPilotosOrdenados(String temporada) {
        List<String> pilotos = cachePilotos.get(temporada);
        if (pilotos != null) return pilotos;
        pilotos = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(recursoComoStream(
                            "properties/" + temporada + "/pilotos.properties")));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eqIdx = line.indexOf('=');
                if (eqIdx < 0) continue;
                String name = line.substring(0, eqIdx);
                pilotos.add(name.replaceAll("\\.", ""));
            }
            reader.close();
        } catch (Exception e) {
            Logger.logarExept(e);
        }
        cachePilotos.put(temporada, pilotos);
        return pilotos;
    }

    private static int indiceTime(String temporada, String teamName) {
        List<String> times = getTimesOrdenados(temporada);
        return times.indexOf(teamName);
    }

    private static int indicePiloto(String temporada, String driverKey) {
        List<String> pilotos = getPilotosOrdenados(temporada);
        return pilotos.indexOf(driverKey);
    }

    public static BufferedImage carregaImgSemCache(String img) {
        return ImageUtil.toBufferedImage(img);
    }

    public static BufferedImage carregaImg(String img) {
        BufferedImage bufferedImage = (BufferedImage) bufferImages.get(img);
        if (bufferedImage != null) {
            return bufferedImage;
        }
        bufferedImage = ImageUtil.toBufferedImage(img);
        if (cache) {
            bufferImages.put(img, bufferedImage);
        }
        return bufferedImage;
    }

    public static BufferedImage carregaBufferedImageMeiaTransparenciaBraca(
            String file) {
        BufferedImage buffer = null;
        try {
            buffer = ImageUtil.toBufferedImage(file);
            if (buffer == null) {
                Logger.logar("carregaBufferedImageMeiaTransparenciaBraca buffer nulo");
                return null;
            }

        } catch (Exception e) {
            Logger.logar("Erro gerando transparencia para :" + file);
            Logger.logarExept(e);
        }

        BufferedImage bufferedImageRetorno = new BufferedImage(
                buffer.getWidth(), buffer.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Raster srcRaster = buffer.getData();
        WritableRaster destRaster = bufferedImageRetorno.getRaster();
        int[] argbArray;

        for (int i = 0; i < buffer.getWidth(); i++) {
            for (int j = 0; j < buffer.getHeight(); j++) {
                argbArray = new int[4];
                argbArray = srcRaster.getPixel(i, j, argbArray);

                Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
                        argbArray[3]);
                if (c.getRed() > 250 && c.getGreen() > 250
                        && c.getBlue() > 250) {
                    argbArray[3] = 0;
                } else {
                    argbArray[3] = 100;
                }
                destRaster.setPixel(i, j, argbArray);
            }
        }

        return bufferedImageRetorno;
    }

    public static BufferedImage carregaBufferedImageTransparecia(String file,
                                                                 Color cor) {
        BufferedImage srcBufferedImage = carregaImagem(file);
        BufferedImage bufferedImageRetorno = new BufferedImage(
                srcBufferedImage.getWidth(), srcBufferedImage.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Raster srcRaster = srcBufferedImage.getData();
        WritableRaster destRaster = bufferedImageRetorno.getRaster();
        int[] argbArray;

        for (int i = 0; i < srcBufferedImage.getWidth(); i++) {
            for (int j = 0; j < srcBufferedImage.getHeight(); j++) {
                argbArray = new int[4];
                argbArray = srcRaster.getPixel(i, j, argbArray);

                Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
                        argbArray[3]);
                if (c.equals(cor)) {
                    argbArray[3] = 0;
                }
                destRaster.setPixel(i, j, argbArray);
            }
        }

        return bufferedImageRetorno;
    }

    public static BufferedImage carregaImagem(String file) {
        BufferedImage bufferedImage;
        bufferedImage = (BufferedImage) bufferImages.get(file);
        if (bufferedImage == null) {
            try {
                bufferedImage = ImageUtil.toCompatibleImage(ImageIO
                        .read(CarregadorRecursos.recursoURL(file)));
            } catch (Exception e) {
            }
        }
        if (cache && bufferedImage != null) {
            bufferImages.put(file, bufferedImage);
        }
        return bufferedImage;
    }

    public static BufferedImage carregaImagemSemCache(String file) {
        try {
            return ImageUtil.toCompatibleImage(ImageIO
                    .read(CarregadorRecursos.recursoURL(file)));
        } catch (Exception e) {
        }
        return null;
    }

    public static BufferedImage carregaBufferedImageTransparecia(
            String string) {
        return carregaBufferedImageTransparecia(string, null);
    }

    public static List<String> carregarCreditosJogo() {
        List<String> creditos = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    CarregadorRecursos.recursoComoStream("creditos.txt"),
                    "UTF-8"));
            String linha = reader.readLine();
            while (linha != null) {
                creditos.add(linha + "\n");
                linha = reader.readLine();
            }
        } catch (IOException e1) {
            Logger.logarExept(e1);
        }

        return creditos;
    }

    public BufferedImage obterCapacete(Piloto piloto, String temporada) {
        if (temporada == null) {
            return desenhaCapacete(piloto);
        }
        if (Global.FORCE_MODELO_V2) {
            return desenhaCapacete(piloto);
        }
        try {
            String nomeOriginal = piloto.getNomeOriginal();
            String chave = nomeOriginal + temporada;
            BufferedImage ret = bufferCapacete.get(chave);
            if (ret == null) {
                String driverKey = nomeOriginal.replaceAll("\\.", "");
                if (SpriteSheet.isDisponivel(temporada)) {
                    int idx = indicePiloto(temporada, driverKey);
                    if (idx >= 0) {
                        ret = SpriteSheet.getCapacete(temporada, idx);
                    }
                }
                if (ret == null && !carregaApenasSprites) {
                    try {
                        ret = CarregadorRecursos.carregaImagem("capacetes/"
                                + temporada + "/" + driverKey + ".png");
                    } catch (Exception e) {
                    }
                }
                if (ret == null) {
                    ret = desenhaCapacete(piloto);
                }
                if (ret != null && cache) {
                    bufferCapacete.put(chave, ret);
                }
            }
            return ret;
        } catch (Exception e) {
            return null;
        }
    }

    public BufferedImage desenhaCapacete(Piloto piloto) {
        Carro carro = piloto.getCarro();
        return pintarModeloV2("png/capacete-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.CAP_W, SpriteSheet.CAP_H);
    }

    public Object carregarRecurso(String nmRecurso)
            throws ClassNotFoundException, IOException {
        ObjectInputStream ois = new ObjectInputStream(
                CarregadorRecursos.recursoComoStream(nmRecurso));
        return ois.readObject();
    }

    public BufferedImage obterCarroLado(Piloto piloto, String temporada) {
        if (temporada == null) {
            Vector<String> temps = getVectorTemps();
            if (temps != null && !temps.isEmpty()) {
                temporada = "t"+temps.lastElement();
            }
        }
        Carro carro = piloto.getCarro();
        if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
            return obterCarroLadoSemAreofolio(piloto, temporada);
        }
        if (Global.FORCE_MODELO_V2) {
            return desenhaCarroLado(carro);
        }
        BufferedImage carroLado = bufferCarrosLado.get(carro.getNome());
        if (carroLado == null) {
            if (SpriteSheet.isDisponivel(temporada) && carro.getImg() != null) {
                int idx = indiceTime(temporada, extrairTime(carro.getImg()));
                if (idx >= 0) {
                    carroLado = SpriteSheet.getCarroLado(temporada, idx);
                }
            }
            if (carroLado == null && !carregaApenasSprites && carro.getImg() != null) {
                try {
                    BufferedImage carroLadoPng;
                    if (carro.getImg().endsWith(".png")) {
                        carroLadoPng = CarregadorRecursos
                                .carregaImagem(carro.getImg());
                        carroLado = carroLadoPng;
                    } else {
                        carroLadoPng = CarregadorRecursos
                                .carregaImgSemCache(carro.getImg());

                        if (carroLadoPng != null) {
                            carroLado = carroLadoPng;
                        }
                    }
                } catch (Exception e) {
                    carroLado = desenhaCarroLado(carro);
                }
            }
            if (carroLado == null) {
                carroLado = desenhaCarroLado(carro);
            }
        }
        if (cache) {
            bufferCarrosLado.put(carro.getNome(), carroLado);
        }
        return carroLado;
    }

    private BufferedImage desenhaCarroLado(Carro carro) {
        return pintarModeloV2("png/carro-lado-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.LADO_W, SpriteSheet.LADO_H);
    }

    public BufferedImage obterCarroLadoSemAreofolio(Piloto piloto,
                                                    String temporada) {
        Carro carro = piloto.getCarro();
        BufferedImage carroLado = bufferCarrosLadoSemAreofolio
                .get(carro.getNome());
        if (carroLado == null) {
            if (SpriteSheet.isDisponivel(temporada) && carro.getImg() != null) {
                int idx = indiceTime(temporada, extrairTime(carro.getImg()));
                if (idx >= 0) {
                    carroLado = SpriteSheet.getCarroLado(temporada, idx);
                }
            }
            if (carroLado == null && !carregaApenasSprites && carro.getImg() != null) {
                try {
                    BufferedImage carroLadoPng;
                    carroLadoPng = CarregadorRecursos
                            .carregaImagem(carro.getImg());
                    carroLado = carroLadoPng;
                } catch (Exception e) {
                    carroLado = desenhaCArroladoSemAereofolio(carro);
                }
            }
            if (carroLado == null) {
                carroLado = desenhaCArroladoSemAereofolio(carro);
            }
        }
        if (cache) {
            bufferCarrosLadoSemAreofolio.put(carro.getNome(), carroLado);
        }
        return carroLado;
    }

    private BufferedImage desenhaCArroladoSemAereofolio(Carro carro) {
        return pintarModeloV2("png/carro-lado-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.LADO_W, SpriteSheet.LADO_H);
    }

    public BufferedImage obterCarroCimaSemAreofolio(Piloto piloto,
                                                    String temporada) {
        Carro carro = piloto.getCarro();
        if (Global.FORCE_MODELO_V2) {
            return desenhaCarroCimaSemAsa(carro);
        }
        BufferedImage carroCima = bufferCarrosCimaSemAreofolio
                .get(carro.getNome());
        if (carroCima == null && SpriteSheet.isDisponivel(temporada)
                && carro.getImg() != null) {
            int idx = indiceTime(temporada, extrairTime(carro.getImg()));
            BufferedImage top = SpriteSheet.getCarroCima(temporada, idx);
            List<String> times = getTimesOrdenados(temporada);
            int wingOverlayIdx = (times != null && times.size() > 10) ? times.size() : 10;
            BufferedImage wingOverlay = SpriteSheet.getWingOverlay(temporada, wingOverlayIdx);
            if (top != null && wingOverlay != null) {
                carroCima = ImageUtil.copiaImagem(top);
                Graphics2D graphics = (Graphics2D) carroCima.getGraphics();
                graphics.setComposite(composite);
                Util.setarHints(graphics);
                graphics.drawImage(wingOverlay, 0, 0, null);
                graphics.dispose();
            }
        }
        if (carroCima == null && !carregaApenasSprites && carro.getImg() != null) {
            carroCima = CarregadorRecursos.carregaImagemSemCache(
                    carro.getImg().replaceAll(".png", "_cima.png"));
            if (carroCima != null) {
                String[] split = carro.getImg().split("/");
                String nmimg = split[split.length - 1];
                BufferedImage wingOverlay = CarregadorRecursos.carregaImagem(
                        carro.getImg().replaceAll(nmimg, "wing_overlay_cima.png"));
                Graphics2D graphics = (Graphics2D) carroCima.getGraphics();
                graphics.setComposite(composite);
                Util.setarHints((Graphics2D) graphics);
                graphics.drawImage(wingOverlay, 0, 0, null);
                graphics.dispose();
            }
        }
        if (carroCima == null) {
            carroCima = desenhaCarroCimaSemAsa(carro);
        }
        if (cache) {
            bufferCarrosCimaSemAreofolio.put(carro.getNome(), carroCima);
        }
        return carroCima;
    }


    public BufferedImage obterCarroCima(Piloto piloto, String temporada) {
        if (piloto == null) {
            return null;
        }
        if (piloto.getCarro() == null) {
            return null;
        }
        Carro carro = piloto.getCarro();
        if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
            return obterCarroCimaSemAreofolio(piloto, temporada);
        }
        if (Global.FORCE_MODELO_V2) {
            preAquecerSemAsa(carro);
            return desenhaCarroCima(carro);
        }
        BufferedImage carroCima = bufferCarrosCima.get(carro.getNome());
        if (carroCima == null) {
            if (SpriteSheet.isDisponivel(temporada) && carro.getImg() != null) {
                int idx = indiceTime(temporada, extrairTime(carro.getImg()));
                if (idx >= 0) {
                    carroCima = SpriteSheet.getCarroCima(temporada, idx);
                }
            }
            if (carroCima == null && !carregaApenasSprites && carro.getImg() != null
                    && carro.getImg().endsWith("png")) {
                carroCima = CarregadorRecursos.carregaImagem(
                        carro.getImg().replaceAll(".png", "_cima.png"));
            }
        }
        if (carroCima == null) {
            carroCima = desenhaCarroCima(carro);
            preAquecerSemAsa(carro);
        }
        if (cache) {
            bufferCarrosCima.put(carro.getNome(), carroCima);
        }
        return carroCima;
    }

    public BufferedImage desenhaCarroCima(Carro carro) {
        return pintarModeloV2("png/carro-cima-v2.png", carro.getCor1(), carro.getCor2(), SpriteSheet.CIMA_W, SpriteSheet.CIMA_H);
    }

    public BufferedImage desenhaCarroCimaSemAsa(Carro carro) {
        return pintarModeloV2("png/carro-cima-v2.png", carro.getCor1(), carro.getCor2(),
                SpriteSheet.CIMA_W, SpriteSheet.CIMA_H, "png/carro-cima-sem_asa-v2.png");
    }

    private void preAquecerSemAsa(Carro carro) {
        if (bufferCarrosCimaSemAreofolio.containsKey(carro.getNome())) return;
        BufferedImage semAsa = desenhaCarroCimaSemAsa(carro);
        if (cache) {
            bufferCarrosCimaSemAreofolio.put(carro.getNome(), semAsa);
        }
    }

    public synchronized List<CircuitosDefault> carregarCircuitosDefaults()
            throws IOException, ClassNotFoundException {
        if (circuitosDefauts != null) {
            return circuitosDefauts;
        }
        circuitosDefauts = new ArrayList<CircuitosDefault>();
        Map<String, String> carregarCircuitos = ControleRecursos
                .carregarCircuitos();
        for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
                .hasNext(); ) {
            CircuitosDefault cd = new CircuitosDefault();
            String nmCircuitoOri = (String) iterator.next();
            cd.setNome(Util.substVogais(nmCircuitoOri));
            cd.setArquivo(carregarCircuitos.get(nmCircuitoOri));
            Circuito circuito = CarregadorRecursos.carregarCircuito(cd.getArquivo());
            cd.setProbalidadeChuva(circuito.getProbalidadeChuva());
            circuitosDefauts.add(cd);
        }

        return circuitosDefauts;
    }

    /**
     * Nome do arquivo de metadados (traçado + propriedades leves) a partir
     * do nome do arquivo de objetos, pelo mesmo padrão de sufixo usado para
     * derivar o jpg de referência (ver {@link #aplicarBackGroundPorConvencao}).
     */
    public static String nomeArquivoMetadados(String nmCircuitoXml) {
        return nmCircuitoXml.replaceFirst("\\.xml$", "_meta.xml");
    }

    /**
     * Lê só o terceiro campo (ativo) da linha correspondente em
     * {@code properties/circuitos.properties}
     * (<code>&lt;arquivo&gt;=&lt;NomeExibicao&gt;,&lt;ciclo&gt;,&lt;ativo&gt;</code>),
     * sem tocar em nenhum XML de circuito. Ausência da linha, do terceiro
     * campo, ou do próprio arquivo de properties equivale a {@code false}.
     */
    private static boolean lerAtivoDeCircuitosProperties(String nmCircuito) {
        Properties properties = new Properties();
        try (InputStream stream = recursoComoStream("properties/circuitos.properties")) {
            if (stream == null) {
                return false;
            }
            properties.load(stream);
        } catch (IOException e) {
            Logger.logarExept(e);
            return false;
        }
        String valor = properties.getProperty(nmCircuito);
        if (valor == null) {
            return false;
        }
        String[] campos = valor.split(",");
        if (campos.length < 3) {
            return false;
        }
        return Boolean.parseBoolean(campos[2].trim());
    }

    /**
     * Lê só a flag "ativo" de {@code properties/circuitos.properties}, sem
     * desserializar o circuito inteiro: a listagem de circuitos (menu/lobby)
     * só precisa desse boolean, e desserializar todos os circuitos (grafos
     * com milhares de pontos e objetos) só pra isso deixava todos presos no
     * bufferCircuitos desde a abertura do menu — o circuito completo só
     * deve ser carregado quando uma corrida (ou o preview do menu)
     * realmente o usa.
     */
    public static boolean circuitoAtivo(String nmCircuito) {
        Circuito emCache = bufferCircuitos.get(nmCircuito);
        if (emCache != null) {
            return emCache.isAtivo();
        }
        return lerAtivoDeCircuitosProperties(nmCircuito);
    }

    /**
     * Decodifica {@code <nome>_mro.xml} (objetos/objetosCenario) e, se
     * existir, {@code <nome>_mro_meta.xml} (metadados leves + pista/box),
     * mesclando os dois num único {@code Circuito}. {@code ativo} é sempre
     * populado a partir de {@code circuitos.properties} (nunca do XML,
     * mesmo em circuitos ainda não migrados para o novo formato de dois
     * arquivos). O circuito retornado já sai vetorizado
     * ({@link Circuito#vetorizarPista()}), então nenhum chamador precisa
     * fazer isso de novo.
     */
    public static Circuito carregarCircuito(String nmCircuito)
            throws IOException, ClassNotFoundException {
        Circuito circuito = bufferCircuitos.get(nmCircuito);
        if (circuito == null) {
            XMLDecoder decoderObjetos = new XMLDecoder(CarregadorRecursos.recursoComoStream("circuitos/" + nmCircuito));
            Circuito circuitoObjetos = (Circuito) decoderObjetos.readObject();
            InputStream streamMeta = recursoComoStream("circuitos/" + nomeArquivoMetadados(nmCircuito));
            if (streamMeta != null) {
                Circuito circuitoMeta = (Circuito) new XMLDecoder(streamMeta).readObject();
                circuitoMeta.setObjetos(circuitoObjetos.getObjetos());
                circuitoMeta.setObjetosCenario(circuitoObjetos.getObjetosCenario());
                circuito = circuitoMeta;
            } else {
                circuito = circuitoObjetos;
            }
            circuito.setAtivo(circuitoAtivo(nmCircuito));
            aplicarBackGroundPorConvencao(circuito, nmCircuito);
            migrarObjetoLivreParaCenario(circuito);
            circuito.vetorizarPista();
        }
        if (cache) {
            bufferCircuitos.put(nmCircuito, circuito);
        }
        return circuito;
    }

    /**
     * Leitura leve de um circuito: decodifica só
     * {@code <nome>_mro_meta.xml} (metadados + pista/box, suficiente para
     * desenhar uma miniatura), sem tocar em {@code <nome>_mro.xml}
     * (objetos/objetosCenario) nem chamar {@code vetorizarPista()}. Se o
     * arquivo de metadados não existir (circuito no formato antigo de
     * arquivo único), cai no carregamento completo via
     * {@link #carregarCircuito}.
     */
    public static Circuito carregarMetadadosCircuito(String nmCircuito)
            throws IOException, ClassNotFoundException {
        InputStream streamMeta = recursoComoStream("circuitos/" + nomeArquivoMetadados(nmCircuito));
        if (streamMeta == null) {
            return carregarCircuito(nmCircuito);
        }
        Circuito circuitoMeta = (Circuito) new XMLDecoder(streamMeta).readObject();
        circuitoMeta.setAtivo(circuitoAtivo(nmCircuito));
        return circuitoMeta;
    }

    /**
     * Atualiza (ou acrescenta) o terceiro campo CSV (ativo) da linha de
     * {@code nmCircuitoXml} em {@code src/main/resources/properties/circuitos.properties},
     * preservando todas as outras linhas exatamente como estavam — leitura e
     * escrita linha a linha, não via {@code Properties.store()} (que
     * reordena as linhas e escreve um comentário de timestamp, gerando
     * diffs git ruidosos). Se a linha de {@code nmCircuitoXml} ainda não
     * existir (circuito nunca listado em circuitos.properties), não grava
     * nada e só registra um aviso — criar automaticamente a linha
     * (nome de exibição/ciclo) de um circuito novo está fora do escopo desta
     * mudança.
     */
    public static void atualizarAtivoEmCircuitosProperties(String nmCircuitoXml, boolean ativo) throws IOException {
        atualizarAtivoEmCircuitosProperties(new File("src/main/resources/properties/circuitos.properties"),
                nmCircuitoXml, ativo);
    }

    /**
     * Mesma lógica de {@link #atualizarAtivoEmCircuitosProperties(String, boolean)},
     * mas recebendo o arquivo alvo explicitamente — usado por testes para não
     * mutar o {@code circuitos.properties} real do projeto.
     */
    static void atualizarAtivoEmCircuitosProperties(File arquivo, String nmCircuitoXml, boolean ativo)
            throws IOException {
        if (!arquivo.exists()) {
            return;
        }
        List<String> linhas = new ArrayList<String>();
        boolean encontrada = false;
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String linha;
            while ((linha = reader.readLine()) != null) {
                int idxIgual = linha.indexOf('=');
                if (idxIgual > 0 && linha.substring(0, idxIgual).equals(nmCircuitoXml)) {
                    String[] campos = linha.substring(idxIgual + 1).split(",", -1);
                    String nomeExibicao = campos.length > 0 ? campos[0] : "";
                    String ciclo = campos.length > 1 ? campos[1] : "";
                    linha = nmCircuitoXml + "=" + nomeExibicao + "," + ciclo + "," + ativo;
                    encontrada = true;
                }
                linhas.add(linha);
            }
        }
        if (!encontrada) {
            Logger.logar("circuitos.properties não tem entrada para " + nmCircuitoXml
                    + "; valor de ativo não foi gravado.");
            return;
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(arquivo), StandardCharsets.UTF_8))) {
            for (String linha : linhas) {
                writer.write(linha);
                writer.newLine();
            }
        }
    }

    /**
     * Deriva o nome do jpg de referência do próprio nome do XML do circuito
     * (ex.: "albert_park_mro.xml" -> "albert_park_mro.jpg") em vez de
     * depender de uma propriedade gravada no XML.
     * <p>
     * Com {@link Global#GERAR_IMAGEM_CIRCUITO_EM_MEMORIA} ativa, o nome é
     * atribuído sempre, mesmo sem o jpg nos recursos: os *_mro.jpg ficam de
     * fora do jar final (são só referência de edição, ver exclusão no
     * pom.xml) e o nome vira apenas a chave que o cliente web usa em
     * /letsRace/circuitoJpg/&lt;nome&gt; para pedir a imagem gerada
     * proceduralmente. Sem a flag (caminho legado, que lê o jpg de verdade),
     * mantém o comportamento de só atribuir se o arquivo existir.
     */
    private static void aplicarBackGroundPorConvencao(Circuito circuito, String nmCircuitoXml) {
        String nomeJpg = nmCircuitoXml.replaceFirst("\\.xml$", ".jpg");
        if (Global.GERAR_IMAGEM_CIRCUITO_EM_MEMORIA
                || CarregadorRecursos.recursoURL("circuitos/" + nomeJpg) != null) {
            circuito.definirBackGroundPorConvencao(nomeJpg);
        }
    }

    /**
     * Migra ObjetoLivre gravado em circuito.objetos (classificação legada,
     * de quando ObjetoLivre não era considerado um objeto de cenário) para
     * circuito.objetosCenario (classificação atual) — sem isso, circuitos
     * XML salvos antes dessa mudança manteriam esses objetos "presos" na
     * lista de objetos de função (Escapada/Transparencia) e nunca seriam
     * desenhados de fato em corrida, já que DesenhoProceduralCircuito só
     * desenha objetosCenario. Idempotente: não faz nada se já migrado.
     */
    private static void migrarObjetoLivreParaCenario(Circuito circuito) {
        List<ObjetoPista> objetos = circuito.getObjetos();
        if (objetos == null) {
            return;
        }
        List<ObjetoPista> livres = new ArrayList<ObjetoPista>();
        for (Iterator<ObjetoPista> iterator = objetos.iterator(); iterator.hasNext(); ) {
            ObjetoPista objetoPista = iterator.next();
            if (objetoPista instanceof ObjetoLivre) {
                livres.add(objetoPista);
                iterator.remove();
            }
        }
        if (livres.isEmpty()) {
            return;
        }
        if (circuito.getObjetosCenario() == null) {
            circuito.setObjetosCenario(new ArrayList<ObjetoPista>());
        }
        circuito.getObjetosCenario().addAll(livres);
    }

    private static class PilotoComparator implements Comparator<Piloto> {
        public int compare(Piloto piloto0, Piloto piloto1) {
            return Integer.valueOf(piloto1.getHabilidadeReal())
                    .compareTo(Integer.valueOf(piloto0.getHabilidadeReal()));
        }
    }

    private static class CarroComparator implements Comparator<Carro> {
        @Override
        public int compare(Carro carro1, Carro carro2) {
            return carro1.getNome().compareTo(carro2.getNome());
        }

    }
}
