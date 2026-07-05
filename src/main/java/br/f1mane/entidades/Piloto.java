package br.f1mane.entidades;

import br.f1mane.controles.InterfaceJogo;
import br.f1mane.recursos.idiomas.Lang;
import br.f1mane.servidor.JogoServidor;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private String idUsuario;
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
    private No noAtual = new No();
    @JsonIgnore
    private int stress;
    /**
     * Magnitude de incremento de estresse por freada mal-sucedida sob
     * pressão, sinalizada por processaFreioNaReta() (que consome e reseta
     * retardaFreiandoReta no mesmo instante) e consumida por processaStress().
     * null quando não há incremento pendente neste tick.
     */
    @JsonIgnore
    private Integer freioNaRetaMalSucedidoNesteTick;
    /**
     * Sinalizado por ControleCorrida.danificaAreofolio() quando o piloto
     * sofre um acidente com perda de aerofólio neste tick; consumido por
     * processaStress(), que aplica o incremento de estresse correspondente.
     */
    @JsonIgnore
    private boolean sofreuDanoAereofolioNesteTick;
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
    private int ganhoSuave;
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
    private boolean driveThrough;
    @JsonIgnore
    private boolean processouVoltaBox;
    @JsonIgnore
    private Double maxGanhoBaixa = Double.valueOf(0);
    @JsonIgnore
    private Double maxGanhoAlta = Double.valueOf(0);
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
    private boolean colisaoDiantera;
    @JsonIgnore
    private boolean colisaoCentro;
    @JsonIgnore
    private int ciclosPresoFila;
    @JsonIgnore
    private double limiteEvitarBatrCarroFrente;
    @JsonIgnore
    private boolean evitaBaterCarroFrente;
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
    private boolean podeUsarDRS;
    @JsonIgnore
    String segundosParaRival;
    protected static final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
    private boolean temMotor;
    private boolean temCombustivel;
    private boolean superAquecido;
    private int porcentagemDesgastePneus;
    private int porcentagemCombustivel;
    private int porcentagemMotor;
    private int porcentagemCorridaRestante;
    private boolean temPneu;
    private int manualTemporario;
    @JsonIgnore
    private ArrayList<Integer> listaNosSuaves = new ArrayList<>();
    @JsonIgnore
    private transient InterfaceJogo controleJogo;

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    @JsonIgnore
    public InterfaceJogo getControleJogo() {
        return controleJogo;
    }

    @JsonIgnore
    public void setControleJogo(InterfaceJogo controleJogo) {
        this.controleJogo = controleJogo;
    }

    public int getGanhoSuave() {
        return ganhoSuave;
    }

    public double getGanho() {
        return ganho;
    }

    public void setGanho(double ganho) {
        this.ganho = ganho;
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
        } else
            return nome.equals(other.nome);
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

    @Override
    public ArrayList<Integer> getListaNosSuaves() {
        return listaNosSuaves;
    }

    public void setNoAtual(No no) {
        setNoAnterior(getNoAtual());
        this.noAtual = no;
    }

    public boolean emMovimento() {
        if (getNoAnterior() == null) {
            return true;
        }
        Point atual = null;
        if (getNoAtual() != null) {
            atual = getNoAtual().getPoint();
        }
        if (atual == null) {
            return true;
        }
        return atual.equals(getNoAnterior().getPoint());
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

    public void processarCiclo() {
        List<No> pista = controleJogo.getNosDaPista();
        int index = processaNovoIndex();
        int diff = index - pista.size();
        /**
         * Completou Volta
         */
        if (diff >= 0) {
            if (controleJogo.isCorridaTerminada()) {
                controleJogo.setRecebeuBanderada(this);
            }
            if (getHabilidadeAntesQualify() > getHabilidade()) {
                setHabilidade(getHabilidade() + 1);
            }
            setNumeroVolta(getNumeroVolta() + 1);
            setBoxSaiuNestaVolta(false);
            processaAjustesPosQualificacao(Global.MAX_VOLTAS
                    / (controleJogo.totalVoltasCorrida() == 0 ? 1 : controleJogo.totalVoltasCorrida()));
            processaUltimosDesgastesPneuECombustivel();
            index = diff;
            if (getNumeroVolta() > 0) {
                getCarro().setCargaErs(Global.CARGA_ERS);
            }
            ativarErs = false;
            controleJogo.processaVoltaRapida(this);
            /**
             * calback de nova volta para corrida Toda
             */
            if (posicao == 1) {
                controleJogo.processaNovaVolta();
            }
            if (numeroVolta > controleJogo.totalVoltasCorrida()) {
                numeroVolta = controleJogo.totalVoltasCorrida();
                return;
            }
        }

        this.setNoAtual((No) pista.get(index));
    }

    private void processaAjustesPosQualificacao(int i) {
        if (!usandoErs() && getCarro().getPotenciaAntesQualify() > getCarro().getPotencia()) {
            getCarro().setPotencia(getCarro().getPotencia() + i);
        }
        if (!usandoErs() && getCarro().getPotenciaAntesQualify() < getCarro().getPotencia()) {
            getCarro().setPotencia(getCarro().getPotencia() - i);
        }
        if (getCarro().getAeroAntesQualify() > getCarro().getAerodinamica()) {
            getCarro().setAerodinamica(getCarro().getAerodinamica() + i);
        }
        if (getCarro().getAeroAntesQualify() < getCarro().getAerodinamica()) {
            getCarro().setAerodinamica(getCarro().getAerodinamica() - i);
        }
        if (getCarro().getFreiosAntesQualify() > getCarro().getFreios()) {
            getCarro().setFreios(getCarro().getFreios() + i);
        }
        if (getCarro().getFreiosAntesQualify() < getCarro().getFreios()) {
            getCarro().setFreios(getCarro().getFreios() - i);
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
            ultimoConsumoCombust = Integer.valueOf(pCombust);
        } else {
            if (ultimoConsumoCombust.intValue() > pCombust) {
                ultsConsumosCombustivel.add(Integer.valueOf(ultimoConsumoCombust.intValue() - pCombust));
                ultimoConsumoCombust = Integer.valueOf(pCombust);

            }
        }
        int pPneu = getCarro().getPorcentagemDesgastePneus();
        if (ultimoConsumoPneu == null) {
            ultimoConsumoPneu = Integer.valueOf(pPneu);
        } else {
            if (ultimoConsumoPneu.intValue() > pPneu) {
                ultsConsumosPneu.add(Integer.valueOf(ultimoConsumoCombust.intValue() - pPneu));
                ultimoConsumoPneu = Integer.valueOf(pPneu);
            }
        }
    }

    private void processaIaIrBox() {
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (isJogadorHumano() || isRecebeuBanderada()) {
            return;
        }
        int pneusPorcentagem = getCarro().getPorcentagemDesgastePneus();
        int combustPorcentagem = getCarro().getPorcentagemCombustivel();
        int corridaPorcentagem = controleJogo.porcentagemCorridaConcluida();
        if (controleJogo.isSemReabastecimento()) {
            combustPorcentagem = 100;
        }

        if (controleJogo.isSafetyCarNaPista()) {
            if (combustPorcentagem < 20 || pneusPorcentagem < 50) {
                box = true;
            }
        }

        boolean boxPneus = false;
        if (pneusPorcentagem < 15) {
            boxPneus = true;
        }

        if (boxPneus) {
            if (voltas.size() > 3) {
                Volta voltaUltima = voltas.get(voltas.size() - 1);
                Volta voltaPenultima = voltas.get(voltas.size() - 2);
                Volta voltaAntiPenultima = voltas.get(voltas.size() - 3);
                if (voltaUltima.getTempoNumero().longValue() > voltaPenultima.getTempoNumero().longValue()
                        && voltaUltima.getTempoNumero().longValue() > voltaAntiPenultima.getTempoNumero().longValue()) {
                    box = true;
                }
            }
            if (colisao == null && noAtual.verificaCurvaBaixa() && (ganho < (.9 * maxGanhoBaixa.doubleValue()))) {
                box = true;
            }
            if (colisao == null && noAtual.verificaCurvaAlta() && (ganho < (.9 * maxGanhoAlta.doubleValue()))) {
                box = true;
            }
        }

        int limiteUltimasVoltas = 80;
        if (controleJogo.isBoxRapido()) {
            limiteUltimasVoltas = 85;
        }

        if (box && corridaPorcentagem > limiteUltimasVoltas && getQtdeParadasBox() > 0) {
            box = false;
        }

        if (carro.verificaPneusIncompativeisClima()) {
            box = true;
        }

        if (controleJogo.isSemReabastecimento() && !carro.verificaPneusIncompativeisClima()
                && controleJogo.isSemTrocaPneu() && !carro.verificaDano()) {
            box = false;
        }

        if (carro.verificaDano()) {
            box = true;
        }
        if ((controleJogo.isSemReabastecimento() && combustPorcentagem < 5)
                || (!controleJogo.isSemTrocaPneu() && pneusPorcentagem < 5)) {
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

    private int processaNovoIndex() {
        int index = getNoAtual().getIndex();
        /**
         * Devagarinho qdo a corrida termina
         */
        if (controleJogo.isCorridaTerminada() && isRecebeuBanderada() && !getNoAtual().verificaRetaOuLargada()) {
            setDevagarAposBanderada(true);
        }
        if (controleJogo.isCorridaTerminada() && isRecebeuBanderada()
                && (!getNoAtual().verificaRetaOuLargada() || isDevagarAposBanderada())) {
            double novoModificador = 20;
            if (noAtual.verificaCurvaAlta()) {
                novoModificador = 15;
            }
            if (noAtual.verificaCurvaBaixa()) {
                novoModificador = 10;
            }
            long avancoBandeirada = limitaAvancoCarroFrente(Math.round(novoModificador));
            index += avancoBandeirada;
            setPtosPista(Util.inteiro(avancoBandeirada + getPtosPista()));
            setVelocidade(controleJogo.getRandom().intervalo(50, 65));
            if (carroPilotoDaFrenteRetardatario != null
                    && getTracado() == carroPilotoDaFrenteRetardatario.getPiloto().getTracado()) {
                mudarTracado(controleJogo.getRandom().intervalo(0, 2));
            }
            return index;
        }
        if (desqualificado) {
            return getNoAtual().getIndex();
        }
        processaGanho();
        calculaCarrosAdjacentes();
        processaIAnovoIndex();
        processaIaIrBox();
        processaUsoERS();
        processaUsoDRS();
        processaPontoEscape();
        processaEscapadaDaPista();
        processaFaiscas();
        processaTurbulencia();
        processaGanhoDanificado();
        processaPneusIncomaptiveis();
        processaFreioNaReta();
        processaEvitaBaterCarroFrente();
        processaMudarTracado();
        processaColisao();
        processaPenalidadeColisao();
        processaTravouRodas();
        processaLimitadorGanho();
        processaGanhoMedio();
        processaEstatisticasGanho();
        processaGanhoSafetyCar();
        processaUltimas5Voltas();
        processaMudancaRegime();
        processaSegundosParaRival();
        controleJogo.verificaAcidente(this);
        processaStress();
        long roundGanho = Math.round(ganho);
        long avancoLimitado = limitaAvancoCarroFrente(roundGanho);
        if (avancoLimitado < roundGanho) {
            ganho = avancoLimitado;
        }
        setPtosPista(Util.inteiro(getPtosPista() + avancoLimitado));
        index += avancoLimitado;
        setVelocidade(calculoVelocidade(ganho));
        return index;
    }

    private void processaSegundosParaRival() {
        setSegundosParaRival(controleJogo.calculaSegundosParaRival(this));
    }

    private void processaEstatisticasGanho() {
        if (noAtual.verificaRetaOuLargada()) {
            ganhosReta.add(Double.valueOf(ganho));
        }
        if (noAtual.verificaCurvaAlta()) {
            ganhosAlta.add(Double.valueOf(ganho));
            if (ganho > maxGanhoAlta.doubleValue()) {
                maxGanhoAlta = Double.valueOf(ganho);
            }
        }
        if (noAtual.verificaCurvaBaixa()) {
            ganhosBaixa.add(Double.valueOf(ganho));
            if (ganho > maxGanhoBaixa.doubleValue()) {
                maxGanhoBaixa = Double.valueOf(ganho);
            }
        }
        if (ganho > ganhoMax) {
            ganhoMax = ganho;
        }
    }

    private void processaPneusIncomaptiveis() {
        if (!carro.verificaPneusIncompativeisClima()) {
            return;
        }
        if (isRecebeuBanderada()) {
            return;
        }
        if (getNoAtual().verificaCurvaBaixa()) {
            if (ganho > 15) {
                ganho = 15;
            }
        }
        if (getNoAtual().verificaCurvaAlta()) {
            if (ganho > 20) {
                ganho = 20;
            }
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
            if (volta.getTempoNumero() == null && volta.getCiclosFim() == 0
                    || volta.getTempoNumero().longValue() == 0) {
                continue;
            }
            ultimas5Voltas.add((numeroVolta + 1) + " - " + volta.getTempoVoltaFormatado());
            numeroVolta--;
            if (ultimas5Voltas.size() >= 5) {
                break;
            }
        }

    }

    public void processaAlertaAerefolio() {
        setAlertaAerefolio(false);
        if (controleJogo.isModoQualify()) {
            return;
        }
        int durabilidade = Global.DURABILIDADE_AREOFOLIO / 2;
        if (getCarro().getDurabilidadeAereofolio() <= durabilidade) {
            setAlertaAerefolio(true);
        }

    }

    public void processaAlertaMotor() {
        setAlertaMotor(false);
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (getCarro().verificaMotorSuperAquecido()) {
            setAlertaMotor(true);
        }

    }

    public void processaFaiscas() {
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

        if (isFreiandoReta() && getCarro().getPorcentagemCombustivel() > controleJogo.getRandom().intervalo(40, 50)) {
            mod -= .50;
            if (getTracado() != 0) {
                mod -= .50;
            }
        }
        if (getCarro().getGiro() == Carro.GIRO_MAX_VAL && getNoAtual() != null && getNoAtual().verificaRetaOuLargada()
                && !Clima.CHUVA.equals(controleJogo.getClima()) && getVelocidade() != 0
                && controleJogo.getRandom().nextDouble() > mod) {
            setFaiscas(true);
        }

    }

    public void calculaCarrosAdjacentes() {
        if (isRecebeuBanderada()) {
            return;
        }
        carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
        carroPilotoAtras = controleJogo.obterCarroAtras(this);
        carroPilotoDaFrenteRetardatario = controleJogo.obterCarroNaFrenteRetardatario(this, false);
        calculaDiffParaProximoRetardatario = controleJogo.calculaDiffParaProximoRetardatario(this, false);
        calculaDiffParaProximoRetardatarioMesmoTracado = controleJogo.calculaDiffParaProximoRetardatario(this, true);
        calculaDiferencaParaAnterior = controleJogo.calculaDiferencaParaAnterior(this);
        calculaDiferencaParaProximo = controleJogo.calculaDiferencaParaProximo(this);
        if (getPosicao() > 1) {
            calculaSegundosParaProximo = controleJogo.calculaSegundosParaProximo(this);
        }
        if (carroPilotoAtras != null && carroPilotoAtras.getPiloto() != null
                && getPosicao() < controleJogo.getPilotos().size()) {
            if (carroPilotoAtras.getPiloto().isDesqualificado()) {
                calculaSegundosParaAnterior = "";
            } else {
                calculaSegundosParaAnterior = controleJogo.calculaSegundosParaProximo(carroPilotoAtras.getPiloto(),
                        carroPilotoAtras.getPiloto().getDiferencaParaProximo());
            }

        }
    }

    private void processaGanhoSafetyCar() {
        if (!controleJogo.isSafetyCarNaPista()) {
            return;
        }
        ganho = controleJogo.ganhoComSafetyCar(ganho, controleJogo, this);
    }

    private int calculoVelocidade(double ganho) {
        int val = 290;
        double porcent = getCarro().getPorcentagemCombustivel() / 100.0;
        val += (21 - (porcent / 5.0));
        boolean naReta = false;
        if (noAtual != null && !freiandoReta && (ativarDRS || ativarErs)) {
            naReta = noAtual.verificaRetaOuLargada();
        }
        return Util.inteiro(((val * ganho * ((naReta) ? 1 : 0.7) / ganhoMax) + ganho * ((naReta) ? 1 : 0.7)));
    }

    public void processaColisao() {
        if (controleJogo.isModoQualify()) {
            setColisao(null);
            return;
        }
        boolean verificaNoPitLane = controleJogo.verificaNoPitLane(this);
        if (verificaNoPitLane) {
            setColisao(null);
            return;
        }
        centralizaCarroColisao();
        if (Global.LOG_COLISAO && diateiraColisao != null && centroColisao != null && trazeiraColisao != null) {
            Logger.logar("[COLISAO] piloto=" + getNome()
                    + " noIndex=" + (noAtual != null ? noAtual.getIndex() : -1)
                    + " tracado=" + tracado
                    + " diant=[" + diateiraColisao.x + "," + diateiraColisao.y + "," + diateiraColisao.width + ","
                    + diateiraColisao.height + "]"
                    + " centro=[" + centroColisao.x + "," + centroColisao.y + "," + centroColisao.width + ","
                    + centroColisao.height + "]"
                    + " traz=[" + trazeiraColisao.x + "," + trazeiraColisao.y + "," + trazeiraColisao.width + ","
                    + trazeiraColisao.height + "]"
                    + " ganho=" + ganho);
        }
        List<Piloto> pilotos = controleJogo.getPilotos();
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
            Piloto pilotoFrente = (Piloto) iterator.next();
            if (pilotoFrente.equals(this)) {
                continue;
            }
            if (verificaNaoPrecisaDesviar(pilotoFrente)) {
                continue;
            }
            if (this.equals(pilotoFrente.getColisao())) {
                continue;
            }
            pilotoFrente.centralizaCarroColisao();
            boolean diantTraz = getDiateiraColisao().intersects(pilotoFrente.getTrazeiraColisao());
            boolean diantCentro = getDiateiraColisao().intersects(pilotoFrente.getCentroColisao());
            colisaoDiantera = diantTraz || diantCentro;
            colisaoCentro = getCentroColisao().intersects(pilotoFrente.getTrazeiraColisao());
            Piloto colisao = (colisaoDiantera || colisaoCentro) ? pilotoFrente : null;
            if (colisao != null) {
                if (Global.LOG_COLISAO) {
                    String tipo = colisaoDiantera
                            ? (diantCentro ? "DIANTEIRA_CENTRO" : "DIANTEIRA_TRAZEIRA")
                            : "CENTRO_TRAZEIRA";
                    Logger.logar("[COLISAO_EVENTO] atras=" + getNome()
                            + "(idx=" + (noAtual != null ? noAtual.getIndex() : -1) + ")"
                            + " frente=" + pilotoFrente.getNome()
                            + "(idx=" + (pilotoFrente.getNoAtual() != null ? pilotoFrente.getNoAtual().getIndex() : -1)
                            + ")"
                            + " tipo=" + tipo);
                }
                setColisao(colisao);
                return;
            }
        }
        setColisao(null);
    }

    public void processaPenalidadeColisao() {
        if (getColisao() == null) {
            ciclosPresoFila = 0;
            return;
        }
        Piloto pilotoFrente = getColisao();
        /**
         * Considera tambem o carro da frente que ainda esta cruzando esta
         * linha (mudou de tracado mas o corpo ainda nao saiu dela); antes a
         * penalidade exigia tracado identico e o carro de tras atravessava.
         */
        boolean mesmaLinha = pilotoFrente.getTracado() == this.tracado
                || (pilotoFrente.getIndiceTracado() > 0 && pilotoFrente.getTracadoAntigo() == this.tracado);
        if (mesmaLinha) {
            double ganhoFrente = pilotoFrente.getGanho();
            if (colisaoDiantera && getCentroColisao().intersects(pilotoFrente.getCentroColisao())) {
                ganho = Math.min(ganho, ganhoFrente);
            } else if (colisaoDiantera) {
                ganho = Math.min(ganho * 0.7, ganhoFrente);
            } else if (colisaoCentro) {
                ganho = Math.min(ganho, ganhoFrente);
            }
        }
        if (mesmaLinha && ganho <= 10) {
            ciclosPresoFila++;
        } else {
            ciclosPresoFila = 0;
        }
    }

    /**
     * Escapa da fila indiana: preso ha varios ciclos atras de um carro lento
     * na mesma linha, muda para um tracado lateral comprovadamente livre. A
     * verificacao padrao de mudanca de tracado bloqueia quando qualquer carro
     * fora do tracado alvo esta a 75 nos - numa fila os proprios vizinhos
     * impedem a saida mesmo com o tracado do lado totalmente vazio.
     */
    boolean tentarEscaparFilaIndiana() {
        if (ciclosPresoFila < 8) {
            return false;
        }
        if (controleJogo.isSafetyCarNaPista() || isRecebeuBanderada()) {
            return false;
        }
        if (getPtosBox() != 0 || isBox()) {
            return false;
        }
        int tracadoAtual = getTracado();
        if (tracadoAtual != 0 && tracadoAtual != 1 && tracadoAtual != 2) {
            return false;
        }
        int[] alvos;
        if (tracadoAtual == 0) {
            int primeiro = controleJogo.getRandom().intervalo(1, 2);
            alvos = new int[] { primeiro, primeiro == 1 ? 2 : 1 };
        } else {
            alvos = new int[] { 0 };
        }
        for (int i = 0; i < alvos.length; i++) {
            if (verificaTracadoLivreParaEscapar(alvos[i]) && mudarTracado(alvos[i], false, true)) {
                ciclosPresoFila = 0;
                if (Global.LOG_COLISAO) {
                    Logger.logar("[ESCAPE_FILA] piloto=" + getNome() + " de=" + tracadoAtual + " para=" + alvos[i]
                            + " idx=" + (getNoAtual() != null ? getNoAtual().getIndex() : -1)
                            + " volta=" + getNumeroVolta());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Livre = nenhum carro ocupando ou cruzando o tracado alvo numa janela de
     * 100 nos atras (nao fechar quem vem rapido) e 60 nos a frente.
     */
    public boolean verificaTracadoLivreParaEscapar(int alvo) {
        No no = getNoAtual();
        if (no == null || no.isBox()) {
            return false;
        }
        List<No> pista = controleJogo.getNosDaPista();
        if (pista == null || pista.isEmpty()) {
            return false;
        }
        int n = pista.size();
        int meuIndex = no.getIndex();
        List<Piloto> pilotos = controleJogo.getPilotos();
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto outro = pilotos.get(i);
            if (outro == null || outro.equals(this) || verificaNaoPrecisaDesviar(outro)) {
                continue;
            }
            No noOutro = outro.getNoAtual();
            if (noOutro == null || noOutro.isBox() || outro.getPtosBox() != 0
                    || controleJogo.verificaNoPitLane(outro)) {
                continue;
            }
            boolean ocupaAlvo = outro.getTracado() == alvo
                    || (outro.getIndiceTracado() > 0 && outro.getTracadoAntigo() == alvo);
            if (!ocupaAlvo) {
                continue;
            }
            int d = noOutro.getIndex() - meuIndex;
            if (d > n / 2) {
                d -= n;
            }
            if (d < -n / 2) {
                d += n;
            }
            if (d >= -100 && d <= 60) {
                return false;
            }
        }
        return true;
    }

    /**
     * Garante que o avanco do ciclo nunca entre nem atravesse a area do carro
     * logo a frente na mesma linha. A penalidade de colisao apenas iguala o
     * ganho ao do carro da frente: quando a aproximacao por ciclo e maior que
     * a janela das areas de colisao (carro parado por acidente, largada,
     * bandeirada) o carro de tras invadia ou passava por cima do da frente.
     */
    public long limitaAvancoCarroFrente(long avanco) {
        if (avanco <= 0) {
            return avanco;
        }
        if (controleJogo.isModoQualify()) {
            return avanco;
        }
        No no = getNoAtual();
        if (no == null || no.isBox() || controleJogo.verificaNoPitLane(this)) {
            return avanco;
        }
        List<No> pista = controleJogo.getNosDaPista();
        if (pista == null || pista.isEmpty()) {
            return avanco;
        }
        int n = pista.size();
        int meuIndex = no.getIndex();
        int distanciaMinima = METADE_CARRO * 2;
        long avancoLimitado = avanco;
        List<Piloto> pilotos = controleJogo.getPilotos();
        for (int i = 0; i < pilotos.size(); i++) {
            Piloto outro = pilotos.get(i);
            if (outro == null || outro.equals(this) || verificaNaoPrecisaDesviar(outro)) {
                continue;
            }
            No noOutro = outro.getNoAtual();
            if (noOutro == null || noOutro.isBox() || outro.getPtosBox() != 0
                    || controleJogo.verificaNoPitLane(outro)) {
                continue;
            }
            boolean mesmaLinha = outro.getTracado() == getTracado()
                    || (outro.getIndiceTracado() > 0 && outro.getTracadoAntigo() == getTracado());
            if (!mesmaLinha) {
                continue;
            }
            int dOutro = noOutro.getIndex() - meuIndex;
            if (dOutro < 0) {
                dOutro += n;
            }
            if (dOutro == 0 && getPosicao() < outro.getPosicao()) {
                continue;
            }
            if (dOutro > avancoLimitado + distanciaMinima) {
                continue;
            }
            long novoAvanco = dOutro - distanciaMinima;
            if (novoAvanco < 0) {
                novoAvanco = 0;
            }
            if (novoAvanco < avancoLimitado) {
                avancoLimitado = novoAvanco;
            }
        }
        return avancoLimitado;
    }

    public void processaEscapadaDaPista() {
        if (controleJogo.isSafetyCarNaPista()) {
            return;
        }
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (getPtosBox() != 0) {
            return;
        }
        if (verificaForaPista(this)) {
            ganho *= 0.50;
        }
        /**
         * Escapa para os tracados 4 ou 5
         */
        if (getStress() > getValorLimiteStressePararErrarCurva() && !controleJogo.isSafetyCarNaPista()
                && AGRESSIVO.equals(modoPilotagem)) {
            if (escapaTracado()) {
                incStress(testeHabilidadePiloto() ? 1 : 3);
                controleJogo.travouRodas(this);
                if (controleJogo.verificaInfoRelevante(this)) {
                    controleJogo.info(Lang.msg("saiDaPista",
                            new String[] { Html.vermelho(nomeJogadorFormatado()), Html.vermelho(getNome()) }));
                }
            } else if (No.CURVA_BAIXA.equals(getNoAtual().getTipo()) && (getTracado() == 0)
                    && (carro.getPorcentagemDesgastePneus() < 30)) {
                /**
                 * Escapa para os tracados 1 ou 2
                 */
                controleJogo.travouRodas(this);
                if (getTracadoAntigo() != 0) {
                    if (getTracadoAntigo() == 1) {
                        mudarTracado(2);
                    } else {
                        mudarTracado(1);
                    }
                } else {
                    mudarTracado(controleJogo.getRandom().intervalo(1, 2));
                }
                return;
            }
        }

    }

    public void processaTravouRodas() {
        No no = getNoAtual();
        if (isRecebeuBanderada() || controleJogo.isSafetyCarNaPista()) {
            return;
        }
        if (controleJogo.isChovendo() || getPtosBox() != 0) {
            return;
        }
        if (no.verificaCurvaBaixa()) {
            if (getStress() > 80) {
                controleJogo.travouRodas(this);
            }
            if (controleJogo.asfaltoAbrasivo() && getStress() > 70) {
                controleJogo.travouRodas(this);
            }
        } else if (no.verificaCurvaAlta()) {
            if (getStress() > 70) {
                controleJogo.travouRodas(this);
            }
            if (controleJogo.asfaltoAbrasivo() && getStress() > 50 && controleJogo.getRandom().nextDouble() > 0.5) {
                controleJogo.travouRodas(this);
            }
        } else if (no.verificaRetaOuLargada()) {
            if (getStress() > 60) {
                controleJogo.travouRodas(this);
                if (controleJogo.asfaltoAbrasivo() && getStress() > 80) {
                    controleJogo.travouRodas(this);
                }
            }
        }
        if (isColisaoDiantera() || isColisaoCentro()) {
            controleJogo.travouRodasPorColisao(this);
        }
    }

    private void processaGanho() {
        ganho = calculaModificadorPrincipal();
        ganho = getCarro().calcularModificadorCarro(ganho, getNoAtual());
    }

    public double getValorLimiteStressePararErrarCurva() {
        return Global.LIMITE_ESTRESSE_PARA_RERRAR_CURVA;
    }

    private void processaGanhoDanificado() {
        if (!danificado()) {
            return;
        }
        if (isRecebeuBanderada()) {
            return;
        }
        boolean danificado = (Carro.PNEU_FURADO.equals(getCarro().getDanificado())
                || (Carro.PERDEU_AEREOFOLIO.equals(getCarro().getDanificado())));
        if (getNoAtual().verificaCurvaBaixa() && danificado && ganho > 10) {
            ganho = 10;
        }
        if (getNoAtual().verificaCurvaAlta() && danificado && ganho > 15) {
            ganho = 15;
        }
        if (Carro.PNEU_FURADO.equals(getCarro().getDanificado()) && getNoAtual().verificaRetaOuLargada()
                && ganho > 20) {
            ganho = 20;
        }
        if (Carro.PERDEU_AEREOFOLIO.equals(getCarro().getDanificado()) && getNoAtual().verificaRetaOuLargada()
                && ganho > 30) {
            ganho = 30;
        }
    }

    /**
     * Controla Efeito turbulencia e ultrapassagens usando tracado
     */
    private void processaTurbulencia() {
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (isRecebeuBanderada()) {
            return;
        }
        if (carroPilotoDaFrenteRetardatario == null) {
            return;
        }
        if (carroPilotoDaFrenteRetardatario.getPiloto().isDesqualificado()) {
            return;
        }
        if ((carroPilotoDaFrenteRetardatario.getPiloto().getTracado() == getTracado())) {
            return;
        }
        if (isPodeUsarDRS() && controleJogo.isDrs()) {
            return;
        }
        if (controleJogo.isSafetyCarNaPista()) {
            return;
        }
        double diff = calculaDiferencaParaProximo;
        double multiplicadoGanhoTurbulencia = (controleJogo.getFatorUtrapassagem());
        double distLimiteTurbulencia = 100.0 / multiplicadoGanhoTurbulencia;
        if (diff < distLimiteTurbulencia && !verificaForaPista(carroPilotoDaFrenteRetardatario.getPiloto())) {
            if (getTracado() != carroPilotoDaFrenteRetardatario.getPiloto().getTracado()) {
                if (getNoAtual().verificaRetaOuLargada()) {
                    multiplicadoGanhoTurbulencia += (getCarro().testePotencia() && getCarro().testeAerodinamica()) ? 0.3
                            : 0.1;
                } else {
                    multiplicadoGanhoTurbulencia += (testeHabilidadePilotoAerodinamicaFreios() ? 0.2 : 0.1);
                }
            }
            if (multiplicadoGanhoTurbulencia > 1) {
                multiplicadoGanhoTurbulencia = 1;
            } else if (multiplicadoGanhoTurbulencia < 0.1) {
                multiplicadoGanhoTurbulencia = 0.1;
            }
            ganho *= (multiplicadoGanhoTurbulencia);
        }
    }

    private boolean verificaForaPista(Piloto piloto) {
        boolean voltando = getIndiceTracado() > 0 && (piloto.getTracadoAntigo() == 4 || piloto.getTracadoAntigo() == 5);
        return piloto.getTracado() == 4 || piloto.getTracado() == 5 || voltando;
    }

    void processaFreioNaReta() {
        if (isRecebeuBanderada()) {
            return;
        }
        boolean testPilotoPneus = getCarro().testeFreios();
        /**
         * efeito freiar na reta
         */
        No obterProxCurva = controleJogo.obterProxCurva(getNoAtual());
        if (obterProxCurva != null && obterProxCurva.verificaCurvaBaixa()
                && controleJogo.isNoZonaFrenagem(getNoAtual()) && getNoAtual().verificaRetaOuLargada()) {
            int indexProxCurva = obterProxCurva.getIndex();
            if (indexProxCurva < getNoAtual().getIndex()) {
                indexProxCurva += controleJogo.getNosDaPista().size();
            }
            double val = indexProxCurva - getNoAtual().getIndex();
            double distAfrente = 300.0;
            freiandoReta = true;
            double multi = (val / distAfrente);

            if (testPilotoPneus) {
                retardaFreiandoReta = true;
            }

            if (!retardaFreiandoReta && Piloto.AGRESSIVO.equals(getModoPilotagem())) {
                retardaFreiandoReta = true;
            }

            double minMulti = 0.7;
            if (controleJogo.isChovendo()) {
                minMulti -= 0.3;
                retardaFreiandoReta = false;
            }
            if (calculaDiffParaProximoRetardatario < 50) {
                minMulti -= controleJogo.getRandom().intervalo(0.05, 0.15);
                retardaFreiandoReta = false;
            } else if (calculaDiffParaProximoRetardatarioMesmoTracado < 100) {
                minMulti -= 0.1;
                retardaFreiandoReta = false;
            } else if (calculaDiffParaProximoRetardatarioMesmoTracado < 150) {
                minMulti -= controleJogo.getRandom().intervalo(0.05, 0.1);
                retardaFreiandoReta = false;
            }
            if (retardaFreiandoReta) {
                if (getStress() > 50 && Piloto.AGRESSIVO.equals(getModoPilotagem())) {
                    controleJogo.travouRodas(this);
                }
                minMulti += (testPilotoPneus) ? 0.2 : 0.1;
            }
            if (multi < minMulti)
                multi = minMulti;
            ganho *= multi;
        } else {
            freiandoReta = false;
        }

        if (getNoAtual().verificaCurvaBaixa() && retardaFreiandoReta) {
            if (getPosicao() <= 3 && controleJogo.getRandom().nextDouble() > 0.9 && !testeHabilidadePilotoFreios()) {
                freioNaRetaMalSucedidoNesteTick = 30 - (getCarro().getPorcentagemDesgastePneus() / 100);
                if (controleJogo.verificaInfoRelevante(this) && controleJogo.getRandom().nextDouble() > 0.7) {
                    controleJogo
                            .info(Lang.msg("014", new String[] { nomeJogadorFormatado(), Html.negrito(getNome()) }));
                }
            }
            retardaFreiandoReta = false;
        }
    }

    private void processaLimitadorGanho() {
        if (ganho < 0) {
            ganho = 0;
        }
        if (verificaForaPista(this)) {
            return;
        }
        if (isColisaoCentro()) {
            ganho *= 0.5;
            return;
        }
        if (isColisaoDiantera()) {
            ganho *= 0.7;
            return;
        }
        if (evitaBaterCarroFrente) {
            ganho *= (calculaDiffParaProximoRetardatarioMesmoTracado / limiteEvitarBatrCarroFrente);
            return;
        }
    }

    private void processaUsoERS() {
        getCarro().setPontenciaErs(false);
        if (controleJogo.isErs() && ativarErs && getPtosBox() == 0) {
            if (getCarro().getCargaErs() <= 0) {
                ativarErs = false;
            } else {
                getCarro().usaErs();
            }
        }
    }

    private void processaUsoDRS() {
        if (!controleJogo.isDrs()) {
            return;
        }
        if (verificaPodeUsarDRS() && ativarDRS) {
            getCarro().setAsa(Carro.MENOS_ASA);
        }
        if (!getNoAtual().verificaRetaOuLargada()) {
            getCarro().setAsa(Carro.MAIS_ASA);
            ativarDRS = false;
            podeUsarDRS = false;
        }
    }

    private boolean verificaPodeUsarDRS() {
        if (getPtosBox() != 0) {
            podeUsarDRS = false;
            return false;
        }
        if (controleJogo.isDrs() && getNumeroVolta() > 1 && !controleJogo.isSafetyCarNaPista()
                && !controleJogo.isChovendo() && !controleJogo.isCorridaTerminada()
                && carroPilotoDaFrenteRetardatario != null && getNoAtual().verificaRetaOuLargada()
                && calculaDiffParaProximoRetardatario < Global.LIMITE_DRS) {
            No obterCurvaAnterior = controleJogo.obterCurvaAnterior(getNoAtual());
            No obterProxCurva = controleJogo.obterProxCurva(getNoAtual());
            if (obterCurvaAnterior == null || obterProxCurva == null) {
                return false;
            }
            int indexProxCurva = obterProxCurva.getIndex();
            int indexCurvaAnterior = obterCurvaAnterior.getIndex();
            if (indexProxCurva < indexCurvaAnterior) {
                indexProxCurva += controleJogo.getNosDaPista().size();
            }
            if ((indexProxCurva - indexCurvaAnterior) >= Global.TAMANHO_RETA_DRS) {
                if (getNoAtual().getIndex() > indexCurvaAnterior && getNoAtual().getIndex() < indexCurvaAnterior + 40) {
                    podeUsarDRS = true;
                }
            }
        }
        return podeUsarDRS;
    }

    private void processaStress() {
        processaStressDesgastePneus();
        int fatorStresse = controleJogo.getRandom().intervalo(1, 5);
        if (getNoAtual().verificaCurvaAlta() || getNoAtual().verificaCurvaBaixa()) {
            fatorStresse /= 2;
        }
        if (NORMAL.equals(getModoPilotagem())) {
            decStress(fatorStresse);
        } else if (LENTO.equals(getModoPilotagem())) {
            decStress(fatorStresse * (testeHabilidadePiloto() ? 2 : 1));
        }
        processaStressPneusIncompativeis();
        processaStressFreioNaRetaMalSucedido();
        processaStressColisao();
        processaStressDanoAereofolio();
    }

    /**
     * Espelha as condições de estresse que antes viviam em
     * Carro.calculaDesgastePneus(No) — o desgaste de pneu em si continua lá,
     * só o incremento/decremento de estresse foi movido pra cá.
     */
    /**
     * Magnitude do incremento escala por modo de pilotagem: AGRESSIVO usa o
     * desgaste alto cheio, NORMAL usa metade disso, LENTO não gera incremento
     * algum (só se beneficia da recuperação abaixo).
     */
    private void processaStressDesgastePneus() {
        if (isRecebeuBanderada() || controleJogo.isSafetyCarNaPista()) {
            return;
        }
        No no = getNoAtual();
        if (controleJogo.getRandom().nextDouble() < (getCarro().getPorcentagemDesgastePneus() / 100.0)) {
            return;
        }
        if (no.verificaCurvaBaixa()) {
            if (AGRESSIVO.equals(getModoPilotagem())) {
                incStress(testeHabilidadePilotoAerodinamicaFreios() ? 10 : 20);
            } else if (NORMAL.equals(getModoPilotagem())) {
                incStress(testeHabilidadePilotoAerodinamicaFreios() ? 5 : 10);
            }
        } else if (no.verificaCurvaAlta()) {
            if (AGRESSIVO.equals(getModoPilotagem())) {
                incStress(testeHabilidadePilotoCarro() ? 10 : 20);
            } else if (NORMAL.equals(getModoPilotagem())) {
                incStress(testeHabilidadePilotoCarro() ? 5 : 10);
            }
        }
    }

    private void processaStressPneusIncompativeis() {
        if (!carro.verificaPneusIncompativeisClima() || isRecebeuBanderada()) {
            return;
        }
        if (getNoAtual().verificaCurvaBaixa()) {
            incStress(testeHabilidadePiloto() ? 0 : 4);
        }
        if (getNoAtual().verificaCurvaAlta()) {
            incStress(testeHabilidadePiloto() ? 0 : 2);
        }
    }

    private void processaStressFreioNaRetaMalSucedido() {
        if (freioNaRetaMalSucedidoNesteTick == null) {
            return;
        }
        incStress(freioNaRetaMalSucedidoNesteTick);
        freioNaRetaMalSucedidoNesteTick = null;
    }

    private void processaStressColisao() {
        if (evitaBaterCarroFrente) {
            incStress(8);
        } else if (getColisao() != null) {
            incStress(12);
        }

    }

    /**
     * Chamado diretamente por ControleBox.processarPilotoBox() enquanto o
     * piloto avança na fila do box — não faz parte de processaStress()
     * porque processarCiclo() (que chama processaStress()) só roda quando
     * getPtosBox() == 0, exatamente o oposto da janela em que este gatilho
     * se aplica.
     */
    public void processaStressFilaBox() {
        decStress(2);
    }

    /**
     * Chamado por ControleCorrida.danificaAreofolio() ao decidir que houve
     * dano de aerofólio por acidente — a decisão de acidente em si continua
     * lá; aqui só sinalizamos o flag consumido logo abaixo.
     */
    public void sinalizaDanoAereofolio() {
        sofreuDanoAereofolioNesteTick = true;
    }

    private void processaStressDanoAereofolio() {
        if (!sofreuDanoAereofolioNesteTick) {
            return;
        }
        incStress(30);
        sofreuDanoAereofolioNesteTick = false;
    }

    private void processaIAnovoIndex() {
        if (colisao != null || isRecebeuBanderada() || controleJogo.isModoQualify()) {
            return;
        }
        if (isJogadorHumano() &&
                (Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())
                        || controleJogo instanceof JogoServidor)) {
            return;
        }

        if (isManualTemporario()) {
            decrementaManualTemporario();
            return;
        }

        porcentagemDesgastePneus = getCarro().getPorcentagemDesgastePneus();
        porcentagemCombustivel = getCarro().getPorcentagemCombustivel();
        superAquecido = getCarro().verificaMotorSuperAquecido();
        porcentagemMotor = getCarro().getPorcentagemDesgasteMotor();
        porcentagemCorridaRestante = 100 - controleJogo.porcentagemCorridaConcluida();
        temMotor = porcentagemMotor > 30 || porcentagemMotor > porcentagemCorridaRestante;
        temCombustivel = porcentagemCombustivel > 10;
        if (controleJogo.isSemReabastecimento()) {
            temCombustivel = porcentagemCombustivel > porcentagemCorridaRestante;
        }
        temPneu = porcentagemDesgastePneus > 30;
        if (controleJogo.isSemTrocaPneu()) {
            temPneu = porcentagemDesgastePneus > 50 || porcentagemDesgastePneus > porcentagemCorridaRestante;
        }
        if (controleJogo.isSafetyCarNaPista() || getPtosBox() != 0 || danificado()) {
            getCarro().setGiro(Carro.GIRO_MIN_VAL);
            setModoPilotagem(LENTO);
            return;
        }
        iaTentaUsarErs();
        if (controleJogo.isDrs()) {
            iaTentaUsarDRS();
        }
        boolean tentaPassarFrete = tentarPassaPilotoDaFrente();
        boolean tentarEscaparAtras = false;
        if (!tentaPassarFrete) {
            tentarEscaparAtras = tentarEscaparPilotoAtras(false);
        }
        if (getNumeroVolta() > 0) {
            if (tentaPassarFrete && calculaDiferencaParaProximo < 100
                    && carroPilotoDaFrente.getPiloto().getPtosBox() == 0) {
                ativarErs = true;
                if (controleJogo.verificaInfoRelevante(this)) {
                    String txt = Lang.msg("tentaPassarFrete", new String[] { nomeJogadorFormatado(),
                            Html.negrito(getNome()), Html.negrito(carroPilotoDaFrente.getPiloto().getNome()) });
                    controleJogo.info(Html.preto(txt));
                }
            } else if (tentarEscaparAtras && calculaDiferencaParaAnterior < 100 && getPtosBox() == 0) {
                ativarErs = true;
                if (controleJogo.verificaInfoRelevante(this)) {
                    String txt = Lang.msg("tentarEscaparAtras", new String[] { nomeJogadorFormatado(),
                            Html.negrito(getNome()), Html.negrito(carroPilotoAtras.getPiloto().getNome()) });
                    controleJogo.info(Html.preto(txt));
                }
            }
        }

        if (!tentaPassarFrete && !tentarEscaparAtras) {
            getCarro().setGiro(Carro.GIRO_NOR_VAL);
            setModoPilotagem(NORMAL);
        }

        if (getCarro().verificaCondicoesCautelaGiro()) {
            getCarro().setGiro(Carro.GIRO_MIN_VAL);
        }
        if (getCarro().verificaCondicoesCautelaPneu()) {
            setModoPilotagem(LENTO);
        }
    }

    public void setManualTemporario() {
        if (!isJogadorHumano()) {
            return;
        }
        manualTemporario = 150;
    }

    private void decrementaManualTemporario() {
        if (manualTemporario > 0) {
            manualTemporario--;
        }
    }

    public boolean isManualTemporario() {
        if (manualTemporario > 0) {
            return true;
        }
        return false;
    }

    private void processaMudarTracado() {
        if (isRecebeuBanderada()) {
            return;
        }
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (isJogadorHumano() && Global.CONTROLE_MANUAL.equals(controleJogo.getAutomaticoManual())) {
            return;
        }
        if (!noAtual.verificaRetaOuLargada() && !controleJogo.isSafetyCarNaPista()) {
            mudouTracadoReta = 0;
        }
        Piloto pilotoBateu = controleJogo.getPilotoBateu();
        if (controleJogo.isSafetyCarNaPista() && pilotoBateu != null && !getNoAtual().isBox()
                && !pilotoBateu.getCarro().isRecolhido() && getTracado() == pilotoBateu.getTracado()) {
            int indiceCarro = pilotoBateu.getNoAtual().getIndex();

            int traz = indiceCarro - 300;
            int frente = indiceCarro + 100;

            List lista = pilotoBateu.obterPista();

            if (traz < 0) {
                traz = (lista.size() - 1) + traz;
            }
            if (frente > (lista.size() - 1)) {
                frente = (frente - (lista.size() - 1)) - 1;
            }
            if (getTracado() == 4) {
                mudarTracado(2);
            } else if (getTracado() == 5) {
                mudarTracado(1);
            } else if (getNoAtual().getIndex() >= traz && getNoAtual().getIndex() <= frente
                    && getIndiceTracado() == 0) {
                /**
                 * So forca o desvio com a animacao anterior concluida; o
                 * branch roda a cada ciclo, entao apenas espera a vez.
                 */
                int novapos = 0;
                if (pilotoBateu.getTracado() == 0) {
                    novapos = controleJogo.getRandom().intervalo(1, 2);
                }
                mudarTracado(novapos, true);
                return;
            }
        }
        if (isBoxSaiuNestaVolta() && controleJogo.verificaSaidaBox(this)) {
            mudarTracado(controleJogo.getCircuito().getLadoBoxSaidaBox(), true);
        } else if (getTracado() == controleJogo.getCircuito().getLadoBoxSaidaBox()
                && controleJogo.verificaSaidaBox(this) && getNumeroVolta() > 0) {
            mudarTracado(0);
        } else if (isBox() && getTracado() != controleJogo.getCircuito().getLadoBoxSaidaBox()
                && controleJogo.verificaEntradaBox(this)) {
            if (getTracado() == 0) {
                mudarTracado(controleJogo.getCircuito().getLadoBoxSaidaBox());
            } else {
                mudarTracado(0);
            }

        } else if (tentarEscaparFilaIndiana()) {
            /**
             * Preso em fila indiana com tracado lateral livre: ja mudou.
             */
        } else if ((evitaBaterCarroFrente && carroPilotoDaFrenteRetardatario != null
                && getTracado() == carroPilotoDaFrenteRetardatario.getPiloto().getTracado())
                || calculaDiffParaProximoRetardatario < (testeHabilidadePiloto() ? 100 : 150)) {
            desviaPilotoNaFrente(this, carroPilotoDaFrenteRetardatario.getPiloto());
        } else if (controleJogo.getNumVoltaAtual() > 1 && !isJogadorHumano() && testeHabilidadePiloto()
                && pontoEscape != null && calculaDiffParaProximoRetardatario > 150
                && distanciaEscape < (Carro.RAIO_DERRAPAGEM)) {
            if (getTracado() != 0) {
                mudarTracado(0);
            } else {
                int ladoEscape = controleJogo.obterLadoEscape(pontoEscape);
                if (ladoEscape == 5) {
                    mudarTracado(2);
                }
                if (ladoEscape == 4) {
                    mudarTracado(1);
                }
            }
        } else if ((calculaDiffParaProximoRetardatarioMesmoTracado < calculaDiferencaParaAnterior
                && calculaDiffParaProximoRetardatarioMesmoTracado < (testeHabilidadePilotoCarro() ? 150 : 200))) {
            desviaPilotoNaFrente(this, carroPilotoDaFrenteRetardatario.getPiloto());
        } else if (!isJogadorHumano() && carroPilotoAtras != null && mudouTracadoReta <= 1
                && calculaDiferencaParaAnterior < 150 && carroPilotoAtras.getPiloto().getTracado() != getTracado()
                && calculaDiferencaParaAnterior > 100 && carroPilotoAtras.getPiloto().getPtosBox() == 0
                && testeHabilidadePiloto() && !isFreiandoReta()) {
            if (mudarTracado(carroPilotoAtras.getPiloto().getTracado()) && noAtual.verificaRetaOuLargada()) {
                mudouTracadoReta++;
            }
        } else if (!isJogadorHumano() && calculaDiferencaParaAnterior > 250
                && calculaDiffParaProximoRetardatario > 250) {
            mudarTracado(0);
        }
    }

    public void desviaPilotoNaFrente(Piloto piloto, Piloto pilotoNaFrente) {
        boolean lento = Piloto.LENTO.equals(piloto.getModoPilotagem())
                && Carro.GIRO_MIN_VAL == piloto.getCarro().getGiro();
        if (!lento && verificaPassarRetardatario(piloto, pilotoNaFrente)) {
            pilotoNaFrente.getCarro().setGiro(Carro.GIRO_MIN_VAL);
            pilotoNaFrente.setModoPilotagem(Piloto.LENTO);
            pilotoNaFrente.incStress(pilotoNaFrente.testeHabilidadePiloto() ? 1 : 2);
            mensagemRetardatario(piloto, pilotoNaFrente);
        }
        int novapos = 0;
        if (pilotoNaFrente.getTracado() == 0) {
            novapos = controleJogo.getRandom().intervalo(1, 2);
            if (piloto.verificaColisaoAoMudarDeTracado(novapos)) {
                if (novapos == 2) {
                    novapos = 1;
                } else {
                    novapos = 2;
                }
            }
        }
        piloto.mudarTracado(novapos);
    }

    public void mensagemRetardatario(Piloto piloto, Piloto pilotoNaFrente) {
        if (controleJogo.verificaInfoRelevante(piloto) && controleJogo.getRandom().nextDouble() > 0.9
                && !controleJogo.isSafetyCarNaPista()) {
            if (pilotoNaFrente.getTracado() == piloto.getTracado()) {
                String msg = Lang.msg("020", new String[] { pilotoNaFrente.getNome(), piloto.getNome() });
                controleJogo.info(Html.azul(msg));
            } else if (pilotoNaFrente.getTracado() == piloto.getTracado()
                    && pilotoNaFrente.equals(piloto.getColisao())) {
                String msg = Lang.msg("021", new String[] { pilotoNaFrente.getNome(), piloto.getNome() });
                controleJogo.info(Html.azul(msg));
            }
            pilotoNaFrente.incStress(pilotoNaFrente.testeHabilidadePiloto() ? 2 : 5);
        }
    }

    public boolean verificaPassarRetardatario(Piloto piloto, Piloto pilotoNaFrente) {
        return !controleJogo.isCorridaTerminada() && !piloto.isRecebeuBanderada()
                && pilotoNaFrente.getNumeroVolta() < getNumeroVolta()
                && pilotoNaFrente.getPtosPista() < piloto.getPtosPista() && !pilotoNaFrente.isDesqualificado()
                && (pilotoNaFrente.getPtosBox() == 0);
    }

    private boolean tentarEscaparPilotoAtras(boolean tentaPassarFrete) {
        if (tentaPassarFrete) {
            return false;
        }
        if (carroPilotoAtras == null) {
            return false;
        }
        Piloto pilotoAtras = carroPilotoAtras.getPiloto();
        if (pilotoAtras.getPtosBox() != 0) {
            return false;
        }
        if (calculaDiferencaParaAnterior < (controleJogo.isDrs() ? 600 : 300) && testeHabilidadePilotoCarro()) {
            modoIADefesaAtaque();
            return true;
        }
        return false;
    }

    private void iaTentaUsarDRS() {
        if (controleJogo.isChovendo()) {
            return;
        }
        if (controleJogo.getNumVoltaAtual() <= 1) {
            return;
        }
        if (ativarDRS) {
            return;
        }
        setAtivarDRS(getNoAtual().verificaRetaOuLargada() && testeHabilidadePiloto());
    }

    private void iaTentaUsarErs() {
        if (!controleJogo.isErs()) {
            return;
        }
        if (noAtual == null) {
            return;
        }
        int percetagemDeVoltaConcluida = controleJogo.percetagemDeVoltaConcluida(this);
        if (percetagemDeVoltaConcluida > 20 && noAtual.verificaRetaOuLargada()) {
            ativarErs = true;
        }
        if (percetagemDeVoltaConcluida > 60) {
            ativarErs = true;
        }
    }

    public boolean verificaNaoPrecisaDesviar(Piloto pilotoFrente) {
        return pilotoFrente.verificaNaoPrecisaDesenhar();
    }

    public boolean verificaNaoPrecisaDesenhar() {
        String danificado = getCarro().getDanificado();
        return getCarro().isPaneSeca() || Carro.ABANDONOU.equals(danificado) || getCarro().isRecolhido()
                || Carro.PANE_SECA.equals(danificado) || Carro.EXPLODIU_MOTOR.equals(danificado);
    }

    private void processaEvitaBaterCarroFrente() {
        evitaBaterCarroFrente = false;
        if (controleJogo.isModoQualify()) {
            return;
        }
        if (carroPilotoDaFrenteRetardatario == null) {
            return;
        }
        Piloto pilotoFrente = carroPilotoDaFrenteRetardatario.getPiloto();
        if (this.equals(pilotoFrente)) {
            return;
        }
        if (verificaNaoPrecisaDesviar(pilotoFrente)) {
            return;
        }
        if (pilotoFrente.getPtosBox() > 0) {
            return;
        }
        limiteEvitarBatrCarroFrente = 200;
        if (getCarro().getDurabilidadeAereofolio() < (Global.DURABILIDADE_AREOFOLIO / 2)) {
            limiteEvitarBatrCarroFrente += 100;
        }
        if (pilotoFrente.getColisao() != null) {
            limiteEvitarBatrCarroFrente += 100;
        }
        if (pilotoFrente.getColisao() != null && pilotoFrente.getTracado() == getTracado()) {
            limiteEvitarBatrCarroFrente += 100;
        }
        if (calculaDiffParaProximoRetardatarioMesmoTracado < limiteEvitarBatrCarroFrente
                && (pilotoFrente.getTracado() == getTracado()
                        || pilotoFrente.getColisao() != null)) {
            evitaBaterCarroFrente = true;
        }
    }

    public boolean escapaTracado() {
        /**
         * Verificar na entrada da curva e nao na area de escape
         */
        if (getTracado() == 4 || getTracado() == 5) {
            return false;
        }
        /**
         * Espera a animacao da troca de tracado atual terminar; escapar no
         * meio dela resetava a interpolacao e o carro teleportava de linha.
         */
        if (getIndiceTracado() > 0) {
            return false;
        }
        if (pontoEscape == null) {
            return false;
        }
        if (distanciaEscape > Carro.RAIO_DERRAPAGEM) {
            return false;
        }
        if (getNoAtual() != null && indexRefEscape < getNoAtual().getIndex()) {
            return false;
        }
        int ladoEscape = controleJogo.obterLadoEscape(pontoEscape);
        if (ladoEscape == 5 && getTracado() != 1) {
            return false;
        }
        if (ladoEscape == 5 && controleJogo.getCircuito().getPista5Full().get(noAtual.getIndex()) != null) {
            return false;
        }
        if (ladoEscape == 4 && controleJogo.getCircuito().getPista4Full().get(noAtual.getIndex()) != null) {
            return false;
        }
        if (ladoEscape == 4 && getTracado() != 2) {
            return false;
        }
        if ((ladoEscape == 4 || ladoEscape == 5) && getTracado() == 0) {
            return false;
        }
        mudarTracado(ladoEscape, true);
        return true;
    }

    @JsonIgnore
    public Point getPontoDerrapada() {
        return pontoEscape;
    }

    public void processaPontoEscape() {
        distanciaEscape = Double.MAX_VALUE;
        pontoEscape = null;
        indexRefEscape = 0;
        if (getNoAtual() == null) {
            return;
        }
        int index = getNoAtual().getIndex() + 100;
        if (index >= controleJogo.getNosDaPista().size()) {
            return;
        }
        No proxPt = controleJogo.getNosDaPista().get(index);
        Circuito circuito = controleJogo.getCircuito();
        Map<PontoEscape, List<No>> escapeMap = circuito.getEscapeMap();
        if (escapeMap == null) {
            return;
        }
        Point p = proxPt.getPoint();
        Set<PontoEscape> keySet = escapeMap.keySet();
        for (Iterator<PontoEscape> iterator = keySet.iterator(); iterator.hasNext();) {
            PontoEscape pontoEscapada = iterator.next();
            double distaciaEntrePontos = GeoUtil.distaciaEntrePontos(p, pontoEscapada.getPoint());
            if (distaciaEntrePontos < distanciaEscape) {
                if (escapeMap.get(pontoEscapada).get(index) != null) {
                    indexRefEscape = 0;
                    distanciaEscape = Double.MAX_VALUE;
                    pontoEscape = null;
                    return;
                }
                distanciaEscape = distaciaEntrePontos;
                pontoEscape = pontoEscapada.getPoint();
                indexRefEscape = index;
            }
        }
    }

    public Rectangle2D centralizaCarroColisao() {
        if (controleJogo.isModoQualify()) {
            return null;
        }
        if (getNoAnterior() != null && diateiraColisao != null && centroColisao != null && trazeiraColisao != null
                && emMovimento()) {
            return null;
        }
        No noAtual = getNoAtual();
        if (noAtual == null) {
            return null;
        }
        int cont = noAtual.getIndex();
        List<No> lista = controleJogo.obterPista(noAtual);
        if (lista == null) {
            return null;
        }
        Point p = noAtual.getPoint();
        if (p == null) {
            return null;
        }
        int carx = p.x;
        int cary = p.y;
        int traz = cont - METADE_CARRO;
        int frente = cont + METADE_CARRO;
        if (traz < 0) {
            if (controleJogo.getNosDoBox().size() == lista.size()) {
                traz = 0;
            } else {
                traz = (lista.size() - 1) + traz;
            }
        }
        if (frente > (lista.size() - 1)) {
            if (controleJogo.getNosDoBox().size() == lista.size()) {
                frente = lista.size() - 1;
            } else {
                frente = (frente - (lista.size() - 1)) - 1;
            }
        }
        if (traz > (lista.size() - 1)) {
            if (controleJogo.getNosDoBox().size() == lista.size()) {
                traz = lista.size() - 1;
            } else {
                traz = (traz - (lista.size() - 1)) - 1;
            }
        }

        Point trazCar = lista.get(traz).getPoint();
        trazCar = new Point(trazCar.x, trazCar.y);
        Point frenteCar = lista.get(frente).getPoint();
        frenteCar = new Point(frenteCar.x, frenteCar.y);
        double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
        Rectangle2D rectangle = new Rectangle2D.Double((p.x - Carro.MEIA_LARGURA_CIMA), (p.y - Carro.MEIA_ALTURA_CIMA),
                Carro.LARGURA_CIMA, Carro.ALTURA_CIMA);
        Point p1;
        Point p2;
        Point p4 = null;
        Point p5 = null;
        if (noAtual.isBox()) {
            p1 = controleJogo.getCircuito().getBox1Full().get(noAtual.getIndex()).getPoint();
            p2 = controleJogo.getCircuito().getBox2Full().get(noAtual.getIndex()).getPoint();
        } else {
            p1 = controleJogo.getCircuito().getPista1Full().get(noAtual.getIndex()).getPoint();
            p2 = controleJogo.getCircuito().getPista2Full().get(noAtual.getIndex()).getPoint();
            p5 = controleJogo.getCircuito().getPista5Full().get(noAtual.getIndex()) != null
                    ? controleJogo.getCircuito().getPista5Full().get(noAtual.getIndex()).getPoint()
                    : p1;
            p4 = controleJogo.getCircuito().getPista4Full().get(noAtual.getIndex()) != null
                    ? controleJogo.getCircuito().getPista4Full().get(noAtual.getIndex()).getPoint()
                    : p1;
        }
        if (p4 == null) {
            p4 = p2;
        }
        if (p5 == null) {
            p5 = p1;
        }
        if (getTracado() == 0) {
            carx = p.x;
            cary = p.y;
            int indTracado = getIndiceTracado();
            if (indTracado > 0 && getTracadoAntigo() != 0) {
                Point pReta = linhaColisaoTracado0(p, p1, p2, p4, p5, indTracado);
                carx = pReta.x;
                cary = pReta.y;
            }
        }
        if (getTracado() == 1) {
            carx = Util.inteiro((p1.x));
            cary = Util.inteiro((p1.y));
            int indTracado = getIndiceTracado();
            if (indTracado > 0 && getTracadoAntigo() != 1) {
                Point pReta = linhaColisaoTracado1(p, p1, p2, p4, p5, indTracado);
                carx = pReta.x;
                cary = pReta.y;
            }
        }

        if (getTracado() == 5) {
            carx = Util.inteiro((p5.x));
            cary = Util.inteiro((p5.y));
            int indTracado = getIndiceTracado();
            if (indTracado > 0 && getTracadoAntigo() != 5) {
                Point pReta = linhaColisaoTracado5(p, p1, p2, p5, indTracado);
                carx = pReta.x;
                cary = pReta.y;
            }
        }

        if (getTracado() == 2) {
            carx = Util.inteiro((p2.x));
            cary = Util.inteiro((p2.y));
            int indTracado = getIndiceTracado();
            if (indTracado > 0 && getTracadoAntigo() != 2) {
                Point pReta = linhaColisaoTracado2(p, p1, p2, p4, p5, indTracado);
                carx = pReta.x;
                cary = pReta.y;
            }
        }

        if (getTracado() == 4) {
            carx = Util.inteiro((p4.x));
            cary = Util.inteiro((p4.y));
            int indTracado = getIndiceTracado();
            if (indTracado > 0 && getTracadoAntigo() != 4) {
                Point pReta = linhaColisaoTracado4(p, p1, p2, p4, indTracado);
                carx = pReta.x;
                cary = pReta.y;
            }
        }

        rectangle = new Rectangle2D.Double((carx - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
                (cary - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO), Carro.ALTURA * Carro.FATOR_AREA_CARRO,
                Carro.ALTURA * Carro.FATOR_AREA_CARRO);

        centroColisao = rectangle.getBounds();

        trazCar = GeoUtil.calculaPonto(calculaAngulo + 90, Util.inteiro(METADE_CARRO), new Point(carx, cary));

        Rectangle2D trazRec = new Rectangle2D.Double((trazCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
                (trazCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO), Carro.ALTURA * Carro.FATOR_AREA_CARRO,
                Carro.ALTURA * Carro.FATOR_AREA_CARRO);
        trazeiraColisao = trazRec.getBounds();

        frenteCar = GeoUtil.calculaPonto(calculaAngulo + 270, Util.inteiro(METADE_CARRO), new Point(carx, cary));

        Rectangle2D frenteRec = new Rectangle2D.Double((frenteCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
                (frenteCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO), Carro.ALTURA * Carro.FATOR_AREA_CARRO,
                Carro.ALTURA * Carro.FATOR_AREA_CARRO);
        diateiraColisao = frenteRec.getBounds();

        return rectangle;
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

    public List obterPista() {
        return controleJogo.obterPista(this.getNoAtual());
    }

    public Piloto() {
        zerarGanhoEVariaveisUlt();
    }

    public void zerarGanhoEVariaveisUlt() {
        listGanho = new ArrayList<Double>();
        listGanho.addAll(IntStream.range(0, 15)
                .mapToObj(num -> Double.valueOf(0))
                .collect(Collectors.toList()));
        velocidade = 0;
        velocidadeAnterior = 0;
        ultimaMudancaPos = 0;
        ultimoConsumoCombust = Integer.valueOf(0);
        ultimoConsumoPneu = Integer.valueOf(0);
    }

    public void processaGanhoMedio() {
        if (controleJogo.isModoQualify()) {
            return;
        }
        while (listGanho.size() > (noAtual.verificaRetaOuLargada() ? 20 : 10)) {
            listGanho.remove(0);
        }
        listGanho.add(Double.valueOf(ganho));
        double soma = 0;
        for (Iterator iterator = listGanho.iterator(); iterator.hasNext();) {
            Double val = (Double) iterator.next();
            soma += val.doubleValue();
        }
        ganho = soma / listGanho.size();
    }

    private boolean tentarPassaPilotoDaFrente() {
        if (carroPilotoDaFrente == null) {
            return false;
        }
        if (getStress() > getValorLimiteStressePararErrarCurva()) {
            return false;
        }
        if (calculaDiferencaParaProximo < 500 && testeHabilidadePilotoCarro()) {
            modoIADefesaAtaque();
            return true;
        }
        return false;
    }

    private void processaMudancaRegime() {
        if (isRecebeuBanderada()) {
            setModoPilotagem(Piloto.LENTO);
            getCarro().setGiro(Carro.GIRO_MIN_VAL);
            return;
        }
        if (AGRESSIVO.equals(getModoPilotagem())) {
            mensangesModoAgressivo();
        }
        if (LENTO.equals(getModoPilotagem())) {
            mensagemPilotoLento();
        }
    }

    private void modoIADefesaAtaque() {
        if (!testeHabilidadePiloto()) {
            return;
        }
        double valorLimiteStressePararErrarCurva = 100;
        boolean derrapa = getNoAtual() != null && indexRefEscape > getNoAtual().getIndex();
        if (derrapa && testeHabilidadePiloto()) {
            valorLimiteStressePararErrarCurva = getValorLimiteStressePararErrarCurva();
        }
        boolean maxPilotagem = false;
        boolean maxCarro = false;

        if (getNoAtual().verificaRetaOuLargada()) {
            maxCarro = (!superAquecido && temMotor && temCombustivel && testeHabilidadePilotoCarro()) || (!superAquecido
                    && ativarDRS && controleJogo.isDrs() && Carro.MENOS_ASA.equals(getCarro().getAsa()));
        } else {
            maxPilotagem = temPneu && stress < valorLimiteStressePararErrarCurva;
        }
        if (maxPilotagem) {
            setModoPilotagem(AGRESSIVO);
        } else {
            setModoPilotagem(NORMAL);
        }
        if (maxCarro) {
            getCarro().setGiro(Carro.GIRO_MAX_VAL);
        } else {
            getCarro().setGiro(Carro.GIRO_NOR_VAL);
        }
    }

    private void mensagemPilotoLento() {
        if (!controleJogo.verificaInfoRelevante(this)) {
            return;
        }
        if (controleJogo.isSafetyCarNaPista() || getCarro().verificaDano()) {
            return;
        }
        if ((controleJogo.getNumVoltaAtual() - voltaMensagemLento) > 2) {
            voltaMensagemLento = controleJogo.getNumVoltaAtual();
        } else {
            return;
        }
        controleJogo.info(Html.vermelho(nomeJogadorFormatado() + " " + getNome() + Lang.msg("057")));
    }

    private void mensangesModoAgressivo() {
        if (controleJogo.isSafetyCarNaPista()) {
            return;
        }
        if (controleJogo.verificaNoPitLane(this)) {
            return;
        }
        if (!controleJogo.verificaInfoRelevante(this)) {
            return;
        }
        if (controleJogo.getRandom().nextDouble() < 0.995) {
            return;
        }
        if (AGRESSIVO.equals(getModoPilotagem())) {
            if (controleJogo.isChovendo()) {
                controleJogo
                        .info(Html.negrito(nomeJogadorFormatado() + " " + getNome()) + Html.negrito(Lang.msg("052")));
            } else if (getNoAtual().verificaCurvaBaixa()) {

                if (controleJogo.getRandom().nextDouble() > 0.5) {
                    controleJogo.info(
                            Html.txtRedBold(nomeJogadorFormatado() + " " + getNome()) + Html.negrito(Lang.msg("053")));
                } else {
                    controleJogo.info(
                            Html.txtRedBold(nomeJogadorFormatado() + " " + getNome()) + Html.negrito(Lang.msg("054")));
                }
            }
        } else {
            if (controleJogo.isChovendo()) {
                controleJogo
                        .info(Html.negrito(nomeJogadorFormatado() + " " + getNome()) + Html.vermelho(Lang.msg("055")));
            } else {
                controleJogo
                        .info(Html.negrito(nomeJogadorFormatado() + " " + getNome()) + Html.vermelho(Lang.msg("056")));
            }
        }
    }

    private boolean testeHabilidadePilotoHumanoCarro() {
        if (danificado()) {
            return false;
        }
        return carro.testePotencia() && testeHabilidadePiloto();
    }

    private int calculaModificadorPrincipal() {
        double comparador = 0.3;
        if (Carro.GIRO_MAX_VAL == getCarro().getGiro()) {
            comparador += getCarro().testePotencia() ? 0.3 : 0.2;
        }
        if (Carro.GIRO_NOR_VAL == getCarro().getGiro()) {
            comparador += testeHabilidadePiloto() ? 0.1 : 0.0;
        }
        if (Carro.GIRO_MIN_VAL == getCarro().getGiro()) {
            comparador += getCarro().testePotencia() ? 0.0 : -0.1;
        }
        if (!noAtual.verificaRetaOuLargada()) {
            if (AGRESSIVO.equals(modoPilotagem)) {
                comparador += testeHabilidadePiloto() ? 0.3 : 0.2;
            }
            if (NORMAL.equals(modoPilotagem)) {
                comparador += testeHabilidadePiloto() ? 0.1 : 0.0;
            }
            if (LENTO.equals(modoPilotagem)) {
                comparador += testeHabilidadePiloto() ? 0.0 : -0.1;
            }
        }
        if (usandoErs()) {
            comparador += 0.2;
        }
        if (controleJogo.isChovendo() && !getNoAtual().verificaRetaOuLargada()) {
            comparador -= testeHabilidadePilotoAerodinamica() ? 0.2 : 0.3;
        }
        if (getNoAtual().verificaRetaOuLargada() && testeHabilidadePiloto() && getCarro().testePotencia()
                && getCarro().testeAerodinamica()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 50 : 45);
        } else if (getNoAtual().verificaRetaOuLargada() && getCarro().testePotencia()
                && getCarro().testeAerodinamica()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 45 : 40);
        } else if (getNoAtual().verificaRetaOuLargada() && getCarro().testePotencia()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 40 : 35);
        } else if (getNoAtual().verificaRetaOuLargada()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 35 : 30);
        } else if (getNoAtual().verificaCurvaAlta() && testeHabilidadePilotoAerodinamica()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 30 : 25);
        } else if (getNoAtual().verificaCurvaAlta()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 25 : 20);
        } else if (getNoAtual().verificaCurvaBaixa() && testeHabilidadePilotoFreios()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 20 : 15);
        } else if (getNoAtual().verificaCurvaBaixa()) {
            return (controleJogo.getRandom().nextDouble() < comparador ? 15 : 10);
        } else {
            return (controleJogo.getRandom().nextDouble() < comparador) ? 10 : 5;
        }
    }

    public boolean testeHabilidadePilotoCarro() {
        if (danificado()) {
            return false;
        }
        return carro.testePotencia() && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePiloto() {
        if (danificado()) {
            return false;
        }
        return controleJogo.getRandom().nextDouble() < (habilidade / 1000.0);
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

    public void efetuarSaidaBox() {
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
        Collections.sort(ordenaVoltas, new MyComparator());

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

    /**
     * Escala de recuperação por modo de pilotagem: NORMAL recupera 10% a
     * mais (recuperação mais cadenciada — reduzido de 25%, que ficava
     * agressivo demais somado ao decaimento passivo incondicional por tick),
     * LENTO recupera 50% a mais (mais que o normal); AGRESSIVO não muda
     * (mantém a ausência de decaimento passivo que já tinha).
     */
    public void decStress(int val) {
        if (NORMAL.equals(getModoPilotagem())) {
            val = Math.round(val * 1.1f);
        } else if (LENTO.equals(getModoPilotagem())) {
            val = Math.round(val * 1.5f);
        }
        if (stress > 0 && (stress - val) > 0
                && (controleJogo.getRandom().nextDouble() > ((700.0 - getPosicao() * 10) / 1000.0))) {
            stress -= val;
        }
    }

    /**
     * Escala de geração por modo de pilotagem: AGRESSIVO gera 50% a menos —
     * o máximo possível sem zerar o menor incremento existente (val=1, usado
     * em colisão/desconcentração: Math.round(1 * 0.5) = 1, mas qualquer fator
     * menor que 0.5 arredondaria pra 0 e a regra deixaria de gerar estresse);
     * NORMAL também gera metade.
     */
    public void incStress(int val) {
        if (isRecebeuBanderada()) {
            return;
        }
        if (AGRESSIVO.equals(getModoPilotagem()) || NORMAL.equals(getModoPilotagem())) {
            val = Math.round(val * 0.5f);
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
            if ((controleJogo.getRandom().nextDouble() < ((900 - getPosicao() * 17.5) / 1000.0)))
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
        for (Iterator iterator = ultsConsumosCombustivel.iterator(); iterator.hasNext();) {
            Integer longVal = (Integer) iterator.next();
            valmed += longVal.doubleValue();
        }
        if (ultsConsumosCombustivel.isEmpty())
            return 0;
        return valmed / ultsConsumosCombustivel.size();
    }

    public double calculaConsumoMedioPneu() {
        double valmed = 0;
        for (Iterator iterator = ultsConsumosPneu.iterator(); iterator.hasNext();) {
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

    public boolean mudarTracado(int mudarTracado) {
        return mudarTracado(mudarTracado, false);
    }

    public boolean mudarTracado(int mudarTracado, boolean forcaMudar) {
        return mudarTracado(mudarTracado, forcaMudar, false);
    }

    public boolean mudarTracado(int mudarTracado, boolean forcaMudar, boolean escapandoFila) {
        if (!forcaMudar && isRecebeuBanderada()) {
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
        if (getCarro().testeFreios()) {
            multi -= 2;
        }
        /**
         * Com atualizacao suave o carro desenhado fica atras da posicao real;
         * mudar de tracado e voltar logo em seguida faz o carro suave cortar a
         * linha de outro carro. O cooldown vale para qualquer mudanca, nao so
         * quando ja existe colisao, exceto ida ao box e volta de escapada.
         */
        boolean cooldownSuave = controleJogo.isAtualizacaoSuave() && !isBox();
        if (cooldownSuave) {
            /**
             * O intervalo minimo entre mudancas cobre a animacao inteira da
             * troca (indiceTracado cai 2 por ciclo) mais uma folga, para a
             * proxima mudanca nunca comecar com a anterior ainda animando.
             */
            double ciclosAnimacao = controleJogo.getCircuito().getIndiceTracado() / 2.0;
            if (multi < ciclosAnimacao + 4) {
                multi = ciclosAnimacao + 4;
            }
        }
        if (!forcaMudar && getTracado() != 4 && getTracado() != 5 && (cooldownSuave || getColisao() != null)
                && (agora - ultimaMudancaPos) < (controleJogo.tempoCicloCircuito() * multi)) {
            return false;
        }
        if (getTracado() == 1 && mudarTracado == 2) {
            return false;
        }
        if (getTracado() == 2 && mudarTracado == 1) {
            return false;
        }
        if (!forcaMudar && !escapandoFila && verificaColisaoAoMudarDeTracado(mudarTracado)) {
            return false;
        } else {
            /**
             * Cooldown conta a partir da ultima mudanca efetivada; tentativas
             * bloqueadas nao renovam o cooldown, senao um carro preso em fila
             * nunca acumula tempo suficiente para conseguir mudar de tracado.
             */
            ultimaMudancaPos = System.currentTimeMillis();
            int tracadoAntigoAnterior = getTracadoAntigo();
            int indiceRestante = indiceTracado;
            setTracadoAntigo(getTracado());
            setTracado(mudarTracado);
            calculaIndiceTracado();
            if (indiceRestante > 0) {
                /**
                 * Mudanca forcada no meio da animacao anterior. Se esta
                 * voltando para a linha de origem, continua da posicao
                 * lateral atual (espelha o progresso) em vez de teleportar
                 * o carro para o inicio da nova interpolacao.
                 */
                if (mudarTracado == tracadoAntigoAnterior) {
                    int continua = indiceTracado - indiceRestante;
                    if (continua < 1) {
                        continua = 1;
                    }
                    setIndiceTracado(continua);
                }
                if (Global.LOG_COLISAO) {
                    Logger.logar("[TRACADO_RESET] piloto=" + getNome() + " de=" + getTracadoAntigo() + " para="
                            + mudarTracado + " antAnterior=" + tracadoAntigoAnterior + " restante=" + indiceRestante
                            + " forca=" + forcaMudar);
                }
            }
            return true;
        }
    }

    public void calculaIndiceTracado() {
        double novoIndice = controleJogo.getCircuito().getIndiceTracado();
        if (getTracadoAntigo() == 4 || getTracadoAntigo() == 5) {
            novoIndice = controleJogo.getCircuito().getIndiceTracadoForaPista();
        }
        setIndiceTracado((int) novoIndice);
    }

    public int decIndiceTracado() {
        if (indiceTracado <= 0) {
            return 0;
        }
        if ((getTracadoAntigo() == 4 || getTracadoAntigo() == 5)
                && indiceTracado < (Carro.ALTURA * controleJogo.getCircuito().getMultiplicadorLarguraPista())) {
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

    public void calculaVelocidadeExibir() {
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
            incFreiada = controleJogo.getRandom().intervalo(5, 10);
        }
        if (getNoAtual().verificaCurvaAlta()) {
            incFreiada = controleJogo.getRandom().intervalo(0, 5);
        }
        if (isFreiandoReta()) {
            incFreiada += controleJogo.getRandom().intervalo(5, 10);
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

    public boolean verificaColisaoAoMudarDeTracado(int pos) {
        if (getPtosBox() != 0) {
            return false;
        }
        int indice = getNoAtual().getIndex();
        List pilotos = controleJogo.getPilotosCopia();
        for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
            Piloto piloto = (Piloto) iterator.next();
            if (this.equals(piloto) || piloto.getTracado() == pos) {
                continue;
            }
            int indiceCarro = piloto.getNoAtual().getIndex();
            int traz = indiceCarro - 75;
            int frente = indiceCarro + 75;
            List lista = piloto.obterPista();
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

    public int calculaDiffParaAnterior() {
        return controleJogo.calculaDiferencaParaAnterior(this);
    }

    public int obterNovoTracadoPossivel() {
        if (tracado == 0) {
            return controleJogo.getRandom().intervalo(1, 2);
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

    public boolean testeHabilidadePilotoAerodinamica() {
        if (danificado()) {
            return false;
        }
        return carro.testeAerodinamica() && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePilotoFreios() {
        if (danificado()) {
            return false;
        }
        return carro.testeFreios() && testeHabilidadePiloto();
    }

    public boolean testeHabilidadePilotoAerodinamicaFreios() {
        if (danificado()) {
            return false;
        }
        return carro.testeFreios() && carro.testeAerodinamica() && testeHabilidadePiloto();
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
            for (Iterator iterator = list.iterator(); iterator.hasNext();) {
                total += ((Double) iterator.next()).doubleValue();
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
        return podeUsarDRS;
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
                    if (Util.isWrapperType(object.getClass())) {
                        continue;
                    }
                    valor = object.toString();
                }
                campos.add(field.getName() + " = " + valor + "<br>");
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
        Collections.sort(campos, new StringComparator());
        for (Iterator<String> iterator = campos.iterator(); iterator.hasNext();) {
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
        this.podeUsarDRS = podeUsarDRS;
    }

    public boolean usandoErs() {
        return isAtivarErs() && getCarro().getCargaErs() > 0;
    }

    public String getSegundosParaRival() {
        return segundosParaRival;
    }

    public void setSegundosParaRival(String segundosParaRival) {
        this.segundosParaRival = segundosParaRival;
    }

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            return o1.toLowerCase().compareTo(o2.toLowerCase());
        }
    }

    private static class MyComparator implements Comparator {
        public int compare(Object arg0, Object arg1) {
            Volta v0 = (Volta) arg0;
            Volta v1 = (Volta) arg1;

            return Double.compare(v0.obterTempoVolta().doubleValue(), v1.obterTempoVolta().doubleValue());
        }
    }
}
