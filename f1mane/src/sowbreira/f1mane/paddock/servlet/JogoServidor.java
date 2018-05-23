package sowbreira.f1mane.paddock.servlet;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.ControleCorrida;
import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.paddock.entidades.BufferTexto;
import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DetalhesJogo;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.paddock.entidades.TOs.VoltaJogadorOnline;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 18:28:27
 */
public class JogoServidor extends ControleJogoLocal implements InterfaceJogo {

	private String nomeJogoServidor;
	private String nomeCriador;
	private String tokenCriador;
	private long tempoCriacao, tempoInicio, tempoFim;
	/* mapJogadoresOnline.put(token, dadosParticiparJogo) */
	private Map<String, DadosCriarJogo> mapJogadoresOnline = new HashMap<String, DadosCriarJogo>();
	private Map<String, BufferTexto> mapJogadoresOnlineTexto = new HashMap<String, BufferTexto>();
	/* Chave numVolta , valor lista de VoltaJogadorOnline */
	private Map mapVoltasJogadoresOnline = new HashMap();
	private int contadorVolta = 0;
	private DadosCriarJogo dadosCriarJogo;
	private String estado = Comandos.ESPERANDO_JOGO_COMECAR;
	private int luzes = 5;
	private ControleJogosServer controleJogosServer;
	private ControleClassificacao controleClassificacao;
	private ControleCampeonatoServidor controleCampeonatoServidor;
	private boolean disparouInicio;
	private TravadaRoda travadaRoda;

	@Override
	public int hashCode() {
		return nomeJogoServidor.hashCode();
	}

	public boolean isCorridaIniciada() {
		return disparouInicio;
	}

	public void setControleCampeonatoServidor(
			ControleCampeonatoServidor controleCampeonatoServidor) {
		this.controleCampeonatoServidor = controleCampeonatoServidor;
	}

	public void processaNovaVolta() {
		super.processaNovaVolta();
		List voltasJogadoresOnline = new ArrayList();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.isJogadorHumano()) {
				VoltaJogadorOnline voltaJogadorOnline = new VoltaJogadorOnline();
				voltaJogadorOnline.setJogador(piloto.getTokenJogador());
				voltaJogadorOnline.setPiloto(piloto.getNome());
				voltasJogadoresOnline.add(voltaJogadorOnline);
			}
		}
		mapVoltasJogadoresOnline.put(new Integer(contadorVolta++),
				voltasJogadoresOnline);
	}

	public String getNomeCriador() {
		return nomeCriador;
	}

	public void setNomeCriador(String nomeCriador) {
		this.nomeCriador = nomeCriador;
	}

	public void setarNivelCorrida() {
		if (ControleJogoLocal.FACIL.equals(getNivelCorrida())) {
			niveljogo = FACIL_NV;
		} else if (ControleJogoLocal.DIFICIL.equals(getNivelCorrida())) {
			niveljogo = DIFICIL_NV;
		}
	}

	public JogoServidor(String temporada, DadosCriarJogo dadosCriarJogo)
			throws Exception {
		super(temporada);
		this.dadosCriarJogo = dadosCriarJogo;
		controleEstatisticas = new ControleEstatisticas(JogoServidor.this);
		processarEntradaDados();
		carregaRecursos((String) getCircuitos().get(circuitoSelecionado));
		setarNivelCorrida();
		controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
				diffultrapassagem.intValue());
		controleCorrida.getControleClima()
				.gerarClimaInicial(new Clima(dadosCriarJogo.getClima()));
	}

	public String getEstado() {
		return estado;
	}

	public Map<String, BufferTexto> getMapJogadoresOnlineTexto() {
		return mapJogadoresOnlineTexto;
	}

	public void setControleJogosServer(
			ControleJogosServer controleJogosServer) {
		this.controleJogosServer = controleJogosServer;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosCriarJogo;
	}

	public void setDadosCriarJogo(DadosCriarJogo dadosCriarJogo) {
		this.dadosCriarJogo = dadosCriarJogo;
	}

	public Map<String, DadosCriarJogo> getMapJogadoresOnline() {
		return mapJogadoresOnline;
	}

	public void setMapJogadoresOnline(Map mapJogadoresOnline) {
		this.mapJogadoresOnline = mapJogadoresOnline;
	}

	public String getNomeJogoServidor() {
		return nomeJogoServidor;
	}

	public void setNomeJogoServidor(String nomeJogoServidor) {
		this.nomeJogoServidor = nomeJogoServidor;
	}

	public long getTempoCriacao() {
		return tempoCriacao;
	}

	public void setTempoCriacao(long tempoCriacao) {
		this.tempoCriacao = tempoCriacao;
	}

	public Object adicionarJogador(SessaoCliente sessaoCliente,
			DadosCriarJogo dadosParticiparJogo) {
		for (Iterator iter = mapJogadoresOnline.keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			DadosCriarJogo valor = (DadosCriarJogo) mapJogadoresOnline.get(key);
			if (dadosParticiparJogo.getIdPiloto() == valor.getIdPiloto()) {
				for (Iterator iter2 = pilotos.iterator(); iter2.hasNext();) {
					Piloto piloto = (Piloto) iter2.next();
					if (piloto.getId() == dadosParticiparJogo.getIdPiloto()) {
						if (sessaoCliente.isGuest()) {
							return new MsgSrv(
									Lang.msg("escolhidoPorOutroJogador",
											new String[]{piloto.getNome()}));
						} else {
							return new MsgSrv(Lang.msg("257",
									new String[]{piloto.getNome(), key}));
						}
					}
				}
			}
		}
		boolean pilotoDisponivel = false;
		Piloto pilotoSelecionado = null;
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.getId() == dadosParticiparJogo.getIdPiloto()) {
				pilotoDisponivel = true;
				pilotoSelecionado = piloto;
			}
			if (piloto.getId() == dadosParticiparJogo.getIdPiloto()
					&& piloto.isDesqualificado()) {
				return new MsgSrv(
						Lang.msg("258", new String[]{piloto.getNome()}));
			}
		}
		if (pilotoDisponivel) {
			mapJogadoresOnline.put(sessaoCliente.getToken(),
					dadosParticiparJogo);
			mapJogadoresOnlineTexto.put(sessaoCliente.getToken(),
					new BufferTexto());
		} else {
			return new MsgSrv(Lang.msg("260",
					new String[]{dadosParticiparJogo.getPiloto()}));

		}
		dadosCriarJogo.setPilotosCarreira(pilotos);
		return null;
	}

	public void preencherDetalhes(DetalhesJogo detalhesJogo) {
		Map detMap = detalhesJogo.getJogadoresPilotos();
		for (Iterator iter = mapJogadoresOnline.keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			DadosCriarJogo valor = (DadosCriarJogo) mapJogadoresOnline.get(key);
			// TODO Token
			CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
					.obterCarreiraSrv(key);
			String piloto = "";
			if (carreiraDadosSrv != null && carreiraDadosSrv.isModoCarreira()) {
				piloto = carreiraDadosSrv.getNomePiloto();
			} else {
				piloto = valor.getPiloto();
			}
			detMap.put(key, piloto);
		}
		detalhesJogo.setDadosCriarJogo(getDadosCriarJogo());
		detalhesJogo.setTempoCriacao(getTempoCriacao());
		detalhesJogo.setNomeCriador(getNomeCriador());
		detalhesJogo.setDadosCriarJogo(dadosCriarJogo);
	}

	protected void processarEntradaDados() throws Exception {
		try {
			this.nivelCorrida = dadosCriarJogo.getNivelCorrida();

			qtdeVoltas = dadosCriarJogo.getQtdeVoltas();
			if (qtdeVoltas.intValue() <= Constantes.MIN_VOLTAS) {
				qtdeVoltas = new Integer(Constantes.MIN_VOLTAS);
			}
			if (qtdeVoltas.intValue() >= Constantes.MAX_VOLTAS) {
				qtdeVoltas = new Integer(Constantes.MAX_VOLTAS);
			}
			diffultrapassagem = dadosCriarJogo.getDiffultrapassagem();
			circuitoSelecionado = dadosCriarJogo.getCircuitoSelecionado();
			reabastacimento = dadosCriarJogo.isReabastecimento();
			trocaPneu = dadosCriarJogo.isTrocaPneu();
			ers = dadosCriarJogo.isErs();
			drs = dadosCriarJogo.isDrs();
			safetyCar = dadosCriarJogo.isSafetyCar();
		} catch (Exception e) {
			Logger.topExecpts(e);
		}

	}

	@Override
	public boolean isSemReabastacimento() {
		if (dadosCriarJogo != null)
			return !dadosCriarJogo.isReabastecimento();
		return super.isSemReabastacimento();
	}

	@Override
	public boolean isSemTrocaPneu() {
		if (dadosCriarJogo != null)
			return !dadosCriarJogo.isTrocaPneu();
		return super.isSemTrocaPneu();
	}

	public void iniciarJogo() throws Exception {
		if (disparouInicio) {
			return;
		}
		disparouInicio = true;
		gerenciadorVisual = null;
		atualizarJogadoresOnlineCarreira();
		Logger.logar("atualizarJogadoresOnlineCarreira();");
		atualizarJogadoresOnline();
		Logger.logar("atualizarJogadoresOnline();");
		controleCorrida.gerarGridLargada();
		Logger.logar("controleCorrida.gerarGridLargada();");
		setEstado(Comandos.MOSTRANDO_QUALIFY);
		List carrobox = new ArrayList();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (!carrobox.contains(piloto.getCarro())) {
				carrobox.add(piloto.getCarro());
			}
			if (piloto.isJogadorHumano()) {
				CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
						.obterCarreiraSrv(piloto.getTokenJogador());
				if (carreiraDadosSrv != null
						&& carreiraDadosSrv.isModoCarreira()) {
					piloto.getCarro().setImg(null);
				}
			}
		}
		controleCorrida.getControleBox().geraBoxesEquipes(carrobox);
		Thread timer = new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
					tempoInicio = System.currentTimeMillis();
					controleCorrida.iniciarCiclos();
					controleEstatisticas.inicializarThreadConsumidoraInfo();
				} catch (Exception e) {
					Logger.topExecpts(e);
				}

			}

		});
		timer.start();
	}

	public String getNivelCorrida() {
		return dadosCriarJogo.getNivelCorrida();
	}

	public double getNiveljogo() {
		if (InterfaceJogo.DIFICIL.equals(dadosCriarJogo.getNivelCorrida())) {
			return InterfaceJogo.DIFICIL_NV;
		}
		if (InterfaceJogo.FACIL.equals(dadosCriarJogo.getNivelCorrida())) {
			return InterfaceJogo.FACIL_NV;
		}
		if (InterfaceJogo.NORMAL.equals(dadosCriarJogo.getNivelCorrida())) {
			return InterfaceJogo.MEDIO_NV;
		}
		return InterfaceJogo.MEDIO_NV;
	}

	public void atualizarJogadoresOnline() {
		for (Iterator<String> iter = mapJogadoresOnline.keySet()
				.iterator(); iter.hasNext();) {
			String key = iter.next();
			SrvPaddockPack srvPaddockPack = controleJogosServer
					.obterDadosToken(key);
			DadosCriarJogo dadosParticiparJogo = mapJogadoresOnline.get(key);
			for (Iterator<Piloto> iterator = pilotos.iterator(); iterator
					.hasNext();) {
				Piloto piloto = iterator.next();
				if (piloto.getNome().equals(dadosParticiparJogo.getPiloto())
						|| piloto.getId() == dadosParticiparJogo
								.getIdPiloto()) {
					piloto.setNomeJogador(
							srvPaddockPack.getSessaoCliente().getNomeJogador());
					piloto.setImgJogador(srvPaddockPack.getSessaoCliente()
							.getImagemJogador());
					piloto.setTokenJogador(
							srvPaddockPack.getSessaoCliente().getToken());
					piloto.setJogadorHumano(true);
				}
				if (piloto.isJogadorHumano() && mapJogadoresOnline
						.get(piloto.getTokenJogador()) == null) {
					piloto.setNomeJogador(null);
					piloto.setImgJogador(null);
					piloto.setJogadorHumano(false);
				}
			}
		}

	}

	private void atualizarJogadoresOnlineCarreira() {
		for (Iterator iter = mapJogadoresOnline.keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogadoresOnline
					.get(key);
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.getNome().equals(dadosParticiparJogo.getPiloto())) {
					CarreiraDadosSrv carreiraDadosSrv = controleClassificacao
							.obterCarreiraSrv(key);
					if (carreiraDadosSrv.isModoCarreira()) {
						piloto.setNome(carreiraDadosSrv.getNomePiloto());
						dadosParticiparJogo
								.setPiloto(carreiraDadosSrv.getNomePiloto());
						piloto.setHabilidade(
								(int) (carreiraDadosSrv.getPtsPiloto()));
						piloto.getCarro()
								.setNome(carreiraDadosSrv.getNomeCarro());
						piloto.setNomeCarro(carreiraDadosSrv.getNomeCarro());
						piloto.getCarro()
								.setPotencia(carreiraDadosSrv.getPtsCarro());
						piloto.getCarro().setCor1(carreiraDadosSrv.geraCor1());
						piloto.getCarro().setCor2(carreiraDadosSrv.geraCor2());
					}
				}
			}
			dadosParticiparJogo.setPilotosCarreira(pilotos);

		}
	}

	public String getTipoPneuBox(Piloto piloto) {
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogadoresOnline
				.get(piloto.getTokenJogador());
		if (dadosParticiparJogo == null) {
			piloto.setNomeJogador(null);
			piloto.setTokenJogador(null);
			piloto.setJogadorHumano(false);
			return Carro.TIPO_PNEU_DURO;
		}
		return dadosParticiparJogo.getTpPnueu();
	}

	public String getAsaBox(Piloto piloto) {
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogadoresOnline
				.get(piloto.getTokenJogador());
		if (dadosParticiparJogo == null) {
			piloto.setNomeJogador(null);
			piloto.setJogadorHumano(false);
			return Carro.ASA_NORMAL;
		}
		return dadosParticiparJogo.getAsa();
	}

	public Integer getCombustBox(Piloto piloto) {
		DadosCriarJogo dadosParticiparJogo = (DadosCriarJogo) mapJogadoresOnline
				.get(piloto.getTokenJogador());
		if (dadosParticiparJogo == null) {
			piloto.setNomeJogador(null);
			piloto.setTokenJogador(null);
			piloto.setJogadorHumano(false);
			return new Integer(100);
		}
		return dadosParticiparJogo.getCombustivel();
	}

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
			Object combust, Object asa) {
		return super.setUpJogadorHumano(pilotoJogador, tpPneu, combust, asa);
	}

	public void apagarLuz() {
		setEstado(Comandos.LUZES);
		if (luzes == 5) {
			try {
				Thread.sleep(4000);
			} catch (Exception e) {
				Logger.topExecpts(e);
			}
		}
		this.luzes--;
		if (luzes <= 0) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				Logger.topExecpts(e);
			}
			setEstado(Comandos.CORRIDA_INICIADA);
		}
	}

	public void adicionarInfoDireto(String info) {
		infoPrioritaria(info);
	}

	public void info(String info) {
		if (isModoQualify()) {
			return;
		}
		for (Iterator iter = mapJogadoresOnline.keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			BufferTexto bufferTexto = (BufferTexto) mapJogadoresOnlineTexto
					.get(key);
			if (bufferTexto != null) {
				bufferTexto.adicionarTexto(info);
			}

		}
	}

	public void infoPrioritaria(String info) {
		if (isModoQualify()) {
			return;
		}
		for (Iterator iter = mapJogadoresOnline.keySet().iterator(); iter
				.hasNext();) {
			String key = (String) iter.next();
			BufferTexto bufferTexto = (BufferTexto) mapJogadoresOnlineTexto
					.get(key);
			if (bufferTexto != null) {
				bufferTexto.adicionarTextoPrio(info);
			}

		}
	}

	public void exibirResultadoFinal() {
		controleCorrida.pararThreads();
		Thread timer = new Thread(new Runnable() {
			public void run() {
				try {
					setEstado(Comandos.MOSTRA_RESULTADO_FINAL);
					tempoFim = System.currentTimeMillis();
					try {
						for (Iterator iter = mapJogadoresOnline.keySet()
								.iterator(); iter.hasNext();) {
							String key = (String) iter.next();
							SrvPaddockPack obterDadosToken = controleJogosServer
									.obterDadosToken(key);
							if (obterDadosToken != null) {
								obterDadosToken.getSessaoCliente()
										.limpaSelecao();
							}
						}
						controleClassificacao.processaCorrida(tempoInicio,
								tempoFim, mapVoltasJogadoresOnline, pilotos,
								dadosCriarJogo);
						if (!Util.isNullOrEmpty(
								dadosCriarJogo.getNomeCampeonato())) {
							controleCampeonatoServidor.processaCorrida(
									tempoInicio, tempoFim,
									mapVoltasJogadoresOnline, pilotos,
									dadosCriarJogo, controleClassificacao);
						}
					} catch (Exception e) {
						Logger.topExecpts(e);
					}

					Thread.sleep(10000);
					controleEstatisticas.setConsumidorAtivo(false);
					controleJogosServer.removerJogo(JogoServidor.this);
				} catch (Exception e) {
					Logger.topExecpts(e);
				}

			}

		});
		timer.start();
	}

	public void atualizaIndexTracadoPilotos() {
		atualizarJogadoresOnline();
		super.atualizaIndexTracadoPilotos();
	}

	public void informaMudancaClima() {
	}

	public void atulizaTabelaPosicoes() {
	}

	public boolean removerJogador(String token) {
		if (token == null) {
			return false;
		}
		if (!mapJogadoresOnline.containsKey(token)) {
			return false;
		}
		List pilotos = getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (token.equals(piloto.getTokenJogador())) {
				piloto.setNomeJogador(null);
				piloto.setTokenJogador(null);
				piloto.setImgJogador(null);
				piloto.setJogadorHumano(false);
				mapJogadoresOnline.remove(token);
				return true;
			}
		}
		return false;

	}

	public Map getMapVoltasJogadoresOnline() {
		return mapVoltasJogadoresOnline;
	}

	public void setControleClassificacao(
			ControleClassificacao controleClassificacao) {
		this.controleClassificacao = controleClassificacao;
	}

	@Override
	public void travouRodas(Piloto piloto) {
		if (piloto.isRecebeuBanderada()) {
			return;
		}
		if (isChovendo()) {
			return;
		}
		if (piloto.getPtosBox() != 0) {
			return;
		}
		this.travadaRoda = new TravadaRoda();
		this.travadaRoda.setIdNo(mapaNosIds.get(piloto.getNoAtual()));
		this.travadaRoda.setTracado(piloto.getTracado());
		piloto.setTravouRodas(true);
	}

	public TravadaRoda getTravadaRoda() {
		return travadaRoda;
	}

	public int getNumJogadores() {
		int cont = 0;
		List piList = getPilotos();
		for (Iterator iter = piList.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.isJogadorHumano()) {
				cont++;
			}
		}
		return cont;
	}

	public BufferedImage obterCarroCimaSemAreofolio(Piloto piloto) {
		return carregadorRecursos.obterCarroCimaSemAreofolio(piloto,
				getTemporada());
	}

	public void encerraCorrida() {
		if (!isCorridaTerminada()) {
			infoPrioritaria(Html
					.vinho(Lang.msg("024", new Object[]{getNumVoltaAtual()})));
			controleCorrida.terminarCorrida();
		}
		setCorridaTerminada(true);
	}

	public String getTokenCriador() {
		return tokenCriador;
	}

	public void setTokenCriador(String tokenCriador) {
		this.tokenCriador = tokenCriador;
	}

}
