package sowbreira.f1mane.paddock.servlet;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;

@Path("/paddock")
public class ServletRest  {

	@GET
	@Path("/piloto")
	@Produces(MediaType.APPLICATION_JSON)
	public ClientPaddockPack showHelloWorld() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setNomeJogador("Paulo Sobreira");
		return clientPaddockPack;
	}

}