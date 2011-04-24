package sowbreira.f1mane.paddock.applet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.swing.JOptionPane;

import org.hibernate.property.MapAccessor;

import com.jhlabs.image.OilFilter;

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
import br.nnpe.Util;

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
				jogoCliente.preparaGerenciadorVisual();
				Logger.logar("jogoCliente.preparaGerenciadorVisual();");
				jogoCliente.carregaBackGroundCliente();
				Logger.logar(" run() jogoCliente.carregaBackGroundCliente();");
				esperaJogoComecar();
				mostraQualify();
				apagaLuzesLargada();
				processaCiclosCorrida(tempoCiclo);
				mostraResultadoFinal(tempoCiclo);
				verificaEstadoJogo();
				sleep(controlePaddockCliente.getLatenciaMinima());
			} catch (Exception e) {
				if (jogoCliente != null) {
					jogoCliente.matarTodasThreads();
				}
				Logger.logarExept(e);
			}
		}
		if (jogoCliente != null) {
			jogoCliente.matarTodasThreads();
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
							List pilotos = jogoCliente.getPilotos();
							for (Iterator iterator = pilotos.iterator(); iterator
									.hasNext();) {
								Piloto piloto = (Piloto) iterator.next();
								piloto.decIndiceTracado();
							}
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
				sleep(1000);
				atualizarDados();
				// jogoCliente.preparaGerenciadorVisual();
				jogoCliente.atualizaPainel();
				if (jogoCliente.getPilotos() != null) {
					Piloto p = (Piloto) jogoCliente.getPilotos().get(
							Util.intervalo(0,
									jogoCliente.getPilotos().size() - 1));
					int cont = 0;
					while (p.getPosicao() == 0
							|| p.getCiclosVoltaQualificacao() == 0) {
						if (cont > 5) {
							break;
						}
						sleep(500);
						atualizarDados();
						cont++;
					}
				}
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
			jogoCliente.carregaBackGroundCliente();
			Logger.logar(" esperaJogoComecar() jogoCliente.carregaBackGroundCliente();");
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
	private boolean consumidorAtivo = false;
	private Object[] posisArrayBuff;
	private double divPosis = 1;
	private int sleepConsumidorPosis = 10;
	private Map mapPosis = null;
	private boolean lagLongo = false;
	private long ultPoisis;

	public void atualizarListaPilotos(Object[] posisArray) {
		if (jogoCliente.getMainFrame().isAtualizacaoSuave()) {
			posisBuffer.add(posisArray);
			if (ultPoisis != 0
					&& (System.currentTimeMillis() - ultPoisis) > 10000) {
				lagLongo = true;
			}

			ultPoisis = System.currentTimeMillis();
			iniciaConsumidorPosis();
		} else {
			consumidorAtivo = false;
			if (posisArray != null) {
				for (int i = 0; i < posisArray.length; i++) {
					Posis posis = (Posis) posisArray[i];
					jogoCliente.atualizaPosicaoPiloto(posis);
				}
			}

		}
	}

	private void iniciaConsumidorPosis() {
		if (consumidorPosis != null && consumidorPosis.isAlive()) {
			return;
		}
		posisArrayBuff = (Object[]) posisBuffer.remove(0);
		if (posisArrayBuff != null) {
			for (int i = 0; i < posisArrayBuff.length; i++) {
				Posis posis = (Posis) posisArrayBuff[i];
				jogoCliente.atualizaPosicaoPiloto(posis);
			}
		}
		ultPoisis = 0;
		mapPosis = new HashMap();
		consumidorAtivo = true;
		consumidorPosis = new Thread(new Runnable() {
			@Override
			public void run() {
				while (jogoAtivo && consumidorAtivo) {
					if (!posisBuffer.isEmpty()) {
						posisArrayBuff = (Object[]) posisBuffer.remove(0);
					}
					if (posisArrayBuff != null) {
						for (int i = 0; i < posisArrayBuff.length; i++) {
							Posis posis = (Posis) posisArrayBuff[i];
							atualizaPosicaoPiloto(posis);
						}
					}
					sleep(sleepConsumidorPosis);
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
				piloto.setAutoPos(posis.autoPos);
				if (posis.idNo >= -1) {
					No no = (No) mapaIdsNos.get(new Integer(posis.idNo));
					if (piloto.getNoAtual() == null) {
						piloto.setNoAtual(no);
					} else {
						if (piloto.isJogadorHumano()) {
							jogoCliente.setPosisRec(no.getPoint());
							jogoCliente.setPosisAtual(piloto.getNoAtual()
									.getPoint());
						}
						if (lagLongo) {
							Logger.logar("lag longo");
							piloto.setNoAtual(no);
							lagLongo = false;
							return;
						}
						int indexPiloto = piloto.getNoAtual().getIndex();
						No noNovo = null;
						int diffINdex = no.getIndex() - indexPiloto;

						if (diffINdex < 0) {
							diffINdex = (no.getIndex() + jogoCliente
									.getNosDaPista().size()) - indexPiloto;
							if (piloto.isJogadorHumano()) {
								Logger.logar("no.getIndex() " + no.getIndex());
								Logger.logar("indexPiloto " + indexPiloto);
								Logger.logar("diffINdex " + diffINdex);
							}
						}

						if (diffINdex <= 10) {
							divPosis = 30;
							sleepConsumidorPosis = 20;
						} else if (diffINdex > 10 && diffINdex <= 20) {
							divPosis = 25;
							sleepConsumidorPosis = 20;
						} else if (diffINdex > 20 && diffINdex <= 50) {
							divPosis = 20;
							sleepConsumidorPosis = 20;
						} else if (diffINdex > 50 && diffINdex <= 100) {
							divPosis = 18;
							sleepConsumidorPosis = 19;
						} else if (diffINdex > 100 && diffINdex <= 150) {
							divPosis = 16;
							sleepConsumidorPosis = 18;
						} else if (diffINdex > 150 && diffINdex <= 200) {
							divPosis = 14;
							sleepConsumidorPosis = 17;
						} else if (diffINdex > 200 && diffINdex <= 250) {
							divPosis = 13;
							sleepConsumidorPosis = 16;
						} else if (diffINdex > 250 && diffINdex <= 300) {
							divPosis = 12;
							sleepConsumidorPosis = 15;
						} else if (diffINdex > 300 && diffINdex <= 350) {
							divPosis = 11;
							sleepConsumidorPosis = 14;
						} else if (diffINdex > 350 && diffINdex <= 400) {
							divPosis = 10;
							sleepConsumidorPosis = 13;
						} else if (diffINdex > 400 && diffINdex <= 450) {
							divPosis = 9;
							sleepConsumidorPosis = 12;
						} else if (diffINdex > 450 && diffINdex <= 500) {
							divPosis = 8;
							sleepConsumidorPosis = 11;
						} else if (diffINdex > 500 && diffINdex <= 600) {
							divPosis = 7;
							sleepConsumidorPosis = 10;
						} else if (diffINdex > 600 && diffINdex <= 700) {
							divPosis = 6;
							sleepConsumidorPosis = 10;
						} else if (diffINdex > 700 && diffINdex <= 800) {
							divPosis = 5;
							sleepConsumidorPosis = 10;
						} else if (diffINdex > 800 && diffINdex <= 900) {
							divPosis = 4;
							sleepConsumidorPosis = 10;
						} else if (diffINdex > 1000 && diffINdex <= 1200) {
							divPosis = 3;
							sleepConsumidorPosis = 9;
						} else if (diffINdex > 1200 && diffINdex <= 1400) {
							divPosis = 2;
							sleepConsumidorPosis = 8;
						} else if (diffINdex > 1400 && diffINdex <= 1500) {
							divPosis = 1;
							sleepConsumidorPosis = 7;
						} else if (diffINdex > 1500 && diffINdex <= 1600) {
							divPosis = 1;
							sleepConsumidorPosis = 6;
						} else {
							divPosis = 1;
							sleepConsumidorPosis = 5;
						}
						if (diffINdex > 5000
								&& !(jogoCliente.getNosDoBox().contains(no) && jogoCliente
										.getNosDaPista().contains(
												piloto.getNoAtual()))
								&& !(jogoCliente.getNosDaPista().contains(no) && jogoCliente
										.getNosDoBox().contains(
												piloto.getNoAtual()))) {
							if (piloto.isJogadorHumano()) {
								Logger.logar("(diffINdex > 3000)"
										+ piloto.getNome() + " " + diffINdex);
							}
							piloto.setNoAtual(no);
							return;
						}
						No noAtual = piloto.getNoAtual();

						boolean entrouNoBox = false;
						if (jogoCliente.getNosDoBox().contains(no)
								&& jogoCliente.getNosDaPista()
										.contains(noAtual)) {
							if ((Math.abs(jogoCliente.getCircuito()
									.getEntradaBoxIndex() - noAtual.getIndex())) < 50) {
								entrouNoBox = true;
							} else {
								divPosis = 10;
								sleepConsumidorPosis = 10;
							}
						}

						boolean saiuNoBox = false;
						if (jogoCliente.getNosDaPista().contains(no)
								&& jogoCliente.getNosDoBox().contains(noAtual)) {
							if (piloto.isJogadorHumano()) {
								Logger.logar("SAIU DO BOX "
										+ ((Math.abs(jogoCliente.getNosDoBox()
												.size() - noAtual.getIndex()))));
							}

							if ((Math.abs(jogoCliente.getNosDoBox().size()
									- noAtual.getIndex())) < 50) {
								saiuNoBox = true;
								if (piloto.isJogadorHumano()) {
									Logger.logar("SAIU DO BOX");
								}
							} else {
								divPosis = 15;
								sleepConsumidorPosis = 15;
							}
						}

						double ganho = (piloto.getGanho() / divPosis);
						if (ganho < 1) {
							ganho = 1;
						}
						indexPiloto += calculaNovoGanhoPosis(piloto, ganho);

						if (jogoCliente.getNosDaPista().contains(noAtual)) {
							int diff = indexPiloto
									- jogoCliente.getNosDaPista().size();

							if (diff >= 0) {
								indexPiloto = diff;
							}
							noNovo = (No) jogoCliente.getNosDaPista().get(
									indexPiloto);
						} else if (jogoCliente.getNosDoBox().contains(noAtual)) {
							int diff = indexPiloto
									- jogoCliente.getNosDoBox().size();
							if (diff >= 0) {
								indexPiloto = jogoCliente.getNosDoBox().size() - 1;
							}
							noNovo = (No) jogoCliente.getNosDoBox().get(
									indexPiloto);
						}
						if (entrouNoBox) {
							noNovo = (No) jogoCliente.getNosDoBox().get(0);
						}
						if (saiuNoBox) {
							noNovo = (No) jogoCliente.getNosDaPista().get(
									jogoCliente.getCircuito()
											.getSaidaBoxIndex());
						}
						if (noNovo != null)
							piloto.setNoAtual(noNovo);
						if (piloto
								.verificaColisaoCarroFrente(jogoCliente, true)) {
							if (diffINdex < 750) {
								piloto.setNoAtual(noAtual);
							}
							int novoTracado = Util.intervalo(0, 2);
							while (novoTracado == piloto.getTracado()) {
								novoTracado = Util.intervalo(0, 2);
							}
							if (piloto.getIndiceTracado() == 0)
								piloto.mudarTracado(novoTracado, jogoCliente,
										true);
						} else {
							if (piloto.getIndiceTracado() <= 0) {
								piloto.setTracadoAntigo(piloto.getTracado());
							}
							piloto.setTracado(posis.tracado);
							if (piloto.getIndiceTracado() <= 0
									&& piloto.getTracado() != piloto
											.getTracadoAntigo()) {
								if (piloto
										.verificaColisaoCarroFrente(jogoCliente)) {
									piloto.setIndiceTracado(0);
								} else {
									piloto.setIndiceTracado((int) (Carro.ALTURA * jogoCliente
											.getCircuito()
											.getMultiplicadorLarguraPista()));
								}
							}

						}
					}
				}
				break;
			}

		}
	}

	private double calculaNovoGanhoPosis(Piloto piloto, double ganho) {
		List ganhoList = (List) mapPosis.get(piloto);
		if (ganhoList == null) {
			ganhoList = new ArrayList();
			ganhoList.add(ganho);
		} else {
			ganhoList.add(ganho);
			if (ganhoList.size() > 12) {
				ganhoList.remove(0);
			}
		}
		double sum = 0;
		for (Iterator iterator = ganhoList.iterator(); iterator.hasNext();) {
			Double g = (Double) iterator.next();
			sum += g;
		}
		return sum / ganhoList.size();
	}

	public static void main(String[] args) {
		int valor = 2000;
		System.out.println(valor > 1500 && valor <= 2000);
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
			jogoCliente.getMainFrame().setVisible(false);
			jogoCliente.matarTodasThreads();
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

	public void mudarGiroMotor(final Object selectedItem) {
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
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void mudarModoBox(boolean modoBox) {
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
		Thread thread = new Thread(runnable);
		thread.start();

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
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void mudarAutoPos() {

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
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void mudarPos(final int tracado) {
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
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void mudarModoDRS(final boolean modo) {
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				try {
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
		Thread thread = new Thread(runnable);
		thread.start();

	}

	public void mudarModoKers(final boolean modo) {
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
		Thread thread = new Thread(runnable);
		thread.start();

	}
}
