package sowbreira.f1mane.controles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Pausa;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.SafetyCar;
import sowbreira.f1mane.entidades.Volta;
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
	private double velocidadeJogo;
	private long tempoCiclo;
	private boolean corridaIniciada;
	private double fatorAcidente = Util.intervalo(0.5, 0.9);
	private long pontosPilotoLargada;
	private boolean asfaltoAbrasivo;
	private Pausa pausaAtual;
	private List tempoPausado = new ArrayList();

	public long getPontosPilotoLargada() {
		return pontosPilotoLargada;
	}

	public double getFatorAcidente() {
		return fatorAcidente;
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
		//qtdeVoltas = 1;
		this.tempoCiclo = Constantes.CICLO;
		this.fatorUtrapassagem = fatorUtr / 1000;
		if (this.fatorUtrapassagem > 0.5) {
			this.fatorUtrapassagem = 0.5;
		}
		this.fatorUtrapassagem = 1.0 - this.fatorUtrapassagem;
		this.velocidadeJogo = 1.5;
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
		if (Math.random() > 0.5) {
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
		return pausaAtual != null && pausaAtual.getPausaFimMilis() == 0;
	}

	public void setCorridaPausada(boolean corridaPausada) {
		if (pausaAtual == null) {
			pausaAtual = new Pausa();
			pausaAtual.setPausaIniMilis(System.currentTimeMillis());
		} else {
			pausaAtual.setPausaFimMilis(System.currentTimeMillis());
			tempoPausado.add(pausaAtual);
			pausaAtual = null;
		}
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

	public void verificaUltraPassagem(Piloto piloto) {
		if (controleJogo.isModoQualify()) {
			return;
		}
		Piloto pilotoNaFrente = piloto.getColisao();
		if (pilotoNaFrente == null) {
			return;
		}
		if (controleJogo.isSafetyCarNaPista()) {
			return;
		}
		if (piloto.getCarro().verificaDano()) {
			return;
		}
		if (!piloto.isColisaoDiantera()) {
			return;
		}
		verificaAcidenteUltrapassagem(piloto, pilotoNaFrente);
	}

	public void fazPilotoMudarTracado(Piloto piloto, Piloto pilotoNaFrente) {
		if (piloto.isAutoPos()) {
			int novapos = Util.intervalo(0, 2);
			int cont = 0;
			while (novapos == pilotoNaFrente.getTracado() && cont < 7) {
				novapos = Util.intervalo(0, 2);
				cont++;
			}
			piloto.mudarTracado(novapos, controleJogo,
					verificaCarroLentoOuDanificado(pilotoNaFrente));
		}
		if (verificaPassarRetardatario(piloto, pilotoNaFrente)) {
			pilotoNaFrente.setCiclosDesconcentrado(Util.intervalo(20, 50));
			mensagemRetardatario(piloto, pilotoNaFrente);
		}
	}

	public boolean verificaPassarRetardatario(Piloto piloto,
			Piloto pilotoNaFrente) {
		return !controleJogo.isCorridaTerminada()
				&& !piloto.isRecebeuBanderada()
				&& pilotoNaFrente.getPtosPista() < piloto.getPtosPista()
				&& !pilotoNaFrente.isDesqualificado()
				&& (pilotoNaFrente.getPtosBox() == 0);
	}

	public boolean verificaCarroLentoOuDanificado(Piloto pilotoNaFrente) {
		return (Carro.BATEU_FORTE.equals(pilotoNaFrente.getCarro()
				.getDanificado()) && !pilotoNaFrente.getCarro().isRecolhido())
				|| Carro.PERDEU_AEREOFOLIO.equals(pilotoNaFrente.getCarro()
						.getDanificado())
				|| Carro.PNEU_FURADO.equals(pilotoNaFrente.getCarro()
						.getDanificado()) || pilotoNaFrente.isDesqualificado();
	}

	public void mensagemRetardatario(Piloto piloto, Piloto pilotoNaFrente) {
		if (controleJogo.verificaInfoRelevante(piloto)) {
			if (Math.random() > 0.9) {
				if (!controleJogo.isSafetyCarNaPista()) {
					if (Math.random() > 0.5) {
						controleJogo.info(Html.azul(Lang.msg(
								"021",
								new String[] { pilotoNaFrente.getNome(),
										piloto.getNome() })));
					} else {
						controleJogo.info(Html.azul(Lang.msg(
								"020",
								new String[] { pilotoNaFrente.getNome(),
										piloto.getNome() })));
					}
					pilotoNaFrente.setModoPilotagem(Piloto.LENTO);
					pilotoNaFrente.incStress(50);
				}
			}
		}
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

	public void verificaAcidenteUltrapassagem(Piloto piloto,
			Piloto pilotoNaFrente) {
		double fatorAcidenteLocal = fatorAcidente;
		if (controleJogo.isChovendo()) {
			fatorAcidenteLocal -= .2;
		}
		if (piloto.isJogadorHumano()
				&& piloto.getCarro().verificaPneusIncompativeisClima(
						controleJogo)) {
			fatorAcidenteLocal -= .2;
		}
		if (fatorAcidenteLocal < 0.1) {
			fatorAcidenteLocal = 0.1;
		}
		if (Math.random() < fatorAcidenteLocal) {
			return;
		}
		if (piloto.isJogadorHumano()) {
			verificaAcidenteUltrapassagemJogadorHumano(piloto, pilotoNaFrente,
					fatorAcidenteLocal);
		} else {
			verificaAcidenteUltrapassagemIA(piloto, pilotoNaFrente,
					fatorAcidenteLocal);
		}
	}

	private void verificaAcidenteUltrapassagemIA(Piloto piloto,
			Piloto pilotoNaFrente, double fatorAcidenteLocal) {
		int stress = (int) (100 * fatorAcidenteLocal);
		if (piloto.getCarro().getDurabilidadeAereofolio() <= 0) {
			if (!controleSafetyCar.safetyCarUltimas3voltas()
					&& !piloto.isDesqualificado()
					&& piloto.getStress() > stress) {
				piloto.getCarro().setDanificado(Carro.BATEU_FORTE);
				Logger.logar(piloto.getNome() + " BATEU_FORTE");
				controleJogo.infoPrioritaria(Html.bold(Html.red(Lang.msg(
						"016",
						new String[] { piloto.getNome(),
								pilotoNaFrente.getNome() }))));
				piloto.setDesqualificado(true);
				controleSafetyCar.safetyCarNaPista(piloto);
			} else {
				if (piloto.getStress() > (stress / 2)) {
					perdeuAereofolio(piloto, pilotoNaFrente);
				} else {
					piloto.incStress(10);
				}
			}
		} else {
			if (piloto.getStress() > (stress / 2)) {
				danificaAreofolio(piloto);
			} else {
				piloto.incStress(10);
			}
		}
	}

	private void perdeuAereofolio(Piloto piloto, Piloto pilotoNaFrente) {
		piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
		Logger.logar(piloto.getNome() + " PERDEU_AEREOFOLIO");
		controleJogo.infoPrioritaria(Html.bold(Html.red(Lang.msg("017",
				new String[] { piloto.getNome(), pilotoNaFrente.getNome() }))));
	}

	private void verificaAcidenteUltrapassagemJogadorHumano(Piloto piloto,
			Piloto pilotoNaFrente, double fatorAcidenteLocal) {
		int stress = (int) (100 * fatorAcidenteLocal);
		if (!Piloto.AGRESSIVO.equals(piloto.getModoPilotagem())) {
			return;
		}
		No noAtual = piloto.getNoAtual();
		if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
			if (piloto.getStress() > stress) {
				danificaAreofolio(piloto);
				if (piloto.getPosicao() <= 5 || piloto.isJogadorHumano()) {
					controleJogo.infoPrioritaria(Html.superRed(Lang.msg(
							"109",
							new String[] { piloto.getNome(),
									pilotoNaFrente.getNome() })));
				}
			}
		} else if ((noAtual.verificaCruvaAlta())
				&& (piloto.getStress() > stress) && piloto.isAgressivo()) {
			piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
			controleJogo.infoPrioritaria(Lang.msg("015",
					new String[] { Html.superRed(piloto.getNome()),
							pilotoNaFrente.getNome() }));
		} else if ((noAtual.verificaCruvaBaixa())
				&& (piloto.getStress() > stress)) {
			piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
			controleJogo.infoPrioritaria(Lang.msg("015",
					new String[] { Html.superRed(piloto.getNome()),
							pilotoNaFrente.getNome() }));
		}
	}

	public void danificaAreofolio(Piloto piloto) {
		piloto.getCarro().setDurabilidadeAereofolio(
				piloto.getCarro().getDurabilidadeAereofolio() - 1);
	}

	public static void main(String[] args) {
		Logger.logar(100 * (.950));
		// double fatorPerdaAreofolio = .995;
		//
		// fatorPerdaAreofolio -= (.7 / 10);
		//
		// Logger.logar(fatorPerdaAreofolio);
		// new Date(1);
		// System.out.println(Util.intervalo(85, 95) / 100.0);
		// System.out.println(Util.intervalo(0.1, 0.9));
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

	/**
	 * Minimo 0.5 = Mais dificil de passar Maximo 1.0 = Mais facil de passar
	 */
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

	public Carro obterCarroNaFrenteRetardatario(Piloto piloto,
			boolean analisaTracado) {
		List<Piloto> pilotos = controleJogo.getPilotos();
		int menorDistancia = Integer.MAX_VALUE;
		Carro carroFrente = null;
		if (piloto.getPtosBox() != 0) {
			return carroFrente;
		}
		int indexAtual = piloto.getNoAtual().getIndex();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto pilotoFrente = (Piloto) iterator.next();
			if (pilotoFrente.getPtosBox() != 0) {
				continue;
			}
			if (pilotoFrente.getTracado() == 4
					|| pilotoFrente.getTracado() == 5) {
				continue;
			}
			if (analisaTracado
					&& pilotoFrente.getTracado() != piloto.getTracado()) {
				continue;
			}
			if (pilotoFrente.isDesqualificado()
					&& pilotoFrente.getCarro().isRecolhido()) {
				continue;
			}
			int indexFrente = pilotoFrente.getNoAtual().getIndex();
			if (indexFrente > indexAtual
					&& (indexFrente - indexAtual) < menorDistancia) {
				menorDistancia = (indexFrente - indexAtual);
				carroFrente = pilotoFrente.getCarro();
			}

			indexFrente += controleJogo.obterPista(piloto).size();

			if (indexFrente > indexAtual
					&& (indexFrente - indexAtual) < menorDistancia) {
				menorDistancia = (indexFrente - indexAtual);
				carroFrente = pilotoFrente.getCarro();
			}
		}
		return carroFrente;
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
		return velocidadeJogo;
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
		System.out.println("diminueFatorAcidade " + fatorAcidente);
	}

	public void diminueFatorAcidade() {
		fatorAcidente -= 0.1;
		Logger.logar("aumentaFatorAcidade " + fatorAcidente);
		System.out.println("aumentaFatorAcidade " + fatorAcidente);
	}

	public boolean asfaltoAbrasivo() {
		return asfaltoAbrasivo
				&& (Math.random() < ((double) controleJogo.getNumVoltaAtual() / (double) controleJogo
						.getQtdeTotalVoltas()));
	}

	public double ganhoComSafetyCar(double ganho, InterfaceJogo controleJogo,
			Piloto p) {
		return controleSafetyCar.ganhoComSafetyCar(ganho, controleJogo, p);
	}

	public void safetyCarNaPista(Piloto piloto) {
		controleSafetyCar.safetyCarNaPista(piloto);
	}

	public void descontaTempoPausado(Volta volta) {
		for (Iterator iterator = tempoPausado.iterator(); iterator.hasNext();) {
			Pausa pausa = (Pausa) iterator.next();
			if (volta.getCiclosInicio() <= pausa.getPausaIniMilis()
					&& volta.getCiclosFim() > pausa.getPausaFimMilis()) {
				volta.setTempoPausado(volta.getTempoPausado()
						+ (pausa.getPausaFimMilis() - pausa.getPausaIniMilis()));
			}
		}
	}

	public static Integer calculaPontos25(Piloto p) {
		if (p.getPosicao() == 1) {
			return new Integer(25);
		} else if (p.getPosicao() == 2) {
			return new Integer(18);
		} else if (p.getPosicao() == 3) {
			return new Integer(15);
		} else if (p.getPosicao() == 4) {
			return new Integer(12);
		} else if (p.getPosicao() == 5) {
			return new Integer(10);
		} else if (p.getPosicao() == 6) {
			return new Integer(8);
		} else if (p.getPosicao() == 7) {
			return new Integer(6);
		} else if (p.getPosicao() == 8) {
			return new Integer(4);
		} else if (p.getPosicao() == 9) {
			return new Integer(2);
		} else if (p.getPosicao() == 10) {
			return new Integer(1);
		} else {
			return new Integer(0);
		}
	}

	public void climaChuvoso() {
		controleClima.climaChuvoso();

	}

	public void climaEnsolarado() {
		controleClima.climaEnsolarado();
	}

	public boolean safetyCarUltimas3voltas() {
		return controleSafetyCar.safetyCarUltimas3voltas();
	}

}
