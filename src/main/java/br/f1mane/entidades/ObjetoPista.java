package br.f1mane.entidades;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

public abstract class ObjetoPista implements Serializable {

	private static final long serialVersionUID = 4416705642227491612L;
	/** Margem de tolerância (px de tela) somada à área de clique, para facilitar acertar objetos finos. */
	private static final int TOLERANCIA_CLIQUE_PX = 6;
	boolean pintaEmcima;
	Color corPimaria;
	Color corSecundaria;
	int transparencia;
	int altura;
	int largura;
	double angulo;
	Point posicaoQuina;
	private String nome;
	int inicioTransparencia;
	int fimTransparencia;
	boolean transparenciaBox;

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public String toString() {
		return getNome() + " " + getClass().getSimpleName();
	}

	public int getAltura() {
		return altura;
	}

	public void setAltura(int altura) {
		this.altura = altura;
	}

	public int getLargura() {
		return largura;
	}

	public void setLargura(int largura) {
		this.largura = largura;
	}

	public Point getPosicaoQuina() {
		return posicaoQuina;
	}

	public void setPosicaoQuina(Point posicaoQuina) {
		this.posicaoQuina = posicaoQuina;
	}

	public abstract void desenha(Graphics2D g2d, double zoom);

	public abstract Rectangle obterArea();

	/**
	 * Área usada para detectar clique/seleção no editor: como
	 * {@link #obterArea()}, mas com uma margem de tolerância e considerando
	 * a rotação ({@code angulo}) em torno do próprio centro, para coincidir
	 * com a forma realmente desenhada (que é rotacionada só na hora de
	 * pintar, não no retângulo bruto que cada subtipo guarda). {@code
	 * obterArea()} continua retornando o retângulo sem rotação porque
	 * também é usado por lógica de jogo (ex.: máscara de transparência do
	 * box), que não deve mudar de comportamento.
	 */
	public Rectangle obterAreaClique() {
		Rectangle base = obterArea();
		if (base == null) {
			return base;
		}
		Rectangle expandido = new Rectangle(base);
		expandido.grow(TOLERANCIA_CLIQUE_PX, TOLERANCIA_CLIQUE_PX);
		if (angulo == 0) {
			return expandido;
		}
		double rad = Math.toRadians(angulo);
		AffineTransform rotacao = AffineTransform.getRotateInstance(rad, expandido.getCenterX(),
				expandido.getCenterY());
		return rotacao.createTransformedShape(expandido).getBounds();
	}

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

	public Color getCorPimaria() {
		return corPimaria;
	}

	public void setCorPimaria(Color corPimaria) {
		this.corPimaria = corPimaria;
	}

	public Color getCorSecundaria() {
		return corSecundaria;
	}

	public void setCorSecundaria(Color corSecundaria) {
		this.corSecundaria = corSecundaria;
	}

	public int getTransparencia() {
		return transparencia;
	}

	public void setTransparencia(int transparencia) {
		if (transparencia < 0) {
			transparencia = 0;
		}
		if (transparencia > 255) {
			transparencia = 255;
		}
		this.transparencia = transparencia;
	}

	public int getInicioTransparencia() {
		return inicioTransparencia;
	}

	public void setInicioTransparencia(int inicioTransparencia) {
		this.inicioTransparencia = inicioTransparencia;
	}

	public int getFimTransparencia() {
		return fimTransparencia;
	}

	public void setFimTransparencia(int fimTransparencia) {
		this.fimTransparencia = fimTransparencia;
	}

	public boolean isTransparenciaBox() {
		return transparenciaBox;
	}

	public void setTransparenciaBox(boolean transparenciaBox) {
		this.transparenciaBox = transparenciaBox;
	}

}
