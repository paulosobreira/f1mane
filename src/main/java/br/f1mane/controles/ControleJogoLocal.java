package br.f1mane.controles;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.TemporadasDefauts;
import sowbreira.f1mane.entidades.Volta;
import br.f1mane.paddock.entidades.TOs.TravadaRoda;
import br.f1mane.paddock.servlet.JogoServidor;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.GerenciadorVisual;
import br.f1mane.visao.PainelTabelaResultadoFinal;

/**
 * @author Paulo Sobreira
 */
public class ControleJogoLocal extends ControleRecursos
		implements
			InterfaceJogo {
	protected Piloto pilotoSelecionado;
	protected Piloto pilotoJogador;

	protected GerenciadorVisual gerenciadorVisual;
	protected ControleCorrida controleCorrida;
	protected ControleEstatisticas controleEstatisticas;
	protected ControleCampeonato controleCampeonato;

	protected List pilotosJogadores = new ArrayList();
	protected double niveljogo = InterfaceJogo.MEDIO_NV;
	protected String nivelCorrida;
	protected boolean corridaTerminada;
	protected boolean trocaPneu;
	protected boolean reabastecimento;
	protected boolean ers;
	protected boolean drs;
	protected boolean safetyCar = true;

	protected boolean continuaCampeonato;

	protected Integer qtdeVoltas = null;
	protected Integer diffultrapassagem = null;
	protected String circuitoSelecionado = null;
	protected boolean atualizacaoSuave = true;

	private MainFrame mainFrame;

	public ControleJogoLocal(String temporada) throws Exception {
		super(temporada);
		if (!(this instanceof JogoServidor)) {
			gerenciadorVisual = new GerenciadorVisual(this);
		}
		controleEstatisticas = new ControleEstatisticas(this);
	}

	public static void main(String[] args) {
		long var = (long) Math.pow(2, 149);
		System.out.println(var);
	}

	public ControleJogoLocal() throws Exception {
		super();
		gerenciadorVisual = new GerenciadorVisual(this);
		controleEstatisticas = new ControleEstatisticas(this);
	}

	public ControleJogoLocal(MainFrame mainFrame) throws Exception {
		super();
		gerenciadorVisual = new GerenciadorVisual(this);
		controleEstatisticas = new ControleEstatisticas(this);
		controleCampeonato = new ControleCampeonato(mainFrame);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCombustBox(sowbreira.f1mane.entidades.Piloto)
	 */
	public Integer getCombustBox(Piloto piloto) {
		return piloto.getCombustJogador();
	}

	public boolean isErs() {
		return ers;
	}

	public void setErs(boolean ers) {
		this.ers = ers;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getTipoPneuBox(sowbreira.f1mane.entidades.Piloto)
	 */
	public String getTipoPneuBox(Piloto piloto) {
		return piloto.getTipoPneuJogador();
	}

	protected void setarNivelCorrida() {
		if (ControleJogoLocal.FACIL.equals(getNivelCorrida())) {
			niveljogo = FACIL_NV;
		} else if (ControleJogoLocal.DIFICIL.equals(getNivelCorrida())) {
			niveljogo = DIFICIL_NV;
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#setNiveljogo(double)
	 */
	public void setNiveljogo(double niveljogo) {
		this.niveljogo = niveljogo;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#isCorridaTerminada()
	 */
	public boolean isCorridaTerminada() {
		return corridaTerminada;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#setCorridaTerminada(boolean)
	 */
	public void setCorridaTerminada(boolean corridaTerminada) {
		this.corridaTerminada = corridaTerminada;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getNosDoBox()
	 */
	public List<No> getNosDoBox() {
		return nosDoBox;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getMainFrame()
	 */
	public MainFrame getMainFrame() {
		return mainFrame;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getNivelCorrida()
	 */
	public String getNivelCorrida() {
		return nivelCorrida;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#setNivelCorrida(java.lang.String)
	 */
	public void setNivelCorrida(String nivelCorrida) {
		this.nivelCorrida = nivelCorrida;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCircuito()
	 */
	public Circuito getCircuito() {
		return circuito;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getNosDaPista()
	 */
	public List getNosDaPista() {
		return nosDaPista;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCarros()
	 */
	public List getCarros() {
		return carros;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getPilotos()
	 */
	public List<Piloto> getPilotos() {
		return pilotos;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#matarTodasThreads()
	 */
	public void matarTodasThreads() {
		try {
			if (controleCorrida != null) {
				controleCorrida.finalize();
			}
			if (gerenciadorVisual != null) {
				gerenciadorVisual.finalize();
			}
			if (controleEstatisticas != null) {
				controleEstatisticas.setConsumidorAtivo(false);
				controleEstatisticas.finalize();
			}
			System.gc();
		} catch (Throwable e) {
			Logger.logarExept(e);
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaNivelJogo()
	 */
	public boolean verificaNivelJogo() {
		return Math.random() < getNiveljogo();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getClima()
	 */
	public String getClima() {
		if (controleCorrida != null)
			return controleCorrida.getControleClima().getClima();
		return null;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#atualizaIndexTracadoPilotos()
	 */
	public void atualizaIndexTracadoPilotos() {
		decrementaTracado();
		if (gerenciadorVisual != null) {
			gerenciadorVisual.voltaPilotoAutomaticaJogador();
		}
	}

	public void decrementaTracado() {
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.decIndiceTracado(this);
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#info(java.lang.String)
	 */
	public void info(String info) {
		if (isModoQualify()) {
			return;
		}
		controleEstatisticas.info(info);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#infoPrioritaria(java.lang.String)
	 */
	public void infoPrioritaria(String info) {
		if (isModoQualify()) {
			return;
		}
		controleEstatisticas.info(info, true);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#porcentagemCorridaConcluida()
	 */
	public int porcentagemCorridaConcluida() {
		if (controleCorrida == null) {
			return 0;
		}
		return controleCorrida.porcentagemCorridaConcluida();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getNumVoltaAtual()
	 */
	public int getNumVoltaAtual() {
		if (controleCorrida == null) {
			return 0;
		}
		return controleCorrida.voltaAtual();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#totalVoltasCorrida()
	 */
	public int totalVoltasCorrida() {
		if (controleCorrida == null) {
			return 0;
		}
		return controleCorrida.getQtdeTotalVoltas();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaBoxOcupado(sowbreira.f1mane.entidades.Carro)
	 */
	public boolean verificaBoxOcupado(Carro carro) {
		if (isModoQualify()) {
			return false;
		}
		return controleCorrida.verificaBoxOcupado(carro);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#calculaSegundosParaLider(sowbreira.f1mane.entidades.Piloto)
	 */
	public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
		return controleEstatisticas.calculaSegundosParaLider(pilotoSelecionado);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaUltimaVolta()
	 */
	public boolean verificaUltimaVolta() {
		if (isModoQualify() || controleCorrida == null) {
			return false;
		}
		return ((controleCorrida.getQtdeTotalVoltas()
				- 1) == getNumVoltaAtual());
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#processaVoltaRapida(sowbreira.f1mane.entidades.Piloto)
	 */
	public void processaVoltaRapida(Piloto piloto) {
		controleEstatisticas.processaVoltaRapida(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCicloAtual()
	 */
	public int getCicloAtual() {
		return controleCorrida.getCicloAtual();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaVoltaMaisRapidaCorrida(sowbreira.f1mane.entidades.Piloto)
	 */
	public void verificaVoltaMaisRapidaCorrida(Piloto piloto) {
		controleEstatisticas.verificaVoltaMaisRapida(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterIndicativoCorridaCompleta()
	 */
	public double obterIndicativoCorridaCompleta() {
		return (porcentagemCorridaConcluida() / 100.0) + 1;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterMelhorVolta()
	 */
	public Volta obterMelhorVolta() {
		return controleEstatisticas.getVoltaMaisRapida();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaUltraPassagem(sowbreira.f1mane.entidades.Piloto,
	 *      int)
	 */
	public void verificaAcidente(Piloto piloto) {
		controleCorrida.verificaAcidente(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getNiveljogo()
	 */
	public double getNiveljogo() {
		return niveljogo;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#efetuarSelecaoPilotoJogador(java.lang.Object,
	 *      java.lang.Object, java.lang.Object, java.lang.String)
	 */
	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
			Object combust, String nomeJogador, Object asa) {
		pilotoJogador = (Piloto) selec;
		pilotoJogador.setJogadorHumano(true);
		pilotoJogador.setNomeJogador(nomeJogador);
		pilotoJogador.setTipoPneuJogador((String) tpneu);
		pilotoJogador.setCombustJogador((Integer) combust);
		pilotoJogador.setAsaJogador((String) asa);
		pilotosJogadores.add(pilotoJogador);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#mudarModoBox()
	 */
	public boolean mudarModoBox() {
		if (pilotoJogador.getPtosBox() != 0) {
			return false;
		}
		if (pilotoJogador != null) {
			int porcentCombust = 50;
			String tpPneu = Carro.TIPO_PNEU_DURO;
			String tpAsa = Carro.ASA_NORMAL;
			setBoxJogadorHumano(tpPneu, porcentCombust, tpAsa);
			pilotoJogador.setBox(!pilotoJogador.isBox());
			return pilotoJogador.isBox();
		}
		return false;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#setBoxJogadorHumano(java.lang.Object,
	 *      java.lang.Object)
	 */
	public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa) {
		String tipoPneuJogador = (String) tpneu;
		Integer combustJogador = (Integer) combust;
		String asaJogador = (String) asa;
		if (pilotoJogador != null) {
			pilotoJogador.setTipoPneuJogador(tipoPneuJogador);
			pilotoJogador.setCombustJogador(combustJogador);
			pilotoJogador.setAsaJogador(asaJogador);
			pilotoJogador.setTipoPneuBox(tipoPneuJogador);
			pilotoJogador.setQtdeCombustBox(combustJogador);
			pilotoJogador.setAsaBox(asaJogador);
		}

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#selecionaPilotoJogador()
	 */
	public void selecionaPilotoJogador() {
		pilotoSelecionado = pilotoJogador;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#apagarLuz()
	 */
	public void apagarLuz() {
		gerenciadorVisual.apagarLuz();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#processaNovaVolta()
	 */
	public void processaNovaVolta() {
		int qtdeDesqualificados = 0;
		Piloto piloto = pilotos.get(0);

		if (piloto.getNumeroVolta() == (totalVoltasCorrida() - 1)
				&& (piloto.getPosicao() == 1) && !isCorridaTerminada()) {
			String nomeJogadorFormatado = piloto.nomeJogadorFormatado();
			if (Util.isNullOrEmpty(nomeJogadorFormatado)) {
				nomeJogadorFormatado = " ";
			}
			infoPrioritaria(Html.preto(piloto.getNome()) + Html.verde(
					Lang.msg("045", new String[]{nomeJogadorFormatado})));
		}

		for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext();) {
			piloto = iter.next();
			if (piloto.isDesqualificado()) {
				qtdeDesqualificados++;
			}
		}
		if (qtdeDesqualificados >= 10) {
			setCorridaTerminada(true);
			controleCorrida.terminarCorrida();
			infoPrioritaria(Html
					.vinho(Lang.msg("024", new Object[]{getNumVoltaAtual()})));
		}
		if (getNumVoltaAtual() == 2 && isDrs() && !isChovendo()
				&& !isSafetyCarNaPista()) {
			infoPrioritaria(Html.azul(Lang.msg("drsHabilitado")));
		}
		controleCorrida.getControleClima().processaPossivelMudancaClima();
		if (!isSafetyCarNaPista()) {
			Thread nvolta = new Thread(new Runnable() {
				public void run() {
					try {
						Thread.sleep(5000);
						controleEstatisticas.tabelaComparativa();
					} catch (Exception e) {
						Logger.logarExept(e);
					}
				}
			});
			nvolta.start();
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#isChovendo()
	 */
	public boolean isChovendo() {
		return Clima.CHUVA.equals(getClima());
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#informaMudancaClima()
	 */
	public void informaMudancaClima() {
		gerenciadorVisual.informaMudancaClima();

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#pausarJogo()
	 */
	public void pausarJogo() {
		info(Html.preto(controleCorrida.isCorridaPausada()
				? Lang.msg("025")
				: Lang.msg("026")));
		controleCorrida.setCorridaPausada(!controleCorrida.isCorridaPausada());

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterResultadoFinal()
	 */
	public PainelTabelaResultadoFinal obterResultadoFinal() {

		return gerenciadorVisual.getResultadoFinal();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#isSafetyCarNaPista()
	 */
	public boolean isSafetyCarNaPista() {
		if (controleCorrida == null)
			return false;
		return controleCorrida.isSafetyCarNaPista();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getSafetyCar()
	 */
	public SafetyCar getSafetyCar() {
		return controleCorrida.getSafetyCar();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#isSafetyCarVaiBox()
	 */
	public boolean isSafetyCarVaiBox() {
		if (controleCorrida != null) {
			return controleCorrida.isSafetyCarVaiBox();
		}
		return false;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterCarroNaFrente(sowbreira.f1mane.entidades.Piloto)
	 */
	public Carro obterCarroNaFrente(Piloto piloto) {
		return controleCorrida.obterCarroNaFrente(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterCarroAtras(sowbreira.f1mane.entidades.Piloto)
	 */
	public Carro obterCarroAtras(Piloto piloto) {
		return controleCorrida.obterCarroAtras(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#calculaSegundosParaProximo(sowbreira.f1mane.entidades.Piloto)
	 */
	public String calculaSegundosParaProximo(Piloto psel) {
		return controleEstatisticas.calculaSegundosParaProximo(psel);
	}

	public double calculaDiferencaParaProximoDouble(Piloto psel) {
		return controleEstatisticas.calculaDiferencaParaProximoDouble(psel);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getIndexVelcidadeDaPista()
	 */
	public double getIndexVelcidadeDaPista() {
		if (controleCorrida != null) {
			return controleCorrida.getIndexVelcidadeDaPista();
		}
		return 0;
	}

	@Override
	public void iniciarJogo() throws Exception {
		iniciarJogo(null);
	}

	/**
	 * @param campeonato
	 * @see sowbreira.f1mane.controles.InterfaceJogo#iniciarJogoSingle()
	 */
	public void iniciarJogo(ControleCampeonato controleCampeonato)
			throws Exception {
		this.controleCampeonato = controleCampeonato;
		Campeonato campeonato = null;
		if (controleCampeonato != null)
			campeonato = controleCampeonato.getCampeonato();
		if (gerenciadorVisual.iniciarJogoMulti(campeonato)) {
			processarEntradaDados();
			carregaRecursos((String) getCircuitos().get(circuitoSelecionado),
					gerenciadorVisual.getListaPilotosCombo(),
					gerenciadorVisual.getListaCarrosCombo());
			this.nivelCorrida = Lang.key(gerenciadorVisual
					.getComboBoxNivelCorrida().getSelectedItem().toString());
			setarNivelCorrida();
			controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
					diffultrapassagem.intValue());
			controleCorrida.getControleClima()
					.gerarClimaInicial((Clima) gerenciadorVisual
							.getComboBoxClimaInicial().getSelectedItem());
			controleCorrida.gerarGridLargada();
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleCorrida.iniciarCorrida();
			if (controleCampeonato != null) {
				controleCampeonato.iniciaCorrida(circuitoSelecionado);
			}
			controleEstatisticas.inicializarThreadConsumidoraInfo();
		}
		Logger.logar("Circuito Selecionado " + circuitoSelecionado);
		Logger.logar("porcentagemChuvaCircuito(circuitoSelecionado) "
				+ porcentagemChuvaCircuito(circuitoSelecionado));
		Logger.logar(
				"porcentagemChuvaCircuito() " + porcentagemChuvaCircuito());
	}

	@Override
	public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado, String clima) throws Exception {
		Map circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
		List pilotos = new ArrayList((Collection) circuitosPilotos
				.get("t" + campeonato.getTemporada()));
		Piloto pilotoSel = null;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (campeonato.getNomePiloto().equals(piloto.getNome())) {
				pilotoSel = piloto;
				break;
			}

		}
		iniciarJogoMenuLocal(campeonato.getCircuitoVez(),
				campeonato.getTemporada(), campeonato.getQtdeVoltas(),
				Util.intervalo(130, 370), clima, campeonato.getNivel(),
				pilotoSel, campeonato.isKers(), campeonato.isDrs(),
				campeonato.isTrocaPneus(), campeonato.isReabastecimento(),
				combustivelSelecionado, asaSelecionado, pneuSelecionado,campeonato.isSafetycar());
		this.controleCampeonato = new ControleCampeonato(campeonato, mainFrame);
		controleCampeonato.iniciaCorrida(campeonato.getCircuitoVez());
	}

	@Override
	public void iniciarJogoMenuLocal(String circuitoSelecionado,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean ers,
			boolean drs, boolean trocaPneus, boolean reabastecimento,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado,boolean safetycar) throws Exception {
		this.qtdeVoltas = new Integer(numVoltasSelecionado);
		this.diffultrapassagem = new Integer(turbulenciaSelecionado);
		this.reabastecimento = reabastecimento;
		this.trocaPneu = trocaPneus;
		this.circuitoSelecionado = circuitoSelecionado;
		this.ers = ers;
		this.drs = drs;
		this.safetyCar = safetycar;
		this.nivelCorrida = nivelSelecionado;
		setTemporada("t" + temporadaSelecionada);
		carregarPilotosCarros();
		carregaRecursos((String) getCircuitos().get(circuitoSelecionado));
		setarNivelCorrida();
		List<Piloto> pilotosList = getPilotos();
		for (Iterator iterator = pilotosList.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (piloto.equals(pilotoSelecionado)) {
				efetuarSelecaoPilotoJogador(piloto, pneuSelecionado,
						new Integer(combustivelSelecionado), "Fl-Mane",
						asaSelecionado);
				break;
			}
		}
		controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
				diffultrapassagem.intValue());
		controleCorrida.getControleClima()
				.gerarClimaInicial(new Clima(climaSelecionado));
		controleCorrida.gerarGridLargada();
		gerenciadorVisual.iniciarInterfaceGraficaJogo();
		controleCorrida.iniciarCorrida();
		controleEstatisticas.inicializarThreadConsumidoraInfo();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCircuitos()
	 */
	public Map<String, String> getCircuitos() {
		return circuitos;
	}

	protected void processarEntradaDados() throws Exception {
		try {
			qtdeVoltas = (Integer) gerenciadorVisual.getSpinnerQtdeVoltas()
					.getValue();
			if (qtdeVoltas.intValue() != 0) {
				if (qtdeVoltas.intValue() >= 72) {
					qtdeVoltas = new Integer(72);
				}
			}
			diffultrapassagem = (Integer) gerenciadorVisual
					.getSpinnerDificuldadeUltrapassagem().getValue();
			circuitoSelecionado = (String) gerenciadorVisual
					.getComboBoxCircuito().getSelectedItem();

			if (gerenciadorVisual.getReabastecimento().isSelected()) {
				reabastecimento = true;
			}
			if (gerenciadorVisual.getTrocaPneu().isSelected()) {
				trocaPneu = true;
			}
			if (gerenciadorVisual.getErs().isSelected()) {
				ers = true;
			}
			if (gerenciadorVisual.getDrs().isSelected()) {
				drs = true;
			}
			setTemporada("t" + gerenciadorVisual.getComboBoxTemporadas()
					.getSelectedItem());
		} catch (Exception e) {
			throw new Exception(Lang.msg("027"));
		}

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#exibirResultadoFinal()
	 */
	public void exibirResultadoFinal() {
		gerenciadorVisual.exibirResultadoFinal();
		if (Logger.ativo)
			mainFrame.exibirResultadoFinal(
					gerenciadorVisual.exibirResultadoFinal());
		controleCorrida.pararThreads();
		controleEstatisticas.setConsumidorAtivo(false);
		if (controleCampeonato != null) {
			Logger.logar(
					"controleCampeonato.processaFimCorrida(getPilotos());");
			controleCampeonato.processaFimCorrida(getPilotos());
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			Logger.logar((i + 1) + " Posicao " + piloto.getPosicao() + " - "
					+ piloto.getNome() + " Volta :" + piloto.getNumeroVolta()
					+ " Paradas Box :" + piloto.getQtdeParadasBox()
					+ " Vantagem :" + piloto.getVantagem());

		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#abandonar()
	 */
	public void abandonar() {
		if (pilotoJogador != null) {
			pilotoJogador.getCarro().setDanificado(Carro.ABANDONOU, this);
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#zerarMelhorVolta()
	 */
	public void zerarMelhorVolta() {
		controleEstatisticas.zerarMelhorVolta();

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#adicionarInfoDireto(java.lang.String)
	 */
	public void adicionarInfoDireto(String string) {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.adicionarInfoDireto(string);

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#atulizaTabelaPosicoes()
	 */
	public void atulizaTabelaPosicoes() {

	}

	public void selecionouPiloto(Piloto pilotoSelecionado) {
		this.pilotoSelecionado = pilotoSelecionado;
		if (pilotosJogadores.contains(pilotoSelecionado)) {
			pilotoJogador = pilotoSelecionado;
		}
		gerenciadorVisual.atualizaPilotoSelecionado();
	}

	public Piloto getPilotoSelecionado() {
		return pilotoSelecionado;
	}

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
			Object combust, Object asa) {
		if (asa == null) {
			controleCorrida.processarTipoAsaAutomatico(pilotoJogador);
			asa = pilotoJogador.getCarro().getAsa();
		}

		String tipoPneu = (String) tpPneu;
		Integer qtdeCombustPorcent = (Integer) combust;
		if (isSemReabastecimento() && qtdeCombustPorcent.intValue() < 75) {
			qtdeCombustPorcent = new Integer(75);
		}
		pilotoJogador.getCarro().trocarPneus(this, tipoPneu,
				controleCorrida.getDistaciaCorrida());
		int undsComnustAbastecer = (controleCorrida.getTanqueCheio()
				* qtdeCombustPorcent.intValue()) / 100;
		if (isSemReabastecimento() && isCorridaIniciada() && !isModoQualify()
				&& pilotoJogador.getNumeroVolta() >= 0) {
			undsComnustAbastecer = 0;
		}
		pilotoJogador.getCarro().setCombustivel(undsComnustAbastecer
				+ pilotoJogador.getCarro().getCombustivel());
		if (isDrs()) {
			pilotoJogador.getCarro().setAsa(Carro.MAIS_ASA);
		} else {
			String strAsa = (String) asa;
			if (!strAsa.equals(pilotoJogador.getCarro().getAsa())) {
				infoPrioritaria(
						Html.laranja(
								Lang.msg("028",
										new String[]{
												pilotoJogador
														.nomeJogadorFormatado(),
												pilotoJogador.getNome()})));
			}
			pilotoJogador.getCarro().setAsa(strAsa);
		}
		if (undsComnustAbastecer < 0) {
			undsComnustAbastecer = 0;
		}
		return undsComnustAbastecer;
	}

	public Volta obterMelhorVolta(Piloto pilotoSelecionado) {
		return pilotoSelecionado.obterVoltaMaisRapida();
	}

	public Piloto getPilotoJogador() {
		return pilotoJogador;
	}

	public void mudarGiroMotor(Object selectedItem) {
		String giroMotor = (String) selectedItem;
		if (pilotoJogador != null) {
			pilotoJogador.getCarro().mudarGiroMotor(giroMotor);
		}

	}

	public int calculaDiferencaParaProximo(Piloto piloto) {
		if (controleEstatisticas == null) {
			return 0;
		}
		return controleEstatisticas.calculaDiferencaParaProximo(piloto);
	}

	public void mudarModoPilotagem(String modo) {
		if (pilotoJogador != null)
			pilotoJogador.setModoPilotagem(modo);
	}

	public String getAsaBox(Piloto piloto) {
		return piloto.getAsaJogador();
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public boolean isModoQualify() {
		if (controleCorrida != null) {
			return controleCorrida.isModoQualify();
		}
		return false;
	}

	public void tabelaComparativa() {
		controleEstatisticas.tabelaComparativa();

	}

	public void iniciaJanela() {
		// TODO Auto-generated method stub

	}

	public boolean isCorridaIniciada() {
		if (controleCorrida == null) {
			return false;
		}
		return controleCorrida.isCorridaIniciada();
	}

	public int getMediaPontecia() {
		if (getCarros() == null) {
			return 0;
		}
		int somaPontecias = 0;
		for (int i = 0; i < getCarros().size(); i++) {
			Carro carro = (Carro) getCarros().get(i);
			somaPontecias += (carro.getPotencia() + carro.getFreios()
					+ carro.getAerodinamica());
		}
		int mediaPontecia = somaPontecias / (getCarros().size());
		return mediaPontecia;
	}

	@Override
	public void mudaPilotoSelecionado() {
		Piloto outro = null;
		for (int i = 0; i < pilotosJogadores.size(); i++) {
			Piloto pl = (Piloto) pilotosJogadores.get(i);
			if (pl.equals(pilotoSelecionado)) {
				if ((i + 1) < pilotosJogadores.size()) {
					outro = (Piloto) pilotosJogadores.get(i + 1);
				} else {
					outro = (Piloto) pilotosJogadores.get(0);
				}
				break;
			}
		}

	}

	public boolean isSemTrocaPneu() {
		return !trocaPneu;
	}

	public boolean isSemReabastecimento() {
		return !reabastecimento;
	}

	@Override
	public List getCarrosBox() {
		return controleCorrida.getControleBox().getCarrosBox();
	}

	@Override
	public void mudarTracado(int pos) {
		if (pilotoJogador == null)
			return;
		pilotoJogador.mudarTracado(pos, this);
	}

	/**
	 * Minimo 0.5 = Mais dificil de passar Maximo 1.0 = Mais facil de passar
	 */
	@Override
	public double getFatorUtrapassagem() {
		if (controleCorrida != null) {
			return controleCorrida.getFatorUtrapassagem();
		}
		return 0;
	}

	@Override
	public void mudarAutoPos(boolean autoPos) {
		if (pilotoJogador == null)
			return;

	}

	@Override
	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente) {
	}

	@Override
	public No getNoEntradaBox() {
		return controleCorrida.getControleBox().getEntradaBox();

	}

	@Override
	public void travouRodas(Piloto piloto) {
		travouRodas(piloto, false);
	}

	@Override
	public void travouRodas(Piloto piloto, boolean semFumaca) {
		if (piloto.isRecebeuBanderada()) {
			return;
		}
		if (isChovendo()) {
			return;
		}
		if (piloto.getPtosBox() != 0) {
			return;
		}
		double lim = 0.3;
		if (asfaltoAbrasivo()) {
			lim = 0.5;
		}
		if (Math.random() > lim) {
			return;
		}
		TravadaRoda travadaRoda = new TravadaRoda();
		travadaRoda.setIdNo(mapaNosIds.get(piloto.getNoAtual()));
		travadaRoda.setTracado(piloto.getTracado());
		int qtdeFumaca = 0;
		if (piloto.getNoAtual().verificaRetaOuLargada()) {
			qtdeFumaca = Util.intervalo(10, 25);
		} else if (piloto.getNoAtual().verificaCurvaAlta()) {
			qtdeFumaca = Util.intervalo(10, 30);
		} else if (piloto.getNoAtual().verificaCurvaBaixa()) {
			qtdeFumaca = Util.intervalo(10, 20);
		}
		if (semFumaca) {
			piloto.setMarcaPneu(true);
			piloto.setTravouRodas(0);
		} else {
			piloto.setTravouRodas(qtdeFumaca);
		}
		if (gerenciadorVisual != null)
			gerenciadorVisual.adicinaTravadaRoda(travadaRoda);

	}

	@Override
	public boolean verificaNoPitLane(Piloto piloto) {
		if(piloto==null){
			return false;
		}
		return piloto.getPtosBox() > 0;
	}

	public BufferedImage carregaBackGround(String backGround) {
		URL url = null;
		try {
			String caminho = mainFrame.getCodeBase()
					+ "sowbreira/f1mane/recursos/" + backGround;
			Logger.logar("Caminho Carregar Bkg " + caminho);
			url = new URL(caminho);
			BufferedImage buff = ImageIO.read(url.openStream());
			return buff;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	@Override
	public boolean mudarModoDRS() {
		if (pilotoJogador == null)
			return false;
		if (isChovendo()) {
			controleEstatisticas.info(Lang.msg("drsDesabilitado"));
			pilotoJogador.setAtivarDRS(false);
			return false;
		}
		if (getNumVoltaAtual() <= 1) {
			pilotoJogador.setAtivarDRS(false);
			return false;
		}
		pilotoJogador.setAtivarDRS(!pilotoJogador.isAtivarDRS());
		return pilotoJogador.isAtivarDRS();
	}

	@Override
	public boolean mudarModoKers() {
		if (pilotoJogador == null)
			return false;
		pilotoJogador.setAtivarErs(!pilotoJogador.isAtivarErs());
		return pilotoJogador.isAtivarErs();
	}

	@Override
	public int calculaDiferencaParaAnterior(Piloto piloto) {
		return controleEstatisticas.calculaDiferencaParaAnterior(piloto);
	}

	@Override
	public int percetagemDeVoltaConcluida(Piloto pilotoSelecionado) {
		if (circuito == null) {
			return 0;
		}
		if (pilotoSelecionado.getPtosBox() != 0) {
			return 0;
		}
		double pista = circuito.getPistaFull().size();
		double indexPiloto = pilotoSelecionado.getNoAtual().getIndex();
		return (int) ((indexPiloto / pista) * 100.0);
	}

	@Override
	public boolean verirficaDesafiandoCampeonato(Piloto piloto) {
		if (controleCampeonato != null) {
			Campeonato campeonato = controleCampeonato.getCampeonato();
			if (campeonato != null) {
				if (piloto.getNome().equals(campeonato.getRival())) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean verificaCampeonatoComRival() {
		if (controleCampeonato != null) {
			Campeonato campeonato = controleCampeonato.getCampeonato();
			if (campeonato != null) {
				return !Util.isNullOrEmpty(campeonato.getRival());
			}
		}
		return false;
	}

	@Override
	public String calculaSegundosParaRival(Piloto pilotoSelecionado) {
		String rival = null;
		if (controleCampeonato != null) {
			Campeonato campeonato = controleCampeonato.getCampeonato();
			if (campeonato != null) {
				rival = campeonato.getRival();
			}
		}
		if (Util.isNullOrEmpty(rival)) {
			return null;
		}
		Piloto pRival = null;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (piloto.getNome().equals(rival)) {
				pRival = piloto;
			}
		}

		long tempo = Constantes.CICLO;
		return controleEstatisticas.calculaSegundosParaRival(pilotoSelecionado,
				pRival, tempo);
	}

	@Override
	public void verificaDesafioCampeonatoPiloto() {
		if (controleCampeonato == null) {
			return;
		}
		controleCampeonato.verificaDesafioCampeonatoPiloto();

	}

	@Override
	public void aumentaFatorAcidade() {
		if (controleCorrida != null) {
			controleCorrida.aumentaFatorAcidade();
		}

	}

	@Override
	public void diminueFatorAcidade() {
		if (controleCorrida != null) {
			controleCorrida.diminueFatorAcidade();
		}
	}

	@Override
	public void setPontosPilotoLargada(long ptosPista) {
		controleCorrida.setPontosPilotoLargada(ptosPista);
	}

	@Override
	public boolean asfaltoAbrasivo() {
		if (controleCorrida != null) {
			return controleCorrida.asfaltoAbrasivo();
		}
		return false;
	}

	@Override
	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
			Piloto p) {
		return controleCorrida.ganhoComSafetyCar(ganho, controleJogo, p);
	}

	@Override
	public void driveThru() {
		// TODO Auto-generated method stub

	}

	@Override
	public int porcentagemChuvaCircuito() {
		return porcentagemChuvaCircuito(circuitoSelecionado);
	}

	@Override
	public boolean isBoxRapido() {
		if (controleCorrida == null) {
			return false;
		}
		return controleCorrida.getControleBox().isBoxRapido();
	}

	@Override
	public List<String> listaInfo() {
		if (gerenciadorVisual != null) {
			return gerenciadorVisual.getBufferTextual();
		}
		return new ArrayList<String>();
	}

	@Override
	public void forcaSafatyCar() {
		if (controleCorrida.isSafetyCarNaPista()) {
			return;
		}
		int i = 1;
		Piloto piloto = pilotos.get(pilotos.size() - i);
		while (Carro.BATEU_FORTE.equals(piloto.getCarro().getDanificado())) {
			piloto = pilotos.get(pilotos.size() - (++i));
		}
		piloto.getCarro().setDanificado(Carro.BATEU_FORTE, this);
		controleCorrida.safetyCarNaPista(piloto);
	}

	@Override
	public No obterProxCurva(No noAtual) {
		return mapaNoProxCurva.get(noAtual);
	}

	@Override
	public boolean verificaLag() {
		return false;
	}

	@Override
	public int getLag() {
		return 0;
	}

	@Override
	public int calculaDiffParaProximoRetardatario(Piloto piloto,
			boolean analisaTracado) {
		return controleEstatisticas.calculaDiffParaProximoRetardatario(piloto,
				analisaTracado);
	}

	@Override
	public No getNoSaidaBox() {
		return controleCorrida.getNoSaidaBox();
	}

	@Override
	public void selecionaPilotoCima() {
		gerenciadorVisual.selecionaPilotoCima();

	}

	@Override
	public void selecionaPilotoBaixo() {
		gerenciadorVisual.selecionaPilotoBaixo();

	}

	@Override
	public boolean isJogoPausado() {
		return controleCorrida.isCorridaPausada();
	}

	@Override
	public void descontaTempoPausado(Volta volta) {
		controleCorrida.descontaTempoPausado(volta);

	}

	@Override
	public void criarCampeonato() throws Exception {
		controleCampeonato.criarCampeonato();
	}

	@Override
	public void criarCampeonatoPiloto() throws Exception {
		controleCampeonato.criarCampeonatoPiloto();
	}

	@Override
	public Campeonato continuarCampeonato() {
		return controleCampeonato.continuarCampeonato();
	}

	@Override
	public Campeonato continuarCampeonatoXml() {
		return controleCampeonato.continuarCampeonatoXml();
	}

	@Override
	public void dadosPersistenciaCampeonato(Campeonato campeonato) {
		if (controleCampeonato != null && campeonato == null) {
			campeonato = controleCampeonato.getCampeonato();
		}
		ControleCampeonato.dadosPersistencia(campeonato, mainFrame);
	}

	@Override
	public void proximaCorridaCampeonato() {
		controleCampeonato.detalhesCorrida();
	}

	@Override
	public void climaChuvoso() {
		controleCorrida.climaChuvoso();

	}

	@Override
	public void climaLimpo() {
		controleCorrida.climaLimpo();

	}

	@Override
	public void ativaVerControles() {
		if (gerenciadorVisual != null)
			gerenciadorVisual.ativaVerControles();
	}

	public boolean verificaPistaEmborrachada() {
		double indicativoEmborrachamentoPista = .85;
		if (!isChovendo()) {
			double emborrachamento = porcentagemCorridaConcluida() / 200.0;
			if (emborrachamento > .4) {
				emborrachamento = .4;
			}
			indicativoEmborrachamentoPista -= emborrachamento;
		}
		return Math.random() > 0.5
				|| Math.random() > indicativoEmborrachamentoPista;
	}

	@Override
	public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabastecimento) {
		controleCampeonato = new ControleCampeonato(mainFrame);
		return controleCampeonato.criarCampeonatoPiloto(cirucitosCampeonato,
				temporadaSelecionada, numVoltasSelecionado,
				turbulenciaSelecionado, climaSelecionado, nivelSelecionado,
				pilotoSelecionado, kers, drs, trocaPneus, reabastecimento);
	}

	@Override
	public void voltaMenuPrincipal() {
		if (controleCampeonato != null) {
			controleCampeonato.continuarCampeonatoCache();
			matarTodasThreads();
			mainFrame.setCampeonato(controleCampeonato.getCampeonato());
			mainFrame.iniciar();
		} else {
			matarTodasThreads();
			mainFrame.iniciar();
		}
	}

	@Override
	public List<PilotosPontosCampeonato> geraListaPilotosPontos() {
		if (controleCampeonato == null) {
			return new ArrayList<PilotosPontosCampeonato>();
		}
		controleCampeonato.geraListaPilotosPontos();
		return controleCampeonato.getPilotosPontos();
	}

	@Override
	public List<ConstrutoresPontosCampeonato> geraListaContrutoresPontos() {
		if (controleCampeonato == null) {
			return new ArrayList<ConstrutoresPontosCampeonato>();
		}
		controleCampeonato.geraListaContrutoresPontos();
		return controleCampeonato.getContrutoresPontos();
	}

	@Override
	public void continuarCampeonato(Campeonato campeonato) {
		if (controleCampeonato == null) {
			controleCampeonato = new ControleCampeonato(mainFrame);
		}
		controleCampeonato.setCampeonato(campeonato);

	}

	@Override
	public Piloto obterRivalCampeonato() {
		if (controleCampeonato == null) {
			return null;
		}
		if (controleCampeonato.getCampeonato() == null) {
			return null;
		}
		if (controleCampeonato.getCampeonato().getRival() == null) {
			return null;
		}
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto p = (Piloto) iterator.next();
			if (p.getNome()
					.equals(controleCampeonato.getCampeonato().getRival())) {
				return p;
			}

		}
		return null;
	}

	@Override
	public Carro obterCarroNaFrenteRetardatario(Piloto piloto,
			boolean analisaTracado) {
		return controleCorrida.obterCarroNaFrenteRetardatario(piloto,
				analisaTracado);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#desenhaQualificacao()
	 */
	public void desenhaQualificacao() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			Logger.logarExept(e);
		}
		if (gerenciadorVisual != null) {
			gerenciadorVisual.setDesenhouCreditos(true);
		}
		desenhouQualificacao();
	}

	@Override
	public void desenhouQualificacao() {
		if (gerenciadorVisual != null) {
			try {
				while (gerenciadorVisual.naoDesenhouPilotosQualificacao()) {
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			}
			try {
				Thread.sleep(4000);
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			}
			gerenciadorVisual.setDesenhouQualificacao(true);
		} else {
			try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			}
		}
		selecionaPilotoJogador();
	}

	@Override
	public void detalhesCorridaCampeonato() {
		controleCampeonato.detalhesCorrida();
	}

	@Override
	public boolean safetyCarUltimas3voltas() {
		if (controleCorrida != null) {
			return controleCorrida.safetyCarUltimas3voltas();
		}
		return false;
	}

	@Override
	public double getFatorAcidente() {
		if (controleCorrida != null) {
			return controleCorrida.getFatorAcidente();
		}
		return 0;
	}

	@Override
	public boolean verificaInfoRelevante(Piloto piloto) {
		return controleEstatisticas.verificaInfoRelevante(piloto);
	}

	@Override
	public void processaMudancaEquipeCampeontato() {
		if (controleCampeonato != null) {
			controleCampeonato.processaMudancaEquipe();
		}

	}

	@Override
	public Campeonato continuarCampeonatoXmlDisco() {
		if (controleCampeonato != null) {
			return controleCampeonato.continuarCampeonatoXmlDisco();
		}
		return null;
	}

	@Override
	public No obterCurvaAnterior(No noAtual) {
		return mapaNoCurvaAnterior.get(noAtual);
	}

	@Override
	public int getFPS() {
		if (gerenciadorVisual != null) {
			return gerenciadorVisual.getFps();
		}
		return 0;
	}

	@Override
	public String calculaSegundosParaProximo(Piloto psel, int diferenca) {
		return controleEstatisticas.calculaSegundosParaProximo(psel, diferenca);
	}

	@Override
	public void pilotoSelecionadoMinimo() {
		if (pilotoJogador != null) {
			pilotoJogador.setModoPilotagem(Piloto.LENTO);
			pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_MIN);
			pilotoJogador.setAtivarErs(false);
		}

	}

	@Override
	public void pilotoSelecionadoNormal() {
		if (pilotoJogador != null) {
			pilotoJogador.setModoPilotagem(Piloto.NORMAL);
			pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_NOR);
			pilotoJogador.setAtivarErs(false);
		}

	}

	@Override
	public void pilotoSelecionadoMaximo() {
		if (pilotoJogador != null) {
			pilotoJogador.setModoPilotagem(Piloto.AGRESSIVO);
			pilotoJogador.getCarro().mudarGiroMotor(Carro.GIRO_MAX);
			pilotoJogador.setAtivarErs(true);
			pilotoJogador.setAtivarDRS(true);
		}

	}

	@Override
	public boolean mostraTipoPneuAdversario() {
		return true;
	}

	@Override
	public JPanel painelNarracao() {
		if (gerenciadorVisual != null) {
			return gerenciadorVisual.getPainelNarracaoText();
		}
		return null;
	}

	@Override
	public JPanel painelDebug() {
		if (controleEstatisticas != null) {
			return controleEstatisticas.getPainelDebug();
		}
		return null;
	}

	@Override
	public void atualizaInfoDebug() {
		if (controleEstatisticas != null) {
			controleEstatisticas.atualizaInfoDebug();
		}
	}

	@Override
	public void atualizaInfoDebug(StringBuilder buffer) {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory() / 1048576;
		long allocatedMemory = runtime.totalMemory() / 1048576;
		long freeMemory = runtime.freeMemory() / 1048576;

		buffer.append("MAXMEMORY :" + maxMemory + "<br>");
		buffer.append("ALLOCATEDMEMORY :" + allocatedMemory + "<br>");
		buffer.append("FREEMEMORY :" + freeMemory + "<br>");

		Field[] declaredFields = this.getClass().getDeclaredFields();
		List<String> campos = new ArrayList<String>();
		buffer.append("-=ControleJogo=- <br>");
		campos.add("asfaltoAbrasivo = " + this.asfaltoAbrasivo() + "<br>");
		campos.add("porcentagemChuvaCircuito = "
				+ this.porcentagemChuvaCircuito() + "<br>");
		campos.add("isBoxRapido = " + this.isBoxRapido() + "<br>");
		campos.add("verificaPistaEmborrachada = "
				+ this.verificaPistaEmborrachada() + "<br>");
		campos.add("porcentagemCorridaConcluida = "
				+ this.porcentagemCorridaConcluida() + "<br>");
		campos.add(
				"FatorUtrapassagem = " + this.getFatorUtrapassagem() + "<br>");
		campos.add("FatorAcidente = " + this.getFatorAcidente() + "<br>");
		campos.add("verificaNivelJogo = " + this.verificaNivelJogo() + "<br>");
		campos.add("NumVoltaAtual = " + this.getNumVoltaAtual() + "<br>");
		campos.add(
				"totalVoltasCorrida = " + this.totalVoltasCorrida() + "<br>");
		campos.add(
				"verificaUltimaVolta = " + this.verificaUltimaVolta() + "<br>");
		for (Field field : declaredFields) {
			try {
				Object object = field.get(this);
				String valor = "null";
				if (object != null) {
					if (!Util.isWrapperType(object.getClass())) {
						continue;
					}
					valor = object.toString();
				}
				campos.add(field.getName() + " = " + valor + "<br>");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		campos.add("Clima = " + this.getClima() + "<br>");
		campos.add("IndicativoCorridaCompleta = "
				+ this.obterIndicativoCorridaCompleta() + "<br>");
		campos.add("isChovendo = " + this.isChovendo() + "<br>");
		campos.add(
				"isSafetyCarNaPista = " + this.isSafetyCarNaPista() + "<br>");
		campos.add("isSafetyCarVaiBox = " + this.isSafetyCarVaiBox() + "<br>");
		campos.add("IndexVelcidadeDaPista = " + this.getIndexVelcidadeDaPista()
				+ "<br>");
		campos.add("isModoQualify = " + this.isModoQualify() + "<br>");
		campos.add("Temporada = " + this.getTemporada() + "<br>");
		campos.add("verificaCampeonatoComRival = "
				+ this.verificaCampeonatoComRival() + "<br>");
		campos.add("safetyCarUltimas3voltas = " + this.safetyCarUltimas3voltas()
				+ "<br>");
		campos.add("mostraTipoPneuAdversario = "
				+ this.mostraTipoPneuAdversario() + "<br>");
		campos.add(
				"isCorridaTerminada = " + this.isCorridaTerminada() + "<br>");
		campos.add("isCorridaIniciada = " + this.isCorridaIniciada() + "<br>");
		campos.add("MediaPontecia = " + this.getMediaPontecia() + "<br>");
		Collections.sort(campos, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return o1.toLowerCase().compareTo(o2.toLowerCase());
			}
		});
		for (Iterator<String> iterator = campos.iterator(); iterator
				.hasNext();) {
			buffer.append(iterator.next());
		}
	}

	@Override
	public void forcaQuerbraAereofolio(Piloto piloto) {
		if (piloto == null) {
			return;
		}
		piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO,
				this);
		piloto.getCarro().setDurabilidadeAereofolio(0);
	}

	public boolean isAtualizacaoSuave() {
		return atualizacaoSuave;
	}

	public void setAtualizacaoSuave(boolean atualizacaoSuave) {
		this.atualizacaoSuave = atualizacaoSuave;
	}

	@Override
	public void setRecebeuBanderada(Piloto piloto) {
		if (!piloto.isRecebeuBanderada()) {
			piloto.setRecebeuBanderada(true);
			if (!piloto.isDesqualificado()) {
				piloto.setPosicaoBandeirada(piloto.getPosicao());
			}
			if (piloto.getCarroPilotoAtras() != null) {
				piloto.setVantagem(piloto.getCalculaSegundosParaAnterior());
			}
			Logger.logar(piloto.toString() + " Pts " + piloto.getPtosPista());
			Logger.logar(
					piloto.toString() + " Pts Depois " + piloto.getPtosPista());

			String nomeJogadorFormatado = piloto.nomeJogadorFormatado();
			if (Util.isNullOrEmpty(nomeJogadorFormatado)) {
				nomeJogadorFormatado = " ";
			}
			if (piloto.getPosicao() == 1) {
				infoPrioritaria(
						Html.preto(piloto.getNome())
								+ Html.verde(Lang.msg("044",
										new String[]{
												String.valueOf(
														piloto.getPosicao()),
												nomeJogadorFormatado})));
			} else {
				info(Html.preto(piloto.getNome())
						+ Html.verde(
								Lang.msg("044",
										new String[]{
												String.valueOf(
														piloto.getPosicao()),
												nomeJogadorFormatado})));
			}
			double somaBaixa = 0;
			for (Iterator iterator = piloto.getGanhosBaixa()
					.iterator(); iterator.hasNext();) {
				Double d = (Double) iterator.next();
				somaBaixa += d;
			}
			double somaAlta = 0;
			for (Iterator iterator = piloto.getGanhosAlta().iterator(); iterator
					.hasNext();) {
				Double d = (Double) iterator.next();
				somaAlta += d;
			}
			double somaReta = 0;
			for (Iterator iterator = piloto.getGanhosReta().iterator(); iterator
					.hasNext();) {
				Double d = (Double) iterator.next();
				somaReta += d;
			}
			somaBaixa /= piloto.getGanhosBaixa().size();
			somaAlta /= piloto.getGanhosAlta().size();
			somaReta /= piloto.getGanhosReta().size();
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss .S");
			Logger.logar("Bandeirada " + this + " Pts pista "
					+ piloto.getPtosPista() + " Pos " + piloto.getPosicao()
					+ " T " + df.format(new Date()));
			Logger.logar(" SomaBaixa " + somaBaixa + " SomaAlta " + somaAlta
					+ " SomaReta " + somaReta);

		}

	}

	public boolean isTrocaPneu() {
		return trocaPneu;
	}

	public boolean isReabastecimento() {
		return reabastecimento;
	}

	public boolean isSafetyCar() {
		return safetyCar;
	}

	@Override
	public Piloto getPilotoBateu() {
		return controleCorrida.getPilotoBateu();
	}

	@Override
	public boolean verificaSaidaBox(Piloto piloto) {
		return controleCorrida.verificaSaidaBox(piloto);
	}

	@Override
	public boolean verificaEntradaBox(Piloto piloto) {
		return controleCorrida.verificaEntradaBox(piloto);
	}

	@Override
	public Double getFatorBoxTemporada() {
		TemporadasDefauts temporadasDefauts = carregadorRecursos
				.carregarTemporadasPilotosDefauts().get(getTemporada());
		return temporadasDefauts.getFatorBox();
	}

	@Override
	public void desqualificaPiloto(Piloto piloto) {
		int desqualificados = 0;
		List<Piloto> pilotos = getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoLista = (Piloto) iterator.next();
			if (pilotoLista.isDesqualificado()) {
				desqualificados++;
			}

		}
		piloto.setDesqualificado(true);
		piloto.setPtosPista(desqualificados);
		piloto.setPosicaoBandeirada(pilotos.size() + desqualificados);
	}

	@Override
	public String getVantagem() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setVantagem(String vantagem) {
		// TODO Auto-generated method stub
		
	}

}