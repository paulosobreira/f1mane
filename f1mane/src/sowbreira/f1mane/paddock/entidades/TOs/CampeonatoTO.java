package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import sowbreira.f1mane.paddock.entidades.persistencia.CampeonatoSrv;
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CampeonatoTO implements Serializable {

	private static final long serialVersionUID = 1L;

	private String nomeCircuitoAtual;

	private String arquivoCircuitoAtual;

	private transient String nomePiloto;

	private transient String carroPiloto;

	private transient String temporadaCarro;

	private transient String temporadaCapacete;

	private transient String idCarro;

	private transient String idPiloto;
	
	private transient Long ultimaCorrida;

	private boolean modoCarreira;

	private CampeonatoSrv campeonato = new CampeonatoSrv();

	private String rodadaCampeonato;

	private List<CorridaCampeonatoTO> corridas = new ArrayList<CorridaCampeonatoTO>();

	private List<DadosClassificacaoCarros> carros = new ArrayList<DadosClassificacaoCarros>();

	private List<DadosClassificacaoJogador> jogadores = new ArrayList<DadosClassificacaoJogador>();

	private List<DadosClassificacaoPilotos> pilotos = new ArrayList<DadosClassificacaoPilotos>();

	public String getNomePiloto() {
		return nomePiloto;
	}

	public void setNomePiloto(String nomePiloto) {
		this.nomePiloto = nomePiloto;
	}

	public String getCarroPiloto() {
		return carroPiloto;
	}

	public void setCarroPiloto(String carroPiloto) {
		this.carroPiloto = carroPiloto;
	}

	public String getTemporadaCarro() {
		return temporadaCarro;
	}

	public void setTemporadaCarro(String temporadaCarro) {
		this.temporadaCarro = temporadaCarro;
	}

	public String getTemporadaCapacete() {
		return temporadaCapacete;
	}

	public void setTemporadaCapacete(String temporadaCapacete) {
		this.temporadaCapacete = temporadaCapacete;
	}

	public String getIdCarro() {
		return idCarro;
	}

	public void setIdCarro(String idCarro) {
		this.idCarro = idCarro;
	}

	public void setCampeonato(CampeonatoSrv campeonato) {
		this.campeonato = campeonato;
	}

	public String getIdPiloto() {
		return idPiloto;
	}

	public void setIdPiloto(String idPiloto) {
		this.idPiloto = idPiloto;
	}

	public String getTemporada() {
		return campeonato.getTemporada();
	}

	public String getNome() {
		return campeonato.getNome();
	}

	public boolean isDrs() {
		return campeonato.isDrs();
	}

	public boolean isReabastecimento() {
		return campeonato.isReabastecimento();
	}

	public boolean isTrocaPneus() {
		return campeonato.isTrocaPneus();
	}

	public Integer getQtdeVoltas() {
		return campeonato.getQtdeVoltas();
	}

	public boolean isErs() {
		return campeonato.isErs();
	}

	public List<CorridaCampeonatoTO> getCorridas() {
		return corridas;
	}

	public String getNomeCircuitoAtual() {
		return nomeCircuitoAtual;
	}

	public void setNomeCircuitoAtual(String nomeCircuitoAtual) {
		this.nomeCircuitoAtual = nomeCircuitoAtual;
	}

	public String getArquivoCircuitoAtual() {
		return arquivoCircuitoAtual;
	}

	public void setArquivoCircuitoAtual(String arquivoCircuitoAtual) {
		this.arquivoCircuitoAtual = arquivoCircuitoAtual;
	}

	public boolean isModoCarreira() {
		return modoCarreira;
	}

	public void setModoCarreira(boolean modoCarreira) {
		this.modoCarreira = modoCarreira;
	}

	public Long getId() {
		return campeonato.getId();
	}

	public List<DadosClassificacaoCarros> getCarros() {
		return carros;
	}

	public void setCarros(List<DadosClassificacaoCarros> carros) {
		this.carros = carros;
	}

	public List<DadosClassificacaoJogador> getJogadores() {
		return jogadores;
	}

	public void setJogadores(List<DadosClassificacaoJogador> jogadores) {
		this.jogadores = jogadores;
	}

	public List<DadosClassificacaoPilotos> getPilotos() {
		return pilotos;
	}

	public void setPilotos(List<DadosClassificacaoPilotos> pilotos) {
		this.pilotos = pilotos;
	}

	public String getRodadaCampeonato() {
		return rodadaCampeonato;
	}

	public void setRodadaCampeonato(String rodadaCampeonato) {
		this.rodadaCampeonato = rodadaCampeonato;
	}

	public void setId(int id) {
		campeonato.setId(id);
	}

	public void limpaListas() {
		corridas.clear();
		pilotos.clear();
		carros.clear();
		jogadores.clear();
	}

	public Long getUltimaCorrida() {
		return ultimaCorrida;
	}

	public void setUltimaCorrida(Long ultimaCorrida) {
		this.ultimaCorrida = ultimaCorrida;
	}

}
