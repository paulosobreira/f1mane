package sowbreira.f1mane.paddock.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nnpe.Constantes;
import br.nnpe.Html;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.paddock.PaddockServer;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosParciais;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.PosisPack;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.servlet.ControlePaddockServidor;

@Path("/letsRace")
public class LetsRace {

	@GET
	@Path("/posicaoPilotos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response posicaoPilotos() {
		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		Object posis = controlePaddock.obterPosicaoPilotos("¢088¢ 0-2016");
		if (posis == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser iniciado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (posis instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) posis;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (posis instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) posis;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		PosisPack posisPack = new PosisPack();
		posisPack.decode((String) posis);
		return Response.status(200).entity(posisPack).build();
	}

	@GET
	@Path("/dadosPiloto")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosParciais() {

		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		Object dParciais = controlePaddock
				.obterDadosParciaisPilotos(new String[]{"¢088¢ 0-2016","Mane1","1"});

		if (dParciais == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser iniciado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (dParciais instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) dParciais;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (dParciais instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) dParciais;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		DadosParciais dadosParciais = new DadosParciais();
		dadosParciais.decode((String) dParciais);
		return Response.status(200).entity(dadosParciais).build();
	}

	@GET
	@Path("/dadosJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosJogo() {
		SessaoCliente sessaoCliente = new SessaoCliente();
		sessaoCliente.setNomeJogador("Sobreira");

		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		List<String> obterJogos = controlePaddock.obterJogos();
		clientPaddockPack.setNomeJogo(obterJogos.get(0));
		clientPaddockPack.setSessaoCliente(sessaoCliente);
		Object dadosJogo = controlePaddock.obterDadosJogo(clientPaddockPack);
		if (dadosJogo == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser iniciado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (dadosJogo instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) dadosJogo;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (dadosJogo instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) dadosJogo;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(200).entity(dadosJogo).build();
	}

	@GET
	@Path("/inciarJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response inciarJogo() {
		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		SessaoCliente sessaoCliente = new SessaoCliente();
		sessaoCliente.setNomeJogador("Sobreira");
		clientPaddockPack.setSessaoCliente(sessaoCliente);
		Object iniciarJogo = controlePaddock.iniciaJogo(clientPaddockPack);
		if (iniciarJogo == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser iniciado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (iniciarJogo instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) iniciarJogo;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (iniciarJogo instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) iniciarJogo;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		Integer ret = null;
		try {
			ret = new Integer(iniciarJogo.toString());	
		} catch (Exception e) {
			return Response.status(500)
					.entity(Html.escapeHtml(e.getMessage()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(ret).build();
	}

	@GET
	@Path("/criarJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarJogo() {
		ControlePaddockServidor controlePaddock = PaddockServer
				.getControlePaddock();
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		SessaoCliente sessaoCliente = new SessaoCliente();
		sessaoCliente.setNomeJogador("Sobreira");
		clientPaddockPack.setSessaoCliente(sessaoCliente);
		DadosCriarJogo dadosCriarJogo = gerarJogoLetsRace();
		clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
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

	private DadosCriarJogo gerarJogoLetsRace() {
		DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
		dadosCriarJogo.setTemporada("t2016");
		dadosCriarJogo.setQtdeVoltas(Constantes.MIN_VOLTAS);
		dadosCriarJogo.setDiffultrapassagem(250);
		Map<String, String> carregarCircuitos = ControleRecursos
				.carregarCircuitos();
		dadosCriarJogo.setCircuitoSelecionado(
				carregarCircuitos.keySet().iterator().next());
		dadosCriarJogo.setNivelCorrida(ControleJogoLocal.NORMAL);
		dadosCriarJogo.setClima(Clima.SOL);
		dadosCriarJogo.setReabastecimento(false);
		dadosCriarJogo.setTrocaPneu(true);
		dadosCriarJogo.setKers(true);
		dadosCriarJogo.setDrs(true);
		return dadosCriarJogo;
	}
}
