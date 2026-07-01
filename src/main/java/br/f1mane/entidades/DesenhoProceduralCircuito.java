package br.f1mane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;

import br.nnpe.Util;

/**
 * Desenho procedural do circuito (traçado da pista/zebra/box e objetos de
 * cenário), extraído do editor de circuitos (br.f1mane.editor.MainPanelEditor,
 * modo "sem imagem de fundo") para ser reutilizável fora do editor — em
 * particular, para gerar em memória a imagem de fundo usada em corrida, no
 * lugar do arquivo estático circuitos/*_mro.jpg.
 */
public final class DesenhoProceduralCircuito {

	/** Cor padrão do asfalto, usada quando {@code Circuito.corAsfalto} não foi definida. */
	public static final Color COR_PISTA = new Color(192, 192, 192);
	private static final int MARGEM_IMAGEM = 500;

	private DesenhoProceduralCircuito() {
	}

	/**
	 * Desenha pista, zebra e box do circuito no {@code Graphics2D} informado,
	 * na escala {@code zoom} — sem os objetos de cenário. É o mesmo desenho
	 * usado pelo editor de circuitos quando está em modo "sem imagem de
	 * fundo" (não confundir com {@link #desenha}, que inclui os objetos e é
	 * usado para gerar a imagem de fundo da corrida).
	 */
	public static void desenhaPistaZebraEBox(Graphics2D g2d, Circuito circuito, double zoom) {
		int larguraPistaPixeis = Util.inteiro(Carro.LARGURA * 1.5
				* circuito.getMultiplicadorLarguraPista() * zoom);
		BasicStroke pista = new BasicStroke(larguraPistaPixeis, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		BasicStroke pistaTinta = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke box = new BasicStroke(Util.inteiro(larguraPistaPixeis * .4),
				BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke zebra = new BasicStroke(Util.inteiro(larguraPistaPixeis * 1.05),
				BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{10, 10}, 0);

		desenhaTintaPistaEZebra(g2d, circuito, zoom, pistaTinta, zebra);
		desenhaPista(g2d, circuito, zoom, pista);
		desenhaPistaBox(g2d, circuito, zoom, box);
	}

	/**
	 * Desenha pista, zebra, box e objetos de cenário (Circuito.objetosCenario)
	 * do circuito no {@code Graphics2D} informado, na escala {@code zoom}.
	 * {@code Circuito.objetos} (Escapada/Transparencia) não faz parte deste
	 * desenho — esses dois já têm tratamento próprio em tempo real (debug e
	 * máscara de boxes, respectivamente) e não devem ficar gravados de forma
	 * permanente na imagem.
	 */
	public static void desenha(Graphics2D g2d, Circuito circuito, double zoom) {
		desenhaPistaZebraEBox(g2d, circuito, zoom);
		desenhaObjetos(g2d, circuito, zoom);
	}

	/**
	 * Gera, em memória, a imagem de fundo do circuito na escala real
	 * (zoom = 1), com dimensões calculadas a partir dos limites dos nós de
	 * pista e box. Requer que {@code circuito.vetorizarPista(...)} já tenha
	 * sido chamado (pistaKey/boxKey populados).
	 */
	public static BufferedImage geraImagem(Circuito circuito) {
		int maxX = 0;
		int maxY = 0;
		for (No no : circuito.getPistaKey()) {
			maxX = Math.max(maxX, no.getX());
			maxY = Math.max(maxY, no.getY());
		}
		for (No no : circuito.getBoxKey()) {
			maxX = Math.max(maxX, no.getX());
			maxY = Math.max(maxY, no.getY());
		}
		BufferedImage image = new BufferedImage(maxX + MARGEM_IMAGEM, maxY + MARGEM_IMAGEM,
				BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = image.createGraphics();
		Util.setarHints(g2d);
		g2d.setColor(circuito.getCorFundo() != null ? circuito.getCorFundo() : Color.WHITE);
		g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		desenha(g2d, circuito, 1.0);
		g2d.dispose();
		return image;
	}

	private static void desenhaPista(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke stroke) {
		g2d.setColor(circuito.getCorAsfalto() != null ? circuito.getCorAsfalto() : COR_PISTA);
		g2d.setStroke(stroke);
		No oldNo = null;
		for (Iterator<No> iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
			}
			oldNo = no;
		}
		No noFinal = circuito.getPistaKey().get(0);
		g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
				Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
	}

	private static void desenhaPistaBox(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke stroke) {
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.setStroke(stroke);
		No oldNo = null;
		for (Iterator<No> iter = circuito.getBoxKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
			}
			oldNo = no;
		}
		if (circuito.getBoxKey() != null && !circuito.getBoxKey().isEmpty()) {
			No noFinal = circuito.getBoxKey().get(circuito.getBoxKey().size() - 1);
			g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
					Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
		}
	}

	private static void desenhaTintaPistaEZebra(Graphics2D g2d, Circuito circuito, double zoom,
			BasicStroke pistaTinta, BasicStroke zebra) {
		No oldNo = null;
		for (Iterator<No> iter = circuito.getPistaKey().iterator(); iter.hasNext(); ) {
			No no = iter.next();
			if (oldNo != null) {
				g2d.setColor(Color.WHITE);
				g2d.setStroke(pistaTinta);
				g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
						Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
				if (No.CURVA_ALTA.equals(oldNo.getTipo()) || No.CURVA_BAIXA.equals(oldNo.getTipo())) {
					g2d.setColor(Color.RED);
					g2d.setStroke(zebra);
					g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
							Util.inteiro(no.getX() * zoom), Util.inteiro(no.getY() * zoom));
				}
			}
			oldNo = no;
		}
		No noFinal = circuito.getPistaKey().get(0);
		g2d.setColor(Color.WHITE);
		g2d.setStroke(pistaTinta);
		g2d.drawLine(Util.inteiro(oldNo.getX() * zoom), Util.inteiro(oldNo.getY() * zoom),
				Util.inteiro(noFinal.getX() * zoom), Util.inteiro(noFinal.getY() * zoom));
	}

	private static void desenhaObjetos(Graphics2D g2d, Circuito circuito, double zoom) {
		if (circuito.getObjetosCenario() == null) {
			return;
		}
		for (ObjetoPista objetoPista : circuito.getObjetosCenario()) {
			objetoPista.desenha(g2d, zoom);
		}
	}
}
