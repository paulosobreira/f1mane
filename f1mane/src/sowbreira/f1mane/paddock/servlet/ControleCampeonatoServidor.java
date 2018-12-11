package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import br.nnpe.Constantes;
import br.nnpe.Dia;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.controles.ControleJogoLocal;
import sowbreira.f1mane.controles.ControleRecursos;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.TemporadasDefauts;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.TOs.CampeonatoTO;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.CorridaCampeonatoTO;
import sowbreira.f1mane.paddock.entidades.TOs.DadosClassificacaoCarros;
import sowbreira.f1mane.paddock.entidades.TOs.DadosClassificacaoJogador;
import sowbreira.f1mane.paddock.entidades.TOs.DadosClassificacaoPilotos;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonatoSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.DadosCorridaCampeonatoSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.CarregadorRecursos;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class ControleCampeonatoServidor {

	private ControlePersistencia controlePersistencia;
	private ControlePaddockServidor controlePaddockServidor;
	private CarregadorRecursos carregadorRecursos = CarregadorRecursos
			.getCarregadorRecursos(false);

	public ControleCampeonatoServidor(ControlePersistencia controlePersistencia,
			ControlePaddockServidor controlePaddockServidor) {
		super();
		this.controlePersistencia = controlePersistencia;
		this.controlePaddockServidor = controlePaddockServidor;
	}

	public Object criarCampeonato(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack.getSessaoCliente() == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		CampeonatoSrv campeonato = (CampeonatoSrv) clientPaddockPack
				.getDataObject();
		return criarCampeonato(campeonato,
				clientPaddockPack.getSessaoCliente().getToken());
	}

	public Object criarCampeonato(CampeonatoSrv campeonato, String token) {
		if (token == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		if (Util.isNullOrEmpty(campeonato.getNome())) {
			return (new MsgSrv(Lang.msg("nomeCampeonatoObrigatorio")));
		}
		if (campeonato.getIdPiloto() == null) {
			return (new MsgSrv(Lang.msg("selecionePiloto")));
		}
		if (campeonato.getCorridaCampeonatos().size() < 5) {
			return (new MsgSrv(Lang.msg("min5CorridasCampeonato")));
		}
		Session session = controlePersistencia.getSession();
		try {
			List pesquisaCampeonatosEmAberto = controlePersistencia
					.pesquisaCampeonatosEmAberto(token, session, false);
			if (!pesquisaCampeonatosEmAberto.isEmpty()) {
				return (new MsgSrv(Lang.msg("campeonatoEmAberto")));
			}
			if (controlePersistencia.existeNomeCampeonato(session,
					campeonato.getNome())) {
				return new MsgSrv(Lang.msg("nomeCampeonatoNaoDisponivel"));
			}

			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(token, session);
			if (jogadorDadosSrv == null) {
				return (new MsgSrv(Lang.msg("238")));
			}
			if (verificaCampeonatoEmAberto(jogadorDadosSrv, session)) {
				return (new MsgSrv(Lang.msg("jogadorTemCampeonatoEmAberto")));
			}
			Map<String, TemporadasDefauts> carregarTemporadasPilotosDefauts = carregadorRecursos
					.carregarTemporadasPilotosDefauts();
			Logger.logar(
					"campeonato.getTemporada() " + campeonato.getTemporada());
			TemporadasDefauts temporadasDefauts = carregarTemporadasPilotosDefauts
					.get("t" + campeonato.getTemporada());
			campeonato.setTrocaPneus(temporadasDefauts.getTrocaPneu());
			campeonato.setDrs(temporadasDefauts.getDrs());
			campeonato.setErs(temporadasDefauts.getErs());
			campeonato
					.setReabastecimento(temporadasDefauts.getReabastecimento());
			campeonato.setQtdeVoltas(Constantes.MIN_VOLTAS);
			campeonato.setNivel(ControleJogoLocal.NORMAL);
			campeonato.setJogadorDadosSrv(jogadorDadosSrv);
			List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
					.getCorridaCampeonatos();
			long rodada = 1;
			for (Iterator iterator = corridaCampeonatos.iterator(); iterator
					.hasNext();) {
				CorridaCampeonatoSrv corridaCampeonato = (CorridaCampeonatoSrv) iterator
						.next();
				corridaCampeonato.setRodada(rodada);
				corridaCampeonato.setCampeonato(campeonato);
				rodada++;

			}
			campeonato.setFinalizado(false);
			controlePersistencia.gravarDados(session, campeonato);
			return (new MsgSrv(Lang.msg("campeonatoCriado")));
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	private boolean verificaCampeonatoEmAberto(JogadorDadosSrv jogadorDadosSrv,
			Session session) {
		List campeonatos = controlePersistencia
				.pesquisaCampeonatos(jogadorDadosSrv, session);
		for (Iterator iterator = campeonatos.iterator(); iterator.hasNext();) {
			CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
			List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
					.getCorridaCampeonatos();
			for (CorridaCampeonatoSrv corridaCampeonato : corridaCampeonatos) {
				if (corridaCampeonato.getTempoFim() == null
						|| corridaCampeonato.getTempoFim() == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public Object listarCampeonatos() {
		Session session = controlePersistencia.getSession();
		try {
			List<CampeonatoSrv> campeonatos = controlePersistencia
					.obterListaCampeonatos(session);
			List retorno = new ArrayList();
			for (Iterator iterator = campeonatos.iterator(); iterator
					.hasNext();) {
				CampeonatoSrv campeonato = (CampeonatoSrv) iterator.next();
				Object[] row = new Object[5];
				row[0] = campeonato.getNome();
				row[1] = campeonato.getJogadorDadosSrv().getNome();
				row[2] = verificaCampeonatoConcluido(campeonato);
				row[3] = campeonato.getDataCriacao();
				row[4] = campeonato.getId();
				retorno.add(row);
			}
			return retorno;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public boolean verificaCampeonatoConcluido(CampeonatoSrv campeonato) {
		List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (CorridaCampeonatoSrv corridaCampeonato : corridaCampeonatos) {
			if (corridaCampeonato.getTempoFim() == null) {
				return false;
			}
		}
		return true;
	}

	public Object obterCampeonato(ClientPaddockPack clientPaddockPack) {
		Long idCampeonato = (Long) clientPaddockPack.getDataObject();
		return obterCampeonato(idCampeonato.toString());
	}

	public Object obterCampeonato(String idCampeonato) {
		Session session = controlePersistencia.getSession();
		try {
			CampeonatoSrv campeonato = controlePersistencia
					.pesquisaCampeonato(session, idCampeonato, true);
			return campeonato;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public void processaCorrida(long tempoInicio, long tempoFim,
			Map mapVoltasJogadoresOnline, List pilotos,
			DadosCriarJogo dadosCriarJogo,
			ControleClassificacao controleClassificacao) {
		if (!Constantes.DATABASE) {
			return;
		}
		String idCampeonato = dadosCriarJogo.getIdCampeonato().toString();
		Session session = controlePersistencia.getSession();
		try {
			CampeonatoSrv campeonato = controlePersistencia
					.pesquisaCampeonato(session, idCampeonato, false);
			if (campeonato == null) {
				return;
			}
			CorridaCampeonatoSrv corridaCampeonatoCorrente = null;
			String circuitoSelecionado = Util
					.substVogais(dadosCriarJogo.getCircuitoSelecionado());
			for (CorridaCampeonatoSrv corridaCampeonato : campeonato
					.getCorridaCampeonatos()) {
				if (circuitoSelecionado
						.equals(corridaCampeonato.getNomeCircuito())) {
					corridaCampeonatoCorrente = corridaCampeonato;
					break;
				}
			}
			if (corridaCampeonatoCorrente == null) {
				Logger.logar("corridaCampeonatoCorrente nulo");
				return;
			}
			corridaCampeonatoCorrente.setTempoInicio(tempoInicio);
			corridaCampeonatoCorrente.setTempoFim(tempoFim);
			for (Iterator iter = pilotos.iterator(); iter.hasNext();) {
				Piloto piloto = (Piloto) iter.next();
				DadosCorridaCampeonatoSrv dadosCorridaCampeonato = new DadosCorridaCampeonatoSrv();
				dadosCorridaCampeonato
						.setCorridaCampeonato(corridaCampeonatoCorrente);
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia
						.carregaDadosJogador(piloto.getTokenJogador(), session);
				if (jogadorDadosSrv != null) {
					dadosCorridaCampeonato.setJogador(jogadorDadosSrv.getId());
				}
				dadosCorridaCampeonato.setPiloto(piloto.getNome());
				dadosCorridaCampeonato
						.setCorPiloto(piloto.getCarro().getCor1Hex());
				dadosCorridaCampeonato.setCarro(piloto.getNomeCarro());
				int pts = controleClassificacao.gerarPontos(piloto);
				dadosCorridaCampeonato.setPontos(pts);
				dadosCorridaCampeonato.setPosicao(piloto.getPosicao());
				dadosCorridaCampeonato
						.setTpPneu(piloto.getCarro().getTipoPneu());
				dadosCorridaCampeonato.setNumVoltas(piloto.getNumeroVolta());
				Volta volta = piloto.obterVoltaMaisRapida();
				if (volta != null)
					dadosCorridaCampeonato
							.setVoltaMaisRapida(volta.getTempoVoltaFormatado());
				dadosCorridaCampeonato
						.setQtdeParadasBox(piloto.getQtdeParadasBox());
				dadosCorridaCampeonato.setDesgastePneus(String.valueOf(
						piloto.getCarro().getPorcentagemDesgastePneus() + "%"));
				dadosCorridaCampeonato.setCombustivelRestante(String.valueOf(
						piloto.getCarro().getPorcentagemCombustivel() + "%"));
				dadosCorridaCampeonato.setDesgasteMotor(String.valueOf(
						piloto.getCarro().getPorcentagemDesgasteMotor() + "%"));
				corridaCampeonatoCorrente.getDadosCorridaCampeonatos()
						.add(dadosCorridaCampeonato);
			}
			try {
				controlePersistencia.gravarDados(session, campeonato);
			} catch (Exception e) {
				Logger.topExecpts(e);
			}
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public CampeonatoSrv obterCampeonatoEmAberto(String token) {
		List pesquisaCampeonatos = null;
		Session session = controlePersistencia.getSession();
		try {
			pesquisaCampeonatos = controlePersistencia
					.pesquisaCampeonatosEmAberto(token, session, true);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		if (pesquisaCampeonatos == null || pesquisaCampeonatos.isEmpty()) {
			return null;
		}
		CampeonatoSrv campeonato = (CampeonatoSrv) pesquisaCampeonatos.get(0);
		List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonatoSrv corridaCampeonato = (CorridaCampeonatoSrv) iterator
					.next();
			corridaCampeonato.setCampeonato(null);
		}
		return campeonato;
	}

	public CampeonatoSrv obterCampeonatoId(String id) {
		CampeonatoSrv campeonato = null;
		Session session = controlePersistencia.getSession();
		try {
			campeonato = controlePersistencia.pesquisaCampeonatoId(id, session);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		if (campeonato == null) {
			return null;
		}
		List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonatoSrv corridaCampeonato = (CorridaCampeonatoSrv) iterator
					.next();
			corridaCampeonato.setCampeonato(null);
		}
		return campeonato;
	}

	public CampeonatoTO obterCampeonatoIdTO(String id) {
		CampeonatoSrv campeonato = obterCampeonatoId(id);
		if (campeonato == null) {
			return null;
		}
		CampeonatoTO campeonatoTO = new CampeonatoTO();
		processsaCorridaCampeonatoTO(campeonato, campeonatoTO);
		Collections.sort(campeonatoTO.getCorridas(),
				new Comparator<CorridaCampeonatoTO>() {
					public int compare(CorridaCampeonatoTO arg0,
							CorridaCampeonatoTO arg1) {
						return arg0.getRodada().compareTo(arg1.getRodada());
					}
				});
		preencherContrutores(campeonato, campeonatoTO);
		return campeonatoTO;

	}

	public CampeonatoTO obterCampeonatoEmAbertoTO(String token) {
		CampeonatoSrv campeonato = obterCampeonatoEmAberto(token);
		if (campeonato == null) {
			return null;
		}
		CampeonatoTO campeonatoTO = new CampeonatoTO();
		processsaCorridaCampeonatoTO(campeonato, campeonatoTO);
		if ("0".equals(campeonato.getIdPiloto())) {
			CarreiraDadosSrv carreiraDados = controlePaddockServidor
					.obterCarreiraSrv(token);
			if (carreiraDados == null) {
				return null;
			}
			campeonatoTO.setModoCarreira(true);
			processaCampeonatoTOCarreira(campeonatoTO, carreiraDados);
		} else {
			campeonatoTO.setModoCarreira(false);
			processaCampeonatoTOPilotoSelecionado(campeonato, campeonatoTO);
		}
		Collections.sort(campeonatoTO.getCorridas(),
				new Comparator<CorridaCampeonatoTO>() {
					public int compare(CorridaCampeonatoTO arg0,
							CorridaCampeonatoTO arg1) {
						return arg0.getRodada().compareTo(arg1.getRodada());
					}
				});
		preencherContrutores(campeonato, campeonatoTO);
		return campeonatoTO;
	}

	public void processaCampeonatoTOPilotoSelecionado(CampeonatoSrv campeonato,
			CampeonatoTO campeonatoTO) {
		Map<String, TemporadasDefauts> tempDefsMap = carregadorRecursos
				.carregarTemporadasPilotosDefauts();
		TemporadasDefauts temporadasDefauts = tempDefsMap
				.get("t" + campeonato.getTemporada());
		List<Piloto> pilotos = temporadasDefauts.getPilotos();
		for (Iterator iterator = pilotos.iterator(); iterator.hasNext();) {
			Piloto piloto = (Piloto) iterator.next();
			if (String.valueOf(piloto.getId())
					.equals(campeonato.getIdPiloto())) {
				campeonatoTO.setIdPiloto(campeonato.getIdPiloto());
				campeonatoTO
						.setIdCarro(String.valueOf(piloto.getCarro().getId()));
				campeonatoTO.setCarroPiloto(piloto.getNomeCarro());
				campeonatoTO.setNomePiloto(piloto.getNome());
			}

		}
		campeonatoTO.setTemporadaCarro(campeonato.getTemporada());
		campeonatoTO.setTemporadaCapacete(campeonato.getTemporada());
	}

	public void processsaCorridaCampeonatoTO(CampeonatoSrv campeonato,
			CampeonatoTO campeonatoTO) {
		campeonatoTO.setCampeonato(campeonato);
		campeonatoTO.setUltimaCorrida(campeonato.getDataCriacao().getTime());
		List<CorridaCampeonatoSrv> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		int rodada = 1;
		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonatoSrv corridaCampeonato = (CorridaCampeonatoSrv) iterator
					.next();
			CorridaCampeonatoTO corridaCampeonatoTO = new CorridaCampeonatoTO();
			corridaCampeonatoTO.setRodada(corridaCampeonato.getRodada());
			corridaCampeonatoTO
					.setNomeCircuito(corridaCampeonato.getNomeCircuito());
			corridaCampeonatoTO.setArquivoCircuito(
					ControleRecursos.nomeCircuitoParaArquivoCircuito(
							corridaCampeonato.getNomeCircuito(), true));
			campeonatoTO.getCorridas().add(corridaCampeonatoTO);
			if (corridaCampeonato.getTempoFim() == null
					&& campeonatoTO.getArquivoCircuitoAtual() == null) {
				campeonatoTO.setNomeCircuitoAtual(
						corridaCampeonato.getNomeCircuito());
				campeonatoTO.setArquivoCircuitoAtual(
						corridaCampeonatoTO.getArquivoCircuito());
			}
			if (corridaCampeonato.getTempoFim() != null) {
				rodada++;
				campeonatoTO
						.setUltimaCorrida(corridaCampeonato.getTempoInicio());
				Dia dia = new Dia(corridaCampeonato.getTempoFim());
				corridaCampeonatoTO.setData(dia.toString());
				List<DadosCorridaCampeonatoSrv> dadosCorridaCampeonatos = corridaCampeonato
						.getDadosCorridaCampeonatos();
				for (Iterator iterator2 = dadosCorridaCampeonatos
						.iterator(); iterator2.hasNext();) {
					DadosCorridaCampeonatoSrv dadosCorridaCampeonato = (DadosCorridaCampeonatoSrv) iterator2
							.next();
					if (dadosCorridaCampeonato.getPosicao() == 1) {
						corridaCampeonatoTO.setVencedor(
								dadosCorridaCampeonato.getPiloto());
						corridaCampeonatoTO.setCorVencedor(
								dadosCorridaCampeonato.getCorPiloto());
					}
				}
			}
		}
		if (rodada > corridaCampeonatos.size()) {
			rodada = corridaCampeonatos.size();
		}
		campeonatoTO
				.setRodadaCampeonato(rodada + "/" + corridaCampeonatos.size());
	}

	public void processaCampeonatoTOCarreira(CampeonatoTO campeonatoTO,
			CarreiraDadosSrv carreiraDados) {
		campeonatoTO.setNomePiloto(carreiraDados.getNomePiloto());
		campeonatoTO.setTemporadaCarro(Util.rgb2hex(carreiraDados.geraCor1()));
		campeonatoTO.setIdCarro(Util.rgb2hex(carreiraDados.geraCor2()));

		campeonatoTO
				.setTemporadaCapacete(Util.rgb2hex(carreiraDados.geraCor1()));

		campeonatoTO.setIdPiloto(Util.rgb2hex(carreiraDados.geraCor2()));

		if (carreiraDados.getIdCapaceteLivery() != null
				&& carreiraDados.getTemporadaCapaceteLivery() != null) {
			campeonatoTO.setTemporadaCapacete(
					carreiraDados.getTemporadaCapaceteLivery().toString());
			campeonatoTO.setIdPiloto(
					carreiraDados.getIdCapaceteLivery().toString());
		}

		if (carreiraDados.getIdCarroLivery() != null
				&& carreiraDados.getTemporadaCarroLivery() != null) {
			campeonatoTO.setTemporadaCarro(
					carreiraDados.getTemporadaCarroLivery().toString());
			campeonatoTO
					.setIdCarro(carreiraDados.getIdCarroLivery().toString());
		}

		campeonatoTO.setCarroPiloto(carreiraDados.getNomeCarro());
	}

	public void preencherContrutores(CampeonatoSrv campeonatoSrv,
			CampeonatoTO campeonatoTO) {
		Map mapaCarros = new HashMap();
		Map mapaCarrosCor = new HashMap();
		Map mapaPilotos = new HashMap();
		Map mapaPilotosCor = new HashMap();
		Map mapaJogadores = new HashMap();
		Map mapaJogadoresCorridas = new HashMap();

		List<CorridaCampeonatoSrv> corridaCampeonatos = campeonatoSrv
				.getCorridaCampeonatos();
		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonatoSrv corridaCampeonatoSrv = (CorridaCampeonatoSrv) iterator
					.next();
			List<DadosCorridaCampeonatoSrv> dadosCorridaCampeonatos = corridaCampeonatoSrv
					.getDadosCorridaCampeonatos();
			for (Iterator iterator2 = dadosCorridaCampeonatos
					.iterator(); iterator2.hasNext();) {
				DadosCorridaCampeonatoSrv dadosCorridaCampeonatoSrv = (DadosCorridaCampeonatoSrv) iterator2
						.next();

				Integer ptsCarro = (Integer) mapaCarros
						.get(dadosCorridaCampeonatoSrv.getCarro());
				if (ptsCarro == null) {
					mapaCarros.put(dadosCorridaCampeonatoSrv.getCarro(),
							new Integer(dadosCorridaCampeonatoSrv.getPontos()));
				} else {
					mapaCarros
							.put(dadosCorridaCampeonatoSrv.getCarro(),
									new Integer(dadosCorridaCampeonatoSrv
											.getPontos()
											+ ptsCarro.intValue()));
				}
				mapaCarrosCor.put(dadosCorridaCampeonatoSrv.getCarro(),
						dadosCorridaCampeonatoSrv.getCorPiloto());

				Integer ptsPiloto = (Integer) mapaPilotos
						.get(dadosCorridaCampeonatoSrv.getPiloto());
				if (ptsPiloto == null) {
					mapaPilotos.put(dadosCorridaCampeonatoSrv.getPiloto(),
							new Integer(dadosCorridaCampeonatoSrv.getPontos()));
				} else {
					mapaPilotos
							.put(dadosCorridaCampeonatoSrv.getPiloto(),
									new Integer(dadosCorridaCampeonatoSrv
											.getPontos()
											+ ptsPiloto.intValue()));
				}
				mapaPilotosCor.put(dadosCorridaCampeonatoSrv.getPiloto(),
						dadosCorridaCampeonatoSrv.getCorPiloto());

				if (dadosCorridaCampeonatoSrv.getJogador() != null) {
					Integer ptsJogador = (Integer) mapaJogadores
							.get(dadosCorridaCampeonatoSrv.getJogador());
					if (ptsJogador == null) {
						mapaJogadores.put(
								dadosCorridaCampeonatoSrv.getJogador(),
								new Integer(
										dadosCorridaCampeonatoSrv.getPontos()));
					} else {
						mapaJogadores
								.put(dadosCorridaCampeonatoSrv.getJogador(),
										new Integer(dadosCorridaCampeonatoSrv
												.getPontos()
												+ ptsJogador.intValue()));
					}
					Integer corrida = (Integer) mapaJogadoresCorridas
							.get(dadosCorridaCampeonatoSrv.getJogador());
					if (corrida == null) {
						mapaJogadoresCorridas.put(
								dadosCorridaCampeonatoSrv.getJogador(),
								new Integer(1));
					} else {
						mapaJogadoresCorridas.put(
								dadosCorridaCampeonatoSrv.getJogador(),
								new Integer(corrida + 1));
					}
				}

			}
		}

		List<DadosClassificacaoCarros> listaCarros = new LinkedList<DadosClassificacaoCarros>();
		List<DadosClassificacaoPilotos> listaPilotos = new LinkedList<DadosClassificacaoPilotos>();
		List<DadosClassificacaoJogador> listaJogadores = new LinkedList<DadosClassificacaoJogador>();
		for (Iterator iterator = mapaCarros.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			DadosClassificacaoCarros dadosConstrutoresCarros = new DadosClassificacaoCarros();
			dadosConstrutoresCarros.setNome(key);
			dadosConstrutoresCarros.setPontos((Integer) mapaCarros.get(key));
			dadosConstrutoresCarros.setCor((String) mapaCarrosCor.get(key));
			if (dadosConstrutoresCarros.getPontos() > 0)
				listaCarros.add(dadosConstrutoresCarros);

		}
		for (Iterator iterator = mapaPilotos.keySet().iterator(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			DadosClassificacaoPilotos dadosConstrutoresPilotos = new DadosClassificacaoPilotos();
			dadosConstrutoresPilotos.setNome(key);
			dadosConstrutoresPilotos.setPontos((Integer) mapaPilotos.get(key));
			dadosConstrutoresPilotos.setCor((String) mapaPilotosCor.get(key));
			if (dadosConstrutoresPilotos.getPontos() > 0)
				listaPilotos.add(dadosConstrutoresPilotos);
		}

		Session session = controlePersistencia.getSession();
		try {
			for (Iterator iterator = mapaJogadores.keySet().iterator(); iterator
					.hasNext();) {
				Long key = (Long) iterator.next();
				DadosClassificacaoJogador dadosClassificacaoJogador = new DadosClassificacaoJogador();
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia
						.carregaDadosJogadorId(key, session);
				if (jogadorDadosSrv == null) {
					continue;
				}
				dadosClassificacaoJogador.setNome(jogadorDadosSrv.getNome());
				dadosClassificacaoJogador
						.setImagemJogador(jogadorDadosSrv.getImagemJogador());
				dadosClassificacaoJogador
						.setPontos((Integer) mapaJogadores.get(key));
				dadosClassificacaoJogador
						.setCorridas((Integer) mapaJogadoresCorridas.get(key));
				if (dadosClassificacaoJogador.getPontos() > 0) {
					listaJogadores.add(dadosClassificacaoJogador);
				}
			}
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		campeonatoTO.setCarros(listaCarros);
		campeonatoTO.setPilotos(listaPilotos);
		campeonatoTO.setJogadores(listaJogadores);

		Collections.sort(listaCarros,
				new Comparator<DadosClassificacaoCarros>() {
					public int compare(DadosClassificacaoCarros arg0,
							DadosClassificacaoCarros arg1) {
						return new Integer(arg1.getPontos())
								.compareTo(arg0.getPontos());
					}
				});

		Collections.sort(listaPilotos,
				new Comparator<DadosClassificacaoPilotos>() {
					public int compare(DadosClassificacaoPilotos arg0,
							DadosClassificacaoPilotos arg1) {
						return new Integer(arg1.getPontos())
								.compareTo(arg0.getPontos());
					}
				});

		Collections.sort(listaJogadores,
				new Comparator<DadosClassificacaoJogador>() {
					public int compare(DadosClassificacaoJogador arg0,
							DadosClassificacaoJogador arg1) {
						return new Integer(arg1.getPontos())
								.compareTo(arg0.getPontos());
					}
				});
	}

	public Object finalizaCampeonato(CampeonatoTO campeonato, String token) {
		Session session = controlePersistencia.getSession();
		try {
			List pesquisaCampeonatosEmAberto = controlePersistencia
					.pesquisaCampeonatosEmAberto(token, session, false);
			if (pesquisaCampeonatosEmAberto.isEmpty()) {
				return null;
			}
			CampeonatoSrv campeonatoSrv = (CampeonatoSrv) pesquisaCampeonatosEmAberto
					.get(0);
			if (!campeonatoSrv.getId().equals(campeonato.getId())) {
				return null;
			}
			campeonatoSrv.setFinalizado(true);
			controlePersistencia.gravarDados(session, campeonatoSrv);
			return campeonato;
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}
}
