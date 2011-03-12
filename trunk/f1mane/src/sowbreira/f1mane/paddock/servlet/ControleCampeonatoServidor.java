package sowbreira.f1mane.paddock.servlet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import br.nnpe.Logger;
import br.nnpe.Util;

import sowbreira.f1mane.entidades.Piloto;
import sowbreira.f1mane.entidades.Volta;
import sowbreira.f1mane.paddock.entidades.TOs.ClientPaddockPack;
import sowbreira.f1mane.paddock.entidades.TOs.DadosCriarJogo;
import sowbreira.f1mane.paddock.entidades.TOs.ErroServ;
import sowbreira.f1mane.paddock.entidades.TOs.MsgSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.Campeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CarreiraDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.CorridasDadosSrv;
import sowbreira.f1mane.paddock.entidades.persistencia.DadosCorridaCampeonato;
import sowbreira.f1mane.paddock.entidades.persistencia.JogadorDadosSrv;
import sowbreira.f1mane.recursos.idiomas.Lang;

public class ControleCampeonatoServidor {

	private ControlePersistencia controlePersistencia;

	public ControleCampeonatoServidor(ControlePersistencia controlePersistencia) {
		super();
		this.controlePersistencia = controlePersistencia;
	}

	public Object criarCampeonato(ClientPaddockPack clientPaddockPack) {
		if (clientPaddockPack.getSessaoCliente() == null) {
			return (new MsgSrv(Lang.msg("210")));
		}
		Session session = controlePersistencia.getSession();
		try {

			JogadorDadosSrv jogadorDadosSrv = controlePersistencia
					.carregaDadosJogador(clientPaddockPack.getSessaoCliente()
							.getNomeJogador(), session);
			if (jogadorDadosSrv == null) {
				return (new MsgSrv(Lang.msg("238")));
			}
			Campeonato campeonato = (Campeonato) clientPaddockPack
					.getDataObject();
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
		List campeonatos = controlePersistencia.pesquisaCampeonatos(
				jogadorDadosSrv, session);
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
		Campeonato campeonatoBanco = controlePersistencia.pesquisaCampeonato(
				session, nome, false);
		return campeonatoBanco != null;
	}

	public Object listarCampeonatos() {
		Session session = controlePersistencia.getSession();
		try {
			List<Campeonato> campeonatos = controlePersistencia
					.obterListaCampeonatos(session);
			List retorno = new ArrayList();
			for (Iterator iterator = campeonatos.iterator(); iterator.hasNext();) {
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
		String campString = (String) clientPaddockPack.getDataObject();
		Session session = controlePersistencia.getSession();
		try {
			Campeonato campeonato = controlePersistencia.pesquisaCampeonato(
					session, campString, true);
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
		String campString = dadosCriarJogo.getNomeCampeonato();
		Session session = controlePersistencia.getSession();
		try {

			Campeonato campeonato = controlePersistencia.pesquisaCampeonato(
					session, campString, false);
			if (campeonato == null) {
				Logger.logar("campeonato nulo");
				return;
			}
			CorridaCampeonato corridaCampeonatoCorrente = null;
			for (CorridaCampeonato corridaCampeonato : campeonato
					.getCorridaCampeonatos()) {
				if (dadosCriarJogo.getCircuitoSelecionado().equals(
						corridaCampeonato.getNomeCircuito())) {
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
						.carregaDadosJogador(piloto.getNomeJogador(), session);
				if (jogadorDadosSrv != null) {
					dadosCorridaCampeonato
							.setJogador(jogadorDadosSrv.getNome());
				}
				dadosCorridaCampeonato.setPiloto(piloto.getNome());
				dadosCorridaCampeonato.setCarro(piloto.getNomeCarro());
				int pts = controleClassificacao.gerarPontos(piloto);
				dadosCorridaCampeonato.setPontos(pts);
				dadosCorridaCampeonato.setPosicao(piloto.getPosicao());
				dadosCorridaCampeonato.setTpPneu(piloto.getCarro()
						.getTipoPneu());
				dadosCorridaCampeonato.setNumVoltas(piloto.getNumeroVolta());
				Volta volta = piloto.obterVoltaMaisRapida();
				if (volta != null)
					dadosCorridaCampeonato.setVoltaMaisRapida(volta
							.obterTempoVoltaFormatado());
				dadosCorridaCampeonato.setQtdeParadasBox(piloto
						.getQtdeParadasBox());
				dadosCorridaCampeonato.setDesgastePneus(String.valueOf(piloto
						.getCarro().porcentagemDesgastePeneus() + "%"));
				dadosCorridaCampeonato.setCombustivelRestante(String
						.valueOf(piloto.getCarro().porcentagemCombustivel()
								+ "%"));
				dadosCorridaCampeonato.setDesgasteMotor(String.valueOf(piloto
						.getCarro().porcentagemDesgasteMotor() + "%"));
				corridaCampeonatoCorrente.getDadosCorridaCampeonatos().add(
						dadosCorridaCampeonato);
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
}
