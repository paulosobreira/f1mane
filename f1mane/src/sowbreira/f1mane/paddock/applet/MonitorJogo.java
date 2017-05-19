package sowbreira.f1mane.paddock.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import sowbreira.f1mane.controles.ControleCorrida;
import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.No;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.Comandos;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosParciais;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.Posis;
import sowbreira.f1mane.paddock.entidades.TOs.PosisPack;
import sowbreira.f1mane.paddock.entidades.TOs.SessaoCliente;
import sowbreira.f1mane.paddock.entidades.TOs.SrvJogoPack;
import sowbreira.f1mane.paddock.entidades.TOs.TravadaRoda;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

/**
 * @author Paulo Sobreira Criado em 05/08/2007 as 11:43:33
 */
public class MonitorJogo implements Runnable {
	private JogoCliente jogoCliente;
	private ControlePaddockCliente controlePaddockCliente;
	private String estado = null;
	private SessaoCliente sessaoCliente;
	private Thread atualizadorPainel;
	private Thread threadCmd;
	private boolean jogoAtivo = true;
	public long lastPosis = 0;
	public boolean procPosis = false;
	private boolean atualizouDados;
	private boolean setZoom;
	private boolean apagouLuz;
	protected boolean modoBox;

	public boolean isJogoAtivo() {
		return jogoAtivo;
	}

	public void setJogoAtivo(boolean jogoAtivo) {
		this.jogoAtivo = jogoAtivo;
	}

	public MonitorJogo(JogoCliente local,
			ControlePaddockCliente controlePaddockCliente,
			SessaoCliente sessaoCliente) {
		this.jogoCliente = local;
		this.controlePaddockCliente = controlePaddockCliente;
		this.sessaoCliente = sessaoCliente;
	}

	public void run() {
		boolean interrupt = false;
		while (!interrupt && controlePaddockCliente.isComunicacaoServer()
				&& jogoAtivo) {
			try {
				long tempoCiclo = 500;
				Logger.logar("MonitorJogo");
				Logger.logar("MonitorJogo verificaEstadoJogo()");
				verificaEstadoJogo();
				Logger.logar(
						"MonitorJogo jogoCliente.preparaGerenciadorVisual(true)");
				jogoCliente.preparaGerenciadorVisual(true);
				Logger.logar("MonitorJogo esperaJogoComecar()");
				esperaJogoComecar();
				Logger.logar("MonitorJogo mostraQualify()");
				mostraQualify();
				Logger.logar("MonitorJogo apagaLuzesLargada()");
				apagaLuzesLargada();
				Logger.logar("MonitorJogo processaCiclosCorrida(tempoCiclo)");
				processaCiclosCorrida(tempoCiclo);
				Logger.logar("MonitorJogo mostraResultadoFinal(tempoCiclo)");
				mostraResultadoFinal(tempoCiclo);
				Thread.sleep(controlePaddockCliente.getLatenciaMinima());
			} catch (Exception e) {
				interrupt = true;
				matarCmdThread();
				Logger.logarExept(e);
				if (!(e instanceof InterruptedException)) {
					JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
							e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
				}
			}
		}
		if (jogoCliente != null) {
			jogoCliente.matarThreadsResultadoFnal();
		}

	}

	private void apagaLuzesLargada() {
		boolean interupt = false;
		while (!interupt && Comandos.LUZES.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				iniciaJalena();
				atualizarDados();
				jogoCliente.desenhouQualificacao();
				atualizaZoom();
				if (!apagouLuz) {
					Logger.logar(
							"apagaLuzesLargada atualizarDadosParciais(jogoCliente.getDadosJogo(), 5000");
					atualizarDadosParciais(jogoCliente.getDadosJogo(),
							jogoCliente.getPilotoSelecionado());
					Thread.sleep(6500);
				} else {
					Logger.logar("apagaLuzesLargada 500");
					Thread.sleep(500);
				}
				verificaEstadoJogo();
				Logger.logar("apagaLuzesLargada verificaEstadoJogo");
				apagarLuz();
			} catch (InterruptedException e) {
				interupt = true;
				Logger.logarExept(e);
			}
		}
	}

	private void mostraResultadoFinal(long tempoCiclo)
			throws InterruptedException {
		boolean interrupt = false;
		while (!interrupt && Comandos.MOSTRA_RESULTADO_FINAL.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				List pilotos = jogoCliente.getPilotosCopia();
				for (Iterator iterator = pilotos.iterator(); iterator
						.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					atualizarDadosParciais(jogoCliente.getDadosJogo(), piloto);
					try {
						Thread.sleep(300);
					} catch (InterruptedException e) {
						interrupt = true;
						Logger.logarExept(e);
					}
				}
				atualizarDados();
				jogoCliente.exibirResultadoFinal();
				jogoAtivo = false;
				Thread.sleep(tempoCiclo);
			} catch (InterruptedException e) {
				interrupt = true;
				Logger.logarExept(e);
				throw e;
			}
		}
	}

	private void processaCiclosCorrida(long tempoCiclo)
			throws InterruptedException {
		boolean interrupt = false;
		tempoCiclo = 1000;
		boolean atualizaPosicoes = true;
		while (!interrupt && Comandos.CORRIDA_INICIADA.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				if (getLatenciaReal() > 1000) {
					jogoCliente.setAtualizacaoSuave(false);
				} else {
					jogoCliente.setAtualizacaoSuave(true);
				}
				if (controlePaddockCliente
						.getLatenciaReal() > Constantes.LATENCIA_MAX) {
					jogoCliente.autoDrs();
				}
				atualizarDados();
				iniciaJalena();
				atualizaZoom();
				if (atualizaPosicoes) {
					apagarLuz();
				}
				jogoCliente.desenhaQualificacao();
				jogoCliente.desenhouQualificacao();
				jogoCliente.selecionaPilotoJogador();
				disparaAtualizadorPainel(tempoCiclo);
				atualizarDadosParciais(jogoCliente.getDadosJogo(),
						jogoCliente.getPilotoSelecionado(), atualizaPosicoes);
				atualizaPosicoes = true;
				Thread.sleep(tempoCiclo);
			} catch (InterruptedException e) {
				interrupt = true;
				Logger.logarExept(e);
				throw e;
			}
		}
	}

	private void atualizaZoom() {
		if (!setZoom && jogoCliente.getFPS() >= 30) {
			jogoCliente.setMouseZoom(0.7);
			setZoom = true;
		}
	}

	private void disparaAtualizadorPainel(final long tempoCiclo) {
		if (atualizadorPainel != null) {
			return;
		}
		atualizadorPainel = new Thread(new Runnable() {
			public void run() {
				Logger.logar("MonitorJogo disparaAtualizadorPainel");
				boolean interrupt = false;
				while (!interrupt && jogoAtivo) {
					try {
						if (jogoCliente.getPilotoSelecionado() == null) {
							jogoCliente.selecionaPilotoJogador();
						}
						jogoCliente.atualizaPainel();
						Thread.sleep(tempoCiclo);
					} catch (Exception e) {
						interrupt = true;
						Logger.logarExept(e);
					}
				}

			}

		});
		atualizadorPainel.start();
	}

	private void mostraQualify() throws InterruptedException {
		boolean interrupt = false;
		boolean creditos = false;
		boolean atualizouDadosQualify = false;
		while (!interrupt && Comandos.MOSTRANDO_QUALIFY.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			int cont = 0;
			while (!atualizouDadosQualify && !atualizouDados && cont < 15) {
				atualizarDados();
				if (atualizouDados) {
					atualizouDadosQualify = atualizouDados;
				} else {
					cont++;
					Thread.sleep(100);
				}
			}
			iniciaJalena();
			if (!creditos) {
				Thread.sleep(1500);
				creditos = true;
			}
			jogoCliente.desenhaQualificacao();
			verificaEstadoJogo();
		}
	}

	private void esperaJogoComecar() throws InterruptedException {
		boolean interupt = false;
		while (!interupt && Comandos.ESPERANDO_JOGO_COMECAR.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			jogoCliente.carregaBackGroundCliente();
			verificaEstadoJogo();
			try {
				atualizouDados = false;
				Thread.sleep(500);
			} catch (InterruptedException e) {
				interupt = true;
				Logger.logarExept(e);
				throw e;
			}
		}
	}

	private void atualizaModoCarreira() {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.VER_CARREIRA, sessaoCliente);

			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret != null) {
				CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) ret;
				if (carreiraDadosSrv.isModoCarreira()) {
					jogoCliente.setNomePilotoJogador(
							carreiraDadosSrv.getNomePiloto());
				}
			}
			clientPaddockPack = new ClientPaddockPack(
					Comandos.DADOS_PILOTOS_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret != null) {
				clientPaddockPack = (ClientPaddockPack) ret;
				if (clientPaddockPack.getDadosJogoCriado()
						.getPilotosCarreira() != null) {
					Logger.logar(
							" Dentro dadosParticiparJogo.getPilotosCarreira()");
					List pilots = clientPaddockPack.getDadosJogoCriado()
							.getPilotosCarreira();
					List carros = new ArrayList();
					for (Iterator iterator = pilots.iterator(); iterator
							.hasNext();) {
						Piloto piloto = (Piloto) iterator.next();
						if (!carros.contains(piloto.getCarro())) {
							carros.add(piloto.getCarro());
						}
					}
					Logger.logar("Tamanho da lista Cliente " + carros.size());
					jogoCliente.geraBoxesEquipes(carros);
				}
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void atualizaPosisPack(PosisPack posisPack) {
		if (posisPack == null) {
			return;
		}
		if (posisPack.safetyNoId != 0) {
			jogoCliente.setSafetyCarBol(true);
			jogoCliente.atualizaPosSafetyCar(posisPack.safetyNoId,
					posisPack.safetySair);
		} else {
			jogoCliente.setSafetyCarBol(false);
		}
		if (posisPack.posis == null) {
			return;
		}
		atualizarListaPilotos(posisPack.posis);
	}

	private boolean retornoNaoValido(Object ret) {
		if (ret instanceof ErroServ || ret instanceof MsgSrv) {
			return true;
		}
		return false;
	}

	public void atualizarListaPilotos(Object[] posisArray) {
		List<Piloto> pilotos = jogoCliente.getPilotos();
		if (pilotos == null) {
			return;
		}
		for (int i = 0; i < posisArray.length; i++) {
			Posis posis = (Posis) posisArray[i];
			for (Iterator<Piloto> iter = pilotos.iterator(); iter.hasNext();) {
				Piloto piloto = iter.next();
				piloto.setFaiscas(false);
				if (piloto.getId() != posis.idPiloto) {
					continue;
				}
				String statusPilotos = posis.status;
				if (statusPilotos != null) {
					if (statusPilotos.startsWith("P")) {
						piloto.setPtosPista(
								new Long(statusPilotos.split("P")[1]));
					} else if (statusPilotos.startsWith("F")) {
						piloto.setPtosPista(
								new Long(statusPilotos.split("F")[1]));
						piloto.setFaiscas(true);
					} else if (statusPilotos.startsWith("T")) {
						piloto.setPtosPista(
								new Long(statusPilotos.split("T")[1]));
						jogoCliente.travouRodas(piloto);
						TravadaRoda travadaRoda = new TravadaRoda();
						travadaRoda.setIdNo(
								jogoCliente.obterIdPorNo(piloto.getNoAtual()));
						jogoCliente.travouRodas(travadaRoda);
					} else if ("R".equals(statusPilotos)) {
						piloto.getCarro().setRecolhido(true);
					}
				}
				piloto.setJogadorHumano(posis.humano);
				int pos = posis.tracado;
				double mod = Carro.ALTURA;

				if (piloto.getTracado() == 0 && (pos == 4 || pos == 5)) {
					mod *= 3;
				} else if ((piloto.getTracado() == 1
						|| piloto.getTracado() == 2)
						&& (pos == 4 || pos == 5)) {
					mod *= 2;
				} else if ((piloto.getTracado() == 5
						|| piloto.getTracado() == 4)
						&& (pos == 2 || pos == 1)) {
					mod *= 2;
				}
				if (piloto.getIndiceTracado() > 0
						&& pos != piloto.getTracado()) {
					piloto.decIndiceTracado();
				} else {
					if (piloto.getIndiceTracado() <= 0) {
						piloto.setTracadoAntigo(piloto.getTracado());
					}
					piloto.setTracado(pos);
					if (piloto.getIndiceTracado() <= 0 && piloto
							.getTracado() != piloto.getTracadoAntigo()) {
						piloto.setIndiceTracado((int) (mod * jogoCliente
								.getCircuito().getMultiplicadorLarguraPista()));
					}
				}
				jogoCliente.calculaSegundosParaLider(piloto);
				piloto.calculaCarrosAdjacentes(jogoCliente);
				Map<Integer, No> mapaIdsNos = jogoCliente.getMapaIdsNos();
				List nosDoBox = jogoCliente.getNosDoBox();
				if (posis.idNo >= -1) {
					No no = (No) mapaIdsNos.get(new Integer(posis.idNo));
					piloto.setNoAtual(no);
					if (nosDoBox.contains(no)) {
						piloto.setPtosBox(1);
					} else {
						piloto.setPtosBox(0);
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		// int valor = 2000;
		// System.out.println(valor > 1500 && valor <= 2000);

		for (int i = 0; i < 200; i += 5) {
			System.out.println(
					"if (diffINdex >=" + i + "&& diffINdex <" + (i + 5));
		}
		// int cont = 0;
		// for (int i = 0; i < 2000; i += 20) {
		// cont++;
		// }
		// System.out.println(cont);
	}

	private void apagarLuz() {
		jogoCliente.apagarLuz();
		apagouLuz = true;
	}

	public void atualizarDados() {
		if (atualizouDados) {
			return;
		}
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.OBTER_DADOS_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret != null) {
				DadosJogo dadosJogo = (DadosJogo) ret;
				jogoCliente.setDadosJogo(dadosJogo);
				if ((jogoCliente.getPilotos() == null
						|| jogoCliente.getPilotos().isEmpty())
						|| (jogoCliente.getPilotos() != null
								&& dadosJogo.getPilotosList() != null
								&& jogoCliente.getPilotos().size() != dadosJogo
										.getPilotosList().size())) {
					atualizouDados = false;
				} else {
					atualizouDados = true;
					atualizaModoCarreira();
					atualizarDadosParciais(dadosJogo, null);
					Logger.logar("atualizouDados = true");
				}
			} else {
				atualizouDados = false;
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			atualizouDados = false;
		}
	}

	private void iniciaJalena() {
		if (jogoAtivo) {
			jogoCliente.iniciaJanela();
		}
	}

	private void verificaEstadoJogo() {
		ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.VERIFICA_ESTADO_JOGO, sessaoCliente);
		clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
		Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret != null) {
			SrvJogoPack jogoPack = (SrvJogoPack) ret;
			estado = jogoPack.getEstadoJogo();
		}
	}

	public String getEstado() {
		return estado;
	}

	public void abandonar() {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.SAIR_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			controlePaddockCliente.enviarObjeto(clientPaddockPack);
			jogoCliente.matarTodasThreads();
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}

	}
	public void atualizarDadosParciais(DadosJogo dadosJogo,
			Piloto pilotoSelecionado) {
		atualizarDadosParciais(dadosJogo, pilotoSelecionado, false);
	}

	public void atualizarDadosParciais(DadosJogo dadosJogo,
			Piloto pilotoSelecionado, boolean atualizaPosicoes) {
		try {
			String dataSend = jogoCliente.getNomeJogoCriado() + "#"
					+ sessaoCliente.getNomeJogador();
			if (pilotoSelecionado != null) {
				dataSend += "#" + pilotoSelecionado.getId();
			}
			Object ret = controlePaddockCliente.enviarObjeto(dataSend, true);
			if (retornoNaoValido(ret)) {
				return;
			}
			if (ret == null) {
				Logger.logar("atualizarDadosParciais null");
				return;
			}
			// dec dadosParciais
			String enc = (String) ret;
			DadosParciais dadosParciais = new DadosParciais();
			dadosParciais.decode(enc);
			estado = dadosParciais.estado;
			jogoCliente.verificaMudancaClima(dadosParciais.clima);
			dadosJogo.setClima(dadosParciais.clima);
			dadosJogo
					.setMelhoVolta(new Volta(dadosParciais.melhorVoltaCorrida));
			if (dadosParciais.texto != null
					&& !"".equals(dadosParciais.texto)) {
				dadosJogo.setTexto(dadosParciais.texto);
			}
			dadosJogo.setVoltaAtual(dadosParciais.voltaAtual);
			List<Piloto> pilotos = jogoCliente.getPilotos();
			if (pilotoSelecionado != null) {
				Piloto piloto = pilotoSelecionado;
				piloto.setNumeroVolta((int) Math.floor(piloto.getPtosPista()
						/ jogoCliente.getNosDaPista().size()));
				piloto.setMelhorVolta(new Volta(dadosParciais.melhorVolta));
				piloto.getVoltas().clear();
				piloto.getVoltas().add(new Volta(dadosParciais.ultima5));
				piloto.getVoltas().add(new Volta(dadosParciais.ultima4));
				piloto.getVoltas().add(new Volta(dadosParciais.ultima3));
				piloto.getVoltas().add(new Volta(dadosParciais.ultima2));
				piloto.getVoltas().add(new Volta(dadosParciais.ultima1));
				piloto.processaUltimas5Voltas();
				piloto.setNomeJogador(dadosParciais.nomeJogador);
				piloto.setQtdeParadasBox(dadosParciais.paradas);
				if (piloto.getNomeJogador() != null) {
					piloto.setJogadorHumano(true);
				} else {
					piloto.setJogadorHumano(false);
				}
				piloto.getCarro().setDanificado(dadosParciais.dano);
				if (!jogoCliente.isSafetyCarNaPista()
						&& piloto.isDesqualificado()) {
					piloto.getCarro().setRecolhido(true);
				}
				piloto.setBox(dadosParciais.box);
				piloto.setStress(dadosParciais.stress);
				piloto.setPodeUsarDRS(dadosParciais.podeUsarDRS);
				piloto.setRecebeuBanderada(dadosParciais.recebeuBanderada);
				piloto.getCarro().setCargaErs(dadosParciais.cargaKers);
				piloto.setAlertaMotor(dadosParciais.alertaMotor);
				piloto.setAlertaAerefolio(dadosParciais.alertaAerefolio);
				if (piloto.getCargaKersOnline() != dadosParciais.cargaKers) {
					piloto.setAtivarErs(true);
					piloto.setCargaKersOnline(dadosParciais.cargaKers);
				} else {
					piloto.setAtivarErs(false);
				}
				piloto.getCarro()
						.setPorcentagemDesgasteMotor(dadosParciais.pMotor);
				piloto.getCarro()
						.setPorcentagemDesgastePneus(dadosParciais.pPneus);
				piloto.getCarro()
						.setPorcentagemCombustivel(dadosParciais.pCombust);
				piloto.getCarro().setAsa(dadosParciais.asaBox);
				piloto.getCarro().setTipoPneu(dadosParciais.tpPneus);
				if (piloto.getCarroPilotoFrente() != null) {
					piloto.getCarroPilotoFrente()
							.setTipoPneu(dadosParciais.tpPneusFrente);
				}
				if (piloto.getCarroPilotoAtras() != null) {
					piloto.getCarroPilotoAtras()
							.setTipoPneu(dadosParciais.tpPneusAtras);
				}
				piloto.setVelocidade(dadosParciais.velocidade);
				piloto.setVelocidadeExibir(dadosParciais.velocidade);
				piloto.setVelocidade(dadosParciais.velocidade);
				piloto.setQtdeCombustBox(dadosParciais.combustBox);
				piloto.setTipoPneuBox(dadosParciais.tpPneusBox);
				piloto.setModoPilotagem(dadosParciais.modoPilotar);
				piloto.setAsaBox(dadosParciais.asaBox);
				piloto.getCarro().setAsa(dadosParciais.asa);
				piloto.getCarro().setGiro(dadosParciais.giro);
				piloto.setVantagem(dadosParciais.vantagem);
			}
			if (atualizaPosicoes) {
				atualizaPosisPack(dadosParciais.posisPack);
			}
			Collections.sort(pilotos, new Comparator<Piloto>() {
				@Override
				public int compare(Piloto piloto0, Piloto piloto1) {
					return ControleCorrida.compare(piloto0, piloto1);
				}
			});
			for (int i = 0; i < pilotos.size(); i++) {
				Piloto piloto = (Piloto) pilotos.get(i);
				piloto.setPosicao(i + 1);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void mudarGiroMotor(final Object selectedItem) {
		Logger.logar("mudarGiroMotor " + selectedItem);
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					String giro = (String) selectedItem;
					if (!Carro.GIRO_MAX.equals(giro)
							&& !Carro.GIRO_MIN.equals(giro)
							&& !Carro.GIRO_NOR.equals(giro)) {
						return;
					}
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_GIRO_MOTOR, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					clientPaddockPack.setGiroMotor(giro);
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoBox() {
		Logger.logar("alterarOpcoesBox ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_BOX, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					int porcentCombust = 50;
					String tpPneu = Carro.TIPO_PNEU_DURO;
					String tpAsa = Carro.ASA_NORMAL;
					clientPaddockPack.setTpPneuBox(tpPneu);
					clientPaddockPack.setCombustBox(porcentCombust);
					clientPaddockPack.setAsaBox(tpAsa);
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
					modoBox = ret != null;
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void alterarOpcoesBox(Object tpPneu, Object combust, Object asa) {
		Logger.logar("alterarOpcoesBox ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		final ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
				Comandos.ALTERAR_OPCOES_BOX, sessaoCliente);
		clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
		clientPaddockPack.setTpPneuBox((String) tpPneu);
		clientPaddockPack.setCombustBox((Integer) combust);
		clientPaddockPack.setAsaBox((String) asa);
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();
	}

	public void mudarModoPilotagem(final String modo) {
		Logger.logar("mudarModoPilotagem " + modo);
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_PILOTAGEM, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					clientPaddockPack.setModoPilotagem(modo);
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarAutoPos() {
		Logger.logar("mudarAutoPos ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_AUTOPOS, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarTracado(final int tracado) {
		Logger.logar("mudarTracado(final int tracado) = " + tracado);
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_TRACADO, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());

					clientPaddockPack.setTracado(tracado);
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}

		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoDRS(final boolean modo) {
		Logger.logar("mudarModoDRS " + modo);
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					if (jogoCliente.isChovendo()) {
						jogoCliente.info(Lang.msg("drsDesabilitado"));
					}
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_DRS, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					clientPaddockPack.setDataObject(new Boolean(modo));
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoKers(final boolean modo) {
		Logger.logar("mudarModoDRS " + modo);
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_KERS, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					clientPaddockPack.setDataObject(modo);
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void driveThru(final Piloto pilotoSelecionado) {
		if (pilotoSelecionado == null || !pilotoSelecionado.isJogadorHumano()
				|| sessaoCliente.getNomeJogador()
						.equals(pilotoSelecionado.getNomeJogador())) {
			jogoCliente
					.adicionarInfoDireto(Lang.msg("selecionePilotoDriveThru"));
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.DRIVE_THRU, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					clientPaddockPack
							.setDataObject(pilotoSelecionado.getNomeJogador());
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		Thread thread = new Thread(runnable);
		thread.start();
	}

	public void fechaJanela() {
		if (jogoCliente != null && jogoCliente.getMainFrame() != null)
			jogoCliente.getMainFrame().setVisible(false);

	}

	public void matarCmdThread() {
		if (threadCmd != null) {
			threadCmd.interrupt();
		}
	}

	public void matarTodasThreads() {
		if (atualizadorPainel != null) {
			atualizadorPainel.interrupt();
		}
		if (threadCmd != null) {
			threadCmd.interrupt();
		}
	}

	public int getLatenciaReal() {
		if (controlePaddockCliente == null) {
			return 0;
		}
		return controlePaddockCliente.getLatenciaReal();
	}

	public void pilotoSelecionadoMinimo() {
		Logger.logar("pilotoSelecionadoMinimo ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_PILOTO_MINIMO, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void pilotoSelecionadoNormal() {
		Logger.logar("pilotoSelecionadoNormal ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_PILOTO_NORMAL, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();
	}

	public void pilotoSelecionadoMaximo() {
		Logger.logar("pilotoSelecionadoMaximo ");
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_PILOTO_MAXIMO, sessaoCliente);
					clientPaddockPack
							.setNomeJogo(jogoCliente.getNomeJogoCriado());
					Object ret = controlePaddockCliente
							.enviarObjeto(clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();
	}

	public boolean getModoBox() {
		return modoBox;
	}
}
