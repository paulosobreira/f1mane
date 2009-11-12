package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import sowbreira.f1mane.controles.InterfaceJogo;
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
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 18:21:11
 */
public class ControleJogosServer {
	private DadosPaddock dadosPaddock;
	private ControleClassificacao controleClassificacao;
	private Map mapaJogosCriados = new HashMap();
	public static int MaxJogo = 4;
	public static int qtdeJogos = 0;

	/**
	 * @param dadosPaddock
	 */
	public ControleJogosServer(DadosPaddock dadosPaddock,
			ControleClassificacao controleClassificacao) {
		super();
		this.dadosPaddock = dadosPaddock;
		this.controleClassificacao = controleClassificacao;
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
		if (mapaJogosCriados.size() > MaxJogo) {
			return new MsgSrv(Lang.msg("204", new Object[] { MaxJogo + 1 }));
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

			jogoServidor = new JogoServidor(temporada);
			CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
					.verCarreira(clientPaddockPack);
			if (carreiraDadosSrv.isModoCarreira()) {
				if (InterfaceJogo.DIFICIL.equals(clientPaddockPack
						.getDadosJogoCriado().getNivelCorrida())
						&& verificaExcedePotencia(jogoServidor
								.getMediaPontecia(), carreiraDadosSrv
								.getPtsCarro(), jogoServidor.getNiveljogo())) {
					return new MsgSrv(Lang.msg("261"));
				}
			}
			jogoServidor.setNomeCriador(clientPaddockPack.getSessaoCliente()
					.getNomeJogador());
			jogoServidor.setTempoCriacao(System.currentTimeMillis());
		} catch (Exception e) {
			Logger.logarExept(e);
			ErroServ erroServ = new ErroServ(e);
			return erroServ;
		}
		jogoServidor.setNomeJogoServidor(Lang.msg("088") + " " + (qtdeJogos++)
				+ "-" + temporada.replaceAll("t", ""));
		mapaJogosCriados
				.put(clientPaddockPack.getSessaoCliente(), jogoServidor);
		gerarListaJogosCriados();
		jogoServidor.prepararJogoOnline(clientPaddockPack.getDadosJogoCriado());
		jogoServidor.adicionarJogador(clientPaddockPack.getSessaoCliente()
				.getNomeJogador(), clientPaddockPack.getDadosJogoCriado());
		jogoServidor.setControleJogosServer(this);
		jogoServidor.setControleClassificacao(controleClassificacao);
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
			permitidoAcimaMedia = 100;
		}
		if (InterfaceJogo.MEDIO_NV == nivel) {
			permitidoAcimaMedia = 50;
		}
		return (mediaPontecia + permitidoAcimaMedia) < ptsCarro;
	}

	private void gerarListaJogosCriados() {
		List jogos = new ArrayList();
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
				.verCarreira(clientPaddockPack);
		if (carreiraDadosSrv.isModoCarreira()) {
			if (jogoServidor.isCorridaIniciada()) {
				return new MsgSrv(Lang.msg("247"));
			}
			if (jogoServidor.getNiveljogo() == InterfaceJogo.DIFICIL_NV
					&& verificaExcedePotencia(jogoServidor.getMediaPontecia(),
							carreiraDadosSrv.getPtsCarro(), jogoServidor
									.getNiveljogo())) {
				return new MsgSrv(Lang.msg("261"));
			}
		}

		if (jogoServidor.isCorridaTerminada()) {
			return new MsgSrv(Lang.msg("206", new String[] { nomeJogo }));
		}
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("207", new String[] { nomeJogo }));
		}
		Object retorno = jogoServidor.adicionarJogador(clientPaddockPack
				.getSessaoCliente().getNomeJogador(), clientPaddockPack
				.getDadosJogoCriado());
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
			if (jogoServidor.getMapJogadoresOnline().get(
					sessaoCliente.getNomeJogador()) != null) {
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
			return new MsgSrv(Lang.msg("207", new String[] { nomeJogo }));
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
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
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
		JogoServidor jogoServidor = obterJogoPeloNomeDono(clientPaddockPack
				.getSessaoCliente().getNomeJogador());
		if (jogoServidor == null) {
			return new MsgSrv(Lang.msg("208"));
		}
		try {
			jogoServidor.iniciarJogo();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
		return null;
	}

	public Object obterDadosJogo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		DadosJogo dadosJogo = new DadosJogo();
		dadosJogo.setMelhoVolta(jogoServidor.obterMelhorVolta());
		dadosJogo.setVoltaAtual(jogoServidor.getNumVoltaAtual());
		dadosJogo.setClima(jogoServidor.getClima());
		List pilotos = jogoServidor.getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.setMelhorVolta(null);
			Volta melhor = piloto.obterVoltaMaisRapida();
			piloto.setMelhorVolta(melhor);
		}
		dadosJogo.setCorridaTerminada(jogoServidor.isCorridaTerminada());
		dadosJogo.setPilotosList(pilotos);

		Map mapJogo = jogoServidor.getMapJogadoresOnlineTexto();
		BufferTexto bufferTexto = (BufferTexto) mapJogo.get(clientPaddockPack
				.getSessaoCliente().getNomeJogador());
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
		List posisList = new ArrayList();
		List pilotos = jogoServidor.getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			Posis posis = new Posis();
			posis.idPiloto = piloto.getId();
			posis.agressivo = piloto.isAgressivo();
			posis.idNo = ((Integer) jogoServidor.getMapaNosIds().get(
					piloto.getNoAtual())).intValue();
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
		if (jogoServidor.isSafetyCarNaPista()) {
			pack.safetyNoId = ((Integer) jogoServidor.getMapaNosIds().get(
					jogoServidor.getSafetyCar().getNoAtual())).intValue();
			pack.safetySair = jogoServidor.getSafetyCar().isVaiProBox();
		}
		return pack.encode();

	}

	public Object mudarModoAgressivo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		List piList = jogoServidor.getPilotos();
		for (Iterator iter = piList.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (clientPaddockPack.getSessaoCliente().getNomeJogador().equals(
					piloto.getNomeJogador())) {
				piloto.setAgressivo(!piloto.isAgressivo());
				break;
			}
		}
		return null;
	}

	public Object mudarGiroMotor(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		List piList = jogoServidor.getPilotos();
		for (Iterator iter = piList.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (clientPaddockPack.getSessaoCliente().getNomeJogador().equals(
					piloto.getNomeJogador())) {
				piloto.getCarro().mudarGiroMotor(
						clientPaddockPack.getGiroMotor());
				break;
			}
		}
		return null;
	}

	public Object mudarModoBox(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		List piList = jogoServidor.getPilotos();
		for (Iterator iter = piList.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (clientPaddockPack.getSessaoCliente().getNomeJogador().equals(
					piloto.getNomeJogador())) {
				if (!piloto.entrouNoBox()) {
					piloto.setBox(!piloto.isBox());
					piloto.setTipoPneuBox(clientPaddockPack.getTpPneuBox());
					piloto.setQtdeCombustBox(clientPaddockPack.getCombustBox());
					piloto.setAsaBox(clientPaddockPack.getAsaBox());
					break;
				}
			}
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogo
				.get(clientPaddockPack.getSessaoCliente().getNomeJogador());
		dadosParticiparJogo.setCombustivel(new Integer(clientPaddockPack
				.getCombustBox()));
		dadosParticiparJogo.setTpPnueu(clientPaddockPack.getTpPneuBox());
		dadosParticiparJogo.setAsa(clientPaddockPack.getAsaBox());
		return null;
	}

	public Object abandonarJogo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		if (jogoServidor == null) {
			return null;
		}
		if (clientPaddockPack.getSessaoCliente() == null) {
			return null;
		}
		Map mapJogo = jogoServidor.getMapJogadoresOnline();
		mapJogo.remove(clientPaddockPack.getSessaoCliente().getNomeJogador());
		jogoServidor.getMapJogadoresOnlineTexto().remove(
				clientPaddockPack.getSessaoCliente().getNomeJogador());

		jogoServidor.removerJogador(clientPaddockPack.getSessaoCliente()
				.getNomeJogador());

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
		List pilotos = jogoServidor.getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			dadosParciais.pilotsPonts[piloto.getId() - 1] = piloto
					.getPtosPista();

			if (args.length > 2 && piloto.getId() == Integer.parseInt(args[2])) {
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
				dadosParciais.pselMotor = piloto.getCarro().getMotor();
				dadosParciais.pselStress = piloto.getStress();
				dadosParciais.pselCombust = piloto.getCarro().getCombustivel();
				dadosParciais.pselPneus = piloto.getCarro().getPneus();
				dadosParciais.pselMaxPneus = piloto.getCarro()
						.getDurabilidadeMaxPneus();
				dadosParciais.pselTpPneus = piloto.getCarro().getTipoPneu();
				dadosParciais.pselAsa = piloto.getCarro().getAsa();
				dadosParciais.pselParadas = piloto.getQtdeParadasBox();
				dadosParciais.pselVelocidade = piloto.getVelocidade();
				dadosParciais.pselCombustBox = piloto.getQtdeCombustBox();
				dadosParciais.pselTpPneusBox = piloto.getTipoPneuBox();
				dadosParciais.pselModoPilotar = piloto.getModoPilotagem();
				dadosParciais.pselAsaBox = piloto.getAsaBox();
				dadosParciais.pselGiro = piloto.getCarro().getGiro();
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

	public Object mudarModoAutoAgressivo(ClientPaddockPack clientPaddockPack) {
		JogoServidor jogoServidor = obterJogoPeloNome(clientPaddockPack
				.getNomeJogo());
		List piList = jogoServidor.getPilotos();
		for (Iterator iter = piList.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (clientPaddockPack.getSessaoCliente().getNomeJogador().equals(
					piloto.getNomeJogador())) {
				piloto.setModoPilotagem(clientPaddockPack.getModoPilotagem());
				break;
			}
		}
		return null;
	}

}
