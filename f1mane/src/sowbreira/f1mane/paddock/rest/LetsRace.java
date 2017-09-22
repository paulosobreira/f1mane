package sowbreira.f1mane.paddock.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.TemporadasDefauts;
import sowbreira.f1mane.paddock.PaddockServer;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosParciais;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.servlet.ControleJogosServer;
import sowbreira.f1mane.paddock.servlet.ControlePaddockServidor;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

@Path("/letsRace")
public class LetsRace {

	private CarregadorRecursos carregadorRecursos = CarregadorRecursos
			.getCarregadorRecursos();
	private ControlePaddockServidor controlePaddock = PaddockServer
			.getControlePaddock();

	@GET
	@Compress
	@Path("/circuito")
	@Produces(MediaType.APPLICATION_JSON)
	public Response circuito(@QueryParam("nomeJogo") String nomeJogo) {
		Logger.logar("String nomeJogo " + nomeJogo);
		Object circuito = controlePaddock.obterCircuito(nomeJogo);
		if (circuito == null) {
			return Response.status(400)
					.entity(Html.escapeHtml("Jogo não pode ser iniciado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (circuito instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) circuito;
			return Response.status(400)
					.entity(Html.escapeHtml(msgSrv.getMessageString()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (circuito instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) circuito;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		Circuito circuitoJogo = (Circuito) circuito;
		return Response.status(200).entity(circuitoJogo).build();
	}

	@GET
	@Compress
	@Path("/dadosParciais/{nomeJogo}/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosParciais(@HeaderParam("token") String token,
			@PathParam("nomeJogo") String nomeJogo,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		Object dParciais = controlePaddock
				.obterDadosParciaisPilotos(new String[]{nomeJogo,
						sessaoCliente.getNomeJogador(), idPiloto});
		Response erro = processsaMensagem(dParciais);
		if (erro != null) {
			return erro;
		}
		DadosParciais dadosParciais = new DadosParciais();
		dadosParciais.decode((String) dParciais);
		return Response.status(200).entity(dadosParciais).build();
	}

	@GET
	@Compress
	@Path("/dadosJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosJogo(@QueryParam("nomeJogo") String nomeJogo) {
		SessaoCliente sessaoCliente = new SessaoCliente();
		sessaoCliente.setNomeJogador("Sobreira");
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setNomeJogo(nomeJogo);
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
	@Path("/obterJogos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response obterJogos() {
		return Response.status(200).entity(controlePaddock.obterJogos())
				.build();
	}

	@GET
	@Path("/atualizarDadosVisao")
	@Produces(MediaType.APPLICATION_JSON)
	public Response atualizarDadosVisao() {
		return Response.status(200)
				.entity(controlePaddock.atualizarDadosVisao()).build();
	}

	@GET
	@Path("/criarSessaoVisitante")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoVisitante() {
		return Response.status(200)
				.entity(controlePaddock.criarSessaoVisitante()).build();
	}

	@GET
	@Compress
	@Path("/criarJogo/{temporada}/{idPiloto}/{circuito}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarJogo(@HeaderParam("token") String token,
			@PathParam("temporada") String temporada,
			@PathParam("idPiloto") String idPiloto,
			@PathParam("circuito") String circuito) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setSessaoCliente(sessaoCliente);
		DadosCriarJogo dadosCriarJogo = gerarJogoLetsRace(temporada, circuito,
				idPiloto);
		clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
		List<String> obterJogos = controlePaddock.obterJogos();
		if (!obterJogos.isEmpty()) {
			clientPaddockPack.setNomeJogo(obterJogos.get(0));
		}
		SrvPaddockPack srvPaddockPack = null;
		Object statusJogo = null;
		statusJogo = controlePaddock.obterJogoPeloNome(clientPaddockPack);
		/**
		 * Criar Jogo
		 */
		if (statusJogo == null) {
			statusJogo = controlePaddock.criarJogo(clientPaddockPack);
			Response erro = processsaMensagem(statusJogo);
			if (erro != null) {
				return erro;
			}
		} else {
			DadosJogo dadosJogo = (DadosJogo) controlePaddock
					.obterDadosJogo(clientPaddockPack);
			List<Piloto> pilotosList = dadosJogo.getPilotosList();
			for (Iterator iterator = pilotosList.iterator(); iterator
					.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.isJogadorHumano()
						&& piloto.getId() == dadosCriarJogo.getIdPiloto()) {
					if (sessaoCliente.getNomeJogador()
							.equals(piloto.getNomeJogador())) {
						return Response.status(200).entity(dadosJogo).build();
					} else {
						return Response.status(400).entity(dadosJogo).build();
					}
				}
			}
		}
		srvPaddockPack = (SrvPaddockPack) statusJogo;

		/**
		 * Preenchento todos possiveis campos para nome do jogo Bagunça...
		 */
		clientPaddockPack.setDadosCriarJogo(srvPaddockPack.getDadosCriarJogo());
		clientPaddockPack.getDadosJogoCriado()
				.setNomeJogo(srvPaddockPack.getNomeJogoCriado());
		clientPaddockPack.setNomeJogo(srvPaddockPack.getNomeJogoCriado());

		/**
		 * Entrar Jogo
		 */
		if (statusJogo != null) {
			statusJogo = controlePaddock.entrarJogo(clientPaddockPack);
			Response erro = processsaMensagem(statusJogo);
			if (erro != null) {
				return erro;
			}
		}
		/**
		 * Iniciar Jogo
		 */
		if (statusJogo != null) {
			statusJogo = controlePaddock.iniciaJogo(clientPaddockPack);
			Response erro = processsaMensagem(statusJogo);
			if (erro != null) {
				return erro;
			}
		}
		return Response.status(200).entity(statusJogo).build();
	}

	private Response processsaMensagem(Object objeto) {
		if (objeto == null) {
			return Response.status(400).entity(Html.escapeHtml("Objeto Nulo."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (objeto instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) objeto;
			return Response.status(400)
					.entity(Html.escapeHtml(
							Lang.decodeTextoKey(msgSrv.getMessageString())))
					.type(MediaType.TEXT_HTML).build();
		}
		if (objeto instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) objeto;
			return Response.status(500)
					.entity(Html.escapeHtml(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return null;
	}

	@GET
	@Path("/circuitos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response circuitos() {
		Map<String, String> carregarCircuitos = ControleRecursos
				.carregarCircuitos();
		return Response.status(200).entity(carregarCircuitos).build();
	}

	@GET
	@Path("/circuitoMini/{nmCircuito}")
	@Produces("image/png")
	public Response circuitoMini(@PathParam("nmCircuito") String nmCircuito)
			throws IOException, ClassNotFoundException {
		Object rec = carregadorRecursos.carregarRecurso(nmCircuito);
		Circuito circuito = (Circuito) rec;
		BufferedImage carroCima = circuito.desenhaMiniCircuito();
		if (carroCima == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(carroCima, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.status(200).entity(imageData).build();
	}

	private DadosCriarJogo gerarJogoLetsRace(String temporada, String circuito,
			String idPiloto) {
		DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
		dadosCriarJogo.setTemporada("t" + temporada);
		dadosCriarJogo.setQtdeVoltas(Constantes.MIN_VOLTAS);
		dadosCriarJogo.setDiffultrapassagem(250);
		Map<String, String> carregarCircuitos = ControleRecursos
				.carregarCircuitos();
		String pista = "Interlagos";
		for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
				.hasNext();) {
			String nmCircuito = (String) iterator.next();
			if (carregarCircuitos.get(nmCircuito).equals(circuito)) {
				pista = nmCircuito;
			}
		}
		dadosCriarJogo.setCircuitoSelecionado(pista);
		dadosCriarJogo.setNivelCorrida(ControleJogoLocal.NORMAL);
		dadosCriarJogo.setClima(Clima.SOL);
		TemporadasDefauts temporadasDefauts = carregadorRecursos
				.carregarTemporadasPilotosDefauts().get("t" + temporada);
		dadosCriarJogo
				.setReabastecimento(temporadasDefauts.getReabastecimento());
		dadosCriarJogo.setTrocaPneu(temporadasDefauts.getTrocaPneu());
		dadosCriarJogo.setErs(temporadasDefauts.getErs());
		dadosCriarJogo.setDrs(temporadasDefauts.getDrs());
		dadosCriarJogo.setIdPiloto(new Integer(idPiloto));
		if (temporadasDefauts.getReabastecimento()) {
			dadosCriarJogo.setCombustivel(50);
		} else {
			dadosCriarJogo.setCombustivel(85);
		}
		dadosCriarJogo.setTpPnueu(Carro.TIPO_PNEU_MOLE);
		return dadosCriarJogo;
	}

	@GET
	@Path("/carroCima")
	@Produces("image/png")
	public Response carroCima(@QueryParam("nomeJogo") String nomeJogo,
			@QueryParam("idPiloto") String idPiloto) throws IOException {
		BufferedImage carroCima = controlePaddock.obterCarroCima(nomeJogo,
				idPiloto);
		if (carroCima == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(carroCima, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/capacete")
	@Produces("image/png")
	public Response capacete(@QueryParam("id") String id,
			@QueryParam("temporada") String temporada) throws IOException {
		temporada = "t" + temporada;
		BufferedImage capacetes = null;
		List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos()
				.get(temporada);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (Integer.parseInt(id) == piloto.getId()) {
				capacetes = carregadorRecursos
						.obterCapacete(piloto.getNomeOriginal(), temporada);
				break;
			}
		}
		if (capacetes == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(capacetes, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/carroLado")
	@Produces("image/png")
	public Response carroLado(@QueryParam("id") String id,
			@QueryParam("temporada") String temporada) throws IOException {
		temporada = "t" + temporada;
		BufferedImage capacetes = null;
		List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos()
				.get(temporada);
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (Integer.parseInt(id) == piloto.getId()) {
				capacetes = carregadorRecursos.obterCarroLado(piloto,
						temporada);
				break;
			}
		}
		if (capacetes == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(capacetes, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/setaCima")
	@Produces("image/png")
	public Response setaCima() throws IOException {
		BufferedImage setaCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		if (setaCima == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(setaCima, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/setaBaixo")
	@Produces("image/png")
	public Response setaBaixo() throws IOException {
		BufferedImage setaBaixo = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroBaixo.png",
						200);
		if (setaBaixo == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(setaBaixo, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/setaEsquerda")
	@Produces("image/png")
	public Response setaEsquerda() throws IOException {
		BufferedImage setaCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		BufferedImage setaEsquerda = ImageUtil.rotacionar(setaCima, 270);
		if (setaEsquerda == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(setaEsquerda, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/setaDireita")
	@Produces("image/png")
	public Response setaDireita() throws IOException {
		BufferedImage setaCima = CarregadorRecursos
				.carregaBufferedImageTranspareciaBranca("SetaCarroCima.png",
						200);
		BufferedImage setaDireita = ImageUtil.rotacionar(setaCima, 90);
		if (setaDireita == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(setaDireita, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Path("/png/{recurso}")
	@Produces("image/png")
	public Response png(@PathParam("recurso") String recurso)
			throws IOException {
		BufferedImage buffer = CarregadorRecursos
				.carregaBufferedImage(recurso + ".png");
		if (buffer == null) {
			return Response.status(200).entity("null").build();
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(buffer, "png", baos);
		byte[] imageData = baos.toByteArray();
		return Response.ok(new ByteArrayInputStream(imageData)).build();
	}

	@GET
	@Compress
	@Path("/temporadasPilotos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadasPilotos() {
		return Response.status(200)
				.entity(carregadorRecursos.carregarTemporadasPilotos()).build();
	}

	@GET
	@Compress
	@Path("/temporadas/{temporada}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadasDefaults(
			@PathParam("temporada") String temporada) {
		temporada = "t" + temporada;
		Map<String, TemporadasDefauts> carregarTemporadasPilotosDefauts = carregadorRecursos
				.carregarTemporadasPilotosDefauts();
		return Response.status(200)
				.entity(carregarTemporadasPilotosDefauts.get(temporada))
				.build();
	}

	@GET
	@Compress
	@Path("/temporadas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadas() {
		return Response.status(200)
				.entity(carregadorRecursos.carregarTemporadas()).build();
	}

	/**
	 * potencia : GIRO_MIN , GIRO_NOR , GIRO_MAX
	 */
	@GET
	@Compress
	@Path("/potenciaMotor/{potencia}/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response potenciaMotor(@HeaderParam("token") String token,
			@PathParam("potencia") String potencia,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();;
		return Response
				.status(200).entity(controleJogosServer
						.mudarGiroMotor(sessaoCliente, idPiloto, potencia))
				.build();
	}

	/**
	 * agresividade : LENTO , NORMAL , AGRESSIVO
	 */
	@GET
	@Compress
	@Path("/agressividadePiloto/{agresividade}/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response agressividadePiloto(@HeaderParam("token") String token,
			@PathParam("agresividade") String agresividade,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();;
		return Response
				.status(200).entity(controleJogosServer
						.mudarGiroMotor(sessaoCliente, idPiloto, agresividade))
				.build();
	}
}
