package sowbreira.f1mane.entidades;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sowbreira.f1mane.controles.ControleQualificacao;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class Piloto implements Serializable {
	private static final long serialVersionUID = 698992658460848522L;
	public static final String AGRESSIVO = "AGRESSIVO";
	public static final String NORMAL = "NORMAL";
	public static final String LENTO = "LENTO";
	private int setaCima;
	private int setaBaixo;
	private transient Rectangle diateira;
	private transient Rectangle centro;
	private transient Rectangle trazeira;
	private List ultsConsumosCombustivel = new LinkedList();
	private Integer ultimoConsumoCombust;
	private List ultsConsumosPneu = new LinkedList();
	private Integer ultimoConsumoPneu;
	protected String tipoPeneuJogador;
	protected String asaJogador;
	protected Integer combustJogador;
	private int id;
	private int velocidade;
	private int velocidadeExibir;
	private int velocidadeAnterior;
	private transient String setUpIncial;
	private String nome;
	private String nomeOriginal;
	private String nomeCarro;
	private String nomeJogador;
	private transient int habilidadeAntesQualify;
	private transient int habilidade;
	private int carX;
	private int carY;
	private long ptosPista;
	private int ptosPistaIncial;
	private int ultimoIndice;
	private int tracado;
	private int tracadoAntigo;
	private int indiceTracado;
	private double ganhoMax = Integer.MIN_VALUE;
	private double ultGanhoReta = 0;
	private boolean autoPos = true;
	private Point p1;
	private Point p2;
	private Point p5;
	private Point p4;
	private Point pontoDerrapada;
	private double distanciaDerrapada = Double.MAX_VALUE;
	private Double angulo;
	private transient int ptosBox;
	private int posicao;
	private int posicaoInicial;
	private transient int paradoBox;
	private int qtdeParadasBox;
	private boolean desqualificado;
	private boolean jogadorHumano;
	private transient boolean recebeuBanderada;
	private boolean box;
	private boolean boxBaixoRendimento;
	private boolean agressivo = true;
	private boolean acelerando;
	private Carro carro = new Carro();
	private No noAnterior = new No();
	private No noAtual = new No();
	private No noAtualSuave;
	private int numeroVolta;
	private int stress;
	private transient int ciclosDesconcentrado;
	private transient int porcentagemCombustUltimaParadaBox;
	private transient Map msgsBox = new HashMap();
	private List voltas = new ArrayList();
	private String modoPilotagem = NORMAL;
	private Volta voltaAtual;
	private int ciclosVoltaQualificacao;
	private Volta ultimaVolta;
	private Volta melhorVolta;
	private String segundosParaLider;
	private String tipoPneuBox;
	private String asaBox;
	private String vantagem;
	private int qtdeCombustBox;
	private long parouNoBoxMilis;
	private long saiuDoBoxMilis;
	private int msgTentativaNumVolta = 2;
	private ArrayList listGanho;
	private long ultimaMudancaPos;
	private double ganho;
	private int ganhoSuave;
	private boolean ativarKers;
	private int cargaKersVisual;
	private int cargaKersOnline;
	private boolean ativarDRS;
	private int novoModificador;
	private Set votosDriveThru = new HashSet();
	private boolean driveThrough;
	private long timeStampChegeda;
	private boolean cruzouLargada = false;
	private List<Double> ganhosBaixa = new ArrayList<Double>();
	private Double maxGanhoBaixa = new Double(0);
	private List<Double> ganhosAlta = new ArrayList<Double>();
	private Double maxGanhoAlta = new Double(0);
	private List<Double> ganhosReta = new ArrayList<Double>();
	private boolean travouRodas;
	private int contTravouRodas;
	private boolean freiandoReta;
	private boolean retardaFreiandoReta;
	private int ultModificador;
	private int tracadoDelay;
	private int naoDesenhaEfeitos;
	private long indexTracadoDelay;
	private int tamanhoBufferGanho = 10;
	private Piloto colisao;
	private int meiaEnvergadura = 20;
	private int diferencaParaProximoRetardatario;
	private boolean colisaoDiantera;
	private boolean colisaoCentro;
	private int mudouTracadoReta;
	private int indexRefDerrapada;

	public int getGanhoSuave() {
		return ganhoSuave;
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

	public void setContTravouRodas(int contTravouRodas) {
		this.contTravouRodas = contTravouRodas;
	}

	public int getContTravouRodas() {
		return contTravouRodas;
	}

	public boolean isTravouRodas() {
		return travouRodas;
	}

	public void setTravouRodas(boolean travouRodas) {
		this.travouRodas = travouRodas;
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

	public boolean isAtivarKers() {
		return ativarKers;
	}

	public int getCargaKersOnline() {
		return cargaKersOnline;
	}

	public void setCargaKersOnline(int cargaKersOnline) {
		this.cargaKersOnline = cargaKersOnline;
	}

	public int getCargaKersVisual() {
		return cargaKersVisual;
	}

	public void setCargaKersVisual(int cargaKersVisual) {
		this.cargaKersVisual = cargaKersVisual;
	}

	public void setAtivarKers(boolean ativarKers) {
		this.ativarKers = ativarKers;
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

	public String getTipoPeneuJogador() {
		return tipoPeneuJogador;
	}

	public void setTipoPeneuJogador(String tipoPeneuJogador) {
		this.tipoPeneuJogador = tipoPeneuJogador;
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

	public void setVoltas(List voltas) {
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
		return ((segundosParaLider == null) ? Lang.msg("Lider")
				: segundosParaLider);
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

	public List getVoltas() {
		return voltas;
	}

	public int getQtdeParadasBox() {
		return qtdeParadasBox;
	}

	public boolean isRecebeuBanderada() {
		return recebeuBanderada;
	}

	public long getTimeStampChegeda() {
		return timeStampChegeda;
	}

	public void setTimeStampChegeda(long timeStampChegeda) {
		this.timeStampChegeda = timeStampChegeda;
	}

	public void setRecebeuBanderada(boolean recebueBanderada,
			InterfaceJogo controleJogo) {
		if (!this.recebeuBanderada) {
			setTimeStampChegeda(System.currentTimeMillis());
			Carro carroAtraz = controleJogo.obterCarroAtraz(this);
			if (carroAtraz != null) {
				setVantagem(controleJogo.calculaSegundosParaProximo(carroAtraz
						.getPiloto()));
			}
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss .S");
			if (this.getPosicao() == 1) {
				controleJogo.infoPrioritaria(Html.superBlack(getNome())
						+ Html.superGreen(Lang.msg("044",
								new Object[] { getPosicao() })));
			} else {
				controleJogo.info(Html.superBlack(getNome())
						+ Html.green(Lang.msg("044",
								new Object[] { getPosicao() })));
			}
			double somaBaixa = 0;
			for (Iterator iterator = ganhosBaixa.iterator(); iterator.hasNext();) {
				Double d = (Double) iterator.next();
				somaBaixa += d;
			}
			double somaAlta = 0;
			for (Iterator iterator = ganhosAlta.iterator(); iterator.hasNext();) {
				Double d = (Double) iterator.next();
				somaAlta += d;
			}
			double somaReta = 0;
			for (Iterator iterator = ganhosReta.iterator(); iterator.hasNext();) {
				Double d = (Double) iterator.next();
				somaReta += d;
			}
			somaBaixa /= ganhosBaixa.size();
			somaAlta /= ganhosAlta.size();
			somaReta /= ganhosReta.size();
			// System.out.println("Bandeirada " + this + " Pts pista "
			// + this.getPtosPista() + " Pos " + getPosicao() + " T "
			// + df.format(new Date()));
			// System.out.println(" SomaBaixa " + somaBaixa + " SomaAlta " +
			// somaAlta
			// + " SomaReta " + somaReta);
			// controleJogo.pausarJogo();
		}
		this.recebeuBanderada = recebueBanderada;
	}

	public void setQtdeParadasBox(int qtdeParadasBox) {
		this.qtdeParadasBox = qtdeParadasBox;
	}

	public String getSetUpIncial() {
		return setUpIncial;
	}

	public void setSetUpIncial(String setUpIncial) {
		this.setUpIncial = setUpIncial;
	}

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

	public void setAgressivoF4(boolean regMaximo) {
		if (regMaximo && verificaDesconcentrado()) {
			return;
		}
		this.agressivo = regMaximo;
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
		List pista = controleJogo.getNosDaPista();
		int index = processaNovoIndex(controleJogo);
		int diff = index - pista.size();
		/**
		 * Completou Volta
		 */
		if (diff >= 0) {
			if (!controleJogo.isModoQualify() && !cruzouLargada) {
				setPtosPista(controleJogo.getNosDaPista().size());
				cruzouLargada = true;
			}
			setNumeroVolta(getNumeroVolta() + 1);
			processaAjustesAntesDepoisQuyalify(Constantes.MAX_VOLTAS
					/ controleJogo.getQtdeTotalVoltas());
			processaUltimosDesgastesPneuECombustivel();

			index = diff;
			getCarro().setCargaKers(InterfaceJogo.CARGA_KERS);
			ativarKers = false;
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
					tempoVolta = getUltimaVolta().obterTempoVoltaFormatado();
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

		if (controleJogo.isCorridaTerminada()) {
			int indexPiloto = getNoAtual().getIndex();
			if (indexPiloto < 100) {
				setRecebeuBanderada(true, controleJogo);
			}
		}

		verificaIrBox(controleJogo);
		this.setNoAtual((No) pista.get(index));
	}

	private void processaAjustesAntesDepoisQuyalify(int i) {
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
		int pCombust = getCarro().porcentagemCombustivel();
		if (ultimoConsumoCombust == null) {
			ultimoConsumoCombust = new Integer(pCombust);
		} else {
			if (ultimoConsumoCombust.intValue() > pCombust) {
				ultsConsumosCombustivel.add(ultimoConsumoCombust.intValue()
						- pCombust);
				ultimoConsumoCombust = new Integer(pCombust);

			}
		}
		int pPneu = getCarro().porcentagemDesgastePeneus();
		if (ultimoConsumoPneu == null) {
			ultimoConsumoPneu = new Integer(pPneu);
		} else {
			if (ultimoConsumoPneu.intValue() > pPneu) {
				ultsConsumosPneu.add(ultimoConsumoCombust.intValue() - pPneu);
				ultimoConsumoPneu = new Integer(pPneu);
			}
		}
	}

	private void verificaIrBox(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return;
		}

		if (isJogadorHumano() || isRecebeuBanderada() || getPtosPista() < 0) {
			return;
		}
		int pneus = getCarro().porcentagemDesgastePeneus();
		int combust = getCarro().porcentagemCombustivel();
		int corrida = controleJogo.porcentagemCorridaCompletada();
		if (controleJogo.isSemReabastacimento()) {
			combust = 100;
		}
		mensagemBoxOcupado(controleJogo);

		if ((combust < 5) && !controleJogo.isCorridaTerminada()) {
			box = true;
		} else {
			box = false;
		}

		if (controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isSafetyCarVaiBox()) {
			if (combust < 20 || pneus < 50) {
				box = true;
			}

		}

		if (!boxBaixoRendimento && colisao == null && pneus < 20
				&& (ganho < (.7 * maxGanhoBaixa))) {
			boxBaixoRendimento = true;
		}

		if (boxBaixoRendimento) {
			box = true;
		}

		int limiteUltimasVoltas = 75;
		if (controleJogo.isBoxRapido()) {
			limiteUltimasVoltas = 85;
		}

		if (box && corrida > limiteUltimasVoltas && getQtdeParadasBox() > 0) {
			if (controleJogo.verificaInfoRelevante(this)
					&& !msgsBox.containsKey(Messagens.IR_BOX_FINAL_CORRIDA)) {
				controleJogo.info(Html.orange(Lang.msg("047",
						new String[] { getNome() })));
				msgsBox.put(Messagens.IR_BOX_FINAL_CORRIDA,
						Messagens.IR_BOX_FINAL_CORRIDA);
			}
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

		if (controleJogo.getNumVoltaAtual() < 1) {
			box = false;
		}

		if (controleJogo.isCorridaTerminada()) {
			box = false;
		}
	}

	private void mensagemBoxOcupado(InterfaceJogo controleJogo) {
		if (box && controleJogo.verificaBoxOcupado(getCarro())) {
			if (!Messagens.BOX_OCUPADO.equals(msgsBox
					.get(Messagens.BOX_OCUPADO))) {
				if (controleJogo.verificaInfoRelevante(this)) {
					controleJogo.info(Html.orange(Lang.msg("046",
							new String[] { Html.bold(getNome()) })));
				}
				msgsBox.put(Messagens.BOX_OCUPADO, Messagens.BOX_OCUPADO);
			}
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

	private int processaNovoIndex(InterfaceJogo controleJogo) {
		int index = getNoAtual().getIndex();
		/**
		 * Devagarinho qdo a corrida termina
		 */
		if ((controleJogo.isCorridaTerminada() && isRecebeuBanderada())) {
			double novoModificador = (controleJogo.getCircuito()
					.getMultiplciador());
			index += novoModificador;
			setPtosPista(Util.inte(novoModificador + getPtosPista()));
			setVelocidade(Util.intervalo(50, 65));
			return index;
		}
		if (desqualificado) {
			return getNoAtual().getIndex();
		}
		novoModificador = calcularNovoModificador(controleJogo);
		novoModificador = getCarro().calcularModificadorCarro(novoModificador,
				agressivo, getNoAtual(), controleJogo);

		processaStress(controleJogo);
		processaLimitadorModificador();
		processaUsoKERS(controleJogo);
		processaUsoDRS(controleJogo);
		verificaMudancaRegime(controleJogo);
		processaGanho(controleJogo);
		processaPontoDerrapada(controleJogo);
		processaIAnovoIndex(controleJogo);
		processaEscapadaDaPista(controleJogo);
		processaTurbulencia(controleJogo);
		processaGanhoDanificado();
		processaFreioNaReta(controleJogo);
		processaEvitaBaterCarroFrente(controleJogo);
		processaMudarTracado(controleJogo);
		processaColisao(controleJogo);
		controleJogo.verificaUltraPassagem(this);
		if (getColisao() != null) {
			penalidadeColisao(controleJogo);
		}
		ganho = processaGanhoMedio(controleJogo, ganho);
		processaLimitadorGanho(controleJogo);
		processaGanhoSafetyCar(controleJogo);
		decremetaPilotoDesconcentrado(controleJogo);
		setPtosPista(Util.inte(getPtosPista() + ganho));
		index += Math.round(ganho);
		setVelocidade(calculoVelocidade(ganho));
		return index;
	}

	private void processaGanhoSafetyCar(InterfaceJogo controleJogo) {
		if (!controleJogo.isSafetyCarNaPista()) {
			return;
		}
		ganho = controleJogo.ganhoComSafetyCar(ganho, controleJogo, this);
		if (ganho > 40) {
			ganho = 40;
		}
		if (ganho < 10 && controleJogo.calculaDiferencaParaProximo(this) > 100) {
			ganho = 10;
		}
		if (getTracado() == 4) {
			mudarTracado(2, controleJogo, true);
		}
		if (getTracado() == 5) {
			mudarTracado(1, controleJogo, true);
		}
	}

	public static void main(String[] args) {
		// Piloto p = new Piloto();
		// p.ganhoMax = 60;
		// p.freiandoReta = true;
		// p.acelerando = false;
		// System.out.println(p.calculoVelocidade(10));
		System.out.println(Math.log(2) / Math.log(2));
	}

	private int calculoVelocidade(double ganho) {
		int val = 300;
		double porcent = getCarro().porcentagemCombustivel() / 100.0;
		val += (21 - (porcent / 5.0));
		boolean naReta = false;
		if (noAtual != null && !freiandoReta
				&& (acelerando || ativarDRS || ativarKers))
			naReta = noAtual.verificaRetaOuLargada();
		return Util
				.inte(((val * ganho * ((naReta) ? 1 : 0.7) / ganhoMax) + ganho
						* ((naReta) ? 1 : 0.7)));
	}

	public void processaColisao(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			setColisao(null);
			return;
		}
		verificaColisaoCarroFrente(controleJogo);
	}

	public void penalidadeColisao(InterfaceJogo controleJogo) {
		acelerando = false;
		setAgressivoF4(false);
		incStress(testeHabilidadePiloto(controleJogo) ? Util.intervalo(5, 10)
				: Util.intervalo(10, 20));
		if (getStress() > 90 && Piloto.AGRESSIVO.equals(getModoPilotagem())) {
			controleJogo.travouRodas(this);
			setCiclosDesconcentrado(5);
		}
		if (Piloto.AGRESSIVO.equals(getModoPilotagem()))
			setCiclosDesconcentrado(5);
	}

	public void processaEscapadaDaPista(InterfaceJogo controleJogo) {
		if (controleJogo.isSafetyCarNaPista()) {
			return;
		}
		/**
		 * Derrapa mas Fica na pista
		 */
		if (No.CURVA_BAIXA.equals(getNoAtual().getTipo()) && agressivo
				&& (getTracado() == 0)
				&& (carro.porcentagemDesgastePeneus() < 30)) {
			if (getStress() > 60)
				controleJogo.travouRodas(this);
			if (getTracadoAntigo() != 0) {
				if (getTracadoAntigo() == 1) {
					mudarTracado(2, controleJogo, true);
				} else {
					mudarTracado(1, controleJogo, true);
				}
			} else {
				mudarTracado(Util.intervalo(1, 2), controleJogo, true);
			}
			return;
		}

		/**
		 * Escapa la fora
		 */
		if (getStress() > getValorLimiteStressePararErrarCurva(controleJogo)
				&& !controleJogo.isSafetyCarNaPista()
				&& AGRESSIVO.equals(modoPilotagem)
				&& !testeHabilidadePilotoCarro(controleJogo)
				&& getPtosBox() == 0) {
			controleJogo.travouRodas(this);
			derrapa(controleJogo);
		}
		/**
		 * Volta a pista apos derrapagem
		 */
		if (getTracado() == 4 || getTracado() == 5) {
			if (!verificaDesconcentrado()) {
				setCiclosDesconcentrado(Util.intervalo(50, 150));
				if (controleJogo.verificaInfoRelevante(this))
					controleJogo.info(Lang.msg("saiDaPista",
							new String[] { Html.superRed(getNome()) }));
			}
			setModoPilotagem(LENTO);
			if (getIndiceTracado() <= 0) {
				if (controleJogo.isChovendo()) {
					ganho *= 0.1;
				} else {
					ganho *= 0.3;
				}
				if (getTracado() == 4) {
					mudarTracado(2, controleJogo, true);
				}
				if (getTracado() == 5) {
					mudarTracado(1, controleJogo, true);
				}
			} else {
				if (controleJogo.isChovendo()) {
					ganho *= 0.5;
				} else {
					ganho *= 0.7;
				}
				decStress(2);
			}
		}
	}

	private void processaGanho(InterfaceJogo controleJogo) {
		ganho = ((novoModificador * controleJogo.getCircuito()
				.getMultiplciador()) * (controleJogo.getIndexVelcidadeDaPista()));
		if (verificaForaPista(this)) {
			ganho *= controleJogo.getFatorUtrapassagem();
		}
	}

	public double getValorLimiteStressePararErrarCurva(
			InterfaceJogo controleJogo) {
		if (isJogadorHumano()) {
			return 100 * (1.0 - controleJogo.getNiveljogo());
		} else {
			return 100 * (controleJogo.getNiveljogo() + 0.1);
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
		ultModificador = novoModificador;
	}

	private void processaGanhoDanificado() {
		if (danificado()) {
			if (getNoAtual().verificaCruvaBaixa()
					&& (Carro.PNEU_FURADO.equals(getCarro().getDanificado()) || (Carro.PERDEU_AEREOFOLIO
							.equals(getCarro().getDanificado())))) {
				if (ganho > 10)
					ganho = 10;
			}
			if (getNoAtual().verificaCruvaAlta()
					&& (Carro.PNEU_FURADO.equals(getCarro().getDanificado()) || (Carro.PERDEU_AEREOFOLIO
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
		Carro carroPilotoDaFrente = controleJogo
				.obterCarroNaFrenteRetardatario(this, true);
		if (carroPilotoDaFrente == null) {
			return;
		}
		if (carroPilotoDaFrente.getPiloto().isDesqualificado()) {
			return;
		}
		if ((carroPilotoDaFrente.getPiloto().getTracado() == 1 && getTracado() == 2)
				|| (carroPilotoDaFrente.getPiloto().getTracado() == 2 && getTracado() == 1)) {
			return;
		}
		double diff = controleJogo.calculaDiffParaProximoRetardatario(this,
				false);
		double distLimiteTurbulencia = 50;
		double nGanho = (controleJogo.getFatorUtrapassagem());
		if (diff < distLimiteTurbulencia
				&& !verificaForaPista(carroPilotoDaFrente.getPiloto())) {
			if (getTracado() != carroPilotoDaFrente.getPiloto().getTracado()) {
				if (getNoAtual().verificaRetaOuLargada()) {
					nGanho += (getCarro().testePotencia() && getCarro()
							.testeAerodinamica()) ? 0.3 : 0.1;
				} else {
					nGanho += (testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? 0.3
							: 0.1);
				}
			}
			if (nGanho > 1) {
				nGanho = 1;
			} else if (nGanho < 0.01) {
				nGanho = 0.01;
			}
			ganho *= (nGanho);
		}
	}

	private boolean verificaForaPista(Piloto piloto) {
		boolean voltando = false;
		if (getIndiceTracado() > 0
				&& (piloto.getTracadoAntigo() == 4 || piloto.getTracadoAntigo() == 5)) {
			voltando = true;
		}
		return piloto.getTracado() == 4 || piloto.getTracado() == 5 || voltando;
	}

	private void processaFreioNaReta(InterfaceJogo controleJogo) {
		boolean testPilotoPneus = Carro.TIPO_PNEU_MOLE.equals(getCarro()
				.getTipoPneu()) && getCarro().testeFreios();
		/**
		 * efeito freiar na reta
		 */
		No obterProxCurva = controleJogo.obterProxCurva(getNoAtual());
		if (obterProxCurva != null) {
			double val = obterProxCurva.getIndex() - getNoAtual().getIndex();
			double distAfrente = 300.0;
			if (controleJogo.getNumVoltaAtual() <= 1) {
				distAfrente = 500.0;
			}
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
				if (!controleJogo.isModoQualify()
						&& (controleJogo.isChovendo() || controleJogo
								.getNumVoltaAtual() <= 1)) {
					minMulti -= 0.3;
					retardaFreiandoReta = false;
				}
				if (controleJogo
						.calculaDiffParaProximoRetardatario(this, false) < 50) {
					minMulti -= Util.intervalo(0.05, 0.15);
					retardaFreiandoReta = false;
				} else if (controleJogo.calculaDiffParaProximoRetardatario(
						this, true) < 100) {
					minMulti -= 0.1;
					retardaFreiandoReta = false;
				} else if (controleJogo.calculaDiffParaProximoRetardatario(
						this, true) < 150) {
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

		if (getNoAtual().verificaCruvaBaixa() && retardaFreiandoReta) {
			if (getPosicao() <= 3 && Math.random() > 0.9
					&& !testeHabilidadePilotoFreios(controleJogo)) {
				incStress(Util.intervalo(5, 10));
				agressivo = false;
				if (controleJogo.verificaInfoRelevante(this)
						&& Math.random() > 0.7)
					controleJogo.info(Lang.msg("014",
							new String[] { Html.bold(getNome()) }));
			}
			retardaFreiandoReta = false;
		}
	}

	private void processaLimitadorGanho(InterfaceJogo controleJogo) {
		if (getColisao() != null) {
			acelerando = false;
			ganho = Util.intervalo(0, 1);
			return;
		}
		if (ganho > 0 && ganho < 1) {
			ganho = 1;
		}
		if (ganho > 70) {
			ganho = 70;
		}
		double emborrachamento = controleJogo.porcentagemCorridaCompletada() / 100.0;
		if (getNoAtual().verificaCruvaBaixa()) {
			double limite = 25;
			if (Math.random() < emborrachamento
					&& !controleJogo.verificaNivelJogo()) {
				limite = 30;
			}
			if (ganho > limite) {
				ganho = limite;
			}
			ganhosBaixa.add(ganho);
			if (ganho > maxGanhoBaixa) {
				maxGanhoBaixa = ganho;
			}
		}
		if (getNoAtual().verificaCruvaAlta()) {
			double limite = 30;
			if (Math.random() < emborrachamento
					&& !controleJogo.verificaNivelJogo()) {
				limite = 35;
			}
			if (ganho > limite) {
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

	private void processaUsoKERS(InterfaceJogo controleJogo) {

		if (controleJogo.isKers() && ativarKers && getPtosBox() == 0
				&& getNumeroVolta() > 0) {
			if (getCarro().getCargaKers() <= 0) {
				ativarKers = false;
			} else {
				if (!getCarro().testePotencia()) {
					return;
				}
				if (controleJogo.getNumVoltaAtual() <= 1) {
					ganho *= 1.1;
				} else {
					ganho *= 1.2;
				}
				getCarro().usaKers();
			}
		}
	}

	private void processaUsoDRS(InterfaceJogo controleJogo) {

		if (controleJogo.isDrs() && ativarDRS && getPtosBox() == 0
				&& getNumeroVolta() > 0) {

			if (getNoAtual().verificaRetaOuLargada()
					&& controleJogo.calculaDiffParaProximoRetardatario(this,
							false) < Constantes.LIMITE_DRS) {
				Carro carroNaFrenteRetardatario = controleJogo
						.obterCarroNaFrenteRetardatario(this, false);
				if (carroNaFrenteRetardatario != null
						&& carroNaFrenteRetardatario.getPiloto()
								.getNumeroVolta() > getNumeroVolta()
						&& carroNaFrenteRetardatario.getPiloto().getPosicao() < (getPosicao() - 1)) {
					ativarDRS = false;
					getCarro().setAsa(Carro.MAIS_ASA);
					return;
				}
				getCarro().setAsa(Carro.MENOS_ASA);
				if (Math.random() > 0.9 && !getCarro().testeAerodinamica()) {
					getCarro().setAsa(Carro.ASA_NORMAL);
				}
			} else {
				ativarDRS = false;
				getCarro().setAsa(Carro.MAIS_ASA);
			}
			if (!getNoAtual().verificaRetaOuLargada()
					&& testeHabilidadePiloto(controleJogo)) {
				ativarDRS = false;
			}
		} else if (controleJogo.isDrs()) {
			ativarDRS = false;
			getCarro().setAsa(Carro.MAIS_ASA);
		}
	}

	private void processaStress(InterfaceJogo controleJogo) {
		int fatorStresse = Util.intervalo(1,
				(int) controleJogo.getNiveljogo() * 10);
		if (getNoAtual().verificaCruvaAlta()
				|| getNoAtual().verificaCruvaBaixa()) {
			fatorStresse /= 2;
		}
		if (NORMAL.equals(getModoPilotagem()) || !agressivo) {
			decStress(fatorStresse);
		} else if (LENTO.equals(getModoPilotagem())) {
			decStress(fatorStresse
					* (testeHabilidadePiloto(controleJogo) ? 2 : 1));
		}
	}

	private void processaIAnovoIndex(InterfaceJogo controleJogo) {
		if (colisao != null) {
			agressivo = false;
			return;
		}
		if (controleJogo.isModoQualify() || controleJogo.isSafetyCarNaPista()
				|| isJogadorHumano() || getPtosBox() != 0 || danificado()
				|| verificaDesconcentrado()) {
			return;
		}
		if (controleJogo.isKers()) {
			tentaUsarKers(controleJogo);
		}
		if (controleJogo.isDrs()) {
			tentaUsarDRS(controleJogo);
		}
		if (ControleQualificacao.modoQualify) {
			return;
		}
		boolean tentaPassarFrete = tentarPassaPilotoDaFrente(controleJogo);
		boolean tentarEscaparTraz = tentarEscaparPilotoDaTraz(controleJogo,
				tentaPassarFrete);
		if (!tentaPassarFrete && !tentarEscaparTraz) {
			getCarro().setGiro(Carro.GIRO_NOR_VAL);
			setModoPilotagem(NORMAL);
		}
		if (getCarro().verificaCondicoesCautelaGiro(controleJogo)
				|| entrouNoBox()) {
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
		}
	}

	private void processaMudarTracado(InterfaceJogo controleJogo) {
		if (!noAtual.verificaRetaOuLargada()) {
			mudouTracadoReta = 0;
		}
		if (!isAutoPos()) {
			return;
		}
		double diff = controleJogo.calculaDiffParaProximoRetardatario(this,
				true);
		int diffAnt = controleJogo.calculaDiferencaParaAnterior(this);
		if (diff < diffAnt) {
			if (diff < 150) {
				if (testeHabilidadePiloto(controleJogo)) {
					controleJogo.fazPilotoMudarTracado(this, controleJogo
							.obterCarroNaFrenteRetardatario(this, true)
							.getPiloto());
				} else {
					controleJogo.fazPilotoMudarTracado(this, controleJogo
							.obterCarroNaFrenteRetardatario(this, false)
							.getPiloto());

				}
			}
		} else {
			Carro carroAtraz = controleJogo.obterCarroAtraz(this);
			if (carroAtraz == null) {
				return;
			}
			if (mudouTracadoReta > 1) {
				return;
			}
			Piloto pilotoAtraz = carroAtraz.getPiloto();
			if (diffAnt < 200 && diffAnt > 50 && pilotoAtraz.getPtosBox() == 0
					&& testeHabilidadePiloto(controleJogo) && !isFreiandoReta()
					&& !isJogadorHumano()
					&& controleJogo.getNiveljogo() < Math.random()) {
				if (mudarTracado(pilotoAtraz.getTracado(), controleJogo, false)
						&& noAtual.verificaRetaOuLargada()) {
					mudouTracadoReta++;
				}

			} else if (!isJogadorHumano()) {
				mudarTracado(0, controleJogo, false);
			}

		}
		if (!isJogadorHumano()
				&& testeHabilidadePiloto(controleJogo)
				&& pontoDerrapada != null
				&& distanciaDerrapada < ((2 * controleJogo.getNiveljogo()) * Carro.RAIO_DERRAPAGEM)) {
			int ladoDerrapa = controleJogo.obterLadoDerrapa(pontoDerrapada);
			if (ladoDerrapa == 5 && getTracado() != 2) {
				mudarTracado(2, controleJogo, false);
			}
			if (ladoDerrapa == 4 && getTracado() != 1) {
				mudarTracado(1, controleJogo, false);
			}
		}
	}

	private boolean tentarEscaparPilotoDaTraz(InterfaceJogo controleJogo,
			boolean tentaPassarFrete) {
		if (tentaPassarFrete) {
			return false;
		}
		if (Math.random() > (controleJogo.getNiveljogo() + 0.2)) {
			return false;
		}
		int calculaDiferencaParaAnterior = controleJogo
				.calculaDiferencaParaAnterior(this);
		if (calculaDiferencaParaAnterior < 200
				&& testeHabilidadePiloto(controleJogo)) {
			modoIADefesaAtaque(controleJogo, null);
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
				&& testeHabilidadePiloto(controleJogo)) {
			ativarDRS = true;
		} else {
			ativarDRS = false;
		}
	}

	private void tentaUsarKers(InterfaceJogo controleJogo) {
		if (verificaDesconcentrado()) {
			return;
		}
		int calculaDiferencaParaAnterior = controleJogo
				.calculaDiferencaParaAnterior(this);
		if (calculaDiferencaParaAnterior < Util.intervalo(20, 30)
				&& Math.random() < controleJogo.getNiveljogo()
				&& testeHabilidadePiloto(controleJogo)) {
			ativarKers = true;
		} else {
			ativarKers = false;
		}
		if (controleJogo.percetagemDeVoltaCompletada(this) > 60) {
			ativarKers = true;
		}

	}

	public double getGanho() {
		return ganho;
	}

	public void verificaColisaoCarroFrente(InterfaceJogo controleJogo) {
		boolean verificaNoPitLane = controleJogo.verificaNoPitLane(this);
		if (verificaNoPitLane) {
			setColisao(null);
			return;
		}
		List<Piloto> pilotos = controleJogo.getPilotos();
		centralizaDianteiraTrazeiraCarro(controleJogo);
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoFrente = (Piloto) iterator.next();
			if (pilotoFrente.equals(this)) {
				continue;
			}
			if (verificaNaoPrecisaDesvia(controleJogo, pilotoFrente)) {
				continue;
			}
			pilotoFrente.centralizaDianteiraTrazeiraCarro(controleJogo);

			colisaoDiantera = getDiateira().intersects(
					pilotoFrente.getTrazeira())
					|| getDiateira().intersects(pilotoFrente.getCentro());
			colisaoCentro = getCentro().intersects(pilotoFrente.getTrazeira());

			colisao = (colisaoDiantera || colisaoCentro) ? pilotoFrente : null;
			if (colisao != null) {
				return;
			}
		}
		setColisao(null);

	}

	private boolean verificaNaoPrecisaDesvia(InterfaceJogo controleJogo,
			Piloto pilotoFrente) {
		return (pilotoFrente.isDesqualificado() && !controleJogo
				.isSafetyCarNaPista()) || pilotoFrente.getCarro().isRecolhido();
	}

	private void processaEvitaBaterCarroFrente(InterfaceJogo controleJogo) {
		Carro obterCarroNaFrenteRetardatario = controleJogo
				.obterCarroNaFrenteRetardatario(this, true);
		if (obterCarroNaFrenteRetardatario == null) {
			return;
		}

		Piloto piloto = obterCarroNaFrenteRetardatario.getPiloto();
		if (this.equals(piloto)) {
			return;
		}
		if (verificaNaoPrecisaDesvia(controleJogo, piloto)) {
			return;
		}
		if (piloto.getPtosBox() > 0) {
			return;
		}
		double limite = 250;
		if (getNoAtual().verificaRetaOuLargada() && !isFreiandoReta()
				&& controleJogo.getNumVoltaAtual() > 1
				&& !controleJogo.safetyCarUltimas3voltas()) {
			limite -= controleJogo.porcentagemCorridaCompletada();
		}

		diferencaParaProximoRetardatario = controleJogo
				.calculaDiffParaProximoRetardatario(this, true);
		if (diferencaParaProximoRetardatario < limite
				&& (getTracado() == piloto.getTracado())) {
			ganho *= (diferencaParaProximoRetardatario / limite);
		}
	}

	public boolean derrapa(InterfaceJogo controleJogo) {
		/**
		 * Verificar na entrada da curva e nao na area de escape
		 */
		if (getTracado() == 4 || getTracado() == 5) {
			return false;
		}
		if (pontoDerrapada == null) {
			return false;
		}
		if (distanciaDerrapada > Carro.RAIO_DERRAPAGEM) {
			return false;
		}
		if (getNoAtual() != null && indexRefDerrapada < getNoAtual().getIndex()) {
			return false;
		}
		int ladoDerrapa = controleJogo.obterLadoDerrapa(pontoDerrapada);
		if (ladoDerrapa == 5 && getTracado() == 2) {
			return false;
		}
		if (ladoDerrapa == 4 && getTracado() == 1) {
			return false;
		}
		mudarTracado(ladoDerrapa, controleJogo, true);
		return true;
	}

	public Point getPontoDerrapada() {
		return pontoDerrapada;
	}

	public void processaPontoDerrapada(InterfaceJogo controleJogo) {
		distanciaDerrapada = Double.MAX_VALUE;
		pontoDerrapada = null;
		indexRefDerrapada = 0;
		double multi = 0.6;
		if (getTracado() == 0) {
			multi = 1.2;
		}
		if (getNoAtual() == null) {
			return;
		}

		int index = (int) (getNoAtual().getIndex() + controleJogo
				.getTempoCiclo() * multi);
		if (index >= controleJogo.getNosDaPista().size()) {
			return;
		}
		No proxPt = controleJogo.getNosDaPista().get(index);
		Circuito circuito = controleJogo.getCircuito();
		List<Point> escapeList = circuito.getEscapeList();
		if (escapeList == null) {
			return;
		}
		Point p = proxPt.getPoint();
		for (Iterator iterator = escapeList.iterator(); iterator.hasNext();) {
			Point point = (Point) iterator.next();
			double distaciaEntrePontos = GeoUtil.distaciaEntrePontos(p, point);
			if (distaciaEntrePontos < distanciaDerrapada) {
				distanciaDerrapada = distaciaEntrePontos;
				pontoDerrapada = point;
				indexRefDerrapada = index;
			}
		}
	}

	public Rectangle2D centralizaDianteiraTrazeiraCarro(
			InterfaceJogo controleJogo) {
		No noAtual = getNoAtual();
		if (getNoAtualSuave() != null) {
			noAtual = getNoAtualSuave();
		}
		int cont = noAtual.getIndex();
		List lista = controleJogo.obterPista(noAtual);
		if (lista == null) {
			return null;
		}
		Point p = noAtual.getPoint();
		int carx = p.x;
		int cary = p.y;
		int traz = cont - meiaEnvergadura;
		int frente = cont + meiaEnvergadura;
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

		Point trazCar = ((No) lista.get(traz)).getPoint();
		trazCar = new Point(trazCar.x, trazCar.y);
		Point frenteCar = ((No) lista.get(frente)).getPoint();
		frenteCar = new Point(frenteCar.x, frenteCar.y);
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		setAngulo(calculaAngulo);
		Rectangle2D rectangle = new Rectangle2D.Double(
				(p.x - Carro.MEIA_LARGURA_CIMA),
				(p.y - Carro.MEIA_ALTURA_CIMA), Carro.LARGURA_CIMA,
				Carro.ALTURA_CIMA);
		Point p1 = GeoUtil.calculaPonto(
				calculaAngulo,
				Util.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(
				calculaAngulo + 180,
				Util.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		Point p5 = GeoUtil.calculaPonto(
				calculaAngulo,
				Util.inte(Carro.ALTURA
						* 3
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		Point p4 = GeoUtil.calculaPonto(
				calculaAngulo + 180,
				Util.inte(Carro.ALTURA
						* 3
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));

		setP1(p1);
		setP2(p2);
		setP5(p5);
		setP4(p4);
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
			carx = Util.inte((p1.x));
			cary = Util.inte((p1.y));
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
			carx = Util.inte((p5.x));
			cary = Util.inte((p5.y));
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
			carx = Util.inte((p2.x));
			cary = Util.inte((p2.y));
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
			carx = Util.inte((p4.x));
			cary = Util.inte((p4.y));
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

		setCarX(carx);
		setCarY(cary);

		rectangle = new Rectangle2D.Double((carx - Carro.MEIA_ALTURA
				* Carro.FATOR_AREA_CARRO), (cary - Carro.MEIA_ALTURA
				* Carro.FATOR_AREA_CARRO), Carro.ALTURA
				* Carro.FATOR_AREA_CARRO, Carro.ALTURA * Carro.FATOR_AREA_CARRO);

		setCentro(rectangle.getBounds());

		trazCar = GeoUtil.calculaPonto(calculaAngulo + 90,
				Util.inte(meiaEnvergadura), new Point(carx, cary));

		Rectangle2D trazRec = new Rectangle2D.Double(
				(trazCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(trazCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO, Carro.ALTURA
						* Carro.FATOR_AREA_CARRO);
		setTrazeira(trazRec.getBounds());

		frenteCar = GeoUtil.calculaPonto(calculaAngulo + 270,
				Util.inte(meiaEnvergadura), new Point(carx, cary));

		Rectangle2D frenteRec = new Rectangle2D.Double(
				(frenteCar.x - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				(frenteCar.y - Carro.MEIA_ALTURA * Carro.FATOR_AREA_CARRO),
				Carro.ALTURA * Carro.FATOR_AREA_CARRO, Carro.ALTURA
						* Carro.FATOR_AREA_CARRO);
		setDiateira(frenteRec.getBounds());

		return rectangle;
	}

	public List obterPista(InterfaceJogo controleJogo) {
		return controleJogo.obterPista(this.getNoAtual());
	}

	public Piloto() {
		zerarGanhoEVariaveisUlt();
	}

	public void zerarGanhoEVariaveisUlt() {
		listGanho = new ArrayList();
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
		if (Math.random() > (controleJogo.getNiveljogo() + 0.2)) {
			return false;
		}
		Carro carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
		if (carroPilotoDaFrente == null) {
			return false;
		}
		Piloto pilotoFrente = carroPilotoDaFrente.getPiloto();
		if (pilotoFrente.getPtosBox() != 0) {
			return false;
		}
		int diff = calculaDiffParaProximo(controleJogo);
		int size = controleJogo.getCircuito().getPistaFull().size();
		int distBrigaMax = (int) (size * controleJogo.getNiveljogo());
		int distBrigaMin = 0;
		if (diff > distBrigaMin && diff < distBrigaMax
				&& testeHabilidadePiloto(controleJogo)) {
			modoIADefesaAtaque(controleJogo, carroPilotoDaFrente);
			return true;
		}
		return false;
	}

	private void modoIADefesaAtaque(InterfaceJogo controleJogo,
			Carro carroPilotoDaFrente) {
		int porcentagemCombustivel = getCarro().porcentagemCombustivel();
		int porcentagemDesgastePeneus = getCarro().porcentagemDesgastePeneus();
		boolean superAquecido = getCarro().verificaMotorSuperAquecido();
		boolean drsAtivado = Carro.MENOS_ASA.equals(getCarro().getAsa())
				&& controleJogo.isDrs() && !controleJogo.isChovendo();
		int motor = getCarro().porcentagemDesgasteMotor();
		int corrida = controleJogo.porcentagemCorridaCompletada();
		boolean temMotor = motor > corrida;
		int combustivel = getCarro().porcentagemCombustivel();
		boolean temCombustivel = combustivel > corrida
				&& porcentagemCombustivel > 5;
		double valorLimiteStressePararErrarCurva = getValorLimiteStressePararErrarCurva(controleJogo);
		boolean maxUltimasVoltas = temCombustivel
				&& controleJogo.verificaUltimasVoltas();
		boolean maxCorrida = !superAquecido
				&& porcentagemCombustivel > porcentagemDesgastePeneus
				&& temMotor && temCombustivel;
		if (maxUltimasVoltas || maxCorrida) {
			getCarro().setGiro(Carro.GIRO_MAX_VAL);
		}
		if (drsAtivado && porcentagemCombustivel > 5) {
			getCarro().setGiro(Carro.GIRO_MAX_VAL);
		}
		if (testeHabilidadePiloto(controleJogo)) {
			No no = getNoAtual();
			if (Carro.MAIS_ASA.equals(getCarro().getAsa())) {
				if ((no.verificaCruvaAlta() || no.verificaCruvaBaixa())) {
					if (maxUltimasVoltas || maxCorrida) {
						getCarro().setGiro(Carro.GIRO_MAX_VAL);
					}
				}
				if (no.verificaRetaOuLargada()) {
					getCarro().setGiro(Carro.GIRO_NOR_VAL);
				}
			}
			if (Carro.MENOS_ASA.equals(getCarro().getAsa())) {
				if ((no.verificaCruvaAlta() || no.verificaCruvaBaixa())) {
					getCarro().setGiro(Carro.GIRO_NOR_VAL);
				}
				if (no.verificaRetaOuLargada()) {
					if (maxUltimasVoltas || maxCorrida) {
						getCarro().setGiro(Carro.GIRO_MAX_VAL);
					}
				}
			}
		}
		if (testeHabilidadePiloto(controleJogo)) {
			int min = 15;
			if (Carro.TIPO_PNEU_MOLE.equals(getCarro().getTipoPneu())
					&& !controleJogo.asfaltoAbrasivo()) {
				min = 10;
			}
			if (Carro.TIPO_PNEU_DURO.equals(getCarro().getTipoPneu())
					&& controleJogo.asfaltoAbrasivo()) {
				min = 10;
			}
			if (pontoDerrapada != null
					&& distanciaDerrapada > Carro.RAIO_DERRAPAGEM) {
				valorLimiteStressePararErrarCurva = 100;
			}
			boolean maxPilotagem = !getNoAtual().verificaRetaOuLargada()
					&& porcentagemCombustivel < porcentagemDesgastePeneus
					&& porcentagemDesgastePeneus > min
					&& stress < valorLimiteStressePararErrarCurva;
			boolean maxPilotagemFinal = !getNoAtual().verificaRetaOuLargada()
					&& controleJogo.verificaUltimasVoltas()
					&& porcentagemDesgastePeneus > min
					&& stress < valorLimiteStressePararErrarCurva;
			if (maxPilotagemFinal || maxPilotagem) {
				setModoPilotagem(AGRESSIVO);
				if (carroPilotoDaFrente != null) {
					memsagemTentaPasssar(controleJogo, carroPilotoDaFrente);
				}
			}
		}
	}

	private void memsagemTentaPasssar(InterfaceJogo controleJogo,
			Carro carroPilotoDaFrente) {
		if (Math.random() < controleJogo.obterIndicativoCorridaCompleta()
				&& controleJogo.verificaInfoRelevante(this)
				&& msgTentativaNumVolta == getNumeroVolta()) {
			int val = Util.intervalo(1, 4);
			msgTentativaNumVolta = getNumeroVolta() + val;
			String txt = "";
			switch (val) {

			case 1:
				txt = Lang.msg(
						"048",
						new String[] {
								Html.bold(getNome()),
								Html.bold(carroPilotoDaFrente.getPiloto()
										.getNome()) });
				controleJogo.info(Html.silver(txt));
				break;
			case 2:
				txt = Lang.msg(
						"049",
						new String[] {
								Html.bold(getNome()),
								Html.bold(carroPilotoDaFrente.getPiloto()
										.getNome()) });
				controleJogo.info(Html.silver(txt));
				break;
			case 3:
				txt = Lang.msg(
						"050",
						new String[] {
								Html.bold(getNome()),
								Html.bold(carroPilotoDaFrente.getPiloto()
										.getNome()) });
				controleJogo.info(Html.silver(txt));
				break;
			case 4:
				txt = Lang.msg(
						"051",
						new String[] {
								Html.bold(getNome()),
								Html.bold(carroPilotoDaFrente.getPiloto()
										.getNome()) });
				controleJogo.info(Html.silver(txt));
				break;

			default:
				break;
			}
		}
	}

	private void verificaMudancaRegime(InterfaceJogo controleJogo) {
		if (verificaDesconcentrado()) {
			agressivo = false;
			return;
		}
		boolean novoModoAgressivo = agressivo;
		boolean qualyJogHumano = isJogadorHumano()
				|| controleJogo.isModoQualify();
		if (testeHabilidadePiloto(controleJogo)) {
			if (carro.verificaPilotoNormal(controleJogo) && !qualyJogHumano) {
				novoModoAgressivo = false;
				if (!Messagens.PILOTO_EM_CAUTELA.equals(msgsBox
						.get(Messagens.PILOTO_EM_CAUTELA))
						&& controleJogo.verificaInfoRelevante(this)) {
					controleJogo
							.info(Html.superRed(getNome() + Lang.msg("057")));
					msgsBox.put(Messagens.PILOTO_EM_CAUTELA,
							Messagens.PILOTO_EM_CAUTELA);
				}
			} else if (!getNoAtual().verificaRetaOuLargada()
					&& !getNoAtual().isBox()) {
				if (!qualyJogHumano && controleJogo.verificaNivelJogo()) {
					if (testeHabilidadePiloto(controleJogo)) {
						novoModoAgressivo = true;
					} else {
						novoModoAgressivo = false;
					}
				} else {
					novoModoAgressivo = false;
				}
			} else {
				novoModoAgressivo = true;
			}

		} else {
			if (No.CURVA_BAIXA.equals(getNoAtual().getTipo())) {
				novoModoAgressivo = false;
				incStress(5);
			} else if (No.CURVA_ALTA.equals(getNoAtual().getTipo())) {
				incStress(3);
			}
		}
		if (!qualyJogHumano && controleJogo.isSafetyCarNaPista()) {
			novoModoAgressivo = false;
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
		}
		if (isJogadorHumano() && !controleJogo.isModoQualify()) {
			if (!testeHabilidadePilotoHumanoCarro(controleJogo)
					&& !controleJogo.isSafetyCarNaPista()
					&& Math.random() < controleJogo.getNiveljogo()) {
				if (AGRESSIVO.equals(getModoPilotagem())) {
					if (controleJogo.isChovendo() && Math.random() > 0.970) {
						controleJogo.info(Html.bold(getNome())
								+ Html.bold(Lang.msg("052")));
					} else if (Math.random() > 0.97
							&& getNoAtual().verificaCruvaBaixa()) {

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
							controleJogo.info(Html.bold(getNome())
									+ Html.red(Lang.msg("055")));
						} else {
							controleJogo.info(Html.bold(getNome())
									+ Html.red(Lang.msg("056")));
						}
					}
				}
			}
			if (AGRESSIVO.equals(getModoPilotagem())) {
				novoModoAgressivo = true;
			}
			if (LENTO.equals(getModoPilotagem())) {
				novoModoAgressivo = false;
			}
		}
		agressivo = novoModoAgressivo;
	}

	private boolean testeHabilidadePilotoHumanoCarro(InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		if (Math.random() < controleJogo.getNiveljogo()) {
			return false;
		}
		return carro.testePotencia() && testeHabilidadePiloto(controleJogo);
	}

	public boolean verificaDesconcentrado() {
		return ciclosDesconcentrado > 0;
	}

	public boolean decremetaPilotoDesconcentrado(InterfaceJogo interfaceJogo) {
		if (colisao != null) {
			return false;
		}
		if (ciclosDesconcentrado <= 0) {
			ciclosDesconcentrado = 0;
			return false;
		}
		double val = (interfaceJogo.getTempoCiclo() / 80);
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

	private int calcularNovoModificador(InterfaceJogo controleJogo) {

		double bonusSecundario = 0.5;

		if (Carro.GIRO_MAX_VAL == getCarro().getGiro()) {
			bonusSecundario += getCarro().testePotencia() ? 0.2 : 0.1;
		}
		if (Carro.GIRO_MIN_VAL == getCarro().getGiro()
				&& !getNoAtual().verificaRetaOuLargada()) {
			bonusSecundario -= getCarro().testePotencia() ? 0.1 : 0.2;
		}
		if (controleJogo.isChovendo()) {
			bonusSecundario -= 0.1;
		}
		if (getNoAtual().verificaRetaOuLargada() && getCarro().testePotencia()) {
			acelerando = true;
			return (Math.random() < bonusSecundario ? 4 : 3);
		} else if (getNoAtual().verificaRetaOuLargada()
				&& getCarro().testePotencia() && getCarro().testeAerodinamica()) {
			acelerando = true;
			return (Math.random() < bonusSecundario ? 3 : 2);
		} else if (getNoAtual().verificaCruvaAlta()
				&& getCarro().testePotencia() && agressivo) {
			acelerando = true;
			return (Math.random() < bonusSecundario ? 3 : 2);
		} else if (getNoAtual().verificaCruvaAlta() && !agressivo
				&& testeHabilidadePilotoAerodinamica(controleJogo)) {
			acelerando = false;
			return (Math.random() < bonusSecundario ? 3 : 1);
		} else if (getNoAtual().verificaCruvaBaixa() && agressivo
				&& testeHabilidadePilotoAerodinamica(controleJogo)
				&& testeHabilidadePilotoFreios(controleJogo)) {
			acelerando = false;
			return (Math.random() < bonusSecundario ? 2 : 1);
		} else if (getNoAtual().verificaCruvaBaixa()
				&& testeHabilidadePilotoFreios(controleJogo)) {
			acelerando = false;
			return 1;
		} else {
			acelerando = false;
			return (Math.random() < bonusSecundario) ? 1 : 0;
		}
	}

	public boolean testeHabilidadePilotoCarro(InterfaceJogo interfaceJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testePotencia() && testeHabilidadePiloto(interfaceJogo);
	}

	public boolean testeHabilidadePiloto(InterfaceJogo interfaceJogo) {
		if (danificado() || verificaDesconcentrado()) {
			return false;
		}
		boolean teste = Math.random() < (habilidade / 1000.0);
		return teste;
	}

	public boolean danificado() {
		return carro.verificaDano();
	}

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

	public void processaVoltaNovaBox(InterfaceJogo interfaceJogo) {
		if (getVoltaAtual() == null) {
			Volta volta = new Volta();
			volta.setCiclosInicio(System.currentTimeMillis()
					- (getPtosBox() * interfaceJogo.getTempoCiclo()));
			setVoltaAtual(volta);

			return;
		}
		Volta volta = getVoltaAtual();

		volta.setCiclosFim(System.currentTimeMillis());
		interfaceJogo.descontaTempoPausado(volta);
		volta.setVoltaBox(true);
		setUltimaVolta(volta);
		voltas.add(volta);
		volta = new Volta();
		volta.setCiclosInicio(System.currentTimeMillis()
				- (getPtosBox() * interfaceJogo.getTempoCiclo()));
		setVoltaAtual(volta);
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
				msgsBox.put(Messagens.BOX_OCUPADO, null);
				msgsBox.put(Messagens.PILOTO_EM_CAUTELA, null);
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
			novoLado = interfaceJogo.getCircuito().getLadoBoxSaidaBox() == 1 ? 2
					: 1;
		}
		mudarTracado(novoLado, interfaceJogo, true);
		boxBaixoRendimento = false;
		if (getNumeroVolta() > 0)
			processaVoltaNovaBox(interfaceJogo);
	}

	public String obterTempoVoltaAtual() {
		if (voltaAtual == null) {
			return "";
		}

		return voltaAtual.obterTempoVoltaFormatado();
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
		carro.abandonou();

	}

	public Volta getMelhorVolta() {
		return melhorVolta;
	}

	public void setMelhorVolta(Volta melhorVolta) {
		this.melhorVolta = melhorVolta;
	}

	public int calculaDiffParaProximo(InterfaceJogo controleJogo) {
		return controleJogo.calculaDiferencaParaProximo(this);

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
		} else {
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

	public void mudarTracado(int pos, InterfaceJogo interfaceJogo) {
		mudarTracado(pos, interfaceJogo, testeHabilidadePiloto(interfaceJogo));
	}

	public boolean mudarTracado(int mudarTracado, InterfaceJogo interfaceJogo,
			boolean mesmoEmCurva) {
		if (indiceTracado != 0) {
			return false;
		}
		if (getTracado() == 4 && (mudarTracado == 0 || mudarTracado == 1)) {
			return false;
		}
		if (getTracado() == 5 && (mudarTracado == 0 || mudarTracado == 2)) {
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
		if (getTracado() == mudarTracado) {
			return false;
		}
		long agora = System.currentTimeMillis();
		if (getTracado() != 4
				&& getTracado() != 5
				&& (agora - ultimaMudancaPos) < (interfaceJogo.getTempoCiclo() * 10)) {
			return false;
		}
		if (getTracado() == 1 && mudarTracado == 2) {
			return false;
		}
		if (getTracado() == 2 && mudarTracado == 1) {
			return false;
		}
		if (!mesmoEmCurva) {
			if ((No.CURVA_BAIXA.equals(getNoAtual().getTipo()) && !testeHabilidadePilotoCarro(interfaceJogo))) {
				return false;
			}
		}
		if (!verificaColisaoAoMudarDeTracado(interfaceJogo, mudarTracado)) {
			double mod = Carro.ALTURA;
			if (getTracado() == 0 && (mudarTracado == 4 || mudarTracado == 5)) {
				mod *= 3.5;
			} else if ((getTracado() == 1 || getTracado() == 2)
					&& (mudarTracado == 4 || mudarTracado == 5)) {
				mod *= 2;
			} else if ((getTracado() == 5 || getTracado() == 4)
					&& (mudarTracado == 2 || mudarTracado == 1)) {
				mod *= 2;
			}
			setTracadoAntigo(getTracado());
			setTracado(mudarTracado);

			double novoIndice = (mod * interfaceJogo.getCircuito()
					.getMultiplicadorLarguraPista());

			if (getPtosBox() != 0) {
				novoIndice *= 0.7;
			}
			setIndiceTracado((int) novoIndice);

			ultimaMudancaPos = System.currentTimeMillis();
			return true;
		} else {
			ultimaMudancaPos = System.currentTimeMillis()
					+ (interfaceJogo.getTempoCiclo() * 20);
		}
		return false;

	}

	public int decIndiceTracado() {
		return decIndiceTracado(1);
	}

	public int decIndiceTracado(int decExtra) {
		if (indiceTracado <= 0) {
			return 0;
		}
		if (getPtosBox() != 0) {
			indiceTracado--;
		} else if (indiceTracado % 2 == 0) {
			indiceTracado -= 3;
		} else {
			indiceTracado -= 2;
		}
		if (getTracado() == 4 || getTracado() == 5) {
			indiceTracado -= decExtra;
		} else {
			if (noAtual.verificaRetaOuLargada()
					&& getCarro().testeAerodinamica()) {
				indiceTracado -= decExtra;
			}
		}
		if (indiceTracado <= 0) {
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

	private boolean verificaColisaoAoMudarDeTracado(InterfaceJogo controleJogo,
			int pos) {
		if (getPtosBox() != 0) {
			return false;
		}
		int indice = getNoAtual().getIndex();
		List pilotos = controleJogo.getPilotosCopia();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (this.equals(piloto) || piloto.getTracado() == getTracado()) {
				continue;
			}

			int indiceCarro = piloto.getNoAtual().getIndex();

			int traz = indiceCarro - Carro.LARGURA;
			int frente = indiceCarro + Carro.LARGURA;

			List lista = piloto.obterPista(controleJogo);

			if (traz < 0) {
				traz = (lista.size() - 1) + traz;
			}
			if (frente > (lista.size() - 1)) {
				frente = (frente - (lista.size() - 1)) - 1;
			}

			if (indice >= traz && indice <= frente) {
				return true;
			}
		}
		return false;

	}

	public int getPtosPistaIncial() {
		return ptosPistaIncial;
	}

	public void setPtosPistaIncial(int ptosPistaIncial) {
		this.ptosPistaIncial = ptosPistaIncial;
	}

	public void mudarAutoTracado() {
		autoPos = !autoPos;
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

	public boolean adicionaVotoDriveThru(String nomeJogador, List piList) {
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

	public int getTracadoDelay() {
		return tracadoDelay;
	}

	public void setTracadoDelay(int tracadoDelay) {
		this.tracadoDelay = tracadoDelay;
	}

	public long getIndexTracadoDelay() {
		return indexTracadoDelay;
	}

	public void setIndexTracadoDelay(long indexTracadoDelay) {
		this.indexTracadoDelay = indexTracadoDelay;
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
		return carro.testeAerodinamica() && testeHabilidadePiloto(controleJogo);
	}

	public boolean testeHabilidadePilotoFreios(InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testeFreios() && testeHabilidadePiloto(controleJogo);
	}

	public boolean testeHabilidadePilotoAerodinamicaFreios(
			InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		return carro.testeFreios() && carro.testeAerodinamica()
				&& testeHabilidadePiloto(controleJogo);
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

	public int getDiferencaParaProximoRetardatario() {
		return diferencaParaProximoRetardatario;
	}

	public boolean isColisaoDiantera() {
		return colisaoDiantera;
	}

	public boolean isColisaoCentro() {
		return colisaoCentro;
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

	public double getMedGanhosBaixa() {
		return mediaLista(ganhosBaixa);
	}

	public double getMedGanhosAlta() {
		return mediaLista(ganhosAlta);
	}

	public double getMedGanhosReta() {
		return mediaLista(ganhosReta);
	}

	private double mediaLista(List list) {
		if (list == null) {
			return 0;
		}
		double total = 0;
		synchronized (list) {
			for (Iterator iterator = list.iterator(); iterator.hasNext();) {
				total += (Double) iterator.next();
			}
		}
		return total / list.size();
	}

	public double getDistanciaDerrapada() {
		return distanciaDerrapada;
	}

	public String getVantagem() {
		return vantagem;
	}

	public void setVantagem(String vantagem) {
		this.vantagem = vantagem;
	}

}
