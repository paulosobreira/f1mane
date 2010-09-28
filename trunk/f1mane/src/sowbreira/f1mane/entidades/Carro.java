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
	public static final int LARGURA = 88;
	public static final int ALTURA = 34;
	public static final int MEIA_LARGURA = 44;
	public static final int MEIA_ALTURA = 17;
	private Color cor1;
	private Color cor2;
	private String danificado;
	private String nome;
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
		if ((pneus < 10) || (combust < 15)) {
			return true;
		}

		if ((pneus < combust) && (pneus < 15)) {
			return true;
		}

		if (motor < 14) {
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

		if ((pneus < 20) || (combust < 20)) {
			return true;
		}

		if ((pneus < combust) && (pneus < 15)) {
			return true;
		}

		if (motor < 15) {
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
			mod = 0.6;
		}
		if (GIRO_MIN_VAL == giro) {
			mod = 0.4;
		}
		int novoModificador = 0;
		if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
			mod -= 0.1;
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			mod -= 0.2;
		}
		if (no.verificaRetaOuLargada()) {
			if (Math.random() < (controleJogo.getNiveljogo() + 0.25)) {
				return novoModificador;
			}
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador++;
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& !testePotencia()) {
				novoModificador--;
			}
		}
		if (no.verificaCruvaAlta() || no.verificaCruvaBaixa()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& !testePotencia()) {
				novoModificador--;
			} else if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
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
				valDesgaste = ((testePotencia() ? 3 : 4) + novoModDano);
			else
				valDesgaste = ((testePotencia() ? 2 : 3) + novoModDano);
		} else if (giro == GIRO_NOR_VAL) {
			if (agressivo)
				valDesgaste = ((testePotencia() ? 1 : 2) + novoModDano);
			else
				valDesgaste = ((testePotencia() ? 0 : 1) + novoModDano);
		} else {
			if (agressivo)
				valDesgaste = ((testePotencia() ? 0 : 1) + novoModDano);
		}
		if (Clima.SOL.equals(controleJogo.getClima())
				&& Math.random() < (giro / 10.0) && agressivo) {
			valDesgaste += 1;
		}

		if (valDesgaste < 0) {
			valDesgaste = 0;
		}
		double indexVelcidadeDaPista = 0;
		if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 120.0 : 70.0));
		} else if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 110.0 : 60.0));
		} else if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 100.0 : 50.0));
		}
		if (!controleJogo.isModoQualify()
				&& piloto.verificaColisaoCarroFrente(controleJogo)) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista() * 3;
		}
		motor -= (valDesgaste * controleJogo.getCircuito().getMultiplciador() * indexVelcidadeDaPista);
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
		double fator = Math.random();
		double comparar = .7;
		if (controleJogo.isSemReabastacimento()) {
			if (InterfaceJogo.DIFICIL == controleJogo.getNivelCorrida())
				comparar = .85;
			else if (InterfaceJogo.NORMAL == controleJogo.getNivelCorrida())
				comparar = .9;
			if (InterfaceJogo.FACIL == controleJogo.getNivelCorrida())
				comparar = .95;
		}

		int valConsumo = 0;
		if (agressivo) {
			valConsumo = ((fator > comparar) ? 2 : 1);
		} else {
			valConsumo = ((fator > comparar) ? 1 : 0);
		}
		fator = Math.random();
		if (giro == GIRO_MIN_VAL) {
			valConsumo += ((fator > comparar) ? 1 : 0);
		} else if (giro == GIRO_NOR_VAL) {
			valConsumo += ((fator > comparar) ? 2 : 1);
		} else if (giro == GIRO_MAX_VAL) {
			valConsumo += ((fator > comparar) ? 3 : 2);
		}
		fator = Math.random();
		if (valConsumo <= 0) {
			if (fator > comparar) {
				valConsumo = 1;
			}
		}
		combustivel -= (valConsumo
				* controleJogo.getCircuito().getMultiplciador() * controleJogo
				.getIndexVelcidadeDaPista());

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
		if (TIPO_PNEU_MOLE.equals(tipoPneu)) {
			if ((no.verificaCruvaBaixa() || no.verificaCruvaAlta())
					&& (porcent > 10) && (Math.random() > indicativo)) {
				novoModificador += 1;
			}
		} else if (TIPO_PNEU_DURO.equals(tipoPneu)) {
			if (no.verificaCruvaAlta() && (porcent > 30) && (porcent < 80)
					&& (Math.random() > indicativo)) {
				novoModificador += 1;
			} else if (no.verificaCruvaBaixa() && (porcent > 40)
					&& (porcent < 60) && (Math.random() > indicativo)) {
				novoModificador += 1;
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

		int novoModDano = Util.inte(controleJogo.getNiveljogo()
				* (novoModificador > 5 ? 5 : novoModificador));
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (agressivo)
				desgPneus += (novoModDano);
		}
		if (agressivo && no.verificaCruvaBaixa()) {
			if (piloto.isJogadorHumano()) {
				if (controleJogo.verificaNivelJogo()) {
					piloto.incStress(Math.random() > .3 ? 1 : 0);
				}
			}
			boolean teste = piloto.testeHabilidadePilotoCarro();
			desgPneus += (teste ? 3 : 4 + novoModDano);
			if (!teste && Math.random() > 0.6 && !controleJogo.isChovendo()
					&& getPiloto().getPtosBox() == 0) {
				controleJogo.travouRodas(getPiloto());
			}
		} else if (agressivo && no.verificaCruvaAlta()) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 2
					: 3 + novoModDano);
		} else if (agressivo) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 1 : 2);
		} else {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 0 : 1);
		}
		if (Clima.SOL.equals(controleJogo.getClima())) {
			desgPneus += 1;
		}

		int percent = porcentagemCombustivel();
		double val = porcent / 100.0;
		if (Math.random() < val) {
			desgPneus += 1;
		}
		double indexVelcidadeDaPista = 0;
		if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 120.0 : 70.0));
		} else if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 110.0 : 60.0));
		} else if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
			indexVelcidadeDaPista = controleJogo.getIndexVelcidadeDaPista()
					* (porcentagemCombustivel() / (controleJogo
							.isSemReabastacimento() ? 100.0 : 50.0));
		}

		double valDesgaste = (desgPneus
				* controleJogo.getCircuito().getMultiplciador() * indexVelcidadeDaPista);
		if (controleJogo.isSafetyCarNaPista()) {
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
