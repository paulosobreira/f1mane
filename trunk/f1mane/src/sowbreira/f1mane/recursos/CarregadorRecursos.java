package sowbreira.f1mane.recursos;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import sowbreira.f1mane.entidades.Circuito;
import br.nnpe.ImageUtil;

public class CarregadorRecursos {

	private static Map bufferCarros = new HashMap();

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
				System.out.println("img=" + buffer);
				System.exit(1);
			}

		} catch (Exception e) {
			System.out.println("Erro gerando transparencia para :" + file);
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
			System.out.println("backGround=" + backGround);
			System.exit(1);
		}

		circuito.setBackGround(backGroundStr);

		return backGround;
	}

	public static InputStream recursoComoStream(String string) {
		CarregadorRecursos rec = new CarregadorRecursos();

		return rec.getClass().getResourceAsStream(string);
	}

	public static void main(String[] args) {

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
}
