package sowbreira.f1mane.paddock.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.ws.rs.Encoded;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringEscapeUtils;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.CircuitosDefauts;
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
import sowbreira.f1mane.visao.PainelCircuito;

@Path("/letsRace")
public class LetsRace {

	private CarregadorRecursos carregadorRecursos = CarregadorRecursos
			.getCarregadorRecursos();
	private ControlePaddockServidor controlePaddock = PaddockServer
			.getControlePaddock();

	@GET
	@Path("/criarSessaoVisitante")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoVisitante() {
		return Response.status(200)
				.entity(controlePaddock.criarSessaoVisitante()).build();
	}

	@GET
	@Path("/dadosToken")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosToken(@HeaderParam("token") String token) {
		return Response.status(200)
				.entity(controlePaddock.obterDadosToken(token)).build();
	}

//	@GET
//	@Path("/criarSessaoGoogleTeste")
//	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoGoogle() {
		return Response.status(200)
				.entity(controlePaddock.criarSessaoGoogle("123",
						"Paulo Sobreira",
						"https://lh4.googleusercontent.com/-edNcQ95Ak5w/AAAAAAAAAAI/AAAAAAAABVE/4C3Yv5L5UDo/s96-c/photo.jpg",
						"sowbreira@gmail.com"))
				.build();
	}

	@GET
	@Path("/criarSessaoGoogle")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoGoogle(@HeaderParam("idGoogle") String idGoogle,
			@HeaderParam("nome") String nome,
			@HeaderParam("urlFoto") String urlFoto,
			@HeaderParam("email") String email) {
		return Response.status(200).entity(controlePaddock
				.criarSessaoGoogle(idGoogle, nome, urlFoto, email)).build();
	}

	@GET
	@Compress
	@Path("/circuito")
	@Produces(MediaType.APPLICATION_JSON)
	public Response circuito(@QueryParam("nomeCircuito") String nomeCircuito) {
		Logger.logar("String nomeCircuito " + nomeCircuito);
		Circuito circuito = null;
		try {
			circuito = CarregadorRecursos.carregarCircuito(nomeCircuito);
		} catch (ClassNotFoundException | IOException e) {
			Logger.logarExept(e);
			return Response.status(500).entity(e)
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (circuito == null) {
			return Response.status(400)
					.entity(StringEscapeUtils
							.escapeHtml4("Circuito não encontrado."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(200).entity(circuito).build();
	}

	@GET
	@Path("/sairJogo/{nomeJogo}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response sairJogo(@HeaderParam("token") String token,
			@PathParam("nomeJogo") String nomeJogo) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		controlePaddock.sairJogoToken(nomeJogo, token);
		return Response.status(200).build();
	}

	@GET
	@Compress
	@Path("/dadosParciais/{nomeJogo}/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosParciais(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@PathParam("nomeJogo") String nomeJogo,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		DadosParciais dadosParciais = controlePaddock.obterDadosParciaisPilotos(
				nomeJogo, sessaoCliente.getToken(), idPiloto);
		dadosParciais.texto = Lang.decodeTextoKey(dadosParciais.texto, idioma);
		return Response.status(200).entity(dadosParciais).build();
	}

	@GET
	@Compress
	@Path("/dadosJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosJogo(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@QueryParam("nomeJogo") String nomeJogo) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
		clientPaddockPack.setNomeJogo(nomeJogo);
		clientPaddockPack.setSessaoCliente(sessaoCliente);
		Object dadosJogoObj = controlePaddock.obterDadosJogo(clientPaddockPack);
		DadosJogo dadosJogo = (DadosJogo) dadosJogoObj;
		if (dadosJogoObj == null || !(dadosJogoObj instanceof DadosJogo)) {
			if (!controlePaddock.obterJogos().isEmpty()) {
				String jogo = controlePaddock.obterJogos().get(0);
				clientPaddockPack = new ClientPaddockPack();
				clientPaddockPack.setNomeJogo(jogo);
				clientPaddockPack.setSessaoCliente(sessaoCliente);
				dadosJogoObj = controlePaddock
						.obterDadosJogo(clientPaddockPack);
			}
			if (dadosJogoObj == null) {
				DadosJogo nenhum = new DadosJogo();
				nenhum.setEstado("NENHUM");
				dadosJogoObj = nenhum;
			}
			dadosJogo = (DadosJogo) dadosJogoObj;
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
	@Path("/verificaServico")
	@Produces(MediaType.APPLICATION_JSON)
	public Response verificaServico() {
		return Response.status(200).entity("ok").build();
	}

	@GET
	@Compress
	@Path("/jogar/{temporada}/{idPiloto}/{circuito}/{numVoltas}/{tipoPneu}/{combustivel}/{asa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response jogar(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@PathParam("temporada") String temporada,
			@PathParam("idPiloto") String idPiloto,
			@PathParam("circuito") String circuito,
			@PathParam("numVoltas") String numVoltas,
			@PathParam("tipoPneu") String tipoPneu,
			@PathParam("combustivel") String combustivel,
			@PathParam("asa") String asa) {
		try {
			SessaoCliente sessaoCliente = controlePaddock
					.obterSessaoPorToken(token);
			if (sessaoCliente == null) {
				return Response.status(401).build();
			}
			sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setSessaoCliente(sessaoCliente);
			DadosCriarJogo dadosCriarJogo = gerarJogoLetsRace(temporada,
					circuito, idPiloto, numVoltas, tipoPneu, combustivel, asa);
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
				Response erro = processsaMensagem(statusJogo, idioma);
				if (erro != null) {
					return erro;
				}
			}

			srvPaddockPack = (SrvPaddockPack) statusJogo;

			if (srvPaddockPack != null
					&& srvPaddockPack.getDadosCriarJogo() != null
					&& (!srvPaddockPack.getDadosCriarJogo().getTemporada()
							.equals(dadosCriarJogo.getTemporada())
							|| !dadosCriarJogo.getCircuitoSelecionado()
									.equals(srvPaddockPack.getDadosCriarJogo()
											.getCircuitoSelecionado()))) {
				return Response.status(500)
						.entity(new MsgSrv(
								Lang.msgKey("existeJogoEmAndamando", idioma)))
						.type(MediaType.APPLICATION_JSON).build();
			}

			/**
			 * Preenchento todos possiveis campos para nome do jogo Bagunça...
			 */
			clientPaddockPack.setDadosCriarJogo(dadosCriarJogo);
			clientPaddockPack.getDadosJogoCriado()
					.setNomeJogo(srvPaddockPack.getNomeJogoCriado());
			clientPaddockPack.setNomeJogo(srvPaddockPack.getNomeJogoCriado());

			/**
			 * Entrar Jogo
			 */
			if (statusJogo != null) {
				statusJogo = controlePaddock.entrarJogo(clientPaddockPack);
				Response erro = processsaMensagem(statusJogo, idioma);
				if (erro != null) {
					return erro;
				}
				controlePaddock.atualizarDadosVisao();
			}
			DadosJogo dadosJogo = (DadosJogo) controlePaddock
					.obterDadosJogo(clientPaddockPack);
			return Response.status(200).entity(dadosJogo).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	private Response processsaMensagem(Object objeto, String idioma) {
		if (objeto == null) {
			return Response.status(500).entity(new MsgSrv("Server error."))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (objeto instanceof MsgSrv) {
			MsgSrv msgSrv = (MsgSrv) objeto;
			return Response.status(400)
					.entity(new MsgSrv(Lang
							.decodeTextoKey(msgSrv.getMessageString(), idioma)))
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (objeto instanceof ErroServ) {
			ErroServ erroServ = (ErroServ) objeto;
			return Response.status(500)
					.entity(new MsgSrv(erroServ.obterErroFormatado()))
					.type(MediaType.APPLICATION_JSON).build();
		}
		return null;
	}

	@GET
	@Path("/circuitos")
	@Produces(MediaType.APPLICATION_JSON)
	public Response circuitos() {
		List<CircuitosDefauts> circuitosDefauts;
		try {
			circuitosDefauts = carregadorRecursos.carregarCircuitosDefaults();
			List<CircuitosDefauts> shuffle = new ArrayList<>();
			shuffle.addAll(circuitosDefauts);
			Collections.shuffle(shuffle);
			return Response.status(200).entity(shuffle).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}

	}

	@GET
	@Path("/circuitoBg/{nmCircuito}")
	@Produces("image/jpg")
	public Response circuitoBg(@PathParam("nmCircuito") String nmCircuito) {
		try {
			nmCircuito = nmCircuito.replace("jpg", "f1mane");
			Object rec = carregadorRecursos.carregarRecurso(nmCircuito);
			Circuito circuito = (Circuito) rec;
			circuito.vetorizarPista();
			InterfaceJogo jogo = null;
			if (controlePaddock.obterJogos() != null
					&& !controlePaddock.obterJogos().isEmpty()) {
				jogo = controlePaddock
						.obterJogoPeloNome(controlePaddock.obterJogos().get(0));
			}

			PainelCircuito painelCircuito = new PainelCircuito(circuito, jogo);
			BufferedImage bg = painelCircuito.desenhaCircuito();
			if (bg == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(bg, "jpg", baos);
			byte[] imageData = baos.toByteArray();
			return Response.status(200).entity(imageData).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/circuitoMini/{nmCircuito}")
	@Produces("image/png")
	public Response circuitoMini(@PathParam("nmCircuito") String nmCircuito) {
		try {
			Object rec = carregadorRecursos.carregarRecurso(nmCircuito);
			Circuito circuito = (Circuito) rec;
			BufferedImage mini = circuito.desenhaMiniCircuito();
			if (mini == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(mini, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.status(200).entity(imageData).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/objetoPista/{nmCircuito}/{indice}")
	@Produces("image/png")
	public Response objetoPista(@PathParam("nmCircuito") String nmCircuito,
			@PathParam("indice") String indice) {
		try {
			Object rec = carregadorRecursos.carregarRecurso(nmCircuito);
			Circuito circuito = (Circuito) rec;
			BufferedImage carroCima = circuito.desenhaObjetoPista(indice);
			if (carroCima == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(carroCima, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.status(200).entity(imageData).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	private DadosCriarJogo gerarJogoLetsRace(String temporada, String circuito,
			String idPiloto, String numVoltas, String tipoPneu,
			String combustivel, String asa)
			throws ClassNotFoundException, IOException {
		numVoltas = "1";
		DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
		dadosCriarJogo.setTemporada("t" + temporada);
		dadosCriarJogo.setQtdeVoltas(Constantes.MIN_VOLTAS);
		dadosCriarJogo.setDiffultrapassagem(250);
		Map<String, String> carregarCircuitos = ControleRecursos
				.carregarCircuitos();
		String pista = "";
		for (Iterator iterator = carregarCircuitos.keySet().iterator(); iterator
				.hasNext();) {
			String nmCircuito = (String) iterator.next();
			if (carregarCircuitos.get(nmCircuito).equals(circuito)) {
				pista = nmCircuito;
			}
		}
		// pista = "Monte Carlo";
		// dadosCriarJogo.setSafetyCar(false);
		dadosCriarJogo.setCircuitoSelecionado(pista);
		dadosCriarJogo.setNivelCorrida(ControleJogoLocal.NORMAL);
		dadosCriarJogo.setAsa(asa);
		dadosCriarJogo.setTpPnueu(tipoPneu);
		if (!Util.isNullOrEmpty(numVoltas)) {
			dadosCriarJogo
					.setQtdeVoltas(new Integer(Util.extrairNumeros(numVoltas)));
		}
		Circuito circuitoObj = CarregadorRecursos.carregarCircuito(circuito);
		if (Math.random() < (circuitoObj.getProbalidadeChuva() / 100.0)) {
			dadosCriarJogo.setClima(Clima.NUBLADO);
		} else {
			dadosCriarJogo.setClima(Clima.SOL);
		}
		TemporadasDefauts temporadasDefauts = carregadorRecursos
				.carregarTemporadasPilotosDefauts().get("t" + temporada);

		if (!Util.isNullOrEmpty(combustivel)) {
			Integer fuel = new Integer(Util.extrairNumeros(combustivel));
			if (fuel > 100) {
				fuel = 100;
			}
			if (fuel < 10) {
				fuel = 10;
			}
			dadosCriarJogo.setCombustivel(fuel);
		} else {
			if (temporadasDefauts.getReabastecimento()) {
				dadosCriarJogo.setCombustivel(Util.intervalo(25, 50));
			} else {
				dadosCriarJogo.setCombustivel(Util.intervalo(70, 90));
			}
		}
		dadosCriarJogo
				.setReabastecimento(temporadasDefauts.getReabastecimento());
		dadosCriarJogo.setTrocaPneu(temporadasDefauts.getTrocaPneu());
		dadosCriarJogo.setErs(temporadasDefauts.getErs());
		dadosCriarJogo.setDrs(temporadasDefauts.getDrs());
		dadosCriarJogo.setIdPiloto(new Integer(idPiloto));

		// dadosCriarJogo.setClima(Clima.CHUVA);
		// dadosCriarJogo.setTpPnueu(Carro.TIPO_PNEU_CHUVA);
		return dadosCriarJogo;
	}

	@GET
	@Path("/carroCima")
	@Produces("image/png")
	public Response carroCima(@QueryParam("nomeJogo") String nomeJogo,
			@QueryParam("idPiloto") String idPiloto) {
		try {
			BufferedImage carroCima = controlePaddock.obterCarroCima(nomeJogo,
					idPiloto);
			if (carroCima == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(carroCima, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.ok(new ByteArrayInputStream(imageData)).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/carroCimaSemAreofolio")
	@Produces("image/png")
	public Response carroCimaSemAreofolio(
			@QueryParam("nomeJogo") String nomeJogo,
			@QueryParam("idPiloto") String idPiloto) {
		try {
			BufferedImage carroCima = controlePaddock
					.obterCarroCimaSemAreofolio(nomeJogo, idPiloto);
			if (carroCima == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(carroCima, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.ok(new ByteArrayInputStream(imageData)).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/capacete")
	@Produces("image/png")
	public Response capacete(@QueryParam("id") String id,
			@QueryParam("temporada") String temporada) {
		try {
			temporada = "t" + temporada;
			BufferedImage capacetes = null;
			List<Piloto> list = carregadorRecursos.carregarTemporadasPilotos()
					.get(temporada);
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (Integer.parseInt(id) == piloto.getId()) {
					capacetes = carregadorRecursos.obterCapacete(piloto,
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
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/carroLado")
	@Produces("image/png")
	public Response carroLado(@QueryParam("id") String id,
			@QueryParam("temporada") String temporada) {
		try {
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
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/png/{recurso}")
	@Produces("image/png")
	public Response png(@PathParam("recurso") String recurso) {
		try {
			BufferedImage buffer = CarregadorRecursos
					.carregaBufferedImage(recurso + ".png");
			if (buffer == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(buffer, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.ok(new ByteArrayInputStream(imageData)).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

	@GET
	@Path("/png/{recurso}/{trasnparencia}")
	@Produces("image/png")
	public Response png(@PathParam("recurso") String recurso,
			@PathParam("trasnparencia") String trasnparencia) {
		try {
			BufferedImage buffer = ImageUtil.geraTransparenciaAlpha(
					CarregadorRecursos.carregaBufferedImage(recurso + ".png"),
					new Integer(trasnparencia));
			if (buffer == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(buffer, "png", baos);
			byte[] imageData = baos.toByteArray();
			return Response.ok(new ByteArrayInputStream(imageData)).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
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

	@SuppressWarnings("static-access")
	@GET
	@Compress
	@Path("/temporadas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadas() {
		return Response.status(200)
				.entity(carregadorRecursos.carregarTemporadas()).build();
	}

	@GET
	@Compress
	@Path("/sobre")
	@Produces(MediaType.TEXT_HTML + ";charset=UTF-8")
	public Response sobre() {
		List<String> carregarCreditosJogo = CarregadorRecursos
				.carregarCreditosJogo();
		StringBuilder buffer = new StringBuilder();
		for (Iterator<String> iterator = carregarCreditosJogo
				.iterator(); iterator.hasNext();) {
			String string = iterator.next();
			buffer.append("<br>");
			buffer.append(string);
		}
		return Response.status(200).entity(buffer.toString()).build();
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
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
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
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();;
		return Response.status(200)
				.entity(controleJogosServer.mudarAgressividadePiloto(
						sessaoCliente, idPiloto, agresividade))
				.build();
	}

	@GET
	@Compress
	@Path("/tracadoPiloto/{tracado}/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response tracadoPiloto(@HeaderParam("token") String token,
			@PathParam("tracado") String tracado,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		return Response
				.status(200).entity(controleJogosServer
						.mudarTracadoPiloto(sessaoCliente, idPiloto, tracado))
				.build();
	}

	@GET
	@Compress
	@Path("/drsPiloto/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response drsPiloto(@HeaderParam("token") String token,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		return Response.status(200)
				.entity(controleJogosServer.mudarDrs(sessaoCliente, idPiloto))
				.build();
	}

	@GET
	@Compress
	@Path("/ersPiloto/{idPiloto}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response ersPiloto(@HeaderParam("token") String token,
			@PathParam("idPiloto") String idPiloto) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		return Response.status(200)
				.entity(controleJogosServer.mudarErs(sessaoCliente, idPiloto))
				.build();
	}

	@GET
	@Compress
	@Path("/boxPiloto/{idPiloto}/{ativa}/{pneu}/{combustivel}/{asa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response boxPiloto(@HeaderParam("token") String token,
			@PathParam("idPiloto") String idPiloto,
			@PathParam("ativa") Boolean ativa, @PathParam("pneu") String pneu,
			@PathParam("combustivel") Integer combustivel,
			@PathParam("asa") String asa) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();;
		return Response.status(200)
				.entity(controleJogosServer.boxPiloto(sessaoCliente, idPiloto,
						ativa, pneu, combustivel, asa))
				.build();
	}

	@GET
	@Compress
	@Path("/lang/{lang}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lang(@PathParam("lang") String lang) {
		try {
			PropertyResourceBundle bundle = Lang.carregraBundleMensagens(lang);
			Set<String> keySet = bundle.keySet();
			LinkedList<String> values = new LinkedList<String>();
			LinkedList<String> keys = new LinkedList<String>();
			for (Iterator iterator = keySet.iterator(); iterator.hasNext();) {
				String key = (String) iterator.next();
				values.add(bundle.getString(key));
				keys.add(key);
			}
			Map<String, LinkedList> retorno = new HashMap<String, LinkedList>();
			retorno.put("keys", keys);
			retorno.put("values", values);
			return Response.status(200).entity(retorno).build();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
	}

}
