package sowbreira.f1mane.controles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Circuito;
import sowbreira.f1mane.entidades.Messagens;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.Logger;
import br.nnpe.Util;

/**
 * @author Paulo Sobreira Criado em 09/06/2007 as 17:17:28
 */
public class ControleBox {
	public static String UMA_OU_MAIS_PARADAS = "UMA_OU_MAIS_PARADAS";
	public static String UMA_PARADA = "UMA_UMA_PARADA";
	private No entradaBox;
	private No saidaBox;
	private No paradaBox;
	private int qtdeNosPistaRefBox;
	private InterfaceJogo controleJogo;
	private ControleCorrida controleCorrida;
	private Map boxEquipes;
	private Hashtable boxEquipesOcupado;
	private Circuito circuito;
	private List carrosBox;

	public List getCarrosBox() {
		return carrosBox;
	}

	/**
	 * @param controleJogo
	 * @param controleCorrida
	 * @throws Exception
	 */
	public ControleBox(InterfaceJogo controleJogo,
			ControleCorrida controleCorrida) throws Exception {
		super();
		this.controleJogo = controleJogo;
		this.controleCorrida = controleCorrida;
		entradaBox = (No) controleJogo.getCircuito().getPistaFull().get(
				controleJogo.getCircuito().getEntradaBoxIndex());
		paradaBox = (No) controleJogo.getCircuito().getBoxFull().get(
				controleJogo.getCircuito().getParadaBoxIndex());
		saidaBox = (No) controleJogo.getCircuito().getPistaFull().get(
				controleJogo.getCircuito().getSaidaBoxIndex());
		circuito = controleJogo.getCircuito();
		calculaQtdeNosPistaRefBox();
		if (saidaBox == null) {
			throw new Exception("Saida box não encontrada!");
		}

		geraBoxesEquipes(carrosBox);
	}

	public ControleBox() {
	}

	public void geraBoxesEquipes(List cBox) {
		this.carrosBox = cBox;
		boxEquipes = new HashMap();
		boxEquipesOcupado = new Hashtable();
		CarregadorRecursos carregadorRecursos = new CarregadorRecursos(false);
		try {
			if (carrosBox == null)
				carrosBox = carregadorRecursos
						.carregarListaCarrosArquivo(controleJogo.getTemporada()
								.replaceAll("\\*", ""));
		} catch (IOException e) {
			Logger.logarExept(e);
		}
		Collections.sort(carrosBox, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Carro carro0 = (Carro) arg0;
				Carro carro1 = (Carro) arg1;

				return new Integer(carro1.getPotencia()).compareTo(new Integer(
						carro0.getPotencia()));
			}
		});

		List ptosBox = controleJogo.getNosDoBox();

		int paradas = circuito.getParadaBoxIndex();

		int cont = 0;
		for (Iterator iter = carrosBox.iterator(); iter.hasNext();) {
			Carro carro = (Carro) iter.next();
			int indexParada = paradas + Util.inte(Carro.LARGURA * 1.5 * cont)
					+ Carro.LARGURA;
			if (cont < 12)
				cont++;
			boxEquipes.put(carro, ptosBox.get(indexParada));
		}
	}

	public void calculaNosBox(List pontosPista2, List pontosBox2)
			throws Exception {
		No boxEntrada = (No) pontosBox2.get(0);
		No boxSaida = (No) pontosBox2.get(pontosBox2.size() - 1);
		boolean entrada = false;
		boolean saida = false;

		for (Iterator iter = pontosPista2.iterator(); iter.hasNext();) {
			No noPista = (No) iter.next();

			if (GeoUtil.drawBresenhamLine(noPista.getPoint(),
					boxEntrada.getPoint()).size() < 3) {
				if (!entrada) {
					entradaBox = noPista;
				}

				entrada = true;
				noPista.setNoEntradaBox(entrada);
			}

			if (GeoUtil.drawBresenhamLine(noPista.getPoint(),
					boxSaida.getPoint()).size() < 3) {
				saida = true;
				noPista.setNoSaidaBox(saida);
				saidaBox = noPista;
			}
		}

		for (Iterator iter = pontosBox2.iterator(); iter.hasNext();) {
			No noBox = (No) iter.next();

			if (No.PARADA_BOX.equals(noBox.getTipo()) && paradaBox == null) {
				paradaBox = noBox;
			}
		}

		if (paradaBox == null) {
			throw new Exception("Parada de box não encontrrada");
		}

		if (!entrada) {
			throw new Exception("Entrada de box não encontrrada");
		}

		if (!saida) {
			throw new Exception("Saida de box não encontrrada");
		}
	}

	public void processarPilotoBox(Piloto piloto) {
		int cont = piloto.getNoAtual().getIndex();
		if (!(cont > (circuito.getEntradaBoxIndex() - 50) && cont < (circuito
				.getEntradaBoxIndex() + 50))
				&& (piloto.getPtosBox() <= 0)) {
			return;
		} else {
			if (boxEquipesOcupado == null) {
				boxEquipesOcupado = new Hashtable();
			}
			if (boxEquipesOcupado.get(piloto.getCarro()) == null) {
				boxEquipesOcupado.put(piloto.getCarro(), piloto.getCarro());
			}
			List boxList = controleJogo.getNosDoBox();
			No box = (No) boxEquipes.get(piloto.getCarro());
			if (piloto.getPtosBox() == 0
					&& (box.equals(piloto.getNoAtual()) || (cont > (circuito
							.getEntradaBoxIndex() - 75) && cont < (circuito
							.getEntradaBoxIndex() + 75)))) {
				if (piloto.getPosicao() < 8)
					controleJogo.info(Html.orange(Lang.msg("entraBox",
							new String[] { piloto.getNome() })));
				Logger.logar(piloto.getNome() + " Entrou no Box " + cont);
				piloto.setPtosBox(Util.inte((piloto.getPtosBox() + 1)
						* circuito.getMultiplciador()));
			} else {
				box = piloto.getNoAtual();
				int ptosBox = 0;
				if (box.isBox()) {
					/**
					 * gera limite velocidade no box
					 */
					ptosBox += 1;
				} else if (box.verificaRetaOuLargada()) {
					ptosBox += ((Math.random() > .1) ? 2 : 1);
				} else if (box.verificaCruvaAlta()) {
					ptosBox += ((Math.random() > .2) ? 2 : 1);
				} else if (box.verificaCruvaBaixa()) {
					ptosBox += ((Math.random() > .3) ? 2 : 1);
				}
				ptosBox *= circuito.getMultiplciador();
				if (piloto.verificaColisaoCarroFrente(controleJogo)) {
					piloto.mudarTracado(Util.intervalo(0, 2), controleJogo);
					ptosBox = 0;
				}
				int novosPtsBox = Util.inte(ptosBox) + piloto.getPtosBox();
				No nobox = (No) boxEquipes.get(piloto.getCarro());
				int indexParada = piloto.obterPista(controleJogo)
						.indexOf(nobox);
				if (novosPtsBox >= (indexParada - (Carro.MEIA_LARGURA))
						&& novosPtsBox <= (indexParada)) {
					piloto
							.setTracado(controleJogo.getCircuito().getLadoBox() == 1 ? 2
									: 1);
				} else {
					piloto.setTracado(0);
				}
				piloto.processaVelocidade(novosPtsBox, piloto.getNoAtual());

				piloto.setPtosBox(novosPtsBox);
			}

			if (piloto.getPtosBox() < boxList.size()) {
				if (controleJogo.getNiveljogo() == InterfaceJogo.DIFICIL_NV) {
					piloto.decStress(1);
				} else if (controleJogo.getNiveljogo() == InterfaceJogo.MEDIO_NV) {
					piloto.decStress(2);
				} else if (controleJogo.getNiveljogo() == InterfaceJogo.FACIL_NV) {
					piloto.decStress(3);
				}
				piloto.setNoAtual((No) boxList.get(piloto.getPtosBox()));
			} else {
				processarPilotoSairBox(piloto, controleJogo);
			}
		}

		No box = (No) boxEquipes.get(piloto.getCarro());
		int contBox = piloto.getNoAtual().getIndex();
		if ((contBox > (box.getIndex() - 15) && contBox < (box.getIndex() + 15))
				&& !piloto.decrementaParadoBox() && piloto.isBox()) {
			processarPilotoPararBox(piloto);
		}
	}

	public No getEntradaBox() {
		return entradaBox;
	}

	private void processarPilotoPararBox(Piloto piloto) {
		piloto.setVelocidade(0);
		int qtdeCombust = 0;
		piloto.setTracado(controleJogo.getCircuito().getLadoBox() == 1 ? 2 : 1);
		if (!piloto.isJogadorHumano()) {
			if (piloto.getCarro().verificaDano()) {
				if (controleCorrida.porcentagemCorridaCompletada() < 35) {
					qtdeCombust = setupParadaUnica(piloto);
				} else {
					qtdeCombust = setupDuasOuMaisParadas(piloto);
				}
			} else if (UMA_OU_MAIS_PARADAS.equals(piloto.getSetUpIncial())
					|| controleJogo.isSemReabastacimento()) {
				qtdeCombust = setupDuasOuMaisParadas(piloto);
			} else {
				qtdeCombust = setupParadaUnica(piloto);
			}
		} else {
			Integer combust = controleJogo.getCombustBox(piloto);
			if (controleJogo.isSemReabastacimento()) {
				combust = new Integer(0);
			}
			qtdeCombust = controleJogo.setUpJogadorHumano(piloto, controleJogo
					.getTipoPeneuBox(piloto), combust, controleJogo
					.getAsaBox(piloto));
		}

		int porcentCombust = (100 * qtdeCombust)
				/ controleCorrida.getTanqueCheio();
		long penalidade = 0;
		Carro carro = (Carro) boxEquipesOcupado.get(piloto.getCarro());
		if (carro != null && !carro.getPiloto().equals(piloto)) {
			controleJogo.info(Html.orange(Lang.msg("298", new String[] { carro
					.getNome() })));
			penalidade = 15;
			if (piloto.isJogadorHumano()) {
				if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
					penalidade = 20;
				}
				if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
					penalidade = 5;
				}
			}
			penalidade = Util.inte(penalidade
					* (2 - (carro.getPotencia() / 1000)));
		}
		carro = piloto.getCarro();

		carro.setMotor(carro.getMotor()
				+ (Util.inte(carro.getDurabilidadeMaxMotor()
						* Util.intervalo(1, 5) / 100.0)));
		carro.setPotencia(carro.getPotencia() + Util.intervalo(-5, +5));
		if (carro.getDurabilidadeAereofolio() <= 0) {
			penalidade = Util.inte(penalidade
					* (2 - (carro.getPotencia() / 1000)));
		}
		piloto.gerarCiclosPadoBox(porcentCombust, controleCorrida
				.obterTempoCilco(), penalidade);
		piloto.setParouNoBoxMilis(System.currentTimeMillis());
		piloto.setSaiuDoBoxMilis(0);
		if (piloto.isJogadorHumano()) {
			controleJogo
					.infoPrioritaria(Html.orange(Lang.msg("002", new String[] {
							piloto.getNome(),
							String.valueOf(controleJogo.getNumVoltaAtual()) })));
		} else if (piloto.getPosicao() < 9) {
			controleJogo.info(Html.orange(Lang.msg("002", new String[] {
					piloto.getNome(),
					String.valueOf(controleJogo.getNumVoltaAtual()) })));
		}
		carro.setDanificado(null);
		if (carro.getDurabilidadeAereofolio() <= 0
				|| InterfaceJogo.DIFICIL_NV != controleJogo.getNiveljogo()) {
			int durabilidade = 5;
			if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
				durabilidade = 6;
			}
			if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
				durabilidade = 4;
			}
			carro.setDurabilidadeAereofolio(durabilidade);
			penalidade = Util.inte(penalidade
					* (2 - (carro.getPotencia() / 1000)));
		}
		if (controleJogo.isKers()) {
			piloto.getCarro().setCargaKers(InterfaceJogo.CARGA_KERS);
			piloto.setAtivarKers(false);
		}
		piloto.getCarro().setTemperaturaMotor(0);
		piloto.setBox(false);
	}

	private void processarPilotoSairBox(Piloto piloto,
			InterfaceJogo interfaceJogo) {
		piloto.setNoAtual(saidaBox);
		// Todo Ptos Pista
		piloto.setPtosPista(piloto.getPtosPista() + qtdeNosPistaRefBox);
		long diff = piloto.getSaiuDoBoxMilis() - piloto.getParouNoBoxMilis();
		String[] strings = new String[] { piloto.getNome(),
				ControleEstatisticas.formatarTempo(diff),
				String.valueOf(piloto.getPorcentagemCombustUltimaParadaBox()),
				Lang.msg(piloto.getCarro().getTipoPneu()) };
		String info = Lang.msg("003", strings);
		if (piloto.isJogadorHumano()) {
			controleJogo.infoPrioritaria(Html.orange(info));
		} else if (piloto.getPosicao() < 9) {
			controleJogo.info(Html.orange(info));
		}

		boxEquipesOcupado.remove(piloto.getCarro());
		if (controleJogo.isCorridaTerminada()) {
			piloto.setRecebeuBanderada(true, controleJogo);
		}
		if (controleJogo.isSafetyCarNaPista() && piloto.getVoltaAtual() != null) {
			piloto.getVoltaAtual().setVoltaSafetyCar(true);
		}
		piloto.efetuarSaidaBox(interfaceJogo);

	}

	public int setupParadaUnica(Piloto piloto) {
		if (controleJogo.isChovendo()) {
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_CHUVA,
					controleCorrida.getDistaciaCorrida());
			piloto.setSetUpIncial(UMA_OU_MAIS_PARADAS);
		} else {
			int voltaAtual = piloto.getNumeroVolta();
			int metade = controleJogo.getQtdeTotalVoltas() / 2;
			if (voltaAtual > metade && piloto.testeHabilidadePiloto()) {
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_MOLE,
						controleCorrida.getDistaciaCorrida());
				Logger.logar("setupParadaUnica com Pneu Mole "
						+ piloto.getNome());
			} else {
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_DURO,
						controleCorrida.getDistaciaCorrida());
			}

		}
		if (piloto.testeHabilidadePiloto())
			processarTipoAsaAutomatico(piloto);

		int percentagem = 0;

		int consumoMedio = (int) piloto.calculaConsumoMedioCombust();
		piloto.limparConsumoMedioCombust();
		if (consumoMedio == 0) {
			if (piloto.getQtdeParadasBox() == 0) {
				percentagem = 40 + ((int) (Math.random() * 50));
			} else if (piloto.getQtdeParadasBox() == 1) {
				percentagem = 30 + ((int) (Math.random() * 30));
			} else {
				percentagem = 10 + ((int) (Math.random() * 30));
			}

			if (piloto.getCarro().verificaDano()) {
				percentagem = 70;
			}
		} else {
			int qtdeVoltRest = controleJogo.getQtdeTotalVoltas()
					- controleJogo.getNumVoltaAtual();
			percentagem = (consumoMedio * (qtdeVoltRest)) + 15;
		}

		int qtddeCombust = (controleCorrida.getTanqueCheio() * percentagem) / 100;
		if (controleJogo.isSemReabastacimento()) {
			qtddeCombust = 0;
		}
		int diffCombust = qtddeCombust - piloto.getCarro().getCombustivel();

		if (diffCombust < 0) {
			return 0;
		}

		piloto.getCarro().setCombustivel(
				qtddeCombust + piloto.getCarro().getCombustivel());

		return diffCombust;
	}

	public void setupCorridaQualificacaoAleatoria(Piloto piloto, int posicao) {

		if (piloto.isJogadorHumano()) {
			controleJogo.setUpJogadorHumano(piloto, controleJogo
					.getTipoPeneuBox(piloto), controleJogo
					.getCombustBox(piloto), controleJogo.getAsaBox(piloto));

			return;
		}

		if ((Math.random() > .5) && (posicao > 8)) {
			piloto.setSetUpIncial(UMA_PARADA);
			setupParadaUnica(piloto);

		} else {
			piloto.setSetUpIncial(UMA_OU_MAIS_PARADAS);
			setupDuasOuMaisParadas(piloto);

		}
		if (controleJogo.isSemReabastacimento()) {
			double mod = 0.9;
			if (piloto.testeHabilidadePiloto()) {
				mod = 0.8;
			}
			if (piloto.testeHabilidadePilotoCarro()) {
				mod = 0.7;
			}
			piloto.getCarro().setCombustivel(
					(int) (controleCorrida.getTanqueCheio() * mod));
		}

		if (controleCorrida.getControleClima().isClimaAleatorio()) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				Logger.logarExept(e);
			}
			int val = 1 + ((int) (Math.random() * 3));
			switch (val) {
			case 1:
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_DURO,
						controleCorrida.getDistaciaCorrida());

				break;
			case 2:
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_MOLE,
						controleCorrida.getDistaciaCorrida());

				break;

			case 3:
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_CHUVA,
						controleCorrida.getDistaciaCorrida());

				break;

			default:
				break;
			}
		}

		processarTipoAsaAutomatico(piloto);

	}

	private void processarTipoAsaAutomatico(Piloto piloto) {
		int noAlta = 0;
		int noMedia = 0;
		int noBaixa = 0;
		List list = controleJogo.getCircuito().geraPontosPista();
		for (Iterator iterator = list.iterator(); iterator.hasNext();) {
			No no = (No) iterator.next();
			if (no.verificaRetaOuLargada()) {
				noAlta++;
			}
			if (no.verificaCruvaAlta()) {
				noMedia++;
			}
			if (no.verificaCruvaBaixa()) {
				noBaixa++;
			}
		}
		double total = noAlta + noMedia + noBaixa;
		int alta = (int) (100 * noAlta / total);
		int media = (int) (100 * noMedia / total);
		int baixa = (int) (100 * noBaixa / total);
		if (alta >= 78 && piloto.testeHabilidadePilotoCarro()) {
			piloto.getCarro().setAsa(Carro.MENOS_ASA);
		}
		if (baixa >= 15 && piloto.testeHabilidadePilotoCarro()) {
			piloto.getCarro().setAsa(Carro.MAIS_ASA);
		}
		if (media >= 25 && piloto.testeHabilidadePilotoCarro()) {
			piloto.getCarro().setAsa(Carro.MAIS_ASA);
		}
		if (media >= 15 && piloto.testeHabilidadePilotoCarro()) {
			piloto.getCarro().setAsa(Carro.ASA_NORMAL);
		}
		if (controleJogo.isChovendo() && piloto.testeHabilidadePiloto()) {
			piloto.getCarro().setAsa(Carro.MAIS_ASA);
		}

	}

	public int setupDuasOuMaisParadas(Piloto piloto) {
		if (controleJogo.isChovendo()) {
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_CHUVA,
					controleCorrida.getDistaciaCorrida());
		} else {
			if (controleJogo.isSemReabastacimento()
					&& !controleJogo.isKers()
					&& !controleJogo.isDrs()
					&& piloto.getNumeroVolta() > 1
					&& piloto.getNumeroVolta() < (controleJogo
							.getQtdeTotalVoltas() / 2))
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_DURO,
						controleCorrida.getDistaciaCorrida());
			else
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_MOLE,
						controleCorrida.getDistaciaCorrida());
		}
		if (piloto.testeHabilidadePiloto())
			processarTipoAsaAutomatico(piloto);

		int percentagem = 0;

		int consumoMedioCombustivel = (int) piloto.calculaConsumoMedioCombust();

		if (!controleJogo.isSemReabastacimento())
			piloto.limparConsumoMedioCombust();
		if (!controleJogo.isSemTrocaPneu())
			piloto.limparConsumoMedioPneus();

		if (consumoMedioCombustivel == 0) {
			if (controleJogo.getNumVoltaAtual() == 0) {
				percentagem = Util.intervalo(40, 60);
			} else {
				if (piloto.getQtdeParadasBox() == 0) {
					percentagem = 50 + ((int) (Math.random() * 50));
				} else if (piloto.getQtdeParadasBox() == 1) {
					percentagem = 40 + ((int) (Math.random() * 40));
				} else {
					percentagem = 20 + ((int) (Math.random() * 30));
				}
			}
			if (piloto.getCarro().verificaDano()) {
				percentagem = 70;
			}
		} else {
			int qtdeVoltRest = controleJogo.getQtdeTotalVoltas()
					- controleJogo.getNumVoltaAtual();
			percentagem = (consumoMedioCombustivel * (qtdeVoltRest / 2)) + 15;
		}

		int qtddeCombust = (controleCorrida.getTanqueCheio() * percentagem) / 100;
		if (controleJogo.isSemReabastacimento()) {
			qtddeCombust = 0;
		}
		int diffCombust = qtddeCombust - piloto.getCarro().getCombustivel();

		if (diffCombust < 0) {
			return 0;
		}
		piloto.getCarro().setCombustivel(
				qtddeCombust + piloto.getCarro().getCombustivel());

		return diffCombust;
	}

	public void calculaQtdeNosPistaRefBox() {
		List ptosPista = controleJogo.getNosDaPista();
		int ateFim = ptosPista.size() - ptosPista.indexOf(entradaBox);
		int ateSaidaBox = ptosPista.indexOf(saidaBox);
		qtdeNosPistaRefBox = Util.inte((ateFim + ateSaidaBox));
		Logger.logar("qtdeNosPistaRefBox " + qtdeNosPistaRefBox);
	}

	public boolean verificaBoxOcupado(Carro carro) {
		if ((boxEquipesOcupado.get(carro)) != null) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		// int val = 1 + ((int) (Math.random() * 3));
		// Logger.logar(val);
		// Hashtable hashtable = new Hashtable();
		// hashtable.put("someval", null);
		System.out.println("asdk*hjsak".replaceAll("\\*", ""));
	}

	public No getSaidaBox() {
		return saidaBox;
	}

	public int calculaQtdePtsPistaPoleParaSaidaBox(Piloto pole) {
		List ptsPista = controleJogo.getNosDaPista();
		int diferenca = 0;
		int indexPole = 0;
		int indexSaidaBox = 0;
		for (int i = 0; i < ptsPista.size(); i++) {
			No no = (No) ptsPista.get(i);
			if (no.equals(pole.getNoAtual())) {
				indexPole = i;
			}
			if (no.equals(saidaBox)) {
				indexSaidaBox = i;
			}
		}
		diferenca = pole.getPtosPista() + (indexSaidaBox - indexPole);
		if (indexSaidaBox > indexPole) {
			diferenca = pole.getPtosPista() + (indexSaidaBox - indexPole);
		} else {
			diferenca = pole.getPtosPista()
					+ ((ptsPista.size() - indexPole) + indexSaidaBox);
		}
		return diferenca;
	}

	public No getParadaBox() {
		return paradaBox;
	}

}
