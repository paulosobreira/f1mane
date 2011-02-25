package sowbreira.f1mane.paddock.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import sowbreira.f1mane.entidades.Carro;
import sowbreira.f1mane.entidades.No;
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
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 05/08/2007 as 11:43:33
 */
public class MonitorJogo implements Runnable {
	private JogoCliente jogoCliente;
	private ControlePaddockCliente controlePaddockCliente;
	private String estado = Comandos.ESPERANDO_JOGO_COMECAR;
	private SessaoCliente sessaoCliente;
	private Thread monitorQualificacao;
	private Thread atualizadorPainel;
	private boolean jogoAtivo = true;
	private int luz = 5;
	public long lastPosis = 0;
	public boolean procPosis = false;
	private boolean setouZoom = false;
	private boolean atualizouDados;

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
		while (controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			try {
				long tempoCiclo = jogoCliente.getTempoCiclo();
				if (tempoCiclo < controlePaddockCliente.getLatenciaMinima()) {
					tempoCiclo = controlePaddockCliente.getLatenciaMinima();
				}
				esperaJogoComecar();
				mostraQualify();
				apagaLuzesLargada();
				processaCiclosCorrida(tempoCiclo);
				mostraResultadoFinal(tempoCiclo);
				verificaEstadoJogo();
				sleep(controlePaddockCliente.getLatenciaMinima());
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	private void apagaLuzesLargada() {
		if (Comandos.LUZES5.equals(estado)) {
			while (luz > 4) {
				apagarLuz();
				luz--;
			}
		} else if (Comandos.LUZES4.equals(estado)) {
			while (luz > 3) {
				apagarLuz();
				luz--;
			}
		} else if (Comandos.LUZES3.equals(estado)) {
			while (luz > 2) {
				apagarLuz();
				luz--;
			}
		} else if (Comandos.LUZES2.equals(estado)) {
			while (luz > 1) {
				apagarLuz();
				luz--;
			}
		} else if (Comandos.LUZES1.equals(estado)) {
			while (luz > 0) {
				apagarLuz();
				luz--;
			}
		}
	}

	private void mostraResultadoFinal(long tempoCiclo) {
		while (Comandos.MOSTRA_RESULTADO_FINAL.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			List pilotos = jogoCliente.getPilotos();
			for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
				Piloto piloto = (Piloto) iterator.next();
				// jogoCliente.adicionarInfoDireto(piloto.getPosicao() + " "
				// + piloto.getNome() + " " + piloto.getCarro().getNome());
				atualizarDadosParciais(jogoCliente.getDadosJogo(), piloto);
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					Logger.logarExept(e);
				}
			}
			atualizarDados();
			jogoCliente.exibirResultadoFinal();
			jogoAtivo = false;
			sleep(tempoCiclo);

		}
	}

	private void processaCiclosCorrida(long tempoCiclo) {
		int delayVerificaStado = 20;
		while (Comandos.CORRIDA_INICIADA.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			disparaAtualizadorPainel();
			if (!atualizouDados) {
				atualizarDados();
				atualizaModoCarreira();
				atualizouDados = true;
			}
			if (monitorQualificacao != null) {
				jogoCliente.pularQualificacao();
				monitorQualificacao = null;
			}
			if (!setouZoom) {
				jogoCliente.setZoom(0.5);

				setouZoom = true;
			}
			delayVerificaStado--;
			if (delayVerificaStado <= 0) {
				if (((Piloto) jogoCliente.getPilotos().get(0)).getNumeroVolta() != 0) {
					while (luz > 0) {
						apagarLuz();
						luz--;
					}
					for (Iterator iterator = jogoCliente.getPilotos()
							.iterator(); iterator.hasNext();) {
						Piloto piloto = (Piloto) iterator.next();
						piloto.setVelocidade(1);
					}
				}

				atualizarDadosParciais(jogoCliente.getDadosJogo(),
						jogoCliente.getPilotoSelecionado());
				if (controlePaddockCliente.getLatenciaReal() > 2000) {
					delayVerificaStado = 2;
				} else {
					delayVerificaStado = 4;
				}
				continue;
			}
			iniciaJalena();
			atualizaPosicoes();
			sleep(tempoCiclo);
		}
	}

	private void disparaAtualizadorPainel() {
		if (atualizadorPainel == null) {
			atualizadorPainel = new Thread(new Runnable() {

				public void run() {
					while (jogoAtivo) {
						try {
							if (jogoCliente.getPilotoSelecionado() == null)
								jogoCliente.selecionaPilotoJogador();
							jogoCliente.atualizaPainel();
							jogoCliente.verificaProgramacaoBox();
							Thread.sleep(100);
						} catch (Exception e) {
							Logger.logarExept(e);
						}
					}

				}

			});
			atualizadorPainel.start();
		}
	}

	private void mostraQualify() {
		while (Comandos.MOSTRANDO_QUALIFY.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			verificaEstadoJogo();
			iniciaJalena();

			if (monitorQualificacao == null) {
				monitorQualificacao = new Thread(new MonitorQualificacao(
						jogoCliente));
				atualizarDados();
				jogoCliente.preparaGerenciadorVisual();
				jogoCliente.atualizaPainel();
				sleep(1000);
				monitorQualificacao.start();
			}
			sleep(100);
		}
		if (monitorQualificacao != null)
			monitorQualificacao.interrupt();
	}

	private void esperaJogoComecar() {
		while (Comandos.ESPERANDO_JOGO_COMECAR.equals(estado)
				&& controlePaddockCliente.isComunicacaoServer() && jogoAtivo) {
			verificaEstadoJogo();
			sleep(1000);
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
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void atualizaPosicoes() {
		try {
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
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	private boolean retornoNaoValido(Object ret) {
		if (ret instanceof ErroServ || ret instanceof MsgSrv) {
			return true;
		}
		return false;
	}

	private Vector posisBuffer = new Vector();
	private Thread consumidorPosis = null;
	private Object[] posisArrayBuff;

	public void atualizarListaPilotos(Object[] posisArray) {
		posisBuffer.add(posisArray);
		iniciaConsumidorPosis();

	}

	private void iniciaConsumidorPosis() {
		if (consumidorPosis != null) {
			return;
		}
		consumidorPosis = new Thread(new Runnable() {
			@Override
			public void run() {
				while (!jogoCliente.isCorridaTerminada()) {
					if (!posisBuffer.isEmpty()) {
						posisArrayBuff = (Object[]) posisBuffer.remove(0);
					}
					if (posisArrayBuff != null) {
						for (int i = 0; i < posisArrayBuff.length; i++) {
							Posis posis = (Posis) posisArrayBuff[i];
							atualizaPosicaoPiloto(posis);
						}
					}
					sleep(40);
				}
			}
		});
		consumidorPosis.start();

	}

	public void atualizaPosicaoPiloto(Posis posis) {
		List pilotos = jogoCliente.getPilotos();
		Map mapaIdsNos = jogoCliente.getMapaIdsNos();
		Map mapaNosIds = jogoCliente.getMapaNosIds();
		for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
			Piloto piloto = (Piloto) iter.next();
			if (piloto.getId() == posis.idPiloto) {
				piloto.setAgressivo(posis.agressivo);
				piloto.setJogadorHumano(posis.humano);
				piloto.setTracado(posis.tracado);
				piloto.setAutoPos(posis.autoPos);
				if (posis.idNo >= -1) {
					No no = (No) mapaIdsNos.get(new Integer(posis.idNo));
					if (piloto.getNoAtual() == null) {
						piloto.setNoAtual(no);
					} else {
						No noAtual = piloto.getNoAtual();
						int indexPiloto = piloto.getNoAtual().getIndex();
						No noNovo = null;
						if (indexPiloto < no.getIndex()) {
							indexPiloto += piloto.getGanho() / 3;
							if (jogoCliente.getNosDaPista().contains(no)) {
								int diff = indexPiloto
										- jogoCliente.getNosDaPista().size();

								if (diff >= 0) {
									indexPiloto = diff;
								}
								noNovo = (No) jogoCliente.getNosDaPista().get(
										indexPiloto);
							} else {
								int diff = indexPiloto
										- jogoCliente.getNosDoBox().size();
								if (diff >= 0) {
									indexPiloto = jogoCliente.getNosDoBox()
											.size() - 1;
								}
								noNovo = (No) jogoCliente.getNosDoBox().get(
										indexPiloto);
							}
							if (noNovo != null)
								piloto.setNoAtual(noNovo);
							if (piloto.verificaColisaoCarroFrente(jogoCliente)) {
								piloto.setNoAtual(noAtual);
							}
						} else {
							piloto.setNoAtual(no);
						}
					}
				}
				break;
			}

		}
	}

	private void apagarLuz() {
		iniciaJalena();
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
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void iniciaJalena() {
		if (jogoAtivo) {
			jogoCliente.iniciaJanela();
		}
	}

	public void sleep(long l) {
		try {
			Thread.sleep(l);
		} catch (InterruptedException e) {
			Logger.logarExept(e);
		}

	}

	private void verificaEstadoJogo() {
		try {
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
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	public String getEstado() {
		return estado;
	}

	public void abandonar() {
		try {
			jogoAtivo = false;
			jogoCliente.getMainFrame().setVisible(false);
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.ABANDONAR, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);

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
				jogoCliente.travouRodas(dadosParciais.travadaRoda);
				if (dadosParciais.texto != null
						&& !"".equals(dadosParciais.texto))
					dadosJogo.setTexto(dadosParciais.texto);
				dadosJogo.setVoltaAtual(dadosParciais.voltaAtual);
				List pilotos = dadosJogo.getPilotosList();
				for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
					Piloto piloto = (Piloto) iter.next();
					piloto.setPtosPista(dadosParciais.pilotsPonts[piloto
							.getId() - 1]);
					piloto.calcularVolta(jogoCliente);
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
						piloto.setBox(dadosParciais.pselBox);
						piloto.setStress(dadosParciais.pselStress);
						piloto.getCarro().setMotor(dadosParciais.pselMotor);
						piloto.getCarro().setPneus(dadosParciais.pselPneus);
						piloto.getCarro().setDurabilidadeMaxPneus(
								dadosParciais.pselMaxPneus);
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
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(),
					e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	public void mudarGiroMotor(Object selectedItem) {
		try {
			String giro = (String) selectedItem;
			if (!Carro.GIRO_MAX.equals(giro) && !Carro.GIRO_MIN.equals(giro)
					&& !Carro.GIRO_NOR.equals(giro)) {
				return;
			}
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_GIRO_MOTOR, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			clientPaddockPack.setGiroMotor(giro);
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}

	}

	public void mudarModoBox(boolean modoBox) {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_MODO_BOX, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			clientPaddockPack.setTpPneuBox(jogoCliente.getDadosCriarJogo()
					.getTpPnueu());
			clientPaddockPack.setCombustBox(jogoCliente.getDadosCriarJogo()
					.getCombustivel().intValue());
			clientPaddockPack.setAsaBox(jogoCliente.getDadosCriarJogo()
					.getAsa());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}
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
			jogoAtivo = false;
		}

	}

	public void mudarModoPilotagem(String modo) {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_MODO_PILOTAGEM, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			clientPaddockPack.setModoPilotagem(modo);
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}
	}

	public void mudarAutoPos() {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_MODO_AUTOPOS, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}
	}

	public void mudarPos(int tracado) {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.MUDAR_TRACADO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			clientPaddockPack.setTracado(tracado);
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack,
					true);
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
		}
	}
}
