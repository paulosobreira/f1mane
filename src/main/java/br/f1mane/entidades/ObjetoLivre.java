package br.f1mane.entidades;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ObjetoLivre extends ObjetoPista {
	/** Tamanho (em unidades de mundo, antes do zoom) da célula da grade do padrão de preenchimento. */
	private static final int PASSO_PADRAO_LOCAL = Carro.ALTURA;
	/** Semente fixa: a dispersão da brita é "aleatória" mas sempre a mesma entre renderizações. */
	private static final long SEMENTE_BRITA = 20260703L;

	/**
	 * Campo legado (polígono de linhas retas), mantido apenas para leitura de
	 * circuitos XML existentes. Novos objetos e edições passam a usar
	 * {@link #vertices}; ver {@link #obterVerticesEfetivos()}.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	private List<PontoCurva> vertices = new ArrayList<PontoCurva>();
	private TipoObjetoLivre tipo = TipoObjetoLivre.POLIGONO_SIMPLES;
	GeneralPath generalPath = new GeneralPath();

	public ObjetoLivre() {
		setCorPimaria(new Color(120, 120, 120));
		setCorSecundaria(new Color(60, 60, 60));
		setTransparencia(255);
	}

	public TipoObjetoLivre getTipo() {
		return tipo;
	}

	public void setTipo(TipoObjetoLivre tipo) {
		this.tipo = tipo != null ? tipo : TipoObjetoLivre.POLIGONO_SIMPLES;
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public List<PontoCurva> getVertices() {
		return vertices;
	}

	public void setVertices(List<PontoCurva> vertices) {
		this.vertices = vertices;
	}

	/**
	 * {@code vertices} se não estiver vazio; caso contrário, sintetiza uma
	 * lista equivalente a partir do {@code pontos} legado (haste nula em cada
	 * vértice, ou seja, segmentos retos) sem alterar o estado do objeto — a
	 * migração efetiva de {@code pontos} para {@code vertices} só acontece
	 * quando o objeto é editado (ver {@link #inicializarVerticesSeNecessario()}).
	 */
	private List<PontoCurva> obterVerticesEfetivos() {
		if (!vertices.isEmpty()) {
			return vertices;
		}
		List<PontoCurva> sintetizados = new ArrayList<PontoCurva>();
		for (Point ponto : pontos) {
			sintetizados.add(new PontoCurva(new Point(ponto)));
		}
		return sintetizados;
	}

	/**
	 * Copia {@code pontos} (legado) para {@code vertices} se este ainda
	 * estiver vazio, tornando {@code vertices} a fonte de verdade a partir
	 * daqui — chamado pelo editor ao iniciar a edição de pontos/hastes de um
	 * objeto legado.
	 */
	public void inicializarVerticesSeNecessario() {
		if (!vertices.isEmpty() || pontos.isEmpty()) {
			return;
		}
		for (Point ponto : pontos) {
			vertices.add(new PontoCurva(new Point(ponto)));
		}
	}

	public void gerar() {
		List<PontoCurva> pts = obterVerticesEfetivos();
		GeneralPath path = new GeneralPath();
		if (!pts.isEmpty()) {
			Point primeiro = pts.get(0).getPosicao();
			path.moveTo(primeiro.x, primeiro.y);
			for (int i = 0; i < pts.size(); i++) {
				PontoCurva atual = pts.get(i);
				PontoCurva proximo = pts.get((i + 1) % pts.size());
				Point controleSaida = atual.getControleSaida();
				Point controleEntrada = proximo.getControleEntrada();
				Point destino = proximo.getPosicao();
				path.curveTo(controleSaida.x, controleSaida.y, controleEntrada.x, controleEntrada.y,
						destino.x, destino.y);
			}
			path.closePath();
		}
		generalPath = path;
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		if (posicaoQuina != null) {
			Rectangle bounds = generalPath.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			generalPath.transform(translacao);
		}

		double rad = Math.toRadians((double) getAngulo());
		AffineTransform affineTransform = AffineTransform
				.getScaleInstance(1, 1);
		affineTransform.setToRotation(rad, generalPath.getBounds().getCenterX(),
				generalPath.getBounds().getCenterY());
		GeneralPath pathRotacionado = new GeneralPath(generalPath);
		pathRotacionado.transform(affineTransform);
		affineTransform.setToScale(zoom, zoom);
		Shape formaFinal = pathRotacionado.createTransformedShape(affineTransform);

		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(formaFinal);

		if (tipo == TipoObjetoLivre.BRITA) {
			desenhaBrita(g2d, formaFinal, zoom);
		} else if (tipo != TipoObjetoLivre.POLIGONO_SIMPLES) {
			desenhaPadraoEmGrade(g2d, formaFinal, zoom);
		}
	}

	/**
	 * Sobrepõe, restrito à área da forma, um padrão procedural simples e
	 * determinístico (grade de passo fixo, sem aleatoriedade) característico
	 * do {@link #tipo}, usando {@code corSecundaria}. Usado por todos os
	 * tipos exceto {@link TipoObjetoLivre#BRITA} (ver {@link #desenhaBrita}).
	 */
	private void desenhaPadraoEmGrade(Graphics2D g2d, Shape formaFinal, double zoom) {
		Shape clipAnterior = g2d.getClip();
		g2d.clip(formaFinal);
		Rectangle bounds = formaFinal.getBounds();
		// Vegetação densa usa uma grade mais apertada (metade do passo padrão,
		// ~4x mais marcas por área) que os demais tipos desta grade — o desenho
		// em cruz sozinho ficava com espaço demais entre touceiras.
		double fatorDensidade = tipo == TipoObjetoLivre.VEGETACAO_DENSA ? 0.5 : 1.0;
		int passo = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * fatorDensidade * zoom));
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
				getCorSecundaria().getBlue(), getTransparencia()));
		int linha = 0;
		for (int y = bounds.y; y < bounds.y + bounds.height; y += passo) {
			int deslocamentoLinha = (linha % 2 == 0) ? 0 : passo / 2;
			for (int x = bounds.x - passo; x < bounds.x + bounds.width + passo; x += passo) {
				desenhaPrimitivaPadrao(g2d, x + deslocamentoLinha, y + passo / 2, passo);
			}
			linha++;
		}
		g2d.setClip(clipAnterior);
	}

	private void desenhaPrimitivaPadrao(Graphics2D g2d, int cx, int cy, int passo) {
		int raio = Math.max(2, passo / 5);
		switch (tipo) {
		case VEGETACAO_SIMPLES:
			g2d.drawLine(cx - raio, cy + raio, cx + raio, cy - raio);
			break;
		case VEGETACAO_DENSA:
			g2d.drawLine(cx - raio, cy, cx + raio, cy);
			g2d.drawLine(cx, cy - raio, cx, cy + raio);
			g2d.drawLine(cx - raio, cy + raio, cx, cy - raio / 2);
			break;
		case AGUA:
			g2d.drawArc(cx - raio * 2, cy - raio, raio * 4, raio * 2, 0, 180);
			break;
		default:
			break;
		}
	}

	/**
	 * Brita: pontos pequenos espalhados em posições pseudo-aleatórias (não
	 * alinhados em grade) e em densidade bem maior que os outros padrões. A
	 * semente é fixa e a ordem de varredura é sempre a mesma, então o
	 * resultado é determinístico entre renderizações sucessivas.
	 */
	private void desenhaBrita(Graphics2D g2d, Shape formaFinal, double zoom) {
		Shape clipAnterior = g2d.getClip();
		g2d.clip(formaFinal);
		Rectangle bounds = formaFinal.getBounds();
		int passoBase = Math.max(4, (int) Math.round(PASSO_PADRAO_LOCAL * zoom));
		int celula = Math.max(2, passoBase / 4);
		int diametro = Math.max(1, celula / 4);
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria().getGreen(),
				getCorSecundaria().getBlue(), getTransparencia()));
		Random random = new Random(SEMENTE_BRITA);
		for (int y = bounds.y - celula; y < bounds.y + bounds.height + celula; y += celula) {
			for (int x = bounds.x - celula; x < bounds.x + bounds.width + celula; x += celula) {
				int deslocX = random.nextInt(celula);
				int deslocY = random.nextInt(celula);
				g2d.fillOval(x + deslocX, y + deslocY, diametro, diametro);
			}
		}
		g2d.setClip(clipAnterior);
	}

	@Override
	public Rectangle obterArea() {
		return generalPath.getBounds();
	}

	/** Forma vetorial atual (após {@link #gerar()}), pública para inspeção/edição no editor. */
	public Shape getForma() {
		return generalPath;
	}
}
