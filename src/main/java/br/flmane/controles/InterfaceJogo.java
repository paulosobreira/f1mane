package br.flmane.controles;

import br.flmane.MainFrame;
import br.flmane.entidades.*;
import br.flmane.visao.PainelTabelaResultadoFinal;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;

public interface InterfaceJogo {

    public List<String> listaInfo();

    public boolean isSemTrocaPneu();

    public boolean isSemReabastecimento();

    public Integer getCombustBox(Piloto piloto);

    public String getTipoPneuBox(Piloto piloto);

    public String getAsaBox(Piloto piloto);

    public boolean isCorridaTerminada();

    public boolean isCorridaIniciada();

    public void setCorridaTerminada(boolean corridaTerminada);

    public List<No> getNosDoBox();

    public MainFrame getMainFrame();

    public int getMediaPontecia();

    public Circuito getCircuito();

    public List<No> getNosDaPista();

    public List<Carro> getCarros();

    public List<Piloto> getPilotos();

    public void matarTodasThreads();

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

    public void verificaAcidente(Piloto piloto);

    public void efetuarSelecaoPilotoJogador(Object selec, Object tpneu,
                                            Object combust, String nomeJogador, Object asa);

    public boolean mudarModoBox();

    public void setBoxJogadorHumano(Object tpneu, Object combust, Object asa);

    public void selecionaPilotoJogador();

    public void apagarLuz();

    public void processaNovaVolta();

    public boolean isChovendo();

    /**
     * Estado contínuo (0.0 seco a 1.0 chuva plena) usado para interpolar os
     * bônus/penalidades de ganho relacionados a clima, independente do clima
     * categórico exibido por {@link #isChovendo()}/{@link #getClima()}.
     */
    public double getMolhado();

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

    public double calculaDiferencaParaProximoDouble(Piloto psel);

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

    public void setManualTemporario();

    /** Aciona o tick de decisão de piloto automático (ControleAutomacao) para {@code piloto}. */
    public void processarAutomacao(Piloto piloto);

    /** Suspende a automação de {@code piloto} temporariamente — chamado internamente por {@code Piloto.setManualTemporario()}. */
    public void suspenderAutomacaoTemporariamente(Piloto piloto);

    /** Leitura pura de suspensão temporária — chamado internamente por {@code Piloto.isManualTemporario()}. */
    public boolean isAutomacaoSuspensaTemporariamente(Piloto piloto);

    /** Causas de traçado exclusivas de piloto automático — chamadas por {@code Piloto.processaMudarTracado()}. */
    public boolean decideTentarEscaparFilaIndiana(Piloto piloto);

    public boolean decideEvitaColidirComRetardatario(Piloto piloto);

    public boolean decideDesviaRetardatarioMesmoTracado(Piloto piloto);

    public boolean decideEspelhaTracadoCarroAtras(Piloto piloto);

    public boolean decideRecentralizaSemTrafego(Piloto piloto);

    /** Aciona o uso de DRS ({@code ControleDrs}) para {@code piloto}. */
    public void processarUsoDRS(Piloto piloto);

    /** Aciona a frenagem na zona de frenagem ({@code ControleFreio}) para {@code piloto}. */
    public void processarFreioNaReta(Piloto piloto);

    /** Aciona a derrapagem (traçado 0 → 1/2, {@code ControleEscapada}) para {@code piloto}. */
    public void processarDerrapagem(Piloto piloto);

    /** Aciona a escapada da pista (traçado 1/2 ↔ 4/5, {@code ControleEscapada}) para {@code piloto}. */
    public void processarEscapadaDaPista(Piloto piloto);

    public BufferedImage obterCarroCima(Piloto piloto);

    public void ajusteUltrapassagem(Piloto piloto, Piloto pilotoFrente);

    public List<No> obterPista(No noPiloto);

    public BufferedImage obterCarroLado(Piloto piloto);

    public BufferedImage obterCapacete(Piloto piloto);

    public No getNoEntradaBox();

    public void travouRodas(Piloto piloto);

    public void travouRodasPorColisao(Piloto piloto);

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

    public String obterSegundosParaRival(Piloto pilotoSelecionado);

    public void verificaDesafioCampeonatoPiloto();

    public void aumentaFatorAcidade();

    public void diminueFatorAcidade();

    public void setPontosPilotoLargada(long ptosPista);

    public boolean asfaltoAbrasivo();

    public boolean asfaltoAbrasivoReal();

    public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
                                    Piloto p);

    public void driveThru();

    public int porcentagemChuvaCircuito();

    public JPanel painelNarracao();

    public void forcaSafatyCar();

    public No obterProxCurva(No noAtual);

    public boolean isNoZonaFrenagem(No no);

    public boolean verificaLag();

    public int getLag();

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
                                     String pneuSelecionado, boolean safetycar, boolean simulacao) throws Exception;

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

    public void processaMudancaEquipeCampeontato() throws Exception;

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

    public void forcaQuebraAereofolio(Piloto piloto);

    public boolean isAtualizacaoSuave();

    public boolean isSafetyCar();

    public void setAtualizacaoSuave(boolean atualizacaoSuave);

    public void setRecebeuBanderada(Piloto piloto);

    public Piloto obterPilotoPorId(String id);

    public Piloto getPilotoBateu();

    public boolean verificaEntradaBox(Piloto piloto);

    public Double getFatorBoxTemporada();

    public void desqualificaPiloto(Piloto piloto);

    public String getVantagem();

    public void setVantagem(String vantagem);

    double getFatorConsumoPneuSemTroca();

    double getFatorConsumoCombustivelSemReabastecimento();

    long tempoCicloCircuito();

    /**
     * Tempo médio de volta real, em milissegundos, calculado a partir das voltas já
     * registradas do piloto líder. Antes da primeira volta do líder fechar, retorna
     * a estimativa {@code nosDaPista.size() * tempoCicloCircuito()}.
     */
    long tempoMedioVoltaMs();

    String getAutomaticoManual();

    public GameRandom getRandom();
}