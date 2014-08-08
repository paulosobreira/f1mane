package sowbreira.f1mane.recursos;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Piloto;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;

public class CarregadorRecursos {
	private HashMap temporadas;
	private Vector vectorTemps;
	private static Map bufferImages = new HashMap();
	private static Map bufferCarros = new HashMap();

	public CarregadorRecursos(boolean carregaTemp) {
		if (carregaTemp)
			carregarTemporadas();
	}

	public Vector getVectorTemps() {
		return vectorTemps;
	}

	public void carregarTemporadas() {
		if (temporadas != null) {
			return;
		}
		if (temporadas == null) {
			temporadas = new HashMap();
			vectorTemps = new Vector();
		}
		final Properties properties = new Properties();

		try {
			properties
					.load(recursoComoStreamIn("properties/temporadas.properties"));

			Enumeration propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String name = (String) propName.nextElement();
				temporadas.put(properties.getProperty(name), name);
				vectorTemps.add(properties.getProperty(name));
			}
			Collections.sort(vectorTemps, new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					String o1s = (String) o1;
					String o2s = (String) o2;
					return o2s.compareTo(o1s);
				}

			});
		} catch (IOException e) {
			Logger.logarExept(e);
		}
	}

	public HashMap getTemporadas() {
		return temporadas;
	}

	public static URL carregarImagem(String imagem) {
		return CarregadorRecursos.class.getResource(imagem);
	}

	public static BufferedImage carregaBufferedImageTranspareciaBranca(
			String file) {
		BufferedImage buffer = null;
		try {
			buffer = ImageUtil.toBufferedImage(file);
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
			Logger.logarExept(e);
		}

		return ImageUtil.geraTransparencia(buffer);
	}

	public static BufferedImage carregaBufferedImage(String file) {
		BufferedImage buffer = null;
		try {
			buffer = ImageUtil.toBufferedImage(file);
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
			Logger.logarExept(e);
		}

		return buffer;
	}

	public static BufferedImage carregaBufferedImageTranspareciaBranca(
			String file, int ingVal) {
		return carregaBufferedImageTranspareciaBranca(file, ingVal, 255);
	}

	public static BufferedImage carregaBufferedImageTranspareciaBranca(
			String file, int ingVal, int translucidez) {
		BufferedImage buffer = null;
		try {
			buffer = ImageUtil.toBufferedImage(file);
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
			Logger.logarExept(e);
		}

		return ImageUtil.geraTransparencia(buffer, ingVal, translucidez);
	}

	public static BufferedImage carregaBufferedImageTranspareciaPreta(
			String file, int translucidez) {
		BufferedImage buffer = null;
		try {
			buffer = ImageUtil.toBufferedImage(file);
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
			Logger.logarExept(e);
		}

		return ImageUtil.geraTransparencia(buffer, Color.BLACK, translucidez);
	}

	public static BufferedImage carregaBackGround(String backGroundStr,
			JPanel panel, Circuito circuito) {

		BufferedImage backGround = null;
		try {
			backGround = ImageIO.read(CarregadorRecursos.class
					.getResource(backGroundStr));
		} catch (IOException e) {
			Logger.logarExept(e);
		}
		if (panel != null)
			panel.setSize(backGround.getWidth(), backGround.getHeight());

		if (backGround == null) {
			Logger.logar("backGround=" + backGround);
			System.exit(1);
		}

		circuito.setBackGround(backGroundStr);

		return backGround;
	}

	public static InputStream recursoComoStream(String string) {
		CarregadorRecursos rec = new CarregadorRecursos(false);
		return rec.getClass().getResourceAsStream(string);
	}

	public InputStream recursoComoStreamIn(String string) {
		return this.getClass().getResourceAsStream(string);
	}

	public static void main(String[] args) throws URISyntaxException,
			IOException, ClassNotFoundException {
		// String val = "tn_2008voi-mclaren.gif";
		// System.out.println(Util.intervalo(0, 0));

		// gerarListaCarrosLado();
		// gerarCarrosCima();
		// JFrame frame = new JFrame();
		// frame.setSize(200, 200);
		// frame.setVisible(true);
		// Graphics2D graphics2d = (Graphics2D) frame.getContentPane()
		// .getGraphics();
		// BufferedImage gerarCorresCarros = gerarCorresCarros(Color.BLUE, 1);
		// graphics2d.drawImage(gerarCorresCarros, 0, 0, null);
		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(false);

		// Properties properties = new Properties();
		//
		// properties.load(CarregadorRecursos
		// .recursoComoStream("properties/pistas.properties"));
		//
		// Enumeration propName = properties.propertyNames();
		// double media = 0;
		// double qtde = 0;
		// while (propName.hasMoreElements()) {
		// final String name = (String) propName.nextElement();
		// // System.out.println(name);
		// ObjectInputStream ois = new ObjectInputStream(carregadorRecursos
		// .getClass().getResourceAsStream(name));
		//
		// Circuito circuito = (Circuito) ois.readObject();
		// // System.out.println(properties.getProperty(name));
		// System.out.println(name + " " + circuito.getNome() + " "
		// + circuito.getMultiplciador());
		// media += circuito.getMultiplciador();
		// qtde++;
		// // circuito.setMultiplicador(circuito.getMultiplciador() + 1);
		// // FileOutputStream fileOutputStream = new FileOutputStream(new
		// // File(
		// // name));
		// // ObjectOutputStream oos = new
		// // ObjectOutputStream(fileOutputStream);
		// // oos.writeObject(circuito);
		// // oos.flush();
		// // fileOutputStream.close();
		// }
		// System.out.println("Media "+(media/qtde));

		// BufferedImage travadaRodaImg = CarregadorRecursos
		// .carregaBufferedImageTranspareciaBranca("travadaRoda.png", 200,
		// 50);
		// JOptionPane.showConfirmDialog(null, new JLabel(new ImageIcon(
		// travadaRodaImg)));
	}

	private static void gerarListaCarrosLado() throws IOException {
		List carList = new LinkedList();
		File file = new File("src/sowbreira/f1mane/recursos/carros");
		File[] dir = file.listFiles();
		for (int i = 0; i < dir.length; i++) {
			if (!dir[i].getName().startsWith(".")) {
				File[] imgCar = dir[i].listFiles();
				for (int j = 0; j < imgCar.length; j++) {
					if (!imgCar[j].getName().startsWith(".")
							&& !imgCar[j].getName().equals("Thumbs.db")) {
						String str = imgCar[j].getPath().split("recursos")[1];
						str = str.substring(1, str.length());
						carList.add(str);

					}
				}
			}
		}
		FileWriter fileWriter = new FileWriter(
				"src/sowbreira/f1mane/recursos/carlist.txt");
		for (Iterator iterator = carList.iterator(); iterator.hasNext();) {
			String carro = (String) iterator.next();
			StringBuffer nCarro = new StringBuffer();
			for (int i = 0; i < carro.length(); i++) {
				if (carro.charAt(i) == '\\') {
					nCarro.append('/');
				} else {
					nCarro.append(carro.charAt(i));
				}
			}
			Logger.logar(nCarro.toString());
			fileWriter.write(nCarro.toString() + "\n");
		}
		fileWriter.close();

	}

	private static void gerarCarrosCima() throws IOException {
		File fileT = new File("src/sowbreira/f1mane/recursos/properties");
		File[] dirT = fileT.listFiles();
		for (int i = 0; i < dirT.length; i++) {
			String temporarada = dirT[i].getName();
			if (!temporarada.contains(".")) {

				Properties properties = new Properties();

				properties.load(CarregadorRecursos.class
						.getResourceAsStream("properties/" + temporarada
								+ "/carros.properties"));

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
					carro.setCor1(new Color(Integer.parseInt(red), Integer
							.parseInt(green), Integer.parseInt(blue)));

					red = values[5];
					green = values[6];
					blue = values[7];
					carro.setCor2(new Color(Integer.parseInt(red), Integer
							.parseInt(green), Integer.parseInt(blue)));
					BufferedImage carroCima = CarregadorRecursos
							.carregaImg("CarroCima.png");

					BufferedImage cor1 = gerarCoresCarros(carro.getCor1(),
							"CarroCimaC1.png");
					BufferedImage cor2 = gerarCoresCarros(carro.getCor2(),
							"CarroCimaC2.png");
					Graphics graphics = carroCima.getGraphics();
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

	public static BufferedImage gerarCoresCarros(Color corPintar, String carro) {
		ImageIcon img = new ImageIcon(
				CarregadorRecursos.class.getResource(carro));

		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);
		srcBufferedImage = ImageUtil.geraTransparencia(srcBufferedImage,
				Color.BLACK);
		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];
		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);
				Color c = new Color(argbArray[0], argbArray[1], argbArray[2]);
				argbArray[0] = (int) ((argbArray[0] + corPintar.getRed()) / 2);
				argbArray[1] = (int) ((argbArray[1] + corPintar.getGreen()) / 2);
				argbArray[2] = (int) ((argbArray[2] + corPintar.getBlue()) / 2);
				if (Color.WHITE.equals(c)) {
					argbArray[3] = 0;
				}

				// argbArray[3] = 255;
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
		bufferedImage = carregaBufferedImageTranspareciaBranca(img);
		bufferCarros.put(img, bufferedImage);
		return bufferedImage;
	}

	public List carregarListaPilotos(String temporarada) throws IOException {
		List retorno = new ArrayList();
		Properties properties = new Properties();

		properties.load(recursoComoStreamIn("properties/" + temporarada
				+ "/pilotos.properties"));

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
			int duasCasas = Integer.parseInt(prop.split(",")[1])
					+ (Math.random() > .5 ? (-1 * Util.intervalo(0, 1)) : Util
							.intervalo(0, 1));
			piloto.setHabilidade(Integer.parseInt(String.valueOf(duasCasas)
					+ Util.intervalo(0, 9)));
			retorno.add(piloto);
		}
		Collections.sort(retorno, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;

				return new Integer(piloto1.getHabilidade())
						.compareTo(new Integer(piloto0.getHabilidade()));
			}
		});

		return retorno;
	}

	public List carregarListaCarros(String temporada) throws IOException {
		if (temporadas.get(temporada) != null) {
			List pilotos = (List) temporadas.get(temporada);
			Set carros = new HashSet();
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				carros.add(piloto.getCarro());
			}
			List carrosL = new ArrayList(carros);
			Collections.sort(carrosL, new Comparator() {

				@Override
				public int compare(Object o1, Object o2) {
					Carro carro1 = (Carro) o1;
					Carro carro2 = (Carro) o2;
					return carro1.getNome().compareTo(carro2.getNome());
				}

			});
			return carrosL;
		}

		return carregarListaCarrosArquivo(temporada);
	}

	public List carregarListaCarrosArquivo(String temporada) throws IOException {
		List retorno = new ArrayList();
		Properties properties = new Properties();
		properties.load(recursoComoStreamIn("properties/" + temporada
				+ "/carros.properties"));
		Enumeration propNames = properties.propertyNames();
		while (propNames.hasMoreElements()) {
			Carro carro = new Carro();
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
			carro.setCor1(new Color(Integer.parseInt(red), Integer
					.parseInt(green), Integer.parseInt(blue)));
			red = values[5];
			green = values[6];
			blue = values[7];
			carro.setCor2(new Color(Integer.parseInt(red), Integer
					.parseInt(green), Integer.parseInt(blue)));
			retorno.add(carro);
		}
		return retorno;
	}

	public void ligarPilotosCarros(List pilotos, List carros) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();

			for (Iterator iterator = carros.iterator(); iterator.hasNext();) {
				Carro carro = (Carro) iterator.next();

				if (piloto.getNomeCarro().equals(carro.getNome())) {
					piloto.setCarro(criarCopiaCarro(carro, piloto));
				}
			}
		}
	}

	public static Carro criarCopiaCarro(Carro carro, Piloto piloto) {
		Carro carroNovo = new Carro();
		carroNovo.setNome(carro.getNome());
		carroNovo.setCor1(carro.getCor1());
		carroNovo.setCor2(carro.getCor2());
		carroNovo.setImg(carro.getImg());
		carroNovo.setPiloto(piloto);
		carroNovo.setPotenciaReal(carro.getPotencia());
		carroNovo.setAerodinamica(carro.getAerodinamica());
		carroNovo.setFreios(carro.getFreios());
		carroNovo.setPotencia(carro.getPotencia()
				+ (Math.random() > .5 ? -5 : 5));
		return carroNovo;
	}

	public Map carregarTemporadasPilotos() {
		Map circuitosPilotos = new HashMap();
		final Properties properties = new Properties();
		try {
			properties
					.load(recursoComoStreamIn("properties/temporadas.properties"));
			Enumeration propName = properties.propertyNames();
			while (propName.hasMoreElements()) {
				final String temporada = (String) propName.nextElement();
				List pilotos = carregarListaPilotos(temporada);
				List carros = carregarListaCarros(temporada);
				ligarPilotosCarros(pilotos, carros);
				circuitosPilotos.put(temporada, pilotos);
			}
		} catch (IOException e) {
			Logger.logarExept(e);
		}
		return circuitosPilotos;
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
		bufferImages.put(img, bufferedImage);
		return bufferedImage;
	}

	public static BufferedImage carregaBufferedImageMeiaTransparenciaBraca(
			String file) {
		BufferedImage buffer = null;
		try {
			buffer = ImageUtil.toBufferedImage(file);
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
			Logger.logarExept(e);
		}
		ImageIcon img = new ImageIcon(buffer);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
				argbArray = new int[4];
				argbArray = srcRaster.getPixel(i, j, argbArray);

				Color c = new Color(argbArray[0], argbArray[1], argbArray[2],
						argbArray[3]);
				if (c.getRed() > 250 && c.getGreen() > 250 && c.getBlue() > 250) {
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
		BufferedImage buffer = carregaImagem(file);
		ImageIcon img = new ImageIcon(buffer);
		BufferedImage srcBufferedImage = new BufferedImage(img.getIconWidth(),
				img.getIconHeight(), BufferedImage.TYPE_INT_ARGB);

		srcBufferedImage.getGraphics().drawImage(img.getImage(), 0, 0, null);

		BufferedImage bufferedImageRetorno = new BufferedImage(
				img.getIconWidth(), img.getIconHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Raster srcRaster = srcBufferedImage.getData();
		WritableRaster destRaster = bufferedImageRetorno.getRaster();
		int[] argbArray = new int[4];

		for (int i = 0; i < img.getIconWidth(); i++) {
			for (int j = 0; j < img.getIconHeight(); j++) {
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
		try {
			return ImageIO.read(CarregadorRecursos.class.getResource(file));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static BufferedImage carregaBufferedImageTransparecia(String string) {
		return carregaBufferedImageTransparecia(string, null);
	}
}
