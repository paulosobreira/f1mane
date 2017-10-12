package sowbreira.f1mane.entidades;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import br.nnpe.Constantes;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.ControleCorrida;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.idiomas.Lang;
import sowbreira.f1mane.visao.PainelCircuito;

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
	public static final int MEIAENVERGADURA = 20;

	private int id;
	private String nome;
	private String nomeCarro;
	private String nomeJogador;
	private int posicao;
	private boolean desqualificado;
	private boolean jogadorHumano;
	private int numeroVolta;
	private Volta melhorVolta;
	private Carro carro = new Carro();
	private String vantagem;
	private int qtdeParadasBox;

	@JsonIgnore
	private int carX;
	@JsonIgnore
	private int carY;
	@JsonIgnore
	private double ganho;
	@JsonIgnore
	private int velocidadeExibir;
	@JsonIgnore
	private boolean autoPos = true;
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
	private boolean podeUsarDRS;
	@JsonIgnore
	private String calculaSegundosParaProximo;
	@JsonIgnore
	private int cargaErsVisual;
	@JsonIgnore
	private int cargaKersOnline;
	@JsonIgnore
	private List<String> ultimas5Voltas = new ArrayList<String>();
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
	private boolean asfaltoAbrasivo;
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
	private transient int habilidade;
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
	private int ciclosVoltaQualificacao;
	@JsonIgnore
	private long parouNoBoxMilis;
	@JsonIgnore
	private long saiuDoBoxMilis;
	@JsonIgnore
	private long ultimaMudancaPos;
	@JsonIgnore
	private int novoModificadorBruto;
	@JsonIgnore
	private int novoModificador;
	@JsonIgnore
	private int novoModificadorCarro;
	@JsonIgnore
	private boolean driveThrough;
	@JsonIgnore
	private Double maxGanhoBaixa = new Double(0);
	@JsonIgnore
	private Double maxGanhoAlta = new Double(0);
	@JsonIgnore
	private int contTravouRodas;
	@JsonIgnore
	private boolean travouRodas;
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
	private int tamanhoBufferGanho = 10;
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
	private Set<String> votosDriveThru = new HashSet<String>();
	@JsonIgnore
	private List<Integer> ultsConsumosCombustivel = new LinkedList<Integer>();
	@JsonIgnore
	private List<Integer> ultsConsumosPneu = new LinkedList<Integer>();
	@JsonIgnore
	private List<Double> ganhosBaixa = new ArrayList<Double>();
	@JsonIgnore
	private List<Double> ganhosAlta = new ArrayList<Double>();
	@JsonIgnore
	private List<Double> ganhosReta = new ArrayList<Double>();
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
			if (contTravouRodas > 0 && noAtual != null
					&& noAtual.verificaRetaOuLargada()) {
				contTravouRodas--;
			}
			return true;
		}
		return false;
	}

	public void setTravouRodas(int contTravouRodas) {
		if (contTravouRodas <= 0) {
			setTravouRodas(false);
		} else {
			setTravouRodas(true);
		}
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

	public void setAutoPos(boolean autoPos) {
		this.autoPos = autoPos;
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

	public boolean isAutoPos() {
		return autoPos;
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
			if (other.nome != null)
				return false;
		} else if (!nome.equals(other.nome))
			return false;
		return true;
	}

	public String getTipoPneuJogador() {
		return tipoPneuJogador;
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

	public void setVoltas(List<Volta> voltas) {
		this.voltas = voltas;
	}

	public long getParouNoBoxMilis() {
		return parouNoBoxMilis;
	}

	public int getCiclosVoltaQualificacao() {
		return ciclosVoltaQualificacao;
	}

	public void setCiclosVoltaQualificacao(int ciclosVoltaQualificacao) {
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

	public String getNome() {
		return nome;
	}

	public void setUltimaVolta(Volta ultimaVolta) {
		this.ultimaVolta = ultimaVolta;
	}

	public void setNome(String nome) {
		if (nome == null || "".equals(nome))
			nome = "H";
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
		List<No> pista = controleJogo.getNosDaPista();
		int index = processaNovoIndex(controleJogo);
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
			processaAjustesPosQualificacao(
					Constantes.MAX_VOLTAS / controleJogo.totalVoltasCorrida());
			processaUltimosDesgastesPneuECombustivel();
			processaAsfaltoAbrasivoIA(controleJogo);
			index = diff;
			if (getNumeroVolta() > 0) {
				getCarro().setCargaErs(InterfaceJogo.CARGA_ERS);
			}
			ativarErs = false;
			controleJogo.processaVoltaRapida(this);
			/**
			 * calback de nova volta para corrida Toda
			 */
			if (posicao == 1) {
				controleJogo.processaNovaVolta();
			}
			if (Logger.ativo && !controleJogo.isModoQualify()) {
				String tempoVolta = "";
				if (getUltimaVolta() != null) {
					tempoVolta = getUltimaVolta().getTempoVoltaFormatado();
				}
				Logger.logar(" Numero Volta " + getNumeroVolta() + " "
						+ getNome() + " Posição " + getPosicao() + " Tempo "
						+ tempoVolta);
			}
			if (numeroVolta > controleJogo.totalVoltasCorrida()) {
				numeroVolta = controleJogo.totalVoltasCorrida();
				return;
			}
		}
		verificaIrBox(controleJogo);
		this.setNoAtual((No) pista.get(index));
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
				ultsConsumosCombustivel
						.add(ultimoConsumoCombust.intValue() - pCombust);
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

	private void processaAsfaltoAbrasivoIA(InterfaceJogo controleJogo) {
		if (isJogadorHumano() || isAsfaltoAbrasivo()) {
			return;
		}
		boolean testeAbrasivo = testeHabilidadePiloto()
				&& controleJogo.asfaltoAbrasivo();
		if (testeAbrasivo && !isAsfaltoAbrasivo() && getPosicao() == 1) {
			controleJogo
					.infoPrioritaria(Html.orange(Lang.msg("asfaltoAbrasivo")));
		}
		this.setAsfaltoAbrasivo(testeAbrasivo);
	}

	private void verificaIrBox(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (isJogadorHumano() || isRecebeuBanderada() || getPtosPista() < 0) {
			return;
		}
		int pneus = getCarro().getPorcentagemDesgastePneus();
		int combust = getCarro().getPorcentagemCombustivel();
		int corrida = controleJogo.porcentagemCorridaConcluida();
		if (controleJogo.isSemReabastacimento()) {
			combust = 100;
		}

		if ((combust < 5) && !controleJogo.isCorridaTerminada()) {
			box = true;
		}

		if (controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isSafetyCarVaiBox()) {
			if (combust < 20 || pneus < 50) {
				box = true;
			}

		}
		boolean boxPneus = false;
		if (Carro.TIPO_PNEU_MOLE.equals(getCarro().getTipoPneu())
				&& pneus < 20) {
			boxPneus = true;
		} else if (pneus < 15) {
			boxPneus = true;
		}

		if (boxPneus) {
			if (voltas.size() > 3) {
				Volta voltaUltima = voltas.get(voltas.size() - 1);
				Volta voltaPenultima = voltas.get(voltas.size() - 2);
				Volta voltaAntiPenultima = voltas.get(voltas.size() - 3);
				if (voltaUltima.getTempoNumero() > voltaPenultima
						.getTempoNumero()
						&& voltaUltima.getTempoNumero() > voltaAntiPenultima
								.getTempoNumero()) {
					box = true;
				}
			}
			if (colisao == null && noAtual.verificaCurvaBaixa()
					&& (ganho < (.9 * maxGanhoBaixa))) {
				box = true;
			}
			if (colisao == null && noAtual.verificaCurvaAlta()
					&& (ganho < (.9 * maxGanhoAlta))) {
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

		if (controleJogo.isSemReabastacimento()
				&& !carro.verificaPneusIncompativeisClima(controleJogo)
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

	protected static final SimpleDateFormat formatter = new SimpleDateFormat(
			"dd/MM/yyyy hh:mm:ss");

	public int getNovoModificador() {
		return novoModificador;
	}
	public int getNovoModificadorCarro() {
		return novoModificadorCarro;
	}
	public int getNovoModificadorBruto() {
		return novoModificadorBruto;
	}
	private int processaNovoIndex(InterfaceJogo controleJogo) {
		int index = getNoAtual().getIndex();
		/**
		 * Devagarinho qdo a corrida termina
		 */
		if ((controleJogo.isCorridaTerminada() && isRecebeuBanderada())) {
			double novoModificador = (controleJogo.getCircuito()
					.getMultiplciador()) * 2;
			index += novoModificador;
			setPtosPista(Util.inteiro(novoModificador + getPtosPista()));
			setVelocidade(Util.intervalo(50, 65));
			return index;
		}
		if (desqualificado) {
			return getNoAtual().getIndex();
		}
		novoModificador = calcularModificador(controleJogo);
		novoModificadorBruto = novoModificador;
		novoModificador = getCarro().calcularModificadorCarro(novoModificador,
				agressivo, getNoAtual(), controleJogo);
		novoModificadorCarro = novoModificador;
		processaLimitadorModificador();
		calculaCarrosAdjacentes(controleJogo);
		processaStress(controleJogo);
		processaUsoERS(controleJogo);
		processaUsoDRS(controleJogo);
		processaMudancaRegime(controleJogo);
		processaGanho(controleJogo);
		processaEscapadaDaPista(controleJogo);
		processaIAnovoIndex(controleJogo);
		processaTurbulencia(controleJogo);
		processaFaiscas(controleJogo);
		processaGanhoDanificado();
		processaFreioNaReta(controleJogo);
		processaEvitaBaterCarroFrente(controleJogo);
		processaMudarTracado(controleJogo);
		processaColisao(controleJogo);
		controleJogo.verificaAcidente(this);
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
		setPtosPista(Util.inteiro(getPtosPista() + ganho));
		index += Math.round(ganho);
		setVelocidade(calculoVelocidade(ganho));
		return index;
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
					|| volta.getTempoNumero() == 0) {
				continue;
			}
			ultimas5Voltas
					.add(numeroVolta + " - " + volta.getTempoVoltaFormatado());
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
		int durabilidade = controleJogo.getDurabilidadeAreofolio() / 2;
		if (getCarro().getDurabilidadeAereofolio() <= durabilidade) {
			setAlertaAerefolio(true);
		}

	}

	public void processaAlertaMotor(InterfaceJogo controleJogo) {
		setAlertaMotor(false);
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (getCarro().getPorcentagemDesgasteMotor() <= 25
				|| getCarro().verificaMotorSuperAquecido()) {
			setAlertaMotor(true);
		}

	}

	public void processaFaiscas(InterfaceJogo controleJogo) {
		setFaiscas(false);
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (getPtosBox() != 0) {
			return;
		}

		double mod = .995;

		if (isFreiandoReta() && getCarro().getPorcentagemCombustivel() > Util
				.intervalo(40, 50)) {
			mod -= .50;
			if (getTracado() != 0) {
				mod -= .50;
			}
		}
		if (getCarro().getGiro() == Carro.GIRO_MAX_VAL && getNoAtual() != null
				&& getNoAtual().verificaRetaOuLargada()
				&& Clima.SOL.equals(controleJogo.getClima())
				&& getVelocidade() != 0 && Math.random() > mod) {
			setFaiscas(true);
		}

	}

	public void calculaCarrosAdjacentes(InterfaceJogo controleJogo) {
		calculaDiffParaProximoRetardatario = controleJogo
				.calculaDiffParaProximoRetardatario(this, false);
		calculaDiffParaProximoRetardatarioMesmoTracado = controleJogo
				.calculaDiffParaProximoRetardatario(this, true);
		calculaDiferencaParaAnterior = controleJogo
				.calculaDiferencaParaAnterior(this);
		carroPilotoDaFrenteRetardatario = controleJogo
				.obterCarroNaFrenteRetardatario(this, false);
		calculaDiferencaParaProximo = controleJogo
				.calculaDiferencaParaProximo(this);
		carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
		carroPilotoAtras = controleJogo.obterCarroAtras(this);
		if (getPosicao() > 1) {
			calculaSegundosParaProximo = controleJogo
					.calculaSegundosParaProximo(this);
		}
		if (carroPilotoAtras != null && carroPilotoAtras.getPiloto() != null
				&& getPosicao() < controleJogo.getPilotos().size()) {
			calculaSegundosParaAnterior = controleJogo
					.calculaSegundosParaProximo(carroPilotoAtras.getPiloto(),
							carroPilotoAtras.getPiloto()
									.getDiferencaParaProximo());
		}
	}

	private void processaGanhoSafetyCar(InterfaceJogo controleJogo) {
		if (!controleJogo.isSafetyCarNaPista()) {
			return;
		}
		ganho = controleJogo.ganhoComSafetyCar(ganho, controleJogo, this);
	}

	public static void main(String[] args) {

	}

	private int calculoVelocidade(double ganho) {
		int val = 290;
		double porcent = getCarro().getPorcentagemCombustivel() / 100.0;
		val += (21 - (porcent / 5.0));
		boolean naReta = false;
		if (noAtual != null && !freiandoReta
				&& (acelerando || ativarDRS || ativarErs)) {
			naReta = noAtual.verificaRetaOuLargada();
		}
		return Util.inteiro(((val * ganho * ((naReta) ? 1 : 0.7) / ganhoMax)
				+ ganho * ((naReta) ? 1 : 0.7)));
	}

	public void processaColisao(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			setColisao(null);
			return;
		}
		boolean verificaNoPitLane = controleJogo.verificaNoPitLane(this);
		if (verificaNoPitLane) {
			setColisao(null);
			return;
		}
		centralizaCarroColisao(controleJogo);
		List<Piloto> pilotos = controleJogo.getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoFrente = (Piloto) iterator.next();
			if (pilotoFrente.equals(this)) {
				continue;
			}
			if (verificaNaoPrecisaDesviar(controleJogo, pilotoFrente)) {
				continue;
			}
			if (this.equals(pilotoFrente.getColisao())) {
				continue;
			}
			pilotoFrente.centralizaCarroColisao(controleJogo);
			colisaoDiantera = getDiateiraColisao()
					.intersects(pilotoFrente.getTrazeiraColisao())
					|| getDiateiraColisao()
							.intersects(pilotoFrente.getCentroColisao());
			colisaoCentro = getCentroColisao()
					.intersects(pilotoFrente.getTrazeiraColisao());
			colisao = (colisaoDiantera || colisaoCentro) ? pilotoFrente : null;
			if (colisao != null) {
				return;
			}
		}
		setColisao(null);
	}

	public void processaPenalidadeColisao(InterfaceJogo controleJogo) {
		if (getColisao() == null) {
			return;
		}
		acelerando = false;
		setAgressivo(false, controleJogo);
		incStress(10);
		if (evitaBaterCarroFrente) {
			incStress(5);
		}
		Piloto pilotoFrente = getColisao();
		pilotoFrente.setCiclosDesconcentrado(0);
	}

	public void processaEscapadaDaPista(InterfaceJogo controleJogo) {
		if (controleJogo.isSafetyCarNaPista()) {
			return;
		}
		/**
		 * Escapa para os tracados 1 ou 2
		 */
		if (No.CURVA_BAIXA.equals(getNoAtual().getTipo()) && agressivo
				&& (getTracado() == 0)
				&& (carro.getPorcentagemDesgastePneus() < 30)) {
			if (getStress() > 60)
				controleJogo.travouRodas(this);
			if (getTracadoAntigo() != 0) {
				if (getTracadoAntigo() == 1) {
					mudarTracado(2, controleJogo);
				} else {
					mudarTracado(1, controleJogo);
				}
			} else {
				mudarTracado(Util.intervalo(1, 2), controleJogo);
			}
			return;
		}

		/**
		 * Escapa para os tracados 4 ou 5
		 */
		if (getStress() > getValorLimiteStressePararErrarCurva(controleJogo)
				&& !controleJogo.isSafetyCarNaPista()
				&& AGRESSIVO.equals(modoPilotagem)
				&& !testeHabilidadePilotoCarro() && getPtosBox() == 0) {
			if (escapaTracado(controleJogo) && !verificaDesconcentrado()) {
				setCiclosDesconcentrado(Util.intervalo(50, 150));
				if (controleJogo.verificaInfoRelevante(this))
					controleJogo.info(Lang.msg("saiDaPista",
							new String[]{Html.superRed(getNome())}));
			}
		}
		/**
		 * Volta a pista apos escapada
		 */
		if (getTracado() == 4 || getTracado() == 5) {
			setModoPilotagem(LENTO);
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
			if (getIndiceTracado() <= 0) {
				int mudarTracado = 0;
				if (getTracado() == 4) {
					No no = controleJogo.getCircuito().getPista4Full()
							.get(getNoAtual().getIndex());
					if (no == null || no.getTracado() != 4) {
						mudarTracado = 2;
						mudarTracado(mudarTracado, controleJogo, true);
					}
				}
				if (getTracado() == 5) {
					No no = controleJogo.getCircuito().getPista5Full()
							.get(getNoAtual().getIndex());
					if (no == null || no.getTracado() != 5) {
						mudarTracado = 1;
						mudarTracado(mudarTracado, controleJogo, true);
					}
				}
				if (carroPilotoAtras != null
						&& carroPilotoAtras.getPiloto()
								.getTracado() == mudarTracado
						&& carroPilotoAtras.getPiloto().getColisao() != null) {
					ganho *= 1.2;
				} else {
					ganho *= .5;
				}
			} else {
				setModoPilotagem(LENTO);
				getCarro().setGiro(Carro.GIRO_MIN_VAL);
				controleJogo.travouRodas(this);
			}
		}
	}

	private void processaGanho(InterfaceJogo controleJogo) {
		ganho = ((novoModificador
				* controleJogo.getCircuito().getMultiplciador())
				* (controleJogo.getIndexVelcidadeDaPista()));
		if (verificaForaPista(this)) {
			ganho *= controleJogo.getFatorUtrapassagem();
		}
	}

	public double getValorLimiteStressePararErrarCurva(
			InterfaceJogo controleJogo) {
		if (isJogadorHumano()) {
			return 100 * (1.3 - controleJogo.getNiveljogo());
		} else {
			return 100 * (controleJogo.getNiveljogo() + 0.2);
		}
	}

	private void processaLimitadorModificador() {
		if (novoModificador > 5) {
			novoModificador = 5;
		} else if (novoModificador < 1) {
			novoModificador = 1;
		}
		if (novoModificador - ultModificador > 1) {
			novoModificador = ultModificador + 1;
		}
		if (ultModificador - novoModificador > 1) {
			novoModificador = ultModificador - 1;
		}
		if (noAtual.verificaRetaOuLargada() && novoModificador < 3) {
			novoModificador = 3;
		}
		ultModificador = novoModificador;
	}

	private void processaGanhoDanificado() {
		if (danificado()) {
			if (getNoAtual().verificaCurvaBaixa()
					&& (Carro.PNEU_FURADO.equals(getCarro().getDanificado())
							|| (Carro.PERDEU_AEREOFOLIO
									.equals(getCarro().getDanificado())))) {
				if (ganho > 10)
					ganho = 10;
			}
			if (getNoAtual().verificaCurvaAlta()
					&& (Carro.PNEU_FURADO.equals(getCarro().getDanificado())
							|| (Carro.PERDEU_AEREOFOLIO
									.equals(getCarro().getDanificado())))) {
				if (ganho > 15)
					ganho = 15;

			}
			if (Carro.PNEU_FURADO.equals(getCarro().getDanificado())
					&& getNoAtual().verificaRetaOuLargada()) {
				if (ganho > 20)
					ganho = 20;
			}
		}
	}

	/**
	 * Controla Efeito turbulencia e ultrapassagens usando tracado
	 */
	private void processaTurbulencia(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (carroPilotoDaFrenteRetardatario == null) {
			return;
		}
		if (carroPilotoDaFrenteRetardatario.getPiloto().isDesqualificado()) {
			return;
		}
		if ((carroPilotoDaFrenteRetardatario.getPiloto()
				.getTracado() == getTracado())) {
			return;
		}
		double diff = calculaDiferencaParaProximo;
		double multiplicadoGanhoTurbulencia = (controleJogo
				.getFatorUtrapassagem());
		double distLimiteTurbulencia = 50.0 / multiplicadoGanhoTurbulencia;
		if (diff < distLimiteTurbulencia && !verificaForaPista(
				carroPilotoDaFrenteRetardatario.getPiloto())) {
			if (getTracado() != carroPilotoDaFrenteRetardatario.getPiloto()
					.getTracado()) {
				if (getNoAtual().verificaRetaOuLargada()) {
					multiplicadoGanhoTurbulencia += (getCarro().testePotencia()
							&& getCarro().testeAerodinamica()) ? 0.1 : -0.1;
				} else {
					multiplicadoGanhoTurbulencia += (testeHabilidadePilotoAerodinamicaFreios(
							controleJogo) ? 0.1 : -0.1);
				}
			}
			if (multiplicadoGanhoTurbulencia > 1) {
				multiplicadoGanhoTurbulencia = 1;
			} else if (multiplicadoGanhoTurbulencia < 0.01) {
				multiplicadoGanhoTurbulencia = 0.01;
			}
			ganho *= (multiplicadoGanhoTurbulencia);
		}
	}

	private boolean verificaForaPista(Piloto piloto) {
		boolean voltando = false;
		if (getIndiceTracado() > 0 && (piloto.getTracadoAntigo() == 4
				|| piloto.getTracadoAntigo() == 5)) {
			voltando = true;
		}
		return piloto.getTracado() == 4 || piloto.getTracado() == 5 || voltando;
	}

	private void processaFreioNaReta(InterfaceJogo controleJogo) {
		boolean testPilotoPneus = getCarro().testeFreios();
		/**
		 * efeito freiar na reta
		 */
		No obterProxCurva = controleJogo.obterProxCurva(getNoAtual());
		if (obterProxCurva != null && obterProxCurva.verificaCurvaBaixa()) {
			int indexProxCurva = obterProxCurva.getIndex();
			if (indexProxCurva < getNoAtual().getIndex()) {
				indexProxCurva += controleJogo.getNosDaPista().size();
			}
			double val = indexProxCurva - getNoAtual().getIndex();
			double distAfrente = 300.0;
			if (val < distAfrente && getNoAtual().verificaRetaOuLargada()) {
				freiandoReta = true;
				acelerando = false;
				double multi = (val / distAfrente);

				if (testPilotoPneus) {
					retardaFreiandoReta = true;
				}

				if (!retardaFreiandoReta
						&& Piloto.AGRESSIVO.equals(getModoPilotagem())) {
					retardaFreiandoReta = true;
				}

				double minMulti = 0.7;
				if (controleJogo.isChovendo()) {
					minMulti -= 0.3;
					retardaFreiandoReta = false;
				}
				if (calculaDiffParaProximoRetardatario < 50) {
					minMulti -= Util.intervalo(0.05, 0.15);
					retardaFreiandoReta = false;
				} else if (calculaDiffParaProximoRetardatarioMesmoTracado < 100) {
					minMulti -= 0.1;
					retardaFreiandoReta = false;
				} else if (calculaDiffParaProximoRetardatarioMesmoTracado < 150) {
					minMulti -= Util.intervalo(0.05, 0.1);
					retardaFreiandoReta = false;
				}
				if (retardaFreiandoReta) {
					if (getStress() > 90
							&& Piloto.AGRESSIVO.equals(getModoPilotagem())) {
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
		} else {
			freiandoReta = false;
		}

		if (getNoAtual().verificaCurvaBaixa() && retardaFreiandoReta) {
			if (getPosicao() <= 3 && Math.random() > 0.9
					&& !testeHabilidadePilotoFreios(controleJogo)) {
				incStress(Util.intervalo(5, 10));
				agressivo = false;
				if (controleJogo.verificaInfoRelevante(this)
						&& Math.random() > 0.7)
					controleJogo.info(Lang.msg("014",
							new String[]{Html.bold(getNome())}));
			}
			retardaFreiandoReta = false;
		}
	}

	private void processaLimitadorGanho(InterfaceJogo controleJogo) {
		limiteGanho = false;
		if (verificaForaPista(this)) {
			return;
		}
		if (getColisao() != null) {
			acelerando = false;
			if (ganho > 5) {
				ganho = 5;
			}
			return;
		}
		if (evitaBaterCarroFrente) {
			ganho *= (calculaDiffParaProximoRetardatarioMesmoTracado
					/ limiteEvitarBatrCarroFrente);
			return;
		}
		if (ganho > 0.0 && ganho < 1.0) {
			ganho = Math.random() > 0.5 ? 1 : 0;
		}
		if (ganho > 70) {
			limiteGanho = true;
			ganho = 70;
		}
		double emborrachamento = controleJogo.porcentagemCorridaConcluida()
				/ 100.0;
		if (getNoAtual().verificaCurvaBaixa()) {
			double limite = 25;
			if (Math.random() < emborrachamento) {
				limite = 35;
			}
			if (ganho > limite) {
				limiteGanho = true;
				ganho = limite;
			}
			ganhosBaixa.add(ganho);
			if (ganho > maxGanhoBaixa) {
				maxGanhoBaixa = ganho;
			}
		}
		if (getNoAtual().verificaCurvaAlta()) {
			double limite = 30;
			if (Math.random() < emborrachamento) {
				limite = 40;
			}
			if (ganho > limite) {
				limiteGanho = true;
				ganho = limite;
			}
			ganhosAlta.add(ganho);
			if (ganho > maxGanhoAlta) {
				maxGanhoAlta = ganho;
			}
		}
		if (getNoAtual().verificaRetaOuLargada()) {
			ganhosReta.add(ganho);
		}
		if (ganho > ganhoMax) {
			ganhoMax = ganho;
		}
		if (acelerando) {
			if (ganho < ultGanhoReta) {
				ganho = ultGanhoReta;
			} else {
				ultGanhoReta = ganho;
			}
		} else {
			ultGanhoReta = 0;
		}
	}

	public boolean isLimiteGanho() {
		return limiteGanho;
	}

	private void processaUsoERS(InterfaceJogo controleJogo) {
		if (controleJogo.isErs() && ativarErs && getPtosBox() == 0) {
			if (getCarro().getCargaErs() <= 0) {
				ativarErs = false;
			} else {
				double rev = (1000 - carro.getPotencia()) / 10000;
				ganho *= Util.intervalo(1.05, 1.1 + (rev * 2));
				getCarro().usaKers();
			}
		}
	}

	private void processaUsoDRS(InterfaceJogo controleJogo) {
		if (!verificaPodeUsarDRS(controleJogo)) {
			podeUsarDRS = false;
			if (controleJogo.isDrs()) {
				ativarDRS = false;
				getCarro().setAsa(Carro.MAIS_ASA);
			}
			return;
		}
		podeUsarDRS = true;
		if (ativarDRS) {
			getCarro().setAsa(Carro.MENOS_ASA);
			if (!getNoAtual().verificaRetaOuLargada()
					&& testeHabilidadePiloto()) {
				ativarDRS = false;
			}
		} else if (controleJogo.isDrs()) {
			ativarDRS = false;
			getCarro().setAsa(Carro.MAIS_ASA);
		}
	}

	public void setPodeUsarDRS(boolean podeUsarDRS) {
		this.podeUsarDRS = podeUsarDRS;
	}

	private boolean verificaPodeUsarDRS(InterfaceJogo controleJogo) {
		if (controleJogo.isDrs() && getPtosBox() == 0 && getNumeroVolta() > 1
				&& !controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isChovendo()
				&& getNoAtual().verificaRetaOuLargada()
				&& carroPilotoDaFrenteRetardatario != null
				&& calculaDiffParaProximoRetardatario < Constantes.LIMITE_DRS) {
			No obterCurvaAnterior = controleJogo
					.obterCurvaAnterior(getNoAtual());
			No obterProxCurva = controleJogo.obterProxCurva(getNoAtual());
			if (obterCurvaAnterior == null || obterProxCurva == null) {
				return false;
			}
			int indexProxCurva = obterProxCurva.getIndex();
			int indexCurvaAnterior = obterCurvaAnterior.getIndex();
			if (indexProxCurva < indexCurvaAnterior) {
				indexProxCurva += controleJogo.getNosDaPista().size();
			}
			if ((indexProxCurva
					- indexCurvaAnterior) >= Constantes.TAMANHO_RETA_DRS) {
				return true;
			}
		}
		return false;
	}

	private void processaStress(InterfaceJogo controleJogo) {
		int fatorStresse = Util.intervalo(1,
				(int) controleJogo.getNiveljogo() * 10);
		if (getNoAtual().verificaCurvaAlta()
				|| getNoAtual().verificaCurvaBaixa()) {
			fatorStresse /= 2;
		}
		if (NORMAL.equals(getModoPilotagem()) || !agressivo) {
			decStress(fatorStresse);
		} else if (LENTO.equals(getModoPilotagem())) {
			decStress(fatorStresse * (testeHabilidadePiloto() ? 2 : 1));
		}
	}

	private void processaIAnovoIndex(InterfaceJogo controleJogo) {
		if (colisao != null) {
			agressivo = false;
			return;
		}
		if (controleJogo.isModoQualify() || isJogadorHumano()
				|| verificaDesconcentrado()) {
			return;
		}
		if (controleJogo.isSafetyCarNaPista() || getPtosBox() != 0
				|| danificado()) {
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
			setModoPilotagem(LENTO);
			return;
		}
		if (controleJogo.isErs()) {
			tentaUsarErs(controleJogo);
		}
		if (controleJogo.isDrs()) {
			tentaUsarDRS(controleJogo);
		}
		boolean tentaPassarFrete = tentarPassaPilotoDaFrente(controleJogo);
		boolean tentarEscaparAtras = false;
		if (!tentaPassarFrete) {
			tentarEscaparAtras = tentarEscaparPilotoAtras(controleJogo,
					tentaPassarFrete);
		}
		if (Math.random() > 0.95 && controleJogo.verificaInfoRelevante(this)) {
			if (tentaPassarFrete) {
				String txt = Lang.msg("tentaPassarFrete", new String[]{
						Html.bold(getNome()),
						Html.bold(carroPilotoDaFrente.getPiloto().getNome())});
				controleJogo.info(Html.black(txt));
			}
			if (tentarEscaparAtras) {
				String txt = Lang.msg("tentarEscaparAtras", new String[]{
						Html.bold(getNome()),
						Html.bold(carroPilotoAtras.getPiloto().getNome())});
				controleJogo.info(Html.black(txt));
			}
		}
		if (!tentaPassarFrete && !tentarEscaparAtras) {
			getCarro().setGiro(Carro.GIRO_NOR_VAL);
			setModoPilotagem(NORMAL);
		}

		if (limiteGanho && controleJogo.verificaNivelJogo()
				&& testeHabilidadePilotoCarro()) {
			getCarro().setGiro(Carro.GIRO_NOR_VAL);
			setModoPilotagem(NORMAL);
		}
		if (ativarDRS) {
			getCarro().setGiro(Carro.GIRO_MAX_VAL);
		}
		if (getCarro().verificaCondicoesCautelaGiro(controleJogo)
				|| entrouNoBox()) {
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
		}
		if (getCarro().verificaCondicoesCautelaPneu(controleJogo)
				|| entrouNoBox()) {
			setModoPilotagem(LENTO);
		}
	}

	private void processaMudarTracado(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (!isAutoPos()) {
			return;
		}
		if (!noAtual.verificaRetaOuLargada()) {
			mudouTracadoReta = 0;
		}
		if ((evitaBaterCarroFrente && carroPilotoDaFrenteRetardatario != null
				&& getTracado() == carroPilotoDaFrenteRetardatario.getPiloto()
						.getTracado())
				|| getDiferencaParaProximo() < (testeHabilidadePiloto()
						? 100
						: 150)) {
			controleJogo.fazPilotoMudarTracado(this,
					carroPilotoDaFrenteRetardatario.getPiloto());
		} else if (!isJogadorHumano() && testeHabilidadePiloto()
				&& pontoEscape != null
				&& calculaDiffParaProximoRetardatario > 150
				&& distanciaEscape < ((2 * controleJogo.getNiveljogo())
						* Carro.RAIO_DERRAPAGEM)) {
			if (getTracado() != 0) {
				mudarTracado(0, controleJogo);
			} else {
				int ladoEscape = controleJogo.obterLadoEscape(pontoEscape);
				if (ladoEscape == 5) {
					mudarTracado(2, controleJogo);
				}
				if (ladoEscape == 4) {
					mudarTracado(1, controleJogo);
				}
			}
		} else if ((calculaDiffParaProximoRetardatarioMesmoTracado < calculaDiferencaParaAnterior
				&& calculaDiffParaProximoRetardatarioMesmoTracado < (testeHabilidadePilotoCarro()
						? 150
						: 200))) {
			controleJogo.fazPilotoMudarTracado(this,
					carroPilotoDaFrenteRetardatario.getPiloto());
		} else if (!isJogadorHumano() && carroPilotoAtras != null
				&& mudouTracadoReta <= 1 && calculaDiferencaParaAnterior < 150
				&& carroPilotoAtras.getPiloto().getTracado() != getTracado()
				&& calculaDiferencaParaAnterior > 100
				&& carroPilotoAtras.getPiloto().getPtosBox() == 0
				&& testeHabilidadePiloto() && !isFreiandoReta()
				&& controleJogo.getNiveljogo() < Math.random()) {
			if (mudarTracado(carroPilotoAtras.getPiloto().getTracado(),
					controleJogo) && noAtual.verificaRetaOuLargada()) {
				mudouTracadoReta++;
			}
		} else if (!isJogadorHumano() && calculaDiferencaParaAnterior > 250
				&& calculaDiffParaProximoRetardatario > 250) {
			mudarTracado(0, controleJogo);
		}
	}

	private boolean tentarEscaparPilotoAtras(InterfaceJogo controleJogo,
			boolean tentaPassarFrete) {
		if (tentaPassarFrete) {
			return false;
		}
		if (Math.random() > (controleJogo.getNiveljogo() + 0.3)) {
			return false;
		}
		if (calculaDiferencaParaAnterior < (controleJogo.isDrs() ? 600 : 300)
				&& testeHabilidadePilotoCarro()) {
			modoIADefesaAtaque(controleJogo);
			return true;
		}
		return false;
	}

	private void tentaUsarDRS(InterfaceJogo controleJogo) {
		if (verificaDesconcentrado()) {
			return;
		}
		if (controleJogo.isChovendo()) {
			return;
		}
		if (controleJogo.getNumVoltaAtual() <= 1) {
			return;
		}
		if (ativarDRS) {
			return;
		}
		if (getNoAtual().verificaRetaOuLargada()
				&& Math.random() < (controleJogo.getNiveljogo())
				&& testeHabilidadePiloto()) {
			ativarDRS = true;
		} else {
			ativarDRS = false;
		}
	}

	private void tentaUsarErs(InterfaceJogo controleJogo) {
		if (verificaDesconcentrado()) {
			return;
		}
		if (noAtual == null) {
			return;
		}
		int percetagemDeVoltaConcluida = controleJogo
				.percetagemDeVoltaConcluida(this);
		if (percetagemDeVoltaConcluida > 50
				&& noAtual.verificaRetaOuLargada()) {
			ativarErs = true;
		}
		if (percetagemDeVoltaConcluida > 70) {
			ativarErs = true;
		}
	}

	public boolean verificaNaoPrecisaDesviar(InterfaceJogo controleJogo,
			Piloto pilotoFrente) {
		boolean naoPrecisa = false;
		if (controleJogo.isSafetyCarNaPista()) {
			if (pilotoFrente.getCarro().isRecolhido()) {
				naoPrecisa = true;
			}
		} else {
			String danificado = pilotoFrente.getCarro().getDanificado();
			if (pilotoFrente.isDesqualificado()
					|| pilotoFrente.getCarro().isPaneSeca()
					|| Carro.ABANDONOU.equals(danificado)
					|| Carro.BATEU_FORTE.equals(danificado)
					|| Carro.PANE_SECA.equals(danificado)
					|| Carro.EXPLODIU_MOTOR.equals(danificado)) {
				naoPrecisa = true;
			}
		}
		return naoPrecisa;
	}

	private void processaEvitaBaterCarroFrente(InterfaceJogo controleJogo) {
		evitaBaterCarroFrente = false;
		if (controleJogo.isModoQualify()) {
			return;
		}
		if (carroPilotoDaFrenteRetardatario == null) {
			return;
		}
		Piloto piloto = carroPilotoDaFrenteRetardatario.getPiloto();
		if (this.equals(piloto)) {
			return;
		}
		if (verificaNaoPrecisaDesviar(controleJogo, piloto)) {
			return;
		}
		if (piloto.getPtosBox() > 0) {
			return;
		}
		limiteEvitarBatrCarroFrente = 150;
		if (getCarro().getDurabilidadeAereofolio() < controleJogo
				.getDurabilidadeAreofolio() / 2) {
			limiteEvitarBatrCarroFrente += 100;
		}
		if (piloto.getColisao() != null) {
			limiteEvitarBatrCarroFrente += 100;
		}
		if (piloto.getColisao() != null
				&& piloto.getTracado() == getTracado()) {
			limiteEvitarBatrCarroFrente += 100;
		}
		if (calculaDiffParaProximoRetardatarioMesmoTracado < limiteEvitarBatrCarroFrente) {
			evitaBaterCarroFrente = true;
		}
	}

	public boolean escapaTracado(InterfaceJogo controleJogo) {
		processaPontoEscape(controleJogo);
		/**
		 * Verificar na entrada da curva e nao na area de escape
		 */
		if (getTracado() == 4 || getTracado() == 5) {
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
		if (ladoEscape == 5 && getTracado() == 2) {
			return false;
		}
		if (ladoEscape == 4 && getTracado() == 1) {
			return false;
		}
		if ((ladoEscape == 4 || ladoEscape == 5) && getTracado() == 0) {
			return false;
		}
		mudarTracado(ladoEscape, controleJogo);
		return true;
	}

	@JsonIgnore
	public Point getPontoDerrapada() {
		return pontoEscape;
	}

	public void processaPontoEscape(InterfaceJogo controleJogo) {
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
		for (Iterator<PontoEscape> iterator = keySet.iterator(); iterator
				.hasNext();) {
			PontoEscape pontoEscapada = iterator.next();
			double distaciaEntrePontos = GeoUtil.distaciaEntrePontos(p,
					pontoEscapada.getPoint());
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

	public Rectangle2D centralizaCarroColisao(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return null;
		}
		if (getNoAnterior() != null && diateiraColisao != null
				&& centroColisao != null && trazeiraColisao != null
				&& !emMovimento()) {
			return null;
		}
		No noAtual = getNoAtual();
		int cont = noAtual.getIndex();
		List<No> lista = controleJogo.obterPista(noAtual);
		if (lista == null) {
			return null;
		}
		Point p = noAtual.getPoint();
		int carx = p.x;
		int cary = p.y;
		int traz = cont - MEIAENVERGADURA;
		int frente = cont + MEIAENVERGADURA;
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
		Rectangle2D rectangle = new Rectangle2D.Double(
				(p.x - Carro.MEIA_LARGURA_CIMA), (p.y - Carro.MEIA_ALTURA_CIMA),
				Carro.LARGURA_CIMA, Carro.ALTURA_CIMA);
		Point p1 = null;
		Point p2 = null;
		Point p4 = null;
		Point p5 = null;
		if (noAtual.isBox()) {
			p1 = controleJogo.getCircuito().getBox1Full()
					.get(noAtual.getIndex()).getPoint();
			p2 = controleJogo.getCircuito().getBox2Full()
					.get(noAtual.getIndex()).getPoint();
		} else {
			p1 = controleJogo.getCircuito().getPista1Full()
					.get(noAtual.getIndex()).getPoint();
			p2 = controleJogo.getCircuito().getPista2Full()
					.get(noAtual.getIndex()).getPoint();
			p5 = controleJogo.getCircuito().getPista5Full()
					.get(noAtual.getIndex()) != null
							? controleJogo.getCircuito().getPista5Full()
									.get(noAtual.getIndex()).getPoint()
							: p1;
			p4 = controleJogo.getCircuito().getPista4Full()
					.get(noAtual.getIndex()) != null
							? controleJogo.getCircuito().getPista4Full()
									.get(noAtual.getIndex()).getPoint()
							: p1;
			if (p4 == null) {
				p4 = p2;
			}
			if (p5 == null) {
				p5 = p1;
			}
		}
		if (getTracado() == 0) {
			carx = p.x;
			cary = p.y;
			int indTracado = getIndiceTracado();
			if (indTracado > 0 && getTracadoAntigo() != 0) {
				List drawBresenhamLine = null;
				if (getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p.x, p.y);
				}
				if (getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p.x, p.y);
				}
				if (getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
							p.x, p.y);
				}
				if (getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
							p.x, p.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}
		if (getTracado() == 1) {
			carx = Util.inteiro((p1.x));
			cary = Util.inteiro((p1.y));
			int indTracado = getIndiceTracado();
			if (indTracado > 0 && getTracadoAntigo() != 1) {
				List drawBresenhamLine = null;
				if (getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p1.x, p1.y);
				}
				if (getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p1.x, p1.y);
				}
				if (getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
							p1.x, p1.y);
				}
				if (getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
							p1.x, p1.y);
				}

				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (getTracado() == 5) {
			carx = Util.inteiro((p5.x));
			cary = Util.inteiro((p5.y));
			int indTracado = getIndiceTracado();
			if (indTracado > 0 && getTracadoAntigo() != 5) {
				List drawBresenhamLine = null;
				if (getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p5.x, p5.y);
				}
				if (getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p5.x, p5.y);
				}
				if (getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p5.x, p5.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (getTracado() == 2) {
			carx = Util.inteiro((p2.x));
			cary = Util.inteiro((p2.y));
			int indTracado = getIndiceTracado();
			if (indTracado > 0 && getTracadoAntigo() != 2) {
				List drawBresenhamLine = null;
				if (getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p2.x, p2.y);
				}
				if (getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p2.x, p2.y);
				}

				if (getTracadoAntigo() == 4) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p4.x, p4.y,
							p2.x, p2.y);
				}
				if (getTracadoAntigo() == 5) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p5.x, p5.y,
							p2.x, p2.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		if (getTracado() == 4) {
			carx = Util.inteiro((p4.x));
			cary = Util.inteiro((p4.y));
			int indTracado = getIndiceTracado();
			if (indTracado > 0 && getTracadoAntigo() != 4) {
				List drawBresenhamLine = null;
				if (getTracadoAntigo() == 0) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p.x, p.y,
							p4.x, p4.y);
				}
				if (getTracadoAntigo() == 1) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p1.x, p1.y,
							p4.x, p4.y);
				}
				if (getTracadoAntigo() == 2) {
					drawBresenhamLine = GeoUtil.drawBresenhamLine(p2.x, p2.y,
							p4.x, p4.y);
				}
				int indice = drawBresenhamLine.size() - indTracado;
				if (indice <= 0) {
					indice = 0;
				}
				if (indice >= drawBresenhamLine.size()) {
					indice = drawBresenhamLine.size() - 1;
				}

				Point pReta = (Point) drawBresenhamLine.get(indice);
				carx = pReta.x;
				cary = pReta.y;
			}
		}

		rectangle = new Rectangle2D.Double(
				(carx - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(cary - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);

		centroColisao = rectangle.getBounds();

		trazCar = GeoUtil.calculaPonto(calculaAngulo + 90,
				Util.inteiro(MEIAENVERGADURA), new Point(carx, cary));

		Rectangle2D trazRec = new Rectangle2D.Double(
				(trazCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(trazCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);
		trazeiraColisao = trazRec.getBounds();

		frenteCar = GeoUtil.calculaPonto(calculaAngulo + 270,
				Util.inteiro(MEIAENVERGADURA), new Point(carx, cary));

		Rectangle2D frenteRec = new Rectangle2D.Double(
				(frenteCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(frenteCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO,
				Carro.ALTURA * Carro.FATOR_AREA_CARRO);
		diateiraColisao = frenteRec.getBounds();

		return rectangle;
	}

	public List obterPista(InterfaceJogo controleJogo) {
		return controleJogo.obterPista(this.getNoAtual());
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
		for (Iterator iterator = listGanho.iterator(); iterator.hasNext();) {
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
		Piloto pilotoFrente = carroPilotoDaFrente.getPiloto();
		if (pilotoFrente.getPtosBox() != 0) {
			return false;
		}
		if (Math.random() > (controleJogo.getNiveljogo() + 0.3)) {
			return false;
		}
		int size = controleJogo.getCircuito().getPistaFull().size();
		int distBrigaMax = (int) (size * (controleJogo.getNiveljogo() + 0.2));
		if (calculaDiferencaParaProximo < distBrigaMax
				&& testeHabilidadePilotoCarro()) {
			modoIADefesaAtaque(controleJogo);
			return true;
		}
		return false;
	}

	private void modoIADefesaAtaque(InterfaceJogo controleJogo) {
		double porcentagemCombustivel = getCarro().getPorcentagemCombustivel();
		double porcentagemDesgastePneus = getCarro()
				.getPorcentagemDesgastePneus();
		boolean superAquecido = getCarro().verificaMotorSuperAquecido();
		boolean drsAtivado = Carro.MENOS_ASA.equals(getCarro().getAsa())
				&& controleJogo.isDrs() && !controleJogo.isChovendo();
		int porcentagemMotor = getCarro().getPorcentagemDesgasteMotor();
		int porcentagemCorridaRestante = 100
				- controleJogo.porcentagemCorridaConcluida();
		int limiteDesgaste = 10;
		boolean temMotor = porcentagemMotor > limiteDesgaste
				? Math.random() < porcentagemMotor / 100.0
				: porcentagemMotor > porcentagemCorridaRestante;
		boolean temCombustivel = porcentagemCombustivel > 10;
		if (controleJogo.isSemReabastacimento()) {
			temCombustivel = porcentagemCombustivel > limiteDesgaste
					? Math.random() < porcentagemCombustivel / 100.0
					: porcentagemCombustivel > porcentagemCorridaRestante;
		}
		boolean temPneu = porcentagemDesgastePneus > 10;
		if (controleJogo.isSemTrocaPneu()) {
			temPneu = porcentagemDesgastePneus > limiteDesgaste
					? Math.random() < porcentagemDesgastePneus / 100.0
					: porcentagemDesgastePneus > porcentagemCorridaRestante;
		}
		double valorLimiteStressePararErrarCurva = 100;
		boolean derrapa = getNoAtual() != null
				&& indexRefEscape > getNoAtual().getIndex();
		if (derrapa && testeHabilidadePiloto()) {
			valorLimiteStressePararErrarCurva = getValorLimiteStressePararErrarCurva(
					controleJogo);
		}
		boolean maxGiro = false;
		if (!superAquecido && temMotor && temCombustivel
				&& (calculaDiferencaParaAnterior < (controleJogo.isDrs()
						? 800
						: 400) || calculaDiffParaProximoRetardatario < 400
						|| drsAtivado)) {
			maxGiro = true;
		}
		boolean maxPilotagem = false;
		if (getNoAtual().verificaRetaOuLargada()) {
			maxPilotagem = temCombustivel && temPneu
					&& stress < valorLimiteStressePararErrarCurva
					&& !getCarro().isPneuAquecido();
		} else {
			maxPilotagem = temPneu
					&& stress < valorLimiteStressePararErrarCurva;
		}

		if (maxGiro) {
			getCarro().setGiro(Carro.GIRO_MAX_VAL);
		}

		No no = getNoAtual();
		if (no.verificaRetaOuLargada()) {
			setAtivarErs(true);
		}

		if (pontoEscape != null && distanciaEscape > Carro.RAIO_DERRAPAGEM) {
			valorLimiteStressePararErrarCurva = 100;
		}
		if (maxPilotagem) {
			setModoPilotagem(AGRESSIVO);
		}
	}

	private void processaMudancaRegime(InterfaceJogo controleJogo) {
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
		controleJogo.info(Html.superRed(getNome() + Lang.msg("057")));
	}

	private void mensangesModoAgressivo(InterfaceJogo controleJogo) {
		if (!controleJogo.verificaInfoRelevante(this)) {
			return;
		}
		if (Math.random() < 0.95) {
			return;
		}
		if (AGRESSIVO.equals(getModoPilotagem())) {
			if (controleJogo.isChovendo() && Math.random() > 0.970) {
				controleJogo.info(
						Html.bold(getNome()) + Html.bold(Lang.msg("052")));
			} else if (Math.random() > 0.97
					&& getNoAtual().verificaCurvaBaixa()) {

				if (Math.random() > 0.95) {
					controleJogo.info(Html.txtRedBold(getNome())
							+ Html.bold(Lang.msg("053")));
				} else {
					controleJogo.info(Html.txtRedBold(getNome())
							+ Html.bold(Lang.msg("054")));
				}
			}
		} else {
			if (Math.random() > 0.999) {
				if (controleJogo.isChovendo()) {
					controleJogo.info(
							Html.bold(getNome()) + Html.red(Lang.msg("055")));
				} else {
					controleJogo.info(
							Html.bold(getNome()) + Html.red(Lang.msg("056")));
				}
			}
		}
	}

	private boolean testeHabilidadePilotoHumanoCarro(
			InterfaceJogo controleJogo) {
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
		if (colisao != null) {
			return false;
		}
		if (ciclosDesconcentrado <= 0) {
			ciclosDesconcentrado = 0;
			if (isProblemaLargada()) {
				if (getPosicao() < 8) {
					interfaceJogo.info(Html.superRed(
							getNome() + " " + Lang.msg("problemaLargada")));
				}
				setProblemaLargada(false);
			}
			return false;
		}
		double val = (Constantes.CICLO / 80);
		if (val < 1.0) {
			if (Math.random() > val) {
				val = 1;
			} else {
				val = 0;
			}
		}
		int dec = (int) val;
		if (AGRESSIVO.equals(modoPilotagem) && getStress() < 70) {
			incStress(1);
			dec++;
		}
		ciclosDesconcentrado -= dec;
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
		if (Carro.GIRO_MIN_VAL == getCarro().getGiro()
				&& !getNoAtual().verificaRetaOuLargada()) {
			bonusMotor -= getCarro().testePotencia() ? 0.0 : 0.1;
		}
		if (getNoAtual().verificaRetaOuLargada()
				&& getCarro().testePotencia()) {
			acelerando = true;
			return (Math.random() < bonusMotor ? 4 : 3);
		} else if (getNoAtual().verificaRetaOuLargada()
				&& getCarro().testePotencia()
				&& getCarro().testeAerodinamica()) {
			acelerando = true;
			return (Math.random() < bonusMotor ? 3 : 2);
		} else if (getNoAtual().verificaCurvaAlta()
				&& getCarro().testePotencia() && agressivo) {
			acelerando = true;
			return (Math.random() < bonusMotor ? 3 : 2);
		} else if (getNoAtual().verificaCurvaAlta() && !agressivo
				&& testeHabilidadePilotoAerodinamica(controleJogo)) {
			acelerando = false;
			return (Math.random() < bonusMotor ? 3 : 1);
		} else if (getNoAtual().verificaCurvaBaixa() && agressivo
				&& testeHabilidadePilotoAerodinamica(controleJogo)
				&& testeHabilidadePilotoFreios(controleJogo)) {
			acelerando = false;
			return (Math.random() < bonusMotor ? 2 : 1);
		} else if (getNoAtual().verificaCurvaBaixa()
				&& testeHabilidadePilotoFreios(controleJogo)) {
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

	public void setPorcentagemCombustUltimaParadaBox(
			int porcentagemCombustUltimaParadaBox) {
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
		if (ptosBox == 0) {
			return false;
		}

		return true;
	}

	public void efetuarSaidaBox(InterfaceJogo interfaceJogo) {
		qtdeParadasBox++;
		ptosBox = 0;
		box = false;
		int novoLado = 0;
		if (interfaceJogo.getCircuito().getLadoBoxSaidaBox() != 0) {
			novoLado = interfaceJogo.getCircuito().getLadoBoxSaidaBox() == 1
					? 2
					: 1;
		}
		mudarTracado(novoLado, interfaceJogo);
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

		if (voltas.isEmpty()) {
			return null;
		}
		List ordenaVoltas = new ArrayList();
		for (Iterator iterator = voltas.iterator(); iterator.hasNext();) {
			Volta volta = (Volta) iterator.next();
			ordenaVoltas.add(volta);
		}
		Collections.sort(ordenaVoltas, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Volta v0 = (Volta) arg0;
				Volta v1 = (Volta) arg1;

				return Double.compare(v0.obterTempoVolta(),
						v1.obterTempoVolta());
			}
		});

		return (Volta) ordenaVoltas.get(0);
	}

	public void abandonar() {
		setDesqualificado(true);
		carro.abandonar();

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
		if (stress > 0 && (stress - val) > 0
				&& (Math.random() > ((700.0 - getPosicao() * 20) / 1000.0))) {
			stress -= val;
		}
	}

	public void incStress(int val) {
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
		for (Iterator iterator = ultsConsumosCombustivel.iterator(); iterator
				.hasNext();) {
			Integer longVal = (Integer) iterator.next();
			valmed += longVal.doubleValue();
		}
		if (ultsConsumosCombustivel.isEmpty())
			return 0;
		return valmed / ultsConsumosCombustivel.size();
	}

	public double calculaConsumoMedioPneu() {
		double valmed = 0;
		for (Iterator iterator = ultsConsumosPneu.iterator(); iterator
				.hasNext();) {
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
		if (isRecebeuBanderada()) {
			return false;
		}
		return mudarTracado(mudarTracado, interfaceJogo, false);
	}

	public boolean mudarTracado(int mudarTracado, InterfaceJogo interfaceJogo,
			boolean mesmoComColisao) {
		if (isRecebeuBanderada()) {
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
		if (indiceTracado != 0) {
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
		if (getTracado() != 4 && getTracado() != 5 && getColisao() != null
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
		if (!mesmoComColisao && verificaColisaoAoMudarDeTracado(interfaceJogo,
				mudarTracado)) {
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
			novoIndice = interfaceJogo.getCircuito()
					.getIndiceTracadoForaPista();
		}
		setIndiceTracado((int) novoIndice);
	}

	public int decIndiceTracado(InterfaceJogo interfaceJogo) {
		if (indiceTracado <= 0) {
			return 0;
		}
		if ((getTracadoAntigo() == 4 || getTracadoAntigo() == 5)
				&& indiceTracado < (Carro.ALTURA * interfaceJogo.getCircuito()
						.getMultiplicadorLarguraPista())) {
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

		No no = getNoAtualSuave();
		if (no == null) {
			no = getNoAtual();
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
		int velocidade = (controleJogo.isSafetyCarNaPista()
				? getVelocidadeExibir() / 2
				: getVelocidadeExibir());
		setVelocidadeExibir(velocidade);
	}

	public boolean verificaColisaoAoMudarDeTracado(InterfaceJogo controleJogo,
			int pos) {
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
		return controleJogo.calculaDiferencaParaAnterior(this);
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

	public boolean testeHabilidadePilotoAerodinamica(
			InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testeAerodinamica() && testeHabilidadePiloto();
	}

	public boolean testeHabilidadePilotoFreios(InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testeFreios() && testeHabilidadePiloto();
	}

	public boolean testeHabilidadePilotoAerodinamicaFreios(
			InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testeFreios() && carro.testeAerodinamica()
				&& testeHabilidadePiloto();
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

	public boolean isProblemaLargada() {
		return problemaLargada;
	}

	public void setProblemaLargada(boolean problemaLargada) {
		this.problemaLargada = problemaLargada;
	}

	public void atualizaInfoDebug(StringBuffer buffer) {
		Field[] declaredFields = Piloto.class.getDeclaredFields();
		buffer.append("---====Piloto====--- <br>");
		List<String> campos = new ArrayList<String>();
		campos.add("GanhosAltaMedia = "
				+ PainelCircuito.df4.format(getMedGanhosAlta()) + "<br>");

		campos.add("GanhosBaixaMedia = "
				+ PainelCircuito.df4.format(getMedGanhosBaixa()) + "<br>");

		campos.add("GanhosRetaMedia = "
				+ PainelCircuito.df4.format(getMedGanhosReta()) + "<br>");

		campos.add("DiffSuaveReal = "
				+ (getNoAtual().getIndex() - (getNoAtualSuave() != null
						? getNoAtualSuave().getIndex()
						: 0))
				+ "<br>");

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
		for (Iterator<String> iterator = campos.iterator(); iterator
				.hasNext();) {
			buffer.append(iterator.next());
		}
	}

	public boolean isAsfaltoAbrasivo() {
		return asfaltoAbrasivo;
	}

	public void setAsfaltoAbrasivo(boolean asfaltoAbrasivo) {
		this.asfaltoAbrasivo = asfaltoAbrasivo;
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
				if (ultimas5Voltas == null || ultimas5Voltas.isEmpty()) {
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

	public int getPontosCorrida() {
		return ControleCorrida.calculaPontos25(this);
	}

	public void setPontosCorrida(int pontosCorrida) {
	}

	public int getDiferencaPosiscoesCorrida() {
		return (getPosicaoInicial() - getPosicao());
	}

	public void setDiferencaPosiscoesCorrida(int diferencaPosiscoesCorrida) {
	}

}
