package sowbreira.f1mane.paddock.servlet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import br.nnpe.Logger;

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
		JogadorDadosSrv jogadorDadosSrv = controlePersistencia
				.carregaDadosJogador(clientPaddockPack.getSessaoCliente()
						.getNomeJogador());
		if (jogadorDadosSrv == null) {
			return (new MsgSrv(Lang.msg("238")));
		}
		Campeonato campeonato = (Campeonato) clientPaddockPack.getDataObject();
		campeonato.setJogadorDadosSrv(jogadorDadosSrv);
		try {
			controlePersistencia.gravarDados(campeonato);
		} catch (Exception e) {
			return new ErroServ(e);
		}
		return (new MsgSrv(Lang.msg("campeonatoCriado")));
	}

	public Object listarCampeonatos() {
		List<Campeonato> campeonatos = controlePersistencia
				.obterListaCampeonatos();
		String[] ret = new String[campeonatos.size()];
		for (int i = 0; i < ret.length; i++) {
			ret[i] = campeonatos.get(i).getNome();
		}
		return ret;
	}

	public Object obterCampeonato(ClientPaddockPack clientPaddockPack) {
		String campString = (String) clientPaddockPack.getDataObject();

		Campeonato campeonato = controlePersistencia.pesquisaCampeonato(
				campString, true);

		return campeonato;
	}

	public void processaCorrida(long tempoInicio, long tempoFim,
			Map mapVoltasJogadoresOnline, List pilotos,
			DadosCriarJogo dadosCriarJogo,
			ControleClassificacao controleClassificacao) {
		String campString = dadosCriarJogo.getNomeCampeonato();
		Campeonato campeonato = controlePersistencia.pesquisaCampeonato(
				campString, false);
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
					.carregaDadosJogador(piloto.getNomeJogador());
			if (jogadorDadosSrv != null) {
				dadosCorridaCampeonato.setJogador(jogadorDadosSrv.getNome());
			}
			dadosCorridaCampeonato.setPiloto(piloto.getNome());
			dadosCorridaCampeonato.setCarro(piloto.getNomeCarro());
			int pts = controleClassificacao.gerarPontos(piloto);
			dadosCorridaCampeonato.setPontos(pts);
			dadosCorridaCampeonato.setPosicao(piloto.getPosicao());
			dadosCorridaCampeonato.setTpPneu(piloto.getCarro().getTipoPneu());
			dadosCorridaCampeonato.setNumVoltas(piloto.getNumeroVolta());
			Volta volta = piloto.obterVoltaMaisRapida();
			if (volta != null)
				dadosCorridaCampeonato.setVoltaMaisRapida(volta
						.obterTempoVoltaFormatado());
			dadosCorridaCampeonato
					.setQtdeParadasBox(piloto.getQtdeParadasBox());
			dadosCorridaCampeonato.setDesgastePneus(String.valueOf(piloto
					.getCarro().porcentagemDesgastePeneus()
					+ "%"));
			dadosCorridaCampeonato.setCombustivelRestante(String.valueOf(piloto
					.getCarro().porcentagemCombustivel()
					+ "%"));
			dadosCorridaCampeonato.setDesgasteMotor(String.valueOf(piloto
					.getCarro().porcentagemDesgasteMotor()
					+ "%"));
			corridaCampeonatoCorrente.getDadosCorridaCampeonatos().add(
					dadosCorridaCampeonato);
			try {
				controlePersistencia.gravarDados(dadosCorridaCampeonato,
						corridaCampeonatoCorrente);
			} catch (Exception e) {
				Logger.topExecpts(e);
			}
		}
		try {
			controlePersistencia.gravarDados(campeonato);
		} catch (Exception e) {
			Logger.topExecpts(e);
		}
	}
}
