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

	public long getTempoCiclo() {
		return tempoCiclo;
	}

	public ControleCorrida(ControleJogoLocal jogo, int qtdeVoltas,
			double fatorUtrapassagem, double indexVelcidadeDaPista,
			long tempoCiclo) throws Exception {
		controleJogo = jogo;
		this.tempoCiclo = (tempoCiclo < 50 ? 50 : tempoCiclo);
		this.fatorUtrapassagem = fatorUtrapassagem / 1000;
		this.indexVelcidadeDaPista = indexVelcidadeDaPista / 1000;
		int valCalc = (qtdeVoltas < 22 ? 22 : qtdeVoltas);
		distaciaCorrida = jogo.getNosDaPista().size() * valCalc;

		definirDurabilidadeMotores();
		qtdeTotalVoltas = qtdeVoltas;
		tanqueCheio = (distaciaCorrida + (distaciaCorrida / 2));
		definirTanqueCheio();
		controleBox = new ControleBox(controleJogo, this);
		controleSafetyCar = new ControleSafetyCar(controleJogo, this);
		controleClima = new ControleClima(controleJogo, qtdeTotalVoltas);
		controleCiclo = new ControleCiclo(controleJogo, this, tempoCiclo);
		controleQualificacao = new ControleQualificacao(controleJogo,
				controleBox);
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
		int valCalc = (qtdeTotalVoltas < 22 ? 22 : qtdeTotalVoltas);
		durabilidadeMaxMotor = (int) (distaciaCorrida * 1.85)
				+ ((73 - valCalc) * 30);
		for (int i = 0; i < controleJogo.getPilotos().size(); i++) {
			Piloto piloto = (Piloto) controleJogo.getPilotos().get(i);
			piloto.getCarro().setDurabilidadeMaxMotor(durabilidadeMaxMotor);
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

	public boolean verificaNivelCorrida() {
		return (Math.random() < controleJogo.getNiveljogo());
	}

	public void gerarGridLargadaSemQualificacao() {
		controleQualificacao.gerarGridLargadaSemQualificacao();
	}

	public void iniciarCorrida() {
		controleJogo.selecionaPilotoJogador();
		controleJogo.atualizaPainel();
		controleCiclo.start();
	}

	protected void finalize() throws Throwable {
		super.finalize();

		if (controleCiclo != null) {
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

	public int verificaUltraPassagem(Piloto piloto, int index) {
		if (piloto.getPosicao() == 1) {
			return index;
		}
		Piloto pilotoNaFrente = acharPilotoDaFrente(piloto);
		if (piloto.equals(pilotoNaFrente)) {
			return index;
		}

		double fatorUtrapassagemTemp = fatorUtrapassagem;

		if (pilotoNaFrente.getCarro().verificaDano()) {
			fatorUtrapassagemTemp = .005;
		}

		if (pilotoNaFrente.entrouNoBox() || piloto.entrouNoBox()
				|| piloto.isDesqualificado() || isSafetyCarNaPista()) {
			return index;
		}

		if (fatorUtrapassagemTemp <= 0.3 && piloto.testeHabilidadePilotoCarro()
				&& piloto.getNoAtual().verificaRetaOuLargada()) {
			return 3;
		}
		if (pilotoNaFrente.getPtosPista() <= (piloto.getPtosPista() + index)) {
			if (piloto.testeHabilidadePilotoCarro()) {
				if ((Math.random() > fatorUtrapassagemTemp)) {
					ajusteUltrapassagem(pilotoNaFrente, piloto);

					return 2;
				} else if (!pilotoNaFrente.testeHabilidadePilotoCarro()
						&& (Math.random() > fatorUtrapassagemTemp)) {
					if (pilotoNaFrente.getPosicao() < 9
							&& Math.random() > 0.950) {

						controleJogo.info(Lang.msg("013", new Object[] {
								Html.bold(piloto.getNome()),
								Html.bold(pilotoNaFrente.getNome()) }));
					}

					ajusteUltrapassagem(pilotoNaFrente, piloto);

					return 3;
				} else {
					boolean estadoPrevio = piloto.isAgressivo();
					ajusteUltrapassagem(piloto, pilotoNaFrente);

					verificaAcidenteUltrapassagem(estadoPrevio, piloto,
							pilotoNaFrente);
					return 1;
				}
			}

			if (piloto.isJogadorHumano() && Math.random() > 0.950) {
				controleJogo.info(Lang.msg("014", new Object[] { Html
						.bold(piloto.getNome()) }));
			}
			if (!piloto.isJogadorHumano()) {
				piloto.setAgressivo(false);
				piloto.setCiclosDesconcentrado(100 - piloto.getHabilidade());
			}
			return 1;
		} else {
			return index;
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
			if (No.CURVA_BAIXA.equals(piloto.getNoAtual().getTipo())
					&& estadoPrevioAgressivo && (Math.random() > fatorAcidente)) {
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
						if (Math.random() < controleJogo.getNiveljogo())
							piloto.getCarro().setDurabilidadeAereofolio(
									piloto.getCarro()
											.getDurabilidadeAereofolio() - 1);
						if (piloto.isJogadorHumano())
							controleJogo.infoPrioritaria(Lang.msg("109",
									new String[] {
											Html.superRed(piloto.getNome()),
											pilotoNaFrente.getNome() }));
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
		System.out.println(100 * (.950));
		// double fatorPerdaAreofolio = .995;
		//
		// fatorPerdaAreofolio -= (.7 / 10);
		//
		// System.out.println(fatorPerdaAreofolio);
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

		if (perdedor.isJogadorHumano() && !ganhador.isJogadorHumano()) {
			return;
		}
		perdedor.setAgressivo(false);
		perdedor
				.gerarDesconcentracao((int) (100 - (perdedor.getHabilidade() * controleJogo
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

	public int verificaRetardatario(Piloto piloto, int index) {
		Piloto retardatario = acharRetardatarioMesmoNo(piloto);
		if (retardatario == null) {
			return index;
		}
		double fatorUtrapassagemTemp = fatorUtrapassagem / 3.0;

		if (retardatario.getCarro().verificaDano()) {
			fatorUtrapassagemTemp = .005;
		}

		if (retardatario.entrouNoBox() || piloto.entrouNoBox()
				|| piloto.isDesqualificado() || isSafetyCarNaPista()) {
			return index;
		}

		if (fatorUtrapassagemTemp <= 0.3 && piloto.testeHabilidadePilotoCarro()
				&& piloto.getNoAtual().verificaRetaOuLargada()) {
			return 3;
		}
		if (piloto.testeHabilidadePilotoCarro()) {
			if ((Math.random() > fatorUtrapassagemTemp)) {
				return index;
			} else if (!retardatario.testeHabilidadePilotoCarro()
					&& (Math.random() > fatorUtrapassagemTemp)) {
				if ((Math.random() > 0.95)
						&& (piloto.isJogadorHumano() || piloto.getPosicao() < 9)) {
					controleJogo.info(Lang.msg("020", new String[] {
							Html.azul(Html.bold(retardatario.getNome())),
							Html.bold(piloto.getNome()) }));
				}
				return 2;
			} else {
				if ((Math.random() > 0.95)
						&& (piloto.isJogadorHumano() || piloto.getPosicao() < 9)) {
					controleJogo.info(Lang.msg("021", new String[] {
							Html.azul(Html.bold(retardatario.getNome())),
							Html.bold(piloto.getNome()) }));
				}
				boolean estadoPrevio = piloto.isAgressivo();
				if ((Math.random() > 0.95)) {
					verificaAcidenteUltrapassagem(estadoPrevio, piloto,
							retardatario);
				}
				return 1;
			}
		}
		if (!piloto.isJogadorHumano()) {
			piloto.setAgressivo(false);
			piloto.setCiclosDesconcentrado(100 - piloto.getHabilidade());
		}
		return 1;

	}

	private Piloto acharRetardatarioMesmoNo(Piloto piloto) {
		List piList = controleJogo.getPilotos();
		No noPiloto = piloto.getNoAtual();
		No noFrente = null;
		List nos = controleJogo.getNosDaPista();
		for (int i = 0; i < nos.size(); i++) {
			No no = (No) nos.get(i);
			if (noPiloto == no) {
				if (i == (nos.size() - 1)) {
					noFrente = (No) nos.get(0);
				} else {
					noFrente = (No) nos.get(i + 1);
				}
			}
		}

		for (int i = 0; i < piList.size(); i++) {
			Piloto p = (Piloto) piList.get(i);
			if (piloto.getPosicao() > p.getPosicao()) {
				continue;
			}

			if (p == piloto) {
				continue;
			} else if ((p.getNoAtual() == noPiloto || p.getNoAtual() == noFrente)
					&& piloto.getPosicao() < p.getPosicao()
					&& piloto.getNumeroVolta() > p.getNumeroVolta()) {
				return p;
			}
		}
		return null;
	}
}
