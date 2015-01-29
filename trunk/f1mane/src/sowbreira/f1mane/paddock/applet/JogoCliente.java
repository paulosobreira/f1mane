package sowbreira.f1mane.paddock.applet;

import java.awt.Point;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleBox;
import sowbreira.f1mane.controles.ControleCampeonato;
import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.controles.InterfaceJogo;
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
import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogo;
import sowbreira.f1mane.paddock.entidades.TOs.Posis;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.GerenciadorVisual;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;
import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;

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
	private boolean safetyCarBol;
	private boolean modoAgressivo;
	private boolean modoBox;
	private String nomeJogador;
	private Piloto pilotoSelecionado;
	private DadosJogo dadosJogo;
	private String clima;
	private MainFrame mainFrame;
	private ControleBox controleBox;

	public JogoCliente(String temporada) throws Exception {
		super(temporada);
	}

	public DadosJogo getDadosJogo() {
		return dadosJogo;
	}

	public void setDadosJogo(DadosJogo dadosJogo) throws Exception {
		this.dadosJogo = dadosJogo;
		if ((dadosJogo != null && dadosJogo.getPilotosList() != null && !dadosJogo
				.getPilotosList().isEmpty())) {
			if (pilotos != null) {
				pilotos.clear();
			} else {
				pilotos = new ArrayList();
			}
			List pilotosList = dadosJogo.getPilotosList();
			for (Iterator iterator = pilotosList.iterator(); iterator.hasNext();) {
				Piloto object = (Piloto) iterator.next();
				if (pilotos.contains(object)) {
					throw new Exception("Piloto Repetido");
				} else {
					pilotos.add(object);
				}
			}

		}
		if (Comandos.CORRIDA_INICIADA.equals(monitorJogo.getEstado())) {
			if (dadosJogo != null && dadosJogo.getClima() != null
					&& clima != null && !clima.equals(dadosJogo.getClima())) {
				clima = dadosJogo.getClima();
				if (gerenciadorVisual == null) {
					preparaGerenciadorVisual();
				}
				gerenciadorVisual.informaMudancaClima();
			}
		}

	}

	public void iniciarJogoOnline(DadosCriarJogo dadosParticiparJogo,
			String nomeJogoCriado,
			ControlePaddockCliente controlePaddockCliente,
			SessaoCliente sessaoCliente, String nomePilotoJogador) {
		try {
			carregaRecursos((String) getCircuitos().get(
					dadosParticiparJogo.getCircuitoSelecionado()));
			controleBox = new ControleBox(this, null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(getMainFrame(), e.getMessage());
			Logger.logarExept(e);
		}
		this.dadosParticiparJogo = dadosParticiparJogo;
		this.nomeJogoCriado = nomeJogoCriado;
		this.nomePilotoJogador = nomePilotoJogador;
		monitorJogo = new MonitorJogo(this, controlePaddockCliente,
				sessaoCliente);
		nomeJogador = sessaoCliente.getNomeJogador();
		clima = dadosParticiparJogo.getClima().getClima();
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

	public void preparaGerenciadorVisual(boolean monitor)
			throws InterruptedException {
		if (gerenciadorVisual != null) {
			return;
		}
		try {
			gerenciadorVisual = new GerenciadorVisual(this);
			controleEstatisticas = new ControleEstatisticas(this);
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleEstatisticas.inicializarThreadConsumidoraInfo(500);
		} catch (Exception e) {
			if (monitor && e instanceof InterruptedException) {
				throw (InterruptedException) e;
			}
			StackTraceElement[] trace = e.getStackTrace();
			StringBuffer retorno = new StringBuffer();
			int size = ((trace.length > 10) ? 10 : trace.length);

			for (int i = 0; i < size; i++)
				retorno.append(trace[i] + "\n");
			JOptionPane.showMessageDialog(getMainFrame(), retorno.toString(),
					Lang.msg("059"), JOptionPane.ERROR_MESSAGE);
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
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
		gerenciadorVisual.apagarLuz();

	}

	public void atualizaPainel() {
	}

	public void atulizaTabelaPosicoes() {
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
		if (dadosJogo != null && !"".equals(dadosJogo.getTexto())
				&& dadosJogo.getTexto() != null) {
			gerenciadorVisual.adicionarInfoDireto(Lang.decodeTexto(dadosJogo
					.getTexto()));
			dadosJogo.setTexto(null);
		}
	}

	public String calculaSegundosParaLider(Piloto pilotoSelecionado) {
		long tempo = dadosParticiparJogo.getTempoCiclo().intValue();
		return controleEstatisticas.calculaSegundosParaLider(pilotoSelecionado,
				tempo);
	}

	public String calculaSegundosParaProximo(Piloto psel) {
		long tempo = dadosParticiparJogo.getTempoCiclo().intValue();
		return controleEstatisticas.calculaSegundosParaProximo(psel, tempo);
	}

	public double calculaSegundosParaProximoDouble(Piloto psel) {
		long tempo = dadosParticiparJogo.getTempoCiclo().intValue();
		return controleEstatisticas.calculaSegundosParaProximoDouble(psel,
				tempo);
	}

	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
			Object combust, String nomeJogador, Object asa) {

	}

	public void exibirResultadoFinal() {
		gerenciadorVisual.exibirResultadoFinal();
		// mainFrame
		// .exibirResiltadoFinal(gerenciadorVisual.exibirResultadoFinal());
		matarTodasThreads();
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

	public String getNivelCorrida() {
		return dadosParticiparJogo.getNivelCorrida();
	}

	public double getNiveljogo() {
		if (InterfaceJogo.DIFICIL.equals(dadosParticiparJogo.getNivelCorrida())) {
			return InterfaceJogo.DIFICIL_NV;
		}
		if (InterfaceJogo.NORMAL.equals(dadosParticiparJogo.getNivelCorrida())) {
			return InterfaceJogo.MEDIO_NV;
		}
		if (InterfaceJogo.FACIL.equals(dadosParticiparJogo.getNivelCorrida())) {
			return InterfaceJogo.FACIL_NV;
		}
		return 0;
	}

	public List getNosDaPista() {
		return nosDaPista;
	}

	public List getNosDoBox() {
		return nosDoBox;
	}

	public int getNumVoltaAtual() {
		if (dadosJogo != null)
			return dadosJogo.getVoltaAtual();
		return 0;
	}

	public List<Piloto> getPilotos() {
		return pilotos;
	}

	public SafetyCar getSafetyCar() {
		return safetyCar;
	}

	public void setSafetyCarBol(boolean safetyCarBol) {
		this.safetyCarBol = safetyCarBol;
	}

	public long getTempoCiclo() {
		return dadosParticiparJogo.getTempoCiclo().longValue();
	}

	public String getTipoPeneuBox(Piloto piloto) {
		return piloto.getTipoPneuBox();
	}

	public void info(String info) {
		// TODO Auto-generated method stub

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
		if (Clima.CHUVA.equals(clima)) {
			return true;
		}
		return false;
	}

	public boolean isCorridaTerminada() {
		if (dadosJogo != null) {
			return dadosJogo.isCorridaTerminada();
		}
		return false;
	}

	public boolean isSafetyCarNaPista() {
		return safetyCarBol;
	}

	public boolean isSafetyCarVaiBox() {
		if (safetyCar != null)
			return safetyCar.isVaiProBox();
		return false;
	}

	public void matarTodasThreads() {
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
			if (gerenciadorVisual != null) {
				gerenciadorVisual.finalize();
			}
		} catch (Throwable e) {
			Logger.logarExept(e);
		}
	}

	public boolean mudarModoAgressivo() {
		modoAgressivo = !modoAgressivo;
		monitorJogo.mudarModoAgressivo(modoAgressivo);
		return modoAgressivo;
	}

	public boolean mudarModoBox() {
		if (modoBox) {
			modoBox = !modoBox;
		}
		monitorJogo.mudarModoBox(modoBox);
		return modoBox;
	}

	public Carro obterCarroAtraz(Piloto piloto) {
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

	public int porcentagemCorridaCompletada() {
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
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (nomeJogador.equals(piloto.getNomeJogador())) {
				pilotoSelecionado = piloto;
				break;
			}
		}
	}

	public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa) {
		dadosParticiparJogo.setCombustivel((Integer) combust);
		dadosParticiparJogo.setTpPnueu((String) tpneu);
		dadosParticiparJogo.setAsa((String) asa);
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
		if (dadosParticiparJogo != null
				&& dadosParticiparJogo.getQtdeVoltas() != null)
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

	public boolean verificaUltimasVoltas() {
		// TODO Auto-generated method stub
		return false;
	}

	public void verificaUltraPassagem(Piloto piloto) {
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

	public void atualizaPosicaoPiloto(Posis posis) {
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.getId() == posis.idPiloto) {
				piloto.setAgressivo(posis.agressivo, this);
				piloto.setJogadorHumano(posis.humano);
				int pos = posis.tracado;
				double mod = Carro.ALTURA;
				if (piloto.getTracado() == 0 && (pos == 4 || pos == 5)) {
					mod *= 3;
				} else if ((piloto.getTracado() == 1 || piloto.getTracado() == 2)
						&& (pos == 4 || pos == 5)) {
					mod *= 2;
				} else if ((piloto.getTracado() == 5 || piloto.getTracado() == 4)
						&& (pos == 2 || pos == 1)) {
					mod *= 2;
				}
				if (piloto.getIndiceTracado() <= 0) {
					piloto.setTracadoAntigo(piloto.getTracado());
				}
				piloto.setTracado(pos);
				if (piloto.getIndiceTracado() <= 0
						&& piloto.getTracado() != piloto.getTracadoAntigo()) {
					piloto.setIndiceTracado((int) (mod * getCircuito()
							.getMultiplicadorLarguraPista()));
				}
				piloto.setAutoPos(posis.autoPos);
				if (posis.idNo >= -1) {
					No no = (No) mapaIdsNos.get(new Integer(posis.idNo));
					piloto.setNoAtual(no);
				}
				break;
			}
		}
	}

	public void selecionouPiloto(Piloto pilotoSelecionado) {
		this.pilotoSelecionado = pilotoSelecionado;
	}

	public Piloto getPilotoSelecionado() {
		if (pilotoSelecionado != null) {
			for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
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
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
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
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
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
			if (getNiveljogo() != InterfaceJogo.DIFICIL_NV)
				gerenciadorVisual.informaMudancaClima();
		}
	}

	public void mudarGiroMotor(Object selectedItem) {
		monitorJogo.mudarGiroMotor(selectedItem);

	}

	public int calculaDiferencaParaProximo(Piloto piloto) {
		return 0;
	}

	public void mudarModoPilotagem(String modo) {
		monitorJogo.mudarModoPilotagem(modo);
	}

	public String getAsaBox(Piloto piloto) {
		return piloto.getAsaBox();
	}

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
			Object combust, Object asa) {
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
		}
	}

	public boolean isCorridaIniciada() {
		return false;
	}

	public int getMediaPontecia() {

		return 0;
	}

	public void verificaProgramacaoBox() {
		gerenciadorVisual.verificaProgramacaoBox();

	}

	@Override
	public void iniciarJogo(ControleCampeonato controleCampeonato)
			throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void mudaPilotoSelecionado() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSemReabastacimento() {
		return dadosParticiparJogo.isSemReabastecimento();
	}

	@Override
	public boolean isSemTrocaPneu() {
		return dadosParticiparJogo.isSemTrocaPeneu();
	}

	@Override
	public void setSemReabastacimento(boolean semReabastacimento) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setSemTrocaPneu(boolean semTrocaPneu) {
		// TODO Auto-generated method stub

	}

	@Override
	public List getCarrosBox() {
		return controleBox.getCarrosBox();
	}

	@Override
	public void mudarPos(int pos) {
		if (pilotoSelecionado != null) {
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
		}
		monitorJogo.mudarPos(pos);
	}

	@Override
	public double getFatorUtrapassagem() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void mudarAutoPos() {
		monitorJogo.mudarAutoPos();
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
		// TODO Auto-generated method stub

	}

	public void travouRodas(TravadaRoda travadaRoda) {
		if (gerenciadorVisual != null && travadaRoda != null)
			gerenciadorVisual.adicinaTravadaRoda(travadaRoda);

	}

	@Override
	public boolean verificaNoPitLane(Piloto piloto) {
		return piloto.getPtosBox() > 0;
	}

	public BufferedImage carregaBackGround(String backGround) {
		if (isCorridaIniciada()
				&& monitorJogo.getLatenciaReal() > Constantes.LATENCIA_MAX) {
			try {
				Thread.sleep(Util.intervalo(5000, 10000));
			} catch (InterruptedException e) {
			}
		}
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

	public void setPosisRec(No no) {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.setPosisRec(no);

	}

	public void setPosisAtual(Point point) {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.setPosisAtual(point);

	}

	public void carregaBackGroundCliente() {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.carregaBackGroundCliente();

	}

	@Override
	public boolean isKers() {
		return dadosParticiparJogo.isKers();
	}

	@Override
	public void setKers(boolean kers) {
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
		monitorJogo.mudarModoKers(!pilotoSelecionado.isAtivarKers());
		return pilotoSelecionado.isAtivarKers();
	}

	@Override
	public int calculaDiferencaParaAnterior(Piloto piloto) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int percetagemDeVoltaCompletada(Piloto pilotoSelecionado) {
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
	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
			Piloto p) {
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
		if (nomeJogador == null) {
			return;
		}
		if (!nomeJogador.equals(pilotoSelecionado.getNomeJogador())) {
			return;
		}
		if (pilotoSelecionado.getNoAtual() != null
				&& !isChovendo()
				&& pilotoSelecionado.getNoAtual().verificaRetaOuLargada()
				&& pilotoSelecionado.getNumeroVolta() > 0
				&& pilotoSelecionado.getPtosBox() == 0
				&& !Carro.MENOS_ASA.equals(pilotoSelecionado.getCarro()
						.getAsa())
				&& (obterCarroNaFrente(pilotoSelecionado) != null && obterCarroNaFrente(
						pilotoSelecionado).getPiloto().getPtosBox() == 0)
				&& calculaSegundosParaProximoDouble(pilotoSelecionado) < 1) {
			monitorJogo.mudarModoDRS(true);
		}
	}

	@Override
	public boolean verificaLag() {
		if (monitorJogo == null) {
			return false;
		}
		return monitorJogo.getLatenciaReal() > Constantes.LATENCIA_MAX;
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
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			piloto.decIndiceTracado();
		}
	}

	@Override
	public int calculaDiffParaProximoRetardatario(Piloto piloto,
			boolean analisaTracado) {
		if (controleEstatisticas == null) {
			System.out.println("controleEstatisticas null");
		}
		return controleEstatisticas.calculaDiffParaProximoRetardatario(piloto,
				analisaTracado);
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
	public boolean isCorridaPausada() {
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
	public void climaEnsolarado() {
		// TODO Auto-generated method stub

	}

	@Override
	public void ativaVerControles() {
		if (gerenciadorVisual != null)
			gerenciadorVisual.ativaVerControles();
	}

	@Override
	public void iniciarJogoMenuLocal(String circuitoSelecionado,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabasteciemto,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean verificaPistaEmborrachada() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabasteciemto) {
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
	public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato,
			int combustivelSelecionado, String asaSelecionado,
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
	public Carro obterCarroNaFrenteRetardatario(Piloto piloto,
			boolean analisaTracado) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void fazPilotoMudarTracado(Piloto piloto, Piloto pilotoFrente) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getDurabilidadeAreofolio() {
		// TODO Auto-generated method stub
		return 0;
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

}
