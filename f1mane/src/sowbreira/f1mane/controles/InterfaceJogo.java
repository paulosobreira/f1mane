package sowbreira.f1mane.controles;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sowbreira.f1mane.MainFrame;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.visao.PainelTabelaResultadoFinal;

public interface InterfaceJogo {
	public static boolean VALENDO = true;
	public static String NORMAL = "NORMAL";
	public static String FACIL = "FACIL";
	public static String DIFICIL = "DIFICIL";
	public static double FACIL_NV = .3;
	public static double MEDIO_NV = .5;
	public static double DIFICIL_NV = .7;
	public static String VERSAO = "4.18";

	public boolean isSemTrocaPneu();

	public void setSemTrocaPneu(boolean semTrocaPneu);

	public boolean isSemReabastacimento();

	public void setSemReabastacimento(boolean semReabastacimento);

	public Integer getCombustBox(Piloto piloto);

	public String getTipoPeneuBox(Piloto piloto);

	public String getAsaBox(Piloto piloto);

	public void setNiveljogo(double niveljogo);

	public boolean isCorridaTerminada();

	public boolean isCorridaIniciada();

	public void setCorridaTerminada(boolean corridaTerminada);

	public List getNosDoBox();

	public MainFrame getMainFrame();

	public int getMediaPontecia();

	public String getNivelCorrida();

	public void setNivelCorrida(String nivelCorrida);

	public Circuito getCircuito();

	public List getNosDaPista();

	public List getCarros();

	public List getPilotos();

	public void matarTodasThreads();

	/**
	 * Quanto Mais dificil o jogo mais facil de retornar true
	 * 
	 * @return boolean
	 */
	public boolean verificaNivelJogo();

	public String getClima();

	public void atualizaPainel();

	public void info(String info);

	public void infoPrioritaria(String info);

	public int porcentagemCorridaCompletada();

	public int getNumVoltaAtual();

	public int totalVoltasCorrida();

	public int getQtdeTotalVoltas();

	public boolean verificaUltimasVoltas();

	public boolean verificaBoxOcupado(Carro carro);

	public String calculaSegundosParaLider(Piloto pilotoSelecionado);

	public boolean verificaUltima();

	public void processaVoltaRapida(Piloto piloto);

	public int getCicloAtual();

	public void verificaVoltaMaisRapidaCorrida(Piloto piloto);

	public double obterIndicativoCorridaCompleta();

	public Volta obterMelhorVolta();

	public double verificaUltraPassagem(Piloto piloto, double novoModificador);

	public double getNiveljogo();

	public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
			Object combust, String nomeJogador, Object asa);

	public boolean mudarModoAgressivo();

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

	public Carro obterCarroAtraz(Piloto piloto);

	public String calculaSegundosParaProximo(Piloto psel);

	public double getIndexVelcidadeDaPista();

	public Map getCircuitos();

	public void iniciarJogo() throws Exception;

	public void iniciarJogo(ControleCampeonato controleCampeonato)
			throws Exception;

	public void exibirResultadoFinal();

	public void abandonar();

	public void desenhaQualificacao();

	public long getTempoCiclo();

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

	public void verificaProgramacaoBox();

	public void mudaPilotoSelecionado();

	public List getCarrosBox();

	public void mudarPos(int i);

	public double getFatorUtrapassagem();

	public Set getSetChegada();

	public String getTemporada();

	public void setTemporada(String string);

	public void mudarAutoPos();

	public BufferedImage obterCarroCima(Piloto piloto);

	public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente);

	public void verificaAcidenteUltrapassagem(boolean agressivo, Piloto piloto,
			Piloto pilotoFrente);

	public List obterPista(Piloto piloto);

	public BufferedImage obterCarroLado(Piloto piloto);

	public No getNoEntradaBox();

	public void setZoom(double d);

	public void travouRodas(Piloto piloto);

	public No obterNoPorId(int idNo);

	public Integer obterIdPorNo(No no);

	public List obterNosPista();

	public boolean verificaNoPitLane(Piloto piloto);

	public BufferedImage carregaBackGround(String backGround);

}