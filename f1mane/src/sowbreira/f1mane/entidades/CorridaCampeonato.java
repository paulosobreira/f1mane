package sowbreira.f1mane.entidades;

import java.io.Serializable;

import br.nnpe.Util;

import sowbreira.f1mane.controles.InterfaceJogo;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:47:15
 */
public class CorridaCampeonato implements Serializable {

	private int posicao;
	private String piloto;
	private String carro;
	private String tpPneu;
	private String voltaMaisRapida;
	private int qtdeParadasBox;
	private String desgastePneus;
	private String combustivelRestante;
	private String desgasteMotor;
	private int numVoltas;
	private int pontos;
	private long tempoInicio, tempoFim;

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

	public long getTempoFim() {
		return tempoFim;
	}

	public void setTempoFim(long tempoFim) {
		this.tempoFim = tempoFim;
	}

	public long getTempoInicio() {
		return tempoInicio;
	}

	public void setTempoInicio(long tempoInicio) {
		this.tempoInicio = tempoInicio;
	}

	public int getPosicao() {
		return posicao;
	}

	public void setPosicao(int posicao) {
		this.posicao = posicao;
	}

}
