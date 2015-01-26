package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.io.Serializable;

import sowbreira.f1mane.controles.ControleQualificacao;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
import br.nnpe.Html;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 06/05/2007 as 11:09:15
 */
public class Carro implements Serializable {
	private static final long serialVersionUID = -181518724082829223L;
	public final static String TIPO_PNEU_MOLE = "TIPO_PNEU_MOLE";
	public final static String TIPO_PNEU_DURO = "TIPO_PNEU_DURO";
	public final static String TIPO_PNEU_CHUVA = "TIPO_PNEU_CHUVA";
	public final static String PNEU_FURADO = "PNEU_FURADO";
	public final static String PERDEU_AEREOFOLIO = "PERDEU_AEREOFOLIO";
	public static final String BATEU_FORTE = "BATEU_FORTE";
	public static final String PANE_SECA = "PANE_SECA";
	public static final String ABANDONOU = "ABANDONOU";
	public static final String EXPLODIU_MOTOR = "EXPLODIU_MOTOR";
	public static final String GIRO_MIN = "GIRO_MIN";
	public static final String GIRO_NOR = "GIRO_NOR";
	public static final String GIRO_MAX = "GIRO_MAX";
	public static final int GIRO_MIN_VAL = 1;
	public static final int GIRO_NOR_VAL = 5;
	public static final int GIRO_MAX_VAL = 9;
	public static final String MAIS_ASA = "MAIS_ASA";
	public static final String ASA_NORMAL = "ASA_NORMAL";
	public static final String MENOS_ASA = "MENOS_ASA";
	public static final int LARGURA = 62;
	public static final int LARGURA_CIMA = 86;
	public static final int ALTURA = 24;
	public static final int ALTURA_CIMA = 86;
	public static final int MEIA_LARGURA = 31;
	public static final int MEIA_LARGURA_CIMA = 43;
	public static final int MEIA_ALTURA = 12;
	public static final int MEIA_ALTURA_CIMA = 43;
	public static final double FATOR_AREA_CARRO = .7;
	public static final int RAIO_DERRAPAGEM = 155;
	private Color cor1;
	private Color cor2;
	private String danificado;
	private String nome;
	private String img;
	private String asa = ASA_NORMAL;
	private int potenciaAntesQualify;
	private int potencia;
	private int aerodinamica;
	private int freios;
	private int potenciaReal;
	private int durabilidadeAereofolio;
	private int giro = GIRO_NOR_VAL;
	private int combustivel;
	private int tanqueCheio;
	private int pneus;
	private int durabilidadeMaxPneus;
	private int motor;
	private int temperaturaMotor;
	private int cargaKers;
	private int durabilidadeMaxMotor;
	private String tipoPneu;
	private boolean paneSeca;
	private boolean recolhido;
	private Piloto piloto;
	private int tempMax;

	public int getPotenciaAntesQualify() {
		return potenciaAntesQualify;
	}

	public void setPotenciaAntesQualify(int potenciaAntesQualify) {
		this.potenciaAntesQualify = potenciaAntesQualify;
	}

	public int getCargaKers() {
		return cargaKers;
	}

	public void setCargaKers(int cargaKers) {
		this.cargaKers = cargaKers;
	}

	public int getGiro() {
		return giro;
	}

	@Override
	public String toString() {
		return nome;
	}

	public void setGiro(int giro) {
		this.giro = giro;
	}

	public boolean equals(Object arg0) {
		Carro carro = (Carro) arg0;

		return nome.equals(carro.getNome());
	}

	public int getMotor() {
		return motor;
	}

	public boolean isRecolhido() {
		return recolhido;
	}

	public void setRecolhido(boolean recolhido) {
		this.recolhido = recolhido;
	}

	public void setMotor(int motor) {
		this.motor = motor;
	}

	public int getDurabilidadeMaxMotor() {
		return durabilidadeMaxMotor;
	}

	public Color getCor2() {
		return cor2;
	}

	public void setCor2(Color cor2) {
		this.cor2 = cor2;
	}

	public void setDurabilidadeMaxMotor(int durabilidadeMaxMotor,
			int mediaPontecia) {
		if (this.durabilidadeMaxMotor != 0)
			return;
		if (mediaPontecia < 800) {
			mediaPontecia = 800;
		}
		this.durabilidadeMaxMotor = Util
				.inte((durabilidadeMaxMotor * (mediaPontecia / 1000.0))
						+ (durabilidadeMaxMotor * (getPotencia() / 1000.0)));
		this.motor = this.durabilidadeMaxMotor;
	}

	public int hashCode() {
		return nome.hashCode();
	}

	public Piloto getPiloto() {
		return piloto;
	}

	public void setPiloto(Piloto piloto) {
		this.piloto = piloto;
	}

	public int getCombustivel() {
		return combustivel;
	}

	public int getDurabilidadeMaxPneus() {
		return durabilidadeMaxPneus;
	}

	public int getTanqueCheio() {
		return tanqueCheio;
	}

	public int getPotenciaReal() {
		return potenciaReal;
	}

	public void setPotenciaReal(int potenciaReal) {
		this.potenciaReal = potenciaReal;
	}

	public void setPotencia(int potencia) {
		this.potencia = potencia;
	}

	public void setTanqueCheio(int tanqueCheio) {
		this.tanqueCheio = tanqueCheio;
	}

	public void setCombustivel(int combustivel) {
		if (combustivel > tanqueCheio) {
			combustivel = tanqueCheio;
		}

		this.combustivel = combustivel;
	}

	public int getPneus() {
		return pneus;
	}

	public void setPneus(int pneus) {
		this.pneus = pneus;
	}

	public void setPorcentPneus(int porcent) {
		this.pneus = Util.inte(durabilidadeMaxPneus * (porcent / 100.0));
	}

	public void setDurabilidadeMaxPneus(int durabilidadeMaxPneus) {
		this.durabilidadeMaxPneus = durabilidadeMaxPneus;
	}

	public String getTipoPneu() {
		return tipoPneu;
	}

	public void trocarPneus(InterfaceJogo interfaceJogo, String tipoPneu,
			int distaciaCorrida) {
		if (interfaceJogo.isSemTrocaPneu() && !interfaceJogo.isModoQualify()
				&& this.tipoPneu != null) {
			if (!verificaPneusIncompativeisClima(interfaceJogo) && pneus > 0) {
				return;
			}
		}
		this.tipoPneu = tipoPneu;

		if (Carro.TIPO_PNEU_DURO.equals(tipoPneu)) {
			setPneuDuro(distaciaCorrida);
		} else {
			setPneuMoleOuChuva(distaciaCorrida);
		}
	}

	public Color getCor1() {
		return cor1;
	}

	public void setCor1(Color cor) {
		this.cor1 = cor;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
	}

	public int getPotencia() {
		return potencia;
	}

	public boolean verificaPilotoNormal(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return false;
		}
		int pneus = porcentagemDesgastePeneus();
		double consumoMedioPenus = getPiloto().calculaConsumoMedioPneu();
		if (pneus < (consumoMedioPenus)) {
			return true;
		}
		if (Carro.TIPO_PNEU_MOLE.equals(getTipoPneu())) {
			if (pneus < 10) {
				return true;
			}
		} else {
			if (pneus < 5) {
				return true;
			}
		}
		return false;
	}

	public boolean verificaCondicoesCautelaGiro(InterfaceJogo controleJogo) {
		if (controleJogo.isModoQualify()) {
			return false;
		}
		int combust = porcentagemCombustivel();
		int motor = porcentagemDesgasteMotor();
		if ((motor <= 5) || (combust <= 5)) {
			return true;
		}
		return false;
	}

	public boolean verificaPneusIncompativeisClima(InterfaceJogo controleJogo) {
		if (controleJogo.isChovendo() && !TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			return true;
		}
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			return true;
		}
		return false;
	}

	public boolean testePotencia() {
		boolean teste = Math.random() < (potencia / 1000.0);
		return teste;

	}

	public boolean testeAerodinamica() {
		boolean teste = Math.random() < (aerodinamica / 1000.0);
		return teste;

	}

	public boolean testeFreios() {
		boolean teste = Math.random() < (freios / 1000.0);
		return teste;

	}

	public static void main(String[] args) {
		// Logger.logar((.7 * 30));
		// Logger.logar(Math.random() * 1000);
		// System.out.println(Math.random() < 1 / 10.0);
		// double val = 100;
		// val *= 0.1;
		int vcar = 50;
		vcar *= .3;
		System.out.println(vcar);
	}

	public void setPneuDuro(int distaciaCorrida) {
		pneus = Util.inte(distaciaCorrida * 1.2);
		durabilidadeMaxPneus = pneus;
	}

	public void setPneuMoleOuChuva(int distaciaCorrida) {
		pneus = Util.inte(distaciaCorrida * 0.60);
		durabilidadeMaxPneus = pneus;
	}

	public int calcularModificadorCarro(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		novoModificador = calculaModificadorPneu(novoModificador, agressivo,
				no, controleJogo);
		calculaDesgasteMotor(novoModificador, agressivo, no, controleJogo);
		novoModificador = calculaModificadorAsaGiro(novoModificador, no,
				controleJogo);
		novoModificador = calculaModificadorCombustivel(novoModificador, no,
				controleJogo);
		return novoModificador;
	}

	private int calculaModificadorAsaGiro(int novoModificadorOri, No no,
			InterfaceJogo controleJogo) {
		double mod = 0.5;
		boolean testeAerodinamica = testeAerodinamica();
		if (GIRO_MAX_VAL == giro) {
			mod = 0.7;
		}
		if (GIRO_MIN_VAL == giro) {
			mod = 0.3;
		}
		if (controleJogo.isChovendo() && MAIS_ASA.equals(getAsa())
				&& !no.verificaRetaOuLargada()
				&& getPiloto().testeHabilidadePilotoAerodinamica(controleJogo)) {
			if (no.verificaCruvaBaixa()) {
				novoModificadorOri++;
			} else {
				novoModificadorOri += Util.intervalo(0, 1);
			}
		}
		if (Math.random() > mod || !testeAerodinamica) {
			return novoModificadorOri;
		}
		int novoModificador = 0;

		if (no.verificaRetaOuLargada()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod) {
				novoModificador += (testeAerodinamica ? 2 : 1);
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() > mod) {
				novoModificador -= Math.random() < 0.2 ? (testeAerodinamica ? 0
						: 1) : (testeAerodinamica ? 1 : 2);
			}
		}
		if (no.verificaCruvaAlta() || no.verificaCruvaBaixa()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod) {
				novoModificador -= Util.intervalo(testeAerodinamica ? 0 : 1,
						testeAerodinamica ? 1 : 2);
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& testeAerodinamica) {
				novoModificador += Math.random() < 0.7 ? 1 : 0;
			}
		}
		return novoModificadorOri + novoModificador;
	}

	public int getTempMax() {
		return tempMax;
	}

	public void setTempMax(int tempMax) {
		this.tempMax = tempMax;
	}

	private void processaTemperaturaMotor(InterfaceJogo controleJogo) {
		if (giro == GIRO_MAX_VAL && temperaturaMotor < tempMax) {
			temperaturaMotor++;
			if (getPiloto().isJogadorHumano()
					&& (temperaturaMotor >= tempMax - 6 && temperaturaMotor <= tempMax - 5))
				controleJogo
						.infoPrioritaria(Html.orange(Lang.msg("temperatura",
								new String[] { Html.txtRedBold(getPiloto()
										.getNome()) })));
		}
		if (giro != GIRO_MAX_VAL) {
			if (getPiloto().getNoAtual().verificaRetaOuLargada()) {
				temperaturaMotor -= 3;
			}
			if (getPiloto().getNoAtual().verificaCruvaAlta()) {
				temperaturaMotor -= 2;
			}
			if (getPiloto().getNoAtual().verificaCruvaBaixa()) {
				temperaturaMotor -= 1;
			}
		}
		if (giro == GIRO_MIN_VAL) {
			temperaturaMotor -= 1;
		}
		if (temperaturaMotor > tempMax) {
			temperaturaMotor = tempMax;
		}
		if (temperaturaMotor < 0) {
			temperaturaMotor = 0;
		}
	}

	public boolean verificaMotorSuperAquecido() {
		return temperaturaMotor >= tempMax;
	}

	public int getTemperaturaMotor() {
		return temperaturaMotor;
	}

	public void setTemperaturaMotor(int temperaturaMotor) {
		this.temperaturaMotor = temperaturaMotor;
	}

	private int calculaModificadorCombustivel(int novoModificador, No no,
			InterfaceJogo controleJogo) {
		int percent = porcentagemCombustivel();
		double indicativo = percent / 100.0;
		boolean testePotencia = testePotencia();

		if (No.CURVA_BAIXA.equals(no)) {
			if (0 <= indicativo && indicativo < .2) {
				if ((Math.random() > .1))
					novoModificador += Util.intervalo(1, 2);
			} else if (.2 <= indicativo && indicativo < .3) {
				if ((Math.random() > .2))
					novoModificador += Util.intervalo(0, 2);
			} else if (.3 <= indicativo && indicativo < .4) {
				if ((Math.random() > .3))
					novoModificador += Util.intervalo(0, 1);
			} else if (.4 <= indicativo && indicativo < .5) {
				if ((Math.random() > .4))
					novoModificador += Util.intervalo(0, 1);
			} else if (.5 <= indicativo && indicativo < .6) {
				if ((Math.random() < .6))
					novoModificador -= 1;
			} else if (.7 <= indicativo && indicativo < .8) {
				if ((Math.random() < .7))
					novoModificador -= 1;
			} else if (.8 <= indicativo && indicativo < .9) {
				if ((Math.random() < .8))
					novoModificador -= Util.intervalo(1, 2);
			} else if (.9 <= indicativo) {
				if ((Math.random() < .9))
					novoModificador -= Util.intervalo(1, 3);
			}
		} else if (No.CURVA_ALTA.equals(no)) {
			if (0 <= indicativo && indicativo < .2) {
				if ((Math.random() > .2))
					novoModificador += Util.intervalo(1, 2);
			} else if (.2 <= indicativo && indicativo < .3) {
				if ((Math.random() > .3))
					novoModificador += Util.intervalo(0, 2);
			} else if (.3 <= indicativo && indicativo < .4) {
				if ((Math.random() > .4))
					novoModificador += Util.intervalo(0, 1);
			} else if (.4 <= indicativo && indicativo < .5) {
				if ((Math.random() > .5))
					novoModificador += Util.intervalo(0, 1);
			} else if (.5 <= indicativo && indicativo < .6) {
				if ((Math.random() < .6))
					novoModificador -= 1;
			} else if (.7 <= indicativo && indicativo < .8) {
				if ((Math.random() < .7))
					novoModificador -= Util.intervalo(1, 2);
			} else if (.8 <= indicativo && indicativo < .9) {
				if ((Math.random() < .8))
					novoModificador -= Util.intervalo(1, 2);
			} else if (.9 <= indicativo) {
				if ((Math.random() < .9))
					novoModificador -= Util.intervalo(1, 3);
			}
		} else if (no.verificaRetaOuLargada()) {
			if (0 <= indicativo && indicativo < .1) {
				if ((Math.random() > .7))
					novoModificador += Util.intervalo(1, 2);
			} else if (.1 <= indicativo && indicativo < .2) {
				if ((Math.random() > .5))
					novoModificador += Util.intervalo(0, 1);
			} else if (.8 <= indicativo && indicativo < .9) {
				if ((Math.random() < .5))
					novoModificador -= Util.intervalo(0, 1);
			} else if (.9 <= indicativo) {
				if ((Math.random() < .7))
					novoModificador -= Util.intervalo(1, 2);
			}
		}
		calculaConsumoCombustivel(controleJogo, percent, testePotencia);
		if (percent == 0 && novoModificador > 0) {
			novoModificador--;
		}
		return novoModificador;
	}

	private void calculaDesgasteMotor(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		int valDesgaste = 0;
		int novoModDano = novoModificador;
		boolean testePotencia = testePotencia();
		if (giro == GIRO_MAX_VAL) {
			valDesgaste = ((testePotencia ? 6 : 7) + novoModDano);
		} else if (giro == GIRO_NOR_VAL) {
			valDesgaste = ((testePotencia ? 4 : 5) + novoModDano);
		} else {
			valDesgaste = ((testePotencia ? 1 : 2) + novoModDano);
		}
		if (Clima.SOL.equals(controleJogo.getClima())
				&& Math.random() < (giro / 10.0)) {
			valDesgaste += 1;
		}

		if (valDesgaste < 0) {
			valDesgaste = 0;
		}
		int dist = 50;
		if (!controleJogo.isModoQualify()
				&& controleJogo.getNumVoltaAtual() != 1
				&& controleJogo
						.calculaDiffParaProximoRetardatario(piloto, true) < dist) {
			valDesgaste += testePotencia ? 1 : 2;
		}
		processaTemperaturaMotor(controleJogo);
		double desg = (valDesgaste * controleJogo.getCircuito()
				.getMultiplciador());
		if (verificaMotorSuperAquecido()) {
			double desgasteTemp = 1;
			desgasteTemp = testePotencia ? 2 : 3;
			desg *= desgasteTemp;
		}
		if (verificaDano()) {
			desg /= 2;
		}
		int porcent = porcentagemDesgasteMotor();

		if (porcent < 5 && (GIRO_MIN_VAL == giro)) {
			desg *= 0.1;
		}

		motor -= desg;

		if (porcent < 0) {
			piloto.setDesqualificado(true);
			setDanificado(Carro.EXPLODIU_MOTOR);
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("042",
					new String[] { piloto.getNome() })));

		}
	}

	private void calculaConsumoCombustivel(InterfaceJogo controleJogo,
			int percent, boolean testePotencia) {
		int valConsumo = 0;

		if (giro == GIRO_MIN_VAL) {
			valConsumo += (testePotencia ? 0 : 3);
		} else if (giro == GIRO_NOR_VAL) {
			valConsumo += (testePotencia ? 3 : 5);
		} else if (giro == GIRO_MAX_VAL) {
			valConsumo += (testePotencia ? 5 : 7);
		}

		if (giro == GIRO_MAX_VAL) {
			if (piloto.getNoAtual().verificaRetaOuLargada()) {
				valConsumo += testePotencia() ? 0 : 3;
			} else {
				valConsumo += testePotencia() || testeAerodinamica() ? 3 : 5;
			}
		}

		double consumoTotal = (valConsumo * controleJogo.getCircuito()
				.getMultiplciador());

		combustivel -= consumoTotal;

		if (percent < 0) {
			combustivel = 0;
			setDanificado(PANE_SECA);
			getPiloto().setDesqualificado(true);
			paneSeca = true;
			controleJogo.infoPrioritaria(Html.txtRedBold(getNome()
					+ Lang.msg("118")));
		}
	}

	public boolean isPaneSeca() {
		return paneSeca;
	}

	private int calculaModificadorPneu(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		int porcentPneus = porcentagemDesgastePeneus();
		if (controleJogo.isSemTrocaPneu() && Math.random() > .7) {
			return novoModificador;
		}

		if (TIPO_PNEU_MOLE.equals(tipoPneu)
				|| (controleJogo.isModoQualify() && !controleJogo.isChovendo() && testeFreios())) {
			int intervaloMin = Util.intervalo(5, 10);
			int intervaloMax = Util.intervalo(95, 100);
			if (no.verificaCruvaBaixa() || no.verificaCruvaAlta()) {
				if ((porcentPneus > intervaloMin)
						&& (controleJogo.verificaPistaEmborrachada())
						&& ((controleJogo.isModoQualify() && !controleJogo
								.isChovendo()) || porcentPneus < intervaloMax)) {
					if (porcentPneus > (intervaloMin + 10))
						novoModificador += 1;
				} else if (!testeFreios()
						|| (porcentPneus < intervaloMin || (porcentPneus > intervaloMax))) {
					novoModificador -= 1;
					msgPneusFrios(controleJogo, porcentPneus, intervaloMax);
				}
			}
		} else if (TIPO_PNEU_DURO.equals(tipoPneu)) {
			if (no.verificaCruvaAlta()) {
				int mod = 0;
				if (!controleJogo.asfaltoAbrasivo()) {
					mod = Util.intervalo(5, 15);
				}
				int intervaloMin = Util.intervalo(5 + mod, 10 + mod);
				int intervaloMax = Util.intervalo(90 - mod, 95 - mod);
				if ((porcentPneus > intervaloMin)
						&& (porcentPneus < intervaloMax)
						&& (controleJogo.verificaPistaEmborrachada())) {
					if (porcentPneus > (intervaloMin + 10))
						novoModificador += 1;
				} else if (!getPiloto().testeHabilidadePilotoFreios(
						controleJogo)
						|| (porcentPneus < intervaloMin || (porcentPneus > intervaloMax))) {
					novoModificador -= 1;
					msgPneusFrios(controleJogo, porcentPneus, intervaloMax);
				}
			}
			if (no.verificaCruvaBaixa()) {
				int mod = 0;
				if (!controleJogo.asfaltoAbrasivo()) {
					mod = Util.intervalo(5, 10);
				}
				int intervaloMin = Util.intervalo(10 + mod, 15 + mod);
				int intervaloMax = Util.intervalo(85 - mod, 90 - mod);
				if ((porcentPneus > intervaloMin)
						&& (porcentPneus < intervaloMax)
						&& (controleJogo.verificaPistaEmborrachada())) {
					if (porcentPneus > (intervaloMin + 10))
						novoModificador += 1;
				} else if ((!getPiloto().testeHabilidadePilotoFreios(
						controleJogo))
						|| (porcentPneus < intervaloMin || (porcentPneus > intervaloMax))) {
					novoModificador -= 1;
					msgPneusFrios(controleJogo, porcentPneus, intervaloMax);
				}
			}
		} else if (TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (no.verificaCruvaBaixa() && Math.random() > (.80)) {
				novoModificador -= 1;
			} else if (no.verificaCruvaAlta() && Math.random() > (.90)) {
				novoModificador -= 1;
			} else if (no.verificaRetaOuLargada() && Math.random() > (.99)) {
				novoModificador -= 1;
			}
		}

		double divVoltas = (controleJogo.getQtdeTotalVoltas() / Constantes.MAX_VOLTAS);
		if (divVoltas >= 1) {
			divVoltas = 0.999;
		}
		if (verificaPneusIncompativeisClima(controleJogo)
				&& novoModificador >= 1) {
			if (no.verificaCruvaBaixa() || no.verificaCruvaAlta()) {
				novoModificador = 0;
			} else if ((Math.random() > divVoltas)
					&& !getPiloto().testeHabilidadePilotoFreios(controleJogo)) {
				novoModificador--;
			}
		}

		if ((pneus < 0) && (novoModificador > 1)) {
			novoModificador -= 1;
		}

		if ((novoModificador > 0)
				&& no.verificaCruvaBaixa()
				&& !piloto.isAgressivo()
				&& (!piloto.testeHabilidadePilotoFreios(controleJogo) || (TIPO_PNEU_DURO
						.equals(tipoPneu) && !controleJogo.asfaltoAbrasivo()))) {
			novoModificador -= 1;
		}

		if ((novoModificador > 0)
				&& no.verificaCruvaAlta()
				&& !piloto.isAgressivo()
				&& (!piloto.testeHabilidadePilotoFreios(controleJogo) || (TIPO_PNEU_DURO
						.equals(tipoPneu) && !controleJogo.asfaltoAbrasivo()))) {
			novoModificador -= 1;
		}

		double valDesgaste = calculaDesgastePneus(novoModificador, agressivo,
				no, controleJogo, porcentPneus);

		pneus -= valDesgaste;
		if ((pneus < 0) && !verificaDano()) {
			setDanificado(PNEU_FURADO);
			pneus = -1;
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("043",
					new String[] { getPiloto().getNome() })));

		}
		// System.out.println(" Depois " + novoModificador);
		return novoModificador;
	}

	private double calculaDesgastePneus(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo, int porcentPneus) {
		int desgPneus = 1;
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (agressivo && !no.verificaRetaOuLargada())
				desgPneus++;
		}
		if (agressivo && no.verificaCruvaBaixa()) {
			int stress = Util.intervalo(1, 3);
			if (verificaPneusIncompativeisClima(controleJogo)) {
				piloto.incStress(getPiloto()
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? stress
						: 3 + stress);
			} else {
				piloto.incStress(getPiloto()
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? stress
						: stress);
			}
			if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
				boolean teste = piloto
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo);
				if (getPiloto().getStress() > 80
						&& Piloto.AGRESSIVO.equals(piloto.getModoPilotagem())) {
					teste = false;
					if (!no.verificaRetaOuLargada()) {
						controleJogo.travouRodas(getPiloto());
					}
					piloto.decStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? stress : 5 + stress);
				}
				if (controleJogo.asfaltoAbrasivo()
						&& !controleJogo.isChovendo()
						&& !no.verificaRetaOuLargada()
						&& getPiloto().getStress() > 70) {
					controleJogo.travouRodas(getPiloto());
				}
				desgPneus += (teste ? 6 : 8);
			}
		} else if (agressivo && no.verificaCruvaAlta()) {
			desgPneus += (piloto
					.testeHabilidadePilotoAerodinamicaFreios(controleJogo) ? 3
					: 4);
			if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
				boolean teste = piloto
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo);
				if (getPiloto().getStress() > 70
						&& Piloto.AGRESSIVO.equals(piloto.getModoPilotagem())) {
					teste = false;
					controleJogo.travouRodas(getPiloto());
					piloto.decStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? 4 : 2);
				}
				if (controleJogo.asfaltoAbrasivo()
						&& !controleJogo.isChovendo()
						&& !no.verificaRetaOuLargada()
						&& getPiloto().getStress() > 50 && Math.random() > 0.5) {
					controleJogo.travouRodas(getPiloto());
				}
				desgPneus += (teste ? 4 : 6);
			}
		} else if (agressivo && no.verificaRetaOuLargada()) {
			int indexFrete = no.getIndex() + 50;
			if (indexFrete < (controleJogo.getNosDaPista().size() - 1)) {
				No noFrente = controleJogo.getNosDaPista().get(indexFrete);
				boolean teste = piloto
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo);
				if (getPiloto().getStress() > 60 && !controleJogo.isChovendo()
						&& getPiloto().getPtosBox() == 0
						&& noFrente.verificaCruvaBaixa()) {
					controleJogo.travouRodas(getPiloto());
					piloto.incStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? 0 : 5);
					if (controleJogo.asfaltoAbrasivo()
							&& getPiloto().getStress() > 80
							&& !controleJogo.isChovendo()
							&& noFrente.verificaCruvaAlta()
							&& Math.random() > 0.7) {
						controleJogo.travouRodas(getPiloto());
					}
					teste = false;
				}
				desgPneus += (teste ? 0 : 1);
			}
		} else {
			if (!no.verificaRetaOuLargada()) {
				if (controleJogo.asfaltoAbrasivo()) {
					desgPneus += (piloto
							.testeHabilidadePilotoFreios(controleJogo) ? 5 : 10);
				} else {
					desgPneus += (piloto
							.testeHabilidadePilotoFreios(controleJogo) ? 1 : 5);
				}
			}
		}
		if (!controleJogo.isChovendo()
				&& (Clima.SOL.equals(controleJogo.getClima()) || Math.random() > 0.5)) {
			if (no.verificaCruvaBaixa()) {
				if (!piloto
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo))
					desgPneus += Util.intervalo(1, 2);
				else
					desgPneus += 1;
			} else if (no.verificaCruvaAlta()) {
				if (!piloto
						.testeHabilidadePilotoAerodinamicaFreios(controleJogo))
					desgPneus += Util.intervalo(0, 2);
				else
					desgPneus += 1;
			}
		}
		double porcentComb = porcentagemCombustivel() / 100.0;
		if (no.verificaRetaOuLargada()) {
			porcentComb = 0;
		}
		double fator = (2 - ((piloto.getCarro().getAerodinamica() + piloto
				.getCarro().getFreios()) / 2000.0));
		if (controleJogo.isSemTrocaPneu()) {
			fator /= 2;
		} else if (piloto.getQtdeParadasBox() == 0
				&& controleJogo.porcentagemCorridaCompletada() > 50) {
			fator *= 2;
		}

		double combustivel = fator + porcentComb;

		double valDesgaste = (desgPneus
				* controleJogo.getCircuito().getMultiplciador() * combustivel);
		if (controleJogo.isSafetyCarNaPista()) {
			valDesgaste /= 5;
		}

		if (controleJogo.isSemTrocaPneu()) {
			valDesgaste *= 0.7;
		}

		if (porcentPneus > 25
				&& (piloto.getNoAtual().verificaCruvaBaixa() || piloto
						.getNoAtual().verificaCruvaAlta())) {
			valDesgaste *= Piloto.AGRESSIVO.equals(getPiloto()
					.getModoPilotagem()) ? Util.intervalo(1.05, 1.15) : 1.0;
		} else {
			valDesgaste *= 1.0;
		}
		if (controleJogo.isChovendo()
				&& TIPO_PNEU_CHUVA.equals(tipoPneu)
				|| (controleJogo.isChovendo() && !TIPO_PNEU_CHUVA
						.equals(tipoPneu))) {
			if (controleJogo.asfaltoAbrasivo()) {
				valDesgaste *= 0.4;
			} else {
				valDesgaste *= 0.2;
			}
		}

		if (porcentPneus < 25) {
			valDesgaste *= 0.7;
		}

		if (porcentPneus < 15) {
			valDesgaste *= 0.5;
		}

		if (verificaDano()) {
			valDesgaste /= 3;
		}
		return valDesgaste;
	}

	private void msgPneusFrios(InterfaceJogo controleJogo, int porcent,
			int intervaloMax) {
		if (getPiloto().isJogadorHumano() && !controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isChovendo() && (porcent > intervaloMax)
				&& Math.random() > .99
				&& controleJogo.verificaInfoRelevante(piloto)) {
			controleJogo.info(Html.orange(Lang.msg("pneusFrios",
					new String[] { Html.txtRedBold(getPiloto().getNome()) })));
		}
	}

	public int porcentagemDesgastePeneus() {
		if (durabilidadeMaxPneus == 0) {
			return 0;
		}
		return (100 * pneus) / durabilidadeMaxPneus;
	}

	public int porcentagemDesgasteMotor() {
		if (durabilidadeMaxMotor == 0) {
			return 0;
		}
		return (100 * motor) / durabilidadeMaxMotor;
	}

	public int porcentagemCombustivel() {
		if (tanqueCheio == 0) {
			return 0;
		}
		return (100 * combustivel) / tanqueCheio;
	}

	public String getDanificado() {
		return danificado;
	}

	public void setDanificado(String danificado) {
		if (piloto.isBox()) {
			piloto.setBox(true);
		}
		if (Carro.PANE_SECA.equals(danificado)) {
			paneSeca = true;
		}
		if (Carro.PERDEU_AEREOFOLIO.equals(danificado)) {
			setDurabilidadeAereofolio(0);
		}
		this.danificado = danificado;
		if (ABANDONOU.equals(danificado) || BATEU_FORTE.equals(danificado)
				|| PANE_SECA.equals(danificado)
				|| EXPLODIU_MOTOR.equals(danificado)) {
			getPiloto().setDesqualificado(true);
			getPiloto().setPtosPista(getPiloto().getPtosPista() / 2);
		}

	}

	public boolean verificaDano() {
		return ((danificado == null) ? false : true);
	}

	public void abandonou() {
		danificado = ABANDONOU;

	}

	public void setTipoPneu(String tipoPneu) {
		this.tipoPneu = tipoPneu;
	}

	public void mudarGiroMotor(String giroMotor) {
		if (GIRO_MAX.equals(giroMotor)) {
			giro = GIRO_MAX_VAL;
		} else if (GIRO_MIN.equals(giroMotor)) {
			giro = GIRO_MIN_VAL;
		} else if (GIRO_NOR.equals(giroMotor)) {
			giro = GIRO_NOR_VAL;
		}

	}

	public String getGiroFormatado() {
		switch (giro) {
		case 1:
			return Lang.msg("Min");
		case 5:
			return Lang.msg("Nor");
		case 9:
			return Lang.msg("Max");
		default:
			break;
		}
		return null;
	}

	public String getAsa() {
		return asa;
	}

	public void setAsa(String asa) {
		this.asa = asa;
	}

	public int getDurabilidadeAereofolio() {
		return durabilidadeAereofolio;
	}

	public void setDurabilidadeAereofolio(int durabilidadeAereofolio) {
		this.durabilidadeAereofolio = durabilidadeAereofolio;
	}

	public void usaKers() {
		if (cargaKers > 0) {
			cargaKers -= (testeAerodinamica() && testePotencia() ? 1 : 2);
		}

	}

	public boolean isPneuFurado() {
		return PNEU_FURADO.equals(danificado);
	}

	public boolean verificaParado() {
		return (!isRecolhido() && BATEU_FORTE.equals(danificado))
				|| EXPLODIU_MOTOR.equals(danificado)
				|| PANE_SECA.equals(danificado) || ABANDONOU.equals(danificado);
	}

	public int getAerodinamica() {
		return aerodinamica;
	}

	public void setAerodinamica(int aerodinamica) {
		this.aerodinamica = aerodinamica;
	}

	public int getFreios() {
		return freios;
	}

	public void setFreios(int freios) {
		this.freios = freios;
	}

}
