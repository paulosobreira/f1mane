package sowbreira.f1mane.paddock.servlet;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nnpe.Util;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.recursos.CarregadorRecursos;

@Path("/paddock")
public class ServletRest {

	@GET
	@Path("/piloto")
	@Produces(MediaType.APPLICATION_JSON)
	public ClientPaddockPack showHelloWorld() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setNomeJogador("Paulo Sobreira");
		return clientPaddockPack;
	}

	@GET
	@Path("/carroCima")
	@Produces("image/png")
	public Response carroCima() throws IOException {

		BufferedImage carroLado = CarregadorRecursos
				.carregaBufferedImage("cima20092016/CarroCima.png");
		BufferedImage cor1 = CarregadorRecursos.gerarCoresCarros(
				Util.criarCorAleatoria(), "cima20092016/CarroCimaC1.png");
		BufferedImage cor2 = CarregadorRecursos.gerarCoresCarros(
				Util.criarCorAleatoria(), "cima20092016/CarroCimaC2.png");
		Graphics graphics = carroLado.getGraphics();
		graphics.drawImage(cor1, 0, 0, null);
		graphics.drawImage(cor2, 0, 0, null);
		graphics.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(carroLado, "png", baos);
		byte[] imageData = baos.toByteArray();

		return Response.ok(new ByteArrayInputStream(imageData)).build();

	}

	@GET
	@Path("/carro")
	@Produces("image/png")
	public Response carro() throws IOException {
		String img = "carros/t2016/mercedes.png";
		// String img = "normalAsa.png";
		BufferedImage originalImage = CarregadorRecursos
				.carregaBufferedImage(img);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "png", baos);
		byte[] imageData = baos.toByteArray();

		// uncomment line below to send non-streamed
		// return Response.ok(imageData).build();

		// uncomment line below to send streamed
		return Response.ok(new ByteArrayInputStream(imageData)).build();

	}

}
