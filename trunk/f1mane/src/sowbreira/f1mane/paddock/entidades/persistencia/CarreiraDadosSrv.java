package sowbreira.f1mane.paddock.entidades.persistencia;

import java.awt.Color;
import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import org.hibernate.annotations.ForeignKey;

/**
 * @author Paulo Sobreira Criado em 27/06/2009 as 23:01:35
 */
@Entity
public class CarreiraDadosSrv extends F1ManeDados implements Serializable {

	private int ptsConstrutores;
	private int ptsPiloto;
	private int ptsCarro;
	private String nomePiloto;
	private String nomeCarro;
	private boolean modoCarreira;
	private int c1R;
	private int c1G;
	private int c1B;
	private int c2R;
	private int c2G;
	private int c2B;
	@OneToOne
	@JoinColumn(nullable = false)
	private JogadorDadosSrv jogadorDadosSrv;

	public Color geraCor1() {
		return new Color(c1R, c1G, c1B);

	}

	public Color geraCor2() {
		return new Color(c2R, c2G, c2B);

	}

	public int getPtsConstrutores() {
		return ptsConstrutores;
	}

	public void setPtsConstrutores(int ptsConstrutores) {
		this.ptsConstrutores = ptsConstrutores;
	}

	public int getPtsPiloto() {
		return ptsPiloto;
	}

	public void setPtsPiloto(int ptsPiloto) {
		this.ptsPiloto = ptsPiloto;
	}

	public int getPtsCarro() {
		return ptsCarro;
	}

	public void setPtsCarro(int ptsCarro) {
		this.ptsCarro = ptsCarro;
	}

	public String getNomePiloto() {
		return nomePiloto;
	}

	public void setNomePiloto(String nomePiloto) {
		this.nomePiloto = nomePiloto;
	}

	public String getNomeCarro() {
		return nomeCarro;
	}

	public void setNomeCarro(String nomeCarro) {
		this.nomeCarro = nomeCarro;
	}

	public boolean isModoCarreira() {
		return modoCarreira;
	}

	public void setModoCarreira(boolean modoCarreira) {
		this.modoCarreira = modoCarreira;
	}

	public int getC1R() {
		return c1R;
	}

	public void setC1R(int c1r) {
		c1R = c1r;
	}

	public int getC1G() {
		return c1G;
	}

	public void setC1G(int c1g) {
		c1G = c1g;
	}

	public int getC1B() {
		return c1B;
	}

	public void setC1B(int c1b) {
		c1B = c1b;
	}

	public int getC2R() {
		return c2R;
	}

	public void setC2R(int c2r) {
		c2R = c2r;
	}

	public int getC2G() {
		return c2G;
	}

	public void setC2G(int c2g) {
		c2G = c2g;
	}

	public int getC2B() {
		return c2B;
	}

	public void setC2B(int c2b) {
		c2B = c2b;
	}

	public JogadorDadosSrv getJogadorDadosSrv() {
		return jogadorDadosSrv;
	}

	public void setJogadorDadosSrv(JogadorDadosSrv jogadorDadosSrv) {
		this.jogadorDadosSrv = jogadorDadosSrv;
	}

}
