package br.f1mane.servidor.controles;

import br.f1mane.controles.ControleEstatisticas;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.JogoServidor;
import br.f1mane.servidor.entidades.BufferTexto;
import br.f1mane.servidor.entidades.TOs.*;
import br.f1mane.servidor.entidades.persistencia.CarreiraDadosSrv;
import br.nnpe.Global;
import br.nnpe.Logger;
import br.nnpe.Util;
import org.hibernate.Session;
import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.Volta;

import java.util.*;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 18:21:11
 */
public class ControleJogosServer {
	private final DadosPaddock dadosPaddock;
	private final ControleClassificacao controleClassificacao;
	private Map<SessaoCliente, JogoServidor> mapaJogosCriados = new HashMap<SessaoCliente, JogoServidor>();
	private final ControleCampeonatoServidor controleCampeonatoServidor;
	private final ControlePersistencia controlePersistencia;
	private final ControlePaddockServidor controlePaddockServidor;
	public static final int MaxJogo = 5;
	public static int qtdeJogos = 0;

	/**
	 * @param dadosPaddock
	 */
	public ControleJogosServer(DadosPaddock dadosPaddock,
							   ControleClassificacao controleClassificacao,
							   ControleCampeonatoServidor controleCampeonatoServidor,
							   ControlePersistencia controlePersistencia,
							   ControlePaddockServidor controlePaddockServidor) {
		super();
		this.dadosPaddock = dadosPaddock;
		this.controleClassificacao = controleClassificacao;
		this.controleCampeonatoServidor = controleCampeonatoServidor;
		this.controlePersistencia = controlePersistencia;
		this.controlePaddockServidor = controlePaddockServidor;
	}

	public Map<SessaoCliente, JogoServidor> getMapaJogosCriados() {
		return mapaJogosCriados;
	}

	public void setMapaJogosCriados(
			Map<SessaoCliente, JogoServidor> mapaJogosCriados) {
		this.mapaJogosCriados = mapaJogosCriados;
	}

	public Object criarJogo(ClientPaddockPack clientPaddockPack)
			throws Exception {
		if (verificaJaEmAlgumJogo(clientPaddockPack.getSessaoCliente())) {
			String nomeJogo = clientPaddockPack.getDadosJogoCriado()
					.getNomeJogo();
			JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
			if (jogoServidor != null) {
				SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
				srvPaddockPack
						.setSessaoCliente(clientPaddockPack.getSessaoCliente());
				srvPaddockPack
						.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
				srvPaddockPack.setDadosPaddock(dadosPaddock);
				return srvPaddockPack;
			}
		}

		if ((mapaJogosCriados.size() + 1) > MaxJogo) {
			return new MsgSrv(Lang.msg("204", new Object[]{Integer.valueOf(MaxJogo)}));
		}
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente element = iter.next();
			if (element.equals(clientPaddockPack.getSessaoCliente())) {
				return new MsgSrv(Lang.msg("205"));
			}
		}
		JogoServidor jogoServidor;
		String temporada = clientPaddockPack.getDadosJogoCriado()
				.getTemporada();

		Logger.logar("Temporada Serviddor " + temporada);
		jogoServidor = new JogoServidor(temporada,
				clientPaddockPack.getDadosJogoCriado());
		jogoServidor.setNomeCriador(
				clientPaddockPack.getSessaoCliente().getNomeJogador());
		jogoServidor.setTokenCriador(
				clientPaddockPack.getSessaoCliente().getToken());
		jogoServidor.setNomeJogoServidor(
				(qtdeJogos++) + "-" + temporada.replaceAll("t", ""));
		jogoServidor.setControleClassificacao(controleClassificacao);
		jogoServidor.setControleJogosServer(this);
		jogoServidor.setControleCampeonatoServidor(controleCampeonatoServidor);
		SrvPaddockPack srvPaddockPack = preparaSrvPaddockPack(clientPaddockPack,
				jogoServidor);
		CarreiraDadosSrv carreiraDadosSrv = null;
		if (!clientPaddockPack.getSessaoCliente().isGuest()) {
			carreiraDadosSrv = controleClassificacao.obterCarreiraSrv(
					clientPaddockPack.getSessaoCliente().getToken());
		}
		if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
			if (jogoServidor.isCorridaIniciada()) {
				return new MsgSrv(Lang.msg("247"));
			}
			if (Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
					|| Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())
					|| Util.isNullOrEmpty(
					carreiraDadosSrv.getNomePilotoAbreviado())) {
				return new MsgSrv(Lang.msg("128"));
			}
			if (verificaPotenciaLimite(jogoServidor.getMediaPontecia(),
					carreiraDadosSrv.getPtsCarro()
							+ carreiraDadosSrv.getPtsAerodinamica()
							+ carreiraDadosSrv.getPtsFreio())) {
				String media = (jogoServidor.getMediaPontecia()) + "";
				return new MsgSrv(Lang.msg("261", new String[]{media}));

			}
		}
		mapaJogosCriados.put(clientPaddockPack.getSessaoCliente(),
				jogoServidor);
		gerarListaJogosCriados();
		return srvPaddockPack;
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

	private boolean verificaPotenciaLimite(int mediaPontecia, int ptsCarro) {
		return (mediaPontecia) < ptsCarro;
	}

	private void gerarListaJogosCriados() {
		List<String> jogos = new ArrayList<String>();
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
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
			return new MsgSrv(Lang.msg("210"));
		}
		if (verificaJaEmAlgumJogo(clientPaddockPack.getSessaoCliente())) {
			return new MsgSrv(Lang.msg("203"));
		}
		String nomeJogo = clientPaddockPack.getDadosJogoCriado().getNomeJogo();
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);

		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("207", new String[]{nomeJogo}));
		}

		CarreiraDadosSrv carreiraDadosSrv = null;
		if (!clientPaddockPack.getSessaoCliente().isGuest()) {
			carreiraDadosSrv = controleClassificacao.obterCarreiraSrv(
					clientPaddockPack.getSessaoCliente().getToken());
		}
		if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
			if (jogoServidor.isCorridaIniciada()) {
				return new MsgSrv(Lang.msg("247"));
			}
			if (Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
					|| Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())
					|| Util.isNullOrEmpty(
					carreiraDadosSrv.getNomePilotoAbreviado())) {
				return new MsgSrv(Lang.msg("128"));
			}
			if (verificaPotenciaLimite(jogoServidor.getMediaPontecia(),
					carreiraDadosSrv.getPtsCarro()
							+ carreiraDadosSrv.getPtsAerodinamica()
							+ carreiraDadosSrv.getPtsFreio())) {
				String media = (jogoServidor.getMediaPontecia()) + "";
				return new MsgSrv(Lang.msg("261", new String[]{media}));

			}
		}

		if (jogoServidor.isCorridaTerminada()) {
			return new MsgSrv(Lang.msg("206", new String[]{nomeJogo}));
		}
		jogoServidor.setControleClassificacao(controleClassificacao);
		Object retorno = jogoServidor.adicionarJogador(
				clientPaddockPack.getSessaoCliente(),
				clientPaddockPack.getDadosJogoCriado());
		if (retorno != null) {
			return retorno;
		}
		SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
		srvPaddockPack.setSessaoCliente(clientPaddockPack.getSessaoCliente());
		srvPaddockPack.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
		srvPaddockPack.setDadosPaddock(dadosPaddock);
		jogoServidor.atualizarJogadoresOnline();
		if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
			jogoServidor.atualizarJogadoresOnlineCarreira();
		}
		return srvPaddockPack;
	}

	public boolean verificaJaEmAlgumJogo(SessaoCliente sessaoCliente) {
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidor.getMapJogadoresOnline()
					.get(sessaoCliente.getToken()) != null) {
				return true;
			}
		}
		return false;
	}

	public JogoServidor obterJogoPeloNome(String nomeJogo) {
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidorTemp = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidorTemp.getNomeJogoServidor().equals(nomeJogo)) {
				return jogoServidorTemp;
			}
		}
		return null;
	}

	private JogoServidor obterJogoPeloTokenDono(String tokenDono) {
		if (tokenDono == null) {
			return null;
		}
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente key = iter.next();
			JogoServidor jogoServidorTemp = (JogoServidor) mapaJogosCriados
					.get(key);
			if (tokenDono.equals(key.getToken())) {
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
		JogoServidor jogoServidor = obterJogoPeloTokenDono(
				clientPaddockPack.getSessaoCliente().getToken());
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("208"));
		}
		try {
			jogoServidor.iniciarJogo();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
		return clientPaddockPack;
	}

	public Object obterDadosJogo(ClientPaddockPack clientPaddockPack) {
		String nomeJogo = clientPaddockPack.getNomeJogo();
		JogoServidor jogoServidor;
		SessaoCliente sessaoCliente = clientPaddockPack.getSessaoCliente();
		if (nomeJogo != null) {
			jogoServidor = obterJogoPeloNome(nomeJogo);
		} else {
			jogoServidor = obterJogoPorSessaoCliente(sessaoCliente);
		}
		if (jogoServidor == null) {
			return null;
		}
		DadosJogo dadosJogo = new DadosJogo();
		dadosJogo.setMelhoVolta(jogoServidor.obterMelhorVolta());
		dadosJogo.setVoltaAtual(Integer.valueOf(jogoServidor.getNumVoltaAtual()));
		dadosJogo.setClima(jogoServidor.getClima());
		dadosJogo.setCorridaIniciada(Boolean.valueOf(jogoServidor.isCorridaIniciada()));
		dadosJogo.setNomeJogo(clientPaddockPack.getNomeJogo());
		dadosJogo.setErs(Boolean.valueOf(jogoServidor.isErs()));
		dadosJogo.setDrs(Boolean.valueOf(jogoServidor.isDrs()));
		dadosJogo.setTrocaPneu(Boolean.valueOf(jogoServidor.isTrocaPneu()));
		dadosJogo.setReabastecimento(Boolean.valueOf(jogoServidor.isReabastecimento()));
		dadosJogo.setNomeCircuito(
				Util.substVogais(jogoServidor.getCircuito().getNome()));
		dadosJogo.setArquivoCircuito(jogoServidor.getCircuitos()
				.get(jogoServidor.getCircuito().getNome()));
		dadosJogo.setTemporada(jogoServidor.getTemporada().replaceAll("t", ""));
		Integer segundosParaIniciar = Integer.valueOf(Global.SEGUNDOS_PARA_INICIAR_CORRRIDA.intValue()
				- Util.inteiro(((System.currentTimeMillis()
				- jogoServidor.getTempoCriacao()) / 1000)));
		if (segundosParaIniciar.intValue() < 0) {
			segundosParaIniciar = Integer.valueOf(0);
		}
		dadosJogo.setSegundosParaIniciar(
				ControleEstatisticas.dez.format(segundosParaIniciar));
		Piloto pilotoSessao = obterPilotoPorSessaoCliente(sessaoCliente);
		if (pilotoSessao != null) {
			dadosJogo.setIdPilotoSelecionado(Integer.valueOf(pilotoSessao.getId()));
		}
		dadosJogo.setNumeroVotas(Integer.valueOf(jogoServidor.totalVoltasCorrida()));
		dadosJogo.setEstado(jogoServidor.getEstado());
		List pilotos = jogoServidor.getPilotosCopia();
		for (Iterator iter = pilotos.iterator(); iter.hasNext(); ) {
			Piloto piloto = (Piloto) iter.next();
			piloto.setMelhorVolta(null);
			Volta melhor = piloto.obterVoltaMaisRapida();
			piloto.setMelhorVolta(melhor);
			piloto.setTempoVoltaQualificacao(ControleEstatisticas
					.formatarTempo(Long.valueOf(piloto.getCiclosVoltaQualificacao())));
		}
		dadosJogo.setCorridaTerminada(Boolean.valueOf(jogoServidor.isCorridaTerminada()));
		dadosJogo.setPilotos(pilotos);

		dadosJogo.setCampeonato(
				jogoServidor.getDadosCriarJogo().getNomeCampeonato());
		dadosJogo.setRodadaCampeonato(
				jogoServidor.getDadosCriarJogo().getRodadaCampeonato());

		Map mapJogo = jogoServidor.getMapJogadoresOnlineTexto();
		return dadosJogo;
	}

	private Piloto obterPilotoPorSessaoCliente(SessaoCliente sessaoCliente) {
		if (sessaoCliente == null) {
			return null;
		}
		if (sessaoCliente.getIdPilotoAtual() == null) {
			return null;
		}
		Piloto acharPiloto = null;
		for (Iterator<SessaoCliente> iterator = mapaJogosCriados.keySet()
				.iterator(); iterator.hasNext(); ) {
			JogoServidor jogoServidor = mapaJogosCriados.get(iterator.next());
			List piList = jogoServidor.getPilotos();
			for (Iterator iter = piList.iterator(); iter.hasNext(); ) {
				Piloto piloto = (Piloto) iter.next();
				if (sessaoCliente.getIdPilotoAtual().intValue() == piloto
						.getId()) {
					acharPiloto = piloto;
					break;
				}
			}
		}
		return acharPiloto;
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
		List<Posis> posisList = new ArrayList<Posis>();
		List<Piloto> pilotos = jogoServidor.getPilotosCopia();
		if (pilotos == null) {
			Logger.logar("gerarPosicaoPilotos pilotos == null");
			return null;
		}
		for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext(); ) {
			Piloto piloto = iter.next();
			Posis posis = new Posis();
			String statusPilotos = null;
			if (piloto.verificaNaoPrecisaDesenhar()) {
				statusPilotos = "R";
			} else if (piloto.isRecebeuBanderada() && Carro.PERDEU_AEREOFOLIO
					.equals(piloto.getCarro().getDanificado())) {
				statusPilotos = "BA";
			} else if (piloto.isRecebeuBanderada()) {
				statusPilotos = "B";
			} else if (Carro.PERDEU_AEREOFOLIO
					.equals(piloto.getCarro().getDanificado())) {
				statusPilotos = "A";
			} else if (piloto.isTravouRodas()) {
				statusPilotos = "T";
			} else if (piloto.isMarcaPneu()) {
				statusPilotos = "M";
			} else if (piloto.isFaiscas()) {
				statusPilotos = "F";
			}
			posis.setStatus(statusPilotos);
			posis.setIdPiloto(piloto.getId());
			posis.setTracado(piloto.getTracado());
			Integer integer = jogoServidor.getMapaNosIds()
					.get(piloto.getNoAtual());
			if (integer == null) {
				continue;
			}
			posis.setIdNo(integer.intValue());
			posis.setHumano(piloto.isJogadorHumano());

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
			pack.safetyTracado = jogoServidor.getSafetyCar().getTracado();
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
		try {
			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return null;
			}
			piloto.setAtivarDRS(true);
			int giroAntes = piloto.getCarro().getGiro();
			piloto.getCarro().mudarGiroMotor(giro);
			return Boolean.valueOf(giroAntes != piloto.getCarro().getGiro());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Boolean.FALSE;
	}

	public Boolean mudarAgressividadePiloto(SessaoCliente sessaoCliente,
											String idPiloto, String agressividade) {
		try {
			if (!Piloto.LENTO.equals(agressividade)
					&& !Piloto.AGRESSIVO.equals(agressividade)
					&& !Piloto.NORMAL.equals(agressividade)) {
				return Boolean.FALSE;
			}

			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return Boolean.FALSE;
			}
			piloto.setAtivarDRS(true);
			piloto.setModoPilotagem(agressividade);
			return Boolean.valueOf(agressividade.equals(piloto.getModoPilotagem()));
		} catch (Exception e) {
			Logger.logarExept(e);

		}
		return Boolean.FALSE;
	}

	public Piloto obterPilotoPorId(SessaoCliente sessaoCliente,
								   String idPiloto) {
		Piloto acharPiloto = null;
		for (Iterator<SessaoCliente> iterator = mapaJogosCriados.keySet()
				.iterator(); iterator.hasNext(); ) {
			JogoServidor jogoServidor = mapaJogosCriados.get(iterator.next());
			if (!jogoServidor.getNomeJogoServidor()
					.equals(sessaoCliente.getJogoAtual())) {
				continue;
			}
			Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor
					.getMapJogadoresOnline();
			if (!String.valueOf(mapJogadoresOnline.get(sessaoCliente.getToken())
					.getIdPiloto()).equals(idPiloto)) {
				return null;
			}
			List piList = jogoServidor.getPilotos();
			for (Iterator iter = piList.iterator(); iter.hasNext(); ) {
				Piloto piloto = (Piloto) iter.next();
				if (String.valueOf(piloto.getId()).equals(idPiloto)) {
					acharPiloto = piloto;
					break;
				}
			}
		}
		return acharPiloto;
	}

	public JogoServidor obterJogoPorSessaoCliente(SessaoCliente sessaoCliente) {
		for (Iterator<SessaoCliente> iterator = mapaJogosCriados.keySet()
				.iterator(); iterator.hasNext(); ) {
			JogoServidor jogoServidor = mapaJogosCriados.get(iterator.next());
			Map<String, DadosCriarJogo> mapJogadoresOnline = jogoServidor
					.getMapJogadoresOnline();
			if (mapJogadoresOnline.containsKey(sessaoCliente.getToken())) {
				return jogoServidor;
			}
		}
		return null;
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
				.get(clientPaddockPack.getSessaoCliente().getToken());
		dadosParticiparJogo
				.setCombustivel(Integer.valueOf(clientPaddockPack.getCombustBox()));
		dadosParticiparJogo.setTpPneu(clientPaddockPack.getTpPneuBox());
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
		mapJogo.remove(clientPaddockPack.getSessaoCliente().getToken());
		jogoServidor.getMapJogadoresOnlineTexto()
				.remove(clientPaddockPack.getSessaoCliente().getToken());

		jogoServidor.removerJogador(
				clientPaddockPack.getSessaoCliente().getToken());

		return null;
	}

	public void sairJogoToken(String nomeJogo, String token) {
		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return;
		}
		if (token == null) {
			return;
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		mapJogo.remove(token);
		jogoServidor.getMapJogadoresOnlineTexto().remove(token);
		jogoServidor.removerJogador(token);
	}

	public void removerJogo(JogoServidor servidor) {
		SessaoCliente remover = null;
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente element = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(element);
			if (servidor.equals(jogoServidor)) {
				remover = element;
			}
		}
		if (remover != null) {
			mapaJogosCriados.remove(remover);
		}
		gerarListaJogosCriados();

	}

	/**
	 * @param args args[0] jogo args[1] tokenJogador args[2] pilto Sel
	 * @return
	 */
	public Object obterDadosParciaisPilotos(String[] args) {
		if (args == null || args.length < 3) {
			return null;
		}
		DadosParciais dadosParciais = obterDadosParciaisPilotos(args[0],
				args[1], args[2]);
		if (dadosParciais == null) {
			return null;
		}
		return dadosParciais.encode();
	}

	public DadosParciais obterDadosParciaisPilotos(String nomeJogo,
												   String tokenJogador, String idPilotoStr) {

		JogoServidor jogoServidor = obterJogoPeloNome(nomeJogo);
		if (jogoServidor == null) {
			return null;
		}
		if (!jogoServidor.getMapJogadoresOnline().containsKey(tokenJogador)) {
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
		for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext(); ) {
			Piloto piloto = iter.next();
			if (Util.isNullOrEmpty(idPilotoStr)) {
				break;
			}
			int idPiloto = 0;
			try {
				idPiloto = Integer.valueOf(idPilotoStr).intValue();
			} catch (Exception e) {
			}
			if (piloto.getId() != idPiloto) {
				continue;
			}
			Volta obterVoltaMaisRapida = piloto.obterVoltaMaisRapida();
			if (obterVoltaMaisRapida != null) {
				dadosParciais.melhorVolta = obterVoltaMaisRapida
						.getTempoNumero();
			}
			int contVolta = 1;
			List<Volta> voltas = piloto.getVoltasCopy();
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
			if (dadosParciais.stress > 100) {
				dadosParciais.stress = 100;
			}
			if (dadosParciais.stress < 0) {
				dadosParciais.stress = 0;
			}
			dadosParciais.cargaErs = piloto.getCarro().getCargaErs();
			if (dadosParciais.cargaErs > 100) {
				dadosParciais.cargaErs = 100;
			}
			if (dadosParciais.cargaErs < 0) {
				dadosParciais.cargaErs = 0;
			}
			dadosParciais.alertaMotor = piloto.isAlertaMotor();
			dadosParciais.alertaAerefolio = piloto.isAlertaAerefolio();
			dadosParciais.pCombust = piloto.getCarro()
					.getPorcentagemCombustivel();
			dadosParciais.pVolta = piloto.getNumeroVolta();
			if (dadosParciais.pCombust > 100) {
				dadosParciais.pCombust = 100;
			}
			if (dadosParciais.pCombust < 0) {
				dadosParciais.pCombust = 0;
			}
			dadosParciais.pPneus = piloto.getCarro()
					.getPorcentagemDesgastePneus();
			if (dadosParciais.pPneus > 100) {
				dadosParciais.pPneus = 100;
			}
			if (dadosParciais.pPneus < 0) {
				dadosParciais.pPneus = 0;
			}
			dadosParciais.pMotor = piloto.getCarro()
					.getPorcentagemDesgasteMotor();
			if (dadosParciais.pMotor > 100) {
				dadosParciais.pMotor = 100;
			}
			if (dadosParciais.pMotor < 0) {
				dadosParciais.pMotor = 0;
			}
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
			if (piloto.getPosicao() == 1) {
				dadosParciais.vantagem = piloto
						.getCalculaSegundosParaAnterior();
			} else if (piloto.getPosicao() == pilotos.size() - 1) {
				dadosParciais.vantagem = piloto.getCalculaSegundosParaProximo();
			} else {
				Long anterior = Util.extrairNumerosLong(
						piloto.getCalculaSegundosParaAnterior());
				Long proximo = Util.extrairNumerosLong(
						piloto.getCalculaSegundosParaProximo());
				boolean atrasBox = false;
				boolean frenteBox = false;
				if (piloto.getCarroPilotoAtras() != null
						&& jogoServidor.verificaNoPitLane(
						piloto.getCarroPilotoAtras().getPiloto())) {
					atrasBox = true;
				}
				if (piloto.getCarroPilotoAtras() != null
						&& piloto.getCarroPilotoDaFrente() != null
						&& jogoServidor.verificaNoPitLane(
						piloto.getCarroPilotoDaFrente().getPiloto())) {
					frenteBox = true;
				}

				if (anterior != null && proximo != null) {
					if (anterior.longValue() < proximo.longValue() && !atrasBox) {
						dadosParciais.vantagem = piloto
								.getCalculaSegundosParaAnterior();
					} else if (anterior.longValue() > proximo.longValue() && !frenteBox) {
						dadosParciais.vantagem = piloto
								.getCalculaSegundosParaProximo();
					} else {
						dadosParciais.vantagem = "BOX";
					}
				}
			}

		}
		Map<String, BufferTexto> mapJogo = jogoServidor
				.getMapJogadoresOnlineTexto();
		BufferTexto bufferTexto = (BufferTexto) mapJogo.get(tokenJogador);
		if (bufferTexto != null) {
			dadosParciais.texto = bufferTexto.consumirTexto();
		}
		return dadosParciais;

	}

	public boolean removerCliente(SessaoCliente sessaoCliente) {
		if (sessaoCliente == null) {
			return false;
		}
		boolean removeu = false;
		for (Iterator<SessaoCliente> iter = mapaJogosCriados.keySet()
				.iterator(); iter.hasNext(); ) {
			SessaoCliente element = iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(element);
			if (jogoServidor == null || jogoServidor.isCorridaTerminada()) {
				continue;
			}
			if (jogoServidor.removerJogador(sessaoCliente.getToken())) {
				sessaoCliente.limpaSelecao();
				removeu = true;
			}
		}
		return removeu;

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

	public Object setManualTemporario(ClientPaddockPack clientPaddockPack,
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
		return null;
	}

	public Object mudarTracado(ClientPaddockPack clientPaddockPack) {
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
		piloto.mudarTracado(clientPaddockPack.getTracado(), jogoServidor);
		return null;
	}

	public Object mudarTracadoPiloto(SessaoCliente sessaoCliente,
									 String idPiloto, String tracado) {
		try {
			if (!"0".equals(tracado) && !"1".equals(tracado)
					&& !"2".equals(tracado)) {
				return Boolean.FALSE;
			}

			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return Boolean.FALSE;
			}
			piloto.setAtivarDRS(true);
			return Boolean.valueOf(piloto.mudarTracado(Integer.parseInt(tracado),
					obterJogoPeloNome(sessaoCliente.getJogoAtual())));
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Boolean.FALSE;
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
			for (Iterator iter = piList.iterator(); iter.hasNext(); ) {
				Piloto piloto = (Piloto) iter.next();
				if (clientPaddockPack.getSessaoCliente().getToken()
						.equals(piloto.getTokenJogador())) {
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
				.setNumVoltas(jogoServidor.getDadosCriarJogo().getQtdeVoltas().longValue());
		srvPaddockPack.setDetalhesJogo(detalhesJogo);
		return srvPaddockPack;
	}

	public Object driveThru(ClientPaddockPack clientPaddockPack) {
		/*
		 * JogoServidor jogoServidor = obterJogoPeloNome(
		 * clientPaddockPack.getNomeJogo()); if (jogoServidor == null) { return
		 * null; } String requisitorDriveThru =
		 * clientPaddockPack.getSessaoCliente() .getNomeJogador(); String
		 * jogadorDriveTru = (String) clientPaddockPack.getDataObject(); if
		 * (Util.isNullOrEmpty(jogadorDriveTru)) { Logger.logar(
		 * "jogadorDriveTru null"); return null; } Piloto piloto =
		 * obterPiloto(clientPaddockPack, jogoServidor); if (piloto == null) {
		 * return null; } int metadeJogadores = jogoServidor.getNumJogadores() /
		 * 2; if (piloto.adicionaVotoDriveThru(requisitorDriveThru)) { if
		 * (piloto.getVotosDriveThru() > (metadeJogadores)) {
		 * piloto.setDriveThrough(true); jogoServidor.infoPrioritaria(
		 * Html.driveThru(Lang.msg("penalidadePilotoDriveThru", new
		 * String[]{Html.vermelho(jogadorDriveTru),
		 * Html.negrito(requisitorDriveThru), "" + piloto.getVotosDriveThru(),
		 * "" + (metadeJogadores + 1)}))); } else {
		 * jogoServidor.infoPrioritaria(
		 * Html.driveThru(Lang.msg("votoPilotoDriveThru", new
		 * String[]{Html.vermelho(jogadorDriveTru),
		 * Html.negrito(requisitorDriveThru), "" + piloto.getVotosDriveThru(),
		 * "" + (metadeJogadores + 1)}))); } }
		 */
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
				.get(clientPaddockPack.getSessaoCliente().getToken());
		dadosParticiparJogo
				.setCombustivel(Integer.valueOf(clientPaddockPack.getCombustBox()));
		dadosParticiparJogo.setTpPneu(clientPaddockPack.getTpPneuBox());
		dadosParticiparJogo.setAsa(clientPaddockPack.getAsaBox());
		return null;
	}

	public Object mudarDrs(SessaoCliente sessaoCliente, String idPiloto) {
		try {
			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return Boolean.FALSE;
			}
			piloto.setAtivarDRS(true);
			return Boolean.valueOf(Carro.MENOS_ASA.equals(piloto.getCarro().getAsa()));
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Boolean.FALSE;
	}

	public Object mudarErs(SessaoCliente sessaoCliente, String idPiloto) {
		try {
			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return Boolean.FALSE;
			}
			piloto.setAtivarErs(!piloto.isAtivarErs());
			return Boolean.valueOf(piloto.isAtivarErs());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Boolean.FALSE;
	}

	public Object boxPiloto(SessaoCliente sessaoCliente, String idPiloto,
							Boolean ativa, String pneu, Integer combustivel, String asa) {
		try {
			Piloto piloto = obterPilotoPorId(sessaoCliente, idPiloto);
			if (piloto == null) {
				return Boolean.FALSE;
			}
			if (!Carro.MAIS_ASA.equals(asa) && !Carro.MENOS_ASA.equals(asa)
					&& !Carro.ASA_NORMAL.equals(asa)) {
				return Boolean.FALSE;
			}
			if (!Carro.TIPO_PNEU_CHUVA.equals(pneu)
					&& !Carro.TIPO_PNEU_MOLE.equals(pneu)
					&& !Carro.TIPO_PNEU_DURO.equals(pneu)) {
				return Boolean.FALSE;
			}
			if (combustivel.intValue() > 100) {
				combustivel = Integer.valueOf(100);
			}
			if (combustivel.intValue() < 0) {
				combustivel = Integer.valueOf(0);
			}
			piloto.setBox(ativa.booleanValue());
			piloto.setTipoPneuBox(pneu);
			piloto.setQtdeCombustBox(combustivel.intValue());
			piloto.setAsaBox(asa);
			JogoServidor jogoServidor = obterJogoPeloNome(
					sessaoCliente.getJogoAtual());
			Map mapJogo = jogoServidor.getMapJogadoresOnline();
			DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogo
					.get(sessaoCliente.getToken());
			dadosParticiparJogo.setCombustivel(combustivel);
			dadosParticiparJogo.setTpPneu(pneu);
			dadosParticiparJogo.setAsa(asa);
			return Boolean.valueOf(piloto.isBox());
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return Boolean.FALSE;
	}

	public SrvPaddockPack obterDadosToken(String token) {
		try {
			List<SessaoCliente> clientes = dadosPaddock.getClientes();
			for (Iterator iterator = clientes.iterator(); iterator.hasNext(); ) {
				SessaoCliente sessaoCliente = (SessaoCliente) iterator.next();
				if (sessaoCliente.getToken().equals(token)) {
					SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
					srvPaddockPack.setSessaoCliente(sessaoCliente);
					return srvPaddockPack;
				}
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;

	}

	public Object equipe(SessaoCliente sessaoCliente) {
		Session session = controlePersistencia.getSession();
		try {
			return controleClassificacao.verCarreira(sessaoCliente.getToken(),
					session);
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public Object equipePilotoCarro(SessaoCliente sessaoCliente) {
		Session session = controlePersistencia.getSession();
		try {
			Piloto piloto = new Piloto();
			CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) controleClassificacao
					.verCarreira(sessaoCliente.getToken(), session);
			if (carreiraDadosSrv == null
					|| Util.isNullOrEmpty(carreiraDadosSrv.getNomeCarro())
					|| Util.isNullOrEmpty(carreiraDadosSrv.getNomePiloto())) {
				return null;
			}
			controlePersistencia.carreiraDadosParaPiloto(carreiraDadosSrv,
					piloto);
			return piloto;
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public Object gravarEquipe(SessaoCliente sessaoCliente, String idioma,
							   CarreiraDadosSrv equipe) {
		return controleClassificacao.atualizaCarreira(sessaoCliente.getToken(),
				equipe);
	}

	public ControleClassificacao getControleClassificacao() {
		return controleClassificacao;
	}

	public ControleCampeonatoServidor getControleCampeonatoServidor() {
		return controleCampeonatoServidor;
	}

	public ControlePersistencia getControlePersistencia() {
		return controlePersistencia;
	}

	public ControlePaddockServidor getControlePaddockServidor() {
		return controlePaddockServidor;
	}

}
