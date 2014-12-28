package sowbreira.f1mane.controles;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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
import br.nnpe.Constantes;
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
	private long qtdeNosPistaRefBox;
	private InterfaceJogo controleJogo;
	private ControleCorrida controleCorrida;
	private Map boxEquipes;
	private Hashtable boxEquipesOcupado;
	private Circuito circuito;
	private List carrosBox;
	private boolean boxRapido = false;
	private int ultIndiceParada = 0;

	public boolean isBoxRapido() {
		return boxRapido;
	}

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
		entradaBox = (No) controleJogo.getCircuito().getPistaFull()
				.get(controleJogo.getCircuito().getEntradaBoxIndex());
		paradaBox = (No) controleJogo.getCircuito().getBoxFull()
				.get(controleJogo.getCircuito().getParadaBoxIndex());
		saidaBox = (No) controleJogo.getCircuito().getPistaFull()
				.get(controleJogo.getCircuito().getSaidaBoxIndex());
		circuito = controleJogo.getCircuito();
		calculaQtdeNosPistaRefBox();
		if (saidaBox == null) {
			throw new Exception("Saida box não encontrada!");
		}
		if (Math.random() < .5) {
			boxRapido = true;
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
			if (indexParada > ultIndiceParada) {
				ultIndiceParada = indexParada;
			}
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
				.getEntradaBoxIndex() + 50)) && (piloto.getPtosBox() <= 0)) {
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
				if (controleJogo.verificaInfoRelevante(piloto))
					controleJogo.info(Html.orange(Lang.msg("entraBox",
							new String[] { piloto.getNome() })));
				Logger.logar(piloto.getNome() + " Entrou no Box na Volta : "
						+ piloto.getNumeroVolta() + " Pneus : "
						+ piloto.getCarro().porcentagemDesgastePeneus()
						+ " Combustivel : "
						+ piloto.getCarro().porcentagemCombustivel()
						+ " Motor : "
						+ piloto.getCarro().porcentagemDesgasteMotor());
				piloto.setPtosBox(Util.inte((piloto.getPtosBox() + 1)
						* circuito.getMultiplciador()));
			} else {
				box = piloto.getNoAtual();
				No nobox = (No) boxEquipes.get(piloto.getCarro());
				int indexParada = piloto.obterPista(controleJogo)
						.indexOf(nobox);
				int ptosBox = 0;
				int indexParadaMenos10 = indexParada - 10;
				int indexParadaMais10 = indexParada + 10;
				if (box.isBox()
						&& ((piloto.getPtosBox() < indexParadaMenos10 || piloto
								.getPtosBox() > indexParadaMais10))) {
					/**
					 * gera limite velocidade no box
					 */
					ptosBox += ((boxRapido) ? 2 : 1);
				} else if (box.verificaRetaOuLargada()) {
					ptosBox += ((boxRapido) ? 3 : Util.intervalo(2, 3));
				} else if (box.verificaCruvaAlta()) {
					ptosBox += ((boxRapido) ? 2 : Util.intervalo(1, 2));
				} else {
					ptosBox += 1;
				}

				double iPilot = piloto.getNoAtual().getIndex();
				double tamPista = controleJogo.getNosDoBox().size();
				boolean mais90Porcent = (iPilot / tamPista) > 0.9;
				if (controleJogo.isSafetyCarNaPista()) {
					mais90Porcent = false;
				}

				if (!mais90Porcent
						&& controleJogo.isSafetyCarNaPista()
						&& piloto.getNoAtual().getIndex() > (ultIndiceParada + Carro.MEIA_LARGURA)
						&& verificaTemCarroPassandoSaida()) {
					ptosBox = 0;
				}

				ptosBox *= circuito.getMultiplciador();
				int novosPtsBox = Util.inte(ptosBox) + piloto.getPtosBox();
				piloto.setPtosBox(novosPtsBox);
				piloto.setVelocidade(Util.intervalo(50, 60) + ptosBox);
				if (novosPtsBox >= (indexParada - (3 * Carro.LARGURA))
						&& novosPtsBox <= (indexParada)) {
					piloto.mudarTracado((controleJogo.getCircuito()
							.getLadoBox() == 1 ? 2 : 1), controleJogo);
				} else if (!mais90Porcent) {
					piloto.mudarTracado(0, controleJogo);
				}
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
		if ((contBox > (box.getIndex() - 16) && contBox < (box.getIndex() + 16))
				&& !piloto.decrementaParadoBox() && piloto.isBox()) {
			processarPilotoPararBox(piloto);
		}
	}

	private boolean verificaTemCarroPassandoSaida() {
		List pilotos = controleJogo.getPilotosCopia();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (piloto.getPtosBox() > 0 || piloto.isDesqualificado()
					|| piloto.getCarro().verificaDano()) {
				continue;
			}
			No noAtual = piloto.getNoAtual();
			int iniAnalise = saidaBox.getIndex() - 200;
			if (controleCorrida.isSafetyCarNaPista()) {
				iniAnalise = 0;
			}
			if (saidaBox.getIndex() + 50 > noAtual.getIndex()
					&& noAtual.getIndex() > iniAnalise) {
				return true;
			}
		}
		return false;
	}

	public No getEntradaBox() {
		return entradaBox;
	}

	private void processarPilotoPararBox(Piloto piloto) {
		if (piloto.isDriveThrough()) {
			return;
		}
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
			qtdeCombust = controleJogo.setUpJogadorHumano(piloto,
					controleJogo.getTipoPeneuBox(piloto), combust,
					controleJogo.getAsaBox(piloto));
		}

		int porcentCombust = (100 * qtdeCombust)
				/ controleCorrida.getTanqueCheio();
		long penalidade = 0;
		Carro carro = (Carro) boxEquipesOcupado.get(piloto.getCarro());
		if (carro != null && !carro.getPiloto().equals(piloto)) {
			if (controleJogo.verificaInfoRelevante(piloto)) {
				controleJogo.info(Html.orange(Lang.msg("298",
						new String[] { carro.getNome() })));
			}
			penalidade = 25;
			penalidade = Util.inte(penalidade
					* (2 - (carro.getPotencia() / 1000)));
		}
		carro = piloto.getCarro();

		carro.setMotor(carro.getMotor()
				+ (Util.inte(carro.getDurabilidadeMaxMotor()
						* Util.intervalo(1, 15) / 100.0)));
		if (carro.getMotor() > carro.getDurabilidadeMaxMotor()) {
			carro.setMotor(carro.getDurabilidadeMaxMotor());
		}

		carro.setPotencia(carro.getPotencia() + Util.intervalo(-5, +5));

		if ((piloto.getPosicao() / controleJogo.getPilotos().size() < Math
				.random()) && !controleJogo.verificaNivelJogo()) {
			if (piloto.getHabilidadeAntesQualify() > piloto.getHabilidade()) {
				piloto.setHabilidade(piloto.getHabilidadeAntesQualify());
			}
			if (piloto.getCarro().getPotenciaAntesQualify() > piloto.getCarro()
					.getPotencia()) {
				piloto.getCarro().setPotencia(
						piloto.getCarro().getPotenciaAntesQualify());
			}
		}

		if (carro.getDurabilidadeAereofolio() <= 0) {
			penalidade = Util.inte(penalidade
					* (2 - (carro.getPotencia() / 1000)));
		}

		double paradoBox = ((((porcentCombust + penalidade) * 100) / controleCorrida
				.obterTempoCilco()) + 30);
		piloto.setParadoBox((int) paradoBox);
		piloto.setPorcentagemCombustUltimaParadaBox(porcentCombust);

		piloto.setParouNoBoxMilis(System.currentTimeMillis());
		piloto.setSaiuDoBoxMilis(0);
		if (controleJogo.verificaInfoRelevante(piloto)) {
			if (piloto.isJogadorHumano()) {
				controleJogo.infoPrioritaria(Html.orange(Lang
						.msg("002",
								new String[] {
										piloto.getNome(),
										String.valueOf(controleJogo
												.getNumVoltaAtual()) })));
			} else if (piloto.getPosicao() < 9) {
				controleJogo
						.info(Html.orange(Lang.msg(
								"002",
								new String[] {
										piloto.getNome(),
										String.valueOf(controleJogo
												.getNumVoltaAtual()) })));
			}
		}
		carro.setDanificado(null);
		if (carro.getDurabilidadeAereofolio() <= 0
				|| InterfaceJogo.DIFICIL_NV != controleJogo.getNiveljogo()) {
			carro.setDurabilidadeAereofolio(controleJogo
					.getDurabilidadeAreofolio());
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
		if (piloto.isDriveThrough()) {
			piloto.limparDriveThrough();
			piloto.setBox(false);
			controleJogo.infoPrioritaria(Html.driveThru(Lang.msg(
					"cumpriuDriveThru", new String[] { piloto.getNome() })));
		}
		piloto.setNoAtual(saidaBox);
		piloto.setPtosPista(piloto.getPtosPista() + qtdeNosPistaRefBox);
		piloto.setNumeroVolta(piloto.getNumeroVolta() + 1);
		/**
		 * calback de nova volta para corrida Toda
		 */
		if (piloto.getPosicao() == 1) {
			controleJogo.processaNovaVolta();
		}

		long diff = piloto.getSaiuDoBoxMilis() - piloto.getParouNoBoxMilis();
		String[] strings = new String[] { piloto.getNome(),
				ControleEstatisticas.formatarTempo(diff),
				String.valueOf(piloto.getPorcentagemCombustUltimaParadaBox()),
				Lang.msg(piloto.getCarro().getTipoPneu()) };
		String info = Lang.msg("003", strings);
		if (controleJogo.verificaInfoRelevante(piloto)) {
			if (piloto.isJogadorHumano()) {
				controleJogo.infoPrioritaria(Html.orange(info));
			} else if (piloto.getPosicao() < 9) {
				controleJogo.info(Html.orange(info));
			}
		}

		boxEquipesOcupado.remove(piloto.getCarro());
		if (controleJogo.isCorridaTerminada()) {
			piloto.setRecebeuBanderada(true, controleJogo);
		}
		if (controleJogo.isSafetyCarNaPista() && piloto.getVoltaAtual() != null) {
			piloto.getVoltaAtual().setVoltaSafetyCar(true);
		}
		piloto.efetuarSaidaBox(interfaceJogo);
		if (interfaceJogo.isCorridaTerminada()) {
			piloto.setRecebeuBanderada(true, interfaceJogo);
		}
	}

	public int setupParadaUnica(Piloto piloto) {
		if (controleJogo.isChovendo()) {
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_CHUVA,
					controleCorrida.getDistaciaCorrida());
			piloto.setSetUpIncial(UMA_OU_MAIS_PARADAS);
		} else {
			int voltaAtual = piloto.getNumeroVolta();
			int metade = controleJogo.getQtdeTotalVoltas() / 2;
			boolean asfaltoAbrasivo = testaAsfaltoAbrasivoIA(piloto);
			if (voltaAtual > metade && !asfaltoAbrasivo) {
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_MOLE,
						controleCorrida.getDistaciaCorrida());
			} else {
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_DURO,
						controleCorrida.getDistaciaCorrida());
			}

		}
		if (piloto.testeHabilidadePiloto(controleJogo))
			processarTipoAsaAutomatico(piloto);

		int percentagem = 0;

		int consumoMedio = (int) piloto.calculaConsumoMedioCombust();
		piloto.limparConsumoMedioCombust();
		if (consumoMedio == 0) {
			if (piloto.getQtdeParadasBox() == 0) {
				percentagem = 50 + ((int) (Math.random() * 60));
			} else if (piloto.getQtdeParadasBox() == 1) {
				percentagem = 40 + ((int) (Math.random() * 50));
			} else {
				percentagem = 30 + ((int) (Math.random() * 40));
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

	public void setupCorridaQualificacaoAleatoria(Piloto piloto) {
		if (piloto.isJogadorHumano()) {
			Integer combustBox = controleJogo.getCombustBox(piloto);
			if (combustBox.intValue() <= 10) {
				combustBox = new Integer(10);
			}
			controleJogo.setUpJogadorHumano(piloto,
					controleJogo.getTipoPeneuBox(piloto), combustBox,
					controleJogo.getAsaBox(piloto));
			return;
		}
		if (piloto.testeHabilidadePilotoCarro(controleJogo)
				&& controleJogo.asfaltoAbrasivo()
				&& !controleJogo.isBoxRapido()) {
			piloto.setSetUpIncial(UMA_PARADA);
			setupParadaUnica(piloto);
		} else {
			piloto.setSetUpIncial(UMA_OU_MAIS_PARADAS);
			setupDuasOuMaisParadas(piloto);
		}
		if (controleJogo.isSemReabastacimento()) {
			double mod = 0.9;
			if (piloto.testeHabilidadePiloto(controleJogo)) {
				mod = 0.85;
			}
			if (piloto.testeHabilidadePilotoCarro(controleJogo)) {
				mod = 0.75;
			}
			piloto.getCarro().setCombustivel(
					(int) (controleCorrida.getTanqueCheio() * mod));
		}
		processarTipoAsaAutomatico(piloto);
	}

	private void processarTipoAsaAutomatico(Piloto piloto) {
		piloto.getCarro().setAsa(Carro.ASA_NORMAL);
		if (piloto.testeHabilidadePiloto(controleJogo)) {
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

			if (alta >= 60) {
				piloto.getCarro().setAsa(Carro.MENOS_ASA);
			}
			if (baixa >= 15) {
				piloto.getCarro().setAsa(Carro.MAIS_ASA);
			}
			if (media >= 25) {
				piloto.getCarro().setAsa(Carro.MAIS_ASA);
			}
		}
		if (controleJogo.isChovendo()) {
			piloto.getCarro().setAsa(Carro.MAIS_ASA);
		}

	}

	public int setupDuasOuMaisParadas(Piloto piloto) {
		if (controleJogo.isChovendo()) {
			piloto.getCarro().trocarPneus(controleJogo, Carro.TIPO_PNEU_CHUVA,
					controleCorrida.getDistaciaCorrida());
		} else {
			boolean asfaltoAbrasivo = testaAsfaltoAbrasivoIA(piloto);
			if (asfaltoAbrasivo)
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_DURO,
						controleCorrida.getDistaciaCorrida());
			else
				piloto.getCarro().trocarPneus(controleJogo,
						Carro.TIPO_PNEU_MOLE,
						controleCorrida.getDistaciaCorrida());
		}
		if (!(InterfaceJogo.FACIL_NV == controleJogo.getNiveljogo())
				|| piloto.testeHabilidadePiloto(controleJogo))
			processarTipoAsaAutomatico(piloto);

		int percentagem = 0;

		int consumoMedioCombustivel = (int) piloto.calculaConsumoMedioCombust();

		if (!controleJogo.isSemReabastacimento())
			piloto.limparConsumoMedioCombust();
		if (!controleJogo.isSemTrocaPneu())
			piloto.limparConsumoMedioPneus();

		if (consumoMedioCombustivel == 0) {
			if (controleJogo.getNumVoltaAtual() == 0) {
				percentagem = Util.intervalo(40, 50);
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

	private boolean testaAsfaltoAbrasivoIA(Piloto piloto) {
		boolean asfaltoAbrasivo = controleJogo.asfaltoAbrasivo();
		int qtdeteste = piloto.getHabilidade() / 10;
		if (!asfaltoAbrasivo) {
			for (int i = 0; i < qtdeteste; i++) {
				asfaltoAbrasivo = controleJogo.asfaltoAbrasivo();
				if (asfaltoAbrasivo) {
					break;
				}
			}
		}
		return asfaltoAbrasivo;
	}

	public void calculaQtdeNosPistaRefBox() {
		List ptosPista = controleJogo.getNosDaPista();
		int ateFim = ptosPista.size() - ptosPista.indexOf(entradaBox);
		int ateSaidaBox = ptosPista.indexOf(saidaBox);
		qtdeNosPistaRefBox = Util.inte((ateFim + ateSaidaBox));
		Logger.logar("qtdeNosPistaRefBox " + qtdeNosPistaRefBox);
		Logger.logar("getNosDaPista() " + controleJogo.getNosDaPista().size());
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

	public long calculaQtdePtsPistaPoleParaSaidaBox(Piloto pole) {
		List ptsPista = controleJogo.getNosDaPista();
		long diferenca = 0;
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
