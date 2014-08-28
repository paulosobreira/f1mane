package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.List;

import br.nnpe.Constantes;

import sowbreira.f1mane.entidades.Clima;

/**
 * @author Paulo Sobreira Criado em 12/08/2007 as 17:04:24
 */
public class DadosCriarJogo implements Serializable {

	private static final long serialVersionUID = 8923188552953563909L;

	private Integer diffultrapassagem = null;
	private String nomeCampeonato;
	private Integer habilidade = null;
	private Integer potencia = null;
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
	private Clima clima = null;
	private Integer qtdeVoltas = null;
	private String nivelCorrida = null;
	private List pilotosCarreira;
	private boolean semReabastecimento;
	private boolean semTrocaPeneu;
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

	public boolean isSemReabastecimento() {
		return semReabastecimento;
	}

	public void setSemReabastecimento(boolean semReabastecimento) {
		this.semReabastecimento = semReabastecimento;
	}

	public boolean isSemTrocaPeneu() {
		return semTrocaPeneu;
	}

	public void setSemTrocaPeneu(boolean semTrocaPeneu) {
		this.semTrocaPeneu = semTrocaPeneu;
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

	public Clima getClima() {
		return clima;
	}

	public void setClima(Clima clima) {
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

	public Integer getHabilidade() {
		return habilidade;
	}

	public void setHabilidade(Integer habilidade) {
		this.habilidade = habilidade;
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

	public Integer getPotencia() {
		return potencia;
	}

	public void setPotencia(Integer potencia) {
		this.potencia = potencia;
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
