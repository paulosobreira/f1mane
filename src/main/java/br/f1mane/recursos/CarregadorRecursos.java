package br.f1mane.recursos;

import br.f1mane.controles.ControleRecursos;
import br.f1mane.entidades.*;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.beans.XMLDecoder;
import java.io.*;
import java.net.URL;
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
    private static final AlphaComposite composite = AlphaComposite
            .getInstance(AlphaComposite.DST_OUT, 1);

    private static CarregadorRecursos carregadorRecursos;

    private static boolean cache;

    private static String versao;

    private static String versaoMesAno;

    private static URL codeBase;

    private static String applet;

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
        return decimalFormat.format(new Integer(getVersao())) + " " + getVersaoMesAno();
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
            int habilidade = Integer
                    .parseInt(String.valueOf(duasCasas) + Util.intervalo(0, 9));
            if (habilidade > 999) {
                habilidade = 999;
            }
            piloto.setHabilidade(habilidade);
            piloto.setHabilidadeReal(
                    Integer.parseInt(String.valueOf(duasCasas) + "0"));
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
        try {
            String nomeOriginal = piloto.getNomeOriginal();
            String chave = nomeOriginal + temporada;
            BufferedImage ret = bufferCapacete.get(chave);
            if (ret == null) {
                try {
                    ret = CarregadorRecursos.carregaImagem("capacetes/"
                            + temporada + "/"
                            + nomeOriginal.replaceAll("\\.", "") + ".png");
                } catch (Exception e) {
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
        BufferedImage ret;
        Carro carro = piloto.getCarro();
        BufferedImage base = CarregadorRecursos.carregaImagem("png/Capacete.png");
        BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
                carro.getCor1(), "png/CapaceteC1.png", base.getType());
        BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
                carro.getCor2(), "png/CapaceteC2.png", base.getType());
        BufferedImage capacete = new BufferedImage(base.getWidth(),
                base.getHeight(), base.getType());
        Graphics graphics = capacete.getGraphics();
        Util.setarHints((Graphics2D) graphics);
        graphics.drawImage(base, 0, 0, null);
        graphics.drawImage(cor2, 0, 0, null);
        graphics.drawImage(cor1, 0, 0, null);
        graphics.dispose();
        ret = capacete;
        return ret;
    }

    public Object carregarRecurso(String nmRecurso)
            throws ClassNotFoundException, IOException {
        ObjectInputStream ois = new ObjectInputStream(
                CarregadorRecursos.recursoComoStream(nmRecurso));
        return ois.readObject();
    }

    public BufferedImage obterCarroLado(Piloto piloto, String temporada) {
        Carro carro = piloto.getCarro();
        if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
            return obterCarroLadoSemAreofolio(piloto, temporada);

        }
        BufferedImage carroLado = bufferCarrosLado.get(carro.getNome());
        if (carroLado == null) {
            if (carro.getImg() != null) {
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
            } else {
                carroLado = desenhaCarroLado(carro);
            }
        }
        if (cache) {
            bufferCarrosLado.put(carro.getNome(), carroLado);
        }
        return carroLado;
    }

    private BufferedImage desenhaCarroLado(Carro carro) {
        BufferedImage carroLado = CarregadorRecursos
                .carregaImagem("png/CarroLado.png");
        BufferedImage cor1 = CarregadorRecursos
                .gerarCoresCarros(carro.getCor1(), "png/CarroLadoC1.png");
        BufferedImage cor2 = CarregadorRecursos
                .gerarCoresCarros(carro.getCor2(), "png/CarroLadoC2.png");
        Graphics graphics = carroLado.getGraphics();
        Util.setarHints((Graphics2D) graphics);
        graphics.drawImage(cor1, 0, 0, null);
        graphics.drawImage(cor2, 0, 0, null);
        graphics.dispose();
        return carroLado;
    }

    public BufferedImage obterCarroLadoSemAreofolio(Piloto piloto,
                                                    String temporada) {
        Carro carro = piloto.getCarro();
        BufferedImage carroLado = bufferCarrosLadoSemAreofolio
                .get(carro.getNome());
        if (carroLado == null) {
            if (carro.getImg() != null) {
                try {
                    BufferedImage carroLadoPng;
                    carroLadoPng = CarregadorRecursos
                            .carregaImagem(carro.getImg());
                    carroLado = carroLadoPng;
                } catch (Exception e) {
                    carroLado = desenhaCArroladoSemAereofolio(carro);
                }
            } else {
                carroLado = desenhaCArroladoSemAereofolio(carro);
            }
        }
        if (cache) {
            bufferCarrosLadoSemAreofolio.put(carro.getNome(), carroLado);
        }
        return carroLado;
    }

    private BufferedImage desenhaCArroladoSemAereofolio(Carro carro) {
        BufferedImage carroLado = CarregadorRecursos
                .carregaImagem("png/CarroLado.png");
        BufferedImage cor1 = CarregadorRecursos
                .gerarCoresCarros(carro.getCor1(), "png/CarroLadoC1.png");
        BufferedImage cor2 = CarregadorRecursos
                .gerarCoresCarros(carro.getCor2(), "png/CarroLadoC3.png");
        Graphics graphics = carroLado.getGraphics();
        Util.setarHints((Graphics2D) graphics);
        graphics.drawImage(cor1, 0, 0, null);
        graphics.drawImage(cor2, 0, 0, null);
        graphics.dispose();
        return carroLado;
    }

    public BufferedImage obterCarroCimaSemAreofolio(Piloto piloto,
                                                    String temporada) {
        String modelo = obterModeloCarroCima(temporada);
        Carro carro = piloto.getCarro();
        BufferedImage carroCima = bufferCarrosCimaSemAreofolio
                .get(carro.getNome());
        if (carro.getImg() != null) {
            carroCima = CarregadorRecursos.carregaImagemSemCache(
                    carro.getImg().replaceAll(".png", "_cima.png"));
            if (carroCima != null) {
                String[] split = carro.getImg().split("/");
                String nmimg = split[split.length - 1];
                BufferedImage noWing = CarregadorRecursos.carregaImagem(
                        carro.getImg().replaceAll(nmimg, "nowing_cima.png"));
                Graphics2D graphics = (Graphics2D) carroCima.getGraphics();
                graphics.setComposite(composite);
                Util.setarHints((Graphics2D) graphics);
                graphics.drawImage(noWing, 0, 0, null);
                graphics.dispose();
            }
        }
        if (carroCima == null) {
            BufferedImage base = CarregadorRecursos
                    .carregaImagem(modelo + "CarroCima.png");
            carroCima = new BufferedImage(base.getWidth(), base.getHeight(),
                    base.getType());
            BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
                    carro.getCor1(), modelo + "CarroCimaC1.png",
                    base.getType());
            BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
                    carro.getCor2(), modelo + "CarroCimaC3.png",
                    base.getType());
            Graphics graphics = carroCima.getGraphics();
            Util.setarHints((Graphics2D) graphics);
            graphics.drawImage(base, 0, 0, null);
            graphics.drawImage(cor2, 0, 0, null);
            graphics.drawImage(cor1, 0, 0, null);
            graphics.dispose();
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
        String modelo = obterModeloCarroCima(temporada);
        Carro carro = piloto.getCarro();
        if (Carro.PERDEU_AEREOFOLIO.equals(piloto.getCarro().getDanificado())) {
            return obterCarroCimaSemAreofolio(piloto, temporada);
        }
        BufferedImage carroCima = bufferCarrosCima.get(carro.getNome());
        if (carroCima == null && carro.getImg() != null
                && carro.getImg().endsWith("png")) {
            carroCima = CarregadorRecursos.carregaImagem(
                    carro.getImg().replaceAll(".png", "_cima.png"));
        }
        if (carroCima == null) {
            carroCima = desenhaCarroCima(modelo, carro);
        }
        if (cache) {
            bufferCarrosCima.put(carro.getNome(), carroCima);
        }
        return carroCima;
    }

    public BufferedImage desenhaCarroCima(String modelo, Carro carro) {
        BufferedImage carroCima;
        BufferedImage base = CarregadorRecursos
                .carregaImagem(modelo + "CarroCima.png");
        carroCima = new BufferedImage(base.getWidth(), base.getHeight(),
                base.getType());
        BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
                carro.getCor1(), modelo + "CarroCimaC1.png", base.getType());
        BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
                carro.getCor2(), modelo + "CarroCimaC2.png", base.getType());
        Graphics graphics = carroCima.getGraphics();
        Util.setarHints((Graphics2D) graphics);
        graphics.drawImage(base, 0, 0, null);
        graphics.drawImage(cor2, 0, 0, null);
        graphics.drawImage(cor1, 0, 0, null);
        graphics.dispose();
        return carroCima;
    }

    private String obterModeloCarroCima(String temporada) {
        String modelo = "png/cima2017/";
        if (temporada == null) {
            return modelo;
        }
        int anoTemporada = Integer.parseInt(temporada.replace("t", ""));
        if (anoTemporada < 2017) {
            modelo = "png/cima20092016/";
        }
        if (anoTemporada < 2009) {
            modelo = "png/cima19982008/";
        }
        if (anoTemporada < 1997) {
            modelo = "png/cima19801997/";
        }
        if (anoTemporada <= 1980) {
            modelo = "png/cima19701979/";
        }
        return modelo;
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

    public static Circuito carregarCircuito(String nmCircuito)
            throws IOException, ClassNotFoundException {
        Circuito circuito = bufferCircuitos.get(nmCircuito);
        if (circuito == null) {
            XMLDecoder xmlDecoder = new XMLDecoder(CarregadorRecursos.recursoComoStream("circuitos/" + nmCircuito));
            circuito = (Circuito) xmlDecoder.readObject();
        }
        if (cache) {
            bufferCircuitos.put(nmCircuito, circuito);
        }
        return circuito;
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
