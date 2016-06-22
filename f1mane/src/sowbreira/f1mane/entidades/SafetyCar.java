package sowbreira.f1mane.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SafetyCar implements Serializable, PilotoSuave {

	private No noAtual = new No();
	private No noAnterior = new No();
	private No noAtualSuave;
	private int ganhoSuave;
	private long ptosPista;
	private int saiuVolta;
	private int tracado;
	private boolean vaiProBox;
	private boolean naPista;
	private boolean esperando;
	private List mediaSc = new ArrayList();

	public boolean isEsperando() {
		return esperando;
	}

	public void setEsperando(boolean esperando) {
		this.esperando = esperando;
	}

	public int getTracado() {
		return tracado;
	}

	public void setTracado(int tracado) {
		this.tracado = tracado;
	}

	public List getMediaSc() {
		return mediaSc;
	}

	public boolean isNaPista() {
		return naPista;
	}

	public int getSaiuVolta() {
		return saiuVolta;
	}

	public void setSaiuVolta(int saiuVolta) {
		this.saiuVolta = saiuVolta;
	}

	public void setNaPista(boolean naPista) {
		this.naPista = naPista;
	}

	public SafetyCar() {
	}

	public No getNoAtual() {
		return noAtual;
	}

	public void setNoAtual(No noAtual) {
		noAnterior = this.noAtual; 
		this.noAtual = noAtual;
	}

	public long getPtosPista() {
		return ptosPista;
	}

	public void setPtosPista(long ptosPista) {
		this.ptosPista = ptosPista;
	}

	public boolean isVaiProBox() {
		return vaiProBox;
	}

	public void setVaiProBox(boolean vaiProBox) {
		this.vaiProBox = vaiProBox;
	}

	@Override
	public No getNoAtualSuave() {
		return this.noAtualSuave;
	}

	@Override
	public int getGanhoSuave() {
		return this.ganhoSuave;
	}

	@Override
	public void setGanhoSuave(int ganhoSuave) {
		this.ganhoSuave = ganhoSuave;

	}

	@Override
	public void setNoAtualSuave(No noAtualSuave) {
		this.noAtualSuave = noAtualSuave;

	}

	@Override
	public No getNoAnterior() {
		return noAnterior;
	}

}
