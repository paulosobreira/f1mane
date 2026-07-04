package br.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

public class ObjetoEscapada extends ObjetoPista {

	public final static Color red = new Color(250, 0, 0, 50);

	/**
	 * Largura e altura funcionam como as propriedades da onda do traçado de
	 * escapada (Circuito.gerarEscapeMap/preencheTracadoEscapeSuave): largura
	 * é o comprimento da zona de escapada e altura é a amplitude (crista),
	 * numa razão de 1:1 com os pixels do traçado (sem fator de escala
	 * escondido). Os valores padrão aproximam a mesma proporção
	 * comprimento:amplitude (~15:1) que o cálculo fixo anterior produzia.
	 */
	public ObjetoEscapada() {
		setLargura(600);
		setAltura(60);
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		GeneralPath generalPath = new GeneralPath(getExterno());
		affineTransform.setToRotation(rad,
				generalPath.getBounds().getCenterX(), generalPath.getBounds()
						.getCenterY());
		generalPath.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		g2d.setColor(red);
		g2d.fill(generalPath.createTransformedShape(affineTransform));
		g2d.setColor(Color.WHITE);
		g2d.fillOval(centro().x - 5, centro().y - 5, 10, 10);
	}

	@Override
	public Rectangle obterArea() {
		return getExterno().getBounds();
	}

	public Point centro() {
		if(posicaoQuina==null){
			return null;
		}
		return new Point(posicaoQuina.x + (getLargura() / 2), posicaoQuina.y
				+ (getAltura() / 2));
	}

	public Ellipse2D getExterno() {
		if(posicaoQuina==null){
			return null;
		}
		return new Ellipse2D.Double(getPosicaoQuina().x, getPosicaoQuina().y,
				getLargura(), getAltura());
	}

	public static void main(String[] args) {
	}

	/** Objeto de função (fica na listinha de baixo do editor): fora do sistema de níveis, sem sufixo "(nível)". */
	@Override
	public String toString() {
		return getNome() + " " + getClass().getSimpleName();
	}
}
