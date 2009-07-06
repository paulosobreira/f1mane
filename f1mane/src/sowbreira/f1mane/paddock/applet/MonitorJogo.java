package sowbreira.f1mane.paddock.applet;

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
				while (Comandos.ESPERANDO_JOGO_COMECAR.equals(estado)
						&& controlePaddockCliente.isComunicacaoServer()
						&& jogoAtivo) {
					verificaEstadoJogo();
					sleep(2000);
				}
				while (Comandos.MOSTRANDO_QUALIFY.equals(estado)
						&& controlePaddockCliente.isComunicacaoServer()
						&& jogoAtivo) {
					verificaEstadoJogo();
					iniciaJalena();
					if (monitorQualificacao == null) {
						monitorQualificacao = new Thread(
								new MonitorQualificacao(jogoCliente));
						atualizarDados();
						jogoCliente.preparaGerenciadorVisual();
						monitorQualificacao.start();
					}
					sleep(2000);
				}
				int delayVerificaStado = 20;
				while (Comandos.CORRIDA_INICIADA.equals(estado)
						&& controlePaddockCliente.isComunicacaoServer()
						&& jogoAtivo) {
					if (!atualizouDados) {
						atualizarDados();
						atualizaModoCarreira();
						atualizouDados = true;
					}
					if (monitorQualificacao != null) {
						jogoCliente.pularQualificacao();
						monitorQualificacao = null;
					}
					delayVerificaStado--;
					if (delayVerificaStado <= 0) {
						if (((Piloto) jogoCliente.getPilotos().get(0))
								.getPtosPista() > 0) {
							while (luz > 0) {
								apagarLuz();
								luz--;
							}
						}
						atualizarDadosParciais(jogoCliente.getDadosJogo(),
								jogoCliente.getPilotoSelecionado());
						if (controlePaddockCliente.getLatenciaReal() > 2000) {
							delayVerificaStado = 2;
						} else {
							delayVerificaStado = 4;
						}
						sleep(tempoCiclo);
						continue;
					}

					iniciaJalena();
					atualizaPosicoes();
					jogoCliente.atualizaPainel();
					if (atualizadorPainel == null) {
						atualizadorPainel = new Thread(new Runnable() {

							public void run() {
								while (jogoAtivo) {
									try {
										if (jogoCliente.getPilotoSelecionado() == null)
											jogoCliente
													.selecionaPilotoJogador();
										jogoCliente.atualizaPainel();

										Thread.sleep(80);
									} catch (Exception e) {
										Logger.logarExept(e);
									}
								}

							}

						});
						atualizadorPainel.start();
					}
					sleep(tempoCiclo);
				}
				while (Comandos.MOSTRA_RESULTADO_FINAL.equals(estado)
						&& controlePaddockCliente.isComunicacaoServer()
						&& jogoAtivo) {
					atualizarDados();
					jogoCliente.exibirResultadoFinal();
					jogoAtivo = false;
					sleep(tempoCiclo);

				}
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
				verificaEstadoJogo();
				sleep(controlePaddockCliente.getLatenciaMinima());
			} catch (Exception e) {
				Logger.logarExept(e);
			}
		}

	}

	private void atualizaModoCarreira() {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.VER_CARREIRA, sessaoCliente);

			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (ret != null) {
				CarreiraDadosSrv carreiraDadosSrv = (CarreiraDadosSrv) ret;
				if (carreiraDadosSrv.isModoCarreira()) {
					jogoCliente.setNomePilotoJogador(carreiraDadosSrv
							.getNomePiloto());
				}
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(), e
					.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void atualizaPosicoes() {
		try {
			Object ret = controlePaddockCliente.enviarObjeto(jogoCliente
					.getNomeJogoCriado(), true);
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
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(), e
					.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}

	}

	private void atualizarListaPilotos(Object[] posisArray) {
		for (int i = 0; i < posisArray.length; i++) {
			Posis posis = (Posis) posisArray[i];
			jogoCliente.atualizaPosicaoPiloto(posis);
		}

	}

	private void apagarLuz() {
		iniciaJalena();
		jogoCliente.apagarLuz();
		jogoCliente.atualizaPainel();

	}

	public void atualizarDados() {
		try {
			ClientPaddockPack clientPaddockPack = new ClientPaddockPack(
					Comandos.OBTER_DADOS_JOGO, sessaoCliente);
			clientPaddockPack.setNomeJogo(jogoCliente.getNomeJogoCriado());
			Object ret = controlePaddockCliente.enviarObjeto(clientPaddockPack);
			if (ret != null) {
				DadosJogo dadosJogo = (DadosJogo) ret;
				jogoCliente.setDadosJogo(dadosJogo);
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(), e
					.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void iniciaJalena() {
		if (jogoAtivo) {
			jogoCliente.iniciaJanela();
		}
	}

	private void sleep(long l) {
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
			if (ret != null) {
				SrvJogoPack jogoPack = (SrvJogoPack) ret;
				estado = jogoPack.getEstadoJogo();
			}
		} catch (Exception e) {
			Logger.logarExept(e);
			jogoAtivo = false;
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(), e
					.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
			JOptionPane.showMessageDialog(jogoCliente.getMainFrame(), e
					.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
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
}
