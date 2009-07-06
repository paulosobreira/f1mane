package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sowbreira.f1mane.controles.InterfaceJogo;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresCarros;
import sowbreira.f1mane.paddock.entidades.TOs.DadosConstrutoresPilotos;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.DadosJogador;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.TOs.SrvPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.VoltaJogadorOnline;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;
import br.nnpe.Logger;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:50:08
 */
public class ControleClassificacao {
	private ControlePersistencia controlePersistencia;

	/**
	 * @param controlePersistencia
	 */
	public ControleClassificacao(ControlePersistencia controlePersistencia) {
		super();
		this.controlePersistencia = controlePersistencia;
	}

	public List obterListaClassificacao() {
		List returnList = new ArrayList();
		synchronized (controlePersistencia.getPaddockDados()) {
			Set jogadores = controlePersistencia.obterListaJogadores();
			for (Iterator iter = jogadores.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia
						.carregaDadosJogador(key);
				DadosJogador dadosJogador = new DadosJogador();
				dadosJogador.setNome(jogadorDadosSrv.getNome());
				dadosJogador.setUltimoAceso(jogadorDadosSrv.getUltimoLogon());
				dadosJogador.setPontos(somarPontos(jogadorDadosSrv));
				dadosJogador.setCorridas(jogadorDadosSrv.getCorridas().size());
				returnList.add(dadosJogador);
			}
		}
		Collections.sort(returnList, new Comparator() {

			public int compare(Object arg0, Object arg1) {
				DadosJogador d0 = (DadosJogador) arg0;
				DadosJogador d1 = (DadosJogador) arg1;
				return new Long(d1.getPontos()).compareTo(new Long(d0
						.getPontos()));
			}

		});
		return returnList;
	}

	private long somarPontos(JogadorDadosSrv jogadorDadosSrv) {
		List corridas = jogadorDadosSrv.getCorridas();
		int pts = 0;
		for (Iterator iter = corridas.iterator(); iter.hasNext();) {
			CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iter.next();
			pts += corridasDadosSrv.getPontos();
		}
		return pts;
	}

	public void processaCorrida(long tempoInicio, long tempoFim,
			Map mapVoltasJogadoresOnline, List pilotos,
			DadosCriarJogo dadosCriarJogo) {
		synchronized (controlePersistencia.getPaddockDados()) {
			for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				if (piloto.isJogadorHumano()) {
					JogadorDadosSrv jogadorDadosSrv = controlePersistencia
							.carregaDadosJogador(piloto.getNomeJogador());
					if (jogadorDadosSrv == null) {
						continue;
					}
					CorridasDadosSrv corridasDadosSrv = new CorridasDadosSrv();
					corridasDadosSrv
							.setTemporada(dadosCriarJogo.getTemporada());
					corridasDadosSrv.setPiloto(piloto.getNome());
					corridasDadosSrv.setCarro(piloto.getNomeCarro());
					corridasDadosSrv.setTempoInicio(tempoInicio);
					corridasDadosSrv.setTempoFim(tempoFim);
					corridasDadosSrv.setCircuito(dadosCriarJogo
							.getCircuitoSelecionado());
					corridasDadosSrv.setNumVoltas(dadosCriarJogo
							.getQtdeVoltas().intValue());
					corridasDadosSrv.setNivel(dadosCriarJogo.getNivelCorrida());
					int pts = gerarPontos(piloto);
					corridasDadosSrv.setPontos(pts);
					corridasDadosSrv.setPosicao(piloto.getPosicao());
					processarPontos(mapVoltasJogadoresOnline, piloto,
							corridasDadosSrv);
					jogadorDadosSrv.getCorridas().add(corridasDadosSrv);
					CarreiraDadosSrv carreiraDadosSrv = jogadorDadosSrv
							.getCarreiraDadosSrv();
					if (carreiraDadosSrv.isModoCarreira()) {
						int ptsCorrida = corridasDadosSrv.getPontos();
						carreiraDadosSrv.setPtsConstrutores(carreiraDadosSrv
								.getPtsConstrutores()
								+ ptsCorrida + 5);
					}
				}
			}
		}
		try {
			controlePersistencia.gravarDados();
		} catch (Exception e) {
			ServletBaseDados.topExecpts(e);
		}

	}

	private void processarPontos(Map mapVoltasJogadoresOnline, Piloto piloto,
			CorridasDadosSrv corridasDadosSrv) {
		double numVoltas = corridasDadosSrv.getNumVoltas();
		double voltasCompletadas = 0;
		for (Iterator iterMaster = mapVoltasJogadoresOnline.keySet().iterator(); iterMaster
				.hasNext();) {
			Integer lap = (Integer) iterMaster.next();
			List voltas = (List) mapVoltasJogadoresOnline.get(lap);
			for (Iterator iter = voltas.iterator(); iter.hasNext();) {
				VoltaJogadorOnline jogadorOnline = (VoltaJogadorOnline) iter
						.next();
				if (jogadorOnline.getJogador().equals(piloto.getNomeJogador())) {
					voltasCompletadas++;
					if (!jogadorOnline.getPiloto().equals(piloto.getNome())) {
						corridasDadosSrv.setMudouCarro(true);
					}
					continue;
				}
			}
		}
		if (voltasCompletadas >= numVoltas) {
			voltasCompletadas = numVoltas;
		}

		if (corridasDadosSrv.isMudouCarro()) {
			corridasDadosSrv.setPontos(0);
		} else {
			double porcent = voltasCompletadas / numVoltas;
			corridasDadosSrv.setPorcentConcluida((int) (porcent * 100));
			int pontos = (int) (porcent * corridasDadosSrv.getPontos());
			if (InterfaceJogo.FACIL.equals(corridasDadosSrv.getNivel())) {
				pontos /= 2;
			}
			if (InterfaceJogo.DIFICIL.equals(corridasDadosSrv.getNivel())) {
				pontos *= 2;
			}
			corridasDadosSrv.setPontos(pontos);

		}
	}

	public static void main(String[] args) {
		int var = 15;
		var /= 4;
		Logger.logar(var);
		// Logger.logar(Math.ceil(4.0 / 6.0));
	}

	private int gerarPontos(Piloto p) {
		if (p.getPosicao() == 1) {
			return 10;
		} else if (p.getPosicao() == 2) {
			return 8;
		} else if (p.getPosicao() == 3) {
			return 6;
		} else if (p.getPosicao() == 4) {
			return 5;
		} else if (p.getPosicao() == 5) {
			return 4;
		} else if (p.getPosicao() == 6) {
			return 3;
		} else if (p.getPosicao() == 7) {
			return 2;
		} else if (p.getPosicao() == 8) {
			return 1;
		} else {
			return 0;
		}
	}

	public List obterListaCorridas(String nomeJogador) {
		synchronized (controlePersistencia.getPaddockDados()) {
			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(nomeJogador);
			if (jogadorDadosSrv == null) {
				return null;
			}
			return jogadorDadosSrv.getCorridas();

		}
	}

	public void preencherListaContrutores(SrvPaddockPack srvPaddockPack) {
		Map mapaCarros = new HashMap();
		Map mapaPilotos = new HashMap();

		synchronized (controlePersistencia.getPaddockDados()) {
			Set jogadores = controlePersistencia.obterListaJogadores();
			for (Iterator iter = jogadores.iterator(); iter.hasNext();) {
				String key = (String) iter.next();
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia
						.carregaDadosJogador(key);
				List corridas = jogadorDadosSrv.getCorridas();
				for (Iterator iterator = corridas.iterator(); iterator
						.hasNext();) {
					CorridasDadosSrv corridasDadosSrv = (CorridasDadosSrv) iterator
							.next();
					Integer ptsCarro = (Integer) mapaCarros
							.get(corridasDadosSrv.getCarro());
					if (ptsCarro == null) {
						mapaCarros.put(corridasDadosSrv.getCarro(),
								new Integer(corridasDadosSrv.getPontos()));
					} else {
						mapaCarros.put(corridasDadosSrv.getCarro(),
								new Integer(corridasDadosSrv.getPontos()
										+ ptsCarro.intValue()));
					}
					Integer ptsPiloto = (Integer) mapaPilotos
							.get(corridasDadosSrv.getPiloto());
					if (ptsPiloto == null) {
						mapaPilotos.put(corridasDadosSrv.getPiloto(),
								new Integer(corridasDadosSrv.getPontos()));
					} else {
						mapaPilotos.put(corridasDadosSrv.getPiloto(),
								new Integer(corridasDadosSrv.getPontos()
										+ ptsPiloto.intValue()));
					}

				}
			}
		}

		List listaCarros = new LinkedList();
		List listaPilotos = new LinkedList();
		for (Iterator iterator = mapaCarros.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			DadosConstrutoresCarros dadosConstrutoresCarros = new DadosConstrutoresCarros();
			dadosConstrutoresCarros.setNome(key);
			dadosConstrutoresCarros.setPontos((Integer) mapaCarros.get(key));
			if (dadosConstrutoresCarros.getPontos() > 0)
				listaCarros.add(dadosConstrutoresCarros);

		}
		for (Iterator iterator = mapaPilotos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			DadosConstrutoresPilotos dadosConstrutoresPilotos = new DadosConstrutoresPilotos();
			dadosConstrutoresPilotos.setNome(key);
			dadosConstrutoresPilotos.setPontos((Integer) mapaPilotos.get(key));
			if (dadosConstrutoresPilotos.getPontos() > 0)
				listaPilotos.add(dadosConstrutoresPilotos);
		}
		srvPaddockPack.setListaConstrutoresCarros(listaCarros);
		srvPaddockPack.setListaConstrutoresPilotos(listaPilotos);
	}

	public CarreiraDadosSrv verCarreira(ClientPaddockPack clientPaddockPack) {
		synchronized (controlePersistencia.getPaddockDados()) {
			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(clientPaddockPack.getSessaoCliente()
							.getNomeJogador());
			CarreiraDadosSrv carreiraDadosSrv = jogadorDadosSrv
					.getCarreiraDadosSrv();
			if (carreiraDadosSrv.getPtsCarro() == 0) {
				carreiraDadosSrv.setPtsCarro(650);
			}
			if (carreiraDadosSrv.getPtsPiloto() == 0) {
				carreiraDadosSrv.setPtsPiloto(650);
			}
			return carreiraDadosSrv;
		}
	}

	public Object atualizaCarreira(ClientPaddockPack clientPaddockPack) {
		synchronized (controlePersistencia.getPaddockDados()) {
			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(clientPaddockPack.getSessaoCliente()
							.getNomeJogador());
			CarreiraDadosSrv carreiraDadosSrv = jogadorDadosSrv
					.getCarreiraDadosSrv();
			// Validar pts servidor.
			// int ptConsBan = carreiraDadosSrv.getPtsConstrutores();
			// int ptConsCli = clientPaddockPack.getJogadorDadosSrv()
			// .getPtsConstrutores();
			// int ptPilotoBan = carreiraDadosSrv.getPtsPiloto();
			// int ptPilotoCli = clientPaddockPack.getJogadorDadosSrv()
			// .getPtsPiloto();

			carreiraDadosSrv.setNomePiloto(clientPaddockPack
					.getJogadorDadosSrv().getNomePiloto());
			carreiraDadosSrv.setNomeCarro(clientPaddockPack
					.getJogadorDadosSrv().getNomeCarro());
			carreiraDadosSrv.setImgCarro(clientPaddockPack.getJogadorDadosSrv()
					.getImgCarro());
			carreiraDadosSrv.setPtsCarro(clientPaddockPack.getJogadorDadosSrv()
					.getPtsCarro());
			carreiraDadosSrv.setPtsPiloto(clientPaddockPack
					.getJogadorDadosSrv().getPtsPiloto());
			carreiraDadosSrv.setPtsConstrutores(clientPaddockPack
					.getJogadorDadosSrv().getPtsConstrutores());
			carreiraDadosSrv.setModoCarreira(clientPaddockPack
					.getJogadorDadosSrv().isModoCarreira());
			carreiraDadosSrv.setC1R(clientPaddockPack.getJogadorDadosSrv()
					.getC1R());
			carreiraDadosSrv.setC1G(clientPaddockPack.getJogadorDadosSrv()
					.getC1G());
			carreiraDadosSrv.setC1B(clientPaddockPack.getJogadorDadosSrv()
					.getC1B());
			carreiraDadosSrv.setC2R(clientPaddockPack.getJogadorDadosSrv()
					.getC2R());
			carreiraDadosSrv.setC2G(clientPaddockPack.getJogadorDadosSrv()
					.getC2G());
			carreiraDadosSrv.setC2B(clientPaddockPack.getJogadorDadosSrv()
					.getC2B());
			jogadorDadosSrv.setCarreiraDadosSrv(carreiraDadosSrv);
			return new MsgSrv(Lang.msg("250"));
		}
	}

	public CarreiraDadosSrv obterCarreiraSrv(String key) {
		synchronized (controlePersistencia.getPaddockDados()) {

			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(key);
			CarreiraDadosSrv carreiraDadosSrv = jogadorDadosSrv
					.getCarreiraDadosSrv();
			return carreiraDadosSrv;
		}
	}
}
