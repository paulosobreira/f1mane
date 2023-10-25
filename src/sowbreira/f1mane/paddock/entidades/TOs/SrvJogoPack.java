package sowbreira.f1mane.paddock.entidades.TOs;

import java.io.Serializable;

/**
 * @author Paulo Sobreira Criado em 05/08/2007 as 18:07:28
 */
public class SrvJogoPack implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6153734460672276818L;
	private String estadoJogo;
	private boolean agressivo;
	private boolean box;

	public boolean isAgressivo() {
		return agressivo;
	}

	public void setAgressivo(boolean agressivo) {
		this.agressivo = agressivo;
	}

	public String getEstadoJogo() {
		return estadoJogo;
	}

	public void setEstadoJogo(String estadoJogo) {
		this.estadoJogo = estadoJogo;
	}

	public boolean isBox() {
		return box;
	}

	public void setBox(boolean box) {
		this.box = box;
	}

}
