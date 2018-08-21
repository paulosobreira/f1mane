package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;
import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.DadosCorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class ControleCampeonatoServidor {

	private ControlePersistencia controlePersistencia;

	public ControleCampeonatoServidor(
			ControlePersistencia controlePersistencia) {
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
		if(Util.isNullOrEmpty(campeonato.getNome())){
			return (new MsgSrv(Lang.msg("nomeCampeonatoObrigatorio")));
		}
		if(campeonato.getIdPiloto()==null){
			return (new MsgSrv(Lang.msg("selecionePiloto")));
		}
		if(campeonato.getCorridaCampeonatos().size()<5){
			return (new MsgSrv(Lang.msg("min5CorridasCampeonato")));
		}	
		Session session = controlePersistencia.getSession();
		try {

			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(token, session);
			if (jogadorDadosSrv == null) {
				return (new MsgSrv(Lang.msg("238")));
			}
			if (verifircaNomeCampeonato(campeonato, session)) {
				return (new MsgSrv(Lang.msg("nomeCampeonatoNaoDisponivel")));
			}
			if (verificaCampeonatoEmAberto(jogadorDadosSrv, session)) {
				return (new MsgSrv(Lang.msg("jogadorTemCampeonatoEmAberto")));
			}
			campeonato.setJogadorDadosSrv(jogadorDadosSrv);
			try {
				controlePersistencia.gravarDados(session, campeonato);
			} catch (Exception e) {
				return new ErroServ(e);
			}
			return (new MsgSrv(Lang.msg("campeonatoCriado")));
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

	private boolean verifircaNomeCampeonato(Campeonato campeonato,
			Session session) {
		String nome = campeonato.getNome();
		if (Util.isNullOrEmpty(nome)) {
			return true;
		}
		Campeonato campeonatoBanco = controlePersistencia
				.pesquisaCampeonato(session, nome, false);
		return campeonatoBanco != null;
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
		String campString = dadosCriarJogo.getNomeCampeonato();
		Session session = controlePersistencia.getSession();
		try {

			Campeonato campeonato = controlePersistencia
					.pesquisaCampeonato(session, campString, false);
			if (campeonato == null) {
				Logger.logar("campeonato nulo");
				return;
			}
			CorridaCampeonato corridaCampeonatoCorrente = null;
			for (CorridaCampeonato corridaCampeonato : campeonato
					.getCorridaCampeonatos()) {
				if (dadosCriarJogo.getCircuitoSelecionado()
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
/*
	public Object obterCampeonatoEmAberto(String token) {
		List pesquisaCampeonatos = null;
		Session session = controlePersistencia.getSession();
		try {
			pesquisaCampeonatos = controlePersistencia
					.pesquisaCampeonatos(token, session, true);
		} finally {
			if (session.isOpen()) {
				session.close();
			}
		}
		if (pesquisaCampeonatos == null) {
			return null;
		}
		for (Iterator iterator = pesquisaCampeonatos.iterator(); iterator
				.hasNext();) {
			Campeonato campeonato = (Campeonato) iterator.next();
			if (verificaCampeonatoConcluido(campeonato)) {
				continue;
			} else {
				return campeonato;
			}
		}
		return null;
	}
*/
	public Object obterCampeonatoEmAberto(String token) {
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
		return pesquisaCampeonatos.get(0);
	}
}
