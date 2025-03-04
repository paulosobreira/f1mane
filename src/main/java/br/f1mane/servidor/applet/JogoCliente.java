package br.f1mane.servidor.applet;

import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import br.nnpe.Global;
import br.nnpe.ImageUtil;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.MainFrame;
import br.f1mane.controles.ControleBox;
import br.f1mane.controles.ControleCampeonato;
import br.f1mane.controles.ControleEstatisticas;
import br.f1mane.controles.ControleRecursos;
import br.f1mane.controles.InterfaceJogo;
import br.f1mane.entidades.Campeonato;
import br.f1mane.entidades.Carro;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.Clima;
import br.f1mane.entidades.ConstrutoresPontosCampeonato;
import br.f1mane.entidades.No;
import br.f1mane.entidades.Piloto;
import br.f1mane.entidades.PilotosPontosCampeonato;
import br.f1mane.entidades.SafetyCar;
import br.f1mane.entidades.Volta;
import br.f1mane.servidor.entidades.Comandos;
import br.f1mane.servidor.entidades.TOs.DadosCriarJogo;
import br.f1mane.servidor.entidades.TOs.DadosJogo;
import br.f1mane.servidor.entidades.TOs.SessaoCliente;
import br.f1mane.servidor.entidades.TOs.TravadaRoda;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.GerenciadorVisual;
import br.f1mane.visao.PainelTabelaResultadoFinal;

/**
 * @author Paulo Sobreira Criado em 29/07/2007 as 17:30:43
 */
public class JogoCliente extends ControleRecursos implements InterfaceJogo {
	private DadosCriarJogo dadosParticiparJogo;
	private String nomeJogoCriado;
	private String nomePilotoJogador;
	private MonitorJogo monitorJogo;
	private Thread threadMonitoraJogoOnline;
	private GerenciadorVisual gerenciadorVisual;
	private ControleEstatisticas controleEstatisticas;
	private SafetyCar safetyCar = new SafetyCar();
	private boolean safetyCarNaPista;
	private String tokenJogador;
	private Piloto pilotoSelecionado;
	private DadosJogo dadosJogo;
	private String clima;
	private MainFrame mainFrame;
	private ControleBox controleBox;
	private boolean atualizacaoSuave = true;
	private String vantagem;

	public JogoCliente(String temporada) throws Exception {
		super(temporada);
	}

	public DadosJogo getDadosJogo() {
		return dadosJogo;
	}

	public void setDadosJogo(DadosJogo dadosJogo) throws Exception {
		this.dadosJogo = dadosJogo;
		if ((dadosJogo != null && dadosJogo.getPilotos() != null && !dadosJogo.getPilotos().isEmpty())) {
			if (pilotos != null) {
				pilotos.clear();
			} else {
				pilotos = new ArrayList();
			}
			List pilotosList = dadosJogo.getPilotos();
			for (Iterator iterator = pilotosList.iterator(); iterator.hasNext(); ) {
				Piloto object = (Piloto) iterator.next();
				if (pilotos.contains(object)) {
					throw new Exception("Piloto Repetido");
				} else {
					pilotos.add(object);
				}
			}

		}
		if (Comandos.CORRIDA_INICIADA.equals(monitorJogo.getEstado())) {
			if (dadosJogo != null && dadosJogo.getClima() != null && clima != null
					&& !clima.equals(dadosJogo.getClima())) {
				clima = dadosJogo.getClima();
				if (gerenciadorVisual == null) {
					preparaGerenciadorVisual();
				}
				gerenciadorVisual.informaMudancaClima();
			}
		}

	}

	public void iniciarJogoOnline(DadosCriarJogo dadosParticiparJogo, String nomeJogoCriado,
								  ControlePaddockCliente controlePaddockCliente, SessaoCliente sessaoCliente, String nomePilotoJogador) {
		try {
			if (getCircuito() == null) {
				carregaRecursos((String) getCircuitos().get(dadosParticiparJogo.getCircuitoSelecionado()));
			}
			controleBox = new ControleBox(this, null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(getMainFrame(), e.getMessage());
			Logger.logarExept(e);
		}
		this.dadosParticiparJogo = dadosParticiparJogo;
		this.nomeJogoCriado = nomeJogoCriado;
		this.nomePilotoJogador = nomePilotoJogador;
		monitorJogo = new MonitorJogo(this, controlePaddockCliente, sessaoCliente);
		tokenJogador = sessaoCliente.getToken();
		clima = dadosParticiparJogo.getClima();
		mainFrame.setControleJogo(this);
		selecionaPilotoJogador();
		threadMonitoraJogoOnline = new Thread(monitorJogo);
		threadMonitoraJogoOnline.setPriority(Thread.MIN_PRIORITY);
		threadMonitoraJogoOnline.start();
	}

	public void preparaGerenciadorVisual() {
		try {
			preparaGerenciadorVisual(false);
		} catch (Exception e) {
			Logger.logarExept(e);
		}
	}

	public void preparaGerenciadorVisual(boolean monitor) throws InterruptedException {
		if (gerenciadorVisual != null) {
			return;
		}
		try {
			gerenciadorVisual = new GerenciadorVisual(this);
			controleEstatisticas = new ControleEstatisticas(this);
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleEstatisticas.inicializarThreadConsumidoraInfo();
		} catch (Exception e) {
			StackTraceElement[] trace = e.getStackTrace();
			StringBuilder retorno = new StringBuilder();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i]).append("\n");
			JOptionPane.showMessageDialog(getMainFrame(), retorno.toString(), Lang.msg("059"),
					JOptionPane.ERROR_MESSAGE);
			Logger.logarExept(e);
		}

	}

	public MonitorJogo getMonitorJogo() {
		return monitorJogo;
	}

	public void setMonitorJogo(MonitorJogo threadMonitoraJogo) {
		this.monitorJogo = threadMonitoraJogo;
	}

	public DadosCriarJogo getDadosCriarJogo() {
		return dadosParticiparJogo;
	}

	public String getNomeJogoCriado() {
		return nomeJogoCriado;
	}

	public void setNomeJogoCriado(String nomeJogoCriado) {
		this.nomeJogoCriado = nomeJogoCriado;
	}

	public void abandonar() {
		if (mainFrame != null) {
			WindowListener[] windowListeners = mainFrame.getWindowListeners();
			for (int i = 0; i < windowListeners.length; i++) {
				mainFrame.removeWindowListener(windowListeners[i]);
			}
		}
		if (monitorJogo != null) {
			monitorJogo.abandonar();
		}
	}

	public void adicionarInfoDireto(String string) {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.adicionarInfoDireto(string);

	}

	public void apagarLuz() {
		gerenciadorVisual.apagarLuz();

	}

	public void atualizaIndexTracadoPilotos() {
		decrementaTracado();
	}

	public void atulizaTabelaPosicoes() {
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
	}

	public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
		return controleEstatisticas.calculaSegundosParaLider(pilotoSelecionado);
	}

	public String calculaSegundosParaProximo(Piloto psel) {
		return controleEstatisticas.calculaSegundosParaProximo(psel);
	}

	public double calculaDiferencaParaProximoDouble(Piloto psel) {
		return controleEstatisticas.calculaDiferencaParaProximoDouble(psel);
	}

	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu, Object combust, String nomeJogador,
											Object asa) {

	}

	public void exibirResultadoFinal() {
		gerenciadorVisual.exibirResultadoFinal();
		// mainFrame
		// .exibirResiltadoFinal(gerenciadorVisual.exibirResultadoFinal());
		matarThreadsResultadoFnal();
	}

	public List getCarros() {
		return carros;
	}

	public int getCicloAtual() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Circuito getCircuito() {
		return circuito;
	}

	public Map getCircuitos() {
		return circuitos;
	}

	public String getClima() {
		return clima;
	}

	public Integer getCombustBox(Piloto piloto) {
		return new Integer(piloto.getQtdeCombustBox());
	}

	public double getIndexVelcidadeDaPista() {
		// TODO Auto-generated method stub
		return 0;
	}

	public MainFrame getMainFrame() {
		return mainFrame;
	}

	public List<No> getNosDaPista() {
		return nosDaPista;
	}

	public List<No> getNosDoBox() {
		return nosDoBox;
	}

	public int getNumVoltaAtual() {
		if (dadosJogo != null)
			return dadosJogo.getVoltaAtual().intValue();
		return 0;
	}

	public List<Piloto> getPilotos() {
		return pilotos;
	}

	public SafetyCar getSafetyCar() {
		return safetyCar;
	}

	public void setSafetyCarNaPista(boolean safetyCarNaPista) {
		this.safetyCarNaPista = safetyCarNaPista;
	}

	public String getTipoPneuBox(Piloto piloto) {
		return piloto.getTipoPneuBox();
	}

	public void info(String info) {
		controleEstatisticas.info(info);
	}

	public void infoPrioritaria(String info) {
		// TODO Auto-generated method stub

	}

	public void informaMudancaClima() {
		// TODO Auto-generated method stub
	}

	public void iniciarJogo() throws Exception {
		// TODO Auto-generated method stub

	}

	public void iniciarJogoSingle() throws Exception {
		// TODO Auto-generated method stub

	}

	public boolean isChovendo() {
		return Clima.CHUVA.equals(clima);
	}

	public boolean isCorridaTerminada() {
		if (dadosJogo != null) {
			return dadosJogo.getCorridaTerminada().booleanValue();
		}
		return false;
	}

	public boolean isSafetyCarNaPista() {
		return safetyCarNaPista;
	}

	public boolean isSafetyCarVaiBox() {
		if (safetyCar != null)
			return !safetyCar.isVaiProBox();
		return true;
	}

	public void matarThreadsResultadoFnal() {
		try {
			if (monitorJogo != null) {
				monitorJogo.setJogoAtivo(false);
				monitorJogo.matarTodasThreads();
			}
			if (threadMonitoraJogoOnline != null) {
				threadMonitoraJogoOnline.interrupt();
			}
			if (controleEstatisticas != null) {
				controleEstatisticas.setConsumidorAtivo(false);
			}
		} catch (Throwable e) {
			Logger.logarExept(e);
		}
	}

	public void matarTodasThreads() {
		try {
			matarThreadsResultadoFnal();
			if (gerenciadorVisual != null) {
				gerenciadorVisual.finalize();
			}
		} catch (Throwable e) {
			Logger.logarExept(e);
		}
	}

	public boolean mudarModoBox() {
		monitorJogo.mudarModoBox();
		return monitorJogo.getModoBox();
	}

	public Carro obterCarroAtras(Piloto piloto) {
		int pos = piloto.getPosicao();
		if (pos < 0) {
			return null;
		}
		if (pos > pilotos.size() - 1) {
			return null;
		}
		return ((Piloto) pilotos.get(pos)).getCarro();
	}

	public Carro obterCarroNaFrente(Piloto piloto) {
		int pos = piloto.getPosicao() - 2;
		if (pos < 0) {
			return null;
		}
		if (pos > pilotos.size() - 1) {
			return null;
		}
		return ((Piloto) pilotos.get(pos)).getCarro();

	}

	public double obterIndicativoCorridaCompleta() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Volta obterMelhorVolta() {
		if (dadosJogo != null)
			return dadosJogo.getMelhoVolta();
		return null;
	}

	public PainelTabelaResultadoFinal obterResultadoFinal() {
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
		return gerenciadorVisual.getResultadoFinal();
	}

	public void pausarJogo() {
		// TODO Auto-generated method stub

	}

	public int porcentagemCorridaConcluida() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void processaNovaVolta() {
		// TODO Auto-generated method stub
	}

	public void processaVoltaRapida(Piloto piloto) {
		// TODO Auto-generated method stub

	}

	public void selecionaPilotoJogador() {
		if (pilotoSelecionado != null) {
			return;
		}
		for (Iterator iter = pilotos.iterator(); iter.hasNext(); ) {
			Piloto piloto = (Piloto) iter.next();
			if (tokenJogador.equals(piloto.getTokenJogador())) {
				pilotoSelecionado = piloto;
				break;
			}
		}
	}

	public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa) {
		dadosParticiparJogo.setCombustivel((Integer) combust);
		dadosParticiparJogo.setTpPneu((String) tpneu);
		dadosParticiparJogo.setAsa((String) asa);
		if (monitorJogo != null) {
			monitorJogo.alterarOpcoesBox(tpneu, combust, asa);
		} else {
			Logger.logar("monitorJogo null");
		}
	}

	public void setCorridaTerminada(boolean corridaTerminada) {
		// TODO Auto-generated method stub

	}

	public void setNivelCorrida(String nivelCorrida) {
		// TODO Auto-generated method stub

	}

	public void setNiveljogo(double niveljogo) {
		// TODO Auto-generated method stub

	}

	public int totalVoltasCorrida() {
		if (dadosParticiparJogo != null && dadosParticiparJogo.getQtdeVoltas() != null)
			return dadosParticiparJogo.getQtdeVoltas().intValue();
		return 0;
	}

	public boolean verificaBoxOcupado(Carro carro) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean verificaNivelJogo() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean verificaUltimaVolta() {
		// TODO Auto-generated method stub
		return false;
	}

	public void verificaAcidente(Piloto piloto) {
		// TODO Auto-generated method stub
	}

	public void verificaVoltaMaisRapidaCorrida(Piloto piloto) {
		// TODO Auto-generated method stub

	}

	public void zerarMelhorVolta() {
		// TODO Auto-generated method stub

	}

	public void pularQualificacao() {
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
	}

	public void selecionouPiloto(Piloto pilotoSelecionado) {
		this.pilotoSelecionado = pilotoSelecionado;
	}

	public Piloto getPilotoSelecionado() {
		if (pilotoSelecionado != null) {
			for (Iterator iter = getPilotosCopia().iterator(); iter.hasNext(); ) {
				Piloto piloto = (Piloto) iter.next();
				if (piloto.getId() == pilotoSelecionado.getId()) {
					return piloto;
				}
			}

		}
		return pilotoSelecionado;
	}

	public void atualizaPosSafetyCar(int safetyId, boolean safetySair) {
		No no = (No) mapaIdsNos.get(new Integer(safetyId));
		safetyCar = new SafetyCar();
		safetyCar.setNoAtual(no);
		safetyCar.setVaiProBox(safetySair);
		if (safetySair) {
			for (Iterator iterator = getPilotosCopia().iterator(); iterator.hasNext(); ) {
				Piloto piloto = (Piloto) iterator.next();
				if (piloto.isDesqualificado()) {
					piloto.getCarro().setRecolhido(true);
				}
			}
		}
	}

	public Volta obterMelhorVolta(Piloto pilotoSelecionado) {
		return pilotoSelecionado.getMelhorVolta();
	}

	public void setNomePilotoJogador(String nomePilotoJogador) {
		this.nomePilotoJogador = nomePilotoJogador;
	}

	public Piloto getPilotoJogador() {
		List<Piloto> pilotosCopia = getPilotosCopia();
		for (Iterator iter = pilotosCopia.iterator(); iter.hasNext(); ) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.getNome().equals(nomePilotoJogador)) {
				return piloto;
			}
		}
		return null;
	}

	public void verificaMudancaClima(String climaNovo) {
		if (clima != null && !clima.equals(climaNovo)) {
			clima = climaNovo;
		}
	}
	public void mudarGiroMotor(Object selectedItem) {
		monitorJogo.mudarGiroMotor(selectedItem);

	}

	public int calculaDiferencaParaProximo(Piloto piloto) {
		if (controleEstatisticas == null) {
			return 0;
		}
		int calculaDiferencaParaProximo = controleEstatisticas.calculaDiferencaParaProximo(piloto);
		if (calculaDiferencaParaProximo < 0) {
			calculaDiferencaParaProximo = 0;
		}
		return calculaDiferencaParaProximo;
	}

	public void mudarModoPilotagem(String modo) {
		monitorJogo.mudarModoPilotagem(modo);
	}

	public String getAsaBox(Piloto piloto) {
		return piloto.getAsaBox();
	}

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu, Object combust, Object asa) {
		monitorJogo.alterarOpcoesBox(tpPneu, combust, asa);
		return 0;
	}

	public void setMainFrame(MainFrame mainFrame) {
		this.mainFrame = mainFrame;
	}

	public int verificaRetardatario(Piloto piloto, int novoModificador) {
		// TODO Auto-generated method stub
		return 0;
	}

	public boolean isModoQualify() {
		// TODO Auto-generated method stub
		return false;
	}

	public void tabelaComparativa() {
		// TODO Auto-generated method stub

	}

	public int getQtdeTotalVoltas() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void iniciaJanela() {
		if (!getMainFrame().isVisible()) {
            getMainFrame().setVisible(true);
            getMainFrame().pack();
            getMainFrame().setSize(1280, 720);
		}
	}

	public boolean isCorridaIniciada() {
		return false;
	}

	public int getMediaPontecia() {

		return 0;
	}

	@Override
	public void iniciarJogo(ControleCampeonato controleCampeonato) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void mudaPilotoSelecionado() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSemReabastecimento() {
		return !dadosParticiparJogo.isReabastecimento();
	}

	@Override
	public boolean isSemTrocaPneu() {
		return !dadosParticiparJogo.isTrocaPneu();
	}

	@Override
	public List getCarrosBox() {
		return controleBox.getCarrosBox();
	}

	@Override
	public void mudarTracado(int pos) {
		if (pilotoSelecionado == null) {
			return;
		}
		if (pilotoSelecionado.getSetaBaixo() <= 0) {
			if (pilotoSelecionado.getTracado() == 0 && pos == 1) {
				pilotoSelecionado.setSetaCima(11);
			}
			if (pilotoSelecionado.getTracado() == 2 && pos == 0) {
				pilotoSelecionado.setSetaCima(11);
			}
		}
		if (pilotoSelecionado.getSetaCima() <= 0) {
			if (pilotoSelecionado.getTracado() == 0 && pos == 2) {
				pilotoSelecionado.setSetaBaixo(11);
			}
			if (pilotoSelecionado.getTracado() == 1 && pos == 0) {
				pilotoSelecionado.setSetaBaixo(11);
			}
		}
		monitorJogo.mudarTracado(pos);
	}

	@Override
	public double getFatorUtrapassagem() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setManualTemporario() {
		monitorJogo.setManualTemporario();
	}

	@Override
	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente) {
		// TODO Auto-generated method stub

	}

	public void geraBoxesEquipes(List carros) {
		controleBox.geraBoxesEquipes(carros);
	}

	@Override
	public No getNoEntradaBox() {
		return controleBox.getEntradaBox();

	}

	@Override
	public void travouRodas(Piloto piloto) {
		piloto.setTravouRodas(10);
	}

	public void travouRodas(TravadaRoda travadaRoda) {
		if (gerenciadorVisual != null && travadaRoda != null) {
			gerenciadorVisual.adicinaTravadaRoda(travadaRoda);
		}

	}

	@Override
	public boolean verificaNoPitLane(Piloto piloto) {
		return piloto.getPtosBox() > 0;
	}

	public BufferedImage carregaBackGround(String backGround) {
		if (isCorridaIniciada() && monitorJogo.getLatenciaReal() > Global.LATENCIA_MAX) {
			try {
				Thread.sleep(Util.intervalo(5000, 10000));
			} catch (InterruptedException e) {
			}
		}
		URL url;
		try {
			String caminho = mainFrame.getCodeBase()
					+ "/sowbreira/f1mane/recursos/" + backGround;


			Logger.logar("Caminho Carregar Bkg " + caminho);
			url = new URL(caminho);
			BufferedImage buff = ImageUtil.toCompatibleImage(ImageIO.read(url.openStream()));
			return buff;
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}

	public void carregaBackGroundCliente() {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.carregaBackGroundCliente();

	}

	@Override
	public boolean isErs() {
		return dadosParticiparJogo.isErs();
	}

	@Override
	public void setErs(boolean Ers) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isDrs() {
		return dadosParticiparJogo.isDrs();
	}

	@Override
	public void setDrs(boolean drs) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean mudarModoDRS() {
		if (pilotoSelecionado == null) {
			return false;
		}
		monitorJogo.mudarModoDRS(true);
		return pilotoSelecionado.isAtivarDRS();
	}

	@Override
	public boolean mudarModoKers() {
		if (pilotoSelecionado == null) {
			return false;
		}
		monitorJogo.mudarModoKers(!pilotoSelecionado.isAtivarErs());
		return pilotoSelecionado.isAtivarErs();
	}

	@Override
	public int calculaDiferencaParaAnterior(Piloto piloto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int percetagemDeVoltaConcluida(Piloto pilotoSelecionado) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean verirficaDesafiandoCampeonato(Piloto piloto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verificaCampeonatoComRival() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String calculaSegundosParaRival(Piloto pilotoSelecionado) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String obterSegundosParaRival(Piloto pilotoSelecionado) {
		return null;
	}

	@Override
	public void verificaDesafioCampeonatoPiloto() {
		// TODO Auto-generated method stub

	}

	@Override
	public void aumentaFatorAcidade() {
		// TODO Auto-generated method stub

	}

	@Override
	public void diminueFatorAcidade() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPontosPilotoLargada(long ptosPista) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean asfaltoAbrasivo() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean asfaltoAbrasivoReal() {
		return false;
	}

	@Override
	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo, Piloto p) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void driveThru() {
		monitorJogo.driveThru(pilotoSelecionado);
	}

	@Override
	public int porcentagemChuvaCircuito() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isBoxRapido() {
		// TODO Auto-generated method stub
		return false;
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
		// TODO Auto-generated method stub

	}

	@Override
	public No obterProxCurva(No noAtual) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setMouseZoom(double d) {
		if (gerenciadorVisual != null)
			gerenciadorVisual.setMouseZoom(d);

	}

	public void interruptDesenhaQualificao() {

	}

	public void autoDrs() {
		if (pilotoSelecionado == null) {
			return;
		}
		if (tokenJogador == null) {
			return;
		}
		if (!tokenJogador.equals(pilotoSelecionado.getTokenJogador())) {
			return;
		}
		if (pilotoSelecionado.getNoAtual() != null && !isChovendo()
				&& pilotoSelecionado.getNoAtual().verificaRetaOuLargada() && pilotoSelecionado.getNumeroVolta() > 0
				&& pilotoSelecionado.getPtosBox() == 0 && !Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro().getAsa())
				&& (obterCarroNaFrente(pilotoSelecionado) != null
				&& obterCarroNaFrente(pilotoSelecionado).getPiloto().getPtosBox() == 0)
				&& calculaDiferencaParaProximoDouble(pilotoSelecionado) < 1) {
			monitorJogo.mudarModoDRS(true);
		}
	}

	@Override
	public boolean verificaLag() {
		if (monitorJogo == null) {
			return false;
		}
		return monitorJogo.getLatenciaReal() > Global.LATENCIA_MIN;
	}

	@Override
	public int getLag() {
		if (monitorJogo == null) {
			return 0;
		}
		return monitorJogo.getLatenciaReal();
	}

	@Override
	public void decrementaTracado() {
		List<Piloto> pilotosCopia = getPilotosCopia();
		for (Iterator iterator = pilotosCopia.iterator(); iterator.hasNext(); ) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.decIndiceTracado(this);
		}
	}

	@Override
	public int calculaDiffParaProximoRetardatario(Piloto piloto, boolean analisaTracado) {
		if (controleEstatisticas == null) {
			System.out.println("controleEstatisticas null");
		}
		return controleEstatisticas.calculaDiffParaProximoRetardatario(piloto, analisaTracado);
	}

	@Override
	public No getNoSaidaBox() {
		return controleBox.getSaidaBox();
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
		return false;
	}

	@Override
	public void descontaTempoPausado(Volta volta) {
		// TODO Auto-generated method stub

	}

	@Override
	public void criarCampeonato() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void criarCampeonatoPiloto() throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void proximaCorridaCampeonato() {
		// TODO Auto-generated method stub

	}

	@Override
	public void climaChuvoso() {
		// TODO Auto-generated method stub

	}

	@Override
	public void climaLimpo() {
		// TODO Auto-generated method stub

	}

	@Override
	public void ativaVerControles() {
		if (gerenciadorVisual != null)
			gerenciadorVisual.ativaVerControles();
	}

	@Override
	public void iniciarJogoMenuLocal(String circuitoSelecionado, String temporadaSelecionada, int numVoltasSelecionado,
									 int turbulenciaSelecionado, String climaSelecionado, String nivelSelecionado, Piloto pilotoSelecionado,
									 boolean kers, boolean drs, boolean trocaPneus, boolean reabastecimento, int combustivelSelecionado,
									 String asaSelecionado, String pneuSelecionado, boolean safetyCar) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean verificaPistaEmborrachada() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato, String temporadaSelecionada,
											int numVoltasSelecionado, int turbulenciaSelecionado, String climaSelecionado, String nivelSelecionado,
											Piloto pilotoSelecionado, boolean kers, boolean drs, boolean trocaPneus, boolean reabastecimento) {
		return null;
	}

	@Override
	public void voltaMenuPrincipal() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<PilotosPontosCampeonato> geraListaPilotosPontos() {
		return null;
	}

	@Override
	public List<ConstrutoresPontosCampeonato> geraListaContrutoresPontos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato, int combustivelSelecionado, String asaSelecionado,
											  String pneuSelecionado, String clima) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public Campeonato continuarCampeonato() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void dadosPersistenciaCampeonato(Campeonato campeonato) {
		// TODO Auto-generated method stub

	}

	@Override
	public void continuarCampeonato(Campeonato campeonato) {
		// TODO Auto-generated method stub

	}

	@Override
	public Piloto obterRivalCampeonato() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Carro obterCarroNaFrenteRetardatario(Piloto piloto, boolean analisaTracado) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void desenhouQualificacao() {
		if (gerenciadorVisual != null) {
			gerenciadorVisual.setDesenhouQualificacao(true);
		}
	}

	public void desenhaQualificacao() {
		if (gerenciadorVisual != null) {
			gerenciadorVisual.setDesenhouCreditos(true);
		}
	}

	@Override
	public void detalhesCorridaCampeonato() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean safetyCarUltimas3voltas() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getFatorAcidente() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean verificaInfoRelevante(Piloto piloto) {
		return controleEstatisticas.verificaInfoRelevante(piloto);
	}

	@Override
	public Campeonato continuarCampeonatoXml() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void processaMudancaEquipeCampeontato() {
		// TODO Auto-generated method stub

	}

	@Override
	public Campeonato continuarCampeonatoXmlDisco() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public No obterCurvaAnterior(No noAtual) {
		// TODO Auto-generated method stub
		return null;
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
		monitorJogo.pilotoSelecionadoMinimo();

	}

	@Override
	public void pilotoSelecionadoNormal() {
		monitorJogo.pilotoSelecionadoNormal();

	}

	@Override
	public void pilotoSelecionadoMaximo() {
		monitorJogo.pilotoSelecionadoMaximo();

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
		Field[] declaredFields = JogoCliente.class.getDeclaredFields();
		buffer.append("-=JogoCliente=- <br>");
		for (Field field : declaredFields) {
			try {
				Object object = field.get(this);
				String valor = "null";
				if (object != null) {
					if (Util.isWrapperType(object.getClass())) {
						continue;
					}
					valor = object.toString();
				}
				buffer.append(field.getName()).append(" = ").append(valor).append("<br>");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

	}

	public boolean isAtualizacaoSuave() {
		return atualizacaoSuave;
	}

	public void setAtualizacaoSuave(boolean atualizacaoSuave) {
		this.atualizacaoSuave = atualizacaoSuave;
	}

	@Override
	public void setRecebeuBanderada(Piloto piloto) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSafetyCar() {
		if (dadosParticiparJogo != null) {
			return dadosParticiparJogo.isSafetyCar();
		}
		return false;
	}

	@Override
	public Piloto getPilotoBateu() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean verificaSaidaBox(Piloto piloto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean verificaEntradaBox(Piloto piloto) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Double getFatorBoxTemporada() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void travouRodas(Piloto piloto, boolean semFumaca) {
		// TODO Auto-generated method stub

	}

	@Override
	public void desqualificaPiloto(Piloto piloto) {
		// TODO Auto-generated method stub

	}

	@Override
	public void forcaQuerbraAereofolio(Piloto piloto) {
		// TODO Auto-generated method stub

	}


	@Override
	public String getVantagem() {
		return vantagem;
	}

	@Override
	public void setVantagem(String vantagem) {
		this.vantagem = vantagem;
	}

	@Override
	public double getFatorConsumoPneuSemTroca() {
		return 0;
	}

	@Override
	public double getFatorConsumoCombustivelSemReabastecimento() {
		return 0;
	}

	@Override
	public long tempoCicloCircuito() {
		return circuitosCiclo.get(circuito.getNome());
	}

	@Override
	public String getAutomaticoManual() {
		return Global.CONTROLE_AUTOMATICO;
	}


}
