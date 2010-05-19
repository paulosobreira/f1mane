package sowbreira.f1mane.controles;

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
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.GeoUtil;
import br.nnpe.Html;
import br.nnpe.Logger;

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
		saidaBox = (No) controleJogo.getCircuito().getBoxFull().get(
				controleJogo.getCircuito().getSaidaBoxIndex());
		calculaQtdeNosPistaRefBox();
		circuito = controleJogo.getCircuito();
		if (saidaBox == null) {
			throw new Exception("Saida box não encontrada!");
		}

		geraBoxesEquipes();
	}

	public ControleBox() {
	}

	private void geraBoxesEquipes() {
		// TODO MUltiplicador
		boxEquipes = new HashMap();
		boxEquipesOcupado = new Hashtable();

		List pilots = controleJogo.getPilotos();
		Map mapCarros = new HashMap();
		List carros = new ArrayList();
		for (Iterator iterator = pilots.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (!mapCarros.containsKey(piloto.getCarro().getNome())) {
				mapCarros.put(piloto.getCarro().getNome(), piloto.getCarro()
						.getNome());
				carros.add(piloto.getCarro());
			}

		}
		Collections.sort(carros, new Comparator() {
			public int compare(Object arg0, Object arg1) {
				Carro carro0 = (Carro) arg0;
				Carro carro1 = (Carro) arg1;

				return new Integer(carro1.getPotencia()).compareTo(new Integer(
						carro0.getPotencia()));
			}
		});

		List ptosBox = controleJogo.getNosDoBox();
		int indexParada = ptosBox.indexOf(paradaBox);

		for (Iterator iter = carros.iterator(); iter.hasNext();) {
			Carro carro = (Carro) iter.next();

			if (indexParada > (ptosBox.size() - 1)) {
				indexParada = ptosBox.size() - 1;
			}

			boxEquipes.put(carro, ptosBox.get(indexParada));
			boxEquipesOcupado.put(carro, "");
			indexParada += (Carro.LARGURA * 2);
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
		if (!(cont > (circuito.getEntradaBoxIndex() - 100) && cont < (circuito
				.getEntradaBoxIndex() + 100))
				&& (piloto.getPtosBox() <= 0)) {
			return;
		} else {
			if (boxEquipesOcupado == null) {
				boxEquipesOcupado = new Hashtable();
			}
			if ("".equals(boxEquipesOcupado.get(piloto.getCarro()))) {
				boxEquipesOcupado.put(piloto.getCarro(), piloto.getCarro());
			}
			List boxList = controleJogo.getNosDoBox();
			No box = (No) boxEquipes.get(piloto.getCarro());
			if (box.equals(piloto.getNoAtual())
					|| (cont > (circuito.getEntradaBoxIndex() - 100) && cont < (circuito
							.getEntradaBoxIndex() + 100))) {
				piloto.setPtosBox(piloto.getPtosBox() + 1);
			} else {
				box = piloto.getNoAtual();
				int ptosBox = 0;
				if (box.isBox()) {
					/**
					 * gera limite velocidade no box
					 */
					ptosBox += 1;
				} else if (box.verificaRetaOuLargada()) {
					ptosBox += ((Math.random() > .8) ? 3 : 2);
				} else if (box.verificaCruvaAlta()) {
					ptosBox += ((Math.random() > .8) ? 2 : 1);
					;
				} else {
					ptosBox += 1;
				}
				ptosBox *= circuito.getMultiplciador();
				piloto.processaVelocidade(ptosBox, piloto.getNoAtual());
				piloto.setPtosBox(ptosBox + piloto.getPtosBox());
			}

			if (piloto.getPtosBox() < boxList.size()) {
				piloto.decStress(1);
				piloto.setNoAtual((No) boxList.get(piloto.getPtosBox()));
			} else {
				processarPilotoSairBox(piloto, controleJogo);
			}
		}

		No box = (No) boxEquipes.get(piloto.getCarro());
		int contBox = piloto.getNoAtual().getIndex();
		if ((contBox > (circuito.getParadaBoxIndex() - 10) && contBox < (circuito
				.getParadaBoxIndex() + 10))
				&& !piloto.decrementaParadoBox()) {
			processarPilotoPararBox(piloto);
		}
	}

	private void processarPilotoPararBox(Piloto piloto) {
		piloto.setVelocidade(0);
		int qtdeCombust = 0;
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
			penalidade = 30;
			if (piloto.isJogadorHumano()) {
				if (InterfaceJogo.DIFICIL_NV == controleJogo.getNiveljogo()) {
					penalidade = 50;
				}
				if (InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo()) {
					penalidade = 20;
				}
			}
		}
		piloto.gerarCiclosPadoBox(porcentCombust, controleCorrida
				.obterTempoCilco(), penalidade);
		piloto.getCarro().ajusteMotorParadaBox();
		piloto.setParouNoBoxMilis(System.currentTimeMillis());
		if (piloto.getNumeroVolta() > 0)
			piloto.processaVoltaNovaBox(controleJogo);
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

	}

	private void processarPilotoSairBox(Piloto piloto,
			InterfaceJogo interfaceJogo) {
		piloto.setNoAtual(saidaBox);
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

		boxEquipesOcupado.put(piloto.getCarro(), "");
		// System.out.println("Liberou " + piloto.toString());
		if (controleJogo.isCorridaTerminada()) {
			piloto.setRecebeuBanderada(true, controleJogo);
		}
		controleJogo.saiuBox(piloto);
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
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_DURO,
					controleCorrida.getDistaciaCorrida());
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
		if (controleJogo.isSemReabastacimento()) {
			setupParadaUnica(piloto);
		} else {
			if ((Math.random() > .5) && (posicao > 8)) {
				piloto.setSetUpIncial(UMA_PARADA);
				setupParadaUnica(piloto);

			} else {
				piloto.setSetUpIncial(UMA_OU_MAIS_PARADAS);
				setupDuasOuMaisParadas(piloto);

			}
		}
		if (controleJogo.isSemReabastacimento()) {
			piloto.getCarro().setCombustivel(controleCorrida.getTanqueCheio());
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
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_MOLE,
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
			percentagem = (consumoMedioCombustivel * (qtdeVoltRest / 2)) + 10;
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
		qtdeNosPistaRefBox = ateFim + ateSaidaBox;
	}

	public boolean verificaBoxOcupado(Carro carro) {
		if ((!"".equals(boxEquipesOcupado.get(carro)))) {
			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		int val = 1 + ((int) (Math.random() * 3));
		Logger.logar(val);
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
}
