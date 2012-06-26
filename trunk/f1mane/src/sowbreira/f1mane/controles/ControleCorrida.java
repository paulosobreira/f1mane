package sowbreira.f1mane.controles;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Constantes;
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
	private double fatorAcidente = (Util.intervalo(.7, .9));
	private long pontosPilotoLargada;
	private boolean asfaltoAbrasivo;

	public long getPontosPilotoLargada() {
		return pontosPilotoLargada;
	}

	public void setPontosPilotoLargada(long pontosPilotoLargada) {
		this.pontosPilotoLargada = pontosPilotoLargada;
	}

	public long getTempoCiclo() {
		return tempoCiclo;
	}

	public ControleCorrida(ControleJogoLocal jogo, int qtdeVoltas,
			double fatorUtr, long tempoCiclo) throws Exception {
		controleJogo = jogo;
		this.tempoCiclo = (tempoCiclo < Constantes.MIN_CICLO ? Constantes.MIN_CICLO
				: tempoCiclo);
		this.tempoCiclo = (this.tempoCiclo > Constantes.MAX_CICLO ? Constantes.MAX_CICLO
				: this.tempoCiclo);
		this.fatorUtrapassagem = fatorUtr / 1000;
		if (this.fatorUtrapassagem > 0.5) {
			this.fatorUtrapassagem = 0.5;
		}
		this.fatorUtrapassagem = 1.0 - this.fatorUtrapassagem;
		this.indexVelcidadeDaPista = 1.5;
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
		if (controleJogo.verificaNivelJogo()) {
			asfaltoAbrasivo = true;
		}
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
				long ptosPista0 = piloto0.getPtosPista();
				long ptosPista1 = piloto1.getPtosPista();
				if (piloto0.getTimeStampChegeda() != 0
						&& piloto1.getTimeStampChegeda() != 0) {
					Long val = new Long(Long.MAX_VALUE
							- piloto0.getTimeStampChegeda());
					val = new Long(val.toString().substring(
							val.toString().length() / 4,
							val.toString().length()));
					ptosPista0 = (val * piloto0.getNumeroVolta());
					val = new Long(Long.MAX_VALUE
							- piloto1.getTimeStampChegeda());
					val = new Long(val.toString().substring(
							val.toString().length() / 4,
							val.toString().length()));
					ptosPista1 = (val * piloto1.getNumeroVolta());
				}
				return new Long(ptosPista1).compareTo(new Long(ptosPista0));
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
						&& pilotoNaFrente.testeHabilidadePiloto(controleJogo)
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
							if (piloto.testeHabilidadePiloto(controleJogo)) {
								multi = 2;
							}
							if (pilotoAtraz.getPtosPista() > (piloto
									.getPtosPista() - (multi * Carro.LARGURA))) {
								sendoPressionado = true;
								piloto.incStress(piloto
										.testeHabilidadePiloto(controleJogo) ? 2
										: 4);
							}
						}
						if (piloto.testeHabilidadePiloto(controleJogo)
								&& !sendoPressionado && piloto.isAutoPos()) {
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
					return val;
				}
				if (No.CURVA_ALTA.equals(noAtualCarro.getTipo())) {
					double val = ganho * (percent * 0.4);
					return val;

				}
				double val = ganho * (percent * 0.6);
				return val;
			}
			Carro carroAtraz = controleJogo.obterCarroAtraz(piloto);
			if (carroAtraz != null) {
				Piloto pilotoAtraz = carroAtraz.getPiloto();
				multi = 2;
				if (piloto.testeHabilidadePiloto(controleJogo)) {
					multi = 3;
				}
				if (pilotoAtraz != null
						&& piloto.isAutoPos()
						&& pilotoAtraz.getPtosPista() > (piloto.getPtosPista() - (multi * Carro.LARGURA))) {
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
			fatorAcidenteLocal -= .2;
		}
		if (piloto.isJogadorHumano()
				&& piloto.getCarro().verificaPneusIncompativeisClima(
						controleJogo)) {
			fatorAcidenteLocal -= .2;
		}
		if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
			fatorAcidenteLocal -= .1;
		}
		if ((Math.random() < fatorAcidenteLocal)) {
			return;
		}
		if (piloto.isJogadorHumano()) {
			if (piloto.isAgressivo()) {
				No noAtual = piloto.getNoAtual();
				if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
					if ((piloto.getStress() > (5 * piloto.getCarro()
							.getDurabilidadeAereofolio()))) {
						piloto.getCarro()
								.setDurabilidadeAereofolio(
										piloto.getCarro()
												.getDurabilidadeAereofolio() - 1);
						if (InterfaceJogo.DIFICIL_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(30);
						if (InterfaceJogo.MEDIO_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(15);
						if (InterfaceJogo.FACIL_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(5);
						controleJogo.infoPrioritaria(Lang.msg("109",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
					}
				} else if ((noAtual.verificaCruvaAlta())
						&& Math.random() > fatorAcidenteLocal
						&& (piloto.getStress() > 70) && piloto.isAgressivo()
						&& !piloto.testeHabilidadePiloto(controleJogo)) {
					piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
					if (piloto.getPosicao() <= 10)
						controleJogo.infoPrioritaria(Lang.msg("015",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
				} else if ((noAtual.verificaCruvaBaixa())
						&& Math.random() > fatorAcidenteLocal
						&& (piloto.getStress() > 90)) {
					piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
					if (piloto.getPosicao() <= 10)
						controleJogo.infoPrioritaria(Lang.msg("015",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
				}
			}
		} else {
			if (!piloto.testeHabilidadePiloto(controleJogo)) {
				if (piloto.getCarro().getDurabilidadeAereofolio() <= 0
						&& Math.random() > fatorAcidenteLocal
						&& !controleSafetyCar.safetyCarUltimas3voltas()
						&& !piloto.testeHabilidadePiloto(controleJogo)) {
					if (piloto.testeHabilidadePiloto(controleJogo)
							|| Math.random() < fatorAcidenteLocal
							|| controleJogo.verificaUltimasVoltas()
							|| piloto.getStress() <= Util.intervalo(60, 80)) {
						piloto.incStress(Util.intervalo(30, 50));
						piloto.setCiclosDesconcentrado(Util.intervalo(100, 200));
					} else {
						piloto.getCarro().setDanificado(Carro.BATEU_FORTE);
						controleJogo.infoPrioritaria(Lang.msg("016",
								new String[] { Html.superRed(piloto.getNome()),
										pilotoNaFrente.getNome() }));
						piloto.setDesqualificado(true);
						controleSafetyCar.safetyCarNaPista(piloto);
					}
				} else {
					if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
						if (controleJogo.getNumVoltaAtual() <= 1
								&& Math.random() < fatorAcidenteLocal) {
							return;
						}
						piloto.incStress(Util.intervalo(40, 60));
						piloto.getCarro()
								.setDurabilidadeAereofolio(
										piloto.getCarro()
												.getDurabilidadeAereofolio() - 1);
					} else {
						if (piloto.getStress() > 90) {
							piloto.getCarro().setDanificado(
									Carro.PERDEU_AEREOFOLIO);
							controleJogo.infoPrioritaria(Lang.msg(
									"017",
									new String[] {
											Html.superRed(piloto.getNome()),
											pilotoNaFrente.getNome() }));
						} else {
							piloto.incStress(Util.intervalo(40, 50));
						}
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
		new Date(1);
		System.out.println(Util.intervalo(85, 95) / 100.0);
	}

	public void ajusteUltrapassagem(Piloto perdedor, Piloto ganhador) {
		if (perdedor.isDesqualificado() || ganhador.isDesqualificado()) {
			return;
		}
		if (!ganhador.isJogadorHumano())
			ganhador.setAgressivo(true, controleJogo);
		ganhador.setCiclosDesconcentrado(0);
		if (!controleJogo.isSafetyCarNaPista()) {
			if (perdedor.isJogadorHumano() && Math.random() > 0.950) {
				controleJogo.info(Lang.msg(
						"018",
						new String[] { Html.bold(perdedor.getNome()),
								Html.bold(ganhador.getNome()) }));
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
		perdedor.setAgressivo(false, controleJogo);
		perdedor.setCiclosDesconcentrado(Util.intervalo(200, 400));

	}

	public int porcentagemCorridaCompletada() {
		long maxAteAgora = ((Piloto) controleJogo.getPilotos().get(0))
				.getPtosPista();

		return (int) (maxAteAgora * 100) / distaciaCorrida;
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
		Piloto pole = (Piloto) controleJogo.getPilotos().get(0);
		int indexPole = pole.getNoAtual().getIndex();
		if (!pole.isRecebeuBanderada()
				&& voltaAtual() == (getQtdeTotalVoltas())) {
			controleJogo.setCorridaTerminada(true);
			pole.setRecebeuBanderada(true, controleJogo);
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
				controleJogo.infoPrioritaria(Html.red(Lang
						.msg("asfaltoAbrasivo")));
				atualizaClassificacao();
				controleJogo.exibirResultadoFinal();
				Logger.logar("========final corrida============");
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

	public long calculaQtdePtsPistaPoleParaSaidaBox() {
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
		Logger.logar("diminueFatorAcidade " + fatorAcidente);

	}

	public void diminueFatorAcidade() {
		fatorAcidente -= 0.1;
		Logger.logar("aumentaFatorAcidade " + fatorAcidente);
	}

	public boolean asfaltoAbrasivo() {
		return asfaltoAbrasivo;
	}

	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
			Piloto p) {
		return controleSafetyCar.ganhoComSafetyCar(ganho, controleJogo, p);
	}

	public void safetyCarNaPista(Piloto piloto) {
		controleSafetyCar.safetyCarNaPista(piloto);
	}
}
