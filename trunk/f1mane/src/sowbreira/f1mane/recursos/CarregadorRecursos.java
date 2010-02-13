package sowbreira.f1mane.recursos;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
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

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Piloto;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;

public class CarregadorRecursos {
	private HashMap temporadas;
	private Vector vectorTemps;
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
			ImageIcon icon = new ImageIcon(CarregadorRecursos.class
					.getResource(file));
			buffer = ImageUtil.toBufferedImage(icon.getImage());
			if (buffer == null) {
				Logger.logar("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			Logger.logar("Erro gerando transparencia para :" + file);
		}

		return ImageUtil.geraTransparencia(buffer);
	}

	public static BufferedImage carregaBackGround(String backGroundStr,
			JPanel panel, Circuito circuito) {
		ImageIcon icon = new ImageIcon(CarregadorRecursos.class
				.getResource(backGroundStr));
		BufferedImage backGround = ImageUtil.toBufferedImage(icon.getImage());
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
			IOException {
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
			piloto.setNome(name);
			piloto.setNomeCarro(prop.split(",")[0]);
			int duasCasas = Integer.parseInt(prop.split(",")[1])
					+ (Math.random() > .5 ? -1 : 1);
			piloto.setHabilidade(Integer.parseInt(String.valueOf(duasCasas)
					+ (int) (0 + Math.random() * 9)));
			// Logger.logar(piloto + " " + piloto.getHabilidade());
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

	public List carregarListaCarros(String temporarada) throws IOException {
		if (temporadas.get(temporarada) != null) {
			List pilotos = (List) temporadas.get(temporarada);
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

		List retorno = new ArrayList();
		Properties properties = new Properties();

		properties.load(recursoComoStreamIn("properties/" + temporarada
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

	public Carro criarCopiaCarro(Carro carro, Piloto piloto) {
		Carro carroNovo = new Carro();
		carroNovo.setNome(carro.getNome());
		carroNovo.setCor1(carro.getCor1());
		carroNovo.setCor2(carro.getCor2());
		carroNovo.setImg(carro.getImg());
		carroNovo.setPiloto(piloto);
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

}
