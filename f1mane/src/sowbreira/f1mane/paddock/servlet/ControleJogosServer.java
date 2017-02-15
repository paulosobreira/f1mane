package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.BufferTexto;
import sowbreira.f1mane.paddock.entidades.Comandos;
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
import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 18:21:11
 */
public class ControleJogosServer {
	private DadosPaddock dadosPaddock;
	private ControleClassificacao controleClassificacao;
	private Map mapaJogosCriados = new HashMap();
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

	public Map getMapaJogosCriados() {
		return mapaJogosCriados;
	}

	public void setMapaJogosCriados(Map mapaJogosCriados) {
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
			for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
					.hasNext();) {
				SessaoCliente element = (SessaoCliente) iter.next();
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
			jogoServidor.setNomeJogoServidor(Lang.msg("088") + " "
					+ (qtdeJogos++) + "-" + temporada.replaceAll("t", ""));
			mapaJogosCriados.put(clientPaddockPack.getSessaoCliente(),
					jogoServidor);
			gerarListaJogosCriados();

			jogoServidor.setControleClassificacao(controleClassificacao);
			jogoServidor.setControleJogosServer(this);
			jogoServidor
					.setControleCampeonatoServidor(controleCampeonatoServidor);
			SrvPaddockPack srvPaddockPack = new SrvPaddockPack();
			srvPaddockPack.setDadosCriarJogo(jogoServidor.getDadosCriarJogo());
			srvPaddockPack
					.setNomeJogoCriado(jogoServidor.getNomeJogoServidor());
			srvPaddockPack
					.setSessaoCliente(clientPaddockPack.getSessaoCliente());
			srvPaddockPack.setDadosPaddock(dadosPaddock);
			return srvPaddockPack;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
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
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
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
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("207", new String[]{nomeJogo}));
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
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = (SessaoCliente) iter.next();
			JogoServidor jogoServidor = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidor.getMapJogadoresOnline()
					.get(sessaoCliente.getNomeJogador()) != null) {
				return true;
			}
		}
		return false;
	}

	private JogoServidor obterJogoPeloNome(String nomeJogo) {
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = (SessaoCliente) iter.next();
			JogoServidor jogoServidorTemp = (JogoServidor) mapaJogosCriados
					.get(key);
			if (jogoServidorTemp.getNomeJogoServidor().equals(nomeJogo)) {
				return jogoServidorTemp;
			}
		}
		return null;
	}

	private JogoServidor obterJogoPeloNomeDono(String nomeDono) {
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente key = (SessaoCliente) iter.next();
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
		return 200;
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
	public Object obterPosicaoPilotos(String data) {
		JogoServidor jogoServidor = obterJogoPeloNome(data);
		if (jogoServidor == null) {
			return null;
		}
		if (jogoServidor.getMapaNosIds() == null) {
			return null;
		}
		List posisList = new ArrayList();
		List pilotos = jogoServidor.getPilotosCopia();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			Posis posis = new Posis();
			posis.idPiloto = piloto.getId();
			posis.tracado = piloto.getTracado();
			posis.autoPos = piloto.isAutoPos();
			posis.agressivo = piloto.isAgressivo();
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
		return pack.encode();

	}

	public Object mudarGiroMotor(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.getCarro().mudarGiroMotor(clientPaddockPack.getGiroMotor());
		return null;
	}

	public Object mudarModoBox(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
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
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
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
		dadosParciais.melhorVolta = jogoServidor.obterMelhorVolta();
		dadosParciais.voltaAtual = jogoServidor.getNumVoltaAtual();
		dadosParciais.clima = jogoServidor.getClima();
		dadosParciais.estado = jogoServidor.getEstado();
		List pilotos = jogoServidor.getPilotosCopia();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			dadosParciais.pilotsPonts[piloto.getId() - 1] = piloto
					.getPtosPista();
			if (piloto.isRecebeuBanderada()) {
				dadosParciais.pilotsTs[piloto.getId() - 1] = piloto
						.getTimeStampChegeda();
			} else if (piloto.decContTravouRodas()) {
				dadosParciais.pilotsTs[piloto.getId() - 1] = -2;
			} else if (piloto.getCarro().isRecolhido()
					|| Carro.PANE_SECA.equals(piloto.getCarro().getDanificado())
					|| Carro.EXPLODIU_MOTOR
							.equals(piloto.getCarro().getDanificado())) {
				dadosParciais.pilotsTs[piloto.getId() - 1] = -1;
			}

			if (args.length > 2
					&& piloto.getId() == Integer.parseInt(args[2])) {
				dadosParciais.peselMelhorVolta = piloto.obterVoltaMaisRapida();
				int contVolta = 1;
				List voltas = piloto.getVoltas();
				for (int i = voltas.size() - 1; i > -1; i--) {
					Volta volta = (Volta) voltas.get(i);
					if (contVolta == 1) {
						dadosParciais.peselUltima1 = volta;
					}
					if (contVolta == 2) {
						dadosParciais.peselUltima2 = volta;
					}
					if (contVolta == 3) {
						dadosParciais.peselUltima3 = volta;
					}
					if (contVolta == 4) {
						dadosParciais.peselUltima4 = volta;
					}
					if (contVolta == 5) {
						dadosParciais.peselUltima5 = volta;
					}
					contVolta++;
					if (contVolta > 5) {
						break;
					}

				}
				dadosParciais.nomeJogador = piloto.getNomeJogador();
				dadosParciais.dano = piloto.getCarro().getDanificado();
				dadosParciais.pselBox = piloto.isBox();
				dadosParciais.freiandoReta = piloto.isFreiandoReta();
				dadosParciais.podeUsarDRS = piloto.isPodeUsarDRS();
				dadosParciais.pselMotor = piloto.getCarro().getMotor();
				dadosParciais.pselStress = piloto.getStress();
				dadosParciais.cargaKers = piloto.getCarro().getCargaKers();
				dadosParciais.temperaturaMotor = piloto.getCarro()
						.getTemperaturaMotor();
				dadosParciais.pselDurAereofolio = piloto.getCarro()
						.getDurabilidadeAereofolio();
				dadosParciais.pselCombust = piloto.getCarro().getCombustivel();
				dadosParciais.pselPneus = piloto.getCarro().getPneus();
				dadosParciais.pselMaxPneus = piloto.getCarro()
						.getDurabilidadeMaxPneus();
				dadosParciais.pselTpPneus = piloto.getCarro().getTipoPneu();
				dadosParciais.pselAsa = piloto.getCarro().getAsa();
				dadosParciais.pselParadas = piloto.getQtdeParadasBox();
				dadosParciais.pselVelocidade = piloto.getVelocidadeExibir();
				dadosParciais.pselCombustBox = piloto.getQtdeCombustBox();
				dadosParciais.pselTpPneusBox = piloto.getTipoPneuBox();
				dadosParciais.pselModoPilotar = piloto.getModoPilotagem();
				dadosParciais.pselAsaBox = piloto.getAsaBox();
				dadosParciais.pselGiro = piloto.getCarro().getGiro();
				dadosParciais.vantagem = piloto.getVantagem();
			}
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnlineTexto();
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
		for (Iterator iter = mapaJogosCriados.keySet().iterator(); iter
				.hasNext();) {
			SessaoCliente element = (SessaoCliente) iter.next();
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setModoPilotagem(clientPaddockPack.getModoPilotagem());
		return null;
	}

	public Object mudarModoAutoPos(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.mudarAutoTracado();
		return null;
	}

	public Object mudarTracado(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(
				clientPaddockPack.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.mudarTracado(clientPaddockPack.getTracado(), jogoServidor);
		return null;
	}

	private Piloto acharPiloto(ClientPaddockPack clientPaddockPack,
			JogoServidor jogoServidor) {
		Piloto acharPiloto = acharPilotoLista(clientPaddockPack, jogoServidor);
		int cont = 0;
		while (acharPiloto == null && cont < 5) {
			acharPiloto = acharPilotoLista(clientPaddockPack, jogoServidor);
			cont++;
		}
		return acharPiloto;
	}

	private Piloto acharPilotoLista(ClientPaddockPack clientPaddockPack,
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarKers(
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarKers(false);
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarKers(false);
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
		if (piloto == null) {
			return null;
		}
		piloto.setAtivarDRS(true);
		piloto.setAtivarKers(true);
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
		Piloto piloto = acharPiloto(clientPaddockPack, jogoServidor);
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
}
