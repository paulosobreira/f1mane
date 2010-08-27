package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import br.nnpe.Util;

import sowbreira.f1mane.controles.InterfaceJogo;

/**
 * @author Paulo Sobreira Criado em 27/10/2007 as 18:47:15
 */
@Entity
public class DadosCorridaCampeonato extends F1ManeDados {

	private int posicao;
	private String piloto;
	private String jogador;
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
	private CorridaCampeonato corridaCampeonato;

	public CorridaCampeonato getCorridaCampeonato() {
		return corridaCampeonato;
	}

	public String getJogador() {
		return jogador;
	}

	public void setJogador(String jogador) {
		this.jogador = jogador;
	}

	public void setCorridaCampeonato(CorridaCampeonato corridaCampeonato) {
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

}
