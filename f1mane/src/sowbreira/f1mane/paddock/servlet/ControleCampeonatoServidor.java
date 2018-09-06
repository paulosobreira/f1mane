package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.DadosCorridaCampeonato;
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
	}

	public Object criarCampeonato(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack.getSessaoCliente() == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		Campeonato campeonato = (Campeonato) clientPaddockPack.getDataObject();
		return criarCampeonato(campeonato,
				clientPaddockPack.getSessaoCliente().getToken());
	}

	public Object criarCampeonato(Campeonato campeonato, String token) {
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
			List<CorridaCampeonato> corridaCampeonatos = campeonato
					.getCorridaCampeonatos();
			long rodada = 1;
			for (Iterator iterator = corridaCampeonatos.iterator(); iterator
					.hasNext();) {
				CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
						.next();
				corridaCampeonato.setRodada(rodada);
				corridaCampeonato.setCampeonato(campeonato);
				rodada++;

			}
			controlePersistencia.gravarDados(session, campeonato);
			return (new MsgSrv(Lang.msg("campeonatoCriado")));
		} catch (Exception e) {
			Logger.logarExept(e);
			return new ErroServ(e);
		} finally {
			if (session.isOpen())
				session.close();
		}
	}

	private boolean verificaCampeonatoEmAberto(JogadorDadosSrv jogadorDadosSrv,
			Session session) {
		List campeonatos = controlePersistencia
				.pesquisaCampeonatos(jogadorDadosSrv, session);
		for (Iterator iterator = campeonatos.iterator(); iterator.hasNext();) {
			Campeonato campeonato = (Campeonato) iterator.next();
			List<CorridaCampeonato> corridaCampeonatos = campeonato
					.getCorridaCampeonatos();
			for (CorridaCampeonato corridaCampeonato : corridaCampeonatos) {
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
			List<Campeonato> campeonatos = controlePersistencia
					.obterListaCampeonatos(session);
			List retorno = new ArrayList();
			for (Iterator iterator = campeonatos.iterator(); iterator
					.hasNext();) {
				Campeonato campeonato = (Campeonato) iterator.next();
				Object[] row = new Object[4];
				row[0] = campeonato.getNome();
				row[1] = campeonato.getJogadorDadosSrv().getNome();
				row[2] = verificaCampeonatoConcluido(campeonato);
				row[3] = campeonato.getDataCriacao();
				retorno.add(row);
			}
			return retorno;
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
	}

	public boolean verificaCampeonatoConcluido(Campeonato campeonato) {
		List<CorridaCampeonato> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (CorridaCampeonato corridaCampeonato : corridaCampeonatos) {
			if (corridaCampeonato.getTempoFim() == null) {
				return false;
			}
		}
		return true;
	}

	public Object obterCampeonato(ClientPaddockPack clientPaddockPack) {
		String idCampeonato = (String) clientPaddockPack.getDataObject();
		return obterCampeonato(idCampeonato);
	}

	public Object obterCampeonato(String idCampeonato) {
		Session session = controlePersistencia.getSession();
		try {
			Campeonato campeonato = controlePersistencia
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
			Campeonato campeonato = controlePersistencia
					.pesquisaCampeonato(session, idCampeonato, false);
			if (campeonato == null) {
				return;
			}
			CorridaCampeonato corridaCampeonatoCorrente = null;
			String circuitoSelecionado = Util
					.substVogais(dadosCriarJogo.getCircuitoSelecionado());
			for (CorridaCampeonato corridaCampeonato : campeonato
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
				DadosCorridaCampeonato dadosCorridaCampeonato = new DadosCorridaCampeonato();
				dadosCorridaCampeonato
						.setCorridaCampeonato(corridaCampeonatoCorrente);
				JogadorDadosSrv jogadorDadosSrv = controlePersistencia
						.carregaDadosJogador(piloto.getTokenJogador(), session);
				if (jogadorDadosSrv != null) {
					dadosCorridaCampeonato
							.setJogador(jogadorDadosSrv.getNome());
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

	public Campeonato obterCampeonatoEmAberto(String token) {
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
		Campeonato campeonato = (Campeonato) pesquisaCampeonatos.get(0);
		List<CorridaCampeonato> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();
		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
					.next();
			corridaCampeonato.setCampeonato(null);
		}
		return campeonato;
	}

	public CampeonatoTO obterCampeonatoEmAbertoTO(String token) {
		Campeonato campeonato = obterCampeonatoEmAberto(token);
		if (campeonato == null) {
			return null;
		}
		CampeonatoTO campeonatoTO = new CampeonatoTO();
		campeonatoTO.setCampeonato(campeonato);

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
		return campeonatoTO;
	}

	public void processaCampeonatoTOPilotoSelecionado(Campeonato campeonato,
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

	public void processsaCorridaCampeonatoTO(Campeonato campeonato,
			CampeonatoTO campeonatoTO) {
		List<CorridaCampeonato> corridaCampeonatos = campeonato
				.getCorridaCampeonatos();

		for (Iterator iterator = corridaCampeonatos.iterator(); iterator
				.hasNext();) {
			CorridaCampeonato corridaCampeonato = (CorridaCampeonato) iterator
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
				Dia dia = new Dia(corridaCampeonato.getTempoFim());
				corridaCampeonatoTO.setData(dia.toString());
				List<DadosCorridaCampeonato> dadosCorridaCampeonatos = corridaCampeonato
						.getDadosCorridaCampeonatos();
				for (Iterator iterator2 = dadosCorridaCampeonatos
						.iterator(); iterator2.hasNext();) {
					DadosCorridaCampeonato dadosCorridaCampeonato = (DadosCorridaCampeonato) iterator2
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

}
