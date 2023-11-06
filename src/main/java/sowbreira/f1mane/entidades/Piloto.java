package sowbreira.f1mane.entidades;

import br.f1mane.controles.InterfaceJogo;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.visao.PainelCircuito;
import br.nnpe.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.*;

/**
 * @author Paulo Sobreira
 */
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Piloto implements Serializable, PilotoSuave {
    private static final long serialVersionUID = 698992658460848522L;
    public static final String AGRESSIVO = "AGRESSIVO";
    public static final String NORMAL = "NORMAL";
    public static final String LENTO = "LENTO";
    public static final int METADE_CARRO = 20;

    private int id;
    private Carro carro = new Carro();
    private String nome;
    private String nomeAbreviado;
    private String nomeCarro;
    private String nomeJogador;
    private String imgJogador;
    private int posicao;
    private int posicaoBandeirada;
    private boolean desqualificado;
    private boolean jogadorHumano;
    private int numeroVolta;
    private Volta melhorVolta;
    private String vantagem;
    private int qtdeParadasBox;
    private String tempoVoltaQualificacao;
    private int pontosCorrida;
    private long porcentagemPontosCorrida;
    private int habilidade;
    private int habilidadeReal;

    private String temporadaCapaceteLivery;
    private String temporadaCarroLivery;
    private String idCapaceteLivery;
    private String idCarroLivery;

    @JsonIgnore
    private String tokenJogador;
    @JsonIgnore
    private boolean boxSaiuNestaVolta = false;
    @JsonIgnore
    private int carX;
    @JsonIgnore
    private int carY;
    @JsonIgnore
    private double ganho;
    @JsonIgnore
    private int velocidadeExibir;
    @JsonIgnore
    private long ptosPista;
    @JsonIgnore
    private int tracado;
    @JsonIgnore
    private boolean box;
    @JsonIgnore
    private boolean agressivo = true;
    @JsonIgnore
    private No noAtual = new No();
    @JsonIgnore
    private int stress;
    @JsonIgnore
    private String modoPilotagem = NORMAL;
    @JsonIgnore
    private Volta voltaAtual;
    @JsonIgnore
    private Volta ultimaVolta;
    @JsonIgnore
    private String segundosParaLider;
    @JsonIgnore
    private String tipoPneuBox;
    @JsonIgnore
    private String asaBox;
    @JsonIgnore
    private int qtdeCombustBox;
    @JsonIgnore
    private boolean ativarErs;
    @JsonIgnore
    private boolean ativarDRS;
    @JsonIgnore
    private boolean alertaMotor;
    @JsonIgnore
    private boolean alertaAerefolio;
    @JsonIgnore
    private String calculaSegundosParaProximo;
    @JsonIgnore
    private int cargaErsVisual;
    @JsonIgnore
    private int cargaKersOnline;
    @JsonIgnore
    private final List<String> ultimas5Voltas = new ArrayList<String>();
    @JsonIgnore
    private String calculaSegundosParaAnterior;
    @JsonIgnore
    private Carro carroPilotoAtras;
    @JsonIgnore
    private Carro carroPilotoDaFrente;
    @JsonIgnore
    private No noAtualSuave;
    @JsonIgnore
    private int setaCima;
    @JsonIgnore
    private int setaBaixo;
    @JsonIgnore
    protected String tipoPneuJogador;
    @JsonIgnore
    protected String asaJogador;
    @JsonIgnore
    protected Integer combustJogador;
    @JsonIgnore
    private int indiceTracado;
    @JsonIgnore
    private Double angulo;
    @JsonIgnore
    private int ganhoTotalReta;
    @JsonIgnore
    private int ganhoBrutoReta;
    @JsonIgnore
    private int ganhoTotalAlta;
    @JsonIgnore
    private int ganhoBrutoAlta;
    @JsonIgnore
    private int ganhoTotalBaixa;
    @JsonIgnore
    private int ganhoBrutoBaixa;
    @JsonIgnore
    private int ganhoSuave;
    @JsonIgnore
    private int ultModificador;
    @JsonIgnore
    private boolean limiteGanho;
    @JsonIgnore
    private boolean acelerando;
    @JsonIgnore
    private double ultGanhoReta = 0;
    @JsonIgnore
    private Integer ultimoConsumoCombust;
    @JsonIgnore
    private Integer ultimoConsumoPneu;
    @JsonIgnore
    private int velocidade;
    @JsonIgnore
    private int velocidadeAnterior;
    @JsonIgnore
    private transient String setUpIncial;
    @JsonIgnore
    private String nomeOriginal;
    @JsonIgnore
    private transient int habilidadeAntesQualify;
    @JsonIgnore
    private int ultimoIndice;
    @JsonIgnore
    private int tracadoAntigo;
    @JsonIgnore
    private double ganhoMax = Integer.MIN_VALUE;
    @JsonIgnore
    private double distanciaEscape = Double.MAX_VALUE;
    @JsonIgnore
    private int posicaoInicial;
    @JsonIgnore
    private No noAnterior = new No();
    @JsonIgnore
    private transient int ciclosDesconcentrado;
    @JsonIgnore
    private transient int porcentagemCombustUltimaParadaBox;
    @JsonIgnore
    private long ciclosVoltaQualificacao;
    @JsonIgnore
    private long parouNoBoxMilis;
    @JsonIgnore
    private long saiuDoBoxMilis;
    @JsonIgnore
    private long ultimaMudancaPos;
    @JsonIgnore
    private int novoModificador;
    @JsonIgnore
    private int novoModificadorCarro;
    @JsonIgnore
    private boolean driveThrough;
    @JsonIgnore
    private boolean processouVoltaBox;
    @JsonIgnore
    private Double maxGanhoBaixa = new Double(0);
    @JsonIgnore
    private Double maxGanhoAlta = new Double(0);
    @JsonIgnore
    private int contTravouRodas;
    @JsonIgnore
    private boolean travouRodas;
    @JsonIgnore
    private boolean marcaPneu;
    @JsonIgnore
    private boolean faiscas;
    @JsonIgnore
    private boolean freiandoReta;
    @JsonIgnore
    private boolean retardaFreiandoReta;
    @JsonIgnore
    private int tracadoDelay;
    @JsonIgnore
    private int naoDesenhaEfeitos;
    @JsonIgnore
    private final int tamanhoBufferGanho = 10;
    @JsonIgnore
    private boolean colisaoDiantera;
    @JsonIgnore
    private boolean colisaoCentro;
    @JsonIgnore
    private double limiteEvitarBatrCarroFrente;
    @JsonIgnore
    private boolean evitaBaterCarroFrente;
    @JsonIgnore
    private boolean problemaLargada;
    @JsonIgnore
    private boolean recebeuBanderada;
    @JsonIgnore
    private boolean devagarAposBanderada;
    @JsonIgnore
    private int mudouTracadoReta;
    @JsonIgnore
    private int indexRefEscape;
    @JsonIgnore
    private int calculaDiferencaParaProximo;
    @JsonIgnore
    private int voltaMensagemLento;
    @JsonIgnore
    private int ptosBox;
    @JsonIgnore
    private int paradoBox;
    @JsonIgnore
    private int calculaDiffParaProximoRetardatario;
    @JsonIgnore
    private int calculaDiffParaProximoRetardatarioMesmoTracado;
    @JsonIgnore
    private int calculaDiferencaParaAnterior = Integer.MAX_VALUE;
    @JsonIgnore
    private List<Volta> voltas = new ArrayList<Volta>();
    @JsonIgnore
    private final Set<String> votosDriveThru = new HashSet<String>();
    @JsonIgnore
    private final List<Integer> ultsConsumosCombustivel = new LinkedList<Integer>();
    @JsonIgnore
    private final List<Integer> ultsConsumosPneu = new LinkedList<Integer>();
    @JsonIgnore
    private final List<Double> ganhosBaixa = new ArrayList<Double>();
    @JsonIgnore
    private final List<Double> ganhosAlta = new ArrayList<Double>();
    @JsonIgnore
    private final List<Double> ganhosReta = new ArrayList<Double>();
    @JsonIgnore
    private ArrayList<Double> listGanho;
    @JsonIgnore
    private Point p0;
    @JsonIgnore
    private Point p1;
    @JsonIgnore
    private Point p2;
    @JsonIgnore
    private Point p5;
    @JsonIgnore
    private Point p4;
    @JsonIgnore
    private Point pontoEscape;
    @JsonIgnore
    private Rectangle diateira;
    @JsonIgnore
    private Rectangle centro;
    @JsonIgnore
    private Rectangle trazeira;
    @JsonIgnore
    private Rectangle diateiraColisao;
    @JsonIgnore
    private Rectangle centroColisao;
    @JsonIgnore
    private Rectangle trazeiraColisao;
    @JsonIgnore
    private Carro carroPilotoDaFrenteRetardatario;
    @JsonIgnore
    private Piloto colisao;
    @JsonIgnore
    private int contadorPodeAcionarDRS;
    @JsonIgnore
    private int contadorIndiceUltimoAcionamentoDRS;
    protected static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private static final int CONTADOR_LIBERAR_DRS = 20;
    private boolean temMotor;
    private boolean temCombustivel;
    private boolean superAquecido;
    private int porcentagemDesgastePneus;
    private int porcentagemCombustivel;
    private int porcentagemMotor;
    private int porcentagemCorridaRestante;
    private boolean temPneu;

    public int getGanhoSuave() {
        return ganhoSuave;
    }

    public double getGanho() {
        return ganho;
    }

    public List getGanhosBaixa() {
        return ganhosBaixa;
    }

    public List getGanhosAlta() {
        return ganhosAlta;
    }

    public List getGanhosReta() {
        return ganhosReta;
    }

    public void setGanhoSuave(int ganhoSuave) {
        this.ganhoSuave = ganhoSuave;
    }

    public No getNoAtualSuave() {
        return noAtualSuave;
    }

    public void setNoAtualSuave(No noAtualSuave) {
        setNoAnterior(getNoAtualSuave());
        this.noAtualSuave = noAtualSuave;
    }

    @JsonIgnore
    public int getHabilidadeAntesQualify() {
        return habilidadeAntesQualify;
    }

    public void setHabilidadeAntesQualify(int habilidadeAntesQualify) {
        this.habilidadeAntesQualify = habilidadeAntesQualify;
    }

    public boolean decContTravouRodas() {
        if (contTravouRodas > 0) {
            contTravouRodas--;
            if (contTravouRodas > 0 && noAtual != null && noAtual.verificaRetaOuLargada()) {
                contTravouRodas--;
            }
            return true;
        }
        return false;
    }

    public void setTravouRodas(int contTravouRodas) {
        setTravouRodas(contTravouRodas > 0);
        this.contTravouRodas = contTravouRodas;
    }

    public int getContTravouRodas() {
        return contTravouRodas;
    }

    public Rectangle getDiateira() {
        return diateira;
    }

    public int getTracadoAntigo() {
        return tracadoAntigo;
    }

    public void setTracadoAntigo(int tracadoAntigo) {
        this.tracadoAntigo = tracadoAntigo;
    }

    public boolean isDriveThrough() {
        return driveThrough;
    }

    public void setDriveThrough(boolean driveThrough) {
        this.driveThrough = driveThrough;
    }

    public boolean isAtivarErs() {
        return ativarErs;
    }

    public int getCargaKersOnline() {
        return cargaKersOnline;
    }

    public void setCargaKersOnline(int cargaKersOnline) {
        this.cargaKersOnline = cargaKersOnline;
    }

    public int getCargaErsVisual() {
        return cargaErsVisual;
    }

    public void setCargaErsVisual(int cargaErsVisual) {
        this.cargaErsVisual = cargaErsVisual;
    }

    public void setAtivarErs(boolean ativarErs) {
        this.ativarErs = ativarErs;
    }

    public boolean isAtivarDRS() {
        return ativarDRS;
    }

    public void setAtivarDRS(boolean ativarDRS) {
        this.ativarDRS = ativarDRS;
    }

    public void setDiateira(Rectangle diateira) {
        this.diateira = diateira;
    }

    public Rectangle getCentro() {
        return centro;
    }

    public void setCentro(Rectangle centro) {
        this.centro = centro;
    }

    public Rectangle getTrazeira() {
        return trazeira;
    }

    public void setTrazeira(Rectangle trazeira) {
        this.trazeira = trazeira;
    }

    public Double getAngulo() {
        return angulo;
    }

    public void setAngulo(Double angulo) {
        this.angulo = angulo;
    }

    @JsonIgnore
    public Point getP0() {
        return getNoAtual().getPoint();
    }

    public Point getP1() {
        return p1;
    }

    public void setP1(Point p1) {
        this.p1 = p1;
    }

    public Point getP5() {
        return p5;
    }

    public void setP5(Point p5) {
        this.p5 = p5;
    }

    public Point getP4() {
        return p4;
    }

    public void setP4(Point p4) {
        this.p4 = p4;
    }

    public Point getP2() {
        return p2;
    }

    public void setP2(Point p2) {
        this.p2 = p2;
    }

    public int getTracado() {
        return tracado;
    }

    public void setTracado(int tracado) {
        this.tracado = tracado;
    }

    public int getSetaCima() {
        return setaCima;
    }

    public void setSetaCima(int setaCima) {
        this.setaCima = setaCima;
    }

    public int getSetaBaixo() {
        return setaBaixo;
    }

    public void setSetaBaixo(int setaBaixo) {
        this.setaBaixo = setaBaixo;
    }

    public int getCarX() {
        return carX;
    }

    public void setCarX(int carX) {
        this.carX = carX;
    }

    public int getCarY() {
        return carY;
    }

    public void setCarY(int carY) {
        this.carY = carY;
    }

    public int getUltimoIndice() {
        return ultimoIndice;
    }

    public void setUltimoIndice(int ultimoIndice) {
        this.ultimoIndice = ultimoIndice;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((nome == null) ? 0 : nome.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Piloto other = (Piloto) obj;
        if (nome == null) {
            return other.nome == null;
        } else return nome.equals(other.nome);
    }

    public String getTipoPneuJogador() {
        return tipoPneuJogador;
    }

    public boolean isDevagarAposBanderada() {
        return devagarAposBanderada;
    }

    public void setDevagarAposBanderada(boolean devagarAposBanderada) {
        this.devagarAposBanderada = devagarAposBanderada;
    }

    public void setTipoPneuJogador(String tipoPneuJogador) {
        this.tipoPneuJogador = tipoPneuJogador;
    }

    public String getAsaJogador() {
        return asaJogador;
    }

    public void setAsaJogador(String asaJogador) {
        this.asaJogador = asaJogador;
    }

    public Integer getCombustJogador() {
        return combustJogador;
    }

    public void setCombustJogador(Integer combustJogador) {
        this.combustJogador = combustJogador;
    }

    public int getVelocidade() {
        return velocidade;
    }

    public String getNomeJogador() {
        return nomeJogador;
    }

    public int getId() {
        return id;
    }

    public int getQtdeCombustBox() {
        return qtdeCombustBox;
    }

    public void setQtdeCombustBox(int qtdeCombustBox) {
        this.qtdeCombustBox = qtdeCombustBox;
    }

    public String getTipoPneuBox() {
        return tipoPneuBox;
    }

    public void setTipoPneuBox(String tipoPneuBox) {
        this.tipoPneuBox = tipoPneuBox;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }

    public String getImgJogador() {
        return imgJogador;
    }

    public void setImgJogador(String imgJogador) {
        this.imgJogador = imgJogador;
    }

    public void setVoltas(List<Volta> voltas) {
        this.voltas = voltas;
    }

    public long getParouNoBoxMilis() {
        return parouNoBoxMilis;
    }

    public long getCiclosVoltaQualificacao() {
        return ciclosVoltaQualificacao;
    }

    public void setCiclosVoltaQualificacao(long ciclosVoltaQualificacao) {
        this.ciclosVoltaQualificacao = ciclosVoltaQualificacao;
    }

    public void setParouNoBoxMilis(long entrouNoBoxMilis) {
        this.parouNoBoxMilis = entrouNoBoxMilis;
    }

    public long getSaiuDoBoxMilis() {
        return saiuDoBoxMilis;
    }

    public void setSaiuDoBoxMilis(long saiuDoBoxMilis) {
        this.saiuDoBoxMilis = saiuDoBoxMilis;
    }

    public Volta getUltimaVolta() {
        return ultimaVolta;
    }

    public boolean isDesqualificado() {
        return desqualificado;
    }

    public void setDesqualificado(boolean desqualificado) {
        this.desqualificado = desqualificado;
    }

    public String getSegundosParaLider() {
        return ((segundosParaLider == null) ? "0" : segundosParaLider);
    }

    public void setSegundosParaLider(String segundosParaLider) {
        if (segundosParaLider == null) {
            segundosParaLider = "";
        }

        this.segundosParaLider = segundosParaLider;
    }

    public Volta getVoltaAtual() {
        return voltaAtual;
    }

    public void setVoltaAtual(Volta voltaAtual) {
        if (voltaAtual != null)
            voltaAtual.setPiloto(this.getId());
        this.voltaAtual = voltaAtual;
    }

    public List<Volta> getVoltas() {
        return voltas;
    }

    public List<Volta> getVoltasCopy() {
        List<Volta> copy = new ArrayList<Volta>();
        while (copy.isEmpty()) {
            try {
                if (voltas == null || voltas.isEmpty()) {
                    return copy;
                }
                copy.addAll(voltas);
            } catch (Exception e) {
                copy.clear();
                Logger.logarExept(e);
            }
        }
        return copy;
    }

    public int getQtdeParadasBox() {
        return qtdeParadasBox;
    }

    public boolean isRecebeuBanderada() {
        return recebeuBanderada;
    }

    public void setRecebeuBanderada(boolean recebeuBanderada) {
        this.recebeuBanderada = recebeuBanderada;
    }

    public void setQtdeParadasBox(int qtdeParadasBox) {
        this.qtdeParadasBox = qtdeParadasBox;
    }

    @JsonIgnore
    public String getSetUpIncial() {
        return setUpIncial;
    }

    public void setSetUpIncial(String setUpIncial) {
        this.setUpIncial = setUpIncial;
    }

    @JsonIgnore
    public int getCiclosDesconcentrado() {
        return ciclosDesconcentrado;
    }

    public int getPtosBox() {
        return ptosBox;
    }

    public void setPtosBox(int ptosBox) {
        this.ptosBox = ptosBox;
    }

    public boolean isBox() {
        return box;
    }

    public void setBox(boolean box) {
        this.box = box;
    }

    public void setCiclosDesconcentrado(int ciclosDelay) {
        if (ciclosDelay > 0 && verificaDesconcentrado()) {
            return;
        }
        this.ciclosDesconcentrado = ciclosDelay;
    }

    public boolean isAgressivo() {
        return agressivo;
    }

    public void setAgressivo(boolean regMaximo, InterfaceJogo interfaceJogo) {
        if (regMaximo && verificaDesconcentrado()) {
            return;
        }
        this.agressivo = regMaximo;
    }

    public boolean isJogadorHumano() {
        return jogadorHumano;
    }

    public void setJogadorHumano(boolean jogadorHumano) {
        this.jogadorHumano = jogadorHumano;
    }

    public int getNumeroVolta() {
        return numeroVolta;
    }

    public void setNumeroVolta(int numeroVolta) {
        this.numeroVolta = numeroVolta;
    }

    public Carro getCarro() {
        return carro;
    }

    public long getPtosPista() {
        return ptosPista;
    }

    public void setPtosPista(long ptosPista) {
        if (isDesqualificado()) {
            return;
        }
        this.ptosPista = ptosPista;
    }

    public No getNoAtual() {
        return noAtual;
    }

    public void setNoAtual(No no) {
        setNoAnterior(getNoAtual());
        this.noAtual = no;
    }

    public boolean emMovimento() {
        if (getNoAnterior() == null) {
            return false;
        }
        Point atual = null;
        if (getNoAtual() != null) {
            atual = getNoAtual().getPoint();
        }
        if (getNoAtualSuave() != null) {
            atual = getNoAtualSuave().getPoint();
        }
        if (atual == null) {
            return false;
        }
        return !atual.equals(getNoAnterior().getPoint());
    }

    public No getNoAnterior() {
        return noAnterior;
    }

    public void setNoAnterior(No noAnterior) {
        this.noAnterior = noAnterior;
    }

    public void setCarro(Carro carro) {
        this.carro = carro;
    }

    public String getNomeCarro() {
        return nomeCarro;
    }

    public void setNomeCarro(String carro) {
        this.nomeCarro = carro;
    }

    public int getHabilidade() {
        return habilidade;
    }

    public void setHabilidade(int habilidade) {
        this.habilidade = habilidade;
    }

    public String getNomeAbreviado() {
        if (!Util.isNullOrEmpty(nomeAbreviado)) {
            return nomeAbreviado;
        }
        if (nome != null && nome.contains(".")) {
            try {
                String nmPiloto = nome.split("\\.")[1];
                nmPiloto = nmPiloto.substring(0, 3);
                return nmPiloto;
            } catch (Exception e) {
            }
        }
        if (nome.length() > 3) {
            return nome.substring(0, 3);
        } else {
            return nome;
        }
    }

    public void setNomeAbreviado(String nomeAbreviado) {
        this.nomeAbreviado = nomeAbreviado;
    }

    public String getNome() {
        return nome;
    }

    public void setUltimaVolta(Volta ultimaVolta) {
        this.ultimaVolta = ultimaVolta;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String toString() {
        return nome + " - " + getCarro().getNome();
    }

    public void setVelocidade(int velocidade) {
        if (this.velocidade != 1)
            this.velocidadeAnterior = this.velocidade;
        this.velocidade = velocidade;
    }

    public void setVelocidadeAnterior(int velocidadeAnterior) {
        this.velocidadeAnterior = velocidadeAnterior;
    }

    public int getVelocidadeAnterior() {
        return velocidadeAnterior;
    }

    public void processarCiclo(InterfaceJogo controleJogo) {
    }

    private void processaAjustesPosQualificacao(int i) {
        if (getCarro().getPotenciaAntesQualify() > getCarro().getPotencia()) {
            getCarro().setPotencia(getCarro().getPotencia() + i);
        }
        if (getCarro().getPotenciaAntesQualify() < getCarro().getPotencia()) {
            getCarro().setPotencia(getCarro().getPotencia() - i);
        }

        if (getHabilidadeAntesQualify() > getHabilidade()) {
            setHabilidade(getHabilidade() + i);
        }
        if (getHabilidadeAntesQualify() < getHabilidade()) {
            setHabilidade(getHabilidade() - i);
        }
    }

    private void processaUltimosDesgastesPneuECombustivel() {
        int pCombust = getCarro().getPorcentagemCombustivel();
        if (ultimoConsumoCombust == null) {
            ultimoConsumoCombust = new Integer(pCombust);
        } else {
            if (ultimoConsumoCombust.intValue() > pCombust) {
                ultsConsumosCombustivel.add(ultimoConsumoCombust.intValue() - pCombust);
                ultimoConsumoCombust = new Integer(pCombust);

            }
        }
        int pPneu = getCarro().getPorcentagemDesgastePneus();
        if (ultimoConsumoPneu == null) {
            ultimoConsumoPneu = new Integer(pPneu);
        } else {
            if (ultimoConsumoPneu.intValue() > pPneu) {
                ultsConsumosPneu.add(ultimoConsumoCombust.intValue() - pPneu);
                ultimoConsumoPneu = new Integer(pPneu);
            }
        }
    }

    private void verificaIaIrBox(InterfaceJogo controleJogo) {
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (isJogadorHumano() || isRecebeuBanderada() || getPtosPista() < 0) {
            return;
        }
        int pneus = getCarro().getPorcentagemDesgastePneus();
        int combust = getCarro().getPorcentagemCombustivel();
        int corrida = controleJogo.porcentagemCorridaConcluida();
        if (controleJogo.isSemReabastecimento()) {
            combust = 100;
        }

        if ((combust < 5) && !controleJogo.isCorridaTerminada()) {
            box = true;
        }

        if (controleJogo.isSafetyCarNaPista() && !controleJogo.isSafetyCarVaiBox()) {
            if (combust < 20 || pneus < 50) {
                box = true;
            }

        }
        boolean boxPneus = false;
        if (Carro.TIPO_PNEU_MOLE.equals(getCarro().getTipoPneu()) && pneus < 20) {
            boxPneus = true;
        } else if (pneus < 15) {
            boxPneus = true;
        }

        if (boxPneus) {
            if (voltas.size() > 3) {
                Volta voltaUltima = voltas.get(voltas.size() - 1);
                Volta voltaPenultima = voltas.get(voltas.size() - 2);
                Volta voltaAntiPenultima = voltas.get(voltas.size() - 3);
                if (voltaUltima.getTempoNumero() > voltaPenultima.getTempoNumero()
                        && voltaUltima.getTempoNumero() > voltaAntiPenultima.getTempoNumero()) {
                    box = true;
                }
            }
            if (colisao == null && noAtual.verificaCurvaBaixa() && (ganho < (.9 * maxGanhoBaixa))) {
                box = true;
            }
            if (colisao == null && noAtual.verificaCurvaAlta() && (ganho < (.9 * maxGanhoAlta))) {
                box = true;
            }
        }

        int limiteUltimasVoltas = 80;
        if (controleJogo.isBoxRapido()) {
            limiteUltimasVoltas = 85;
        }

        if (box && corrida > limiteUltimasVoltas && getQtdeParadasBox() > 0) {
            box = false;
        }

        if (carro.verificaPneusIncompativeisClima(controleJogo)) {
            box = true;
        }

        if (controleJogo.isSemReabastecimento() && !carro.verificaPneusIncompativeisClima(controleJogo)
                && controleJogo.isSemTrocaPneu() && !carro.verificaDano()) {
            box = false;
        }

        if (carro.verificaDano()) {
            box = true;
        }

        if (controleJogo.verificaUltimaVolta()) {
            box = false;
        }

        if (controleJogo.getNumVoltaAtual() < 0) {
            box = false;
        }

        if (controleJogo.isCorridaTerminada()) {
            box = false;
        }
    }

    public int getPosicao() {
        return posicao;
    }

    public void setPosicao(int posicao) {
        this.posicao = posicao;
    }

    public int getNovoModificador() {
        return novoModificador;
    }

    public int getNovoModificadorCarro() {
        return novoModificadorCarro;
    }

    private int processaNovoIndex(InterfaceJogo controleJogo) {
        int index = getNoAtual().getIndex();
        /**
         * Devagarinho qdo a corrida termina
         */
        if (controleJogo.isCorridaTerminada() && isRecebeuBanderada() && !getNoAtual().verificaRetaOuLargada()) {
            setDevagarAposBanderada(true);
        }
        if (controleJogo.isCorridaTerminada() && isRecebeuBanderada()
                && (!getNoAtual().verificaRetaOuLargada() || isDevagarAposBanderada())) {
            double novoModificador = (controleJogo.getCircuito().getMultiplciador()) * 2;
            index += novoModificador;
            setPtosPista(Util.inteiro(novoModificador + getPtosPista()));
            setVelocidade(Util.intervalo(50, 65));
            if (carroPilotoDaFrenteRetardatario != null
                    && getTracado() == carroPilotoDaFrenteRetardatario.getPiloto().getTracado()) {
                mudarTracado(Util.intervalo(0, 2), controleJogo, true);
            }
            return index;
        }

        if (desqualificado) {
            return getNoAtual().getIndex();
        }
        novoModificador = calcularModificador(controleJogo);
        novoModificador = getCarro().calcularModificadorCarro(novoModificador, agressivo, getNoAtual(), controleJogo);
        novoModificadorCarro = novoModificador;
        processaLimitadorModificador();
        calculaCarrosAdjacentes(controleJogo);
        processaStress(controleJogo);
        processaUsoERS(controleJogo);
        processaUsoDRS(controleJogo);
        processaMudancaRegime(controleJogo);
        processaGanho(controleJogo);
        processaPontoEscape(controleJogo);
        processaEscapadaDaPista(controleJogo);
        processaIAnovoIndex(controleJogo);
        processaTurbulencia(controleJogo);
        processaFaiscas(controleJogo);
        processaGanhoDanificado();
        processaPneusIncomaptiveis(controleJogo);
        processaFreioNaReta(controleJogo);
        processaEvitaBaterCarroFrente(controleJogo);
        processaMudarTracado(controleJogo);
        processaColisao(controleJogo);
        processaPenalidadeColisao(controleJogo);
        ganho = processaGanhoMedio(controleJogo, ganho);
        if (noAtual.verificaRetaOuLargada()) {
            ganhoBrutoReta += ganho;
        }
        if (noAtual.verificaCurvaAlta()) {
            ganhoBrutoAlta += ganho;
        }
        if (noAtual.verificaCurvaBaixa()) {
            ganhoBrutoBaixa += ganho;
        }
        processaLimitadorGanho(controleJogo);
        if (noAtual.verificaRetaOuLargada()) {
            ganhoTotalReta += ganho;
        }
        if (noAtual.verificaCurvaAlta()) {
            ganhoTotalAlta += ganho;
        }
        if (noAtual.verificaCurvaBaixa()) {
            ganhoTotalBaixa += ganho;
        }
        processaGanhoSafetyCar(controleJogo);
        processaUltimas5Voltas();
        decrementaPilotoDesconcentrado(controleJogo);
        long roundGanho = Math.round(ganho);
        setPtosPista(Util.inteiro(getPtosPista() + roundGanho));
        index += roundGanho;
        setVelocidade(calculoVelocidade(ganho));
        return index;
    }

    private void processaPneusIncomaptiveis(InterfaceJogo interfaceJogo) {
        if (!carro.verificaPneusIncompativeisClima(interfaceJogo)) {
            return;
        }
        if (isRecebeuBanderada()) {
            return;
        }
        if (getNoAtual().verificaCurvaBaixa()) {
            if (ganho > 15) {
                ganho = 15;
            }
            incStress(testeHabilidadePiloto() ? 0 : 4);
        }
        if (getNoAtual().verificaCurvaAlta()) {
            if (ganho > 20) {
                ganho = 20;
            }
            incStress(testeHabilidadePiloto() ? 0 : 2);
        }
    }

    public void processaUltimas5Voltas() {
        if (voltas == null || voltas.isEmpty()) {
            return;
        }
        ultimas5Voltas.clear();
        int numeroVolta = getNumeroVolta() - 1;
        for (int i = voltas.size() - 1; i >= 0; i--) {
            Volta volta = (Volta) voltas.get(i);
            if (volta.getTempoNumero() == null && volta.getCiclosFim() == 0 || volta.getTempoNumero() == 0) {
                continue;
            }
            ultimas5Voltas.add(numeroVolta + " - " + volta.getTempoVoltaFormatado());
            numeroVolta--;
            if (ultimas5Voltas.size() >= 5) {
                break;
            }
        }

    }

    public void processaAlertaAerefolio(InterfaceJogo controleJogo) {
        setAlertaAerefolio(false);
        if (controleJogo.isModoQualify()) {
            return;
        }
        int durabilidade = InterfaceJogo.DURABILIDADE_AREOFOLIO / 2;
        if (getCarro().getDurabilidadeAereofolio() <= durabilidade) {
            setAlertaAerefolio(true);
        }

    }

    public void processaAlertaMotor(InterfaceJogo controleJogo) {
        setAlertaMotor(false);
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (getCarro().verificaMotorSuperAquecido()) {
            setAlertaMotor(true);
        }

    }

    public void processaFaiscas(InterfaceJogo controleJogo) {
        setFaiscas(false);
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (isRecebeuBanderada()) {
            return;
        }
        if (getPtosBox() != 0) {
            return;
        }
        double mod = .995;

        if (isFreiandoReta() && getCarro().getPorcentagemCombustivel() > Util.intervalo(40, 50)) {
            mod -= .50;
            if (getTracado() != 0) {
                mod -= .50;
            }
        }
        if (getCarro().getGiro() == Carro.GIRO_MAX_VAL && getNoAtual() != null && getNoAtual().verificaRetaOuLargada()
                && !Clima.CHUVA.equals(controleJogo.getClima()) && getVelocidade() != 0 && Math.random() > mod) {
            setFaiscas(true);
        }

    }

    public void calculaCarrosAdjacentes(InterfaceJogo controleJogo) {
    }

    private void processaGanhoSafetyCar(InterfaceJogo controleJogo) {

    }

    private int calculoVelocidade(double ganho) {
        return 0;
    }

    public void processaColisao(InterfaceJogo controleJogo) {

    }

    public void processaPenalidadeColisao(InterfaceJogo controleJogo) {

    }

    public void processaEscapadaDaPista(InterfaceJogo controleJogo) {

    }

    private void processaGanho(InterfaceJogo controleJogo) {

    }

    public double getValorLimiteStressePararErrarCurva(InterfaceJogo controleJogo) {
        if (isJogadorHumano()) {
            return 100 * (1.3 - controleJogo.getNiveljogo());
        } else {
            return 100 * (controleJogo.getNiveljogo() + 0.2);
        }
    }

    private void processaLimitadorModificador() {
       ultModificador = novoModificador;
    }

    private void processaGanhoDanificado() {

    }

    /**
     * Controla Efeito turbulencia e ultrapassagens usando tracado
     */
    private void processaTurbulencia(InterfaceJogo controleJogo) {

    }

    private boolean verificaForaPista(Piloto piloto) {
        boolean voltando = getIndiceTracado() > 0 && (piloto.getTracadoAntigo() == 4 || piloto.getTracadoAntigo() == 5);
        return piloto.getTracado() == 4 || piloto.getTracado() == 5 || voltando;
    }

    private void processaFreioNaReta(InterfaceJogo controleJogo) {

    }

    private void processaLimitadorGanho(InterfaceJogo controleJogo) {

    }

    public boolean isLimiteGanho() {
        return limiteGanho;
    }

    private void processaUsoERS(InterfaceJogo controleJogo) {

    }

    private void processaUsoDRS(InterfaceJogo controleJogo) {

    }

    private boolean verificaPodeUsarDRS(InterfaceJogo controleJogo) {

        return false;
    }

    private void processaStress(InterfaceJogo controleJogo) {

    }

    private void processaIAnovoIndex(InterfaceJogo controleJogo) {

    }

    private void processaMudarTracado(InterfaceJogo controleJogo) {

    }

    public void desviaPilotoNaFrente(Piloto piloto, Piloto pilotoNaFrente, InterfaceJogo controleJogo) {
       }

    public void mensagemRetardatario(Piloto piloto, Piloto pilotoNaFrente, InterfaceJogo controleJogo) {

    }

    public boolean verificaPassarRetardatario(Piloto piloto, Piloto pilotoNaFrente, InterfaceJogo controleJogo) {
        return !controleJogo.isCorridaTerminada() && !piloto.isRecebeuBanderada()
                && pilotoNaFrente.getNumeroVolta() < getNumeroVolta()
                && pilotoNaFrente.getPtosPista() < piloto.getPtosPista() && !pilotoNaFrente.isDesqualificado()
                && (pilotoNaFrente.getPtosBox() == 0);
    }

    private boolean tentarEscaparPilotoAtras(InterfaceJogo controleJogo, boolean tentaPassarFrete) {
        return false;
    }

    private void iaTentaUsarDRS(InterfaceJogo controleJogo) {
    }

    private void iaTentaUsarErs(InterfaceJogo controleJogo) {
    }

    public boolean verificaNaoPrecisaDesviar(Piloto pilotoFrente) {
        return pilotoFrente.verificaNaoPrecisaDesenhar();
    }

    public boolean verificaNaoPrecisaDesenhar() {
        String danificado = getCarro().getDanificado();
        return getCarro().isPaneSeca() || Carro.ABANDONOU.equals(danificado) || getCarro().isRecolhido()
                || Carro.PANE_SECA.equals(danificado) || Carro.EXPLODIU_MOTOR.equals(danificado);
    }

    private void processaEvitaBaterCarroFrente(InterfaceJogo controleJogo) {
    }

    public boolean escapaTracado(InterfaceJogo controleJogo) {
        return false;
    }

    @JsonIgnore
    public Point getPontoDerrapada() {
        return pontoEscape;
    }

    public void processaPontoEscape(InterfaceJogo controleJogo) {

    }

    public Rectangle2D centralizaCarroColisao(InterfaceJogo controleJogo) {
        return null;
    }

    private Point linhaColisaoTracado4(Point p, Point p1, Point p2, Point p4, int indTracado) {
        List drawBresenhamLine = null;
        if (getTracadoAntigo() == 0) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y, p4.x, p4.y);
        }
        if (getTracadoAntigo() == 1) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y, p4.x, p4.y);
        }
        if (getTracadoAntigo() == 2) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y, p4.x, p4.y);
        }
        if (drawBresenhamLine == null) {
            Logger.logar("Piloto.centralizaCarroColisao drawBresenhamLine=null 5");
            return null;
        }
        int indice = linhaIndiceColisaoTracado(indTracado, drawBresenhamLine);

        Point pReta = (Point) drawBresenhamLine.get(indice);
        return pReta;
    }

    private Point linhaColisaoTracado2(Point p, Point p1, Point p2, Point p4, Point p5, int indTracado) {
        List drawBresenhamLine = null;
        if (getTracadoAntigo() == 0) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y, p2.x, p2.y);
        }
        if (getTracadoAntigo() == 1) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y, p2.x, p2.y);
        }
        if (getTracadoAntigo() == 4) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y, p2.x, p2.y);
        }
        if (getTracadoAntigo() == 5) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y, p2.x, p2.y);
        }
        if (drawBresenhamLine == null) {
            Logger.logar("Piloto.centralizaCarroColisao drawBresenhamLine=null 4");
            return null;
        }
        int indice = linhaIndiceColisaoTracado(indTracado, drawBresenhamLine);

        Point pReta = (Point) drawBresenhamLine.get(indice);
        return pReta;
    }

    private Point linhaColisaoTracado5(Point p, Point p1, Point p2, Point p5, int indTracado) {
        List drawBresenhamLine = null;
        if (getTracadoAntigo() == 0) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y, p5.x, p5.y);
        }
        if (getTracadoAntigo() == 1) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y, p5.x, p5.y);
        }
        if (getTracadoAntigo() == 2) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y, p5.x, p5.y);
        }
        if (drawBresenhamLine == null) {
            Logger.logar("Piloto.centralizaCarroColisao drawBresenhamLine=null 3");
            return null;
        }
        int indice = linhaIndiceColisaoTracado(indTracado, drawBresenhamLine);

        Point pReta = (Point) drawBresenhamLine.get(indice);
        return pReta;
    }

    private int linhaIndiceColisaoTracado(int indTracado, List drawBresenhamLine) {
        int indice = drawBresenhamLine.size() - indTracado;
        if (indice <= 0) {
            indice = 0;
        }
        if (indice >= drawBresenhamLine.size()) {
            indice = drawBresenhamLine.size() - 1;
        }
        return indice;
    }

    private Point linhaColisaoTracado1(Point p, Point p1, Point p2, Point p4, Point p5, int indTracado) {
        List drawBresenhamLine = null;
        if (getTracadoAntigo() == 0) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y, p1.x, p1.y);
        }
        if (getTracadoAntigo() == 2) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y, p1.x, p1.y);
        }
        if (getTracadoAntigo() == 4) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y, p1.x, p1.y);
        }
        if (getTracadoAntigo() == 5) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y, p1.x, p1.y);
        }
        if (drawBresenhamLine == null) {
            Logger.logar("Piloto.centralizaCarroColisao drawBresenhamLine=null 2");
            return null;
        }
        int indice = linhaIndiceColisaoTracado(indTracado, drawBresenhamLine);

        Point pReta = (Point) drawBresenhamLine.get(indice);
        return pReta;
    }

    private Point linhaColisaoTracado0(Point p, Point p1, Point p2, Point p4, Point p5, int indTracado) {
        List drawBresenhamLine = null;
        if (getTracadoAntigo() == 1) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y, p.x, p.y);
        }
        if (getTracadoAntigo() == 2) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y, p.x, p.y);
        }
        if (getTracadoAntigo() == 5) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y, p.x, p.y);
        }
        if (getTracadoAntigo() == 4) {
            drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y, p.x, p.y);
        }
        if (drawBresenhamLine == null) {
            Logger.logar("Piloto.centralizaCarroColisao drawBresenhamLine=null 1");
            return null;
        }
        int indice = linhaIndiceColisaoTracado(indTracado, drawBresenhamLine);

        Point pReta = (Point) drawBresenhamLine.get(indice);
        return pReta;
    }

    public List obterPista(InterfaceJogo controleJogo) {
        return null;
    }

    public Piloto() {
        zerarGanhoEVariaveisUlt();
    }

    public void zerarGanhoEVariaveisUlt() {
        listGanho = new ArrayList<Double>();
        velocidade = 0;
        velocidadeAnterior = 0;
        ultGanhoReta = 0;
        ultimaMudancaPos = 0;
        ultimoConsumoCombust = 0;
        ultimoConsumoPneu = 0;
        ultModificador = 0;
    }

    public double processaGanhoMedio(InterfaceJogo controleJogo, double ganho) {
        if (controleJogo.isModoQualify()) {
            return ganho;
        }
        while (listGanho.size() > tamanhoBufferGanho) {
            listGanho.remove(0);
        }
        listGanho.add(ganho);
        double soma = 0;
        for (Iterator iterator = listGanho.iterator(); iterator.hasNext(); ) {
            Double val = (Double) iterator.next();
            soma += val.doubleValue();
        }
        double ganhoMed = soma / listGanho.size();
        return ganhoMed;
    }

    private boolean tentarPassaPilotoDaFrente(InterfaceJogo controleJogo) {
        if (carroPilotoDaFrente == null) {
            return false;
        }
        if (getStress() > getValorLimiteStressePararErrarCurva(controleJogo)) {
            return false;
        }
        if (calculaDiferencaParaProximo < 500 && testeHabilidadePilotoCarro()) {
            modoIADefesaAtaque(controleJogo);
        } else {
            modoIADefesaAtaque(controleJogo);
        }
        return true;
    }

    private void modoIADefesaAtaque(InterfaceJogo controleJogo) {
        double valorLimiteStressePararErrarCurva = 100;
        boolean derrapa = getNoAtual() != null && indexRefEscape > getNoAtual().getIndex();
        if (derrapa && testeHabilidadePiloto()) {
            valorLimiteStressePararErrarCurva = getValorLimiteStressePararErrarCurva(controleJogo);
        }
        boolean maxPilotagem;
        if (getNoAtual().verificaRetaOuLargada()) {
            maxPilotagem = temCombustivel && temPneu && stress < valorLimiteStressePararErrarCurva
                    && !getCarro().isPneuAquecido();
        } else {
            maxPilotagem = temPneu && stress < valorLimiteStressePararErrarCurva;
        }
        No no = getNoAtual();
        if (no.verificaRetaOuLargada()) {
            setAtivarErs(true);
        }
        if (maxPilotagem && testeHabilidadePiloto()) {
            setModoPilotagem(AGRESSIVO);
        }
    }

    private void processaMudancaRegime(InterfaceJogo controleJogo) {
        if (isRecebeuBanderada()) {
            setModoPilotagem(Piloto.LENTO);
            getCarro().setGiro(Carro.GIRO_MIN_VAL);
            return;
        }
        if (verificaDesconcentrado()) {
            agressivo = false;
            if (Piloto.AGRESSIVO.equals(getModoPilotagem())) {
                setModoPilotagem(Piloto.NORMAL);
            }
            if (Carro.GIRO_MAX_VAL == getCarro().getGiro()) {
                getCarro().setGiro(Carro.GIRO_NOR_VAL);
            }
            return;
        }
        boolean novoModoAgressivo = agressivo;
        if (AGRESSIVO.equals(getModoPilotagem())) {
            novoModoAgressivo = true;
            mensangesModoAgressivo(controleJogo);
        }
        if (NORMAL.equals(getModoPilotagem())) {
            if (noAtual.verificaRetaOuLargada()) {
                novoModoAgressivo = true;
            } else {
                if (testeHabilidadePilotoCarro()) {
                    novoModoAgressivo = false;
                }
            }
        }
        if (LENTO.equals(getModoPilotagem())) {
            novoModoAgressivo = false;
            mensagemPilotoLento(controleJogo);
        }
        agressivo = novoModoAgressivo;
    }

    private void mensagemPilotoLento(InterfaceJogo controleJogo) {
    }

    private void mensangesModoAgressivo(InterfaceJogo controleJogo) {

    }

    private boolean testeHabilidadePilotoHumanoCarro(InterfaceJogo controleJogo) {
        if (danificado()) {
            return false;
        }
        if (Math.random() < controleJogo.getNiveljogo()) {
            return false;
        }
        return carro.testePotencia() && testeHabilidadePiloto();
    }

    public boolean verificaDesconcentrado() {
        return ciclosDesconcentrado > 0;
    }

    public boolean decrementaPilotoDesconcentrado(InterfaceJogo interfaceJogo) {

        return true;
    }

    private int calcularModificador(InterfaceJogo controleJogo) {

        double bonusMotor = 0.5;

        if (Carro.GIRO_MAX_VAL == getCarro().getGiro()) {
            bonusMotor += getCarro().testePotencia() ? 0.2 : 0.1;
        }
        if (Carro.GIRO_NOR_VAL == getCarro().getGiro()) {
            bonusMotor += getCarro().testePotencia() ? 0.1 : 0.0;
        }
        if (Carro.GIRO_MIN_VAL == getCarro().getGiro() && !getNoAtual().verificaRetaOuLargada()) {
            bonusMotor -= getCarro().testePotencia() ? 0.0 : 0.1;
        }

        if (controleJogo.isChovendo() && !getNoAtual().verificaRetaOuLargada()) {
            bonusMotor -= testeHabilidadePilotoAerodinamica(controleJogo) ? 0.2 : 0.3;
        }
        if (getNoAtual().verificaRetaOuLargada() && getCarro().testePotencia()) {
            acelerando = true;
            return (Math.random() < bonusMotor ? 4 : 3);
        } else if (getNoAtual().verificaRetaOuLargada() && getCarro().testePotencia()
                && getCarro().testeAerodinamica()) {
            acelerando = true;
            return (Math.random() < bonusMotor ? 3 : 2);
        } else if (getNoAtual().verificaCurvaAlta() && getCarro().testePotencia() && agressivo) {
            acelerando = true;
            return (Math.random() < bonusMotor ? 3 : 2);
        } else if (getNoAtual().verificaCurvaAlta() && !agressivo && testeHabilidadePilotoAerodinamica(controleJogo)) {
            acelerando = false;
            return (Math.random() < bonusMotor ? 3 : 1);
        } else if (getNoAtual().verificaCurvaBaixa() && agressivo && testeHabilidadePilotoAerodinamica(controleJogo)
                && testeHabilidadePilotoFreios(controleJogo)) {
            acelerando = false;
            return (Math.random() < bonusMotor ? 2 : 1);
        } else if (getNoAtual().verificaCurvaBaixa() && testeHabilidadePilotoFreios(controleJogo)) {
            acelerando = false;
            return 1;
        } else {
            acelerando = false;
            return (Math.random() < bonusMotor) ? 1 : 0;
        }
    }

    public boolean testeHabilidadePilotoCarro() {
        if (danificado()) {
            return false;
        }
        return carro.testePotencia() && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePiloto() {
        if (danificado() || verificaDesconcentrado()) {
            return false;
        }
        boolean teste = new Random().nextDouble() < (habilidade / 1000.0);
        return teste;
    }

    public boolean danificado() {
        return carro.verificaDano();
    }

    @JsonIgnore
    public int getPorcentagemCombustUltimaParadaBox() {
        if (porcentagemCombustUltimaParadaBox < 0) {
            porcentagemCombustUltimaParadaBox = 0;
        }

        return porcentagemCombustUltimaParadaBox;
    }

    public void setParadoBox(int paradoBox) {
        this.paradoBox = paradoBox;
    }

    public int getParadoBox() {
        return paradoBox;
    }

    public void setPorcentagemCombustUltimaParadaBox(int porcentagemCombustUltimaParadaBox) {
        this.porcentagemCombustUltimaParadaBox = porcentagemCombustUltimaParadaBox;
    }

    public boolean decrementaParadoBox() {
        if (paradoBox < 0) {
            paradoBox = 0;
        }

        if (paradoBox > 0) {
            paradoBox--;
        }

        if (paradoBox == 0) {
            if (saiuDoBoxMilis == 0) {
                saiuDoBoxMilis = System.currentTimeMillis();
            }
            return false;
        }

        return true;
    }

    public boolean entrouNoBox() {
        return ptosBox != 0;
    }

    public void efetuarSaidaBox(InterfaceJogo interfaceJogo) {
        qtdeParadasBox++;
        ptosBox = 0;
        box = false;
        setProcessouVoltaBox(false);
        setBoxSaiuNestaVolta(true);
    }

    public String obterTempoVoltaAtual() {
        if (voltaAtual == null) {
            return "";
        }

        return voltaAtual.getTempoVoltaFormatado();
    }

    public Volta obterVoltaMaisRapida() {
        if (melhorVolta != null && (voltas.isEmpty())) {
            return melhorVolta;
        }
        if (voltas == null) {
            return null;
        }
        List<Volta> voltasCp = getVoltasCopy();
        if (voltasCp.isEmpty()) {
            return null;
        }
        List ordenaVoltas = new ArrayList(voltasCp);
        Collections.sort(ordenaVoltas, new Comparator() {
            public int compare(Object arg0, Object arg1) {
                Volta v0 = (Volta) arg0;
                Volta v1 = (Volta) arg1;

                return Double.compare(v0.obterTempoVolta(), v1.obterTempoVolta());
            }
        });

        return (Volta) ordenaVoltas.get(0);
    }

    public Volta getMelhorVolta() {
        return melhorVolta;
    }

    public void setMelhorVolta(Volta melhorVolta) {
        this.melhorVolta = melhorVolta;
    }

    public String getAsaBox() {
        return asaBox;
    }

    public void setAsaBox(String asaBox) {
        this.asaBox = asaBox;
    }

    public String getModoPilotagem() {
        return modoPilotagem;
    }

    public void setModoPilotagem(String modoPilotagem) {
        this.modoPilotagem = modoPilotagem;
    }

    public int getStress() {
        return stress;
    }

    public void decStress(int val) {
        if (stress > 0 && (stress - val) > 0 && (Math.random() > ((700.0 - getPosicao() * 20) / 1000.0))) {
            stress -= val;
        }
    }

    public void incStress(int val) {
        if (isRecebeuBanderada()) {
            return;
        }
        if (val < 1) {
            return;
        }
        if (stress > 90) {
            val = 1;
        }
        if (stress > 80 && val > 2) {
            val = 2;
        }
        if (stress > 70 && val > 3) {
            val = 3;
        }
        if (stress < 100 && (stress + val) < 100) {
            if ((Math.random() < ((900 - getPosicao() * 35) / 1000.0)))
                stress += val;
        }
        if (stress >= 99 && AGRESSIVO.equals(getModoPilotagem())) {
            setModoPilotagem(NORMAL);
        }
    }

    public void setStress(int stress) {
        this.stress = stress;
    }

    public double calculaConsumoMedioCombust() {
        double valmed = 0;
        for (Iterator iterator = ultsConsumosCombustivel.iterator(); iterator.hasNext(); ) {
            Integer longVal = (Integer) iterator.next();
            valmed += longVal.doubleValue();
        }
        if (ultsConsumosCombustivel.isEmpty())
            return 0;
        return valmed / ultsConsumosCombustivel.size();
    }

    public double calculaConsumoMedioPneu() {
        double valmed = 0;
        for (Iterator iterator = ultsConsumosPneu.iterator(); iterator.hasNext(); ) {
            Integer longVal = (Integer) iterator.next();
            valmed += longVal.doubleValue();
        }
        if (ultsConsumosPneu.isEmpty())
            return 0;
        return valmed / ultsConsumosCombustivel.size();
    }

    public void limparConsumoMedioCombust() {
        ultimoConsumoCombust = null;
        ultsConsumosCombustivel.clear();
    }

    public void limparConsumoMedioPneus() {
        ultimoConsumoPneu = null;
        ultsConsumosPneu.clear();
    }

    public boolean mudarTracado(int mudarTracado, InterfaceJogo interfaceJogo) {
        return mudarTracado(mudarTracado, interfaceJogo, false);
    }

    public boolean mudarTracado(int mudarTracado, InterfaceJogo interfaceJogo, boolean forcaMudar) {
        if (interfaceJogo == null) {
            return false;
        }
        if (!forcaMudar && isRecebeuBanderada()) {
            return false;
        }
        if (!forcaMudar && verificaDesconcentrado() && (getTracado() == 4 || getTracado() == 5)
                && (mudarTracado != 4 && mudarTracado != 5)) {
            return false;
        }
        if (getSetaBaixo() <= 0) {
            if (getTracado() == 0 && mudarTracado == 1) {
                setSetaCima(11);
            }
            if (getTracado() == 2 && mudarTracado == 0) {
                setSetaCima(11);
            }
        }
        if (getSetaCima() <= 0) {
            if (getTracado() == 0 && mudarTracado == 2) {
                setSetaBaixo(11);
            }
            if (getTracado() == 1 && mudarTracado == 0) {
                setSetaBaixo(11);
            }
        }
        if (!forcaMudar && indiceTracado != 0) {
            return false;
        }
        if (getTracado() == 4 && (mudarTracado == 0 || mudarTracado == 1)) {
            return false;
        }
        if (getTracado() == 5 && (mudarTracado == 0 || mudarTracado == 2)) {
            return false;
        }
        if (getTracado() == 0 && (mudarTracado == 4 || mudarTracado == 5)) {
            return false;
        }
        if (getTracado() == mudarTracado) {
            return false;
        }
        long agora = System.currentTimeMillis();
        double multi = 10;
        if (testeHabilidadePilotoCarro()) {
            multi -= 4;
        }
        if (getCarro().testeAerodinamica()) {
            multi -= 2;
        }
        if (getCarro().testeFreios(interfaceJogo)) {
            multi -= 2;
        }
        if (!forcaMudar && getTracado() != 4 && getTracado() != 5 && getColisao() != null
                && (agora - ultimaMudancaPos) < (Constantes.CICLO * multi)) {
            return false;
        }
        if (getTracado() == 1 && mudarTracado == 2) {
            return false;
        }
        if (getTracado() == 2 && mudarTracado == 1) {
            return false;
        }
        ultimaMudancaPos = System.currentTimeMillis();
        if (!forcaMudar && verificaColisaoAoMudarDeTracado(interfaceJogo, mudarTracado)) {
            return false;
        } else {
            setTracadoAntigo(getTracado());
            setTracado(mudarTracado);
            calculaIndiceTracado(interfaceJogo);
            return true;
        }
    }

    public void calculaIndiceTracado(InterfaceJogo interfaceJogo) {
        double novoIndice = interfaceJogo.getCircuito().getIndiceTracado();
        if (getTracadoAntigo() == 4 || getTracadoAntigo() == 5) {
            novoIndice = interfaceJogo.getCircuito().getIndiceTracadoForaPista();
        }
        setIndiceTracado((int) novoIndice);
    }

    public int decIndiceTracado(InterfaceJogo interfaceJogo) {
        if (indiceTracado <= 0) {
            return 0;
        }
        if ((getTracadoAntigo() == 4 || getTracadoAntigo() == 5)
                && indiceTracado < (Carro.ALTURA * interfaceJogo.getCircuito().getMultiplicadorLarguraPista())) {
            indiceTracado--;
        } else {
            indiceTracado -= 2;
        }
        if (indiceTracado < 0) {
            indiceTracado = 0;
        }
        return indiceTracado;
    }

    public int getIndiceTracado() {
        return indiceTracado;
    }

    public void setIndiceTracado(int indiceTracado) {
        this.indiceTracado = indiceTracado;
    }

    public void calculaVelocidadeExibir(InterfaceJogo controleJogo) {
        if (controleJogo.isJogoPausado()) {
            setVelocidadeExibir(0);
            return;
        }
        int incAcell = 6;
        if (getVelocidadeExibir() > 100) {
            incAcell = 3;
        }
        if (getVelocidadeExibir() > 200) {
            incAcell = 2;
        }
        int incFreiada = 1;
        if (getNoAtual().verificaCurvaBaixa()) {
            incFreiada = Util.intervalo(5, 10);
        }
        if (getNoAtual().verificaCurvaAlta()) {
            incFreiada = Util.intervalo(0, 5);
        }
        if (isFreiandoReta()) {
            incFreiada += Util.intervalo(5, 10);
        }
        if (getVelocidadeExibir() > 100) {
            incFreiada++;
        }
        if (getVelocidadeExibir() > 200) {
            incFreiada++;
        }

        if (getVelocidade() >= getVelocidadeExibir()) {
            int diff = (getVelocidade() - getVelocidadeExibir());
            int limite = 1;
            if (getVelocidadeExibir() > 100) {
                limite = 5;
            }
            if (getVelocidadeExibir() > 200) {
                limite = 10;
            }
            if (diff > limite) {
                incAcell++;
            }
            setVelocidadeExibir(getVelocidadeExibir() + incAcell);
        }
        if (getVelocidade() < getVelocidadeExibir()) {
            setVelocidadeExibir(getVelocidadeExibir() - incFreiada);
        }
        int velocidade = (controleJogo.isSafetyCarNaPista() ? getVelocidadeExibir() / 2 : getVelocidadeExibir());
        setVelocidadeExibir(velocidade);
    }

    public boolean verificaColisaoAoMudarDeTracado(InterfaceJogo controleJogo, int pos) {
        if (getPtosBox() != 0) {
            return false;
        }
        int indice = getNoAtual().getIndex();
        List pilotos = controleJogo.getPilotosCopia();
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext(); ) {
            Piloto piloto = (Piloto) iterator.next();
            if (this.equals(piloto) || piloto.getTracado() == pos) {
                continue;
            }
            int indiceCarro = piloto.getNoAtual().getIndex();
            int traz = indiceCarro - 50;
            int frente = indiceCarro + 50;
            List lista = piloto.obterPista(controleJogo);
            if (traz < 0) {
                traz = (lista.size() - 1) + traz;
            }
            if (frente > (lista.size() - 1)) {
                frente = (frente - (lista.size() - 1)) - 1;
            }

            if (indice >= traz && indice <= frente) {
                // Logger.logar("verificaColisaoAoMudarDeTracado " + getNome());
                return true;
            }
        }
        return false;

    }

    public int calculaDiffParaAnterior(InterfaceJogo controleJogo) {
        return 0;
    }

    public int obterNovoTracadoPossivel() {
        if (tracado == 0) {
            return Util.intervalo(1, 2);
        }
        return 0;
    }

    public boolean adicionaVotoDriveThru(String nomeJogador) {
        if (driveThrough) {
            return false;
        }
        if (votosDriveThru.contains(nomeJogador)) {
            return false;
        }
        votosDriveThru.add(nomeJogador);
        return true;
    }

    public int getVotosDriveThru() {
        return votosDriveThru.size();
    }

    public void limparDriveThrough() {
        driveThrough = false;
        votosDriveThru.clear();

    }

    public boolean isFreiandoReta() {
        return freiandoReta;
    }

    public void setFreiandoReta(boolean freiandoReta) {
        this.freiandoReta = freiandoReta;
    }

    public int getTracadoDelay() {
        return tracadoDelay;
    }

    public void setTracadoDelay(int tracadoDelay) {
        this.tracadoDelay = tracadoDelay;
    }

    public int getNaoDesenhaEfeitos() {
        return naoDesenhaEfeitos;
    }

    public void setNaoDesenhaEfeitos(int naoDesenhaEfeitos) {
        this.naoDesenhaEfeitos = naoDesenhaEfeitos;
    }

    public boolean isAcelerando() {
        return acelerando;
    }

    public int getVelocidadeExibir() {
        return velocidadeExibir;
    }

    public void setVelocidadeExibir(int velocidadeExibir) {
        this.velocidadeExibir = velocidadeExibir;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public boolean testeHabilidadePilotoAerodinamica(InterfaceJogo controleJogo) {
        if (danificado()) {
            return false;
        }
        return carro.testeAerodinamica() && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePilotoFreios(InterfaceJogo controleJogo) {
        if (danificado()) {
            return false;
        }
        return carro.testeFreios(controleJogo) && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePilotoAerodinamicaFreios(InterfaceJogo controleJogo) {
        if (danificado()) {
            return false;
        }
        return carro.testeFreios(controleJogo) && carro.testeAerodinamica() && testeHabilidadePiloto();
    }

    public int getPosicaoInicial() {
        return posicaoInicial;
    }

    public void setPosicaoInicial(int posicaoInicial) {
        this.posicaoInicial = posicaoInicial;
    }

    public Piloto getColisao() {
        return colisao;
    }

    public void setColisao(Piloto colisao) {
        this.colisao = colisao;
    }

    @JsonIgnore
    public int getDiferencaParaProximoRetardatario() {
        return calculaDiffParaProximoRetardatarioMesmoTracado;
    }

    public boolean isColisaoDiantera() {
        return colisaoDiantera;
    }

    public boolean isColisaoCentro() {
        return colisaoCentro;
    }

    @JsonIgnore
    public double getMedGanhosBaixa() {
        return mediaLista(ganhosBaixa);
    }

    @JsonIgnore
    public double getMedGanhosAlta() {
        return mediaLista(ganhosAlta);
    }

    @JsonIgnore
    public double getMedGanhosReta() {
        return mediaLista(ganhosReta);
    }

    private double mediaLista(List list) {
        if (list == null) {
            return 0;
        }
        double total = 0;
        try {
            for (Iterator iterator = list.iterator(); iterator.hasNext(); ) {
                total += (Double) iterator.next();
            }
            return total / list.size();
        } catch (Exception e) {
            return 0;
        }
    }

    @JsonIgnore
    public double getDistanciaDerrapada() {
        return distanciaEscape;
    }

    public String getVantagem() {
        return vantagem;
    }

    public void setVantagem(String vantagem) {
        this.vantagem = vantagem;
    }

    @JsonIgnore
    public boolean isProcessaEvitaBaterCarroFrente() {
        return evitaBaterCarroFrente;
    }

    public boolean isPodeUsarDRS() {
        return contadorPodeAcionarDRS > 0;
    }

    public Carro getCarroPilotoDaFrente() {
        return carroPilotoDaFrente;
    }

    public Carro getCarroPilotoAtras() {
        return carroPilotoAtras;
    }

    @JsonIgnore
    public int getDiferencaParaProximo() {
        return calculaDiferencaParaProximo;
    }

    public boolean isProblemaLargada() {
        return problemaLargada;
    }

    public void setProblemaLargada(boolean problemaLargada) {
        this.problemaLargada = problemaLargada;
    }

    public void atualizaInfoDebug(StringBuilder buffer) {
        Field[] declaredFields = Piloto.class.getDeclaredFields();
        buffer.append("---====Piloto====--- <br>");
        List<String> campos = new ArrayList<String>();
        campos.add("GanhosAltaMedia = " + PainelCircuito.df4.format(getMedGanhosAlta()) + "<br>");

        campos.add("GanhosBaixaMedia = " + PainelCircuito.df4.format(getMedGanhosBaixa()) + "<br>");

        campos.add("GanhosRetaMedia = " + PainelCircuito.df4.format(getMedGanhosReta()) + "<br>");

        campos.add("DiffSuaveReal = "
                + (getNoAtual().getIndex() - (getNoAtualSuave() != null ? getNoAtualSuave().getIndex() : 0)) + "<br>");

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
        Collections.sort(campos, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.toLowerCase().compareTo(o2.toLowerCase());
            }
        });
        for (Iterator<String> iterator = campos.iterator(); iterator.hasNext(); ) {
            buffer.append(iterator.next());
        }
    }

    public boolean isTravouRodas() {
        return travouRodas;
    }

    public void setTravouRodas(boolean travouRodas) {
        this.travouRodas = travouRodas;
    }

    public boolean isFaiscas() {
        return faiscas;
    }

    public void setFaiscas(boolean faiscas) {
        this.faiscas = faiscas;
    }

    public boolean isAlertaMotor() {
        return alertaMotor;
    }

    public void setAlertaMotor(boolean alertaMotor) {
        this.alertaMotor = alertaMotor;
    }

    public boolean isAlertaAerefolio() {
        return alertaAerefolio;
    }

    public void setAlertaAerefolio(boolean alertaAerefolio) {
        this.alertaAerefolio = alertaAerefolio;
    }

    public String getCalculaSegundosParaProximo() {
        return calculaSegundosParaProximo;
    }

    public List<String> getUltimas5Voltas() {
        List<String> copy = new ArrayList<String>();
        while (copy.isEmpty()) {
            try {
                if (ultimas5Voltas.isEmpty()) {
                    return copy;
                }
                copy.addAll(ultimas5Voltas);
            } catch (Exception e) {
                copy.clear();
                Logger.logarExept(e);
            }
        }
        return copy;
    }

    public String getCalculaSegundosParaAnterior() {
        return calculaSegundosParaAnterior;
    }

    public int getGanhoTotalReta() {
        return ganhoTotalReta;
    }

    public int getGanhoBrutoReta() {
        return ganhoBrutoReta;
    }

    public int getGanhoTotalAlta() {
        return ganhoTotalAlta;
    }

    public int getGanhoBrutoAlta() {
        return ganhoBrutoAlta;
    }

    public int getGanhoTotalBaixa() {
        return ganhoTotalBaixa;
    }

    public int getGanhoBrutoBaixa() {
        return ganhoBrutoBaixa;
    }

    public Rectangle getDiateiraColisao() {
        return diateiraColisao;
    }

    public Rectangle getCentroColisao() {
        return centroColisao;
    }

    public Rectangle getTrazeiraColisao() {
        return trazeiraColisao;
    }

    public int getIndexRefEscape() {
        return indexRefEscape;
    }

    public int getDiferencaPosiscoesCorrida() {
        return (getPosicaoInicial() - getPosicao());
    }

    public void setDiferencaPosiscoesCorrida(int diferencaPosiscoesCorrida) {
    }

    public String getTempoVoltaQualificacao() {
        return tempoVoltaQualificacao;
    }

    public void setTempoVoltaQualificacao(String tempoVoltaQualificacao) {
        this.tempoVoltaQualificacao = tempoVoltaQualificacao;
    }

    public boolean isBoxSaiuNestaVolta() {
        return boxSaiuNestaVolta;
    }

    public void setBoxSaiuNestaVolta(boolean boxSaiuNestaVolta) {
        this.boxSaiuNestaVolta = boxSaiuNestaVolta;
    }

    public int getPosicaoBandeirada() {
        return posicaoBandeirada;
    }

    public void setPosicaoBandeirada(int posicaoBandeirada) {
        this.posicaoBandeirada = posicaoBandeirada;
    }

    public boolean isMarcaPneu() {
        return marcaPneu;
    }

    public void setMarcaPneu(boolean marcaPneu) {
        this.marcaPneu = marcaPneu;
    }

    public String getTokenJogador() {
        return tokenJogador;
    }

    public void setTokenJogador(String tokenJogador) {
        this.tokenJogador = tokenJogador;
    }

    public String nomeJogadorFormatado() {
        if (getNomeJogador() == null) {
            return "";
        }
        return "(" + getNomeJogador() + ")";
    }

    public int getPontosCorrida() {
        return pontosCorrida;
    }

    public void setPontosCorrida(int pontosCorrida) {
        this.pontosCorrida = pontosCorrida;
    }

    public long getPorcentagemPontosCorrida() {
        return porcentagemPontosCorrida;
    }

    public void setPorcentagemPontosCorrida(long porcentagemPontosCorrida) {
        this.porcentagemPontosCorrida = porcentagemPontosCorrida;
    }

    public String getTemporadaCapaceteLivery() {
        return temporadaCapaceteLivery;
    }

    public void setTemporadaCapaceteLivery(String temporadaCapaceteLivery) {
        this.temporadaCapaceteLivery = temporadaCapaceteLivery;
    }

    public String getTemporadaCarroLivery() {
        return temporadaCarroLivery;
    }

    public void setTemporadaCarroLivery(String temporadaCarroLivery) {
        this.temporadaCarroLivery = temporadaCarroLivery;
    }

    public String getIdCarroLivery() {
        return idCarroLivery;
    }

    public void setIdCarroLivery(String idCarroLivery) {
        this.idCarroLivery = idCarroLivery;
    }

    public String getIdCapaceteLivery() {
        return idCapaceteLivery;
    }

    public void setIdCapaceteLivery(String idCapaceteLivery) {
        this.idCapaceteLivery = idCapaceteLivery;
    }

    public int getHabilidadeReal() {
        return habilidadeReal;
    }

    public void setHabilidadeReal(int habilidadeReal) {
        this.habilidadeReal = habilidadeReal;
    }

    public boolean isProcessouVoltaBox() {
        return processouVoltaBox;
    }

    public void setProcessouVoltaBox(boolean processouVoltaBox) {
        this.processouVoltaBox = processouVoltaBox;
    }

    public void setPodeUsarDRS(boolean podeUsarDRS) {
        if (podeUsarDRS && contadorPodeAcionarDRS <= 0) {
            contadorPodeAcionarDRS = CONTADOR_LIBERAR_DRS;
        }

    }
}
