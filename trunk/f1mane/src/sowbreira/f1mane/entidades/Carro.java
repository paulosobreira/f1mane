package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.io.Serializable;

import sowbreira.f1mane.controles.ControleQualificacao;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.idiomas.Lang;
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
	private int durabilidadeAereofolio;
	private int giro = GIRO_NOR_VAL;
	private int combustivel;
	private int tanqueCheio;
	private int pneus;
	private int durabilidadeMaxPneus;
	private int motor;
	private int durabilidadeMaxMotor;
	private String tipoPneu;
	private boolean paneSeca;
	private boolean recolhido;
	private Piloto piloto;

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

		if (controleJogo.isSemReabastacimento() && combust < 15) {
			return true;
		}

		double consumoMedioCombust = getPiloto().calculaConsumoMedioCombust();
		if (combust < (consumoMedioCombust)) {
			return true;
		}
		if (Carro.TIPO_PNEU_MOLE.equals(getTipoPneu())) {
			if (pneus < 15) {
				return true;
			}
		} else {
			if (pneus < 10) {
				return true;
			}
		}

		if (motor < 5) {
			return true;
		}
		return false;
	}

	public boolean verificaCondicoesCautelaGiro(InterfaceJogo controleJogo) {
		int pneus = porcentagemDesgastePeneus();
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
		System.out.println(Math.round(0.5));
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

		if (GIRO_MAX_VAL == giro) {
			mod = 0.65;
		}
		if (GIRO_MIN_VAL == giro) {
			mod = 0.35;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
			mod -= 0.1;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			mod -= 0.2;
		}
		if (Math.random() > mod) {
			return novoModificadorOri;
		}
		int novoModificador = 0;

		if (no.verificaRetaOuLargada()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador++;
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& !getPiloto().testeHabilidadePilotoOuCarro()) {
				novoModificador--;
			}
		}
		if (no.verificaCruvaAlta() || no.verificaCruvaBaixa()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& !getPiloto().testeHabilidadePilotoCarro()) {
				novoModificador--;
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& getPiloto().testeHabilidadePilotoOuCarro()) {
				novoModificador++;
			}
		}
		if (controleJogo.isChovendo() && !MAIS_ASA.equals(getAsa())) {
			if (Math.random() < .500) {
				novoModificador--;
			}
		}
		// System.out.println("Novo "
		// + (novoModificadorOri + Util.inte(+Math.round(novoModificador
		// * (1.0 - controleJogo.getNiveljogo())))) + " Velho "
		// + novoModificadorOri + " Calc " + novoModificador);
		return novoModificadorOri
				+ Util.inte(+Math.round(novoModificador
						* (1.0 - controleJogo.getNiveljogo())));
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
		motor -= (valDesgaste * controleJogo.getCircuito().getMultiplciador());
		if (porcentagemDesgasteMotor() < 0) {
			piloto.setDesqualificado(true);
			setDanificado(Carro.EXPLODIU_MOTOR);
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("042",
					new String[] { piloto.getNome() })));

		}
	}

	private int calculaModificadorCombustivel(int novoModificador,
			boolean agressivo, No no, InterfaceJogo controleJogo) {
		int percent = porcentagemCombustivel();
		double indicativo = percent / 100.0;
		if (!controleJogo.isChovendo()) {
			if (No.CURVA_BAIXA.equals(no)) {
				if (.1 <= indicativo && indicativo < .2) {
					if ((Math.random() > .1))
						novoModificador += 1;
				} else if (.2 <= indicativo && indicativo < .3) {
					if ((Math.random() > .2))
						novoModificador += 1;
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
						novoModificador -= 1;
				} else if (.9 <= indicativo) {
					if ((Math.random() < .9))
						novoModificador -= 1;
				}
			} else if (No.CURVA_ALTA.equals(no)) {
				if (.1 <= indicativo && indicativo < .2) {
					if ((Math.random() > .2))
						novoModificador += 1;
				} else if (.2 <= indicativo && indicativo < .3) {
					if ((Math.random() > .3))
						novoModificador += 1;
				} else if (.3 <= indicativo && indicativo < .4) {
					if ((Math.random() > .4))
						novoModificador += 1;
				} else if (.4 <= indicativo && indicativo < .5) {
					if ((Math.random() > .5))
						novoModificador += 1;
				} else if (.5 <= indicativo && indicativo < .6) {
					if ((Math.random() < .6))
						novoModificador -= 1;
				} else if (.7 <= indicativo && indicativo < .8) {
					if ((Math.random() < .7))
						novoModificador -= 1;
				} else if (.8 <= indicativo && indicativo < .9) {
					if ((Math.random() < .8))
						novoModificador -= 1;
				} else if (.9 <= indicativo) {
					if ((Math.random() < .9))
						novoModificador -= 1;
				}
			}
		}
		int dificudade = 2;
		if (InterfaceJogo.DIFICIL == controleJogo.getNivelCorrida())
			dificudade = 3;
		else if (InterfaceJogo.NORMAL == controleJogo.getNivelCorrida())
			dificudade = 2;
		if (InterfaceJogo.FACIL == controleJogo.getNivelCorrida())
			dificudade = 1;

		int valConsumo = 0;
		if (agressivo) {
			valConsumo = (getPiloto().testeHabilidadePilotoCarro() ? 1 : 2);
		} else {
			valConsumo = (testePotencia() ? 0 : 1);
		}
		if (giro == GIRO_MIN_VAL) {
			valConsumo += ((testePotencia()) ? 0 : 1);
		} else if (giro == GIRO_NOR_VAL) {
			valConsumo += ((getPiloto().testeHabilidadePilotoOuCarro()) ? 1 : 2);
		} else if (giro == GIRO_MAX_VAL) {
			valConsumo += ((getPiloto().testeHabilidadePilotoCarro()) ? 3 : 4);
		}
		combustivel -= (valConsumo
				* controleJogo.getCircuito().getMultiplciador() * dificudade);

		if (percent < 0) {
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
		double indicativo = .6;
		if (agressivo) {
			indicativo = .4;
		}
		if (TIPO_PNEU_MOLE.equals(tipoPneu)
				&& getPiloto().testeHabilidadePilotoOuCarro()) {
			if ((no.verificaCruvaBaixa() || no.verificaCruvaAlta())
					&& (porcent > Util.intervalo(10, 25))
					&& (Math.random() > indicativo)
					&& (porcent < Util.intervalo(80, 90))) {
				novoModificador += 1;
			}
		} else if (TIPO_PNEU_DURO.equals(tipoPneu)
				&& getPiloto().testeHabilidadePilotoCarro()) {
			if (no.verificaCruvaAlta()) {
				if ((porcent > Util.intervalo(25, 35))
						&& (porcent < Util.intervalo(70, 80))
						&& (Math.random() > indicativo)) {
					novoModificador += 1;
				} else if (porcent < Util.intervalo(10, 20)
						|| (porcent > Util.intervalo(80, 90))) {
					if (!getPiloto().testeHabilidadePiloto())
						novoModificador -= 1;
				}
			}
			if (no.verificaCruvaBaixa()) {
				if ((porcent > Util.intervalo(25, 35))
						&& (porcent < Util.intervalo(60, 70))
						&& (Math.random() > indicativo)) {
					novoModificador += 1;
				} else if (porcent < Util.intervalo(20, 30)
						|| (porcent > Util.intervalo(70, 80))) {
					if (!getPiloto().testeHabilidadePiloto())
						novoModificador -= 1;
				}
			}
		} else if (TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (no.verificaCruvaBaixa() && Math.random() > .8) {
				novoModificador -= 1;
			} else if (no.verificaCruvaAlta() && Math.random() > .9) {
				novoModificador -= 1;
			}
		}
		if ((pneus < 0) && (novoModificador > 1)) {
			novoModificador -= 1;
		}
		int desgPneus = 0;
		int novoModDano = Util
				.inte((novoModificador > 5 ? 5 : novoModificador));
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (agressivo)
				desgPneus += (novoModDano);
		}
		if (agressivo && no.verificaCruvaBaixa()) {
			if (controleJogo.verificaNivelJogo()) {
				piloto.incStress(getPiloto().testeHabilidadePilotoOuCarro() ? 0
						: 1);
			}
			boolean teste = piloto.testeHabilidadePilotoCarro();
			desgPneus += (teste ? 4 : 6) + novoModDano;
			if (!teste && Math.random() > 0.6 && !controleJogo.isChovendo()
					&& getPiloto().getPtosBox() == 0) {
				controleJogo.travouRodas(getPiloto());
			}
		} else if (agressivo && no.verificaCruvaAlta()) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 3 : 4)
					+ novoModDano;
		} else if (agressivo) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 3 : 4);
		} else {
			desgPneus += (piloto.testeHabilidadePilotoOuCarro() ? 1 : 2);
		}
		if (Clima.SOL.equals(controleJogo.getClima())) {
			if (TIPO_PNEU_MOLE.equals(tipoPneu)
					&& !piloto.testeHabilidadePilotoCarro())
				desgPneus += 2;
			else
				desgPneus += 1;
		}
		double porcentComb = porcentagemCombustivel() / 1000.0;
		double combustivel = 1;
		if (Math.random() < porcentComb)
			combustivel = (piloto.testeHabilidadePiloto() ? Util
					.intervalo(1, 3) : Util.intervalo(2, 4));

		double valDesgaste = (desgPneus
				* controleJogo.getCircuito().getMultiplciador() * combustivel);
		if (controleJogo.isSafetyCarNaPista()) {
			valDesgaste /= 3;
		}

		if (controleJogo.isSemTrocaPneu()) {
			valDesgaste *= 0.7;
		}

		if (!controleJogo.isSemTrocaPneu() && getPiloto().isJogadorHumano()) {
			if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
				valDesgaste *= Math.random() < .7 ? 1.1 : 1.2;
			} else if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				valDesgaste *= Math.random() < .5 ? 1.1 : 1.2;
			}
		} else {
			valDesgaste *= 1.0;
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

	public int porcentagemDesgastePeneus() {
		return (100 * pneus) / durabilidadeMaxPneus;
	}

	public int porcentagemDesgasteMotor() {
		return (100 * motor) / durabilidadeMaxMotor;
	}

	public int porcentagemCombustivel() {
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

}
