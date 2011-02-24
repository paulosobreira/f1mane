package sowbreira.f1mane.paddock.applet;

import java.awt.MediaTracker;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.controles.ControleBox;
import sowbreira.f1mane.controles.ControleCampeonato;
import sowbreira.f1mane.controles.ControleEstatisticas;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Clima;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
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
import br.nnpe.ImageUtil;
import br.nnpe.Logger;

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
	private boolean syncBox;
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

	public void setDadosJogo(DadosJogo dadosJogo) {
		this.dadosJogo = dadosJogo;
		if (dadosJogo != null && dadosJogo.getPilotosList() != null
				&& !dadosJogo.getPilotosList().isEmpty())
			pilotos = dadosJogo.getPilotosList();
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
			gerenciadorVisual = new GerenciadorVisual(this);
			gerenciadorVisual.iniciarInterfaceGraficaJogo();
			controleEstatisticas = new ControleEstatisticas(this);
			controleEstatisticas
					.inicializarThreadConsumidoraInfo(2000 + ((int) Math
							.random() * 500));

		} catch (Exception e) {
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
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
		gerenciadorVisual.atualizaPainel();

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
		gerenciadorVisual.atulizaTabelaPosicoes();
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

	public void desenhaQualificacao() {
		if (gerenciadorVisual == null) {
			preparaGerenciadorVisual();
		}
		gerenciadorVisual.desenhaQualificacao();

	}

	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
			Object combust, String nomeJogador, Object asa) {
		// TODO Auto-generated method stub

	}

	public void exibirResultadoFinal() {
		mainFrame
				.exibirResiltadoFinal(gerenciadorVisual.exibirResultadoFinal());
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
		// TODO Auto-generated method stub
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

	public List getPilotos() {
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
			}
			if (controleEstatisticas != null) {
				controleEstatisticas.setConsumidorAtivo(false);
			}
			if (threadMonitoraJogoOnline != null) {
				threadMonitoraJogoOnline.interrupt();
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

	public boolean verificaUltima() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean verificaUltimasVoltas() {
		// TODO Auto-generated method stub
		return false;
	}

	public double verificaUltraPassagem(Piloto piloto, double novoModificador) {
		// TODO Auto-generated method stub
		return 0;
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
		gerenciadorVisual.setTempoSleep(0);

	}

	private Vector posisBuffer = new Vector();
	private Thread consumidorPosis = null;
	private Posis posisBuff;

	public void atualizaPosicaoPiloto(Posis posis) {
		posisBuffer.add(posis);
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.getId() == posis.idPiloto) {
				piloto.setAgressivo(posis.agressivo);
				piloto.setJogadorHumano(posis.humano);
				piloto.setTracado(posis.tracado);
				piloto.setAutoPos(posis.autoPos);
				break;
			}

		}
		iniciaConsumidorPosis();
	}

	private void iniciaConsumidorPosis() {
		if (consumidorPosis != null) {
			return;
		}
		consumidorPosis = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!isCorridaTerminada()) {
					if (!posisBuffer.isEmpty()) {
						posisBuff = (Posis) posisBuffer.remove(0);
					}
					if (posisBuff == null) {
						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
						}
					}
					for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
						Piloto piloto = (Piloto) iter.next();
						if (piloto.getId() == posisBuff.idPiloto) {
							if (posisBuff.idNo >= -1) {
								No no = (No) mapaIdsNos.get(new Integer(
										posisBuff.idNo));
								int index = no.getIndex();
								int diff = index - getNosDaPista().size();
								if (diff >= 0) {
									index = diff;
								}
								if (piloto.getNoAtual() != null) {
									int indexPiloto = piloto.getNoAtual()
											.getIndex();
									if (indexPiloto < index
											|| piloto.getNumeroVolta() < posisBuff.volta) {
										indexPiloto++;
										int diffPiloto = indexPiloto
												- getNosDaPista().size();
										if (diffPiloto >= 0) {
											indexPiloto = diffPiloto;
										}
										No newNo = (No) mapaIdsNos
												.get(new Integer(indexPiloto));
										piloto.setNoAtual(newNo);
									}
								} else {
									piloto.setNoAtual(no);
								}
								try {
									Thread.sleep(50);
								} catch (InterruptedException e) {
								}
							}
							break;
						}

					}
				}

			}
		});
		consumidorPosis.start();
	}

	public void atualizaPosicaoPilotoOld(Posis posis) {

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
			gerenciadorVisual.informaMudancaClima();
			clima = climaNovo;
		}
	}

	public void mudarGiroMotor(Object selectedItem) {
		monitorJogo.mudarGiroMotor(selectedItem);

	}

	public int calculaDiferencaParaProximo(Piloto piloto) {
		// TODO Auto-generated method stub
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
		} else if (!syncBox) {
			gerenciadorVisual.sincronizarMenuInicioMenuBox(dadosParticiparJogo
					.getTpPnueu(), dadosParticiparJogo.getCombustivel(),
					dadosParticiparJogo.getAsa());
			syncBox = true;
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
	public Set getSetChegada() {
		return new HashSet();
	}

	@Override
	public void mudarAutoPos() {
		monitorJogo.mudarAutoPos();
	}

	@Override
	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente) {
		// TODO Auto-generated method stub

	}

	@Override
	public void verificaAcidenteUltrapassagem(boolean agressivo, Piloto piloto,
			Piloto pilotoFrente) {
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
	public void setZoom(double d) {
		if (gerenciadorVisual == null) {
			return;
		}
		gerenciadorVisual.setZoom(d);

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
		// TODO Auto-generated method stub
		return false;
	}

	public BufferedImage carregaBackGround(String backGround) {
		if (mainFrame.getApplet() == null) {
			Logger.logar("mainFrame.getApplet()==null ");
			return null;
		}
		URL url = null;
		try {
			String caminho = mainFrame.getApplet().getCodeBase()
					+ "sowbreira/f1mane/recursos/" + backGround;
			Logger.logar("Caminho Carregar Bkg " + caminho);
			url = new URL(caminho);
			ImageIcon icon = new ImageIcon(url);
			BufferedImage buff = ImageUtil.toBufferedImage(icon.getImage());
			if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
				Logger.logar("Status " + icon.getImageLoadStatus()
						+ " Nao Carregado " + url);
				return null;
			} else {
				return buff;
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}
		return null;
	}
}
