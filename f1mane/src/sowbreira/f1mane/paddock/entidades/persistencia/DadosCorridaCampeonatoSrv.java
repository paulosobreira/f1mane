package sowbreira.f1mane.paddock.entidades.persistencia;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:47:15
 */
@Entity(name = "f1_dadoscorridacampeonatosrv")
public class DadosCorridaCampeonatoSrv extends F1ManeDados {

	private int posicao;
	private String piloto;
	private String corPiloto;
	private Long jogador;
	private String carro;
	private String tpPneu;
	private String voltaMaisRapida;
	private int qtdeParadasBox;
	private String desgastePneus;
	private String combustivelRestante;
	private String desgasteMotor;
	private int numVoltas;
	private int pontos;
	@ManyToOne
	@JoinColumn(nullable = false)
	private CorridaCampeonatoSrv corridaCampeonato;

	public CorridaCampeonatoSrv getCorridaCampeonato() {
		return corridaCampeonato;
	}

	public Long getJogador() {
		return jogador;
	}

	public void setJogador(Long jogador) {
		this.jogador = jogador;
	}

	public void setCorridaCampeonato(CorridaCampeonatoSrv corridaCampeonato) {
		this.corridaCampeonato = corridaCampeonato;
	}

	public String getTpPneu() {
		return tpPneu;
	}

	public void setTpPneu(String tpPneu) {
		this.tpPneu = tpPneu;
	}

	public String getVoltaMaisRapida() {
		return voltaMaisRapida;
	}

	public void setVoltaMaisRapida(String voltaMaisRapida) {
		this.voltaMaisRapida = voltaMaisRapida;
	}

	public int getQtdeParadasBox() {
		return qtdeParadasBox;
	}

	public void setQtdeParadasBox(int qtdeParadasBox) {
		this.qtdeParadasBox = qtdeParadasBox;
	}

	public String getDesgastePneus() {
		return desgastePneus;
	}

	public void setDesgastePneus(String desgastePneus) {
		this.desgastePneus = desgastePneus;
	}

	public String getCombustivelRestante() {
		return combustivelRestante;
	}

	public void setCombustivelRestante(String combustivelRestante) {
		this.combustivelRestante = combustivelRestante;
	}

	public String getDesgasteMotor() {
		return desgasteMotor;
	}

	public void setDesgasteMotor(String desgasteMotor) {
		this.desgasteMotor = desgasteMotor;
	}

	public int getNumVoltas() {
		return numVoltas;
	}

	public void setNumVoltas(int numVoltas) {
		this.numVoltas = numVoltas;
	}

	public int getPontos() {
		return pontos;
	}

	public void setPontos(int pontos) {
		this.pontos = pontos;
	}

	public String getCarro() {
		return carro;
	}

	public void setCarro(String carro) {
		this.carro = carro;
	}

	public String getPiloto() {
		return piloto;
	}

	public void setPiloto(String piloto) {
		this.piloto = piloto;
	}

	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

	public String getCorPiloto() {
		return corPiloto;
	}

	public void setCorPiloto(String corPiloto) {
		this.corPiloto = corPiloto;
	}

}
