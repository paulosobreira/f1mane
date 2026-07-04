package br.f1mane.entidades;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Guard rails: um misto de {@link ObjetoArquibancada} (linhas finas repetidas)
 * com {@link ObjetoLivre} (desenhado clicando ponto a ponto no editor, botão
 * direito finaliza) — é um encadeamento de vetores de guardrails: cada par de
 * pontos consecutivos vira um segmento reto de barreira, e o padrão de
 * linhas finas percorre a extensão inteira do encadeamento continuamente
 * (não reinicia a cada segmento/vértice).
 */
public class ObjetoGuardRails extends ObjetoDesenho {

	/**
	 * Espessura de cada linha, em unidades de mundo (antes do zoom) — bem
	 * mais fina que a da arquibancada (10px). 1px é o mínimo possível.
	 */
	private static final int LARGURA_LINHA = 1;
	/** Vão entre linhas consecutivas — mesma proporção 1:1 da arquibancada. */
	private static final int VAO_ENTRE_LINHAS = LARGURA_LINHA;

	/**
	 * Pontos clicados pelo usuário no editor (um por clique esquerdo,
	 * finalizado com o direito), na ordem em que formam o encadeamento —
	 * mesma ideia de {@link ObjetoLivre#getPontos()}, mas sem curva: os
	 * segmentos entre pontos consecutivos são sempre retos.
	 */
	private List<Point> pontos = new ArrayList<Point>();
	private GeneralPath caminho = new GeneralPath();
	private OrientacaoGuardRails orientacao = OrientacaoGuardRails.VERTICAL;

	public ObjetoGuardRails() {
		setLargura(2);
		setCorPimaria(new Color(220, 220, 220));
		setCorSecundaria(new Color(220, 220, 220));
		setTransparencia(255);
	}

	public List<Point> getPontos() {
		return pontos;
	}

	public void setPontos(List<Point> pontos) {
		this.pontos = pontos;
	}

	public OrientacaoGuardRails getOrientacao() {
		return orientacao;
	}

	public void setOrientacao(OrientacaoGuardRails orientacao) {
		this.orientacao = orientacao != null ? orientacao : OrientacaoGuardRails.VERTICAL;
	}

	/**
	 * Reconstrói {@link #caminho} a partir de {@link #pontos} — chamado
	 * explicitamente ao finalizar a criação no editor (como
	 * {@link ObjetoLivre#gerar()}), e automaticamente por {@link #desenha}
	 * na primeira vez (após recarregar de um XML, por exemplo, já que
	 * {@code caminho} não é bean property e não é persistido).
	 */
	public void gerar() {
		GeneralPath path = new GeneralPath();
		if (!pontos.isEmpty()) {
			Point primeiro = pontos.get(0);
			path.moveTo(primeiro.x, primeiro.y);
			for (int i = 1; i < pontos.size(); i++) {
				Point p = pontos.get(i);
				path.lineTo(p.x, p.y);
			}
		}
		caminho = path;
	}

	@Override
	public void desenha(Graphics2D g2d, double zoom) {
		if (caminho.getCurrentPoint() == null) {
			gerar();
		}
		if (posicaoQuina != null) {
			Rectangle bounds = caminho.getBounds();
			AffineTransform translacao = AffineTransform.getTranslateInstance(
					posicaoQuina.x - bounds.x, posicaoQuina.y - bounds.y);
			caminho.transform(translacao);
		}

		List<Point2D> vertices = extrairVertices(caminho);
		if (vertices.size() < 2) {
			return;
		}

		AffineTransform escala = AffineTransform.getScaleInstance(zoom, zoom);

		Shape formaExterna = new BasicStroke(Math.max(1, largura), BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)
				.createStrokedShape(caminho);
		g2d.setColor(new Color(getCorPimaria().getRed(), getCorPimaria()
				.getGreen(), getCorPimaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(formaExterna));

		// Linhas finas encadeadas do começo ao fim de TODO o percurso (soma
		// de todos os segmentos), não de cada segmento isoladamente: a
		// quantidade é a máxima que cabe no período-alvo linha+vão, e o vão
		// é recalculado pra essa quantidade de linhas encostar exatamente no
		// início e no final do percurso — nunca sobra vão só no final,
		// diferente da arquibancada (que usa período fixo e pode deixar
		// sobra).
		GeneralPath linhas = construirLinhas(vertices);
		g2d.setColor(new Color(getCorSecundaria().getRed(), getCorSecundaria()
				.getGreen(), getCorSecundaria().getBlue(), getTransparencia()));
		g2d.fill(escala.createTransformedShape(linhas));
	}

	private static List<Point2D> extrairVertices(GeneralPath caminho) {
		List<Point2D> vertices = new ArrayList<Point2D>();
		double[] coords = new double[6];
		for (PathIterator it = caminho.getPathIterator(null); !it.isDone(); it.next()) {
			int tipo = it.currentSegment(coords);
			if (tipo == PathIterator.SEG_MOVETO || tipo == PathIterator.SEG_LINETO) {
				vertices.add(new Point2D.Double(coords[0], coords[1]));
			}
		}
		return vertices;
	}

	private GeneralPath construirLinhas(List<Point2D> vertices) {
		int quantidadeSegmentos = vertices.size() - 1;
		double[] comprimentoSegmento = new double[quantidadeSegmentos];
		double comprimentoTotal = 0;
		for (int i = 0; i < quantidadeSegmentos; i++) {
			comprimentoSegmento[i] = vertices.get(i).distance(vertices.get(i + 1));
			comprimentoTotal += comprimentoSegmento[i];
		}

		int periodoAlvo = LARGURA_LINHA + VAO_ENTRE_LINHAS;
		int quantidadeLinhas = Math.max(1, (int) (comprimentoTotal / periodoAlvo));
		double vaoReal = quantidadeLinhas > 1
				? (comprimentoTotal - quantidadeLinhas * LARGURA_LINHA) / (quantidadeLinhas - 1)
				: 0;
		double periodoReal = LARGURA_LINHA + vaoReal;

		GeneralPath linhas = new GeneralPath();
		int segmentoAtual = 0;
		double inicioSegmentoAtual = 0;
		for (int i = 0; i < quantidadeLinhas; i++) {
			double distanciaAlvo = i * periodoReal;
			while (segmentoAtual < quantidadeSegmentos - 1
					&& distanciaAlvo > inicioSegmentoAtual + comprimentoSegmento[segmentoAtual]) {
				inicioSegmentoAtual += comprimentoSegmento[segmentoAtual];
				segmentoAtual++;
			}
			Point2D p1 = vertices.get(segmentoAtual);
			Point2D p2 = vertices.get(segmentoAtual + 1);
			double compSeg = comprimentoSegmento[segmentoAtual];
			double t = compSeg == 0 ? 0 : (distanciaAlvo - inicioSegmentoAtual) / compSeg;
			double cx = p1.getX() + (p2.getX() - p1.getX()) * t;
			double cy = p1.getY() + (p2.getY() - p1.getY()) * t;
			double dx = compSeg == 0 ? 1 : (p2.getX() - p1.getX()) / compSeg;
			double dy = compSeg == 0 ? 0 : (p2.getY() - p1.getY()) / compSeg;
			adicionaTick(linhas, cx, cy, dx, dy);
		}
		return linhas;
	}

	/**
	 * Acrescenta em {@code destino} um retângulo centrado em {@code cx,cy},
	 * com {@link #largura} (a espessura da barreira) num eixo e
	 * {@link #LARGURA_LINHA} no outro — qual eixo é qual depende de
	 * {@link #orientacao}: {@code VERTICAL} (padrão) usa o vetor
	 * perpendicular ao segmento ({@code nx,ny}) como eixo comprido, formando
	 * barras cruzando o percurso; {@code HORIZONTAL} usa o próprio vetor do
	 * segmento ({@code dx,dy}) como eixo comprido, formando traços ao longo
	 * dele. Construído por vetores em vez de {@code AffineTransform.rotate}
	 * porque cada marca pode ter uma direção diferente (segmentos do
	 * encadeamento nem sempre têm o mesmo ângulo).
	 */
	private void adicionaTick(GeneralPath destino, double cx, double cy, double dx, double dy) {
		double nx = -dy;
		double ny = dx;
		double eixoLongoX = orientacao == OrientacaoGuardRails.HORIZONTAL ? dx : nx;
		double eixoLongoY = orientacao == OrientacaoGuardRails.HORIZONTAL ? dy : ny;
		double eixoCurtoX = orientacao == OrientacaoGuardRails.HORIZONTAL ? nx : dx;
		double eixoCurtoY = orientacao == OrientacaoGuardRails.HORIZONTAL ? ny : dy;
		double meiaLargura = largura / 2.0;
		double meiaEspessura = LARGURA_LINHA / 2.0;
		destino.moveTo(cx + eixoLongoX * meiaLargura + eixoCurtoX * meiaEspessura,
				cy + eixoLongoY * meiaLargura + eixoCurtoY * meiaEspessura);
		destino.lineTo(cx - eixoLongoX * meiaLargura + eixoCurtoX * meiaEspessura,
				cy - eixoLongoY * meiaLargura + eixoCurtoY * meiaEspessura);
		destino.lineTo(cx - eixoLongoX * meiaLargura - eixoCurtoX * meiaEspessura,
				cy - eixoLongoY * meiaLargura - eixoCurtoY * meiaEspessura);
		destino.lineTo(cx + eixoLongoX * meiaLargura - eixoCurtoX * meiaEspessura,
				cy + eixoLongoY * meiaLargura - eixoCurtoY * meiaEspessura);
		destino.closePath();
	}

	@Override
	public Rectangle obterArea() {
		return caminho.getBounds();
	}
}
