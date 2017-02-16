package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.List;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import br.nnpe.Constantes;

/**
 * @author Paulo Sobreira Criado em 12/08/2007 as 17:04:24
 */
@JsonSerialize(include= JsonSerialize.Inclusion.NON_NULL)
public class DadosCriarJogo implements Serializable {

	private static final long serialVersionUID = 8923188552953563909L;

	private Integer diffultrapassagem = null;
	private String nomeCampeonato;
	private Integer tempoQualificacao = null;
	private String asa;
	private String temporada;
	private String tpPnueu;
	private String piloto = "";
	private Integer combustivel;
	private String senha;
	private String nomeJogo;
	private String circuitoSelecionado = null;
	private Integer tempoCiclo = Constantes.CICLO;
	private String clima = null;
	private Integer qtdeVoltas = null;
	private String nivelCorrida = null;
	private List pilotosCarreira;
	private boolean reabastecimento;
	private boolean trocaPneu;
	private boolean kers;
	private boolean drs;

	public String getNomeCampeonato() {
		return nomeCampeonato;
	}

	public void setNomeCampeonato(String nomeCampeonato) {
		this.nomeCampeonato = nomeCampeonato;
	}

	public boolean isKers() {
		return kers;
	}

	public void setKers(boolean kers) {
		this.kers = kers;
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

	public String getNivelCorrida() {
		return nivelCorrida;
	}

	public void setNivelCorrida(String nivelCorrida) {
		this.nivelCorrida = nivelCorrida;
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
		return tempoCiclo;
	}

	public Integer getTempoQualificacao() {
		return tempoQualificacao;
	}

	public void setTempoQualificacao(Integer tempoQualificacao) {
		this.tempoQualificacao = tempoQualificacao;
	}

	public String getTpPnueu() {
		return tpPnueu;
	}

	public void setTpPnueu(String tpPnueu) {
		this.tpPnueu = tpPnueu;
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

}
