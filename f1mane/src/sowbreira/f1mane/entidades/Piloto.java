package sowbreira.f1mane.entidades;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
	private int aceleracao = 7;
	private static final double FATOR_AREA_CARRO = .7;
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
	private int velocidadeAnterior;
	private int velocidadeLargada;
	private transient String setUpIncial;
	private String nome;
	private String nomeCarro;
	private String nomeJogador;
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
	private boolean autoPos = true;
	private Point p1;
	private Point p2;
	private Double angulo;
	private transient int ptosBox;
	private int posicao;
	private transient int paradoBox;
	private int qtdeParadasBox;
	private boolean desqualificado;
	private boolean jogadorHumano;
	private transient boolean recebeuBanderada;
	private boolean box;
	private boolean agressivo = true;
	private Carro carro = new Carro();
	private No noAtual = new No();
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
	private int qtdeCombustBox;
	private long parouNoBoxMilis;
	private long saiuDoBoxMilis;
	private int msgTentativaNumVolta = 2;
	private ArrayList listGanho;
	private long ultimaMudancaPos;
	private double ganho;
	private boolean ativarKers;
	private int cargaKersVisual;
	private int cargaKersOnline;
	private boolean ativarDRS;
	private int novoModificador;
	private Set votosDriveThru = new HashSet();
	private boolean driveThrough;
	private long timeStampChegeda;
	private boolean cruzouLargada = false;
	private List ganhosBaixa = new ArrayList();
	private List ganhosAlta = new ArrayList();
	private List ganhosReta = new ArrayList();
	private boolean travouRodas;
	private int contTravouRodas;

	public boolean decContTravouRodas() {
		if (contTravouRodas > 0) {
			contTravouRodas--;
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
			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss .S");
			if (this.getPosicao() == 1) {
				controleJogo.infoPrioritaria(Html.superBlack(getNome())
						+ Html.superGreen(Lang.msg("044", new Object[] {
								getPosicao(),
								df.format(new Date(getTimeStampChegeda())) })));
			} else {
				controleJogo.info(Html.superBlack(getNome())
						+ Html.green(Lang.msg("044", new Object[] {
								getPosicao(),
								df.format(new Date(getTimeStampChegeda())) })));
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
			System.out.println("Bandeirada " + this + " Pts pista "
					+ this.getPtosPista() + " Pos " + getPosicao() + " T "
					+ df.format(new Date()));
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
		this.ciclosDesconcentrado = ciclosDelay;
	}

	public boolean isAgressivo() {
		return agressivo;
	}

	public void setAgressivoF4(boolean regMaximo) {
		this.agressivo = regMaximo;
	}

	public void setAgressivo(boolean regMaximo, InterfaceJogo interfaceJogo) {
		if (regMaximo && verificaPilotoDesconcentrado(interfaceJogo)) {
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
		this.noAtual = no;
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
		int index = calcularNovoIndex(controleJogo);
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
			if (!controleJogo.isModoQualify()) {
				Logger.logar(" Numero Volta " + getNumeroVolta() + " "
						+ getNome() + " Pos " + getPosicao() + " Pts "
						+ getPtosPista());
			}

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
					ultsConsumosPneu.add(ultimoConsumoCombust.intValue()
							- pPneu);
					ultimoConsumoPneu = new Integer(pPneu);
				}
			}

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

			if (numeroVolta > controleJogo.totalVoltasCorrida()) {
				numeroVolta = controleJogo.totalVoltasCorrida();
				return;
			}
		}

		if (controleJogo.isCorridaTerminada()) {
			int indexPiloto = getNoAtual().getIndex();
			int tamPista = controleJogo.getNosDaPista().size();
			if (indexPiloto < 100) {
				setRecebeuBanderada(true, controleJogo);
			}
		}

		verificaIrBox(controleJogo);
		this.setNoAtual((No) pista.get(index));
	}

	private void verificaIrBox(InterfaceJogo controleJogo) {
		if (jogadorHumano || recebeuBanderada || getPtosPista() < 0) {
			return;
		}
		int pneus = getCarro().porcentagemDesgastePeneus();
		int combust = getCarro().porcentagemCombustivel();
		if (controleJogo.isSemReabastacimento()) {
			combust = 100;
		}
		if (box && controleJogo.verificaBoxOcupado(getCarro())) {
			if (!Messagens.BOX_OCUPADO.equals(msgsBox
					.get(Messagens.BOX_OCUPADO))) {
				if (getPosicao() < 9) {
					controleJogo.info(Html.orange(Lang.msg("046",
							new String[] { Html.bold(getNome()) })));
				}
				msgsBox.put(Messagens.BOX_OCUPADO, Messagens.BOX_OCUPADO);
			}
		}

		if ((combust < 5) && !controleJogo.isCorridaTerminada()) {
			box = true;
		} else {
			box = false;
		}

		double consumoMedioPneus = calculaConsumoMedioPneu();
		if (pneus < 2 * consumoMedioPneus
				&& (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu()) || Carro.TIPO_PNEU_CHUVA
						.equals(carro.getTipoPneu()))) {
			box = true;
		}
		int minPneu = 5;
		if (controleJogo.isBoxRapido()) {
			minPneu = 10;
		}

		if (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu())
				&& (pneus < minPneu)) {
			box = true;
		}

		if ((Carro.TIPO_PNEU_DURO.equals(carro.getTipoPneu()))
				&& (pneus < minPneu)) {
			box = true;
		}

		if (Carro.TIPO_PNEU_CHUVA.equals(carro.getTipoPneu())
				&& (pneus < minPneu)) {
			box = true;
		}

		if (controleJogo.asfaltoAbrasivo()) {
			if (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu())
					&& (pneus < 20)) {
				box = true;
			}
			if ((Carro.TIPO_PNEU_DURO.equals(carro.getTipoPneu()) || Carro.TIPO_PNEU_CHUVA
					.equals(carro.getTipoPneu()))
					&& (pneus < 20)) {
				box = true;
			}
		} else {
			if (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu())
					&& (pneus < 10)) {
				box = true;
			}
			if ((Carro.TIPO_PNEU_DURO.equals(carro.getTipoPneu()) || Carro.TIPO_PNEU_CHUVA
					.equals(carro.getTipoPneu()))
					&& (pneus < 30)) {
				box = true;
			}
		}

		if (controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isSafetyCarVaiBox()) {
			if (combust < 20 || pneus < 50) {
				box = true;
			}

		}

		if (carro.verificaPneusIncompativeisClima(controleJogo)) {
			box = true;
		}

		if (controleJogo.verificaUltimasVoltas() && (combust <= 5)
				&& (pneus <= 5)) {
			controleJogo.info(Html.orange(Lang.msg("047",
					new String[] { getNome() })));
			msgsBox.put(Messagens.IR_BOX_FINAL_CORRIDA,
					Messagens.IR_BOX_FINAL_CORRIDA);
			box = false;
		}

		if (carro.verificaDano()) {
			box = true;
		}

		if (controleJogo.verificaUltimasVoltas()) {
			box = false;
		}
		if (controleJogo.getNumVoltaAtual() < 1) {
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

	private int calcularNovoIndex(InterfaceJogo controleJogo) {
		int index = noAtual.getIndex();
		int fator = 0;
		if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
			fator = (Util.intervalo(1, 2));
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
			fator = (Util.intervalo(0, 2));
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			fator = (Util.intervalo(0, 1));
		}
		if (NORMAL.equals(modoPilotagem) || !agressivo) {
			decStress(fator);
		} else if (LENTO.equals(modoPilotagem)) {
			decStress(fator * 2);
		}
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
		if (getCarro().isPaneSeca()) {
			desqualificado = true;
			controleJogo.infoPrioritaria(Html.txtRedBold(getNome()
					+ Lang.msg("118")));
		}
		processaIAnovoIndex(controleJogo);

		novoModificador = calcularNovoModificador(controleJogo);
		novoModificador = getCarro().calcularModificadorCarro(novoModificador,
				agressivo, noAtual, controleJogo);

		if (novoModificador > 5) {
			novoModificador = 5;
		} else if (novoModificador < 1) {
			novoModificador = 1;
		}

		if (danificado()) {
			if (Carro.PNEU_FURADO.equals(getCarro().getDanificado())
					|| (Carro.PERDEU_AEREOFOLIO.equals(getCarro()
							.getDanificado()) && !noAtual
							.verificaRetaOuLargada()))
				novoModificador = Util.intervalo(1, 2);
		}

		ganho = ((novoModificador * controleJogo.getCircuito()
				.getMultiplciador()) * (controleJogo.getIndexVelcidadeDaPista()));
		if (!controleJogo.isModoQualify()) {
			ganho = controleJogo.verificaUltraPassagem(this, ganho);
		}

		if (controleJogo.isKers() && ativarKers && getPtosBox() == 0
				&& getNumeroVolta() > 0) {
			if (getCarro().getCargaKers() <= 0) {
				ativarKers = false;
			} else {
				ganho *= 1.2;
				getCarro().usaKers();
			}
		}

		if (controleJogo.isDrs() && ativarDRS && getPtosBox() == 0
				&& getNumeroVolta() > 0) {
			if (controleJogo.calculaSegundosParaProximoDouble(this) < 1) {
				getCarro().setAsa(Carro.MENOS_ASA);
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

		/**
		 * Controla Efeito turbulencia e ultrapassagens usando tracado
		 */
		if (!controleJogo.isModoQualify() || !controleJogo.isSafetyCarNaPista()) {
			Carro carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
			if (carroPilotoDaFrente != null) {
				double diff = calculaDiffParaProximo(controleJogo);
				double distBrigaMin = 30;
				double nGanho = (controleJogo.getFatorUtrapassagem());
				if (diff < distBrigaMin) {
					if (getTracado() != carroPilotoDaFrente.getPiloto()
							.getTracado()) {
						if (No.CURVA_ALTA.equals(noAtual.getTipo())
								|| No.CURVA_BAIXA.equals(noAtual.getTipo())) {
							boolean pass = Math.random() > controleJogo
									.getFatorUtrapassagem();
							if (isAgressivo() && pass) {
								nGanho += 0.1;
							}
							if (Carro.GIRO_MAX_VAL == getCarro().getGiro()
									&& pass) {
								nGanho += 0.1;
							}
						}
					} else {
						No noDafrente = carroPilotoDaFrente.getPiloto()
								.getNoAtual();
						if (No.CURVA_ALTA.equals(noDafrente)
								|| No.CURVA_BAIXA.equals(noDafrente)) {
							if (controleJogo.isChovendo()
									&& Math.random() > 0.5) {
								nGanho = 1;
							} else {
								nGanho = distBrigaMin / (diff * 1.5);
							}
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
		}
		boolean colisao = false;
		if (!controleJogo.isModoQualify()
				&& verificaColisaoCarroFrente(controleJogo)) {
			colisao = true;
		}
		if (colisao) {
			setAgressivoF4(false);
			setCiclosDesconcentrado(200);
		}

		int indexAtual = noAtual.getIndex();
		/**
		 * efeito freiar na reta
		 */
		if (indexAtual + 70 < (controleJogo.getNosDaPista().size() - 1)) {
			No no = controleJogo.getNosDaPista().get(indexAtual + 70);
			if (no.verificaCruvaBaixa() && noAtual.verificaRetaOuLargada()) {
				ganho *= testeHabilidadePiloto(controleJogo) ? 0.6 : 0.8;
			}
		}

		ganho = calculaGanhoMedio(ganho);

		if (controleJogo.isSafetyCarNaPista()) {
			ganho = controleJogo.ganhoComSafetyCar(ganho, controleJogo, this);
		}
		if (!controleJogo.isSafetyCarNaPista() && ganho > 0 && ganho < 1) {
			ganho = 1;
		}
		if (ganho > 60) {
			ganho = 60;
		}

		setPtosPista(Util.inte(getPtosPista() + ganho));

		if (noAtual.verificaCruvaBaixa()) {
			ganhosBaixa.add(ganho);
		}
		if (noAtual.verificaCruvaAlta()) {
			ganhosAlta.add(ganho);
		}
		if (noAtual.verificaRetaOuLargada()) {
			ganhosReta.add(ganho);
		}
		index += Math.round(ganho);
		setVelocidade(Util.inte(ganho * 5.25));
		if (ganho > ganhoMax) {
			ganhoMax = ganho;
		}
		return index;
	}

	private void processaIAnovoIndex(InterfaceJogo controleJogo) {
		verificaMudancaRegime(controleJogo);
		if (!controleJogo.isModoQualify() && !controleJogo.isSafetyCarNaPista()) {
			tentarPassaPilotoDaFrente(controleJogo);
			tentarEscaparPilotoDaTraz(controleJogo);
		}
		if (!isJogadorHumano() && !controleJogo.isModoQualify()
				&& controleJogo.isKers() && !controleJogo.isSafetyCarNaPista()
				&& getPtosBox() == 0) {
			tentaUsarKers(controleJogo);
		}

		if ((!isJogadorHumano() || controleJogo.isModoQualify())
				&& controleJogo.isDrs() && !controleJogo.isSafetyCarNaPista()
				&& getPtosBox() == 0) {
			tentaUsarDRS(controleJogo);
		}
	}

	private void mudarTracadoDiferenteDe(int trac, InterfaceJogo controleJogo) {
		if (trac == 0) {
			mudarTracado(Util.intervalo(1, 2), controleJogo);
		} else {
			mudarTracado(0, controleJogo);
		}

	}

	private void tentarEscaparPilotoDaTraz(InterfaceJogo controleJogo) {
		if (jogadorHumano || danificado() || getPtosBox() != 0) {
			return;
		}
		if (ControleQualificacao.modoQualify) {
			return;
		}
		int calculaDiferencaParaAnterior = controleJogo
				.calculaDiferencaParaAnterior(this);
		if (calculaDiferencaParaAnterior < Util.intervalo(50 * controleJogo
				.getNiveljogo(), 75 * controleJogo.getNiveljogo())
				&& Math.random() < controleJogo.getNiveljogo()
				&& testeHabilidadePiloto(controleJogo)) {
			setAgressivo(true, controleJogo);
			int porcentagemCombustivel = getCarro().porcentagemCombustivel();
			int porcentagemDesgastePeneus = getCarro()
					.porcentagemDesgastePeneus();
			if (!testeHabilidadePiloto(controleJogo)) {
				porcentagemCombustivel = 0;
				porcentagemDesgastePeneus = 0;
			}
			if (Math.abs((porcentagemCombustivel - porcentagemDesgastePeneus)) < 20) {
				getCarro().setGiro(Carro.GIRO_MAX_VAL);
			}
		}
	}

	private void tentaUsarDRS(InterfaceJogo controleJogo) {
		if (getNoAtual().verificaRetaOuLargada()
				&& Math.random() < (controleJogo.getNiveljogo() - 0.1)
				&& testeHabilidadePiloto(controleJogo)) {
			ativarDRS = true;
		} else {
			ativarDRS = false;
		}
	}

	private void tentaUsarKers(InterfaceJogo controleJogo) {
		int calculaDiferencaParaAnterior = controleJogo
				.calculaDiferencaParaAnterior(this);
		if (calculaDiferencaParaAnterior < Util.intervalo(20, 30)
				&& Math.random() < controleJogo.getNiveljogo()
				&& testeHabilidadePiloto(controleJogo)) {
			ativarKers = true;
		} else {
			ativarKers = false;
		}
		if (controleJogo.percetagemDeVoltaCompletada(this) > 75) {
			ativarKers = true;
		}

	}

	public double getGanho() {
		return ganho;
	}

	public boolean verificaColisaoCarroFrente(InterfaceJogo controleJogo) {
		return verificaColisaoCarroFrente(controleJogo, false);
	}

	public boolean verificaColisaoCarroFrente(InterfaceJogo controleJogo,
			boolean somenteVerifica) {
		try {
			boolean verificaNoPitLane = controleJogo.verificaNoPitLane(this);
			if (verificaNoPitLane) {
				return false;
			}
			List pilotos = controleJogo.getPilotos();
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				boolean verificaNoPitLaneOutro = controleJogo
						.verificaNoPitLane(piloto);
				if (this.equals(piloto)) {
					continue;
				}
				if (verificaNoPitLaneOutro) {
					continue;
				}
				if (!somenteVerifica) {
					centralizaCarro(controleJogo);
					piloto.centralizaCarro(controleJogo);
				}
				boolean intercecionou = getDiateira().intersects(
						piloto.getTrazeira())
						|| getDiateira().intersects(piloto.getCentro())
						|| getDiateira().intersects(piloto.getDiateira())
						|| getCentro().intersects(piloto.getTrazeira())
						|| getCentro().intersects(piloto.getCentro())
						|| getCentro().intersects(piloto.getDiateira());
				boolean msmPista = obterPista(controleJogo).size() == piloto
						.obterPista(controleJogo).size();
				boolean msmTracado = piloto.getTracado() == getTracado();
				msmPista = msmPista && msmTracado;
				if (intercecionou && msmPista) {
					if (piloto.getCarro().isPaneSeca()
							|| piloto.getCarro().isRecolhido()) {
						return false;
					}
					if (!somenteVerifica) {
						if (getPtosBox() == 0 && piloto.getPtosBox() == 0) {
							controleJogo.verificaAcidenteUltrapassagem(this
									.isAgressivo(), this, piloto);
						}
						if (piloto.isDesqualificado()
								|| (!Util.isNullOrEmpty(piloto.getCarro()
										.getDanificado()) && (Carro.BATEU_FORTE
										.equals(piloto.getCarro()
												.getDanificado())
										|| Carro.PERDEU_AEREOFOLIO
												.equals(piloto.getCarro()
														.getDanificado()) || Carro.PNEU_FURADO
										.equals(piloto.getCarro()
												.getDanificado())))) {
							mudarTracado(obterNovoTracadoPossivel(),
									controleJogo, true);
						}
					}
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			Logger.logarExept(e);
			return false;
		}
	}

	public Rectangle2D centralizaCarro(InterfaceJogo controleJogo) {
		No noAtual = getNoAtual();
		Point p = noAtual.getPoint();

		List lista = obterPista(controleJogo);

		if (lista == null) {
			return null;
		}

		int cont = noAtual.getIndex();

		int carx = p.x;
		int cary = p.y;

		int traz = cont - 44;
		int frente = cont + 44;

		if (traz < 0) {
			traz = (lista.size() - 1) + traz;
		}
		if (frente > (lista.size() - 1)) {
			frente = (frente - (lista.size() - 1)) - 1;
		}

		Point trazCar = ((No) lista.get(traz)).getPoint();
		Point frenteCar = ((No) lista.get(frente)).getPoint();
		double calculaAngulo = GeoUtil.calculaAngulo(frenteCar, trazCar, 0);
		Rectangle2D rectangle = new Rectangle2D.Double((p.x - Carro.MEIA_ALTURA
				* FATOR_AREA_CARRO), (p.y - Carro.MEIA_ALTURA
				* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
				Carro.ALTURA * FATOR_AREA_CARRO);
		Point p1 = GeoUtil.calculaPonto(calculaAngulo, Util.inte(Carro.ALTURA
				* controleJogo.getCircuito().getMultiplicadorLarguraPista()),
				new Point(Util.inte(rectangle.getCenterX()), Util
						.inte(rectangle.getCenterY())));
		Point p2 = GeoUtil.calculaPonto(calculaAngulo + 180, Util
				.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()), new Point(
				Util.inte(rectangle.getCenterX()), Util.inte(rectangle
						.getCenterY())));
		if (getTracado() == 0) {
			carx = p.x;
			cary = p.y;
		}
		if (getTracado() == 1) {
			carx = Util.inte((p1.x));
			cary = Util.inte((p1.y));
		}
		if (getTracado() == 2) {
			carx = Util.inte((p2.x));
			cary = Util.inte((p2.y));
		}
		rectangle = new Rectangle2D.Double((carx - Carro.MEIA_ALTURA
				* FATOR_AREA_CARRO), (cary - Carro.MEIA_ALTURA
				* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
				Carro.ALTURA * FATOR_AREA_CARRO);
		setCarX(carx);
		setCarY(cary);
		setCentro(rectangle.getBounds());

		Rectangle2D trazRec = new Rectangle2D.Double(
				(trazCar.x - Carro.MEIA_ALTURA * FATOR_AREA_CARRO),
				(trazCar.y - Carro.MEIA_ALTURA * FATOR_AREA_CARRO),
				Carro.ALTURA * FATOR_AREA_CARRO, Carro.ALTURA
						* FATOR_AREA_CARRO);
		Point p1Traz = GeoUtil.calculaPonto(calculaAngulo, Util
				.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()), new Point(
				Util.inte(trazRec.getCenterX()), Util
						.inte(trazRec.getCenterY())));
		Point p2Traz = GeoUtil.calculaPonto(calculaAngulo + 180, Util
				.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()), new Point(
				Util.inte(trazRec.getCenterX()), Util
						.inte(trazRec.getCenterY())));
		if (getTracado() == 1) {
			trazRec = new Rectangle2D.Double((p1Traz.x - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), (p1Traz.y - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
					Carro.ALTURA * FATOR_AREA_CARRO);
		}
		if (getTracado() == 2) {
			trazRec = new Rectangle2D.Double((p2Traz.x - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), (p2Traz.y - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
					Carro.ALTURA * FATOR_AREA_CARRO);
		}
		setTrazeira(trazRec.getBounds());

		Rectangle2D frenteRec = new Rectangle2D.Double(
				(frenteCar.x - Carro.MEIA_ALTURA * FATOR_AREA_CARRO),
				(frenteCar.y - Carro.MEIA_ALTURA * FATOR_AREA_CARRO),
				Carro.ALTURA * FATOR_AREA_CARRO, Carro.ALTURA
						* FATOR_AREA_CARRO);
		Point p1Frente = GeoUtil.calculaPonto(calculaAngulo, Util
				.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()), new Point(
				Util.inte(frenteRec.getCenterX()), Util.inte(frenteRec
						.getCenterY())));
		Point p2Frente = GeoUtil.calculaPonto(calculaAngulo + 180, Util
				.inte(Carro.ALTURA
						* controleJogo.getCircuito()
								.getMultiplicadorLarguraPista()), new Point(
				Util.inte(frenteRec.getCenterX()), Util.inte(frenteRec
						.getCenterY())));
		if (getTracado() == 1) {
			frenteRec = new Rectangle2D.Double((p1Frente.x - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), (p1Frente.y - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
					Carro.ALTURA * FATOR_AREA_CARRO);
		}
		if (getTracado() == 2) {
			frenteRec = new Rectangle2D.Double((p2Frente.x - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), (p2Frente.y - Carro.MEIA_ALTURA
					* FATOR_AREA_CARRO), Carro.ALTURA * FATOR_AREA_CARRO,
					Carro.ALTURA * FATOR_AREA_CARRO);
		}
		setDiateira(frenteRec.getBounds());

		return rectangle;
	}

	public List obterPista(InterfaceJogo controleJogo) {

		return controleJogo.obterPista(this);
	}

	public Piloto() {
		zerarGanho();
	}

	public void zerarGanho() {
		listGanho = new ArrayList();
		for (int i = 0; i < listGanho.size(); i++) {
			listGanho.add(0.0);
		}
	}

	public double calculaGanhoMedio(double ganho) {
		int acel = aceleracao;
		if (noAtual.verificaRetaOuLargada()) {
			acel++;
		} else {
			acel--;
		}
		if (listGanho.size() > acel) {
			listGanho.remove(0);
		}
		listGanho.add(ganho);
		double soma = 0;
		for (Iterator iterator = listGanho.iterator(); iterator.hasNext();) {
			Double val = (Double) iterator.next();
			soma += val.doubleValue();
		}
		return soma / listGanho.size();
	}

	private void tentarPassaPilotoDaFrente(InterfaceJogo controleJogo) {
		if (jogadorHumano || danificado()) {
			return;
		}
		if (ControleQualificacao.modoQualify) {
			return;
		}
		if (getStress() > (testeHabilidadePiloto(controleJogo) ? 90 : 70)) {
			return;
		}
		int diff = calculaDiffParaProximo(controleJogo);
		int size = controleJogo.getCircuito().getPistaFull().size();
		double multiplciador = controleJogo.getCircuito().getMultiplciador();
		size /= multiplciador;
		int distBrigaMax = (int) (size * (controleJogo.getNiveljogo() + 0.3));
		int distBrigaMin = 0;
		Carro carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
		if (carroPilotoDaFrente != null) {
			Piloto pilotoFrente = carroPilotoDaFrente.getPiloto();
			if (pilotoFrente.isJogadorHumano()) {
				if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
					distBrigaMin = 10;
				} else if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
					distBrigaMin = 5;
				}
			}
		}
		if (diff > distBrigaMin && diff < distBrigaMax
				&& testeHabilidadePiloto(controleJogo)) {
			if (carroPilotoDaFrente != null) {
				Piloto pilotoFrente = carroPilotoDaFrente.getPiloto();
				if (!pilotoFrente.entrouNoBox()) {
					getCarro().setGiro(Carro.GIRO_MAX_VAL);
					if (testeHabilidadePiloto(controleJogo)) {
						No no = getNoAtual();
						if (Carro.MAIS_ASA.equals(getCarro().getAsa())) {
							if ((no.verificaCruvaAlta() || no
									.verificaCruvaBaixa())) {
								int porcentagemCombustivel = getCarro()
										.porcentagemCombustivel();
								int porcentagemDesgastePeneus = getCarro()
										.porcentagemDesgastePeneus();
								if (!testeHabilidadePiloto(controleJogo)) {
									porcentagemCombustivel = 0;
									porcentagemDesgastePeneus = 0;
								}
								if (Math
										.abs((porcentagemCombustivel - porcentagemDesgastePeneus)) < 20) {
									getCarro().setGiro(Carro.GIRO_MAX_VAL);
								}
							}
							if (no.verificaRetaOuLargada()) {
								getCarro().setGiro(Carro.GIRO_NOR_VAL);
							}

						}
						if (Carro.MENOS_ASA.equals(getCarro().getAsa())) {
							if ((no.verificaCruvaAlta() || no
									.verificaCruvaBaixa())) {
								getCarro().setGiro(Carro.GIRO_NOR_VAL);
							}
							if (no.verificaRetaOuLargada()) {
								int porcentagemCombustivel = getCarro()
										.porcentagemCombustivel();
								int porcentagemDesgastePeneus = getCarro()
										.porcentagemDesgastePeneus();
								if (Math
										.abs((porcentagemCombustivel - porcentagemDesgastePeneus)) < 20) {
									getCarro().setGiro(Carro.GIRO_MAX_VAL);
								}
							}
						}
					}
					if (testeHabilidadePiloto(controleJogo)) {
						int porcentagemCombustivel = getCarro()
								.porcentagemCombustivel();
						int porcentagemDesgastePeneus = getCarro()
								.porcentagemDesgastePeneus();
						if (!testeHabilidadePiloto(controleJogo)) {
							porcentagemCombustivel = 0;
							porcentagemDesgastePeneus = 0;
						}
						if ((diff < 15 || !noAtual.verificaRetaOuLargada())
								&& Math
										.abs((porcentagemCombustivel - porcentagemDesgastePeneus)) < 20) {
							setModoPilotagem(AGRESSIVO);
						}
						if (Math.random() < controleJogo
								.obterIndicativoCorridaCompleta()
								&& Math.random() > .9
								&& getPosicao() < 9
								&& msgTentativaNumVolta == getNumeroVolta()) {

							int val = 1 + (int) (Math.random() * 4);
							msgTentativaNumVolta = getNumeroVolta() + val;
							String txt = "";
							switch (val) {

							case 1:
								txt = Lang.msg("048", new String[] {
										Html.bold(getNome()),
										Html.bold(carroPilotoDaFrente
												.getPiloto().getNome()) });
								controleJogo.info(Html.silver(txt));
								break;
							case 2:
								txt = Lang.msg("049", new String[] {
										Html.bold(getNome()),
										Html.bold(carroPilotoDaFrente
												.getPiloto().getNome()) });
								controleJogo.info(Html.silver(txt));
								break;
							case 3:
								txt = Lang.msg("050", new String[] {
										Html.bold(getNome()),
										Html.bold(carroPilotoDaFrente
												.getPiloto().getNome()) });
								controleJogo.info(Html.silver(txt));
								break;
							case 4:
								txt = Lang.msg("051", new String[] {
										Html.bold(getNome()),
										Html.bold(carroPilotoDaFrente
												.getPiloto().getNome()) });
								controleJogo.info(Html.silver(txt));
								break;

							default:
								break;
							}
						}
					} else {
						setModoPilotagem(NORMAL);
					}
				}
			}
		} else {
			getCarro().setGiro(Carro.GIRO_NOR_VAL);
		}
		if (getCarro().verificaCondicoesCautelaGiro(controleJogo)
				|| entrouNoBox()) {
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
		}

	}

	public static void main(String[] args) throws InterruptedException {
		// Logger.logar(1 + (int) (Math.random() * 3));
		// System.out.println(0.5 * 1.5);
		// System.out.println(Math.floor(4.9));
		// Long time = System.currentTimeMillis();
		// System.out.println(time.toString().length());
		// System.out.println(time);
		// time = new Long(time.toString().substring(
		// time.toString().length() / 10, time.toString().length()));
		// System.out.println(time);
		//
		// time *= 72;
		//
		// System.out.println(time);

		double d = 100;
		double s = 199;
		System.out.println("11,99".contains("."));
	}

	private void verificaMudancaRegime(InterfaceJogo controleJogo) {
		if (verificaPilotoDesconcentrado(controleJogo)) {
			if (!isJogadorHumano()) {
				agressivo = false;
			}
			return;
		}
		boolean novoModoAgressivo = agressivo;
		if (testeHabilidadePiloto(controleJogo)) {
			if (carro.verificaCondicoesCautela(controleJogo)
					&& !isJogadorHumano()) {
				novoModoAgressivo = false;
				if (!Messagens.PILOTO_EM_CAUTELA.equals(msgsBox
						.get(Messagens.PILOTO_EM_CAUTELA))
						&& getPosicao() <= 3) {
					controleJogo.info(Html
							.superRed(getNome() + Lang.msg("057")));
					msgsBox.put(Messagens.PILOTO_EM_CAUTELA,
							Messagens.PILOTO_EM_CAUTELA);
				}
			} else if (!noAtual.verificaRetaOuLargada() && !noAtual.isBox()) {
				if (!jogadorHumano && controleJogo.verificaNivelJogo()) {
					if (testeHabilidadePiloto(controleJogo)) {
						novoModoAgressivo = true;
					} else {
						novoModoAgressivo = false;
						setCiclosDesconcentrado(Util.intervalo(10, 50));
					}
				} else {
					novoModoAgressivo = false;
				}
			} else {
				novoModoAgressivo = true;
			}
			if (!jogadorHumano && controleJogo.isSafetyCarNaPista()) {
				novoModoAgressivo = false;
				getCarro().setGiro(Carro.GIRO_MIN_VAL);
			}
		} else {
			if (No.CURVA_BAIXA.equals(noAtual.getTipo())) {
				if (agressivo && (getTracado() == 0)
						&& (carro.porcentagemDesgastePeneus() < 30)) {
					if (getTracadoAntigo() != 0) {
						if (getTracadoAntigo() == 1) {
							mudarTracado(2, controleJogo, true);
						} else {
							mudarTracado(1, controleJogo, true);
						}
					} else {
						mudarTracado(Util.intervalo(1, 2), controleJogo, true);
					}
				}
				novoModoAgressivo = false;
				ciclosDesconcentrado = (int) (15 * controleJogo.getNiveljogo());
			} else if (No.CURVA_ALTA.equals(noAtual.getTipo())) {
				ciclosDesconcentrado = (int) (10 * controleJogo.getNiveljogo());
			} else {
				ciclosDesconcentrado = (int) (5 * controleJogo.getNiveljogo());
			}
			ciclosDesconcentrado *= (1 - controleJogo.getNiveljogo());
		}
		if (jogadorHumano) {
			if (!testeHabilidadePilotoHumanoCarro(controleJogo)
					&& !controleJogo.isSafetyCarNaPista()
					&& Math.random() < controleJogo.getNiveljogo()) {
				if (AGRESSIVO.equals(modoPilotagem)) {
					if (controleJogo.isChovendo() && Math.random() > 0.970) {
						controleJogo.info(Html.txtRedBold(getNome())
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
					if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
						incStress(Util.intervalo(0, 1));
					}
					if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
						incStress(Util.intervalo(0, 2));
					}
					if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
						incStress(Util.intervalo(1, 2));
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
			if (AGRESSIVO.equals(modoPilotagem)) {
				novoModoAgressivo = true;
			}
			if (LENTO.equals(modoPilotagem)) {
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

	public boolean verificaPilotoDesconcentrado(InterfaceJogo interfaceJogo) {
		if (ciclosDesconcentrado <= 0) {
			ciclosDesconcentrado = 0;
			return false;
		}
		if (!jogadorHumano && interfaceJogo.verificaNivelJogo()) {
			ciclosDesconcentrado--;
		}
		ciclosDesconcentrado--;

		return true;
	}

	private int calcularNovoModificador(InterfaceJogo controleJogo) {

		double bonusSecundario = 0.5;

		if (Carro.GIRO_MAX_VAL == getCarro().getGiro()) {
			bonusSecundario += getCarro().testePotencia() ? 0.3 : 0.2;
		}
		if (Carro.GIRO_MIN_VAL == getCarro().getGiro()) {
			bonusSecundario -= getCarro().testePotencia() ? 0.2 : 0.3;
		}
		if (controleJogo.isChovendo()) {
			bonusSecundario -= 0.1;
		}
		if (noAtual.verificaRetaOuLargada() && getCarro().testePotencia()) {
			return (Math.random() < bonusSecundario ? 4 : 3);
		} else if (noAtual.verificaRetaOuLargada()
				&& getCarro().testePotencia()) {
			return (Math.random() < bonusSecundario ? 3 : 2);
		} else if (noAtual.verificaCruvaAlta()
				&& testeHabilidadePilotoOuCarro(controleJogo) && agressivo) {
			return (Math.random() < bonusSecundario ? 3 : 2);
		} else if (noAtual.verificaCruvaAlta() && !agressivo
				&& testeHabilidadePilotoCarro(controleJogo)) {
			return (Math.random() < bonusSecundario ? 2 : 1);
		} else if (agressivo && noAtual.verificaCruvaBaixa()
				&& testeHabilidadePilotoCarro(controleJogo)) {
			return (Math.random() < bonusSecundario ? 2 : 1);
		} else {
			if (testeHabilidadePiloto(controleJogo)
					&& (Math.random() < bonusSecundario)) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public boolean testeHabilidadePilotoCarro(InterfaceJogo interfaceJogo) {
		if (danificado()) {
			return false;
		}

		return carro.testePotencia() && testeHabilidadePiloto(interfaceJogo);
	}

	public boolean testeHabilidadePilotoOuCarro(InterfaceJogo interfaceJogo) {
		if (danificado()) {
			return false;
		}

		return carro.testePotencia() || testeHabilidadePiloto(interfaceJogo);
	}

	public boolean testeHabilidadePiloto(InterfaceJogo interfaceJogo) {
		if (danificado() || verificaPilotoDesconcentrado(interfaceJogo)) {
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

				return Double.compare(v0.obterTempoVolta(), v1
						.obterTempoVolta());
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
				&& (Math.random() > ((900.0 - getPosicao() * 40) / 1000.0))) {
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
			if ((Math.random() < ((900.0 - getPosicao() * 40) / 1000.0)))
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
		mudarTracado(pos, interfaceJogo, false);
	}

	public boolean mudarTracado(int pos, InterfaceJogo interfaceJogo,
			boolean mesmoEmCurva) {
		if (indiceTracado != 0) {
			return false;
		}
		if (getSetaBaixo() <= 0) {
			if (getTracado() == 0 && pos == 1) {
				setSetaCima(11);
			}
			if (getTracado() == 2 && pos == 0) {
				setSetaCima(11);
			}
		}
		if (getSetaCima() <= 0) {
			if (getTracado() == 0 && pos == 2) {
				setSetaBaixo(11);
			}
			if (getTracado() == 1 && pos == 0) {
				setSetaBaixo(11);
			}
		}

		if (getTracado() == pos) {
			return false;
		}

		long agora = System.currentTimeMillis();
		if ((agora - ultimaMudancaPos) < (interfaceJogo.getTempoCiclo() * 10)) {
			return false;
		}
		if (getTracado() == 1 && pos == 2) {
			return false;
		}
		if (getTracado() == 2 && pos == 1) {
			return false;
		}
		if (!mesmoEmCurva) {
			if ((No.CURVA_BAIXA.equals(getNoAtual().getTipo()) && !testeHabilidadePilotoCarro(interfaceJogo))) {
				return false;
			}
		}
		int tracado = getTracado();
		if (!verificaColisaoPos(interfaceJogo, pos)) {
			setTracadoAntigo(getTracado());
			setTracado(pos);
			setIndiceTracado((int) (Carro.ALTURA * interfaceJogo.getCircuito()
					.getMultiplicadorLarguraPista()));
			ultimaMudancaPos = System.currentTimeMillis();
			return true;
		} else {
			ultimaMudancaPos = System.currentTimeMillis()
					+ (interfaceJogo.getTempoCiclo() * 20);
			int gerarDesconcentracao = Util.intervalo(5, 10);
			setCiclosDesconcentrado(gerarDesconcentracao);
		}
		return false;

	}

	public int decIndiceTracado() {
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

	private boolean verificaColisaoPos(InterfaceJogo controleJogo, int pos) {
		int indice = getNoAtual().getIndex();
		List pilotos = controleJogo.getPilotos();
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
}
