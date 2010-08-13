package sowbreira.f1mane.entidades;

import java.awt.Graphics2D;
import java.io.Serializable;

public abstract class ObjetoPista implements Serializable {

	boolean pintaEmcima;
	int corPimaria;
	int corSecundaria;
	int transparencia;
	double angulo;

	public abstract void desenha(Graphics2D g2d, double zoom);

	public double getAngulo() {
		return angulo;
	}

	public void setAngulo(double angulo) {
		this.angulo = angulo;
	}

	public boolean isPintaEmcima() {
		return pintaEmcima;
	}

	public void setPintaEmcima(boolean pintaEmcima) {
		this.pintaEmcima = pintaEmcima;
	}

	public int getCorPimaria() {
		return corPimaria;
	}

	public void setCorPimaria(int corPimaria) {
		this.corPimaria = corPimaria;
	}

	public int getCorSecundaria() {
		return corSecundaria;
	}

	public void setCorSecundaria(int corSecundaria) {
		this.corSecundaria = corSecundaria;
	}

	public int getTransparencia() {
		return transparencia;
	}

	public void setTransparencia(int transparencia) {
		this.transparencia = transparencia;
	}

}
