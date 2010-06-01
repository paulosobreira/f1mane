package sowbreira.f1mane.controles;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.GerenciadorVisual;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Html;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira
 */
public class ControleJogoLocal extends ControleRecursos implements
		InterfaceJogo {
	protected Piloto pilotoSelecionado;
	protected Piloto pilotoJogador;
	protected List pilotosJogadores = new ArrayList();
	protected double niveljogo = InterfaceJogo.MEDIO_NV;
	protected String nivelCorrida;
	protected boolean corridaTerminada;
	protected boolean semTrocaPneu;
	protected boolean semReabastacimento;
	protected ControleCorrida controleCorrida;
	protected GerenciadorVisual gerenciadorVisual;
	protected ControleEstatisticas controleEstatisticas;
	protected Integer qtdeVoltas = null;
	protected Integer diffultrapassagem = null;
	protected Integer tempoCiclo = null;
	protected Integer veloMaxReta = null;
	protected Integer habilidade = null;
	protected Integer potencia = null;
	protected Integer tempoQualificacao = null;
	protected String circuitoSelecionado = null;
	protected ControleCampeonato controleCampeonato;
	private MainFrame mainFrame;
	public Set setChegada = new HashSet();

	public ControleJogoLocal(String temporada) throws Exception {
		super(temporada);
		gerenciadorVisual = new GerenciadorVisual(this);
		controleEstatisticas = new ControleEstatisticas(this);

	}

	public ControleJogoLocal() throws Exception {
		super();
		gerenciadorVisual = new GerenciadorVisual(this);
		controleEstatisticas = new ControleEstatisticas(this);
	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#getCombustBox(sowbreira.f1mane.entidades.Piloto)
	 */
	public Integer getCombustBox(Piloto piloto) {
		return piloto.getCombustJogador();
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
	public List getPilotos() {
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
		gerenciadorVisual.atualizaPainel();
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
		return ((controleCorrida.getQtdeTotalVoltas() - 3) < getNumVoltaAtual());
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
	 * @see sowbreira.f1mane.controles.InterfaceJogo#verificaUltima()
	 */
	public boolean verificaUltima() {
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
	public double verificaUltraPassagem(Piloto piloto, double novoModificador) {
		return controleCorrida.verificaUltraPassagem(piloto, novoModificador);
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
		pilotoJogador.setAgressivo(!pilotoJogador.isAgressivo());
		pilotoJogador.setCiclosDesconcentrado(40);
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
		pilotoJogador.setTipoPeneuJogador(tipoPeneuJogador);
		pilotoJogador.setCombustJogador(combustJogador);
		pilotoJogador.setAsaJogador(asaJogador);
		pilotoJogador.setTipoPneuBox(tipoPeneuJogador);
		pilotoJogador.setQtdeCombustBox(combustJogador);
		pilotoJogador.setAsaBox(asaJogador);

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
		if (piloto.getNumeroVolta() == (totalVoltasCorrida() - 2)
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
					gerenciadorVisual.getListaPilotosCombo(), gerenciadorVisual
							.getListaCarrosCombo());
			this.nivelCorrida = Lang.key(gerenciadorVisual
					.getComboBoxNivelCorrida().getSelectedItem().toString());
			controleCorrida = new ControleCorrida(this, qtdeVoltas.intValue(),
					diffultrapassagem.intValue(), veloMaxReta.intValue(),
					tempoCiclo.intValue());
			setarNivelCorrida();
			controleCorrida.getControleClima().gerarClimaInicial(
					(Clima) gerenciadorVisual.getComboBoxClimaInicial()
							.getSelectedItem());
			controleCorrida.gerarGridLargadaSemQualificacao();
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleCorrida.iniciarCorrida();
			if (controleCampeonato != null) {
				controleCampeonato.iniciaCorrida(circuitoSelecionado);
			}
			controleEstatisticas.inicializarThreadConsumidoraInfo(1500);
		}
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
			tempoCiclo = (Integer) gerenciadorVisual.getSpinnerTempoCiclo()
					.getValue();
			veloMaxReta = (Integer) gerenciadorVisual
					.getSpinnerIndexVelcidadeEmReta().getValue();
			habilidade = (Integer) gerenciadorVisual
					.getSpinnerSkillPadraoPilotos().getValue();
			circuitoSelecionado = (String) gerenciadorVisual
					.getComboBoxCircuito().getSelectedItem();
			if (habilidade.intValue() != 0) {
				if (habilidade.intValue() <= 50) {
					habilidade = new Integer(50);
				}
				if (habilidade.intValue() >= 99) {
					habilidade = new Integer(99);
				}
				definirHabilidadePadraoPilotos(habilidade.intValue());
			}

			potencia = (Integer) gerenciadorVisual
					.getSpinnerPotenciaPadraoCarros().getValue();
			if (potencia.intValue() != 0) {
				if (potencia.intValue() <= 500) {
					potencia = new Integer(500);
				}
				if (potencia.intValue() >= 999) {
					potencia = new Integer(999);
				}
				definirPotenciaPadraoCarros(potencia.intValue());
			}

			if (gerenciadorVisual.getSpinnerQtdeMinutosQualificacao() != null) {
				tempoQualificacao = (Integer) gerenciadorVisual
						.getSpinnerQtdeMinutosQualificacao().getValue();
				if (tempoQualificacao.intValue() < 3) {
					tempoQualificacao = new Integer(3);
				}
				if (tempoQualificacao.intValue() > 15) {
					tempoQualificacao = new Integer(15);
				}
			}
			if (gerenciadorVisual.getSemReabastacimento().isSelected()) {
				semReabastacimento = true;
			}
			if (gerenciadorVisual.getSemTrocaPneu().isSelected()) {
				semTrocaPneu = true;
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
		mainFrame
				.exibirResiltadoFinal(gerenciadorVisual.exibirResultadoFinal());
		controleCorrida.pararThreads();
		controleEstatisticas.setConsumidorAtivo(false);
		if (controleCampeonato != null) {
			controleCampeonato.processaFimCorrida(getPilotos());
		}
		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			Logger.logar(" Posicao :" + (i + 1) + "-" + piloto.getNome()
					+ " Volta :" + piloto.getNumeroVolta() + " Paradas Box :"
					+ piloto.getQtdeParadasBox() + " Pontos Pista :"
					+ piloto.getPtosPista());

		}
		Logger.logar("setChegada " + setChegada.size());
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
	 * @see sowbreira.f1mane.controles.InterfaceJogo#desenhaQualificacao()
	 */
	public void desenhaQualificacao() {
		if (gerenciadorVisual != null) {
			gerenciadorVisual.desenhaQualificacao();
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
		gerenciadorVisual.adicionarInfoDireto(string);

	}

	/**
	 * @see sowbreira.f1mane.controles.InterfaceJogo#atulizaTabelaPosicoes()
	 */
	public void atulizaTabelaPosicoes() {
		gerenciadorVisual.atulizaTabelaPosicoes();

	}

	public void selecionouPiloto(Piloto pilotoSelecionado) {
		this.pilotoSelecionado = pilotoSelecionado;
		if (pilotosJogadores.contains(pilotoSelecionado)) {
			pilotoJogador = pilotoSelecionado;
		}

	}

	public Piloto getPilotoSelecionado() {
		return pilotoSelecionado;
	}

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
			Object combust, Object asa) {
		String tipoPneu = (String) tpPneu;
		Integer qtdeCombustPorcent = (Integer) combust;

		pilotoJogador.getCarro().trocarPneus(this, tipoPneu,
				controleCorrida.getDistaciaCorrida());

		int undsComnustAbastecer = (controleCorrida.getTanqueCheio() * qtdeCombustPorcent
				.intValue()) / 100;

		pilotoJogador.getCarro().setCombustivel(
				undsComnustAbastecer
						+ pilotoJogador.getCarro().getCombustivel());
		String strAsa = (String) asa;
		if (!strAsa.equals(pilotoJogador.getCarro().getAsa())) {
			infoPrioritaria(Html.orange(Lang.msg("028",
					new String[] { pilotoJogador.getNome() })));
		}
		pilotoJogador.getCarro().setAsa(strAsa);
		if (undsComnustAbastecer < 0) {
			undsComnustAbastecer = 0;
		}
		return undsComnustAbastecer;
	}

	public void saiuBox(Piloto piloto) {

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
	public void iniciarJogo() throws Exception {
		iniciarJogo(null);
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
		if (outro != null) {
			gerenciadorVisual.getPainelPosicoes().atulizaTabelaPosicoes(
					pilotos, outro);
		}

	}

	@Override
	public void setPilotos(List list) {
		this.pilotos = list;

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
		pilotoJogador.mudarPos(pos, this);
	}

	@Override
	public double getFatorUtrapassagem() {
		return controleCorrida.getFatorUtrapassagem();
	}

	public Set getSetChegada() {
		return setChegada;
	}

	@Override
	public void mudarAutoPos() {
		if (pilotoJogador == null)
			return;
		pilotoJogador.mudarAutoPos();

	}

	@Override
	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente) {
		controleCorrida.ajusteUltrapassagem(piloto, pilotoFrente);
	}

	@Override
	public void verificaAcidenteUltrapassagem(boolean agressivo, Piloto piloto,
			Piloto pilotoFrente) {
		controleCorrida.verificaAcidenteUltrapassagem(agressivo, piloto,
				pilotoFrente);

	}
}
