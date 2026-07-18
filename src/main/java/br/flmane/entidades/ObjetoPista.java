package br.flmane.entidades;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.Serializable;

public abstract class ObjetoPista implements Serializable {

	private static final long serialVersionUID = 4416705642227491612L;
	/** Margem de tolerância (px de tela) somada à área de clique, para facilitar acertar objetos finos. */
	protected static final int TOLERANCIA_CLIQUE_PX = 6;
	boolean pintaEmcima;
	/**
	 * Nível de desenho em relação à pista (que está no nível 0): valores
	 * negativos desenham abaixo do asfalto (quanto mais negativo, mais no
	 * fundo), 0 é logo acima dele (padrão, comportamento antigo) e valores
	 * positivos desenham por cima, na ordem crescente (quanto maior, mais em
	 * cima). Sem limite — qualquer inteiro é válido.
	 */
	int nivelDesenho;
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
		return getNome() + " " + getClass().getSimpleName() + " (" + nivelDesenho + ")";
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
	 * Área "real" ocupada visualmente pelo objeto — por padrão igual a
	 * {@link #obterArea()}, mas subtipos cuja área bruta não reflete a
	 * espessura/extensão real do desenho (ex.: {@code ObjetoArquibancada},
	 * desenhado por um encadeamento de pontos onde {@code obterArea()} é só
	 * o retângulo da centerline, sem a largura do lance) sobrescrevem este
	 * método. Base tanto de {@link #obterAreaClique()} quanto do contorno
	 * visual de seleção desenhado no editor — as duas coisas precisam
	 * coincidir com o que é realmente desenhado, não com o retângulo bruto
	 * que {@code obterArea()} guarda para fins de posicionamento/lógica de
	 * jogo.
	 */
	public Rectangle obterAreaVisual() {
		return obterArea();
	}

	/**
	 * Área usada para detectar clique/seleção no editor: como
	 * {@link #obterAreaVisual()}, mas com uma margem de tolerância e
	 * considerando a rotação ({@code angulo}) em torno do próprio centro,
	 * para coincidir com a forma realmente desenhada (que é rotacionada só
	 * na hora de pintar, não no retângulo bruto que cada subtipo guarda).
	 * {@code obterArea()} continua retornando o retângulo sem rotação nem
	 * expansão porque também é usado por lógica de jogo (ex.: máscara de
	 * transparência do box), que não deve mudar de comportamento.
	 */
	public Rectangle obterAreaClique() {
		Rectangle base = obterAreaVisual();
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
		// Ponte com XMLs antigos, que só têm pintaEmcima: true equivalia a
		// desenhar por cima de tudo (nível 1). Mantém os dois campos
		// coerentes também quando o setter é chamado por código novo.
		if (pintaEmcima && nivelDesenho < 1) {
			nivelDesenho = 1;
		} else if (!pintaEmcima && nivelDesenho > 0) {
			nivelDesenho = 0;
		}
	}

	public int getNivelDesenho() {
		return nivelDesenho;
	}

	/** Sem limite de faixa; mantém {@code pintaEmcima} coerente (nível >= 1 = por cima). */
	public void setNivelDesenho(int nivelDesenho) {
		this.nivelDesenho = nivelDesenho;
		this.pintaEmcima = nivelDesenho >= 1;
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
