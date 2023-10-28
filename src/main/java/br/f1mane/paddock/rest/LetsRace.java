package br.f1mane.paddock.rest;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.f1mane.controles.ControleRecursos;
import br.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.CircuitosDefauts;
import sowbreira.f1mane.entidades.TemporadasDefauts;
import br.f1mane.paddock.PaddockServer;
import br.f1mane.paddock.entidades.TOs.CampeonatoTO;
import br.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import br.f1mane.paddock.entidades.TOs.DadosJogo;
import br.f1mane.paddock.entidades.TOs.DadosParciais;
import br.f1mane.paddock.entidades.TOs.ErroServ;
import br.f1mane.paddock.entidades.TOs.MsgSrv;
import br.f1mane.paddock.entidades.TOs.SessaoCliente;
import br.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import br.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import br.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import br.f1mane.paddock.servlet.ControleJogosServer;
import br.f1mane.paddock.servlet.ControlePaddockServidor;
import br.f1mane.recursos.CarregadorRecursos;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.PainelCircuito;

@Path("/letsRace")
public class LetsRace {

	private CarregadorRecursos carregadorRecursos = CarregadorRecursos
			.getCarregadorRecursos(false);
	private ControlePaddockServidor controlePaddock = PaddockServer
			.getControlePaddock();

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
	@Path("/criarSessaoVisitante")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoVisitante() {
		return Response.status(200)
				.entity(controlePaddock.criarSessaoVisitante()).build();
	}

	@GET
	@Path("/renovarSessaoVisitante/{token}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response renovarSessaoVisitante(@PathParam("token") String token) {
		return Response.status(200)
				.entity(controlePaddock.renovarSessaoVisitante(token)).build();
	}

	@GET
	@Path("/dadosToken")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosToken(@HeaderParam("token") String token) {
		SrvPaddockPack obterDadosToken = controlePaddock.obterDadosToken(token);
		if (obterDadosToken != null
				&& obterDadosToken.getSessaoCliente() != null) {
			obterDadosToken.getSessaoCliente()
					.setUlimaAtividade(System.currentTimeMillis());
			return Response.status(200).entity(obterDadosToken).build();
		}
		return Response.status(404).build();
	}

	@GET
	@Path("/criarSessaoGoogleTeste/{idGoogle}/{nome}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response criarSessaoGoogle(@PathParam("idGoogle") String idGoogle,
			@PathParam("nome") String nome) {
		if (!Logger.ativo) {
			return Response.status(400).type(MediaType.APPLICATION_JSON)
					.build();
		}
		return Response.status(200)
				.entity(controlePaddock.criarSessaoGoogle(idGoogle, nome,
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
		Object criarSessaoGoogle = controlePaddock.criarSessaoGoogle(idGoogle,
				nome, urlFoto, email);
		if (criarSessaoGoogle instanceof ErroServ) {
			return Response.status(500).entity(criarSessaoGoogle).build();
		} else {
			return Response.status(200).entity(criarSessaoGoogle).build();
		}
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
			if (circuito != null) {
				circuito.vetorizarPista();
				circuito.gerarObjetosNoTransparencia();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return Response.status(500).entity(erroServ)
					.type(MediaType.APPLICATION_JSON).build();
		}
		if (circuito == null) {
			return Response.status(404).build();
		}
		return Response.status(200).entity(circuito).build();
	}

	@GET
	@Compress
	@Path("/circuitoClassificacao/{arquivoCircuito}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response circuitoClassificacao(
			@PathParam("arquivoCircuito") String arquivoCircuito) {
		return Response.status(200)
				.entity(controlePaddock
						.obterClassificacaoCircuito(ControleRecursos
								.nomeArquivoCircuitoParaPista(arquivoCircuito)))
				.build();
	}

	@GET
	@Compress
	@Path("/temporadaClassificacao/{temporadaSelecionada}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadaClassificacao(
			@PathParam("temporadaSelecionada") String temporadaSelecionada) {
		return Response.status(200)
				.entity(controlePaddock
						.obterClassificacaoTemporada(temporadaSelecionada))
				.build();
	}

	@GET
	@Compress
	@Path("/classificacaoGeral")
	@Produces(MediaType.APPLICATION_JSON)
	public Response classificacaoGeral() {
		return Response.status(200)
				.entity(controlePaddock.obterClassificacaoGeral()).build();
	}

	@GET
	@Compress
	@Path("/classificacaoEquipes")
	@Produces(MediaType.APPLICATION_JSON)
	public Response classificacaoEquipes() {
		return Response.status(200)
				.entity(controlePaddock.obterClassificacaoEquipes()).build();
	}

	@GET
	@Compress
	@Path("/classificacaoCampeonato")
	@Produces(MediaType.APPLICATION_JSON)
	public Response classificacaoCampeonato() {
		return Response.status(200)
				.entity(controlePaddock.obterClassificacaoCampeonato()).build();
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
		controlePaddock.sairJogoToken(nomeJogo, token, sessaoCliente);
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
		if (dadosParciais == null) {
			return Response.status(401).build();
		}
		try {
			dadosParciais.texto = Lang.decodeTextoKey(dadosParciais.texto,
					idioma);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Response.status(200).entity(dadosParciais).build();
	}

	@GET
	@Compress
	@Path("/dadosJogo")
	@Produces(MediaType.APPLICATION_JSON)
	public Response dadosJogo(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@QueryParam("nomeJogo") String nomeJogo,
			@QueryParam("modoCarreira") String modoCarreira) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		DadosJogo dadosJogo = null;
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack();
			clientPaddockPack.setNomeJogo(nomeJogo);
			clientPaddockPack.setSessaoCliente(sessaoCliente);
			Object dadosJogoObj = controlePaddock
					.obterDadosJogo(clientPaddockPack);
			dadosJogo = (DadosJogo) dadosJogoObj;
			if (dadosJogoObj == null || !(dadosJogoObj instanceof DadosJogo)) {
				if (!controlePaddock.obterJogos().isEmpty()) {
					String jogo = controlePaddock.obterJogos().get(0);
					clientPaddockPack = new ClientPaddockPack();
					clientPaddockPack.setNomeJogo(jogo);
					clientPaddockPack.setSessaoCliente(sessaoCliente);
					dadosJogoObj = controlePaddock
							.obterDadosJogo(clientPaddockPack);
					if ("true".equals(modoCarreira) && controlePaddock
							.obterJogoPeloNome(jogo).isCorridaIniciada()) {
						dadosJogoObj = null;
					}
				}
				if (dadosJogoObj == null) {
					DadosJogo nenhum = new DadosJogo();
					nenhum.setEstado("NENHUM");
					dadosJogoObj = nenhum;
				}
				dadosJogo = (DadosJogo) dadosJogoObj;
			}
		} catch (Exception e) {
			Logger.logarExept(e);
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
	@Path("/jogar/{temporada}/{idPiloto}/{circuito}/{numVoltas}/{tipoPneu}/{combustivel}/{asa}/{modoCarreira}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response jogar(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@PathParam("temporada") String temporada,
			@PathParam("idPiloto") String idPiloto,
			@PathParam("circuito") String circuito,
			@PathParam("numVoltas") String numVoltas,
			@PathParam("tipoPneu") String tipoPneu,
			@PathParam("combustivel") String combustivel,
			@PathParam("asa") String asa,
			@PathParam("modoCarreira") String modoCarreira) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		Object jogar = controlePaddock.jogar(temporada, circuito, idPiloto,
				numVoltas, tipoPneu, combustivel, asa, sessaoCliente,
				modoCarreira);
		Response processsaMensagem = processsaMensagem(jogar, idioma);
		if (processsaMensagem != null) {
			return processsaMensagem;
		} else {
			return Response.status(200).entity(jogar).build();
		}
	}

	@GET
	@Compress
	@Path("/jogarCampeonato/{tipoPneu}/{combustivel}/{asa}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response jogarCampeonato(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma,
			@PathParam("tipoPneu") String tipoPneu,
			@PathParam("combustivel") String combustivel,
			@PathParam("asa") String asa) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		Object jogar = controlePaddock.jogarCampeonato(tipoPneu, combustivel,
				asa, sessaoCliente);
		Response processsaMensagem = processsaMensagem(jogar, idioma);
		if (processsaMensagem != null) {
			return processsaMensagem;
		} else {
			return Response.status(200).entity(jogar).build();
		}
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

	@GET
	@Path("/carroCima/{temporada}/{carro}")
	@Produces("image/png")
	public Response carroCimaTemporadaCarro(
			@PathParam("temporada") String temporada,
			@PathParam("carro") String carro) {
		try {
			BufferedImage carroCima = controlePaddock
					.carroCimaTemporadaCarro(temporada, carro);
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
	@Path("/carroCimaSemAreofolio/{temporada}/{carro}")
	@Produces("image/png")
	public Response carroCimaSemAreofolioTemporadaCarro(
			@PathParam("temporada") String temporada,
			@PathParam("carro") String carro) {
		try {
			BufferedImage carroCima = controlePaddock
					.carroCimaSemAreofolioTemporadaCarro(temporada, carro);
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
	@Path("/capacete/{temporada}/{piloto}")
	@Produces("image/png")
	public Response capaceteTemporadaPiloto(
			@PathParam("temporada") String temporada,
			@PathParam("piloto") String piloto) {
		try {
			BufferedImage capacete = controlePaddock
					.capaceteTemporadaPiloto(temporada, piloto);
			if (capacete == null) {
				return Response.status(200).entity("null").build();
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(capacete, "png", baos);
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
	@Path("/carroLado/{temporada}/{carro}")
	@Produces("image/png")
	public Response carroLadoTemporadaCarro(
			@PathParam("temporada") String temporada,
			@PathParam("carro") String carro) {
		try {
			BufferedImage carroCima = controlePaddock
					.carroLadoTemporadaCarro(temporada, carro);
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
		TemporadasDefauts temporadasDefauts = null;
		try {
			temporada = "t" + temporada;
			Map<String, TemporadasDefauts> carregarTemporadasPilotosDefauts = carregadorRecursos
					.carregarTemporadasPilotosDefauts();
			temporadasDefauts = carregarTemporadasPilotosDefauts.get(temporada);
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(200).entity(temporadasDefauts).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Compress
	@Path("/temporadas")
	@Produces(MediaType.APPLICATION_JSON)
	public Response temporadas() {
		Vector<String> carregarTemporadas = null;
		try {
			carregarTemporadas = carregadorRecursos.carregarTemporadas();
		} catch (Exception e) {
			Logger.topExecpts(e);
			return Response.status(500)
					.entity(new ErroServ(e).obterErroFormatado())
					.type(MediaType.APPLICATION_JSON).build();
		}
		return Response.status(200).entity(carregarTemporadas).build();
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

	@GET
	@Compress
	@Path("/equipe")
	@Produces(MediaType.APPLICATION_JSON)
	public Response equipe(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		Object ret = controleJogosServer.equipe(sessaoCliente);
		if (ret == null) {
			return Response.status(204).build();
		}
		Response erro = processsaMensagem(ret, idioma);
		if (erro != null) {
			return erro;
		}
		return Response.status(200).entity(ret).build();
	}

	@GET
	@Compress
	@Path("/equipePilotoCarro")
	@Produces(MediaType.APPLICATION_JSON)
	public Response equipePilotoCarro(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		Object ret = controleJogosServer.equipePilotoCarro(sessaoCliente);
		if (ret == null) {
			return Response.status(204).build();
		}
		Response erro = processsaMensagem(ret, idioma);
		if (erro != null) {
			return erro;
		}
		return Response.status(200).entity(ret).build();
	}

	@POST
	@Compress
	@Path("/equipe")
	@Produces(MediaType.APPLICATION_JSON)
	public Response gravarEquipe(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma, CarreiraDadosSrv equipe) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		ControleJogosServer controleJogosServer = controlePaddock
				.getControleJogosServer();
		Object ret = controleJogosServer.gravarEquipe(sessaoCliente, idioma,
				equipe);
		if (ret.equals(new MsgSrv(Lang.msg("250")))) {
			return Response.status(200).entity(ret).build();
		}
		return processsaMensagem(ret, idioma);
	}

	@POST
	@Compress
	@Path("/campeonato")
	@Produces(MediaType.APPLICATION_JSON)
	public Response gravarCampeonato(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma, CampeonatoSrv campeonato) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		Object ret = null;
		try {
			ret = controlePaddock.criarCampeonato(campeonato, token);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		if (ret.equals(new MsgSrv(Lang.msg("campeonatoCriado")))) {
			return Response.status(200).entity(ret).build();
		}
		return processsaMensagem(ret, idioma);
	}

	@POST
	@Compress
	@Path("/finalizaCampeonato")
	@Produces(MediaType.APPLICATION_JSON)
	public Response finalizaCampeonato(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma, CampeonatoTO campeonato) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());
		Object ret = null;
		try {
			ret = controlePaddock.finalizaCampeonato(campeonato, token);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Response.status(200).entity(campeonato).build();
	}

	@GET
	@Compress
	@Path("/campeonato")
	@Produces(MediaType.APPLICATION_JSON)
	public Response campeonato(@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma) {
		SessaoCliente sessaoCliente = controlePaddock
				.obterSessaoPorToken(token);
		if (sessaoCliente == null) {
			return Response.status(401).build();
		}
		if (sessaoCliente.isGuest()) {
			return Response.status(403).build();
		}
		sessaoCliente.setUlimaAtividade(System.currentTimeMillis());

		CampeonatoTO campeonato = null;
		try {
			campeonato = controlePaddock.obterCampeonatoEmAberto(token);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		if (campeonato == null) {
			return Response.status(204).build();
		}

		return Response.status(200).entity(campeonato).build();
	}

	@GET
	@Compress
	@Path("/campeonato/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response campeonatoPorId(@PathParam("id") String id,
			@HeaderParam("token") String token,
			@HeaderParam("idioma") String idioma) {
		CampeonatoTO campeonato = null;
		try {
			campeonato = controlePaddock.obterCampeonatoId(id);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		if (campeonato == null) {
			return Response.status(204).build();
		}
		return Response.status(200).entity(campeonato).build();
	}

}
