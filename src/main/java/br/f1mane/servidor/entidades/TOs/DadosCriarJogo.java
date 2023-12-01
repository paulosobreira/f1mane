package br.f1mane.servidor.entidades.TOs;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import br.nnpe.Constantes;
import br.nnpe.Logger;
import br.nnpe.Util;
import br.f1mane.controles.ControleJogoLocal;
import br.f1mane.controles.ControleRecursos;
import br.f1mane.entidades.Circuito;
import br.f1mane.entidades.Clima;
import br.f1mane.entidades.TemporadasDefault;
import br.f1mane.recursos.CarregadorRecursos;

/**
 * @author Paulo Sobreira Criado em 12/08/2007 as 17:04:24
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DadosCriarJogo implements Serializable {

	private static final long serialVersionUID = 8923188552953563909L;

	private Integer diffultrapassagem = null;
	private String nomeCampeonato;
	private String rodadaCampeonato;
	private Long idCampeonato;
	private Integer tempoQualificacao = null;
	private String asa;
	private String temporada;
	private String tpPneu;
	private String piloto = "";
	private int idPiloto = 0;
	private Integer combustivel;
	private String senha;
	private String nomeJogo;
	private String circuitoSelecionado = null;
	private String clima = null;
	private Integer qtdeVoltas = null;
	private String nivelJogo = null;
	private List pilotosCarreira;
	private boolean reabastecimento;
	private boolean trocaPneu;
	private boolean ers;
	private boolean drs;
	private boolean safetyCar = true;

	public static DadosCriarJogo gerarJogoLetsRace(String temporada,
												   String arquivoCircuito, String idPiloto, String numVoltas,
												   String tipoPneu, String combustivel, String asa)
			throws ClassNotFoundException, IOException {
		Logger.logar("gerarJogoLetsRace temporada " + temporada);
		Logger.logar("gerarJogoLetsRace arquivoCircuito " + arquivoCircuito);
		Logger.logar("gerarJogoLetsRace idPiloto " + idPiloto);
		Logger.logar("gerarJogoLetsRace numVoltas " + numVoltas);
		Logger.logar("gerarJogoLetsRace tipoPneu " + tipoPneu);
		Logger.logar("gerarJogoLetsRace combustivel " + combustivel);
		Logger.logar("gerarJogoLetsRace asa " + asa);

		DadosCriarJogo dadosCriarJogo = new DadosCriarJogo();
		dadosCriarJogo.setTemporada("t" + temporada);
		if (!Util.isNullOrEmpty(numVoltas)) {
			dadosCriarJogo
					.setQtdeVoltas(new Integer(Util.extrairNumeros(numVoltas)));
		}
		dadosCriarJogo.setQtdeVoltas(Integer.valueOf(Constantes.MIN_VOLTAS));
		dadosCriarJogo.setDiffultrapassagem(Integer.valueOf(Util.intervalo(50, 500)));

		String pista = ControleRecursos
				.nomeArquivoCircuitoParaPista(arquivoCircuito);

		// pista = "Monte Carlo";
		// dadosCriarJogo.setSafetyCar(false);
		dadosCriarJogo.setCircuitoSelecionado(pista);
		dadosCriarJogo.setNivelJogo(ControleJogoLocal.NORMAL);
		dadosCriarJogo.setAsa(asa);
		dadosCriarJogo.setTpPneu(tipoPneu);
		Circuito circuitoObj = CarregadorRecursos
				.carregarCircuito(arquivoCircuito);
		if (Math.random() < (circuitoObj.getProbalidadeChuva() / 100.0)) {
			dadosCriarJogo.setClima(Clima.NUBLADO);
		} else {
			dadosCriarJogo.setClima(Clima.SOL);
		}
		TemporadasDefault temporadasDefault = CarregadorRecursos
				.getCarregadorRecursos(false).carregarTemporadasPilotosDefauts()
				.get("t" + temporada);

		if (!Util.isNullOrEmpty(combustivel)) {
			int fuel = new Integer(Util.extrairNumeros(combustivel)).intValue();
			if (fuel > 100) {
				fuel = 100;
			}
			if (fuel < 10) {
				fuel = 10;
			}
			dadosCriarJogo.setCombustivel(Integer.valueOf(fuel));
		} else {
			if (temporadasDefault.getReabastecimento().booleanValue()) {
				dadosCriarJogo.setCombustivel(Integer.valueOf(Util.intervalo(25, 50)));
			} else {
				dadosCriarJogo.setCombustivel(Integer.valueOf(Util.intervalo(70, 90)));
			}
		}
		dadosCriarJogo
				.setReabastecimento(temporadasDefault.getReabastecimento().booleanValue());
		dadosCriarJogo.setTrocaPneu(temporadasDefault.getTrocaPneu().booleanValue());
		dadosCriarJogo.setErs(temporadasDefault.getErs().booleanValue());
		dadosCriarJogo.setDrs(temporadasDefault.getDrs().booleanValue());
		dadosCriarJogo.setIdPiloto(new Integer(idPiloto).intValue());
		dadosCriarJogo.setSafetyCar(temporadasDefault.getSafetyCar().booleanValue());
		return dadosCriarJogo;
	}

	public String getNomeCampeonato() {
		return nomeCampeonato;
	}

	public void setNomeCampeonato(String nomeCampeonato) {
		this.nomeCampeonato = nomeCampeonato;
	}

	public boolean isErs() {
		return ers;
	}

	public void setErs(boolean ers) {
		this.ers = ers;
	}

	public boolean isDrs() {
		return drs;
	}

	public void setDrs(boolean drs) {
		this.drs = drs;
	}

	public boolean isReabastecimento() {
		return reabastecimento;
	}

	public void setReabastecimento(boolean reabastecimento) {
		this.reabastecimento = reabastecimento;
	}

	public boolean isTrocaPneu() {
		return trocaPneu;
	}

	public void setTrocaPneu(boolean trocaPneu) {
		this.trocaPneu = trocaPneu;
	}

	public List getPilotosCarreira() {
		return pilotosCarreira;
	}

	public void setPilotosCarreira(List pilotosCarreira) {
		this.pilotosCarreira = pilotosCarreira;
	}

	public String getCircuitoSelecionado() {
		return circuitoSelecionado;
	}

	public void setCircuitoSelecionado(String circuitoSelecionado) {
		this.circuitoSelecionado = circuitoSelecionado;
	}

	public String getClima() {
		return clima;
	}

	public void setClima(String clima) {
		this.clima = clima;
	}

	public Integer getCombustivel() {
		return combustivel;
	}

	public void setCombustivel(Integer combustivel) {
		this.combustivel = combustivel;
	}

	public Integer getDiffultrapassagem() {
		return diffultrapassagem;
	}

	public void setDiffultrapassagem(Integer diffultrapassagem) {
		this.diffultrapassagem = diffultrapassagem;
	}

	public String getNivelJogo() {
		return nivelJogo;
	}

	public void setNivelJogo(String nivelJogo) {
		this.nivelJogo = nivelJogo;
	}

	public String getNomeJogo() {
		return nomeJogo;
	}

	public void setNomeJogo(String nomeJogo) {
		this.nomeJogo = nomeJogo;
	}

	public String getPiloto() {
		return piloto;
	}

	public void setPiloto(String piloto) {
		this.piloto = piloto;
	}

	public Integer getQtdeVoltas() {
		return qtdeVoltas;
	}

	public void setQtdeVoltas(Integer qtdeVoltas) {
		this.qtdeVoltas = qtdeVoltas;
	}

	public String getSenha() {
		return senha;
	}

	public void setSenha(String senha) {
		this.senha = senha;
	}

	public Integer getTempoCiclo() {
		Integer tempoCiclo = Integer.valueOf(Constantes.CICLO);
		return tempoCiclo;
	}

	public Integer getTempoQualificacao() {
		return tempoQualificacao;
	}

	public void setTempoQualificacao(Integer tempoQualificacao) {
		this.tempoQualificacao = tempoQualificacao;
	}

	public String getTpPneu() {
		return tpPneu;
	}

	public void setTpPneu(String tpPneu) {
		this.tpPneu = tpPneu;
	}

	public String getAsa() {
		return asa;
	}

	public void setAsa(String asa) {
		this.asa = asa;
	}

	public String getTemporada() {
		return temporada;
	}

	public void setTemporada(String temporada) {
		this.temporada = temporada;
	}

	public int getIdPiloto() {
		return idPiloto;
	}

	public void setIdPiloto(int idPiloto) {
		this.idPiloto = idPiloto;
	}

	public boolean isSafetyCar() {
		return safetyCar;
	}

	public void setSafetyCar(boolean safetyCar) {
		this.safetyCar = safetyCar;
	}

	public Long getIdCampeonato() {
		return idCampeonato;
	}

	public void setIdCampeonato(Long idCampeonato) {
		this.idCampeonato = idCampeonato;
	}

	public String getRodadaCampeonato() {
		return rodadaCampeonato;
	}

	public void setRodadaCampeonato(String rodadaCampeonato) {
		this.rodadaCampeonato = rodadaCampeonato;
	}

}
