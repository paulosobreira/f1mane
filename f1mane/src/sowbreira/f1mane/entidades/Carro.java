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
	public static final int ALTURA = 24;
	public static final int MEIA_LARGURA = 31;
	public static final int MEIA_ALTURA = 12;
	private Color cor1;
	private Color cor2;
	private String danificado;
	private String nome;
	private String img;
	private String asa = ASA_NORMAL;
	private int potencia;
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

	public void setDurabilidadeMaxPneus(int durabilidadeMaxPneus) {
		this.durabilidadeMaxPneus = durabilidadeMaxPneus;
	}

	public String getTipoPneu() {
		return tipoPneu;
	}

	public void trocarPneus(InterfaceJogo interfaceJogo, String tipoPneu,
			int distaciaCorrida) {
		if (interfaceJogo.isSemTrocaPneu()) {
			if (!(interfaceJogo.isChovendo()
					|| TIPO_PNEU_CHUVA.equals(tipoPneu) || pneus <= 0)) {
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

	public void setPotencia(int potencia) {
		this.potencia = potencia;
	}

	public boolean verificaCondicoesCautela(InterfaceJogo controleJogo) {
		int pneus = porcentagemDesgastePeneus();
		int combust = porcentagemCombustivel();
		int motor = porcentagemDesgasteMotor();
		double consumoMedioPenus = getPiloto().calculaConsumoMedioPneu();
		if (pneus < (consumoMedioPenus)) {
			return true;
		}
		if (controleJogo.isSemReabastacimento() && combust < 10) {
			return true;
		}
		double consumoMedioCombust = getPiloto().calculaConsumoMedioCombust();
		if (combust < (consumoMedioCombust)) {
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
		if (motor < 5) {
			return true;
		}
		return false;
	}

	public boolean verificaCondicoesCautelaGiro(InterfaceJogo controleJogo) {
		int combust = porcentagemCombustivel();
		int motor = porcentagemDesgasteMotor();
		double consumoMedioCombust = getPiloto().calculaConsumoMedioCombust();
		if (combust < consumoMedioCombust) {
			return true;
		}
		if (controleJogo.isSemReabastacimento() && combust < 20) {
			return true;
		}
		if ((motor < 10) || (combust < 10)) {
			return true;
		}
		return false;
	}

	public boolean verificaPneusIncompativeisClima(InterfaceJogo controleJogo) {
		if (ControleQualificacao.modoQualify) {
			return false;
		}
		if (controleJogo.isChovendo() && !tipoPneu.equals(TIPO_PNEU_CHUVA)) {
			return true;
		}
		if (!controleJogo.isChovendo() && tipoPneu.equals(TIPO_PNEU_CHUVA)) {
			return true;
		}
		return false;
	}

	public boolean testePotencia() {
		boolean teste = Math.random() < (potencia / 1000.0);
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
		novoModificador = calculaModificadorCombustivel(novoModificador,
				agressivo, no, controleJogo);
		return novoModificador;
	}

	private int calculaModificadorAsaGiro(int novoModificadorOri, No no,
			InterfaceJogo controleJogo) {
		double mod = 0.5;

		if (controleJogo.isSemReabastacimento() || controleJogo.isDrs()) {
			if (GIRO_MAX_VAL == giro) {
				mod = 0.8;
			}
			if (GIRO_MIN_VAL == giro) {
				mod = 0.2;
			}
		} else {
			if (GIRO_MAX_VAL == giro) {
				mod = 0.7;
			}
			if (GIRO_MIN_VAL == giro) {
				mod = 0.3;
			}
		}

		if (controleJogo.isChovendo() && MAIS_ASA.equals(getAsa())
				&& !getPiloto().testeHabilidadePilotoCarro(controleJogo)) {
			novoModificadorOri++;
		}
		if (Math.random() > mod || !testePotencia()) {
			return novoModificadorOri;
		}
		int novoModificador = 0;

		if (no.verificaRetaOuLargada()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador++;
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod) {
				novoModificador -= Math.random() < 0.6 ? (testePotencia() ? 0
						: 1) : (testePotencia() ? 1 : 2);
			}
		}
		if (no.verificaCruvaAlta() || no.verificaCruvaBaixa()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod) {
				novoModificador -= Util.intervalo(testePotencia() ? 0 : 1,
						testePotencia() ? 1 : 2);
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador += Math.random() < 0.7 ? 1 : 0;
			}
		}

		// System.out.println("Novo "
		// + (novoModificadorOri + Util.inte(+Math.round(novoModificador
		// * (1.0 - controleJogo.getNiveljogo())))) + " Velho "
		// + novoModificadorOri + " Calc " + novoModificador);
		return novoModificadorOri + novoModificador;
	}

	public int getTempMax() {
		return tempMax;
	}

	public void setTempMax(int tempMax) {
		this.tempMax = tempMax;
	}

	private void calculaDesgasteMotor(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		int valDesgaste = 0;
		int novoModDano = novoModificador;
		if (giro == GIRO_MAX_VAL) {
			if (agressivo)
				valDesgaste = ((testePotencia() ? 6 : 7) + novoModDano);
			else
				valDesgaste = ((testePotencia() ? 5 : 6) + novoModDano);
		} else if (giro == GIRO_NOR_VAL) {
			if (agressivo)
				valDesgaste = ((testePotencia() ? 4 : 5) + novoModDano);
			else
				valDesgaste = ((testePotencia() ? 2 : 3) + novoModDano);
		} else {
			if (agressivo)
				valDesgaste = ((testePotencia() ? 1 : 2) + novoModDano);
		}
		if (Clima.SOL.equals(controleJogo.getClima())
				&& Math.random() < (giro / 10.0)) {
			valDesgaste += agressivo ? 2 : 1;
		}

		if (valDesgaste < 0) {
			valDesgaste = 0;
		}
		if (!controleJogo.isSemReabastacimento()) {
			int valor = Util.intervalo(1, 100);
			int perCombust = porcentagemCombustivel();
			if (valor < perCombust)
				if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
					valDesgaste += (testePotencia() ? 0 : 1);
				} else if (InterfaceJogo.MEDIO_NV == controleJogo
						.getNiveljogo()) {
					valDesgaste += (testePotencia() ? 1 : 2);
				} else if (InterfaceJogo.DIFICIL_NV == controleJogo
						.getNiveljogo()) {
					valDesgaste += (testePotencia() ? 2 : 3);
				}
		}
		int dist = 20;
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			dist = 30;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
			dist = 10;
		}
		if (!controleJogo.isModoQualify()
				&& controleJogo.getNumVoltaAtual() != 1
				&& piloto.calculaDiffParaProximo(controleJogo) < dist) {
			valDesgaste += testePotencia() ? 1 : 2;
		}
		if (giro == GIRO_MAX_VAL && temperaturaMotor < tempMax) {
			temperaturaMotor++;
			if (getPiloto().isJogadorHumano()
					&& (temperaturaMotor >= tempMax - 6 && temperaturaMotor <= tempMax - 5))
				controleJogo.infoPrioritaria(Html.orange(Lang.msg(
						"temperatura", new String[] { Html
								.txtRedBold(getPiloto().getNome()) })));
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
		double desg = (valDesgaste * controleJogo.getCircuito()
				.getMultiplciador());
		if (verificaMotorSuperAquecido()) {

			double desgasteTemp = 1;

			if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
				desgasteTemp = Util.intervalo(1.1, 2);
			}
			if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
				desgasteTemp = Util.intervalo(1.5, 2);
			}
			if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
				desgasteTemp = Util.intervalo(1.3, 2.5);
			}

			desg *= desgasteTemp;
		}
		if (getPiloto().isJogadorHumano() && giro == GIRO_MAX_VAL
				&& !testePotencia() && MENOS_ASA.equals(getAsa())
				&& no.verificaRetaOuLargada()) {
			desg += Util.intervalo(1, controleJogo.verificaNivelJogo() ? 2 : 1);
		}

		if (verificaDano()) {
			desg /= 2;
		}
		int porcent = porcentagemDesgasteMotor();

		if (porcent < 5 && (GIRO_MIN_VAL == giro)) {
			desg *= 0.1;
		}

		motor -= desg;

		if (porcent < 0
				&& (GIRO_MIN_VAL == giro || !getPiloto().isJogadorHumano())) {
			porcent = 1;
		}

		if (porcent < 0) {
			piloto.setDesqualificado(true);
			setDanificado(Carro.EXPLODIU_MOTOR);
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("042",
					new String[] { piloto.getNome() })));

		}
	}

	private boolean verificaMotorSuperAquecido() {
		return temperaturaMotor >= tempMax;
	}

	public int getTemperaturaMotor() {
		return temperaturaMotor;
	}

	public void setTemperaturaMotor(int temperaturaMotor) {
		this.temperaturaMotor = temperaturaMotor;
	}

	private int calculaModificadorCombustivel(int novoModificador,
			boolean agressivo, No no, InterfaceJogo controleJogo) {
		int percent = porcentagemCombustivel();
		double indicativo = percent / 100.0;
		if (No.CURVA_BAIXA.equals(no)) {
			if (0 <= indicativo && indicativo < .2) {
				if ((Math.random() > .1))
					novoModificador += Util.intervalo(1, 3);
			} else if (.2 <= indicativo && indicativo < .3) {
				if ((Math.random() > .2))
					novoModificador += Util.intervalo(1, 2);
			} else if (.3 <= indicativo && indicativo < .4) {
				if ((Math.random() > .3))
					novoModificador += 1;
			} else if (.4 <= indicativo && indicativo < .5) {
				if ((Math.random() > .4))
					novoModificador += 1;
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
					novoModificador += Util.intervalo(1, 3);
			} else if (.2 <= indicativo && indicativo < .3) {
				if ((Math.random() > .3))
					novoModificador += Util.intervalo(1, 2);
			} else if (.3 <= indicativo && indicativo < .4) {
				if ((Math.random() > .4))
					novoModificador += Util.intervalo(1, 2);
			} else if (.4 <= indicativo && indicativo < .5) {
				if ((Math.random() > .5))
					novoModificador += 1;
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
		int dificudade = 3;
		if (InterfaceJogo.DIFICIL == controleJogo.getNivelCorrida())
			dificudade = ((testePotencia()) ? 2 : 4);
		else if (InterfaceJogo.NORMAL == controleJogo.getNivelCorrida())
			dificudade = ((testePotencia()) ? 1 : 3);
		else if (InterfaceJogo.FACIL == controleJogo.getNivelCorrida())
			dificudade = ((testePotencia()) ? 1 : 2);

		int valConsumo = 0;
		if (agressivo) {
			valConsumo = (getPiloto().testeHabilidadePilotoCarro(controleJogo) ? 1
					: 2);
		} else {
			valConsumo = (testePotencia() ? 0 : 1);
		}

		if (giro == GIRO_MIN_VAL) {
			valConsumo += ((testePotencia()) ? 0 : 1);
		} else if (giro == GIRO_NOR_VAL) {
			valConsumo += ((testePotencia()) ? 1 : 2);
		} else if (giro == GIRO_MAX_VAL) {
			valConsumo += ((getPiloto()
					.testeHabilidadePilotoCarro(controleJogo)) ? 2 : 4);
		} else if (giro == GIRO_MAX_VAL && verificaMotorSuperAquecido()) {
			valConsumo += ((getPiloto()
					.testeHabilidadePilotoCarro(controleJogo)) ? 3 : 4);
		}

		double consumoTotal = (valConsumo
				* controleJogo.getCircuito().getMultiplciador() * dificudade);

		if (!getPiloto().isJogadorHumano() && percent < 5) {
			setGiro(GIRO_MIN_VAL);
		}

		if (GIRO_MIN_VAL == getGiro() && percent < 5) {
			consumoTotal *= .1;
		}

		combustivel -= consumoTotal;

		if (percent < 0 && getPiloto().isJogadorHumano()) {
			combustivel = 0;
			setDanificado(PANE_SECA);
			getPiloto().setDesqualificado(true);
			paneSeca = true;
		}
		return novoModificador;
	}

	public boolean isPaneSeca() {
		return paneSeca;
	}

	private int calculaModificadorPneu(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		int porcent = porcentagemDesgastePeneus();
		// System.out.print("porcent " + porcent + " Antes " + novoModificador);
		if (controleJogo.isSemTrocaPneu() && Math.random() > .4) {
			return novoModificador;
		}
		double indicativo = .85;
		if (!controleJogo.isChovendo()) {
			double emborrachamento = controleJogo
					.porcentagemCorridaCompletada() / 200.0;
			if (emborrachamento > .4) {
				emborrachamento = .4;
			}
			indicativo -= emborrachamento;
		}
		if (agressivo) {
			indicativo -= .2;
		}
		if (TIPO_PNEU_MOLE.equals(tipoPneu)
				&& getPiloto().testeHabilidadePilotoOuCarro(controleJogo)) {
			int intervaloMin = Util.intervalo(3, 10);
			if (no.verificaCruvaBaixa())
				intervaloMin = Util.intervalo(7, 15);
			int intervaloMax = Util.intervalo(95, 100);
			if (no.verificaCruvaBaixa() || no.verificaCruvaAlta()) {
				if ((porcent > intervaloMin)
						&& (Math.random() > indicativo - 0.05)
						&& ((controleJogo.isModoQualify() && !controleJogo
								.isChovendo()) || porcent < intervaloMax)) {
					novoModificador += 1;
				} else if (!getPiloto().testeHabilidadePiloto(controleJogo)
						|| (porcent < intervaloMin || (porcent > intervaloMax))) {
					if (getPiloto().isAgressivo()) {
						novoModificador -= 1;
						msgPneusFrios(controleJogo, porcent, intervaloMax);
					}
				}
			}
		} else if (TIPO_PNEU_DURO.equals(tipoPneu)) {
			if (no.verificaCruvaAlta()) {
				int mod = 0;
				if (!controleJogo.asfaltoAbrasivo()) {
					mod = Util.intervalo(15, 25);
				}
				int intervaloMin = Util.intervalo(5 + mod, 10 + mod);
				int intervaloMax = Util.intervalo(90 - mod, 95 - mod);
				if ((porcent > intervaloMin) && (porcent < intervaloMax)
						&& (Math.random() > indicativo)) {
					novoModificador += 1;
				} else if (!getPiloto().testeHabilidadePilotoOuCarro(
						controleJogo)
						|| (porcent < intervaloMin || (porcent > intervaloMax))) {
					if (getPiloto().isAgressivo()) {
						novoModificador -= 1;
						msgPneusFrios(controleJogo, porcent, intervaloMax);
					}
				}
			}
			if (no.verificaCruvaBaixa()) {
				int mod = 0;
				if (!controleJogo.asfaltoAbrasivo()) {
					mod = Util.intervalo(10, 20);
				}
				int intervaloMin = Util.intervalo(10 + mod, 15 + mod);
				int intervaloMax = Util.intervalo(85 - mod, 90 - mod);
				if ((porcent > intervaloMin) && (porcent < intervaloMax)
						&& (Math.random() > indicativo)) {
					novoModificador += 1;
				} else if ((!getPiloto().testeHabilidadePilotoCarro(
						controleJogo))
						|| (porcent < intervaloMin || (porcent > intervaloMax))) {
					if (getPiloto().isAgressivo() || !testePotencia()) {
						novoModificador -= 1;
						msgPneusFrios(controleJogo, porcent, intervaloMax);
					}
				}
			}
		} else if (TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			double mod = 0;
			if (agressivo && getPiloto().testeHabilidadePiloto(controleJogo)) {
				mod = Util.intervalo(1, 9) / 100.0;
			} else if (agressivo) {
				mod = Util.intervalo(-5, 5) / 100.0;
			}
			if (no.verificaCruvaBaixa() && Math.random() > (.80 + mod)) {
				novoModificador -= 1;
			} else if (no.verificaCruvaAlta() && Math.random() > (.90 + mod)) {
				novoModificador -= 1;
			}
		}

		if (piloto.getTracado() != 0
				&& (no.verificaCruvaBaixa() || no.verificaCruvaAlta())
				&& !piloto.testeHabilidadePiloto(controleJogo)
				&& Math.random() > controleJogo.getFatorUtrapassagem()) {
			novoModificador--;
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
					&& !getPiloto().testeHabilidadePilotoCarro(controleJogo)) {
				novoModificador--;
			}
		}

		if ((pneus < 0) && (novoModificador > 1)) {
			novoModificador -= 1;
		}
		int desgPneus = 0;
		int novoModDesgaste = Util.inte((novoModificador > 5 ? 5
				: novoModificador));
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (agressivo)
				desgPneus += (novoModDesgaste);
		}
		if (agressivo && no.verificaCruvaBaixa()) {
			int perda = 0;
			if (controleJogo.verificaNivelJogo()) {
				if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
					perda = (Util.intervalo(0, 1));
				}
				if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
					perda = (Util.intervalo(0, 2));
				}
				if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
					perda = (Util.intervalo(1, 2));
				}
				if (verificaPneusIncompativeisClima(controleJogo)) {
					piloto.incStress(getPiloto().testeHabilidadePilotoCarro(
							controleJogo) ? 1 : 1 + perda);
				} else {
					piloto.incStress(getPiloto().testeHabilidadePilotoCarro(
							controleJogo) ? 0 : perda);
				}
			}
			if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
				boolean teste = piloto.testeHabilidadePilotoCarro(controleJogo);
				if (getPiloto().getStress() > 80
						&& Piloto.AGRESSIVO.equals(piloto.getModoPilotagem())) {
					teste = false;
					controleJogo.travouRodas(getPiloto());
					piloto.decStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? 4 : 2 + perda);
				}
				if (controleJogo.asfaltoAbrasivo()
						&& !controleJogo.isChovendo()
						&& !getPiloto().isJogadorHumano()
						&& Math.random() > 0.5) {
					controleJogo.travouRodas(getPiloto());
				}
				desgPneus += (teste ? 6 : 24) + novoModDesgaste;
			}
		} else if (agressivo && no.verificaCruvaAlta()) {
			desgPneus += (piloto.testeHabilidadePilotoCarro(controleJogo) ? 3
					: 4)
					+ novoModDesgaste;
			if (!controleJogo.isChovendo() && getPiloto().getPtosBox() == 0) {
				boolean teste = piloto.testeHabilidadePilotoCarro(controleJogo);
				if (getPiloto().getStress() > 70
						&& Piloto.AGRESSIVO.equals(piloto.getModoPilotagem())) {
					teste = false;
					controleJogo.travouRodas(getPiloto());
					piloto.decStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? 6 : 3);
				}
				if (controleJogo.asfaltoAbrasivo()
						&& !controleJogo.isChovendo()
						&& !getPiloto().isJogadorHumano()
						&& Math.random() > 0.7) {
					controleJogo.travouRodas(getPiloto());
				}
				desgPneus += (teste ? 2 : 12) + novoModDesgaste;
			}
		} else if (agressivo && no.verificaRetaOuLargada()) {
			int indexFrete = no.getIndex() + 50;
			if (indexFrete < (controleJogo.getNosDaPista().size() - 1)) {
				No noFrente = controleJogo.getNosDaPista().get(indexFrete);
				if (getPiloto().getStress() > 60 && !controleJogo.isChovendo()
						&& getPiloto().getPtosBox() == 0
						&& noFrente.verificaCruvaBaixa()) {
					boolean teste = piloto
							.testeHabilidadePilotoCarro(controleJogo);
					desgPneus += (teste ? 3 : 7) + novoModDesgaste;
					controleJogo.travouRodas(getPiloto());
					piloto.incStress(getPiloto().testeHabilidadePiloto(
							controleJogo) ? 5 : 10);
					if (controleJogo.asfaltoAbrasivo()
							&& !getPiloto().isJogadorHumano()
							&& !controleJogo.isChovendo()
							&& Math.random() > 0.9) {
						controleJogo.travouRodas(getPiloto());
					}
				}
			}
		} else if (agressivo) {
			desgPneus += (piloto.testeHabilidadePilotoCarro(controleJogo) ? 3
					: 4);
		} else {
			desgPneus += (piloto.testeHabilidadePilotoOuCarro(controleJogo) ? 1
					: 2);
		}
		if (Clima.SOL.equals(controleJogo.getClima()) && piloto.isAgressivo()) {
			if (no.verificaCruvaBaixa()) {
				if (!piloto.testeHabilidadePilotoCarro(controleJogo))
					desgPneus += Util.intervalo(1, 2);
				else
					desgPneus += 1;
			} else if (no.verificaCruvaAlta()) {
				if (!piloto.testeHabilidadePilotoCarro(controleJogo))
					desgPneus += Util.intervalo(0, 2);
				else
					desgPneus += 1;
			}
			if (controleJogo.asfaltoAbrasivo()
					&& (no.verificaCruvaBaixa() || no.verificaCruvaAlta())) {
				desgPneus += Util.intervalo(1, 5);
			}
		}

		double modComb = porcentagemCombustivel() / 100.0;
		double valDesgaste = (desgPneus * controleJogo.getCircuito()
				.getMultiplciador());
		double quartoDesgaste = valDesgaste / 4;
		valDesgaste = (3 * quartoDesgaste) + (quartoDesgaste * modComb);
		if (controleJogo.isSafetyCarNaPista()) {
			valDesgaste /= 3;
		}
		if (controleJogo.isSemTrocaPneu()) {
			valDesgaste *= 0.7;
		}
		if (!controleJogo.isSemTrocaPneu()
				&& porcent > 25
				&& (piloto.getNoAtual().verificaCruvaBaixa() || piloto
						.getNoAtual().verificaCruvaAlta())) {
			if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
				valDesgaste *= Piloto.AGRESSIVO.equals(getPiloto()
						.getModoPilotagem()) ? Util.intervalo(1.05, 1.15) : 1.0;
			} else if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				valDesgaste *= Piloto.AGRESSIVO.equals(getPiloto()
						.getModoPilotagem()) ? Util.intervalo(1.1, 1.2) : 1.0;
			}
		} else {
			valDesgaste *= 1.0;
		}
		if (controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (controleJogo.asfaltoAbrasivo()) {
				valDesgaste *= 0.75;
			} else {
				valDesgaste *= 0.5;
			}
		}

		if (porcent < 25) {
			valDesgaste *= 0.7;
		}

		if (porcent < 15) {
			valDesgaste *= 0.5;
		}

		if (!getPiloto().isJogadorHumano() && porcent < 10) {
			valDesgaste *= 0.25;
		}

		if (verificaDano()) {
			valDesgaste /= 3;
		}
		pneus -= valDesgaste;
		if ((pneus < 0) && !verificaDano()) {
			danificado = PNEU_FURADO;
			pneus = -1;
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("043",
					new String[] { getPiloto().getNome() })));
		}
		// System.out.println(" Depois " + novoModificador);
		return novoModificador;
	}

	private void msgPneusFrios(InterfaceJogo controleJogo, int porcent,
			int intervaloMax) {
		if (getPiloto().isJogadorHumano() && !controleJogo.isSafetyCarNaPista()
				&& !controleJogo.isChovendo() && (porcent > intervaloMax)
				&& Math.random() > .99) {
			controleJogo.info(Html.superBlue(Lang.msg("pneusFrios",
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
			cargaKers--;
		}

	}

}
