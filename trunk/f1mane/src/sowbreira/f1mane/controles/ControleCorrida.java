package sowbreira.f1mane.controles;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira
 */
public class ControleCorrida {
	private int distaciaCorrida;
	private int durabilidadeMaxMotor;
	private int tanqueCheio;
	private int voltaAtual;
	private int qtdeTotalVoltas;
	private ControleJogoLocal controleJogo;
	private ControleCiclo controleCiclo;
	private ControleBox controleBox;
	private ControleSafetyCar controleSafetyCar;
	private ControleClima controleClima;
	private ControleQualificacao controleQualificacao;
	private double fatorUtrapassagem;
	private double indexVelcidadeDaPista;
	private long tempoCiclo;
	private boolean corridaPausada;
	private boolean corridaIniciada;
	private double fatorAcidente = .80;

	public long getTempoCiclo() {
		return tempoCiclo;
	}

	public ControleCorrida(ControleJogoLocal jogo, int qtdeVoltas,
			double fatorUtr, long tempoCiclo) throws Exception {
		controleJogo = jogo;
		this.tempoCiclo = (tempoCiclo < 50 ? 50 : tempoCiclo);
		this.fatorUtrapassagem = fatorUtr / 1000;
		if (this.fatorUtrapassagem > 0.5) {
			this.fatorUtrapassagem = 0.5;
		}
		this.fatorUtrapassagem = 1.0 - this.fatorUtrapassagem;
		this.indexVelcidadeDaPista = 1.5;
		if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
			this.indexVelcidadeDaPista = 1.6;
		}
		if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
			this.indexVelcidadeDaPista = 1.4;
		}
		int valCalc = (qtdeVoltas < 12 ? 12 : qtdeVoltas);
		distaciaCorrida = jogo.getNosDaPista().size() * valCalc;
		definirDurabilidadeMotores();
		qtdeTotalVoltas = qtdeVoltas;
		controleBox = new ControleBox(controleJogo, this);
		controleSafetyCar = new ControleSafetyCar(controleJogo, this);
		controleClima = new ControleClima(controleJogo, qtdeTotalVoltas);
		controleCiclo = new ControleCiclo(controleJogo, this, tempoCiclo);
		controleQualificacao = new ControleQualificacao(controleJogo,
				controleBox);
		if (controleJogo.isSemReabastacimento()) {
			tanqueCheio = (distaciaCorrida + Util.inte(distaciaCorrida / 1.4));
		} else
			tanqueCheio = (distaciaCorrida + (distaciaCorrida / 2));
		definirTanqueCheio();
	}

	public ControleBox getControleBox() {
		return controleBox;
	}

	public ControleQualificacao getControleQualificacao() {
		return controleQualificacao;
	}

	private void definirTanqueCheio() {
		List pilotos = controleJogo.getPilotos();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			piloto.getCarro().setTanqueCheio(tanqueCheio);
		}

	}

	private void definirDurabilidadeMotores() {
		int valCalc = (qtdeTotalVoltas < 12 ? 12 : qtdeTotalVoltas);
		durabilidadeMaxMotor = (int) (distaciaCorrida * 1.4);
		int somaPontecias = 0;
		for (int i = 0; i < controleJogo.getCarros().size(); i++) {
			Carro carro = (Carro) controleJogo.getCarros().get(i);
			somaPontecias += carro.getPotencia();
		}
		int mediaPontecia = somaPontecias / controleJogo.getCarros().size();
		for (int i = 0; i < controleJogo.getPilotos().size(); i++) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			piloto.getCarro().setDurabilidadeMaxMotor(durabilidadeMaxMotor,
					mediaPontecia);
		}

	}

	public ControleClima getControleClima() {
		return controleClima;
	}

	public int getQtdeTotalVoltas() {
		return qtdeTotalVoltas;
	}

	public void terminarCorrida() {
		qtdeTotalVoltas = voltaAtual + 1;
	}

	public int getDistaciaCorrida() {
		return distaciaCorrida;
	}

	public boolean isCorridaPausada() {
		return corridaPausada;
	}

	public void setCorridaPausada(boolean corridaPausada) {
		this.corridaPausada = corridaPausada;
	}

	public void gerarGridLargadaSemQualificacao() {
		controleQualificacao.gerarGridLargadaSemQualificacao();
	}

	public void iniciarCorrida() {
		Logger.logar("iniciarCorrida()");
		controleJogo.selecionaPilotoJogador();
		Logger.logar("selecionaPilotoJogador()");
		controleJogo.atualizaPainel();
		Logger.logar("atualizaPainel()");
		controleCiclo.start();
		Logger.logar("controleCiclo.start()");
		corridaIniciada = true;
		Logger.logar("corridaIniciada = true");
	}

	protected void finalize() throws Throwable {
		super.finalize();

		if (controleCiclo != null) {
			controleCiclo.setProcessadoCilcos(false);
			controleCiclo.interrupt();
		}

		if (controleClima != null) {
			controleClima.matarThreads();
		}
		if (controleSafetyCar != null) {
			controleSafetyCar.matarThreads();
		}
	}

	public void atualizaClassificacao() {
		List pilotos = controleJogo.getPilotos();
		Collections.sort(pilotos, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Piloto piloto0 = (Piloto) arg0;
				Piloto piloto1 = (Piloto) arg1;
				int thisVal = piloto1.getPtosPista();
				int anotherVal = piloto0.getPtosPista();

				return ((thisVal < anotherVal) ? (-1)
						: ((thisVal == anotherVal) ? 0 : 1));
			}
		});

		for (int i = 0; i < pilotos.size(); i++) {
			Piloto piloto = (Piloto) pilotos.get(i);
			piloto.setPosicao(i + 1);
		}
	}

	public double verificaUltraPassagem(Piloto piloto, double ganho) {

		No noAtualCarro = piloto.getNoAtual();
		List listaPiloto = piloto.obterPista(controleJogo);

		int indCentroCarro = noAtualCarro.getIndex();
		double indFrenteCarro = indCentroCarro + ganho + Carro.MEIA_ALTURA;

		if (indFrenteCarro < 0) {
			indFrenteCarro = (listaPiloto.size() - 1) + indFrenteCarro;
		}
		if (indFrenteCarro > listaPiloto.size()) {
			indFrenteCarro = indFrenteCarro - (listaPiloto.size() - 1);
		}

		List pilotos = controleJogo.pilotos;
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoNaFrente = (Piloto) iterator.next();
			if (pilotoNaFrente.equals(piloto)) {
				continue;
			}

			if (((piloto.getPtosBox() > 0)) != (pilotoNaFrente.getPtosBox() > 0)) {
				continue;
			}
			if ((Carro.BATEU_FORTE.equals(pilotoNaFrente.getCarro()
					.getDanificado()) || pilotoNaFrente.getCarro()
					.isRecolhido())
					|| Carro.EXPLODIU_MOTOR.equals(pilotoNaFrente.getCarro()
							.getDanificado())
					|| Carro.PANE_SECA.equals(pilotoNaFrente.getCarro()
							.getDanificado())) {
				continue;
			}
			int indCentroCarroFrente = pilotoNaFrente.getNoAtual().getIndex();
			List listaCarroFrente = pilotoNaFrente.obterPista(controleJogo);

			double indTrazCarroFrente = indCentroCarroFrente
					- (Carro.MEIA_LARGURA);
			if (indTrazCarroFrente < 0) {
				indTrazCarroFrente = (listaCarroFrente.size() - 1)
						+ indTrazCarroFrente;
			}
			if (indTrazCarroFrente > listaCarroFrente.size()) {
				indTrazCarroFrente = indFrenteCarro
						- (listaCarroFrente.size() - 1);
			}

			double multi = 1.1;

			if (No.LARGADA.equals(noAtualCarro.getTipo())
					|| No.RETA.equals(noAtualCarro.getTipo())) {
				multi = 1.5;
			}
			if (No.CURVA_ALTA.equals(noAtualCarro.getTipo())) {
				multi = 1.3;
			}
			if ((Math.abs(indFrenteCarro - indTrazCarroFrente) < (multi * Carro.MEIA_LARGURA))
					&& (pilotoNaFrente.getTracado() == piloto.getTracado())) {
				if (Math.abs(indFrenteCarro - indTrazCarroFrente) < (Carro.MEIA_LARGURA)) {
					ajusteUltrapassagem(piloto, pilotoNaFrente);
				}
				if (!controleJogo.isCorridaTerminada()
						&& !piloto.isRecebeuBanderada()
						&& !controleJogo.verificaNivelJogo()
						&& pilotoNaFrente.testeHabilidadePiloto()
						&& pilotoNaFrente.getPtosPista() < piloto
								.getPtosPista()
						&& !pilotoNaFrente.isDesqualificado()
						&& (pilotoNaFrente.getPtosBox() == 0)
						&& pilotoNaFrente.isAutoPos()) {
					pilotoNaFrente.mudarTracado(Util.intervalo(1, 2),
							controleJogo, true);
					if (piloto.getPosicao() < 8) {
						if (Math.random() > 0.9) {
							if (!controleJogo.isSafetyCarNaPista()) {
								if (Math.random() > 0.5) {
									controleJogo.info(Html.azul(Lang.msg(
											"021",
											new String[] {
													pilotoNaFrente.getNome(),
													piloto.getNome() })));
								} else {
									controleJogo.info(Html.azul(Lang.msg(
											"020",
											new String[] {
													pilotoNaFrente.getNome(),
													piloto.getNome() })));
								}
							}
						}
					}
				} else {
					boolean sendoPressionado = false;
					Carro carroAtraz = controleJogo.obterCarroAtraz(piloto);
					if ((Carro.BATEU_FORTE.equals(pilotoNaFrente.getCarro()
							.getDanificado()) && !pilotoNaFrente.getCarro()
							.isRecolhido())
							|| Carro.PERDEU_AEREOFOLIO.equals(pilotoNaFrente
									.getCarro().getDanificado())
							|| Carro.PNEU_FURADO.equals(pilotoNaFrente
									.getCarro().getDanificado())
							|| pilotoNaFrente.isDesqualificado()) {
						int novapos = Util.intervalo(0, 2);
						while (novapos == pilotoNaFrente.getPosicao()) {
							novapos = Util.intervalo(0, 2);
						}
						piloto.mudarTracado(novapos, controleJogo, true);
					} else {
						if (carroAtraz != null) {
							Piloto pilotoAtraz = carroAtraz.getPiloto();
							if (piloto.testeHabilidadePiloto()) {
								multi = 2;
							}
							if (pilotoAtraz.getPtosPista() > (piloto
									.getPtosPista() - (multi * Carro.LARGURA))) {
								sendoPressionado = true;
							}
						}
						if (piloto.testeHabilidadePiloto() && !sendoPressionado
								&& piloto.isAutoPos()) {
							int novoTracado = Util.intervalo(0, 2);
							while (novoTracado == piloto.getTracado()) {
								novoTracado = Util.intervalo(0, 2);
							}
							piloto.mudarTracado(novoTracado, controleJogo);
						}

					}
				}

				double percent = 1 - fatorUtrapassagem;
				if (No.LARGADA.equals(noAtualCarro.getTipo())
						|| No.RETA.equals(noAtualCarro.getTipo())) {
					double val = ganho * (percent * 0.2);
					if (piloto.isJogadorHumano())
						Logger.logar("ganho * (percent * 0.2) " + val);
					return val;
				}
				if (No.CURVA_ALTA.equals(noAtualCarro.getTipo())) {
					double val = ganho * (percent * 0.4);
					if (piloto.isJogadorHumano())
						Logger.logar("ganho * (percent * 0.4) " + val);
					return val;

				}
				double val = ganho * (percent * 0.6);
				if (piloto.isJogadorHumano())
					Logger.logar("ganho * (percent * 0.6); " + val);
				return val;
			}
			Carro carroAtraz = controleJogo.obterCarroAtraz(piloto);
			if (carroAtraz != null) {
				Piloto pilotoAtraz = carroAtraz.getPiloto();
				multi = 2;
				if (piloto.testeHabilidadePiloto()) {
					multi = 3;
				}
				if (pilotoAtraz != null
						&& piloto.isAutoPos()
						&& pilotoAtraz.getPtosPista() > (piloto.getPtosPista() - (multi * Carro.LARGURA))) {
					// Logger.logar(piloto.getNome() + " pressionado por "
					// + carroAtraz.getPiloto().getNome());
					piloto.mudarTracado(0, controleJogo);
				}
			}
		}

		return ganho;
	}

	public Piloto acharPilotoDaFrente(Piloto piloto) {
		List piList = controleJogo.getPilotos();
		for (int i = 0; i < piList.size(); i++) {
			Piloto elementPiloto = (Piloto) piList.get(i);

			if (elementPiloto == piloto) {
				break;
			} else if (elementPiloto.getPosicao() == (piloto.getPosicao() - 1)) {
				return elementPiloto;
			}
		}
		return piloto;
	}

	public void verificaAcidenteUltrapassagem(boolean estadoPrevioAgressivo,
			Piloto piloto, Piloto pilotoNaFrente) {
		if (controleJogo.isSafetyCarNaPista()) {
			return;
		}
		if (piloto.getCarro().verificaDano()) {
			return;
		}
		double fatorAcidenteLocal = fatorAcidente;
		if (controleJogo.isChovendo()) {
			fatorAcidenteLocal -= .5;
		}
		if (piloto.isJogadorHumano()) {
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				fatorAcidenteLocal += .15;
			}
			if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				fatorAcidenteLocal -= .15;
			}
		}
		if ((Math.random() < fatorAcidenteLocal)) {
			return;
		}
		if (piloto.isJogadorHumano()) {
			if (piloto.isAgressivo()) {
				No noAtual = piloto.getNoAtual();
				if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
					if ((piloto.getStress() > 30)) {
						piloto.getCarro()
								.setDurabilidadeAereofolio(
										piloto.getCarro()
												.getDurabilidadeAereofolio() - 1);
					}
					if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo())
						piloto.incStress(30);
					if (InterfaceJogo.MEDIO_NV == controleJogo.getNiveljogo())
						piloto.incStress(20);
					if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo())
						piloto.incStress(10);
					controleJogo.infoPrioritaria(Lang.msg("109",
							new String[] { Html.superRed(piloto.getNome()),
									pilotoNaFrente.getNome() }));
				} else if ((noAtual.verificaCruvaBaixa() || noAtual
						.verificaCruvaAlta())
						&& (piloto.getStress() > 70)
						&& piloto.isAgressivo()
						&& !piloto.testeHabilidadePilotoCarro()) {
					piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
					if (piloto.getPosicao() <= 10)
						controleJogo.infoPrioritaria(Lang.msg("015",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
				} else if ((noAtual.verificaCruvaBaixa())
						&& (piloto.getStress() > 90)) {
					piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
					if (piloto.getPosicao() <= 10)
						controleJogo.infoPrioritaria(Lang.msg("015",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
				}
			}
		} else {
			if (!piloto.testeHabilidadePilotoCarro()) {
				if (((piloto.getCarro().getDurabilidadeAereofolio() <= 1 || (piloto
						.getCarro().getDurabilidadeAereofolio() <= 2)
						&& Math.random() > fatorAcidenteLocal))
						&& !controleSafetyCar.safetyCarUltimas3voltas()
						&& (controleJogo.getNumVoltaAtual() > 1)
						&& Math.random() > fatorAcidenteLocal
						&& !piloto.testeHabilidadePiloto()) {
					piloto.getCarro().setDanificado(Carro.BATEU_FORTE);
					controleJogo.infoPrioritaria(Lang.msg("016",
							new String[] { Html.superRed(piloto.getNome()),
									pilotoNaFrente.getNome() }));
					piloto.setDesqualificado(true);
					atualizaClassificacao();
					controleSafetyCar.safetyCarNaPista(piloto);
				} else {
					if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
						if (controleJogo.getNumVoltaAtual() == 1
								&& Math.random() < 0.5) {
							return;
						}
						piloto.getCarro()
								.setDurabilidadeAereofolio(
										piloto.getCarro()
												.getDurabilidadeAereofolio() - 1);
					} else {
						piloto.getCarro()
								.setDanificado(Carro.PERDEU_AEREOFOLIO);
						controleJogo.infoPrioritaria(Lang.msg("017",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
					}
				}
				return;
			}

		}

	}

	public static void main(String[] args) {
		Logger.logar(100 * (.950));
		// double fatorPerdaAreofolio = .995;
		//
		// fatorPerdaAreofolio -= (.7 / 10);
		//
		// Logger.logar(fatorPerdaAreofolio);
	}

	public void ajusteUltrapassagem(Piloto perdedor, Piloto ganhador) {
		if (perdedor.isDesqualificado() || ganhador.isDesqualificado()) {
			return;
		}
		if (!ganhador.isJogadorHumano())
			ganhador.setAgressivo(true);
		ganhador.setCiclosDesconcentrado(0);
		if (!controleJogo.isSafetyCarNaPista()) {
			if (perdedor.isJogadorHumano() && Math.random() > 0.950) {
				controleJogo.info(Lang.msg(
						"018",
						new String[] { Html.bold(perdedor.getNome()),
								Html.bold(ganhador.getNome()) }));
			}

			if (ganhador.isJogadorHumano() && Math.random() > 0.950) {
				controleJogo.info(Lang.msg(
						"019",
						new String[] { Html.bold(ganhador.getNome()),
								Html.bold(perdedor.getNome()) }));
			}
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
			perdedor.incStress(Util.intervalo(0, 1));
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
			perdedor.incStress(Util.intervalo(0, 2));
		}
		if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
			perdedor.incStress(Util.intervalo(1, 2));
		}
		if (perdedor.isJogadorHumano() && !ganhador.isJogadorHumano()) {
			return;
		}
		perdedor.setAgressivo(false);
		perdedor.gerarDesconcentracao((int) (100 - ((perdedor.getHabilidade() / 10) * controleJogo
				.getNiveljogo())));

	}

	public int porcentagemCorridaCompletada() {
		int maxAteAgora = ((Piloto) controleJogo.getPilotos().get(0))
				.getPtosPista();

		return (maxAteAgora * 100) / distaciaCorrida;
	}

	public int voltaAtual() {
		if (!controleJogo.isCorridaTerminada()) {
			voltaAtual = ((Piloto) controleJogo.getPilotos().get(0))
					.getNumeroVolta();
		}

		return voltaAtual;
	}

	public double getFatorUtrapassagem() {
		return fatorUtrapassagem;
	}

	public int getTanqueCheio() {
		return tanqueCheio;
	}

	public void processarPilotoBox(Piloto piloto) {
		controleBox.processarPilotoBox(piloto);
	}

	public void verificaFinalCorrida() {
		if (getQtdeTotalVoltas() == voltaAtual()) {
			controleJogo.setCorridaTerminada(true);

			Piloto piloto = (Piloto) controleJogo.getPilotos().get(0);
			piloto.setRecebeuBanderada(true, controleJogo);
		}

		if (controleJogo.isCorridaTerminada()) {
			List pilotos = controleJogo.getPilotos();
			boolean todosReceberamBaderada = true;

			for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();

				if (!piloto.isRecebeuBanderada() && !piloto.isDesqualificado()) {
					todosReceberamBaderada = false;
				}
			}

			if (todosReceberamBaderada) {
				controleCiclo.setProcessadoCilcos(false);
				controleJogo.exibirResultadoFinal();
			}
		}
	}

	public boolean verificaBoxOcupado(Carro carro) {
		return controleBox.verificaBoxOcupado(carro);
	}

	public int getCicloAtual() {
		return controleCiclo.getContadorCiclos();
	}

	public void pararThreads() {
		controleCiclo.setProcessadoCilcos(false);
	}

	public long obterTempoCilco() {
		return controleCiclo.getTempoCiclo();
	}

	public int calculaQtdePtsPistaPoleParaSaidaBox() {
		Piloto pole = (Piloto) controleJogo.getPilotos().get(0);
		return controleBox.calculaQtdePtsPistaPoleParaSaidaBox(pole);
	}

	public No getNoSaidaBox() {

		return controleBox.getSaidaBox();
	}

	public void processaVoltaSafetyCar() {
		if (!controleSafetyCar.isSaftyCarNaPista()) {
			return;
		}
		controleSafetyCar.processarCiclo();

	}

	public boolean isSafetyCarNaPista() {

		return controleSafetyCar.isSaftyCarNaPista();
	}

	public SafetyCar getSafetyCar() {

		return controleSafetyCar.getSafetyCar();
	}

	public boolean isSafetyCarVaiBox() {

		return controleSafetyCar.isSafetyCarVaiBox();
	}

	public Carro obterCarroNaFrente(Piloto piloto) {
		int pos = piloto.getPosicao() - 2;
		if (pos < 0) {
			return null;
		}
		if (pos > controleJogo.getPilotos().size() - 1) {
			return null;
		}
		return ((Piloto) controleJogo.getPilotos().get(pos)).getCarro();
	}

	public Carro obterCarroAtraz(Piloto piloto) {
		int pos = piloto.getPosicao();
		if (pos < 0) {
			return null;
		}
		if (pos > controleJogo.getPilotos().size() - 1) {
			return null;
		}
		return ((Piloto) controleJogo.getPilotos().get(pos)).getCarro();
	}

	public double getIndexVelcidadeDaPista() {
		return indexVelcidadeDaPista;
	}

	public void iniciarQualificacao(int tempoQualificacao) {
		controleQualificacao.iniciarQualificacao(tempoQualificacao);

	}

	public void iniciarCiclos() {
		controleCiclo.start();
	}

	public boolean isCorridaIniciada() {
		return corridaIniciada;
	}

	public void aumentaFatorAcidade() {
		fatorAcidente += 0.1;

	}

	public void diminueFatorAcidade() {
		fatorAcidente -= 0.1;
	}
}
