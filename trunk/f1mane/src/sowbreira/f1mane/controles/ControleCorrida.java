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

	public long getTempoCiclo() {
		return tempoCiclo;
	}

	public ControleCorrida(ControleJogoLocal jogo, int qtdeVoltas,
			double fatorUtrapassagem, double indexVelcidadeDaPista,
			long tempoCiclo) throws Exception {
		controleJogo = jogo;
		this.tempoCiclo = (tempoCiclo < 50 ? 50 : tempoCiclo);
		this.fatorUtrapassagem = fatorUtrapassagem / 1000;
		if (fatorUtrapassagem > 0.4) {
			fatorUtrapassagem = 0.4;
		}
		this.fatorUtrapassagem = 1 - fatorUtrapassagem;
		this.indexVelcidadeDaPista = indexVelcidadeDaPista / 1000;
		if (indexVelcidadeDaPista < 0.5) {
			indexVelcidadeDaPista = 0.5;
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
		durabilidadeMaxMotor = (int) (distaciaCorrida * 1.85)
				+ ((73 - valCalc) * 30);
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

		controleJogo.selecionaPilotoJogador();
		controleJogo.atualizaPainel();
		controleCiclo.start();
		corridaIniciada = true;
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
		double indTrazCarro = indCentroCarro + ganho + Carro.LARGURA;

		if (indTrazCarro < 0) {
			indTrazCarro = (listaPiloto.size() - 1) + indTrazCarro;
		}
		if (indTrazCarro > listaPiloto.size()) {
			indTrazCarro = indTrazCarro - (listaPiloto.size() - 1);
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
			int indCentroCarroFrente = pilotoNaFrente.getNoAtual().getIndex();
			List listaCarroFrente = null;
			if (pilotoNaFrente.getPtosBox() > 0) {
				listaCarroFrente = controleJogo.getNosDoBox();
			} else {
				listaCarroFrente = controleJogo.getNosDaPista();
			}
			double indTrazCarroFrente = indCentroCarroFrente
					- (Carro.MEIA_LARGURA);
			if (indTrazCarroFrente < 0) {
				indTrazCarroFrente = (listaCarroFrente.size() - 1)
						+ indTrazCarroFrente;
			}
			if (indTrazCarroFrente > listaCarroFrente.size()) {
				indTrazCarroFrente = indTrazCarro
						- (listaCarroFrente.size() - 1);
			}
			double multi = 1;

			if (No.LARGADA.equals(noAtualCarro.getTipo())
					|| No.RETA.equals(noAtualCarro.getTipo())) {
				multi = 1.5;
			}
			if (No.CURVA_ALTA.equals(noAtualCarro.getTipo())) {
				multi = 1.3;
			}
			if ((((indTrazCarroFrente) < indTrazCarro) && (indTrazCarroFrente
					+ (multi * Carro.LARGURA) > (indTrazCarro)))
					&& pilotoNaFrente.getTracado() == piloto.getTracado()) {
				ajusteUltrapassagem(piloto, pilotoNaFrente);
				verificaAcidenteUltrapassagem(piloto.isAgressivo(), piloto,
						pilotoNaFrente);
				piloto.setAgressivo(false);
				if (piloto.testeHabilidadePiloto())
					piloto.mudarPos(Util.intervalo(0, 2), controleJogo);

				if (No.LARGADA.equals(noAtualCarro.getTipo())
						|| No.RETA.equals(noAtualCarro.getTipo())) {
					return ganho * 0.7;
				}
				if (No.CURVA_ALTA.equals(noAtualCarro.getTipo())) {
					return ganho * 0.5;
				}
				return ganho * 0.3;
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

	private void verificaAcidenteUltrapassagem(boolean estadoPrevioAgressivo,
			Piloto piloto, Piloto pilotoNaFrente) {
		if (piloto.getCarro().verificaDano()) {
			return;
		}
		double fatorAcidente = .995;
		if (controleJogo.isChovendo()) {
			fatorAcidente = .950;
		}
		if (piloto.isJogadorHumano()) {
			fatorAcidente -= (controleJogo.getNiveljogo() / 10);
			if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
				if (No.CURVA_BAIXA.equals(piloto.getNoAtual().getTipo())
						&& Math.random() < controleJogo.getNiveljogo()) {
					if (controleJogo.verificaNivelJogo()) {
						piloto
								.getCarro()
								.setDurabilidadeAereofolio(
										piloto.getCarro()
												.getDurabilidadeAereofolio() - 1);
						if (InterfaceJogo.DIFICIL_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(40);
						if (InterfaceJogo.MEDIO_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(30);
						if (InterfaceJogo.FACIL_NV == controleJogo
								.getNiveljogo())
							piloto.incStress(20);
					}
					controleJogo.infoPrioritaria(Lang.msg("109", new String[] {
							Html.superRed(piloto.getNome()),
							pilotoNaFrente.getNome() }));
				}
			} else if (No.CURVA_BAIXA.equals(piloto.getNoAtual().getTipo())
					&& (Math.random() > fatorAcidente && (piloto.getStress() > 70))) {
				piloto.getCarro().setDanificado(Carro.PERDEU_AEREOFOLIO);
				controleJogo.infoPrioritaria(Lang.msg("015", new String[] {
						Html.superRed(piloto.getNome()),
						pilotoNaFrente.getNome() }));
			}
		} else {
			if ((Math.random() > fatorAcidente) && estadoPrevioAgressivo) {
				if (Math.random() > 0.6
						&& Math.random() < controleJogo.getNiveljogo()
						&& !controleSafetyCar.safetyCarUltimas3voltas()) {
					piloto.getCarro().setDanificado(Carro.BATEU_FORTE);
					controleJogo.infoPrioritaria(Lang.msg("016", new String[] {
							Html.superRed(piloto.getNome()),
							pilotoNaFrente.getNome() }));
					piloto.setDesqualificado(true);
					atualizaClassificacao();
					controleSafetyCar.safetyCarNaPista(piloto);
				} else {
					if (piloto.getCarro().getDurabilidadeAereofolio() > 0) {
						if (Math.random() < controleJogo.getNiveljogo()) {
							piloto.getCarro().setDurabilidadeAereofolio(
									piloto.getCarro()
											.getDurabilidadeAereofolio() - 1);
						}
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

	private void ajusteUltrapassagem(Piloto perdedor, Piloto ganhador) {
		if (perdedor.isDesqualificado() || ganhador.isDesqualificado()) {
			return;
		}
		if (!ganhador.isJogadorHumano())
			ganhador.setAgressivo(true);
		ganhador.setCiclosDesconcentrado(0);

		if (perdedor.isJogadorHumano() && Math.random() > 0.950) {
			controleJogo.info(Lang.msg("018", new String[] {
					Html.bold(perdedor.getNome()),
					Html.bold(ganhador.getNome()) }));
		}

		if (ganhador.isJogadorHumano() && Math.random() > 0.950) {
			controleJogo.info(Lang.msg("019", new String[] {
					Html.bold(ganhador.getNome()),
					Html.bold(perdedor.getNome()) }));
		}
		if (controleJogo.verificaNivelJogo()) {
			perdedor.incStress(1);
		}
		if (perdedor.isJogadorHumano() && !ganhador.isJogadorHumano()) {
			return;
		}
		perdedor.setAgressivo(false);
		perdedor
				.gerarDesconcentracao((int) (100 - ((perdedor.getHabilidade() / 10) * controleJogo
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

	public int calculaModificadorComSafetyCar(Piloto piloto, int novoModificador) {
		return controleSafetyCar.calculaModificadorComSafetyCar(piloto,
				novoModificador);
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
}
