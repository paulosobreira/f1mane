package sowbreira.f1mane.controles;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.paddock.servlet.JogoServidor;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.GerenciadorVisual;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class ControleJogoLocal extends ControleRecursos implements
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
	protected boolean semTrocaPneu;
	protected boolean semReabastacimento;
	protected boolean kers;
	protected boolean drs;

	protected boolean continuaCampeonato;

	protected Integer qtdeVoltas = null;
	protected Integer diffultrapassagem = null;
	protected Integer tempoCiclo = null;
	protected String circuitoSelecionado = null;

	private MainFrame mainFrame;

	public ControleJogoLocal(String temporada) throws Exception {
		super(temporada);
		if (!(this instanceof JogoServidor))
			gerenciadorVisual = new GerenciadorVisual(this);
		controleEstatisticas = new ControleEstatisticas(this);
	}

	public static void main(String[] args) {
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

	public boolean isKers() {
		return kers;
	}

	public void setKers(boolean kers) {
		this.kers = kers;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getTipoPeneuBox(sowbreira.f1mane.entidades.Piloto)
	 */
	public String getTipoPeneuBox(Piloto piloto) {
		return piloto.getTipoPeneuJogador();
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
	public List getNosDoBox() {
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
	 * @see sowbreira.f1mane.controles.InterfaceJogo#atualizaPainel()
	 */
	public void atualizaPainel() {
		decrementaTracado();
	}

	public void decrementaTracado() {
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.decIndiceTracado();
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#info(java.lang.String)
	 */
	public void info(String info) {
		controleEstatisticas.info(info);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#infoPrioritaria(java.lang.String)
	 */
	public void infoPrioritaria(String info) {
		controleEstatisticas.info(info, true);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#porcentagemCorridaCompletada()
	 */
	public int porcentagemCorridaCompletada() {
		if (controleCorrida == null) {
			return 0;
		}
		return controleCorrida.porcentagemCorridaCompletada();
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
		if (ControleQualificacao.modoQualify) {
			return 1;
		}
		if (controleCorrida == null) {
			return 0;
		}
		return controleCorrida.getQtdeTotalVoltas();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaUltimasVoltas()
	 */
	public boolean verificaUltimasVoltas() {
		if (ControleQualificacao.modoQualify) {
			return false;
		}
		int corrida = porcentagemCorridaCompletada();
		return (corrida > 75);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaBoxOcupado(sowbreira.f1mane.entidades.Carro)
	 */
	public boolean verificaBoxOcupado(Carro carro) {
		if (ControleQualificacao.modoQualify) {
			return false;
		}
		return controleCorrida.verificaBoxOcupado(carro);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#calculaSegundosParaLider(sowbreira.f1mane.entidades.Piloto)
	 */
	public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
		long tempo = controleCorrida.obterTempoCilco();
		return controleEstatisticas.calculaSegundosParaLider(pilotoSelecionado,
				tempo);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaUltimaVolta()
	 */
	public boolean verificaUltimaVolta() {
		if (ControleQualificacao.modoQualify) {
			return false;
		}
		return ((controleCorrida.getQtdeTotalVoltas() - 1) == getNumVoltaAtual());
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
		return (porcentagemCorridaCompletada() / 100.0) + 1;
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
	public void verificaUltraPassagem(Piloto piloto) {
		controleCorrida.verificaUltraPassagem(piloto);
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

		pilotoJogador.setTipoPeneuJogador((String) tpneu);
		pilotoJogador.setCombustJogador((Integer) combust);
		pilotoJogador.setAsaJogador((String) asa);
		pilotosJogadores.add(pilotoJogador);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#mudarModoAgressivo()
	 */
	public boolean mudarModoAgressivo() {
		if (pilotoJogador == null)
			return false;
		pilotoJogador.setAgressivoF4(true);
		return pilotoJogador.isAgressivo();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#mudarModoBox()
	 */
	public boolean mudarModoBox() {
		if (pilotoJogador != null) {
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
		String tipoPeneuJogador = (String) tpneu;
		Integer combustJogador = (Integer) combust;
		String asaJogador = (String) asa;
		if (pilotoJogador != null) {
			pilotoJogador.setTipoPeneuJogador(tipoPeneuJogador);
			pilotoJogador.setCombustJogador(combustJogador);
			pilotoJogador.setAsaJogador(asaJogador);
			pilotoJogador.setTipoPneuBox(tipoPeneuJogador);
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
		Piloto piloto = (Piloto) pilotos.get(0);
		if (piloto.getNumeroVolta() == (totalVoltasCorrida() - 1)
				&& (piloto.getPosicao() == 1) && !isCorridaTerminada()) {

			infoPrioritaria(Html.superBlack(piloto.getNome())
					+ Html.superGreen(Lang.msg("045")));
		}

		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			piloto = (Piloto) iter.next();
			if (piloto.isDesqualificado()) {
				qtdeDesqualificados++;
			}
		}
		if (qtdeDesqualificados >= 10) {
			setCorridaTerminada(true);
			controleCorrida.terminarCorrida();
			infoPrioritaria(Html.superDarkRed(Lang.msg("024",
					new Object[] { getNumVoltaAtual() })));
		}
		if (getNumVoltaAtual() == 2 && isDrs() && !isChovendo()) {
			infoPrioritaria(Html.superBlue(Lang.msg("drsHabilitado")));
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
		Integer porcentagemCorridaCompletada = porcentagemCorridaCompletada();
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
		info(Html.cinza(controleCorrida.isCorridaPausada() ? Lang.msg("025")
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

		return controleCorrida.isSafetyCarVaiBox();
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterCarroNaFrente(sowbreira.f1mane.entidades.Piloto)
	 */
	public Carro obterCarroNaFrente(Piloto piloto) {
		return controleCorrida.obterCarroNaFrente(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#obterCarroAtraz(sowbreira.f1mane.entidades.Piloto)
	 */
	public Carro obterCarroAtraz(Piloto piloto) {
		return controleCorrida.obterCarroAtraz(piloto);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#calculaSegundosParaProximo(sowbreira.f1mane.entidades.Piloto)
	 */
	public String calculaSegundosParaProximo(Piloto psel) {
		long tempo = controleCorrida.obterTempoCilco();
		return controleEstatisticas.calculaSegundosParaProximo(psel, tempo);
	}

	public double calculaSegundosParaProximoDouble(Piloto psel) {
		long tempo = controleCorrida.obterTempoCilco();
		return controleEstatisticas.calculaSegundosParaProximoDouble(psel,
				tempo);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getIndexVelcidadeDaPista()
	 */
	public double getIndexVelcidadeDaPista() {

		return controleCorrida.getIndexVelcidadeDaPista();
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
					diffultrapassagem.intValue(), tempoCiclo.intValue());
			controleCorrida.getControleClima().gerarClimaInicial(
					(Clima) gerenciadorVisual.getComboBoxClimaInicial()
							.getSelectedItem());
			controleCorrida.gerarGridLargadaSemQualificacao();
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleCorrida.iniciarCorrida();
			if (controleCampeonato != null) {
				controleCampeonato.iniciaCorrida(circuitoSelecionado);
			}
			controleEstatisticas.inicializarThreadConsumidoraInfo(500);
		}
		Logger.logar("Circuito Selecionado " + circuitoSelecionado);
		Logger.logar("porcentagemChuvaCircuito(circuitoSelecionado) "
				+ porcentagemChuvaCircuito(circuitoSelecionado));
		Logger.logar("porcentagemChuvaCircuito() " + porcentagemChuvaCircuito());
	}

	@Override
	public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado, String clima) throws Exception {
		Map circuitosPilotos = carregadorRecursos.carregarTemporadasPilotos();
		List pilotos = new ArrayList((Collection) circuitosPilotos.get("t"
				+ campeonato.getTemporada()));
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
				campeonato.isSemTrocaPneus(),
				campeonato.isSemReabasteciemnto(), combustivelSelecionado,
				asaSelecionado, pneuSelecionado);
		this.controleCampeonato = new ControleCampeonato(campeonato, mainFrame);
		controleCampeonato.iniciaCorrida(campeonato.getCircuitoVez());
	}

	@Override
	public void iniciarJogoMenuLocal(String circuitoSelecionado,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabasteciemto,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado) throws Exception {
		this.qtdeVoltas = new Integer(numVoltasSelecionado);
		this.diffultrapassagem = new Integer(turbulenciaSelecionado);
		this.tempoCiclo = Constantes.CICLO;
		this.semReabastacimento = !reabasteciemto;
		this.semTrocaPneu = !trocaPneus;
		this.circuitoSelecionado = circuitoSelecionado;
		this.kers = kers;
		this.drs = drs;
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
						new Integer(combustivelSelecionado), "F1-Mane",
						asaSelecionado);
				break;
			}
		}
		controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
				diffultrapassagem.intValue(), tempoCiclo.intValue());
		controleCorrida.getControleClima().gerarClimaInicial(
				new Clima(climaSelecionado));
		controleCorrida.gerarGridLargadaSemQualificacao();
		gerenciadorVisual.iniciarInterfaceGraficaJogo();
		controleCorrida.iniciarCorrida();
		controleEstatisticas.inicializarThreadConsumidoraInfo(500);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCircuitos()
	 */
	public Map getCircuitos() {
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
			this.tempoCiclo = Constantes.CICLO;
			circuitoSelecionado = (String) gerenciadorVisual
					.getComboBoxCircuito().getSelectedItem();

			if (gerenciadorVisual.getSemReabastacimento().isSelected()) {
				semReabastacimento = true;
			}
			if (gerenciadorVisual.getSemTrocaPneu().isSelected()) {
				semTrocaPneu = true;
			}
			if (gerenciadorVisual.getKers().isSelected()) {
				kers = true;
			}
			if (gerenciadorVisual.getDrs().isSelected()) {
				drs = true;
			}
			setTemporada("t"
					+ gerenciadorVisual.getComboBoxTemporadas()
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
			mainFrame.exibirResultadoFinal(gerenciadorVisual
					.exibirResultadoFinal());
		controleCorrida.pararThreads();
		controleEstatisticas.setConsumidorAtivo(false);
		if (controleCampeonato != null) {
			Logger.logar("controleCampeonato.processaFimCorrida(getPilotos());");
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
			pilotoJogador.abandonar();
		}
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getTempoCiclo()
	 */
	public long getTempoCiclo() {

		return controleCorrida.getTempoCiclo();
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
		String tipoPneu = (String) tpPneu;
		Integer qtdeCombustPorcent = (Integer) combust;
		if (isSemReabastacimento() && qtdeCombustPorcent.intValue() < 75) {
			qtdeCombustPorcent = new Integer(75);
		}

		pilotoJogador.getCarro().trocarPneus(this, tipoPneu,
				controleCorrida.getDistaciaCorrida());

		int undsComnustAbastecer = (controleCorrida.getTanqueCheio() * qtdeCombustPorcent
				.intValue()) / 100;
		if (isSemReabastacimento() && isCorridaIniciada()
				&& pilotoJogador.getNumeroVolta() != 0) {
			undsComnustAbastecer = 0;
		}
		pilotoJogador.getCarro().setCombustivel(
				undsComnustAbastecer
						+ pilotoJogador.getCarro().getCombustivel());
		if (isDrs()) {
			pilotoJogador.getCarro().setAsa(Carro.MAIS_ASA);
		} else {
			String strAsa = (String) asa;
			if (!strAsa.equals(pilotoJogador.getCarro().getAsa())) {
				infoPrioritaria(Html.orange(Lang.msg("028",
						new String[] { pilotoJogador.getNome() })));
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
		return ControleQualificacao.modoQualify;
	}

	public void tabelaComparativa() {
		controleEstatisticas.tabelaComparativa();

	}

	public int getQtdeTotalVoltas() {
		return controleCorrida.getQtdeTotalVoltas();
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
		int somaPontecias = 0;
		for (int i = 0; i < getCarros().size(); i++) {
			Carro carro = (Carro) getCarros().get(i);
			somaPontecias += carro.getPotencia();
		}
		int mediaPontecia = somaPontecias / getCarros().size();
		return mediaPontecia;
	}

	public void verificaProgramacaoBox() {
		gerenciadorVisual.verificaProgramacaoBox();

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
		return semTrocaPneu;
	}

	public void setSemTrocaPneu(boolean semTrocaPneu) {
		this.semTrocaPneu = semTrocaPneu;
	}

	public boolean isSemReabastacimento() {
		return semReabastacimento;
	}

	public void setSemReabastacimento(boolean semReabastacimento) {
		this.semReabastacimento = semReabastacimento;
	}

	@Override
	public List getCarrosBox() {
		return controleCorrida.getControleBox().getCarrosBox();
	}

	@Override
	public void mudarPos(int pos) {
		if (pilotoJogador == null)
			return;
		pilotoJogador.mudarTracado(pos, this);
	}

	/**
	 * Minimo 0.5 = Mais dificil de passar Maximo 1.0 = Mais facil de passar
	 */
	@Override
	public double getFatorUtrapassagem() {
		return controleCorrida.getFatorUtrapassagem();
	}

	@Override
	public void mudarAutoPos() {
		if (pilotoJogador == null)
			return;
		pilotoJogador.mudarAutoTracado();

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
		if (isChovendo()) {
			return;
		}
		TravadaRoda travadaRoda = new TravadaRoda();
		travadaRoda.setIdNo(mapaNosIds.get(piloto.getNoAtual()));
		travadaRoda.setTracado(piloto.getTracado());
		piloto.setTravouRodas(true);
		int qtdeFumaca = 0;
		if (piloto.getNoAtual().verificaRetaOuLargada()) {
			qtdeFumaca = Util.intervalo(10, 20);
		} else if (piloto.getNoAtual().verificaCruvaAlta()) {
			qtdeFumaca = Util.intervalo(10, 30);
		} else if (piloto.getNoAtual().verificaCruvaBaixa()) {
			qtdeFumaca = Util.intervalo(10, 50);
		}
		piloto.setContTravouRodas(qtdeFumaca);
		if (gerenciadorVisual != null)
			gerenciadorVisual.adicinaTravadaRoda(travadaRoda);

	}

	@Override
	public boolean verificaNoPitLane(Piloto piloto) {
		return piloto.getPtosBox() > 0;
		// return piloto.getPtosBox() > controleCorrida.getControleBox()
		// .getParadaBox().getIndex();
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
		pilotoJogador.setAtivarKers(!pilotoJogador.isAtivarKers());
		return pilotoJogador.isAtivarKers();
	}

	@Override
	public int calculaDiferencaParaAnterior(Piloto piloto) {
		return controleEstatisticas.calculaDiferencaParaAnterior(piloto);
	}

	@Override
	public int percetagemDeVoltaCompletada(Piloto pilotoSelecionado) {
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

		long tempo = controleCorrida.obterTempoCilco();
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
		return controleCorrida.asfaltoAbrasivo();
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
	public void mostraCompsSwing() {
		if (gerenciadorVisual != null) {
			gerenciadorVisual.mostraRadioPadock();
		}
	}

	@Override
	public List listaInfo() {
		if (gerenciadorVisual != null) {
			return gerenciadorVisual.getBufferTextual();
		}
		return new ArrayList();
	}

	@Override
	public void forcaSafatyCar() {
		int i = 1;
		Piloto piloto = pilotos.get(pilotos.size() - i);
		while (Carro.BATEU_FORTE.equals(piloto.getCarro().getDanificado())) {
			piloto = pilotos.get(pilotos.size() - (++i));
		}
		piloto.getCarro().setDanificado(Carro.BATEU_FORTE);
		piloto.setDesqualificado(true);
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
		if (pilotoJogador.getNoAtual() == null
				|| pilotoJogador.getNoAtualSuave() == null
				|| pilotoJogador == null) {
			return 0;
		}

		int val = pilotoJogador.getNoAtual().getIndex()
				- pilotoJogador.getNoAtualSuave().getIndex();
		if (val < 0) {
			return 0;
		}
		return val;
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
	public boolean isCorridaPausada() {
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
	public void climaEnsolarado() {
		controleCorrida.climaEnsolarado();

	}

	@Override
	public void ativaVerControles() {
		if (gerenciadorVisual != null)
			gerenciadorVisual.ativaVerControles();
	}

	public boolean verificaPistaEmborrachada() {
		double indicativoEmborrachamentoPista = .85;
		if (!isChovendo()) {
			double emborrachamento = porcentagemCorridaCompletada() / 200.0;
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
			boolean drs, boolean trocaPneus, boolean reabasteciemto) {
		controleCampeonato = new ControleCampeonato(mainFrame);
		return controleCampeonato.criarCampeonatoPiloto(cirucitosCampeonato,
				temporadaSelecionada, numVoltasSelecionado,
				turbulenciaSelecionado, climaSelecionado, nivelSelecionado,
				pilotoSelecionado, kers, drs, trocaPneus, reabasteciemto);
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
			if (p.getNome().equals(
					controleCampeonato.getCampeonato().getRival())) {
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

	@Override
	public void fazPilotoMudarTracado(Piloto piloto, Piloto pilotoFrente) {
		controleCorrida.fazPilotoMudarTracado(piloto, pilotoFrente);
	}

	@Override
	public int getDurabilidadeAreofolio() {
		return (InterfaceJogo.DURABILIDADE_AREOFOLIO * getQtdeTotalVoltas()) / 72;
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#desenhaQualificacao()
	 */
	public void desenhaQualificacao() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (gerenciadorVisual != null) {
			gerenciadorVisual.setDesenhouCreditos(true);
		}
		desenhouQualificacao();
	}

	@Override
	public void desenhouQualificacao() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if (gerenciadorVisual != null) {
			gerenciadorVisual.setDesenhouQualificacao(true);
		}
		selecionaPilotoJogador();
	}

	@Override
	public void detalhesCorridaCampeonato() {
		controleCampeonato.detalhesCorrida();
	}

	@Override
	public boolean safetyCarUltimas3voltas() {
		return controleCorrida.safetyCarUltimas3voltas();
	}

	@Override
	public double getFatorAcidente() {
		return controleCorrida.getFatorAcidente();
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

}