package sowbreira.f1mane.entidades;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SafetyCar implements Serializable {

	private No noAtual = new No();
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

}
