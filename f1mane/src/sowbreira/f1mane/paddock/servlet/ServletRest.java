package sowbreira.f1mane.paddock.servlet;

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
	@Path("/carro")
	@Produces("image/png")
	public Response carro() throws IOException {
		String img = "carros/t2016/mercedes.png";
		// String img = "normalAsa.png";
		BufferedImage originalImage = CarregadorRecursos
				.carregaBufferedImage(img, false);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(originalImage, "png", baos);
		byte[] imageData = baos.toByteArray();

		// uncomment line below to send non-streamed
		// return Response.ok(imageData).build();

		// uncomment line below to send streamed
		return Response.ok(new ByteArrayInputStream(imageData)).build();

	}

}
