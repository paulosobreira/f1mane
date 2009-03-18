package sowbreira.f1mane.entidades;

import sowbreira.f1mane.controles.InterfaceJogo;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Html;

/**
 * @author Paulo Sobreira
 */
public class Piloto implements Serializable {
	private static final long serialVersionUID = 698992658460848522L;
	public static final String AGRESSIVO = "Agressivo";
	public static final String NORMAL = "Normal";
	public static final String LENTO = "Cauteloso";
	private int id;
	private int velocidade;
	private int velocidadeLargada;
	private transient String setUpIncial;
	private String nome;
	private String nomeCarro;
	private String nomeJogador;
	private transient int habilidade;
	private transient double notaQualificacaoAleatoria;
	private int ptosPista;
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
	private transient int ciclosDesconcentrado;
	private transient int porcentagemCombustUltimaParadaBox;
	private transient Map msgsQueSeRepetemMuito = new HashMap();
	private List voltas = new ArrayList();
	private String modoPilotagem;
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
		return ((segundosParaLider == null) ? "Lider" : segundosParaLider);
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

	public void setRecebeuBanderada(boolean recebueBanderada,
			InterfaceJogo controleJogo) {
		if (!this.recebeuBanderada) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(0);
			if (this.nome.equals(piloto.getNome())) {
				controleJogo
						.infoPrioritaria(Html.superBlack(getNome())
								+ Html
										.superGreen(" recebe a bandeirada apos completar "
												+ getNumeroVolta() + " voltas."));
			} else {
				controleJogo.info(Html.superBlack(getNome())
						+ Html.verde(" recebe a bandeirada apos completar "
								+ getNumeroVolta() + " voltas."));
			}
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

	public void setAgressivo(boolean regMaximo) {
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

	public double getNotaQualificacaoAleatoria() {
		return notaQualificacaoAleatoria;
	}

	public void setNotaQualificacaoAleatoria(double notaQualificacao) {
		this.notaQualificacaoAleatoria = notaQualificacao;
	}

	public Carro getCarro() {
		return carro;
	}

	public int getPtosPista() {
		return ptosPista;
	}

	public void setPtosPista(int ptosPista) {
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
		this.velocidade = velocidade;
	}

	/**
	 * Fechado não mexa mais em index aqui.
	 */
	public void processarCiclo(InterfaceJogo controleJogo) {
		List pista = controleJogo.getNosDaPista();
		int index = calcularNovoIndex(controleJogo);
		int diff = index - pista.size();

		/**
		 * Completou Volta
		 */
		if (diff >= 0) {
			index = diff;
			controleJogo.processaVoltaRapida(this);
			/**
			 * calback de nova volta para corrida Toda
			 */
			if (posicao == 1) {
				controleJogo.processaNovaVolta();
			}

			if ((posicao == 1)
					&& (numeroVolta == (controleJogo.totalVoltasCorrida() - 1))) {
				controleJogo.infoPrioritaria(Html.superBlack(getNome())
						+ Html.superGreen(" Abre a ultima volta."));
			}

			if (controleJogo.isCorridaTerminada()) {
				setRecebeuBanderada(true, controleJogo);
			}

			if (numeroVolta > controleJogo.totalVoltasCorrida()) {
				numeroVolta = controleJogo.totalVoltasCorrida();

				return;
			}
		}

		calcularVolta(controleJogo);
		verificaIrBox(controleJogo);

		this.setNoAtual((No) pista.get(index));
	}

	public void processaVelocidade(int index, No no) {
		if (velocidadeLargada < 50) {
			velocidade += ((int) (Math.random() * (20 * index)));
			velocidadeLargada = velocidade;
			return;
		}
		int fatorAcel = 10;
		if (getCarro().testePotencia()) {
			fatorAcel = 20;
		}
		switch (index) {
		case 0:
			velocidade -= 10 + ((int) (Math.random() * (fatorAcel + 5)));
			break;

		case 1:
			if (no.verificaRetaOuLargada()) {
				velocidade--;
			} else {
				if (velocidade > 200)
					velocidade -= 10 + ((int) (Math.random() * (fatorAcel + 25)));
				else if (velocidade > 150)
					velocidade -= 10 + ((int) (Math.random() * (fatorAcel + 15)));
				else if (velocidade > 80) {
					velocidade -= ((int) (Math.random() * (fatorAcel + 10)));
				} else {
					velocidade += ((int) (Math.random() * fatorAcel + 5));
				}
			}
			if (velocidade < 50) {
				velocidade = 40 + ((int) (Math.random() * 20));
			}

			break;
		case 2:
			if (no.verificaRetaOuLargada()) {
				velocidade += ((int) (Math.random() * fatorAcel / 2));
			} else {
				if (velocidade > 200 && no.verificaCruvaBaixa())
					velocidade -= ((int) (Math.random() * (fatorAcel + 30)));
				else if (velocidade > 150 && no.verificaCruvaBaixa())
					velocidade -= ((int) (Math.random() * (fatorAcel + 10)));
				else if (velocidade > 270)
					velocidade -= ((int) (Math.random() * (fatorAcel)));
				else {
					velocidade += ((int) (Math.random() * fatorAcel));
				}
			}
			break;
		case 3:
			velocidade += ((int) (Math.random() * (fatorAcel / 2)));
			break;

		default:
			break;
		}

		if (velocidade < 0) {
			velocidade = 10 + ((int) (Math.random() * 10));
		}
		if (velocidade > 350) {
			velocidade = 320 + ((int) (Math.random() * 30));
		}

	}

	private void verificaIrBox(InterfaceJogo controleJogo) {
		if (jogadorHumano || recebeuBanderada || ptosPista < 0) {
			return;
		}

		int pneus = getCarro().porcentagemDesgastePeneus();
		int combust = getCarro().porcentagemCombustivel();

		if ((combust < 10) && !controleJogo.isCorridaTerminada()) {
			box = true;
		} else {
			box = false;
		}

		if (Carro.TIPO_PNEU_DURO.equals(carro.getTipoPneu()) && (pneus < 10)) {
			box = true;
		}

		if ((Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu()) || Carro.TIPO_PNEU_CHUVA
				.equals(carro.getTipoPneu()))
				&& (pneus < 20)) {
			box = true;
		}

		if (box && controleJogo.verificaBoxOcupado(getCarro()) && (combust > 5)
				&& (pneus > 12)) {
			if (!Messagens.BOX_OCUPADO.equals(msgsQueSeRepetemMuito
					.get(Messagens.BOX_OCUPADO))) {
				if (isJogadorHumano() || getPosicao() < 4) {
					controleJogo.infoPrioritaria(Html.orange("Box para "
							+ Html.bold(getNome())
							+ " : Sua parada podera ser na proxima volta."));
				} else if (getPosicao() < 9) {
					controleJogo.info(Html.orange("Box para "
							+ Html.bold(getNome())
							+ " : Sua parada podera ser na proxima volta."));
				}

				msgsQueSeRepetemMuito.put(Messagens.BOX_OCUPADO,
						Messagens.BOX_OCUPADO);
			}

			box = false;
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

		if (controleJogo.verificaUltimasVoltas() && (combust > 3)
				&& (combust <= 5) && (pneus <= 12) && (pneus > 5)) {
			if (!Messagens.IR_BOX_FINAL_CORRIDA.equals(msgsQueSeRepetemMuito
					.get(Messagens.IR_BOX_FINAL_CORRIDA))) {
				controleJogo.info(Html.orange(getNome()
						+ " evita ir para box no final da corrida."));
				msgsQueSeRepetemMuito.put(Messagens.IR_BOX_FINAL_CORRIDA,
						Messagens.IR_BOX_FINAL_CORRIDA);
			}

			box = false;
		}

		if (carro.verificaDano()) {
			box = true;
		}

		if (controleJogo.verificaUltima()) {
			box = false;
		}
		if (controleJogo.getNumVoltaAtual() < 1) {
			box = false;
		}
		if (controleJogo.isCorridaTerminada()) {
			box = false;
		}
	}

	public Map getMsgsQueSeRepetemMuito() {
		return msgsQueSeRepetemMuito;
	}

	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	public void calcularVolta(InterfaceJogo controleJogo) {
		int tamanhoCircuito = controleJogo.getNosDaPista().size();

		if ((ptosPista == 0) || (tamanhoCircuito == 0)) {
			numeroVolta = 0;
		}

		numeroVolta = ptosPista / tamanhoCircuito;

		if (numeroVolta > controleJogo.totalVoltasCorrida()) {
			numeroVolta = controleJogo.totalVoltasCorrida();
		}
	}

	private int calcularNovoIndex(InterfaceJogo controleJogo) {
		int index = noAtual.getIndex();

		/**
		 * Devagarinho qdo a corrida termina
		 */
		if ((controleJogo.isCorridaTerminada() && isRecebeuBanderada())) {

			index += ((Math.random() > 0.4) ? 1 : 0);
			ptosPista += 1;

			return index;
		}

		if (!desqualificado) {
			if (getCarro().isPaneSeca()) {
				desqualificado = true;
				controleJogo.infoPrioritaria(Html.txtRedBold(getNome()
						+ " Sofre pane seca."));
			}
		} else {
			return index;
		}

		verificaMudancaRegime(controleJogo);
		verificaMudancaGiro(controleJogo);
		int novoModificador = calcularNovoModificador(controleJogo);

		novoModificador = getCarro().calcularModificadorCarro(novoModificador,
				agressivo, noAtual, controleJogo);
		if (!controleJogo.isModoQualify()) {
			novoModificador = controleJogo.verificaUltraPassagem(this,
					novoModificador);

			novoModificador = controleJogo.verificaRetardatario(this,
					novoModificador);
		}

		if (noAtual.verificaCruvaBaixa() || noAtual.verificaCruvaAlta()) {
			if (carro.verificaPneusIncompativeisClima(controleJogo)
					&& novoModificador > 1) {
				novoModificador = ((Math.random() > 0.4) ? 1 : 0);
			}
		}
		if (novoModificador > 3) {
			novoModificador = 3;
		} else if (novoModificador < 0) {
			novoModificador = 0;
		}

		if (danificado()) {
			novoModificador = 1;
		}
		novoModificador = controleJogo.calculaModificadorComSafetyCar(this,
				novoModificador);
		processaVelocidade(novoModificador, noAtual);
		index += novoModificador;
		ptosPista += novoModificador;

		return index;
	}

	private void verificaMudancaGiro(InterfaceJogo controleJogo) {
		if (jogadorHumano) {
			return;
		}
		int diff = calculaDiffParaProximo(controleJogo);
		int distBrigaMax = 30;
		int distBrigaMin = 0;
		if (controleJogo.getNiveljogo() == .3) {
			distBrigaMin = 20;
		} else if (controleJogo.getNiveljogo() == .5) {
			distBrigaMin = 15;
		} else if (controleJogo.getNiveljogo() == .7) {
			distBrigaMin = 10;
		}
		if (controleJogo.porcentagemCorridaCompletada() > 60) {
			distBrigaMax = 50;
		}

		Carro carroPilotoDaFrente = controleJogo.obterCarroNaFrente(this);
		if (diff > distBrigaMin && diff < distBrigaMax
				&& testeHabilidadePiloto()) {
			if (carroPilotoDaFrente != null) {
				Piloto pilotoFrente = carroPilotoDaFrente.getPiloto();
				if (pilotoFrente.isJogadorHumano()
						&& !pilotoFrente.entrouNoBox()
						&& !controleJogo.isSafetyCarNaPista()
						&& Math.random() > controleJogo.getNiveljogo()) {
					if (Math.random() < controleJogo
							.obterIndicativoCorridaCompleta()
							&& Math.random() > .95 && getPosicao() < 9) {
						int val = 1 + (int) (Math.random() * 4);
						switch (val) {
						case 1:
							controleJogo.info(Html.silver("Parece que "
									+ Html.bold(getNome())
									+ " decidiu partir de vez para cima de "
									+ Html.bold(carroPilotoDaFrente.getPiloto()
											.getNome())));
							break;
						case 2:
							controleJogo.info(Html.silver(Html.bold(getNome())
									+ " procura não perder contato visual com "
									+ Html.bold(carroPilotoDaFrente.getPiloto()
											.getNome())));
							break;
						case 3:
							controleJogo
									.info(Html
											.silver(Html.bold(getNome())
													+ " tenta diminuir a diferença de tempo para "
													+ Html
															.bold(carroPilotoDaFrente
																	.getPiloto()
																	.getNome())));
							break;
						case 4:
							controleJogo
									.info(Html
											.silver(Html.bold(getNome())
													+ " tenta tirar o máximo do carro para andar no ritimo de "
													+ Html
															.bold(carroPilotoDaFrente
																	.getPiloto()
																	.getNome())));
							break;

						default:
							break;
						}
					}
				}
			}
			getCarro().setGiro(Carro.GIRO_MAX_VAL);
		} else {
			getCarro().setGiro(Carro.GIRO_NOR_VAL);
		}
		if (getCarro().verificaCondicoesCautelaGiro()
				|| controleJogo.isSafetyCarNaPista() || entrouNoBox()) {
			getCarro().setGiro(Carro.GIRO_MIN_VAL);
		}

	}

	public static void main(String[] args) {
		System.out.println(1 + (int) (Math.random() * 3));
	}

	private void verificaMudancaRegime(InterfaceJogo controleJogo) {
		if (verificaPilotoDesconcentrado()) {
			return;
		}

		if (jogadorHumano) {
			if (carro.isFritouPneuNaUltimaCurvaBaixa()
					&& !testeHabilidadePilotoHumanoCarro(controleJogo)
					&& !getNoAtual().verificaCruvaBaixa()
					&& Math.random() < controleJogo.getNiveljogo()) {
				if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
					setAgressivo(false);
				}
				carro.setFritouPneuNaUltimaCurvaBaixa(false);
				if (AGRESSIVO.equals(modoPilotagem)) {
					if (controleJogo.isChovendo()) {
						controleJogo.info(Html.txtRedBold(getNome())
								+ Html.bold(" guia além de seu limite "
										+ "deslizando muito na pista molhada"));
					} else {
						if (Math.random() > 0.5) {
							controleJogo
									.info(Html.txtRedBold(getNome())
											+ Html
													.bold(" guia além de seu limite "
															+ "travando pneus em curva de baixa"));
						} else {
							controleJogo
									.info(Html.txtRedBold(getNome())
											+ Html
													.bold(" guia além de seu limite "
															+ "perdendo trazeira em curva de baixa"));
						}
					}
				} else {
					if (controleJogo.isChovendo()) {
						controleJogo.info(Html.bold(getNome())
								+ Html.verde(" guia com cautela após "
										+ "sair da pista molhada"));
					} else {
						controleJogo.info(Html.bold(getNome())
								+ Html.verde(" guia com cautela após "
										+ "travar pneus em curva de baixa"));
					}
				}

			}
			carro.setFritouPneuNaUltimaCurvaBaixa(false);
			if (AGRESSIVO.equals(modoPilotagem)) {
				setAgressivo(true);
				return;
			}
			if (LENTO.equals(modoPilotagem)) {
				setAgressivo(false);
				return;
			}
		}

		if (testeHabilidadePiloto() && controleJogo.verificaNivelJogo()) {
			if (carro.verificaCondicoesCautela()) {
				agressivo = false;
				if (!Messagens.PILOTO_EM_CAUTELA.equals(msgsQueSeRepetemMuito
						.get(Messagens.PILOTO_EM_CAUTELA))) {
					controleJogo.info(Html.superRed(getNome()
							+ " Parece ter problemas e esta lento."));
					msgsQueSeRepetemMuito.put(Messagens.PILOTO_EM_CAUTELA,
							Messagens.PILOTO_EM_CAUTELA);
				}
			} else if (No.CURVA_BAIXA.equals(noAtual.getTipo())) {
				agressivo = false;
			} else {
				agressivo = true;
			}
		} else if (!testeHabilidadePiloto()) {
			if (No.CURVA_BAIXA.equals(noAtual.getTipo())) {
				agressivo = false;
				ciclosDesconcentrado = gerarDesconcentracao((int) (14 * controleJogo
						.getNiveljogo()));
				if (Math.random() > .991 && getPosicao() < 9) {
					controleJogo.info(Html.bold(getNome())
							+ " guia com cautela após "
							+ "travar pneus em curva de baixa");
				}
			} else if (No.CURVA_ALTA.equals(noAtual.getTipo())) {
				ciclosDesconcentrado = gerarDesconcentracao((int) (10 * controleJogo
						.getNiveljogo()));
			} else {
				ciclosDesconcentrado = gerarDesconcentracao((int) (4 * controleJogo
						.getNiveljogo()));
			}

			ciclosDesconcentrado *= controleJogo.getNiveljogo();
		}
	}

	private boolean testeHabilidadePilotoHumanoCarro(InterfaceJogo controleJogo) {
		if (danificado()) {
			return false;
		}
		if (Math.random() < controleJogo.getNiveljogo()) {
			return false;
		}
		return testeHabilidadePiloto() && carro.testePotencia();
	}

	public int gerarDesconcentracao(int fator) {
		return (fator + (int) (Math.random() * 5));
	}

	public boolean verificaPilotoDesconcentrado() {
		if (ciclosDesconcentrado <= 0) {
			ciclosDesconcentrado = 0;

			return false;
		}

		ciclosDesconcentrado--;

		return true;
	}

	private int calcularNovoModificador(InterfaceJogo controleJogo) {

		double bonusSecundario = getCarro().getGiro() / 10;
		if (controleJogo.isChovendo()) {
			bonusSecundario -= .3;
		} else if (Carro.TIPO_PNEU_MOLE.equals(carro.getTipoPneu())) {
			bonusSecundario += .1;
		}
		if (testeHabilidadePilotoCarro() && agressivo
				&& noAtual.verificaRetaOuLargada()
				&& (Math.random() < controleJogo.getIndexVelcidadeDaPista())
				&& testeHabilidadePiloto()) {
			return 3;
		} else if (testeHabilidadePilotoCarro()
				&& noAtual.verificaRetaOuLargada()) {
			return (Math.random() < bonusSecundario ? 3 : 2);
		} else if (testeHabilidadePilotoCarro() && agressivo
				&& noAtual.verificaCruvaAlta()) {
			return 2;
		} else if (testeHabilidadePilotoCarro() && !agressivo
				&& noAtual.verificaCruvaAlta()) {
			return (Math.random() < bonusSecundario ? 2 : 1);
		} else if (testeHabilidadePilotoCarro() && agressivo
				&& noAtual.verificaCruvaBaixa()) {
			return (Math.random() < bonusSecundario ? 2 : 1);
		} else {
			if (!carro.testePotencia() && (Math.random() > bonusSecundario)) {
				return 0;
			} else {
				return 1;
			}
		}
	}

	public boolean testeHabilidadePilotoCarro() {
		if (danificado()) {
			return false;
		}

		return testeHabilidadePiloto() && carro.testePotencia();
	}

	public boolean testeHabilidadePiloto() {
		if (danificado()) {
			return false;
		}
		boolean teste = Math.random() < (habilidade / 100.0);
		return teste;
	}

	private boolean danificado() {
		return carro.verificaDano();
	}

	public void gerarCiclosPadoBox(int porcentCombust, long ciclos) {
		paradoBox = (int) (((porcentCombust * 100) / ciclos)) + 50;
		porcentagemCombustUltimaParadaBox = porcentCombust;
	}

	public int getPorcentagemCombustUltimaParadaBox() {
		if (porcentagemCombustUltimaParadaBox < 0) {
			porcentagemCombustUltimaParadaBox = 0;
		}

		return porcentagemCombustUltimaParadaBox;
	}

	public void processaVoltaNovaBox() {
		if (getVoltaAtual() == null) {
			Volta volta = new Volta();
			volta.setCiclosInicio(System.currentTimeMillis());
			setVoltaAtual(volta);

			return;
		}

		Volta volta = getVoltaAtual();
		volta.setCiclosFim(System.currentTimeMillis());

		setUltimaVolta(volta);
		voltas.add(volta);
		volta = new Volta();
		volta.setCiclosInicio(System.currentTimeMillis());
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

	public void efetuarSaidaBox() {
		qtdeParadasBox++;
		ptosBox = 0;
		box = false;
		carro.setDanificado(null);
		msgsQueSeRepetemMuito.put(Messagens.BOX_OCUPADO, null);
		msgsQueSeRepetemMuito.put(Messagens.PILOTO_EM_CAUTELA, null);
		if (numeroVolta > 0)
			processaVoltaNovaBox();
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

}
