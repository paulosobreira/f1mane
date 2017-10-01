package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.BufferTexto;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosPaddock;
import sowbreira.f1mane.paddock.entidades.TOs.DadosParciais;
import sowbreira.f1mane.paddock.entidades.TOs.DetalhesJogo;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.Posis;
import sowbreira.f1mane.paddock.entidades.TOs.PosisPack;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvJogoPack;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 18:21:11
 */
public class ControleJogosServer {
	private DadosPaddock dadosPaddock;
	private ControleClassificacao controleClassificacao;
	private Map<SessaoCliente, JogoServidor> mapaJogosCriados = new HashMap<SessaoCliente, JogoServidor>();
	private ControleCampeonatoServidor controleCampeonatoServidor;
	private ControlePersistencia controlePersistencia;
	public static int MaxJogo = 1;
	public static int qtdeJogos = 0;

	/**
	 * @param dadosPaddock
	 */
	public ControleJogosServer(DadosPaddock dadosPaddock,
			ControleClassificacao controleClassificacao,
			ControleCampeonatoServidor controleCampeonatoServidor,
			ControlePersistencia controlePersistencia) {
		super();
		this.dadosPaddock = dadosPaddock;
		this.controleClassificacao = controleClassificacao;
		this.controleCampeonatoServidor = controleCampeonatoServidor;
		this.controlePersistencia = controlePersistencia;
	}

	public Map<SessaoCliente, JogoServidor> getMapaJogosCriados() {
		return mapaJogosCriados;
	}

	public void setMapaJogosCriados(
			Map<SessaoCliente, JogoServidor> mapaJogosCriados) {
		this.mapaJogosCriados = mapaJogosCriados;
	}

	public Object criarJogo(ClientPaddockPack clientPaddockPack) {
		if (verificaJaEmAlgumJogo(clientPaddockPack.getSessaoCliente())) {
			return new MsgSrv(Lang.msg("203"));

		}
		Session session = controlePersistencia.getSession();
		try {

			if (!Util.isNullOrEmpty(clientPaddockPack.getDadosJogoCriado()
					.getNomeCampeonato())) {
				Campeonato campeonato = controlePersistencia.pesquisaCampeonato(
						session, clientPaddockPack.getDadosJogoCriado()
								.getNomeCampeonato(),
						false);
				if (campeonato != null && !clientPaddockPack.getSessaoCliente()
						.getNomeJogador().equalsIgnoreCase(
								campeonato.getJogadorDadosSrv().getNome())) {
					return new MsgSrv(Lang.msg("somenteDonoPodeCriar"));
				}

			}
			if ((mapaJogosCriados.size() + 1) > MaxJogo) {
				return new MsgSrv(Lang.msg("204", new Object[]{MaxJogo}));
			}
			for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
					.iterator(); iter.hasNext();) {
				SessaoCliente element = iter.next();
				if (element.equals(clientPaddockPack.getSessaoCliente())) {
					return new MsgSrv(Lang.msg("205"));
				}
			}
			JogoServidor jogoServidor = null;
			String temporada = clientPaddockPack.getDadosJogoCriado()
					.getTemporada();
			try {
				Logger.logar("Temporada Serviddor " + temporada);
				jogoServidor = new JogoServidor(temporada);
				jogoServidor.prepararJogoOnline(
						clientPaddockPack.getDadosJogoCriado());
				jogoServidor.setNomeCriador(
						clientPaddockPack.getSessaoCliente().getNomeJogador());
				jogoServidor.setTempoCriacao(System.currentTimeMillis());
			} catch (Exception e) {
				Logger.logarExept(e);
				ErroServ erroServ = new ErroServ(e);
				return erroServ;
			}
			jogoServidor.setNomeJogoServidor(
					(qtdeJogos++) + "-" + temporada.replaceAll("t", ""));
			mapaJogosCriados.put(clientPaddockPack.getSessaoCliente(),
					jogoServidor);
			gerarListaJogosCriados();

			jogoServidor.setControleClassificacao(controleClassificacao);
			jogoServidor.setControleJogosServer(this);
			jogoServidor
					.setControleCampeonatoServidor(controleCampeonatoServidor);
			SrvPaddockPack srvPaddockPack = preparaSrvPaddockPack(
					clientPaddockPack, jogoServidor);
			return srvPaddockPack;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public SrvPaddockPack preparaSrvPaddockPack(
			ClientPaddockPack clientPaddockPack, JogoServidor jogoServidor) {
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
		srvPaddockPack.setNomeJogoCriado(jogoServidor.getNomeJogoServidor());
		srvPaddockPack.setSessaoCliente(clientPaddockPack.getSessaoCliente());
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		return srvPaddockPack;
	}

	private boolean verificaExcedePotencia(int mediaPontecia, int ptsCarro,
			double nivel) {
		int permitidoAcimaMedia = 0;
		if (InterfaceJogo.FACIL_NV == nivel) {
			permitidoAcimaMedia = Constantes.ACIMA_MEDIA_FACIL;
		}
		if (InterfaceJogo.MEDIO_NV == nivel) {
			permitidoAcimaMedia = Constantes.ACIMA_MEDIA_NORMAL;
		}
		return (mediaPontecia + permitidoAcimaMedia) < ptsCarro;
	}

	private void gerarListaJogosCriados() {
		List<String> jogos = new ArrayList<String>();
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente element = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(element);
			jogos.add(jogoServidor.getNomeJogoServidor());
		}
		Collections.sort(jogos);
		dadosPaddock.setJogosCriados(jogos);

	}

	public Object entrarJogo(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack.getSessaoCliente() == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		if (verificaJaEmAlgumJogo(clientPaddockPack.getSessaoCliente())) {
			return new MsgSrv(Lang.msg("203"));
		}
		String nomeJogo = clientPaddockPack.getDadosJogoCriado().getNomeJogo();
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);

		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("207", new String[]{nomeJogo}));
		}

		CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
				.obterCarreiraSrv(
						clientPaddockPack.getSessaoCliente().getNomeJogador());
		if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
			if (jogoServidor.isCorridaIniciada()) {
				return new MsgSrv(Lang.msg("247"));
			}
			if (verificaExcedePotencia(jogoServidor.getMediaPontecia(),
					carreiraDadosSrv.getPtsCarro()
							+ carreiraDadosSrv.getPtsAerodinamica()
							+ carreiraDadosSrv.getPtsFreio(),
					jogoServidor.getNiveljogo())) {
				int permitidoAcimaMedia = 0;
				if (InterfaceJogo.FACIL_NV == jogoServidor.getNiveljogo()) {
					permitidoAcimaMedia = Constantes.ACIMA_MEDIA_FACIL;
				}
				if (InterfaceJogo.MEDIO_NV == jogoServidor.getNiveljogo()) {
					permitidoAcimaMedia = Constantes.ACIMA_MEDIA_NORMAL;
				}
				String media = (jogoServidor.getMediaPontecia()
						+ permitidoAcimaMedia) + "";
				return new MsgSrv(Lang.msg("261", new String[]{media}));

			}
		}

		if (jogoServidor.isCorridaTerminada()) {
			return new MsgSrv(Lang.msg("206", new String[]{nomeJogo}));
		}
		jogoServidor.setControleClassificacao(controleClassificacao);
		Object retorno = jogoServidor.adicionarJogador(
				clientPaddockPack.getSessaoCliente().getNomeJogador(),
				clientPaddockPack.getDadosJogoCriado());
		if (retorno != null) {
			return retorno;
		}
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(clientPaddockPack.getSessaoCliente());
		srvPaddockPack.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		return srvPaddockPack;
	}

	public boolean verificaJaEmAlgumJogo(SessaoCliente sessaoCliente) {
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidor.getMapJogadoresOnline()
					.get(sessaoCliente.getNomeJogador()) != null) {
				return true;
			}
		}
		return false;
	}

	public JogoServidor obterJogoPeloNome(String nomeJogo) {
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidorTemp = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidorTemp.getNomeJogoServidor().equals(nomeJogo)) {
				return jogoServidorTemp;
			}
		}
		return null;
	}

	private JogoServidor obterJogoPeloNomeDono(String nomeDono) {
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidorTemp = (JogoServidor) mapaJogosCriados
					.get(key);
			if (nomeDono.equals(key.getNomeJogador())) {
				return jogoServidorTemp;
			}
		}
		return null;
	}

	public Object detalhesJogo(ClientPaddockPack clientPaddockPack) {
		String nomeJogo = clientPaddockPack.getNomeJogo();
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("207", new String[]{nomeJogo}));
		}
		DetalhesJogo detalhesJogo = new DetalhesJogo();
		jogoServidor.preencherDetalhes(detalhesJogo);
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(clientPaddockPack.getSessaoCliente());
		srvPaddockPack.setDetalhesJogo(detalhesJogo);
		detalhesJogo.setVoltaAtual(jogoServidor.getNumVoltaAtual());
		return srvPaddockPack;

	}

	public Object verificaEstadoJogo(ClientPaddockPack clientPaddockPack) {
		SrvJogoPack srvJogoPack = new SrvJogoPack();
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		srvJogoPack.setEstadoJogo(jogoServidor.getEstado());
		return srvJogoPack;
	}

	public Object iniciaJogo(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack.getSessaoCliente() == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		JogoServidor jogoServidor = obterJogoPeloNomeDono(
				clientPaddockPack.getSessaoCliente().getNomeJogador());
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("208"));
		}
		try {
			jogoServidor.iniciarJogo();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
		return obterDadosJogo(clientPaddockPack);
	}

	public Object obterDadosJogo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		DadosJogo dadosJogo = new DadosJogo();
		dadosJogo.setMelhoVolta(jogoServidor.obterMelhorVolta());
		dadosJogo.setVoltaAtual(jogoServidor.getNumVoltaAtual());
		dadosJogo.setClima(jogoServidor.getClima());
		dadosJogo.setCorridaIniciada(jogoServidor.isCorridaIniciada());
		dadosJogo.setNomeJogo(clientPaddockPack.getNomeJogo());
		dadosJogo.setErs(jogoServidor.isErs());
		dadosJogo.setDrs(jogoServidor.isDrs());
		dadosJogo.setTrocaPneu(jogoServidor.isTrocaPneu());
		dadosJogo.setReabastacimento(jogoServidor.isReabastacimento());
		List pilotos = jogoServidor.getPilotosCopia();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.setMelhorVolta(null);
			Volta melhor = piloto.obterVoltaMaisRapida();
			piloto.setMelhorVolta(melhor);
		}
		dadosJogo.setCorridaTerminada(jogoServidor.isCorridaTerminada());
		dadosJogo.setPilotosList(pilotos);

		Map mapJogo = jogoServidor.getMapJogadoresOnlineTexto();
		BufferTexto bufferTexto = (BufferTexto) mapJogo
				.get(clientPaddockPack.getSessaoCliente().getNomeJogador());
		if (bufferTexto != null) {
			dadosJogo.setTexto(bufferTexto.consumirTexto());
		}
		return dadosJogo;
	}

	/**
	 * Big Change
	 * 
	 * @param nomeJogo
	 * @return
	 */
	public Object obterPosicaoPilotos(String nomeJogo) {
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return null;
		}
		if (jogoServidor.getMapaNosIds() == null) {
			return null;
		}
		PosisPack pack = gerarPosicaoPilotos(jogoServidor);
		return pack.encode();

	}

	private PosisPack gerarPosicaoPilotos(JogoServidor jogoServidor) {
		List posisList = new ArrayList();
		List pilotos = jogoServidor.getPilotosCopia();
		if (pilotos == null) {
			Logger.logar("gerarPosicaoPilotos pilotos == null");
			return null;
		}
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			Posis posis = new Posis();
			String statusPilotos = "P" + String.valueOf(piloto.getPtosPista());
			if (piloto.isTravouRodas()) {
				statusPilotos = "T" + String.valueOf(piloto.getPtosPista());
			} else if (piloto.isFaiscas()) {
				statusPilotos = "F" + String.valueOf(piloto.getPtosPista());
			} else if (piloto.getCarro().isRecolhido()) {
				statusPilotos = "R";
			}
			posis.status = statusPilotos;
			posis.idPiloto = piloto.getId();
			posis.tracado = piloto.getTracado();
			Integer integer = jogoServidor.getMapaNosIds()
					.get(piloto.getNoAtual());
			if (integer == null) {
				continue;
			}
			posis.idNo = integer.intValue();
			posis.humano = piloto.isJogadorHumano();

			posisList.add(posis);
		}
		PosisPack pack = new PosisPack();
		Object[] object = posisList.toArray();
		Posis[] posis = new Posis[object.length];
		for (int i = 0; i < posis.length; i++) {
			posis[i] = (Posis) object[i];
		}
		pack.posis = posis;
		if (jogoServidor.isSafetyCarNaPista()
				&& jogoServidor.getSafetyCar() != null) {
			pack.safetyNoId = ((Integer) jogoServidor.getMapaNosIds()
					.get(jogoServidor.getSafetyCar().getNoAtual())).intValue();
			pack.safetySair = jogoServidor.getSafetyCar().isVaiProBox();
		}
		return pack;
	}

	public Object mudarGiroMotor(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.getCarro().mudarGiroMotor(clientPaddockPack.getGiroMotor());
		return null;
	}

	public Boolean mudarGiroMotor(SessaoCliente sessaoCliente, String idPiloto,
			String giro) {
		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		int giroAntes = piloto.getCarro().getGiro();
		piloto.getCarro().mudarGiroMotor(giro);
		return giroAntes != piloto.getCarro().getGiro();
	}

	public Boolean mudarAgressividadePiloto(SessaoCliente sessaoCliente,
			String idPiloto, String agressividade) {
		if (!Piloto.LENTO.equals(agressividade)
				&& !Piloto.AGRESSIVO.equals(agressividade)
				&& !Piloto.NORMAL.equals(agressividade)) {
			return false;
		}

		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return false;
		}
		piloto.setAtivarDRS(true);
		piloto.setModoPilotagem(agressividade);
		return agressividade.equals(piloto.getModoPilotagem());
	}

	public Piloto obterPilotoPorId(SessaoCliente sessaoCliente,
			String idPiloto) {
		Piloto acharPiloto = null;
		for (Iterator<SessaoCliente> iterator = mapaJogosCriados.keySet()
				.iterator(); iterator.hasNext();) {
			JogoServidor jogoServidor = mapaJogosCriados.get(iterator.next());
			Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor
					.getMapJogadoresOnline();
			if (!String
					.valueOf(mapJogadoresOnline
							.get(sessaoCliente.getNomeJogador()).getIdPiloto())
					.equals(idPiloto)) {
				return null;
			}
			List piList = jogoServidor.getPilotos();
			for (Iterator iter = piList.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				if (String.valueOf(piloto.getId()).equals(idPiloto)) {
					acharPiloto = piloto;
					break;
				}
			}
		}
		return acharPiloto;
	}

	public JogoServidor obterJogoPorIdPiloto(SessaoCliente sessaoCliente,
			String idPiloto) {
		JogoServidor acharJogo = null;
		for (Iterator<SessaoCliente> iterator = mapaJogosCriados.keySet()
				.iterator(); iterator.hasNext();) {
			JogoServidor jogoServidor = mapaJogosCriados.get(iterator.next());
			Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor
					.getMapJogadoresOnline();
			if (!String
					.valueOf(mapJogadoresOnline
							.get(sessaoCliente.getNomeJogador()).getIdPiloto())
					.equals(idPiloto)) {
				return null;
			}
			List piList = jogoServidor.getPilotos();
			for (Iterator iter = piList.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				if (String.valueOf(piloto.getId()).equals(idPiloto)) {
					acharJogo = jogoServidor;
					break;
				}
			}
		}
		return acharJogo;
	}

	public Object mudarModoBox(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		if (!piloto.entrouNoBox()) {
			piloto.setBox(!piloto.isBox());
			piloto.setTipoPneuBox(clientPaddockPack.getTpPneuBox());
			piloto.setQtdeCombustBox(clientPaddockPack.getCombustBox());
			piloto.setAsaBox(clientPaddockPack.getAsaBox());
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogo
				.get(clientPaddockPack.getSessaoCliente().getNomeJogador());
		dadosParticiparJogo
				.setCombustivel(new Integer(clientPaddockPack.getCombustBox()));
		dadosParticiparJogo.setTpPnueu(clientPaddockPack.getTpPneuBox());
		dadosParticiparJogo.setAsa(clientPaddockPack.getAsaBox());
		if (piloto.isBox()) {
			return "BOX";
		}
		return null;
	}

	public Object sairDoJogo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		if (clientPaddockPack.getSessaoCliente() == null) {
			return null;
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		mapJogo.remove(clientPaddockPack.getSessaoCliente().getNomeJogador());
		jogoServidor.getMapJogadoresOnlineTexto()
				.remove(clientPaddockPack.getSessaoCliente().getNomeJogador());

		jogoServidor.removerJogador(
				clientPaddockPack.getSessaoCliente().getNomeJogador());

		return null;
	}

	public void removerJogo(JogoServidor servidor) {
		SessaoCliente remover = null;
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente element = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(element);
			if (servidor.equals(jogoServidor)) {
				remover = element;
			}
		}
		if (remover != null)
			mapaJogosCriados.remove(remover);
		gerarListaJogosCriados();

	}

	/**
	 * 
	 * @param args
	 *            args[0] jogo args[1] apelido args[2] pilto Sel
	 * @return
	 */
	public Object obterDadosParciaisPilotos(String[] args) {
		JogoServidor jogoServidor = obterJogoPeloNome(args[0]);
		if (jogoServidor == null) {
			return null;
		}
		DadosParciais dadosParciais = new DadosParciais();
		Volta obterMelhorVolta = jogoServidor.obterMelhorVolta();
		if (obterMelhorVolta != null) {
			dadosParciais.melhorVoltaCorrida = obterMelhorVolta
					.getTempoNumero();
		}
		dadosParciais.voltaAtual = jogoServidor.getNumVoltaAtual();
		dadosParciais.clima = jogoServidor.getClima();
		dadosParciais.estado = jogoServidor.getEstado();
		dadosParciais.posisPack = gerarPosicaoPilotos(jogoServidor);
		List<Piloto> pilotos = jogoServidor.getPilotosCopia();
		for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = iter.next();
			if (args.length <= 2) {
				break;
			}
			if (piloto.getId() != Integer.parseInt(args[2])) {
				continue;
			}
			Volta obterVoltaMaisRapida = piloto.obterVoltaMaisRapida();
			if (obterVoltaMaisRapida != null) {
				dadosParciais.melhorVolta = obterVoltaMaisRapida
						.getTempoNumero();
			}
			int contVolta = 1;
			List<Volta> voltas = piloto.getVoltas();
			for (int i = voltas.size() - 1; i > -1; i--) {
				Volta volta = (Volta) voltas.get(i);
				if (contVolta == 1) {
					dadosParciais.ultima1 = volta.getTempoNumero();
				}
				if (contVolta == 2) {
					dadosParciais.ultima2 = volta.getTempoNumero();
				}
				if (contVolta == 3) {
					dadosParciais.ultima3 = volta.getTempoNumero();
				}
				if (contVolta == 4) {
					dadosParciais.ultima4 = volta.getTempoNumero();
				}
				if (contVolta == 5) {
					dadosParciais.ultima5 = volta.getTempoNumero();
				}
				contVolta++;
				if (contVolta > 5) {
					break;
				}

			}
			dadosParciais.nomeJogador = piloto.getNomeJogador();
			dadosParciais.dano = piloto.getCarro().getDanificado();
			dadosParciais.box = piloto.isBox();
			dadosParciais.podeUsarDRS = piloto.isPodeUsarDRS();
			dadosParciais.recebeuBanderada = piloto.isRecebeuBanderada();
			dadosParciais.stress = piloto.getStress();
			dadosParciais.cargaErs = piloto.getCarro().getCargaErs();
			dadosParciais.alertaMotor = piloto.isAlertaMotor();
			dadosParciais.alertaAerefolio = piloto.isAlertaAerefolio();
			dadosParciais.pCombust = piloto.getCarro()
					.getPorcentagemCombustivel();
			dadosParciais.pPneus = piloto.getCarro()
					.getPorcentagemDesgastePneus();
			dadosParciais.pMotor = piloto.getCarro()
					.getPorcentagemDesgasteMotor();
			if (piloto.getCarroPilotoDaFrente() != null) {
				dadosParciais.tpPneusFrente = piloto.getCarroPilotoDaFrente()
						.getTipoPneu();
			}
			if (piloto.getCarroPilotoAtras() != null) {
				dadosParciais.tpPneusAtras = piloto.getCarroPilotoAtras()
						.getTipoPneu();
			}
			dadosParciais.tpPneus = piloto.getCarro().getTipoPneu();
			dadosParciais.asa = piloto.getCarro().getAsa();
			dadosParciais.paradas = piloto.getQtdeParadasBox();
			dadosParciais.velocidade = piloto.getVelocidade() == 0
					? 0
					: piloto.getVelocidadeExibir();
			dadosParciais.combustBox = piloto.getQtdeCombustBox();
			dadosParciais.tpPneusBox = piloto.getTipoPneuBox();
			if (Util.isNullOrEmpty(dadosParciais.tpPneusBox)) {
				dadosParciais.tpPneusBox = dadosParciais.tpPneus;
			}
			dadosParciais.asaBox = piloto.getAsaBox();
			if (Util.isNullOrEmpty(dadosParciais.asaBox)) {
				dadosParciais.asaBox = dadosParciais.asa;
			}
			dadosParciais.modoPilotar = piloto.getModoPilotagem();
			dadosParciais.giro = piloto.getCarro().getGiro();
			dadosParciais.vantagem = piloto.getVantagem();
		}
		Map<String, BufferTexto> mapJogo = jogoServidor
				.getMapJogadoresOnlineTexto();
		BufferTexto bufferTexto = (BufferTexto) mapJogo.get(args[1]);
		if (bufferTexto != null) {
			dadosParciais.texto = bufferTexto.consumirTexto();
		}
		// enc dadosParciais

		return dadosParciais.encode();
	}

	public void removerClienteInativo(SessaoCliente sessaoCliente) {
		if (sessaoCliente == null) {
			return;
		}
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext();) {
			SessaoCliente element = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(element);
			jogoServidor.removerJogador(sessaoCliente.getNomeJogador());
		}

	}

	public Object mudarModoPilotagem(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setModoPilotagem(clientPaddockPack.getModoPilotagem());
		return null;
	}

	public Object mudarModoAutoPos(ClientPaddockPack clientPaddockPack,
			boolean autoPos) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAutoPos(autoPos);
		return null;
	}

	public Object mudarTracado(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		Logger.logar(
				"mudarTracado clientPaddockPack.getSessaoCliente().getNomeJogador()"
						+ clientPaddockPack.getSessaoCliente()
								.getNomeJogador());
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.mudarTracado(clientPaddockPack.getTracado(), jogoServidor);
		return null;
	}

	public Object mudarTracadoPiloto(SessaoCliente sessaoCliente,
			String idPiloto, String tracado) {
		if (!"0".equals(tracado) && !"1".equals(tracado)
				&& !"2".equals(tracado)) {
			return false;
		}

		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return false;
		}
		piloto.setAtivarDRS(true);
		return piloto.mudarTracado(Integer.parseInt(tracado),
				obterJogoPorIdPiloto(sessaoCliente, idPiloto));
	}

	private Piloto obterPiloto(ClientPaddockPack clientPaddockPack,
			JogoServidor jogoServidor) {
		Piloto acharPiloto = obterPilotoLista(clientPaddockPack, jogoServidor);
		int cont = 0;
		while (acharPiloto == null && cont < 5) {
			acharPiloto = obterPilotoLista(clientPaddockPack, jogoServidor);
			cont++;
		}
		return acharPiloto;
	}

	private Piloto obterPilotoLista(ClientPaddockPack clientPaddockPack,
			JogoServidor jogoServidor) {
		try {
			List piList = jogoServidor.getPilotos();
			Piloto acharPiloto = null;
			for (Iterator iter = piList.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				if (clientPaddockPack.getSessaoCliente().getNomeJogador()
						.equals(piloto.getNomeJogador())) {
					acharPiloto = piloto;
					break;
				}
			}
			return acharPiloto;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public Object dadosPilotosJogo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		clientPaddockPack.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
		return clientPaddockPack;
	}

	public Object mudarDrs(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		if (jogoServidor.isChovendo()) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		return null;
	}

	public Object mudarKers(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarErs(
				((Boolean) (clientPaddockPack.getDataObject())).booleanValue());
		return null;
	}

	public Object detalhesVoltasJogo(ClientPaddockPack clientPaddockPack) {
		String nomeJogo = clientPaddockPack.getNomeJogo();
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return null;
		}
		DetalhesJogo detalhesJogo = new DetalhesJogo();
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		detalhesJogo.setVoltaAtual(jogoServidor.getNumVoltaAtual());
		detalhesJogo
				.setNumVoltas(jogoServidor.getDadosCriarJogo().getQtdeVoltas());
		srvPaddockPack.setDetalhesJogo(detalhesJogo);
		return srvPaddockPack;
	}

	public Object driveThru(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		String requisitorDriveThru = clientPaddockPack.getSessaoCliente()
				.getNomeJogador();
		String jogadorDriveTru = (String) clientPaddockPack.getDataObject();
		if (Util.isNullOrEmpty(jogadorDriveTru)) {
			Logger.logar("jogadorDriveTru null");
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		int metadeJogadores = jogoServidor.getNumJogadores() / 2;
		if (piloto.adicionaVotoDriveThru(requisitorDriveThru)) {
			if (piloto.getVotosDriveThru() > (metadeJogadores)) {
				piloto.setDriveThrough(true);
				jogoServidor.infoPrioritaria(
						Html.driveThru(Lang.msg("penalidadePilotoDriveThru",
								new String[]{Html.superRed(jogadorDriveTru),
										Html.bold(requisitorDriveThru),
										"" + piloto.getVotosDriveThru(),
										"" + (metadeJogadores + 1)})));
			} else {
				jogoServidor.infoPrioritaria(
						Html.driveThru(Lang.msg("votoPilotoDriveThru",
								new String[]{Html.superRed(jogadorDriveTru),
										Html.bold(requisitorDriveThru),
										"" + piloto.getVotosDriveThru(),
										"" + (metadeJogadores + 1)})));
			}
		}
		return null;
	}

	public Object mudarPilotoMinimo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarErs(false);
		piloto.setModoPilotagem(Piloto.LENTO);
		piloto.getCarro().mudarGiroMotor(Carro.GIRO_MIN);
		return null;
	}

	public Object mudarPilotoNormal(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarErs(false);
		piloto.setModoPilotagem(Piloto.NORMAL);
		piloto.getCarro().mudarGiroMotor(Carro.GIRO_NOR);
		return null;
	}

	public Object mudarPilotoMaximo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarErs(true);
		piloto.setModoPilotagem(Piloto.AGRESSIVO);
		piloto.getCarro().mudarGiroMotor(Carro.GIRO_MAX);
		return null;
	}

	public Object alterarOpcoesBox(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = obterPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			Logger.logar("Piloto null");
			return null;
		}
		if (piloto.getPtosBox() != 0) {
			return null;
		}
		if (!piloto.entrouNoBox()) {
			piloto.setTipoPneuBox(clientPaddockPack.getTpPneuBox());
			piloto.setQtdeCombustBox(clientPaddockPack.getCombustBox());
			piloto.setAsaBox(clientPaddockPack.getAsaBox());
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogo
				.get(clientPaddockPack.getSessaoCliente().getNomeJogador());
		dadosParticiparJogo
				.setCombustivel(new Integer(clientPaddockPack.getCombustBox()));
		dadosParticiparJogo.setTpPnueu(clientPaddockPack.getTpPneuBox());
		dadosParticiparJogo.setAsa(clientPaddockPack.getAsaBox());
		return null;
	}

	public Object mudarDrs(SessaoCliente sessaoCliente, String idPiloto) {
		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return false;
		}
		piloto.setAtivarDRS(true);
		return Carro.MENOS_ASA.equals(piloto.getCarro().getAsa());
	}

	public Object mudarErs(SessaoCliente sessaoCliente, String idPiloto) {
		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return false;
		}
		piloto.setAtivarErs(!piloto.isAtivarErs());
		return piloto.isAtivarErs();
	}

	public Object boxPiloto(SessaoCliente sessaoCliente, String idPiloto,
			Boolean ativa, String pneu, Integer combustivel, String asa) {
		Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
		if (piloto == null) {
			return false;
		}
		if (piloto.entrouNoBox()) {
			return false;
		}
		if (!Carro.MAIS_ASA.equals(asa) && !Carro.MENOS_ASA.equals(asa)
				&& !Carro.ASA_NORMAL.equals(asa)) {
			return false;
		}
		if (!Carro.TIPO_PNEU_CHUVA.equals(pneu)
				&& !Carro.TIPO_PNEU_MOLE.equals(pneu)
				&& !Carro.TIPO_PNEU_DURO.equals(pneu)) {
			return false;
		}
		if (combustivel > 100) {
			combustivel = 100;
		}
		if (combustivel < 0) {
			combustivel = 0;
		}
		piloto.setBox(ativa);
		piloto.setTipoPneuBox(pneu);
		piloto.setQtdeCombustBox(combustivel);
		piloto.setAsaBox(asa);
		JogoServidor jogoServidor = obterJogoPorIdPiloto(sessaoCliente,
				idPiloto);
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogo
				.get(sessaoCliente.getNomeJogador());
		dadosParticiparJogo.setCombustivel(combustivel);
		dadosParticiparJogo.setTpPnueu(pneu);
		dadosParticiparJogo.setAsa(asa);
		return piloto.isBox();
	}

}
