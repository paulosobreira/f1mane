package sowbreira.f1mane.controles;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Campeonato;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.ConstrutoresPontosCampeonato;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.PilotosPontosCampeonato;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;

public interface InterfaceJogo {
	public static boolean DEBUG_SEM_CHUVA = false;
	public static String NORMAL = "NORMAL";
	public static String FACIL = "FACIL";
	public static String DIFICIL = "DIFICIL";
	public static double FACIL_NV = .3;
	public static double MEDIO_NV = .5;
	public static double DIFICIL_NV = .7;
	public static int CARGA_ERS = 100;
	public static int DURABILIDADE_AREOFOLIO = 4;

	public List<String> listaInfo();

	public boolean isSemTrocaPneu();

	public boolean isSemReabastacimento();

	public Integer getCombustBox(Piloto piloto);

	public String getTipoPneuBox(Piloto piloto);
	
	public void setTipoPneuBox(Piloto piloto);

	public String getAsaBox(Piloto piloto);

	public void setNiveljogo(double niveljogo);

	public boolean isCorridaTerminada();

	public boolean isCorridaIniciada();

	public void setCorridaTerminada(boolean corridaTerminada);

	public List<No> getNosDoBox();

	public MainFrame getMainFrame();

	public int getMediaPontecia();

	public String getNivelCorrida();

	public void setNivelCorrida(String nivelCorrida);

	public Circuito getCircuito();

	public List<No> getNosDaPista();

	public List<Carro> getCarros();

	public List<Piloto> getPilotos();

	public void matarTodasThreads();

	/**
	 * Quanto Mais dificil o jogo mais ira de retornar true
	 * 
	 * @return boolean
	 */
	public boolean verificaNivelJogo();

	public String getClima();

	public void atualizaIndexTracadoPilotos();

	public void info(String info);

	public void infoPrioritaria(String info);

	public int porcentagemCorridaConcluida();

	public int getNumVoltaAtual();

	public int totalVoltasCorrida();

	public boolean verificaBoxOcupado(Carro carro);

	public String calculaSegundosParaLider(Piloto pilotoSelecionado);

	public boolean verificaUltimaVolta();

	public void processaVoltaRapida(Piloto piloto);

	public int getCicloAtual();

	public void verificaVoltaMaisRapidaCorrida(Piloto piloto);

	public double obterIndicativoCorridaCompleta();

	public Volta obterMelhorVolta();

	public double getNiveljogo();

	public void verificaAcidente(Piloto piloto);

	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
			Object combust, String nomeJogador, Object asa);

	public boolean mudarModoBox();

	public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa);

	public void selecionaPilotoJogador();

	public void apagarLuz();

	public void processaNovaVolta();

	public boolean isChovendo();

	public void informaMudancaClima();

	public void pausarJogo();

	public PainelTabelaResultadoFinal obterResultadoFinal();

	public boolean isSafetyCarNaPista();

	public SafetyCar getSafetyCar();

	public boolean isSafetyCarVaiBox();

	public Carro obterCarroNaFrente(Piloto piloto);

	public Carro obterCarroAtras(Piloto piloto);

	public String calculaSegundosParaProximo(Piloto psel);

	public String calculaSegundosParaProximo(Piloto psel, int diferenca);

	public double getIndexVelcidadeDaPista();

	public Map getCircuitos();

	public void iniciarJogo() throws Exception;

	public void iniciarJogo(ControleCampeonato controleCampeonato)
			throws Exception;

	public void exibirResultadoFinal();

	public void abandonar();

	public void desenhaQualificacao();

	public void zerarMelhorVolta();

	public void adicionarInfoDireto(String string);

	public void atulizaTabelaPosicoes();

	public void selecionouPiloto(Piloto pilotoSelecionado);

	public Piloto getPilotoSelecionado();

	public int setUpJogadorHumano(Piloto pilotoJogador, Object tpPneu,
			Object combust, Object asa);

	public Volta obterMelhorVolta(Piloto pilotoSelecionado);

	public Piloto getPilotoJogador();

	public void mudarGiroMotor(Object selectedItem);

	public int calculaDiferencaParaProximo(Piloto piloto);

	public double calculaSegundosParaProximoDouble(Piloto psel);

	public void mudarModoPilotagem(String modo);

	public void setMainFrame(MainFrame mainFrame);

	public boolean isModoQualify();

	public void tabelaComparativa();

	public void iniciaJanela();

	public void mudaPilotoSelecionado();

	public List getCarrosBox();

	public void mudarTracado(int i);

	/**
	 * Minimo 0.5 = Mais dificil de passar; Maximo 1.0 = Mais facil de passar;
	 */
	public double getFatorUtrapassagem();

	public String getTemporada();

	public void setTemporada(String string);

	public void mudarAutoPos(boolean autoPos);

	public BufferedImage obterCarroCima(Piloto piloto);

	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente);

	public List<No> obterPista(No noPiloto);

	public BufferedImage obterCarroLado(Piloto piloto);

	public BufferedImage obterCapacete(Piloto piloto);

	public No getNoEntradaBox();

	public void travouRodas(Piloto piloto);

	public No obterNoPorId(int idNo);

	public Integer obterIdPorNo(No no);

	public List<No> obterNosPista();

	public boolean verificaNoPitLane(Piloto piloto);

	public boolean verificaSaidaBox(Piloto piloto);

	public BufferedImage carregaBackGround(String backGround);

	public boolean isErs();

	public void setErs(boolean kers);

	public boolean isDrs();

	public void setDrs(boolean drs);

	public boolean mudarModoDRS();

	public boolean mudarModoKers();

	public int calculaDiferencaParaAnterior(Piloto piloto);

	public int percetagemDeVoltaConcluida(Piloto pilotoSelecionado);

	public boolean verirficaDesafiandoCampeonato(Piloto piloto);

	public boolean verificaCampeonatoComRival();

	public String calculaSegundosParaRival(Piloto pilotoSelecionado);

	public void verificaDesafioCampeonatoPiloto();

	public void aumentaFatorAcidade();

	public void diminueFatorAcidade();

	public void setPontosPilotoLargada(long ptosPista);

	public boolean asfaltoAbrasivo();

	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
			Piloto p);

	public void driveThru();

	public int porcentagemChuvaCircuito();

	public boolean isBoxRapido();

	public JPanel painelNarracao();

	public void forcaSafatyCar();

	public No obterProxCurva(No noAtual);

	public boolean verificaLag();

	public int getLag();

	public int obterLadoEscape(Point pontoDerrapada);

	public void decrementaTracado();

	public int calculaDiffParaProximoRetardatario(Piloto piloto,
			boolean analisaTracado);

	public No getNoSaidaBox();

	public void selecionaPilotoCima();

	public void selecionaPilotoBaixo();

	public boolean isJogoPausado();

	public void descontaTempoPausado(Volta volta);

	public void criarCampeonato() throws Exception;

	public void criarCampeonatoPiloto() throws Exception;

	public Campeonato continuarCampeonato();

	public void dadosPersistenciaCampeonato(Campeonato campeonato);

	public void proximaCorridaCampeonato();

	public void climaLimpo();

	public void climaChuvoso();

	public void ativaVerControles();

	public void iniciarJogoMenuLocal(String circuitoSelecionado,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabastecimento,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado) throws Exception;

	public boolean verificaPistaEmborrachada();

	public Campeonato criarCampeonatoPiloto(List cirucitosCampeonato,
			String temporadaSelecionada, int numVoltasSelecionado,
			int turbulenciaSelecionado, String climaSelecionado,
			String nivelSelecionado, Piloto pilotoSelecionado, boolean kers,
			boolean drs, boolean trocaPneus, boolean reabastecimento);

	public void voltaMenuPrincipal();

	public List<PilotosPontosCampeonato> geraListaPilotosPontos();

	public List<ConstrutoresPontosCampeonato> geraListaContrutoresPontos();

	public void iniciarJogoCapeonatoMenuLocal(Campeonato campeonato,
			int combustivelSelecionado, String asaSelecionado,
			String pneuSelecionado, String clima) throws Exception;

	public void continuarCampeonato(Campeonato campeonato);

	public Piloto obterRivalCampeonato();

	public Carro obterCarroNaFrenteRetardatario(Piloto piloto,
			boolean analisaTracado);

	public void desenhouQualificacao();

	public void detalhesCorridaCampeonato();

	public boolean safetyCarUltimas3voltas();

	public double getFatorAcidente();

	public List<Piloto> getPilotosCopia();

	public boolean verificaInfoRelevante(Piloto piloto);

	public Campeonato continuarCampeonatoXml();

	public void processaMudancaEquipeCampeontato();

	public Campeonato continuarCampeonatoXmlDisco();

	public No obterCurvaAnterior(No noAtual);

	public int getFPS();

	public void pilotoSelecionadoMinimo();

	public void pilotoSelecionadoNormal();

	public void pilotoSelecionadoMaximo();

	public boolean mostraTipoPneuAdversario();

	public JPanel painelDebug();

	public void atualizaInfoDebug();

	public void atualizaInfoDebug(StringBuilder buffer);

	public void forcaQuerbraAereofolio();

	public boolean isAtualizacaoSuave();

	public boolean isSafetyCar();

	public void setAtualizacaoSuave(boolean atualizacaoSuave);

	public void setRecebeuBanderada(Piloto piloto);

	public Piloto obterPilotoPorId(String id);

	public Piloto getPilotoBateu();

	public boolean verificaEntradaBox(Piloto piloto);

	public Double getFatorBoxTemporada();

	void travouRodas(Piloto piloto, boolean semFumaca);

	public void desqualificaPiloto(Piloto piloto);

}