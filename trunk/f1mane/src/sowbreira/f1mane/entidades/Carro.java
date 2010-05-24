package sowbreira.f1mane.entidades;

import java.awt.Color;
import java.io.Serializable;

import sowbreira.f1mane.controles.ControleQualificacao;
import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;

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
	private String img;
	private Color cor1;
	private Color cor2;
	private String danificado;
	private String nome;
	private String asa = ASA_NORMAL;
	private int potencia;
	private int durabilidadeAereofolio = 3;
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
	private boolean fritouPneuNaUltimaCurvaBaixa;

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

	public String getImg() {
		return img;
	}

	public void setImg(String img) {
		this.img = img;
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
		this.durabilidadeMaxMotor = (int) (durabilidadeMaxMotor * (mediaPontecia / 1000.0))
				+ getPotencia();
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

	public boolean verificaCondicoesCautela() {
		int pneus = porcentagemDesgastePeneus();
		int combust = porcentagemCombustivel();
		int motor = porcentagemDesgasteMotor();

		double consumoMedioPenus = getPiloto().calculaConsumoMedioPneu();

		if (pneus < 1.5 * consumoMedioPenus) {
			return true;
		}

		double consumoMedioCombust = getPiloto().calculaConsumoMedioCombust();
		if (combust < 1.5 * consumoMedioCombust) {
			return true;
		}
		if ((pneus < 10) || (combust < 7)) {
			return true;
		}

		if ((pneus < combust) && (pneus < 10)) {
			return true;
		}

		if (motor < 7) {
			return true;
		}
		return false;
	}

	public boolean verificaCondicoesCautelaGiro() {
		int pneus = porcentagemDesgastePeneus();
		int combust = porcentagemCombustivel();
		int motor = porcentagemDesgasteMotor();

		double consumoMedioPenus = getPiloto().calculaConsumoMedioPneu();

		if (pneus < 1.5 * consumoMedioPenus) {
			return true;
		}

		double consumoMedioCombust = getPiloto().calculaConsumoMedioCombust();
		if (combust < 1.5 * consumoMedioCombust) {
			return true;
		}

		if ((pneus < 10) || (combust < 10)) {
			return true;
		}

		if ((pneus < combust) && (pneus < 5)) {
			return true;
		}

		if (motor < 10) {
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
	}

	public void setPneuDuro(int distaciaCorrida) {
		pneus = (distaciaCorrida);
		durabilidadeMaxPneus = pneus;
	}

	public void setPneuMoleOuChuva(int distaciaCorrida) {
		pneus = (distaciaCorrida + ((distaciaCorrida * 25) / 100)) / 2;
		durabilidadeMaxPneus = pneus;
	}

	public int calcularModificadorCarro(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		novoModificador = calculaModificadorPneu(novoModificador, agressivo,
				no, controleJogo);
		novoModificador = calculaModificadorCombustivel(novoModificador,
				agressivo, no, controleJogo);
		novoModificador = calculaModificadorAsaGiro(novoModificador, no,
				controleJogo);
		calculaDesgasteMotor(novoModificador, agressivo, no, controleJogo);
		return novoModificador;
	}

	private int calculaModificadorAsaGiro(int novoModificador, No no,
			InterfaceJogo controleJogo) {
		double mod = 0.5;
		if (GIRO_MAX_VAL == giro) {
			mod = 0.6;
		}
		if (GIRO_MIN_VAL == giro) {
			mod = 0.4;
		}

		if (Math.random() > .8) {
			return novoModificador;
		}
		if (no.verificaRetaOuLargada()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador++;
			}
			if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& !testePotencia()) {
				novoModificador--;
			}
		}
		if (no.verificaCruvaAlta() || no.verificaCruvaBaixa()) {
			if (MENOS_ASA.equals(getAsa()) && Math.random() < mod
					&& !testePotencia()) {
				novoModificador--;
			}
			if (MAIS_ASA.equals(getAsa()) && Math.random() < mod
					&& testePotencia()) {
				novoModificador++;
			}
		}
		if (ASA_NORMAL.equals(getAsa()) && (Math.random() < mod - .3)
				&& testePotencia()) {
			novoModificador++;
		}
		if (controleJogo.isChovendo() && !MAIS_ASA.equals(getAsa())) {
			if (Math.random() < .500) {
				novoModificador--;
			}
		}
		return novoModificador;
	}

	private void calculaDesgasteMotor(int novoModificador, boolean agressivo,
			No no, InterfaceJogo controleJogo) {
		int valDesgaste = 0;
		int novoModDano = novoModificador;
		if (giro == 9) {
			valDesgaste = ((testePotencia() ? 3 : 4) + novoModDano);
			if (piloto.isJogadorHumano()) {
				valDesgaste += 1;
				if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
					valDesgaste += 1;
				}
			}

		} else if (giro == 5) {
			valDesgaste = ((testePotencia() ? 1 : 2) + novoModDano);
			if (piloto.isJogadorHumano()) {
				if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
					valDesgaste += 1;
				}
				if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()
						&& valDesgaste > 1) {
					valDesgaste -= 1;
				}
			}
		} else {
			valDesgaste = ((testePotencia() ? 0 : 1));
			if (!piloto.isAgressivo()) {
				valDesgaste = 0;
			}
		}
		if (Clima.SOL.equals(controleJogo.getClima())
				&& Math.random() < (giro / 10.0)) {
			valDesgaste += 1;
		}

		if (valDesgaste < 0) {
			valDesgaste = 0;
		}
		motor -= (valDesgaste * controleJogo.getCircuito().getMultiplciador() * controleJogo
				.getIndexVelcidadeDaPista());
		if (porcentagemDesgasteMotor() < 0) {
			piloto.setDesqualificado(true);
			setDanificado(Carro.EXPLODIU_MOTOR);
			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("042",
					new String[] { piloto.getNome() })));

		}
	}

	public void ajusteMotorParadaBox() {
		int mod = piloto.getQtdeParadasBox();
		if (mod < 3) {
			mod = 1;
		}
		motor += ((durabilidadeMaxMotor * (potencia / 10000.0)) / mod);
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
		int valConsumo = 0;
		if (agressivo) {
			valConsumo = ((fator > .5) ? 3 : 2);
		} else {
			valConsumo = ((fator > .5) ? 2 : 1);
		}
		if (giro == 1) {
			valConsumo -= ((fator > .5) ? 2 : 1);
		} else if (giro == 9) {
			valConsumo += ((fator > .5) ? 3 : 2);
		}
		if (valConsumo <= 0) {
			if (Math.random() > .5) {
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
		if (controleJogo.isSemTrocaPneu() && Math.random() > .4) {
			return novoModificador;
		}
		double indicativo = .7;
		if (agressivo) {
			indicativo = .5;
		}
		if (TIPO_PNEU_MOLE.equals(tipoPneu)) {
			if ((no.verificaCruvaAlta() || no.verificaCruvaBaixa())
					&& (porcent > 10) && (Math.random() > indicativo)) {
				novoModificador += 1;
			}
		} else if (TIPO_PNEU_DURO.equals(tipoPneu)) {
			if (no.verificaCruvaBaixa() && (porcent > 30) && (porcent < 70)
					&& (Math.random() > indicativo)) {
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

		// int novoModDano = novoModificador > 3 ? 3 : novoModificador;
		int novoModDano = novoModificador;
		if (!controleJogo.isChovendo() && TIPO_PNEU_CHUVA.equals(tipoPneu)) {
			if (agressivo)
				desgPneus += ((controleJogo.getNiveljogo() * 1.5) + novoModDano);
		}
		if (agressivo && no.verificaCruvaBaixa()) {
			if (piloto.isJogadorHumano()) {
				fritouPneuNaUltimaCurvaBaixa = true;
				if (controleJogo.verificaNivelJogo()) {
					piloto.incStress(Math.random() > .3 ? 1 : 0);
				}
			}
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 3
					: 4 + novoModDano);
		} else if (agressivo && no.verificaCruvaAlta()) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 2
					: 3 + novoModDano);
		} else if (agressivo) {
			desgPneus += (piloto.testeHabilidadePilotoCarro() ? 1
					: 2 + novoModDano);
		} else {
			desgPneus += 1;
		}
		if (Clima.SOL.equals(controleJogo.getClima())) {
			if (Math.random() > indicativo)
				desgPneus += 2;
			else
				desgPneus += 1;
		}

		int percent = porcentagemCombustivel();
		double val = porcent / 100.0;
		if (Math.random() < val) {
			desgPneus += 1;
		}

		if (piloto.isJogadorHumano()
				&& InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()
				&& Math.random() < .5) {
			desgPneus += 1;
		}
		if (piloto.isJogadorHumano()
				&& InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()
				&& desgPneus > 1) {
			desgPneus -= 1;
		}
		pneus -= (desgPneus * controleJogo.getCircuito().getMultiplciador() * controleJogo
				.getIndexVelcidadeDaPista());
		if ((pneus < 0) && !verificaDano()) {
			danificado = PNEU_FURADO;
			pneus = -1;

			controleJogo.infoPrioritaria(Html.superRed(Lang.msg("043",
					new String[] { getPiloto().getNome() })));

		}

		return novoModificador;
	}

	public int porcentagemDesgastePeneus() {
		return (100 * pneus) / durabilidadeMaxPneus;
	}

	public int porcentagemDesgasteMotor() {
		return (100 * motor) / durabilidadeMaxMotor;
	}

	public boolean isFritouPneuNaUltimaCurvaBaixa() {
		return fritouPneuNaUltimaCurvaBaixa;
	}

	public void setFritouPneuNaUltimaCurvaBaixa(
			boolean lascouPneuNaUltimaCurvaBaixa) {
		this.fritouPneuNaUltimaCurvaBaixa = lascouPneuNaUltimaCurvaBaixa;
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
		this.danificado = danificado;
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
