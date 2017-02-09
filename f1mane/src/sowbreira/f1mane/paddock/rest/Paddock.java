package sowbreira.f1mane.paddock.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nnpe.Html;
import sowbreira.f1mane.paddock.PaddockServer;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.servlet.ControlePaddockServidor;

@Path("/paddock")
public class Paddock {
	
	@POST
	@Path("/letsRace")
	@Produces(MediaType.APPLICATION_JSON)
	public Response letsRace() {
		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setNomeJogador("Paulo Sobreira");
		Object criarJogo = controlePaddock.criarJogo(clientPaddockPack);
		if (criarJogo == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser criado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (criarJogo instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) criarJogo;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (criarJogo instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) criarJogo;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		SrvPaddockPack srvPaddockPack = (SrvPaddockPack) criarJogo;
		return Response.status(200).entity(srvPaddockPack).build();
	}
}
