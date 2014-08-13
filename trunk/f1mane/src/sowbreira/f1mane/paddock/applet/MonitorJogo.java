package sowbreira.f1mane.paddock.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.Piloto;
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
import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;

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
				long tempoCiclo = jogoCliente.getTempoCiclo();
				if (tempoCiclo < controlePaddockCliente.getLatenciaMinima()) {
					tempoCiclo = controlePaddockCliente.getLatenciaMinima();
				}
				Logger.logar("MonitorJogo");
				Logger.logar("MonitorJogo verificaEstadoJogo()");
				verificaEstadoJogo();
				Logger.logar("MonitorJogo jogoCliente.preparaGerenciadorVisual(true)");
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
				matarTodasThreads();
				Logger.logarExept(e);
			}
		}
		if (jogoCliente != null) {
			jogoCliente.matarTodasThreads();
		}

	}

	private void apagaLuzesLargada() {
		boolean interupt = false;
		while (!interupt && Comandos.LUZES.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				iniciaJalena();
				if (!atualizouDados) {
					atualizarDados();
				}
				jogoCliente.desenhouQualificacao();
				atualizaZoom();
				Thread.sleep(1000);
				apagarLuz();
				verificaEstadoJogo();
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

				List pilotos = jogoCliente.getPilotos();
				for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
					Piloto piloto = (Piloto) iterator.next();
					// jogoCliente.adicionarInfoDireto(piloto.getPosicao() + " "
					// + piloto.getNome() + " " + piloto.getCarro().getNome());
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
		int delayVerificaStado = 20;
		boolean interrupt = false;
		while (!interrupt && Comandos.CORRIDA_INICIADA.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				if (!atualizouDados) {
					atualizarDados();
					atualizaModoCarreira();
					atualizaPosicoes();
				}
				iniciaJalena();
				atualizaZoom();
				apagarLuz();
				jogoCliente.desenhaQualificacao();
				jogoCliente.desenhouQualificacao();
				jogoCliente.selecionaPilotoJogador();
				disparaAtualizadorPainel(tempoCiclo);
				delayVerificaStado--;
				if (delayVerificaStado <= 0) {
					atualizarDadosParciais(jogoCliente.getDadosJogo(),
							jogoCliente.getPilotoSelecionado());
					if (controlePaddockCliente.getLatenciaReal() > Constantes.LATENCIA_MAX) {
						jogoCliente.autoDrs();
					}
					if (controlePaddockCliente.getLatenciaReal() > 2000) {
						delayVerificaStado = 5;
					} else {
						delayVerificaStado = 7;
					}
					continue;
				}
				atualizaPosicoes();
				Thread.sleep(tempoCiclo);
				verificaEstadoJogo();
			} catch (InterruptedException e) {
				interrupt = true;
				Logger.logarExept(e);
				throw e;
			}
		}
	}

	private void atualizaZoom() {
		if (!setZoom) {
			jogoCliente.setMouseZoom(0.7);
			setZoom = true;
		}
	}

	private void disparaAtualizadorPainel(final long tempoCiclo) {
		if (atualizadorPainel == null) {
			atualizadorPainel = new Thread(new Runnable() {
				public void run() {
					Logger.logar("MonitorJogo disparaAtualizadorPainel(tempoCiclo);");
					boolean interrupt = false;
					while (!interrupt && jogoAtivo) {
						try {
							if (jogoCliente.getPilotoSelecionado() == null) {
								jogoCliente.selecionaPilotoJogador();
							}
							jogoCliente.decrementaTracado();
							jogoCliente.verificaProgramacaoBox();
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
	}

	private void mostraQualify() throws InterruptedException {
		boolean interrupt = false;
		boolean creditos = false;
		boolean atualizouDadosQualify = false;
		while (!interrupt && Comandos.MOSTRANDO_QUALIFY.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			int cont = 0;
			while (!atualizouDadosQualify && !atualizouDados && cont < 5) {
				atualizarDados();
				if (atualizouDados) {
					atualizouDadosQualify = atualizouDados;
				} else {
					cont++;
					Thread.sleep(1000);
				}
			}
			iniciaJalena();
			if (!creditos) {
				Thread.sleep(3000);
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
					jogoCliente.setNomePilotoJogador(carreiraDadosSrv
							.getNomePiloto());
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
				if (clientPaddockPack.getDadosJogoCriado().getPilotosCarreira() != null) {
					Logger.logar(" Dentro dadosParticiparJogo.getPilotosCarreira()");
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

	private void atualizaPosicoes() {
		Object ret = controlePaddockCliente.enviarObjeto(
				jogoCliente.getNomeJogoCriado(), true);
		if (retornoNaoValido(ret)) {
			return;
		}
		if (ret != null) {
			String enc = (String) ret;
			PosisPack posisPack = new PosisPack();
			posisPack.decode(enc);
			if (posisPack.safetyNoId != 0) {
				jogoCliente.setSafetyCarBol(true);
				jogoCliente.atualizaPosSafetyCar(posisPack.safetyNoId,
						posisPack.safetySair);
			} else {
				jogoCliente.setSafetyCarBol(false);
			}
			atualizarListaPilotos(posisPack.posis);
		}
	}

	private boolean retornoNaoValido(Object ret) {
		if (ret instanceof ErroServ || ret instanceof MsgSrv) {
			return true;
		}
		return false;
	}

	public void atualizarListaPilotos(Object[] posisArray) {
		if (posisArray != null) {
			for (int i = 0; i < posisArray.length; i++) {
				Posis posis = (Posis) posisArray[i];
				jogoCliente.atualizaPosicaoPiloto(posis);
			}
		}
	}

	public static void main(String[] args) {
		// int valor = 2000;
		// System.out.println(valor > 1500 && valor <= 2000);

		for (int i = 0; i < 200; i += 5) {
			System.out.println("if (diffINdex >=" + i + "&& diffINdex <"
					+ (i + 5));
		}
		// int cont = 0;
		// for (int i = 0; i < 2000; i += 20) {
		// cont++;
		// }
		// System.out.println(cont);
	}

	private void apagarLuz() {
		jogoCliente.apagarLuz();
	}

	public void atualizarDados() {
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
				if ((jogoCliente.getPilotos() == null || jogoCliente
						.getPilotos().isEmpty())
						|| (jogoCliente.getPilotos() != null
								&& dadosJogo.getPilotosList() != null && jogoCliente
								.getPilotos().size() != dadosJogo
								.getPilotosList().size())) {
					atualizouDados = false;
				} else {
					atualizouDados = true;
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
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			jogoCliente.matarTodasThreads();
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}

	}

	public void atualizarDadosParciais(DadosJogo dadosJogo,
			Piloto pilotoSelecionado) {
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
			if (ret != null) {
				// dec dadosParciais
				String enc = (String) ret;
				DadosParciais dadosParciais = new DadosParciais();
				dadosParciais.decode(enc);
				estado = dadosParciais.estado;
				jogoCliente.verificaMudancaClima(dadosParciais.clima);
				dadosJogo.setClima(dadosParciais.clima);
				dadosJogo.setMelhoVolta(dadosParciais.melhorVolta);
				if (dadosParciais.texto != null
						&& !"".equals(dadosParciais.texto))
					dadosJogo.setTexto(dadosParciais.texto);
				dadosJogo.setVoltaAtual(dadosParciais.voltaAtual);
				List pilotos = jogoCliente.getPilotos();
				for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
					Piloto piloto = (Piloto) iter.next();
					piloto.setPtosPista(dadosParciais.pilotsPonts[piloto
							.getId() - 1]);
					piloto.setNumeroVolta((int) Math.floor(piloto
							.getPtosPista()
							/ jogoCliente.getNosDaPista().size()));
					long valTsFinal = dadosParciais.pilotsTs[piloto.getId() - 1];
					if (valTsFinal == -1) {
						piloto.getCarro().setRecolhido(true);
					} else if (valTsFinal == -2) {
						if (!piloto.decContTravouRodas()) {
							piloto.setContTravouRodas(Util.intervalo(10, 60));
							TravadaRoda travadaRoda = new TravadaRoda();
							travadaRoda.setIdNo(this.jogoCliente
									.obterIdPorNo(piloto.getNoAtual()));
							travadaRoda.setTracado(piloto.getTracado());
							jogoCliente.travouRodas(travadaRoda);
						}

					} else {
						piloto.setTimeStampChegeda(valTsFinal);
					}
					if (pilotoSelecionado != null
							&& pilotoSelecionado.equals(piloto)) {
						piloto.setMelhorVolta(dadosParciais.peselMelhorVolta);
						piloto.getVoltas().clear();
						piloto.getVoltas().add(dadosParciais.peselUltima5);
						piloto.getVoltas().add(dadosParciais.peselUltima4);
						piloto.getVoltas().add(dadosParciais.peselUltima3);
						piloto.getVoltas().add(dadosParciais.peselUltima2);
						piloto.getVoltas().add(dadosParciais.peselUltima1);
						piloto.setNomeJogador(dadosParciais.nomeJogador);
						piloto.setQtdeParadasBox(dadosParciais.pselParadas);
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
						piloto.setBox(dadosParciais.pselBox);
						piloto.setStress(dadosParciais.pselStress);
						piloto.getCarro().setCargaKers(dadosParciais.cargaKers);
						piloto.getCarro().setTemperaturaMotor(
								dadosParciais.temperaturaMotor);
						if (piloto.getCargaKersOnline() != dadosParciais.cargaKers) {
							piloto.setAtivarKers(true);
							piloto.setCargaKersOnline(dadosParciais.cargaKers);
						} else {
							piloto.setAtivarKers(false);
						}
						piloto.getCarro().setMotor(dadosParciais.pselMotor);
						piloto.getCarro().setPneus(dadosParciais.pselPneus);
						piloto.getCarro().setDurabilidadeMaxPneus(
								dadosParciais.pselMaxPneus);
						piloto.getCarro().setDurabilidadeAereofolio(
								dadosParciais.pselDurAereofolio);
						piloto.getCarro().setCombustivel(
								dadosParciais.pselCombust);
						piloto.getCarro().setAsa(dadosParciais.pselAsaBox);
						piloto.getCarro()
								.setTipoPneu(dadosParciais.pselTpPneus);
						piloto.setVelocidade(dadosParciais.pselVelocidade);
						piloto.setQtdeCombustBox(dadosParciais.pselCombustBox);
						piloto.setTipoPneuBox(dadosParciais.pselTpPneusBox);
						piloto.setModoPilotagem(dadosParciais.pselModoPilotar);
						piloto.setAsaBox(dadosParciais.pselAsaBox);
						piloto.getCarro().setAsa(dadosParciais.pselAsa);
						piloto.getCarro().setGiro(dadosParciais.pselGiro);
					}
				}
				Collections.sort(pilotos, new Comparator() {
					public int compare(Object arg0, Object arg1) {
						Piloto piloto0 = (Piloto) arg0;
						Piloto piloto1 = (Piloto) arg1;
						long p1Val = piloto1.getPtosPista();
						long p0Val = piloto0.getPtosPista();
						if (piloto0.getTimeStampChegeda() != 0
								&& piloto1.getTimeStampChegeda() != 0) {
							Long val = new Long(Long.MAX_VALUE
									- piloto0.getTimeStampChegeda());
							val = new Long(val.toString().substring(
									val.toString().length() / 4,
									val.toString().length()));
							p0Val = (val * piloto0.getNumeroVolta());
							val = new Long(Long.MAX_VALUE
									- piloto1.getTimeStampChegeda());
							val = new Long(val.toString().substring(
									val.toString().length() / 4,
									val.toString().length()));
							p1Val = (val * piloto1.getNumeroVolta());
						}
						return ((p1Val < p0Val) ? (-1) : ((p1Val == p0Val) ? 0
								: 1));
					}
				});

				for (int i = 0; i < pilotos.size(); i++) {
					Piloto piloto = (Piloto) pilotos.get(i);
					piloto.setPosicao(i + 1);
				}
			}
		} catch (Exception e) {
			atualizarDados();
			Logger.logarExept(e);
		}

	}

	public void mudarGiroMotor(final Object selectedItem) {
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
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setGiroMotor(giro);
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoBox(boolean modoBox) {
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_BOX, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setTpPneuBox(jogoCliente
							.getDadosCriarJogo().getTpPnueu());
					clientPaddockPack.setCombustBox(jogoCliente
							.getDadosCriarJogo().getCombustivel().intValue());
					clientPaddockPack.setAsaBox(jogoCliente.getDadosCriarJogo()
							.getAsa());
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoAgressivo(boolean modoAgressivo) {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_MODO_AGRESSIVO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
		}

	}

	public void mudarModoPilotagem(final String modo) {
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_PILOTAGEM, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setModoPilotagem(modo);
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarAutoPos() {
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_MODO_AUTOPOS, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}

			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarPos(final int tracado) {
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_TRACADO, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setTracado(tracado);
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}

		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoDRS(final boolean modo) {
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
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setDataObject(new Boolean(modo));
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void mudarModoKers(final boolean modo) {
		if (threadCmd != null && threadCmd.isAlive()) {
			return;
		}
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.MUDAR_KERS, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setDataObject(modo);
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
				} catch (Exception e) {
					Logger.logarExept(e);
				}
			}
		};
		threadCmd = new Thread(runnable);
		threadCmd.start();

	}

	public void driveThru(final Piloto pilotoSelecionado) {
		if (pilotoSelecionado == null
				|| !pilotoSelecionado.isJogadorHumano()
				|| sessaoCliente.getNomeJogador().equals(
						pilotoSelecionado.getNomeJogador())) {
			jogoCliente.adicionarInfoDireto(Lang
					.msg("selecionePilotoDriveThru"));
			return;
		}
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				try {
					ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
							Comandos.DRIVE_THRU, sessaoCliente);
					clientPaddockPack.setNomeJogo(jogoCliente
							.getNomeJogoCriado());
					clientPaddockPack.setDataObject(pilotoSelecionado
							.getNomeJogador());
					Object ret = controlePaddockCliente.enviarObjeto(
							clientPaddockPack, true);
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
}
