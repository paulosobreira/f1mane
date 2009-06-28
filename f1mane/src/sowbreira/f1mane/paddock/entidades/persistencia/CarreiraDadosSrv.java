package sowbreira.f1mane.paddock.entidades.persistencia;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 27/06/2009 as 23:01:35
 */
public class CarreiraDadosSrv implements Serializable {

	private int ptsConstrutores;
	private int ptsPiloto;
	private int ptsCarro;
	private String nomePiloto;
	private String nomeCarro;
	private boolean modoCarreira;

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

}
